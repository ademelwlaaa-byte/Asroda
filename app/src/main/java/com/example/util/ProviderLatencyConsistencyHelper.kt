package com.example.util

/**
 * Evaluates provider latency jitter (spread between fastest and slowest recent calls)
 * into a concise Turkish stability badge.
 */
object ProviderLatencyConsistencyHelper {

    data class StabilityReport(
        val spreadMs: Long,
        val badge: String
    )

    fun evaluate(samplesMs: List<Long>): StabilityReport {
        val valid = samplesMs.filter { it > 0L }
        if (valid.size < 2) {
            return StabilityReport(0L, "📊 Veri Toplanıyor")
        }
        val min = valid.minOrNull() ?: 0L
        val max = valid.maxOrNull() ?: 0L
        val spread = (max - min).coerceAtLeast(0L)
        val badge = when {
            spread <= 450L -> "💎 Çok Stabil (±${spread}ms)"
            spread <= 1500L -> "〰️ Dengeli (±${spread}ms)"
            else -> "🌊 Dalgalı (±${spread}ms)"
        }
        return StabilityReport(spread, badge)
    }
}
