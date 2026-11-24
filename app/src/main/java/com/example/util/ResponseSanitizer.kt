package com.example.util

/**
 * Cleans common artifacts emitted by open-source / custom OpenAI-compatible models
 * (stray `Assistant:` / `Asistan:` prefixes, leaked `<think>...</think>` blocks,
 * excessive blank-line runs) before displaying or persisting story replies.
 */
object ResponseSanitizer {

    private val thinkBlockRegex = Regex("(?is)<think>.*?</think>")
    private val unclosedThinkRegex = Regex("(?is)<think>.*$")
    private val leadingRolePrefixRegex = Regex("^(?:assistant|asistan|model|yanıt|cevap)\\s*:\\s*", RegexOption.IGNORE_CASE)
    private val multiBlankLinesRegex = Regex("\\n{3,}")

    fun sanitizeForDisplay(raw: String): String {
        if (raw.isBlank()) return ""
        var text = raw.replace("\r\n", "\n")
        text = thinkBlockRegex.replace(text, "")
        // Only strip an unclosed <think> if there is visible content before it; otherwise preserve cleaned inner text
        if (text.contains("<think>", ignoreCase = true)) {
            val beforeThink = text.substringBefore("<think>").trim()
            text = if (beforeThink.isNotEmpty()) {
                unclosedThinkRegex.replace(text, "")
            } else {
                text.replace(Regex("(?i)</?think>"), "")
            }
        }
        text = text.trim()
        text = leadingRolePrefixRegex.replaceFirst(text, "")
        text = multiBlankLinesRegex.replace(text, "\n\n")
        text = NarrativeTextFormatter.formatWallOfText(text)
        return text.trim()
    }
}
