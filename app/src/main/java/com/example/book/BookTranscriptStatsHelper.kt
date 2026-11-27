package com.example.book

import com.example.util.StoryDialogueToNarrationRatioHelper
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
        var words = 0L
        for (item in state.transcript) {
            when (item) {
                is BookTranscriptItem.SceneBlock -> {
                    for (p in item.paragraphs) {
                        val visible = StoryDialogueToNarrationRatioHelper.stripHiddenTags(p)
                        if (visible.isNotBlank()) {
                            words += TextWordCounter.countWords(visible).coerceAtLeast(0).toLong()
                        }
                    }
                }
                is BookTranscriptItem.DecisionBlock -> {
                    val opt = StoryDialogueToNarrationRatioHelper.stripHiddenTags(item.decision.selectedOption)
                    val impact = StoryDialogueToNarrationRatioHelper.stripHiddenTags(item.decision.impactText)
                    if (opt.isNotBlank()) words += TextWordCounter.countWords(opt).coerceAtLeast(0).toLong()
                    if (impact.isNotBlank()) words += TextWordCounter.countWords(impact).coerceAtLeast(0).toLong()
                }
            }
        }
        val safeWords = words.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        val pages = if (words <= 0L) 0 else ((words + 249L) / 250L).coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
        return TranscriptStats(
            totalWords = safeWords,
            estimatedPages = pages,
            entryCount = state.transcript.size
        )
    }

    fun formatCompact(stats: TranscriptStats): String {
        if (stats.totalWords <= 0 || stats.estimatedPages <= 0) return "Henüz okuma kaydı yok"
        return "${stats.totalWords} kelime (~${stats.estimatedPages} sayfa)"
    }
}
