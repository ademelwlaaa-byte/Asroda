package com.example.util

import com.example.data.local.BotEntity

/**
 * Computes concise Turkish summary counts for the user's bot & universe library.
 */
object BotCategoryCountHelper {

    data class LibraryCounts(
        val total: Int,
        val personal: Int,
        val universe: Int,
        val favorites: Int,
        val withDrafts: Int
    )

    fun compute(
        bots: List<BotEntity>,
        favoriteIds: Set<String> = emptySet(),
        draftBotIds: Set<String> = emptySet()
    ): LibraryCounts {
        if (bots.isEmpty()) return LibraryCounts(0, 0, 0, 0, 0)
        var total = 0
        var personal = 0
        var universe = 0
        var favs = 0
        var drafts = 0
        val seenIds = HashSet<String>(bots.size)
        for (b in bots) {
            if (b.id.isNotBlank() && !seenIds.add(b.id)) continue
            total++
            if (b.mode.trim().equals("universe", ignoreCase = true)) universe++ else personal++
            if (b.id in favoriteIds) favs++
            if (b.id in draftBotIds) drafts++
        }
        return LibraryCounts(
            total = total,
            personal = personal,
            universe = universe,
            favorites = favs,
            withDrafts = drafts
        )
    }

    fun formatSummary(counts: LibraryCounts): String {
        val parts = mutableListOf<String>()
        parts.add("${counts.total.coerceAtLeast(0)} hikaye")
        if (counts.universe > 0) parts.add("${counts.universe} evren")
        if (counts.favorites > 0) parts.add("${counts.favorites} favori")
        if (counts.withDrafts > 0) parts.add("${counts.withDrafts} taslak")
        return parts.joinToString(" • ")
    }
}
