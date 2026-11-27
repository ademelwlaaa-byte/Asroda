package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.LocalCosmicMotionPolicy
import com.example.ui.animation.MotionIntensityMode
import com.example.ui.animation.drawMiniStar
import com.example.ui.animation.rememberCosmicPhaseState
import kotlin.math.PI
import kotlin.math.sin

private val DotColorTop = Color(0xFFF3E8FF)
private val DotColorMid = Color(0xFFA855F7)
private val DotColorBottom = Color(0xFF7C3AED)

internal data class TypingDotSample(
    val liftFactor: Float,
    val scale: Float,
    val glowAlpha: Float
)

internal fun computeTypingDotSample(
    wavePhase: Float,
    dotIndex: Int,
    reducedMotion: Boolean
): TypingDotSample {
    val safePhase = if (wavePhase.isFinite()) wavePhase else 0f
    val safeIdx = dotIndex.coerceIn(0, 10)
    val wave = sin(safePhase - safeIdx * 0.88f)
    val liftFactor = if (reducedMotion) 0f else -wave.coerceAtLeast(0f)
    val scale = if (reducedMotion) {
        0.82f + 0.18f * (0.5f + 0.5f * wave)
    } else {
        0.76f + 0.28f * (0.5f + 0.5f * wave)
    }
    val glowAlpha = (0.28f + 0.62f * (0.5f + 0.5f * wave)).coerceIn(0f, 1f)
    return TypingDotSample(liftFactor = liftFactor, scale = scale, glowAlpha = glowAlpha)
}

/**
 * Cosmic Typing Wave:
 * Synchronized harmonic-wave glowing amethyst orbs + rising micro starlight motes.
 * Reads animation phases strictly inside Canvas (0 FPS root recomposition) and respects
 * [LocalCosmicMotionPolicy] + [LocalCosmicMotionConfig].
 */
@Composable
fun TypingDots(
    dotSize: Dp = 8.dp,
    dotColor: Color = DotColorMid,
    modifier: Modifier = Modifier
) {
    val config = LocalCosmicMotionConfig.current
    val policy = LocalCosmicMotionPolicy.current
    val allowMotion = policy.shouldAnimateContinuously && config.enableAmbientLoops
    val isReduced = config.mode == MotionIntensityMode.REDUCED

    val wavePhaseState = rememberCosmicPhaseState(
        durationMs = if (isReduced) 1500 else 920,
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        staticValue = 0.85f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "typingWavePhase",
        enabled = allowMotion
    )
    val moteRiseState = rememberCosmicPhaseState(
        durationMs = 2200,
        initialValue = 0f,
        targetValue = 1f,
        staticValue = 0.4f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "typingMoteRise",
        enabled = allowMotion && config.enableParticles
    )

    val orbGradientColors = remember(dotColor) {
        if (dotColor == DotColorMid) {
            listOf(DotColorTop, DotColorMid, DotColorBottom)
        } else {
            listOf(DotColorTop, dotColor, dotColor.copy(alpha = 0.82f))
        }
    }

    val canvasW = dotSize * 4.8f
    val canvasH = dotSize * 2.5f

    Box(
        modifier = modifier.padding(vertical = 3.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .width(canvasW)
                .height(canvasH)
        ) {
            val baseR = (dotSize / 2f).toPx()
            val spacing = baseR * 3.1f
            val startX = size.width / 2f - spacing
            val baseY = size.height * 0.62f
            val liftPxMax = 5.5.dp.toPx()

            val wavePhase = wavePhaseState.value
            val moteRise = moteRiseState.value

            // Subtle rising lavender starlight sparks above the typing wave
            if (config.enableParticles && allowMotion) {
                val sparkCount = if (config.mode == MotionIntensityMode.FULL) 4 else 2
                for (s in 0 until sparkCount) {
                    val p = (moteRise + s * (1f / sparkCount)) % 1f
                    val sx = startX + spacing * (0.25f + (s % 3) * 0.75f) + sin(wavePhase + s * 1.7f) * 3.dp.toPx()
                    val sy = baseY - p * size.height * 0.68f
                    val sparkAlpha = sin(p * PI.toFloat()).coerceIn(0f, 1f) * 0.72f
                    val sr = (2.1f + (s % 2) * 0.8f).dp.toPx() * (1f - p * 0.35f)
                    drawMiniStar(
                        center = Offset(sx, sy),
                        outerR = sr,
                        color = Color(0xFFE9D5FF).copy(alpha = sparkAlpha)
                    )
                }
            }

            // 3 Harmonic Wave Orbs
            for (i in 0 until 3) {
                val sample = computeTypingDotSample(wavePhase, i, isReduced)
                val cx = startX + i * spacing
                val cy = baseY + sample.liftFactor * liftPxMax
                val r = baseR * sample.scale

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            dotColor.copy(alpha = sample.glowAlpha * 0.58f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = r * 2.35f
                    ),
                    radius = r * 2.35f,
                    center = Offset(cx, cy)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = orbGradientColors,
                        center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                        radius = r * 1.4f
                    ),
                    radius = r,
                    center = Offset(cx, cy)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.82f),
                    radius = r * 0.32f,
                    center = Offset(cx - r * 0.26f, cy - r * 0.28f)
                )
            }
        }
    }
}
