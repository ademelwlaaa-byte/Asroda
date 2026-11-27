package com.example.util

import android.content.Context

/**
 * Per-bot private Story Scratchpad (Not Defteri) allowing players to keep clues,
 * side-character names, inventory, or plot plans alongside any long roleplay story.
 */
object BotScratchpadStore {

    private const val PREFS_NAME = "asroda_bot_scratchpad_v1"
    private const val KEY_PREFIX = "note_"
    private const val KEY_TS_PREFIX = "note_ts_"
    internal const val MAX_NOTE_CHARS = 24_000
    internal const val MAX_STORED_NOTES = 120
    private val storeLock = Any()

    private fun prefs(context: Context) =
        (context.applicationContext ?: context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadNote(context: Context, botId: String): String {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return ""
        return try {
            val raw = prefs(context)
                .getString("$KEY_PREFIX$cleanId", "")
                .orEmpty()
            if (raw.length > MAX_NOTE_CHARS) raw.take(MAX_NOTE_CHARS) else raw
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
        synchronized(storeLock) {
            try {
                val p = prefs(context)
                val bounded = if (text.length > MAX_NOTE_CHARS) text.take(MAX_NOTE_CHARS) else text
                val editor = p.edit()
                    .putString("$KEY_PREFIX$cleanId", bounded)
                    .putLong("$KEY_TS_PREFIX$cleanId", System.currentTimeMillis())

                val allEntries = p.all
                val otherIds = allEntries.keys
                    .filter { it.startsWith(KEY_PREFIX) && !it.startsWith(KEY_TS_PREFIX) }
                    .map { it.removePrefix(KEY_PREFIX) }
                    .filter { it.isNotBlank() && it != cleanId }
                if (otherIds.size >= MAX_STORED_NOTES) {
                    val overflow = otherIds.size - MAX_STORED_NOTES + 1
                    otherIds
                        .map { id -> id to ((allEntries["$KEY_TS_PREFIX$id"] as? Number)?.toLong() ?: 0L) }
                        .sortedWith(compareBy<Pair<String, Long>> { it.second }.thenBy { it.first })
                        .take(overflow)
                        .forEach { (oldId, _) ->
                            editor.remove("$KEY_PREFIX$oldId")
                            editor.remove("$KEY_TS_PREFIX$oldId")
                        }
                }
                editor.apply()
            } catch (_: Exception) {}
        }
    }

    fun clearNote(context: Context, botId: String) {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return
        synchronized(storeLock) {
            try {
                prefs(context)
                    .edit()
                    .remove("$KEY_PREFIX$cleanId")
                    .remove("$KEY_TS_PREFIX$cleanId")
                    .apply()
            } catch (_: Exception) {}
        }
    }

    fun hasNote(context: Context, botId: String): Boolean = loadNote(context, botId).isNotBlank()
}
