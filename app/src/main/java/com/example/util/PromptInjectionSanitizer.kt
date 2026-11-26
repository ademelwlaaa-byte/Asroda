package com.example.util

/**
 * Sanitizes user-authored Director Rules and story notes before system-prompt injection
 * so accidental code fences, control characters, internal state protocol tokens, or
 * raw chat-template boundary markers never corrupt prompts or state parsing.
 */
object PromptInjectionSanitizer {

    private val controlCharsRegex = Regex("[\\p{Cc}&&[^\\n\\t]]")
    private val tripleBacktickRegex = Regex("`{3,}")
    private val multiBlankLinesRegex = Regex("\\n{3,}")
    private val internalProtocolTokenRegex = Regex(
        """\[\[\s*(?:STATE_JSON|MEMORY_SAVE|STATE|CHARACTER_EMOTION|CAST_MEMBER|WORLD_ATMOSPHERE)\b[^]]*\]?\]?""",
        RegexOption.IGNORE_CASE
    )
    private val templateBoundaryRegex = Regex(
        """(?i)(?:</?(?:system|assistant|user|think|thought|scratchpad|internal)\s*>|<\|(?:im_start|im_end|endoftext|system|assistant|user)\|>|\[/?INST\]|<<\s*/?SYS\s*>>)"""
    )

    fun sanitizeDirective(raw: String, maxLength: Int = 600): String {
        if (raw.isBlank()) return ""
        val cleaned = raw
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(controlCharsRegex, "")
            .replace(tripleBacktickRegex, "'")
            .replace(internalProtocolTokenRegex, "")
            .replace(templateBoundaryRegex, "")
            .replace(multiBlankLinesRegex, "\n\n")
            .trim()
        return if (cleaned.length > maxLength) cleaned.take(maxLength).trim() else cleaned
    }
}
