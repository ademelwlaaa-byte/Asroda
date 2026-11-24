package com.example.util

import com.example.data.local.BotEntity

/**
 * Evaluates how complete and well-crafted a character/universe card is (%0..%100).
 */
object BotCardCompletenessHelper {

    data class Completeness(
        val percent: Int,
        val label: String
    )

    fun evaluate(bot: BotEntity): Completeness {
        var score = 0
        if (bot.aiName.isNotBlank() || bot.universeName.isNotBlank()) score += 20
        if (bot.aiPersonality.trim().length >= 15) score += 20
        if (bot.scenario.trim().length >= 15) score += 20
        if (bot.openingMessage.trim().length >= 15) score += 20
        if (bot.userCharName.isNotBlank()) score += 10
        if (bot.avatarUrl.isNotBlank() || bot.pinnedMemory.isNotBlank()) score += 10
        val label = when {
            score >= 90 -> "✦ Eksiksiz Kart"
            score >= 70 -> "✓ Hazır"
            else -> "🌱 Geliştirilebilir"
        }
        return Completeness(percent = score.coerceIn(0, 100), label = label)
    }
}
