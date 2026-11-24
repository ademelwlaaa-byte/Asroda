package com.example

import com.example.data.local.MessageEntity
import com.example.data.repository.ProviderFallbackLogEntry
import com.example.util.ProviderErrorCategoryClassifier
import com.example.util.StoryDialogueToNarrationRatioHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V110MegaBatchUpgradeTest {

    @Test
    fun dialogueToNarrationRatio_distinguishesDialogueHeavyAndDescriptiveStories() {
        val dialogueHeavy = listOf(
            MessageEntity(
                id = "m1",
                botId = "b1",
                role = "assistant",
                text = "\"Burada bekle,\" dedi. \"Hemen geri döneceğim, sakın kapıyı açma ve kimseye güvenme.\""
            )
        )
        val dRes = StoryDialogueToNarrationRatioHelper.analyze(dialogueHeavy)
        assertTrue(dRes.dialoguePercent >= 50)
        assertEquals("🎭 Yoğun Diyalog", dRes.styleLabel)
        assertTrue(StoryDialogueToNarrationRatioHelper.formatCompact(dRes).contains("%${dRes.dialoguePercent} diyalog"))

        val descriptive = listOf(
            MessageEntity(
                id = "m2",
                botId = "b1",
                role = "assistant",
                text = "Yağmur İstanbul sokaklarını yıkarken sis kulelerin etrafında ağır ağır dönüyordu. Uzaktan gemi düdükleri duyuluyordu."
            )
        )
        val nRes = StoryDialogueToNarrationRatioHelper.analyze(descriptive)
        assertEquals(0, nRes.dialoguePercent)
        assertEquals(100, nRes.narrationPercent)
        assertEquals("📖 Betimleyici Anlatı", nRes.styleLabel)
    }

    @Test
    fun providerErrorCategoryClassifier_classifiesCodesAndTalliesDominantCause() {
        assertEquals(
            ProviderErrorCategoryClassifier.Category.AUTH,
            ProviderErrorCategoryClassifier.classify(401, "Invalid API key")
        )
        assertEquals(
            ProviderErrorCategoryClassifier.Category.RATE_LIMIT,
            ProviderErrorCategoryClassifier.classify(429, "Too many requests")
        )
        assertEquals(
            ProviderErrorCategoryClassifier.Category.TIMEOUT,
            ProviderErrorCategoryClassifier.classify(null, "SocketTimeoutException: timed out")
        )

        val logs = listOf(
            ProviderFallbackLogEntry(providerName = "Groq", status = "FAILED", statusCode = 429, errorMessage = "Rate limit reached", layer = 1),
            ProviderFallbackLogEntry(providerName = "OpenRouter", status = "FAILED", statusCode = 429, errorMessage = "Too many requests", layer = 2),
            ProviderFallbackLogEntry(providerName = "Cerebras", status = "FAILED", statusCode = 503, errorMessage = "Service unavailable", layer = 2),
            ProviderFallbackLogEntry(providerName = "Gemini", status = "SUCCESS", statusCode = 200, errorMessage = "", layer = 3)
        )
        val summary = ProviderErrorCategoryClassifier.summarizeDominantCategory(logs)
        assertEquals("Baskın Hata: ⏳ Hız Limiti (2/3)", summary)
    }
}
