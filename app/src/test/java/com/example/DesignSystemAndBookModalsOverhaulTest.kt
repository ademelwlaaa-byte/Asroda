package com.example

import com.example.book.BookReadState
import com.example.book.CosmicDriftBookRegistry
import com.example.ui.components.formatNormalizedSecondaryEmotion
import com.example.ui.components.formatNormalizedTurkishMoodBadge
import com.example.ui.components.mapSliderFractionToValue
import com.example.ui.screens.PackDownloadSessionGuard
import com.example.ui.screens.buildFateTreeChapterNodes
import com.example.ui.screens.buildReaderChapterSwitcherItems
import com.example.ui.screens.cosmic3d.CosmicDuelBossId
import com.example.ui.screens.cosmic3d.StoryMiniGameId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignSystemAndBookModalsOverhaulTest {

    private fun sampleReadState(
        activeChapter: Int = 2,
        completedChapters: Set<Int> = setOf(1)
    ): BookReadState = BookReadState(
        bookId = CosmicDriftBookRegistry.BOOK_ID,
        activeChapter = activeChapter,
        completedChapters = completedChapters,
        currentNodeId = "ch2_start",
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

    // ── MorCamDesignSystem: Slider Fraction Mapping ──

    @Test
    fun mapSliderFractionToValue_clampsAndSnapsStepsAccurately() {
        assertEquals(0f, mapSliderFractionToValue(-0.5f, 0f, 100f, steps = 0), 0.001f)
        assertEquals(100f, mapSliderFractionToValue(1.5f, 0f, 100f, steps = 0), 0.001f)
        assertEquals(50f, mapSliderFractionToValue(0.5f, 0f, 100f, steps = 0), 0.001f)

        // With 3 intermediate steps -> 4 segments (0, 25, 50, 75, 100)
        assertEquals(25f, mapSliderFractionToValue(0.24f, 0f, 100f, steps = 3), 0.001f)
        assertEquals(75f, mapSliderFractionToValue(0.76f, 0f, 100f, steps = 3), 0.001f)
    }

    // ── EmotionStatusSection: Turkish Emotion Label Normalization ──

    @Test
    fun formatNormalizedTurkishMoodBadge_normalizesEnglishAndAppliesTurkishTitleCase() {
        val happy = formatNormalizedTurkishMoodBadge("happy")
        assertTrue("Expected Turkish label for 'happy', got '$happy'", happy.isNotBlank() && !happy.equals("happy", ignoreCase = true))
        assertTrue(happy.first().isUpperCase())

        val fallback = formatNormalizedTurkishMoodBadge("   ")
        assertEquals("Nötr", fallback)

        assertEquals("", formatNormalizedSecondaryEmotion("   "))
        assertTrue(formatNormalizedSecondaryEmotion("anxious").isNotBlank())
    }

    // ── BookReaderOverlays: Chapter Switcher Projection ──

    @Test
    fun buildReaderChapterSwitcherItems_marksCompletedActiveAndUnlockedCorrectly() {
        val state = sampleReadState(activeChapter = 3, completedChapters = setOf(1, 2))
        val items = buildReaderChapterSwitcherItems(state)

        assertEquals(CosmicDriftBookRegistry.chapters.size, items.size)
        val ch1 = items.first { it.chapter.number == 1 }
        val ch3 = items.first { it.chapter.number == 3 }
        val ch5 = items.first { it.chapter.number == 5 }

        assertTrue(ch1.isCompleted && ch1.isUnlocked && !ch1.isActive)
        assertTrue(ch3.isActive && ch3.isUnlocked && !ch3.isCompleted)
        assertFalse(ch5.isUnlocked)
    }

    // ── BookFateTreeModal: Fate Tree Node & Medal Projection ──

    @Test
    fun buildFateTreeChapterNodes_splitsSeasonsAndAttachesMedals() {
        val state = sampleReadState(activeChapter = 10, completedChapters = setOf(1, 2, 7, 9, 10))
        val miniGames = mapOf(
            StoryMiniGameId.WHITE_EYE_RESONANCE.key to "S",
            StoryMiniGameId.BOSPHORUS_CORE_BALANCE.key to "A"
        )
        val bossMedals = mapOf(
            CosmicDuelBossId.VAELOR_ROOFTOP.name to "S",
            CosmicDuelBossId.MIRROR_ZERO.name to "B"
        )

        val season1 = buildFateTreeChapterNodes(
            allChapters = CosmicDriftBookRegistry.chapters,
            seasonTab = 1,
            state = state,
            savedMiniGameRanks = miniGames,
            savedBossMedals = bossMedals
        )
        assertTrue(season1.all { it.chapter.number <= 10 })
        assertEquals("👁 S", season1.first { it.chapter.number == 2 }.medalLabel)
        assertEquals("🌀 A", season1.first { it.chapter.number == 7 }.medalLabel)
        assertEquals("⚔ S", season1.first { it.chapter.number == 10 }.medalLabel)
        assertTrue(season1.first { it.chapter.number == 10 }.isCurrent)

        val season2 = buildFateTreeChapterNodes(
            allChapters = CosmicDriftBookRegistry.chapters,
            seasonTab = 2,
            state = state,
            savedMiniGameRanks = miniGames,
            savedBossMedals = bossMedals
        )
        assertTrue(season2.all { it.chapter.number >= 11 })
        assertTrue(season2.first { it.chapter.number == 11 }.isUnlocked)
        assertEquals("⚔ B", season2.first { it.chapter.number == 13 }.medalLabel)
        assertFalse(season2.first { it.chapter.number == 13 }.isUnlocked)
    }

    // ── BookContentPacksModal: Atomic Single-Flight & Cancellation Guard ──

    @Test
    fun packDownloadSessionGuard_blocksDoubleTapAndSuppressesStaleCallbacksAfterCancel() {
        val guard = PackDownloadSessionGuard()
        assertFalse(guard.isDownloading())

        val token1 = guard.tryStart()
        assertNotNull(token1)
        assertTrue(guard.isDownloading())
        assertTrue(guard.isCurrent(token1!!))

        // Rapid second tap MUST be ignored
        assertNull(guard.tryStart())

        // Cancel invalidates token1 so stale onSuccess/onFailure callbacks are dropped
        guard.cancel()
        assertFalse(guard.isDownloading())
        assertFalse(guard.isCurrent(token1))
        assertFalse(guard.complete(token1))

        // User can start a fresh download after cancel and complete it cleanly
        val token2 = guard.tryStart()
        assertNotNull(token2)
        assertTrue(guard.isCurrent(token2!!))
        assertTrue(guard.complete(token2))
        assertFalse(guard.isDownloading())
        assertFalse(guard.complete(token2))
    }
}
