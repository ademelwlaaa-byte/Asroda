package com.example.ui.screens.cosmic3d

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.R
import com.example.ui.animation.LocalCosmicMotionPolicy
import com.example.ui.screens.CharacterPortraitKey
import com.example.ui.screens.CinematicCharacterAvatar
import com.example.ui.screens.ConfigureTrueFullscreenDialogWindow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import kotlinx.coroutines.delay
import java.util.Locale

private val BossPanelShape = RoundedCornerShape(20.dp)
private val BossVictoryPanelShape = RoundedCornerShape(22.dp)
private val BossActionShape = RoundedCornerShape(14.dp)
private val BossBarShape = RoundedCornerShape(4.dp)
private val BossIntroCtaBrush = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED)))
private val BossBreakCtaBrush = Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFF7C3AED)))
private val BossVictoryCtaBrush = Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF2563EB)))

internal object CosmicBossDuelCalculator {
    fun computeShieldAfterOrder(currentShieldPct: Int, requiredOrdersCount: Int): Int {
        val stepDrop = 70 / requiredOrdersCount.coerceAtLeast(1)
        return (currentShieldPct.coerceIn(0, 100) - stepDrop).coerceIn(25, 100)
    }

    fun computeDuelRank(totalScore: Int): String = when {
        totalScore >= 1400 -> "S"
        totalScore >= 1000 -> "A"
        else -> "B"
    }

    fun shieldProgressFraction(shieldPct: Int): Float =
        (shieldPct.toFloat() / 100f).coerceIn(0f, 1f)
}

enum class CosmicDuelBossId {
    VAELOR_ROOFTOP,
    MIRROR_ZERO,
    ULTRON_PRIME;

    companion object {
        fun fromTag(raw: String): CosmicDuelBossId = when (raw.trim().uppercase(Locale.ROOT)) {
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

private data class AvengersCommsOrder(
    val id: String,
    val speakerKey: String,
    val portraitKey: CharacterPortraitKey,
    val title: String,
    val tacticalDesc: String,
    val voiceLine: String,
    val lineId: String,
    val sfx: CosmicSfx
)

private data class CinematicShowdownSpec(
    val bossId: CosmicDuelBossId,
    val displayTitle: String,
    val actBadge: String,
    val accent: Color,
    val ambientSceneId: String,
    val particleMode: String,
    val act0Image: Int,
    val act1Image: Int,
    val act2Image: Int,
    val victoryImage: Int,
    val act0Speaker: String,
    val act0Line: String,
    val act0LineId: String,
    val act1EnemySpeaker: String,
    val act1EnemyLine: String,
    val act1EnemyLineId: String,
    val requiredOrdersCount: Int,
    val commsOrders: List<AvengersCommsOrder>,
    val act2Speaker: String,
    val act2Line: String,
    val act2LineId: String,
    val whiteEyeTitle: String,
    val whiteEyeLine: String,
    val redEyeTitle: String,
    val redEyeLine: String,
    val victorySpeaker: String,
    val victoryLine: String,
    val victoryLineId: String,
    val victoryHeadline: String,
    val victoryDesc: String
)

private fun resolveShowdownSpec(bossId: CosmicDuelBossId, pietroSaved: Boolean): CinematicShowdownSpec = when (bossId) {
    CosmicDuelBossId.VAELOR_ROOFTOP -> CinematicShowdownSpec(
        bossId = bossId,
        displayTitle = "2.5D Yüzleşme · Yüce Hakem Vaelor",
        actBadge = "1. SEZON FİNALİ · AVENGERS KULESİ ÇATISI",
        accent = Color(0xFF38BDF8),
        ambientSceneId = "BLUE_SKY_2006",
        particleMode = "STORM",
        act0Image = R.drawable.img_game_rooftop_arena,
        act1Image = R.drawable.img_anim_armada,
        act2Image = R.drawable.img_anim_mutlak_uyanis,
        victoryImage = R.drawable.img_end_natasha_aiden_terrace_1795553969764,
        act0Speaker = "VAELOR",
        act0Line = "Diz çök Aiden Blackwood! O mavi kıvılcım Soluk Mühür'e aittir!",
        act0LineId = "voice_vaelor_rooftop_p2",
        act1EnemySpeaker = "NATASHA",
        act1EnemyLine = "Vaelor çatıya indi Aiden! Zemine dokunup ışınlan, mavi toplarda Emilim basıp geri fırlat!",
        act1EnemyLineId = "voice_vaelor_rooftop_intro",
        requiredOrdersCount = 2,
        commsOrders = listOf(
            AvengersCommsOrder(
                id = "steve",
                speakerKey = "STEVE",
                portraitKey = CharacterPortraitKey.STEVE,
                title = "Steve Rogers · Kalkan Kırıcı",
                tacticalDesc = "Vaelor'un ön savunmasını Vibranyum kalkanla yar",
                voiceLine = "Kalkanımı yolladım Aiden! Gardı kırıldı, şimdi yüklen!",
                lineId = "voice_steve_assist",
                sfx = CosmicSfx.AVENGERS_SUPPORT
            ),
            AvengersCommsOrder(
                id = "natasha",
                speakerKey = "NATASHA",
                portraitKey = CharacterPortraitKey.NATASHA,
                title = "Natasha Romanoff · Elektrik Kilidi",
                tacticalDesc = "Zırh eklemlerine yüksek voltajlı Widow ısırığı kilitle",
                voiceLine = "Elektrik şokları kilitlendi Aiden! Şimdi ışınlanıp vur!",
                lineId = "voice_natasha_assist",
                sfx = CosmicSfx.COMMS_CHIRP
            ),
            AvengersCommsOrder(
                id = "tony",
                speakerKey = "TONY",
                portraitKey = CharacterPortraitKey.TONY,
                title = "Tony Stark · Yörünge Işını",
                tacticalDesc = "Stark uydusundan çatıya doğrudan enerji ışını indir",
                voiceLine = "Kenara ışınlan evlat — yörünge ışınını tam üstüne indiriyorum!",
                lineId = "voice_tony_assist",
                sfx = CosmicSfx.LASER_FIRE
            ),
            AvengersCommsOrder(
                id = "wanda",
                speakerKey = "WANDA",
                portraitKey = CharacterPortraitKey.WANDA,
                title = "Wanda Maximoff · Kızıl Kaos Kilidi",
                tacticalDesc = "Vaelor'u havada kızıl büyüyle sabitle",
                voiceLine = "Onu kızıl kaosla tuttum Aiden! Kırmızı Göz'ünü serbest bırak!",
                lineId = "voice_wanda_assist",
                sfx = CosmicSfx.WHITE_EYE_NOVA
            )
        ),
        act2Speaker = "WANDA",
        act2Line = "Zırhı çatladı Aiden! Ekibin gücü ve Kırmızı Göz'le bu çatı savaşını bitir!",
        act2LineId = "voice_vaelor_rooftop_p3",
        whiteEyeTitle = "✦ BEYAZ GÖZ · ZAMAN KIRILMASI",
        whiteEyeLine = "Zamanın bitti, Vaelor!",
        redEyeTitle = "🔥 KIRMIZI GÖZ · MOLEKÜLER DARBE",
        redEyeLine = "Yeter! Manhattan'a dokunamazsın!",
        victorySpeaker = "STEVE",
        victoryLine = "Başardın Aiden! Vaelor diz çöktü — Manhattan güvende!",
        victoryLineId = "voice_vaelor_rooftop_victory",
        victoryHeadline = "✦ VAELOR DİZ ÇÖKTÜ · ZAFER! ✦",
        victoryDesc = "Avengers Kulesi'nin çatısında ekip komutasıyla Vaelor'un kozmik zırhını parçaladın ve Manhattan'ı kurtardın."
    )

    CosmicDuelBossId.MIRROR_ZERO -> CinematicShowdownSpec(
        bossId = bossId,
        displayTitle = "2.5D Ayna Yüzleşmesi · Sıfır",
        actBadge = "2. SEZON · KULE TERASI · KIRIK AYNA",
        accent = Color(0xFFA855F7),
        ambientSceneId = "DARK_MIRROR",
        particleMode = "RIFT",
        act0Image = R.drawable.img_scene3d_zero_rift_wide_1795627491596,
        act1Image = R.drawable.img_ch1_c2_stealth_terrace,
        act2Image = R.drawable.img_postcredits_twin_aiden,
        victoryImage = R.drawable.img_end_wanda_aiden_pier_1795553970888,
        act0Speaker = "ZERO",
        act0Line = "Beşiği açmalarına izin veremem Aiden! Beni geçmeden o laboratuvara giremezsin!",
        act0LineId = "voice_mirror_zero_intro",
        act1EnemySpeaker = "ZERO",
        act1EnemyLine = "Her hamleni ezbere biliyorum — çünkü sen benim beş yıl önceki hâlimsin!",
        act1EnemyLineId = "voice_mirror_zero_p2",
        requiredOrdersCount = 2,
        commsOrders = listOf(
            AvengersCommsOrder(
                id = "natasha",
                speakerKey = "NATASHA",
                portraitKey = CharacterPortraitKey.NATASHA,
                title = "Natasha · Soğukkanlı Taktik",
                tacticalDesc = "Sıfır'ın öfkesini kendi aleyhine çevirip dengesini boz",
                voiceLine = "Aiden, o öfkeyle savaşıyor — sen hâlâ insanlığını koruyorsun! Dengesini kır ve durdur onu!",
                lineId = "voice_mirror_zero_p3",
                sfx = CosmicSfx.DASH_BLINK
            ),
            AvengersCommsOrder(
                id = "wanda",
                speakerKey = "WANDA",
                portraitKey = CharacterPortraitKey.WANDA,
                title = "Wanda · Zihin Yankısı Kalkanı",
                tacticalDesc = "Karanlık geleceğin parazit dalgasını kızıl kalkanla kes",
                voiceLine = "Onu kızıl kaosla tuttum Aiden! Kırmızı Göz'ünü serbest bırak!",
                lineId = "voice_wanda_assist",
                sfx = CosmicSfx.WHITE_EYE_NOVA
            ),
            AvengersCommsOrder(
                id = "steve",
                speakerKey = "STEVE",
                portraitKey = CharacterPortraitKey.STEVE,
                title = "Steve · Savunma Hattı",
                tacticalDesc = "Vibranyum Beşiği kapısını ekip kalkanıyla güvene al",
                voiceLine = "Kalkanımı yolladım Aiden! Gardı kırıldı, şimdi yüklen!",
                lineId = "voice_steve_assist",
                sfx = CosmicSfx.AVENGERS_SUPPORT
            )
        ),
        act2Speaker = "NATASHA",
        act2Line = "Aiden, o öfkeyle savaşıyor — sen hâlâ insanlığını koruyorsun! Dengesini kır ve durdur onu!",
        act2LineId = "voice_mirror_zero_p3",
        whiteEyeTitle = "✦ KADERİ REDDET · BEYAZ GÖZ",
        whiteEyeLine = "Ben senin geleceğin olmak zorunda değilim!",
        redEyeTitle = "🔥 EKİBİNİ KORU · KIRMIZI GÖZ",
        redEyeLine = "Kule'ye ve ekibime dokunma!",
        victorySpeaker = "WANDA",
        victoryLine = "Başardın Aiden... Sıfır diz çöktü! Beşik güvende, şimdi gerçeği öğreneceğiz!",
        victoryLineId = "voice_mirror_zero_victory",
        victoryHeadline = "✦ AYNA YÜZLEŞMESİ KAZANILDI! ✦",
        victoryDesc = "Kendi karanlık geleceğin Sıfır'ı durdurdun, Vibranyum Beşiği'ni korudun ve yeni bir kader çizdin."
    )

    CosmicDuelBossId.ULTRON_PRIME -> CinematicShowdownSpec(
        bossId = bossId,
        displayTitle = "2.5D Sokovia Finali · Ultron Prime",
        actBadge = "2. SEZON FİNALİ · SOKOVİA MEYDANI",
        accent = Color(0xFFF43F5E),
        ambientSceneId = "BLUE_SKY_2006",
        particleMode = "EMBER",
        act0Image = R.drawable.img_postcredits_ruined_nyc,
        act1Image = R.drawable.img_portrait_ultron_prime_1795557413989,
        act2Image = R.drawable.img_anim_mutlak_uyanis,
        victoryImage = R.drawable.img_scene_natasha_aiden_terrace_1795640635755,
        act0Speaker = "ULTRON",
        act0Line = "Kuklalarım koparıldı ama bu şehir gökten düşecek Aiden Blackwood! İplerim artık yok!",
        act0LineId = "voice_ultron_prime_intro",
        act1EnemySpeaker = "ULTRON",
        act1EnemyLine = "Enerji kesicilerimi aşamazsın! Demir bükülmez — ama insan kalbin kırılır!",
        act1EnemyLineId = "voice_ultron_prime_p2",
        requiredOrdersCount = 3,
        commsOrders = buildList {
            add(
                AvengersCommsOrder(
                    id = "thor",
                    speakerKey = "THOR",
                    portraitKey = CharacterPortraitKey.THOR,
                    title = "Thor · Asgard Yıldırımı",
                    tacticalDesc = "Vibranyum gövdeyi doğrudan yıldırım dalgasıyla aşırı yükle",
                    voiceLine = "Asgard'ın fırtınası üstüne insin! Hazır mısın Aiden?",
                    lineId = "voice_thor_assist",
                    sfx = CosmicSfx.GENERATOR_EXPLODE
                )
            )
            add(
                AvengersCommsOrder(
                    id = "vision",
                    speakerKey = "VISION",
                    portraitKey = CharacterPortraitKey.VISION,
                    title = "Vision · Zihin Taşı Işını",
                    tacticalDesc = "Ultron'u ağdan kopar ve zırh çekirdeğini açığa çıkar",
                    voiceLine = "Zihin Taşı'nı odaklıyorum Aiden — zırhı açıldı, şimdi vur!",
                    lineId = "voice_vision_assist",
                    sfx = CosmicSfx.LASER_FIRE
                )
            )
            if (pietroSaved) {
                add(
                    AvengersCommsOrder(
                        id = "pietro",
                        speakerKey = "PIETRO",
                        portraitKey = CharacterPortraitKey.PIETRO,
                        title = "Pietro · Süpersonik Temizlik",
                        tacticalDesc = "Sokovia meydanındaki tüm Ultron nöbetçilerini saniyede indir",
                        voiceLine = "Bunu görmedin mi Ultron?! Nöbetçileri temizledim — sıra sende Aiden!",
                        lineId = "voice_pietro_assist",
                        sfx = CosmicSfx.DASH_BLINK
                    )
                )
            }
            add(
                AvengersCommsOrder(
                    id = "tony",
                    speakerKey = "TONY",
                    portraitKey = CharacterPortraitKey.TONY,
                    title = "Tony Stark · Yörünge Işını",
                    tacticalDesc = "Üçlü ışın birleşimiyle Vibranyum gövdeyi erit",
                    voiceLine = "Kenara ışınlan evlat — yörünge ışınını tam üstüne indiriyorum!",
                    lineId = "voice_tony_assist",
                    sfx = CosmicSfx.LASER_FIRE
                )
            )
            add(
                AvengersCommsOrder(
                    id = "wanda",
                    speakerKey = "WANDA",
                    portraitKey = CharacterPortraitKey.WANDA,
                    title = "Wanda · Kızıl Kaos Darbesi",
                    tacticalDesc = "Ultron'un çekirdeğini kızıl enerjiyle kilitle",
                    voiceLine = "Onu kızıl kaosla tuttum Aiden! Kırmızı Göz'ünü serbest bırak!",
                    lineId = "voice_wanda_assist",
                    sfx = CosmicSfx.WHITE_EYE_NOVA
                )
            )
        },
        act2Speaker = "ARCHITECT",
        act2Line = "Kayıp Anahtarım... metal kırılabilir ama tohumum ekildi. Şimdi sınırını göster!",
        act2LineId = "voice_ultron_prime_p3",
        whiteEyeTitle = "✦ SOKOVİA'YI KURTAR · BEYAZ GÖZ",
        whiteEyeLine = "Sokovia'yı düşürmene izin vermeyeceğim!",
        redEyeTitle = "🔥 EKİBİM İÇİN · KIRMIZI GÖZ FİNALİ",
        redEyeLine = "Bütün ekibim için — BURADA BİTİYOR ULTRON!",
        victorySpeaker = "VISION",
        victoryLine = "Ultron'un Vibranyum çekirdeği parçalandı Aiden! Birlikte başardık — şehir kurtuluyor!",
        victoryLineId = "voice_ultron_prime_victory",
        victoryHeadline = "✦ ULTRON PRİME PARÇALANDI · ZAFER! ✦",
        victoryDesc = "Sokovia meydanında Avengers ile kusursuz koordinasyonla Vibranyum Ultron Prime'ı devirdin ve şehri kurtardın!"
    )
}

/**
 * Full-Screen 4-Act 2.5D Interactive Cinematic Showdown replacing legacy arcade boss games.
 * - ZERO face/mouth/eye layer hacks (pure Motion-Cinema 2.5D cuts, particles & camera shake).
 * - Live Avengers Tactical Comms Command with real ElevenLabs Turkish dubbing.
 * - Guaranteed unbroken Victory Cinematic & full character victory dialogue before exit.
 */
@Composable
fun CosmicRooftopBossDuelDialog(
    title: String,
    bossId: CosmicDuelBossId = CosmicDuelBossId.VAELOR_ROOFTOP,
    pietroSaved: Boolean = false,
    onVictory: (score: Int, rank: String) -> Unit,
    onDismiss: () -> Unit
) {
    val spec = remember(bossId, pietroSaved) { resolveShowdownSpec(bossId, pietroSaved) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val motionPolicy = LocalCosmicMotionPolicy.current

    val soundEngine = remember { Cosmic3DSoundEngine(context, enableOrchestralMusic = true) }
    var isVoiceSpeaking by remember { mutableStateOf(false) }
    val voiceEngine = remember(soundEngine) {
        CosmicCharacterVoiceEngine(context) { speaking ->
            isVoiceSpeaking = speaking
            soundEngine.setVoiceDucking(speaking)
        }
    }

    var isMuted by remember { mutableStateOf(false) }
    var isLifecycleResumed by remember { mutableStateOf(true) }
    var actIndex by remember { mutableIntStateOf(0) } // 0: Intro, 1: Avengers Comms, 2: Ultimate Final, 3: Victory Cinematic
    var bossShieldPct by remember { mutableIntStateOf(100) }
    var totalScore by remember { mutableIntStateOf(0) }
    var shakeIntensity by remember { mutableFloatStateOf(0f) }
    var victoryReported by remember { mutableStateOf(false) }

    val issuedOrderIds = remember { mutableStateListOf<String>() }
    var activeSpeaker by remember { mutableStateOf<CharacterPortraitKey?>(null) }
    var activeSubtitle by remember { mutableStateOf("") }

    fun speakLine(speakerKey: String, text: String, lineId: String? = null) {
        activeSpeaker = CharacterPortraitKey.resolveFromLabel(speakerKey)
        activeSubtitle = text
        if (!isMuted && isLifecycleResumed && text.isNotBlank()) {
            voiceEngine.speak(speakerKey, text, lineId)
        }
    }

    DisposableEffect(lifecycleOwner, soundEngine, voiceEngine) {
        soundEngine.start(ambientSceneId = spec.ambientSceneId, ambientVolume = 0.36f)
        val warmList = buildList {
            add(Triple(spec.act0Speaker, spec.act0Line, spec.act0LineId))
            add(Triple(spec.act1EnemySpeaker, spec.act1EnemyLine, spec.act1EnemyLineId))
            spec.commsOrders.forEach { add(Triple(it.speakerKey, it.voiceLine, it.lineId)) }
            add(Triple(spec.act2Speaker, spec.act2Line, spec.act2LineId))
            add(Triple("AIDEN", spec.whiteEyeLine, null))
            add(Triple("AIDEN", spec.redEyeLine, null))
            add(Triple(spec.victorySpeaker, spec.victoryLine, spec.victoryLineId))
        }
        voiceEngine.prewarmLines(warmList)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    isLifecycleResumed = false
                    soundEngine.setMuted(true)
                    voiceEngine.setMuted(true)
                    voiceEngine.stopCurrentLine()
                }
                Lifecycle.Event.ON_RESUME -> {
                    isLifecycleResumed = true
                    soundEngine.setMuted(isMuted)
                    voiceEngine.setMuted(isMuted)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            voiceEngine.stopAndRelease()
            soundEngine.stopAndRelease()
        }
    }

    LaunchedEffect(shakeIntensity) {
        if (shakeIntensity > 0f) {
            delay(420L)
            shakeIntensity = 0f
        }
    }

    LaunchedEffect(actIndex) {
        when (actIndex) {
            0 -> {
                soundEngine.play(CosmicSfx.TIME_SLOW_ENTER, volume = 0.65f)
                speakLine(spec.act0Speaker, spec.act0Line, spec.act0LineId)
            }
            1 -> {
                soundEngine.play(CosmicSfx.COMMS_CHIRP, volume = 0.7f)
                speakLine(spec.act1EnemySpeaker, spec.act1EnemyLine, spec.act1EnemyLineId)
            }
            2 -> {
                soundEngine.play(CosmicSfx.LASER_WARNING, volume = 0.72f)
                if (!motionPolicy.shouldUseQuickTransitions) shakeIntensity = 0.85f
                speakLine(spec.act2Speaker, spec.act2Line, spec.act2LineId)
            }
            3 -> {
                soundEngine.play(CosmicSfx.VICTORY_FANFARE, volume = 0.85f)
                bossShieldPct = 0
                speakLine(spec.victorySpeaker, spec.victoryLine, spec.victoryLineId)
            }
        }
    }

    val activeBgRes = when (actIndex) {
        0 -> spec.act0Image
        1 -> spec.act1Image
        2 -> spec.act2Image
        else -> spec.victoryImage
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
                imageRes = activeBgRes,
                accent = if (actIndex == 3) Color(0xFF34D399) else spec.accent,
                particleMode = if (actIndex == 3) "WHITE_EYE" else spec.particleMode,
                shakeIntensity = shakeIntensity
            )

            // Edge-to-edge top & bottom vignette scrims so system bars blend seamlessly into the 2.5D cinema scene
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xCC05030E), Color(0x6605030E), Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0x8805030E), Color(0xDE04020B))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Cinema Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x99060312))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎬 ${spec.actBadge}",
                            fontFamily = InterFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = spec.accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = title.ifBlank { spec.displayTitle },
                            fontFamily = DisplaySerifFontFamily,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                soundEngine.setMuted(isMuted)
                                voiceEngine.setMuted(isMuted)
                            },
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0x44FFFFFF))
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
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0x44EF4444))
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

                // Boss Shield Progress Bar (Acts 0..2)
                if (actIndex < 3) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x7705030E))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PERDE ${actIndex + 1}/3 · DÜŞMAN SAVUNMASI",
                                fontFamily = InterFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE9D5FF)
                            )
                            Text(
                                text = "%$bossShieldPct",
                                fontFamily = InterFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = spec.accent
                            )
                        }
                        LinearProgressIndicator(
                            progress = { CosmicBossDuelCalculator.shieldProgressFraction(bossShieldPct) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(BossBarShape),
                            color = spec.accent,
                            trackColor = Color(0x33FFFFFF)
                        )
                    }
                }

                // Center Interactive Stage
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    when (actIndex) {
                        0 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(BossPanelShape)
                                    .background(Color(0xCC090418))
                                    .border(1.dp, spec.accent.copy(alpha = 0.65f), BossPanelShape)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "PERDE I · YÜZLEŞME BAŞLIYOR",
                                    fontFamily = InterFontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = spec.accent
                                )
                                Text(
                                    text = "Sıfır arcade tekrarı — taktik Avengers telsiz komutasıyla düşmanın savunmasını kır ve final kararını ver.",
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFFE2D9F3)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(BossActionShape)
                                        .background(BossIntroCtaBrush)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            actIndex = 1
                                        }
                                        .padding(vertical = 13.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isVoiceSpeaking) "KONUŞMAYI GEÇ & KOMUTAYA GEÇ ▶" else "AVENGERS TELSİZ KOMUTASINA GEÇ ▶",
                                        fontFamily = InterFontFamily,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        1 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(BossPanelShape)
                                    .background(Color(0xCC090418))
                                    .border(1.dp, spec.accent.copy(alpha = 0.65f), BossPanelShape)
                                    .padding(14.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "PERDE II · CANLI AVENGERS TELSİZ KOMUTASI (${issuedOrderIds.size}/${spec.requiredOrdersCount})",
                                    fontFamily = InterFontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFDE047)
                                )
                                spec.commsOrders.forEach { order ->
                                    key(order.id) {
                                        val used = order.id in issuedOrderIds
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(BossActionShape)
                                                .background(if (used) Color(0x3310B981) else Color(0x442E1065))
                                                .border(
                                                    1.dp,
                                                    if (used) Color(0xFF34D399) else order.portraitKey.accentColor.copy(alpha = 0.65f),
                                                    BossActionShape
                                                )
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    soundEngine.play(order.sfx, volume = 0.85f)
                                                    if (!motionPolicy.shouldUseQuickTransitions) shakeIntensity = 1.0f
                                                    speakLine(order.speakerKey, order.voiceLine, order.lineId)
                                                    if (!used) {
                                                        issuedOrderIds.add(order.id)
                                                        totalScore += 450
                                                        bossShieldPct = CosmicBossDuelCalculator.computeShieldAfterOrder(
                                                            currentShieldPct = bossShieldPct,
                                                            requiredOrdersCount = spec.requiredOrdersCount
                                                        )
                                                    }
                                                }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            CinematicCharacterAvatar(key = order.portraitKey, size = 38.dp)
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = order.title,
                                                    fontFamily = InterFontFamily,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = order.tacticalDesc,
                                                    fontFamily = InterFontFamily,
                                                    fontSize = 10.8.sp,
                                                    color = Color(0xFFD8B4FE)
                                                )
                                            }
                                            Icon(
                                                imageVector = if (used) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = if (used) Color(0xFF34D399) else Color.White
                                            )
                                        }
                                    }
                                }

                                if (issuedOrderIds.size >= spec.requiredOrdersCount) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(BossActionShape)
                                            .background(BossBreakCtaBrush)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                actIndex = 2
                                            }
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SAVUNMA KIRILDI · FİNAL HAMLESİNE GEÇ ▶",
                                            fontFamily = InterFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            var finalMoveChosen by remember(bossId, actIndex) { mutableStateOf(false) }
                            val finalMoves = remember(spec) {
                                listOf(
                                    Triple(spec.whiteEyeTitle, spec.whiteEyeLine, Color(0xFF38BDF8)),
                                    Triple(spec.redEyeTitle, spec.redEyeLine, Color(0xFFF43F5E))
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(BossPanelShape)
                                    .background(Color(0xDD0A041A))
                                    .border(1.2.dp, Color(0xFFFDE047).copy(alpha = 0.75f), BossPanelShape)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "PERDE III · FİNAL DARBESİ",
                                    fontFamily = InterFontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFDE047)
                                )
                                if (!finalMoveChosen) {
                                    finalMoves.forEach { (btnTitle, aidenLine, clr) ->
                                        key(btnTitle) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(BossActionShape)
                                                    .background(clr.copy(alpha = 0.24f))
                                                    .border(1.1.dp, clr.copy(alpha = 0.85f), BossActionShape)
                                                    .clickable {
                                                        finalMoveChosen = true
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        soundEngine.play(CosmicSfx.WHITE_EYE_NOVA, volume = 0.95f)
                                                        if (!motionPolicy.shouldUseQuickTransitions) shakeIntensity = 1.3f
                                                        bossShieldPct = 0
                                                        totalScore += 600
                                                        speakLine("AIDEN", aidenLine)
                                                    }
                                                    .padding(13.dp)
                                            ) {
                                                Text(
                                                    text = btnTitle,
                                                    fontFamily = InterFontFamily,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "\"$aidenLine\"",
                                                    fontFamily = InterFontFamily,
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFFE9D5FF)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = if (isVoiceSpeaking) {
                                            "🎙 Aiden final darbesini indiriyor — konuşma bitince Zafer Sahnesine geçebilirsin..."
                                        } else {
                                            "✦ Darbe indi! Zafer Sinematiğine geçmeye hazır."
                                        },
                                        fontFamily = InterFontFamily,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFDE68A)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(BossActionShape)
                                            .background(BossVictoryCtaBrush)
                                            .clickable { actIndex = 3 }
                                            .padding(vertical = 13.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "ZAFER SİNEMATİĞİNİ AÇ ✦",
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
                            val rank = CosmicBossDuelCalculator.computeDuelRank(totalScore)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(BossVictoryPanelShape)
                                    .background(Color(0xDD061314))
                                    .border(1.4.dp, Color(0xFF34D399), BossVictoryPanelShape)
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = spec.victoryHeadline,
                                    fontFamily = DisplaySerifFontFamily,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6EE7B7),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "SİNEMA DERECESİ: $rank · PUAN: $totalScore",
                                    fontFamily = InterFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.0.sp,
                                    color = Color(0xFFFDE047)
                                )
                                Text(
                                    text = spec.victoryDesc,
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFECFDF5),
                                    textAlign = TextAlign.Center
                                )
                                if (isVoiceSpeaking) {
                                    Text(
                                        text = "🎙 Zafer repliği çalıyor — bitmesini dinleyebilirsin...",
                                        fontFamily = InterFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFFFDE68A)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(BossActionShape)
                                        .background(BossVictoryCtaBrush)
                                        .clickable {
                                            if (!victoryReported) {
                                                victoryReported = true
                                                onVictory(totalScore, rank)
                                            } else {
                                                onDismiss()
                                            }
                                        }
                                        .padding(vertical = 13.dp),
                                    horizontalArrangement = Arrangement.Center
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
                    }
                }

                // Bottom Cinema Subtitle Ribbon — floating rounded glass card safely above navigation bar
                if (activeSubtitle.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, bottom = 10.dp)
                            .clip(BossPanelShape)
                            .background(Color(0xE2070414))
                            .border(0.9.dp, Color(0x55C4B5FD), BossPanelShape)
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        activeSpeaker?.let { speakerKey ->
                            CinematicCharacterAvatar(key = speakerKey, size = 40.dp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = activeSpeaker?.displayName ?: "Telsiz",
                                    fontFamily = InterFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeSpeaker?.accentColor ?: Color(0xFFC4B5FD),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isVoiceSpeaking) {
                                    Text(
                                        text = "🎙 KONUŞUYOR",
                                        fontFamily = InterFontFamily,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF6EE7B7),
                                        maxLines = 1
                                    )
                                }
                            }
                            Text(
                                text = "\"$activeSubtitle\"",
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
