package com.example.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.BotEntity
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.CosmicSparkleBurst
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.LocalCosmicMotionPolicy
import com.example.ui.animation.cosmicStaggeredEnter
import com.example.ui.animation.rememberCosmicPhaseState
import com.example.ui.components.BotAvatarImage
import com.example.ui.components.MoodColors
import com.example.ui.components.MorCamButtonVariant
import com.example.ui.components.MorCamCapsuleButton
import com.example.ui.components.MorCamDialogShell
import com.example.ui.components.MorCamDivider
import com.example.ui.components.MorCamPopupMenu
import com.example.ui.components.MorCamPopupMenuItem
import com.example.ui.components.MorCamTokens
import com.example.ui.components.OrbView
import com.example.ui.components.StoryOverlayTextSanitizer
import com.example.ui.theme.LiterarySerifFontFamily
import com.example.util.MessageTimestampFormatter

private val BotCardShape = RoundedCornerShape(24.dp)
private val BotCardAvatarShape = RoundedCornerShape(18.dp)
private val BotCardPillShape = RoundedCornerShape(50)

private val BotCardBgBrush = Brush.linearGradient(
    colors = listOf(Color(0x5C220F40), Color(0x4C130827), Color(0x561E0D3A)),
    start = Offset(0f, 0f),
    end = Offset(900f, 450f)
)
private val BotCardBorderBrush = Brush.linearGradient(
    listOf(Color(0xDCE9D5FF), Color(0x889F7AEA), Color(0x665B21B6), Color(0xCCD8B4FE))
)
private val BotCardGlassRefractionBrush = Brush.verticalGradient(
    0.00f to Color(0x28E9D5FF),
    0.16f to Color.Transparent,
    0.84f to Color.Transparent,
    1.00f to Color(0x207C3AED)
)
private val BotCardLeftBarBrush = Brush.verticalGradient(
    listOf(Color.Transparent, Color(0xCCE9D5FF), Color(0xFFE9D5FF), Color(0xCCE9D5FF), Color.Transparent)
)
private val BotAvatarBorderBrush = Brush.linearGradient(
    listOf(Color(0xEAF3E8FF), Color(0xA8C084FC), Color(0x887C3AED), Color(0xDCE9D5FF))
)
private val BotStatusBadgeBorderBrush = Brush.linearGradient(
    listOf(Color(0xCCE9D5FF), Color(0x887C3AED))
)
private val BotOnlineDotBrush = Brush.radialGradient(
    listOf(Color(0xFFA7F3D0), Color(0xFF34D399), Color(0xFF10B981))
)
private val BotMoreExpandedBgBrush = Brush.radialGradient(
    listOf(Color(0xA67C3AED), Color(0x883B1470))
)
private val BotMoreCollapsedBgBrush = Brush.verticalGradient(
    listOf(Color(0x5231175A), Color(0x46190B33))
)
private val BotMoreCollapsedBorderBrush = Brush.linearGradient(
    listOf(Color(0xCCE9D5FF), Color(0x668B5CF6))
)
private val BotPlayOrbBgBrush = Brush.radialGradient(
    colors = listOf(Color(0x9C9F55F7), Color(0x887C3AED), Color(0x7C4C1D95), Color(0x882E1065)),
    center = Offset(38f, 28f),
    radius = 74f
)
private val BotPlayOrbBorderBrush = Brush.verticalGradient(
    listOf(Color(0xFFF5F0FF), Color(0xFFD8B4FE), Color(0xFF9333EA), Color(0xFFE9D5FF))
)
private val BotTagPillBgBrush = Brush.verticalGradient(
    listOf(Color(0x5C2C1554), Color(0x4C16092E))
)
private val BotTagPillBorderBrush = Brush.linearGradient(
    listOf(Color(0xBBE9D5FF), Color(0x668B5CF6))
)
private val BotTitleShadowStyle = TextStyle(
    shadow = Shadow(
        color = Color(0xAA120524),
        offset = Offset(0f, 1.5f),
        blurRadius = 6f
    )
)

internal fun resolveBotCardDisplayName(
    botId: String,
    mode: String,
    universeName: String,
    aiName: String
): String {
    val isUniverse = mode == "universe"
    return when {
        botId == "starter_aetheria" || universeName.trim().startsWith("Aetheria: Neon", ignoreCase = true) -> "Aetheria"
        isUniverse -> universeName.trim().ifBlank { aiName.trim().ifBlank { "Evren" } }
        else -> aiName.trim().ifBlank { "Karakter" }
    }
}

internal fun resolveBotCardDescription(
    botId: String,
    scenario: String,
    openingMessage: String
): String = when (botId) {
    "starter_ayla" -> "Lise/üniversiteden beri en yakın dostun. Akşam saatlerinde hep yanında."
    "starter_aetheria" -> "Neon ışıklarıyla aydınlatılmış Aetheria şehrinin yüksek kulelerinde geçen macera."
    else -> StoryOverlayTextSanitizer.sanitizePreview(
        scenario.ifBlank { openingMessage },
        maxLength = 220
    )
}

@DrawableRes
internal fun resolveBuiltInBotAvatarRes(bot: BotEntity): Int? {
    // 1. Direct built-in preset ID match first (highest precision)
    when (bot.id) {
        "starter_ayla" -> return R.drawable.img_ayla_avatar
        "starter_aetheria" -> return R.drawable.img_aetheria_universe
        "preset_aiden_mcu_cosmic" -> return R.drawable.img_aiden_blackwood_avatar
        "preset_aiden_zoktay" -> return R.drawable.img_aiden_wide_thumb1_gs
        "preset_aiden_dispatch" -> return R.drawable.img_aiden_wide_thumb2_dispatch
        "preset_aiden_joker_emma" -> return R.drawable.img_aiden_wide_thumb3_joker
        "preset_aiden_doctor_jenna" -> return R.drawable.img_aiden_wide_thumb4_hospital
        "preset_aiden_obsidian_sydney" -> return R.drawable.img_aiden_wide_thumb5_tokyo
        "preset_aiden_nevermore_wednesday" -> return R.drawable.img_aiden_wide_thumb6_nevermore_1795547716938
        "preset_aiden_lambo_football" -> return R.drawable.img_aiden_modal_carousel_gs
        "preset_aiden_marvel_thanos" -> return R.drawable.img_postcredits_twin_aiden
        "preset_aiden_impossible_spiderman" -> return R.drawable.img_ch1_c5_tower_night
        "preset_aiden_eilish_family" -> return R.drawable.img_aiden_portrait
        "preset_aria" -> return R.drawable.img_template_aria_left
        "preset_eldoria" -> return R.drawable.img_template_eldoria_left
        "preset_tokyo" -> return R.drawable.aiden_dispatch
        "preset_crimson_clocktower" -> return R.drawable.img_tpl_clocktower_1795553971128
        "preset_neon_kowloon_noir" -> return R.drawable.img_tpl_neon_noir_1795553970903
        "preset_stormbreak_corsairs" -> return R.drawable.img_aetheria_universe
        "preset_aurora_lodge" -> return R.drawable.img_tpl_aurora_lodge_1795553969948
    }

    // 2. Match cloned / UUID-instantiated bots via title/name first, then scenario metadata
    val titleMeta = "${bot.aiName} ${bot.universeName}"
    val fullMeta = "$titleMeta ${bot.scenario}"
    return when {
        bot.aiName.equals("Ayla", ignoreCase = true) ->
            R.drawable.img_ayla_avatar
        bot.universeName.contains("Aetheria", ignoreCase = true) ->
            R.drawable.img_aetheria_universe
        titleMeta.contains("Kozmik Sürükleniş", ignoreCase = true) ||
            titleMeta.contains("Cosmic Drift", ignoreCase = true) ->
            R.drawable.img_aiden_blackwood_avatar
        fullMeta.contains("Zoktay", ignoreCase = true) ||
            fullMeta.contains("Galatasaray", ignoreCase = true) ->
            R.drawable.img_aiden_wide_thumb1_gs
        fullMeta.contains("Dispatch", ignoreCase = true) ||
            fullMeta.contains("Blonde Blazer", ignoreCase = true) ->
            R.drawable.img_aiden_wide_thumb2_dispatch
        fullMeta.contains("Viren City", ignoreCase = true) ||
            fullMeta.contains("Joker", ignoreCase = true) ||
            titleMeta.contains("Emma Myers", ignoreCase = true) ->
            R.drawable.img_aiden_wide_thumb3_joker
        fullMeta.contains("Presbyterian", ignoreCase = true) ||
            fullMeta.contains("Cerrah", ignoreCase = true) ||
            fullMeta.contains("Mucize Doktor", ignoreCase = true) ||
            fullMeta.contains("Miracle Doctor", ignoreCase = true) ||
            fullMeta.contains("Jenna", ignoreCase = true) ->
            R.drawable.img_aiden_wide_thumb4_hospital
        fullMeta.contains("Obsidian", ignoreCase = true) ||
            fullMeta.contains("Sydney", ignoreCase = true) ||
            fullMeta.contains("Shibuya", ignoreCase = true) ->
            R.drawable.img_aiden_wide_thumb5_tokyo
        fullMeta.contains("Nevermore", ignoreCase = true) ||
            fullMeta.contains("Wednesday", ignoreCase = true) ->
            R.drawable.img_aiden_wide_thumb6_nevermore_1795547716938
        fullMeta.contains("Lamborghini", ignoreCase = true) ||
            fullMeta.contains("Lambo", ignoreCase = true) ->
            R.drawable.img_aiden_modal_carousel_gs
        fullMeta.contains("Thanos", ignoreCase = true) ->
            R.drawable.img_postcredits_twin_aiden
        fullMeta.contains("Spider-Man", ignoreCase = true) ||
            fullMeta.contains("Queens", ignoreCase = true) ->
            R.drawable.img_ch1_c5_tower_night
        fullMeta.contains("Eilish", ignoreCase = true) ||
            fullMeta.contains("Billie", ignoreCase = true) ->
            R.drawable.img_aiden_portrait
        bot.aiName.startsWith("Aria", ignoreCase = true) ->
            R.drawable.img_template_aria_left
        bot.universeName.contains("Eldoria", ignoreCase = true) ->
            R.drawable.img_template_eldoria_left
        fullMeta.contains("Saat Kulesi", ignoreCase = true) ||
            fullMeta.contains("Clocktower", ignoreCase = true) ->
            R.drawable.img_tpl_clocktower_1795553971128
        fullMeta.contains("Kowloon", ignoreCase = true) ->
            R.drawable.img_tpl_neon_noir_1795553970903
        fullMeta.contains("Fırtına Kıyısı", ignoreCase = true) ||
            fullMeta.contains("Stormbreak", ignoreCase = true) ->
            R.drawable.img_aetheria_universe
        fullMeta.contains("Kuzey Işıkları Oteli", ignoreCase = true) ||
            fullMeta.contains("Aurora Lodge", ignoreCase = true) ->
            R.drawable.img_tpl_aurora_lodge_1795553969948
        else -> null
    }
}

@Composable
fun BotCardItem(
    bot: BotEntity,
    @Suppress("UNUSED_PARAMETER") confirmDeleteId: String?,
    @Suppress("UNUSED_PARAMETER") onConfirmDeleteChange: (String?) -> Unit,
    onOpenBot: (String) -> Unit,
    onDeleteBot: ((String) -> Unit)? = null,
    onTogglePrivacy: ((BotEntity) -> Unit)? = null,
    onCloneBot: ((String) -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    frostedBackdrop: ImageBitmap? = null,
    scrollSync: Int = 0,
    staggerIndex: Int = 0,
    lastMessagePreview: String? = null
) {
    val motionConfig = LocalCosmicMotionConfig.current
    val motionPolicy = LocalCosmicMotionPolicy.current
    val isUniverse = bot.mode == "universe"
    val displayName = remember(bot.id, bot.mode, bot.universeName, bot.aiName) {
        resolveBotCardDisplayName(bot.id, bot.mode, bot.universeName, bot.aiName)
    }
    val hue = remember(isUniverse) { MoodColors.getMoodHue(if (isUniverse) "curious" else "calm") }
    val isTemplate = remember(bot) { !com.example.data.repository.EmochiRepository.isBotPublishable(bot) }

    var isMenuExpanded by remember(bot.id) { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember(bot.id) { mutableStateOf(false) }
    var favoriteBurstTrigger by remember(bot.id) { mutableIntStateOf(0) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScaleState = animateFloatAsState(
        targetValue = if (isPressed && motionConfig.enableMicroSprings) 0.972f else 1f,
        animationSpec = CosmicMotionSpecs.snappyOrb(),
        label = "cardPressScale"
    )

    val starScaleState = animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1f,
        animationSpec = CosmicMotionSpecs.playfulSpark(),
        label = "favoriteStarScale"
    )

    // Draw-phase-only State<Float> glow driver -> 0 FPS root recomposition per card in LazyColumn!
    val animOffset = remember(bot.id) { (bot.id.hashCode().and(0xFF)) / 255f }
    val allowAmbient = motionPolicy.shouldAnimateContinuously && motionConfig.enableAmbientLoops
    val cardGlowAlphaState = rememberCosmicPhaseState(
        durationMs = 2600 + (animOffset * 600).toInt(),
        initialValue = 0.72f,
        targetValue = 1.0f,
        staticValue = 0.88f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "cardGlow_${bot.id}",
        enabled = allowAmbient
    )

    val context = LocalContext.current
    val resolvedBackdrop = frostedBackdrop ?: remember(context) {
        createFrostedGothicManorBitmap(context, R.drawable.img_botlist_gothic_wisteria_bg)
    }

    // Determine meaningful tags from bot metadata
    val primaryTag: Pair<ImageVector, String> = remember(bot.id, bot.mode, bot.writingStyle, bot.userCharName) {
        when {
            isUniverse -> Icons.Default.Public to "Evren"
            bot.id == "preset_aria" || bot.userCharName.contains("Dedektif", ignoreCase = true) ->
                Icons.Default.Person to "Dedektif"
            bot.writingStyle.equals("chat", ignoreCase = true) ->
                Icons.Default.Forum to "Sohbet"
            else -> Icons.Default.SportsEsports to "Roleplay"
        }
    }
    val secondaryTag: Pair<ImageVector, String> = remember(bot.id, bot.intensity, bot.isNsfw, bot.mode) {
        when {
            bot.id == "preset_aria" -> Icons.Default.AutoAwesome to "Dram"
            bot.intensity.equals("intense", ignoreCase = true) -> Icons.Default.AutoAwesome to "Yoğun"
            isUniverse -> Icons.Default.AutoAwesome to "Fantastik"
            else -> Icons.Default.AutoAwesome to "Normal"
        }
    }

    val isRecentActiveChat = remember(bot.lastMessageTimestamp) {
        bot.lastMessageTimestamp > 0L &&
            (System.currentTimeMillis() - bot.lastMessageTimestamp) <= 86_400_000L
    }
    val isOnlineActive = isRecentActiveChat ||
        bot.proactiveEnabled ||
        bot.id == "starter_ayla"

    val relativeTimeLabel = remember(bot.lastMessageTimestamp) {
        if (bot.lastMessageTimestamp > 0L) {
            MessageTimestampFormatter.formatRelative(bot.lastMessageTimestamp)
        } else {
            "Yeni"
        }
    }

    val cardDescription = remember(bot.id, bot.scenario, bot.openingMessage, lastMessagePreview) {
        lastMessagePreview?.takeIf { it.isNotBlank() }
            ?: resolveBotCardDescription(bot.id, bot.scenario, bot.openingMessage)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .cosmicStaggeredEnter(index = staggerIndex, key = bot.id)
            .graphicsLayer {
                val s = cardScaleState.value
                scaleX = s
                scaleY = s
            }
            .drawWithCache {
                val cr = 24.dp.toPx()
                val baseGlow = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 1.6.dp.toPx()
                    maskFilter = android.graphics.BlurMaskFilter(
                        7.dp.toPx(),
                        android.graphics.BlurMaskFilter.Blur.NORMAL
                    )
                }
                val glintPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 1.15.dp.toPx()
                    maskFilter = android.graphics.BlurMaskFilter(
                        4.dp.toPx(),
                        android.graphics.BlurMaskFilter.Blur.NORMAL
                    )
                }
                val glintStart = 24.dp.toPx()
                val glintTopEnd = size.width * 0.36f
                val glintBotStart = size.width * 0.62f
                val glintBotEnd = size.width - 24.dp.toPx()
                onDrawBehind {
                    val glowAlpha = cardGlowAlphaState.value.coerceIn(0f, 1f)
                    baseGlow.color = Color(0xFF9333EA).copy(alpha = 0.24f * glowAlpha).toArgb()
                    glintPaint.color = Color(0xFFE9D5FF).copy(alpha = 0.28f * glowAlpha).toArgb()
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, cr, cr, baseGlow)
                        canvas.nativeCanvas.drawLine(glintStart, 0f, glintTopEnd, 0f, glintPaint)
                        canvas.nativeCanvas.drawLine(glintBotStart, size.height, glintBotEnd, size.height, glintPaint)
                    }
                }
            }
            .clip(BotCardShape)
            .templatesBackdropBlur(
                blurredBitmap = resolvedBackdrop,
                alpha = 0.78f,
                scrollSync = scrollSync
            )
            .background(BotCardBgBrush)
            .drawWithCache {
                val cr = 24.dp.toPx()
                val edgeFrost = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 4.5.dp.toPx()
                    color = android.graphics.Color.argb(34, 232, 214, 255)
                    maskFilter = android.graphics.BlurMaskFilter(
                        4.dp.toPx(),
                        android.graphics.BlurMaskFilter.Blur.NORMAL
                    )
                }
                val barW = 2.6.dp.toPx()
                val barH = (size.height * 0.36f).coerceIn(30.dp.toPx(), 52.dp.toPx())
                val barTopLeft = Offset(1.dp.toPx(), (size.height - barH) * 0.5f)
                val barSize = Size(barW, barH)
                val barCorner = CornerRadius(barW, barW)
                val poolCenter = Offset(size.width - 36.dp.toPx(), size.height - 28.dp.toPx())
                val poolRadius = 78.dp.toPx()
                val poolBrush = Brush.radialGradient(
                    colors = listOf(Color(0x229333EA), Color(0x0C6D28D9), Color.Transparent),
                    center = poolCenter,
                    radius = poolRadius
                )
                onDrawBehind {
                    drawRect(brush = BotCardGlassRefractionBrush)
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, cr, cr, edgeFrost)
                    }
                    drawRoundRect(
                        brush = BotCardLeftBarBrush,
                        topLeft = barTopLeft,
                        size = barSize,
                        cornerRadius = barCorner
                    )
                    drawCircle(
                        brush = poolBrush,
                        center = poolCenter,
                        radius = poolRadius
                    )
                }
            }
            .border(BorderStroke(width = 1.15.dp, brush = BotCardBorderBrush), BotCardShape)
            .clickable(interactionSource = interactionSource, indication = null) { onOpenBot(bot.id) }
            .testTag("bot_card_${bot.id}")
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: 1:1 Rounded-Square Portrait Thumbnail with Status Orb
            Box(
                modifier = Modifier
                    .size(106.dp)
                    .aspectRatio(1f)
                    .drawWithCache {
                        val r = 18.dp.toPx()
                        val avatarHalo = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 1.8.dp.toPx()
                            maskFilter = android.graphics.BlurMaskFilter(
                                6.dp.toPx(),
                                android.graphics.BlurMaskFilter.Blur.NORMAL
                            )
                        }
                        onDrawBehind {
                            avatarHalo.color = Color(0xFF9333EA)
                                .copy(alpha = 0.36f * cardGlowAlphaState.value.coerceIn(0f, 1f))
                                .toArgb()
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, r, r, avatarHalo)
                            }
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(BotCardAvatarShape)
                        .background(Color(0xFF160A2C))
                        .border(BorderStroke(1.25.dp, BotAvatarBorderBrush), BotCardAvatarShape),
                    contentAlignment = Alignment.Center
                ) {
                    val builtInAvatarRes = remember(bot.id, bot.aiName, bot.universeName, bot.scenario) {
                        resolveBuiltInBotAvatarRes(bot)
                    }
                    when {
                        bot.avatarUrl.isNotBlank() -> {
                            BotAvatarImage(
                                avatarUrl = bot.avatarUrl,
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(BotCardAvatarShape),
                                fallback = { OrbView(hue = hue, size = 84.dp) }
                            )
                        }
                        builtInAvatarRes != null -> {
                            val builtInRequest = remember(context, builtInAvatarRes) {
                                ImageRequest.Builder(context)
                                    .data(builtInAvatarRes)
                                    .allowRgb565(true)
                                    .crossfade(false)
                                    .build()
                            }
                            AsyncImage(
                                model = builtInRequest,
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(BotCardAvatarShape)
                            )
                        }
                        else -> {
                            OrbView(hue = hue, size = 84.dp)
                        }
                    }
                }

                // Bottom-Right Active Emerald Indicator (only shown for recently active/proactive bots;
                // omitted on fresh/inactive bots instead of a misleading crescent moon)
                if (isOnlineActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD130728))
                            .border(BorderStroke(1.dp, BotStatusBadgeBorderBrush), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.5.dp)
                                .clip(CircleShape)
                                .background(BotOnlineDotBrush)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(13.dp))

            // Right Content Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Row 1: 2-Line Character/Universe Title + Interactive Star + More Orb
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = displayName,
                            fontFamily = LiterarySerifFontFamily,
                            color = Color(0xFFFBF8FF),
                            fontSize = 16.5.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = BotTitleShadowStyle,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable(
                                    enabled = onToggleFavorite != null,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    if (!isFavorite) favoriteBurstTrigger++
                                    onToggleFavorite?.invoke()
                                }
                                .testTag("favorite_bot_${bot.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            CosmicSparkleBurst(
                                triggerKey = favoriteBurstTrigger,
                                modifier = Modifier.size(34.dp),
                                accentColor = Color(0xFFF3E8FF),
                                secondaryColor = Color(0xFFC084FC),
                                particleCount = 10
                            )
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                                tint = if (isFavorite) Color(0xFFD8B4FE) else Color(0xFFBBA4E6).copy(alpha = 0.82f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer {
                                        val s = starScaleState.value
                                        scaleX = s
                                        scaleY = s
                                    }
                            )
                        }
                    }

                    // Top-Right Compact Frosted Glass More Button (•••)
                    Box {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isMenuExpanded) BotMoreExpandedBgBrush else BotMoreCollapsedBgBrush)
                                .border(
                                    BorderStroke(
                                        width = if (isMenuExpanded) 1.2.dp else 0.95.dp,
                                        brush = if (isMenuExpanded) MorCamTokens.RimGradient else BotMoreCollapsedBorderBrush
                                    ),
                                    CircleShape
                                )
                                .clickable { isMenuExpanded = true }
                                .testTag("bot_more_menu_${bot.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Daha Fazla",
                                tint = if (isMenuExpanded) Color.White else Color(0xFFF3E8FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        MorCamPopupMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            headerLabel = "BOT İŞLEMLERİ",
                            minWidth = 224.dp
                        ) {
                            if (onTogglePrivacy != null && !isTemplate) {
                                MorCamPopupMenuItem(
                                    title = if (bot.isPublic) "Özel Yap" else "Herkese Açık Yap",
                                    subtitle = if (bot.isPublic) "Yalnızca senin listende görünür" else "Keşfet vitrininde paylaş",
                                    icon = if (bot.isPublic) Icons.Default.Lock else Icons.Default.Public,
                                    onClick = {
                                        isMenuExpanded = false
                                        onTogglePrivacy.invoke(bot.copy(isPublic = !bot.isPublic))
                                    },
                                    testTag = "bot_menu_privacy_${bot.id}"
                                )
                            }
                            if (onCloneBot != null) {
                                MorCamPopupMenuItem(
                                    title = "Botu Kopyala",
                                    subtitle = "Aynı ayarlarla yeni kopya oluştur",
                                    icon = Icons.Default.ContentCopy,
                                    onClick = {
                                        isMenuExpanded = false
                                        onCloneBot.invoke(bot.id)
                                    },
                                    testTag = "bot_menu_clone_${bot.id}"
                                )
                            }
                            if (onDeleteBot != null) {
                                if ((onTogglePrivacy != null && !isTemplate) || onCloneBot != null) {
                                    MorCamDivider(modifier = Modifier.padding(vertical = 2.dp))
                                }
                                MorCamPopupMenuItem(
                                    title = "Botu Sil",
                                    subtitle = "Kalıcı olarak kaldır",
                                    icon = Icons.Default.DeleteOutline,
                                    destructive = true,
                                    onClick = {
                                        isMenuExpanded = false
                                        showDeleteConfirmDialog = true
                                    },
                                    testTag = "bot_menu_delete_${bot.id}"
                                )
                            }
                        }
                    }
                }

                if (relativeTimeLabel.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = relativeTimeLabel,
                        fontFamily = LiterarySerifFontFamily,
                        color = Color(0xFFD8C8F2).copy(alpha = 0.68f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Row 2: Last Message Preview or Description Snippet (2 Lines, Editorial Serif)
                Text(
                    text = cardDescription,
                    fontFamily = LiterarySerifFontFamily,
                    color = Color(0xFFEAE1FA).copy(alpha = 0.90f),
                    fontSize = 12.6.sp,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(9.dp))

                // Row 3: Two Compact Glass Tag Pills (Left) + Subtle Matte Chevron Indicator (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BotCardGlassTagPill(
                            icon = primaryTag.first,
                            label = primaryTag.second
                        )
                        val hasDraft = remember(bot.id) {
                            com.example.util.ChatDraftStore.hasDraft(context, bot.id)
                        }
                        if (hasDraft) {
                            BotCardGlassTagPill(
                                icon = Icons.Default.Edit,
                                label = "Taslak"
                            )
                        } else {
                            BotCardGlassTagPill(
                                icon = secondaryTag.first,
                                label = secondaryTag.second
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    val playInteraction = remember { MutableInteractionSource() }
                    val playPressed by playInteraction.collectIsPressedAsState()
                    val playScaleState = animateFloatAsState(
                        targetValue = if (playPressed && motionConfig.enableMicroSprings) 0.90f else 1f,
                        animationSpec = CosmicMotionSpecs.snappyOrb(),
                        label = "playOrbScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .graphicsLayer {
                                val s = playScaleState.value
                                scaleX = s
                                scaleY = s
                            }
                            .clip(CircleShape)
                            .background(Color(0x4221103D))
                            .border(BorderStroke(0.8.dp, Color(0x70D8B4FE)), CircleShape)
                            .clickable(
                                interactionSource = playInteraction,
                                indication = null
                            ) { onOpenBot(bot.id) }
                            .testTag("play_bot_${bot.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Sohbete Başla",
                            tint = Color(0xFFE9D5FF).copy(alpha = 0.88f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        MorCamDialogShell(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = "Sohbeti Sil",
            subtitle = "Kalıcı silme onayı",
            icon = Icons.Default.DeleteOutline
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Bu karakter/evren kaydını silmek istediğinizden emin misiniz? Tüm konuşma geçmişiniz silinecektir.",
                    fontFamily = LiterarySerifFontFamily,
                    color = MorCamTokens.LavenderLight,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MorCamCapsuleButton(
                        text = "Vazgeç",
                        onClick = { showDeleteConfirmDialog = false },
                        variant = MorCamButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MorCamCapsuleButton(
                        text = "Sil",
                        onClick = {
                            showDeleteConfirmDialog = false
                            onDeleteBot?.invoke(bot.id)
                        },
                        variant = MorCamButtonVariant.Destructive,
                        icon = Icons.Default.DeleteOutline,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BotCardGlassTagPill(
    icon: ImageVector,
    label: String
) {
    Row(
        modifier = Modifier
            .clip(BotCardPillShape)
            .background(BotTagPillBgBrush)
            .border(BorderStroke(0.9.dp, BotTagPillBorderBrush), BotCardPillShape)
            .padding(horizontal = 9.dp, vertical = 4.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFD8B4FE),
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            fontFamily = LiterarySerifFontFamily,
            color = Color(0xFFF3E8FF),
            fontSize = 11.2.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}
