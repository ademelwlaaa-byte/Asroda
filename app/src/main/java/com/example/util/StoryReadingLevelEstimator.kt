package com.example.util

import com.example.data.local.MessageEntity

/**
 * Estimates Turkish prose style/complexity based on average sentence length across assistant turns.
 */
object StoryReadingLevelEstimator {

    data class ProseMetrics(
        val averageWordsPerSentence: Int,
        val styleBadge: String
    )

    fun estimate(messages: List<MessageEntity>): ProseMetrics {
        val aiTexts = messages.filter { it.role == "assistant" && it.text.isNotBlank() }.map { it.text }
        if (aiTexts.isEmpty()) return ProseMetrics(0, "Henüz veri yok")

        var totalWords = 0
        var totalSentences = 0
        for (text in aiTexts) {
            val sentences = TtsSentenceChunker.splitSentences(text)
            totalSentences += sentences.size.coerceAtLeast(1)
            totalWords += TextWordCounter.countWords(text)
        }
        val avg = if (totalSentences > 0) (totalWords / totalSentences).coerceAtLeast(1) else 0
        val badge = when {
            avg >= 18 -> "Zengin Roman Dili"
            avg >= 10 -> "Dengeli Edebi"
            else -> "Akıcı & Hızlı"
        }
        return ProseMetrics(averageWordsPerSentence = avg, styleBadge = badge)
    }

    fun formatCompact(metrics: ProseMetrics): String {
        if (metrics.averageWordsPerSentence <= 0) return metrics.styleBadge
        return "${metrics.styleBadge} (Ort. ${metrics.averageWordsPerSentence} kelime/cümle)"
    }
}
