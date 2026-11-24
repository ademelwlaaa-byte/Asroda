package com.example.util

import com.example.data.managers.ProviderHealthState

/**
 * Computes a 0-100 overall AI provider fleet health score and Turkish grade.
 */
object ProviderHealthScoreCalculator {

    data class FleetScore(
        val score: Int,
        val grade: String
    )

    fun compute(
        states: Map<String, ProviderHealthState>,
        nowMs: Long = System.currentTimeMillis()
    ): FleetScore {
        if (states.isEmpty()) return FleetScore(100, "Mükemmel")
        var total = 0
        for (st in states.values) {
            var s = 100
            if (st.cooldownUntilMs > nowMs) s -= 60
            s -= (st.consecutiveFailures * 15).coerceAtMost(35)
            if (st.lastLatencyMs > 4000L) s -= 15 else if (st.lastLatencyMs > 2000L) s -= 5
            total += s.coerceIn(0, 100)
        }
        val avg = (total / states.size).coerceIn(0, 100)
        val grade = when {
            avg >= 85 -> "Mükemmel"
            avg >= 65 -> "İyi"
            avg >= 40 -> "Dikkat"
            else -> "Kritik"
        }
        return FleetScore(score = avg, grade = grade)
    }
}
