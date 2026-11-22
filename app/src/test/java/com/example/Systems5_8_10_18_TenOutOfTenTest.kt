package com.example

import android.content.Context
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.EntityRelationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.MilestoneEntity
import com.example.data.local.PendingReappraisalEntity
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.EmochiRepository
import com.example.data.repository.SystemPromptBuilder
import com.example.util.AppFeatureFlags
import com.example.util.BookAudioController
import com.example.util.BookChoiceParser
import com.example.util.ChatTtsManager
import kotlinx.coroutines.runBlocking
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
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Systems5_8_10_18_TenOutOfTenTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: EmochiRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        BookAudioController.resetForTesting()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = EmochiRepository(db, context)
    }

    @After
    fun tearDown() {
        BookAudioController.resetForTesting()
        db.close()
    }

    private fun makeBot(
        id: String = "bot_s581018",
        storyDayCounter: Long = 5L,
        storyCalendarDate: String = "18 Kasım"
    ): BotEntity = BotEntity(
        id = id,
        mode = "personal",
        aiName = "Aiden",
        aiPersonality = "Soğukkanlı, ketum ve sadık bir dedektif.",
        scenario = "Yağmurlu gece katedral kütüphanesinde kayıp mührü arıyoruz.",
        universeName = "",
        keyCharactersJson = "[]",
        userCharName = "Selin",
        userCharDesc = "Araştırmacı gazeteci.",
        openingMessage = "Yağmur dinmeden bu dosyayı çözmeliyiz.",
        writingStyle = "rp",
        intensity = "normal",
        storyDayCounter = storyDayCounter,
        storyCalendarDate = storyCalendarDate
    )

    // =========================================================================
    // TEST 1 (Sistem #8): Unified [[STATE_JSON]] & Conditional Token Injection
    // =========================================================================
    @Test
    fun test1_system8_unifiedStateJsonAndConditionalPromptInjection() {
        val bot = makeBot(id = "bot_sys8_prompt")
        val settings = UserSettingsEntity()

        // Without active cast or pending reappraisals: cast_emo & reapp MUST be omitted to save tokens
        val minimalPrompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = settings,
                castMembers = emptyList(),
                pendingReappraisals = emptyList()
            )
        )
        assertTrue("Unified STATE_JSON contract must exist", minimalPrompt.contains("[[STATE_JSON"))
        assertTrue("Unified STATE_JSON must include skip", minimalPrompt.contains("\"skip\""))
        assertTrue("Unified STATE_JSON must include mile", minimalPrompt.contains("\"mile\""))
        assertFalse("cast_emo must be omitted when there are no active cast members", minimalPrompt.contains("\"cast_emo\""))
        assertFalse("reapp must be omitted when there are no pending reappraisals", minimalPrompt.contains("\"reapp\""))
        assertFalse("[BEKLEYEN DUYGUSAL OLGUNLAŞMA] must be omitted when empty", minimalPrompt.contains("[BEKLEYEN DUYGUSAL OLGUNLAŞMA]"))

        // With active cast + 3 pending reappraisals: cast_emo & reapp MUST appear, reappraisals capped at 2
        val cast = listOf(
            CastMemberEntity(botId = bot.id, name = "Natasha", description = "Eski istihbarat subayı", role = "Müttefik"),
            CastMemberEntity(botId = bot.id, name = "Elena", description = "Arşiv görevlisi", role = "Arkadaş")
        )
        val reappraisals = listOf(
            PendingReappraisalEntity(
                id = 11L,
                botId = bot.id,
                originalEventDescription = "Katedraldeki kırıcı söz",
                originalEmotionState = "{}",
                triggeredAtMessageIndex = 3,
                triggeredAtTimestamp = 1_000L,
                reappraisalEligibleAtMessageIndex = 10,
                reappraisalEligibleAtTimestamp = 2_000L,
                resolved = false,
                expired = false
            ),
            PendingReappraisalEntity(
                id = 12L,
                botId = bot.id,
                originalEventDescription = "Gizlenen mektup gerilimi",
                originalEmotionState = "{}",
                triggeredAtMessageIndex = 5,
                triggeredAtTimestamp = 3_000L,
                reappraisalEligibleAtMessageIndex = 12,
                reappraisalEligibleAtTimestamp = 4_000L,
                resolved = false,
                expired = false
            ),
            PendingReappraisalEntity(
                id = 13L,
                botId = bot.id,
                originalEventDescription = "Üçüncü taşan olay",
                originalEmotionState = "{}",
                triggeredAtMessageIndex = 6,
                triggeredAtTimestamp = 5_000L,
                reappraisalEligibleAtMessageIndex = 15,
                reappraisalEligibleAtTimestamp = 6_000L,
                resolved = false,
                expired = false
            )
        )

        val fullPrompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = settings,
                castMembers = cast,
                pendingReappraisals = reappraisals,
                recentSceneText = "Natasha ve Elena kütüphane kapısında bekliyor."
            )
        )
        assertTrue("cast_emo must be included when cast members are active", fullPrompt.contains("\"cast_emo\""))
        assertTrue("reapp must be included when pending reappraisals exist", fullPrompt.contains("\"reapp\""))
        assertTrue("Pending reappraisal block must be injected", fullPrompt.contains("[BEKLEYEN DUYGUSAL OLGUNLAŞMA]"))
        assertTrue("First reappraisal must appear", fullPrompt.contains("Katedraldeki kırıcı söz"))
        assertTrue("Second reappraisal must appear", fullPrompt.contains("Gizlenen mektup gerilimi"))
        assertFalse("Third reappraisal must be capped at max 2 items", fullPrompt.contains("Üçüncü taşan olay"))
    }

    // =========================================================================
    // TEST 2 (Sistem #8): Zero-Leakage Tag & Truncated STATE_JSON Cleaning
    // =========================================================================
    @Test
    fun test2_system8_zeroLeakageAndPartialTagCleaning() {
        val rawFull = """
            <think>Önce duygusal tonu ayarlayayım, güven arttı.</think>
            *Yağmur damlaları vitray camlarda süzülürken Aiden yavaşça sana döndü.*
            "Bunu bana anlattığın için teşekkür ederim, Selin."
            [[STATE_JSON {"dom":"güven","int":72,"aff":4,"trs":5,"mile":{"k":"first_secret","t":"İlk Sır","d":"Geçmişini açtı"}}]]
        """.trimIndent()

        val cleanedFull = EmochiRepository.cleanEmotionTags(rawFull)
        assertTrue("Narrative prose must be preserved", cleanedFull.contains("Yağmur damlaları"))
        assertTrue("Dialogue must be preserved", cleanedFull.contains("teşekkür ederim, Selin"))
        assertFalse("No STATE_JSON leakage", cleanedFull.contains("STATE_JSON"))
        assertFalse("No <think> leakage", cleanedFull.contains("<think>") || cleanedFull.contains("Önce duygusal"))
        assertFalse("No JSON braces leakage", cleanedFull.contains("\"dom\""))

        // Mid-cut / truncated STATE_JSON (missing closing ]]) + unclosed <think>
        val rawTruncated = """
            *Aiden kaşlarını çatarak masadaki mührü inceledi.*
            "Bu iz burada bırakılmamalıydı."
            [[STATE_JSON {"dom":"tedirgin","int":80,"mile":{"k":"broken_seal","t":"Yarım kalan
        """.trimIndent()

        val cleanedTruncated = EmochiRepository.cleanEmotionTags(rawTruncated)
        assertTrue(cleanedTruncated.contains("masadaki mührü inceledi"))
        assertTrue(cleanedTruncated.contains("Bu iz burada bırakılmamalıydı."))
        assertFalse("Truncated [[STATE_JSON must leave 0 chars", cleanedTruncated.contains("STATE_JSON"))
        assertFalse("Truncated [[ must leave 0 chars", cleanedTruncated.contains("[["))
        assertFalse("Truncated JSON payload must leave 0 chars", cleanedTruncated.contains("broken_seal"))
    }

    // =========================================================================
    // TEST 3 (Sistem #10): Full Transactional Rewind Completeness
    // =========================================================================
    @Test
    fun test3_system10_rewindToMessageCompleteness() = runBlocking {
        val bot = makeBot(id = "bot_sys10_rewind")
        db.botDao().insertOrUpdate(bot)

        val baseTs = 1_750_000_000_000L
        val messages = (1..10).map { i ->
            MessageEntity(
                id = "m_$i",
                botId = bot.id,
                role = if (i % 2 == 1) "user" else "assistant",
                text = "Mesaj #$i içerik",
                timestamp = baseTs + i * 60_000L
            )
        }
        messages.forEach { db.messageDao().insertMessage(it) }

        // Early reappraisal triggered at msg 3, resolved later at msg 7 (must reopen on rewind to msg 4!)
        val earlyReappId = db.pendingReappraisalDao().insert(
            PendingReappraisalEntity(
                botId = bot.id,
                originalEventDescription = "Erken kırgınlık",
                originalEmotionState = "{}",
                triggeredAtMessageIndex = 3,
                triggeredAtTimestamp = baseTs + 3 * 60_000L,
                reappraisalEligibleAtMessageIndex = 6,
                reappraisalEligibleAtTimestamp = baseTs + 6 * 60_000L,
                resolved = true,
                expired = false
            )
        )
        // Late reappraisal triggered at msg 8 (must be deleted on rewind to msg 4!)
        db.pendingReappraisalDao().insert(
            PendingReappraisalEntity(
                botId = bot.id,
                originalEventDescription = "Geç kırgınlık",
                originalEmotionState = "{}",
                triggeredAtMessageIndex = 8,
                triggeredAtTimestamp = baseTs + 8 * 60_000L,
                reappraisalEligibleAtMessageIndex = 14,
                reappraisalEligibleAtTimestamp = baseTs + 14 * 60_000L,
                resolved = false,
                expired = false
            )
        )

        // Milestones: one early (t2), one late (t8)
        db.milestoneDao().insertMilestone(
            MilestoneEntity(
                id = "ms_early",
                botId = bot.id,
                type = "trust_25",
                title = "İlk Güven · Gün 2",
                emoji = "🤝",
                affectionAtTime = 35,
                trustAtTime = 28,
                reachedAt = baseTs + 2 * 60_000L
            )
        )
        db.milestoneDao().insertMilestone(
            MilestoneEntity(
                id = "ms_late",
                botId = bot.id,
                type = "story_secret_oath",
                title = "Gizli Yemin · Gün 5",
                emoji = "✦",
                affectionAtTime = 65,
                trustAtTime = 60,
                reachedAt = baseTs + 8 * 60_000L
            )
        )

        // Entity relations: one at msg 3, one at msg 9
        db.entityRelationDao().insertRelation(
            EntityRelationEntity(
                botId = bot.id,
                sourceEntityId = 1L,
                sourceEntityName = "Aiden",
                relationType = "müttefik",
                targetEntityId = 2L,
                targetEntityName = "Selin",
                confidence = "high",
                createdAtMessageIndex = 3
            )
        )
        db.entityRelationDao().insertRelation(
            EntityRelationEntity(
                botId = bot.id,
                sourceEntityId = 1L,
                sourceEntityName = "Aiden",
                relationType = "rakip",
                targetEntityId = 3L,
                targetEntityName = "Viktor",
                confidence = "high",
                createdAtMessageIndex = 9
            )
        )

        val rewound = repository.rewindToMessage(bot.id, "m_4")
        assertTrue("rewindToMessage must return true", rewound)

        val remainingMessages = db.messageDao().getMessagesForBotList(bot.id)
        assertEquals("Messages 5..10 must be deleted", 4, remainingMessages.size)
        assertEquals("m_4", remainingMessages.last().id)

        val remainingMilestones = db.milestoneDao().getMilestonesForBot(bot.id)
        assertEquals("Only early milestone should remain", 1, remainingMilestones.size)
        assertEquals("ms_early", remainingMilestones.first().id)

        val remainingRelations = db.entityRelationDao().getRelationsForBot(bot.id)
        assertEquals("Post-rewind entity relation must be deleted", 1, remainingRelations.size)
        assertEquals("Selin", remainingRelations.first().targetEntityName)

        val remainingReapps = db.pendingReappraisalDao().getAllForBot(bot.id)
        assertEquals("Late reappraisal must be deleted, early preserved", 1, remainingReapps.size)
        val reopened = remainingReapps.first()
        assertEquals(earlyReappId, reopened.id)
        assertFalse("Early reappraisal resolved after rewind point MUST reopen", reopened.resolved)
        assertFalse("Reopened reappraisal must not be expired", reopened.expired)
    }

    // =========================================================================
    // TEST 4 (Sistem #10): Deep Clone Isolation & No Hardcoded Date Leak
    // =========================================================================
    @Test
    fun test4_system10_cloneBotIsolation() = runBlocking {
        val source = makeBot(
            id = "bot_sys10_src",
            storyDayCounter = 12L,
            storyCalendarDate = "18 Kasım"
        )
        db.botDao().insertOrUpdate(source)

        db.milestoneDao().insertMilestone(
            MilestoneEntity(
                id = "ms_src_1",
                botId = source.id,
                type = "trust_50",
                title = "Güven Eşiği · Gün 10",
                emoji = "🛡️",
                affectionAtTime = 55,
                trustAtTime = 52
            )
        )
        db.pendingReappraisalDao().insert(
            PendingReappraisalEntity(
                botId = source.id,
                originalEventDescription = "Kaynak tartışma",
                originalEmotionState = "{}",
                triggeredAtMessageIndex = 12,
                triggeredAtTimestamp = 1_750_000_000_000L,
                reappraisalEligibleAtMessageIndex = 20,
                reappraisalEligibleAtTimestamp = 1_750_001_000_000L,
                resolved = false,
                expired = false
            )
        )
        db.sensitiveTriggerDao().insertOrUpdate(
            SensitiveTriggerEntity(
                botId = source.id,
                triggerTopic = "İhanet",
                sourceDescription = "Geçmiş yara",
                intensityMultiplier = 2.1,
                recentTriggerCount = 3
            )
        )

        val cloned = repository.cloneBot(source.id)
        assertNotNull("cloneBot must return a new BotEntity", cloned)
        val cloneId = cloned!!.id

        assertEquals("Cloned bot must start with 0 milestones", 0, db.milestoneDao().getMilestonesForBot(cloneId).size)
        assertEquals("Cloned bot must start with 0 pending reappraisals", 0, db.pendingReappraisalDao().getAllForBot(cloneId).size)

        val clonedTriggers = db.sensitiveTriggerDao().getTriggersForBot(cloneId)
        assertEquals("Static sensitive triggers must be copied to clone", 1, clonedTriggers.size)
        assertEquals("Cloned trigger recentTriggerCount must reset to 0", 0, clonedTriggers.first().recentTriggerCount)

        assertFalse(
            "Cloned storyCalendarDate must never leak hardcoded '10 Ekim 2024'",
            cloned.storyCalendarDate.contains("10 Ekim 2024")
        )
    }

    // =========================================================================
    // TEST 5 (Sistem #5): BookChoiceParser Resilience, Badges & Lock Parsing
    // =========================================================================
    @Test
    fun test5_system5_bookChoiceParserResilienceAndBadges() {
        val aiText = """
            Yağmur katedralin taş avlusunda şiddetleniyor.
            🔀 NE YAPACAKSIN?
            1. [Cesur] [İlişki: Natasha +1] Kapıyı kırıp içeri dal
            2. [Gizli] [Riskli] Kırmızı mührü sessizce incele
            3. [Kilitli: Güven >= 50] Yasaklı arşivi aç
        """.trimIndent()

        val choices = BookChoiceParser.parseAiBookChoices(aiText)
        assertEquals(3, choices.size)

        val c1 = choices[0]
        assertEquals("Kapıyı kırıp içeri dal", c1.displayText)
        assertEquals("Natasha +1", c1.relationshipHint)
        assertFalse(c1.isLocked)
        assertTrue(c1.badges.any { it.kind == BookChoiceParser.ChoiceBadgeKind.TONE && it.label == "Cesur" })
        assertTrue(c1.badges.any { it.kind == BookChoiceParser.ChoiceBadgeKind.RELATIONSHIP })

        val c2 = choices[1]
        assertEquals("Kırmızı mührü sessizce incele", c2.displayText)
        assertTrue(c2.badges.any { it.kind == BookChoiceParser.ChoiceBadgeKind.SECRET })
        assertTrue(c2.badges.any { it.kind == BookChoiceParser.ChoiceBadgeKind.RISK })

        val c3 = choices[2]
        assertEquals("Yasaklı arşivi aç", c3.displayText)
        assertTrue("Choice 3 must be locked", c3.isLocked)
        assertEquals("Güven >= 50", c3.lockReason)
        assertTrue(c3.badges.any { it.kind == BookChoiceParser.ChoiceBadgeKind.LOCKED })

        // Malformed & unclosed bracket resilience: MUST NEVER throw
        val malformedSamples = listOf("[bozuk seçenek", "", "[[[", "1. [Kilitli:]", "]]]][[[", "   ")
        malformedSamples.forEach { bad ->
            val parsedSingle = BookChoiceParser.parseChoiceDisplay(bad)
            assertNotNull(parsedSingle)
            val parsedList = BookChoiceParser.parseAiBookChoices(bad)
            assertNotNull(parsedList)
        }
    }

    // =========================================================================
    // TEST 6 (Sistem #5): Book Music Lifecycle & TTS Mutual Exclusion
    // =========================================================================
    @Test
    fun test6_system5_bookMusicLifecycleAndTtsMutualExclusion() {
        BookAudioController.simulatePlaybackStateForTest(playing = true, muted = false)
        assertTrue(BookAudioController.isPlayingFlow.value)
        assertFalse(BookAudioController.isMutedFlow.value)

        // Background pause must pause playback WITHOUT toggling manual mute
        BookAudioController.pauseForBackground(0L)
        assertFalse("Music must pause on background", BookAudioController.isPlayingFlow.value)
        assertFalse("Manual mute flag must stay false after background pause", BookAudioController.isMutedFlow.value)

        // Foreground resume must restore playback cleanly
        BookAudioController.resumeFromBackground(0L)
        assertTrue("Music must resume on foreground return", BookAudioController.isPlayingFlow.value)

        // Manual mute MUST be respected across background/foreground cycle
        BookAudioController.setManualMuteForTest(true)
        assertFalse(BookAudioController.isPlayingFlow.value)
        BookAudioController.pauseForBackground(0L)
        BookAudioController.resumeFromBackground(0L)
        assertFalse("Manually muted music MUST stay silent on resume", BookAudioController.isPlayingFlow.value)
        assertTrue(BookAudioController.isMutedFlow.value)

        // TTS <-> Book Music mutual exclusion (both directions)
        AppFeatureFlags.setTtsEngineMode(AppFeatureFlags.TTS_ENGINE_DEVICE)
        val tts = ChatTtsManager(context)
        try {
            tts.deviceSpeakOverride = { _, _ -> true }
            tts.simulateInitCompleteForTest()

            // Start book music, then start TTS -> book music MUST pause
            BookAudioController.simulatePlaybackStateForTest(playing = true, muted = false)
            assertTrue(BookAudioController.isPlayingFlow.value)

            val started = tts.speakMessage("msg_mutex_1", "\"Şimdi beni dikkatle dinle.\"")
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertTrue("TTS should start speaking", started)
            assertFalse("Starting Chat TTS MUST pause BookAudioController", BookAudioController.isPlayingFlow.value)
            assertTrue("TTS should be active", tts.isSpeaking.value)

            // Now restart book music -> TTS MUST stop cleanly via onPlaybackStarted hook
            BookAudioController.simulatePlaybackStateForTest(playing = true, muted = false)
            assertFalse("Starting book music MUST stop active Chat TTS", tts.isSpeaking.value)
            assertNull(tts.speakingMessageId.value)
        } finally {
            tts.shutdown()
            BookAudioController.resetForTesting()
        }
    }

    // =========================================================================
    // TEST 7 (Sistem #18): Hybrid Milestones (Numeric + Narrative) & Dedup
    // =========================================================================
    @Test
    fun test7_system18_hybridMilestonesAndDeduplication() = runBlocking {
        val bot = makeBot(id = "bot_sys18_hybrid", storyDayCounter = 7L, storyCalendarDate = "22 Kasım")
        db.botDao().insertOrUpdate(bot)

        // Channel 1: Numeric thresholds crossed (trust 50 -> trust_25 + trust_50)
        val unlockedNumeric = repository.checkAndSaveMilestone(bot.id, affection = 20, trust = 50)
        assertNotNull("Crossing trust 50 should return highest newly unlocked milestone", unlockedNumeric)
        assertEquals("trust_50", unlockedNumeric!!.type)
        assertTrue("Numeric milestone must carry story day stamp", unlockedNumeric.storyStamp.contains("Gün 7"))
        assertEquals(2, db.milestoneDao().getMilestonesForBot(bot.id).size)

        // Oscillation test: trust drops to 45 then rises back to 52 -> MUST NOT re-award trust_50
        assertNull(repository.checkAndSaveMilestone(bot.id, affection = 20, trust = 45))
        assertNull("Oscillating across trust 50 must NEVER duplicate milestone", repository.checkAndSaveMilestone(bot.id, affection = 20, trust = 52))
        assertEquals(1, db.milestoneDao().getMilestonesForBot(bot.id).count { it.type == "trust_50" })

        // Channel 2: AI Narrative milestone + case/punctuation deduplication
        val narrative = repository.recordNarrativeMilestone(
            botId = bot.id,
            rawKey = "first_secret",
            rawTitle = "İlk Sır",
            rawDescription = "Aiden geçmişindeki yangını ilk kez paylaştı.",
            affection = 42,
            trust = 54
        )
        assertNotNull("Narrative milestone must save", narrative)
        assertEquals("story_first_secret", narrative!!.type)
        assertTrue(narrative.isNarrative)
        assertEquals("İlk Sır", narrative.displayTitle)
        assertEquals("Aiden geçmişindeki yangını ilk kez paylaştı.", narrative.narrativeDescription)
        assertTrue(narrative.storyStamp.contains("Gün 7"))

        // Duplicate normalized key ("First Secret" / "story_first_secret") MUST return null
        val dupNarrative = repository.recordNarrativeMilestone(
            botId = bot.id,
            rawKey = "First Secret",
            rawTitle = "İlk Sır Tekrarı",
            rawDescription = "Tekrar denemesi"
        )
        assertNull("Normalized duplicate narrative milestone MUST be rejected", dupNarrative)
        assertEquals(1, db.milestoneDao().getMilestonesForBot(bot.id).count { it.type == "story_first_secret" })
    }

    // =========================================================================
    // TEST 8 (Sistem #18): Milestone Prompt Feedback (Top 3) & Rewind Cleanup
    // =========================================================================
    @Test
    fun test8_system18_milestonePromptFeedbackAndRewind() = runBlocking {
        val bot = makeBot(id = "bot_sys18_prompt_rewind")
        db.botDao().insertOrUpdate(bot)

        val baseTs = 1_760_000_000_000L
        (1..4).forEach { i ->
            db.messageDao().insertMessage(
                MessageEntity(
                    id = "mr_$i",
                    botId = bot.id,
                    role = if (i % 2 == 1) "user" else "assistant",
                    text = "Sahne mesajı $i",
                    timestamp = baseTs + i * 60_000L
                )
            )
        }

        val msList = listOf(
            MilestoneEntity("ms1", bot.id, "trust_25", "İlk Güven · Gün 1", "🤝", 20, 26, reachedAt = baseTs + 60_000L),
            MilestoneEntity("ms2", bot.id, "affection_30", "İlk Kıvılcım · Gün 2", "💫", 32, 30, reachedAt = baseTs + 2 * 60_000L),
            MilestoneEntity("ms3", bot.id, "story_shared_umbrella", "Yağmur Altında Sığınak · Gün 3", "✦", 48, 45, reachedAt = baseTs + 3 * 60_000L),
            MilestoneEntity("ms4", bot.id, "story_cathedral_oath", "Katedral Yemini · Gün 4", "✦", 62, 58, reachedAt = baseTs + 4 * 60_000L)
        )
        msList.forEach { db.milestoneDao().insertMilestone(it) }

        val top3 = repository.getRecentMilestonesForBot(bot.id, limit = 3)
        assertEquals("Top-3 milestone query must cap at 3", 3, top3.size)

        val promptBeforeRewind = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = UserSettingsEntity(),
                recentMilestones = top3
            )
        )
        assertTrue(promptBeforeRewind.contains("[İLİŞKİ DÖNÜM NOKTALARI]:"))
        assertTrue(promptBeforeRewind.contains("Katedral Yemini"))
        assertTrue(promptBeforeRewind.contains("Yağmur Altında Sığınak"))
        assertTrue(promptBeforeRewind.contains("İlk Kıvılcım"))
        assertFalse("4th oldest milestone must not appear in top-3 prompt line", promptBeforeRewind.contains("İlk Güven"))

        // Rewind to message 2 (t2): ms3 (t3) & ms4 (t4) MUST be wiped from DB and prompt
        assertTrue(repository.rewindToMessage(bot.id, "mr_2"))
        val afterRewind = repository.getRecentMilestonesForBot(bot.id, limit = 3)
        assertEquals(2, afterRewind.size)
        assertFalse(afterRewind.any { it.id == "ms3" || it.id == "ms4" })

        val promptAfterRewind = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = UserSettingsEntity(),
                recentMilestones = afterRewind
            )
        )
        assertFalse("Rewound milestone ms4 must disappear from prompt", promptAfterRewind.contains("Katedral Yemini"))
        assertFalse("Rewound milestone ms3 must disappear from prompt", promptAfterRewind.contains("Yağmur Altında Sığınak"))
        assertTrue("Preserved milestone ms2 must remain in prompt", promptAfterRewind.contains("İlk Kıvılcım"))
    }
}
