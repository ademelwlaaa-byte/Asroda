package com.example.util

import java.util.Locale

/**
 * Sanitizes custom HTTP header names and values to prevent CR/LF injection,
 * OkHttp illegal control/non-ASCII crashes, duplicate header keys, or hop-by-hop overrides.
 */
object CustomApiHeaderSanitizer {

    private val invalidNameChars = Regex("[^A-Za-z0-9\\-_]")
    private val lineBreakChars = Regex("[\\r\\n]+")
    private val forbiddenHeaderNames = setOf(
        "host",
        "content-length",
        "transfer-encoding",
        "connection",
        "keep-alive",
        "upgrade",
        "te",
        "trailer",
        "proxy-connection"
    )

    fun sanitizeHeaderName(raw: String): String {
        return raw.trim().replace(invalidNameChars, "").take(128)
    }

    fun sanitizeHeaderValue(raw: String): String {
        val flattened = raw.replace(lineBreakChars, " ")
        val sb = StringBuilder(flattened.length)
        for (ch in flattened) {
            val code = ch.code
            // RFC 7230 / OkHttp strictly allows printable ASCII (0x20..0x7E) and HTAB (0x09)
            if (code == 0x09 || code in 0x20..0x7E) {
                sb.append(ch)
            }
        }
        return sb.toString().trim().take(2048)
    }

    fun buildSafeHeaderMap(rawHeaders: Map<String, String>): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        val seenLower = HashMap<String, String>()
        for ((k, v) in rawHeaders) {
            val cleanKey = sanitizeHeaderName(k)
            val cleanVal = sanitizeHeaderValue(v)
            if (cleanKey.isEmpty() || cleanVal.isEmpty()) continue
            val lowerKey = cleanKey.lowercase(Locale.ROOT)
            if (lowerKey in forbiddenHeaderNames) continue
            val existingKey = seenLower[lowerKey]
            if (existingKey != null && existingKey != cleanKey) {
                result.remove(existingKey)
            }
            seenLower[lowerKey] = cleanKey
            result[cleanKey] = cleanVal
        }
        return result
    }
}
