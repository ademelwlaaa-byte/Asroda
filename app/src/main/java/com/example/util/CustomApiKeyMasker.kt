package com.example.util

/**
 * Masks API keys for safe display in UI previews and diagnostic logs without ever leaking secrets.
 */
object CustomApiKeyMasker {

    fun mask(rawKey: String): String {
        val clean = ApiKeyFormatValidator.sanitizeKey(rawKey)
        if (clean.isEmpty()) return ""
        if (clean.length <= 10) return "••••"
        return "${clean.take(4)}••••${clean.takeLast(4)}"
    }
}
