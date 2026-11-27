package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.LocalCosmicMotionPolicy
import com.example.ui.animation.rememberCosmicPhaseState
import com.example.ui.theme.AppColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium multi-ring loading spinner with gradient sweep and glow.
 * Uses draw-phase-only State<Float> animation drivers and drawWithCache brushes
 * so parent containers experience 0 FPS recomposition while spinning.
 *
 * @param size    Outer size in dp (default 36)
 * @param color   Primary accent color (default PrimaryViolet)
 * @param small   If true, renders a compact inline version (14dp, no glow)
 */
@Composable
fun EmochiLoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    color: Color = AppColors.PrimaryViolet,
    small: Boolean = false
) {
    val motionConfig = LocalCosmicMotionConfig.current
    val motionPolicy = LocalCosmicMotionPolicy.current
    val allowMotion = motionPolicy.shouldAnimateContinuously && motionConfig.enableAmbientLoops

    // Draw-phase-only State<Float> drivers (0 root recompositions/sec, auto-paused in background)
    val outerAngleState = rememberCosmicPhaseState(
        durationMs = 900,
        initialValue = 0f,
        targetValue = 360f,
        staticValue = 45f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "spinnerOuterAngle",
        enabled = allowMotion
    )
    val innerAngleState = rememberCosmicPhaseState(
        durationMs = 1400,
        initialValue = 360f,
        targetValue = 0f,
        staticValue = 225f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "spinnerInnerAngle",
        enabled = allowMotion && !small
    )
    val glowAlphaState = rememberCosmicPhaseState(
        durationMs = 700,
        initialValue = 0.3f,
        targetValue = 0.7f,
        staticValue = 0.5f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "spinnerGlowAlpha",
        enabled = allowMotion && !small
    )

    if (small) {
        Spacer(
            modifier = modifier
                .size(size)
                .drawWithCache {
                    val strokeW = 2.dp.toPx()
                    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round)
                    val arcSize = Size(
                        (this.size.width - strokeW).coerceAtLeast(1f),
                        (this.size.height - strokeW).coerceAtLeast(1f)
                    )
                    val topLeft = Offset(strokeW / 2f, strokeW / 2f)
                    val trackColor = color.copy(alpha = 0.2f)
                    val sweepBrush = Brush.sweepGradient(
                        listOf(Color.Transparent, color, color.copy(alpha = 0.4f))
                    )
                    onDrawBehind {
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke
                        )
                        rotate(outerAngleState.value) {
                            drawArc(
                                brush = sweepBrush,
                                startAngle = -90f,
                                sweepAngle = 240f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = stroke
                            )
                        }
                    }
                }
        )
    } else {
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Spacer(
                modifier = Modifier
                    .size(size)
                    .drawWithCache {
                        val cx = this.size.width / 2f
                        val cy = this.size.height / 2f
                        val center = Offset(cx, cy)
                        val outerR = this.size.minDimension / 2f
                        val strokeOuter = (size.toPx() * 0.10f).coerceAtLeast(3f)
                        val strokeInner = (size.toPx() * 0.07f).coerceAtLeast(2f)

                        val glowStroke = Stroke(width = 8.dp.toPx())
                        val outerStroke = Stroke(width = strokeOuter, cap = StrokeCap.Round)
                        val innerStroke = Stroke(width = strokeInner, cap = StrokeCap.Round)

                        val outerTopLeft = Offset(strokeOuter / 2f, strokeOuter / 2f)
                        val outerArcSize = Size(
                            (this.size.width - strokeOuter).coerceAtLeast(1f),
                            (this.size.height - strokeOuter).coerceAtLeast(1f)
                        )
                        val outerSweepBrush = Brush.sweepGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.5f to color.copy(alpha = 0.5f),
                                0.75f to color,
                                1.0f to color.copy(alpha = 0.3f)
                            ),
                            center = center
                        )

                        val innerPad = outerR * 0.32f
                        val innerTopLeft = Offset(innerPad + strokeInner / 2f, innerPad + strokeInner / 2f)
                        val innerArcSize = Size(
                            (this.size.width - innerPad * 2f - strokeInner).coerceAtLeast(1f),
                            (this.size.height - innerPad * 2f - strokeInner).coerceAtLeast(1f)
                        )
                        val secondColor = Color(
                            red = (color.red * 0.6f + 0.4f).coerceIn(0f, 1f),
                            green = (color.green * 0.3f + 0.1f).coerceIn(0f, 1f),
                            blue = (color.blue * 0.8f + 0.2f).coerceIn(0f, 1f),
                            alpha = 1f
                        )
                        val innerSweepBrush = Brush.sweepGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.6f to secondColor.copy(alpha = 0.6f),
                                0.85f to secondColor,
                                1.0f to secondColor.copy(alpha = 0.2f)
                            ),
                            center = center
                        )

                        val outerTrackColor = color.copy(alpha = 0.15f)
                        val innerTrackColor = secondColor.copy(alpha = 0.12f)
                        val glowRadius = outerR + 4.dp.toPx()
                        val tipR = outerR - strokeOuter / 2f

                        onDrawBehind {
                            val outerAngle = outerAngleState.value
                            val innerAngle = innerAngleState.value
                            val glowAlpha = glowAlphaState.value.coerceIn(0f, 1f)

                            // Outer glow ring
                            drawCircle(
                                color = color.copy(alpha = glowAlpha * 0.25f),
                                radius = glowRadius,
                                center = center,
                                style = glowStroke
                            )

                            // Outer arc track
                            drawArc(
                                color = outerTrackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = outerTopLeft,
                                size = outerArcSize,
                                style = outerStroke
                            )

                            // Outer sweep arc
                            rotate(outerAngle, pivot = center) {
                                drawArc(
                                    brush = outerSweepBrush,
                                    startAngle = -90f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    topLeft = outerTopLeft,
                                    size = outerArcSize,
                                    style = outerStroke
                                )
                            }

                            // Outer arc tip dot glow
                            val tipRadians = Math.toRadians((outerAngle - 90f).toDouble())
                            val tipCenter = Offset(
                                cx + tipR * cos(tipRadians).toFloat(),
                                cy + tipR * sin(tipRadians).toFloat()
                            )
                            drawCircle(
                                color = color,
                                radius = strokeOuter * 0.7f,
                                center = tipCenter
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.6f),
                                radius = strokeOuter * 0.35f,
                                center = tipCenter
                            )

                            // Inner arc (reverse, shorter)
                            drawArc(
                                color = innerTrackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = innerTopLeft,
                                size = innerArcSize,
                                style = innerStroke
                            )
                            rotate(innerAngle, pivot = center) {
                                drawArc(
                                    brush = innerSweepBrush,
                                    startAngle = -90f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = innerTopLeft,
                                    size = innerArcSize,
                                    style = innerStroke
                                )
                            }

                            // Center dot pulse
                            drawCircle(
                                color = color.copy(alpha = glowAlpha * 0.5f),
                                radius = strokeOuter * 1.2f,
                                center = center
                            )
                            drawCircle(
                                color = color.copy(alpha = 0.9f),
                                radius = strokeOuter * 0.5f,
                                center = center
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.7f),
                                radius = strokeOuter * 0.2f,
                                center = center
                            )
                        }
                    }
            )
        }
    }
}

/**
 * Full-screen loading overlay with centered spinner + optional text.
 */
@Composable
fun EmochiLoadingOverlay(
    visible: Boolean,
    text: String = "Yükleniyor...",
    color: Color = AppColors.PrimaryViolet
) {
    if (!visible) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC080614)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmochiLoadingSpinner(size = 52.dp, color = color)
            Text(
                text = text,
                color = Color(0xFFCDA8FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}
