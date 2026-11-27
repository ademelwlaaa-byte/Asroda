package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.LocalCosmicMotionPolicy
import com.example.ui.animation.rememberCosmicPhaseState
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val OrbSpecColors = listOf(
    Color.White.copy(alpha = 0.70f),
    Color.White.copy(alpha = 0.20f),
    Color.Transparent
)

private val OrbTrLocale: Locale = Locale.forLanguageTag("tr-TR")

internal fun normalizeOrbHue(hue: Float): Float {
    if (!hue.isFinite()) return 200f
    return ((hue % 360f) + 360f) % 360f
}

object MoodColors {
    fun getMoodHue(mood: String): Float {
        val norm = mood.trim().lowercase(OrbTrLocale)
        return when (norm) {
            "joy", "neşeli", "neseli" -> 38f
            "warm", "sıcak", "sicak" -> 340f
            "sad", "hüzünlü", "huzunlu" -> 250f
            "tense", "gergin" -> 8f
            "curious", "meraklı", "merakli" -> 275f
            else -> 200f
        }
    }

    fun getMoodLabel(mood: String): String {
        val norm = mood.trim().lowercase(OrbTrLocale)
        return when (norm) {
            "joy", "neşeli", "neseli" -> "neşeli"
            "warm", "sıcak", "sicak" -> "sıcak"
            "sad", "hüzünlü", "huzunlu" -> "hüzünlü"
            "tense", "gergin" -> "gergin"
            "curious", "meraklı", "merakli" -> "meraklı"
            else -> "sakin"
        }
    }
}

@Composable
fun OrbView(
    hue: Float,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val motionConfig = LocalCosmicMotionConfig.current
    val motionPolicy = LocalCosmicMotionPolicy.current
    val animatedHueState = animateFloatAsState(
        targetValue = normalizeOrbHue(hue),
        animationSpec = tween(
            durationMillis = if (motionPolicy.shouldUseQuickTransitions) 140 else 420,
            easing = FastOutSlowInEasing
        ),
        label = "orbHueMorph"
    )

    // Draw-phase-only State<Float> animation drivers -> 0 FPS root recomposition!
    val allowAmbient = motionPolicy.shouldAnimateContinuously && motionConfig.enableAmbientLoops
    val rotationAngleState = rememberCosmicPhaseState(
        durationMs = 8000,
        initialValue = 0f,
        targetValue = 360f,
        staticValue = 24f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "orbRotation",
        enabled = allowAmbient
    )
    val pulseAlphaState = rememberCosmicPhaseState(
        durationMs = 1800,
        initialValue = 0.55f,
        targetValue = 0.95f,
        staticValue = 0.78f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "orbPulse",
        enabled = allowAmbient
    )
    val innerPulseState = rememberCosmicPhaseState(
        durationMs = 2400,
        initialValue = 0.70f,
        targetValue = 1.0f,
        staticValue = 0.85f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "innerPulse",
        enabled = allowAmbient
    )

    Canvas(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    ) {
        val r = this.size.minDimension / 2f
        if (r <= 0.5f) return@Canvas
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val center = Offset(cx, cy)

        val safeHue = normalizeOrbHue(animatedHueState.value)
        val rotationAngle = rotationAngleState.value
        val pulseAlpha = pulseAlphaState.value.coerceIn(0f, 1f)
        val innerPulse = innerPulseState.value.coerceIn(0f, 1f)

        val coreColor = Color.hsl(safeHue, 0.90f, 0.78f)
        val midColor = Color.hsl(safeHue, 0.80f, 0.55f)
        val outerColor = Color.hsl(safeHue, 0.70f, 0.35f)
        val darkEdge = Color.hsl(safeHue, 0.60f, 0.12f)
        val deepEdge = Color.hsl(safeHue, 0.50f, 0.06f)
        val complementHue = (safeHue + 180f) % 360f
        val nebulaColor = Color.hsl(complementHue, 0.75f, 0.60f, alpha = 0.35f)
        val analogHue = (safeHue + 60f) % 360f
        val swirl1 = Color.hsl(analogHue, 0.85f, 0.65f, alpha = 0.30f)
        val ringColor = Color.hsl(safeHue, 0.95f, 0.80f, alpha = 0.60f)
        val ambientGlow = Color.hsl(safeHue, 0.80f, 0.55f, alpha = 0.20f)

        // Layer 1: Ambient outer glow (soft, large)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ambientGlow, Color.Transparent),
                center = center,
                radius = r * 1.15f
            ),
            radius = r * 1.15f,
            center = center
        )

        // Layer 2: Deep dark base
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(darkEdge, deepEdge),
                center = center,
                radius = r
            ),
            radius = r,
            center = center
        )

        // Layer 3: Main sphere radial gradient (off-center highlight)
        val highlightOffset = Offset(cx - r * 0.30f, cy - r * 0.30f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(coreColor, midColor, outerColor, darkEdge),
                center = highlightOffset,
                radius = r * 1.40f
            ),
            radius = r,
            center = center
        )

        // Layer 4: Rotating nebula swirl (complement hue)
        rotate(rotationAngle, pivot = center) {
            drawNebulaSwirl(center, r, nebulaColor, swirl1, pulseAlpha)
        }

        // Layer 5: Inner glow core (breathing)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    coreColor.copy(alpha = innerPulse * 0.55f),
                    Color.Transparent
                ),
                center = highlightOffset,
                radius = r * 0.65f
            ),
            radius = r * 0.60f,
            center = highlightOffset
        )

        // Layer 6: Energy ring (pulsing, sharp)
        drawCircle(
            color = ringColor.copy(alpha = pulseAlpha * 0.70f),
            radius = r * 0.82f,
            center = center,
            style = Stroke(width = 1.8.dp.toPx())
        )

        // Layer 7: Outer thin rim
        drawCircle(
            color = coreColor.copy(alpha = 0.40f),
            radius = r * 0.97f,
            center = center,
            style = Stroke(width = 0.8.dp.toPx())
        )

        // Layer 8: Specular glass highlight (large soft)
        val specOffset = Offset(cx - r * 0.28f, cy - r * 0.30f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = OrbSpecColors,
                center = specOffset,
                radius = r * 0.38f
            ),
            radius = r * 0.34f,
            center = specOffset
        )

        // Layer 9: Tiny sharp specular dot
        val dotOffset = Offset(cx - r * 0.34f, cy - r * 0.36f)
        drawCircle(
            color = Color.White.copy(alpha = 0.92f),
            radius = r * 0.07f,
            center = dotOffset
        )
    }
}

private fun DrawScope.drawNebulaSwirl(
    center: Offset,
    radius: Float,
    color1: Color,
    color2: Color,
    alpha: Float
) {
    val steps = 8
    for (i in 0 until steps) {
        val angle = (i.toFloat() / steps) * 360f
        val rad = Math.toRadians(angle.toDouble())
        val dist = radius * 0.45f
        val x = center.x + cos(rad).toFloat() * dist
        val y = center.y + sin(rad).toFloat() * dist
        val blobRadius = radius * (0.22f + (i % 3) * 0.06f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (i % 2 == 0) color1.copy(alpha = alpha * 0.45f) else color2.copy(alpha = alpha * 0.35f),
                    Color.Transparent
                ),
                center = Offset(x, y),
                radius = blobRadius
            ),
            radius = blobRadius,
            center = Offset(x, y)
        )
    }
}
