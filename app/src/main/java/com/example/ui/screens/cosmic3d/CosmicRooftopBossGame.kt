package com.example.ui.screens.cosmic3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.screens.CharacterPortraitKey
import com.example.ui.screens.CinematicCharacterAvatar
import com.example.ui.screens.ConfigureTrueFullscreenDialogWindow
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

enum class CosmicDuelBossId {
    VAELOR_ROOFTOP,
    MIRROR_ZERO,
    ULTRON_PRIME;

    companion object {
        fun fromTag(raw: String): CosmicDuelBossId = when (raw.trim().uppercase()) {
            "MIRROR_ZERO", "ZERO_DUEL", "AYNA_DUELLOSU" -> MIRROR_ZERO
            "ULTRON_PRIME", "SOKOVIA_ULTRON", "ULTRON_BOSS" -> ULTRON_PRIME
            else -> VAELOR_ROOFTOP
        }
    }
}

enum class CosmicDuelDifficulty(
    val labelTr: String,
    val enemyDamageScale: Float,
    val absorbWindowSec: Float,
    val perfectParrySec: Float,
    val attackIntervalScale: Float
) {
    STORY("Hikâye Odaklı", 0.62f, 1.05f, 0.36f, 1.25f),
    BALANCED("Dengeli", 1.0f, 0.78f, 0.28f, 1.0f),
    COSMIC("Kozmik Meydan", 1.28f, 0.64f, 0.22f, 0.84f)
}

private data class BossDuelProfile(
    val id: CosmicDuelBossId,
    val fullName: String,
    val shortTag: String,
    val portraitKey: CharacterPortraitKey,
    val cardDrawableRes: Int?, // null -> renders custom high-craft Ultron Prime card
    val arenaBgRes: Int,
    val primaryColor: Color,
    val phase1Header: String,
    val phase2Header: String,
    val phase3Header: String,
    val droneLabel: String,
    val introSpeaker: String,
    val introHeader: String,
    val introText: String,
    val p2Speaker: String,
    val p2Header: String,
    val p2Text: String,
    val p3Speaker: String,
    val p3Header: String,
    val p3Text: String,
    val whiteEyeLine: String,
    val redEyeLine: String,
    val victorySpeaker: String,
    val victoryHeader: String,
    val victoryText: String,
    val victoryHeadline: String,
    val victoryDesc: String,
    val startBtnText: String,
    val defeatText: String,
    val moveIntervalP1: Float,
    val moveIntervalP3: Float
)

private fun resolveBossProfile(bossId: CosmicDuelBossId): BossDuelProfile = when (bossId) {
    CosmicDuelBossId.VAELOR_ROOFTOP -> BossDuelProfile(
        id = bossId,
        fullName = "YÜCE HAKEM VAELOR",
        shortTag = "VAELOR",
        portraitKey = CharacterPortraitKey.VAELOR,
        cardDrawableRes = R.drawable.img_char_vaelor,
        arenaBgRes = R.drawable.img_game_rooftop_arena,
        primaryColor = Color(0xFFF43F5E),
        phase1Header = "FAZ I · AVENGERS KULESİ ÇATISI",
        phase2Header = "FAZ II · DRONLAR & SÜPÜREN DALGA",
        phase3Header = "FAZ III · VAELOR SON DİRENİŞ!",
        droneLabel = "DRON İMHA! +220",
        introSpeaker = "NATASHA",
        introHeader = "NATASHA ROMANOFF · TELSİZ",
        introText = "Vaelor çatıya indi Aiden! Zemine dokunup ışınlan, mavi toplarda Emilim basıp geri fırlat!",
        p2Speaker = "VAELOR",
        p2Header = "YÜCE HAKEM VAELOR",
        p2Text = "Diz çök Aiden Blackwood! O mavi kıvılcım Soluk Mühür'e aittir!",
        p3Speaker = "WANDA",
        p3Header = "WANDA MAXIMOFF · TELSİZ",
        p3Text = "Zırhı çatladı Aiden! Ekibin gücü ve Kırmızı Göz'le bu çatı savaşını bitir!",
        whiteEyeLine = "Zamanın bitti, Vaelor!",
        redEyeLine = "Yeter! Manhattan'a dokunamazsın!",
        victorySpeaker = "STEVE",
        victoryHeader = "STEVE ROGERS · TELSİZ",
        victoryText = "Başardın Aiden! Vaelor diz çöktü — Manhattan güvende!",
        victoryHeadline = "✦ VAELOR DİZ ÇÖKTÜ · ZAFER! ✦",
        victoryDesc = "Avengers Kulesi'nin çatısında Vaelor'un dengesini ve kalkanını parçaladın! Hikayenin devamı artık açıldı.",
        startBtnText = "⚔ ÇATI DÜELLOSUNU BAŞLAT ▶",
        defeatText = "Kalk Aiden — pes etmek yok! Mavi toplarda Emilim bas, dalgalardan ışınlanarak sıyrıl!",
        moveIntervalP1 = 2.8f,
        moveIntervalP3 = 2.0f
    )

    CosmicDuelBossId.MIRROR_ZERO -> BossDuelProfile(
        id = bossId,
        fullName = "SIFIR · YARA İZLİ AIDEN",
        shortTag = "SIFIR",
        portraitKey = CharacterPortraitKey.ZERO,
        cardDrawableRes = R.drawable.img_postcredits_twin_aiden,
        arenaBgRes = R.drawable.img_ch1_c2_stealth_terrace,
        primaryColor = Color(0xFF38BDF8),
        phase1Header = "FAZ I · AYNA DÜELLOSU · KULE TERASI",
        phase2Header = "FAZ II · TEKİLLİK PARAZİTİ & YANKI",
        phase3Header = "FAZ III · KIRILMIŞ GELECEK ÇARPIŞMASI!",
        droneLabel = "YANKI KÜRESİ İMHA! +220",
        introSpeaker = "ZERO",
        introHeader = "SIFIR · GELECEKTEN GELEN AIDEN",
        introText = "Beşiği açmalarına izin veremem Aiden! Beni geçmeden o laboratuvara giremezsin!",
        p2Speaker = "ZERO",
        p2Header = "SIFIR · YARA İZLİ AIDEN",
        p2Text = "Her hamleni ezbere biliyorum — çünkü sen benim beş yıl önceki hâlimsin!",
        p3Speaker = "NATASHA",
        p3Header = "NATASHA ROMANOFF · TELSİZ",
        p3Text = "Aiden, o öfkeyle savaşıyor — sen hâlâ insanlığını koruyorsun! Dengesini kır ve durdur onu!",
        whiteEyeLine = "Ben senin geleceğin olmak zorunda değilim!",
        redEyeLine = "Kule'ye ve ekibime dokunma!",
        victorySpeaker = "WANDA",
        victoryHeader = "WANDA MAXIMOFF · TELSİZ",
        victoryText = "Başardın Aiden... Sıfır diz çöktü! Beşik güvende, şimdi gerçeği öğreneceğiz!",
        victoryHeadline = "✦ AYNA DÜELLOSU KAZANILDI! ✦",
        victoryDesc = "Kule terasında kendi karanlık geleceğin Sıfır'ı durdurdun ve Vibranyum Beşiği'ni korudun! Hikaye devam ediyor.",
        startBtnText = "⚔ AYNA DÜELLOSUNU BAŞLAT ▶",
        defeatText = "Sıfır çok hızlı Aiden — mavi yankı toplarında Emilim basıp karşı ışın yolla, kırmızı dalgalardan ışınlan!",
        moveIntervalP1 = 1.9f,
        moveIntervalP3 = 1.35f
    )

    CosmicDuelBossId.ULTRON_PRIME -> BossDuelProfile(
        id = bossId,
        fullName = "VİBRANYUM ULTRON PRİME",
        shortTag = "ULTRON",
        portraitKey = CharacterPortraitKey.ULTRON,
        cardDrawableRes = R.drawable.img_portrait_ultron_prime_1795557413989,
        arenaBgRes = R.drawable.img_postcredits_ruined_nyc,
        primaryColor = Color(0xFFEF4444),
        phase1Header = "FAZ I · SOKOVİA MEYDANI · DEMİR FIRTINA",
        phase2Header = "FAZ II · ULTRON NÖBETÇİLERİ & VİBRANYUM DALGA",
        phase3Header = "FAZ III · MİMAR'IN YANKISI & SON SAVAŞ!",
        droneLabel = "NÖBETÇİ İMHA! +220",
        introSpeaker = "ULTRON",
        introHeader = "ULTRON PRİME · VİBRANYUM ZIRH",
        introText = "Kuklalarım koparıldı ama bu şehir gökten düşecek Aiden Blackwood! İplerim artık yok!",
        p2Speaker = "ULTRON",
        p2Header = "ULTRON PRİME · SOKOVİA",
        p2Text = "Enerji kesicilerimi aşamazsın! Demir bükülmez — ama insan kalbin kırılır!",
        p3Speaker = "ARCHITECT",
        p3Header = "ASIL MİMAR · ULTRON'UN GÖZÜNDEN",
        p3Text = "Kayıp Anahtarım... metal kırılabilir ama tohumum ekildi. Şimdi sınırını göster!",
        whiteEyeLine = "Sokovia'yı düşürmene izin vermeyeceğim!",
        redEyeLine = "Bütün ekibim için — BURADA BİTİYOR ULTRON!",
        victorySpeaker = "VISION",
        victoryHeader = "VİZYON · ZİHİN TAŞI",
        victoryText = "Ultron'un Vibranyum çekirdeği parçalandı Aiden! Birlikte başardık — şehir kurtuluyor!",
        victoryHeadline = "✦ ULTRON PRİME PARÇALANDI · ZAFER! ✦",
        victoryDesc = "Sokovia meydanında Avengers ve Vision ile omuz omuza Vibranyum Ultron Prime'ı devirdin! 2. Sezon finali açıldı.",
        startBtnText = "⚔ ULTRON FİNAL DÜELLOSUNU BAŞLAT ▶",
        defeatText = "Pes etme Aiden! Ultron'un enerji kürelerini Emilim'le geri çevir, Vision ve Avengers desteğini çağır!",
        moveIntervalP1 = 2.3f,
        moveIntervalP3 = 1.65f
    )
}

private enum class RooftopDuelPhase { READY, PLAYING, VICTORY, DEFEAT }

private data class RooftopTelegraphZone(
    val id: Int,
    val lane: Int,
    var timer: Float,
    val maxTimer: Float
)

private data class SweepingWaveAttack(
    val id: Int,
    var xFrac: Float,      // sweeps from 0.0 -> 1.0 or 1.0 -> 0.0
    val dir: Float,        // +1f or -1f
    val speed: Float,
    var hitPlayer: Boolean = false
)

private data class RooftopDrone(
    val id: Int,
    val lane: Int,         // 0 (left) or 4 (right)
    var hp: Int,
    var chargeTimer: Float, // fires sniper shot when reaching 0!
    val maxCharge: Float
)

private data class RooftopProjectile(
    val id: Int,
    val fromPlayer: Boolean,
    val lane: Int,
    val targetDroneId: Int? = null,
    var progress: Float,   // 0f = player ground line, 1f = origin line
    val color: Color,
    val damage: Int,
    val isSuperCounter: Boolean = false,
    val isHeavyFinisher: Boolean = false,
    val originXFrac: Float? = null,
    val originYFrac: Float? = null
)

private data class CombatSpark(
    var xFrac: Float,
    var yFrac: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    var age: Float = 0f,
    val radius: Float = 4f
)

private data class BlinkGhost(
    val lane: Int,
    var age: Float,
    val tint: Color
)

private data class ShockRingFx(
    val xFrac: Float,
    val yFrac: Float,
    val color: Color,
    var age: Float,
    val maxRadiusFrac: Float = 0.42f
)

private data class FloatingCombatText(
    val id: Int,
    val xFrac: Float,
    val yFrac: Float,
    val text: String,
    val color: Color,
    var age: Float = 0f
)

private data class RooftopCommsLine(
    val speakerKey: String,
    val speakerName: String,
    val text: String,
    val id: Int
)

private val LANE_X_FRACS = floatArrayOf(0.14f, 0.32f, 0.50f, 0.68f, 0.86f)

/**
 * AAA Responsive 100% Canon Aiden Blackwood Rooftop Boss Duel on Avengers Tower's shattered rainy helipad.
 *
 * Engineered to fit EVERY Android phone screen with ZERO clipping or HUD overlap via a strict
 * 3-Zone Column layout (Top Compact HUD + weight(1f) BoxWithConstraints Arena + Compact Bottom Deck).
 *
 * Deep Canon Mechanics (ZERO FLYING):
 * - Grounded Quantum Blink Teleportation (5 rooftop lanes + Perfect Blink Bullet-Time Reflex)
 * - Vaelor Posture / Guard Break System (Parry, Perfect Blink, & White Eye fill Posture -> 2x Critical Stagger!)
 * - 3-Hit Kinetic Strike Combo + Destructible Tarikat Attack Drones + Sweeping Crimson Laser Wave!
 * - Drift Energy Absorption Parry (+10 HP blue healing + Super Counter Beam!)
 * - White Eye Psychic Stun (instant Guard Break + 4s paralyze) & Red Eye Molecular Demolition Ultimate
 * - Avengers Team-Up Assists (Steve, Natasha, Tony, Wanda) with live portraits & voice lines!
 */
@Composable
fun CosmicRooftopBossDuelDialog(
    title: String,
    bossId: CosmicDuelBossId = CosmicDuelBossId.VAELOR_ROOFTOP,
    pietroSaved: Boolean = false,
    onVictory: (score: Int, rank: String) -> Unit,
    onDismiss: () -> Unit
) {
    val profile = remember(bossId) { resolveBossProfile(bossId) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    val soundEngine = remember { Cosmic3DSoundEngine(context, enableOrchestralMusic = true) }
    val voiceEngine = remember(soundEngine) {
        CosmicCharacterVoiceEngine(context) { speaking ->
            soundEngine.setVoiceDucking(speaking)
        }
    }

    var isMuted by remember { mutableStateOf(false) }
    var voiceEnabled by remember { mutableStateOf(true) }
    var isPaused by remember { mutableStateOf(false) }
    var victoryReported by remember { mutableStateOf(false) }
    var hitStopTimer by remember { mutableFloatStateOf(0f) }

    // Core Duel State
    var selectedDifficulty by remember { mutableStateOf(CosmicDuelDifficulty.BALANCED) }
    var duelPhase by remember { mutableStateOf(RooftopDuelPhase.READY) }
    var bossHp by remember { mutableIntStateOf(100) }
    var bossPosture by remember { mutableIntStateOf(0) } // 0..100 -> 100 triggers 3.8s CRITICAL STAGGER!
    var bossGuardUp by remember { mutableStateOf(false) }
    var bossStunTimer by remember { mutableFloatStateOf(0f) }
    var bossHitFlash by remember { mutableFloatStateOf(0f) }
    var bossLane by remember { mutableIntStateOf(2) }

    // Boss-Specific Signature States
    var mirrorDecoyLane by remember { mutableStateOf<Int?>(null) }
    var ultronArmorPlates by remember { mutableIntStateOf(if (bossId == CosmicDuelBossId.ULTRON_PRIME) 3 else 0) }
    var beamClashActive by remember { mutableStateOf(false) }
    var beamClashDone by remember { mutableStateOf(false) }
    var beamClashProgress by remember { mutableFloatStateOf(0.45f) }

    var playerLane by remember { mutableIntStateOf(2) }
    var playerHp by remember { mutableIntStateOf(100) }
    var playerHitFlash by remember { mutableFloatStateOf(0f) }
    var playerLungeAnim by remember { mutableFloatStateOf(0f) }
    var playerInvulnTimer by remember { mutableFloatStateOf(0f) }
    var strikeComboStep by remember { mutableIntStateOf(0) } // 0, 1, 2 (3rd hit = Heavy Twin Bolt!)

    // Canon Aiden Power States
    var absorbActiveTimer by remember { mutableFloatStateOf(0f) }
    var perfectParryTimer by remember { mutableFloatStateOf(0f) }
    var absorbCooldown by remember { mutableFloatStateOf(0f) }
    var blinkCooldown by remember { mutableFloatStateOf(0f) }
    var reflexSlowTimer by remember { mutableFloatStateOf(0f) }
    var whiteEyeCharge by remember { mutableIntStateOf(55) }
    var whiteEyeFlashTimer by remember { mutableFloatStateOf(0f) }
    var redEyeCharge by remember { mutableIntStateOf(30) }
    var redEyeChainTimer by remember { mutableFloatStateOf(0f) }
    var supportCharge by remember { mutableIntStateOf(60) }
    var resonanceCharge by remember { mutableIntStateOf(0) } // 0..100 -> Cosmic Resonance Super Ultimate!
    var supportIndex by remember { mutableIntStateOf(0) }
    var supportFxTimer by remember { mutableFloatStateOf(0f) }
    var supportFxHero by remember { mutableStateOf("STEVE") }

    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(1) }
    var maxCombo by remember { mutableIntStateOf(1) }
    var perfectParryCount by remember { mutableIntStateOf(0) }
    var cameraShake by remember { mutableFloatStateOf(0f) }
    var rainTime by remember { mutableFloatStateOf(0f) }

    var phase2Announced by remember { mutableStateOf(false) }
    var phase3Announced by remember { mutableStateOf(false) }
    var victoryDelayTimer by remember { mutableFloatStateOf(0f) }

    var activeComms by remember { mutableStateOf<RooftopCommsLine?>(null) }
    var commsTimer by remember { mutableFloatStateOf(0f) }
    var nextId by remember { mutableIntStateOf(1) }

    val telegraphs = remember { mutableStateListOf<RooftopTelegraphZone>() }
    val sweeps = remember { mutableStateListOf<SweepingWaveAttack>() }
    val drones = remember { mutableStateListOf<RooftopDrone>() }
    val projectiles = remember { mutableStateListOf<RooftopProjectile>() }
    val blinkGhosts = remember { mutableStateListOf<BlinkGhost>() }
    val shockRings = remember { mutableStateListOf<ShockRingFx>() }
    val floatingTexts = remember { mutableStateListOf<FloatingCombatText>() }
    val combatSparks = remember { mutableStateListOf<CombatSpark>() }

    fun spawnSparks(xFrac: Float, yFrac: Float, color: Color, count: Int = 12) {
        if (combatSparks.size > 64) {
            repeat(minOf(count, combatSparks.size)) { combatSparks.removeAt(0) }
        }
        repeat(count) {
            val angle = Random.nextFloat() * 6.283f
            val speed = 0.25f + Random.nextFloat() * 0.55f
            combatSparks.add(
                CombatSpark(
                    xFrac = xFrac,
                    yFrac = yFrac,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed * 0.72f,
                    color = color,
                    age = 0f,
                    radius = 2.8f + Random.nextFloat() * 3.8f
                )
            )
        }
    }

    val rainSeeds = remember {
        List(38) { idx ->
            Triple(
                Random(idx * 37 + 11).nextFloat(),
                Random(idx * 53 + 7).nextFloat(),
                0.75f + Random(idx * 19 + 3).nextFloat() * 0.9f
            )
        }
    }

    fun spawnFloatText(xFrac: Float, yFrac: Float, text: String, color: Color) {
        if (floatingTexts.size > 10) floatingTexts.removeAt(0)
        nextId++
        floatingTexts.add(FloatingCombatText(nextId, xFrac.coerceIn(0.14f, 0.86f), yFrac, text, color))
    }

    fun speakComms(speakerKey: String, speakerName: String, text: String, lineId: String? = null) {
        nextId++
        activeComms = RooftopCommsLine(speakerKey, speakerName, text, nextId)
        commsTimer = 3.8f
        soundEngine.play(CosmicSfx.COMMS_CHIRP, volume = 0.55f, affectedByTimeSlow = false)
        if (voiceEnabled && !isMuted) {
            voiceEngine.speak(speakerKey, text, lineId)
        }
    }

    fun computeRank(): String {
        val total = score + playerHp * 18 + maxCombo * 180
        return when {
            playerHp >= 70 && total >= 4200 -> "S"
            total >= 2600 -> "A"
            else -> "B"
        }
    }

    fun addBossPosture(amount: Int) {
        if (bossStunTimer > 0f || bossHp <= 0) return
        bossPosture = (bossPosture + amount).coerceAtMost(100)
        if (bossPosture >= 100) {
            bossPosture = 0
            bossGuardUp = false
            bossStunTimer = 3.8f
            hitStopTimer = 0.055f
            cameraShake = 0.75f
            telegraphs.clear()
            shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFFDE047), 0f, 0.56f))
            spawnSparks(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFFDE047), 16)
            spawnFloatText(LANE_X_FRACS[bossLane], 0.16f, "✦ DENGE KIRILDI! x2 HASAR!", Color(0xFFFDE047))
            soundEngine.play(CosmicSfx.CRYSTAL_SHATTER, volume = 0.95f, affectedByTimeSlow = false)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun damageBoss(rawAmount: Int, postureAdd: Int = 8, bypassGuard: Boolean = false) {
        if (bossHp <= 0 || victoryDelayTimer > 0f) return
        val isCrit = bossStunTimer > 0f
        val effective = when {
            bossGuardUp && !bypassGuard -> (rawAmount / 3).coerceAtLeast(1)
            isCrit -> (rawAmount * 2.0f).toInt()
            else -> rawAmount
        }
        bossHp = (bossHp - effective).coerceAtLeast(0)
        bossHitFlash = 1f
        score += effective * 32 * combo
        soundEngine.play(CosmicSfx.BOSS_HIT, volume = 0.58f, pan = (LANE_X_FRACS[bossLane] - 0.5f) * 1.4f)

        spawnFloatText(
            xFrac = LANE_X_FRACS[bossLane] + (Random.nextFloat() - 0.5f) * 0.12f,
            yFrac = 0.22f,
            text = when {
                bossGuardUp && !bypassGuard -> "KALKAN -$effective"
                isCrit -> "KRİTİK -$effective!"
                else -> "-$effective"
            },
            color = when {
                bossGuardUp && !bypassGuard -> Color(0xFF93C5FD)
                isCrit -> Color(0xFFFDE047)
                else -> Color.White
            }
        )

        if (postureAdd > 0) addBossPosture(postureAdd)

        // Ultron Prime Breakable Vibranium Armor Plates
        if (bossId == CosmicDuelBossId.ULTRON_PRIME && ultronArmorPlates > 0) {
            val expectedPlates = when {
                bossHp > 70 -> 3
                bossHp > 42 -> 2
                bossHp > 20 -> 1
                else -> 0
            }
            if (expectedPlates < ultronArmorPlates || (bypassGuard && rawAmount >= 18)) {
                ultronArmorPlates = minOf(ultronArmorPlates - 1, expectedPlates).coerceAtLeast(0)
                hitStopTimer = 0.06f
                cameraShake = 0.85f
                shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFFDE047), 0f, 0.54f))
                spawnSparks(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFBAE6FD), 18)
                spawnFloatText(LANE_X_FRACS[bossLane], 0.14f, "✦ VİBRANYUM ZIRH PARÇALANDI!", Color(0xFFFDE047))
                soundEngine.play(CosmicSfx.CRYSTAL_SHATTER, volume = 0.92f, affectedByTimeSlow = false)
            }
        }

        // Ultron Prime Phase 3 Interactive FINAL BEAM CLASH at <= 18% HP!
        if (bossId == CosmicDuelBossId.ULTRON_PRIME && !beamClashDone && !beamClashActive && bossHp in 1..18) {
            beamClashActive = true
            beamClashProgress = 0.44f
            telegraphs.clear()
            sweeps.clear()
            projectiles.clear()
            drones.clear()
            cameraShake = 0.9f
            speakComms(
                "VISION",
                "AVENGERS · FİNAL IŞIN ÇARPIŞMASI!",
                "Hızlı dokun Aiden! Vision, Thor, Wanda ve Tony seninle birlikte Ultron'un çekirdeğini yarıyor!",
                "voice_ultron_beam_clash"
            )
            return
        }

        if (!phase2Announced && bossHp in 31..65) {
            phase2Announced = true
            bossGuardUp = true
            if (bossId == CosmicDuelBossId.MIRROR_ZERO) {
                mirrorDecoyLane = if (bossLane <= 2) 4 else 0
            }
            spawnSparks(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFF43F5E), 18)
            speakComms(
                profile.p2Speaker,
                profile.p2Header,
                profile.p2Text,
                "voice_${bossId.name.lowercase()}_p2"
            )
        } else if (!phase3Announced && bossHp in 1..30) {
            phase3Announced = true
            bossGuardUp = false
            supportCharge = 100
            resonanceCharge = (resonanceCharge + 35).coerceAtMost(100)
            spawnSparks(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFC084FC), 18)
            speakComms(
                profile.p3Speaker,
                profile.p3Header,
                profile.p3Text,
                "voice_${bossId.name.lowercase()}_p3"
            )
        } else if (bossHp <= 0) {
            telegraphs.clear()
            sweeps.clear()
            drones.clear()
            projectiles.clear()
            beamClashActive = false
            mirrorDecoyLane = null
            victoryDelayTimer = 1.45f
            hitStopTimer = 0.08f
            cameraShake = 1.0f
            soundEngine.setTimeSlowActive(false)
            soundEngine.play(CosmicSfx.GENERATOR_EXPLODE, volume = 1.0f, affectedByTimeSlow = false)
            soundEngine.play(CosmicSfx.VICTORY_FANFARE, volume = 0.95f, affectedByTimeSlow = false)
            shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.25f, Color(0xFF38BDF8), 0f, 0.88f))
            shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.25f, Color.White, 0.12f, 0.74f))
            spawnSparks(LANE_X_FRACS[bossLane], 0.24f, Color(0xFF38BDF8), 26)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            // Immediately unlock story progress so even an early dismiss after defeating the boss succeeds!
            if (!victoryReported) {
                victoryReported = true
                onVictory(score, computeRank())
            }
            speakComms(
                profile.victorySpeaker,
                profile.victoryHeader,
                profile.victoryText,
                "voice_${bossId.name.lowercase()}_victory"
            )
        }
    }

    fun damagePlayer(amount: Int) {
        if (playerInvulnTimer > 0f || victoryDelayTimer > 0f || beamClashActive) return
        val scaledDmg = (amount * selectedDifficulty.enemyDamageScale).roundToInt().coerceAtLeast(4)
        playerHp = (playerHp - scaledDmg).coerceAtLeast(0)
        playerHitFlash = 1f
        playerInvulnTimer = 1.15f
        cameraShake = 0.72f
        combo = 1
        spawnFloatText(LANE_X_FRACS[playerLane], 0.68f, "-$scaledDmg CAN", Color(0xFFF87171))
        soundEngine.play(CosmicSfx.PLAYER_SHIELD_HIT, volume = 0.90f, affectedByTimeSlow = false)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (playerHp <= 0) {
            soundEngine.setTimeSlowActive(false)
            duelPhase = RooftopDuelPhase.DEFEAT
            speakComms(
                "NATASHA",
                "NATASHA ROMANOFF · TELSİZ",
                profile.defeatText,
                "voice_${bossId.name.lowercase()}_defeat"
            )
        }
    }

    // Canon Grounded Quantum Blink Teleportation (ZERO FLYING!)
    fun blinkToLane(targetLane: Int) {
        if (duelPhase != RooftopDuelPhase.PLAYING || isPaused || victoryDelayTimer > 0f) return
        val clamped = targetLane.coerceIn(0, 4)
        if (clamped == playerLane || blinkCooldown > 0f) return

        val playerXFrac = LANE_X_FRACS[playerLane]
        val escapingTelegraph = telegraphs.any { it.lane == playerLane && it.timer <= 0.44f }
        val escapingSweep = sweeps.any { abs(it.xFrac - playerXFrac) < 0.14f }

        val departedLane = playerLane
        blinkGhosts.add(BlinkGhost(departedLane, 0f, Color(0xFF38BDF8)))
        shockRings.add(ShockRingFx(playerXFrac, 0.78f, Color(0xFF38BDF8), 0f, 0.22f))

        playerLane = clamped
        blinkCooldown = 0.10f
        playerInvulnTimer = maxOf(playerInvulnTimer, 0.30f)
        shockRings.add(ShockRingFx(LANE_X_FRACS[clamped], 0.78f, Color(0xFFBAE6FD), 0f, 0.20f))
        spawnSparks(LANE_X_FRACS[clamped], 0.78f, Color(0xFF7DD3FC), 7)
        soundEngine.play(CosmicSfx.DASH_BLINK, volume = 0.78f, pan = (LANE_X_FRACS[clamped] - 0.5f) * 1.4f, affectedByTimeSlow = false)
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

        // Mirror Zero signature mechanic: Echo Shadow Strike locks onto your departed lane!
        if (bossId == CosmicDuelBossId.MIRROR_ZERO && bossHp <= 70 && bossStunTimer <= 0f && telegraphs.size < 3 && Random.nextFloat() < 0.42f) {
            nextId++
            telegraphs.add(RooftopTelegraphZone(nextId, departedLane, 0.95f, 0.95f))
            spawnFloatText(LANE_X_FRACS[departedLane], 0.62f, "⚡ ZAMAN YANKISI!", Color(0xFF38BDF8))
        }

        if (escapingTelegraph || escapingSweep) {
            reflexSlowTimer = 1.55f
            soundEngine.setTimeSlowActive(true)
            soundEngine.play(CosmicSfx.TIME_SLOW_ENTER, volume = 0.76f, affectedByTimeSlow = false)
            whiteEyeCharge = (whiteEyeCharge + 18).coerceAtMost(100)
            redEyeCharge = (redEyeCharge + 18).coerceAtMost(100)
            supportCharge = (supportCharge + 14).coerceAtMost(100)
            resonanceCharge = (resonanceCharge + 22).coerceAtMost(100)
            addBossPosture(22)
            combo = (combo + 1).coerceAtMost(8)
            if (combo > maxCombo) maxCombo = combo
            score += 200 * combo
            spawnFloatText(LANE_X_FRACS[clamped], 0.65f, "⚡ KUSURSUZ IŞINLANMA!", Color(0xFF7DD3FC))
        }
    }

    // 3-Hit Canon Drift Kinetic Strike (auto-targets aligned drone or Vaelor!)
    fun triggerCosmicStrike(forcedDroneId: Int? = null) {
        if (duelPhase != RooftopDuelPhase.PLAYING || isPaused || victoryDelayTimer > 0f) return
        val step = strikeComboStep % 3
        strikeComboStep++
        val isFinisher = (step == 2)
        playerLungeAnim = if (isFinisher) 1.0f else 0.65f

        val targetDrone = forcedDroneId?.let { id -> drones.firstOrNull { it.id == id } }
            ?: drones.firstOrNull { abs(it.lane - playerLane) <= 1 }

        nextId++
        projectiles.add(
            RooftopProjectile(
                id = nextId,
                fromPlayer = true,
                lane = playerLane,
                targetDroneId = targetDrone?.id,
                progress = 0.08f,
                color = if (isFinisher) Color(0xFFBAE6FD) else Color(0xFF38BDF8),
                damage = if (isFinisher) 8 else 4,
                isHeavyFinisher = isFinisher
            )
        )
        soundEngine.play(
            if (isFinisher) CosmicSfx.TRIPLE_BURST else CosmicSfx.COSMIC_BEAM,
            volume = if (isFinisher) 0.70f else 0.52f,
            pan = (LANE_X_FRACS[playerLane] - 0.5f) * 1.4f,
            affectedByTimeSlow = false
        )
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    // Drift Form Cosmic Energy Absorption / True Perfect Parry
    fun triggerAbsorbParry() {
        if (duelPhase != RooftopDuelPhase.PLAYING || isPaused || absorbCooldown > 0f || victoryDelayTimer > 0f || beamClashActive) return
        absorbActiveTimer = selectedDifficulty.absorbWindowSec
        perfectParryTimer = selectedDifficulty.perfectParrySec
        absorbCooldown = 1.55f
        shockRings.add(ShockRingFx(LANE_X_FRACS[playerLane], 0.78f, Color(0xFF38BDF8), 0f, 0.30f))
        soundEngine.play(CosmicSfx.COSMIC_BEAM, volume = 0.58f, affectedByTimeSlow = false)
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    // Cosmic Resonance Ultimate Finisher (100% Resonance Bar!)
    fun triggerCosmicResonanceUltimate() {
        if (duelPhase != RooftopDuelPhase.PLAYING || isPaused || resonanceCharge < 100 || victoryDelayTimer > 0f || beamClashActive) return
        resonanceCharge = 0
        hitStopTimer = 0.095f
        playerInvulnTimer = maxOf(playerInvulnTimer, 1.6f)
        cameraShake = 1.0f
        bossGuardUp = false
        mirrorDecoyLane = null
        telegraphs.clear()
        sweeps.clear()
        drones.clear()
        listOf(0, 1, 2, 3, 4).forEachIndexed { idx, ln ->
            blinkGhosts.add(BlinkGhost(ln, idx * 0.08f, Color(0xFFFDE047)))
            shockRings.add(ShockRingFx(LANE_X_FRACS[ln], 0.50f, Color(0xFF38BDF8), idx * 0.06f, 0.55f))
        }
        shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFFDE047), 0f, 0.95f))
        spawnSparks(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFFDE047), 28)
        soundEngine.play(CosmicSfx.WHITE_EYE_NOVA, volume = 1.0f, affectedByTimeSlow = false)
        soundEngine.play(CosmicSfx.AVENGERS_SUPPORT, volume = 0.9f, affectedByTimeSlow = false)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        spawnFloatText(LANE_X_FRACS[bossLane], 0.14f, "✦ KOZMİK REZONANS SÜPER BİTİRİCİ!", Color(0xFFFDE047))
        speakComms("AIDEN", "AIDEN BLACKWOOD · KOZMİK REZONANS", "Mavi enerji, Beyaz Göz ve Kırmızı Göz — tek bir irade!", "voice_resonance_ultimate")
        damageBoss(28, postureAdd = 65, bypassGuard = true)
    }

    // White Eye Psychic Stun (instant Posture Break + destroys drones!)
    fun triggerWhiteEyeStun() {
        if (duelPhase != RooftopDuelPhase.PLAYING || isPaused || whiteEyeCharge < 100 || victoryDelayTimer > 0f) return
        whiteEyeCharge = 0
        whiteEyeFlashTimer = 0.95f
        bossPosture = 0
        bossGuardUp = false
        bossStunTimer = 4.2f
        reflexSlowTimer = maxOf(reflexSlowTimer, 2.1f)
        cameraShake = 0.65f
        telegraphs.clear()
        sweeps.clear()
        shockRings.add(ShockRingFx(LANE_X_FRACS[playerLane], 0.76f, Color.White, 0f, 0.85f))
        shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFE0F2FE), 0f, 0.55f))
        soundEngine.play(CosmicSfx.TIME_SLOW_ENTER, volume = 0.95f, affectedByTimeSlow = false)
        soundEngine.setTimeSlowActive(true)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        hitStopTimer = 0.055f
        spawnSparks(LANE_X_FRACS[bossLane], 0.24f, Color.White, 16)
        spawnFloatText(LANE_X_FRACS[bossLane], 0.16f, "👁 BEYAZ GÖZ · FELÇ!", Color.White)
        speakComms("AIDEN", "AIDEN BLACKWOOD · BEYAZ GÖZ", profile.whiteEyeLine, "voice_${bossId.name.lowercase()}_white")
        damageBoss(7, postureAdd = 0, bypassGuard = true)
    }

    // Red Eye Molecular Kinetic Demolition (3-Blink Rooftop Finisher!)
    fun triggerRedEyeFinisher() {
        if (duelPhase != RooftopDuelPhase.PLAYING || isPaused || redEyeCharge < 100 || victoryDelayTimer > 0f) return
        redEyeCharge = 0
        redEyeChainTimer = 1.25f
        hitStopTimer = 0.07f
        playerInvulnTimer = maxOf(playerInvulnTimer, 1.35f)
        cameraShake = 1.0f
        telegraphs.clear()
        sweeps.clear()
        if (drones.isNotEmpty()) {
            drones.forEach {
                shockRings.add(ShockRingFx(LANE_X_FRACS[it.lane], 0.46f, Color(0xFFF43F5E), 0f, 0.35f))
                spawnSparks(LANE_X_FRACS[it.lane], 0.46f, Color(0xFFF43F5E), 12)
            }
            drones.clear()
        }
        listOf(0, 2, 4).forEachIndexed { i, ln -> blinkGhosts.add(BlinkGhost(ln, i * 0.14f, Color(0xFFF43F5E))) }
        shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.25f, Color(0xFFF43F5E), 0f, 0.90f))
        shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.25f, Color(0xFF38BDF8), 0.10f, 0.72f))
        spawnSparks(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFF43F5E), 22)
        soundEngine.play(CosmicSfx.WHITE_EYE_NOVA, volume = 1.0f, affectedByTimeSlow = false)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        spawnFloatText(LANE_X_FRACS[bossLane], 0.15f, "🔴 KIRMIZI GÖZ YIKIMI!", Color(0xFFF43F5E))
        speakComms("AIDEN", "AIDEN BLACKWOOD · KIRMIZI GÖZ", profile.redEyeLine, "voice_${bossId.name.lowercase()}_red")
        damageBoss(24, postureAdd = 45, bypassGuard = true)
    }

    // Avengers Team-Up Support (includes Vision & Thor in Ultron Prime finale!)
    fun triggerAvengersTeamUp() {
        if (duelPhase != RooftopDuelPhase.PLAYING || isPaused || supportCharge < 100 || victoryDelayTimer > 0f) return
        supportCharge = 0
        val heroes = if (bossId == CosmicDuelBossId.ULTRON_PRIME) {
            if (pietroSaved) listOf("PIETRO", "VISION", "THOR", "WANDA", "STEVE", "NATASHA", "TONY")
            else listOf("VISION", "THOR", "WANDA", "STEVE", "NATASHA", "TONY")
        } else {
            listOf("STEVE", "NATASHA", "TONY", "WANDA")
        }
        val hero = heroes[supportIndex % heroes.size]
        supportIndex++
        supportFxHero = hero
        supportFxTimer = 1.35f
        cameraShake = 0.72f
        soundEngine.play(CosmicSfx.AVENGERS_SUPPORT, volume = 0.92f, affectedByTimeSlow = false)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        drones.clear()

        when (hero) {
            "PIETRO" -> {
                bossGuardUp = false
                telegraphs.clear()
                sweeps.clear()
                bossStunTimer = maxOf(bossStunTimer, 2.5f)
                shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.24f, Color(0xFF7DD3FC), 0f, 0.76f))
                spawnSparks(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFBAE6FD), 20)
                speakComms("PIETRO", "PİETRO MAXİMOFF · GÜMÜŞ ŞİMŞEK", "Bunu görmedin mi Ultron?! Nöbetçileri temizledim — sıra sende Aiden!", "voice_pietro_assist")
                damageBoss(18, postureAdd = 50, bypassGuard = true)
            }
            "VISION" -> {
                bossGuardUp = false
                telegraphs.clear()
                shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFEAB308), 0f, 0.68f))
                speakComms("VISION", "VİZYON · ZİHİN TAŞI IŞINI", "Zihin Taşı'nı odaklıyorum Aiden — zırhı açıldı, şimdi vur!", "voice_vision_assist")
                damageBoss(19, postureAdd = 55, bypassGuard = true)
            }
            "THOR" -> {
                bossStunTimer = maxOf(bossStunTimer, 2.6f)
                shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.25f, Color(0xFF38BDF8), 0f, 0.72f))
                speakComms("THOR", "THOR · MJOLNİR YILDIRIMI", "Asgard'ın fırtınası üstüne insin! Hazır mısın Aiden?", "voice_thor_assist")
                damageBoss(17, postureAdd = 45, bypassGuard = true)
            }
            "STEVE" -> {
                bossGuardUp = false
                telegraphs.clear()
                shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.25f, Color(0xFF60A5FA), 0f, 0.55f))
                speakComms("STEVE", "STEVE ROGERS · KALKAN", "Kalkanımı yolladım Aiden! Gardı kırıldı, şimdi yüklen!", "voice_steve_assist")
                damageBoss(14, postureAdd = 55, bypassGuard = true)
            }
            "NATASHA" -> {
                bossStunTimer = maxOf(bossStunTimer, 2.8f)
                shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.25f, Color(0xFFF472B6), 0f, 0.55f))
                speakComms("NATASHA", "NATASHA ROMANOFF · ŞOK", "Elektrik şokları kilitlendi Aiden! Şimdi ışınlanıp vur!", "voice_natasha_assist")
                damageBoss(13, postureAdd = 35, bypassGuard = true)
            }
            "TONY" -> {
                shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.24f, Color(0xFFFBBF24), 0f, 0.65f))
                speakComms("TONY", "TONY STARK · YÖRÜNGE IŞINI", "Kenara ışınlan evlat — yörünge ışınını tam üstüne indiriyorum!", "voice_tony_assist")
                damageBoss(18, postureAdd = 40, bypassGuard = true)
            }
            else -> {
                redEyeCharge = (redEyeCharge + 45).coerceAtMost(100)
                bossStunTimer = maxOf(bossStunTimer, 2.4f)
                shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.25f, Color(0xFFC084FC), 0f, 0.65f))
                speakComms("WANDA", "WANDA MAXIMOFF · KIZIL KAOS", "Onu kızıl kaosla tuttum Aiden! Kırmızı Göz'ünü serbest bırak!", "voice_wanda_assist")
                damageBoss(14, postureAdd = 40, bypassGuard = true)
            }
        }
    }

    fun startNewDuel() {
        telegraphs.clear()
        sweeps.clear()
        drones.clear()
        projectiles.clear()
        blinkGhosts.clear()
        shockRings.clear()
        floatingTexts.clear()
        combatSparks.clear()
        victoryReported = false
        hitStopTimer = 0f

        bossHp = 100
        bossPosture = 0
        bossGuardUp = false
        bossStunTimer = 0f
        bossHitFlash = 0f
        bossLane = 2
        mirrorDecoyLane = null
        ultronArmorPlates = if (bossId == CosmicDuelBossId.ULTRON_PRIME) 3 else 0
        beamClashActive = false
        beamClashDone = false
        beamClashProgress = 0.45f

        playerLane = 2
        playerHp = 100
        playerHitFlash = 0f
        playerLungeAnim = 0f
        playerInvulnTimer = 0f
        strikeComboStep = 0

        absorbActiveTimer = 0f
        perfectParryTimer = 0f
        absorbCooldown = 0f
        blinkCooldown = 0f
        reflexSlowTimer = 0f
        whiteEyeCharge = 60
        whiteEyeFlashTimer = 0f
        redEyeCharge = 35
        redEyeChainTimer = 0f
        supportCharge = 65
        resonanceCharge = 0
        supportIndex = 0
        supportFxTimer = 0f

        score = 0
        combo = 1
        maxCombo = 1
        perfectParryCount = 0
        cameraShake = 0f
        phase2Announced = false
        phase3Announced = false
        victoryDelayTimer = 0f
        isPaused = false

        soundEngine.setTimeSlowActive(false)
        duelPhase = RooftopDuelPhase.PLAYING

        speakComms(
            profile.introSpeaker,
            profile.introHeader,
            profile.introText,
            "voice_${bossId.name.lowercase()}_intro"
        )
    }

    DisposableEffect(soundEngine, voiceEngine, bossId) {
        soundEngine.start(ambientSceneId = "MANHATTAN_ARMADA", ambientVolume = 0.42f)
        // Pre-render and cinema-master all character battle lines asynchronously so combat audio has ZERO delay!
        val linesToWarm = buildList {
            add(Triple(profile.introSpeaker, profile.introText, "voice_${bossId.name.lowercase()}_intro"))
            add(Triple(profile.p2Speaker, profile.p2Text, "voice_${bossId.name.lowercase()}_p2"))
            add(Triple(profile.p3Speaker, profile.p3Text, "voice_${bossId.name.lowercase()}_p3"))
            add(Triple("STEVE", "Kalkanımı yolladım Aiden! Gardı kırıldı, şimdi yüklen!", "voice_steve_assist"))
            add(Triple("NATASHA", "Elektrik şokları kilitlendi Aiden! Şimdi ışınlanıp vur!", "voice_natasha_assist"))
            add(Triple("TONY", "Kenara ışınlan evlat — yörünge ışınını tam üstüne indiriyorum!", "voice_tony_assist"))
            add(Triple("WANDA", "Onu kızıl kaosla tuttum Aiden! Kırmızı Göz'ünü serbest bırak!", "voice_wanda_assist"))
            if (bossId == CosmicDuelBossId.ULTRON_PRIME) {
                if (pietroSaved) {
                    add(Triple("PIETRO", "Bunu görmedin mi Ultron?! Nöbetçileri temizledim — sıra sende Aiden!", "voice_pietro_assist"))
                }
                add(Triple("VISION", "Zihin Taşı'nı odaklıyorum Aiden — zırhı açıldı, şimdi vur!", "voice_vision_assist"))
                add(Triple("THOR", "Asgard'ın fırtınası üstüne insin! Hazır mısın Aiden?", "voice_thor_assist"))
            }
            add(Triple("AIDEN", profile.whiteEyeLine, "voice_${bossId.name.lowercase()}_white"))
            add(Triple("AIDEN", profile.redEyeLine, "voice_${bossId.name.lowercase()}_red"))
            add(Triple(profile.victorySpeaker, profile.victoryText, "voice_${bossId.name.lowercase()}_victory"))
            add(Triple("NATASHA", profile.defeatText, "voice_${bossId.name.lowercase()}_defeat"))
        }
        voiceEngine.prewarmLines(linesToWarm)
        onDispose {
            soundEngine.stopAndRelease()
            voiceEngine.stopAndRelease()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    soundEngine.setMuted(true)
                    voiceEngine.setMuted(true)
                    if (duelPhase == RooftopDuelPhase.PLAYING) isPaused = true
                }
                Lifecycle.Event.ON_RESUME -> {
                    soundEngine.setMuted(isMuted)
                    voiceEngine.setMuted(isMuted || !voiceEnabled)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 60FPS Deterministic Simulation Loop
    LaunchedEffect(duelPhase, isPaused) {
        if (duelPhase != RooftopDuelPhase.PLAYING || isPaused) return@LaunchedEffect
        var lastNs = 0L
        var bossAttackTimer = 1.25f
        var bossMoveTimer = 2.6f
        var droneSpawnTimer = 4.5f
        var wasReflexSlow = false

        while (duelPhase == RooftopDuelPhase.PLAYING && !isPaused) {
            withFrameNanos { now ->
                if (lastNs == 0L) { lastNs = now; return@withFrameNanos }
                val rawDt = ((now - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNs = now
                rainTime += rawDt

                if (victoryDelayTimer > 0f) {
                    victoryDelayTimer -= rawDt
                    shockRings.forEach { it.age += rawDt * 0.9f }
                    shockRings.removeAll { it.age >= 1f }
                    combatSparks.forEach {
                        it.xFrac += it.vx * rawDt
                        it.yFrac += it.vy * rawDt
                        it.age += rawDt * 1.4f
                    }
                    combatSparks.removeAll { it.age >= 1f }
                    if (victoryDelayTimer <= 0f) {
                        duelPhase = RooftopDuelPhase.VICTORY
                        if (!victoryReported) {
                            victoryReported = true
                            onVictory(score, computeRank())
                        }
                    }
                    return@withFrameNanos
                }

                // Ultron Prime Interactive Final Beam Clash Loop
                if (beamClashActive) {
                    val pushBackSpeed = 0.24f * selectedDifficulty.enemyDamageScale
                    beamClashProgress = (beamClashProgress - rawDt * pushBackSpeed).coerceIn(0.08f, 1.0f)
                    cameraShake = 0.35f
                    if (beamClashProgress >= 1.0f) {
                        beamClashActive = false
                        beamClashDone = true
                        score += 900
                        damageBoss(bossHp + 20, postureAdd = 100, bypassGuard = true)
                    }
                    return@withFrameNanos
                }

                // Micro Hit-Stop crunch on heavy blows & parries (authentic AAA action feel!)
                if (hitStopTimer > 0f) {
                    hitStopTimer = (hitStopTimer - rawDt).coerceAtLeast(0f)
                    return@withFrameNanos
                }

                if (commsTimer > 0f) { commsTimer -= rawDt; if (commsTimer <= 0f) activeComms = null }
                if (absorbActiveTimer > 0f) absorbActiveTimer = (absorbActiveTimer - rawDt).coerceAtLeast(0f)
                if (perfectParryTimer > 0f) perfectParryTimer = (perfectParryTimer - rawDt).coerceAtLeast(0f)
                if (absorbCooldown > 0f) absorbCooldown = (absorbCooldown - rawDt).coerceAtLeast(0f)
                if (blinkCooldown > 0f) blinkCooldown = (blinkCooldown - rawDt).coerceAtLeast(0f)
                if (playerInvulnTimer > 0f) playerInvulnTimer = (playerInvulnTimer - rawDt).coerceAtLeast(0f)
                if (playerLungeAnim > 0f) playerLungeAnim = (playerLungeAnim - rawDt * 5.5f).coerceAtLeast(0f)
                if (bossHitFlash > 0f) bossHitFlash = (bossHitFlash - rawDt * 4f).coerceAtLeast(0f)
                if (playerHitFlash > 0f) playerHitFlash = (playerHitFlash - rawDt * 3f).coerceAtLeast(0f)
                if (whiteEyeFlashTimer > 0f) whiteEyeFlashTimer = (whiteEyeFlashTimer - rawDt).coerceAtLeast(0f)
                if (redEyeChainTimer > 0f) redEyeChainTimer = (redEyeChainTimer - rawDt).coerceAtLeast(0f)
                if (supportFxTimer > 0f) supportFxTimer = (supportFxTimer - rawDt).coerceAtLeast(0f)
                if (cameraShake > 0f) cameraShake = (cameraShake - rawDt * 2.8f).coerceAtLeast(0f)

                if (reflexSlowTimer > 0f) {
                    reflexSlowTimer = (reflexSlowTimer - rawDt).coerceAtLeast(0f)
                    wasReflexSlow = true
                } else if (wasReflexSlow) {
                    wasReflexSlow = false
                    soundEngine.setTimeSlowActive(false)
                    soundEngine.play(CosmicSfx.TIME_SLOW_EXIT, volume = 0.62f, affectedByTimeSlow = false)
                }

                val worldScale = if (reflexSlowTimer > 0f) 0.26f else 1.0f
                val worldDt = rawDt * worldScale

                whiteEyeCharge = (whiteEyeCharge + (if (Random.nextFloat() < rawDt * 3.5f) 1 else 0)).coerceAtMost(100)
                supportCharge = (supportCharge + (if (Random.nextFloat() < rawDt * 4.2f) 1 else 0)).coerceAtMost(100)

                // Phase 2 & 3 Tarikat Attack Drones!
                if (bossHp <= 65 && bossStunTimer <= 0f) {
                    droneSpawnTimer -= worldDt
                    if (droneSpawnTimer <= 0f && drones.size < 2) {
                        droneSpawnTimer = if (bossHp <= 30) 5.5f else 7.2f
                        val lane = if (drones.none { it.lane == 0 }) 0 else 4
                        nextId++
                        drones.add(RooftopDrone(nextId, lane, hp = 8, chargeTimer = 3.2f, maxCharge = 3.2f))
                        shockRings.add(ShockRingFx(LANE_X_FRACS[lane], 0.46f, Color(0xFFF43F5E), 0f, 0.20f))
                    }
                }
                for (dr in drones) {
                    dr.chargeTimer -= worldDt
                    if (dr.chargeTimer <= 0f) {
                        dr.chargeTimer = dr.maxCharge
                        // Drone fires absorbable cyan-crimson bolt straight from its exact barrel!
                        nextId++
                        projectiles.add(
                            RooftopProjectile(
                                id = nextId,
                                fromPlayer = false,
                                lane = playerLane,
                                progress = 0.98f,
                                color = Color(0xFF38BDF8),
                                damage = 12,
                                originXFrac = LANE_X_FRACS[dr.lane],
                                originYFrac = 0.46f
                            )
                        )
                        spawnSparks(LANE_X_FRACS[dr.lane], 0.46f, Color(0xFFF43F5E), 6)
                        soundEngine.play(CosmicSfx.COSMIC_BEAM, volume = 0.48f, pan = (LANE_X_FRACS[dr.lane] - 0.5f) * 1.4f)
                    }
                }

                // Boss AI: Stunned vs 3 Escalating Attack Patterns
                if (bossStunTimer > 0f) {
                    bossStunTimer = (bossStunTimer - rawDt).coerceAtLeast(0f)
                } else {
                    bossMoveTimer -= worldDt
                    if (bossMoveTimer <= 0f) {
                        bossMoveTimer = if (bossHp <= 30) profile.moveIntervalP3 else profile.moveIntervalP1
                        val prevBossLane = bossLane
                        bossLane = (bossLane + (if (Random.nextBoolean()) 1 else -1)).coerceIn(1, 3)
                        if (bossId == CosmicDuelBossId.MIRROR_ZERO && bossLane != prevBossLane) {
                            shockRings.add(ShockRingFx(LANE_X_FRACS[prevBossLane], 0.24f, Color(0xFF38BDF8), 0f, 0.20f))
                            shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.24f, Color(0xFF7DD3FC), 0f, 0.20f))
                        }
                    }

                    bossAttackTimer -= worldDt
                    if (bossAttackTimer <= 0f) {
                        val baseInterval = when {
                            bossHp <= 30 -> 1.25f
                            bossHp <= 65 -> 1.55f
                            else -> 1.85f
                        }
                        bossAttackTimer = baseInterval * selectedDifficulty.attackIntervalScale
                        val roll = Random.nextFloat()
                        when {
                            // Pattern 1: Absorbable Energy Orb (Blue-Silver -> Parry to heal + counter!)
                            roll < 0.40f -> {
                                nextId++
                                projectiles.add(
                                    RooftopProjectile(nextId, fromPlayer = false, lane = playerLane, progress = 0.94f, color = Color(0xFF60A5FA), damage = 15)
                                )
                                soundEngine.play(CosmicSfx.LASER_WARNING, volume = 0.52f)
                            }
                            // Pattern 2 (Phase 2 & 3): Sweeping Rooftop Crimson Wave! (Blink OVER it!)
                            bossHp <= 65 && roll < 0.68f && sweeps.isEmpty() -> {
                                val fromLeft = Random.nextBoolean()
                                nextId++
                                sweeps.add(
                                    SweepingWaveAttack(
                                        id = nextId,
                                        xFrac = if (fromLeft) 0.04f else 0.96f,
                                        dir = if (fromLeft) 1f else -1f,
                                        speed = if (bossHp <= 30) 0.56f else 0.46f
                                    )
                                )
                                soundEngine.play(CosmicSfx.LASER_FIRE, volume = 0.68f)
                            }
                            // Vaelor Signature Pattern: Synchronized Cross-Lane Cage!
                            bossId == CosmicDuelBossId.VAELOR_ROOFTOP && bossHp <= 65 && roll < 0.84f -> {
                                val warn = if (bossHp <= 30) 1.05f else 1.25f
                                val cageLanes = if (playerLane % 2 == 0) listOf(0, 2, 4) else listOf(1, 3)
                                cageLanes.forEach { ln ->
                                    nextId++
                                    telegraphs.add(RooftopTelegraphZone(nextId, ln, warn, warn))
                                }
                                soundEngine.play(CosmicSfx.LASER_WARNING, volume = 0.64f)
                            }
                            // Pattern 3: Ground Slam Crimson Telegraph Rings (Blink out!)
                            else -> {
                                val count = if (bossHp <= 30) 3 else 2
                                val lanes = mutableSetOf(playerLane)
                                while (lanes.size < count) lanes.add(Random.nextInt(0, 5))
                                val warn = if (bossHp <= 30) 1.05f else 1.25f
                                lanes.forEach { ln ->
                                    nextId++
                                    telegraphs.add(RooftopTelegraphZone(nextId, ln, warn, warn))
                                }
                                soundEngine.play(CosmicSfx.LASER_WARNING, volume = 0.58f)
                            }
                        }
                    }
                }

                // Advance Sweeping Crimson Waves
                val deadSweeps = ArrayList<SweepingWaveAttack>()
                val playerXFrac = LANE_X_FRACS[playerLane]
                for (sw in sweeps) {
                    sw.xFrac += sw.dir * sw.speed * worldDt
                    if (!sw.hitPlayer && abs(sw.xFrac - playerXFrac) < 0.07f) {
                        sw.hitPlayer = true
                        damagePlayer(16)
                    }
                    if (sw.xFrac < -0.05f || sw.xFrac > 1.05f) deadSweeps.add(sw)
                }
                sweeps.removeAll(deadSweeps.toSet())

                // Advance Ground Telegraph Zones
                val doneTele = ArrayList<RooftopTelegraphZone>()
                for (tz in telegraphs) {
                    tz.timer -= worldDt
                    if (tz.timer <= 0f) {
                        doneTele.add(tz)
                        shockRings.add(ShockRingFx(LANE_X_FRACS[tz.lane], 0.78f, Color(0xFFF43F5E), 0f, 0.28f))
                        cameraShake = maxOf(cameraShake, 0.34f)
                        soundEngine.play(CosmicSfx.LASER_FIRE, volume = 0.62f, pan = (LANE_X_FRACS[tz.lane] - 0.5f) * 1.4f)
                        if (tz.lane == playerLane) damagePlayer(17)
                    }
                }
                telegraphs.removeAll(doneTele.toSet())

                // Advance Projectiles
                val deadProj = ArrayList<RooftopProjectile>()
                for (p in projectiles) {
                    if (p.fromPlayer) {
                        p.progress += rawDt * 2.0f
                        val targetDr = p.targetDroneId?.let { tid -> drones.firstOrNull { it.id == tid } }
                        if (targetDr != null && p.progress >= 0.54f) {
                            deadProj.add(p)
                            targetDr.hp -= p.damage
                            shockRings.add(ShockRingFx(LANE_X_FRACS[targetDr.lane], 0.46f, Color(0xFF38BDF8), 0f, 0.22f))
                            if (targetDr.hp <= 0) {
                                drones.remove(targetDr)
                                score += 220 * combo
                                redEyeCharge = (redEyeCharge + 20).coerceAtMost(100)
                                whiteEyeCharge = (whiteEyeCharge + 15).coerceAtMost(100)
                                resonanceCharge = (resonanceCharge + 16).coerceAtMost(100)
                                spawnSparks(LANE_X_FRACS[targetDr.lane], 0.46f, Color(0xFF34D399), 15)
                                if (bossId == CosmicDuelBossId.VAELOR_ROOFTOP) {
                                    bossGuardUp = false
                                    addBossPosture(32)
                                    spawnFloatText(LANE_X_FRACS[targetDr.lane], 0.42f, "✦ MÜHÜR KRİSTALİ KIRILDI!", Color(0xFFFDE047))
                                } else {
                                    spawnFloatText(LANE_X_FRACS[targetDr.lane], 0.42f, profile.droneLabel, Color(0xFF34D399))
                                }
                                soundEngine.play(CosmicSfx.CRYSTAL_SHATTER, volume = 0.75f)
                            }
                            continue
                        }
                        if (p.progress >= 0.94f) {
                            deadProj.add(p)
                            shockRings.add(ShockRingFx(LANE_X_FRACS[bossLane], 0.24f, if (p.isSuperCounter) Color.White else p.color, 0f, if (p.isSuperCounter) 0.42f else 0.22f))
                            spawnSparks(LANE_X_FRACS[bossLane], 0.24f, if (p.isSuperCounter) Color.White else p.color, if (p.isSuperCounter || p.isHeavyFinisher) 14 else 7)
                            if (p.isSuperCounter || p.isHeavyFinisher) hitStopTimer = 0.036f
                            whiteEyeCharge = (whiteEyeCharge + 6).coerceAtMost(100)
                            redEyeCharge = (redEyeCharge + 7).coerceAtMost(100)
                            supportCharge = (supportCharge + 6).coerceAtMost(100)
                            resonanceCharge = (resonanceCharge + if (p.isSuperCounter) 16 else 4).coerceAtMost(100)
                            if (bossId == CosmicDuelBossId.MIRROR_ZERO && mirrorDecoyLane != null && p.lane == bossLane) {
                                mirrorDecoyLane = null
                                addBossPosture(18)
                                spawnFloatText(LANE_X_FRACS[bossLane], 0.16f, "✦ AYNA KLONU KIRILDI!", Color(0xFF7DD3FC))
                            }
                            damageBoss(p.damage, postureAdd = if (p.isSuperCounter) 35 else if (p.isHeavyFinisher) 14 else 6, bypassGuard = p.isSuperCounter)
                        }
                    } else {
                        p.progress -= worldDt * 0.90f
                        if (p.progress <= 0.14f) {
                            deadProj.add(p)
                            if (p.lane == playerLane) {
                                if (absorbActiveTimer > 0f) {
                                    val isPerfectParry = perfectParryTimer > 0f
                                    // CANON DRIFT ABSORPTION + PERFECT PARRY x2 SUPER COUNTER!
                                    hitStopTimer = if (isPerfectParry) 0.09f else 0.048f
                                    if (isPerfectParry) {
                                        perfectParryCount++
                                        reflexSlowTimer = maxOf(reflexSlowTimer, 1.25f)
                                        soundEngine.setTimeSlowActive(true)
                                        soundEngine.play(CosmicSfx.TIME_SLOW_ENTER, volume = 0.85f, affectedByTimeSlow = false)
                                    }
                                    val healAmt = if (isPerfectParry) 14 else 10
                                    playerHp = (playerHp + healAmt).coerceAtMost(100)
                                    redEyeCharge = (redEyeCharge + if (isPerfectParry) 32 else 25).coerceAtMost(100)
                                    whiteEyeCharge = (whiteEyeCharge + if (isPerfectParry) 30 else 22).coerceAtMost(100)
                                    supportCharge = (supportCharge + if (isPerfectParry) 24 else 18).coerceAtMost(100)
                                    resonanceCharge = (resonanceCharge + if (isPerfectParry) 30 else 16).coerceAtMost(100)
                                    combo = (combo + if (isPerfectParry) 2 else 1).coerceAtMost(8)
                                    if (combo > maxCombo) maxCombo = combo
                                    score += (if (isPerfectParry) 420 else 240) * combo
                                    shockRings.add(
                                        ShockRingFx(
                                            LANE_X_FRACS[playerLane],
                                            0.78f,
                                            if (isPerfectParry) Color(0xFFFDE047) else Color(0xFF38BDF8),
                                            0f,
                                            if (isPerfectParry) 0.62f else 0.44f
                                        )
                                    )
                                    spawnSparks(LANE_X_FRACS[playerLane], 0.78f, if (isPerfectParry) Color(0xFFFDE047) else Color(0xFF34D399), if (isPerfectParry) 24 else 18)
                                    spawnFloatText(
                                        LANE_X_FRACS[playerLane],
                                        0.66f,
                                        if (isPerfectParry) "✦ MÜKEMMEL EMİLİM! x2 +$healAmt CAN" else "🛡 EMİLDİ! +$healAmt CAN",
                                        if (isPerfectParry) Color(0xFFFDE047) else Color(0xFF34D399)
                                    )
                                    soundEngine.play(CosmicSfx.SPARK_COLLECT, volume = 0.90f, affectedByTimeSlow = false)
                                    soundEngine.play(CosmicSfx.TRIPLE_BURST, volume = 0.88f, affectedByTimeSlow = false)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    nextId++
                                    projectiles.add(
                                        RooftopProjectile(
                                            nextId,
                                            fromPlayer = true,
                                            lane = playerLane,
                                            progress = 0.16f,
                                            color = if (isPerfectParry) Color(0xFFFDE047) else Color(0xFFBAE6FD),
                                            damage = if (isPerfectParry) 22 else 14,
                                            isSuperCounter = true
                                        )
                                    )
                                } else {
                                    damagePlayer(p.damage)
                                }
                            } else {
                                shockRings.add(ShockRingFx(LANE_X_FRACS[p.lane], 0.78f, Color(0xFF60A5FA), 0f, 0.18f))
                            }
                        }
                    }
                }
                projectiles.removeAll(deadProj.toSet())

                // Decay visual effects & spark physics
                blinkGhosts.forEach { it.age += rawDt * 2.5f }
                blinkGhosts.removeAll { it.age >= 1f }
                shockRings.forEach { it.age += rawDt * 2.1f }
                shockRings.removeAll { it.age >= 1f }
                floatingTexts.forEach { it.age += rawDt * 1.15f }
                floatingTexts.removeAll { it.age >= 1f }
                combatSparks.forEach {
                    it.xFrac += it.vx * rawDt
                    it.yFrac += it.vy * rawDt
                    it.age += rawDt * 1.85f
                }
                combatSparks.removeAll { it.age >= 1f }
            }
        }
    }

    val pulseInf = rememberInfiniteTransition(label = "duel_pulse")
    val pulseAlpha by pulseInf.animateFloat(
        initialValue = 0.56f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(560, easing = LinearEasing), RepeatMode.Reverse),
        label = "pAlpha"
    )
    val animatedBossHp by animateFloatAsState(bossHp / 100f, tween(200), label = "bHp")
    val animatedPosture by animateFloatAsState(bossPosture / 100f, tween(180), label = "bPost")
    val animatedPlayerHp by animateFloatAsState(playerHp / 100f, tween(200), label = "pHp")

    val supportPool = if (bossId == CosmicDuelBossId.ULTRON_PRIME) {
        if (pietroSaved) listOf("PIETRO", "VISION", "THOR", "WANDA", "STEVE", "NATASHA", "TONY")
        else listOf("VISION", "THOR", "WANDA", "STEVE", "NATASHA", "TONY")
    } else {
        listOf("STEVE", "NATASHA", "TONY", "WANDA")
    }
    val nextSupportHero = supportPool[supportIndex % supportPool.size]
    val nextSupportKey = CharacterPortraitKey.resolveFromLabel(nextSupportHero) ?: CharacterPortraitKey.STEVE

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
        val shakeX = if (cameraShake > 0.02f) (Random.nextFloat() - 0.5f) * cameraShake * 18f else 0f
        val shakeY = if (cameraShake > 0.02f) (Random.nextFloat() - 0.5f) * cameraShake * 14f else 0f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF04030D))
                .graphicsLayer { translationX = shakeX; translationY = shakeY }
        ) {
            // Fullscreen IMAX Boss Arena Backdrop behind the entire 3-Zone layout
            Image(
                painter = painterResource(id = profile.arenaBgRes),
                contentDescription = profile.fullName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when {
                            redEyeChainTimer > 0f -> Color(0x55991B1B)
                            reflexSlowTimer > 0f -> Color(0x450284C7)
                            playerHitFlash > 0.08f -> Color(0xFFDC2626).copy(alpha = playerHitFlash * 0.42f)
                            else -> Color(0x38040614)
                        }
                    )
            )
            if (whiteEyeFlashTimer > 0.20f) {
                Image(
                    painter = painterResource(id = R.drawable.img_ch1_c1_vision_trace),
                    contentDescription = "Beyaz Göz",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = ((whiteEyeFlashTimer - 0.20f) / 0.75f).coerceIn(0f, 0.72f) }
                )
            }

            // === STRICT 3-ZONE RESPONSIVE LAYOUT (GUARANTEED 100% SCREEN FIT ON EVERY DEVICE!) ===
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 12.dp, bottom = 22.dp)
            ) {
                val isCompactHeight = maxHeight < 710.dp
                Column(modifier = Modifier.fillMaxSize()) {
                    // ZONE 1: COMPACT TOP BOSS & PLAYER HUD
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xD8060516))
                            .padding(horizontal = 10.dp, vertical = if (isCompactHeight) 4.dp else 7.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 3.dp else 5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when {
                                        bossHp > 65 -> profile.phase1Header
                                        bossHp > 30 -> profile.phase2Header
                                        else -> profile.phase3Header
                                    },
                                    fontFamily = InterFontFamily,
                                    color = if (bossHp <= 30) Color(0xFFFCA5A5) else Color(0xFF7DD3FC),
                                    fontSize = if (isCompactHeight) 8.8.sp else 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!isCompactHeight) {
                                    Text(
                                        text = "Zemine dokun: Işınlan · Mavi Top: Emilim · Dronlara tıkla!",
                                        fontFamily = InterFontFamily,
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            DuelHudBtn(if (voiceEnabled && !isMuted) "🎙" else "🔇") {
                                voiceEnabled = !voiceEnabled
                                voiceEngine.setMuted(isMuted || !voiceEnabled)
                            }
                            DuelHudBtn(if (isMuted) "🔇" else "🔊") {
                                isMuted = !isMuted
                                soundEngine.setMuted(isMuted)
                                voiceEngine.setMuted(isMuted || !voiceEnabled)
                            }
                            if (duelPhase == RooftopDuelPhase.PLAYING) {
                                DuelHudBtn(if (isPaused) "▶" else "⏸") { isPaused = !isPaused }
                            }
                            DuelHudBtn("✕") { onDismiss() }
                        }
                    }

                    // Vaelor HP Bar + Posture Break Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(13.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xDD140E2A))
                            .border(0.9.dp, Color(0x99F43F5E), RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedBossHp.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFF43F5E), Color(0xFF38BDF8))))
                        )
                        Text(
                            text = if (bossStunTimer > 0f) "${profile.shortTag} $bossHp% · FELÇ x2 HASAR!" else "${profile.fullName} · $bossHp%",
                            fontFamily = InterFontFamily,
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Aiden HP Bar
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("AIDEN", fontFamily = InterFontFamily, color = Color(0xFF7DD3FC), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            Box(modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50)).background(Color(0xBB1E1B3A))) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedPlayerHp.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF34D399))))
                                )
                            }
                            Text("$playerHp%", fontFamily = InterFontFamily, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        // Boss Posture Gauge
                        Row(modifier = Modifier.weight(0.85f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("DENGE", fontFamily = InterFontFamily, color = Color(0xFFFDE047), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(50)).background(Color(0xBB1E1B3A))) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedPosture.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFFDE047))
                                )
                            }
                        }
                        Text(
                            text = if (combo > 1) "🔥x$combo · $score" else "$score",
                            fontFamily = InterFontFamily,
                            color = Color(0xFFBAE6FD),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Boss-Specific Telemetry Strip (Ultron Vibranium Armor / Mirror Decoy / Cosmic Resonance Ultimate)
                    if (bossId == CosmicDuelBossId.ULTRON_PRIME || mirrorDecoyLane != null || resonanceCharge >= 20) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (bossId == CosmicDuelBossId.ULTRON_PRIME) {
                                Text(
                                    text = "🛡 ZIRH: ${"◆".repeat(ultronArmorPlates)}${"◇".repeat((3 - ultronArmorPlates).coerceAtLeast(0))}",
                                    fontFamily = InterFontFamily,
                                    color = if (ultronArmorPlates > 0) Color(0xFFFCA5A5) else Color(0xFF6EE7B7),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else if (mirrorDecoyLane != null) {
                                Text(
                                    text = "✦ AYNA KLONU AKTİF · GERÇEK SIFIR'I VUR!",
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFF7DD3FC),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("REZONANS", fontFamily = InterFontFamily, color = Color(0xFFFDE047), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(50)).background(Color(0xBB1E1B3A))) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((resonanceCharge / 100f).coerceIn(0f, 1f))
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(50))
                                            .background(Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFFFDE047), Color(0xFFF43F5E))))
                                    )
                                }
                                Text("%$resonanceCharge", fontFamily = InterFontFamily, color = Color(0xFFFDE047), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ZONE 2: DEDICATED UNOBSTRUCTED RESPONSIVE ROOFTOP PLAYFIELD (weight = 1f!)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pointerInput(duelPhase, isPaused) {
                            detectTapGestures { offset ->
                                if (duelPhase != RooftopDuelPhase.PLAYING || isPaused) return@detectTapGestures
                                val xFrac = offset.x / size.width.coerceAtLeast(1)
                                val yFrac = offset.y / size.height.coerceAtLeast(1)
                                // Tap directly on an active drone to blast it!
                                val tappedDrone = drones.firstOrNull {
                                    abs(LANE_X_FRACS[it.lane] - xFrac) < 0.14f && abs(0.46f - yFrac) < 0.14f
                                }
                                if (tappedDrone != null) {
                                    triggerCosmicStrike(forcedDroneId = tappedDrone.id)
                                    return@detectTapGestures
                                }
                                // Tap upper half on Vaelor -> attack! Tap lower half -> Blink to rooftop lane!
                                if (yFrac < 0.42f) {
                                    triggerCosmicStrike()
                                } else {
                                    val laneIdx = (xFrac * 5f).toInt().coerceIn(0, 4)
                                    if (laneIdx == playerLane) triggerCosmicStrike() else blinkToLane(laneIdx)
                                }
                            }
                        }
                        .pointerInput(duelPhase, isPaused) {
                            var accum = 0f
                            detectHorizontalDragGestures(
                                onDragEnd = { accum = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    if (duelPhase == RooftopDuelPhase.PLAYING && !isPaused) {
                                        change.consume()
                                        accum += dragAmount
                                        if (accum > 46f) { accum = 0f; blinkToLane(playerLane + 1) }
                                        else if (accum < -46f) { accum = 0f; blinkToLane(playerLane - 1) }
                                    }
                                }
                            )
                        }
                ) {
                    val density = LocalDensity.current
                    val arenaWpx = with(density) { maxWidth.toPx() }
                    val arenaHpx = with(density) { maxHeight.toPx() }
                    val bossCardW = minOf(maxWidth * 0.21f, maxHeight * 0.19f).coerceIn(54.dp, 94.dp)
                    val bossCardH = (bossCardW * 1.22f).coerceIn(66.dp, 114.dp)
                    val aidenCardW = minOf(maxWidth * 0.19f, maxHeight * 0.18f).coerceIn(52.dp, 88.dp)
                    val aidenCardH = (aidenCardW * 1.22f).coerceIn(64.dp, 108.dp)

                    // 60FPS Live Battlefield Canvas (Rain, Floor Tiles, Shadows, Sweeps, Drones, Beams, Shockwaves)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val bossY = h * 0.24f
                        val bossX = w * LANE_X_FRACS[bossLane]
                        val playerY = h * 0.78f
                        val playerX = w * LANE_X_FRACS[playerLane]

                        // Driving Night Rain
                        val rainSpd = if (reflexSlowTimer > 0f) 0.16f else 1.5f
                        rainSeeds.forEach { (rx, ry, spd) ->
                            val prog = (ry + rainTime * rainSpd * spd) % 1f
                            val sx = ((rx - prog * 0.14f) + 1f) % 1f * w
                            val sy = prog * h
                            drawLine(Color(0x4CBae6FD), Offset(sx, sy), Offset(sx - 5f, sy + 20f), strokeWidth = 1.3f)
                        }

                        // Ground Rooftop Blink Pads + Wet Concrete Shadows
                        for (ln in 0..4) {
                            val lx = w * LANE_X_FRACS[ln]
                            val isCur = ln == playerLane
                            drawOval(
                                color = if (isCur) Color(0xBB38BDF8) else Color(0x4094A3B8),
                                topLeft = Offset(lx - w * 0.065f, playerY + h * 0.10f),
                                size = Size(w * 0.13f, h * 0.032f),
                                style = Stroke(width = if (isCur) 2.5f else 1.2f)
                            )
                        }
                        // Dark realistic puddle cast shadows beneath Vaelor & Aiden
                        drawOval(Color(0x99000000), Offset(bossX - w * 0.11f, bossY + h * 0.12f), Size(w * 0.22f, h * 0.045f))
                        drawOval(Color(0xAA000000), Offset(playerX - w * 0.10f, playerY + h * 0.10f), Size(w * 0.20f, h * 0.038f))

                        // Crimson Ground Slam Telegraph Zones
                        telegraphs.forEach { tz ->
                            val lx = w * LANE_X_FRACS[tz.lane]
                            val u = 1f - (tz.timer / tz.maxTimer).coerceIn(0f, 1f)
                            drawOval(
                                color = Color(0xFFF43F5E).copy(alpha = 0.24f + u * 0.45f),
                                topLeft = Offset(lx - w * 0.09f, playerY + h * 0.04f),
                                size = Size(w * 0.18f, h * 0.09f)
                            )
                            drawOval(
                                color = Color(0xFFFCA5A5),
                                topLeft = Offset(lx - w * 0.09f * u, playerY + h * 0.085f - h * 0.045f * u),
                                size = Size(w * 0.18f * u, h * 0.09f * u),
                                style = Stroke(width = 2.6f)
                            )
                            drawLine(Color(0xFFF43F5E).copy(alpha = 0.25f + u * 0.55f), Offset(bossX, bossY), Offset(lx, playerY + h * 0.08f), strokeWidth = 2f + u * 4f)
                        }

                        // Sweeping Crimson Laser Wall (Phase 2 & 3 — Blink past it!)
                        sweeps.forEach { sw ->
                            val sx = w * sw.xFrac
                            drawLine(Color(0x66F43F5E), Offset(sx, h * 0.30f), Offset(sx, h * 0.96f), strokeWidth = 28f, cap = StrokeCap.Round)
                            drawLine(Color(0xFFF43F5E), Offset(sx, h * 0.30f), Offset(sx, h * 0.96f), strokeWidth = 10f, cap = StrokeCap.Round)
                            drawLine(Color.White, Offset(sx, h * 0.30f), Offset(sx, h * 0.96f), strokeWidth = 3f, cap = StrokeCap.Round)
                        }

                        // Tarikat Attack Drones (Hovering at mid-arena left/right — tap to destroy!)
                        drones.forEach { dr ->
                            val dx = w * LANE_X_FRACS[dr.lane]
                            val dy = h * 0.46f
                            val ch = 1f - (dr.chargeTimer / dr.maxCharge).coerceIn(0f, 1f)
                            drawCircle(Brush.radialGradient(listOf(Color(0xFFF43F5E), Color.Transparent), Offset(dx, dy), 36f), 36f, Offset(dx, dy))
                            drawCircle(Color(0xFF1E293B), 20f, Offset(dx, dy))
                            drawCircle(Color(0xFFFCA5A5), 20f, Offset(dx, dy), style = Stroke(width = 2.5f))
                            drawCircle(Color(0xFFF43F5E).copy(alpha = 0.4f + ch * 0.6f), 7f + ch * 6f, Offset(dx, dy))
                            // Sniper laser sight charging toward Aiden
                            drawLine(Color(0xFFF43F5E).copy(alpha = 0.2f + ch * 0.6f), Offset(dx, dy + 18f), Offset(playerX, playerY), strokeWidth = 1.6f + ch * 2f)
                        }

                        // Quantum Blink Afterimages
                        blinkGhosts.forEach { g ->
                            val gx = w * LANE_X_FRACS[g.lane]
                            val a = (1f - g.age).coerceIn(0f, 1f) * 0.68f
                            drawCircle(Brush.radialGradient(listOf(g.tint.copy(alpha = a), Color.Transparent), Offset(gx, playerY), 48f), 48f, Offset(gx, playerY))
                            drawLine(g.tint.copy(alpha = a), Offset(gx, playerY), Offset(playerX, playerY), strokeWidth = 3.2f, cap = StrokeCap.Round)
                        }

                        // Projectiles (with true origin interpolation for drone sniper bolts!)
                        projectiles.forEach { p ->
                            val targetDr = p.targetDroneId?.let { tid -> drones.firstOrNull { it.id == tid } }
                            val px: Float
                            val py: Float
                            if (p.fromPlayer) {
                                val startX = w * LANE_X_FRACS[p.lane]
                                val endX = if (targetDr != null) w * LANE_X_FRACS[targetDr.lane] else bossX
                                val endY = if (targetDr != null) h * 0.46f else bossY
                                val t = if (targetDr != null) (p.progress / 0.54f).coerceIn(0f, 1f) else p.progress
                                px = startX + (endX - startX) * t
                                py = playerY + (endY - playerY) * t
                            } else {
                                val oX = p.originXFrac?.let { w * it } ?: bossX
                                val oY = p.originYFrac?.let { h * it } ?: bossY
                                val endX = w * LANE_X_FRACS[p.lane]
                                val t = (1f - p.progress).coerceIn(0f, 1f)
                                px = oX + (endX - oX) * t
                                py = oY + (playerY - oY) * t
                            }
                            val r = if (p.isSuperCounter) 26f else if (!p.fromPlayer) 22f else if (p.isHeavyFinisher) 21f else 15f
                            drawCircle(Brush.radialGradient(listOf(Color.White, p.color, Color.Transparent), Offset(px, py), r * 1.65f), r * 1.65f, Offset(px, py))
                            if (!p.fromPlayer) {
                                drawCircle(Color(0xFFBAE6FD), r * 1.18f, Offset(px, py), style = Stroke(width = 2.6f))
                            }
                        }

                        // Parry Sweet-Spot Golden-Cyan Ring around Aiden when an absorbable orb is in window!
                        val orbInParryWindow = projectiles.any { !it.fromPlayer && it.lane == playerLane && it.progress in 0.14f..0.46f }
                        if (orbInParryWindow && absorbActiveTimer <= 0f) {
                            drawCircle(
                                color = Color(0xFFFDE047).copy(alpha = 0.72f * pulseAlpha),
                                radius = 72f,
                                center = Offset(playerX, playerY),
                                style = Stroke(width = 3.6f)
                            )
                        }

                        // Combat Spark Bursts!
                        combatSparks.forEach { sp ->
                            val a = (1f - sp.age).coerceIn(0f, 1f)
                            drawCircle(
                                color = sp.color.copy(alpha = a),
                                radius = sp.radius * (0.4f + a * 0.6f),
                                center = Offset(w * sp.xFrac, h * sp.yFrac)
                            )
                        }

                        // Avengers Support Visual FX (Bespoke FX per Avenger!)
                        if (supportFxTimer > 0f) {
                            val a = (supportFxTimer / 1.35f).coerceIn(0f, 1f)
                            val col = when (supportFxHero) {
                                "STEVE" -> Color(0xFF60A5FA)
                                "NATASHA" -> Color(0xFFF472B6)
                                "TONY" -> Color(0xFFFBBF24)
                                "PIETRO" -> Color(0xFF7DD3FC)
                                "VISION" -> Color(0xFFFDE047)
                                "THOR" -> Color(0xFF38BDF8)
                                else -> Color(0xFFC084FC)
                            }
                            when (supportFxHero) {
                                "TONY" -> {
                                    drawLine(col.copy(alpha = a), Offset(bossX - 20f, 0f), Offset(bossX - 8f, bossY + 18f), strokeWidth = 16f)
                                    drawLine(Color.White.copy(alpha = a), Offset(bossX - 20f, 0f), Offset(bossX - 8f, bossY + 18f), strokeWidth = 5f)
                                    drawLine(col.copy(alpha = a), Offset(bossX + 20f, 0f), Offset(bossX + 8f, bossY + 18f), strokeWidth = 16f)
                                }
                                "VISION" -> {
                                    // Mind-Stone golden beam descending from sky onto Ultron/Boss
                                    drawLine(Color(0xFFFDE047).copy(alpha = a * 0.92f), Offset(bossX, 0f), Offset(bossX, bossY + 24f), strokeWidth = 22f)
                                    drawLine(Color.White.copy(alpha = a), Offset(bossX, 0f), Offset(bossX, bossY + 24f), strokeWidth = 7f)
                                    drawCircle(Color(0xFFFDE047).copy(alpha = a * 0.8f), 42f + (1f - a) * 65f, Offset(bossX, bossY), style = Stroke(width = 5f))
                                }
                                "THOR" -> {
                                    // Branching Asgardian Mjolnir sky-to-boss lightning strike!
                                    val bolt = Path().apply {
                                        moveTo(bossX + 18f, 0f)
                                        lineTo(bossX - 26f, bossY * 0.34f)
                                        lineTo(bossX + 22f, bossY * 0.66f)
                                        lineTo(bossX, bossY + 16f)
                                    }
                                    drawPath(bolt, Color(0xFF38BDF8).copy(alpha = a * 0.9f), style = Stroke(width = 16f, cap = StrokeCap.Round))
                                    drawPath(bolt, Color.White.copy(alpha = a), style = Stroke(width = 5.5f, cap = StrokeCap.Round))
                                    drawCircle(Color(0xFF7DD3FC).copy(alpha = a * 0.8f), 55f + (1f - a) * 100f, Offset(bossX, bossY), style = Stroke(width = 5f))
                                }
                                "PIETRO" -> {
                                    // High-speed Quicksilver silver-cyan sonic slash trails across boss!
                                    drawLine(Color(0xFF38BDF8).copy(alpha = a * 0.9f), Offset(0f, bossY - 45f), Offset(w, bossY + 35f), strokeWidth = 11f, cap = StrokeCap.Round)
                                    drawLine(Color(0xFFE0F2FE).copy(alpha = a), Offset(w, bossY - 25f), Offset(0f, bossY + 48f), strokeWidth = 8f, cap = StrokeCap.Round)
                                    drawCircle(Color(0xFF7DD3FC).copy(alpha = a * 0.75f), 48f + (1f - a) * 90f, Offset(bossX, bossY), style = Stroke(width = 4.5f))
                                }
                                else -> {
                                    drawCircle(col.copy(alpha = a * 0.85f), 70f + (1f - a) * 130f, Offset(bossX, bossY), style = Stroke(width = 6f))
                                }
                            }
                        }

                        // Aiden Absorption Barrier Dome
                        if (absorbActiveTimer > 0f) {
                            drawCircle(Brush.radialGradient(listOf(Color(0x3338BDF8), Color(0xBB7DD3FC), Color.Transparent), Offset(playerX, playerY), 68f), 68f, Offset(playerX, playerY))
                            drawCircle(Color.White, 58f, Offset(playerX, playerY), style = Stroke(width = 3.2f))
                        }

                        // Shockwave Rings
                        shockRings.forEach { sr ->
                            val a = (1f - sr.age).coerceIn(0f, 1f)
                            drawCircle(sr.color.copy(alpha = a * 0.85f), 16f + sr.age * w * sr.maxRadiusFrac, Offset(w * sr.xFrac, h * sr.yFrac), style = Stroke(width = 4f * a + 1f))
                        }
                    }

                    // FULL-FRAME LIVE-ACTION BOSS FIGHTER CARD (100% Screen-Clamped Proportional Positioning!)
                    if (bossHp > 0 && duelPhase == RooftopDuelPhase.PLAYING) {
                        val bWpx = with(density) { bossCardW.toPx() }
                        val bHpx = with(density) { bossCardH.toPx() }
                        val bXpx = (arenaWpx * LANE_X_FRACS[bossLane] - bWpx / 2f)
                            .roundToInt()
                            .coerceIn(6, (arenaWpx - bWpx - 6f).roundToInt().coerceAtLeast(6))
                        val staggerDrop = if (bossStunTimer > 0f) 14 else 0
                        val bYpx = (arenaHpx * 0.24f - bHpx / 2f + staggerDrop)
                            .roundToInt()
                            .coerceIn(6, (arenaHpx - bHpx - 6f).roundToInt().coerceAtLeast(6))
                        val borderCol = when {
                            bossStunTimer > 0f -> Color(0xFFFDE047)
                            bossGuardUp -> Color(0xFF38BDF8)
                            else -> Color(0xFFF43F5E)
                        }

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(bXpx, bYpx) }
                                .width(bossCardW)
                                .height(bossCardH)
                                .graphicsLayer {
                                    scaleX = 1f + bossHitFlash * 0.08f
                                    scaleY = 1f + bossHitFlash * 0.08f
                                }
                                .clip(RoundedCornerShape(18.dp))
                                .border(if (bossGuardUp || bossStunTimer > 0f) 2.4.dp else 1.5.dp, borderCol, RoundedCornerShape(18.dp))
                        ) {
                            if (profile.cardDrawableRes != null) {
                                Image(
                                    painter = painterResource(id = profile.cardDrawableRes),
                                    contentDescription = profile.fullName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Bespoke High-Craft Vibranium Ultron Prime Fighter Portrait
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(listOf(Color(0xFF3F0A0A), Color(0xFF18181B), Color(0xFF09090B)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CinematicCharacterAvatar(
                                        key = CharacterPortraitKey.ULTRON,
                                        size = bossCardW * 0.72f,
                                        borderWidth = 2.dp,
                                        borderColor = if (bossHp <= 30) Color(0xFFFDE047) else Color(0xFFEF4444)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color(0xDD080514)))
                            )
                            Text(
                                text = when {
                                    bossStunTimer > 0f -> "FELÇ ${ceil(bossStunTimer).toInt()}s"
                                    bossGuardUp -> "🛡 KALKAN"
                                    else -> profile.shortTag
                                },
                                fontFamily = InterFontFamily,
                                color = if (bossStunTimer > 0f) Color(0xFFFDE047) else Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 4.dp)
                            )
                        }

                        // MIRROR ZERO PHASE 2 HOLOGRAPHIC DECOY CLONE (Visibly rendered on mirrorDecoyLane!)
                        val activeDecoyLane = mirrorDecoyLane
                        if (bossId == CosmicDuelBossId.MIRROR_ZERO && activeDecoyLane != null && activeDecoyLane != bossLane) {
                            val dXpx = (arenaWpx * LANE_X_FRACS[activeDecoyLane] - bWpx / 2f)
                                .roundToInt()
                                .coerceIn(6, (arenaWpx - bWpx - 6f).roundToInt().coerceAtLeast(6))
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(dXpx, bYpx) }
                                    .width(bossCardW)
                                    .height(bossCardH)
                                    .graphicsLayer { alpha = 0.62f + 0.16f * pulseAlpha }
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.6.dp, Color(0xFF38BDF8), RoundedCornerShape(18.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_postcredits_twin_aiden),
                                    contentDescription = "Sıfır Ayna Klonu",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(listOf(Color(0x5538BDF8), Color(0xAA0C4A6E))))
                                )
                                Text(
                                    text = "✦ AYNA KLONU",
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFE0F2FE),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 4.dp)
                                )
                            }
                        }
                    }

                    // FULL-BODY GROUNDED LIVE-ACTION AIDEN BLACKWOOD FIGHTER (100% Screen-Clamped, ZERO FLYING!)
                    if (duelPhase == RooftopDuelPhase.PLAYING) {
                        val aWpx = with(density) { aidenCardW.toPx() }
                        val aHpx = with(density) { aidenCardH.toPx() }
                        val aXpx = (arenaWpx * LANE_X_FRACS[playerLane] - aWpx / 2f)
                            .roundToInt()
                            .coerceIn(6, (arenaWpx - aWpx - 6f).roundToInt().coerceAtLeast(6))
                        val lungeUp = (playerLungeAnim * 16f).roundToInt()
                        val aYpx = (arenaHpx * 0.78f - aHpx / 2f - lungeUp)
                            .roundToInt()
                            .coerceIn(6, (arenaHpx - aHpx - 6f).roundToInt().coerceAtLeast(6))
                        val orbClose = projectiles.any { !it.fromPlayer && it.lane == playerLane && it.progress in 0.14f..0.46f }
                        val aBorder = when {
                            redEyeChainTimer > 0f -> Color(0xFFF43F5E)
                            whiteEyeFlashTimer > 0f -> Color.White
                            absorbActiveTimer > 0f -> Color(0xFF34D399)
                            orbClose -> Color(0xFFFDE047)
                            else -> Color(0xFF38BDF8)
                        }

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(aXpx, aYpx) }
                                .width(aidenCardW)
                                .height(aidenCardH)
                                .graphicsLayer {
                                    alpha = if (playerInvulnTimer > 0f && ((rainTime * 20f).toInt() % 2 == 0)) 0.55f else 1f
                                    scaleX = 1f + playerLungeAnim * 0.06f
                                    scaleY = 1f + playerLungeAnim * 0.06f
                                }
                                .clip(RoundedCornerShape(18.dp))
                                .border(if (absorbActiveTimer > 0f || orbClose) 2.5.dp else 1.5.dp, aBorder, RoundedCornerShape(18.dp))
                        ) {
                            val aidenSpriteRes = when {
                                redEyeChainTimer > 0f -> R.drawable.img_anim_mutlak_uyanis
                                whiteEyeFlashTimer > 0f -> R.drawable.img_ch1_c1_vision_trace
                                else -> R.drawable.img_aiden_portrait
                            }
                            Image(
                                painter = painterResource(id = aidenSpriteRes),
                                contentDescription = "Aiden Blackwood",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Dynamic Eye/Power State Aura Tint (Blue Drift / White Eye / Red Eye)
                            val powerTint = when {
                                redEyeChainTimer > 0f -> Color(0x55F43F5E)
                                whiteEyeFlashTimer > 0f -> Color(0x55FFFFFF)
                                absorbActiveTimer > 0f -> Color(0x4438BDF8)
                                else -> Color.Transparent
                            }
                            if (powerTint != Color.Transparent) {
                                Box(modifier = Modifier.fillMaxSize().background(powerTint))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(0.58f to Color.Transparent, 1f to Color(0xDD05081A)))
                            )
                            Text(
                                text = when {
                                    absorbActiveTimer > 0f -> "🛡 EMİLİM!"
                                    orbClose -> "⚡ EMİLİM BAS!"
                                    redEyeChainTimer > 0f -> "🔴 KIRMIZI GÖZ"
                                    whiteEyeFlashTimer > 0f -> "👁 BEYAZ GÖZ"
                                    else -> "AIDEN"
                                },
                                fontFamily = InterFontFamily,
                                color = if (orbClose) Color(0xFFFDE047) else Color(0xFFBAE6FD),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 4.dp)
                            )
                        }
                    }

                    // Avengers Support Cinematic Cut-In Strip across Mid-Arena!
                    if (supportFxTimer > 0.15f && duelPhase == RooftopDuelPhase.PLAYING) {
                        val heroKey = CharacterPortraitKey.resolveFromLabel(supportFxHero) ?: CharacterPortraitKey.STEVE
                        val cutAlpha = (supportFxTimer / 1.1f).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .graphicsLayer { alpha = cutAlpha; scaleY = 0.92f + cutAlpha * 0.08f }
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(heroKey.accentColor.copy(alpha = 0.88f), Color(0xE80A071E), heroKey.accentColor.copy(alpha = 0.45f))
                                    )
                                )
                                .border(1.4.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CinematicCharacterAvatar(key = heroKey, size = 40.dp, borderWidth = 1.6.dp, borderColor = Color.White)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("✦ AVENGERS TAKIM SALDIRISI", fontFamily = InterFontFamily, color = Color(0xFFFEF08A), fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                Text(heroKey.displayName.uppercase(), fontFamily = DisplaySerifFontFamily, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Floating Rising Combat Damage & Event Numbers! (Reads rainTime so it animates smoothly at 60fps!)
                    val animTick = rainTime
                    if (animTick >= 0f) {
                        floatingTexts.forEach { ft ->
                            val fx = (arenaWpx * ft.xFrac.coerceIn(0.18f, 0.82f) - 70f)
                                .roundToInt()
                                .coerceIn(12, (arenaWpx - 150f).roundToInt().coerceAtLeast(16))
                            val fy = (arenaHpx * (ft.yFrac - ft.age * 0.13f)).roundToInt()
                            Text(
                                text = ft.text,
                                fontFamily = InterFontFamily,
                                color = ft.color.copy(alpha = (1f - ft.age).coerceIn(0f, 1f)),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.offset { IntOffset(fx, fy) }
                            )
                        }
                    }

                    // Compact Floating Avengers Voiced Comms Pill (pinned cleanly at very top of playfield)
                    val comms = activeComms
                    androidx.compose.animation.AnimatedVisibility(
                        visible = comms != null && duelPhase == RooftopDuelPhase.PLAYING,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        if (comms != null) {
                            val key = CharacterPortraitKey.resolveFromLabel(comms.speakerKey) ?: CharacterPortraitKey.NATASHA
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xE80A0922))
                                    .border(1.dp, key.accentColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CinematicCharacterAvatar(key = key, size = 32.dp, borderWidth = 1.2.dp, borderColor = key.accentColor)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("🎙 ${comms.speakerName}", fontFamily = InterFontFamily, color = key.accentColor, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(comms.text, fontFamily = InterFontFamily, color = Color.White, fontSize = 10.5.sp, lineHeight = 14.sp, maxLines = 2)
                                }
                            }
                        }
                    }
                }

                // ZONE 3: COMPACT BOTTOM CANON POWER DECK (GUARANTEED 100% FIT ON EVERY SCREEN!)
                if (duelPhase == RooftopDuelPhase.PLAYING && !isPaused) {
                    val supportReady = supportCharge >= 100
                    val whiteReady = whiteEyeCharge >= 100
                    val redReady = redEyeCharge >= 100
                    val parryReady = absorbCooldown <= 0.05f

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xEC060516))
                            .padding(horizontal = 8.dp, vertical = if (isCompactHeight) 5.dp else 7.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 4.dp else 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(if (supportReady) nextSupportKey.accentColor.copy(alpha = 0.34f * pulseAlpha) else Color(0xCC110B28))
                                    .border(if (supportReady) 1.3.dp else 0.85.dp, if (supportReady) nextSupportKey.accentColor else Color(0x5594A3B8), RoundedCornerShape(11.dp))
                                    .clickable(enabled = supportReady) { triggerAvengersTeamUp() }
                                    .padding(horizontal = 8.dp, vertical = if (isCompactHeight) 4.dp else 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CinematicCharacterAvatar(key = nextSupportKey, size = if (isCompactHeight) 22.dp else 25.dp, borderWidth = 1.dp, borderColor = nextSupportKey.accentColor)
                                Text(
                                    text = if (supportReady) {
                                        "📡 ${nextSupportKey.displayName.uppercase()} ÇAĞIR!"
                                    } else {
                                        "📡 ${nextSupportKey.displayName} · %$supportCharge"
                                    },
                                    fontFamily = InterFontFamily,
                                    color = if (supportReady) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 9.2.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (resonanceCharge >= 100) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(Brush.horizontalGradient(listOf(Color(0xEE2563EB), Color(0xEED97706), Color(0xEEDC2626))))
                                        .border(1.3.dp, Color(0xFFFDE047), RoundedCornerShape(11.dp))
                                        .clickable { triggerCosmicResonanceUltimate() }
                                        .padding(horizontal = 8.dp, vertical = if (isCompactHeight) 5.dp else 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✦ SÜPER REZONANS! ✦",
                                        fontFamily = InterFontFamily,
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            DuelPowerBtn(
                                title = "⚡ DARBE",
                                sub = "KOMBO ${(strikeComboStep % 3) + 1}/3",
                                enabled = true,
                                bg = Color(0xDD1D4ED8),
                                border = Color(0xFF7DD3FC),
                                compact = isCompactHeight,
                                modifier = Modifier.weight(1f)
                            ) { triggerCosmicStrike() }

                            val parrySweetSpot = projectiles.any { !it.fromPlayer && it.lane == playerLane && it.progress in 0.14f..0.46f }
                            DuelPowerBtn(
                                title = "🛡 EMİLİM",
                                sub = when {
                                    parrySweetSpot && parryReady -> "ŞİMDİ BAS!"
                                    parryReady -> "PARRY"
                                    else -> "${ceil(absorbCooldown).toInt()}s"
                                },
                                enabled = parryReady,
                                bg = when {
                                    absorbActiveTimer > 0f -> Color(0xEE059669)
                                    parrySweetSpot && parryReady -> Color(0xEED97706).copy(alpha = pulseAlpha)
                                    parryReady -> Color(0xDD0369A1)
                                    else -> Color(0xCC1E1B3A)
                                },
                                border = if (parrySweetSpot && parryReady) Color(0xFFFDE047) else if (parryReady) Color(0xFF38BDF8) else Color(0x5594A3B8),
                                compact = isCompactHeight,
                                modifier = Modifier.weight(1f)
                            ) { triggerAbsorbParry() }

                            DuelPowerBtn(
                                title = "👁 BEYAZ",
                                sub = if (whiteReady) "FELÇ ET!" else "%$whiteEyeCharge",
                                enabled = whiteReady,
                                bg = if (whiteReady) Color(0xE0E2E8F0).copy(alpha = pulseAlpha * 0.45f) else Color(0xCC1E1B3A),
                                border = if (whiteReady) Color.White else Color(0x55E2E8F0),
                                compact = isCompactHeight,
                                modifier = Modifier.weight(1f)
                            ) { triggerWhiteEyeStun() }

                            DuelPowerBtn(
                                title = "🔴 KIRMIZI",
                                sub = if (redReady) "YIKIM!" else "%$redEyeCharge",
                                enabled = redReady,
                                bg = if (redReady) Color(0xE8DC2626).copy(alpha = pulseAlpha) else Color(0xCC2A0B18),
                                border = if (redReady) Color(0xFFFCA5A5) else Color(0x66F43F5E),
                                compact = isCompactHeight,
                                modifier = Modifier.weight(1f)
                            ) { triggerRedEyeFinisher() }
                        }
                    }
                }
                }
            }

            // ULTRON PRIME PHASE 3 INTERACTIVE FINAL BEAM CLASH OVERLAY!
            if (duelPhase == RooftopDuelPhase.PLAYING && beamClashActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC050312))
                        .pointerInput(Unit) {
                            detectTapGestures {
                                beamClashProgress = (beamClashProgress + 0.085f).coerceAtMost(1f)
                                cameraShake = 0.65f
                                soundEngine.play(CosmicSfx.COSMIC_BEAM, volume = 0.72f, affectedByTimeSlow = false)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.90f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xF00B0622))
                            .border(1.6.dp, Color(0xFFFDE047), RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "✦ FİNAL IŞIN ÇARPIŞMASI · SOKOVİA ✦",
                            fontFamily = DisplaySerifFontFamily,
                            color = Color(0xFFFDE047),
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Vision, Thor, Wanda ve Tony seninle! Ultron Prime'ın vibranyum ışınını kırmak için EKRANA HIZLA DOKUN!",
                            fontFamily = InterFontFamily,
                            color = Color(0xFFE2D9F8),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF991B1B))
                                .border(1.4.dp, Color(0xFFFDE047), RoundedCornerShape(50))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(beamClashProgress.coerceIn(0.05f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFFFDE047), Color.White)))
                            )
                            Text(
                                text = "AIDEN & AVENGERS %${(beamClashProgress * 100).toInt()} ⚔ ULTRON",
                                fontFamily = InterFontFamily,
                                color = Color(0xFF04030D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Text(
                            text = "⚡ HIZLI DOKUN! ⚡",
                            fontFamily = DisplaySerifFontFamily,
                            color = Color(0xFF7DD3FC),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // PAUSE OVERLAY
            if (duelPhase == RooftopDuelPhase.PLAYING && isPaused) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xC2040310)), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.84f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xF50C0822))
                            .border(1.3.dp, Color(0xFF38BDF8), RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("DÜELLO DURAKLATILDI", fontFamily = DisplaySerifFontFamily, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED))))
                                .clickable { isPaused = false }.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("DEVAM ET ▶", fontFamily = InterFontFamily, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold) }
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                .background(Color(0x991E1B3A)).border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                                .clickable { onDismiss() }.padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Hikayeye Dön", fontFamily = InterFontFamily, color = Color(0xFFE0E7FF), fontSize = 12.5.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            // SCROLL-SAFE READY / VICTORY / DEFEAT OVERLAYS (NEVER CLIP ON COMPACT SCREENS!)
            if (duelPhase != RooftopDuelPhase.PLAYING) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xBD050414))
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xF50B0720))
                            .border(
                                1.5.dp,
                                when (duelPhase) {
                                    RooftopDuelPhase.VICTORY -> Color(0xFF34D399)
                                    RooftopDuelPhase.DEFEAT -> Color(0xFFF87171)
                                    else -> Color(0xFF38BDF8)
                                },
                                RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            CinematicCharacterAvatar(CharacterPortraitKey.AIDEN, size = 54.dp, borderWidth = 2.dp, borderColor = Color(0xFF38BDF8))
                            Text("VS", fontFamily = DisplaySerifFontFamily, color = Color(0xFFFDE047), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            CinematicCharacterAvatar(profile.portraitKey, size = 54.dp, borderWidth = 2.dp, borderColor = profile.primaryColor)
                        }

                        Text(
                            text = when (duelPhase) {
                                RooftopDuelPhase.VICTORY -> profile.victoryHeadline
                                RooftopDuelPhase.DEFEAT -> "DÜŞTÜN — TEKRAR KALK!"
                                else -> title.uppercase()
                            },
                            fontFamily = DisplaySerifFontFamily,
                            color = when (duelPhase) {
                                RooftopDuelPhase.VICTORY -> Color(0xFF6EE7B7)
                                RooftopDuelPhase.DEFEAT -> Color(0xFFFCA5A5)
                                else -> Color.White
                            },
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        if (duelPhase == RooftopDuelPhase.VICTORY) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x99064E3B))
                                    .border(1.dp, Color(0x8834D399), RoundedCornerShape(14.dp))
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("DERECE", fontFamily = InterFontFamily, color = Color(0xFFD1FAE5), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                    Text(computeRank(), fontFamily = DisplaySerifFontFamily, color = Color(0xFFFDE047), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("SKOR", fontFamily = InterFontFamily, color = Color(0xFFD1FAE5), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                    Text("$score", fontFamily = DisplaySerifFontFamily, color = Color(0xFF7DD3FC), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("MAKS KOMBO", fontFamily = InterFontFamily, color = Color(0xFFD1FAE5), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                    Text("x$maxCombo", fontFamily = DisplaySerifFontFamily, color = Color(0xFFF9A8D4), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }

                        Text(
                            text = when (duelPhase) {
                                RooftopDuelPhase.VICTORY -> profile.victoryDesc
                                RooftopDuelPhase.DEFEAT ->
                                    "İpucu: Kırmızı zeminlerden ve süpüren lazer dalgasından son anda ışınlanarak zamanı yavaşlat; mavi toplarda EMİLİM basıp +10 can al ve süper karşı darbe vur!"
                                else ->
                                    "• Alt zemine dokun/kaydır: Anlık Işınlanma (Son anda kaç = Zaman Yavaşlar!)\n• 🛡 EMİLİM: Mavi enerji topunu emip +10 can al & süper karşı ışın yolla\n• 👁 BEYAZ GÖZ: ${profile.shortTag}'un Denge & Kalkanını kır (x2 Kritik Hasar!)\n• 🔴 KIRMIZI GÖZ: 3'lü ışınlanmalı moleküler yıkım\n• Dron/küreler indiğinde üstlerine dokunup imha et!"
                            },
                            fontFamily = InterFontFamily,
                            color = Color(0xFFE2D9F8),
                            fontSize = 11.8.sp,
                            lineHeight = 17.sp,
                            textAlign = TextAlign.Center
                        )

                        if (duelPhase != RooftopDuelPhase.VICTORY) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CosmicDuelDifficulty.entries.forEach { diff ->
                                    val selected = selectedDifficulty == diff
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (selected) Color(0xCC2563EB) else Color(0x66160C30))
                                            .border(
                                                1.dp,
                                                if (selected) Color(0xFFBAE6FD) else Color(0x44FFFFFF),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedDifficulty = diff }
                                            .padding(horizontal = 4.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = diff.labelTr,
                                            fontFamily = InterFontFamily,
                                            color = if (selected) Color.White else Color(0xFFCBD5E1),
                                            fontSize = 9.5.sp,
                                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(15.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFFDC2626))))
                                .border(1.2.dp, Color(0xFFBAE6FD), RoundedCornerShape(15.dp))
                                .clickable { startNewDuel() }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (duelPhase) {
                                    RooftopDuelPhase.VICTORY -> "TEKRAR OYNA ✦"
                                    RooftopDuelPhase.DEFEAT -> "YENİDEN DENE ✦"
                                    else -> profile.startBtnText
                                },
                                fontFamily = InterFontFamily,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        if (duelPhase == RooftopDuelPhase.VICTORY) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF059669))
                                    .border(1.dp, Color(0xFF6EE7B7), RoundedCornerShape(14.dp))
                                    .clickable { onDismiss() }
                                    .padding(vertical = 11.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("HİKAYEYE DEVAM ET (KİLİT AÇILDI) ▶", fontFamily = InterFontFamily, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        } else if (duelPhase == RooftopDuelPhase.DEFEAT) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x991E1B3A))
                                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                                    .clickable { onDismiss() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Geri Dön", fontFamily = InterFontFamily, color = Color(0xFFE0E7FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuelHudBtn(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color(0xCC120A2E))
            .border(1.dp, Color(0x99FFFFFF), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DuelPowerBtn(
    title: String,
    sub: String,
    enabled: Boolean,
    bg: Color,
    border: Color,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(bg)
            .border(1.1.dp, border, RoundedCornerShape(11.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = if (compact) 6.dp else 8.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontFamily = InterFontFamily,
                color = Color.White,
                fontSize = if (compact) 8.5.sp else 9.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sub,
                fontFamily = InterFontFamily,
                color = if (enabled) Color(0xFFE0F2FE) else Color(0xFF94A3B8),
                fontSize = if (compact) 7.4.sp else 7.8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
