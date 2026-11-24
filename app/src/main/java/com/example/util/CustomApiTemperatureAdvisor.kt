package com.example.util

import java.util.Locale

/**
 * Recommends an ideal sampling temperature badge for a Custom API model identifier
 * so users get rich roleplay without coherence breakdown.
 */
object CustomApiTemperatureAdvisor {

    fun recommendBadge(modelId: String): String? {
        val m = modelId.trim().lowercase(Locale.ROOT)
        if (m.isEmpty()) return null
        return when {
            m.contains("r1") || m.contains("reason") || m.contains("thinking") || m.startsWith("o1") || m.startsWith("o3") ->
                "🌡️ Önerilen Sıcaklık: 0.60"
            m.contains("mini") || m.contains("flash") || m.contains("haiku") || m.contains("8b") || m.contains("instant") ->
                "🌡️ Önerilen Sıcaklık: 0.75"
            m.contains("pro") || m.contains("sonnet") || m.contains("opus") || m.contains("70b") || m.contains("deepseek-chat") ->
                "🌡️ Önerilen Sıcaklık: 0.85"
            else -> null
        }
    }
}
