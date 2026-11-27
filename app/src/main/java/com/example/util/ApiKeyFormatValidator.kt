package com.example.util

/**
 * Cleans and validates user-pasted API keys so common copy/paste mistakes (leading "Bearer ",
 * surrounding quotes, embedded newlines/spaces) never cause avoidable 401 errors.
 */
object ApiKeyFormatValidator {

    private val bearerPrefixRegex = Regex("^bearer\\s+", RegexOption.IGNORE_CASE)
    private val invisibleCharsRegex = Regex("[\\u200B-\\u200D\\uFEFF\\u00AD]")

    fun sanitizeKey(rawKey: String): String {
        var cleaned = rawKey
            .replace("\r", "")
            .replace("\n", "")
            .replace("\u00A0", " ")
            .replace(invisibleCharsRegex, "")
            .trim()
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) ||
            (cleaned.startsWith("'") && cleaned.endsWith("'"))
        ) {
            cleaned = cleaned.substring(1, cleaned.length - 1).trim()
        }
        cleaned = cleaned.replace(bearerPrefixRegex, "").trim()
        return cleaned
    }

    fun detectWarning(rawKey: String): String? {
        val cleaned = sanitizeKey(rawKey)
        if (cleaned.isEmpty()) return null
        if (cleaned.startsWith("http://", ignoreCase = true) ||
            cleaned.startsWith("https://", ignoreCase = true)
        ) {
            return "API anahtarı yerine URL yapıştırmış olabilirsiniz."
        }
        if (cleaned.any { it.isWhitespace() }) {
            return "API anahtarı içinde boşluk karakteri tespit edildi."
        }
        if (cleaned.length < 8) {
            return "API anahtarı beklenenden kısa görünüyor."
        }
        return null
    }
}
