package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import com.example.book.BookStateRepository
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
 * Floating Telltale / Life is Strange-style cinematic reaction card shown briefly after a choice.
 * Surfaces character-specific dynamic reactions, unlocked secret alerts, branch badges, and a
 * concise narrative echo so every choice feels immediately tangible and alive.
 */
@Composable
fun BookConsequenceBanner(
    decision: RecordedDecision?,
    visible: Boolean,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && decision != null,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        if (decision != null) {
            val topChar = remember(decision) {
                decision.bondDeltas.entries.maxByOrNull { kotlin.math.abs(it.value) }
            }
            val (whisperText, whisperTint) = remember(decision, topChar) {
                when {
                    decision.secretUnlocked != null ->
                        "Gizli İz Açıldı · ${decision.secretUnlocked}" to Color(0xFFFDE047)
                    topChar != null && topChar.value > 0 -> {
                        val line = when (topChar.key) {
                            BookCharacterId.NATASHA -> "Natasha bu anı unutmayacak..."
                            BookCharacterId.WANDA -> "Wanda zihnindeki yankıyı hissetti..."
                            BookCharacterId.STEVE -> "Steve sana olan güvenini tazeledi..."
                            BookCharacterId.TONY -> "Tony bu hamleyi dikkatle not etti..."
                            BookCharacterId.THOR -> "Thor savaşçı saygısıyla başını eğdi..."
                            BookCharacterId.BRUCE -> "Bruce kontrolüne hayran kaldı..."
                            BookCharacterId.CLINT -> "Clint sırtını sana yasladı..."
                            BookCharacterId.FURY -> "Nick Fury dosyanı onaylayarak kapattı..."
                            BookCharacterId.VOSS -> "Voss sana olan borcunu ve kardeşliğini hissetti..."
                            BookCharacterId.MIRA -> "Mira senin sayende kurduğu yeni hayata tutundu..."
                            BookCharacterId.PIETRO -> "Pietro bu hamleni hızıyla selamladı..."
                            BookCharacterId.ZERO -> "Sıfır'ın gözlerinde ilk kez umut parladı..."
                            BookCharacterId.VISION -> "Vision sözlerindeki hakikati kaydetti..."
                        }
                        line to Color(topChar.key.accentHex)
                    }
                    topChar != null && topChar.value < 0 ->
                        "${topChar.key.shortName} bu hamleni unutmayacak..." to Color(0xFFFCA5A5)
                    decision.isBranching -> "Bu seçim hikâyenin yolunu değiştirdi..." to Color(0xFFFEF08A)
                    decision.isRelationship -> "Bu an hatırlanacak..." to Color(0xFFF9A8D4)
                    else -> "Seçimin yankı bırakacak..." to Color(0xFFD8B4FE)
                }
            }
            val isCritical = decision.impactLevel.equals("kritik", ignoreCase = true)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .morCamCosmicGlow(cornerRadius = 20.dp, glowColor = whisperTint, alpha = 0.36f, blurDp = 14.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xF21E0C42), Color(0xF5110626))
                        )
                    )
                    .border(
                        width = if (isCritical) 1.25.dp else 0.95.dp,
                        color = whisperTint.copy(alpha = if (isCritical) 0.72f else 0.55f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
                    .padding(horizontal = 15.dp, vertical = 11.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (topChar != null) {
                            CinematicCharacterAvatar(
                                key = CharacterPortraitKey.fromBookCharacter(topChar.key),
                                size = 34.dp,
                                borderWidth = 1.2.dp,
                                borderColor = whisperTint
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = whisperTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = whisperText,
                            fontFamily = LiterarySerifFontFamily,
                            color = Color(0xFFFAF6FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isCritical || !decision.branchTag.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(whisperTint.copy(alpha = 0.16f))
                                    .border(0.7.dp, whisperTint.copy(alpha = 0.5f), RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = decision.branchTag ?: "KRİTİK",
                                    fontFamily = InterFontFamily,
                                    color = whisperTint,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.4.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    if (decision.impactText.isNotBlank()) {
                        Text(
                            text = decision.impactText,
                            fontFamily = InterFontFamily,
                            color = Color(0xFFD8CCF2),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Subtle floating toast when a newly encountered character joins the Codex & Flag system.
 */
@Composable
fun BookCharacterUnlockedBanner(
    character: BookCharacterId?,
    visible: Boolean,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && character != null,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        if (character != null) {
            val tint = Color(character.accentHex)
            Row(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .morCamCosmicGlow(cornerRadius = 50.dp, glowColor = tint, alpha = 0.38f, blurDp = 12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xF21F0E45), Color(0xF5120729))
                        )
                    )
                    .border(1.1.dp, tint.copy(alpha = 0.75f), RoundedCornerShape(50))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
                    .padding(start = 8.dp, end = 16.dp, top = 7.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CinematicCharacterAvatar(
                    key = CharacterPortraitKey.fromBookCharacter(character),
                    size = 34.dp,
                    borderWidth = 1.3.dp,
                    borderColor = tint
                )
                Column {
                    Text(
                        text = "✦ KARAKTER DOSYASINA EKLENDİ",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFFEF08A),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.9.sp
                    )
                    Text(
                        text = "${character.displayName} · ${character.roleTitle}",
                        fontFamily = LiterarySerifFontFamily,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
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
    ),
    CharacterLoreEntry(
        id = BookCharacterId.FURY,
        codename = "S.H.I.E.L.D. DİREKTÖRÜ · GÖLGE STRATEJİ",
        bio = "Yirmi yıl boyunca Aiden'ın dosyasını elinde tutan ama Konsey'in karanlık planlarına boyun eğmeyip gerçeğin peşine düşen kurt komutan.",
        quote = "“Dünyayı kurtarmak için bazen kuralları yıkan tek bir adam gerekir.”"
    ),
    CharacterLoreEntry(
        id = BookCharacterId.VOSS,
        codename = "DENEK 6-B · GÖLGE KORUYUCU",
        bio = "Proje Ayna'nın Kırmızı Göz'le yaşayan en eski hayatta kalanı. Aiden'ın onu Raft kafesinden çekip çıkarmasıyla hayatı değişti; şimdi Aiden'ın kurduğu güvenli sığınakta öfkesini kontrol etmeyi öğreniyor ve sahada ekibin gölge gözcüsü oluyor.",
        quote = "“Bana 'canavar' yerine ilk kez 'kardeşim' diyen adamı asla yalnız bırakmam, 7-A.”"
    ),
    CharacterLoreEntry(
        id = BookCharacterId.MIRA,
        codename = "DENEK 8-C · DENGELENMİŞ YANKI",
        bio = "İstanbul Boğazı'nda Dr. Evelyn Hale'in elinde büyüyen en genç denek. Aiden'ın onu yok etmek yerine kucaklamasıyla kendi kimliğini buldu; artık hem sivilleri koruyan güçlü bir müttefik hem de Aiden'ın koruyucu kanadı altındaki küçük kız kardeşi.",
        quote = "“Bana gücümün bir lanet değil kalkan olabileceğini sen öğrettin, Aiden.”"
    ),
    CharacterLoreEntry(
        id = BookCharacterId.PIETRO,
        codename = "QUICKSILVER · GÜMÜŞ ŞİMŞEK",
        bio = "Wanda'nın Sokovia'da kaybettiği sandığı ikiz kardeşi. Ses hızını aşan refleksleri, sivri dili ve ablasını korumak için dünyayı yakabilecek ateşli kalbiyle oyunun dengesini değiştiriyor.",
        quote = "“Bunu görmedin mi? Sen ışınlanıyorsun, ben zamanı geçiyorum.”"
    ),
    CharacterLoreEntry(
        id = BookCharacterId.ZERO,
        codename = "SIFIR · KARANLIK GELECEK YANKISI",
        bio = "Başka bir evrenden değil — Sokovia'nın düştüğü ve Avengers'ın dağıldığı karanlık gelecekten beş yıl geriye dönen yara izli Aiden Blackwood. Aynı kalp, daha ağır bir bedel.",
        quote = "“Ben senin olabileceğin en karanlık gölgeyim — ama kaderi hâlâ değiştirebilirsin.”"
    ),
    CharacterLoreEntry(
        id = BookCharacterId.VISION,
        codename = "VİZYON · ZİHİN TAŞI VE HAKİKAT",
        bio = "J.A.R.V.I.S.'in bilinci, Vibranyum beden ve Zihin Taşı'nın birleşiminden doğan benzersiz varlık. Evrenin en saf terazisi ve Aiden'ın çoklu evrendeki tekilliğini ilk okuyan göz.",
        quote = "“Ben hayatın tarafındayım. Ve sen, Aiden Blackwood, bu evrende tek olansın.”"
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
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        com.example.ui.components.ConfigureEdgeToEdgeDialogWindow()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .widthIn(max = 560.dp)
                .padding(horizontal = 14.dp, vertical = 14.dp)
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
                    val chronicleCtx = androidx.compose.ui.platform.LocalContext.current
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x552A1452))
                            .border(0.8.dp, Color(0x88C4B5FD), RoundedCornerShape(50))
                            .clickable {
                                val md = com.example.book.BookChronicleExporter.formatDossierMarkdown(state)
                                val cm = chronicleCtx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                cm?.setPrimaryClip(android.content.ClipData.newPlainText("Aiden Kronik", md))
                                android.widget.Toast.makeText(
                                    chronicleCtx,
                                    "Kronik dosyası panoya kopyalandı",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "📋 Kopyala",
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x552A1452))
                            .border(0.8.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(50))
                            .clickable {
                                val cert = com.example.book.StoryCompletionCertificateHelper.buildCertificate(state)
                                try {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Kozmik Sürükleniş — Okur Kronik Belgesi")
                                        putExtra(android.content.Intent.EXTRA_TEXT, cert)
                                    }
                                    chronicleCtx.startActivity(
                                        android.content.Intent.createChooser(shareIntent, "Kronik Belgesini Paylaş")
                                    )
                                } catch (_: Exception) {}
                            }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✦ Paylaş",
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    MorCamCloseOrb(onClick = onDismiss, tag = "book_chronicle_close")
                }

                Spacer(modifier = Modifier.height(10.dp))

                val progressRibbon = remember(state) {
                    val s = com.example.book.BookProgressSummaryFormatter.compute(state)
                    val badge = com.example.book.BookChapterCompletionBadgeHelper.resolveBadge(s.completionPercent)
                    val bond = com.example.book.BookBondSummaryHelper.compute(state)
                    val transcriptStats = com.example.book.BookTranscriptStatsHelper.compute(state)
                    val readPart = if (transcriptStats.totalWords > 0) {
                        " • Okunan: ${com.example.book.BookTranscriptStatsHelper.formatCompact(transcriptStats)}"
                    } else ""
                    val statusLine = com.example.book.BookFatigueAlertHelper.formatStatusRibbon(state)
                    val branchLine = com.example.book.BookBranchDiscoveryHelper.formatCompact(
                        com.example.book.BookBranchDiscoveryHelper.compute(state)
                    )
                    "$badge • ${com.example.book.BookProgressSummaryFormatter.formatRibbon(s)}\n${com.example.book.BookBondSummaryHelper.formatCompact(bond)}$readPart\n$statusLine • $branchLine"
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x441B0D36))
                        .border(0.7.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = progressRibbon,
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.SilverLavenderBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

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

                                // Dynamic Spoiler-Free Story Clarity Recap up to the player's current chapter!
                                val chapterClarityRecap = remember(state.activeChapter) {
                                    when (state.activeChapter) {
                                        1 -> "14 Kasım 2006'da beş yaşındayken New York gökyüzünde açılan mavi bir yarıkla ailen kayboldu ve içine kozmik bir güç yerleşti. 20 yıldır S.H.I.E.L.D. ve gizli birimlerden kaçıyorsun — bu gece Avengers seni avlamak yerine masaya çağırıyor."
                                        2 -> "İlk gece Avengers Kulesi'ne girdin. Ekip sana avı bitirme teklifi sundu; bu sabah cam masada kuralları ve 30 günlük deneme süresini belirliyorsun."
                                        3 -> "Avengers Kulesi'nde 30 günlük deneme sürendesin. Bruce kanındaki 2006 enerjisinin doğal bir kaza değil, yapay olarak zorlanmış bir deney izi taşıdığını keşfetti."
                                        4 -> "S.H.I.E.L.D.'ın üstündeki Dünya Güvenlik Konseyi'nden gizli bir el seni Kule'den kaçırmaya kalktı. Nick Fury bu baskını onaylamadı ve Manhattan Limanı 19 Numaralı Rıhtım'da 2006 imzanla eşleşen eski bir cihaz buldu."
                                        5 -> "Limanda 2006 gecesinin bir deney olduğunu öğrendin: Proje Ayna. Sen Denek 7-A'sın. Şimdi aynı deneyin dengesiz kırmızı güçlü hayatta kalanı Denek 6-B (Elias Voss) Brooklyn'de seni çağırıyor."
                                        6 -> "Voss'tan Proje Ayna'nın baş bilim insanının Dr. Evelyn Hale olduğunu ve üçüncü denek Mira'nın (8-C) İstanbul'da ana Rezonans Kulesi'ni açmaya hazırlandığını öğrendin. Avengers ile İstanbul'dasın."
                                        7 -> "Yerebatan arşivinde 2006 gecesi annenle babanın seni korumak için canını feda ettiğini gördün. Dr. Evelyn Hale pişmanlıkla kapına geldi; şimdi Boğaz altındaki Rezonans Kulesi'nde Mira ile yüzleşme vakti."
                                        8 -> "İstanbul'da Rezonans Kulesi'ni durdurdun ama kapanış dalgası uzaya dev bir sinyal yolladı. Thor uyardı: kadim Soluk Mühür Tarikatı (Vaelor & Kaelen) içindeki 2006 çekirdeğini almaya geliyor; Dr. Hale'in patronu Direktör Marcus Vance ise onlarla gizlice anlaştı!"
                                        9 -> "Tarikat öncüsü Kaelen'i aştın ve Direktör Vance'in Kuzey Kutbu'nda 'Kıyamet Aynası' adlı dev kozmik enerji kafesini kurduğunu öğrendin. Şimdi Buz Kalesi'ne inip hem Vance hem de Yüce Hakem Vaelor'la ilk kez çarpışacaksın."
                                        10 -> "1. Sezon Finali: Kuzey Kutbu'ndan kaçan Vance, Avengers Kulesi çekirdeğini bombaya çeviriyor; yukarıda ise Yüce Hakem Vaelor Manhattan'ı kuşattı. Baban Dr. Thomas Blackwood'un 2006 sırrı ve çatı düellosu seni bekliyor!"
                                        11 -> "2. Sezon Başlıyor: Vaelor yenildi ama Manhattan üzerindeki çatlak kapanmadı. 1. Sezon finalindeki seçiminin izleriyle Avengers ekibi Sokovia'daki son HYDRA kalesine iniyor — bu gece Kule'de büyük kutlama, Mjolnir testi ve karanlık bir uyanış var."
                                        12 -> "Kutlama gecesi Tony'nin Zihin Taşı deneyi, Asıl Mimar'ın 2006 sinyaliyle zehirlendi ve ULTRON doğdu! Ekip içinde güven sarsılırken Vibranyum izi Afrika'ya uzanıyor; Wanda'nın ikizi Pietro Maximoff ise Ultron'un safında ortaya çıktı."
                                        13 -> "Afrika kıyısındaki hurda gemisinde Ultron seni Vibranyum Enerji Kesicilerle ilk kez gerçek anlamda yaraladı. Zaman dondu: Asıl Mimar yüz yüze konuştu ve geleceğin yara izli Aiden'ı (Sıfır) seni kurtarmak için ilk kez ortaya çıktı!"
                                        14 -> "Sarsılmış Avengers, Clint Barton'ın gizli aile çiftliğine sığındı. Fırtına öncesi bu tek sakin gecede ekip yaralarını sarıyor, Nick Fury masaya dönüyor ve kalbinin yakınlaştığı kişiyle canlı 3D baş başa bir dönüm noktası yaşanıyor."
                                        15 -> "Savaşa dönmeden önce Aiden, Brooklyn'de kurduğu gizli güvenli eve uğruyor: kurtardığı Elias Voss (6-B) ve Mira (8-C) ile derin psikolojik ve duygusal bir buluşma! İkisi de Aiden'ın desteğiyle iyileşiyor ve yaklaşan fırtınada yanında durmak istiyor."
                                        16 -> "Seul sokaklarında Ultron'un Vibranyum Beşiği'ni kaçırma kovalamacası! Pietro gerçekleri görüp Wanda ve Avengers'ın yanına geçti. Ancak Kule'de Sıfır Beşiği yok etmek için karşına çıkıyor — Ayna Düellosu başlıyor!"
                                        17 -> "Beşik'ten doğan VISION, Mjolnir'i zahmetsizce kaldırarak herkesi susturdu! Vision ve Doctor Strange, Sıfır'ın başka bir evrenden değil, karanlık geleceğinden gelen TEK Aiden olduğunu ve 2006'da babanın Mimar'ın Anahtarını çaldığını açıklıyor."
                                        18 -> "Kule'de yaralı Sıfır'la canlı 3D yüzleşme vakti. Yanağındaki gümüş yara izinin ve çökmüş geleceğin bedelini öğreniyorsun. Ultron ise Sokovia şehrini bir göktaşına çevirmek için son hazırlığını yaptı!"
                                        19 -> "Sokovia'ya kalkıştan hemen önce Aiden, Voss ve Mira'yı Avengers ekibinin önüne çıkarıp onları savunuyor ('Onlar ailem, canavar değil!'). Savaş öncesi gecede suçluluk, insanlık ve Ultron'un gerçek yüzü üzerine derin psikolojik konuşmalar yaşanıyor."
                                        20 -> "Sokovia gökyüzüne yükseliyor! Avengers, Vision, Pietro, Voss, Mira ve sen şehri kurtarmak için sokaklara iniyorsunuz. Çatlamış Ultron zırhının ardından altın-mavi gözlerle bakan Asıl Mimar doğrudan seninle konuşuyor."
                                        21 -> "Uçan şehrin merkezindeki kilisede son savunma! Çekirdeği korurken sivil tahliyesinde Pietro ölümcül bir ateş hattına dalıyor — 2. Sezon'un en ağır kader kararı burada veriliyor."
                                        22 -> "Pietro'nun kader anının hemen ardından Ultron Prime, seni zihinsel olarak yıkmak için Voss ve Mira'yı çöken katedral kenarında kıstırıyor! Asıl Mimar 2006 travmanla zihnine sızarken en ağır psikolojik sınavı veriyorsun."
                                        23 -> "Büyük Final · Perde I: Sokovia meydanında Vibranyum Ultron Prime'a karşı zorunlu final düellosu! Ultron'un insanlık üzerine unutulmaz son konuşması, şehrin kurtuluşu ve Sıfır'ın yıldız tozuna karışan yürek burkan şafak vedası!"
                                        else -> "2. Sezon Büyük Finali · Perde II: Voss ve Mira'nın özgür yeni hayatları ve gümüş pusula hediyesi, 2006 kâbusuyla psikolojik barışma, Natasha veya Wanda ile canlı 3D gün batımı final sahnesi — ve 3. Sezon'u açacak büyük sürpriz!"
                                    }
                                }
                                DossierLoreCard(
                                    title = "ŞİMDİYE KADAR NE OLDU? (BÖLÜM ${activeMeta.romanNumeral} ÖZETİ)",
                                    accent = Color(0xFF34D399),
                                    body = chapterClarityRecap
                                )

                                // Live Season 2 Carried-Over Destiny & Romance Card (shown Ch 11+)
                                if (state.activeChapter >= 11) {
                                    val s1EndLabel = when (state.flags["season1_ending"]) {
                                        "twin_stars" -> "Gölge ve Işık (Sıcak Aile)"
                                        "shield_reborn" -> "Kozmik Nöbet (Fury & Yeni S.H.I.E.L.D.)"
                                        else -> "Kozmik Komutan (Avengers Öncüsü)"
                                    }
                                    val mjolnirLabel = when (state.flags["s2_mjolnir"]) {
                                        "pure_hands" -> "Çıplak elle 2 mm oynattı!"
                                        "cosmic_cheat" -> "Kozmik kıvılcımla çarpıldı"
                                        "heart_test" -> "Kadim rünler parladı"
                                        "natasha_pass" -> "Natasha ile pas geçti"
                                        "wanda_pass" -> "Wanda ile pas geçti"
                                        else -> "Henüz test edilmedi"
                                    }
                                    val romanceLabel = when (state.flags["s2_romance"]) {
                                        "natasha" -> "Natasha Romanoff ♥ (Aşama ${state.flags["s2_romance_stage"] ?: "1"}/4)"
                                        "wanda" -> "Wanda Maximoff ♥ (Aşama ${state.flags["s2_romance_stage"] ?: "1"}/4)"
                                        "none" -> "Avengers Kardeşliği (Plâtonik)"
                                        else -> "Henüz belirlenmedi"
                                    }
                                    val pietroLabel = when (state.flags["s2_pietro_fate"]) {
                                        "saved" -> "Kurtarıldı · Hayatta! (3. Sezon'a taşındı)"
                                        "sacrificed" -> "Kahramanca fedakârlık"
                                        else -> "Kaderi henüz yazılmadı"
                                    }
                                    val vossMiraLabel = when (state.flags["ch24f_voss_mira_epilogue"]) {
                                        "sunday_family" -> "Özgür · 6-B, 7-A, 8-C Sonsuz Ailesi"
                                        "free_guardians" -> "Özgür · Gizli Koruyucular"
                                        else -> when (state.flags["ch19g_voss_mira_stand"]) {
                                            "fierce_defense", "strategic_vouch" -> "Avengers Operasyonunda Yanında!"
                                            else -> "Brooklyn Güvenli Sığınağında İyileşiyorlar"
                                        }
                                    }
                                    DossierLoreCard(
                                        title = "✦ 2. SEZON CANLI KADER & SEÇİM İZİN",
                                        accent = Color(0xFFFDE047),
                                        body = "• 1. Sezon Mirası: $s1EndLabel\n• Mjolnir Sınavı: $mjolnirLabel\n• Kalbinin Yolu: $romanceLabel\n• Voss & Mira (6-B & 8-C): $vossMiraLabel\n• Pietro Maximoff: $pietroLabel"
                                    )
                                }

                                // Current Scene Location Card
                                DossierLoreCard(
                                    title = "AKTİF KONUM & SAHNE",
                                    accent = accentColor,
                                    body = "${currentNode?.locationTag ?: activeMeta.subtitle}\n“${currentNode?.sceneTitle ?: activeMeta.title}” — ${activeMeta.subtitle}"
                                )

                                // Powers & Relics Lore (100% Canon, No Flying!)
                                DossierLoreCard(
                                    title = "MAVİ GÖZ · SÜRÜKLENİŞ (UÇUŞ YOK)",
                                    accent = Color(0xFF38BDF8),
                                    body = "Aiden uçamaz — tamamen zemin ve kuantum ışınlanma ustasıdır. Zihninde sabitlediği noktaya mavi kıvılcımla anlık ışınlanır, yaraları hızla iyileşir ve gelen enerjiyi sınırlı ölçüde emip geri yönlendirebilir."
                                )
                                DossierLoreCard(
                                    title = "BEYAZ GÖZ & KIRMIZI GÖZ (SINIRLAR)",
                                    accent = Color(0xFFF472B6),
                                    body = "Beyaz Göz telepati, hafıza silme ve zihin felci sağlar ama ağır kullanımda burun kanaması ve yorgunluk yapar. Kırmızı Göz ise yıkıcı moleküler güç verir ama kontrol etmesi zordur."
                                )
                                DossierLoreCard(
                                    title = "ANTİKA GÜMÜŞ MADALYON",
                                    accent = Color(0xFFFEF08A),
                                    body = "Beş yaşındaki 14 Kasım 2006 gecesinden ve ailesinden geriye kalan tek fiziksel hatıra. Karanlıkta nefesini sabitleyen sessiz çapa."
                                )
                            }

                            // ── TAB 1: PROGRESSIVELY UNLOCKED CHARACTER CODEX + LIVE FLAGS ──
                            1 -> {
                                val unlockedSet = remember(state.activeChapter, state.highestCompletedChapter, state.currentNodeId, state.numericFlags) {
                                    BookStateRepository.unlockedCharacters(state).toSet()
                                }
                                val unlockedEntries = remember(unlockedSet) {
                                    CORE_CHARACTER_CODEX.filter { it.id in unlockedSet }
                                }
                                val lockedCount = (CORE_CHARACTER_CODEX.size - unlockedEntries.size).coerceAtLeast(0)
                                Text(
                                    text = "Hikâyede tanıştığın karakterler (${unlockedEntries.size}/${CORE_CHARACTER_CODEX.size}) ve seçimlerindeki canlı izleri. Yeni karakterler sahnelerini okudukça otomatik açılır.",
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.LavenderMuted,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.5.sp
                                )
                                unlockedEntries.forEach { entry ->
                                    val cAccent = Color(entry.id.accentHex)
                                    val charFlags = remember(state.numericFlags, entry.id) {
                                        BookStateRepository.characterFlagsFor(state, entry.id)
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0x521E0D3E))
                                            .border(0.9.dp, cAccent.copy(alpha = 0.48f), RoundedCornerShape(18.dp))
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            CinematicCharacterAvatar(
                                                key = CharacterPortraitKey.fromBookCharacter(entry.id),
                                                size = 54.dp,
                                                borderWidth = 1.5.dp,
                                                borderColor = cAccent
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
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
                                        if (charFlags.isNotEmpty()) {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                charFlags.forEach { (axis, value) ->
                                                    val chipColor = if (axis.isNegativeTone || value < 0) Color(0xFFFCA5A5) else Color(axis.accentHex)
                                                    val sign = if (value > 0) "+$value" else "$value"
                                                    Row(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(50))
                                                            .background(chipColor.copy(alpha = 0.14f))
                                                            .border(0.7.dp, chipColor.copy(alpha = 0.5f), RoundedCornerShape(50))
                                                            .padding(horizontal = 8.dp, vertical = 3.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = axis.turkishLabel.replaceFirstChar { it.uppercase() },
                                                            fontFamily = InterFontFamily,
                                                            color = Color(0xFFFAF6FF),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                        Text(
                                                            text = sign,
                                                            fontFamily = InterFontFamily,
                                                            color = chipColor,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (lockedCount > 0) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0x3D180A34))
                                            .border(0.85.dp, Color(0x55C4B5FD), RoundedCornerShape(16.dp))
                                            .padding(horizontal = 13.dp, vertical = 11.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MorCamTokens.LavenderSoft,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "+$lockedCount yeni karakter hikâyede karşılaştıkça otomatik olarak bu listeye eklenecek.",
                                            fontFamily = InterFontFamily,
                                            color = MorCamTokens.LavenderLight,
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
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
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
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
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        com.example.ui.components.ConfigureEdgeToEdgeDialogWindow()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .widthIn(max = 560.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp)
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
                        if (chapter.number == 1) {
                            Text(
                                text = "✦ 1. SEZON · KOZMİK SÜRÜKLENİŞ (BÖLÜM I – X)",
                                fontFamily = InterFontFamily,
                                color = Color(0xFF93C5FD),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp, start = 4.dp)
                            )
                        } else if (chapter.number == 11) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "✦ 2. SEZON · DEMİR VE YANKI (BÖLÜM XI – XXIV)",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFFDE047),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp, start = 4.dp)
                            )
                        }
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
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        com.example.ui.components.ConfigureEdgeToEdgeDialogWindow()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .widthIn(max = 520.dp)
                .padding(horizontal = 18.dp, vertical = 14.dp)
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
