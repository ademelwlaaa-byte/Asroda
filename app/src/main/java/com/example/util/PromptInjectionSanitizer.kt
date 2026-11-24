package com.example.util

/**
 * Sanitizes user-authored Director Rules and story notes before system-prompt injection
 * so accidental code fences, control characters, or excessive whitespace never corrupt prompts.
 */
object PromptInjectionSanitizer {

    private val controlCharsRegex = Regex("[\\p{Cc}&&[^\\n\\t]]")
    private val tripleBacktickRegex = Regex("`{3,}")
    private val multiBlankLinesRegex = Regex("\\n{3,}")

    fun sanitizeDirective(raw: String, maxLength: Int = 600): String {
        if (raw.isBlank()) return ""
        val cleaned = raw
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(controlCharsRegex, "")
            .replace(tripleBacktickRegex, "'")
            .replace(multiBlankLinesRegex, "\n\n")
            .trim()
        return if (cleaned.length > maxLength) cleaned.take(maxLength).trim() else cleaned
    }
}
