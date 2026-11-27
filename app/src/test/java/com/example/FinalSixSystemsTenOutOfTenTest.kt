package com.example

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.MemoryToolRegistry
import com.example.data.api.ModelResponseResult
import com.example.data.local.AppDatabase
import com.example.data.local.ArchivedMemoryEntity
import com.example.data.local.BotEntity
import com.example.data.local.CustomProviderEntity
import com.example.data.local.EmotionState
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MemoryFactEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PendingReappraisalEntity
import com.example.data.local.PrimaryEmotions
import com.example.data.local.RelationshipAxes
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.managers.BackupSnapshotManager
import com.example.data.managers.ProviderFailureType
import com.example.data.managers.ProviderFallbackManager
import com.example.data.managers.ProviderHealthTracker
import com.example.data.managers.RagRetrievalBundle
import com.example.data.repository.BaselineEmotionEngine
import com.example.data.repository.EmochiRepository
import com.example.data.repository.MemoryEngine
import com.example.data.repository.MemoryLifecycleCodec
import com.example.data.repository.MemoryLifecycleMeta
import com.example.data.repository.MemoryTier
import com.example.data.repository.SystemPromptBuilder
import com.example.ui.viewmodel.EmochiViewModel
import com.example.util.AppFeatureFlags
import com.example.util.ChatExporter
import com.example.util.ChatTtsManager
import com.example.util.EdgeNeuralTtsClient
import com.example.util.EmotionalTtsProsodyEngine
import com.example.util.SecureApiKeyVault
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinalSixSystemsTenOutOfTenTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: EmochiRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        AppFeatureFlags.clearAiUtilityModels()
        AppFeatureFlags.setStrictApiTimeout(true)
        AppFeatureFlags.setTtsEngineMode(AppFeatureFlags.TTS_ENGINE_NEURAL)
        AppFeatureFlags.setTtsEmotionalProsody(true)
        ProviderHealthTracker.resetAll()
        EdgeNeuralTtsClient.synthesizerOverride = null

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = EmochiRepository(db, context)
    }

    @After
    fun tearDown() {
        EdgeNeuralTtsClient.synthesizerOverride = null
        ProviderHealthTracker.resetAll()
        AppFeatureFlags.clearAiUtilityModels()
        File(context.filesDir, "emochi_autobackup.json").delete()
        File(context.filesDir, "emochi_autobackup.json.tmp").delete()
        File(context.cacheDir, "exports").deleteRecursively()
        File(context.cacheDir, "tts_audio_cache").deleteRecursively()
        db.close()
    }

    private fun makeBot(
        id: String = "bot_final_1",
        isPublic: Boolean = false,
        isTemplate: Boolean = false
    ): BotEntity = BotEntity(
        id = id,
        mode = "personal",
        aiName = "Aiden",
        aiPersonality = "Soğukkanlı, derin ve koruyucu bir dedektif.",
        scenario = "Yağmurlu gece katedral avlusunda gizli bir ipucu arıyoruz.",
        universeName = "",
        keyCharactersJson = "[]",
        userCharName = "Selin",
        userCharDesc = "Araştırmacı gazeteci.",
        openingMessage = "Yağmur dinmeden buradan çıkmalıyız.",
        writingStyle = "rp",
        intensity = "normal",
        isPublic = isPublic,
        isTemplate = isTemplate
    )

    // =========================================================================
    // TEST 1 (Sistem #1): KG → RAG Bridge, Semantic Dedup & CORE/Pinned TTL Lock
    // =========================================================================
    @Test
    fun test1_system1_kgRagBridge_semanticDedup_andCorePinnedTtlImmunity() = runBlocking {
        val bot = makeBot(id = "bot_rag_10")
        db.botDao().insertOrUpdate(bot)

        val graphNote = "Elena → [kardeş] → Aiden (güvenilir müttefik)"
        val duplicateFact = MemoryFactEntity(
            id = 1L,
            botId = bot.id,
            subject = "Elena",
            key = "kardeş",
            value = "Aiden güvenilir müttefik",
            confidence = MemoryLifecycleCodec.encode(
                MemoryLifecycleMeta(baseConfidence = "high", tier = MemoryTier.ARC, createdStoryDay = 1L, expiresStoryDay = 10L)
            ),
            userCorrected = false
        )
        val pinnedCoreFact = MemoryFactEntity(
            id = 2L,
            botId = bot.id,
            subject = "Selin",
            key = "alerji",
            value = "Fıstık alerjisi hayati tehlike taşır",
            confidence = MemoryLifecycleCodec.encode(
                MemoryLifecycleMeta(baseConfidence = "certain", tier = MemoryTier.CORE, createdStoryDay = 1L, expiresStoryDay = 0L)
            ),
            userCorrected = true
        )
        val lowRankEvent = MemoryEventEntity(
            id = 3L,
            botId = bot.id,
            timestamp = 1_000_000L,
            description = "Selin fıstık alerjisi hayati tehlike taşır",
            importanceScore = 40
        )

        // Strict dedup: Graph note drops duplicate fact, Pinned/CORE fact survives even on tight <=32K budget,
        // and duplicate LivingMemory event is dropped.
        val packed = MemoryEngine.buildBoundedMemoryBlock(
            facts = listOf(duplicateFact, pinnedCoreFact),
            events = listOf(lowRankEvent),
            maxTokens = 40, // Very tight <=32K-style ceiling
            graphResolutionNote = graphNote
        )
        assertTrue("Pinned/CORE fact MUST survive tight token ceiling", packed.facts.any { it.id == 2L })
        assertFalse("Fact duplicating Knowledge Graph note must be deduplicated", packed.facts.any { it.id == 1L })
        assertTrue("Event duplicating Pinned/CORE fact must be deduplicated", packed.events.isEmpty())

        // SystemPromptBuilder injects [İLİŞKİ AĞI GERÇEĞİ] directly from graphResolutionNote
        val ragBundle = RagRetrievalBundle(
            facts = packed.facts,
            events = packed.events,
            graphResolutionNote = graphNote
        )
        val systemPrompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = UserSettingsEntity(),
                relevantFacts = ragBundle.facts,
                relevantEvents = ragBundle.events,
                graphResolutionNote = ragBundle.graphResolutionNote
            )
        )
        assertTrue(
            "System prompt must include [İLİŞKİ AĞI GERÇEĞİ] KG bridge block",
            systemPrompt.contains("[İLİŞKİ AĞI GERÇEĞİ]") && systemPrompt.contains("Elena")
        )

        // Hard Immunity Lock in reapExpiredMemories: Pinned & CORE never reaped; expired EPHEMERAL is reaped
        val pinnedEphemeral = MemoryFactEntity(
            botId = bot.id,
            subject = "Selin",
            key = "söz",
            value = "Asla ihanet etmeyeceğim",
            confidence = MemoryLifecycleCodec.encode(
                MemoryLifecycleMeta(baseConfidence = "certain", tier = MemoryTier.EPHEMERAL, createdStoryDay = 1L, expiresStoryDay = 2L)
            ),
            userCorrected = true
        )
        val coreFact = MemoryFactEntity(
            botId = bot.id,
            subject = "Aiden",
            key = "geçmiş",
            value = "Kardeşini Londra yangınında kaybetti",
            confidence = MemoryLifecycleCodec.encode(
                MemoryLifecycleMeta(baseConfidence = "certain", tier = MemoryTier.CORE, createdStoryDay = 1L, expiresStoryDay = 2L)
            ),
            userCorrected = false
        )
        val expiredEphemeral = MemoryFactEntity(
            botId = bot.id,
            subject = "Selin",
            key = "kahve",
            value = "Sabah sütlü filtre kahve içti",
            confidence = MemoryLifecycleCodec.encode(
                MemoryLifecycleMeta(baseConfidence = "medium", tier = MemoryTier.EPHEMERAL, createdStoryDay = 1L, expiresStoryDay = 3L)
            ),
            userCorrected = false
        )
        db.memoryFactDao().insertFacts(listOf(pinnedEphemeral, coreFact, expiredEphemeral))

        val reaped = repository.reapExpiredMemories(bot.id, currentStoryDay = 25L, currentMessageCount = 120)
        assertEquals("Only unpinned expired EPHEMERAL fact should be reaped", 1, reaped)

        val stillActive = db.memoryFactDao().getActiveFacts(bot.id)
        assertEquals(2, stillActive.size)
        assertTrue(stillActive.any { it.key == "söz" && it.userCorrected })
        assertTrue(stillActive.any { it.key == "geçmiş" })
        assertFalse(stillActive.any { it.key == "kahve" })
    }

    // =========================================================================
    // TEST 2 (Sistem #2): Strict Baseline Clamping + Idempotent Trigger Persistence
    // =========================================================================
    @Test
    fun test2_system2_baselineStrictClamping_andIdempotentTriggerPersistence() = runBlocking {
        val maliciousJson = """
            {
              "relationshipType": "friend",
              "bigFive": { "openness": 999, "conscientiousness": -50, "extraversion": "NaN", "agreeableness": 140, "neuroticism": -10 },
              "attachmentStyle": "chaos_invalid_style",
              "regulationCapacity": 500,
              "affectionDifficultyMultiplier": 9.9,
              "initialPlutchik": { "joy": 250, "trust": -40, "anger": 999, "sadness": -5, "fear": 50, "anticipation": 10, "surprise": 0, "disgust": 0 },
              "initialAxes": { "affection": 999, "respect": -25, "comfort": 300, "resentment": -99 },
              "dominantEmotion": "DROP TABLE bots; 12345 !!!",
              "sensitiveTriggers": [
                { "topic": "İhanet", "intensityMultiplier": 9.0, "reaction": "Sert tepki" },
                { "topic": "Yalan", "intensityMultiplier": 0.1, "reaction": "Soğuma" }
              ],
              "confidence": 999
            }
        """.trimIndent()

        val parsed = BaselineEmotionEngine.parseAnalysisJson(
            jsonStr = maliciousJson,
            botId = "bot_base_2",
            aiName = "Aiden",
            personality = "Dedektif",
            scenario = "Katedral"
        )

        assertEquals(BaselineEmotionEngine.BaselineSource.AI_ANALYZED, parsed.source)
        assertTrue(parsed.personalityProfile.openness in 0..100)
        assertTrue(parsed.personalityProfile.conscientiousness in 0..100)
        assertTrue(parsed.personalityProfile.extraversion in 0..100)
        assertTrue(
            "attachmentStyle must be normalized to whitelist",
            parsed.personalityProfile.attachmentStyle in setOf("secure", "anxious", "avoidant", "fearful_avoidant")
        )
        assertTrue(parsed.regulationCapacity in 10..100)
        assertTrue(parsed.difficultyMultiplier in 0.50..2.00)
        assertTrue(parsed.baselineEmotionState.primaryEmotions.joy in 0..100)
        assertTrue(parsed.baselineEmotionState.primaryEmotions.trust in 0..100)
        assertTrue(parsed.baselineEmotionState.relationshipAxes.affectionScore in 0..100)
        assertTrue(parsed.baselineEmotionState.relationshipAxes.resentmentScore in 0..100)
        assertFalse("Invalid dominantEmotion injection must be sanitized", parsed.baselineEmotionState.dominantEmotion.contains("DROP"))
        parsed.sensitiveTriggers.forEach { tr ->
            assertTrue(
                "Trigger multiplier must be clamped to 1.2..2.5, was ${tr.intensityMultiplier}",
                tr.intensityMultiplier in 1.2..2.5
            )
        }

        // Local heuristic fallback reports LOCAL_HEURISTIC source
        val heuristic = BaselineEmotionEngine.calculateDeterministicFallback(
            botId = "bot_base_2",
            aiName = "Aiden",
            personality = "Eski sevgili",
            scenario = "Ayrıldık"
        )
        assertEquals(BaselineEmotionEngine.BaselineSource.LOCAL_HEURISTIC, heuristic.source)

        // Idempotent persistBaselineTriggers: preserves [USER] triggers & creates zero duplicates across repeated saves
        val bot = makeBot(id = "bot_base_2")
        db.botDao().insertOrUpdate(bot)
        db.sensitiveTriggerDao().insertOrUpdate(
            SensitiveTriggerEntity(
                botId = bot.id,
                triggerTopic = "Özel Travma",
                sourceDescription = "[USER] Kullanıcının eklediği özel tetikleyici",
                intensityMultiplier = 2.0
            )
        )

        val baselineBatch = listOf(
            SensitiveTriggerEntity(botId = bot.id, triggerTopic = "İhanet", sourceDescription = "Geçmiş yara", intensityMultiplier = 2.4),
            SensitiveTriggerEntity(botId = bot.id, triggerTopic = "Yalan", sourceDescription = "Güven kırılması", intensityMultiplier = 1.7)
        )
        repository.persistBaselineTriggers(bot.id, baselineBatch)
        repository.persistBaselineTriggers(bot.id, baselineBatch) // Second call must be 100% idempotent

        val persisted = db.sensitiveTriggerDao().getTriggersForBot(bot.id)
        assertEquals("Should contain 1 [USER] trigger + 2 baseline triggers with zero duplicates", 3, persisted.size)
        assertTrue("[USER] trigger must be preserved untouched", persisted.any { it.triggerTopic == "Özel Travma" && it.sourceDescription.startsWith("[USER]") })
        assertEquals(1, persisted.count { it.triggerTopic == "İhanet" })
        assertEquals(1, persisted.count { it.triggerTopic == "Yalan" })
    }

    // =========================================================================
    // TEST 3 (Sistem #3): Double-Submit Lock & Atomic Bot Creation
    // =========================================================================
    @Test
    fun test3_system3_doubleSubmitGuard_createsSingleBotAndCommitsAtomically() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = EmochiViewModel(app)
        // Allow ViewModel init coroutines to settle
        delay(150)

        val botA = makeBot(id = "bot_double_submit_A")
        val botB = makeBot(id = "bot_double_submit_B")
        val triggers = listOf(
            SensitiveTriggerEntity(botId = botA.id, triggerTopic = "Terk edilme", sourceDescription = "Çocukluk", intensityMultiplier = 1.8)
        )

        val completedCount = AtomicInteger(0)
        // Fire two synchronous double-taps back-to-back on Main — AtomicBoolean lock MUST admit strictly 1
        vm.createBotFromWizard(botA, triggers, onComplete = { completedCount.incrementAndGet() })
        vm.createBotFromWizard(botB, triggers, onComplete = { completedCount.incrementAndGet() })

        // Pump Robolectric MainLooper + IO dispatchers until atomic creation finishes
        repeat(20) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            if (completedCount.get() > 0 && !vm.isCreatingBot.value) return@repeat
            delay(50)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Concurrent double-submit MUST create strictly 1 bot", 1, completedCount.get())

        // Verify direct repository.createBotAtomic writes bot + triggers + opening message atomically
        val atomicBot = makeBot(id = "bot_atomic_wizard_1")
        val saved = repository.createBotAtomic(
            bot = atomicBot,
            baselineTriggers = listOf(
                SensitiveTriggerEntity(botId = atomicBot.id, triggerTopic = "Güvensizlik", sourceDescription = "Geçmiş", intensityMultiplier = 1.9)
            )
        )
        assertNotNull(db.botDao().getBotById(saved.id))
        assertEquals(1, db.sensitiveTriggerDao().getTriggersForBot(saved.id).size)
        assertEquals(1, db.messageDao().getMessagesForBotList(saved.id).size)
    }

    // =========================================================================
    // TEST 4 (Sistem #9): CancellationException Isolation & resetHealth() Recovery
    // =========================================================================
    @Test
    fun test4_system9_cancellationDoesNotPenalizeHealth_andResetHealthClears401() = runBlocking {
        val settings = UserSettingsEntity(
            selectedProvider = "groq",
            groqApiKey = "gsk_test_123",
            openaiApiKey = "sk_test_456",
            enableAutoFallback = true,
            fallbackChainOrder = "groq,openai"
        )
        val manager = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = {},
            customExecutor = { _, _, _, _, _ ->
                throw CancellationException("User pressed stop")
            }
        )

        var caughtCancellation = false
        try {
            manager.executeWithFallback(
                model = "llama-3.3-70b-versatile",
                settings = settings,
                systemPrompt = "Test",
                messages = listOf(MessageEntity(id = "u1", botId = "b1", role = "user", text = "Selam")),
                buildConfigGeminiKey = ""
            )
        } catch (_: CancellationException) {
            caughtCancellation = true
        }

        assertTrue("CancellationException MUST propagate instead of falling back", caughtCancellation)
        val groqHealth = ProviderHealthTracker.getState("groq")
        assertEquals("Cancellation MUST NOT increment failure counter", 0, groqHealth?.totalFailureCount ?: 0)
        assertEquals(0L, ProviderHealthTracker.remainingCooldownMs("groq"))

        // Now trigger a 401 AUTH_ERROR cooldown and confirm resetHealth() clears it instantly
        ProviderHealthTracker.recordFailure("groq", ProviderFailureType.AUTH_ERROR, "HTTP 401 Unauthorized")
        assertTrue("401 should put provider into cooldown", ProviderHealthTracker.remainingCooldownMs("groq") > 0L)

        ProviderHealthTracker.resetHealth("groq")
        assertEquals("resetHealth() must immediately clear 401 cooldown", 0L, ProviderHealthTracker.remainingCooldownMs("groq"))
        assertNull(ProviderHealthTracker.getState("groq"))
    }

    // =========================================================================
    // TEST 5 (Sistem #9): Configurable strictApiTimeout Toggle Behavior
    // =========================================================================
    @Test
    fun test5_system9_configurableStrictApiTimeout_switchesLimitsCleanly() {
        AppFeatureFlags.setStrictApiTimeout(true)
        val strictUtil = ProviderFallbackManager.resolveTimeoutConfig(ProviderFallbackManager.ExecutionMode.UTILITY)
        val strictNarr = ProviderFallbackManager.resolveTimeoutConfig(ProviderFallbackManager.ExecutionMode.NARRATIVE)

        assertTrue(strictUtil.strictEnabled)
        assertEquals(ProviderFallbackManager.STRICT_UTILITY_TIMEOUT_MS, strictUtil.readTimeoutMs)
        assertEquals(ProviderFallbackManager.STRICT_UTILITY_MAX_ATTEMPTS, strictUtil.maxFallbackAttempts)

        assertTrue(strictNarr.strictEnabled)
        assertEquals(ProviderFallbackManager.STRICT_NARRATIVE_TIMEOUT_MS, strictNarr.readTimeoutMs)
        assertEquals(Int.MAX_VALUE, strictNarr.maxFallbackAttempts)

        AppFeatureFlags.setStrictApiTimeout(false)
        val relaxedUtil = ProviderFallbackManager.resolveTimeoutConfig(ProviderFallbackManager.ExecutionMode.UTILITY)
        val relaxedNarr = ProviderFallbackManager.resolveTimeoutConfig(ProviderFallbackManager.ExecutionMode.NARRATIVE)

        assertFalse(relaxedUtil.strictEnabled)
        assertEquals(0L, relaxedUtil.readTimeoutMs)
        assertEquals(ProviderFallbackManager.UNLIMITED_SAFETY_CALL_TIMEOUT_MS, relaxedUtil.callTimeoutMs)

        assertFalse(relaxedNarr.strictEnabled)
        assertEquals(0L, relaxedNarr.readTimeoutMs)
        assertEquals(ProviderFallbackManager.UNLIMITED_SAFETY_CALL_TIMEOUT_MS, relaxedNarr.callTimeoutMs)
    }

    // =========================================================================
    // TEST 6 (Sistem #11): EmotionalTtsProsodyEngine — Mood Contrast, Dialogue vs Narration, SSML Escaping
    // =========================================================================
    @Test
    fun test6_system11_emotionalProsodyEngine_moodContrastDialogueSplitAndXmlEscaping() {
        val sampleText = "*Pencereden süzülen yağmuru izler...* \"Bana 2 < 3 & 'güven' sözü verdin!\""

        val sadState = EmotionState(
            primaryEmotions = PrimaryEmotions(sadness = 78),
            dominantEmotion = "hüzünlü"
        )
        val angryState = EmotionState(
            primaryEmotions = PrimaryEmotions(anger = 82),
            dominantEmotion = "öfkeli"
        )

        val sadSegments = EmotionalTtsProsodyEngine.buildProsodySegments(
            rawText = sampleText,
            emotionState = sadState,
            readActions = true,
            emotionalProsodyEnabled = true
        )
        val angrySegments = EmotionalTtsProsodyEngine.buildProsodySegments(
            rawText = sampleText,
            emotionState = angryState,
            readActions = true,
            emotionalProsodyEnabled = true
        )

        assertTrue("Must separate narration and dialogue segments", sadSegments.size >= 2)
        assertEquals(EmotionalTtsProsodyEngine.SegmentKind.NARRATION, sadSegments[0].kind)
        assertEquals(EmotionalTtsProsodyEngine.SegmentKind.DIALOGUE, sadSegments[1].kind)

        val sadDialogue = sadSegments[1]
        val angryDialogue = angrySegments[1]
        assertTrue("Sad emotion must speak slower than angry emotion", sadDialogue.ratePercent < angryDialogue.ratePercent)
        assertTrue("Sad emotion must have lower pitch than angry emotion", sadDialogue.pitchHz < angryDialogue.pitchHz)
        assertTrue("Sad narration with ellipsis must pause longer than angry exclamation", sadSegments[0].pauseAfterMs > angryDialogue.pauseAfterMs)

        val ssml = EmotionalTtsProsodyEngine.buildSsml("tr-TR-EmelNeural", sadSegments)
        assertTrue("SSML must contain <prosody> tags", ssml.contains("<prosody "))
        assertTrue("SSML must contain <break> pause between segments", ssml.contains("<break time='"))
        assertTrue("SSML must escape '<' to &lt;", ssml.contains("&lt;"))
        assertTrue("SSML must escape '&' to &amp;", ssml.contains("&amp;"))
        assertFalse("Raw unescaped '2 < 3' must never appear in SSML", ssml.contains("2 < 3"))
    }

    // =========================================================================
    // TEST 7 (Sistem #11): ChatTtsManager — Layer 1 → Layer 2 Silent Fallback, Cold-Start Queue & State Reset
    // =========================================================================
    @Test
    fun test7_system11_chatTtsManager_neuralToDeviceSilentFallback_coldStartAndReset() = runBlocking {
        AppFeatureFlags.setTtsEngineMode(AppFeatureFlags.TTS_ENGINE_NEURAL)
        // Simulate offline/failed Edge Neural synthesis
        EdgeNeuralTtsClient.synthesizerOverride = { _, _ -> null }

        val manager = ChatTtsManager(context)
        val spokenByDevice = AtomicInteger(0)
        manager.deviceSpeakOverride = { segments, _ ->
            spokenByDevice.addAndGet(segments.size)
            true
        }

        // Cold-start: speakMessage before TextToSpeech.onInit finishes -> neural fails -> queued in pendingSpeakRequest
        val started = manager.speakMessage(
            messageId = "msg_tts_fallback_1",
            rawText = "\"Yağmur altında seni bekledim.\"",
            emotionState = EmotionState(dominantEmotion = "romantik", primaryEmotions = PrimaryEmotions(joy = 40))
        )
        assertTrue(started)
        repeat(10) {
            delay(30)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }

        // Simulate Android TextToSpeech onInit(SUCCESS) completing after cold start
        manager.simulateInitCompleteForTest()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertEquals("Failed Layer 1 Neural MUST fall back silently to Layer 2 Device", "device", manager.lastActiveTier)
        assertTrue("Queued cold-start segments must be spoken on init", spokenByDevice.get() >= 1)
        assertEquals("msg_tts_fallback_1", manager.speakingMessageId.value)

        // Utterance error MUST reset speakingMessageId = null so equalizer never gets stuck
        manager.simulateUtteranceErrorForTest()
        assertNull("speakingMessageId must reset to null on error", manager.speakingMessageId.value)
        assertFalse("isSpeaking must reset to false on error", manager.isSpeaking.value)

        // Also verify stop() resets cleanly
        manager.speakMessage(messageId = "msg_tts_stop_2", rawText = "Test cümlesi.")
        delay(80)
        manager.stop()
        assertNull(manager.speakingMessageId.value)
        assertFalse(manager.isSpeaking.value)

        manager.shutdown()
    }

    // =========================================================================
    // TEST 8 (Sistem #13): Backup v2 Full Tables, Custom Key Preservation & Corrupt/Future Validation
    // =========================================================================
    @Test
    fun test8_system13_backupV2FullTables_keyPreservation_andValidationGuard() = runBlocking {
        val bot = makeBot(id = "bot_backup_full")
        db.botDao().insertOrUpdate(bot)
        db.messageDao().insertMessage(MessageEntity(id = "m_b1", botId = bot.id, role = "assistant", text = "Selam", timestamp = 1_000_000L))
        db.userSettingsDao().insertOrUpdate(UserSettingsEntity(groqApiKey = "gsk_LOCAL_SUPER_SECRET"))
        db.customProviderDao().insertProvider(
            CustomProviderEntity(
                label = "Benim Sunucum",
                baseUrl = "https://my-llm.local/v1",
                apiKeyEncrypted = SecureApiKeyVault.encrypt("sk_CUSTOM_SECRET_999"),
                modelName = "llama-3.3-70b"
            )
        )
        db.pendingReappraisalDao().insert(
            PendingReappraisalEntity(
                botId = bot.id,
                originalEventDescription = "Sert tartışma",
                originalEmotionState = "{}",
                triggeredAtMessageIndex = 5,
                triggeredAtTimestamp = 1_000_000L,
                reappraisalEligibleAtMessageIndex = 20,
                reappraisalEligibleAtTimestamp = 1_500_000L,
                resolved = false,
                expired = false
            )
        )
        db.archivedMemoryDao().insertArchive(
            ArchivedMemoryEntity(
                botId = bot.id,
                originalMemoryId = 42L,
                memoryType = "fact",
                content = "Selin | kahve | espresso",
                archivedReason = "natural_decay_ttl"
            )
        )

        // Default export masks BOTH built-in and customProvider API keys while including all v2 tables
        val exported = repository.exportDataSnapshot(includeApiKeys = false)
        assertFalse("Built-in key must be masked by default", exported.contains("gsk_LOCAL_SUPER_SECRET"))
        assertFalse("Custom provider key must be masked by default", exported.contains("sk_CUSTOM_SECRET_999"))
        assertTrue("Export must include customProviders", exported.contains("Benim Sunucum"))
        assertTrue("Export must include pendingReappraisals", exported.contains("Sert tartışma"))
        assertTrue("Export must include archivedMemories", exported.contains("espresso"))

        // Corrupt JSON & unsupported future schema version MUST fail validation without touching DB
        val corruptValidation = repository.validateBackupJson("{not valid json!!!")
        assertFalse(corruptValidation.isValid)

        val futureValidation = repository.validateBackupJson("""{"version": 99, "bots": [], "settings": {"id": 1}}""")
        assertFalse("Future version v99 must be rejected", futureValidation.isValid)

        var threwOnCorrupt = false
        try {
            repository.importDataSnapshot("{corrupt", overwriteExisting = true)
        } catch (_: IllegalArgumentException) {
            threwOnCorrupt = true
        }
        assertTrue(threwOnCorrupt)
        assertNotNull("Existing bot must remain untouched after rejected corrupt import", db.botDao().getBotById(bot.id))

        // Restore sanitized backup over existing device: local built-in & custom provider keys MUST survive
        repository.importDataSnapshot(exported, overwriteExisting = false)
        val restoredSettings = db.userSettingsDao().getUserSettings()
        assertEquals("gsk_LOCAL_SUPER_SECRET", restoredSettings?.groqApiKey)

        val restoredCustom = db.customProviderDao().getAllProviders().firstOrNull { it.label == "Benim Sunucum" }
        assertNotNull(restoredCustom)
        assertEquals("sk_CUSTOM_SECRET_999", SecureApiKeyVault.decrypt(restoredCustom!!.apiKeyEncrypted))
        assertEquals(1, db.pendingReappraisalDao().getAllForBot(bot.id).size)
        assertEquals(1, db.archivedMemoryDao().getArchivesForBot(bot.id).size)
    }

    // =========================================================================
    // TEST 9 (Sistem #13): Streaming 1,500-Message ChatExporter + 24h Stale Cleanup
    // =========================================================================
    @Test
    fun test9_system13_streamingChatExporter_1500Messages_andStaleCleanup() {
        val bot = makeBot(id = "bot_stream_export")
        val now = 1_760_000_000_000L
        val messages = (1..1500).map { i ->
            MessageEntity(
                id = "msg_$i",
                botId = bot.id,
                role = if (i % 2 == 0) "assistant" else "user",
                text = "Bölüm $i — Mor salkımlar altında uzun edebi sahne metni ve diyalog satırı.",
                timestamp = now - (1500 - i) * 60_000L,
                isStarred = (i == 1500)
            )
        }

        // Plant a stale export older than 24h and a fresh export
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val staleFile = File(exportsDir, "stale_export.txt").apply {
            writeText("old")
            setLastModified(now - 30L * 3600L * 1000L)
        }
        val freshFile = File(exportsDir, "fresh_export.txt").apply {
            writeText("fresh")
            setLastModified(now - 2L * 3600L * 1000L)
        }

        val txtFile = ChatExporter.exportToFile(context, bot, messages, ChatExporter.ExportFormat.TEXT, ChatExporter.DateRange.ALL, now)
        val mdFile = ChatExporter.exportToFile(context, bot, messages, ChatExporter.ExportFormat.MARKDOWN, ChatExporter.DateRange.ALL, now)
        val jsonFile = ChatExporter.exportToFile(context, bot, messages, ChatExporter.ExportFormat.JSON, ChatExporter.DateRange.ALL, now)

        assertFalse("Stale >24h export file must be auto-cleaned", staleFile.exists())
        assertTrue("Fresh <24h export file must remain", freshFile.exists())

        assertTrue(txtFile.exists() && txtFile.length() > 50_000L)
        assertTrue(mdFile.exists() && mdFile.length() > 50_000L)
        val parsedJson = JSONObject(jsonFile.readText(Charsets.UTF_8))
        assertEquals(1500, parsedJson.getInt("messageCount"))
        assertEquals(1500, parsedJson.getJSONArray("messages").length())
    }

    // =========================================================================
    // TEST 10 (U1): Starter & Template-Origin Bots Can NEVER Be Public
    // =========================================================================
    @Test
    fun test10_u1_starterAndTemplateBots_cannotBePublishedOrClonedPublic() = runBlocking {
        val starterAyla = makeBot(id = "starter_ayla", isPublic = true)
        val templateBot = makeBot(id = "custom_tpl_1", isPublic = true, isTemplate = true)
        val userFromTemplate = makeBot(id = "user_from_tpl_9", isPublic = true, isTemplate = false)
        AppFeatureFlags.markTemplateOriginBot(userFromTemplate.id)
        val originalUserBot = makeBot(id = "user_original_1", isPublic = true, isTemplate = false)

        assertFalse(EmochiRepository.isBotPublishable(starterAyla))
        assertFalse(EmochiRepository.isBotPublishable(templateBot))
        assertFalse(EmochiRepository.isBotPublishable(userFromTemplate))
        assertTrue(EmochiRepository.isBotPublishable(originalUserBot))

        // saveBot & createBotAtomic must silently force isPublic = false for non-publishable bots
        repository.saveBot(starterAyla)
        repository.createBotAtomic(userFromTemplate, createOpeningMessage = false)
        repository.saveBot(originalUserBot)

        assertFalse(db.botDao().getBotById(starterAyla.id)!!.isPublic)
        assertFalse(db.botDao().getBotById(userFromTemplate.id)!!.isPublic)
        assertTrue(db.botDao().getBotById(originalUserBot.id)!!.isPublic)

        // Cloning a starter/template bot must keep the clone non-publishable too!
        val clonedStarter = repository.cloneBot(starterAyla.id)
        assertNotNull(clonedStarter)
        assertFalse(clonedStarter!!.isPublic)
        assertFalse("Clone of starter bot must inherit template-origin lock", EmochiRepository.isBotPublishable(clonedStarter))
    }

    // =========================================================================
    // TEST 11 (U2): AI-Selected Small Utility Model Across Built-In & Custom Providers
    // =========================================================================
    @Test
    fun test11_u2_aiSelectedUtilityModel_appliesToAllBuiltinAndCustomProviders() {
        val settings = UserSettingsEntity()
        val custom = CustomProviderEntity(
            id = 7L,
            label = "MyGateway",
            baseUrl = "https://gw.example/v1",
            apiKeyEncrypted = "",
            modelName = "big-model-70b"
        )

        // Defaults before AI selection
        assertEquals("openai/gpt-oss-20b", EmochiRepository.resolveUtilityModel("groq", settings))
        assertEquals("gemini-2.0-flash", EmochiRepository.resolveUtilityModel("gemini", settings))

        // AI picks optimal small utility models for built-in AND custom providers
        AppFeatureFlags.setAiUtilityModel("groq", "llama-3.2-3b-preview", source = "ai")
        AppFeatureFlags.setAiUtilityModel("gemini", "gemini-2.5-flash-lite", source = "ai")
        AppFeatureFlags.setAiUtilityModel("openai", "gpt-4.1-nano", source = "ai")
        AppFeatureFlags.setAiUtilityModel("custom_7", "qwen2.5-7b-instruct", source = "ai")

        assertEquals("llama-3.2-3b-preview", EmochiRepository.resolveUtilityModel("groq", settings))
        assertEquals("gemini-2.5-flash-lite", EmochiRepository.resolveUtilityModel("gemini", settings))
        assertEquals("gpt-4.1-nano", EmochiRepository.resolveUtilityModel("openai", settings))
        assertEquals("qwen2.5-7b-instruct", EmochiRepository.resolveUtilityModel("custom_7", settings, custom))
        assertEquals("ai", AppFeatureFlags.getAiUtilityModelSource("groq"))
    }

    // =========================================================================
    // TEST 12 (U3): Empty / <think>-Only Response Recovery via Tool-Free Retry & Provider Fallback
    // =========================================================================
    @Test
    fun test12_u3_emptyAndThinkOnlyResponses_recoverCleanlyViaRetryAndFallback() = runBlocking {
        val settings = UserSettingsEntity(
            selectedProvider = "groq",
            groqApiKey = "gsk_test",
            openaiApiKey = "sk_test",
            enableAutoFallback = true,
            fallbackChainOrder = "groq,openai"
        )

        // Scenario A: Provider returns <think>-only when called with tools, then succeeds on automatic tool-free retry
        val groqAttempts = AtomicInteger(0)
        val managerRetry = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = {},
            customExecutor = { providerKey, _, _, _, tools ->
                if (providerKey == "groq") {
                    val n = groqAttempts.incrementAndGet()
                    if (n == 1) {
                        ModelResponseResult(
                            text = "<think>Sadece iç düşünce döndü</think>",
                            usedProvider = "groq"
                        )
                    } else {
                        assertTrue("Empty-response retry must strip tools", tools.isEmpty())
                        ModelResponseResult(
                            text = "*Gülümseyerek başını salladı.* \"Buradayım, seni dinliyorum.\"",
                            usedProvider = "groq"
                        )
                    }
                } else {
                    ModelResponseResult(text = "Fallback", usedProvider = providerKey)
                }
            }
        )

        val retryResult = managerRetry.executeWithFallback(
            model = "llama-3.3-70b-versatile",
            settings = settings,
            systemPrompt = "Sen Aiden'sın.",
            messages = listOf(MessageEntity(id = "m1", botId = "b1", role = "user", text = "Neredesin?")),
            buildConfigGeminiKey = "",
            tools = MemoryToolRegistry.CENTRAL_TOOLS
        )
        assertTrue(retryResult.text.contains("Buradayım"))
        assertEquals("groq", retryResult.usedProvider)
        assertEquals(2, groqAttempts.get())

        // Scenario B: First provider stays persistently empty -> seamlessly falls back to second provider
        val managerFallback = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = {},
            customExecutor = { providerKey, _, _, _, _ ->
                if (providerKey == "groq") {
                    ModelResponseResult(text = "   [[STATE_JSON {}]]   ", usedProvider = "groq")
                } else {
                    ModelResponseResult(
                        text = "\"İkinci sağlayıcıdan eksiksiz yanıt geldi!\"",
                        usedProvider = providerKey
                    )
                }
            }
        )

        val fallbackResult = managerFallback.executeWithFallback(
            model = "llama-3.3-70b-versatile",
            settings = settings,
            systemPrompt = "Sen Aiden'sın.",
            messages = listOf(MessageEntity(id = "m2", botId = "b1", role = "user", text = "Devam et")),
            buildConfigGeminiKey = ""
        )
        assertNotEquals("groq", fallbackResult.usedProvider)
        assertEquals("openai", fallbackResult.usedProvider)
        assertTrue(fallbackResult.text.contains("eksiksiz yanıt"))
    }

    // =========================================================================
    // TEST 13 (Smoke): MainActivity Cold Launch & BotListScreen Composition
    // =========================================================================
    @Test
    fun test13_mainActivityColdLaunch_rendersMenuWithoutCrash() {
        // Use realistic 60Hz VSYNC (16ms) in Robolectric PAUSED looper mode so Compose
        // draw-phase infinite animations don't re-post 0ms frames infinitely during idle().
        @Suppress("DEPRECATION")
        org.robolectric.shadows.ShadowChoreographer.setPostFrameCallbackDelay(16)
        try {
            val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
                .create()
                .start()
                .resume()
                .visible()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            val activity = controller.get()
            assertNotNull("MainActivity must launch cleanly", activity)
            assertFalse("MainActivity must not finish immediately", activity.isFinishing)
            controller.pause().stop().destroy()
        } finally {
            org.robolectric.shadows.ShadowChoreographer.reset()
        }
    }
}
