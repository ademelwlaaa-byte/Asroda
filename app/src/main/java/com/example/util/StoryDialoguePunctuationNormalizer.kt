package com.example.util

/**
 * Normalizes messy LLM punctuation glitches (extra spaces before punctuation, 4+ dot runs,
 * excessive repeated exclamation/question marks) while preserving expressive "?!" / "!?" pairs.
 */
object StoryDialoguePunctuationNormalizer {

    private val spaceBeforePunctRegex = Regex("\\s+([,;:.!?])")
    private val fourPlusDotsRegex = Regex("\\.{4,}")
    private val excessBangRegex = Regex("!{3,}")
    private val excessQuestionRegex = Regex("\\?{3,}")

    fun normalize(text: String): String {
        if (text.isBlank()) return text
        var out = spaceBeforePunctRegex.replace(text, "$1")
        out = fourPlusDotsRegex.replace(out, "...")
        out = excessBangRegex.replace(out, "!")
        out = excessQuestionRegex.replace(out, "?")
        return out
    }
}
