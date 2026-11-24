package com.example.util

/**
 * Classifies AI provider latency in milliseconds into concise Turkish speed badges.
 */
object ProviderLatencyBadgeFormatter {

    fun formatBadge(latencyMs: Long): String {
        if (latencyMs <= 0L) return "—"
        return when {
            latencyMs <= 600L -> "⚡ Çok Hızlı (${latencyMs} ms)"
            latencyMs <= 1800L -> "✓ İyi (${latencyMs} ms)"
            latencyMs <= 4000L -> "⏳ Yavaş (${latencyMs} ms)"
            else -> "🐢 Çok Yavaş (${latencyMs} ms)"
        }
    }
}
