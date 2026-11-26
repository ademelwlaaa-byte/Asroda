package com.example

import android.content.Context
import android.media.AudioManager
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.EmotionState
import com.example.data.local.PrimaryEmotions
import com.example.ui.screens.cosmic3d.Cosmic3DSoundEngine
import com.example.ui.screens.cosmic3d.CosmicCharacterVoiceEngine
import com.example.util.AppFeatureFlags
import com.example.util.BookAudioController
import com.example.util.ChatTtsManager
import com.example.util.EdgeNeuralTtsClient
import com.example.util.EmotionalTtsProsodyEngine
import com.example.util.TtsSentenceChunker
import kotlinx.coroutines.delay
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
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AudioTtsAndVoiceEngineOverhaulTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        File(context.cacheDir, "tts_audio_cache_v3").deleteRecursively()
        EdgeNeuralTtsClient.synthesizerOverride = null
        EdgeNeuralTtsClient.resetCircuitBreakerForTest()
        BookAudioController.resetForTesting()
    }

    @After
    fun tearDown() {
        EdgeNeuralTtsClient.synthesizerOverride = null
        EdgeNeuralTtsClient.resetCircuitBreakerForTest()
        BookAudioController.resetForTesting()
        File(context.cacheDir, "tts_audio_cache_v3").deleteRecursively()
    }

    // =========================================================================
    // 1. TtsSentenceChunker: Turkish quotes, ordinals, Unicode ellipsis & abbrevs
    // =========================================================================
    @Test
    fun ttsSentenceChunker_handlesTurkishQuotesOrdinalsEllipsesAndAbbreviations() {
        // Closing quotes attached to boundary punctuation must stay on the quoted sentence
        val quoted = TtsSentenceChunker.splitSentences("\"Gidelim.\" dedi.")
        assertEquals(listOf("\"Gidelim.\"", "dedi."), quoted)

        val quotedAbbrev = TtsSentenceChunker.splitSentences("\"Dr. Voss geldi!\" diye bağırdı.")
        assertEquals(listOf("\"Dr. Voss geldi!\"", "diye bağırdı."), quotedAbbrev)

        // Turkish ordinals (1. sınıf, 2. bölüm, 3. kez) must NOT trigger premature splits
        val ordinals = TtsSentenceChunker.splitSentences("1. sınıf ve 2. bölüm başladı. 3. kez denedim.")
        assertEquals(
            listOf("1. sınıf ve 2. bölüm başladı.", "3. kez denedim."),
            ordinals
        )

        // 4-digit years followed by a new sentence SHOULD still split cleanly
        val yearSplit = TtsSentenceChunker.splitSentences("Yıl 2026. Savaş başladı.")
        assertEquals(listOf("Yıl 2026.", "Savaş başladı."), yearSplit)

        // Unicode ellipsis (…) and ASCII ellipsis (...) must split cleanly at end of run
        val unicodeEllipsis = TtsSentenceChunker.splitSentences("Bekle… Geliyorum.")
        assertEquals(listOf("Bekle…", "Geliyorum."), unicodeEllipsis)

        val asciiEllipsis = TtsSentenceChunker.splitSentences("Dur... Hazır ol!")
        assertEquals(listOf("Dur...", "Hazır ol!"), asciiEllipsis)

        // Expanded Turkish abbreviations (cad., sok., mah., dk., tl., av., vb., no.)
        val abbrevs = TtsSentenceChunker.splitSentences(
            "İstiklal Cad. No. 5, Çiçek Mah. 3. Sok. adresinde 15 dk. bekledik, 50 tl. ödedik. Av. Kaya vb. isimler oradaydı."
        )
        assertEquals(2, abbrevs.size)
        assertTrue(abbrevs[0].contains("İstiklal Cad. No. 5"))
        assertTrue(abbrevs[0].endsWith("50 tl. ödedik."))
        assertTrue(abbrevs[1].startsWith("Av. Kaya vb."))
    }

    // =========================================================================
    // 2. EmotionalTtsProsodyEngine: Prosody-coherent neural batching
    // =========================================================================
    @Test
    fun emotionalTtsProsodyEngine_splitsBatchesOnNarrationDialogueAndProsodyShifts() {
        val mixedScene = "*Yavaşça fısıldayarak eğilir...* \"Bunu kimseye söyleme!\" *Birden kükreyerek bağırır!* \"Hemen kaçın?!\""
        val segments = EmotionalTtsProsodyEngine.buildProsodySegments(
            rawText = mixedScene,
            emotionState = EmotionState(dominantEmotion = "gergin", primaryEmotions = PrimaryEmotions(anger = 35)),
            readActions = true,
            emotionalProsodyEnabled = true
        )
        assertTrue("Expected at least 4 distinct narration/dialogue segments", segments.size >= 4)

        val batches = EmotionalTtsProsodyEngine.batchSegmentsForNeural(
            segments = segments,
            maxCharsPerBatch = 650,
            firstBatchMaxChars = 220
        )
        assertTrue("Narration & dialogue with different prosody must be split into distinct batches", batches.size >= 4)
        batches.forEach { batch ->
            val kinds = batch.map { it.kind }.distinct()
            assertEquals("Each neural batch must preserve a coherent segment kind", 1, kinds.size)
        }
    }

    // =========================================================================
    // 3. EdgeNeuralTtsClient: Weighted prosody normalization & clean break pauses
    // =========================================================================
    @Test
    fun edgeNeuralTtsClient_normalizesMultiProsodyWithWeightedAverageAndNoDoubleEllipsis() {
        val multiProsodySsml = "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='tr-TR'>" +
            "<voice name='tr-TR-AhmetNeural'>" +
            "<prosody rate='-20%' pitch='-10Hz'>Dur...</prosody>" +
            "<break time='250ms'/>" +
            "<prosody rate='+20%' pitch='+10Hz'>Bu çok daha uzun ve enerjik bir diyalog cümlesi olduğu için ortalama prosodi buna yaklaşmalı!</prosody>" +
            "</voice></speak>"

        val normalized = EdgeNeuralTtsClient.normalizeSsmlForEdgeReadAloud("tr-TR-AhmetNeural", multiProsodySsml)
        assertFalse("Normalized Edge SSML must not contain <break tags", normalized.contains("<break", ignoreCase = true))
        assertFalse("Must NOT produce double ellipsis after existing '...'", normalized.contains("... ..."))
        assertTrue("Must contain full Microsoft Edge voice name", normalized.contains("Microsoft Server Speech Text to Speech Voice (tr-TR, AhmetNeural)"))

        // Weighted average should be positive because the +20% / +10Hz segment is much longer than "Dur..."
        val rateMatch = Regex("""rate='([+-]?\d+)%'""").find(normalized)
        val pitchMatch = Regex("""pitch='([+-]?\d+)Hz'""").find(normalized)
        assertNotNull("Normalized SSML must carry rate attribute", rateMatch)
        assertNotNull("Normalized SSML must carry pitch attribute", pitchMatch)
        assertTrue("Weighted rate should be positive (> 0), was ${rateMatch!!.groupValues[1]}", rateMatch.groupValues[1].toInt() > 0)
        assertTrue("Weighted pitch should be positive (> 0), was ${pitchMatch!!.groupValues[1]}", pitchMatch.groupValues[1].toInt() > 0)

        // Break after plain word without punctuation inserts natural pause punctuation
        val plainBreakSsml = "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='tr-TR'>" +
            "<voice name='tr-TR-EmelNeural'><prosody rate='+0%' pitch='+0Hz'>Bekle<break time='380ms'/>hemen geliyorum</prosody></voice></speak>"
        val normPlain = EdgeNeuralTtsClient.normalizeSsmlForEdgeReadAloud("tr-TR-EmelNeural", plainBreakSsml)
        assertTrue("Long break after unpunctuated word should become ellipsis pause", normPlain.contains("Bekle... hemen geliyorum"))
    }

    // =========================================================================
    // 4. EdgeNeuralTtsClient: Circuit breaker opens after 2 WS failures & cools down
    // =========================================================================
    @Test
    fun edgeNeuralTtsClient_circuitBreakerFastFailsOfflineAndRecoversAfterCooldown() = runBlocking {
        var fakeNow = 1_000_000L
        EdgeNeuralTtsClient.clockMsProvider = { fakeNow }

        assertFalse(EdgeNeuralTtsClient.isNeuralCircuitOpen())

        // 1st failure does not trip the circuit yet
        EdgeNeuralTtsClient.recordNeuralWsFailure(fakeNow)
        assertFalse("Single transient failure must not open circuit", EdgeNeuralTtsClient.isNeuralCircuitOpen())

        // 2nd consecutive failure opens circuit for 45s
        EdgeNeuralTtsClient.recordNeuralWsFailure(fakeNow)
        assertTrue("2 consecutive WS failures must open circuit", EdgeNeuralTtsClient.isNeuralCircuitOpen())

        // While open, synthesizeToCachedMp3 fast-fails immediately (0ms) without touching network
        val startMs = System.currentTimeMillis()
        val result = EdgeNeuralTtsClient.synthesizeToCachedMp3(
            context = context,
            voiceId = "tr-TR-EmelNeural",
            ssml = EdgeNeuralTtsClient.buildEdgeSingleProsodySsml("tr-TR-EmelNeural", "Devre kesici testi benzersiz 991")
        )
        val elapsedMs = System.currentTimeMillis() - startMs
        assertNull("Open circuit must fast-fail with null", result)
        assertTrue("Open circuit fast-fail must return promptly (<500ms), took ${elapsedMs}ms", elapsedMs < 500L)

        // Just before cooldown expiry: still open
        fakeNow += EdgeNeuralTtsClient.CIRCUIT_BREAKER_COOLDOWN_MS - 1L
        assertTrue(EdgeNeuralTtsClient.isNeuralCircuitOpen())

        // After cooldown expiry: half-open/closed again
        fakeNow += 2L
        assertFalse("Circuit must close after cooldown expires", EdgeNeuralTtsClient.isNeuralCircuitOpen())

        // Success resets failure counter so 1 new failure won't immediately re-open
        EdgeNeuralTtsClient.recordNeuralWsSuccess()
        EdgeNeuralTtsClient.recordNeuralWsFailure(fakeNow)
        assertFalse(EdgeNeuralTtsClient.isNeuralCircuitOpen())
    }

    // =========================================================================
    // 5. ChatTtsManager: Mid-message neural batch failure seamlessly continues on Device TTS
    // =========================================================================
    @Test
    fun chatTtsManager_midBatchNeuralFailure_continuesRemainingSegmentsOnDeviceTts() = runBlocking {
        AppFeatureFlags.setTtsEngineMode(AppFeatureFlags.TTS_ENGINE_NEURAL)

        val synthCalls = AtomicInteger(0)
        val dummyMp3 = ByteArray(256) { 0x55 }
        EdgeNeuralTtsClient.synthesizerOverride = { _, _ ->
            // Batch #0 succeeds via Neural; batch #1+ simulates mid-message network drop
            if (synthCalls.getAndIncrement() == 0) dummyMp3 else null
        }

        val manager = ChatTtsManager(context)
        manager.simulateInitCompleteForTest()

        val neuralPlayedCount = AtomicInteger(0)
        manager.playNeuralFileOverride = { file ->
            assertTrue("Synthesized MP3 cache file must exist", file.exists() && file.length() >= 128L)
            neuralPlayedCount.incrementAndGet()
            true
        }

        val deviceFallbackTexts = mutableListOf<String>()
        manager.deviceSpeakOverride = { remainingSegments, _ ->
            deviceFallbackTexts.addAll(remainingSegments.map { it.text })
            true
        }

        val multiBatchMessage = "*Gece yarısı rüzgar usulca eser.* \"İkinci kısım cihaz sesiyle kesintisiz devam etmeli!\""
        val totalSegments = EmotionalTtsProsodyEngine.buildProsodySegments(
            rawText = multiBatchMessage,
            readActions = true,
            emotionalProsodyEnabled = true
        )
        assertTrue("Test fixture must yield >= 2 segments", totalSegments.size >= 2)

        val started = manager.speakMessage(
            messageId = "msg_mid_batch_fallback",
            rawText = multiBatchMessage,
            readActions = true
        )
        assertTrue(started)

        repeat(20) {
            delay(25)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }

        assertEquals("Batch 0 must play on Neural layer", 1, neuralPlayedCount.get())
        assertTrue(
            "Mid-message failure must hand off remaining segments to Device TTS instead of truncating",
            deviceFallbackTexts.isNotEmpty()
        )
        assertTrue(
            "Device TTS must receive ONLY remaining segments, not replay batch 0",
            deviceFallbackTexts.size < totalSegments.size
        )
        assertTrue(
            "Remaining dialogue text must be spoken by Device TTS",
            deviceFallbackTexts.any { it.contains("cihaz sesiyle kesintisiz") }
        )
        assertEquals("device", manager.lastActiveTier)
        assertEquals("msg_mid_batch_fallback", manager.speakingMessageId.value)

        manager.stop()
        assertFalse(manager.isSpeaking.value)
        assertNull(manager.speakingMessageId.value)
        manager.shutdown()
    }

    // =========================================================================
    // 6. BookAudioController: AudioFocus loss/duck/regain + voice duck target preservation
    // =========================================================================
    @Test
    fun bookAudioController_audioFocusAndVoiceDuckingPreserveTargetVolume() {
        BookAudioController.simulatePlaybackStateForTest(
            playing = true,
            muted = false,
            rawResId = 42,
            volume = 0.85f
        )
        assertEquals(0.85f, BookAudioController.currentTargetVolumeForTest, 0.001f)
        assertEquals(0.85f, BookAudioController.currentVolumeForTest, 0.001f)

        // 1. System transient duck (e.g. navigation prompt) lowers live volume WITHOUT overwriting target volume
        BookAudioController.handleAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertTrue("System duck must lower current volume", BookAudioController.currentVolumeForTest < 0.30f)
        assertEquals("System duck must NOT corrupt resumeTargetVolume", 0.85f, BookAudioController.currentTargetVolumeForTest, 0.001f)

        // Regain restores full 0.85f volume
        BookAudioController.handleAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertEquals(0.85f, BookAudioController.currentVolumeForTest, 0.001f)

        // 2. Character voice ducking must ALSO preserve resumeTargetVolume (regression guard!)
        BookAudioController.setVoiceDucking(true)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertTrue("Voice duck must lower volume", BookAudioController.currentVolumeForTest < 0.35f)
        assertEquals("Voice duck must preserve target volume", 0.85f, BookAudioController.currentTargetVolumeForTest, 0.001f)

        BookAudioController.setVoiceDucking(false)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertEquals("Ending voice duck must restore full 0.85f", 0.85f, BookAudioController.currentVolumeForTest, 0.001f)

        // 3. Transient focus loss (e.g. phone call) pauses without muting and resumes on regain
        BookAudioController.handleAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertFalse(BookAudioController.isPlayingFlow.value)
        assertFalse(BookAudioController.isMutedFlow.value)

        BookAudioController.handleAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertTrue("Transient focus loss must resume on AUDIOFOCUS_GAIN", BookAudioController.isPlayingFlow.value)

        // 4. Permanent focus loss (another media player) pauses and does NOT unexpectedly resume later
        BookAudioController.handleAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertFalse(BookAudioController.isPlayingFlow.value)

        BookAudioController.handleAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertFalse("Permanent AUDIOFOCUS_LOSS must NOT auto-resume on later focus gain", BookAudioController.isPlayingFlow.value)
    }

    // =========================================================================
    // 7. CosmicCharacterVoiceEngine: Bounded LRU envelope cache & clean teardown
    // =========================================================================
    @Test
    fun cosmicCharacterVoiceEngine_boundsEnvelopeCacheAndClearsOnRelease() {
        val engine = CosmicCharacterVoiceEngine(context)
        val totalToInsert = CosmicCharacterVoiceEngine.MAX_CACHED_ENVELOPES + 20
        for (i in 0 until totalToInsert) {
            engine.putEnvelope("/cache/line_$i.wav", floatArrayOf(0.1f, 0.5f, 0.2f))
        }
        assertEquals(
            "Envelope cache must stay bounded at MAX_CACHED_ENVELOPES",
            CosmicCharacterVoiceEngine.MAX_CACHED_ENVELOPES,
            engine.cachedEnvelopeCountForTest()
        )

        engine.stopCurrentLine()
        engine.stopAndRelease()
        assertEquals(
            "stopAndRelease must clear cached speech envelopes",
            0,
            engine.cachedEnvelopeCountForTest()
        )
        // Idempotent second release must never crash
        engine.stopAndRelease()
    }

    // =========================================================================
    // 8. Cosmic3DSoundEngine: Lock-free fixed polyphony voice pool & clean release
    // =========================================================================
    @Test
    fun cosmic3DSoundEngine_fixedPolyphonyPoolCapsAndClearsOnRelease() {
        val soundEngine = Cosmic3DSoundEngine(context = context, enableOrchestralMusic = false)
        val dummyBuf = FloatArray(128) { 0.25f }

        var accepted = 0
        for (i in 0 until Cosmic3DSoundEngine.MAX_POLYPHONY + 6) {
            if (soundEngine.enqueueVoiceSlotForTest(dummyBuf)) {
                accepted++
            }
        }
        assertEquals(Cosmic3DSoundEngine.MAX_POLYPHONY, accepted)
        assertEquals(Cosmic3DSoundEngine.MAX_POLYPHONY, soundEngine.activeVoiceCountForTest())

        soundEngine.stopAndRelease()
        assertEquals("stopAndRelease must clear all voice slots", 0, soundEngine.activeVoiceCountForTest())
    }
}
