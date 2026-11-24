package com.example.util

import com.example.data.local.BotEntity
import java.util.Locale

enum class BotListSortOrder(val labelTr: String) {
    RECENT("Son Aktivite"),
    NAME_AZ("A → Z"),
    FAVORITES_FIRST("Favoriler"),
    DRAFTS_FIRST("Taslaklar")
}

object BotListSorter {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    fun sortBots(
        bots: List<BotEntity>,
        order: BotListSortOrder,
        favoriteIds: Set<String>,
        draftBotIds: Set<String>
    ): List<BotEntity> {
        return when (order) {
            BotListSortOrder.RECENT -> bots
            BotListSortOrder.NAME_AZ -> bots.sortedBy { displayNameOf(it).lowercase(trLocale) }
            BotListSortOrder.FAVORITES_FIRST -> bots.sortedWith(
                compareByDescending<BotEntity> { it.id in favoriteIds }
                    .thenBy { displayNameOf(it).lowercase(trLocale) }
            )
            BotListSortOrder.DRAFTS_FIRST -> bots.sortedWith(
                compareByDescending<BotEntity> { it.id in draftBotIds }
                    .thenByDescending { it.id in favoriteIds }
            )
        }
    }

    private fun displayNameOf(bot: BotEntity): String {
        return if (bot.mode == "universe") {
            bot.universeName.ifBlank { bot.aiName }
        } else {
            bot.aiName.ifBlank { bot.universeName }
        }
    }
}
