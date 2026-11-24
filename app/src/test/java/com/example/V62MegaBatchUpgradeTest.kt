package com.example

import com.example.data.local.MessageEntity
import com.example.util.ApiKeyFormatValidator
import com.example.util.StoryPacingAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V62MegaBatchUpgradeTest {

    @Test
    fun apiKeyFormatValidator_sanitizesBearerPrefixQuotesAndNewlines() {
        assertEquals("sk-test-1234567890", ApiKeyFormatValidator.sanitizeKey("  Bearer sk-test-1234567890\r\n"))
        assertEquals("sk-abc12345", ApiKeyFormatValidator.sanitizeKey("\"sk-abc12345\""))
        assertEquals("gsk_99999999", ApiKeyFormatValidator.sanitizeKey("  bearer   gsk_99999999  "))

        assertNotNull(ApiKeyFormatValidator.detectWarning("https://api.openai.com/v1"))
        assertNotNull(ApiKeyFormatValidator.detectWarning("short"))
        assertNull(ApiKeyFormatValidator.detectWarning("Bearer sk-valid-key-987654321"))
    }

    @Test
    fun storyPacingAnalyzer_detectsDialogueVsNarrationAndAverageLength() {
        val dialogueHeavy = listOf(
            MessageEntity(id = "1", botId = "b1", role = "assistant", text = "\"Hemen yola çıkmalıyız!\" dedi. \"Zamanımız daralıyor, herkes hazır mı?\"", timestamp = 1L),
            MessageEntity(id = "2", botId = "b1", role = "user", text = "\"Hazırım, kapıyı aç!\"", timestamp = 2L)
        )
        val summary = StoryPacingAnalyzer.analyze(dialogueHeavy)
        assertEquals("Diyalog Odaklı", summary.styleLabel)
        assertTrue(summary.dialoguePercent >= 55)
        assertTrue(summary.averageAssistantWords >= 8)

        val formatted = StoryPacingAnalyzer.formatCompact(summary)
        assertTrue(formatted.contains("Diyalog Odaklı"))
        assertTrue(formatted.contains("kelime"))
    }
}
