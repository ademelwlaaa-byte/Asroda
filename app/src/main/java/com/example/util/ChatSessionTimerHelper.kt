package com.example.util

/**
 * Formats interactive reading/writing session durations into concise Turkish labels.
 */
object ChatSessionTimerHelper {

    fun formatElapsedTurkish(elapsedMs: Long): String {
        val safeSec = (elapsedMs.coerceAtLeast(0L) / 1000L).toInt()
        val hours = safeSec / 3600
        val mins = (safeSec % 3600) / 60
        val secs = safeSec % 60
        return when {
            hours > 0 -> "$hours sa $mins dk"
            mins > 0 -> "$mins dk"
            else -> "$secs sn"
        }
    }

    fun wordsPerMinute(wordCount: Int, elapsedMs: Long): Int {
        if (wordCount <= 0 || elapsedMs < 15_000L) return 0
        val minutes = elapsedMs.toDouble() / 60_000.0
        return (wordCount.toDouble() / minutes).toInt().coerceAtLeast(1)
    }
}
