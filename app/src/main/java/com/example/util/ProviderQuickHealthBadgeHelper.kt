package com.example.util

import com.example.data.managers.ProviderHealthState

/**
 * Formats a concise Turkish 1-word health pill for any [ProviderHealthState].
 */
object ProviderQuickHealthBadgeHelper {

    fun formatBadge(state: ProviderHealthState?, nowMs: Long = System.currentTimeMillis()): String {
        if (state == null) return "Hazır"
        return when {
            state.cooldownUntilMs > nowMs -> "Beklemede"
            state.consecutiveFailures >= 2 -> "Sorunlu"
            else -> "Hazır"
        }
    }
}
