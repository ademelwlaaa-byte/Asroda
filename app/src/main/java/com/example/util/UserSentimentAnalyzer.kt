package com.example.util

import com.example.data.local.MessageEntity
import com.example.data.repository.TurkishTextAnalyzer

enum class UserMood(val label: String, val systemHint: String) {
    POSITIVE("Pozitif", "Kullanıcı şu an iyi bir ruh halinde görünüyor. Enerjik ve samimi yanıtlar ver."),
    EXCITED("Heyecanlı", "Kullanıcı heyecanlı ve coşkulu görünüyor. Aynı enerjiyi yansıt, neşeli ol."),
    NEUTRAL("Nötr", ""),
    NEGATIVE("Negatif", "Kullanıcı şu an mutsuz veya gergin görünüyor. Nazik, anlayışlı ve destekleyici ol."),
    WITHDRAWN("Mesafeli", "Kullanıcı kısa ve ilgisiz yanıtlar veriyor. Zorlamadan ilgisini çekmeye çalış.")
}

/**
 * Turkish-locale, negation-aware & RP-action-aware User Sentiment Analyzer.
 * Strips `*action*` narration before scoring so dramatic RP prose never triggers false NEGATIVE hints,
 * handles Turkish negations ("mutlu değilim", "iyi değilim"), and avoids `!!`/`!!!` double-counting.
 */
object UserSentimentAnalyzer {

    private val positiveWords = setOf(
        "mutlu", "harika", "süper", "güzel", "seviyorum", "mükemmel", "iyi", "güldüm",
        "teşekkür", "sevdim", "eğlendim", "keyifli", "memnun", "sevinçli", "harikaydı", "bayıldım"
    )

    private val positiveEmojis = setOf("😊", "😄", "🥰", "❤️", "💕", "✨", "🎉", "👍")

    private val negativeWords = setOf(
        "üzgün", "kötü", "sinirli", "sıkıldım", "yoruldum", "bıktım", "nefret",
        "berbat", "korkunç", "rezalet", "mutsuz", "bunaldım", "yorucu", "sıkıcı",
        "kızgın", "bozuldum", "hayal kırıklığı"
    )

    private val negativeEmojis = setOf("😢", "😭", "😡", "😤", "💔")

    private val excitedWords = setOf("wow", "aman", "inanılmaz", "şaşırdım", "beklemiyordum", "oley", "yaşasın")

    private val RP_ACTION_REGEX = Regex("""\*[^*]{1,400}\*""")
    private val MULTI_EXCL_REGEX = Regex("""!{2,}""")
    private val NEGATION_REGEX = Regex("""(?<![\p{L}\p{N}])(değil\p{L}*|degil\p{L}*|yok)(?![\p{L}\p{N}])""")

    fun analyze(messages: List<MessageEntity>, windowSize: Int = 3): UserMood {
        val userMessages = messages.filter { it.role == "user" }.takeLast(windowSize)
        if (userMessages.isEmpty()) return UserMood.NEUTRAL

        var positiveScore = 0
        var negativeScore = 0
        var excitedScore = 0
        var withdrawnScore = 0

        for (msg in userMessages) {
            // Strip *action* RP narration so dramatic roleplay scenes don't skew out-of-character mood
            val directRaw = RP_ACTION_REGEX.replace(msg.text, " ").trim()
            val norm = TurkishTextAnalyzer.normalize(directRaw)
            if (norm.isBlank()) continue

            val words = norm.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (words.size <= 2 && positiveEmojis.none { directRaw.contains(it) } && negativeEmojis.none { directRaw.contains(it) }) {
                withdrawnScore++
            }

            val hasNegation = NEGATION_REGEX.containsMatchIn(norm)

            for (word in positiveWords) {
                if (TurkishTextAnalyzer.containsWordPrefix(norm, word)) {
                    if (hasNegation && isNegatedNear(norm, word)) {
                        negativeScore++
                    } else {
                        positiveScore++
                    }
                }
            }
            for (emoji in positiveEmojis) {
                if (directRaw.contains(emoji)) positiveScore++
            }

            for (word in negativeWords) {
                if (TurkishTextAnalyzer.containsWordPrefix(norm, word)) {
                    if (hasNegation && isNegatedNear(norm, word)) {
                        positiveScore++
                    } else {
                        negativeScore++
                    }
                }
            }
            for (emoji in negativeEmojis) {
                if (directRaw.contains(emoji)) negativeScore++
            }

            for (word in excitedWords) {
                if (TurkishTextAnalyzer.containsWordPrefix(norm, word)) excitedScore++
            }

            // Single check for exclamation bursts (no double-counting "!!" inside "!!!")
            if (MULTI_EXCL_REGEX.containsMatchIn(directRaw)) {
                excitedScore++
            }
        }

        // Withdrawn: majority short messages, no positive/negative/excited signal
        if (withdrawnScore >= 2 && positiveScore == 0 && negativeScore == 0 && excitedScore == 0) {
            return UserMood.WITHDRAWN
        }

        val maxScore = maxOf(positiveScore, negativeScore, excitedScore)
        if (maxScore == 0) return UserMood.NEUTRAL

        return when {
            excitedScore >= maxScore && excitedScore > 0 && negativeScore == 0 -> UserMood.EXCITED
            negativeScore > positiveScore -> UserMood.NEGATIVE
            positiveScore >= negativeScore && positiveScore > 0 -> UserMood.POSITIVE
            else -> UserMood.NEUTRAL
        }
    }

    private fun isNegatedNear(normText: String, kw: String): Boolean {
        val idx = normText.indexOf(kw)
        if (idx < 0) return false
        val after = normText.substring((idx + kw.length).coerceAtMost(normText.length), (idx + kw.length + 18).coerceAtMost(normText.length))
        val before = normText.substring((idx - 12).coerceAtLeast(0), idx)
        return NEGATION_REGEX.containsMatchIn(after) || before.contains("hiç") || before.contains("asla")
    }

    fun buildMoodHint(mood: UserMood): String {
        if (mood.systemHint.isBlank()) return ""
        return "\n\n[KULLANICI DUYGU SENSÖRÜ - GİZLİ]\n${mood.systemHint}"
    }
}
