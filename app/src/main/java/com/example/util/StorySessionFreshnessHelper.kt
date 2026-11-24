package com.example.util

/**
 * Formats how recently a story session was active into a concise Turkish freshness badge.
 */
object StorySessionFreshnessHelper {

    fun formatFreshness(lastTimestampMs: Long, nowMs: Long = System.currentTimeMillis()): String {
        if (lastTimestampMs <= 0L) return "🆕 Henüz başlamadı"
        val deltaMs = (nowMs - lastTimestampMs).coerceAtLeast(0L)
        val minutes = deltaMs / 60_000L
        val hours = deltaMs / 3_600_000L
        val days = deltaMs / 86_400_000L
        return when {
            minutes < 15L -> "🟢 Az önce aktif"
            hours < 24L -> "🕒 Bugün aktif (${hours.coerceAtLeast(1L)} sa önce)"
            days < 7L -> "📅 $days gün önce"
            days < 30L -> "📖 ${days / 7L} hafta önce"
            else -> "🕸️ Uzun süredir bekliyor"
        }
    }
}
