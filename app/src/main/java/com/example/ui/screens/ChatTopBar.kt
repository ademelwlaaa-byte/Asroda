package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.animation.CosmicVoiceEqualizer
import com.example.ui.components.MorCamChipPill
import com.example.ui.components.MorCamCircleIconButton
import com.example.ui.components.MorCamGlassTextField
import com.example.ui.components.MorCamTokens
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.local.BotEntity
import com.example.ui.animation.CosmicFloatingDeltaPop
import com.example.ui.theme.DisplaySerifFontFamily
import kotlinx.coroutines.flow.StateFlow

/**
 * Modular 1:1 Mor Cam Chat Top Bar combining the translucent glass header plate,
 * sigil/avatar medallion with floating affection delta pop, 4 aligned touch targets,
 * live Search Bar + Yıldızlı pill, and isolated TTS status banner.
 */
@Composable
internal fun ChatHeaderTopBar(
    bot: BotEntity,
    headerTitle: String,
    avatarInitial: String,
    emotionAuraColor: Color,
    pulseGlowAlpha: Float,
    affectionDeltaTrigger: Int,
    affectionDeltaText: String?,
    affectionDeltaPositive: Boolean,
    refGlassBitmaps: AetheriaRefGlassBitmaps,
    showSearchBar: Boolean,
    localSearchQuery: String,
    matchCount: Int,
    activeMatchIndex: Int,
    starredCount: Int,
    speakingMessageIdFlow: StateFlow<String?>,
    isImeVisible: Boolean,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    onBackClick: () -> Unit,
    onQuickProfileClick: () -> Unit,
    onToggleSearch: () -> Unit,
    onSettingsClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onPrevMatch: () -> Unit,
    onNextMatch: () -> Unit,
    onOpenStarredSheet: () -> Unit,
    onStopSpeaking: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 4.dp, bottom = 6.dp)
                .animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(841f / 136f)
            ) {
                val headerW = maxWidth
                val headerH = maxHeight

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (refGlassBitmaps.headerNoLetter.width > 1) {
                        drawImage(
                            image = refGlassBitmaps.headerNoLetter,
                            dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
                            dstSize = IntSize(
                                kotlin.math.round(size.width).toInt().coerceAtLeast(1),
                                kotlin.math.round(size.height).toInt().coerceAtLeast(1)
                            ),
                            filterQuality = FilterQuality.High
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(
                            x = headerW * (145f / 841f),
                            y = headerH * (30f / 136f)
                        )
                        .size(
                            width = headerW * (72f / 841f),
                            height = headerH * (76f / 136f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val fallbackInitial: @Composable () -> Unit = {
                        Text(
                            text = avatarInitial,
                            fontSize = 23.sp,
                            fontFamily = DisplaySerifFontFamily,
                            fontWeight = FontWeight.Normal,
                            style = TextStyle(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color(0xFFF3E8FF),
                                        Color(0xFFD8B4FE)
                                    )
                                ),
                                shadow = Shadow(
                                    color = Color(0xCCA855F7),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 10f
                                )
                            )
                        )
                    }
                    if (bot.avatarUrl.isNotBlank()) {
                        com.example.ui.components.BotAvatarImage(
                            avatarUrl = bot.avatarUrl,
                            contentDescription = headerTitle,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .size(headerW * (54f / 841f))
                                .clip(CircleShape)
                                .border(
                                    1.35.dp,
                                    emotionAuraColor.copy(alpha = 0.45f + 0.35f * pulseGlowAlpha),
                                    CircleShape
                                ),
                            fallback = fallbackInitial
                        )
                    } else {
                        fallbackInitial()
                    }
                    if (affectionDeltaTrigger > 0) {
                        CosmicFloatingDeltaPop(
                            deltaText = affectionDeltaText,
                            triggerKey = affectionDeltaTrigger,
                            color = if (affectionDeltaPositive) Color(0xFF4ADE80) else Color(0xFFF472B6),
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(
                            x = headerW * (248f / 841f),
                            y = headerH * (24f / 136f)
                        )
                        .size(
                            width = headerW * (360f / 841f),
                            height = headerH * (46f / 136f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = headerTitle,
                        color = Color(0xFFFBF8FF),
                        fontSize = if (headerTitle.length > 20) 14.5.sp else 15.5.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = LiterarySerifFontFamily,
                        letterSpacing = 0.1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xCC120524),
                                offset = Offset(0f, 1.5f),
                                blurRadius = 6f
                            )
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.145f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onBackClick
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.595f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onQuickProfileClick
                            )
                            .testTag("chat_header_quick_profile_button")
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.125f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleSearch
                            )
                            .testTag("chat_search_toggle_button")
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.135f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSettingsClick
                            )
                            .testTag("bot_settings_button")
                    )
                }
            }

            ChatSearchBarAndNavigation(
                visible = showSearchBar,
                localSearchQuery = localSearchQuery,
                matchCount = matchCount,
                activeMatchIndex = activeMatchIndex,
                starredCount = starredCount,
                rootSize = rootSize,
                blurredBgBitmap = blurredBgBitmap,
                onQueryChange = onQueryChange,
                onClearQuery = onClearQuery,
                onPrevMatch = onPrevMatch,
                onNextMatch = onNextMatch,
                onOpenStarredSheet = onOpenStarredSheet
            )

            ChatSpeakingStatusBanner(
                speakingMessageIdFlow = speakingMessageIdFlow,
                isImeVisible = isImeVisible,
                rootSize = rootSize,
                blurredBgBitmap = blurredBgBitmap,
                onStopSpeaking = onStopSpeaking
            )
        }
    }
}

/**
 * Modular Mor Cam Search Bar with:
 * - Instant query filtering
 * - Direct ⭐ Yıldızlananlar Mor Cam shortcut pill
 * - Live `1 / N` result counter + ↑ / ↓ match navigation controls
 */
@Composable
internal fun ChatSearchBarAndNavigation(
    visible: Boolean,
    localSearchQuery: String,
    matchCount: Int,
    activeMatchIndex: Int,
    starredCount: Int,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onPrevMatch: () -> Unit,
    onNextMatch: () -> Unit,
    onOpenStarredSheet: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(220, easing = FastOutSlowInEasing)) { -it } + fadeIn(tween(220)),
        exit = slideOutVertically(tween(180)) { -it } + fadeOut(tween(180))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MorCamGlassTextField(
                    value = localSearchQuery,
                    onValueChange = onQueryChange,
                    placeholder = "Mesajlarda ara...",
                    leadingIcon = Icons.Default.Search,
                    singleLine = true,
                    minHeight = 46.dp,
                    modifier = Modifier
                        .weight(1f)
                        .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.55f)
                        .testTag("chat_search_input"),
                    trailingContent = if (localSearchQuery.isNotEmpty()) {
                        {
                            MorCamCircleIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Aramayı Temizle",
                                onClick = onClearQuery,
                                size = 36.dp
                            )
                        }
                    } else null
                )

                // Direct Mor Cam ⭐ Yıldızlananlar shortcut pill
                Row(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(50))
                        .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.62f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x883B1470), Color(0x99240C4A))
                            )
                        )
                        .border(
                            BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0xFFFDE68A), Color(0xFFA855F7)))),
                            RoundedCornerShape(50)
                        )
                        .clickable(onClick = onOpenStarredSheet)
                        .padding(horizontal = 12.dp)
                        .testTag("chat_search_starred_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Yıldızlanan Mesajlar",
                        tint = Color(0xFFFDE68A),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (starredCount > 0) "Yıldızlı ($starredCount)" else "Yıldızlı",
                        color = Color(0xFFFAF5FF),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = InterFontFamily
                    )
                }
            }

            // Live search navigation bar (1 / N + ↑ ↓)
            AnimatedVisibility(
                visible = localSearchQuery.isNotBlank(),
                enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                exit = fadeOut(tween(140)) + shrinkVertically(tween(140))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.58f)
                        .background(Color(0x881A0A36))
                        .border(BorderStroke(0.85.dp, MorCamTokens.RimGradient), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (matchCount > 0) {
                            "${(activeMatchIndex + 1).coerceIn(1, matchCount)} / $matchCount eşleşme"
                        } else {
                            "Eşleşme bulunamadı"
                        },
                        color = if (matchCount > 0) MorCamTokens.SilverLavenderBright else MorCamTokens.LavenderMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = InterFontFamily,
                        modifier = Modifier.testTag("chat_search_match_counter")
                    )

                    if (matchCount > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x664C1D95))
                                    .border(0.85.dp, Color(0xAAD8B4FE), CircleShape)
                                    .clickable(onClick = onPrevMatch)
                                    .testTag("chat_search_prev_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Önceki eşleşme",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x664C1D95))
                                    .border(0.85.dp, Color(0xAAD8B4FE), CircleShape)
                                    .clickable(onClick = onNextMatch)
                                    .testTag("chat_search_next_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Sonraki eşleşme",
                                    tint = Color.White,
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
 * Isolated TTS status banner that collects speakingMessageIdFlow locally,
 * preventing top-level ChatScreen recompositions when TTS starts or stops.
 */
@Composable
internal fun ChatSpeakingStatusBanner(
    speakingMessageIdFlow: StateFlow<String?>,
    isImeVisible: Boolean,
    rootSize: IntSize,
    blurredBgBitmap: ImageBitmap,
    onStopSpeaking: () -> Unit
) {
    val speakingMessageId by speakingMessageIdFlow.collectAsStateWithLifecycle()
    val isSpeaking = speakingMessageId != null

    AnimatedVisibility(
        visible = isSpeaking && !isImeVisible,
        enter = fadeIn(tween(200)) + expandVertically(tween(200)),
        exit = fadeOut(tween(160)) + shrinkVertically(tween(160))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(50))
                .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.62f)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xA6200D44), Color(0xB814082C), Color(0xA6200D44))
                    )
                )
                .border(BorderStroke(1.dp, MorCamTokens.RimGradient), RoundedCornerShape(50))
                .padding(start = 14.dp, end = 6.dp, top = 5.dp, bottom = 5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CosmicVoiceEqualizer(
                        active = isSpeaking,
                        barCount = 5,
                        color = MorCamTokens.LavenderLight,
                        height = 15.dp
                    )
                    Text(
                        text = "Sesli okunuyor...",
                        color = MorCamTokens.SilverLavenderBright,
                        fontFamily = LiterarySerifFontFamily,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                MorCamChipPill(
                    label = "Durdur",
                    selected = true,
                    icon = Icons.Default.Stop,
                    onClick = onStopSpeaking
                )
            }
        }
    }
}
