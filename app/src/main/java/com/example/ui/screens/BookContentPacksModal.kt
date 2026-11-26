package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.book.BookContentPackManager
import com.example.book.CosmicDriftBookRegistry
import com.example.ui.components.MorCamTokens
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import kotlinx.coroutines.launch

/**
 * Seamless In-Flow Story Download & Auto-Update Dialog.
 *
 * - No extra menu or clutter: triggers automatically when the player taps "Kitaba Başla" for the
 *   first time (asking permission before downloading) OR when an over-the-air chapter/media update
 *   is detected upon entering the book.
 * - Immediately opens the story the instant downloading finishes!
 */
@Composable
fun StoryLaunchDownloadDialog(
    bookTitle: String,
    remoteUpdate: BookContentPackManager.RemoteStoryUpdateInfo?,
    autoStartUpdate: Boolean,
    onReadyToOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeDownload by BookContentPackManager.activeDownload.collectAsState()

    var isDownloading by remember(autoStartUpdate) { mutableStateOf(autoStartUpdate) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun startDownload() {
        isDownloading = true
        errorMessage = null
        scope.launch {
            val res = BookContentPackManager.downloadAndPrepareBook(
                context = context,
                bookId = CosmicDriftBookRegistry.BOOK_ID,
                remoteUpdate = remoteUpdate
            )
            res.onSuccess {
                onReadyToOpen()
            }.onFailure { err ->
                isDownloading = false
                errorMessage = err.message ?: "İndirme tamamlanamadı. Lütfen tekrar deneyin."
            }
        }
    }

    LaunchedEffect(autoStartUpdate) {
        if (autoStartUpdate) {
            startDownload()
        }
    }

    val accent = if (remoteUpdate != null) Color(0xFFFDE047) else Color(0xFF38BDF8)

    Dialog(
        onDismissRequest = {
            if (!isDownloading) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = !isDownloading,
            dismissOnClickOutside = !isDownloading
        )
    ) {
        com.example.ui.components.ConfigureEdgeToEdgeDialogWindow()
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .heightIn(max = (maxHeight * 0.92f).coerceAtLeast(240.dp))
                    .morCamCosmicGlow(cornerRadius = 26.dp, glowColor = accent, alpha = 0.45f, blurDp = 18.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(MorCamTokens.GlassSurfaceTop, MorCamTokens.GlassSurfaceMid, MorCamTokens.GlassSurfaceBottom)
                        )
                    )
                    .border(1.3.dp, MorCamTokens.RimGradient, RoundedCornerShape(26.dp))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.20f))
                        .border(1.2.dp, accent.copy(alpha = 0.80f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = when {
                        remoteUpdate != null && isDownloading -> "SES & HİKÂYE PAKETİ GÜNCELLENİYOR"
                        remoteUpdate != null -> "YENİ SES PAKETİ GÜNCELLEMESİ — İNDİRİLSİN Mİ?"
                        isDownloading -> "HİKÂYE VE SES PAKETİ HAZIRLANIYOR"
                        else -> "HİKÂYE & SES PAKETİ İNDİRİLSİN Mİ?"
                    },
                    fontFamily = InterFontFamily,
                    color = accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = bookTitle,
                    fontFamily = DisplaySerifFontFamily,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                if (!isDownloading) {
                    val sizeText = remoteUpdate?.sizeLabel ?: "~8,1 MB"
                    val descText = remoteUpdate?.notes
                        ?: "1. ve 2. Sezon'un (20 Bölüm) 2.5D sahne görselleri, müzik ve stüdyo ses verileri ($sizeText) indirildikten sonra hikâye otomatik başlayacak."
                    Text(
                        text = descText,
                        fontFamily = InterFontFamily,
                        color = Color(0xFFDCD3F5),
                        fontSize = 12.8.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage!!,
                            fontFamily = InterFontFamily,
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED))))
                            .border(1.1.dp, Color(0xCCBAE6FD), RoundedCornerShape(16.dp))
                            .clickable { startDownload() }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (remoteUpdate != null) "SES PAKETİNİ GÜNCELLE VE BAŞLA ($sizeText) ▶" else "İNDİR VE BAŞLA ($sizeText) ▶",
                            fontFamily = InterFontFamily,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x441B0D36))
                            .border(0.85.dp, Color(0x55C4B5FD), RoundedCornerShape(14.dp))
                            .clickable {
                                if (remoteUpdate != null) onReadyToOpen() else onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (remoteUpdate != null) "Şimdilik Atla (Mevcutla Aç)" else "Şimdi Değil",
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    val dl = activeDownload
                    val frac = dl?.fraction?.takeIf { it >= 0f } ?: 0.35f
                    val pctInt = (frac * 100f).toInt().coerceIn(0, 100)

                    Text(
                        text = dl?.phaseLabel ?: "Paket indiriliyor...",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFE9D5FF),
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x660F172A))
                            .border(0.8.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(frac.coerceIn(0.06f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFFC084FC))))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (dl != null && dl.totalBytes > 0L) {
                                "${BookContentPackManager.formatSizeMb(dl.downloadedBytes)} / ${BookContentPackManager.formatSizeMb(dl.totalBytes)}"
                            } else {
                                "Hazırlanıyor..."
                            },
                            fontFamily = InterFontFamily,
                            color = Color(0xFFBAE6FD),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "%$pctInt",
                            fontFamily = InterFontFamily,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Text(
                        text = "İptal Et",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFFCA5A5),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                BookContentPackManager.cancelActiveDownload()
                                isDownloading = false
                                onDismiss()
                            }
                            .padding(top = 4.dp, bottom = 2.dp, start = 12.dp, end = 12.dp)
                    )
                }
            }
        }
    }
}
}
