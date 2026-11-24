package com.example

import com.example.util.StoryDirectorSummaryHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V74MegaBatchUpgradeTest {

    @Test
    fun storyDirectorSummaryHelper_formatsActiveConfigurationCleanly() {
        val withRule = StoryDirectorSummaryHelper.formatActiveConfig(
            responseLength = "novel",
            ttsSpeed = 1.15f,
            fontScale = 1.1f,
            hasDirectorRule = true
        )
        assertTrue(withRule.contains("Uzunluk: Roman"))
        assertTrue(withRule.contains("Ses: 1.15x"))
        assertTrue(withRule.contains("Yazı: %110"))
        assertTrue(withRule.contains("Kural Aktif"))

        val noRule = StoryDirectorSummaryHelper.formatActiveConfig(
            responseLength = "short",
            ttsSpeed = 1.0f,
            fontScale = 0.9f,
            hasDirectorRule = false
        )
        assertTrue(noRule.contains("Uzunluk: Kısa"))
        assertFalse(noRule.contains("Kural Aktif"))
    }
}
