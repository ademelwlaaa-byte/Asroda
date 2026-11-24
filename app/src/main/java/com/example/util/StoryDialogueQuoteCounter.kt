package com.example.util

import com.example.data.local.MessageEntity

/**
 * Counts quoted dialogue lines and italic roleplay action cues across an interactive story.
 */
object StoryDialogueQuoteCounter {

    private val dialogueRegex = Regex("[\"“«][^\"”»]{2,}[\"”»]")
    private val actionRegex = Regex("\\*[^*\n]{2,}\\*")

    data class ExpressionCounts(
        val dialogueLines: Int,
        val actionCues: Int
    )

    fun count(messages: List<MessageEntity>): ExpressionCounts {
        var dialogues = 0
        var actions = 0
        for (m in messages) {
            if (m.role != "user" && m.role != "assistant") continue
            dialogues += dialogueRegex.findAll(m.text).count()
            actions += actionRegex.findAll(m.text).count()
        }
        return ExpressionCounts(dialogueLines = dialogues, actionCues = actions)
    }

    fun formatCompact(counts: ExpressionCounts): String {
        return "${counts.dialogueLines} replik • ${counts.actionCues} aksiyon"
    }
}
