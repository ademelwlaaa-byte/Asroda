package com.example.util

import com.example.data.local.EmotionState
import com.example.data.repository.EmochiRepository
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Live Emotion & Literary Scene Prosody Engine for Emochi's 2-tier TTS architecture.
 *
 * Transforms raw RP / novel messages into expressive speech segments:
 *  - Separates character dialogue ("...") from narration/actions (*...*)
 *  - Interprets literary punctuation (`...`, `—`, `!`, `?`) into natural breath pauses & inflections
 *  - Maps the bot's live [EmotionState] onto expressive rate/pitch/pause deltas
 *  - Generates XML-escaped SSML `<prosody>` + `<break>` for Layer 1 (Edge Neural TTS)
 *    AND per-chunk rate/pitch + `playSilentUtterance` plans for Layer 2 (Android Device TTS).
 */
object EmotionalTtsProsodyEngine {

    enum class EmotionToneProfile(
        val ratePercentDelta: Int,
        val pitchHzDelta: Int,
        val basePauseMs: Long
    ) {
        SAD_HURT(ratePercentDelta = -10, pitchHzDelta = -4, basePauseMs = 420L),
        ANGRY_TENSE(ratePercentDelta = +8, pitchHzDelta = +3, basePauseMs = 150L),
        ROMANTIC_INTIMATE(ratePercentDelta = -12, pitchHzDelta = -3, basePauseMs = 350L),
        HAPPY_EXCITED(ratePercentDelta = +6, pitchHzDelta = +5, basePauseMs = 180L),
        FEARFUL_ANXIOUS(ratePercentDelta = +5, pitchHzDelta = +6, basePauseMs = 260L),
        NEUTRAL_LITERARY(ratePercentDelta = 0, pitchHzDelta = 0, basePauseMs = 280L)
    }

    enum class SegmentKind {
        DIALOGUE,
        NARRATION
    }

    data class ProsodySegment(
        val text: String,
        val kind: SegmentKind,
        val ratePercent: Int,      // e.g. -12, +8 (relative to 100%)
        val pitchHz: Int,          // e.g. -4, +5
        val pauseAfterMs: Long,    // natural breath pause after segment
        val effectiveAndroidRate: Float,
        val effectiveAndroidPitch: Float
    )

    private val TR_LOCALE = Locale.forLanguageTag("tr-TR")
    private val SEGMENT_TOKEN_REGEX = Regex("""(\*[^*]+\*|"[^"]+"|«[^»]+»|“[^”]+”)""", setOf(RegexOption.DOT_MATCHES_ALL))
    private val SENTENCE_SPLIT_REGEX = Regex("""(?<=[.!?…—])\s+""")

    /**
     * Resolves the dominant expressive prosody profile from the character's live [EmotionState].
     */
    fun resolveEmotionProfile(emotionState: EmotionState?): EmotionToneProfile {
        if (emotionState == null) return EmotionToneProfile.NEUTRAL_LITERARY
        val dom = "${emotionState.dominantEmotion} ${emotionState.mood} ${emotionState.secondaryMood}"
            .lowercase(TR_LOCALE)
        val pe = emotionState.primaryEmotions
        val ra = emotionState.relationshipAxes

        return when {
            dom.contains("üzgün") || dom.contains("kırgın") || dom.contains("melankol") ||
                dom.contains("hüzün") || dom.contains("buruk") || dom.contains("ağla") ||
                dom.contains("özlem") || dom.contains("sad") || dom.contains("hurt") ||
                pe.sadness >= 58 || ra.resentmentScore >= 65 && pe.sadness >= 40 ->
                EmotionToneProfile.SAD_HURT

            dom.contains("öfke") || dom.contains("sinir") || dom.contains("kızgın") ||
                dom.contains("gergin") || dom.contains("kıskan") || dom.contains("sert") ||
                dom.contains("düşman") || dom.contains("angry") || dom.contains("furious") ||
                pe.anger >= 55 ->
                EmotionToneProfile.ANGRY_TENSE

            dom.contains("romantik") || dom.contains("tutku") || dom.contains("çapkın") ||
                dom.contains("fısıl") || dom.contains("şefkat") || dom.contains("sevecen") ||
                dom.contains("aşık") || dom.contains("yakın") || dom.contains("warm") ||
                dom.contains("romantic") || dom.contains("intimate") ||
                (ra.affectionScore >= 78 && pe.anger < 25 && pe.sadness < 30) ->
                EmotionToneProfile.ROMANTIC_INTIMATE

            dom.contains("korku") || dom.contains("tedirgin") || dom.contains("şaşkın") ||
                dom.contains("panik") || dom.contains("endişe") || dom.contains("telaş") ||
                dom.contains("fear") || dom.contains("anxious") ||
                pe.fear >= 50 || pe.surprise >= 60 ->
                EmotionToneProfile.FEARFUL_ANXIOUS

            dom.contains("mutlu") || dom.contains("neşeli") || dom.contains("heyecan") ||
                dom.contains("coşku") || dom.contains("sevinç") || dom.contains("happy") ||
                dom.contains("excited") || pe.joy >= 60 ->
                EmotionToneProfile.HAPPY_EXCITED

            else -> EmotionToneProfile.NEUTRAL_LITERARY
        }
    }

    /**
     * Builds expressive prosody segments for a message, respecting dialogue vs narration,
     * punctuation inflections, user base speed/pitch, and live [EmotionState].
     */
    fun buildProsodySegments(
        rawText: String,
        emotionState: EmotionState? = null,
        baseSpeed: Float = 1.0f,
        basePitch: Float = 1.0f,
        readActions: Boolean = false,
        emotionalProsodyEnabled: Boolean = true,
        maxChunkChars: Int = 340
    ): List<ProsodySegment> {
        if (rawText.isBlank()) return emptyList()

        // Strip internal tags first without stripping *actions* yet so we can classify them cleanly
        val noInternalTags = EmochiRepository.cleanEmotionTags(rawText)
            .replace(Regex("""\[OOC:.*?\]""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), "")
            .replace(Regex("""^#{1,6}\s+""", RegexOption.MULTILINE), "")
            .trim()
        if (noInternalTags.isBlank()) return emptyList()

        val profile = if (emotionalProsodyEnabled) {
            resolveEmotionProfile(emotionState)
        } else {
            EmotionToneProfile.NEUTRAL_LITERARY
        }

        val rawParts = mutableListOf<Pair<SegmentKind, String>>()
        var cursor = 0
        for (match in SEGMENT_TOKEN_REGEX.findAll(noInternalTags)) {
            if (match.range.first > cursor) {
                val between = noInternalTags.substring(cursor, match.range.first).trim()
                if (between.isNotBlank()) {
                    rawParts.add(SegmentKind.DIALOGUE to between)
                }
            }
            val token = match.value
            when {
                token.startsWith("*") && token.endsWith("*") -> {
                    if (readActions) {
                        val inner = token.removeSurrounding("*").trim()
                        if (inner.isNotBlank()) {
                            rawParts.add(SegmentKind.NARRATION to inner)
                        }
                    }
                }
                else -> {
                    val inner = token
                        .trimStart('"', '«', '“')
                        .trimEnd('"', '»', '”')
                        .trim()
                    if (inner.isNotBlank()) {
                        rawParts.add(SegmentKind.DIALOGUE to inner)
                    }
                }
            }
            cursor = match.range.last + 1
        }
        if (cursor < noInternalTags.length) {
            val tail = noInternalTags.substring(cursor).trim()
            if (tail.isNotBlank()) {
                rawParts.add(SegmentKind.DIALOGUE to tail)
            }
        }

        // If readActions=false stripped everything (action-only RP message), fall back to reading cleaned action text softly
        if (rawParts.isEmpty()) {
            val fallback = ChatTtsManager.prepareTtsText(rawText, readActions = true)
            if (fallback.isNotBlank()) {
                rawParts.add(SegmentKind.NARRATION to fallback)
            }
        }

        val baseSpeedPercent = ((baseSpeed.coerceIn(0.5f, 2.0f) - 1.0f) * 100f).roundToInt()
        val basePitchHz = ((basePitch.coerceIn(0.5f, 2.0f) - 1.0f) * 35f).roundToInt()

        val segments = mutableListOf<ProsodySegment>()
        for ((kind, rawBlock) in rawParts) {
            val cleanedBlock = rawBlock
                .replace(Regex("""[*_~`]+"""), "")
                .replace(Regex("""\s+"""), " ")
                .trim()
            if (cleanedBlock.isBlank()) continue

            val sentences = splitIntoBoundedSentences(cleanedBlock, maxChunkChars)
            for (sentence in sentences) {
                val cleanSentence = sentence.trim()
                if (cleanSentence.isBlank()) continue

                val isNarration = kind == SegmentKind.NARRATION
                var ratePct = baseSpeedPercent + if (emotionalProsodyEnabled) profile.ratePercentDelta else 0
                var pitchHz = basePitchHz + if (emotionalProsodyEnabled) profile.pitchHzDelta else 0
                var pauseMs = if (emotionalProsodyEnabled) profile.basePauseMs else 220L

                // Subtle contrast: narration is calmer and slightly deeper than spoken dialogue
                if (emotionalProsodyEnabled && isNarration) {
                    ratePct -= 4
                    pitchHz -= 2
                    pauseMs = maxOf(pauseMs, 300L)
                }

                // Literary punctuation & Turkish vocal-cue prosody modulation
                if (emotionalProsodyEnabled) {
                    val lower = cleanSentence.lowercase(Locale.forLanguageTag("tr-TR"))
                    when {
                        cleanSentence.endsWith("...") || cleanSentence.endsWith("…") || cleanSentence.contains("—") -> {
                            pauseMs = maxOf(pauseMs, 380L).coerceAtMost(480L)
                            ratePct -= 4
                            pitchHz -= 2
                        }
                        cleanSentence.endsWith("?!") || cleanSentence.endsWith("!?") -> {
                            ratePct += 6
                            pitchHz += 5
                            pauseMs = minOf(pauseMs, 180L)
                        }
                        cleanSentence.endsWith("!") -> {
                            ratePct += 4
                            pitchHz += 3
                            pauseMs = minOf(pauseMs, 200L)
                        }
                        cleanSentence.endsWith("?") -> {
                            pitchHz += 3
                            pauseMs = maxOf(pauseMs, 220L)
                        }
                    }
                    when {
                        lower.contains("fısılda") || lower.contains("fısıltı") || lower.contains("whisper") -> {
                            ratePct -= 8
                            pitchHz -= 4
                            pauseMs = maxOf(pauseMs, 340L)
                        }
                        lower.contains("bağır") || lower.contains("haykır") || lower.contains("kükre") || lower.contains("shout") -> {
                            ratePct += 6
                            pitchHz += 4
                            pauseMs = minOf(pauseMs, 180L)
                        }
                        lower.contains("hıçkır") || lower.contains("ağla") || lower.contains("titreyerek") -> {
                            ratePct -= 7
                            pitchHz -= 3
                            pauseMs = maxOf(pauseMs, 360L)
                        }
                        lower.contains("kahkaha") || lower.contains("gülümse") || lower.contains("kıkırda") -> {
                            ratePct += 4
                            pitchHz += 4
                        }
                    }
                }

                val clampedRatePct = ratePct.coerceIn(-45, 65)
                val clampedPitchHz = pitchHz.coerceIn(-25, 25)
                val androidRate = (1.0f + clampedRatePct / 100f).coerceIn(0.55f, 1.95f)
                val androidPitch = (1.0f + clampedPitchHz / 40f).coerceIn(0.65f, 1.65f)

                segments.add(
                    ProsodySegment(
                        text = cleanSentence,
                        kind = kind,
                        ratePercent = clampedRatePct,
                        pitchHz = clampedPitchHz,
                        pauseAfterMs = pauseMs.coerceIn(100L, 520L),
                        effectiveAndroidRate = androidRate,
                        effectiveAndroidPitch = androidPitch
                    )
                )
            }
        }
        return segments
    }

    /**
     * Escapes XML special characters so arbitrary novel prose never breaks SSML synthesis.
     */
    fun escapeXml(text: String): String {
        val sb = StringBuilder(text.length + 16)
        for (ch in text) {
            when (ch) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                '\'' -> sb.append("&apos;")
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Builds a valid Microsoft Edge Neural SSML document for one or more [ProsodySegment]s.
     */
    fun buildSsml(
        voiceName: String,
        segments: List<ProsodySegment>,
        xmlLang: String = if (voiceName.startsWith("en-", ignoreCase = true)) "en-US" else "tr-TR"
    ): String {
        val safeVoice = escapeXml(voiceName.ifBlank { AppFeatureFlags.DEFAULT_NEURAL_VOICE })
        val sb = StringBuilder(512)
        sb.append("<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='")
        sb.append(escapeXml(xmlLang))
        sb.append("'><voice name='")
        sb.append(safeVoice)
        sb.append("'>")

        segments.forEachIndexed { index, seg ->
            val rateStr = if (seg.ratePercent >= 0) "+${seg.ratePercent}%" else "${seg.ratePercent}%"
            val pitchStr = if (seg.pitchHz >= 0) "+${seg.pitchHz}Hz" else "${seg.pitchHz}Hz"
            sb.append("<prosody rate='")
            sb.append(rateStr)
            sb.append("' pitch='")
            sb.append(pitchStr)
            sb.append("'>")
            sb.append(escapeXml(seg.text))
            sb.append("</prosody>")
            if (index < segments.lastIndex && seg.pauseAfterMs > 0L) {
                sb.append("<break time='")
                sb.append(seg.pauseAfterMs)
                sb.append("ms'/>")
            }
        }

        sb.append("</voice></speak>")
        return sb.toString()
    }

    /**
     * Groups prosody segments into bounded SSML request batches.
     * Batch #0 uses a tighter [firstBatchMaxChars] cap (~220 chars) so the very first spoken
     * sentence starts in <0.6s while batches 1..N prefetch seamlessly in the background.
     */
    fun batchSegmentsForNeural(
        segments: List<ProsodySegment>,
        maxCharsPerBatch: Int = 650,
        firstBatchMaxChars: Int = 220
    ): List<List<ProsodySegment>> {
        if (segments.isEmpty()) return emptyList()
        val batches = mutableListOf<List<ProsodySegment>>()
        var current = mutableListOf<ProsodySegment>()
        var currentChars = 0

        for (seg in segments) {
            val activeLimit = if (batches.isEmpty()) {
                firstBatchMaxChars.coerceAtMost(maxCharsPerBatch).coerceAtLeast(80)
            } else {
                maxCharsPerBatch
            }
            if (current.isNotEmpty() && currentChars + seg.text.length > activeLimit) {
                batches.add(current)
                current = mutableListOf()
                currentChars = 0
            }
            current.add(seg)
            currentChars += seg.text.length
        }
        if (current.isNotEmpty()) {
            batches.add(current)
        }
        return batches
    }

    private fun splitIntoBoundedSentences(text: String, maxChars: Int): List<String> {
        val rawSentences = text.split(SENTENCE_SPLIT_REGEX).map { it.trim() }.filter { it.isNotEmpty() }
        val result = mutableListOf<String>()
        for (sentence in rawSentences) {
            if (sentence.length <= maxChars) {
                result.add(sentence)
            } else {
                result.addAll(ChatTtsManager.splitForTts(sentence, maxChunkChars = maxChars))
            }
        }
        return result
    }
}
