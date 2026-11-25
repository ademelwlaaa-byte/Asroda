package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.ModelResponseResult
import com.example.data.local.AppDatabase
import com.example.data.local.CustomProviderEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.managers.ProviderFallbackManager
import com.example.data.managers.ProviderHealthTracker
import com.example.data.repository.EmochiRepository
import com.example.ui.screens.cosmic3d.CosmicCharacterVoiceEngine
import com.example.util.AppFeatureFlags
import com.example.util.ChatTtsManager
import com.example.util.EdgeNeuralTtsClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class V32SystemsAuditFixesTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        EdgeNeuralTtsClient.synthesizerOverride = null
    }

    @After
    fun tearDown() {
        EdgeNeuralTtsClient.synthesizerOverride = null
    }

    @Test
    fun previewVoice_doesNotOverwriteSavedNeuralVoicePreference() {
        val originalVoice = "tr-TR-EmelNeural"
        AppFeatureFlags.setTtsNeuralVoice(originalVoice)
        val ttsManager = ChatTtsManager(context)
        ttsManager.playNeuralFileOverride = { true }
        EdgeNeuralTtsClient.synthesizerOverride = { _, _ -> ByteArray(512) { 0x42 } }

        try {
            ttsManager.previewVoice(
                voiceName = "",
                neuralVoiceId = "tr-TR-AhmetNeural"
            )
            assertEquals(
                "Previewing another voice must NOT silently overwrite user's saved neural voice",
                originalVoice,
                AppFeatureFlags.getTtsNeuralVoice()
            )
        } finally {
            ttsManager.shutdown()
        }
    }

    @Test
    fun edgeNeuralTtsClient_concurrentWritesToSameKeyDoNotCorruptCache() = runBlocking {
        val payload = ByteArray(1024) { idx -> (idx % 251).toByte() }
        EdgeNeuralTtsClient.synthesizerOverride = { _, _ -> payload }

        val uniqueSsml = "<speak>Concurrent test ${System.nanoTime()}</speak>"
        val results = (1..6).map {
            async {
                EdgeNeuralTtsClient.synthesizeToCachedMp3(
                    context = context,
                    voiceId = "tr-TR-EmelNeural",
                    ssml = uniqueSsml
                )
            }
        }.awaitAll()

        results.forEach { file ->
            assertNotNull("Every concurrent caller should receive a valid cached MP3 file", file)
            assertTrue("Cached file should exist and match full payload size", file!!.exists() && file.length() == payload.size.toLong())
        }
    }

    @Test
    fun bookModeVoiceEngine_castsAllKeyCharactersToFreeNeuralVoices() {
        val engine = CosmicCharacterVoiceEngine(context)
        try {
            val knownFreeVoiceIds = EdgeNeuralTtsClient.NEURAL_VOICES.map { it.id }.toSet()
            val characters = listOf(
                "NATASHA", "WANDA", "MIRA", "AIDEN", "STEVE", "TONY",
                "PIETRO", "VISION", "THOR", "VOSS", "ZERO", "ULTRON", "VAELOR"
            )
            characters.forEach { speaker ->
                val cast = engine.resolveFreeNeuralCast(speaker)
                assertTrue(
                    "Speaker $speaker should map to a curated free EdgeNeural voice, got ${cast.voiceId}",
                    cast.voiceId in knownFreeVoiceIds
                )
                assertTrue("Rate for $speaker should stay within natural bounds", cast.baseRate in 0.80f..1.20f)
                assertTrue("Pitch for $speaker should stay within natural bounds", cast.basePitch in 0.75f..1.15f)
            }
        } finally {
            engine.stopAndRelease()
        }
    }

    @Test
    fun customApiModelParser_returnsAllActiveChatModelsAndFiltersInactiveAndNonChat() {
        val rawJson = """
            {
              "data": [
                {"id": "llama-3.3-70b-versatile", "active": true},
                {"id": "qwen/qwen3-32b", "enabled": true, "status": "active"},
                {"id": "deepseek-r1-distill-llama-70b", "active": false},
                {"id": "old-gpt-3.5", "deprecated": true},
                {"id": "archived-model", "status": "decommissioned"},
                {"id": "whisper-large-v3", "active": true},
                {"id": "text-embedding-3-large", "type": "embedding"},
                {"id": "dall-e-3", "active": true},
                {"id": "meta-llama/llama-guard-4-12b", "active": true},
                {"id": "flux-pro-1.1", "architecture": {"output_modalities": ["image"]}},
                {"id": "openai/gpt-4.1-mini", "architecture": {"output_modalities": ["text"]}}
              ]
            }
        """.trimIndent()

        val parsed = ProviderFallbackManager.parseActiveChatModelsFromJson(rawJson)
        assertEquals(
            listOf("llama-3.3-70b-versatile", "openai/gpt-4.1-mini", "qwen/qwen3-32b"),
            parsed
        )
        assertFalse(parsed.contains("deepseek-r1-distill-llama-70b"))
        assertFalse(parsed.contains("whisper-large-v3"))
        assertFalse(parsed.contains("text-embedding-3-large"))
        assertFalse(parsed.contains("meta-llama/llama-guard-4-12b"))
    }

    @Test
    fun resolveUtilityModel_prefersManualCustomUtilityModelOverAutoSelectedFlag() {
        val providerKey = "custom_99"
        AppFeatureFlags.setAiUtilityModel(providerKey, "auto-picked-wrong-model", "ai")
        val customEntity = CustomProviderEntity(
            id = 99L,
            label = "My Proxy",
            baseUrl = "https://proxy.example.com/v1",
            apiKeyEncrypted = "enc",
            modelName = "deepseek-r1",
            utilityModel = "my-manual-small-model",
            utilityModelSource = "manual"
        )

        try {
            val resolved = EmochiRepository.resolveUtilityModel(
                providerKey = providerKey,
                settings = UserSettingsEntity(),
                customProvider = customEntity
            )
            assertEquals(
                "User's explicit manual custom utilityModel MUST always beat auto-selected AppFeatureFlags model",
                "my-manual-small-model",
                resolved
            )
        } finally {
            AppFeatureFlags.clearAiUtilityModel(providerKey)
        }
    }

    @Test
    fun customProvider_usesOwnModelNameInStreamAndFallbackEvenWhenPlaceholderModelPassed() = runBlocking {
        ProviderHealthTracker.resetAll()
        AppFeatureFlags.setSseStreaming(true)
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val insertedId = db.customProviderDao().insertProvider(
                CustomProviderEntity(
                    label = "My Custom RU Proxy",
                    baseUrl = "https://custom-proxy.example.com/v1",
                    apiKeyEncrypted = "plain_or_enc",
                    modelName = "deepseek/deepseek-v3.2-special",
                    apiFormat = "openai"
                )
            )
            val providerKey = "custom_$insertedId"
            val settings = UserSettingsEntity(
                selectedProvider = providerKey,
                enableAutoFallback = false
            )
            val userMsg = MessageEntity(id = "u_custom", botId = "b_custom", role = "user", text = "Selam", timestamp = 1L)

            val repo = EmochiRepository(db, context)
            assertEquals(
                "resolveEffectiveModelForProvider must resolve the custom provider's real modelName",
                "deepseek/deepseek-v3.2-special",
                repo.resolveEffectiveModelForProvider(settings)
            )

            var streamCapturedModel = ""
            var nonStreamCapturedModel = ""
            val manager = ProviderFallbackManager(
                customProviderDao = db.customProviderDao(),
                onLogFallback = {},
                customExecutor = { pKey, resolvedModel, _, _, _ ->
                    nonStreamCapturedModel = resolvedModel
                    ModelResponseResult(text = "Merhaba yanıtı", usedProvider = pKey)
                },
                customStreamExecutor = { pKey, resolvedModel, _, _, onToken, _ ->
                    streamCapturedModel = resolvedModel
                    onToken("Merhaba akış yanıtı")
                    ModelResponseResult(text = "Merhaba akış yanıtı", usedProvider = pKey)
                }
            )

            // Even if a caller passes placeholder "gemini-2.0-flash", customEntity.modelName MUST win!
            manager.executeChatStream(
                model = "gemini-2.0-flash",
                settings = settings,
                systemPrompt = "Sys",
                messages = listOf(userMsg),
                buildConfigGeminiKey = "",
                onVisibleDelta = { _, _ -> }
            )
            assertEquals(
                "executeChatStream MUST send customEntity.modelName instead of gemini-2.0-flash",
                "deepseek/deepseek-v3.2-special",
                streamCapturedModel
            )

            manager.executeWithFallback(
                model = "gemini-2.0-flash",
                settings = settings,
                systemPrompt = "Sys",
                messages = listOf(userMsg),
                buildConfigGeminiKey = ""
            )
            assertEquals(
                "executeWithFallback MUST send customEntity.modelName instead of gemini-2.0-flash",
                "deepseek/deepseek-v3.2-special",
                nonStreamCapturedModel
            )
        } finally {
            ProviderHealthTracker.resetAll()
            db.close()
        }
    }

    @Test
    fun edgeNeuralTtsClient_cleansStaleTmpFilesAndEnforcesLruLimit() {
        val dir = java.io.File(context.cacheDir, "tts_lru_test_${System.nanoTime()}").apply { mkdirs() }
        try {
            val staleTmp = java.io.File(dir, "orphan.123.tmp").apply {
                writeBytes(ByteArray(256))
                setLastModified(System.currentTimeMillis() - 120_000L)
            }
            val oldestMp3 = java.io.File(dir, "oldest.mp3").apply {
                writeBytes(ByteArray(1000))
                setLastModified(System.currentTimeMillis() - 50_000L)
            }
            val newestMp3 = java.io.File(dir, "newest.mp3").apply {
                writeBytes(ByteArray(1000))
                setLastModified(System.currentTimeMillis())
            }

            EdgeNeuralTtsClient.enforceCacheLruLimit(dir, maxBytes = 1200L)

            assertFalse("Stale orphan .tmp file should be deleted", staleTmp.exists())
            assertFalse("Oldest MP3 should be evicted to respect LRU cap", oldestMp3.exists())
            assertTrue("Newest MP3 should be kept", newestMp3.exists())
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun chatTtsManager_shutdownDoesNotClearNewerInstanceBookAudioListener() {
        val first = ChatTtsManager(context)
        val second = ChatTtsManager(context)
        try {
            val secondListener = com.example.util.BookAudioController.onPlaybackStarted
            assertNotNull("Second instance should register BookAudioController listener", secondListener)
            first.shutdown()
            assertTrue(
                "Shutting down an older ChatTtsManager must NOT wipe a newer instance's listener",
                com.example.util.BookAudioController.onPlaybackStarted === secondListener
            )
        } finally {
            second.shutdown()
        }
    }

    @Test
    fun extractBookVoiceCardEntries_extractsOnlyVoiceCardsWithResolvedSpeakers() {
        val paragraphs = listOf(
            "Normal anlatı paragrafı.",
            "[[DOSSIER|SHIELD Raporu|Gizli dosya içeriği]]",
            "[[COMMS|Natasha Romanoff|Aiden, kuleye çıkma!]]",
            "[[MIND|Wanda Maximoff|Zaman dokusu parçalanıyor...]]",
            "[[AUDIO|Elias Voss (6-B)|Sistem çökmek üzere.|Ek satır]]",
            "[[ALARM|Kırmızı Alarm|Tahliye]]",
            "[[COMMS|Tony Stark|   ]]"
        )
        val extracted = com.example.ui.screens.extractBookVoiceCardEntries(paragraphs)
        assertEquals(3, extracted.size)
        assertEquals("NATASHA", extracted[0].first)
        assertEquals("Aiden, kuleye çıkma!", extracted[0].second)
        assertEquals("WANDA", extracted[1].first)
        assertEquals("Zaman dokusu parçalanıyor...", extracted[1].second)
        assertEquals("VOSS", extracted[2].first)
        assertEquals("Sistem çökmek üzere.|Ek satır", extracted[2].second)
    }

    @Test
    fun bookModeVoiceEngine_distinctActorsAndNaturalTwinAidenProsody() {
        val engine = CosmicCharacterVoiceEngine(context)
        try {
            val natasha = engine.resolveFreeNeuralCast("NATASHA")
            val wanda = engine.resolveFreeNeuralCast("WANDA")
            val mira = engine.resolveFreeNeuralCast("MIRA")
            val tony = engine.resolveFreeNeuralCast("TONY")
            val steve = engine.resolveFreeNeuralCast("STEVE")
            val twinAiden = engine.resolveFreeNeuralCast("TWIN_AIDEN")

            assertTrue(
                "Natasha, Wanda, and Mira must use distinct neural female actors",
                setOf(natasha.voiceId, wanda.voiceId, mira.voiceId).size == 3
            )
            assertTrue(
                "Tony, Steve, and Twin Aiden must use distinct neural male actors",
                setOf(tony.voiceId, steve.voiceId, twinAiden.voiceId).size == 3
            )
            assertTrue(
                "Twin Aiden rate must stay natural (not slurred slow-motion), got ${twinAiden.baseRate}",
                twinAiden.baseRate in 0.92f..1.05f
            )
            assertTrue(
                "Twin Aiden pitch must stay natural (not muddy bass), got ${twinAiden.basePitch}",
                twinAiden.basePitch in 0.92f..1.05f
            )
        } finally {
            engine.stopAndRelease()
        }
    }

    @Test
    fun edgeNeuralTtsClient_formatsOfficialFullEdgeVoiceNameInSsml() {
        assertEquals(
            "Microsoft Server Speech Text to Speech Voice (tr-TR, AhmetNeural)",
            EdgeNeuralTtsClient.toFullEdgeVoiceName("tr-TR-AhmetNeural")
        )
        assertEquals(
            "Microsoft Server Speech Text to Speech Voice (de-DE, SeraphinaMultilingualNeural)",
            EdgeNeuralTtsClient.toFullEdgeVoiceName("de-DE-SeraphinaMultilingualNeural")
        )
        val built = EdgeNeuralTtsClient.buildEdgeSingleProsodySsml(
            voiceId = "tr-TR-AhmetNeural",
            plainText = "Demir fırtına başlayacak.",
            ratePercent = -4,
            pitchHz = -2
        )
        assertTrue(
            "SSML must contain official full Edge ReadAloud voice name",
            built.contains("Microsoft Server Speech Text to Speech Voice (tr-TR, AhmetNeural)")
        )
        val normalized = EdgeNeuralTtsClient.normalizeSsmlForEdgeReadAloud(
            voiceId = "tr-TR-EmelNeural",
            rawSsml = "<speak xml:lang='tr-TR'><voice name='tr-TR-EmelNeural'><prosody rate='+0%' pitch='+0Hz' volume='+0%'>Merhaba</prosody></voice></speak>"
        )
        assertTrue(
            "Normalized SSML must upgrade short voice IDs to official full Edge voice name",
            normalized.contains("Microsoft Server Speech Text to Speech Voice (tr-TR, EmelNeural)")
        )
    }
}
