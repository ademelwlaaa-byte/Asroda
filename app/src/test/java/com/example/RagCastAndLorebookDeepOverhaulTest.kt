package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.LorebookEntryEntity
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MemoryFactEntity
import com.example.data.local.MessageEntity
import com.example.data.managers.BotMemoryIndex
import com.example.data.managers.CastMemberManager
import com.example.data.managers.LorebookManager
import com.example.data.managers.MemoryRetrievalManager
import com.example.data.managers.MemoryVectorCache
import com.example.data.repository.KeyCharacter
import com.example.data.repository.LivingMemoryCodec
import kotlinx.coroutines.CancellationException
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RagCastAndLorebookDeepOverhaulTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var memoryManager: MemoryRetrievalManager
    private lateinit var castManager: CastMemberManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        memoryManager = MemoryRetrievalManager(
            fragmentDao = db.memoryFragmentDao(),
            memoryFactDao = db.memoryFactDao(),
            memoryEventDao = db.memoryEventDao(),
            memoryCheckpointDao = db.memoryCheckpointDao()
        )
        castManager = CastMemberManager(
            castMemberDao = db.castMemberDao(),
            emotionDao = db.characterEmotionDao(),
            botDao = db.botDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun makeBot(botId: String = "bot_pkg_r"): BotEntity = BotEntity(
        id = botId,
        mode = "personal",
        aiName = "Aiden",
        aiPersonality = "Korumacı ve gözlemci",
        scenario = "Yağmurlu bir İstanbul akşamı",
        universeName = "",
        keyCharactersJson = "[]",
        userCharName = "Eren",
        userCharDesc = "Araştırmacı",
        openingMessage = "Hoş geldin.",
        writingStyle = "novel",
        intensity = "normal",
        storyDayCounter = 4L,
        storyCalendarDate = "2026-05-10",
        sceneTime = "akşam"
    )

    // ── 1. MemoryRetrievalManager: Math, Bounded Cache, Dedupe & Cancellation ──

    @Test
    fun memoryRetrieval_safeCosineSimilarityHandlesNaNInfinityZeroNormAndMismatchedDims() {
        assertEquals(0f, MemoryRetrievalManager.safeCosineSimilarity(floatArrayOf(), floatArrayOf()), 1e-6f)
        assertEquals(0f, MemoryRetrievalManager.safeCosineSimilarity(floatArrayOf(1f, 2f), floatArrayOf(1f)), 1e-6f)
        assertEquals(0f, MemoryRetrievalManager.safeCosineSimilarity(floatArrayOf(0f, 0f), floatArrayOf(1f, 1f)), 1e-6f)
        assertEquals(0f, MemoryRetrievalManager.safeCosineSimilarity(floatArrayOf(Float.NaN, 1f), floatArrayOf(1f, 1f)), 1e-6f)
        assertEquals(0f, MemoryRetrievalManager.safeCosineSimilarity(floatArrayOf(Float.POSITIVE_INFINITY, 1f), floatArrayOf(1f, 1f)), 1e-6f)
        assertEquals(0f, MemoryRetrievalManager.safeCosineSimilarity(floatArrayOf(Float.NEGATIVE_INFINITY, 1f), floatArrayOf(1f, 1f)), 1e-6f)

        assertEquals(1f, MemoryRetrievalManager.safeCosineSimilarity(floatArrayOf(3f, 4f), floatArrayOf(6f, 8f)), 1e-5f)
        assertEquals(-1f, MemoryRetrievalManager.safeCosineSimilarity(floatArrayOf(1f, 0f), floatArrayOf(-2f, 0f)), 1e-5f)
        assertEquals(0f, MemoryRetrievalManager.safeCosineSimilarity(floatArrayOf(1f, 0f), floatArrayOf(0f, 5f)), 1e-5f)
    }

    @Test
    fun memoryRetrieval_vectorCacheEnforcesBoundedLruEvictionAndSafeMinCapacity() {
        fun dummyIndex(id: String) = BotMemoryIndex(
            botId = id,
            facts = emptyList(),
            events = emptyList(),
            checkpoints = emptyList(),
            fragments = emptyList(),
            builtAtMs = 1_000L
        )

        val cache = MemoryVectorCache(maxBots = 2)
        cache.put("bot_a", dummyIndex("bot_a"))
        cache.put("bot_b", dummyIndex("bot_b"))
        // Touch bot_a so bot_b becomes least-recently-used
        assertNotNull(cache.get("bot_a"))
        cache.put("bot_c", dummyIndex("bot_c"))

        assertEquals(2, cache.size())
        assertNotNull("Most recently accessed bot_a must stay cached", cache.get("bot_a"))
        assertNull("Least recently used bot_b must be evicted", cache.get("bot_b"))
        assertNotNull("Newly inserted bot_c must be cached", cache.get("bot_c"))

        // Non-positive capacity clamps safely to 1 without throwing
        val minCache = MemoryVectorCache(maxBots = 0)
        minCache.put("x", dummyIndex("x"))
        minCache.put("y", dummyIndex("y"))
        assertEquals(1, minCache.size())
        assertNull(minCache.get("x"))
        assertNotNull(minCache.get("y"))
    }

    @Test
    fun memoryRetrieval_nearDuplicateSuppressionPreservesUserLockedAndDistinctSubjects() {
        val fact1 = MemoryFactEntity(
            id = 1L,
            botId = "b1",
            subject = "Eren",
            key = "İçecek",
            value = "Sabahları sade Türk kahvesi içmeyi sever",
            userCorrected = false
        )
        val fact1Dup = MemoryFactEntity(
            id = 2L,
            botId = "b1",
            subject = "Eren",
            key = "İçecek",
            value = "Sabahları sade Türk kahvesi içmeyi seviyor",
            userCorrected = false
        )
        val factLocked = MemoryFactEntity(
            id = 3L,
            botId = "b1",
            subject = "Eren",
            key = "İçecek",
            value = "Sabahları sade Türk kahvesi içmeyi sever",
            userCorrected = true
        )
        val factOtherSubject = MemoryFactEntity(
            id = 4L,
            botId = "b1",
            subject = "Aiden",
            key = "İçecek",
            value = "Sabahları sade Türk kahvesi içmeyi sever",
            userCorrected = false
        )

        val dedupedFacts = MemoryRetrievalManager.deduplicateFacts(
            listOf(fact1, fact1Dup, factLocked, factOtherSubject)
        )
        assertEquals(listOf(1L, 3L, 4L), dedupedFacts.map { it.id })

        val ev1 = MemoryEventEntity(
            id = 10L,
            botId = "b1",
            timestamp = 1000L,
            description = LivingMemoryCodec.encode(
                povCharacter = "Aiden",
                title = "Çatıdaki Yemin",
                emotionTag = "Güven",
                salience = 8,
                narrative = "Yağmur altında çatıdaki gizli anahtarı birlikte saklayacağımıza söz verdik.",
                innerThought = ""
            )
        )
        val ev1Dup = MemoryEventEntity(
            id = 11L,
            botId = "b1",
            timestamp = 1100L,
            description = LivingMemoryCodec.encode(
                povCharacter = "Aiden",
                title = "Çatıdaki Yemin",
                emotionTag = "Güven",
                salience = 7,
                narrative = "Yağmur altında çatıdaki gizli anahtarı birlikte saklayacağımıza söz verdik.",
                innerThought = ""
            )
        )
        val evDistinct = MemoryEventEntity(
            id = 12L,
            botId = "b1",
            timestamp = 1200L,
            description = LivingMemoryCodec.encode(
                povCharacter = "Aiden",
                title = "Kütüphane Keşfi",
                emotionTag = "Merak",
                salience = 6,
                narrative = "Eski kütüphane mahzeninde mühürlü bir harita bulduk.",
                innerThought = ""
            )
        )
        val dedupedEvents = MemoryRetrievalManager.deduplicateEvents(listOf(ev1, ev1Dup, evDistinct))
        assertEquals(listOf(10L, 12L), dedupedEvents.map { it.id })
    }

    @Test
    fun memoryRetrieval_parseAiRagDecisionDeduplicatesAndHandlesFencedAndMalformedJson() {
        val fenced = """
            ```json
            {"pick":["f1","F1","e4","c2"],"miss_queries":["Gizli Anahtar","gizli anahtar","Mahzen"]}
            ```
        """.trimIndent()
        val parsed = MemoryRetrievalManager.parseAiRagDecision(fenced)
        assertNotNull(parsed)
        assertEquals(listOf("F1", "E4", "C2"), parsed!!.first)
        assertEquals(listOf("Gizli Anahtar", "Mahzen"), parsed.second)

        assertNull(MemoryRetrievalManager.parseAiRagDecision(""))
        assertNull(MemoryRetrievalManager.parseAiRagDecision("not json at all"))
        assertNull(MemoryRetrievalManager.parseAiRagDecision("{broken json"))
    }

    @Test
    fun memoryRetrieval_cancellationExceptionPropagatesFromAiSelectorAndGraphResolver() = runBlocking {
        val bot = makeBot("bot_cancel_rag")
        db.botDao().insertOrUpdate(bot)
        db.memoryFactDao().insertFact(
            MemoryFactEntity(
                botId = bot.id,
                subject = "Eren",
                key = "Sır",
                value = "Eski kütüphane mahzeninin anahtarını taşır.",
                confidence = "certain"
            )
        )
        val msgs = listOf(
            MessageEntity(
                id = "m1",
                botId = bot.id,
                role = "user",
                text = "Kütüphane mahzenindeki gizli plan hakkında konuşmamız gerek."
            )
        )

        var graphCancelled = false
        try {
            memoryManager.retrieveCoPilotedContext(
                bot = bot,
                recentMessages = msgs,
                graphResolver = { throw CancellationException("graph cancelled") }
            )
        } catch (_: CancellationException) {
            graphCancelled = true
        }
        assertTrue("CancellationException from graphResolver must propagate", graphCancelled)

        var selectorCancelled = false
        try {
            memoryManager.retrieveCoPilotedContext(
                bot = bot,
                recentMessages = msgs,
                aiSelector = { _, _ -> throw CancellationException("selector cancelled") }
            )
        } catch (_: CancellationException) {
            selectorCancelled = true
        }
        assertTrue("CancellationException from aiSelector must propagate", selectorCancelled)
    }

    // ── 2. CastMemberManager: Age, Turkish İ/ı Dedup, Blacklist Alias & Cleanup ──

    @Test
    fun castMember_computeExactAgeHandlesBirthdayBoundariesYearFallbackAndInvalidDates() {
        assertEquals(19, CastMemberManager.computeExactAge("2000-05-10", "2020-05-09"))
        assertEquals(20, CastMemberManager.computeExactAge("2000-05-10", "2020-05-10"))
        assertEquals(20, CastMemberManager.computeExactAge("2000-05-10", "2020-05-11"))
        assertEquals(30, CastMemberManager.computeExactAge("1995", "2025"))
        assertNull(CastMemberManager.computeExactAge("2030-01-01", "2025-01-01"))
        assertNull(CastMemberManager.computeExactAge("", "2025-01-01"))
        assertNull(CastMemberManager.computeExactAge("invalid-date", "2025-01-01"))
    }

    @Test
    fun castMember_turkishLocaleCaseFoldingMergesDuplicatesAndEnforcesBlacklistAliases() = runBlocking {
        assertEquals(CastMemberManager.turkishNameKey("İrem"), CastMemberManager.turkishNameKey("irem"))
        assertEquals(CastMemberManager.turkishNameKey("Irmak"), CastMemberManager.turkishNameKey("ırmak"))

        val bot = makeBot("bot_cast_tr")
        db.botDao().insertOrUpdate(bot)

        // Register İrem, then re-register lowercase "irem" -> must merge into a single row!
        castManager.ensureSideCharacterRegistered(
            botId = bot.id,
            rawCharacterName = "İrem",
            role = "Yakın Dost",
            cueSentence = "İrem sessizce kapıyı araladı.",
            bot = bot,
            confidence = 90,
            pendingConfirmation = false
        )
        castManager.ensureSideCharacterRegistered(
            botId = bot.id,
            rawCharacterName = "irem",
            role = "Yakın Dost",
            cueSentence = "irem notları masaya bıraktı.",
            bot = bot,
            confidence = 90,
            pendingConfirmation = false
        )
        val afterMerge = castManager.getCastMembersForBot(bot.id)
        assertEquals("Turkish İ/i variant must NOT create duplicate cast card", 1, afterMerge.size)
        assertEquals(2, afterMerge.first().appearanceCount)

        // Blacklist via lowercase "irem" must locate and blacklist "İrem"
        castManager.deleteCharacterAndBlacklist(bot.id, "irem")
        assertTrue(castManager.getCastMembersForBot(bot.id).isEmpty())
        assertNull(db.characterEmotionDao().getEmotionForCharacter(bot.id, "İrem"))

        // KeyCharacter mirroring must NOT resurrect blacklisted character
        castManager.ensureKeyCharacterMirroredAsCastMember(
            botId = bot.id,
            keyChar = KeyCharacter(name = "İrem", desc = "Eski dost")
        )
        assertTrue("Blacklisted character must not be resurrected by KeyCharacter mirror", castManager.getCastMembersForBot(bot.id).isEmpty())
    }

    @Test
    fun castMember_auditAndCleanRemovesCorruptNamesMergesTurkishDuplicatesAndClampsScores() = runBlocking {
        val bot = makeBot("bot_cast_audit")
        db.botDao().insertOrUpdate(bot)

        db.castMemberDao().insertCastMember(
            CastMemberEntity(
                botId = bot.id,
                name = "...",
                role = "Yan Karakter",
                description = "",
                isAutoAdded = true
            )
        )
        db.castMemberDao().insertCastMember(
            CastMemberEntity(
                botId = bot.id,
                name = "İrem Kaya",
                role = "Friend",
                description = "Arkadaş",
                affectionScore = 140,
                importanceScore = 120,
                appearanceCount = 2,
                isAutoAdded = true
            )
        )
        db.castMemberDao().insertCastMember(
            CastMemberEntity(
                botId = bot.id,
                name = "irem kaya",
                role = "Yan Karakter",
                description = "",
                affectionScore = 60,
                appearanceCount = 1,
                isAutoAdded = true
            )
        )
        db.characterEmotionDao().insertOrUpdate(
            CharacterEmotionEntity(botId = bot.id, characterName = "irem kaya", emotionState = "{}")
        )

        val report = castManager.auditAndCleanCastForBot(bot.id)
        assertEquals(1, report.removedFalsePositives)
        assertEquals(1, report.mergedDuplicates)
        assertEquals(1, report.normalizedTurkishRoles)

        val survivors = castManager.getCastMembersForBot(bot.id)
        assertEquals(1, survivors.size)
        assertEquals("İrem Kaya", survivors.first().name)
        assertEquals(3, survivors.first().appearanceCount)
        assertTrue("Affection score must be clamped to 0..100", survivors.first().affectionScore in 0..100)
        assertTrue("Importance score must be clamped to 0..100", survivors.first().importanceScore in 0..100)
        assertFalse("English role 'Friend' must be normalized to Turkish", survivors.first().role.equals("Friend", ignoreCase = true))
    }

    // ── 3. LorebookManager: Negative Exclusions, Chaining, Tight Budget & Import ──

    @Test
    fun lorebook_negativeExclusionBlocksBothSubstringAndIndependentKeywordsAndChainedTriggers() {
        val swordEntry = LorebookEntryEntity(
            id = "e_sword",
            lorebookId = "lb",
            title = "Gümüş Kılıç",
            keywords = "kılıç, -oyuncak",
            content = "Gümüş kılıç kadim mühür taşır ve ejderha kapısını açar.",
            priority = 9
        )
        val dragonGateEntry = LorebookEntryEntity(
            id = "e_gate",
            lorebookId = "lb",
            title = "Ejderha Kapısı",
            keywords = "ejderha, -yasak",
            content = "Ejderha kapısı yeraltı zindanına açılır.",
            priority = 8
        )
        val dungeonHop2Entry = LorebookEntryEntity(
            id = "e_hop2",
            lorebookId = "lb",
            title = "Zindan Bekçisi",
            keywords = "zindan",
            content = "İkinci sıçrama tetiklenmemelidir.",
            priority = 7
        )

        // Independent negative exclusion "-oyuncak" must block "kılıç"
        val blockedDirect = LorebookManager.selectEntriesForContext(
            entries = listOf(swordEntry, dragonGateEntry, dungeonHop2Entry),
            recentMessageTexts = listOf("Çocuk elindeki ahşap oyuncak kılıcı salladı.")
        )
        assertTrue(blockedDirect.selected.isEmpty())

        // Without "-oyuncak", softened "kılıcını" triggers swordEntry, 1-hop chains dragonGateEntry,
        // and strictly stops before 2-hop dungeonHop2Entry!
        val triggered1Hop = LorebookManager.selectEntriesForContext(
            entries = listOf(swordEntry, dragonGateEntry, dungeonHop2Entry),
            recentMessageTexts = listOf("Aiden karanlıkta gümüş kılıcını çekti.")
        )
        assertEquals(listOf("e_sword", "e_gate"), triggered1Hop.selected.map { it.entry.id })
        assertTrue(triggered1Hop.selected.any { it.entry.id == "e_gate" && it.reason.startsWith("zincir:") })

        // Negative exclusion on chained entry ("-yasak") present in recent messages must block chained trigger!
        val blockedChain = LorebookManager.selectEntriesForContext(
            entries = listOf(swordEntry, dragonGateEntry),
            recentMessageTexts = listOf("Bu bölgede yasak bir büyü var, ama gümüş kılıcını çekti.")
        )
        assertEquals(listOf("e_sword"), blockedChain.selected.map { it.entry.id })
    }

    @Test
    fun lorebook_tightTokenBudgetNeverExceedsBudgetAndTrimsSecondConstantEntry() {
        val longConstant1 = LorebookEntryEntity(
            id = "c1",
            lorebookId = "lb",
            title = "Birinci Kural",
            keywords = "",
            content = "Bu evrende büyü her zaman bir bedel ister ve denge bozulduğunda gökyüzü kararır.",
            priority = 10,
            isConstant = true
        )
        val longConstant2 = LorebookEntryEntity(
            id = "c2",
            lorebookId = "lb",
            title = "İkinci Kural",
            keywords = "",
            content = "Karanlık mühürler yalnızca kadim kan bağına sahip mirasçılar tarafından açılabilir ve unutulmuş sırlar uyanır. ".repeat(6),
            priority = 9,
            isConstant = true
        )

        val result = LorebookManager.selectEntriesForContext(
            entries = listOf(longConstant1, longConstant2),
            recentMessageTexts = listOf("Merhaba."),
            tokenBudget = 160
        )
        assertTrue("Estimated tokens (${result.estimatedTokens}) must never exceed budget 160", result.estimatedTokens <= 160)
        assertEquals("Both constant rules should be preserved (second trimmed cleanly instead of dropped)", 2, result.selected.size)
        assertTrue(result.promptBlock.contains("Birinci Kural"))
        assertTrue(result.promptBlock.contains("İkinci Kural"))
        assertTrue(result.promptBlock.contains("…"))
    }

    @Test
    fun lorebook_importJsonValidatesOversizedBlankDeduplicatesAndSupportsSillyTavernMap() {
        assertTrue(LorebookManager.safeImportLorebookJson("   ").isFailure)
        assertTrue(LorebookManager.safeImportLorebookJson("{broken").isFailure)

        val oversized = "{\"name\":\"Big\",\"entries\":[]}" + " ".repeat(LorebookManager.MAX_IMPORT_JSON_CHARS + 16)
        assertTrue(LorebookManager.safeImportLorebookJson(oversized).isFailure)

        val sillyTavernJson = """
            {
              "name": "Kadim Dünya",
              "token_budget": 900,
              "entries": {
                "0": {
                  "comment": "Karanlık Kule",
                  "key": ["kule", "gölge"],
                  "content": "Karanlık kule kuzey dağlarındadır.",
                  "category": "MEKAN",
                  "order": 8
                },
                "1": {
                  "comment": "Karanlık Kule",
                  "key": ["kule"],
                  "content": "Karanlık kule kuzey dağlarındadır.",
                  "category": "MEKAN",
                  "order": 8
                }
              }
            }
        """.trimIndent()

        val bundle = LorebookManager.importLorebookJson(sillyTavernJson)
        assertEquals("Kadim Dünya", bundle.lorebook.name)
        assertEquals(900, bundle.lorebook.tokenBudget)
        assertEquals("Duplicate entries must be deduplicated on import", 1, bundle.entries.size)
        assertEquals("mekan", bundle.entries.first().category)
        assertEquals("kule, gölge", bundle.entries.first().keywords)
    }
}
