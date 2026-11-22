@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.LocalCosmicMotionPolicy
import com.example.ui.animation.cosmicDialogEnter
import com.example.ui.animation.cosmicGlassSheen
import com.example.ui.animation.cosmicPopupEnter
import com.example.ui.animation.cosmicTapSpring
import com.example.ui.animation.rememberCosmicPhase
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import kotlin.math.roundToInt

/**
 * Unified Mor Cam (Cosmic Frosted Glass) Lavender-Silver Design System.
 * Shared across Discover, Global Settings sub-modals, Bot Settings sub-modals/dialogs,
 * Neural Vault, Emotion Studio, and Chat overlays/panels.
 */
object MorCamTokens {
    val GlassSurfaceTop = Color(0xF01C0C3B)
    val GlassSurfaceMid = Color(0xF5120729)
    val GlassSurfaceBottom = Color(0xF70B041B)

    val CardSurfaceTop = Color(0xCC210F44)
    val CardSurfaceBottom = Color(0xD613082B)

    val FieldSurfaceTop = Color(0xB8180A33)
    val FieldSurfaceBottom = Color(0xCC0E0520)

    val SilverLavenderBright = Color(0xFFF5EEFF)
    val LavenderLight = Color(0xFFE9D5FF)
    val LavenderSoft = Color(0xFFD8B4FE)
    val LavenderMuted = Color(0xFFBBA4E3)
    val LavenderSubtle = Color(0xFF9E87C8)

    val AmethystPrimary = Color(0xFF9333EA)
    val AmethystDeep = Color(0xFF6D28D9)
    val AmethystNight = Color(0xFF4C1D95)
    val AmethystGlow = Color(0xFFA855F7)

    // Cohesive plum-rose destructive tokens (replaces harsh red blocks)
    val DestructiveTop = Color(0xCC6B163A)
    val DestructiveMid = Color(0xD850102C)
    val DestructiveBottom = Color(0xE03A0A20)
    val DestructiveRimLight = Color(0xFFFBCFE8)
    val DestructiveRimAccent = Color(0xFFF472B6)
    val DestructiveText = Color(0xFFFFE4F1)

    val RimGradient = Brush.linearGradient(
        listOf(
            Color(0xEBF5EEFF),
            Color(0x9EC084FC),
            Color(0x6E7C3AED),
            Color(0xCCE9D5FF)
        )
    )

    val SubtleRimGradient = Brush.linearGradient(
        listOf(
            Color(0x88E9D5FF),
            Color(0x4D9333EA),
            Color(0x78C084FC)
        )
    )

    val PrimaryButtonFill = Brush.horizontalGradient(
        listOf(Color(0xE67C3AED), Color(0xEC9333EA), Color(0xE66D28D9))
    )

    val SecondaryButtonFill = Brush.horizontalGradient(
        listOf(Color(0xA6230F47), Color(0xB8160830), Color(0xA6230F47))
    )

    val DestructiveButtonFill = Brush.horizontalGradient(
        listOf(DestructiveTop, DestructiveMid, DestructiveBottom)
    )

    val DestructiveRimGradient = Brush.linearGradient(
        listOf(DestructiveRimLight, DestructiveRimAccent, Color(0xAA9D174D))
    )

    val TitleShadow = Shadow(
        color = Color(0xB89333EA),
        offset = Offset(0f, 1.5f),
        blurRadius = 10f
    )
}

enum class MorCamButtonVariant {
    Primary,
    Secondary,
    Destructive
}

private val reusableMorCamStarPath = Path()

fun Modifier.morCamCosmicGlow(
    cornerRadius: Dp,
    glowColor: Color = MorCamTokens.AmethystGlow,
    alpha: Float = 0.36f,
    blurDp: Dp = 12.dp
): Modifier = this.drawWithCache {
    val cr = cornerRadius.toPx()
    val dx = 2.dp.toPx()
    val dy = 3.dp.toPx()
    val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
        color = android.graphics.Color.argb(105, 7, 2, 18)
        maskFilter = android.graphics.BlurMaskFilter(11.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    val haloPaint = if (alpha > 0.01f) {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2.6.dp.toPx()
            color = android.graphics.Color.argb(
                (alpha.coerceIn(0f, 1f) * 255).toInt(),
                (glowColor.red * 255).toInt(),
                (glowColor.green * 255).toInt(),
                (glowColor.blue * 255).toInt()
            )
            maskFilter = android.graphics.BlurMaskFilter(blurDp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
    } else null

    onDrawBehind {
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            native.drawRoundRect(
                dx,
                dy,
                size.width - dx,
                size.height + dy,
                cr,
                cr,
                shadowPaint
            )
            if (haloPaint != null) {
                native.drawRoundRect(0f, 0f, size.width, size.height, cr, cr, haloPaint)
            }
        }
    }
}

fun DrawScope.drawMorCamFourPointStar(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    coreColor: Color = Color(0xFFFFFFFF),
    edgeColor: Color = MorCamTokens.LavenderLight,
    glowColor: Color = MorCamTokens.AmethystGlow,
    glowAlpha: Float = 0.55f
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = glowAlpha.coerceIn(0f, 1f)),
                glowColor.copy(alpha = (glowAlpha * 0.24f).coerceIn(0f, 1f)),
                Color.Transparent
            ),
            center = center,
            radius = outerRadius * 1.55f
        ),
        radius = outerRadius * 1.55f,
        center = center
    )
    reusableMorCamStarPath.reset()
    reusableMorCamStarPath.moveTo(center.x, center.y - outerRadius)
    reusableMorCamStarPath.quadraticTo(center.x + innerRadius * 0.42f, center.y - innerRadius * 0.42f, center.x + outerRadius, center.y)
    reusableMorCamStarPath.quadraticTo(center.x + innerRadius * 0.42f, center.y + innerRadius * 0.42f, center.x, center.y + outerRadius)
    reusableMorCamStarPath.quadraticTo(center.x - innerRadius * 0.42f, center.y + innerRadius * 0.42f, center.x - outerRadius, center.y)
    reusableMorCamStarPath.quadraticTo(center.x - innerRadius * 0.42f, center.y - innerRadius * 0.42f, center.x, center.y - outerRadius)
    reusableMorCamStarPath.close()
    drawPath(
        path = reusableMorCamStarPath,
        brush = Brush.radialGradient(
            colors = listOf(coreColor, edgeColor),
            center = center,
            radius = outerRadius
        )
    )
}

@Composable
fun MorCamDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0x66E9D5FF),
                        Color(0x99C084FC),
                        Color(0x66E9D5FF),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun MorCamDialogShell(
    onDismissRequest: () -> Unit,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    maxWidthFraction: Float = 0.93f,
    showTopStarCrest: Boolean = true,
    showCloseButton: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(maxWidthFraction)
                .padding(vertical = 14.dp)
                .cosmicDialogEnter(key = title),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (showTopStarCrest) 8.dp else 0.dp)
                    .morCamCosmicGlow(cornerRadius = 26.dp, alpha = 0.38f, blurDp = 14.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MorCamTokens.GlassSurfaceTop,
                                MorCamTokens.GlassSurfaceMid,
                                MorCamTokens.GlassSurfaceBottom
                            )
                        )
                    )
                    .cosmicGlassSheen(cornerRadius = 26.dp, periodMs = 6600, maxAlpha = 0.11f)
                    .border(BorderStroke(1.2.dp, MorCamTokens.RimGradient), RoundedCornerShape(26.dp))
            ) {
                // Subtle internal silver-lavender top light refraction
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x2EE9D5FF),
                                Color(0x109333EA),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, 0f),
                            radius = size.width * 0.85f
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (icon != null) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0x998B5CF6), Color(0x663B1470))
                                        )
                                    )
                                    .border(1.dp, MorCamTokens.RimGradient, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MorCamTokens.SilverLavenderBright,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontFamily = DisplaySerifFontFamily,
                                color = MorCamTokens.SilverLavenderBright,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(shadow = MorCamTokens.TitleShadow)
                            )
                            if (!subtitle.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subtitle,
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.LavenderMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        if (showCloseButton) {
                            MorCamCircleIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Kapat",
                                onClick = onDismissRequest,
                                size = 38.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    MorCamDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    content()
                }
            }

            if (showTopStarCrest) {
                MorCamAnimatedStarCrest(
                    size = 22.dp,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
internal fun MorCamAnimatedStarCrest(
    size: Dp,
    modifier: Modifier = Modifier,
    baseGlowAlpha: Float = 0.55f
) {
    val motionConfig = LocalCosmicMotionConfig.current
    val motionPolicy = LocalCosmicMotionPolicy.current
    val animateCrest = motionPolicy.shouldAnimateContinuously && motionConfig.enableAmbientLoops
    val pulse = rememberCosmicPhase(
        durationMs = 2200,
        initialValue = 0.86f,
        targetValue = 1.12f,
        staticValue = 1f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "starPulse",
        enabled = animateCrest
    )
    val rotDeg = rememberCosmicPhase(
        durationMs = 3600,
        initialValue = -7f,
        targetValue = 7f,
        staticValue = 0f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "starTilt",
        enabled = animateCrest
    )
    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
                rotationZ = rotDeg
            }
    ) {
        drawMorCamFourPointStar(
            center = Offset(this.size.width / 2f, this.size.height / 2f),
            outerRadius = this.size.minDimension * 0.48f,
            innerRadius = this.size.minDimension * 0.16f,
            glowAlpha = (baseGlowAlpha * pulse).coerceIn(0f, 0.9f)
        )
    }
}

@Composable
fun MorCamBottomSheetShell(
    onDismissRequest: () -> Unit,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailingHeaderContent: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xF7110626),
        contentColor = MorCamTokens.SilverLavenderBright,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 0.dp,
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x66C084FC), Color(0xFFF3E8FF), Color(0x66C084FC))
                            )
                        )
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0x998B5CF6), Color(0x663B1470))
                                )
                            )
                            .border(1.1.dp, MorCamTokens.RimGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MorCamTokens.SilverLavenderBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = DisplaySerifFontFamily,
                        color = MorCamTokens.SilverLavenderBright,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(shadow = MorCamTokens.TitleShadow)
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderMuted,
                            fontSize = 11.5.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (trailingHeaderContent != null) {
                    trailingHeaderContent()
                }

                MorCamCircleIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Kapat",
                    onClick = onDismissRequest,
                    size = 38.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            MorCamDivider()
            Spacer(modifier = Modifier.height(14.dp))

            content()

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun MorCamGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    highlighted: Boolean = false,
    paddingHorizontal: Dp = 14.dp,
    paddingVertical: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    if (highlighted) {
                        listOf(Color(0xD62A1356), Color(0xDE170A35))
                    } else {
                        listOf(MorCamTokens.CardSurfaceTop, MorCamTokens.CardSurfaceBottom)
                    }
                )
            )
            .border(
                BorderStroke(
                    width = if (highlighted) 1.2.dp else 0.9.dp,
                    brush = if (highlighted) MorCamTokens.RimGradient else MorCamTokens.SubtleRimGradient
                ),
                shape = shape
            )
            .padding(horizontal = paddingHorizontal, vertical = paddingVertical),
        content = content
    )
}

@Composable
fun MorCamCapsuleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: MorCamButtonVariant = MorCamButtonVariant.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(50)
    val fillBrush = when (variant) {
        MorCamButtonVariant.Primary -> MorCamTokens.PrimaryButtonFill
        MorCamButtonVariant.Secondary -> MorCamTokens.SecondaryButtonFill
        MorCamButtonVariant.Destructive -> MorCamTokens.DestructiveButtonFill
    }
    val borderBrush = when (variant) {
        MorCamButtonVariant.Primary -> MorCamTokens.RimGradient
        MorCamButtonVariant.Secondary -> MorCamTokens.SubtleRimGradient
        MorCamButtonVariant.Destructive -> MorCamTokens.DestructiveRimGradient
    }
    val textColor = when (variant) {
        MorCamButtonVariant.Primary -> Color.White
        MorCamButtonVariant.Secondary -> MorCamTokens.LavenderLight
        MorCamButtonVariant.Destructive -> MorCamTokens.DestructiveText
    }
    val glowColor = when (variant) {
        MorCamButtonVariant.Primary -> MorCamTokens.AmethystGlow
        MorCamButtonVariant.Secondary -> MorCamTokens.AmethystDeep
        MorCamButtonVariant.Destructive -> MorCamTokens.DestructiveRimAccent
    }

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.48f }
            .cosmicTapSpring(
                interactionSource = interactionSource,
                enabled = enabled,
                pressedScale = 0.95f,
                rippleColor = glowColor,
                cornerRadius = 24.dp
            )
            .morCamCosmicGlow(
                cornerRadius = 24.dp,
                glowColor = glowColor,
                alpha = if (variant == MorCamButtonVariant.Secondary) 0.18f else 0.36f,
                blurDp = 9.dp
            )
            .clip(shape)
            .background(fillBrush)
            .cosmicGlassSheen(
                enabled = enabled && variant == MorCamButtonVariant.Primary,
                cornerRadius = 24.dp,
                periodMs = 4600,
                maxAlpha = 0.18f
            )
            .border(BorderStroke(1.1.dp, borderBrush), shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(
                text = text,
                fontFamily = LiterarySerifFontFamily,
                color = textColor,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MorCamCircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    isDestructive: Boolean = false,
    isActive: Boolean = false,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bgBrush = when {
        isDestructive -> Brush.radialGradient(listOf(Color(0x996B163A), Color(0xB83A0A20)))
        isActive -> Brush.radialGradient(listOf(Color(0xCC7C3AED), Color(0xB84C1D95)))
        else -> Brush.radialGradient(listOf(Color(0x8A2B1456), Color(0xB814082C)))
    }
    val borderBrush = when {
        isDestructive -> MorCamTokens.DestructiveRimGradient
        isActive -> MorCamTokens.RimGradient
        else -> MorCamTokens.SubtleRimGradient
    }
    val tint = if (isDestructive) MorCamTokens.DestructiveRimLight else MorCamTokens.SilverLavenderBright

    Box(
        modifier = modifier
            .size(size.coerceAtLeast(36.dp))
            .cosmicTapSpring(
                interactionSource = interactionSource,
                pressedScale = 0.89f,
                rippleColor = if (isDestructive) MorCamTokens.DestructiveRimAccent else MorCamTokens.LavenderSoft,
                cornerRadius = size / 2
            )
            .clip(CircleShape)
            .background(bgBrush)
            .border(BorderStroke(1.dp, borderBrush), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size((size.value * 0.45f).dp.coerceIn(16.dp, 22.dp))
        )
    }
}

@Composable
fun MorCamGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    minHeight: Dp = 48.dp,
    testTag: String? = null
) {
    var focused by remember { mutableStateOf(false) }
    val motionConfig = LocalCosmicMotionConfig.current
    val shape = RoundedCornerShape(if (singleLine) 24.dp else 18.dp)
    val borderWidth by animateDpAsState(
        targetValue = if (focused) 1.45.dp else 0.95.dp,
        animationSpec = if (motionConfig.enableMicroSprings) CosmicMotionSpecs.snappyOrb() else tween(120),
        label = "fieldBorderWidth"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (focused && motionConfig.enableMicroSprings) 1.14f else 1f,
        animationSpec = CosmicMotionSpecs.playfulSpark(),
        label = "fieldIconBounce"
    )
    val focusGlowAlpha by animateFloatAsState(
        targetValue = if (focused) 0.42f else 0f,
        animationSpec = tween(220),
        label = "fieldFocusGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .then(
                if (focusGlowAlpha > 0.01f) {
                    Modifier.morCamCosmicGlow(
                        cornerRadius = if (singleLine) 24.dp else 18.dp,
                        glowColor = MorCamTokens.AmethystGlow,
                        alpha = focusGlowAlpha,
                        blurDp = 10.dp
                    )
                } else Modifier
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(MorCamTokens.FieldSurfaceTop, MorCamTokens.FieldSurfaceBottom)
                )
            )
            .border(
                BorderStroke(
                    width = borderWidth,
                    brush = if (focused) MorCamTokens.RimGradient else MorCamTokens.SubtleRimGradient
                ),
                shape = shape
            )
            .padding(horizontal = 14.dp, vertical = if (singleLine) 10.dp else 12.dp),
        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (focused) MorCamTokens.LavenderLight else MorCamTokens.LavenderMuted,
                    modifier = Modifier
                        .padding(top = if (singleLine) 0.dp else 2.dp)
                        .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
                        .size(18.dp)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.LavenderSubtle,
                        fontSize = 13.5.sp,
                        maxLines = if (singleLine) 1 else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    textStyle = TextStyle(
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.SilverLavenderBright,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    ),
                    cursorBrush = SolidColor(MorCamTokens.LavenderSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused }
                        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
                )
            }

            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
fun MorCamOptionRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    selected: Boolean = false,
    destructive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val bgBrush = when {
        destructive -> MorCamTokens.DestructiveButtonFill
        selected -> Brush.horizontalGradient(listOf(Color(0xB85B21B6), Color(0xA67C3AED), Color(0xB84C1D95)))
        else -> Brush.verticalGradient(listOf(Color(0x9E210F44), Color(0xB013082B)))
    }
    val borderBrush = when {
        destructive -> MorCamTokens.DestructiveRimGradient
        selected -> MorCamTokens.RimGradient
        else -> MorCamTokens.SubtleRimGradient
    }
    val contentTint = if (destructive) MorCamTokens.DestructiveText else MorCamTokens.SilverLavenderBright

    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .cosmicTapSpring(
                interactionSource = interactionSource,
                pressedScale = 0.97f,
                rippleColor = if (destructive) MorCamTokens.DestructiveRimAccent else MorCamTokens.LavenderSoft,
                cornerRadius = 16.dp
            )
            .clip(shape)
            .background(bgBrush)
            .border(BorderStroke(if (selected || destructive) 1.15.dp else 0.9.dp, borderBrush), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (destructive) Color(0x45F472B6) else Color(0x40A855F7)
                    )
                    .border(
                        0.8.dp,
                        if (destructive) MorCamTokens.DestructiveRimLight.copy(alpha = 0.7f) else MorCamTokens.LavenderSoft.copy(alpha = 0.6f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentTint,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = LiterarySerifFontFamily,
                color = contentTint,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = InterFontFamily,
                    color = if (destructive) Color(0xFFFBCFE8).copy(alpha = 0.8f) else MorCamTokens.LavenderMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun MorCamSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xCC7C3AED) else Color(0x66211140),
        animationSpec = tween(200),
        label = "morCamSwitchTrack"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "morCamSwitchThumb"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0x94200F42), Color(0xAA120728))
                )
            )
            .border(
                BorderStroke(
                    0.9.dp,
                    if (checked) MorCamTokens.RimGradient else MorCamTokens.SubtleRimGradient
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = LiterarySerifFontFamily,
                    color = MorCamTokens.SilverLavenderBright,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                if (!badge.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x45A855F7))
                            .border(0.7.dp, MorCamTokens.LavenderSoft.copy(alpha = 0.6f), RoundedCornerShape(50))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderLight,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = InterFontFamily,
                    color = MorCamTokens.LavenderMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 15.5.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .width(44.dp)
                .height(24.dp)
                .clip(CircleShape)
                .background(trackColor)
                .border(
                    1.dp,
                    if (checked) MorCamTokens.LavenderLight else Color(0x66C084FC),
                    CircleShape
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            if (checked) listOf(Color.White, MorCamTokens.LavenderLight)
                            else listOf(Color(0xFFE9D5FF), Color(0xFFBBA4E3))
                        )
                    )
            )
        }
    }
}

@Composable
fun MorCamChipPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val motionConfig = LocalCosmicMotionConfig.current
    val interactionSource = remember { MutableInteractionSource() }
    val selectedScale by animateFloatAsState(
        targetValue = if (selected && motionConfig.enableMicroSprings) 1.03f else 1f,
        animationSpec = CosmicMotionSpecs.playfulSpark(),
        label = "chipPopScale"
    )
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .heightIn(min = 36.dp)
            .graphicsLayer { scaleX = selectedScale; scaleY = selectedScale }
            .cosmicTapSpring(
                interactionSource = interactionSource,
                pressedScale = 0.94f,
                cornerRadius = 18.dp
            )
            .clip(shape)
            .background(
                if (selected) MorCamTokens.PrimaryButtonFill
                else Brush.horizontalGradient(listOf(Color(0x8A1E0E3D), Color(0x9E130829)))
            )
            .border(
                BorderStroke(
                    if (selected) 1.1.dp else 0.85.dp,
                    if (selected) MorCamTokens.RimGradient else MorCamTokens.SubtleRimGradient
                ),
                shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else MorCamTokens.LavenderSoft,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = label,
            fontFamily = LiterarySerifFontFamily,
            color = if (selected) Color.White else MorCamTokens.LavenderLight.copy(alpha = 0.86f),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Floating Mor Cam Frosted-Glass Popup Menu.
 * Replaces flat opaque Material DropdownMenus with translucent deep-amethyst glass,
 * top silver-lavender light refraction, metallic rim border, cosmic glow, and top star crest.
 */
@Composable
fun MorCamPopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    headerLabel: String? = null,
    minWidth: Dp = 216.dp,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val menuShape = RoundedCornerShape(22.dp)
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = null,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = minWidth, max = 270.dp)
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .cosmicPopupEnter(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .morCamCosmicGlow(
                        cornerRadius = 22.dp,
                        glowColor = MorCamTokens.AmethystGlow,
                        alpha = 0.42f,
                        blurDp = 12.dp
                    )
                    .clip(menuShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xEB220E47),
                                Color(0xF0150830),
                                Color(0xF50D0520)
                            )
                        )
                    )
                    .border(BorderStroke(1.15.dp, MorCamTokens.RimGradient), menuShape)
            ) {
                // Soft internal top-center lavender refraction sheen
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x34E9D5FF),
                                Color(0x149333EA),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, 0f),
                            radius = size.width * 0.9f
                        )
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.00f to Color(0x1EE9D5FF),
                            0.25f to Color.Transparent,
                            0.82f to Color.Transparent,
                            1.00f to Color(0x1F7C3AED)
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!headerLabel.isNullOrBlank()) {
                        Text(
                            text = headerLabel,
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderMuted.copy(alpha = 0.90f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        MorCamDivider(modifier = Modifier.padding(bottom = 2.dp))
                    }
                    content()
                }
            }

            // Animated 4-point silver-lavender starlight crest on top rim
            MorCamAnimatedStarCrest(
                size = 16.dp,
                baseGlowAlpha = 0.45f,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

/**
 * Interactive Mor Cam Pill Row for [MorCamPopupMenu].
 * Features a glowing circular crystal icon well, press spring scale, active check badge,
 * and cohesive plum-rose destructive styling.
 */
@Composable
fun MorCamPopupMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    selected: Boolean = false,
    destructive: Boolean = false,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "popupItemScale"
    )
    val shape = RoundedCornerShape(14.dp)
    val bgBrush = when {
        destructive -> Brush.horizontalGradient(
            listOf(Color(0x8A5B1233), Color(0x9E3E0B23), Color(0x8A50102C))
        )
        selected -> Brush.horizontalGradient(
            listOf(Color(0xC07C3AED), Color(0xB89333EA), Color(0xB85B21B6))
        )
        else -> Brush.horizontalGradient(
            listOf(Color(0x6627124E), Color(0x78170933))
        )
    }
    val borderBrush = when {
        destructive -> MorCamTokens.DestructiveRimGradient
        selected -> MorCamTokens.RimGradient
        else -> MorCamTokens.SubtleRimGradient
    }
    val iconWellBg = when {
        destructive -> Brush.radialGradient(listOf(Color(0x88F472B6), Color(0x6650102C)))
        selected -> Brush.radialGradient(listOf(Color(0xCCFFFFFF), Color(0x88A855F7)))
        else -> Brush.radialGradient(listOf(Color(0x75A855F7), Color(0x552E1065)))
    }
    val iconTint = when {
        destructive -> MorCamTokens.DestructiveRimLight
        selected -> Color(0xFF2B0B5A)
        else -> MorCamTokens.SilverLavenderBright
    }
    val titleColor = when {
        destructive -> MorCamTokens.DestructiveText
        selected -> Color.White
        else -> MorCamTokens.SilverLavenderBright
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(bgBrush)
            .border(
                BorderStroke(if (selected || destructive) 1.05.dp else 0.8.dp, borderBrush),
                shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(iconWellBg)
                .border(
                    0.85.dp,
                    if (destructive) MorCamTokens.DestructiveRimLight.copy(alpha = 0.72f)
                    else MorCamTokens.LavenderLight.copy(alpha = 0.65f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = LiterarySerifFontFamily,
                color = titleColor,
                fontSize = 13.5.sp,
                fontWeight = if (selected || destructive) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontFamily = InterFontFamily,
                    color = if (destructive) Color(0xFFFBCFE8).copy(alpha = 0.80f)
                    else if (selected) Color(0xFFF3E8FF).copy(alpha = 0.90f)
                    else MorCamTokens.LavenderMuted,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0x45FFFFFF))
                    .border(0.8.dp, Color(0xE0F5EEFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seçili",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * Shared Mor Cam Translucent Glass-Tube Slider with Glowing Crystal Orb Thumb.
 * Replaces plain Material 3 Slider bars across Emotion Studio, Character Emotion Editor,
 * and Create Bot Wizard with a unified lavender-silver frosted aesthetic.
 */
@Composable
fun MorCamGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    steps: Int = 0,
    accentColor: Color = MorCamTokens.AmethystGlow
) {
    val span = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
    val clamped = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val fraction = ((clamped - valueRange.start) / span).coerceIn(0f, 1f)

    fun mapFractionToValue(rawFraction: Float): Float {
        val f = rawFraction.coerceIn(0f, 1f)
        val snappedFraction = if (steps > 0) {
            val segments = steps + 1
            (f * segments).roundToInt().toFloat() / segments.toFloat()
        } else {
            f
        }
        return valueRange.start + snappedFraction * span
    }

    Box(
        modifier = modifier
            .height(34.dp)
            .pointerInput(valueRange, steps) {
                detectTapGestures { offset ->
                    val w = size.width.toFloat().coerceAtLeast(1f)
                    onValueChange(mapFractionToValue(offset.x / w))
                }
            }
            .pointerInput(valueRange, steps) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val w = size.width.toFloat().coerceAtLeast(1f)
                    onValueChange(mapFractionToValue(change.position.x / w))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackH = 6.dp.toPx()
            val cy = size.height / 2f
            val cr = trackH / 2f
            val horizontalPad = 8.dp.toPx()
            val trackW = (size.width - horizontalPad * 2f).coerceAtLeast(1f)
            val activeW = (trackW * fraction).coerceIn(0f, trackW)

            // Frosted translucent glass tube background
            drawRoundRect(
                color = Color(0x52180A32),
                topLeft = Offset(horizontalPad, cy - cr),
                size = Size(trackW, trackH),
                cornerRadius = CornerRadius(cr, cr)
            )
            drawRoundRect(
                color = Color(0x70C084FC),
                topLeft = Offset(horizontalPad, cy - cr),
                size = Size(trackW, trackH),
                cornerRadius = CornerRadius(cr, cr),
                style = Stroke(width = 0.85.dp.toPx())
            )

            // Luminous active glass fill
            if (activeW > 1f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xAA6D28D9), accentColor, Color(0xFFF3E8FF)),
                        startX = horizontalPad,
                        endX = horizontalPad + activeW.coerceAtLeast(cr * 2f)
                    ),
                    topLeft = Offset(horizontalPad, cy - cr),
                    size = Size(activeW.coerceAtLeast(cr * 2f), trackH),
                    cornerRadius = CornerRadius(cr, cr)
                )
            }

            // Glowing crystal orb thumb
            val thumbX = horizontalPad + trackW * fraction
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF3E8FF),
                        accentColor.copy(alpha = 0.85f),
                        Color.Transparent
                    ),
                    center = Offset(thumbX, cy),
                    radius = 12.dp.toPx()
                ),
                radius = 12.dp.toPx(),
                center = Offset(thumbX, cy)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFE9D5FF)),
                    center = Offset(thumbX - 1.dp.toPx(), cy - 1.dp.toPx()),
                    radius = 6.5.dp.toPx()
                ),
                radius = 6.5.dp.toPx(),
                center = Offset(thumbX, cy)
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.85f),
                radius = 6.5.dp.toPx(),
                center = Offset(thumbX, cy),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
