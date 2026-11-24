package com.example

import com.example.book.BookReadState
import com.example.book.BookSessionResumeHelper
import com.example.util.ProviderLatencyBadgeFormatter
import com.example.util.StoryExportFilenameHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V67MegaBatchUpgradeTest {

    @Test
    fun providerLatencyBadgeFormatter_classifiesSpeedTiersCleanly() {
        assertEquals("—", ProviderLatencyBadgeFormatter.formatBadge(0L))
        assertTrue(ProviderLatencyBadgeFormatter.formatBadge(320L).startsWith("⚡ Çok Hızlı"))
        assertTrue(ProviderLatencyBadgeFormatter.formatBadge(1200L).startsWith("✓ İyi"))
        assertTrue(ProviderLatencyBadgeFormatter.formatBadge(3100L).startsWith("⏳ Yavaş"))
        assertTrue(ProviderLatencyBadgeFormatter.formatBadge(5500L).startsWith("🐢 Çok Yavaş"))
    }

    @Test
    fun storyExportFilenameHelper_buildsCleanSlugFromTurkishTitle() {
        val name = StoryExportFilenameHelper.buildMarkdownFilename(
            title = "Işık Büyücüsü: Soğuk Kule!",
            nowMs = 1_700_000_000_000L
        )
        assertTrue(name.startsWith("isik_buyucusu_soguk_kule_asroda_"))
        assertTrue(name.endsWith(".md"))

        val fallback = StoryExportFilenameHelper.buildMarkdownFilename("!!!", nowMs = 1_700_000_000_000L)
        assertTrue(fallback.startsWith("asroda_hikaye_asroda_"))
    }

    @Test
    fun bookSessionResumeHelper_formatsNewInProgressAndCompletedStates() {
        val base = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 1,
            completedChapters = emptySet(),
            currentNodeId = "ch1_n1",
            transcript = emptyList(),
            chapterDecisions = emptyMap(),
            bonds = emptyMap(),
            fatigue = 0,
            mystery = 0,
            flags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet(),
            isChapterCompleted = false
        )
        assertTrue(BookSessionResumeHelper.buildResumeSubtitle(base, 10).contains("Yeni yolculuk"))

        val mid = base.copy(activeChapter = 4, completedChapters = setOf(1, 2, 3))
        val midText = BookSessionResumeHelper.buildResumeSubtitle(mid, 10)
        assertTrue(midText.contains("Bölüm 4"))
        assertTrue(midText.contains("%30"))
        assertTrue(midText.contains("7 bölüm kaldı"))

        val done = base.copy(activeChapter = 10, completedChapters = (1..10).toSet())
        assertTrue(BookSessionResumeHelper.buildResumeSubtitle(done, 10).contains("Tamamlandı (%100)"))
    }
}
