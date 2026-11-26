package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.animation.CosmicStreamingCaret
import com.example.ui.animation.CosmicVoiceEqualizer
import com.example.ui.animation.cosmicMessageEnter
import com.example.ui.animation.cosmicShimmerSweep
import com.example.ui.components.TypingDots
import com.example.ui.theme.InterFontFamily
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.ui.animation.CosmicSparkleBurst
import com.example.ui.animation.cosmicBorderBreathing
import com.example.ui.animation.cosmicPopOnTrigger
import com.example.ui.animation.cosmicTapSpring
import com.example.ui.components.BotAvatarImage
import com.example.ui.components.MorCamButtonVariant
import com.example.ui.components.MorCamCapsuleButton
import com.example.ui.components.MorCamGlassCard
import com.example.ui.components.MorCamGlassTextField
import com.example.ui.components.MorCamTokens
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private val NEVER_THINKING_FLOW: StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(false)

private val BUBBLE_CLOCK_FORMATTER: ThreadLocal<java.text.SimpleDateFormat> =
    object : ThreadLocal<java.text.SimpleDateFormat>() {
        override fun initialValue(): java.text.SimpleDateFormat =
            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    }

internal fun formatBubbleClockTime(timestampMs: Long): String {
    if (timestampMs <= 0L) return ""
    val fmt = BUBBLE_CLOCK_FORMATTER.get()
        ?: java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return fmt.format(java.util.Date(timestampMs))
}

internal fun computeLastAssistantReplyIndex(messages: List<MessageEntity>): Int =
    messages.indexOfLast { it.role == "assistant" && it.text.isNotBlank() }

internal fun isMessageAutoCollapsible(isOldMessage: Boolean, cleanText: String): Boolean =
    isOldMessage && (cleanText.length > 180 || cleanText.count { it == '\n' } >= 4)

/**
 * Formats narrative text using [formatNarrativeText] and overlays Mor Cam translucent
 * lavender highlights on all case-insensitive occurrences of [highlightQuery].
 */
fun formatNarrativeTextWithHighlight(
    text: String,
    highlightQuery: String = ""
): AnnotatedString {
    val base = formatNarrativeText(text)
    val q = highlightQuery.trim()
    if (q.isEmpty()) return base

    val raw = base.text
    val lowerRaw = raw.lowercase(java.util.Locale.forLanguageTag("tr-TR"))
    val lowerQ = q.lowercase(java.util.Locale.forLanguageTag("tr-TR"))
    if (!lowerRaw.contains(lowerQ)) return base

    return buildAnnotatedString {
        append(base)
        var startIdx = 0
        while (startIdx < lowerRaw.length) {
            val found = lowerRaw.indexOf(lowerQ, startIdx)
            if (found < 0) break
            addStyle(
                style = SpanStyle(
                    background = Color(0x66A855F7),
                    color = Color.White
                ),
                start = found,
                end = (found + lowerQ.length).coerceAtMost(raw.length)
            )
            startIdx = found + lowerQ.length
        }
    }
}

/**
 * Leaf composable isolating per-character streaming recomposition strictly to the single
 * active bot message bubble. TopBar, InputDock, and all older bubbles never recompose during streaming!
 */
@Composable
internal fun IsolatedStreamingBotBubbleText(
    messageId: String,
    persistedText: String,
    streamingMsgIdFlow: StateFlow<String?>,
    streamingTextFlow: StateFlow<String?>,
    isStreamingThinkingFlow: StateFlow<Boolean> = NEVER_THINKING_FLOW,
    isCollapsed: Boolean,
    highlightQuery: String
) {
    val activeStreamingId by streamingMsgIdFlow.collectAsStateWithLifecycle()
    val isThisStreaming = activeStreamingId == messageId
    val displayText = if (isThisStreaming) {
        val liveText by streamingTextFlow.collectAsStateWithLifecycle()
        liveText ?: persistedText
    } else {
        persistedText
    }
    val isThinking = if (isThisStreaming) {
        val thinking by isStreamingThinkingFlow.collectAsStateWithLifecycle()
        thinking
    } else {
        false
    }
    val effectiveCollapsed = isCollapsed && !isThisStreaming

    if (isThisStreaming && isThinking && displayText.isBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            CosmicStreamingCaret(color = Color(0xFFD8B4FE), size = 14.dp)
            Text(
                text = "✦ Düşünüyor...",
                color = Color(0xFFE9D5FF).copy(alpha = 0.88f),
                fontSize = 14.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontFamily = LiterarySerifFontFamily
            )
        }
        return
    }

    val annotated = remember(displayText, highlightQuery) {
        formatNarrativeTextWithHighlight(displayText, highlightQuery)
    }

    Column {
        Text(
            text = annotated,
            color = Color.White.copy(alpha = 0.97f),
            fontSize = 15.sp,
            lineHeight = 22.5.sp,
            letterSpacing = 0.1.sp,
            fontFamily = LiterarySerifFontFamily,
            maxLines = if (effectiveCollapsed) 4 else Int.MAX_VALUE,
            overflow = if (effectiveCollapsed) TextOverflow.Ellipsis else TextOverflow.Clip,
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0x88120524),
                    offset = Offset(0f, 1f),
                    blurRadius = 3.5f
                )
            )
        )
        if (isThisStreaming && isThinking) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "✦ Düşünüyor...",
                color = Color(0xFFD8B4FE).copy(alpha = 0.78f),
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontFamily = LiterarySerifFontFamily
            )
        }
    }
}

/**
 * Isolated streaming caret shown only inside the currently streaming message bubble.
 */
@Composable
internal fun IsolatedStreamingCaret(
    messageId: String,
    streamingMsgIdFlow: StateFlow<String?>,
    color: Color
) {
    val activeStreamingId by streamingMsgIdFlow.collectAsStateWithLifecycle()
    if (activeStreamingId == messageId) {
        CosmicStreamingCaret(color = color, size = 14.dp)
    }
}

/**
 * Message-scoped TTS footer control: animates CosmicVoiceEqualizer ONLY for the bubble
 * whose `msg.id == speakingMessageId`. Tapping stops if active or switches TTS to this bubble.
 */
@Composable
internal fun IsolatedMessageTtsFooter(
    messageId: String,
    messageText: String,
    timeFormatted: String,
    speakingMessageIdFlow: StateFlow<String?>,
    onSpeakMessage: (String, String) -> Unit,
    onStopSpeaking: () -> Unit
) {
    val speakingMessageId by speakingMessageIdFlow.collectAsStateWithLifecycle()
    val isThisMsgSpeaking = speakingMessageId == messageId

    Row(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isThisMsgSpeaking) onStopSpeaking() else onSpeakMessage(messageId, messageText)
            }
            .testTag("message_tts_toggle_$messageId"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timeFormatted.ifBlank { "22:50" },
            color = Color(0xEBF8F5FF),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = InterFontFamily
        )
        Spacer(modifier = Modifier.width(5.dp))
        if (isThisMsgSpeaking) {
            CosmicVoiceEqualizer(
                active = true,
                barCount = 4,
                color = Color(0xFFE879F9),
                height = 12.dp
            )
        } else {
            DoubleCheckmarkIcon(tint = Color(0xEBFFFFFF))
        }
    }
}

/**
 * Isolated waiting crystal shimmer indicator: hides as soon as streamingMsgId becomes non-null
 * (1 transition per reply instead of per-character recomposition).
 */
@Composable
internal fun IsolatedWaitingShimmerItem(
    isSending: Boolean,
    streamingMsgIdFlow: StateFlow<String?>,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap
) {
    val activeStreamingId by streamingMsgIdFlow.collectAsStateWithLifecycle()
    if (!isSending || activeStreamingId != null) return

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .cosmicMessageEnter(messageKey = "shimmer_typing", isUser = false, animate = true)
            .padding(top = 4.dp, bottom = 6.dp)
    ) {
        val tW = maxWidth
        val shimmerTransition = rememberInfiniteTransition(label = "aetheriaShimmerTrans")
        val shimmerProgress by shimmerTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "aetheriaShimmerBar"
        )
        Row(
            modifier = Modifier
                .padding(start = tW * (123f / 841f))
                .height(38.dp)
                .morCamOuterGlow(cornerRadius = 19.dp, shadowAlpha = 0.30f, rimGlowAlpha = 0.28f, bottomFlareAlpha = 0.50f)
                .clip(RoundedCornerShape(50))
                .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.70f)
                .morCamGlassSurface(
                    baseSmokeColor = Color(0x48180832),
                    topRightFrostAlpha = 0.22f,
                    topLeftFrostAlpha = 0.16f,
                    topStarFrostAlpha = 0f,
                    bottomEdgeRefractionAlpha = 0.20f,
                    cornerRadius = 19.dp
                )
                .cosmicShimmerSweep(active = true, cornerRadius = 19.dp)
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFE9D5FF).copy(alpha = 0.55f + 0.35f * shimmerProgress),
                                Color(0xFFA855F7).copy(alpha = 0.45f),
                                Color(0xFFD8B4FE).copy(alpha = 0.85f - 0.30f * shimmerProgress)
                            )
                        )
                    ),
                    RoundedCornerShape(50)
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TypingDots(dotSize = 6.dp, dotColor = Color(0xFFE9D5FF))
            Text(
                text = "Yanıt hazırlanıyor",
                color = Color(0xFFE9D5FF).copy(alpha = 0.88f),
                fontSize = 12.sp,
                fontFamily = LiterarySerifFontFamily
            )
        }
    }
}

/**
 * Shared Mor Cam translucent reaction picker capsule with smooth VERTICAL center-anchored
 * spring entrance/exit and staggered bouncy emoji wave.
 */
@Composable
internal fun MorCamReactionPickerCapsule(
    visible: Boolean,
    messageId: String,
    selectedReaction: String,
    reactionEmojis: List<String>,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    onSelectEmoji: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(160)) +
            expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMedium)
            ) +
            slideInVertically(
                initialOffsetY = { -it / 3 },
                animationSpec = spring(dampingRatio = 0.68f, stiffness = Spring.StiffnessMedium)
            ) +
            scaleIn(
                initialScale = 0.84f,
                transformOrigin = TransformOrigin(0.5f, 0f),
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium)
            ),
        exit = fadeOut(tween(120)) +
            shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(140, easing = FastOutSlowInEasing)
            ) +
            slideOutVertically(
                targetOffsetY = { -it / 3 },
                animationSpec = tween(140, easing = FastOutSlowInEasing)
            ) +
            scaleOut(
                targetScale = 0.86f,
                transformOrigin = TransformOrigin(0.5f, 0f),
                animationSpec = tween(120)
            )
    ) {
        Box(
            modifier = modifier
                .morCamOuterGlow(cornerRadius = 24.dp, shadowAlpha = 0.36f, rimGlowAlpha = 0.36f, bottomFlareAlpha = 0.65f)
                .clip(RoundedCornerShape(24.dp))
                .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.76f)
                .morCamGlassSurface(
                    baseSmokeColor = Color(0x36180830),
                    topRightFrostAlpha = 0.24f,
                    topLeftFrostAlpha = 0.16f,
                    topStarFrostAlpha = 0f,
                    bottomEdgeRefractionAlpha = 0.22f
                )
                .border(
                    1.dp,
                    Brush.linearGradient(listOf(Color(0xFFF3E8FF), Color(0xFFA855F7), Color(0xFFD8B4FE))),
                    RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .testTag("reaction_picker_$messageId")
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                reactionEmojis.forEachIndexed { idx, emoji ->
                    val isSelected = selectedReaction == emoji
                    val emojiInteraction = remember { MutableInteractionSource() }
                    val emojiPressed by emojiInteraction.collectIsPressedAsState()

                    val enterScale = remember(messageId, idx) { Animatable(0.42f) }
                    val enterOffsetY = remember(messageId, idx) { Animatable(14f) }
                    val enterAlpha = remember(messageId, idx) { Animatable(0f) }

                    LaunchedEffect(visible, messageId, idx) {
                        if (visible) {
                            enterScale.snapTo(0.42f)
                            enterOffsetY.snapTo(14f)
                            enterAlpha.snapTo(0f)
                            delay(idx * 28L)
                            coroutineScope {
                                launch { enterAlpha.animateTo(1f, tween(130)) }
                                launch {
                                    enterOffsetY.animateTo(
                                        0f,
                                        spring(dampingRatio = 0.56f, stiffness = Spring.StiffnessMedium)
                                    )
                                }
                                launch {
                                    enterScale.animateTo(
                                        1f,
                                        spring(dampingRatio = 0.48f, stiffness = Spring.StiffnessMedium)
                                    )
                                }
                            }
                        }
                    }

                    val interactScale by animateFloatAsState(
                        targetValue = when {
                            emojiPressed -> 0.84f
                            isSelected -> 1.18f
                            else -> 1f
                        },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "emojiInteractScale_${messageId}_$idx"
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                val combinedScale = enterScale.value * interactScale
                                scaleX = combinedScale
                                scaleY = combinedScale
                                translationY = enterOffsetY.value
                                alpha = enterAlpha.value
                            }
                            .clip(CircleShape)
                            .background(
                                if (isSelected)
                                    Brush.radialGradient(listOf(Color(0xCC9333EA), Color(0x994C1D95)))
                                else
                                    Brush.radialGradient(listOf(Color(0x36A855F7), Color(0x10000000)))
                            )
                            .then(
                                if (isSelected) Modifier.border(1.dp, Color(0xFFF5D0FE), CircleShape)
                                else Modifier
                            )
                            .clickable(interactionSource = emojiInteraction, indication = null) {
                                onSelectEmoji(if (isSelected) "" else emoji)
                            }
                            .testTag("reaction_emoji_${messageId}_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 19.sp)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ChatLoadOlderMessagesHeader(
    nextBatchCount: Int,
    hiddenOlderCount: Int,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    onLoadOlder: () -> Unit
) {
    val loadMoreInteraction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .cosmicTapSpring(loadMoreInteraction, pressedScale = 0.95f)
                .morCamOuterGlow(cornerRadius = 22.dp, shadowAlpha = 0.28f, rimGlowAlpha = 0.24f, bottomFlareAlpha = 0.42f)
                .clip(RoundedCornerShape(50))
                .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.70f)
                .morCamGlassSurface(
                    baseSmokeColor = Color(0x421A0A38),
                    topRightFrostAlpha = 0.22f,
                    topLeftFrostAlpha = 0.16f,
                    topStarFrostAlpha = 0f,
                    bottomEdgeRefractionAlpha = 0.18f
                )
                .border(
                    BorderStroke(
                        0.95.dp,
                        Brush.linearGradient(
                            listOf(Color(0xD8F3E8FF), Color(0x66A855F7), Color(0xCCE9D5FF))
                        )
                    ),
                    RoundedCornerShape(50)
                )
                .clickable(
                    interactionSource = loadMoreInteraction,
                    indication = null,
                    onClick = onLoadOlder
                )
                .padding(horizontal = 16.dp, vertical = 9.dp)
                .testTag("chat_load_older_messages_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "Eski Mesajları Göster",
                tint = Color(0xFFE9D5FF),
                modifier = Modifier.size(16.dp)
            )
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Daha Fazla Mesaj Göster (+$nextBatchCount)",
                    color = Color(0xFFFBF8FF),
                    fontSize = 12.2.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = InterFontFamily
                )
                Text(
                    text = "$hiddenOlderCount eski mesaj sistemde kayıtlı",
                    color = Color(0xCCDDD6FE),
                    fontSize = 10.sp,
                    fontFamily = InterFontFamily
                )
            }
        }
    }
}

@Composable
internal fun ChatInlineEditMessageCard(
    editingText: String,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    onEditingTextChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    MorCamGlassCard(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.62f),
        cornerRadius = 20.dp,
        highlighted = true,
        paddingHorizontal = 14.dp,
        paddingVertical = 14.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0x888B5CF6), Color(0x553B1470))))
                    .border(0.9.dp, MorCamTokens.RimGradient, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MorCamTokens.SilverLavenderBright,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = "Mesajı Düzenle",
                color = MorCamTokens.SilverLavenderBright,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = LiterarySerifFontFamily
            )
        }
        MorCamGlassTextField(
            value = editingText,
            onValueChange = onEditingTextChange,
            placeholder = "Mesaj metnini düzenle...",
            singleLine = false,
            minHeight = 76.dp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MorCamCapsuleButton(
                text = "Vazgeç",
                onClick = onCancel,
                variant = MorCamButtonVariant.Secondary,
                modifier = Modifier.weight(1f)
            )
            MorCamCapsuleButton(
                text = "Kaydet",
                onClick = onSave,
                variant = MorCamButtonVariant.Primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ChatUserMessageItem(
    msg: MessageEntity,
    timeFormatted: String,
    isOldMessage: Boolean,
    hasBotStartedReplying: Boolean,
    isFreshMsg: Boolean,
    isHighlighted: Boolean,
    isExpandedOld: Boolean,
    isMenuOpen: Boolean,
    isReactionPickerOpen: Boolean,
    anyReactionPickerOpen: Boolean,
    activeHighlightQuery: String,
    copiedMsgId: String?,
    isSending: Boolean,
    reactionEmojis: List<String>,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    refGlassBitmaps: AetheriaRefGlassBitmaps,
    onToggleMenu: () -> Unit,
    onCloseReactionPicker: () -> Unit,
    onToggleReactionPicker: () -> Unit,
    onToggleExpandOld: () -> Unit,
    onSetReaction: (String) -> Unit,
    onCopied: () -> Unit,
    onContinueScene: () -> Unit,
    onToggleStar: () -> Unit,
    onEdit: () -> Unit,
    onRewind: () -> Unit,
    onDelete: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val userInteraction = remember { MutableInteractionSource() }
    val isUserPressed by userInteraction.collectIsPressedAsState()
    val userScale by animateFloatAsState(
        targetValue = if (isUserPressed) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "userBubbleScale_${msg.id}"
    )
    var showSecondaryUserActions by remember(msg.id) { mutableStateOf(false) }
    val userPillsScrollState = rememberScrollState()

    LaunchedEffect(isMenuOpen) {
        if (!isMenuOpen) showSecondaryUserActions = false
    }
    LaunchedEffect(showSecondaryUserActions) {
        if (showSecondaryUserActions) {
            delay(60)
            userPillsScrollState.animateScrollTo(260)
        }
    }

    var userStarBurstTrigger by remember(msg.id) { mutableIntStateOf(0) }
    val userHighlightAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0f,
        animationSpec = tween(320),
        label = "userHighlightAlpha_${msg.id}"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .cosmicMessageEnter(messageKey = msg.id, isUser = true, animate = isFreshMsg)
            .graphicsLayer {
                scaleX = userScale
                scaleY = userScale
            }
            .drawBehind {
                if (userHighlightAlpha > 0.01f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFFFDE68A).copy(alpha = 0.28f * userHighlightAlpha),
                                Color(0xFFD8B4FE).copy(alpha = 0.34f * userHighlightAlpha)
                            )
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                }
            }
            .aetheriaThreeSliceGlassBubble(
                rootSize = rootSize,
                blurredBitmap = blurredBgBitmap,
                topSlice = refGlassBitmaps.userTop,
                midSlice = refGlassBitmaps.userMid,
                bottomSlice = refGlassBitmaps.userBottom,
                isUserBubble = true,
                frostedAlpha = 0.62f
            )
            .combinedClickable(
                interactionSource = userInteraction,
                indication = null,
                onClick = {
                    if (anyReactionPickerOpen) {
                        onCloseReactionPicker()
                    } else {
                        onToggleMenu()
                    }
                },
                onLongClick = onToggleReactionPicker
            )
    ) {
        val uW = maxWidth
        val cleanUserText = remember(msg.text) { cleanCopyText(msg.text) }
        val isUserMsgAutoCollapsible = remember(isOldMessage, cleanUserText) {
            isMessageAutoCollapsible(isOldMessage, cleanUserText)
        }
        val isUserMsgCollapsed = isUserMsgAutoCollapsible && !isExpandedOld
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing))
                .heightIn(min = uW * (108f / 841f))
                .padding(
                    start = uW * (204f / 841f),
                    end = uW * (48f / 841f),
                    top = 21.dp,
                    bottom = 14.dp
                )
        ) {
            Text(
                text = remember(cleanUserText, activeHighlightQuery) {
                    formatNarrativeTextWithHighlight(cleanUserText, activeHighlightQuery)
                },
                color = Color.White.copy(alpha = 0.97f),
                fontSize = 15.sp,
                lineHeight = 22.5.sp,
                letterSpacing = 0.1.sp,
                fontFamily = LiterarySerifFontFamily,
                maxLines = if (isUserMsgCollapsed) 4 else Int.MAX_VALUE,
                overflow = if (isUserMsgCollapsed) TextOverflow.Ellipsis else TextOverflow.Clip,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0x88120524),
                        offset = Offset(0f, 1f),
                        blurRadius = 3.5f
                    )
                )
            )
            if (isUserMsgAutoCollapsible) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x5226104C))
                        .border(0.8.dp, Color(0x88D8B4FE), RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleExpandOld
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("chat_expand_msg_${msg.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (isUserMsgCollapsed) "Daha Fazla Göster" else "Daha Az Göster",
                        color = Color(0xFFF3E8FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = InterFontFamily
                    )
                    Icon(
                        imageVector = if (isUserMsgCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = if (isUserMsgCollapsed) "Daha Fazla Göster" else "Daha Az Göster",
                        tint = Color(0xFFE9D5FF),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AnimatedVisibility(
                        visible = msg.reaction.isNotBlank(),
                        enter = fadeIn(tween(150)) +
                            scaleIn(
                                initialScale = 0.55f,
                                animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMedium)
                            ),
                        exit = fadeOut(tween(110)) +
                            scaleOut(targetScale = 0.6f, animationSpec = tween(110))
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x5A3B1568))
                                .border(0.9.dp, Color(0xCCE9D5FF), RoundedCornerShape(50))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onSetReaction("") }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .testTag("user_reaction_badge_${msg.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = msg.reaction, fontSize = 13.sp)
                        }
                    }
                    if (msg.isStarred || userStarBurstTrigger > 0) {
                        Box(contentAlignment = Alignment.Center) {
                            if (msg.isStarred) {
                                Text(
                                    text = "⭐",
                                    fontSize = 12.sp,
                                    modifier = Modifier.cosmicPopOnTrigger(userStarBurstTrigger, peakScale = 1.35f)
                                )
                            }
                            CosmicSparkleBurst(
                                triggerKey = userStarBurstTrigger,
                                accentColor = Color(0xFFFDE68A),
                                secondaryColor = Color(0xFFD8B4FE),
                                particleCount = 8,
                                maxRadius = 20.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormatted.ifBlank { "22:47" },
                        color = Color(0xEBF8F5FF),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = InterFontFamily
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    if (hasBotStartedReplying) {
                        DoubleCheckmarkIcon(tint = Color(0xEBFFFFFF))
                    } else {
                        SingleCheckmarkIcon(tint = Color(0xEBFFFFFF))
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            visible = isMenuOpen && !anyReactionPickerOpen,
            enter = fadeIn(tween(180)) + expandVertically(tween(180)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(140))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0.0f to Color.Transparent,
                                0.05f to Color.Black,
                                0.95f to Color.Black,
                                1.0f to Color.Transparent
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
                    .horizontalScroll(userPillsScrollState)
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                @Composable
                fun UserGlassActionPill(
                    iconType: String? = null,
                    label: String,
                    isDanger: Boolean = false,
                    isActive: Boolean = false,
                    onClick: () -> Unit
                ) {
                    val pillInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .cosmicTapSpring(
                                interactionSource = pillInteraction,
                                pressedScale = 0.93f,
                                cornerRadius = 20.dp
                            )
                            .morCamOuterGlow(cornerRadius = 20.dp, shadowAlpha = 0.34f, rimGlowAlpha = 0.24f, bottomFlareAlpha = 0.42f)
                            .clip(RoundedCornerShape(50))
                            .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.85f)
                            .morCamGlassSurface(
                                baseSmokeColor = when {
                                    isDanger -> Color(0x603E0E1A)
                                    isActive -> Color(0x664C1D95)
                                    else -> Color(0x52160A2B)
                                },
                                topRightFrostAlpha = 0.22f,
                                topLeftFrostAlpha = 0.18f,
                                topStarFrostAlpha = 0f,
                                bottomEdgeRefractionAlpha = 0.18f,
                                cornerRadius = 20.dp
                            )
                            .border(
                                BorderStroke(
                                    0.9.dp,
                                    if (isDanger) {
                                        Brush.linearGradient(listOf(Color(0xCCF87171), Color(0x99EF4444)))
                                    } else {
                                        Brush.linearGradient(listOf(Color(0xCC9F70E6), Color(0x997B4BC2), Color(0xCCA879EC)))
                                    }
                                ),
                                RoundedCornerShape(50)
                            )
                            .morCamTopAndBottomSpecular(cornerRadius = 20.dp, topSheenAlpha = 0.22f, bottomFlareAlpha = 0.44f)
                            .clickable(interactionSource = pillInteraction, indication = null) { onClick() }
                            .padding(start = if (iconType != null) 7.dp else 14.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (iconType != null) {
                            GlassPillIconBadge(type = iconType, modifier = Modifier.size(26.dp))
                        }
                        Text(
                            text = label,
                            color = if (isDanger) Color(0xFFFCA5A5) else Color(0xFFFBF8FF),
                            fontSize = 13.sp,
                            fontFamily = LiterarySerifFontFamily
                        )
                    }
                }

                UserGlassActionPill(
                    iconType = "copy",
                    label = if (copiedMsgId == msg.id) "Kopyalandı ✓" else "Kopyala",
                    isActive = copiedMsgId == msg.id
                ) {
                    clipboardManager.setText(AnnotatedString(cleanCopyText(msg.text)))
                    onCopied()
                    android.widget.Toast.makeText(context, "Kopyalandı", android.widget.Toast.LENGTH_SHORT).show()
                }

                UserGlassActionPill(iconType = "continue", label = "Devam Et") {
                    if (!isSending) {
                        onContinueScene()
                    }
                }

                UserGlassActionPill(
                    iconType = "more",
                    label = "Diğer",
                    isActive = showSecondaryUserActions
                ) {
                    showSecondaryUserActions = !showSecondaryUserActions
                }

                AnimatedVisibility(
                    visible = showSecondaryUserActions,
                    enter = fadeIn(tween(180)) + expandHorizontally(tween(220, easing = FastOutSlowInEasing)),
                    exit = fadeOut(tween(140)) + shrinkHorizontally(tween(180))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserGlassActionPill(
                            label = if (msg.isStarred) "⭐ Yıldızı Kaldır" else "☆ Yıldızla",
                            isActive = msg.isStarred
                        ) {
                            val willStar = !msg.isStarred
                            if (willStar) userStarBurstTrigger += 1
                            onToggleStar()
                            android.widget.Toast.makeText(
                                context,
                                if (willStar) "★ Mesaj yıldızlandı — AI hafızasında öncelikli tutulacak" else "Yıldız kaldırıldı",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        UserGlassActionPill(label = "✏️ Düzenle") {
                            showSecondaryUserActions = false
                            onEdit()
                        }
                        UserGlassActionPill(label = "⏪ Buraya Sar") {
                            showSecondaryUserActions = false
                            onRewind()
                        }
                        UserGlassActionPill(label = "🗑️ Sil", isDanger = true) {
                            showSecondaryUserActions = false
                            onDelete()
                        }
                    }
                }
            }
        }

        MorCamReactionPickerCapsule(
            visible = isReactionPickerOpen,
            messageId = msg.id,
            selectedReaction = msg.reaction,
            reactionEmojis = reactionEmojis,
            rootSize = rootSize,
            blurredBgBitmap = blurredBgBitmap,
            onSelectEmoji = { chosen ->
                onSetReaction(chosen)
                onCloseReactionPicker()
            },
            modifier = Modifier.padding(end = 18.dp, top = 6.dp, bottom = 4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ChatBotMessageItem(
    bot: BotEntity,
    msg: MessageEntity,
    headerTitle: String,
    avatarInitial: String,
    timeFormatted: String,
    isOldMessage: Boolean,
    isLastMessage: Boolean,
    isFreshMsg: Boolean,
    isHighlighted: Boolean,
    isExpandedOld: Boolean,
    isMenuOpen: Boolean,
    isReactionPickerOpen: Boolean,
    anyReactionPickerOpen: Boolean,
    activeHighlightQuery: String,
    copiedMsgId: String?,
    isSending: Boolean,
    emotionAuraColor: Color,
    reactionEmojis: List<String>,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    refGlassBitmaps: AetheriaRefGlassBitmaps,
    streamingMsgIdFlow: StateFlow<String?>,
    streamingTextFlow: StateFlow<String?>,
    isStreamingThinkingFlow: StateFlow<Boolean> = NEVER_THINKING_FLOW,
    speakingMessageIdFlow: StateFlow<String?>,
    onToggleMenu: () -> Unit,
    onCloseMenu: () -> Unit,
    onCloseReactionPicker: () -> Unit,
    onToggleReactionPicker: () -> Unit,
    onToggleExpandOld: () -> Unit,
    onSetReaction: (String) -> Unit,
    onCopied: () -> Unit,
    onContinueScene: () -> Unit,
    onRegenerate: () -> Unit,
    onSpeakMessage: (String, String) -> Unit,
    onStopSpeaking: () -> Unit,
    onToggleStar: () -> Unit,
    onEdit: () -> Unit,
    onRewind: () -> Unit,
    onDelete: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var starBurstTrigger by remember(msg.id) { mutableIntStateOf(0) }
    val botBubbleInteraction = remember { MutableInteractionSource() }
    val isBotBubblePressed by botBubbleInteraction.collectIsPressedAsState()
    val botBubbleScale by animateFloatAsState(
        targetValue = when {
            isBotBubblePressed -> 0.985f
            isReactionPickerOpen -> 1.01f
            else -> 1f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "botBubbleScale_${msg.id}"
    )

    var showSecondaryAiActions by remember(msg.id) { mutableStateOf(false) }
    val aiPillsScrollState = rememberScrollState()

    LaunchedEffect(isMenuOpen) {
        if (!isMenuOpen) showSecondaryAiActions = false
    }
    LaunchedEffect(showSecondaryAiActions) {
        if (showSecondaryAiActions) {
            delay(60)
            aiPillsScrollState.animateScrollTo(320)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cosmicMessageEnter(messageKey = msg.id, isUser = false, animate = isFreshMsg)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .cosmicBorderBreathing(
                    enabled = isSending && isLastMessage,
                    cornerRadius = 24.dp,
                    glowColor = emotionAuraColor,
                    minAlpha = 0.14f,
                    maxAlpha = 0.45f,
                    periodMs = 1700
                )
                .graphicsLayer {
                    scaleX = botBubbleScale
                    scaleY = botBubbleScale
                }
                .aetheriaThreeSliceGlassBubble(
                    rootSize = rootSize,
                    blurredBitmap = blurredBgBitmap,
                    topSlice = refGlassBitmaps.botTop,
                    midSlice = refGlassBitmaps.botMid,
                    bottomSlice = refGlassBitmaps.botBottom,
                    isUserBubble = false,
                    frostedAlpha = 0.62f
                )
                .drawBehind {
                    if (isHighlighted) {
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0x55FDE68A),
                                    Color(0x48D8B4FE),
                                    Color.Transparent
                                )
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(26.dp.toPx(), 26.dp.toPx())
                        )
                    }
                }
                .combinedClickable(
                    interactionSource = botBubbleInteraction,
                    indication = null,
                    onClick = {
                        if (anyReactionPickerOpen) {
                            onCloseReactionPicker()
                        } else {
                            onToggleMenu()
                        }
                    },
                    onLongClick = {
                        showSecondaryAiActions = false
                        onToggleReactionPicker()
                    }
                )
        ) {
            val bW = maxWidth

            Box(
                modifier = Modifier
                    .offset(
                        x = bW * (18f / 841f),
                        y = bW * (8f / 841f)
                    )
                    .size(bW * (104f / 841f)),
                contentAlignment = Alignment.Center
            ) {
                if (refGlassBitmaps.sigilNoLetter.width > 1) {
                    Image(
                        bitmap = refGlassBitmaps.sigilNoLetter,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                val fallbackSigilText: @Composable () -> Unit = {
                    Text(
                        text = avatarInitial,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = DisplaySerifFontFamily,
                        style = TextStyle(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFE9D5FF),
                                    Color(0xFFC084FC)
                                )
                            ),
                            shadow = Shadow(
                                color = Color(0xCCA855F7),
                                offset = Offset(0f, 0f),
                                blurRadius = 10f
                            )
                        )
                    )
                }
                if (bot.avatarUrl.isNotBlank()) {
                    BotAvatarImage(
                        avatarUrl = bot.avatarUrl,
                        contentDescription = headerTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(bW * (54f / 841f))
                            .clip(CircleShape),
                        fallback = fallbackSigilText
                    )
                } else {
                    fallbackSigilText()
                }
            }

            val cleanBotText = remember(msg.text) { cleanCopyText(msg.text) }
            val isBotMsgAutoCollapsible = remember(isOldMessage, cleanBotText) {
                isMessageAutoCollapsible(isOldMessage, cleanBotText)
            }
            val isBotMsgCollapsed = isBotMsgAutoCollapsible && !isExpandedOld

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing))
                    .heightIn(min = bW * (180f / 841f))
                    .padding(
                        start = bW * (156f / 841f),
                        end = bW * (84f / 841f),
                        top = bW * (54f / 841f),
                        bottom = 16.dp
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IsolatedStreamingBotBubbleText(
                    messageId = msg.id,
                    persistedText = msg.text,
                    streamingMsgIdFlow = streamingMsgIdFlow,
                    streamingTextFlow = streamingTextFlow,
                    isStreamingThinkingFlow = isStreamingThinkingFlow,
                    isCollapsed = isBotMsgCollapsed,
                    highlightQuery = activeHighlightQuery
                )

                if (isBotMsgAutoCollapsible) {
                    Spacer(modifier = Modifier.height(7.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x5226104C))
                            .border(0.85.dp, Color(0x88D8B4FE), RoundedCornerShape(50))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleExpandOld
                            )
                            .padding(horizontal = 11.dp, vertical = 4.5.dp)
                            .testTag("chat_expand_msg_${msg.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isBotMsgCollapsed) "Daha Fazla Göster" else "Daha Az Göster",
                            color = Color(0xFFF3E8FF),
                            fontSize = 11.2.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily
                        )
                        Icon(
                            imageVector = if (isBotMsgCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isBotMsgCollapsed) "Daha Fazla Göster" else "Daha Az Göster",
                            tint = Color(0xFFE9D5FF),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnimatedVisibility(
                            visible = msg.reaction.isNotBlank(),
                            enter = fadeIn(tween(150)) +
                                scaleIn(
                                    initialScale = 0.55f,
                                    animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMedium)
                                ),
                            exit = fadeOut(tween(110)) +
                                scaleOut(targetScale = 0.6f, animationSpec = tween(110))
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0x553B1568))
                                    .border(0.9.dp, Color(0xBBE9D5FF), RoundedCornerShape(50))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onSetReaction("") }
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .testTag("ai_reaction_badge_${msg.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = msg.reaction, fontSize = 13.sp)
                            }
                        }
                        if (msg.isStarred || starBurstTrigger > 0) {
                            Box(contentAlignment = Alignment.Center) {
                                if (msg.isStarred) {
                                    Text(
                                        text = "⭐",
                                        fontSize = 12.sp,
                                        modifier = Modifier.cosmicPopOnTrigger(starBurstTrigger, peakScale = 1.35f)
                                    )
                                }
                                CosmicSparkleBurst(
                                    triggerKey = starBurstTrigger,
                                    accentColor = Color(0xFFFDE68A),
                                    secondaryColor = Color(0xFFD8B4FE),
                                    particleCount = 8,
                                    maxRadius = 20.dp,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        IsolatedStreamingCaret(
                            messageId = msg.id,
                            streamingMsgIdFlow = streamingMsgIdFlow,
                            color = emotionAuraColor
                        )
                    }

                    IsolatedMessageTtsFooter(
                        messageId = msg.id,
                        messageText = msg.text,
                        timeFormatted = timeFormatted,
                        speakingMessageIdFlow = speakingMessageIdFlow,
                        onSpeakMessage = onSpeakMessage,
                        onStopSpeaking = onStopSpeaking
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isMenuOpen && !anyReactionPickerOpen,
            enter = fadeIn(tween(180)) + expandVertically(tween(180)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(140))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val rowStartPad = maxWidth * (123f / 841f)
                val rowEndPad = if (showSecondaryAiActions) 24.dp else maxWidth * (59f / 841f)
                val startFraction = (rowStartPad / maxWidth).coerceIn(0.05f, 0.25f)

                @Composable
                fun BotGlassActionPill(
                    iconType: String? = null,
                    label: String,
                    enabled: Boolean = true,
                    isDanger: Boolean = false,
                    isActive: Boolean = false,
                    pillModifier: Modifier = Modifier,
                    onClick: () -> Unit
                ) {
                    val pillInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = pillModifier
                            .height(38.dp)
                            .cosmicTapSpring(
                                interactionSource = pillInteraction,
                                enabled = enabled,
                                pressedScale = 0.93f,
                                cornerRadius = 19.dp
                            )
                            .morCamOuterGlow(cornerRadius = 19.dp, shadowAlpha = 0.34f, rimGlowAlpha = 0.24f, bottomFlareAlpha = 0.42f)
                            .clip(RoundedCornerShape(50))
                            .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.82f)
                            .morCamGlassSurface(
                                baseSmokeColor = when {
                                    isDanger -> Color(0x603E0E1A)
                                    isActive -> Color(0x664C1D95)
                                    else -> Color(0x52160A2B)
                                },
                                topRightFrostAlpha = 0.22f,
                                topLeftFrostAlpha = 0.18f,
                                topStarFrostAlpha = 0f,
                                bottomEdgeRefractionAlpha = 0.18f,
                                cornerRadius = 19.dp
                            )
                            .border(
                                BorderStroke(
                                    0.95.dp,
                                    if (isDanger) {
                                        Brush.linearGradient(listOf(Color(0xCCF87171), Color(0x99EF4444)))
                                    } else if (isActive) {
                                        Brush.linearGradient(listOf(Color(0xEEFAF5FF), Color(0xCCA855F7), Color(0xEEE9D5FF)))
                                    } else {
                                        Brush.linearGradient(listOf(Color(0xCCE9D5FF), Color(0x889333EA), Color(0xCCD8B4FE)))
                                    }
                                ),
                                RoundedCornerShape(50)
                            )
                            .morCamTopAndBottomSpecular(cornerRadius = 19.dp, topSheenAlpha = 0.24f, bottomFlareAlpha = 0.46f)
                            .clickable(enabled = enabled, interactionSource = pillInteraction, indication = null) { onClick() }
                            .padding(start = if (iconType != null) 6.dp else 12.dp, end = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
                    ) {
                        if (iconType != null) {
                            GlassPillIconBadge(type = iconType, modifier = Modifier.size(23.dp))
                        }
                        Text(
                            text = label,
                            color = if (isDanger) Color(0xFFFCA5A5) else Color(0xFFFBF8FF),
                            fontSize = 12.2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = LiterarySerifFontFamily
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (showSecondaryAiActions) {
                                Modifier
                                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                    .drawWithContent {
                                        drawContent()
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                0.0f to Color.Transparent,
                                                (startFraction * 0.72f) to Color.Transparent,
                                                (startFraction + 0.04f) to Color.Black,
                                                0.94f to Color.Black,
                                                1.0f to Color.Transparent
                                            ),
                                            blendMode = BlendMode.DstIn
                                        )
                                    }
                                    .horizontalScroll(aiPillsScrollState)
                            } else {
                                Modifier
                            }
                        )
                        .padding(start = rowStartPad, end = rowEndPad, top = 6.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotGlassActionPill(
                        iconType = "copy",
                        label = if (copiedMsgId == msg.id) "Kopyalandı ✓" else "Kopyala",
                        isActive = copiedMsgId == msg.id,
                        pillModifier = if (!showSecondaryAiActions) Modifier.weight(1.06f) else Modifier
                    ) {
                        clipboardManager.setText(AnnotatedString(cleanCopyText(msg.text)))
                        onCopied()
                        android.widget.Toast.makeText(context, "Kopyalandı", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    BotGlassActionPill(
                        iconType = "continue",
                        label = "Devam Et",
                        enabled = !isSending,
                        pillModifier = if (!showSecondaryAiActions) Modifier.weight(1.16f) else Modifier
                    ) {
                        onCloseMenu()
                        showSecondaryAiActions = false
                        onContinueScene()
                    }
                    BotGlassActionPill(
                        iconType = "more",
                        label = "Diğer",
                        isActive = showSecondaryAiActions,
                        pillModifier = if (!showSecondaryAiActions) Modifier.weight(0.92f) else Modifier
                    ) {
                        showSecondaryAiActions = !showSecondaryAiActions
                    }

                    AnimatedVisibility(
                        visible = showSecondaryAiActions,
                        enter = fadeIn(tween(180)) + expandHorizontally(tween(220, easing = FastOutSlowInEasing)),
                        exit = fadeOut(tween(140)) + shrinkHorizontally(tween(180))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BotGlassActionPill(label = "🔄 Yeniden Oluştur", enabled = !isSending) {
                                onCloseMenu()
                                showSecondaryAiActions = false
                                onRegenerate()
                            }
                            val activeSpeakingIdForPill by speakingMessageIdFlow.collectAsStateWithLifecycle()
                            val isThisPillSpeaking = activeSpeakingIdForPill == msg.id
                            BotGlassActionPill(
                                label = if (isThisPillSpeaking) "⏹️ Durdur" else "🎙️ Sesli Oku",
                                isActive = isThisPillSpeaking
                            ) {
                                if (isThisPillSpeaking) {
                                    onStopSpeaking()
                                } else {
                                    onSpeakMessage(msg.id, msg.text)
                                }
                            }
                            BotGlassActionPill(
                                label = if (msg.isStarred) "⭐ Yıldızı Kaldır" else "☆ Yıldızla",
                                isActive = msg.isStarred
                            ) {
                                val willStar = !msg.isStarred
                                if (willStar) starBurstTrigger += 1
                                onToggleStar()
                                android.widget.Toast.makeText(
                                    context,
                                    if (willStar) "★ Mesaj yıldızlandı — AI hafızasında öncelikli tutulacak" else "Yıldız kaldırıldı",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                            BotGlassActionPill(label = "⏪ Buraya Sar") {
                                onCloseMenu()
                                showSecondaryAiActions = false
                                onRewind()
                            }
                            BotGlassActionPill(label = "✏️ Düzenle") {
                                onCloseMenu()
                                showSecondaryAiActions = false
                                onEdit()
                            }
                            BotGlassActionPill(label = "🗑️ Sil", isDanger = true) {
                                onCloseMenu()
                                showSecondaryAiActions = false
                                onDelete()
                            }
                        }
                    }
                }
            }
        }

        MorCamReactionPickerCapsule(
            visible = isReactionPickerOpen,
            messageId = msg.id,
            selectedReaction = msg.reaction,
            reactionEmojis = reactionEmojis,
            rootSize = rootSize,
            blurredBgBitmap = blurredBgBitmap,
            onSelectEmoji = { chosen ->
                onSetReaction(chosen)
                onCloseReactionPicker()
            },
            modifier = Modifier.padding(start = 48.dp, top = 6.dp, bottom = 4.dp)
        )
    }
}

/**
 * Modular LazyColumn message timeline with Mor Cam multi-stop DstIn atmospheric fade mask,
 * 20-message load-older header, inline editing, user/bot bubble delegation, and isolated shimmer.
 */
@Composable
internal fun ChatMessageTimelineList(
    bot: BotEntity,
    displayName: String,
    avatarInitial: String,
    listState: LazyListState,
    topInsetPadding: Dp,
    bottomInsetPadding: Dp,
    validMessages: List<MessageEntity>,
    initialMessageIds: Set<String>,
    hasLoadOlderHeader: Boolean,
    chatPageSize: Int,
    hiddenOlderCount: Int,
    editingMessageId: String?,
    editingText: String,
    highlightedMessageId: String?,
    expandedOldMessageIds: Set<String>,
    expandedMenuMessageId: String?,
    expandedUserMenuMessageId: String?,
    reactionPickerMessageId: String?,
    activeHighlightQuery: String,
    copiedMsgId: String?,
    isSending: Boolean,
    emotionAuraColor: Color,
    reactionEmojis: List<String>,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    refGlassBitmaps: AetheriaRefGlassBitmaps,
    streamingMsgIdFlow: StateFlow<String?>,
    streamingTextFlow: StateFlow<String?>,
    isStreamingThinkingFlow: StateFlow<Boolean> = NEVER_THINKING_FLOW,
    speakingMessageIdFlow: StateFlow<String?>,
    onLoadOlder: (nextBatchCount: Int) -> Unit,
    onEditingTextChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String, String) -> Unit,
    onToggleUserMenu: (String) -> Unit,
    onToggleBotMenu: (String) -> Unit,
    onCloseBotMenu: () -> Unit,
    onCloseReactionPicker: () -> Unit,
    onToggleReactionPicker: (String) -> Unit,
    onToggleExpandOld: (String) -> Unit,
    onSetReaction: (String, String) -> Unit,
    onCopied: (String) -> Unit,
    onContinueScene: () -> Unit,
    onRegenerate: () -> Unit,
    onSpeakMessage: (String, String) -> Unit,
    onStopSpeaking: () -> Unit,
    onToggleStar: (String) -> Unit,
    onStartEdit: (MessageEntity) -> Unit,
    onRequestRewind: (String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Precompute once in O(N) outside LazyColumn so user bubbles check reply status in O(1) instead of O(N^2)
    val lastNonBlankAssistantIndex = remember(validMessages) {
        computeLastAssistantReplyIndex(validMessages)
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            top = (topInsetPadding + 8.dp).coerceAtLeast(88.dp),
            bottom = (bottomInsetPadding + 14.dp).coerceAtLeast(144.dp)
        ),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                val h = size.height
                if (h > 10f) {
                    val topClearEnd = ((topInsetPadding - 22.dp).toPx().coerceAtLeast(0f) / h).coerceIn(0f, 0.25f)
                    val topFadeMid1 = ((topInsetPadding - 4.dp).toPx().coerceAtLeast(0f) / h).coerceIn(topClearEnd + 0.005f, 0.30f)
                    val topFadeMid2 = ((topInsetPadding + 14.dp).toPx().coerceAtLeast(0f) / h).coerceIn(topFadeMid1 + 0.005f, 0.35f)
                    val topFadeEnd = ((topInsetPadding + 36.dp).toPx().coerceAtLeast(0f) / h).coerceIn(topFadeMid2 + 0.005f, 0.42f)
                    val botFadeStart = ((h - (bottomInsetPadding + 22.dp).toPx()) / h).coerceIn(0.55f, 0.94f)
                    val botFadeEnd = ((h - (bottomInsetPadding - 10.dp).toPx()) / h).coerceIn(botFadeStart + 0.005f, 0.99f)

                    drawRect(
                        brush = Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            topClearEnd to Color.Transparent,
                            topFadeMid1 to Color.Black.copy(alpha = 0.28f),
                            topFadeMid2 to Color.Black.copy(alpha = 0.68f),
                            topFadeEnd to Color.Black,
                            botFadeStart to Color.Black,
                            botFadeEnd to Color.Black.copy(alpha = 0.18f),
                            1.0f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
            },
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (hasLoadOlderHeader) {
            item(key = "load_older_messages_header") {
                val nextBatchCount = minOf(chatPageSize, hiddenOlderCount)
                ChatLoadOlderMessagesHeader(
                    nextBatchCount = nextBatchCount,
                    hiddenOlderCount = hiddenOlderCount,
                    rootSize = rootSize,
                    blurredBgBitmap = blurredBgBitmap,
                    onLoadOlder = { onLoadOlder(nextBatchCount) }
                )
            }
        }

        itemsIndexed(validMessages, key = { _, msg -> msg.id }) { index, msg ->
            // Stable boolean (only flips once when message ages past the 5 most recent) so older bubbles skip recomposition
            val isOldMessage = (validMessages.size - index) > 5
            val timeFormatted = remember(msg.timestamp) { formatBubbleClockTime(msg.timestamp) }

            if (editingMessageId == msg.id) {
                ChatInlineEditMessageCard(
                    editingText = editingText,
                    rootSize = rootSize,
                    blurredBgBitmap = blurredBgBitmap,
                    onEditingTextChange = onEditingTextChange,
                    onCancel = onCancelEdit,
                    onSave = { onSaveEdit(msg.id, editingText) }
                )
            } else if (msg.role == "user") {
                val hasBotStartedReplying = index < lastNonBlankAssistantIndex
                ChatUserMessageItem(
                    msg = msg,
                    timeFormatted = timeFormatted,
                    isOldMessage = isOldMessage,
                    hasBotStartedReplying = hasBotStartedReplying,
                    isFreshMsg = msg.id !in initialMessageIds,
                    isHighlighted = highlightedMessageId == msg.id,
                    isExpandedOld = msg.id in expandedOldMessageIds,
                    isMenuOpen = expandedUserMenuMessageId == msg.id,
                    isReactionPickerOpen = reactionPickerMessageId == msg.id,
                    anyReactionPickerOpen = reactionPickerMessageId != null,
                    activeHighlightQuery = activeHighlightQuery,
                    copiedMsgId = copiedMsgId,
                    isSending = isSending,
                    reactionEmojis = reactionEmojis,
                    rootSize = rootSize,
                    blurredBgBitmap = blurredBgBitmap,
                    refGlassBitmaps = refGlassBitmaps,
                    onToggleMenu = { onToggleUserMenu(msg.id) },
                    onCloseReactionPicker = onCloseReactionPicker,
                    onToggleReactionPicker = { onToggleReactionPicker(msg.id) },
                    onToggleExpandOld = { onToggleExpandOld(msg.id) },
                    onSetReaction = { onSetReaction(msg.id, it) },
                    onCopied = { onCopied(msg.id) },
                    onContinueScene = onContinueScene,
                    onToggleStar = { onToggleStar(msg.id) },
                    onEdit = { onStartEdit(msg) },
                    onRewind = { onRequestRewind(msg.id) },
                    onDelete = { onDeleteMessage(msg.id) }
                )
            } else {
                ChatBotMessageItem(
                    bot = bot,
                    msg = msg,
                    headerTitle = displayName,
                    avatarInitial = avatarInitial,
                    timeFormatted = timeFormatted,
                    isOldMessage = isOldMessage,
                    isLastMessage = index == validMessages.lastIndex,
                    isFreshMsg = msg.id !in initialMessageIds,
                    isHighlighted = highlightedMessageId == msg.id,
                    isExpandedOld = msg.id in expandedOldMessageIds,
                    isMenuOpen = expandedMenuMessageId == msg.id,
                    isReactionPickerOpen = reactionPickerMessageId == msg.id,
                    anyReactionPickerOpen = reactionPickerMessageId != null,
                    activeHighlightQuery = activeHighlightQuery,
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
                    onToggleMenu = { onToggleBotMenu(msg.id) },
                    onCloseMenu = onCloseBotMenu,
                    onCloseReactionPicker = onCloseReactionPicker,
                    onToggleReactionPicker = { onToggleReactionPicker(msg.id) },
                    onToggleExpandOld = { onToggleExpandOld(msg.id) },
                    onSetReaction = { onSetReaction(msg.id, it) },
                    onCopied = { onCopied(msg.id) },
                    onContinueScene = onContinueScene,
                    onRegenerate = onRegenerate,
                    onSpeakMessage = onSpeakMessage,
                    onStopSpeaking = onStopSpeaking,
                    onToggleStar = { onToggleStar(msg.id) },
                    onEdit = { onStartEdit(msg) },
                    onRewind = { onRequestRewind(msg.id) },
                    onDelete = { onDeleteMessage(msg.id) }
                )
            }
        }

        if (isSending) {
            item(key = "aetheria_shimmer_indicator") {
                IsolatedWaitingShimmerItem(
                    isSending = isSending,
                    streamingMsgIdFlow = streamingMsgIdFlow,
                    rootSize = rootSize,
                    blurredBgBitmap = blurredBgBitmap
                )
            }
        }
    }
}


