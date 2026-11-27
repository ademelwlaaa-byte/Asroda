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
        val completed = state.completedChapters.count { it in 1..safeTotal }.coerceIn(0, safeTotal)
        val pct = ((completed * 100f) / safeTotal).toInt().coerceIn(0, 100)
        val decisions = state.chapterDecisions.values.sumOf { list -> list.count { it.selectedOption.isNotBlank() } }
        val secrets = state.discoveredSecrets.count { it.isNotBlank() }
        return BookProgressSummary(
            completedChapters = completed,
            totalChapters = safeTotal,
            completionPercent = pct,
            totalDecisions = decisions,
            discoveredSecrets = secrets
        )
    }

    fun formatRibbon(summary: BookProgressSummary): String {
        return "İlerleme: %${summary.completionPercent} (${summary.completedChapters}/${summary.totalChapters} bölüm) • ${summary.totalDecisions} karar • ${summary.discoveredSecrets} sır"
    }

    fun formatSeasonBreakdown(
        state: BookReadState,
        season1Total: Int = 10,
        totalChapters: Int = CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS
    ): String {
        val safeTotal = totalChapters.coerceAtLeast(1)
        val safeS1 = season1Total.coerceIn(1, safeTotal)
        val s1Done = state.completedChapters.count { it in 1..safeS1 }
        val s2Total = (safeTotal - safeS1).coerceAtLeast(0)
        if (s2Total == 0) return "1. Sezon: $s1Done/$safeS1"
        val s2Done = state.completedChapters.count { it in (safeS1 + 1)..safeTotal }
        return "1. Sezon: $s1Done/$safeS1 • 2. Sezon: $s2Done/$s2Total"
    }
}
