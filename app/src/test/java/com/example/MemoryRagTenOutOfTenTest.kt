package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.MemoryToolRegistry
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.EmochiRepository
import com.example.data.repository.MemoryEngine
import com.example.data.repository.MemoryTier
import com.example.data.repository.RelativeTimeCueResolver
import com.example.data.repository.TurkishTextAnalyzer
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
@Config(sdk = [36])
class MemoryRagTenOutOfTenTest {

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
        db.close()
    }

    @Test
    fun temporalBoostIsRrfCalibratedAndPreservesRelevanceAmongMultipleTemporalCandidates() = runBlocking {
        assertTrue(
            "TEMPORAL_BOOST must be calibrated in RRF units (~0.025), not 0.55",
            MemoryEngine.TEMPORAL_BOOST in 0.015f..0.040f
        )

        val botId = "bot_rag_temporal_10_10"
        val botDay5 = BotEntity(
            id = botId,
            mode = "personal",
            aiName = "Aiden",
            aiPersonality = "Sıcak",
            scenario = "Ev",
            universeName = "",
            keyCharactersJson = "[]",
            userCharName = "Kullanıcı",
            userCharDesc = "Dost",
            openingMessage = "Selam.",
            writingStyle = "rp",
            intensity = "normal",
            storyDayCounter = 5L
        )
        db.botDao().insertOrUpdate(botDay5)

        repo.upsertNeuralVaultFact(
            botId = botId,
            subject = "Kullanıcı",
            key = "Kıyafet",
            value = "Dün siyah deri ceket giydi",
            tier = MemoryTier.EPHEMERAL,
            ttlDays = 3,
            importance = 4,
            storyDay = 4L
        )
        repo.upsertNeuralVaultFact(
            botId = botId,
            subject = "Kullanıcı",
            key = "Akşam Yemeği",
            value = "Akşam sarımsaklı kremalı makarna yedi",
            tier = MemoryTier.EPHEMERAL,
            ttlDays = 3,
            importance = 4,
            storyDay = 4L
        )
        repo.upsertNeuralVaultFact(
            botId = botId,
            subject = "Aiden",
            key = "Meslek",
            value = "Gizli ajan olarak çalışıyor",
            tier = MemoryTier.CORE,
            importance = 9,
            storyDay = 1L
        )

        val bundle = repo.managers.memoryRetrievalManager.retrieveCoPilotedContext(
            bot = botDay5,
            recentMessages = listOf(
                MessageEntity("m1", botId, "user", "Dünkü sarımsaklı makarnadan tekrar yap", 1000L)
            ),
            aiSelector = null
        )

        assertTrue(bundle.temporalTargetMatched)
        assertTrue(
            "Relevant ephemeral meal must outrank unrelated ephemeral jacket from the same day",
            bundle.facts.firstOrNull()?.value?.contains("makarna") == true
        )
    }

    @Test
    fun dynamicAvgDocLenAndSeparateRemoteDenseRrfChannel() = runBlocking {
        val botId = "bot_rag_avglen"
        repo.upsertNeuralVaultFact(
            botId = botId,
            subject = "Natasha",
            key = "Sır",
            value = "Eski laboratuvar dosyasını şifreli kasada saklıyor",
            tier = MemoryTier.CORE,
            importance = 9,
            storyDay = 1L
        )
        val index = repo.managers.memoryRetrievalManager.getOrBuildBotIndex(botId)
        assertTrue("avgDocLen should be computed dynamically and positive", index.avgDocLen >= 4f)

        val localOnly = MemoryEngine.rrfScore(semRank = 1, lexRank = 1, entityRank = 1)
        val withRemote = MemoryEngine.rrfScore(semRank = 1, lexRank = 1, entityRank = 1, remoteDenseRank = 1)
        val beyondCutoff = MemoryEngine.rrfScore(semRank = 1, lexRank = 1, entityRank = 1, remoteDenseRank = MemoryEngine.REMOTE_DENSE_RANK_CUTOFF + 5)

        assertTrue("Remote dense rank 1 should boost score cleanly", withRemote > localOnly)
        assertEquals("Remote dense rank beyond cutoff must not contribute", localOnly, beyondCutoff, 1e-6f)
    }

    @Test
    fun relativeTimeCueResolverUnicodeBoundariesAndNoWorldFalsePositive() {
        assertNull("'dünya' must not false-trigger yesterday", RelativeTimeCueResolver.resolveTemporalTarget("Bu dünya çok garip", 5L))

        val yest = RelativeTimeCueResolver.resolveTemporalTarget("Bana dünkinden yap", 5L)
        assertNotNull(yest)
        assertEquals(4L, yest!!.targetStoryDayMin)
        assertEquals(4L, yest.targetStoryDayMax)

        val nDays = RelativeTimeCueResolver.resolveTemporalTarget("3 gün önce ne konuşmuştuk?", 10L)
        assertNotNull(nDays)
        assertEquals(6L, nDays!!.targetStoryDayMin)
        assertEquals(8L, nDays.targetStoryDayMax)

        val lastWeek = RelativeTimeCueResolver.resolveTemporalTarget("Geçen hafta ne oldu?", 15L)
        assertNotNull(lastWeek)
        assertEquals(5L, lastWeek!!.targetStoryDayMin)
        assertEquals(11L, lastWeek.targetStoryDayMax)

        val anaphoric = RelativeTimeCueResolver.resolveTemporalTarget("Aynısından bir tane daha", 5L)
        assertNotNull(anaphoric)
        assertEquals(4L, anaphoric!!.targetStoryDayMin)
        assertEquals(5L, anaphoric.targetStoryDayMax)
    }

    @Test
    fun trivialNoiseFilterUsesWordStartPrefixNotRawSubstring() {
        assertTrue(
            "'kanepe' must not false-match 'kan' marker on trivial sitting",
            MemoryEngine.isTrivialNoiseFact("Hareket", "Kanepeye doğru baktı ve pencereden dışarı baktı", importance = 4)
        )
        assertFalse(
            "Actual bleeding/wound must be preserved",
            MemoryEngine.isTrivialNoiseFact("Durum", "Elini kanayan yarasına bastırdı", importance = 4)
        )
        assertFalse(
            "Actual kiss must be preserved",
            MemoryEngine.isTrivialNoiseFact("An", "İlk kez yanağından öptü", importance = 4)
        )
    }

    @Test
    fun contradictionDetectorHandlesGroupsStrictCitiesPolarityNegationAndNumbers() {
        assertTrue(MemoryEngine.isContradictingText("Evli", "Boşandı"))
        assertTrue(MemoryEngine.isContradictingText("Doktor olarak çalışıyor", "Avukat oldu"))
        assertTrue(MemoryEngine.isContradictingText("Roma'da yaşıyor", "Paris'te yaşıyor"))
        assertFalse(
            "'romantik' must NOT false-match city 'roma'",
            MemoryEngine.isContradictingText("Çok romantik biri", "Paris'te yaşıyor")
        )
        assertTrue(MemoryEngine.isContradictingText("Kahveyi çok seviyor", "Kahveyi hiç sevmiyor"))
        assertTrue(MemoryEngine.isContradictingText("Geceleri dışarı çıkar", "Geceleri dışarı çıkar değil"))
        assertTrue(MemoryEngine.isContradictingText("25 yaşında", "30 yaşında"))
        assertFalse(MemoryEngine.isContradictingText("25 yaşında", "25 yaşında ve mühendis"))
    }

    @Test
    fun memoryToolSchemaUsesIntegerAndScannerHandlesNestedBracketsParensEscapedQuotes() {
        val saveTool = MemoryToolRegistry.CENTRAL_TOOLS.first { it.name == "save_memory" }
        assertEquals("integer", saveTool.properties["importance"]?.type)
        assertEquals("integer", saveTool.properties["ttl_days"]?.type)

        val geminiSave = MemoryToolRegistry.toGeminiTools().first().functionDeclarations.first { it.name == "save_memory" }
        assertEquals("INTEGER", geminiSave.parameters.properties["importance"]?.type)
        assertEquals("INTEGER", geminiSave.parameters.properties["ttl_days"]?.type)

        assertEquals(8, MemoryToolRegistry.parseIntArg(8.0, 5))
        assertEquals(3, MemoryToolRegistry.parseIntArg("3.0", 0))
        assertEquals(7, MemoryToolRegistry.parseIntArg("invalid", 7))

        val rawResponse = """
            Harika bir geceydi.
            [ACTIVE_MEMORY_CALL: save_memory(content="İstanbul (Kadıköy) [Gizli Oda] \"altın anahtar\" orada", category="fact", key="Anahtar", importance=9, ttl_days=0) | update_memory(memoryId="#12", newContent="Yeni bilgi (güncel)")]
        """.trimIndent()

        val parsed = MemoryToolRegistry.parseTextFallbackMemoryCalls(rawResponse)
        assertEquals(2, parsed.size)
        assertEquals("save_memory", parsed[0].name)
        assertEquals("""İstanbul (Kadıköy) [Gizli Oda] "altın anahtar" orada""", parsed[0].arguments["content"])
        assertEquals(9, MemoryToolRegistry.parseIntArg(parsed[0].arguments["importance"], 0))
        assertEquals("update_memory", parsed[1].name)
        assertEquals("#12", parsed[1].arguments["memoryId"])
        assertEquals("Yeni bilgi (güncel)", parsed[1].arguments["newContent"])
    }

    @Test
    fun vectorHelpersAreNanSafeAndDelegated() {
        val mgr = repo.managers.memoryRetrievalManager
        assertEquals(0f, mgr.cosineSimilarity(floatArrayOf(Float.NaN, 1f), floatArrayOf(1f, 1f)), 1e-6f)
        assertNull(MemoryEngine.parseEmbeddingFromJson("""[1.0, "nan"]"""))
        val vec = MemoryEngine.computeTurkishFeatureEmbedding("Natasha'nın gizli planı")
        val json = mgr.floatArrayToJson(vec)
        val roundTrip = mgr.jsonToFloatArray(json)
        assertEquals(vec.size, roundTrip.size)
        assertEquals(1f, mgr.cosineSimilarity(vec, roundTrip), 1e-4f)
        assertTrue(TurkishTextAnalyzer.overlapCoefficient(setOf("a", "b"), setOf("a", "b", "c")) == 1f)
    }
}
