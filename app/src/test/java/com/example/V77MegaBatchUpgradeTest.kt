package com.example

import com.example.data.managers.ProviderHealthState
import com.example.data.repository.ProviderFallbackLogEntry
import com.example.util.ApiDiagnosticsFormatter
import com.example.util.ReadingMilestoneBadges
import com.example.util.ReadingStreakTracker
import com.example.util.StorySessionGoalStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V77MegaBatchUpgradeTest {

    @Test
    fun apiDiagnosticsFormatter_includesHealthSummaryAndSuccessRate() {
        val report = ApiDiagnosticsFormatter.formatReport(
            appVersion = "7.7",
            healthMap = mapOf(
                "gemini" to ProviderHealthState(providerKey = "gemini", lastLatencyMs = 280L, totalSuccessCount = 5)
            ),
            logs = listOf(
                ProviderFallbackLogEntry(providerName = "gemini", status = "SUCCESS"),
                ProviderFallbackLogEntry(providerName = "groq", status = "FAILED", statusCode = 429)
            )
        )
        assertTrue(report.contains("Özet: Sağlıklı: 1"))
        assertTrue(report.contains("Başarı: Son 2 denemede %50 başarı"))
    }

    @Test
    fun readingMilestoneBadges_awardsExpectedRankFromStreakAndGoal() {
        val legend = ReadingMilestoneBadges.resolveBadge(
            ReadingStreakTracker.StreakState(currentStreakDays = 5, longestStreakDays = 32, totalActiveDays = 40, lastActiveDayKey = "2026-11-24")
        )
        assertEquals("Efsanevi Yazar", legend.title)

        val goalChamp = ReadingMilestoneBadges.resolveBadge(
            ReadingStreakTracker.StreakState(currentStreakDays = 2, longestStreakDays = 2, totalActiveDays = 2, lastActiveDayKey = "2026-11-24"),
            StorySessionGoalStore.DailyGoalProgress(sentToday = 15, dailyGoal = 15, percentComplete = 100, goalReached = true)
        )
        assertEquals("Günlük Hedef Şampiyonu", goalChamp.title)
        assertTrue(ReadingMilestoneBadges.formatCompact(goalChamp).contains("Günlük Hedef Şampiyonu"))
    }
}
