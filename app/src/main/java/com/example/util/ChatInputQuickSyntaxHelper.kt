package com.example.util

/**
 * Wraps or appends roleplay formatting (*action*, "dialogue", [OOC: ...]) cleanly
 * without producing duplicate delimiters.
 */
object ChatInputQuickSyntaxHelper {

    fun wrapAction(input: String): String {
        val t = input.trim()
        if (t.isEmpty()) return "**"
        if (t.startsWith("*") && t.endsWith("*") && t.length >= 2) return t
        return "*$t*"
    }

    fun wrapDialogue(input: String): String {
        val t = input.trim()
        if (t.isEmpty()) return "\"\""
        if (t.startsWith("\"") && t.endsWith("\"") && t.length >= 2) return t
        return "\"$t\""
    }

    fun wrapOoc(input: String): String {
        val t = input.trim()
        if (t.isEmpty()) return "[OOC: ]"
        if (t.startsWith("[OOC:", ignoreCase = true) && t.endsWith("]")) return t
        return "[OOC: $t]"
    }
}
