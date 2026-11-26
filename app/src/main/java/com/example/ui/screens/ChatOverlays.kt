package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.MessageEntity
import com.example.data.local.MilestoneEntity
import com.example.ui.animation.CosmicConfettiOverlay
import com.example.ui.animation.cosmicGlassSheen
import com.example.ui.animation.cosmicShakeOnTrigger
import com.example.ui.components.MorCamBottomSheetShell
import com.example.ui.components.MorCamButtonVariant
import com.example.ui.components.MorCamCapsuleButton
import com.example.ui.components.MorCamChipPill
import com.example.ui.components.MorCamDialogShell
import com.example.ui.components.MorCamDivider
import com.example.ui.components.MorCamGlassCard
import com.example.ui.components.MorCamOptionRow
import com.example.ui.components.MorCamTokens
import com.example.ui.components.drawMorCamFourPointStar
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.EmochiError
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.example.data.local.AffectionEventEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionState
import com.example.data.repository.KeyCharacter
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.components.BotQuickProfileSheet
import com.example.ui.components.NeuralVaultModal
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.viewmodel.EmochiViewModel
import kotlinx.coroutines.delay

/**
 * Live Neural Vault & Yaşanan Anı toast pill banner.
 */
@Composable
internal fun ChatMemoryCommitBanner(
    notice: EmochiViewModel.MemoryCommitNotice?,
    visible: Boolean,
    onOpenVault: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 64.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible && notice != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(240)),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(tween(200))
        ) {
            notice?.let { item ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                if (item.isLivingMemory) {
                                    listOf(Color(0xE63B1470), Color(0xE66D28D9))
                                } else {
                                    listOf(Color(0xE6221042), Color(0xE64C1D95))
                                }
                            )
                        )
                        .border(
                            width = 0.9.dp,
                            brush = Brush.horizontalGradient(listOf(Color(0x99E9D5FF), Color(0x66A855F7))),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable(onClick = onOpenVault)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("memory_commit_pill"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Hafıza Güncellendi",
                        tint = Color(0xFFFDE68A),
                        modifier = Modifier.size(15.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = item.title,
                            color = Color(0xFFFAF5FF),
                            fontFamily = LiterarySerifFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.subtitle,
                            color = Color(0xFFE9D5FF),
                            fontFamily = InterFontFamily,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mor Cam confirmation dialog for Rewind ("Buradan Devam Et").
 */
@Composable
internal fun ChatRewindConfirmDialog(
    targetMessageId: String?,
    onDismiss: () -> Unit,
    onConfirmRewind: (String) -> Unit
) {
    val targetId = targetMessageId ?: return
    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = "Buradan Devam Et",
        subtitle = "Zaman çizgisini bu mesaja geri sar",
        icon = Icons.Default.History
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Bu noktadan devam edersen sonraki mesajlar ve onlarla ilgili hafıza, anı, ilişki ağı ve kilometre taşları temizlenecek. Emin misin?",
                color = MorCamTokens.LavenderLight,
                fontFamily = LiterarySerifFontFamily,
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MorCamCapsuleButton(
                    text = "İptal",
                    onClick = onDismiss,
                    variant = MorCamButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                )
                MorCamCapsuleButton(
                    text = "Evet, Devam Et",
                    onClick = { onConfirmRewind(targetId) },
                    variant = MorCamButtonVariant.Destructive,
                    modifier = Modifier.weight(1.25f)
                )
            }
        }
    }
}

/**
 * Mor Cam Starred Messages BottomSheet with jump-to-message & AI memory priority indicator.
 */
@Composable
internal fun ChatStarredMessagesSheet(
    bot: BotEntity,
    starredMessages: List<MessageEntity>,
    onLoadStarred: () -> Unit,
    onDismiss: () -> Unit,
    onJumpToMessage: (String) -> Unit,
    onUnstarMessage: (String) -> Unit
) {
    LaunchedEffect(Unit) { onLoadStarred() }

    MorCamBottomSheetShell(
        onDismissRequest = onDismiss,
        title = "Yıldızlanan Mesajlar",
        subtitle = "${starredMessages.size} önemli an • AI hafızasında öncelikli",
        icon = Icons.Default.Star
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x3D2E1065))
                .border(0.85.dp, Color(0x66D8B4FE), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFFDE68A),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Yıldızladığın mesajlar hem hızlı yer imi olarak saklanır hem de AI hafızasında öncelikli tutulur.",
                fontFamily = InterFontFamily,
                color = MorCamTokens.LavenderLight,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }

        if (starredMessages.isEmpty()) {
            MorCamGlassCard(
                cornerRadius = 20.dp,
                paddingHorizontal = 22.dp,
                paddingVertical = 28.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0x888B5CF6), Color(0x443B1470))))
                            .border(1.1.dp, MorCamTokens.RimGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(30.dp)) {
                            drawMorCamFourPointStar(
                                center = Offset(size.width / 2f, size.height / 2f),
                                outerRadius = size.minDimension * 0.46f,
                                innerRadius = size.minDimension * 0.16f
                            )
                        }
                    }
                    Text(
                        text = "Henüz yıldızlanan mesaj yok",
                        fontFamily = LiterarySerifFontFamily,
                        color = MorCamTokens.SilverLavenderBright,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Hem kendi mesajlarına hem bot mesajlarına dokunup Diğer → ☆ Yıldızla diyerek özel anları AI hafızasına sabitleyebilirsin.",
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.LavenderMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(starredMessages, key = { it.id }) { starred ->
                    val isAi = starred.role == "assistant"
                    MorCamGlassCard(
                        cornerRadius = 18.dp,
                        highlighted = !isAi,
                        paddingHorizontal = 14.dp,
                        paddingVertical = 12.dp,
                        modifier = Modifier.clickable { onJumpToMessage(starred.id) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    if (isAi) listOf(Color(0xAA7C3AED), Color(0x663B1470))
                                                    else listOf(Color(0xAA9333EA), Color(0x664C1D95))
                                                )
                                            )
                                            .border(0.9.dp, MorCamTokens.RimGradient, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isAi) Icons.Default.AutoAwesome else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MorCamTokens.SilverLavenderBright,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = if (isAi) bot.aiName else (bot.userCharName.ifBlank { "Sen" }),
                                        fontFamily = LiterarySerifFontFamily,
                                        color = MorCamTokens.LavenderLight,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE68A),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            MorCamDivider()

                            val previewText = remember(starred.id, starred.text) {
                                val cleaned = cleanCopyText(starred.text)
                                if (cleaned.length > 220) cleaned.take(220) + "…" else cleaned
                            }
                            Text(
                                text = previewText,
                                fontFamily = LiterarySerifFontFamily,
                                color = MorCamTokens.SilverLavenderBright,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MorCamChipPill(
                                    label = "Mesaja Git",
                                    selected = true,
                                    icon = Icons.Default.ChevronRight,
                                    onClick = { onJumpToMessage(starred.id) },
                                    modifier = Modifier.testTag("jump_to_starred_${starred.id}")
                                )
                                MorCamChipPill(
                                    label = "Kaldır",
                                    selected = false,
                                    icon = Icons.Default.Star,
                                    onClick = { onUnstarMessage(starred.id) },
                                    modifier = Modifier.testTag("unstar_message_${starred.id}")
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
 * Floating error banner anchored below the top bar so it never shifts the LazyColumn
 * or collides with the DstIn atmospheric fade mask.
 */
@Composable
internal fun ChatFloatingErrorBanner(
    errorMessage: String?,
    topInsetPadding: Dp,
    onRetryFromBanner: (() -> Unit)?,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !errorMessage.isNullOrBlank(),
        modifier = modifier
            .padding(top = topInsetPadding + 6.dp, start = 12.dp, end = 12.dp)
            .zIndex(20f),
        enter = slideInVertically(
            animationSpec = tween(260, easing = FastOutSlowInEasing)
        ) { -it } + fadeIn(tween(180)),
        exit = slideOutVertically(
            animationSpec = tween(200)
        ) { -it } + fadeOut(tween(150))
    ) {
        val err = errorMessage.orEmpty()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .cosmicShakeOnTrigger(err)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xEB3B0D1D), Color(0xEB1D0819))
                    )
                )
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFFFCA5A5), Color(0xFFDC2626), Color(0xFFFCA5A5))
                        )
                    ),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("chat_floating_error_banner")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x38F87171)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = EmochiError,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = err,
                        color = Color(0xFFFECACA),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onRetryFromBanner != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x4DF87171))
                                .border(1.dp, Color(0x88FCA5A5), RoundedCornerShape(10.dp))
                                .clickable { onRetryFromBanner(); onClearError() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Tekrar",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x30F87171))
                            .clickable(onClick = onClearError),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFFFECACA),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mor Cam Scene Atmosphere & Chat Background Picker dialog.
 */
@Composable
internal fun ChatBackgroundPickerDialog(
    bot: BotEntity,
    onDismiss: () -> Unit,
    onUseAvatarBg: () -> Unit,
    onUseDefaultThemeBg: () -> Unit,
    onOpenSettingsForGallery: () -> Unit
) {
    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = "Sahne Atmosferi & Arka Plan",
        subtitle = "Sohbet ekranında görünmesini istediğiniz atmosferi seçin",
        icon = Icons.Default.Wallpaper
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MorCamOptionRow(
                title = "Karakter Portresini Arka Plan Yap",
                subtitle = "Karakterin portresi yumuşak cam katmanla arkaplana yayılır",
                icon = Icons.Default.Face,
                selected = bot.chatBgUrl == "USE_AVATAR",
                onClick = onUseAvatarBg
            )
            MorCamOptionRow(
                title = "Aetheria Gotik Katedral Teması",
                subtitle = "Varsayılan mor salkımlı kozmik cam atmosferi",
                icon = Icons.Default.AutoAwesome,
                selected = bot.chatBgUrl.isBlank(),
                onClick = onUseDefaultThemeBg
            )
            MorCamOptionRow(
                title = "Galeriden Özel Fotoğraf Seç",
                subtitle = "Bot ayarlarından kendi sahne görselini yükle",
                icon = Icons.Default.Image,
                selected = bot.chatBgUrl.isNotBlank() && bot.chatBgUrl != "USE_AVATAR",
                onClick = onOpenSettingsForGallery
            )
            Spacer(modifier = Modifier.height(4.dp))
            MorCamCapsuleButton(
                text = "Kapat",
                onClick = onDismiss,
                variant = MorCamButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Non-blocking Mor Cam banner offering to add a newly discovered cast member.
 */
@Composable
internal fun ChatPendingCastBanner(
    pendingCastMember: CastMemberEntity?,
    visible: Boolean,
    onConfirm: (Long) -> Unit,
    onDismissTemporarily: (Long) -> Unit,
    onBlacklist: (Long) -> Unit
) {
    AnimatedVisibility(
        visible = visible && pendingCastMember != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(250)),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(tween(200))
    ) {
        pendingCastMember?.let { pending ->
            MorCamGlassCard(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .morCamCosmicGlow(cornerRadius = 18.dp, alpha = 0.34f, blurDp = 10.dp)
                    .testTag("pending_cast_banner"),
                cornerRadius = 18.dp,
                highlighted = true,
                paddingHorizontal = 14.dp,
                paddingVertical = 12.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0x998B5CF6), Color(0x663B1470))))
                            .border(0.9.dp, MorCamTokens.RimGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MorCamTokens.SilverLavenderBright,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Yeni karakter fark ettim: ${pending.name}",
                        color = MorCamTokens.SilverLavenderBright,
                        fontFamily = LiterarySerifFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = pending.description.take(90).ifBlank { pending.role },
                    color = MorCamTokens.LavenderMuted,
                    fontFamily = InterFontFamily,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MorCamChipPill(
                        label = "Evet, ekle",
                        selected = true,
                        onClick = { onConfirm(pending.id) },
                        modifier = Modifier.testTag("pending_cast_confirm_button")
                    )
                    MorCamChipPill(
                        label = "Şimdilik geç",
                        selected = false,
                        onClick = { onDismissTemporarily(pending.id) },
                        modifier = Modifier.testTag("pending_cast_dismiss_button")
                    )
                    MorCamChipPill(
                        label = "Bir daha sorma",
                        selected = false,
                        onClick = { onBlacklist(pending.id) },
                        modifier = Modifier.testTag("pending_cast_blacklist_button")
                    )
                }
            }
        }
    }
}

/**
 * Sistem #18: Upgraded Mor Cam Milestone Celebration Overlay (Gold/Lavender Shimmer, 4.5s auto-dismiss).
 * Clearly distinguishes AI narrative milestones (✦ Hikaye Dönüm Noktası) from numeric trust/affection thresholds,
 * renders story-calendar stamps and descriptions, and lets the user jump straight to the Timeline.
 */
@Composable
internal fun ChatMilestoneCelebrationBanner(
    activeMilestone: MilestoneEntity?,
    onDismiss: () -> Unit,
    onOpenTimeline: () -> Unit
) {
    AnimatedVisibility(
        visible = activeMilestone != null,
        enter = fadeIn(tween(340)) + scaleIn(tween(360, easing = FastOutSlowInEasing), initialScale = 0.80f),
        exit = fadeOut(tween(360)) + scaleOut(tween(260), targetScale = 0.88f)
    ) {
        val milestone = activeMilestone ?: return@AnimatedVisibility
        val isNarrative = milestone.isNarrative
        val motionConfig = LocalCosmicMotionConfig.current
        val glowAlphaState: State<Float>
        val particleProgressState: State<Float>
        if (motionConfig.enableAmbientLoops) {
            val infinite = rememberInfiniteTransition(label = "milestoneGlow")
            glowAlphaState = infinite.animateFloat(0.58f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "mg")
            particleProgressState = infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "mp")
        } else {
            glowAlphaState = remember { mutableFloatStateOf(0.88f) }
            particleProgressState = remember { mutableFloatStateOf(0.25f) }
        }
        val starColors = remember {
            listOf(
                Color(0xFFFDE68A),
                MorCamTokens.SilverLavenderBright,
                MorCamTokens.LavenderLight,
                Color(0xFFFBBF24),
                Color(0xFFC4B5FD)
            )
        }

        val goldLavenderRim = remember(isNarrative) {
            if (isNarrative) {
                Brush.linearGradient(listOf(Color(0xFFFDE68A), Color(0xFFD8B4FE), Color(0xFFFBBF24), Color(0xFFE9D5FF)))
            } else {
                Brush.linearGradient(listOf(Color(0xFFE9D5FF), Color(0xFFFDE68A), Color(0xFFA855F7), Color(0xFFF3E8FF)))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("milestone_celebration_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xB3070214))
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onDismiss)
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val particleProgress = particleProgressState.value
                repeat(28) { i ->
                    val x = size.width * ((i * 0.137f + particleProgress * 0.55f) % 1f)
                    val y = size.height * ((i * 0.173f + particleProgress * 0.35f + i * 0.025f) % 1f)
                    val r = 3.5f + (i % 4) * 2.2f
                    if (i % 3 == 0) {
                        drawMorCamFourPointStar(
                            center = Offset(x, y),
                            outerRadius = r * 2.3f,
                            innerRadius = r * 0.7f,
                            coreColor = Color.White.copy(alpha = 0.88f),
                            edgeColor = starColors[i % starColors.size],
                            glowAlpha = 0.38f
                        )
                    } else {
                        drawCircle(
                            color = starColors[i % starColors.size].copy(alpha = 0.65f),
                            radius = r,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            CosmicConfettiOverlay(
                triggerKey = milestone,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .morCamCosmicGlow(cornerRadius = 28.dp, alpha = 0.52f, blurDp = 18.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xF0221042),
                                    MorCamTokens.GlassSurfaceMid,
                                    MorCamTokens.GlassSurfaceBottom
                                )
                            )
                        )
                        .cosmicGlassSheen(cornerRadius = 28.dp, periodMs = 2800, maxAlpha = 0.28f)
                        .border(BorderStroke(1.4.dp, goldLavenderRim), RoundedCornerShape(28.dp))
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    if (isNarrative) listOf(Color(0xBBF59E0B), Color(0x887C3AED), Color(0x553B1470))
                                    else listOf(Color(0xAA9333EA), Color(0x663B1470))
                                )
                            )
                            .border(1.3.dp, goldLavenderRim, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = milestone.emoji.ifBlank { if (isNarrative) "✦" else "💜" },
                            fontSize = 34.sp,
                            modifier = Modifier.graphicsLayer { alpha = glowAlphaState.value }
                        )
                    }

                    Text(
                        text = if (isNarrative) "✦ HİKAYE DÖNÜM NOKTASI ✦" else "YENİ İLİŞKİ KİLOMETRE TAŞI",
                        fontFamily = InterFontFamily,
                        color = if (isNarrative) Color(0xFFFDE68A) else MorCamTokens.LavenderSoft,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = milestone.displayTitle,
                        fontFamily = DisplaySerifFontFamily,
                        color = MorCamTokens.SilverLavenderBright,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        style = TextStyle(shadow = MorCamTokens.TitleShadow)
                    )

                    if (milestone.storyStamp.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x523B1470))
                                .border(0.85.dp, Color(0x99FDE68A), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "📅 ${milestone.storyStamp}",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFFEF3C7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (milestone.narrativeDescription.isNotBlank()) {
                        Text(
                            text = milestone.narrativeDescription,
                            fontFamily = LiterarySerifFontFamily,
                            color = MorCamTokens.LavenderLight,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    MorCamDivider()

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MorCamChipPill(
                            label = "💜 Sevgi ${milestone.affectionAtTime}",
                            selected = false,
                            onClick = {}
                        )
                        MorCamChipPill(
                            label = "🛡️ Güven ${milestone.trustAtTime}",
                            selected = false,
                            onClick = {}
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MorCamCapsuleButton(
                            text = "Zaman Çizgisi",
                            onClick = onOpenTimeline,
                            variant = MorCamButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("milestone_open_timeline_button")
                        )
                        MorCamCapsuleButton(
                            text = "Harika",
                            onClick = onDismiss,
                            variant = MorCamButtonVariant.Primary,
                            icon = Icons.Default.AutoAwesome,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("milestone_dismiss_button")
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.TopCenter)
                ) {
                    drawMorCamFourPointStar(
                        center = Offset(size.width / 2f, size.height / 2f),
                        outerRadius = size.minDimension * 0.48f,
                        innerRadius = size.minDimension * 0.16f,
                        coreColor = Color.White,
                        edgeColor = Color(0xFFFDE68A)
                    )
                }
            }
        }
    }
}

/**
 * Sistem #18: Chronological Story Milestone Timeline Card.
 * Displayed inside both BotQuickProfileSheet (Character Quick Profile) and BotSettingsModal Tab 1 (Duygu).
 * Distinguishes numeric milestones (🛡️ Güven / 💜 Sevgi) from AI narrative milestones (✦ Hikaye Anı)
 * along a vertical amethyst-gold timeline spine stamped with the story day/date.
 */
@Composable
fun StoryMilestoneTimelineCard(
    milestones: List<MilestoneEntity>,
    modifier: Modifier = Modifier
) {
    val chronological = remember(milestones) {
        milestones.sortedByDescending { it.reachedAt }
    }
    MorCamGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("story_milestone_timeline_card"),
        cornerRadius = 18.dp,
        highlighted = chronological.isNotEmpty(),
        paddingHorizontal = 14.dp,
        paddingVertical = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFDE68A),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "İlişki & Hikaye Zaman Çizgisi",
                    fontFamily = LiterarySerifFontFamily,
                    color = MorCamTokens.SilverLavenderBright,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x553B1470))
                    .border(0.8.dp, Color(0x88D8B4FE), RoundedCornerShape(50))
                    .padding(horizontal = 9.dp, vertical = 2.5.dp)
            ) {
                Text(
                    text = "${chronological.size} an",
                    fontFamily = InterFontFamily,
                    color = Color(0xFFF3E8FF),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (chronological.isEmpty()) {
            Text(
                text = "Henüz kilometre taşı açılmadı. Hikayede güven, sevgi veya unutulmaz bir dönüm noktası yaşandığında bu zaman çizgisine mühürlenir.",
                fontFamily = InterFontFamily,
                color = MorCamTokens.LavenderMuted,
                fontSize = 11.5.sp,
                lineHeight = 16.5.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                chronological.forEachIndexed { idx, item ->
                    val isNarrative = item.isNarrative
                    val accentBorder = if (isNarrative) Color(0xBBFDE68A) else Color(0x88C084FC)
                    val nodeColor = if (isNarrative) Color(0xFFFDE68A) else Color(0xFFD8B4FE)
                    val badgeLabel = if (isNarrative) "✦ Hikaye Anı" else "Sayısal Eşik"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("milestone_timeline_item_${item.type}"),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            if (isNarrative) listOf(Color(0xBBF59E0B), Color(0x664C1D95))
                                            else listOf(Color(0xAA9333EA), Color(0x553B1470))
                                        )
                                    )
                                    .border(1.dp, accentBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.emoji.ifBlank { if (isNarrative) "✦" else "💜" },
                                    fontSize = 12.sp
                                )
                            }
                            if (idx < chronological.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .width(1.5.dp)
                                        .height(26.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(nodeColor.copy(alpha = 0.65f), Color(0x33A855F7))
                                            )
                                        )
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isNarrative) Color(0x452D1458) else Color(0x381B0B36)
                                )
                                .border(0.85.dp, accentBorder.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 11.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.displayTitle,
                                    fontFamily = LiterarySerifFontFamily,
                                    color = MorCamTokens.SilverLavenderBright,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isNarrative) Color(0x45F59E0B) else Color(0x457C3AED))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = badgeLabel,
                                        fontFamily = InterFontFamily,
                                        color = if (isNarrative) Color(0xFFFEF3C7) else Color(0xFFE9D5FF),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (item.narrativeDescription.isNotBlank()) {
                                Text(
                                    text = item.narrativeDescription,
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.LavenderLight.copy(alpha = 0.90f),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.storyStamp.ifBlank { "Hikaye Kaydı" },
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFFDE68A).copy(alpha = 0.88f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Sevgi ${item.affectionAtTime} • Güven ${item.trustAtTime}",
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.LavenderMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

internal data class ChatEmotionHudState(
    val pulseGlowAlpha: Float,
    val emotionAuraColor: Color,
    val affectionDeltaText: String?,
    val affectionDeltaTrigger: Int,
    val affectionDeltaPositive: Boolean
)

private val CHAT_MOOD_JOY_KEYWORDS = listOf("harika", "süper", "mutlu", "güldü", "🎉", "😊")
private val CHAT_MOOD_WARM_KEYWORDS = listOf("canım", "değerlisin", "sarıl", "güzelim", "sevgi", "💕", "❤️")
private val CHAT_MOOD_SAD_KEYWORDS = listOf("üzgün", "üzülüyorum", "kötü", "yalnız", "😢")
private val CHAT_MOOD_TENSE_KEYWORDS = listOf("tehlike", "sinirli", "öfke", "korktu")
private val CHAT_MOOD_CURIOUS_KEYWORDS = listOf("merak", "acaba", "ilginç")
private val CHAT_OVERLAY_TR_LOCALE = java.util.Locale.forLanguageTag("tr-TR")

internal fun detectChatMoodFromAssistantText(text: String): String {
    if (text.isBlank()) return "calm"
    val lower = text.lowercase(CHAT_OVERLAY_TR_LOCALE)
    return when {
        CHAT_MOOD_JOY_KEYWORDS.any { lower.contains(it) } -> "joy"
        CHAT_MOOD_WARM_KEYWORDS.any { lower.contains(it) } -> "warm"
        CHAT_MOOD_SAD_KEYWORDS.any { lower.contains(it) } -> "sad"
        CHAT_MOOD_TENSE_KEYWORDS.any { lower.contains(it) } -> "tense"
        CHAT_MOOD_CURIOUS_KEYWORDS.any { lower.contains(it) } -> "curious"
        else -> "calm"
    }
}

internal fun formatAffectionDeltaBadge(diff: Int): String? = when {
    diff > 0 -> "+$diff Sevgi ✦"
    diff < 0 -> "$diff Sevgi"
    else -> null
}

/**
 * Tracks ambient glow pulse, detected mood aura color, and floating affection delta badge for ChatScreen.
 */
@Composable
internal fun rememberChatEmotionHudState(
    bot: BotEntity,
    rawValidMessages: List<MessageEntity>
): ChatEmotionHudState {
    val motionConfig = LocalCosmicMotionConfig.current
    val pulseGlowAlpha: Float = if (motionConfig.enableAmbientLoops) {
        val infiniteTransition = rememberInfiniteTransition(label = "chatScreenAnimations")
        infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.90f,
            animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "pulseGlowAlpha"
        ).value
    } else 0.62f

    val lastAiText = remember(rawValidMessages) {
        rawValidMessages.lastOrNull { it.role == "assistant" }?.text ?: ""
    }
    val detectedMood = remember(lastAiText) { detectChatMoodFromAssistantText(lastAiText) }

    val emotionState = remember(bot.emotionState) { EmotionState.fromJson(bot.emotionState) }
    val emotionAuraColor by animateColorAsState(
        targetValue = when (detectedMood) {
            "joy" -> Color(0xFFFDE047)
            "warm" -> Color(0xFFF472B6)
            "sad" -> Color(0xFF818CF8)
            "tense" -> Color(0xFFFB7185)
            "curious" -> Color(0xFF38BDF8)
            else -> Color(0xFFC084FC)
        },
        animationSpec = tween(700),
        label = "emotionAuraColor"
    )

    var lastObservedAffection by remember(bot.id) { mutableIntStateOf(emotionState.affection) }
    var affectionDeltaText by remember(bot.id) { mutableStateOf<String?>(null) }
    var affectionDeltaTrigger by remember(bot.id) { mutableIntStateOf(0) }
    var affectionDeltaPositive by remember(bot.id) { mutableStateOf(true) }

    LaunchedEffect(emotionState.affection) {
        val diff = emotionState.affection - lastObservedAffection
        val badge = formatAffectionDeltaBadge(diff)
        if (badge != null) {
            affectionDeltaPositive = diff > 0
            affectionDeltaText = badge
            affectionDeltaTrigger += 1
            lastObservedAffection = emotionState.affection
        }
    }

    return ChatEmotionHudState(
        pulseGlowAlpha = pulseGlowAlpha,
        emotionAuraColor = emotionAuraColor,
        affectionDeltaText = affectionDeltaText,
        affectionDeltaTrigger = affectionDeltaTrigger,
        affectionDeltaPositive = affectionDeltaPositive
    )
}

/**
 * Hosts all secondary ChatScreen modal dialogs, bottom sheets, and floating celebration/status banners.
 */
@Composable
internal fun ChatScreenOverlaysHost(
    bot: BotEntity,
    displayName: String,
    messages: List<MessageEntity>,
    keyCharacters: List<KeyCharacter>,
    characterEmotions: List<CharacterEmotionEntity>,
    affectionEvents: List<AffectionEventEntity>,
    activeMilestones: List<MilestoneEntity>,
    starredMessages: List<MessageEntity>,
    memoryCommitNotice: EmochiViewModel.MemoryCommitNotice?,
    pendingCastMember: CastMemberEntity?,
    rewindTargetMessageId: String?,
    activeMilestone: MilestoneEntity?,
    showBotSettings: Boolean,
    showBackgroundPicker: Boolean,
    showQuickNeuralVault: Boolean,
    showStarredSheet: Boolean,
    showQuickProfile: Boolean,
    viewModel: EmochiViewModel,
    onDismissBackgroundPicker: () -> Unit,
    onOpenBotSettings: () -> Unit,
    onDismissQuickNeuralVault: () -> Unit,
    onOpenQuickNeuralVault: () -> Unit,
    onDismissRewind: () -> Unit,
    onConfirmRewind: (String) -> Unit,
    onDismissMilestone: () -> Unit,
    onOpenQuickProfile: () -> Unit,
    onDismissQuickProfile: () -> Unit,
    onDismissStarredSheet: () -> Unit,
    onJumpToMessage: (String) -> Unit,
    onSaveBotProfile: (BotEntity, List<KeyCharacter>) -> Unit,
    onResetChat: (Boolean) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    if (showBackgroundPicker) {
        ChatBackgroundPickerDialog(
            bot = bot,
            onDismiss = onDismissBackgroundPicker,
            onUseAvatarBg = {
                onSaveBotProfile(bot.copy(chatBgUrl = "USE_AVATAR"), keyCharacters)
                onDismissBackgroundPicker()
            },
            onUseDefaultThemeBg = {
                onSaveBotProfile(bot.copy(chatBgUrl = ""), keyCharacters)
                onDismissBackgroundPicker()
            },
            onOpenSettingsForGallery = {
                onDismissBackgroundPicker()
                onOpenBotSettings()
            }
        )
    }

    ChatMemoryCommitBanner(
        notice = memoryCommitNotice,
        visible = !showBotSettings && !showQuickNeuralVault,
        onOpenVault = {
            viewModel.dismissMemoryCommitNotice()
            onOpenQuickNeuralVault()
        }
    )

    if (showQuickNeuralVault) {
        NeuralVaultModal(
            bot = bot,
            viewModel = viewModel,
            onDismiss = onDismissQuickNeuralVault,
            onSaveMemory = { updatedBot -> onSaveBotProfile(updatedBot, keyCharacters) }
        )
    }

    ChatPendingCastBanner(
        pendingCastMember = pendingCastMember,
        visible = !showBotSettings,
        onConfirm = { viewModel.confirmPendingCastMember(bot.id, it) },
        onDismissTemporarily = { viewModel.dismissCastMemberTemporarily(bot.id, it) },
        onBlacklist = { viewModel.blacklistAndDismissCastMember(bot.id, it) }
    )

    ChatRewindConfirmDialog(
        targetMessageId = rewindTargetMessageId,
        onDismiss = onDismissRewind,
        onConfirmRewind = onConfirmRewind
    )

    ChatMilestoneCelebrationBanner(
        activeMilestone = activeMilestone,
        onDismiss = onDismissMilestone,
        onOpenTimeline = {
            onDismissMilestone()
            onOpenQuickProfile()
        }
    )

    if (showStarredSheet) {
        ChatStarredMessagesSheet(
            bot = bot,
            starredMessages = starredMessages,
            onLoadStarred = { viewModel.loadStarredMessages() },
            onDismiss = onDismissStarredSheet,
            onJumpToMessage = { targetMsgId ->
                onDismissStarredSheet()
                onJumpToMessage(targetMsgId)
            },
            onUnstarMessage = { msgId -> viewModel.toggleMessageStar(msgId) }
        )
    }

    if (showQuickProfile) {
        BotQuickProfileSheet(
            bot = bot,
            keyCharacters = keyCharacters,
            characterEmotions = characterEmotions,
            affectionEvents = affectionEvents,
            milestones = activeMilestones,
            onDismiss = onDismissQuickProfile,
            onSaveBot = { updatedBot -> onSaveBotProfile(updatedBot, keyCharacters) },
            onOpenFullSettings = onOpenBotSettings,
            onResetChat = { fullReset ->
                onDismissQuickProfile()
                onResetChat(fullReset)
            },
            onExportChat = {
                val fullText = messages.joinToString("\n\n") {
                    "${if (it.role == "user") bot.userCharName.ifBlank { "Kullanıcı" } else displayName}: ${it.text}"
                }
                clipboardManager.setText(AnnotatedString(fullText))
                android.widget.Toast.makeText(context, "Tüm sohbet metni panoya kopyalandı!", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Handles smooth IME-open scroll and action-menu/reaction-picker overflow scroll in ChatScreen.
 */
@Composable
internal fun ChatImeAndMenuScrollEffects(
    listState: LazyListState,
    isImeVisible: Boolean,
    hasLoadOlderHeader: Boolean,
    validMessagesCount: Int,
    isSending: Boolean,
    expandedMenuMessageId: String?,
    expandedUserMenuMessageId: String?,
    reactionPickerMessageId: String?
) {
    val density = LocalDensity.current
    LaunchedEffect(isImeVisible) {
        if (isImeVisible && (reactionPickerMessageId ?: expandedMenuMessageId ?: expandedUserMenuMessageId) == null) {
            val totalItems = (if (hasLoadOlderHeader) 1 else 0) + validMessagesCount + if (isSending) 1 else 0
            if (totalItems > 0) {
                delay(90)
                try { listState.animateScrollToItem(totalItems - 1) } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(expandedMenuMessageId, expandedUserMenuMessageId, reactionPickerMessageId, isImeVisible) {
        val targetId = reactionPickerMessageId ?: expandedMenuMessageId ?: expandedUserMenuMessageId ?: return@LaunchedEffect
        delay(110)
        try {
            val layoutInfo = listState.layoutInfo
            val itemInfo = layoutInfo.visibleItemsInfo.find { it.key == targetId } ?: return@LaunchedEffect
            val visibleBottom = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
            val itemBottom = itemInfo.offset + itemInfo.size
            val overflowPx = (itemBottom + with(density) { 58.dp.toPx() }) - visibleBottom
            if (overflowPx > 0f) {
                listState.animateScrollBy(overflowPx, tween(260, easing = FastOutSlowInEasing))
            } else if (itemBottom > visibleBottom * 0.55f) {
                listState.animateScrollBy(with(density) { 38.dp.toPx() }, tween(240, easing = FastOutSlowInEasing))
            }
        } catch (_: Exception) {}
    }
}

/**
 * Renders an optional user-selected custom chat wallpaper using memory-safe RGB_565 Coil decoding.
 */
@Composable
internal fun ChatCustomBackgroundLayer(bot: BotEntity) {
    val context = LocalContext.current
    if (bot.chatBgUrl.isNotBlank() && bot.chatBgUrl != "USE_AVATAR" && bot.id != "starter_aetheria" && bot.id != "starter_aiden_stories") {
        val chatBgRequest = remember(bot.chatBgUrl, context) {
            ImageRequest.Builder(context)
                .data(bot.chatBgUrl)
                .allowRgb565(true)
                .crossfade(250)
                .build()
        }
        AsyncImage(
            model = chatBgRequest,
            contentDescription = "Sohbet Arka Planı",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.85f
        )
    }
}



