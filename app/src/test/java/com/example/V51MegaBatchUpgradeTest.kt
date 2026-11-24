package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BotEntity
import com.example.util.BotListSortOrder
import com.example.util.BotListSorter
import com.example.util.BotScratchpadStore
import com.example.util.ResponseSanitizer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class V51MegaBatchUpgradeTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun responseSanitizer_stripsThinkBlocksStrayPrefixesAndExcessBlankLines() {
        val raw = "<think>internal chain of thought</think>\nAssistant:  *Kapı açıldı.*\n\n\n\n\"Hoş geldin.\""
        val cleaned = ResponseSanitizer.sanitizeForDisplay(raw)
        assertEquals("*Kapı açıldı.*\n\n\"Hoş geldin.\"", cleaned)
    }

    @Test
    fun botScratchpadStore_savesLoadsAndClearsPerBotNotes() {
        val botId = "bot_pad_${System.nanoTime()}"
        try {
            BotScratchpadStore.saveNote(context, botId, "Kule anahtarı: 3. katta gizli.")
            assertEquals("Kule anahtarı: 3. katta gizli.", BotScratchpadStore.loadNote(context, botId))
            BotScratchpadStore.clearNote(context, botId)
            assertEquals("", BotScratchpadStore.loadNote(context, botId))
        } finally {
            BotScratchpadStore.clearNote(context, botId)
        }
    }

    @Test
    fun botListSorter_sortsByNameFavoritesAndDraftsCorrectly() {
        fun testBot(id: String, name: String) = BotEntity(
            id = id,
            mode = "personal",
            universeName = "",
            keyCharactersJson = "[]",
            aiName = name,
            aiPersonality = "",
            userCharName = "A",
            userCharDesc = "",
            scenario = "",
            openingMessage = "",
            writingStyle = "literary",
            intensity = "medium"
        )
        val bots = listOf(testBot("1", "Zara"), testBot("2", "Aiden"), testBot("3", "Mira"))

        val az = BotListSorter.sortBots(bots, BotListSortOrder.NAME_AZ, emptySet(), emptySet())
        assertEquals(listOf("2", "3", "1"), az.map { it.id })

        val fav = BotListSorter.sortBots(bots, BotListSortOrder.FAVORITES_FIRST, setOf("1"), emptySet())
        assertEquals("1", fav.first().id)

        val drafts = BotListSorter.sortBots(bots, BotListSortOrder.DRAFTS_FIRST, emptySet(), setOf("3"))
        assertEquals("3", drafts.first().id)
    }
}
