package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.book.BookReadState
import com.example.book.BookTranscriptItem
import com.example.book.BookTranscriptStatsHelper
import com.example.book.RecordedDecision
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.managers.CustomApiQuickPresets
import com.example.data.managers.ProviderHealthState
import com.example.util.BackupRetentionAdvisor
import com.example.util.BookChoiceParser
import com.example.util.BotCategoryCountHelper
import com.example.util.BotDirectorNoteStore
import com.example.util.BotListSortOrder
import com.example.util.BotListSorter
import com.example.util.BotScratchpadStore
import com.example.util.ChatDraftStore
import com.example.util.ChatScrollPolicy
import com.example.util.CustomApiEndpointInfoHelper
import com.example.util.CustomApiNameAutoSuggester
import com.example.util.ProviderHealthSummaryFormatter
import com.example.util.StoryDialogueToNarrationRatioHelper
import com.example.util.StoryVocabularyRichnessHelper
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
class ChatStoresTelemetryAndBookStatsOverhaulTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        BackupRetentionAdvisor.clearRecord(context)
    }

    private fun makeBot(
        id: String,
        aiName: String,
        mode: String = "personal",
        universeName: String = ""
    ): BotEntity = BotEntity(
        id = id,
        mode = mode,
        aiName = aiName,
        aiPersonality = "Sakin",
        scenario = "Gece",
        universeName = universeName,
        keyCharactersJson = "[]",
        userCharName = "Eren",
        userCharDesc = "Gezgin",
        openingMessage = "Merhaba.",
        writingStyle = "novel",
        intensity = "normal"
    )

    // ── 1. ChatScrollPolicy ──────────────────────────────────────────────────

    @Test
    fun chatScrollPolicy_handlesClockReversalNegativeBoundsAndOverflowSafely() {
        assertFalse(
            ChatScrollPolicy.isAtBottom(
                totalItems = 10,
                canScrollForward = true,
                lastVisibleIndex = -1,
                lastVisibleEndPx = 100,
                viewportEndPx = 100
            )
        )
        assertTrue(
            ChatScrollPolicy.isAtBottom(
                totalItems = 5,
                canScrollForward = true,
                lastVisibleIndex = 4,
                lastVisibleEndPx = 1000,
                viewportEndPx = 1000,
                tolerancePx = -50
            )
        )
        assertNull(
            ChatScrollPolicy.computeBottomOverflowPx(
                totalItems = 5,
                lastVisibleIndex = -2,
                lastVisibleEndPx = 500,
                viewportEndPx = 400
            )
        )
        assertEquals(
            120f,
            ChatScrollPolicy.computeBottomOverflowPx(
                totalItems = 5,
                lastVisibleIndex = 4,
                lastVisibleEndPx = 1120,
                viewportEndPx = 1000
            )
        )
        // Backward wall-clock jump (nowMs < lastScrollMs) must NOT freeze auto-scroll
        assertTrue(
            ChatScrollPolicy.shouldAutoScrollOnStreamChunk(
                chunk = "Merhaba",
                isStickyToBottom = true,
                isUserScrollInProgress = false,
                nowMs = 1_000L,
                lastScrollMs = 5_000L
            )
        )
        assertFalse(
            ChatScrollPolicy.shouldAutoScrollOnStreamChunk(
                chunk = "Merhaba",
                isStickyToBottom = true,
                isUserScrollInProgress = true,
                nowMs = 10_000L,
                lastScrollMs = 1_000L
            )
        )
    }

    // ── 2. Per-bot Stores (Draft, Scratchpad, DirectorNote) ──────────────────

    @Test
    fun perBotStores_enforceBoundsControlCharSanitizationBlankClearAndLru() {
        // ChatDraftStore
        ChatDraftStore.saveDraft(context, "  bot_draft_1  ", "A".repeat(ChatDraftStore.MAX_DRAFT_CHARS + 500))
        assertEquals(ChatDraftStore.MAX_DRAFT_CHARS, ChatDraftStore.loadDraft(context, "bot_draft_1").length)
        assertTrue(ChatDraftStore.hasDraft(context, "bot_draft_1"))
        ChatDraftStore.saveDraft(context, "bot_draft_1", "   ")
        assertFalse(ChatDraftStore.hasDraft(context, "bot_draft_1"))
        ChatDraftStore.saveDraft(context, "   ", "ignored")
        assertEquals("", ChatDraftStore.loadDraft(context, "   "))

        // BotScratchpadStore
        BotScratchpadStore.saveNote(context, "bot_pad_1", "B".repeat(BotScratchpadStore.MAX_NOTE_CHARS + 200))
        assertEquals(BotScratchpadStore.MAX_NOTE_CHARS, BotScratchpadStore.loadNote(context, "bot_pad_1").length)
        assertTrue(BotScratchpadStore.hasNote(context, "bot_pad_1"))
        BotScratchpadStore.clearNote(context, "bot_pad_1")
        assertFalse(BotScratchpadStore.hasNote(context, "bot_pad_1"))

        // BotDirectorNoteStore strips invisible control chars while preserving newlines
        BotDirectorNoteStore.saveRule(context, "bot_dir_1", "  \u0000Türkçe\u0007 yaz.\nKısa tut.  ")
        assertEquals("Türkçe yaz.\nKısa tut.", BotDirectorNoteStore.loadRule(context, "bot_dir_1"))
        assertTrue(BotDirectorNoteStore.hasRule(context, "bot_dir_1"))
        BotDirectorNoteStore.saveRule(context, "bot_dir_1", "\u0000\u0007   ")
        assertFalse(BotDirectorNoteStore.hasRule(context, "bot_dir_1"))
    }

    // ── 3. ProviderHealthSummaryFormatter ────────────────────────────────────

    @Test
    fun providerHealthSummaryFormatter_redactsSecretsAndBreaksLatencyTiesDeterministically() {
        val now = 10_000L
        val states = mapOf(
            "" to ProviderHealthState(providerKey = "", lastLatencyMs = 50L),
            "groq" to ProviderHealthState(providerKey = "groq", lastLatencyMs = 240L, cooldownUntilMs = 0L),
            "deepseek" to ProviderHealthState(providerKey = "deepseek", lastLatencyMs = 240L, cooldownUntilMs = 0L),
            "https://user:secret@api.custom.dev/v1?api_key=sk-abc123456789" to ProviderHealthState(
                providerKey = "custom",
                lastLatencyMs = 110L,
                cooldownUntilMs = 0L
            ),
            "openrouter" to ProviderHealthState(providerKey = "openrouter", lastLatencyMs = 80L, cooldownUntilMs = now + 30_000L)
        )

        val summary = ProviderHealthSummaryFormatter.summarize(states, nowMs = now)
        assertEquals(4, summary.totalTracked)
        assertEquals(3, summary.healthyCount)
        assertEquals(1, summary.cooldownCount)
        assertNotNull(summary.fastestProviderKey)
        assertFalse("Credentials must never leak into summary key", summary.fastestProviderKey!!.contains("secret"))
        assertFalse("Query API key must never leak into summary key", summary.fastestProviderKey!!.contains("sk-abc"))

        val banner = ProviderHealthSummaryFormatter.formatBanner(summary)
        assertFalse(banner.contains("secret"))
        assertFalse(banner.contains("sk-abc"))

        // Tie-breaking between groq & deepseek (both 240 ms) picks alphabetically first "deepseek"
        val tied = ProviderHealthSummaryFormatter.summarize(
            mapOf(
                "groq" to ProviderHealthState(providerKey = "groq", lastLatencyMs = 240L),
                "deepseek" to ProviderHealthState(providerKey = "deepseek", lastLatencyMs = 240L)
            ),
            nowMs = now
        )
        assertEquals("deepseek", tied.fastestProviderKey)
    }

    // ── 4. StoryDialogueToNarrationRatioHelper & StoryVocabularyRichnessHelper ─

    @Test
    fun storyDialogueAndVocabularyHelpers_stripHiddenReasoningAndStateJsonTags() {
        val narrationWithHiddenJson = """
            <think>Internal reasoning with "quoted English words" and alpha beta gamma delta</think>
            Yağmur pencere camına usulca vururken eski mahzenin taş duvarları soğuk bir sessizliğe bürünmüştü.
            [[STATE_JSON {"dominant_emotion":"melankolik","why":"rainy mood","aff":65}]]
            [[MEMORY_SAVE {"subject":"Aiden","key":"Location","value":"Mahzen"}]]
        """.trimIndent()

        val msg = MessageEntity(id = "m1", botId = "b1", role = "assistant", text = narrationWithHiddenJson)
        val ratio = StoryDialogueToNarrationRatioHelper.analyze(listOf(msg))
        assertEquals("Quoted JSON keys/values inside [[STATE_JSON]] must NOT inflate dialogue %", 0, ratio.dialoguePercent)
        assertEquals(100, ratio.narrationPercent)
        assertEquals("📖 Betimleyici Anlatı", ratio.styleLabel)

        val vocab = StoryVocabularyRichnessHelper.analyze(listOf(msg))
        assertTrue(vocab.totalWords in 8..16)
        assertTrue("Hidden tags must be excluded from total word count", vocab.totalWords < 20)

        // Turkish İ/i folding in vocabulary richness: "İstanbul" and "istanbul" count as ONE unique word
        val trMsg = MessageEntity(id = "m2", botId = "b1", role = "assistant", text = "İstanbul istanbul İSTANBUL")
        val trVocab = StoryVocabularyRichnessHelper.analyze(listOf(trMsg))
        assertEquals(3, trVocab.totalWords)
        assertEquals(1, trVocab.uniqueWords)
    }

    // ── 5. CustomApiEndpointInfoHelper, AutoSuggester & QuickPresets ─────────

    @Test
    fun customApiEndpointInfoSuggesterAndPresets_handleSecuritySchemesLocalHostsAndTurkishLocale() {
        // Non-HTTP(S) schemes and whitespace must be rejected as invalid
        assertEquals("Geçersiz URL", CustomApiEndpointInfoHelper.inspect("ftp://example.com/v1").badgeText)
        assertEquals("Geçersiz URL", CustomApiEndpointInfoHelper.inspect("javascript:alert(1)").badgeText)
        assertEquals("Geçersiz URL", CustomApiEndpointInfoHelper.inspect("https://bad host.com/v1").badgeText)

        // Private/local hosts including IPv6 loopback, 0.0.0.0, and RFC1918 172.16-31
        assertTrue(CustomApiEndpointInfoHelper.inspect("http://[::1]:11434/v1").isLocalServer)
        assertTrue(CustomApiEndpointInfoHelper.inspect("http://0.0.0.0:1234/v1").isLocalServer)
        assertTrue(CustomApiEndpointInfoHelper.inspect("http://172.20.10.4:11434/v1").isLocalServer)
        assertFalse(CustomApiEndpointInfoHelper.inspect("https://172.32.0.1/v1").isLocalServer)

        // Secret stripping from URLs
        val sanitized = CustomApiEndpointInfoHelper.sanitizeUrlForDisplay(
            "https://user:topsecret@api.openai.com/v1/chat?api_key=sk-secret999#frag"
        )
        assertEquals("https://api.openai.com/v1/chat", sanitized)
        assertFalse(sanitized.contains("topsecret"))
        assertFalse(sanitized.contains("sk-secret"))

        // Turkish locale safety: ASCII hostname starting with 'i' capitalizes to 'I', NOT Turkish 'İ'!
        assertEquals("Inference", CustomApiNameAutoSuggester.suggestLabel("https://inference.mycloud.io/v1"))
        assertEquals("Yerel Ollama", CustomApiNameAutoSuggester.suggestLabel("http://192.168.1.5:11434/v1"))
        assertEquals("LM Studio", CustomApiNameAutoSuggester.suggestLabel("http://127.0.0.1:1234/v1"))

        // Quick presets integrity & lookup
        val ids = CustomApiQuickPresets.presets.map { it.id }
        assertEquals("Preset IDs must be unique", ids.size, ids.toSet().size)
        assertEquals("openrouter", CustomApiQuickPresets.findById("OPENROUTER")?.id)
        assertEquals("groq", CustomApiQuickPresets.findMatchingPreset("https://api.groq.com/openai/v1?key=abc")?.id)
        assertEquals("ollama", CustomApiQuickPresets.findMatchingPreset("http://127.0.0.1:11434/v1")?.id)
    }

    // ── 6. BotCategoryCountHelper, BotListSorter & BackupRetentionAdvisor ────

    @Test
    fun libraryCountsTurkishSortOrderAndBackupRetentionAdvisor_behaveDeterministically() {
        val bots = listOf(
            makeBot(id = "b_z", aiName = "Zeynep"),
            makeBot(id = "b_c", aiName = "Çınar"),
            makeBot(id = "b_i", aiName = "İrem"),
            makeBot(id = "b_u", aiName = "Anlatıcı", mode = "UNIVERSE", universeName = "Aether"),
            makeBot(id = "b_i", aiName = "İrem") // duplicate ID
        )

        val counts = BotCategoryCountHelper.compute(
            bots = bots,
            favoriteIds = setOf("b_i", "b_u"),
            draftBotIds = setOf("b_z")
        )
        assertEquals("Duplicate bot ID must not double-count total", 4, counts.total)
        assertEquals(1, counts.universe)
        assertEquals(3, counts.personal)
        assertEquals(2, counts.favorites)
        assertEquals(1, counts.withDrafts)

        val uniqueBots = bots.distinctBy { it.id }
        val sortedAz = BotListSorter.sortBots(uniqueBots, BotListSortOrder.NAME_AZ, emptySet(), emptySet())
        assertEquals("b_u", sortedAz.first().id) // "Aether" first
        assertEquals("b_z", sortedAz.last().id)  // "Zeynep" last

        val sortedFavs = BotListSorter.sortBots(uniqueBots, BotListSortOrder.FAVORITES_FIRST, setOf("b_z"), emptySet())
        assertEquals("b_z", sortedFavs.first().id)

        // BackupRetentionAdvisor
        val initial = BackupRetentionAdvisor.getStatus(context, nowMs = 1_000_000L)
        assertTrue(initial.isReminderDue)
        assertEquals(0L, initial.lastBackupMs)

        // Non-positive timestamp must be ignored
        BackupRetentionAdvisor.recordBackup(context, nowMs = 0L)
        assertEquals(0L, BackupRetentionAdvisor.getStatus(context, nowMs = 1_000_000L).lastBackupMs)

        val t0 = 1_700_000_000_000L
        BackupRetentionAdvisor.recordBackup(context, nowMs = t0)
        val fresh = BackupRetentionAdvisor.getStatus(context, nowMs = t0 + 3_600_000L)
        assertFalse(fresh.isReminderDue)
        val stale = BackupRetentionAdvisor.getStatus(context, nowMs = t0 + 8L * 86_400_000L)
        assertTrue(stale.isReminderDue)
    }

    // ── 7. BookTranscriptStatsHelper & BookChoiceParser ──────────────────────

    @Test
    fun bookTranscriptStatsAndChoiceParser_stripHiddenTagsAndFoldTurkishBadges() {
        val decision = RecordedDecision(
            chapterNumber = 1,
            nodeId = "n1",
            decisionTitle = "Kapı",
            choiceId = "c1",
            selectedOption = "Kapıyı sessizce aç <think>internal secret</think>",
            impactText = "İçeri adım attın [[STATE_JSON {\"dom\":\"gergin\"}]]",
            bondDeltas = emptyMap(),
            fatigueDelta = 0,
            mysteryDelta = 1,
            secretUnlocked = null,
            branchTag = null,
            isBranching = false,
            isRelationship = false,
            impactLevel = "low"
        )
        val state = BookReadState(
            bookId = "book1",
            activeChapter = 1,
            completedChapters = emptySet(),
            currentNodeId = "n1",
            transcript = listOf(
                BookTranscriptItem.SceneBlock(
                    nodeId = "n1",
                    sceneOrder = 1,
                    sceneTitle = "Başlangıç",
                    locationTag = "Kule",
                    paragraphs = listOf(
                        "<thought>hidden reasoning words</thought> Gece sessizdi ve rüzgar esiyordu [[MEMORY_SAVE {\"x\":1}]]"
                    )
                ),
                BookTranscriptItem.DecisionBlock(decision)
            ),
            chapterDecisions = emptyMap(),
            bonds = emptyMap(),
            fatigue = 0,
            mystery = 0,
            flags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet(),
            isChapterCompleted = false
        )

        val stats = BookTranscriptStatsHelper.compute(state)
        // Visible words: "Gece sessizdi ve rüzgar esiyordu" (5) + "Kapıyı sessizce aç" (3) + "İçeri adım attın" (3) = 11
        assertEquals(11, stats.totalWords)
        assertEquals(1, stats.estimatedPages)
        assertEquals(2, stats.entryCount)
        assertEquals("11 kelime (~1 sayfa)", BookTranscriptStatsHelper.formatCompact(stats))

        // BookChoiceParser Turkish İ/i badge deduplication
        val parsedChoice = BookChoiceParser.parseChoiceDisplay(
            rawLabel = "[İçgüdü] [içgüdü] Karanlığa doğru ilerle",
            toneHint = "İçgüdü"
        )
        assertEquals("Karanlığa doğru ilerle", parsedChoice.displayText)
        assertEquals("Turkish İ/i duplicate badges must collapse to 1", 1, parsedChoice.badges.size)
    }
}
