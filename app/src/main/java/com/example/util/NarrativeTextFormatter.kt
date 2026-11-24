package com.example.util

/**
 * Breaks dense single-block "wall of text" LLM outputs (>380 chars without paragraph breaks)
 * into readable literary paragraphs at natural sentence/dialogue boundaries.
 * Never alters already-formatted multi-paragraph replies or code blocks.
 */
object NarrativeTextFormatter {

    private const val WALL_OF_TEXT_THRESHOLD = 380
    private const val TARGET_PARAGRAPH_CHARS = 240
    private val sentenceBoundaryRegex = Regex("(?<=[.!?…\"”»])\\s+(?=[A-ZÇĞİÖŞÜ\"“«*])")

    fun formatWallOfText(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.length < WALL_OF_TEXT_THRESHOLD) return trimmed
        if (trimmed.contains("\n") || trimmed.contains("```")) return trimmed

        val parts = trimmed.split(sentenceBoundaryRegex).map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.size < 3) return trimmed

        val paragraphs = mutableListOf<String>()
        val current = StringBuilder()
        for (part in parts) {
            if (current.isNotEmpty() && current.length + part.length > TARGET_PARAGRAPH_CHARS) {
                paragraphs.add(current.toString().trim())
                current.clear()
            }
            if (current.isNotEmpty()) current.append(' ')
            current.append(part)
        }
        if (current.isNotEmpty()) {
            paragraphs.add(current.toString().trim())
        }
        return paragraphs.joinToString("\n\n")
    }
}
