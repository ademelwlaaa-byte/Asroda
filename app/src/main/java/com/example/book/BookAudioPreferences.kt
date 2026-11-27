package com.example.book

import android.content.Context

/**
 * Persists Book Mode soundtrack volume preference (0.15f .. 1.0f) across chapters and sessions.
 */
object BookAudioPreferences {

    private const val PREFS_NAME = "asroda_book_audio_prefs_v1"
    private const val KEY_MUSIC_VOLUME = "ambient_music_volume"
    const val MIN_VOLUME = 0.15f
    const val MAX_VOLUME = 1.0f

    fun loadMusicVolume(context: Context): Float {
        return try {
            val ctx = context.applicationContext ?: context
            val raw = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getFloat(KEY_MUSIC_VOLUME, 0.85f)
            if (raw.isFinite()) raw.coerceIn(MIN_VOLUME, MAX_VOLUME) else 0.85f
        } catch (_: Exception) {
            0.85f
        }
    }

    fun saveMusicVolume(context: Context, volume: Float): Float {
        val clamped = if (volume.isFinite()) volume.coerceIn(MIN_VOLUME, MAX_VOLUME) else 0.85f
        try {
            val ctx = context.applicationContext ?: context
            ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putFloat(KEY_MUSIC_VOLUME, clamped)
                .apply()
        } catch (_: Exception) {}
        return clamped
    }
}
