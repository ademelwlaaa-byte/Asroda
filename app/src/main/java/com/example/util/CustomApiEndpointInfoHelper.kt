package com.example.util

import java.net.URI
import java.util.Locale

/**
 * Extracts user-friendly endpoint diagnostics (local vs cloud, HTTPS status, clean host name)
 * for Custom API provider cards and logs.
 */
object CustomApiEndpointInfoHelper {

    data class EndpointInfo(
        val host: String,
        val isLocalServer: Boolean,
        val isSecureHttps: Boolean,
        val badgeText: String
    )

    fun inspect(rawUrl: String): EndpointInfo {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) {
            return EndpointInfo("", isLocalServer = false, isSecureHttps = false, badgeText = "URL yok")
        }
        val uri = try {
            URI(if (trimmed.contains("://")) trimmed else "https://$trimmed")
        } catch (_: Exception) {
            null
        }
        val scheme = uri?.scheme?.lowercase(Locale.ROOT).orEmpty()
        val host = uri?.host?.lowercase(Locale.ROOT).orEmpty()
        val isHttps = scheme == "https"
        val isLocal = host == "localhost" ||
            host == "127.0.0.1" ||
            host == "10.0.2.2" ||
            host.startsWith("192.168.") ||
            host.startsWith("10.") ||
            host.endsWith(".local")

        val badge = when {
            isLocal -> "Yerel Sunucu ($host)"
            host.isNotBlank() && isHttps -> "🔒 $host"
            host.isNotBlank() -> "⚠ HTTP • $host"
            else -> "Geçersiz URL"
        }
        return EndpointInfo(
            host = host,
            isLocalServer = isLocal,
            isSecureHttps = isHttps,
            badgeText = badge
        )
    }
}
