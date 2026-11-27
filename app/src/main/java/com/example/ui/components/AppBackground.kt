package com.example.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.R
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.LocalCosmicMotionPolicy
import com.example.ui.animation.MotionIntensityMode
import com.example.ui.animation.rememberCosmicPhaseState
import com.example.ui.theme.AppColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class StarParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radiusPx: Float,
    val baseAlpha: Float,
    val phaseOffset: Float,
    val twinkleSpeed: Float,
    val color: Color
)

private data class AuroraBlob(
    val xRatio: Float,
    val yRatio: Float,
    val radiusRatio: Float,
    val color: Color,
    val phaseOffset: Float,
    val driftX: Float,
    val driftY: Float
)

private data class FloatingCosmicDust(
    val xRatio: Float,
    val initialYRatio: Float,
    val radiusDp: Float,
    val riseSpeed: Float,
    val swayAmp: Float,
    val phase: Float,
    val color: Color
)

private val SeamlessBgEasing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

internal fun computeBackgroundLayerAlphas(templatesProgress: Float): Pair<Float, Float> {
    val safeTemplates = if (templatesProgress.isFinite()) templatesProgress.coerceIn(0f, 1f) else 0f
    return (1f - safeTemplates) to safeTemplates
}

internal fun computeDustEdgeFade(wrappedY: Float): Float {
    val y = if (wrappedY.isFinite()) ((wrappedY % 1f) + 1f) % 1f else 0.5f
    return when {
        y < 0.08f -> (y / 0.08f).coerceIn(0f, 1f)
        y > 0.92f -> ((1f - y) / 0.08f).coerceIn(0f, 1f)
        else -> 1f
    }
}

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF06030E),
    activeTab: String = "chats"
) {
    // 600ms Silky Crossfade (150ms when Reduce Motion / quick transitions active)
    val motionConfig = LocalCosmicMotionConfig.current
    val motionPolicy = LocalCosmicMotionPolicy.current
    val isTemplates = activeTab == "templates"
    val templatesProgressState = animateFloatAsState(
        targetValue = if (isTemplates) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (motionPolicy.shouldUseQuickTransitions) 150 else 600,
            easing = SeamlessBgEasing
        ),
        label = "templatesBgProgress"
    )
    // Only toggle AsyncImage composition at 0/1 visibility thresholds (0 recompositions during crossfade)
    val showCosmicLayer by remember { derivedStateOf { templatesProgressState.value < 0.999f } }
    val showTemplatesLayer by remember { derivedStateOf { templatesProgressState.value > 0.001f } }

    // Build RGB_565 Coil requests (50% RAM reduction vs ARGB_8888, automatic viewport downsampling)
    val context = LocalContext.current
    val cosmicBgRequest = remember(context) {
        ImageRequest.Builder(context)
            .data(R.drawable.img_botlist_gothic_wisteria_bg)
            .allowRgb565(true)
            .crossfade(false)
            .build()
    }
    val gothicManorBgRequest = remember(context) {
        ImageRequest.Builder(context)
            .data(R.drawable.img_templates_bg_gothic_manor)
            .allowRgb565(true)
            .crossfade(false)
            .build()
    }

    // Warm both RGB_565 backgrounds in Coil memory cache so tab switches have zero flicker
    LaunchedEffect(context) {
        context.imageLoader.enqueue(cosmicBgRequest)
        context.imageLoader.enqueue(gothicManorBgRequest)
    }

    val stars = remember {
        val rand = Random(42)
        val list = ArrayList<StarParticle>(91)
        val colors = listOf(
            AppColors.White,
            AppColors.PurpleSoft,
            AppColors.Hex_BAE6FD,
            AppColors.YellowLight,
            Color(0xFFF0ABFC)
        )
        for (i in 0 until 65) {
            list.add(
                StarParticle(
                    xRatio = rand.nextFloat(),
                    yRatio = rand.nextFloat(),
                    radiusPx = rand.nextFloat() * 1.3f + 0.6f,
                    baseAlpha = rand.nextFloat() * 0.22f + 0.06f,
                    phaseOffset = rand.nextFloat() * (2f * PI.toFloat()),
                    twinkleSpeed = 0.6f + rand.nextFloat() * 0.9f,
                    color = colors[i % colors.size]
                )
            )
        }
        for (i in 0 until 26) {
            list.add(
                StarParticle(
                    xRatio = rand.nextFloat(),
                    yRatio = rand.nextFloat(),
                    radiusPx = rand.nextFloat() * 2.2f + 1.2f,
                    baseAlpha = rand.nextFloat() * 0.30f + 0.12f,
                    phaseOffset = rand.nextFloat() * (2f * PI.toFloat()),
                    twinkleSpeed = 0.4f + rand.nextFloat() * 0.6f,
                    color = colors[i % colors.size]
                )
            )
        }
        list
    }

    // 14 subtle micro purple/lilac light particles (scaled by motion intensity)
    val floatingDust = remember {
        val rand = Random(108)
        val palette = listOf(
            Color(0xFFD8B4FE),
            Color(0xFFC084FC),
            Color(0xFFA855F7),
            Color(0xFFF3E8FF)
        )
        List(14) { i ->
            FloatingCosmicDust(
                xRatio = 0.06f + rand.nextFloat() * 0.88f,
                initialYRatio = rand.nextFloat(),
                radiusDp = 0.9f + rand.nextFloat() * 1.25f,
                riseSpeed = 0.09f + rand.nextFloat() * 0.14f,
                swayAmp = 0.010f + rand.nextFloat() * 0.018f,
                phase = rand.nextFloat() * (2f * PI.toFloat()),
                color = palette[i % palette.size]
            )
        }
    }

    val auroraBlobs = remember {
        listOf(
            AuroraBlob(0.25f, 0.14f, 0.62f, Color(0xFF7C3AED), 0f, +0.06f, +0.04f),
            AuroraBlob(0.74f, 0.24f, 0.50f, Color(0xFF4C1D95), 1.2f, -0.05f, +0.03f),
            AuroraBlob(0.50f, 0.60f, 0.72f, Color(0xFF1E1B4B), 2.4f, +0.03f, -0.05f),
            AuroraBlob(0.14f, 0.52f, 0.44f, Color(0xFF5B21B6), 3.6f, +0.05f, +0.03f),
            AuroraBlob(0.84f, 0.76f, 0.48f, Color(0xFF6D28D9), 4.8f, -0.04f, -0.05f)
        )
    }

    // Draw-phase-only State<Float> drivers -> 0 FPS root recomposition across all screens!
    val allowAmbient = motionPolicy.shouldAnimateContinuously && motionConfig.enableAmbientLoops
    val auroraTimeState = rememberCosmicPhaseState(
        durationMs = 18000,
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        staticValue = 0.8f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "auroraTime",
        enabled = allowAmbient
    )
    val twinkleTimeState = rememberCosmicPhaseState(
        durationMs = 4200,
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        staticValue = 1.2f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "twinkleTime",
        enabled = allowAmbient
    )
    val dustRiseProgressState = rememberCosmicPhaseState(
        durationMs = 22000,
        initialValue = 0f,
        targetValue = 1f,
        staticValue = 0f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "dustRiseProgress",
        enabled = allowAmbient
    )
    val shootingStarPhaseState = rememberCosmicPhaseState(
        durationMs = 11500,
        initialValue = 0f,
        targetValue = 1f,
        staticValue = 0.9f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "shootingStarPhase",
        enabled = allowAmbient
    )

    val starLimit = when (motionConfig.mode) {
        MotionIntensityMode.FULL -> stars.size
        MotionIntensityMode.BALANCED -> (stars.size * 0.78f).toInt()
        MotionIntensityMode.REDUCED -> (stars.size * 0.45f).toInt()
    }.coerceAtMost(stars.size)

    val dustCount = when (motionConfig.mode) {
        MotionIntensityMode.FULL -> floatingDust.size
        MotionIntensityMode.BALANCED -> 9
        MotionIntensityMode.REDUCED -> 0
    }.coerceAtMost(floatingDust.size)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        // Layer A: Cosmic Background (Chats / Discover / Books) — RGB_565 downsampled via Coil
        if (showCosmicLayer) {
            AsyncImage(
                model = cosmicBgRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val (cosmicAlpha, _) = computeBackgroundLayerAlphas(templatesProgressState.value)
                        val cosmicScale = 1f + (1f - cosmicAlpha) * 0.04f
                        alpha = cosmicAlpha
                        scaleX = cosmicScale
                        scaleY = cosmicScale
                    }
            )
        }

        // Layer B: Gothic Manor Background (Templates Tab) — RGB_565 downsampled via Coil
        if (showTemplatesLayer) {
            AsyncImage(
                model = gothicManorBgRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val (_, templatesAlpha) = computeBackgroundLayerAlphas(templatesProgressState.value)
                        val templatesScale = 1f + (1f - templatesAlpha) * 0.04f
                        alpha = templatesAlpha
                        scaleX = templatesScale
                        scaleY = templatesScale
                    }
            )
        }

        // Layer C: Ambient Particles, Starfield & Delicate Shooting Stardust Glint (draw-phase only)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val (cosmicProgress, templatesProgress) = computeBackgroundLayerAlphas(templatesProgressState.value)
            val auroraTime = auroraTimeState.value
            val twinkleTime = twinkleTimeState.value
            val dustRiseProgress = dustRiseProgressState.value
            val shootingStarPhase = shootingStarPhaseState.value

            if (cosmicProgress > 0.01f) {
                for (idx in auroraBlobs.indices) {
                    val blob = auroraBlobs[idx]
                    val phase = auroraTime + blob.phaseOffset
                    val driftX = sin(phase * blob.driftX * 4f) * w * 0.05f
                    val driftY = cos(phase * blob.driftY * 4f) * h * 0.04f
                    val cx = blob.xRatio * w + driftX
                    val cy = blob.yRatio * h + driftY
                    val radius = blob.radiusRatio * w
                    val alpha = (0.038f + 0.020f * sin(phase + idx)) * cosmicProgress

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                blob.color.copy(alpha = (alpha * 1.4f).coerceIn(0f, 1f)),
                                blob.color.copy(alpha = (alpha * 0.45f).coerceIn(0f, 1f)),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = radius
                        ),
                        radius = radius,
                        center = Offset(cx, cy)
                    )
                }

                for (si in 0 until starLimit) {
                    val star = stars[si]
                    val sx = star.xRatio * w
                    val sy = star.yRatio * h
                    val twinkle = 0.5f + 0.5f * sin(twinkleTime * star.twinkleSpeed + star.phaseOffset)
                    val alpha = (star.baseAlpha * (0.35f + 0.85f * twinkle) * cosmicProgress).coerceIn(0f, 1f)

                    if (star.radiusPx > 1.9f && twinkle > 0.72f) {
                        drawCircle(
                            color = star.color.copy(alpha = alpha * 0.22f),
                            radius = star.radiusPx * 2.8f,
                            center = Offset(sx, sy)
                        )
                    }
                    drawCircle(
                        color = star.color.copy(alpha = alpha),
                        radius = star.radiusPx,
                        center = Offset(sx, sy)
                    )
                }

                // Delicate diagonal shooting stardust streak across the upper cosmos
                if (motionConfig.enableParticles && shootingStarPhase < 0.09f) {
                    val t = shootingStarPhase / 0.09f
                    val streakFade = sin(t * PI.toFloat()).coerceIn(0f, 1f) * cosmicProgress
                    val headX = w * (0.14f + t * 0.62f)
                    val headY = h * (0.07f + t * 0.16f)
                    val tailX = headX - w * 0.16f
                    val tailY = headY - h * 0.042f
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFD8B4FE).copy(alpha = 0.42f * streakFade),
                                Color.White.copy(alpha = 0.78f * streakFade)
                            ),
                            start = Offset(tailX, tailY),
                            end = Offset(headX, headY)
                        ),
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY),
                        strokeWidth = 1.6.dp.toPx()
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f * streakFade),
                        radius = 2.0.dp.toPx(),
                        center = Offset(headX, headY)
                    )
                }
            }

            // Rising Micro Purple/Lilac Cosmic Dust Particles
            if (motionConfig.enableParticles && dustCount > 0) {
                val dustVisibility = (0.30f + 0.45f * templatesProgress) * motionConfig.particleScale.coerceAtMost(1.15f)
                for (di in 0 until dustCount) {
                    val mote = floatingDust[di]
                    val rawY = mote.initialYRatio - dustRiseProgress * mote.riseSpeed * 3.5f
                    val wrappedY = ((rawY % 1f) + 1f) % 1f
                    val swayX = sin(twinkleTime * 0.8f + mote.phase) * mote.swayAmp
                    val px = (mote.xRatio + swayX).coerceIn(0.02f, 0.98f) * w
                    val py = wrappedY * h

                    val edgeFade = computeDustEdgeFade(wrappedY)
                    val pulse = 0.35f + 0.65f * (0.5f + 0.5f * sin(twinkleTime * 1.3f + mote.phase))
                    val moteAlpha = (pulse * edgeFade * dustVisibility * 0.34f).coerceIn(0f, 1f)
                    val rPx = mote.radiusDp.dp.toPx()

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                mote.color.copy(alpha = moteAlpha * 0.48f),
                                mote.color.copy(alpha = moteAlpha * 0.14f),
                                Color.Transparent
                            ),
                            center = Offset(px, py),
                            radius = rPx * 2.5f
                        ),
                        radius = rPx * 2.5f,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = Color(0xFFE9D5FF).copy(alpha = moteAlpha * 0.60f),
                        radius = rPx * 0.68f,
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
