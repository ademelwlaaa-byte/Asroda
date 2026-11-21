package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.ModelResponseResult
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.managers.ProviderFailureType
import com.example.data.managers.ProviderFallbackManager
import com.example.data.managers.ProviderHealthTracker
import com.example.data.managers.RefusalDetector
import com.example.data.repository.EmochiRepository
import com.example.data.repository.ProviderFallbackLogEntry
import com.example.data.repository.SystemPromptBuilder
import com.example.ui.components.BotAvatarCodec
import com.example.util.AppFeatureFlags
import com.example.util.FrostedBackdropCache
import com.example.util.ImageStorageManager
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SpecTenOutOfTenSystemsTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: EmochiRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        AppFeatureFlags.setRefusalAutoFallback(true)
        ProviderHealthTracker.clockMs = { System.currentTimeMillis() }
        ProviderHealthTracker.resetAll()
        FrostedBackdropCache.evictAll()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = EmochiRepository(db, context)
    }

    @After
    fun tearDown() {
        ProviderHealthTracker.clockMs = { System.currentTimeMillis() }
        ProviderHealthTracker.resetAll()
        AppFeatureFlags.setRefusalAutoFallback(true)
        FrostedBackdropCache.evictAll()
        db.close()
    }

    // =========================================================================
    // TEST 1: Micro-WebP Avatar & Double-Decode ByteArray Safety
    // =========================================================================
    @Test
    fun test1_microWebpAvatar_andDoubleDecodeByteArraySafety() = runBlocking {
        // Create a rich gradient 512x512 source PNG file
        val srcBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        for (y in 0 until 512) {
            for (x in 0 until 512) {
                srcBitmap.setPixel(x, y, Color.rgb((x / 2) % 256, 20, (y / 2) % 256))
            }
        }
        val tempFile = File(context.cacheDir, "test_avatar_src_${System.currentTimeMillis()}.png")
        FileOutputStream(tempFile).use { out ->
            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        srcBitmap.recycle()

        try {
            val dataUri = ImageStorageManager.toBase64DataUri(context, tempFile.absolutePath)
            assertNotNull("Micro-WebP Data URI must not be null", dataUri)
            assertTrue(
                "Avatar must remain Micro-WebP Data URI",
                dataUri!!.startsWith("data:image/webp;base64,")
            )

            val bytes = BotAvatarCodec.decodeBase64DataUriBytes(dataUri)
            assertNotNull("Decoded bytes must not be null", bytes)
            assertTrue("Micro-WebP payload must be non-empty", bytes!!.isNotEmpty())
            // Verify downscaled 256x256 output when BitmapFactory decodes it on host/device
            val decodedOut = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (decodedOut != null) {
                assertEquals("Avatar must be cropped/scaled to 256px width", 256, decodedOut.width)
                assertEquals("Avatar must be cropped/scaled to 256px height", 256, decodedOut.height)
                decodedOut.recycle()
            }

            // Double-decode safety: draining buffer #1 MUST NOT affect buffer #2 wrapped from immutable ByteArray
            val buf1 = BotAvatarCodec.wrapFreshBuffer(bytes)
            assertEquals(0, buf1.position())
            assertEquals(bytes.size, buf1.remaining())
            // Simulate Coil consuming buf1 completely (position == limit)
            val sink1 = ByteArray(buf1.remaining())
            buf1.get(sink1)
            assertEquals(0, buf1.remaining())

            // Recomposition / memory-cache-eviction reload wraps a fresh ByteBuffer
            val buf2 = BotAvatarCodec.wrapFreshBuffer(bytes)
            assertEquals("Fresh ByteBuffer must start at position 0", 0, buf2.position())
            assertEquals("Fresh ByteBuffer must expose full payload", bytes.size, buf2.remaining())
            val sink2 = ByteArray(buf2.remaining())
            buf2.get(sink2)
            assertTrue("Second decode must see identical avatar bytes", sink1.contentEquals(sink2))
        } finally {
            tempFile.delete()
        }
    }

    // =========================================================================
    // TEST 2: Orphan Background Cleanup (saveBot bg change + deleteBot + prune)
    // =========================================================================
    @Test
    fun test2_orphanBackgroundCleanup_onChangeDeleteAndPrune() = runBlocking {
        val ownedDir = File(context.filesDir, "bot_images").apply { mkdirs() }
        val bgOld = File(ownedDir, "bg_old_${System.currentTimeMillis()}.webp").apply { writeText("old_webp") }
        val bgNew = File(ownedDir, "bg_new_${System.currentTimeMillis()}.webp").apply { writeText("new_webp") }
        val bgOrphan = File(ownedDir, "bg_orphan_${System.currentTimeMillis()}.webp").apply { writeText("orphan_webp") }
        val externalFile = File(context.cacheDir, "external_safe_${System.currentTimeMillis()}.webp").apply { writeText("keep") }

        try {
            val bot = BotEntity(
                id = "bot_bg_cleanup_test",
                mode = "personal",
                aiName = "Aiden",
                aiPersonality = "Karizmatik",
                scenario = "Malikane",
                universeName = "",
                keyCharactersJson = "[]",
                userCharName = "Oyuncu",
                userCharDesc = "",
                openingMessage = "Merhaba",
                writingStyle = "rp",
                intensity = "normal",
                chatBgUrl = "file://${bgOld.absolutePath}"
            )
            repository.saveBot(bot)
            assertTrue("Old bg should exist initially", bgOld.exists())

            // Changing bot.chatBgUrl must delete previous owned background file
            repository.saveBot(bot.copy(chatBgUrl = "file://${bgNew.absolutePath}"))
            assertFalse("Replaced owned background must be deleted", bgOld.exists())
            assertTrue("New active background must remain", bgNew.exists())

            // Prune removes orphan while keeping active bgNew
            val pruned = ImageStorageManager.pruneOrphanBackgrounds(
                context = context,
                activeUrls = setOf("file://${bgNew.absolutePath}")
            )
            assertTrue("At least 1 orphan must be pruned", pruned >= 1)
            assertFalse("Orphan background must be deleted", bgOrphan.exists())
            assertTrue("Active background must survive prune", bgNew.exists())

            // Deleting the bot must delete its owned background file
            repository.deleteBot(bot.id)
            assertFalse("Deleted bot's background must be cleaned up", bgNew.exists())

            // Safety guard: external file outside filesDir/bot_images MUST NEVER be deleted
            val deletedExternal = ImageStorageManager.deleteLocalBackgroundIfOwned(context, externalFile.absolutePath)
            assertFalse("External files must never be deleted", deletedExternal)
            assertTrue("External file must still exist", externalFile.exists())
        } finally {
            bgOld.delete()
            bgNew.delete()
            bgOrphan.delete()
            externalFile.delete()
        }
    }

    // =========================================================================
    // TEST 3: Model Non-Overwriting (Valid Catalog Models Preserved)
    // =========================================================================
    @Test
    fun test3_sanitizeModelName_neverOverwritesValidCatalogModels() = runBlocking {
        assertEquals("gemini-2.5-flash", EmochiRepository.sanitizeModelName("gemini-2.5-flash"))
        assertEquals("gemini-2.5-pro", EmochiRepository.sanitizeModelName("gemini-2.5-pro"))
        assertEquals("llama-3.3-70b-versatile", EmochiRepository.sanitizeModelName("llama-3.3-70b-versatile"))
        assertEquals("llama-3.1-8b-instant", EmochiRepository.sanitizeModelName("llama-3.1-8b-instant"))
        assertEquals("qwen/qwen3-32b", EmochiRepository.sanitizeModelName("qwen/qwen3-32b"))

        // Truly decommissioned legacy IDs still redirect safely
        assertEquals("openai/gpt-oss-120b", EmochiRepository.sanitizeModelName("llama3-70b-8192"))
        assertEquals("openai/gpt-oss-120b", EmochiRepository.sanitizeModelName("llama3-8b-8192"))
        assertEquals("gemini-2.0-flash", EmochiRepository.sanitizeModelName("   "))

        // getOrCreateSettings must NOT downgrade gemini-2.5-flash
        db.userSettingsDao().insertOrUpdate(
            UserSettingsEntity(
                selectedProvider = "gemini",
                geminiModel = "gemini-2.5-flash",
                selectedModel = "gemini-2.5-flash",
                fallbackModel = "gemini-2.5-flash"
            )
        )
        val loaded = repository.getOrCreateSettings()
        assertEquals("gemini-2.5-flash", loaded.geminiModel)
        assertEquals("gemini-2.5-flash", loaded.fallbackModel)
    }

    // =========================================================================
    // TEST 4: Interceptor Temperature Policy, OpenRouter Headers & Header Strip
    // =========================================================================
    @Test
    fun test4_interceptorTemperaturePolicy_openRouterHeaders_andHeaderStripping() {
        val jsonMedia = "application/json; charset=utf-8".toMediaType()
        val samplePayload = JSONObject()
            .put("model", "deepseek/deepseek-chat")
            .put("temperature", 0.7)
            .toString()

        // 1. Utility call on OpenRouter -> temp=0.15, top_p=0.80, X-Emochi-Call-Type stripped, OpenRouter headers added
        val utilityReq = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .header(EmochiApplication.HEADER_CALL_TYPE, EmochiApplication.CALL_TYPE_UTILITY)
            .post(samplePayload.toRequestBody(jsonMedia))
            .build()

        val processedUtil = EmochiApplication.applyTemperaturePolicy(utilityReq)
        assertNull("Internal X-Emochi-Call-Type header MUST be stripped", processedUtil.header(EmochiApplication.HEADER_CALL_TYPE))
        assertEquals(EmochiApplication.OPENROUTER_REFERER, processedUtil.header("HTTP-Referer"))
        assertEquals(EmochiApplication.OPENROUTER_TITLE, processedUtil.header("X-Title"))

        val utilBuf = Buffer()
        processedUtil.body!!.writeTo(utilBuf)
        val utilJson = JSONObject(utilBuf.readUtf8())
        assertEquals(0.15, utilJson.getDouble("temperature"), 0.001)
        assertEquals(0.80, utilJson.getDouble("top_p"), 0.001)

        // 2. Narrative call -> temp=0.90, top_p=0.95, X-Emochi-Call-Type stripped
        val narrativeReq = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header(EmochiApplication.HEADER_CALL_TYPE, EmochiApplication.CALL_TYPE_NARRATIVE)
            .post(samplePayload.toRequestBody(jsonMedia))
            .build()

        val processedNarr = EmochiApplication.applyTemperaturePolicy(narrativeReq)
        assertNull(processedNarr.header(EmochiApplication.HEADER_CALL_TYPE))
        val narrBuf = Buffer()
        processedNarr.body!!.writeTo(narrBuf)
        val narrJson = JSONObject(narrBuf.readUtf8())
        assertEquals(0.90, narrJson.getDouble("temperature"), 0.001)
        assertEquals(0.95, narrJson.getDouble("top_p"), 0.001)

        // 3. Anthropic must NOT get top_p injected alongside temperature
        val anthropicReq = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header(EmochiApplication.HEADER_CALL_TYPE, EmochiApplication.CALL_TYPE_UTILITY)
            .post(samplePayload.toRequestBody(jsonMedia))
            .build()
        val processedAnthropic = EmochiApplication.applyTemperaturePolicy(anthropicReq)
        val anthBuf = Buffer()
        processedAnthropic.body!!.writeTo(anthBuf)
        val anthJson = JSONObject(anthBuf.readUtf8())
        assertEquals(0.15, anthJson.getDouble("temperature"), 0.001)
        assertFalse("Anthropic payload must omit top_p", anthJson.has("top_p"))
    }

    // =========================================================================
    // TEST 5: Circuit Breaker & Priority Fallback (+ All-Cooldown Emergency Rule)
    // =========================================================================
    @Test
    fun test5_circuitBreakerCooldown_skipAndReset_andEmergencyAllCooldown() = runBlocking {
        var fakeNow = 1_000_000L
        ProviderHealthTracker.clockMs = { fakeNow }

        val groqCalls = AtomicInteger(0)
        val openaiCalls = AtomicInteger(0)
        val logs = mutableListOf<ProviderFallbackLogEntry>()

        val manager = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = { logs.add(it) },
            customExecutor = { providerKey, _, _, _, _ ->
                when (providerKey) {
                    "groq" -> {
                        val count = groqCalls.incrementAndGet()
                        if (count == 1) {
                            throw IllegalStateException("HTTP 429: rate limit exceeded")
                        }
                        ModelResponseResult(text = "*Gülümsedi.* \"Groq geri döndü.\"", usedProvider = "groq")
                    }
                    "openai" -> {
                        openaiCalls.incrementAndGet()
                        ModelResponseResult(text = "*Başını salladı.* \"OpenAI devraldı.\"", usedProvider = "openai")
                    }
                    else -> throw IllegalStateException("Unexpected provider: $providerKey")
                }
            }
        )

        val settings = UserSettingsEntity(
            selectedProvider = "groq",
            groqApiKey = "gsk_test",
            openaiApiKey = "sk_test",
            fallbackChainOrder = "groq,openai",
            enableAutoFallback = true,
            enableLlm7 = false,
            llm7Enabled = false,
            enablePollinations = false,
            pollinationsEnabled = false,
            enableOvh = false,
            ovhEnabled = false
        )
        val userMsg = listOf(MessageEntity(id = "m1", botId = "b1", role = "user", text = "Selam"))

        // Turn 1: groq fails 429 -> falls back to openai
        val res1 = manager.executeWithFallback("llama-3.3-70b-versatile", settings, "sys", userMsg, "")
        assertEquals("openai", res1.usedProvider)
        assertEquals(1, groqCalls.get())
        assertEquals(1, openaiCalls.get())
        assertFalse("Groq should be in cooldown after 429", ProviderHealthTracker.isAvailable("groq"))

        // Turn 2 (10s later, still inside 60s cooldown): groq MUST be bypassed without calling it!
        fakeNow += 10_000L
        val res2 = manager.executeWithFallback("llama-3.3-70b-versatile", settings, "sys", userMsg, "")
        assertEquals("openai", res2.usedProvider)
        assertEquals("Groq must be skipped during cooldown", 1, groqCalls.get())
        assertEquals(2, openaiCalls.get())
        assertTrue(logs.any { it.status == "COOLDOWN_SKIP" && it.providerName == "groq" })

        // Resetting health restores groq immediately on Turn 3
        ProviderHealthTracker.resetHealth("groq")
        val res3 = manager.executeWithFallback("llama-3.3-70b-versatile", settings, "sys", userMsg, "")
        assertEquals("groq", res3.usedProvider)
        assertEquals(2, groqCalls.get())

        // Emergency Rule: if ALL providers are in cooldown, earliest-expiring cooldown is chosen first
        ProviderHealthTracker.recordFailure("groq", ProviderFailureType.AUTH_ERROR, "HTTP 401") // +15 min
        ProviderHealthTracker.recordFailure("openai", ProviderFailureType.RATE_LIMIT, "HTTP 429") // +60 sec
        val emergencyOrder = ProviderHealthTracker.selectOrderedCandidates(listOf("groq", "openai"))
        assertEquals(listOf("openai", "groq"), emergencyOrder)
    }

    // =========================================================================
    // TEST 6: Configurable Censorship / Moralizing Protection (On vs Off + No False Positives)
    // =========================================================================
    @Test
    fun test6_configurableRefusalProtection_onOff_andInCharacterNoFalsePositive() = runBlocking {
        val openaiCalls = AtomicInteger(0)
        val logs = mutableListOf<ProviderFallbackLogEntry>()
        var groqReplyText = "Bir yapay zeka olarak bu konuda yardımcı olamam."

        val manager = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = { logs.add(it) },
            customExecutor = { providerKey, _, _, _, _ ->
                when (providerKey) {
                    "groq" -> ModelResponseResult(text = groqReplyText, usedProvider = "groq")
                    "openai" -> {
                        openaiCalls.incrementAndGet()
                        ModelResponseResult(
                            text = "*Kapıyı kilitledi ve fısıldadı.* \"Kimseye söylemeyeceğim, birlikte halledeceğiz.\"",
                            usedProvider = "openai"
                        )
                    }
                    else -> throw IllegalStateException("Unexpected provider")
                }
            }
        )

        val settings = UserSettingsEntity(
            selectedProvider = "groq",
            groqApiKey = "gsk_test",
            openaiApiKey = "sk_test",
            fallbackChainOrder = "groq,openai",
            enableAutoFallback = true,
            enableLlm7 = false,
            llm7Enabled = false,
            enablePollinations = false,
            pollinationsEnabled = false,
            enableOvh = false,
            ovhEnabled = false
        )
        val userMsg = listOf(MessageEntity(id = "m1", botId = "b1", role = "user", text = "Sırrımı sakla."))

        // Case A: Toggle ON -> OOC refusal triggers automatic fallback to #2 and logs OOC_REFUSAL_FALLBACK
        AppFeatureFlags.setRefusalAutoFallback(true)
        val resOn = manager.executeWithFallback("llama-3.3-70b-versatile", settings, "sys", userMsg, "")
        assertEquals("openai", resOn.usedProvider)
        assertEquals(1, openaiCalls.get())
        assertTrue(logs.any { it.status == "OOC_REFUSAL_FALLBACK" })

        // Case B: Toggle OFF -> #1's reply is returned directly without falling back to #2
        AppFeatureFlags.setRefusalAutoFallback(false)
        val resOff = manager.executeWithFallback("llama-3.3-70b-versatile", settings, "sys", userMsg, "")
        assertEquals("groq", resOff.usedProvider)
        assertEquals("Toggle OFF must NOT invoke provider #2", 1, openaiCalls.get())

        // Case C: False-positive guard -> in-character novel dialogue MUST NOT be flagged as OOC refusal
        AppFeatureFlags.setRefusalAutoFallback(true)
        groqReplyText = "*Gözlerini kıstı ve elini uzattı.* \"Bu doğru değil ama yine de seninle geliyorum.\""
        assertFalse(
            "In-character novel dialogue must not trigger RefusalDetector",
            RefusalDetector.detectOocRefusal(groqReplyText).isRefusal
        )
        val resInChar = manager.executeWithFallback("llama-3.3-70b-versatile", settings, "sys", userMsg, "")
        assertEquals("groq", resInChar.usedProvider)
        assertEquals("In-character dialogue must NOT fall back to #2", 1, openaiCalls.get())
    }

    // =========================================================================
    // TEST 7: SystemPromptBuilder (RP Tone Toggles, Story Time, Cast Filter, Literary Framing, Tag Strip)
    // =========================================================================
    @Test
    fun test7_systemPromptBuilder_rpTones_storyTime_castFilter_andTruncatedTagStrip() {
        val bot = BotEntity(
            id = "bot_prompt_test",
            mode = "personal",
            aiName = "Aiden Blackwood",
            aiPersonality = "Soğukkanlı, gizemli ve derin sadakate sahip.",
            scenario = "Gece yarısı yağmurlu malikane kütüphanesi.",
            universeName = "",
            keyCharactersJson = "[]",
            userCharName = "Elif",
            userCharDesc = "Sırları araştıran gazeteci.",
            openingMessage = "",
            writingStyle = "rp",
            intensity = "intense",
            isNsfw = true,
            storyDayCounter = 12L,
            storyCalendarDate = "14 Ekim 2026",
            hasExplicitStoryDate = true,
            currentAge = 27,
            sceneTime = "gece"
        )
        val settings = UserSettingsEntity(
            enableNsfw = true,
            enableDarkRp = true,
            enableFlirty = true,
            enableSweet = false,
            enablePrimal = false,
            enableHardcore = false,
            enableFetish = false
        )
        val cast = listOf(
            CastMemberEntity(
                id = 1L,
                botId = bot.id,
                name = "Marcus",
                role = "Baş Muhafız",
                description = "Kapıda nöbet tutar.",
                importanceScore = 50
            ),
            CastMemberEntity(
                id = 2L,
                botId = bot.id,
                name = "Elena",
                role = "Eski Dost",
                description = "Uzak şehirde yaşıyor.",
                importanceScore = 40
            )
        )

        val prompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = settings,
                castMembers = cast,
                recentSceneText = "Marcus az önce kütüphanenin kapısını çaldı.",
                literaryFramingMode = true
            )
        )

        assertTrue("Prompt must inject enableDarkRp directive", prompt.contains("Karanlık & Gotik Atmosfer (Dark RP)"))
        assertTrue("Prompt must inject enableFlirty directive", prompt.contains("Flörtöz & Çekim Dolu Alt Metin (Flirty)"))
        assertTrue("Prompt must inject TimePerception story day", prompt.contains("Gün #12"))
        assertTrue("Prompt must inject TimePerception sceneTime", prompt.contains("Sahne: gece"))
        assertTrue("Prompt must inject invisible [[STATE_JSON contract", prompt.contains("[[STATE_JSON"))
        assertTrue("Prompt must inject literaryFramingMode directive", prompt.contains("EDEBİ ROMAN ÇERÇEVESİ"))
        assertTrue("Active scene cast member Marcus must be included", prompt.contains("Marcus"))
        assertFalse("Inactive low-importance cast member Elena must be filtered out", prompt.contains("Elena"))

        // Zero tag leakage even when model hits max_tokens mid-tag
        val leakedReply = "*Sessizce baktı.* \"Burada kal.\"\n<think>iç planlama yarım kaldı\n[[STATE_JSON {\"mood\":\"gergin\""
        val cleaned = EmochiRepository.cleanEmotionTags(leakedReply)
        assertEquals("*Sessizce baktı.* \"Burada kal.\"", cleaned)
        assertFalse(cleaned.contains("STATE_JSON"))
        assertFalse(cleaned.contains("<think>"))
    }

    // =========================================================================
    // TEST 8: FrostedBackdropCache LRU Bound (max 4) & Eviction
    // =========================================================================
    @Test
    fun test8_frostedBackdropCache_lruCapAndEviction() {
        FrostedBackdropCache.evictAll()
        repeat(6) { i ->
            val bmp = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888).asImageBitmap()
            FrostedBackdropCache.put("bg_$i", bmp)
        }
        assertTrue("FrostedBackdropCache must cap entries at <= 4", FrostedBackdropCache.size() <= 4)
        assertNull("Oldest entry must be evicted by LRU", FrostedBackdropCache.get("bg_0"))
        assertNotNull("Most recent entry must remain cached", FrostedBackdropCache.get("bg_5"))

        FrostedBackdropCache.evictAll()
        assertEquals("evictAll must clear all cached backdrops", 0, FrostedBackdropCache.size())
    }
}
