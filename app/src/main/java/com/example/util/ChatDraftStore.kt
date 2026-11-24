package com.example.util

import android.content.Context

/**
 * Lightweight, crash-safe per-bot chat draft persistence.
 *
 * Preserves unsent roleplay replies across bot switches, screen rotations, and OS process death,
 * while capping stored drafts at [MAX_STORED_DRAFTS] (LRU eviction) so SharedPreferences stays tiny.
 */
object ChatDraftStore {

    private const val PREFS_NAME = "asroda_chat_drafts_v1"
    private const val KEY_TEXT_PREFIX = "draft_text_"
    private const val KEY_TS_PREFIX = "draft_ts_"
    internal const val MAX_DRAFT_CHARS = 16_000
    internal const val MAX_STORED_DRAFTS = 60

    fun loadDraft(context: Context, botId: String): String {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return ""
        return try {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString("$KEY_TEXT_PREFIX$cleanId", "")
                .orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    fun saveDraft(context: Context, botId: String, rawText: String) {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return
        if (rawText.isBlank()) {
            clearDraft(context, cleanId)
            return
        }
        try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val bounded = if (rawText.length > MAX_DRAFT_CHARS) rawText.take(MAX_DRAFT_CHARS) else rawText
            val editor = prefs.edit()
                .putString("$KEY_TEXT_PREFIX$cleanId", bounded)
                .putLong("$KEY_TS_PREFIX$cleanId", System.currentTimeMillis())

            // Enforce LRU cap so old deleted/abandoned bots never bloat preferences
            val allTs = prefs.all.entries
                .filter { it.key.startsWith(KEY_TS_PREFIX) }
                .mapNotNull { entry ->
                    val id = entry.key.removePrefix(KEY_TS_PREFIX)
                    val ts = (entry.value as? Number)?.toLong() ?: 0L
                    if (id == cleanId) null else id to ts
                }
            if (allTs.size >= MAX_STORED_DRAFTS) {
                val overflow = allTs.size - MAX_STORED_DRAFTS + 1
                allTs.sortedBy { it.second }.take(overflow).forEach { (oldId, _) ->
                    editor.remove("$KEY_TEXT_PREFIX$oldId")
                    editor.remove("$KEY_TS_PREFIX$oldId")
                }
            }
            editor.apply()
        } catch (_: Exception) {
        }
    }

    fun clearDraft(context: Context, botId: String) {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return
        try {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove("$KEY_TEXT_PREFIX$cleanId")
                .remove("$KEY_TS_PREFIX$cleanId")
                .apply()
        } catch (_: Exception) {
        }
    }

    fun hasDraft(context: Context, botId: String): Boolean = loadDraft(context, botId).isNotBlank()
}
