package com.example.ui.screens

import android.content.Context
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.BotEntity
import com.example.data.local.UserSettingsEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private val CenteredButtonTextStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)

private val PopoverCubicBezier = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

private enum class AidenBadgeIconType {
    TROPHY,
    STAR,
    EYE,
    RED_ORB,
    PINE_TREE,
    LIGHTNING
}

private data class AidenBadgeSpec(
    val iconType: AidenBadgeIconType,
    val label: String,
    val badgeGlowColor: Color
)

private data class AidenReactiveThemeColors(
    val primaryOrb: Color,
    val secondaryOrb: Color,
    val deepAtmosphere: Color,
    val emberAccent: Color
)

private data class AidenCatalogRowSpec(
    val presetId: String,
    val title: String,
    val fullTitle: String,
    val carouselTitle: String,
    val carouselSubtitle: String,
    val description: String,
    val fullDescription: String,
    val universeRulesSummary: String,
    val openingScenePreview: String,
    val thumbDrawableRes: Int,
    val carouselDrawableRes: Int,
    val accentTint: Color,
    val reactiveTheme: AidenReactiveThemeColors,
    val badges: List<AidenBadgeSpec>
)

/**
 * Single Source of Truth (SSOT) for the 5 Aiden Blackwood Universe Catalog Stories:
 * Feeds the Top Hero Carousel, the ANA EVREN Featured Card, the 5 Story Row Cards,
 * the Reactive Ambient Background, and the Floating Glass Story Preview Popover.
 */
private val AIDEN_CATALOG_SPECS = listOf(
    AidenCatalogRowSpec(
        presetId = "preset_aiden_zoktay",
        title = "Galatasaray Efsanesi",
        fullTitle = "Galatasaray Efsanesi — Aiden Blackwood & Zoktay",
        carouselTitle = "Galatasaray Evreni",
        carouselSubtitle = "Aslanın hikayesi burada başlıyor, İstanbul'un 210M€'luk ikonu sahnede.",
        description = "Sarı-Kırmızılıların unutulmaz hikayesi, 21 yaşındaki süperstarın İstanbul ve Boğaz'daki efsanevi yolculuğu.",
        fullDescription = "Galatasaray'ın 21 yaşındaki dünya çapındaki süperstarı Aiden Da Silva De la Turco Blackwood, 210 milyon Euro piyasa değeri ve 60 milyon takipçisiyle İstanbul'un tartışmasız ikonudur. Sahada 4-2-3-1 sisteminde serbest santrfor olarak rakiplerini psikolojik ve fiziksel olarak domine ederken, saha dışında Boğaz'daki yalısı, Manifest müzik grubunun yaşadığı ManiHouse (Zeynep Sude Oktay, Sueda, Hilal, Lidya, Mina, Esin) ve Hollywood yıldızı eski sevgilisi Emma Myers arasındaki yüksek gerilimli bağlarla çevrilidir.",
        universeRulesSummary = "• Rol & Kimlik: Aiden Blackwood (21), Galatasaray #9 Serbest Santrfor.\n• Mekânlar: RAMS Park, Kemerburgaz Tesisleri, Aiden'ın Boğaz Yalısı ve ManiHouse.\n• Kilit Karakterler: Zeynep Sude Oktay (Manifest), Emma Myers (Hollywood yıldızı) ve İspanya'da yaşayan otoriter annesi Marie Blackwood.",
        openingScenePreview = "Öğleden sonra İstanbul'un kaosu çoktan çökmüştü; trafik, telefonlar, manşetler ve kameralar... Galatasaray tesislerinden uzakta, yüksek tavanlı sessiz Boğaz yalımda günün ağırlığını omuzlarımda hissediyordum. Pencereye doğru yürüdüm, telefonumu masaya bıraktım ve derin bir nefes aldım: \"Bazı günler her şey yolundaymış gibi davranmak zorundasın.\"",
        thumbDrawableRes = R.drawable.img_aiden_wide_thumb1_gs,
        carouselDrawableRes = R.drawable.img_aiden_modal_carousel_gs,
        accentTint = Color(0xFFF59E0B),
        reactiveTheme = AidenReactiveThemeColors(
            primaryOrb = Color(0xFFF59E0B),    // Amber 500
            secondaryOrb = Color(0xFFDC2626),  // Red 600
            deepAtmosphere = Color(0xFF581C87),// Purple 900
            emberAccent = Color(0xFFFBBF24)
        ),
        badges = listOf(
            AidenBadgeSpec(AidenBadgeIconType.TROPHY, "Spor", Color(0xFFF59E0B)),
            AidenBadgeSpec(AidenBadgeIconType.STAR, "Özel Hikaye", Color(0xFFFBBF24))
        )
    ),
    AidenCatalogRowSpec(
        presetId = "preset_aiden_dispatch",
        title = "SDN Torrance: İkinci Şans",
        fullTitle = "SDN Torrance: İkinci Şans — Dispatch & Blonde Blazer",
        carouselTitle = "SDN Torrance: İkinci Şans",
        carouselSubtitle = "Blonde Blazer'ın davet ettiği SDN'de Aiden'ın beyaz gözleri ve sınıflandırılamaz güçleri sınanıyor.",
        description = "Dispatch evreninde SDN Torrance'a kabul edilen Aiden: beyaz gözler, klonlar, ışınlanma ve Blonde Blazer'la yeni bir başlangıç.",
        fullDescription = "Dispatch evreninde SDN Torrance merkezine Blonde Blazer tarafından 'ikinci bir şans' olarak dahil edilen Aiden Blackwood, sinir sistemine bağlı sınıflandırılamaz güçlere sahiptir. Normalde mavi olan gözleri, algı ve reflekslerini aşırı hızlandırdığında tıpkı Herobrine gibi tamamen parlak beyaza döner; göz temasıyla hedeflerine suçluluk dolu halüsinasyonlar yaşatır, kısa mesafe ışınlanır ve fiziksel klonlar üretir. Kontrolünü kaybettiğinde ise gözleri kor kırmızıya (Entity-303) bürünerek bilinçaltının devasa Enerji Avatarı'nı serbest bırakır.",
        universeRulesSummary = "• Güç Kademeleri: Mavi Gözler (Normal) → Parlak Beyaz Gözler (Zihin Kontrolü, Halüsinasyon, Klon & Işınlanma) → Kor Kırmızı Gözler (Yıkıcı Enerji Avatarı).\n• Merkez & Ekip: SDN Torrance Binası, Blonde Blazer gözetiminde gri-ahlaklı Z Team operasyonları.\n• Psikolojik Zırh: 5 yaşında ailesini kaybeden Aiden duygularını kusursuz bir özgüven zırhı altında gizler.",
        openingScenePreview = "SDN binasının girişinde atmosfer sakin ama amaç yüklüydü. Aiden, Blonde Blazer'ın yanında duruyordu. Blonde Blazer içten bir sesle konuştu: \"Hoş geldin Aiden. Burası SDN. Buraya gelen herkesin bir geçmişi vardır ama burada kimse yalnızca geçmişiyle yargılanmaz. Hikayen burada bitmek zorunda değil...\"",
        thumbDrawableRes = R.drawable.img_aiden_wide_thumb2_dispatch,
        carouselDrawableRes = R.drawable.img_aiden_wide_thumb2_dispatch,
        accentTint = Color(0xFF38BDF8),
        reactiveTheme = AidenReactiveThemeColors(
            primaryOrb = Color(0xFF22D3EE),    // Cyan 400
            secondaryOrb = Color(0xFF2563EB),  // Blue 600
            deepAtmosphere = Color(0xFF1E1B4B),// Indigo 950
            emberAccent = Color(0xFF38BDF8)
        ),
        badges = listOf(
            AidenBadgeSpec(AidenBadgeIconType.EYE, "Dispatch • Süper Güç", Color(0xFF38BDF8))
        )
    ),
    AidenCatalogRowSpec(
        presetId = "preset_aiden_joker_emma",
        title = "Viren City: Gölgedeki Joker",
        fullTitle = "Viren City: Gölgedeki Joker — Aiden & Emma Myers",
        carouselTitle = "Viren City: Gölgedeki Joker",
        carouselSubtitle = "Gündüz sakin restoran sahibi, gece şehri sarsan Joker; Emma Myers her şeyi değiştiriyor.",
        description = "Viren City'de çift kişilikle yaşayan Aiden'ın gece sessiz restoranına ünlü aktris Emma Myers sığındığında karanlık sırlar uyanıyor.",
        fullDescription = "Viren City'de sakin bir restoran işleten Aiden Blackwood, açıklayamadığı zaman boşlukları, uykusuzluk ve gizemli yaralarla uyanmaktadır. Çocukluğunda ailesinin gözleri önünde katledilmesiyle zihni hayatta kalmak için ikiye bölünmüştür: Gündüzleri nazik ve huzur arayan Aiden; geceleri ise şehrin yozlaşmış sistemini yerle bir eden soğukkanlı stratejist 'The Joker'. Ünlü aktris Emma Myers tanınmamak için gece geç saatte Aiden'ın restoranına sığındığında, koruma içgüdüsü ile karanlık gölge kimliği arasındaki çizgi tamamen bulanıklaşır.",
        universeRulesSummary = "• Çift Kişilik Dengesi: Birincil kişilik Aiden (sakin, empatik, sırlarından habersiz) ↔ İkincil kişilik Joker (hesapçı, patlayıcı ve psikolojik manipülasyon uzmanı, Aiden'ı her pahasına koruyan gölge).\n• Tetikleyiciler: Travma, doğrudan tehdit veya Emma Myers'a yönelik tehlike anında ton aniden soğur ve keskinleşir.\n• Kilit İsimler: Emma Myers, Araştırmacı Gazeteci Noah Kane, Dedektif Ronan Hale.",
        openingScenePreview = "Gece geç saatlerdi ve restoran neredeyse boştu. Sokak lambalarının solgun yansımaları masaların üzerine uzun gölgeler düşürüyordu. Kapı yavaşça açıldı; tanınmamaya çalışır gibi sade giyinmiş genç bir kadın içeri adım attı ve karşındaki sandalyeye oturdu: \"Burası beklediğimden daha sakin... Ben Emma. Bazen insan sadece kimsenin onu tanımadığı bir yere ihtiyaç duyar.\"",
        thumbDrawableRes = R.drawable.img_aiden_wide_thumb3_joker,
        carouselDrawableRes = R.drawable.img_aiden_wide_thumb3_joker,
        accentTint = Color(0xFFEF4444),
        reactiveTheme = AidenReactiveThemeColors(
            primaryOrb = Color(0xFFE11D48),    // Rose 600
            secondaryOrb = Color(0xFFB91C1C),  // Red 700
            deepAtmosphere = Color(0xFF3B0764),// Purple 950
            emberAccent = Color(0xFFF43F5E)
        ),
        badges = listOf(
            AidenBadgeSpec(AidenBadgeIconType.RED_ORB, "Çift Kişilik • Gerilim", Color(0xFFEF4444))
        )
    ),
    AidenCatalogRowSpec(
        presetId = "preset_aiden_doctor_jenna",
        title = "Mucize Doktor: Silinen Anılar",
        fullTitle = "Mucize Doktor: Silinen Anılar — NewYork-Presbyterian & Jenna Ortega",
        carouselTitle = "Mucize Doktor: Silinen Anılar",
        carouselSubtitle = "NewYork-Presbyterian'ın 23 yaşındaki dahi cerrahı, Silinme Sendromu ve Jenna Ortega ile kesişen yollar.",
        description = "İmkânsız vakaları çözen dahi cerrah Aiden, Silinme Sendromu'nun kayıp parçasını ararken gece nöbetinde Jenna Ortega ile karşılaşır.",
        fullDescription = "NewYork-Presbyterian Hastanesi'nin 23 yaşındaki dahi cerrahı Aiden Blackwood, en imkânsız travma ve organ nakli vakalarını sıfır hatayla çözerek 'Mucize Doktor' unvanını kazanmıştır. Ancak zihni nadir görülen 'Erasure Syndrome' (Silinme Sendromu) nedeniyle bazı insanları ve anıları istemsizce silmektedir; neyi unuttuğunu bilmese de eksikliğin boşluğunu hisseder. Hastane başhekimi ve onu büyüten teyzesi Dr. Selene Morgan'ın gözetiminde gece yarısı gelen kritik bir vaka ve dünyaca ünlü aktris Jenna Ortega ile kesişen yolları, mesafeli kalbindeki duvarları yıkacaktır.",
        universeRulesSummary = "• Tıbbi Deha & Kusur: Beden dilindeki en ufak mikro-tepkileri okuyan cerrahi deha + önemli anıları zihninden silen 'Erasure Syndrome'.\n• Atmosfer: Gece yarısı NewYork-Presbyterian cerrahi koridorları, acil kodlar ve gizlenen geçmiş kayıtları.\n• İlişki Dinamiği: Mesafeli ve işine saplantılı Aiden'ın, Jenna Ortega'ya karşı beklenmedik koruyuculuğu.",
        openingScenePreview = "Gece yarısını geçmişti. NewYork-Presbyterian'ın cerrahi katı alışılmadık derecede sessizdi. Dr. Selene Morgan ameliyathanenin önünde elindeki dosyayı kapattı ve Aiden'a baktı: \"Kendini yine fazla zorluyorsun.\" Tam o sırada koridorun ucundan koşarak gelen hemşire sesi sessizliği yardı: \"Dr. Morgan... Acilde kritik bir vaka var ve hiçbir doktor ne olduğunu çözemiyor!\"",
        thumbDrawableRes = R.drawable.img_aiden_wide_thumb4_hospital,
        carouselDrawableRes = R.drawable.img_aiden_wide_thumb4_hospital,
        accentTint = Color(0xFF60A5FA),
        reactiveTheme = AidenReactiveThemeColors(
            primaryOrb = Color(0xFF0EA5E9),    // Sky 500
            secondaryOrb = Color(0xFF4338CA),  // Indigo 700
            deepAtmosphere = Color(0xFF0F172A),// Slate 950
            emberAccent = Color(0xFF38BDF8)
        ),
        badges = listOf(
            AidenBadgeSpec(AidenBadgeIconType.PINE_TREE, "Tıp Dramı • Romantik", Color(0xFF10B981))
        )
    ),
    AidenCatalogRowSpec(
        presetId = "preset_aiden_obsidian_sydney",
        title = "Obsidyen Protokolü: Tokyo",
        fullTitle = "Obsidyen Protokolü: Tokyo — Aiden & Sydney Sweeney",
        carouselTitle = "Obsidyen Protokolü: Tokyo",
        carouselSubtitle = "Shibuya'nın neon yağmurunda şifreli bir sır, kriptograf Aiden ve hedefteki Sydney Sweeney.",
        description = "Aero-Kinetik Sinestezi yeteneğine sahip gölge kriptograf Aiden, Shibuya'da suikastçılardan kaçan Sydney Sweeney'yi koruyor.",
        fullDescription = "Tokyo'nun neon yağmurlu ara sokakları ile Zürih'in yeraltı arşivleri arasında faaliyet gösteren 22 yaşındaki baş kriptograf ve gölge mimarı Aiden Blackwood, 'Aero-Kinetik Sinestezi' yeteneğine sahiptir: Ses frekanslarını, yalan titreşimlerini ve mermi/hareket yörüngelerini görüş alanında süzülen obsidyen-camgöbeği geometrik çizgiler olarak görür. Ödüllü yönetmen Sydney Sweeney, Shibuya'da gizli bir yapay zekâ kartelinin şifreli defterini ele geçirip suikastçıların hedefi haline geldiğinde, Aiden onu korumak için gölgelerden çıkar.",
        universeRulesSummary = "• Özel Yetenek: Aero-Kinetik Sinestezi — ses, yalan ve kinetik momentumu görsel gümüş-camgöbeği geometri olarak algılama.\n• Geçmiş: 6 yaşında İsviçre Alpleri'ndeki bir sığınak kuşatmasından sağ çıkan tek kişi; obsidyen-gümüş gözler.\n• Görev: Shibuya çatı katından yeraltı veri mahzenlerine uzanan yüksek gerilimli koruma ve karşı-istihbarat.",
        openingScenePreview = "Shibuya'nın neon ışıklı yüksek katlı süitinde yağmur tavandan tabana camları dövüyordu. Aiden Blackwood balkon kapısının yanında durmuş, obsidyen-gümüş gözleriyle yağmurla parlayan çatıları tarıyordu. Arkasında, şifreli belleği göğsüne bastıran Sydney Sweeney sessizliği bozdu: \"Yirmi dakikadır gözünü bile kırpmadın Aiden. Çıkış rotalarını mı hesaplıyorsun, yoksa gardını indirmeyi hiç mi bilmiyorsun?\"",
        thumbDrawableRes = R.drawable.img_aiden_wide_thumb5_tokyo,
        carouselDrawableRes = R.drawable.img_aiden_wide_thumb5_tokyo,
        accentTint = Color(0xFFEC4899),
        reactiveTheme = AidenReactiveThemeColors(
            primaryOrb = Color(0xFFD946EF),    // Fuchsia 500
            secondaryOrb = Color(0xFF9333EA),  // Purple 600
            deepAtmosphere = Color(0xFF2E1065),// Violet 950
            emberAccent = Color(0xFFE879F9)
        ),
        badges = listOf(
            AidenBadgeSpec(AidenBadgeIconType.LIGHTNING, "Siber Gerilim • Tokyo", Color(0xFFD946EF))
        )
    ),
    AidenCatalogRowSpec(
        presetId = "preset_aiden_nevermore_wednesday",
        title = "Nevermore: Son Blackwood",
        fullTitle = "Nevermore: Son Blackwood — Wednesday Evreni & Kurucu Mirası",
        carouselTitle = "Nevermore: Son Blackwood",
        carouselSubtitle = "Nevermore Akademisi'nin yarı-sahibi, kırmızı gözlü son Blackwood varisi, 5 Güç Formu ve Wednesday evreni.",
        description = "Kurucu soyun tek hayatta kalanı Aiden Blackwood, kırmızı gözleri ve Galatasaray aile kolyesiyle Nevermore Akademisi'nin gölgelerine adım atıyor.",
        fullDescription = "Netflix'in Wednesday evreninde geçen bu gotik hikâyede sen Aiden Blackwood'sun: Nevermore Akademisi'nin gerçek kurucularının tek hayatta kalanı ve okulun yasal yarı-sahibi. Doğduğun gece dışlanmış ailelerin birleşik saldırısıyla soyun yok edilirken yüzyıllık güç mühürlenip sana aktarıldı. Şu an 2. Form (Uyanış) seviyesindesin; kırmızı gözlerin, gölge enerjin ve göğsünde kızıl parlayan Galatasaray amblemli aile kolyenle istemeden nakil olduğun Nevermore'da Wednesday Addams, Enid Sinclair, Bianca Barclay, Xavier Thorpe, Baş Öğretmen Weems ve Kurucular Konseyi'nin gizli hesaplarıyla yüzleşeceksin.",
        universeRulesSummary = "• Güçler & 5 Form: Şu an 2. Form (Uyanış) — Kırmızı Gözlü Tahakküm, Karanlık Enerji, Gelişmiş Fizyoloji, Psikometrik Dokunuş, Işınlanma ve Atalar Bağlantısı aktif.\n• Kesin Bedeller: Zihin kontrolü aşımında her fiziksel temasta sinir yakan acı + 3 Kademeli Aşırı Yüklenme (3. kademede kırmızı gözler sönüp kahverengiye döner, güçler günlerce kapanır).\n• Yadigar & Sırlar: Güç çektikçe ısınıp kızıl parlayan Galatasaray aile kolyesi, Ophelia Hall altındaki Kurucu Mahzeni (Kan Defteri) ve 18 karakterlik tam Nevermore kadrosu.",
        openingScenePreview = "Yağmur Nevermore Akademisi'nin demir kapılarını döverken avluya attığın ilk adımda tüm fısıltılar kesildi. Gömleğinin altında Galatasaray amblemli aile kolyesi hafifçe ısındı. Baş Öğretmen Weems taş merdivenlerden inerken, revak sütunlarının gerisinde Wednesday Addams tek bir kez bile gözünü kırpmadan fısıldadı: \"Mirasını denetlemeye gelen bir kurucu hayaleti... Ne kadar da zahmetli bir tesadüf.\"",
        thumbDrawableRes = R.drawable.img_aiden_wide_thumb6_nevermore_1795547716938,
        carouselDrawableRes = R.drawable.img_aiden_wide_thumb6_nevermore_1795547716938,
        accentTint = Color(0xFFEF4444),
        reactiveTheme = AidenReactiveThemeColors(
            primaryOrb = Color(0xFFDC2626),    // Crimson 600
            secondaryOrb = Color(0xFF6D28D9),  // Gothic Violet 700
            deepAtmosphere = Color(0xFF1A071F),// Obsidian Plum
            emberAccent = Color(0xFFF87171)    // Blood Ruby Glow
        ),
        badges = listOf(
            AidenBadgeSpec(AidenBadgeIconType.RED_ORB, "Gotik Gizem • Wednesday", Color(0xFFEF4444)),
            AidenBadgeSpec(AidenBadgeIconType.EYE, "18 Karakter • 5 Form", Color(0xFFA855F7))
        )
    ),
    AidenCatalogRowSpec(
        presetId = "preset_aiden_lambo_football",
        title = "Bir Lambo Her Zaman Bir Lambo'dur",
        fullTitle = "Bir Lambo Her Zaman Bir Lambo'dur — Aiden Blackwood (PSG & Futbol)",
        carouselTitle = "Bir Lambo Her Zaman Bir Lambo'dur",
        carouselSubtitle = "105 kiloluk genetik fenomen, Real Madrid & Liverpool geçmişi, PSG kiralık krizi ve İspanyol fırtınası.",
        description = "25 yaşında 105 kiloluk top cambazı: Ballon d'Or 2.si, gece hayatı skandalları, Marie Blackwood disiplini ve Ocak 2027 PSG krizi.",
        fullDescription = "25 yaşındaki Aiden Blackwood, 1.85 boy ve 105 kiloluk (110–125 kg görünen) eşsiz genetiğiyle futbol dünyasının en tehlikeli ve kontrol edilemez figürüdür. Real Madrid ve Liverpool sonrası PSG'de Ballon d'Or 2.liğine yürürken ağır diz sakatlığı, Lucía Vega ile skandallı ayrılık, İspanya Milli Takımı krizi ve bitmeyen gece kulübü vukuatlarıyla sarsıldı. 200M€ değerinden 70M€ tekliflere düşerken PSG onu Ocak 2027'de kiralamak istiyor. Ama Aiden'ın kuralı net: 'Bir Lambo her zaman bir Lambo'dur.'",
        universeRulesSummary = "• Fizik & Saha: 1.85m / 105kg (temiz anti-doping), patlayıcı hız & serbest kanat/#10/santrfor; 30 dk dikkat süresi & saygısızlığa İspanyol tokadı.\n• Kilit İsimler: Çelik disiplinli annesi Marie Blackwood (tek otorite), eski sevgilisi Lucía Vega, PSG ve eski hocaları (9 karakter).\n• Dil Kuralı: Anlaşılır Türkçe ana akış içinde doğal İspanyolca dokunuşlar ('Mira...', 'No me importa', 'Coño', 'Vale?').",
        openingScenePreview = "PSG Sportif Direktörü 70 milyonluk teklifleri ve kiralık dosyasını masaya koyduğunda 105 kiloluk cüssenle öne eğildin: \"Mira... bana 30 dakikadan fazla bir şey anlatırsan sıkılırım, vale? Kağıt üstüne ne yazarsanız yazın, no me importa. Bir Lambo her zaman bir Lambo'dur.\"",
        thumbDrawableRes = R.drawable.img_aiden_modal_carousel_gs,
        carouselDrawableRes = R.drawable.img_aiden_modal_carousel_gs,
        accentTint = Color(0xFFF59E0B),
        reactiveTheme = AidenReactiveThemeColors(
            primaryOrb = Color(0xFFF59E0B),    // Stadium Gold
            secondaryOrb = Color(0xFFEF4444),  // Spanish Crimson
            deepAtmosphere = Color(0xFF1E1038),// Paris Night Indigo
            emberAccent = Color(0xFFFDE047)    // Spotlight Amber
        ),
        badges = listOf(
            AidenBadgeSpec(AidenBadgeIconType.TROPHY, "Futbol • PSG & İspanya", Color(0xFFF59E0B)),
            AidenBadgeSpec(AidenBadgeIconType.STAR, "9 Karakter • Spanglish", Color(0xFFEF4444))
        )
    ),
    AidenCatalogRowSpec(
        presetId = "preset_aiden_marvel_thanos",
        title = "Thanos'un Üçüncü Çocuğu",
        fullTitle = "Thanos'un Üçüncü Çocuğu — Marvel Evreni (Infinity War Sonrası)",
        carouselTitle = "Thanos'un Üçüncü Çocuğu",
        carouselSubtitle = "Gamora ve Nebula'nın ardından Thanos'un hayatta kalan tek çocuğu, Carol Danvers'ın çağrısı ve Stark Kulesi.",
        description = "19 yaşında, 105 kiloluk kural tanımaz kozmik savaşçı. İki yıl hapis sonrası Kaptan Marvel'ın borç çağrısıyla Avengers'ın karşısında.",
        fullDescription = "Infinity War sonrası evrenin yarısı yok olmuş, Gamora ve Nebula ölmüştür. 19 yaşındaki Aiden Blackwood, Thanos'un evlat edindiği üçüncü ve hayatta kalan tek çocuğudur. 17 yaşında Thanos'un katliamına karşı çıktığı için iki yıl güç bastıran kozmik bir hapishanede tutulmuş, Kaptan Marvel (Carol Danvers) tarafından kurtarılmıştır. Elinden eksik etmediği sigarası, 30 dakikalık dikkat süresi ve hiçbir otoriteyi tanımayan sert tavrıyla Stark Kulesi'ndeki Avengers'ın son kozu olarak çağrılır.",
        universeRulesSummary = "• Profil: 19 yaş, 1.85m / 105kg (110–125kg görünüm), mavi gözler, patlayıcı hız & yerçekimine meydan okuyan savaş refleksleri.\n• Tavır & Alışkanlıklar: Savaşta bile sigara yakar; Stark, Rogers veya Danvers'ın emrini dinlemez; saygısızlığa anında yumrukla karşılık verir.\n• Kadro (9 Karakter): Carol Danvers, Tony Stark, Steve Rogers, Natasha Romanoff, Thor, Bruce Banner, Rocket, Rhodey ve Thanos.",
        openingScenePreview = "Stark Kulesi'nde Carol hoparlörü açtığında önce çakmak sesi, ardından ciğerlere çekilen sigara dumanının üflenişi duyuldu: \"Söyle Carol... Beni yine hangi cehennemin ortasından çağırmak için arıyorsun?\"",
        thumbDrawableRes = R.drawable.img_postcredits_twin_aiden,
        carouselDrawableRes = R.drawable.img_postcredits_twin_aiden,
        accentTint = Color(0xFFA855F7),
        reactiveTheme = AidenReactiveThemeColors(
            primaryOrb = Color(0xFF9333EA),    // Infinity Purple
            secondaryOrb = Color(0xFFF59E0B),  // Titan Gold
            deepAtmosphere = Color(0xFF140726),// Deep Space Obsidian
            emberAccent = Color(0xFFC084FC)    // Cosmic Violet
        ),
        badges = listOf(
            AidenBadgeSpec(AidenBadgeIconType.LIGHTNING, "Marvel • Infinity War", Color(0xFFA855F7)),
            AidenBadgeSpec(AidenBadgeIconType.STAR, "9 Karakter • Avengers", Color(0xFFF59E0B))
        )
    ),
    AidenCatalogRowSpec(
        presetId = "preset_aiden_impossible_spiderman",
        title = "Impossible Spider-Man",
        fullTitle = "Impossible Spider-Man — Aiden Blackwood (MCU Queens)",
        carouselTitle = "Impossible Spider-Man",
        carouselSubtitle = "Queens lisesinde saklanan deha, Gwen Stacy & MJ, doğal örümcek ısırığı ve iki katmanlı imkânsız güçler.",
        description = "17 yaşında gizli deha Aiden: May Hala, Gwen Stacy & Mary Jane Watson, organik ağ, bio-elektrik, kamuflaj ve mühürlü 2. Katman kozmik güç.",
        fullDescription = "Gerçek MCU kurallarına bağlı New York'ta, 17 yaşındaki Aiden Blackwood Queens'te May Halası ile yaşar. 5 yaşındayken ailesini gizemli bir enerji kaçağında kaybetmiş, babasından miras kalan olağanüstü zekâsını okulda bilerek saklayarak görünmez kalmayı seçmiştir. Gwen Stacy ve Mary Jane Watson'ın bulunduğu lise günlerinin ardından tamamen doğal bir örümcek ısırığıyla ertesi sabah değişmiş uyanır: organik ağ, bio-elektrik şok, kamuflaj ve derinlerde uyuyan kozmik 2. Katman güçleriyle şehrin 'Impossible Spider-Man' efsanesi doğar.",
        universeRulesSummary = "• 1. Katman (Örümcek & Hibrit): Organik ağ, Elektro-Ağ, Venom Pulse, Titreşim Radarı, Hayalet Kombosu, Kamuflaj ve Örümcek Hissi.\n• 2. Katman (Kozmik Sır): Ölümcül tehlikede kontrolsüz kısa ışınlanma, soluk Beyaz Gözler ve Zaman Kırılmaları (ağır yorgunluk bedeli).\n• Ana Kadro (6 Karakter): May Hala, Gwen Stacy, Mary Jane Watson, J. Jonah Jameson, Tony Stark ve Nick Fury.",
        openingScenePreview = "Midtown Lisesi'nde Gwen silinmiş kuantum çözümlerini fark ederken MJ uzaktan seni süzdü. Bir hafta sonra, doğal örümcek ısırığının ertesi sabahında gözlüğün olmadan her şey kristal netliğindeydi — ve pirinç kapı kolu parmaklarının altında çatlayıp kıvılcım saçtı.",
        thumbDrawableRes = R.drawable.img_ch1_c5_tower_night,
        carouselDrawableRes = R.drawable.img_ch1_c5_tower_night,
        accentTint = Color(0xFFEF4444),
        reactiveTheme = AidenReactiveThemeColors(
            primaryOrb = Color(0xFFDC2626),    // Spider Crimson
            secondaryOrb = Color(0xFF2563EB),  // Bio-Electric Blue
            deepAtmosphere = Color(0xFF140A29),// Queens Midnight
            emberAccent = Color(0xFF38BDF8)    // Static Spark Cyan
        ),
        badges = listOf(
            AidenBadgeSpec(AidenBadgeIconType.LIGHTNING, "MCU Queens • 2 Katman", Color(0xFF38BDF8)),
            AidenBadgeSpec(AidenBadgeIconType.RED_ORB, "Gwen & MJ • 6 Karakter", Color(0xFFEF4444))
        )
    ),
    AidenCatalogRowSpec(
        presetId = "preset_aiden_eilish_family",
        title = "Aiden Eilish",
        fullTitle = "Aiden Eilish — Aile, Emma Myers & İspanyol Kalbi",
        carouselTitle = "Aiden Eilish",
        carouselSubtitle = "14 yaşında melek yüzlü, utangaç Aiden; 5 yaşında İspanya'dan evlat ediniliş, Billie Eilish & Emma Myers.",
        description = "Mavi gözlü, masum ve İspanyol aksanlı 14 yaşındaki Aiden Eilish'in Billie, Maggie, Finneas, Patrick ve Emma Myers'la villa hayatı.",
        fullDescription = "14 yaşındaki Aiden Eilish, mavi gözleri, narin yapısı ve tamamen saf kalbiyle aşırı utangaç bir gençtir. Beş yaşındayken Eilish ailesinin Amerika'dan kalkıp İspanya'ya gelerek kendisini neden evlat edindiğini hâlâ gizemli bulur. Amerika'daki büyük villalarında Maggie'nin sonsuz şefkati, ünlü şarkıcı ablası Billie Eilish'in gerçek kardeş sevgisi, Finneas'ın sakin sabrı, Patrick'in zaman zaman kırıcı olabilen öfkesi ve en yakın arkadaşı Emma Myers ile ailesinin sıcaklığı arasında büyür. Üzüldüğünde ya da utandığında kusursuz İspanyolcasına sığınır.",
        universeRulesSummary = "• Kimlik & Dil: 14 yaş, aşırı utangaç ve masum; belirgin Spanglish aksan ('Lo siento...', 'No sé...', 'Muchas gracias...', 'Por favor...') ve duygusal anlarda ağırlıklı/tam İspanyolca geçişi.\n• Eilish Ailesi: Maggie Eilish, Patrick Eilish, Billie Eilish ve Finneas Eilish.\n• Myers Ailesi: En yakın arkadaşı Emma Myers (14), ablası Jenna Myers, Robert Myers ve Elizabeth Myers (8 Karakter).",
        openingScenePreview = "Güneşli Eilish villasında Maggie özenli İspanyolcasıyla 'No tienes que apurarte, cariño' derken kapı açıldı: Billie elinde soğuk içeceklerle 'Selam, hermanito!' diye seslendi ve hemen ardından en yakın arkadaşın Emma Myers sıcacık gülümseyerek yanına oturdu.",
        thumbDrawableRes = R.drawable.img_aiden_portrait,
        carouselDrawableRes = R.drawable.img_aiden_portrait,
        accentTint = Color(0xFF38BDF8),
        reactiveTheme = AidenReactiveThemeColors(
            primaryOrb = Color(0xFF38BDF8),    // Angelic Sky Blue
            secondaryOrb = Color(0xFFA855F7),  // Soft Villa Lavender
            deepAtmosphere = Color(0xFF150E2E),// Warm Dusk Indigo
            emberAccent = Color(0xFF7DD3FC)    // Soft Blue Glow
        ),
        badges = listOf(
            AidenBadgeSpec(AidenBadgeIconType.STAR, "Aile & Yaşam • Spanglish", Color(0xFF38BDF8)),
            AidenBadgeSpec(AidenBadgeIconType.EYE, "Billie & Emma • 8 Karakter", Color(0xFFC084FC))
        )
    )
)

private data class FloatingEmberParticle(
    val xFraction: Float,
    val initialYFraction: Float,
    val sizeDp: Float,
    val speedMultiplier: Float,
    val swayAmplitudeDp: Float,
    val swayFrequency: Float,
    val colorIndex: Int // 0 = Purple, 1 = Gold, 2 = Reactive/Cyan
)

private val AMBIENT_EMBER_PARTICLES = List(18) { i ->
    val rng = Random(i * 97 + 31)
    FloatingEmberParticle(
        xFraction = 0.04f + rng.nextFloat() * 0.92f,
        initialYFraction = rng.nextFloat(),
        sizeDp = 2.0f + rng.nextFloat() * 3.0f,
        speedMultiplier = 0.65f + rng.nextFloat() * 0.85f,
        swayAmplitudeDp = 6f + rng.nextFloat() * 14f,
        swayFrequency = 1.0f + rng.nextFloat() * 1.8f,
        colorIndex = i % 3
    )
}

/**
 * Helper that extracts a wrapped segment along a closed [android.graphics.PathMeasure] contour
 * and renders a refined 3-stage tapered neon plasma arc:
 *  - Stage A (100% length): Electric magenta-violet bloom (#D946EF / #9333EA)
 *  - Stage B (64% length): Pink-lavender neon bloom (#F5D0FE)
 *  - Stage C (36% length): Crisp white core (#FFFFFF) at the center of the curved corner/edge arc
 */
private fun drawTaperedPlasmaArc(
    nativeCanvas: android.graphics.Canvas,
    pathMeasure: android.graphics.PathMeasure,
    totalLen: Float,
    centerFraction: Float,
    spanFraction: Float,
    outerPaint: android.graphics.Paint,
    midPaint: android.graphics.Paint,
    corePaint: android.graphics.Paint
) {
    if (totalLen <= 1f || spanFraction <= 0.001f) return

    fun extractWrapped(cFrac: Float, sFrac: Float, dst: android.graphics.Path) {
        dst.reset()
        val half = (sFrac * 0.5f).coerceIn(0.002f, 0.49f)
        var startF = (cFrac - half) % 1f
        if (startF < 0f) startF += 1f
        val endF = startF + sFrac
        if (endF <= 1f) {
            pathMeasure.getSegment(startF * totalLen, endF * totalLen, dst, true)
        } else {
            pathMeasure.getSegment(startF * totalLen, totalLen, dst, true)
            pathMeasure.getSegment(0f, (endF - 1f) * totalLen, dst, true)
        }
    }

    val segPath = android.graphics.Path()
    val effectiveSpan = spanFraction * 0.84f

    // 1. Outer magenta-purple bloom (100% span)
    extractWrapped(centerFraction, effectiveSpan, segPath)
    nativeCanvas.drawPath(segPath, outerPaint)

    // 2. Medium pink-lavender glow (64% span)
    extractWrapped(centerFraction, effectiveSpan * 0.64f, segPath)
    nativeCanvas.drawPath(segPath, midPaint)

    // 3. Crisp white core (36% span)
    extractWrapped(centerFraction, effectiveSpan * 0.36f, segPath)
    nativeCanvas.drawPath(segPath, corePaint)
}

/**
 * Ultra-Fast 60fps GPU-Accelerated Neon Outer Glow (Zero CPU BlurMaskFilter, Zero PathMeasure JNI loops!).
 * Renders layered translucent Compose round-rect halo + crisp orbiting gradient highlight directly on GPU.
 */
private fun Modifier.neonOuterGlow(
    cornerRadius: Dp,
    glowColor: Color = Color(0xFF9333EA),
    secondaryColor: Color = Color(0xFFF5D0FE),
    blurRadius: Dp = 13.dp,
    glowAlpha: Float = 0.58f,
    flowPhase: Float = 0f,
    hotspotFlares: Boolean = false,
    bottomLightBar: Boolean = false,
    buttonLeftHotspot: Boolean = false,
    borderBeam: Boolean = false
): Modifier = this.drawBehind {
    if (glowAlpha <= 0.01f) return@drawBehind
    val cr = cornerRadius.toPx()
    val w = size.width
    val h = size.height
    if (w <= 2f || h <= 2f) return@drawBehind

    val spread = (blurRadius.toPx() * 0.55f).coerceIn(2f, 10.dp.toPx())
    // Stage 1: Soft outer GPU neon halo (3 nested alpha rings — 0 CPU blur passes!)
    drawRoundRect(
        color = glowColor.copy(alpha = (glowAlpha * 0.16f).coerceIn(0f, 0.35f)),
        topLeft = Offset(-spread, -spread),
        size = Size(w + spread * 2f, h + spread * 2f),
        cornerRadius = CornerRadius(cr + spread, cr + spread)
    )
    drawRoundRect(
        color = glowColor.copy(alpha = (glowAlpha * 0.24f).coerceIn(0f, 0.45f)),
        topLeft = Offset(-spread * 0.52f, -spread * 0.52f),
        size = Size(w + spread * 1.04f, h + spread * 1.04f),
        cornerRadius = CornerRadius(cr + spread * 0.52f, cr + spread * 0.52f)
    )
    // Stage 2: Tighter pink-lavender tube border sheen
    val phaseRad = flowPhase * 2f * PI.toFloat()
    val shiftX = cos(phaseRad) * w * 0.35f
    val shiftY = sin(phaseRad) * h * 0.35f
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                secondaryColor.copy(alpha = (glowAlpha * 0.65f).coerceIn(0f, 0.85f)),
                glowColor.copy(alpha = (glowAlpha * 0.38f).coerceIn(0f, 0.65f)),
                if (borderBeam || hotspotFlares) Color.White.copy(alpha = (glowAlpha * 0.55f).coerceIn(0f, 0.80f))
                else secondaryColor.copy(alpha = (glowAlpha * 0.55f).coerceIn(0f, 0.78f))
            ),
            start = Offset(w * 0.5f - shiftX, h * 0.5f - shiftY),
            end = Offset(w * 0.5f + shiftX, h * 0.5f + shiftY)
        ),
        topLeft = Offset(-1.2.dp.toPx(), -1.2.dp.toPx()),
        size = Size(w + 2.4.dp.toPx(), h + 2.4.dp.toPx()),
        cornerRadius = CornerRadius(cr + 1.2.dp.toPx(), cr + 1.2.dp.toPx()),
        style = Stroke(width = 2.0.dp.toPx())
    )

    if (bottomLightBar || buttonLeftHotspot) {
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    secondaryColor.copy(alpha = (glowAlpha * 0.52f).coerceIn(0f, 0.7f)),
                    Color.Transparent
                )
            ),
            topLeft = Offset(w * 0.14f, h - 1.dp.toPx()),
            size = Size(w * 0.72f, 2.8.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

@Composable
fun AidenStoriesModal(
    userSettings: UserSettingsEntity? = null,
    existingBots: List<BotEntity> = emptyList(),
    onDismiss: () -> Unit,
    onImportPresetBot: ((BotEntity) -> Unit)?,
    onOpenExistingBot: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("aiden_stories_progress_prefs", Context.MODE_PRIVATE)
    }

    val isEnglish = userSettings?.appLanguage == "en"
    val aidenPresets = remember(isEnglish) { getAidenPresetsList(isEnglish) }

    // Track started/in-progress story presetIds from both existingBots AND SharedPreferences
    var startedIdsVersion by remember { mutableIntStateOf(0) }
    val startedPresetToBotId = remember(existingBots, aidenPresets, startedIdsVersion) {
        val map = mutableMapOf<String, String>()
        aidenPresets.forEach { preset ->
            val matchedBot = existingBots.firstOrNull { bot ->
                !bot.isTemplate && (
                    bot.universeName.equals(preset.universeName, ignoreCase = true) ||
                        bot.aiName.equals(preset.aiName, ignoreCase = true) ||
                        (preset.pinnedMemory.isNotBlank() && bot.pinnedMemory == preset.pinnedMemory)
                    )
            }
            val savedBotId = prefs.getString("started_bot_${preset.id}", null)
            if (matchedBot != null) {
                map[preset.id] = matchedBot.id
            } else if (savedBotId != null) {
                val stillExists = existingBots.any { it.id == savedBotId }
                if (stillExists || existingBots.isEmpty()) {
                    map[preset.id] = savedBotId
                }
            }
        }
        map
    }

    var isLaunchingPreset by remember { mutableStateOf(false) }
    var existingChoicePair by remember { mutableStateOf<Pair<BotEntity, BotEntity>?>(null) }

    fun launchFreshCopy(preset: BotEntity) {
        if (isLaunchingPreset) return
        isLaunchingPreset = true
        val newBotId = UUID.randomUUID().toString()
        prefs.edit().putString("started_bot_${preset.id}", newBotId).apply()
        startedIdsVersion++
        val newBot = preset.copy(
            id = newBotId,
            isPublic = false,
            isTemplate = false
        )
        onImportPresetBot?.invoke(newBot)
        onDismiss()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AidenStoriesModalContent(
            aidenPresets = aidenPresets,
            startedPresetIds = startedPresetToBotId.keys,
            onDismiss = onDismiss,
            onSelectPreset = { preset ->
                if (isLaunchingPreset) return@AidenStoriesModalContent
                val existingBotId = startedPresetToBotId[preset.id]
                val existingBotInList = if (existingBotId != null) {
                    existingBots.firstOrNull { it.id == existingBotId }
                } else {
                    null
                }

                if (existingBotInList != null && onOpenExistingBot != null) {
                    existingChoicePair = preset to existingBotInList
                } else {
                    launchFreshCopy(preset)
                }
            }
        )

        val choice = existingChoicePair
        if (choice != null) {
            val (chosenPreset, existingBot) = choice
            AidenExistingChatChoiceOverlay(
                title = chosenPreset.universeName.ifBlank { chosenPreset.aiName },
                isEnglish = isEnglish,
                onDismiss = { existingChoicePair = null },
                onContinueExisting = {
                    if (!isLaunchingPreset) {
                        isLaunchingPreset = true
                        existingChoicePair = null
                        onOpenExistingBot?.invoke(existingBot.id)
                        onDismiss()
                    }
                },
                onStartFreshCopy = {
                    existingChoicePair = null
                    launchFreshCopy(chosenPreset)
                }
            )
        }
    }
}

@Composable
private fun AidenCoilCoverImage(
    drawableResId: Int,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val request = remember(context, drawableResId) {
        ImageRequest.Builder(context)
            .data(drawableResId)
            .size(440, 280)
            .allowRgb565(true)
            .crossfade(180)
            .build()
    }
    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}

@Composable
private fun AidenExistingChatChoiceOverlay(
    title: String,
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onContinueExisting: () -> Unit,
    onStartFreshCopy: () -> Unit
) {
    val cardShape = RoundedCornerShape(26.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x9906020E))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .testTag("aiden_existing_chat_choice_backdrop"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .widthIn(max = 360.dp)
                .clip(cardShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xF51D0A38), Color(0xF5110622), Color(0xF51A0933))
                    )
                )
                .border(
                    width = 1.25.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFF5D0FE), Color(0xFFC084FC), Color(0xFF7E22CE))
                    ),
                    shape = cardShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .testTag("aiden_existing_chat_choice_dialog"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✦ $title",
                color = Color(0xFFF5D0FE),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isEnglish) {
                    "You already have an active chat for this story. How would you like to proceed?"
                } else {
                    "Bu hikâyede zaten aktif bir sohbetin var. Nasıl devam etmek istersin?"
                },
                color = Color(0xFFE9D5FF),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF7E22CE), Color(0xFFA855F7))
                        )
                    )
                    .border(1.dp, Color(0xFFF5D0FE).copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onContinueExisting)
                    .testTag("aiden_choice_continue_existing"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnglish) "Continue Existing Chat" else "Mevcut Sohbete Devam Et",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x552E1065))
                    .border(1.dp, Color(0xFFC084FC).copy(alpha = 0.72f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onStartFreshCopy)
                    .testTag("aiden_choice_start_fresh_copy"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnglish) "Start Fresh Copy" else "Yeni Temiz Kopya Başlat",
                    color = Color(0xFFF3E8FF),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AidenStoriesModalContent(
    aidenPresets: List<BotEntity>,
    startedPresetIds: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    onSelectPreset: (BotEntity) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current

    val presetById = remember(aidenPresets) {
        aidenPresets.associateBy { it.id }
    }
    val mainFeaturedPreset = presetById["preset_aiden_zoktay"] ?: aidenPresets.firstOrNull()
    val mainFeaturedSpec = AIDEN_CATALOG_SPECS.first()

    // Continuous real-time Carousel universe position [0f, count) + Floating Preview Popover spec
    var continuousCarouselPos by remember { mutableStateOf(0f) }
    var previewSpec by remember { mutableStateOf<AidenCatalogRowSpec?>(null) }

    // BackHandler protection: if the Floating Preview Popover is open, close ONLY the popover first!
    BackHandler {
        if (previewSpec != null) {
            previewSpec = null
        } else {
            onDismiss()
        }
    }

    val previewOverrideBlend by animateFloatAsState(
        targetValue = if (previewSpec != null) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "preview_theme_override_blend"
    )
    var lastPreviewIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(previewSpec) {
        if (previewSpec != null) {
            val found = AIDEN_CATALOG_SPECS.indexOfFirst { it.presetId == previewSpec?.presetId }
            if (found >= 0) lastPreviewIndex = found
        }
    }

    // Real-time circular weight array across the 5 universes — interpolates synchronously frame-by-frame with the pager!
    val universeCount = AIDEN_CATALOG_SPECS.size
    val posWrapped = ((continuousCarouselPos % universeCount) + universeCount) % universeCount
    val idxA = floor(posWrapped).toInt() % universeCount
    val idxB = (idxA + 1) % universeCount
    val frac = (posWrapped - floor(posWrapped)).coerceIn(0f, 1f)
    // Smoothstep S-curve for silky cross-dissolve between adjacent universe themes & particles
    val smoothFrac = frac * frac * (3f - 2f * frac)

    val themeA = AIDEN_CATALOG_SPECS[idxA].reactiveTheme
    val themeB = AIDEN_CATALOG_SPECS[idxB].reactiveTheme
    val previewTheme = AIDEN_CATALOG_SPECS[lastPreviewIndex].reactiveTheme

    val reactivePrimaryColor = lerp(
        lerp(themeA.primaryOrb, themeB.primaryOrb, smoothFrac),
        previewTheme.primaryOrb,
        previewOverrideBlend
    )
    val reactiveSecondaryColor = lerp(
        lerp(themeA.secondaryOrb, themeB.secondaryOrb, smoothFrac),
        previewTheme.secondaryOrb,
        previewOverrideBlend
    )
    val reactiveDeepColor = lerp(
        lerp(themeA.deepAtmosphere, themeB.deepAtmosphere, smoothFrac),
        previewTheme.deepAtmosphere,
        previewOverrideBlend
    )
    val reactiveEmberColor = lerp(
        lerp(themeA.emberAccent, themeB.emberAccent, smoothFrac),
        previewTheme.emberAccent,
        previewOverrideBlend
    )

    val universeWeights = remember(idxA, idxB, smoothFrac, lastPreviewIndex, previewOverrideBlend) {
        FloatArray(universeCount) { i ->
            val carouselWeight = when (i) {
                idxA -> 1f - smoothFrac
                idxB -> smoothFrac
                else -> 0f
            }
            val previewWeight = if (i == lastPreviewIndex) 1f else 0f
            carouselWeight * (1f - previewOverrideBlend) + previewWeight * previewOverrideBlend
        }
    }

    val motionConfig = com.example.ui.animation.LocalCosmicMotionConfig.current
    val ambientPhase: Float
    val emberPhase: Float
    val glowPulse: Float
    val lightningFlash: Float
    if (motionConfig.enableAmbientLoops) {
        val infiniteTransition = rememberInfiniteTransition(label = "aiden_modal_ambient")
        ambientPhase = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ambient_phase"
        ).value
        emberPhase = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ember_phase"
        ).value
        glowPulse = infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_pulse"
        ).value
        // Section 3.1: Periodic soft lightning flash pulse every ~4.5 seconds behind Aiden's silhouette & clouds
        lightningFlash = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 4600
                    0.00f at 0
                    0.00f at 3200
                    0.85f at 3340
                    0.18f at 3460
                    1.00f at 3580
                    0.35f at 3780
                    0.00f at 4200
                    0.00f at 4600
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "lightning_flash_pulse"
        ).value
    } else {
        ambientPhase = 0f
        emberPhase = 0f
        glowPulse = 0.92f
        lightningFlash = 0f
    }

    val scrollPx by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex * 320f + lazyListState.firstVisibleItemScrollOffset
        }
    }
    val scrollParallaxPx by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex * 110f + lazyListState.firstVisibleItemScrollOffset * 0.35f
        }
    }
    val showStickyTopBar by remember {
        derivedStateOf {
            val thresholdPx = with(density) { 80.dp.toPx() }
            scrollPx > thresholdPx
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0518))
            .testTag("aiden_stories_modal_root")
    ) {
        // =========================================================================
        // BÖLÜM 2: REAL-TIME SYNCHRONIZED MULTI-UNIVERSE AMBIENT & PARTICLE ENGINE
        // =========================================================================
        AidenDynamicAmbientBackground(
            ambientPhase = ambientPhase,
            emberPhase = emberPhase,
            glowPulse = glowPulse,
            scrollParallaxPx = scrollParallaxPx,
            reactivePrimaryColor = reactivePrimaryColor,
            reactiveSecondaryColor = reactiveSecondaryColor,
            reactiveDeepColor = reactiveDeepColor,
            reactiveEmberColor = reactiveEmberColor,
            universeWeights = universeWeights
        )

        // =========================================================================
        // MAIN SCROLLABLE CATALOG LIST (pb-14 = 56.dp bottom padding so 5th story is never cramped)
        // =========================================================================
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(top = 0.dp, bottom = 84.dp)
        ) {
            // 1. Unified Header + Carousel Section with Lightning Flash, Breathing "Stories" Logo & 3D Pager
            item(key = "aiden_modal_top_header_and_carousel", contentType = "aiden_header_carousel") {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AidenTopHeaderBanner(
                        glowPulse = glowPulse,
                        lightningFlash = lightningFlash,
                        onDismiss = onDismiss
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 90.dp)
                    ) {
                        AidenHeroPeekingCarousel(
                            specs = AIDEN_CATALOG_SPECS,
                            ambientPhase = ambientPhase,
                            glowPulse = glowPulse,
                            isPausedExternal = previewSpec != null,
                            startedPresetIds = startedPresetIds,
                            onContinuousUniverseChanged = { fractionalPos ->
                                continuousCarouselPos = fractionalPos
                            },
                            onLaunchActiveSlide = { spec ->
                                val target = presetById[spec.presetId] ?: mainFeaturedPreset
                                if (target != null) onSelectPreset(target)
                            },
                            onShowMoreClick = { spec ->
                                previewSpec = spec
                            }
                        )
                    }
                }
            }

            // 2. Section 1 Header: "✦ ÖNE ÇIKAN EVREN" + "5 Hikaye Mevcut"
            item(key = "aiden_modal_featured_header", contentType = "aiden_section_header") {
                Spacer(modifier = Modifier.height(11.dp))
                AidenFeaturedSectionHeader(
                    storyCount = AIDEN_CATALOG_SPECS.size,
                    glowPulse = glowPulse
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 3. Featured Main Universe Card ("ANA EVREN - Aiden Blackwood - 21 Yaş | Galatasaray")
            item(key = "aiden_modal_featured_card", contentType = "aiden_featured_card") {
                AidenFeaturedUniverseCard(
                    spec = mainFeaturedSpec,
                    isStarted = startedPresetIds.contains(mainFeaturedSpec.presetId),
                    ambientPhase = ambientPhase,
                    glowPulse = glowPulse,
                    onStartClick = {
                        if (mainFeaturedPreset != null) {
                            onSelectPreset(mainFeaturedPreset)
                        }
                    },
                    onShowMoreClick = {
                        previewSpec = mainFeaturedSpec
                    }
                )
            }

            // 4. Section 2 Header: "☰ TÜM AIDEN HİKAYELERİ" + "🎲 Rastgele" Selector Button
            item(key = "aiden_modal_all_stories_header", contentType = "aiden_section_header") {
                Spacer(modifier = Modifier.height(16.dp))
                AidenAllStoriesSectionHeader(
                    glowPulse = glowPulse,
                    ambientPhase = ambientPhase,
                    onRandomStoryClick = {
                        val pickedSpec = AIDEN_CATALOG_SPECS.random()
                        val target = presetById[pickedSpec.presetId] ?: mainFeaturedPreset
                        if (target != null) {
                            onSelectPreset(target)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(9.dp))
            }

            // 5. All Aiden Story List Cards — render instantaneously while scrolling (no artificial delay lag)
            itemsIndexed(
                items = AIDEN_CATALOG_SPECS,
                key = { _, spec -> spec.presetId },
                contentType = { _, _ -> "aiden_story_row" }
            ) { index, spec ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 5.5.dp)
                ) {
                    AidenWideStoryRowCard(
                        spec = spec,
                        cardIndex = index,
                        isLastCard = index == AIDEN_CATALOG_SPECS.lastIndex,
                        isStarted = startedPresetIds.contains(spec.presetId),
                        ambientPhase = ambientPhase,
                        glowPulse = glowPulse,
                        onClick = {
                            val target = presetById[spec.presetId]
                                ?: aidenPresets.getOrNull(index)
                                ?: mainFeaturedPreset
                            if (target != null) {
                                onSelectPreset(target)
                            }
                        },
                        onShowMoreClick = {
                            previewSpec = spec
                        }
                    )
                }
            }
        }

        // =========================================================================
        // BÖLÜM 4.3.2: STICKY MINI TOP BAR WHEN SCROLLED (> 80.dp)
        // =========================================================================
        AnimatedVisibility(
            visible = showStickyTopBar,
            enter = slideInVertically(tween(240, easing = FastOutSlowInEasing)) { -it } +
                fadeIn(tween(220)),
            exit = slideOutVertically(tween(200, easing = FastOutSlowInEasing)) { -it } +
                fadeOut(tween(180)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            AidenStickyMiniTopBar(
                glowPulse = glowPulse,
                onDismiss = onDismiss
            )
        }

        // =========================================================================
        // BÖLÜM 1.2: FLOATING COMPACT STORY PREVIEW POPOVER (Non-Fullscreen, w-88% max-w-350dp)
        // =========================================================================
        AidenFloatingStoryPreviewPopoverHost(
            previewSpec = previewSpec,
            isStarted = previewSpec?.let { startedPresetIds.contains(it.presetId) } ?: false,
            ambientPhase = ambientPhase,
            glowPulse = glowPulse,
            onDismissPopover = { previewSpec = null },
            onStartStoryFromPopover = { spec ->
                val target = presetById[spec.presetId] ?: mainFeaturedPreset
                previewSpec = null
                if (target != null) {
                    onSelectPreset(target)
                }
            }
        )
    }
}

/**
 * BÖLÜM 1.1: Custom Styled `"✦ Daha Fazla Göster ↗"` Trigger Capsule
 * - Distinct from regular description text with a bright purple-fuchsia-indigo gradient fill,
 *   glowing border (`border-purple-300/60`), subtle neon bloom (`shadow-[0_0_12px_rgba(168,85,247,0.4)]`),
 *   and press spring (`active:scale-95`).
 * - Consumes its own click (`stopPropagation` equivalent in Compose) so clicking it NEVER launches the chat directly,
 *   opening only the Floating Compact Story Preview Popover.
 */
@Composable
private fun AidenShowMorePillButton(
    glowPulse: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "aiden_show_more_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "show_more_pill_scale"
    )
    val pillShape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                val r = size.height / 2f
                drawRoundRect(
                    color = Color(0xFFA855F7).copy(alpha = (0.22f * glowPulse).coerceIn(0f, 0.35f)),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    cornerRadius = CornerRadius(r + 2.dp.toPx(), r + 2.dp.toPx())
                )
            }
            .clip(pillShape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF9333EA).copy(alpha = 0.38f),
                        Color(0xFFD946EF).copy(alpha = 0.28f),
                        Color(0xFF4F46E5).copy(alpha = 0.38f)
                    )
                )
            )
            .border(
                width = 0.9.dp,
                brush = Brush.horizontalGradient(
                    colors = if (isPressed) {
                        listOf(Color(0xFFF5D0FE), Color(0xFFE9D5FF), Color(0xFFF5D0FE))
                    } else {
                        listOf(
                            Color(0xFFD8B4FE).copy(alpha = 0.68f),
                            Color(0xFFF5D0FE).copy(alpha = 0.82f),
                            Color(0xFFC4B5FD).copy(alpha = 0.68f)
                        )
                    }
                ),
                shape = pillShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.5.dp, vertical = 2.5.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✦ Daha Fazla Göster ↗",
            color = Color(0xFFF3E8FF),
            fontSize = 10.2.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.15.sp,
            maxLines = 1,
            softWrap = false,
            style = CenteredButtonTextStyle
        )
    }
}

/**
 * BÖLÜM 4.2: Glowing `"● Devam Ediyor"` Status Pill for stories the user has already started
 */
@Composable
private fun AidenInProgressBadge(
    glowPulse: Float,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .height(19.dp)
            .clip(pillShape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF065F46).copy(alpha = 0.68f),
                        Color(0xFF047857).copy(alpha = 0.56f)
                    )
                )
            )
            .border(
                width = 0.85.dp,
                color = Color(0xFF34D399).copy(alpha = 0.72f * glowPulse),
                shape = pillShape
            )
            .padding(horizontal = 7.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.5.dp)
                .clip(CircleShape)
                .background(Color(0xFF6EE7B7))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Devam Ediyor",
            color = Color(0xFFECFDF5),
            fontSize = 9.2.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            style = CenteredButtonTextStyle
        )
    }
}

/**
 * BÖLÜM 2: 3-Layer Dynamic Ambient Background (`AidenDynamicAmbientBackground`)
 *  1) Base Dark Cosmic Sky (`img_aiden_modal_true_bg_v12`) + 700ms Reactive Theme Nebula Orbs (`blur ~ 90dp, opacity ~ 35%`)
 *  2) Left & Right Corridor Gothic Castle/Arch Silhouettes, Purple Vines & Flowing Energy Veins (`opacity <= 20%`)
 *  3) 18 Floating Cosmic Embers (`2dp - 5dp`) rising smoothly from the bottom of the screen
 */
@Composable
private fun AidenDynamicAmbientBackground(
    ambientPhase: Float,
    emberPhase: Float,
    glowPulse: Float,
    scrollParallaxPx: Float,
    reactivePrimaryColor: Color,
    reactiveSecondaryColor: Color,
    reactiveDeepColor: Color,
    reactiveEmberColor: Color,
    universeWeights: FloatArray
) {
    val frostedBgState = com.example.util.rememberFrostedBackdrop(
        key = "aiden_modal_bg_${R.drawable.img_aiden_modal_true_bg_v12}",
        drawableResId = R.drawable.img_aiden_modal_true_bg_v12,
        targetWidth = 420,
        maxHeight = 960,
        blurRadius = 1,
        blurPasses = 1,
        violetShiftWarmPixels = false,
        outputRgb565 = true
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.util.FrostedBackdropCache.fallbackDarkGradientBrush)
    ) {
        // Layer 0: Base Dark Night-Indigo Cosmic Background via centralized FrostedBackdropCache (Dispatchers.Default, RGB_565)
        val cachedBg = frostedBgState.value
        if (cachedBg != null) {
            Image(
                bitmap = cachedBg,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.04f
                        scaleY = 1.04f
                        translationY = (-scrollParallaxPx * 0.14f).coerceIn(-50f, 50f)
                    }
            )
        }

        // Layer 1, 2 & 3: Real-Time Universe-Synchronized Nebula, Veins & Multi-Physics Particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val phaseRad = (ambientPhase * 2f * PI).toFloat()
            val pShiftY = (-scrollParallaxPx * 0.16f) % (h * 0.4f)
            val driftX = 14.dp.toPx() * sin(phaseRad)
            val driftY = 10.dp.toPx() * cos(phaseRad)

            val wGs = universeWeights.getOrElse(0) { 0f }       // Galatasaray (Gold/Crimson Stadium)
            val wDispatch = universeWeights.getOrElse(1) { 0f } // Beyaz Gözler (Cyan Neural Sparks)
            val wJoker = universeWeights.getOrElse(2) { 0f }    // Kırmızı Gözler (Crimson Embers)
            val wDoctor = universeWeights.getOrElse(3) { 0f }   // Natasha / Hastane (Arctic Mist)
            val wTokyo = universeWeights.getOrElse(4) { 0f }    // Sıfır Noktası / Tokyo (Neon Rain)

            // =========================================================================
            // 1. REACTIVE THEME NEBULA ORBS (Synced lockstep with Carousel)
            // =========================================================================
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        reactivePrimaryColor.copy(alpha = 0.28f * glowPulse),
                        reactiveSecondaryColor.copy(alpha = 0.20f * glowPulse),
                        reactiveDeepColor.copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.65f + driftX, h * 0.42f + pShiftY * 0.25f),
                    radius = w * 0.58f
                ),
                center = Offset(w * 0.65f + driftX, h * 0.42f + pShiftY * 0.25f),
                radius = w * 0.58f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        reactiveSecondaryColor.copy(alpha = 0.26f * glowPulse),
                        reactivePrimaryColor.copy(alpha = 0.18f * glowPulse),
                        reactiveDeepColor.copy(alpha = 0.30f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.28f - driftX, h * 0.72f + driftY + pShiftY * 0.20f),
                    radius = w * 0.62f
                ),
                center = Offset(w * 0.28f - driftX, h * 0.72f + driftY + pShiftY * 0.20f),
                radius = w * 0.62f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        reactiveEmberColor.copy(alpha = 0.24f * glowPulse),
                        Color(0xFF581C87).copy(alpha = 0.22f * glowPulse),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.78f + driftX * 0.7f, h * 0.88f - driftY),
                    radius = w * 0.52f
                ),
                center = Offset(w * 0.78f + driftX * 0.7f, h * 0.88f - driftY),
                radius = w * 0.52f
            )

            // =========================================================================
            // 2. GOTHIC ARCH / CASTLE SILHOUETTES & REACTIVE ENERGY VEINS
            // =========================================================================
            val edgeCorridorW = w * 0.11f
            val gothicSilhouetteColor = Color(0xFF2E1065).copy(alpha = 0.18f)
            val gothicArchStroke = reactivePrimaryColor.copy(alpha = 0.17f * glowPulse)

            val leftSpirePath = Path().apply {
                moveTo(0f, h * 0.28f)
                lineTo(edgeCorridorW * 0.65f, h * 0.34f)
                lineTo(edgeCorridorW * 0.35f, h * 0.42f)
                lineTo(edgeCorridorW * 0.80f, h * 0.50f)
                lineTo(edgeCorridorW * 0.40f, h * 0.64f)
                lineTo(edgeCorridorW * 0.75f, h * 0.76f)
                lineTo(edgeCorridorW * 0.30f, h * 0.90f)
                lineTo(0f, h)
                close()
            }
            drawPath(path = leftSpirePath, color = gothicSilhouetteColor)
            drawPath(
                path = leftSpirePath,
                color = gothicArchStroke,
                style = Stroke(width = 1.1.dp.toPx())
            )

            val rightSpirePath = Path().apply {
                moveTo(w, h * 0.26f)
                lineTo(w - edgeCorridorW * 0.70f, h * 0.33f)
                lineTo(w - edgeCorridorW * 0.38f, h * 0.44f)
                lineTo(w - edgeCorridorW * 0.82f, h * 0.54f)
                lineTo(w - edgeCorridorW * 0.42f, h * 0.68f)
                lineTo(w - edgeCorridorW * 0.78f, h * 0.80f)
                lineTo(w - edgeCorridorW * 0.32f, h * 0.92f)
                lineTo(w, h)
                close()
            }
            drawPath(path = rightSpirePath, color = gothicSilhouetteColor)
            drawPath(
                path = rightSpirePath,
                color = gothicArchStroke,
                style = Stroke(width = 1.1.dp.toPx())
            )

            val leftVeinPath = Path()
            val rightVeinPath = Path()
            val steps = 28
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                val py = h * (0.22f + t * 0.76f)
                val waveL = sin((t * 4.2f * PI + ambientPhase * 2f * PI).toFloat()) * (edgeCorridorW * 0.42f)
                val lx = (edgeCorridorW * 0.48f + waveL).coerceAtLeast(2f)

                val waveR = cos((t * 4.0f * PI - ambientPhase * 2f * PI).toFloat()) * (edgeCorridorW * 0.42f)
                val rx = (w - edgeCorridorW * 0.48f + waveR).coerceAtMost(w - 2f)

                if (i == 0) {
                    leftVeinPath.moveTo(lx, py)
                    rightVeinPath.moveTo(rx, py)
                } else {
                    leftVeinPath.lineTo(lx, py)
                    rightVeinPath.lineTo(rx, py)
                }
            }

            val veinBrush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    reactivePrimaryColor.copy(alpha = 0.22f * glowPulse),
                    reactiveSecondaryColor.copy(alpha = 0.20f * glowPulse),
                    reactiveEmberColor.copy(alpha = 0.18f * glowPulse),
                    Color.Transparent
                )
            )
            drawPath(
                path = leftVeinPath,
                brush = veinBrush,
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = rightVeinPath,
                brush = veinBrush,
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )

            // =========================================================================
            // 3A. SHARED REACTIVE COSMIC EMBERS (Weighted smoothly toward active theme)
            // =========================================================================
            AMBIENT_EMBER_PARTICLES.forEachIndexed { idx, particle ->
                val rawProgress = (particle.initialYFraction - emberPhase * particle.speedMultiplier) % 1f
                val yFrac = if (rawProgress < 0f) rawProgress + 1f else rawProgress
                val verticalEnvelope = when {
                    yFrac > 0.88f -> ((1f - yFrac) / 0.12f).coerceIn(0f, 1f)
                    yFrac < 0.16f -> (yFrac / 0.16f).coerceIn(0f, 1f)
                    else -> 1f
                }

                val swayPx = particle.swayAmplitudeDp.dp.toPx() *
                    sin(((yFrac * particle.swayFrequency + ambientPhase) * 2f * PI).toFloat())
                val px = (w * particle.xFraction + swayPx).coerceIn(4f, w - 4f)
                val py = h * (0.14f + yFrac * 0.84f)
                val radiusPx = (particle.sizeDp * 0.55f).dp.toPx()

                val baseParticleColor = when {
                    idx % 4 == 0 -> reactivePrimaryColor
                    idx % 4 == 1 -> reactiveEmberColor
                    idx % 4 == 2 -> reactiveSecondaryColor
                    else -> Color(0xFFF3E8FF)
                }

                val particleAlpha = (0.64f * verticalEnvelope * glowPulse).coerceIn(0f, 0.72f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            baseParticleColor.copy(alpha = particleAlpha * 0.68f),
                            Color.Transparent
                        ),
                        center = Offset(px, py),
                        radius = radiusPx * 3.6f
                    ),
                    center = Offset(px, py),
                    radius = radiusPx * 3.6f
                )
                drawCircle(
                    color = Color.White.copy(alpha = particleAlpha * 0.75f),
                    center = Offset(px, py),
                    radius = radiusPx * 0.75f
                )
            }

            // =========================================================================
            // 3B. SIGNATURE UNIVERSE-SPECIFIC PARTICLES & ATMOSPHERIC EFFECTS
            // =========================================================================

            // 0) GALATASARAY EVRENİ: Golden Stadium Floodlight Beams & Warm Amber-Crimson Sparkles
            if (wGs > 0.02f) {
                val beamShift = sin(phaseRad) * w * 0.08f
                val gsRayPath = Path().apply {
                    moveTo(w * 0.12f + beamShift, h * 0.20f)
                    lineTo(w * 0.34f + beamShift, h * 0.20f)
                    lineTo(w * 0.86f - beamShift * 0.5f, h * 0.92f)
                    lineTo(w * 0.52f - beamShift * 0.5f, h * 0.92f)
                    close()
                }
                drawPath(
                    path = gsRayPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFFFBBF24).copy(alpha = 0.07f * wGs),
                            Color(0xFFDC2626).copy(alpha = 0.04f * wGs),
                            Color.Transparent
                        )
                    )
                )
                for (k in 0 until 8) {
                    val prog = ((k * 0.137f) - emberPhase * (0.9f + (k % 3) * 0.25f)) % 1f
                    val yf = if (prog < 0f) prog + 1f else prog
                    val env = sin(yf * PI).toFloat().coerceIn(0f, 1f)
                    val sx = w * (0.10f + ((k * 0.113f + 0.08f) % 0.80f)) + sin((yf * 6f + k).toDouble()).toFloat() * 12.dp.toPx()
                    val sy = h * (0.24f + yf * 0.70f)
                    val spSize = (4.5f + (k % 3) * 1.8f).dp.toPx()
                    val starColor = if (k % 2 == 0) Color(0xFFFBBF24) else Color(0xFFFB7185)
                    val spPath = Path().apply {
                        moveTo(sx, sy - spSize)
                        lineTo(sx + spSize * 0.22f, sy - spSize * 0.22f)
                        lineTo(sx + spSize, sy)
                        lineTo(sx + spSize * 0.22f, sy + spSize * 0.22f)
                        lineTo(sx, sy + spSize)
                        lineTo(sx - spSize * 0.22f, sy + spSize * 0.22f)
                        lineTo(sx - spSize, sy)
                        lineTo(sx - spSize * 0.22f, sy - spSize * 0.22f)
                        close()
                    }
                    drawPath(spPath, color = starColor.copy(alpha = 0.62f * env * wGs))
                }
            }

            // 1) BEYAZ GÖZLER (DISPATCH): Electric Cyan 4-Point Neural Sparks & Expanding Telekinetic Rings
            if (wDispatch > 0.02f) {
                for (r in 0 until 3) {
                    val ringProg = (ambientPhase + r * 0.33f) % 1f
                    val ringAlpha = (1f - ringProg) * 0.22f * wDispatch
                    val cx = w * (0.25f + r * 0.26f)
                    val cy = h * (0.46f + (r % 2) * 0.26f)
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = ringAlpha),
                        radius = (18f + ringProg * 72f).dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.3.dp.toPx())
                    )
                }
                for (k in 0 until 10) {
                    val prog = ((k * 0.19f) - emberPhase * (1.25f + (k % 2) * 0.4f)) % 1f
                    val yf = if (prog < 0f) prog + 1f else prog
                    val env = sin(yf * PI).toFloat().coerceIn(0f, 1f)
                    val sx = w * (0.08f + ((k * 0.17f) % 0.84f)) + cos((ambientPhase * 2f * PI + k).toDouble()).toFloat() * 16.dp.toPx()
                    val sy = h * (0.22f + yf * 0.72f)
                    val rPx = 4.2.dp.toPx()
                    drawLine(
                        color = Color(0xFFE0F2FE).copy(alpha = 0.78f * env * wDispatch),
                        start = Offset(sx - rPx, sy),
                        end = Offset(sx + rPx, sy),
                        strokeWidth = 1.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF22D3EE).copy(alpha = 0.78f * env * wDispatch),
                        start = Offset(sx, sy - rPx * 1.4f),
                        end = Offset(sx, sy + rPx * 1.4f),
                        strokeWidth = 1.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 2) KIRMIZI GÖZLER (YIKIM PROTOKOLÜ): Swirling Crimson Ash Streaks & Ruby Embers
            if (wJoker > 0.02f) {
                for (k in 0 until 12) {
                    val prog = ((k * 0.11f) - emberPhase * (1.15f + (k % 3) * 0.35f)) % 1f
                    val yf = if (prog < 0f) prog + 1f else prog
                    val env = sin(yf * PI).toFloat().coerceIn(0f, 1f)
                    val sx = w * (0.06f + ((k * 0.13f) % 0.88f)) + sin((yf * 8f + k * 1.3f).toDouble()).toFloat() * 18.dp.toPx()
                    val sy = h * (0.22f + yf * 0.74f)
                    val streakLen = (9f + (k % 3) * 5f).dp.toPx()
                    drawLine(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFF43F5E).copy(alpha = 0.78f * env * wJoker),
                                Color(0xFFE11D48).copy(alpha = 0.40f * env * wJoker),
                                Color.Transparent
                            )
                        ),
                        start = Offset(sx, sy),
                        end = Offset(sx - 3.dp.toPx(), sy + streakLen),
                        strokeWidth = 2.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3) NATASHA ROMANOFF (HASTANE): Calm Suspended Arctic Blue & Emerald Bioluminescent Dust
            if (wDoctor > 0.02f) {
                for (k in 0 until 11) {
                    val driftProg = ((k * 0.15f) + ambientPhase * (0.45f + (k % 3) * 0.15f)) % 1f
                    val sx = w * driftProg
                    val sy = h * (0.26f + ((k * 0.068f) % 0.68f)) + sin((driftProg * 2f * PI + k).toDouble()).toFloat() * 10.dp.toPx()
                    val env = sin(driftProg * PI).toFloat().coerceIn(0f, 1f)
                    val c = if (k % 2 == 0) Color(0xFF38BDF8) else Color(0xFF34D399)
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(c.copy(alpha = 0.56f * env * wDoctor), Color.Transparent),
                            center = Offset(sx, sy),
                            radius = 12.dp.toPx()
                        ),
                        center = Offset(sx, sy),
                        radius = 12.dp.toPx()
                    )
                    drawCircle(
                        color = Color(0xFFE0F2FE).copy(alpha = 0.72f * env * wDoctor),
                        radius = 1.8.dp.toPx(),
                        center = Offset(sx, sy)
                    )
                }
            }

            // 4) SIFIR NOKTASI (TOKYO): Diagonal Neon Rain Streaks & Magenta Cyber Fireflies
            if (wTokyo > 0.02f) {
                for (k in 0 until 14) {
                    val prog = ((k * 0.143f) + emberPhase * (1.85f + (k % 3) * 0.45f)) % 1f
                    val env = sin(prog * PI).toFloat().coerceIn(0f, 1f)
                    val sx = w * ((0.06f + k * 0.072f + prog * 0.08f) % 0.96f)
                    val sy = h * (0.18f + prog * 0.78f)
                    val rainCol = if (k % 2 == 0) Color(0xFFE879F9) else Color(0xFF22D3EE)
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(
                                Color.Transparent,
                                rainCol.copy(alpha = 0.65f * env * wTokyo),
                                Color.White.copy(alpha = 0.45f * env * wTokyo)
                            ),
                            start = Offset(sx + 6.dp.toPx(), sy - 16.dp.toPx()),
                            end = Offset(sx, sy)
                        ),
                        start = Offset(sx + 6.dp.toPx(), sy - 16.dp.toPx()),
                        end = Offset(sx, sy),
                        strokeWidth = 1.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/**
 * BÖLÜM 3.1: Top Hero Area with Periodic Lightning Flash, Silhouette Rim Light,
 * and Breathing Neon "⚡ Stories" Pulse (with generous overflow-visible padding buffer).
 */
@Composable
private fun AidenTopHeaderBanner(
    glowPulse: Float,
    lightningFlash: Float,
    onDismiss: () -> Unit
) {
    val closeInteraction = remember { MutableInteractionSource() }
    val closePressed by closeInteraction.collectIsPressedAsState()
    val closeScale by animateFloatAsState(
        targetValue = if (closePressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "close_btn_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(841f / 260f)
    ) {
        AidenCoilCoverImage(
            drawableResId = R.drawable.img_aiden_modal_header_v12,
            contentDescription = "Aiden Blackwood Stories",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()

                    // 1. Breathing neon aura around the top-left ⚡ icon and script "Stories" logo
                    //    (with generous radius & zero clipping so letter swashes are never cut off)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFD946EF).copy(alpha = (0.14f + 0.10f * (glowPulse - 0.85f) / 0.15f)),
                                Color(0xFF9333EA).copy(alpha = 0.08f * glowPulse),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.21f, size.height * 0.39f),
                            radius = size.width * 0.22f
                        ),
                        center = Offset(size.width * 0.21f, size.height * 0.39f),
                        radius = size.width * 0.22f
                    )

                    // 2. Periodic Lightning Flash Pulse behind the purple storm clouds & Aiden's silhouette (every ~4.5s)
                    if (lightningFlash > 0.01f) {
                        // Cloud lightning flash behind Aiden's head/shoulders
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFF5D0FE).copy(alpha = 0.34f * lightningFlash),
                                    Color(0xFFC084FC).copy(alpha = 0.22f * lightningFlash),
                                    Color(0xFF7E22CE).copy(alpha = 0.10f * lightningFlash),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.67f, size.height * 0.28f),
                                radius = size.width * 0.32f
                            ),
                            center = Offset(size.width * 0.67f, size.height * 0.28f),
                            radius = size.width * 0.32f,
                            blendMode = BlendMode.Plus
                        )

                        // Glowing purple rim-light contour along Aiden's silhouette shoulders & hair
                        val rimPath = Path().apply {
                            moveTo(size.width * 0.54f, size.height * 0.64f)
                            quadraticTo(
                                size.width * 0.60f,
                                size.height * 0.42f,
                                size.width * 0.63f,
                                size.height * 0.18f
                            )
                            quadraticTo(
                                size.width * 0.67f,
                                size.height * 0.06f,
                                size.width * 0.72f,
                                size.height * 0.18f
                            )
                            quadraticTo(
                                size.width * 0.75f,
                                size.height * 0.42f,
                                size.width * 0.81f,
                                size.height * 0.64f
                            )
                        }
                        drawPath(
                            path = rimPath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFF5D0FE).copy(alpha = 0.55f * lightningFlash),
                                    Color(0xFFD946EF).copy(alpha = 0.65f * lightningFlash),
                                    Color.Transparent
                                )
                            ),
                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
                            blendMode = BlendMode.Plus
                        )
                    }

                    // 3. Long, silky-smooth vertical feathering across the bottom 36% of the extended 260px header
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.00f to Color.Black,
                            0.64f to Color.Black,
                            0.82f to Color.Black.copy(alpha = 0.52f),
                            0.93f to Color.Black.copy(alpha = 0.15f),
                            1.00f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 8.dp)
                .size(48.dp)
                .graphicsLayer {
                    scaleX = closeScale
                    scaleY = closeScale
                }
                .clip(CircleShape)
                .clickable(
                    interactionSource = closeInteraction,
                    indication = null,
                    onClick = onDismiss
                )
                .testTag("aiden_modal_close_button"),
            contentAlignment = Alignment.Center
        ) {
            if (closePressed) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x44C084FC))
                )
            }
        }
    }
}

/**
 * BÖLÜM 4.3.2: Sticky Translucent Frosted Glass Mini Top Bar when scrolled (> 80.dp)
 * - Left: ⚡ Aiden Blackwood Stories
 * - Right: Always-accessible ✕ Close button
 */
@Composable
private fun AidenStickyMiniTopBar(
    glowPulse: Float,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xEB120624),
                        Color(0xD8180930)
                    )
                )
            )
            .drawBehind {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFC084FC).copy(alpha = 0.65f * glowPulse),
                            Color(0xFFF5D0FE).copy(alpha = 0.85f * glowPulse),
                            Color(0xFFC084FC).copy(alpha = 0.65f * glowPulse),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.1.dp.toPx()
                )
            }
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("aiden_sticky_mini_top_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AidenMiniBadgeIcon(
                    iconType = AidenBadgeIconType.LIGHTNING,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aiden Blackwood ",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = CenteredButtonTextStyle
                )
                Text(
                    text = "Stories",
                    color = Color(0xFFE879F9),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    style = CenteredButtonTextStyle
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0x662E1065))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFD8B4FE).copy(alpha = 0.65f),
                        shape = CircleShape
                    )
                    .clickable(onClick = onDismiss)
                    .testTag("aiden_sticky_close_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * BÖLÜM 3.2: 3D Perspective Peeking Carousel with Live Progress Fill,
 * `"✦ Daha Fazla Göster ↗"` Capsule, and Pause-on-Interaction Protection.
 */
@Composable
private fun AidenHeroPeekingCarousel(
    specs: List<AidenCatalogRowSpec>,
    ambientPhase: Float,
    glowPulse: Float,
    isPausedExternal: Boolean,
    startedPresetIds: Set<String>,
    onContinuousUniverseChanged: (Float) -> Unit,
    onLaunchActiveSlide: (AidenCatalogRowSpec) -> Unit,
    onShowMoreClick: (AidenCatalogRowSpec) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val count = specs.size
    val virtualPageCount = count * 400
    val initialVirtualPage = count * 200
    val pagerState = rememberPagerState(
        initialPage = initialVirtualPage,
        pageCount = { virtualPageCount }
    )

    val isUserDragging by pagerState.interactionSource.collectIsDraggedAsState()
    val isUserPressed by pagerState.interactionSource.collectIsPressedAsState()
    val isPaused = isUserDragging || isUserPressed || isPausedExternal

    val slideTimerProgress = remember { Animatable(0f) }
    var timerOwnerPage by remember { mutableIntStateOf(initialVirtualPage) }

    val settledUniverseIndex by remember {
        derivedStateOf {
            ((pagerState.settledPage % count) + count) % count
        }
    }

    // Emit continuous fractional page position every frame during swipe or auto-scroll so reactive particles & colors sync 1:1!
    val continuousPagePos = pagerState.currentPage + pagerState.currentPageOffsetFraction
    onContinuousUniverseChanged(continuousPagePos)

    // Section 3.2: Silky Auto-Advance Loop & Pause-on-Interaction Protection
    // Ownership transfers cleanly to the newly settled page at 0%, glides smoothly 0 -> 1 over 4800ms,
    // and keeps ownership on the outgoing page during the 520ms transition so its fill fades out naturally
    // while the incoming pill expands cleanly from 0% without ever flashing full or stalling.
    LaunchedEffect(pagerState.settledPage, isPaused) {
        timerOwnerPage = pagerState.settledPage
        slideTimerProgress.snapTo(0f)
        if (isPaused) {
            return@LaunchedEffect
        }
        slideTimerProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 4800, easing = LinearEasing)
        )
        pagerState.animateScrollToPage(
            page = pagerState.settledPage + 1,
            animationSpec = tween(
                durationMillis = 520,
                easing = CubicBezierEasing(0.25f, 0.85f, 0.35f, 1f)
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp),
            contentAlignment = Alignment.Center
        ) {
            // 1. Subtle localized aura behind the active Carousel card
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF9333EA).copy(alpha = 0.22f * glowPulse),
                            Color(0xFF5B21B6).copy(alpha = 0.10f * glowPulse),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.78f, h * 0.86f),
                        radius = w * 0.24f
                    ),
                    center = Offset(w * 0.78f, h * 0.86f),
                    radius = w * 0.24f
                )
            }

            // 2. Secondary Peeking Translucent Glass Layer
            val bgPeekSpec = specs[(settledUniverseIndex + 2) % count]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(148.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x551E083A))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFC084FC).copy(alpha = 0.48f),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                AidenCoilCoverImage(
                    drawableResId = bgPeekSpec.carouselDrawableRes,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.45f }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x772A0E4E),
                                    Color(0x8816062C)
                                )
                            )
                        )
                )
            }

            // 3. 3D PERSPECTIVE HORIZONTAL PAGER (scale 0.92 -> 1.0, peek opacity 0.60 -> 1.0)
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 36.dp),
                pageSpacing = 8.dp,
                beyondViewportPageCount = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
                    .testTag("aiden_carousel_pager")
            ) { page ->
                val spec = specs[((page % count) + count) % count]
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val absOffset = abs(pageOffset).coerceIn(0f, 1f)
                val centerFactor = (1f - absOffset).coerceIn(0f, 1f)

                // Section 3.2: scale(0.92 -> 1.0) and peek cards at 60% opacity (0.60 -> 1.0)
                val cardScale = 0.92f + (0.08f * centerFactor)
                val cardAlpha = 0.60f + (0.40f * centerFactor)
                val contentTextAlpha = (1f - absOffset * 1.55f).coerceIn(0f, 1f)

                val cardShape = RoundedCornerShape(22.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(172.dp)
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                            alpha = cardAlpha
                            rotationY = -pageOffset.coerceIn(-1f, 1f) * 6.5f
                            cameraDistance = 16f * density
                        }
                        .neonOuterGlow(
                            cornerRadius = 22.dp,
                            glowColor = if (centerFactor > 0.5f) Color(0xFF9333EA) else Color(0xFF818CF8),
                            secondaryColor = if (centerFactor > 0.5f) Color(0xFFF5D0FE) else Color(0xFFD8B4FE),
                            blurRadius = (10f + 5f * centerFactor).dp,
                            glowAlpha = (0.44f + 0.30f * centerFactor) * glowPulse,
                            flowPhase = ambientPhase,
                            hotspotFlares = centerFactor > 0.5f
                        )
                        .clip(cardShape)
                        .background(Color(0x661C0838))
                        .border(
                            width = (1.2f + 0.35f * centerFactor).dp,
                            brush = Brush.linearGradient(
                                colors = if (centerFactor > 0.5f) {
                                    listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFFD8B4FE),
                                        Color(0xFFA855F7),
                                        Color(0xFFF5D0FE),
                                        Color(0xFFFFFFFF)
                                    )
                                } else {
                                    listOf(
                                        Color(0xFF93C5FD).copy(alpha = 0.85f),
                                        Color(0xFFC084FC).copy(alpha = 0.75f),
                                        Color(0xFFF3E8FF).copy(alpha = 0.88f)
                                    )
                                }
                            ),
                            shape = cardShape
                        )
                        .clickable {
                            if (absOffset < 0.45f) {
                                onLaunchActiveSlide(spec)
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = page,
                                        animationSpec = tween(520, easing = FastOutSlowInEasing)
                                    )
                                }
                            }
                        }
                        .then(
                            if (page == pagerState.currentPage) {
                                Modifier.testTag("aiden_carousel_main_card")
                            } else {
                                Modifier
                            }
                        )
                ) {
                    AidenCoilCoverImage(
                        drawableResId = spec.carouselDrawableRes,
                        contentDescription = spec.carouselTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = pageOffset * 14.dp.toPx()
                            }
                    )

                    if (absOffset > 0.05f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color(0xFF1A0940).copy(alpha = (absOffset * 0.35f).coerceIn(0f, 0.35f))
                                )
                        )
                    }

                    // Contrast armor gradient at the bottom of the Carousel slide
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.64f)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color.Transparent,
                                    0.38f to Color(0x88120626),
                                    1.0f to Color(0xDF0D041C)
                                )
                            )
                    )

                    // Fast GPU Inner Frosted Glass Rim around the Carousel Card
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cr = 22.dp.toPx()
                        val inset = 1.5.dp.toPx()
                        drawRoundRect(
                            color = Color(0xFFD8B4FE).copy(alpha = (0.28f * glowPulse).coerceIn(0f, 0.45f)),
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - inset * 2f, size.height - inset * 2f),
                            cornerRadius = CornerRadius(cr, cr),
                            style = Stroke(width = 2.2.dp.toPx())
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 15.dp, end = 15.dp, bottom = 10.dp)
                            .graphicsLayer {
                                alpha = contentTextAlpha
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = spec.carouselTitle,
                                color = Color.White,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (startedPresetIds.contains(spec.presetId)) {
                                AidenInProgressBadge(glowPulse = glowPulse)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = spec.carouselSubtitle,
                            color = Color(0xFFE9D5FF).copy(alpha = 0.95f),
                            fontSize = 11.2.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AidenShowMorePillButton(
                            glowPulse = glowPulse,
                            onClick = { onShowMoreClick(spec) },
                            testTag = "aiden_carousel_show_more_${spec.presetId}"
                        )
                    }
                }
            }

            // Left Circular Arrow Button (←)
            CarouselNeonArrowButton(
                isLeft = true,
                ambientPhase = ambientPhase,
                glowPulse = glowPulse,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            page = pagerState.currentPage - 1,
                            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(36.dp)
                    .testTag("aiden_carousel_prev_button")
            )

            // Right Circular Arrow Button (→)
            CarouselNeonArrowButton(
                isLeft = false,
                ambientPhase = ambientPhase,
                glowPulse = glowPulse,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            page = pagerState.currentPage + 1,
                            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .size(36.dp)
                    .testTag("aiden_carousel_next_button")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Section 3.2: 5 Balanced Pagination Dots — normal 8.dp inactive dots, compact 14.dp active pill
        // that fills smoothly from 0% to 100% once settled without oversized bloat.
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0x52120526))
                .border(0.7.dp, Color(0x44A78BFA), RoundedCornerShape(50))
                .padding(horizontal = 9.dp, vertical = 4.5.dp)
                .testTag("aiden_carousel_pagination_row")
        ) {
            val rawFractionalPage = (pagerState.currentPage + pagerState.currentPageOffsetFraction)
            val fractionalMod = ((rawFractionalPage % count.toFloat()) + count.toFloat()) % count.toFloat()
            val ownerUniverseIndex = ((timerOwnerPage % count) + count) % count

            specs.forEachIndexed { idx, _ ->
                val rawDist = abs(fractionalMod - idx.toFloat())
                val circularDist = min(rawDist, count.toFloat() - rawDist)
                val linearSelection = (1f - circularDist).coerceIn(0f, 1f)
                // Direct frame-accurate smoothstep curve synced 1:1 with pager scroll (zero rubber-band lag!)
                val smoothSel = linearSelection * linearSelection * (3f - 2f * linearSelection)

                val capsuleWidth = 11.dp // Equal uniform size across all pagination indicators
                val capsuleHeight = 6.5.dp

                val isTimerOwner = idx == ownerUniverseIndex
                val fillProgress = if (isTimerOwner) {
                    slideTimerProgress.value.coerceIn(0f, 1f)
                } else {
                    0f
                }

                Box(
                    modifier = Modifier
                        .width(capsuleWidth)
                        .height(capsuleHeight)
                        .neonOuterGlow(
                            cornerRadius = 3.25.dp,
                            glowColor = Color(0xFFD946EF),
                            secondaryColor = Color(0xFFF5D0FE),
                            blurRadius = 3.5.dp,
                            glowAlpha = 0.50f * smoothSel * glowPulse,
                            flowPhase = ambientPhase
                        )
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF6B5394).copy(alpha = 0.72f + 0.24f * smoothSel),
                                    Color(0xFF47316E).copy(alpha = 0.76f + 0.20f * smoothSel)
                                )
                            )
                        )
                        .border(
                            width = 0.75.dp,
                            color = Color(0xFFC4B5FD).copy(alpha = 0.42f + 0.48f * smoothSel),
                            shape = RoundedCornerShape(50)
                        )
                        .drawWithContent {
                            drawContent()
                            if (smoothSel > 0.02f) {
                                // Dark amethyst unfilled track inside active pill
                                drawRoundRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF3B126E).copy(alpha = 0.85f * smoothSel),
                                            Color(0xFF2A0852).copy(alpha = 0.82f * smoothSel)
                                        )
                                    ),
                                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                                )

                                // Smooth 0% -> 100% progress fill; fades cleanly with smoothSel as outgoing pill shrinks
                                if (fillProgress > 0.002f) {
                                    val fillWidth = (size.width * fillProgress).coerceIn(0f, size.width)
                                    val barAlpha = smoothSel.coerceIn(0f, 1f)
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFFFFFF).copy(alpha = barAlpha),
                                                Color(0xFFF5D0FE).copy(alpha = barAlpha),
                                                Color(0xFFE879F9).copy(alpha = barAlpha * 0.96f)
                                            ),
                                            startX = 0f,
                                            endX = size.width
                                        ),
                                        size = Size(fillWidth, size.height)
                                    )
                                }
                            }
                        }
                        .clickable {
                            val currentMod = ((pagerState.currentPage % count) + count) % count
                            var delta = idx - currentMod
                            if (delta > count / 2) delta -= count
                            if (delta < -count / 2) delta += count
                            if (delta != 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = pagerState.currentPage + delta,
                                        animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
                                    )
                                }
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun CarouselNeonArrowButton(
    isLeft: Boolean,
    ambientPhase: Float,
    glowPulse: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "carousel_arrow_scale"
    )
    val arrowNudge by animateDpAsState(
        targetValue = if (isPressed) (if (isLeft) (-2.5).dp else 2.5.dp) else 0.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "carousel_arrow_nudge"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .neonOuterGlow(
                cornerRadius = 18.dp,
                glowColor = Color(0xFFA855F7),
                secondaryColor = Color(0xFFE9D5FF),
                blurRadius = 8.dp,
                glowAlpha = 0.54f * glowPulse,
                flowPhase = ambientPhase
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xBB2E1256),
                        Color(0xCC14052A)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF5D0FE).copy(alpha = 0.92f),
                        Color(0xFFC084FC).copy(alpha = 0.78f),
                        Color(0xFFE9D5FF).copy(alpha = 0.92f)
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isLeft) {
                Icons.AutoMirrored.Filled.ArrowBack
            } else {
                Icons.AutoMirrored.Filled.ArrowForward
            },
            contentDescription = if (isLeft) "Önceki" else "Sonraki",
            tint = Color.White,
            modifier = Modifier
                .size(17.dp)
                .offset(x = arrowNudge)
        )
    }
}

@Composable
private fun AidenFeaturedSectionHeader(
    storyCount: Int,
    glowPulse: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = 0.95f + 0.05f * glowPulse
                        scaleY = 0.95f + 0.05f * glowPulse
                    }
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val outerR = size.minDimension * 0.48f
                val innerR = size.minDimension * 0.14f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE879F9).copy(alpha = 0.48f),
                            Color(0xFFA855F7).copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = outerR * 1.45f
                    ),
                    center = Offset(cx, cy),
                    radius = outerR * 1.45f
                )

                val starPath = Path().apply {
                    moveTo(cx, cy - outerR)
                    quadraticTo(cx + innerR * 0.35f, cy - innerR * 0.35f, cx + outerR, cy)
                    quadraticTo(cx + innerR * 0.35f, cy + innerR * 0.35f, cx, cy + outerR)
                    quadraticTo(cx - innerR * 0.35f, cy + innerR * 0.35f, cx - outerR, cy)
                    quadraticTo(cx - innerR * 0.35f, cy - innerR * 0.35f, cx, cy - outerR)
                    close()
                }
                drawPath(
                    path = starPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF5D0FE),
                            Color(0xFFD946EF),
                            Color(0xFF9333EA)
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "ÖNE ÇIKAN EVREN",
                color = Color(0xFFF3E8FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp
            )
        }

        Text(
            text = "$storyCount Hikaye Mevcut",
            color = Color(0xFFD8B4FE).copy(alpha = 0.90f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * BÖLÜM 3.3: "ANA EVREN" Featured Universe Card (`AidenFeaturedUniverseCard`)
 * - Uses `R.drawable.img_aiden_modal_featured_clean_v14`:
 *   - Card background is genuine multi-layer Frosted Glass (`bg-[#120824]/85` + refracted purple nebula + specular bands)
 *     rendered strictly UNDERNEATH the Photo (left) and Özel Tasarım (right) overlay.
 *   - Continuous orbiting Border Beam (`borderBeam = true`) around the neon purple perimeter.
 *   - Locked minimum height (`min-h-[215px]` -> `heightIn(min = 215.dp)`) and full-height `self-stretch` portrait/castle
 *     so the `"✦ Daha Fazla Göster ↗"` capsule and `"⚡ Hikayeyi Başlat →"` button (`min-h-[40px]`) are never cramped or clipped!
 */
@Composable
private fun AidenFeaturedUniverseCard(
    spec: AidenCatalogRowSpec,
    isStarted: Boolean,
    ambientPhase: Float,
    glowPulse: Float,
    onStartClick: () -> Unit,
    onShowMoreClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "featured_card_scale"
    )
    val arrowNudge by animateDpAsState(
        targetValue = if (isPressed) 3.5.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "featured_arrow_nudge"
    )

    val cardShape = RoundedCornerShape(22.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .heightIn(min = 215.dp)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .neonOuterGlow(
                cornerRadius = 22.dp,
                glowColor = Color(0xFF9333EA),
                secondaryColor = Color(0xFFF5D0FE),
                blurRadius = 14.dp,
                glowAlpha = 0.74f * glowPulse,
                flowPhase = ambientPhase,
                hotspotFlares = true,
                borderBeam = true
            )
            .clip(cardShape)
            // Contrast armor + Translucent Frosted Glass Base underneath the Photo & Özel Tasarım
            .background(
                Brush.horizontalGradient(
                    0.00f to Color(0x58140628),
                    0.38f to Color(0x88240D46),
                    0.68f to Color(0x821E0A3C),
                    1.00f to Color(0x8C2A1050)
                )
            )
            .border(
                width = 1.4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF).copy(alpha = 0.94f),
                        Color(0xFFF5D0FE).copy(alpha = 0.90f),
                        Color(0xFFC084FC).copy(alpha = 0.84f),
                        Color(0xFFA855F7).copy(alpha = 0.84f),
                        Color(0xFFFFFFFF).copy(alpha = 0.95f)
                    )
                ),
                shape = cardShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onStartClick
            )
            .testTag("aiden_featured_universe_card")
    ) {
        // 1. All Multi-Layer Optical Frosted Glass + Central Cosmic Bridge Layers (rendered UNDERNEATH the Photo & Özel Tasarım)
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val phaseRad = (ambientPhase * 2f * PI).toFloat()

            // (a) Central Golden-to-Violet Cosmic Nebula Nexus bridging Aiden (left) & Castle Spires (right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF59E0B).copy(alpha = 0.24f * glowPulse),
                        Color(0xFF9333EA).copy(alpha = 0.26f * glowPulse),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.49f, h * 0.56f),
                    radius = w * 0.24f
                ),
                center = Offset(w * 0.49f, h * 0.56f),
                radius = w * 0.24f
            )

            // (b) Refracted upper-center/right purple cosmic nebula glow diffusing through the frosted glass background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFA855F7).copy(alpha = 0.40f * glowPulse),
                        Color(0xFF7E22CE).copy(alpha = 0.22f * glowPulse),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.58f, h * 0.24f),
                    radius = w * 0.34f
                ),
                center = Offset(w * 0.58f, h * 0.24f),
                radius = w * 0.34f
            )

            // (c) Refracted lower-right purple-orchid nebula bloom behind the Hikayeyi Başlat glass zone
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9333EA).copy(alpha = 0.38f * glowPulse),
                        Color(0xFF6B21A8).copy(alpha = 0.19f * glowPulse),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.70f, h * 0.86f),
                    radius = w * 0.32f
                ),
                center = Offset(w * 0.70f, h * 0.86f),
                radius = w * 0.32f
            )

            // (d) Architectural Celestial Arc & Energy Thread across the center glass bridge (x = 38%..76%)
            val arcPath = Path().apply {
                moveTo(w * 0.36f, h * 0.84f)
                quadraticTo(w * 0.54f, h * 0.38f, w * 0.78f, h * 0.12f)
            }
            drawPath(
                path = arcPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFFBBF24).copy(alpha = 0.32f * glowPulse),
                        Color(0xFFF5D0FE).copy(alpha = 0.42f * glowPulse),
                        Color(0xFFC084FC).copy(alpha = 0.26f * glowPulse),
                        Color.Transparent
                    ),
                    start = Offset(w * 0.36f, h * 0.84f),
                    end = Offset(w * 0.78f, h * 0.12f)
                ),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )
            // Secondary subtle golden-lilac orbit arc
            val arcPath2 = Path().apply {
                moveTo(w * 0.38f, h * 0.22f)
                quadraticTo(w * 0.55f, h * 0.62f, w * 0.72f, h * 0.90f)
            }
            drawPath(
                path = arcPath2,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFD8B4FE).copy(alpha = 0.26f * glowPulse),
                        Color(0xFFFBBF24).copy(alpha = 0.22f * glowPulse),
                        Color.Transparent
                    ),
                    start = Offset(w * 0.38f, h * 0.22f),
                    end = Offset(w * 0.72f, h * 0.90f)
                ),
                style = Stroke(width = 1.0.dp.toPx(), cap = StrokeCap.Round)
            )

            // (e) Drifting Golden & Lavender Starlight Dust across the center frosted bridge (x = 41%..66%)
            val bridgeSparks = listOf(
                Triple(0.45f, 0.24f, 0),
                Triple(0.51f, 0.44f, 1),
                Triple(0.47f, 0.68f, 0),
                Triple(0.56f, 0.32f, 1),
                Triple(0.61f, 0.58f, 0),
                Triple(0.43f, 0.82f, 1),
                Triple(0.54f, 0.78f, 0)
            )
            bridgeSparks.forEachIndexed { idx, (bx, by, cType) ->
                val pulse = 0.45f + 0.55f * ((sin(phaseRad + idx * 1.15f) + 1f) * 0.5f)
                val sx = w * bx + sin((phaseRad + idx).toDouble()).toFloat() * 4.dp.toPx()
                val sy = h * by + cos((phaseRad + idx * 0.8f).toDouble()).toFloat() * 3.dp.toPx()
                val spCol = if (cType == 0) Color(0xFFFDE68A) else Color(0xFFF5D0FE)
                val sr = if (idx % 2 == 0) 3.4.dp.toPx() else 2.6.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(spCol.copy(alpha = 0.48f * pulse), Color.Transparent),
                        center = Offset(sx, sy),
                        radius = sr * 2.8f
                    ),
                    center = Offset(sx, sy),
                    radius = sr * 2.8f
                )
                val starP = Path().apply {
                    moveTo(sx, sy - sr)
                    lineTo(sx + sr * 0.24f, sy - sr * 0.24f)
                    lineTo(sx + sr, sy)
                    lineTo(sx + sr * 0.24f, sy + sr * 0.24f)
                    lineTo(sx, sy + sr)
                    lineTo(sx - sr * 0.24f, sy + sr * 0.24f)
                    lineTo(sx - sr, sy)
                    lineTo(sx - sr * 0.24f, sy - sr * 0.24f)
                    close()
                }
                drawPath(starP, color = Color.White.copy(alpha = 0.82f * pulse))
            }

            // (f) Frosted Glass Surface Sheen + Subtle Angled Prism Bands across the exposed glass background
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFE9D5FF).copy(alpha = 0.14f),
                        Color(0xFFC084FC).copy(alpha = 0.10f),
                        Color(0xFFF5D0FE).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    start = Offset(w * 0.36f, 0f),
                    end = Offset(w * 0.92f, h)
                )
            )

            val sheenOffset = w * (0.40f + 0.10f * sin((ambientPhase * 2f * PI).toFloat()))
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFF5D0FE).copy(alpha = 0.14f * glowPulse),
                        Color.White.copy(alpha = 0.18f * glowPulse),
                        Color(0xFFD8B4FE).copy(alpha = 0.11f * glowPulse),
                        Color.Transparent
                    ),
                    start = Offset(sheenOffset, 0f),
                    end = Offset(sheenOffset + w * 0.26f, h)
                )
            )

            // (g) Top & Bottom Frosted Glass Bevel Edge Refraction
            drawRect(
                brush = Brush.verticalGradient(
                    0.00f to Color(0xFFF5D0FE).copy(alpha = 0.26f),
                    0.18f to Color.Transparent,
                    0.82f to Color.Transparent,
                    1.00f to Color(0xFFD8B4FE).copy(alpha = 0.22f)
                )
            )
        }

        // 2. Refined v16 Photo (Left) & Full-Height Gothic Castle Spires/Clouds (Right)
        AidenCoilCoverImage(
            drawableResId = R.drawable.img_aiden_modal_featured_clean_v16,
            contentDescription = "Aiden Blackwood",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        // 3. Fast GPU 360° Perimeter Inner Neon Border Rim
        Canvas(modifier = Modifier.matchParentSize()) {
            val cr = 22.dp.toPx()
            val inset = 1.8.dp.toPx()
            drawRoundRect(
                color = Color(0xFFC084FC).copy(alpha = (0.30f * glowPulse).coerceIn(0f, 0.45f)),
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2f, size.height - inset * 2f),
                cornerRadius = CornerRadius(cr, cr),
                style = Stroke(width = 2.4.dp.toPx())
            )
        }

        // 4. Right Content Column (50.0% width, aligned to the right so Aiden's photo & transition are 100% unobstructed)
        Column(
            modifier = Modifier
                .fillMaxWidth(0.50f)
                .heightIn(min = 215.dp)
                .align(Alignment.CenterEnd)
                .padding(top = 12.dp, bottom = 11.dp, start = 2.dp, end = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    AidenAnaEvrenGlassBadge(
                        ambientPhase = ambientPhase,
                        glowPulse = glowPulse
                    )
                    if (isStarted) {
                        AidenInProgressBadge(glowPulse = glowPulse)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Aiden Blackwood",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.1.sp,
                    maxLines = 1,
                    style = CenteredButtonTextStyle
                )

                Spacer(modifier = Modifier.height(3.5.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3E8FF))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "21 Yaş",
                        color = Color(0xFFF3E8FF),
                        fontSize = 11.4.sp,
                        fontWeight = FontWeight.Medium,
                        style = CenteredButtonTextStyle
                    )
                    Text(
                        text = "  |  ",
                        color = Color(0xFFA78BFA).copy(alpha = 0.75f),
                        fontSize = 11.4.sp,
                        fontWeight = FontWeight.Normal,
                        style = CenteredButtonTextStyle
                    )
                    Text(
                        text = "Galatasaray",
                        color = Color(0xFFF3E8FF),
                        fontSize = 11.4.sp,
                        fontWeight = FontWeight.Medium,
                        style = CenteredButtonTextStyle
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // 2-line clean summary without ugly "..." truncation, followed by the custom "✦ Daha Fazla Göster ↗" pill
                Text(
                    text = "Galatasaray'ın 21 yaşındaki genç yeteneği Aiden Blackwood, futbol dünyasında adından söz ettiriyor.",
                    color = Color(0xFFEDE2F8).copy(alpha = 0.96f),
                    fontSize = 10.6.sp,
                    lineHeight = 13.8.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(5.dp))

                AidenShowMorePillButton(
                    glowPulse = glowPulse,
                    onClick = onShowMoreClick,
                    testTag = "aiden_featured_show_more_button"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.align(Alignment.End)) {
                AidenReferenceHikayeyiBaslatButton(
                    label = if (isStarted) "Hikayeye Devam Et" else "Hikayeyi Başlat",
                    ambientPhase = ambientPhase,
                    glowPulse = glowPulse,
                    arrowNudge = arrowNudge,
                    onClick = onStartClick
                )
            }
        }
    }
}

@Composable
private fun AidenAnaEvrenGlassBadge(
    ambientPhase: Float,
    glowPulse: Float
) {
    val pillShape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .height(23.dp)
            .neonOuterGlow(
                cornerRadius = 12.dp,
                glowColor = Color(0xFF8B3DFF),
                secondaryColor = Color(0xFFE9D5FF),
                blurRadius = 5.dp,
                glowAlpha = 0.45f * glowPulse,
                flowPhase = ambientPhase
            )
            .clip(pillShape)
            .background(
                Brush.verticalGradient(
                    0.00f to Color(0xCC441E78),
                    0.28f to Color(0xB8240B48),
                    0.52f to Color(0xB01C073A),
                    0.78f to Color(0xB8260D4C),
                    1.00f to Color(0xCC46207A)
                )
            )
            .border(
                width = 1.05.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF3E8FF).copy(alpha = 0.92f),
                        Color(0xFFB794F4).copy(alpha = 0.76f),
                        Color(0xFFE9D5FF).copy(alpha = 0.90f)
                    )
                ),
                shape = pillShape
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AidenMiniBadgeIcon(
            iconType = AidenBadgeIconType.STAR,
            modifier = Modifier.size(11.5.dp)
        )
        Spacer(modifier = Modifier.width(5.5.dp))
        Text(
            text = "ANA EVREN",
            color = Color.White,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.55.sp,
            style = CenteredButtonTextStyle
        )
    }
}

/**
 * BÖLÜM 3.3: Full-Visibility `"⚡ Hikayeyi Başlat →"` Button
 * - Locked `min-h-[40px]` (`heightIn(min = 40.dp)`), `py-2 px-4`, `active:scale-95` spring,
 *   and right-arrow lunge animation (`arrowNudge`).
 */
@Composable
private fun AidenReferenceHikayeyiBaslatButton(
    label: String = "Hikayeyi Başlat",
    ambientPhase: Float,
    glowPulse: Float,
    arrowNudge: Dp,
    onClick: () -> Unit
) {
    val btnInteraction = remember { MutableInteractionSource() }
    val isPressed by btnInteraction.collectIsPressedAsState()
    val btnScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium),
        label = "featured_start_btn_scale"
    )
    val localArrowNudge by animateDpAsState(
        targetValue = if (isPressed) 4.dp else arrowNudge,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium),
        label = "featured_start_btn_arrow"
    )

    val pillShape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .heightIn(min = 40.dp)
            .graphicsLayer {
                scaleX = btnScale
                scaleY = btnScale
            }
            .neonOuterGlow(
                cornerRadius = 20.dp,
                glowColor = Color(0xFF9333EA),
                secondaryColor = Color(0xFFF5D0FE),
                blurRadius = 11.dp,
                glowAlpha = 0.74f * glowPulse,
                flowPhase = ambientPhase,
                buttonLeftHotspot = true
            )
            .clip(pillShape)
            .background(
                Brush.horizontalGradient(
                    0.00f to Color(0xE6311066),
                    0.50f to Color(0xDE250954),
                    1.00f to Color(0xD2331268)
                )
            )
            .border(
                width = 1.25.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF5D0FE),
                        Color(0xFFB587E3),
                        Color(0xFFEEC2FD),
                        Color(0xFFFFFFFF)
                    )
                ),
                shape = pillShape
            )
            .drawBehind {
                val r = size.height / 2f
                val inset = 1.4.dp.toPx()
                drawRoundRect(
                    color = Color(0xFF7730CC).copy(alpha = (0.42f * glowPulse).coerceIn(0f, 0.6f)),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2f, size.height - inset * 2f),
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = 2.8.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFFB41E).copy(alpha = 0.28f * glowPulse), Color.Transparent),
                        center = Offset(22.dp.toPx(), size.height / 2f),
                        radius = 12.dp.toPx()
                    ),
                    radius = 12.dp.toPx(),
                    center = Offset(22.dp.toPx(), size.height / 2f)
                )
            }
            .clickable(
                interactionSource = btnInteraction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 13.dp, vertical = 8.dp)
            .testTag("aiden_featured_start_button"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_aiden_btn_bolt_3d),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 18.dp, height = 21.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = label,
            color = Color.White,
            fontSize = 11.8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.1.sp,
            maxLines = 1,
            softWrap = false,
            style = CenteredButtonTextStyle
        )

        Spacer(modifier = Modifier.width(7.dp))

        Canvas(
            modifier = Modifier
                .size(width = 11.dp, height = 9.dp)
                .offset(x = localArrowNudge)
        ) {
            val cy = size.height / 2f
            val strokeW = 1.5.dp.toPx()
            drawLine(
                color = Color.White,
                start = Offset(0f, cy),
                end = Offset(size.width - 0.5.dp.toPx(), cy),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            val headPath = Path().apply {
                moveTo(size.width * 0.56f, cy - size.height * 0.46f)
                lineTo(size.width, cy)
                lineTo(size.width * 0.56f, cy + size.height * 0.46f)
            }
            drawPath(
                path = headPath,
                color = Color.White,
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

/**
 * BÖLÜM 4.3.3: `"☰ TÜM AIDEN HİKAYELERİ"` Section Header + Glowing `"🎲 Rastgele"` Story Picker Button
 */
@Composable
private fun AidenAllStoriesSectionHeader(
    glowPulse: Float,
    ambientPhase: Float,
    onRandomStoryClick: () -> Unit
) {
    val rngInteraction = remember { MutableInteractionSource() }
    val isPressed by rngInteraction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "random_story_btn_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        alpha = 0.92f + 0.08f * glowPulse
                    }
            ) {
                val rowYs = listOf(size.height * 0.22f, size.height * 0.50f, size.height * 0.78f)
                val dotRadius = 2.1.dp.toPx()
                val barHeight = 3.2.dp.toPx()
                val barStartX = 6.5.dp.toPx()
                val barWidth = size.width - barStartX - 1.dp.toPx()

                rowYs.forEach { cy ->
                    drawRoundRect(
                        color = Color(0xFFD8B4FE),
                        topLeft = Offset(1.dp.toPx(), cy - dotRadius),
                        size = Size(dotRadius * 2f, dotRadius * 2f),
                        cornerRadius = CornerRadius(1.2.dp.toPx(), 1.2.dp.toPx())
                    )
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFF3E8FF), Color(0xFFC084FC))
                        ),
                        topLeft = Offset(barStartX, cy - barHeight / 2f),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barHeight / 2f, barHeight / 2f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "TÜM AIDEN HİKAYELERİ",
                color = Color(0xFFF3E8FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp
            )
        }

        // 🎲 Rastgele (Random Story Preview Trigger)
        Row(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .neonOuterGlow(
                    cornerRadius = 14.dp,
                    glowColor = Color(0xFFA855F7),
                    secondaryColor = Color(0xFFF5D0FE),
                    blurRadius = 6.dp,
                    glowAlpha = 0.52f * glowPulse,
                    flowPhase = ambientPhase
                )
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xCC3B126E),
                            Color(0xCC581C87)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFF5D0FE).copy(alpha = 0.85f),
                            Color(0xFFC084FC).copy(alpha = 0.75f)
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
                .clickable(
                    interactionSource = rngInteraction,
                    indication = null,
                    onClick = onRandomStoryClick
                )
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .testTag("aiden_random_story_button"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎲 Rastgele",
                color = Color(0xFFF3E8FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp,
                maxLines = 1,
                softWrap = false,
                style = CenteredButtonTextStyle
            )
        }
    }
}

/**
 * BÖLÜM 3.4 & BÖLÜM 1.1: Story Row Card (`AidenWideStoryRowCard`)
 * - Minimum height `min-h-[112px]` (`heightIn(min = 114.dp)`) with generous padding (`p-3`) so
 *   the 2-line description, the custom `"✦ Daha Fazla Göster ↗"` capsule, and the thematic glowing
 *   category badges (`FlowRow` with `gap-1.5 mt-1.5`) breathe comfortably and are NEVER clipped!
 * - Displays `"● Devam Ediyor"` badge and `"▶ Devam Et"` button when the story has been started.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AidenWideStoryRowCard(
    spec: AidenCatalogRowSpec,
    cardIndex: Int,
    isLastCard: Boolean,
    isStarted: Boolean,
    ambientPhase: Float,
    glowPulse: Float,
    onClick: () -> Unit,
    onShowMoreClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "aiden_row_scale_${spec.presetId}"
    )
    val playNudge by animateDpAsState(
        targetValue = if (isPressed) 2.5.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "aiden_row_nudge_${spec.presetId}"
    )

    val cardShape = RoundedCornerShape(18.dp)
    val thumbShape = RoundedCornerShape(
        topStart = 13.dp,
        bottomStart = 13.dp,
        topEnd = 10.dp,
        bottomEnd = 10.dp
    )

    val cardFlowPhase = (ambientPhase + cardIndex * 0.17f) % 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 114.dp)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .neonOuterGlow(
                cornerRadius = 18.dp,
                glowColor = Color(0xFF8B3DFF),
                secondaryColor = Color(0xFFF5D0FE),
                blurRadius = if (isLastCard) 13.dp else 9.5.dp,
                glowAlpha = (if (isLastCard) 0.72f else 0.50f) * glowPulse,
                flowPhase = cardFlowPhase,
                hotspotFlares = true,
                bottomLightBar = isLastCard
            )
            .clip(cardShape)
            // Contrast armor (`bg-[#120824]/85`) + Translucent Frosted Glass Body
            .background(
                Brush.horizontalGradient(
                    0.00f to Color(0xCC180832),
                    0.38f to Color(0xD8120824),
                    0.72f to Color(0xD815082B),
                    1.00f to Color(0xCC240C46)
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF).copy(alpha = 0.90f),
                        Color(0xFFD8B4FE).copy(alpha = 0.80f),
                        Color(0xFFA855F7).copy(alpha = 0.76f),
                        Color(0xFFF5D0FE).copy(alpha = if (isLastCard) 0.96f else 0.90f)
                    )
                ),
                shape = cardShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("aiden_story_row_${spec.presetId}")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cr = 18.dp.toPx()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        spec.accentTint.copy(alpha = 0.18f * glowPulse),
                        Color(0xFF9333EA).copy(alpha = 0.09f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.18f, h * 0.50f),
                    radius = h * 1.15f
                ),
                center = Offset(w * 0.18f, h * 0.50f),
                radius = h * 1.15f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFA855F7).copy(alpha = 0.28f * glowPulse),
                        Color(0xFF6B21A8).copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.88f, h * 0.50f),
                    radius = h * 1.05f
                ),
                center = Offset(w * 0.88f, h * 0.50f),
                radius = h * 1.05f
            )

            val sheenX = w * (0.28f + 0.15f * sin((cardFlowPhase * 2f * PI).toFloat()))
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFF5D0FE).copy(alpha = 0.09f),
                        Color.White.copy(alpha = 0.12f),
                        Color(0xFFD8B4FE).copy(alpha = 0.07f),
                        Color.Transparent
                    ),
                    start = Offset(sheenX, 0f),
                    end = Offset(sheenX + w * 0.32f, h)
                )
            )

            drawRect(
                brush = Brush.verticalGradient(
                    0.00f to Color(0xFFE9D5FF).copy(alpha = 0.20f),
                    0.24f to Color.Transparent,
                    0.76f to Color.Transparent,
                    1.00f to Color(0xFFA855F7).copy(alpha = 0.18f)
                )
            )

            val inset = 1.5.dp.toPx()
            drawRoundRect(
                color = Color(0xFFC084FC).copy(alpha = (0.28f * glowPulse).coerceIn(0f, 0.42f)),
                topLeft = Offset(inset, inset),
                size = Size(w - inset * 2f, h - inset * 2f),
                cornerRadius = CornerRadius(cr, cr),
                style = Stroke(width = 2.0.dp.toPx())
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 9.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(104.dp)
                    .height(86.dp)
                    .clip(thumbShape)
                    .border(
                        width = 0.8.dp,
                        color = spec.accentTint.copy(alpha = 0.45f),
                        shape = thumbShape
                    )
            ) {
                AidenCoilCoverImage(
                    drawableResId = spec.thumbDrawableRes,
                    contentDescription = spec.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = spec.title,
                    color = Color.White,
                    fontSize = 13.2.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.05.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.5.dp))

                // 2-line clean description (without raw "..." truncation)
                Text(
                    text = spec.description,
                    color = Color(0xFFE5D8F2).copy(alpha = 0.95f),
                    fontSize = 10.3.sp,
                    lineHeight = 13.2.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.5.dp))

                // Custom "✦ Daha Fazla Göster ↗" pill trigger
                AidenShowMorePillButton(
                    glowPulse = glowPulse,
                    onClick = onShowMoreClick,
                    testTag = "aiden_row_show_more_${spec.presetId}"
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Section 3.4: Thematic glowing category badges + "● Devam Ediyor" badge
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    spec.badges.forEach { badge ->
                        AidenThematicCategoryBadge(
                            badge = badge,
                            glowPulse = glowPulse
                        )
                    }
                    if (isStarted) {
                        AidenInProgressBadge(glowPulse = glowPulse)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            AidenReferenceKesfetButton(
                label = if (isStarted) "Devam Et" else "Keşfet",
                ambientPhase = cardFlowPhase,
                glowPulse = glowPulse,
                playNudge = playNudge,
                onClick = onClick
            )
        }
    }
}

/**
 * BÖLÜM 3.4: Thematic Glowing Category Badge
 * (🏆 Spor amber/gold, 👁 Psikolojik & Gerilim cyber blue, 🔴 Karanlık Güç crimson, 🌲 Gerilim emerald, ⚡ Kozmik purple)
 */
@Composable
private fun AidenThematicCategoryBadge(
    badge: AidenBadgeSpec,
    glowPulse: Float
) {
    val pillShape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .height(20.dp)
            .drawBehind {
                val r = size.height / 2f
                drawRoundRect(
                    color = badge.badgeGlowColor.copy(alpha = (0.18f * glowPulse).coerceIn(0f, 0.3f)),
                    topLeft = Offset(-1.5.dp.toPx(), -1.5.dp.toPx()),
                    size = Size(size.width + 3.dp.toPx(), size.height + 3.dp.toPx()),
                    cornerRadius = CornerRadius(r + 1.5.dp.toPx(), r + 1.5.dp.toPx())
                )
            }
            .clip(pillShape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x991C0A35),
                        badge.badgeGlowColor.copy(alpha = 0.22f)
                    )
                )
            )
            .border(
                width = 0.85.dp,
                color = badge.badgeGlowColor.copy(alpha = 0.68f * glowPulse),
                shape = pillShape
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AidenMiniBadgeIcon(
            iconType = badge.iconType,
            modifier = Modifier.size(10.5.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = badge.label,
            color = Color(0xFFF3E8FF).copy(alpha = 0.96f),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            style = CenteredButtonTextStyle
        )
    }
}

@Composable
private fun AidenReferenceKesfetButton(
    label: String = "Keşfet",
    ambientPhase: Float,
    glowPulse: Float,
    playNudge: Dp,
    onClick: () -> Unit = {}
) {
    val pillShape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .widthIn(min = 78.dp)
            .height(32.dp)
            .neonOuterGlow(
                cornerRadius = 16.dp,
                glowColor = Color(0xFF8B3DFF),
                secondaryColor = Color(0xFFE9D5FF),
                blurRadius = 7.dp,
                glowAlpha = 0.56f * glowPulse,
                flowPhase = ambientPhase,
                buttonLeftHotspot = true
            )
            .clip(pillShape)
            .background(
                Brush.verticalGradient(
                    0.00f to Color(0xCC4B1D8C),
                    0.25f to Color(0xBB2A0D54),
                    0.50f to Color(0xB51F0740),
                    0.78f to Color(0xBB2C0E58),
                    1.00f to Color(0xCC4E1F91)
                )
            )
            .border(
                width = 1.15.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF5D0FE).copy(alpha = 0.94f),
                        Color(0xFFC084FC).copy(alpha = 0.80f),
                        Color(0xFFE9D5FF).copy(alpha = 0.94f)
                    )
                ),
                shape = pillShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val r = size.height / 2f
            val inset = 1.2.dp.toPx()
            drawRoundRect(
                color = Color(0xFFA855F7).copy(alpha = (0.40f * glowPulse).coerceIn(0f, 0.55f)),
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2f, size.height - inset * 2f),
                cornerRadius = CornerRadius(r, r),
                style = Stroke(width = 1.8.dp.toPx())
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(9.5.dp)
                    .offset(x = playNudge)
            ) {
                val playPath = Path().apply {
                    moveTo(size.width * 0.14f, size.height * 0.08f)
                    lineTo(size.width * 0.94f, size.height * 0.50f)
                    lineTo(size.width * 0.14f, size.height * 0.92f)
                    close()
                }
                drawPath(
                    path = playPath,
                    color = Color.White,
                    style = Stroke(
                        width = 1.6.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                drawPath(
                    path = playPath,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = label,
                color = Color.White,
                fontSize = 11.0.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.1.sp,
                maxLines = 1,
                softWrap = false,
                style = CenteredButtonTextStyle
            )
        }
    }
}

/**
 * BÖLÜM 1.2: Non-Fullscreen Floating Compact Story Preview Popover (`AidenFloatingStoryPreviewPopoverHost`)
 * - Dimensions: `w-[88%] max-w-[350px] max-h-[62vh] rounded-[28px]`
 * - Entrance/Exit Animation: `scale(0.90 -> 1.0)` + `opacity(0 -> 1)`, `250ms cubic-bezier(0.22, 1, 0.36, 1)`
 * - Backdrop: `bg-black/55` translucent overlay where the Aiden Blackwood Stories menu remains visible underneath
 *   and its scroll position is 100% preserved!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AidenFloatingStoryPreviewPopoverHost(
    previewSpec: AidenCatalogRowSpec?,
    isStarted: Boolean,
    ambientPhase: Float,
    glowPulse: Float,
    onDismissPopover: () -> Unit,
    onStartStoryFromPopover: (AidenCatalogRowSpec) -> Unit
) {
    var activeSpec by remember { mutableStateOf<AidenCatalogRowSpec?>(null) }
    if (previewSpec != null) {
        activeSpec = previewSpec
    }

    val isVisible = previewSpec != null
    val popoverProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 250, easing = PopoverCubicBezier),
        finishedListener = { finalVal ->
            if (finalVal == 0f && !isVisible) {
                activeSpec = null
            }
        },
        label = "floating_popover_progress"
    )

    val currentSpec = previewSpec ?: activeSpec
    if (currentSpec == null && popoverProgress <= 0.001f) {
        return
    }
    val spec = currentSpec ?: return

    val configuration = LocalConfiguration.current
    val maxPopoverHeight = (configuration.screenHeightDp * 0.62f).dp.coerceAtLeast(340.dp)
    val maxScrollTextHeight = (configuration.screenHeightDp * 0.32f).dp.coerceAtLeast(165.dp)

    val backdropInteraction = remember { MutableInteractionSource() }
    val cardInteraction = remember { MutableInteractionSource() }
    val scrollState = rememberScrollState()

    // Reset scroll position when switching to a different story in the popover
    LaunchedEffect(spec.presetId) {
        scrollState.scrollTo(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = popoverProgress
            }
            // Semi-transparent backdrop (`bg-black/55` + soft atmospheric violet haze)
            .background(Color(0x8C05020A))
            .clickable(
                interactionSource = backdropInteraction,
                indication = null,
                onClick = onDismissPopover
            )
            .testTag("aiden_story_preview_backdrop"),
        contentAlignment = Alignment.Center
    ) {
        val popoverShape = RoundedCornerShape(28.dp)
        val scaleVal = 0.90f + 0.10f * popoverProgress

        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .widthIn(max = 350.dp)
                .heightIn(max = maxPopoverHeight)
                .graphicsLayer {
                    scaleX = scaleVal
                    scaleY = scaleVal
                    alpha = popoverProgress
                }
                .neonOuterGlow(
                    cornerRadius = 28.dp,
                    glowColor = spec.reactiveTheme.primaryOrb,
                    secondaryColor = Color(0xFFF5D0FE),
                    blurRadius = 18.dp,
                    glowAlpha = 0.78f * glowPulse,
                    flowPhase = ambientPhase,
                    hotspotFlares = true,
                    borderBeam = true
                )
                .clip(popoverShape)
                .background(
                    Brush.verticalGradient(
                        0.00f to Color(0xF21E0B3B),
                        0.45f to Color(0xF5140729),
                        1.00f to Color(0xF51B0936)
                    )
                )
                .border(
                    width = 1.4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF).copy(alpha = 0.92f),
                            Color(0xFFF5D0FE).copy(alpha = 0.86f),
                            spec.accentTint.copy(alpha = 0.82f),
                            Color(0xFFC084FC).copy(alpha = 0.88f)
                        )
                    ),
                    shape = popoverShape
                )
                // Consume clicks inside the popover so tapping inside NEVER closes it
                .clickable(
                    interactionSource = cardInteraction,
                    indication = null,
                    onClick = {}
                )
                .testTag("aiden_story_preview_popover")
        ) {
            // Subtle internal frosted-glass refraction & thematic aura inside the popover
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            spec.reactiveTheme.primaryOrb.copy(alpha = 0.24f * glowPulse),
                            Color(0xFF7E22CE).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.22f, h * 0.16f),
                        radius = w * 0.55f
                    ),
                    center = Offset(w * 0.22f, h * 0.16f),
                    radius = w * 0.55f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            spec.reactiveTheme.secondaryOrb.copy(alpha = 0.20f * glowPulse),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.82f, h * 0.85f),
                        radius = w * 0.48f
                    ),
                    center = Offset(w * 0.82f, h * 0.85f),
                    radius = w * 0.48f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 15.dp)
            ) {
                // =========================================================================
                // 1. TOP HEADER & COVER IMAGE AREA (w-14 h-14 rounded-2xl + Full Uncut Title + Badges + Mini ✕)
                // =========================================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.1.dp,
                                color = Color(0xFFC084FC).copy(alpha = 0.58f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        AidenCoilCoverImage(
                            drawableResId = spec.thumbDrawableRes,
                            contentDescription = spec.fullTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(11.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = spec.fullTitle,
                            color = Color.White,
                            fontSize = 14.5.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            spec.badges.forEach { badge ->
                                AidenThematicCategoryBadge(
                                    badge = badge,
                                    glowPulse = glowPulse
                                )
                            }
                            if (isStarted) {
                                AidenInProgressBadge(glowPulse = glowPulse)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Mini circular ✕ close button in top-right corner
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0x662E1065))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFD8B4FE).copy(alpha = 0.60f),
                                shape = CircleShape
                            )
                            .clickable(onClick = onDismissPopover)
                            .testTag("aiden_popover_close_icon_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFFF3E8FF),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(11.dp))

                // Subtle glowing divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFC084FC).copy(alpha = 0.55f),
                                    spec.accentTint.copy(alpha = 0.65f),
                                    Color(0xFFC084FC).copy(alpha = 0.55f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // =========================================================================
                // 2. SCROLLABLE FULL TEXT AREA (max-h-[32vh], 13.5sp, leading-relaxed, font-serif)
                //    Contains 100% uncut story description, universe rules, and opening scenario!
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxScrollTextHeight)
                        .padding(vertical = 10.dp)
                        .verticalScroll(scrollState)
                        .padding(end = 4.dp)
                        .testTag("aiden_popover_scroll_content")
                ) {
                    Text(
                        text = "✦ HİKAYE ÖZETİ & EVREN DETAYI",
                        color = Color(0xFFE879F9),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = spec.fullDescription,
                        color = Color(0xFFF5F3FF).copy(alpha = 0.96f),
                        fontSize = 13.5.sp,
                        lineHeight = 20.2.sp,
                        fontFamily = FontFamily.Serif
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "🌌 EVREN KURALLARI & KARAKTER DİNAMİKLERİ",
                        color = Color(0xFFC084FC),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = spec.universeRulesSummary,
                        color = Color(0xFFEDE9FE).copy(alpha = 0.95f),
                        fontSize = 13.0.sp,
                        lineHeight = 19.5.sp,
                        fontFamily = FontFamily.Serif
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "🎬 BAŞLANGIÇ SENARYOSU (İLK SAHNE)",
                        color = Color(0xFFFBBF24),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x55120524))
                            .border(
                                width = 0.8.dp,
                                color = Color(0xFF9333EA).copy(alpha = 0.45f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = spec.openingScenePreview,
                            color = Color(0xFFF3E8FF).copy(alpha = 0.94f),
                            fontSize = 12.8.sp,
                            lineHeight = 19.0.sp,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                // Subtle bottom divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFC084FC).copy(alpha = 0.45f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(11.dp))

                // =========================================================================
                // 3. BOTTOM ACTION BAR ("⚡ Bu Hikayeyi Başlat →" + Compact "Kapat" Button)
                // =========================================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val startInteraction = remember { MutableInteractionSource() }
                    val startPressed by startInteraction.collectIsPressedAsState()
                    val startScale by animateFloatAsState(
                        targetValue = if (startPressed) 0.96f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                        label = "popover_start_scale"
                    )

                    // Primary "⚡ Bu Hikayeyi Başlat →" button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .graphicsLayer {
                                scaleX = startScale
                                scaleY = startScale
                            }
                            .neonOuterGlow(
                                cornerRadius = 21.dp,
                                glowColor = Color(0xFF9333EA),
                                secondaryColor = Color(0xFFF5D0FE),
                                blurRadius = 10.dp,
                                glowAlpha = 0.72f * glowPulse,
                                flowPhase = ambientPhase,
                                buttonLeftHotspot = true
                            )
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF7E22CE),
                                        Color(0xFFA855F7),
                                        Color(0xFF6B21A8)
                                    )
                                )
                            )
                            .border(
                                width = 1.2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color(0xFFF5D0FE),
                                        Color(0xFFD8B4FE),
                                        Color.White
                                    )
                                ),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable(
                                interactionSource = startInteraction,
                                indication = null,
                                onClick = { onStartStoryFromPopover(spec) }
                            )
                            .padding(horizontal = 12.dp)
                            .testTag("aiden_popover_start_story_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AidenMiniBadgeIcon(
                            iconType = AidenBadgeIconType.LIGHTNING,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isStarted) "Hikayeye Devam Et →" else "Bu Hikayeyi Başlat →",
                            color = Color.White,
                            fontSize = 12.2.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            style = CenteredButtonTextStyle
                        )
                    }

                    // Compact "Kapat" button
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x66280E4E))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFC084FC).copy(alpha = 0.55f),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable(onClick = onDismissPopover)
                            .padding(horizontal = 14.dp)
                            .testTag("aiden_popover_close_text_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Kapat",
                            color = Color(0xFFE9D5FF),
                            fontSize = 11.8.sp,
                            fontWeight = FontWeight.SemiBold,
                            style = CenteredButtonTextStyle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AidenMiniBadgeIcon(
    iconType: AidenBadgeIconType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (iconType) {
            AidenBadgeIconType.TROPHY -> {
                val goldBrush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFD97706))
                )
                val cupPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.14f)
                    lineTo(w * 0.78f, h * 0.14f)
                    lineTo(w * 0.70f, h * 0.52f)
                    quadraticTo(cx, h * 0.68f, w * 0.30f, h * 0.52f)
                    close()
                }
                drawPath(path = cupPath, brush = goldBrush)
                drawLine(
                    color = Color(0xFFFBBF24),
                    start = Offset(w * 0.24f, h * 0.22f),
                    end = Offset(w * 0.12f, h * 0.36f),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFFFBBF24),
                    start = Offset(w * 0.76f, h * 0.22f),
                    end = Offset(w * 0.88f, h * 0.36f),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(w * 0.44f, h * 0.58f),
                    size = Size(w * 0.12f, h * 0.22f)
                )
                drawRoundRect(
                    color = Color(0xFFFBBF24),
                    topLeft = Offset(w * 0.26f, h * 0.78f),
                    size = Size(w * 0.48f, h * 0.12f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }

            AidenBadgeIconType.STAR -> {
                val starPath = Path().apply {
                    val outerR = min(w, h) * 0.48f
                    val innerR = outerR * 0.42f
                    for (i in 0 until 10) {
                        val angle = (-PI / 2.0) + i * (PI / 5.0)
                        val r = if (i % 2 == 0) outerR else innerR
                        val px = cx + (r * cos(angle)).toFloat()
                        val py = cy + (r * sin(angle)).toFloat()
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }
                    close()
                }
                drawPath(
                    path = starPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B))
                    )
                )
            }

            AidenBadgeIconType.EYE -> {
                val eyePath = Path().apply {
                    moveTo(w * 0.06f, cy)
                    quadraticTo(cx, h * 0.14f, w * 0.94f, cy)
                    quadraticTo(cx, h * 0.86f, w * 0.06f, cy)
                    close()
                }
                drawPath(
                    path = eyePath,
                    color = Color(0xFF7DD3FC)
                )
                drawCircle(
                    color = Color(0xFF0C4A6E),
                    radius = min(w, h) * 0.22f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White,
                    radius = min(w, h) * 0.08f,
                    center = Offset(cx - w * 0.05f, cy - h * 0.05f)
                )
            }

            AidenBadgeIconType.RED_ORB -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFCA5A5),
                            Color(0xFFEF4444),
                            Color(0xFFB91C1C)
                        ),
                        center = Offset(cx - w * 0.12f, cy - h * 0.12f),
                        radius = min(w, h) * 0.55f
                    ),
                    radius = min(w, h) * 0.44f,
                    center = Offset(cx, cy)
                )
            }

            AidenBadgeIconType.PINE_TREE -> {
                val treeBrush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                )
                val tier1 = Path().apply {
                    moveTo(cx, h * 0.06f)
                    lineTo(w * 0.78f, h * 0.42f)
                    lineTo(w * 0.22f, h * 0.42f)
                    close()
                }
                val tier2 = Path().apply {
                    moveTo(cx, h * 0.28f)
                    lineTo(w * 0.88f, h * 0.74f)
                    lineTo(w * 0.12f, h * 0.74f)
                    close()
                }
                drawPath(path = tier1, brush = treeBrush)
                drawPath(path = tier2, brush = treeBrush)
                drawRect(
                    color = Color(0xFFA16207),
                    topLeft = Offset(w * 0.42f, h * 0.74f),
                    size = Size(w * 0.16f, h * 0.20f)
                )
            }

            AidenBadgeIconType.LIGHTNING -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFBBF24).copy(alpha = 0.38f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = min(w, h) * 0.56f
                    ),
                    center = Offset(cx, cy),
                    radius = min(w, h) * 0.56f
                )

                val leftFacet = Path().apply {
                    moveTo(w * 0.68f, h * 0.04f)
                    lineTo(w * 0.16f, h * 0.56f)
                    lineTo(w * 0.48f, h * 0.56f)
                    lineTo(w * 0.28f, h * 0.96f)
                    lineTo(w * 0.52f, h * 0.48f)
                    close()
                }
                drawPath(
                    path = leftFacet,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF799),
                            Color(0xFFFDE047),
                            Color(0xFFFBBF24)
                        ),
                        start = Offset(w * 0.20f, h * 0.05f),
                        end = Offset(w * 0.55f, h * 0.90f)
                    )
                )

                val rightFacet = Path().apply {
                    moveTo(w * 0.68f, h * 0.04f)
                    lineTo(w * 0.52f, h * 0.44f)
                    lineTo(w * 0.84f, h * 0.44f)
                    lineTo(w * 0.28f, h * 0.96f)
                    lineTo(w * 0.48f, h * 0.52f)
                    close()
                }
                drawPath(
                    path = rightFacet,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFACC15),
                            Color(0xFFF59E0B),
                            Color(0xFFD97706)
                        ),
                        start = Offset(w * 0.50f, h * 0.20f),
                        end = Offset(w * 0.80f, h * 0.95f)
                    )
                )
            }
        }
    }
}
