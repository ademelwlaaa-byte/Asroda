package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.book.BookChapterMeta
import com.example.book.BookCharacterId
import com.example.book.BookChoiceDef
import com.example.book.BookReadState
import com.example.book.BookStateRepository
import com.example.book.BookTranscriptItem
import com.example.ui.screens.cosmic3d.CosmicRooftopBossDuelDialog
import com.example.book.CosmicDriftBookRegistry
import com.example.book.RecordedDecision
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.ui.animation.CosmicConfettiOverlay
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.CosmicSparkleBurst
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.cosmicTapSpring
import com.example.ui.components.AppBackground
import com.example.ui.components.MorCamTokens
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import com.example.util.BookAudioController
import com.example.util.BookChoiceParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Flat LazyColumn manuscript row model — enables exact paragraph-level scroll save/restore,
 * zero-overlap in-flow choice auto-reveal, and smooth scroll-to-new-scene-top after decisions.
 */
private sealed class ManuscriptRow(val key: String) {
    data class Cover(val meta: BookChapterMeta) :
        ManuscriptRow("chapter_cover_${meta.number}")

    data class SceneHeader(
        val transcriptIdx: Int,
        val nodeId: String,
        val sceneOrder: Int,
        val sceneTitle: String,
        val locationTag: String,
        val isFirstScene: Boolean
    ) : ManuscriptRow("scene_hdr_${transcriptIdx}_$nodeId")

    data class Paragraph(
        val transcriptIdx: Int,
        val nodeId: String,
        val paragraphIdx: Int,
        val text: String,
        val useDropCap: Boolean
    ) : ManuscriptRow("scene_p_${transcriptIdx}_${nodeId}_$paragraphIdx")

    data class Echo(
        val transcriptIdx: Int,
        val nodeId: String,
        val echoIdx: Int,
        val label: String,
        val body: String
    ) : ManuscriptRow("scene_echo_${transcriptIdx}_${nodeId}_$echoIdx")

    data class ClosingQuote(
        val transcriptIdx: Int,
        val nodeId: String,
        val quote: String
    ) : ManuscriptRow("scene_quote_${transcriptIdx}_$nodeId")

    data class DecisionSeal(
        val transcriptIdx: Int,
        val decision: RecordedDecision
    ) : ManuscriptRow("decision_seal_${transcriptIdx}_${decision.choiceId}")

    data class ChoiceAnchor(
        val nodeId: String,
        val prompt: String,
        val choices: List<BookChoiceDef>
    ) : ManuscriptRow("choice_anchor_$nodeId")

    data class ChapterComplete(
        val chapterNumber: Int
    ) : ManuscriptRow("chapter_complete_$chapterNumber")
}

/**
 * Collector's-Edition Mor Cam Interactive Book Reader.
 * - Resiliently binds to deterministic zero-latency BookStateRepository engine for "Kozmik Sürükleniş".
 * - Starts fresh/restarted chapters strictly at top (index 0, offset 0) and restores exact scroll
 *   position whenever the reader exits and returns mid-chapter.
 * - Auto-reveals the in-flow Choice Sanctuary right beneath the last paragraph the instant the
 *   reader scrolls to the decision point — never overlapping text.
 * - Distinct illuminated styling for quoted dialogue ("..." / “...”).
 */
@Composable
fun BookReaderView(
    bot: BotEntity,
    messages: List<MessageEntity>,
    isSending: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onEnsureOpeningMessage: () -> Unit,
    onResetChat: (Boolean) -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val motionConfig = LocalCosmicMotionConfig.current

    // Resilient detector: works for canonical preset_aiden_mcu_cosmic AND legacy Room copies
    val useDeterministicEngine = remember(bot.id, bot.aiName, bot.universeName) {
        CosmicDriftBookRegistry.supportsDeterministicEngine(bot)
    }

    val engineStateFlow = remember(bot.id) {
        BookStateRepository.stateFlow(context, CosmicDriftBookRegistry.BOOK_ID)
    }
    val engineState by engineStateFlow.collectAsState()

    // Nonce incremented on chapter restart/switch so LazyListState cleanly resets to (0, 0)
    var chapterResetNonce by remember { mutableIntStateOf(0) }

    // Persisted Reader Preferences (font size & optional ambient soundtrack!)
    val readerPrefs = remember(context) {
        context.getSharedPreferences("cosmic_book_reader_prefs", Context.MODE_PRIVATE)
    }
    var fontSizeSp by remember {
        mutableFloatStateOf(readerPrefs.getFloat("font_size_sp", 16.8f).coerceIn(14f, 22.5f))
    }
    var ambientMusicEnabled by remember { mutableStateOf(false) }
    var activeVoiceCardKey by remember { mutableStateOf<String?>(null) }
    val readerVoiceEngine = remember(context) {
        com.example.ui.screens.cosmic3d.CosmicCharacterVoiceEngine(context) { speaking ->
            com.example.util.BookAudioController.setVoiceDucking(speaking)
            if (!speaking) {
                activeVoiceCardKey = null
            }
        }
    }
    DisposableEffect(readerVoiceEngine) {
        onDispose {
            com.example.util.BookAudioController.setVoiceDucking(false)
            readerVoiceEngine.stopAndRelease()
        }
    }
    var showChapterSummaryModal by remember { mutableStateOf(false) }
    var showTwinAidenPostCredits by remember { mutableStateOf(false) }
    var showSeason2PostCredits by remember { mutableStateOf(false) }
    var showChronicleModal by remember { mutableStateOf(false) }
    var showChapterSwitcher by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showQuickMandatoryDuel by remember { mutableStateOf(false) }

    // Stop any playing scene voice card cleanly on chapter/scene transitions or modal switches
    LaunchedEffect(
        engineState.activeChapter,
        engineState.currentNodeId,
        showChapterSummaryModal,
        showTwinAidenPostCredits,
        showSeason2PostCredits,
        showQuickMandatoryDuel
    ) {
        readerVoiceEngine.stopCurrentLine()
        activeVoiceCardKey = null
    }

    // Background-prewarm upcoming voice cards (COMMS/AUDIO/MIND) in 100% free Edge Neural voices
    // so tapping any dialogue card plays instantaneously with 0ms synthesis delay.
    LaunchedEffect(engineState.activeChapter, engineState.currentNodeId, engineState.transcript.size) {
        val recentParagraphs = engineState.transcript
            .takeLast(2)
            .filterIsInstance<BookTranscriptItem.SceneBlock>()
            .flatMap { it.paragraphs }
        val voiceEntries = extractBookVoiceCardEntries(recentParagraphs).takeLast(4)
        if (voiceEntries.isNotEmpty()) {
            readerVoiceEngine.prewarmLines(voiceEntries)
        }
    }

    // Instant consequence banner + newly unlocked character toast + sparkle/confetti + cosmic screen edge pulse triggers
    var lastDecisionBanner by remember { mutableStateOf<RecordedDecision?>(null) }
    var showDecisionBanner by remember { mutableStateOf(false) }
    var knownUnlockedChars by remember {
        mutableStateOf(BookStateRepository.unlockedCharacters(engineState).toSet())
    }
    var newlyUnlockedCharBanner by remember { mutableStateOf<BookCharacterId?>(null) }
    var showCharUnlockBanner by remember { mutableStateOf(false) }
    var lastChoiceAppliedAtMs by remember { mutableStateOf(0L) }
    var choiceSparkTrigger by remember { mutableIntStateOf(0) }
    var chapterCompleteConfetti by remember { mutableIntStateOf(0) }
    var choicePulseTrigger by remember { mutableIntStateOf(0) }
    var choicePulseColor by remember { mutableStateOf(Color(0xFF38BDF8)) }

    // Persisted mandatory mini-game completion tracking (unlocks choices only after winning!)
    val miniGamePrefs = remember(context) {
        context.getSharedPreferences("cosmic_book_minigames", Context.MODE_PRIVATE)
    }
    var completedMiniGameNodes by remember {
        mutableStateOf(
            miniGamePrefs.getStringSet("completed_nodes", emptySet())?.toSet().orEmpty()
        )
    }
    var isFullscreenMode by remember { mutableStateOf(false) }
    BackHandler(enabled = isFullscreenMode) {
        isFullscreenMode = false
    }

    fun markMiniGameCompleted(nodeId: String) {
        if (nodeId !in completedMiniGameNodes) {
            val updated = completedMiniGameNodes + nodeId
            completedMiniGameNodes = updated
            miniGamePrefs.edit().putStringSet("completed_nodes", updated).apply()
            choicePulseColor = Color(0xFF34D399)
            choicePulseTrigger += 1
            choiceSparkTrigger += 1
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun clearMiniGameForChapter(chapterNum: Int) {
        val prefix = "ch${chapterNum}_"
        val updated = completedMiniGameNodes.filterNot { it.startsWith(prefix) }.toSet()
        completedMiniGameNodes = updated
        miniGamePrefs.edit().putStringSet("completed_nodes", updated).apply()
    }

    DisposableEffect(Unit) {
        onDispose {
            // Preserve smooth 2-second fade-out when leaving reader instead of hard-cutting audio
            BookAudioController.stopIfNotAlreadyFading(fadeOutMs = 2000L)
        }
    }

    LaunchedEffect(bot.id, messages.size, useDeterministicEngine) {
        if (!useDeterministicEngine && messages.isEmpty()) {
            onEnsureOpeningMessage()
        }
    }

    val activeChapterMeta = remember(engineState.activeChapter) {
        CosmicDriftBookRegistry.chapterMeta(engineState.activeChapter)
    }
    val currentSceneNode = remember(engineState.currentNodeId) {
        CosmicDriftBookRegistry.node(engineState.currentNodeId)
    }
    val deterministicChoices = remember(currentSceneNode) {
        currentSceneNode?.choices.orEmpty()
    }
    val currentNodeMiniGameTag = remember(currentSceneNode) {
        currentSceneNode?.paragraphs
            ?.map { it.trim() }
            ?.firstOrNull { it.startsWith("[[GAME3D|") && it.endsWith("]]") }
    }
    val currentNodeRequiresMiniGame = currentNodeMiniGameTag != null
    val currentNodeBossInfo = remember(currentNodeMiniGameTag) {
        val parts = currentNodeMiniGameTag
            ?.removePrefix("[[")
            ?.removeSuffix("]]")
            ?.split("|")
            .orEmpty()
        val bossId = com.example.ui.screens.cosmic3d.CosmicDuelBossId.fromTag(parts.getOrNull(1).orEmpty())
        val bossTitle = parts.getOrNull(2)?.trim().orEmpty().ifBlank {
            when (bossId) {
                com.example.ui.screens.cosmic3d.CosmicDuelBossId.MIRROR_ZERO -> "AYNA DÜELLOSU · KULE TERASI"
                com.example.ui.screens.cosmic3d.CosmicDuelBossId.ULTRON_PRIME -> "DEMİR VE YANKI · SOKOVİA MEYDANI"
                com.example.ui.screens.cosmic3d.CosmicDuelBossId.VAELOR_ROOFTOP -> "SON YÜZLEŞME · AVENGERS KULESİ ÇATISI"
            }
        }
        bossId to bossTitle
    }

    // Build flat manuscript rows for exact scroll save/restore & in-flow choice reveal
    val manuscriptRows = remember(
        engineState.activeChapter,
        engineState.transcript,
        engineState.isChapterCompleted,
        engineState.currentNodeId,
        deterministicChoices,
        currentSceneNode
    ) {
        buildList {
            add(ManuscriptRow.Cover(activeChapterMeta))
            engineState.transcript.forEachIndexed { tIdx, item ->
                when (item) {
                    is BookTranscriptItem.SceneBlock -> {
                        val isFirstScene = tIdx == 0 && item.sceneOrder == 1
                        add(
                            ManuscriptRow.SceneHeader(
                                transcriptIdx = tIdx,
                                nodeId = item.nodeId,
                                sceneOrder = item.sceneOrder,
                                sceneTitle = item.sceneTitle,
                                locationTag = item.locationTag,
                                isFirstScene = isFirstScene
                            )
                        )
                        item.paragraphs.forEachIndexed { pIdx, paragraph ->
                            add(
                                ManuscriptRow.Paragraph(
                                    transcriptIdx = tIdx,
                                    nodeId = item.nodeId,
                                    paragraphIdx = pIdx,
                                    text = paragraph,
                                    useDropCap = isFirstScene && pIdx == 0
                                )
                            )
                        }
                        item.memoryEchoes.forEachIndexed { eIdx, (label, body) ->
                            add(
                                ManuscriptRow.Echo(
                                    transcriptIdx = tIdx,
                                    nodeId = item.nodeId,
                                    echoIdx = eIdx,
                                    label = label,
                                    body = body
                                )
                            )
                        }
                        if (item.isChapterEnd && !item.chapterEndQuote.isNullOrBlank()) {
                            add(
                                ManuscriptRow.ClosingQuote(
                                    transcriptIdx = tIdx,
                                    nodeId = item.nodeId,
                                    quote = item.chapterEndQuote
                                )
                            )
                        }
                    }

                    is BookTranscriptItem.DecisionBlock -> {
                        add(ManuscriptRow.DecisionSeal(tIdx, item.decision))
                    }
                }
            }
            if (!engineState.isChapterCompleted && deterministicChoices.isNotEmpty()) {
                add(
                    ManuscriptRow.ChoiceAnchor(
                        nodeId = engineState.currentNodeId,
                        prompt = currentSceneNode?.choicePrompt ?: "Kararını seç:",
                        choices = deterministicChoices
                    )
                )
            }
            if (engineState.isChapterCompleted) {
                add(ManuscriptRow.ChapterComplete(engineState.activeChapter))
            }
        }
    }

    // Create LazyListState keyed ONLY on chapter + resetNonce so it restores saved scroll
    // position on re-entry and starts strictly at (0, 0) on fresh/restarted chapters.
    val listState = remember(engineState.activeChapter, chapterResetNonce) {
        val maxIdx = (manuscriptRows.size - 1).coerceAtLeast(0)
        val restoredIdx = engineState.savedScrollIndex.coerceIn(0, maxIdx)
        val restoredOff = if (restoredIdx == 0 && engineState.savedScrollIndex == 0) {
            engineState.savedScrollOffset.coerceAtLeast(0)
        } else {
            engineState.savedScrollOffset.coerceAtLeast(0)
        }
        LazyListState(
            firstVisibleItemIndex = restoredIdx,
            firstVisibleItemScrollOffset = restoredOff
        )
    }

    // Continuously persist exact scroll position as the reader scrolls (true debounce via collectLatest)
    LaunchedEffect(listState, engineState.activeChapter, useDeterministicEngine) {
        if (!useDeterministicEngine) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collectLatest { (idx, off) ->
                delay(250)
                BookStateRepository.saveScrollPosition(
                    context = context,
                    firstVisibleIndex = idx,
                    firstVisibleOffset = off,
                    bookId = CosmicDriftBookRegistry.BOOK_ID
                )
            }
    }

    // Pause chapter-end soundtrack smoothly on background (ON_STOP 250ms fade) and resume on ON_START (300ms fade) so split-screen/dialogs don't cut music
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> BookAudioController.pauseForBackground(fadeOutMs = 250L)
                Lifecycle.Event.ON_START -> BookAudioController.resumeFromBackground(fadeInMs = 300L)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Also flush exact scroll position synchronously on exit / disposal
    DisposableEffect(listState, engineState.activeChapter, useDeterministicEngine) {
        onDispose {
            if (useDeterministicEngine) {
                BookStateRepository.saveScrollPosition(
                    context = context,
                    firstVisibleIndex = listState.firstVisibleItemIndex,
                    firstVisibleOffset = listState.firstVisibleItemScrollOffset,
                    bookId = CosmicDriftBookRegistry.BOOK_ID
                )
            }
        }
    }

    // Auto-reveal Choice Sanctuary the instant the reader reaches the bottom of the active scene
    var revealedChoiceNodes by remember(engineState.activeChapter, chapterResetNonce) {
        mutableStateOf(emptySet<String>())
    }
    val activeAnchorKey = "choice_anchor_${engineState.currentNodeId}"
    val isAnchorInViewport by remember(listState, activeAnchorKey) {
        derivedStateOf {
            val visible = listState.layoutInfo.visibleItemsInfo
            if (visible.isEmpty()) false
            else visible.any { it.key == activeAnchorKey }
        }
    }
    LaunchedEffect(isAnchorInViewport, engineState.currentNodeId) {
        if (isAnchorInViewport && engineState.currentNodeId !in revealedChoiceNodes) {
            revealedChoiceNodes = revealedChoiceNodes + engineState.currentNodeId
        }
    }

    // Track newly appended scene scroll target so choosing glides to the START of the new scene
    var pendingScrollToNodeId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(pendingScrollToNodeId, manuscriptRows.size) {
        val targetNode = pendingScrollToNodeId ?: return@LaunchedEffect
        val targetRowIdx = manuscriptRows.indexOfLast {
            it is ManuscriptRow.SceneHeader && it.nodeId == targetNode
        }.takeIf { it >= 0 } ?: manuscriptRows.indexOfLast {
            it is ManuscriptRow.DecisionSeal
        }
        if (targetRowIdx >= 0) {
            delay(90)
            try {
                // Scroll smoothly to the Decision Seal right above the new Scene Header (or Scene Header itself)
                val scrollTarget = (targetRowIdx - 1).coerceAtLeast(0)
                listState.animateScrollToItem(scrollTarget)
            } catch (_: Exception) {
            }
            pendingScrollToNodeId = null
        }
    }

    // Custom AI book fallback derived state
    val lastAssistantMsg = remember(messages) {
        messages.lastOrNull { it.role == "assistant" || it.role == "model" }
    }
    val customAiChoices = remember(lastAssistantMsg?.text, useDeterministicEngine) {
        if (!useDeterministicEngine && lastAssistantMsg != null) {
            BookChoiceParser.parseAiBookChoices(lastAssistantMsg.text)
        } else {
            emptyList()
        }
    }
    val customAiChapterComplete = remember(lastAssistantMsg?.text, customAiChoices, useDeterministicEngine) {
        if (useDeterministicEngine) false
        else {
            val txt = lastAssistantMsg?.text ?: ""
            customAiChoices.isEmpty() && (
                txt.contains("BÖLÜM SONU", ignoreCase = true) ||
                    txt.contains("TAMAMLANDI", ignoreCase = true) ||
                    txt.contains("ÖZET EKRANI", ignoreCase = true)
                )
        }
    }
    val isChapterCompleted = if (useDeterministicEngine) engineState.isChapterCompleted else customAiChapterComplete

    // Trigger subtle celebration confetti once when the chapter's closing scene arrives,
    // while letting the reader finish reading peacefully and tap "Bölümü Bitir ✦" to launch
    // the Life is Strange 2-style Chapter End Screen & soundtrack.
    var celebratedChapter by remember(chapterResetNonce) { mutableIntStateOf(-1) }
    LaunchedEffect(isChapterCompleted, engineState.activeChapter, chapterResetNonce) {
        if (isChapterCompleted && celebratedChapter != engineState.activeChapter) {
            celebratedChapter = engineState.activeChapter
            chapterCompleteConfetti += 1
        }
    }

    LaunchedEffect(lastDecisionBanner) {
        if (lastDecisionBanner != null) {
            showCharUnlockBanner = false
            showDecisionBanner = true
            delay(2700)
            showDecisionBanner = false
        }
    }

    // Detect when a brand-new character enters the story as the player advances scenes
    val currentUnlockedChars = remember(
        engineState.activeChapter,
        engineState.highestCompletedChapter,
        engineState.currentNodeId,
        engineState.numericFlags
    ) {
        BookStateRepository.unlockedCharacters(engineState).toSet()
    }
    LaunchedEffect(currentUnlockedChars) {
        val added = currentUnlockedChars - knownUnlockedChars
        if (added.isNotEmpty() && useDeterministicEngine) {
            knownUnlockedChars = currentUnlockedChars
            val newest = added.first()
            // Wait cleanly if decision consequence banner is currently showing so banners NEVER collide
            if (showDecisionBanner) {
                delay(2850)
            }
            newlyUnlockedCharBanner = newest
            showCharUnlockBanner = true
            delay(2800)
            showCharUnlockBanner = false
        } else {
            knownUnlockedChars = currentUnlockedChars
        }
    }

    // Real scene reading progress inside current chapter
    val currentSceneOrder = remember(engineState.transcript, currentSceneNode) {
        currentSceneNode?.sceneOrder
            ?: (engineState.transcript.lastOrNull { it is BookTranscriptItem.SceneBlock } as? BookTranscriptItem.SceneBlock)?.sceneOrder
            ?: 1
    }
    val sceneProgressFraction by animateFloatAsState(
        targetValue = if (useDeterministicEngine) {
            (currentSceneOrder.toFloat() / activeChapterMeta.sceneCount.coerceAtLeast(1)).coerceIn(0.08f, 1f)
        } else {
            (messages.count { it.role == "user" }.coerceAtLeast(1) / 5f).coerceIn(0.12f, 1f)
        },
        animationSpec = CosmicMotionSpecs.velvetGlass(),
        label = "bookSceneProgress"
    )

    // Ambient subtle shimmer & drifting stardust phase (respects Reduced Motion)
    val glowPulse: Float
    val stardustPhase: Float
    if (motionConfig.enableAmbientLoops) {
        val inf = rememberInfiniteTransition(label = "bookReaderAmbient")
        glowPulse = inf.animateFloat(
            initialValue = 0.68f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Reverse),
            label = "bookGlow"
        ).value
        stardustPhase = inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
            label = "bookStardust"
        ).value
    } else {
        glowPulse = 0.85f
        stardustPhase = 0f
    }

    val accentColor = Color(activeChapterMeta.accentHex)

    // ── Modals ──────────────────────────────────────────────────────────────
    if (showChapterSummaryModal) {
        ChapterSummaryModal(
            messages = messages,
            bookReadState = if (useDeterministicEngine) engineState else null,
            onDismiss = { showChapterSummaryModal = false },
            onExitToBooks = {
                showChapterSummaryModal = false
                if (useDeterministicEngine) {
                    BookStateRepository.saveScrollPosition(
                        context = context,
                        firstVisibleIndex = listState.firstVisibleItemIndex,
                        firstVisibleOffset = listState.firstVisibleItemScrollOffset,
                        bookId = CosmicDriftBookRegistry.BOOK_ID
                    )
                }
                onBack()
            },
            onRestartChapter = {
                showChapterSummaryModal = false
                if (useDeterministicEngine) {
                    BookStateRepository.restartCurrentChapter(context, CosmicDriftBookRegistry.BOOK_ID)
                    chapterResetNonce += 1
                } else {
                    onResetChat(false)
                }
            },
            onNextChapter = if (useDeterministicEngine && engineState.activeChapter < CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS) {
                {
                    showChapterSummaryModal = false
                    val nextCh = engineState.activeChapter + 1
                    BookStateRepository.jumpToChapter(context, nextCh, forceRestartIfSame = true)
                    chapterResetNonce += 1
                }
            } else null
        )
    }

    if (showChronicleModal && useDeterministicEngine) {
        BookChronicleModal(
            state = engineState,
            onDismiss = { showChronicleModal = false }
        )
    }

    if (showChapterSwitcher && useDeterministicEngine) {
        InReaderChapterSwitcherDialog(
            state = engineState,
            onDismiss = { showChapterSwitcher = false },
            onSelectChapter = { targetChapter ->
                showChapterSwitcher = false
                ambientMusicEnabled = false
                BookAudioController.stop(fadeOutMs = 2000L)
                val switching = targetChapter != engineState.activeChapter
                BookStateRepository.jumpToChapter(
                    context = context,
                    chapterNumber = targetChapter,
                    forceRestartIfSame = switching
                )
                if (switching) {
                    chapterResetNonce += 1
                }
            }
        )
    }

    if (showResetDialog) {
        BookResetOptionsDialog(
            chapterNumber = if (useDeterministicEngine) engineState.activeChapter else 1,
            onDismiss = { showResetDialog = false },
            onRestartChapter = {
                showResetDialog = false
                ambientMusicEnabled = false
                BookAudioController.stop(fadeOutMs = 2000L)
                if (useDeterministicEngine) {
                    clearMiniGameForChapter(engineState.activeChapter)
                    BookStateRepository.restartCurrentChapter(context, CosmicDriftBookRegistry.BOOK_ID)
                    chapterResetNonce += 1
                } else {
                    onResetChat(false)
                }
            },
            onResetEntireBook = {
                showResetDialog = false
                ambientMusicEnabled = false
                BookAudioController.stop(fadeOutMs = 2000L)
                if (useDeterministicEngine) {
                    completedMiniGameNodes = emptySet()
                    miniGamePrefs.edit().remove("completed_nodes").apply()
                    BookStateRepository.resetEntireBook(context, CosmicDriftBookRegistry.BOOK_ID)
                    chapterResetNonce += 1
                } else {
                    onResetChat(true)
                }
            }
        )
    }

    if (showTwinAidenPostCredits) {
        TwinAidenPostCreditsDialog(
            onContinueToChoicesSummary = {
                showTwinAidenPostCredits = false
                showChapterSummaryModal = true
            }
        )
    }

    if (showSeason2PostCredits) {
        Season2ArchitectPostCreditsDialog(
            onContinueToChoicesSummary = {
                showSeason2PostCredits = false
                showChapterSummaryModal = true
            }
        )
    }

    if (showQuickMandatoryDuel) {
        CosmicRooftopBossDuelDialog(
            title = currentNodeBossInfo.second,
            bossId = currentNodeBossInfo.first,
            pietroSaved = engineState.flags["s2_pietro_fate"] == "saved",
            onVictory = { _, _ ->
                markMiniGameCompleted(engineState.currentNodeId)
            },
            onDismiss = { showQuickMandatoryDuel = false }
        )
    }

    // ── Main Collector's-Edition Mor Cam Reading Stage ──────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        // Unified Cosmic Wisteria backdrop + deep nocturnal obsidian-amethyst reading veil
        AppBackground(activeTab = "chats")
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xD60C051C),
                            Color(0xE8080315),
                            Color(0xF0070312),
                            Color(0xE60D051F)
                        )
                    )
                )
        )

        // Subtle ambient stardust motes floating behind the manuscript
        if (motionConfig.enableAmbientLoops) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val moteCount = 16
                for (i in 0 until moteCount) {
                    val fx = ((i * 73 + 19) % 100) / 100f
                    val fyBase = ((i * 41 + 11) % 100) / 100f
                    val fy = (fyBase - stardustPhase * (0.35f + (i % 3) * 0.12f) + 1f) % 1f
                    val sway = sin((stardustPhase * 6.283f) + i) * 14f
                    val alpha = (0.12f + (i % 4) * 0.05f) * glowPulse
                    val radius = (1.1f + (i % 3) * 0.65f).dp.toPx()
                    drawCircle(
                        color = if (i % 3 == 0) accentColor.copy(alpha = alpha) else Color(0xFFE9D5FF).copy(alpha = alpha),
                        radius = radius,
                        center = Offset(size.width * fx + sway, size.height * fy)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── FLOATING MOR CAM TOP READER CHROME ──────────────────────────
            AnimatedVisibility(
                visible = !isFullscreenMode,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xEE0D051F), Color(0xDC0B041A), Color(0xB8090316))
                            )
                        )
                        .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReaderMorCamIconOrb(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            testTag = "book_back_button",
                            onClick = {
                                if (useDeterministicEngine) {
                                    BookStateRepository.saveScrollPosition(
                                        context = context,
                                        firstVisibleIndex = listState.firstVisibleItemIndex,
                                        firstVisibleOffset = listState.firstVisibleItemScrollOffset,
                                        bookId = CosmicDriftBookRegistry.BOOK_ID
                                    )
                                }
                                BookAudioController.stop(fadeOutMs = 2000L)
                                onBack()
                            }
                        )

                        // Center Chapter Selector Glass Capsule (optically centered, no bottom rim collision)
                        val chapterPillInteraction = remember { MutableInteractionSource() }
                        val tightPillTextStyle = remember {
                            TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .cosmicTapSpring(chapterPillInteraction, pressedScale = 0.97f)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xC824104A), Color(0xD414082C), Color(0xC824104A))
                                    )
                                )
                                .border(1.1.dp, MorCamTokens.RimGradient, RoundedCornerShape(50))
                                .clickable(
                                    enabled = useDeterministicEngine,
                                    interactionSource = chapterPillInteraction,
                                    indication = null
                                ) { showChapterSwitcher = true }
                                .testTag("book_chapter_selector_pill")
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text(
                                        text = if (useDeterministicEngine) {
                                            val seasonTag = if (activeChapterMeta.number >= 11) "2. SEZON" else "1. SEZON"
                                            "$seasonTag · BÖLÜM ${activeChapterMeta.romanNumeral} · $currentSceneOrder/${activeChapterMeta.sceneCount}"
                                        } else {
                                            "İNTERAKTİF KİTAP"
                                        },
                                        style = tightPillTextStyle,
                                        fontFamily = InterFontFamily,
                                        color = accentColor,
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.85.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (useDeterministicEngine) activeChapterMeta.title else bot.aiName,
                                        style = tightPillTextStyle,
                                        fontFamily = DisplaySerifFontFamily,
                                        color = MorCamTokens.SilverLavenderBright,
                                        fontSize = 13.5.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                if (useDeterministicEngine) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Bölüm Seç",
                                        tint = MorCamTokens.LavenderLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        if (useDeterministicEngine) {
                            ReaderMorCamIconOrb(
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = "Aiden'ın Dosyası & Evren",
                                testTag = "book_chronicle_button",
                                isActive = engineState.discoveredSecrets.isNotEmpty(),
                                activeColor = Color(0xFFD8B4FE),
                                onClick = { showChronicleModal = true }
                            )
                        }

                        ReaderMorCamIconOrb(
                            icon = Icons.Default.Refresh,
                            contentDescription = "Yeniden Başlat",
                            testTag = "book_reset_button",
                            onClick = { showResetDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    // Hairline luminous progress thread + Typography & Focus controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x452E175A))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(sceneProgressFraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF9333EA), accentColor, Color(0xFFFAF5FF))
                                        )
                                    )
                            )
                        }

                        if (useDeterministicEngine) {
                            val remainingMin = remember(
                                currentSceneOrder,
                                activeChapterMeta.sceneCount,
                                engineState.isChapterCompleted,
                                engineState.transcript.size
                            ) {
                                val scenes = engineState.transcript.filterIsInstance<BookTranscriptItem.SceneBlock>()
                                val wordCount = scenes.sumOf { block ->
                                    block.paragraphs.sumOf { com.example.util.ChatStoryStats.countWords(it) }
                                }
                                com.example.book.BookReadingEstimator.estimateRemainingMinutes(
                                    currentSceneOrder = currentSceneOrder,
                                    totalScenes = activeChapterMeta.sceneCount,
                                    isChapterCompleted = engineState.isChapterCompleted,
                                    observedTranscriptWordCount = wordCount,
                                    observedSceneCount = scenes.size
                                )
                            }
                            Text(
                                text = com.example.book.BookReadingEstimator.formatBadgeText(remainingMin),
                                fontFamily = InterFontFamily,
                                color = MorCamTokens.LavenderLight.copy(alpha = 0.88f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }

                        // Compact Typography Capsule [A− | A+]
                        Row(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x8A1C0C3A))
                                .border(0.85.dp, Color(0x66C4B5FD), RoundedCornerShape(50))
                                .padding(horizontal = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        val next = (fontSizeSp - 1.2f).coerceAtLeast(14f)
                                        fontSizeSp = next
                                        readerPrefs.edit().putFloat("font_size_sp", next).apply()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                    .testTag("book_font_decrease")
                            ) {
                                Text("A−", fontFamily = InterFontFamily, color = MorCamTokens.LavenderLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(12.dp)
                                    .background(Color(0x44C4B5FD))
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        val next = (fontSizeSp + 1.2f).coerceAtMost(22.5f)
                                        fontSizeSp = next
                                        readerPrefs.edit().putFloat("font_size_sp", next).apply()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                    .testTag("book_font_increase")
                            ) {
                                Text("A+", fontFamily = InterFontFamily, color = MorCamTokens.SilverLavenderBright, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Optional Reading Ambient Music Toggle Orb
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (ambientMusicEnabled) accentColor.copy(alpha = 0.28f) else Color(0x8A1C0C3A))
                                .border(
                                    0.85.dp,
                                    if (ambientMusicEnabled) accentColor else Color(0x66C4B5FD),
                                    CircleShape
                                )
                                .clickable {
                                    ambientMusicEnabled = !ambientMusicEnabled
                                    if (ambientMusicEnabled) {
                                        BookAudioController.play(
                                            context = context,
                                            rawResId = R.raw.kozmiksuruklenismuzik,
                                            loop = true,
                                            volume = 0.28f,
                                            fadeInMs = 1200L,
                                            autoUnmute = true
                                        )
                                    } else {
                                        BookAudioController.stop(fadeOutMs = 900L)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (ambientMusicEnabled) "🎵" else "🔇",
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0x8A1C0C3A))
                                .border(0.85.dp, Color(0x66C4B5FD), CircleShape)
                                .clickable { isFullscreenMode = true }
                                .testTag("book_fullscreen_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Odak Okuma Modu",
                                tint = MorCamTokens.LavenderLight,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    // Clean luminous glass divider so scrolled prose never collides visually with header controls
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color(0x55D8B4FE), Color.Transparent)
                                )
                            )
                    )
                }
            }

            // ── MAIN ILLUMINATED MANUSCRIPT READING STREAM ──────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                if (useDeterministicEngine) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 680.dp),
                        contentPadding = PaddingValues(
                            start = 18.dp,
                            end = 18.dp,
                            top = if (isFullscreenMode) 22.dp else 12.dp,
                            bottom = if (isFullscreenMode) 84.dp else 56.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = manuscriptRows,
                            key = { _, row -> row.key }
                        ) { _, row ->
                            when (row) {
                                is ManuscriptRow.Cover -> {
                                    CollectorChapterCoverCard(
                                        chapterMeta = row.meta,
                                        glowPulse = glowPulse
                                    )
                                }

                                is ManuscriptRow.SceneHeader -> {
                                    ManuscriptSceneHeader(
                                        sceneOrder = row.sceneOrder,
                                        sceneTitle = row.sceneTitle,
                                        locationTag = row.locationTag,
                                        chapterAccent = accentColor,
                                        isFirstScene = row.isFirstScene
                                    )
                                }

                                is ManuscriptRow.Paragraph -> {
                                    ManuscriptParagraphBlock(
                                        rawText = row.text,
                                        fontSizeSp = fontSizeSp,
                                        chapterAccent = accentColor,
                                        useDropCap = row.useDropCap,
                                        isMiniGameCompleted = row.nodeId in completedMiniGameNodes,
                                        pietroSaved = engineState.flags["s2_pietro_fate"] == "saved",
                                        activeVoiceCardKey = activeVoiceCardKey,
                                        onToggleSpeakCard = { speakerKey, bodyText, cardKey ->
                                            if (activeVoiceCardKey == cardKey) {
                                                readerVoiceEngine.stopCurrentLine()
                                                activeVoiceCardKey = null
                                            } else {
                                                activeVoiceCardKey = cardKey
                                                readerVoiceEngine.speak(speakerKey, bodyText)
                                            }
                                        },
                                        onMiniGameVictory = { markMiniGameCompleted(row.nodeId) }
                                    )
                                }

                                is ManuscriptRow.Echo -> {
                                    MemoryEchoInlay(
                                        label = row.label,
                                        body = row.body,
                                        fontSizeSp = fontSizeSp,
                                        chapterAccent = accentColor
                                    )
                                }

                                is ManuscriptRow.ClosingQuote -> {
                                    ChapterClosingQuotePlate(
                                        quote = row.quote,
                                        chapterAccent = accentColor
                                    )
                                }

                                is ManuscriptRow.DecisionSeal -> {
                                    BookDecisionSealRibbon(decision = row.decision)
                                }

                                is ManuscriptRow.ChoiceAnchor -> {
                                    val isLockedByMandatoryDuel =
                                        currentNodeRequiresMiniGame && row.nodeId !in completedMiniGameNodes
                                    if (isLockedByMandatoryDuel) {
                                        // HARD PROGRESSION GATE: Story choices stay locked until Vaelor is defeated!
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 14.dp)
                                                .morCamCosmicGlow(
                                                    cornerRadius = 22.dp,
                                                    glowColor = Color(0xFFF43F5E),
                                                    alpha = 0.45f,
                                                    blurDp = 18.dp
                                                )
                                                .clip(RoundedCornerShape(22.dp))
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color(0xEE2B091B), Color(0xF511061E))
                                                    )
                                                )
                                                .border(1.4.dp, Color(0xCCF43F5E), RoundedCornerShape(22.dp))
                                                .padding(horizontal = 18.dp, vertical = 18.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = "🔒 ZORUNLU SAVAŞ — KARARLAR KİLİTLİ",
                                                fontFamily = InterFontFamily,
                                                color = Color(0xFFFCA5A5),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.sp
                                            )
                                            Text(
                                                text = when (currentNodeBossInfo.first) {
                                                    com.example.ui.screens.cosmic3d.CosmicDuelBossId.MIRROR_ZERO ->
                                                        "Hikayede ilerlemek için kendi karanlık geleceğin Sıfır'ı Kule terasında durdurmalısın!"
                                                    com.example.ui.screens.cosmic3d.CosmicDuelBossId.ULTRON_PRIME ->
                                                        "2. Sezon finalini açmak için Sokovia meydanında Vibranyum Ultron Prime'ı yenmelisin!"
                                                    com.example.ui.screens.cosmic3d.CosmicDuelBossId.VAELOR_ROOFTOP ->
                                                        "Hikayede ilerlemek için Yüce Hakem Vaelor'u Avengers Kulesi'nin çatısında yenmelisin!"
                                                },
                                                fontFamily = DisplaySerifFontFamily,
                                                color = Color.White,
                                                fontSize = 15.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(Color(0xFF2563EB), Color(0xFFDC2626))
                                                        )
                                                    )
                                                    .border(1.1.dp, Color(0xFFBAE6FD), RoundedCornerShape(14.dp))
                                                    .clickable { showQuickMandatoryDuel = true }
                                                    .padding(vertical = 13.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = when (currentNodeBossInfo.first) {
                                                        com.example.ui.screens.cosmic3d.CosmicDuelBossId.MIRROR_ZERO ->
                                                            "⚔ AYNA DÜELLOSUNA GİR ▶"
                                                        com.example.ui.screens.cosmic3d.CosmicDuelBossId.ULTRON_PRIME ->
                                                            "⚔ ULTRON PRİME DÜELLOSUNA GİR ▶"
                                                        com.example.ui.screens.cosmic3d.CosmicDuelBossId.VAELOR_ROOFTOP ->
                                                            "⚔ VAELOR ÇATI DÜELLOSUNA GİR ▶"
                                                    },
                                                    fontFamily = InterFontFamily,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    } else {
                                        AutoRevealMorCamChoiceSanctuary(
                                            nodeId = row.nodeId,
                                            isRevealed = row.nodeId in revealedChoiceNodes,
                                            prompt = row.prompt,
                                            choices = row.choices,
                                            accentColor = accentColor,
                                            glowPulse = glowPulse,
                                            onForceReveal = {
                                                revealedChoiceNodes = revealedChoiceNodes + row.nodeId
                                            },
                                            onSelectChoice = { choice ->
                                                val nowMs = android.os.SystemClock.elapsedRealtime()
                                                if (nowMs - lastChoiceAppliedAtMs < 350L) return@AutoRevealMorCamChoiceSanctuary
                                                lastChoiceAppliedAtMs = nowMs
                                                val isCritical = choice.effect.impactLevel.equals("kritik", ignoreCase = true)
                                                haptic.performHapticFeedback(
                                                    if (isCritical) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                                                )
                                                val toneLower = choice.toneHint.lowercase()
                                                choicePulseColor = when {
                                                    toneLower.contains("beyaz göz") -> Color(0xFFF8FAFC)
                                                    choice.effect.redEyeDelta > 0 || toneLower.contains("kızıl") || toneLower.contains("wanda") -> Color(0xFFF43F5E)
                                                    toneLower.contains("natasha") || toneLower.contains("gölge") -> Color(0xFFF472B6)
                                                    toneLower.contains("zaman") || toneLower.contains("kozmik") || toneLower.contains("kalkan") -> Color(0xFF38BDF8)
                                                    else -> Color(0xFFFDE047)
                                                }
                                                choicePulseTrigger += 1
                                                choiceSparkTrigger += 1
                                                val updated = BookStateRepository.selectChoice(
                                                    context = context,
                                                    choiceId = choice.id,
                                                    bookId = CosmicDriftBookRegistry.BOOK_ID
                                                )
                                                pendingScrollToNodeId = updated.currentNodeId
                                                updated.currentChapterDecisions.lastOrNull()?.let { latest ->
                                                    lastDecisionBanner = latest
                                                }
                                            }
                                        )
                                    }
                                }

                                is ManuscriptRow.ChapterComplete -> {
                                    MorCamChapterCompleteCard(
                                        state = engineState,
                                        chapterMeta = activeChapterMeta,
                                        accentColor = accentColor,
                                        glowPulse = glowPulse,
                                        onOpenSummaryModal = {
                                            BookAudioController.play(
                                                context = context,
                                                rawResId = R.raw.kozmiksuruklenismuzik,
                                                loop = true,
                                                volume = 1.0f,
                                                fadeInMs = 1500L,
                                                autoUnmute = true
                                            )
                                            when (engineState.activeChapter) {
                                                10 -> showTwinAidenPostCredits = true
                                                24 -> showSeason2PostCredits = true
                                                else -> showChapterSummaryModal = true
                                            }
                                        },
                                        onOpenChronicle = { showChronicleModal = true },
                                        onNextChapter = {
                                            ambientMusicEnabled = false
                                            BookAudioController.stop(fadeOutMs = 2000L)
                                            val nextCh = (engineState.activeChapter + 1)
                                                .coerceAtMost(CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS)
                                            BookStateRepository.jumpToChapter(context, nextCh, forceRestartIfSame = true)
                                            chapterResetNonce += 1
                                        },
                                        onRestartChapter = {
                                            ambientMusicEnabled = false
                                            BookAudioController.stop(fadeOutMs = 2000L)
                                            clearMiniGameForChapter(engineState.activeChapter)
                                            BookStateRepository.restartCurrentChapter(context, CosmicDriftBookRegistry.BOOK_ID)
                                            chapterResetNonce += 1
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    CustomAiBookCanvas(
                        messages = messages,
                        choices = customAiChoices,
                        isSending = isSending,
                        errorMessage = errorMessage,
                        fontSizeSp = fontSizeSp,
                        glowPulse = glowPulse,
                        onSelectChoice = { option -> onSendMessage(option.rawLine) },
                        onSendCustomText = onSendMessage,
                        onClearError = onClearError
                    )
                }

                // Floating Consequence Feedback Pill at top of reading stage (zero overlap with exit fullscreen!)
                BookConsequenceBanner(
                    decision = lastDecisionBanner,
                    visible = showDecisionBanner,
                    onDismiss = { showDecisionBanner = false },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                )

                // Floating Newly-Unlocked Character Toast (never overlaps decision banner)
                BookCharacterUnlockedBanner(
                    character = newlyUnlockedCharBanner,
                    visible = showCharUnlockBanner && !showDecisionBanner,
                    onDismiss = { showCharUnlockBanner = false },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                )

                // Ergonomic Floating Exit Fullscreen Pill at Bottom-End (thumb-friendly & NEVER overlaps top banners!)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isFullscreenMode,
                    enter = fadeIn(tween(220)) + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut(tween(180)) + slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 18.dp, end = 14.dp)
                ) {
                    val exitInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .cosmicTapSpring(exitInteraction, pressedScale = 0.92f)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xD91E0D40), Color(0xE6110626))
                                )
                            )
                            .border(1.dp, MorCamTokens.RimGradient, RoundedCornerShape(50))
                            .clickable(interactionSource = exitInteraction, indication = null) {
                                isFullscreenMode = false
                            }
                            .testTag("book_exit_fullscreen")
                            .padding(horizontal = 13.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = "%${(sceneProgressFraction * 100f).toInt().coerceIn(1, 100)}",
                            fontFamily = InterFontFamily,
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 1.dp, height = 12.dp)
                                .background(Color(0x55D8B4FE))
                        )
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Tam Ekrandan Çık",
                            tint = MorCamTokens.SilverLavenderBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Çık",
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Cosmic screen-edge vignette pulse reacting to Aiden's chosen power (White Eye / Time / Crimson / Shadow)
        CosmicChoicePulseOverlay(
            triggerKey = choicePulseTrigger,
            pulseColor = choicePulseColor,
            modifier = Modifier.fillMaxSize()
        )

        // Cosmic micro-sparkle feedback on choice selection & chapter celebration
        CosmicSparkleBurst(
            triggerKey = choiceSparkTrigger,
            accentColor = choicePulseColor,
            modifier = Modifier.fillMaxSize()
        )
        CosmicConfettiOverlay(
            triggerKey = chapterCompleteConfetti,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Non-blocking 1.1s cosmic edge vignette pulse triggered on every story decision.
 * Gives White Eyes (pure icy white), Time Dilation / Cosmic Shield (electric blue),
 * Crimson / Wanda (scarlet), and Shadow / Natasha (rose-violet) an instant tactile feel.
 */
@Composable
private fun CosmicChoicePulseOverlay(
    triggerKey: Int,
    pulseColor: Color,
    modifier: Modifier = Modifier
) {
    var active by remember { mutableStateOf(false) }
    LaunchedEffect(triggerKey) {
        if (triggerKey > 0) {
            active = true
            delay(180L)
            active = false
        }
    }
    val alpha by animateFloatAsState(
        targetValue = if (active) 0.42f else 0f,
        animationSpec = tween(durationMillis = if (active) 160 else 820),
        label = "cosmic_choice_pulse"
    )
    if (alpha > 0.005f) {
        Canvas(modifier = modifier) {
            val radius = size.maxDimension * 0.72f
            drawRect(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Transparent,
                        0.55f to Color.Transparent,
                        0.85f to pulseColor.copy(alpha = alpha * 0.45f),
                        1.0f to pulseColor.copy(alpha = alpha)
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = radius
                )
            )
        }
    }
}

/**
 * Harmonized Mor Cam reader for custom user-created AI books.
 */
@Composable
private fun CustomAiBookCanvas(
    messages: List<MessageEntity>,
    choices: List<BookChoiceParser.ParsedBookChoice>,
    isSending: Boolean,
    errorMessage: String?,
    fontSizeSp: Float,
    glowPulse: Float,
    onSelectChoice: (BookChoiceParser.ParsedBookChoice) -> Unit,
    onSendCustomText: (String) -> Unit,
    onClearError: () -> Unit
) {
    var freeInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(messages, key = { idx, msg -> "${msg.id}_$idx" }) { _, msg ->
                if (msg.role == "user") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x7034176A))
                            .border(0.9.dp, Color(0x88D8B4FE), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 11.dp)
                    ) {
                        Text(
                            text = "✦ Seçimin: ${msg.text}",
                            fontFamily = LiterarySerifFontFamily,
                            color = Color(0xFFFAF6FF),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                } else {
                    val cleanedBody = remember(msg.text) {
                        stripChoiceBlockFromAiProse(msg.text)
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        cleanedBody.split("\n\n").filter { it.isNotBlank() }.forEach { para ->
                            ManuscriptParagraphBlock(
                                rawText = para.trim(),
                                fontSizeSp = fontSizeSp,
                                chapterAccent = Color(0xFFC084FC),
                                useDropCap = false
                            )
                        }
                    }
                }
            }

            if (isSending) {
                item(key = "custom_ai_loading") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x881D0C3D))
                            .border(0.9.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFC084FC)
                        )
                        Text(
                            text = "Bir sonraki sahne yazılıyor...",
                            fontFamily = LiterarySerifFontFamily,
                            color = MorCamTokens.LavenderLight,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }

            if (!isSending && choices.isNotEmpty()) {
                item(key = "custom_ai_choices_${messages.size}") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .morCamCosmicGlow(cornerRadius = 22.dp, glowColor = Color(0xFFA855F7), alpha = 0.34f * glowPulse, blurDp = 12.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xDE1B0B3A))
                            .border(1.1.dp, MorCamTokens.RimGradient, RoundedCornerShape(22.dp))
                            .padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Text(
                            text = "✦ KARAR NOKTASI ✦",
                            fontFamily = DisplaySerifFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 13.5.sp,
                            letterSpacing = 1.4.sp
                        )
                        choices.forEachIndexed { idx, opt ->
                            val interaction = remember(opt.rawLine) { MutableInteractionSource() }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cosmicTapSpring(interaction, pressedScale = if (opt.isLocked) 1f else 0.97f)
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(if (opt.isLocked) Color(0x661C152B) else Color(0x88281354))
                                    .border(
                                        1.dp,
                                        if (opt.isLocked) Color(0x6694A3B8) else Color(0x99D8B4FE),
                                        RoundedCornerShape(15.dp)
                                    )
                                    .clickable(
                                        enabled = !opt.isLocked,
                                        interactionSource = interaction,
                                        indication = null
                                    ) {
                                        onSelectChoice(opt)
                                    }
                                    .padding(horizontal = 13.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (opt.isLocked) "🔒 " else "${idx + 1}. ",
                                        fontFamily = DisplaySerifFontFamily,
                                        color = if (opt.isLocked) Color(0xFF94A3B8) else Color(0xFFD8B4FE),
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = formatLiteraryParagraph(opt.displayText),
                                        fontFamily = LiterarySerifFontFamily,
                                        color = if (opt.isLocked) Color(0x99CBD5E1) else Color(0xFFFAF6FF),
                                        fontSize = 14.5.sp,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (opt.badges.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        opt.badges.take(3).forEach { badge ->
                                            val tint = when (badge.kind) {
                                                BookChoiceParser.ChoiceBadgeKind.RISK -> Color(0xFFF43F5E)
                                                BookChoiceParser.ChoiceBadgeKind.SECRET -> Color(0xFFC084FC)
                                                BookChoiceParser.ChoiceBadgeKind.RELATIONSHIP -> Color(0xFFF472B6)
                                                BookChoiceParser.ChoiceBadgeKind.BRANCH -> Color(0xFFFDE047)
                                                BookChoiceParser.ChoiceBadgeKind.LOCKED -> Color(0xFF94A3B8)
                                                BookChoiceParser.ChoiceBadgeKind.TONE -> Color(0xFFD8B4FE)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(tint.copy(alpha = 0.18f))
                                                    .border(0.7.dp, tint.copy(alpha = 0.55f), RoundedCornerShape(50))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = badge.label,
                                                    fontFamily = InterFontFamily,
                                                    color = tint,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                                if (opt.isLocked && !opt.lockReason.isNullOrBlank()) {
                                    Text(
                                        text = "Koşul: ${opt.lockReason}",
                                        fontFamily = InterFontFamily,
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item(key = "custom_ai_error") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x8850102C))
                            .border(1.dp, Color(0xAAF472B6), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            fontFamily = InterFontFamily,
                            color = Color(0xFFFFE4F1),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Kapat",
                            fontFamily = InterFontFamily,
                            color = Color(0xFFFBCFE8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onClearError() }.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        if (!isSending && choices.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = freeInput,
                    onValueChange = { freeInput = it },
                    placeholder = { Text("Kendi hamleni yaz...", color = MorCamTokens.LavenderMuted, fontSize = 13.5.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD8B4FE),
                        unfocusedBorderColor = Color(0x66C4B5FD),
                        focusedContainerColor = Color(0xB8180A33),
                        unfocusedContainerColor = Color(0xB8180A33),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MorCamTokens.PrimaryButtonFill)
                        .border(1.dp, MorCamTokens.RimGradient, CircleShape)
                        .clickable(enabled = freeInput.isNotBlank()) {
                            val text = freeInput.trim()
                            freeInput = ""
                            onSendCustomText(text)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gönder",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun stripChoiceBlockFromAiProse(raw: String): String {
    val lines = raw.split("\n")
    val out = mutableListOf<String>()
    var inChoices = false
    for (line in lines) {
        val t = line.trim()
        if (t.startsWith("🔀") || t.startsWith("1️⃣")) {
            inChoices = true
            continue
        }
        if (inChoices && (t.matches(Regex("^[1-6][\\.\\)]\\s*.*")) || t.startsWith("2️⃣") || t.startsWith("3️⃣") || t.startsWith("4️⃣") || t == "---")) {
            continue
        }
        if (t == "---" || t.startsWith("#")) continue
        out.add(line)
    }
    return out.joinToString("\n").trim()
}
