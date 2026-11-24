package com.example.util

import com.example.data.local.MessageEntity
import kotlin.math.roundToInt

/**
 * Computes the proportion of spoken dialogue (text inside quotation marks)
 * versus descriptive narration across a story's messages.
 */
object StoryDialogueToNarrationRatioHelper {

    data class RatioSummary(
        val dialoguePercent: Int,
        val narrationPercent: Int,
        val styleLabel: String
    )

    private val QUOTE_REGEX = Regex("[\"“«]([^\"”»]{2,})[\"”»]")

    fun analyze(messages: List<MessageEntity>): RatioSummary {
        val texts = messages
            .filter { it.text.isNotBlank() && it.role != "system" }
            .map { it.text.trim() }
        if (texts.isEmpty()) {
            return RatioSummary(0, 100, "📝 Kısa Taslak")
        }

        var totalChars = 0
        var dialogueChars = 0
        for (text in texts) {
            totalChars += text.length
            QUOTE_REGEX.findAll(text).forEach { match ->
                dialogueChars += match.groupValues[1].trim().length
            }
        }
        if (totalChars < 20) {
            return RatioSummary(0, 100, "📝 Kısa Taslak")
        }

        val dialoguePct = ((dialogueChars.toDouble() / totalChars.toDouble()) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
        val narrationPct = (100 - dialoguePct).coerceIn(0, 100)

        val styleLabel = when {
            dialoguePct >= 50 -> "🎭 Yoğun Diyalog"
            dialoguePct >= 25 -> "⚖️ Dengeli Roman"
            else -> "📖 Betimleyici Anlatı"
        }
        return RatioSummary(dialoguePct, narrationPct, styleLabel)
    }

    fun formatCompact(summary: RatioSummary): String {
        if (summary.styleLabel == "📝 Kısa Taslak") return summary.styleLabel
        return "${summary.styleLabel} (%${summary.dialoguePercent} diyalog • %${summary.narrationPercent} betimleme)"
    }
}
