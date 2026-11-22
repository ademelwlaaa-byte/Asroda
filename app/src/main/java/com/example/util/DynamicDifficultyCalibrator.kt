package com.example.util

import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.TurkishTextAnalyzer
import java.util.Locale
import kotlin.math.abs

/**
 * Y5 — Dinamik Zorluk Ayarı (10/10)
 * Kullanıcının konuşma kalıplarını (Türkçe olumsuzlama ve RP anlatım farkındalığıyla) analiz ederek
 * `bot.baseAffectionDifficulty` kazanım hızı çarpanını kademeli olarak kalibre eder.
 *
 * Çarpan semantiği (`EmotionEngine.calculateClampedAffectionDelta` ile %100 hizalı):
 * - Yüksek çarpan (örn. 1.3–2.5) = DAHA KOLAY / HIZLI yakınlık artışı (pozitif/sıcak kullanıcı)
 * - Düşük çarpan (örn. 0.5–0.8) = DAHA ZOR / YAVAŞ yakınlık artışı (agresif/kaba kullanıcı)
 *
 * Her 20 mesajda bir (veya çağrıldığında) çalıştırılır.
 */
object DynamicDifficultyCalibrator {

    private const val WINDOW = 20          // kaç mesaja bak
    private const val MAX_SHIFT = 0.15f    // tek seferde max ±0.15 kayma
    private const val FLOOR = 0.5f
    private const val CEILING = 2.5f
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

    data class CalibrationResult(
        val newDifficulty: Float,
        val oldDifficulty: Float,
        val delta: Float,
        val reason: String
    )

    /**
     * Son [WINDOW] kullanıcı mesajını inceleyerek yeni kazanım hızı çarpanını hesaplar.
     * Özellik kapalıysa [bot.baseAffectionDifficulty] olduğu gibi döner.
     */
    fun calibrate(bot: BotEntity, recentMessages: List<MessageEntity>): CalibrationResult {
        val old = bot.baseAffectionDifficulty.toFloat()

        if (!AppFeatureFlags.isDynamicDifficultyEnabled) {
            return CalibrationResult(old, old, 0f, "dinamik zorluk kapalı")
        }

        val userMsgs = recentMessages
            .filter { it.role == "user" }
            .takeLast(WINDOW)

        if (userMsgs.size < 5) {
            return CalibrationResult(old, old, 0f, "yeterli mesaj yok (${userMsgs.size})")
        }

        val scores = userMsgs.map { scoreMessage(it.text) }
        val avgScore = scores.average().toFloat()   // -1.0 (agresif) .. +1.0 (pozitif)

        // Doğru yön: EmotionEngine maxAllowed & maxDailyBudget değerlerini baseMultiplier ile ÇARPAR.
        // Pozitif kullanıcı (+avgScore) -> çarpan ARTAR (daha kolay/hızlı kazanım)
        // Agresif/kaba kullanıcı (-avgScore) -> çarpan AZALIR (daha zor/yavaş kazanım)
        val targetShift = avgScore * 0.3f
        val clampedShift = targetShift.coerceIn(-MAX_SHIFT, MAX_SHIFT)

        val newRaw = old + clampedShift
        val newDiff = newRaw.coerceIn(FLOOR, CEILING)
        val actualDelta = newDiff - old

        val reason = buildString {
            append(String.format(Locale.US, "avg_sentiment=%.2f", avgScore))
            append(String.format(Locale.US, ", shift=%+.3f", actualDelta))
            if (abs(actualDelta) < 0.001f) append(" (değişim yok)")
        }

        return CalibrationResult(newDiff, old, actualDelta, reason)
    }

    /** Mesajı -1 (çok agresif) .. +1 (çok pozitif) arası puanlar; Türkçe olumsuzlama ve RP anlatımını dikkate alır. */
    internal fun scoreMessage(text: String): Float {
        if (text.isBlank()) return 0f

        // RP eylem anlatımını (*...*) doğrudan kullanıcı hitabından ayır; kurgusal dram kullanıcı saldırganlığı değildir
        val directPart = RP_ACTION_REGEX.replace(text, " ").trim()
        val rpActionsOnly = RP_ACTION_REGEX.findAll(text).joinToString(" ") { it.value.trim('*') }

        val directScore = scoreSegment(directPart)
        val rpScore = if (rpActionsOnly.isNotBlank()) scoreSegment(rpActionsOnly) * 0.35f else 0f

        var score = directScore + rpScore

        // Yalnızca doğrudan mesaj kısa + tamamen büyük harf ise agresiflik sinyali ver
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
                // "mutlu değilim", "iyi değil", "güzel değil" gibi olumsuzlanmış pozitif ifadeler tersine döner
                if (hasNegation && isNegatedNear(norm, kw)) {
                    score -= 0.20f
                } else {
                    score += 0.20f
                }
            }
        }

        for (kw in NEGATIVE_KW) {
            if (TurkishTextAnalyzer.containsWordPrefix(norm, kw)) {
                score -= 0.30f
            }
        }

        for (phrase in AGGRESSIVE_PHRASES) {
            if (TurkishTextAnalyzer.containsWordPrefix(norm, phrase)) {
                score -= 0.15f
            }
        }

        // Çift sayma olmadan ünlem/soru yığılması kontrolü (!! ile !!! bir kez sayılır)
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

    /** Her 20 mesajda bir tetiklenmesi gerekip gerekmediğini kontrol eder. */
    fun shouldRun(totalMessageCount: Int): Boolean =
        AppFeatureFlags.isDynamicDifficultyEnabled && totalMessageCount > 0 && totalMessageCount % 20 == 0
}