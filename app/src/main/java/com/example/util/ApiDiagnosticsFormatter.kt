package com.example.util

import com.example.data.managers.ProviderHealthState
import com.example.data.repository.ProviderFallbackLogEntry

/**
 * Formats a sanitized, key-free diagnostic snapshot of provider health and fallback attempts
 * for 1-tap clipboard export inside FallbackLogModal.
 */
object ApiDiagnosticsFormatter {

    fun formatReport(
        appVersion: String,
        healthMap: Map<String, ProviderHealthState>,
        logs: List<ProviderFallbackLogEntry>,
        nowMs: Long = System.currentTimeMillis()
    ): String {
        return buildString {
            append("=== Asroda v").append(appVersion).append(" API Tanılama Raporu ===\n")
            val healthSummary = ProviderHealthSummaryFormatter.summarize(healthMap, nowMs)
            append("Özet: ").append(ProviderHealthSummaryFormatter.formatBanner(healthSummary)).append("\n")
            val rateLine = ProviderFallbackSuccessRateHelper.formatCompact(logs)
            if (rateLine.isNotBlank()) {
                append("Başarı: ").append(rateLine).append("\n")
            }
            if (healthMap.isNotEmpty()) {
                append("\n[Sağlayıcı Sağlığı]\n")
                healthMap.values.sortedBy { it.providerKey }.forEach { st ->
                    val status = if (st.cooldownUntilMs > nowMs) {
                        val remLabel = ProviderCooldownFormatter.formatRemaining(st.cooldownUntilMs - nowMs)
                        "COOLDOWN (${st.lastFailureType?.name ?: "ERR"}, $remLabel)"
                    } else {
                        val rolling = ProviderRollingLatencyTracker.getMetrics(st.providerKey)
                        if (rolling != null && rolling.sampleCount >= 2) {
                            "HAZIR (son ${st.lastLatencyMs}ms • medyan ${rolling.medianMs}ms)"
                        } else {
                            "HAZIR (${st.lastLatencyMs}ms)"
                        }
                    }
                    append("- ").append(st.providerKey)
                        .append(": ").append(status)
                        .append(" | Başarı: ").append(st.totalSuccessCount)
                        .append(", Hata: ").append(st.totalFailureCount)
                        .append("\n")
                }
            }
            if (logs.isNotEmpty()) {
                append("\n[Son Deneme Logları (").append(logs.take(12).size).append(")]\n")
                logs.take(12).forEach { entry ->
                    append("- ").append(entry.providerName)
                        .append(" [L").append(entry.layer).append("]: ")
                        .append(entry.status)
                    entry.statusCode?.let { append(" (HTTP ").append(it).append(")") }
                    entry.errorMessage?.takeIf { it.isNotBlank() }?.let {
                        append(" • ").append(it.take(120))
                    }
                    append("\n")
                }
            }
        }.trim()
    }
}
