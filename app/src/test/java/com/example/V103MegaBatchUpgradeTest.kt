package com.example

import com.example.data.managers.ProviderHealthState
import com.example.util.ApiDiagnosticsFormatter
import com.example.util.ProviderRollingLatencyTracker
import com.example.util.StoryReadingEstimateRangeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class V103MegaBatchUpgradeTest {

    @Before
    fun setup() {
        ProviderRollingLatencyTracker.clearAll()
    }

    @Test
    fun storyReadingEstimateRangeHelper_formatsZeroShortAndLongCounts() {
        assertEquals("< 1 dk", StoryReadingEstimateRangeHelper.formatRange(0))
        assertEquals("~1 dk", StoryReadingEstimateRangeHelper.formatRange(120))
        assertEquals("~4–6 dk", StoryReadingEstimateRangeHelper.formatRange(900))
    }

    @Test
    fun apiDiagnosticsFormatter_includesRollingMedianWhenAvailable() {
        ProviderRollingLatencyTracker.recordSample("gemini", 200L)
        ProviderRollingLatencyTracker.recordSample("gemini", 350L)
        ProviderRollingLatencyTracker.recordSample("gemini", 500L)
        val report = ApiDiagnosticsFormatter.formatReport(
            appVersion = "10.3",
            healthMap = mapOf("gemini" to ProviderHealthState(providerKey = "gemini", lastLatencyMs = 500L)),
            logs = emptyList()
        )
        assertTrue(report.contains("medyan 350ms"))
    }
}
