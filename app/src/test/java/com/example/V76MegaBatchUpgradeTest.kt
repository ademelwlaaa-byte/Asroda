package com.example

import com.example.data.repository.ProviderFallbackLogEntry
import com.example.util.ProviderFallbackSuccessRateHelper
import com.example.util.StoryOpeningQualityChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V76MegaBatchUpgradeTest {

    @Test
    fun providerFallbackSuccessRateHelper_computesAccurateRateAndLabel() {
        val logs = listOf(
            ProviderFallbackLogEntry(providerName = "gemini", status = "SUCCESS"),
            ProviderFallbackLogEntry(providerName = "groq", status = "SUCCESS"),
            ProviderFallbackLogEntry(providerName = "openrouter", status = "FAILED", statusCode = 429, errorMessage = "rate limit"),
            ProviderFallbackLogEntry(providerName = "deepseek", status = "SUCCESS")
        )
        val stats = ProviderFallbackSuccessRateHelper.compute(logs)
        assertEquals(4, stats.totalAttempts)
        assertEquals(3, stats.successCount)
        assertEquals(75, stats.successPercent)

        val compact = ProviderFallbackSuccessRateHelper.formatCompact(logs)
        assertTrue(compact.contains("%75 başarı"))
        assertTrue(compact.contains("3/4"))
    }

    @Test
    fun storyOpeningQualityChecker_scoresRichVsMinimalOpenings() {
        val rich = StoryOpeningQualityChecker.evaluate(
            openingMessage = "*Yağmur camlara vururken haritayı masaya serer.* \"Bu gece kuleye gizlice sızmamız gerekiyor, hazır mısın?\"",
            scenario = "İstanbul'un sisli sokaklarında eski bir saat kulesinde gizli bir geçit keşfedilir."
        )
        assertTrue(rich.scorePercent >= 85)
        assertEquals("✦ Roman Kalitesi", rich.badge)

        val minimal = StoryOpeningQualityChecker.evaluate(
            openingMessage = "Merhaba.",
            scenario = "Oda."
        )
        assertTrue(minimal.scorePercent < 60)
        assertEquals("🌱 Temel Taslak", minimal.badge)
    }
}
