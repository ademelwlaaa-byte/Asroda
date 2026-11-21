package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.book.BookCharacterId
import com.example.book.BookReadState
import com.example.book.CosmicDriftBookRegistry
import com.example.book.RecordedDecision
import com.example.ui.animation.cosmicTapSpring
import com.example.ui.components.MorCamButtonVariant
import com.example.ui.components.MorCamCapsuleButton
import com.example.ui.components.MorCamDivider
import com.example.ui.components.MorCamTokens
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily

/**
 * Floating Life is Strange-style atmospheric whisper shown briefly after a choice.
 * Intentionally hides all numeric character points / stat deltas until the Chapter End Screen.
 */
@Composable
fun BookConsequenceBanner(
    decision: RecordedDecision?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && decision != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
        modifier = modifier
    ) {
        if (decision != null) {
            val (whisperText, whisperTint) = when {
                decision.isBranching -> "Bu seçim hikâyenin yolunu değiştirdi..." to Color(0xFFFEF08A)
                decision.secretUnlocked != null -> "Karanlıkta önemli bir iz yakaladın..." to Color(0xFFFDE047)
                decision.isRelationship -> "Bu an hatırlanacak..." to Color(0xFFF9A8D4)
                else -> "Seçimin yankı bırakacak..." to Color(0xFFD8B4FE)
            }
            Box(
                modifier = Modifier
                    .morCamCosmicGlow(cornerRadius = 50.dp, glowColor = whisperTint, alpha = 0.30f, blurDp = 12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xE81C0B3C), Color(0xF0120729), Color(0xE81C0B3C))
                        )
                    )
                    .border(0.95.dp, whisperTint.copy(alpha = 0.55f), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = whisperTint,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = whisperText,
                        fontFamily = LiterarySerifFontFamily,
                        color = Color(0xFFFAF6FF),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private data class CharacterLoreEntry(
    val id: BookCharacterId,
    val codename: String,
    val bio: String,
    val quote: String
)

private val CORE_CHARACTER_CODEX = listOf(
    CharacterLoreEntry(
        id = BookCharacterId.STEVE,
        codename = "KAPTAN AMERİKA · SAHA LİDERİ",
        bio = "S.H.I.E.L.D.'in seni tek kelimelik bir tehdit dosyasına sıkıştırmasına karşı çıkan, sözüne sadık ve saha disiplinini her şeyin üstünde tutan lider.",
        quote = "“Seni kilitlemek için çağırmadık. Hikâyenin diğer yarısını dinlemek istiyoruz.”"
    ),
    CharacterLoreEntry(
        id = BookCharacterId.NATASHA,
        codename = "BLACK WIDOW · GÖLGE STRATEJİST",
        bio = "Karanlık koridorlardan geçmiş keskin bir gözlemci. Söylenmeyen kelimeleri, tereddütleri ve kaçış planlarını yarım saniyede okur.",
        quote = "“Kaçmayı zaten ustalıkla biliyorsun. Asıl zor olan bir gece kalmak.”"
    ),
    CharacterLoreEntry(
        id = BookCharacterId.WANDA,
        codename = "KIZIL YANKI · ZİHİNSEL EMPATİ",
        bio = "Yalnızlığın ve duvarların ardında yaşamanın ağırlığını bilen tek kişi. Zihinsel frekansın Kule'deki kızıl enerjisiyle beklenmedik biçimde yankılanıyor.",
        quote = "“Zihnindeki duvarların nasıl örüldüğünü tanıyorum. Yalnız değilsin.”"
    ),
    CharacterLoreEntry(
        id = BookCharacterId.TONY,
        codename = "IRON MAN · KULE MİMARI",
        bio = "Yirmi yıl önceki mavi anomalinin kuantum izini çözen tek dâhi. Alaycı mizahının altında seni Fury'den önce masaya çekmeye çalışan keskin bir zekâ var.",
        quote = "“Fury seni çelik bir odaya kilitlemeden önce masaya çağırmak istedim.”"
    ),
    CharacterLoreEntry(
        id = BookCharacterId.CLINT,
        codename = "HAWKEYE · KESKİN GÖZ",
        bio = "Kule'deki en ağır gerginlikleri bile tek bir kuru espriyle yumuşatan, tehlike anında gözünü kırpmadan sırtını kollayan saha ortağı.",
        quote = "“Burada tavan sağlam, kahve de bedava. En azından oturup konuşmaya değer.”"
    ),
    CharacterLoreEntry(
        id = BookCharacterId.BRUCE,
        codename = "DR. BANNER · KUANTUM & DENGE",
        bio = "İçinde kontrol edilmesi gereken yıkıcı bir güçle yaşamanın ne demek olduğunu bilen bilim insanı. Enerji imzanı bir silah değil, bir insan hikâyesi olarak görüyor.",
        quote = "“O mavi ışık sönmedi; bir çocuğun sinir sistemine yerleşti.”"
    ),
    CharacterLoreEntry(
        id = BookCharacterId.THOR,
        codename = "ASGARD PRENSİ · KOZMİK TANIK",
        bio = "Dokuz Diyar'ın kadim enerjilerini tanıyan savaşçı. Gücündeki kozmik izi dünyalı cihazlardan çok daha derinden seziyor.",
        quote = "“Gözlerindeki ışık bu dünyaya ait değil — ama iraden senin.”"
    )
)

/**
 * Spoiler-Free In-Universe "Aiden'ın Dosyası & Evren Rehberi" Modal.
 * Replaces the old numeric stat/bond percentage screen with an atmospheric
 * 3-tab dossier: Aiden'ın Profili & Güç Notları, 2012 Avengers + Wanda Karakter Rehberi,
 * and Keşfedilen Sırlar & İzler Arşivi.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookChronicleModal(
    state: BookReadState,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val activeMeta = remember(state.activeChapter) {
        CosmicDriftBookRegistry.chapterMeta(state.activeChapter)
    }
    val currentNode = remember(state.currentNodeId) {
        CosmicDriftBookRegistry.node(state.currentNodeId)
    }
    val accentColor = Color(activeMeta.accentHex)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .morCamCosmicGlow(cornerRadius = 26.dp, glowColor = accentColor, alpha = 0.42f, blurDp = 16.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(MorCamTokens.GlassSurfaceTop, MorCamTokens.GlassSurfaceMid, MorCamTokens.GlassSurfaceBottom)
                    )
                )
                .border(1.3.dp, MorCamTokens.RimGradient, RoundedCornerShape(26.dp))
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aiden'ın Dosyası & Evren",
                            fontFamily = DisplaySerifFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "S.H.I.E.L.D. Arşiv Kodu · 05-BLUE  •  Bölüm ${activeMeta.romanNumeral}",
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderMuted,
                            fontSize = 11.2.sp
                        )
                    }
                    MorCamCloseOrb(onClick = onDismiss, tag = "book_chronicle_close")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Immersive Tabs: Aiden • Karakterler • Arşiv
                val tabs = listOf("Aiden", "Karakterler", "Sır Arşivi")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x55180A32))
                        .border(0.9.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(50))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabs.forEachIndexed { index, label ->
                        val active = selectedTab == index
                        val tabInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .cosmicTapSpring(tabInteraction, pressedScale = 0.96f)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (active) Brush.horizontalGradient(
                                        listOf(accentColor.copy(alpha = 0.55f), Color(0xAA4C1D95))
                                    ) else Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color.Transparent)
                                    )
                                )
                                .border(
                                    width = if (active) 0.9.dp else 0.dp,
                                    color = if (active) accentColor.copy(alpha = 0.85f) else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable(interactionSource = tabInteraction, indication = null) {
                                    selectedTab = index
                                }
                                .testTag("dossier_tab_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = InterFontFamily,
                                color = if (active) Color.White else MorCamTokens.LavenderMuted,
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "dossierTabSwitch"
                ) { tab ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 460.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (tab) {
                            // ── TAB 0: AIDEN BLACKWOOD FIELD DOSSIER ──
                            0 -> {
                                // Hero Portrait + Identity Card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0x8A2B1259), Color(0x7815082E))
                                            )
                                        )
                                        .border(1.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                                        .padding(13.dp),
                                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 76.dp, height = 102.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(1.2.dp, accentColor.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_aiden_portrait),
                                            contentDescription = "Aiden Blackwood",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = "ADAY · GİZLİ DOSYA",
                                            fontFamily = InterFontFamily,
                                            color = Color(0xFFFEF08A),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp
                                        )
                                        Text(
                                            text = "Aiden Blackwood",
                                            fontFamily = DisplaySerifFontFamily,
                                            color = Color.White,
                                            fontSize = 18.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "25 yaşında • 20 yıldır kaçakta. Keskin hatları, buz mavisi gözleri ve sessiz karizmasıyla girdiği her odada bakışları üzerine çeken, beş yaşındayken Manhattan göğünü yaran mavi ışığın tek tanığı.",
                                            fontFamily = InterFontFamily,
                                            color = MorCamTokens.LavenderLight,
                                            fontSize = 11.8.sp,
                                            lineHeight = 16.5.sp
                                        )
                                    }
                                }

                                // Current Scene Location Card
                                DossierLoreCard(
                                    title = "AKTİF KONUM & SAHNE",
                                    accent = accentColor,
                                    body = "${currentNode?.locationTag ?: activeMeta.subtitle}\n“${currentNode?.sceneTitle ?: activeMeta.title}” — ${activeMeta.subtitle}"
                                )

                                // Powers & Relics Lore (no spoilers / no numeric meters)
                                DossierLoreCard(
                                    title = "ANLIK IŞINLANMA",
                                    accent = Color(0xFF38BDF8),
                                    body = "Portal veya geçit açmaz; zihninde sabitlediği noktaya göz kırpması kadar kısa, ince mavi kıvılcımlı bir sıçramayla doğrudan ışınlanır."
                                )
                                DossierLoreCard(
                                    title = "BEYAZ GÖZ & KIRMIZI GÖZ",
                                    accent = Color(0xFFF472B6),
                                    body = "On beşinde uyanan Beyaz Göz zamanı ve zihin sınırlarını soğuk berraklıkla okur. On yedisinde alevlenen Kırmızı Göz ise kontrolü kaybettiğinde yıkıcı bir kozmik fırtınaya dönüşür."
                                )
                                DossierLoreCard(
                                    title = "ANTİKA GÜMÜŞ MADALYON",
                                    accent = Color(0xFFFEF08A),
                                    body = "Beş yaşındaki o mavi geceden ve ailesinden geriye kalan tek fiziksel hatıra. Karanlıkta nefesini sabitleyen sessiz çapa."
                                )
                            }

                            // ── TAB 1: 2012 AVENGERS + WANDA CHARACTER CODEX ──
                            1 -> {
                                Text(
                                    text = "Hikâyede karşılaştığın 2012 Avengers kadrosu ve Wanda Maximoff hakkında saha gözlemleri. Kararlarının gerçek sonuçlarını bölüm sonu ekranında göreceksin.",
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.LavenderMuted,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.5.sp
                                )
                                CORE_CHARACTER_CODEX.forEach { entry ->
                                    val cAccent = Color(entry.id.accentHex)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0x521E0D3E))
                                            .border(0.9.dp, cAccent.copy(alpha = 0.48f), RoundedCornerShape(18.dp))
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(cAccent.copy(alpha = 0.22f))
                                                    .border(0.9.dp, cAccent.copy(alpha = 0.75f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = cAccent,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = entry.id.displayName,
                                                    fontFamily = DisplaySerifFontFamily,
                                                    color = Color.White,
                                                    fontSize = 15.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = entry.codename,
                                                    fontFamily = InterFontFamily,
                                                    color = cAccent,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.9.sp
                                                )
                                            }
                                        }
                                        Text(
                                            text = entry.bio,
                                            fontFamily = InterFontFamily,
                                            color = MorCamTokens.SilverLavenderBright,
                                            fontSize = 12.sp,
                                            lineHeight = 17.5.sp
                                        )
                                        Text(
                                            text = entry.quote,
                                            fontFamily = LiterarySerifFontFamily,
                                            fontStyle = FontStyle.Italic,
                                            color = cAccent.copy(alpha = 0.92f),
                                            fontSize = 12.5.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }

                            // ── TAB 2: DISCOVERED SECRETS & STORY PATHS ARCHIVE ──
                            else -> {
                                // Discovered Secrets Section
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0x4D200E42))
                                        .border(0.9.dp, Color(0x55FDE047), RoundedCornerShape(18.dp))
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(9.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFFFDE047),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "AÇIĞA ÇIKAN GİZLİ İZLER (${state.discoveredSecrets.size})",
                                            fontFamily = InterFontFamily,
                                            color = Color(0xFFFEF08A),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    if (state.discoveredSecrets.isEmpty()) {
                                        Text(
                                            text = "Henüz gizli bir istihbarat kaydı açığa çıkmadı. Meraklı ve riskli kararların karanlıktaki izleri ortaya çıkaracak.",
                                            fontFamily = InterFontFamily,
                                            color = MorCamTokens.LavenderMuted,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    } else {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                                            verticalArrangement = Arrangement.spacedBy(7.dp)
                                        ) {
                                            state.discoveredSecrets.forEach { secret ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .background(Color(0x2AFDE047))
                                                        .border(0.8.dp, Color(0x77FDE047), RoundedCornerShape(50))
                                                        .padding(horizontal = 11.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "✦ $secret",
                                                        fontFamily = InterFontFamily,
                                                        color = Color(0xFFFEF9C3),
                                                        fontSize = 11.8.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Chosen Story Paths
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0x4D200E42))
                                        .border(0.9.dp, Color(0x66C084FC), RoundedCornerShape(18.dp))
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(9.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Route,
                                            contentDescription = null,
                                            tint = Color(0xFFD8B4FE),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "SEÇİLEN HİKÂYE YOLLARI (${state.discoveredBranches.size})",
                                            fontFamily = InterFontFamily,
                                            color = MorCamTokens.LavenderLight,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    if (state.discoveredBranches.isEmpty()) {
                                        Text(
                                            text = "Kritik dönüm noktalarındaki kararların hikâyeyi farklı sahnelere dallandıracak.",
                                            fontFamily = InterFontFamily,
                                            color = MorCamTokens.LavenderMuted,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    } else {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                                            verticalArrangement = Arrangement.spacedBy(7.dp)
                                        ) {
                                            state.discoveredBranches.forEach { branch ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .background(Color(0x33A855F7))
                                                        .border(0.8.dp, Color(0x88D8B4FE), RoundedCornerShape(50))
                                                        .padding(horizontal = 11.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "⑂ $branch",
                                                        fontFamily = InterFontFamily,
                                                        color = MorCamTokens.SilverLavenderBright,
                                                        fontSize = 11.8.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Atmospheric reminder about Chapter End Screen
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0x3D180A34))
                                        .border(0.8.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(16.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Karakter bayrakları ve seçim sonuçları okuma keyfini bozmamak için yalnızca “Bölümü Bitir” ekranında açılır.",
                                        fontFamily = InterFontFamily,
                                        fontStyle = FontStyle.Italic,
                                        color = MorCamTokens.LavenderSoft,
                                        fontSize = 11.2.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DossierLoreCard(
    title: String,
    accent: Color,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x4D1E0D3D))
            .border(0.85.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = title,
            fontFamily = InterFontFamily,
            color = accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
        )
        Text(
            text = body,
            fontFamily = InterFontFamily,
            color = MorCamTokens.SilverLavenderBright,
            fontSize = 12.2.sp,
            lineHeight = 18.sp
        )
    }
}

/**
 * In-reader Mor Cam Chapter Switcher Modal — allows jumping directly to any unlocked chapter.
 */
@Composable
fun InReaderChapterSwitcherDialog(
    state: BookReadState,
    onDismiss: () -> Unit,
    onSelectChapter: (Int) -> Unit
) {
    val maxUnlocked = (state.highestCompletedChapter + 1)
        .coerceIn(1, CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 22.dp)
                .morCamCosmicGlow(cornerRadius = 26.dp, glowColor = MorCamTokens.AmethystGlow, alpha = 0.42f, blurDp = 16.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(MorCamTokens.GlassSurfaceTop, MorCamTokens.GlassSurfaceMid, MorCamTokens.GlassSurfaceBottom)
                    )
                )
                .border(1.3.dp, MorCamTokens.RimGradient, RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bölümler",
                            fontFamily = DisplaySerifFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tamamlanan: ${state.completedChapters.size} / ${CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS}",
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderMuted,
                            fontSize = 12.sp
                        )
                    }
                    MorCamCloseOrb(onClick = onDismiss, tag = "reader_chapters_close")
                }

                Spacer(modifier = Modifier.height(12.dp))
                MorCamDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 430.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    CosmicDriftBookRegistry.chapters.forEach { chapter ->
                        val isCompleted = chapter.number in state.completedChapters
                        val isActive = chapter.number == state.activeChapter
                        val isUnlocked = chapter.number <= maxUnlocked || isCompleted || isActive
                        val accent = Color(chapter.accentHex)
                        val interaction = remember { MutableInteractionSource() }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .cosmicTapSpring(interaction, pressedScale = 0.97f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when {
                                        isActive -> Brush.horizontalGradient(listOf(Color(0x703D1F7A), Color(0x55200E46)))
                                        isCompleted -> Brush.horizontalGradient(listOf(Color(0x4A064E3B), Color(0x45140A2B)))
                                        isUnlocked -> Brush.horizontalGradient(listOf(Color(0x4A26124B), Color(0x4515092D)))
                                        else -> Brush.horizontalGradient(listOf(Color(0x30170C30), Color(0x30110722)))
                                    }
                                )
                                .border(
                                    width = if (isActive) 1.25.dp else 0.9.dp,
                                    brush = when {
                                        isActive -> Brush.linearGradient(listOf(Color(0xEEF5EEFF), accent))
                                        isCompleted -> Brush.linearGradient(listOf(Color(0x9934D399), Color(0x55059669)))
                                        isUnlocked -> Brush.linearGradient(listOf(Color(0x77C4B5FD), Color(0x447C3AED)))
                                        else -> Brush.linearGradient(listOf(Color(0x337C3AED), Color(0x223B1E74)))
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(
                                    enabled = isUnlocked,
                                    interactionSource = interaction,
                                    indication = null
                                ) { onSelectChapter(chapter.number) }
                                .testTag("reader_chapter_item_${chapter.number}")
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCompleted -> Color(0xFF10B981)
                                            isActive -> accent
                                            isUnlocked -> accent.copy(alpha = 0.35f)
                                            else -> Color(0x333B226E)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isCompleted -> Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Tamamlandı",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    !isUnlocked -> Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Kilitli",
                                        tint = Color(0x88C4B5FD),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    else -> Text(
                                        text = "${chapter.number}",
                                        fontFamily = DisplaySerifFontFamily,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bölüm ${chapter.number}: ${chapter.title}",
                                    fontFamily = LiterarySerifFontFamily,
                                    color = if (isUnlocked) MorCamTokens.SilverLavenderBright else Color(0x88B4A0D8),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${chapter.sceneCount} Sahne • ${chapter.decisionCount} Karar • ~${chapter.estimatedMinutes} dk",
                                    fontFamily = InterFontFamily,
                                    color = if (isActive) accent else MorCamTokens.LavenderMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(accent.copy(alpha = 0.22f))
                                        .border(0.8.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(50))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Aktif",
                                        fontFamily = InterFontFamily,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MorCamTokens.LavenderSoft,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean Mor Cam dialog to either restart the current chapter or reset the entire book.
 */
@Composable
fun BookResetOptionsDialog(
    chapterNumber: Int,
    onDismiss: () -> Unit,
    onRestartChapter: () -> Unit,
    onResetEntireBook: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .morCamCosmicGlow(cornerRadius = 24.dp, glowColor = MorCamTokens.AmethystGlow, alpha = 0.42f, blurDp = 14.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(MorCamTokens.GlassSurfaceTop, MorCamTokens.GlassSurfaceMid, MorCamTokens.GlassSurfaceBottom)
                    )
                )
                .border(1.3.dp, MorCamTokens.RimGradient, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x4D7C3AED))
                            .border(1.dp, MorCamTokens.RimGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MorCamTokens.SilverLavenderBright,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Yeniden Başlat",
                            fontFamily = DisplaySerifFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Farklı bir dallanma yolunu keşfet",
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = "Yalnızca Bölüm $chapterNumber'i başa sararak önceki bağlarını koruyabilir veya tüm kitabı Bölüm 1'den sıfırlayabilirsin.",
                    fontFamily = InterFontFamily,
                    color = MorCamTokens.LavenderLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                MorCamCapsuleButton(
                    text = "Bölüm $chapterNumber'i Yeniden Oyna",
                    onClick = onRestartChapter,
                    variant = MorCamButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth().testTag("reset_chapter_only_btn")
                )
                MorCamCapsuleButton(
                    text = "Tüm Kitabı Sıfırla (Bölüm 1)",
                    onClick = onResetEntireBook,
                    variant = MorCamButtonVariant.Destructive,
                    modifier = Modifier.fillMaxWidth().testTag("reset_entire_book_btn")
                )
                MorCamCapsuleButton(
                    text = "Vazgeç",
                    onClick = onDismiss,
                    variant = MorCamButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth().testTag("reset_cancel_btn")
                )
            }
        }
    }
}

@Composable
private fun MorCamCloseOrb(
    onClick: () -> Unit,
    tag: String
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(38.dp)
            .cosmicTapSpring(interaction, pressedScale = 0.88f)
            .clip(CircleShape)
            .background(Color(0x552C1654))
            .border(1.dp, Color(0x99D8B4FE), CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Kapat",
            tint = MorCamTokens.SilverLavenderBright,
            modifier = Modifier.size(17.dp)
        )
    }
}
