package com.example.util

/**
 * Checks and repairs unbalanced quotation marks or single-asterisk action delimiters
 * in roleplay prose.
 */
object StoryDialogueFormatValidator {

    fun hasBalancedQuotes(text: String): Boolean = text.count { it == '"' } % 2 == 0

    fun hasBalancedAsterisks(text: String): Boolean = text.count { it == '*' } % 2 == 0

    fun autoCloseUnbalanced(text: String): String {
        var out = text.trimEnd()
        if (out.isEmpty()) return out
        if (!hasBalancedQuotes(out)) out += "\""
        if (!hasBalancedAsterisks(out)) out += "*"
        return out
    }
}
