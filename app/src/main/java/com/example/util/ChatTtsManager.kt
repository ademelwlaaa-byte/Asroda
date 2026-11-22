package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.data.local.EmotionState
import com.example.data.repository.EmochiRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

/**
 * 10/10 2-Tier Emotional Neural + Enhanced Device TTS Engine for Emochi.
 *
 * - Layer 1 (Default, Free, 0 MB APK bloat): Microsoft Edge Neural TTS via [EdgeNeuralTtsClient]
 *   with live [EmotionState] & literary dialogue/narration SSML prosody ([EmotionalTtsProsodyEngine]),
 *   15 MB LRU MP3 disk cache, and seamless multi-batch [MediaPlayer] playback.
 * - Layer 2 (Offline / Silent Automatic Fallback): High-quality Android [TextToSpeech] voices
 *   (`QUALITY_VERY_HIGH`, `tr-tr-x-*-network`, `tr-tr-x-ama-local` prioritized) with per-segment
 *   dynamic rate/pitch and `playSilentUtterance` breath pauses.
 * - Cold-start safety: requests arriving before `TextToSpeech.onInit` finishes are queued in
 *   [pendingSpeakRequest] and played automatically on ready.
 * - Zero stuck equalizer guarantee: done/error/stop/audio-focus-loss/onStop always resets
 *   [speakingMessageId], [isSpeaking], releases [MediaPlayer], and abandons audio focus.
 * - Bidirectional [BookAudioController] deconfliction.
 */
class ChatTtsManager(context: Context) {

    data class TtsVoiceOption(
        val name: String,
        val displayName: String,
        val localeTag: String,
        val isTurkish: Boolean,
        val isNetwork: Boolean,
        val qualityScore: Int
    )

    private data class PendingSpeakRequest(
        val messageId: String?,
        val rawText: String,
        val speed: Float,
        val pitch: Float,
        val voiceName: String,
        val languageTag: String,
        val readActions: Boolean,
        val emotionState: EmotionState?
    )

    private val appContext: Context = context.applicationContext
    private val audioManager: AudioManager? =
        appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val ttsScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var tts: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null
    private var activeNeuralJob: Job? = null

    @Volatile
    private var isInitialized: Boolean = false
    @Volatile
    private var activeSessionId: String? = null
    @Volatile
    private var activeFinalUtteranceId: String? = null
    @Volatile
    private var pendingSpeakRequest: PendingSpeakRequest? = null

    private var audioFocusRequest: AudioFocusRequest? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<TtsVoiceOption>>(emptyList())
    val availableVoices: StateFlow<List<TtsVoiceOption>> = _availableVoices.asStateFlow()

    private val _isTurkishVoiceMissing = MutableStateFlow(false)
    val isTurkishVoiceMissing: StateFlow<Boolean> = _isTurkishVoiceMissing.asStateFlow()

    /** Diagnostic/test indicator of which layer served the most recent utterance ("neural" | "device" | "none"). */
    @Volatile
    var lastActiveTier: String = "none"
        private set

    /** Optional test hook to play synthesized MP3 files without real Android MediaPlayer hardware. */
    @Volatile
    internal var playNeuralFileOverride: (suspend (File) -> Boolean)? = null

    /** Optional test hook to bypass Android TextToSpeech binder calls in isolated unit tests. */
    @Volatile
    internal var deviceSpeakOverride: ((List<EmotionalTtsProsodyEngine.ProsodySegment>, String) -> Boolean)? = null

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                stop()
            }
        }
    }

    init {
        BookAudioController.onPlaybackStarted = {
            if (_isSpeaking.value) {
                stop()
            }
        }
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    configureEngine()
                    flushPendingSpeakRequest()
                }
            }
        } catch (_: Exception) {
            isInitialized = false
        }
    }

    private fun configureEngine() {
        val engine = tts ?: return
        try {
            engine.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            val result = engine.setLanguage(Locale.forLanguageTag("tr-TR"))
            val trMissing = result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED
            _isTurkishVoiceMissing.value = trMissing
            if (trMissing) {
                engine.setLanguage(Locale.getDefault())
            }
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    val session = activeSessionId
                    if (utteranceId != null && session != null && utteranceId.startsWith(session)) {
                        _isSpeaking.value = true
                    }
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId != null && utteranceId == activeFinalUtteranceId) {
                        clearSpeakingState()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    val session = activeSessionId
                    if (utteranceId == null || (session != null && utteranceId.startsWith(session))) {
                        clearSpeakingState()
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    val session = activeSessionId
                    if (utteranceId == null || (session != null && utteranceId.startsWith(session))) {
                        clearSpeakingState()
                    }
                }
            })
            refreshVoices()
        } catch (_: Exception) {
        }
    }

    private fun flushPendingSpeakRequest() {
        val req = pendingSpeakRequest ?: return
        pendingSpeakRequest = null
        speakWithDeviceEngine(
            messageId = req.messageId,
            rawText = req.rawText,
            speed = req.speed,
            pitch = req.pitch,
            voiceName = req.voiceName,
            languageTag = req.languageTag,
            readActions = req.readActions,
            emotionState = req.emotionState
        )
    }

    /** Test helper to simulate `TextToSpeech.OnInitListener(SUCCESS)` firing after cold-start. */
    internal fun simulateInitCompleteForTest() {
        isInitialized = true
        flushPendingSpeakRequest()
    }

    /** Test helper to simulate `UtteranceProgressListener.onError(...)` and verify equalizer resets. */
    internal fun simulateUtteranceErrorForTest() {
        clearSpeakingState()
    }

    fun refreshVoices() {
        val engine = tts ?: return
        if (!isInitialized) return
        try {
            val rawVoices: Set<Voice> = engine.voices ?: emptySet()
            val filtered = rawVoices
                .filter { v ->
                    val lang = v.locale?.language?.lowercase(Locale.ROOT) ?: ""
                    lang == "tr" || lang == "en"
                }
                .mapIndexed { idx, v ->
                    val lang = v.locale?.language?.lowercase(Locale.ROOT) ?: ""
                    val isTr = lang == "tr"
                    val isNet = v.isNetworkConnectionRequired
                    val country = v.locale?.country?.uppercase(Locale.ROOT).orEmpty()
                    val langLabel = if (isTr) "Türkçe" else "İngilizce"
                    val regionSuffix = if (country.isNotBlank() && country != "TR") " ($country)" else ""
                    val qualityTag = when {
                        v.quality >= Voice.QUALITY_VERY_HIGH -> "HD+"
                        v.quality >= Voice.QUALITY_HIGH -> "HD"
                        else -> ""
                    }
                    val shortCode = v.name
                        .substringAfterLast('#')
                        .substringAfterLast('-')
                        .replace("-local", "")
                        .replace("-network", "")
                        .take(10)
                        .ifBlank { "${idx + 1}" }
                    val label = buildString {
                        append(langLabel)
                        append(regionSuffix)
                        append(" • ")
                        append(shortCode.uppercase(Locale.ROOT))
                        if (qualityTag.isNotBlank()) {
                            append(" ")
                            append(qualityTag)
                        }
                    }
                    TtsVoiceOption(
                        name = v.name,
                        displayName = label,
                        localeTag = v.locale?.toLanguageTag().orEmpty(),
                        isTurkish = isTr,
                        isNetwork = isNet,
                        qualityScore = computeEnhancedVoiceScore(v.name, v.quality, isTr, isNet)
                    )
                }
                .sortedWith(
                    compareByDescending<TtsVoiceOption> { it.isTurkish }
                        .thenByDescending { it.qualityScore }
                        .thenBy { it.name }
                )
            _availableVoices.value = filtered
            if (filtered.none { it.isTurkish }) {
                val avail = try { engine.isLanguageAvailable(Locale.forLanguageTag("tr-TR")) } catch (_: Exception) { TextToSpeech.LANG_AVAILABLE }
                _isTurkishVoiceMissing.value = avail == TextToSpeech.LANG_MISSING_DATA || avail == TextToSpeech.LANG_NOT_SUPPORTED
            } else {
                _isTurkishVoiceMissing.value = false
            }
        } catch (_: Exception) {
        }
    }

    fun speakMessage(
        messageId: String?,
        rawText: String,
        speed: Float = 1.0f,
        pitch: Float = 1.0f,
        voiceName: String = "",
        languageTag: String = "tr",
        readActions: Boolean = AppFeatureFlags.isTtsReadActionsEnabled,
        emotionState: EmotionState? = null
    ): Boolean {
        val segments = EmotionalTtsProsodyEngine.buildProsodySegments(
            rawText = rawText,
            emotionState = emotionState,
            baseSpeed = speed,
            basePitch = pitch,
            readActions = readActions,
            emotionalProsodyEnabled = AppFeatureFlags.isTtsEmotionalProsodyEnabled,
            maxChunkChars = DEFAULT_MAX_CHUNK_CHARS
        )
        if (segments.isEmpty()) {
            stop()
            return false
        }

        // Prevent clash with BookReaderView chapter soundtrack
        BookAudioController.pauseIfPlaying()

        // Cancel any previous playback before starting the new message
        cancelActivePlaybackOnly()

        val useNeural = AppFeatureFlags.getTtsEngineMode() == AppFeatureFlags.TTS_ENGINE_NEURAL
        if (useNeural) {
            startNeuralPlaybackWithSilentFallback(
                messageId = messageId,
                rawText = rawText,
                segments = segments,
                speed = speed,
                pitch = pitch,
                deviceVoiceName = voiceName,
                languageTag = languageTag,
                readActions = readActions,
                emotionState = emotionState
            )
            return true
        }

        return speakWithDeviceEngine(
            messageId = messageId,
            rawText = rawText,
            speed = speed,
            pitch = pitch,
            voiceName = voiceName,
            languageTag = languageTag,
            readActions = readActions,
            emotionState = emotionState,
            precomputedSegments = segments
        )
    }

    private fun startNeuralPlaybackWithSilentFallback(
        messageId: String?,
        rawText: String,
        segments: List<EmotionalTtsProsodyEngine.ProsodySegment>,
        speed: Float,
        pitch: Float,
        deviceVoiceName: String,
        languageTag: String,
        readActions: Boolean,
        emotionState: EmotionState?
    ) {
        val sessionId = "emochi_neural_${UUID.randomUUID().toString().take(8)}"
        activeSessionId = sessionId
        activeFinalUtteranceId = null
        pendingSpeakRequest = null
        _speakingMessageId.value = messageId
        _isSpeaking.value = true
        requestAudioFocus()

        val preferredNeuralVoice = resolveNeuralVoiceId(languageTag)
        val batches = EmotionalTtsProsodyEngine.batchSegmentsForNeural(segments)

        activeNeuralJob = ttsScope.launch {
            var anyPlayed = false
            try {
                for (batch in batches) {
                    if (activeSessionId != sessionId) return@launch
                    val ssml = EmotionalTtsProsodyEngine.buildSsml(
                        voiceName = preferredNeuralVoice,
                        segments = batch,
                        xmlLang = if (preferredNeuralVoice.startsWith("en-", ignoreCase = true)) "en-US" else "tr-TR"
                    )
                    val mp3File = EdgeNeuralTtsClient.synthesizeToCachedMp3(
                        context = appContext,
                        voiceId = preferredNeuralVoice,
                        ssml = ssml
                    )
                    if (activeSessionId != sessionId) return@launch

                    if (mp3File == null) {
                        if (!anyPlayed) {
                            // Zero-interruption silent fallback to Layer 2 (Enhanced Device TTS + Prosody)
                            speakWithDeviceEngine(
                                messageId = messageId,
                                rawText = rawText,
                                speed = speed,
                                pitch = pitch,
                                voiceName = deviceVoiceName,
                                languageTag = languageTag,
                                readActions = readActions,
                                emotionState = emotionState,
                                precomputedSegments = segments
                            )
                            return@launch
                        } else {
                            break
                        }
                    }

                    lastActiveTier = "neural"
                    val playedOk = playMp3FileBlocking(mp3File, sessionId)
                    if (!playedOk) {
                        if (!anyPlayed && activeSessionId == sessionId) {
                            speakWithDeviceEngine(
                                messageId = messageId,
                                rawText = rawText,
                                speed = speed,
                                pitch = pitch,
                                voiceName = deviceVoiceName,
                                languageTag = languageTag,
                                readActions = readActions,
                                emotionState = emotionState,
                                precomputedSegments = segments
                            )
                            return@launch
                        }
                        break
                    }
                    anyPlayed = true
                }
                if (activeSessionId == sessionId) {
                    clearSpeakingState()
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Exception) {
                if (!anyPlayed && activeSessionId == sessionId) {
                    speakWithDeviceEngine(
                        messageId = messageId,
                        rawText = rawText,
                        speed = speed,
                        pitch = pitch,
                        voiceName = deviceVoiceName,
                        languageTag = languageTag,
                        readActions = readActions,
                        emotionState = emotionState,
                        precomputedSegments = segments
                    )
                } else if (activeSessionId == sessionId) {
                    clearSpeakingState()
                }
            }
        }
    }

    private suspend fun playMp3FileBlocking(file: File, sessionId: String): Boolean {
        val overridePlayer = playNeuralFileOverride
        if (overridePlayer != null) {
            return try {
                overridePlayer(file)
            } catch (_: Exception) {
                false
            }
        }
        return withContext(Dispatchers.Main.immediate) {
            if (activeSessionId != sessionId) return@withContext false
            suspendCancellableCoroutine { cont ->
                releaseMediaPlayer()
                val player = MediaPlayer()
                mediaPlayer = player
                try {
                    player.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    player.setDataSource(file.absolutePath)
                    player.setOnCompletionListener {
                        releaseMediaPlayer()
                        if (cont.isActive) cont.resume(true)
                    }
                    player.setOnErrorListener { _, _, _ ->
                        releaseMediaPlayer()
                        if (cont.isActive) cont.resume(false)
                        true
                    }
                    player.setOnPreparedListener { mp ->
                        if (activeSessionId == sessionId && cont.isActive) {
                            mp.start()
                        } else {
                            releaseMediaPlayer()
                            if (cont.isActive) cont.resume(false)
                        }
                    }
                    cont.invokeOnCancellation {
                        releaseMediaPlayer()
                    }
                    player.prepareAsync()
                } catch (_: Exception) {
                    releaseMediaPlayer()
                    if (cont.isActive) cont.resume(false)
                }
            }
        }
    }

    private fun speakWithDeviceEngine(
        messageId: String?,
        rawText: String,
        speed: Float,
        pitch: Float,
        voiceName: String,
        languageTag: String,
        readActions: Boolean,
        emotionState: EmotionState?,
        precomputedSegments: List<EmotionalTtsProsodyEngine.ProsodySegment>? = null
    ): Boolean {
        val segments = precomputedSegments ?: EmotionalTtsProsodyEngine.buildProsodySegments(
            rawText = rawText,
            emotionState = emotionState,
            baseSpeed = speed,
            basePitch = pitch,
            readActions = readActions,
            emotionalProsodyEnabled = AppFeatureFlags.isTtsEmotionalProsodyEnabled,
            maxChunkChars = DEFAULT_MAX_CHUNK_CHARS
        )
        if (segments.isEmpty()) {
            clearSpeakingState()
            return false
        }

        // Cold-start race protection: queue until onInit(SUCCESS) completes
        if (!isInitialized) {
            pendingSpeakRequest = PendingSpeakRequest(
                messageId = messageId,
                rawText = rawText,
                speed = speed,
                pitch = pitch,
                voiceName = voiceName,
                languageTag = languageTag,
                readActions = readActions,
                emotionState = emotionState
            )
            _speakingMessageId.value = messageId
            _isSpeaking.value = true
            return true
        }

        val sessionId = "emochi_tts_${UUID.randomUUID().toString().take(8)}"
        activeSessionId = sessionId
        activeFinalUtteranceId = "${sessionId}_${segments.lastIndex}"
        _speakingMessageId.value = messageId
        _isSpeaking.value = true
        lastActiveTier = "device"
        requestAudioFocus()

        val testOverride = deviceSpeakOverride
        if (testOverride != null) {
            val ok = testOverride(segments, sessionId)
            if (!ok) clearSpeakingState()
            return ok
        }

        val engine = tts
        if (engine == null) {
            clearSpeakingState()
            return false
        }

        applyBaseVoiceSelection(engine, voiceName, languageTag)

        segments.forEachIndexed { idx, seg ->
            val queueMode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val utteranceId = "${sessionId}_$idx"
            try {
                engine.setSpeechRate(seg.effectiveAndroidRate)
                engine.setPitch(seg.effectiveAndroidPitch)
                engine.speak(seg.text, queueMode, null, utteranceId)
                if (idx < segments.lastIndex && seg.pauseAfterMs > 0L) {
                    engine.playSilentUtterance(
                        seg.pauseAfterMs,
                        TextToSpeech.QUEUE_ADD,
                        "${sessionId}_pause_$idx"
                    )
                }
            } catch (_: Exception) {
                clearSpeakingState()
                return false
            }
        }
        return true
    }

    fun previewVoice(
        voiceName: String,
        speed: Float = 1.0f,
        pitch: Float = 1.0f,
        languageTag: String = "tr",
        emotionState: EmotionState? = null,
        neuralVoiceId: String? = null
    ): Boolean {
        if (!neuralVoiceId.isNullOrBlank()) {
            AppFeatureFlags.setTtsNeuralVoice(neuralVoiceId)
        }
        val sampleText = if (languageTag.lowercase(Locale.ROOT).startsWith("en")) {
            "*She leans closer by the violet window...* \"Hello! This is how Emochi reads stories with real emotion.\""
        } else {
            DEFAULT_PREVIEW_SAMPLE_TR
        }
        return speakMessage(
            messageId = PREVIEW_MESSAGE_ID,
            rawText = sampleText,
            speed = speed,
            pitch = pitch,
            voiceName = voiceName,
            languageTag = languageTag,
            readActions = true,
            emotionState = emotionState ?: EmotionState(
                primaryEmotions = com.example.data.local.PrimaryEmotions(joy = 60, trust = 75),
                relationshipAxes = com.example.data.local.RelationshipAxes(affectionScore = 82),
                dominantEmotion = "romantik"
            )
        )
    }

    fun stop() {
        cancelActivePlaybackOnly()
        clearSpeakingState()
    }

    fun shutdown() {
        stop()
        BookAudioController.onPlaybackStarted = null
        try {
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
        isInitialized = false
        try {
            ttsScope.cancel()
        } catch (_: Exception) {
        }
    }

    private fun cancelActivePlaybackOnly() {
        pendingSpeakRequest = null
        activeNeuralJob?.cancel()
        activeNeuralJob = null
        releaseMediaPlayer()
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
    }

    private fun releaseMediaPlayer() {
        val mp = mediaPlayer ?: return
        mediaPlayer = null
        try {
            if (mp.isPlaying) mp.stop()
        } catch (_: Exception) {
        }
        try {
            mp.reset()
        } catch (_: Exception) {
        }
        try {
            mp.release()
        } catch (_: Exception) {
        }
    }

    private fun resolveNeuralVoiceId(languageTag: String): String {
        val selected = AppFeatureFlags.getTtsNeuralVoice()
        if (selected.isNotBlank()) return selected
        return if (languageTag.lowercase(Locale.ROOT).startsWith("en")) {
            "en-US-AvaMultilingualNeural"
        } else {
            AppFeatureFlags.DEFAULT_NEURAL_VOICE
        }
    }

    private fun applyBaseVoiceSelection(
        engine: TextToSpeech,
        voiceName: String,
        languageTag: String = "tr"
    ) {
        try {
            val allVoices = engine.voices ?: emptySet()
            if (voiceName.isNotBlank()) {
                val chosen = allVoices.firstOrNull { it.name == voiceName }
                if (chosen != null) {
                    engine.voice = chosen
                    return
                }
            }
            val wantEnglish = languageTag.lowercase(Locale.ROOT).startsWith("en")
            val targetLang = if (wantEnglish) "en" else "tr"
            val bestInstalled = allVoices
                .filter { it.locale?.language?.lowercase(Locale.ROOT) == targetLang }
                .maxByOrNull { v ->
                    computeEnhancedVoiceScore(
                        name = v.name,
                        quality = v.quality,
                        isTurkish = targetLang == "tr",
                        isNetwork = v.isNetworkConnectionRequired
                    )
                }
            if (bestInstalled != null) {
                engine.voice = bestInstalled
                return
            }
            val preferredLocale = if (wantEnglish) Locale.US else Locale.forLanguageTag("tr-TR")
            val result = engine.setLanguage(preferredLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.getDefault())
            }
        } catch (_: Exception) {
        }
    }

    private fun requestAudioFocus() {
        val am = audioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .setWillPauseWhenDucked(false)
                    .build()
                audioFocusRequest = req
                am.requestAudioFocus(req)
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(focusChangeListener)
            }
        } catch (_: Exception) {
        }
    }

    private fun clearSpeakingState() {
        activeSessionId = null
        activeFinalUtteranceId = null
        pendingSpeakRequest = null
        _isSpeaking.value = false
        _speakingMessageId.value = null
        abandonAudioFocus()
    }

    companion object {
        const val DEFAULT_MAX_CHUNK_CHARS = 350
        const val PREVIEW_MESSAGE_ID = "__tts_voice_preview__"
        const val DEFAULT_PREVIEW_SAMPLE_TR =
            "*Gece rüzgârı mor salkımların arasından usulca süzülür...* \"Beni burada beklediğini biliyordum! Hikâyemiz tam şimdi başlıyor.\""

        private val MULTILINE_ACTION_REGEX = Regex("\\*.*?\\*", RegexOption.DOT_MATCHES_ALL)
        private val MARKDOWN_SYMBOL_REGEX = Regex("[#`~_>]+")
        private val EMOJI_AND_SYMBOL_REGEX = Regex("[\\p{So}\\p{Cs}\\uFE0F\\u200D]+")
        private val WHITESPACE_REGEX = Regex("\\s+")
        private val SENTENCE_BOUNDARY_REGEX = Regex("""(?<=[.!?…])\s+""")

        /**
         * Ranks Android device voices so Google's high-quality network/modern Turkish voices
         * (`QUALITY_VERY_HIGH`, `tr-tr-x-*-network`, `tr-tr-x-ama-local`) always beat legacy robotic voices.
         */
        fun computeEnhancedVoiceScore(
            name: String,
            quality: Int,
            isTurkish: Boolean,
            isNetwork: Boolean
        ): Int {
            val lower = name.lowercase(Locale.ROOT)
            var score = quality
            if (quality >= Voice.QUALITY_VERY_HIGH) score += 250
            if (isTurkish) {
                if (lower.contains("tr-tr-x-") && lower.contains("network")) score += 400
                if (lower.contains("tr-tr-x-ama-local") || lower.contains("tr-tr-x-efu")) score += 320
                if (lower.contains("tr-tr-x-")) score += 180
            } else {
                if (lower.contains("-network")) score += 160
            }
            if (isNetwork && quality >= Voice.QUALITY_HIGH) score += 100
            return score
        }

        /**
         * Strips technical tags via [EmochiRepository.cleanEmotionTags], handles `*action*` blocks
         * (including multi-line action blocks via DOT_MATCHES_ALL), and removes markdown/emojis.
         */
        fun prepareTtsText(rawText: String, readActions: Boolean = false): String {
            if (rawText.isBlank()) return ""
            var cleaned = EmochiRepository.cleanEmotionTags(rawText)
            cleaned = if (readActions) {
                cleaned.replace("*", "")
            } else {
                cleaned.replace(MULTILINE_ACTION_REGEX, " ")
            }
            cleaned = cleaned
                .replace(MARKDOWN_SYMBOL_REGEX, " ")
                .replace(EMOJI_AND_SYMBOL_REGEX, " ")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace(WHITESPACE_REGEX, " ")
                .trim()

            if (cleaned.isBlank() && !readActions) {
                val fallback = EmochiRepository.cleanEmotionTags(rawText)
                    .replace("*", "")
                    .replace(MARKDOWN_SYMBOL_REGEX, " ")
                    .replace(EMOJI_AND_SYMBOL_REGEX, " ")
                    .replace(WHITESPACE_REGEX, " ")
                    .trim()
                return fallback
            }
            return cleaned
        }

        /**
         * Splits cleaned narrative text at sentence boundaries into chunks of at most [maxChunkChars]
         * so Android TextToSpeech never clips long paragraphs.
         */
        fun splitForTts(cleanText: String, maxChunkChars: Int = DEFAULT_MAX_CHUNK_CHARS): List<String> {
            val normalized = cleanText.trim()
            if (normalized.isEmpty()) return emptyList()
            val limit = maxChunkChars.coerceAtLeast(40)
            val sentences = normalized.split(SENTENCE_BOUNDARY_REGEX).filter { it.isNotBlank() }
            val chunks = mutableListOf<String>()
            val current = StringBuilder()

            fun flushCurrent() {
                if (current.isNotEmpty()) {
                    chunks.add(current.toString().trim())
                    current.clear()
                }
            }

            for (sentence in sentences) {
                val trimmed = sentence.trim()
                if (trimmed.isEmpty()) continue
                if (trimmed.length > limit) {
                    flushCurrent()
                    var remaining = trimmed
                    while (remaining.length > limit) {
                        var cut = remaining.lastIndexOf(' ', limit)
                        if (cut <= limit / 3) cut = limit
                        chunks.add(remaining.substring(0, cut).trim())
                        remaining = remaining.substring(cut).trimStart()
                    }
                    if (remaining.isNotEmpty()) {
                        current.append(remaining)
                    }
                } else {
                    if (current.isNotEmpty() && current.length + 1 + trimmed.length > limit) {
                        flushCurrent()
                    }
                    if (current.isNotEmpty()) current.append(' ')
                    current.append(trimmed)
                }
            }
            flushCurrent()
            return chunks
        }
    }
}
