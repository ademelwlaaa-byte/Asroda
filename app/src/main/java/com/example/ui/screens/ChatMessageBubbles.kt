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
import com.example.ui.theme.LiterarySerifFontFamily
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
    val effectiveCollapsed = isCollapsed && !isThisStreaming
    val annotated = remember(displayText, highlightQuery) {
        formatNarrativeTextWithHighlight(displayText, highlightQuery)
    }

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
