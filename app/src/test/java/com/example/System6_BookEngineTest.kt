package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.book.BookCatalogProvider
import com.example.book.BookChapterMeta
import com.example.book.BookCharacterId
import com.example.book.BookChoiceDef
import com.example.book.BookChoiceEffect
import com.example.book.BookRegistryHub
import com.example.book.BookSceneNode
import com.example.book.BookStateRepository
import com.example.book.BookTranscriptItem
import com.example.book.CosmicDriftBookRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class System6_BookEngineTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("aetheria_book_engine_state_v4", Context.MODE_PRIVATE)
            .edit().clear().commit()
        BookStateRepository.resetForTesting()
    }

    @After
    fun tearDown() {
        BookRegistryHub.unregister("custom_test_book")
        BookStateRepository.resetForTesting()
    }

    @Test
    fun normalizeDeltaScalesStandardAndWatershedBoundsAccurately() {
        assertEquals(0, BookStateRepository.normalizeDelta(0, isWatershed = false))
        assertEquals(2, BookStateRepository.normalizeDelta(2, isWatershed = false))
        assertEquals(3, BookStateRepository.normalizeDelta(8, isWatershed = false))
        assertEquals(3, BookStateRepository.normalizeDelta(10, isWatershed = false))
        assertEquals(4, BookStateRepository.normalizeDelta(10, isWatershed = true))
        assertEquals(4, BookStateRepository.normalizeDelta(20, isWatershed = false))
        assertEquals(5, BookStateRepository.normalizeDelta(20, isWatershed = true))
        assertEquals(-5, BookStateRepository.normalizeDelta(-20, isWatershed = true))
    }

    @Test
    fun lightweightScrollPersistenceRestoresWithoutRewritingTranscriptJson() {
        val bookId = CosmicDriftBookRegistry.BOOK_ID
        val initial = BookStateRepository.getState(context, bookId)
        assertEquals(1, initial.activeChapter)

        // Advance one choice so state JSON exists in prefs
        val startNode = CosmicDriftBookRegistry.node(initial.currentNodeId)!!
        assertTrue(startNode.choices.isNotEmpty())
        val afterChoice = BookStateRepository.selectChoice(context, startNode.choices.first().id, bookId)
        assertTrue(afterChoice.transcript.size >= 3)

        val prefs = context.getSharedPreferences("aetheria_book_engine_state_v4", Context.MODE_PRIVATE)
        val jsonBeforeScroll = prefs.getString("state_$bookId", null)
        assertNotNull(jsonBeforeScroll)

        // Save scroll position -> must write lightweight Int keys WITHOUT touching state JSON
        BookStateRepository.saveScrollPosition(context, firstVisibleIndex = 14, firstVisibleOffset = 220, bookId = bookId)
        assertEquals(jsonBeforeScroll, prefs.getString("state_$bookId", null))
        assertEquals(14, prefs.getInt("scroll_idx_$bookId", -1))
        assertEquals(220, prefs.getInt("scroll_off_$bookId", -1))

        // Cold reload restores exact scroll position
        BookStateRepository.resetForTesting()
        val reloaded = BookStateRepository.getState(context, bookId)
        assertEquals(14, reloaded.savedScrollIndex)
        assertEquals(220, reloaded.savedScrollOffset)
        assertEquals(afterChoice.currentNodeId, reloaded.currentNodeId)
    }

    @Test
    fun restartCurrentChapterRestoresSnapshotAndPreventsStatFarming() {
        val bookId = CosmicDriftBookRegistry.BOOK_ID
        val initial = BookStateRepository.getState(context, bookId)
        val initialBonds = initial.bonds

        val startNode = CosmicDriftBookRegistry.node(initial.currentNodeId)!!
        val afterChoice = BookStateRepository.selectChoice(context, startNode.choices.first().id, bookId)
        assertTrue(afterChoice.currentChapterDecisions.isNotEmpty())

        // Restart chapter -> bonds, flags & chapterDecisions must cleanly revert to pre-chapter snapshot
        val restarted = BookStateRepository.restartCurrentChapter(context, bookId)
        assertEquals(initial.currentNodeId, restarted.currentNodeId)
        assertEquals(initialBonds, restarted.bonds)
        assertTrue(restarted.currentChapterDecisions.isEmpty())
        assertEquals(0, restarted.savedScrollIndex)
        assertFalse(restarted.isChapterCompleted)
    }

    @Test
    fun multiBookRegistryHubResolvesCustomDeterministicBookAndRecoversCorruptedJson() {
        val customBookId = "custom_test_book"
        val customProvider = object : BookCatalogProvider {
            override val bookId: String = customBookId
            override val totalPlayableChapters: Int = 1
            override val chapters: List<BookChapterMeta> = listOf(
                BookChapterMeta(
                    number = 1,
                    romanNumeral = "I",
                    title = "Bölüm 1: Yeni Evren",
                    titleEn = "Chapter 1: New Universe",
                    subtitle = "Test başlangıcı",
                    description = "Test bölümü açıklaması",
                    descriptionEn = "Test chapter description",
                    estimatedMinutes = 5,
                    accentHex = 0xFF9333EAL,
                    startNodeId = "tb_s1",
                    sceneCount = 2,
                    decisionCount = 1
                )
            )
            override val allNodes: Map<String, BookSceneNode> = mapOf(
                "tb_s1" to BookSceneNode(
                    id = "tb_s1",
                    chapterNumber = 1,
                    sceneOrder = 1,
                    sceneTitle = "Başlangıç",
                    locationTag = "Kule",
                    paragraphs = listOf("Kule sessizdi."),
                    decisionTitle = "İlk Karar",
                    choices = listOf(
                        BookChoiceDef(
                            id = "tb_c1",
                            label = "Kapıyı aç",
                            toneHint = "Cesur",
                            nextNodeId = "tb_s2",
                            effect = BookChoiceEffect(
                                impactSummary = "Kapı açıldı.",
                                bondDeltas = mapOf(BookCharacterId.NATASHA to 3),
                                setFlags = mapOf("door_opened" to "yes")
                            )
                        )
                    )
                ),
                "tb_s2" to BookSceneNode(
                    id = "tb_s2",
                    chapterNumber = 1,
                    sceneOrder = 2,
                    sceneTitle = "Bölüm Sonu",
                    locationTag = "Salon",
                    paragraphs = listOf("İçerisi aydınlıktı."),
                    isChapterEnd = true,
                    chapterEndQuote = "Her kapı yeni bir başlangıçtır."
                )
            )
        }

        BookRegistryHub.register(customProvider)
        assertTrue(CosmicDriftBookRegistry.supportsDeterministicEngine(customBookId))

        val s1 = BookStateRepository.getState(context, customBookId)
        assertEquals("tb_s1", s1.currentNodeId)

        val s2 = BookStateRepository.selectChoice(context, "tb_c1", customBookId)
        assertEquals("tb_s2", s2.currentNodeId)
        assertEquals("yes", s2.flags["door_opened"])
        assertTrue(s2.isChapterCompleted)
        assertTrue(s2.transcript.any { it is BookTranscriptItem.DecisionBlock })

        // Corrupt persisted JSON -> cold reload recovers gracefully to Chapter 1 Scene 1 without crashing
        context.getSharedPreferences("aetheria_book_engine_state_v4", Context.MODE_PRIVATE)
            .edit().putString("state_$customBookId", "{corrupt_json").commit()
        BookStateRepository.resetForTesting()

        val recovered = BookStateRepository.getState(context, customBookId)
        assertEquals("tb_s1", recovered.currentNodeId)
        assertFalse(recovered.isChapterCompleted)
    }
}
