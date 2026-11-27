package com.example

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionState
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PrimaryEmotions
import com.example.data.local.RelationshipAxes
import com.example.data.repository.EmochiRepository
import com.example.ui.viewmodel.EmochiViewModel
import com.example.util.AppFeatureFlags
import com.example.work.BackgroundMemoryMergeWorker
import com.example.work.ProactiveMessageWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WorkersViewModelAndDatabaseHygieneOverhaulTest {

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
        repo.close()
        db.close()
    }

    @Test
    fun proactiveWorker_sanitizesPreviewGeneratesNonCollidingNotificationIdsAndEnforcesQuietHours() {
        val raw = "<think>internal thought</think>*Gülümseyerek elini uzattı.*   \"Seni çok özledim!\" [[STATE_JSON {\"dom\":\"mutlu\"}]]"
        val preview = ProactiveMessageWorker.sanitizeNotificationPreview(raw)
        assertFalse(preview.contains("<think>"))
        assertFalse(preview.contains("STATE_JSON"))
        assertFalse(preview.contains("*"))
        assertTrue(preview.contains("Seni çok özledim!"))

        val id1 = ProactiveMessageWorker.notificationIdForBot("bot_alpha")
        val id2 = ProactiveMessageWorker.notificationIdForBot("bot_beta")
        assertTrue("Notification ID must stay positive in dedicated >= 200_000 range", id1 >= 200_000)
        assertTrue(id2 >= 200_000)
        assertTrue(id1 != id2)

        assertTrue(ProactiveMessageWorker.isQuietHours(23))
        assertTrue(ProactiveMessageWorker.isQuietHours(3))
        assertFalse(ProactiveMessageWorker.isQuietHours(14))

        val now = 10_000_000_000L
        val msgs = listOf(
            MessageEntity(id = "m1", botId = "b1", role = "user", text = "Selam", timestamp = now - ProactiveMessageWorker.IDLE_THRESHOLD_MS - 60_000L)
        )
        assertTrue(
            ProactiveMessageWorker.shouldSendProactiveMessage(
                botId = "b1",
                proactiveEnabled = true,
                activeBotId = null,
                botLastMessageTimestamp = 0L,
                recentMessages = msgs,
                nowMs = now,
                currentHourOfDay = 15
            )
        )
        assertFalse(
            "Active open chat must never receive proactive notifications",
            ProactiveMessageWorker.shouldSendProactiveMessage(
                botId = "b1",
                proactiveEnabled = true,
                activeBotId = "b1",
                botLastMessageTimestamp = 0L,
                recentMessages = msgs,
                nowMs = now,
                currentHourOfDay = 15
            )
        )
    }

    @Test
    fun memoryMergeWorker_atomicallyMergesPlainDuplicateEventsWhileProtectingCoreAndLivingMemories() = runBlocking {
        val botId = "bot_merge_atomic"
        db.botDao().insertOrUpdate(
            BotEntity(
                id = botId,
                mode = "roleplay",
                aiName = "Aiden",
                aiPersonality = "Sakin",
                scenario = "Kule",
                universeName = "Kozmik",
                keyCharactersJson = "[]",
                userCharName = "Sen",
                userCharDesc = "Yolcu",
                openingMessage = "Merhaba.",
                writingStyle = "novel",
                intensity = "medium"
            )
        )
        val e1 = MemoryEventEntity(
            botId = botId,
            description = "Kuledeki gizli kapıyı gece yarısı birlikte açtık ve haritayı bulduk.",
            importanceScore = 60,
            storyDayIndex = 2L
        )
        val e2 = MemoryEventEntity(
            botId = botId,
            description = "Kuledeki gizli kapıyı gece yarısı birlikte açtık ve haritayı bulduk.",
            importanceScore = 65,
            storyDayIndex = 3L
        )
        val coreProtected = MemoryEventEntity(
            botId = botId,
            description = "[CORE] Aiden asla ihanet etmez.",
            importanceScore = 95,
            storyDayIndex = 1L
        )
        db.memoryEventDao().insertEvent(e1)
        db.memoryEventDao().insertEvent(e2)
        db.memoryEventDao().insertEvent(coreProtected)

        assertTrue(BackgroundMemoryMergeWorker.isProtectedEvent(coreProtected))

        val mutated = BackgroundMemoryMergeWorker.runMaintenanceForBot(
            botId = botId,
            repository = repo,
            db = db,
            customMerger = { "Gece yarısı kuledeki gizli kapıyı açıp haritayı birlikte buldular." }
        )
        assertTrue(mutated)
        val activeAfter = db.memoryEventDao().getActiveEvents(botId)
        assertTrue("Protected CORE event must remain untouched", activeAfter.any { it.description.contains("asla ihanet etmez") })
        assertTrue("Merged event must be persisted", activeAfter.any { it.description.contains("haritayı birlikte buldular") })
        assertEquals(2, db.archivedMemoryDao().getArchivesForBot(botId).size)
    }

    @Test
    fun viewModel_updateCharacterEmotionPreservesExistingFullV3EmotionFields() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = EmochiViewModel(app)
        delay(120)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val botId = "bot_vm_char_emo"
        val charName = "Natasha"
        val initialFullState = EmotionState(
            primaryEmotions = PrimaryEmotions(joy = 72, trust = 40, fear = 15),
            relationshipAxes = RelationshipAxes(affectionScore = 50, respectScore = 88, comfortScore = 77, resentmentScore = 12),
            dominantEmotion = "sakin",
            speechPattern = "soğukkanlı ve keskin",
            resilience = 9
        )
        vm.repository.insertOrUpdateCharacterEmotion(
            CharacterEmotionEntity(
                botId = botId,
                characterName = charName,
                emotionState = initialFullState.toJson()
            )
        )

        vm.updateCharacterEmotion(
            botId = botId,
            characterName = charName,
            newMood = "angry",
            affection = 65,
            trust = 70,
            tension = 80
        )
        repeat(10) {
            delay(40)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }

        val saved = vm.repository.getEmotionForCharacter(botId, charName)
        val restored = EmotionState.fromJson(saved?.emotionState)
        assertEquals("öfkeli", restored.dominantEmotion)
        assertEquals(65, restored.relationshipAxes.affectionScore)
        assertEquals(70, restored.primaryEmotions.trust)
        assertEquals("crisis", restored.tension)
        // Crucially, all pre-existing v3 fields MUST be preserved instead of wiped!
        assertEquals(88, restored.relationshipAxes.respectScore)
        assertEquals(77, restored.relationshipAxes.comfortScore)
        assertEquals(72, restored.primaryEmotions.joy)
        assertEquals("soğukkanlı ve keskin", restored.speechPattern)
        assertEquals(9, restored.resilience)
    }
}
