package com.example

import com.example.util.QuickReplyChipsHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V58MegaBatchUpgradeTest {

    @Test
    fun quickReplyChipsHelper_adaptsSuggestionsToSceneTone() {
        val qChips = QuickReplyChipsHelper.suggestStarters("Bana gerçekten güveniyor musun?")
        assertEquals(3, qChips.size)
        assertTrue(qChips.any { it.contains("Gerçeği") })

        val actionChips = QuickReplyChipsHelper.suggestStarters("Kırmızı alarm çaldı, silah sesleri yaklaşıyor!")
        assertEquals(3, actionChips.size)
        assertTrue(actionChips.any { it.contains("siper") })

        val romChips = QuickReplyChipsHelper.suggestStarters("Yavaşça elini tutup kulağına fısıldadı.")
        assertEquals(3, romChips.size)
        assertTrue(romChips.any { it.contains("elini") || it.contains("yaklaşıp") })
    }
}
