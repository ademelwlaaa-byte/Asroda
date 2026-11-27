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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Atomic single-flight download guard with cancellation generation tokens.
 * Prevents duplicate download launches on rapid taps and suppresses stale completion/error
 * callbacks after user cancellation or dialog disposal.
 */
internal class PackDownloadSessionGuard {
    private val inFlight = AtomicBoolean(false)
    private val generation = AtomicInteger(0)

    fun tryStart(): Int? {
        if (!inFlight.compareAndSet(false, true)) return null
        return generation.incrementAndGet()
    }

    fun isCurrent(token: Int): Boolean = inFlight.get() && generation.get() == token

    fun cancel(): Int {
        val next = generation.incrementAndGet()
        inFlight.set(false)
        return next
    }

    fun complete(token: Int): Boolean {
        if (generation.get() != token) return false
        return inFlight.compareAndSet(true, false)
    }

    fun isDownloading(): Boolean = inFlight.get()
}

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
    val activeDownload by BookContentPackManager.activeDownload.collectAsStateWithLifecycle()

    val downloadGuard = remember { PackDownloadSessionGuard() }
    var downloadJob by remember { mutableStateOf<Job?>(null) }
    var isDownloading by remember(autoStartUpdate) { mutableStateOf(autoStartUpdate) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun cancelDownloadSession() {
        downloadGuard.cancel()
        downloadJob?.cancel()
        downloadJob = null
        BookContentPackManager.cancelActiveDownload()
        isDownloading = false
    }

    fun startDownload() {
        val token = downloadGuard.tryStart() ?: return
        isDownloading = true
        errorMessage = null
        downloadJob = scope.launch {
            try {
                val res = BookContentPackManager.downloadAndPrepareBook(
                    context = context,
                    bookId = CosmicDriftBookRegistry.BOOK_ID,
                    remoteUpdate = remoteUpdate
                )
                if (!downloadGuard.complete(token)) return@launch
                res.onSuccess {
                    isDownloading = false
                    onReadyToOpen()
                }.onFailure { err ->
                    isDownloading = false
                    if (err !is CancellationException) {
                        errorMessage = err.message ?: "İndirme tamamlanamadı. Lütfen tekrar deneyin."
                    }
                }
            } catch (_: CancellationException) {
                if (downloadGuard.complete(token)) {
                    isDownloading = false
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (downloadGuard.isDownloading()) {
                downloadGuard.cancel()
                downloadJob?.cancel()
                downloadJob = null
                BookContentPackManager.cancelActiveDownload()
            }
        }
    }

    LaunchedEffect(autoStartUpdate) {
        if (autoStartUpdate) {
            startDownload()
        }
    }

    val isUpdate = remoteUpdate != null
    val accent = if (isUpdate) Color(0xFFFDE047) else Color(0xFF38BDF8)
    val accentSecondary = if (isUpdate) Color(0xFFC084FC) else Color(0xFF818CF8)
    val sizeText = remoteUpdate?.sizeLabel ?: "~8,1 MB"

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
                .background(Color(0x9904020B))
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .heightIn(max = (maxHeight * 0.90f).coerceAtLeast(260.dp))
                    .morCamCosmicGlow(cornerRadius = 28.dp, glowColor = accent, alpha = 0.34f, blurDp = 22.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF61B0D37),
                                Color(0xF8110725),
                                Color(0xFA090416)
                            )
                        )
                    )
                    .border(
                        width = 1.25.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                accent.copy(alpha = 0.72f),
                                Color(0x66C084FC),
                                Color(0x337C3AED),
                                Color(0x88E9D5FF)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
            ) {
                // Subtle top celestial radial highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accent.copy(alpha = 0.14f),
                                    accentSecondary.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Sculpted Dual-Ring Emblem
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        accent.copy(alpha = 0.28f),
                                        accentSecondary.copy(alpha = 0.12f),
                                        Color(0x440B051D)
                                    )
                                )
                            )
                            .border(
                                width = 1.3.dp,
                                brush = Brush.linearGradient(listOf(accent, Color(0xFFD8B4FE))),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0x99120729))
                                .border(0.8.dp, accent.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isUpdate) Icons.Default.SystemUpdate else Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }

                    // Compact Status + Size Pill Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(accent.copy(alpha = 0.14f))
                            .border(0.85.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = when {
                                isUpdate && isDownloading -> "SES PAKETİ GÜNCELLENİYOR"
                                isUpdate -> "SES PAKETİ GÜNCELLEMESİ · $sizeText"
                                isDownloading -> "İÇERİK PAKETİ HAZIRLANIYOR"
                                else -> "HİKÂYE & SES PAKETİ · $sizeText"
                            },
                            fontFamily = InterFontFamily,
                            color = accent,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Book Title
                    Text(
                        text = bookTitle,
                        fontFamily = DisplaySerifFontFamily,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 27.sp
                    )

                    if (!isDownloading) {
                        val descText = remoteUpdate?.notes
                            ?: "1. ve 2. Sezon'un (20 Bölüm) 2.5D sahne görselleri, müzik ve stüdyo ses verileri ($sizeText) indirildikten sonra hikâye otomatik başlayacak."

                        // Frosted Release Notes / Package Info Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0x7814092C))
                                .border(0.9.dp, Color(0x44C4B5FD), RoundedCornerShape(18.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = descText,
                                fontFamily = InterFontFamily,
                                color = Color(0xFFE6DCF9),
                                fontSize = 13.sp,
                                lineHeight = 19.5.sp,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "İndirme bittiğinde hikâye otomatik açılır",
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFBBA4E3),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (!errorMessage.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x447F1D1D))
                                    .border(0.9.dp, Color(0x88FCA5A5), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFFECACA),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Primary CTA Button — Crisp single-row layout with centered icon + label + size chip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .morCamCosmicGlow(
                                    cornerRadius = 16.dp,
                                    glowColor = Color(0xFF8B5CF6),
                                    alpha = 0.42f,
                                    blurDp = 14.dp
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF9333EA))
                                    )
                                )
                                .border(
                                    width = 1.1.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xCCBAE6FD), Color(0xDDE9D5FF), Color(0xCCC084FC))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { startDownload() }
                                .testTag("story_pack_primary_action")
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                text = if (isUpdate) "Güncelle ve Başla" else "İndir ve Başla",
                                fontFamily = InterFontFamily,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0x38FFFFFF))
                                    .border(0.7.dp, Color(0x66FFFFFF), RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 2.5.dp)
                            ) {
                                Text(
                                    text = sizeText,
                                    fontFamily = InterFontFamily,
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        // Secondary Skip / Dismiss Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x521C0E38))
                                .border(0.9.dp, Color(0x55C4B5FD), RoundedCornerShape(14.dp))
                                .clickable {
                                    if (isUpdate) onReadyToOpen() else onDismiss()
                                }
                                .testTag("story_pack_secondary_action")
                                .padding(horizontal = 16.dp, vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isUpdate) "Şimdilik Atla (Mevcutla Aç)" else "Şimdi Değil",
                                fontFamily = InterFontFamily,
                                color = MorCamTokens.LavenderLight,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        val dl = activeDownload
                        val frac = dl?.fraction?.takeIf { it >= 0f } ?: 0.35f
                        val pctInt = (frac * 100f).toInt().coerceIn(0, 100)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0x7814092C))
                                .border(0.9.dp, Color(0x44C4B5FD), RoundedCornerShape(18.dp))
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dl?.phaseLabel ?: "Paket indiriliyor...",
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFE9D5FF),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(accent.copy(alpha = 0.18f))
                                        .border(0.7.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(50))
                                        .padding(horizontal = 9.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "%$pctInt",
                                        fontFamily = InterFontFamily,
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0x77090418))
                                    .border(0.8.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(50))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(frac.coerceIn(0.06f, 1f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF38BDF8), Color(0xFFA855F7), accent)
                                            )
                                        )
                                )
                            }

                            Text(
                                text = if (dl != null && dl.totalBytes > 0L) {
                                    "${BookContentPackManager.formatSizeMb(dl.downloadedBytes)} / ${BookContentPackManager.formatSizeMb(dl.totalBytes)}"
                                } else {
                                    "Bağlantı kuruluyor..."
                                },
                                fontFamily = InterFontFamily,
                                color = Color(0xFFBAE6FD),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x3850102C))
                                .border(0.85.dp, Color(0x66FCA5A5), RoundedCornerShape(14.dp))
                                .clickable {
                                    cancelDownloadSession()
                                    onDismiss()
                                }
                                .testTag("story_pack_cancel_action")
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "İndirmeyi İptal Et",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFFECACA),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
