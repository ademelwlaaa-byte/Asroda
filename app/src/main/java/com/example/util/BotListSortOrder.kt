package com.example.util

import com.example.data.local.BotEntity
import java.text.Collator
import java.util.Locale

enum class BotListSortOrder(val labelTr: String) {
    RECENT("Son Aktivite"),
    NAME_AZ("A → Z"),
    FAVORITES_FIRST("Favoriler"),
    DRAFTS_FIRST("Taslaklar")
}

object BotListSorter {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    private fun turkishSortKey(raw: String): String =
        raw.trim().lowercase(trLocale).replace("\u0307", "")

    private fun compareTurkishNames(a: BotEntity, b: BotEntity): Int {
        val keyA = turkishSortKey(displayNameOf(a))
        val keyB = turkishSortKey(displayNameOf(b))
        val cmp = try {
            Collator.getInstance(trLocale).compare(keyA, keyB)
        } catch (_: Exception) {
            keyA.compareTo(keyB)
        }
        return if (cmp != 0) cmp else a.id.compareTo(b.id)
    }

    private fun recencyTimestampOf(bot: BotEntity): Long =
        maxOf(bot.lastMessageTimestamp, bot.updatedAt)

    fun sortBots(
        bots: List<BotEntity>,
        order: BotListSortOrder,
        favoriteIds: Set<String>,
        draftBotIds: Set<String>
    ): List<BotEntity> {
        if (bots.size <= 1) return bots.toList()
        return when (order) {
            BotListSortOrder.RECENT -> bots.sortedWith(
                compareByDescending<BotEntity> { it.id in favoriteIds }
                    .thenByDescending { it.lastMessageTimestamp }
                    .thenByDescending { recencyTimestampOf(it) }
                    .thenComparator(::compareTurkishNames)
            )
            BotListSortOrder.NAME_AZ -> bots.sortedWith(::compareTurkishNames)
            BotListSortOrder.FAVORITES_FIRST -> bots.sortedWith(
                compareByDescending<BotEntity> { it.id in favoriteIds }
                    .thenByDescending { it.lastMessageTimestamp }
                    .thenByDescending { recencyTimestampOf(it) }
                    .thenComparator(::compareTurkishNames)
            )
            BotListSortOrder.DRAFTS_FIRST -> bots.sortedWith(
                compareByDescending<BotEntity> { it.id in draftBotIds }
                    .thenByDescending { it.id in favoriteIds }
                    .thenByDescending { it.lastMessageTimestamp }
                    .thenByDescending { recencyTimestampOf(it) }
                    .thenComparator(::compareTurkishNames)
            )
        }
    }

    private fun displayNameOf(bot: BotEntity): String {
        return if (bot.mode.trim().equals("universe", ignoreCase = true)) {
            bot.universeName.ifBlank { bot.aiName }
        } else {
            bot.aiName.ifBlank { bot.universeName }
        }
    }
}
