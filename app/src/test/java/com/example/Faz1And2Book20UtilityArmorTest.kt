package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.book.BookStateRepository
import com.example.book.CosmicDriftBookRegistry
import com.example.book.StoryFlagAxis
import com.example.data.local.AppDatabase
import com.example.data.local.CustomProviderEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.managers.ProviderFallbackManager
import com.example.data.managers.ProviderHealthTracker
import com.example.ui.screens.cosmic3d.CosmicDuelBossId
import com.example.ui.screens.cosmic3d.CosmicDuelDifficulty
import com.example.ui.screens.cosmic3d.StoryMiniGameId
import com.example.ui.screens.cosmic3d.StoryMiniGameScorer
import com.example.util.AppFeatureFlags
import com.sun.net.httpserver.HttpServer
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
import java.net.InetSocketAddress

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Faz1And2Book20UtilityArmorTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        ProviderHealthTracker.resetAll()
        BookStateRepository.resetEntireBook(context, CosmicDriftBookRegistry.BOOK_ID)
    }

    @After
    fun tearDown() {
        ProviderHealthTracker.resetAll()
    }

    @Test
    fun utilityModel404Mismatch_doesNotPoisonHealth_clearsAutoCache_andRetriesMainModel() = runBlocking {
        val attemptedModels = mutableListOf<String>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/v1/chat/completions") { exchange ->
            val reqText = exchange.requestBody.bufferedReader().use { it.readText() }
            val model = runCatching { JSONObject(reqText).optString("model") }.getOrDefault("")
            attemptedModels.add(model)
            if (model == "stale/nonexistent-mini") {
                val errBody = """{"error":{"code":"model_not_found","message":"The model 'stale/nonexistent-mini' does not exist"}}"""
                    .toByteArray(Charsets.UTF_8)
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(404, errBody.size.toLong())
                exchange.responseBody.use { it.write(errBody) }
            } else {
                val okBody = """{"choices":[{"message":{"content":"{\"ok\":true}"}}]}"""
                    .toByteArray(Charsets.UTF_8)
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, okBody.size.toLong())
                exchange.responseBody.use { it.write(okBody) }
            }
        }
        server.start()

        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val baseUrl = "http://127.0.0.1:${server.address.port}/v1"
            val insertedId = db.customProviderDao().insertProvider(
                CustomProviderEntity(
                    label = "Armor Test Proxy",
                    baseUrl = baseUrl,
                    apiKeyEncrypted = "",
                    modelName = "deepseek/deepseek-v3.2-special",
                    utilityModel = "",
                    apiFormat = "openai"
                )
            )
            val providerKey = "custom_$insertedId"
            AppFeatureFlags.setAiUtilityModel(providerKey, "stale/nonexistent-mini", source = "ai_detected")

            val manager = ProviderFallbackManager(
                customProviderDao = db.customProviderDao(),
                onLogFallback = {}
            )

            val settings = UserSettingsEntity(
                selectedProvider = providerKey,
                selectedModel = "gemini-2.0-flash",
                enableAutoFallback = false,
                enablePollinations = false,
                enableLlm7 = false,
                enableOvh = false
            )

            val result = manager.executeUtilityRequest(
                settings = settings,
                systemPrompt = "Extract JSON",
                userMessage = "Test input",
                buildConfigGeminiKey = ""
            )

            assertTrue("Utility request should succeed after 1x main-model recovery retry", result.text.contains("ok"))
            assertEquals(
                "Must try cached utility model first, then retry customEntity.modelName (never gemini-2.0-flash)",
                listOf("stale/nonexistent-mini", "deepseek/deepseek-v3.2-special"),
                attemptedModels
            )
            assertTrue(
                "ProviderHealthTracker MUST NOT put provider into cooldown on utility 404 model_not_found",
                ProviderHealthTracker.isAvailable(providerKey)
            )
            assertEquals(
                "Stale non-manual utility model cache should be cleared after 404 mismatch",
                "",
                AppFeatureFlags.getAiUtilityModel(providerKey)
            )
        } finally {
            server.stop(0)
            db.close()
        }
    }

    @Test
    fun utilityModelMismatch_preservesManualUtilityPreference() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/v1/chat/completions") { exchange ->
            val reqText = exchange.requestBody.bufferedReader().use { it.readText() }
            val model = runCatching { JSONObject(reqText).optString("model") }.getOrDefault("")
            if (model == "user/manual-mini") {
                val errBody = """{"error":{"code":"model_not_found","message":"model not found"}}"""
                    .toByteArray(Charsets.UTF_8)
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(404, errBody.size.toLong())
                exchange.responseBody.use { it.write(errBody) }
            } else {
                val okBody = """{"choices":[{"message":{"content":"ok"}}]}""".toByteArray(Charsets.UTF_8)
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, okBody.size.toLong())
                exchange.responseBody.use { it.write(okBody) }
            }
        }
        server.start()

        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val baseUrl = "http://127.0.0.1:${server.address.port}/v1"
            val insertedId = db.customProviderDao().insertProvider(
                CustomProviderEntity(
                    label = "Manual Proxy",
                    baseUrl = baseUrl,
                    apiKeyEncrypted = "",
                    modelName = "main-rp-model",
                    utilityModel = "",
                    apiFormat = "openai"
                )
            )
            val providerKey = "custom_$insertedId"
            AppFeatureFlags.setAiUtilityModel(providerKey, "user/manual-mini", source = "manual")

            val manager = ProviderFallbackManager(
                customProviderDao = db.customProviderDao(),
                onLogFallback = {}
            )

            val settings = UserSettingsEntity(
                selectedProvider = providerKey,
                selectedModel = "gemini-2.0-flash",
                enableAutoFallback = false,
                enablePollinations = false,
                enableLlm7 = false,
                enableOvh = false
            )

            val result = manager.executeUtilityRequest(settings, "sys", "usr", "")
            assertEquals("ok", result.text)
            assertEquals(
                "Manual utility model choice must NOT be wiped automatically",
                "user/manual-mini",
                AppFeatureFlags.getAiUtilityModel(providerKey)
            )
        } finally {
            server.stop(0)
            db.close()
        }
    }

    @Test
    fun storyMiniGameScorer_andDifficultyPresets_areDeterministic() {
        assertEquals("S", StoryMiniGameScorer.computeRank(90, 100))
        assertEquals("A", StoryMiniGameScorer.computeRank(65, 100))
        assertEquals("B", StoryMiniGameScorer.computeRank(40, 100))
        assertEquals(StoryMiniGameId.WHITE_EYE_RESONANCE, StoryMiniGameId.fromTag("white_eye_resonance"))
        assertEquals(StoryMiniGameId.BOSPHORUS_CORE_BALANCE, StoryMiniGameId.fromTag("BOSPHORUS_CORE_BALANCE"))
        assertEquals(StoryMiniGameId.RED_EYE_WILLPOWER, StoryMiniGameId.fromTag("RED_EYE_WILLPOWER"))

        assertTrue(CosmicDuelDifficulty.STORY.enemyDamageScale < CosmicDuelDifficulty.BALANCED.enemyDamageScale)
        assertTrue(CosmicDuelDifficulty.COSMIC.enemyDamageScale > CosmicDuelDifficulty.BALANCED.enemyDamageScale)
        assertTrue(CosmicDuelDifficulty.STORY.perfectParrySec > CosmicDuelDifficulty.COSMIC.perfectParrySec)
    }

    @Test
    fun bookStateRepository_persistsOfflineMiniGameRanksAndUpdatesStoryFlagsWithoutDowngrade() {
        val afterB = BookStateRepository.recordMiniGameOutcome(
            context = context,
            gameKey = StoryMiniGameId.WHITE_EYE_RESONANCE.key,
            rank = "B",
            score = 420
        )
        assertEquals("B", BookStateRepository.getSavedMiniGameRank(context, StoryMiniGameId.WHITE_EYE_RESONANCE.key))
        assertEquals(1, afterB.numericFlags["story:${StoryFlagAxis.WHITE_EYE_CONTROL.key}"])

        val afterS = BookStateRepository.recordMiniGameOutcome(
            context = context,
            gameKey = StoryMiniGameId.WHITE_EYE_RESONANCE.key,
            rank = "S",
            score = 980
        )
        assertEquals("S", BookStateRepository.getSavedMiniGameRank(context, StoryMiniGameId.WHITE_EYE_RESONANCE.key))
        assertEquals(3, afterS.numericFlags["story:${StoryFlagAxis.WHITE_EYE_CONTROL.key}"])

        val afterReplayB = BookStateRepository.recordMiniGameOutcome(
            context = context,
            gameKey = StoryMiniGameId.WHITE_EYE_RESONANCE.key,
            rank = "B",
            score = 310
        )
        assertEquals("S", BookStateRepository.getSavedMiniGameRank(context, StoryMiniGameId.WHITE_EYE_RESONANCE.key))
        assertEquals(3, afterReplayB.numericFlags["story:${StoryFlagAxis.WHITE_EYE_CONTROL.key}"])

        assertEquals("A", BookStateRepository.recordBossMedal(context, CosmicDuelBossId.VAELOR_ROOFTOP.name, "A", 1400))
        assertEquals("A", BookStateRepository.recordBossMedal(context, CosmicDuelBossId.VAELOR_ROOFTOP.name, "B", 900))
        assertEquals("S", BookStateRepository.recordBossMedal(context, CosmicDuelBossId.VAELOR_ROOFTOP.name, "S", 2100))
        assertEquals("S", BookStateRepository.getAllBossMedals(context)[CosmicDuelBossId.VAELOR_ROOFTOP.name])

        BookStateRepository.resetEntireBook(context, CosmicDriftBookRegistry.BOOK_ID)
        assertNull(BookStateRepository.getSavedMiniGameRank(context, StoryMiniGameId.WHITE_EYE_RESONANCE.key))
        assertTrue(BookStateRepository.getAllBossMedals(context).isEmpty())
    }

    @Test
    fun cosmicDriftChapters_embedAllThreeOfflineMiniGameTagsCleanly() {
        val ch2Node = CosmicDriftBookRegistry.node("ch2_s2_white_eye")
        val ch7Node = CosmicDriftBookRegistry.node("ch7_s2_tunnels")
        val ch9Node = CosmicDriftBookRegistry.node("ch9_s2_clash")

        assertNotNull(ch2Node)
        assertNotNull(ch7Node)
        assertNotNull(ch9Node)

        assertTrue(
            "Chapter 2 Scene 2 must embed [[MINIGAME|WHITE_EYE_RESONANCE|...]]",
            ch2Node!!.paragraphs.any { it.startsWith("[[MINIGAME|WHITE_EYE_RESONANCE|") && it.endsWith("]]") }
        )
        assertTrue(
            "Chapter 7 Scene 2 must embed [[MINIGAME|BOSPHORUS_CORE_BALANCE|...]]",
            ch7Node!!.paragraphs.any { it.startsWith("[[MINIGAME|BOSPHORUS_CORE_BALANCE|") && it.endsWith("]]") }
        )
        assertTrue(
            "Chapter 9 Scene 2 must embed [[MINIGAME|RED_EYE_WILLPOWER|...]]",
            ch9Node!!.paragraphs.any { it.startsWith("[[MINIGAME|RED_EYE_WILLPOWER|") && it.endsWith("]]") }
        )
        assertFalse(
            "All embedded mini-games must be distinct",
            StoryMiniGameId.entries.map { it.key }.distinct().size < 3
        )
    }
}
