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
    private val abbreviationTailRegex = Regex("""(?i)(?:^|\s)(?:dr|prof|mr|mrs|ms|st|av|alb|yzb|doç|yrd|vb|vs)\.$""")

    fun formatWallOfText(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.length < WALL_OF_TEXT_THRESHOLD) return trimmed
        if (trimmed.contains("\n") || trimmed.contains("```")) return trimmed

        val rawParts = trimmed.split(sentenceBoundaryRegex).map { it.trim() }.filter { it.isNotEmpty() }
        if (rawParts.size < 3) return trimmed

        // Re-join accidental splits right after honorific/abbreviations ("Dr. Ahmet", "Prof. Yılmaz")
        val parts = mutableListOf<String>()
        for (p in rawParts) {
            if (parts.isNotEmpty() && abbreviationTailRegex.containsMatchIn(parts.last())) {
                parts[parts.lastIndex] = parts.last() + " " + p
            } else {
                parts.add(p)
            }
        }
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
