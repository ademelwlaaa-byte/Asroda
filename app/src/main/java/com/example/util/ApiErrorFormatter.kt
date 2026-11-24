package com.example.util

import org.json.JSONObject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Locale
import javax.net.ssl.SSLException

/**
 * Translates raw LLM / Custom API HTTP responses and network exceptions into concise,
 * actionable Turkish messages while guaranteeing API keys/secrets are always redacted.
 */
object ApiErrorFormatter {

    private val HTTP_CODE_REGEX = Regex("""\b(?:SSE\s+)?HTTP\s+(\d{3})\b""", RegexOption.IGNORE_CASE)

    fun formatException(e: Throwable, providerLabel: String? = null): String {
        val rawMsg = SecureApiKeyVault.redactSecrets(e.message.orEmpty()).trim()
        val labelPrefix = providerLabel?.trim()?.takeIf { it.isNotBlank() }?.let { "[$it] " } ?: ""

        if (e is SseStreamParser.SseHttpException) {
            return formatHttpError(
                httpCode = e.httpCode,
                rawBody = e.errorBody,
                providerLabel = providerLabel
            )
        }

        if (e is UnknownHostException || rawMsg.contains("unable to resolve host", ignoreCase = true)) {
            return "${labelPrefix}Sunucu adresi bulunamadı. İnternet bağlantınızı ve Özel API Base URL adresini kontrol edin."
        }
        if (e is ConnectException || rawMsg.contains("failed to connect", ignoreCase = true) ||
            rawMsg.contains("connection refused", ignoreCase = true)
        ) {
            return "${labelPrefix}Sunucuya bağlanılamadı (bağlantı reddedildi). Yerel sunucunun (Ollama/LM Studio) çalıştığını ve adres/port bilgisini kontrol edin."
        }
        if (e is SSLException || rawMsg.contains("sslhandshake", ignoreCase = true) ||
            rawMsg.contains("certpathvalidatorexception", ignoreCase = true)
        ) {
            return "${labelPrefix}SSL/TLS güvenlik sertifikası doğrulanamadı. Özel API adresinde https/http protokolünü kontrol edin."
        }
        if (e is SocketTimeoutException || rawMsg.contains("timeout", ignoreCase = true) ||
            rawMsg.contains("zaman aşımı", ignoreCase = true)
        ) {
            return "${labelPrefix}AI sağlayıcısı zaman aşımına uğradı. Ağınızı kontrol edin veya Ayarlar'dan daha hızlı bir model seçin."
        }

        val extractedCode = HTTP_CODE_REGEX.find(rawMsg)?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (extractedCode != null) {
            val bodyPart = rawMsg.substringAfter(":", "").trim()
            return formatHttpError(extractedCode, bodyPart, providerLabel)
        }

        return when {
            rawMsg.contains("içerik kısıtlaması", ignoreCase = true) ||
                rawMsg.contains("içerik filtresi", ignoreCase = true) ||
                rawMsg.contains("güvenlik filtresine", ignoreCase = true) ||
                rawMsg.contains("politikası", ignoreCase = true) ||
                rawMsg.contains("refusal", ignoreCase = true) ||
                rawMsg.contains("content_filter", ignoreCase = true) -> {
                rawMsg.ifBlank {
                    "Seçili AI sağlayıcısı içerik kısıtlaması politikası gereği bu yanıtı süzdü. Lütfen Ayarlar -> AI Model Ayarları menüsünden farklı bir model seçin."
                }
            }
            rawMsg.contains("429", ignoreCase = true) ||
                rawMsg.contains("kotası", ignoreCase = true) ||
                rawMsg.contains("rate limit", ignoreCase = true) -> {
                "${labelPrefix}API kullanım kotası doldu (429 Rate Limit). Biraz bekleyin veya Ayarlar -> AI Model Ayarları'ndan başka bir sağlayıcı/model seçin."
            }
            rawMsg.contains("invalid_api_key", ignoreCase = true) ||
                rawMsg.contains("incorrect api key", ignoreCase = true) ||
                rawMsg.contains("unauthorized", ignoreCase = true) -> {
                "${labelPrefix}API anahtarı geçersiz veya eksik (401). Lütfen Ayarlar -> AI Model Ayarları / Özel API bölümünden anahtarınızı kontrol edin."
            }
            rawMsg.contains("insufficient_quota", ignoreCase = true) ||
                rawMsg.contains("insufficient_balance", ignoreCase = true) ||
                rawMsg.contains("credit balance", ignoreCase = true) -> {
                "${labelPrefix}API hesap bakiyesi veya kredisi yetersiz (402). Sağlayıcı panelinizden kredinizi kontrol edin veya farklı bir sağlayıcı seçin."
            }
            rawMsg.isNotBlank() -> "${labelPrefix}${sanitizeDisplayDetail(rawMsg)}"
            else -> "${labelPrefix}Yanıt alınamadı. Lütfen ağ bağlantınızı veya API ayarlarınızı kontrol edin."
        }
    }

    fun formatHttpError(
        httpCode: Int,
        rawBody: String,
        providerLabel: String? = null
    ): String {
        val labelPrefix = providerLabel?.trim()?.takeIf { it.isNotBlank() }?.let { "[$it] " } ?: ""
        val detail = extractConciseErrorDetail(rawBody)
        val detailSuffix = if (detail.isNotBlank()) " ($detail)" else ""

        return when (httpCode) {
            400, 422 -> "${labelPrefix}Geçersiz istek veya desteklenmeyen model parametresi (HTTP $httpCode). Özel API model adını ve formatını kontrol edin.$detailSuffix"
            401 -> "${labelPrefix}API anahtarı geçersiz veya yetkisiz (HTTP 401). Ayarlar -> AI Model Ayarları / Özel API bölümünden anahtarınızı doğrulayın.$detailSuffix"
            402 -> "${labelPrefix}API hesap kredisi/bakiyesi yetersiz (HTTP 402). Sağlayıcı hesabınızı kontrol edin veya başka bir sağlayıcı seçin.$detailSuffix"
            403 -> "${labelPrefix}Erişim reddedildi (HTTP 403). API anahtarı yetkisini, model erişimini veya bölge kısıtlamasını kontrol edin.$detailSuffix"
            404 -> "${labelPrefix}Endpoint veya model bulunamadı (HTTP 404). Özel API Base URL (ör. .../v1) ve Model ID alanlarını kontrol edin.$detailSuffix"
            408 -> "${labelPrefix}Sunucu yanıt süresi doldu (HTTP 408). Lütfen tekrar deneyin.$detailSuffix"
            429 -> "${labelPrefix}API kullanım kotası doldu (HTTP 429 Rate Limit). Biraz bekleyin veya farklı bir sağlayıcı/model seçin.$detailSuffix"
            in 500..599 -> "${labelPrefix}AI sağlayıcı sunucusu geçici olarak yanıt veremiyor (HTTP $httpCode). Bir süre sonra tekrar deneyin.$detailSuffix"
            else -> "${labelPrefix}API isteği başarısız oldu (HTTP $httpCode).$detailSuffix"
        }
    }

    /**
     * Extracts a clean, single-line human-readable error message from OpenAI/Gemini/Anthropic JSON
     * payloads, strips HTML error pages, redacts secrets, and bounds length.
     */
    internal fun extractConciseErrorDetail(rawBody: String): String {
        val redacted = SecureApiKeyVault.redactSecrets(rawBody).trim()
        if (redacted.isBlank()) return ""

        // Strip raw HTML pages (e.g., nginx 404/502 HTML) so users never see <html>...</html>
        val lower = redacted.lowercase(Locale.ROOT)
        if (lower.startsWith("<!doctype") || lower.startsWith("<html") || lower.contains("</body>")) {
            return ""
        }

        if (redacted.startsWith("{")) {
            try {
                val obj = JSONObject(redacted)
                val errObj = obj.optJSONObject("error")
                val extracted = errObj?.optString("message")?.takeIf { it.isNotBlank() }
                    ?: obj.optString("message").takeIf { it.isNotBlank() }
                    ?: obj.optString("detail").takeIf { it.isNotBlank() }
                    ?: obj.optString("error").takeIf { it.isNotBlank() && !it.startsWith("{") }
                if (!extracted.isNullOrBlank()) {
                    return sanitizeDisplayDetail(extracted)
                }
            } catch (_: Exception) {
                // Fall through to plain text sanitization
            }
        }
        return sanitizeDisplayDetail(redacted)
    }

    private fun sanitizeDisplayDetail(raw: String): String {
        val singleLine = SecureApiKeyVault.redactSecrets(raw)
            .replace(Regex("""\s+"""), " ")
            .trim()
        return if (singleLine.length > 140) singleLine.take(137).trimEnd() + "..." else singleLine
    }
}
