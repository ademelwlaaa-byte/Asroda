package com.example.util

/**
 * Formats a story message into a clean Turkish Markdown blockquote snippet for sharing or notes.
 */
object MessageQuoteSnippetHelper {

    fun formatBlockquote(text: String, speakerName: String = "", maxChars: Int = 240): String {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.isEmpty()) return ""
        val clipped = if (clean.length > maxChars) clean.take(maxChars).trimEnd() + "…" else clean
        return if (speakerName.isNotBlank()) {
            "> $clipped\n— *${speakerName.trim()}*"
        } else {
            "> $clipped"
        }
    }
}
