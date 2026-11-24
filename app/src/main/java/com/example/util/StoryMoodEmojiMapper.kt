package com.example.util

import java.util.Locale

/**
 * Maps Turkish and English mood/atmosphere keywords to consistent atmospheric emoji labels.
 */
object StoryMoodEmojiMapper {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    fun mapToBadge(moodText: String): String {
        val m = moodText.trim().lowercase(trLocale)
        return when {
            m.contains("romantik") || m.contains("aşk") || m.contains("şefkat") || m.contains("romantic") -> "💫 Romantik"
            m.contains("gerilim") || m.contains("korku") || m.contains("tehlike") || m.contains("tense") -> "⚡ Gerilim"
            m.contains("gizem") || m.contains("esrar") || m.contains("karanlık") || m.contains("mystery") -> "🕯 Gizemli"
            m.contains("hüzün") || m.contains("üzgün") || m.contains("melankol") || m.contains("sad") -> "🌧 Melankolik"
            m.contains("aksiyon") || m.contains("savaş") || m.contains("çatışma") || m.contains("action") -> "⚔ Aksiyon"
            m.contains("mutlu") || m.contains("neşe") || m.contains("umut") || m.contains("happy") -> "☀ Umutlu"
            else -> "✦ Dengeli"
        }
    }
}
