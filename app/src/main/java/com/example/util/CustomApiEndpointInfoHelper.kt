package com.example.util

import java.net.URI
import java.util.Locale

/**
 * Extracts user-friendly endpoint diagnostics (local vs cloud, HTTPS status, clean host name)
 * for Custom API provider cards and logs.
 */
object CustomApiEndpointInfoHelper {

    private val ALLOWED_SCHEMES = setOf("http", "https")
    private val RFC1918_172_REGEX = Regex("""^172\.(?:1[6-9]|2\d|3[0-1])\.\d{1,3}\.\d{1,3}$""")
    private val CONTROL_OR_WS_REGEX = Regex("""[\u0000-\u0020\u007F]""")

    data class EndpointInfo(
        val host: String,
        val isLocalServer: Boolean,
        val isSecureHttps: Boolean,
        val badgeText: String
    )

    /**
     * Strips embedded URL credentials (`user:pass@`), query strings (`?api_key=...`),
     * and fragments (`#token=...`) so endpoint diagnostics and logs never leak secrets.
     */
    fun sanitizeUrlForDisplay(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank() || CONTROL_OR_WS_REGEX.containsMatchIn(trimmed)) return ""
        val uri = try {
            URI(if (trimmed.contains("://")) trimmed else "https://$trimmed")
        } catch (_: Exception) {
            return ""
        }
        val scheme = uri.scheme?.lowercase(Locale.ROOT).orEmpty()
        val host = uri.host?.lowercase(Locale.ROOT).orEmpty()
        if (scheme !in ALLOWED_SCHEMES || host.isBlank()) return ""
        val portPart = if (uri.port in 1..65535) ":${uri.port}" else ""
        val pathPart = uri.rawPath.orEmpty()
        return "$scheme://$host$portPart$pathPart"
    }

    fun inspect(rawUrl: String): EndpointInfo {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) {
            return EndpointInfo("", isLocalServer = false, isSecureHttps = false, badgeText = "URL yok")
        }
        if (CONTROL_OR_WS_REGEX.containsMatchIn(trimmed)) {
            return EndpointInfo("", isLocalServer = false, isSecureHttps = false, badgeText = "Geçersiz URL")
        }
        val uri = try {
            URI(if (trimmed.contains("://")) trimmed else "https://$trimmed")
        } catch (_: Exception) {
            null
        }
        val scheme = uri?.scheme?.lowercase(Locale.ROOT).orEmpty()
        val rawHost = uri?.host?.lowercase(Locale.ROOT).orEmpty()
        if (scheme !in ALLOWED_SCHEMES || rawHost.isBlank()) {
            return EndpointInfo("", isLocalServer = false, isSecureHttps = false, badgeText = "Geçersiz URL")
        }
        val bareHost = rawHost.removePrefix("[").removeSuffix("]")
        val isHttps = scheme == "https"
        val isLocal = bareHost == "localhost" ||
            bareHost == "127.0.0.1" ||
            bareHost == "0.0.0.0" ||
            bareHost == "::1" ||
            bareHost == "10.0.2.2" ||
            bareHost.startsWith("192.168.") ||
            bareHost.startsWith("10.") ||
            RFC1918_172_REGEX.matches(bareHost) ||
            bareHost.endsWith(".local")

        val badge = when {
            isLocal -> "Yerel Sunucu ($rawHost)"
            isHttps -> "🔒 $rawHost"
            else -> "⚠ HTTP • $rawHost"
        }
        return EndpointInfo(
            host = rawHost,
            isLocalServer = isLocal,
            isSecureHttps = isHttps,
            badgeText = badge
        )
    }
}
