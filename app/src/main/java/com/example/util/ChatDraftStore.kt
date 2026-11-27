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
    private val storeLock = Any()

    private fun prefs(context: Context) =
        (context.applicationContext ?: context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadDraft(context: Context, botId: String): String {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return ""
        return try {
            val raw = prefs(context)
                .getString("$KEY_TEXT_PREFIX$cleanId", "")
                .orEmpty()
            if (raw.length > MAX_DRAFT_CHARS) raw.take(MAX_DRAFT_CHARS) else raw
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
        synchronized(storeLock) {
            try {
                val p = prefs(context)
                val bounded = if (rawText.length > MAX_DRAFT_CHARS) rawText.take(MAX_DRAFT_CHARS) else rawText
                val editor = p.edit()
                    .putString("$KEY_TEXT_PREFIX$cleanId", bounded)
                    .putLong("$KEY_TS_PREFIX$cleanId", System.currentTimeMillis())

                // Enforce deterministic LRU cap across all stored draft IDs (including any orphaned text keys)
                val allEntries = p.all
                val otherIds = LinkedHashSet<String>()
                for (key in allEntries.keys) {
                    when {
                        key.startsWith(KEY_TEXT_PREFIX) -> {
                            val id = key.removePrefix(KEY_TEXT_PREFIX)
                            if (id.isNotBlank() && id != cleanId) otherIds.add(id)
                        }
                        key.startsWith(KEY_TS_PREFIX) -> {
                            val id = key.removePrefix(KEY_TS_PREFIX)
                            if (id.isNotBlank() && id != cleanId) otherIds.add(id)
                        }
                    }
                }
                if (otherIds.size >= MAX_STORED_DRAFTS) {
                    val overflow = otherIds.size - MAX_STORED_DRAFTS + 1
                    otherIds
                        .map { id -> id to ((allEntries["$KEY_TS_PREFIX$id"] as? Number)?.toLong() ?: 0L) }
                        .sortedWith(compareBy<Pair<String, Long>> { it.second }.thenBy { it.first })
                        .take(overflow)
                        .forEach { (oldId, _) ->
                            editor.remove("$KEY_TEXT_PREFIX$oldId")
                            editor.remove("$KEY_TS_PREFIX$oldId")
                        }
                }
                editor.apply()
            } catch (_: Exception) {
            }
        }
    }

    fun clearDraft(context: Context, botId: String) {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return
        synchronized(storeLock) {
            try {
                prefs(context)
                    .edit()
                    .remove("$KEY_TEXT_PREFIX$cleanId")
                    .remove("$KEY_TS_PREFIX$cleanId")
                    .apply()
            } catch (_: Exception) {
            }
        }
    }

    fun hasDraft(context: Context, botId: String): Boolean = loadDraft(context, botId).isNotBlank()
}
