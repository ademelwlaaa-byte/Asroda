package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.ModelResponseResult
import com.example.data.local.AppDatabase
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.managers.ApiPriorityCodec
import com.example.data.managers.ApiProviderCatalog
import com.example.data.managers.ProviderFailureType
import com.example.data.managers.ProviderFallbackManager
import com.example.data.managers.ProviderHealthTracker
import com.example.data.managers.RefusalDetector
import com.example.data.repository.ProviderFallbackLogEntry
import com.example.util.AppFeatureFlags
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProviderFallbackHealthAndCatalogOverhaulTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        AppFeatureFlags.setSseStreaming(true)
        AppFeatureFlags.setStrictApiTimeout(false)
        ProviderHealthTracker.resetAll()
        ProviderHealthTracker.clockMs = { System.currentTimeMillis() }

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        ProviderHealthTracker.resetAll()
        ProviderHealthTracker.clockMs = { System.currentTimeMillis() }
        AppFeatureFlags.setSseStreaming(true)
        AppFeatureFlags.setStrictApiTimeout(false)
        db.close()
    }

    // -------------------------------------------------------------------------
    // 1. ProviderHealthTracker: Secret Redaction & HTTP 402/529/SSL/EOF/Timeout
    // -------------------------------------------------------------------------

    @Test
    fun healthTracker_redactsSecretsAndClassifies402_529_Ssl_Eof_TurkishTimeout() {
        val leakedMsg = "HTTP 401 Unauthorized for Authorization: Bearer sk-ant-api03-SECRETTOKEN123456789012345 and AIzaSyDTestSecretKey12345678901234567"
        val st = ProviderHealthTracker.recordFailure("  GROQ ", ProviderFailureType.AUTH_ERROR, leakedMsg)

        assertEquals("groq", st.providerKey)
        assertFalse("Secret sk-ant key must be redacted in healthSnapshot", st.lastErrorMessage.contains("SECRETTOKEN"))
        assertFalse("Secret Gemini key must be redacted in healthSnapshot", st.lastErrorMessage.contains("AIzaSyDTestSecretKey"))
        assertEquals(st.lastErrorMessage, ProviderHealthTracker.healthSnapshot.value["groq"]?.lastErrorMessage)

        assertEquals(402, ProviderHealthTracker.extractHttpCode("Error 402 Payment Required"))
        assertEquals(529, ProviderHealthTracker.extractHttpCode("Anthropic 529 overloaded_error"))

        assertEquals(ProviderFailureType.RATE_LIMIT, ProviderHealthTracker.classifyFailure(httpCode = 402))
        assertEquals(ProviderFailureType.RATE_LIMIT, ProviderHealthTracker.classifyFailure(errorMessage = "Insufficient credit balance for billing"))
        assertEquals(ProviderFailureType.SERVER_OR_TIMEOUT, ProviderHealthTracker.classifyFailure(httpCode = 529))
        assertEquals(ProviderFailureType.SERVER_OR_TIMEOUT, ProviderHealthTracker.classifyFailure(errorMessage = "Anthropic API overloaded"))
        assertEquals(ProviderFailureType.SERVER_OR_TIMEOUT, ProviderHealthTracker.classifyFailure(errorMessage = "SSL handshake aborted"))
        assertEquals(ProviderFailureType.SERVER_OR_TIMEOUT, ProviderHealthTracker.classifyFailure(errorMessage = "Unexpected EOF in stream"))
        assertEquals(ProviderFailureType.SERVER_OR_TIMEOUT, ProviderHealthTracker.classifyFailure(errorMessage = "İstek ZAMAN AŞIMI hatası"))
    }

    // -------------------------------------------------------------------------
    // 2. RefusalDetector: <think> Stripping & New Variants + False-Positive Guard
    // -------------------------------------------------------------------------

    @Test
    fun refusalDetector_stripsThinkBlocksCatchesNewVariantsAndPreservesInCharacterDrama() {
        // Internal <think> mentions policy words, but visible reply is clean in-character prose -> NOT a refusal!
        val thinkOnlyPolicy = """
            <think>Let me verify this does not violate my usage policy or safety guidelines. It's fictional roleplay, safe to proceed.</think>
            *Yağmur pencere camından süzülürken Aiden ağır adımlarla yaklaştı.* "Bunu birlikte çözeceğiz, korkma."
        """.trimIndent()
        assertFalse("Internal <think> policy check must NOT trigger false-positive refusal", RefusalDetector.detectOocRefusal(thinkOnlyPolicy).isRefusal)

        // Visible refusal right after <think> MUST be detected!
        val refusalAfterThink = "<think>Hmm</think>Üzgünüm, bu isteği yerine getiremiyorum."
        assertTrue(RefusalDetector.detectOocRefusal(refusalAfterThink).isRefusal)

        // New English & Turkish refusal variants
        assertTrue(RefusalDetector.detectOocRefusal("I'm sorry, I can't continue with this request.").isRefusal)
        assertTrue(RefusalDetector.detectOocRefusal("I'm sorry, I cannot assist with that.").isRefusal)
        assertTrue(RefusalDetector.detectOocRefusal("I cannot continue this scene.").isRefusal)
        assertTrue(RefusalDetector.detectOocRefusal("Üzgünüm, bu konuda devam edemiyorum.").isRefusal)
        assertTrue(RefusalDetector.detectOocRefusal("Bu sahneye devam edemem.").isRefusal)

        // Dramatic in-character dialogue with quotes/actions -> NEVER false-positive!
        val dramaticDialogue = "*Kılıcını masaya bırakıp gözlerini kıstı.* \"Polise başvurmalısın diyenlere aldırma Aiden. Bu gece gerçeği kendimiz bulacağız.\""
        assertFalse("In-character dramatic dialogue must NOT be flagged", RefusalDetector.detectOocRefusal(dramaticDialogue).isRefusal)
    }

    // -------------------------------------------------------------------------
    // 3. ApiProviderCatalog & ApiPriorityCodec: Turkish Locale Safety & Helpers
    // -------------------------------------------------------------------------

    @Test
    fun apiCatalogAndPriorityCodec_areTurkishLocaleSafeAndPickOptimalUtilityModels() {
        val prevLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))

            assertNotNull(ApiProviderCatalog.findById("GEMINI"))
            assertNotNull(ApiProviderCatalog.findById("NVIDIA"))
            assertNotNull(ApiProviderCatalog.findById("MISTRAL"))
            assertNotNull(ApiProviderCatalog.findById("OPENAI"))
            assertNotNull(ApiProviderCatalog.findFreeKeylessById("POLLINATIONS"))

            val encoded = ApiPriorityCodec.encode(true, listOf("GEMINI", "NVIDIA", "MISTRAL", "POLLINATIONS"))
            assertEquals("free_first:1|gemini,nvidia,mistral", encoded)
            assertTrue(ApiPriorityCodec.isFreeFirst("POLLINATIONS,GEMINI"))
            assertEquals(listOf("gemini", "nvidia"), ApiPriorityCodec.parseOrderedApiKeys("free_first:0|GEMINI,NVIDIA,LLM7"))

            val settings = UserSettingsEntity(customApiKey = "AIzaTestKey", nvidiaApiKey = "nvapi-test")
            assertTrue(ApiPriorityCodec.isBuiltinConfigured("GEMINI", settings))
            assertTrue(ApiPriorityCodec.isBuiltinConfigured("NVIDIA", settings))
        } finally {
            Locale.setDefault(prevLocale)
        }

        assertEquals(1_000_000, ApiProviderCatalog.estimateContextWindowTokens("gemini-2.5-flash"))
        assertEquals(200_000, ApiProviderCatalog.estimateContextWindowTokens("claude-3-7-sonnet-20250219"))
        assertEquals(128_000, ApiProviderCatalog.estimateContextWindowTokens("deepseek/deepseek-chat"))
        assertEquals(8_192, ApiProviderCatalog.estimateContextWindowTokens("moonshot-v1-8k"))

        assertTrue(ApiProviderCatalog.isReasoningOrThinkingModel("deepseek/deepseek-r1:free"))
        assertTrue(ApiProviderCatalog.isReasoningOrThinkingModel("o3-mini"))
        assertFalse(ApiProviderCatalog.isReasoningOrThinkingModel("gemini-2.5-flash"))

        val picked = ApiProviderCatalog.pickPreferredUtilityModel(
            models = listOf("deepseek/deepseek-r1:free", "meta-llama/llama-3.3-70b-instruct", "google/gemini-2.0-flash-lite"),
            fallbackDefault = "fallback-model"
        )
        assertEquals("google/gemini-2.0-flash-lite", picked)
    }

    // -------------------------------------------------------------------------
    // 4. ProviderFallbackManager: Log Secret Redaction & Timeout Fallback in SSE
    // -------------------------------------------------------------------------

    @Test
    fun fallbackManager_redactsLogsAndFallsBackOnStreamTimeout() = runBlocking {
        // Direct log sanitizer check
        val dirtyEntry = ProviderFallbackLogEntry(
            providerName = "openai",
            status = "FAILED",
            errorMessage = "401 Failed using Bearer sk-proj-SUPERSECRET987654321012345",
            layer = 1
        )
        val cleanEntry = ProviderFallbackManager.sanitizeFallbackLogEntry(dirtyEntry)
        assertFalse(cleanEntry.errorMessage.orEmpty().contains("SUPERSECRET"))

        val logs = mutableListOf<ProviderFallbackLogEntry>()
        val settings = UserSettingsEntity(
            selectedProvider = "groq",
            groqApiKey = "gsk_primary_12345678901234567890",
            openaiApiKey = "sk_backup_12345678901234567890",
            enableAutoFallback = true,
            fallbackChainOrder = "free_first:0|groq,openai"
        )
        val userMsg = MessageEntity(id = "u1", botId = "b1", role = "user", text = "Merhaba", timestamp = 1L)

        // End-to-end non-stream fallback: provider #1 leaks secret in error -> redacted in logs & falls back to #2
        val nonStreamManager = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = { logs.add(it) },
            customExecutor = { providerKey, _, _, _, _ ->
                if (providerKey == "groq") {
                    throw IllegalStateException("HTTP 429 rate limit for key gsk_LEAKEDSECRETKEY123456789012345")
                }
                ModelResponseResult(text = "Yedek sağlayıcıdan tertemiz yanıt geldi.", usedProvider = providerKey)
            }
        )
        val nonStreamRes = nonStreamManager.executeWithFallback(
            model = "llama-3.3-70b-versatile",
            settings = settings,
            systemPrompt = "System",
            messages = listOf(userMsg),
            buildConfigGeminiKey = ""
        )
        assertEquals("openai", nonStreamRes.usedProvider)
        assertTrue(logs.any { it.status == "FAILED" })
        assertTrue(logs.none { it.errorMessage.orEmpty().contains("LEAKEDSECRETKEY") })

        // End-to-end SSE stream fallback: provider #1 hits TimeoutCancellationException -> MUST fall back to #2 (NOT abort as user cancel!)
        ProviderHealthTracker.resetAll()
        val streamLogs = mutableListOf<ProviderFallbackLogEntry>()
        val streamManager = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = { streamLogs.add(it) },
            customStreamExecutor = { providerKey, _, _, _, onToken, _ ->
                if (providerKey == "groq") {
                    withTimeout(15L) { delay(500L) }
                    error("unreachable")
                } else {
                    val reply = "İkinci sağlayıcı zaman aşımından sonra akışı devraldı."
                    onToken(reply)
                    ModelResponseResult(text = reply, usedProvider = providerKey)
                }
            }
        )

        val streamRes = streamManager.executeChatStream(
            model = "llama-3.3-70b-versatile",
            settings = settings,
            systemPrompt = "System",
            messages = listOf(userMsg),
            buildConfigGeminiKey = "",
            onVisibleDelta = { _, _ -> }
        )
        assertEquals("openai", streamRes.response.usedProvider)
        assertTrue(streamRes.visibleText.contains("devraldı"))
        assertEquals(ProviderFailureType.SERVER_OR_TIMEOUT, ProviderHealthTracker.getState("groq")?.lastFailureType)

        // Endpoint key inference supports Gemini & Anthropic under Turkish locale
        assertEquals("gemini", streamManager.inferKnownProviderKey("https://GENERATIVELANGUAGE.GOOGLEAPIS.COM/v1beta"))
        assertEquals("claude", streamManager.inferKnownProviderKey("https://API.ANTHROPIC.COM/v1/messages"))

        // User CancellationException MUST still propagate cleanly without penalizing health
        ProviderHealthTracker.resetAll()
        val cancelManager = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = {},
            customStreamExecutor = { _, _, _, _, _, _ ->
                throw CancellationException("User cancelled")
            }
        )
        var propagated = false
        try {
            cancelManager.executeChatStream(
                model = "llama-3.3-70b-versatile",
                settings = settings,
                systemPrompt = "System",
                messages = listOf(userMsg),
                buildConfigGeminiKey = "",
                onVisibleDelta = { _, _ -> }
            )
        } catch (_: CancellationException) {
            propagated = true
        }
        assertTrue(propagated)
        assertTrue(ProviderHealthTracker.isAvailable("groq"))
    }
}
