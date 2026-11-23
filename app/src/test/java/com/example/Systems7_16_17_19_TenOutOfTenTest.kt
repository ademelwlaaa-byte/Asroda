package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.EmotionState
import com.example.data.local.MessageEntity
import com.example.data.local.PersonalityProfile
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.BackupSnapshot
import com.example.data.repository.BaselineEmotionEngine
import com.example.data.repository.CastDetectorEngine
import com.example.data.repository.EmochiRepository
import com.example.data.repository.MicroAiCastMemberDto
import com.example.data.repository.MicroAiCastResponse
import com.example.data.repository.SystemPromptBuilder
import com.example.ui.animation.CosmicMotionConfig
import com.example.ui.animation.MotionIntensityMode
import com.example.ui.animation.MotionPolicy
import com.example.util.AppFeatureFlags
import com.example.util.DynamicDifficultyCalibrator
import com.example.util.MicroAiSentimentDto
import com.example.util.SecureApiKeyVault
import com.example.util.SentimentAnalysisResult
import com.example.util.UserMood
import com.example.util.UserSentimentAnalyzer
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
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
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Systems7_16_17_19_TenOutOfTenTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: EmochiRepository

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = EmochiRepository(db, ctx)
        AppFeatureFlags.setDynamicDifficulty(true)
        AppFeatureFlags.setMicroAiCastSentiment(true)
        AppFeatureFlags.setReduceMotion(false)
    }

    @After
    fun tearDown() {
        repo.close()
        db.close()
        AppFeatureFlags.setDynamicDifficulty(true)
        AppFeatureFlags.setMicroAiCastSentiment(true)
        AppFeatureFlags.setReduceMotion(false)
    }

    private fun createTestBot(
        id: String = "bot_s16_17",
        aiName: String = "Asroda",
        userCharName: String = "Eren",
        baseDifficulty: Double = 1.0,
        attachmentStyle: String = "secure",
        regulationCapacity: Int = 55
    ): BotEntity {
        val profile = PersonalityProfile(attachmentStyle = attachmentStyle)
        return BotEntity(
            id = id,
            mode = "personal",
            aiName = aiName,
            aiPersonality = if (attachmentStyle.contains("avoidant")) "Soğuk, mesafeli ve inatçı" else "Sıcak ve anlayışlı",
            scenario = "İstanbul'da yağmurlu bir akşam",
            universeName = "",
            keyCharactersJson = "[]",
            userCharName = userCharName,
            userCharDesc = "Gezgin",
            openingMessage = "Merhaba.",
            writingStyle = "novel",
            intensity = "normal",
            baseAffectionDifficulty = baseDifficulty,
            baselineEmotionAnalysisJson = profile.toJson(),
            emotionalRegulationCapacity = regulationCapacity
        )
    }

    // ── 1. Sistem #7: Motion Policy (Reduce Motion / Background / Power Save) ──

    @Test
    fun test1_system7_motionPolicyPausesInBackgroundReduceMotionAndPowerSave() {
        // Normal foreground -> continuous animation allowed
        val normalForeground = MotionPolicy(
            reduceMotion = false,
            isAppInForeground = true,
            isPowerSaveMode = false,
            isSystemAnimatorDisabled = false
        )
        assertTrue(normalForeground.shouldAnimateContinuously)
        assertFalse(normalForeground.shouldUseQuickTransitions)
        val fullCfg = CosmicMotionConfig.effective(MotionIntensityMode.FULL, normalForeground)
        assertTrue(fullCfg.enableAmbientLoops)
        assertTrue(fullCfg.enableParticles)

        // reduceMotion = true -> 0% continuous animation, quick transitions
        val reduceMotionPolicy = MotionPolicy(
            reduceMotion = true,
            isAppInForeground = true,
            isPowerSaveMode = false,
            isSystemAnimatorDisabled = false
        )
        assertFalse(reduceMotionPolicy.shouldAnimateContinuously)
        assertTrue(reduceMotionPolicy.shouldUseQuickTransitions)
        val reducedCfg = CosmicMotionConfig.effective(MotionIntensityMode.FULL, reduceMotionPolicy)
        assertFalse(reducedCfg.enableAmbientLoops)
        assertFalse(reducedCfg.enableParticles)

        // isAppInForeground = false (ON_STOP) -> 0% GPU background stop
        val backgroundPolicy = MotionPolicy(
            reduceMotion = false,
            isAppInForeground = false,
            isPowerSaveMode = false,
            isSystemAnimatorDisabled = false
        )
        assertFalse(backgroundPolicy.shouldAnimateContinuously)

        // isPowerSaveMode = true -> paused
        val powerSavePolicy = MotionPolicy(
            reduceMotion = false,
            isAppInForeground = true,
            isPowerSaveMode = true,
            isSystemAnimatorDisabled = false
        )
        assertFalse(powerSavePolicy.shouldAnimateContinuously)
        assertTrue(powerSavePolicy.shouldUseQuickTransitions)

        // System ANIMATOR_DURATION_SCALE == 0f -> paused
        val systemZeroScale = MotionPolicy(
            reduceMotion = false,
            isAppInForeground = true,
            isPowerSaveMode = false,
            isSystemAnimatorDisabled = true
        )
        assertFalse(systemZeroScale.shouldAnimateContinuously)
    }

    // ── 2. Sistem #16: Türkçe Ek Normalizasyonu & Kara Liste Kilidi ──────────

    @Test
    fun test2_system16_turkishSuffixNormalizationAndBlacklistAliasLock() = runBlocking {
        // Turkish suffix stemming -> single canonical entity "Leyla"
        assertEquals("Leyla", CastDetectorEngine.normalizeTurkishEntityName("Leyla'nın"))
        assertEquals("Leyla", CastDetectorEngine.normalizeTurkishEntityName("Leyla'yla"))
        assertEquals("Leyla", CastDetectorEngine.normalizeTurkishEntityName("Leyla'ya"))
        assertEquals("Leyla", CastDetectorEngine.normalizeTurkishEntityName("Leyla’dan"))
        assertEquals("Elif", CastDetectorEngine.normalizeTurkishEntityName("Doktor Elif'e"))

        // Blacklist rejects days, places, verbs, and main bot/user names
        val bot = createTestBot(id = "bot_bl_lock", aiName = "Asroda", userCharName = "Eren")
        db.botDao().insertOrUpdate(bot)

        val blacklist = CastDetectorEngine.getBlacklist(ApplicationProvider.getApplicationContext())
        assertTrue("galata" in blacklist)
        assertTrue("pazartesi" in blacklist)
        assertTrue("ocak" in blacklist)
        assertTrue("baktı" in blacklist)

        val noisyHits = CastDetectorEngine.detectCandidates(
            combinedText = "Pazartesi günü Galata Kulesi önünde Baktı ve Karanlık çöktü. Asroda gülümsedi.",
            blacklist = blacklist + setOf("asroda", "eren")
        )
        assertFalse(noisyHits.any { it.canonicalName.equals("Galata", ignoreCase = true) })
        assertFalse(noisyHits.any { it.canonicalName.equals("Pazartesi", ignoreCase = true) })
        assertFalse(noisyHits.any { it.canonicalName.equals("Karanlık", ignoreCase = true) })
        assertFalse(noisyHits.any { it.canonicalName.equals("Asroda", ignoreCase = true) })

        // Blacklist lock: once "Elif" (with alias "Doktor Elif") is blacklisted, neither name nor alias can re-enter
        repo.managers.castMemberManager.ensureSideCharacterRegistered(
            botId = bot.id,
            rawCharacterName = "Doktor Elif",
            aliases = listOf("Dr. Elif", "Başhekim"),
            bot = bot,
            confidence = 90,
            pendingConfirmation = false
        )
        val createdElif = db.castMemberDao().findByName(bot.id, "Elif")
        assertNotNull(createdElif)
        repo.managers.castMemberManager.blacklistCastMember(bot.id, createdElif!!.id)

        // Attempt re-registration via alias AND via Micro-AI result
        repo.managers.castMemberManager.ensureSideCharacterRegistered(
            botId = bot.id,
            rawCharacterName = "Başhekim",
            aliases = listOf("Doktor Elif"),
            bot = bot,
            confidence = 95,
            pendingConfirmation = false
        )
        val activeAfter = repo.managers.castMemberManager.getCastMembersForBot(bot.id)
        assertTrue("Blacklisted character must NEVER re-enter via alias", activeAfter.none { it.name == "Elif" || it.name == "Başhekim" })
    }

    // ── 3. Sistem #16: Ufak AI (UTILITY) Kadro Çıkarımı, Yanlış Pozitif Eleme & Maks 4 ──

    @Test
    fun test3_system16_microAiCastExtractionFiltersFalsePositivesAndCapsAtMax4() = runBlocking {
        val bot = createTestBot(id = "bot_micro_cast")
        db.botDao().insertOrUpdate(bot)

        // Blacklist "Sarp" in DB to verify Micro-AI cannot revive blacklisted names
        db.castMemberDao().insertCastMember(
            CastMemberEntity(
                botId = bot.id,
                name = "Sarp",
                description = "",
                isBlacklisted = true
            )
        )
        val blacklistedDb = repo.managers.castMemberManager.getBlacklistedAliasTokensForBot(bot.id)

        val mockUtilityJson = """
            {
              "cast": [
                {"name":"Kadir'in","is_person":true,"aliases":["yaşlı saatçi"],"role":"saat ustası","relation":"mahalle tanıdığı","age":62,"in_scene":true},
                {"name":"Galata","is_person":false,"aliases":[],"role":"mekan","relation":"","age":null,"in_scene":true},
                {"name":"Sarp","is_person":true,"aliases":["eski bekçi"],"role":"bekçi","relation":"","age":45,"in_scene":true}
              ],
              "rejected": ["Galata", "Karanlık"]
            }
        """.trimIndent()

        val result = CastDetectorEngine.extractAndValidateCastWithMicroAi(
            recentTwoMessagesText = "Kapının eşiğinde duran yaşlı saatçi Kadir sessizce gülümsedi; Galata sisler içindeydi.",
            botName = bot.aiName,
            userCharName = bot.userCharName,
            knownNames = emptySet(),
            blacklistedDbNames = blacklistedDb,
            utilityCaller = { _, _, _ -> mockUtilityJson }
        )

        assertTrue(result.usedMicroAi)
        assertEquals(1, result.members.size)
        val kadirDto = result.members.first()
        assertEquals("Kadir", kadirDto.name)
        assertEquals("saat ustası", kadirDto.role)
        assertEquals(62, kadirDto.age)
        assertTrue(kadirDto.aliases.contains("yaşlı saatçi"))
        assertTrue("galata" in result.rejected)

        repo.managers.castMemberManager.applyMicroAiCastExtractionResult(
            botId = bot.id,
            result = result,
            bot = bot,
            sceneContext = "Yaşlı saatçi Kadir sessizce gülümsedi."
        )
        val storedKadir = db.castMemberDao().findByName(bot.id, "Kadir")
        assertNotNull(storedKadir)
        assertEquals("saat ustası", storedKadir!!.role)
        assertEquals(62, storedKadir.currentAge)
        assertTrue(CastDetectorEngine.extractStoredAliases(storedKadir.multiRoleJson).contains("yaşlı saatçi"))

        // Verify filterActiveInSceneCastMembers strictly caps at max 4 active characters
        val sixCast = (1..6).map { idx ->
            CastMemberEntity(
                id = idx.toLong(),
                botId = bot.id,
                name = "Karakter$idx",
                description = "Rol $idx",
                importanceScore = 92 - idx,
                appearanceCount = 10 - idx
            )
        }
        val activeScene = CastDetectorEngine.filterActiveInSceneCastMembers(
            allCastMembers = sixCast,
            recentMessagesText = "Karakter1, Karakter2, Karakter3, Karakter4, Karakter5 ve Karakter6 salondaydı."
        )
        assertEquals("Active in-scene cast MUST be capped at max 4", 4, activeScene.size)
    }

    // ── 4. Sistem #16: Ufak AI Kapalı / Hata Fallback Testi ──────────────────

    @Test
    fun test4_system16_microAiDisabledAndErrorSilentlyFallbackToLayer1() = runBlocking {
        val sampleScene = "Komiser Hakan kapıyı çaldı ve içeri girdi."
        val callCount = AtomicInteger(0)

        // A) Feature flag OFF -> 0 Utility calls, local Layer 1 still extracts "Hakan"
        AppFeatureFlags.setMicroAiCastSentiment(false)
        val offResult = CastDetectorEngine.extractAndValidateCastWithMicroAi(
            recentTwoMessagesText = sampleScene,
            botName = "Asroda",
            userCharName = "Eren",
            utilityCaller = { _, _, _ ->
                callCount.incrementAndGet()
                "{}"
            }
        )
        assertEquals(0, callCount.get())
        assertFalse(offResult.usedMicroAi)
        assertTrue(offResult.members.any { it.name == "Hakan" })

        // B) Feature flag ON, Utility AI throws exception -> silent fallback to Layer 1 with zero crash
        AppFeatureFlags.setMicroAiCastSentiment(true)
        val errorResult = CastDetectorEngine.extractAndValidateCastWithMicroAi(
            recentTwoMessagesText = sampleScene,
            botName = "Asroda",
            userCharName = "Eren",
            utilityCaller = { _, _, _ ->
                callCount.incrementAndGet()
                throw RuntimeException("Simulated 503 offline error")
            }
        )
        assertEquals(1, callCount.get())
        assertFalse(errorResult.usedMicroAi)
        assertTrue(errorResult.members.any { it.name == "Hakan" })

        // C) Trivial short turn -> shouldInvokeMicroAiCastExtraction returns false (saves quota)
        assertFalse(CastDetectorEngine.shouldInvokeMicroAiCastExtraction("Tamam geliyorum"))
    }

    // ── 5. Sistem #17: Türkçe Olumsuzlama, İroni & OOC Yerel Testi ───────────

    @Test
    fun test5_system17_turkishNegationIronyAndOocLocalSensor() {
        // 1. "hiç de mutlu değilim" -> negative score & NEGATIVE mood
        val negatedPos = UserSentimentAnalyzer.analyzeLocalDetailed("hiç de mutlu değilim")
        assertTrue("Expected negative valence, got ${negatedPos.valence}", negatedPos.valence < 0f)
        assertEquals(UserMood.NEGATIVE, negatedPos.mood)

        // 2. "kızgın değilim, merak etme" -> must NOT trigger anger/negative
        val negatedNeg = UserSentimentAnalyzer.analyzeLocalDetailed("kızgın değilim, merak etme")
        assertTrue("Expected non-negative valence, got ${negatedNeg.valence}", negatedNeg.valence >= 0f)
        assertTrue("Must not be NEGATIVE", negatedNeg.mood != UserMood.NEGATIVE)

        // 3. "güya çok sevindim (!)" -> no positive inflation + needsMicroAiDisambiguation = true
        val ironic = UserSentimentAnalyzer.analyzeLocalDetailed("güya çok sevindim (!)")
        assertTrue("Irony must not inflate positive score (got ${ironic.valence})", ironic.valence <= 0f)
        assertTrue("Irony must not resolve to POSITIVE mood", ironic.mood != UserMood.POSITIVE)
        assertTrue("Irony must flag needsMicroAiDisambiguation", ironic.needsMicroAiDisambiguation)

        // 4. "((biraz hızlı geçelim))" -> pure OOC, does not pollute IC conflict
        val ooc = UserSentimentAnalyzer.analyzeLocalDetailed("((biraz hızlı geçelim))")
        assertTrue(ooc.isOocMeta)
        assertEquals("biraz hızlı geçelim", ooc.oocDirective)
        assertEquals(0f, ooc.valence, 0.0001f)
        assertEquals(UserMood.NEUTRAL, ooc.mood)
    }

    // ── 6. Sistem #17: Ufak AI Alt Metin Harmanlama, 2.5sn Timeout & Kısa Mesaj Skibi ──

    @Test
    fun test6_system17_microAiSubtextBlendTimeoutFallbackAndShortSkip() = runBlocking {
        AppFeatureFlags.setMicroAiCastSentiment(true)
        val passiveMsg = "neyse boşver, sen haklısın..."
        val localBaseline = UserSentimentAnalyzer.analyzeLocalDetailed(passiveMsg)

        // A) Mock Utility AI succeeds -> 0.70 AI + 0.30 Local blend + subtext hint
        val blended = UserSentimentAnalyzer.analyzeSentimentWithMicroAi(
            userMessage = passiveMsg,
            lastBotMessageSummary = "Sözünü kestim.",
            timeoutMs = 2500L,
            utilityCaller = { _, _, _ ->
                """{"tone":"hurt","valence":-0.75,"intensity":0.80,"is_ooc_meta":false,"subtext":"kırgınlığını gizliyor","resistance_delta":-0.08}"""
            }
        )
        assertTrue(blended.usedMicroAi)
        val expectedValence = (0.70f * -0.75f + 0.30f * localBaseline.valence).coerceIn(-1f, 1f)
        assertEquals(expectedValence, blended.valence, 0.01f)
        assertEquals("kırgınlığını gizliyor", blended.subtext)
        assertEquals("hurt", blended.tone)
        val hint = UserSentimentAnalyzer.buildSubtextPromptHint(blended)
        assertTrue(hint.contains("[KULLANICI ALT METNİ]: kırgınlığını gizliyor"))

        // B) Mock Utility AI delays 3.0s -> 2.5s race timeout triggers clean fallback to Layer 1
        val startMs = System.currentTimeMillis()
        val timedOut = UserSentimentAnalyzer.analyzeSentimentWithMicroAi(
            userMessage = passiveMsg,
            timeoutMs = 2500L,
            utilityCaller = { _, _, _ ->
                delay(3000L)
                """{"tone":"warm","valence":0.9,"intensity":0.9,"is_ooc_meta":false,"subtext":"geç geldi","resistance_delta":0.1}"""
            }
        )
        val elapsedMs = System.currentTimeMillis() - startMs
        assertFalse("Timed-out Micro-AI must fall back to Layer 1", timedOut.usedMicroAi)
        assertTrue("Race timeout must not wait full 3s+ (elapsed=${elapsedMs}ms)", elapsedMs < 2950L)

        // C) Short unambiguous message "Tamam" -> 0 Utility AI calls
        val shortCalls = AtomicInteger(0)
        val shortRes = UserSentimentAnalyzer.analyzeSentimentWithMicroAi(
            userMessage = "Tamam",
            utilityCaller = { _, _, _ ->
                shortCalls.incrementAndGet()
                "{}"
            }
        )
        assertEquals("Short unambiguous message must not invoke Micro-AI", 0, shortCalls.get())
        assertFalse(shortRes.usedMicroAi)
    }

    // ── 7. Sistem #17: EMA Histerezis, OOC Koruması & Kişilik Çapası ─────────

    @Test
    fun test7_system17_emaHysteresisOocImmunityFeatureFlagAndAvoidantAnchor() {
        // A) Single harsh or warm turn -> EMA smooth shift (<= 0.15, no abrupt jump)
        val secureBot = createTestBot(id = "bot_ema_sec", baseDifficulty = 1.0, attachmentStyle = "secure")
        val harshTurn = DynamicDifficultyCalibrator.calibrateTurn(
            bot = secureBot,
            sentiment = SentimentAnalysisResult(
                tone = "tense",
                valence = -1.0f,
                intensity = 1.0f,
                isOocMeta = false,
                resistanceDelta = 0.50f // should be clamped to +0.15f
            )
        )
        assertTrue("EMA must prevent sudden jump (delta=${harshTurn.delta})", abs(harshTurn.delta) <= 0.15f)
        assertTrue("Harsh turn should move difficulty smoothly downward", harshTurn.delta < 0f && harshTurn.newDifficulty > 0.80f)

        // B) OOC meta directive -> resistance/difficulty unchanged
        val oocTurn = DynamicDifficultyCalibrator.calibrateTurn(
            bot = secureBot,
            sentiment = SentimentAnalysisResult(
                tone = "tense",
                valence = -0.9f,
                intensity = 0.9f,
                isOocMeta = true,
                resistanceDelta = 0.15f
            )
        )
        assertEquals(0f, oocTurn.delta, 0.0001f)
        assertEquals(1.0f, oocTurn.newDifficulty, 0.0001f)

        // C) Feature flag disabled -> returns neutral 1.0f and empty prompt directive
        AppFeatureFlags.setDynamicDifficulty(false)
        val offTurn = DynamicDifficultyCalibrator.calibrateTurn(
            bot = createTestBot(id = "bot_off", baseDifficulty = 1.45),
            sentiment = SentimentAnalysisResult(tone = "warm", valence = 0.9f, resistanceDelta = -0.10f)
        )
        assertEquals(1.0f, offTurn.newDifficulty, 0.0001f)
        assertEquals(0f, offTurn.delta, 0.0001f)
        assertTrue(offTurn.promptDirective.isEmpty())
        assertEquals(1.0f, DynamicDifficultyCalibrator.resolveEffectiveMultiplier(createTestBot(baseDifficulty = 1.6)), 0.0001f)

        // D) dismissive_avoidant baseline anchor: never collapses below personality resistance corridor even after 15 ultra-warm turns
        AppFeatureFlags.setDynamicDifficulty(true)
        val avoidantCorridor = DynamicDifficultyCalibrator.resolvePersonalityCorridor("dismissive_avoidant", 75)
        var avoidantBot = createTestBot(
            id = "bot_avoidant",
            baseDifficulty = 0.80,
            attachmentStyle = "dismissive_avoidant",
            regulationCapacity = 75
        )
        repeat(15) {
            val res = DynamicDifficultyCalibrator.calibrateTurn(
                bot = avoidantBot,
                sentiment = SentimentAnalysisResult(
                    tone = "warm",
                    valence = 1.0f,
                    intensity = 1.0f,
                    isOocMeta = false,
                    resistanceDelta = -0.15f
                ),
                attachmentStyleOverride = "dismissive_avoidant"
            )
            avoidantBot = avoidantBot.copy(baseAffectionDifficulty = res.newDifficulty.toDouble())
        }
        assertTrue(
            "dismissive_avoidant multiplier (${avoidantBot.baseAffectionDifficulty}) must never exceed corridor ceiling (${avoidantCorridor.maxMultiplier})",
            avoidantBot.baseAffectionDifficulty <= avoidantCorridor.maxMultiplier + 0.0001
        )
    }

    // ── 8. Sistem #19: Yıkıcı Migrasyon Yasağı & Kesintisiz v1->v48 Şema Bütünlüğü ──

    @Test
    fun test8_system19_noDestructiveMigrationGaplessV1ToV48ChainAndSchema48Valid() {
        val migrations = AppDatabase.ALL_MIGRATIONS
        assertEquals("Unbroken v1->v48 chain must have 47 contiguous migrations", 47, migrations.size)
        for (i in migrations.indices) {
            val expectedFrom = i + 1
            val expectedTo = i + 2
            assertEquals("Gap detected at index $i", expectedFrom, migrations[i].startVersion)
            assertEquals("Gap detected at index $i", expectedTo, migrations[i].endVersion)
        }

        // Verify 48.json schema file exists and is valid Room v48 JSON
        val schemaFile = resolveProjectFile("app/schemas/com.example.data.local.AppDatabase/48.json")
        assertTrue("Schema 48.json must exist at ${schemaFile.absolutePath}", schemaFile.exists())
        val schemaJson = JSONObject(schemaFile.readText())
        assertEquals(48, schemaJson.getJSONObject("database").getInt("version"))

        // Verify AppDatabase.kt source has ZERO fallbackToDestructiveMigration calls
        val appDbSource = resolveProjectFile("app/src/main/java/com/example/data/local/AppDatabase.kt").readText()
        assertFalse(
            "AppDatabase.kt must NOT contain fallbackToDestructiveMigration",
            appDbSource.contains("fallbackToDestructiveMigration")
        )
    }

    // ── 9. Sistem #19: Release Log Maskeleme & DTO JSON Serileştirme Güvenliği ──

    @Test
    fun test9_system19_releaseLogRedactionAndDtoSerializationRoundTrip() {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

        // 1. EmotionState round-trip
        val emo = EmotionState(
            primaryEmotions = com.example.data.local.PrimaryEmotions(trust = 58),
            relationshipAxes = com.example.data.local.RelationshipAxes(affectionScore = 64),
            dominantEmotion = "meraklı"
        )
        val emoRestored = EmotionState.fromJson(emo.toJson())
        assertEquals(emo.dominantEmotion, emoRestored.dominantEmotion)
        assertEquals(emo.relationshipAxes.affectionScore, emoRestored.relationshipAxes.affectionScore)
        assertEquals(emo.primaryEmotions.trust, emoRestored.primaryEmotions.trust)

        // 2. BaselineAnalysisResult round-trip
        val baseline = BaselineEmotionEngine.calculateDeterministicFallback(
            botId = "bot_rt",
            aiName = "Asroda",
            personality = "Soğuk ama sadık",
            scenario = "Karanlık kütüphane",
            userCharName = "Eren",
            userCharDesc = "Araştırmacı"
        )
        val baselineJson = baseline.toCanonicalJson()
        val baselineParsed = BaselineEmotionEngine.parseAnalysisJson(
            jsonStr = baselineJson,
            botId = "bot_rt",
            aiName = "Asroda",
            personality = "Soğuk ama sadık",
            scenario = "Karanlık kütüphane"
        )
        assertEquals(baseline.personalityProfile.attachmentStyle, baselineParsed.personalityProfile.attachmentStyle)

        // 3. BackupSnapshot Moshi round-trip
        val snapshot = BackupSnapshot(
            version = 2,
            exportedAt = 1_700_000_000_000L,
            bots = listOf(createTestBot()),
            messages = emptyList(),
            settings = UserSettingsEntity()
        )
        val snapAdapter = moshi.adapter(BackupSnapshot::class.java)
        val snapRestored = snapAdapter.fromJson(snapAdapter.toJson(snapshot))
        assertNotNull(snapRestored)
        assertEquals(2, snapRestored!!.version)
        assertEquals("Asroda", snapRestored.bots.first().aiName)

        // 4. Micro-AI Cast & Sentiment DTO round-trip
        val castAdapter = moshi.adapter(MicroAiCastResponse::class.java)
        val castDto = MicroAiCastResponse(
            cast = listOf(MicroAiCastMemberDto(name = "Kadir", aliases = listOf("saatçi"), role = "usta", age = 62, inScene = true)),
            rejected = listOf("Galata")
        )
        val castRestored = castAdapter.fromJson(castAdapter.toJson(castDto))
        assertEquals("Kadir", castRestored!!.cast.first().name)
        assertEquals(62, castRestored.cast.first().age)

        val sentAdapter = moshi.adapter(MicroAiSentimentDto::class.java)
        val sentDto = MicroAiSentimentDto(tone = "hurt", valence = -0.75f, intensity = 0.8f, subtext = "gizli kırgınlık", resistanceDelta = -0.08f)
        val sentRestored = sentAdapter.fromJson(sentAdapter.toJson(sentDto))
        assertEquals("gizli kırgınlık", sentRestored!!.subtext)

        // 5. Log redaction & Release HTTP logger safety
        val leakyLog = "Authorization: Bearer sk-or-v1-secret1234567890abcdef x-api-key: AIzaSyD1234567890123456789012345 gsk_secretGroqKey987654321"
        val scrubbed = SecureApiKeyVault.redactSecrets(leakyLog)
        assertFalse("Gemini key leaked!", scrubbed.contains("AIzaSyD"))
        assertFalse("OpenRouter key leaked!", scrubbed.contains("sk-or-v1-secret"))
        assertFalse("Groq key leaked!", scrubbed.contains("gsk_secretGroqKey"))

        val releaseLogger = SecureApiKeyVault.createSafeHttpLoggingInterceptor(isDebug = false)
        assertEquals(HttpLoggingInterceptor.Level.NONE, releaseLogger.level)
    }

    // ── 10. Sistem #19: CI Test Kapısı & R8 ProGuard Kural Doğrulaması ───────

    @Test
    fun test10_system19_ciWorkflowQualityGateAndR8RulesVerified() {
        val workflowText = resolveProjectFile(".github/workflows/android-release.yml").readText()
        val testGateIdx = workflowText.indexOf("testDebugUnitTest")
        val releaseBuildIdx = workflowText.indexOf("assembleRelease")
        assertTrue("android-release.yml must run testDebugUnitTest", testGateIdx >= 0)
        assertTrue("android-release.yml must run assembleRelease", releaseBuildIdx >= 0)
        assertTrue("testDebugUnitTest MUST execute BEFORE assembleRelease", testGateIdx < releaseBuildIdx)
        assertTrue("android-release.yml must upload JUnit XML results", workflowText.contains("test-results/testDebugUnitTest/*.xml"))

        val proguardText = resolveProjectFile("app/proguard-rules.pro").readText()
        assertTrue(proguardText.contains("BackupSnapshot"))
        assertTrue(proguardText.contains("EmotionState"))
        assertTrue(proguardText.contains("MicroAiCastMemberDto"))
        assertTrue(proguardText.contains("MicroAiSentimentDto"))
        assertTrue(proguardText.contains("KeystoreEncryptionManager"))
        assertTrue(proguardText.contains("android.util.Log"))
    }

    // ── 11. Regresyon: Uzun Parantez İçi Saf OOC + Çocuk/Aile Bağlamında Boş Yanıt Koruması ──

    @Test
    fun test11_pureOocMinorContextEmptyReplyRecovery() = runBlocking {
        val exactUserOocPrompt =
            "(bu senaryoda Aiden şuan 20 yaşındadır sezonun başıdır. Aiden 38 milyon euro ve şuanda 10 milyon takipçisi vardır. " +
                "medyada hem aşırı sevilir hemde baya fazla eleştirilir. zeynep Sude Oktay şarkıcı değildir köylü kızıdır rize köyünde ailesiyle yaşar. " +
                "Benim annemin en küçük kardeşi Brianna yani benim teyzem olur. ve Brianna teyzemin 2,5 yaşındaki kızı Adriana. " +
                "adriana 2,5 yaşındadır melek gibi bir kızdır Aiden'i çok sever bende Onu çok severim. Adriana'yı çok fazla şımartırım.)"

        // 1. Single outer parenthesis OOC must be recognized as pure OOC regardless of length
        val split = UserSentimentAnalyzer.splitIcAndOoc(exactUserOocPrompt)
        assertTrue("Long single-paren setup must be detected as pure OOC", split.isPureOoc)
        assertEquals("", split.icText)
        assertTrue(split.oocDirective.contains("Adriana"))

        // 2. Minor/toddler family context must be detected and suppress +18 / NSFW directives
        assertTrue(
            "2,5 yaşındaki kızı must activate minor/child family safety guard",
            SystemPromptBuilder.containsMinorOrChildContext(exactUserOocPrompt)
        )
        val bot = createTestBot(aiName = "Aiden", userCharName = "Kullanıcı").copy(isNsfw = true)
        val settings = UserSettingsEntity(enableNsfw = true, enableFlirty = true, enableDarkRp = true)
        val builtPrompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = settings,
                latestUserMessage = exactUserOocPrompt,
                isPureOocTurn = true,
                recentSceneText = exactUserOocPrompt
            )
        )
        assertFalse("NSFW +18 directive MUST be suppressed when toddler/child family context exists", builtPrompt.contains("+18 RP"))
        assertFalse("Physical intimacy wording MUST be suppressed", builtPrompt.contains("fiziksel yakınlığı"))
        assertTrue("Pure-OOC mandate must instruct model not to return empty/tool-only reply", builtPrompt.contains("SAF OOC SENARYO KURULUMU ZORUNLULUĞU"))

        // 3. OpenAI null content must NOT parse as literal "null"
        val parsedNull = com.example.data.api.MemoryToolRegistry.parseOpenAiToolCalls(
            JSONObject("""{"content":null,"tool_calls":[]}""")
        )
        assertEquals("", parsedNull.first)

        // 4. ProviderFallbackManager skips native tools on pure-OOC turn & recovers automatically from SAFETY/empty block
        var attemptCount = 0
        var receivedToolsOnFirstCall = -1
        var retryHadSafeMarker = false
        val mgr = com.example.data.managers.ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = {},
            customExecutor = { _, _, sysPrompt, _, tools ->
                attemptCount++
                if (attemptCount == 1) {
                    receivedToolsOnFirstCall = tools.size
                    throw IllegalStateException("Gemini API yanıt döndürmedi (candidates boş veya filtre engelledi).")
                } else {
                    retryHadSafeMarker = sysPrompt.contains("GÜVENLİ YANIT & OOC KURULUM KURTARMA") && !sysPrompt.contains("+18 RP")
                    com.example.data.api.ModelResponseResult(
                        text = "(Kurulum anlaşıldı.) *Sezon başının serin İstanbul akşamında Aiden telefonundaki eleştiri yorumlarını kapatıp küçük Adriana'ya gülümsedi.* \"Hazır mıyız bakalım?\"",
                        usedProvider = "gemini"
                    )
                }
            }
        )
        val userMsg = MessageEntity(
            id = "ooc_msg_1",
            botId = bot.id,
            role = "user",
            text = exactUserOocPrompt,
            timestamp = System.currentTimeMillis()
        )
        val res = mgr.executeWithFallback(
            model = "gemini-2.5-flash",
            settings = settings.copy(selectedProvider = "gemini", customApiKey = "AIzaSyTestKey12345678901234567890123"),
            systemPrompt = builtPrompt,
            messages = listOf(userMsg),
            buildConfigGeminiKey = "",
            tools = com.example.data.api.MemoryToolRegistry.CENTRAL_TOOLS
        )
        assertEquals("Pure-OOC setup must skip native tools on first narrative call", 0, receivedToolsOnFirstCall)
        assertEquals("Should recover on same provider via safe retry", 2, attemptCount)
        assertTrue("Retry prompt must apply safe recovery sanitizer", retryHadSafeMarker)
        assertTrue("Recovered reply must contain visible Turkish narrative", res.text.contains("Adriana"))
    }

    private fun resolveProjectFile(relativeFromRepoRoot: String): File {
        val direct = File(relativeFromRepoRoot)
        if (direct.exists()) return direct
        val parent = File("..", relativeFromRepoRoot)
        if (parent.exists()) return parent
        return direct
    }
}
