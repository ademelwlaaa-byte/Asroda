package com.example.util

import java.net.URI

/**
 * Validates Custom API endpoint URLs and returns concise Turkish error feedback when malformed.
 */
object CustomApiUrlValidationHelper {

    fun validate(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return "Base URL boş bırakılamaz."
        if (trimmed.any { it.isWhitespace() }) return "URL içinde boşluk karakteri olamaz."
        if (trimmed.contains("://") &&
            !trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return "URL http:// veya https:// ile başlamalı."
        }
        val normalized = CustomApiUrlNormalizer.normalizeBaseUrl(trimmed)
        return try {
            val uri = URI(normalized)
            val scheme = uri.scheme?.lowercase(java.util.Locale.ROOT).orEmpty()
            val host = uri.host.orEmpty()
            when {
                scheme != "http" && scheme != "https" -> "URL http:// veya https:// ile başlamalı."
                host.isBlank() -> "Geçerli bir alan adı veya IP adresi gir."
                else -> null
            }
        } catch (_: Exception) {
            "Geçersiz URL biçimi."
        }
    }

    fun isValid(rawUrl: String): Boolean = validate(rawUrl) == null
}
