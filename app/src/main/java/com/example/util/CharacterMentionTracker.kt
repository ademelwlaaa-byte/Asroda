package com.example.util

import com.example.data.local.MessageEntity

/**
 * Counts how often named cast/key characters appear across an interactive story conversation.
 */
object CharacterMentionTracker {

    private val jsonNameFieldRegex = Regex("\"name\"\\s*:\\s*\"([^\"]{2,60})\"")
    private val plainStringItemRegex = Regex("\"([^\"{}\\[\\]:,]{2,60})\"")

    data class CharacterMention(
        val name: String,
        val count: Int
    )

    fun extractCastNames(keyCharactersJson: String): List<String> {
        val trimmed = keyCharactersJson.trim()
        if (trimmed.isBlank() || trimmed == "[]") return emptyList()
        val fromObjects = jsonNameFieldRegex.findAll(trimmed)
            .map { it.groupValues[1].trim() }
            .filter { it.length >= 2 }
            .toList()
        if (fromObjects.isNotEmpty()) return fromObjects.distinct()
        return plainStringItemRegex.findAll(trimmed)
            .map { it.groupValues[1].trim() }
            .filter { it.length >= 2 }
            .distinct()
            .toList()
    }

    fun countMentions(
        messages: List<MessageEntity>,
        castNames: List<String>,
        maxResults: Int = 5
    ): List<CharacterMention> {
        if (castNames.isEmpty() || messages.isEmpty()) return emptyList()
        val combinedFolded = messages
            .filter { it.role == "user" || it.role == "assistant" }
            .joinToString("\n") { ChatSearchHelper.foldTurkish(it.text) }
        if (combinedFolded.isBlank()) return emptyList()

        return castNames.mapNotNull { originalName ->
            val needle = ChatSearchHelper.foldTurkish(originalName.trim())
            if (needle.length < 2) return@mapNotNull null
            var idx = combinedFolded.indexOf(needle)
            var count = 0
            while (idx >= 0) {
                count++
                idx = combinedFolded.indexOf(needle, idx + needle.length)
            }
            if (count > 0) CharacterMention(originalName, count) else null
        }.sortedWith(compareByDescending<CharacterMention> { it.count }.thenBy { it.name })
            .take(maxResults.coerceIn(1, 10))
    }
}
