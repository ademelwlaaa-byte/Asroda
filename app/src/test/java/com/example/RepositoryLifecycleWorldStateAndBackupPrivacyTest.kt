package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.EmochiRepository
import com.example.util.AppFeatureFlags
import com.example.util.BotDirectorNoteStore
import com.example.util.BotScratchpadStore
import com.example.util.ChatDraftStore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RepositoryLifecycleWorldStateAndBackupPrivacyTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: EmochiRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = EmochiRepository(db, context)
    }

    @After
    fun tearDown() {
        repo.close()
        db.close()
    }

    private fun makeBot(
        id: String,
        aiName: String = "Aiden",
        worldAtmosphere: String = "",
        sceneTime: String = "",
        lastTimeMismatchCorrectionNote: String = "",
        chatBgUrl: String = ""
    ) = BotEntity(
        id = id,
        mode = "roleplay",
        aiName = aiName,
        aiPersonality = "Soğukkanlı ve zeki.",
        scenario = "Gece yarısı gizli araştırma.",
        universeName = "Kozmik",
        keyCharactersJson = "[]",
        userCharName = "Sen",
        userCharDesc = "Gözlemci",
        openingMessage = "Merhaba.",
        writingStyle = "novel",
        intensity = "medium",
        worldAtmosphere = worldAtmosphere,
        sceneTime = sceneTime,
        lastTimeMismatchCorrectionNote = lastTimeMismatchCorrectionNote,
        chatBgUrl = chatBgUrl
    )

    @Test
    fun worldStateManager_preservesMultiValuedRelationsWhileReplacingExclusiveOnes() = runBlocking {
        val botId = "bot_graph_multi"
        db.botDao().insertOrUpdate(makeBot(id = botId, aiName = "Aiden"))

        // Multi-valued relation: "arkadaşı" must keep BOTH Mehmet and Kerem!
        repo.upsertEntityRelation(botId, "Aiden", "arkadaşı", "Mehmet", currentMessageIndex = 5)
        repo.upsertEntityRelation(botId, "Aiden", "arkadaşı", "Kerem", currentMessageIndex = 8)

        val rels = repo.managers.worldStateManager.getEntityRelationsForBot(botId)
        val friends = rels.filter { it.relationType == "arkadaşı" }.map { it.targetEntityName }.toSet()
        assertEquals(setOf("Mehmet", "Kerem"), friends)

        // Shortest path finds BOTH friends, while single-step traverse prefers newest (Kerem)
        val pathMehmet = repo.managers.worldStateManager.findShortestRelationPath(botId, "Aiden", "Mehmet")
        val pathKerem = repo.managers.worldStateManager.findShortestRelationPath(botId, "Aiden", "Kerem")
        assertNotNull(pathMehmet)
        assertNotNull(pathKerem)
        val latestFriend = repo.traverseRelationGraph(botId, "Aiden", listOf("arkadaşı"))
        assertEquals("Kerem", latestFriend?.finalTargetName)

        // Exclusive relation: "patronu" must replace older target
        repo.upsertEntityRelation(botId, "Aiden", "patronu", "Voss", currentMessageIndex = 3)
        repo.upsertEntityRelation(botId, "Aiden", "patronu", "Fury", currentMessageIndex = 9)
        val bosses = repo.managers.worldStateManager.getEntityRelationsForBot(botId)
            .filter { it.relationType == "patronu" }
        assertEquals(1, bosses.size)
        assertEquals("Fury", bosses.first().targetEntityName)
    }

    @Test
    fun worldStateManager_enforcesWordBoundariesAndResolvesCastAliasesInTwoEntityQueries() = runBlocking {
        val botId = "bot_graph_boundary"
        db.botDao().insertOrUpdate(makeBot(id = botId, aiName = "Aiden"))

        repo.upsertEntityRelation(botId, "Ali", "dostu", "Ece", currentMessageIndex = 2)
        repo.upsertEntityRelation(botId, "Natasha Romanoff", "müttefiki", "Aiden", currentMessageIndex = 4)

        // "kaliteli" contains "ali" and "gece" contains "ece", must NOT falsely match Ali <-> Ece!
        val falseMatch = repo.detectAndResolveGraphQuery(botId, "Bu gece kaliteli bir plan ne olurdu?")
        assertNull("Substring inside larger Turkish words must not trigger graph match", falseMatch)

        // Alias resolution in two-entity query ("Kızıl Ajan" -> "Natasha Romanoff")
        val cast = listOf(
            CastMemberEntity(
                botId = botId,
                name = "Natasha Romanoff",
                description = "Ajan",
                multiRoleJson = """["alias:Kızıl Ajan"]"""
            )
        )
        val aliasMatch = repo.detectAndResolveGraphQuery(botId, "Kızıl Ajan ile Aiden'ın ne ilişkisi var?", cast)
        assertNotNull("Stored cast alias should resolve in two-entity graph queries", aliasMatch)
        assertTrue(aliasMatch!!.isComplete)
    }

    @Test
    fun cloneBotResetAndDeleteBot_isolateWorldStatePreserveSharedBgAndCleanStores() = runBlocking {
        val bgDir = File(context.filesDir, "bot_images").apply { mkdirs() }
        val sharedBgFile = File(bgDir, "bg_shared_test.jpg").apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }

        val origId = "bot_orig_lifecycle"
        db.botDao().insertOrUpdate(
            makeBot(
                id = origId,
                aiName = "Selin",
                worldAtmosphere = """{"location":"Eski Kule","weather":"Fırtına"}""",
                sceneTime = "gece",
                lastTimeMismatchCorrectionNote = "stale note",
                chatBgUrl = sharedBgFile.absolutePath
            )
        )
        ChatDraftStore.saveDraft(context, origId, "unsent draft")
        BotScratchpadStore.saveNote(context, origId, "secret clue")
        BotDirectorNoteStore.saveRule(context, origId, "stay dark")

        // 1. Clone must reset mid-story worldAtmosphere and sceneTime
        val cloned = repo.cloneBot(origId)
        assertNotNull(cloned)
        assertEquals("", cloned!!.worldAtmosphere)
        assertEquals("", cloned.sceneTime)

        // 2. Resetting original must also clear worldAtmosphere, sceneTime, and stale time mismatch note
        repo.resetBotMemoryAndEmotion(origId)
        val afterReset = db.botDao().getBotById(origId)!!
        assertEquals("", afterReset.worldAtmosphere)
        assertEquals("", afterReset.sceneTime)
        assertEquals("", afterReset.lastTimeMismatchCorrectionNote)

        // 3. Deleting original must clean per-bot stores AND preserve sharedBgFile while clone still references it
        repo.deleteBot(origId)
        assertEquals("", ChatDraftStore.loadDraft(context, origId))
        assertEquals("", BotScratchpadStore.loadNote(context, origId))
        assertEquals("", BotDirectorNoteStore.loadRule(context, origId))
        assertTrue("Shared local background must survive while cloned bot still references it", sharedBgFile.exists())

        // 4. Deleting the last bot referencing sharedBgFile must clean up the file
        repo.deleteBot(cloned.id)
        assertFalse("Unreferenced local background should be cleaned up", sharedBgFile.exists())
    }

    @Test
    fun legacyRollback_deletesOrphanedCharacterEmotionsForAutoAddedCast() = runBlocking {
        val botId = "bot_legacy_rollback"
        val now = System.currentTimeMillis()
        db.botDao().insertOrUpdate(makeBot(id = botId, aiName = "Aiden"))
        val msg = MessageEntity(id = "msg_legacy_1", botId = botId, role = "assistant", text = "Karan ortaya çıktı.", timestamp = now)
        db.messageDao().insertMessage(msg)

        db.castMemberDao().insertCastMember(
            CastMemberEntity(
                botId = botId,
                name = "Karan",
                description = "Otomatik karakter",
                isAutoAdded = true,
                firstAppearedAt = now + 100L
            )
        )
        db.characterEmotionDao().insertOrUpdate(
            CharacterEmotionEntity(
                botId = botId,
                characterName = "Karan",
                emotionState = """{"trust":40}"""
            )
        )

        val rolledBack = repo.rollbackMessageTurnData(botId, msg.id, deleteTargetMessage = true)
        assertTrue(rolledBack)
        assertTrue(db.castMemberDao().getCastMembersForBot(botId).none { it.name == "Karan" })
        assertNull(
            "Orphaned character_emotions row must be removed along with rolled-back auto-cast",
            db.characterEmotionDao().getEmotionForCharacter(botId, "Karan")
        )
    }

    @Test
    fun backupPrivacyXmls_excludePrivateRoleplayPreferences() {
        val backupXml = File("src/main/res/xml/backup_rules.xml").readText()
        val extractXml = File("src/main/res/xml/data_extraction_rules.xml").readText()
        val sensitivePrefs = listOf(
            "asroda_chat_drafts_v1.xml",
            "asroda_bot_scratchpad_v1.xml",
            "asroda_bot_director_notes_v1.xml",
            "asroda_story_bookmarks_v1.xml",
            "asroda_roleplay_macros_v1.xml"
        )
        for (pref in sensitivePrefs) {
            assertTrue("backup_rules.xml must exclude $pref", backupXml.contains(pref))
            assertTrue("data_extraction_rules.xml must exclude $pref", extractXml.contains(pref))
        }
    }
}
