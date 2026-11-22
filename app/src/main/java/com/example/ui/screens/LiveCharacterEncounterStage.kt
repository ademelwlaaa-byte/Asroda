package com.example.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.R
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.screens.cosmic3d.Cosmic3DSoundEngine
import com.example.ui.screens.cosmic3d.CosmicCharacterVoiceEngine
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class LiveEncounterProfile(
    val speakerKey: String,
    val displayName: String,
    val moodRole: String,
    @param:DrawableRes val portraitRes: Int,
    @param:DrawableRes val roomBackdropRes: Int,
    val accent: Color,
    val eyeYFrac: Float,
    val leftEyeXFrac: Float,
    val rightEyeXFrac: Float,
    val eyelidTone: Color,
    val ambientType: String, // "RAIN_WARM", "STARLIGHT_CRIMSON", "SNOW_BLUE", "DARK_MIRROR"
    val openingLine: String,
    val gazeLines: List<String>,
    val closerLines: List<String>,
    val talkLines: List<String>,
    val idleLines: List<String>
)

private fun resolveLiveEncounterProfile(characterKey: String, sceneId: String): LiveEncounterProfile {
    return when (characterKey.uppercase()) {
        "NATASHA" -> LiveEncounterProfile(
            speakerKey = "NATASHA",
            displayName = "Natasha Romanoff",
            moodRole = when {
                sceneId.contains("FINALE") -> "GÜN BATIMI TERASI · YENİ BAŞLANGIÇ"
                sceneId.contains("FARM") || sceneId.contains("VERANDA") -> "YAĞMURLU VERANDA · YALNIZ AN"
                else -> "GECE TERASI · SESSİZ YAKINLIK"
            },
            portraitRes = R.drawable.img_char_natasha,
            roomBackdropRes = R.drawable.img_ch1_c4_natasha_gaze,
            accent = Color(0xFFF472B6),
            eyeYFrac = 0.435f,
            leftEyeXFrac = 0.415f,
            rightEyeXFrac = 0.575f,
            eyelidTone = Color(0xFF2B1C24),
            ambientType = "RAIN_WARM",
            openingLine = if (sceneId.contains("FINALE")) {
                "Sokovia bitti Aiden... masada sıcacık köfteler, üstümüzde yıldızlar var. İlk kez geleceğe korkmadan bakıyorum."
            } else {
                "Yağmurun ahşap çatıya vurma sesini duyuyor musun Aiden? Bu gece kimse bizden bir görev beklemiyor."
            },
            gazeLines = listOf(
                "Bana öyle uzun uzun bakma Blackwood... yoksa ajanlık kurallarımı bozacağım.",
                "Gözlerindeki o mavi ışık söndüğünde sadece yorgun bir insan görüyorum — ve en çok o hâlini seviyorum.",
                "Eskiden birinin gözlerinin içine üç saniyeden fazla bakmazdım. Seninle zaman duruyor."
            ),
            closerLines = listOf(
                "Elin sıcak... yirmi yıl boyunca kimsenin elini bu kadar güvenle tutmamıştım.",
                "Bir adım daha yaklaşırsan seni bir daha o soğuk görevlere yollayamam, biliyorsun değil mi?",
                "Başını eğme Aiden. Burada duvar örmene gerek yok — ben yanındayım."
            ),
            talkLines = listOf(
                "Voss ve Mira için kurduğun sığınağı gördüm. Onlara kimsenin bana vermediği bir şansı verdin.",
                "Kızıl Oda'da bize kalbin zayıflık olduğunu öğrettiler. Senin yanında ilk kez bunun bir yalan olduğunu anladım.",
                "Bu savaş bitince Hell's Kitchen'da söz verdiğin o iki tabak sıcak köfteyi unutursan seni bulurum."
            ),
            idleLines = listOf(
                "Bana öyle sessizce bakmaya devam edersen, sonunda ben konuşmak zorunda kalacağım Aiden...",
                "Hâlâ buradasın... güzel. Acelemiz yok. Sadece yağmuru dinleyelim.",
                "Biliyor musun, ilk kez bir gecenin hiç bitmemesini istiyorum.",
                "Düşüncelere daldın yine... ne düşünüyorsun, Blackwood?"
            )
        )

        "WANDA" -> LiveEncounterProfile(
            speakerKey = "WANDA",
            displayName = "Wanda Maximoff",
            moodRole = if (sceneId.contains("FINALE")) "GÖL İSKELESİ · KIZIL VE MAVİ" else "YILDIZLI BAHÇE · KIZIL VE MAVİ",
            portraitRes = R.drawable.img_char_wanda,
            roomBackdropRes = R.drawable.img_ch1_c4_wanda_gaze,
            accent = Color(0xFFC084FC),
            eyeYFrac = 0.430f,
            leftEyeXFrac = 0.420f,
            rightEyeXFrac = 0.580f,
            eyelidTone = Color(0xFF281928),
            ambientType = "STARLIGHT_CRIMSON",
            openingLine = if (sceneId.contains("FINALE")) {
                "Gölün sularında yıldızlar parlıyor Aiden... İlk kez zihnimde hiç kâbus yok, sadece senin sıcaklığın var."
            } else {
                "Yıldızlar bu gece çok yakın görünüyor Aiden... Zihnindeki o gürültü ilk kez tamamen susmuş."
            },
            gazeLines = listOf(
                "Gözlerinin içine baktığımda korku değil, sadece derin, sakin bir mavi görüyorum.",
                "Bana böyle baktığında avuçlarımdaki kızıl ışık bile ısınmayı bırakıp yumuşuyor.",
                "İlk kez biri bana 'tehlikeli' değil, 'değerli' olduğumu hissettiriyor."
            ),
            closerLines = listOf(
                "Ellerimi tuttuğunda ikimizin enerjisi birbirine dolanıyor... üşümüyorum artık.",
                "Biraz daha yakın dur Aiden — dünyanın bütün gürültüsü senin nefesinle susuyor.",
                "Pietro'yu kurtaracağımıza gerçekten inanıyorum. Çünkü sen sözünü hep tutarsın."
            ),
            talkLines = listOf(
                "Mira'yla dün konuştum Aiden. Sana 'abi' derken gözleri parlıyordu — ona yeni bir hayat verdin.",
                "Bazen gücümün beni yutmasından korkuyorum... ama senin elini tuttuğumda kendimi hatırlıyorum.",
                "Sokovia'dan sonra nereye gidersek gidelim, yanında olduğum yer benim evim."
            ),
            idleLines = listOf(
                "Zihnini dinlemiyorum, söz veriyorum... ama sessizliğin bile çok huzurlu Aiden.",
                "Eğer biraz daha böyle sessiz kalırsan, yıldızları aşağı çekip sana göstereceğim...",
                "Yanımda kaldığın için teşekkür ederim. Gerçekten... yalnız olmamak garip ama güzel.",
                "Hâlâ gitmedin... bu gece burada kalabilir miyiz?"
            )
        )

        "VOSS" -> LiveEncounterProfile(
            speakerKey = "VOSS",
            displayName = "Elias Voss · Denek 6-B",
            moodRole = "BROOKLYN GÜVENLİ SIĞINAĞI · KARDEŞLİK",
            portraitRes = R.drawable.img_ch5_voss_blizzard,
            roomBackdropRes = R.drawable.img_ch5_voss_blizzard,
            accent = Color(0xFFF87171),
            eyeYFrac = 0.365f,
            leftEyeXFrac = 0.66f,
            rightEyeXFrac = 0.77f,
            eyelidTone = Color(0xFF24161A),
            ambientType = "SNOW_BLUE",
            openingLine = "Hoş geldin 7-A. Çay demledim — senin öğrettiğin nefes egzersizi sayesinde üç haftadır tek bir cam bile çatlatmadım.",
            gazeLines = listOf(
                "Gözlerime bakınca artık sadece kırık bir deney değil, bir adam görüyorsun... bunu kimse yapmamıştı.",
                "Kırmızı Göz eskiden beni yakardı Aiden. Şimdi onu senin öğrettiğin gibi kalkan yapmayı öğreniyorum."
            ),
            closerLines = listOf(
                "Omuzların yine taş gibi sertleşmiş kardeşim. Herkesi kurtarmaya çalışırken kendini unutma.",
                "Mira uyuyor; sonunda kâbus görmeden uyuyabiliyor. Bunun mimarı sensin."
            ),
            talkLines = listOf(
                "Beni Raft hücresinde çürümeye bırakabilirdin. Bırakmadın. Sokovia'da sırtın bana emanet.",
                "Dr. Hale'in dosyalarını inceledim — Ultron'un peşinde olduğu şey sadece metal değil, senin iraden.",
                "Bazen kendi kendime soruyorum: biz gerçekten normal bir hayat yaşayabilir miyiz Aiden? Senin sayende ilk kez 'evet' diyorum."
            ),
            idleLines = listOf(
                "Yine herkesten sakladığın bir yük mü taşıyorsun 7-A? Bana yalan söyleyemezsin, aynı deneyden çıktık.",
                "Sessiz durman bile iyi geliyor be kardeşim... eskiden sessizlik hücre demekti, şimdi huzur.",
                "Ne zaman çağırırsan buradayım. Ultron'muş, Mimar'mış — ikisi de önce beni aşmak zorunda."
            )
        )

        "MIRA" -> LiveEncounterProfile(
            speakerKey = "MIRA",
            displayName = "Mira · Denek 8-C",
            moodRole = if (sceneId.contains("GARDEN")) "YENİ HAYATIN SABAHI · ÖZGÜR MİRA" else "GÜVENLİ SIĞINAK · YENİ HAYAT",
            portraitRes = R.drawable.img_ch6_istanbul_cistern,
            roomBackdropRes = R.drawable.img_anim_bosphorus,
            accent = Color(0xFF38BDF8),
            eyeYFrac = 0.41f,
            leftEyeXFrac = 0.44f,
            rightEyeXFrac = 0.56f,
            eyelidTone = Color(0xFF1A2233),
            ambientType = "STARLIGHT_CRIMSON",
            openingLine = if (sceneId.contains("GARDEN")) {
                "Aiden abi! Artık hiçbir laboratuvar numarası yok — bu pusula ikimizin de kalbinden sana, her zaman yanında olduğumuzun kanıtı."
            } else {
                "Aiden! Geldiğini kapıdan bile değil, enerjinin sakinliğinden anladım. Sana göstermek istediğim yeni bir şey öğrendim!"
            },
            gazeLines = listOf(
                "İstanbul'da bana vurmayıp elini uzattığın o saniyeyi her sabah hatırlıyorum.",
                "Artık aynaya baktığımda Dr. Hale'in silahını değil, sadece Mira'yı görüyorum."
            ),
            closerLines = listOf(
                "Bruce'la yaptığımız seanslar çok iyi geliyor ama en çok seninle konuştuğumda nefes alabiliyorum.",
                "Voss sürekli abilik taslıyor ama ikimiz de biliyoruz — bu aileyi bir arada tutan sensin."
            ),
            talkLines = listOf(
                "Dün ilk kez korkmadan markete gidip kendi başıma ekmek aldım Aiden... küçücük bir şey ama benim için dünyaya bedeldi.",
                "Sokovia'da beni geride bırakma lütfen. Bana korumayı sen öğrettin — şimdi sivil insanları korumak istiyorum.",
                "Eğer bir gün Asıl Mimar karşına çıkarsa, 6-B, 7-A ve 8-C aynı cephede duracağız."
            ),
            idleLines = listOf(
                "Yine yorgunsun Aiden... otur biraz, sana sıcak çay getireyim.",
                "Biliyor musun, ilk kez geleceğin nasıl olacağını merak ederek uyanıyorum.",
                "Sessiz kaldığında gözlerin tıpkı Boğaz'ın şafağı gibi parlıyor."
            )
        )

        else -> LiveEncounterProfile(
            speakerKey = "ZERO",
            displayName = "Sıfır · Karanlık Gelecek",
            moodRole = if (sceneId.contains("SUNRISE")) "ALTIN ŞAFAK GÜVERTESİ · SON VEDA" else "REVİR ODASI · KIRIK AYNA",
            portraitRes = R.drawable.img_postcredits_twin_aiden,
            roomBackdropRes = R.drawable.img_postcredits_ruined_nyc,
            accent = Color(0xFF38BDF8),
            eyeYFrac = 0.435f,
            leftEyeXFrac = 0.43f,
            rightEyeXFrac = 0.57f,
            eyelidTone = Color(0xFF131D2D),
            ambientType = "DARK_MIRROR",
            openingLine = if (sceneId.contains("SUNRISE")) {
                "Başardın Aiden... benim zamanımda göremediğim bütün yüzler bugün gülümsüyor. Artık huzurla yıldız tozuna karışabilirim."
            } else {
                "Kendi yüzüne beş yıl sonrasından bakmak garip, değil mi Aiden? Yara izime bakıp korkma — bu kaderi hâlâ değiştirebilirsin."
            },
            gazeLines = listOf(
                "Gözlerindeki ışık hâlâ sönmemiş... benim zamanımda bu ışık Sokovia düştüğü gün öldü.",
                "Aynı gözler, aynı kalp — ama senin yanında hâlâ tutunabileceğin insanlar var."
            ),
            closerLines = listOf(
                "Fazla yaklaşma — Tekillik Paraziti ikimizin de damarlarını yakıyor. Ama elini uzatmandan nefret etmedim.",
                "Voss'u ve Mira'yı kurtarmışsın... benim zamanımda ikisine de yetişememiştim."
            ),
            talkLines = listOf(
                "Asıl Mimar'dan korkma ama onu asla küçümseme. O senin gücünü değil, kalbinin kırılmasını bekliyor.",
                "Natasha ya da Wanda... kalbini kime açtıysan, ona sıkı tutun. Ben yalnızlığı seçtim ve her şeyi kaybettim.",
                "Ultron sadece demir bir çekiç. Asıl sınav, o çekiç indiğinde hâlâ insan kalabilmek."
            ),
            idleLines = listOf(
                "Beni inceliyorsun... 'Ben gerçekten buna dönüşür müyüm?' diye soruyorsun. Hayır Aiden — dönüşmeyeceksin.",
                "Sessizlik... beş yıldır ilk kez siren sesi olmadan bir odada oturuyorum.",
                "Zamanım azalıyor. Her doğru kararında benim karanlık geleceğim biraz daha siliniyor."
            )
        )
    }
}

/**
 * AAA Interactive Living 3D/2.5D Character Encounter Stage embedded inside intimate & emotional story scenes
 * via [[LIVE3D|CHARACTER|SCENE_ID|TITLE|SUBTITLE]].
 *
 * Features:
 * - Multi-plane 3D parallax (touch/drag camera orbit + organic breathing motion)
 * - Natural anatomical blinking every 3.2-5.4 seconds + gaze-tracking luminous eye catchlights
 * - Interactive actions ("Gözlerine Bak", "Yaklaş", "Konuş") + Fullscreen Theater Mode
 * - Smart Idle Spoken Personality System (speaks spontaneous intimate short Turkish lines when lingering!)
 * - Real Neural Cinema AI Voice Engine (Gemini / OpenAI) with smooth ambient soundscape ducking!
 */
@Composable
fun LiveCharacterEncounterStage(
    characterKey: String,
    sceneId: String,
    title: String,
    subtitle: String,
    chapterAccent: Color
) {
    val profile = remember(characterKey, sceneId) { resolveLiveEncounterProfile(characterKey, sceneId) }
    var isFullscreen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .morCamCosmicGlow(cornerRadius = 22.dp, glowColor = profile.accent, alpha = 0.44f, blurDp = 18.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xF5070516))
            .border(1.25.dp, profile.accent.copy(alpha = 0.78f), RoundedCornerShape(22.dp))
    ) {
        LiveEncounterCoreContent(
            profile = profile,
            title = title,
            subtitle = subtitle,
            isFullscreen = false,
            onToggleFullscreen = { isFullscreen = true }
        )
    }

    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
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
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                LiveEncounterCoreContent(
                    profile = profile,
                    title = title,
                    subtitle = subtitle,
                    isFullscreen = true,
                    onToggleFullscreen = { isFullscreen = false }
                )
            }
        }
    }
}

@Composable
private fun LiveEncounterCoreContent(
    profile: LiveEncounterProfile,
    title: String,
    subtitle: String,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var voiceEnabled by remember { mutableStateOf(true) }
    var isLeanedIn by remember { mutableStateOf(false) }
    var dragYaw by remember { mutableFloatStateOf(0f) }
    var dragPitch by remember { mutableFloatStateOf(0f) }
    var currentDialogue by remember(profile.speakerKey) { mutableStateOf(profile.openingLine) }
    var interactionTick by remember { mutableIntStateOf(0) }
    var gazeIdx by remember { mutableIntStateOf(0) }
    var closeIdx by remember { mutableIntStateOf(0) }
    var talkIdx by remember { mutableIntStateOf(0) }
    var idleIdx by remember { mutableIntStateOf(0) }
    var auraPulseTrigger by remember { mutableFloatStateOf(0f) }

    // Natural organic blinking animation (0f = eyes open, 1f = eyes closed)
    var blinkAmount by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(profile.speakerKey) {
        val rng = Random(profile.speakerKey.hashCode())
        while (true) {
            delay(rng.nextLong(2900L, 5100L))
            // Smooth rapid close -> soft open
            blinkAmount = 0.55f
            delay(38L)
            blinkAmount = 1.0f
            delay(75L)
            blinkAmount = 0.45f
            delay(45L)
            blinkAmount = 0f
            // Occasional natural double-blink
            if (rng.nextFloat() < 0.22f) {
                delay(180L)
                blinkAmount = 1.0f
                delay(70L)
                blinkAmount = 0f
            }
        }
    }

    val sfxEngine = remember(profile.speakerKey, isFullscreen) {
        Cosmic3DSoundEngine(context, enableOrchestralMusic = false)
    }
    val voiceEngine = remember(sfxEngine) {
        CosmicCharacterVoiceEngine(context) { speaking ->
            sfxEngine.setVoiceDucking(speaking)
        }
    }

    DisposableEffect(sfxEngine, voiceEngine) {
        if (isFullscreen) {
            sfxEngine.start(ambientSceneId = "BLUE_SKY_2006", ambientVolume = 0.34f)
        }
        onDispose {
            sfxEngine.stopAndRelease()
            voiceEngine.stopAndRelease()
        }
    }

    fun speakLine(line: String, lineKey: String) {
        currentDialogue = line
        auraPulseTrigger = 1f
        if (voiceEnabled) {
            voiceEngine.speak(profile.speakerKey, line, lineKey)
        }
    }

    // Smooth aura decay
    LaunchedEffect(auraPulseTrigger) {
        if (auraPulseTrigger > 0f) {
            repeat(12) {
                delay(45L)
                auraPulseTrigger = (auraPulseTrigger - 0.085f).coerceAtLeast(0f)
            }
        }
    }

    // Smart Idle Spoken Personality System!
    // If the user lingers watching Natasha/Wanda/Voss/Mira/Zero quietly, they spontaneously speak!
    LaunchedEffect(interactionTick, isFullscreen, profile.speakerKey) {
        val waitMs = when (idleIdx) {
            0 -> if (isFullscreen) 7500L else 11500L
            1 -> 12000L
            2 -> 14000L
            else -> 16000L
        }
        delay(waitMs)
        val line = profile.idleLines[idleIdx % profile.idleLines.size]
        idleIdx++
        speakLine(line, "live_${profile.speakerKey.lowercase()}_idle_$idleIdx")
    }

    val inf = rememberInfiniteTransition(label = "live3d_${profile.speakerKey}")
    val breath by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3800, easing = LinearEasing), RepeatMode.Reverse),
        label = "breath"
    )
    val weatherFlow by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
        label = "weather"
    )
    val smoothLean by animateFloatAsState(
        targetValue = if (isLeanedIn) 1.07f else 1.01f,
        animationSpec = tween(550),
        label = "lean"
    )
    val smoothBlink by animateFloatAsState(
        targetValue = blinkAmount,
        animationSpec = tween(55),
        label = "blink"
    )

    val particles = remember(profile.speakerKey) {
        List(28) { i ->
            Triple(Random(i * 31 + 7).nextFloat(), Random(i * 53 + 13).nextFloat(), 0.6f + Random(i * 19).nextFloat() * 0.9f)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xEE180828), Color(0xEE0C132E))))
                .padding(horizontal = 13.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "✦ CANLI 3D SAHNE · ${profile.moodRole}",
                    fontFamily = InterFontFamily,
                    color = profile.accent,
                    fontSize = 8.8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.9.sp
                )
                Text(
                    text = title.ifBlank { profile.displayName },
                    fontFamily = DisplaySerifFontFamily,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (voiceEnabled) profile.accent.copy(alpha = 0.25f) else Color(0x88160C2E))
                        .border(0.9.dp, if (voiceEnabled) profile.accent else Color(0x66FFFFFF), RoundedCornerShape(50))
                        .clickable {
                            voiceEnabled = !voiceEnabled
                            voiceEngine.setMuted(!voiceEnabled)
                        }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (voiceEnabled) "🔊 SES" else "🔇 SES",
                        fontFamily = InterFontFamily,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xAA160C2E))
                        .border(0.9.dp, Color(0xCCBAE6FD), RoundedCornerShape(50))
                        .clickable { onToggleFullscreen() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isFullscreen) "✕ KAPAT" else "⤢ TAM EKRAN",
                        fontFamily = InterFontFamily,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Interactive Living 3D Viewport (Responsive — never over-zooms!)
        val stageModifier = if (isFullscreen) {
            Modifier.fillMaxWidth().weight(1f)
        } else {
            Modifier.fillMaxWidth().height(320.dp)
        }

        BoxWithConstraints(
            modifier = stageModifier
                .background(Color(0xFF050410))
                .pointerInput(profile.speakerKey) {
                    detectDragGestures(
                        onDragEnd = { dragYaw = 0f; dragPitch = 0f },
                        onDragCancel = { dragYaw = 0f; dragPitch = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            interactionTick++
                            dragYaw = (dragYaw + dragAmount.x * 0.045f).coerceIn(-12f, 12f)
                            dragPitch = (dragPitch + dragAmount.y * 0.045f).coerceIn(-8f, 8f)
                        }
                    )
                }
        ) {
            // Plane 1 (Deep Room Background moving opposite to touch for genuine 3D stereo parallax!)
            Image(
                painter = painterResource(id = profile.roomBackdropRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.34f
                        scaleX = 1.12f
                        scaleY = 1.12f
                        translationX = dragYaw * 2.1f
                        translationY = dragPitch * 1.4f
                    }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                profile.accent.copy(alpha = 0.14f + auraPulseTrigger * 0.16f),
                                Color(0xCC060414),
                                Color(0xF503020A)
                            )
                        )
                    )
            )

            // Plane 2 (LIVING CHARACTER PORTRAIT — Exact aspect fit, breathing, 3D tilt & real eyelid blinking!)
            val portraitModifier = if (isFullscreen) {
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.88f)
                    .aspectRatio(1f)
            } else {
                Modifier
                    .align(Alignment.Center)
                    .height(276.dp)
                    .aspectRatio(1f)
            }

            Box(
                modifier = portraitModifier
                    .graphicsLayer {
                        val breathScale = smoothLean + sin(breath * PI.toFloat()) * 0.014f
                        scaleX = breathScale
                        scaleY = breathScale
                        translationX = -dragYaw * 1.6f
                        translationY = -dragPitch * 1.1f - sin(breath * PI.toFloat()) * 3.5f
                        rotationY = dragYaw * 0.85f
                        rotationX = -dragPitch * 0.65f
                        cameraDistance = 16f * density
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = if (auraPulseTrigger > 0.1f) 1.8.dp else 1.1.dp,
                        color = profile.accent.copy(alpha = 0.65f + auraPulseTrigger * 0.35f),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Image(
                    painter = painterResource(id = profile.portraitRes),
                    contentDescription = profile.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Soft Cinema Edge Lighting + REAL-TIME EYELID BLINKING & EYE CATCHLIGHT TRACKING!
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val eyeY = h * profile.eyeYFrac
                    val lx = w * profile.leftEyeXFrac
                    val rx = w * profile.rightEyeXFrac
                    val eyeW = w * 0.095f
                    val eyeH = h * 0.048f

                    // Subtle living eye glint that tracks touch/gaze direction!
                    val glintAlpha = (1f - smoothBlink) * 0.42f
                    if (glintAlpha > 0.03f) {
                        val gxOff = (dragYaw / 12f) * (eyeW * 0.16f)
                        val gyOff = (dragPitch / 8f) * (eyeH * 0.14f)
                        drawCircle(Color.White.copy(alpha = glintAlpha), radius = w * 0.008f, center = Offset(lx + gxOff, eyeY + gyOff))
                        drawCircle(Color.White.copy(alpha = glintAlpha), radius = w * 0.008f, center = Offset(rx + gxOff, eyeY + gyOff))
                    }

                    // Organic Eyelid Blink Sweep (soft upper lid descent + natural lash crease)
                    if (smoothBlink > 0.04f) {
                        val lidH = eyeH * smoothBlink
                        drawOval(
                            brush = Brush.verticalGradient(
                                listOf(
                                    profile.eyelidTone.copy(alpha = 0.82f * smoothBlink),
                                    profile.eyelidTone.copy(alpha = 0.94f * smoothBlink)
                                ),
                                startY = eyeY - eyeH * 0.52f,
                                endY = eyeY - eyeH * 0.52f + lidH
                            ),
                            topLeft = Offset(lx - eyeW * 0.5f, eyeY - eyeH * 0.52f),
                            size = Size(eyeW, lidH.coerceAtLeast(2f))
                        )
                        drawOval(
                            brush = Brush.verticalGradient(
                                listOf(
                                    profile.eyelidTone.copy(alpha = 0.82f * smoothBlink),
                                    profile.eyelidTone.copy(alpha = 0.94f * smoothBlink)
                                ),
                                startY = eyeY - eyeH * 0.52f,
                                endY = eyeY - eyeH * 0.52f + lidH
                            ),
                            topLeft = Offset(rx - eyeW * 0.5f, eyeY - eyeH * 0.52f),
                            size = Size(eyeW, lidH.coerceAtLeast(2f))
                        )
                        // Fine dark upper lash line descending naturally
                        val lashY = eyeY - eyeH * 0.45f + lidH * 0.88f
                        drawLine(
                            color = Color(0xFF0A060E).copy(alpha = 0.85f * smoothBlink),
                            start = Offset(lx - eyeW * 0.45f, lashY),
                            end = Offset(lx + eyeW * 0.45f, lashY),
                            strokeWidth = 2.4f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFF0A060E).copy(alpha = 0.85f * smoothBlink),
                            start = Offset(rx - eyeW * 0.45f, lashY),
                            end = Offset(rx + eyeW * 0.45f, lashY),
                            strokeWidth = 2.4f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Dynamic warm rim-light reacting to closeness & dialogue
                    if (auraPulseTrigger > 0.02f || isLeanedIn) {
                        val rimA = (if (isLeanedIn) 0.18f else 0f) + auraPulseTrigger * 0.22f
                        drawRect(
                            brush = Brush.radialGradient(
                                listOf(Color.Transparent, profile.accent.copy(alpha = rimA)),
                                center = Offset(w * 0.5f, h * 0.45f),
                                radius = w * 0.72f
                            )
                        )
                    }
                }

                // Bottom gradient scrim for readability inside portrait
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD070514))))
                )
                Text(
                    text = "↔ Sürükle: 3D Bakış · Göz kırpar & bekleyince konuşur",
                    fontFamily = InterFontFamily,
                    color = Color(0xCCDDD6FE),
                    fontSize = 8.5.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 7.dp)
                )
            }

            // Plane 3 (Foreground 60fps Atmosphere Particles — Rain, Warm Fireflies, Crimson/Blue Sparks)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                particles.forEachIndexed { idx, (px, py, spd) ->
                    when (profile.ambientType) {
                        "RAIN_WARM" -> {
                            val rProg = (py + weatherFlow * spd * 1.9f) % 1f
                            val rx = ((px - rProg * 0.12f) + 1f) % 1f * w
                            drawLine(Color(0x36BAE6FD), Offset(rx, rProg * h), Offset(rx - 4f, rProg * h + 16f), strokeWidth = 1.2f)
                            if (idx % 4 == 0) {
                                val fProg = (1f - ((py + weatherFlow * spd * 0.4f) % 1f))
                                val fx = px * w + sin(weatherFlow * 6.28f + idx) * 14f
                                drawCircle(Color(0xFFFDE047).copy(alpha = 0.52f), radius = 2.2f, center = Offset(fx, fProg * h))
                            }
                        }
                        "STARLIGHT_CRIMSON" -> {
                            val fProg = (1f - ((py + weatherFlow * spd * 0.55f) % 1f))
                            val fx = px * w + cos(weatherFlow * 6.28f + idx) * 16f
                            val col = if (idx % 2 == 0) profile.accent else Color(0xFFBAE6FD)
                            drawCircle(col.copy(alpha = 0.65f), radius = 2.1f + (idx % 2), center = Offset(fx, fProg * h))
                        }
                        else -> {
                            val prog = (py + weatherFlow * spd * 0.85f) % 1f
                            val fx = px * w + sin(weatherFlow * 6.28f + idx) * 12f
                            drawCircle(profile.accent.copy(alpha = 0.55f), radius = 2.0f + (idx % 2), center = Offset(fx, prog * h))
                        }
                    }
                }
            }
        }

        // Live Voiced Dialogue Plate + Interactive Actions Deck
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xF50A061C))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedContent(
                targetState = currentDialogue,
                transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(180)) },
                label = "live_dialogue"
            ) { line ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x661C0E3A))
                        .border(0.9.dp, profile.accent.copy(alpha = 0.52f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = profile.displayName.uppercase(),
                        fontFamily = InterFontFamily,
                        color = profile.accent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.9.sp
                    )
                    Text(
                        text = "“$line”",
                        fontFamily = LiterarySerifFontFamily,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            if (subtitle.isNotBlank() && !isFullscreen) {
                Text(
                    text = subtitle,
                    fontFamily = InterFontFamily,
                    color = Color(0xFFBBAED9),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                LiveActionPill("👁 Gözlerine Bak", profile.accent, Modifier.weight(1f)) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    interactionTick++
                    val line = profile.gazeLines[gazeIdx % profile.gazeLines.size]
                    gazeIdx++
                    speakLine(line, "live_${profile.speakerKey.lowercase()}_gaze_$gazeIdx")
                }
                LiveActionPill(if (isLeanedIn) "↩ Geri Çekil" else "🤝 Yaklaş", Color(0xFF38BDF8), Modifier.weight(1f)) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    interactionTick++
                    isLeanedIn = !isLeanedIn
                    if (isLeanedIn) {
                        val line = profile.closerLines[closeIdx % profile.closerLines.size]
                        closeIdx++
                        speakLine(line, "live_${profile.speakerKey.lowercase()}_close_$closeIdx")
                    }
                }
                LiveActionPill("💬 Konuş", Color(0xFFFDE047), Modifier.weight(1f)) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    interactionTick++
                    val line = profile.talkLines[talkIdx % profile.talkLines.size]
                    talkIdx++
                    speakLine(line, "live_${profile.speakerKey.lowercase()}_talk_$talkIdx")
                }
            }
        }
    }
}

@Composable
private fun LiveActionPill(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.18f))
            .border(0.95.dp, accent.copy(alpha = 0.72f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 9.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
