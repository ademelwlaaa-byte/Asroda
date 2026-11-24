package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.BackupHealthChecker
import com.example.util.BotDirectorNoteStore
import com.example.util.RepetitionDetector
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
class V54MegaBatchUpgradeTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun botDirectorNoteStore_persistsAndClearsPerBotRule() {
        val botId = "bot_dir_${System.nanoTime()}"
        try {
            BotDirectorNoteStore.saveRule(context, botId, "Asla benim yerime konuşma.")
            assertEquals("Asla benim yerime konuşma.", BotDirectorNoteStore.loadRule(context, botId))
            BotDirectorNoteStore.clearRule(context, botId)
            assertEquals("", BotDirectorNoteStore.loadRule(context, botId))
        } finally {
            BotDirectorNoteStore.clearRule(context, botId)
        }
    }

    @Test
    fun repetitionDetector_flagsRepeatedTurkishPhrasesAcrossTurns() {
        val replies = listOf(
            "Natasha derin bir nefes aldı ve pencereden dışarı baktı.",
            "Sessizlik sürerken derin bir nefes aldı ve silahını kontrol etti.",
            "Kulede rüzgar eserken derin bir nefes aldı."
        )
        val loops = RepetitionDetector.detectLoopingPhrases(replies)
        assertTrue("Should detect repeated 4-gram 'derin bir nefes aldı'", loops.contains("derin bir nefes aldı"))
    }

    @Test
    fun backupHealthChecker_parsesValidBackupAndRejectsCorruptJson() {
        val json = """
            {"version":2,"bots":[{"id":"1"},{"id":"2"}],"messages":[{"id":"m1"}],"customProviders":[{"id":1}],"settings":{"geminiApiKey":"AIzaTest"}}
        """.trimIndent()
        val p = BackupHealthChecker.inspectJson(json)
        assertTrue(p.isValidJson)
        assertEquals(2, p.schemaVersion)
        assertEquals(2, p.botCount)
        assertEquals(1, p.messageCount)
        assertEquals(1, p.customProviderCount)
        assertTrue(p.includesPlaintextKeys)

        val bad = BackupHealthChecker.inspectJson("{corrupt")
        assertFalse(bad.isValidJson)
    }
}
