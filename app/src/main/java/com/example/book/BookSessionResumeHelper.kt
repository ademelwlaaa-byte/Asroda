package com.example.book

/**
 * Builds concise Turkish resume subtitles for Book Mode cards and overlays.
 */
object BookSessionResumeHelper {

    fun buildResumeSubtitle(
        state: BookReadState,
        totalChapters: Int = CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS
    ): String {
        val summary = BookProgressSummaryFormatter.compute(state, totalChapters)
        val remaining = (summary.totalChapters - summary.completedChapters).coerceAtLeast(0)
        return when {
            summary.completedChapters == 0 && summary.totalDecisions == 0 ->
                "Yeni yolculuk • Bölüm ${state.activeChapter} seni bekliyor"
            remaining == 0 ->
                "Tamamlandı (%100) • ${summary.totalDecisions} karar • ${summary.discoveredSecrets} sır"
            else ->
                "Bölüm ${state.activeChapter} • %${summary.completionPercent} tamamlandı • $remaining bölüm kaldı"
        }
    }
}
