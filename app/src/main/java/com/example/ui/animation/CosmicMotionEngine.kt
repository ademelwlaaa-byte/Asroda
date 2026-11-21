package com.example.ui.animation

import android.content.Context
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * User-selectable global motion intensity modes (persisted in SharedPreferences).
 */
enum class MotionIntensityMode(
    val key: String,
    val label: String,
    val subtitle: String
) {
    FULL(
        key = "full",
        label = "Tam",
        subtitle = "Tüm yıldız tozu, cam parıltısı, kutlama ve yay efektleri"
    ),
    BALANCED(
        key = "balanced",
        label = "Dengeli",
        subtitle = "Dengeli atmosfer, cam yansımaları ve tam yay fiziği"
    ),
    REDUCED(
        key = "reduced",
        label = "Azaltılmış",
        subtitle = "Pil dostu, sürekli döngüler kapalı, sade yumuşak geçişler"
    );

    companion object {
        fun fromKey(key: String?): MotionIntensityMode =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: BALANCED
    }
}

/**
 * Runtime configuration distributed app-wide via [LocalCosmicMotionConfig].
 */
data class CosmicMotionConfig(
    val mode: MotionIntensityMode,
    val enableAmbientLoops: Boolean,
    val enableGlassSheen: Boolean,
    val enableParticles: Boolean,
    val enableCelebrations: Boolean,
    val enableMicroSprings: Boolean,
    val durationScale: Float,
    val staggerStepMs: Int,
    val particleScale: Float
) {
    fun scaledDuration(baseMs: Int): Int =
        (baseMs * durationScale).toInt().coerceAtLeast(if (mode == MotionIntensityMode.REDUCED) 90 else 120)

    companion object {
        fun from(mode: MotionIntensityMode): CosmicMotionConfig = when (mode) {
            MotionIntensityMode.FULL -> CosmicMotionConfig(
                mode = mode,
                enableAmbientLoops = true,
                enableGlassSheen = true,
                enableParticles = true,
                enableCelebrations = true,
                enableMicroSprings = true,
                durationScale = 1.0f,
                staggerStepMs = 42,
                particleScale = 1.25f
            )
            MotionIntensityMode.BALANCED -> CosmicMotionConfig(
                mode = mode,
                enableAmbientLoops = true,
                enableGlassSheen = true,
                enableParticles = true,
                enableCelebrations = true,
                enableMicroSprings = true,
                durationScale = 1.0f,
                staggerStepMs = 34,
                particleScale = 0.78f
            )
            MotionIntensityMode.REDUCED -> CosmicMotionConfig(
                mode = mode,
                enableAmbientLoops = false,
                enableGlassSheen = false,
                enableParticles = false,
                enableCelebrations = false,
                enableMicroSprings = false,
                durationScale = 0.55f,
                staggerStepMs = 0,
                particleScale = 0f
            )
        }
    }
}

val LocalCosmicMotionConfig = compositionLocalOf {
    CosmicMotionConfig.from(MotionIntensityMode.BALANCED)
}

/**
 * Persistent preference manager for [MotionIntensityMode] with instant reactive [StateFlow].
 */
object CosmicMotionPreferences {
    private const val PREF_NAME = "aetheria_cosmic_motion_prefs_v1"
    private const val KEY_MODE = "motion_intensity_mode"

    @Volatile
    private var appContext: Context? = null

    private val _mode = MutableStateFlow(MotionIntensityMode.BALANCED)
    val mode: StateFlow<MotionIntensityMode> = _mode.asStateFlow()

    fun init(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        val saved = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MODE, MotionIntensityMode.BALANCED.key)
        _mode.value = MotionIntensityMode.fromKey(saved)
    }

    fun setMode(context: Context, newMode: MotionIntensityMode) {
        val ctx = appContext ?: context.applicationContext
        appContext = ctx
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, newMode.key)
            .apply()
        _mode.value = newMode
    }
}

/**
 * 4 Calibrated Cosmic Spring Physics profiles & smooth Bezier curves.
 */
object CosmicMotionSpecs {
    val CosmicEaseOut = CubicBezierEasing(0.16f, 1.0f, 0.30f, 1.0f)
    val CosmicEaseInOut = CubicBezierEasing(0.37f, 0.0f, 0.23f, 1.0f)

    fun <T> snappyOrb() = spring<T>(
        dampingRatio = 0.62f,
        stiffness = 560f
    )

    fun <T> velvetGlass() = spring<T>(
        dampingRatio = 0.78f,
        stiffness = 320f
    )

    fun <T> silkyPortal() = spring<T>(
        dampingRatio = 0.86f,
        stiffness = 240f
    )

    fun <T> playfulSpark() = spring<T>(
        dampingRatio = 0.48f,
        stiffness = 440f
    )

    /**
     * Directional depth-scaled portal transition for top-level screens (Menu <-> Chat / Wizard).
     */
    fun cosmicPortalTransition(
        forward: Boolean,
        config: CosmicMotionConfig
    ): ContentTransform {
        if (!config.enableMicroSprings) {
            return fadeIn(tween(150)) togetherWith fadeOut(tween(120))
        }
        val enterDur = config.scaledDuration(340)
        val exitDur = config.scaledDuration(250)
        val slideDiv = 8
        return if (forward) {
            (fadeIn(tween(enterDur, easing = CosmicEaseOut)) +
                slideInHorizontally(tween(enterDur, easing = CosmicEaseOut)) { it / slideDiv } +
                scaleIn(tween(enterDur, easing = CosmicEaseOut), initialScale = 0.965f))
                .togetherWith(
                    fadeOut(tween(exitDur, easing = FastOutSlowInEasing)) +
                        slideOutHorizontally(tween(exitDur, easing = FastOutSlowInEasing)) { -it / 14 } +
                        scaleOut(tween(exitDur, easing = FastOutSlowInEasing), targetScale = 0.97f)
                )
        } else {
            (fadeIn(tween(enterDur, easing = CosmicEaseOut)) +
                slideInHorizontally(tween(enterDur, easing = CosmicEaseOut)) { -it / slideDiv } +
                scaleIn(tween(enterDur, easing = CosmicEaseOut), initialScale = 0.975f))
                .togetherWith(
                    fadeOut(tween(exitDur, easing = FastOutSlowInEasing)) +
                        slideOutHorizontally(tween(exitDur, easing = FastOutSlowInEasing)) { it / 10 } +
                        scaleOut(tween(exitDur, easing = FastOutSlowInEasing), targetScale = 0.96f)
                )
        }
    }

    /**
     * Directional horizontal glide for tabs (Home tabs, Global Settings tabs, Wizard steps).
     */
    fun cosmicTabTransition(
        forward: Boolean,
        config: CosmicMotionConfig
    ): ContentTransform {
        if (!config.enableMicroSprings) {
            return fadeIn(tween(130)) togetherWith fadeOut(tween(100))
        }
        val enterDur = config.scaledDuration(280)
        val exitDur = config.scaledDuration(200)
        val dir = if (forward) 1 else -1
        return (fadeIn(tween(enterDur, easing = CosmicEaseOut)) +
            slideInHorizontally(tween(enterDur, easing = CosmicEaseOut)) { dir * (it / 9) })
            .togetherWith(
                fadeOut(tween(exitDur, easing = FastOutSlowInEasing)) +
                    slideOutHorizontally(tween(exitDur, easing = FastOutSlowInEasing)) { -dir * (it / 12) }
            )
    }
}

/**
 * Tactile spring compression + soft lavender pulse ring on press.
 * GPU-only via [graphicsLayer] and [drawWithContent].
 */
fun Modifier.cosmicTapSpring(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.94f,
    rippleColor: Color = Color(0xFFD8B4FE),
    cornerRadius: Dp = 20.dp
): Modifier = composed {
    val config = LocalCosmicMotionConfig.current
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetScale = if (isPressed && enabled) {
        if (config.enableMicroSprings) pressedScale else 0.98f
    } else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (config.enableMicroSprings) CosmicMotionSpecs.snappyOrb() else tween(90),
        label = "cosmicTapScale"
    )
    val ringProgress = remember { Animatable(0f) }

    LaunchedEffect(isPressed, config.enableMicroSprings) {
        if (isPressed && enabled && config.enableMicroSprings) {
            ringProgress.snapTo(0.05f)
            ringProgress.animateTo(1f, tween(340, easing = CosmicMotionSpecs.CosmicEaseOut))
        } else if (!isPressed) {
            ringProgress.snapTo(0f)
        }
    }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawWithContent {
            drawContent()
            val p = ringProgress.value
            if (p > 0.01f && p < 0.99f) {
                val alpha = (1f - p) * 0.42f
                val inset = (1f - p) * 4.dp.toPx()
                val cr = cornerRadius.toPx().coerceAtMost(size.minDimension / 2f)
                drawRoundRect(
                    color = rippleColor.copy(alpha = alpha),
                    topLeft = Offset(inset, inset),
                    size = Size(
                        (size.width - inset * 2f).coerceAtLeast(1f),
                        (size.height - inset * 2f).coerceAtLeast(1f)
                    ),
                    cornerRadius = CornerRadius(cr, cr),
                    style = Stroke(width = (1.8f * (1f - p * 0.5f)).dp.toPx())
                )
            }
        }
}

/**
 * Periodic diagonal silver-lavender glass light sweep across Mor Cam cards, pills, and covers.
 */
fun Modifier.cosmicGlassSheen(
    enabled: Boolean = true,
    cornerRadius: Dp = 20.dp,
    periodMs: Int = 5200,
    sheenColor: Color = Color(0xFFF3E8FF),
    maxAlpha: Float = 0.16f
): Modifier = composed {
    val config = LocalCosmicMotionConfig.current
    if (!enabled || !config.enableGlassSheen) return@composed this

    val infinite = rememberInfiniteTransition(label = "cosmicGlassSheen")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenPhase"
    )

    val effectiveAlpha = if (config.mode == MotionIntensityMode.FULL) maxAlpha else maxAlpha * 0.72f

    this.drawWithContent {
        drawContent()
        // Sheen sweeps cleanly during the first 28% of each cycle, then rests calmly
        if (phase < 0.28f && size.width > 4f && size.height > 4f) {
            val t = phase / 0.28f
            val bandW = size.width * 0.34f
            val startX = -bandW + (size.width + bandW * 2f) * t
            val cr = cornerRadius.toPx().coerceAtMost(size.minDimension / 2f)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        sheenColor.copy(alpha = effectiveAlpha * 0.45f),
                        sheenColor.copy(alpha = effectiveAlpha),
                        sheenColor.copy(alpha = effectiveAlpha * 0.45f),
                        Color.Transparent
                    ),
                    start = Offset(startX, 0f),
                    end = Offset(startX + bandW, size.height)
                ),
                cornerRadius = CornerRadius(cr, cr)
            )
        }
    }
}

/**
 * Soft pulsing amethyst border aura for highlighted cards, active tabs, and focused inputs.
 */
fun Modifier.cosmicBorderBreathing(
    enabled: Boolean = true,
    cornerRadius: Dp = 20.dp,
    glowColor: Color = Color(0xFFC084FC),
    minAlpha: Float = 0.12f,
    maxAlpha: Float = 0.42f,
    periodMs: Int = 2400
): Modifier = composed {
    val config = LocalCosmicMotionConfig.current
    if (!enabled || !config.enableAmbientLoops) return@composed this

    val infinite = rememberInfiniteTransition(label = "cosmicBorderBreath")
    val alpha by infinite.animateFloat(
        initialValue = minAlpha,
        targetValue = if (config.mode == MotionIntensityMode.FULL) maxAlpha else maxAlpha * 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderBreathAlpha"
    )

    this.drawWithContent {
        drawContent()
        val cr = cornerRadius.toPx().coerceAtMost(size.minDimension / 2f)
        drawRoundRect(
            color = glowColor.copy(alpha = alpha),
            cornerRadius = CornerRadius(cr, cr),
            style = Stroke(width = 1.6.dp.toPx())
        )
    }
}

/**
 * Staggered spring cascade entrance for list items, cards, and catalog rows.
 * Runs once per [key], strictly GPU-only via [graphicsLayer].
 */
fun Modifier.cosmicStaggeredEnter(
    index: Int,
    key: Any = index,
    maxStaggerIndex: Int = 8,
    slideOffsetDp: Dp = 18.dp,
    initialScale: Float = 0.96f
): Modifier = composed {
    val config = LocalCosmicMotionConfig.current
    val progress = remember(key) {
        Animatable(if (config.mode == MotionIntensityMode.REDUCED) 1f else 0f)
    }

    LaunchedEffect(key, config.mode) {
        if (config.mode == MotionIntensityMode.REDUCED) {
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        val clampedIndex = index.coerceIn(0, maxStaggerIndex)
        if (clampedIndex > 0 && config.staggerStepMs > 0) {
            delay((clampedIndex * config.staggerStepMs).toLong())
        }
        progress.animateTo(
            targetValue = 1f,
            animationSpec = CosmicMotionSpecs.velvetGlass()
        )
    }

    this.graphicsLayer {
        val p = progress.value.coerceIn(0f, 1.05f)
        alpha = p.coerceIn(0f, 1f)
        translationY = (1f - p) * slideOffsetDp.toPx()
        val s = initialScale + (1f - initialScale) * p
        scaleX = s
        scaleY = s
    }
}

/**
 * Directional spring reveal for incoming chat messages (User from bottom-right, AI from bottom-left).
 */
fun Modifier.cosmicMessageEnter(
    messageKey: Any,
    isUser: Boolean,
    animate: Boolean = true
): Modifier = composed {
    val config = LocalCosmicMotionConfig.current
    val progress = remember(messageKey) {
        Animatable(if (!animate || config.mode == MotionIntensityMode.REDUCED) 1f else 0f)
    }

    LaunchedEffect(messageKey, animate, config.mode) {
        if (!animate || config.mode == MotionIntensityMode.REDUCED) {
            progress.snapTo(1f)
        } else if (progress.value < 0.99f) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = CosmicMotionSpecs.velvetGlass()
            )
        }
    }

    this.graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        translationY = (1f - p) * 16.dp.toPx()
        translationX = (1f - p) * (if (isUser) 14.dp.toPx() else -14.dp.toPx())
        val s = 0.94f + 0.06f * p
        scaleX = s
        scaleY = s
        transformOrigin = if (isUser) TransformOrigin(0.92f, 0.85f) else TransformOrigin(0.08f, 0.85f)
    }
}

/**
 * Velvet spring modal/dialog entrance (0.90 -> 1.0 scale + upward glide + alpha).
 */
fun Modifier.cosmicDialogEnter(key: Any = Unit): Modifier = composed {
    val config = LocalCosmicMotionConfig.current
    val progress = remember(key) { Animatable(0f) }

    LaunchedEffect(key) {
        if (!config.enableMicroSprings) {
            progress.animateTo(1f, tween(130))
        } else {
            progress.animateTo(1f, CosmicMotionSpecs.velvetGlass())
        }
    }

    this.graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        translationY = (1f - p) * 18.dp.toPx()
        val s = 0.91f + 0.09f * p
        scaleX = s
        scaleY = s
    }
}

/**
 * Corner-anchored spring unfurl for MorCamPopupMenu dropdowns.
 */
fun Modifier.cosmicPopupEnter(
    origin: TransformOrigin = TransformOrigin(0.88f, 0f)
): Modifier = composed {
    val config = LocalCosmicMotionConfig.current
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (!config.enableMicroSprings) {
            progress.animateTo(1f, tween(110))
        } else {
            progress.animateTo(1f, CosmicMotionSpecs.snappyOrb())
        }
    }

    this.graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        val s = 0.86f + 0.14f * p
        scaleX = s
        scaleY = s
        transformOrigin = origin
    }
}

/**
 * Gentle horizontal attention shake when an error/retry state triggers.
 */
fun Modifier.cosmicShakeOnTrigger(triggerKey: Any?): Modifier = composed {
    val config = LocalCosmicMotionConfig.current
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        if (triggerKey == null || !config.enableMicroSprings) return@LaunchedEffect
        val offsets = listOf(8f, -7f, 5f, -3.5f, 2f, 0f)
        for (target in offsets) {
            offsetX.animateTo(target, tween(48, easing = FastOutSlowInEasing))
        }
    }

    this.graphicsLayer {
        translationX = offsetX.value.dp.toPx()
    }
}

/**
 * Soft floating vertical levitation for empty states, featured crests, and floating orbs.
 */
fun Modifier.cosmicFloatingLevitation(
    amplitudeDp: Dp = 5.dp,
    durationMs: Int = 2600
): Modifier = composed {
    val config = LocalCosmicMotionConfig.current
    if (!config.enableAmbientLoops) return@composed this

    val infinite = rememberInfiniteTransition(label = "cosmicFloat")
    val dy by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cosmicFloatDy"
    )
    this.graphicsLayer {
        translationY = dy * amplitudeDp.toPx()
    }
}

/**
 * "Cosmic Weaving" shimmer scan overlay for AI text/opening generation & loading cards.
 */
fun Modifier.cosmicShimmerSweep(
    active: Boolean,
    cornerRadius: Dp = 16.dp,
    shimmerColor: Color = Color(0xFFD8B4FE)
): Modifier = composed {
    val config = LocalCosmicMotionConfig.current
    if (!active) return@composed this

    val infinite = rememberInfiniteTransition(label = "cosmicShimmerSweep")
    val progress by infinite.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (config.mode == MotionIntensityMode.REDUCED) 2200 else 1350,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerSweepProg"
    )

    this.drawWithContent {
        drawContent()
        val cr = cornerRadius.toPx().coerceAtMost(size.minDimension / 2f)
        val band = size.width * 0.45f
        val x = progress * size.width
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    shimmerColor.copy(alpha = 0.16f),
                    Color.White.copy(alpha = 0.24f),
                    shimmerColor.copy(alpha = 0.16f),
                    Color.Transparent
                ),
                start = Offset(x - band, 0f),
                end = Offset(x + band, size.height)
            ),
            cornerRadius = CornerRadius(cr, cr)
        )
    }
}

/**
 * Launches a quick 1-shot spring scale punch whenever [triggerKey] increments.
 */
fun Modifier.cosmicPopOnTrigger(
    triggerKey: Int,
    peakScale: Float = 1.18f
): Modifier = composed {
    val config = LocalCosmicMotionConfig.current
    val scale = remember { Animatable(1f) }

    LaunchedEffect(triggerKey) {
        if (triggerKey <= 0 || !config.enableMicroSprings) return@LaunchedEffect
        scale.snapTo(1f)
        launch {
            scale.animateTo(peakScale, tween(110, easing = CosmicMotionSpecs.CosmicEaseOut))
            scale.animateTo(1f, CosmicMotionSpecs.playfulSpark())
        }
    }

    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}
