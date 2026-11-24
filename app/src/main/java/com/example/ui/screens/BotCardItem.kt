package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.example.ui.components.MorCamButtonVariant
import com.example.ui.components.MorCamCapsuleButton
import com.example.ui.components.MorCamDialogShell
import com.example.ui.components.MorCamDivider
import com.example.ui.components.MorCamPopupMenu
import com.example.ui.components.MorCamPopupMenuItem
import com.example.ui.components.MorCamTokens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.BotEntity
import com.example.ui.components.BotAvatarImage
import com.example.ui.components.MoodColors
import com.example.ui.components.OrbView
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.CosmicSparkleBurst
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.cosmicStaggeredEnter
import com.example.ui.theme.EmochiError
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily

@Composable
fun BotCardItem(
    bot: BotEntity,
    confirmDeleteId: String?,
    onConfirmDeleteChange: (String?) -> Unit,
    onOpenBot: (String) -> Unit,
    onDeleteBot: ((String) -> Unit)? = null,
    onTogglePrivacy: ((BotEntity) -> Unit)? = null,
    onCloneBot: ((String) -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    frostedBackdrop: ImageBitmap? = null,
    scrollSync: Int = 0,
    staggerIndex: Int = 0
) {
    val motionConfig = LocalCosmicMotionConfig.current
    val isUniverse = bot.mode == "universe"
    val displayName = when {
        bot.id == "starter_aetheria" || bot.universeName.startsWith("Aetheria: Neon", ignoreCase = true) -> "Aetheria"
        isUniverse -> bot.universeName.ifBlank { bot.aiName.ifBlank { "Evren" } }
        else -> bot.aiName.ifBlank { "Karakter" }
    }
    val hue = MoodColors.getMoodHue(if (isUniverse) "curious" else "calm")
    val isTemplate = !com.example.data.repository.EmochiRepository.isBotPublishable(bot)

    var isMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var favoriteBurstTrigger by remember { mutableStateOf(0) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed && motionConfig.enableMicroSprings) 0.972f else 1f,
        animationSpec = CosmicMotionSpecs.snappyOrb(),
        label = "cardPressScale"
    )

    val starScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1f,
        animationSpec = CosmicMotionSpecs.playfulSpark(),
        label = "favoriteStarScale"
    )

    val animOffset = remember(bot.id) { (bot.id.hashCode().and(0xFF)) / 255f }
    val cardGlowAlpha = if (motionConfig.enableAmbientLoops) {
        val infiniteTransition = rememberInfiniteTransition(label = "cardAnim_${bot.id}")
        val animatedAlpha by infiniteTransition.animateFloat(
            initialValue = 0.72f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2600 + (animOffset * 600).toInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cardGlowAlpha"
        )
        animatedAlpha
    } else {
        0.88f
    }

    val context = LocalContext.current
    val resolvedBackdrop = frostedBackdrop ?: remember(context) {
        createFrostedGothicManorBitmap(context, R.drawable.img_botlist_gothic_wisteria_bg)
    }
    val cardShape = RoundedCornerShape(24.dp)

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

    // Active emerald dot vs quiet crescent moon badge
    val isOnlineActive = !isUniverse && (
        bot.proactiveEnabled ||
        bot.id == "starter_ayla" ||
        bot.lastMessageTimestamp > 0L
    )

    val cardDescription = when {
        bot.id == "starter_ayla" -> "Lise/üniversiteden beri en yakın dostun. Akşam saatlerinde hep yanında."
        bot.id == "starter_aetheria" -> "Neon ışıklarıyla aydınlatılmış Aetheria şehrinin yüksek kulelerinde geçen macera."
        else -> bot.scenario.ifBlank { bot.openingMessage }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .cosmicStaggeredEnter(index = staggerIndex, key = bot.id)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .drawBehind {
                drawIntoCanvas { canvas ->
                    // Soft royal-violet Mor Cam outer rim halo
                    val baseGlow = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 1.6.dp.toPx()
                        color = Color(0xFF9333EA).copy(alpha = 0.24f * cardGlowAlpha).toArgb()
                        maskFilter = android.graphics.BlurMaskFilter(
                            7.dp.toPx(),
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                    }
                    val cr = 24.dp.toPx()
                    canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, cr, cr, baseGlow)

                    // Subtle top-left & bottom-right lavender specular rim glints
                    val glintPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 1.15.dp.toPx()
                        color = Color(0xFFE9D5FF).copy(alpha = 0.28f * cardGlowAlpha).toArgb()
                        maskFilter = android.graphics.BlurMaskFilter(
                            4.dp.toPx(),
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                    }
                    canvas.nativeCanvas.drawLine(24.dp.toPx(), 0f, size.width * 0.36f, 0f, glintPaint)
                    canvas.nativeCanvas.drawLine(size.width * 0.62f, size.height, size.width - 24.dp.toPx(), size.height, glintPaint)
                }
            }
            .clip(cardShape)
            .templatesBackdropBlur(
                blurredBitmap = resolvedBackdrop,
                alpha = 0.78f,
                scrollSync = scrollSync
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x5C220F40),
                        Color(0x4C130827),
                        Color(0x561E0D3A)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 450f)
                )
            )
            .drawBehind {
                val cr = 24.dp.toPx()
                // Top and bottom inner glass refraction shelves
                drawRect(
                    brush = Brush.verticalGradient(
                        0.00f to Color(0x28E9D5FF),
                        0.16f to Color.Transparent,
                        0.84f to Color.Transparent,
                        1.00f to Color(0x207C3AED)
                    )
                )
                // Gentle inner frosted perimeter bevel
                drawIntoCanvas { canvas ->
                    val edgeFrost = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 4.5.dp.toPx()
                        color = android.graphics.Color.argb(34, 232, 214, 255)
                        maskFilter = android.graphics.BlurMaskFilter(4.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, cr, cr, edgeFrost)
                }
                // Delicate left-edge vertical specular glass highlight bar (signature reference detail)
                val barW = 2.6.dp.toPx()
                val barH = (size.height * 0.36f).coerceIn(30.dp.toPx(), 52.dp.toPx())
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xCCE9D5FF),
                            Color(0xFFE9D5FF),
                            Color(0xCCE9D5FF),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(1.dp.toPx(), (size.height - barH) * 0.5f),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(barW, barW)
                )
                // Ambient violet light pool near bottom-right play orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x229333EA),
                            Color(0x0C6D28D9),
                            Color.Transparent
                        ),
                        center = Offset(size.width - 36.dp.toPx(), size.height - 28.dp.toPx()),
                        radius = 78.dp.toPx()
                    ),
                    center = Offset(size.width - 36.dp.toPx(), size.height - 28.dp.toPx()),
                    radius = 78.dp.toPx()
                )
            }
            .border(
                BorderStroke(
                    width = 1.15.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xDCE9D5FF),
                            Color(0x889F7AEA),
                            Color(0x665B21B6),
                            Color(0xCCD8B4FE)
                        )
                    )
                ),
                cardShape
            )
            .clickable(interactionSource = interactionSource, indication = null) { onOpenBot(bot.id) }
            .testTag("bot_card_${bot.id}")
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Left: 1:1 Rounded-Square Portrait Thumbnail with Status Orb ──
            val avatarShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .size(106.dp)
                    .aspectRatio(1f)
                    .drawBehind {
                        drawIntoCanvas { canvas ->
                            val avatarHalo = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                style = android.graphics.Paint.Style.STROKE
                                strokeWidth = 1.8.dp.toPx()
                                color = Color(0xFF9333EA).copy(alpha = 0.36f * cardGlowAlpha).toArgb()
                                maskFilter = android.graphics.BlurMaskFilter(
                                    6.dp.toPx(),
                                    android.graphics.BlurMaskFilter.Blur.NORMAL
                                )
                            }
                            val r = 18.dp.toPx()
                            canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, r, r, avatarHalo)
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(avatarShape)
                        .background(Color(0xFF160A2C))
                        .border(
                            BorderStroke(
                                1.25.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xEAF3E8FF),
                                        Color(0xA8C084FC),
                                        Color(0x887C3AED),
                                        Color(0xDCE9D5FF)
                                    )
                                )
                            ),
                            avatarShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        bot.avatarUrl.isNotBlank() -> {
                            BotAvatarImage(
                                avatarUrl = bot.avatarUrl,
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(avatarShape),
                                fallback = { OrbView(hue = hue, size = 84.dp) }
                            )
                        }
                        bot.id == "starter_ayla" || bot.aiName.equals("Ayla", ignoreCase = true) -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_ayla_avatar),
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(avatarShape)
                            )
                        }
                        bot.id == "starter_aetheria" || bot.universeName.contains("Aetheria", ignoreCase = true) -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_aetheria_universe),
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(avatarShape)
                            )
                        }
                        bot.id == "preset_aiden_mcu_cosmic" ||
                            bot.aiName.contains("Kozmik Sürükleniş", ignoreCase = true) ||
                            bot.aiName.contains("Cosmic Drift", ignoreCase = true) -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_aiden_blackwood_avatar),
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(avatarShape)
                            )
                        }
                        bot.id == "preset_aria" || bot.aiName.startsWith("Aria", ignoreCase = true) -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_template_aria_left),
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(avatarShape)
                            )
                        }
                        bot.id == "preset_eldoria" || bot.universeName.contains("Eldoria", ignoreCase = true) -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_template_eldoria_left),
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(avatarShape)
                            )
                        }
                        else -> {
                            OrbView(hue = hue, size = 84.dp)
                        }
                    }
                }

                // Bottom-Right Status Badge (Online Emerald Dot or Soft Crescent Moon)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(23.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD130728))
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(Color(0xCCE9D5FF), Color(0x887C3AED))
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isOnlineActive) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0xFFA7F3D0),
                                            Color(0xFF34D399),
                                            Color(0xFF10B981)
                                        )
                                    )
                                )
                        )
                    } else {
                        // Delicate vector crescent moon in soft luminous lavender
                        Canvas(modifier = Modifier.size(12.dp)) {
                            val cx = size.width * 0.52f
                            val cy = size.height * 0.50f
                            val r = size.minDimension * 0.42f
                            val moonPath = Path().apply {
                                moveTo(cx + r * 0.15f, cy - r * 0.92f)
                                cubicTo(
                                    cx - r * 0.95f, cy - r * 0.65f,
                                    cx - r * 0.95f, cy + r * 0.72f,
                                    cx + r * 0.25f, cy + r * 0.92f
                                )
                                cubicTo(
                                    cx + r * 0.75f, cy + r * 0.85f,
                                    cx + r * 0.95f, cy + r * 0.42f,
                                    cx + r * 0.82f, cy + r * 0.22f
                                )
                                cubicTo(
                                    cx + r * 0.18f, cy + r * 0.45f,
                                    cx - r * 0.35f, cy + r * 0.02f,
                                    cx - r * 0.12f, cy - r * 0.45f
                                )
                                cubicTo(
                                    cx + r * 0.02f, cy - r * 0.70f,
                                    cx + r * 0.25f, cy - r * 0.82f,
                                    cx + r * 0.15f, cy - r * 0.92f
                                )
                                close()
                            }
                            drawPath(
                                path = moonPath,
                                color = Color(0xFFE9D5FF)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(13.dp))

            // ── Right Content Column ──
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Row 1: Character Name + Interactive Star + More Orb
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayName,
                            fontFamily = LiterarySerifFontFamily,
                            color = Color(0xFFFBF8FF),
                            fontSize = 20.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0xAA120524),
                                    offset = Offset(0f, 1.5f),
                                    blurRadius = 6f
                                )
                            ),
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Interactive Favorite Star (Harmonized Luminous Amethyst-Lavender + Sparkle Burst)
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
                                        scaleX = starScale
                                        scaleY = starScale
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
                                .background(
                                    if (isMenuExpanded) {
                                        Brush.radialGradient(
                                            listOf(Color(0xA67C3AED), Color(0x883B1470))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(Color(0x5231175A), Color(0x46190B33))
                                        )
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        width = if (isMenuExpanded) 1.2.dp else 0.95.dp,
                                        brush = if (isMenuExpanded) {
                                            MorCamTokens.RimGradient
                                        } else {
                                            Brush.linearGradient(
                                                listOf(Color(0xCCE9D5FF), Color(0x668B5CF6))
                                            )
                                        }
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

                Spacer(modifier = Modifier.height(5.dp))

                // Row 2: Description Snippet (2-3 Lines, Editorial Serif)
                Text(
                    text = cardDescription,
                    fontFamily = LiterarySerifFontFamily,
                    color = Color(0xFFEAE1FA).copy(alpha = 0.90f),
                    fontSize = 12.8.sp,
                    lineHeight = 17.5.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3: Two Compact Glass Tag Pills (Left) + Circular Crystal Play Button (Right)
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

                    // Circular Crystal Amethyst Play Orb (Matches Reference + Harmonized App Palette)
                    val playInteraction = remember { MutableInteractionSource() }
                    val playPressed by playInteraction.collectIsPressedAsState()
                    val playScale by animateFloatAsState(
                        targetValue = if (playPressed && motionConfig.enableMicroSprings) 0.88f else 1f,
                        animationSpec = CosmicMotionSpecs.snappyOrb(),
                        label = "playOrbScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = playScale
                                scaleY = playScale
                            }
                            .drawBehind {
                                drawIntoCanvas { canvas ->
                                    val orbGlow = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                        style = android.graphics.Paint.Style.STROKE
                                        strokeWidth = 2.dp.toPx()
                                        color = Color(0xFFA855F7).copy(alpha = 0.50f * cardGlowAlpha).toArgb()
                                        maskFilter = android.graphics.BlurMaskFilter(
                                            7.dp.toPx(),
                                            android.graphics.BlurMaskFilter.Blur.NORMAL
                                        )
                                    }
                                    canvas.nativeCanvas.drawCircle(
                                        size.width * 0.5f,
                                        size.height * 0.5f,
                                        size.minDimension * 0.5f,
                                        orbGlow
                                    )
                                }
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x9C9F55F7),
                                        Color(0x887C3AED),
                                        Color(0x7C4C1D95),
                                        Color(0x882E1065)
                                    ),
                                    center = Offset(38f, 28f),
                                    radius = 74f
                                )
                            )
                            .drawBehind {
                                // Top specular crystal sheen
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.34f),
                                            Color(0xFFE9D5FF).copy(alpha = 0.12f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width * 0.38f, size.height * 0.14f),
                                        radius = size.width * 0.42f
                                    ),
                                    center = Offset(size.width * 0.38f, size.height * 0.14f),
                                    radius = size.width * 0.42f
                                )
                            }
                            .border(
                                BorderStroke(
                                    1.25.dp,
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFF5F0FF),
                                            Color(0xFFD8B4FE),
                                            Color(0xFF9333EA),
                                            Color(0xFFE9D5FF)
                                        )
                                    )
                                ),
                                CircleShape
                            )
                            .clickable(
                                interactionSource = playInteraction,
                                indication = null
                            ) { onOpenBot(bot.id) }
                            .testTag("play_bot_${bot.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Sohbete Başla",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    // Harmonized Mor Cam Delete Confirmation Dialog
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
    val pillShape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(pillShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0x5C2C1554), Color(0x4C16092E))
                )
            )
            .border(
                BorderStroke(
                    0.9.dp,
                    Brush.linearGradient(
                        listOf(Color(0xBBE9D5FF), Color(0x668B5CF6))
                    )
                ),
                pillShape
            )
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
