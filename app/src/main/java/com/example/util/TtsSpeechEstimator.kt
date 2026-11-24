package com.example.util

import kotlin.math.roundToInt

/**
 * Estimates spoken TTS audio duration for Turkish literary text given a playback speed factor.
 */
object TtsSpeechEstimator {

    /** Average comfortable Turkish expressive narration speed (words per minute at 1.0x). */
    const val BASE_TURKISH_WPM = 135.0

    fun estimateSeconds(wordCount: Int, speedMultiplier: Float = 1.0f): Int {
        if (wordCount <= 0) return 0
        val safeSpeed = speedMultiplier.coerceIn(0.5f, 2.5f).toDouble()
        val effectiveWpm = BASE_TURKISH_WPM * safeSpeed
        val minutes = wordCount.toDouble() / effectiveWpm
        return (minutes * 60.0).roundToInt().coerceAtLeast(1)
    }

    fun formatDurationTurkish(totalSeconds: Int): String {
        if (totalSeconds <= 0) return "0 sn"
        val hours = totalSeconds / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return when {
            hours > 0 -> "~${hours} sa ${mins} dk"
            mins > 0 && secs > 0 -> "~${mins} dk ${secs} sn"
            mins > 0 -> "~${mins} dk"
            else -> "~${secs} sn"
        }
    }
}
