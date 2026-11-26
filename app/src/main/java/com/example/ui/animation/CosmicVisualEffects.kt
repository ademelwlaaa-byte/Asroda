package com.example.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InterFontFamily
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Dual-orbit starlight ring + breathing amethyst core loader.
 * Replaces flat CircularProgressIndicators across app startup & chat loading.
 */
@Composable
fun CosmicOrbLoader(
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    primaryColor: Color = Color(0xFFC084FC),
    secondaryColor: Color = Color(0xFF7C3AED),
    label: String? = null
) {
    val policy = LocalCosmicMotionPolicy.current
    val animate = policy.shouldAnimateContinuously
    val outerAngleState = rememberCosmicPhaseState(
        durationMs = 2200,
        initialValue = 0f,
        targetValue = 360f,
        staticValue = 35f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "cosmicOrbOuter",
        enabled = animate
    )
    val innerAngleState = rememberCosmicPhaseState(
        durationMs = 1500,
        initialValue = 360f,
        targetValue = 0f,
        staticValue = 210f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "cosmicOrbInner",
        enabled = animate
    )
    val pulseState = rememberCosmicPhaseState(
        durationMs = 1050,
        initialValue = 0.72f,
        targetValue = 1.0f,
        staticValue = 0.88f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "cosmicOrbPulse",
        enabled = animate
    )

    val outerSweepColors = remember(primaryColor, secondaryColor) {
        listOf(Color.Transparent, secondaryColor.copy(alpha = 0.35f), primaryColor, Color(0xFFF5EEFF))
    }
    val innerSweepColors = remember(primaryColor) {
        listOf(Color.Transparent, Color(0xFFE9D5FF).copy(alpha = 0.85f), primaryColor.copy(alpha = 0.65f))
    }
    val coreColors = remember(primaryColor, secondaryColor) {
        listOf(Color.White, primaryColor, secondaryColor)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .drawWithCache {
                    val r = this.size.minDimension / 2f
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    val outerSweepBrush = Brush.sweepGradient(colors = outerSweepColors, center = center)
                    val innerSweepBrush = Brush.sweepGradient(colors = innerSweepColors, center = center)
                    val outerStroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                    val innerStroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                    val outerTopLeft = Offset(center.x - r * 0.84f, center.y - r * 0.84f)
                    val outerSize = Size(r * 1.68f, r * 1.68f)
                    val innerTopLeft = Offset(center.x - r * 0.56f, center.y - r * 0.56f)
                    val innerSize = Size(r * 1.12f, r * 1.12f)
                    val tipRad = Math.toRadians(250.0)
                    val tip = Offset(
                        center.x + cos(tipRad).toFloat() * r * 0.84f,
                        center.y + sin(tipRad).toFloat() * r * 0.84f
                    )
                    val tipRadius = 2.6.dp.toPx()

                    onDrawBehind {
                        val outerAngle = outerAngleState.value
                        val innerAngle = innerAngleState.value
                        val pulse = pulseState.value

                        // Ambient halo
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    secondaryColor.copy(alpha = 0.36f * pulse),
                                    primaryColor.copy(alpha = 0.14f * pulse),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = r
                            ),
                            radius = r,
                            center = center
                        )

                        // Outer orbiting neon arc
                        rotate(outerAngle, pivot = center) {
                            drawArc(
                                brush = outerSweepBrush,
                                startAngle = 0f,
                                sweepAngle = 250f,
                                useCenter = false,
                                topLeft = outerTopLeft,
                                size = outerSize,
                                style = outerStroke
                            )
                            drawCircle(color = Color.White, radius = tipRadius, center = tip)
                        }

                        // Inner counter-orbiting ring
                        rotate(innerAngle, pivot = center) {
                            drawArc(
                                brush = innerSweepBrush,
                                startAngle = 40f,
                                sweepAngle = 190f,
                                useCenter = false,
                                topLeft = innerTopLeft,
                                size = innerSize,
                                style = innerStroke
                            )
                        }

                        // Luminous breathing core
                        val coreR = r * 0.24f * (0.90f + 0.14f * pulse)
                        drawCircle(
                            brush = Brush.radialGradient(colors = coreColors, center = center, radius = coreR),
                            radius = coreR,
                            center = center
                        )
                    }
                }
        )

        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                color = Color(0xFFD8CCF0),
                fontSize = 12.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * One-shot radial starburst particle burst (starring messages, send orb launch, favorite toggle, step completion).
 */
@Composable
fun CosmicSparkleBurst(
    triggerKey: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFFD8B4FE),
    secondaryColor: Color = Color(0xFFFDE68A),
    particleCount: Int = 10,
    maxRadius: Dp = 30.dp
) {
    val config = LocalCosmicMotionConfig.current
    if (!config.enableParticles && !config.enableCelebrations) return

    val progress = remember { Animatable(1f) }
    val particles = remember(particleCount) {
        val rand = Random(314)
        List(particleCount) { idx ->
            val angle = (idx.toFloat() / particleCount) * (2f * PI.toFloat()) + (rand.nextFloat() - 0.5f) * 0.35f
            val distFactor = 0.62f + rand.nextFloat() * 0.45f
            val radiusFactor = 0.7f + rand.nextFloat() * 0.6f
            Triple(angle, distFactor, radiusFactor)
        }
    }

    LaunchedEffect(triggerKey) {
        if (triggerKey <= 0) return@LaunchedEffect
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(config.scaledDuration(520), easing = CosmicMotionSpecs.CosmicEaseOut)
        )
    }

    if (progress.value < 0.99f) {
        Canvas(modifier = modifier) {
            val p = progress.value
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = maxRadius.toPx()
            val alpha = (1f - p).coerceIn(0f, 1f)

            // Expanding subtle shockwave ring
            if (p < 0.65f) {
                val ringP = p / 0.65f
                drawCircle(
                    color = accentColor.copy(alpha = (1f - ringP) * 0.55f),
                    radius = maxR * 0.25f + maxR * 0.72f * ringP,
                    center = Offset(cx, cy),
                    style = Stroke(width = (2.0f * (1f - ringP)).dp.toPx())
                )
            }

            particles.forEachIndexed { idx, (angle, distFactor, rFactor) ->
                val dist = maxR * distFactor * p
                val px = cx + cos(angle) * dist
                val py = cy + sin(angle) * dist
                val dotColor = if (idx % 3 == 0) secondaryColor else accentColor
                val starR = (3.4f * rFactor * (1f - p * 0.65f)).dp.toPx()
                if (idx % 2 == 0) {
                    drawMiniStar(Offset(px, py), starR * 1.25f, dotColor.copy(alpha = alpha))
                } else {
                    drawCircle(color = dotColor.copy(alpha = alpha), radius = starR * 0.65f, center = Offset(px, py))
                }
            }
        }
    }
}

/**
 * Full-overlay celebratory cosmic confetti starburst for Milestone unlocks.
 */
@Composable
fun CosmicConfettiOverlay(
    triggerKey: Any?,
    modifier: Modifier = Modifier,
    palette: List<Color> = listOf(
        Color(0xFFE9D5FF),
        Color(0xFFA855F7),
        Color(0xFFFDE68A),
        Color(0xFF34D399),
        Color(0xFFF472B6)
    )
) {
    val config = LocalCosmicMotionConfig.current
    if (!config.enableCelebrations || triggerKey == null) return

    val progress = remember(triggerKey) { Animatable(0f) }
    val count = if (config.mode == MotionIntensityMode.FULL) 34 else 22
    val pieces = remember(triggerKey, count) {
        val rand = Random(triggerKey.hashCode())
        List(count) { i ->
            ConfettiSpec(
                startX = 0.12f + rand.nextFloat() * 0.76f,
                driftX = (rand.nextFloat() - 0.5f) * 0.28f,
                fallSpeed = 0.55f + rand.nextFloat() * 0.55f,
                sizeDp = 3.2f + rand.nextFloat() * 3.8f,
                phase = rand.nextFloat() * 6.28f,
                isStar = i % 3 == 0,
                color = palette[i % palette.size]
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(config.scaledDuration(2300), easing = LinearEasing))
    }

    if (progress.value < 0.99f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val p = progress.value
            val fade = if (p > 0.72f) ((1f - p) / 0.28f).coerceIn(0f, 1f) else 1f
            pieces.forEach { piece ->
                val x = (piece.startX + piece.driftX * p + sin(p * 10f + piece.phase) * 0.03f) * size.width
                val y = (-0.06f + p * 1.15f * piece.fallSpeed) * size.height
                val r = piece.sizeDp.dp.toPx()
                if (piece.isStar) {
                    drawMiniStar(Offset(x, y), r * 1.35f, piece.color.copy(alpha = fade * 0.92f))
                } else {
                    drawCircle(
                        color = piece.color.copy(alpha = fade * 0.85f),
                        radius = r * 0.62f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

private data class ConfettiSpec(
    val startX: Float,
    val driftX: Float,
    val fallSpeed: Float,
    val sizeDp: Float,
    val phase: Float,
    val isStar: Boolean,
    val color: Color
)

/**
 * Live 5-bar smooth cosmic voice equalizer for active TTS playback.
 */
@Composable
fun CosmicVoiceEqualizer(
    active: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    color: Color = Color(0xFFD8B4FE),
    height: Dp = 16.dp
) {
    val config = LocalCosmicMotionConfig.current
    val policy = LocalCosmicMotionPolicy.current
    if (!active) return

    val animate = policy.shouldAnimateContinuously && config.mode != MotionIntensityMode.REDUCED
    val wavePhase = rememberCosmicPhase(
        durationMs = 860,
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        staticValue = 1.2f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "cosmicVoiceEq",
        enabled = animate
    )
    val barBrush = remember(color) {
        Brush.verticalGradient(listOf(Color.White, color, Color(0xFF9333EA)))
    }

    Canvas(
        modifier = modifier
            .width((barCount * 5).dp)
            .height(height)
    ) {
        val barW = 2.4.dp.toPx()
        val gap = (size.width - barW * barCount) / (barCount - 1).coerceAtLeast(1)
        for (i in 0 until barCount) {
            val weight = if (!animate) {
                0.45f + (i % 2) * 0.25f
            } else {
                0.26f + 0.74f * (0.5f + 0.5f * sin(wavePhase + i * 1.05f))
            }
            val barH = (size.height * weight).coerceAtLeast(3.dp.toPx())
            val x = i * (barW + gap)
            val y = (size.height - barH) / 2f
            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
            )
        }
    }
}

/**
 * Vertical rolling counter badge text (e.g. "0 Ekli" -> "1 Ekli", "1 Aktif" -> "2 Aktif").
 */
@Composable
fun CosmicAnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    color: Color = Color(0xFFF3E8FF),
    fontSize: TextUnit = 11.5.sp,
    fontWeight: FontWeight = FontWeight.Medium
) {
    val config = LocalCosmicMotionConfig.current
    AnimatedContent(
        targetState = count,
        modifier = modifier,
        transitionSpec = {
            if (!config.enableMicroSprings) {
                fadeIn(tween(110)) togetherWith fadeOut(tween(90))
            } else if (targetState >= initialState) {
                (slideInVertically(tween(220, easing = CosmicMotionSpecs.CosmicEaseOut)) { it } + fadeIn(tween(200)))
                    .togetherWith(slideOutVertically(tween(180)) { -it } + fadeOut(tween(160)))
            } else {
                (slideInVertically(tween(220, easing = CosmicMotionSpecs.CosmicEaseOut)) { -it } + fadeIn(tween(200)))
                    .togetherWith(slideOutVertically(tween(180)) { it } + fadeOut(tween(160)))
            }
        },
        label = "cosmicCounter"
    ) { value ->
        Text(
            text = "$prefix$value$suffix",
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = InterFontFamily,
            maxLines = 1
        )
    }
}

/**
 * Floating "+2 ✦" affection / emotion delta badge that rises gently and fades out.
 */
@Composable
fun CosmicFloatingDeltaPop(
    deltaText: String?,
    triggerKey: Any?,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4ADE80)
) {
    val config = LocalCosmicMotionConfig.current
    if (deltaText.isNullOrBlank() || triggerKey == null) return

    val progress = remember(triggerKey) { Animatable(0f) }
    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(config.scaledDuration(1350), easing = CosmicMotionSpecs.CosmicEaseOut)
        )
    }

    if (progress.value < 0.99f) {
        val p = progress.value
        val alpha = when {
            p < 0.18f -> p / 0.18f
            p > 0.68f -> ((1f - p) / 0.32f).coerceIn(0f, 1f)
            else -> 1f
        }
        Box(
            modifier = modifier
                .graphicsLayer {
                    this.alpha = alpha
                    translationY = -p * 28.dp.toPx()
                    val s = if (p < 0.22f) 0.82f + (p / 0.22f) * 0.24f else 1.06f - (p - 0.22f) * 0.12f
                    scaleX = s
                    scaleY = s
                }
                .clip(RoundedCornerShape(50))
                .background(Color(0xDD1A0836))
                .border(1.dp, color.copy(alpha = 0.78f), RoundedCornerShape(50))
                .padding(horizontal = 9.dp, vertical = 3.dp)
        ) {
            Text(
                text = deltaText,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily
            )
        }
    }
}

/**
 * Luminous pulsing 4-point star / streaming caret shown at the end of live AI output.
 */
@Composable
fun CosmicStreamingCaret(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD8B4FE),
    size: Dp = 14.dp
) {
    val policy = LocalCosmicMotionPolicy.current
    val pulse = rememberCosmicPhase(
        durationMs = 650,
        initialValue = 0.45f,
        targetValue = 1.0f,
        staticValue = 0.85f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "streamingCaret",
        enabled = policy.shouldAnimateContinuously
    )
    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val r = this.size.minDimension * 0.46f * (0.82f + 0.18f * pulse)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(color.copy(alpha = 0.55f * pulse), Color.Transparent),
                center = center,
                radius = r * 1.45f
            ),
            radius = r * 1.45f,
            center = center
        )
        drawMiniStar(center, r, Color.White.copy(alpha = 0.75f + 0.25f * pulse))
    }
}

/**
 * Animated Mor Cam Toast Banner with thin shrinking progress underline.
 */
@Composable
fun CosmicToastBanner(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    durationMs: Long = 2600L
) {
    val config = LocalCosmicMotionConfig.current
    val progress = remember(message) { Animatable(1f) }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            progress.snapTo(1f)
            progress.animateTo(0f, tween(durationMs.toInt(), easing = LinearEasing))
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = if (config.enableMicroSprings) CosmicMotionSpecs.velvetGlass() else tween(140)
        ) { -it } + fadeIn(tween(180)),
        exit = slideOutVertically(tween(200)) { -it } + fadeOut(tween(160))
    ) {
        val accent = if (isError) Color(0xFFF472B6) else Color(0xFFC084FC)
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        if (isError) listOf(Color(0xEE4A0E2A), Color(0xEE2B071B))
                        else listOf(Color(0xEE240D48), Color(0xEE14062A))
                    )
                )
                .border(1.1.dp, accent.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isError) "⚠" else "✦",
                    color = accent,
                    fontSize = 13.sp
                )
                Text(
                    text = message.orEmpty(),
                    color = Color(0xFFFDFBFF),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = InterFontFamily,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value.coerceIn(0f, 1f))
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(accent, Color.White)))
                )
            }
        }
    }
}

/**
 * Live interactive motion preview strip inside Global Settings -> Sistem tab.
 * Immediately visualizes stardust density, glass sheen sweep, and spring orb bounce
 * for the selected [MotionIntensityMode].
 */
@Composable
fun CosmicMotionPreviewStrip(
    mode: MotionIntensityMode,
    modifier: Modifier = Modifier
) {
    val previewConfig = remember(mode) { CosmicMotionConfig.from(mode) }
    val policy = LocalCosmicMotionPolicy.current
    val allowPreviewAnim = policy.shouldAnimateContinuously && previewConfig.enableAmbientLoops
    val phase = rememberCosmicPhase(
        durationMs = 3200,
        initialValue = 0f,
        targetValue = 1f,
        staticValue = 0.35f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "motionPreviewPhase",
        enabled = allowPreviewAnim
    )
    val bounce = rememberCosmicPhase(
        durationMs = 1400,
        initialValue = -1f,
        targetValue = 1f,
        staticValue = 0f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "motionPreviewBounce",
        enabled = allowPreviewAnim
    )
    val orbCoreColors = remember { listOf(Color.White, Color(0xFFC084FC), Color(0xFF6D28D9)) }
    val sheenColors = remember(mode) {
        listOf(
            Color.Transparent,
            Color(0xFFF3E8FF).copy(alpha = if (mode == MotionIntensityMode.FULL) 0.22f else 0.14f),
            Color.Transparent
        )
    }

    val dustSeeds = remember {
        val rand = Random(77)
        List(14) { Triple(rand.nextFloat(), rand.nextFloat(), 1.2f + rand.nextFloat() * 1.8f) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xCC0D0420), Color(0xCC1E0A3D), Color(0xCC0D0420))
                )
            )
            .border(1.dp, Color(0x88A855F7), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (previewConfig.enableParticles) {
                val visibleCount = if (mode == MotionIntensityMode.FULL) dustSeeds.size else 8
                for (i in 0 until visibleCount) {
                    val (sx, sy, rDp) = dustSeeds[i]
                    val px = ((sx + phase * 0.25f) % 1f) * w
                    val py = (((sy - phase * 0.45f) % 1f) + 1f) % 1f * h
                    val r = rDp.dp.toPx()
                    drawCircle(
                        color = Color(0xFFD8B4FE).copy(alpha = if (mode == MotionIntensityMode.FULL) 0.72f else 0.45f),
                        radius = r,
                        center = Offset(px, py)
                    )
                }
            }
            if (previewConfig.enableGlassSheen) {
                val sheenX = (phase * 1.5f - 0.25f) * w
                drawRect(
                    brush = Brush.linearGradient(
                        colors = sheenColors,
                        start = Offset(sheenX - w * 0.18f, 0f),
                        end = Offset(sheenX + w * 0.18f, h)
                    )
                )
            }
            // Preview orbiting orb on the right
            val orbCenter = Offset(w - 22.dp.toPx(), h / 2f + if (allowPreviewAnim) bounce * 4.dp.toPx() else 0f)
            val orbR = 11.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(orbCoreColors, center = orbCenter, radius = orbR),
                radius = orbR,
                center = orbCenter
            )
            if (previewConfig.enableAmbientLoops) {
                drawCircle(
                    color = Color(0xFFE9D5FF).copy(alpha = 0.45f),
                    radius = orbR * (1.28f + 0.12f * bounce),
                    center = orbCenter,
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
        }

        Column {
            Text(
                text = "Canlı Önizleme • ${mode.label}",
                color = Color(0xFFF5EEFF),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = InterFontFamily
            )
            Text(
                text = mode.subtitle,
                color = Color(0xFFC4B5DF),
                fontSize = 10.sp,
                fontFamily = InterFontFamily,
                maxLines = 1
            )
        }
    }
}

private val reusableMiniStarPath = Path()

internal fun DrawScope.drawMiniStar(
    center: Offset,
    outerR: Float,
    color: Color
) {
    val innerR = outerR * 0.34f
    reusableMiniStarPath.reset()
    reusableMiniStarPath.moveTo(center.x, center.y - outerR)
    reusableMiniStarPath.quadraticTo(center.x + innerR * 0.45f, center.y - innerR * 0.45f, center.x + outerR, center.y)
    reusableMiniStarPath.quadraticTo(center.x + innerR * 0.45f, center.y + innerR * 0.45f, center.x, center.y + outerR)
    reusableMiniStarPath.quadraticTo(center.x - innerR * 0.45f, center.y + innerR * 0.45f, center.x - outerR, center.y)
    reusableMiniStarPath.quadraticTo(center.x - innerR * 0.45f, center.y - innerR * 0.45f, center.x, center.y - outerR)
    reusableMiniStarPath.close()
    drawPath(path = reusableMiniStarPath, color = color)
}
