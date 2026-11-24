package com.example

import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.util.BotSearchFilterHelper
import com.example.util.ChapterSummaryExporter
import com.example.util.MessageTimestampFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V64MegaBatchUpgradeTest {

    @Test
    fun botSearchFilterHelper_matchesTurkishDiacriticsAcrossFields() {
        val bot = BotEntity(
            id = "b1",
            mode = "universe",
            universeName = "Soğuk Kule",
            keyCharactersJson = "[]",
            aiName = "Işık Büyücüsü",
            aiPersonality = "Bilge ve gizemli",
            userCharName = "Gezgin",
            userCharDesc = "Cesur",
            scenario = "Çiçekli bahçede kadim bir geçit açılır.",
            openingMessage = "Hoş geldin.",
            writingStyle = "Edebi",
            intensity = "Medium"
        )
        assertTrue(BotSearchFilterHelper.matches(bot, "isik"))
        assertTrue(BotSearchFilterHelper.matches(bot, "soguk"))
        assertTrue(BotSearchFilterHelper.matches(bot, "cicekli"))
        assertTrue(BotSearchFilterHelper.matches(bot, "buyucu"))
        assertFalse(BotSearchFilterHelper.matches(bot, "uzay gemisi"))
    }

    @Test
    fun chapterSummaryExporter_formatsAtmosphereQuoteAndRecentTurns() {
        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "user", text = "Kuleye tırmandım.", timestamp = 1L),
            MessageEntity(id = "2", botId = "b1", role = "assistant", text = "\"Bu fırtına daha yeni başlıyor!\" dedi gökyüzüne bakarak.", timestamp = 2L)
        )
        val recap = ChapterSummaryExporter.formatRecapMarkdown(
            title = "Aiden",
            userName = "Kaan",
            messages = msgs
        )
        assertTrue(recap.contains("Aiden — Bölüm Özeti"))
        assertTrue(recap.contains("Atmosfer:"))
        assertTrue(recap.contains("Anlatım Dengesi:"))
        assertTrue(recap.contains("Bu fırtına daha yeni başlıyor!"))
        assertTrue(recap.contains("**Kaan:**"))
    }

    @Test
    fun messageTimestampFormatter_formatsRelativeTurkishIntervals() {
        val now = 1_700_000_000_000L
        assertEquals("Az önce", MessageTimestampFormatter.formatRelative(now - 20_000L, now))
        assertEquals("5 dk önce", MessageTimestampFormatter.formatRelative(now - 5 * 60_000L, now))
        assertEquals("3 sa önce", MessageTimestampFormatter.formatRelative(now - 3 * 3_600_000L, now))
        assertEquals("Dün", MessageTimestampFormatter.formatRelative(now - 26 * 3_600_000L, now))
        assertEquals("4 gün önce", MessageTimestampFormatter.formatRelative(now - 4 * 86_400_000L, now))
    }
}
