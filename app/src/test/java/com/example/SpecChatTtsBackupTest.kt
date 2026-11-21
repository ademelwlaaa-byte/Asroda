package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ArchivedMemoryEntity
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.EntityRelationEntity
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MemoryFactEntity
import com.example.data.local.MessageEntity
import com.example.data.local.MilestoneEntity
import com.example.data.local.RoutineEntity
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.BaselineEmotionEngine
import com.example.data.repository.EmochiRepository
import com.example.data.repository.EmotionState
import com.example.util.AppFeatureFlags
import com.example.util.ChatExporter
import com.example.util.ChatTtsManager
import kotlinx.coroutines.runBlocking
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SpecChatTtsBackupTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: EmochiRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = EmochiRepository(db, context)
    }

    @After
    fun tearDown() {
        File(context.filesDir, "emochi_autobackup.json").delete()
        File(context.filesDir, "emochi_autobackup.json.tmp").delete()
        context.getExternalFilesDir(null)?.let {
            File(it, "emochi_autobackup.json").delete()
        }
        db.close()
    }

    private fun createSampleBot(
        id: String = "bot_spec_1",
        hasExplicitStoryDate: Boolean = false,
        storyCalendarDate: String = ""
    ): BotEntity = BotEntity(
        id = id,
        mode = "personal",
        aiName = "Aiden",
        aiPersonality = "Gizemli, mesafeli ama sadık stratejist.",
        scenario = "Gece yarısı katedral bahçesinde buluşma.",
        universeName = "",
        keyCharactersJson = "[]",
        userCharName = "Selin",
        userCharDesc = "Korkusuz araştırmacı.",
        openingMessage = "Geç kaldın.",
        writingStyle = "rp",
        intensity = "normal",
        storyCalendarDate = storyCalendarDate,
        hasExplicitStoryDate = hasExplicitStoryDate
    )

    // =========================================================================
    // TEST 1: Rewind Tam Temizlik (milestones, entity_relations, archived_memories, routines, cache)
    // =========================================================================
    @Test
    fun test1_rewindToMessage_cleansMilestonesRelationsArchivesRoutinesAndCounters() = runBlocking {
        val bot = createSampleBot(id = "bot_rewind_1")
        db.botDao().insertOrUpdate(bot)

        val t1 = 1_000_000L
        val t2 = 2_000_000L
        val t3 = 3_000_000L

        db.messageDao().insertMessage(MessageEntity(id = "m1", botId = bot.id, role = "assistant", text = "Açılış", timestamp = t1))
        db.messageDao().insertMessage(MessageEntity(id = "m2", botId = bot.id, role = "user", text = "Selam", timestamp = t2))
        db.messageDao().insertMessage(MessageEntity(id = "m3", botId = bot.id, role = "assistant", text = "İleri sahne", timestamp = t3))

        // Milestone before rewind (keep) & after rewind (delete)
        db.milestoneDao().insertMilestone(
            MilestoneEntity(id = "${bot.id}_early", botId = bot.id, type = "early", title = "İlk Kıvılcım", emoji = "✨", reachedAt = t1, affectionAtTime = 60, trustAtTime = 55)
        )
        db.milestoneDao().insertMilestone(
            MilestoneEntity(id = "${bot.id}_late", botId = bot.id, type = "late", title = "Derin Bağ", emoji = "💜", reachedAt = t3, affectionAtTime = 90, trustAtTime = 85)
        )

        // Entity relations before (index 2 -> keep) & after (index 3 -> delete)
        db.entityRelationDao().insertRelation(
            EntityRelationEntity(botId = bot.id, sourceEntityId = 1L, sourceEntityName = "Aiden", relationType = "ally", targetEntityId = 2L, targetEntityName = "Selin", confidence = "high", createdAtMessageIndex = 2)
        )
        db.entityRelationDao().insertRelation(
            EntityRelationEntity(botId = bot.id, sourceEntityId = 1L, sourceEntityName = "Aiden", relationType = "rival", targetEntityId = 3L, targetEntityName = "Marcus", confidence = "high", createdAtMessageIndex = 3)
        )

        // Archived memories before (keep) & after (delete)
        db.archivedMemoryDao().insertArchive(
            ArchivedMemoryEntity(botId = bot.id, originalMemoryId = 10L, memoryType = "fact", content = "Eski arşiv", archivedReason = "test", timestamp = t1)
        )
        db.archivedMemoryDao().insertArchive(
            ArchivedMemoryEntity(botId = bot.id, originalMemoryId = 11L, memoryType = "fact", content = "İleri arşiv", archivedReason = "test", timestamp = t3)
        )

        // Routine mentioned in the future relative to rewind
        db.routineDao().insertRoutine(
            RoutineEntity(botId = bot.id, routineDescription = "Gece devriyesi", frequency = "daily", dayOfWeek = "", lastMentionedStoryDate = "2027-05-10")
        )

        repository.incrementRegenerateCount(bot.id)
        assertTrue(repository.getRegenerateCount(bot.id) > 0)

        val ok = repository.rewindToMessage(bot.id, "m2")
        assertTrue("Rewind should succeed", ok)

        // Messages trimmed to m1, m2
        val remainingMsgs = db.messageDao().getMessagesForBotList(bot.id)
        assertEquals(2, remainingMsgs.size)
        assertEquals("m2", remainingMsgs.last().id)

        // Milestones after t2 deleted, earlier preserved
        val remainingMilestones = db.milestoneDao().getMilestonesForBot(bot.id)
        assertEquals(1, remainingMilestones.size)
        assertEquals("early", remainingMilestones.first().type)

        // Entity relations with createdAtMessageIndex > 2 deleted
        val remainingRelations = db.entityRelationDao().getRelationsForBot(bot.id)
        assertEquals(1, remainingRelations.size)
        assertEquals("Selin", remainingRelations.first().targetEntityName)

        // Archived memories after t2 deleted
        val remainingArchives = db.archivedMemoryDao().getArchivesForBot(bot.id)
        assertEquals(1, remainingArchives.size)
        assertEquals("Eski arşiv", remainingArchives.first().content)

        // Routine lastMentionedStoryDate reset because restoredCalDate is blank
        val remainingRoutines = db.routineDao().getRoutinesForBot(bot.id)
        assertEquals(1, remainingRoutines.size)
        assertEquals("", remainingRoutines.first().lastMentionedStoryDate)

        // In-memory regenerate counter reset
        assertEquals(0, repository.getRegenerateCount(bot.id))
    }

    // =========================================================================
    // TEST 2: CloneBot Tetikleyici, Tarih (2026-08-29 yok!) ve LLM Baseline Koruması
    // =========================================================================
    @Test
    fun test2_cloneBot_copiesTriggersResetsCountPreservesBaselineAndNoHardcodedDate() = runBlocking {
        val customBaselineState = EmotionState(
            mood = "temkinli",
            intensity = 6,
            affection = 32,
            trust = 28,
            tension = 68,
            hurt = 14,
            obsession = 5,
            respect = 74,
            comfort = 22
        )
        val storedAnalysis = BaselineEmotionEngine.BaselineAnalysisResult(
            baselineEmotionState = customBaselineState,
            worldAtmosphereMood = "gergin",
            worldAtmosphereIntensity = 7,
            baseAffectionDifficulty = 1.4,
            emotionalRegulationCapacity = 65,
            relationshipArchetype = "rival",
            reasoningSummary = "LLM analizi",
            source = "llm"
        )

        val botWithoutExplicitDate = createSampleBot(
            id = "bot_src_nodate",
            hasExplicitStoryDate = false,
            storyCalendarDate = "2026-11-11"
        ).copy(
            baselineEmotionAnalysisJson = BaselineEmotionEngine.serialize(storedAnalysis),
            baselineAnalysisSource = "llm"
        )
        db.botDao().insertOrUpdate(botWithoutExplicitDate)
        db.sensitiveTriggerDao().insertOrUpdate(
            SensitiveTriggerEntity(
                botId = botWithoutExplicitDate.id,
                triggerTopic = "İhanet",
                sourceDescription = "Geçmiş travma",
                intensityMultiplier = 1.8,
                recentTriggerCount = 4
            )
        )

        val clonedNoDate = repository.cloneBot(botWithoutExplicitDate.id)
        assertNotNull(clonedNoDate)
        assertEquals("Implicit date bot must clone with empty storyCalendarDate, NOT 2026-08-29", "", clonedNoDate!!.storyCalendarDate)
        assertFalse(clonedNoDate.storyCalendarDate == "2026-08-29")

        // Stored LLM baseline emotion must be preserved on clone
        val clonedEmotion = EmotionState.fromJson(clonedNoDate.emotionState)
        assertEquals(32, clonedEmotion.affection)
        assertEquals(28, clonedEmotion.trust)
        assertEquals(68, clonedEmotion.tension)

        // Sensitive triggers copied with recentTriggerCount reset to 0
        val clonedTriggers = db.sensitiveTriggerDao().getTriggersForBot(clonedNoDate.id)
        assertEquals(1, clonedTriggers.size)
        assertEquals("İhanet", clonedTriggers.first().triggerTopic)
        assertEquals(0, clonedTriggers.first().recentTriggerCount)

        // Bot WITH explicit story date must keep its explicit calendar date
        val botWithExplicitDate = createSampleBot(
            id = "bot_src_withdate",
            hasExplicitStoryDate = true,
            storyCalendarDate = "1899-10-31"
        )
        db.botDao().insertOrUpdate(botWithExplicitDate)
        val clonedWithDate = repository.cloneBot(botWithExplicitDate.id)
        assertNotNull(clonedWithDate)
        assertEquals("1899-10-31", clonedWithDate!!.storyCalendarDate)
    }

    // =========================================================================
    // TEST 3: TTS Metin Temizleme & 350 Karakterlik Parçalama (Chunking)
    // =========================================================================
    @Test
    fun test3_ttsCleaningAndChunking_handlesLongMultiParagraphRpReply() {
        val longRpReply = buildString {
            append("<think>İç analiz burada gizlenmeli.</think>\n")
            append("*Aiden yağmur damlalarının camdan süzülüşünü uzun süre sessizce izler,\nardından omzunun üzerinden dönüp gözlerini kısar.*\n\n")
            repeat(8) { idx ->
                append("\"Dinle beni, bu gece katedralin altındaki arşiv odasına indiğimizde hiçbir iz bırakmamalıyız; ")
                append("çünkü Konsey'in gözcüleri her köşede nöbet tutuyor ve en ufak bir hata tüm planı yakar (${idx + 1}).\" ")
            }
            append("\n*Cebinden gümüş mührü çıkarıp masaya bırakır.*\n")
            append("[[STATE_JSON {\"affection\":72,\"trust\":66}]]")
        }
        assertTrue("Fixture should exceed 1200 chars", longRpReply.length > 1200)

        val dialogueOnly = ChatTtsManager.prepareTtsText(longRpReply, readActions = false)
        assertFalse("Internal <think> tag must be removed", dialogueOnly.contains("İç analiz"))
        assertFalse("STATE_JSON must be removed", dialogueOnly.contains("STATE_JSON"))
        assertFalse("Multiline *action* must be stripped when readActions=false", dialogueOnly.contains("yağmur damlalarının"))
        assertFalse("Single-line *action* must be stripped when readActions=false", dialogueOnly.contains("gümüş mührü"))
        assertTrue("Dialogue must remain intact", dialogueOnly.contains("arşiv odasına indiğimizde"))

        val withActions = ChatTtsManager.prepareTtsText(longRpReply, readActions = true)
        assertFalse("Asterisks should be cleaned when reading actions", withActions.contains("*"))
        assertTrue("Action text should be preserved when readActions=true", withActions.contains("yağmur damlalarının"))
        assertFalse("STATE_JSON must still be removed", withActions.contains("STATE_JSON"))

        val chunks = ChatTtsManager.splitForTts(dialogueOnly, maxChunkChars = 350)
        assertTrue("Long text must be split into multiple chunks", chunks.size >= 3)
        chunks.forEach { chunk ->
            assertTrue("Each TTS chunk must be <= 350 chars, was ${chunk.length}", chunk.length <= 350)
            assertTrue("Chunk must not be blank", chunk.isNotBlank())
        }
    }

    // =========================================================================
    // TEST 4: BackupSnapshot v2 Tamlığı & v1 Geriye Dönük Uyumluluk
    // =========================================================================
    @Test
    fun test4_backupSnapshotV2_roundTripsAllTablesAndSupportsV1LegacyJson() = runBlocking {
        val bot = createSampleBot(id = "bot_v2_1")
        db.botDao().insertOrUpdate(bot)
        db.messageDao().insertMessage(MessageEntity(id = "msg_v2_1", botId = bot.id, role = "assistant", text = "Hoş geldin.", timestamp = 1_500_000L))
        db.memoryFactDao().insertFact(MemoryFactEntity(botId = bot.id, subject = "Selin", key = "meslek", value = "Dedektif"))
        db.memoryEventDao().insertEvent(MemoryEventEntity(botId = bot.id, timestamp = 1_500_000L, description = "Katedralde anlaşma yapıldı", importanceScore = 8))
        db.castMemberDao().insertCastMember(CastMemberEntity(botId = bot.id, name = "Elena", role = "Müttefik"))
        db.sensitiveTriggerDao().insertOrUpdate(SensitiveTriggerEntity(botId = bot.id, triggerTopic = "Kayıp", sourceDescription = "Kardeşi", intensityMultiplier = 1.6))
        db.routineDao().insertRoutine(RoutineEntity(botId = bot.id, routineDescription = "Sabah koşusu", frequency = "daily"))
        db.milestoneDao().insertMilestone(MilestoneEntity(id = "${bot.id}_m1", botId = bot.id, type = "first_bond", title = "İlk Bağ", emoji = "💫", affectionAtTime = 65, trustAtTime = 60))

        val exportedV2 = repository.exportDataSnapshot(includeApiKeys = false)
        val parsedRoot = JSONObject(exportedV2)
        assertEquals(2, parsedRoot.getInt("version"))

        // Wipe and restore into clean state via overwriteExisting = true
        val summary = repository.importDataSnapshot(exportedV2, overwriteExisting = true)
        assertEquals(2, summary.version)
        assertEquals(1, summary.botsCount)
        assertEquals(1, summary.messagesCount)
        assertEquals(1, summary.memoryFactsCount)
        assertEquals(1, summary.memoryEventsCount)
        assertEquals(1, summary.castMembersCount)
        assertEquals(1, summary.milestonesCount)

        assertEquals(1, db.memoryFactDao().getActiveFacts(bot.id).size)
        assertEquals(1, db.memoryEventDao().getActiveEvents(bot.id).size)
        assertEquals(1, db.castMemberDao().getCastMembersForBot(bot.id).size)
        assertEquals(1, db.sensitiveTriggerDao().getTriggersForBot(bot.id).size)
        assertEquals(1, db.routineDao().getRoutinesForBot(bot.id).size)
        assertEquals(1, db.milestoneDao().getMilestonesForBot(bot.id).size)

        // Legacy v1 JSON (no v2 fields at all) must still import cleanly!
        val legacyV1Json = """
            {
              "version": 1,
              "bots": [
                {
                  "id": "bot_legacy_v1",
                  "mode": "personal",
                  "aiName": "EskiBot",
                  "aiPersonality": "Klasik",
                  "scenario": "Test",
                  "universeName": "",
                  "keyCharactersJson": "[]",
                  "userCharName": "Sen",
                  "userCharDesc": "",
                  "openingMessage": "Merhaba v1",
                  "writingStyle": "rp",
                  "intensity": "normal"
                }
              ],
              "messages": [
                {
                  "id": "msg_v1_1",
                  "botId": "bot_legacy_v1",
                  "role": "assistant",
                  "text": "Merhaba v1",
                  "timestamp": 123456
                }
              ],
              "settings": { "id": 1 }
            }
        """.trimIndent()

        val v1Summary = repository.importDataSnapshot(legacyV1Json, overwriteExisting = false)
        assertEquals(1, v1Summary.version)
        assertEquals(1, v1Summary.botsCount)
        assertNotNull(db.botDao().getBotById("bot_legacy_v1"))
    }

    // =========================================================================
    // TEST 5: API Anahtarı Maskeleme Güvenliği & Mevcut Cihaz Anahtarlarını Koruma
    // =========================================================================
    @Test
    fun test5_apiKeyMasking_stripsAllKeysByDefaultAndPreservesDeviceKeysOnRestore() = runBlocking {
        val secretSettings = UserSettingsEntity(
            customApiKey = "AIza-SECRET-GEMINI",
            groqApiKey = "gsk_SECRET_GROQ",
            claudeApiKey = "sk-ant-SECRET-CLAUDE",
            openaiApiKey = "sk-SECRET-OPENAI",
            openRouterApiKey = "sk-or-SECRET-OR",
            nvidiaApiKey = "nvapi-SECRET-NVIDIA",
            mistralApiKey = "mistral-SECRET-KEY",
            backupApiKey = "backup-SECRET-KEY"
        )
        db.userSettingsDao().insertOrUpdate(secretSettings)
        db.botDao().insertOrUpdate(createSampleBot(id = "bot_sec_1"))

        val safeExport = repository.exportDataSnapshot(includeApiKeys = false)
        listOf(
            "AIza-SECRET-GEMINI",
            "gsk_SECRET_GROQ",
            "sk-ant-SECRET-CLAUDE",
            "sk-SECRET-OPENAI",
            "sk-or-SECRET-OR",
            "nvapi-SECRET-NVIDIA",
            "mistral-SECRET-KEY",
            "backup-SECRET-KEY"
        ).forEach { secret ->
            assertFalse("Default export MUST NOT contain secret key $secret", safeExport.contains(secret))
        }

        // Importing the sanitized backup onto this device must NOT wipe existing saved device API keys
        repository.importDataSnapshot(safeExport, overwriteExisting = false)
        val afterRestore = db.userSettingsDao().getUserSettings()
        assertNotNull(afterRestore)
        assertEquals("AIza-SECRET-GEMINI", afterRestore!!.customApiKey)
        assertEquals("gsk_SECRET_GROQ", afterRestore.groqApiKey)
        assertEquals("sk-ant-SECRET-CLAUDE", afterRestore.claudeApiKey)
        assertEquals("sk-SECRET-OPENAI", afterRestore.openaiApiKey)
        assertEquals("sk-or-SECRET-OR", afterRestore.openRouterApiKey)
        assertEquals("nvapi-SECRET-NVIDIA", afterRestore.nvidiaApiKey)
        assertEquals("mistral-SECRET-KEY", afterRestore.mistralApiKey)
        assertEquals("backup-SECRET-KEY", afterRestore.backupApiKey)

        // Opt-in includeApiKeys = true must include keys when explicitly requested
        val optInExport = repository.exportDataSnapshot(includeApiKeys = true)
        assertTrue(optInExport.contains("AIza-SECRET-GEMINI"))
        assertTrue(optInExport.contains("gsk_SECRET_GROQ"))
    }

    // =========================================================================
    // TEST 6: Atomik Dahili Otomatik Yedekleme (No .tmp Leftover, No External Copy, Sanitized)
    // =========================================================================
    @Test
    fun test6_atomicAutoBackup_writesInternalOnlyStripsKeysAndCleansLegacyExternal() = runBlocking {
        db.botDao().insertOrUpdate(createSampleBot(id = "bot_atomic_1"))
        db.userSettingsDao().insertOrUpdate(UserSettingsEntity(openaiApiKey = "sk-SHOULD-NOT-LEAK"))

        // Plant a legacy external backup file to verify cleanup
        val extDir = context.getExternalFilesDir(null)
        val legacyExtFile = extDir?.let { File(it, "emochi_autobackup.json") }
        legacyExtFile?.writeText("""{"legacy":true}""")

        repository.forceBackupNow()

        val internalBackup = File(context.filesDir, "emochi_autobackup.json")
        val tempFile = File(context.filesDir, "emochi_autobackup.json.tmp")

        assertTrue("Internal auto-backup file must exist", internalBackup.exists() && internalBackup.length() > 0L)
        assertFalse("Temp .tmp file must be atomically replaced/removed", tempFile.exists())
        if (legacyExtFile != null) {
            assertFalse("Legacy external auto-backup must be removed for security", legacyExtFile.exists())
        }

        val jsonContent = internalBackup.readText(Charsets.UTF_8)
        assertFalse("Auto-backup must NEVER contain plaintext API keys", jsonContent.contains("sk-SHOULD-NOT-LEAK"))
        val parsed = JSONObject(jsonContent)
        assertEquals(2, parsed.getInt("version"))
        assertEquals(1, parsed.getJSONArray("bots").length())
    }

    // =========================================================================
    // TEST 7: ChatExporter .txt / .md / .json Çıktıları ve DateRange Filtreleme
    // =========================================================================
    @Test
    fun test7_chatExporter_formatsAndDateRangeFiltering() {
        val bot = createSampleBot(id = "bot_exp_1")
        val now = 1_750_000_000_000L
        val dayMs = 24L * 60L * 60L * 1000L

        val msgs = listOf(
            MessageEntity(id = "e_old", botId = bot.id, role = "user", text = "45 gün önceki mesaj", timestamp = now - 45L * dayMs),
            MessageEntity(id = "e_month", botId = bot.id, role = "assistant", text = "20 gün önceki yanıt", timestamp = now - 20L * dayMs),
            MessageEntity(id = "e_week", botId = bot.id, role = "user", text = "3 gün önceki mesaj", timestamp = now - 3L * dayMs),
            MessageEntity(id = "e_day", botId = bot.id, role = "assistant", text = "2 saat önceki yanıt [[STATE_JSON {}]]", timestamp = now - 2L * 3600L * 1000L)
        )

        assertEquals(4, ChatExporter.filterByRange(msgs, ChatExporter.DateRange.ALL, now).size)
        assertEquals(3, ChatExporter.filterByRange(msgs, ChatExporter.DateRange.LAST_30_DAYS, now).size)
        assertEquals(2, ChatExporter.filterByRange(msgs, ChatExporter.DateRange.LAST_7_DAYS, now).size)
        assertEquals(1, ChatExporter.filterByRange(msgs, ChatExporter.DateRange.LAST_24_HOURS, now).size)

        val txtOut = ChatExporter.buildExportContent(bot, msgs, ChatExporter.ExportFormat.TEXT, ChatExporter.DateRange.LAST_7_DAYS, now)
        assertTrue(txtOut.contains("Aiden"))
        assertTrue(txtOut.contains("3 gün önceki mesaj"))
        assertFalse("Old 45d message must be excluded from LAST_7_DAYS", txtOut.contains("45 gün önceki"))
        assertFalse("Internal STATE_JSON must be stripped from exported text", txtOut.contains("STATE_JSON"))

        val mdOut = ChatExporter.buildExportContent(bot, msgs, ChatExporter.ExportFormat.MARKDOWN, ChatExporter.DateRange.LAST_24_HOURS, now)
        assertTrue("Markdown must include H1 header", mdOut.startsWith("# Aiden"))
        assertTrue("Markdown must include quote block", mdOut.contains("> 2 saat önceki yanıt"))
        assertTrue("Markdown must include horizontal separator", mdOut.contains("---"))

        val jsonOut = ChatExporter.buildExportContent(bot, msgs, ChatExporter.ExportFormat.JSON, ChatExporter.DateRange.LAST_30_DAYS, now)
        val parsedJson = JSONObject(jsonOut)
        assertEquals(2, parsedJson.getInt("version"))
        assertEquals("Aiden", parsedJson.getString("botName"))
        assertEquals(3, parsedJson.getJSONArray("messages").length())
    }
}
