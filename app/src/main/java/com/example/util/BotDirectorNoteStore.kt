package com.example.util

import android.content.Context

/**
 * Stores per-bot persistent OOC Director Rules (e.g. "Never speak for my character",
 * "Write in cinematic third-person Turkish") editable directly inside Chat's Director modal.
 */
object BotDirectorNoteStore {

    private const val PREFS_NAME = "asroda_bot_director_notes_v1"
    private const val KEY_PREFIX = "director_rule_"
    private const val KEY_TS_PREFIX = "director_ts_"
    internal const val MAX_RULE_CHARS = 1_500
    internal const val MAX_STORED_RULES = 120
    private val CONTROL_CHARS_REGEX = Regex("""[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]""")
    private val storeLock = Any()

    private fun prefs(context: Context) =
        (context.applicationContext ?: context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadRule(context: Context, botId: String): String {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return ""
        return try {
            val raw = prefs(context)
                .getString("$KEY_PREFIX$cleanId", "")
                .orEmpty()
            if (raw.length > MAX_RULE_CHARS) raw.take(MAX_RULE_CHARS) else raw
        } catch (_: Exception) {
            ""
        }
    }

    fun saveRule(context: Context, botId: String, rule: String) {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return
        val sanitized = rule.replace(CONTROL_CHARS_REGEX, "").trim()
        if (sanitized.isBlank()) {
            clearRule(context, cleanId)
            return
        }
        synchronized(storeLock) {
            try {
                val p = prefs(context)
                val bounded = sanitized.take(MAX_RULE_CHARS)
                val editor = p.edit()
                    .putString("$KEY_PREFIX$cleanId", bounded)
                    .putLong("$KEY_TS_PREFIX$cleanId", System.currentTimeMillis())

                val allEntries = p.all
                val otherIds = allEntries.keys
                    .filter { it.startsWith(KEY_PREFIX) }
                    .map { it.removePrefix(KEY_PREFIX) }
                    .filter { it.isNotBlank() && it != cleanId }
                if (otherIds.size >= MAX_STORED_RULES) {
                    val overflow = otherIds.size - MAX_STORED_RULES + 1
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

    fun clearRule(context: Context, botId: String) {
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

    fun hasRule(context: Context, botId: String): Boolean = loadRule(context, botId).isNotBlank()
}
