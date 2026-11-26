package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.CustomProviderEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.EmochiRepository
import com.example.util.ApiErrorFormatter
import com.example.util.AppFeatureFlags
import com.example.util.CustomApiUrlNormalizer
import com.example.util.SecureApiKeyVault
import com.example.util.SseStreamParser
import kotlinx.coroutines.runBlocking
import okio.Buffer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.BufferedReader
import java.io.EOFException
import java.io.StringReader

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ApiStreamingAndCustomProviderOverhaulTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: EmochiRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = EmochiRepository(db, context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun readRequestBody(req: okhttp3.Request): String {
        val buf = Buffer()
        req.body?.writeTo(buf)
        return buf.readUtf8()
    }

    @Test
    fun test1_claudeNativeAndCustomAnthropicLiveSseRequestBuilding() {
        val pfm = repo.managers.providerFallbackManager
        val settings = UserSettingsEntity(claudeApiKey = "sk-ant-api03-test1234567890abcdef")
        val msgs = listOf(
            MessageEntity("m1", "b1", "assistant", "Hoş geldin.", 1000L),
            MessageEntity("m2", "b1", "user", "Selam", 2000L),
            MessageEntity("m3", "b1", "user", "Nasılsın?", 3000L)
        )

        val pair = pfm.buildNarrativeStreamHttpRequest(
            providerKey = "claude",
            model = "claude-sonnet-4-20250514",
            settings = settings,
            customEntity = null,
            buildConfigGeminiKey = "",
            systemPrompt = "Sen Natasha'sın.",
            messages = msgs
        )
        assertNotNull("Built-in Claude must now support native live SSE streaming", pair)
        val (req, fmt) = pair!!
        assertEquals(SseStreamParser.SseFormat.CLAUDE, fmt)
        assertEquals("https://api.anthropic.com/v1/messages", req.url.toString())
        assertEquals("2023-06-01", req.header("anthropic-version"))
        assertEquals("sk-ant-api03-test1234567890abcdef", req.header("x-api-key"))

        val bodyJson = JSONObject(readRequestBody(req))
        assertTrue(bodyJson.getBoolean("stream"))
        assertEquals("Sen Natasha'sın.", bodyJson.getString("system"))
        val arr = bodyJson.getJSONArray("messages")
        // Must start with user (prepended "..."), alternate assistant, and merge consecutive user messages
        assertEquals(3, arr.length())
        assertEquals("user", arr.getJSONObject(0).getString("role"))
        assertEquals("assistant", arr.getJSONObject(1).getString("role"))
        assertEquals("user", arr.getJSONObject(2).getString("role"))
        assertTrue(arr.getJSONObject(2).getString("content").contains("Selam"))
        assertTrue(arr.getJSONObject(2).getString("content").contains("Nasılsın?"))

        // Custom Anthropic proxy ending in /v1 must NOT duplicate /v1/v1/messages
        val customAnthropic = CustomProviderEntity(
            id = 5L,
            label = "Claude Proxy",
            baseUrl = "https://my-claude-proxy.example.com/v1",
            apiKeyEncrypted = SecureApiKeyVault.encrypt("sk-ant-proxy-9876543210abcdef"),
            modelName = "claude-3-7-sonnet-20250219",
            apiFormat = "anthropic"
        )
        val customPair = pfm.buildNarrativeStreamHttpRequest(
            providerKey = "custom_5",
            model = customAnthropic.modelName,
            settings = settings,
            customEntity = customAnthropic,
            buildConfigGeminiKey = "",
            systemPrompt = "Sistem",
            messages = msgs
        )
        assertNotNull(customPair)
        assertEquals("https://my-claude-proxy.example.com/v1/messages", customPair!!.first.url.toString())
        assertEquals(SseStreamParser.SseFormat.CLAUDE, customPair.second)
    }

    @Test
    fun test2_openAiReasoningModels_omitUnsupportedSamplingParamsAndUseMaxCompletionTokens() {
        val pfm = repo.managers.providerFallbackManager
        assertTrue(pfm.isOpenAiReasoningModel("o1"))
        assertTrue(pfm.isOpenAiReasoningModel("o3-mini"))
        assertTrue(pfm.isOpenAiReasoningModel("openai/o4-mini"))
        assertTrue(pfm.isOpenAiReasoningModel("gpt-5"))
        assertTrue(pfm.isOpenAiReasoningModel("gpt-5-mini"))
        assertFalse(pfm.isOpenAiReasoningModel("gpt-4o"))
        assertFalse(pfm.isOpenAiReasoningModel("deepseek-chat"))

        val settings = UserSettingsEntity(openaiApiKey = "sk-proj-test1234567890abcdef")
        val msgs = listOf(MessageEntity("u1", "b1", "user", "Merhaba", 1000L))
        val pair = pfm.buildNarrativeStreamHttpRequest(
            providerKey = "openai",
            model = "o3-mini",
            settings = settings,
            customEntity = null,
            buildConfigGeminiKey = "",
            systemPrompt = "Test",
            messages = msgs
        )
        assertNotNull(pair)
        val body = JSONObject(readRequestBody(pair!!.first))
        assertFalse("o3-mini must NOT send unsupported temperature", body.has("temperature"))
        assertFalse("o3-mini must NOT send unsupported top_p", body.has("top_p"))
        assertTrue(body.getBoolean("stream"))
    }

    @Test
    fun test3_urlResolversAndNormalizer_fixAnthropicV1PollinationsGeminiAndLocalAddresses() {
        val pfm = repo.managers.providerFallbackManager

        // Anthropic URL resolution (never /v1/v1/messages)
        assertEquals("https://api.anthropic.com/v1/messages", pfm.resolveAnthropicMessagesUrl("https://api.anthropic.com"))
        assertEquals("https://api.anthropic.com/v1/messages", pfm.resolveAnthropicMessagesUrl("https://api.anthropic.com/v1"))
        assertEquals("https://api.anthropic.com/v1/messages", pfm.resolveAnthropicMessagesUrl("https://api.anthropic.com/v1/messages"))

        // Pollinations URL resolution (always resolves to /openai/chat/completions)
        assertEquals(
            "https://text.pollinations.ai/openai/chat/completions",
            pfm.resolveOpenAiChatCompletionsUrl("https://text.pollinations.ai")
        )
        assertEquals(
            "https://text.pollinations.ai/openai/chat/completions",
            pfm.resolveOpenAiChatCompletionsUrl("https://text.pollinations.ai/openai")
        )

        // Gemini custom proxy URL resolution with query stripping
        assertEquals(
            "https://proxy.example.com/v1beta/models/gemini-2.5-flash:streamGenerateContent?alt=sse",
            pfm.resolveGeminiEndpointUrl("https://proxy.example.com/v1beta/models?key=AIzaFake", "gemini-2.5-flash", stream = true)
        )

        // CustomApiUrlNormalizer edge cases
        assertEquals(
            "https://api.anthropic.com",
            CustomApiUrlNormalizer.normalizeBaseUrl("https://api.anthropic.com/v1/messages")
        )
        assertEquals(
            "http://172.16.0.10:11434/v1",
            CustomApiUrlNormalizer.normalizeBaseUrl("172.16.0.10:11434/v1/chat/completions")
        )
        assertEquals(
            "http://macbook.local:1234/v1",
            CustomApiUrlNormalizer.normalizeBaseUrl("macbook.local:1234/v1/models?foo=bar")
        )
        assertEquals(
            "https://api.openai.com/v1",
            CustomApiUrlNormalizer.normalizeBaseUrl("\uFEFF  https://api.openai.com//v1/chat/completions#frag  ")
        )
    }

    @Test
    fun test4_sseStreamParser_parsesClaudeFullStreamAndInlineHttp200Errors() = runBlocking {
        // 1. Claude full SSE sequence (message_start + thinking_delta + text_delta + message_delta + message_stop)
        val claudeLines = listOf(
            "event: message_start",
            """data: {"type":"message_start","message":{"usage":{"input_tokens":120}}}""",
            "event: content_block_delta",
            """data: {"type":"content_block_delta","delta":{"type":"thinking_delta","thinking":"iç düşünce"}}""",
            """data: {"type":"content_block_delta","delta":{"type":"text_delta","text":"Merhaba Aiden."}}""",
            """data: {"type":"message_delta","delta":{"stop_reason":"end_turn"},"usage":{"output_tokens":45}}""",
            """data: {"type":"message_stop"}"""
        )
        val chunks = SseStreamParser.parseSseLines(claudeLines, SseStreamParser.SseFormat.CLAUDE)
        assertEquals(120L, chunks.sumOf { it.promptTokens })
        assertEquals(45L, chunks.sumOf { it.candidateTokens })
        assertEquals("iç düşünce", chunks.joinToString("") { it.reasoningToken })
        assertEquals("Merhaba Aiden.", chunks.joinToString("") { it.token })
        assertTrue(chunks.any { it.finishReason == "end_turn" })
        assertTrue(chunks.last().isDone)

        // 2. Inline HTTP-200 OpenRouter/Anthropic SSE error payload
        val errChunk = SseStreamParser.parseSseLine(
            """data: {"type":"error","error":{"type":"overloaded_error","message":"Anthropic servers overloaded"}}""",
            SseStreamParser.SseFormat.CLAUDE
        )
        assertNotNull(errChunk)
        assertEquals(529, errChunk!!.errorCode)
        assertTrue(errChunk.errorMessage.contains("overloaded"))

        // 3. Non-SSE plain JSON error returned on HTTP 200
        val received = mutableListOf<SseStreamParser.StreamChunk>()
        val rawJsonError = """{"error":{"code":429,"message":"Rate limit exceeded for free tier"}}"""
        SseStreamParser.parseSseReader(BufferedReader(StringReader(rawJsonError)), SseStreamParser.SseFormat.OPENAI) {
            received.add(it)
        }
        assertEquals(1, received.size)
        assertEquals(429, received.first().errorCode)
        assertTrue(received.first().errorMessage.contains("Rate limit"))
    }

    @Test
    fun test5_apiErrorFormatter_formats529RetryAfterContextLengthAndEofCleanly() {
        val overloaded = ApiErrorFormatter.formatException(
            SseStreamParser.SseHttpException(529, """{"error":{"message":"Overloaded"}}""", retryAfterSeconds = 20L),
            providerLabel = "Claude"
        )
        assertTrue(overloaded.contains("[Claude]"))
        assertTrue(overloaded.contains("529"))
        assertTrue(overloaded.contains("20 sn"))

        val ctxOverflow = ApiErrorFormatter.formatHttpError(
            httpCode = 400,
            rawBody = """{"error":{"message":"This model's maximum context length is 8192 tokens (context_length_exceeded)"}}"""
        )
        assertTrue("Must give Turkish context-length guidance", ctxOverflow.contains("bağlam penceresini aştı"))

        val eofMsg = ApiErrorFormatter.formatException(EOFException("unexpected end of stream"))
        assertTrue("Must format EOFException into clear Turkish", eofMsg.contains("beklenmedik şekilde kesildi"))
    }
}
