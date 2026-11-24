package com.example

import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.managers.ProviderHealthState
import com.example.util.ChatTranscriptFormatter
import com.example.util.ProviderHealthScoreCalculator
import com.example.util.StoryGenreTagClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V100MilestoneUpgradeTest {

    @Test
    fun v100Milestone_endToEndUtilitiesIntegrateCleanly() {
        val bot = BotEntity(
            id = "v100_bot",
            mode = "universe",
            universeName = "Asroda Galaksisi",
            keyCharactersJson = """[{"name":"Aiden"},{"name":"Natasha"}]""",
            aiName = "Aiden",
            aiPersonality = "Cesur ve gizemli bir gezgin",
            userCharName = "Kaan",
            userCharDesc = "Kaşif",
            scenario = "Kozmik bir uzay istasyonunda büyük bir savaş başlar.",
            openingMessage = "*Yıldızlara bakar.* \"Yolculuk şimdi başlıyor!\"",
            writingStyle = "Edebi",
            intensity = "Medium"
        )
        val tags = StoryGenreTagClassifier.inferTags(bot)
        assertTrue(tags.contains("Bilim Kurgu"))
        assertTrue(tags.contains("Aksiyon"))

        val md = ChatTranscriptFormatter.formatMarkdown(
            botDisplayName = "Aiden",
            userDisplayName = "Kaan",
            messages = listOf(
                MessageEntity(id = "1", botId = "v100_bot", role = "assistant", text = "Yolculuk şimdi başlıyor!", timestamp = 1_700_000_000_000L)
            )
        )
        assertTrue(md.contains("# Aiden — Hikaye Kaydı"))
        assertTrue(md.contains("1 mesaj"))

        val score = ProviderHealthScoreCalculator.compute(
            mapOf("gemini" to ProviderHealthState(providerKey = "gemini", lastLatencyMs = 250L))
        )
        assertEquals(100, score.score)
        assertEquals("Mükemmel", score.grade)
    }
}
