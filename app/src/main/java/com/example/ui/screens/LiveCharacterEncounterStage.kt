package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
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
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.screens.cosmic3d.Cosmic3DSoundEngine
import com.example.ui.screens.cosmic3d.CosmicCharacterVoiceEngine
import com.example.ui.screens.cosmic3d.FacialVisemeFrame
import com.example.ui.screens.cosmic3d.Living3DEncounterMeshRenderer
import com.example.ui.screens.cosmic3d.LivingEncounterTextureLoader
import com.example.ui.screens.cosmic3d.LivingEncounterTextureSet
import com.example.ui.screens.cosmic3d.LivingFaceRigSpec
import com.example.ui.screens.cosmic3d.TurkishSpeechVisemeAnalyzer
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private data class LiveEncounterProfile(
    val speakerKey: String,
    val displayName: String,
    val moodRole: String,
    @param:DrawableRes val lookoutSceneRes: Int,  // 0° side-profile gazing out at scenery
    @param:DrawableRes val midTurnSceneRes: Int,  // 45° pixel-locked intermediate head-turn frame
    @param:DrawableRes val turnedSceneRes: Int,   // 90° pixel-locked eye-contact frame
    @param:DrawableRes val exprSceneRes: Int,     // 90° pixel-locked real closed-eyes + speaking-mouth frame
    @param:DrawableRes val windHairSceneRes: Int? = null, // Optional pixel-locked real wind-swept hair frame
    val rig: LivingFaceRigSpec,
    val accent: Color,
    val ambientType: String, // "BALCONY_CITY", "RAIN_WARM", "STARLIGHT_PIER", "SNOW_BLUE", "DARK_MIRROR"
    val watchesSceneryWhenSilent: Boolean,
    val compassLeftLabel: String,
    val compassCenterLabel: String,
    val compassRightLabel: String,
    val silentStatusLabel: String,
    val speakingStatusLabel: String,
    val openingLine: String,
    val gazeLines: List<String>,
    val closerLines: List<String>,
    val talkLines: List<String>,
    val idleLines: List<String>
)

private fun resolveLiveEncounterProfile(characterKey: String, sceneId: String): LiveEncounterProfile {
    return when (characterKey.uppercase()) {
        "NATASHA" -> {
            val isBalcony = sceneId.contains("BALCONY", ignoreCase = true) || sceneId.contains("FINALE", ignoreCase = true)
            LiveEncounterProfile(
                speakerKey = "NATASHA",
                displayName = "Natasha Romanoff",
                moodRole = when {
                    sceneId.contains("FINALE") -> "GÜN BATIMI TERASI · YENİ BAŞLANGIÇ"
                    sceneId.contains("FARM") || sceneId.contains("VERANDA") -> "YAĞMURLU VERANDA · YALNIZ AN"
                    else -> "AVENGERS KULESİ BALKONU · MANHATTAN"
                },
                lookoutSceneRes = R.drawable.natasha_3d_turned_1795640635973,
                midTurnSceneRes = R.drawable.natasha_3d_turned_1795640635973,
                turnedSceneRes = R.drawable.natasha_3d_turned_1795640635973,
                exprSceneRes = R.drawable.natasha_3d_expr_local,
                windHairSceneRes = null,
                rig = LivingFaceRigSpec(
                    charCenterXLookout = 0.635f,
                    charCenterXTurned = 0.635f,
                    headCenterX = 0.632f,
                    headCenterY = 0.285f,
                    headRadiusX = 0.072f,
                    headRadiusY = 0.145f,
                    leftEyeX = 0.604f,
                    leftEyeY = 0.290f,
                    rightEyeX = 0.654f,
                    rightEyeY = 0.285f,
                    eyePatchRx = 0.022f,
                    eyePatchRy = 0.024f,
                    mouthCenterX = 0.625f,
                    mouthCenterY = 0.404f,
                    mouthPatchRx = 0.036f,
                    mouthPatchRy = 0.044f,
                    hairMinX = 0.540f,
                    hairMaxX = 0.790f,
                    hairMinY = 0.100f,
                    hairMaxY = 0.520f,
                    hasLongFlowingHair = true,
                    turnSign = +1f,
                    windDir = -1f
                ),
                accent = Color(0xFFF472B6),
                ambientType = if (isBalcony) "BALCONY_CITY" else "RAIN_WARM",
                watchesSceneryWhenSilent = true,
                compassLeftLabel = "Manhattan",
                compassCenterLabel = "Natasha",
                compassRightLabel = "Teras",
                silentStatusLabel = if (isBalcony) {
                    "🌃 Balkon korkuluğuna yaslanıp Manhattan'ı izliyor"
                } else {
                    "🌧 Verandadan yağmurlu geceyi izliyor"
                },
                speakingStatusLabel = "🎙 Başını omzunun üzerinden sana çevirip konuşuyor",
                openingLine = when {
                    sceneId.contains("FINALE") ->
                        "Sokovia bitti Aiden... masada sıcacık köfteler, üstümüzde yıldızlar var. İlk kez geleceğe korkmadan bakıyorum."
                    isBalcony ->
                        "Aşağıdaki Manhattan ışıklarına bak Aiden... Yukarıda herkes gülüyor ama en gerçek sessizlik senin yanında."
                    else ->
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
                    "Şehir aşağıda hiç uyumuyor... ama senin yanında ilk kez gerçekten dinlenebiliyorum Aiden.",
                    "Hâlâ buradasın... güzel. Acelemiz yok. Sadece rüzgârı ve geceyi dinleyelim.",
                    "Biliyor musun, ilk kez bir gecenin hiç bitmemesini istiyorum.",
                    "Düşüncelere daldın yine... aşağıya bakarken bile ne düşündüğünü hissedebiliyorum, Blackwood."
                )
            )
        }

        "WANDA" -> LiveEncounterProfile(
            speakerKey = "WANDA",
            displayName = "Wanda Maximoff",
            moodRole = if (sceneId.contains("FINALE")) "GÖL İSKELESİ · KIZIL VE MAVİ" else "YILDIZLI BAHÇE · KIZIL VE MAVİ",
            lookoutSceneRes = R.drawable.wanda_pier_lookout_locked_1795636448939,
            midTurnSceneRes = R.drawable.wanda_pier_turn45_1795636481659,
            turnedSceneRes = R.drawable.wanda_pier_turned_v2_1795634363790,
            exprSceneRes = R.drawable.wanda_pier_expr_1795636420063,
            rig = LivingFaceRigSpec(
                charCenterXLookout = 0.355f,
                charCenterXTurned = 0.355f,
                headCenterX = 0.362f,
                headCenterY = 0.270f,
                headRadiusX = 0.056f,
                headRadiusY = 0.115f,
                leftEyeX = 0.356f,
                leftEyeY = 0.259f,
                rightEyeX = 0.383f,
                rightEyeY = 0.260f,
                eyePatchRx = 0.018f,
                eyePatchRy = 0.018f,
                mouthCenterX = 0.372f,
                mouthCenterY = 0.317f,
                mouthPatchRx = 0.032f,
                mouthPatchRy = 0.036f,
                hairMinX = 0.230f,
                hairMaxX = 0.460f,
                hairMinY = 0.160f,
                hairMaxY = 0.560f,
                hasLongFlowingHair = true,
                turnSign = -1f,
                windDir = +1f
            ),
            accent = Color(0xFFC084FC),
            ambientType = "STARLIGHT_PIER",
            watchesSceneryWhenSilent = true,
            compassLeftLabel = "Bahçe",
            compassCenterLabel = "Wanda",
            compassRightLabel = "Yıldızlı Göl",
            silentStatusLabel = "✨ Gölün sularındaki yıldız yansımalarını izliyor",
            speakingStatusLabel = "🎙 Başını sana çevirip yumuşak bir fısıltıyla konuşuyor",
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
            lookoutSceneRes = R.drawable.img_scene3d_voss_safehouse_wide_1795627490131,
            midTurnSceneRes = R.drawable.voss_safehouse_midturn_1795636451372,
            turnedSceneRes = R.drawable.voss_safehouse_turned_1795634272790,
            exprSceneRes = R.drawable.voss_safehouse_expr_1795636419756,
            rig = LivingFaceRigSpec(
                charCenterXLookout = 0.525f,
                charCenterXTurned = 0.525f,
                headCenterX = 0.512f,
                headCenterY = 0.255f,
                headRadiusX = 0.078f,
                headRadiusY = 0.162f,
                leftEyeX = 0.474f,
                leftEyeY = 0.249f,
                rightEyeX = 0.506f,
                rightEyeY = 0.244f,
                eyePatchRx = 0.021f,
                eyePatchRy = 0.020f,
                mouthCenterX = 0.494f,
                mouthCenterY = 0.350f,
                mouthPatchRx = 0.044f,
                mouthPatchRy = 0.056f,
                hairMinX = 0.450f,
                hairMaxX = 0.590f,
                hairMinY = 0.095f,
                hairMaxY = 0.260f,
                hasLongFlowingHair = false,
                turnSign = +1f,
                windDir = -1f
            ),
            accent = Color(0xFFF87171),
            ambientType = "SNOW_BLUE",
            watchesSceneryWhenSilent = true,
            compassLeftLabel = "Karlı Sokak",
            compassCenterLabel = "Voss",
            compassRightLabel = "Sığınak",
            silentStatusLabel = "❄ Pencereden karlı Brooklyn sokağını kolluyor",
            speakingStatusLabel = "🎙 Omzunun üzerinden sana dönüp konuşuyor",
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
            lookoutSceneRes = R.drawable.img_scene3d_mira_bosphorus_wide_1795627491989,
            midTurnSceneRes = R.drawable.mira_bosphorus_midturn_1795636453273,
            turnedSceneRes = R.drawable.mira_bosphorus_turned_1795634276359,
            exprSceneRes = R.drawable.mira_bosphorus_expr_1795636420061,
            rig = LivingFaceRigSpec(
                charCenterXLookout = 0.345f,
                charCenterXTurned = 0.345f,
                headCenterX = 0.352f,
                headCenterY = 0.285f,
                headRadiusX = 0.068f,
                headRadiusY = 0.132f,
                leftEyeX = 0.350f,
                leftEyeY = 0.281f,
                rightEyeX = 0.382f,
                rightEyeY = 0.276f,
                eyePatchRx = 0.020f,
                eyePatchRy = 0.019f,
                mouthCenterX = 0.371f,
                mouthCenterY = 0.348f,
                mouthPatchRx = 0.036f,
                mouthPatchRy = 0.042f,
                hairMinX = 0.185f,
                hairMaxX = 0.375f,
                hairMinY = 0.160f,
                hairMaxY = 0.640f,
                hasLongFlowingHair = true,
                turnSign = -1f,
                windDir = -1f
            ),
            accent = Color(0xFF38BDF8),
            ambientType = "STARLIGHT_PIER",
            watchesSceneryWhenSilent = true,
            compassLeftLabel = "Sarmaşık",
            compassCenterLabel = "Mira",
            compassRightLabel = "Boğaz Ufku",
            silentStatusLabel = "🌊 Boğaz'ın parlayan ufuk çizgisini izliyor",
            speakingStatusLabel = "🎙 Başını sana çevirip heyecanla konuşuyor",
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
            lookoutSceneRes = R.drawable.img_scene3d_zero_rift_wide_1795627491596,
            midTurnSceneRes = R.drawable.zero_rift_midturn_1795636453880,
            turnedSceneRes = R.drawable.zero_rift_turned_1795634276607,
            exprSceneRes = R.drawable.zero_rift_expr_1795636419519,
            rig = LivingFaceRigSpec(
                charCenterXLookout = 0.465f,
                charCenterXTurned = 0.465f,
                headCenterX = 0.492f,
                headCenterY = 0.305f,
                headRadiusX = 0.076f,
                headRadiusY = 0.145f,
                leftEyeX = 0.498f,
                leftEyeY = 0.299f,
                rightEyeX = 0.531f,
                rightEyeY = 0.303f,
                eyePatchRx = 0.022f,
                eyePatchRy = 0.021f,
                mouthCenterX = 0.511f,
                mouthCenterY = 0.384f,
                mouthPatchRx = 0.038f,
                mouthPatchRy = 0.046f,
                hairMinX = 0.390f,
                hairMaxX = 0.575f,
                hairMinY = 0.140f,
                hairMaxY = 0.350f,
                hasLongFlowingHair = true,
                turnSign = -1f,
                windDir = +1f
            ),
            accent = Color(0xFF38BDF8),
            ambientType = "DARK_MIRROR",
            watchesSceneryWhenSilent = true,
            compassLeftLabel = "Harabe",
            compassCenterLabel = "Sıfır",
            compassRightLabel = "Zaman Yarığı",
            silentStatusLabel = "⚡ Çatlayan zaman ufkunu sessizce süzüyor",
            speakingStatusLabel = "🎙 Başını sana çevirip mavi gözlerini kilitliyor",
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
 * True First-Person Panoramic 3D Living Character Encounter Stage powered by GPU `drawBitmapMesh`.
 *
 * - ZERO black bars on left/right/top/bottom at any pan angle (-78°..+78°) or screen aspect ratio.
 * - ZERO sudden face-zoom jump: smoothly turns the character's head over their shoulder inside the
 *   EXACT same 16:9 panoramic frame via 3D mesh head-yaw pivot + pixel-locked pair cross-projection.
 * - ZERO amateur oval stickers: real-time 32×32 high-res facial sub-mesh deforms the actual
 *   photographed eyelids, irises, lower lip, upper lip, chin, jaw, eyebrows, and wind-blown hair!
 */
@Composable
fun LiveCharacterEncounterStage(
    characterKey: String,
    sceneId: String,
    title: String,
    subtitle: String,
    chapterAccent: Color,
    startFullscreen: Boolean = false,
    onCloseFullscreen: (() -> Unit)? = null
) {
    val profile = remember(characterKey, sceneId) { resolveLiveEncounterProfile(characterKey, sceneId) }
    var isFullscreen by remember(startFullscreen, characterKey, sceneId) { mutableStateOf(startFullscreen) }

    if (!startFullscreen) {
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
    }

    if (isFullscreen) {
        Dialog(
            onDismissRequest = {
                isFullscreen = false
                onCloseFullscreen?.invoke()
            },
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
                    .background(Color(0xFF03020A))
            ) {
                LiveEncounterCoreContent(
                    profile = profile,
                    title = title,
                    subtitle = subtitle,
                    isFullscreen = true,
                    onToggleFullscreen = {
                        isFullscreen = false
                        onCloseFullscreen?.invoke()
                    }
                )
            }
        }
    }
}

@Composable
private fun rememberFirstPersonGyroOffset(enabled: Boolean): Pair<Float, Float> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var gyroYaw by remember { mutableFloatStateOf(0f) }
    var gyroPitch by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context, lifecycleOwner, enabled) {
        if (!enabled) {
            gyroYaw = 0f
            gyroPitch = 0f
            return@DisposableEffect onDispose {}
        }
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ?: return@DisposableEffect onDispose {}
        val sensor = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: return@DisposableEffect onDispose {}

        val rotMat = FloatArray(9)
        val orient = FloatArray(3)
        var sx = 0f
        var sy = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val tx: Float
                val ty: Float
                if (event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotMat, event.values)
                    SensorManager.getOrientation(rotMat, orient)
                    tx = (orient[2] * 26f).coerceIn(-22f, 22f)
                    ty = ((orient[1] + 0.65f) * 14f).coerceIn(-10f, 10f)
                } else {
                    tx = (-event.values[0] * 2.4f).coerceIn(-22f, 22f)
                    ty = ((event.values[1] - 5.5f) * 1.4f).coerceIn(-10f, 10f)
                }
                sx += (tx - sx) * 0.14f
                sy += (ty - sy) * 0.14f
                gyroYaw = sx
                gyroPitch = sy
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        val obs = LifecycleEventObserver { _, ev ->
            when (ev) {
                Lifecycle.Event.ON_PAUSE -> sm.unregisterListener(listener)
                Lifecycle.Event.ON_RESUME -> sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(obs)
            sm.unregisterListener(listener)
        }
    }
    return gyroYaw to gyroPitch
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

    // Decode pixel-locked 3-angle head-turn sequence + real closed-eyes/speaking-mouth/wind-hair patches off UI thread
    var textureSet by remember(profile.speakerKey, profile.lookoutSceneRes, profile.midTurnSceneRes, profile.turnedSceneRes, profile.exprSceneRes) {
        mutableStateOf<LivingEncounterTextureSet?>(null)
    }
    LaunchedEffect(profile.speakerKey, profile.lookoutSceneRes, profile.midTurnSceneRes, profile.turnedSceneRes, profile.exprSceneRes) {
        val loaded = withContext(Dispatchers.IO) {
            LivingEncounterTextureLoader.load(
                context = context,
                lookoutRes = profile.lookoutSceneRes,
                midTurnRes = profile.midTurnSceneRes,
                turnedRes = profile.turnedSceneRes,
                exprRes = profile.exprSceneRes,
                windHairRes = profile.windHairSceneRes,
                rig = profile.rig
            )
        }
        textureSet?.release()
        textureSet = loaded
    }
    DisposableEffect(profile.speakerKey, profile.lookoutSceneRes, profile.midTurnSceneRes, profile.turnedSceneRes, profile.exprSceneRes) {
        onDispose {
            textureSet?.release()
            textureSet = null
        }
    }

    val meshRenderer = remember(profile.speakerKey) { Living3DEncounterMeshRenderer() }

    var voiceEnabled by remember { mutableStateOf(true) }
    var gyroEnabled by remember { mutableStateOf(false) }
    var isLeanedIn by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var attentionLockUntilMs by remember { mutableStateOf(0L) }
    var attentionLocked by remember { mutableStateOf(false) }

    // First-person panoramic head yaw (-78° left scenery .. 0° companion .. +78° right scenery)
    var camYawTarget by remember(profile.speakerKey, isFullscreen) { mutableFloatStateOf(0f) }
    var camPitchTarget by remember(profile.speakerKey, isFullscreen) { mutableFloatStateOf(0f) }

    val (gyroYaw, gyroPitch) = rememberFirstPersonGyroOffset(enabled = gyroEnabled)

    var currentDialogue by remember(profile.speakerKey) { mutableStateOf(profile.openingLine) }
    var interactionTick by remember { mutableIntStateOf(0) }
    var gazeIdx by remember { mutableIntStateOf(0) }
    var closeIdx by remember { mutableIntStateOf(0) }
    var talkIdx by remember { mutableIntStateOf(0) }
    var idleIdx by remember { mutableIntStateOf(0) }

    val sfxEngine = remember(profile.speakerKey, isFullscreen) {
        Cosmic3DSoundEngine(context, enableOrchestralMusic = false)
    }
    val voiceEngine = remember(sfxEngine) {
        CosmicCharacterVoiceEngine(context) { speaking ->
            isSpeaking = speaking
            sfxEngine.setVoiceDucking(speaking)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, sfxEngine, voiceEngine, profile.speakerKey) {
        if (isFullscreen) {
            sfxEngine.start(ambientSceneId = "BLUE_SKY_2006", ambientVolume = 0.28f)
        }
        val initialPrewarm = buildList {
            add(Triple(profile.speakerKey, profile.openingLine, "live_${profile.speakerKey.lowercase()}_open"))
            profile.gazeLines.firstOrNull()?.let { add(Triple(profile.speakerKey, it, "live_${profile.speakerKey.lowercase()}_gaze_1")) }
            profile.closerLines.firstOrNull()?.let { add(Triple(profile.speakerKey, it, "live_${profile.speakerKey.lowercase()}_close_1")) }
            profile.talkLines.firstOrNull()?.let { add(Triple(profile.speakerKey, it, "live_${profile.speakerKey.lowercase()}_talk_1")) }
        }
        voiceEngine.prewarmLines(initialPrewarm)
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    voiceEngine.stopCurrentLine()
                    sfxEngine.setMuted(true)
                }
                Lifecycle.Event.ON_RESUME -> {
                    sfxEngine.setMuted(!voiceEnabled)
                }
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

    // Live 60fps speech visemes + natural speaking head nods + wind gust physics + eye micro-saccades
    var activeViseme by remember { mutableStateOf(FacialVisemeFrame()) }
    var speakingNodDeg by remember { mutableFloatStateOf(0f) }
    var windTimeSec by remember { mutableFloatStateOf(0f) }
    var windGustIntensity by remember { mutableFloatStateOf(0.18f) }
    var microSaccadeXTarget by remember { mutableFloatStateOf(0f) }
    var microSaccadeYTarget by remember { mutableFloatStateOf(0f) }

    val microSaccadeX by animateFloatAsState(
        targetValue = microSaccadeXTarget,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "micro_saccade_x"
    )
    val microSaccadeY by animateFloatAsState(
        targetValue = microSaccadeYTarget,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "micro_saccade_y"
    )

    // Continuous 60fps wind gust envelope
    LaunchedEffect(profile.speakerKey) {
        var lastNs = 0L
        var elapsed = 0f
        while (true) {
            withFrameNanos { now ->
                if (lastNs != 0L) {
                    val dt = ((now - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    elapsed += dt
                    windTimeSec = elapsed
                    val slowCycle = (sin(elapsed * 0.60f) * 0.5f + 0.5f)
                    val gustWave = (sin(elapsed * 1.12f + 1.1f) * 0.5f + 0.5f) * slowCycle * slowCycle
                    val ripple = sin(elapsed * 3.3f) * 0.05f
                    windGustIntensity = (0.14f + gustWave * 0.84f + ripple).coerceIn(0.08f, 1.0f)
                }
                lastNs = now
            }
        }
    }

    // Real-time 60fps Turkish Viseme + Audio RMS Jaw/Lip/Brow rig loop
    LaunchedEffect(isSpeaking, currentDialogue, profile.speakerKey) {
        if (!isSpeaking) {
            // Smooth relax back to closed lips
            var cur = activeViseme
            repeat(8) {
                cur = FacialVisemeFrame(
                    jawDrop = cur.jawDrop * 0.55f,
                    lipWidth = cur.lipWidth * 0.55f,
                    upperLipLift = cur.upperLipLift * 0.55f,
                    browLift = cur.browLift * 0.65f,
                    cheekSmile = cur.cheekSmile * 0.65f
                )
                activeViseme = cur
                speakingNodDeg *= 0.65f
                delay(16L)
            }
            activeViseme = FacialVisemeFrame()
            speakingNodDeg = 0f
            return@LaunchedEffect
        }
        var t = 0f
        var lastNs = 0L
        var smoothedJaw = 0f
        var smoothedWidth = 0f
        var smoothedUpper = 0f
        var smoothedBrow = 0f
        var smoothedSmile = 0f

        while (isSpeaking) {
            withFrameNanos { now ->
                if (lastNs == 0L) {
                    lastNs = now
                    return@withFrameNanos
                }
                val dt = ((now - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNs = now
                t += dt

                val rms = voiceEngine.currentSpeechAmplitude()
                val target = TurkishSpeechVisemeAnalyzer.sampleViseme(
                    dialogueText = currentDialogue,
                    speechElapsedSec = t,
                    audioRms = rms
                )
                val jawBlend = if (target.jawDrop > smoothedJaw) 0.52f else 0.30f
                smoothedJaw += (target.jawDrop - smoothedJaw) * jawBlend
                smoothedWidth += (target.lipWidth - smoothedWidth) * 0.34f
                smoothedUpper += (target.upperLipLift - smoothedUpper) * 0.42f
                smoothedBrow += (target.browLift - smoothedBrow) * 0.22f
                smoothedSmile += (target.cheekSmile - smoothedSmile) * 0.24f

                activeViseme = FacialVisemeFrame(
                    jawDrop = smoothedJaw.coerceIn(0f, 1f),
                    lipWidth = smoothedWidth.coerceIn(-0.36f, 0.36f),
                    upperLipLift = smoothedUpper.coerceIn(0f, 0.35f),
                    browLift = smoothedBrow.coerceIn(0f, 0.32f),
                    cheekSmile = smoothedSmile.coerceIn(0f, 0.25f)
                )
                speakingNodDeg = sin(t * 3.8f) * (0.45f + smoothedJaw * 0.95f)
            }
        }
    }

    // Subtle organic eye micro-saccades
    LaunchedEffect(profile.speakerKey) {
        val rng = Random(profile.speakerKey.hashCode() + 99)
        while (true) {
            delay(rng.nextLong(1250L, 2800L))
            microSaccadeXTarget = (rng.nextFloat() - 0.5f) * 0.28f
            microSaccadeYTarget = (rng.nextFloat() - 0.5f) * 0.18f
        }
    }

    // 100% synchronized natural human blinking (both eyes close & open at the exact same millisecond!)
    var blinkTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(profile.speakerKey) {
        val rng = Random(profile.speakerKey.hashCode())
        while (true) {
            delay(rng.nextLong(2200L, 3900L))
            blinkTarget = 1.0f
            delay(105L)
            blinkTarget = 0f
            if (rng.nextFloat() < 0.24f) {
                delay(180L)
                blinkTarget = 1.0f
                delay(92L)
                blinkTarget = 0f
            }
        }
    }

    val smoothBlink by animateFloatAsState(
        targetValue = blinkTarget,
        animationSpec = tween(durationMillis = if (blinkTarget > 0.5f) 58 else 102, easing = FastOutSlowInEasing),
        label = "blink_sync"
    )

    // Hold eye contact briefly (2.6s) after finishing a line, then smoothly turn head back (90° -> 45° -> 0°) to scenery!
    LaunchedEffect(attentionLockUntilMs, isSpeaking) {
        if (isSpeaking) {
            attentionLocked = true
        } else if (attentionLockUntilMs > 0L) {
            attentionLocked = true
            val rem = (attentionLockUntilMs - System.currentTimeMillis()).coerceAtLeast(0L)
            if (rem > 0L) delay(rem)
            if (!isSpeaking) attentionLocked = false
        } else {
            attentionLocked = false
        }
    }

    fun speakLine(line: String, lineKey: String, autoTurnCameraToHer: Boolean = false) {
        currentDialogue = line
        attentionLockUntilMs = System.currentTimeMillis() + 2600L
        attentionLocked = true
        if (autoTurnCameraToHer && abs(camYawTarget) > 22f) {
            camYawTarget = 0f
            camPitchTarget = 0f
        }
        if (voiceEnabled) {
            voiceEngine.speak(profile.speakerKey, line, lineKey)
        }
    }

    // Start in SILENT SCENERY LOOKOUT (0° profile) for 1.5s, then smoothly turn head (0° -> 45° -> 90°) as she speaks!
    LaunchedEffect(isFullscreen, profile.speakerKey) {
        if (isFullscreen && voiceEnabled) {
            delay(1500L)
            if (interactionTick == 0) {
                speakLine(profile.openingLine, "live_${profile.speakerKey.lowercase()}_open")
            }
        }
    }

    // Organic Idle Spoken Personality System
    LaunchedEffect(interactionTick, isFullscreen, profile.speakerKey) {
        val waitMs = when (idleIdx) {
            0 -> if (isFullscreen) 11500L else 14000L
            1 -> 14000L
            2 -> 16000L
            else -> 18000L
        }
        delay(waitMs)
        val line = profile.idleLines[idleIdx % profile.idleLines.size]
        idleIdx++
        speakLine(line, "live_${profile.speakerKey.lowercase()}_idle_$idleIdx")
    }

    val inf = rememberInfiniteTransition(label = "live3d_fps_${profile.speakerKey}")
    val breath by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Reverse),
        label = "breath"
    )
    val weatherFlow by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing), RepeatMode.Restart),
        label = "weather"
    )

    // ONLY zoom in subtly when the user explicitly taps "🤝 Yaklaş" — NEVER jump-zoom when turning head!
    val smoothLean by animateFloatAsState(
        targetValue = if (isLeanedIn) 1.30f else 1.0f,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = Spring.StiffnessLow),
        label = "lean_zoom"
    )
    val smoothCamYaw by animateFloatAsState(
        targetValue = (camYawTarget + gyroYaw).coerceIn(-78f, 78f),
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "fp_cam_yaw"
    )
    val smoothCamPitch by animateFloatAsState(
        targetValue = (camPitchTarget + gyroPitch).coerceIn(-18f, 18f),
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "fp_cam_pitch"
    )

    val isLookingAwayFromCompanion = abs(smoothCamYaw) > 42f
    val isLookingAtPlayer = (isSpeaking || attentionLocked || isLeanedIn || !profile.watchesSceneryWhenSilent) && !isLookingAwayFromCompanion

    // Crisp 520ms real 3-keyframe head-turn animation (0° profile -> 45° oblique mid-turn -> 90° eye contact)
    val turnToPlayerProgress by animateFloatAsState(
        targetValue = if (isLookingAtPlayer) 1f else 0f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "head_turn_progress"
    )

    val particles = remember(profile.speakerKey) {
        List(36) { i ->
            Triple(Random(i * 31 + 7).nextFloat(), Random(i * 53 + 13).nextFloat(), 0.6f + Random(i * 19).nextFloat() * 0.9f)
        }
    }
    val cityLights = remember(profile.speakerKey) {
        List(28) { i ->
            Triple(
                Random(i * 71 + 11).nextFloat(),
                0.42f + Random(i * 29 + 5).nextFloat() * 0.45f,
                2.2f + Random(i * 13 + 3).nextFloat() * 4.2f
            )
        }
    }

    val stageBoxModifier = if (isFullscreen) {
        Modifier.fillMaxSize()
    } else {
        Modifier
            .fillMaxWidth()
            .height(410.dp)
    }

    Box(modifier = if (isFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth()) {
        Column(modifier = if (isFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth()) {
            BoxWithConstraints(
                modifier = stageBoxModifier
                    .background(Color(0xFF04030D))
                    .pointerInput(profile.speakerKey, isFullscreen) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                interactionTick++
                                // Natural first-person look-around: drag left -> look right, drag right -> look left
                                camYawTarget = (camYawTarget - dragAmount.x * 0.22f).coerceIn(-78f, 78f)
                                camPitchTarget = (camPitchTarget - dragAmount.y * 0.10f).coerceIn(-18f, 18f)
                            }
                        )
                    }
            ) {
                val decoded = textureSet
                if (decoded == null) {
                    // Instant zero-flash full-bleed preview while IO bitmap decode completes (~30ms)
                    Image(
                        painter = painterResource(id = profile.lookoutSceneRes),
                        contentDescription = "${profile.displayName} — ${profile.moodRole}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // ── GPU DRAWBITMAPMESH LIVING 3D PANORAMA + HIGH-RES FACIAL RIG ──
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val trackGazeX = ((-smoothCamYaw / 78f) * 0.65f + microSaccadeX).coerceIn(-0.95f, 0.95f)
                        val trackGazeY = ((-smoothCamPitch / 18f) * 0.45f + microSaccadeY).coerceIn(-0.75f, 0.75f)
                        drawIntoCanvas { composeCanvas ->
                            meshRenderer.drawScene(
                                canvas = composeCanvas.nativeCanvas,
                                textures = decoded,
                                rig = profile.rig,
                                params = Living3DEncounterMeshRenderer.FrameParams(
                                    canvasW = size.width,
                                    canvasH = size.height,
                                    camYawDeg = smoothCamYaw,
                                    camPitchDeg = smoothCamPitch,
                                    leanZoom = smoothLean,
                                    turnProgress = turnToPlayerProgress,
                                    breathPhase = breath,
                                    windTimeSec = windTimeSec,
                                    windGust = windGustIntensity,
                                    blink = smoothBlink,
                                    gazeX = trackGazeX,
                                    gazeY = trackGazeY,
                                    speakingNodDeg = speakingNodDeg,
                                    viseme = activeViseme
                                )
                            )
                        }
                    }
                }

                // ── LAYER 2: SHARED 3D ATMOSPHERIC PARTICLES (RAIN / STARLIGHT / SNOW / EMBERS) ──
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val fgShiftX = -smoothCamYaw * (w * 0.014f)
                    val fgShiftY = -smoothCamPitch * (h * 0.009f)

                    if (profile.ambientType == "BALCONY_CITY" || profile.ambientType == "RAIN_WARM") {
                        cityLights.take(14).forEachIndexed { idx, (lx, ly, r) ->
                            val sx = (((lx * w * 1.6f - w * 0.3f + fgShiftX * 0.55f) % (w * 1.4f)) + w * 1.4f) % (w * 1.4f) - w * 0.2f
                            val sy = ly * h + fgShiftY * 0.45f
                            val tw = 0.35f + 0.65f * sin(weatherFlow * 6.28f + idx * 1.4f)
                            val col = if (idx % 2 == 0) Color(0xFFFDE047) else Color(0xFF38BDF8)
                            drawCircle(col.copy(alpha = tw * 0.16f), radius = r * 1.6f, center = Offset(sx, sy))
                        }
                    } else if (profile.ambientType == "STARLIGHT_PIER") {
                        for (row in 0..4) {
                            val ry = h * (0.66f + row * 0.05f) + fgShiftY * 0.40f
                            val wave = sin(weatherFlow * 6.28f + row * 0.95f) * (10f + windGustIntensity * 8f)
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(Color.Transparent, profile.accent.copy(alpha = 0.18f), Color(0x44BAE6FD), Color.Transparent)
                                ),
                                start = Offset(w * 0.16f + wave + fgShiftX * 0.35f, ry),
                                end = Offset(w * 0.86f + wave + fgShiftX * 0.35f, ry),
                                strokeWidth = 1.8f
                            )
                        }
                    }

                    val gustSlant = profile.rig.windDir * (8f + windGustIntensity * 24f)
                    particles.forEachIndexed { idx, (px, py, spd) ->
                        when (profile.ambientType) {
                            "BALCONY_CITY", "RAIN_WARM" -> {
                                val rProg = (py + weatherFlow * spd * (1.72f + windGustIntensity * 0.52f)) % 1f
                                val rx = ((((px + profile.rig.windDir * rProg * (0.10f + windGustIntensity * 0.14f)) % 1f) + 1f) % 1f) * w
                                drawLine(
                                    color = Color(0xFFBAE6FD).copy(alpha = 0.14f + windGustIntensity * 0.14f),
                                    start = Offset(rx, rProg * h),
                                    end = Offset(rx + gustSlant * 0.32f, rProg * h + 14f),
                                    strokeWidth = 1.15f
                                )
                                if (windGustIntensity > 0.58f && idx < 3) {
                                    val mistAlpha = ((windGustIntensity - 0.58f) / 0.42f) * 0.10f
                                    val mx = (((weatherFlow * 1.35f + idx * 0.28f) % 1f) * w * 1.4f - w * 0.2f)
                                    val my = h * (0.22f + idx * 0.16f)
                                    drawLine(
                                        brush = Brush.horizontalGradient(
                                            listOf(Color.Transparent, Color(0xFFE0F2FE).copy(alpha = mistAlpha), Color.Transparent)
                                        ),
                                        start = Offset(mx, my),
                                        end = Offset(mx + profile.rig.windDir * w * 0.36f, my + 7f),
                                        strokeWidth = 2.2f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                            "STARLIGHT_PIER" -> {
                                val fProg = (1f - ((py + weatherFlow * spd * 0.46f) % 1f))
                                val fx = (px * w + cos(weatherFlow * 6.28f + idx) * (13f + windGustIntensity * 12f) + fgShiftX * 0.2f + w) % w
                                val col = if (idx % 2 == 0) profile.accent else Color(0xFFBAE6FD)
                                drawCircle(col.copy(alpha = 0.54f), radius = 1.9f + (idx % 2), center = Offset(fx, fProg * h))
                            }
                            else -> {
                                val prog = (py + weatherFlow * spd * 0.84f) % 1f
                                val fx = (px * w + sin(weatherFlow * 6.28f + idx) * 14f + fgShiftX * 0.24f + w) % w
                                drawCircle(profile.accent.copy(alpha = 0.50f), radius = 1.9f + (idx % 2), center = Offset(fx, prog * h))
                            }
                        }
                    }
                }

                // Soft Top & Bottom Cinema Vignettes for crystal-clear floating HUD legibility
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isFullscreen) 136.dp else 88.dp)
                        .align(Alignment.TopCenter)
                        .background(Brush.verticalGradient(listOf(Color(0xD8050311), Color.Transparent)))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isFullscreen) 280.dp else 104.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xB4060414), Color(0xEE050310))
                            )
                        )
                )

                // ── FLOATING TOP BAR + FIRST-PERSON COMPASS HUD ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .then(
                            if (isFullscreen) {
                                Modifier.statusBarsPadding().padding(top = 14.dp, start = 14.dp, end = 14.dp)
                            } else {
                                Modifier.padding(top = 10.dp, start = 12.dp, end = 12.dp)
                            }
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✦ 1. ŞAHIS 3D SAHNE · ${profile.moodRole}",
                                fontFamily = InterFontFamily,
                                color = profile.accent,
                                fontSize = 8.6.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = title.ifBlank { profile.displayName },
                                fontFamily = DisplaySerifFontFamily,
                                color = Color.White,
                                fontSize = if (isFullscreen) 16.sp else 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (isFullscreen) {
                                Box(
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (gyroEnabled) Color(0x4438BDF8) else Color(0x99120A28))
                                        .border(0.9.dp, if (gyroEnabled) Color(0xFF38BDF8) else Color(0x55FFFFFF), RoundedCornerShape(50))
                                        .clickable { gyroEnabled = !gyroEnabled }
                                        .testTag("live3d_gyro_toggle")
                                        .padding(horizontal = 11.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (gyroEnabled) "🧭 AÇIK" else "🧭",
                                        fontFamily = InterFontFamily,
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (voiceEnabled) profile.accent.copy(alpha = 0.25f) else Color(0x99120A28))
                                    .border(0.9.dp, if (voiceEnabled) profile.accent else Color(0x66FFFFFF), RoundedCornerShape(50))
                                    .clickable {
                                        voiceEnabled = !voiceEnabled
                                        voiceEngine.setMuted(!voiceEnabled)
                                    }
                                    .testTag("live3d_voice_toggle")
                                    .padding(horizontal = 11.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (voiceEnabled) "🔊" else "🔇",
                                    fontFamily = InterFontFamily,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xB8140C2E))
                                    .border(0.9.dp, Color(0xCCBAE6FD), RoundedCornerShape(50))
                                    .clickable { onToggleFullscreen() }
                                    .testTag(if (isFullscreen) "live3d_close_fullscreen" else "live3d_open_fullscreen")
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
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

                    // FIRST-PERSON 360° HEAD-TURN COMPASS BAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x88080518))
                            .border(0.8.dp, Color(0x44BAE6FD), RoundedCornerShape(50))
                            .clickable {
                                camYawTarget = 0f
                                camPitchTarget = 0f
                            }
                            .testTag("live3d_compass_reset")
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val yawDeg = smoothCamYaw.roundToInt()
                        Text(
                            text = "← ${profile.compassLeftLabel}",
                            fontFamily = InterFontFamily,
                            color = if (yawDeg < -20) Color(0xFF7DD3FC) else Color(0x99CBD5E1),
                            fontSize = 8.8.sp,
                            fontWeight = if (yawDeg < -20) FontWeight.ExtraBold else FontWeight.Medium
                        )
                        Text(
                            text = when {
                                abs(yawDeg) > 16 -> "↺ ${if (yawDeg > 0) "+$yawDeg°" else "$yawDeg°"} · ${profile.compassCenterLabel}'ya Dön"
                                windGustIntensity > 0.62f && profile.rig.hasLongFlowingHair -> "🌬 Gece Rüzgârı · ↔ Sürükleyip Etrafa Bak"
                                else -> "↔ Parmağınla 1. Şahıs Kamerayı Döndür"
                            },
                            fontFamily = InterFontFamily,
                            color = Color(0xFFE0F2FE),
                            fontSize = 8.8.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${profile.compassRightLabel} →",
                            fontFamily = InterFontFamily,
                            color = if (yawDeg > 20) Color(0xFFFDE047) else Color(0x99CBD5E1),
                            fontSize = 8.8.sp,
                            fontWeight = if (yawDeg > 20) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                }

                // Live Choreography Status Pill (inline card sits at bottom of viewport; fullscreen pill lives inside deck to guarantee zero text overlap!)
                if (!isFullscreen) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp, start = 14.dp, end = 14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xB50B061D))
                            .border(0.85.dp, profile.accent.copy(alpha = if (isSpeaking) 0.88f else 0.46f), RoundedCornerShape(50))
                            .padding(horizontal = 11.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSpeaking) Color(0xFF34D399) else profile.accent)
                        )
                        Text(
                            text = if (turnToPlayerProgress > 0.55f) profile.speakingStatusLabel else profile.silentStatusLabel,
                            fontFamily = InterFontFamily,
                            color = Color(0xFFF3E8FF),
                            fontSize = 9.2.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Fullscreen Floating Dialogue & Interactive Action Deck
                if (isFullscreen) {
                    LiveDialogueAndActionsDeck(
                        profile = profile,
                        currentDialogue = currentDialogue,
                        subtitle = "",
                        statusLabel = if (!isLookingAwayFromCompanion) {
                            if (turnToPlayerProgress > 0.55f) profile.speakingStatusLabel else profile.silentStatusLabel
                        } else {
                            null
                        },
                        isSpeaking = isSpeaking,
                        isLeanedIn = isLeanedIn,
                        isLookingAway = isLookingAwayFromCompanion,
                        isFullscreen = true,
                        onGaze = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            interactionTick++
                            val line = profile.gazeLines[gazeIdx % profile.gazeLines.size]
                            gazeIdx++
                            speakLine(line, "live_${profile.speakerKey.lowercase()}_gaze_$gazeIdx", autoTurnCameraToHer = true)
                            val nextGaze = profile.gazeLines[gazeIdx % profile.gazeLines.size]
                            voiceEngine.prewarmLine(profile.speakerKey, nextGaze, "live_${profile.speakerKey.lowercase()}_gaze_${gazeIdx + 1}")
                        },
                        onCloser = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            interactionTick++
                            isLeanedIn = !isLeanedIn
                            if (isLeanedIn) {
                                camYawTarget = 0f
                                camPitchTarget = 0f
                                val line = profile.closerLines[closeIdx % profile.closerLines.size]
                                closeIdx++
                                speakLine(line, "live_${profile.speakerKey.lowercase()}_close_$closeIdx", autoTurnCameraToHer = true)
                                val nextClose = profile.closerLines[closeIdx % profile.closerLines.size]
                                voiceEngine.prewarmLine(profile.speakerKey, nextClose, "live_${profile.speakerKey.lowercase()}_close_${closeIdx + 1}")
                            }
                        },
                        onTalk = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            interactionTick++
                            val line = profile.talkLines[talkIdx % profile.talkLines.size]
                            talkIdx++
                            speakLine(line, "live_${profile.speakerKey.lowercase()}_talk_$talkIdx", autoTurnCameraToHer = true)
                            val nextTalk = profile.talkLines[talkIdx % profile.talkLines.size]
                            voiceEngine.prewarmLine(profile.speakerKey, nextTalk, "live_${profile.speakerKey.lowercase()}_talk_${talkIdx + 1}")
                        },
                        onLookAtCityOrCompanion = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            interactionTick++
                            if (isLookingAwayFromCompanion) {
                                camYawTarget = 0f
                                camPitchTarget = 0f
                            } else {
                                camYawTarget = if (profile.rig.turnSign > 0f) -64f else 64f
                                camPitchTarget = 4f
                                attentionLocked = false
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    )
                }
            }

            // Inline (embedded in chapter card) dialogue & actions below the 3D stage
            if (!isFullscreen) {
                LiveDialogueAndActionsDeck(
                    profile = profile,
                    currentDialogue = currentDialogue,
                    subtitle = subtitle,
                    isSpeaking = isSpeaking,
                    isLeanedIn = isLeanedIn,
                    isLookingAway = isLookingAwayFromCompanion,
                    isFullscreen = false,
                    onGaze = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        interactionTick++
                        val line = profile.gazeLines[gazeIdx % profile.gazeLines.size]
                        gazeIdx++
                        speakLine(line, "live_${profile.speakerKey.lowercase()}_gaze_$gazeIdx", autoTurnCameraToHer = true)
                        val nextGaze = profile.gazeLines[gazeIdx % profile.gazeLines.size]
                        voiceEngine.prewarmLine(profile.speakerKey, nextGaze, "live_${profile.speakerKey.lowercase()}_gaze_${gazeIdx + 1}")
                    },
                    onCloser = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        interactionTick++
                        isLeanedIn = !isLeanedIn
                        if (isLeanedIn) {
                            camYawTarget = 0f
                            camPitchTarget = 0f
                            val line = profile.closerLines[closeIdx % profile.closerLines.size]
                            closeIdx++
                            speakLine(line, "live_${profile.speakerKey.lowercase()}_close_$closeIdx", autoTurnCameraToHer = true)
                            val nextClose = profile.closerLines[closeIdx % profile.closerLines.size]
                            voiceEngine.prewarmLine(profile.speakerKey, nextClose, "live_${profile.speakerKey.lowercase()}_close_${closeIdx + 1}")
                        }
                    },
                    onTalk = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        interactionTick++
                        val line = profile.talkLines[talkIdx % profile.talkLines.size]
                        talkIdx++
                        speakLine(line, "live_${profile.speakerKey.lowercase()}_talk_$talkIdx", autoTurnCameraToHer = true)
                        val nextTalk = profile.talkLines[talkIdx % profile.talkLines.size]
                        voiceEngine.prewarmLine(profile.speakerKey, nextTalk, "live_${profile.speakerKey.lowercase()}_talk_${talkIdx + 1}")
                    },
                    onLookAtCityOrCompanion = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        interactionTick++
                        if (abs(camYawTarget) > 30f) {
                            camYawTarget = 0f
                            camPitchTarget = 0f
                        } else {
                            camYawTarget = if (profile.rig.turnSign > 0f) -64f else 64f
                            camPitchTarget = 4f
                            attentionLocked = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LiveDialogueAndActionsDeck(
    profile: LiveEncounterProfile,
    currentDialogue: String,
    subtitle: String,
    statusLabel: String? = null,
    isSpeaking: Boolean,
    isLeanedIn: Boolean,
    isLookingAway: Boolean,
    isFullscreen: Boolean,
    onGaze: () -> Unit,
    onCloser: () -> Unit,
    onTalk: () -> Unit,
    onLookAtCityOrCompanion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(if (isFullscreen) Color.Transparent else Color(0xF50A061C))
            .then(
                if (isFullscreen) {
                    Modifier.navigationBarsPadding().padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 30.dp)
                } else {
                    Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                }
            ),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!statusLabel.isNullOrBlank() && isFullscreen) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xB50B061D))
                    .border(0.85.dp, profile.accent.copy(alpha = if (isSpeaking) 0.88f else 0.46f), RoundedCornerShape(50))
                    .padding(horizontal = 11.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSpeaking) Color(0xFF34D399) else profile.accent)
                )
                Text(
                    text = statusLabel,
                    fontFamily = InterFontFamily,
                    color = Color(0xFFF3E8FF),
                    fontSize = 9.2.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        AnimatedContent(
            targetState = currentDialogue,
            transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(160)) },
            label = "live_dialogue"
        ) { line ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .background(if (isFullscreen) Color(0xBF0E0720) else Color(0x661C0E3A))
                    .border(
                        width = if (isSpeaking) 1.25.dp else 0.9.dp,
                        color = profile.accent.copy(alpha = if (isSpeaking) 0.90f else 0.50f),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .padding(horizontal = 13.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profile.displayName.uppercase(),
                        fontFamily = InterFontFamily,
                        color = profile.accent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.9.sp
                    )
                    if (isSpeaking) {
                        Text(
                            text = "🎙 KONUŞUYOR",
                            fontFamily = InterFontFamily,
                            color = Color(0xFF6EE7B7),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Text(
                    text = "“$line”",
                    fontFamily = LiterarySerifFontFamily,
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                    fontSize = 13.2.sp,
                    lineHeight = 19.sp
                )
            }
        }

        if (subtitle.isNotBlank() && !isFullscreen) {
            Text(
                text = subtitle,
                fontFamily = InterFontFamily,
                color = Color(0xFFBBAED9),
                fontSize = 10.8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LiveActionPill("👁 Gözlerine", profile.accent, Modifier.weight(1f), "live3d_btn_gaze", onClick = onGaze)
            LiveActionPill(if (isLeanedIn) "↩ Çekil" else "🤝 Yaklaş", Color(0xFF38BDF8), Modifier.weight(1f), "live3d_btn_closer", onClick = onCloser)
            LiveActionPill("💬 Konuş", Color(0xFFFDE047), Modifier.weight(1f), "live3d_btn_talk", onClick = onTalk)
            LiveActionPill(
                label = if (isLookingAway) "↩ Ona Bak" else "🌃 Manzara",
                accent = Color(0xFFA78BFA),
                modifier = Modifier.weight(1f),
                tag = "live3d_btn_scenery",
                onClick = onLookAtCityOrCompanion
            )
        }
    }
}

@Composable
private fun LiveActionPill(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xB80E0822))
            .border(0.95.dp, accent.copy(alpha = 0.72f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(tag)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            color = Color.White,
            fontSize = 9.8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
