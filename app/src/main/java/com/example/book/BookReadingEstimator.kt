package com.example.book

import kotlin.math.ceil

/**
 * Estimates remaining reading time in minutes for a deterministic Book Mode chapter
 * based on remaining scenes and observed average words per scene.
 */
object BookReadingEstimator {

    private const val DEFAULT_WORDS_PER_SCENE = 260
    private const val WORDS_PER_MINUTE = 190.0

    fun estimateRemainingMinutes(
        currentSceneOrder: Int,
        totalScenes: Int,
        isChapterCompleted: Boolean,
        observedTranscriptWordCount: Int = 0,
        observedSceneCount: Int = 0
    ): Int {
        if (isChapterCompleted || totalScenes <= 0) return 0
        val clampedCurrent = currentSceneOrder.coerceIn(1, totalScenes)
        val remainingScenes = (totalScenes - clampedCurrent + 1).coerceAtLeast(1)
        val avgWordsPerScene = if (observedSceneCount > 0 && observedTranscriptWordCount > 0) {
            (observedTranscriptWordCount / observedSceneCount).coerceIn(140, 650)
        } else {
            DEFAULT_WORDS_PER_SCENE
        }
        val estimatedWords = remainingScenes * avgWordsPerScene
        return ceil(estimatedWords / WORDS_PER_MINUTE).toInt().coerceAtLeast(1)
    }

    fun formatBadgeText(remainingMinutes: Int): String {
        return if (remainingMinutes <= 0) "Bitti" else "~$remainingMinutes dk"
    }
}
