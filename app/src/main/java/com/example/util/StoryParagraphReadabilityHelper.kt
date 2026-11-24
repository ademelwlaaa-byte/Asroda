package com.example.util

import com.example.data.local.MessageEntity

/**
 * Evaluates visual paragraph readability across assistant turns in an interactive story.
 */
object StoryParagraphReadabilityHelper {

    data class ReadabilityReport(
        val avgWordsPerParagraph: Int,
        val label: String
    )

    fun evaluate(messages: List<MessageEntity>): ReadabilityReport {
        val aiMsgs = messages.filter { it.role == "assistant" && it.text.isNotBlank() }
        if (aiMsgs.isEmpty()) return ReadabilityReport(0, "Henüz veri yok")

        var totalWords = 0
        var totalParagraphs = 0
        for (m in aiMsgs) {
            totalWords += TextWordCounter.countWords(m.text)
            totalParagraphs += TextWordCounter.countParagraphs(m.text).coerceAtLeast(1)
        }
        val avg = if (totalParagraphs > 0) (totalWords / totalParagraphs).coerceAtLeast(1) else 0
        val label = when {
            avg <= 45 -> "Çok Rahat"
            avg <= 90 -> "Dengeli"
            else -> "Yoğun"
        }
        return ReadabilityReport(avgWordsPerParagraph = avg, label = label)
    }
}
