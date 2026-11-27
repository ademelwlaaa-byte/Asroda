package com.example.util

import com.example.data.local.MessageEntity

/**
 * Analyzes dialogue vs. descriptive narration balance and average AI turn length
 * for the Chat Story Stats overlay.
 */
object StoryPacingAnalyzer {

    private val dialogueRegex = Regex("\"[^\"]+\"|“[^”]+”|«[^»]+»")

    data class PacingSummary(
        val dialoguePercent: Int,
        val narrationPercent: Int,
        val averageAssistantWords: Int,
        val styleLabel: String
    )

    fun analyze(messages: List<MessageEntity>): PacingSummary {
        val storyMsgs = messages.filter { it.role == "user" || it.role == "assistant" }
        if (storyMsgs.isEmpty()) {
            return PacingSummary(0, 100, 0, "Henüz veri yok")
        }
        var totalChars = 0
        var dialogueChars = 0
        var assistantWordSum = 0
        var assistantCount = 0

        for (m in storyMsgs) {
            val text = com.example.data.repository.EmochiRepository.cleanEmotionTags(m.text).trim()
            if (text.isEmpty()) continue
            totalChars += text.length
            dialogueRegex.findAll(text).forEach { match ->
                dialogueChars += match.value.length
            }
            if (m.role == "assistant") {
                assistantCount++
                assistantWordSum += text.split(Regex("\\s+")).count { it.isNotBlank() }
            }
        }
        val dlgPct = if (totalChars > 0) ((dialogueChars * 100f) / totalChars).toInt().coerceIn(0, 100) else 0
        val narPct = (100 - dlgPct).coerceIn(0, 100)
        val avgWords = if (assistantCount > 0) assistantWordSum / assistantCount else 0
        val label = when {
            dlgPct >= 55 -> "Diyalog Odaklı"
            dlgPct >= 25 -> "Dengeli Roman"
            else -> "Betimleme Ağırlıklı"
        }
        return PacingSummary(
            dialoguePercent = dlgPct,
            narrationPercent = narPct,
            averageAssistantWords = avgWords,
            styleLabel = label
        )
    }

    fun formatCompact(summary: PacingSummary): String {
        return "${summary.styleLabel} (Diyalog %${summary.dialoguePercent} • Ort. ${summary.averageAssistantWords} kelime)"
    }
}
