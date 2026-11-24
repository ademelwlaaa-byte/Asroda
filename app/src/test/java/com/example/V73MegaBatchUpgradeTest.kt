package com.example

import com.example.book.BookCharacterId
import com.example.book.BookReadState
import com.example.book.StoryCompletionCertificateHelper
import com.example.util.ProviderModelTagClassifier
import org.junit.Assert.assertTrue
import org.junit.Test

class V73MegaBatchUpgradeTest {

    @Test
    fun providerModelTagClassifier_identifiesFreeReasoningFastAndPowerfulModels() {
        val r1Free = ProviderModelTagClassifier.classifyTags("deepseek/deepseek-r1:free")
        assertTrue(r1Free.contains("🆓 Ücretsiz"))
        assertTrue(r1Free.contains("🧠 Muhakeme"))

        val flash = ProviderModelTagClassifier.classifyTags("gemini-2.5-flash")
        assertTrue(flash.contains("⚡ Hızlı"))
        assertTrue(flash.contains("📖 Uzun Bağlam"))

        val llama70 = ProviderModelTagClassifier.classifyTags("llama-3.3-70b-versatile")
        assertTrue(llama70.contains("✦ Güçlü"))
    }

    @Test
    fun storyCompletionCertificateHelper_generatesTurkishCertificateWithBondsAndProgress() {
        val state = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 10,
            completedChapters = (1..10).toSet(),
            currentNodeId = "ch10_end",
            transcript = emptyList(),
            chapterDecisions = emptyMap(),
            bonds = mapOf(BookCharacterId.WANDA to 88),
            fatigue = 0,
            mystery = 0,
            flags = emptyMap(),
            discoveredSecrets = setOf("Sır 1"),
            discoveredBranches = emptySet(),
            isChapterCompleted = true
        )
        val cert = StoryCompletionCertificateHelper.buildCertificate(state, totalChapters = 10, nowMs = 1_700_000_000_000L)
        assertTrue(cert.contains("KOZMİK SÜRÜKLENİŞ"))
        assertTrue(cert.contains("%100"))
        assertTrue(cert.contains("+88"))
        assertTrue(cert.contains("1 sır"))
    }
}
