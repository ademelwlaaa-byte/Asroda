package com.example

import com.example.book.BookSecretProgressHelper
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.util.BotPromptBudgetEstimator
import com.example.util.ChapterSummaryExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V86MegaBatchUpgradeTest {

    @Test
    fun botPromptBudgetEstimator_estimatesTokenLoadAndTier() {
        val lightBot = BotEntity(
            id = "b1",
            mode = "personal",
            universeName = "",
            keyCharactersJson = "[]",
            aiName = "Aiden",
            aiPersonality = "Sakin ve kararlı bir yol arkadaşı.",
            userCharName = "Sen",
            userCharDesc = "Gezgin",
            scenario = "Yağmurlu bir akşam.",
            openingMessage = "Hoş geldin.",
            writingStyle = "Edebi",
            intensity = "Medium"
        )
        val report = BotPromptBudgetEstimator.estimate(lightBot)
        assertEquals("Hafif", report.tierLabel)
        assertTrue(BotPromptBudgetEstimator.formatCompact(report).contains("Sistem Yükü:"))
    }

    @Test
    fun bookSecretProgressHelper_sortsTurkishSecretsAndFormatsSummary() {
        val sorted = BookSecretProgressHelper.sortedSecrets(setOf("Şifre", "Çekirdek", "Buzul", "Şifre"))
        assertEquals(listOf("Buzul", "Çekirdek", "Şifre"), sorted)
        assertEquals("3 gizli sır keşfedildi", BookSecretProgressHelper.formatSummary(setOf("Şifre", "Çekirdek", "Buzul")))
        assertEquals("Henüz gizli sır keşfedilmedi", BookSecretProgressHelper.formatSummary(emptySet()))
    }

    @Test
    fun chapterSummaryExporter_includesDepthTierAndProseStyle() {
        val recap = ChapterSummaryExporter.formatRecapMarkdown(
            title = "Aiden",
            userName = "Kaan",
            messages = listOf(
                MessageEntity(id = "1", botId = "b1", role = "assistant", text = "Kulede fırtına koparken fırtına daha da şiddetlendi.", timestamp = 1L)
            )
        )
        assertTrue(recap.contains("**Seviye:**"))
        assertTrue(recap.contains("**Edebi Üslup:**"))
        assertTrue(recap.contains("#fırtına"))
    }
}
