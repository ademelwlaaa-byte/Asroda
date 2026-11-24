package com.example

import com.example.book.BookBondTierClassifier
import com.example.util.CustomApiModelSorter
import org.junit.Assert.assertEquals
import org.junit.Test

class V105MegaBatchUpgradeTest {

    @Test
    fun customApiModelSorter_surfacesFreeAndFlagshipModelsFirst() {
        val unsorted = listOf(
            "zzz-unknown-model",
            "deepseek/deepseek-chat:free",
            "acme/basic-1b"
        )
        val sorted = CustomApiModelSorter.sortForPicker(unsorted)
        assertEquals("deepseek/deepseek-chat:free", sorted.first())
    }

    @Test
    fun bookBondTierClassifier_mapsFullBondSpectrum() {
        assertEquals("💫 Sarsılmaz Bağ", BookBondTierClassifier.classify(85))
        assertEquals("✦ Güvenilir Müttefik", BookBondTierClassifier.classify(55))
        assertEquals("🤝 Yakın Dost", BookBondTierClassifier.classify(20))
        assertEquals("• Nötr", BookBondTierClassifier.classify(0))
        assertEquals("⚡ Gergin", BookBondTierClassifier.classify(-25))
        assertEquals("🔥 Düşmanca", BookBondTierClassifier.classify(-60))
    }
}
