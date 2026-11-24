package com.example.util

import com.example.data.local.BotEntity
import java.util.Locale

/**
 * Infers up to 3 Turkish genre tags from a bot's scenario, personality, and universe name.
 */
object StoryGenreTagClassifier {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    fun inferTags(bot: BotEntity): List<String> {
        val text = "${bot.universeName} ${bot.scenario} ${bot.aiPersonality} ${bot.writingStyle}"
            .lowercase(trLocale)
        val tags = mutableListOf<String>()
        if (text.contains("büyü") || text.contains("ejder") || text.contains("krallık") || text.contains("kule") || text.contains("fantastik")) {
            tags.add("Fantastik")
        }
        if (text.contains("uzay") || text.contains("galaksi") || text.contains("siber") || text.contains("yapay zeka") || text.contains("kozmik")) {
            tags.add("Bilim Kurgu")
        }
        if (text.contains("gizem") || text.contains("dedektif") || text.contains("cinayet") || text.contains("karanlık") || text.contains("lanet")) {
            tags.add("Gizem")
        }
        if (text.contains("aşk") || text.contains("romantik") || text.contains("kalp") || text.contains("sevgili")) {
            tags.add("Romantik")
        }
        if (text.contains("savaş") || text.contains("kılıç") || text.contains("ajan") || text.contains("operasyon") || text.contains("aksiyon")) {
            tags.add("Aksiyon")
        }
        return if (tags.isEmpty()) listOf("Genel Kurgu") else tags.distinct().take(3)
    }
}
