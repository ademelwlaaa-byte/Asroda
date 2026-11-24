package com.example.util

/**
 * Exports a bot's saved Story Bookmarks as a clean, shareable Turkish Markdown timeline.
 */
object StoryBookmarkExporter {

    fun formatMarkdown(
        botDisplayName: String,
        bookmarks: List<StoryBookmarkStore.Bookmark>,
        nowMs: Long = System.currentTimeMillis()
    ): String {
        if (bookmarks.isEmpty()) return ""
        return buildString {
            append("# ").append(botDisplayName.ifBlank { "Asroda" }).append(" — Kayıtlı Sahne Yer İmleri\n\n")
            bookmarks.forEachIndexed { idx, bm ->
                val rel = MessageTimestampFormatter.formatRelative(bm.createdAtMs, nowMs)
                append(idx + 1).append(". **").append(bm.title.trim()).append("**")
                if (rel.isNotBlank()) append(" _(").append(rel).append(")_")
                append("\n")
                val snip = bm.snippet.replace(Regex("\\s+"), " ").trim()
                if (snip.isNotBlank()) {
                    append("   > ").append(snip.take(180))
                    if (snip.length > 180) append("…")
                    append("\n")
                }
            }
        }.trim()
    }
}
