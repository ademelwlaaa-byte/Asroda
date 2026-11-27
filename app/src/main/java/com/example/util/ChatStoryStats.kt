package com.example.util

import com.example.data.local.MessageEntity
import kotlin.math.ceil

/**
 * Computes editorial story statistics for a roleplay chat session (novel pages, word counts,
 * reading time, turn balance) with zero allocations on empty/invalid messages.
 */
object ChatStoryStats {

    data class Summary(
        val totalMessages: Int,
        val userMessages: Int,
        val assistantMessages: Int,
        val userWordCount: Int,
        val assistantWordCount: Int,
        val totalWordCount: Int,
        val estimatedNovelPages: Int,
        val estimatedReadingMinutes: Int,
        val firstTimestampMs: Long,
        val lastTimestampMs: Long
    )

    private val wordSplitRegex = Regex("\\s+")

    fun countWords(text: String): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0
        return trimmed.split(wordSplitRegex).count { it.isNotBlank() }
    }

    fun compute(messages: List<MessageEntity>): Summary {
        val valid = messages.filter {
            (it.role == "user" || it.role == "assistant") &&
                it.status != "error" &&
                it.text.isNotBlank()
        }
        if (valid.isEmpty()) {
            return Summary(
                totalMessages = 0,
                userMessages = 0,
                assistantMessages = 0,
                userWordCount = 0,
                assistantWordCount = 0,
                totalWordCount = 0,
                estimatedNovelPages = 0,
                estimatedReadingMinutes = 0,
                firstTimestampMs = 0L,
                lastTimestampMs = 0L
            )
        }

        var userMsgs = 0
        var aiMsgs = 0
        var userWords = 0
        var aiWords = 0
        var minTs = Long.MAX_VALUE
        var maxTs = 0L

        for (m in valid) {
            val cleanText = com.example.data.repository.EmochiRepository.cleanEmotionTags(m.text)
            val w = countWords(cleanText)
            if (m.role == "user") {
                userMsgs++
                userWords += w
            } else {
                aiMsgs++
                aiWords += w
            }
            if (m.timestamp > 0L) {
                if (m.timestamp < minTs) minTs = m.timestamp
                if (m.timestamp > maxTs) maxTs = m.timestamp
            }
        }

        val totalWords = userWords + aiWords
        val pages = if (totalWords == 0) 0 else ceil(totalWords / 250.0).toInt().coerceAtLeast(1)
        val minutes = if (totalWords == 0) 0 else ceil(totalWords / 200.0).toInt().coerceAtLeast(1)

        return Summary(
            totalMessages = valid.size,
            userMessages = userMsgs,
            assistantMessages = aiMsgs,
            userWordCount = userWords,
            assistantWordCount = aiWords,
            totalWordCount = totalWords,
            estimatedNovelPages = pages,
            estimatedReadingMinutes = minutes,
            firstTimestampMs = if (minTs == Long.MAX_VALUE) 0L else minTs,
            lastTimestampMs = maxTs
        )
    }
}
