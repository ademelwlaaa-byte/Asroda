package com.example.ui.screens.cosmic3d

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.util.EdgeNeuralTtsClient
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * Cinema-grade Emotional Turkish Character Voice Acting & Studio Mastering Engine.
 *
 * Eliminates flat/robotic Android TTS by combining 3 studio layers:
 * 1. Priority-1 Studio Override: Plays any custom `res/raw/<lineId>.mp3/.wav` directly if bundled.
 * 2. HD Neural Turkish Voice Routing + Acted Multi-Beat Micro-Prosody:
 *    - Prioritizes Android's highest-quality Neural/WaveNet Turkish voices (including HD network voices
 *      when available, with seamless local HD fallback) and separates female vs. male voices cleanly.
 *    - Splits every dialogue line into dramatic acted beats (`!`, `?`, `—`, `...`, `.`) with expressive
 *      tempo/inflection shifts and natural actor breath pauses instead of monotone single-pass speech.
 * 3. Real-Time Cinema Mastering DSP (via `synthesizeToFile` -> PCM Studio Chain -> Cached Stereo WAV):
 *    - Warm analog vocal compression, rumble high-pass, character EQ & spatial acoustics:
 *      • NATASHA: Intimate velvety alto + warm tactical comms clarity + soft radio squelch
 *      • WANDA: Warm emotional vocal presence + shimmering ethereal stereo hall tail
 *      • STEVE: Heroic baritone warmth + crisp tactical comms presence
 *      • TONY: Authentic Iron Man HUD helmet comms bandpass + subtle metallic early reflection
 *      • AIDEN: Crisp cinema protagonist vocal presence
 *      • TWIN_AIDEN: Exhausted dark-future gravitas + haunting dimensional rift stereo echo
 *      • VAELOR: Massive cosmic warlord dual-layer sub-harmonic voice doubling (-3.5 semitones) + cathedral reverb
 *    - Automatically ducks background music/SFX via [onSpeakingStateChanged] while characters speak.
 */
class CosmicCharacterVoiceEngine(
    context: Context,
    private val onSpeakingStateChanged: (Boolean) -> Unit = {}
) {
    private data class VoiceBeat(
        val text: String,
        val pitchDelta: Float,
        val rateDelta: Float,
        val pauseAfterMs: Int
    )

    private data class CharacterVoiceProfile(
        val isFemale: Boolean,
        val basePitch: Float,
        val baseRate: Float,
        val voiceSlotIndex: Int,
        val dspStyle: DspStyle
    )

    private enum class DspStyle {
        NATASHA_VELVET_INTIMATE, // Natasha: smoky alto warmth + close-mic presence
        WANDA_ETHEREAL_REVERB,   // Wanda & Strange: emotional warm vocal + shimmering stereo hall
        MIRA_CRYSTAL_YOUTH,      // Mira: bright youthful crystalline lift (+1.2 semitones)
        TACTICAL_COMMS_WARM,     // Steve & Pietro: crisp tactical clarity + warm air
        IRON_MAN_HELMET,         // Tony: authentic HUD helmet comms + metallic early reflection
        HERO_CINEMA,             // Aiden: crisp cinema protagonist close-mic presence
        THOR_ASGARD_HALL,        // Thor: deep booming baritone chest + wide Asgard hall
        VISION_SYNTH_CRYSTAL,    // Vision: pure harmonic shimmer + serene resonant space
        DARK_RIFT_ECHO,          // Twin Aiden (Sıfır) & Voss: haunting dark-future dimensional echo
        ULTRON_METAL_RESONANCE,  // Ultron Prime: cold vibranium baritone resonance + metallic ring
        COSMIC_WARLORD_DUAL      // Vaelor & Asıl Mimar: dual-layer sub-harmonic + cathedral reverb
    }

    private data class DecodedWav(
        val sampleRate: Int,
        val samples: FloatArray // mono normalized -1..1
    )

    private val appContext = context.applicationContext
    private val cacheDir = File(appContext.cacheDir, "cosmic_studio_voices_v5").apply { mkdirs() }
    private val bundledStudioVoices: Set<String> by lazy {
        try {
            appContext.assets.list(STUDIO_VOICES_ASSET_DIR)?.toHashSet() ?: emptySet()
        } catch (_: Throwable) {
            emptySet()
        }
    }
    private val synthExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "CosmicVoice-StudioMaster").apply { isDaemon = true }
    }
    private val synthLatches = ConcurrentHashMap<String, CountDownLatch>()
    private val synthSucceeded = ConcurrentHashMap<String, Boolean>()
    private val envelopeByPath = ConcurrentHashMap<String, FloatArray>()
    private val speakGeneration = AtomicInteger(0)

    @Volatile private var tts: TextToSpeech? = null
    @Volatile private var clipPlayer: MediaPlayer? = null
    @Volatile private var activeEnvelope: FloatArray? = null
    @Volatile private var isSpeakingNow = false
    @Volatile private var activeDirectFallbackId: String? = null
    @Volatile private var directFallbackStartMs: Long = 0L
    @Volatile private var ready = false
    @Volatile private var muted = false
    @Volatile private var released = false

    private val readyLatch = CountDownLatch(1)

    @Volatile private var femaleHdVoices: List<Voice> = emptyList()
    @Volatile private var maleHdVoices: List<Voice> = emptyList()
    @Volatile private var femaleLocalVoices: List<Voice> = emptyList()
    @Volatile private var maleLocalVoices: List<Voice> = emptyList()
    @Volatile private var allTrVoices: List<Voice> = emptyList()
    @Volatile private var localTrVoices: List<Voice> = emptyList()

    init {
        // Purge older v3/v4 cached audio directories once so legacy robotic/accented clips never linger
        synthExecutor.execute {
            try {
                File(appContext.cacheDir, "cosmic_studio_voices_v3").deleteRecursively()
                File(appContext.cacheDir, "cosmic_studio_voices_v4").deleteRecursively()
            } catch (_: Throwable) {}
        }
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS && !released) {
                    configureVoices()
                    ready = true
                }
                readyLatch.countDown()
            }
        } catch (_: Throwable) {
            ready = false
            readyLatch.countDown()
        }
    }

    private fun configureVoices() {
        val engine = tts ?: return
        try {
            engine.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            engine.setLanguage(Locale.forLanguageTag("tr-TR"))
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    if (utteranceId != null && utteranceId == activeDirectFallbackId) {
                        directFallbackStartMs = System.currentTimeMillis()
                        notifySpeaking(true)
                    }
                }
                override fun onDone(utteranceId: String?) {
                    if (utteranceId != null) {
                        if (utteranceId == activeDirectFallbackId) {
                            activeDirectFallbackId = null
                            notifySpeaking(false)
                        } else {
                            synthSucceeded[utteranceId] = true
                            synthLatches.remove(utteranceId)?.countDown()
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId != null) {
                        if (utteranceId == activeDirectFallbackId) {
                            activeDirectFallbackId = null
                            notifySpeaking(false)
                        } else {
                            synthSucceeded[utteranceId] = false
                            synthLatches.remove(utteranceId)?.countDown()
                        }
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    if (utteranceId != null) {
                        if (utteranceId == activeDirectFallbackId) {
                            activeDirectFallbackId = null
                            notifySpeaking(false)
                        } else {
                            synthSucceeded[utteranceId] = false
                            synthLatches.remove(utteranceId)?.countDown()
                        }
                    }
                }
            })

            val allTr = (engine.voices ?: emptySet())
                .filter { it.locale?.language?.equals("tr", ignoreCase = true) == true }
                .sortedWith(
                    compareByDescending<Voice> { scoreVoiceNaturalness(it) }
                        .thenByDescending { it.quality }
                        .thenBy { it.latency }
                )

            val localTr = allTr.filter { !it.isNetworkConnectionRequired }
            allTrVoices = allTr
            localTrVoices = if (localTr.isNotEmpty()) localTr else allTr

            femaleHdVoices = allTr.filter { isFemaleTrVoice(it) }
            maleHdVoices = allTr.filter { isMaleTrVoice(it) }
            femaleLocalVoices = localTrVoices.filter { isFemaleTrVoice(it) }
            maleLocalVoices = localTrVoices.filter { isMaleTrVoice(it) }
        } catch (_: Throwable) {
        }
    }

    private fun scoreVoiceNaturalness(v: Voice): Int {
        val n = v.name.lowercase(Locale.ROOT)
        var s = v.quality
        // Prefer Google's rich Neural / WaveNet / HD Turkish voices over compact legacy voices
        if ("network" in n && v.quality >= Voice.QUALITY_HIGH) s += 160
        if ("neural" in n || "wavenet" in n || "studio" in n) s += 220
        if ("tr-tr-x-" in n) s += 120
        if (v.quality <= Voice.QUALITY_LOW) s -= 200
        return s
    }

    private fun isFemaleTrVoice(v: Voice): Boolean {
        val n = v.name.lowercase(Locale.ROOT)
        val femHints = listOf("female", "ama", "efu", "tmc", "f0", "w0", "filiz", "yelda", "emine", "emel", "seda")
        val maleHints = listOf("male", "cfs", "mfm", "m0", "cem", "kerem", "tolga", "ahmet")
        return femHints.any { it in n } && !maleHints.any { it in n && it != "ama" }
    }

    private fun isMaleTrVoice(v: Voice): Boolean {
        val n = v.name.lowercase(Locale.ROOT)
        val maleHints = listOf("male", "cfs", "mfm", "m0", "cem", "kerem", "tolga", "ahmet")
        return maleHints.any { it in n }
    }

    private fun profileFor(speakerKey: String): CharacterVoiceProfile = when (speakerKey.uppercase(Locale.ROOT)) {
        "NATASHA" -> CharacterVoiceProfile(
            isFemale = true, basePitch = 0.94f, baseRate = 0.94f, voiceSlotIndex = 0,
            dspStyle = DspStyle.NATASHA_VELVET_INTIMATE
        )
        "WANDA" -> CharacterVoiceProfile(
            isFemale = true, basePitch = 1.01f, baseRate = 0.92f, voiceSlotIndex = 1,
            dspStyle = DspStyle.WANDA_ETHEREAL_REVERB
        )
        "STEVE" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.93f, baseRate = 0.95f, voiceSlotIndex = 0,
            dspStyle = DspStyle.TACTICAL_COMMS_WARM
        )
        "TONY" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 1.02f, baseRate = 1.05f, voiceSlotIndex = 1,
            dspStyle = DspStyle.IRON_MAN_HELMET
        )
        "THOR" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.85f, baseRate = 0.90f, voiceSlotIndex = 2,
            dspStyle = DspStyle.THOR_ASGARD_HALL
        )
        "AIDEN" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.97f, baseRate = 0.98f, voiceSlotIndex = 1,
            dspStyle = DspStyle.HERO_CINEMA
        )
        "TWIN_AIDEN", "ZERO", "SIFIR" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.88f, baseRate = 0.86f, voiceSlotIndex = 0,
            dspStyle = DspStyle.DARK_RIFT_ECHO
        )
        "ULTRON" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.80f, baseRate = 0.90f, voiceSlotIndex = 1,
            dspStyle = DspStyle.ULTRON_METAL_RESONANCE
        )
        "ARCHITECT", "MIMAR" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.78f, baseRate = 0.82f, voiceSlotIndex = 2,
            dspStyle = DspStyle.COSMIC_WARLORD_DUAL
        )
        "VISION" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.95f, baseRate = 0.91f, voiceSlotIndex = 1,
            dspStyle = DspStyle.VISION_SYNTH_CRYSTAL
        )
        "PIETRO" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 1.06f, baseRate = 1.08f, voiceSlotIndex = 0,
            dspStyle = DspStyle.TACTICAL_COMMS_WARM
        )
        "STRANGE" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.88f, baseRate = 0.91f, voiceSlotIndex = 2,
            dspStyle = DspStyle.WANDA_ETHEREAL_REVERB
        )
        "VOSS" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.87f, baseRate = 0.90f, voiceSlotIndex = 2,
            dspStyle = DspStyle.DARK_RIFT_ECHO
        )
        "MIRA" -> CharacterVoiceProfile(
            isFemale = true, basePitch = 1.07f, baseRate = 0.98f, voiceSlotIndex = 2,
            dspStyle = DspStyle.MIRA_CRYSTAL_YOUTH
        )
        "BRUCE" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.96f, baseRate = 0.93f, voiceSlotIndex = 1,
            dspStyle = DspStyle.HERO_CINEMA
        )
        "CLINT" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.94f, baseRate = 1.00f, voiceSlotIndex = 0,
            dspStyle = DspStyle.TACTICAL_COMMS_WARM
        )
        "FURY" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.84f, baseRate = 0.90f, voiceSlotIndex = 2,
            dspStyle = DspStyle.TACTICAL_COMMS_WARM
        )
        "VAELOR" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.82f, baseRate = 0.86f, voiceSlotIndex = 2,
            dspStyle = DspStyle.COSMIC_WARLORD_DUAL
        )
        else -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.98f, baseRate = 0.96f, voiceSlotIndex = 0,
            dspStyle = DspStyle.HERO_CINEMA
        )
    }

    private fun pickVoice(profile: CharacterVoiceProfile, localOnly: Boolean): Voice? {
        val pool = if (profile.isFemale) {
            if (localOnly) femaleLocalVoices.ifEmpty { localTrVoices }
            else femaleHdVoices.ifEmpty { allTrVoices }
        } else {
            if (localOnly) maleLocalVoices.ifEmpty { localTrVoices.reversed() }
            else maleHdVoices.ifEmpty { allTrVoices.reversed() }
        }
        if (pool.isEmpty()) return null
        return pool[profile.voiceSlotIndex % pool.size]
    }

    fun setMuted(isMuted: Boolean) {
        muted = isMuted
        if (isMuted) stopCurrentLine()
    }

    fun stopCurrentLine() {
        speakGeneration.incrementAndGet()
        activeDirectFallbackId = null
        activeEnvelope = null
        try {
            tts?.stop()
        } catch (_: Throwable) {
        }
        try {
            clipPlayer?.stop()
            clipPlayer?.release()
        } catch (_: Throwable) {
        }
        clipPlayer = null
        notifySpeaking(false)
    }

    /**
     * Real-time speech loudness (0f..1f) synced directly to the active MediaPlayer's exact audio
     * waveform envelope at 20Hz so 3D character mouths open and close on real spoken syllables!
     */
    fun currentSpeechAmplitude(): Float {
        if (!isSpeakingNow || muted || released) return 0f
        val player = clipPlayer
        val env = activeEnvelope
        if (player != null && env != null && env.isNotEmpty()) {
            return try {
                if (!player.isPlaying) return 0f
                val posMs = player.currentPosition.coerceAtLeast(0)
                val exactIdx = posMs / 50f
                val i0 = exactIdx.toInt().coerceIn(0, env.lastIndex)
                val i1 = (i0 + 1).coerceAtMost(env.lastIndex)
                val frac = (exactIdx - i0).coerceIn(0f, 1f)
                (env[i0] * (1f - frac) + env[i1] * frac).coerceIn(0f, 1f)
            } catch (_: Throwable) {
                0f
            }
        }
        // Natural syllabic modulation fallback when playing raw MP3 or direct device TTS
        val elapsedSec = ((System.currentTimeMillis() - directFallbackStartMs).coerceAtLeast(0L)) / 1000f
        val syllable = (0.5f + 0.5f * sin(elapsedSec * 16.5f) * cos(elapsedSec * 6.3f)).coerceIn(0f, 1f)
        val pauseGate = if (sin(elapsedSec * 2.4f) > -0.68f) 1f else 0.14f
        return (syllable * pauseGate).coerceIn(0f, 1f)
    }

    private fun notifySpeaking(speaking: Boolean) {
        isSpeakingNow = speaking
        if (!speaking) {
            activeEnvelope = null
        }
        try {
            onSpeakingStateChanged(speaking)
        } catch (_: Throwable) {
        }
    }

    /**
     * Background pre-warms Tier 2 (100% Free Neural MP3) clips for upcoming combat/cutscene lines
     * so playback triggers instantaneously with zero synthesis delay during action.
     * Falls back to Tier 3 offline DSP WAV only when completely offline.
     */
    fun prewarmLines(lines: List<Triple<String, String, String?>>) {
        if (released || lines.isEmpty()) return
        synthExecutor.execute {
            if (released) return@execute
            for ((speaker, text, lineId) in lines) {
                if (released || text.isBlank()) break
                // Tier 0 DLC voice pack: pre-master ElevenLabs MP3 through Character Cinema DSP + 20Hz lip-sync WAV cache!
                val studioHashKey = studioVoiceAssetFileName(speaker, text).removeSuffix(".mp3")
                val dlcStudio = com.example.book.BookContentPackManager.findDownloadedVoiceFile(appContext, studioHashKey)
                if (dlcStudio != null) {
                    masterStudioDlcClipIfNeeded(dlcStudio, speaker)
                    continue
                }
                if (bundledStudioVoices.contains("$studioHashKey.mp3")) continue
                if (!lineId.isNullOrBlank()) {
                    val dlcById = com.example.book.BookContentPackManager.findDownloadedVoiceFile(appContext, lineId)
                    if (dlcById != null) {
                        masterStudioDlcClipIfNeeded(dlcById, speaker)
                        continue
                    }
                    val resId = appContext.resources.getIdentifier(lineId, "raw", appContext.packageName)
                    if (resId != 0) continue
                }
                val freeNeuralMasterWav = neuralMasterWavFileFor(speaker, text)
                if (freeNeuralMasterWav.exists() && freeNeuralMasterWav.length() > 1024L) continue
                val freeNeuralCacheMp3 = neuralCacheFileFor(speaker, text, "mp3")
                if (freeNeuralCacheMp3.exists() && freeNeuralCacheMp3.length() > 1024L) continue

                val neuralFile = trySynthesizeFreeNeuralVoice(speaker, text, freeNeuralCacheMp3)
                if (neuralFile != null && neuralFile.exists() && neuralFile.length() > 1024L) {
                    pruneBookVoiceCacheIfNeeded()
                    continue
                }

                // Offline fallback: pre-warm Tier 3 studio-mastered WAV
                if (!ready) readyLatch.await(2500, TimeUnit.MILLISECONDS)
                if (!ready || released) continue
                val cacheFile = cacheFileFor(speaker, text)
                if (!cacheFile.exists() || cacheFile.length() < 1024L) {
                    synthesizeAndMasterToWav(speaker, text, cacheFile)
                    pruneBookVoiceCacheIfNeeded()
                }
            }
        }
    }

    /** Convenience single-line lookahead prewarm for instant next-line voice playback. */
    fun prewarmLine(speakerKey: String, text: String, lineId: String? = null) {
        if (released || text.isBlank()) return
        prewarmLines(listOf(Triple(speakerKey, text, lineId)))
    }

    private fun pruneBookVoiceCacheIfNeeded(maxBytes: Long = 40L * 1024L * 1024L) {
        try {
            val files = cacheDir.listFiles()?.filter { it.isFile } ?: return
            var total = files.sumOf { it.length() }
            if (total <= maxBytes) return
            files.sortedBy { it.lastModified() }.forEach { f ->
                if (total <= maxBytes) return@forEach
                val len = f.length()
                if (f.delete()) total -= len
            }
        } catch (_: Throwable) {
        }
    }

    /**
     * Speaks a character line with emotional acted cadence & cinema mastering.
     */
    fun speak(speakerKey: String, text: String, lineId: String? = null) {
        if (muted || released || text.isBlank()) return

        val myGen = speakGeneration.incrementAndGet()

        // Tier 0A: Downloaded/Imported Optional ElevenLabs Studio Voice Pack + Real-Time Character Cinema DSP & 20Hz Lip-Sync!
        val studioAssetName = studioVoiceAssetFileName(speakerKey, text)
        val studioHashKey = studioAssetName.removeSuffix(".mp3")
        val dlcStudioAudio = com.example.book.BookContentPackManager.findDownloadedVoiceFile(appContext, studioHashKey)
        if (dlcStudioAudio != null) {
            playMasteredStudioDlcClip(dlcStudioAudio, speakerKey, myGen)
            return
        }

        // Tier 0B: Downloaded/Imported Optional Voice Dub Pack matched by explicit lineId
        if (!lineId.isNullOrBlank()) {
            val dlcAudio = com.example.book.BookContentPackManager.findDownloadedVoiceFile(appContext, lineId)
            if (dlcAudio != null) {
                playMasteredStudioDlcClip(dlcAudio, speakerKey, myGen)
                return
            }
        }

        // Tier 1A: Optional bundled studio audio file in assets/studio_voices/<hash>.mp3 (if present)
        if (bundledStudioVoices.contains(studioAssetName)) {
            playBundledStudioAsset("$STUDIO_VOICES_ASSET_DIR/$studioAssetName", myGen)
            return
        }

        // Tier 1B: Bundled custom studio audio file in res/raw/<lineId>
        if (!lineId.isNullOrBlank()) {
            val resId = appContext.resources.getIdentifier(lineId, "raw", appContext.packageName)
            if (resId != 0) {
                playRawClip(resId, myGen)
                return
            }
        }

        // Fast-path: If already cached via free Edge Neural + Studio Mastering, play immediately!
        val freeNeuralMasterWav = neuralMasterWavFileFor(speakerKey, text)
        if (freeNeuralMasterWav.exists() && freeNeuralMasterWav.length() > 1024L) {
            playFileClip(freeNeuralMasterWav, myGen)
            return
        }
        val freeNeuralCacheMp3 = neuralCacheFileFor(speakerKey, text, "mp3")
        if (freeNeuralCacheMp3.exists() && freeNeuralCacheMp3.length() > 1024L) {
            playFileClip(freeNeuralCacheMp3, myGen)
            return
        }

        val offlineCacheFile = cacheFileFor(speakerKey, text)

        // Tier 2 (100% FREE, ZERO API KEY): ALWAYS try native Turkish Edge Neural TTS + Studio Mastering
        // BEFORE falling back to cached offline Android TTS, so a brief network blip never traps a line
        // on robotic offline TTS!
        synthExecutor.execute {
            if (released || muted || myGen != speakGeneration.get()) return@execute

            val freeNeuralFile = trySynthesizeFreeNeuralVoice(speakerKey, text, freeNeuralCacheMp3)
            if (released || muted || myGen != speakGeneration.get()) return@execute
            if (freeNeuralFile != null && freeNeuralFile.exists() && freeNeuralFile.length() > 1024L) {
                playFileClip(freeNeuralFile, myGen)
                return@execute
            }

            // Tier 3 (Offline-only fallback): Reuse cached offline mastered WAV if already synthesized
            if (offlineCacheFile.exists() && offlineCacheFile.length() > 1024L) {
                playFileClip(offlineCacheFile, myGen)
                return@execute
            }

            readyLatch.await(2500, TimeUnit.MILLISECONDS)
            if (!ready || released || muted || myGen != speakGeneration.get()) return@execute

            val masteredOk = synthesizeAndMasterToWav(speakerKey, text, offlineCacheFile)
            if (released || muted || myGen != speakGeneration.get()) return@execute

            if (masteredOk && offlineCacheFile.exists() && offlineCacheFile.length() > 1024L) {
                playFileClip(offlineCacheFile, myGen)
            } else {
                // Safe direct TTS fallback if OEM engine blocks file synthesis
                speakDirectFallback(speakerKey, text)
            }
        }
    }

    private fun neuralCacheFileFor(speakerKey: String, text: String, ext: String): File {
        val digest = MessageDigest.getInstance("SHA-1")
            .digest("FREE_NEURAL_TR_V8|${speakerKey.uppercase(Locale.ROOT)}|$text".toByteArray(Charsets.UTF_8))
            .take(10)
            .joinToString("") { "%02x".format(it) }
        return File(cacheDir, "fnt8_${speakerKey.lowercase(Locale.ROOT)}_$digest.$ext")
    }

    private fun neuralMasterWavFileFor(speakerKey: String, text: String): File {
        val base = neuralCacheFileFor(speakerKey, text, "mp3")
        return File(cacheDir, base.nameWithoutExtension + "_master.wav")
    }

    internal data class FreeNeuralCastSpec(
        val voiceId: String,
        val fallbackVoiceId: String,
        val isFemale: Boolean,
        val baseRate: Float,
        val basePitch: Float,
        val dominantEmotion: String,
        val ratePercentOffset: Int = 0,
        val pitchHzOffset: Int = 0
    )

    internal fun resolveFreeNeuralCast(speakerKey: String): FreeNeuralCastSpec =
        Companion.resolveFreeNeuralCast(speakerKey)

    companion object {
        const val STUDIO_VOICES_ASSET_DIR = "studio_voices"

        /**
         * Deterministic filename for pre-rendered offline ElevenLabs studio voice clips
         * bundled under `assets/studio_voices/`. Matches the generator script bit-for-bit.
         */
        fun studioVoiceAssetFileName(speakerKey: String, text: String): String {
            val sp = speakerKey.trim().uppercase(Locale.ROOT)
            val tx = text.trim()
            val payload = "STUDIO_VOICE_V1|$sp|$tx"
            val digest = MessageDigest.getInstance("SHA-1")
                .digest(payload.toByteArray(Charsets.UTF_8))
                .take(6)
                .joinToString("") { "%02x".format(it) }
            return "v1_${sp.lowercase(Locale.ROOT)}_$digest.mp3"
        }

        /**
         * Distinct Human Cinema Casting: pairs native Turkish neural voices (`tr-TR-AhmetNeural`,
         * `tr-TR-EmelNeural`) with genuinely different Microsoft MultilingualNeural studio actors
         * (all fluent in Turkish!) so every character has an unmistakably real, natural human voice
         * matching their gender, age, and personality — with subtle, organic prosody (ZERO harsh
         * pitch warping or slow-motion distortion!).
         */
        internal fun resolveFreeNeuralCast(speakerKey: String): FreeNeuralCastSpec {
            return when (speakerKey.uppercase(Locale.ROOT)) {
                "NATASHA" -> FreeNeuralCastSpec(
                    voiceId = "de-DE-SeraphinaMultilingualNeural",
                    fallbackVoiceId = "tr-TR-EmelNeural",
                    isFemale = true,
                    baseRate = 0.96f,
                    basePitch = 0.96f,
                    dominantEmotion = "romantik",
                    ratePercentOffset = -3,
                    pitchHzOffset = -2
                )
                "WANDA" -> FreeNeuralCastSpec(
                    voiceId = "en-US-AvaMultilingualNeural",
                    fallbackVoiceId = "tr-TR-EmelNeural",
                    isFemale = true,
                    baseRate = 0.95f,
                    basePitch = 1.00f,
                    dominantEmotion = "huzunlu",
                    ratePercentOffset = -4,
                    pitchHzOffset = 0
                )
                "MIRA" -> FreeNeuralCastSpec(
                    voiceId = "tr-TR-EmelNeural",
                    fallbackVoiceId = "en-US-EmmaMultilingualNeural",
                    isFemale = true,
                    baseRate = 1.02f,
                    basePitch = 1.04f,
                    dominantEmotion = "mutlu",
                    ratePercentOffset = +2,
                    pitchHzOffset = +3
                )
                "STEVE" -> FreeNeuralCastSpec(
                    voiceId = "en-US-BrianMultilingualNeural",
                    fallbackVoiceId = "tr-TR-AhmetNeural",
                    isFemale = false,
                    baseRate = 0.97f,
                    basePitch = 0.97f,
                    dominantEmotion = "kararli",
                    ratePercentOffset = -2,
                    pitchHzOffset = -1
                )
                "TONY" -> FreeNeuralCastSpec(
                    voiceId = "en-US-AndrewMultilingualNeural",
                    fallbackVoiceId = "tr-TR-AhmetNeural",
                    isFemale = false,
                    baseRate = 1.05f,
                    basePitch = 1.02f,
                    dominantEmotion = "mutlu",
                    ratePercentOffset = +5,
                    pitchHzOffset = +2
                )
                "PIETRO" -> FreeNeuralCastSpec(
                    voiceId = "fr-FR-RemyMultilingualNeural",
                    fallbackVoiceId = "tr-TR-AhmetNeural",
                    isFemale = false,
                    baseRate = 1.07f,
                    basePitch = 1.04f,
                    dominantEmotion = "mutlu",
                    ratePercentOffset = +7,
                    pitchHzOffset = +3
                )
                "VISION" -> FreeNeuralCastSpec(
                    voiceId = "en-US-BrianMultilingualNeural",
                    fallbackVoiceId = "tr-TR-AhmetNeural",
                    isFemale = false,
                    baseRate = 0.95f,
                    basePitch = 1.00f,
                    dominantEmotion = "sakin",
                    ratePercentOffset = -4,
                    pitchHzOffset = 0
                )
                "THOR" -> FreeNeuralCastSpec(
                    voiceId = "de-DE-FlorianMultilingualNeural",
                    fallbackVoiceId = "tr-TR-AhmetNeural",
                    isFemale = false,
                    baseRate = 0.94f,
                    basePitch = 0.94f,
                    dominantEmotion = "kararli",
                    ratePercentOffset = -4,
                    pitchHzOffset = -3
                )
                "VOSS" -> FreeNeuralCastSpec(
                    voiceId = "tr-TR-AhmetNeural",
                    fallbackVoiceId = "en-US-BrianMultilingualNeural",
                    isFemale = false,
                    baseRate = 0.96f,
                    basePitch = 0.94f,
                    dominantEmotion = "gergin",
                    ratePercentOffset = -3,
                    pitchHzOffset = -4
                )
                "TWIN_AIDEN", "ZERO", "SIFIR" -> FreeNeuralCastSpec(
                    // Native Turkish intense young-adult scarred Aiden — natural human cadence, zero monster pitch-drop!
                    voiceId = "tr-TR-AhmetNeural",
                    fallbackVoiceId = "en-US-AndrewMultilingualNeural",
                    isFemale = false,
                    baseRate = 0.95f,
                    basePitch = 0.96f,
                    dominantEmotion = "huzunlu",
                    ratePercentOffset = -4,
                    pitchHzOffset = -2
                )
                "ULTRON" -> FreeNeuralCastSpec(
                    voiceId = "de-DE-FlorianMultilingualNeural",
                    fallbackVoiceId = "tr-TR-AhmetNeural",
                    isFemale = false,
                    baseRate = 0.95f,
                    basePitch = 0.92f,
                    dominantEmotion = "ofkeli",
                    ratePercentOffset = -4,
                    pitchHzOffset = -4
                )
                "ARCHITECT", "MIMAR", "VAELOR" -> FreeNeuralCastSpec(
                    voiceId = "de-DE-FlorianMultilingualNeural",
                    fallbackVoiceId = "tr-TR-AhmetNeural",
                    isFemale = false,
                    baseRate = 0.92f,
                    basePitch = 0.90f,
                    dominantEmotion = "ofkeli",
                    ratePercentOffset = -6,
                    pitchHzOffset = -5
                )
                "BRUCE" -> FreeNeuralCastSpec(
                    voiceId = "en-US-BrianMultilingualNeural",
                    fallbackVoiceId = "tr-TR-AhmetNeural",
                    isFemale = false,
                    baseRate = 0.96f,
                    basePitch = 1.01f,
                    dominantEmotion = "sakin",
                    ratePercentOffset = -3,
                    pitchHzOffset = +1
                )
                "CLINT" -> FreeNeuralCastSpec(
                    voiceId = "en-US-AndrewMultilingualNeural",
                    fallbackVoiceId = "tr-TR-AhmetNeural",
                    isFemale = false,
                    baseRate = 1.00f,
                    basePitch = 0.97f,
                    dominantEmotion = "kararli",
                    ratePercentOffset = 0,
                    pitchHzOffset = -2
                )
                "FURY" -> FreeNeuralCastSpec(
                    voiceId = "de-DE-FlorianMultilingualNeural",
                    fallbackVoiceId = "tr-TR-AhmetNeural",
                    isFemale = false,
                    baseRate = 0.98f,
                    basePitch = 0.93f,
                    dominantEmotion = "kararli",
                    ratePercentOffset = -1,
                    pitchHzOffset = -4
                )
                "STRANGE" -> FreeNeuralCastSpec(
                    voiceId = "en-US-BrianMultilingualNeural",
                    fallbackVoiceId = "tr-TR-AhmetNeural",
                    isFemale = false,
                    baseRate = 0.97f,
                    basePitch = 0.96f,
                    dominantEmotion = "sakin",
                    ratePercentOffset = -2,
                    pitchHzOffset = -2
                )
                else -> FreeNeuralCastSpec(
                    voiceId = "tr-TR-AhmetNeural",
                    fallbackVoiceId = "en-US-AndrewMultilingualNeural",
                    isFemale = false,
                    baseRate = 0.99f,
                    basePitch = 1.00f,
                    dominantEmotion = "kararli",
                    ratePercentOffset = 0,
                    pitchHzOffset = 0
                )
            }
        }
    }

    /**
     * Infers subtle, human-natural expressive prosody shifts from line emotion & punctuation.
     * Strictly bounded so neural actors NEVER sound unnaturally slowed down or pitch-warped!
     */
    private fun computeLineExpressiveDeltas(text: String, cast: FreeNeuralCastSpec): Triple<Int, Int, Int> {
        val lower = text.lowercase(Locale.forLanguageTag("tr-TR"))
        var ratePct = cast.ratePercentOffset
        var pitchHz = cast.pitchHzOffset
        var volPct = 0

        val hasEllipsis = text.contains("...") || text.contains("…") || text.contains("—")
        val hasExclaim = text.contains("!")
        val hasQuestion = text.contains("?")

        when {
            lower.contains("fısılda") || lower.contains("sessiz") || lower.contains("yanındayım") ||
                lower.contains("yıldız") || lower.contains("elini") || lower.contains("öp") ||
                lower.contains("seviyorum") || lower.contains("huzur") -> {
                ratePct -= 2
                pitchHz -= 1
                volPct -= 3
            }
            lower.contains("dikkat") || lower.contains("ateş") || lower.contains("kaç") ||
                lower.contains("yok et") || lower.contains("kır") || lower.contains("parçala") ||
                lower.contains("dur!") || hasExclaim -> {
                ratePct += 3
                pitchHz += 2
                volPct += 4
            }
            lower.contains("kork") || lower.contains("üzgün") || lower.contains("yalnız") ||
                lower.contains("yara") || lower.contains("kaybet") || lower.contains("veda") ||
                lower.contains("ağla") -> {
                ratePct -= 2
                pitchHz -= 1
                volPct -= 2
            }
        }
        if (hasEllipsis) {
            ratePct -= 2
        }
        if (hasQuestion) {
            pitchHz += 1
        }
        return Triple(ratePct.coerceIn(-10, 10), pitchHz.coerceIn(-6, 6), volPct.coerceIn(-8, 8))
    }

    /**
     * Tier 2 (100% FREE, ZERO API KEY): Synthesizes expressive Turkish character dialogue
     * via [EdgeNeuralTtsClient] (primary Edge ReadAloud neural actor -> secondary fallback actor ->
     * Tier 2.5 free HTTPS Google Neural backup if WSS is blocked), mastered cleanly with real 20Hz RMS lip-sync!
     */
    private fun trySynthesizeFreeNeuralVoice(
        speakerKey: String,
        text: String,
        outMp3: File
    ): File? {
        return try {
            val cast = resolveFreeNeuralCast(speakerKey)
            val cleanedText = text
                .replace("“", "")
                .replace("”", "")
                .replace("\"", "")
                .replace("—", ", ")
                .trim()
            if (cleanedText.isBlank()) return null

            val (ratePct, pitchHz, volPct) = computeLineExpressiveDeltas(cleanedText, cast)
            val voiceCandidates = listOf(cast.voiceId, cast.fallbackVoiceId).distinct()

            var downloadedMp3: File? = null
            var usedHttpBackup = false
            for (candidateVoice in voiceCandidates) {
                val ssml = EdgeNeuralTtsClient.buildEdgeSingleProsodySsml(
                    voiceId = candidateVoice,
                    plainText = cleanedText,
                    ratePercent = ratePct,
                    pitchHz = pitchHz,
                    volumePercent = volPct
                )
                val cached = runBlocking {
                    EdgeNeuralTtsClient.synthesizeToCachedMp3(
                        context = appContext,
                        voiceId = candidateVoice,
                        ssml = ssml,
                        timeoutMs = 5_500L
                    )
                }
                if (cached != null && cached.exists() && cached.length() > 128L) {
                    downloadedMp3 = cached
                    break
                }
            }
            // Tier 2.5: If WebSocket Edge ReadAloud was unreachable on this network, fall back to
            // plain-HTTPS free Google Neural TTS BEFORE ever dropping to robotic offline Android TTS!
            if (downloadedMp3 == null) {
                val httpBackup = runBlocking {
                    EdgeNeuralTtsClient.synthesizeFreeHttpNeuralBackup(
                        context = appContext,
                        plainText = cleanedText,
                        langCode = "tr",
                        timeoutMs = 5_500L
                    )
                }
                if (httpBackup != null && httpBackup.exists() && httpBackup.length() > 256L) {
                    downloadedMp3 = httpBackup
                    usedHttpBackup = true
                }
            }
            val sourceMp3 = downloadedMp3 ?: return null

            val profile = profileFor(speakerKey)
            val masteredWav = File(cacheDir, outMp3.nameWithoutExtension + "_master.wav")
            if (masteredWav.exists() && masteredWav.length() > 1024L) return masteredWav
            val decoded = decodeCompressedAudioToMonoFloat(sourceMp3)
            if (decoded != null && decoded.samples.size > decoded.sampleRate / 12) {
                // Google HTTP TR neural voice is naturally female; apply smooth SOLA gender transposition
                // for male characters ONLY when using the HTTP backup so male heroes never sound female!
                val genderSemitones = if (usedHttpBackup && !profile.isFemale) {
                    when (profile.dspStyle) {
                        DspStyle.COSMIC_WARLORD_DUAL, DspStyle.ULTRON_METAL_RESONANCE -> -5.5f
                        DspStyle.THOR_ASGARD_HALL -> -5.0f
                        else -> -4.2f
                    }
                } else 0f
                val (left, right) = applyCharacterCinemaDsp(decoded.samples, decoded.sampleRate, profile.dspStyle, genderSemitones)
                val envelope = computeSpeechRmsEnvelope(left, right, decoded.sampleRate)
                val tmpWav = File(cacheDir, "${masteredWav.name}.${UUID.randomUUID()}.tmp")
                writeStereoWav16(tmpWav, decoded.sampleRate, left, right)
                if (!tmpWav.renameTo(masteredWav)) {
                    tmpWav.copyTo(masteredWav, overwrite = true)
                    tmpWav.delete()
                }
                if (masteredWav.exists() && masteredWav.length() > 1024L) {
                    envelopeByPath[masteredWav.absolutePath] = envelope
                    return masteredWav
                }
            }

            val tmp = File(cacheDir, "${outMp3.name}.${UUID.randomUUID()}.tmp")
            sourceMp3.copyTo(tmp, overwrite = true)
            if (!tmp.renameTo(outMp3)) {
                tmp.copyTo(outMp3, overwrite = true)
                tmp.delete()
            }
            if (outMp3.exists() && outMp3.length() > 128L) outMp3 else sourceMp3
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Decodes a cached neural MP3 clip to mono Float PCM using Android's built-in hardware/software
     * MediaExtractor + MediaCodec so we can apply Iron Man helmet comms, Sıfır's dimensional echo,
     * Ultron's vibranium resonance, and Vaelor's dual-layer warlord bass directly onto real neural speech!
     */
    private fun decodeCompressedAudioToMonoFloat(mp3File: File): DecodedWav? {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        return try {
            extractor.setDataSource(mp3File.absolutePath)
            var audioTrack = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                val mime = f.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrack = i
                    format = f
                    break
                }
            }
            if (audioTrack < 0 || format == null) return null
            extractor.selectTrack(audioTrack)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return null
            var sampleRate = if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) format.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 24000
            var channelCount = if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 1

            codec = MediaCodec.createDecoderByType(mime).apply {
                configure(format, null, null, 0)
                start()
            }
            val pcmOut = ByteArrayOutputStream(65536)
            val info = MediaCodec.BufferInfo()
            var sawInputEos = false
            var sawOutputEos = false
            val deadlineNs = System.nanoTime() + 4_000_000_000L

            while (!sawOutputEos && System.nanoTime() < deadlineNs) {
                if (!sawInputEos) {
                    val inIdx = codec.dequeueInputBuffer(10_000)
                    if (inIdx >= 0) {
                        val inBuf = codec.getInputBuffer(inIdx)
                        val sampleSize = if (inBuf != null) extractor.readSampleData(inBuf, 0) else -1
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            sawInputEos = true
                        } else {
                            codec.queueInputBuffer(inIdx, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
                val outIdx = codec.dequeueOutputBuffer(info, 10_000)
                when {
                    outIdx >= 0 -> {
                        val outBuf = codec.getOutputBuffer(outIdx)
                        if (outBuf != null && info.size > 0) {
                            val chunk = ByteArray(info.size)
                            outBuf.position(info.offset)
                            outBuf.get(chunk)
                            pcmOut.write(chunk)
                        }
                        codec.releaseOutputBuffer(outIdx, false)
                        if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            sawOutputEos = true
                        }
                    }
                    outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val nf = codec.outputFormat
                        if (nf.containsKey(MediaFormat.KEY_SAMPLE_RATE)) sampleRate = nf.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        if (nf.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) channelCount = nf.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    }
                }
            }
            val rawPcm = pcmOut.toByteArray()
            if (rawPcm.size < 256) return null
            val frameBytes = channelCount.coerceAtLeast(1) * 2
            val frames = rawPcm.size / frameBytes
            val floats = FloatArray(frames)
            val bb = ByteBuffer.wrap(rawPcm).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until frames) {
                var sum = 0f
                for (c in 0 until channelCount.coerceAtLeast(1)) {
                    sum += bb.short.toFloat() / 32768f
                }
                floats[i] = sum / channelCount.coerceAtLeast(1)
            }
            DecodedWav(sampleRate.coerceIn(8000, 48000), floats)
        } catch (_: Throwable) {
            null
        } finally {
            try { codec?.stop() } catch (_: Throwable) {}
            try { codec?.release() } catch (_: Throwable) {}
            try { extractor.release() } catch (_: Throwable) {}
        }
    }

    private fun cacheFileFor(speakerKey: String, text: String): File {
        val digest = MessageDigest.getInstance("SHA-1")
            .digest("OFFLINE_V8|${speakerKey.uppercase(Locale.ROOT)}|$text".toByteArray(Charsets.UTF_8))
            .take(8)
            .joinToString("") { "%02x".format(it) }
        return File(cacheDir, "off8_${speakerKey.lowercase(Locale.ROOT)}_$digest.wav")
    }

    /**
     * Splits Turkish dramatic dialogue into acted emotional beats so pitch, tempo, and
     * pauses breathe naturally instead of droning in flat TTS monotony.
     */
    private fun splitIntoEmotionalBeats(rawText: String, profile: CharacterVoiceProfile): List<VoiceBeat> {
        val cleaned = rawText
            .replace("“", "")
            .replace("”", "")
            .replace("\"", "")
            .trim()
        if (cleaned.isEmpty()) return emptyList()

        // Split after sentence/dramatic punctuation while keeping punctuation attached
        val regex = Regex("[^.!?—…]+(?:\\.{3}|[.!?—…]+)?")
        val rawSegments = regex.findAll(cleaned)
            .map { it.value.trim() }
            .filter { it.isNotEmpty() }
            .toList()
            .ifEmpty { listOf(cleaned) }

        return rawSegments.mapIndexed { idx, seg ->
            val isLast = idx == rawSegments.lastIndex
            val isExclaim = seg.endsWith("!")
            val isQuestion = seg.endsWith("?")
            val isDramaticPause = seg.endsWith("...") || seg.endsWith("…") || seg.endsWith("—")

            // Clean trailing em-dash so TTS doesn't pronounce punctuation oddly
            val spokenText = seg.trimEnd('—', ' ').ifEmpty { seg }

            val pitchDelta = when {
                isExclaim -> if (profile.dspStyle == DspStyle.COSMIC_WARLORD_DUAL) 0.02f else 0.045f
                isQuestion -> 0.035f
                isDramaticPause -> -0.03f
                isLast -> -0.02f
                else -> 0.0f
            }
            val rateDelta = when {
                isExclaim -> 0.055f
                isDramaticPause -> -0.065f
                isQuestion -> 0.015f
                isLast && profile.dspStyle == DspStyle.DARK_RIFT_ECHO -> -0.05f
                else -> 0.0f
            }
            val pauseMs = when {
                isLast -> 0
                isDramaticPause -> 240
                isExclaim -> 125
                isQuestion -> 175
                else -> 150
            }
            VoiceBeat(spokenText, pitchDelta, rateDelta, pauseMs)
        }
    }

    private fun synthesizeAndMasterToWav(speakerKey: String, text: String, outFile: File): Boolean {
        val engine = tts ?: return false
        val profile = profileFor(speakerKey)
        val beats = splitIntoEmotionalBeats(text, profile)
        if (beats.isEmpty()) return false

        // Try HD voice first (may be network neural); if first beat fails, switch cleanly to local HD
        var useLocalOnly = false
        var sampleRate = 24000
        val stitchedChunks = ArrayList<FloatArray>()

        for ((idx, beat) in beats.withIndex()) {
            if (released) return false
            var decoded = synthesizeSingleBeat(engine, beat, profile, localOnly = useLocalOnly)
            if (decoded == null && !useLocalOnly) {
                useLocalOnly = true
                decoded = synthesizeSingleBeat(engine, beat, profile, localOnly = true)
            }
            if (decoded == null || decoded.samples.isEmpty()) return false

            if (idx == 0) {
                sampleRate = decoded.sampleRate
            }
            val trimmed = trimSilence(decoded.samples, threshold = 0.008f, padSamples = (sampleRate * 0.025f).toInt())
            val resampled = if (decoded.sampleRate != sampleRate) {
                resampleLinear(trimmed, decoded.sampleRate.toFloat() / sampleRate.toFloat())
            } else {
                trimmed
            }
            stitchedChunks.add(resampled)
            if (beat.pauseAfterMs > 0) {
                stitchedChunks.add(FloatArray((sampleRate * beat.pauseAfterMs / 1000f).toInt()))
            }
        }

        val totalMono = stitchedChunks.sumOf { it.size }
        if (totalMono < sampleRate / 10) return false
        val mono = FloatArray(totalMono)
        var cursor = 0
        for (chunk in stitchedChunks) {
            chunk.copyInto(mono, cursor)
            cursor += chunk.size
        }

        // Detect gender mismatch when device only ships a single Turkish offline TTS voice (typically female!)
        // and apply genuine pitch/formant transposition so male characters NEVER sound female offline!
        val chosenVoice = pickVoice(profile, localOnly = useLocalOnly)
        val genderShiftSemitones = when {
            !profile.isFemale && (chosenVoice == null || !isMaleTrVoice(chosenVoice)) -> {
                when (profile.dspStyle) {
                    DspStyle.COSMIC_WARLORD_DUAL, DspStyle.ULTRON_METAL_RESONANCE -> -7.0f
                    DspStyle.DARK_RIFT_ECHO -> -6.0f
                    else -> -5.2f
                }
            }
            profile.isFemale && chosenVoice != null && isMaleTrVoice(chosenVoice) -> +4.5f
            else -> 0f
        }

        // Apply character-specific Cinema Mastering DSP (produces lush Stereo Left/Right channels!)
        val (left, right) = applyCharacterCinemaDsp(mono, sampleRate, profile.dspStyle, genderShiftSemitones)
        val envelope = computeSpeechRmsEnvelope(left, right, sampleRate)

        val tmpFile = File(cacheDir, "${outFile.name}.tmp")
        return try {
            writeStereoWav16(tmpFile, sampleRate, left, right)
            if (outFile.exists()) outFile.delete()
            val ok = tmpFile.renameTo(outFile)
            if (ok) envelopeByPath[outFile.absolutePath] = envelope
            ok
        } catch (_: Throwable) {
            tmpFile.delete()
            false
        }
    }

    private fun synthesizeSingleBeat(
        engine: TextToSpeech,
        beat: VoiceBeat,
        profile: CharacterVoiceProfile,
        localOnly: Boolean
    ): DecodedWav? {
        val voice = pickVoice(profile, localOnly = localOnly)
        val tmpWav = File(cacheDir, "beat_${UUID.randomUUID()}.wav")
        val uttId = "studio_${UUID.randomUUID()}"
        val latch = CountDownLatch(1)
        synthLatches[uttId] = latch
        synthSucceeded[uttId] = false

        return try {
            if (voice != null) {
                engine.voice = voice
            }
            val pitch = (profile.basePitch + beat.pitchDelta).coerceIn(0.72f, 1.18f)
            val rate = (profile.baseRate + beat.rateDelta).coerceIn(0.76f, 1.18f)
            engine.setPitch(pitch)
            engine.setSpeechRate(rate)
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            val code = engine.synthesizeToFile(beat.text, params, tmpWav, uttId)
            if (code != TextToSpeech.SUCCESS) {
                synthLatches.remove(uttId)
                return null
            }
            val finished = latch.await(4500, TimeUnit.MILLISECONDS)
            val ok = finished && (synthSucceeded.remove(uttId) == true)
            if (!ok || !tmpWav.exists() || tmpWav.length() < 64L) {
                null
            } else {
                decodeWavToMonoFloat(tmpWav)
            }
        } catch (_: Throwable) {
            null
        } finally {
            synthLatches.remove(uttId)
            synthSucceeded.remove(uttId)
            tmpWav.delete()
        }
    }

    private fun speakDirectFallback(speakerKey: String, text: String) {
        val engine = tts ?: return
        if (!ready || muted || released) return
        try {
            val profile = profileFor(speakerKey)
            val voice = pickVoice(profile, localOnly = true) ?: pickVoice(profile, localOnly = false)
            if (voice != null) engine.voice = voice
            engine.setPitch(profile.basePitch)
            engine.setSpeechRate(profile.baseRate)
            val uttId = "fallback_${UUID.randomUUID()}"
            activeDirectFallbackId = uttId
            directFallbackStartMs = System.currentTimeMillis()
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, uttId)
        } catch (_: Throwable) {
            activeDirectFallbackId = null
            notifySpeaking(false)
        }
    }

    /**
     * Computes a compact 20fps (50ms/frame) normalized speech loudness envelope directly from the
     * mastered stereo PCM waveform so 3D character mouths track real syllables & pauses accurately.
     */
    private fun computeSpeechRmsEnvelope(left: FloatArray, right: FloatArray, sr: Int): FloatArray {
        val total = minOf(left.size, right.size)
        val hop = (sr / 20).coerceAtLeast(1) // 50ms window
        val frameCount = ((total + hop - 1) / hop).coerceAtLeast(1)
        val env = FloatArray(frameCount)
        var peak = 0.05f
        for (f in 0 until frameCount) {
            val start = f * hop
            val end = (start + hop).coerceAtMost(total)
            var sumSq = 0.0
            for (i in start until end) {
                val m = (left[i] + right[i]) * 0.5f
                sumSq += m * m
            }
            val rms = sqrt(sumSq / (end - start).coerceAtLeast(1)).toFloat()
            env[f] = rms
            if (rms > peak) peak = rms
        }
        val invPeak = 1f / peak.coerceAtLeast(0.08f)
        for (f in 0 until frameCount) {
            val norm = (env[f] * invPeak).coerceIn(0f, 1f)
            // Soft noise gate so breaths/reverb tails keep mouth closed cleanly
            env[f] = if (norm < 0.09f) 0f else ((norm - 0.09f) / 0.91f).pow(0.85f).coerceIn(0f, 1f)
        }
        return env
    }

    // ── CINEMA VOICE MASTERING DSP CHAIN ────────────────────────────────────────

    private fun applyCharacterCinemaDsp(
        mono: FloatArray,
        sr: Int,
        style: DspStyle,
        genderShiftSemitones: Float = 0f
    ): Pair<FloatArray, FloatArray> {
        var working = mono.copyOf()
        if (abs(genderShiftSemitones) > 0.2f) {
            val pitchRatio = 2.0.pow(genderShiftSemitones / 12.0).toFloat()
            working = resamplePitchPreservingLength(working, pitchRatio)
            if (genderShiftSemitones < 0f) {
                // Warm male chest-resonance boost (180Hz body)
                val chest = working.copyOf()
                lowPassInPlace(chest, sr, 240f)
                for (i in working.indices) working[i] = working[i] * 0.90f + chest[i] * 0.35f
            }
        }

        // Step 1: High-pass rumble removal (85Hz) so voice sits cleanly above battle sub-bass
        highPassInPlace(working, sr, 85f)

        // Step 2: Character-specific tonal shaping & vocal layering
        val extraTailSamples = when (style) {
            DspStyle.COSMIC_WARLORD_DUAL -> (sr * 0.36f).toInt()
            DspStyle.WANDA_ETHEREAL_REVERB, DspStyle.THOR_ASGARD_HALL, DspStyle.VISION_SYNTH_CRYSTAL -> (sr * 0.24f).toInt()
            else -> (sr * 0.14f).toInt()
        }
        val n = working.size + extraTailSamples
        val left = FloatArray(n)
        val right = FloatArray(n)

        when (style) {
            DspStyle.NATASHA_VELVET_INTIMATE -> {
                // Natasha Romanoff: Velvety smoky alto warmth (300Hz body + smooth de-essed intimacy)
                val body = working.copyOf()
                lowPassInPlace(body, sr, 340f)
                val silk = working.copyOf()
                lowPassInPlace(silk, sr, 5200f)
                val dL = (sr * 0.017f).toInt().coerceAtLeast(1)
                val dR = (sr * 0.025f).toInt().coerceAtLeast(1)
                for (i in working.indices) {
                    val base = silk[i] * 0.92f + body[i] * 0.30f
                    val ambL = if (i >= dL) silk[i - dL] * 0.11f else 0f
                    val ambR = if (i >= dR) silk[i - dR] * 0.11f else 0f
                    left[i] = base + ambL
                    right[i] = base + ambR
                }
            }

            DspStyle.MIRA_CRYSTAL_YOUTH -> {
                // Mira (Denek 8-C): Bright youthful crystalline timbre (+1.1 semitone gentle lift + open air)
                val bright = resamplePitchPreservingLength(working, 2.0.pow(1.1 / 12.0).toFloat())
                val dL = (sr * 0.026f).toInt().coerceAtLeast(1)
                val dR = (sr * 0.039f).toInt().coerceAtLeast(1)
                for (i in bright.indices) {
                    val s = bright[i]
                    val eL = if (i >= dL) bright[i - dL] * 0.13f else 0f
                    val eR = if (i >= dR) bright[i - dR] * 0.13f else 0f
                    left[i] = s + eL
                    right[i] = s + eR
                }
            }

            DspStyle.IRON_MAN_HELMET -> {
                // Tony Stark: Iconic crisp HUD helmet comms (high-pass 240Hz + low-pass 4600Hz + 8ms metallic early reflection)
                highPassInPlace(working, sr, 240f)
                lowPassInPlace(working, sr, 4600f)
                val dL = (sr * 0.0085f).toInt().coerceAtLeast(1)
                val dR = (sr * 0.0115f).toInt().coerceAtLeast(1)
                for (i in working.indices) {
                    val s = working[i]
                    val rL = if (i >= dL) working[i - dL] * 0.22f else 0f
                    val rR = if (i >= dR) working[i - dR] * 0.20f else 0f
                    left[i] = s * 0.94f + rL
                    right[i] = s * 0.94f - rR
                }
                addSubtleCommsClick(left, right, sr)
            }

            DspStyle.TACTICAL_COMMS_WARM -> {
                // Steve & Pietro: Warm cinematic studio clarity + gentle stereo room
                val warmth = working.copyOf()
                lowPassInPlace(warmth, sr, 420f)
                val dL = (sr * 0.014f).toInt().coerceAtLeast(1)
                val dR = (sr * 0.019f).toInt().coerceAtLeast(1)
                for (i in working.indices) {
                    val base = working[i] + warmth[i] * 0.24f
                    val ambL = if (i >= dL) working[i - dL] * 0.10f else 0f
                    val ambR = if (i >= dR) working[i - dR] * 0.10f else 0f
                    left[i] = base + ambL
                    right[i] = base + ambR
                }
                addSubtleCommsClick(left, right, sr)
            }

            DspStyle.THOR_ASGARD_HALL -> {
                // Thor: Deep Asgardian baritone chest warmth + thunderous grand hall reflection
                val chest = working.copyOf()
                lowPassInPlace(chest, sr, 280f)
                val wetSrc = working.copyOf()
                lowPassInPlace(wetSrc, sr, 3200f)
                val d1 = (sr * 0.045f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.078f).toInt().coerceAtLeast(1)
                val d3 = (sr * 0.145f).toInt().coerceAtLeast(1)
                for (i in 0 until n) {
                    val dry = if (i < working.size) working[i] * 0.92f + chest[i] * 0.34f else 0f
                    val e1 = if (i in d1 until working.size + d1) wetSrc[i - d1] * 0.22f else 0f
                    val e2 = if (i in d2 until working.size + d2) wetSrc[i - d2] * 0.19f else 0f
                    val e3 = if (i in d3 until working.size + d3) wetSrc[i - d3] * 0.11f else 0f
                    left[i] = dry + e1 + e3
                    right[i] = dry + e2 + e3
                }
            }

            DspStyle.VISION_SYNTH_CRYSTAL -> {
                // Vision: Pure crystalline Mind-Stone harmonic shimmer + balanced tranquil space
                val d1 = (sr * 0.018f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.033f).toInt().coerceAtLeast(1)
                val d3 = (sr * 0.068f).toInt().coerceAtLeast(1)
                for (i in 0 until n) {
                    val dry = if (i < working.size) working[i] * 0.94f else 0f
                    val s1 = if (i in d1 until working.size + d1) working[i - d1] * 0.16f else 0f
                    val s2 = if (i in d2 until working.size + d2) working[i - d2] * 0.14f else 0f
                    val s3 = if (i in d3 until working.size + d3) working[i - d3] * 0.09f else 0f
                    left[i] = dry + s1 + s3
                    right[i] = dry + s2 + s3
                }
            }

            DspStyle.WANDA_ETHEREAL_REVERB -> {
                // Wanda & Strange: Velvet intimate vocal + shimmering stereo hall echo (38ms L / 62ms R)
                val d1 = (sr * 0.038f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.062f).toInt().coerceAtLeast(1)
                val d3 = (sr * 0.112f).toInt().coerceAtLeast(1)
                val wetSrc = working.copyOf()
                lowPassInPlace(wetSrc, sr, 3600f)
                for (i in 0 until n) {
                    val dry = if (i < working.size) working[i] else 0f
                    val e1 = if (i in d1 until working.size + d1) wetSrc[i - d1] * 0.21f else 0f
                    val e2 = if (i in d2 until working.size + d2) wetSrc[i - d2] * 0.18f else 0f
                    val e3 = if (i in d3 until working.size + d3) wetSrc[i - d3] * 0.10f else 0f
                    left[i] = dry + e1 + e3
                    right[i] = dry + e2 + e3
                }
            }

            DspStyle.DARK_RIFT_ECHO -> {
                // Twin Aiden (Sıfır): Razor-clear intimate close-mic young baritone with subtle chest warmth
                // and whisper-soft cinema night-air depth (ZERO muddy bathroom slapback echo!)
                val chest = working.copyOf()
                lowPassInPlace(chest, sr, 260f)
                val airWet = working.copyOf()
                lowPassInPlace(airWet, sr, 3400f)
                val d1 = (sr * 0.032f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.049f).toInt().coerceAtLeast(1)
                for (i in 0 until n) {
                    val dry = if (i < working.size) working[i] * 0.96f + chest[i] * 0.18f else 0f
                    val ambL = if (i in d1 until working.size + d1) airWet[i - d1] * 0.075f else 0f
                    val ambR = if (i in d2 until working.size + d2) airWet[i - d2] * 0.065f else 0f
                    left[i] = dry + ambL
                    right[i] = dry + ambR
                }
            }

            DspStyle.ULTRON_METAL_RESONANCE -> {
                // Ultron Prime: Iconic cinematic Vibranium AI resonance — cold metallic comb resonator +
                // subtle 52Hz ring-mod robotic edge + deep -1.4 semitone sub-harmonic metallic shadow!
                val subMetal = resamplePitchPreservingLength(working, 2.0.pow(-1.4 / 12.0).toFloat())
                highPassInPlace(working, sr, 150f)
                val d1 = (sr * 0.0065f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.0125f).toInt().coerceAtLeast(1)
                val d3 = (sr * 0.0210f).toInt().coerceAtLeast(1)
                val ringStep = (2.0 * PI * 52.0) / sr.toDouble()
                for (i in 0 until n) {
                    val dry = if (i < working.size) working[i] else 0f
                    val ring = dry * (0.86f + 0.14f * sin(i * ringStep).toFloat())
                    val sub = if (i < subMetal.size) subMetal[i] * 0.30f else 0f
                    val m1 = if (i in d1 until working.size + d1) working[i - d1] * 0.28f else 0f
                    val m2 = if (i in d2 until working.size + d2) -working[i - d2] * 0.22f else 0f
                    val m3 = if (i in d3 until working.size + d3) working[i - d3] * 0.14f else 0f
                    left[i] = ring * 0.82f + sub + m1 + m3
                    right[i] = ring * 0.82f + sub + m2 - m3
                }
            }

            DspStyle.COSMIC_WARLORD_DUAL -> {
                // Vaelor / Architect: Massive dual-layer cosmic cinema voice (-2.2 semitone sub-shadow + cathedral hall)
                val subShadow = resamplePitchPreservingLength(working, 2.0.pow(-2.2 / 12.0).toFloat())
                val chest = working.copyOf()
                lowPassInPlace(chest, sr, 240f)
                val wet = working.copyOf()
                lowPassInPlace(wet, sr, 2800f)
                val d1 = (sr * 0.046f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.084f).toInt().coerceAtLeast(1)
                val d3 = (sr * 0.138f).toInt().coerceAtLeast(1)
                for (i in 0 until n) {
                    val dry = if (i < working.size) working[i] * 0.86f + chest[i] * 0.26f else 0f
                    val sub = if (i < subShadow.size) subShadow[i] * 0.26f else 0f
                    val e1 = if (i in d1 until working.size + d1) wet[i - d1] * 0.18f else 0f
                    val e2 = if (i in d2 until working.size + d2) wet[i - d2] * 0.15f else 0f
                    val e3 = if (i in d3 until working.size + d3) wet[i - d3] * 0.09f else 0f
                    left[i] = dry + sub + e1 + e3
                    right[i] = dry + sub + e2 + e3
                }
            }

            DspStyle.HERO_CINEMA -> {
                // Aiden / Hero: Crisp close-mic cinema presence + subtle heroic chest warmth
                val warmth = working.copyOf()
                lowPassInPlace(warmth, sr, 340f)
                val d1 = (sr * 0.020f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.029f).toInt().coerceAtLeast(1)
                for (i in working.indices) {
                    val base = working[i] + warmth[i] * 0.18f
                    val rL = if (i >= d1) working[i - d1] * 0.08f else 0f
                    val rR = if (i >= d2) working[i - d2] * 0.08f else 0f
                    left[i] = base + rL
                    right[i] = base + rR
                }
            }
        }

        // Step 3: Transparent Cinema RMS Auto-Leveling + Clean Soft-Knee Peak Limiter (zero vocal distortion!)
        var sumSq = 0.0
        for (i in 0 until n) {
            val v = (left[i] + right[i]) * 0.5f
            sumSq += v * v
        }
        val rms = sqrt(sumSq / n.coerceAtLeast(1)).toFloat().coerceAtLeast(0.01f)
        val makeupGain = (0.24f / rms).coerceIn(0.65f, 2.35f)
        fun softLimit(x: Float): Float {
            val ax = abs(x)
            if (ax <= 0.78f) return x
            val excess = (ax - 0.78f) / 0.22f
            val shaped = 0.78f + 0.18f * tanh(excess)
            return if (x >= 0f) shaped else -shaped
        }
        for (i in 0 until n) {
            left[i] = softLimit(left[i] * makeupGain)
            right[i] = softLimit(right[i] * makeupGain)
        }

        return Pair(left, right)
    }

    private fun addSubtleCommsClick(left: FloatArray, right: FloatArray, sr: Int) {
        val clickLen = (sr * 0.026f).toInt().coerceAtMost(left.size / 4)
        for (i in 0 until clickLen) {
            val t = i.toFloat() / sr
            val env = exp(-t * 140f)
            val chirp = sin(2.0 * PI * (1480.0 + t * 18000.0) * t).toFloat() * env * 0.16f
            left[i] += chirp
            right[i] += chirp
        }
    }

    private fun highPassInPlace(buf: FloatArray, sr: Int, cutoffHz: Float) {
        val rc = 1f / (2f * PI.toFloat() * cutoffHz)
        val dt = 1f / sr
        val alpha = rc / (rc + dt)
        var prevOut = 0f
        var prevIn = 0f
        for (i in buf.indices) {
            val x = buf[i]
            val y = alpha * (prevOut + x - prevIn)
            prevIn = x
            prevOut = y
            buf[i] = y
        }
    }

    private fun lowPassInPlace(buf: FloatArray, sr: Int, cutoffHz: Float) {
        val rc = 1f / (2f * PI.toFloat() * cutoffHz)
        val dt = 1f / sr
        val alpha = dt / (rc + dt)
        var prev = 0f
        for (i in buf.indices) {
            prev += alpha * (buf[i] - prev)
            buf[i] = prev
        }
    }

    private fun trimSilence(samples: FloatArray, threshold: Float, padSamples: Int): FloatArray {
        var start = 0
        while (start < samples.size && abs(samples[start]) < threshold) start++
        var end = samples.size - 1
        while (end > start && abs(samples[end]) < threshold) end--
        val s = (start - padSamples).coerceAtLeast(0)
        val e = (end + padSamples).coerceAtMost(samples.size - 1)
        return if (e <= s) samples else samples.copyOfRange(s, e + 1)
    }

    private fun resampleLinear(src: FloatArray, ratio: Float): FloatArray {
        val outLen = (src.size / ratio).toInt().coerceAtLeast(1)
        val out = FloatArray(outLen)
        for (i in 0 until outLen) {
            val pos = i * ratio
            val i0 = pos.toInt().coerceIn(0, src.lastIndex)
            val i1 = (i0 + 1).coerceAtMost(src.lastIndex)
            val frac = pos - i0
            out[i] = src[i0] * (1f - frac) + src[i1] * frac
        }
        return out
    }

    /**
     * Smooth SOLA (Synchronized Overlap-Add) pitch shifter preserving duration with phase alignment!
     * Cross-correlates each overlapping frame against the existing output tail to eliminate the
     * 46Hz metallic chopping / Dalek artifacts of naive overlap-add pitch shifters.
     */
    private fun resamplePitchPreservingLength(src: FloatArray, pitchRatio: Float): FloatArray {
        val n = src.size
        if (n < 512) return src.copyOf()
        val resampled = resampleLinear(src, pitchRatio)
        val stretchRatio = resampled.size.toFloat() / n.toFloat()
        if (abs(stretchRatio - 1f) < 0.01f) return resampled.copyOf(n)

        val frameLen = 768
        val ovLen = 256
        val outHop = frameLen - ovLen
        val searchMax = 128
        val out = FloatArray(n)
        val firstCopy = minOf(frameLen, resampled.size, n)
        for (i in 0 until firstCopy) out[i] = resampled[i]

        var outPos = outHop
        while (outPos + ovLen < n) {
            val targetInPos = (outPos * stretchRatio).toInt().coerceIn(0, (resampled.size - frameLen).coerceAtLeast(0))
            // Search best phase-aligned offset via normalized cross-correlation over overlap window
            var bestLag = 0
            var bestScore = Float.NEGATIVE_INFINITY
            val maxLag = minOf(searchMax, (resampled.size - targetInPos - frameLen).coerceAtLeast(0))
            val minLag = -minOf(searchMax / 2, targetInPos)
            var lag = minLag
            while (lag <= maxLag) {
                var corr = 0f
                var k = 0
                while (k < ovLen) {
                    corr += out[outPos + k] * resampled[targetInPos + lag + k]
                    k += 2
                }
                if (corr > bestScore) {
                    bestScore = corr
                    bestLag = lag
                }
                lag += 2
            }
            val srcStart = (targetInPos + bestLag).coerceIn(0, (resampled.size - 1).coerceAtLeast(0))
            for (k in 0 until frameLen) {
                val dstIdx = outPos + k
                val srcIdx = srcStart + k
                if (dstIdx >= n || srcIdx >= resampled.size) break
                if (k < ovLen) {
                    val fade = k.toFloat() / ovLen.toFloat()
                    out[dstIdx] = out[dstIdx] * (1f - fade) + resampled[srcIdx] * fade
                } else {
                    out[dstIdx] = resampled[srcIdx]
                }
            }
            outPos += outHop
        }
        return out
    }

    // ── WAV I/O (Chunk-Safe RIFF Reader & Stereo 16-bit Writer) ─────────────────

    private fun decodeWavToMonoFloat(file: File): DecodedWav? {
        val bytes = file.readBytes()
        if (bytes.size < 44) return null
        val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        if (bytes[0] != 'R'.code.toByte() || bytes[1] != 'I'.code.toByte()) return null

        var channels = 1
        var sampleRate = 24000
        var bitsPerSample = 16
        var dataOffset = -1
        var dataLength = 0

        var pos = 12
        while (pos + 8 <= bytes.size) {
            val id = String(bytes, pos, 4, Charsets.US_ASCII)
            val chunkSize = bb.getInt(pos + 4).coerceAtLeast(0)
            val chunkDataStart = pos + 8
            if (id == "fmt " && chunkDataStart + 16 <= bytes.size) {
                channels = bb.getShort(chunkDataStart + 2).toInt().coerceAtLeast(1)
                sampleRate = bb.getInt(chunkDataStart + 4).coerceIn(8000, 48000)
                bitsPerSample = bb.getShort(chunkDataStart + 14).toInt()
            } else if (id == "data") {
                dataOffset = chunkDataStart
                dataLength = chunkSize.coerceAtMost(bytes.size - chunkDataStart)
                break
            }
            pos = chunkDataStart + chunkSize + (chunkSize and 1)
        }

        if (dataOffset < 0 || dataLength < 4 || bitsPerSample != 16) return null
        val frameBytes = channels * 2
        val frameCount = dataLength / frameBytes
        val out = FloatArray(frameCount)
        var p = dataOffset
        for (i in 0 until frameCount) {
            var sum = 0f
            for (c in 0 until channels) {
                sum += bb.getShort(p).toFloat() / 32768f
                p += 2
            }
            out[i] = sum / channels
        }
        return DecodedWav(sampleRate, out)
    }

    private fun writeStereoWav16(file: File, sampleRate: Int, left: FloatArray, right: FloatArray) {
        val frames = minOf(left.size, right.size)
        val dataBytes = frames * 4
        val baos = ByteArrayOutputStream(44 + dataBytes)
        val hdr = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        hdr.put("RIFF".toByteArray(Charsets.US_ASCII))
        hdr.putInt(36 + dataBytes)
        hdr.put("WAVE".toByteArray(Charsets.US_ASCII))
        hdr.put("fmt ".toByteArray(Charsets.US_ASCII))
        hdr.putInt(16)
        hdr.putShort(1) // PCM
        hdr.putShort(2) // Stereo
        hdr.putInt(sampleRate)
        hdr.putInt(sampleRate * 4)
        hdr.putShort(4)
        hdr.putShort(16)
        hdr.put("data".toByteArray(Charsets.US_ASCII))
        hdr.putInt(dataBytes)
        baos.write(hdr.array())

        val pcm = ByteBuffer.allocate(dataBytes).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until frames) {
            pcm.putShort((left[i] * 32767f).toInt().coerceIn(-32767, 32767).toShort())
            pcm.putShort((right[i] * 32767f).toInt().coerceIn(-32767, 32767).toShort())
        }
        baos.write(pcm.array())
        FileOutputStream(file).use { it.write(baos.toByteArray()) }
    }

    // ── MEDIA PLAYER PLAYBACK WITH VOICE DUCKING ────────────────────────────────

    private fun masteredDlcWavFileFor(dlcFile: File, speakerKey: String): File {
        val styleTag = profileFor(speakerKey).dspStyle.name.lowercase(Locale.ROOT)
        return File(cacheDir, "dlc_m3_${dlcFile.nameWithoutExtension}_${dlcFile.length()}_$styleTag.wav")
    }

    private fun masterStudioDlcClipIfNeeded(dlcFile: File, speakerKey: String): File {
        val masteredWav = masteredDlcWavFileFor(dlcFile, speakerKey)
        if (masteredWav.exists() && masteredWav.length() > 1024L) return masteredWav
        return try {
            val decoded = decodeCompressedAudioToMonoFloat(dlcFile)
            if (decoded != null && decoded.samples.size > decoded.sampleRate / 12) {
                val profile = profileFor(speakerKey)
                val (left, right) = applyCharacterCinemaDsp(decoded.samples, decoded.sampleRate, profile.dspStyle, 0f)
                writeStereoWav16(masteredWav, decoded.sampleRate, left, right)
                envelopeByPath[masteredWav.absolutePath] = computeSpeechRmsEnvelope(left, right, decoded.sampleRate)
                pruneBookVoiceCacheIfNeeded()
                masteredWav
            } else {
                dlcFile
            }
        } catch (_: Throwable) {
            dlcFile
        }
    }

    private fun playMasteredStudioDlcClip(dlcFile: File, speakerKey: String, expectedGen: Int) {
        if (muted || released || expectedGen != speakGeneration.get()) return
        val cachedMaster = masteredDlcWavFileFor(dlcFile, speakerKey)
        if (cachedMaster.exists() && cachedMaster.length() > 1024L) {
            playFileClip(cachedMaster, expectedGen)
            return
        }
        synthExecutor.execute {
            if (muted || released || expectedGen != speakGeneration.get()) return@execute
            val readyFile = masterStudioDlcClipIfNeeded(dlcFile, speakerKey)
            if (muted || released || expectedGen != speakGeneration.get()) return@execute
            playFileClip(readyFile, expectedGen)
        }
    }

    @Synchronized
    private fun playBundledStudioAsset(assetPath: String, expectedGen: Int) {
        if (muted || released || expectedGen != speakGeneration.get()) return
        try {
            try {
                clipPlayer?.stop()
                clipPlayer?.release()
            } catch (_: Throwable) {}
            clipPlayer = null
            activeEnvelope = null

            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            var preparedViaFd = false
            try {
                appContext.assets.openFd(assetPath).use { afd ->
                    mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                }
                preparedViaFd = true
            } catch (_: Throwable) {
                // Fallback if APK compressed the asset: extract once to cacheDir and play from file
                val cached = File(cacheDir, assetPath.substringAfterLast('/'))
                if (!cached.exists() || cached.length() < 1024L) {
                    appContext.assets.open(assetPath).use { input ->
                        FileOutputStream(cached).use { output -> input.copyTo(output) }
                    }
                }
                mp.setDataSource(cached.absolutePath)
                preparedViaFd = true
            }
            if (!preparedViaFd) {
                mp.release()
                return
            }
            mp.setOnCompletionListener { player ->
                try { player.release() } catch (_: Throwable) {}
                if (clipPlayer === player) {
                    clipPlayer = null
                    notifySpeaking(false)
                }
            }
            mp.setOnErrorListener { player, _, _ ->
                try { player.release() } catch (_: Throwable) {}
                if (clipPlayer === player) {
                    clipPlayer = null
                    notifySpeaking(false)
                }
                true
            }
            mp.prepare()
            mp.setVolume(1.0f, 1.0f)
            directFallbackStartMs = System.currentTimeMillis()
            clipPlayer = mp
            notifySpeaking(true)
            mp.start()
        } catch (_: Throwable) {
            notifySpeaking(false)
        }
    }

    private fun playRawClip(resId: Int, expectedGen: Int) {
        if (muted || released || expectedGen != speakGeneration.get()) return
        try {
            try {
                clipPlayer?.stop()
                clipPlayer?.release()
            } catch (_: Throwable) {}
            clipPlayer = null
            activeEnvelope = null
            val afd = appContext.resources.openRawResourceFd(resId) ?: return
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            afd.use { mp.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            mp.setOnCompletionListener { player ->
                try { player.release() } catch (_: Throwable) {}
                if (clipPlayer === player) {
                    clipPlayer = null
                    notifySpeaking(false)
                }
            }
            mp.setOnErrorListener { player, _, _ ->
                try { player.release() } catch (_: Throwable) {}
                if (clipPlayer === player) {
                    clipPlayer = null
                    notifySpeaking(false)
                }
                true
            }
            mp.prepare()
            mp.setVolume(1.0f, 1.0f)
            directFallbackStartMs = System.currentTimeMillis()
            clipPlayer = mp
            notifySpeaking(true)
            mp.start()
        } catch (_: Throwable) {
            notifySpeaking(false)
        }
    }

    @Synchronized
    private fun playFileClip(file: File, expectedGen: Int) {
        if (muted || released || expectedGen != speakGeneration.get()) return
        try {
            try {
                clipPlayer?.stop()
                clipPlayer?.release()
            } catch (_: Throwable) {
            }
            clipPlayer = null

            // Load or lazily compute 20Hz RMS lip-sync envelope for cached WAV files
            var env = envelopeByPath[file.absolutePath]
            if (env == null && file.name.endsWith(".wav", ignoreCase = true)) {
                val decoded = decodeWavToMonoFloat(file)
                if (decoded != null && decoded.samples.isNotEmpty()) {
                    env = computeSpeechRmsEnvelope(decoded.samples, decoded.samples, decoded.sampleRate)
                    envelopeByPath[file.absolutePath] = env
                }
            }
            activeEnvelope = env

            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            mp.setDataSource(file.absolutePath)
            mp.setOnCompletionListener { player ->
                try {
                    player.release()
                } catch (_: Throwable) {
                }
                if (clipPlayer === player) {
                    clipPlayer = null
                    notifySpeaking(false)
                }
            }
            mp.setOnErrorListener { player, _, _ ->
                try {
                    player.release()
                } catch (_: Throwable) {
                }
                if (clipPlayer === player) {
                    clipPlayer = null
                    notifySpeaking(false)
                }
                true
            }
            mp.prepare()
            mp.setVolume(1.0f, 1.0f)
            directFallbackStartMs = System.currentTimeMillis()
            clipPlayer = mp
            notifySpeaking(true)
            mp.start()
        } catch (_: Throwable) {
            activeEnvelope = null
            notifySpeaking(false)
        }
    }

    fun stopAndRelease() {
        released = true
        stopCurrentLine()
        try {
            synthExecutor.shutdownNow()
        } catch (_: Throwable) {
        }
        try {
            tts?.shutdown()
        } catch (_: Throwable) {
        }
        tts = null
        ready = false
    }
}
