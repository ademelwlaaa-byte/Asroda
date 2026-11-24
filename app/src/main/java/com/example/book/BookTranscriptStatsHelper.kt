package com.example.book

import com.example.util.TextWordCounter

/**
 * Computes transcript word count and estimated novel pages read in the active Book Mode session.
 */
object BookTranscriptStatsHelper {

    data class TranscriptStats(
        val totalWords: Int,
        val estimatedPages: Int,
        val entryCount: Int
    )

    fun compute(state: BookReadState): TranscriptStats {
        if (state.transcript.isEmpty()) return TranscriptStats(0, 0, 0)
        var words = 0
        for (item in state.transcript) {
            when (item) {
                is BookTranscriptItem.SceneBlock -> {
                    for (p in item.paragraphs) {
                        words += TextWordCounter.countWords(p)
                    }
                }
                is BookTranscriptItem.DecisionBlock -> {
                    words += TextWordCounter.countWords(item.decision.selectedOption)
                    words += TextWordCounter.countWords(item.decision.impactText)
                }
            }
        }
        val pages = if (words <= 0) 0 else ((words + 249) / 250).coerceAtLeast(1)
        return TranscriptStats(
            totalWords = words,
            estimatedPages = pages,
            entryCount = state.transcript.size
        )
    }

    fun formatCompact(stats: TranscriptStats): String {
        if (stats.totalWords <= 0) return "Henüz okuma kaydı yok"
        return "${stats.totalWords} kelime (~${stats.estimatedPages} sayfa)"
    }
}
