package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CustomProviderEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.managers.ProviderFallbackManager
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
}
