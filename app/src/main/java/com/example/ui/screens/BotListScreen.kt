package com.example.ui.screens

import com.example.ui.theme.AppColors
import com.example.ui.theme.EmochiGradients

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import com.example.ui.components.drawMorCamFourPointStar
import com.example.ui.theme.CalligraphicScriptFontFamily
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Tune
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import com.example.ui.components.MorCamChipPill
import com.example.ui.components.MorCamPopupMenu
import com.example.ui.components.MorCamPopupMenuItem
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.BotEntity
import com.example.data.local.UserSettingsEntity
import com.example.ui.components.AppBackground
import com.example.ui.components.GlobalSettingsModal
import com.example.ui.components.MoodColors
import com.example.ui.components.OrbView
import com.example.ui.components.customTextFieldColors
import com.example.ui.theme.EmochiBackground
import com.example.ui.theme.EmochiBorder
import com.example.ui.theme.EmochiCard
import com.example.ui.theme.EmochiError
import com.example.ui.theme.EmochiPrimary
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.cosmicFloatingLevitation
import com.example.ui.animation.cosmicGlassSheen
import com.example.ui.animation.cosmicTapSpring
import com.example.ui.theme.EmochiSurface
import com.example.ui.theme.EmochiTextMuted
import com.example.ui.theme.EmochiTextPrimary
import com.example.ui.theme.EmochiTextSecondary

@Composable
fun BotListScreen(
    botList: List<BotEntity>,
    userSettings: UserSettingsEntity?,
    onOpenBot: (String) -> Unit,
    onNewBot: () -> Unit,
    onDeleteBot: (String) -> Unit,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    onExportData: suspend (Boolean) -> String,
    onImportData: suspend (String, Boolean) -> com.example.data.repository.BackupImportSummary,
    onImportPresetBot: ((BotEntity) -> Unit)? = null,
    onTogglePrivacy: ((BotEntity) -> Unit)? = null,
    onCloneBot: ((String) -> Unit)? = null
) {
    var showGlobalSettings by remember { mutableStateOf(false) }
    var showAidenStoriesMenu by remember { mutableStateOf(false) }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "personal", "universe"
    var searchQuery by remember { mutableStateOf("") }
    var activeTab by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("chats") } // "chats", "discover", "templates", "books"

    val chatsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val templatesListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val discoverListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val booksListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // System Back button closes Aiden Stories menu or returns to "chats" tab first before exiting
    BackHandler(enabled = showAidenStoriesMenu || activeTab != "chats") {
        if (showAidenStoriesMenu) {
            showAidenStoriesMenu = false
        } else {
            activeTab = "chats"
            coroutineScope.launch { chatsListState.scrollToItem(0) }
        }
    }

    var bottomBarTopInRootPx by remember { mutableStateOf(0f) }
    var contentTopInRootPx by remember { mutableStateOf(0f) }
    val context = LocalContext.current
    val frostedManorState = com.example.util.rememberFrostedBackdrop(
        key = "gothic_manor_${R.drawable.img_templates_bg_gothic_manor}",
        drawableResId = R.drawable.img_templates_bg_gothic_manor,
        targetWidth = 360,
        maxHeight = 860,
        blurRadius = 4,
        blurPasses = 2,
        violetShiftWarmPixels = true,
        outputRgb565 = true
    )
    val frostedCosmicState = com.example.util.rememberFrostedBackdrop(
        key = "gothic_manor_${R.drawable.img_botlist_gothic_wisteria_bg}",
        drawableResId = R.drawable.img_botlist_gothic_wisteria_bg,
        targetWidth = 360,
        maxHeight = 860,
        blurRadius = 4,
        blurPasses = 2,
        violetShiftWarmPixels = true,
        outputRgb565 = true
    )
    val frostedManorBitmap = frostedManorState.value ?: com.example.util.FrostedBackdropCache.placeholderImageBitmap
    val frostedCosmicBitmap = frostedCosmicState.value ?: com.example.util.FrostedBackdropCache.placeholderImageBitmap
    val bgSeamlessEasing = remember { androidx.compose.animation.core.CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f) }
    val templatesBgProgress by animateFloatAsState(
        targetValue = if (activeTab == "templates") 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = bgSeamlessEasing),
        label = "bottomBarTemplatesBgProgress"
    )
    val activeScrollSync = when (activeTab) {
        "templates" -> templatesListState.firstVisibleItemIndex * 100000 + templatesListState.firstVisibleItemScrollOffset
        "discover"  -> discoverListState.firstVisibleItemIndex * 100000 + discoverListState.firstVisibleItemScrollOffset
        "books"     -> booksListState.firstVisibleItemIndex * 100000 + booksListState.firstVisibleItemScrollOffset
        else        -> chatsListState.firstVisibleItemIndex * 100000 + chatsListState.firstVisibleItemScrollOffset
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppBackground(activeTab = activeTab)
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            val motionConfig = LocalCosmicMotionConfig.current
            // Infinite transitions for live neon glow, center orb pulse, and specular shimmer (gated by MotionIntensityMode)
            val centerPulseScale: Float
            val centerPulseAlpha: Float
            val orbRingRot: Float
            if (motionConfig.enableAmbientLoops) {
                val navInfinite = rememberInfiniteTransition(label = "navInfinite")
                centerPulseScale = navInfinite.animateFloat(
                    initialValue = 0.96f, targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "centerPulse"
                ).value
                centerPulseAlpha = navInfinite.animateFloat(
                    initialValue = 0.55f, targetValue = 0.95f,
                    animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "centerPulseAlpha"
                ).value
                orbRingRot = navInfinite.animateFloat(
                    initialValue = 0f, targetValue = 360f,
                    animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
                    label = "orbRingRot"
                ).value
            } else {
                centerPulseScale = 1f
                centerPulseAlpha = 0.80f
                orbRingRot = 0f
            }

            data class NavTab(val key: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)
            val tabs = listOf(
                NavTab("discover",  Icons.Default.Public,   "Keşfet"),
                NavTab("chats",     Icons.Default.Forum,    "Sohbet"),
                NavTab("templates", Icons.Default.Star,     "Şablon"),
                NavTab("books",     Icons.Default.MenuBook, "Kitap")
            )
            val listStates = mapOf(
                "discover"  to discoverListState,
                "chats"     to chatsListState,
                "templates" to templatesListState,
                "books"     to booksListState
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                // Reference-Matched Sleek Frosted Mor Cam Bottom Navigation Dock
                val dockShape = RoundedCornerShape(36.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .align(Alignment.Center)
                        .then(
                            Modifier.let {
                                androidx.compose.ui.Modifier.onGloballyPositioned { coords ->
                                    bottomBarTopInRootPx = coords.positionInRoot().y
                                }
                            }
                        )
                        .drawBehind {
                            drawIntoCanvas { canvas ->
                                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeWidth = 1.8.dp.toPx()
                                    color = Color(0x489333EA).toArgb()
                                    maskFilter = android.graphics.BlurMaskFilter(
                                        7.dp.toPx(),
                                        android.graphics.BlurMaskFilter.Blur.NORMAL
                                    )
                                }
                                canvas.nativeCanvas.drawRoundRect(
                                    0f,
                                    0f,
                                    size.width,
                                    size.height,
                                    36.dp.toPx(),
                                    36.dp.toPx(),
                                    paint
                                )
                            }
                        }
                        .clip(dockShape)
                        .templatesBackdropBlur(
                            blurredBitmap = frostedCosmicBitmap,
                            secondaryBitmap = frostedManorBitmap,
                            secondaryProgress = templatesBgProgress,
                            alpha = 0.78f,
                            scrollSync = activeScrollSync
                        )
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x52261148),
                                    Color(0x40140829),
                                    Color(0x4E220F40)
                                )
                            )
                        )
                        .drawBehind {
                            val cr = 36.dp.toPx()
                            // 1. Subtle top & bottom inner frosted glass shelf refraction
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0.00f to Color(0x2AE9D5FF),
                                    0.22f to Color.Transparent,
                                    0.80f to Color.Transparent,
                                    1.00f to Color(0x247C3AED)
                                )
                            )
                            // 2. Calm inner frosted perimeter bevel
                            drawIntoCanvas { canvas ->
                                val edgeFrost = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeWidth = 4.5.dp.toPx()
                                    color = android.graphics.Color.argb(34, 232, 214, 255)
                                    maskFilter = android.graphics.BlurMaskFilter(4.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                                }
                                canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, cr, cr, edgeFrost)
                            }
                            // 3. Center ambient amethyst glass refraction behind + Oluştur
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x369333EA),
                                        Color(0x166D28D9),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.50f, size.height * 0.50f),
                                    radius = size.width * 0.28f
                                ),
                                center = Offset(size.width * 0.50f, size.height * 0.50f),
                                radius = size.width * 0.28f
                            )
                        }
                        .border(
                            BorderStroke(
                                1.15.dp,
                                Brush.horizontalGradient(
                                    0.00f to Color(0x98A78BFA),
                                    0.24f to Color(0xCCD8B4FE),
                                    0.50f to Color(0xEAF3E8FF),
                                    0.76f to Color(0xCCD8B4FE),
                                    1.00f to Color(0x98A78BFA)
                                )
                            ),
                            dockShape
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clean, box-free reference tab renderer with subtle luminous amethyst backlit aura when selected
                        @Composable
                        fun RenderNavTabItem(
                            tab: NavTab,
                            isSelected: Boolean,
                            modifier: Modifier = Modifier
                        ) {
                            val tabInteraction = remember { MutableInteractionSource() }
                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.08f else 1.0f,
                                animationSpec = CosmicMotionSpecs.snappyOrb(),
                                label = "navTabScale_${tab.key}"
                            )
                            val activeGlowAlpha by animateFloatAsState(
                                targetValue = if (isSelected) 1f else 0f,
                                animationSpec = tween(220),
                                label = "navTabGlow_${tab.key}"
                            )

                            Box(
                                modifier = modifier
                                    .fillMaxHeight()
                                    .cosmicTapSpring(
                                        interactionSource = tabInteraction,
                                        pressedScale = 0.92f,
                                        cornerRadius = 24.dp
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .drawBehind {
                                        if (activeGlowAlpha > 0.01f) {
                                            // Soft radial amethyst-lavender halo behind the active tab (no bulky box!)
                                            drawCircle(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        Color(0xFF9333EA).copy(alpha = 0.34f * activeGlowAlpha),
                                                        Color(0xFF6D28D9).copy(alpha = 0.14f * activeGlowAlpha),
                                                        Color.Transparent
                                                    ),
                                                    center = Offset(size.width * 0.5f, size.height * 0.48f),
                                                    radius = size.minDimension * 0.62f
                                                ),
                                                center = Offset(size.width * 0.5f, size.height * 0.48f),
                                                radius = size.minDimension * 0.62f
                                            )
                                            // Delicate luminous lavender glint line along bottom glass edge
                                            val barW = size.width * 0.46f
                                            val barH = 2.dp.toPx()
                                            drawRoundRect(
                                                brush = Brush.horizontalGradient(
                                                    listOf(
                                                        Color.Transparent,
                                                        Color(0xFFE9D5FF).copy(alpha = 0.90f * activeGlowAlpha),
                                                        Color.Transparent
                                                    )
                                                ),
                                                topLeft = Offset((size.width - barW) * 0.5f, size.height - barH - 2.dp.toPx()),
                                                size = Size(barW, barH),
                                                cornerRadius = CornerRadius(barH, barH)
                                            )
                                        }
                                    }
                                    .clickable(
                                        indication = null,
                                        interactionSource = tabInteraction
                                    ) {
                                        if (activeTab == tab.key) {
                                            coroutineScope.launch { listStates[tab.key]?.animateScrollToItem(0) }
                                        } else {
                                            activeTab = tab.key
                                            coroutineScope.launch { listStates[tab.key]?.scrollToItem(0) }
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                                ) {
                                    when (tab.key) {
                                        "discover" -> {
                                            // Reference Wireframe Meridian/Latitude Globe Icon
                                            Canvas(modifier = Modifier.size(24.dp)) {
                                                val r = size.minDimension * 0.44f
                                                val c = Offset(size.width * 0.5f, size.height * 0.5f)
                                                val strokeColor = if (isSelected) Color.White else Color(0xFFE2D0FA).copy(alpha = 0.88f)
                                                val sw = 1.55.dp.toPx()
                                                drawCircle(
                                                    color = strokeColor,
                                                    radius = r,
                                                    center = c,
                                                    style = Stroke(width = sw)
                                                )
                                                drawOval(
                                                    color = strokeColor,
                                                    topLeft = Offset(c.x - r * 0.48f, c.y - r),
                                                    size = Size(r * 0.96f, r * 2f),
                                                    style = Stroke(width = sw * 0.92f)
                                                )
                                                drawLine(
                                                    color = strokeColor,
                                                    start = Offset(c.x, c.y - r),
                                                    end = Offset(c.x, c.y + r),
                                                    strokeWidth = sw * 0.85f
                                                )
                                                drawLine(
                                                    color = strokeColor,
                                                    start = Offset(c.x - r, c.y),
                                                    end = Offset(c.x + r, c.y),
                                                    strokeWidth = sw * 0.9f
                                                )
                                                drawLine(
                                                    color = strokeColor.copy(alpha = 0.85f),
                                                    start = Offset(c.x - r * 0.84f, c.y - r * 0.48f),
                                                    end = Offset(c.x + r * 0.84f, c.y - r * 0.48f),
                                                    strokeWidth = sw * 0.8f
                                                )
                                                drawLine(
                                                    color = strokeColor.copy(alpha = 0.85f),
                                                    start = Offset(c.x - r * 0.84f, c.y + r * 0.48f),
                                                    end = Offset(c.x + r * 0.84f, c.y + r * 0.48f),
                                                    strokeWidth = sw * 0.8f
                                                )
                                            }
                                        }
                                        "chats" -> {
                                            // Reference Double Speech Bubble with '...'
                                            Canvas(modifier = Modifier.size(24.dp)) {
                                                val bw = size.width
                                                val bh = size.height
                                                val mainBrush = Brush.verticalGradient(
                                                    if (isSelected) listOf(Color.White, Color(0xFFF3E8FF))
                                                    else listOf(Color(0xFFE9D5FF), Color(0xFFC4B5FD))
                                                )
                                                val backColor = if (isSelected) Color(0xFFD8B4FE).copy(alpha = 0.84f)
                                                else Color(0xFFA78BFA).copy(alpha = 0.65f)

                                                val subBubble = Path().apply {
                                                    addRoundRect(
                                                        androidx.compose.ui.geometry.RoundRect(
                                                            left = bw * 0.36f,
                                                            top = bh * 0.34f,
                                                            right = bw * 0.94f,
                                                            bottom = bh * 0.80f,
                                                            cornerRadius = CornerRadius(bw * 0.14f, bw * 0.14f)
                                                        )
                                                    )
                                                    moveTo(bw * 0.74f, bh * 0.78f)
                                                    lineTo(bw * 0.88f, bh * 0.92f)
                                                    lineTo(bw * 0.62f, bh * 0.78f)
                                                    close()
                                                }
                                                drawPath(path = subBubble, color = backColor)

                                                val mainBubble = Path().apply {
                                                    addRoundRect(
                                                        androidx.compose.ui.geometry.RoundRect(
                                                            left = bw * 0.06f,
                                                            top = bh * 0.12f,
                                                            right = bw * 0.82f,
                                                            bottom = bh * 0.68f,
                                                            cornerRadius = CornerRadius(bw * 0.16f, bw * 0.16f)
                                                        )
                                                    )
                                                    moveTo(bw * 0.24f, bh * 0.66f)
                                                    lineTo(bw * 0.16f, bh * 0.86f)
                                                    lineTo(bw * 0.40f, bh * 0.68f)
                                                    close()
                                                }
                                                drawPath(path = mainBubble, brush = mainBrush)

                                                val dotColor = Color(0xFF22103E)
                                                val dotY = bh * 0.40f
                                                val dotR = bw * 0.052f
                                                drawCircle(color = dotColor, radius = dotR, center = Offset(bw * 0.28f, dotY))
                                                drawCircle(color = dotColor, radius = dotR, center = Offset(bw * 0.44f, dotY))
                                                drawCircle(color = dotColor, radius = dotR, center = Offset(bw * 0.60f, dotY))
                                            }
                                        }
                                        "templates" -> {
                                            // Reference 5-Point Luminous Star
                                            Canvas(modifier = Modifier.size(24.dp)) {
                                                val cx = size.width * 0.5f
                                                val cy = size.height * 0.5f
                                                val outerR = size.minDimension * 0.46f
                                                val innerR = outerR * 0.45f
                                                val starPath = Path().apply {
                                                    for (i in 0 until 10) {
                                                        val angle = Math.toRadians((i * 36 - 90).toDouble())
                                                        val rad = if (i % 2 == 0) outerR else innerR
                                                        val px = cx + (rad * kotlin.math.cos(angle)).toFloat()
                                                        val py = cy + (rad * kotlin.math.sin(angle)).toFloat()
                                                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                                                    }
                                                    close()
                                                }
                                                drawPath(
                                                    path = starPath,
                                                    brush = Brush.verticalGradient(
                                                        if (isSelected) {
                                                            listOf(Color.White, Color(0xFFF3E8FF), Color(0xFFD8B4FE))
                                                        } else {
                                                            listOf(Color(0xFFE9D5FF), Color(0xFFC4B5FD), Color(0xFFA78BFA))
                                                        }
                                                    )
                                                )
                                            }
                                        }
                                        else -> {
                                            // Reference Open Book Icon
                                            Canvas(modifier = Modifier.size(24.dp)) {
                                                val bw = size.width
                                                val bh = size.height
                                                val leftPage = Path().apply {
                                                    moveTo(bw * 0.48f, bh * 0.22f)
                                                    cubicTo(bw * 0.34f, bh * 0.14f, bw * 0.16f, bh * 0.16f, bw * 0.07f, bh * 0.24f)
                                                    lineTo(bw * 0.07f, bh * 0.80f)
                                                    cubicTo(bw * 0.20f, bh * 0.72f, bw * 0.35f, bh * 0.72f, bw * 0.48f, bh * 0.82f)
                                                    close()
                                                }
                                                val rightPage = Path().apply {
                                                    moveTo(bw * 0.52f, bh * 0.22f)
                                                    cubicTo(bw * 0.66f, bh * 0.14f, bw * 0.84f, bh * 0.16f, bw * 0.93f, bh * 0.24f)
                                                    lineTo(bw * 0.93f, bh * 0.80f)
                                                    cubicTo(bw * 0.80f, bh * 0.72f, bw * 0.65f, bh * 0.72f, bw * 0.52f, bh * 0.82f)
                                                    close()
                                                }
                                                drawPath(
                                                    path = leftPage,
                                                    brush = Brush.verticalGradient(
                                                        if (isSelected) listOf(Color.White, Color(0xFFF3E8FF))
                                                        else listOf(Color(0xFFE9D5FF), Color(0xFFB794F4))
                                                    )
                                                )
                                                drawPath(
                                                    path = rightPage,
                                                    brush = Brush.verticalGradient(
                                                        if (isSelected) listOf(Color.White, Color(0xFFF3E8FF), Color(0xFFD8B4FE))
                                                        else listOf(Color(0xFFD8B4FE), Color(0xFFA78BFA))
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = tab.label,
                                        fontFamily = LiterarySerifFontFamily,
                                        color = if (isSelected) Color.White else Color(0xFFE9D5FF).copy(alpha = 0.86f),
                                        fontSize = 11.8.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        style = androidx.compose.ui.text.TextStyle(
                                            shadow = if (isSelected) {
                                                Shadow(color = Color(0xAA9333EA), offset = Offset.Zero, blurRadius = 8f)
                                            } else {
                                                Shadow(color = Color(0x88120524), offset = Offset(0f, 1f), blurRadius = 4f)
                                            }
                                        )
                                    )
                                }
                            }
                        }

                        // Left two tabs ("Keşfet" | "Sohbet") with hairline vertical glass divider
                        tabs.take(2).forEachIndexed { idx, tab ->
                            RenderNavTabItem(
                                tab = tab,
                                isSelected = activeTab == tab.key,
                                modifier = Modifier.weight(1f)
                            )
                            if (idx == 0) {
                                Box(
                                    modifier = Modifier
                                        .width(0.85.dp)
                                        .height(36.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    Color(0x5AE9D5FF),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                        }

                        // Spacer for Prominent Center Celestial Orb (+ Oluştur)
                        Spacer(modifier = Modifier.width(88.dp))

                        // Right two tabs ("Şablon" | "Kitap") with symmetrical hairline vertical glass divider
                        tabs.drop(2).forEachIndexed { idx, tab ->
                            RenderNavTabItem(
                                tab = tab,
                                isSelected = activeTab == tab.key,
                                modifier = Modifier.weight(1f)
                            )
                            if (idx == 0) {
                                Box(
                                    modifier = Modifier
                                        .width(0.85.dp)
                                        .height(36.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    Color(0x5AE9D5FF),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

                // Center Celestial Concentric 3D Orb FAB (+ Oluştur) matching reference in harmonized Mor Cam palette
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-3).dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Swirling gossamer lavender-amethyst orbital filaments wrapping the center orb
                    Canvas(
                        modifier = Modifier
                            .size(106.dp)
                            .graphicsLayer { rotationZ = orbRingRot * 0.42f }
                    ) {
                        val c = Offset(size.width * 0.5f, size.height * 0.5f)
                        val rx = size.width * 0.48f
                        val ry = size.height * 0.37f
                        listOf(-30f, 18f, 68f, 122f).forEachIndexed { idx, deg ->
                            rotate(degrees = deg, pivot = c) {
                                drawOval(
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0x90E9D5FF),
                                            Color(0x4CA855F7),
                                            Color.Transparent,
                                            Color(0x70D8B4FE),
                                            Color.Transparent
                                        )
                                    ),
                                    topLeft = Offset(c.x - rx, c.y - ry),
                                    size = Size(rx * 2f, ry * 2f),
                                    style = Stroke(width = if (idx % 2 == 0) 1.1.dp.toPx() else 0.75.dp.toPx())
                                )
                            }
                        }
                    }

                    // Outer see-through frosted Mor Cam halo ring (#1)
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .drawBehind {
                                drawIntoCanvas { canvas ->
                                    val halo = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                        style = android.graphics.Paint.Style.STROKE
                                        strokeWidth = 2.4.dp.toPx()
                                        color = Color(0xFF9333EA).copy(alpha = 0.45f * centerPulseAlpha).toArgb()
                                        maskFilter = android.graphics.BlurMaskFilter(
                                            9.dp.toPx(),
                                            android.graphics.BlurMaskFilter.Blur.NORMAL
                                        )
                                    }
                                    canvas.nativeCanvas.drawCircle(size.width * 0.5f, size.height * 0.5f, size.minDimension * 0.48f, halo)
                                }
                            }
                            .clip(CircleShape)
                            .templatesBackdropBlur(
                                blurredBitmap = frostedCosmicBitmap,
                                secondaryBitmap = frostedManorBitmap,
                                secondaryProgress = templatesBgProgress,
                                alpha = 0.75f,
                                scrollSync = activeScrollSync
                            )
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0x52461C80),
                                        Color(0x361B0A38)
                                    )
                                )
                            )
                            .border(
                                BorderStroke(
                                    1.2.dp,
                                    Brush.linearGradient(
                                        listOf(Color(0xE8F3E8FF), Color(0x908B5CF6), Color(0xE0E9D5FF))
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner glowing crystal amethyst sphere (#2) with '+' and 'Oluştur'
                        val createOrbInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .cosmicTapSpring(
                                    interactionSource = createOrbInteraction,
                                    pressedScale = 0.90f,
                                    cornerRadius = 34.dp
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xB8B066FF),
                                            Color(0x9C7C3AED),
                                            Color(0x924C1D95),
                                            Color(0xA42E1065)
                                        ),
                                        center = Offset(78f, 58f),
                                        radius = 122f
                                    )
                                )
                                .drawBehind {
                                    // Top pearl specular flare & bottom amethyst reflection
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.34f),
                                                Color(0xFFE9D5FF).copy(alpha = 0.14f),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width * 0.44f, size.height * 0.06f),
                                            radius = size.width * 0.42f
                                        ),
                                        center = Offset(size.width * 0.44f, size.height * 0.06f),
                                        radius = size.width * 0.42f
                                    )
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.36f),
                                                Color(0xFFD8B4FE).copy(alpha = 0.16f),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width * 0.50f, size.height * 0.96f),
                                            radius = size.width * 0.42f
                                        ),
                                        center = Offset(size.width * 0.50f, size.height * 0.96f),
                                        radius = size.width * 0.42f
                                    )
                                }
                                .border(
                                    BorderStroke(
                                        1.45.dp,
                                        Brush.verticalGradient(
                                            listOf(Color.White, Color(0xFFD8B4FE), Color(0xFFA855F7), Color(0xFFF3E8FF))
                                        )
                                    ),
                                    CircleShape
                                )
                                .clickable(
                                    interactionSource = createOrbInteraction,
                                    indication = null
                                ) { onNewBot() }
                                .testTag("create_new_bot_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Oluştur",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(27.dp)
                                        .offset(y = 1.dp)
                                )
                                Text(
                                    text = "Oluştur",
                                    fontFamily = LiterarySerifFontFamily,
                                    color = Color.White,
                                    fontSize = 11.2.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 11.5.sp,
                                    modifier = Modifier.offset(y = (-1).dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            val tabOrder = listOf("discover", "chats", "templates", "books")
            val homeMotionConfig = LocalCosmicMotionConfig.current
            val sharedFavContext = LocalContext.current
            val sharedFavPrefs = remember(sharedFavContext) {
                sharedFavContext.getSharedPreferences("velora_bot_favorites_v1", android.content.Context.MODE_PRIVATE)
            }
            var sharedFavoriteBotIds by remember {
                mutableStateOf(
                    if (sharedFavPrefs.contains("fav_ids")) {
                        sharedFavPrefs.getStringSet("fav_ids", emptySet())?.toSet() ?: emptySet()
                    } else {
                        setOf("starter_ayla")
                    }
                )
            }
            val toggleSharedFavorite: (String) -> Unit = { botId ->
                val adding = !sharedFavoriteBotIds.contains(botId)
                val updated = if (adding) sharedFavoriteBotIds + botId else sharedFavoriteBotIds - botId
                sharedFavoriteBotIds = updated
                sharedFavPrefs.edit().putStringSet("fav_ids", updated).apply()
                android.widget.Toast.makeText(
                    sharedFavContext,
                    if (adding) "★ Favorilere eklendi ve üste sabitlendi" else "Favorilerden çıkarıldı",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    val fromIdx = tabOrder.indexOf(initialState)
                    val toIdx = tabOrder.indexOf(targetState)
                    CosmicMotionSpecs.cosmicTabTransition(forward = toIdx > fromIdx, config = homeMotionConfig)
                },
                modifier = Modifier
                    .weight(1f)
                    .then(
                        Modifier.let {
                            androidx.compose.ui.Modifier.onGloballyPositioned { coords ->
                                contentTopInRootPx = coords.positionInRoot().y
                            }
                        }
                    )
                    // Exact root-coordinate measured feathered DstIn vertical fade mask right above the bottom dock:
                    // Scrolling cards dissolve smoothly over a ~30dp feather zone before reaching the top border
                    // of the bottom dock, so there is NO hard horizontal scissor-cut line when scrolling AND
                    // NO cards ever show through behind the see-through glass bottom bar!
                    .graphicsLayer {
                        compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        val h = size.height
                        if (h > 0f) {
                            val dockTopLocalY = if (bottomBarTopInRootPx > 0f) {
                                (bottomBarTopInRootPx - contentTopInRootPx).coerceIn(0f, h)
                            } else {
                                (h - 94.dp.toPx()).coerceAtLeast(0f)
                            }
                            val fadeEndPx = (dockTopLocalY - 2.dp.toPx()).coerceAtLeast(1f)
                            val fadeStartPx = (fadeEndPx - 30.dp.toPx()).coerceAtLeast(0f)
                            val s0 = (fadeStartPx / h).coerceIn(0f, 1f)
                            val s1 = (fadeEndPx / h).coerceIn(s0, 1f)
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0.00f to Color.White,
                                    s0 to Color.White,
                                    (s0 + (s1 - s0) * 0.35f) to Color.White.copy(alpha = 0.72f),
                                    (s0 + (s1 - s0) * 0.70f) to Color.White.copy(alpha = 0.28f),
                                    s1 to Color.Transparent,
                                    1.00f to Color.Transparent
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                    }
            ) { tab ->
                when (tab) {
                    "templates" -> {
                        ExploreTabContent(
                            userSettings = userSettings,
                            existingBots = botList,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onImportPresetBot = onImportPresetBot,
                            onOpenExistingBot = onOpenBot,
                            onOpenAidenMenu = { showAidenStoriesMenu = true },
                            onOpenSettings = { showGlobalSettings = true },
                            lazyListState = templatesListState
                        )
                    }
                    "discover" -> {
                        val publicBots = remember(botList) {
                            botList.filter { it.isPublic && com.example.data.repository.EmochiRepository.isBotPublishable(it) }
                        }
                        DiscoverTabContent(
                            botList = publicBots,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onOpenBot = onOpenBot,
                            onOpenSettings = { showGlobalSettings = true },
                            lazyListState = discoverListState,
                            onCloneBot = onCloneBot,
                            favoriteBotIds = sharedFavoriteBotIds,
                            onToggleFavorite = toggleSharedFavorite,
                            frostedBackdrop = frostedCosmicBitmap,
                            scrollSync = activeScrollSync
                        )
                    }
                    "books" -> {
                        BooksTabContent(
                            botList = botList,
                            userSettings = userSettings,
                            onOpenBot = onOpenBot,
                            onDeleteBot = onDeleteBot,
                            onImportPresetBot = { bot -> onImportPresetBot?.invoke(bot) },
                            onOpenSettings = { showGlobalSettings = true },
                            lazyListState = booksListState,
                            frostedBackdrop = frostedCosmicBitmap,
                            scrollSync = activeScrollSync
                        )
                    }
                    else -> {
                        // "chats" tab -> Shows user's active bots
                        ChatsTabContent(
                            botList = botList,
                            userSettings = userSettings,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            selectedFilter = selectedFilter,
                            onFilterChange = { selectedFilter = it },
                            confirmDeleteId = confirmDeleteId,
                            onConfirmDeleteChange = { confirmDeleteId = it },
                            onOpenBot = onOpenBot,
                            onDeleteBot = onDeleteBot,
                            onTogglePrivacy = onTogglePrivacy,
                            onCloneBot = onCloneBot,
                            onOpenAidenMenu = { showAidenStoriesMenu = true },
                            onOpenSettings = { showGlobalSettings = true },
                            onNewBot = onNewBot,
                            lazyListState = chatsListState,
                            favoriteBotIds = sharedFavoriteBotIds,
                            onToggleFavorite = toggleSharedFavorite,
                            frostedBackdrop = frostedCosmicBitmap,
                            scrollSync = activeScrollSync
                        )
                    }
                }
            }
        }
    }

        if (showGlobalSettings && userSettings != null) {
            GlobalSettingsModal(
                settings = userSettings,
                onDismiss = { showGlobalSettings = false },
                onSaveSettings = onSaveSettings,
                onExportData = onExportData,
                onImportData = onImportData
            )
        }
    }

    if (showAidenStoriesMenu) {
        AidenStoriesModal(
            userSettings = userSettings,
            existingBots = botList,
            onDismiss = { showAidenStoriesMenu = false },
            onImportPresetBot = onImportPresetBot,
            onOpenExistingBot = onOpenBot
        )
    }
}

@Composable
fun TopHeader(
    activeTab: String,
    botCount: Int,
    onOpenSettings: () -> Unit,
    frostedBackdrop: ImageBitmap? = null,
    scrollSync: Int = 0
) {
    val context = LocalContext.current
    val resolvedBackdrop = frostedBackdrop ?: remember(context) {
        createFrostedGothicManorBitmap(context, R.drawable.img_botlist_gothic_wisteria_bg)
    }

    val headerMotionConfig = LocalCosmicMotionConfig.current
    val settingsOrbitRot: Float
    val waveAngle: Float
    val starPulse: Float
    if (headerMotionConfig.enableAmbientLoops) {
        val infinite = rememberInfiniteTransition(label = "header")
        settingsOrbitRot = infinite.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(11000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "settingsOrbitRot"
        ).value
        waveAngle = infinite.animateFloat(
            initialValue = -6f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "waveAngle"
        ).value
        starPulse = infinite.animateFloat(
            initialValue = 0.72f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "starPulse"
        ).value
    } else {
        settingsOrbitRot = 0f
        waveAngle = 0f
        starPulse = 0.88f
    }

    val titleText = when (activeTab) {
        "templates" -> "Şablonlar"
        "discover"  -> "Keşfet"
        "books"     -> "Kitaplar"
        else        -> "Botlarım"
    }
    val subtitleText = when (activeTab) {
        "templates" -> "Hazır dünyaları keşfet ve maceraya başla."
        "discover"  -> "Topluluk karakterlerini incele ve sohbete katıl."
        "books"     -> "Etkileşimli hikayelerini ve serilerini yönet."
        else        -> "Kendi botlarını yönet ve sohbetlerine devam et."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Greeting Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👋",
                        fontSize = 17.sp,
                        modifier = Modifier.graphicsLayer { rotationZ = waveAngle }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hoş geldin",
                        fontFamily = LiterarySerifFontFamily,
                        color = Color(0xFFF0E7FF),
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Normal,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(
                                color = Color(0xAA120524),
                                offset = Offset(0f, 1.5f),
                                blurRadius = 6f
                            )
                        )
                    )
                }

                // Calligraphic Script Hero Title ("Botlarım", "Keşfet", "Şablonlar", "Kitaplar")
                // Renders seamless Great Vibes glyphs for Turkish ş/Ş/ğ/Ğ/İ without jarring Roboto fallback
                CalligraphicTurkishTitle(
                    text = titleText,
                    modifier = Modifier.offset(y = (-2).dp)
                )

                // Ornamental Glowing Starlight Curve Underline cleanly spaced below script descenders
                Canvas(
                    modifier = Modifier
                        .padding(top = 1.dp, bottom = 2.dp)
                        .width(196.dp)
                        .height(14.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val cy = h * 0.56f

                    // Soft curved light arc peaking gently in the center
                    val arcPath = Path().apply {
                        moveTo(0f, cy + 2.5.dp.toPx())
                        quadraticBezierTo(w * 0.50f, cy - 3.dp.toPx(), w, cy + 2.5.dp.toPx())
                    }
                    drawPath(
                        path = arcPath,
                        brush = Brush.horizontalGradient(
                            0.00f to Color.Transparent,
                            0.20f to Color(0x70A855F7),
                            0.50f to Color(0xF5F3E8FF),
                            0.80f to Color(0x70A855F7),
                            1.00f to Color.Transparent
                        ),
                        style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Center 4-point star flare
                    val starCx = w * 0.50f
                    val starCy = cy - 0.5.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.85f * starPulse),
                                Color(0xFFD8B4FE).copy(alpha = 0.42f * starPulse),
                                Color.Transparent
                            ),
                            center = Offset(starCx, starCy),
                            radius = 9.dp.toPx()
                        ),
                        center = Offset(starCx, starCy),
                        radius = 9.dp.toPx()
                    )
                    val sOut = 5.5.dp.toPx() * (0.9f + 0.1f * starPulse)
                    val sIn = 1.3.dp.toPx()
                    val starPath = Path().apply {
                        moveTo(starCx, starCy - sOut)
                        lineTo(starCx + sIn, starCy - sIn)
                        lineTo(starCx + sOut * 1.45f, starCy)
                        lineTo(starCx + sIn, starCy + sIn)
                        lineTo(starCx, starCy + sOut)
                        lineTo(starCx - sIn, starCy + sIn)
                        lineTo(starCx - sOut * 1.45f, starCy)
                        lineTo(starCx - sIn, starCy - sIn)
                        close()
                    }
                    drawPath(path = starPath, color = Color.White.copy(alpha = 0.96f))
                }
            }

            // Right: Exact Shared Concentric Translucent Mor Cam Settings Button (Same as Şablon Menu)
            MorCamSettingsButton(
                frostedManorBitmap = resolvedBackdrop,
                scrollSync = scrollSync,
                orbitDeg = settingsOrbitRot,
                glowAlpha = starPulse,
                onClick = onOpenSettings,
                testTag = "global_settings_button"
            )
        }

        // Clean Editorial Serif Subtitle directly below the script title + underline
        Text(
            text = subtitleText,
            fontFamily = LiterarySerifFontFamily,
            color = Color(0xFFEAE1FA).copy(alpha = 0.90f),
            fontSize = 13.5.sp,
            lineHeight = 18.sp,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(
                    color = Color(0xAA120524),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 6f
                )
            ),
            modifier = Modifier.offset(y = (-2).dp)
        )
    }
}

@Composable
fun ChatsTabContent(
    botList: List<BotEntity>,
    userSettings: UserSettingsEntity? = null,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    confirmDeleteId: String?,
    onConfirmDeleteChange: (String?) -> Unit,
    onOpenBot: (String) -> Unit,
    onDeleteBot: (String) -> Unit,
    onTogglePrivacy: ((BotEntity) -> Unit)? = null,
    onCloneBot: ((String) -> Unit)? = null,
    onOpenAidenMenu: (() -> Unit)? = null,
    onOpenSettings: () -> Unit,
    onNewBot: (() -> Unit)? = null,
    lazyListState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    favoriteBotIds: Set<String> = emptySet(),
    onToggleFavorite: ((String) -> Unit)? = null,
    frostedBackdrop: ImageBitmap? = null,
    scrollSync: Int = 0
) {
    val context = LocalContext.current
    val resolvedBackdrop = frostedBackdrop ?: remember(context) {
        createFrostedGothicManorBitmap(context, R.drawable.img_botlist_cosmic_bg)
    }

    val toggleFavorite: (String) -> Unit = { botId ->
        onToggleFavorite?.invoke(botId)
    }

    // Sort & Quick Filter Mode controlled by the circular Tune Glass Orb next to the Search bar
    // Default to "favorites_first" so starring any bot immediately pins it to the top of the list!
    var sortMode by remember { mutableStateOf("favorites_first") }
    var showSortMenu by remember { mutableStateOf(false) }

    val chatsOnlyList = remember(botList) {
        botList.filter { bot ->
            bot.mode != "book" &&
            bot.id != "preset_aiden_mcu_cosmic" &&
            !bot.id.startsWith("book_") &&
            !bot.aiName.contains("Kitap") &&
            !bot.aiPersonality.contains("KİTAP MODU") &&
            !bot.scenario.contains("KİTAP MODU")
        }
    }

    val filteredBots = remember(chatsOnlyList, searchQuery, selectedFilter, sortMode, favoriteBotIds) {
        val base = chatsOnlyList.filter { bot ->
            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                    bot.aiName.lowercase().contains(query) ||
                    bot.universeName.lowercase().contains(query) ||
                    bot.scenario.lowercase().contains(query)

            val matchesFilter = when (selectedFilter) {
                "favorites" -> favoriteBotIds.contains(bot.id)
                "personal" -> bot.mode == "personal"
                "universe" -> bot.mode == "universe"
                else -> true
            }
            val matchesFav = sortMode != "favorites_only" || favoriteBotIds.contains(bot.id)
            matchesFilter && matchesQuery && matchesFav
        }
        when (sortMode) {
            "favorites_first" -> base.sortedByDescending { if (favoriteBotIds.contains(it.id)) 1 else 0 }
            "name_asc" -> base.sortedBy {
                (if (it.mode == "universe") it.universeName.ifBlank { it.aiName } else it.aiName).lowercase()
            }
            else -> base
        }
    }

    val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 168.dp + navBottomPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Header (Greeting + Calligraphic Script "Botlarım" + Starlight Underline + Subtitle + Orbiting Gear Orb)
        item(key = "top_header") {
            TopHeader(
                activeTab = "chats",
                botCount = chatsOnlyList.size,
                onOpenSettings = onOpenSettings,
                frostedBackdrop = resolvedBackdrop,
                scrollSync = scrollSync
            )
        }

        // 2. Frosted Search Capsule + Circular Tune/Filter Orb + Horizontal Category Filter Pills
        item(key = "search_and_filters") {
            Column(modifier = Modifier.fillMaxWidth()) {
                var isSearchFocused by remember { mutableStateOf(false) }
                val searchBorderAlpha by animateFloatAsState(
                    targetValue = if (isSearchFocused) 1f else 0.86f,
                    animationSpec = tween(220),
                    label = "searchBorder"
                )
                val searchShape = RoundedCornerShape(50)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Wide Frosted Glass Search Pill
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .drawBehind {
                                drawIntoCanvas { canvas ->
                                    val searchGlow = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                        style = android.graphics.Paint.Style.STROKE
                                        strokeWidth = 1.6.dp.toPx()
                                        color = Color(0x489333EA).toArgb()
                                        maskFilter = android.graphics.BlurMaskFilter(
                                            6.dp.toPx(),
                                            android.graphics.BlurMaskFilter.Blur.NORMAL
                                        )
                                    }
                                    val r = size.height * 0.5f
                                    canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, r, r, searchGlow)
                                }
                            }
                            .clip(searchShape)
                            .templatesBackdropBlur(
                                blurredBitmap = resolvedBackdrop,
                                alpha = 0.78f,
                                scrollSync = scrollSync
                            )
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x56241044),
                                        Color(0x48140829),
                                        Color(0x521F0D3B)
                                    )
                                )
                            )
                            .drawBehind {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        0.00f to Color(0x26E9D5FF),
                                        0.24f to Color.Transparent,
                                        0.78f to Color.Transparent,
                                        1.00f to Color(0x1E7C3AED)
                                    )
                                )
                            }
                            .border(
                                BorderStroke(
                                    if (isSearchFocused) 1.35.dp else 1.1.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xE0E9D5FF).copy(alpha = searchBorderAlpha),
                                            Color(0x889F7AEA).copy(alpha = searchBorderAlpha),
                                            Color(0x706D28D9).copy(alpha = searchBorderAlpha),
                                            Color(0xD4D8B4FE).copy(alpha = searchBorderAlpha)
                                        )
                                    )
                                ),
                                searchShape
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Ara",
                            tint = Color(0xFFF3E8FF),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Botlarını ara...",
                                    fontFamily = LiterarySerifFontFamily,
                                    color = Color(0xFFD4C4EE).copy(alpha = 0.80f),
                                    fontSize = 14.5.sp
                                )
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                singleLine = true,
                                cursorBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFD8B4FE))),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = LiterarySerifFontFamily,
                                    color = Color(0xFFFBF8FF),
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_bot_field")
                                    .onFocusChanged { isSearchFocused = it.isFocused }
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x459333EA))
                                    .border(0.8.dp, Color(0x99E9D5FF), CircleShape)
                                    .clickable { onSearchQueryChange("") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Temizle",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    // Right Circular Frosted Glass Tune / Sort Orb (Matches Reference)
                    Box {
                        val hasActiveSort = sortMode != "recent"
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .drawBehind {
                                    drawIntoCanvas { canvas ->
                                        val orbHalo = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                            style = android.graphics.Paint.Style.STROKE
                                            strokeWidth = 1.6.dp.toPx()
                                            color = (if (hasActiveSort) Color(0x88A855F7) else Color(0x489333EA)).toArgb()
                                            maskFilter = android.graphics.BlurMaskFilter(
                                                6.dp.toPx(),
                                                android.graphics.BlurMaskFilter.Blur.NORMAL
                                            )
                                        }
                                        canvas.nativeCanvas.drawCircle(
                                            size.width * 0.5f,
                                            size.height * 0.5f,
                                            size.minDimension * 0.5f,
                                            orbHalo
                                        )
                                    }
                                }
                                .clip(CircleShape)
                                .templatesBackdropBlur(
                                    blurredBitmap = resolvedBackdrop,
                                    alpha = 0.78f,
                                    scrollSync = scrollSync
                                )
                                .background(
                                    if (hasActiveSort) {
                                        Brush.radialGradient(
                                            listOf(Color(0x807C3AED), Color(0x643B1070))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(Color(0x58261148), Color(0x48140829))
                                        )
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        1.15.dp,
                                        Brush.linearGradient(
                                            if (hasActiveSort) {
                                                listOf(Color.White, Color(0xFFD8B4FE), Color(0xFFA855F7))
                                            } else {
                                                listOf(Color(0xDCE9D5FF), Color(0x809F7AEA), Color(0xCCD8B4FE))
                                            }
                                        )
                                    ),
                                    CircleShape
                                )
                                .clickable { showSortMenu = true }
                                .testTag("bot_sort_filter_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Sırala ve Filtrele",
                                tint = Color(0xFFFBF8FF),
                                modifier = Modifier.size(21.dp)
                            )
                            if (hasActiveSort) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE9D5FF))
                                )
                            }
                        }

                        MorCamPopupMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            headerLabel = "SIRALAMA & FİLTRE",
                            minWidth = 232.dp
                        ) {
                            val sortOptions = listOf(
                                Triple("recent", "Varsayılan Sıralama", Icons.Default.AutoAwesome),
                                Triple("favorites_first", "Önce Favoriler", Icons.Default.Star),
                                Triple("favorites_only", "Sadece Favoriler", Icons.Default.Favorite),
                                Triple("name_asc", "İsme Göre (A-Z)", Icons.Default.SortByAlpha)
                            )
                            sortOptions.forEach { (key, title, iconVec) ->
                                MorCamPopupMenuItem(
                                    title = title,
                                    icon = iconVec,
                                    selected = sortMode == key,
                                    onClick = {
                                        sortMode = key
                                        showSortMenu = false
                                    },
                                    testTag = "sort_option_$key"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Filter Pills Row (Reference layout: Icon + Label + Circular Count Badge)
                // Smooth dynamic alpha fading-edge transitions on both left & right edges + bloom clearance
                val allCount = chatsOnlyList.size
                val favoritesCount = chatsOnlyList.count { favoriteBotIds.contains(it.id) }
                val personalCount = chatsOnlyList.count { it.mode == "personal" }
                val universeCount = chatsOnlyList.count { it.mode == "universe" }
                val filterScrollState = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            val w = size.width
                            if (w > 1f) {
                                val fadePx = 44.dp.toPx().coerceAtMost(w * 0.28f)
                                val scrollPx = filterScrollState.value.toFloat()
                                val maxScrollPx = filterScrollState.maxValue.toFloat()
                                val rampPx = 24.dp.toPx().coerceAtLeast(1f)

                                val leftStrength = (scrollPx / rampPx).coerceIn(0f, 1f)
                                val rightStrength = if (maxScrollPx > 0f) {
                                    ((maxScrollPx - scrollPx) / rampPx).coerceIn(0f, 1f)
                                } else 0f

                                if (leftStrength > 0.01f) {
                                    val edgeW = fadePx * leftStrength
                                    val endFrac = (edgeW / w).coerceIn(0.001f, 0.45f)
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            0.00f to Color.Transparent,
                                            (endFrac * 0.35f) to Color.White.copy(alpha = 0.28f),
                                            (endFrac * 0.72f) to Color.White.copy(alpha = 0.76f),
                                            endFrac to Color.White,
                                            1.00f to Color.White
                                        ),
                                        blendMode = BlendMode.DstIn
                                    )
                                }

                                if (rightStrength > 0.01f) {
                                    val edgeW = fadePx * rightStrength
                                    val startFrac = (1f - edgeW / w).coerceIn(0.55f, 0.999f)
                                    val span = 1f - startFrac
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            0.00f to Color.White,
                                            startFrac to Color.White,
                                            (startFrac + span * 0.28f) to Color.White.copy(alpha = 0.76f),
                                            (startFrac + span * 0.65f) to Color.White.copy(alpha = 0.28f),
                                            1.00f to Color.Transparent
                                        ),
                                        blendMode = BlendMode.DstIn
                                    )
                                }
                            }
                        }
                        .horizontalScroll(filterScrollState)
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filters = listOf(
                        Triple("all", "Tümü", allCount),
                        Triple("favorites", "Favoriler", favoritesCount),
                        Triple("personal", "Karakterler", personalCount),
                        Triple("universe", "Evrenler", universeCount)
                    )

                    filters.forEach { (key, label, count) ->
                        val isSelected = selectedFilter == key
                        val shape = RoundedCornerShape(50)

                        val chipScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.02f else 1f,
                            animationSpec = tween(180, easing = FastOutSlowInEasing),
                            label = "chipScale_$key"
                        )

                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .graphicsLayer { scaleX = chipScale; scaleY = chipScale }
                                .drawBehind {
                                    drawIntoCanvas { canvas ->
                                        val outerBloom = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                            style = android.graphics.Paint.Style.STROKE
                                            strokeWidth = if (isSelected) 2.2.dp.toPx() else 1.2.dp.toPx()
                                            color = (if (isSelected) Color(0x889333EA) else Color(0x307C3AED)).toArgb()
                                            maskFilter = android.graphics.BlurMaskFilter(
                                                if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                                                android.graphics.BlurMaskFilter.Blur.NORMAL
                                            )
                                        }
                                        val r = size.height * 0.5f
                                        canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, r, r, outerBloom)
                                    }
                                }
                                .clip(shape)
                                .templatesBackdropBlur(
                                    blurredBitmap = resolvedBackdrop,
                                    alpha = if (isSelected) 0.65f else 0.78f,
                                    scrollSync = scrollSync
                                )
                                .background(
                                    if (isSelected) {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0x949333EA),
                                                Color(0x806D28D9),
                                                Color(0x744C1D95),
                                                Color(0x887E22CE)
                                            ),
                                            start = Offset(0f, 0f),
                                            end = Offset(240f, 110f)
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(Color(0x54230F42), Color(0x46130827))
                                        )
                                    }
                                )
                                .drawBehind {
                                    if (isSelected) {
                                        // Top pearl-lavender crystal highlight inside active chip
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.34f),
                                                    Color(0xFFE9D5FF).copy(alpha = 0.14f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(size.width * 0.25f, 0f),
                                                radius = size.width * 0.55f
                                            ),
                                            center = Offset(size.width * 0.25f, 0f),
                                            radius = size.width * 0.55f
                                        )
                                    } else {
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                0.00f to Color(0x20E9D5FF),
                                                0.30f to Color.Transparent,
                                                1.00f to Color(0x166D28D9)
                                            )
                                        )
                                    }
                                }
                                .border(
                                    BorderStroke(
                                        width = if (isSelected) 1.4.dp else 1.05.dp,
                                        brush = if (isSelected) {
                                            Brush.linearGradient(
                                                listOf(
                                                    Color(0xFFF8F4FF),
                                                    Color(0xFFE9D5FF),
                                                    Color(0xFFC084FC),
                                                    Color(0xFFE9D5FF)
                                                )
                                            )
                                        } else {
                                            Brush.linearGradient(
                                                listOf(Color(0xBBE9D5FF), Color(0x668B5CF6), Color(0x99C4B5FD))
                                            )
                                        }
                                    ),
                                    shape = shape
                                )
                                .clickable { onFilterChange(key) }
                                .testTag("filter_chip_$key")
                                .padding(horizontal = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when (key) {
                                    "all" -> {
                                        // Crisp 2x2 rounded square grid icon
                                        Canvas(modifier = Modifier.size(16.dp)) {
                                            val stroke = Stroke(width = 1.65.dp.toPx())
                                            val tintColor = if (isSelected) Color.White else Color(0xFFD8B4FE)
                                            val cell = size.width * 0.38f
                                            val gap = size.width * 0.14f
                                            val start = size.width * 0.05f
                                            val cr = CornerRadius(1.8.dp.toPx())
                                            drawRoundRect(tintColor, Offset(start, start), Size(cell, cell), cr, stroke)
                                            drawRoundRect(tintColor, Offset(start + cell + gap, start), Size(cell, cell), cr, stroke)
                                            drawRoundRect(tintColor, Offset(start, start + cell + gap), Size(cell, cell), cr, stroke)
                                            drawRoundRect(tintColor, Offset(start + cell + gap, start + cell + gap), Size(cell, cell), cr, stroke)
                                        }
                                    }
                                    "favorites" -> {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFFFDE68A) else Color(0xFFD8B4FE),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    "personal" -> {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else Color(0xFFD8B4FE),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    else -> {
                                        // Ringed Planet icon (Saturn motif from reference)
                                        Canvas(modifier = Modifier.size(18.dp)) {
                                            val c = Offset(size.width * 0.5f, size.height * 0.5f)
                                            val planetColor = if (isSelected) Color.White else Color(0xFFD8B4FE)
                                            val r = size.minDimension * 0.28f
                                            drawCircle(color = planetColor, radius = r, center = c)
                                            rotate(degrees = -24f, pivot = c) {
                                                drawOval(
                                                    color = planetColor,
                                                    topLeft = Offset(c.x - size.width * 0.46f, c.y - size.height * 0.14f),
                                                    size = Size(size.width * 0.92f, size.height * 0.28f),
                                                    style = Stroke(width = 1.4.dp.toPx())
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = label,
                                    fontFamily = LiterarySerifFontFamily,
                                    color = if (isSelected) Color.White else Color(0xFFEDE4FF),
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false
                                )

                                // Circular Frosted Count Badge (Matches Reference)
                                Box(
                                    modifier = Modifier
                                        .size(23.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color(0x45FFFFFF) else Color(0x4432175E)
                                        )
                                        .border(
                                            0.85.dp,
                                            if (isSelected) Color(0xCCF3E8FF) else Color(0x70B794F4),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = count.toString(),
                                        fontFamily = InterFontFamily,
                                        color = if (isSelected) Color.White else Color(0xFFE9D5FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Bot Items or Harmonized Frosted Empty States
        if (chatsOnlyList.isEmpty()) {
            item(key = "empty_no_bots") {
                val emptyShape = RoundedCornerShape(24.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(emptyShape)
                        .templatesBackdropBlur(
                            blurredBitmap = resolvedBackdrop,
                            alpha = 0.78f,
                            scrollSync = scrollSync
                        )
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x5C220F40), Color(0x4C130827))
                            )
                        )
                        .border(
                            BorderStroke(
                                1.15.dp,
                                Brush.linearGradient(listOf(Color(0xCCE9D5FF), Color(0x707C3AED), Color(0xCCD8B4FE)))
                            ),
                            emptyShape
                        )
                        .padding(26.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.cosmicFloatingLevitation(amplitudeDp = 4.dp, durationMs = 2800)) {
                            OrbView(hue = 275f, size = 58.dp)
                        }
                        Text(
                            text = "Henüz hiç karakter yok",
                            fontFamily = LiterarySerifFontFamily,
                            color = Color(0xFFFBF8FF),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Aşağıdaki karttan veya (+) butonundan hayalindeki karakteri yaratabilirsin.",
                            fontFamily = LiterarySerifFontFamily,
                            color = Color(0xFFD8C8F2),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else if (filteredBots.isEmpty()) {
            item(key = "empty_search") {
                val emptyShape = RoundedCornerShape(22.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(emptyShape)
                        .templatesBackdropBlur(
                            blurredBitmap = resolvedBackdrop,
                            alpha = 0.78f,
                            scrollSync = scrollSync
                        )
                        .background(Color(0x52180A30))
                        .border(
                            BorderStroke(
                                1.05.dp,
                                Brush.linearGradient(listOf(Color(0xBBE9D5FF), Color(0x557C3AED)))
                            ),
                            emptyShape
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFFD8B4FE),
                            modifier = Modifier
                                .size(26.dp)
                                .cosmicFloatingLevitation(amplitudeDp = 3.dp, durationMs = 2600)
                        )
                        Text(
                            text = if (sortMode == "favorites_only" || selectedFilter == "favorites") {
                                "Henüz favorilere eklenmiş bot yok. Bot kartlarındaki yıldız (★) simgesine dokunarak favori ekleyebilirsin."
                            } else {
                                "Aramanıza uygun sonuç bulunamadı."
                            },
                            fontFamily = LiterarySerifFontFamily,
                            color = Color(0xFFE9D5FF),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        if (searchQuery.isNotBlank() || sortMode != "favorites_first" || selectedFilter != "all") {
                            Spacer(modifier = Modifier.height(4.dp))
                            MorCamChipPill(
                                label = "Filtreyi Sıfırla",
                                selected = true,
                                icon = Icons.Default.Refresh,
                                onClick = {
                                    onSearchQueryChange("")
                                    onFilterChange("all")
                                    sortMode = "favorites_first"
                                }
                            )
                        }
                    }
                }
            }
        } else {
            itemsIndexed(filteredBots, key = { _, it -> it.id }) { idx, bot ->
                BotCardItem(
                    bot = bot,
                    confirmDeleteId = confirmDeleteId,
                    onConfirmDeleteChange = onConfirmDeleteChange,
                    onOpenBot = onOpenBot,
                    onDeleteBot = onDeleteBot,
                    onTogglePrivacy = onTogglePrivacy,
                    onCloneBot = onCloneBot,
                    isFavorite = favoriteBotIds.contains(bot.id),
                    onToggleFavorite = { toggleFavorite(bot.id) },
                    frostedBackdrop = resolvedBackdrop,
                    scrollSync = scrollSync,
                    staggerIndex = idx
                )
            }
        }

        // 4. "Yeni bir bot oluştur" Frosted Mor Cam CTA Banner Card (Matches Reference)
        if (onNewBot != null) {
            item(key = "create_bot_cta_banner") {
                val ctaShape = RoundedCornerShape(30.dp)
                val ctaInteraction = remember { MutableInteractionSource() }
                val ctaPressed by ctaInteraction.collectIsPressedAsState()
                val ctaScale by animateFloatAsState(
                    targetValue = if (ctaPressed) 0.972f else 1f,
                    animationSpec = CosmicMotionSpecs.snappyOrb(),
                    label = "ctaBannerScale"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                        .height(74.dp)
                        .graphicsLayer {
                            scaleX = ctaScale
                            scaleY = ctaScale
                        }
                        .drawBehind {
                            drawIntoCanvas { canvas ->
                                val ctaHalo = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeWidth = 1.6.dp.toPx()
                                    color = Color(0x469333EA).toArgb()
                                    maskFilter = android.graphics.BlurMaskFilter(
                                        7.dp.toPx(),
                                        android.graphics.BlurMaskFilter.Blur.NORMAL
                                    )
                                }
                                val r = 30.dp.toPx()
                                canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, r, r, ctaHalo)
                            }
                        }
                        .clip(ctaShape)
                        .cosmicGlassSheen(cornerRadius = 30.dp, periodMs = 4400)
                        .templatesBackdropBlur(
                            blurredBitmap = resolvedBackdrop,
                            alpha = 0.78f,
                            scrollSync = scrollSync
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0x5E261148),
                                    Color(0x4E16092D),
                                    Color(0x5C2C1554)
                                )
                            )
                        )
                        .drawBehind {
                            // Top & bottom subtle frosted glass edge sheen
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0.00f to Color(0x2AE9D5FF),
                                    0.22f to Color.Transparent,
                                    0.80f to Color.Transparent,
                                    1.00f to Color(0x247C3AED)
                                )
                            )
                            // Gentle ambient amethyst bloom behind the left 4-point star
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x36A855F7),
                                        Color(0x166D28D9),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.26f, size.height * 0.50f),
                                    radius = 52.dp.toPx()
                                ),
                                center = Offset(size.width * 0.26f, size.height * 0.50f),
                                radius = 52.dp.toPx()
                            )
                        }
                        .border(
                            BorderStroke(
                                1.15.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xE0E9D5FF),
                                        Color(0x889F7AEA),
                                        Color(0x685B21B6),
                                        Color(0xDCD8B4FE)
                                    )
                                )
                            ),
                            ctaShape
                        )
                        .clickable(
                            interactionSource = ctaInteraction,
                            indication = null
                        ) { onNewBot() }
                        .testTag("create_new_bot_banner")
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.width(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Crisp Luminous 4-Point Sparkle Star Emblem
                            Canvas(modifier = Modifier.size(30.dp)) {
                                val cx = size.width * 0.5f
                                val cy = size.height * 0.5f
                                val rOut = size.minDimension * 0.46f
                                val rIn = rOut * 0.26f
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(
                                            Color(0x70D8B4FE),
                                            Color(0x289333EA),
                                            Color.Transparent
                                        ),
                                        center = Offset(cx, cy),
                                        radius = size.minDimension * 0.56f
                                    ),
                                    center = Offset(cx, cy),
                                    radius = size.minDimension * 0.56f
                                )
                                val star = Path().apply {
                                    moveTo(cx, cy - rOut)
                                    quadraticBezierTo(cx, cy - rIn, cx + rOut, cy)
                                    quadraticBezierTo(cx, cy + rIn, cx, cy + rOut)
                                    quadraticBezierTo(cx, cy + rIn, cx - rOut, cy)
                                    quadraticBezierTo(cx, cy - rIn, cx, cy - rOut)
                                    close()
                                }
                                drawPath(
                                    path = star,
                                    brush = Brush.verticalGradient(
                                        listOf(Color.White, Color(0xFFE9D5FF), Color(0xFFC084FC))
                                    )
                                )
                            }

                            Column {
                                Text(
                                    text = "Yeni bir bot oluştur",
                                    fontFamily = LiterarySerifFontFamily,
                                    color = Color(0xFFFBF8FF),
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "Hayalindeki karakteri yarat",
                                    fontFamily = LiterarySerifFontFamily,
                                    color = Color(0xFFD8C8F2).copy(alpha = 0.88f),
                                    fontSize = 12.2.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Yeni bot oluştur",
                            tint = Color(0xFFF3E8FF),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoverTabContent(
    botList: List<BotEntity>, // PUBLIC BOTS ONLY
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenBot: (String) -> Unit,
    onOpenSettings: () -> Unit,
    lazyListState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onCloneBot: ((String) -> Unit)? = null,
    favoriteBotIds: Set<String> = emptySet(),
    onToggleFavorite: ((String) -> Unit)? = null,
    frostedBackdrop: ImageBitmap? = null,
    scrollSync: Int = 0
) {
    val filteredPublicBots = remember(botList, searchQuery, favoriteBotIds) {
        botList.filter { bot ->
            val query = searchQuery.trim().lowercase()
            query.isEmpty() ||
                    bot.aiName.lowercase().contains(query) ||
                    bot.universeName.lowercase().contains(query) ||
                    bot.scenario.lowercase().contains(query)
        }.sortedByDescending { if (favoriteBotIds.contains(it.id)) 1 else 0 }
    }

    val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 168.dp + navBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "top_header") {
            TopHeader(
                activeTab = "discover",
                botCount = filteredPublicBots.size,
                onOpenSettings = onOpenSettings,
                frostedBackdrop = frostedBackdrop,
                scrollSync = scrollSync
            )
        }

        item(key = "discover_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                com.example.ui.components.MorCamGlassCard(
                    cornerRadius = 20.dp,
                    highlighted = true,
                    paddingHorizontal = 16.dp,
                    paddingVertical = 14.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0x998B5CF6), Color(0x663B1470))
                                    )
                                )
                                .border(1.dp, com.example.ui.components.MorCamTokens.RimGradient, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = com.example.ui.components.MorCamTokens.SilverLavenderBright,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Velora Keşfet • Topluluk",
                                fontFamily = DisplaySerifFontFamily,
                                color = com.example.ui.components.MorCamTokens.SilverLavenderBright,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                style = TextStyle(shadow = com.example.ui.components.MorCamTokens.TitleShadow)
                            )
                            Text(
                                text = "Topluluğun herkese açık paylaştığı karakterler ve evrenler.",
                                fontFamily = InterFontFamily,
                                color = com.example.ui.components.MorCamTokens.LavenderMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                com.example.ui.components.MorCamGlassTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = "Keşfette karakter veya evren ara...",
                    leadingIcon = Icons.Default.Search,
                    trailingContent = if (searchQuery.isNotEmpty()) {
                        {
                            com.example.ui.components.MorCamCircleIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Aramayı Temizle",
                                onClick = { onSearchQueryChange("") },
                                size = 30.dp
                            )
                        }
                    } else null,
                    singleLine = true,
                    testTag = "discover_search_input"
                )
            }
        }

        if (filteredPublicBots.isEmpty()) {
            item(key = "empty_discover") {
                com.example.ui.components.MorCamGlassCard(
                    cornerRadius = 24.dp,
                    highlighted = false,
                    paddingHorizontal = 22.dp,
                    paddingVertical = 28.dp,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(66.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0x997C3AED), Color(0x552E1065), Color.Transparent)
                                    )
                                )
                                .border(1.2.dp, com.example.ui.components.MorCamTokens.RimGradient, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(34.dp)) {
                                drawMorCamFourPointStar(
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    outerRadius = size.minDimension * 0.44f,
                                    innerRadius = size.minDimension * 0.16f
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Eşleşen topluluk botu bulunamadı" else "Henüz herkese açık bot paylaşılmadı",
                            fontFamily = DisplaySerifFontFamily,
                            color = com.example.ui.components.MorCamTokens.SilverLavenderBright,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ayla ve Aetheria gibi varsayılan şablonlar size özeldir. Kendi oluşturduğunuz botları 'Herkese Açık' yaparak Keşfet vitrininde paylaşabilirsiniz.",
                            fontFamily = InterFontFamily,
                            color = com.example.ui.components.MorCamTokens.LavenderMuted,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredPublicBots, key = { it.id }) { bot ->
                BotCardItem(
                    bot = bot,
                    confirmDeleteId = null,
                    onConfirmDeleteChange = {},
                    onOpenBot = onOpenBot,
                    onDeleteBot = null,
                    onTogglePrivacy = null,
                    onCloneBot = onCloneBot,
                    isFavorite = favoriteBotIds.contains(bot.id),
                    onToggleFavorite = onToggleFavorite?.let { toggle -> { toggle(bot.id) } },
                    frostedBackdrop = frostedBackdrop,
                    scrollSync = scrollSync
                )
            }
        }
    }
}

/**
 * Renders calligraphic Great Vibes titles ("Botlarım", "Keşfet", "Şablonlar", "Kitaplar")
 * with native-looking Turkish diacritics (ş/Ş cedilla, ğ/Ğ breve, İ dot) so bundled latin-subset
 * Great Vibes never falls back to a mismatched upright Roboto glyph for 'ş'.
 */
@Composable
private fun CalligraphicTurkishTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val baseText = remember(text) {
        buildString(text.length) {
            text.forEach { ch ->
                append(
                    when (ch) {
                        'ş' -> 's'
                        'Ş' -> 'S'
                        'ğ' -> 'g'
                        'Ğ' -> 'G'
                        'İ' -> 'I'
                        else -> ch
                    }
                )
            }
        }
    }
    val diacriticIndices = remember(text) {
        text.mapIndexedNotNull { idx, ch ->
            when (ch) {
                'ş', 'Ş', 'ğ', 'Ğ', 'İ' -> idx to ch
                else -> null
            }
        }
    }
    var textLayoutResult by remember(text) { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = baseText,
        onTextLayout = { textLayoutResult = it },
        style = androidx.compose.ui.text.TextStyle(
            fontFamily = CalligraphicScriptFontFamily,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFF5EEFF),
                    Color(0xFFD8B4FE)
                )
            ),
            fontSize = 52.sp,
            lineHeight = 56.sp,
            fontWeight = FontWeight.Normal,
            shadow = Shadow(
                color = Color(0xCC7C3AED),
                offset = Offset(0f, 2f),
                blurRadius = 18f
            )
        ),
        modifier = modifier
            .semantics { contentDescription = text }
            .drawWithContent {
                drawContent()
                val layout = textLayoutResult ?: return@drawWithContent
                if (diacriticIndices.isEmpty()) return@drawWithContent
                val baseline = layout.getLineBaseline(0)
                val accentBrush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF7F0FF), Color(0xFFD8B4FE))
                )
                diacriticIndices.forEach { (idx, ch) ->
                    if (idx < baseText.length) {
                        val box = layout.getBoundingBox(idx)
                        when (ch) {
                            'ş', 'Ş' -> {
                                // Delicate italic calligraphic cedilla matching Great Vibes stroke flow
                                val startX = box.left + box.width * if (ch == 'Ş') 0.42f else 0.36f
                                val startY = baseline - 0.5.dp.toPx()
                                val cedillaPath = Path().apply {
                                    moveTo(startX, startY)
                                    cubicTo(
                                        startX - 1.2.dp.toPx(), startY + 2.8.dp.toPx(),
                                        startX + 3.2.dp.toPx(), startY + 3.0.dp.toPx(),
                                        startX + 2.6.dp.toPx(), startY + 6.4.dp.toPx()
                                    )
                                    cubicTo(
                                        startX + 2.1.dp.toPx(), startY + 8.6.dp.toPx(),
                                        startX - 1.8.dp.toPx(), startY + 9.2.dp.toPx(),
                                        startX - 3.6.dp.toPx(), startY + 7.4.dp.toPx()
                                    )
                                }
                                // Royal violet halo glow behind cedilla matching text shadow
                                drawPath(
                                    path = cedillaPath,
                                    color = Color(0x997C3AED),
                                    style = Stroke(width = 4.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawPath(
                                    path = cedillaPath,
                                    brush = accentBrush,
                                    style = Stroke(width = 1.95.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            'ğ', 'Ğ' -> {
                                val cx = box.left + box.width * 0.55f
                                val topY = box.top + if (ch == 'Ğ') 1.dp.toPx() else 10.dp.toPx()
                                val brevePath = Path().apply {
                                    moveTo(cx - 4.2.dp.toPx(), topY)
                                    quadraticBezierTo(cx, topY + 3.4.dp.toPx(), cx + 4.4.dp.toPx(), topY - 0.5.dp.toPx())
                                }
                                drawPath(
                                    path = brevePath,
                                    color = Color(0x887C3AED),
                                    style = Stroke(width = 3.8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawPath(
                                    path = brevePath,
                                    brush = accentBrush,
                                    style = Stroke(width = 1.75.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            'İ' -> {
                                val cx = box.left + box.width * 0.58f
                                val dotY = box.top + 2.dp.toPx()
                                drawCircle(color = Color(0x997C3AED), radius = 3.6.dp.toPx(), center = Offset(cx, dotY))
                                drawCircle(brush = accentBrush, radius = 1.8.dp.toPx(), center = Offset(cx, dotY))
                            }
                        }
                    }
                }
            }
    )
}


fun getAppPresets(isEnglish: Boolean): List<BotEntity> {
    return listOf(
        BotEntity(
            id = "preset_aria",
            mode = "personal",
            aiName = if (isEnglish) "Aria (Cyber Security)" else "Aria (Siber Güvenlik)",
            aiPersonality = if (isEnglish) "Intelligent, direct-spoken, mysterious, witty, and deeply loyal." else "Zeki, doğrudan konuşan, gizemli, alaycı ama içten içe sadık.",
            scenario = if (isEnglish) "The story of a detective working in the city of Neo-Siberia." else "Neo-Siberia şehrinde çalışan bir dedektifin hikayesi.",
            universeName = "",
            keyCharactersJson = "[]",
            userCharName = if (isEnglish) "Detective" else "Dedektif",
            userCharDesc = if (isEnglish) "Experienced digital forensics expert from Merge City." else "Merge Şehrinden gelen tecrübeli adli bilişim uzmanı.",
            openingMessage = if (isEnglish) "*Aria looks up from her terminal screen and narrows her eyes at you.* Finally you arrived. Clearing the digital traces you left on the servers took hours..." else "*Aria terminal ekranından başını kaldırır ve gözlerini kısarak sana bakar.* Nihayet geldin. Sunucularda bıraktığın dijital izleri temizlemem saatlerimi aldı...",
            writingStyle = "rp",
            intensity = "normal",
            isPublic = false,
            isTemplate = true,
            pinnedMemory = if (isEnglish) "MISSION ::: CYBER ::: Investigating data breach in Merge City." else "GÖREV ::: SİBER ::: Merge şehrindeki veri sızıntısını araştırıyoruz."
        ),
        BotEntity(
            id = "preset_eldoria",
            mode = "universe",
            aiName = if (isEnglish) "Kingdom of Eldoria" else "Eldoria Krallığı",
            aiPersonality = if (isEnglish) "Eldoria Realm Narrator." else "Eldoria anlatıcısı.",
            scenario = if (isEnglish) "A feudal fantasy world on the brink of war. Dragons, kingdoms, and rogue guilds." else "Savaşın eşiğindeki feodal bir fantezi dünyası. Ejderhalar, krallıklar ve loncalar.",
            universeName = if (isEnglish) "Kingdom of Eldoria" else "Eldoria Krallığı",
            keyCharactersJson = "[]",
            userCharName = if (isEnglish) "Warrior" else "Savaşçı",
            userCharDesc = if (isEnglish) "A wandering guild mercenary." else "Gezgin bir lonca paralı askeri.",
            openingMessage = if (isEnglish) "Night settles over Mist Valley at the eastern border of Eldoria. As the tavern door creaks open, a cold wind sweeps in..." else "Eldoria Krallığı'nın doğu sınırındaki Sisli Vadi'de gece çöküyor. Hanın kapısı gıcırdayarak açıldığında içeri soğuk bir rüzgar giriyor...",
            writingStyle = "rp",
            intensity = "normal",
            isPublic = false,
            isTemplate = true,
            pinnedMemory = if (isEnglish) "UNIVERSE ::: ELDORIA ::: Tensions rise between royal guards and rebel guilds." else "EVREN ::: ELDORİA ::: Kraliyet muhafızları ve asi loncaları arasında gerilim tırmanıyor."
        ),
        BotEntity(
            id = "preset_tokyo",
            mode = "universe",
            aiName = if (isEnglish) "Tokyo - Neon Future" else "Tokyo - Neon Gelecek",
            aiPersonality = if (isEnglish) "Tokyo Neon Narrator." else "Tokyo anlatıcısı.",
            scenario = if (isEnglish) "A futuristic sci-fi roleplay experience in 2077 Tokyo, focusing on the thin line between technology and humanity." else "2077 Tokyo'sunda teknoloji ve insanlık arasındaki ince çizgiye odaklanan fütüristik bir bilim kurgu rol yapma deneyimi.",
            universeName = if (isEnglish) "Tokyo - Neon Future" else "Tokyo - Neon Gelecek",
            keyCharactersJson = "[]",
            userCharName = if (isEnglish) "Runner" else "Runner",
            userCharDesc = if (isEnglish) "A mercenary from the cyber underworld." else "Siber suç dünyasından gelen bir paralı asker.",
            openingMessage = if (isEnglish) "As you glide through the neon-drenched streets of Shibuya under the acid rain..." else "Şibuya'nın asit yağmuru altındaki neon caddelerinde süzülürken siber-implantlarındaki sinyal bir anda kesilir...",
            writingStyle = "rp",
            intensity = "normal",
            isPublic = false,
            isTemplate = true,
            pinnedMemory = if (isEnglish) "UNIVERSE ::: TOKYO ::: 2077 futuristic technology city." else "EVREN ::: TOKYO ::: 2077 fütüristik teknoloji şehri."
        )
    )
}
