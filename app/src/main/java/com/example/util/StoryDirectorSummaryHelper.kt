package com.example.util

/**
 * Formats a concise Turkish one-line summary of the user's active Story Director settings.
 */
object StoryDirectorSummaryHelper {

    fun formatActiveConfig(
        responseLength: String,
        ttsSpeed: Float,
        fontScale: Float,
        hasDirectorRule: Boolean
    ): String {
        val lenLabel = when (responseLength.trim().lowercase(java.util.Locale.ROOT)) {
            "short" -> "Kısa"
            "detailed" -> "Detaylı"
            "novel" -> "Roman"
            else -> "Dengeli"
        }
        val speedLabel = String.format(java.util.Locale.US, "%.2fx", ttsSpeed.coerceIn(0.5f, 2.0f)).trimEnd('0').trimEnd('.')
            .let { if (it.endsWith("x")) it else "${it}x" }
        val fontPct = (fontScale.coerceIn(0.75f, 1.5f) * 100f).toInt()
        val ruleBadge = if (hasDirectorRule) " • Kural Aktif" else ""
        return "Uzunluk: $lenLabel • Ses: $speedLabel • Yazı: %$fontPct$ruleBadge"
    }
}
