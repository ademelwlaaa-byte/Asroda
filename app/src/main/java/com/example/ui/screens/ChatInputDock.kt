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
import com.example.ui.theme.LiterarySerifFontFamily

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
    onUpdateInputText: (String) -> Unit
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
