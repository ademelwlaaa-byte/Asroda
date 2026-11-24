package com.example.util

import android.content.Context

/**
 * Per-bot private Story Scratchpad (Not Defteri) allowing players to keep clues,
 * side-character names, inventory, or plot plans alongside any long roleplay story.
 */
object BotScratchpadStore {

    private const val PREFS_NAME = "asroda_bot_scratchpad_v1"
    private const val KEY_PREFIX = "note_"
    internal const val MAX_NOTE_CHARS = 24_000

    fun loadNote(context: Context, botId: String): String {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return ""
        return try {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString("$KEY_PREFIX$cleanId", "")
                .orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    fun saveNote(context: Context, botId: String, text: String) {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return
        if (text.isBlank()) {
            clearNote(context, cleanId)
            return
        }
        try {
            val bounded = if (text.length > MAX_NOTE_CHARS) text.take(MAX_NOTE_CHARS) else text
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("$KEY_PREFIX$cleanId", bounded)
                .apply()
        } catch (_: Exception) {}
    }

    fun clearNote(context: Context, botId: String) {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return
        try {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove("$KEY_PREFIX$cleanId")
                .apply()
        } catch (_: Exception) {}
    }
}
