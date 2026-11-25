package com.example.ui.screens.cosmic3d

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.screens.CharacterPortraitKey
import com.example.ui.screens.CinematicCharacterAvatar
import com.example.ui.screens.ConfigureTrueFullscreenDialogWindow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * 100% offline, API-free interactive story mini-games embedded directly inside
 * "Kozmik Sürükleniş" chapters via [[MINIGAME|GAME_ID|TITLE|SUBTITLE]].
 * Outcomes directly update deterministic StoryFlagAxis values and save S/A/B medals locally.
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
        badgeLabel = "RİTİM & ZİHİN ODAĞI",
        accentColor = Color(0xFFE0F2FE),
        rewardSummary = "Beyaz Göz Kontrolü & Zihinsel Denge güncellenir",
        howToPlay = "Daralan kozmik halka altın çembere girdiği anda ekrana dokun! 5 Avenger zihnini tek bir çizik atmadan sabitle."
    ),
    BOSPHORUS_CORE_BALANCE(
        key = "BOSPHORUS_CORE_BALANCE",
        badgeLabel = "FİZİK & ENERJİ DENGESİ",
        accentColor = Color(0xFF38BDF8),
        rewardSummary = "Zihinsel Rezonans & İnsanlık Bağı güçlenir",
        howToPlay = "Ekrana BASILI TUTARAK mavi kozmik itki ver, BIRAKARAK kızıl çekime izin ver. Çekirdeği 9 saniye güvenli bantta tut!"
    ),
    RED_EYE_WILLPOWER(
        key = "RED_EYE_WILLPOWER",
        badgeLabel = "REFLEKS & İRADE MÜHRÜ",
        accentColor = Color(0xFFF43F5E),
        rewardSummary = "Kırmızı Göz Dengesizliği düşer · İrade yükselir",
        howToPlay = "Öfke yükselirken ekranda beliren parlayan irade mühürlerine hızla dokun! Kırmızı Göz çatlaklarını büyümeden kapat."
    );

    companion object {
        fun fromTag(raw: String): StoryMiniGameId = when (raw.trim().uppercase()) {
            "BOSPHORUS_CORE_BALANCE", "BOSPHORUS_CORE", "CORE_BALANCE" -> BOSPHORUS_CORE_BALANCE
            "RED_EYE_WILLPOWER", "RED_EYE_CONTROL", "WILLPOWER" -> RED_EYE_WILLPOWER
            else -> WHITE_EYE_RESONANCE
        }
    }
}

private enum class MiniGamePhase { READY, PLAYING, RESULT }

/**
 * Pure deterministic S/A/B rank calculator (covered by unit tests).
 */
object StoryMiniGameScorer {
    fun computeRank(score: Int, maxPossible: Int): String {
        val ratio = if (maxPossible <= 0) 0f else score.toFloat() / maxPossible.toFloat()
        return when {
            ratio >= 0.82f -> "S"
            ratio >= 0.56f -> "A"
            else -> "B"
        }
    }

    fun rankSummaryTr(gameId: StoryMiniGameId, rank: String): String = when (gameId) {
        StoryMiniGameId.WHITE_EYE_RESONANCE -> when (rank) {
            "S" -> "Kusursuz Zihin Mutlakiyeti! Beyaz Göz Kontrolü +3 · Zihinsel Yorgunluk -1"
            "A" -> "Güçlü Zihin Sabitlemesi! Beyaz Göz Kontrolü +2 · Zihinsel Rezonans +1"
            else -> "Zihinler Durduruldu · Beyaz Göz Kontrolü +1"
        }
        StoryMiniGameId.BOSPHORUS_CORE_BALANCE -> when (rank) {
            "S" -> "Kusursuz Çekirdek Uyumu! Zihinsel Rezonans +3 · İnsanlık Bağı +2"
            "A" -> "Çekirdek Dengelendi! Zihinsel Rezonans +2 · İnsanlık Bağı +1"
            else -> "Fırtına Kontrol Altında · Zihinsel Rezonans +1"
        }
        StoryMiniGameId.RED_EYE_WILLPOWER -> when (rank) {
            "S" -> "Sarsılmaz İrade! Kırmızı Göz Dengesizliği -3 · İnsanlık Bağı +2"
            "A" -> "Öfke Bastırıldı! Kırmızı Göz Dengesizliği -2 · İnsanlık Bağı +1"
            else -> "Kontrol Sağlandı · Kırmızı Göz Dengesizliği -1"
        }
    }
}

/**
 * Mor Cam inline card rendered inside the Book Reader for [[MINIGAME|...]] paragraphs.
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .morCamCosmicGlow(cornerRadius = 22.dp, glowColor = accent, alpha = 0.44f, blurDp = 18.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xF5080619))
            .border(1.3.dp, accent.copy(alpha = 0.78f), RoundedCornerShape(22.dp))
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
                    .clip(RoundedCornerShape(50))
                    .background(accent.copy(alpha = 0.20f))
                    .border(0.9.dp, accent.copy(alpha = 0.72f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "✦ ${gameId.badgeLabel}",
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
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x3334D399))
                        .border(0.9.dp, Color(0xAA6EE7B7), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "★ DERECE $localBestRank",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFFDE047),
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
            fontSize = 16.5.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = subtitle,
            fontFamily = InterFontFamily,
            color = Color(0xFFDCD3F5),
            fontSize = 12.2.sp,
            lineHeight = 18.sp
        )

        Text(
            text = "Etki: ${gameId.rewardSummary}",
            fontFamily = InterFontFamily,
            color = accent.copy(alpha = 0.92f),
            fontSize = 10.8.sp,
            fontWeight = FontWeight.SemiBold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(
                    Brush.horizontalGradient(
                        if (hasCompleted) listOf(Color(0xFF059669), Color(0xFF2563EB))
                        else listOf(Color(0xFF4F46E5), accent.copy(alpha = 0.85f))
                    )
                )
                .border(1.1.dp, Color(0xCCFFFFFF), RoundedCornerShape(15.dp))
                .clickable { isOpen = true }
                .padding(vertical = 12.dp)
                .testTag("story_minigame_launch_${gameId.key}"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (hasCompleted) "✦ SAHNE OYUNUNU TEKRAR OYNA ▶" else "✦ SAHNE OYUNUNU BAŞLAT ▶",
                fontFamily = InterFontFamily,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp
            )
        }
    }

    if (isOpen) {
        StoryMiniGameFullscreenDialog(
            gameId = gameId,
            title = title,
            onComplete = { rank, score ->
                val currentOrder = mapOf("S" to 3, "A" to 2, "B" to 1)
                if ((currentOrder[rank] ?: 0) >= (currentOrder[localBestRank] ?: 0)) {
                    localBestRank = rank
                }
                onCompleted(rank, score)
            },
            onDismiss = { isOpen = false }
        )
    }
}

@Composable
fun StoryMiniGameFullscreenDialog(
    gameId: StoryMiniGameId,
    title: String,
    onComplete: (rank: String, score: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val soundEngine = remember { Cosmic3DSoundEngine(context, enableOrchestralMusic = false) }
    var isMuted by remember { mutableStateOf(false) }

    var phase by remember { mutableStateOf(MiniGamePhase.READY) }
    var finalScore by remember { mutableIntStateOf(0) }
    var finalRank by remember { mutableStateOf("B") }
    var reported by remember { mutableStateOf(false) }

    DisposableEffect(soundEngine, gameId) {
        val sceneTag = when (gameId) {
            StoryMiniGameId.WHITE_EYE_RESONANCE -> "WHITE_EYE_AWAKENING"
            StoryMiniGameId.BOSPHORUS_CORE_BALANCE -> "BOSPHORUS_RESONANCE"
            StoryMiniGameId.RED_EYE_WILLPOWER -> "MUTLAK_UYANIS"
        }
        soundEngine.start(ambientSceneId = sceneTag, ambientVolume = 0.36f)
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

    fun finishGame(score: Int, maxPossible: Int) {
        finalScore = score
        finalRank = StoryMiniGameScorer.computeRank(score, maxPossible)
        phase = MiniGamePhase.RESULT
        soundEngine.play(CosmicSfx.VICTORY_FANFARE, volume = 0.85f, affectedByTimeSlow = false)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (!reported) {
            reported = true
            onComplete(finalRank, finalScore)
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
        ConfigureTrueFullscreenDialogWindow()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF060414), Color(0xFF100828), Color(0xFF050310))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 12.dp, bottom = 22.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "✦ ${gameId.badgeLabel}",
                            fontFamily = InterFontFamily,
                            color = gameId.accentColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.9.sp
                        )
                        Text(
                            text = title,
                            fontFamily = DisplaySerifFontFamily,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xCC160C30))
                                .border(1.dp, gameId.accentColor.copy(alpha = 0.7f), CircleShape)
                                .clickable {
                                    isMuted = !isMuted
                                    soundEngine.setMuted(isMuted)
                                },
                            contentAlignment = Alignment.Center
                        ) { Text(if (isMuted) "🔇" else "🔊", fontSize = 15.sp) }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xCC160C30))
                                .border(1.dp, Color(0xAAFFFFFF), CircleShape)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) { Text("✕", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                    }
                }

                // Active Stage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when (phase) {
                        MiniGamePhase.READY -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.88f)
                                    .verticalScroll(rememberScrollState())
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xEC0D0824))
                                    .border(1.4.dp, gameId.accentColor.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = title.uppercase(),
                                    fontFamily = DisplaySerifFontFamily,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = gameId.howToPlay,
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFE2D9F8),
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "✦ ${gameId.rewardSummary}",
                                    fontFamily = InterFontFamily,
                                    color = gameId.accentColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 50.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED))))
                                        .border(1.2.dp, Color(0xCCFFFFFF), RoundedCornerShape(16.dp))
                                        .clickable {
                                            reported = false
                                            phase = MiniGamePhase.PLAYING
                                        }
                                        .testTag("story_minigame_start_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "BAŞLAT ▶",
                                        fontFamily = InterFontFamily,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        MiniGamePhase.PLAYING -> {
                            when (gameId) {
                                StoryMiniGameId.WHITE_EYE_RESONANCE -> WhiteEyeResonanceStage(
                                    soundEngine = soundEngine,
                                    onFinish = { score, max -> finishGame(score, max) }
                                )
                                StoryMiniGameId.BOSPHORUS_CORE_BALANCE -> BosphorusCoreBalanceStage(
                                    soundEngine = soundEngine,
                                    onFinish = { score, max -> finishGame(score, max) }
                                )
                                StoryMiniGameId.RED_EYE_WILLPOWER -> RedEyeWillpowerStage(
                                    soundEngine = soundEngine,
                                    onFinish = { score, max -> finishGame(score, max) }
                                )
                            }
                        }

                        MiniGamePhase.RESULT -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.88f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xF20B0822))
                                    .border(1.5.dp, Color(0xFF34D399), RoundedCornerShape(24.dp))
                                    .padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "✦ SAHNE TAMAMLANDI ✦",
                                    fontFamily = DisplaySerifFontFamily,
                                    color = Color(0xFF6EE7B7),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0x88064E3B))
                                        .border(1.dp, Color(0x8834D399), RoundedCornerShape(14.dp))
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("DERECE", fontFamily = InterFontFamily, color = Color(0xFFD1FAE5), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(finalRank, fontFamily = DisplaySerifFontFamily, color = Color(0xFFFDE047), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("SKOR", fontFamily = InterFontFamily, color = Color(0xFFD1FAE5), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("$finalScore", fontFamily = DisplaySerifFontFamily, color = Color(0xFF7DD3FC), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                                Text(
                                    text = StoryMiniGameScorer.rankSummaryTr(gameId, finalRank),
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFE2D9F8),
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0x991E1B3A))
                                            .border(1.dp, Color(0x88FFFFFF), RoundedCornerShape(14.dp))
                                            .clickable {
                                                reported = false
                                                phase = MiniGamePhase.PLAYING
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("TEKRAR", fontFamily = InterFontFamily, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1.4f)
                                            .heightIn(min = 48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFF059669))
                                            .border(1.1.dp, Color(0xFF6EE7B7), RoundedCornerShape(14.dp))
                                            .clickable { onDismiss() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("HİKÂYEYE DÖN ▶", fontFamily = InterFontFamily, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SHARED 3D PARTICLE & SHOCKWAVE HELPERS FOR 3D PERSPECTIVE MINI-GAME ARENAS
// ═══════════════════════════════════════════════════════════════════════════════
private class Mini3DBurstParticle(
    var x: Float,
    var y: Float,
    var z: Float,
    val vx: Float,
    val vy: Float,
    val vz: Float,
    val color: Color,
    var age: Float = 0f,
    val life: Float = 0.65f
)

// ═══════════════════════════════════════════════════════════════════════════════
// MINI-GAME 1: BEYAZ GÖZ · 3D NÖRAL REZONANS TÜNELİ (3D PERSPECTIVE RHYTHM LOCK)
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun WhiteEyeResonanceStage(
    soundEngine: Cosmic3DSoundEngine,
    onFinish: (score: Int, maxPossible: Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val targets = remember { listOf("TONY STARK", "STEVE ROGERS", "BRUCE BANNER", "NATASHA ROMANOFF", "WANDA MAXIMOFF") }
    val targetPortraits = remember {
        listOf(
            CharacterPortraitKey.TONY,
            CharacterPortraitKey.STEVE,
            CharacterPortraitKey.BRUCE,
            CharacterPortraitKey.NATASHA,
            CharacterPortraitKey.WANDA
        )
    }
    var stageIdx by remember { mutableIntStateOf(0) }
    var ringRadiusFrac by remember { mutableFloatStateOf(1.0f) } // shrinks 1.0 -> 0.14; golden lock zone ~0.42
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("Altın 3D jiroskop halkasına girince dokun!") }
    var feedbackColor by remember { mutableStateOf(Color(0xFFE0F2FE)) }
    var flashAlpha by remember { mutableFloatStateOf(0f) }
    var shake by remember { mutableFloatStateOf(0f) }
    var tunnelTime by remember { mutableFloatStateOf(0f) }
    var transitionPause by remember { mutableFloatStateOf(0f) }
    var pendingFinish by remember { mutableStateOf(false) }
    val bursts = remember { mutableStateListOf<Mini3DBurstParticle>() }

    fun spawn3DBurst(col: Color, count: Int) {
        repeat(count) {
            val ang = Random.nextFloat() * 6.283f
            val spd = 0.55f + Random.nextFloat() * 1.15f
            bursts.add(
                Mini3DBurstParticle(
                    x = 0f, y = 0f, z = 1.0f,
                    vx = cos(ang) * spd,
                    vy = sin(ang) * spd,
                    vz = -0.6f - Random.nextFloat() * 0.9f,
                    color = col
                )
            )
        }
    }

    fun advanceOrFinish() {
        if (stageIdx + 1 >= targets.size) {
            pendingFinish = true
            transitionPause = 0.38f
        } else {
            transitionPause = 0.28f
        }
    }

    // Single continuous 60fps loop so taps are NEVER dropped between stages & final burst plays cleanly!
    LaunchedEffect(Unit) {
        var lastNs = 0L
        while (true) {
            var shouldExit = false
            withFrameNanos { now ->
                if (lastNs == 0L) { lastNs = now; return@withFrameNanos }
                val dt = ((now - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNs = now
                tunnelTime += dt
                if (flashAlpha > 0f) flashAlpha = (flashAlpha - dt * 2.5f).coerceAtLeast(0f)
                if (shake > 0f) shake = (shake - dt * 4.2f).coerceAtLeast(0f)

                val dead = ArrayList<Mini3DBurstParticle>()
                for (p in bursts) {
                    p.age += dt
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.z = (p.z + p.vz * dt).coerceAtLeast(0.18f)
                    if (p.age >= p.life) dead.add(p)
                }
                if (dead.isNotEmpty()) bursts.removeAll(dead.toSet())

                if (transitionPause > 0f) {
                    transitionPause = (transitionPause - dt).coerceAtLeast(0f)
                    if (transitionPause <= 0f) {
                        if (pendingFinish) {
                            shouldExit = true
                        } else {
                            stageIdx = (stageIdx + 1).coerceAtMost(targets.lastIndex)
                            ringRadiusFrac = 1.0f
                        }
                    }
                    return@withFrameNanos
                }

                val speed = 0.54f + stageIdx * 0.085f
                ringRadiusFrac -= dt * speed
                if (ringRadiusFrac <= 0.14f) {
                    combo = 0
                    shake = 0.75f
                    feedbackText = "KAÇIRILDI · Zihin kıpırdadı!"
                    feedbackColor = Color(0xFFFCA5A5)
                    score += 45
                    soundEngine.play(CosmicSfx.PLAYER_SHIELD_HIT, volume = 0.5f, affectedByTimeSlow = false)
                    advanceOrFinish()
                }
            }
            if (shouldExit) break
        }
        onFinish(score, targets.size * 200)
    }

    val shakeX = if (shake > 0.02f) (Random.nextFloat() - 0.5f) * shake * 18f else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shakeX }
            .pointerInput(Unit) {
                detectTapGestures {
                    if (transitionPause > 0f || pendingFinish) return@detectTapGestures
                    val dist = abs(ringRadiusFrac - 0.42f)
                    when {
                        dist <= 0.09f -> {
                            combo++
                            score += 200
                            feedbackText = "✦ MÜKEMMEL 3D MÜHÜR! +200 (x$combo)"
                            feedbackColor = Color(0xFFFDE047)
                            flashAlpha = 1f
                            spawn3DBurst(Color(0xFFFDE047), 22)
                            soundEngine.play(CosmicSfx.SPARK_COLLECT, volume = 0.9f, affectedByTimeSlow = false)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        dist <= 0.175f -> {
                            combo++
                            score += 135
                            feedbackText = "✓ ZİHİN SABİTLENDİ +135"
                            feedbackColor = Color(0xFF7DD3FC)
                            flashAlpha = 0.65f
                            spawn3DBurst(Color(0xFF38BDF8), 14)
                            soundEngine.play(CosmicSfx.COSMIC_BEAM, volume = 0.7f, affectedByTimeSlow = false)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        else -> {
                            combo = 0
                            shake = 0.65f
                            score += 55
                            feedbackText = "ERKEN / GEÇ · +55"
                            feedbackColor = Color(0xFFFCA5A5)
                            spawn3DBurst(Color(0xFFF43F5E), 10)
                            soundEngine.play(CosmicSfx.LASER_WARNING, volume = 0.55f, affectedByTimeSlow = false)
                        }
                    }
                    advanceOrFinish()
                }
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "ZİHİN ${stageIdx + 1} / ${targets.size} · ${targets[stageIdx]}",
                fontFamily = DisplaySerifFontFamily,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = if (combo > 1) "SKOR: $score · SERİ x$combo 🔥" else "SKOR: $score",
                fontFamily = InterFontFamily,
                color = Color(0xFF7DD3FC),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f
                val maxR = minOf(w, h) * 0.44f

                // 1. 3D PERSPECTIVE NEURAL TUNNEL (Z-projected octagonal depth rings rushing forward)
                val ringCount = 9
                for (rIdx in 0 until ringCount) {
                    val z = 3.2f - ((tunnelTime * 1.4f + rIdx * (3.2f / ringCount)) % 3.0f)
                    val projR = (maxR * 1.25f / z.coerceAtLeast(0.32f)).coerceAtMost(maxOf(w, h) * 0.88f)
                    val alpha = ((3.2f - z) / 3.0f).coerceIn(0.05f, 0.55f)
                    val rot = tunnelTime * 0.45f + rIdx * 0.25f
                    val poly = Path()
                    for (v in 0 until 8) {
                        val a = rot + v * (PI.toFloat() * 2f / 8f)
                        val vx = cx + cos(a) * projR
                        val vy = cy + sin(a) * projR * 0.86f
                        if (v == 0) poly.moveTo(vx, vy) else poly.lineTo(vx, vy)
                    }
                    poly.close()
                    drawPath(poly, Color(0xFF38BDF8).copy(alpha = alpha * 0.55f), style = Stroke(width = 1.6f))
                }

                // 2. 3D PERSPECTIVE RADIAL TUNNEL RAILS (converging to vanishing point)
                for (s in 0 until 8) {
                    val a = tunnelTime * 0.25f + s * (PI.toFloat() * 2f / 8f)
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, Color(0x447DD3FC)),
                            start = Offset(cx, cy),
                            end = Offset(cx + cos(a) * maxR * 1.35f, cy + sin(a) * maxR * 1.35f)
                        ),
                        start = Offset(cx + cos(a) * maxR * 0.14f, cy + sin(a) * maxR * 0.14f),
                        end = Offset(cx + cos(a) * maxR * 1.35f, cy + sin(a) * maxR * 1.35f),
                        strokeWidth = 1.2f
                    )
                }

                // 3. 3D GYROSCOPIC GOLDEN LOCK ORBITS (tilted 3D elliptical planes rotating around target mind!)
                val targetR = maxR * 0.42f
                val tilt1 = abs(cos(tunnelTime * 1.6f)).coerceAtLeast(0.28f)
                val tilt2 = abs(sin(tunnelTime * 1.25f)).coerceAtLeast(0.28f)
                drawOval(
                    color = Color(0x66FDE047),
                    topLeft = Offset(cx - targetR * 1.14f, cy - targetR * 1.14f * tilt1),
                    size = Size(targetR * 2.28f, targetR * 2.28f * tilt1),
                    style = Stroke(width = 2.4f)
                )
                drawOval(
                    color = Color(0x55BAE6FD),
                    topLeft = Offset(cx - targetR * 1.14f * tilt2, cy - targetR * 1.14f),
                    size = Size(targetR * 2.28f * tilt2, targetR * 2.28f),
                    style = Stroke(width = 2.2f)
                )

                // Golden sweet-spot ring band (0.36..0.48)
                drawCircle(
                    color = Color(0x3AFDE047),
                    radius = targetR,
                    center = Offset(cx, cy),
                    style = Stroke(width = maxR * 0.14f)
                )
                drawCircle(
                    color = Color(0xFFFDE047),
                    radius = targetR,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3.2f)
                )

                // Radiant 3D White Eye Core Sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color.White, Color(0xFF7DD3FC).copy(alpha = 0.55f), Color.Transparent),
                        center = Offset(cx - maxR * 0.04f, cy - maxR * 0.04f),
                        radius = maxR * 0.30f
                    ),
                    radius = maxR * 0.30f,
                    center = Offset(cx, cy)
                )

                // 4. CONTRACTING 3D CRYSTAL RESONANCE RING + ORBITING NODES
                val activeR = (maxR * ringRadiusFrac).coerceAtLeast(16f)
                val inSweetSpot = abs(ringRadiusFrac - 0.42f) <= 0.09f
                val ringCol = if (inSweetSpot) Color.White else Color(0xFF38BDF8)
                drawCircle(
                    color = ringCol.copy(alpha = 0.28f),
                    radius = activeR,
                    center = Offset(cx, cy),
                    style = Stroke(width = if (inSweetSpot) 18f else 11f)
                )
                drawCircle(
                    color = ringCol,
                    radius = activeR,
                    center = Offset(cx, cy),
                    style = Stroke(width = if (inSweetSpot) 7.5f else 4.5f)
                )
                for (n in 0 until 6) {
                    val na = -tunnelTime * 2.8f + n * (PI.toFloat() * 2f / 6f)
                    drawCircle(
                        color = if (inSweetSpot) Color(0xFFFDE047) else Color(0xFFBAE6FD),
                        radius = if (inSweetSpot) 6.5f else 4.5f,
                        center = Offset(cx + cos(na) * activeR, cy + sin(na) * activeR)
                    )
                }

                // 5. 3D EXPLODING BURST PARTICLES
                bursts.forEach { p ->
                    val a = (1f - p.age / p.life).coerceIn(0f, 1f)
                    val sx = cx + (p.x / p.z) * maxR
                    val sy = cy + (p.y / p.z) * maxR
                    drawCircle(p.color.copy(alpha = a), radius = (4.5f / p.z).coerceIn(2f, 11f), center = Offset(sx, sy))
                }

                if (flashAlpha > 0.02f) {
                    drawCircle(
                        color = Color.White.copy(alpha = flashAlpha * 0.34f),
                        radius = maxR * (1.12f - flashAlpha * 0.2f),
                        center = Offset(cx, cy)
                    )
                }
            }

            // Active Avenger Target Portrait locked inside the 3D White Eye Neural Core
            CinematicCharacterAvatar(
                key = targetPortraits[stageIdx.coerceIn(0, targetPortraits.lastIndex)],
                size = 54.dp,
                borderWidth = 2.dp,
                borderColor = if (abs(ringRadiusFrac - 0.42f) <= 0.09f) Color(0xFFFDE047) else Color(0xFFBAE6FD),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Text(
            text = feedbackText,
            fontFamily = InterFontFamily,
            color = feedbackColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MINI-GAME 2: BOĞAZ REZONANS ÇEKİRDEĞİ (3D CONTAINMENT REACTOR PHYSICS)
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun BosphorusCoreBalanceStage(
    soundEngine: Cosmic3DSoundEngine,
    onFinish: (score: Int, maxPossible: Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isHolding by remember { mutableStateOf(false) }
    var energyPos by remember { mutableFloatStateOf(0.50f) } // 0 = red overload, 1 = blue overload
    var velocity by remember { mutableFloatStateOf(0f) }
    var targetCenter by remember { mutableFloatStateOf(0.50f) }
    var elapsed by remember { mutableFloatStateOf(0f) }
    var inZoneSeconds by remember { mutableFloatStateOf(0f) }
    var swirlAngle by remember { mutableFloatStateOf(0f) }
    var warningShake by remember { mutableFloatStateOf(0f) }
    val totalDuration = 9.0f

    LaunchedEffect(Unit) {
        var lastNs = 0L
        while (elapsed < totalDuration) {
            withFrameNanos { now ->
                if (lastNs == 0L) { lastNs = now; return@withFrameNanos }
                val dt = ((now - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNs = now
                elapsed += dt
                swirlAngle += dt * 3.6f

                targetCenter = (0.50f + sin(elapsed * 1.45f) * 0.20f + cos(elapsed * 0.7f) * 0.08f).coerceIn(0.24f, 0.76f)

                val accel = if (isHolding) 1.55f else -1.35f
                velocity = (velocity + accel * dt).coerceIn(-0.95f, 0.95f)
                energyPos = (energyPos + velocity * dt).coerceIn(0.04f, 0.96f)
                if (energyPos <= 0.05f || energyPos >= 0.95f) velocity *= -0.3f

                val inSafe = abs(energyPos - targetCenter) <= 0.145f
                if (inSafe) {
                    inZoneSeconds += dt
                    warningShake = (warningShake - dt * 4f).coerceAtLeast(0f)
                } else {
                    warningShake = (warningShake + dt * 2.2f).coerceAtMost(0.55f)
                }
            }
        }
        val finalScore = ((inZoneSeconds / totalDuration) * 1000f).toInt().coerceIn(100, 1000)
        onFinish(finalScore, 1000)
    }

    val progressAnim by animateFloatAsState((elapsed / totalDuration).coerceIn(0f, 1f), tween(80), label = "coreProg")
    val inSafeNow = abs(energyPos - targetCenter) <= 0.145f
    val shakeX = if (warningShake > 0.08f) (Random.nextFloat() - 0.5f) * warningShake * 10f else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shakeX }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isHolding = true
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    soundEngine.play(CosmicSfx.COSMIC_BEAM, volume = 0.35f, affectedByTimeSlow = false)
                    try {
                        waitForUpOrCancellation()
                    } finally {
                        isHolding = false
                    }
                }
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (inSafeNow) "✦ 3D REAKTÖR ÇEKİRDEĞİ DENGEDE!" else "⚠ BASILI TUT / BIRAK — DENGEYE DÖN!",
                fontFamily = DisplaySerifFontFamily,
                color = if (inSafeNow) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "KALAN: ${"%.1f".format((totalDuration - elapsed).coerceAtLeast(0f))} sn · UYUM: %${((inZoneSeconds / totalDuration) * 100f).toInt().coerceIn(0, 100)}",
                fontFamily = InterFontFamily,
                color = Color(0xFFBAE6FD),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val chamberH = (h * 0.80f).coerceAtLeast(160f)
                val chamberTop = (h - chamberH) / 2f
                val chamberBot = chamberTop + chamberH
                val cylRadiusX = minOf(w * 0.28f, 110.dp.toPx())
                val ellipseTilt = 0.28f

                // 1. 3D PERSPECTIVE CONTAINMENT REACTOR BACKGROUND RIBS (Back half of rings)
                for (r in 0..6) {
                    val ry = chamberTop + (r / 6f) * chamberH
                    val ringCol = if (r < 3) Color(0x4438BDF8) else Color(0x44F43F5E)
                    drawOval(
                        color = ringCol,
                        topLeft = Offset(cx - cylRadiusX * 1.25f, ry - cylRadiusX * ellipseTilt),
                        size = Size(cylRadiusX * 2.5f, cylRadiusX * ellipseTilt * 2f),
                        style = Stroke(width = 1.6f)
                    )
                }

                // 2. Z-SORTED 3D DOUBLE-HELIX ORBITING PARTICLES (BACK LAYER: sin(a) < 0)
                for (i in 0 until 26) {
                    val t = i / 26f
                    val a = swirlAngle * 1.4f + t * 11f
                    val zDepth = sin(a)
                    if (zDepth < 0f) {
                        val py = chamberTop + t * chamberH
                        val px = cx + cos(a) * cylRadiusX * 1.15f
                        val scale = 0.65f + (zDepth + 1f) * 0.25f
                        val col = if (i % 2 == 0) Color(0xFF38BDF8) else Color(0xFFF43F5E)
                        drawCircle(col.copy(alpha = 0.42f * scale), radius = 4.2f * scale, center = Offset(px, py))
                    }
                }

                // 3. 3D TRANSLUCENT GLASS REACTOR CYLINDER CORE
                val tubeW = (cylRadiusX * 0.92f).coerceIn(50.dp.toPx(), 74.dp.toPx())
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0x991D4ED8), Color(0xBB0C0724), Color(0x99BE123C))
                    ),
                    topLeft = Offset(cx - tubeW / 2f, chamberTop),
                    size = Size(tubeW, chamberH),
                    cornerRadius = CornerRadius(tubeW / 2f, tubeW / 2f)
                )
                // Specular glass highlight stripe along left edge of 3D cylinder
                drawLine(
                    brush = Brush.verticalGradient(listOf(Color.Transparent, Color(0x66E0F2FE), Color.Transparent)),
                    start = Offset(cx - tubeW * 0.30f, chamberTop + 16f),
                    end = Offset(cx - tubeW * 0.30f, chamberBot - 16f),
                    strokeWidth = 3.2f,
                    cap = StrokeCap.Round
                )

                // 4. 3D MOVING SAFE CONTAINMENT BAND (Glowing 3D perspective collar)
                val safeHalf = chamberH * 0.145f
                val safeCenterY = chamberTop + (1f - targetCenter) * chamberH
                val safeTop = (safeCenterY - safeHalf).coerceAtLeast(chamberTop)
                val safeBot = (safeCenterY + safeHalf).coerceAtMost(chamberBot)
                drawRoundRect(
                    color = if (inSafeNow) Color(0x6634D399) else Color(0x3D38BDF8),
                    topLeft = Offset(cx - tubeW * 0.62f, safeTop),
                    size = Size(tubeW * 1.24f, (safeBot - safeTop).coerceAtLeast(24f)),
                    cornerRadius = CornerRadius(18f, 18f)
                )
                // Top & bottom 3D perspective ellipses framing the safe zone
                val ringAccent = if (inSafeNow) Color(0xFF6EE7B7) else Color(0xFFBAE6FD)
                drawOval(
                    color = ringAccent,
                    topLeft = Offset(cx - tubeW * 0.68f, safeTop - 10f),
                    size = Size(tubeW * 1.36f, 20f),
                    style = Stroke(width = 2.6f)
                )
                drawOval(
                    color = ringAccent,
                    topLeft = Offset(cx - tubeW * 0.68f, safeBot - 10f),
                    size = Size(tubeW * 1.36f, 20f),
                    style = Stroke(width = 2.6f)
                )

                // 5. 3D PLASMA CORE ORB + ELECTRIC TETHER ARCS TO SAFE BAND
                val orbY = chamberTop + (1f - energyPos) * chamberH
                val orbCol = if (isHolding) Color(0xFF38BDF8) else Color(0xFFF43F5E)
                // Energy arc connecting orb toward safe zone center
                val arcPath = Path().apply {
                    moveTo(cx, orbY)
                    val midY = (orbY + safeCenterY) / 2f
                    lineTo(cx + sin(swirlAngle * 5f) * 16f, midY)
                    lineTo(cx, safeCenterY)
                }
                drawPath(arcPath, (if (inSafeNow) Color(0xFF6EE7B7) else orbCol).copy(alpha = 0.75f), style = Stroke(width = 2.5f))

                // 3D lit sphere core
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color.White, orbCol, Color.Transparent),
                        center = Offset(cx, orbY),
                        radius = 34.dp.toPx()
                    ),
                    radius = 34.dp.toPx(),
                    center = Offset(cx, orbY)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color.White, orbCol),
                        center = Offset(cx - 5f, orbY - 5f),
                        radius = 15.dp.toPx()
                    ),
                    radius = 14.dp.toPx(),
                    center = Offset(cx, orbY)
                )

                // 6. Z-SORTED 3D DOUBLE-HELIX ORBITING PARTICLES (FRONT LAYER: sin(a) >= 0)
                for (i in 0 until 26) {
                    val t = i / 26f
                    val a = swirlAngle * 1.4f + t * 11f
                    val zDepth = sin(a)
                    if (zDepth >= 0f) {
                        val py = chamberTop + t * chamberH
                        val px = cx + cos(a) * cylRadiusX * 1.15f
                        val scale = 0.9f + zDepth * 0.55f
                        val col = if (i % 2 == 0) Color(0xFF7DD3FC) else Color(0xFFFDA4AF)
                        drawCircle(col.copy(alpha = 0.85f), radius = 4.6f * scale, center = Offset(px, py))
                    }
                }
            }

            // Aiden (Top-Left · Blue Cosmic Thrust) flanking the 3D containment core
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 6.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                CinematicCharacterAvatar(
                    key = CharacterPortraitKey.AIDEN,
                    size = 46.dp,
                    borderWidth = if (isHolding) 2.4.dp else 1.2.dp,
                    borderColor = if (isHolding) Color(0xFF38BDF8) else Color(0x6638BDF8)
                )
                Text(
                    text = "AİDEN · İTKİ",
                    fontFamily = InterFontFamily,
                    color = if (isHolding) Color(0xFF7DD3FC) else Color(0x99BAE6FD),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Mira (Bottom-Right · Crimson Gravity Pull) flanking the 3D containment core
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                CinematicCharacterAvatar(
                    key = CharacterPortraitKey.MIRA,
                    size = 46.dp,
                    borderWidth = if (!isHolding) 2.4.dp else 1.2.dp,
                    borderColor = if (!isHolding) Color(0xFFF43F5E) else Color(0x66F43F5E)
                )
                Text(
                    text = "MİRA · ÇEKİM",
                    fontFamily = InterFontFamily,
                    color = if (!isHolding) Color(0xFFFDA4AF) else Color(0x99FDA4AF),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x55FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressAnim)
                        .height(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF34D399))))
                )
            }
            Text(
                text = if (isHolding) "⚡ MAVİ İTKİ AKTİF (YUKARI)" else "🔥 KIZIL ÇEKİM AKTİF (AŞAĞI)",
                fontFamily = InterFontFamily,
                color = if (isHolding) Color(0xFF7DD3FC) else Color(0xFFFCA5A5),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MINI-GAME 3: KIRMIZI GÖZ · 3D İRADE MÜHRÜ UÇURUMU (3D DEPTH SHATTER REFLEX)
// ═══════════════════════════════════════════════════════════════════════════════
private class WillpowerSealNode(
    val id: Int,
    val xFrac: Float, // normalized world offset -0.58..0.58
    val yFrac: Float, // normalized world offset -0.52..0.52
    var age: Float = 0f,
    val lifetime: Float = 1.65f
)

@Composable
private fun RedEyeWillpowerStage(
    soundEngine: Cosmic3DSoundEngine,
    onFinish: (score: Int, maxPossible: Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val totalSeals = 8
    var spawnedCount by remember { mutableIntStateOf(0) }
    var sealedCount by remember { mutableIntStateOf(0) }
    var missedCount by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var rageLevel by remember { mutableFloatStateOf(0.25f) }
    var shake by remember { mutableFloatStateOf(0f) }
    var abyssTime by remember { mutableFloatStateOf(0f) }
    val nodes = remember { mutableStateListOf<WillpowerSealNode>() }
    val bursts = remember { mutableStateListOf<Mini3DBurstParticle>() }
    var nextId by remember { mutableIntStateOf(1) }

    fun projectNodeScreen(n: WillpowerSealNode, w: Float, h: Float): Triple<Float, Float, Float> {
        val progress = (n.age / n.lifetime).coerceIn(0f, 1f)
        // Rush from Z = 2.15 toward Z = 0.72 so runes physically fly toward the camera in 3D!
        val z = 2.15f - progress * 1.43f
        val scale = (1.05f / z).coerceIn(0.45f, 1.55f)
        val sx = (w * 0.5f + n.xFrac * w * 0.62f * scale).coerceIn(w * 0.14f, w * 0.86f)
        val sy = (h * 0.5f + n.yFrac * h * 0.56f * scale).coerceIn(h * 0.16f, h * 0.84f)
        return Triple(sx, sy, scale)
    }

    LaunchedEffect(Unit) {
        var lastNs = 0L
        var spawnTimer = 0.25f
        while (sealedCount + missedCount < totalSeals) {
            withFrameNanos { now ->
                if (lastNs == 0L) { lastNs = now; return@withFrameNanos }
                val dt = ((now - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNs = now
                abyssTime += dt
                if (shake > 0f) shake = (shake - dt * 3.5f).coerceAtLeast(0f)

                val deadP = ArrayList<Mini3DBurstParticle>()
                for (p in bursts) {
                    p.age += dt
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.z = (p.z + p.vz * dt).coerceAtLeast(0.2f)
                    if (p.age >= p.life) deadP.add(p)
                }
                if (deadP.isNotEmpty()) bursts.removeAll(deadP.toSet())

                spawnTimer -= dt
                if (spawnTimer <= 0f && spawnedCount < totalSeals && nodes.size < 2) {
                    spawnedCount++
                    spawnTimer = (0.95f - spawnedCount * 0.05f).coerceAtLeast(0.55f)
                    nextId++
                    nodes.add(
                        WillpowerSealNode(
                            id = nextId,
                            xFrac = (Random.nextFloat() - 0.5f) * 1.04f,
                            yFrac = (Random.nextFloat() - 0.5f) * 0.96f,
                            lifetime = (1.75f - spawnedCount * 0.06f).coerceAtLeast(1.18f)
                        )
                    )
                    soundEngine.play(CosmicSfx.LASER_WARNING, volume = 0.45f, affectedByTimeSlow = false)
                }

                val expired = ArrayList<WillpowerSealNode>()
                for (n in nodes) {
                    n.age += dt
                    if (n.age >= n.lifetime) {
                        expired.add(n)
                        missedCount++
                        combo = 0
                        rageLevel = (rageLevel + 0.16f).coerceAtMost(1f)
                        shake = 0.75f
                        soundEngine.play(CosmicSfx.PLAYER_SHIELD_HIT, volume = 0.65f, affectedByTimeSlow = false)
                    }
                }
                nodes.removeAll(expired.toSet())
            }
        }
        // Let the final 3D crystal shatter burst play cleanly for 0.36s before showing results!
        var settle = 0.36f
        while (settle > 0f) {
            withFrameNanos { now ->
                val dt = ((now - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNs = now
                settle -= dt
                abyssTime += dt
                val deadP = ArrayList<Mini3DBurstParticle>()
                for (p in bursts) {
                    p.age += dt
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.z = (p.z + p.vz * dt).coerceAtLeast(0.2f)
                    if (p.age >= p.life) deadP.add(p)
                }
                if (deadP.isNotEmpty()) bursts.removeAll(deadP.toSet())
            }
        }
        onFinish(score, totalSeals * 150)
    }

    val shakeX = if (shake > 0.02f) (Random.nextFloat() - 0.5f) * shake * 16f else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shakeX }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "MÜHÜRLENEN: $sealedCount / $totalSeals · SKOR: $score",
                fontFamily = DisplaySerifFontFamily,
                color = Color.White,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = if (combo > 1) "⚡ İRADE SERİSİ x$combo · Derinlikten fırlayan mühürlere dokun!" else "Derinlikten fırlayan kızıl çatlak mühürlerini kapat!",
                fontFamily = InterFontFamily,
                color = Color(0xFFFCA5A5),
                fontSize = 11.sp,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val h = size.height.toFloat().coerceAtLeast(1f)
                        val hit = nodes.firstOrNull { n ->
                            val (sx, sy, sc) = projectNodeScreen(n, w, h)
                            hypot(tapOffset.x - sx, tapOffset.y - sy) <= (58.dp.toPx() * sc.coerceAtLeast(0.85f))
                        }
                        if (hit != null) {
                            val (hx, hy, _) = projectNodeScreen(hit, w, h)
                            nodes.remove(hit)
                            sealedCount++
                            combo++
                            val speedBonus = ((1f - (hit.age / hit.lifetime)) * 50f).toInt().coerceIn(0, 50)
                            score += 100 + speedBonus
                            rageLevel = (rageLevel - 0.08f).coerceAtLeast(0.08f)
                            // 3D Crystal Shatter Sparks bursting outward from tapped rune
                            val normX = (hx - w * 0.5f) / (w * 0.5f)
                            val normY = (hy - h * 0.5f) / (h * 0.5f)
                            repeat(16) {
                                val ang = Random.nextFloat() * 6.283f
                                val spd = 0.65f + Random.nextFloat() * 1.1f
                                bursts.add(
                                    Mini3DBurstParticle(
                                        x = normX, y = normY, z = 1.0f,
                                        vx = cos(ang) * spd, vy = sin(ang) * spd, vz = -0.5f,
                                        color = if (it % 2 == 0) Color(0xFF38BDF8) else Color(0xFFFDE047)
                                    )
                                )
                            }
                            soundEngine.play(CosmicSfx.CRYSTAL_SHATTER, volume = 0.78f, affectedByTimeSlow = false)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f

                // 1. 3D PERSPECTIVE CRIMSON ABYSS TUNNEL
                drawRect(
                    brush = Brush.radialGradient(
                        listOf(Color(0x28F43F5E), Color(0xFFDC2626).copy(alpha = 0.14f + rageLevel * 0.40f)),
                        center = Offset(cx, cy),
                        radius = maxOf(w, h) * 0.70f
                    )
                )

                // Depth-rushing 3D jagged crimson fracture rings
                for (r in 0 until 7) {
                    val z = 3.0f - ((abyssTime * 1.6f + r * (3.0f / 7f)) % 2.8f)
                    val radius = (minOf(w, h) * 0.58f / z.coerceAtLeast(0.35f)).coerceAtMost(maxOf(w, h) * 0.85f)
                    val alpha = ((3.0f - z) / 2.8f).coerceIn(0.06f, 0.55f)
                    val poly = Path()
                    for (v in 0 until 6) {
                        val a = -abyssTime * 0.65f + v * (PI.toFloat() * 2f / 6f) + r * 0.3f
                        val vx = cx + cos(a) * radius
                        val vy = cy + sin(a) * radius
                        if (v == 0) poly.moveTo(vx, vy) else poly.lineTo(vx, vy)
                    }
                    poly.close()
                    drawPath(poly, Color(0xFFF43F5E).copy(alpha = alpha * (0.45f + rageLevel * 0.4f)), style = Stroke(width = 2.0f))
                }

                // 2. 3D SHATTERED MIRROR CRACKS RADIATING FROM DEPTH
                val crackCount = (rageLevel * 9).toInt().coerceAtLeast(4)
                for (c in 0 until crackCount) {
                    val angle = c * (PI.toFloat() * 2f / crackCount) + sin(abyssTime * 0.6f) * 0.04f
                    val sx = cx + cos(angle) * w * 0.56f
                    val sy = cy + sin(angle) * h * 0.56f
                    val mx = cx + cos(angle + 0.12f) * w * (0.34f - rageLevel * 0.12f)
                    val my = cy + sin(angle + 0.12f) * h * (0.34f - rageLevel * 0.12f)
                    val p = Path().apply {
                        moveTo(sx, sy)
                        lineTo(mx, my)
                    }
                    drawPath(p, Color(0xFFF43F5E).copy(alpha = 0.55f), style = Stroke(width = 2.6f, cap = StrokeCap.Round))
                }

                // 3. 3D PERSPECTIVE-RUSHING WILLPOWER SEAL RUNES (Z: 2.15 -> 0.72)
                nodes.forEach { node ->
                    val (nx, ny, sc) = projectNodeScreen(node, w, h)
                    val remain = (1f - node.age / node.lifetime).coerceIn(0f, 1f)
                    val baseR = (28.dp.toPx() * sc).coerceIn(22.dp.toPx(), 46.dp.toPx())

                    // 3D Perspective trail back toward vanishing center
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, Color(0x88F43F5E)),
                            start = Offset(cx, cy),
                            end = Offset(nx, ny)
                        ),
                        start = Offset(cx + (nx - cx) * 0.45f, cy + (ny - cy) * 0.45f),
                        end = Offset(nx, ny),
                        strokeWidth = 2.2f
                    )

                    // Rotating 3D outer hexagon deadline lock
                    val hexPath = Path()
                    val rot = abyssTime * 3.2f + node.id
                    val outerR = baseR * (1.05f + remain * 0.78f)
                    for (v in 0 until 6) {
                        val a = rot + v * (PI.toFloat() * 2f / 6f)
                        val vx = nx + cos(a) * outerR
                        val vy = ny + sin(a) * outerR
                        if (v == 0) hexPath.moveTo(vx, vy) else hexPath.lineTo(vx, vy)
                    }
                    hexPath.close()
                    drawPath(hexPath, Color(0xFFF43F5E).copy(alpha = 0.90f), style = Stroke(width = 3.2f))

                    // Glowing 3D cyan-white willpower seal core
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.White, Color(0xFF38BDF8), Color(0x88F43F5E)),
                            center = Offset(nx - baseR * 0.14f, ny - baseR * 0.14f),
                            radius = baseR
                        ),
                        radius = baseR,
                        center = Offset(nx, ny)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = baseR * 0.46f,
                        center = Offset(nx, ny),
                        style = Stroke(width = 2.5f)
                    )
                }

                // 4. 3D SHATTER SPARKS
                bursts.forEach { p ->
                    val a = (1f - p.age / p.life).coerceIn(0f, 1f)
                    val sx = cx + (p.x / p.z) * (w * 0.5f)
                    val sy = cy + (p.y / p.z) * (h * 0.5f)
                    drawCircle(p.color.copy(alpha = a), radius = (5.0f / p.z).coerceIn(2f, 10f), center = Offset(sx, sy))
                }
            }
        }
    }
}

