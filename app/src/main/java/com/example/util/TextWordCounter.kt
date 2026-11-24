package com.example.util

/**
 * Lightweight word, character, and paragraph counter for roleplay drafts and story notes.
 */
object TextWordCounter {

    private val whitespaceRegex = Regex("\\s+")
    private val paragraphSplitRegex = Regex("\\n\\s*\\n")

    fun countWords(text: String): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0
        return trimmed.split(whitespaceRegex).count { it.isNotBlank() }
    }

    fun countParagraphs(text: String): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0
        return trimmed.split(paragraphSplitRegex).count { it.isNotBlank() }
    }

    fun formatCompact(text: String): String {
        val words = countWords(text)
        val chars = text.trim().length
        return "$words kelime • $chars karakter"
    }
}
