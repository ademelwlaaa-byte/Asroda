package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.book.BookCharacterId
import com.example.book.CharacterFlagAxis
import com.example.book.FlagChangeEntry
import com.example.book.StoryFlagAxis
import com.example.data.local.AppDatabase
import com.example.data.local.PromptViolationLogEntity
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DatabaseAndBookModelsOverhaulTest {

    @Test
    fun appDatabase_migrationChainIsGaplessAndMatchesCurrentSchemaVersion() {
        assertEquals(50, AppDatabase.CURRENT_SCHEMA_VERSION)
        assertEquals(49, AppDatabase.ALL_MIGRATIONS.size)
        assertTrue(AppDatabase.validateMigrationChain())
        assertEquals(1, AppDatabase.ALL_MIGRATIONS.first().startVersion)
        assertEquals(50, AppDatabase.ALL_MIGRATIONS.last().endVersion)
    }

    @Test
    fun appDatabase_pruneDiagnosticLogsAndWalCheckpointAreSafeAndAccurate() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val dao = db.promptViolationLogDao()
            repeat(65) { idx ->
                dao.insertLog(
                    PromptViolationLogEntity(
                        botId = "bot_1",
                        messageText = "msg_$idx",
                        violationType = "test",
                        timestamp = 1_000L + idx
                    )
                )
            }
            assertEquals(65, dao.getLogsForBot("bot_1").size)

            val prunedTables = AppDatabase.pruneDiagnosticLogs(db, maxRowsPerTable = 50)
            assertEquals(AppDatabase.DIAGNOSTIC_LOG_TABLES.size, prunedTables)
            assertEquals(50, dao.getLogsForBot("bot_1").size)

            assertTrue(AppDatabase.checkpointWalPassive(db))
        } finally {
            db.close()
        }
    }

    @Test
    fun bookModels_fromKeyIsTurkishLocaleSafeAndZeroDeltaFormatsCleanly() {
        val prevLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))

            // Uppercase 'I' keys would fail with bare .lowercase() under tr-TR ('I' -> 'ı')
            assertEquals(BookCharacterId.VISION, BookCharacterId.fromKey("  VISION  "))
            assertEquals(BookCharacterId.PIETRO, BookCharacterId.fromKey("PIETRO"))
            assertEquals(BookCharacterId.MIRA, BookCharacterId.fromKey("MIRA"))
            assertNull(BookCharacterId.fromKey("unknown_hero"))

            assertEquals(CharacterFlagAxis.CURIOSITY, CharacterFlagAxis.fromKey("CURIOSITY"))
            assertEquals(CharacterFlagAxis.SUSPICION, CharacterFlagAxis.fromKey(" suspicion "))
            assertEquals(CharacterFlagAxis.AFFECTION, CharacterFlagAxis.fromKey("AFFECTION"))
            assertNull(CharacterFlagAxis.fromKey(""))

            assertEquals(StoryFlagAxis.SHIELD_SUSPICION, StoryFlagAxis.fromKey("SHIELD_SUSPICION"))
            assertEquals(StoryFlagAxis.FIELD_INITIATIVE, StoryFlagAxis.fromKey("FIELD_INITIATIVE"))
            assertEquals(StoryFlagAxis.WHITE_EYE_CONTROL, StoryFlagAxis.fromKey("WHITE_EYE_CONTROL"))
            assertNull(StoryFlagAxis.fromKey("nonexistent"))

            val pos = FlagChangeEntry("k", "Steve'in güveni", 3, 0xFF60A5FAL)
            val neg = FlagChangeEntry("k", "Natasha'nın şüphesi", -2, 0xFFFCA5A5L)
            val zero = FlagChangeEntry("k", "Tony'nin merakı", 0, 0xFFFBBF24L)
            assertEquals("Steve'in güveni +3 arttı", pos.formatTurkish())
            assertEquals("Natasha'nın şüphesi -2 azaldı", neg.formatTurkish())
            assertEquals("Tony'nin merakı değişmedi", zero.formatTurkish())
            assertFalse(zero.formatTurkish().contains("+0"))
        } finally {
            Locale.setDefault(prevLocale)
        }
    }
}
