package com.example.util

/**
 * Strips or preserves italic roleplay action blocks (*...*) cleanly for TTS preparation
 * without leaving orphaned punctuation or collapsed words.
 */
object TtsActionStripper {

    private val actionBlockRegex = Regex("\\*[^*\n]+\\*")
    private val multiSpaceRegex = Regex("\\s{2,}")

    fun prepare(text: String, includeActions: Boolean): String {
        if (text.isBlank()) return ""
        val processed = if (includeActions) {
            text.replace("*", "")
        } else {
            actionBlockRegex.replace(text, " ")
        }
        return processed
            .replace(multiSpaceRegex, " ")
            .replace(Regex("\\s+([,.!?])"), "$1")
            .trim()
    }
}
