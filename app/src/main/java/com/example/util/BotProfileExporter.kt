package com.example.util

import com.example.data.local.BotEntity

/**
 * Exports a BotEntity character or universe card as clean, shareable Markdown.
 */
object BotProfileExporter {

    fun formatCharacterCardMarkdown(bot: BotEntity): String {
        val isUniverse = bot.mode == "universe"
        val title = if (isUniverse) {
            bot.universeName.ifBlank { bot.aiName.ifBlank { "Evren" } }
        } else {
            bot.aiName.ifBlank { "Karakter" }
        }
        return buildString {
            append("# ").append(title).append(if (isUniverse) " (Evren Kartı)" else " (Karakter Kartı)").append("\n\n")
            if (bot.userCharName.isNotBlank()) {
                append("- **Oyuncu Karakteri:** ").append(bot.userCharName.trim()).append("\n")
            }
            if (bot.writingStyle.isNotBlank()) {
                append("- **Yazım Tarzı:** ").append(bot.writingStyle.trim()).append("\n")
            }
            if (bot.aiPersonality.isNotBlank()) {
                append("\n## Kişilik & Kurallar\n").append(bot.aiPersonality.trim()).append("\n")
            }
            if (bot.scenario.isNotBlank()) {
                append("\n## Senaryo\n").append(bot.scenario.trim()).append("\n")
            }
            if (bot.openingMessage.isNotBlank()) {
                append("\n## Açılış Sahnesi\n").append(bot.openingMessage.trim()).append("\n")
            }
        }.trim()
    }
}
