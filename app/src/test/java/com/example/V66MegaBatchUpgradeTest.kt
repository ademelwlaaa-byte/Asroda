package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.MarkdownEmphasisCleaner
import com.example.util.ResponseSanitizer
import com.example.util.StorySessionGoalStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class V66MegaBatchUpgradeTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun storySessionGoalStore_tracksTurnsGoalsAndDayRollover() {
        val day1 = 1_700_000_000_000L
        val day2 = day1 + 86_400_000L

        StorySessionGoalStore.setDailyGoal(context, 10, day1)
        val p1 = StorySessionGoalStore.recordTurn(context, day1)
        assertEquals(1, p1.sentToday)
        assertEquals(10, p1.dailyGoal)
        assertEquals(10, p1.percentComplete)
        assertFalse(p1.goalReached)

        repeat(9) { StorySessionGoalStore.recordTurn(context, day1) }
        val p10 = StorySessionGoalStore.getTodayProgress(context, day1)
        assertTrue(p10.goalReached)
        assertEquals(100, p10.percentComplete)

        val nextDay = StorySessionGoalStore.getTodayProgress(context, day2)
        assertEquals(0, nextDay.sentToday)
        assertEquals(15, StorySessionGoalStore.nextGoalPreset(10))
    }

    @Test
    fun markdownEmphasisCleaner_fixesDoubleQuotesPaddedItalicsAndDanglingAsterisk() {
        val raw = "Assistant: * sessizce bakar * \"\"Kapıyı aç!\"\" Karanlık çöktü*"
        val cleaned = ResponseSanitizer.sanitizeForDisplay(raw)
        assertTrue(cleaned.contains("*sessizce bakar*"))
        assertTrue(cleaned.contains("\"Kapıyı aç!\""))
        assertFalse(cleaned.endsWith("*"))
        assertEquals("Normal *italik* metin", MarkdownEmphasisCleaner.clean("Normal *italik* metin"))
    }
}
