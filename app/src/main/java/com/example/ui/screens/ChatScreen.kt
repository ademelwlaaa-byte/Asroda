package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AffectionEventEntity
import com.example.data.local.BotEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.MessageEntity
import com.example.data.local.MilestoneEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.KeyCharacter
import com.example.ui.components.BotSettingsModal
import com.example.ui.viewmodel.EmochiViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    bot: BotEntity,
    messages: List<MessageEntity>,
    userSettings: UserSettingsEntity?,
    isSending: Boolean,
    errorMessage: String?,
    keyCharacters: List<KeyCharacter>,
    characterEmotions: List<CharacterEmotionEntity> = emptyList(),
    affectionEvents: List<AffectionEventEntity> = emptyList(),
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onContinueScene: (() -> Unit)? = null,
    onRegenerate: () -> Unit,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onSaveBotProfile: (BotEntity, List<KeyCharacter>) -> Unit,
    onResetChat: (Boolean) -> Unit,
    onDeleteBot: () -> Unit,
    onSpeakText: ((String) -> Unit)? = null,
    onSpeakMessage: ((String, String) -> Unit)? = null,
    speakingMessageIdFlow: StateFlow<String?> = MutableStateFlow(null),
    onStopSpeaking: () -> Unit = {},
    onEnsureOpeningMessage: () -> Unit = {},
    onClearError: () -> Unit = {},
    onRetryMessage: ((String) -> Unit)? = null,
    onRetryFromBanner: (() -> Unit)? = null,
    onRewindToMessage: ((String) -> Unit)? = null,
    onCloneBot: (() -> Unit)? = null,
    streamingMsgIdFlow: StateFlow<String?> = MutableStateFlow(null),
    streamingTextFlow: StateFlow<String?> = MutableStateFlow(null),
    isStreamingThinkingFlow: StateFlow<Boolean> = MutableStateFlow(false),
    onStopGeneration: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var inputText by remember(bot.id) {
        mutableStateOf(com.example.util.ChatDraftStore.loadDraft(context, bot.id))
    }
    val latestInputText by androidx.compose.runtime.rememberUpdatedState(inputText)
    LaunchedEffect(bot.id, inputText) {
        delay(300L)
        com.example.util.ChatDraftStore.saveDraft(context, bot.id, inputText)
    }
    DisposableEffect(bot.id) {
        onDispose {
            com.example.util.ChatDraftStore.saveDraft(context, bot.id, latestInputText)
        }
    }
    var showBotSettings by remember { mutableStateOf(false) }
    var showQuickProfile by remember { mutableStateOf(false) }
    var showBackgroundPicker by remember { mutableStateOf(false) }
    var showPlusToolkit by remember { mutableStateOf(false) }
    var showStarredSheet by remember { mutableStateOf(false) }
    var showQuickNeuralVault by remember { mutableStateOf(false) }

    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }
    var expandedMenuMessageId by remember { mutableStateOf<String?>(null) }
    var expandedUserMenuMessageId by remember { mutableStateOf<String?>(null) }
    var rewindTargetMessageId by remember { mutableStateOf<String?>(null) }
    var reactionPickerMessageId by remember { mutableStateOf<String?>(null) }
    var showSearchBar by remember { mutableStateOf(false) }
    var localSearchQuery by remember { mutableStateOf("") }
    var activeMilestone by remember { mutableStateOf<MilestoneEntity?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val viewModel: EmochiViewModel = viewModel()

    val searchResults by viewModel.searchResults.collectAsState()
    val starredMessages by viewModel.starredMessages.collectAsState()
    val activeMilestones by viewModel.activeMilestones.collectAsStateWithLifecycle()
    val memoryCommitNotice by viewModel.memoryCommitNotice.collectAsState()
    val pendingCastMember by viewModel.pendingCastMember.collectAsState()
    val reactionEmojis = remember { listOf("❤️", "😂", "😮", "😢", "👍", "🔥") }

    // Sistem #18: Auto-dismiss milestone celebration after 4.5s
    LaunchedEffect(Unit) {
        viewModel.milestoneEvent.collect { milestone ->
            activeMilestone = milestone
            delay(4500L)
            if (activeMilestone?.id == milestone.id) {
                activeMilestone = null
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { onStopSpeaking() }
    }

    val rawValidMessages = remember(messages) {
        messages.filter { it.text.isNotBlank() && !it.id.contains("_ref_msg_") }
    }

    // 20-message windowed pagination + auto-collapse for messages > 5 from bottom
    val chatPageSize = 20
    var visibleMessageLimit by remember(bot.id) { mutableIntStateOf(chatPageSize) }
    var expandedOldMessageIds by remember(bot.id) { mutableStateOf(emptySet<String>()) }
    var pendingPrependAnchorIndex by remember(bot.id) { mutableStateOf<Int?>(null) }
    var pendingSnapToBottomAfterCollapse by remember(bot.id) { mutableStateOf(false) }

    val hiddenOlderCount = (rawValidMessages.size - visibleMessageLimit).coerceAtLeast(0)
    val paginatedValidMessages = remember(rawValidMessages, visibleMessageLimit) {
        if (rawValidMessages.size > visibleMessageLimit) rawValidMessages.takeLast(visibleMessageLimit)
        else rawValidMessages
    }
    val hasLoadOlderHeader = hiddenOlderCount > 0

    LaunchedEffect(pendingPrependAnchorIndex, paginatedValidMessages.size) {
        val anchorIdx = pendingPrependAnchorIndex ?: return@LaunchedEffect
        try {
            val clamped = anchorIdx.coerceIn(0, (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))
            listState.scrollToItem(clamped)
        } catch (_: Exception) {}
        pendingPrependAnchorIndex = null
    }

    LaunchedEffect(pendingSnapToBottomAfterCollapse, paginatedValidMessages.size) {
        if (!pendingSnapToBottomAfterCollapse) return@LaunchedEffect
        val totalItems = (if (hasLoadOlderHeader) 1 else 0) + paginatedValidMessages.size + if (isSending) 1 else 0
        if (totalItems > 0) {
            try { listState.scrollToItem(totalItems - 1) } catch (_: Exception) {}
        }
        pendingSnapToBottomAfterCollapse = false
    }

    var highlightedMessageId by remember(bot.id) { mutableStateOf<String?>(null) }
    var pendingJumpMessageId by remember(bot.id) { mutableStateOf<String?>(null) }
    var activeSearchIndex by remember(bot.id, localSearchQuery) { mutableIntStateOf(0) }

    val jumpToMessageById: (String) -> Unit = remember(rawValidMessages, visibleMessageLimit) {
        { targetMsgId ->
            val rawIdx = rawValidMessages.indexOfFirst { it.id == targetMsgId }
            if (rawIdx >= 0) {
                val neededFromEnd = rawValidMessages.size - rawIdx
                if (neededFromEnd > visibleMessageLimit) {
                    visibleMessageLimit = ((neededFromEnd + chatPageSize - 1) / chatPageSize) * chatPageSize
                }
                expandedOldMessageIds = expandedOldMessageIds + targetMsgId
                pendingJumpMessageId = targetMsgId
            }
        }
    }

    LaunchedEffect(bot.id) { viewModel.loadStarredMessages() }
    LaunchedEffect(bot.id, messages.size) { viewModel.checkForPendingCastMembers(bot.id, messages.size) }

    LaunchedEffect(searchResults, localSearchQuery) {
        if (showSearchBar && localSearchQuery.isNotBlank() && searchResults.isNotEmpty()) {
            activeSearchIndex = 0
            jumpToMessageById(searchResults[0].id)
        }
    }

    LaunchedEffect(pendingJumpMessageId, paginatedValidMessages.size, hasLoadOlderHeader) {
        val targetId = pendingJumpMessageId ?: return@LaunchedEffect
        val idxInVisible = paginatedValidMessages.indexOfFirst { it.id == targetId }
        if (idxInVisible >= 0) {
            val lazyIdx = idxInVisible + if (hasLoadOlderHeader) 1 else 0
            try { listState.animateScrollToItem(lazyIdx) } catch (_: Exception) {
                try { listState.scrollToItem(lazyIdx) } catch (_: Exception) {}
            }
            highlightedMessageId = targetId
            pendingJumpMessageId = null
        }
    }

    LaunchedEffect(highlightedMessageId) {
        if (highlightedMessageId != null) {
            delay(2200)
            highlightedMessageId = null
        }
    }

    val visibleItemKeys by remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.mapNotNull { it.key as? String }.toSet() }
    }
    LaunchedEffect(visibleItemKeys, listState.isScrollInProgress) {
        if (visibleItemKeys.isNotEmpty()) {
            if (expandedMenuMessageId != null && expandedMenuMessageId !in visibleItemKeys) expandedMenuMessageId = null
            if (expandedUserMenuMessageId != null && expandedUserMenuMessageId !in visibleItemKeys) expandedUserMenuMessageId = null
            if (reactionPickerMessageId != null && reactionPickerMessageId !in visibleItemKeys) reactionPickerMessageId = null
        }
    }

    val isChatAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()
            val contentEnd = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
            com.example.util.ChatScrollPolicy.isAtBottom(
                totalItems = layoutInfo.totalItemsCount,
                canScrollForward = listState.canScrollForward,
                lastVisibleIndex = lastVisible?.index,
                lastVisibleEndPx = lastVisible?.let { it.offset + it.size },
                viewportEndPx = contentEnd
            )
        }
    }

    var isStickyToBottom by remember(bot.id) { mutableStateOf(true) }
    LaunchedEffect(isChatAtBottom, listState.isScrollInProgress) {
        isStickyToBottom = com.example.util.ChatScrollPolicy.nextStickyState(
            currentSticky = isStickyToBottom,
            isUserScrollInProgress = listState.isScrollInProgress,
            isAtBottom = isChatAtBottom
        )
    }

    suspend fun scrollToTrueBottom(animate: Boolean) {
        val layoutInfo = listState.layoutInfo
        val total = layoutInfo.totalItemsCount
        if (total <= 0) return
        val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()
        val contentEnd = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
        val overflowPx = com.example.util.ChatScrollPolicy.computeBottomOverflowPx(
            totalItems = total,
            lastVisibleIndex = lastVisible?.index,
            lastVisibleEndPx = lastVisible?.let { it.offset + it.size },
            viewportEndPx = contentEnd
        )
        try {
            if (overflowPx != null) {
                if (overflowPx > 0f) {
                    listState.scrollBy(overflowPx)
                }
            } else {
                if (animate) {
                    listState.animateScrollToItem(total - 1)
                } else {
                    listState.scrollToItem(total - 1)
                }
                val updatedInfo = listState.layoutInfo
                val updatedLast = updatedInfo.visibleItemsInfo.lastOrNull()
                val updatedEnd = updatedInfo.viewportEndOffset - updatedInfo.afterContentPadding
                val extraOverflow = com.example.util.ChatScrollPolicy.computeBottomOverflowPx(
                    totalItems = updatedInfo.totalItemsCount,
                    lastVisibleIndex = updatedLast?.index,
                    lastVisibleEndPx = updatedLast?.let { it.offset + it.size },
                    viewportEndPx = updatedEnd
                )
                if (extraOverflow != null && extraOverflow > 0f) {
                    listState.scrollBy(extraOverflow)
                }
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(isChatAtBottom, listState.isScrollInProgress, visibleMessageLimit, expandedOldMessageIds.size, pendingJumpMessageId, highlightedMessageId) {
        if (isChatAtBottom && !listState.isScrollInProgress && pendingJumpMessageId == null && highlightedMessageId == null) {
            delay(180)
            if (isChatAtBottom && !listState.isScrollInProgress && pendingJumpMessageId == null && highlightedMessageId == null) {
                if (expandedOldMessageIds.isNotEmpty()) expandedOldMessageIds = emptySet()
                if (visibleMessageLimit > chatPageSize) {
                    visibleMessageLimit = chatPageSize
                    pendingSnapToBottomAfterCollapse = true
                }
            }
        }
    }

    LaunchedEffect(bot.id, rawValidMessages.isEmpty()) {
        if (rawValidMessages.isEmpty() && !isSending && bot.mode != "book") {
            onEnsureOpeningMessage()
        }
    }

    LaunchedEffect(rawValidMessages.size, isSending) {
        if (isStickyToBottom && !listState.isScrollInProgress) {
            scrollToTrueBottom(animate = true)
        }
    }

    // Throttled coroutine-only auto-scroll during streaming:
    // follows tall multi-paragraph bubbles line-by-line at the bottom while strictly respecting upward user scrolling.
    LaunchedEffect(streamingTextFlow) {
        var lastScrollMs = 0L
        streamingTextFlow.collect { chunk ->
            val nowMs = System.currentTimeMillis()
            if (com.example.util.ChatScrollPolicy.shouldAutoScrollOnStreamChunk(
                    chunk = chunk,
                    isStickyToBottom = isStickyToBottom,
                    isUserScrollInProgress = listState.isScrollInProgress,
                    nowMs = nowMs,
                    lastScrollMs = lastScrollMs
                )
            ) {
                lastScrollMs = nowMs
                scrollToTrueBottom(animate = false)
            }
        }
    }

    val isUniverse = bot.mode == "universe"
    val displayName = if (isUniverse) bot.universeName.ifBlank { bot.aiName.ifBlank { "Evren" } } else bot.aiName.ifBlank { bot.universeName.ifBlank { "Karakter" } }
    val avatarInitial = remember(displayName) {
        displayName.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "A"
    }

    val initialMessageIds = remember(bot.id) { rawValidMessages.map { it.id }.toSet() }
    var sendBurstTrigger by remember(bot.id) { mutableIntStateOf(0) }
    var copiedMsgId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(copiedMsgId) {
        if (copiedMsgId != null) {
            delay(1500)
            copiedMsgId = null
        }
    }

    val emotionHud = rememberChatEmotionHudState(bot = bot, rawValidMessages = rawValidMessages)
    val pulseGlowAlpha = emotionHud.pulseGlowAlpha
    val emotionAuraColor = emotionHud.emotionAuraColor
    val affectionDeltaText = emotionHud.affectionDeltaText
    val affectionDeltaTrigger = emotionHud.affectionDeltaTrigger
    val affectionDeltaPositive = emotionHud.affectionDeltaPositive

    val isBookMode = remember(bot) {
        bot.mode == "book" ||
            bot.id == "preset_aiden_mcu_cosmic" ||
            bot.aiName.contains("Kitap") ||
            bot.aiPersonality.contains("KİTAP MODU") ||
            bot.scenario.contains("KİTAP MODU")
    }

    if (isBookMode) {
        BookReaderView(
            bot = bot,
            messages = rawValidMessages,
            isSending = isSending,
            errorMessage = errorMessage,
            onBack = onBack,
            onSendMessage = onSendMessage,
            onEnsureOpeningMessage = onEnsureOpeningMessage,
            onResetChat = { onResetChat(it) },
            onClearError = onClearError
        )
        if (showBotSettings) {
            BotSettingsModal(
                bot = bot,
                keyCharacters = keyCharacters,
                characterEmotions = characterEmotions,
                affectionEvents = affectionEvents,
                milestones = activeMilestones,
                onDismiss = { showBotSettings = false },
                onSave = onSaveBotProfile,
                onResetChat = onResetChat,
                onDeleteBot = onDeleteBot,
                onCloneBot = onCloneBot
            )
        }
        return
    }

    val rootSizeState = remember { mutableStateOf(IntSize.Zero) }
    val rootPosState = remember { mutableStateOf(Offset.Zero) }
    val rootSize = rootSizeState.value
    val blurredBgState = com.example.util.rememberFrostedBackdrop(
        key = "wisteria_chat_bg",
        drawableResId = com.example.R.drawable.img_chat_bg_aiden_wisteria,
        targetWidth = 360,
        maxHeight = 1080,
        blurRadius = 5,
        blurPasses = 2,
        outputRgb565 = true
    )
    val blurredBgBitmap = blurredBgState.value ?: com.example.util.FrostedBackdropCache.placeholderImageBitmap
    val refGlassBitmaps = rememberAetheriaRefGlassBitmaps()
    val validMessages = paginatedValidMessages

    CompositionLocalProvider(
        LocalAetheriaRootPos provides rootPosState,
        LocalAetheriaRootSize provides rootSizeState
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { rootSizeState.value = it }
                .onGloballyPositioned { if (it.isAttached) rootPosState.value = it.positionInRoot() }
        ) {
            val isImeVisible = WindowInsets.isImeVisible
            ChatImeAndMenuScrollEffects(
                listState = listState,
                isImeVisible = isImeVisible,
                hasLoadOlderHeader = hasLoadOlderHeader,
                validMessagesCount = validMessages.size,
                isSending = isSending,
                expandedMenuMessageId = expandedMenuMessageId,
                expandedUserMenuMessageId = expandedUserMenuMessageId,
                reactionPickerMessageId = reactionPickerMessageId
            )

            RpAtmosphericBackground()
            ChatCustomBackgroundLayer(bot = bot)

            Scaffold(
                modifier = Modifier.graphicsLayer { alpha = if (showBotSettings) 0f else 1f },
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    ChatHeaderTopBar(
                        bot = bot,
                        headerTitle = displayName,
                        avatarInitial = avatarInitial,
                        emotionAuraColor = emotionAuraColor,
                        pulseGlowAlpha = pulseGlowAlpha,
                        affectionDeltaTrigger = affectionDeltaTrigger,
                        affectionDeltaText = affectionDeltaText,
                        affectionDeltaPositive = affectionDeltaPositive,
                        refGlassBitmaps = refGlassBitmaps,
                        showSearchBar = showSearchBar,
                        localSearchQuery = localSearchQuery,
                        matchCount = searchResults.size,
                        activeMatchIndex = activeSearchIndex,
                        starredCount = starredMessages.size,
                        speakingMessageIdFlow = speakingMessageIdFlow,
                        isImeVisible = isImeVisible,
                        rootSize = rootSize,
                        blurredBgBitmap = blurredBgBitmap,
                        onBackClick = {
                            expandedMenuMessageId = null
                            expandedUserMenuMessageId = null
                            reactionPickerMessageId = null
                            onBack()
                        },
                        onQuickProfileClick = { showQuickProfile = true },
                        onToggleSearch = {
                            showSearchBar = !showSearchBar
                            if (!showSearchBar) {
                                localSearchQuery = ""
                                activeSearchIndex = 0
                                viewModel.clearSearch()
                            }
                        },
                        onSettingsClick = { showBotSettings = true },
                        onQueryChange = { q ->
                            localSearchQuery = q
                            activeSearchIndex = 0
                            viewModel.setSearchQuery(q.ifBlank { null })
                        },
                        onClearQuery = {
                            localSearchQuery = ""
                            activeSearchIndex = 0
                            viewModel.clearSearch()
                        },
                        onPrevMatch = {
                            if (searchResults.isNotEmpty()) {
                                activeSearchIndex = (activeSearchIndex - 1 + searchResults.size) % searchResults.size
                                jumpToMessageById(searchResults[activeSearchIndex].id)
                            }
                        },
                        onNextMatch = {
                            if (searchResults.isNotEmpty()) {
                                activeSearchIndex = (activeSearchIndex + 1) % searchResults.size
                                jumpToMessageById(searchResults[activeSearchIndex].id)
                            }
                        },
                        onOpenStarredSheet = { showStarredSheet = true },
                        onStopSpeaking = onStopSpeaking
                    )
                },
                bottomBar = {
                    ChatBottomInputDock(
                        inputText = inputText,
                        isSending = isSending,
                        canEditLastMessage = !isSending && validMessages.isNotEmpty(),
                        showPlusToolkit = showPlusToolkit,
                        sendBurstTrigger = sendBurstTrigger,
                        rootSize = rootSize,
                        blurredBgBitmap = blurredBgBitmap,
                        refGlassBitmaps = refGlassBitmaps,
                        onInputTextChange = { inputText = it },
                        onTogglePlusToolkit = { showPlusToolkit = !showPlusToolkit },
                        onDismissPlusToolkit = { showPlusToolkit = false },
                        onOpenStarredSheet = { showStarredSheet = true },
                        onContinueScene = {
                            if (onContinueScene != null) onContinueScene() else viewModel.continueStoryScene()
                        },
                        onSendMessage = { text ->
                            com.example.util.ChatDraftStore.clearDraft(context, bot.id)
                            isStickyToBottom = true
                            onSendMessage(text)
                        },
                        onEditLastMessage = {
                            validMessages.lastOrNull()?.let { target ->
                                editingMessageId = target.id
                                editingText = target.text
                            }
                        },
                        onOpenBackgroundPicker = { showBackgroundPicker = true },
                        onTriggerSendBurst = { sendBurstTrigger += 1 },
                        onStopGeneration = {
                            if (onStopGeneration != null) onStopGeneration() else viewModel.stopGeneration()
                        },
                        onCopyTranscript = {
                            val md = com.example.util.ChatTranscriptFormatter.formatMarkdown(
                                botDisplayName = displayName,
                                userDisplayName = bot.userCharName.ifBlank { "Sen" },
                                messages = validMessages
                            )
                            if (md.isNotBlank()) {
                                val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                cm?.setPrimaryClip(android.content.ClipData.newPlainText(displayName, md))
                                android.widget.Toast.makeText(
                                    context,
                                    "Hikaye panoya kopyalandı (${validMessages.size} mesaj)",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onShareTranscript = {
                            val md = com.example.util.ChatTranscriptFormatter.formatMarkdown(
                                botDisplayName = displayName,
                                userDisplayName = bot.userCharName.ifBlank { "Sen" },
                                messages = validMessages
                            )
                            if (md.isNotBlank()) {
                                try {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "$displayName — Asroda Hikayesi")
                                        putExtra(android.content.Intent.EXTRA_TEXT, md)
                                    }
                                    context.startActivity(
                                        android.content.Intent.createChooser(shareIntent, "Hikayeyi Paylaş")
                                    )
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }
            ) { innerPadding ->
                val topInsetPadding = innerPadding.calculateTopPadding()
                val bottomInsetPadding = innerPadding.calculateBottomPadding()

                LaunchedEffect(bottomInsetPadding, isImeVisible) {
                    if (isImeVisible) {
                        val totalItems = (if (hasLoadOlderHeader) 1 else 0) + validMessages.size + if (isSending) 1 else 0
                        if (totalItems > 0) {
                            try { listState.scrollToItem(totalItems - 1) } catch (_: Exception) {}
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ChatMessageTimelineList(
                            bot = bot,
                            displayName = displayName,
                            avatarInitial = avatarInitial,
                            listState = listState,
                            topInsetPadding = topInsetPadding,
                            bottomInsetPadding = bottomInsetPadding,
                            validMessages = validMessages,
                            initialMessageIds = initialMessageIds,
                            hasLoadOlderHeader = hasLoadOlderHeader,
                            chatPageSize = chatPageSize,
                            hiddenOlderCount = hiddenOlderCount,
                            editingMessageId = editingMessageId,
                            editingText = editingText,
                            highlightedMessageId = highlightedMessageId,
                            expandedOldMessageIds = expandedOldMessageIds,
                            expandedMenuMessageId = expandedMenuMessageId,
                            expandedUserMenuMessageId = expandedUserMenuMessageId,
                            reactionPickerMessageId = reactionPickerMessageId,
                            activeHighlightQuery = if (showSearchBar) localSearchQuery else "",
                            copiedMsgId = copiedMsgId,
                            isSending = isSending,
                            emotionAuraColor = emotionAuraColor,
                            reactionEmojis = reactionEmojis,
                            rootSize = rootSize,
                            blurredBgBitmap = blurredBgBitmap,
                            refGlassBitmaps = refGlassBitmaps,
                            streamingMsgIdFlow = streamingMsgIdFlow,
                            streamingTextFlow = streamingTextFlow,
                            isStreamingThinkingFlow = isStreamingThinkingFlow,
                            speakingMessageIdFlow = speakingMessageIdFlow,
                            onLoadOlder = { nextBatchCount ->
                                val willStillHaveHeader = (hiddenOlderCount - nextBatchCount) > 0
                                visibleMessageLimit += chatPageSize
                                pendingPrependAnchorIndex = nextBatchCount + if (willStillHaveHeader) 1 else 0
                            },
                            onEditingTextChange = { editingText = it },
                            onCancelEdit = { editingMessageId = null },
                            onSaveEdit = { id, text ->
                                editingMessageId = null
                                onEditMessage(id, text)
                            },
                            onToggleUserMenu = { id ->
                                expandedUserMenuMessageId = if (expandedUserMenuMessageId != id) id else null
                            },
                            onToggleBotMenu = { id ->
                                expandedMenuMessageId = if (expandedMenuMessageId != id) id else null
                            },
                            onCloseBotMenu = { expandedMenuMessageId = null },
                            onCloseReactionPicker = { reactionPickerMessageId = null },
                            onToggleReactionPicker = { id ->
                                expandedMenuMessageId = null
                                expandedUserMenuMessageId = null
                                reactionPickerMessageId = if (reactionPickerMessageId == id) null else id
                            },
                            onToggleExpandOld = { id ->
                                expandedOldMessageIds = if (id in expandedOldMessageIds) expandedOldMessageIds - id else expandedOldMessageIds + id
                            },
                            onSetReaction = { id, emoji -> viewModel.setMessageReaction(id, emoji) },
                            onCopied = { copiedMsgId = it },
                            onContinueScene = {
                                expandedUserMenuMessageId = null
                                if (onContinueScene != null) onContinueScene() else viewModel.continueStoryScene()
                            },
                            onRegenerate = onRegenerate,
                            onSpeakMessage = { id, text ->
                                if (onSpeakMessage != null) onSpeakMessage(id, text) else onSpeakText?.invoke(text)
                            },
                            onStopSpeaking = onStopSpeaking,
                            onToggleStar = { viewModel.toggleMessageStar(it) },
                            onStartEdit = { msg ->
                                editingMessageId = msg.id
                                editingText = msg.text
                                expandedUserMenuMessageId = null
                            },
                            onRequestRewind = { id ->
                                rewindTargetMessageId = id
                                expandedUserMenuMessageId = null
                            },
                            onDeleteMessage = { id ->
                                expandedUserMenuMessageId = null
                                onDeleteMessage(id)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val showScrollToBottom = remember {
                        derivedStateOf {
                            if (!listState.canScrollForward) return@derivedStateOf false
                            val layoutInfo = listState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            if (totalItems == 0) return@derivedStateOf false
                            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
                            (totalItems - 1 - lastVisibleItem.index) >= 3
                        }
                    }

                    ChatFloatingErrorBanner(
                        errorMessage = errorMessage,
                        topInsetPadding = topInsetPadding,
                        onRetryFromBanner = onRetryFromBanner,
                        onClearError = onClearError,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    ChatScrollToBottomFab(
                        visible = showScrollToBottom.value,
                        rootSize = rootSize,
                        blurredBgBitmap = blurredBgBitmap,
                        onClick = {
                            isStickyToBottom = true
                            val totalCount = (if (hasLoadOlderHeader) 1 else 0) + validMessages.size + if (isSending) 1 else 0
                            coroutineScope.launch {
                                expandedOldMessageIds = emptySet()
                                if (visibleMessageLimit > chatPageSize) {
                                    visibleMessageLimit = chatPageSize
                                    pendingSnapToBottomAfterCollapse = true
                                } else if (totalCount > 0) {
                                    scrollToTrueBottom(animate = true)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = bottomInsetPadding + 10.dp, end = 14.dp)
                    )
                }
            }

            if (showBotSettings) {
                BotSettingsModal(
                    bot = bot,
                    keyCharacters = keyCharacters,
                    characterEmotions = characterEmotions,
                    affectionEvents = affectionEvents,
                    milestones = activeMilestones,
                    onDismiss = { showBotSettings = false },
                    onSave = onSaveBotProfile,
                    onResetChat = onResetChat,
                    onDeleteBot = onDeleteBot,
                    onCloneBot = onCloneBot
                )
            }
        }
    }

    ChatScreenOverlaysHost(
        bot = bot,
        displayName = displayName,
        messages = messages,
        keyCharacters = keyCharacters,
        characterEmotions = characterEmotions,
        affectionEvents = affectionEvents,
        activeMilestones = activeMilestones,
        starredMessages = starredMessages,
        memoryCommitNotice = memoryCommitNotice,
        pendingCastMember = pendingCastMember,
        rewindTargetMessageId = rewindTargetMessageId,
        activeMilestone = activeMilestone,
        showBotSettings = showBotSettings,
        showBackgroundPicker = showBackgroundPicker,
        showQuickNeuralVault = showQuickNeuralVault,
        showStarredSheet = showStarredSheet,
        showQuickProfile = showQuickProfile,
        viewModel = viewModel,
        onDismissBackgroundPicker = { showBackgroundPicker = false },
        onOpenBotSettings = { showBotSettings = true },
        onDismissQuickNeuralVault = { showQuickNeuralVault = false },
        onOpenQuickNeuralVault = { showQuickNeuralVault = true },
        onDismissRewind = { rewindTargetMessageId = null },
        onConfirmRewind = { idToRewind ->
            rewindTargetMessageId = null
            onRewindToMessage?.invoke(idToRewind)
        },
        onDismissMilestone = { activeMilestone = null },
        onOpenQuickProfile = { showQuickProfile = true },
        onDismissQuickProfile = { showQuickProfile = false },
        onDismissStarredSheet = { showStarredSheet = false },
        onJumpToMessage = jumpToMessageById,
        onSaveBotProfile = onSaveBotProfile,
        onResetChat = onResetChat
    )
}