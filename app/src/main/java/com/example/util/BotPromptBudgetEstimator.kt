package com.example.util

import com.example.data.local.BotEntity

/**
 * Estimates static system-prompt token overhead for a character/universe card.
 */
object BotPromptBudgetEstimator {

    data class BudgetReport(
        val estimatedTokens: Int,
        val tierLabel: String
    )

    fun estimate(bot: BotEntity): BudgetReport {
        val combined = buildString {
            append(bot.aiPersonality).append(' ')
            append(bot.scenario).append(' ')
            append(bot.userCharDesc).append(' ')
            append(bot.pinnedMemory).append(' ')
            append(bot.storyNotes).append(' ')
            append(bot.openingMessage)
        }
        val words = TextWordCounter.countWords(combined)
        val tokens = TokenBudgetEstimator.estimateTokens(words)
        val tier = when {
            tokens >= 2500 -> "Yoğun"
            tokens >= 900 -> "Dengeli"
            else -> "Hafif"
        }
        return BudgetReport(estimatedTokens = tokens, tierLabel = tier)
    }

    fun formatCompact(report: BudgetReport): String {
        return "Sistem Yükü: ~${TokenBudgetEstimator.formatTokenCount(report.estimatedTokens)} (${report.tierLabel})"
    }
}
