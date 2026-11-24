package com.example.util

import com.example.data.managers.ProviderHealthState

/**
 * Summarizes live AI provider circuit-breaker & latency telemetry into a concise Turkish status line.
 */
object ProviderHealthSummaryFormatter {

    data class HealthSummary(
        val totalTracked: Int,
        val healthyCount: Int,
        val cooldownCount: Int,
        val fastestProviderKey: String?,
        val fastestLatencyMs: Long
    )

    fun summarize(
        states: Map<String, ProviderHealthState>,
        nowMs: Long = System.currentTimeMillis()
    ): HealthSummary {
        if (states.isEmpty()) {
            return HealthSummary(0, 0, 0, null, 0L)
        }
        var healthy = 0
        var cooldown = 0
        var fastestKey: String? = null
        var fastestMs = Long.MAX_VALUE

        for ((key, st) in states) {
            val inCooldown = st.cooldownUntilMs > nowMs
            if (inCooldown) {
                cooldown++
            } else {
                healthy++
                if (st.lastLatencyMs > 0L && st.lastLatencyMs < fastestMs) {
                    fastestMs = st.lastLatencyMs
                    fastestKey = key
                }
            }
        }
        return HealthSummary(
            totalTracked = states.size,
            healthyCount = healthy,
            cooldownCount = cooldown,
            fastestProviderKey = fastestKey,
            fastestLatencyMs = if (fastestKey != null) fastestMs else 0L
        )
    }

    fun formatBanner(summary: HealthSummary): String {
        if (summary.totalTracked == 0) {
            return "Tüm sağlayıcılar hazır • Henüz hata veya bekleme yok"
        }
        val base = "Sağlıklı: ${summary.healthyCount} • Beklemede: ${summary.cooldownCount}"
        val fastest = summary.fastestProviderKey
        return if (!fastest.isNullOrBlank() && summary.fastestLatencyMs > 0L) {
            "$base • En Hızlı: $fastest (${summary.fastestLatencyMs} ms)"
        } else {
            base
        }
    }
}
