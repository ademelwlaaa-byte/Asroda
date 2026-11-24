package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.ChatDraftStore
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
class V40ChatDraftStoreTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun saveAndLoadDraft_isolatesDraftsPerBotAndClearsOnBlank() {
        val botA = "bot_a_${System.nanoTime()}"
        val botB = "bot_b_${System.nanoTime()}"
        try {
            ChatDraftStore.saveDraft(context, botA, "Karanlık koridorda sessizce ilerledim...")
            ChatDraftStore.saveDraft(context, botB, "Natasha'ya telsizden seslendim.")

            assertEquals("Karanlık koridorda sessizce ilerledim...", ChatDraftStore.loadDraft(context, botA))
            assertEquals("Natasha'ya telsizden seslendim.", ChatDraftStore.loadDraft(context, botB))
            assertTrue(ChatDraftStore.hasDraft(context, botA))

            // Saving blank string clears draft cleanly
            ChatDraftStore.saveDraft(context, botA, "   ")
            assertEquals("", ChatDraftStore.loadDraft(context, botA))
            assertFalse(ChatDraftStore.hasDraft(context, botA))
            // Bot B remains untouched
            assertEquals("Natasha'ya telsizden seslendim.", ChatDraftStore.loadDraft(context, botB))
        } finally {
            ChatDraftStore.clearDraft(context, botA)
            ChatDraftStore.clearDraft(context, botB)
        }
    }

    @Test
    fun saveDraft_capsOversizedDraftsSafely() {
        val botId = "bot_cap_${System.nanoTime()}"
        try {
            val huge = "A".repeat(ChatDraftStore.MAX_DRAFT_CHARS + 500)
            ChatDraftStore.saveDraft(context, botId, huge)
            val loaded = ChatDraftStore.loadDraft(context, botId)
            assertEquals(ChatDraftStore.MAX_DRAFT_CHARS, loaded.length)
        } finally {
            ChatDraftStore.clearDraft(context, botId)
        }
    }
}
