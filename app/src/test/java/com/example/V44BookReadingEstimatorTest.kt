package com.example

import com.example.book.BookReadingEstimator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V44BookReadingEstimatorTest {

    @Test
    fun estimateRemainingMinutes_decreasesAsScenesProgressAndReturnsZeroOnCompletion() {
        val startMin = BookReadingEstimator.estimateRemainingMinutes(
            currentSceneOrder = 1,
            totalScenes = 6,
            isChapterCompleted = false
        )
        val midMin = BookReadingEstimator.estimateRemainingMinutes(
            currentSceneOrder = 4,
            totalScenes = 6,
            isChapterCompleted = false
        )
        val endMin = BookReadingEstimator.estimateRemainingMinutes(
            currentSceneOrder = 6,
            totalScenes = 6,
            isChapterCompleted = true
        )

        assertTrue("Start estimate should be greater than mid estimate", startMin > midMin)
        assertTrue("Mid estimate should be at least 1 minute", midMin >= 1)
        assertEquals(0, endMin)
        assertEquals("Bitti", BookReadingEstimator.formatBadgeText(endMin))
        assertEquals("~${midMin} dk", BookReadingEstimator.formatBadgeText(midMin))
    }
}
