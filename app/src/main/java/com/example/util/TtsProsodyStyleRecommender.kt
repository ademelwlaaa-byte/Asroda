package com.example.util

import java.util.Locale

/**
 * Suggests an optimal free on-device TTS speed/tone profile matching the story's dominant atmosphere.
 */
object TtsProsodyStyleRecommender {

    data class TtsStyleAdvice(
        val label: String,
        val recommendedRate: Float
    )

    fun recommendForAtmosphere(atmosphereLabel: String): TtsStyleAdvice {
        val lower = atmosphereLabel.lowercase(Locale.forLanguageTag("tr-TR"))
        return when {
            lower.contains("aksiyon") || lower.contains("savaş") || lower.contains("gerilim") ->
                TtsStyleAdvice("🔥 Dinamik Anlatım (1.08x)", 1.08f)
            lower.contains("gizem") || lower.contains("karanlık") || lower.contains("korku") ->
                TtsStyleAdvice("🕯️ Ağır & Gizemli (0.92x)", 0.92f)
            lower.contains("romantik") || lower.contains("duygusal") || lower.contains("dram") ->
                TtsStyleAdvice("💞 Sıcak & Yumuşak (0.96x)", 0.96f)
            lower.contains("komedi") || lower.contains("neşeli") || lower.contains("mizah") ->
                TtsStyleAdvice("✨ Canlı & Akıcı (1.05x)", 1.05f)
            else ->
                TtsStyleAdvice("🎙️ Dengeli Roman (1.00x)", 1.00f)
        }
    }
}
