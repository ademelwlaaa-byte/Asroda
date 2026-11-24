package com.example.util

import com.example.data.local.MessageEntity
import java.util.Locale

/**
 * Analyzes story turn cadence (longest single scene in words and user-to-AI word ratio).
 */
object StoryTurnCadenceAnalyzer {

    data class CadenceStats(
        val longestTurnWords: Int,
        val userToAiRatio: Double
    )

    fun analyze(messages: List<MessageEntity>): CadenceStats {
        var longest = 0
        var userWords = 0
        var aiWords = 0
        for (m in messages) {
            if (m.role != "user" && m.role != "assistant") continue
            val w = TextWordCounter.countWords(m.text)
            if (w > longest) longest = w
            if (m.role == "user") userWords += w else aiWords += w
        }
        val ratio = if (userWords > 0) aiWords.toDouble() / userWords.toDouble() else 0.0
        return CadenceStats(longestTurnWords = longest, userToAiRatio = ratio)
    }

    fun formatCompact(stats: CadenceStats): String {
        if (stats.longestTurnWords <= 0) return "Henüz sahne yok"
        val ratioPart = if (stats.userToAiRatio > 0.0) {
            " • AI/Sen: ${String.format(Locale.US, "%.1fx", stats.userToAiRatio)}"
        } else ""
        return "En uzun sahne: ${stats.longestTurnWords} kelime$ratioPart"
    }
}
