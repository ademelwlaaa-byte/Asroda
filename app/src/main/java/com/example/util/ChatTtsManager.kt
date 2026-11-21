package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.data.repository.EmochiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

/**
 * Production-grade Text-to-Speech controller for Emochi.
 *
 * - Message-scoped [speakingMessageId] so only the actively spoken bubble animates its equalizer.
 * - Sentence-boundary chunking (<= 350 chars per utterance) with `QUEUE_FLUSH` + `QUEUE_ADD` so
 *   long literary novel responses never truncate at Android's TextToSpeech buffer limit.
 * - Full `AudioFocusRequest` (`AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`, `USAGE_ASSISTANT` /
 *   `CONTENT_TYPE_SPEECH`) with automatic `BookAudioController` deconfliction.
 * - Discovers and sorts installed Turkish & English device voices for the Settings Voice Picker.
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

    private val appContext: Context = context.applicationContext
    private val audioManager: AudioManager? =
        appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var tts: TextToSpeech? = null
    @Volatile
    private var isInitialized: Boolean = false
    @Volatile
    private var activeSessionId: String? = null
    @Volatile
    private var activeFinalUtteranceId: String? = null

    private var audioFocusRequest: AudioFocusRequest? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<TtsVoiceOption>>(emptyList())
    val availableVoices: StateFlow<List<TtsVoiceOption>> = _availableVoices.asStateFlow()

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                stop()
            }
        }
    }

    init {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    configureEngine()
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
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
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
                        qualityScore = v.quality
                    )
                }
                .sortedWith(
                    compareByDescending<TtsVoiceOption> { it.isTurkish }
                        .thenByDescending { it.qualityScore }
                        .thenBy { it.isNetwork }
                        .thenBy { it.name }
                )
            _availableVoices.value = filtered
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
        readActions: Boolean = AppFeatureFlags.isTtsReadActionsEnabled
    ): Boolean {
        val cleaned = prepareTtsText(rawText, readActions = readActions)
        val chunks = splitForTts(cleaned, maxChunkChars = DEFAULT_MAX_CHUNK_CHARS)
        if (chunks.isEmpty()) {
            stop()
            return false
        }

        // Prevent clash with BookReaderView chapter soundtrack
        if (BookAudioController.isPlaying) {
            BookAudioController.stop(fadeOutMs = 250L)
        }

        val engine = tts
        if (engine == null || !isInitialized) {
            return false
        }

        requestAudioFocus()
        applyVoiceParameters(engine, speed, pitch, voiceName, languageTag)

        val sessionId = "emochi_tts_${UUID.randomUUID().toString().take(8)}"
        activeSessionId = sessionId
        activeFinalUtteranceId = "${sessionId}_${chunks.lastIndex}"
        _speakingMessageId.value = messageId
        _isSpeaking.value = true

        chunks.forEachIndexed { idx, chunk ->
            val queueMode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val utteranceId = "${sessionId}_$idx"
            try {
                engine.speak(chunk, queueMode, null, utteranceId)
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
        languageTag: String = "tr"
    ): Boolean {
        val sampleText = if (languageTag.lowercase(Locale.ROOT).startsWith("en")) {
            "Hello, this is a preview of Emochi's reading voice."
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
            readActions = false
        )
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        clearSpeakingState()
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
        isInitialized = false
    }

    private fun applyVoiceParameters(
        engine: TextToSpeech,
        speed: Float,
        pitch: Float,
        voiceName: String,
        languageTag: String = "tr"
    ) {
        try {
            engine.setSpeechRate(speed.coerceIn(0.5f, 2.0f))
            engine.setPitch(pitch.coerceIn(0.5f, 2.0f))
            if (voiceName.isNotBlank()) {
                val chosen = engine.voices?.firstOrNull { it.name == voiceName }
                if (chosen != null) {
                    engine.voice = chosen
                    return
                }
            }
            val preferredLocale = if (languageTag.lowercase(Locale.ROOT).startsWith("en")) {
                Locale.US
            } else {
                Locale.forLanguageTag("tr-TR")
            }
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
        _isSpeaking.value = false
        _speakingMessageId.value = null
        abandonAudioFocus()
    }

    companion object {
        const val DEFAULT_MAX_CHUNK_CHARS = 350
        const val PREVIEW_MESSAGE_ID = "__tts_voice_preview__"
        const val DEFAULT_PREVIEW_SAMPLE_TR =
            "Gece rüzgârı mor salkımların arasından geçerken, hikâyemiz yeni bir sayfaya açılıyor."

        private val MULTILINE_ACTION_REGEX = Regex("\\*.*?\\*", RegexOption.DOT_MATCHES_ALL)
        private val MARKDOWN_SYMBOL_REGEX = Regex("[#`~_>]+")
        private val EMOJI_AND_SYMBOL_REGEX = Regex("[\\p{So}\\p{Cs}\\uFE0F\\u200D]+")
        private val WHITESPACE_REGEX = Regex("\\s+")
        private val SENTENCE_BOUNDARY_REGEX = Regex("""(?<=[.!?…])\s+""")

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

            // If message consisted solely of *action* descriptions and readActions=false, gracefully fall back to reading without asterisks
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
                    // Hard wrap ultra-long single sentence cleanly at word boundaries
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
