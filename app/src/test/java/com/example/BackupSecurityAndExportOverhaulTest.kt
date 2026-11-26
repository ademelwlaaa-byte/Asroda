package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AffectionEventEntity
import com.example.data.local.AppDatabase
import com.example.data.local.ArchivedMemoryEntity
import com.example.data.local.BotEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.CustomProviderEntity
import com.example.data.local.EmotionHistoryEntity
import com.example.data.local.EntityRegistryEntity
import com.example.data.local.MemoryFragmentEntity
import com.example.data.local.MessageEntity
import com.example.data.local.SceneTemplateEntity
import com.example.data.local.StoryProgressEntity
import com.example.data.local.TurnRollbackSnapshotEntity
import com.example.data.repository.EmochiRepository
import com.example.util.AppFeatureFlags
import com.example.util.ChatExporter
import com.example.util.KeystoreEncryptionManager
import com.example.util.SecureApiKeyVault
import com.example.util.StoryArchiveBundler
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupSecurityAndExportOverhaulTest {

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
        File(context.cacheDir, "exports").deleteRecursively()
        db.close()
    }

    private fun makeBot(id: String, name: String = "Natasha", mode: String = "personal", universe: String = "") = BotEntity(
        id = id,
        mode = mode,
        aiName = name,
        aiPersonality = "Soğukkanlı stratejist",
        scenario = "İstanbul operasyon üssü",
        universeName = universe,
        keyCharactersJson = "[]",
        userCharName = "Aiden",
        userCharDesc = "Saha ajanı",
        openingMessage = "Hazır mısın?",
        writingStyle = "rp",
        intensity = "normal"
    )

    @Test
    fun test1_backupSnapshot_roundTripsAllRoomV50TablesAndOverwriteClearsStaleRows() = runBlocking {
        val bot1 = makeBot("bot_full_1", "Natasha")
        db.botDao().insertOrUpdate(bot1)
        db.messageDao().insertMessage(MessageEntity("m1", bot1.id, "assistant", "Üsse hoş geldin.", 1000L))
        db.entityRegistryDao().insertOrUpdateEntity(
            EntityRegistryEntity(
                botId = bot1.id,
                entityName = "Yerebatan Sarnıcı",
                entityType = "location",
                description = "Gizli buluşma noktası",
                firstMentionedAt = 1000L,
                lastMentionedAt = 1500L
            )
        )
        db.storyProgressDao().insertOrUpdate(
            StoryProgressEntity(botId = bot1.id, chapterNumber = 4, currentBranch = "istanbul_shadow", tonyAffinity = 12, natashaBond = 25)
        )
        db.emotionHistoryDao().insertHistory(
            EmotionHistoryEntity(
                botId = bot1.id,
                timestamp = 1200L,
                messageIndex = 2,
                affectionScore = 72,
                respectScore = 68,
                comfortScore = 60,
                resentmentScore = 5,
                trustScore = 74,
                physicalComfortScore = 40,
                obsessionScore = 8,
                dominantEmotion = "güven",
                fullVectorJson = "{}"
            )
        )
        db.affectionEventDao().insertEvent(
            AffectionEventEntity(botId = bot1.id, timestamp = 1300L, scoreDelta = 6, shortDescription = "Çatıda hayatını kurtardı")
        )
        db.memoryFragmentDao().insertFragment(
            MemoryFragmentEntity(botId = bot1.id, content = "Şifreli çanta Yerebatan sütununda saklandı.", category = "fact", createdAt = 1400L)
        )
        db.sceneTemplateDao().insertTemplates(
            listOf(SceneTemplateEntity("tpl_cistern", "Yerebatan Gecesi", "Yeraltı sarnıcı", "rp", "yüksek", 1.25, "Gerilimli gizli operasyon"))
        )
        db.archivedMemoryDao().insertArchive(
            ArchivedMemoryEntity(botId = bot1.id, originalMemoryId = 7L, memoryType = "fact", content = "Eski not", archivedReason = "merged", timestamp = 1500L)
        )
        db.characterEmotionDao().insertOrUpdate(
            CharacterEmotionEntity(botId = bot1.id, characterName = "Tony Stark", emotionState = "{\"mood\":\"alaycı\"}")
        )

        val exportedJson = repo.exportDataSnapshot(includeApiKeys = false)

        // Pollute DB with stale data & rollback snapshots that MUST disappear on overwriteExisting=true
        val staleBot = makeBot("bot_stale_99", "Stale")
        db.botDao().insertOrUpdate(staleBot)
        db.archivedMemoryDao().insertArchive(
            ArchivedMemoryEntity(botId = staleBot.id, originalMemoryId = 99L, memoryType = "event", content = "Silinmeli", archivedReason = "stale", timestamp = 9999L)
        )
        db.turnRollbackSnapshotDao().upsert(
            TurnRollbackSnapshotEntity(messageId = "stale_msg", botId = staleBot.id)
        )
        db.entityRegistryDao().insertOrUpdateEntity(
            EntityRegistryEntity(botId = staleBot.id, entityName = "Eski Mekan", entityType = "location", description = "Silinmeli", firstMentionedAt = 1L, lastMentionedAt = 1L)
        )

        val summary = repo.importDataSnapshot(exportedJson, overwriteExisting = true)
        assertEquals(1, summary.botsCount)

        // All 6 newly added tables + archivedMemories + characterEmotions restored cleanly
        assertEquals(1, db.entityRegistryDao().getEntitiesForBot(bot1.id).size)
        assertEquals("Yerebatan Sarnıcı", db.entityRegistryDao().getEntitiesForBot(bot1.id).first().entityName)
        val restoredProgress = db.storyProgressDao().getProgressForBot(bot1.id)
        assertNotNull(restoredProgress)
        assertEquals(4, restoredProgress!!.chapterNumber)
        assertEquals("istanbul_shadow", restoredProgress.currentBranch)
        assertEquals(1, db.emotionHistoryDao().getAllHistoryForBot(bot1.id).size)
        assertEquals(1, db.affectionEventDao().getEventsForBotList(bot1.id).size)
        assertEquals(1, db.memoryFragmentDao().getRecentFragments(bot1.id, 10).size)
        assertTrue(db.sceneTemplateDao().getAllTemplates().any { it.id == "tpl_cistern" })
        assertEquals(1, db.archivedMemoryDao().getArchivesForBot(bot1.id).size)
        assertEquals(1, db.characterEmotionDao().getEmotionsForBot(bot1.id).size)

        // Stale rows from deleted bot MUST be completely wiped
        assertEquals(0, db.archivedMemoryDao().getArchivesForBot(staleBot.id).size)
        assertEquals(0, db.turnRollbackSnapshotDao().getSnapshotsForBot(staleBot.id).size)
        assertEquals(0, db.entityRegistryDao().getEntitiesForBot(staleBot.id).size)
    }

    @Test
    fun test2_customProviderKeys_unifiedEncryptionLegacyMigrationAndMaskedPreserve() = runBlocking {
        val rawSecret = "sk-or-v1-testCustomProviderSecret99887766"
        val legacyEncrypted = KeystoreEncryptionManager.encrypt(rawSecret)
        val vaultEncrypted = SecureApiKeyVault.encrypt(rawSecret)

        assertTrue(SecureApiKeyVault.isEncrypted(vaultEncrypted))
        assertEquals(rawSecret, SecureApiKeyVault.decryptFlexible(vaultEncrypted))
        assertEquals(rawSecret, SecureApiKeyVault.decryptFlexible(legacyEncrypted))
        assertEquals(rawSecret, SecureApiKeyVault.decryptFlexible(rawSecret))

        // Insert a custom provider encrypted with legacy KeystoreEncryptionManager
        db.customProviderDao().insertProvider(
            CustomProviderEntity(
                label = "Benim Proxy",
                baseUrl = "https://proxy.example.com/v1",
                apiKeyEncrypted = legacyEncrypted,
                modelName = "deepseek-chat"
            )
        )

        // Export WITH API keys -> must decrypt legacy key cleanly to plaintext in export JSON
        val exportWithKeys = repo.exportDataSnapshot(includeApiKeys = true)
        assertTrue("Export with keys must contain decrypted secret", exportWithKeys.contains(rawSecret))

        // Export WITHOUT API keys -> must strip key, and re-importing masked snapshot must preserve local key & upgrade to ENC_V2
        val exportMasked = repo.exportDataSnapshot(includeApiKeys = false)
        assertFalse("Masked export must NOT leak secret", exportMasked.contains(rawSecret))

        repo.importDataSnapshot(exportMasked, overwriteExisting = true)
        val restoredProviders = db.customProviderDao().getAllProviders()
        assertEquals(1, restoredProviders.size)
        assertTrue("Restored key must be upgraded to ENC_V2 vault ciphertext", SecureApiKeyVault.isEncrypted(restoredProviders.first().apiKeyEncrypted))
        assertEquals(rawSecret, SecureApiKeyVault.decryptFlexible(restoredProviders.first().apiKeyEncrypted))
    }

    @Test
    fun test3_secureApiKeyVault_redactsExpandedSecretPatternsAndIsThreadSafe() {
        val noisyLog = """
            hf=hf_AbCdEfGhIjKlMnOpQrStUvWxYz
            gh=ghp_1234567890abcdef1234567890abcdef1234
            gh2=github_pat_11AAAAAA_bbbbbbbbbbbbbbbbbbbbbbbb
            pplx=pplx-1234567890abcdef1234567890abcdef
            fw=fw_3Z9x8y7w6v5u4t3s2r1q0p
            tg=tgp_v1_abcdef1234567890abcdef123456
            json={"apiKey":"super-secret-value-123","token":"tok-987654321"}
        """.trimIndent()

        val redacted = SecureApiKeyVault.redactSecrets(noisyLog)
        assertFalse(redacted.contains("hf_AbCdEf"))
        assertFalse(redacted.contains("ghp_123456"))
        assertFalse(redacted.contains("github_pat_11"))
        assertFalse(redacted.contains("pplx-123456"))
        assertFalse(redacted.contains("fw_3Z9x8y"))
        assertFalse(redacted.contains("tgp_v1_abcdef"))
        assertFalse(redacted.contains("super-secret-value-123"))
        assertFalse(redacted.contains("tok-987654321"))
        assertTrue(redacted.contains("[REDACTED_HF_TOKEN]"))
        assertTrue(redacted.contains("[REDACTED_GITHUB_TOKEN]"))
        assertTrue(redacted.contains("[REDACTED_PERPLEXITY_KEY]"))

        // Concurrent encrypt/decrypt across 12 threads
        val pool = Executors.newFixedThreadPool(12)
        try {
            val tasks = (1..24).map { idx ->
                Callable {
                    val plain = "sk-concurrent-secret-$idx-abcdef1234567890"
                    val enc = SecureApiKeyVault.encrypt(plain)
                    val dec = SecureApiKeyVault.decrypt(enc)
                    assertEquals(plain, dec)
                    true
                }
            }
            val results = pool.invokeAll(tasks).map { it.get() }
            assertTrue(results.all { it })
        } finally {
            pool.shutdownNow()
        }
    }

    @Test
    fun test4_chatExporter_blocksPathTraversalAndAvoidsFilenameCollisions() {
        val evilName = "../../../../etc/passwd\u0000"
        val sanitized = ChatExporter.sanitizeExportFileName(evilName, requiredExtension = "json")
        assertFalse("Must not contain path separators", sanitized.contains("/") || sanitized.contains("\\"))
        assertFalse("Must not contain parent traversal", sanitized.contains(".."))
        assertTrue("Must end with .json", sanitized.endsWith(".json"))

        val f1 = ChatExporter.writeJsonToExportFile(context, "../secret/backup.json", "{\"a\":1}")
        val f2 = ChatExporter.writeJsonToExportFile(context, "../secret/backup.json", "{\"a\":2}")

        val exportsDir = File(context.cacheDir, "exports").canonicalFile
        assertTrue("File 1 must stay inside cacheDir/exports", f1.canonicalPath.startsWith(exportsDir.canonicalPath))
        assertTrue("File 2 must stay inside cacheDir/exports", f2.canonicalPath.startsWith(exportsDir.canonicalPath))
        assertNotEquals("Second write with same name must get collision-safe unique file", f1.name, f2.name)
        assertEquals("{\"a\":1}", f1.readText())
        assertEquals("{\"a\":2}", f2.readText())
    }

    @Test
    fun test5_storyArchiveBundler_stripsInternalTagsAndRespectsUniverseTitle() {
        val universeBot = makeBot(
            id = "bot_uni_1",
            name = "Anlatıcı",
            mode = "universe",
            universe = "Asroda: Gölgeler Çağı"
        )
        val messages = listOf(
            MessageEntity(
                id = "m1",
                botId = universeBot.id,
                role = "assistant",
                text = "<think>gizli plan</think>İstanbul gecesi sessizdi. [[STATE_JSON {\"affection\":80}]]",
                timestamp = 1_000_000L
            )
        )

        val archive = StoryArchiveBundler.buildCompleteArchive(universeBot, messages)
        assertTrue("Universe archive should include universe title", archive.contains("Asroda: Gölgeler Çağı"))
        assertFalse("Internal <think> tag must be stripped from archive", archive.contains("gizli plan"))
        assertFalse("STATE_JSON tag must be stripped from archive", archive.contains("STATE_JSON"))
    }
}
