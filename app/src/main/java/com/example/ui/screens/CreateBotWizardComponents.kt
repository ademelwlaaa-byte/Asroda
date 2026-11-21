package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.CosmicSparkleBurst
import com.example.ui.animation.cosmicGlassSheen
import com.example.ui.animation.cosmicPopOnTrigger
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily

// ── Outer Violet Cosmic Bloom Modifier (Consistent with Mor Cam Glass Language) ──
internal fun Modifier.wizardMorCamGlow(
    cornerRadius: Dp,
    glowColor: Color = Color(0xFFA855F7),
    alpha: Float = 0.45f,
    blurDp: Dp = 12.dp
): Modifier = this.drawBehind {
    val cr = cornerRadius.toPx()
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.argb(92, 8, 3, 20)
            maskFilter = android.graphics.BlurMaskFilter(10.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        native.drawRoundRect(
            2.dp.toPx(),
            3.dp.toPx(),
            size.width - 2.dp.toPx(),
            size.height + 3.dp.toPx(),
            cr,
            cr,
            shadowPaint
        )
        if (alpha > 0.01f) {
            val haloPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3.dp.toPx()
                color = android.graphics.Color.argb(
                    (alpha.coerceIn(0f, 1f) * 255).toInt(),
                    (glowColor.red * 255).toInt(),
                    (glowColor.green * 255).toInt(),
                    (glowColor.blue * 255).toInt()
                )
                maskFilter = android.graphics.BlurMaskFilter(blurDp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            native.drawRoundRect(0f, 0f, size.width, size.height, cr, cr, haloPaint)
        }
    }
}

internal fun DrawScope.drawWizardStarFlare(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    coreColor: Color = Color(0xFFFAF5FF),
    edgeColor: Color = Color(0xFFD8B4FE),
    glowColor: Color = Color(0xFFC084FC),
    glowAlpha: Float = 0.6f
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = glowAlpha.coerceIn(0f, 1f)),
                glowColor.copy(alpha = (glowAlpha * 0.25f).coerceIn(0f, 1f)),
                Color.Transparent
            ),
            center = center,
            radius = outerRadius * 1.45f
        ),
        radius = outerRadius * 1.45f,
        center = center
    )
    val path = Path().apply {
        moveTo(center.x, center.y - outerRadius)
        quadraticTo(center.x + innerRadius * 0.45f, center.y - innerRadius * 0.45f, center.x + outerRadius, center.y)
        quadraticTo(center.x + innerRadius * 0.45f, center.y + innerRadius * 0.45f, center.x, center.y + outerRadius)
        quadraticTo(center.x - innerRadius * 0.45f, center.y + innerRadius * 0.45f, center.x - outerRadius, center.y)
        quadraticTo(center.x - innerRadius * 0.45f, center.y - innerRadius * 0.45f, center.x, center.y - outerRadius)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(coreColor, edgeColor),
            center = center,
            radius = outerRadius
        )
    )
}

// ── Top Mor Cam Frosted Glass Header + 6-Segment Luminous Progress Bar ───────
@Composable
internal fun WizardMorCamHeader(
    stepIdx: Int,
    totalSteps: Int,
    stepLabel: String,
    frostedBackdrop: ImageBitmap,
    scrollSync: Int,
    glowAlpha: Float,
    onBackClick: () -> Unit
) {
    val backInteraction = remember { MutableInteractionSource() }
    val backPressed by backInteraction.collectIsPressedAsState()
    val backScale by animateFloatAsState(
        targetValue = if (backPressed) 0.90f else 1f,
        animationSpec = CosmicMotionSpecs.snappyOrb(),
        label = "wizardBackScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Circular Frosted Glass Back Button (matching GlobalSettingsModal)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .graphicsLayer { scaleX = backScale; scaleY = backScale }
                    .wizardMorCamGlow(cornerRadius = 23.dp, alpha = 0.34f * glowAlpha, blurDp = 9.dp)
                    .clip(CircleShape)
                    .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.76f, scrollSync = scrollSync)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x5245237A), Color(0x3D1E0D3B), Color(0x4C2D1454))
                        )
                    )
                    .border(
                        width = 1.25.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xE8F3E8FF), Color(0x99C084FC), Color(0x887C3AED))
                        ),
                        shape = CircleShape
                    )
                    .clickable(interactionSource = backInteraction, indication = null, onClick = onBackClick)
                    .testTag("wizard_back_icon_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color(0xFFF5EEFF),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center Frosted Glass Title Capsule with Bottom Starlight Flare
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .wizardMorCamGlow(cornerRadius = 24.dp, alpha = 0.42f * glowAlpha, blurDp = 11.dp)
                        .clip(RoundedCornerShape(50))
                        .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.78f, scrollSync = scrollSync)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x5245237A), Color(0x3D1F0E3D), Color(0x4D32165E))
                            )
                        )
                        .drawBehind {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                                    startY = 0f,
                                    endY = size.height * 0.5f
                                ),
                                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                            )
                        }
                        .border(
                            width = 1.3.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xECF3E8FF), Color(0xB8C084FC), Color(0x888B5CF6), Color(0xE0E9D5FF))
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Bot Oluştur",
                            fontFamily = DisplaySerifFontFamily,
                            color = Color(0xFFFAF6FF),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "✦  $stepLabel  ✦",
                            fontFamily = InterFontFamily,
                            color = Color(0xFFD8B4FE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.4.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Signature centered 4-point starlight flare on bottom rim
                Canvas(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 5.dp)
                        .width(84.dp)
                        .height(12.dp)
                ) {
                    val cx = size.width * 0.5f
                    val cy = size.height * 0.5f
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xBBEAD8FF), Color.White, Color(0xBBEAD8FF), Color.Transparent)
                        ),
                        start = Offset(0f, cy),
                        end = Offset(size.width, cy),
                        strokeWidth = 1.1.dp.toPx()
                    )
                    drawWizardStarFlare(
                        center = Offset(cx, cy),
                        outerRadius = 5.5.dp.toPx(),
                        innerRadius = 1.5.dp.toPx(),
                        glowAlpha = 0.7f * glowAlpha
                    )
                }
            }

            // Right Circular Step Counter Medallion ("1/6")
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .cosmicPopOnTrigger(triggerKey = stepIdx, peakScale = 1.14f)
                    .wizardMorCamGlow(cornerRadius = 23.dp, alpha = 0.36f * glowAlpha, blurDp = 9.dp)
                    .clip(CircleShape)
                    .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.76f, scrollSync = scrollSync)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x52482480), Color(0x3D1E0D3B), Color(0x4C31165C))
                        )
                    )
                    .border(
                        width = 1.25.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xE8F3E8FF), Color(0xA6C084FC), Color(0x887C3AED))
                        ),
                        shape = CircleShape
                    )
                    .testTag("wizard_step_counter"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${stepIdx + 1}/$totalSteps",
                    fontFamily = LiterarySerifFontFamily,
                    color = Color(0xFFF5EEFF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6 Equal Full-Width Frosted Crystal Progress Segments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { idx ->
                val isActive = idx == stepIdx
                val isDone = idx < stepIdx
                val segScaleY by animateFloatAsState(
                    targetValue = if (isActive) 1.25f else 1f,
                    animationSpec = CosmicMotionSpecs.snappyOrb(),
                    label = "wizardSegScale_$idx"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.5.dp)
                        .graphicsLayer { scaleY = segScaleY }
                        .then(
                            if (isActive) {
                                Modifier.wizardMorCamGlow(
                                    cornerRadius = 3.dp,
                                    glowColor = Color(0xFFD8B4FE),
                                    alpha = 0.75f * glowAlpha,
                                    blurDp = 6.dp
                                )
                            } else Modifier
                        )
                        .clip(RoundedCornerShape(50))
                        .background(
                            when {
                                isActive -> Brush.horizontalGradient(
                                    listOf(Color(0xFFFAF5FF), Color(0xFFD8B4FE), Color(0xFFA855F7))
                                )
                                isDone -> Brush.horizontalGradient(
                                    listOf(Color(0xCC9333EA), Color(0xCCA855F7))
                                )
                                else -> Brush.horizontalGradient(
                                    listOf(Color(0x4428154D), Color(0x4428154D))
                                )
                            }
                        )
                        .border(
                            width = 0.6.dp,
                            color = when {
                                isActive -> Color(0xE8FFFFFF)
                                isDone -> Color(0x99D8B4FE)
                                else -> Color(0x44C4B5FD)
                            },
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        }
    }
}

// ── Bottom Mor Cam Frosted Glass Action Dock ("Geri" + "İleri / Tamamla") ────
@Composable
internal fun WizardMorCamBottomDock(
    stepIdx: Int,
    totalSteps: Int,
    canNext: Boolean,
    frostedBackdrop: ImageBitmap,
    scrollSync: Int,
    glowAlpha: Float,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val backInteraction = remember { MutableInteractionSource() }
    val backPressed by backInteraction.collectIsPressedAsState()
    val backScale by animateFloatAsState(
        targetValue = if (backPressed) 0.94f else 1f,
        animationSpec = CosmicMotionSpecs.snappyOrb(),
        label = "dockBackScale"
    )

    val nextInteraction = remember { MutableInteractionSource() }
    val nextPressed by nextInteraction.collectIsPressedAsState()
    val nextScale by animateFloatAsState(
        targetValue = if (nextPressed && canNext) 0.94f else 1f,
        animationSpec = CosmicMotionSpecs.snappyOrb(),
        label = "dockNextScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Frosted Glass Pill: "← Geri"
            Box(
                modifier = Modifier
                    .weight(0.92f)
                    .height(52.dp)
                    .graphicsLayer { scaleX = backScale; scaleY = backScale }
                    .wizardMorCamGlow(cornerRadius = 26.dp, alpha = 0.28f * glowAlpha, blurDp = 9.dp)
                    .clip(RoundedCornerShape(50))
                    .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.76f, scrollSync = scrollSync)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x52341A5E), Color(0x42180A30), Color(0x4C261148))
                        )
                    )
                    .drawBehind {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                                startY = 0f,
                                endY = size.height * 0.45f
                            ),
                            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                        )
                    }
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xCCF3E8FF), Color(0x88A855F7), Color(0xAA7C3AED))
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable(interactionSource = backInteraction, indication = null, onClick = onBack)
                    .testTag("wizard_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color(0xFFE9D5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (stepIdx == 0) "Vazgeç" else "Geri",
                        fontFamily = LiterarySerifFontFamily,
                        color = Color(0xFFF5EEFF),
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right Luminous Crystal Mor Cam CTA Pill: "İleri →" / "Tamamla ✓"
            Box(
                modifier = Modifier
                    .weight(1.28f)
                    .height(52.dp)
                    .graphicsLayer { scaleX = nextScale; scaleY = nextScale }
                    .then(
                        if (canNext) {
                            Modifier.wizardMorCamGlow(
                                cornerRadius = 26.dp,
                                glowColor = Color(0xFFC084FC),
                                alpha = 0.62f * glowAlpha,
                                blurDp = 14.dp
                            )
                        } else Modifier
                    )
                    .clip(RoundedCornerShape(50))
                    .cosmicGlassSheen(cornerRadius = 26.dp, enabled = canNext, periodMs = 3600)
                    .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.68f, scrollSync = scrollSync)
                    .background(
                        if (canNext) {
                            Brush.verticalGradient(
                                listOf(Color(0x788B5CF6), Color(0x5E4C1D95), Color(0x706D28D9))
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0x40271447), Color(0x38190C30))
                            )
                        }
                    )
                    .drawBehind {
                        if (canNext) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                                    startY = 0f,
                                    endY = size.height * 0.48f
                                ),
                                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                            )
                        }
                    }
                    .border(
                        width = if (canNext) 1.4.dp else 1.dp,
                        brush = if (canNext) {
                            Brush.linearGradient(
                                listOf(Color(0xF5F5EEFF), Color(0xCCE9D5FF), Color(0x99A855F7), Color(0xEDF3E8FF))
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(Color(0x558B5CF6), Color(0x384C1D95))
                            )
                        },
                        shape = RoundedCornerShape(50)
                    )
                    .clickable(
                        enabled = canNext,
                        interactionSource = nextInteraction,
                        indication = null,
                        onClick = onNext
                    )
                    .testTag("wizard_next_button")
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                if (canNext) {
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(16.dp)
                    ) {
                        drawWizardStarFlare(
                            center = Offset(size.width * 0.5f, size.height * 0.5f),
                            outerRadius = size.minDimension * 0.44f,
                            innerRadius = size.minDimension * 0.14f,
                            glowAlpha = 0.45f * glowAlpha
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (stepIdx == totalSteps - 1) "Botu Oluştur" else "İleri",
                        fontFamily = DisplaySerifFontFamily,
                        color = if (canNext) Color(0xFFFAF6FF) else Color(0x70C4B5FD),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (stepIdx == totalSteps - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (canNext) Color(0xFFFAF6FF) else Color(0x70C4B5FD),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ── Editorial Literary Serif Step Header ─────────────────────────────────────
@Composable
internal fun WizardStepHeader(
    title: String,
    subtitle: String,
    glowAlpha: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(modifier = Modifier.size(22.dp)) {
                drawWizardStarFlare(
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    outerRadius = size.minDimension * 0.48f,
                    innerRadius = size.minDimension * 0.13f,
                    coreColor = Color.White,
                    edgeColor = Color(0xFFD8B4FE),
                    glowColor = Color(0xFFA855F7),
                    glowAlpha = 0.65f * glowAlpha
                )
            }
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(1.2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xCCD8B4FE), Color.Transparent)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = TextStyle(
                fontFamily = DisplaySerifFontFamily,
                color = Color(0xFFFAF6FF),
                fontSize = 27.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 33.sp,
                letterSpacing = (-0.2).sp,
                shadow = Shadow(
                    color = Color(0x80C084FC),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 12f
                )
            ),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            fontFamily = InterFontFamily,
            color = Color(0xFFD5C6F2),
            fontSize = 13.8.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 19.5.sp,
            textAlign = TextAlign.Start
        )
    }
}

// ── Luminous Mor Cam Frosted Glass Choice Card ───────────────────────────────
@Composable
internal fun WizardMorCamChoiceCard(
    title: String,
    subtitle1: String,
    subtitle2: String,
    iconVector: ImageVector,
    isSelected: Boolean,
    frostedBackdrop: ImageBitmap,
    scrollSync: Int,
    glowAlpha: Float,
    testTag: String = "",
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(24.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    var selectBurst by remember { mutableStateOf(0) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.968f else 1f,
        animationSpec = CosmicMotionSpecs.snappyOrb(),
        label = "choiceCardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .wizardMorCamGlow(
                cornerRadius = 24.dp,
                glowColor = if (isSelected) Color(0xFFC084FC) else Color(0xFF7C3AED),
                alpha = if (isSelected) 0.56f * glowAlpha else 0.22f,
                blurDp = if (isSelected) 16.dp else 9.dp
            )
            .clip(cardShape)
            .templatesBackdropBlur(
                blurredBitmap = frostedBackdrop,
                alpha = if (isSelected) 0.80f else 0.72f,
                scrollSync = scrollSync
            )
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        listOf(Color(0x66482180), Color(0x5225104A), Color(0x60341566))
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color(0x48221042), Color(0x3D140829), Color(0x461C0D38))
                    )
                }
            )
            .drawBehind {
                // Top crystal glass sheen
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isSelected) 0.22f else 0.11f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.42f
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
            .clickable(interactionSource = interaction, indication = null) {
                if (!isSelected) selectBurst++
                onClick()
            }
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            // Left Frosted Glass Squircle Icon Medallion
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(
                        if (isSelected) {
                            Brush.radialGradient(
                                listOf(Color(0x998B5CF6), Color(0x663B1470))
                            )
                        } else {
                            Brush.radialGradient(
                                listOf(Color(0x554C1D95), Color(0x441B0B36))
                            )
                        }
                    )
                    .border(
                        width = if (isSelected) 1.35.dp else 1.dp,
                        brush = if (isSelected) {
                            Brush.linearGradient(listOf(Color(0xFFF3E8FF), Color(0xFFC084FC), Color(0xFF7C3AED)))
                        } else {
                            Brush.linearGradient(listOf(Color(0x88C4B5FD), Color(0x446D28D9)))
                        },
                        shape = RoundedCornerShape(17.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFFFAF6FF) else Color(0xFFD8B4FE),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = DisplaySerifFontFamily,
                    color = Color(0xFFFAF6FF),
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.1).sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = subtitle1,
                    fontFamily = InterFontFamily,
                    color = if (isSelected) Color(0xFFE9DCFF) else Color(0xFFC6B8E5),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                if (subtitle2.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtitle2,
                        fontFamily = InterFontFamily,
                        color = if (isSelected) Color(0xFFD8C4FA) else Color(0xFFA99AC9),
                        fontSize = 12.5.sp,
                        lineHeight = 17.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .cosmicPopOnTrigger(triggerKey = selectBurst, peakScale = 1.24f),
                    contentAlignment = Alignment.Center
                ) {
                    CosmicSparkleBurst(
                        triggerKey = selectBurst,
                        modifier = Modifier.size(38.dp),
                        accentColor = Color(0xFFF3E8FF),
                        secondaryColor = Color(0xFFC084FC),
                        particleCount = 10
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFFC084FC), Color(0xFF7C3AED)))
                            )
                            .border(1.dp, Color(0xF0F5EEFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seçili",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0x99D8B4FE),
                    modifier = Modifier.size(21.dp)
                )
            }
        }

        // Double-Rimmed Crystal Glass Border & Rim Glint
        Canvas(modifier = Modifier.matchParentSize()) {
            val cr = 24.dp.toPx()
            if (isSelected) {
                val inset = 3.5.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0x4CE9D5FF), Color(0x1AC084FC), Color(0x45D8B4FE))
                    ),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2f, size.height - inset * 2f),
                    cornerRadius = CornerRadius(20.5.dp.toPx(), 20.5.dp.toPx()),
                    style = Stroke(width = 0.75.dp.toPx())
                )
            }
            drawRoundRect(
                brush = if (isSelected) {
                    Brush.linearGradient(
                        listOf(Color(0xF0F5EEFF), Color(0xC8C084FC), Color(0x998B5CF6), Color(0xE0E9D5FF))
                    )
                } else {
                    Brush.linearGradient(
                        listOf(Color(0x88E9D5FF), Color(0x508B5CF6), Color(0x70C084FC))
                    )
                },
                topLeft = Offset(0.75.dp.toPx(), 0.75.dp.toPx()),
                size = Size(size.width - 1.5.dp.toPx(), size.height - 1.5.dp.toPx()),
                cornerRadius = CornerRadius(cr, cr),
                style = Stroke(width = if (isSelected) 1.45.dp.toPx() else 1.05.dp.toPx())
            )
            if (isSelected) {
                drawWizardStarFlare(
                    center = Offset(size.width * 0.90f, size.height * 0.02f),
                    outerRadius = 5.5.dp.toPx(),
                    innerRadius = 1.4.dp.toPx(),
                    glowAlpha = 0.65f * glowAlpha
                )
            }
        }
    }
}

// ── Translucent Mor Cam Frosted Section Panel ────────────────────────────────
@Composable
internal fun WizardMorCamSectionCard(
    title: String? = null,
    subtitle: String? = null,
    frostedBackdrop: ImageBitmap,
    scrollSync: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wizardMorCamGlow(cornerRadius = 22.dp, alpha = 0.28f, blurDp = 11.dp)
            .clip(shape)
            .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.76f, scrollSync = scrollSync)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0x52241046), Color(0x4615092B), Color(0x4E1E0D3B))
                )
            )
            .border(
                width = 1.15.dp,
                brush = Brush.linearGradient(
                    listOf(Color(0xCCF3E8FF), Color(0x77C084FC), Color(0x607C3AED), Color(0xBBE9D5FF))
                ),
                shape = shape
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    fontFamily = DisplaySerifFontFamily,
                    color = Color(0xFFFAF6FF),
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Medium
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        fontFamily = InterFontFamily,
                        color = Color(0xFFC6B6E8),
                        fontSize = 12.sp,
                        lineHeight = 16.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            content()
        }
    }
}

// ── Refined Serif Field Label ────────────────────────────────────────────────
@Composable
internal fun WizardFieldLabel(
    title: String,
    required: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            fontFamily = LiterarySerifFontFamily,
            color = Color(0xFFF5EEFF),
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp
        )
        if (required) {
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "✦",
                fontFamily = InterFontFamily,
                color = Color(0xFFD8B4FE),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Recessed Frosted Crystal Glass Text Field ────────────────────────────────
@Composable
internal fun WizardMorCamTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    frostedBackdrop: ImageBitmap,
    scrollSync: Int,
    modifier: Modifier = Modifier,
    minHeight: Dp = 54.dp
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .height(minHeight)
            .then(
                if (isFocused) {
                    Modifier.wizardMorCamGlow(
                        cornerRadius = 18.dp,
                        glowColor = Color(0xFFC084FC),
                        alpha = 0.52f,
                        blurDp = 11.dp
                    )
                } else Modifier
            )
            .clip(shape)
            .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.70f, scrollSync = scrollSync)
            .background(
                if (isFocused) {
                    Brush.verticalGradient(listOf(Color(0x622B1254), Color(0x52180933)))
                } else {
                    Brush.verticalGradient(listOf(Color(0x4C1C0C38), Color(0x44120726)))
                }
            )
            .border(
                width = if (isFocused) 1.45.dp else 1.05.dp,
                brush = if (isFocused) {
                    Brush.linearGradient(listOf(Color(0xF5F5EEFF), Color(0xCCC084FC), Color(0xAA8B5CF6)))
                } else {
                    Brush.linearGradient(listOf(Color(0x88D8B4FE), Color(0x4D7C3AED)))
                },
                shape = shape
            )
            .padding(horizontal = 15.dp, vertical = 14.dp),
        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = TextStyle(
                fontFamily = InterFontFamily,
                color = Color(0xFFFAF6FF),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.5.sp
            ),
            cursorBrush = SolidColor(Color(0xFFE9D5FF)),
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = InterFontFamily,
                            color = Color(0x99C4B5FD),
                            fontSize = 13.8.sp,
                            lineHeight = 19.5.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

// ── Translucent Mor Cam Glass Pill Chip ──────────────────────────────────────
@Composable
internal fun WizardMorCamPillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) {
                    Brush.horizontalGradient(listOf(Color(0xAA8B5CF6), Color(0x99A855F7)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0x4D27134D), Color(0x40190B33)))
                }
            )
            .border(
                width = if (selected) 1.3.dp else 0.95.dp,
                brush = if (selected) {
                    Brush.linearGradient(listOf(Color(0xF5F5EEFF), Color(0xCCC084FC)))
                } else {
                    Brush.linearGradient(listOf(Color(0x77C4B5FD), Color(0x447C3AED)))
                },
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            color = if (selected) Color.White else Color(0xFFDCD0F8),
            fontSize = 12.5.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
