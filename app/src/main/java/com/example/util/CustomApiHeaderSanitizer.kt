package com.example.util

/**
 * Sanitizes custom HTTP header names and values to prevent CR/LF injection or malformed requests.
 */
object CustomApiHeaderSanitizer {

    private val invalidNameChars = Regex("[^A-Za-z0-9\\-_]")
    private val lineBreakChars = Regex("[\\r\\n]+")

    fun sanitizeHeaderName(raw: String): String {
        return raw.trim().replace(invalidNameChars, "")
    }

    fun sanitizeHeaderValue(raw: String): String {
        return raw.replace(lineBreakChars, " ").trim()
    }

    fun buildSafeHeaderMap(rawHeaders: Map<String, String>): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        for ((k, v) in rawHeaders) {
            val cleanKey = sanitizeHeaderName(k)
            val cleanVal = sanitizeHeaderValue(v)
            if (cleanKey.isNotEmpty() && cleanVal.isNotEmpty()) {
                result[cleanKey] = cleanVal
            }
        }
        return result
    }
}
