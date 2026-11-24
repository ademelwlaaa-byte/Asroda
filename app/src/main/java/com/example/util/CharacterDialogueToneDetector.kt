package com.example.util

import java.util.Locale

/**
 * Classifies the expressive delivery tone of a single Turkish roleplay/story turn.
 */
object CharacterDialogueToneDetector {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    fun detectTone(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return "Dengeli"
        val lower = trimmed.lowercase(trLocale)
        return when {
            lower.contains("fısılda") || lower.contains("kısık sesle") || lower.contains("alçak sesle") -> "🤫 Fısıltı"
            lower.contains("bağır") || lower.contains("haykır") || trimmed.endsWith("?!") || trimmed.endsWith("!?") -> "🔥 Yüksek Ton"
            trimmed.endsWith("?") -> "❓ Sorgulayıcı"
            trimmed.endsWith("...") || trimmed.endsWith("…") || lower.contains("iç çeker") -> "🌙 Düşünceli"
            else -> "✦ Dengeli"
        }
    }
}
