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
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.ui.components.MorCamBottomSheetShell
import com.example.ui.components.MorCamButtonVariant
import com.example.ui.components.MorCamCapsuleButton
import com.example.ui.components.MorCamChipPill
import com.example.ui.components.MorCamDialogShell
import com.example.ui.components.MorCamDivider
import com.example.ui.components.MorCamGlassCard
import com.example.ui.components.MorCamTokens
import com.example.ui.components.drawMorCamFourPointStar
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import com.example.ui.viewmodel.EmochiViewModel

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

                            Text(
                                text = cleanCopyText(starred.text).take(220) + if (starred.text.length > 220) "…" else "",
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
