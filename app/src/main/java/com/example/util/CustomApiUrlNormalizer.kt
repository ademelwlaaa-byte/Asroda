package com.example.util

/**
 * Normalizes mobile-pasted Custom API Base URLs cleanly:
 * - trims whitespace & control chars
 * - prepends `https://` when scheme is missing (except localhost/192.168/10.x which default to `http://`)
 * - strips `/chat/completions`, `/completions`, or `/models` suffixes
 * - collapses duplicate slashes after scheme
 */
object CustomApiUrlNormalizer {

    private val PRIVATE_172_REGEX = Regex("""^172\.(1[6-9]|2[0-9]|3[0-1])\.""")
    private val INVISIBLE_CHARS_REGEX = Regex("""[\u200B-\u200D\uFEFF\u00AD\s]+""")

    fun normalizeBaseUrl(raw: String): String {
        var cleaned = raw.trim().replace(INVISIBLE_CHARS_REGEX, "")
        if (cleaned.isBlank()) return ""

        // Strip accidental query strings or fragments (e.g., pasted ?key=AIza... or #section)
        val queryIdx = cleaned.indexOf('?')
        if (queryIdx >= 0) cleaned = cleaned.substring(0, queryIdx)
        val hashIdx = cleaned.indexOf('#')
        if (hashIdx >= 0) cleaned = cleaned.substring(0, hashIdx)
        if (cleaned.isBlank()) return ""

        if (!cleaned.startsWith("http://", ignoreCase = true) && !cleaned.startsWith("https://", ignoreCase = true)) {
            val hostOnly = cleaned.substringBefore('/').substringBefore(':')
            val isLocal = cleaned.startsWith("localhost", ignoreCase = true) ||
                cleaned.startsWith("127.") ||
                cleaned.startsWith("192.168.") ||
                cleaned.startsWith("10.") ||
                cleaned.startsWith("[::1]") ||
                PRIVATE_172_REGEX.containsMatchIn(cleaned) ||
                hostOnly.endsWith(".local", ignoreCase = true)
            cleaned = (if (isLocal) "http://" else "https://") + cleaned
        }

        val schemeEnd = cleaned.indexOf("://") + 3
        val scheme = cleaned.substring(0, schemeEnd).lowercase(java.util.Locale.ROOT)
        val rest = cleaned.substring(schemeEnd).replace(Regex("/{2,}"), "/")
        cleaned = (scheme + rest).trimEnd('/')

        val suffixes = listOf(
            "/chat/completions",
            "/completions",
            "/v1/messages",
            "/messages",
            "/models"
        )
        for (suf in suffixes) {
            if (cleaned.endsWith(suf, ignoreCase = true)) {
                cleaned = cleaned.dropLast(suf.length).trimEnd('/')
            }
        }
        return cleaned
    }
}
