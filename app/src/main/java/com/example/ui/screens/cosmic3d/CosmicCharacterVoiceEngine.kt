package com.example.ui.screens.cosmic3d

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Base64
import com.example.data.local.AppDatabase
import com.example.util.SecureApiKeyVault
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
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
        TACTICAL_COMMS_WARM,   // Natasha, Steve, Pietro
        IRON_MAN_HELMET,       // Tony
        ETHEREAL_REVERB,       // Wanda, Strange
        HERO_CINEMA,           // Aiden, Thor, Vision
        DARK_RIFT_ECHO,        // Twin Aiden (Sıfır)
        ULTRON_METAL_RESONANCE,// Ultron Prime (cold vibranium baritone resonance)
        COSMIC_WARLORD_DUAL    // Vaelor & Asıl Mimar (dual-layer sub-harmonic + cathedral reverb)
    }

    private data class DecodedWav(
        val sampleRate: Int,
        val samples: FloatArray // mono normalized -1..1
    )

    private val appContext = context.applicationContext
    private val cacheDir = File(appContext.cacheDir, "cosmic_studio_voices_v3").apply { mkdirs() }
    private val synthExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "CosmicVoice-StudioMaster").apply { isDaemon = true }
    }
    private val synthLatches = ConcurrentHashMap<String, CountDownLatch>()
    private val synthSucceeded = ConcurrentHashMap<String, Boolean>()
    private val speakGeneration = AtomicInteger(0)

    @Volatile private var tts: TextToSpeech? = null
    @Volatile private var clipPlayer: MediaPlayer? = null
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
                override fun onStart(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) {
                    if (utteranceId != null) {
                        synthSucceeded[utteranceId] = true
                        synthLatches.remove(utteranceId)?.countDown()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId != null) {
                        synthSucceeded[utteranceId] = false
                        synthLatches.remove(utteranceId)?.countDown()
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    if (utteranceId != null) {
                        synthSucceeded[utteranceId] = false
                        synthLatches.remove(utteranceId)?.countDown()
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
            isFemale = true, basePitch = 0.96f, baseRate = 0.95f, voiceSlotIndex = 0,
            dspStyle = DspStyle.TACTICAL_COMMS_WARM
        )
        "WANDA" -> CharacterVoiceProfile(
            isFemale = true, basePitch = 1.03f, baseRate = 0.92f, voiceSlotIndex = 1,
            dspStyle = DspStyle.ETHEREAL_REVERB
        )
        "STEVE" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.92f, baseRate = 0.94f, voiceSlotIndex = 0,
            dspStyle = DspStyle.TACTICAL_COMMS_WARM
        )
        "TONY" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 1.01f, baseRate = 1.04f, voiceSlotIndex = 1,
            dspStyle = DspStyle.IRON_MAN_HELMET
        )
        "THOR" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.86f, baseRate = 0.90f, voiceSlotIndex = 2,
            dspStyle = DspStyle.HERO_CINEMA
        )
        "AIDEN" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.96f, baseRate = 0.97f, voiceSlotIndex = 1,
            dspStyle = DspStyle.HERO_CINEMA
        )
        "TWIN_AIDEN", "ZERO", "SIFIR" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.89f, baseRate = 0.85f, voiceSlotIndex = 0,
            dspStyle = DspStyle.DARK_RIFT_ECHO
        )
        "ULTRON" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.80f, baseRate = 0.90f, voiceSlotIndex = 1,
            dspStyle = DspStyle.ULTRON_METAL_RESONANCE
        )
        "ARCHITECT", "MIMAR" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.78f, baseRate = 0.80f, voiceSlotIndex = 2,
            dspStyle = DspStyle.COSMIC_WARLORD_DUAL
        )
        "VISION" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.94f, baseRate = 0.90f, voiceSlotIndex = 1,
            dspStyle = DspStyle.HERO_CINEMA
        )
        "PIETRO" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 1.05f, baseRate = 1.06f, voiceSlotIndex = 0,
            dspStyle = DspStyle.TACTICAL_COMMS_WARM
        )
        "STRANGE" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.88f, baseRate = 0.91f, voiceSlotIndex = 2,
            dspStyle = DspStyle.ETHEREAL_REVERB
        )
        "VOSS" -> CharacterVoiceProfile(
            isFemale = false, basePitch = 0.87f, baseRate = 0.90f, voiceSlotIndex = 2,
            dspStyle = DspStyle.DARK_RIFT_ECHO
        )
        "MIRA" -> CharacterVoiceProfile(
            isFemale = true, basePitch = 1.07f, baseRate = 0.97f, voiceSlotIndex = 2,
            dspStyle = DspStyle.ETHEREAL_REVERB
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

    private fun notifySpeaking(speaking: Boolean) {
        try {
            onSpeakingStateChanged(speaking)
        } catch (_: Throwable) {
        }
    }

    /**
     * Background pre-warms studio-mastered WAV clips for upcoming combat/cutscene lines
     * so playback triggers instantaneously with zero synthesis delay during action.
     */
    fun prewarmLines(lines: List<Triple<String, String, String?>>) {
        if (released || lines.isEmpty()) return
        synthExecutor.execute {
            if (released) return@execute
            readyLatch.await(3, TimeUnit.SECONDS)
            if (!ready || released) return@execute
            for ((speaker, text, lineId) in lines) {
                if (released) break
                if (!lineId.isNullOrBlank()) {
                    val resId = appContext.resources.getIdentifier(lineId, "raw", appContext.packageName)
                    if (resId != 0) continue
                }
                val cacheFile = cacheFileFor(speaker, text)
                if (!cacheFile.exists() || cacheFile.length() < 1024L) {
                    synthesizeAndMasterToWav(speaker, text, cacheFile)
                }
            }
        }
    }

    /**
     * Speaks a character line with emotional acted cadence & cinema mastering.
     */
    fun speak(speakerKey: String, text: String, lineId: String? = null) {
        if (muted || released || text.isBlank()) return

        val myGen = speakGeneration.incrementAndGet()

        // Tier 0: Downloaded/Imported Optional Voice Dub Pack (.ogg/.mp3/.m4a/.wav)
        if (!lineId.isNullOrBlank()) {
            val dlcAudio = com.example.book.BookContentPackManager.findDownloadedVoiceFile(appContext, lineId)
            if (dlcAudio != null) {
                playFileClip(dlcAudio, myGen)
                return
            }
        }

        // Tier 1: Bundled custom studio audio file in res/raw/<lineId>
        if (!lineId.isNullOrBlank()) {
            val resId = appContext.resources.getIdentifier(lineId, "raw", appContext.packageName)
            if (resId != 0) {
                playRawClip(resId)
                return
            }
        }

        val neuralCacheMp3 = neuralCacheFileFor(speakerKey, text, "mp3")
        if (neuralCacheMp3.exists() && neuralCacheMp3.length() > 1024L) {
            playFileClip(neuralCacheMp3, myGen)
            return
        }
        val neuralCacheWav = neuralCacheFileFor(speakerKey, text, "wav")
        if (neuralCacheWav.exists() && neuralCacheWav.length() > 1024L) {
            playFileClip(neuralCacheWav, myGen)
            return
        }

        val cacheFile = cacheFileFor(speakerKey, text)

        // Fast-path: If already studio-mastered in disk cache, play immediately!
        if (cacheFile.exists() && cacheFile.length() > 1024L) {
            playFileClip(cacheFile, myGen)
            return
        }

        // Synthesize REAL Neural Cinema AI Voice (OpenAI gpt-4o-mini-tts / Gemini 2.5 Flash TTS) first!
        // Falls back cleanly to multi-beat Studio DSP offline engine only when no API key is configured or offline.
        synthExecutor.execute {
            if (released || muted || myGen != speakGeneration.get()) return@execute

            val neuralFile = trySynthesizeNeuralCinemaVoice(speakerKey, text, neuralCacheMp3, neuralCacheWav)
            if (released || muted || myGen != speakGeneration.get()) return@execute
            if (neuralFile != null && neuralFile.exists() && neuralFile.length() > 1024L) {
                playFileClip(neuralFile, myGen)
                return@execute
            }

            readyLatch.await(2500, TimeUnit.MILLISECONDS)
            if (!ready || released || muted || myGen != speakGeneration.get()) return@execute

            val masteredOk = synthesizeAndMasterToWav(speakerKey, text, cacheFile)
            if (released || muted || myGen != speakGeneration.get()) return@execute

            if (masteredOk && cacheFile.exists() && cacheFile.length() > 1024L) {
                playFileClip(cacheFile, myGen)
            } else {
                // Safe direct TTS fallback if OEM engine blocks file synthesis
                speakDirectFallback(speakerKey, text)
            }
        }
    }

    private fun neuralCacheFileFor(speakerKey: String, text: String, ext: String): File {
        val digest = MessageDigest.getInstance("SHA-1")
            .digest("NEURAL_V4|${speakerKey.uppercase(Locale.ROOT)}|$text".toByteArray(Charsets.UTF_8))
            .take(10)
            .joinToString("") { "%02x".format(it) }
        return File(cacheDir, "ai_${speakerKey.lowercase(Locale.ROOT)}_$digest.$ext")
    }

    /**
     * Tier 2: REAL Acted Neural Cinema Voice Acting via official documented APIs using the user's
     * own saved OpenAI (`gpt-4o-mini-tts`) or Gemini (`gemini-2.5-flash-preview-tts`) key from Settings!
     * Eliminates flat robotic Android/Google Translate TTS completely and caches audio offline forever.
     */
    private fun trySynthesizeNeuralCinemaVoice(
        speakerKey: String,
        text: String,
        outMp3: File,
        outWav: File
    ): File? {
        val (openaiKey, geminiKey) = resolveUserApiKeys()
        if (openaiKey.isNotBlank()) {
            if (synthesizeViaOpenAiNeuralTts(openaiKey, speakerKey, text, outMp3)) return outMp3
        }
        if (geminiKey.isNotBlank()) {
            if (synthesizeViaGeminiNeuralTts(geminiKey, speakerKey, text, outWav)) return outWav
        }
        return null
    }

    private fun resolveUserApiKeys(): Pair<String, String> {
        return try {
            val rawSettings = runBlocking {
                AppDatabase.getInstance(appContext).userSettingsDao().getUserSettings()
            } ?: return "" to ""
            val decrypted = SecureApiKeyVault.decryptSettingsKeys(rawSettings)
            val openai = decrypted.openaiApiKey.trim()
            val gemini = decrypted.customApiKey.trim().takeIf { it.startsWith("AIza") }
                ?: decrypted.backupApiKey.trim().takeIf { it.startsWith("AIza") }
                ?: ""
            openai to gemini
        } catch (_: Throwable) {
            "" to ""
        }
    }

    private fun actingDirectionFor(speakerKey: String): Triple<String, String, String> {
        // Triple(openAiVoice, geminiVoice, turkishActingDirection)
        return when (speakerKey.uppercase(Locale.ROOT)) {
            "NATASHA" -> Triple(
                "nova", "Aoede",
                "Doğal, kadifemsi, duygulu ve samimi bir kadın sinema seslendirmeni gibi konuş. Robotik olma, nefes al ve hafif fısıltılı sıcak bir ton kullan."
            )
            "WANDA" -> Triple(
                "shimmer", "Kore",
                "Duygusal, içten, hafif kırılgan ama derin bir genç kadın sinema oyuncusu gibi Türkçe dublaj yap. Gerçek insan duygusu ve nefes durakları olsun."
            )
            "MIRA" -> Triple(
                "coral", "Leda",
                "Samimi, umutlu, sıcak ve doğal bir genç kız sinema dublajı gibi konuş."
            )
            "ULTRON" -> Triple(
                "onyx", "Fenrir",
                "Soğuk, zeki, alaycı ve ağır bas tonlu bir sinema kötü karakteri gibi konuş. Dramatik vurgular yap."
            )
            "ARCHITECT", "MIMAR", "VAELOR" -> Triple(
                "onyx", "Fenrir",
                "Kadim, ürpertici derecede sakin, derin bariton ve otoriter bir kozmik varlık gibi ağır tempolu konuş."
            )
            "TWIN_AIDEN", "ZERO", "SIFIR", "VOSS" -> Triple(
                "echo", "Charon",
                "Yorgun, derin, yaralı ama karizmatik ve içten bir erkek sinema başrolü gibi konuş. Gerçek acı ve samimiyet hissedilsin."
            )
            "VISION" -> Triple(
                "sage", "Zephyr",
                "Dingin, bilge, sıcak ve berrak bir sinema karakteri gibi doğal ve akıcı konuş."
            )
            "TONY" -> Triple(
                "ballad", "Orus",
                "Zeki, esprili, hızlı ve karizmatik bir erkek sinema karakteri gibi doğal Türkçe dublaj yap."
            )
            "PIETRO" -> Triple(
                "ash", "Puck",
                "Enerjik, atik, alaycı ama kalbi sıcak bir genç erkek sinema karakteri gibi dinamik konuş."
            )
            else -> Triple(
                "ash", "Puck",
                "Karizmatik, doğal, duygulu ve gerçekçi bir Türk sinema dublaj sanatçısı gibi konuş. Asla mekanik veya yapay okuma."
            )
        }
    }

    private fun synthesizeViaOpenAiNeuralTts(
        apiKey: String,
        speakerKey: String,
        text: String,
        outMp3: File
    ): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            val (voice, _, direction) = actingDirectionFor(speakerKey)
            val body = JSONObject().apply {
                put("model", "gpt-4o-mini-tts")
                put("voice", voice)
                put("input", text)
                put("instructions", direction)
                put("response_format", "mp3")
            }.toString()

            conn = (URL("https://api.openai.com/v1/audio/speech").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 9_000
                readTimeout = 18_000
                doOutput = true
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Content-Type", "application/json")
            }
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode !in 200..299) return false

            val tmp = File(cacheDir, "${outMp3.name}.tmp")
            conn.inputStream.use { inp ->
                FileOutputStream(tmp).use { out -> inp.copyTo(out) }
            }
            if (tmp.length() > 1024L) {
                tmp.renameTo(outMp3)
                true
            } else {
                tmp.delete()
                false
            }
        } catch (_: Throwable) {
            false
        } finally {
            try { conn?.disconnect() } catch (_: Throwable) {}
        }
    }

    private fun synthesizeViaGeminiNeuralTts(
        apiKey: String,
        speakerKey: String,
        text: String,
        outWav: File
    ): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            val (_, geminiVoice, direction) = actingDirectionFor(speakerKey)
            val payload = JSONObject().apply {
                put(
                    "contents",
                    JSONArray().put(
                        JSONObject().put(
                            "parts",
                            JSONArray().put(
                                JSONObject().put("text", "[$direction]\n$text")
                            )
                        )
                    )
                )
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("responseModalities", JSONArray().put("AUDIO"))
                        put(
                            "speechConfig",
                            JSONObject().put(
                                "voiceConfig",
                                JSONObject().put(
                                    "prebuiltVoiceConfig",
                                    JSONObject().put("voiceName", geminiVoice)
                                )
                            )
                        )
                    }
                )
            }.toString()

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-tts:generateContent?key=$apiKey"
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 20_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode !in 200..299) return false

            val respText = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(respText)
            val b64 = root.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optJSONObject("inlineData")
                ?.optString("data")
                .orEmpty()
            if (b64.isBlank()) return false

            val pcmBytes = Base64.decode(b64, Base64.DEFAULT)
            if (pcmBytes.size < 2048) return false

            // Convert 24kHz 16-bit mono PCM from Gemini TTS -> Studio Mastered Stereo WAV!
            val sampleCount = pcmBytes.size / 2
            val floats = FloatArray(sampleCount)
            val bb = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until sampleCount) {
                floats[i] = bb.short / 32768f
            }
            val profile = profileFor(speakerKey)
            val (left, right) = applyCharacterCinemaDsp(floats, 24_000, profile.dspStyle)
            writeStereoWav16(outWav, 24_000, left, right)
            outWav.exists() && outWav.length() > 1024L
        } catch (_: Throwable) {
            false
        } finally {
            try { conn?.disconnect() } catch (_: Throwable) {}
        }
    }

    private fun cacheFileFor(speakerKey: String, text: String): File {
        val digest = MessageDigest.getInstance("SHA-1")
            .digest("${speakerKey.uppercase(Locale.ROOT)}|$text".toByteArray(Charsets.UTF_8))
            .take(8)
            .joinToString("") { "%02x".format(it) }
        return File(cacheDir, "v3_${speakerKey.lowercase(Locale.ROOT)}_$digest.wav")
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

        // Apply character-specific Cinema Mastering DSP (produces lush Stereo Left/Right channels!)
        val (left, right) = applyCharacterCinemaDsp(mono, sampleRate, profile.dspStyle)

        val tmpFile = File(cacheDir, "${outFile.name}.tmp")
        return try {
            writeStereoWav16(tmpFile, sampleRate, left, right)
            if (outFile.exists()) outFile.delete()
            tmpFile.renameTo(outFile)
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
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fallback_${UUID.randomUUID()}")
        } catch (_: Throwable) {
        }
    }

    // ── CINEMA VOICE MASTERING DSP CHAIN ────────────────────────────────────────

    private fun applyCharacterCinemaDsp(
        mono: FloatArray,
        sr: Int,
        style: DspStyle
    ): Pair<FloatArray, FloatArray> {
        val working = mono.copyOf()

        // Step 1: High-pass rumble removal (85Hz) so voice sits cleanly above battle sub-bass
        highPassInPlace(working, sr, 85f)

        // Step 2: Character-specific tonal shaping & vocal layering
        val extraTailSamples = when (style) {
            DspStyle.DARK_RIFT_ECHO -> (sr * 0.55f).toInt()
            DspStyle.COSMIC_WARLORD_DUAL -> (sr * 0.45f).toInt()
            DspStyle.ETHEREAL_REVERB -> (sr * 0.35f).toInt()
            else -> (sr * 0.12f).toInt()
        }
        val n = working.size + extraTailSamples
        val left = FloatArray(n)
        val right = FloatArray(n)

        when (style) {
            DspStyle.IRON_MAN_HELMET -> {
                // Tony Stark: Iconic crisp HUD helmet comms (high-pass 260Hz + low-pass 4600Hz + 8ms metallic early reflection)
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
                // Natasha & Steve: Warm cinematic studio clarity + gentle comms air + subtle stereo room
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

            DspStyle.ETHEREAL_REVERB -> {
                // Wanda: Velvet intimate vocal + shimmering stereo hall echo (38ms L / 62ms R)
                val d1 = (sr * 0.038f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.062f).toInt().coerceAtLeast(1)
                val d3 = (sr * 0.112f).toInt().coerceAtLeast(1)
                val wetSrc = working.copyOf()
                lowPassInPlace(wetSrc, sr, 3600f)
                for (i in 0 until n) {
                    val dry = if (i < working.size) working[i] else 0f
                    val e1 = if (i in d1 until working.size + d1) wetSrc[i - d1] * 0.20f else 0f
                    val e2 = if (i in d2 until working.size + d2) wetSrc[i - d2] * 0.17f else 0f
                    val e3 = if (i in d3 until working.size + d3) wetSrc[i - d3] * 0.09f else 0f
                    left[i] = dry + e1 + e3
                    right[i] = dry + e2 + e3
                }
            }

            DspStyle.DARK_RIFT_ECHO -> {
                // Twin Aiden (Sıfır): Haunting dark-future weight + dimensional rift dual stereo echo!
                val darkWet = working.copyOf()
                lowPassInPlace(darkWet, sr, 2400f)
                val d1 = (sr * 0.085f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.155f).toInt().coerceAtLeast(1)
                val d3 = (sr * 0.265f).toInt().coerceAtLeast(1)
                for (i in 0 until n) {
                    val dry = if (i < working.size) working[i] else 0f
                    val e1 = if (i in d1 until working.size + d1) darkWet[i - d1] * 0.27f else 0f
                    val e2 = if (i in d2 until working.size + d2) darkWet[i - d2] * 0.21f else 0f
                    val e3 = if (i in d3 until working.size + d3) darkWet[i - d3] * 0.12f else 0f
                    left[i] = dry * 0.96f + e1 + e3
                    right[i] = dry * 0.96f + e2 + e3 * 0.8f
                }
            }

            DspStyle.ULTRON_METAL_RESONANCE -> {
                // Ultron Prime: Cold metallic Vibranium chassis resonance + crisp harmonic ring
                val sub = resamplePitchPreservingLength(working, 2.0.pow(-1.8 / 12.0).toFloat())
                val d1 = (sr * 0.011f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.023f).toInt().coerceAtLeast(1)
                val d3 = (sr * 0.068f).toInt().coerceAtLeast(1)
                for (i in 0 until n) {
                    val dry = if (i < working.size) working[i] * 0.86f + sub[i] * 0.28f else 0f
                    val m1 = if (i in d1 until working.size + d1) working[i - d1] * 0.24f else 0f
                    val m2 = if (i in d2 until working.size + d2) -working[i - d2] * 0.16f else 0f
                    val r3 = if (i in d3 until working.size + d3) working[i - d3] * 0.14f else 0f
                    left[i] = dry + m1 + r3
                    right[i] = dry + m2 + r3 * 0.9f
                }
            }

            DspStyle.COSMIC_WARLORD_DUAL -> {
                // Vaelor: Massive movie-villain layering (-3.5 semitone sub-voice harmonic blend + cathedral reverb!)
                val subLayer = resamplePitchPreservingLength(working, 2.0.pow(-3.5 / 12.0).toFloat())
                lowPassInPlace(subLayer, sr, 1900f)
                val combined = FloatArray(working.size) { i ->
                    working[i] * 0.84f + subLayer[i] * 0.36f
                }
                val wet = combined.copyOf()
                lowPassInPlace(wet, sr, 2900f)
                val d1 = (sr * 0.058f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.098f).toInt().coerceAtLeast(1)
                val d3 = (sr * 0.175f).toInt().coerceAtLeast(1)
                for (i in 0 until n) {
                    val dry = if (i < combined.size) combined[i] else 0f
                    val e1 = if (i in d1 until combined.size + d1) wet[i - d1] * 0.24f else 0f
                    val e2 = if (i in d2 until combined.size + d2) wet[i - d2] * 0.22f else 0f
                    val e3 = if (i in d3 until combined.size + d3) wet[i - d3] * 0.12f else 0f
                    left[i] = dry + e1 + e3
                    right[i] = dry + e2 + e3
                }
            }

            DspStyle.HERO_CINEMA -> {
                // Aiden / Thor: Crisp close-mic cinema presence + subtle heroic chest warmth
                val warmth = working.copyOf()
                lowPassInPlace(warmth, sr, 360f)
                val d1 = (sr * 0.022f).toInt().coerceAtLeast(1)
                val d2 = (sr * 0.031f).toInt().coerceAtLeast(1)
                for (i in working.indices) {
                    val base = working[i] + warmth[i] * 0.22f
                    val rL = if (i >= d1) working[i - d1] * 0.12f else 0f
                    val rR = if (i >= d2) working[i - d2] * 0.12f else 0f
                    left[i] = base + rL
                    right[i] = base + rR
                }
            }
        }

        // Step 3: Cinema RMS Auto-Leveling + Warm Analog Soft-Saturation Limiter (punchy over battle SFX!)
        var sumSq = 0.0
        for (i in 0 until n) {
            val v = (left[i] + right[i]) * 0.5f
            sumSq += v * v
        }
        val rms = sqrt(sumSq / n.coerceAtLeast(1)).toFloat().coerceAtLeast(0.01f)
        val makeupGain = (0.28f / rms).coerceIn(0.65f, 2.8f)
        for (i in 0 until n) {
            left[i] = tanh(left[i] * makeupGain * 1.18f) * 0.92f
            right[i] = tanh(right[i] * makeupGain * 1.18f) * 0.92f
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
     * Granular Hann-windowed pitch shift keeping exact original length — creates Vaelor's
     * deep cosmic dual-voice sub-harmonic without desyncing timing!
     */
    private fun resamplePitchPreservingLength(src: FloatArray, pitchRatio: Float): FloatArray {
        val n = src.size
        val out = FloatArray(n)
        val win = 1024
        val hop = win / 2
        val hann = FloatArray(win) { k -> (0.5 * (1.0 - cos(2.0 * PI * k / (win - 1)))).toFloat() }
        var pos = 0
        while (pos < n) {
            for (k in 0 until win) {
                val outIdx = pos + k
                if (outIdx >= n) break
                val srcPos = pos + k * pitchRatio
                val i0 = srcPos.toInt()
                if (i0 in 0 until n - 1) {
                    val frac = srcPos - i0
                    val sample = src[i0] * (1f - frac) + src[i0 + 1] * frac
                    out[outIdx] += sample * hann[k]
                }
            }
            pos += hop
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

    private fun playRawClip(resId: Int) {
        try {
            stopCurrentLine()
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
                try {
                    player.release()
                } catch (_: Throwable) {
                }
                if (clipPlayer === player) {
                    clipPlayer = null
                    notifySpeaking(false)
                }
            }
            mp.prepare()
            mp.setVolume(1.0f, 1.0f)
            notifySpeaking(true)
            mp.start()
            clipPlayer = mp
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
            notifySpeaking(true)
            mp.start()
            clipPlayer = mp
        } catch (_: Throwable) {
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
