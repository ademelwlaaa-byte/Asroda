package com.example.util

import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PersonalityProfile
import com.example.data.repository.TurkishTextAnalyzer
import java.util.Locale
import kotlin.math.abs

/**
 * Sistem #17 — Dinamik Zorluk & Histerezis Kalibratörü (10/10)
 *
 * - Üstel Hareketli Ortalama (EMA): newScore = 0.70 * prevScore + 0.30 * turnTarget
 *   (ve Ufak AI resistance_delta değeri -0.15f..+0.15f ile sınırlı).
 * - Kişilik Taban Çapası (Baseline Anchor): Botun attachmentStyle & emotionalRegulationCapacity
 *   değerine göre zorluk/direniş koridoru (dismissive_avoidant / avoidant karakter 2 mesajda
 *   tamamen savunmasız kalamaz; sıcak/secure karakter gereksiz duvar örmez).
 * - OOC Koruması: is_ooc_meta == true ise rol içi direniş skoru ASLA değişmez.
 * - Özellik Bayrağı Uyumu: isDynamicDifficultyEnabled == false iken nötr 1.0f çarpan döner
 *   ve prompta ekstra zorluk direktifi enjekte etmez.
 */
object DynamicDifficultyCalibrator {

    private const val WINDOW = 20
    private const val MAX_SHIFT = 0.15f
    private const val EMA_PREV_WEIGHT = 0.70f
    private const val EMA_TURN_WEIGHT = 0.30f
    private const val FLOOR = 0.50f
    private const val CEILING = 2.50f
    private val TR_LOCALE = Locale("tr", "TR")

    private val POSITIVE_KW = listOf(
        "seviyorum", "teşekkür", "harika", "güzel", "süper",
        "mükemmel", "tatlı", "özledim", "mutlu", "sevindim",
        "sağol", "canım", "harikaydı", "bayıldım",
        "love", "thank", "great", "wonderful", "miss you", "happy"
    )

    private val NEGATIVE_KW = listOf(
        "siktir", "lanet", "nefret", "berbat", "rezil",
        "aptal", "salak", "ahmak", "saçma", "bıktım",
        "hate", "stupid", "idiot", "trash", "terrible", "awful", "shut up"
    )

    private val AGGRESSIVE_PHRASES = listOf(
        "hadi be", "ne biçim", "nasıl böyle", "yeter artık"
    )

    private val NEGATION_FOLLOW_REGEX = Regex("""(?<![\p{L}\p{N}])(değil\p{L}*|degil\p{L}*|yok|hayır|not)(?![\p{L}\p{N}])""")
    private val RP_ACTION_REGEX = Regex("""\*[^*]{1,400}\*""")
    private val MULTI_EXCL_REGEX = Regex("""!{2,}""")
    private val MULTI_QUEST_REGEX = Regex("""\?{3,}""")

    data class PersonalityCorridor(
        val minMultiplier: Float,
        val maxMultiplier: Float,
        val resistanceFloor: Float,
        val resistanceCeiling: Float
    )

    data class CalibrationResult(
        val newDifficulty: Float,
        val oldDifficulty: Float,
        val delta: Float,
        val reason: String,
        val promptDirective: String = ""
    )

    /**
     * Resolves personality-anchored multiplier & resistance corridors from
     * attachmentStyle and emotionalRegulationCapacity (0..100).
     *
     * Note on multiplier semantics:
     * - Lower multiplier (e.g. 0.55..1.05) = HIGHER emotional resistance (cold/avoidant character)
     * - Higher multiplier (e.g. 0.85..2.20) = LOWER emotional resistance (warm/secure character)
     */
    fun resolvePersonalityCorridor(
        attachmentStyle: String,
        emotionalRegulationCapacity: Int = 50
    ): PersonalityCorridor {
        val style = attachmentStyle.trim().lowercase(Locale.US)
        val regNorm = ((emotionalRegulationCapacity.coerceIn(0, 100) - 50) / 50f) // -1..+1
        val stabilityTrim = (regNorm * 0.08f).coerceIn(-0.08f, 0.08f)

        return when (style) {
            "dismissive_avoidant", "avoidant" -> PersonalityCorridor(
                minMultiplier = (0.50f).coerceIn(FLOOR, CEILING),
                maxMultiplier = (1.10f - stabilityTrim.coerceAtLeast(0f)).coerceIn(0.85f, 1.15f),
                resistanceFloor = 0.45f,
                resistanceCeiling = 0.90f
            )
            "fearful_avoidant" -> PersonalityCorridor(
                minMultiplier = 0.55f,
                maxMultiplier = (1.25f - stabilityTrim.coerceAtLeast(0f)).coerceIn(1.0f, 1.35f),
                resistanceFloor = 0.35f,
                resistanceCeiling = 0.85f
            )
            "anxious" -> PersonalityCorridor(
                minMultiplier = 0.70f,
                maxMultiplier = (1.85f - stabilityTrim).coerceIn(1.55f, 2.05f),
                resistanceFloor = 0.15f,
                resistanceCeiling = 0.70f
            )
            else -> PersonalityCorridor( // "secure" or warm default
                minMultiplier = (0.80f + stabilityTrim.coerceAtLeast(0f)).coerceIn(0.75f, 0.90f),
                maxMultiplier = (2.20f - stabilityTrim.coerceAtLeast(0f)).coerceIn(1.90f, CEILING),
                resistanceFloor = 0.10f,
                resistanceCeiling = 0.60f
            )
        }
    }

    /**
     * Converts a difficulty multiplier into normalized resistance (0.0 = very open, 1.0 = maximum guard).
     */
    fun multiplierToResistance(multiplier: Float): Float =
        ((CEILING - multiplier.coerceIn(FLOOR, CEILING)) / (CEILING - FLOOR)).coerceIn(0f, 1f)

    /**
     * Effective difficulty multiplier respecting AppFeatureFlags.isDynamicDifficultyEnabled.
     * Returns strictly neutral 1.0f when dynamic difficulty is disabled.
     */
    fun resolveEffectiveMultiplier(bot: BotEntity): Float {
        if (!AppFeatureFlags.isDynamicDifficultyEnabled) return 1.0f
        return bot.baseAffectionDifficulty.toFloat().coerceIn(FLOOR, CEILING)
    }

    /**
     * Single-turn EMA hysteresis update from [SentimentAnalysisResult] (Hybrid Layer 1 + Micro-AI).
     *
     * Rules:
     * - Feature flag off -> returns neutral 1.0f, delta = 0f, empty prompt directive.
     * - OOC meta message -> resistance/difficulty unchanged (delta = 0f).
     * - EMA smoothing: newScore = 0.70 * prevScore + 0.30 * turnTarget, max shift ±0.15 per turn.
     * - Clamped strictly inside personality baseline corridor (attachmentStyle + regulationCapacity).
     */
    fun calibrateTurn(
        bot: BotEntity,
        sentiment: SentimentAnalysisResult,
        attachmentStyleOverride: String? = null
    ): CalibrationResult {
        val old = bot.baseAffectionDifficulty.toFloat().coerceIn(FLOOR, CEILING)
        if (!AppFeatureFlags.isDynamicDifficultyEnabled) {
            return CalibrationResult(
                newDifficulty = 1.0f,
                oldDifficulty = old,
                delta = 0f,
                reason = "dinamik zorluk kapalı (nötr 1.0f)",
                promptDirective = ""
            )
        }

        if (sentiment.isOocMeta) {
            return CalibrationResult(
                newDifficulty = old,
                oldDifficulty = old,
                delta = 0f,
                reason = "OOC meta direktifi — rol içi direniş değiştirilmedi",
                promptDirective = ""
            )
        }

        val attachmentStyle = attachmentStyleOverride
            ?: extractAttachmentStyle(bot)
        val corridor = resolvePersonalityCorridor(attachmentStyle, bot.emotionalRegulationCapacity)

        // Clamp Micro-AI resistance_delta to [-0.15, +0.15]; positive resistance_delta => harder => lower multiplier
        val clampedResistanceDelta = sentiment.resistanceDelta.coerceIn(-MAX_SHIFT, MAX_SHIFT)
        val valenceShift = sentiment.valence.coerceIn(-1f, 1f) * 0.35f
        val turnTarget = (old + valenceShift - clampedResistanceDelta)
            .coerceIn(corridor.minMultiplier, corridor.maxMultiplier)

        val emaRaw = EMA_PREV_WEIGHT * old + EMA_TURN_WEIGHT * turnTarget
        val boundedShift = (emaRaw - old).coerceIn(-MAX_SHIFT, MAX_SHIFT)
        val newDiff = (old + boundedShift).coerceIn(corridor.minMultiplier, corridor.maxMultiplier)
        val actualDelta = newDiff - old

        val reason = String.format(
            Locale.US,
            "ema_turn(tone=%s, val=%.2f, resDelta=%+.2f, corridor=%.2f..%.2f) -> %+.3f",
            sentiment.tone,
            sentiment.valence,
            clampedResistanceDelta,
            corridor.minMultiplier,
            corridor.maxMultiplier,
            actualDelta
        )
        return CalibrationResult(
            newDifficulty = newDiff,
            oldDifficulty = old,
            delta = actualDelta,
            reason = reason,
            promptDirective = ""
        )
    }

    /**
     * Next-turn fallback calibration using optional [[STATE_JSON]] "u_tone" emitted by main model.
     */
    fun calibrateFromStateJsonUserTone(bot: BotEntity, uToneRaw: String): CalibrationResult {
        val tone = uToneRaw.trim().lowercase(Locale.US)
        val valence = when (tone) {
            "warm", "affectionate", "playful" -> 0.45f
            "hurt", "sad" -> -0.35f
            "tense", "sarcastic", "hostile", "angry" -> -0.55f
            "distant" -> -0.25f
            else -> 0f
        }
        val resDelta = when (tone) {
            "tense", "sarcastic", "hostile", "angry" -> 0.08f
            "warm", "affectionate" -> -0.06f
            else -> 0f
        }
        return calibrateTurn(
            bot = bot,
            sentiment = SentimentAnalysisResult(
                tone = tone.ifBlank { "neutral" },
                valence = valence,
                intensity = abs(valence),
                isOocMeta = false,
                resistanceDelta = resDelta
            )
        )
    }

    /**
     * Batch calibration over the last [WINDOW] non-OOC user messages with EMA hysteresis
     * and personality baseline corridor clamping.
     */
    fun calibrate(bot: BotEntity, recentMessages: List<MessageEntity>): CalibrationResult {
        val old = bot.baseAffectionDifficulty.toFloat().coerceIn(FLOOR, CEILING)

        if (!AppFeatureFlags.isDynamicDifficultyEnabled) {
            return CalibrationResult(
                newDifficulty = 1.0f,
                oldDifficulty = old,
                delta = 0f,
                reason = "dinamik zorluk kapalı",
                promptDirective = ""
            )
        }

        val userMsgs = recentMessages
            .filter { it.role == "user" && !UserSentimentAnalyzer.isPureOocMessage(it.text) }
            .takeLast(WINDOW)

        if (userMsgs.size < 5) {
            return CalibrationResult(old, old, 0f, "yeterli mesaj yok (${userMsgs.size})")
        }

        val scores = userMsgs.map { scoreMessage(it.text) }
        val avgScore = scores.average().toFloat()

        val corridor = resolvePersonalityCorridor(extractAttachmentStyle(bot), bot.emotionalRegulationCapacity)

        // EMA smoothing around current score with personality baseline anchor
        val turnTarget = (old + avgScore * 0.45f).coerceIn(corridor.minMultiplier, corridor.maxMultiplier)
        val emaNext = EMA_PREV_WEIGHT * old + EMA_TURN_WEIGHT * turnTarget
        val clampedShift = (emaNext - old).coerceIn(-MAX_SHIFT, MAX_SHIFT)
        val newDiff = (old + clampedShift).coerceIn(corridor.minMultiplier, corridor.maxMultiplier)
        val actualDelta = newDiff - old

        val reason = buildString {
            append(String.format(Locale.US, "avg_sentiment=%.2f", avgScore))
            append(String.format(Locale.US, ", shift=%+.3f", actualDelta))
            if (abs(actualDelta) < 0.001f) append(" (değişim yok)")
        }

        return CalibrationResult(newDiff, old, actualDelta, reason)
    }

    private fun extractAttachmentStyle(bot: BotEntity): String {
        val fromJson = try {
            val raw = bot.baselineEmotionAnalysisJson.trim()
            if (raw.startsWith("{")) {
                val obj = org.json.JSONObject(raw)
                obj.optString("attachmentStyle", "")
                    .ifBlank { obj.optString("attachment_style", "") }
                    .ifBlank {
                        obj.optJSONObject("personalityProfile")?.let { pp ->
                            pp.optString("attachmentStyle", "")
                                .ifBlank { pp.optString("attachment_style", "") }
                        } ?: ""
                    }
            } else ""
        } catch (_: Exception) {
            ""
        }
        if (fromJson.isNotBlank()) return fromJson
        val combined = "${bot.aiPersonality} ${bot.scenario}".lowercase(TR_LOCALE)
        return when {
            combined.contains("fearful") || combined.contains("korkulu") -> "fearful_avoidant"
            combined.contains("dismissive") || combined.contains("avoidant") || combined.contains("soğuk") || combined.contains("mesafeli") || combined.contains("inatçı") -> "avoidant"
            combined.contains("anxious") || combined.contains("kaygılı") || combined.contains("terk edilme") -> "anxious"
            else -> "secure"
        }
    }

    /** Mesajı -1 (çok agresif) .. +1 (çok pozitif) arası puanlar; OOC, Türkçe olumsuzlama ve RP anlatımını dikkate alır. */
    internal fun scoreMessage(text: String): Float {
        if (text.isBlank()) return 0f
        val split = UserSentimentAnalyzer.splitIcAndOoc(text)
        if (split.isPureOoc) return 0f

        val directPart = RP_ACTION_REGEX.replace(split.icText, " ").trim()
        val rpActionsOnly = RP_ACTION_REGEX.findAll(split.icText).joinToString(" ") { it.value.trim('*') }

        val directScore = scoreSegment(directPart)
        val rpScore = if (rpActionsOnly.isNotBlank()) scoreSegment(rpActionsOnly) * 0.35f else 0f

        var score = directScore + rpScore

        val directLetters = directPart.filter { it.isLetter() }
        if (directPart.length in 3..14 &&
            directLetters.length >= 3 &&
            directPart == directPart.uppercase(TR_LOCALE)
        ) {
            score -= 0.10f
        }

        return score.coerceIn(-1f, 1f)
    }

    private fun scoreSegment(segment: String): Float {
        if (segment.isBlank()) return 0f
        val norm = TurkishTextAnalyzer.normalize(segment)
        val hasNegation = NEGATION_FOLLOW_REGEX.containsMatchIn(norm)
        var score = 0f

        for (kw in POSITIVE_KW) {
            if (TurkishTextAnalyzer.containsWordPrefix(norm, kw)) {
                if (hasNegation && isNegatedNear(norm, kw)) {
                    score -= 0.20f
                } else {
                    score += 0.20f
                }
            }
        }

        for (kw in NEGATIVE_KW) {
            if (TurkishTextAnalyzer.containsWordPrefix(norm, kw)) {
                if (hasNegation && isNegatedNear(norm, kw)) {
                    score += 0.05f
                } else {
                    score -= 0.30f
                }
            }
        }

        for (phrase in AGGRESSIVE_PHRASES) {
            if (TurkishTextAnalyzer.containsWordPrefix(norm, phrase)) {
                score -= 0.15f
            }
        }

        if (MULTI_EXCL_REGEX.containsMatchIn(segment)) score -= 0.08f
        if (MULTI_QUEST_REGEX.containsMatchIn(segment)) score -= 0.08f

        return score.coerceIn(-1f, 1f)
    }

    private fun isNegatedNear(normText: String, kw: String): Boolean {
        val idx = normText.indexOf(kw)
        if (idx < 0) return false
        val afterWindow = normText.substring((idx + kw.length).coerceAtMost(normText.length), (idx + kw.length + 18).coerceAtMost(normText.length))
        val beforeWindow = normText.substring((idx - 12).coerceAtLeast(0), idx)
        return NEGATION_FOLLOW_REGEX.containsMatchIn(afterWindow) ||
            beforeWindow.contains("hiç") || beforeWindow.contains("asla")
    }

    fun shouldRun(totalMessageCount: Int): Boolean =
        AppFeatureFlags.isDynamicDifficultyEnabled && totalMessageCount > 0 && totalMessageCount % 20 == 0
}