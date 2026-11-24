package com.example.util

/**
 * Formats Custom API connection test results with latency badges or actionable recovery advice.
 */
object CustomApiTestStatusFormatter {

    fun formatResult(
        isSuccess: Boolean,
        latencyMs: Long = 0L,
        httpStatusCode: Int? = null,
        errorDetail: String = ""
    ): String {
        return if (isSuccess) {
            val speed = ProviderLatencyBadgeFormatter.formatBadge(latencyMs)
            if (latencyMs > 0L) "✓ Bağlantı başarılı • $speed" else "✓ Bağlantı başarılı"
        } else {
            val hint = ProviderErrorRecoveryAdvisor.adviseForStatusCode(httpStatusCode)
            val detail = errorDetail.trim().take(80)
            if (detail.isNotBlank()) "✕ Hata ($detail) • $hint" else "✕ Bağlantı başarısız • $hint"
        }
    }
}
