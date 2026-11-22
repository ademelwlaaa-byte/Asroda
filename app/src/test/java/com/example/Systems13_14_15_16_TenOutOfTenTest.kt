package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.CastDetectorEngine
import com.example.data.repository.EmochiRepository
import com.example.data.repository.KeyCharacter
import com.example.work.ProactiveMessageWorker
import com.example.work.RoutineNotificationWorker
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Systems13_14_15_16_TenOutOfTenTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: EmochiRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = EmochiRepository(db, context)
    }

    @After
    fun tearDown() {
        EmochiRepository.activeBotId = null
        db.close()
    }

    // ── System #13: CastDetector & CastMemberManager ──────────────────────────

    @Test
    fun system13_asciiApostropheStemmingTrailingWordBoundaryAndImportanceScale() = runBlocking {
        // 1. ASCII straight apostrophe ' must stem cleanly (previously broken!)
        assertEquals("Ayla", CastDetectorEngine.stemTurkishProperName("Ayla'nın"))
        assertEquals("Kerem", CastDetectorEngine.stemTurkishProperName("Kerem'e"))
        assertEquals("Zar'thul", CastDetectorEngine.stemTurkishProperName("Zar'thul"))

        // 2. Trailing Unicode word boundary prevents "Ali" matching "Alışveriş"
        assertFalse("'Ali' must NOT match 'Alışveriş'", CastDetectorEngine.isNameMentionedInText("Ali", "Bugün alışverişe çıktık."))
        assertTrue("'Ali' must match exact word", CastDetectorEngine.isNameMentionedInText("Ali", "Ali kapıyı açtı."))
        assertTrue("'Ali' must match apostrophe-inflected form", CastDetectorEngine.isNameMentionedInText("Ali", "Ali'nin çantası burada."))

        // 3. KeyCharacter mirrored as CastMember uses 0-100 scale (importanceScore == 90) so core cast passes >= 90 filter
        val botId = "bot_cast_10_10"
        db.botDao().insertOrUpdate(createBot(botId))
        repo.managers.castMemberManager.ensureKeyCharacterMirroredAsCastMember(
            botId = botId,
            keyChar = KeyCharacter(id = "k1", name = "Aylin", desc = "Eski dost ve sırdaş")
        )
        val cast = db.castMemberDao().getCastMembersForBot(botId)
        val aylin = cast.firstOrNull { it.name == "Aylin" }
        assertNotNull(aylin)
        assertEquals(90, aylin!!.importanceScore)
        assertTrue(aylin.personalityJson.isNotBlank())

        val filtered = CastDetectorEngine.filterActiveInSceneCastMembers(cast, "Bu sahnede hiç adı geçmiyor.")
        assertTrue("Core cast (importanceScore=90) must always pass active-scene filter", filtered.any { it.name == "Aylin" })

        // 4. Alias merging: "Doktor Kerem" + "Kerem Bey" -> single canonical "Kerem"
        repo.managers.castMemberManager.ensureSideCharacterRegistered(
            botId = botId,
            rawCharacterName = "Doktor Kerem",
            cueSentence = "Doktor Kerem dosyayı inceledi.",
            confidence = 85,
            pendingConfirmation = false
        )
        repo.managers.castMemberManager.ensureSideCharacterRegistered(
            botId = botId,
            rawCharacterName = "Kerem Bey",
            cueSentence = "Kerem Bey başını salladı.",
            confidence = 85,
            pendingConfirmation = false
        )
        val kerems = db.castMemberDao().getCastMembersForBot(botId).filter { it.name.contains("Kerem", ignoreCase = true) }
        assertEquals("Aliases 'Doktor Kerem' and 'Kerem Bey' must merge into a single row", 1, kerems.size)
        assertEquals(2, kerems.first().appearanceCount)

        // 5. Betrayal event MUST NOT flip unmentioned low-affection cast members
        db.castMemberDao().insertCastMember(
            CastMemberEntity(
                botId = botId,
                name = "Mert",
                description = "Uzak tanıdık",
                role = "Yan Karakter",
                affectionScore = 15,
                pendingUserConfirmation = false,
                lastRoleEvaluationIndex = 0
            )
        )
        repo.managers.castMemberManager.reEvaluateCastMemberRolesIfNeeded(
            botId = botId,
            currentMessageCount = 30,
            sceneContext = "Bilinmeyen bir düşman ihanet edip kaçtı."
        )
        val mertAfter = db.castMemberDao().findByName(botId, "Mert")!!
        assertEquals("Unmentioned low-affection member must NOT flip to enemy", "Yan Karakter", mertAfter.role)

        // 6. Exact month/day birthday age math (before birthday -> age - 1)
        db.castMemberDao().updateCastMember(mertAfter.copy(birthDate = "2000-12-15", currentAge = 20))
        repo.managers.castMemberManager.updateCastMembersAgeForBot(botId, "2026-11-21")
        assertEquals(25, db.castMemberDao().findByName(botId, "Mert")!!.currentAge)
        repo.managers.castMemberManager.updateCastMembersAgeForBot(botId, "2026-12-20")
        assertEquals(26, db.castMemberDao().findByName(botId, "Mert")!!.currentAge)
    }

    // ── System #14: Room DB & MessageDao & Repository saveMessage ─────────────

    @Test
    fun system14_recentMessagesQueryMessageByIdSaveMessageUpdatesTimestampAndLogPruning() = runBlocking {
        val botId = "bot_db_10_10"
        db.botDao().insertOrUpdate(createBot(botId, lastMessageTimestamp = 0L))

        for (i in 1..10) {
            repo.saveMessage(
                MessageEntity(
                    id = "m_$i",
                    botId = botId,
                    role = if (i % 2 == 1) "user" else "assistant",
                    text = "Mesaj #$i",
                    timestamp = i * 1000L
                )
            )
        }

        // saveMessage must advance bot.lastMessageTimestamp!
        assertEquals(10_000L, db.botDao().getBotById(botId)!!.lastMessageTimestamp)

        // getRecentMessagesForBot returns ONLY last 3 in chronological ASC order
        val recent = db.messageDao().getRecentMessagesForBot(botId, 3)
        assertEquals(listOf("m_8", "m_9", "m_10"), recent.map { it.id })

        // O(1) toggleMessageStar via getMessageById
        repo.toggleMessageStar("m_9")
        assertTrue(db.messageDao().getMessageById("m_9")!!.isStarred)
        repo.toggleMessageStar("m_9")
        assertFalse(db.messageDao().getMessageById("m_9")!!.isStarred)

        // Diagnostic log pruning executes cleanly across all 7 tables
        val prunedCount = AppDatabase.pruneDiagnosticLogs(db, maxRowsPerTable = 100)
        assertEquals(AppDatabase.DIAGNOSTIC_LOG_TABLES.size, prunedCount)
    }

    // ── System #15: Shared Process Repository Singleton ───────────────────────

    @Test
    fun system15_sharedRepositorySingletonPreventsScopeLeaks() {
        val r1 = EmochiApplication.getSharedRepository(context)
        val r2 = EmochiApplication.getSharedRepository(context)
        assertSame("Workers and ViewModel must share one process-wide repository instance", r1, r2)
    }

    // ── System #16: Workers Anti-Spam & Turkish Day-Of-Week Matching ──────────

    @Test
    fun system16_proactiveAntiSpamGateAndRoutineTurkishDayMatching() {
        val now = 20 * 60 * 60 * 1000L
        val userMsg3hAgo = MessageEntity("u1", "b1", "user", "Selam", timestamp = now - 3 * 3_600_000L)
        val botMsg3hAgo = MessageEntity("a1", "b1", "assistant", "Nasılsın?", timestamp = now - 3 * 3_600_000L)
        val botMsg14hAgo = MessageEntity("a0", "b1", "assistant", "Orada mısın?", timestamp = now - 14 * 3_600_000L)

        // Eligible: last msg from user 3h ago, bot not active
        assertTrue(
            ProactiveMessageWorker.shouldSendProactiveMessage("b1", true, null, userMsg3hAgo.timestamp, listOf(userMsg3hAgo), now)
        )
        // Blocked: bot is currently open/active
        assertFalse(
            ProactiveMessageWorker.shouldSendProactiveMessage("b1", true, "b1", userMsg3hAgo.timestamp, listOf(userMsg3hAgo), now)
        )
        // Blocked: last msg already from assistant only 3h ago (< 12h cooldown) -> prevents 45-min spam loop!
        assertFalse(
            ProactiveMessageWorker.shouldSendProactiveMessage("b1", true, null, botMsg3hAgo.timestamp, listOf(userMsg3hAgo, botMsg3hAgo), now)
        )
        // Blocked: 2 consecutive assistant messages -> never triple-spam absent user
        assertFalse(
            ProactiveMessageWorker.shouldSendProactiveMessage("b1", true, null, botMsg14hAgo.timestamp, listOf(botMsg14hAgo, botMsg14hAgo.copy(id = "a2")), now)
        )
        // Allowed: single unanswered assistant msg > 12h ago (1 polite follow-up)
        assertTrue(
            ProactiveMessageWorker.shouldSendProactiveMessage("b1", true, null, botMsg14hAgo.timestamp, listOf(userMsg3hAgo.copy(timestamp = now - 15 * 3_600_000L), botMsg14hAgo), now)
        )

        // RoutineNotificationWorker: Turkish AND English day-of-week matching (API 24+ Calendar)
        assertTrue(RoutineNotificationWorker.matchesDayOfWeek("Cumartesi", Calendar.SATURDAY))
        assertTrue(RoutineNotificationWorker.matchesDayOfWeek("saturday", Calendar.SATURDAY))
        assertTrue(RoutineNotificationWorker.matchesDayOfWeek("Çarşamba", Calendar.WEDNESDAY))
        assertTrue(RoutineNotificationWorker.matchesDayOfWeek(null, Calendar.MONDAY))
        assertFalse(RoutineNotificationWorker.matchesDayOfWeek("Pazartesi", Calendar.SATURDAY))
    }

    private fun createBot(id: String, lastMessageTimestamp: Long = 0L) = BotEntity(
        id = id,
        mode = "personal",
        aiName = "Aiden",
        aiPersonality = "Sıcak",
        scenario = "Ev",
        universeName = "",
        keyCharactersJson = "[]",
        userCharName = "Elif",
        userCharDesc = "Dost",
        openingMessage = "Selam.",
        writingStyle = "rp",
        intensity = "normal",
        lastMessageTimestamp = lastMessageTimestamp
    )
}
