package com.example.book

/**
 * Computes structured progress metrics and Turkish summary ribbons for Book Mode.
 */
object BookProgressSummaryFormatter {

    data class BookProgressSummary(
        val completedChapters: Int,
        val totalChapters: Int,
        val completionPercent: Int,
        val totalDecisions: Int,
        val discoveredSecrets: Int
    )

    fun compute(
        state: BookReadState,
        totalChapters: Int = CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS
    ): BookProgressSummary {
        val safeTotal = totalChapters.coerceAtLeast(1)
        val completed = state.completedChapters.size.coerceIn(0, safeTotal)
        val pct = ((completed * 100f) / safeTotal).toInt().coerceIn(0, 100)
        val decisions = state.chapterDecisions.values.sumOf { it.size }
        return BookProgressSummary(
            completedChapters = completed,
            totalChapters = safeTotal,
            completionPercent = pct,
            totalDecisions = decisions,
            discoveredSecrets = state.discoveredSecrets.size
        )
    }

    fun formatRibbon(summary: BookProgressSummary): String {
        return "İlerleme: %${summary.completionPercent} (${summary.completedChapters}/${summary.totalChapters} bölüm) • ${summary.totalDecisions} karar • ${summary.discoveredSecrets} sır"
    }
}
