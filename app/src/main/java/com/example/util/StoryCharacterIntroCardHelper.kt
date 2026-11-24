package com.example.util

import com.example.data.local.BotEntity

/**
 * Generates a concise Turkish elevator-pitch summary for any character or universe card.
 */
object StoryCharacterIntroCardHelper {

    fun buildPitch(bot: BotEntity): String {
        val name = bot.aiName.ifBlank { bot.universeName.ifBlank { "İsimsiz Hikaye" } }
        val modeLabel = if (bot.mode == "universe") "Evren" else "Karakter"
        val budget = BotPromptBudgetEstimator.estimate(bot)
        val snippet = (if (bot.mode == "universe") bot.scenario else bot.aiPersonality)
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(140)
            .ifBlank { "Özel kurgulanmış etkileşimli hikaye." }
        return "$name ($modeLabel • ${budget.tierLabel}) — $snippet"
    }
}
