package com.example.util

import java.util.Locale

/**
 * Classifies AI providers into routing tiers and summarizes fleet redundancy posture
 * for the Provider Reliability & Fallback modal.
 */
object ProviderRoutingTierHelper {

    fun classifyTier(providerName: String): String {
        val p = providerName.trim().lowercase(Locale.ROOT)
        return when {
            p.contains("custom") || p.contains("özel") || p.contains("local") || p.contains("ollama") -> "🔧 Özel API"
            p.contains("groq") || p.contains("cerebras") || p.contains("sambanova") -> "⚡ Ultra Hızlı"
            p.contains("gemini") || p.contains("openai") || p.contains("anthropic") || p.contains("claude") -> "🌟 Ana Akıllı"
            else -> "🔄 Yedek Havuz"
        }
    }

    fun summarizeRedundancy(activeProviderCount: Int): String = when {
        activeProviderCount <= 0 -> "⚠️ Aktif sağlayıcı yok"
        activeProviderCount == 1 -> "🟡 Tek Sağlayıcı (Yedek eklenmesi önerilir)"
        activeProviderCount in 2..3 -> "🟢 Dengeli Yedekli Havuz ($activeProviderCount sağlayıcı)"
        else -> "🛡️ Yüksek Yedekli Çoklu Filo ($activeProviderCount sağlayıcı)"
    }
}
