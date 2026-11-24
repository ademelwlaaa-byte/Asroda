package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.cosmicFloatingLevitation
import com.example.ui.animation.cosmicTapSpring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.example.ui.animation.CosmicSparkleBurst
import com.example.ui.animation.cosmicPopOnTrigger
import com.example.ui.theme.LiterarySerifFontFamily

/**
 * Modular 1:1 Mor Cam Bottom Input Dock containing:
 * - (+) RP & Sahne Araçları Drawer
 * - 3 Quick-Suggestion Glass Cards Plate (Sahneyi Derinleştir / Ne Yapmalıyım? / Düzenle)
 * - Translucent Glass Input Bar Plate ((+), BasicTextField, 📎 Background Picker, ➤ Send Burst)
 */
@Composable
internal fun ChatBottomInputDock(
    inputText: String,
    isSending: Boolean,
    canEditLastMessage: Boolean,
    showPlusToolkit: Boolean,
    sendBurstTrigger: Int,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    refGlassBitmaps: AetheriaRefGlassBitmaps,
    onInputTextChange: (String) -> Unit,
    onTogglePlusToolkit: () -> Unit,
    onDismissPlusToolkit: () -> Unit,
    onOpenStarredSheet: () -> Unit,
    onContinueScene: () -> Unit,
    onSendMessage: (String) -> Unit,
    onEditLastMessage: () -> Unit,
    onOpenBackgroundPicker: () -> Unit,
    onTriggerSendBurst: () -> Unit,
    onStopGeneration: (() -> Unit)? = null,
    onCopyTranscript: (() -> Unit)? = null,
    onShareTranscript: (() -> Unit)? = null,
    onOpenStoryStats: (() -> Unit)? = null,
    onOpenBookmarks: (() -> Unit)? = null,
    autoTtsEnabled: Boolean = false,
    onToggleAutoTts: (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(top = 4.dp, bottom = 4.dp)
        ) {
            ChatPlusToolkitDrawer(
                visible = showPlusToolkit,
                isSending = isSending,
                inputText = inputText,
                rootSize = rootSize,
                blurredBgBitmap = blurredBgBitmap,
                onDismiss = onDismissPlusToolkit,
                onOpenStarredSheet = onOpenStarredSheet,
                onContinueScene = onContinueScene,
                onSendPrompt = onSendMessage,
                onUpdateInputText = onInputTextChange,
                onCopyTranscript = onCopyTranscript,
                onShareTranscript = onShareTranscript,
                onOpenStoryStats = onOpenStoryStats,
                onOpenBookmarks = onOpenBookmarks,
                autoTtsEnabled = autoTtsEnabled,
                onToggleAutoTts = onToggleAutoTts
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(841f / 138f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (refGlassBitmaps.quickCards.width > 1) {
                        drawImage(
                            image = refGlassBitmaps.quickCards,
                            dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
                            dstSize = IntSize(
                                kotlin.math.round(size.width).toInt().coerceAtLeast(1),
                                kotlin.math.round(size.height).toInt().coerceAtLeast(1)
                            ),
                            filterQuality = FilterQuality.High
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.36f)
                            .clickable(
                                enabled = !isSending,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onSendMessage("Mevcut sahneyi kesmeden ortam atmosferini, karakterin iç sesini ve duygusal gerilimi edebi olarak derinleştir.")
                            }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.33f)
                            .clickable(
                                enabled = !isSending,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onSendMessage("Şu anki roman kurgusunda yapabileceğim 3 farklı dramatik hamle (1. Karanlık/Riskli hamle, 2. Tutkulu/Duygusal hamle, 3. Stratejik hamle) öner ve sahneyi sürdür.")
                            }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.31f)
                            .clickable(
                                enabled = canEditLastMessage,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onEditLastMessage
                            )
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(841f / 138f)
            ) {
                val barW = maxWidth
                val barH = maxHeight

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (refGlassBitmaps.inputBarNoText.width > 1) {
                        drawImage(
                            image = refGlassBitmaps.inputBarNoText,
                            dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
                            dstSize = IntSize(
                                kotlin.math.round(size.width).toInt().coerceAtLeast(1),
                                kotlin.math.round(size.height).toInt().coerceAtLeast(1)
                            ),
                            filterQuality = FilterQuality.High
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(x = barW * (22f / 841f), y = barH * (16f / 138f))
                        .size(width = barW * (124f / 841f), height = barH * (106f / 138f))
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTogglePlusToolkit
                        )
                )

                Box(
                    modifier = Modifier
                        .offset(x = barW * (178f / 841f), y = barH * (26f / 138f))
                        .size(width = barW * (402f / 841f), height = barH * (86f / 138f)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "Bir mesaj yazın...",
                            color = Color(0xFFE9D5FF).copy(alpha = 0.85f),
                            fontSize = 15.sp,
                            fontFamily = LiterarySerifFontFamily
                        )
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = onInputTextChange,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 15.5.sp,
                            fontFamily = LiterarySerifFontFamily
                        ),
                        cursorBrush = SolidColor(Color(0xFFE9D5FF)),
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_input_field")
                    )
                }

                Box(
                    modifier = Modifier
                        .offset(x = barW * (586f / 841f), y = barH * (24f / 138f))
                        .size(width = barW * (88f / 841f), height = barH * (90f / 138f))
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenBackgroundPicker
                        )
                )

                val canSendNow = !isSending && inputText.trim().isNotEmpty()
                val canStopNow = isSending && onStopGeneration != null
                val sendInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .offset(x = barW * (695f / 841f), y = barH * (12f / 138f))
                        .size(width = barW * (130f / 841f), height = barH * (114f / 138f))
                        .cosmicTapSpring(
                            interactionSource = sendInteraction,
                            enabled = canSendNow || canStopNow,
                            pressedScale = 0.88f,
                            cornerRadius = 40.dp
                        )
                        .cosmicPopOnTrigger(triggerKey = sendBurstTrigger, peakScale = 1.15f)
                        .clip(CircleShape)
                        .clickable(
                            enabled = canSendNow || canStopNow,
                            interactionSource = sendInteraction,
                            indication = null
                        ) {
                            if (canStopNow) {
                                onStopGeneration?.invoke()
                            } else {
                                val text = inputText.trim()
                                if (text.isNotBlank() && !isSending) {
                                    onTriggerSendBurst()
                                    onInputTextChange("")
                                    onSendMessage(text)
                                }
                            }
                        }
                        .testTag(if (canStopNow) "stop_generation_button" else "send_message_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (canStopNow) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .border(1.1.dp, Color(0xFFE9D5FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .border(1.2.dp, Color.White, RoundedCornerShape(3.dp))
                            )
                        }
                    } else {
                        CosmicSparkleBurst(
                            triggerKey = sendBurstTrigger,
                            accentColor = Color(0xFFE9D5FF),
                            secondaryColor = Color(0xFFA855F7),
                            particleCount = 10,
                            maxRadius = 30.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive (+) RP & Sahne Araçları Drawer — includes direct ⭐ Yıldızlananlar shortcut
 * alongside scene continuation, deepening, fate dice, time skip, atmosphere & inner thoughts.
 */
@Composable
internal fun ChatPlusToolkitDrawer(
    visible: Boolean,
    isSending: Boolean,
    inputText: String,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    onDismiss: () -> Unit,
    onOpenStarredSheet: () -> Unit,
    onContinueScene: () -> Unit,
    onSendPrompt: (String) -> Unit,
    onUpdateInputText: (String) -> Unit,
    onCopyTranscript: (() -> Unit)? = null,
    onShareTranscript: (() -> Unit)? = null,
    onOpenStoryStats: (() -> Unit)? = null,
    onOpenBookmarks: (() -> Unit)? = null,
    autoTtsEnabled: Boolean = false,
    onToggleAutoTts: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + expandVertically(tween(180)),
        exit = fadeOut(tween(140)) + shrinkVertically(tween(140))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tools = listOf(
                Triple("⭐ Yıldızlananlar", "plus_toolkit_starred_button") {
                    onDismiss()
                    onOpenStarredSheet()
                },
                Triple("⏩ Devam Et", "plus_toolkit_continue") {
                    onDismiss()
                    if (!isSending) onContinueScene()
                },
                Triple("✨ Sahneyi Derinleştir", "plus_toolkit_deepen") {
                    onDismiss()
                    if (!isSending) {
                        onSendPrompt("Mevcut sahneyi kesmeden ortam atmosferini, karakterin iç sesini ve duygusal gerilimi edebi olarak derinleştir.")
                    }
                },
                Triple("💡 Ne Yapmalıyım?", "plus_toolkit_suggest") {
                    onDismiss()
                    if (!isSending) {
                        onSendPrompt("Şu anki roman kurgusunda yapabileceğim 3 farklı dramatik hamle (1. Karanlık/Riskli hamle, 2. Tutkulu/Duygusal hamle, 3. Stratejik hamle) öner ve sahneyi sürdür.")
                    }
                },
                Triple("✨ *Eylem Ekle*", "plus_toolkit_action") {
                    val t = inputText.trim()
                    val next = if (t.isBlank()) "*...*" else if (t.startsWith("*") && t.endsWith("*")) t.trim('*') else "*$t*"
                    onUpdateInputText(next)
                    onDismiss()
                },
                Triple("🎲 Kader Zarı", "plus_toolkit_dice") {
                    onDismiss()
                    if (!isSending) onSendPrompt("*Beklenmedik bir gelişme yaşanır ve sahnenin dengesi aniden değişir.*")
                },
                Triple("⏳ Zaman Atla", "plus_toolkit_timeskip") {
                    onDismiss()
                    if (!isSending) onSendPrompt("*Aradan biraz zaman geçer; sahne bir sonraki kritik ana ilerler.*")
                },
                Triple("🌌 Ortamı Betimle", "plus_toolkit_describe") {
                    onDismiss()
                    if (!isSending) onSendPrompt("Lütfen şu an bulunduğumuz ortamı, ışığı, sesleri ve atmosferi tüm duyularla hissettirecek şekilde betimle.")
                },
                Triple("🧠 İç Ses", "plus_toolkit_inner") {
                    onDismiss()
                    if (!isSending) onSendPrompt("Karakterin şu an dışarıya belli etmediği gerçek iç düşüncelerini ve duygularını hissettirerek devam et.")
                },
                Triple("📋 Hikayeyi Kopyala", "plus_toolkit_copy_transcript") {
                    onDismiss()
                    onCopyTranscript?.invoke()
                },
                Triple("📤 Hikayeyi Paylaş", "plus_toolkit_share_transcript") {
                    onDismiss()
                    onShareTranscript?.invoke()
                },
                Triple("📊 Hikaye İstatistikleri", "plus_toolkit_story_stats") {
                    onDismiss()
                    onOpenStoryStats?.invoke()
                },
                Triple("🔖 Yer İmleri", "plus_toolkit_bookmarks") {
                    onDismiss()
                    onOpenBookmarks?.invoke()
                },
                Triple(if (autoTtsEnabled) "🔊 Oto-Ses: Açık" else "🔇 Oto-Ses: Kapalı", "plus_toolkit_auto_tts") {
                    onDismiss()
                    onToggleAutoTts?.invoke()
                }
            )
            tools.forEach { (label, tag, action) ->
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .morCamOuterGlow(cornerRadius = 18.dp, shadowAlpha = 0.28f, rimGlowAlpha = 0.26f, bottomFlareAlpha = 0.56f)
                        .clip(RoundedCornerShape(50))
                        .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.65f)
                        .morCamGlassSurface(
                            baseSmokeColor = Color(0x2816072C),
                            topRightFrostAlpha = 0.20f,
                            topLeftFrostAlpha = 0.12f,
                            topStarFrostAlpha = 0f,
                            bottomEdgeRefractionAlpha = 0.18f
                        )
                        .border(
                            BorderStroke(0.9.dp, Color(0xCCE9D5FF)),
                            RoundedCornerShape(50)
                        )
                        .morCamTopAndBottomSpecular(cornerRadius = 18.dp, topSheenAlpha = 0.24f, bottomFlareAlpha = 0.60f)
                        .clickable { action() }
                        .testTag(tag)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontFamily = LiterarySerifFontFamily
                    )
                }
            }
        }
    }
}

/**
 * Floating Mor Cam "En Aşağı İn" button shown when the user scrolls >= 3 messages above the bottom.
 */
@Composable
internal fun ChatScrollToBottomFab(
    visible: Boolean,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(CosmicMotionSpecs.snappyOrb()) { it / 2 } +
            scaleIn(CosmicMotionSpecs.snappyOrb(), initialScale = 0.82f) +
            fadeIn(tween(180)),
        exit = slideOutVertically(tween(180)) { it / 2 } +
            scaleOut(tween(160), targetScale = 0.85f) +
            fadeOut(tween(140))
    ) {
        val scrollBtnInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .cosmicFloatingLevitation(amplitudeDp = 2.5.dp, durationMs = 2200)
                .cosmicTapSpring(
                    interactionSource = scrollBtnInteraction,
                    pressedScale = 0.92f,
                    cornerRadius = 22.dp
                )
                .morCamOuterGlow(cornerRadius = 22.dp, shadowAlpha = 0.32f, rimGlowAlpha = 0.28f, bottomFlareAlpha = 0.56f)
                .clip(RoundedCornerShape(50))
                .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.72f)
                .morCamGlassSurface(
                    baseSmokeColor = Color(0x28180832),
                    topRightFrostAlpha = 0.24f,
                    topLeftFrostAlpha = 0.16f,
                    topStarFrostAlpha = 0f,
                    bottomEdgeRefractionAlpha = 0.20f
                )
                .border(
                    BorderStroke(
                        0.9.dp,
                        Brush.linearGradient(
                            listOf(Color(0xE0F3E8FF), Color(0x66A855F7), Color(0xD8E9D5FF))
                        )
                    ),
                    RoundedCornerShape(50)
                )
                .morCamTopAndBottomSpecular(cornerRadius = 22.dp, topSheenAlpha = 0.26f, bottomFlareAlpha = 0.82f)
                .clickable(
                    interactionSource = scrollBtnInteraction,
                    indication = null,
                    onClick = onClick
                )
                .testTag("chat_scroll_to_bottom_button")
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "En aşağı in",
                    tint = Color(0xFFFBF8FF),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "En Aşağı İn",
                    color = Color(0xFFFBF8FF),
                    fontSize = 11.8.sp,
                    fontFamily = LiterarySerifFontFamily
                )
            }
        }
    }
}
