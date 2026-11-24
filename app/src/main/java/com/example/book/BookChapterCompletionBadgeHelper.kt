package com.example.book

/**
 * Maps Book Mode overall completion percentage to an evocative Turkish reader title.
 */
object BookChapterCompletionBadgeHelper {

    fun resolveBadge(completionPercent: Int): String {
        val pct = completionPercent.coerceIn(0, 100)
        return when {
            pct >= 100 -> "👑 Evrenin Hakimi"
            pct >= 60 -> "✦ Kozmik Kaşif"
            pct >= 25 -> "🕯 Sır Avcısı"
            pct > 0 -> "📖 Yolculuk Başladı"
            else -> "🌱 İlk Adım"
        }
    }
}
