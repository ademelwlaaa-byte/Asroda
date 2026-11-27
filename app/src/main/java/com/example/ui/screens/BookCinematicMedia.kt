package com.example.ui.screens

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.screens.cosmic3d.Cosmic3DSoundEngine
import com.example.ui.screens.cosmic3d.CosmicCharacterVoiceEngine
import com.example.ui.screens.cosmic3d.CosmicDuelBossId
import com.example.ui.screens.cosmic3d.CosmicRooftopBossDuelDialog
import com.example.ui.screens.cosmic3d.CosmicSfx
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Forces any Compose fullscreen [Dialog] window to fill 100% edge-to-edge across the entire phone
 * screen (behind status bar, cutout, AND bottom gesture bar) with zero activity background peek
 * at the top and immersive transient system bars.
 */
@Composable
fun ConfigureTrueFullscreenDialogWindow(hideSystemBars: Boolean = true) {
    val view = LocalView.current
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
        if (hideSystemBars) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            )
        } else {
            // Keep edge-to-edge transparent bars WITHOUT FLAG_LAYOUT_NO_LIMITS so Compose
            // WindowInsets.statusBars and WindowInsets.navigationBars receive exact real heights!
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            window.addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            )
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.BLACK))
        window.setDimAmount(0f)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        window.decorView.setPadding(0, 0, 0, 0)
        val lp = window.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.gravity = Gravity.FILL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.attributes = lp
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, view)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        if (hideSystemBars) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@DrawableRes
private fun sceneBackdropRes(sceneId: String): Int = when (sceneId) {
    "BLUE_SKY_2006" -> R.drawable.img_anim_blue_sky_2006
    "WHITE_EYE_AWAKENING" -> R.drawable.img_ch1_c1_vision_trace
    "BOSPHORUS_RESONANCE" -> R.drawable.img_anim_bosphorus
    "MANHATTAN_ARMADA" -> R.drawable.img_anim_armada
    else -> R.drawable.img_anim_mutlak_uyanis
}

/**
 * Live-Action 2.5D IMAX Cinema Motion Scene rendered inline inside story chapters
 * via [[ANIM|SCENE_ID|CAPTION|SUBTITLE]].
 *
 * Uses photorealistic live-action movie stills paired with GPU 2.5D depth parallax,
 * interactive 3D tilt, live 60fps rain/lightning/cosmic energy particle overlays,
 * and emotional organic soundscapes (no ugly low-poly primitives!).
 */
@Composable
fun InStoryAnimatedScene(
    sceneId: String,
    caption: String,
    subtitle: String,
    chapterAccent: Color
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isFullscreenOpen by remember { mutableStateOf(false) }
    var inlineAudioEnabled by remember { mutableStateOf(false) }
    var tiltX by remember { mutableFloatStateOf(0f) }
    var inlineSoundRef by remember { mutableStateOf<Cosmic3DSoundEngine?>(null) }

    val accent = when (sceneId) {
        "BLUE_SKY_2006" -> Color(0xFF38BDF8)
        "WHITE_EYE_AWAKENING" -> Color(0xFFF8FAFC)
        "BOSPHORUS_RESONANCE" -> Color(0xFFF43F5E)
        "MANHATTAN_ARMADA" -> Color(0xFF60A5FA)
        "MUTLAK_UYANIS" -> Color(0xFFEAB308)
        else -> chapterAccent
    }

    val motionConfig = LocalCosmicMotionConfig.current
    val (gyroYaw, gyroPitch) = rememberGyroscopeTilt(enabled = motionConfig.enableAmbientLoops && !isFullscreenOpen)

    LaunchedEffect(inlineAudioEnabled, isFullscreenOpen) {
        if (inlineAudioEnabled && !isFullscreenOpen) {
            if (inlineSoundRef == null) {
                val eng = Cosmic3DSoundEngine(context, enableOrchestralMusic = false)
                eng.start(ambientSceneId = sceneId, ambientVolume = 0.65f)
                inlineSoundRef = eng
            } else {
                inlineSoundRef?.setMuted(false)
            }
        } else {
            inlineSoundRef?.stopAndRelease()
            inlineSoundRef = null
        }
    }

    DisposableEffect(lifecycleOwner, isFullscreenOpen) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> inlineSoundRef?.setMuted(true)
                Lifecycle.Event.ON_RESUME -> if (inlineAudioEnabled && !isFullscreenOpen) inlineSoundRef?.setMuted(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            inlineSoundRef?.stopAndRelease()
            inlineSoundRef = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .morCamCosmicGlow(cornerRadius = 22.dp, glowColor = accent, alpha = 0.46f, blurDp = 20.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xF5070516))
            .border(1.2.dp, accent.copy(alpha = 0.74f), RoundedCornerShape(22.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(238.dp)
                .pointerInput(sceneId) {
                    detectHorizontalDragGestures(
                        onDragEnd = { tiltX = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            tiltX = (tiltX + dragAmount * 0.045f).coerceIn(-12f, 12f)
                        }
                    )
                }
        ) {
            LiveActionCinemaMotionCanvas(
                sceneId = sceneId,
                tiltYawDeg = (tiltX + gyroYaw * 0.65f).coerceIn(-14f, 14f),
                tiltPitchDeg = (gyroPitch * 0.55f).coerceIn(-9f, 9f),
                directorProgress = -1f,
                modifier = Modifier.fillMaxSize()
            )

            // Cinema letterbox vignettes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .align(Alignment.TopCenter)
                    .background(Brush.verticalGradient(listOf(Color(0xB8050412), Color.Transparent)))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE00A061C))))
            )

            // Top-left Live Cinema Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xCC09061B))
                    .border(0.85.dp, accent.copy(alpha = 0.78f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "✦ 2.5D SİNEMA · 4 PERDE",
                    fontFamily = InterFontFamily,
                    color = accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.9.sp
                )
            }

            // Top-right Audio & Fullscreen Theater Controls
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .heightIn(min = 34.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (inlineAudioEnabled) accent.copy(alpha = 0.30f) else Color(0xCC110B2E))
                        .border(0.9.dp, if (inlineAudioEnabled) accent else Color(0x99BAE6FD), RoundedCornerShape(50))
                        .clickable { inlineAudioEnabled = !inlineAudioEnabled }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (inlineAudioEnabled) "🔊 SES" else "🔇 SES",
                        fontFamily = InterFontFamily,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .heightIn(min = 34.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xCC110B2E))
                        .border(0.9.dp, Color(0xCCBAE6FD), RoundedCornerShape(50))
                        .clickable { isFullscreenOpen = true }
                        .testTag("cinema_fullscreen_$sceneId")
                        .padding(horizontal = 11.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⤢ YÖNETMEN MODU",
                        fontFamily = InterFontFamily,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                }
            }

            Text(
                text = "↔ Telefonu eğ veya sürükle · Tam ekranda 4 Perde yönetmen sahnesini izle",
                fontFamily = InterFontFamily,
                color = Color(0xCCE2D9F8),
                fontSize = 9.2.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xEC0C0720))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = caption,
                fontFamily = DisplaySerifFontFamily,
                color = Color.White,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    fontFamily = LiterarySerifFontFamily,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFFD6C9F0),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }
    }

    if (isFullscreenOpen) {
        LiveActionFullscreenCinemaDialog(
            sceneId = sceneId,
            caption = caption,
            subtitle = subtitle,
            accent = accent,
            onDismiss = { isFullscreenOpen = false }
        )
    }
}

/**
 * Hardware Gyroscope / Accelerometer 2.5D tilt tracker for living parallax depth.
 * Automatically unregisters on ON_PAUSE & respects reduced-motion settings.
 */
@Composable
private fun rememberGyroscopeTilt(enabled: Boolean): Pair<Float, Float> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var yaw by remember { mutableFloatStateOf(0f) }
    var pitch by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context, lifecycleOwner, enabled) {
        if (!enabled) {
            yaw = 0f
            pitch = 0f
            return@DisposableEffect onDispose {}
        }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ?: return@DisposableEffect onDispose {}
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: return@DisposableEffect onDispose {}

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var smoothX = 0f
        var smoothY = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val targetX: Float
                val targetY: Float
                if (event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    targetX = (orientation[2] * 14f).coerceIn(-10f, 10f)
                    targetY = ((orientation[1] + 0.6f) * 10f).coerceIn(-8f, 8f)
                } else {
                    targetX = (-event.values[0] * 1.4f).coerceIn(-10f, 10f)
                    targetY = ((event.values[1] - 5.5f) * 1.1f).coerceIn(-8f, 8f)
                }
                smoothX += (targetX - smoothX) * 0.14f
                smoothY += (targetY - smoothY) * 0.14f
                // Deadzone filter prevents idle accelerometer jitter from triggering 50Hz recompositions
                if (abs(smoothX - yaw) >= 0.08f) yaw = smoothX
                if (abs(smoothY - pitch) >= 0.08f) pitch = smoothY
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        var isRegistered = false
        fun register() {
            if (!isRegistered) {
                isRegistered = sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
            }
        }
        fun unregister() {
            if (isRegistered) {
                sensorManager.unregisterListener(listener)
                isRegistered = false
            }
        }
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            register()
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> unregister()
                Lifecycle.Event.ON_RESUME -> register()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            unregister()
        }
    }

    return yaw to pitch
}

private fun cinemaBeatLabels(sceneId: String): List<String> = when (sceneId) {
    "BLUE_SKY_2006" -> listOf(
        "Perde 1/4 · 2006 Gökyüzü",
        "Perde 2/4 · Boyut Çatlağı",
        "Perde 3/4 · Mavi Şimşek Zirvesi",
        "Perde 4/4 · Kozmik İz"
    )
    "WHITE_EYE_AWAKENING" -> listOf(
        "Perde 1/4 · Konferans Sessizliği",
        "Perde 2/4 · Göz Bebekleri Ağarıyor",
        "Perde 3/4 · Üç Saniyelik Boşluk",
        "Perde 4/4 · Zihin Yankısı"
    )
    "BOSPHORUS_RESONANCE" -> listOf(
        "Perde 1/4 · Boğaz Altı Mağarası",
        "Perde 2/4 · İkiz Enerji Girdabı",
        "Perde 3/4 · Rezonans Çekirdeği",
        "Perde 4/4 · Fırtına Dengesi"
    )
    "MANHATTAN_ARMADA" -> listOf(
        "Perde 1/4 · Bulutların Ardındaki Gölge",
        "Perde 2/4 · Gümüş Halka Tarama",
        "Perde 3/4 · Gök Yaran Yıldırım",
        "Perde 4/4 · Kuşatma Başlıyor"
    )
    else -> listOf(
        "Perde 1/4 · Çatıda Sağanak",
        "Perde 2/4 · Zaman Yavaşlıyor",
        "Perde 3/4 · Donmuş Yağmur & Mutlak Uyanış",
        "Perde 4/4 · Kozmik Mührün Doğuşu"
    )
}

internal fun computeCinemaClimaxIntensity(timeline: Float): Float {
    val t = if (timeline.isFinite()) timeline.coerceIn(0f, 1f) else 0f
    return when {
        t < 0.25f -> (t / 0.25f) * 0.35f
        t < 0.55f -> 0.35f + ((t - 0.25f) / 0.30f) * 0.45f
        t < 0.82f -> 0.80f + sin(((t - 0.55f) / 0.27f) * PI.toFloat()) * 0.20f
        else -> (1f - ((t - 0.82f) / 0.18f)) * 0.65f + 0.18f
    }.coerceIn(0f, 1f)
}

internal fun computeCinemaDirectedZoom(timeline: Float): Float {
    val t = if (timeline.isFinite()) timeline.coerceIn(0f, 1f) else 0f
    return when {
        t < 0.25f -> 1.02f + (t / 0.25f) * 0.03f
        t < 0.55f -> 1.05f + ((t - 0.25f) / 0.30f) * 0.045f
        t < 0.82f -> 1.095f + sin(((t - 0.55f) / 0.27f) * PI.toFloat()) * 0.025f
        else -> 1.095f - ((t - 0.82f) / 0.18f) * 0.055f
    }.coerceIn(1.0f, 1.15f)
}

internal fun computeCinemaBeatIndex(directorProgress: Float): Int {
    val p = if (directorProgress.isFinite()) directorProgress.coerceIn(0f, 0.9999f) else 0f
    return (p * 4f).toInt().coerceIn(0, 3)
}

internal fun advanceCinemaProgress(current: Float, deltaSec: Float, durationSec: Float = 12f): Float {
    val safeCur = if (current.isFinite()) current.coerceIn(0f, 1f) else 0f
    val safeDt = if (deltaSec.isFinite()) deltaSec.coerceIn(0f, 0.25f) else 0f
    val safeDur = if (durationSec.isFinite() && durationSec > 0.1f) durationSec else 12f
    return ((safeCur + safeDt / safeDur) % 1f + 1f) % 1f
}

private val reusableLightningMainPath = Path()
private val reusableLightningBranchPath = Path()
private val reusableLightningPoints = Array(7) { Offset.Zero }

/**
 * Procedural branching lightning renderer for Manhattan Armada & Blue Sky 2006 cinematics.
 * Uses pre-allocated Path/Array buffers -> zero per-frame allocations at 60fps.
 */
private fun DrawScope.drawBranchingLightning(
    start: Offset,
    end: Offset,
    seed: Int,
    glowColor: Color,
    alpha: Float
) {
    if (!alpha.isFinite() || alpha <= 0.02f) return
    val clampedAlpha = alpha.coerceIn(0f, 1f)
    val rng = Random(seed)
    val segments = 6
    reusableLightningPoints[0] = start
    for (i in 1 until segments) {
        val t = i / segments.toFloat()
        val jitterX = (rng.nextFloat() - 0.5f) * size.width * 0.085f
        val jitterY = (rng.nextFloat() - 0.5f) * size.height * 0.03f
        reusableLightningPoints[i] = Offset(
            start.x + (end.x - start.x) * t + jitterX,
            start.y + (end.y - start.y) * t + jitterY
        )
    }
    reusableLightningPoints[segments] = end

    reusableLightningMainPath.reset()
    reusableLightningMainPath.moveTo(reusableLightningPoints[0].x, reusableLightningPoints[0].y)
    for (i in 1..segments) {
        reusableLightningMainPath.lineTo(reusableLightningPoints[i].x, reusableLightningPoints[i].y)
    }
    drawPath(reusableLightningMainPath, glowColor.copy(alpha = clampedAlpha * 0.42f), style = Stroke(width = 11f, cap = StrokeCap.Round))
    drawPath(reusableLightningMainPath, glowColor.copy(alpha = clampedAlpha * 0.85f), style = Stroke(width = 4.5f, cap = StrokeCap.Round))
    drawPath(reusableLightningMainPath, Color.White.copy(alpha = clampedAlpha), style = Stroke(width = 1.9f, cap = StrokeCap.Round))

    // 2 procedural secondary forks
    for (idx in 0..1) {
        val origin = reusableLightningPoints[if (idx == 0) 2 else 4]
        val dir = if (idx == 0) -1f else 1f
        val midX = origin.x + dir * size.width * (0.045f + rng.nextFloat() * 0.04f)
        val midY = origin.y + size.height * 0.085f
        val tipX = midX + dir * size.width * 0.035f
        val tipY = midY + size.height * 0.075f
        reusableLightningBranchPath.reset()
        reusableLightningBranchPath.moveTo(origin.x, origin.y)
        reusableLightningBranchPath.lineTo(midX, midY)
        reusableLightningBranchPath.lineTo(tipX, tipY)
        drawPath(reusableLightningBranchPath, glowColor.copy(alpha = clampedAlpha * 0.55f), style = Stroke(width = 3.2f, cap = StrokeCap.Round))
        drawPath(reusableLightningBranchPath, Color.White.copy(alpha = clampedAlpha * 0.80f), style = Stroke(width = 1.2f, cap = StrokeCap.Round))
    }
}

/**
 * Reusable 60fps Live-Action 2.5D Multi-Beat Directed Cinema + Procedural GPU VFX Engine.
 * Reads continuous animation phases strictly inside graphicsLayer & Canvas -> 0 root recompositions/sec!
 */
@Composable
private fun LiveActionCinemaMotionCanvas(
    sceneId: String,
    tiltYawDeg: Float,
    tiltPitchDeg: Float,
    directorProgress: Float = -1f,
    modifier: Modifier = Modifier,
    directorProgressProvider: (() -> Float)? = null,
    tiltYawProvider: (() -> Float)? = null,
    tiltPitchProvider: (() -> Float)? = null
) {
    val motionConfig = LocalCosmicMotionConfig.current
    val flowPhaseState: State<Float>
    val ambientTimelineState: State<Float>
    val energyPulseState: State<Float>
    if (motionConfig.enableAmbientLoops) {
        val inf = rememberInfiniteTransition(label = "cinema_vfx_$sceneId")
        flowPhaseState = inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
            label = "flow"
        )
        ambientTimelineState = inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
            label = "ambient_timeline"
        )
        energyPulseState = inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1750, easing = LinearEasing), RepeatMode.Reverse),
            label = "energy"
        )
    } else {
        flowPhaseState = remember { mutableFloatStateOf(0.25f) }
        ambientTimelineState = remember { mutableFloatStateOf(0.35f) }
        energyPulseState = remember { mutableFloatStateOf(0.45f) }
    }

    val particles = remember(sceneId) {
        List(44) { idx ->
            Triple(
                Random(idx * 41 + 9).nextFloat(),
                Random(idx * 67 + 5).nextFloat(),
                0.55f + Random(idx * 23 + 3).nextFloat() * 0.95f
            )
        }
    }
    val reusableShaftPath = remember { Path() }
    val reusableBlueRibbon = remember { Path() }
    val reusableCrimsonRibbon = remember { Path() }

    fun resolveTimeline(): Float {
        val dir = directorProgressProvider?.invoke() ?: directorProgress
        return when {
            dir >= 0f -> dir.coerceIn(0f, 1f)
            motionConfig.enableAmbientLoops -> ambientTimelineState.value
            else -> 0.35f
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(0.dp))) {
        // Layer 1 (Depth 0.6x): High-Res Photorealistic Live-Action Movie Frame (Coil RGB_565 OOM-safe)
        val canvasContext = LocalContext.current
        val backdropRes = remember(sceneId) { sceneBackdropRes(sceneId) }
        val backdropRequest = remember(canvasContext, backdropRes) {
            ImageRequest.Builder(canvasContext)
                .data(backdropRes)
                .allowRgb565(true)
                .crossfade(300)
                .build()
        }
        AsyncImage(
            model = backdropRequest,
            contentDescription = sceneId,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val timeline = resolveTimeline()
                    val climaxIntensity = computeCinemaClimaxIntensity(timeline)
                    val directedZoom = computeCinemaDirectedZoom(timeline)
                    val directedPanX = sin(timeline * 2f * PI.toFloat()) * 9f
                    val directedPanY = cos(timeline * 2f * PI.toFloat()) * 4f
                    val microShake = if (motionConfig.enableAmbientLoops && climaxIntensity > 0.86f && (sceneId == "MANHATTAN_ARMADA" || sceneId == "BLUE_SKY_2006")) {
                        sin(flowPhaseState.value * 42f) * 3.2f
                    } else 0f
                    val yaw = tiltYawProvider?.invoke() ?: tiltYawDeg
                    val pitch = tiltPitchProvider?.invoke() ?: tiltPitchDeg
                    scaleX = directedZoom
                    scaleY = directedZoom
                    translationX = directedPanX + microShake - yaw * 1.65f
                    translationY = directedPanY - pitch * 1.25f
                    rotationY = yaw * 0.72f
                    rotationX = -pitch * 0.56f
                    cameraDistance = 14f * density
                }
        )

        // Layer 2 & 3 (3D Depth Volumetric Light Shafts + Midground 1.25x + Foreground 2.1x Parallax GPU VFX)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val flowPhase = flowPhaseState.value
            val energyPulse = energyPulseState.value
            val timeline = resolveTimeline()
            val climaxIntensity = computeCinemaClimaxIntensity(timeline)
            val yaw = tiltYawProvider?.invoke() ?: tiltYawDeg
            val pitch = tiltPitchProvider?.invoke() ?: tiltPitchDeg
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val midParallaxX = -yaw * 2.5f
            val midParallaxY = -pitch * 1.8f
            val fgParallaxX = -yaw * 4.4f
            val fgParallaxY = -pitch * 3.0f

            // Volumetric 3D diagonal light shafts sweeping across depth plane
            val shaftColor = when (sceneId) {
                "WHITE_EYE_AWAKENING" -> Color(0xFFE2E8F0)
                "BOSPHORUS_RESONANCE" -> Color(0xFFF43F5E)
                "MUTLAK_UYANIS" -> Color(0xFFFDE047)
                else -> Color(0xFF38BDF8)
            }
            for (s in 0..2) {
                val baseX = w * (0.18f + s * 0.30f) + sin(flowPhase * 6.28f + s * 1.7f) * w * 0.05f + midParallaxX * 0.8f
                reusableShaftPath.reset()
                reusableShaftPath.moveTo(baseX - w * 0.04f, 0f)
                reusableShaftPath.lineTo(baseX + w * 0.06f, 0f)
                reusableShaftPath.lineTo(baseX + w * 0.22f + fgParallaxX * 0.35f, h)
                reusableShaftPath.lineTo(baseX + w * 0.06f + fgParallaxX * 0.35f, h)
                reusableShaftPath.close()
                drawPath(
                    path = reusableShaftPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            shaftColor.copy(alpha = (0.08f + climaxIntensity * 0.14f)),
                            shaftColor.copy(alpha = (0.04f + climaxIntensity * 0.07f)),
                            Color.Transparent
                        )
                    )
                )
            }

            // 3D Perspective Depth Star/Debris Field rushing smoothly along Z-axis
            for (d in 0 until 18) {
                val seedX = ((d * 73 + 19) % 100) / 100f - 0.5f
                val seedY = ((d * 41 + 7) % 100) / 100f - 0.5f
                val z = 2.6f - ((flowPhase * 1.25f + d * 0.17f) % 2.2f)
                val projScale = (0.85f / z.coerceAtLeast(0.28f))
                val px = cx + seedX * w * projScale + fgParallaxX * (1.2f / z.coerceAtLeast(0.4f))
                val py = cy + seedY * h * projScale + fgParallaxY * (1.2f / z.coerceAtLeast(0.4f))
                if (px in -20f..(w + 20f) && py in -20f..(h + 20f)) {
                    val alpha = ((2.6f - z) / 2.2f).coerceIn(0f, 1f) * 0.58f
                    drawCircle(
                        color = shaftColor.copy(alpha = alpha),
                        radius = (1.1f + projScale * 1.35f).coerceAtMost(5.5f),
                        center = Offset(px, py)
                    )
                }
            }

            // Subtle storm rain across nocturnal Manhattan & Istanbul scenes
            if (sceneId != "MUTLAK_UYANIS") {
                val rainSpeedScale = if (sceneId == "WHITE_EYE_AWAKENING") (1f - climaxIntensity * 0.85f).coerceAtLeast(0.08f) else 1f
                particles.forEachIndexed { idx, (px, py, spd) ->
                    val prog = (py + flowPhase * spd * 1.85f * rainSpeedScale) % 1f
                    val parallaxMul = if (idx % 3 == 0) fgParallaxX else midParallaxX
                    val sx = ((((px - prog * 0.14f) + 1f) % 1f) * w + parallaxMul + w) % w
                    val sy = prog * h
                    drawLine(
                        color = Color(0x4CBADFFD),
                        start = Offset(sx, sy),
                        end = Offset(sx - 5f * rainSpeedScale, sy + 18f * rainSpeedScale + 3f),
                        strokeWidth = if (idx % 3 == 0) 1.7f else 1.15f
                    )
                }
            }

            when (sceneId) {
                "BLUE_SKY_2006" -> {
                    // Multi-branch procedural sky rift lightning + luminous falling rift embers + horizon flare
                    val boltAlpha = if (climaxIntensity > 0.72f || energyPulse > 0.82f) {
                        ((climaxIntensity * 0.7f) + (if (energyPulse > 0.82f) 0.45f else 0f)).coerceIn(0f, 1f)
                    } else 0f
                    if (boltAlpha > 0.05f) {
                        drawRect(Color(0xFF38BDF8).copy(alpha = boltAlpha * 0.22f))
                        drawBranchingLightning(
                            start = Offset(w * 0.52f + midParallaxX, h * 0.06f),
                            end = Offset(w * 0.48f + midParallaxX, h * 0.62f),
                            seed = ((flowPhase * 12).toInt() + 17),
                            glowColor = Color(0xFF38BDF8),
                            alpha = boltAlpha * 0.92f
                        )
                    }
                    // Horizontal anamorphic blue rift flare
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFF7DD3FC).copy(alpha = 0.24f * climaxIntensity), Color.Transparent)
                        ),
                        topLeft = Offset(0f, h * 0.26f + midParallaxY),
                        size = androidx.compose.ui.geometry.Size(w, h * 0.08f)
                    )
                    particles.take(26).forEachIndexed { idx, (px, py, spd) ->
                        val prog = (py + flowPhase * spd * 0.95f) % 1f
                        val sx = w * (0.20f + px * 0.60f) + sin(flowPhase * 6.28f + idx) * 14f + fgParallaxX
                        val sy = h * 0.10f + prog * h * 0.86f
                        drawCircle(
                            color = Color(0xFFBAE6FD).copy(alpha = (1f - prog) * 0.90f),
                            radius = 1.8f + (idx % 3) * 0.9f,
                            center = Offset(sx, sy)
                        )
                    }
                }

                "WHITE_EYE_AWAKENING" -> {
                    // Radial White-Eye desaturation/freeze pulse wave across Act 2-3 + telepathic rings
                    val eyeCenter = Offset(cx + midParallaxX * 0.6f, h * 0.37f + midParallaxY * 0.6f)
                    val freezeWaveRadius = w * (0.14f + climaxIntensity * 0.82f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color.White.copy(alpha = 0.36f * climaxIntensity),
                                Color(0xFFCBD5E1).copy(alpha = 0.24f * climaxIntensity),
                                Color(0xFF0F172A).copy(alpha = 0.26f * climaxIntensity),
                                Color.Transparent
                            ),
                            center = eyeCenter,
                            radius = freezeWaveRadius.coerceAtLeast(10f)
                        ),
                        radius = freezeWaveRadius,
                        center = eyeCenter
                    )
                    for (r in 0..3) {
                        val p = (flowPhase + r * 0.25f) % 1f
                        drawCircle(
                            color = Color(0xFFF8FAFC).copy(alpha = (1f - p) * (0.30f + climaxIntensity * 0.35f)),
                            radius = 18f + p * w * 0.56f,
                            center = eyeCenter,
                            style = Stroke(width = 1.6f + climaxIntensity * 1.4f)
                        )
                    }
                    // Suspended floating dust motes frozen in the telepathic void
                    particles.take(24).forEachIndexed { idx, (px, py, _) ->
                        val sx = px * w + fgParallaxX
                        val sy = py * h + sin(flowPhase * 6.28f + idx) * (4f * (1f - climaxIntensity * 0.7f))
                        drawCircle(
                            color = Color.White.copy(alpha = 0.35f + climaxIntensity * 0.45f),
                            radius = 1.5f + (idx % 3) * 0.7f,
                            center = Offset(sx, sy)
                        )
                    }
                }

                "BOSPHORUS_RESONANCE" -> {
                    // Counter-rotating twin blue (Aiden) & crimson (Mira) energy ribbons + vortex sparks
                    val vortexX = w * 0.54f + midParallaxX
                    reusableBlueRibbon.reset()
                    reusableCrimsonRibbon.reset()
                    val steps = 28
                    for (i in 0..steps) {
                        val t = i / steps.toFloat()
                        val y = h * (0.88f - t * 0.78f) + midParallaxY
                        val radius = (16f + sin(t * PI.toFloat()) * (44f + climaxIntensity * 32f))
                        val angA = t * 9.5f - flowPhase * 6.28f * 1.6f
                        val angB = -t * 9.5f + flowPhase * 6.28f * 1.6f + PI.toFloat()
                        val ax = vortexX + cos(angA) * radius
                        val bx = vortexX + cos(angB) * radius
                        if (i == 0) {
                            reusableBlueRibbon.moveTo(ax, y)
                            reusableCrimsonRibbon.moveTo(bx, y)
                        } else {
                            reusableBlueRibbon.lineTo(ax, y)
                            reusableCrimsonRibbon.lineTo(bx, y)
                        }
                    }
                    drawPath(reusableBlueRibbon, Color(0xFF38BDF8).copy(alpha = 0.35f + climaxIntensity * 0.45f), style = Stroke(width = 6f, cap = StrokeCap.Round))
                    drawPath(reusableBlueRibbon, Color(0xFFE0F2FE).copy(alpha = 0.55f + climaxIntensity * 0.35f), style = Stroke(width = 2f, cap = StrokeCap.Round))
                    drawPath(reusableCrimsonRibbon, Color(0xFFF43F5E).copy(alpha = 0.35f + climaxIntensity * 0.45f), style = Stroke(width = 6f, cap = StrokeCap.Round))
                    drawPath(reusableCrimsonRibbon, Color(0xFFFDA4AF).copy(alpha = 0.50f + climaxIntensity * 0.35f), style = Stroke(width = 2f, cap = StrokeCap.Round))

                    particles.take(26).forEachIndexed { idx, (px, py, spd) ->
                        val prog = (1f - ((py + flowPhase * spd) % 1f))
                        val angle = flowPhase * 6.28f * 1.8f + idx
                        val sx = vortexX + cos(angle) * (20f + px * 68f) + fgParallaxX * 0.4f
                        val sy = h * 0.12f + prog * h * 0.76f
                        val col = if (idx % 2 == 0) Color(0xFF38BDF8) else Color(0xFFF43F5E)
                        drawCircle(col.copy(alpha = 0.80f * sin(prog * PI.toFloat())), radius = 2.4f, center = Offset(sx, sy))
                    }
                }

                "MANHATTAN_ARMADA" -> {
                    // Dreadnought scanning halo + procedural branching sky lightning + dramatic flash
                    val scanCenter = Offset(w * 0.50f + midParallaxX, h * 0.34f + midParallaxY)
                    drawCircle(
                        color = Color(0xFF93C5FD).copy(alpha = 0.20f + climaxIntensity * 0.25f),
                        radius = w * (0.14f + energyPulse * 0.12f),
                        center = scanCenter,
                        style = Stroke(width = 2.2f)
                    )
                    val strikeAlpha = when {
                        climaxIntensity > 0.76f -> ((climaxIntensity - 0.76f) / 0.24f).coerceIn(0f, 1f)
                        energyPulse > 0.80f -> ((energyPulse - 0.80f) / 0.20f) * 0.85f
                        else -> 0f
                    }
                    if (strikeAlpha > 0.04f) {
                        drawRect(Color(0xFFBAE6FD).copy(alpha = strikeAlpha * 0.22f))
                        drawBranchingLightning(
                            start = Offset(w * 0.48f + midParallaxX, h * 0.22f),
                            end = Offset(w * 0.52f + midParallaxX, h * 0.78f),
                            seed = ((flowPhase * 10).toInt() + 91),
                            glowColor = Color(0xFF60A5FA),
                            alpha = strikeAlpha
                        )
                    }
                }

                else -> {
                    // MUTLAK_UYANIS: Raindrops decelerate to COMPLETE TIME-FREEZE mid-air with crystalline refraction glints!
                    val freezeFactor = climaxIntensity.coerceIn(0f, 1f) // 0 = falling rain, 1 = completely frozen crystal droplets
                    val dropSpeed = (1f - freezeFactor * 0.96f).coerceAtLeast(0.03f)
                    particles.forEachIndexed { idx, (px, py, spd) ->
                        val prog = (py + flowPhase * spd * 1.4f * dropSpeed) % 1f
                        val hoverJitter = sin(flowPhase * 6.28f + idx) * (3.2f * freezeFactor)
                        val sx = (px * w + (if (idx % 2 == 0) fgParallaxX else midParallaxX) + w) % w
                        val sy = prog * h + hoverJitter
                        val col = when {
                            idx % 4 == 0 -> Color(0xFFF43F5E)
                            idx % 3 == 0 -> Color(0xFFFDE047)
                            else -> Color(0xFF7DD3FC)
                        }
                        if (freezeFactor < 0.45f) {
                            // Still falling rain streak shortening as time slows
                            val tailLen = 18f * (1f - freezeFactor * 1.8f).coerceAtLeast(0.15f)
                            drawLine(
                                color = col.copy(alpha = 0.65f),
                                start = Offset(sx, sy),
                                end = Offset(sx - 3f, sy + tailLen),
                                strokeWidth = 1.6f
                            )
                        } else {
                            // Time-frozen glistening crystalline raindrop + cross specular glint
                            val r = 2.2f + (idx % 3) * 1.05f
                            drawCircle(col.copy(alpha = 0.55f + energyPulse * 0.42f), radius = r, center = Offset(sx, sy))
                            drawCircle(Color.White.copy(alpha = 0.75f * freezeFactor), radius = r * 0.42f, center = Offset(sx - r * 0.25f, sy - r * 0.25f))
                        }
                    }
                    // Dual blue & crimson cosmic aura + anamorphic flare at climax
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0xFF38BDF8).copy(alpha = 0.26f * climaxIntensity),
                                Color(0xFFF43F5E).copy(alpha = 0.14f * climaxIntensity),
                                Color.Transparent
                            ),
                            center = Offset(cx + midParallaxX, cy + midParallaxY),
                            radius = w * (0.30f + climaxIntensity * 0.18f)
                        ),
                        radius = w * (0.30f + climaxIntensity * 0.18f),
                        center = Offset(cx + midParallaxX, cy + midParallaxY)
                    )
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFFFDE047).copy(alpha = 0.20f * climaxIntensity), Color.Transparent)
                        ),
                        topLeft = Offset(0f, cy - h * 0.03f + midParallaxY),
                        size = androidx.compose.ui.geometry.Size(w, h * 0.06f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveActionFullscreenCinemaDialog(
    sceneId: String,
    caption: String,
    subtitle: String,
    accent: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val motionConfig = LocalCosmicMotionConfig.current
    var isMuted by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var fillEntireScreen by remember { mutableStateOf(true) }
    var gyroEnabled by remember { mutableStateOf(true) }
    var directorProgress by remember { mutableFloatStateOf(0f) }
    var tiltYaw by remember { mutableFloatStateOf(0f) }
    var tiltPitch by remember { mutableFloatStateOf(0f) }
    var lastHapticBeat by remember { mutableIntStateOf(-1) }

    val (gyroYaw, gyroPitch) = rememberGyroscopeTilt(enabled = gyroEnabled && motionConfig.enableAmbientLoops)
    val smoothYawState = animateFloatAsState(
        targetValue = (tiltYaw + if (gyroEnabled) gyroYaw * 0.85f else 0f).coerceIn(-36f, 36f),
        animationSpec = tween(140),
        label = "cinema_fps_yaw"
    )
    val smoothPitchState = animateFloatAsState(
        targetValue = (tiltPitch + if (gyroEnabled) gyroPitch * 0.70f else 0f).coerceIn(-14f, 14f),
        animationSpec = tween(140),
        label = "cinema_fps_pitch"
    )
    val beatLabels = remember(sceneId) { cinemaBeatLabels(sceneId) }
    val currentBeatIndex by remember {
        derivedStateOf { computeCinemaBeatIndex(directorProgress) }
    }
    var isLifecycleResumed by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    val soundEngine = remember(sceneId) {
        Cosmic3DSoundEngine(context, enableOrchestralMusic = true)
    }

    DisposableEffect(soundEngine, sceneId) {
        soundEngine.start(ambientSceneId = sceneId, ambientVolume = 0.64f)
        if (!isLifecycleResumed || isMuted) {
            soundEngine.setMuted(true)
        }
        onDispose { soundEngine.stopAndRelease() }
    }

    DisposableEffect(lifecycleOwner, soundEngine) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    isLifecycleResumed = false
                    soundEngine.setMuted(true)
                }
                Lifecycle.Event.ON_RESUME -> {
                    isLifecycleResumed = true
                    soundEngine.setMuted(isMuted)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 60fps 12-second 4-Act Director Timeline loop — pauses automatically on background!
    LaunchedEffect(isPlaying, sceneId, isLifecycleResumed) {
        if (!isPlaying || !isLifecycleResumed) return@LaunchedEffect
        var lastNanos = 0L
        while (isPlaying && isLifecycleResumed) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    directorProgress = advanceCinemaProgress(directorProgress, dt, 12f)
                }
                lastNanos = now
            }
        }
    }

    // Haptic climax pulse right as Act 3 (Kırılma Zirvesi) begins!
    LaunchedEffect(currentBeatIndex) {
        if (currentBeatIndex == 2 && lastHapticBeat != 2) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        lastHapticBeat = currentBeatIndex
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        ConfigureTrueFullscreenDialogWindow()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF04030D))
                .pointerInput(sceneId) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // True persistent First-Person 3D camera pan across all 7 cinematic animations!
                            tiltYaw = (tiltYaw - dragAmount.x * 0.14f).coerceIn(-34f, 34f)
                            tiltPitch = (tiltPitch - dragAmount.y * 0.08f).coerceIn(-12f, 12f)
                        }
                    )
                }
        ) {
            if (fillEntireScreen) {
                // TRUE FULL-BLEED EDGE-TO-EDGE FIRST-PERSON IMAX STAGE (fills 100% of phone screen!)
                LiveActionCinemaMotionCanvas(
                    sceneId = sceneId,
                    tiltYawDeg = 0f,
                    tiltPitchDeg = 0f,
                    directorProgress = -1f,
                    modifier = Modifier.fillMaxSize(),
                    directorProgressProvider = { directorProgress },
                    tiltYawProvider = { smoothYawState.value },
                    tiltPitchProvider = { smoothPitchState.value }
                )
            } else {
                // Wide 16:9 letterbox mode with ambient cinema aura (async Coil RGB_565 off main thread)
                val letterboxReq = remember(context, sceneId) {
                    ImageRequest.Builder(context)
                        .data(sceneBackdropRes(sceneId))
                        .allowRgb565(true)
                        .crossfade(200)
                        .build()
                }
                AsyncImage(
                    model = letterboxReq,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.24f; scaleX = 1.15f; scaleY = 1.15f }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xE603020A), Color(0x99060414), Color(0xF003020A))
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .aspectRatio(16f / 9f)
                        .graphicsLayer {
                            rotationY = smoothYawState.value * 0.45f
                            rotationX = -smoothPitchState.value * 0.45f
                            cameraDistance = 18f * density
                        }
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.3.dp, accent.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                ) {
                    LiveActionCinemaMotionCanvas(
                        sceneId = sceneId,
                        tiltYawDeg = 0f,
                        tiltPitchDeg = 0f,
                        directorProgress = -1f,
                        modifier = Modifier.fillMaxSize(),
                        directorProgressProvider = { directorProgress },
                        tiltYawProvider = { smoothYawState.value * 0.55f },
                        tiltPitchProvider = { smoothPitchState.value * 0.55f }
                    )
                }
            }

            // Soft Top & Bottom Cinema Readability Vignettes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .align(Alignment.TopCenter)
                    .background(Brush.verticalGradient(listOf(Color(0xD903020A), Color.Transparent)))
            )

            // Top Director Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xCC09061B))
                        .border(1.dp, accent.copy(alpha = 0.80f), RoundedCornerShape(50))
                        .clickable { tiltYaw = 0f; tiltPitch = 0f }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (abs(tiltYaw) > 4f) "↻ ${beatLabels[currentBeatIndex]}" else "✦ ${beatLabels[currentBeatIndex]}",
                        fontFamily = InterFontFamily,
                        color = accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (gyroEnabled) accent.copy(alpha = 0.25f) else Color(0xD9100926))
                            .border(1.dp, accent.copy(alpha = if (gyroEnabled) 0.90f else 0.45f), CircleShape)
                            .clickable { gyroEnabled = !gyroEnabled },
                        contentAlignment = Alignment.Center
                    ) { Text("🧭", fontSize = 14.sp) }

                    Box(
                        modifier = Modifier
                            .heightIn(min = 42.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xD9100926))
                            .border(1.dp, accent.copy(alpha = 0.72f), RoundedCornerShape(50))
                            .clickable { fillEntireScreen = !fillEntireScreen }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (fillEntireScreen) "16:9" else "⤢ TAM",
                            fontFamily = InterFontFamily,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xD9100926))
                            .border(1.dp, accent.copy(alpha = 0.8f), CircleShape)
                            .clickable {
                                isMuted = !isMuted
                                soundEngine.setMuted(isMuted)
                            },
                        contentAlignment = Alignment.Center
                    ) { Text(if (isMuted) "🔇" else "🔊", fontSize = 16.sp) }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xD9100926))
                            .border(1.dp, Color(0xAAFFFFFF), CircleShape)
                            .clickable { onDismiss() }
                            .testTag("cinema_fullscreen_close"),
                        contentAlignment = Alignment.Center
                    ) { Text("✕", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
            }

            // Bottom Director Timeline Scrubber + Replay + Caption (Generous bottom safe clearance!)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xF5070415))))
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 38.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 4-Act Interactive Timeline Scrubber + Play/Pause + Replay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC160C30))
                            .border(1.dp, accent.copy(alpha = 0.78f), CircleShape)
                            .clickable { isPlaying = !isPlaying }
                            .testTag("cinema_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPlaying) "⏸" else "▶",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (act in 0 until 4) {
                            val actStart = act * 0.25f
                            val fillFrac = ((directorProgress - actStart) / 0.25f).coerceIn(0f, 1f)
                            val isActiveAct = currentBeatIndex == act
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 36.dp)
                                    .clickable {
                                        directorProgress = actStart + 0.01f
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    .testTag("cinema_beat_segment_$act"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isActiveAct) 7.dp else 5.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0x44FFFFFF))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fillFrac)
                                            .height(if (isActiveAct) 7.dp else 5.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (act == 2) Color(0xFFFDE047) else accent)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC160C30))
                            .border(1.dp, Color(0x99BAE6FD), CircleShape)
                            .clickable {
                                directorProgress = 0f
                                isPlaying = true
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            .testTag("cinema_replay_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↺",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = caption,
                    fontFamily = DisplaySerifFontFamily,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        fontFamily = LiterarySerifFontFamily,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFFE0D6F8),
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

/**
 * Chapter 10/13/24 Mandatory Grounded Boss Duel Launch Card!
 * NO FLYING — 100% Canon Aiden Blackwood (Işınlanma, Drift Emilim, Beyaz Göz, Kırmızı Göz + Sesli Avengers Desteği).
 */
@Composable
fun Cosmic3DRiftMiniGame(
    title: String,
    subtitle: String,
    bossId: CosmicDuelBossId = CosmicDuelBossId.VAELOR_ROOFTOP,
    pietroSaved: Boolean = false,
    isCompleted: Boolean = false,
    savedBestRank: String? = null,
    onVictoryWithRank: ((score: Int, rank: String) -> Unit)? = null,
    onVictoryUnlocked: () -> Unit = {}
) {
    var isDuelOpen by remember { mutableStateOf(false) }
    var localWon by remember(isCompleted) { mutableStateOf(isCompleted) }
    var bestScore by remember { mutableIntStateOf(0) }
    var bestRank by remember(savedBestRank) { mutableStateOf(savedBestRank.orEmpty()) }

    val borderAccent = if (localWon) Color(0xFF34D399) else when (bossId) {
        CosmicDuelBossId.MIRROR_ZERO -> Color(0xFF38BDF8)
        CosmicDuelBossId.ULTRON_PRIME -> Color(0xFFEF4444)
        CosmicDuelBossId.VAELOR_ROOFTOP -> Color(0xFFF43F5E)
    }
    val enemyPortrait = when (bossId) {
        CosmicDuelBossId.MIRROR_ZERO -> CharacterPortraitKey.ZERO
        CosmicDuelBossId.ULTRON_PRIME -> CharacterPortraitKey.ULTRON
        CosmicDuelBossId.VAELOR_ROOFTOP -> CharacterPortraitKey.VAELOR
    }
    val previewSceneId = when (bossId) {
        CosmicDuelBossId.MIRROR_ZERO -> "BLUE_SKY_2006"
        CosmicDuelBossId.ULTRON_PRIME -> "MANHATTAN_ARMADA"
        CosmicDuelBossId.VAELOR_ROOFTOP -> "MUTLAK_UYANIS"
    }
    val wonMsg = when (bossId) {
        CosmicDuelBossId.MIRROR_ZERO ->
            "Kule terasında kendi karanlık geleceğin Sıfır'ı durdurdun! Aşağıdaki hikaye kararları artık açık; istersen Ayna Düellosu'nu tekrar oynayabilirsin."
        CosmicDuelBossId.ULTRON_PRIME ->
            "Sokovia meydanında Vibranyum Ultron Prime'ı parçaladın! 2. Sezon final kararları artık açık; istersen düelloyu tekrar oynayabilirsin."
        CosmicDuelBossId.VAELOR_ROOFTOP ->
            "Avengers Kulesi'nin çatısında Vaelor'u dize getirdin! Aşağıdaki hikaye kararları artık açık; istersen düelloyu tekrar oynayabilirsin."
    }
    val btnLabel = when (bossId) {
        CosmicDuelBossId.MIRROR_ZERO ->
            if (localWon) "⚔ AYNA DÜELLOSUNU TEKRAR OYNA ▶" else "⚔ AYNA DÜELLOSUNU BAŞLAT (ZORUNLU) ▶"
        CosmicDuelBossId.ULTRON_PRIME ->
            if (localWon) "⚔ ULTRON FİNALİNİ TEKRAR OYNA ▶" else "⚔ ULTRON PRİME DÜELLOSUNU BAŞLAT (ZORUNLU) ▶"
        CosmicDuelBossId.VAELOR_ROOFTOP ->
            if (localWon) "⚔ VAELOR ÇATI DÜELLOSUNU TEKRAR OYNA ▶" else "⚔ ÇATI DÜELLOSUNU BAŞLAT (ZORUNLU) ▶"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .morCamCosmicGlow(cornerRadius = 24.dp, glowColor = borderAccent, alpha = 0.54f, blurDp = 22.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xF7060516))
            .border(1.5.dp, borderAccent.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
    ) {
        // Header with Live-Action Aiden vs. Vaelor Portraits
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xEE180828), Color(0xEE0B1536))))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CinematicCharacterAvatar(CharacterPortraitKey.AIDEN, size = 42.dp, borderWidth = 1.4.dp, borderColor = Color(0xFF38BDF8))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (localWon) {
                        "✦ ZAFER KAZANILDI${if (bestRank.isNotBlank()) " · DERECE $bestRank" else ""} · KİLİT AÇILDI"
                    } else {
                        "🔒 ZORUNLU BOSS DÜELLOSU · İLERLEMEK İÇİN KAZAN"
                    },
                    fontFamily = InterFontFamily,
                    color = borderAccent,
                    fontSize = 9.2.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = title,
                    fontFamily = DisplaySerifFontFamily,
                    color = Color.White,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            CinematicCharacterAvatar(enemyPortrait, size = 42.dp, borderWidth = 1.4.dp, borderColor = borderAccent)
        }

        // Live-Action Boss Arena Cinema Preview (Aiden standing firmly grounded — NO FLYING!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            LiveActionCinemaMotionCanvas(
                sceneId = previewSceneId,
                tiltYawDeg = 0f,
                tiltPitchDeg = 0f,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color(0x33050412),
                            0.55f to Color.Transparent,
                            1.0f to Color(0xEE070516)
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PowerPreviewChip("⚡ IŞINLANMA", Color(0xFF38BDF8), Modifier.weight(1f))
                    PowerPreviewChip("🛡 DRİFT EMİLİM", Color(0xFF60A5FA), Modifier.weight(1f))
                    PowerPreviewChip("👁 BEYAZ GÖZ", Color(0xFFF8FAFC), Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PowerPreviewChip("🔴 KIRMIZI GÖZ YIKIMI", Color(0xFFF43F5E), Modifier.weight(1f))
                    PowerPreviewChip("🎙 SESLİ AVENGERS DESTEĞİ", Color(0xFFFDE047), Modifier.weight(1f))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xF20A071D))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (localWon) wonMsg else subtitle,
                fontFamily = InterFontFamily,
                color = Color(0xFFDCD3F5),
                fontSize = 12.2.sp,
                lineHeight = 18.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (localWon) listOf(Color(0xFF059669), Color(0xFF2563EB))
                            else listOf(Color(0xFF2563EB), Color(0xFFDC2626))
                        )
                    )
                    .border(1.2.dp, Color(0xCCBAE6FD), RoundedCornerShape(16.dp))
                    .clickable { isDuelOpen = true }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = btnLabel,
                    fontFamily = InterFontFamily,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }

    if (isDuelOpen) {
        CosmicRooftopBossDuelDialog(
            title = title,
            bossId = bossId,
            pietroSaved = pietroSaved,
            onVictory = { finalScore, rank ->
                localWon = true
                if (finalScore >= bestScore) {
                    bestScore = finalScore
                    bestRank = rank
                }
                onVictoryWithRank?.invoke(finalScore, rank)
                onVictoryUnlocked()
            },
            onDismiss = { isDuelOpen = false }
        )
    }
}

@Composable
private fun PowerPreviewChip(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xD80A071E))
            .border(0.85.dp, accent.copy(alpha = 0.70f), RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            color = accent,
            fontSize = 8.8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Full-Screen 4-Cut Interactive Marvel-Style Post-Credits Cinematic — Fallen Timeline Twin Aiden!
 *
 * Cut 0: Dark theater intro card ("1. SEZON FİNALİ TAMAMLANDI · KIRIK ZAMAN ÇİZGİSİ")
 * Cut 1: Ruined parallel Manhattan (half-collapsed Avengers Tower, broken Captain America shield in wet ash, Two Pale Moons)
 * Cut 2: Bass-drop hard-cut face reveal of Scarred Twin Aiden reaching through the crackling blue rift (with haunting spoken line!)
 * Cut 3: Interactive Dimensional Rift Climax — player touches the pulsing blue rift -> blinding shockwave ->
 *        "KOZMİK SÜRÜKLENİŞ · 2. SEZON: ASIL MİMAR" title slam -> transitions directly into Chapter Choices Summary!
 */
@Composable
fun TwinAidenPostCreditsDialog(
    onContinueToChoicesSummary: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var cutIndex by remember { mutableIntStateOf(0) }
    var riftFlashAlpha by remember { mutableFloatStateOf(0f) }
    var twinWalkProgress by remember { mutableFloatStateOf(0f) }
    var twinLastStepCount by remember { mutableIntStateOf(0) }
    var twinIsSpeaking by remember { mutableStateOf(false) }
    var twinMouthOpen by remember { mutableFloatStateOf(0f) }

    val sfxEngine = remember { Cosmic3DSoundEngine(context, enableOrchestralMusic = false) }
    val voiceEngine = remember(sfxEngine) {
        CosmicCharacterVoiceEngine(context) { speaking ->
            twinIsSpeaking = speaking
            sfxEngine.setVoiceDucking(speaking)
        }
    }

    val line1Text = "Sen kazandın Aiden... benim yapamadığımı başardın. Avengers'ı kurtardın. Ama Vaelor sadece bir habercidir — Asıl Mimar seni buldu... ve yakında demir fırtına başlayacak."
    val line2Text = "Kapıyı aç... zaman kırılmadan önce sana onları nasıl hayatta tutacağını göstereyim."

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, sfxEngine, voiceEngine) {
        sfxEngine.start(ambientSceneId = "BLUE_SKY_2006", ambientVolume = 0.50f)
        voiceEngine.prewarmLines(
            listOf(
                Triple("TWIN_AIDEN", line1Text, "voice_twin_aiden_1"),
                Triple("TWIN_AIDEN", line2Text, "voice_twin_aiden_2")
            )
        )
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    voiceEngine.stopCurrentLine()
                    sfxEngine.setMuted(true)
                }
                Lifecycle.Event.ON_RESUME -> sfxEngine.setMuted(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sfxEngine.stopAndRelease()
            voiceEngine.stopAndRelease()
        }
    }

    // Real-time audio-synced lip-sync for Twin Aiden in Cuts 2 & 3!
    LaunchedEffect(twinIsSpeaking) {
        if (!twinIsSpeaking) {
            twinMouthOpen = 0f
            return@LaunchedEffect
        }
        var smooth = 0f
        while (twinIsSpeaking) {
            withFrameNanos {
                val target = voiceEngine.currentSpeechAmplitude()
                smooth += (target - smooth) * (if (target > smooth) 0.45f else 0.25f)
                twinMouthOpen = smooth.coerceIn(0f, 1f)
            }
        }
        twinMouthOpen = 0f
    }

    // 60fps Slow Deliberate Walking Approach for Twin Aiden in Cut 2 (7 slow heavy footsteps out of the misty rift!)
    LaunchedEffect(cutIndex) {
        if (cutIndex == 2) {
            twinWalkProgress = 0f
            twinLastStepCount = 0
            var lastNs = 0L
            while (cutIndex == 2 && twinWalkProgress < 1f) {
                withFrameNanos { now ->
                    if (lastNs == 0L) {
                        lastNs = now
                        return@withFrameNanos
                    }
                    val dt = ((now - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    lastNs = now
                    twinWalkProgress = (twinWalkProgress + dt / 7.8f).coerceAtMost(1f)
                    val stepNum = (twinWalkProgress * 7f).toInt()
                    if (stepNum > twinLastStepCount && stepNum <= 7) {
                        twinLastStepCount = stepNum
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        sfxEngine.play(CosmicSfx.PLAYER_SHIELD_HIT, volume = 0.24f, affectedByTimeSlow = false)
                    }
                }
            }
        } else if (cutIndex == 3) {
            twinWalkProgress = 1f
        } else {
            twinWalkProgress = 0f
        }
    }

    // Automatic cinema pacing — waits gracefully for voice line completion instead of cutting off mid-sentence
    LaunchedEffect(cutIndex) {
        when (cutIndex) {
            0 -> {
                sfxEngine.play(CosmicSfx.TIME_SLOW_ENTER, volume = 0.72f)
                delay(2400L)
                if (cutIndex == 0) cutIndex = 1
            }
            1 -> {
                sfxEngine.play(CosmicSfx.LASER_WARNING, volume = 0.48f)
                delay(4800L)
                if (cutIndex == 1) cutIndex = 2
            }
            2 -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                sfxEngine.play(CosmicSfx.WHITE_EYE_NOVA, volume = 0.90f)
                voiceEngine.speak(
                    speakerKey = "TWIN_AIDEN",
                    text = line1Text,
                    lineId = "voice_twin_aiden_1"
                )
                delay(8400L)
                // Wait up to 6 extra seconds if Twin Aiden is still finishing his spoken sentence
                var extraWaited = 0L
                while (cutIndex == 2 && twinIsSpeaking && extraWaited < 6000L) {
                    delay(200L)
                    extraWaited += 200L
                }
                delay(450L)
                if (cutIndex == 2) cutIndex = 3
            }
            3 -> {
                voiceEngine.speak(
                    speakerKey = "TWIN_AIDEN",
                    text = line2Text,
                    lineId = "voice_twin_aiden_2"
                )
            }
            4 -> {
                voiceEngine.stopCurrentLine()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                sfxEngine.play(CosmicSfx.DASH_BLINK, volume = 0.95f)
                sfxEngine.play(CosmicSfx.VICTORY_FANFARE, volume = 0.85f)
                riftFlashAlpha = 1f
                repeat(20) {
                    delay(35L)
                    riftFlashAlpha = (riftFlashAlpha - 0.055f).coerceAtLeast(0f)
                }
            }
        }
    }

    val pcMotionConfig = LocalCosmicMotionConfig.current
    val kenBurnsState: State<Float>
    val rainFlowState: State<Float>
    val pulseState: State<Float>
    if (pcMotionConfig.enableAmbientLoops) {
        val inf = rememberInfiniteTransition(label = "postcredits_motion")
        kenBurnsState = inf.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
            label = "kb"
        )
        rainFlowState = inf.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing), RepeatMode.Restart),
            label = "rf"
        )
        pulseState = inf.animateFloat(
            initialValue = 0.55f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
            label = "pl"
        )
    } else {
        kenBurnsState = remember { mutableFloatStateOf(0.5f) }
        rainFlowState = remember { mutableFloatStateOf(0.25f) }
        pulseState = remember { mutableFloatStateOf(0.82f) }
    }

    val ashParticles = remember {
        List(36) { i ->
            Triple(Random(i * 43 + 7).nextFloat(), Random(i * 61 + 3).nextFloat(), 0.6f + Random(i * 17).nextFloat() * 0.9f)
        }
    }

    Dialog(
        onDismissRequest = onContinueToChoicesSummary,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        ConfigureTrueFullscreenDialogWindow()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF010207))
        ) {
            // LAYER 1: FULLSCREEN LIVE-ACTION CINEMA CUTS WITH TWIN AIDEN SLOW WALKING APPROACH!
            Crossfade(
                targetState = cutIndex,
                animationSpec = tween(750),
                label = "postcredits_cut"
            ) { activeCut ->
                when (activeCut) {
                    0 -> {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF02030A)))
                    }
                    1 -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_postcredits_ruined_nyc),
                            contentDescription = "Yıkılmış Paralel Manhattan",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val kb = kenBurnsState.value
                                    val sc = 1.08f + kb * 0.12f
                                    scaleX = sc; scaleY = sc
                                    translationX = (kb - 0.5f) * 32f
                                }
                        )
                    }
                    2, 3 -> {
                        // CUT 2 & 3: Twin Aiden INSTANTLY VISIBLE & SLOWLY WALKING FORWARD out of the ruined Manhattan rift!
                        val walkPhase = twinWalkProgress * 7f * PI.toFloat()
                        val isWalkingNow = activeCut == 2 && twinWalkProgress < 0.995f
                        val shoulderRollDeg = if (isWalkingNow) sin(walkPhase) * 2.0f else 0f
                        val approachRiseY = if (activeCut == 2) (1f - twinWalkProgress) * 36f else 0f

                        Box(modifier = Modifier.fillMaxSize()) {
                            // Synchronous Painter guarantees Twin Aiden NEVER flashes blank or gets stuck behind ruined NYC!
                            val twinPainter = painterResource(id = R.drawable.img_postcredits_twin_aiden)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        val kb = kenBurnsState.value
                                        val strideBobY = if (isWalkingNow) -abs(sin(walkPhase)) * 16f else sin(kb * 6.28f) * 3f
                                        val lateralWeightSway = if (isWalkingNow) sin(walkPhase) * 10f else -(kb - 0.5f) * 8f
                                        val approachScale = if (activeCut == 3) {
                                            1.18f + kb * 0.04f
                                        } else {
                                            0.98f + twinWalkProgress * 0.22f
                                        }
                                        scaleX = approachScale
                                        scaleY = approachScale
                                        translationX = lateralWeightSway
                                        translationY = approachRiseY + strideBobY
                                        rotationZ = shoulderRollDeg
                                        rotationY = if (isWalkingNow) sin(walkPhase) * 3.2f else 0f
                                        cameraDistance = 14f * density
                                        alpha = (0.88f + twinWalkProgress * 0.12f).coerceIn(0f, 1f)
                                    }
                            ) {
                                // BiasAlignment(0.06f, -0.08f) keeps Twin Aiden's scarred face, glowing blue eyes,
                                // AND his crackling blue rift hand heroically framed on narrow portrait phones!
                                Image(
                                    painter = twinPainter,
                                    contentDescription = "Yara İzli İkiz Aiden",
                                    contentScale = ContentScale.Crop,
                                    alignment = BiasAlignment(0.06f, -0.08f),
                                    modifier = Modifier.fillMaxSize()
                                )
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val src = twinPainter.intrinsicSize
                                    val imgW = if (src.width.isFinite() && src.width > 1f) src.width else w
                                    val imgH = if (src.height.isFinite() && src.height > 1f) src.height else h
                                    val cropScale = maxOf(w / imgW, h / imgH)
                                    val drawnW = imgW * cropScale
                                    val drawnH = imgH * cropScale
                                    // Match BiasAlignment(0.06f, -0.08f) exact pixel projection
                                    val offX = (w - drawnW) * (0.5f + 0.06f * 0.5f)
                                    val offY = (h - drawnH) * (0.5f - 0.08f * 0.5f)
                                    fun px(nx: Float) = offX + nx * drawnW
                                    fun py(ny: Float) = offY + ny * drawnH

                                    val eyeIntensity = (0.48f + twinWalkProgress * 0.52f) * pulseState.value
                                    val lx = px(0.460f)
                                    val rx = px(0.534f)
                                    val ey = py(0.328f)
                                    val eyeR = (rx - lx) * 0.25f

                                    listOf(lx, rx).forEach { ex ->
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                listOf(
                                                    Color(0xFFE0F2FE).copy(alpha = 0.68f * eyeIntensity),
                                                    Color(0xFF38BDF8).copy(alpha = 0.30f * eyeIntensity),
                                                    Color.Transparent
                                                ),
                                                center = Offset(ex, ey),
                                                radius = eyeR
                                            ),
                                            radius = eyeR,
                                            center = Offset(ex, ey)
                                        )
                                    }

                                    // Real-time audio-synced lip-sync on Twin Aiden when he speaks!
                                    if (twinMouthOpen > 0.04f) {
                                        val mx = px(0.495f)
                                        val my = py(0.488f)
                                        val mw = drawnW * 0.056f * (0.88f + twinMouthOpen * 0.16f)
                                        val mh = (rx - lx) * 0.19f * twinMouthOpen
                                        drawOval(
                                            color = Color(0xE011070C).copy(alpha = 0.85f * twinMouthOpen),
                                            topLeft = Offset(mx - mw * 0.44f, my - mh * 0.38f),
                                            size = androidx.compose.ui.geometry.Size(mw * 0.88f, mh.coerceAtLeast(2f))
                                        )
                                    }
                                }
                            }

                            // Screen-space dark mist vignette & heavy bootstep blue shock-dust
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val lateralWeightSway = if (isWalkingNow) sin(walkPhase) * 10f else -(kenBurnsState.value - 0.5f) * 8f
                                val cx = w * 0.50f + lateralWeightSway * 0.65f

                                val fogAlpha = ((1f - twinWalkProgress) * 0.30f).coerceIn(0f, 0.32f)
                                if (fogAlpha > 0.02f) {
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            listOf(Color(0xFF040B1A).copy(alpha = fogAlpha), Color.Transparent, Color(0xFF020612).copy(alpha = fogAlpha * 1.2f))
                                        )
                                    )
                                }

                                val stepImpact = (1f - abs(sin(walkPhase))).coerceIn(0f, 1f)
                                if (isWalkingNow && stepImpact > 0.72f) {
                                    val dustA = ((stepImpact - 0.72f) / 0.28f) * 0.26f
                                    drawOval(
                                        brush = Brush.radialGradient(
                                            listOf(Color(0xFF38BDF8).copy(alpha = dustA), Color.Transparent),
                                            center = Offset(cx, h * 0.86f),
                                            radius = w * 0.42f
                                        ),
                                        topLeft = Offset(cx - w * 0.42f, h * 0.78f),
                                        size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.16f)
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_postcredits_ruined_nyc),
                            contentDescription = "2. Sezon",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = 0.42f; scaleX = 1.12f; scaleY = 1.12f }
                        )
                    }
                }
            }

            // LAYER 2: LIVE 60FPS 3D DEPTH-RUSHING ASH EMBERS, STORM RAIN & CRACKLING RIFT LIGHTNING
            if (cutIndex in 1..4) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rainFlow = rainFlowState.value
                    val pulse = pulseState.value
                    val w = size.width
                    val h = size.height
                    ashParticles.forEachIndexed { idx, (px, py, spd) ->
                        if (cutIndex == 1) {
                            val prog = (1f - ((py + rainFlow * spd * 0.8f) % 1f))
                            val sx = px * w + sin(rainFlow * 6.28f + idx) * 16f
                            drawCircle(Color(0xFFFB923C).copy(alpha = 0.72f), radius = 2.2f + (idx % 3), center = Offset(sx, prog * h))
                        } else {
                            // 3D outward-rushing blue sparks & drifting ash sweeping past the camera as Twin Aiden walks forward
                            val z = 2.2f - ((rainFlow * spd * 0.95f + idx * 0.08f) % 1.9f)
                            val pScale = (0.75f / z.coerceAtLeast(0.25f))
                            val sx = w * 0.5f + (px - 0.5f) * w * pScale * 1.1f
                            val sy = h * 0.5f + (py - 0.5f) * h * pScale * 1.1f
                            val alpha = ((2.2f - z) / 1.9f).coerceIn(0f, 1f) * 0.82f * pulse
                            drawCircle(
                                color = if (idx % 4 == 0) Color(0xFFFB923C).copy(alpha = alpha * 0.7f) else Color(0xFF7DD3FC).copy(alpha = alpha),
                                radius = (1.6f + pScale * 1.6f).coerceAtMost(5.8f),
                                center = Offset(sx, sy)
                            )
                        }
                        val rProg = (py + rainFlow * spd * 2.0f) % 1f
                        val rx = ((px - rProg * 0.14f) + 1f) % 1f * w
                        drawLine(Color(0x36BAE6FD), Offset(rx, rProg * h), Offset(rx - 5f, rProg * h + 18f), strokeWidth = 1.2f)
                    }
                    // Periodic cracking blue dimensional lightning behind Twin Aiden
                    if ((cutIndex == 2 || cutIndex == 3) && pulse > 0.86f) {
                        drawBranchingLightning(
                            start = Offset(w * 0.24f, h * 0.08f),
                            end = Offset(w * 0.34f, h * 0.62f),
                            seed = (rainFlow * 20).toInt() + 5,
                            glowColor = Color(0xFF38BDF8),
                            alpha = ((pulse - 0.86f) / 0.14f) * 0.75f
                        )
                    }
                }
            }

            // Cinema Letterbox Dark Gradients Top & Bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .align(Alignment.TopCenter)
                    .background(Brush.verticalGradient(listOf(Color(0xEB010207), Color.Transparent)))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDC02030A), Color(0xFA010207))))
            )

            // TOP BAR: Scene Location Tag + Manual Advance / Skip Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xCC070D20))
                        .border(0.9.dp, Color(0x9938BDF8), RoundedCornerShape(50))
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = when (cutIndex) {
                            0 -> "✦ JENERİK SONRASI SAHNE"
                            1 -> "KARANLIK GELECEK · HARABE MANHATTAN"
                            2, 3 -> "KIRIK ZAMAN ÇİZGİSİ · YARA İZLİ AIDEN"
                            else -> "KOZMİK SÜRÜKLENİŞ · 2. SEZON"
                        },
                        fontFamily = InterFontFamily,
                        color = Color(0xFF7DD3FC),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.9.sp
                    )
                }

                if (cutIndex < 3) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xB80E172A))
                            .border(0.8.dp, Color(0x77FFFFFF), RoundedCornerShape(50))
                            .clickable { cutIndex += 1 }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text("İLERİ ›", fontFamily = InterFontFamily, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // CENTER DRAMATIC CONTENT (Cut 0 Title, Cut 3 Interactive Rift Touch, Cut 4 Season 2 Logo Slam!)
            when (cutIndex) {
                0 -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "1. SEZON FİNALİ TAMAMLANDI",
                            fontFamily = InterFontFamily,
                            color = Color(0xFF7DD3FC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.4.sp
                        )
                        Text(
                            text = "Ama zaman çizgisi tek değildi...",
                            fontFamily = DisplaySerifFontFamily,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                3 -> {
                    // INTERACTIVE DIMENSIONAL RIFT TOUCH BUTTON RIGHT IN CENTER!
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(132.dp)
                            .morCamCosmicGlow(cornerRadius = 66.dp, glowColor = Color(0xFF38BDF8), blurDp = 28.dp, alphaProvider = { 0.72f * pulseState.value })
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xCCBAE6FD), Color(0xAA0284C7), Color(0x66060D24))
                                )
                            )
                            .border(2.2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                            .clickable { cutIndex = 4 },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✦", color = Color.White, fontSize = 26.sp)
                            Text(
                                text = "YARIĞA\nDOKUN",
                                fontFamily = InterFontFamily,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                4 -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "KOZMİK SÜRÜKLENİŞ",
                            fontFamily = DisplaySerifFontFamily,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xCC1E3A8A))
                                .border(1.2.dp, Color(0xFF7DD3FC), RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "2. SEZON · DEMİR VE YANKI — YAKINDA",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFFDE047),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.4.sp
                            )
                        }
                        Text(
                            text = "Karanlık gelecekteki yara izli Aiden kapını çaldı — çoklu evrende sadece bir tane Aiden varken, bu nasıl mümkün olabilir? Şimdi 1. Sezonun tüm kararlarına göz at.",
                            fontFamily = LiterarySerifFontFamily,
                            color = Color(0xFFD8D0F2),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // BOTTOM CINEMA SUBTITLES & FINAL CTA BUTTON (Safe clearance above Android gesture bar!)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (cutIndex) {
                    1 -> {
                        Text(
                            text = "Yıkılmış bir Avengers Kulesi'nin ve küllerin içine gömülmüş kırık bir Kaptan Amerika kalkanının üzerinde iki soluk ay parlıyordu...",
                            fontFamily = LiterarySerifFontFamily,
                            fontStyle = FontStyle.Italic,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    2 -> {
                        Text(
                            text = "YARA İZLİ AIDEN · KARANLIK GELECEK",
                            fontFamily = InterFontFamily,
                            color = Color(0xFF38BDF8),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.1.sp
                        )
                        Text(
                            text = "“Sen kazandın Aiden... benim yapamadığımı başardın. Avengers'ı kurtardın. Ama Vaelor sadece bir habercidir — Asıl Mimar seni buldu... ve yakında demir fırtına başlayacak.”",
                            fontFamily = LiterarySerifFontFamily,
                            fontStyle = FontStyle.Italic,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    3 -> {
                        Text(
                            text = "“Kapıyı aç... zaman kırılmadan önce sana onları nasıl hayatta tutacağını göstereyim.”",
                            fontFamily = LiterarySerifFontFamily,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFFBAE6FD),
                            fontSize = 16.sp,
                            lineHeight = 23.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    4 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED))))
                                .border(1.3.dp, Color(0xFFBAE6FD), RoundedCornerShape(16.dp))
                                .clickable { onContinueToChoicesSummary() }
                                .padding(vertical = 15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SEÇİMLERİMİ & SEZON ÖZETİNİ GÖR ✦ ▶",
                                fontFamily = InterFontFamily,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }
                }
            }

            // Blinding Electric-Blue Dimensional Shockwave Flash when touching the Rift!
            if (riftFlashAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFBAE6FD).copy(alpha = riftFlashAlpha))
                )
            }
        }
    }
}

/**
 * Full-Screen 4-Cut Interactive Marvel-Style Season 2 Finale Post-Credits Cinematic!
 * Shown at the end of Chapter 20 BEFORE ChapterSummaryModal.
 *
 * Cut 0: Dark theater intro ("2. SEZON FİNALİ TAMAMLANDI · SOKOVİA ENKAZI")
 * Cut 1: Ruined Sokovia ash & flickering broken Ultron drone eye
 * Cut 2: Eye shifts from crimson to GOLD-BLUE — ASIL MİMAR speaks directly through the dying machine!
 * Cut 3: Interactive Dossier Slam — player taps "✦ DOSYAYI AÇ" to unveil SOKOVIA ANLAŞMALARI (Season 3 Civil War hook!)
 * Cut 4: "KOZMİK SÜRÜKLENİŞ · 3. SEZON: İÇ SAVAŞ VE MİMAR" title card -> transitions into Chapter 20 Choices Summary!
 */
@Composable
fun Season2ArchitectPostCreditsDialog(
    onContinueToChoicesSummary: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sfxEngine = remember { Cosmic3DSoundEngine(context, enableOrchestralMusic = false) }
    var architectIsSpeaking by remember { mutableStateOf(false) }
    var architectVoiceAmp by remember { mutableFloatStateOf(0f) }
    val voiceEngine = remember(sfxEngine) {
        CosmicCharacterVoiceEngine(context) { speaking ->
            architectIsSpeaking = speaking
            sfxEngine.setVoiceDucking(speaking)
        }
    }

    var cutIndex by remember { mutableIntStateOf(0) }
    var flashAlpha by remember { mutableFloatStateOf(0f) }

    val line1 = "Birinci perde kapandı, Kayıp Anahtarım... Demir kuklayı kırdın, ama insanların korkusunu kıramazsın."
    val line2 = "Şimdi seni koruyan kahramanlar bile birbirine karşı duracak. Masadaki dosyayı aç... üçüncü perde başladı."

    LaunchedEffect(architectIsSpeaking) {
        if (!architectIsSpeaking) {
            architectVoiceAmp = 0f
            return@LaunchedEffect
        }
        var smoothed = 0f
        while (architectIsSpeaking) {
            withFrameNanos {
                val target = voiceEngine.currentSpeechAmplitude()
                val blend = if (target > smoothed) 0.50f else 0.25f
                smoothed += (target - smoothed) * blend
                architectVoiceAmp = smoothed.coerceIn(0f, 1f)
            }
        }
        architectVoiceAmp = 0f
    }

    val s2LifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(s2LifecycleOwner, sfxEngine, voiceEngine) {
        sfxEngine.start(ambientSceneId = "BLUE_SKY_2006", ambientVolume = 0.45f)
        voiceEngine.prewarmLines(
            listOf(
                Triple("ARCHITECT", line1, "voice_s2_postcredits_1"),
                Triple("ARCHITECT", line2, "voice_s2_postcredits_2")
            )
        )
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    voiceEngine.stopCurrentLine()
                    sfxEngine.setMuted(true)
                }
                Lifecycle.Event.ON_RESUME -> sfxEngine.setMuted(false)
                else -> Unit
            }
        }
        s2LifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            s2LifecycleOwner.lifecycle.removeObserver(observer)
            sfxEngine.stopAndRelease()
            voiceEngine.stopAndRelease()
        }
    }

    LaunchedEffect(cutIndex) {
        when (cutIndex) {
            0 -> {
                sfxEngine.play(CosmicSfx.TIME_SLOW_ENTER, volume = 0.72f)
                delay(2300L)
                if (cutIndex == 0) cutIndex = 1
            }
            1 -> {
                sfxEngine.play(CosmicSfx.LASER_WARNING, volume = 0.50f)
                delay(4600L)
                if (cutIndex == 1) cutIndex = 2
            }
            2 -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                sfxEngine.play(CosmicSfx.WHITE_EYE_NOVA, volume = 0.88f)
                voiceEngine.speak("ARCHITECT", line1, "voice_s2_postcredits_1")
                delay(8200L)
                // Never cut off the Architect mid-sentence: wait up to 6s extra until his line finishes
                var extraWait = 0L
                while (architectIsSpeaking && extraWait < 6000L && cutIndex == 2) {
                    delay(200L)
                    extraWait += 200L
                }
                if (cutIndex == 2) cutIndex = 3
            }
            3 -> {
                voiceEngine.speak("ARCHITECT", line2, "voice_s2_postcredits_2")
            }
            4 -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                sfxEngine.play(CosmicSfx.VICTORY_FANFARE, volume = 0.85f)
                flashAlpha = 0.92f
                repeat(18) {
                    delay(35L)
                    flashAlpha = (flashAlpha - 0.055f).coerceAtLeast(0f)
                }
            }
        }
    }

    val s2MotionConfig = LocalCosmicMotionConfig.current
    val pulseState: State<Float>
    val kenBurnsState: State<Float>
    val emberPhaseState: State<Float>
    if (s2MotionConfig.enableAmbientLoops) {
        val inf = rememberInfiniteTransition(label = "s2_pc_motion")
        pulseState = inf.animateFloat(
            initialValue = 0.55f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
            label = "s2_pl"
        )
        kenBurnsState = inf.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(8500, easing = LinearEasing), RepeatMode.Reverse),
            label = "s2_kb"
        )
        emberPhaseState = inf.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(6400, easing = LinearEasing), RepeatMode.Restart),
            label = "s2_ember"
        )
    } else {
        pulseState = remember { mutableFloatStateOf(0.82f) }
        kenBurnsState = remember { mutableFloatStateOf(0.5f) }
        emberPhaseState = remember { mutableFloatStateOf(0.25f) }
    }

    Dialog(
        onDismissRequest = onContinueToChoicesSummary,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        ConfigureTrueFullscreenDialogWindow()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF020309))
        ) {
            // Full-Bleed Dynamic Backdrop per Cut:
            // Cut 1 -> Shattered Ultron Prime in Sokovia Ash
            // Cut 2 & 3 -> Full-Bleed Asıl Mimar (Golden-Blue Eyes) with slow dramatic dolly zoom!
            // Cut 4 -> Ruined Sokovia Crater behind Classified UN Accords Dossier
            if (cutIndex >= 1) {
                val activeBackdropRes = when (cutIndex) {
                    1 -> R.drawable.img_portrait_ultron_prime_1795557413989
                    2, 3 -> R.drawable.img_portrait_architect_hero_1795559252160
                    else -> R.drawable.img_postcredits_ruined_nyc
                }
                Image(
                    painter = painterResource(id = activeBackdropRes),
                    contentDescription = when (cutIndex) {
                        1 -> "Sokovia Enkazında Ultron Prime"
                        2, 3 -> "Asıl Mimar"
                        else -> "Sokovia Enkazı"
                    },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val kb = kenBurnsState.value
                            val sc = when (cutIndex) {
                                1 -> 1.06f + kb * 0.08f
                                2, 3 -> 1.04f + kb * 0.09f + architectVoiceAmp * 0.025f
                                else -> 1.10f + kb * 0.08f
                            }
                            scaleX = sc; scaleY = sc
                            translationY = if (cutIndex == 2 || cutIndex == 3) -kb * 16f else 0f
                            alpha = when (cutIndex) {
                                1 -> 0.84f
                                2, 3 -> 0.92f
                                else -> 0.38f
                            }
                        }
                )
                // Cinema letterbox vignette so subtitle & buttons stay crystal legible
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xAA020309),
                                    Color(0x28020309),
                                    Color(0x66020309),
                                    Color(0xEE020309)
                                )
                            )
                        )
                )
            }

            // Cut 1: Crimson-to-Gold Visor Glitch Pulse | Cut 2 & 3: Audio-Synced Celestial Rings & Golden Embers!
            if (cutIndex == 1 || cutIndex == 2 || cutIndex == 3) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pulse = pulseState.value
                    val emberPhase = emberPhaseState.value
                    val cx = size.width / 2f
                    val cy = size.height * 0.36f
                    if (cutIndex == 1) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFFEF4444).copy(alpha = 0.34f * pulse),
                                    Color(0xFFFDE047).copy(alpha = 0.18f * (1f - pulse)),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = size.minDimension * 0.44f
                            ),
                            radius = size.minDimension * 0.44f,
                            center = Offset(cx, cy)
                        )
                    } else {
                        val voiceBoost = architectVoiceAmp
                        val haloR = size.minDimension * (0.34f + voiceBoost * 0.14f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFFFEF08A).copy(alpha = 0.24f + voiceBoost * 0.28f),
                                    Color(0xFF38BDF8).copy(alpha = 0.16f + voiceBoost * 0.20f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = haloR * 1.45f
                            ),
                            radius = haloR * 1.45f,
                            center = Offset(cx, cy)
                        )
                        // Audio-reactive celestial geometry rings around the Architect
                        drawCircle(
                            color = Color(0xFFFDE047).copy(alpha = 0.32f + voiceBoost * 0.55f),
                            radius = haloR,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.2f + voiceBoost * 4.5f)
                        )
                        drawCircle(
                            color = Color(0xFF38BDF8).copy(alpha = 0.26f + voiceBoost * 0.45f),
                            radius = haloR * 1.18f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.6f + voiceBoost * 3.0f)
                        )
                        // Drifting golden & electric-blue cosmic embers
                        for (i in 0 until 22) {
                            val seed = (i * 0.37f) % 1f
                            val yFrac = (1.05f - ((emberPhase + seed) % 1.05f))
                            val xFrac = ((i * 0.19f + sin(emberPhase * 6.28f + i) * 0.04f) % 1f + 1f) % 1f
                            val col = if (i % 2 == 0) Color(0xFFFDE047) else Color(0xFF38BDF8)
                            drawCircle(
                                color = col.copy(alpha = (0.32f + voiceBoost * 0.42f) * (1f - kotlin.math.abs(yFrac - 0.5f) * 1.6f).coerceIn(0.15f, 1f)),
                                radius = 2.4f + (i % 3) * 1.2f,
                                center = Offset(size.width * xFrac, size.height * yFrac)
                            )
                        }
                    }
                }
            }

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✦ 2. SEZON JENERİK SONRASI SAHNE",
                    fontFamily = InterFontFamily,
                    color = Color(0xFFFDE047),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xAA0A071E))
                        .border(0.9.dp, Color(0x66BAE6FD), RoundedCornerShape(50))
                        .clickable {
                            if (cutIndex < 4) cutIndex += 1 else onContinueToChoicesSummary()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (cutIndex < 4) "İLERİ ▸" else "ÖZETE GEÇ ▶",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFE0F2FE),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Centerpiece / Lower-Third Cinema Content (keeps Architect's face unobstructed on Cuts 1–3!)
            Column(
                modifier = Modifier
                    .align(if (cutIndex in 1..3) Alignment.BottomCenter else Alignment.Center)
                    .fillMaxWidth()
                    .then(if (cutIndex in 1..3) Modifier.navigationBarsPadding().padding(bottom = 44.dp) else Modifier)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (cutIndex) {
                    0 -> {
                        Text(
                            text = "2. SEZON FİNALİ TAMAMLANDI",
                            fontFamily = InterFontFamily,
                            color = Color(0xFF7DD3FC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "SOKOVİA KRATERİ · ÜÇ SAAT SONRA",
                            fontFamily = DisplaySerifFontFamily,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xCC060412))
                                .border(1.dp, Color(0x88EF4444), RoundedCornerShape(18.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ULTRON PRİME · SÖNEN MERCEK",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFFCA5A5),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.4.sp
                            )
                            Text(
                                text = "Kül ve betonun altında yarı gömülü bir Ultron kafası son kez kıvılcım saçıyor... Kırmızı mercek önce sönüyor — sonra imkânsız altın-mavi bir renge bürünüyor.",
                                fontFamily = LiterarySerifFontFamily,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFFF3E8FF),
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xD2070618))
                                .border(1.2.dp, Color(0xFFFDE047).copy(alpha = 0.72f + architectVoiceAmp * 0.28f), RoundedCornerShape(18.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (architectIsSpeaking) "🎙 ASIL MİMAR · ALTIN-MAVİ YANKI" else "ASIL MİMAR · ALTIN-MAVİ YANKI",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFFDE047),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.6.sp
                            )
                            Text(
                                text = "“$line1”",
                                fontFamily = LiterarySerifFontFamily,
                                fontStyle = FontStyle.Italic,
                                color = Color.White,
                                fontSize = 15.5.sp,
                                lineHeight = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    3 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xD2070618))
                                .border(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.75f + architectVoiceAmp * 0.25f), RoundedCornerShape(18.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (architectIsSpeaking) "🎙 ASIL MİMAR · ÜÇÜNCÜ PERDE" else "ASIL MİMAR · ÜÇÜNCÜ PERDE",
                                fontFamily = InterFontFamily,
                                color = Color(0xFF7DD3FC),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "“$line2”",
                                fontFamily = LiterarySerifFontFamily,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFFBAE6FD),
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFFB45309), Color(0xFF2563EB))))
                                    .border(1.5.dp, Color(0xFFFEF08A), RoundedCornerShape(18.dp))
                                    .clickable { cutIndex = 4 }
                                    .padding(horizontal = 24.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = "✦ GİZLİ BM DOSYASINI AÇ ✦",
                                    fontFamily = InterFontFamily,
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }
                    4 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xE80A081E))
                                .border(1.5.dp, Color(0xFFFDE047), RoundedCornerShape(22.dp))
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "BİRLEŞMİŞ MİLLETLER · GİZLİ PROTOKOL",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFFCA5A5),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.6.sp
                            )
                            Text(
                                text = "SOKOVİA ANLAŞMALARI",
                                fontFamily = DisplaySerifFontFamily,
                                color = Color(0xFFFEF08A),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "“Ekip bölünecek. Aiden Blackwood'un tekilliği artık saklanamaz.”",
                                fontFamily = LiterarySerifFontFamily,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFFE2D9F8),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "KOZMİK SÜRÜKLENİŞ · 3. SEZON\nİÇ SAVAŞ VE MİMAR — ÇOK YAKINDA",
                                fontFamily = InterFontFamily,
                                color = Color(0xFF7DD3FC),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Bottom CTA on Cut 4
            if (cutIndex == 4) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED))))
                        .border(1.3.dp, Color(0xFFBAE6FD), RoundedCornerShape(16.dp))
                        .clickable { onContinueToChoicesSummary() }
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2. SEZON SEÇİMLERİMİ & KADER RAPORUNU GÖR ✦ ▶",
                        fontFamily = InterFontFamily,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (flashAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFEF08A).copy(alpha = flashAlpha))
                )
            }
        }
    }
}
