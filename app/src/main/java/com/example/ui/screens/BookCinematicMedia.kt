package com.example.ui.screens

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.screens.cosmic3d.Cosmic3DSoundEngine
import com.example.ui.screens.cosmic3d.CosmicCharacterVoiceEngine
import com.example.ui.screens.cosmic3d.CosmicDuelBossId
import com.example.ui.screens.cosmic3d.CosmicRooftopBossDuelDialog
import com.example.ui.screens.cosmic3d.CosmicSfx
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Forces any Compose fullscreen [Dialog] window to fill 100% edge-to-edge across the entire phone
 * screen (including notch/cutout & nav bar areas) with zero floating dialog margin or black bars.
 */
@Composable
fun ConfigureTrueFullscreenDialogWindow() {
    val view = LocalView.current
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        window.setDimAmount(0f)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            if (lp.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = lp
            }
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
                Lifecycle.Event.ON_PAUSE -> inlineSoundRef?.setMuted(true)
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
            .morCamCosmicGlow(cornerRadius = 22.dp, glowColor = accent, alpha = 0.44f, blurDp = 18.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xF5070516))
            .border(1.2.dp, accent.copy(alpha = 0.72f), RoundedCornerShape(22.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(232.dp)
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
                tiltYawDeg = tiltX,
                tiltPitchDeg = 0f,
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
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDC0A061C))))
            )

            // Top-left Live Cinema Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xCC09061B))
                    .border(0.85.dp, accent.copy(alpha = 0.75f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "✦ SİNEMATİK SAHNE",
                    fontFamily = InterFontFamily,
                    color = accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
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
                        .clip(RoundedCornerShape(50))
                        .background(if (inlineAudioEnabled) accent.copy(alpha = 0.30f) else Color(0xCC110B2E))
                        .border(0.9.dp, if (inlineAudioEnabled) accent else Color(0x99BAE6FD), RoundedCornerShape(50))
                        .clickable { inlineAudioEnabled = !inlineAudioEnabled }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
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
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xCC110B2E))
                        .border(0.9.dp, Color(0xCCBAE6FD), RoundedCornerShape(50))
                        .clickable { isFullscreenOpen = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "⤢ TAM EKRAN",
                        fontFamily = InterFontFamily,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Text(
                text = "↔ 2.5D Derinlik için sürükle · Tam ekranda sinema sesli izle",
                fontFamily = InterFontFamily,
                color = Color(0xCCE2D9F8),
                fontSize = 9.5.sp,
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
 * Reusable 60fps Live-Action 2.5D Motion + Atmospheric VFX Canvas Engine.
 */
@Composable
private fun LiveActionCinemaMotionCanvas(
    sceneId: String,
    tiltYawDeg: Float,
    tiltPitchDeg: Float,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "cinema_vfx_$sceneId")
    val flowPhase by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "flow"
    )
    val cameraPush by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9500, easing = LinearEasing), RepeatMode.Reverse),
        label = "push"
    )
    val energyPulse by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1900, easing = LinearEasing), RepeatMode.Reverse),
        label = "energy"
    )

    val particles = remember(sceneId) {
        List(34) { idx ->
            Triple(
                Random(idx * 41 + 9).nextFloat(),
                Random(idx * 67 + 5).nextFloat(),
                0.6f + Random(idx * 23 + 3).nextFloat() * 0.9f
            )
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(0.dp))) {
        // Layer 1: High-Res Photorealistic Live-Action Movie Frame loaded via Coil RGB_565 (OOM-safe, zero Size.ORIGINAL)
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
                    val zoom = 1.03f + cameraPush * 0.03f
                    scaleX = zoom
                    scaleY = zoom
                    translationX = sin(cameraPush * PI.toFloat()) * 6f - tiltYawDeg * 1.1f
                    translationY = cos(cameraPush * PI.toFloat()) * 3f - tiltPitchDeg * 0.9f
                    rotationY = tiltYawDeg * 0.55f
                    rotationX = -tiltPitchDeg * 0.45f
                    cameraDistance = 16f * density
                }
        )

        // Layer 2: Scene-Specific 60fps Live Cinema VFX (Rain, Cosmic Lightning, Eye Bloom, Frozen Sparks)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Subtle storm rain across nocturnal Manhattan & Istanbul scenes
            if (sceneId != "MUTLAK_UYANIS") {
                particles.forEach { (px, py, spd) ->
                    val prog = (py + flowPhase * spd * 1.8f) % 1f
                    val sx = ((px - prog * 0.14f) + 1f) % 1f * w
                    val sy = prog * h
                    drawLine(
                        color = Color(0x44BAE6FD),
                        start = Offset(sx, sy),
                        end = Offset(sx - 5f, sy + 18f),
                        strokeWidth = 1.3f
                    )
                }
            }

            when (sceneId) {
                "BLUE_SKY_2006" -> {
                    // Crackling blue sky rift flash + glowing falling sparks
                    val flash = if (energyPulse > 0.84f) (energyPulse - 0.84f) * 2.4f else 0f
                    if (flash > 0.01f) {
                        drawRect(Color(0xFF38BDF8).copy(alpha = flash * 0.24f))
                    }
                    particles.take(20).forEachIndexed { idx, (px, py, spd) ->
                        val prog = (py + flowPhase * spd * 0.9f) % 1f
                        val sx = w * (0.28f + px * 0.44f) + sin(flowPhase * 6.28f + idx) * 12f
                        val sy = h * 0.15f + prog * h * 0.82f
                        drawCircle(
                            color = Color(0xFFBAE6FD).copy(alpha = (1f - prog) * 0.85f),
                            radius = 1.8f + (idx % 3),
                            center = Offset(sx, sy)
                        )
                    }
                }

                "WHITE_EYE_AWAKENING" -> {
                    // Pulsing White Eye radiant glare + expanding telepathic rings
                    val glowRad = w * (0.20f + energyPulse * 0.08f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.28f * energyPulse), Color(0xFF38BDF8).copy(alpha = 0.14f), Color.Transparent),
                            center = Offset(cx, h * 0.37f),
                            radius = glowRad
                        ),
                        radius = glowRad,
                        center = Offset(cx, h * 0.37f)
                    )
                    for (r in 0..2) {
                        val p = (flowPhase + r * 0.33f) % 1f
                        drawCircle(
                            color = Color(0xFFE0F2FE).copy(alpha = (1f - p) * 0.36f),
                            radius = 22f + p * w * 0.46f,
                            center = Offset(cx, h * 0.37f),
                            style = Stroke(width = 1.8f)
                        )
                    }
                }

                "BOSPHORUS_RESONANCE" -> {
                    // Swirling twin blue & crimson rising energy sparks around the Bosphorus vortex
                    particles.take(24).forEachIndexed { idx, (px, py, spd) ->
                        val prog = (1f - ((py + flowPhase * spd) % 1f))
                        val angle = flowPhase * 6.28f * 1.5f + idx
                        val sx = w * 0.56f + cos(angle) * (18f + px * 58f)
                        val sy = h * 0.14f + prog * h * 0.72f
                        val col = if (idx % 2 == 0) Color(0xFF38BDF8) else Color(0xFFF43F5E)
                        drawCircle(col.copy(alpha = 0.75f * sin(prog * PI.toFloat())), radius = 2.4f, center = Offset(sx, sy))
                    }
                }

                "MANHATTAN_ARMADA" -> {
                    // Periodic sky lightning arcs illuminating the dreadnought + silver ring pulse
                    if (energyPulse > 0.80f) {
                        val a = (energyPulse - 0.80f) / 0.20f
                        drawRect(Color(0xFFBAE6FD).copy(alpha = a * 0.20f))
                        val bolt = Path().apply {
                            moveTo(w * 0.50f, h * 0.38f)
                            lineTo(w * 0.47f, h * 0.49f)
                            lineTo(w * 0.52f, h * 0.57f)
                            lineTo(w * 0.49f, h * 0.68f)
                        }
                        drawPath(bolt, Color(0xFF7DD3FC).copy(alpha = a * 0.85f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                        drawPath(bolt, Color.White.copy(alpha = a), style = Stroke(width = 2f, cap = StrokeCap.Round))
                    }
                }

                else -> {
                    // MUTLAK_UYANIS: Time-frozen glistening raindrops & dual blue/crimson energy arcs!
                    particles.forEachIndexed { idx, (px, py, _) ->
                        val hoverY = py * h + sin(flowPhase * 6.28f + idx) * 3.5f
                        val sx = px * w
                        val col = if (idx % 3 == 0) Color(0xFFF43F5E) else Color(0xFF7DD3FC)
                        drawCircle(col.copy(alpha = 0.45f + energyPulse * 0.45f), radius = 2.2f, center = Offset(sx, hoverY))
                    }
                    // Pulsing cosmic aura around Aiden's hands on the rooftop
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF38BDF8).copy(alpha = 0.22f * energyPulse), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = w * 0.34f
                        ),
                        radius = w * 0.34f,
                        center = Offset(cx, cy)
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
    val lifecycleOwner = LocalLifecycleOwner.current
    var isMuted by remember { mutableStateOf(false) }
    var tiltYaw by remember { mutableFloatStateOf(0f) }
    var tiltPitch by remember { mutableFloatStateOf(0f) }

    val soundEngine = remember(sceneId) {
        Cosmic3DSoundEngine(context, enableOrchestralMusic = true)
    }

    DisposableEffect(soundEngine, sceneId) {
        soundEngine.start(ambientSceneId = sceneId, ambientVolume = 0.64f)
        onDispose { soundEngine.stopAndRelease() }
    }

    DisposableEffect(lifecycleOwner, soundEngine) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> soundEngine.setMuted(true)
                Lifecycle.Event.ON_RESUME -> soundEngine.setMuted(isMuted)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                        onDragEnd = { tiltYaw = 0f; tiltPitch = 0f },
                        onDragCancel = { tiltYaw = 0f; tiltPitch = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            tiltYaw = (tiltYaw + dragAmount.x * 0.035f).coerceIn(-12f, 12f)
                            tiltPitch = (tiltPitch + dragAmount.y * 0.035f).coerceIn(-9f, 9f)
                        }
                    )
                }
        ) {
            // Subtle ambient theater aura backdrop (dimmed, Coil RGB_565 decoded)
            val auraRequest = remember(context, sceneId) {
                ImageRequest.Builder(context)
                    .data(sceneBackdropRes(sceneId))
                    .allowRgb565(true)
                    .crossfade(300)
                    .build()
            }
            AsyncImage(
                model = auraRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.22f; scaleX = 1.15f; scaleY = 1.15f }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xE603020A), Color(0x99060414), Color(0xEC03020A))
                        )
                    )
            )

            // Centerpiece TRUE 16:9 Full-Frame 2.5D Cinema Stage (ZERO over-zoom / ZERO edge cropping!)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .aspectRatio(16f / 9f)
                    .graphicsLayer {
                        rotationY = tiltYaw * 0.72f
                        rotationX = -tiltPitch * 0.62f
                        cameraDistance = 18f * density
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.2.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            ) {
                LiveActionCinemaMotionCanvas(
                    sceneId = sceneId,
                    tiltYawDeg = tiltYaw * 0.45f,
                    tiltPitchDeg = tiltPitch * 0.45f,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xCC09061B))
                        .border(1.dp, accent.copy(alpha = 0.78f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "✦ SİNEMA MODU · 2.5D DERİNLİK",
                        fontFamily = InterFontFamily,
                        color = accent,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xD9100926))
                            .border(1.dp, accent.copy(alpha = 0.8f), CircleShape)
                            .clickable {
                                isMuted = !isMuted
                                soundEngine.setMuted(isMuted)
                            },
                        contentAlignment = Alignment.Center
                    ) { Text(if (isMuted) "🔇" else "🔊", fontSize = 15.sp) }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xD9100926))
                            .border(1.dp, Color(0xAAFFFFFF), CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) { Text("✕", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xF2070415))))
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = caption,
                    fontFamily = DisplaySerifFontFamily,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        fontFamily = LiterarySerifFontFamily,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFFE0D6F8),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

/**
 * Chapter 10 Mandatory Grounded Rooftop Boss Duel Launch Card!
 * NO FLYING — 100% Canon Aiden Blackwood (Işınlanma, Drift Emilim, Beyaz Göz, Kırmızı Göz + Sesli Avengers Desteği).
 * Winning this duel unlocks Chapter 10 Scene 5 story progression.
 */
@Composable
fun Cosmic3DRiftMiniGame(
    title: String,
    subtitle: String,
    bossId: CosmicDuelBossId = CosmicDuelBossId.VAELOR_ROOFTOP,
    pietroSaved: Boolean = false,
    isCompleted: Boolean = false,
    onVictoryUnlocked: () -> Unit = {}
) {
    var isDuelOpen by remember { mutableStateOf(false) }
    var localWon by remember(isCompleted) { mutableStateOf(isCompleted) }
    var bestScore by remember { mutableIntStateOf(0) }
    var bestRank by remember { mutableStateOf("") }

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
    val sfxEngine = remember { Cosmic3DSoundEngine(context, enableOrchestralMusic = false) }
    val voiceEngine = remember(sfxEngine) {
        CosmicCharacterVoiceEngine(context) { speaking ->
            sfxEngine.setVoiceDucking(speaking)
        }
    }

    // 0 = Black Title Intro, 1 = Ruined Dark-Future Manhattan, 2 = Scarred Future Aiden Reveal,
    // 3 = Interactive Rift Touch, 4 = Season 2 Title Card Slam
    var cutIndex by remember { mutableIntStateOf(0) }
    var riftFlashAlpha by remember { mutableFloatStateOf(0f) }

    val line1Text = "Sen kazandın Aiden... benim yapamadığımı başardın. Avengers'ı kurtardın. Ama Vaelor sadece bir habercidir — Asıl Mimar seni buldu... ve yakında demir fırtına başlayacak."
    val line2Text = "Kapıyı aç... zaman kırılmadan önce sana onları nasıl hayatta tutacağını göstereyim."

    DisposableEffect(sfxEngine, voiceEngine) {
        sfxEngine.start(ambientSceneId = "BLUE_SKY_2006", ambientVolume = 0.50f)
        voiceEngine.prewarmLines(
            listOf(
                Triple("TWIN_AIDEN", line1Text, "voice_twin_aiden_1"),
                Triple("TWIN_AIDEN", line2Text, "voice_twin_aiden_2")
            )
        )
        onDispose {
            sfxEngine.stopAndRelease()
            voiceEngine.stopAndRelease()
        }
    }

    // Automatic cinema pacing (user can tap Next anytime)
    LaunchedEffect(cutIndex) {
        when (cutIndex) {
            0 -> {
                sfxEngine.play(CosmicSfx.TIME_SLOW_ENTER, volume = 0.72f)
                delay(2400L)
                if (cutIndex == 0) cutIndex = 1
            }
            1 -> {
                sfxEngine.play(CosmicSfx.LASER_WARNING, volume = 0.48f)
                delay(5200L)
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

    val inf = rememberInfiniteTransition(label = "postcredits_motion")
    val kenBurns by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "kb"
    )
    val rainFlow by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing), RepeatMode.Restart),
        label = "rf"
    )
    val pulse by inf.animateFloat(
        initialValue = 0.55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pl"
    )

    val ashParticles = remember {
        List(32) { i ->
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
            // LAYER 1: FULLSCREEN LIVE-ACTION CINEMA CUTS WITH SMOOTH CROSSFADE & KEN BURNS CAMERA
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
                        // CUT 1: Apocalyptic Ruined Parallel Manhattan + Broken Captain America Shield + Two Moons!
                        val ruinedReq = remember(context) {
                            ImageRequest.Builder(context)
                                .data(R.drawable.img_postcredits_ruined_nyc)
                                .allowRgb565(true)
                                .crossfade(300)
                                .build()
                        }
                        AsyncImage(
                            model = ruinedReq,
                            contentDescription = "Yıkılmış Paralel Manhattan",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val sc = 1.08f + kenBurns * 0.12f
                                    scaleX = sc; scaleY = sc
                                    translationX = (kenBurns - 0.5f) * 32f
                                }
                        )
                    }
                    2, 3 -> {
                        // CUT 2 & 3: Battle-Scarred Glowing-Blue-Eyed Twin Aiden Reaching Through the Rift!
                        val twinReq = remember(context) {
                            ImageRequest.Builder(context)
                                .data(R.drawable.img_postcredits_twin_aiden)
                                .allowRgb565(true)
                                .crossfade(300)
                                .build()
                        }
                        AsyncImage(
                            model = twinReq,
                            contentDescription = "Yara İzli İkiz Aiden",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val sc = if (activeCut == 3) 1.15f + kenBurns * 0.06f else 1.05f + kenBurns * 0.10f
                                    scaleX = sc; scaleY = sc
                                    translationX = -(kenBurns - 0.5f) * 18f
                                }
                        )
                    }
                    else -> {
                        // CUT 4: Season 2 Dark Blue Rift Title Backdrop
                        val s2Req = remember(context) {
                            ImageRequest.Builder(context)
                                .data(R.drawable.img_postcredits_ruined_nyc)
                                .allowRgb565(true)
                                .crossfade(300)
                                .build()
                        }
                        AsyncImage(
                            model = s2Req,
                            contentDescription = "2. Sezon",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = 0.40f; scaleX = 1.12f; scaleY = 1.12f }
                        )
                    }
                }
            }

            // LAYER 2: LIVE 60FPS ASH EMBERS, STORM RAIN & CRACKLING RIFT LIGHTNING
            if (cutIndex in 1..4) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    ashParticles.forEachIndexed { idx, (px, py, spd) ->
                        if (cutIndex == 1) {
                            // Drifting glowing orange ash embers rising over the broken shield!
                            val prog = (1f - ((py + rainFlow * spd * 0.8f) % 1f))
                            val sx = px * w + sin(rainFlow * 6.28f + idx) * 16f
                            drawCircle(Color(0xFFFB923C).copy(alpha = 0.72f), radius = 2.2f + (idx % 3), center = Offset(sx, prog * h))
                        } else {
                            // Electric blue dimensional sparks swirling around Twin Aiden's rift!
                            val prog = (py + rainFlow * spd * 1.1f) % 1f
                            val sx = px * w + cos(rainFlow * 6.28f + idx) * 14f
                            drawCircle(Color(0xFF7DD3FC).copy(alpha = 0.75f * pulse), radius = 2f + (idx % 3), center = Offset(sx, prog * h))
                        }
                        // Fine stormy night rain
                        val rProg = (py + rainFlow * spd * 2.0f) % 1f
                        val rx = ((px - rProg * 0.14f) + 1f) % 1f * w
                        drawLine(Color(0x36BAE6FD), Offset(rx, rProg * h), Offset(rx - 5f, rProg * h + 18f), strokeWidth = 1.2f)
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                            .morCamCosmicGlow(cornerRadius = 66.dp, glowColor = Color(0xFF38BDF8), alpha = 0.72f * pulse, blurDp = 28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xCCBAE6FD), Color(0xAA0284C7), Color(0x66060D24))
                                )
                            )
                            .border(2.2.dp, Color.White.copy(alpha = pulse), CircleShape)
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

            // BOTTOM CINEMA SUBTITLES & FINAL CTA BUTTON
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
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
    val voiceEngine = remember(sfxEngine) {
        CosmicCharacterVoiceEngine(context) { speaking ->
            sfxEngine.setVoiceDucking(speaking)
        }
    }

    var cutIndex by remember { mutableIntStateOf(0) }
    var flashAlpha by remember { mutableFloatStateOf(0f) }

    val line1 = "Birinci perde kapandı, Kayıp Anahtarım... Demir kuklayı kırdın, ama insanların korkusunu kıramazsın."
    val line2 = "Şimdi seni koruyan kahramanlar bile birbirine karşı duracak. Masadaki dosyayı aç... üçüncü perde başladı."

    DisposableEffect(sfxEngine, voiceEngine) {
        sfxEngine.start(ambientSceneId = "BLUE_SKY_2006", ambientVolume = 0.48f)
        voiceEngine.prewarmLines(
            listOf(
                Triple("ARCHITECT", line1, "voice_s2_postcredits_1"),
                Triple("ARCHITECT", line2, "voice_s2_postcredits_2")
            )
        )
        onDispose {
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
                delay(7600L)
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

    val inf = rememberInfiniteTransition(label = "s2_pc_motion")
    val pulse by inf.animateFloat(
        initialValue = 0.55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "s2_pl"
    )
    val kenBurns by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8500, easing = LinearEasing), RepeatMode.Reverse),
        label = "s2_kb"
    )

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
            if (cutIndex >= 1) {
                val sokoviaReq = remember(context) {
                    ImageRequest.Builder(context)
                        .data(R.drawable.img_postcredits_ruined_nyc)
                        .allowRgb565(true)
                        .crossfade(300)
                        .build()
                }
                AsyncImage(
                    model = sokoviaReq,
                    contentDescription = "Sokovia Enkazı",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val sc = 1.08f + kenBurns * 0.10f
                            scaleX = sc; scaleY = sc
                            alpha = if (cutIndex == 4) 0.35f else 0.72f
                        }
                )
            }

            // Gold-Blue Architect Eye Glow Overlay on Cut 2 & 3
            if (cutIndex == 2 || cutIndex == 3) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height * 0.38f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0xFFFEF08A).copy(alpha = 0.42f * pulse),
                                Color(0xFF38BDF8).copy(alpha = 0.25f * pulse),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = size.minDimension * 0.46f
                        ),
                        radius = size.minDimension * 0.46f,
                        center = Offset(cx, cy)
                    )
                }
            }

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
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

            // Centerpiece Cinema Content
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        CinematicCharacterAvatar(
                            key = CharacterPortraitKey.ULTRON,
                            size = 82.dp,
                            borderWidth = 2.dp,
                            borderColor = Color(0xFFEF4444)
                        )
                        Text(
                            text = "Kül ve betonun altında yarı gömülü bir Ultron kafası son kez kıvılcım saçıyor... Kırmızı mercek önce sönüyor — sonra imkânsız bir renge bürünüyor.",
                            fontFamily = LiterarySerifFontFamily,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFFF3E8FF),
                            fontSize = 15.5.sp,
                            lineHeight = 23.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    2 -> {
                        CinematicCharacterAvatar(
                            key = CharacterPortraitKey.ARCHITECT,
                            size = 92.dp,
                            borderWidth = 2.4.dp,
                            borderColor = Color(0xFFFDE047)
                        )
                        Text(
                            text = "ASIL MİMAR · ALTIN-MAVİ YANKI",
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
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    3 -> {
                        CinematicCharacterAvatar(
                            key = CharacterPortraitKey.ARCHITECT,
                            size = 78.dp,
                            borderWidth = 2.dp,
                            borderColor = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "“$line2”",
                            fontFamily = LiterarySerifFontFamily,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFFBAE6FD),
                            fontSize = 15.5.sp,
                            lineHeight = 23.sp,
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
                        .padding(horizontal = 20.dp, vertical = 18.dp)
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
