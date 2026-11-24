package com.example.util

/**
 * Tracks progress toward the next literary word-count milestone (1K -> 5K -> 10K -> 25K -> 50K).
 */
object StoryWordMilestoneHelper {

    private val milestones = listOf(1_000, 5_000, 10_000, 25_000, 50_000, 100_000)

    fun formatNextMilestone(totalWordCount: Int): String {
        val safe = totalWordCount.coerceAtLeast(0)
        val next = milestones.firstOrNull { it > safe }
            ?: return "👑 100.000+ kelime efsane seviyesi!"
        val remaining = next - safe
        return "Sonraki hedef (${next / 1000}K): $remaining kelime kaldı"
    }
}
