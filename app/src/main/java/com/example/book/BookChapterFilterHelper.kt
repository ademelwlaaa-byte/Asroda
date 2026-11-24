package com.example.book

/**
 * Computes Book Mode chapter completion counts and filter labels for quick navigation.
 */
object BookChapterFilterHelper {

    data class ChapterFilterCounts(
        val total: Int,
        val completed: Int,
        val remaining: Int
    )

    fun computeCounts(
        state: BookReadState,
        totalChapters: Int = CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS
    ): ChapterFilterCounts {
        val safeTotal = totalChapters.coerceAtLeast(1)
        val done = state.completedChapters.count { it in 1..safeTotal }
        val rem = (safeTotal - done).coerceAtLeast(0)
        return ChapterFilterCounts(total = safeTotal, completed = done, remaining = rem)
    }

    fun formatCountsLine(counts: ChapterFilterCounts): String {
        return "Tümü (${counts.total}) • Tamamlanan (${counts.completed}) • Kalan (${counts.remaining})"
    }
}
