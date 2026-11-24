package com.example

import com.example.data.local.BotEntity
import com.example.util.BotProfileExporter
import com.example.util.CustomApiUrlNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V59MegaBatchUpgradeTest {

    @Test
    fun customApiUrlNormalizer_fixesMissingSchemeTrailingSuffixesAndDoubleSlashes() {
        assertEquals(
            "https://openrouter.ai/api/v1",
            CustomApiUrlNormalizer.normalizeBaseUrl("  openrouter.ai//api/v1/chat/completions/ ")
        )
        assertEquals(
            "http://192.168.1.5:11434/v1",
            CustomApiUrlNormalizer.normalizeBaseUrl("192.168.1.5:11434/v1/models")
        )
        assertEquals(
            "https://api.groq.com/openai/v1",
            CustomApiUrlNormalizer.normalizeBaseUrl("https://api.groq.com/openai/v1/")
        )
    }

    @Test
    fun botProfileExporter_formatsCharacterCardCleanly() {
        val bot = BotEntity(
            id = "b1",
            mode = "personal",
            universeName = "",
            keyCharactersJson = "[]",
            aiName = "Natasha",
            aiPersonality = "Soğukkanlı, zeki ajan.",
            userCharName = "Aiden",
            userCharDesc = "",
            scenario = "Yağmurlu Manhattan çatısı.",
            openingMessage = "\"Geç kaldın.\"",
            writingStyle = "literary",
            intensity = "medium"
        )
        val md = BotProfileExporter.formatCharacterCardMarkdown(bot)
        assertTrue(md.startsWith("# Natasha (Karakter Kartı)"))
        assertTrue(md.contains("Soğukkanlı, zeki ajan."))
        assertTrue(md.contains("Yağmurlu Manhattan çatısı."))
    }
}
