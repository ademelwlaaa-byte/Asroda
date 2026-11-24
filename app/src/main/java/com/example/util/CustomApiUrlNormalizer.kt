package com.example.util

/**
 * Normalizes mobile-pasted Custom API Base URLs cleanly:
 * - trims whitespace & control chars
 * - prepends `https://` when scheme is missing (except localhost/192.168/10.x which default to `http://`)
 * - strips `/chat/completions`, `/completions`, or `/models` suffixes
 * - collapses duplicate slashes after scheme
 */
object CustomApiUrlNormalizer {

    fun normalizeBaseUrl(raw: String): String {
        var cleaned = raw.trim().replace(Regex("\\s+"), "")
        if (cleaned.isBlank()) return ""

        if (!cleaned.startsWith("http://", ignoreCase = true) && !cleaned.startsWith("https://", ignoreCase = true)) {
            val isLocal = cleaned.startsWith("localhost", ignoreCase = true) ||
                cleaned.startsWith("127.") ||
                cleaned.startsWith("192.168.") ||
                cleaned.startsWith("10.")
            cleaned = (if (isLocal) "http://" else "https://") + cleaned
        }

        val schemeEnd = cleaned.indexOf("://") + 3
        val scheme = cleaned.substring(0, schemeEnd)
        val rest = cleaned.substring(schemeEnd).replace(Regex("/{2,}"), "/")
        cleaned = (scheme + rest).trimEnd('/')

        val suffixes = listOf("/chat/completions", "/completions", "/models")
        for (suf in suffixes) {
            if (cleaned.endsWith(suf, ignoreCase = true)) {
                cleaned = cleaned.dropLast(suf.length).trimEnd('/')
            }
        }
        return cleaned
    }
}
