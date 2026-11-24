package com.example.book

/**
 * Translates Book Mode fatigue and mystery counters into concise Turkish
 * tactical readiness indicators for Chronicle ribbons and chapter recaps.
 */
object BookFatigueAlertHelper {

    fun fatigueBadge(fatigue: Int): String = when {
        fatigue >= 80 -> "🚨 Kritik Yorgunluk ($fatigue)"
        fatigue >= 55 -> "😮‍💨 Yorgun ($fatigue)"
        fatigue >= 25 -> "🧭 Dengeli ($fatigue)"
        else -> "⚡ Dinç ($fatigue)"
    }

    fun mysteryBadge(mystery: Int): String = when {
        mystery >= 70 -> "🌌 Yüksek Gizem ($mystery)"
        mystery >= 35 -> "🕯️ Derinleşen Sır ($mystery)"
        else -> "🔍 Sakin İz ($mystery)"
    }

    fun formatStatusRibbon(state: BookReadState): String {
        return "${fatigueBadge(state.fatigue)} • ${mysteryBadge(state.mystery)}"
    }
}
