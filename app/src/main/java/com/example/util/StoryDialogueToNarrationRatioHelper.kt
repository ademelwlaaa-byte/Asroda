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

    private val HIDDEN_REASONING_REGEX = Regex(
        """<(?:think|thought|scratchpad)\b[^>]*>[\s\S]*?</(?:think|thought|scratchpad)>""",
        RegexOption.IGNORE_CASE
    )
    private val UNCLOSED_REASONING_REGEX = Regex(
        """<(?:think|thought|scratchpad)\b[^>]*>[\s\S]*$""",
        RegexOption.IGNORE_CASE
    )
    private val HIDDEN_META_BLOCK_REGEX = Regex("""\[\[[\s\S]*?\]\]""")
    private val QUOTE_REGEX = Regex("""["“«]([^"”»]{2,})["”»]""")
    private val MULTI_SPACE_REGEX = Regex("""\s+""")

    internal fun stripHiddenTags(raw: String): String {
        if (raw.isBlank()) return ""
        return raw
            .replace(HIDDEN_REASONING_REGEX, " ")
            .replace(UNCLOSED_REASONING_REGEX, " ")
            .replace(HIDDEN_META_BLOCK_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
    }

    fun analyze(messages: List<MessageEntity>): RatioSummary {
        val texts = messages
            .asSequence()
            .filter { !it.role.equals("system", ignoreCase = true) && it.text.isNotBlank() }
            .map { stripHiddenTags(it.text) }
            .filter { it.isNotBlank() }
            .toList()
        if (texts.isEmpty()) {
            return RatioSummary(0, 100, "📝 Kısa Taslak")
        }

        var totalChars = 0L
        var dialogueChars = 0L
        for (text in texts) {
            totalChars += text.length.toLong()
            QUOTE_REGEX.findAll(text).forEach { match ->
                dialogueChars += match.groupValues[1].trim().length.toLong()
            }
        }
        if (totalChars < 20L) {
            return RatioSummary(0, 100, "📝 Kısa Taslak")
        }

        val boundedDialogue = dialogueChars.coerceIn(0L, totalChars)
        val dialoguePct = ((boundedDialogue.toDouble() / totalChars.toDouble()) * 100.0)
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
        return "${summary.styleLabel} (%${summary.dialoguePercent.coerceIn(0, 100)} diyalog • %${summary.narrationPercent.coerceIn(0, 100)} betimleme)"
    }
}
