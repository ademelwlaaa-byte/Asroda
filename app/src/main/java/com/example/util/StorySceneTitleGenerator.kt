package com.example.util

/**
 * Generates a concise 3-5 word Turkish scene title from a story message
 * so 1-tap Story Bookmarks get meaningful names automatically.
 */
object StorySceneTitleGenerator {

    private val stripMarkupRegex = Regex("[*_~`\"“”«»]+")
    private val whitespaceRegex = Regex("\\s+")

    fun suggestTitle(messageText: String, fallbackSceneNumber: Int = 1): String {
        val cleaned = stripMarkupRegex.replace(messageText, " ")
            .replace(whitespaceRegex, " ")
            .trim()
        if (cleaned.isBlank()) return "Sahne #$fallbackSceneNumber"
        val words = cleaned.split(' ').filter { it.length >= 2 }.take(5)
        if (words.isEmpty()) return "Sahne #$fallbackSceneNumber"
        val raw = words.joinToString(" ").trimEnd('.', ',', ';', ':')
        return if (raw.length > 36) raw.take(36).trimEnd() + "…" else raw
    }
}
