package com.example.util

/**
 * Cleans common artifacts emitted by open-source / custom OpenAI-compatible models
 * (stray `Assistant:` / `Asistan:` prefixes, leaked `<think>...</think>` blocks,
 * excessive blank-line runs) before displaying or persisting story replies.
 */
object ResponseSanitizer {

    private val reasoningBlockRegex = Regex("(?is)<(?:think|reasoning|thought|scratchpad|internal)>.*?</(?:think|reasoning|thought|scratchpad|internal)>")
    private val unclosedReasoningRegex = Regex("(?is)<(?:think|reasoning|thought|scratchpad|internal)>.*$")
    private val strayInternalTagsRegex = Regex("(?is)(?:\\[\\[STATE_JSON[\\s\\S]*?\\]\\]|\\[\\[MEMORY_SAVE[\\s\\S]*?\\]\\]|\\[ACTIVE_MEMORY_CALL:[^\\n\\]]*\\]|\\[CAST_MEMBER:[^\\n\\]]*\\])")
    private val leadingRolePrefixRegex = Regex("^(?:assistant|asistan|model|ai|bot|yanıt|cevap)\\s*:\\s*", RegexOption.IGNORE_CASE)
    private val multiBlankLinesRegex = Regex("\\n{3,}")

    fun sanitizeForDisplay(raw: String): String {
        if (raw.isBlank()) return ""
        var text = raw.replace("\r\n", "\n")
        text = reasoningBlockRegex.replace(text, "")
        // Only strip an unclosed reasoning tag if there is visible content before it; otherwise preserve cleaned inner text
        val unclosedMatch = unclosedReasoningRegex.find(text)
        if (unclosedMatch != null) {
            val beforeTag = text.substring(0, unclosedMatch.range.first).trim()
            text = if (beforeTag.isNotEmpty()) {
                beforeTag
            } else {
                text.replace(Regex("(?i)</?(?:think|reasoning|thought|scratchpad|internal)>"), "")
            }
        }
        text = strayInternalTagsRegex.replace(text, "")
        text = text.trim()
        text = leadingRolePrefixRegex.replaceFirst(text, "")
        text = multiBlankLinesRegex.replace(text, "\n\n")
        text = MarkdownEmphasisCleaner.clean(text)
        text = StoryDialoguePunctuationNormalizer.normalize(text)
        text = NarrativeTextFormatter.formatWallOfText(text)
        return text.trim()
    }
}
