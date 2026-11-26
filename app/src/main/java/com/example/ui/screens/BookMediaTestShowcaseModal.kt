package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.components.MorCamTokens
import com.example.ui.screens.cosmic3d.CosmicDuelBossId
import com.example.ui.screens.cosmic3d.CosmicRooftopBossDuelDialog
import com.example.ui.screens.cosmic3d.InStoryMiniGameCard
import com.example.ui.screens.cosmic3d.StoryMiniGameId
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily

data class BookTestBossEntry(
    val bossId: CosmicDuelBossId,
    val title: String,
    val chapterBadge: String,
    val subtitle: String,
    val accent: Color,
    val previewRes: Int
)

data class BookTestMiniGameEntry(
    val gameId: StoryMiniGameId,
    val title: String,
    val chapterBadge: String,
    val subtitle: String
)

data class BookTestAnimationEntry(
    val sceneId: String,
    val title: String,
    val chapterBadge: String,
    val subtitle: String,
    val accent: Color
)

data class BookTestLive3DEntry(
    val id: String,
    val characterKey: String,
    val sceneId: String,
    val title: String,
    val chapterBadge: String,
    val subtitle: String,
    val accent: Color
)

data class BookTestPhotoEntry(
    val id: String,
    val title: String,
    val category: String,
    val drawableRes: Int
)

data class BookTestVoiceEntry(
    val speakerKey: String,
    val displayName: String,
    val voiceActorLabel: String,
    val sampleLine: String,
    val accent: Color
)

/**
 * Single Source of Truth for every interactive game, cinema animation, Live 3D encounter,
 * and visual artwork bundled inside Book Mode ("Kozmik Sürükleniş" Season 1 & 2).
 */
object BookMediaTestCatalog {
    val bossDuels: List<BookTestBossEntry> = listOf(
        BookTestBossEntry(
            bossId = CosmicDuelBossId.VAELOR_ROOFTOP,
            title = "Son Yüzleşme · Vaelor (Avengers Kulesi Çatısı)",
            chapterBadge = "1. SEZON · BÖLÜM 10 FİNAL",
            subtitle = "60fps çatı boss düellosu: Işınlanma, Enerji Emilimi, Beyaz Göz & Kırmızı Göz Ultimate ve Avengers desteği.",
            accent = Color(0xFF38BDF8),
            previewRes = R.drawable.img_game_rooftop_arena
        ),
        BookTestBossEntry(
            bossId = CosmicDuelBossId.MIRROR_ZERO,
            title = "Ayna Düellosu · Sıfır (Kule Terası)",
            chapterBadge = "2. SEZON · BÖLÜM 16",
            subtitle = "Kendi karanlık geleceğin Sıfır'a karşı ayna-klon fazları, mavi yankı topları ve zihin kırıcı kapışma.",
            accent = Color(0xFFA855F7),
            previewRes = R.drawable.img_postcredits_twin_aiden
        ),
        BookTestBossEntry(
            bossId = CosmicDuelBossId.ULTRON_PRIME,
            title = "Demir ve Yankı · Ultron Prime (Sokovia)",
            chapterBadge = "2. SEZON · BÖLÜM 23 FİNAL",
            subtitle = "Vibranyum zırh kırma, ışın çarpışması ve Vision/Thor/Wanda/Steve/Natasha/Tony takım destekli final savaşı.",
            accent = Color(0xFFF43F5E),
            previewRes = R.drawable.img_portrait_ultron_prime_1795557413989
        )
    )

    val miniGames: List<BookTestMiniGameEntry> = listOf(
        BookTestMiniGameEntry(
            gameId = StoryMiniGameId.WHITE_EYE_RESONANCE,
            title = "Beyaz Göz Ritim Odağı",
            chapterBadge = "1. SEZON · BÖLÜM 3",
            subtitle = "Daralan kozmik halkaları altın çemberde mühürleyip beş Avenger'ın zihnini tek bir zarar vermeden sabitle!"
        ),
        BookTestMiniGameEntry(
            gameId = StoryMiniGameId.BOSPHORUS_CORE_BALANCE,
            title = "Boğaz Çekirdeği Dengesi",
            chapterBadge = "1. SEZON · BÖLÜM 7",
            subtitle = "Mavi kozmik itki ile Mira'nın kızıl çekimini dengeleyip çatlayan Rezonans Kulesi'ni dokuz saniye stabil tut!"
        ),
        BookTestMiniGameEntry(
            gameId = StoryMiniGameId.RED_EYE_WILLPOWER,
            title = "Kırmızı Göz İrade Mührü",
            chapterBadge = "1. SEZON · BÖLÜM 9",
            subtitle = "Vance'in kışkırtması öfkeni alevlendirirken hızla açılan kızıl çatlakları mühürle ve kontrolü elinde tut!"
        )
    )

    val animations: List<BookTestAnimationEntry> = listOf(
        BookTestAnimationEntry(
            sceneId = "BLUE_SKY_2006",
            title = "14 Kasım 2006 · Mavi Gökyüzü",
            chapterBadge = "1. SEZON BÖLÜM 1 & 2. SEZON BÖLÜM 13",
            subtitle = "Sonsuzluk Taşı düzeyindeki mavi yarık Manhattan semalarını yardığında beş yaşındaydın.",
            accent = Color(0xFF38BDF8)
        ),
        BookTestAnimationEntry(
            sceneId = "WHITE_EYE_AWAKENING",
            title = "Beyaz Göz · Üç Saniyelik Boşluk",
            chapterBadge = "1. SEZON · BÖLÜM 3",
            subtitle = "Çarpıcı mavi gözlerin kör edici beyaza döndüğünde beş Avenger'ın zihni aynı anda durdu.",
            accent = Color(0xFFE2E8F0)
        ),
        BookTestAnimationEntry(
            sceneId = "BOSPHORUS_RESONANCE",
            title = "Boğaz Altı · Rezonans Kulesi",
            chapterBadge = "1. SEZON · BÖLÜM 7",
            subtitle = "Kırk metrelik çelik çekirdeğin çevresinde mavi kozmik ışık ve Mira'nın kızıl ateşi çarpışıyordu.",
            accent = Color(0xFFF43F5E)
        ),
        BookTestAnimationEntry(
            sceneId = "MANHATTAN_ARMADA",
            title = "Manhattan Sema · Yıldızların Kırıldığı Gece",
            chapterBadge = "1. SEZON · BÖLÜM 10",
            subtitle = "Soluk Mühür ana gemisi mavi yarıktan süzülerek Avengers Kulesi'nin tam karşısında durdu.",
            accent = Color(0xFF60A5FA)
        ),
        BookTestAnimationEntry(
            sceneId = "MUTLAK_UYANIS",
            title = "Mutlak Uyanış · Evrenin En Güçlü Anı",
            chapterBadge = "1. SEZON · BÖLÜM 10 FİNAL",
            subtitle = "Mavi Kozmik Enerji, Beyaz Göz ve Kırmızı Göz tek bir iradede birleşti — yağmur damlaları havada dondu.",
            accent = Color(0xFFEAB308)
        )
    )

    val live3DEncounters: List<BookTestLive3DEntry> = listOf(
        BookTestLive3DEntry(
            id = "live_natasha_balcony",
            characterKey = "NATASHA",
            sceneId = "TOWER_BALCONY",
            title = "Gece Yarısı Balkonu · Natasha",
            chapterBadge = "2. SEZON · BÖLÜM 11",
            subtitle = "Manhattan ışıkları altında partiden kaçış — 3D bakış açısını sürükle, gözlerine bak veya konuşmasını dinle.",
            accent = Color(0xFFF43F5E)
        ),
        BookTestLive3DEntry(
            id = "live_natasha_veranda",
            characterKey = "NATASHA",
            sceneId = "FARM_VERANDA",
            title = "Yağmurlu Veranda · Natasha ile Yalnız",
            chapterBadge = "2. SEZON · BÖLÜM 14",
            subtitle = "Barton Çiftliği'nde gece yağmuru altında baş başa sahne.",
            accent = Color(0xFFFB7185)
        ),
        BookTestLive3DEntry(
            id = "live_wanda_orchard",
            characterKey = "WANDA",
            sceneId = "FARM_ORCHARD",
            title = "Yıldızlı Elma Bahçesi · Wanda ile Yalnız",
            chapterBadge = "2. SEZON · BÖLÜM 14",
            subtitle = "Yıldızların altında Wanda Maximoff'un zihin ışığı ve sessiz fısıltısı.",
            accent = Color(0xFFC084FC)
        ),
        BookTestLive3DEntry(
            id = "live_voss_safehouse",
            characterKey = "VOSS",
            sceneId = "SAFEHOUSE_VOSS",
            title = "Brooklyn Sığınağı · Elias Voss (6-B)",
            chapterBadge = "2. SEZON · BÖLÜM 15",
            subtitle = "Elias Voss ile gergin yüzleşme — 3D sahnede incele ve konuş.",
            accent = Color(0xFF38BDF8)
        ),
        BookTestLive3DEntry(
            id = "live_mira_safehouse",
            characterKey = "MIRA",
            sceneId = "SAFEHOUSE_MIRA",
            title = "Güvenli Sığınak · Mira (8-C)",
            chapterBadge = "2. SEZON · BÖLÜM 15",
            subtitle = "Mira'nın ateşli enerjisi ve eski yaralarıyla yüzleşme.",
            accent = Color(0xFFF97316)
        ),
        BookTestLive3DEntry(
            id = "live_zero_cell",
            characterKey = "ZERO",
            sceneId = "TOWER_CELL",
            title = "Revir Odası · Kendi Karanlık Geleceğin (Sıfır)",
            chapterBadge = "2. SEZON · BÖLÜM 17",
            subtitle = "Sıfır'ın gözlerine bak ve beş yıllık yalnızlığın sesini dinle.",
            accent = Color(0xFFA855F7)
        ),
        BookTestLive3DEntry(
            id = "live_zero_sunrise",
            characterKey = "ZERO",
            sceneId = "HELIDECK_SUNRISE",
            title = "Altın Şafak Güvertesi · Sıfır'ın Son Vedası",
            chapterBadge = "2. SEZON · BÖLÜM 24 FİNAL",
            subtitle = "Sıfır yıldız tozuna karışmadan önce çoklu evrende yeniden tek Aiden kalıyor.",
            accent = Color(0xFFFBBF24)
        ),
        BookTestLive3DEntry(
            id = "live_mira_garden",
            characterKey = "MIRA",
            sceneId = "COMPOUND_GARDEN",
            title = "Yeni Başlangıç Bahçesi · Mira & Voss",
            chapterBadge = "2. SEZON · BÖLÜM 24 FİNAL",
            subtitle = "Mira'nın özgür hayatının ilk sabahında teşekkür sahnesi.",
            accent = Color(0xFF34D399)
        ),
        BookTestLive3DEntry(
            id = "live_natasha_finale",
            characterKey = "NATASHA",
            sceneId = "FINALE_TERRACE",
            title = "Gün Batımı Terası · Natasha Finali",
            chapterBadge = "2. SEZON · BÖLÜM 24 ROMANTİK FİNAL",
            subtitle = "Savaş bitti. Gölden esen akşam rüzgârı ve yıldızlar altında Natasha ile yeni başlangıç.",
            accent = Color(0xFFF43F5E)
        ),
        BookTestLive3DEntry(
            id = "live_wanda_finale",
            characterKey = "WANDA",
            sceneId = "FINALE_PIER",
            title = "Göl İskelesi · Wanda Finali",
            chapterBadge = "2. SEZON · BÖLÜM 24 ROMANTİK FİNAL",
            subtitle = "Yıldızlar gölün suyunda parlıyor; Wanda ilk kez kâbussuz bir geceye gülümsüyor.",
            accent = Color(0xFFD946EF)
        )
    )

    val photos: List<BookTestPhotoEntry> = listOf(
        // Kapaklar & Arenalar
        BookTestPhotoEntry("p_banner_main", "Kozmik Sürükleniş Ana Afiş", "Kapaklar & Arenalar", R.drawable.img_book_kozmik_suruklenis_banner),
        BookTestPhotoEntry("p_hero_card", "Kitap Hero Kart Kapağı", "Kapaklar & Arenalar", R.drawable.img_book_hero_banner_card),
        BookTestPhotoEntry("p_hero_generic", "Genel Kitap Kapak Sanatı", "Kapaklar & Arenalar", R.drawable.img_book_hero_banner_generic),
        BookTestPhotoEntry("p_rooftop_arena", "Avengers Kulesi Çatı Arenası", "Kapaklar & Arenalar", R.drawable.img_game_rooftop_arena),
        BookTestPhotoEntry("p_bg_castle", "Gotik Kale Atmosfer Arka Planı", "Kapaklar & Arenalar", R.drawable.img_book_bg_top_castle),
        BookTestPhotoEntry("p_bg_nebula", "Kozmik Nebula Alt Arka Planı", "Kapaklar & Arenalar", R.drawable.img_book_bg_bottom_nebula),

        // Bölüm Sahne Fotoğrafları (11)
        BookTestPhotoEntry("p_ch1_c1_calm", "Bölüm 1 · Görü (Sakin İzleme)", "Bölüm Sahneleri", R.drawable.img_ch1_c1_vision_calm),
        BookTestPhotoEntry("p_ch1_c1_trace", "Bölüm 1 · Görüye Derin Odaklanma", "Bölüm Sahneleri", R.drawable.img_ch1_c1_vision_trace),
        BookTestPhotoEntry("p_ch1_c2_direct", "Bölüm 1 · Kuleye Direkt Işınlanma", "Bölüm Sahneleri", R.drawable.img_ch1_c2_direct_rift),
        BookTestPhotoEntry("p_ch1_c2_forced", "Bölüm 1 · Zorunlu Yarık Geçişi", "Bölüm Sahneleri", R.drawable.img_ch1_c2_forced_rift),
        BookTestPhotoEntry("p_ch1_c2_stealth", "Bölüm 1 · Terastan Gizli Gözlem", "Bölüm Sahneleri", R.drawable.img_ch1_c2_stealth_terrace),
        BookTestPhotoEntry("p_ch1_c3_stark", "Bölüm 1 · Tony Stark Yüzleşmesi", "Bölüm Sahneleri", R.drawable.img_ch1_c3_stark_faceoff),
        BookTestPhotoEntry("p_ch1_c4_natasha", "Bölüm 1 · Natasha Gece Balkonu", "Bölüm Sahneleri", R.drawable.natasha_3d_turned_1795640635973),
        BookTestPhotoEntry("p_ch1_c4_wanda", "Bölüm 1 · Wanda Göz Teması", "Bölüm Sahneleri", R.drawable.img_ch1_c4_wanda_gaze),
        BookTestPhotoEntry("p_ch1_c5_tower", "Bölüm 1 · Gece Avengers Kulesi", "Bölüm Sahneleri", R.drawable.img_ch1_c5_tower_night),
        BookTestPhotoEntry("p_ch5_voss", "Bölüm 5 · Voss Tipi Pususu", "Bölüm Sahneleri", R.drawable.img_ch5_voss_blizzard),
        BookTestPhotoEntry("p_ch6_istanbul", "Bölüm 6 · İstanbul Yerebatan Sarnıcı", "Bölüm Sahneleri", R.drawable.img_ch6_istanbul_cistern),

        // Bölüm Sonu & Romantik Final Kareleri (2)
        BookTestPhotoEntry("p_end_nat_terrace", "Bölüm Sonu · Natasha & Aiden (Teras)", "Bölüm Sonu & Final Sahneleri", R.drawable.img_scene_natasha_aiden_terrace_1795640635755),
        BookTestPhotoEntry("p_end_wanda_pier", "Bölüm Sonu · Wanda & Aiden (Göl İskelesi)", "Bölüm Sonu & Final Sahneleri", R.drawable.img_end_wanda_aiden_pier_1795553970888),

        // Animasyon & Post-Credits Kareleri (6)
        BookTestPhotoEntry("p_anim_blue_sky", "Animasyon · 2006 Mavi Gökyüzü", "Animasyon & Post-Credits", R.drawable.img_anim_blue_sky_2006),
        BookTestPhotoEntry("p_anim_bosphorus", "Animasyon · Boğaz Rezonans Kulesi", "Animasyon & Post-Credits", R.drawable.img_anim_bosphorus),
        BookTestPhotoEntry("p_anim_armada", "Animasyon · Manhattan Uzay Armadası", "Animasyon & Post-Credits", R.drawable.img_anim_armada),
        BookTestPhotoEntry("p_anim_mutlak", "Animasyon · Mutlak Uyanış", "Animasyon & Post-Credits", R.drawable.img_anim_mutlak_uyanis),
        BookTestPhotoEntry("p_post_nyc", "Post-Credits · Harabe New York", "Animasyon & Post-Credits", R.drawable.img_postcredits_ruined_nyc),
        BookTestPhotoEntry("p_post_twin", "Post-Credits · İkiz Aiden (Sıfır)", "Animasyon & Post-Credits", R.drawable.img_postcredits_twin_aiden),

        // Karakter Portreleri (18)
        BookTestPhotoEntry("p_char_aiden_p", "Aiden Blackwood · Ana Portre", "Karakter Portreleri", R.drawable.img_aiden_portrait),
        BookTestPhotoEntry("p_char_aiden_a", "Aiden Blackwood · Profil Avatarı", "Karakter Portreleri", R.drawable.img_aiden_blackwood_avatar),
        BookTestPhotoEntry("p_char_steve", "Steve Rogers (Captain America)", "Karakter Portreleri", R.drawable.img_char_steve),
        BookTestPhotoEntry("p_char_natasha", "Natasha Romanoff (Black Widow)", "Karakter Portreleri", R.drawable.img_natasha_v5_portrait_1795640601606),
        BookTestPhotoEntry("p_char_wanda", "Wanda Maximoff (Scarlet Witch)", "Karakter Portreleri", R.drawable.img_portrait_wanda_hero_1795557432332),
        BookTestPhotoEntry("p_char_tony", "Tony Stark (Iron Man)", "Karakter Portreleri", R.drawable.img_portrait_tony_hero_1795559343343),
        BookTestPhotoEntry("p_char_clint", "Clint Barton (Hawkeye)", "Karakter Portreleri", R.drawable.img_portrait_clint_hero_1795559319365),
        BookTestPhotoEntry("p_char_pietro", "Pietro Maximoff (Quicksilver)", "Karakter Portreleri", R.drawable.img_portrait_pietro_hero_1795559251696),
        BookTestPhotoEntry("p_char_vision", "Vision · Zihin Taşı Formu", "Karakter Portreleri", R.drawable.img_portrait_vision_hero_1795559320364),
        BookTestPhotoEntry("p_char_strange", "Dr. Stephen Strange (Sanctum)", "Karakter Portreleri", R.drawable.img_portrait_strange_hero_1795616988269),
        BookTestPhotoEntry("p_char_architect", "Asıl Mimar · Altın-Mavi Yankı", "Karakter Portreleri", R.drawable.img_portrait_architect_hero_1795559252160),
        BookTestPhotoEntry("p_char_mira", "Mira · Denek 8-C (İstanbul)", "Karakter Portreleri", R.drawable.img_portrait_mira_8c_1795557413439),
        BookTestPhotoEntry("p_char_voss", "Elias Voss · Denek 6-B (Brooklyn)", "Karakter Portreleri", R.drawable.img_portrait_voss_6b_1795557413860),
        BookTestPhotoEntry("p_char_ultron", "Ultron Prime · Vibranyum Formu", "Karakter Portreleri", R.drawable.img_portrait_ultron_prime_1795557413989),
        BookTestPhotoEntry("p_char_bruce", "Bruce Banner (Hulk)", "Karakter Portreleri", R.drawable.img_char_bruce),
        BookTestPhotoEntry("p_char_fury", "Nick Fury (S.H.I.E.L.D.)", "Karakter Portreleri", R.drawable.img_char_fury),
        BookTestPhotoEntry("p_char_thor", "Thor Odinson", "Karakter Portreleri", R.drawable.img_char_thor),
        BookTestPhotoEntry("p_char_vaelor", "Vaelor (Soluk Mühür Habercisi)", "Karakter Portreleri", R.drawable.img_char_vaelor)
    )

    val studioVoices: List<BookTestVoiceEntry> = listOf(
        BookTestVoiceEntry("NATASHA", "Natasha Romanoff", "Lily · Kadifemsi Ajan", "Aşağıdaki Manhattan ışıklarına bak Aiden... Yukarıda herkes gülüyor ama en gerçek sessizlik senin yanında.", Color(0xFFF472B6)),
        BookTestVoiceEntry("WANDA", "Wanda Maximoff", "Sarah · Sıcak & Duygusal", "Gölün sularında yıldızlar parlıyor Aiden... İlk kez zihnimde hiç kâbus yok, sadece senin sıcaklığın var.", Color(0xFFC084FC)),
        BookTestVoiceEntry("MIRA", "Mira · Denek 8-C", "Jessica · Genç & Parlak", "Aiden! Geldiğini kapıdan bile değil, enerjinin sakinliğinden anladım. Sana göstermek istediğim yeni bir şey öğrendim!", Color(0xFF38BDF8)),
        BookTestVoiceEntry("AIDEN", "Aiden Blackwood", "Charlie · Karizmatik Kahraman", "Bütün ekibim için — BURADA BİTİYOR ULTRON!", Color(0xFF60A5FA)),
        BookTestVoiceEntry("TWIN_AIDEN", "Twin Aiden · Post-Credits", "Callum · Karanlık Gelecek", "Kapıyı aç... zaman kırılmadan önce sana onları nasıl hayatta tutacağını göstereyim.", Color(0xFF818CF8)),
        BookTestVoiceEntry("ZERO", "Sıfır · Yara İzli Aiden", "Callum · Kırık Ayna", "Kendi yüzüne beş yıl sonrasından bakmak garip, değil mi Aiden? Yara izime bakıp korkma — bu kaderi hâlâ değiştirebilirsin.", Color(0xFF38BDF8)),
        BookTestVoiceEntry("VOSS", "Elias Voss · Denek 6-B", "Harry · Sert & Samimi", "Hoş geldin 7-A. Çay demledim — senin öğrettiğin nefes egzersizi sayesinde üç haftadır tek bir cam bile çatlatmadım.", Color(0xFFF87171)),
        BookTestVoiceEntry("STEVE", "Steve Rogers", "Brian · Lider Bariton", "Kalkanımı yolladım Aiden! Gardı kırıldı, şimdi yüklen!", Color(0xFF3B82F6)),
        BookTestVoiceEntry("TONY", "Tony Stark", "Chris · Zeki & Esprili", "Kenara ışınlan evlat — yörünge ışınını tam üstüne indiriyorum!", Color(0xFFF59E0B)),
        BookTestVoiceEntry("THOR", "Thor Odinson", "Adam · Asgard Gücü", "Asgard'ın fırtınası üstüne insin! Hazır mısın Aiden?", Color(0xFF38BDF8)),
        BookTestVoiceEntry("VAELOR", "Yüce Hakem Vaelor", "Adam · Kozmik Lord", "Diz çök Aiden Blackwood! O mavi kıvılcım Soluk Mühür'e aittir!", Color(0xFFF43F5E)),
        BookTestVoiceEntry("ULTRON", "Ultron Prime", "Daniel · Soğuk Yapay Zekâ", "Kuklalarım koparıldı ama bu şehir gökten düşecek Aiden Blackwood! İplerim artık yok!", Color(0xFFEF4444)),
        BookTestVoiceEntry("ARCHITECT", "Asıl Mimar", "Bill · Kadim & Ürpertici", "Birinci perde kapandı, Kayıp Anahtarım... Demir kuklayı kırdın, ama insanların korkusunu kıramazsın.", Color(0xFFFDE047)),
        BookTestVoiceEntry("VISION", "Vision", "George · Dingin & Bilge", "Zihin Taşı'nı odaklıyorum Aiden — zırhı açıldı, şimdi vur!", Color(0xFF34D399)),
        BookTestVoiceEntry("PIETRO", "Pietro Maximoff", "Liam · Hızlı & Enerjik", "Bunu görmedin mi Ultron?! Nöbetçileri temizledim — sıra sende Aiden!", Color(0xFF22D3EE))
    )
}

/**
 * Temporary all-in-one Test Showcase Gallery for inspecting & playing EVERY Book Mode
 * Boss Duel, Embedded Mini-Game, Directed 4-Act Animation, Post-Credits Cutscene,
 * Live 3D Character Encounter, and Story Photo immediately without playing through 24 chapters.
 */
@Composable
fun BookMediaTestShowcaseModal(
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeBossEntry by remember { mutableStateOf<BookTestBossEntry?>(null) }
    var showPostCredits by remember { mutableStateOf(false) }
    var showSeason2PostCredits by remember { mutableStateOf(false) }
    var activeLive3D by remember { mutableStateOf<BookTestLive3DEntry?>(null) }
    var zoomedPhoto by remember { mutableStateOf<BookTestPhotoEntry?>(null) }
    val testBossRanks = remember { mutableStateMapOf<CosmicDuelBossId, String>() }
    val testMiniGameRanks = remember { mutableStateMapOf<StoryMiniGameId, String>() }

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0A0518),
                            Color(0xFF14092C),
                            Color(0xFF090414)
                        )
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("book_media_test_showcase_modal")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "KİTAP MEDYA TEST VİTRİNİ",
                            fontFamily = InterFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
                            color = Color(0xFFFDE047)
                        )
                        Text(
                            text = "Oyunlar, Animasyonlar & Fotolar",
                            fontFamily = DisplaySerifFontFamily,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MorCamTokens.SilverLavenderBright,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "6 Oyun · 7 Animasyon · 10 Canlı 3D · ${BookMediaTestCatalog.photos.size} Foto · 15 Stüdyo Ses",
                            fontFamily = InterFontFamily,
                            fontSize = 11.5.sp,
                            color = Color(0xFFC4B5FD)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0x33A855F7))
                            .border(1.dp, Color(0x66D8B4FE), CircleShape)
                            .testTag("book_test_showcase_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFFF5F3FF)
                        )
                    }
                }

                // Category Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShowcaseTabPill(
                        label = "Oyunlar (6)",
                        icon = Icons.Default.SportsEsports,
                        selected = selectedTab == 0,
                        accent = Color(0xFF38BDF8),
                        tag = "test_showcase_tab_games"
                    ) { selectedTab = 0 }
                    ShowcaseTabPill(
                        label = "Animasyonlar (7)",
                        icon = Icons.Default.Movie,
                        selected = selectedTab == 1,
                        accent = Color(0xFFF59E0B),
                        tag = "test_showcase_tab_animations"
                    ) { selectedTab = 1 }
                    ShowcaseTabPill(
                        label = "Canlı 3D (10)",
                        icon = Icons.Default.ViewInAr,
                        selected = selectedTab == 2,
                        accent = Color(0xFFC084FC),
                        tag = "test_showcase_tab_live3d"
                    ) { selectedTab = 2 }
                    ShowcaseTabPill(
                        label = "Fotolar (${BookMediaTestCatalog.photos.size})",
                        icon = Icons.Default.PhotoLibrary,
                        selected = selectedTab == 3,
                        accent = Color(0xFF34D399),
                        tag = "test_showcase_tab_photos"
                    ) { selectedTab = 3 }
                    ShowcaseTabPill(
                        label = "Sesler (15)",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        selected = selectedTab == 4,
                        accent = Color(0xFFF472B6),
                        tag = "test_showcase_tab_voices"
                    ) { selectedTab = 4 }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Tab Body
                when (selectedTab) {
                    0 -> ShowcaseGamesTab(
                        bossRanks = testBossRanks,
                        miniGameRanks = testMiniGameRanks,
                        onLaunchBoss = { activeBossEntry = it },
                        onMiniGameFinished = { id, rank -> testMiniGameRanks[id] = rank }
                    )
                    1 -> ShowcaseAnimationsTab(
                        onLaunchPostCredits = { showPostCredits = true },
                        onLaunchSeason2PostCredits = { showSeason2PostCredits = true }
                    )
                    2 -> ShowcaseLive3DTab(
                        onLaunchScene = { activeLive3D = it }
                    )
                    3 -> ShowcasePhotosTab(
                        onSelectPhoto = { zoomedPhoto = it }
                    )
                    else -> ShowcaseVoicesTab()
                }
            }
        }
    }

    // Active Boss Duel Launcher
    activeBossEntry?.let { boss ->
        CosmicRooftopBossDuelDialog(
            title = boss.title,
            bossId = boss.bossId,
            pietroSaved = true,
            onVictory = { _, rank ->
                testBossRanks[boss.bossId] = rank
                activeBossEntry = null
            },
            onDismiss = { activeBossEntry = null }
        )
    }

    // Active Post-Credits Launchers (Season 1 & Season 2)
    if (showPostCredits) {
        TwinAidenPostCreditsDialog(
            onContinueToChoicesSummary = { showPostCredits = false }
        )
    }
    if (showSeason2PostCredits) {
        Season2ArchitectPostCreditsDialog(
            onContinueToChoicesSummary = { showSeason2PostCredits = false }
        )
    }

    // Active Live 3D Encounter Stage — Opens Directly in True Edge-to-Edge Fullscreen!
    activeLive3D?.let { entry ->
        LiveCharacterEncounterStage(
            characterKey = entry.characterKey,
            sceneId = entry.sceneId,
            title = entry.title,
            subtitle = entry.subtitle,
            chapterAccent = entry.accent,
            startFullscreen = true,
            onCloseFullscreen = { activeLive3D = null }
        )
    }

    // Photo Fullscreen Lightbox
    zoomedPhoto?.let { photo ->
        val context = LocalContext.current
        Dialog(
            onDismissRequest = { zoomedPhoto = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            ConfigureTrueFullscreenDialogWindow()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF804020A))
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .clickable { zoomedPhoto = null }
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photo.drawableRes)
                            .allowRgb565(true)
                            .crossfade(true)
                            .build(),
                        contentDescription = photo.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.2.dp, Color(0x66D8B4FE), RoundedCornerShape(18.dp))
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = photo.category.uppercase(),
                        fontFamily = InterFontFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFFC084FC)
                    )
                    Text(
                        text = photo.title,
                        fontFamily = DisplaySerifFontFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = { zoomedPhoto = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x5524104A))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowcaseTabPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    accent: Color,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(if (selected) accent.copy(alpha = 0.24f) else Color(0x281B0E36))
            .border(
                width = if (selected) 1.3.dp else 1.dp,
                color = if (selected) accent else Color(0x44C4B5FD),
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .testTag(tag)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) accent else Color(0xFFD8B4FE),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontSize = 12.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else Color(0xFFE9D5FF)
        )
    }
}

@Composable
private fun ShowcaseGamesTab(
    bossRanks: Map<CosmicDuelBossId, String>,
    miniGameRanks: Map<StoryMiniGameId, String>,
    onLaunchBoss: (BookTestBossEntry) -> Unit,
    onMiniGameFinished: (StoryMiniGameId, String) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ShowcaseSectionTitle(
                title = "60fps 3D Boss Düelloları (3)",
                subtitle = "Dokun ve doğrudan tam ekran arenada oyna."
            )
        }
        items(BookMediaTestCatalog.bossDuels, key = { it.bossId.name }) { boss ->
            val earnedRank = bossRanks[boss.bossId]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xD815092B))
                    .border(1.2.dp, boss.accent.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                    .clickable { onLaunchBoss(boss) }
                    .testTag("test_launch_boss_${boss.bossId.name}")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(boss.previewRes)
                            .allowRgb565(true)
                            .build(),
                        contentDescription = boss.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x33080414), Color(0xE0110622))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xCC0D051F))
                            .border(1.dp, boss.accent.copy(alpha = 0.7f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = boss.chapterBadge,
                            fontFamily = InterFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = boss.accent
                        )
                    }
                    if (!earnedRank.isNullOrBlank()) {
                        Text(
                            text = "DERECE: $earnedRank",
                            fontFamily = InterFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF052E16),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF34D399))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(boss.accent)
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF090414),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "DÜELLOYU BAŞLAT",
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF090414)
                        )
                    }
                }
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = boss.title,
                        fontFamily = DisplaySerifFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = boss.subtitle,
                        fontFamily = InterFontFamily,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFFD8CCF2)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            ShowcaseSectionTitle(
                title = "Hikâye İçi Mini Oyunlar (3)",
                subtitle = "Bölüm içi refleks, denge ve irade oyunlarını anında test et."
            )
        }
        items(BookMediaTestCatalog.miniGames, key = { it.gameId.key }) { mini ->
            Column {
                Text(
                    text = mini.chapterBadge,
                    fontFamily = InterFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = mini.gameId.accentColor,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
                InStoryMiniGameCard(
                    gameId = mini.gameId,
                    title = mini.title,
                    subtitle = mini.subtitle,
                    savedBestRank = miniGameRanks[mini.gameId],
                    onCompleted = { rank, _ -> onMiniGameFinished(mini.gameId, rank) }
                )
            }
        }
    }
}

@Composable
private fun ShowcaseAnimationsTab(
    onLaunchPostCredits: () -> Unit,
    onLaunchSeason2PostCredits: () -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ShowcaseSectionTitle(
                title = "Post-Credits Final Sekansları (2 · İnteraktif)",
                subtitle = "1. ve 2. Sezon finalindeki Marvel tarzı sesli, dudak senkronlu yarık sekansları."
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(158.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.4.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                    .clickable(onClick = onLaunchPostCredits)
                    .testTag("test_launch_post_credits_btn")
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(R.drawable.img_postcredits_twin_aiden)
                        .allowRgb565(true)
                        .build(),
                    contentDescription = "Post-Credits: İkiz Aiden",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x30050312), Color(0xEB090418))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "1. SEZON POST-CREDITS · 2. SEZON AÇILIŞI",
                        fontFamily = InterFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "Kırık Zaman Çizgisi · İkiz Aiden (Sıfır)",
                        fontFamily = DisplaySerifFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF38BDF8))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF061020),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "1. SEZON POST-CREDITS OYNAT",
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF061020)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(158.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.4.dp, Color(0xFFFDE047), RoundedCornerShape(20.dp))
                    .clickable(onClick = onLaunchSeason2PostCredits)
                    .testTag("test_launch_s2_post_credits_btn")
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(R.drawable.img_portrait_architect_hero_1795559252160)
                        .allowRgb565(true)
                        .build(),
                    contentDescription = "2. Sezon Post-Credits: Asıl Mimar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x28050312), Color(0xEE090418))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "2. SEZON POST-CREDITS · 3. SEZON AÇILIŞI",
                        fontFamily = InterFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE047)
                    )
                    Text(
                        text = "Sokovia Enkazı · Asıl Mimar & Anlaşmalar",
                        fontFamily = DisplaySerifFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFFDE047))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF140D02),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "2. SEZON POST-CREDITS OYNAT",
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF140D02)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            ShowcaseSectionTitle(
                title = "Yönetmenli 4 Perdeli Sinematik Sahneler (5)",
                subtitle = "Her sahnenin sağ üstünden tam ekran 4 perdeli IMAX modunu açabilirsin."
            )
        }
        items(BookMediaTestCatalog.animations, key = { it.sceneId }) { anim ->
            Column {
                Text(
                    text = anim.chapterBadge,
                    fontFamily = InterFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = anim.accent,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
                InStoryAnimatedScene(
                    sceneId = anim.sceneId,
                    caption = anim.title,
                    subtitle = anim.subtitle,
                    chapterAccent = anim.accent
                )
            }
        }
    }
}

@Composable
private fun ShowcaseLive3DTab(
    onLaunchScene: (BookTestLive3DEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ShowcaseSectionTitle(
                title = "Canlı 3D Karakter Karşılaşmaları (10)",
                subtitle = "2. Sezon'daki tüm etkileşimli 3D karakter sahnelerini tek dokunuşla aç."
            )
        }
        items(BookMediaTestCatalog.live3DEncounters, key = { it.id }) { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xD6170A30))
                    .border(1.1.dp, entry.accent.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                    .clickable { onLaunchScene(entry) }
                    .testTag("test_launch_live3d_${entry.id}")
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(entry.accent.copy(alpha = 0.2f))
                        .border(1.dp, entry.accent.copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = null,
                        tint = entry.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${entry.chapterBadge} · ${entry.characterKey}",
                        fontFamily = InterFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = entry.accent
                    )
                    Text(
                        text = entry.title,
                        fontFamily = DisplaySerifFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entry.subtitle,
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFFD0C2EC),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(entry.accent.copy(alpha = 0.22f))
                        .border(1.dp, entry.accent, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "AÇ",
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowcasePhotosTab(
    onSelectPhoto: (BookTestPhotoEntry) -> Unit
) {
    val context = LocalContext.current
    val grouped = remember { BookMediaTestCatalog.photos.groupBy { it.category } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        grouped.forEach { (category, list) ->
            item(key = "cat_$category") {
                ShowcaseSectionTitle(
                    title = "$category (${list.size})",
                    subtitle = "Büyütmek için herhangi bir görsele dokun."
                )
            }
            val rows = list.chunked(2)
            items(rows, key = { pair -> pair.joinToString("_") { it.id } }) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { photo ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xD0160A2E))
                                .border(1.dp, Color(0x55C4B5FD), RoundedCornerShape(16.dp))
                                .clickable { onSelectPhoto(photo) }
                                .testTag("test_photo_${photo.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.25f)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(photo.drawableRes)
                                        .allowRgb565(true)
                                        .build(),
                                    contentDescription = photo.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xAA0A0518)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Büyüt",
                                        tint = Color(0xFFE9D5FF),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Text(
                                text = photo.title,
                                fontFamily = InterFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF3E8FF),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowcaseVoicesTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val installedPacks by com.example.book.BookContentPackManager.installedPacks.collectAsState()
    val activeDownload by com.example.book.BookContentPackManager.activeDownload.collectAsState()
    var statusNote by remember { mutableStateOf<String?>(null) }
    var playingSpeaker by remember { mutableStateOf<String?>(null) }
    val voiceEngine = remember {
        com.example.ui.screens.cosmic3d.CosmicCharacterVoiceEngine(context) { speaking ->
            if (!speaking) playingSpeaker = null
        }
    }

    DisposableEffect(Unit) {
        com.example.book.BookContentPackManager.ensureLoaded(context)
        onDispose { voiceEngine.stopAndRelease() }
    }

    val isPackInstalled = remember(installedPacks) {
        com.example.book.BookContentPackManager.isStudioVoicePackInstalled(context)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ShowcaseSectionTitle(
                title = "ElevenLabs Türkçe Stüdyo Ses Paketi",
                subtitle = "İsteğe bağlı indirilebilir 119 replik (6.6 MB) — APK'yı şişirmeden çevrimdışı çalışır."
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF1A1034))
                    .border(
                        1.dp,
                        if (isPackInstalled) Color(0xFF34D399).copy(alpha = 0.6f) else Color(0xFFF472B6).copy(alpha = 0.55f),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isPackInstalled) {
                        "✅ ElevenLabs Stüdyo Dublaj Paketi Kurulu (119 replik • Çevrimdışı Hazır)"
                    } else {
                        "⬇ İsteğe Bağlı ElevenLabs Stüdyo Paketi Kurulu Değil (6.6 MB)"
                    },
                    fontFamily = InterFontFamily,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPackInstalled) Color(0xFF6EE7B7) else Color(0xFFFBCFE8)
                )

                activeDownload?.let { prog ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = prog.phaseLabel,
                            fontFamily = InterFontFamily,
                            fontSize = 11.5.sp,
                            color = Color(0xFFE9D5FF)
                        )
                        if (prog.fraction >= 0f) {
                            LinearProgressIndicator(
                                progress = { prog.fraction },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFF472B6),
                                trackColor = Color(0x33FFFFFF)
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFF472B6),
                                trackColor = Color(0x33FFFFFF)
                            )
                        }
                    }
                }

                statusNote?.let { note ->
                    Text(
                        text = note,
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFFDE68A)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF7C3AED))
                            .clickable(enabled = activeDownload == null) {
                                statusNote = null
                                scope.launch {
                                    val res = com.example.book.BookContentPackManager.downloadStudioVoicePack(context)
                                    statusNote = res.fold(
                                        onSuccess = { "✓ 119 ElevenLabs Türkçe stüdyo sesi indirildi ve aktif!" },
                                        onFailure = { "⚠ İndirme başarısız: ${it.message ?: "Bağlantı hatası"}" }
                                    )
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPackInstalled) "Paketi Yenile" else "Stüdyo Sesleri İndir (6.6 MB)",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (isPackInstalled) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x44EF4444))
                                .border(0.8.dp, Color(0x99F87171), RoundedCornerShape(12.dp))
                                .clickable(enabled = activeDownload == null) {
                                    scope.launch {
                                        voiceEngine.stopCurrentLine()
                                        com.example.book.BookContentPackManager.deletePack(
                                            context,
                                            com.example.book.BookContentPackManager.STUDIO_VOICE_PACK_ID
                                        )
                                        statusNote = "Paket silindi; ücretsiz yedek ses motoruna dönüldü."
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        items(BookMediaTestCatalog.studioVoices, key = { it.speakerKey }) { entry ->
            val isPlayingThis = playingSpeaker == entry.speakerKey
            val hashKey = remember(entry.speakerKey, entry.sampleLine) {
                com.example.ui.screens.cosmic3d.CosmicCharacterVoiceEngine
                    .studioVoiceAssetFileName(entry.speakerKey, entry.sampleLine)
                    .removeSuffix(".mp3")
            }
            val hasDownloadedClip = remember(installedPacks, isPackInstalled, hashKey) {
                com.example.book.BookContentPackManager.findDownloadedVoiceFile(context, hashKey) != null
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF160D2D))
                    .border(
                        1.dp,
                        if (isPlayingThis) entry.accent else entry.accent.copy(alpha = 0.35f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        if (isPlayingThis) {
                            voiceEngine.stopCurrentLine()
                            playingSpeaker = null
                        } else {
                            playingSpeaker = entry.speakerKey
                            voiceEngine.speak(entry.speakerKey, entry.sampleLine)
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(entry.accent.copy(alpha = if (isPlayingThis) 0.35f else 0.18f))
                        .border(1.dp, entry.accent.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlayingThis) "Durdur" else "Dinle",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = entry.displayName,
                            fontFamily = InterFontFamily,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (hasDownloadedClip) "🎙 ElevenLabs" else "☁ Neural Yedek",
                            fontFamily = InterFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (hasDownloadedClip) Color(0xFF6EE7B7) else Color(0xFFC4B5FD)
                        )
                    }
                    Text(
                        text = entry.voiceActorLabel,
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        color = entry.accent
                    )
                    Text(
                        text = "\"${entry.sampleLine}\"",
                        fontFamily = InterFontFamily,
                        fontSize = 11.5.sp,
                        color = Color(0xFFE2D9F3),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowcaseSectionTitle(
    title: String,
    subtitle: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontFamily = DisplaySerifFontFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = MorCamTokens.SilverLavenderBright
        )
        Text(
            text = subtitle,
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            color = Color(0xFFB7A6DF)
        )
    }
}
