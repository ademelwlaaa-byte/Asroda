package com.example.util

import com.example.data.local.BotEntity

/**
 * Turkish diacritic-tolerant multi-field bot library search filter.
 */
object BotSearchFilterHelper {

    fun matches(bot: BotEntity, rawQuery: String): Boolean {
        val q = ChatSearchHelper.foldTurkish(rawQuery.trim())
        if (q.isEmpty()) return true
        return ChatSearchHelper.foldTurkish(bot.aiName).contains(q) ||
            ChatSearchHelper.foldTurkish(bot.universeName).contains(q) ||
            ChatSearchHelper.foldTurkish(bot.scenario).contains(q) ||
            ChatSearchHelper.foldTurkish(bot.aiPersonality).contains(q) ||
            ChatSearchHelper.foldTurkish(bot.writingStyle).contains(q)
    }
}
