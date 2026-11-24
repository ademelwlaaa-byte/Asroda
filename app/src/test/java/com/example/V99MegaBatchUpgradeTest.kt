package com.example

import com.example.data.local.BotEntity
import com.example.data.managers.ProviderHealthState
import com.example.util.ProviderHealthScoreCalculator
import com.example.util.StoryGenreTagClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V99MegaBatchUpgradeTest {

    @Test
    fun providerHealthScoreCalculator_scoresHealthyVsDegradedFleets() {
        val now = 1_000_000L
        val healthy = ProviderHealthScoreCalculator.compute(
            mapOf("gemini" to ProviderHealthState(providerKey = "gemini", lastLatencyMs = 300L)),
            nowMs = now
        )
        assertEquals(100, healthy.score)
        assertEquals("Mükemmel", healthy.grade)

        val degraded = ProviderHealthScoreCalculator.compute(
            mapOf("groq" to ProviderHealthState(providerKey = "groq", cooldownUntilMs = now + 60_000L, consecutiveFailures = 2, lastLatencyMs = 5000L)),
            nowMs = now
        )
        assertTrue(degraded.score < 40)
        assertEquals("Kritik", degraded.grade)
    }

    @Test
    fun storyGenreTagClassifier_infersTurkishGenresFromScenario() {
        val bot = BotEntity(
            id = "b1",
            mode = "universe",
            universeName = "Kozmik Kule",
            keyCharactersJson = "[]",
            aiName = "Aiden",
            aiPersonality = "Gizemli bir büyücü",
            userCharName = "Sen",
            userCharDesc = "",
            scenario = "Uzay gemisi karanlık bir büyü kulesine iner.",
            openingMessage = "Hoş geldin.",
            writingStyle = "Edebi",
            intensity = "Medium"
        )
        val tags = StoryGenreTagClassifier.inferTags(bot)
        assertTrue(tags.contains("Fantastik"))
        assertTrue(tags.contains("Bilim Kurgu"))
        assertTrue(tags.contains("Gizem"))
    }
}
