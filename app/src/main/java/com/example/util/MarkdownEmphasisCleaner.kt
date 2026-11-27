package com.example.util

/**
 * Cleans malformed Markdown emphasis and duplicate quotation glitches emitted by LLMs
 * so narrative prose and TTS input remain clean and literary.
 */
object MarkdownEmphasisCleaner {

    private val doubleQuoteGlitchRegex = Regex("\"\"([^\"]+)\"\"")
    private val paddedItalicRegex = Regex("\\*\\s+([^*\n]{2,120}?)\\s+\\*")

    fun clean(text: String): String {
        if (text.isBlank()) return text
        var inCodeFence = false
        return text.lines().joinToString("\n") { line ->
            val trimmedStart = line.trimStart()
            if (trimmedStart.startsWith("```")) {
                inCodeFence = !inCodeFence
                return@joinToString line
            }
            if (inCodeFence) return@joinToString line

            var cleaned = doubleQuoteGlitchRegex.replace(line, "\"$1\"")
            cleaned = paddedItalicRegex.replace(cleaned, "*$1*")
            val trimmedEnd = cleaned.trimEnd()
            val singleStars = trimmedEnd.count { it == '*' }
            if (singleStars % 2 == 1 && trimmedEnd.endsWith("*") && !trimmedEnd.endsWith("**")) {
                trimmedEnd.dropLast(1).trimEnd()
            } else {
                cleaned
            }
        }
    }
}
