package com.example.util

import java.util.Locale

/**
 * Recommends smart HTTP read timeouts (ms) based on model class so deep reasoning models
 * have ample thinking headroom while fast small models fail over rapidly.
 */
object ProviderTimeoutAdvisor {

    fun recommendedReadTimeoutMs(modelId: String): Long {
        val m = modelId.trim().lowercase(Locale.ROOT)
        return when {
            m.contains("r1") || m.contains("thinking") || m.contains("reason") ||
                m.startsWith("o1") || m.startsWith("o3") || m.contains("opus") -> 120_000L
            m.contains("flash") || m.contains("mini") || m.contains("haiku") ||
                m.contains("instant") || m.contains("8b") || m.contains("nano") -> 45_000L
            else -> 75_000L
        }
    }
}
