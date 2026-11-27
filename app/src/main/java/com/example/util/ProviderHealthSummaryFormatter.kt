package com.example.util

import com.example.data.managers.ProviderHealthState
import java.util.Locale

/**
 * Summarizes live AI provider circuit-breaker & latency telemetry into a concise Turkish status line.
 */
object ProviderHealthSummaryFormatter {

    private val SECRET_QUERY_REGEX = Regex("""[?#].*$""")
    private val CREDENTIALS_REGEX = Regex("""(?<=://)[^/@\s]+@""")
    private val TOKEN_FRAGMENT_REGEX = Regex("""(?i)\b(?:bearer\s+\S+|sk-[a-z0-9_-]{8,}|gsk_[a-z0-9_-]{8,}|aiza[0-9a-z_-]{12,})""")

    data class HealthSummary(
        val totalTracked: Int,
        val healthyCount: Int,
        val cooldownCount: Int,
        val fastestProviderKey: String?,
        val fastestLatencyMs: Long
    )

    internal fun sanitizeProviderLabel(rawKey: String): String {
        val trimmed = rawKey.trim()
        if (trimmed.isBlank()) return ""
        return trimmed
            .replace(CREDENTIALS_REGEX, "")
            .replace(SECRET_QUERY_REGEX, "")
            .replace(TOKEN_FRAGMENT_REGEX, "[redacted]")
            .take(64)
            .trim()
    }

    fun summarize(
        states: Map<String, ProviderHealthState>,
        nowMs: Long = System.currentTimeMillis()
    ): HealthSummary {
        if (states.isEmpty()) {
            return HealthSummary(0, 0, 0, null, 0L)
        }
        var total = 0
        var healthy = 0
        var cooldown = 0
        var fastestKey: String? = null
        var fastestMs = Long.MAX_VALUE

        for ((rawKey, st) in states) {
            val key = sanitizeProviderLabel(rawKey)
            if (key.isBlank()) continue
            total++
            val inCooldown = st.cooldownUntilMs > nowMs
            if (inCooldown) {
                cooldown++
            } else {
                healthy++
                val lat = st.lastLatencyMs
                if (lat > 0L) {
                    val isFaster = lat < fastestMs ||
                        (lat == fastestMs && (fastestKey == null || key.lowercase(Locale.ROOT) < fastestKey.lowercase(Locale.ROOT)))
                    if (isFaster) {
                        fastestMs = lat
                        fastestKey = key
                    }
                }
            }
        }
        return HealthSummary(
            totalTracked = total,
            healthyCount = healthy,
            cooldownCount = cooldown,
            fastestProviderKey = fastestKey,
            fastestLatencyMs = if (fastestKey != null && fastestMs < Long.MAX_VALUE) fastestMs else 0L
        )
    }

    fun formatBanner(summary: HealthSummary): String {
        if (summary.totalTracked <= 0) {
            return "Tüm sağlayıcılar hazır • Henüz hata veya bekleme yok"
        }
        val safeHealthy = summary.healthyCount.coerceAtLeast(0)
        val safeCooldown = summary.cooldownCount.coerceAtLeast(0)
        val base = "Sağlıklı: $safeHealthy • Beklemede: $safeCooldown"
        val fastest = summary.fastestProviderKey?.let { sanitizeProviderLabel(it) }
        return if (!fastest.isNullOrBlank() && summary.fastestLatencyMs > 0L) {
            "$base • En Hızlı: $fastest (${summary.fastestLatencyMs} ms)"
        } else {
            base
        }
    }
}
