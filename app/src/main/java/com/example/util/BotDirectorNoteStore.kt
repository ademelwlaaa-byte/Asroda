package com.example.util

import android.content.Context

/**
 * Stores per-bot persistent OOC Director Rules (e.g. "Never speak for my character",
 * "Write in cinematic third-person Turkish") editable directly inside Chat's Director modal.
 */
object BotDirectorNoteStore {

    private const val PREFS_NAME = "asroda_bot_director_notes_v1"
    private const val KEY_PREFIX = "director_rule_"
    internal const val MAX_RULE_CHARS = 1_500

    fun loadRule(context: Context, botId: String): String {
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

    fun saveRule(context: Context, botId: String, rule: String) {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return
        if (rule.isBlank()) {
            clearRule(context, cleanId)
            return
        }
        try {
            val bounded = rule.trim().take(MAX_RULE_CHARS)
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("$KEY_PREFIX$cleanId", bounded)
                .apply()
        } catch (_: Exception) {}
    }

    fun clearRule(context: Context, botId: String) {
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
