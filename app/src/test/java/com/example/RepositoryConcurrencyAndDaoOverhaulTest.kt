package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AffectionEventEntity
import com.example.data.local.AppDatabase
import com.example.data.local.ArchivedMemoryEntity
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionHistoryEntity
import com.example.data.local.EmotionState
import com.example.data.local.EntityRegistryEntity
import com.example.data.local.EntityRelationEntity
import com.example.data.local.MemoryCheckpointEntity
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MemoryFactEntity
import com.example.data.local.MemoryFragmentEntity
import com.example.data.local.MessageEntity
import com.example.data.local.MilestoneEntity
import com.example.data.local.PendingReappraisalEntity
import com.example.data.local.PersonalityProfile
import com.example.data.local.PrimaryEmotions
import com.example.data.local.RelationshipAxes
import com.example.data.local.RoutineEntity
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.local.StoryProgressEntity
import com.example.data.local.TurnRollbackSnapshotEntity
import com.example.data.repository.BaselineEmotionEngine
import com.example.data.repository.EmochiRepository
import com.example.util.AppFeatureFlags
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RepositoryConcurrencyAndDaoOverhaulTest {

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

    private suspend fun seedFullBotGraph(botId: String, baselineJson: String = ""): BotEntity {
        val bot = BotEntity(
            id = botId,
            mode = "personal",
            aiName = "Natasha",
            aiPersonality = "Soğukkanlı stratejist",
            scenario = "İstanbul operasyonu",
            universeName = "",
            keyCharactersJson = "[]",
            userCharName = "Aiden",
            userCharDesc = "Saha ortağı",
            openingMessage = "Hazır mısın?",
            writingStyle = "rp",
            intensity = "normal",
            storyDayCounter = 9L,
            initialAge = 24,
            currentAge = 26,
            storyCalendarDate = "2027-01-15",
            hasExplicitStoryDate = false,
            baselineEmotionAnalysisJson = baselineJson
        )
        db.botDao().insertOrUpdate(bot)
        db.messageDao().insertMessage(MessageEntity("m1_$botId", botId, "assistant", "Başlıyoruz.", 1000L))
        db.memoryFactDao().insertFact(MemoryFactEntity(botId = botId, subject = "Aiden", key = "silah", value = "Bıçak"))
        db.memoryEventDao().insertEvent(MemoryEventEntity(botId = botId, timestamp = 1000L, description = "Çatıdaki yemin", importanceScore = 80))
        db.memoryCheckpointDao().insertCheckpoint(MemoryCheckpointEntity(botId = botId, checkpointNumber = 1, messageRangeStart = 1, messageRangeEnd = 50, summaryText = "Özet", timestamp = 1000L))
        db.castMemberDao().insertCastMember(CastMemberEntity(botId = botId, name = "Tony Stark", description = "Müttefik", role = "Müttefik", affectionScore = 88, relationshipState = "Yakın Dost", isAutoAdded = false))
        db.castMemberDao().insertCastMember(CastMemberEntity(botId = botId, name = "Elias Voss", description = "Dinamik düşman", role = "Rakip", affectionScore = 15, relationshipState = "Düşman", isAutoAdded = true))
        db.characterEmotionDao().insertOrUpdate(CharacterEmotionEntity(botId = botId, characterName = "Tony Stark", emotionState = "{}"))
        db.sensitiveTriggerDao().insertOrUpdate(SensitiveTriggerEntity(botId = botId, triggerTopic = "İhanet", sourceDescription = "Geçmiş", recentTriggerCount = 3))
        db.routineDao().insertRoutine(RoutineEntity(botId = botId, routineDescription = "Sabah koşusu", frequency = "daily"))
        db.entityRelationDao().insertRelation(EntityRelationEntity(botId = botId, sourceEntityId = 1L, sourceEntityName = "Natasha", relationType = "müttefik", targetEntityId = 2L, targetEntityName = "Aiden"))
        db.entityRegistryDao().insertOrUpdateEntity(EntityRegistryEntity(botId = botId, entityName = "Yerebatan", entityType = "location", description = "Sarnıç", firstMentionedAt = 1000L, lastMentionedAt = 1000L))
        db.milestoneDao().insertMilestone(MilestoneEntity(id = "ms_$botId", botId = botId, type = "bond", title = "İlk Güven", emoji = "✨", affectionAtTime = 75, trustAtTime = 70))
        db.pendingReappraisalDao().insert(PendingReappraisalEntity(botId = botId, originalEventDescription = "Tartışma", originalEmotionState = "{}", triggeredAtMessageIndex = 5, triggeredAtTimestamp = 1000L, reappraisalEligibleAtMessageIndex = 10, reappraisalEligibleAtTimestamp = 2000L))
        db.emotionHistoryDao().insertHistory(EmotionHistoryEntity(botId = botId, timestamp = 1000L, messageIndex = 1, dominantEmotion = "temkinli", fullVectorJson = "{}"))
        db.affectionEventDao().insertEvent(AffectionEventEntity(botId = botId, timestamp = 1000L, scoreDelta = 5, shortDescription = "Yardım etti"))
        db.storyProgressDao().insertOrUpdate(StoryProgressEntity(botId = botId, chapterNumber = 3))
        db.memoryFragmentDao().insertFragment(MemoryFragmentEntity(botId = botId, content = "Gizli not", category = "fact", createdAt = 1000L))
        db.turnRollbackSnapshotDao().upsert(TurnRollbackSnapshotEntity(messageId = "m1_$botId", botId = botId))
        db.archivedMemoryDao().insertArchive(ArchivedMemoryEntity(botId = botId, originalMemoryId = 1L, memoryType = "fact", content = "Arşiv", archivedReason = "test", timestamp = 1000L))
        return bot
    }

    @Test
    fun test1_deleteBot_purgesAll19BotScopedTablesWithZeroOrphans() = runBlocking {
        val botId = "bot_delete_all_tables"
        seedFullBotGraph(botId)

        repo.deleteBot(botId)

        assertNull(db.botDao().getBotById(botId))
        assertTrue(db.messageDao().getMessagesForBotList(botId).isEmpty())
        assertTrue(db.memoryFactDao().getActiveFacts(botId).isEmpty())
        assertTrue(db.memoryEventDao().getActiveEvents(botId).isEmpty())
        assertTrue(db.memoryCheckpointDao().getCheckpointsForBot(botId).isEmpty())
        assertTrue(db.castMemberDao().getCastMembersForBot(botId).isEmpty())
        assertTrue(db.characterEmotionDao().getEmotionsForBot(botId).isEmpty())
        assertTrue(db.sensitiveTriggerDao().getTriggersForBot(botId).isEmpty())
        assertTrue(db.routineDao().getRoutinesForBot(botId).isEmpty())
        assertTrue(db.entityRelationDao().getRelationsForBot(botId).isEmpty())
        assertTrue(db.entityRegistryDao().getEntitiesForBot(botId).isEmpty())
        assertTrue(db.milestoneDao().getMilestonesForBot(botId).isEmpty())
        assertTrue(db.pendingReappraisalDao().getAllForBot(botId).isEmpty())
        assertTrue(db.emotionHistoryDao().getAllHistoryForBot(botId).isEmpty())
        assertTrue(db.affectionEventDao().getEventsForBotList(botId).isEmpty())
        assertNull(db.storyProgressDao().getProgressForBot(botId))
        assertTrue(db.memoryFragmentDao().getRecentFragments(botId, 20).isEmpty())
        assertTrue(db.turnRollbackSnapshotDao().getSnapshotsForBot(botId).isEmpty())
        assertTrue(db.archivedMemoryDao().getArchivesForBot(botId).isEmpty())
    }

    @Test
    fun test2_resetBotMemoryAndEmotion_clearsLearnedMemoryPreservesManualCastAndRestoresStoredBaseline() = runBlocking {
        val customBaseline = EmotionState(
            primaryEmotions = PrimaryEmotions(trust = 26, anger = 25),
            relationshipAxes = RelationshipAxes(affectionScore = 24, resentmentScore = 64),
            dominantEmotion = "temkinli"
        )
        val storedAnalysis = BaselineEmotionEngine.BaselineAnalysisResult(
            relationshipType = "rival",
            relationshipNote = "Test",
            personalityProfile = PersonalityProfile.deriveFromPersonality("Natasha", "Soğukkanlı", "İstanbul"),
            regulationCapacity = 70,
            difficultyMultiplier = 1.2,
            baselineEmotionState = customBaseline,
            sensitiveTriggers = emptyList(),
            speechPattern = "Net",
            confidence = 90,
            analysisSource = "llm"
        )

        val botId = "bot_full_reset_test"
        seedFullBotGraph(botId, baselineJson = storedAnalysis.toCanonicalJson())

        repo.resetBotMemoryAndEmotion(botId)

        // All learned memory/story tables wiped
        assertTrue(db.memoryFactDao().getActiveFacts(botId).isEmpty())
        assertTrue(db.memoryEventDao().getActiveEvents(botId).isEmpty())
        assertTrue(db.memoryCheckpointDao().getCheckpointsForBot(botId).isEmpty())
        assertTrue(db.entityRelationDao().getRelationsForBot(botId).isEmpty())
        assertTrue(db.entityRegistryDao().getEntitiesForBot(botId).isEmpty())
        assertTrue(db.milestoneDao().getMilestonesForBot(botId).isEmpty())
        assertTrue(db.emotionHistoryDao().getAllHistoryForBot(botId).isEmpty())
        assertTrue(db.affectionEventDao().getEventsForBotList(botId).isEmpty())
        assertTrue(db.archivedMemoryDao().getArchivesForBot(botId).isEmpty())

        // Auto-added cast removed, manual cast preserved with reset affection/relationship
        val cast = db.castMemberDao().getCastMembersForBot(botId)
        assertEquals(1, cast.size)
        assertEquals("Tony Stark", cast.first().name)
        assertEquals(50, cast.first().affectionScore)
        assertEquals("Tanıdık", cast.first().relationshipState)

        // Authored trigger preserved with recentTriggerCount reset to 0
        val triggers = db.sensitiveTriggerDao().getTriggersForBot(botId)
        assertEquals(1, triggers.size)
        assertEquals(0, triggers.first().recentTriggerCount)

        // Bot baseline, storyDayCounter, currentAge & implicit date cleanly reset
        val resetBot = db.botDao().getBotById(botId)
        assertNotNull(resetBot)
        assertEquals(1L, resetBot!!.storyDayCounter)
        assertEquals(24, resetBot.currentAge)
        assertEquals("", resetBot.storyCalendarDate)
        val restoredEmo = EmotionState.fromJson(resetBot.emotionState)
        assertEquals(24, restoredEmo.relationshipAxes.affectionScore)
        assertEquals(64, restoredEmo.relationshipAxes.resentmentScore)
    }

    @Test
    fun test3_processSaveMemory_nearDuplicateEventUpgradesImportanceInPlaceOnSameId() = runBlocking {
        val botId = "bot_near_dup_upgrade"
        val originalText = "Kullanıcı İstanbul Kadıköy Moda mahallesinde bir apartmanda yaşıyor"
        val nearDupText = "Kullanıcı İstanbul Kadıköy Moda mahallesinde bir apartmanda yaşıyor."
        val vec = repo.computeEmbedding(originalText)

        val originalId = db.memoryEventDao().insertEvent(
            MemoryEventEntity(
                botId = botId,
                timestamp = 1000L,
                description = originalText,
                importanceScore = 60,
                embedding = repo.floatArrayToJson(vec),
                isBotSaved = true
            )
        )

        val saved = repo.processSaveMemory(
            botId = botId,
            content = nearDupText,
            category = "event",
            importance = 9
        )
        assertTrue("Higher-importance near-duplicate merge must return true", saved)
        val active = db.memoryEventDao().getActiveEvents(botId)
        assertEquals(1, active.size)
        assertEquals(originalId, active.first().id)
        assertEquals(90, active.first().importanceScore)
    }
}
