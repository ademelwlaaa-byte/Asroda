package com.example.util

import java.util.Locale

/**
 * Lightweight Turkish vs English language heuristic for multi-language TTS & prose handling.
 */
object TtsVoiceLanguageDetector {

    private val trDiacritics = setOf('ç', 'ğ', 'ı', 'ö', 'ş', 'ü', 'Ç', 'Ğ', 'İ', 'Ö', 'Ş', 'Ü')
    private val trWords = setOf("bir", "ve", "ama", "için", "değil", "gibi", "daha", "sonra", "kadar", "olarak", "ben", "sen", "biz", "nasıl", "neden")
    private val enWords = setOf("the", "and", "with", "that", "this", "from", "have", "your", "what", "where", "would", "could", "should", "they", "their")

    fun detectLanguageCode(text: String): String {
        if (text.isBlank()) return "tr"
        if (text.any { it in trDiacritics }) return "tr"
        val tokens = text.lowercase(Locale.ROOT)
            .split(Regex("[^a-z]+"))
            .filter { it.length >= 2 }
        var trScore = 0
        var enScore = 0
        for (t in tokens) {
            if (t in trWords) trScore++
            if (t in enWords) enScore++
        }
        return if (enScore >= 2 && enScore > trScore) "en" else "tr"
    }

    fun isLikelyEnglish(text: String): Boolean = detectLanguageCode(text) == "en"
}
