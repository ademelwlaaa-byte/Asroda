package com.example.ui.screens.cosmic3d

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.LocalCosmicMotionPolicy
import com.example.ui.animation.rememberCosmicPhaseState
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.screens.CharacterPortraitKey
import com.example.ui.screens.CinematicCharacterAvatar
import com.example.ui.screens.ConfigureTrueFullscreenDialogWindow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val MiniGameCardShape = RoundedCornerShape(22.dp)
private val MiniGamePillShape = RoundedCornerShape(50)
private val MiniGameOptionShape = RoundedCornerShape(14.dp)
private val MiniGamePanelShape = RoundedCornerShape(20.dp)
private val MiniGameBarShape = RoundedCornerShape(4.dp)
private val CinemaDepthVignetteBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xCC04020B),
        Color(0x28060312),
        Color(0x55060312),
        Color(0xEE04020B)
    )
)
private val MiniGameVictoryBtnBrush = Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF2563EB)))
private val MiniGameSealBtnBrush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF7C3AED)))

/**
 * 2.5D Interactive Cinematic Story Scenes embedded inside "Kozmik Sürükleniş"
 * via [[MINIGAME|ID|TITLE|SUBTITLE]].
 *
 * Replaces legacy arcade mini-games with zero-layer-hack Motion-Cinema 2.5D scenes:
 * 1) WHITE_EYE_RESONANCE   -> Telltale-style Timed Critical Decisions (3-Act Slow-Mo Cinema)
 * 2) BOSPHORUS_CORE_BALANCE -> Interactive 2.5D Hologram & Secret Investigation Table
 * 3) RED_EYE_WILLPOWER     -> 3-Stage Psychological Willpower Resistance Cinema
 */
enum class StoryMiniGameId(
    val key: String,
    val badgeLabel: String,
    val accentColor: Color,
    val rewardSummary: String,
    val howToPlay: String
) {
    WHITE_EYE_RESONANCE(
        key = "WHITE_EYE_RESONANCE",
        badgeLabel = "2.5D ZAMANLI KARAR SİNEMATİĞİ",
        accentColor = Color(0xFFBAE6FD),
        rewardSummary = "Beyaz Göz Kontrolü & Zihinsel Denge güncellenir",
        howToPlay = "Zaman donduğunda 8 saniyelik kritik an kararlarını ver. Seçimlerin 2.5D sahnenin akışını ve ekip güvenini doğrudan belirler."
    ),
    BOSPHORUS_CORE_BALANCE(
        key = "BOSPHORUS_CORE_BALANCE",
        badgeLabel = "2.5D HOLOGRAM & SIR İNCELEMESİ",
        accentColor = Color(0xFF38BDF8),
        rewardSummary = "Zihinsel Rezonans & Gizli Arşiv Sırları açılır",
        howToPlay = "Boğaz altındaki Rezonans Kulesi'nde 3 hologram veri düğümünü incele, gizli kayıtları çöz ve çekirdek dengeleme kararını ver."
    ),
    RED_EYE_WILLPOWER(
        key = "RED_EYE_WILLPOWER",
        badgeLabel = "2.5D ZİHİN FIRTINASI & İRADE",
        accentColor = Color(0xFFF43F5E),
        rewardSummary = "Kırmızı Göz Dengesizliği düşer · İrade yükselir",
        howToPlay = "3 perdeli psikolojik baskı sinematiğinde öfkeye teslim olmadan doğru irade tepkisini seç ve kontrolü geri al."
    );

    companion object {
        fun fromTag(raw: String): StoryMiniGameId = when (raw.trim().uppercase(Locale.ROOT)) {
            "BOSPHORUS_CORE_BALANCE", "BOSPHORUS_CORE", "CORE_BALANCE" -> BOSPHORUS_CORE_BALANCE
            "RED_EYE_WILLPOWER", "RED_EYE_CONTROL", "WILLPOWER" -> RED_EYE_WILLPOWER
            else -> WHITE_EYE_RESONANCE
        }
    }
}

/**
 * Pure deterministic S/A/B rank & progression calculator (covered by unit tests).
 */
object StoryMiniGameScorer {
    fun computeRank(score: Int, maxPossible: Int): String {
        if (maxPossible <= 0) return "B"
        val ratio = (score.coerceAtLeast(0).toFloat() / maxPossible.toFloat()).coerceIn(0f, 1.5f)
        return when {
            ratio >= 0.82f -> "S"
            ratio >= 0.56f -> "A"
            else -> "B"
        }
    }

    internal fun computeTimerFraction(timeLeftMs: Int, totalMs: Int = 8000): Float {
        if (totalMs <= 0) return 0f
        return (timeLeftMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    }

    internal fun computeWillpowerAfterChoice(current: Int, delta: Int): Int =
        (current + delta).coerceIn(0, 100)

    fun rankSummaryTr(gameId: StoryMiniGameId, rank: String): String = when (gameId) {
        StoryMiniGameId.WHITE_EYE_RESONANCE -> when (rank) {
            "S" -> "Kusursuz Zaman Hakimiyeti! Beyaz Göz Kontrolü +3 · Ekip Güveni Zirvede"
            "A" -> "Güçlü Zihin Sabitlemesi! Beyaz Göz Kontrolü +2 · Zihinsel Rezonans +1"
            else -> "Zihinler Korundu · Beyaz Göz Kontrolü +1"
        }
        StoryMiniGameId.BOSPHORUS_CORE_BALANCE -> when (rank) {
            "S" -> "Tüm Hologram Sırları Çözüldü! Zihinsel Rezonans +3 · İnsanlık Bağı +2"
            "A" -> "Çekirdek Stabilize Edildi! Zihinsel Rezonans +2 · İnsanlık Bağı +1"
            else -> "Boğaz Çekirdeği Kurtarıldı · Zihinsel Rezonans +1"
        }
        StoryMiniGameId.RED_EYE_WILLPOWER -> when (rank) {
            "S" -> "Sarsılmaz İrade Zaferi! Kırmızı Göz Dengesizliği -3 · İnsanlık Bağı +2"
            "A" -> "Öfke Kontrol Altında! Kırmızı Göz Dengesizliği -2 · İnsanlık Bağı +1"
            else -> "İrade Korundu · Kırmızı Göz Dengesizliği -1"
        }
    }
}

/**
 * Mor Cam inline card rendered inside the Book Reader for [[MINIGAME|...]] paragraphs.
 * Launches the full-screen 2.5D Interactive Cinematic Scene.
 */
@Composable
fun InStoryMiniGameCard(
    gameId: StoryMiniGameId,
    title: String,
    subtitle: String,
    savedBestRank: String?,
    onCompleted: (rank: String, score: Int) -> Unit
) {
    var isOpen by remember { mutableStateOf(false) }
    var localBestRank by remember(savedBestRank) { mutableStateOf(savedBestRank) }
    val hasCompleted = !localBestRank.isNullOrBlank()
    val accent = if (hasCompleted) Color(0xFF34D399) else gameId.accentColor
    val launchBrush = remember(accent) {
        Brush.horizontalGradient(listOf(accent.copy(alpha = 0.30f), Color(0xFF4C1D95)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .morCamCosmicGlow(cornerRadius = 22.dp, glowColor = accent, alpha = 0.44f, blurDp = 18.dp)
            .clip(MiniGameCardShape)
            .background(Color(0xF5080619))
            .border(1.3.dp, accent.copy(alpha = 0.78f), MiniGameCardShape)
            .padding(16.dp)
            .testTag("story_minigame_card_${gameId.key}"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(MiniGamePillShape)
                    .background(accent.copy(alpha = 0.20f))
                    .border(0.9.dp, accent.copy(alpha = 0.72f), MiniGamePillShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🎬 ${gameId.badgeLabel}",
                    fontFamily = InterFontFamily,
                    color = accent,
                    fontSize = 9.2.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }

            if (hasCompleted) {
                Box(
                    modifier = Modifier
                        .clip(MiniGamePillShape)
                        .background(Color(0x3310B981))
                        .border(1.dp, Color(0xFF34D399), MiniGamePillShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "✓ İZLENDİ · DERECE $localBestRank",
                        fontFamily = InterFontFamily,
                        color = Color(0xFF6EE7B7),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Text(
            text = title,
            fontFamily = DisplaySerifFontFamily,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            fontFamily = InterFontFamily,
            color = Color(0xFFDCD3F5),
            fontSize = 12.5.sp,
            lineHeight = 18.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MiniGameOptionShape)
                .background(launchBrush)
                .border(1.dp, accent.copy(alpha = 0.75f), MiniGameOptionShape)
                .clickable { isOpen = true }
                .padding(vertical = 12.dp, horizontal = 14.dp)
                .testTag("story_minigame_launch_${gameId.key}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (hasCompleted) "2.5D SAHNEYİ TEKRAR YAŞA ▶" else "2.5D SİNEMATİK SAHNEYİ BAŞLAT ▶",
                fontFamily = InterFontFamily,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp
            )
        }
    }

    if (isOpen) {
        Interactive25DCinemaSceneDialog(
            gameId = gameId,
            title = title,
            onCompleted = { rank, score ->
                localBestRank = rank
                onCompleted(rank, score)
            },
            onDismiss = { isOpen = false }
        )
    }
}

private data class CinemaDecisionOption(
    val label: String,
    val subtext: String,
    val points: Int,
    val outcomeText: String,
    val speakerKey: String,
    val speakerLine: String
)

private data class HologramNode(
    val id: String,
    val title: String,
    val badge: String,
    val xFrac: Float,
    val yFrac: Float,
    val secretText: String,
    val speakerKey: String,
    val voiceLine: String
)

/**
 * Reusable pure Motion-Cinema 2.5D backdrop (ZERO face/mouth/eye layer hacks!):
 * Reads all camera/particle phases strictly in graphicsLayer/Canvas (0 FPS root recomposition),
 * uses async RGB_565 Coil decoding, guards zero-size canvas modulo, and respects Reduce Motion.
 */
@Composable
internal fun Pure25DMotionCinemaBackdrop(
    imageRes: Int,
    accent: Color,
    particleMode: String, // "WHITE_EYE" | "BOSPHORUS" | "RED_EYE" | "STORM" | "RIFT" | "EMBER"
    shakeIntensity: Float = 0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val motionConfig = LocalCosmicMotionConfig.current
    val motionPolicy = LocalCosmicMotionPolicy.current
    val allowMotion = motionPolicy.shouldAnimateContinuously && motionConfig.enableAmbientLoops
    val allowShake = !motionPolicy.shouldUseQuickTransitions

    // Draw-phase-only State<Float> drivers -> 0 FPS root recomposition!
    val zoomState = rememberCosmicPhaseState(
        durationMs = 9000,
        initialValue = 1.05f,
        targetValue = 1.15f,
        staticValue = 1.07f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "cinema25dZoom",
        enabled = allowMotion
    )
    val panXState = rememberCosmicPhaseState(
        durationMs = 11500,
        initialValue = -22f,
        targetValue = 22f,
        staticValue = 0f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "cinema25dPanX",
        enabled = allowMotion
    )
    val particlePhaseState = rememberCosmicPhaseState(
        durationMs = 6000,
        initialValue = 0f,
        targetValue = 1f,
        staticValue = 0.35f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "cinema25dParticles",
        enabled = allowMotion
    )
    val pulseState = rememberCosmicPhaseState(
        durationMs = 2400,
        initialValue = 0.25f,
        targetValue = 0.72f,
        staticValue = 0.48f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "cinema25dPulse",
        enabled = allowMotion
    )

    val safeShake = if (allowShake && shakeIntensity.isFinite()) shakeIntensity.coerceIn(0f, 2f) else 0f

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF05030D))) {
        AnimatedContent(
            targetState = imageRes,
            transitionSpec = {
                val ms = if (motionPolicy.shouldUseQuickTransitions) 180 else 650
                fadeIn(tween(ms)) togetherWith fadeOut(tween(ms))
            },
            label = "scene_cut_crossfade"
        ) { res ->
            val request = remember(context, res) {
                ImageRequest.Builder(context)
                    .data(res)
                    .allowRgb565(true)
                    .crossfade(false)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val z = zoomState.value
                        scaleX = z
                        scaleY = z
                        val pPhase = particlePhaseState.value
                        val shakeX = if (safeShake > 0.01f) sin(pPhase * 90f) * 14f * safeShake else 0f
                        val shakeY = if (safeShake > 0.01f) cos(pPhase * 75f) * 10f * safeShake else 0f
                        translationX = panXState.value + shakeX
                        translationY = shakeY
                    }
            )
        }

        // Cinema lighting vignette + GPU atmosphere particles (draw-phase only)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f) return@Canvas

            val pulse = pulseState.value.coerceIn(0f, 1f)
            val particlePhase = particlePhaseState.value

            // Soft cinema depth vignette
            drawRect(brush = CinemaDepthVignetteBrush)

            // Ambient color aura
            val auraCenter = Offset(w * 0.5f, h * 0.42f)
            val auraRadius = (w * 0.65f).coerceAtLeast(1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.22f * pulse), Color.Transparent),
                    center = auraCenter,
                    radius = auraRadius
                ),
                center = auraCenter,
                radius = auraRadius
            )

            if (!motionConfig.enableParticles) return@Canvas

            when (particleMode) {
                "STORM" -> {
                    for (i in 0 until 42) {
                        val seed = i * 37.3f
                        val x = (((seed * 19f) % w + particlePhase * w * 0.15f) % w + w) % w
                        val y = (((seed * 41f) + particlePhase * h * 1.8f) % h + h) % h
                        drawLine(
                            color = Color(0x55BAE6FD),
                            start = Offset(x, y),
                            end = Offset(x - 6f, y + 26f),
                            strokeWidth = 1.6f
                        )
                    }
                }
                "EMBER" -> {
                    for (i in 0 until 32) {
                        val rawX = ((i * 67f) + sin(particlePhase * 2f * PI.toFloat() + i) * 28f) % w
                        val x = if (rawX < 0f) rawX + w else rawX
                        val y = h - (((i * 53f + particlePhase * h * 1.1f) % h + h) % h)
                        val r = 2.5f + (i % 4) * 1.4f
                        drawCircle(
                            color = if (i % 3 == 0) Color(0xBBFDE047) else Color(0xAAF43F5E),
                            radius = r,
                            center = Offset(x, y)
                        )
                    }
                }
                "WHITE_EYE", "BOSPHORUS", "RIFT" -> {
                    for (i in 0 until 26) {
                        val angle = (i / 26f) * 2f * PI.toFloat() + particlePhase * 2f * PI.toFloat() * 0.25f
                        val rad = (w * 0.14f) + (i % 5) * (w * 0.09f)
                        val px = w * 0.5f + cos(angle) * rad
                        val py = h * 0.42f + sin(angle) * rad * 0.65f
                        drawCircle(
                            color = accent.copy(alpha = (0.45f * pulse + 0.15f).coerceIn(0f, 1f)),
                            radius = 2.2f + (i % 3) * 1.5f,
                            center = Offset(px, py)
                        )
                    }
                }
                else -> {
                    for (i in 0 until 28) {
                        val x = ((i * 71f) % w + w) % w
                        val y = h - (((i * 43f + particlePhase * h) % h + h) % h)
                        drawCircle(
                            color = Color(0xAAF43F5E),
                            radius = 2.5f + (i % 4),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Interactive25DCinemaSceneDialog(
    gameId: StoryMiniGameId,
    title: String,
    onCompleted: (rank: String, score: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val motionPolicy = LocalCosmicMotionPolicy.current

    val ambientId = when (gameId) {
        StoryMiniGameId.WHITE_EYE_RESONANCE -> "BALCONY_CITY"
        StoryMiniGameId.BOSPHORUS_CORE_BALANCE -> "STARLIGHT_PIER"
        StoryMiniGameId.RED_EYE_WILLPOWER -> "DARK_MIRROR"
    }
    val sfxEngine = remember { Cosmic3DSoundEngine(context, enableOrchestralMusic = false) }
    var isVoiceSpeaking by remember { mutableStateOf(false) }
    val voiceEngine = remember(sfxEngine) {
        CosmicCharacterVoiceEngine(context) { speaking ->
            isVoiceSpeaking = speaking
            sfxEngine.setVoiceDucking(speaking)
        }
    }

    var isMuted by remember { mutableStateOf(false) }
    var isLifecycleResumed by remember { mutableStateOf(true) }
    var actIndex by remember { mutableIntStateOf(0) } // 0..2 interactive acts, 3 = VICTORY CINEMATIC
    var totalScore by remember { mutableIntStateOf(0) }
    var shakeTrigger by remember { mutableFloatStateOf(0f) }
    var activeSubtitleSpeaker by remember { mutableStateOf<CharacterPortraitKey?>(null) }
    var activeSubtitleText by remember { mutableStateOf("") }
    var completedReported by remember { mutableStateOf(false) }

    // Act-specific state
    var timeLeftMs by remember { mutableIntStateOf(8000) }
    val inspectedHolograms = remember { mutableStateListOf<String>() }
    var activeHologramNote by remember { mutableStateOf<String?>(null) }
    var willpowerMeter by remember { mutableIntStateOf(40) }

    val particleMode = when (gameId) {
        StoryMiniGameId.WHITE_EYE_RESONANCE -> "WHITE_EYE"
        StoryMiniGameId.BOSPHORUS_CORE_BALANCE -> "BOSPHORUS"
        StoryMiniGameId.RED_EYE_WILLPOWER -> if (actIndex >= 3) "WHITE_EYE" else "RED_EYE"
    }

    val sceneDrawableRes = when (gameId) {
        StoryMiniGameId.WHITE_EYE_RESONANCE -> when (actIndex) {
            0 -> R.drawable.img_ch1_c1_vision_calm
            1 -> R.drawable.img_ch1_c4_natasha_gaze
            2 -> R.drawable.img_ch1_c4_wanda_gaze
            else -> R.drawable.img_anim_mutlak_uyanis
        }
        StoryMiniGameId.BOSPHORUS_CORE_BALANCE -> when (actIndex) {
            0 -> R.drawable.img_anim_bosphorus
            1 -> R.drawable.img_ch6_istanbul_cistern
            else -> R.drawable.img_scene3d_mira_bosphorus_wide_1795627491989
        }
        StoryMiniGameId.RED_EYE_WILLPOWER -> when (actIndex) {
            0 -> R.drawable.img_ch5_voss_blizzard
            1 -> R.drawable.img_ch1_c2_forced_rift
            2 -> R.drawable.img_ch1_c5_tower_night
            else -> R.drawable.img_anim_mutlak_uyanis
        }
    }

    fun speakCinematic(speakerKey: String, line: String) {
        activeSubtitleSpeaker = CharacterPortraitKey.resolveFromLabel(speakerKey)
        activeSubtitleText = line
        if (!isMuted && isLifecycleResumed && line.isNotBlank()) {
            voiceEngine.speak(speakerKey, line)
        }
    }

    DisposableEffect(lifecycleOwner, sfxEngine, voiceEngine) {
        sfxEngine.start(ambientSceneId = ambientId, ambientVolume = 0.36f)
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    isLifecycleResumed = false
                    sfxEngine.setMuted(true)
                    voiceEngine.setMuted(true)
                    voiceEngine.stopCurrentLine()
                }
                Lifecycle.Event.ON_RESUME -> {
                    isLifecycleResumed = true
                    sfxEngine.setMuted(isMuted)
                    voiceEngine.setMuted(isMuted)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            voiceEngine.stopAndRelease()
            sfxEngine.stopAndRelease()
        }
    }

    // Smooth camera shake decay
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0f) {
            delay(360L)
            shakeTrigger = 0f
        }
    }

    // Scene opening audio & lifecycle-pausable Telltale timer for WHITE_EYE_RESONANCE
    LaunchedEffect(actIndex, gameId) {
        if (gameId == StoryMiniGameId.WHITE_EYE_RESONANCE && actIndex in 0..1) {
            timeLeftMs = 8000
            sfxEngine.play(CosmicSfx.TIME_SLOW_ENTER, volume = 0.65f)
            if (actIndex == 0) {
                speakCinematic(
                    "NATASHA",
                    "Başını eğme Aiden. Burada duvar örmene gerek yok — ben yanındayım."
                )
            } else {
                speakCinematic(
                    "WANDA",
                    "Gözlerinin içine baktığımda korku değil, sadece derin, sakin bir mavi görüyorum."
                )
            }
            while (timeLeftMs > 0 && actIndex in 0..1) {
                delay(100L)
                if (isLifecycleResumed) {
                    timeLeftMs = (timeLeftMs - 100).coerceAtLeast(0)
                }
            }
            if (timeLeftMs == 0 && actIndex in 0..1) {
                totalScore += 250
                actIndex = if (actIndex == 0) 1 else 3
            }
        } else if (gameId == StoryMiniGameId.BOSPHORUS_CORE_BALANCE && actIndex == 0) {
            speakCinematic(
                "MIRA",
                "Aiden! Geldiğini kapıdan bile değil, enerjinin sakinliğinden anladım. Sana göstermek istediğim yeni bir şey öğrendim!"
            )
        } else if (gameId == StoryMiniGameId.RED_EYE_WILLPOWER && actIndex == 0) {
            speakCinematic(
                "VOSS",
                "Kırmızı Göz eskiden beni yakardı Aiden. Şimdi onu senin öğrettiğin gibi kalkan yapmayı öğreniyorum."
            )
        } else if (actIndex >= 3) {
            sfxEngine.play(CosmicSfx.VICTORY_FANFARE, volume = 0.72f)
            val finalRank = StoryMiniGameScorer.computeRank(totalScore, 1000)
            if (!completedReported) {
                completedReported = true
                onCompleted(finalRank, totalScore)
            }
            when (gameId) {
                StoryMiniGameId.WHITE_EYE_RESONANCE -> speakCinematic(
                    "STEVE",
                    "Başardın Aiden! Vaelor diz çöktü — Manhattan güvende!"
                )
                StoryMiniGameId.BOSPHORUS_CORE_BALANCE -> speakCinematic(
                    "MIRA",
                    "Artık aynaya baktığımda Dr. Hale'in silahını değil, sadece Mira'yı görüyorum."
                )
                StoryMiniGameId.RED_EYE_WILLPOWER -> speakCinematic(
                    "VOSS",
                    "Omuzların yine taş gibi sertleşmiş kardeşim. Herkesi kurtarmaya çalışırken kendini unutma."
                )
            }
        }
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
        ConfigureTrueFullscreenDialogWindow(hideSystemBars = false)
        BackHandler { onDismiss() }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Pure25DMotionCinemaBackdrop(
                imageRes = sceneDrawableRes,
                accent = gameId.accentColor,
                particleMode = particleMode,
                shakeIntensity = shakeTrigger
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Cinema Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xAA05030E))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎬 ${gameId.badgeLabel}",
                            fontFamily = InterFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.1.sp,
                            color = gameId.accentColor
                        )
                        Text(
                            text = title,
                            fontFamily = DisplaySerifFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                sfxEngine.setMuted(isMuted)
                                voiceEngine.setMuted(isMuted)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x44FFFFFF))
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Ses",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x44EF4444))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                // Middle Interactive Stage Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    when {
                        actIndex >= 3 -> {
                            val finalRank = StoryMiniGameScorer.computeRank(totalScore, 1000)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MiniGameCardShape)
                                    .background(Color(0xDD0A061C))
                                    .border(1.3.dp, Color(0xFF34D399).copy(alpha = 0.8f), MiniGameCardShape)
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "✦ SAHNE TAMAMLANDI · DERECE $finalRank ✦",
                                    fontFamily = InterFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    color = Color(0xFF6EE7B7)
                                )
                                Text(
                                    text = StoryMiniGameScorer.rankSummaryTr(gameId, finalRank),
                                    fontFamily = DisplaySerifFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                if (isVoiceSpeaking) {
                                    Text(
                                        text = "🎙 Karakter konuşuyor — ses kesilmeden dinleyebilirsin...",
                                        fontFamily = InterFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFFFDE68A)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MiniGameOptionShape)
                                        .background(MiniGameVictoryBtnBrush)
                                        .clickable { onDismiss() }
                                        .padding(vertical = 13.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "HİKÂYEYE DEVAM ET ✦",
                                        fontFamily = InterFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        gameId == StoryMiniGameId.WHITE_EYE_RESONANCE -> {
                            val options = remember(actIndex) {
                                if (actIndex == 0) {
                                    listOf(
                                        CinemaDecisionOption(
                                            label = "Beyaz Göz'le Zihinleri Kalkanla",
                                            subtext = "Beş Avenger'a tek bir zarar vermeden dalgayı sönümle",
                                            points = 500,
                                            outcomeText = "Altın-mavi dalga zihinleri incitmeden mühürledi!",
                                            speakerKey = "WANDA",
                                            speakerLine = "Bana böyle baktığında avuçlarımdaki kızıl ışık bile ısınmayı bırakıp yumuşuyor."
                                        ),
                                        CinemaDecisionOption(
                                            label = "Şok Dalgasını Kendi Göğsünde Em",
                                            subtext = "Riskli ama ekibi anında özgür bırakır",
                                            points = 380,
                                            outcomeText = "Tüm yükü üstlendin — zaman akışı sarsılarak normale döndü.",
                                            speakerKey = "NATASHA",
                                            speakerLine = "Elin sıcak... yirmi yıl boyunca kimsenin elini bu kadar güvenle tutmamıştım."
                                        )
                                    )
                                } else {
                                    listOf(
                                        CinemaDecisionOption(
                                            label = "Zaman Akışını Yavaşça Bırak",
                                            subtext = "Kusursuz kontrolle Beyaz Göz'ü mühürle",
                                            points = 500,
                                            outcomeText = "Yağmur damlaları usulca yere indi — tam kontrol sağlandı!",
                                            speakerKey = "NATASHA",
                                            speakerLine = "Eskiden birinin gözlerinin içine üç saniyeden fazla bakmazdım. Seninle zaman duruyor."
                                        ),
                                        CinemaDecisionOption(
                                            label = "Ani Parlama ile Enerjiyi Dağıt",
                                            subtext = "Hızlı ama yüksek enerji boşalımı",
                                            points = 360,
                                            outcomeText = "Mavi bir şimşek odayı aydınlattı ve bağlantı kesildi.",
                                            speakerKey = "WANDA",
                                            speakerLine = "Yıldızlar bu gece çok yakın görünüyor Aiden... Zihnindeki o gürültü ilk kez tamamen susmuş."
                                        )
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MiniGamePanelShape)
                                    .background(Color(0xCC070414))
                                    .border(1.dp, gameId.accentColor.copy(alpha = 0.6f), MiniGamePanelShape)
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = Color(0xFFFDE047),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "PERDE ${actIndex + 1}/2 · ZAMANLI KARAR",
                                            fontFamily = InterFontFamily,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFFDE047)
                                        )
                                    }
                                    Text(
                                        text = String.format(Locale.US, "%.1fs", timeLeftMs / 1000f),
                                        fontFamily = InterFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (timeLeftMs < 2500) Color(0xFFFCA5A5) else Color.White
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { StoryMiniGameScorer.computeTimerFraction(timeLeftMs, 8000) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(MiniGameBarShape),
                                    color = if (timeLeftMs < 2500) Color(0xFFF43F5E) else Color(0xFF38BDF8),
                                    trackColor = Color(0x33FFFFFF)
                                )
                                options.forEach { opt ->
                                    key(opt.label) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(MiniGameOptionShape)
                                                .background(Color(0x552E1065))
                                                .border(1.dp, Color(0x88C4B5FD), MiniGameOptionShape)
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    sfxEngine.play(CosmicSfx.WHITE_EYE_NOVA, volume = 0.7f)
                                                    if (!motionPolicy.shouldUseQuickTransitions) shakeTrigger = 1f
                                                    totalScore += opt.points
                                                    speakCinematic(opt.speakerKey, opt.speakerLine)
                                                    actIndex = if (actIndex == 0) 1 else 3
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = opt.label,
                                                fontFamily = InterFontFamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = opt.subtext,
                                                fontFamily = InterFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFFD8B4FE)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        gameId == StoryMiniGameId.BOSPHORUS_CORE_BALANCE -> {
                            val nodes = remember {
                                listOf(
                                    HologramNode(
                                        id = "hale_log",
                                        title = "Dr. Hale · Rezonans Şifresi",
                                        badge = "GİZLİ VERİ #1",
                                        xFrac = 0.22f,
                                        yFrac = 0.28f,
                                        secretText = "Dr. Hale kuleyi bir silah değil, 7-A ve 8-C'nin frekansını zorla birleştiren bir kafes olarak tasarlamış.",
                                        speakerKey = "VOSS",
                                        voiceLine = "Dr. Hale'in dosyalarını inceledim — Ultron'un peşinde olduğu şey sadece metal değil, senin iraden."
                                    ),
                                    HologramNode(
                                        id = "mira_pulse",
                                        title = "Mira (8-C) · Nabız Haritası",
                                        badge = "GİZLİ VERİ #2",
                                        xFrac = 0.72f,
                                        yFrac = 0.34f,
                                        secretText = "Mira'nın kızıl enerjisi saldırı değil, korku refleksi — sakin mavi dalga ona dokunduğunda nabzı normale dönüyor.",
                                        speakerKey = "MIRA",
                                        voiceLine = "İstanbul'da bana vurmayıp elini uzattığın o saniyeyi her sabah hatırlıyorum."
                                    ),
                                    HologramNode(
                                        id = "core_lock",
                                        title = "Çekirdek Faz Kilidi",
                                        badge = "GİZLİ VERİ #3",
                                        xFrac = 0.48f,
                                        yFrac = 0.52f,
                                        secretText = "Boğaz altındaki basınç vanası manuel olarak mühürlenebilir — tüm şehir tek bir sarsıntı yaşamadan kurtulur.",
                                        speakerKey = "NATASHA",
                                        voiceLine = "Voss ve Mira için kurduğun sığınağı gördüm. Onlara kimsenin bana vermediği bir şansı verdin."
                                    )
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MiniGamePanelShape)
                                    .background(Color(0xCC06091B))
                                    .border(1.dp, Color(0x8838BDF8), MiniGamePanelShape)
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "HOLOGRAM İNCELEME (${inspectedHolograms.size}/3)",
                                            fontFamily = InterFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF7DD3FC)
                                        )
                                    }
                                }

                                nodes.forEach { node ->
                                    key(node.id) {
                                        val done = node.id in inspectedHolograms
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(MiniGameOptionShape)
                                                .background(if (done) Color(0x3310B981) else Color(0x441E3A8A))
                                                .border(
                                                    1.dp,
                                                    if (done) Color(0xFF34D399) else Color(0x8838BDF8),
                                                    MiniGameOptionShape
                                                )
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    sfxEngine.play(CosmicSfx.SPARK_COLLECT, volume = 0.7f)
                                                    if (!done) {
                                                        inspectedHolograms.add(node.id)
                                                        totalScore += 300
                                                        if (inspectedHolograms.size >= 2) actIndex = 1
                                                    }
                                                    activeHologramNote = node.secretText
                                                    speakCinematic(node.speakerKey, node.voiceLine)
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = node.badge,
                                                    fontFamily = InterFontFamily,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (done) Color(0xFF6EE7B7) else Color(0xFF7DD3FC)
                                                )
                                                Text(
                                                    text = node.title,
                                                    fontFamily = InterFontFamily,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Icon(
                                                imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = if (done) Color(0xFF34D399) else Color.White
                                            )
                                        }
                                    }
                                }

                                activeHologramNote?.let { note ->
                                    Text(
                                        text = "✦ $note",
                                        fontFamily = InterFontFamily,
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        color = Color(0xFFE0F2FE)
                                    )
                                }

                                if (inspectedHolograms.size >= 2) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MiniGameOptionShape)
                                            .background(MiniGameSealBtnBrush)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (!motionPolicy.shouldUseQuickTransitions) shakeTrigger = 0.8f
                                                totalScore += 150
                                                actIndex = 3
                                            }
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "ÇEKİRDEĞİ MÜHÜRLE VE BİTİR ✦",
                                            fontFamily = InterFontFamily,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            val stages = remember {
                                listOf(
                                    Triple(
                                        "AŞAMA 1/3 · ÖFKE DALGASI",
                                        "\"Sen de sadece kırık bir deneysin, 7-A! Kontrol edemezsin!\"",
                                        listOf(
                                            CinemaDecisionOption("Nefesini Sabitle — Mavi Işığa Odaklan", "Öfkeyi soğukkanlılıkla susturur (+35 İrade)", 340, "", "VOSS", "Sessiz durman bile iyi geliyor be kardeşim... eskiden sessizlik hücre demekti, şimdi huzur."),
                                            CinemaDecisionOption("Kırmızı Göz'ü Bastırmadan Kalkan Yap", "Yüksek riskli ama güçlü direniş (+28 İrade)", 260, "", "NATASHA", "Gözlerindeki o mavi ışık söndüğünde sadece yorgun bir insan görüyorum — ve en çok o hâlini seviyorum.")
                                        )
                                    ),
                                    Triple(
                                        "AŞAMA 2/3 · GEÇMİŞİN YANKISI",
                                        "Laboratuvar sirenleri zihninde yankılanıyor — Kırmızı Göz parlıyor!",
                                        listOf(
                                            CinemaDecisionOption("Ekibini ve Verdiğin Sözü Hatırla", "İnsanlık bağını güçlendirir (+35 İrade)", 340, "", "WANDA", "Bazen gücümün beni yutmasından korkuyorum... ama senin elini tuttuğumda kendimi hatırlıyorum."),
                                            CinemaDecisionOption("Karanlığı Soğuk Bir Sessizlikle Mühürle", "Sert disiplinle sinyali keser (+26 İrade)", 260, "", "ZERO", "Ultron sadece demir bir çekiç. Asıl sınav, o çekiç indiğinde hâlâ insan kalabilmek.")
                                        )
                                    ),
                                    Triple(
                                        "AŞAMA 3/3 · İRADE MÜHRÜ",
                                        "Son kızıl çatlak kapanmak üzere — kontrol artık senin ellerinde!",
                                        listOf(
                                            CinemaDecisionOption("Kırmızı ve Maviyi Tek İradede Birleştir", "Kusursuz dengeyi kur (+35 İrade)", 340, "", "MIRA", "Eğer bir gün Asıl Mimar karşına çıkarsa, 6-B, 7-A ve 8-C aynı cephede duracağız."),
                                            CinemaDecisionOption("Tüm Öfkeyi Dışarı İt ve Gözlerini Kapat", "Hızlı yatışma (+26 İrade)", 260, "", "VOSS", "Bazen kendi kendime soruyorum: biz gerçekten normal bir hayat yaşayabilir miyiz Aiden? Senin sayende ilk kez 'evet' diyorum.")
                                        )
                                    )
                                )
                            }
                            val st = stages[actIndex.coerceIn(0, 2)]

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MiniGamePanelShape)
                                    .background(Color(0xCC160512))
                                    .border(1.dp, Color(0x99F43F5E), MiniGamePanelShape)
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = st.first,
                                        fontFamily = InterFontFamily,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFDA4AF)
                                    )
                                    Text(
                                        text = "İRADE: %$willpowerMeter",
                                        fontFamily = InterFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7DD3FC)
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (willpowerMeter / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(MiniGameBarShape),
                                    color = Color(0xFF38BDF8),
                                    trackColor = Color(0x44F43F5E)
                                )
                                Text(
                                    text = st.second,
                                    fontFamily = DisplaySerifFontFamily,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                st.third.forEach { opt ->
                                    key(opt.label) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(MiniGameOptionShape)
                                                .background(Color(0x553B0726))
                                                .border(1.dp, Color(0x88FDA4AF), MiniGameOptionShape)
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    sfxEngine.play(CosmicSfx.WHITE_EYE_NOVA, volume = 0.65f)
                                                    if (!motionPolicy.shouldUseQuickTransitions) shakeTrigger = 0.9f
                                                    totalScore += opt.points
                                                    willpowerMeter = StoryMiniGameScorer.computeWillpowerAfterChoice(willpowerMeter, 24)
                                                    speakCinematic(opt.speakerKey, opt.speakerLine)
                                                    actIndex = if (actIndex < 2) actIndex + 1 else 3
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = opt.label,
                                                fontFamily = InterFontFamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = opt.subtext,
                                                fontFamily = InterFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFFFBCFE8)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Cinema Subtitle Ribbon
                if (activeSubtitleText.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xDD04020B))
                            .border(0.7.dp, Color(0x44C4B5FD))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        activeSubtitleSpeaker?.let { speakerKey ->
                            CinematicCharacterAvatar(key = speakerKey, size = 40.dp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = activeSubtitleSpeaker?.displayName ?: "Hikâye",
                                    fontFamily = InterFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeSubtitleSpeaker?.accentColor ?: Color(0xFFC4B5FD)
                                )
                                if (isVoiceSpeaking) {
                                    Text(
                                        text = "🎙 KONUŞUYOR",
                                        fontFamily = InterFontFamily,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF6EE7B7)
                                    )
                                }
                            }
                            Text(
                                text = "\"$activeSubtitleText\"",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = Color(0xFFF5F3FF)
                            )
                        }
                    }
                }
            }
        }
    }
}
