package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.EmotionState
import com.example.data.local.PersonalityProfile
import com.example.data.local.WorldState
import com.example.data.repository.EmochiRepository
import com.example.data.repository.EmotionEngine
import com.example.data.repository.TimePerceptionEngine
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Systems9_10_11_TenOutOfTenTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: EmochiRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = EmochiRepository(db, context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ── System #9: TimePerceptionEngine ──────────────────────────────────────

    @Test
    fun system9_noFalsePositiveTimeJumpsAndAccurateHalfWeekMath() {
        assertEquals(0, TimePerceptionEngine.parseStoryTimeJump("Üç gün önce tanıştık.").daysToAdvance)
        assertEquals(0, TimePerceptionEngine.parseStoryTimeJump("İki hafta boyunca burada kalacağım.").daysToAdvance)
        assertEquals(0, TimePerceptionEngine.parseStoryTimeJump("Bir ay sürecek bir yolculuk.").daysToAdvance)
        assertEquals(0, TimePerceptionEngine.parseStoryTimeJump("On güzel gün geçirdik.").daysToAdvance)

        assertEquals(3, TimePerceptionEngine.parseStoryTimeJump("Üç gün sonra tekrar buluştular.").daysToAdvance)
        assertEquals(40, TimePerceptionEngine.parseStoryTimeJump("Kırk gün geçti.").daysToAdvance)
        assertEquals(11, TimePerceptionEngine.parseStoryTimeJump("Bir buçuk hafta sonra geri döndü.").daysToAdvance)
        assertEquals(45, TimePerceptionEngine.parseStoryTimeJump("Bir buçuk ay geçti.").daysToAdvance)
        assertEquals(548, TimePerceptionEngine.parseStoryTimeJump("Bir buçuk yıl sonra karşılaştılar.").daysToAdvance)

        // Standalone amount string from story_time_skip JSON still parses cleanly
        assertEquals(3, TimePerceptionEngine.parseStoryTimeJump("3 gün").daysToAdvance)
        assertEquals(11, TimePerceptionEngine.parseStoryTimeJump("bir buçuk hafta").daysToAdvance)

        // Very large day counts do not overflow Int in formatStoryDuration
        val longLabel = TimePerceptionEngine.formatStoryDuration(500_000L, "2026-11-21")
        assertTrue(longLabel.contains("yıl"))
    }

    // ── System #10: Emotion Engine & Models ──────────────────────────────────

    @Test
    fun system10_turkishWordBoundariesSchemaVersionAndUnifiedClamping() {
        // "klasik" / "basit" must NOT false-trigger "asi" (-25 agreeableness)
        val neutral = PersonalityProfile.deriveFromPersonality("Elif", "Klasik müzik seven basit bir insan.", "")
        assertEquals(50, neutral.agreeableness)

        val rebellious = PersonalityProfile.deriveFromPersonality("Elif", "Asi ve soğuk bir karakter.", "")
        assertEquals(25, rebellious.agreeableness)

        // EmotionState.fromJson defaults schemaVersion to 3
        val state = EmotionState.fromJson("""{"dominant_emotion":"sakin"}""")
        assertEquals(3, state.schemaVersion)

        // Unified 11-param clamping matches identically across EmotionEngine, Manager, and Repository
        val dEngine = EmotionEngine.calculateClampedAffectionDelta(prevAffection = 40, rawDelta = 10, totalMsgCount = 12, baseMultiplier = 1.0)
        val dRepo = EmochiRepository.calculateClampedDelta(40, 10, 12, 1.0, 1.0, 1.0, 0, 0, 0)
        val dMgr = repo.managers.emotionEngineManager.calculateClampedAffectionDelta(40, 10, 12)
        assertEquals(dEngine, dRepo)
        assertEquals(dEngine, dMgr)
    }

    // ── System #11: WorldStateManager ────────────────────────────────────────

    @Test
    fun system11_bidirectionalMutualRelationsMultiWordNamesCycleGuardAndDayOfWeek() = runBlocking {
        val wsMgr = repo.managers.worldStateManager
        assertEquals("Cumartesi", wsMgr.getDayOfWeekTurkish("2026-11-21"))
        assertEquals("Pazartesi", wsMgr.getDayOfWeekTurkish("2026-11-23"))

        val botId = "bot_world_10_10"
        db.botDao().insertOrUpdate(
            BotEntity(
                id = botId,
                mode = "universe",
                aiName = "Aiden",
                aiPersonality = "",
                scenario = "",
                universeName = "Asroda",
                keyCharactersJson = "[]",
                userCharName = "Elif",
                userCharDesc = "",
                openingMessage = "",
                writingStyle = "rp",
                intensity = "normal"
            )
        )

        // Bidirectional mutual relation with canonical possessive suffix ("kardeş" -> "kardeşi")
        wsMgr.extractAndSaveEntityRelations(botId, "Ahmet ile Mehmet kardeş.")
        val q1 = wsMgr.detectAndResolveGraphQuery(botId, "Ahmet'in kardeşi kim?")
        val q2 = wsMgr.detectAndResolveGraphQuery(botId, "Mehmet'in kardeşi kim?")
        assertNotNull(q1)
        assertEquals("Mehmet", q1!!.finalTargetName)
        assertNotNull(q2)
        assertEquals("Ahmet", q2!!.finalTargetName)

        // Cycle guard: asking "Ahmet'in kardeşinin kardeşi kim?" must NOT ping-pong back to Ahmet
        val cyclic = wsMgr.detectAndResolveGraphQuery(botId, "Ahmet'in kardeşinin kardeşi kim?")
        assertNotNull(cyclic)
        assertEquals(false, cyclic!!.isComplete)

        // Multi-word & titled names
        wsMgr.extractAndSaveEntityRelations(botId, "Dr. Kerem'in patronu Leyla Aydın'dır.")
        val q3 = wsMgr.detectAndResolveGraphQuery(botId, "Dr. Kerem'in patronu kim?")
        assertNotNull(q3)
        assertEquals("Leyla Aydın", q3!!.finalTargetName)

        // WorldState read/write via botDao
        val updatedWs = WorldState.fromJson("""{"meso":{"current_location":"Kuzey Kulesi"}}""")
        wsMgr.updateWorldState(botId, updatedWs)
        val loadedWs = wsMgr.getWorldState(botId)
        assertEquals("Kuzey Kulesi", loadedWs?.meso?.currentLocation)
    }
}
