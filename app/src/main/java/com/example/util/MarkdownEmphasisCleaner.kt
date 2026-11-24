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
        var out = doubleQuoteGlitchRegex.replace(text, "\"$1\"")
        out = paddedItalicRegex.replace(out, "*$1*")
        // Strip a dangling unclosed single asterisk at the very end of a paragraph
        out = out.lines().joinToString("\n") { line ->
            val trimmed = line.trimEnd()
            val singleStars = trimmed.count { it == '*' }
            if (singleStars % 2 == 1 && trimmed.endsWith("*") && !trimmed.endsWith("**")) {
                trimmed.dropLast(1).trimEnd()
            } else {
                line
            }
        }
        return out
    }
}
