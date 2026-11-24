package com.example.util

/**
 * Classifies an interactive story's scale into evocative Turkish literary tiers.
 */
object StoryDepthTierClassifier {

    data class DepthTier(
        val title: String,
        val emoji: String
    )

    fun classify(totalWordCount: Int): DepthTier {
        return when {
            totalWordCount >= 25_000 -> DepthTier("Efsanevi Destan", "👑")
            totalWordCount >= 10_000 -> DepthTier("Epik Roman", "📚")
            totalWordCount >= 3_000 -> DepthTier("Uzun Bölüm", "📖")
            totalWordCount >= 600 -> DepthTier("Gelişen Hikaye", "✨")
            else -> DepthTier("Yeni Başlangıç", "🌱")
        }
    }

    fun formatLabel(totalWordCount: Int): String {
        val tier = classify(totalWordCount)
        return "${tier.emoji} ${tier.title}"
    }
}
