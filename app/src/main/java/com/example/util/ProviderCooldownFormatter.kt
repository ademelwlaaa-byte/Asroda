package com.example.util

/**
 * Formats AI provider circuit-breaker remaining cooldown durations into concise Turkish labels.
 */
object ProviderCooldownFormatter {

    fun formatRemaining(remainingMs: Long): String {
        if (remainingMs <= 0L) return "Hazır"
        val totalSec = ((remainingMs + 999L) / 1000L).toInt()
        val mins = totalSec / 60
        val secs = totalSec % 60
        return when {
            mins > 0 && secs > 0 -> "$mins dk $secs sn"
            mins > 0 -> "$mins dk"
            else -> "$secs sn"
        }
    }
}
