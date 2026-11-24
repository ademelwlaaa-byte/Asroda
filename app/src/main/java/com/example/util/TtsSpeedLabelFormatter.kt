package com.example.util

import java.util.Locale

/**
 * Maps numeric TTS playback speed multipliers into friendly Turkish labels.
 */
object TtsSpeedLabelFormatter {

    fun formatLabel(speed: Float): String {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        val num = String.format(Locale.US, "%.2f", clamped).trimEnd('0').trimEnd('.')
        val tag = when {
            clamped < 0.92f -> "Yavaş"
            clamped <= 1.07f -> "Doğal"
            clamped <= 1.22f -> "Akıcı"
            else -> "Hızlı"
        }
        return "$tag (${num}x)"
    }
}
