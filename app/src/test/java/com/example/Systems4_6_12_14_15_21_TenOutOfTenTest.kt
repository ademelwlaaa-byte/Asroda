package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.EmotionState
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PrimaryEmotions
import com.example.data.local.RelationshipAxes
import com.example.data.local.RoutineEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.managers.WorldStateManager
import com.example.data.repository.EmochiRepository
import com.example.data.repository.EmotionEngine
import com.example.data.repository.LivingMemoryCodec
import com.example.data.repository.SystemPromptBuilder
import com.example.data.repository.TimePerceptionEngine
import com.example.util.AppFeatureFlags
import com.example.work.BackgroundMemoryMergeWorker
import com.example.work.ProactiveMessageWorker
import com.example.work.RoutineNotificationWorker
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
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Systems4_6_12_14_15_21_TenOutOfTenTest {

    private lateinit var ctx: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: EmochiRepository
    private lateinit var worldStateManager: WorldStateManager

    @Before
    fun setUp() {
        ctx = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(ctx)
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = EmochiRepository(db, ctx)
        worldStateManager = WorldStateManager(
            botDao = db.botDao(),
            routineDao = db.routineDao(),
            entityRelationDao = db.entityRelationDao(),
            entityRegistryDao = db.entityRegistryDao()
        )
        AppFeatureFlags.setRoutineNotif(true)
    }

    @After
    fun tearDown() {
        repo.close()
        db.close()
    }

    private fun resolveAppResDir(): File {
        val candidates = listOf(
            File("src/main/res"),
            File("app/src/main/res")
        )
        return candidates.firstOrNull { it.exists() && it.isDirectory }
            ?: error("Could not locate app/src/main/res from working dir: ${File(".").absolutePath}")
    }

    private fun resolveProjectRootDir(): File {
        val resDir = resolveAppResDir().canonicalFile
        // .../app/src/main/res -> .../
        return resDir.parentFile.parentFile.parentFile.parentFile
    }

    private fun hasValidWebpHeader(file: File): Boolean {
        if (!file.exists() || file.length() < 12L) return false
        val bytes = file.readBytes()
        val riff = bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte()
        val webp = bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() &&
            bytes[11] == 'P'.code.toByte()
        return riff && webp
    }

    private fun containsUtf8ReplacementCorruption(file: File): Boolean {
        val bytes = file.readBytes()
        for (i in 0 until bytes.size - 2) {
            if (bytes[i] == 0xEF.toByte() &&
                bytes[i + 1] == 0xBF.toByte() &&
                bytes[i + 2] == 0xBD.toByte()
            ) {
                return true
            }
        }
        return false
    }

    private fun buildTestBot(
        id: String,
        aiName: String = "Asroda",
        hasExplicitStoryDate: Boolean = false,
        storyCalendarDate: String = "",
        storyDayCounter: Long = 1L,
        regulationCapacity: Int = 55,
        emotionState: EmotionState = EmotionState()
    ): BotEntity = BotEntity(
        id = id,
        mode = "personal",
        aiName = aiName,
        aiPersonality = "Sıcakkanlı ama dikkatli bir karakter.",
        scenario = "İstanbul'da yağmurlu bir gün.",
        universeName = "",
        keyCharactersJson = "[]",
        userCharName = "Eren",
        userCharDesc = "Yakın arkadaş",
        openingMessage = "Merhaba, geldin mi?",
        writingStyle = "novel",
        intensity = "normal",
        emotionState = emotionState.toJson(),
        emotionalRegulationCapacity = regulationCapacity,
        hasExplicitStoryDate = hasExplicitStoryDate,
        storyCalendarDate = storyCalendarDate,
        storyDayCounter = storyDayCounter
    )

    // ── 1. Sistem #4: Logo Bütünlük, Boyut & WebP Koruma Testi ──────────────

    @Test
    fun test1_system4_logoIntegritySizeAndWebpPreservation() {
        val resDir = resolveAppResDir()
        val rootDir = resolveProjectRootDir()

        val densities = listOf("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")
        for (density in densities) {
            val launcher = File(resDir, "mipmap-$density/ic_launcher.webp")
            val launcherRound = File(resDir, "mipmap-$density/ic_launcher_round.webp")
            assertTrue("Missing $launcher", launcher.exists())
            assertTrue("Missing $launcherRound", launcherRound.exists())
            assertTrue("Invalid WebP header in $launcher", hasValidWebpHeader(launcher))
            assertTrue("Invalid WebP header in $launcherRound", hasValidWebpHeader(launcherRound))
            assertFalse("Corrupted UTF-8 bytes in $launcher", containsUtf8ReplacementCorruption(launcher))
            assertFalse("Corrupted UTF-8 bytes in $launcherRound", containsUtf8ReplacementCorruption(launcherRound))

            // Ensure old corrupted mipmap-*-v4 directories are deleted
            assertFalse("Legacy mipmap-$density-v4 should be removed", File(resDir, "mipmap-$density-v4").exists())
        }

        val fgWebp = File(resDir, "drawable-nodpi/ic_launcher_foreground_asset.webp")
        val appLogoWebp = File(resDir, "drawable-nodpi/ic_app_logo.webp")
        val vaiLogoWebp = File(resDir, "drawable-nodpi/ic_vai_logo.webp")

        for (logo in listOf(fgWebp, appLogoWebp, vaiLogoWebp)) {
            assertTrue("Missing $logo", logo.exists())
            assertTrue("Invalid WebP header in $logo", hasValidWebpHeader(logo))
            assertFalse("Corrupted UTF-8 bytes in $logo", containsUtf8ReplacementCorruption(logo))
        }

        assertTrue("ic_app_logo.webp must be < 90 KB (was ${appLogoWebp.length()})", appLogoWebp.length() < 90 * 1024L)
        assertTrue("ic_vai_logo.webp must be < 100 KB (was ${vaiLogoWebp.length()})", vaiLogoWebp.length() < 100 * 1024L)
        assertTrue(
            "ic_launcher_foreground_asset.webp must be < 70 KB (was ${fgWebp.length()})",
            fgWebp.length() < 70 * 1024L
        )

        // Old corrupted logo files & root /res/ must be cleaned up
        assertFalse("Old drawable/ic_launcher_foreground.xml must be deleted", File(resDir, "drawable/ic_launcher_foreground.xml").exists())
        assertFalse("Old ic_app_logo.png must be deleted", File(resDir, "drawable-nodpi/ic_app_logo.png").exists())
        assertFalse("Old ic_vai_logo.png must be deleted", File(resDir, "drawable-nodpi/ic_vai_logo.png").exists())
        assertFalse("Duplicate root res/ folder must be deleted", File(rootDir, "res").exists())

        // Monochrome + white notification icon vectors must exist
        assertTrue("Missing ic_notification_emochi.xml", File(resDir, "drawable/ic_notification_emochi.xml").exists())
        assertTrue("Missing ic_launcher_monochrome.xml", File(resDir, "drawable/ic_launcher_monochrome.xml").exists())

        // CRITICAL: Never delete any of the 96 original drawable-nodpi/*.webp assets (+3 new logos = >= 99)
        val nodpiWebps = File(resDir, "drawable-nodpi")
            .listFiles { f -> f.isFile && f.extension.equals("webp", ignoreCase = true) }
            .orEmpty()
        assertTrue(
            "All 96 original WebP assets + 3 logo WebPs must be preserved (found ${nodpiWebps.size})",
            nodpiWebps.size >= 99
        )
    }

    // ── 2. Sistem #6: Şablon Klonlama Atomikliği & İdempotency Testi ─────────

    @Test
    fun test2_system6_templateAtomicCloneAndIdempotency() = runBlocking {
        val templatePreset = BotEntity(
            id = "preset_template_test_aiden",
            mode = "universe",
            aiName = "Aiden",
            aiPersonality = "İnatçı, gururlu ama sadık. Her Pazartesi antrenmana gider. Sabahları mutlaka koyu kahve içer. İhanet ve alay edilmek onu çileden çıkarır.",
            scenario = "Sezonun ilk kamp sabahında yağmur yağıyor.",
            universeName = "Aiden Stories",
            keyCharactersJson = """[{"name":"Elif","role":"Yakın Arkadaş","description":"Sakin ve güvenilir"}]""",
            userCharName = "Eren",
            userCharDesc = "Takım arkadaşı",
            openingMessage = "*Yağmur altında sahaya baktı.* \"Geç kaldın.\"",
            writingStyle = "novel",
            intensity = "normal",
            isTemplate = true,
            isPublic = true,
            hasExplicitStoryDate = false,
            storyCalendarDate = "2099-01-01" // must be wiped when hasExplicitStoryDate == false
        )

        val created = repo.createBotFromTemplateAtomic(templatePreset)
        val savedBot = db.botDao().getBotById(created.id)
        assertNotNull("Created bot must be persisted atomically", savedBot)
        assertFalse("Cloned bot must not be marked as template", savedBot!!.isTemplate)
        assertFalse("Cloned bot must not be marked public", savedBot.isPublic)
        assertEquals(
            "Explicit story date must stay blank when preset hasExplicitStoryDate=false",
            "",
            savedBot.storyCalendarDate
        )
        assertTrue(
            "Baseline emotion analysis JSON must be initialized during template clone",
            savedBot.baselineEmotionAnalysisJson.isNotBlank()
        )
        assertTrue(
            "Cloned bot must be flagged as template-origin",
            AppFeatureFlags.isTemplateOriginBot(savedBot.id)
        )

        // Opening message & routines transferred atomically
        val messages = db.messageDao().getMessagesForBotList(savedBot.id)
        assertEquals(1, messages.size)
        assertEquals("assistant", messages.first().role)

        val routines = db.routineDao().getRoutinesForBot(savedBot.id)
        assertTrue("Routines must be extracted during atomic clone", routines.size >= 2)

        // Idempotent double-tap guard verification via EmochiViewModel
        val vm = com.example.ui.viewmodel.EmochiViewModel(ctx.applicationContext as android.app.Application, repo)
        val firstAccepted = vm.createPresetBot(templatePreset)
        assertTrue("First preset creation tap must be accepted", firstAccepted)
        val secondImmediateTap = vm.createPresetBot(templatePreset)
        assertFalse("Rapid duplicate double-tap must be blocked by AtomicBoolean lock", secondImmediateTap)
    }

    // ── 3. Sistem #12: Arka Plan İşçisi Güvenlik & Cache Invalidation Testi ──

    @Test
    fun test3_system12_workerSafetyGuardsAndCacheInvalidation() = runBlocking {
        val now = 1_750_000_000_000L
        val threeHoursAgo = now - (3 * 60 * 60 * 1000L)
        val idleUserMsg = listOf(
            MessageEntity(
                id = "m1",
                botId = "bot_worker",
                role = "user",
                text = "Görüşürüz",
                timestamp = threeHoursAgo
            )
        )

        // ProactiveMessageWorker guards: active open chat & quiet hours (23:00-08:00)
        assertFalse(
            "Active open chat must NEVER receive proactive notification",
            ProactiveMessageWorker.shouldSendProactiveMessage(
                botId = "bot_worker",
                proactiveEnabled = true,
                activeBotId = "bot_worker",
                botLastMessageTimestamp = threeHoursAgo,
                recentMessages = idleUserMsg,
                nowMs = now,
                currentHourOfDay = 14
            )
        )
        assertFalse(
            "Quiet hour 23:00 must block proactive worker",
            ProactiveMessageWorker.shouldSendProactiveMessage(
                botId = "bot_worker",
                proactiveEnabled = true,
                activeBotId = null,
                botLastMessageTimestamp = threeHoursAgo,
                recentMessages = idleUserMsg,
                nowMs = now,
                currentHourOfDay = 23
            )
        )
        assertFalse(
            "Quiet hour 03:00 must block proactive worker",
            ProactiveMessageWorker.shouldSendProactiveMessage(
                botId = "bot_worker",
                proactiveEnabled = true,
                activeBotId = null,
                botLastMessageTimestamp = threeHoursAgo,
                recentMessages = idleUserMsg,
                nowMs = now,
                currentHourOfDay = 3
            )
        )
        assertTrue(
            "Daytime non-active idle bot should pass proactive gate",
            ProactiveMessageWorker.shouldSendProactiveMessage(
                botId = "bot_worker",
                proactiveEnabled = true,
                activeBotId = "other_bot",
                botLastMessageTimestamp = threeHoursAgo,
                recentMessages = idleUserMsg,
                nowMs = now,
                currentHourOfDay = 14
            )
        )

        // RoutineNotificationWorker guards: active open chat, quiet hours, feature flag
        assertFalse(
            "Routine notification must skip active open chat",
            RoutineNotificationWorker.shouldNotifyRoutine(
                botId = "bot_worker",
                activeBotId = "bot_worker",
                isRoutineNotifEnabled = true,
                currentHourOfDay = 12
            )
        )
        assertFalse(
            "Routine notification must skip quiet hours (02:00)",
            RoutineNotificationWorker.shouldNotifyRoutine(
                botId = "bot_worker",
                activeBotId = null,
                isRoutineNotifEnabled = true,
                currentHourOfDay = 2
            )
        )
        assertFalse(
            "Routine notification must respect feature flag off",
            RoutineNotificationWorker.shouldNotifyRoutine(
                botId = "bot_worker",
                activeBotId = null,
                isRoutineNotifEnabled = false,
                currentHourOfDay = 12
            )
        )
        assertTrue(
            "Routine notification should pass during daytime for inactive bot",
            RoutineNotificationWorker.shouldNotifyRoutine(
                botId = "bot_worker",
                activeBotId = "other_bot",
                isRoutineNotifEnabled = true,
                currentHourOfDay = 12
            )
        )

        // BackgroundMemoryMergeWorker: CORE & pinned protection + merge + cache invalidation
        val bot = buildTestBot(id = "bot_worker")
        db.botDao().insertOrUpdate(bot)

        val coreDesc = LivingMemoryCodec.encode(
            povCharacter = "Asroda",
            title = "Çocukluk Bağı [CORE]",
            emotionTag = "Güven",
            salience = 10,
            narrative = "Eren karakterin en yakın çocukluk dostudur.",
            innerThought = "Onu asla unutmam."
        )
        val coreEvent = MemoryEventEntity(
            botId = bot.id,
            description = coreDesc,
            importanceScore = 100,
            isBotSaved = true
        )
        assertTrue("CORE/pinned structured event must be protected", BackgroundMemoryMergeWorker.isProtectedEvent(coreEvent))

        val dup1 = MemoryEventEntity(
            botId = bot.id,
            description = "Sahilde birlikte sıcak çay içip eski günlerden konuştuk.",
            importanceScore = 5
        )
        val dup2 = MemoryEventEntity(
            botId = bot.id,
            description = "Sahilde birlikte sıcak çay içip eski günlerden konuştuk.",
            importanceScore = 5
        )
        db.memoryEventDao().insertEvent(coreEvent)
        db.memoryEventDao().insertEvent(dup1)
        db.memoryEventDao().insertEvent(dup2)

        val mutated = BackgroundMemoryMergeWorker.runMaintenanceForBot(
            botId = bot.id,
            repository = repo,
            db = db,
            customMerger = { "Sahilde sıcak çay eşliğinde eski günler yâd edildi." }
        )
        assertTrue("Maintenance must merge duplicates and invalidate memory caches", mutated)

        val remainingActive = db.memoryEventDao().getActiveEvents(bot.id)
        assertTrue(
            "CORE/pinned event must remain active and untouched",
            remainingActive.any { it.description == coreDesc }
        )
        assertTrue(
            "Merged summary must be saved as active event",
            remainingActive.any { it.description.contains("yâd edildi") }
        )
        assertEquals("Original 2 duplicates should merge into 1 + 1 CORE = 2 active events", 2, remainingActive.size)
    }

    // ── 4. Sistem #14: Hikâye Zamanı Bazlı Duygu Sönümleme Testi ─────────────

    @Test
    fun test4_system14_storyTimeEmotionalDecayProportionalToRegulation() {
        val baseline = EmotionState(
            primaryEmotions = PrimaryEmotions(anger = 10, fear = 10, sadness = 10, surprise = 5, trust = 50),
            relationshipAxes = RelationshipAxes(affectionScore = 50, resentmentScore = 5)
        )
        val heated = EmotionState(
            dominantEmotion = "öfkeli",
            primaryEmotions = PrimaryEmotions(anger = 85, fear = 40, sadness = 35, surprise = 60, trust = 72),
            relationshipAxes = RelationshipAxes(affectionScore = 68, resentmentScore = 60)
        )

        // 0 story days -> zero decay (wall-clock completely ignored)
        val unchanged = EmotionEngine.applyNaturalEmotionalDecay(
            currentState = heated,
            storyDaysSkipped = 0,
            regulationCapacity = 90,
            baseline = baseline
        )
        assertEquals(85, unchanged.primaryEmotions.anger)
        assertEquals(72, unchanged.primaryEmotions.trust)
        assertEquals(68, unchanged.relationshipAxes.affectionScore)

        // 3 story days -> anger decays toward baseline proportionally to emotionalRegulationCapacity
        val highCapDecayed = EmotionEngine.applyNaturalEmotionalDecay(
            currentState = heated,
            storyDaysSkipped = 3,
            regulationCapacity = 85,
            baseline = baseline
        )
        val lowCapDecayed = EmotionEngine.applyNaturalEmotionalDecay(
            currentState = heated,
            storyDaysSkipped = 3,
            regulationCapacity = 20,
            baseline = baseline
        )

        assertTrue("High regulation capacity must cool anger significantly", highCapDecayed.primaryEmotions.anger < 85)
        assertTrue("Low regulation capacity must cool anger slower than high capacity", highCapDecayed.primaryEmotions.anger < lowCapDecayed.primaryEmotions.anger)
        assertTrue("Decayed anger must not drop below baseline", highCapDecayed.primaryEmotions.anger >= baseline.primaryEmotions.anger)

        // Long-term trust & affection must NEVER erode on a 3-day skip (< 14 days)
        assertEquals(72, highCapDecayed.primaryEmotions.trust)
        assertEquals(68, highCapDecayed.relationshipAxes.affectionScore)
        assertEquals(72, lowCapDecayed.primaryEmotions.trust)
        assertEquals(68, lowCapDecayed.relationshipAxes.affectionScore)

        // >= 14 story days -> mild distancing (-1..-3) without resetting bond
        val longSkipDecayed = EmotionEngine.applyNaturalEmotionalDecay(
            currentState = heated,
            storyDaysSkipped = 14,
            regulationCapacity = 55,
            baseline = baseline
        )
        assertTrue("14-day skip applies mild -1..-3 distancing on trust", longSkipDecayed.primaryEmotions.trust in 69..71)
        assertTrue("14-day skip applies mild -1..-3 distancing on affection", longSkipDecayed.relationshipAxes.affectionScore in 65..67)
    }

    // ── 5. Sistem #14: Yan Karakter cast_emo Uçtan Uca Testi ─────────────────

    @Test
    fun test5_system14_sideCharacterCastEmoEndToEndAndPromptInjection() = runBlocking {
        val bot = buildTestBot(id = "bot_cast_emo")
        db.botDao().insertOrUpdate(bot)
        db.castMemberDao().insertCastMember(
            CastMemberEntity(
                botId = bot.id,
                name = "Elif",
                role = "Yakın Arkadaş",
                description = "Sakin ve güvenilir bir arkadaş",
                affectionScore = 35
            )
        )

        val aiResponse = """
            *Elif omuzlarını gevşetip hafifçe gülümsedi.* "İyi ki geldin."
            [[STATE_JSON {"dom":"sakin","cast_emo":[{"n":"Elif'in","d":"rahatlamış","v":5}]}]]
        """.trimIndent()

        repo.parseAndApplyEmotionUpdates(bot.id, aiResponse)

        val allCharEmotions = db.characterEmotionDao().getEmotionsForBot(bot.id)
        val elifEmotionEntity = allCharEmotions.firstOrNull { it.characterName == "Elif" }
        assertNotNull("Turkish possessive 'Elif'in' must resolve to canonical 'Elif'", elifEmotionEntity)
        assertFalse(
            "Possessive duplicate 'Elif'in' must NOT be created in character_emotions",
            allCharEmotions.any { it.characterName.contains("'") }
        )

        val parsedElifState = EmotionState.fromJson(elifEmotionEntity!!.emotionState)
        assertEquals("rahatlamış", parsedElifState.dominantEmotion)
        assertTrue("Updated affection must stay clamped in [0..100] and rise from 35", parsedElifState.affection in 36..43)
        assertTrue("Updated trust must stay clamped in [0..100]", parsedElifState.trust in 0..100)

        // Verify next turn SystemPromptBuilder injects live side-character emotions
        val prompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = db.botDao().getBotById(bot.id)!!,
                settings = UserSettingsEntity(),
                castMembers = db.castMemberDao().getCastMembersForBot(bot.id),
                characterEmotions = allCharEmotions,
                recentSceneText = "Elif masada oturuyor."
            )
        )
        assertTrue(
            "Next-turn prompt must include [AKTİF YAN KARAKTER DUYGULARI]",
            prompt.contains("[AKTİF YAN KARAKTER DUYGULARI]")
        )
        assertTrue(
            "Prompt must surface Elif's live mood and trust/affection values",
            prompt.contains("Elif (rahatlamış, güven:")
        )
    }

    // ── 6. Sistem #15: story_time_skip Kaskad & Açık Tarih Koruması Testi ────

    @Test
    fun test6_system15_storyTimeSkipCascadeAndExplicitDateProtection() = runBlocking {
        // Case A: hasExplicitStoryDate == true ("2026-11-10") + "skip": 3 -> "2026-11-13", dayCounter + 3, decay applied
        val angryState = EmotionState(
            dominantEmotion = "öfkeli",
            primaryEmotions = PrimaryEmotions(anger = 80, trust = 60),
            relationshipAxes = RelationshipAxes(affectionScore = 55, resentmentScore = 50)
        )
        val explicitDateBot = buildTestBot(
            id = "bot_time_explicit",
            hasExplicitStoryDate = true,
            storyCalendarDate = "2026-11-10",
            storyDayCounter = 5L,
            regulationCapacity = 70,
            emotionState = angryState
        )
        db.botDao().insertOrUpdate(explicitDateBot)

        repo.parseAndApplyEmotionUpdates(
            botId = explicitDateBot.id,
            rawResponse = """*Üç gün hızla geçti.* [[STATE_JSON {"dom":"sakin","skip":3}]]"""
        )

        val updatedExplicit = db.botDao().getBotById(explicitDateBot.id)!!
        assertEquals("2026-11-13", updatedExplicit.storyCalendarDate)
        assertEquals(8L, updatedExplicit.storyDayCounter)
        val afterSkipEmotion = EmotionState.fromJson(updatedExplicit.emotionState)
        assertTrue(
            "3-day skip must trigger natural anger decay (was 80, now ${afterSkipEmotion.primaryEmotions.anger})",
            afterSkipEmotion.primaryEmotions.anger < 80
        )

        // Case B: hasExplicitStoryDate == false -> storyDayCounter + 2, storyCalendarDate stays "" (never fabricated!)
        val noDateBot = buildTestBot(
            id = "bot_time_nodate",
            hasExplicitStoryDate = false,
            storyCalendarDate = "",
            storyDayCounter = 4L
        )
        db.botDao().insertOrUpdate(noDateBot)

        repo.parseAndApplyEmotionUpdates(
            botId = noDateBot.id,
            rawResponse = """*İki gün sonra tekrar buluştular.* [[STATE_JSON {"dom":"sakin","skip":2}]]"""
        )

        val updatedNoDate = db.botDao().getBotById(noDateBot.id)!!
        assertEquals(6L, updatedNoDate.storyDayCounter)
        assertEquals(
            "Calendar date must NEVER be fabricated when hasExplicitStoryDate == false",
            "",
            updatedNoDate.storyCalendarDate
        )

        // Case C: Quoted future promise ("Yarın sabah görüşürüz") must NOT trigger a time skip
        val quotedPromiseSkip = TimePerceptionEngine.parseHybridSkip(
            rawSkipJson = null,
            legacyStorySkipObj = null,
            narrativeText = """*Elif kapıda el salladı.* "Yarın sabah görüşürüz!" dedi."""
        )
        assertEquals("Quoted future promise must not advance days", 0, quotedPromiseSkip.daysToAdvance)
        assertNull("Quoted future promise must not override scene time", quotedPromiseSkip.newSceneTime)
    }

    // ── 7. Sistem #21: Gün Filtreli Rutin & Tekrar Engeli Testi ──────────────

    @Test
    fun test7_system21_dayFilteredRoutineAndSameDayRepeatLock() = runBlocking {
        val bot = buildTestBot(id = "bot_routine_days")
        db.botDao().insertOrUpdate(bot)

        val mondayId = db.routineDao().insertRoutine(
            RoutineEntity(
                botId = bot.id,
                routineDescription = "Her Pazartesi sabah antrenmana gider.",
                frequency = "weekly",
                dayOfWeek = "Pazartesi",
                lastMentionedStoryDate = ""
            )
        )
        db.routineDao().insertRoutine(
            RoutineEntity(
                botId = bot.id,
                routineDescription = "Her Salı kütüphanede çalışır.",
                frequency = "weekly",
                dayOfWeek = "Salı",
                lastMentionedStoryDate = ""
            )
        )

        // 2026-11-16 is a Monday (Pazartesi)
        val mondayPick = worldStateManager.getActiveRoutineReminder(
            botId = bot.id,
            currentStoryDate = "2026-11-16",
            storyDayCounter = 1L
        )
        assertNotNull("Monday story date must match Monday routine", mondayPick)
        assertEquals(mondayId, mondayPick!!.id)
        assertTrue(mondayPick.routineDescription.contains("Pazartesi"))

        // Mark mentioned on 2026-11-16 -> same-day repeat lock returns null
        worldStateManager.markRoutineMentioned(mondayPick.id, "2026-11-16", bot.id)
        val sameDayRepeat = worldStateManager.getActiveRoutineReminder(
            botId = bot.id,
            currentStoryDate = "2026-11-16",
            storyDayCounter = 1L
        )
        assertNull("Routine already mentioned on 2026-11-16 must not repeat on the same story day", sameDayRepeat)

        // Next Monday (2026-11-23) unlocks Monday routine again
        val nextMondayPick = worldStateManager.getActiveRoutineReminder(
            botId = bot.id,
            currentStoryDate = "2026-11-23",
            storyDayCounter = 8L
        )
        assertNotNull("Next Monday (2026-11-23) must unlock Monday routine again", nextMondayPick)
        assertEquals(mondayId, nextMondayPick!!.id)
    }

    // ── 8. Sistem #21: Doğal Türkçe Rutin Çıkarım & Tekilleştirme Testi ──────

    @Test
    fun test8_system21_naturalTurkishRoutineExtractionAndDeduplication() = runBlocking {
        val bot = buildTestBot(id = "bot_routine_extract")
        db.botDao().insertOrUpdate(bot)

        val personality = "Her Pazartesi antrenmana gider. Sabahları mutlaka kahve içer."

        val firstRun = worldStateManager.extractAndSaveRoutinesFromBotPersonality(
            botId = bot.id,
            personality = personality,
            scenario = ""
        )
        assertEquals("Should extract exactly 2 unique Turkish routines", 2, firstRun.size)
        assertTrue(
            "Should detect weekly Monday routine",
            firstRun.any { it.frequency == "weekly" && it.dayOfWeek == "Pazartesi" }
        )
        assertTrue(
            "Should detect morning coffee routine",
            firstRun.any { it.frequency == "morning" && it.routineDescription.contains("kahve", ignoreCase = true) }
        )

        // Re-running extraction must NOT duplicate routines in DB
        val secondRun = worldStateManager.extractAndSaveRoutinesFromBotPersonality(
            botId = bot.id,
            personality = personality,
            scenario = ""
        )
        assertEquals(2, secondRun.size)
        val allInDb = db.routineDao().getRoutinesForBot(bot.id)
        assertEquals("Re-running extraction must keep exactly 2 rows without duplicates", 2, allInDb.size)
    }
}
