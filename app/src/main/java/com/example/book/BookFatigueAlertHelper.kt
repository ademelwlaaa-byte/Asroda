package com.example.book

/**
 * Translates Book Mode fatigue and mystery counters into concise Turkish
 * tactical readiness indicators for Chronicle ribbons and chapter recaps.
 */
object BookFatigueAlertHelper {

    fun fatigueBadge(fatigue: Int): String {
        val safe = fatigue.coerceIn(0, 100)
        return when {
            safe >= 80 -> "🚨 Kritik Yorgunluk ($safe)"
            safe >= 55 -> "😮‍💨 Yorgun ($safe)"
            safe >= 25 -> "🧭 Dengeli ($safe)"
            else -> "⚡ Dinç ($safe)"
        }
    }

    fun mysteryBadge(mystery: Int): String {
        val safe = mystery.coerceIn(0, 100)
        return when {
            safe >= 70 -> "🌌 Yüksek Gizem ($safe)"
            safe >= 35 -> "🕯️ Derinleşen Sır ($safe)"
            else -> "🔍 Sakin İz ($safe)"
        }
    }

    fun formatStatusRibbon(state: BookReadState): String {
        return "${fatigueBadge(state.fatigue)} • ${mysteryBadge(state.mystery)}"
    }
}
