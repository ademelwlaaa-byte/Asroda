package com.example.util

import java.util.Locale

/**
 * Classifies AI model identifiers into concise Turkish capability badges for Custom API & model pickers.
 */
object ProviderModelTagClassifier {

    fun classifyTags(modelId: String): List<String> {
        val m = modelId.trim().lowercase(Locale.ROOT)
        if (m.isEmpty()) return emptyList()
        val tags = mutableListOf<String>()
        if (m.endsWith(":free") || m.contains("free")) tags.add("🆓 Ücretsiz")
        if (m.contains("r1") || m.contains("thinking") || m.contains("reason") || m.startsWith("o1") || m.startsWith("o3")) {
            tags.add("🧠 Muhakeme")
        }
        if (m.contains("flash") || m.contains("mini") || m.contains("haiku") || m.contains("instant") || m.contains("8b")) {
            tags.add("⚡ Hızlı")
        }
        if (m.contains("pro") || m.contains("opus") || m.contains("sonnet") || m.contains("70b") || m.contains("405b")) {
            tags.add("✦ Güçlü")
        }
        if (m.contains("128k") || m.contains("200k") || m.contains("1m") || m.contains("gemini-2")) {
            tags.add("📖 Uzun Bağlam")
        }
        return tags.distinct().take(3)
    }

    fun formatCompact(modelId: String): String = classifyTags(modelId).joinToString(" • ")
}
