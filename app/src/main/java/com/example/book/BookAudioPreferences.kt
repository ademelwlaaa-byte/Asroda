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
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getFloat(KEY_MUSIC_VOLUME, 0.85f)
                .coerceIn(MIN_VOLUME, MAX_VOLUME)
        } catch (_: Exception) {
            0.85f
        }
    }

    fun saveMusicVolume(context: Context, volume: Float): Float {
        val clamped = volume.coerceIn(MIN_VOLUME, MAX_VOLUME)
        try {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putFloat(KEY_MUSIC_VOLUME, clamped)
                .apply()
        } catch (_: Exception) {}
        return clamped
    }
}
