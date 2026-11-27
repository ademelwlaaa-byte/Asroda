package com.example.util

import android.content.Context

/**
 * Persists user's preferred Chat bubble font scale (0.90x .. 1.25x) so long literary roleplays
 * stay comfortable on any phone screen size.
 */
object ChatTypographyStore {

    private const val PREFS_NAME = "asroda_chat_typography_v1"
    private const val KEY_FONT_SCALE = "chat_font_scale"
    const val MIN_SCALE = 0.88f
    const val MAX_SCALE = 1.30f

    fun loadFontScale(context: Context): Float {
        return try {
            val ctx = context.applicationContext ?: context
            val raw = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getFloat(KEY_FONT_SCALE, 1.0f)
            if (raw.isFinite()) raw.coerceIn(MIN_SCALE, MAX_SCALE) else 1.0f
        } catch (_: Exception) {
            1.0f
        }
    }

    fun saveFontScale(context: Context, scale: Float): Float {
        val clamped = if (scale.isFinite()) scale.coerceIn(MIN_SCALE, MAX_SCALE) else 1.0f
        try {
            val ctx = context.applicationContext ?: context
            ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putFloat(KEY_FONT_SCALE, clamped)
                .apply()
        } catch (_: Exception) {}
        return clamped
    }
}
