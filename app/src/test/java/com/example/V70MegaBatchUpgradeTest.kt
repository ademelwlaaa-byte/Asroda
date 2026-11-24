package com.example

import com.example.util.BackupFileSizeFormatter
import com.example.util.StoryDepthTierClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V70MegaBatchUpgradeTest {

    @Test
    fun storyDepthTierClassifier_scalesAcrossAllFiveLiteraryTiers() {
        assertEquals("🌱 Yeni Başlangıç", StoryDepthTierClassifier.formatLabel(120))
        assertEquals("✨ Gelişen Hikaye", StoryDepthTierClassifier.formatLabel(1200))
        assertEquals("📖 Uzun Bölüm", StoryDepthTierClassifier.formatLabel(4500))
        assertEquals("📚 Epik Roman", StoryDepthTierClassifier.formatLabel(15_000))
        assertEquals("👑 Efsanevi Destan", StoryDepthTierClassifier.formatLabel(30_000))
    }

    @Test
    fun backupFileSizeFormatter_formatsBytesKbMbAndGbCleanly() {
        assertEquals("0 B", BackupFileSizeFormatter.formatBytes(-5L))
        assertEquals("512 B", BackupFileSizeFormatter.formatBytes(512L))
        assertTrue(BackupFileSizeFormatter.formatBytes(2048L).endsWith("KB"))
        assertTrue(BackupFileSizeFormatter.formatBytes(5L * 1024L * 1024L).endsWith("MB"))
        assertTrue(BackupFileSizeFormatter.formatBytes(2L * 1024L * 1024L * 1024L).endsWith("GB"))
    }
}
