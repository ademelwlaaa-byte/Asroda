package com.example.util

import com.example.data.repository.ProviderFallbackLogEntry

/**
 * Computes live API fallback log success rate for [FallbackLogModal].
 */
object ProviderFallbackSuccessRateHelper {

    data class SuccessStats(
        val totalAttempts: Int,
        val successCount: Int,
        val successPercent: Int
    )

    fun compute(logs: List<ProviderFallbackLogEntry>): SuccessStats {
        if (logs.isEmpty()) return SuccessStats(0, 0, 100)
        val ok = logs.count { it.status == "SUCCESS" }
        val pct = ((ok * 100f) / logs.size).toInt().coerceIn(0, 100)
        return SuccessStats(totalAttempts = logs.size, successCount = ok, successPercent = pct)
    }

    fun formatCompact(logs: List<ProviderFallbackLogEntry>): String {
        if (logs.isEmpty()) return ""
        val s = compute(logs)
        return "Son ${s.totalAttempts} denemede %${s.successPercent} başarı (${s.successCount}/${s.totalAttempts})"
    }
}
