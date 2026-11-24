package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.ReadingStreakTracker
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class V57MegaBatchUpgradeTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun readingStreakTracker_incrementsOnConsecutiveDaysAndResetsOnGap() {
        val day1 = 1_700_000_000_000L
        val day2 = day1 + 86_400_000L
        val day5 = day1 + 4L * 86_400_000L

        val s1 = ReadingStreakTracker.recordActivity(context, day1)
        assertEquals(1, s1.currentStreakDays)
        assertEquals(1, s1.longestStreakDays)

        // Same day repeat should not double-count
        val s1Again = ReadingStreakTracker.recordActivity(context, day1 + 3_600_000L)
        assertEquals(1, s1Again.currentStreakDays)

        // Consecutive day increments streak
        val s2 = ReadingStreakTracker.recordActivity(context, day2)
        assertEquals(2, s2.currentStreakDays)
        assertEquals(2, s2.longestStreakDays)

        // Gap resets current streak to 1 while preserving longestStreakDays
        val s5 = ReadingStreakTracker.recordActivity(context, day5)
        assertEquals(1, s5.currentStreakDays)
        assertEquals(2, s5.longestStreakDays)
    }
}
