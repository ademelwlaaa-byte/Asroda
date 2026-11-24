package com.example.ui.screens.cosmic3d

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
// MINI-GAME 1: BEYAZ GÖZ · ZİHİN REZONANSI (RHYTHM & FOCUS RING TIMING)
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun WhiteEyeResonanceStage(
    soundEngine: Cosmic3DSoundEngine,
    onFinish: (score: Int, maxPossible: Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val targets = remember { listOf("TONY STARK", "STEVE ROGERS", "BRUCE BANNER", "NATASHA ROMANOFF", "WANDA MAXIMOFF") }
    var stageIdx by remember { mutableIntStateOf(0) }
    var ringRadiusFrac by remember { mutableFloatStateOf(1.0f) } // shrinks 1.0 -> 0.10; sweet spot ~0.36..0.48
    var score by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("Altın halkaya girince dokun!") }
    var feedbackColor by remember { mutableStateOf(Color(0xFFE0F2FE)) }
    var flashAlpha by remember { mutableFloatStateOf(0f) }

    fun advanceOrFinish() {
        if (stageIdx + 1 >= targets.size) {
            onFinish(score, targets.size * 200)
        } else {
            stageIdx++
            ringRadiusFrac = 1.0f
        }
    }

    LaunchedEffect(stageIdx) {
        var lastNs = 0L
        val speed = 0.56f + stageIdx * 0.09f
        while (true) {
            withFrameNanos { now ->
                if (lastNs == 0L) { lastNs = now; return@withFrameNanos }
                val dt = ((now - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNs = now
                if (flashAlpha > 0f) flashAlpha = (flashAlpha - dt * 2.6f).coerceAtLeast(0f)
                ringRadiusFrac -= dt * speed
                if (ringRadiusFrac <= 0.14f) {
                    feedbackText = "KAÇIRILDI · Zihin kıpırdadı!"
                    feedbackColor = Color(0xFFFCA5A5)
                    score += 45
                    soundEngine.play(CosmicSfx.PLAYER_SHIELD_HIT, volume = 0.5f, affectedByTimeSlow = false)
                    advanceOrFinish()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(stageIdx) {
                detectTapGestures {
                    val dist = abs(ringRadiusFrac - 0.42f)
                    when {
                        dist <= 0.085f -> {
                            score += 200
                            feedbackText = "✦ MÜKEMMEL ODAK! +200"
                            feedbackColor = Color(0xFFFDE047)
                            flashAlpha = 1f
                            soundEngine.play(CosmicSfx.SPARK_COLLECT, volume = 0.9f, affectedByTimeSlow = false)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        dist <= 0.17f -> {
                            score += 130
                            feedbackText = "✓ ZİHİN SABİTLENDİ +130"
                            feedbackColor = Color(0xFF7DD3FC)
                            flashAlpha = 0.65f
                            soundEngine.play(CosmicSfx.COSMIC_BEAM, volume = 0.7f, affectedByTimeSlow = false)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        else -> {
                            score += 55
                            feedbackText = "ERKEN / GEÇ · +55"
                            feedbackColor = Color(0xFFFCA5A5)
                            soundEngine.play(CosmicSfx.LASER_WARNING, volume = 0.55f, affectedByTimeSlow = false)
                        }
                    }
                    advanceOrFinish()
                }
            }
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "ZİHİN ${stageIdx + 1} / ${targets.size} · ${targets[stageIdx]}",
                fontFamily = DisplaySerifFontFamily,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "SKOR: $score",
                fontFamily = InterFontFamily,
                color = Color(0xFF7DD3FC),
                fontSize = 12.5.sp,
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
                val cx = size.width / 2f
                val cy = size.height / 2f
                val maxR = minOf(size.width, size.height) * 0.43f

                // Radiant White Eye iris center
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.92f), Color(0xFF7DD3FC).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = maxR * 0.30f
                    ),
                    radius = maxR * 0.30f,
                    center = Offset(cx, cy)
                )

                // Golden sweet-spot ring band (0.36..0.48)
                val targetR = maxR * 0.42f
                drawCircle(
                    color = Color(0x44FDE047),
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

                // Shrinking dynamic resonance ring
                val activeR = (maxR * ringRadiusFrac).coerceAtLeast(16f)
                val inSweetSpot = abs(ringRadiusFrac - 0.42f) <= 0.085f
                drawCircle(
                    color = if (inSweetSpot) Color.White else Color(0xFF38BDF8),
                    radius = activeR,
                    center = Offset(cx, cy),
                    style = Stroke(width = if (inSweetSpot) 9f else 5.5f)
                )

                if (flashAlpha > 0.02f) {
                    drawCircle(
                        color = Color.White.copy(alpha = flashAlpha * 0.35f),
                        radius = maxR * (1.05f - flashAlpha * 0.2f),
                        center = Offset(cx, cy)
                    )
                }
            }
        }

        Text(
            text = feedbackText,
            fontFamily = InterFontFamily,
            color = feedbackColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MINI-GAME 2: BOĞAZ REZONANS ÇEKİRDEĞİ (HOLD-TO-BALANCE ENERGY PHYSICS)
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
    val totalDuration = 9.0f

    LaunchedEffect(Unit) {
        var lastNs = 0L
        while (elapsed < totalDuration) {
            withFrameNanos { now ->
                if (lastNs == 0L) { lastNs = now; return@withFrameNanos }
                val dt = ((now - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNs = now
                elapsed += dt
                swirlAngle += dt * 3.8f

                // Smoothly moving harmonic target band
                targetCenter = (0.50f + sin(elapsed * 1.45f) * 0.20f + cos(elapsed * 0.7f) * 0.08f).coerceIn(0.24f, 0.76f)

                // Mira crimson pull vs Aiden blue hold thrust
                val accel = if (isHolding) 1.55f else -1.35f
                velocity = (velocity + accel * dt).coerceIn(-0.95f, 0.95f)
                energyPos = (energyPos + velocity * dt).coerceIn(0.04f, 0.96f)
                if (energyPos <= 0.05f || energyPos >= 0.95f) velocity *= -0.3f

                val inSafe = abs(energyPos - targetCenter) <= 0.145f
                if (inSafe) {
                    inZoneSeconds += dt
                }
            }
        }
        val finalScore = ((inZoneSeconds / totalDuration) * 1000f).toInt().coerceIn(100, 1000)
        onFinish(finalScore, 1000)
    }

    val progressAnim by animateFloatAsState((elapsed / totalDuration).coerceIn(0f, 1f), tween(80), label = "coreProg")
    val inSafeNow = abs(energyPos - targetCenter) <= 0.145f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        soundEngine.play(CosmicSfx.COSMIC_BEAM, volume = 0.35f, affectedByTimeSlow = false)
                        tryAwaitRelease()
                        isHolding = false
                    }
                )
            }
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (inSafeNow) "✦ ÇEKİRDEK DENGEDE!" else "⚠ BASILI TUT / BIRAK — DENGEYE DÖN!",
                fontFamily = DisplaySerifFontFamily,
                color = if (inSafeNow) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "KALAN SÜRE: ${"%.1f".format((totalDuration - elapsed).coerceAtLeast(0f))} sn · UYUM: %${((inZoneSeconds / totalDuration) * 100f).toInt().coerceIn(0, 100)}",
                fontFamily = InterFontFamily,
                color = Color(0xFFBAE6FD),
                fontSize = 11.5.sp,
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
                val barW = 54.dp.toPx()
                val barH = h * 0.78f
                val barLeft = (w - barW) / 2f
                val barTop = (h - barH) / 2f

                // Ambient swirling Bosphorus twin vortex behind gauge
                val cx = w / 2f
                val cy = h / 2f
                for (i in 0 until 14) {
                    val a = swirlAngle + i * (PI.toFloat() * 2f / 14f)
                    val rad = 70.dp.toPx() + (i % 3) * 26.dp.toPx()
                    val px = cx + cos(a) * rad
                    val py = cy + sin(a) * rad * 0.65f
                    val col = if (i % 2 == 0) Color(0xFF38BDF8) else Color(0xFFF43F5E)
                    drawCircle(col.copy(alpha = 0.55f), radius = 4.5f, center = Offset(px, py))
                }

                // Vertical energy column background (crimson bottom -> blue top)
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0x881D4ED8), Color(0xAA0F0A24), Color(0x88BE123C))),
                    topLeft = Offset(barLeft, barTop),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                )

                // Moving safe zone band
                val safeHalf = barH * 0.145f
                val safeCenterY = barTop + (1f - targetCenter) * barH
                drawRoundRect(
                    color = if (inSafeNow) Color(0x7734D399) else Color(0x4438BDF8),
                    topLeft = Offset(barLeft - 6f, (safeCenterY - safeHalf).coerceAtLeast(barTop)),
                    size = Size(barW + 12f, safeHalf * 2f),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                drawRoundRect(
                    color = if (inSafeNow) Color(0xFF6EE7B7) else Color(0xFFBAE6FD),
                    topLeft = Offset(barLeft - 6f, (safeCenterY - safeHalf).coerceAtLeast(barTop)),
                    size = Size(barW + 12f, safeHalf * 2f),
                    cornerRadius = CornerRadius(14f, 14f),
                    style = Stroke(width = 2.6f)
                )

                // Player-controlled core orb
                val orbY = barTop + (1f - energyPos) * barH
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color.White, if (isHolding) Color(0xFF38BDF8) else Color(0xFFF43F5E), Color.Transparent),
                        center = Offset(cx, orbY),
                        radius = 30.dp.toPx()
                    ),
                    radius = 30.dp.toPx(),
                    center = Offset(cx, orbY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 12.dp.toPx(),
                    center = Offset(cx, orbY)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
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
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MINI-GAME 3: KIRMIZI GÖZ · İRADE MÜCADELESİ (REFLEX SEAL CONTROL)
// ═══════════════════════════════════════════════════════════════════════════════
private class WillpowerSealNode(
    val id: Int,
    val xFrac: Float,
    val yFrac: Float,
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
    var rageLevel by remember { mutableFloatStateOf(0.25f) }
    var shake by remember { mutableFloatStateOf(0f) }
    val nodes = remember { mutableStateListOf<WillpowerSealNode>() }
    var nextId by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        var lastNs = 0L
        var spawnTimer = 0.25f
        while (sealedCount + missedCount < totalSeals) {
            withFrameNanos { now ->
                if (lastNs == 0L) { lastNs = now; return@withFrameNanos }
                val dt = ((now - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNs = now
                if (shake > 0f) shake = (shake - dt * 3.5f).coerceAtLeast(0f)

                spawnTimer -= dt
                if (spawnTimer <= 0f && spawnedCount < totalSeals && nodes.size < 2) {
                    spawnedCount++
                    spawnTimer = (0.95f - spawnedCount * 0.05f).coerceAtLeast(0.55f)
                    nextId++
                    nodes.add(
                        WillpowerSealNode(
                            id = nextId,
                            xFrac = 0.18f + Random.nextFloat() * 0.64f,
                            yFrac = 0.20f + Random.nextFloat() * 0.60f,
                            lifetime = (1.75f - spawnedCount * 0.06f).coerceAtLeast(1.15f)
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
                        rageLevel = (rageLevel + 0.16f).coerceAtMost(1f)
                        shake = 0.7f
                        soundEngine.play(CosmicSfx.PLAYER_SHIELD_HIT, volume = 0.65f, affectedByTimeSlow = false)
                    }
                }
                nodes.removeAll(expired.toSet())
            }
        }
        onFinish(score, totalSeals * 150)
    }

    val shakeX = if (shake > 0.02f) (Random.nextFloat() - 0.5f) * shake * 16f else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shakeX }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "MÜHÜRLENEN: $sealedCount / $totalSeals · SKOR: $score",
                fontFamily = DisplaySerifFontFamily,
                color = Color.White,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kırmızı Göz çatlaklarını büyümeden dokunarak kapat!",
                fontFamily = InterFontFamily,
                color = Color(0xFFFCA5A5),
                fontSize = 11.5.sp
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
                            val dx = tapOffset.x - n.xFrac * w
                            val dy = tapOffset.y - n.yFrac * h
                            hypot(dx, dy) <= 64.dp.toPx()
                        }
                        if (hit != null) {
                            nodes.remove(hit)
                            sealedCount++
                            val speedBonus = ((1f - (hit.age / hit.lifetime)) * 50f).toInt().coerceIn(0, 50)
                            score += 100 + speedBonus
                            rageLevel = (rageLevel - 0.08f).coerceAtLeast(0.08f)
                            soundEngine.play(CosmicSfx.CRYSTAL_SHATTER, volume = 0.78f, affectedByTimeSlow = false)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Crimson Red-Eye vignette pulsing with rageLevel
                drawRect(
                    brush = Brush.radialGradient(
                        listOf(Color.Transparent, Color(0xFFDC2626).copy(alpha = 0.14f + rageLevel * 0.42f)),
                        center = Offset(w / 2f, h / 2f),
                        radius = maxOf(w, h) * 0.68f
                    )
                )

                // Fracture lines radiating from edges when rage is high
                val crackCount = (rageLevel * 8).toInt().coerceAtLeast(2)
                for (c in 0 until crackCount) {
                    val angle = c * (PI.toFloat() * 2f / crackCount)
                    val sx = w / 2f + cos(angle) * w * 0.52f
                    val sy = h / 2f + sin(angle) * h * 0.52f
                    val mx = w / 2f + cos(angle + 0.12f) * w * (0.42f - rageLevel * 0.14f)
                    val my = h / 2f + sin(angle + 0.12f) * h * (0.42f - rageLevel * 0.14f)
                    val p = Path().apply {
                        moveTo(sx, sy)
                        lineTo(mx, my)
                    }
                    drawPath(p, Color(0xFFF43F5E).copy(alpha = 0.45f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                }

                // Active seal nodes
                nodes.forEach { node ->
                    val cx = node.xFrac * w
                    val cy = node.yFrac * h
                    val remain = (1f - node.age / node.lifetime).coerceIn(0f, 1f)
                    val baseR = 34.dp.toPx()

                    // Shrinking deadline halo
                    drawCircle(
                        color = Color(0xFFF43F5E).copy(alpha = 0.85f),
                        radius = baseR * (1.0f + remain * 0.9f),
                        center = Offset(cx, cy),
                        style = Stroke(width = 3.5f)
                    )
                    // Glowing cyan-white seal core
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.White, Color(0xFF38BDF8), Color(0x88F43F5E)),
                            center = Offset(cx, cy),
                            radius = baseR
                        ),
                        radius = baseR,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = baseR * 0.45f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5f)
                    )
                }
            }
        }
    }
}
