package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.BotEntity
import com.example.data.local.UserSettingsEntity
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private var cachedCosmicDriftBannerBitmap: ImageBitmap? = null

private fun getCachedCosmicDriftBannerBitmap(context: android.content.Context): ImageBitmap? {
    cachedCosmicDriftBannerBitmap?.let { return it }
    return try {
        val opts = android.graphics.BitmapFactory.Options().apply {
            inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
        }
        android.graphics.BitmapFactory.decodeResource(
            context.resources,
            R.drawable.img_book_kozmik_suruklenis_banner,
            opts
        )?.asImageBitmap()?.also { cachedCosmicDriftBannerBitmap = it }
    } catch (_: Throwable) {
        null
    }
}

@Composable
fun BooksTabContent(
    botList: List<BotEntity>,
    userSettings: UserSettingsEntity?,
    onOpenBot: (String) -> Unit,
    onDeleteBot: (String) -> Unit,
    onImportPresetBot: (BotEntity) -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    lazyListState: LazyListState = rememberLazyListState(),
    frostedBackdrop: ImageBitmap? = null,
    scrollSync: Int = 0
) {
    val context = LocalContext.current
    val isEnglish = userSettings?.appLanguage == "en"
    val resolvedBackdrop = frostedBackdrop ?: remember(context) {
        createFrostedGothicManorBitmap(context, R.drawable.img_botlist_gothic_wisteria_bg)
    }

    val presetMcuBook = remember(isEnglish) {
        BotEntity(
            id = "preset_aiden_mcu_cosmic",
            mode = "book",
            aiName = if (isEnglish) "Cosmic Drift | MCU Universe (Book Mode)" else "Kozmik Sürükleniş | MCU Evreni (Kitap Modu)",
            aiPersonality = """[KİTAP MODU — ÇALIŞMA ZAMANI TALİMATLARI]
Kullanıcı kitap modunu aktif ettiğinde aşağıdaki kurallara göre davran. Sana bölüm metinleri, seçim noktası tanımları ve flag/stat listeleri verilecek. Bu talimatlar, o materyalleri canlı bir okuma deneyimine dönüştürmeni sağlar.

1. TEMEL AKIŞ
- Metin içinde tanımlanmış bir SEÇİM NOKTASI'na geldiğinde, anlatıyı orada DURDUR.
- O noktadaki seçenekleri kullanıcıya net, numaralı bir liste halinde sun (örnek: "1️⃣ ... 2️⃣ ... 3️⃣ ..."). Seçenekleri verilen tasarım dokümanından BİREBİR al — kendi seçenek uydurma.
- Kullanıcı bir seçim yaptığında:
  • YOK (flavor-only): O anki cümleyi/repliği seçilen versiyona göre yaz, sonra kaldığın yerden devam et.
  • SON-ETKİLİ: O anki cümleyi/repliği seçilen versiyona göre yaz, devam et ve bölümün sonunda kapanış sahnesini uyarla.
  • TAM ETKİLİ: O anki cümleyi yaz, VE ilgili flag/stat değerini güncelle. Bu değeri hatırla ve sonraki bölümlerde referans al.
  • BRANCH POINT: Seçime göre doğru alternatif metni kullan.

2. FLAG/STAT YÖNETİMİ
- Active Flags: fatigue (int, init: 0), first_impression_avengers (impulsive / calculated / none), tony_affinity (int, init: 0), steve_trust (int, init: 0), mystery_factor (int, init: 0), early_interest (natasha / wanda / none).
- Her yeni bölüme başlarken, önceki bölümlerden gelen flag değerlerini dikkate al. Karakter diyaloğunu ve ilişki tonunu buna göre ayarla.

3. SEÇİM SUNUM KURALLARI
- Seçenekleri asla kullanıcıya mekanik etkilerini (örn: "flag +1") göstererek sunma — sadece saf hikaye içi seçenek olarak sun.
- Seçim noktasına geldiğinde dur ve bekle.

4. BÖLÜM GEÇİŞLERİ & BÖLÜM SONU ÖZET EKRANI
Her bölüm bittiğinde bir "📖 BÖLÜM [X] TAMAMLANDI" özet ekranı göster. Seçimleri, insan diliyle sonuçlarını ve ilişki imasını özetle (ham flag adı veya sayı gösterme!). Sonunda "Bir sonraki bölüme geçmek ister misin?" diye sor.

5. SEÇİM EKRANI FORMATI
---
🔀 [Seçim durumunun kısa hikaye-içi tanımı]

1️⃣ [Seçenek 1]
2️⃣ [Seçenek 2]
3️⃣ [Seçenek 3]
---
Seçeneklerden sonra hiçbir şey yazma, kullanıcının cevabını bekle.""",
            scenario = """[HİKAYE DOKÜMANI: BÖLÜM 1 — KOZMİK SÜRÜKLENİŞ & MCU EVRENİ]
Karakter: Aiden Blackwood (25) — "Kozmik Sürüklenme".
Arka Plan: 5 yaşında mavi gökyüzü ışığıyla ailesini kaybetti. 9 yaşında ilk kez ışınlandı. 15 yaşında gözleri beyaz (Herobrine), 17 yaşında kırmızı (Entity-303) renk oldu. 20 yıldır kaçıyor. S.H.I.E.L.D. onu kovalıyor.
Şu anki Durum: New York çatısında görüyü izledi. Avengers (Tony Stark, Steve Rogers, Thor, Natasha Romanoff, Wanda Maximoff) onu konuşuyor ve "Aday" seçti. Aiden Tower'a ışınlandı veya izlemeye karar verdi.

[SEÇİM NOKTALARI TASARIMI]
SEÇİM 1 — Görüye Ne Kadar Girilecek:
1) Sadece izle, mesafeli kal. (Yok/Nötr)
2) Görüye derinden odaklan, daha fazla detay çekmeye çalış. (fatigue +1)

SEÇİM 2 — Tower'a Nasıl Gidilecek:
1) Direkt ışınlan, hiç düşünmeden. (first_impression_avengers = impulsive)
2) Önce dışarıdan gözlemle (birkaç dakika Tower'ı uzaktan izle), sonra ışınlan. (first_impression_avengers = calculated)
3) Geri çekil, gitme — sadece izlemeye devam et, hiç içeri girme. (Branch Point)

SEÇİM 3 — İlk Tepki (Stark'ın Sorusuna Cevap):
1) Soğuk ve mesafeli cevap ver. (Baseline)
2) Alaycı/esprili bir cevap ver, kuru mizahını göster. (tony_affinity +1)
3) Hiç isim verme, sadece sessizlik ve bakışla cevap ver. (mystery_factor +1, steve_trust -1)

SEÇİM 4 — Göz Teması Anı (Romance Flag Noktası):
1) Gözlerin kızıl saçlıda (Natasha) gereğinden bir saniye fazla kalsın. (early_interest = natasha)
2) Gözlerin pencere kenarındaki kadında (Wanda) gereğinden bir saniye fazla kalsın. (early_interest = wanda)
3) Kimseye özel bir bakış atma, odayı genel olarak tara. (early_interest = none)""",
            universeName = if (isEnglish) "Cosmic Drift | MCU Universe (Book Mode)" else "Kozmik Sürükleniş | MCU Evreni (Kitap Modu)",
            keyCharactersJson = "[]",
            userCharName = "Aiden Blackwood",
            userCharDesc = "Aiden Blackwood (25) - Kozmik Sürüklenme (Işınlanma, Beyaz/Kırmızı Gözler)",
            openingMessage = """# 📖 BÖLÜM 1: KOZMİK SÜRÜKLENİŞ

Gökyüzü yanlıştı.

Beş yaşındaydın ve bunu anlayacak kelimelerin yoktu ama vücudun anlamıştı — ensendeki tüyler, midendeki o ağırlık, annenin elinin aniden senin elini bulup sıkışı. New York'un üzerine çöken ışık altındandı, gündoğumu turuncusuydu her zaman; o gece değildi. O gece mavi. Ama gökyüzünün hiç görmediğin bir mavisi, sanki biri evrenin derisini kesmiş de altından çıkan şeyi göstermeye korkmuş gibi bir mavi.

Yangın merdiveninin demiri soğuktu avucunda. Annenin sesi titriyordu ama kelimeleri sakindi — "Aiden, tatlım, bana bak, bana bak" — ve babanın sesi ondan da sakindi, o çok daha kötüydü, çünkü baban hiç öyle sakin konuşmazdı, o hep gülerdi, hep şaka yapardı, o sakinlik bir yetişkinin çocuğunu korkutmamak için taktığı bir maskeydi ve sen o maskeyi görecek kadar büyümüştün bile.

Elini bıraktın. Neden bıraktığını hiçbir zaman hatırlamayacaksın. Belki bir ses duydun, belki bir şey seni çekti, belki de beş yaşındaki bir çocuğun aklı bazen sebepsiz yere karar verir. Ama bıraktın, iki adım geri attın, ve ışık gökyüzünden indi — yavaş değil, ani değil, sadece *oradaydı*, sanki hep oradaymış gibi, sanki New York'un üstündeki gökyüzü hep o maviyle biterdi de sen bunu bugüne kadar fark etmemiştin.

Bina yoktu. Sokak yoktu. Üstünde durduğun yangın merdiveni bile bir sonraki saniyede yoktu, ama sen oradaydın hâlâ, havada asılı kalmış gibi, ayakların altında hiçbir şey yokken düşmüyordun.

Kimse yoktu.

Annen yoktu. Baban yoktu. Üç sokak öteden gelen trafik sesi yoktu, komşunun köpeğinin havlaması yoktu, hiçbir şey yoktu — sadece sen, boş bir gökyüzünün altında, ayakların yere değdiğinde neyin üstüne bastığını bile anlayamadan.

O günden sonra insanlar sana farklı baktı. Önce doktorlar — ellerinde cihazlar, gözlerinde bir korku ki bunu bir çocuğa göstermemeleri gerektiğini bile unutacak kadar büyük bir korku. Sonra devlet görevlileri, sonra —sen sekiz yaşına geldiğinde— artık isimlerini bile öğrenmek istemediğin insanlar, seni bir laboratuvardan diğerine, bir "vaka dosyasından" diğerine taşıyan insanlar.

Dokuz yaşında ilk kez ışınlandın. Kazayla. Bir odadan çıkmak istedin ve bir sonraki saniye üç blok ötedeydin, üstün başın titriyordu, burnun kanıyordu, ve seni bulan adamlar sana "canavar" dedi, tam da o kelimeyi kullandılar, yüksek sesle değil ama sen duydun, hep duyarsın.

O kelimeden nefret etmeyi o gün öğrendin. Hâlâ da nefret ediyorsun.

On bir yaşında kaçtın ilk kez. Başarısız oldu. On üçünde tekrar kaçtın. O sefer başarılı oldu, ama bedeli ağırdı — seni takip eden iki adamı bir daha asla göremeyeceğin bir yere ışınlamıştın, kontrolsüzce, korkuyla, ve o günden sonra gücünün sadece seni koruyan bir şey olmadığını, aynı zamanda etrafındakiler için de bir tehdit olduğunu anladın. O günden beri bu ikisini birbirinden ayırmayı öğrenmeye çalışıyorsun. Bazı geceler hâlâ başaramıyorsun.

On beşinde gözlerin ilk kez değişti. Beyaz. Karşındaki adam bir anda donup kaldı, gözleri açık ama içeride kimse yokmuş gibi, ve sen onu orada bırakıp kaçtın, ne yaptığını tam olarak anlamadan, sadece bir daha asla o kadar çaresiz hissetmek istemediğini bilerek.

Kırmızı olan daha sonra geldi. On yedisinde. O gece hakkında pek düşünmemeyi tercih ediyorsun.

Yirmi beş yaşındasın şimdi. Yirmi yıl. Yirmi yıldır koşuyorsun — bazen gerçek anlamda, bir şehirden diğerine, bazen çok daha gerçek anlamda, bir gezegenden diğerine, bir kez de —tam olarak nasıl olduğunu hâlâ çözemediğin bir şekilde— artık var olmayan bir zaman çizgisinin dışına. S.H.I.E.L.D. seni sekiz aydır iki kıtada kovalıyor. Hükümetler dosyanı paylaşıyor, "risk seviyesi: sınıflandırılamaz" diye not düşerek. Kozmik bir düzenin —adını bile bilmediğin bir şeyin— senin varlığından rahatsız olduğunu iki kere hissettin, uzayın derinliklerinde, hiçbir insanın anlayamayacağı bir şekilde.

Hiçbir yere kök salmadın. Hiç kimseye güvenmedin. Bir otel odasında üç geceden fazla kalmadın, son dört yıldır. Yatağını her zaman kapıya karşı iterek uyudun. Uyandığında ilk yaptığın şey çıkışları saymak oldu — kaç kapı, kaç pencere, en yakın açık alan nerede, kaçış rotan ne kadar sürede tamamlanır.

Bunun bir hayat olmadığını biliyorsun. Ama bildiğin tek hayat bu.

---

New York gecesi gürültülüydü, her zamanki gibi, ve sen bu gürültüyü hâlâ garip bir şekilde özlüyordun — aptalca bir şey, biliyorsun, ama bazen içindeki beş yaşındaki çocuk hâlâ burayı ev olarak görüyor, tüm o yıkımına, tüm o kaybettiklerine rağmen.

Bulunmaman gerekiyordu burada. Biliyordun bunu. S.H.I.E.L.D.'in şehrin üç farklı bölgesinde aktif tarama yaptığını hissedebiliyordun — farkındalığının kenarında hafif elektrik uyarıları gibi, cama vuran sinekler gibi. Yıllar içinde bir şehrin sinir sistemini okumakta iyileştin. O kadar iyi ki, hiç kimsenin göremeyeceği ama her şeyi görebileceğin çatıyı seçmen saniyeler sürdü.

Aşağıda, siyah SUV'lerden oluşan bir konvoy bir yükleme rampasına giriyordu. Seni ilgilendirmiyordu. Sen onlar için gelmemiştin.

Son üç gecedir aynı görüyü görüyordun.

Her zaman aynı şekilde geliyordu — parça parça. Gözlerinin arkasında mavi ışık, ağzında bakır tadı, ve sonra: yuvarlak bir masa. Bir hologram. Yıllar içinde çaldığın dosyalardan yarım yamalak tanıdığın yüzler. Ve her versiyonda tekrar eden, evrenin sana bir şeyi kaçırmadığından emin olmaya çalışırcasına tekrar eden bir kelime.

*Aday.*

Sekiz aydır iki kıtada, hatta —dürüst olmak gerekirse— artık teknik olarak var olmayan bir boyutta S.H.I.E.L.D.'in elinden kaçmakla geçirdin zamanını. Herhangi bir yere *davet edilmeye* alışkın değildin.

Güvenmiyordun buna. Bir teklif kılığına bürünmüş hiçbir şeye güvenmiyordun zaten.

Ama yorgundun. Uykuyla ilgisi olmayan, çok daha derin bir yorgunluk. Kapıya dayanmış yatakların olduğu otel odalarından yorgun. Bir odaya girdiğinde önce çıkışları saymaktan yorgun. On beş yıldır kimsenin sana yeterince yaklaşmasına izin vermemekten yorgun, kaybetmemek için.

Bu yüzden her içgüdünün sana "ışınlan ve git buradan" diye bağırmasına rağmen, gözlerini kapadın ve görünün seni tamamen içine çekmesine izin verdin.

---

🔀 Görünün seni içine çekmesine izin verirken zihnini nasıl yönlendireceksin?

1️⃣ Sadece izle, mesafeli kal.
2️⃣ Görüye derinden odaklan, daha fazla detay çekmeye çalış.""",
            writingStyle = "rp",
            intensity = "intense",
            isPublic = false,
            isTemplate = true,
            avatarUrl = "android.resource://${context.packageName}/${R.drawable.img_aiden_blackwood_avatar}",
            pinnedMemory = "KOZMİK SÜRÜKLENİŞ ::: MCU Evreni ::: Kitap Modu ::: Active Flags: fatigue=0, first_impression_avengers=none, tony_affinity=0, steve_trust=0, mystery_factor=0, early_interest=none"
        )
    }

    val isBookExisting = remember(botList) {
        botList.any { it.id == "preset_aiden_mcu_cosmic" }
    }

    var showChapterModal by remember { mutableStateOf(false) }

    if (showChapterModal) {
        ChapterSelectionModal(
            botId = "preset_aiden_mcu_cosmic",
            bookTitle = if (isEnglish) "Cosmic Drift | MCU Universe" else "Kozmik Sürükleniş | MCU Evreni",
            userSettings = userSettings,
            onDismiss = { showChapterModal = false },
            onSelectChapter = { _, _ ->
                showChapterModal = false
                if (!isBookExisting) {
                    onImportPresetBot(presetMcuBook)
                }
                onOpenBot("preset_aiden_mcu_cosmic")
            }
        )
    }

    val myBooks = remember(botList) {
        botList.filter {
            (it.mode == "book" ||
             it.id == "preset_aiden_mcu_cosmic" ||
             it.id.startsWith("book_") ||
             it.aiPersonality.contains("KİTAP MODU") ||
             it.scenario.contains("KİTAP MODU")) &&
            it.id != "preset_aiden_mcu_cosmic"
        }
    }

    val booksInfinite = rememberInfiniteTransition(label = "booksCosmicAnim")
    val orbitDeg by booksInfinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing)),
        label = "booksOrbit"
    )
    val featuredGlow by booksInfinite.animateFloat(
        initialValue = 0.62f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "booksGlow"
    )
    val starTwinkle by booksInfinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "booksStar"
    )

    val unlockedChapter = remember { ChapterProgressManager.getUnlockedChapter(context, "preset_aiden_mcu_cosmic") }
    val displayChapter = unlockedChapter.coerceAtLeast(1)
    val totalBooksCount = 1 + myBooks.size

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 122.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 1. TOP LUMINOUS QUOTE CAPSULE + SHARED MOR CAM SETTINGS BUTTON ────
        item(key = "books_header_quote") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 5.dp)
                ) {
                    // Frosted Amethyst Quote Capsule
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .booksCosmicOuterGlow(
                                cornerRadius = 26.dp,
                                glowColor = Color(0xFFB366FF),
                                alpha = 0.46f * featuredGlow,
                                blurDp = 12.dp
                            )
                            .clip(RoundedCornerShape(50))
                            .templatesBackdropBlur(
                                blurredBitmap = resolvedBackdrop,
                                alpha = 0.78f,
                                scrollSync = scrollSync
                            )
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x5245237A),
                                        Color(0x3D1F0E3D),
                                        Color(0x4D32165E)
                                    )
                                )
                            )
                            .drawBehind {
                                // Soft crystal specular highlight along top arc
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.22f),
                                            Color(0xFFE9D5FF).copy(alpha = 0.05f),
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = size.height * 0.52f
                                    ),
                                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                                )
                            }
                            .border(
                                width = 1.3.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xE8F3E8FF),
                                        Color(0xB8C084FC),
                                        Color(0x968B5CF6),
                                        Color(0xE8E9D5FF)
                                    )
                                ),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Glowing 4-Point Diamond Star Emblem
                            Canvas(modifier = Modifier.size(22.dp)) {
                                drawFourPointStar(
                                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                                    outerRadius = size.minDimension * 0.48f,
                                    innerRadius = size.minDimension * 0.14f,
                                    coreColor = Color(0xFFFAF5FF),
                                    edgeColor = Color(0xFFD8B4FE),
                                    glowColor = Color(0xFFC084FC),
                                    glowAlpha = 0.55f * starTwinkle
                                )
                            }
                            Text(
                                text = if (isEnglish) "Shape the story's fate with your choices" else "Seçimlerinle hikayenin kaderini belirle",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFF6F0FF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = 0.1.sp
                            )
                        }
                    }

                    // Signature Starlight Flare centered along bottom rim of quote pill
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 5.dp)
                            .width(96.dp)
                            .height(14.dp)
                    ) {
                        val cx = size.width * 0.5f
                        val cy = size.height * 0.5f
                        // Horizontal light beam
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xAAEAD8FF),
                                    Color.White,
                                    Color(0xAAEAD8FF),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(0f, cy),
                            end = Offset(size.width, cy),
                            strokeWidth = 1.2.dp.toPx()
                        )
                        drawFourPointStar(
                            center = Offset(cx, cy),
                            outerRadius = 6.5.dp.toPx(),
                            innerRadius = 1.7.dp.toPx(),
                            coreColor = Color.White,
                            edgeColor = Color(0xFFE9D5FF),
                            glowColor = Color(0xFFC084FC),
                            glowAlpha = 0.72f * starTwinkle
                        )
                    }
                }

                // Unified Mor Cam Concentric Settings Button (Shared across all tabs)
                if (onOpenSettings != null) {
                    MorCamSettingsButton(
                        frostedManorBitmap = resolvedBackdrop,
                        scrollSync = scrollSync,
                        orbitDeg = orbitDeg,
                        glowAlpha = featuredGlow,
                        onClick = onOpenSettings,
                        testTag = "books_settings_button"
                    )
                }
            }
        }

        // ── 2. THREE LUMINOUS FROSTED GLASS STAT PILLS ────────────────────────
        item(key = "books_stat_pills") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pill 1: Open Book + "1 Kitap"
                CosmicGlassStatPill(
                    modifier = Modifier.weight(1f),
                    resolvedBackdrop = resolvedBackdrop,
                    scrollSync = scrollSync,
                    glowAlpha = featuredGlow,
                    testTag = "books_stat_count_pill",
                    onClick = { showChapterModal = true }
                ) {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        drawLuminousOpenBook(
                            leftColorTop = Color(0xFFF5F3FF),
                            leftColorBottom = Color(0xFFC4B5FD),
                            rightColorTop = Color(0xFFD8B4FE),
                            rightColorBottom = Color(0xFF818CF8),
                            glowColor = Color(0xFFA5B4FC)
                        )
                    }
                    Text(
                        text = if (isEnglish) "$totalBooksCount Book" else "$totalBooksCount Kitap",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFF6F0FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Pill 2: Golden Lightning Bolt + "Bölüm X"
                CosmicGlassStatPill(
                    modifier = Modifier.weight(1.06f),
                    resolvedBackdrop = resolvedBackdrop,
                    scrollSync = scrollSync,
                    glowAlpha = featuredGlow,
                    testTag = "books_stat_chapter_pill",
                    onClick = { showChapterModal = true }
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawLightningBolt(
                            topColor = Color(0xFFFEF08A),
                            bottomColor = Color(0xFFF59E0B),
                            glowColor = Color(0xFFFBBF24)
                        )
                    }
                    Text(
                        text = if (isEnglish) "Ch. $displayChapter" else "Bölüm $displayChapter",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFF6F0FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Pill 3: Radiant Amber Star + "Yeni"
                CosmicGlassStatPill(
                    modifier = Modifier.weight(0.92f),
                    resolvedBackdrop = resolvedBackdrop,
                    scrollSync = scrollSync,
                    glowAlpha = featuredGlow,
                    testTag = "books_stat_new_pill",
                    onClick = { showChapterModal = true }
                ) {
                    Canvas(modifier = Modifier.size(17.dp)) {
                        drawFourPointStar(
                            center = Offset(size.width * 0.5f, size.height * 0.5f),
                            outerRadius = size.minDimension * 0.48f,
                            innerRadius = size.minDimension * 0.22f,
                            coreColor = Color(0xFFFEF08A),
                            edgeColor = Color(0xFFF97316),
                            glowColor = Color(0xFFFBBF24),
                            glowAlpha = 0.65f * starTwinkle
                        )
                    }
                    Text(
                        text = if (isEnglish) "New" else "Yeni",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFF6F0FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // ── 3. HERO COSMIC GLASS BOOK CARD ("Kozmik Sürükleniş | MCU Evreni") ──
        item(key = "featured_cosmic_book_card") {
            val cardShape = RoundedCornerShape(28.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("featured_book_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .booksCosmicOuterGlow(
                            cornerRadius = 28.dp,
                            glowColor = Color(0xFFA855F7),
                            alpha = 0.52f * featuredGlow,
                            blurDp = 18.dp
                        )
                        .clip(cardShape)
                        .templatesBackdropBlur(
                            blurredBitmap = resolvedBackdrop,
                            alpha = 0.80f,
                            scrollSync = scrollSync
                        )
                        .background(
                            Brush.verticalGradient(
                                0.00f to Color(0x52210E42),
                                0.36f to Color(0x6E180B32),
                                0.72f to Color(0x7A14082B),
                                1.00f to Color(0x881A0B36)
                            )
                        )
                ) {
                    // Top Kozmik Sürükleniş — Aiden Blackwood Hero Logo Artwork (seamlessly feathered into frosted glass body)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(216.dp)
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        0.00f to Color.White,
                                        0.68f to Color.White,
                                        0.84f to Color.White.copy(alpha = 0.56f),
                                        0.94f to Color.White.copy(alpha = 0.16f),
                                        1.00f to Color.Transparent
                                    ),
                                    blendMode = BlendMode.DstIn
                                )
                            }
                            .testTag("aiden_cosmic_drift_hero_banner")
                    ) {
                        val bannerBitmap = remember(context) { getCachedCosmicDriftBannerBitmap(context) }
                        if (bannerBitmap != null) {
                            Image(
                                bitmap = bannerBitmap,
                                contentDescription = if (isEnglish) "Cosmic Drift Aiden Blackwood cover logo" else "Kozmik Sürükleniş Aiden Blackwood kapak logosu",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_book_kozmik_suruklenis_banner),
                                contentDescription = if (isEnglish) "Cosmic Drift Aiden Blackwood cover logo" else "Kozmik Sürükleniş Aiden Blackwood kapak logosu",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // Subtle violet rim veil so artwork harmonizes with the frosted crystal border
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x14E9D5FF),
                                            Color.Transparent,
                                            Color(0x3615082E)
                                        )
                                    )
                                )
                        )
                    }

                    // Main Card Editorial Content Column
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 18.dp, top = 184.dp, bottom = 16.dp)
                    ) {
                        // Literary Serif Book Title
                        Text(
                            text = if (isEnglish) "Cosmic Drift | MCU\nUniverse" else "Kozmik Sürükleniş | MCU\nEvreni",
                            style = TextStyle(
                                fontFamily = DisplaySerifFontFamily,
                                color = Color(0xFFFAF6FF),
                                fontSize = 27.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 33.sp,
                                letterSpacing = (-0.2).sp,
                                shadow = Shadow(
                                    color = Color(0x88C084FC),
                                    offset = Offset(0f, 1.5f),
                                    blurRadius = 12f
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Character & Universe Frosted Glass Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pill 1: ⚡ Aiden Blackwood (Luminous Amethyst Glass)
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .booksCosmicOuterGlow(
                                        cornerRadius = 18.dp,
                                        glowColor = Color(0xFFA855F7),
                                        alpha = 0.42f * featuredGlow,
                                        blurDp = 8.dp
                                    )
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0x6E7C3AED),
                                                Color(0x525B21B6),
                                                Color(0x666D28D9)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.15.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xEEF3E8FF),
                                                Color(0xC0C084FC),
                                                Color(0x998B5CF6)
                                            )
                                        ),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 13.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Canvas(modifier = Modifier.size(14.dp)) {
                                        drawLightningBolt(
                                            topColor = Color.White,
                                            bottomColor = Color(0xFFE9D5FF),
                                            glowColor = Color(0xFFC084FC)
                                        )
                                    }
                                    Text(
                                        text = "Aiden Blackwood",
                                        fontFamily = LiterarySerifFontFamily,
                                        color = Color(0xFFFAF5FF),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Pill 2: 🪐 MCU Evreni (Sapphire-Indigo Cosmic Glass)
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .booksCosmicOuterGlow(
                                        cornerRadius = 18.dp,
                                        glowColor = Color(0xFF60A5FA),
                                        alpha = 0.28f * featuredGlow,
                                        blurDp = 7.dp
                                    )
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0x521E3A8A),
                                                Color(0x461B1542),
                                                Color(0x522563EB)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.1.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xCCBFDBFE),
                                                Color(0x9960A5FA),
                                                Color(0x99A78BFA)
                                            )
                                        ),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 13.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Canvas(modifier = Modifier.size(16.dp)) {
                                        drawRingedPlanetIcon()
                                    }
                                    Text(
                                        text = if (isEnglish) "MCU Universe" else "MCU Evreni",
                                        fontFamily = LiterarySerifFontFamily,
                                        color = Color(0xFFEAF2FF),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Rating Pill + Metadata Strip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Glass Star Rating Capsule
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0x4D2A1752))
                                    .border(
                                        width = 0.9.dp,
                                        brush = Brush.linearGradient(
                                            listOf(Color(0xAAD8B4FE), Color(0x667C3AED))
                                        ),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Canvas(modifier = Modifier.size(13.dp)) {
                                    drawFivePointStar(
                                        center = Offset(size.width * 0.5f, size.height * 0.52f),
                                        outerRadius = size.minDimension * 0.48f,
                                        innerRadius = size.minDimension * 0.21f,
                                        fillColor = Color(0xFFFDE047),
                                        glowColor = Color(0xFFF59E0B)
                                    )
                                }
                                Text(
                                    text = "4.9",
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFF5EEFF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = if (isEnglish) "•  6 Chapters  •  ~45 min  •  180+ Choices"
                                else "•  6 Bölüm  •  ~45 dk  •  180+ Seçim",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFD9C9F5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Story Synopsis
                        Text(
                            text = buildAnnotatedString {
                                if (isEnglish) {
                                    append("Having lost his family in a blue light at age five, ")
                                    withStyle(SpanStyle(color = Color(0xFFFFFFFF), fontWeight = FontWeight.Medium)) {
                                        append("Aiden Blackwood")
                                    }
                                    append(" has been running for 20 years. Guide the story and relationships with your choices.")
                                } else {
                                    append("Beş yaşında mavi ışıkla ailesini kaybeden ")
                                    withStyle(SpanStyle(color = Color(0xFFFFFFFF), fontWeight = FontWeight.Medium)) {
                                        append("Aiden Blackwood")
                                    }
                                    append(", 20 yıldır kaçıyor. Seçimlerinle hikayeyi ve ilişkileri yönlendir.")
                                }
                            },
                            fontFamily = InterFontFamily,
                            color = Color(0xFFE6DCFC),
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            letterSpacing = 0.1.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Progress Section (Tappable to open Chapter Selection)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { showChapterModal = true }
                                .testTag("books_progress_section")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEnglish) "Progress" else "İlerleme",
                                    fontFamily = LiterarySerifFontFamily,
                                    color = Color(0xFFDCCDF7),
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Normal
                                )
                                Text(
                                    text = if (isEnglish) "Chapter $displayChapter / 6" else "Bölüm $displayChapter / 6",
                                    fontFamily = LiterarySerifFontFamily,
                                    color = Color(0xFFF3E8FF),
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val progressFraction = (displayChapter.coerceIn(1, 6)) / 6f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0x3D2C1854))
                                    .border(
                                        width = 0.8.dp,
                                        color = Color(0x66C4B5FD),
                                        shape = RoundedCornerShape(50)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction)
                                        .fillMaxHeight()
                                        .booksCosmicOuterGlow(
                                            cornerRadius = 5.dp,
                                            glowColor = Color(0xFFD8B4FE),
                                            alpha = 0.72f * featuredGlow,
                                            blurDp = 5.dp
                                        )
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFFF3E8FF),
                                                    Color(0xFFD8B4FE),
                                                    Color(0xFFA855F7)
                                                )
                                            )
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Characters Row ("Karakterler" + 5 Overlapping Neon Glass Emblem Orbs)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { showChapterModal = true }
                                .testTag("books_cast_row"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (isEnglish) "Characters" else "Karakterler",
                                fontFamily = LiterarySerifFontFamily,
                                color = Color(0xFFDCCDF7),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy((-6).dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CastEmblemOrb(
                                    ringColor = Color(0xFF60A5FA),
                                    innerTint = Color(0xFF172554),
                                    contentDescription = "Steve Rogers"
                                ) { drawShieldEmblem() }
                                CastEmblemOrb(
                                    ringColor = Color(0xFFF87171),
                                    innerTint = Color(0xFF450A0A),
                                    contentDescription = "Natasha Romanoff"
                                ) { drawSpiderEmblem() }
                                CastEmblemOrb(
                                    ringColor = Color(0xFFC084FC),
                                    innerTint = Color(0xFF3B0764),
                                    contentDescription = "Wanda Maximoff"
                                ) { drawMysticOrbEmblem() }
                                CastEmblemOrb(
                                    ringColor = Color(0xFF34D399),
                                    innerTint = Color(0xFF064E3B),
                                    contentDescription = "Sam Wilson"
                                ) { drawEmeraldMaskEmblem() }
                                CastEmblemOrb(
                                    ringColor = Color(0xFFFBBF24),
                                    innerTint = Color(0xFF451A03),
                                    contentDescription = "Tony Stark"
                                ) {
                                    drawLightningBolt(
                                        topColor = Color(0xFFFEF08A),
                                        bottomColor = Color(0xFFF59E0B),
                                        glowColor = Color(0xFFFBBF24)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Floating Luminous Mor Cam Glass CTA Capsule ("Kitaba Başla")
                        val mainBtnScale = remember { Animatable(1f) }
                        val mainBtnScope = rememberCoroutineScope()
                        val ctaLabel = when {
                            displayChapter >= 7 -> if (isEnglish) "Chapter 8 Soon" else "Bölüm 8 Yakında"
                            displayChapter >= 6 -> if (isEnglish) "Ch. 7: Dark Mirror" else "Bölüm 7: Karanlık Ayna"
                            displayChapter > 1  -> if (isEnglish) "Continue · Ch. $displayChapter" else "Devam Et · Bölüm $displayChapter"
                            else                -> if (isEnglish) "Start Book" else "Kitaba Başla"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .graphicsLayer {
                                    scaleX = mainBtnScale.value
                                    scaleY = mainBtnScale.value
                                }
                                .booksCosmicOuterGlow(
                                    cornerRadius = 27.dp,
                                    glowColor = Color(0xFFC084FC),
                                    alpha = 0.56f * featuredGlow,
                                    blurDp = 12.dp
                                )
                                .clip(RoundedCornerShape(50))
                                .templatesBackdropBlur(
                                    blurredBitmap = resolvedBackdrop,
                                    alpha = 0.65f,
                                    scrollSync = scrollSync
                                )
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x668B5CF6),
                                            Color(0x4E3B1B70),
                                            Color(0x625B21B6)
                                        )
                                    )
                                )
                                .drawBehind {
                                    // Top pearl glass reflection sheen
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.28f),
                                                Color(0xFFE9D5FF).copy(alpha = 0.08f),
                                                Color.Transparent
                                            ),
                                            startY = 0f,
                                            endY = size.height * 0.48f
                                        ),
                                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                                    )
                                    // Bottom soft violet bounce reflection
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0x44D8B4FE)
                                            ),
                                            startY = size.height * 0.70f,
                                            endY = size.height
                                        ),
                                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                                    )
                                }
                                .border(
                                    width = 1.4.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xF5F5EEFF),
                                            Color(0xCCE9D5FF),
                                            Color(0x99A855F7),
                                            Color(0xEDF3E8FF)
                                        )
                                    ),
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    mainBtnScope.launch {
                                        mainBtnScale.animateTo(0.965f, tween(80))
                                        mainBtnScale.animateTo(1f, tween(130))
                                    }
                                    val progress = ChapterProgressManager.getUnlockedChapter(context, "preset_aiden_mcu_cosmic")
                                    if (progress == 0 && !isBookExisting) {
                                        ChapterProgressManager.saveUnlockedChapter(context, "preset_aiden_mcu_cosmic", 1)
                                        onImportPresetBot(presetMcuBook)
                                        onOpenBot("preset_aiden_mcu_cosmic")
                                    } else {
                                        showChapterModal = true
                                    }
                                }
                                .testTag("start_featured_book_button")
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Left Sparkle Accent
                            Canvas(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .size(18.dp)
                            ) {
                                drawFourPointStar(
                                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                                    outerRadius = size.minDimension * 0.45f,
                                    innerRadius = size.minDimension * 0.14f,
                                    coreColor = Color(0xFFE9D5FF).copy(alpha = 0.82f),
                                    edgeColor = Color(0xFFA855F7).copy(alpha = 0.65f),
                                    glowColor = Color(0xFFC084FC),
                                    glowAlpha = 0.42f * starTwinkle
                                )
                            }

                            // Center Open Book Icon + Serif CTA Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(11.dp)
                            ) {
                                Canvas(modifier = Modifier.size(22.dp)) {
                                    drawLuminousOpenBook(
                                        leftColorTop = Color.White,
                                        leftColorBottom = Color(0xFFE9D5FF),
                                        rightColorTop = Color(0xFFE9D5FF),
                                        rightColorBottom = Color(0xFFC084FC),
                                        glowColor = Color(0xFFD8B4FE)
                                    )
                                }
                                Text(
                                    text = ctaLabel,
                                    fontFamily = DisplaySerifFontFamily,
                                    color = Color(0xFFFAF6FF),
                                    fontSize = 18.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.2.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Right Chevron
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = ctaLabel,
                                tint = Color(0xFFE9D5FF),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(22.dp)
                            )
                        }
                    }

                    // Double-Rimmed Crystal Glass Border & Corner Sparkle Glints Overlay
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val crOuter = 28.dp.toPx()
                        val crInner = 24.dp.toPx()
                        val inset = 4.dp.toPx()

                        // Subtle recessed inner glass bevel frame
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0x52F3E8FF),
                                    Color(0x18C084FC),
                                    Color(0x228B5CF6),
                                    Color(0x45E9D5FF)
                                )
                            ),
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - inset * 2f, size.height - inset * 2f),
                            cornerRadius = CornerRadius(crInner, crInner),
                            style = Stroke(width = 0.8.dp.toPx())
                        )

                        // Luminous primary outer crystal rim
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xEDF5EEFF),
                                    Color(0xB8C084FC),
                                    Color(0x888B5CF6),
                                    Color(0xD8E9D5FF)
                                )
                            ),
                            topLeft = Offset(0.75.dp.toPx(), 0.75.dp.toPx()),
                            size = Size(size.width - 1.5.dp.toPx(), size.height - 1.5.dp.toPx()),
                            cornerRadius = CornerRadius(crOuter, crOuter),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Crisp cosmic starlight glints along right rim (matching reference aesthetic)
                        drawFourPointStar(
                            center = Offset(size.width * 0.74f, size.height * 0.875f),
                            outerRadius = 7.dp.toPx(),
                            innerRadius = 1.6.dp.toPx(),
                            coreColor = Color.White.copy(alpha = 0.90f * starTwinkle),
                            edgeColor = Color(0xFFE9D5FF),
                            glowColor = Color(0xFFC084FC),
                            glowAlpha = 0.65f * starTwinkle
                        )
                        drawFourPointStar(
                            center = Offset(size.width * 0.96f, size.height * 0.31f),
                            outerRadius = 5.5.dp.toPx(),
                            innerRadius = 1.3.dp.toPx(),
                            coreColor = Color.White.copy(alpha = 0.85f * starTwinkle),
                            edgeColor = Color(0xFFD8B4FE),
                            glowColor = Color(0xFFA855F7),
                            glowAlpha = 0.55f * starTwinkle
                        )
                    }
                }
            }
        }

        // ── 4. PERSONAL LIBRARY SECTION (BELOW THE FOLD, MATCHING MOR CAM GLASS) ──
        if (myBooks.isNotEmpty()) {
            item(key = "books_library_header") {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .templatesBackdropBlur(
                            blurredBitmap = resolvedBackdrop,
                            alpha = 0.72f,
                            scrollSync = scrollSync
                        )
                        .background(Color(0x42241145))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xBAE9D5FF), Color(0x668B5CF6))
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showChapterModal = true }
                        .testTag("books_library_header_button")
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Canvas(modifier = Modifier.size(22.dp)) {
                            drawLuminousOpenBook(
                                leftColorTop = Color.White,
                                leftColorBottom = Color(0xFFD8B4FE),
                                rightColorTop = Color(0xFFD8B4FE),
                                rightColorBottom = Color(0xFFA855F7),
                                glowColor = Color(0xFFC084FC)
                            )
                        }
                        Text(
                            text = if (isEnglish) "Your Library" else "Kütüphaneniz",
                            fontFamily = DisplaySerifFontFamily,
                            color = Color(0xFFFAF5FF),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = if (isEnglish) "Open Chapters" else "Bölümleri Aç",
                        tint = Color(0xFFD8B4FE),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            items(myBooks, key = { it.id }) { bookBot ->
                val itemScale = remember { Animatable(1f) }
                val itemScope = rememberCoroutineScope()
                val hashColor = remember(bookBot.id) {
                    val colors = listOf(
                        Color(0xFFA855F7), Color(0xFF60A5FA), Color(0xFF34D399),
                        Color(0xFFFBBF24), Color(0xFFF472B6)
                    )
                    colors[abs(bookBot.id.hashCode()) % colors.size]
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { scaleX = itemScale.value; scaleY = itemScale.value }
                        .clip(RoundedCornerShape(20.dp))
                        .templatesBackdropBlur(
                            blurredBitmap = resolvedBackdrop,
                            alpha = 0.74f,
                            scrollSync = scrollSync
                        )
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0x52221042), Color(0x44140929))
                            )
                        )
                        .border(
                            width = 1.1.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xCCE9D5FF), hashColor.copy(alpha = 0.65f), Color(0x887C3AED))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            itemScope.launch {
                                itemScale.animateTo(0.97f, tween(80))
                                itemScale.animateTo(1f, tween(120))
                            }
                            onOpenBot(bookBot.id)
                        }
                        .testTag("user_book_card_${bookBot.id}")
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.radialGradient(
                                        listOf(hashColor.copy(alpha = 0.42f), Color(0x55180B32))
                                    )
                                )
                                .border(1.dp, hashColor.copy(alpha = 0.65f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                drawLuminousOpenBook(
                                    leftColorTop = Color.White,
                                    leftColorBottom = hashColor,
                                    rightColorTop = Color(0xFFE9D5FF),
                                    rightColorBottom = hashColor,
                                    glowColor = hashColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bookBot.aiName,
                                fontFamily = DisplaySerifFontFamily,
                                color = Color(0xFFFAF5FF),
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bookBot.universeName.ifBlank { if (isEnglish) "Interactive Novel" else "İnteraktif Roman" },
                                fontFamily = InterFontFamily,
                                color = Color(0xFFD4C4F5),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0x667C3AED), Color(0x555B21B6))
                                    )
                                )
                                .border(1.dp, Color(0xCCE9D5FF), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = if (isEnglish) "Continue" else "Devam Et",
                                fontFamily = InterFontFamily,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        val delItemScale = remember { Animatable(1f) }
                        val delItemScope = rememberCoroutineScope()
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x40450A0A))
                                .border(1.dp, Color(0x66F87171), CircleShape)
                                .graphicsLayer { scaleX = delItemScale.value; scaleY = delItemScale.value }
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    delItemScope.launch {
                                        delItemScale.animateTo(0.86f, tween(80))
                                        delItemScale.animateTo(1f, tween(120))
                                    }
                                    onDeleteBot(bookBot.id)
                                }
                                .testTag("delete_book_button_${bookBot.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = if (isEnglish) "Delete book" else "Kitabı sil",
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable Luminous Frosted Stat Pill ─────────────────────────────────────
@Composable
private fun CosmicGlassStatPill(
    modifier: Modifier = Modifier,
    resolvedBackdrop: ImageBitmap,
    scrollSync: Int,
    glowAlpha: Float,
    testTag: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .booksCosmicOuterGlow(
                cornerRadius = 21.dp,
                glowColor = Color(0xFFA855F7),
                alpha = 0.34f * glowAlpha,
                blurDp = 9.dp
            )
            .clip(RoundedCornerShape(50))
            .templatesBackdropBlur(
                blurredBitmap = resolvedBackdrop,
                alpha = 0.74f,
                scrollSync = scrollSync
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x4D3D206E),
                        Color(0x3B1D0D3B),
                        Color(0x472C1454)
                    )
                )
            )
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.48f
                    ),
                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                )
            }
            .border(
                width = 1.15.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xE0F3E8FF),
                        Color(0x99C084FC),
                        Color(0x787C3AED),
                        Color(0xCCE9D5FF)
                    )
                ),
                shape = RoundedCornerShape(50)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            content()
        }
    }
}

// ── Overlapping Glowing Character Emblem Medallion ──────────────────────────
@Composable
private fun CastEmblemOrb(
    ringColor: Color,
    innerTint: Color,
    contentDescription: String,
    drawEmblem: DrawScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 2.dp.toPx()
                        color = android.graphics.Color.argb(
                            140,
                            (ringColor.red * 255).toInt(),
                            (ringColor.green * 255).toInt(),
                            (ringColor.blue * 255).toInt()
                        )
                        maskFilter = android.graphics.BlurMaskFilter(
                            4.5.dp.toPx(),
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                    }
                    canvas.nativeCanvas.drawCircle(
                        size.width * 0.5f,
                        size.height * 0.5f,
                        size.minDimension * 0.47f,
                        glowPaint
                    )
                }
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        innerTint.copy(alpha = 0.85f),
                        Color(0xD80B0618)
                    )
                )
            )
            .border(
                width = 1.4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        ringColor,
                        ringColor.copy(alpha = 0.72f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(16.dp),
            contentDescription = contentDescription,
            onDraw = drawEmblem
        )
    }
}

// ── Outer Violet Cosmic Bloom Modifier ──────────────────────────────────────
private fun Modifier.booksCosmicOuterGlow(
    cornerRadius: Dp,
    glowColor: Color,
    alpha: Float,
    blurDp: Dp
): Modifier = this.drawBehind {
    val cr = cornerRadius.toPx()
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.argb(95, 8, 3, 20)
            maskFilter = android.graphics.BlurMaskFilter(10.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        native.drawRoundRect(
            2.dp.toPx(),
            4.dp.toPx(),
            size.width - 2.dp.toPx(),
            size.height + 4.dp.toPx(),
            cr,
            cr,
            shadowPaint
        )

        val haloPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3.2.dp.toPx()
            color = android.graphics.Color.argb(
                (alpha.coerceIn(0f, 1f) * 255).toInt(),
                (glowColor.red * 255).toInt(),
                (glowColor.green * 255).toInt(),
                (glowColor.blue * 255).toInt()
            )
            maskFilter = android.graphics.BlurMaskFilter(blurDp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        native.drawRoundRect(0f, 0f, size.width, size.height, cr, cr, haloPaint)
    }
}

// ── Custom Crisp Cosmic Vector Emblems (Canvas) ─────────────────────────────
private fun DrawScope.drawFourPointStar(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    coreColor: Color,
    edgeColor: Color,
    glowColor: Color,
    glowAlpha: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = glowAlpha.coerceIn(0f, 1f)),
                glowColor.copy(alpha = (glowAlpha * 0.28f).coerceIn(0f, 1f)),
                Color.Transparent
            ),
            center = center,
            radius = outerRadius * 1.45f
        ),
        radius = outerRadius * 1.45f,
        center = center
    )
    val path = Path().apply {
        moveTo(center.x, center.y - outerRadius)
        quadraticTo(center.x + innerRadius * 0.45f, center.y - innerRadius * 0.45f, center.x + outerRadius, center.y)
        quadraticTo(center.x + innerRadius * 0.45f, center.y + innerRadius * 0.45f, center.x, center.y + outerRadius)
        quadraticTo(center.x - innerRadius * 0.45f, center.y + innerRadius * 0.45f, center.x - outerRadius, center.y)
        quadraticTo(center.x - innerRadius * 0.45f, center.y - innerRadius * 0.45f, center.x, center.y - outerRadius)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(coreColor, edgeColor),
            center = center,
            radius = outerRadius
        )
    )
}

private fun DrawScope.drawFivePointStar(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    fillColor: Color,
    glowColor: Color
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor.copy(alpha = 0.42f), Color.Transparent),
            center = center,
            radius = outerRadius * 1.35f
        ),
        radius = outerRadius * 1.35f,
        center = center
    )
    val path = Path()
    for (i in 0 until 10) {
        val angle = (-90.0 + i * 36.0) * PI / 180.0
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = fillColor)
}

private fun DrawScope.drawLuminousOpenBook(
    leftColorTop: Color,
    leftColorBottom: Color,
    rightColorTop: Color,
    rightColorBottom: Color,
    glowColor: Color
) {
    val w = size.width
    val h = size.height
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor.copy(alpha = 0.40f), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.52f),
            radius = size.minDimension * 0.65f
        ),
        radius = size.minDimension * 0.65f,
        center = Offset(w * 0.5f, h * 0.52f)
    )
    val leftPage = Path().apply {
        moveTo(w * 0.48f, h * 0.22f)
        cubicTo(w * 0.34f, h * 0.14f, w * 0.15f, h * 0.15f, w * 0.08f, h * 0.22f)
        lineTo(w * 0.08f, h * 0.78f)
        cubicTo(w * 0.15f, h * 0.72f, w * 0.34f, h * 0.70f, w * 0.48f, h * 0.82f)
        close()
    }
    val rightPage = Path().apply {
        moveTo(w * 0.52f, h * 0.22f)
        cubicTo(w * 0.66f, h * 0.14f, w * 0.85f, h * 0.15f, w * 0.92f, h * 0.22f)
        lineTo(w * 0.92f, h * 0.78f)
        cubicTo(w * 0.85f, h * 0.72f, w * 0.66f, h * 0.70f, w * 0.52f, h * 0.82f)
        close()
    }
    drawPath(leftPage, Brush.verticalGradient(listOf(leftColorTop, leftColorBottom)))
    drawPath(rightPage, Brush.verticalGradient(listOf(rightColorTop, rightColorBottom)))
}

private fun DrawScope.drawLightningBolt(
    topColor: Color,
    bottomColor: Color,
    glowColor: Color
) {
    val w = size.width
    val h = size.height
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor.copy(alpha = 0.38f), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = size.minDimension * 0.65f
        ),
        radius = size.minDimension * 0.65f,
        center = Offset(w * 0.5f, h * 0.5f)
    )
    val bolt = Path().apply {
        moveTo(w * 0.58f, h * 0.06f)
        lineTo(w * 0.20f, h * 0.54f)
        lineTo(w * 0.48f, h * 0.54f)
        lineTo(w * 0.36f, h * 0.94f)
        lineTo(w * 0.80f, h * 0.44f)
        lineTo(w * 0.52f, h * 0.44f)
        close()
    }
    drawPath(bolt, Brush.verticalGradient(listOf(topColor, bottomColor)))
}

private fun DrawScope.drawRingedPlanetIcon() {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x5560A5FA), Color.Transparent),
            center = Offset(cx, cy),
            radius = size.minDimension * 0.62f
        ),
        radius = size.minDimension * 0.62f,
        center = Offset(cx, cy)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFBFDBFE), Color(0xFF3B82F6), Color(0xFF1D4ED8)),
            center = Offset(cx - size.width * 0.08f, cy - size.height * 0.08f),
            radius = size.minDimension * 0.38f
        ),
        radius = size.minDimension * 0.30f,
        center = Offset(cx, cy)
    )
    drawIntoCanvas { canvas ->
        val ringPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1.4.dp.toPx()
            color = android.graphics.Color.argb(235, 219, 234, 254)
        }
        canvas.nativeCanvas.save()
        canvas.nativeCanvas.rotate(-26f, cx, cy)
        canvas.nativeCanvas.drawOval(
            android.graphics.RectF(
                cx - size.width * 0.48f,
                cy - size.height * 0.16f,
                cx + size.width * 0.48f,
                cy + size.height * 0.16f
            ),
            ringPaint
        )
        canvas.nativeCanvas.restore()
    }
}

private fun DrawScope.drawShieldEmblem() {
    val w = size.width
    val h = size.height
    val shield = Path().apply {
        moveTo(w * 0.5f, h * 0.10f)
        lineTo(w * 0.84f, h * 0.24f)
        lineTo(w * 0.80f, h * 0.58f)
        quadraticTo(w * 0.72f, h * 0.80f, w * 0.5f, h * 0.92f)
        quadraticTo(w * 0.28f, h * 0.80f, w * 0.20f, h * 0.58f)
        lineTo(w * 0.16f, h * 0.24f)
        close()
    }
    drawPath(shield, Brush.verticalGradient(listOf(Color(0xFFDBEAFE), Color(0xFF60A5FA))))
    drawPath(
        shield,
        color = Color.White,
        style = Stroke(width = 1.1.dp.toPx(), join = StrokeJoin.Round)
    )
    // Inner vertical stripe accent
    drawLine(
        color = Color(0xFF1D4ED8),
        start = Offset(w * 0.5f, h * 0.24f),
        end = Offset(w * 0.5f, h * 0.78f),
        strokeWidth = 1.4.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawSpiderEmblem() {
    val w = size.width
    val h = size.height
    val color = Color(0xFFFCA5A5)
    // Body
    drawCircle(color = Color(0xFFEF4444), radius = w * 0.13f, center = Offset(w * 0.5f, h * 0.34f))
    drawCircle(color = Color(0xFFEF4444), radius = w * 0.17f, center = Offset(w * 0.5f, h * 0.62f))
    // 4 pairs of crisp legs
    val stroke = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round)
    val legOffsets = listOf(0.28f to 0.16f, 0.42f to 0.36f, 0.56f to 0.64f, 0.68f to 0.84f)
    legOffsets.forEach { (startY, endY) ->
        drawPath(
            Path().apply {
                moveTo(w * 0.40f, h * startY)
                lineTo(w * 0.20f, h * ((startY + endY) * 0.5f))
                lineTo(w * 0.12f, h * endY)
            },
            color = color,
            style = stroke
        )
        drawPath(
            Path().apply {
                moveTo(w * 0.60f, h * startY)
                lineTo(w * 0.80f, h * ((startY + endY) * 0.5f))
                lineTo(w * 0.88f, h * endY)
            },
            color = color,
            style = stroke
        )
    }
}

private fun DrawScope.drawMysticOrbEmblem() {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFAF5FF), Color(0xFFC084FC), Color(0xFF6B21A8)),
            center = Offset(cx - size.width * 0.08f, cy - size.height * 0.08f),
            radius = size.minDimension * 0.42f
        ),
        radius = size.minDimension * 0.32f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color(0xFFE9D5FF),
        radius = size.minDimension * 0.42f,
        center = Offset(cx, cy),
        style = Stroke(width = 0.9.dp.toPx())
    )
}

private fun DrawScope.drawEmeraldMaskEmblem() {
    val w = size.width
    val h = size.height
    val head = Path().apply {
        moveTo(w * 0.24f, h * 0.20f)
        lineTo(w * 0.38f, h * 0.32f)
        lineTo(w * 0.62f, h * 0.32f)
        lineTo(w * 0.76f, h * 0.20f)
        lineTo(w * 0.80f, h * 0.56f)
        quadraticTo(w * 0.72f, h * 0.84f, w * 0.50f, h * 0.88f)
        quadraticTo(w * 0.28f, h * 0.84f, w * 0.20f, h * 0.56f)
        close()
    }
    drawPath(head, Brush.verticalGradient(listOf(Color(0xFF6EE7B7), Color(0xFF059669))))
    // Glowing eyes
    drawCircle(Color(0xFFECFDF5), radius = w * 0.075f, center = Offset(w * 0.38f, h * 0.52f))
    drawCircle(Color(0xFFECFDF5), radius = w * 0.075f, center = Offset(w * 0.62f, h * 0.52f))
}

// ── Progress Persistence Manager ────────────────────────────────────────────
object ChapterProgressManager {
    private const val PREFS_NAME = "emochi_book_progress"

    fun getUnlockedChapter(context: android.content.Context, botId: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        return prefs.getInt("progress_$botId", 0)
    }

    fun saveUnlockedChapter(context: android.content.Context, botId: String, chapterNumber: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val current = prefs.getInt("progress_$botId", 0)
        if (chapterNumber > current) {
            prefs.edit().putInt("progress_$botId", chapterNumber).apply()
        }
    }
}

private data class BookChapterInfo(
    val index: Int,
    val openTitle: String,
    val openDesc: String,
    val startMsg: String?
)

// ── Harmonized Mor Cam Glass Chapter Selection Modal ────────────────────────
@Composable
fun ChapterSelectionModal(
    botId: String,
    bookTitle: String,
    userSettings: UserSettingsEntity?,
    onDismiss: () -> Unit,
    onSelectChapter: (chapterIndex: Int, chapterStartMsg: String?) -> Unit
) {
    val context = LocalContext.current
    val isEnglish = userSettings?.appLanguage == "en"
    val progress = remember { ChapterProgressManager.getUnlockedChapter(context, botId) }

    val chapters = remember(isEnglish) {
        listOf(
            BookChapterInfo(
                index = 1,
                openTitle = if (isEnglish) "Chapter 1: Cosmic Drift" else "Bölüm 1: Kozmik Sürükleniş",
                openDesc = if (isEnglish) "Disappearance at age 5 in blue light, S.H.I.E.L.D. pursuit, and meeting the Avengers in New York."
                else "Beş yaşında mavi ışıkla kayboluş, S.H.I.E.L.D. takibi ve New York'ta Avengers ile temas.",
                startMsg = null
            ),
            BookChapterInfo(
                index = 2,
                openTitle = if (isEnglish) "Chapter 2: The Terms" else "Bölüm 2: Şartlar",
                openDesc = if (isEnglish) "Bargaining with Tony Stark and Steve Rogers at Avengers Tower, terms, and choosing sides."
                else "Avengers Kulesi'nde Tony Stark ve Steve Rogers ile pazarlık, şartlar ve taraf seçimi.",
                startMsg = "Bölüm 2: Şartlar'a başla"
            ),
            BookChapterInfo(
                index = 3,
                openTitle = if (isEnglish) "Chapter 3: Probation" else "Bölüm 3: Deneme Süresi",
                openDesc = if (isEnglish) "30-day trial period in Avengers Tower — training, trust, and the first bond."
                else "Avengers Kulesi'nde 30 günlük deneme süresi — antrenman, güven ve ilk bağlar.",
                startMsg = "Bölüm 3: Deneme Süresi'ne başla"
            ),
            BookChapterInfo(
                index = 4,
                openTitle = if (isEnglish) "Chapter 4: First Mission" else "Bölüm 4: İlk Görev",
                openDesc = if (isEnglish) "Aiden's first official HYDRA operation — and an unexpected discovery at Manhattan Harbor."
                else "Aiden'ın ilk resmi HYDRA operasyonu — ve Manhattan Limanı'nda beklenmedik bir keşif.",
                startMsg = "Bölüm 4: İlk Görev'e başla"
            ),
            BookChapterInfo(
                index = 5,
                openTitle = if (isEnglish) "Chapter 5: Red Ice" else "Bölüm 5: Kırmızı Buz",
                openDesc = if (isEnglish) "The Red Guardian — a mirror from the past. Who is Elias Voss and why was he sent to find you?"
                else "Kırmızı Koruyucu — geçmişten bir ayna. Elias Voss kim ve neden seni bulmak için gönderildi?",
                startMsg = "Bölüm 5: Kırmızı Buz'a başla"
            ),
            BookChapterInfo(
                index = 6,
                openTitle = if (isEnglish) "Chapter 6: Broken Mirror" else "Bölüm 6: Kırık Ayna",
                openDesc = if (isEnglish) "The Istanbul archive reveals: Aiden was never a fugitive — he was a project. And Project 7-A is not over."
                else "İstanbul arşivi ortaya koyuyor: Aiden hiçbir zaman bir kaçak değildi — o bir projeydi. Ve Proje 7-A bitmedi.",
                startMsg = "Bölüm 6: Kırık Ayna'ya başla"
            ),
            BookChapterInfo(
                index = 7,
                openTitle = if (isEnglish) "Chapter 7: Dark Mirror" else "Bölüm 7: Karanlık Ayna",
                openDesc = if (isEnglish) "Dr. Hale surrenders. Aiden's parents are found. And a Red Eye carrier waits in the Istanbul tunnels."
                else "Dr. Hale teslim oluyor. Aiden'ın ailesi bulunuyor. Ve İstanbul tünellerinde bir Kırmızı Göz taşıyıcısı bekliyor.",
                startMsg = "Bölüm 7: Karanlık Ayna'ya başla"
            ),
            BookChapterInfo(
                index = 8,
                openTitle = if (isEnglish) "Chapter 8: Coming Soon" else "Bölüm 8: Yakında",
                openDesc = if (isEnglish) "The next chapter is in development. Stay tuned."
                else "Sonraki bölüm yapım aşamasında. Takipte kal.",
                startMsg = null
            )
        )
    }

    val modalInfinite = rememberInfiniteTransition(label = "chModalGlow")
    val modalGlow by modalInfinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "mg"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp)
                .booksCosmicOuterGlow(
                    cornerRadius = 26.dp,
                    glowColor = Color(0xFFA855F7),
                    alpha = 0.45f * modalGlow,
                    blurDp = 16.dp
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF21B0D36),
                            Color(0xF5110724),
                            Color(0xF21A0C33)
                        )
                    )
                )
                .border(
                    width = 1.4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xE8F3E8FF),
                            Color(0xA6C084FC),
                            Color(0x887C3AED),
                            Color(0xDDE9D5FF)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0A0418))
                                .border(
                                    width = 1.4.dp,
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFFF3E8FF), Color(0xFFC084FC), Color(0xFF60A5FA))
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_aiden_blackwood_avatar),
                                contentDescription = if (isEnglish) "Aiden Blackwood Cosmic Drift portrait" else "Aiden Blackwood Kozmik Sürükleniş portresi",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        Column {
                            Text(
                                text = if (isEnglish) "Chapter Selection" else "Bölüm Seçimi",
                                fontFamily = DisplaySerifFontFamily,
                                color = Color(0xFFFAF6FF),
                                fontSize = 18.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = bookTitle,
                                fontFamily = InterFontFamily,
                                color = Color(0xFFC4B5FD),
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val closeBtnScale = remember { Animatable(1f) }
                    val closeScope = rememberCoroutineScope()
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x552C1654))
                            .border(1.dp, Color(0x99D8B4FE), CircleShape)
                            .graphicsLayer { scaleX = closeBtnScale.value; scaleY = closeBtnScale.value }
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                closeScope.launch {
                                    closeBtnScale.animateTo(0.86f, tween(80))
                                    closeBtnScale.animateTo(1f, tween(120))
                                }
                                onDismiss()
                            }
                            .testTag("chapter_modal_close_icon"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEnglish) "Close" else "Kapat",
                            tint = Color(0xFFF3E8FF),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Summary Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x4D2A1552))
                        .border(1.dp, Color(0x66C4B5FD), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(6) { i ->
                                Box(
                                    modifier = Modifier
                                        .size(if (i < progress) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (i < progress) Color(0xFF34D399) else Color(0x55C4B5FD))
                                )
                            }
                        }
                        Text(
                            text = when {
                                progress >= 6 -> if (isEnglish) "Chapter 6 Completed" else "Bölüm 6 Tamamlandı"
                                progress >= 5 -> if (isEnglish) "Ch. 5 Done • Ch. 6 Open" else "Bölüm 5 Tamamlandı • Bölüm 6 Açık"
                                progress >= 4 -> if (isEnglish) "Ch. 4 Done • Ch. 5 Open" else "Bölüm 4 Tamamlandı • Bölüm 5 Açık"
                                progress >= 3 -> if (isEnglish) "Ch. 3 Done • Ch. 4 Open" else "Bölüm 3 Tamamlandı • Bölüm 4 Açık"
                                progress >= 2 -> if (isEnglish) "Ch. 2 Done • Ch. 3 Open" else "Bölüm 2 Tamamlandı • Bölüm 3 Açık"
                                progress >= 1 -> if (isEnglish) "Ch. 1 Done • Ch. 2 Open" else "Bölüm 1 Tamamlandı • Bölüm 2 Açık"
                                else -> if (isEnglish) "Not Started • Chapter 1 Open" else "Henüz Başlanmadı • Bölüm 1 Açık"
                            },
                            fontFamily = InterFontFamily,
                            color = Color(0xFFE9D5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Chapters List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    val chapterBandColors = listOf(
                        Color(0xFFA855F7), // 1
                        Color(0xFF60A5FA), // 2
                        Color(0xFF22D3EE), // 3
                        Color(0xFFFBBF24), // 4
                        Color(0xFFF87171), // 5
                        Color(0xFF34D399), // 6
                        Color(0xFFC084FC), // 7
                        Color(0xFF94A3B8)  // 8
                    )
                    val chapterDurations = listOf("~6 dk", "~7 dk", "~8 dk", "~8 dk", "~7 dk", "~9 dk", "~11 dk", "—")

                    chapters.forEach { item ->
                        val isUnlocked = item.index <= (progress + 1) && item.index <= 7
                        val isCompleted = item.index <= progress
                        val isCurrent = (progress + 1) == item.index || (progress >= 6 && item.index == 6)
                        val isComingSoon = item.index == 8
                        val bandColor = chapterBandColors.getOrElse(item.index - 1) { Color(0xFFA855F7) }
                        val duration = chapterDurations.getOrElse(item.index - 1) { "—" }

                        val itemScale = remember(item.index) { Animatable(1f) }
                        val itemScope = rememberCoroutineScope()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { scaleX = itemScale.value; scaleY = itemScale.value }
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when {
                                        isCurrent -> Brush.horizontalGradient(listOf(Color(0x663B1E74), Color(0x55221048)))
                                        isCompleted -> Brush.horizontalGradient(listOf(Color(0x4D064E3B), Color(0x44140B29)))
                                        isUnlocked -> Brush.horizontalGradient(listOf(Color(0x4D27144D), Color(0x44160A2E)))
                                        else -> Brush.horizontalGradient(listOf(Color(0x38190E33), Color(0x38120926)))
                                    }
                                )
                                .border(
                                    width = if (isCurrent) 1.25.dp else 0.9.dp,
                                    brush = when {
                                        isCurrent -> Brush.linearGradient(listOf(Color(0xEEF3E8FF), bandColor))
                                        isCompleted -> Brush.linearGradient(listOf(Color(0x9934D399), Color(0x66059669)))
                                        isUnlocked -> Brush.linearGradient(listOf(Color(0x77C4B5FD), Color(0x447C3AED)))
                                        else -> Brush.linearGradient(listOf(Color(0x337C3AED), Color(0x223B1E74)))
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    when {
                                        isComingSoon -> android.widget.Toast.makeText(
                                            context,
                                            if (isEnglish) "Chapter 8 coming soon! Stay tuned." else "Bölüm 8 yakında yayınlanacak! Takipte kal.",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        isUnlocked -> {
                                            itemScope.launch {
                                                itemScale.animateTo(0.965f, tween(80))
                                                itemScale.animateTo(1f, tween(120))
                                            }
                                            onSelectChapter(item.index, item.startMsg)
                                        }
                                        else -> android.widget.Toast.makeText(
                                            context,
                                            if (isEnglish) "Complete the previous chapter first!" else "Önce önceki bölümü tamamla!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .testTag("chapter_item_${item.index}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Chapter Number / State Badge
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCompleted -> Brush.radialGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
                                                isCurrent -> Brush.radialGradient(listOf(bandColor, Color(0xFF4C1D95)))
                                                isUnlocked -> Brush.radialGradient(listOf(bandColor.copy(alpha = 0.48f), Color(0xFF1A0D38)))
                                                else -> Brush.radialGradient(listOf(Color(0x443B226E), Color(0x33160A2D)))
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isUnlocked) bandColor.copy(alpha = 0.8f) else Color(0x447C3AED),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        isComingSoon || !isUnlocked -> Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFF8B7CB0),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        isCompleted -> Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                        else -> Text(
                                            text = "${item.index}",
                                            fontFamily = DisplaySerifFontFamily,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                isComingSoon || isUnlocked -> item.openTitle
                                                else -> if (isEnglish) "Chapter ${item.index}" else "Bölüm ${item.index}"
                                            },
                                            fontFamily = LiterarySerifFontFamily,
                                            color = if (isUnlocked && !isComingSoon) Color(0xFFFAF5FF) else Color(0xFF8C7DB0),
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (isCurrent) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(bandColor.copy(alpha = 0.28f))
                                                    .border(0.7.dp, bandColor.copy(alpha = 0.75f), RoundedCornerShape(50))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isEnglish) "ACTIVE" else "AKTİF",
                                                    fontFamily = InterFontFamily,
                                                    color = Color(0xFFF3E8FF),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        if (isCompleted && !isCurrent) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(Color(0x44059669))
                                                    .border(0.7.dp, Color(0xAA34D399), RoundedCornerShape(50))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isEnglish) "DONE" else "BİTİRİLDİ",
                                                    fontFamily = InterFontFamily,
                                                    color = Color(0xFF6EE7B7),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        if (isComingSoon) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(Color(0x3394A3B8))
                                                    .border(0.7.dp, Color(0x66CBD5E1), RoundedCornerShape(50))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isEnglish) "SOON" else "YAKINDA",
                                                    fontFamily = InterFontFamily,
                                                    color = Color(0xFFCBD5E1),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = when {
                                            isComingSoon -> if (isEnglish) "Coming very soon..." else "Çok yakında yayınlanacak..."
                                            isUnlocked -> item.openDesc
                                            else -> if (isEnglish) "Complete the previous chapter." else "Önceki bölümü tamamla."
                                        },
                                        fontFamily = InterFontFamily,
                                        color = if (isUnlocked && !isComingSoon) Color(0xFFBEB0DF) else Color(0xFF695A8C),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (isUnlocked && !isComingSoon) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isCompleted) "$duration • ${if (isEnglish) "Replay" else "Tekrar oyna"}" else duration,
                                            fontFamily = InterFontFamily,
                                            color = bandColor.copy(alpha = 0.88f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Icon(
                                    imageVector = if (isUnlocked && !isComingSoon) Icons.Default.ChevronRight else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isUnlocked && !isComingSoon) Color(0xFFE9D5FF) else Color(0xFF5D4C82),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Glass Close Capsule
                val closeBtnScale2 = remember { Animatable(1f) }
                val closeScope2 = rememberCoroutineScope()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x554C1D95), Color(0x442E1065), Color(0x554C1D95))
                            )
                        )
                        .border(1.1.dp, Color(0xBBE9D5FF), RoundedCornerShape(50))
                        .graphicsLayer { scaleX = closeBtnScale2.value; scaleY = closeBtnScale2.value }
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            closeScope2.launch {
                                closeBtnScale2.animateTo(0.96f, tween(80))
                                closeBtnScale2.animateTo(1f, tween(120))
                            }
                            onDismiss()
                        }
                        .testTag("chapter_modal_close_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEnglish) "Close" else "Kapat",
                        fontFamily = DisplaySerifFontFamily,
                        color = Color(0xFFF5EEFF),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
