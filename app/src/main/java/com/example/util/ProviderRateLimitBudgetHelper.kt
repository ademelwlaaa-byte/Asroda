package com.example.util

/**
 * Computes remaining API quota headroom percentage and Turkish status tier.
 */
object ProviderRateLimitBudgetHelper {

    data class QuotaHeadroom(
        val remainingPercent: Int,
        val statusLabel: String
    )

    fun evaluate(used: Long, limit: Long): QuotaHeadroom {
        if (limit <= 0L) return QuotaHeadroom(100, "Sınırsız")
        val remaining = (limit - used).coerceAtLeast(0L)
        val pct = ((remaining * 100.0) / limit.toDouble()).toInt().coerceIn(0, 100)
        val label = when {
            pct >= 50 -> "Bol Kota"
            pct >= 20 -> "Orta"
            else -> "Kritik"
        }
        return QuotaHeadroom(remainingPercent = pct, statusLabel = label)
    }
}
