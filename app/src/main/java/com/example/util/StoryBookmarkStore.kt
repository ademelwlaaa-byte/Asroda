package com.example.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Per-bot Story Bookmark store allowing players to save named story milestones
 * (linked to a specific messageId) and jump back to them anytime in long roleplay sessions.
 */
object StoryBookmarkStore {

    private const val PREFS_NAME = "asroda_story_bookmarks_v1"
    private const val KEY_PREFIX = "bookmarks_"
    internal const val MAX_BOOKMARKS_PER_BOT = 40

    data class Bookmark(
        val id: String,
        val messageId: String,
        val title: String,
        val snippet: String,
        val createdAtMs: Long
    )

    fun loadBookmarks(context: Context, botId: String): List<Bookmark> {
        val cleanId = botId.trim()
        if (cleanId.isBlank()) return emptyList()
        return try {
            val raw = context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString("$KEY_PREFIX$cleanId", null)
                .orEmpty()
            if (raw.isBlank()) return emptyList()
            val arr = JSONArray(raw)
            val list = mutableListOf<Bookmark>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id").trim()
                val msgId = obj.optString("messageId").trim()
                val title = obj.optString("title").trim()
                if (id.isBlank() || msgId.isBlank() || title.isBlank()) continue
                list.add(
                    Bookmark(
                        id = id,
                        messageId = msgId,
                        title = title,
                        snippet = obj.optString("snippet").trim(),
                        createdAtMs = obj.optLong("createdAtMs", 0L)
                    )
                )
            }
            list.sortedByDescending { it.createdAtMs }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addBookmark(
        context: Context,
        botId: String,
        messageId: String,
        title: String,
        snippet: String
    ): List<Bookmark> {
        val cleanBot = botId.trim()
        val cleanMsg = messageId.trim()
        val cleanTitle = title.trim().take(80)
        if (cleanBot.isBlank() || cleanMsg.isBlank() || cleanTitle.isBlank()) {
            return loadBookmarks(context, cleanBot)
        }
        val existing = loadBookmarks(context, cleanBot)
            .filterNot { it.messageId == cleanMsg }
            .toMutableList()
        val cleanSnippet = com.example.data.repository.EmochiRepository.cleanEmotionTags(snippet)
            .trim()
            .replace(Regex("\\s+"), " ")
            .take(120)
        val created = Bookmark(
            id = "${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(8)}",
            messageId = cleanMsg,
            title = cleanTitle,
            snippet = cleanSnippet,
            createdAtMs = System.currentTimeMillis()
        )
        existing.add(0, created)
        val bounded = existing.take(MAX_BOOKMARKS_PER_BOT)
        persist(context, cleanBot, bounded)
        return bounded
    }

    fun removeBookmark(context: Context, botId: String, bookmarkId: String): List<Bookmark> {
        val cleanBot = botId.trim()
        if (cleanBot.isBlank()) return emptyList()
        val updated = loadBookmarks(context, cleanBot).filterNot { it.id == bookmarkId }
        persist(context, cleanBot, updated)
        return updated
    }

    fun clearAllForBot(context: Context, botId: String) {
        val cleanBot = botId.trim()
        if (cleanBot.isBlank()) return
        try {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove("$KEY_PREFIX$cleanBot")
                .apply()
        } catch (_: Exception) {}
    }

    private fun persist(context: Context, botId: String, bookmarks: List<Bookmark>) {
        try {
            val arr = JSONArray()
            bookmarks.forEach { b ->
                arr.put(
                    JSONObject().apply {
                        put("id", b.id)
                        put("messageId", b.messageId)
                        put("title", b.title)
                        put("snippet", b.snippet)
                        put("createdAtMs", b.createdAtMs)
                    }
                )
            }
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("$KEY_PREFIX$botId", arr.toString())
                .apply()
        } catch (_: Exception) {}
    }
}
