package com.example

import com.example.util.AppUpdateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun compareVersions_handlesSemanticAndPrefixCases() {
        assertTrue(AppUpdateManager.compareVersions("v12.0", "11.9") > 0)
        assertTrue(AppUpdateManager.compareVersions("11.10", "11.9") > 0)
        assertTrue(AppUpdateManager.compareVersions("12.0.1", "12.0") > 0)
        assertEquals(0, AppUpdateManager.compareVersions("v12.0", "12.0"))
        assertTrue(AppUpdateManager.compareVersions("11.8", "12.0") < 0)
    }

    @Test
    fun formatSizeMb_formatsReadableSize() {
        assertEquals("18.5 MB", AppUpdateManager.formatSizeMb((18.5 * 1024 * 1024).toLong()))
        assertEquals("", AppUpdateManager.formatSizeMb(0L))
    }
}
