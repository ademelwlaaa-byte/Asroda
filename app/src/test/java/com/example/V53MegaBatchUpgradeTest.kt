package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.book.BookAudioPreferences
import com.example.data.local.MessageEntity
import com.example.util.ChatSearchHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class V53MegaBatchUpgradeTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun bookAudioPreferences_clampsAndPersistsVolume() {
        assertEquals(BookAudioPreferences.MIN_VOLUME, BookAudioPreferences.saveMusicVolume(context, 0.01f), 0.001f)
        assertEquals(BookAudioPreferences.MAX_VOLUME, BookAudioPreferences.saveMusicVolume(context, 2.5f), 0.001f)
        assertEquals(0.65f, BookAudioPreferences.saveMusicVolume(context, 0.65f), 0.001f)
        assertEquals(0.65f, BookAudioPreferences.loadMusicVolume(context), 0.001f)
    }

    @Test
    fun chatSearchHelper_matchesTurkishCaseInsensitiveAndExtractsSnippets() {
        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "assistant", text = "İstanbul'un sisli gecesinde ışıklar söndü.", timestamp = 1L),
            MessageEntity(id = "2", botId = "b1", role = "user", text = "İSTANBUL sokaklarında yürüdüm, istanbul beni çağırıyordu.", timestamp = 2L),
            MessageEntity(id = "3", botId = "b1", role = "system", text = "İstanbul system hidden", timestamp = 3L)
        )
        val matches = ChatSearchHelper.findMatches(msgs, "istanbul")
        assertEquals(2, matches.size)
        assertEquals("1", matches[0].messageId)
        assertEquals(1, matches[0].matchCountInMessage)
        assertEquals("2", matches[1].messageId)
        assertEquals(2, matches[1].matchCountInMessage)
        assertTrue(matches[0].snippet.contains("İstanbul"))
    }
}
