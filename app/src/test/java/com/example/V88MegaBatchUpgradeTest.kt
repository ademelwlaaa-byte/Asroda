package com.example

import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.util.StoryArchiveBundler
import com.example.util.StoryBookmarkStore
import com.example.util.TtsSpeedLabelFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V88MegaBatchUpgradeTest {

    @Test
    fun storyArchiveBundler_combinesCardRecapAndBookmarks() {
        val bot = BotEntity(
            id = "b1",
            mode = "personal",
            universeName = "",
            keyCharactersJson = "[]",
            aiName = "Aiden",
            aiPersonality = "Kararlı ve cesur",
            userCharName = "Kaan",
            userCharDesc = "Gezgin",
            scenario = "Kuzey kulesi",
            openingMessage = "Hoş geldin.",
            writingStyle = "Edebi",
            intensity = "Medium"
        )
        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "assistant", text = "\"Hazır mısın?\" dedi.", timestamp = 1L)
        )
        val bms = listOf(
            StoryBookmarkStore.Bookmark(id = "bm1", messageId = "1", title = "İlk Karşılaşma", snippet = "Hazır mısın?", createdAtMs = 1L)
        )
        val archive = StoryArchiveBundler.buildCompleteArchive(bot, msgs, bms, nowMs = 60_000L)
        assertTrue(archive.contains("Aiden"))
        assertTrue(archive.contains("Bölüm Özeti"))
        assertTrue(archive.contains("İlk Karşılaşma"))
    }

    @Test
    fun ttsSpeedLabelFormatter_formatsTurkishSpeedTiers() {
        assertEquals("Yavaş (0.85x)", TtsSpeedLabelFormatter.formatLabel(0.85f))
        assertEquals("Doğal (1x)", TtsSpeedLabelFormatter.formatLabel(1.0f))
        assertEquals("Akıcı (1.15x)", TtsSpeedLabelFormatter.formatLabel(1.15f))
        assertEquals("Hızlı (1.3x)", TtsSpeedLabelFormatter.formatLabel(1.3f))
    }
}
