package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CustomEmotionDefinition
import com.example.data.local.EmotionState
import com.example.data.local.MemoryCheckpointEntity
import com.example.data.local.MemoryFragmentEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PersonalityProfile
import com.example.data.local.SideCharacterEmotionUpdate
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.EmochiRepository
import com.example.data.repository.SystemPromptBuilder
import com.example.util.AppFeatureFlags
import kotlinx.coroutines.runBlocking
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
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EmotionModelsSummarizationAndPromptOverhaulTest {

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

    private fun makeBot(id: String = "bot_s1", customLength: String = "default") = BotEntity(
        id = id,
        mode = "roleplay",
        aiName = "Aiden",
        aiPersonality = "Soğukkanlı, gözlemci.",
        scenario = "Gece yarısı kulede nöbet.",
        universeName = "Kozmik",
        keyCharactersJson = "[]",
        userCharName = "Sen",
        userCharDesc = "Yolcu",
        openingMessage = "Merhaba.",
        writingStyle = "novel",
        intensity = "medium",
        customLength = customLength
    )

    @Test
    fun emotionState_jsonRoundTripNeverLeaksLiteralNullStringAndClampsRanges() {
        val original = EmotionState(
            dominantEmotion = "sakin",
            suppressedEmotion = "",
            computedSecondaryEmotion = null
        )
        val json = original.toJson()
        val restored = EmotionState.fromJson(json)
        assertEquals("", restored.suppressedEmotion)
        assertNull(restored.computedSecondaryEmotion)
        assertEquals("sakin", restored.dominantEmotion)

        // Malformed legacy JSON with explicit nulls and out-of-bounds values
        val dirtyJson = """
            {
              "dominant_emotion": null,
              "suppressed_emotion": null,
              "computed_secondary_emotion": "null",
              "defense_mechanism": null,
              "trust": 999,
              "affection": -50,
              "hurt": 200,
              "resilience": 99,
              "peakAffectionScore": 500
            }
        """.trimIndent()
        val parsed = EmotionState.fromJson(dirtyJson)
        assertEquals("nötr", parsed.dominantEmotion)
        assertEquals("", parsed.suppressedEmotion)
        assertNull(parsed.computedSecondaryEmotion)
        assertEquals("none", parsed.defenseMechanism)
        assertEquals(100, parsed.primaryEmotions.trust)
        assertEquals(0, parsed.relationshipAxes.affectionScore)
        assertEquals(100, parsed.relationshipAxes.resentmentScore)
        assertEquals(10, parsed.resilience)
        assertEquals(100, parsed.peakAffectionScore)

        // Inverted CustomEmotionDefinition min > max must normalize without crash
        val customObj = JSONObject("""{"name":"Sadakat","minValue":80,"maxValue":20,"currentValue":500,"purpose":null}""")
        val custom = CustomEmotionDefinition.fromJsonObject(customObj)
        assertEquals(20, custom.minValue)
        assertEquals(80, custom.maxValue)
        assertEquals(80, custom.currentValue)
        assertEquals("", custom.purpose)

        // SideCharacterEmotionUpdate clamps extreme deltas & normalizes English mood
        val sideObj = JSONObject("""{"character_name":"Voss","mood":"angry","affection_delta":500,"trust_delta":-999,"reason":null}""")
        val side = SideCharacterEmotionUpdate.fromJsonObject(sideObj)
        assertNotNull(side)
        assertEquals("öfkeli", side!!.mood)
        assertEquals(25, side.affectionDelta)
        assertEquals(-25, side.trustDelta)
        assertEquals("", side.reason)

        // PersonalityProfile validates attachment_style under tr-TR
        val prev = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            val prof = PersonalityProfile.fromJson("""{"attachment_style":"ANXIOUS","openness":150}""")
            assertEquals("anxious", prof.attachmentStyle)
            assertEquals(100, prof.openness)
            val invalidProf = PersonalityProfile.fromJson("""{"attachment_style":"null"}""")
            assertEquals("secure", invalidProf.attachmentStyle)
        } finally {
            Locale.setDefault(prev)
        }
    }

    @Test
    fun systemPromptBuilder_stripsHiddenTagsFromAnchorsAndHonorsDetailedAndNovelLengths() {
        val botDetailed = makeBot(customLength = "detailed")
        val botNovel = makeBot(customLength = "novel")
        val starred = MessageEntity(
            id = "st1",
            botId = botDetailed.id,
            role = "assistant",
            text = "<think>secret reasoning leak</think>Kule kapısında fısıldadı. [[STATE_JSON {\"dom\":\"gergin\"}]]",
            isStarred = true
        )
        val cp = MemoryCheckpointEntity(
            id = 1L,
            botId = botDetailed.id,
            checkpointNumber = 1,
            messageRangeStart = 1,
            messageRangeEnd = 50,
            summaryText = "<think>cp leak</think>Sahne #1: Kuleye ulaşıldı."
        )
        val frag = MemoryFragmentEntity(
            id = 1L,
            botId = botDetailed.id,
            category = "scene",
            content = "Gizli geçit açıldı. [[STATE_JSON {\"aff\":2}]]"
        )

        val detailedPrompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = botDetailed,
                settings = UserSettingsEntity(),
                starredAnchorMessages = listOf(starred),
                recentCheckpoints = listOf(cp),
                relevantFragments = listOf(frag)
            )
        )
        assertFalse(detailedPrompt.contains("secret reasoning leak"))
        assertFalse(detailedPrompt.contains("cp leak"))
        assertTrue(detailedPrompt.contains("Kule kapısında fısıldadı."))
        assertTrue(detailedPrompt.contains("3 ila 5 paragraf"))

        val novelPrompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = botNovel,
                settings = UserSettingsEntity()
            )
        )
        assertTrue(novelPrompt.contains("5 ila 7 akıcı paragraf"))
    }

    @Test
    fun repository_checkpointAndEmotionUpdatesStripTagsAndRejectNullStrings() = runBlocking {
        val bot = makeBot(id = "bot_cp_emo")
        db.botDao().insertOrUpdate(bot)

        // Insert 50 messages with <think> & [[STATE_JSON]] to verify checkpoint sanitization
        val msgs = (1..50).map { i ->
            MessageEntity(
                id = "m_$i",
                botId = bot.id,
                role = if (i % 2 == 0) "assistant" else "user",
                text = "<think>internal_$i</think>Sahne adımı $i gerçekleşti. [[STATE_JSON {\"dom\":\"sakin\"}]]",
                timestamp = 1_000L + i
            )
        }
        db.messageDao().insertAllMessages(msgs)

        val cp = repo.checkAndGenerateCheckpoint(bot.id, 50)
        assertNotNull(cp)
        assertFalse("Checkpoint must not leak <think>", cp!!.summaryText.contains("<think>"))
        assertFalse("Checkpoint must not leak STATE_JSON", cp.summaryText.contains("STATE_JSON"))
        assertTrue(cp.summaryText.contains("Sahne adımı"))

        // parseAndApplyEmotionUpdates under tr-TR with JSON null fields & English emotion/tension
        val prev = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            val rawReply = "Gözlerini kıstı. [[STATE_JSON {\"dom\":\"angry\",\"suppressed_emotion\":null,\"tension\":\"HIGH\",\"u_tone\":null,\"delta\":{\"value\":-2,\"reason\":null}}]]"
            repo.parseAndApplyEmotionUpdates(bot.id, rawReply)
        } finally {
            Locale.setDefault(prev)
        }
        val updatedBot = db.botDao().getBotById(bot.id)!!
        val updatedEmo = EmotionState.fromJson(updatedBot.emotionState)
        assertEquals("öfkeli", updatedEmo.dominantEmotion)
        assertEquals("", updatedEmo.suppressedEmotion)
        assertEquals("crisis", updatedEmo.tension)
    }
}
