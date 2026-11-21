package com.example.ui.components

import com.example.ui.theme.AppColors
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF06030E),
    activeTab: String = "chats"
) {
    // 600ms Silky Crossfade + Depth Scale (1.04 -> 1.0) + Soft Blur Transition (6dp -> 0dp)
    val isTemplates = activeTab == "templates"
    val templatesProgress by animateFloatAsState(
        targetValue = if (isTemplates) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = SeamlessBgEasing),
        label = "templatesBgProgress"
    )
    val cosmicProgress = 1f - templatesProgress

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
        val list = mutableListOf<StarParticle>()
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

    val motionConfig = com.example.ui.animation.LocalCosmicMotionConfig.current

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
            AuroraBlob(0.25f, 0.14f, 0.62f, Color(0xFF7C3AED), 0f,   +0.06f, +0.04f),
            AuroraBlob(0.74f, 0.24f, 0.50f, Color(0xFF4C1D95), 1.2f, -0.05f, +0.03f),
            AuroraBlob(0.50f, 0.60f, 0.72f, Color(0xFF1E1B4B), 2.4f, +0.03f, -0.05f),
            AuroraBlob(0.14f, 0.52f, 0.44f, Color(0xFF5B21B6), 3.6f, +0.05f, +0.03f),
            AuroraBlob(0.84f, 0.76f, 0.48f, Color(0xFF6D28D9), 4.8f, -0.04f, -0.05f),
        )
    }

    val auroraTime: Float
    val twinkleTime: Float
    val dustRiseProgress: Float
    val shootingStarPhase: Float
    if (motionConfig.enableAmbientLoops) {
        val infinite = rememberInfiniteTransition(label = "bg")
        auroraTime = infinite.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(18000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "auroraTime"
        ).value
        twinkleTime = infinite.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(4200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "twinkleTime"
        ).value
        dustRiseProgress = infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(22000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "dustRiseProgress"
        ).value
        shootingStarPhase = infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(11500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shootingStarPhase"
        ).value
    } else {
        auroraTime = 0.8f
        twinkleTime = 1.2f
        dustRiseProgress = 0f
        shootingStarPhase = 0.9f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        // Layer A: Cosmic Background (Chats / Discover / Books) — RGB_565 downsampled via Coil
        if (cosmicProgress > 0.001f) {
            val cosmicBlurDp = ((1f - cosmicProgress) * 6f).dp
            val cosmicScale = 1f + (1f - cosmicProgress) * 0.04f
            AsyncImage(
                model = cosmicBgRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = cosmicProgress
                        scaleX = cosmicScale
                        scaleY = cosmicScale
                    }
                    .then(if (cosmicBlurDp > 0.2.dp && motionConfig.enableMicroSprings) Modifier.blur(cosmicBlurDp) else Modifier)
            )
        }

        // Layer B: Gothic Manor Background (Templates Tab) — RGB_565 downsampled via Coil
        if (templatesProgress > 0.001f) {
            val templatesBlurDp = ((1f - templatesProgress) * 6f).dp
            val templatesScale = 1f + (1f - templatesProgress) * 0.04f
            AsyncImage(
                model = gothicManorBgRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = templatesProgress
                        scaleX = templatesScale
                        scaleY = templatesScale
                    }
                    .then(if (templatesBlurDp > 0.2.dp && motionConfig.enableMicroSprings) Modifier.blur(templatesBlurDp) else Modifier)
            )
        }

        // Layer C: Ambient Particles, Starfield & Delicate Shooting Stardust Glint
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            if (cosmicProgress > 0.01f) {
                auroraBlobs.forEachIndexed { idx, blob ->
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
                                blob.color.copy(alpha = alpha * 1.4f),
                                blob.color.copy(alpha = alpha * 0.45f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = radius
                        ),
                        radius = radius,
                        center = Offset(cx, cy)
                    )
                }

                val starLimit = when (motionConfig.mode) {
                    com.example.ui.animation.MotionIntensityMode.FULL -> stars.size
                    com.example.ui.animation.MotionIntensityMode.BALANCED -> (stars.size * 0.78f).toInt()
                    com.example.ui.animation.MotionIntensityMode.REDUCED -> (stars.size * 0.45f).toInt()
                }
                for (si in 0 until starLimit.coerceAtMost(stars.size)) {
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
            if (motionConfig.enableParticles) {
                val dustCount = when (motionConfig.mode) {
                    com.example.ui.animation.MotionIntensityMode.FULL -> floatingDust.size
                    com.example.ui.animation.MotionIntensityMode.BALANCED -> 9
                    com.example.ui.animation.MotionIntensityMode.REDUCED -> 0
                }
                val dustVisibility = (0.30f + 0.45f * templatesProgress) * motionConfig.particleScale.coerceAtMost(1.15f)
                for (di in 0 until dustCount) {
                    val mote = floatingDust[di]
                    val rawY = (mote.initialYRatio - dustRiseProgress * mote.riseSpeed * 3.5f)
                    val wrappedY = ((rawY % 1f) + 1f) % 1f
                    val swayX = sin(twinkleTime * 0.8f + mote.phase) * mote.swayAmp
                    val px = ((mote.xRatio + swayX).coerceIn(0.02f, 0.98f)) * w
                    val py = wrappedY * h

                    val edgeFade = when {
                        wrappedY < 0.08f -> wrappedY / 0.08f
                        wrappedY > 0.92f -> (1f - wrappedY) / 0.08f
                        else -> 1f
                    }
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
