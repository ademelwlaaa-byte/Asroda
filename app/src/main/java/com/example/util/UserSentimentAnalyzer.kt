package com.example.util

import com.example.data.local.MessageEntity
import com.example.data.repository.TurkishTextAnalyzer
import com.squareup.moshi.Json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.util.Locale
import kotlin.math.abs

enum class UserMood(val label: String, val systemHint: String) {
    POSITIVE("Pozitif", "Kullanıcı şu an iyi bir ruh halinde görünüyor. Enerjik ve samimi yanıtlar ver."),
    EXCITED("Heyecanlı", "Kullanıcı heyecanlı ve coşkulu görünüyor. Aynı enerjiyi yansıt, neşeli ol."),
    NEUTRAL("Nötr", ""),
    NEGATIVE("Negatif", "Kullanıcı şu an mutsuz veya gergin görünüyor. Nazik, anlayışlı ve destekleyici ol."),
    WITHDRAWN("Mesafeli", "Kullanıcı kısa ve ilgisiz yanıtlar veriyor. Zorlamadan ilgisini çekmeye çalış.")
}

/**
 * R8-safe DTO for Micro-AI (ExecutionMode.UTILITY) User Sentiment & Subtext response.
 */
data class MicroAiSentimentDto(
    @Json(name = "tone") val tone: String = "neutral",
    @Json(name = "valence") val valence: Float = 0f,
    @Json(name = "intensity") val intensity: Float = 0.3f,
    @Json(name = "is_ooc_meta") val isOocMeta: Boolean = false,
    @Json(name = "subtext") val subtext: String = "",
    @Json(name = "resistance_delta") val resistanceDelta: Float = 0f
)

data class SentimentAnalysisResult(
    val mood: UserMood = UserMood.NEUTRAL,
    val tone: String = "neutral",
    val valence: Float = 0f,
    val intensity: Float = 0f,
    val isOocMeta: Boolean = false,
    val oocDirective: String = "",
    val subtext: String = "",
    val resistanceDelta: Float = 0f,
    val needsMicroAiDisambiguation: Boolean = false,
    val usedMicroAi: Boolean = false
)

/**
 * Hybrid Turkish-locale User Sentiment & Subtext Sensor (Sistem #17 — 10/10).
 *
 * Layer 1 (<1 ms local sensor):
 * - 1-3 word left/right Turkish negation window ("hiç de mutlu değilim" -> negative,
 *   "kızgın değilim, merak etme" -> non-angry/neutral)
 * - Turkish intensifiers (1.35x: çok, aşırı, fazlasıyla, gerçekten, !!!) & mitigators (0.60x: biraz, hafif, sanki)
 * - Irony / sarcasm / passive-aggressive signals ((!), güya, tabii canım, aynen öyle..., neyse boşver)
 * - Clean IC vs OOC separation (((...)), [OOC: ...])
 * - RP *action* stripping so fictional dramatic prose never skews user mood
 *
 * Layer 2 (Real Micro-AI / ExecutionMode.UTILITY with 2.5s race timeout):
 * - Skips short unambiguous messages (<35 chars and no irony/ambiguity)
 * - Blends 0.70 Micro-AI + 0.30 Local when Utility AI responds within 2.5s
 * - Emits single-line [KULLANICI ALT METNİ] prompt hint
 * - Instant silent fallback to Layer 1 on timeout, offline, error, or feature flag off
 */
object UserSentimentAnalyzer {

    private val TR_LOCALE = Locale("tr", "TR")

    private val positiveWords = setOf(
        "mutlu", "harika", "süper", "güzel", "seviyorum", "mükemmel", "iyi", "güldüm",
        "teşekkür", "sevdim", "eğlendim", "keyifli", "memnun", "sevinçli", "sevindim", "harikaydı", "bayıldım",
        "sıcak", "samimi", "tatlı", "şahane", "huzurlu", "haklısın"
    )

    private val positiveEmojis = setOf("😊", "😄", "🥰", "❤️", "💕", "✨", "🎉", "👍")

    private val negativeWords = setOf(
        "üzgün", "kötü", "sinirli", "sıkıldım", "yoruldum", "bıktım", "nefret",
        "berbat", "korkunç", "rezalet", "mutsuz", "bunaldım", "yorucu", "sıkıcı",
        "kızgın", "bozuldum", "kırıldım", "kırgın", "hayal kırıklığı", "öfkeli", "pişman"
    )

    private val negativeEmojis = setOf("😢", "😭", "😡", "😤", "💔")

    private val excitedWords = setOf("wow", "aman", "inanılmaz", "şaşırdım", "beklemiyordum", "oley", "yaşasın")

    private val intensifierWords = setOf("çok", "aşırı", "fazlasıyla", "gerçekten", "cidden", "delicesine", "son derece")
    private val mitigatorWords = setOf("biraz", "hafif", "sanki", "azıcık", "kısmen", "eh")

    private val negationTokens = setOf(
        "değil", "değilim", "değilsin", "değildi", "değiliz", "degil", "degilim",
        "hiç", "asla", "yok", "olmadı", "olmaz", "istemiyorum", "istemem", "etmiyorum", "sanma", "sanmıyorum"
    )

    private val ironyMarkers = listOf(
        "(!)",
        "güya",
        "tabii canım",
        "tabi canım",
        "aynen öyle...",
        "aynen öyle",
        "neyse boşver",
        "boşver",
        "sen her zaman haklısın",
        "sen haklısın...",
        "ne ala",
        "bravo sana"
    )

    private val RP_ACTION_REGEX = Regex("""\*[^*]{1,400}\*""")
    private val OOC_DOUBLE_PAREN_REGEX = Regex("""(?s)\(\((.+?)\)\)""")
    private val OOC_BRACKET_REGEX = Regex("""(?is)\[\s*OOC\s*:?\s*([^\]]{1,4000})\]""")
    private val OOC_PAREN_REGEX = Regex("""(?is)\(\s*OOC\s*:?\s*([^)]{1,4000})\)""")
    private val MULTI_EXCL_REGEX = Regex("""!{2,}""")
    private val TRIPLE_EXCL_REGEX = Regex("""!{3,}""")
    private val TURKISH_NEG_VERB_SUFFIX = Regex("""(?:mıyorum|miyorum|muyorum|müyorum|madım|medim|mıyor|miyor|muyor|müyor|mam|mem|maz|mez)$""")

    data class OocSplit(
        val icText: String,
        val oocDirective: String,
        val isPureOoc: Boolean
    )

    /**
     * Returns true when the ENTIRE trimmed message is enclosed in a single balanced outer `(...)` pair
     * (the exact OOC director format taught in Emochi's system prompt).
     */
    private fun isSingleOuterParenthesisBlock(trimmed: String): Boolean {
        if (trimmed.length < 3 || !trimmed.startsWith('(') || !trimmed.endsWith(')')) return false
        var depth = 0
        for (i in trimmed.indices) {
            when (trimmed[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0 && i < trimmed.lastIndex) return false
                    if (depth < 0) return false
                }
            }
        }
        return depth == 0
    }

    /**
     * Separates Out-Of-Character directives (((...)), [OOC: ...], (OOC: ...),
     * and full-message single-parenthesis (...) director notes) from In-Character prose/dialogue.
     */
    fun splitIcAndOoc(rawText: String): OocSplit {
        if (rawText.isBlank()) return OocSplit("", "", false)
        val trimmed = rawText.trim()
        if (isSingleOuterParenthesisBlock(trimmed) && !trimmed.startsWith("((")) {
            val inner = trimmed.substring(1, trimmed.length - 1).trim()
            if (inner.isNotBlank()) {
                return OocSplit(
                    icText = "",
                    oocDirective = inner,
                    isPureOoc = true
                )
            }
        }

        val oocParts = mutableListOf<String>()
        var remaining = rawText

        OOC_DOUBLE_PAREN_REGEX.findAll(remaining).forEach { m ->
            val directive = m.groupValues[1].trim()
            if (directive.isNotBlank()) oocParts.add(directive)
        }
        remaining = OOC_DOUBLE_PAREN_REGEX.replace(remaining, " ")

        OOC_BRACKET_REGEX.findAll(remaining).forEach { m ->
            val directive = m.groupValues[1].trim()
            if (directive.isNotBlank()) oocParts.add(directive)
        }
        remaining = OOC_BRACKET_REGEX.replace(remaining, " ")

        OOC_PAREN_REGEX.findAll(remaining).forEach { m ->
            val directive = m.groupValues[1].trim()
            if (directive.isNotBlank()) oocParts.add(directive)
        }
        remaining = OOC_PAREN_REGEX.replace(remaining, " ")

        val icClean = remaining.trim()
        val oocCombined = oocParts.joinToString("; ").trim()
        return OocSplit(
            icText = icClean,
            oocDirective = oocCombined,
            isPureOoc = oocCombined.isNotBlank() && icClean.isBlank()
        )
    }

    fun isPureOocMessage(rawText: String): Boolean = splitIcAndOoc(rawText).isPureOoc

    /**
     * Layer 1 (<1 ms) detailed Turkish local sentiment analysis for a single user message.
     */
    fun analyzeLocalDetailed(rawText: String): SentimentAnalysisResult {
        if (rawText.isBlank()) return SentimentAnalysisResult()

        val split = splitIcAndOoc(rawText)
        if (split.isPureOoc) {
            return SentimentAnalysisResult(
                mood = UserMood.NEUTRAL,
                tone = "neutral",
                valence = 0f,
                intensity = 0f,
                isOocMeta = true,
                oocDirective = split.oocDirective,
                subtext = "",
                resistanceDelta = 0f,
                needsMicroAiDisambiguation = false,
                usedMicroAi = false
            )
        }

        // Strip *action* RP narration so fictional dramatic scenes don't skew user mood
        val directRaw = RP_ACTION_REGEX.replace(split.icText, " ").trim()
        if (directRaw.isBlank()) {
            return SentimentAnalysisResult(
                mood = UserMood.NEUTRAL,
                tone = "neutral",
                valence = 0f,
                intensity = 0f,
                isOocMeta = false,
                oocDirective = split.oocDirective
            )
        }

        val lowerRaw = directRaw.lowercase(TR_LOCALE)
        val norm = TurkishTextAnalyzer.normalize(directRaw)
        val tokens = tokenizeWords(norm)

        // Check irony / sarcasm / passive-aggressive cues
        val hasIronyMarker = ironyMarkers.any { lowerRaw.contains(it) } ||
            (directRaw.contains("...") && (lowerRaw.contains("haklısın") || lowerRaw.contains("neyse") || lowerRaw.contains("tamam") || lowerRaw.contains("peki")))

        var posWeight = 0f
        var negWeight = 0f
        var excitedWeight = 0f
        var negatedNegativeHit = false

        // Score token-by-token with 1-3 word left/right negation window & intensifier/mitigator modifiers
        for (i in tokens.indices) {
            val tok = tokens[i]
            val modifier = resolveWindowModifier(tokens, i, directRaw)
            val negated = isTokenNegatedInWindow(tokens, i)

            val isPos = positiveWords.any { kw ->
                !kw.contains(" ") && (tok == kw || (kw.length >= 4 && tok.startsWith(kw)))
            }
            val isNeg = negativeWords.any { kw ->
                !kw.contains(" ") && (tok == kw || (kw.length >= 4 && tok.startsWith(kw)))
            }
            val isExc = excitedWords.any { kw -> tok == kw || (kw.length >= 4 && tok.startsWith(kw)) }

            when {
                isPos -> {
                    if (negated) {
                        negWeight += 1.0f * modifier
                    } else if (hasIronyMarker) {
                        // Irony dampens & flips superficial positive words ("güya çok sevindim (!)")
                        negWeight += 0.55f * modifier
                    } else {
                        posWeight += 1.0f * modifier
                    }
                }
                isNeg -> {
                    if (negated) {
                        // "kızgın değilim, merak etme" -> neutral/softened, NEVER triggers anger/negative
                        negatedNegativeHit = true
                        posWeight += 0.15f
                    } else {
                        negWeight += 1.0f * modifier
                    }
                }
                isExc -> {
                    if (hasIronyMarker) {
                        negWeight += 0.35f
                    } else {
                        excitedWeight += 1.0f * modifier
                    }
                }
            }
        }

        // Multi-word negative phrase check ("hayal kırıklığı")
        if (norm.contains("hayal kırıklığı") || norm.contains("hayal kirikligi")) {
            negWeight += 1.1f
        }

        // Emoji contributions
        for (emoji in positiveEmojis) {
            if (directRaw.contains(emoji)) posWeight += if (hasIronyMarker) 0.1f else 0.85f
        }
        for (emoji in negativeEmojis) {
            if (directRaw.contains(emoji)) negWeight += 0.95f
        }

        // Exclamation bursts (single check, no double counting)
        if (MULTI_EXCL_REGEX.containsMatchIn(directRaw)) {
            if (hasIronyMarker) {
                negWeight += 0.25f
            } else if (negWeight > posWeight) {
                negWeight *= 1.15f
            } else {
                excitedWeight += 0.85f
            }
        }

        // Passive-aggressive / irony base negative weight if no explicit emotion word matched
        if (hasIronyMarker && negWeight == 0f) {
            negWeight += 0.50f
        }

        val net = (posWeight - negWeight)
        val totalEnergy = posWeight + negWeight + excitedWeight
        val valence = (net / maxOf(1.2f, totalEnergy + 0.5f)).coerceIn(-1f, 1f)
        val intensity = (totalEnergy / 3.0f).coerceIn(0f, 1f)

        val mood = when {
            negatedNegativeHit && negWeight == 0f && posWeight <= 0.35f -> UserMood.NEUTRAL
            excitedWeight >= maxOf(posWeight, negWeight) && excitedWeight >= 0.8f && negWeight == 0f -> UserMood.EXCITED
            negWeight > posWeight + 0.05f -> UserMood.NEGATIVE
            posWeight > negWeight + 0.05f && !hasIronyMarker -> UserMood.POSITIVE
            else -> UserMood.NEUTRAL
        }

        val tone = when {
            hasIronyMarker -> "sarcastic"
            mood == UserMood.EXCITED -> "playful"
            mood == UserMood.POSITIVE -> "warm"
            mood == UserMood.NEGATIVE && (lowerRaw.contains("kırıl") || lowerRaw.contains("üzgün") || lowerRaw.contains("neyse")) -> "hurt"
            mood == UserMood.NEGATIVE -> "tense"
            else -> "neutral"
        }

        val mixedPolarity = posWeight > 0.4f && negWeight > 0.4f
        val needsDisambiguation = hasIronyMarker || mixedPolarity ||
            (directRaw.contains("...") && directRaw.length >= 16)

        return SentimentAnalysisResult(
            mood = mood,
            tone = tone,
            valence = valence,
            intensity = intensity,
            isOocMeta = false,
            oocDirective = split.oocDirective,
            subtext = "",
            resistanceDelta = (valence * 0.08f).coerceIn(-0.15f, 0.15f),
            needsMicroAiDisambiguation = needsDisambiguation,
            usedMicroAi = false
        )
    }

    /**
     * Backward-compatible multi-message mood analyzer (preserves existing callers & tests).
     */
    fun analyze(messages: List<MessageEntity>, windowSize: Int = 3): UserMood {
        val userMessages = messages.filter { it.role == "user" }.takeLast(windowSize)
        if (userMessages.isEmpty()) return UserMood.NEUTRAL

        var positiveScore = 0f
        var negativeScore = 0f
        var excitedScore = 0f
        var withdrawnScore = 0

        for (msg in userMessages) {
            val split = splitIcAndOoc(msg.text)
            if (split.isPureOoc) continue
            val directRaw = RP_ACTION_REGEX.replace(split.icText, " ").trim()
            val norm = TurkishTextAnalyzer.normalize(directRaw)
            if (norm.isBlank()) continue

            val words = tokenizeWords(norm)
            val local = analyzeLocalDetailed(msg.text)

            if (words.size <= 2 &&
                local.mood == UserMood.NEUTRAL &&
                positiveEmojis.none { directRaw.contains(it) } &&
                negativeEmojis.none { directRaw.contains(it) }
            ) {
                withdrawnScore++
            }

            when (local.mood) {
                UserMood.POSITIVE -> positiveScore += maxOf(1f, abs(local.valence) * 2f)
                UserMood.NEGATIVE -> negativeScore += maxOf(1f, abs(local.valence) * 2f)
                UserMood.EXCITED -> excitedScore += maxOf(1f, local.intensity * 2f)
                UserMood.NEUTRAL, UserMood.WITHDRAWN -> Unit
            }
        }

        if (withdrawnScore >= 2 && positiveScore == 0f && negativeScore == 0f && excitedScore == 0f) {
            return UserMood.WITHDRAWN
        }

        val maxScore = maxOf(positiveScore, negativeScore, excitedScore)
        if (maxScore <= 0f) return UserMood.NEUTRAL

        return when {
            excitedScore >= maxScore && excitedScore > 0f && negativeScore == 0f -> UserMood.EXCITED
            negativeScore > positiveScore -> UserMood.NEGATIVE
            positiveScore >= negativeScore && positiveScore > 0f -> UserMood.POSITIVE
            else -> UserMood.NEUTRAL
        }
    }

    /**
     * Smart budget gate: avoids unnecessary Micro-AI calls on short unambiguous messages
     * like "Tamam" or "Günaydın".
     */
    fun shouldInvokeMicroAiSentiment(
        userMessage: String,
        localResult: SentimentAnalysisResult = analyzeLocalDetailed(userMessage)
    ): Boolean {
        if (!AppFeatureFlags.isMicroAiCastSentimentEnabled) return false
        if (localResult.isOocMeta) return false
        val clean = splitIcAndOoc(userMessage).icText.trim()
        if (clean.isBlank()) return false
        if (clean.length < 35 && !localResult.needsMicroAiDisambiguation) return false
        return localResult.needsMicroAiDisambiguation ||
            clean.length >= 45 ||
            abs(localResult.valence) >= 0.45f
    }

    /**
     * Layer 2 — Hybrid Micro-AI Subtext & Sentiment Analyzer (ExecutionMode.UTILITY, 2.5s race timeout).
     * Blends 0.70 Micro-AI + 0.30 Local when Utility AI responds within [timeoutMs].
     * Falls back immediately to Layer 1 local result on timeout, disabled flag, short message, or error.
     */
    suspend fun analyzeSentimentWithMicroAi(
        userMessage: String,
        lastBotMessageSummary: String = "",
        timeoutMs: Long = 2500L,
        forceMicroAi: Boolean = false,
        utilityCaller: (suspend (systemPrompt: String, userPrompt: String, maxTokens: Int) -> String)? = null
    ): SentimentAnalysisResult {
        val local = analyzeLocalDetailed(userMessage)
        if (!AppFeatureFlags.isMicroAiCastSentimentEnabled || utilityCaller == null) {
            return local
        }
        if (!forceMicroAi && !shouldInvokeMicroAiSentiment(userMessage, local)) {
            return local
        }

        val systemPrompt = """
Sen Emochi'nin Türkçe alt metin ve duygu analiz motorusun.
Yalnızca GEÇERLİ tek JSON nesnesi üret; markdown veya açıklama YAZMA.
Kullanıcının mesajındaki gizli kırgınlık, ironi, pasif-agresif ton, samimiyet ve OOC (karakter dışı) direktifini tespit et.
JSON ŞEMASI:
{"tone":"hurt|warm|tense|playful|distant|sarcastic|neutral","valence":-0.75,"intensity":0.80,"is_ooc_meta":false,"subtext":"kısa alt metin","resistance_delta":-0.08}
        """.trimIndent()

        val userPrompt = buildString {
            if (lastBotMessageSummary.isNotBlank()) {
                appendLine("SON BOT MESAJI ÖZETİ: ${lastBotMessageSummary.take(220)}")
            }
            append("KULLANICI MESAJI: ${userMessage.take(600)}")
        }

        val rawResponse = try {
            withTimeoutOrNull(timeoutMs.coerceAtLeast(100L)) {
                utilityCaller(systemPrompt, userPrompt, 130)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            null
        } ?: return local

        val dto = parseMicroAiSentimentResponse(rawResponse) ?: return local
        return blendLocalAndMicroAi(local, dto)
    }

    fun parseMicroAiSentimentResponse(rawJson: String): MicroAiSentimentDto? {
        if (rawJson.isBlank()) return null
        val cleaned = rawJson.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return try {
            val obj = JSONObject(cleaned.substring(start, end + 1))
            val tone = obj.optString("tone", "neutral").trim().lowercase(Locale.US).ifBlank { "neutral" }
            val valence = obj.optDouble("valence", 0.0).toFloat().coerceIn(-1f, 1f)
            val intensity = obj.optDouble("intensity", 0.3).toFloat().coerceIn(0f, 1f)
            val isOoc = obj.optBoolean("is_ooc_meta", false)
            val subtext = obj.optString("subtext", "").trim().take(180)
            val delta = obj.optDouble("resistance_delta", 0.0).toFloat().coerceIn(-0.15f, 0.15f)
            MicroAiSentimentDto(
                tone = tone,
                valence = valence,
                intensity = intensity,
                isOocMeta = isOoc,
                subtext = subtext,
                resistanceDelta = delta
            )
        } catch (_: Exception) {
            null
        }
    }

    fun blendLocalAndMicroAi(
        local: SentimentAnalysisResult,
        ai: MicroAiSentimentDto
    ): SentimentAnalysisResult {
        val effectiveOoc = local.isOocMeta || ai.isOocMeta
        val blendedValence = if (effectiveOoc) 0f else (0.70f * ai.valence + 0.30f * local.valence).coerceIn(-1f, 1f)
        val blendedIntensity = if (effectiveOoc) 0f else (0.70f * ai.intensity + 0.30f * local.intensity).coerceIn(0f, 1f)
        val clampedDelta = if (effectiveOoc) 0f else ai.resistanceDelta.coerceIn(-0.15f, 0.15f)

        val mood = when {
            effectiveOoc -> UserMood.NEUTRAL
            ai.tone in setOf("hurt", "tense", "sarcastic") || blendedValence <= -0.20f -> UserMood.NEGATIVE
            ai.tone == "distant" -> UserMood.WITHDRAWN
            ai.tone == "playful" && blendedIntensity >= 0.55f -> UserMood.EXCITED
            ai.tone == "warm" || blendedValence >= 0.25f -> UserMood.POSITIVE
            else -> local.mood
        }

        return SentimentAnalysisResult(
            mood = mood,
            tone = ai.tone.ifBlank { local.tone },
            valence = blendedValence,
            intensity = blendedIntensity,
            isOocMeta = effectiveOoc,
            oocDirective = local.oocDirective,
            subtext = ai.subtext.ifBlank { local.subtext },
            resistanceDelta = clampedDelta,
            needsMicroAiDisambiguation = false,
            usedMicroAi = true
        )
    }

    fun buildMoodHint(mood: UserMood): String {
        if (mood.systemHint.isBlank()) return ""
        return "\n\n[KULLANICI DUYGU SENSÖRÜ - GİZLİ]\n${mood.systemHint}"
    }

    /**
     * Single-line prompt injection for Micro-AI detected user subtext & OOC tempo preference.
     */
    fun buildSubtextPromptHint(result: SentimentAnalysisResult): String {
        val parts = mutableListOf<String>()
        if (result.subtext.isNotBlank() && !result.isOocMeta) {
            parts.add("[KULLANICI ALT METNİ]: ${result.subtext.replace("\n", " ").trim().take(160)}")
        }
        if (result.oocDirective.isNotBlank() && result.oocDirective.length <= 200) {
            parts.add("[KULLANICI OOC SAHNE TEMPO TERCİHİ]: ${result.oocDirective.replace("\n", " ").trim().take(160)}")
        }
        return if (parts.isEmpty()) "" else "\n" + parts.joinToString("\n")
    }

    private fun tokenizeWords(normText: String): List<String> =
        normText.split(Regex("""[^\p{L}\p{N}]+""")).filter { it.isNotBlank() }

    /**
     * 1-3 word left/right Turkish negation window around index [idx].
     */
    private fun isTokenNegatedInWindow(tokens: List<String>, idx: Int): Boolean {
        val token = tokens[idx]
        if (TURKISH_NEG_VERB_SUFFIX.containsMatchIn(token)) return true

        val leftStart = (idx - 3).coerceAtLeast(0)
        for (j in leftStart until idx) {
            if (tokens[j] in negationTokens) return true
        }
        val rightEnd = (idx + 3).coerceAtMost(tokens.lastIndex)
        for (j in (idx + 1)..rightEnd) {
            if (tokens[j] in negationTokens) return true
        }
        return false
    }

    /**
     * Intensifier (1.35x) & mitigator (0.60x) modifier around index [idx].
     */
    private fun resolveWindowModifier(tokens: List<String>, idx: Int, rawText: String): Float {
        var mult = 1.0f
        val leftStart = (idx - 2).coerceAtLeast(0)
        for (j in leftStart until idx) {
            val prev = tokens[j]
            if (prev in intensifierWords) mult = maxOf(mult, 1.35f)
            if (prev in mitigatorWords) mult = minOf(mult, 0.60f)
        }
        if (TRIPLE_EXCL_REGEX.containsMatchIn(rawText)) {
            mult = maxOf(mult, 1.35f)
        }
        return mult
    }
}
