package com.example.util

import java.util.Locale

/**
 * Recommends a lightweight, fast small/utility model from a fetched Custom API model list
 * without ever overriding a user's explicit manual selection.
 */
object CustomApiModelRecommendationHelper {

    fun pickFastestUtilityModel(models: List<String>, excludePrimary: String = ""): String? {
        val candidates = models
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals(excludePrimary.trim(), ignoreCase = true) }
        if (candidates.isEmpty()) return null

        fun score(modelId: String): Int {
            val m = modelId.lowercase(Locale.ROOT)
            var s = 0
            if (m.contains("flash-lite") || m.contains("nano") || m.contains("mini")) s += 45
            if (m.contains("flash") || m.contains("haiku") || m.contains("instant")) s += 35
            if (m.contains("8b") || m.contains("7b") || m.contains("9b") || m.contains("3b")) s += 30
            if (m.endsWith(":free") || m.contains("free")) s += 15
            if (m.contains("opus") || m.contains("405b") || m.contains("r1") || m.contains("thinking")) s -= 40
            return s
        }

        return candidates.maxByOrNull { score(it) }
    }
}
