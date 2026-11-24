package com.example.util

/**
 * Extracts explicit speaker attributions (e.g., "Natasha: ..." or "**Wanda:** ...")
 * from multi-character Universe story messages.
 */
object StoryDialogueSpeakerExtractor {

    private val speakerPrefixRegex = Regex("^\\*{0,2}([\\p{L}][\\p{L}\\s'.-]{1,28})\\*{0,2}\\s*:\\s*")

    fun extractSpeakers(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val found = LinkedHashSet<String>()
        for (line in text.lines()) {
            val trimmed = line.trim()
            val match = speakerPrefixRegex.find(trimmed) ?: continue
            val candidate = match.groupValues[1].trim()
            if (candidate.length in 2..28 && !candidate.equals("not", ignoreCase = true) && !candidate.equals("ooc", ignoreCase = true)) {
                found.add(candidate)
            }
        }
        return found.toList()
    }
}
