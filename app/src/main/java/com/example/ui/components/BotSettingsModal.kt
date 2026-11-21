@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.cosmicDialogEnter
import com.example.ui.animation.cosmicGlassSheen
import com.example.ui.animation.cosmicTapSpring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.AffectionEventEntity
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionState
import com.example.data.local.WorldState
import com.example.data.repository.KeyCharacter
import com.example.ui.screens.LocalAetheriaRootSize
import com.example.ui.screens.aetheriaBackdropBlur
import com.example.ui.screens.createFrostedWisteriaBackdropBitmap
import com.example.ui.screens.loadAetheriaRefGlassBitmaps
import com.example.ui.screens.morCamGlassSurface
import com.example.ui.screens.morCamOuterGlow
import com.example.ui.screens.morCamTopAndBottomSpecular
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.EmochiError
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun BotSettingsModal(
    bot: BotEntity,
    keyCharacters: List<KeyCharacter>,
    characterEmotions: List<CharacterEmotionEntity> = emptyList(),
    affectionEvents: List<AffectionEventEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (BotEntity, List<KeyCharacter>) -> Unit,
    onResetChat: (Boolean) -> Unit,
    onDeleteBot: () -> Unit,
    onCloneBot: (() -> Unit)? = null
) {
    BackHandler { onDismiss() }

    val isUniverse = bot.mode == "universe"
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val viewModel: com.example.ui.viewmodel.EmochiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    // ── Shared Wisteria Frosted Glass Backdrop & Reference Glass Plates ──
    val rootSize = LocalAetheriaRootSize.current.value
    val blurredBgBitmap = remember(context) { createSettingsDeepFrostedWisteriaBitmap(context) }
    val refGlassBitmaps = remember(context) { loadAetheriaRefGlassBitmaps(context) }

    // ── 1. Core Identity & Story State (100% Preserved) ──
    var aiName by remember(bot.id) { mutableStateOf(bot.aiName) }
    var universeName by remember(bot.id) { mutableStateOf(bot.universeName) }
    var personality by remember(bot.id) { mutableStateOf(bot.aiPersonality) }
    var scenario by remember(bot.id) { mutableStateOf(bot.scenario) }
    var openingMessage by remember(bot.id) { mutableStateOf(bot.openingMessage) }
    var userCharName by remember(bot.id) { mutableStateOf(bot.userCharName) }
    var userCharDesc by remember(bot.id) { mutableStateOf(bot.userCharDesc) }

    // ── 2. Narrative & Behavior Engine (100% Preserved) ──
    var writingStyle by remember(bot.id) {
        mutableStateOf(if (bot.writingStyle.equals("chat", ignoreCase = true)) "chat" else "rp")
    }
    var intensity by remember(bot.id) {
        mutableStateOf(if (bot.intensity.equals("intense", ignoreCase = true)) "intense" else "normal")
    }
    var customLength by remember(bot.id) {
        mutableStateOf(
            when (bot.customLength.lowercase()) {
                "short", "kisa" -> "short"
                "long", "uzun" -> "long"
                "standard", "orta" -> "standard"
                else -> "default"
            }
        )
    }
    var isNsfw by remember(bot.id) { mutableStateOf(bot.isNsfw) }
    var enableOoc by remember(bot.id) { mutableStateOf(bot.enableOoc) }
    var proactiveEnabled by remember(bot.id) { mutableStateOf(bot.proactiveEnabled) }
    var isPublic by remember(bot.id) { mutableStateOf(bot.isPublic) }

    // ── 3. Visuals (1:1 Proportional Avatar & Chat Wallpaper) ──
    var avatarUrl by remember(bot.id) { mutableStateOf(bot.avatarUrl) }
    var chatBgUrl by remember(bot.id) { mutableStateOf(bot.chatBgUrl) }

    // ── 4. Live Interactive Emotion & World Atmosphere State (100% Preserved) ──
    var localEmotionState by remember(bot.id, bot.emotionState) {
        mutableStateOf(EmotionState.fromJson(bot.emotionState))
    }
    var localWorldState by remember(bot.id, bot.worldAtmosphere) {
        mutableStateOf(WorldState.fromJson(bot.worldAtmosphere))
    }

    // ── 5. Memory & Story Notes State (All 3 Fields + Auto-Memories Preserved) ──
    var pinnedMemory by remember(bot.id) { mutableStateOf(bot.pinnedMemory) }
    var storyNotes by remember(bot.id) { mutableStateOf(bot.storyNotes) }
    var memoryNotes by remember(bot.id) { mutableStateOf(bot.memoryNotes) }

    // ── 6. Cast (Manual KeyCharacters + Auto-Detected Cast Preserved) ──
    var charList by remember(bot.id) { mutableStateOf(keyCharacters.toMutableList()) }
    val autoDetectedCast = remember { mutableStateOf<List<CastMemberEntity>>(emptyList()) }
    var deleteConfirmCast by remember { mutableStateOf<CastMemberEntity?>(null) }

    // ── 7. Clean Category & Sub-View Navigation State ──
    // 0 = Sahne, 1 = Duygu, 2 = Kimlik, 3 = Hafıza
    var activeTab by remember { mutableIntStateOf(0) }
    var showFineTuneNarrative by remember { mutableStateOf(false) }
    var showDeepEmotionSliders by remember { mutableStateOf(false) }
    var identitySubTab by remember { mutableIntStateOf(0) } // 0: Kişilik & Senaryo, 1: İlk Mesaj, 2: Senin Rolün, 3: Kadro
    var memorySubTab by remember { mutableIntStateOf(0) }   // 0: Sabit Hafıza, 1: Hikaye Notları, 2: Ek Notlar, 3: Otomatik

    var showResetAndDeleteSheet by remember { mutableStateOf(false) }
    var showBackupAndCloneSheet by remember { mutableStateOf(false) }
    var confirmResetChat by remember { mutableStateOf(false) }
    var confirmDeleteBot by remember { mutableStateOf(false) }
    var showNeuralVault by remember { mutableStateOf(false) }
    var showEmotionControlModal by remember { mutableStateOf(false) }
    var showSystemPromptSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val lastSystemPrompt by viewModel.lastSystemPrompt.collectAsState()
    val activeMessages by viewModel.activeMessages.collectAsState()
    var filterCount by remember { mutableIntStateOf(0) }

    val activeFactsFlow = remember(bot.id) { viewModel.getActiveFactsFlow(bot.id) }
    val activeFacts by activeFactsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val activeEventsFlow = remember(bot.id) { viewModel.getActiveEventsFlow(bot.id) }
    val activeEvents by activeEventsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(bot.id) {
        filterCount = viewModel.getContentFilterCountForBot(bot.id)
        autoDetectedCast.value = viewModel.getCastMembersForBot(bot.id)
            .filter { it.isAutoAdded && !it.isBlacklisted }
    }

    // Zero-permission Photo Picker -> 256x256 Micro-WebP (~15 KB Base64, EXIF + OOM safe)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { inputUri ->
            coroutineScope.launch {
                com.example.util.AvatarOptimizer.processAvatarToMicroWebPBase64(context, inputUri)
                    .onSuccess { microWebPBase64 ->
                        avatarUrl = microWebPBase64
                    }
                    .onFailure {
                        Toast.makeText(context, "Avatar işlenemedi.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Zero-permission Photo Picker -> 1080x2400 max RGB_565 WebP (%85) local wallpaper
    val bgPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { inputUri ->
            coroutineScope.launch {
                com.example.util.AvatarOptimizer.processBackgroundToWebPFile(context, inputUri)
                    .onSuccess { savedWebPUrl ->
                        chatBgUrl = savedWebPUrl
                    }
                    .onFailure {
                        Toast.makeText(context, "Arka plan görseli işlenemedi.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    fun buildUpdatedBotEntity(): BotEntity {
        val emotionJson = localEmotionState.toJson()
        val worldJson = localWorldState.toJson()
        return bot.copy(
            aiName = aiName.trim().ifBlank { bot.aiName },
            universeName = universeName.trim().ifBlank { bot.universeName },
            aiPersonality = personality,
            scenario = scenario,
            openingMessage = openingMessage,
            userCharName = userCharName.trim(),
            userCharDesc = userCharDesc.trim(),
            writingStyle = writingStyle,
            intensity = intensity,
            customLength = customLength,
            isNsfw = isNsfw,
            enableOoc = enableOoc,
            proactiveEnabled = proactiveEnabled,
            avatarUrl = avatarUrl,
            chatBgUrl = chatBgUrl,
            isPublic = isPublic,
            pinnedMemory = pinnedMemory.trim(),
            storyNotes = storyNotes.trim(),
            memoryNotes = memoryNotes.trim(),
            emotionState = emotionJson,
            previousEmotionState = emotionJson,
            worldAtmosphere = worldJson,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun saveAndDismiss() {
        val updated = buildUpdatedBotEntity()
        val cleanCast = charList.filter { it.name.isNotBlank() }
        onSave(updated, cleanCast)
        Toast.makeText(context, "Ayarlar kaydedildi ✨", Toast.LENGTH_SHORT).show()
        onDismiss()
    }

    val memoryCount = remember(pinnedMemory, storyNotes, memoryNotes, activeFacts.size, activeEvents.size) {
        val pinnedLines = pinnedMemory.lines().count { it.isNotBlank() }
        val storyLines = storyNotes.lines().count { it.isNotBlank() }
        val memLines = memoryNotes.lines().count { it.isNotBlank() }
        pinnedLines + storyLines + memLines + activeFacts.size + activeEvents.size
    }

    val displayTitle = if (isUniverse) universeName.ifBlank { aiName.ifBlank { "Evren" } }
    else aiName.ifBlank { universeName.ifBlank { "Karakter" } }

    val avatarInitial = remember(displayTitle) {
        displayTitle.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "A"
    }

    val selectedQuickPreset = remember(writingStyle, customLength, intensity) {
        when {
            writingStyle == "chat" && customLength == "short" -> "natural"
            writingStyle == "rp" && customLength == "long" && intensity == "intense" -> "intense"
            writingStyle == "rp" && (customLength == "standard" || customLength == "default") && intensity == "normal" -> "balanced"
            else -> "custom"
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ROOT FULL-SCREEN OVERLAY DIRECTLY OVER THE CHAT BACKGROUND (ZERO MESSAGES!)
    // ══════════════════════════════════════════════════════════════════════════
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Intercept touches so nothing leaks behind */ }
    ) {
        // Live preview if user picked a new custom wallpaper inside settings
        if (chatBgUrl != bot.chatBgUrl && chatBgUrl.isNotBlank() && chatBgUrl != "USE_AVATAR") {
            coil.compose.AsyncImage(
                model = chatBgUrl,
                contentDescription = "Yeni Arka Plan Önizlemesi",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.85f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
        ) {
            // ══════════════════════════════════════════════════════════════════════
            // 1. PURPOSE-BUILT MOR CAM GLASS SETTINGS HEADER
            //    Symmetrical circular glass orbs (< & Wallpaper) + 1:1 Cosmic Sigil Portrait + Filigree Title
            // ══════════════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Circular Glass Back Orb (<)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .morCamOuterGlow(cornerRadius = 21.dp, shadowAlpha = 0.36f, rimGlowAlpha = 0.28f, bottomFlareAlpha = 0.42f)
                        .clip(CircleShape)
                        .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.92f)
                        .morCamGlassSurface(
                            baseSmokeColor = Color(0x6615082A),
                            topRightFrostAlpha = 0.24f,
                            topLeftFrostAlpha = 0.18f,
                            topStarFrostAlpha = 0f,
                            bottomEdgeRefractionAlpha = 0.18f,
                            cornerRadius = 21.dp
                        )
                        .border(
                            BorderStroke(
                                1.1.dp,
                                Brush.linearGradient(listOf(Color(0xE8F3E8FF), Color(0x88A855F7), Color(0xD8E9D5FF)))
                            ),
                            CircleShape
                        )
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Geri",
                        tint = Color(0xFFFBF8FF),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Center Softly Frosted Banner Capsule with 1:1 Cosmic Sigil Medallion & Character Title
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .morCamOuterGlow(cornerRadius = 27.dp, shadowAlpha = 0.38f, rimGlowAlpha = 0.26f, bottomFlareAlpha = 0.44f)
                        .clip(RoundedCornerShape(50))
                        .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.92f)
                        .morCamGlassSurface(
                            baseSmokeColor = Color(0x6615082A),
                            topRightFrostAlpha = 0.24f,
                            topLeftFrostAlpha = 0.18f,
                            topStarFrostAlpha = 0f,
                            bottomEdgeRefractionAlpha = 0.20f,
                            cornerRadius = 27.dp
                        )
                        .border(
                            BorderStroke(
                                1.05.dp,
                                Brush.linearGradient(listOf(Color(0xE0F3E8FF), Color(0x78A855F7), Color(0xD0E9D5FF)))
                            ),
                            RoundedCornerShape(50)
                        )
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(start = 5.dp, end = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1:1 Cosmic Sigil Portrait Medallion (matches ChatScreen 1:1)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (refGlassBitmaps.sigilNoLetter.width > 1) {
                                drawImage(
                                    image = refGlassBitmaps.sigilNoLetter,
                                    dstOffset = IntOffset.Zero,
                                    dstSize = IntSize(
                                        size.width.roundToInt().coerceAtLeast(1),
                                        size.height.roundToInt().coerceAtLeast(1)
                                    ),
                                    filterQuality = FilterQuality.High
                                )
                            }
                        }
                        val fallbackInitial: @Composable () -> Unit = {
                            Text(
                                text = avatarInitial,
                                fontSize = 18.sp,
                                fontFamily = DisplaySerifFontFamily,
                                fontWeight = FontWeight.Normal,
                                style = TextStyle(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White, Color(0xFFF3E8FF), Color(0xFFD8B4FE))
                                    ),
                                    shadow = Shadow(
                                        color = Color(0xCCA855F7),
                                        offset = Offset.Zero,
                                        blurRadius = 8f
                                    )
                                )
                            )
                        }
                        if (avatarUrl.isNotBlank()) {
                            BotAvatarImage(
                                avatarUrl = avatarUrl,
                                contentDescription = displayTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(31.dp)
                                    .aspectRatio(1f)
                                    .clip(CircleShape),
                                fallback = fallbackInitial
                            )
                        } else {
                            fallbackInitial()
                        }
                    }

                    // Character Name + Delicate Filigree Subtitle
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = displayTitle,
                            color = Color(0xFFFBF8FF),
                            fontSize = if (displayTitle.length > 18) 14.5.sp else 16.sp,
                            fontFamily = LiterarySerifFontFamily,
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(0.7.dp)
                                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xAAD8B4FE))))
                            )
                            Text(
                                text = if (isUniverse) "✦ Evren Ayarları ✦" else "✦ Karakter Ayarları ✦",
                                color = Color(0xFFE9D5FF).copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontFamily = LiterarySerifFontFamily,
                                maxLines = 1
                            )
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(0.7.dp)
                                    .background(Brush.horizontalGradient(listOf(Color(0xAAD8B4FE), Color.Transparent)))
                            )
                        }
                    }
                }

                // Right Circular Glass Wallpaper Button (Symmetrical with Left Back Orb)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .morCamOuterGlow(cornerRadius = 21.dp, shadowAlpha = 0.36f, rimGlowAlpha = 0.28f, bottomFlareAlpha = 0.42f)
                        .clip(CircleShape)
                        .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.92f)
                        .morCamGlassSurface(
                            baseSmokeColor = if (chatBgUrl.isNotBlank()) Color(0x7A3B1468) else Color(0x6615082A),
                            topRightFrostAlpha = 0.24f,
                            topLeftFrostAlpha = 0.18f,
                            topStarFrostAlpha = 0f,
                            bottomEdgeRefractionAlpha = 0.18f,
                            cornerRadius = 21.dp
                        )
                        .border(
                            BorderStroke(
                                1.1.dp,
                                Brush.linearGradient(listOf(Color(0xE8F3E8FF), Color(0x88A855F7), Color(0xD8E9D5FF)))
                            ),
                            CircleShape
                        )
                        .clickable {
                            bgPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallpaper,
                        contentDescription = "Arka Plan Seç",
                        tint = Color(0xFFFBF8FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════════════
            // 2. UNIFIED FROSTED MOR CAM GLASS SEGMENTED NAVIGATION DOCK
            //    One continuous pill rail with glowing active capsule — calm & refined!
            // ══════════════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .height(42.dp)
                    .morCamOuterGlow(cornerRadius = 21.dp, shadowAlpha = 0.34f, rimGlowAlpha = 0.24f, bottomFlareAlpha = 0.38f)
                    .clip(RoundedCornerShape(50))
                    .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.94f)
                    .morCamGlassSurface(
                        baseSmokeColor = Color(0x6815082A),
                        topRightFrostAlpha = 0.22f,
                        topLeftFrostAlpha = 0.16f,
                        topStarFrostAlpha = 0f,
                        bottomEdgeRefractionAlpha = 0.18f,
                        cornerRadius = 21.dp
                    )
                    .border(
                        BorderStroke(
                            1.0.dp,
                            Brush.linearGradient(listOf(Color(0xD8F3E8FF), Color(0x78A855F7), Color(0xC8E9D5FF)))
                        ),
                        RoundedCornerShape(50)
                    )
                    .padding(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        0 to "Sahne",
                        1 to "Duygu",
                        2 to "Kimlik",
                        3 to "Hafıza"
                    )
                    tabs.forEach { (idx, label) ->
                        val isSelected = activeTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xAA6D28D9), Color(0x999333EA), Color(0xAA581C87))
                                                )
                                            )
                                            .border(
                                                BorderStroke(
                                                    1.05.dp,
                                                    Brush.linearGradient(listOf(Color.White, Color(0xFFE9D5FF), Color(0xFFA855F7)))
                                                ),
                                                RoundedCornerShape(50)
                                            )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { activeTab = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = LiterarySerifFontFamily,
                                color = if (isSelected) Color.White else Color(0xFFE9D5FF).copy(alpha = 0.84f),
                                fontSize = 12.8.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════════════
            // 3. CENTER FLOATING SEE-THROUGH MOR CAM GLASS CARD (With Top 4-Point Star ✦)
            //    Extends gracefully down to the bottom quick cards with deep frosted glass!
            // ══════════════════════════════════════════════════════════════════════
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val targetCardMinHeight = (maxHeight - 16.dp).coerceAtLeast(320.dp)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val motionConfig = LocalCosmicMotionConfig.current
                    MorCamStarGlassCard(
                        rootSize = rootSize,
                        blurredBitmap = blurredBgBitmap,
                        minCardHeight = targetCardMinHeight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .cosmicDialogEnter()
                    ) {
                        AnimatedContent(
                            targetState = activeTab,
                            transitionSpec = {
                                CosmicMotionSpecs.cosmicTabTransition(
                                    forward = targetState >= initialState,
                                    config = motionConfig
                                )
                            },
                            label = "botSettingsTabContent"
                        ) { tabIdx ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                        when (tabIdx) {
                            // ──────────────────────────────────────────────────────────
                            // TAB 0: SAHNE (Anlatım Akışı, İzinler & Görünüm)
                            // ──────────────────────────────────────────────────────────
                            0 -> {
                                MorCamCardHeader(
                                    title = "Sahne & Anlatım Akışı",
                                    badge = when (selectedQuickPreset) {
                                        "natural" -> "Doğal Sohbet"
                                        "intense" -> "Derin & Yoğun"
                                        "balanced" -> "Dengeli Roman"
                                        else -> "Özel Kurgu"
                                    }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Recessed Glass Segmented Track for the 3 Core Narrative Modes
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0x3D120624))
                                        .border(0.8.dp, Color(0x55C084FC), RoundedCornerShape(50))
                                        .padding(3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        "natural" to "Doğal Sohbet",
                                        "balanced" to "Dengeli Roman",
                                        "intense" to "Derin & Yoğun"
                                    ).forEach { (key, title) ->
                                        val isSel = selectedQuickPreset == key
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp)
                                                .clip(RoundedCornerShape(50))
                                                .then(
                                                    if (isSel) {
                                                        Modifier
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    listOf(Color(0x996D28D9), Color(0x889333EA), Color(0x99581C87))
                                                                )
                                                            )
                                                            .border(
                                                                1.dp,
                                                                Brush.linearGradient(listOf(Color.White, Color(0xFFE9D5FF), Color(0xFFA855F7))),
                                                                RoundedCornerShape(50)
                                                            )
                                                    } else Modifier
                                                )
                                                .clickable {
                                                    when (key) {
                                                        "natural" -> {
                                                            writingStyle = "chat"
                                                            customLength = "short"
                                                            intensity = "normal"
                                                        }
                                                        "balanced" -> {
                                                            writingStyle = "rp"
                                                            customLength = "standard"
                                                            intensity = "normal"
                                                        }
                                                        "intense" -> {
                                                            writingStyle = "rp"
                                                            customLength = "long"
                                                            intensity = "intense"
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = title,
                                                fontFamily = LiterarySerifFontFamily,
                                                color = if (isSel) Color.White else Color(0xFFE9D5FF).copy(alpha = 0.84f),
                                                fontSize = 11.6.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                            // Optional Fine-Tune Drawer (Üslup • Uzunluk • Yoğunluk)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showFineTuneNarrative = !showFineTuneNarrative }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✦ Üslup, Uzunluk ve Yoğunluğu Ayrı Seç",
                                    fontFamily = LiterarySerifFontFamily,
                                    color = Color(0xFFE9D5FF),
                                    fontSize = 12.5.sp
                                )
                                Icon(
                                    imageVector = if (showFineTuneNarrative) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFFD8B4FE),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = showFineTuneNarrative,
                                enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                                exit = fadeOut(tween(140)) + shrinkVertically(tween(140))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Yazım Üslubu",
                                        fontFamily = LiterarySerifFontFamily,
                                        color = Color(0xFFD8B4FE),
                                        fontSize = 11.5.sp
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        MorCamChoicePill(
                                            label = "Edebi Roman (RP)",
                                            isSelected = writingStyle == "rp",
                                            onClick = { writingStyle = "rp" },
                                            modifier = Modifier.weight(1f)
                                        )
                                        MorCamChoicePill(
                                            label = "Doğal Sohbet",
                                            isSelected = writingStyle == "chat",
                                            onClick = { writingStyle = "chat" },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Text(
                                        text = "Yanıt Uzunluğu",
                                        fontFamily = LiterarySerifFontFamily,
                                        color = Color(0xFFD8B4FE),
                                        fontSize = 11.5.sp
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(
                                            "default" to "Otomatik",
                                            "short" to "Kısa",
                                            "standard" to "Orta",
                                            "long" to "Uzun"
                                        ).forEach { (k, lbl) ->
                                            MorCamChoicePill(
                                                label = lbl,
                                                isSelected = customLength == k,
                                                onClick = { customLength = k },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Duygu & Atmosfer Yoğunluğu",
                                        fontFamily = LiterarySerifFontFamily,
                                        color = Color(0xFFD8B4FE),
                                        fontSize = 11.5.sp
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        MorCamChoicePill(
                                            label = "Dengeli Akış",
                                            isSelected = intensity == "normal",
                                            onClick = { intensity = "normal" },
                                            modifier = Modifier.weight(1f)
                                        )
                                        MorCamChoicePill(
                                            label = "Tutkulu & Yoğun",
                                            isSelected = intensity == "intense",
                                            onClick = { intensity = "intense" },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Unified Frosted Glass Tray for Scene Permissions (Clean hairlines instead of stacked boxes!)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0x36140729))
                                    .border(0.85.dp, Color(0x55C084FC), RoundedCornerShape(18.dp))
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                MorCamGlassToggleRow(
                                    title = "OOC Yönetmen Komutları",
                                    subtitle = "(...) parantez içi sahne yönlendirmeleri",
                                    checked = enableOoc,
                                    onCheckedChange = { enableOoc = it }
                                )
                                MorCamSubtleTrayDivider()
                                MorCamGlassToggleRow(
                                    title = "Sansürsüz Kurgu (+18)",
                                    subtitle = "Yetişkin ve karanlık romantizm temaları",
                                    checked = isNsfw,
                                    onCheckedChange = { isNsfw = it }
                                )
                                MorCamSubtleTrayDivider()
                                MorCamGlassToggleRow(
                                    title = "Proaktif Mesajlaşma",
                                    subtitle = "Karakter kendi isteğiyle mesaj yazabilir",
                                    checked = proactiveEnabled,
                                    onCheckedChange = { proactiveEnabled = it }
                                )
                                MorCamSubtleTrayDivider()
                                MorCamGlassToggleRow(
                                    title = if (isPublic) "Görünürlük: Herkese Açık" else "Görünürlük: Özel",
                                    subtitle = if (filterCount == 0) "Filtre Durumu: Kesintisiz / Temiz" else "Filtre Durumu: $filterCount içerik uyarısı",
                                    checked = isPublic,
                                    onCheckedChange = { isPublic = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Monochrome Lavender Vector Media Strip (Portre & Arka Plan)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MorCamIconChoicePill(
                                    icon = Icons.Default.PhotoCamera,
                                    label = if (avatarUrl.isBlank()) "Portre Seç" else "Portre Seçildi",
                                    isSelected = avatarUrl.isNotBlank(),
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                MorCamIconChoicePill(
                                    icon = Icons.Default.Wallpaper,
                                    label = if (chatBgUrl.isBlank()) "Arka Plan" else "Özel Arka Plan",
                                    isSelected = chatBgUrl.isNotBlank(),
                                    onClick = {
                                        bgPhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                if (avatarUrl.isNotBlank() || chatBgUrl.isNotBlank()) {
                                    MorCamIconChoicePill(
                                        icon = Icons.Default.Refresh,
                                        label = "Sıfırla",
                                        isSelected = false,
                                        onClick = {
                                            if (chatBgUrl.isNotBlank()) chatBgUrl = ""
                                            else avatarUrl = ""
                                        }
                                    )
                                }
                            }
                        }

                        // ──────────────────────────────────────────────────────────
                        // TAB 1: ✦ DUYGU (Ruh Hali, İlişki Bağları, Atmosfer & Stüdyo)
                        // ──────────────────────────────────────────────────────────
                        1 -> {
                            MorCamCardHeader(
                                title = "Ruh Hali & İlişki Bağları",
                                badge = "${localEmotionState.getMoodEmoji()} %${localEmotionState.affection}"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 8 Mood Chips in 2 Clean Rows of 4
                            val moodRows = listOf(
                                listOf(
                                    Triple("calm", "😊", "Sakin"),
                                    Triple("happy", "😄", "Mutlu"),
                                    Triple("romantic", "❤️", "Romantik"),
                                    Triple("excited", "🤩", "Heyecanlı")
                                ),
                                listOf(
                                    Triple("mysterious", "🕵️", "Gizemli"),
                                    Triple("angry", "😠", "Kızgın"),
                                    Triple("sad", "😢", "Üzgün"),
                                    Triple("shy", "😳", "Utangaç")
                                )
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                moodRows.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        rowItems.forEach { (moodKey, emoji, label) ->
                                            val isSelected = localEmotionState.mood.equals(moodKey, ignoreCase = true) ||
                                                localEmotionState.dominantEmotion.equals(label, ignoreCase = true)
                                            MorCamChoicePill(
                                                label = "$emoji $label",
                                                isSelected = isSelected,
                                                onClick = {
                                                    localEmotionState = localEmotionState.copy(
                                                        dominantEmotion = moodKey
                                                    )
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            MorCamHairlineDivider()

                            // Delicate Translucent Glass-Tube Sliders
                            MorCamGlassSlider(
                                label = "Sevgi & Yakınlık Bağı",
                                value = localEmotionState.affection,
                                accentColor = Color(0xFFD8B4FE),
                                onValueChange = {
                                    localEmotionState = localEmotionState.copy(
                                        relationshipAxes = localEmotionState.relationshipAxes.copy(affectionScore = it)
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            MorCamGlassSlider(
                                label = "Güven Seviyesi",
                                value = localEmotionState.trust,
                                accentColor = Color(0xFFC4B5FD),
                                onValueChange = {
                                    localEmotionState = localEmotionState.copy(
                                        primaryEmotions = localEmotionState.primaryEmotions.copy(trust = it)
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            MorCamGlassSlider(
                                label = "Fiziksel Rahatlık",
                                value = localEmotionState.physicalComfortScore,
                                accentColor = Color(0xFFA78BFA),
                                onValueChange = { localEmotionState = localEmotionState.copy(physicalComfortScore = it) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Expandable Deep Emotions & World Location/Time
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showDeepEmotionSliders = !showDeepEmotionSliders }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✦ Kırgınlık, Takıntı, Gerilim ve Mekan",
                                    fontFamily = LiterarySerifFontFamily,
                                    color = Color(0xFFE9D5FF),
                                    fontSize = 12.5.sp
                                )
                                Icon(
                                    imageVector = if (showDeepEmotionSliders) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFFD8B4FE),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = showDeepEmotionSliders,
                                enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                                exit = fadeOut(tween(140)) + shrinkVertically(tween(140))
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MorCamGlassSlider(
                                        label = "Kırgınlık & İncinme",
                                        value = localEmotionState.hurt,
                                        accentColor = Color(0xFFF472B6),
                                        onValueChange = {
                                            localEmotionState = localEmotionState.copy(
                                                relationshipAxes = localEmotionState.relationshipAxes.copy(resentmentScore = it)
                                            )
                                        }
                                    )
                                    MorCamGlassSlider(
                                        label = "Tutku & Takıntı",
                                        value = localEmotionState.obsession,
                                        accentColor = Color(0xFFC084FC),
                                        onValueChange = { localEmotionState = localEmotionState.copy(obsessionScore = it) }
                                    )
                                    MorCamGlassSlider(
                                        label = "Küresel Sahne Gerilimi",
                                        value = localWorldState.macro.globalTensionLevel,
                                        accentColor = Color(0xFFE879F9),
                                        onValueChange = {
                                            localWorldState = localWorldState.copy(
                                                macro = localWorldState.macro.copy(globalTensionLevel = it)
                                            )
                                        }
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        MorCamGlassTextField(
                                            value = localWorldState.meso.currentLocation,
                                            onValueChange = { loc ->
                                                localWorldState = localWorldState.copy(
                                                    meso = localWorldState.meso.copy(currentLocation = loc)
                                                )
                                            },
                                            label = "Mevcut Mekan",
                                            placeholder = "Örn: Katedral Terası",
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        MorCamGlassTextField(
                                            value = localWorldState.meso.timeOfDay,
                                            onValueChange = { tod ->
                                                localWorldState = localWorldState.copy(
                                                    meso = localWorldState.meso.copy(timeOfDay = tod)
                                                )
                                            },
                                            label = "Zaman Dilimi",
                                            placeholder = "Örn: Gece Yarısı",
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            MorCamHairlineDivider()

                            // Open Full Emotion Studio
                            MorCamChoicePill(
                                label = "✦ Gelişmiş Duygu & Yan Karakter Stüdyosunu Aç ›",
                                isSelected = true,
                                onClick = { showEmotionControlModal = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // ──────────────────────────────────────────────────────────
                        // TAB 2: ✦ KİMLİK (Kişilik, Senaryo, İlk Mesaj, Senin Rolün & Kadro)
                        // ──────────────────────────────────────────────────────────
                        2 -> {
                            val totalCast = charList.size + autoDetectedCast.value.size
                            MorCamCardHeader(
                                title = "Kimlik, Senaryo & Kadro",
                                badge = if (isUniverse) "Evren" else "Karakter"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 4 Clean Sub-Pills so only ONE editor is shown at a time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    0 to "Senaryo",
                                    1 to "İlk Mesaj",
                                    2 to "Rolün",
                                    3 to "Kadro ($totalCast)"
                                ).forEach { (idx, title) ->
                                    MorCamChoicePill(
                                        label = title,
                                        isSelected = identitySubTab == idx,
                                        onClick = { identitySubTab = idx },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            when (identitySubTab) {
                                0 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (isUniverse) {
                                            MorCamGlassTextField(
                                                value = universeName,
                                                onValueChange = { universeName = it },
                                                label = "Evren / Hikaye Adı",
                                                placeholder = "Örn: Aiden Blackwood Stories",
                                                singleLine = true
                                            )
                                        } else {
                                            MorCamGlassTextField(
                                                value = aiName,
                                                onValueChange = { aiName = it },
                                                label = "Karakter Adı",
                                                placeholder = "Örn: Aiden Blackwood",
                                                singleLine = true
                                            )
                                        }
                                        MorCamGlassTextField(
                                            value = personality,
                                            onValueChange = { personality = it },
                                            label = if (isUniverse) "Evren Kuralları & Anlatıcı Kişiliği" else "Karakter Kişiliği & Konuşma Tarzı",
                                            placeholder = "Karakterin geçmişi, tavırları, sırları ve konuşma biçimi...",
                                            minLines = 3,
                                            maxLines = 6
                                        )
                                        MorCamGlassTextField(
                                            value = scenario,
                                            onValueChange = { scenario = it },
                                            label = "Mevcut Sahne & Senaryo",
                                            placeholder = "Hikayenin geçtiği atmosfer ve olay örgüsü...",
                                            minLines = 3,
                                            maxLines = 6
                                        )
                                    }
                                }
                                1 -> {
                                    MorCamGlassTextField(
                                        value = openingMessage,
                                        onValueChange = { openingMessage = it },
                                        label = "İlk Karşılama / Açılış Mesajı",
                                        placeholder = "*Sahne açılırken karakterin söylediği ilk sözler...*",
                                        minLines = 5,
                                        maxLines = 9
                                    )
                                }
                                2 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        MorCamGlassTextField(
                                            value = userCharName,
                                            onValueChange = { userCharName = it },
                                            label = "Hikayedeki Adın / Hitap Şekli",
                                            placeholder = "Karakter sana nasıl seslensin?",
                                            singleLine = true
                                        )
                                        MorCamGlassTextField(
                                            value = userCharDesc,
                                            onValueChange = { userCharDesc = it },
                                            label = "Senin Rolün, Görünüşün & Geçmişin",
                                            placeholder = "Karakterle olan bağın, dış görünüşün ve hikayendeki yerin...",
                                            minLines = 4,
                                            maxLines = 7
                                        )
                                    }
                                }
                                3 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Hikaye Kadrosu (${charList.size} Manuel, ${autoDetectedCast.value.size} Otomatik)",
                                                fontFamily = LiterarySerifFontFamily,
                                                color = Color(0xFFE9D5FF),
                                                fontSize = 12.sp
                                            )
                                            MorCamChoicePill(
                                                label = "+ Karakter Ekle",
                                                isSelected = true,
                                                onClick = {
                                                    val nextList = charList.toMutableList()
                                                    nextList.add(KeyCharacter(name = "", desc = ""))
                                                    charList = nextList
                                                }
                                            )
                                        }

                                        charList.forEachIndexed { index, kc ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(Color(0x281A0B36))
                                                    .border(0.9.dp, Color(0x66C084FC), RoundedCornerShape(14.dp))
                                                    .padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    MorCamGlassTextField(
                                                        value = kc.name,
                                                        onValueChange = { newName ->
                                                            val updated = charList.toMutableList()
                                                            updated[index] = kc.copy(name = newName)
                                                            charList = updated
                                                        },
                                                        label = "Karakter Adı",
                                                        placeholder = "Örn: Emma",
                                                        singleLine = true,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0x33F87171))
                                                            .border(0.9.dp, Color(0x88F87171), CircleShape)
                                                            .clickable {
                                                                val updated = charList.toMutableList()
                                                                updated.removeAt(index)
                                                                charList = updated
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = "Sil",
                                                            tint = Color(0xFFFCA5A5),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                                MorCamGlassTextField(
                                                    value = kc.desc,
                                                    onValueChange = { newDesc ->
                                                        val updated = charList.toMutableList()
                                                        updated[index] = kc.copy(desc = newDesc)
                                                        charList = updated
                                                    },
                                                    label = "Rolü & Özellikleri",
                                                    placeholder = "Kişiliği ve hikayedeki rolü...",
                                                    minLines = 2,
                                                    maxLines = 3
                                                )
                                            }
                                        }

                                        if (autoDetectedCast.value.isNotEmpty()) {
                                            Text(
                                                text = "✦ Sohbetten Otomatik Algılanan Karakterler",
                                                fontFamily = LiterarySerifFontFamily,
                                                color = Color(0xFFD8B4FE),
                                                fontSize = 11.5.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                            autoDetectedCast.value.forEach { member ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0x24180A32))
                                                        .border(0.8.dp, Color(0x55A855F7), RoundedCornerShape(12.dp))
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "${member.name} (${member.role.ifBlank { "Yan Karakter" }})",
                                                            fontFamily = LiterarySerifFontFamily,
                                                            color = Color.White,
                                                            fontSize = 12.5.sp
                                                        )
                                                        if (member.description.isNotBlank()) {
                                                            Text(
                                                                text = member.description,
                                                                fontFamily = LiterarySerifFontFamily,
                                                                color = Color(0xFFD8B4FE),
                                                                fontSize = 11.sp,
                                                                maxLines = 2,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(30.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0x28F87171))
                                                            .clickable { deleteConfirmCast = member },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Close,
                                                            contentDescription = "Kaldır",
                                                            tint = Color(0xFFFCA5A5),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ──────────────────────────────────────────────────────────
                        // TAB 3: ✦ HAFIZA (Sabit Hafıza, Hikaye Notları, Ek Notlar & Neural Vault)
                        // ──────────────────────────────────────────────────────────
                        3 -> {
                            MorCamCardHeader(
                                title = "Kalıcı Hafıza & Neural Vault",
                                badge = "$memoryCount Kayıt"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Neural Vault Launch Pill
                            MorCamChoicePill(
                                label = "✦ Neural Vault Hafıza Merkezini Aç ›",
                                isSelected = true,
                                onClick = { showNeuralVault = true },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3 Clean Sub-Pills for Memory (Sabit, Hikaye, Anılar — Ek Not kaldırıldı)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    0 to "Sabit",
                                    1 to "Hikaye",
                                    2 to "Anılar (${activeEvents.size})"
                                ).forEach { (idx, title) ->
                                    MorCamChoicePill(
                                        label = title,
                                        isSelected = memorySubTab == idx,
                                        onClick = { memorySubTab = idx },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            when (memorySubTab) {
                                0 -> {
                                    MorCamGlassTextField(
                                        value = pinnedMemory,
                                        onValueChange = { pinnedMemory = it },
                                        label = "Sabitlenmiş Anılar (Asla Unutulmaz)",
                                        placeholder = "Örn: İlk öpüşmemiz yağmurlu bir gecede katedral balkonunda gerçekleşti...",
                                        minLines = 5,
                                        maxLines = 8
                                    )
                                }
                                1 -> {
                                    MorCamGlassTextField(
                                        value = storyNotes,
                                        onValueChange = { storyNotes = it },
                                        label = "Hikaye Özeti & Kurgu Notları",
                                        placeholder = "Şimdiye kadar yaşanan kritik olaylar ve sırlar...",
                                        minLines = 5,
                                        maxLines = 8
                                    )
                                }
                                else -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (activeFacts.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0x2A3B1470))
                                                    .border(0.8.dp, Color(0x66C084FC), RoundedCornerShape(10.dp))
                                                    .clickable { showNeuralVault = true }
                                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                            ) {
                                                Text(
                                                    text = "✦ Neural Vault'ta ${activeFacts.size} otomatik bilgi kayıtlı • Görmek için dokun ›",
                                                    fontFamily = InterFontFamily,
                                                    color = Color(0xFFE9D5FF),
                                                    fontSize = 11.2.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        if (activeEvents.isEmpty()) {
                                            Text(
                                                text = "Sadece gerçekten unutulmaz, duygusal ağırlığı yüksek anlar karakterlerin gözünden ve gizli iç sesleriyle burada birikir.",
                                                fontFamily = LiterarySerifFontFamily,
                                                fontStyle = FontStyle.Italic,
                                                color = Color(0xFFD8B4FE),
                                                fontSize = 12.sp,
                                                lineHeight = 17.sp
                                            )
                                        } else {
                                            activeEvents.take(8).forEach { ev ->
                                                val living = com.example.data.repository.LivingMemoryCodec.parse(ev.description, ev.importanceScore)
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0x281A0B36))
                                                        .border(0.85.dp, Color(0x66A855F7), RoundedCornerShape(12.dp))
                                                        .padding(horizontal = 11.dp, vertical = 9.dp),
                                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    if (living.isStructured) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = "✦ ${living.povCharacter}'ın Gözünden",
                                                                fontFamily = InterFontFamily,
                                                                color = Color(0xFFD8B4FE),
                                                                fontSize = 10.5.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            Text(
                                                                text = "★ ${living.salience}/10" + if (living.emotionTag.isNotBlank()) " • ${living.emotionTag}" else "",
                                                                fontFamily = InterFontFamily,
                                                                color = Color(0xFFFDE68A),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                        if (living.title.isNotBlank()) {
                                                            Text(
                                                                text = living.title,
                                                                fontFamily = LiterarySerifFontFamily,
                                                                color = Color(0xFFFAF5FF),
                                                                fontSize = 12.8.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = living.narrative,
                                                        fontFamily = LiterarySerifFontFamily,
                                                        color = Color(0xFFEDE9FE),
                                                        fontSize = 11.8.sp,
                                                        lineHeight = 16.5.sp
                                                    )
                                                    if (living.innerThought.isNotBlank()) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color(0x286D28D9))
                                                                .border(0.6.dp, Color(0x55C4B5FD), RoundedCornerShape(8.dp))
                                                                .padding(horizontal = 9.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(
                                                                text = "💭 İç Ses: \"${living.innerThought}\"",
                                                                fontFamily = LiterarySerifFontFamily,
                                                                fontStyle = FontStyle.Italic,
                                                                color = Color(0xFFE9D5FF),
                                                                fontSize = 11.2.sp,
                                                                lineHeight = 15.5.sp
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
                        }
                        } // end AnimatedContent
                    }
                }
            }
            }

            // ══════════════════════════════════════════════════════════════════════
            // 4. BOTTOM 3 QUICK-ACTION GLASS CARDS (Monochrome Luminous Lavender Vectors!)
            // ══════════════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MorCamBottomQuickCard(
                    title = "Sistem Prompt",
                    subtitle = "Canlı yönerge",
                    icon = Icons.Default.AutoAwesome,
                    rootSize = rootSize,
                    blurredBitmap = blurredBgBitmap,
                    onClick = { showSystemPromptSheet = true },
                    modifier = Modifier.weight(1f)
                )
                MorCamBottomQuickCard(
                    title = "Yedek & Klon",
                    subtitle = "Dışa aktar",
                    icon = Icons.Default.Share,
                    rootSize = rootSize,
                    blurredBitmap = blurredBgBitmap,
                    onClick = { showBackupAndCloneSheet = true },
                    modifier = Modifier.weight(1f)
                )
                MorCamBottomQuickCard(
                    title = "Sıfırla & Sil",
                    subtitle = "Sahneyi yenile",
                    icon = Icons.Default.Refresh,
                    rootSize = rootSize,
                    blurredBitmap = blurredBgBitmap,
                    onClick = { showResetAndDeleteSheet = true },
                    modifier = Modifier.weight(1f)
                )
            }

            // ══════════════════════════════════════════════════════════════════════
            // 5. BOTTOM FLOATING GLASS SAVE BAR
            //    Refined symmetrical lavender filigree capsule + glowing amethyst confirm orb
            // ══════════════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Circular Glass Cancel Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .morCamOuterGlow(cornerRadius = 23.dp, shadowAlpha = 0.38f, rimGlowAlpha = 0.30f, bottomFlareAlpha = 0.48f)
                        .clip(CircleShape)
                        .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.94f)
                        .morCamGlassSurface(
                            baseSmokeColor = Color(0x6E15082A),
                            topRightFrostAlpha = 0.26f,
                            topLeftFrostAlpha = 0.20f,
                            topStarFrostAlpha = 0f,
                            bottomEdgeRefractionAlpha = 0.20f,
                            cornerRadius = 23.dp
                        )
                        .border(
                            BorderStroke(
                                1.15.dp,
                                Brush.linearGradient(
                                    listOf(Color(0xE8F3E8FF), Color(0x98A855F7), Color(0xD8E9D5FF))
                                )
                            ),
                            CircleShape
                        )
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "İptal",
                        tint = Color(0xFFFBF8FF),
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Center Frosted Glass Capsule ("✦  Değişiklikleri Kaydet  ✦")
                val saveCapsuleInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .cosmicTapSpring(
                            interactionSource = saveCapsuleInteraction,
                            pressedScale = 0.955f,
                            cornerRadius = 23.dp
                        )
                        .morCamOuterGlow(cornerRadius = 23.dp, shadowAlpha = 0.38f, rimGlowAlpha = 0.30f, bottomFlareAlpha = 0.54f)
                        .clip(RoundedCornerShape(50))
                        .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.94f)
                        .morCamGlassSurface(
                            baseSmokeColor = Color(0x741E0A3C),
                            topRightFrostAlpha = 0.28f,
                            topLeftFrostAlpha = 0.20f,
                            topStarFrostAlpha = 0f,
                            bottomEdgeRefractionAlpha = 0.22f,
                            cornerRadius = 23.dp
                        )
                        .cosmicGlassSheen(cornerRadius = 23.dp, periodMs = 4900, maxAlpha = 0.16f)
                        .border(
                            BorderStroke(
                                1.2.dp,
                                Brush.linearGradient(
                                    listOf(Color(0xEEF3E8FF), Color(0x98A855F7), Color(0x587C3AED), Color(0xD8E9D5FF))
                                )
                            ),
                            RoundedCornerShape(50)
                        )
                        .morCamTopAndBottomSpecular(cornerRadius = 23.dp)
                        .clickable(
                            interactionSource = saveCapsuleInteraction,
                            indication = null
                        ) { saveAndDismiss() }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✦",
                            color = Color(0xFFD8B4FE),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Değişiklikleri Kaydet",
                            fontFamily = LiterarySerifFontFamily,
                            color = Color(0xFFFBF8FF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "✦",
                            color = Color(0xFFD8B4FE),
                            fontSize = 11.sp
                        )
                    }
                }

                // Right Glowing Amethyst-Violet Send/Save Orb (matches the neon purple orb in Reference Image!)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .drawBehind {
                            drawIntoCanvas { canvas ->
                                val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                    style = android.graphics.Paint.Style.FILL
                                    color = Color(0xFFA855F7).copy(alpha = 0.45f).toArgb()
                                    maskFilter = android.graphics.BlurMaskFilter(
                                        8.dp.toPx(),
                                        android.graphics.BlurMaskFilter.Blur.NORMAL
                                    )
                                }
                                canvas.nativeCanvas.drawCircle(
                                    size.width / 2f,
                                    size.height / 2f,
                                    size.minDimension / 2f,
                                    glowPaint
                                )
                            }
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFB366FF),
                                    Color(0xFF7E22CE),
                                    Color(0xFF581C87)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.4.dp,
                                Brush.linearGradient(
                                    listOf(Color.White, Color(0xFFE9D5FF), Color(0xFFC084FC))
                                )
                            ),
                            CircleShape
                        )
                        .clickable { saveAndDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Kaydet",
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUB-MODALS & DIALOGS (All 100% Preserved & Connected!)
    // ══════════════════════════════════════════════════════════════════════════

    // A. Backup & Clone Quick Dialog
    if (showBackupAndCloneSheet) {
        MorCamDialogShell(
            onDismissRequest = { showBackupAndCloneSheet = false },
            title = "Yedekle & Klonla",
            subtitle = "Sohbet kaydını dışa aktar veya kopya oluştur",
            icon = Icons.Default.Share
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MorCamOptionRow(
                    title = "Sohbet Geçmişini Dışa Aktar",
                    subtitle = ".txt veya .json formatında kaydet ve paylaş",
                    icon = Icons.Default.Share,
                    selected = true,
                    onClick = {
                        showBackupAndCloneSheet = false
                        showExportDialog = true
                    }
                )
                if (onCloneBot != null) {
                    MorCamOptionRow(
                        title = "Karakterin / Evrenin Kopyasını Oluştur",
                        subtitle = "Aynı kişilik ve sahne ayarlarıyla yeni kopya aç",
                        icon = Icons.Default.AutoAwesome,
                        selected = false,
                        onClick = {
                            showBackupAndCloneSheet = false
                            onCloneBot()
                            Toast.makeText(context, "Kopya oluşturuldu ✨", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                MorCamCapsuleButton(
                    text = "Kapat",
                    onClick = { showBackupAndCloneSheet = false },
                    variant = MorCamButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // B. Reset & Delete Quick Dialog
    if (showResetAndDeleteSheet) {
        MorCamDialogShell(
            onDismissRequest = { showResetAndDeleteSheet = false },
            title = "Sahneyi Sıfırla veya Sil",
            subtitle = "Sohbet akışını yenile veya bu kaydı kaldır",
            icon = Icons.Default.Refresh
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MorCamOptionRow(
                    title = "Sohbeti Sıfırla",
                    subtitle = "Hafıza ve ilişki bağları korunur, yeni sahne başlar",
                    icon = Icons.Default.Refresh,
                    selected = true,
                    onClick = {
                        showResetAndDeleteSheet = false
                        onResetChat(false)
                        Toast.makeText(context, "Sohbet sıfırlandı (Hafıza korundu)", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )
                MorCamOptionRow(
                    title = "Tam Sıfırlama",
                    subtitle = "Mesajlar, otomatik hafıza ve duygu durumu ilk haline döner",
                    icon = Icons.Default.Refresh,
                    selected = false,
                    onClick = {
                        showResetAndDeleteSheet = false
                        confirmResetChat = true
                    }
                )
                MorCamOptionRow(
                    title = "Karakteri / Evreni Kalıcı Sil",
                    subtitle = "Tüm sohbet geçmişiyle birlikte tamamen kaldırılır",
                    icon = Icons.Default.Delete,
                    destructive = true,
                    onClick = {
                        showResetAndDeleteSheet = false
                        confirmDeleteBot = true
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                MorCamCapsuleButton(
                    text = "İptal",
                    onClick = { showResetAndDeleteSheet = false },
                    variant = MorCamButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // C. Confirm Full Reset Dialog
    if (confirmResetChat) {
        MorCamDialogShell(
            onDismissRequest = { confirmResetChat = false },
            title = "Tam Sıfırlama Onayı",
            subtitle = "Bu işlem mevcut ilerlemeyi başa alır",
            icon = Icons.Default.Refresh
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Tüm mesajlar, otomatik anılar ve ilişki bağları ilk haline sıfırlanacak. Devam etmek istediğinizden emin misiniz?",
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
                        text = "Vazgeç",
                        onClick = { confirmResetChat = false },
                        variant = MorCamButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MorCamCapsuleButton(
                        text = "Tamamen Sıfırla",
                        onClick = {
                            confirmResetChat = false
                            onResetChat(true)
                            Toast.makeText(context, "Hikaye tamamen sıfırlandı", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        variant = MorCamButtonVariant.Destructive,
                        modifier = Modifier.weight(1.25f)
                    )
                }
            }
        }
    }

    // D. Confirm Delete Bot Dialog
    if (confirmDeleteBot) {
        MorCamDialogShell(
            onDismissRequest = { confirmDeleteBot = false },
            title = "Kalıcı Silme Onayı",
            subtitle = "Geri alınamayan işlem",
            icon = Icons.Default.Delete
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "'$displayTitle' ve tüm sohbet geçmişi kalıcı olarak silinecek. Bu işlem geri alınamaz.",
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
                        text = "Vazgeç",
                        onClick = { confirmDeleteBot = false },
                        variant = MorCamButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MorCamCapsuleButton(
                        text = "Kalıcı Sil",
                        onClick = {
                            confirmDeleteBot = false
                            onDeleteBot()
                            onDismiss()
                        },
                        variant = MorCamButtonVariant.Destructive,
                        icon = Icons.Default.Delete,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }
    }

    // 1. Delete Auto-Detected Cast Member Confirmation
    deleteConfirmCast?.let { member ->
        MorCamDialogShell(
            onDismissRequest = { deleteConfirmCast = null },
            title = "Karakteri Kaldır",
            subtitle = "Otomatik algılanan yan karakter",
            icon = Icons.Default.Delete
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "'${member.name}' hikaye kadrosundan kaldırılsın mı?",
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
                        onClick = { deleteConfirmCast = null },
                        variant = MorCamButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MorCamCapsuleButton(
                        text = "Kaldır",
                        onClick = {
                            viewModel.deleteAutoAddedCastMember(bot.id, member.id, member.name, true)
                            autoDetectedCast.value = autoDetectedCast.value.filter { it.id != member.id }
                            deleteConfirmCast = null
                        },
                        variant = MorCamButtonVariant.Destructive,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // 2. Neural Vault Memory Management Modal
    if (showNeuralVault) {
        val currentDraft = remember(
            aiName, universeName, personality, scenario, openingMessage,
            userCharName, userCharDesc, writingStyle, intensity, customLength,
            isNsfw, enableOoc, proactiveEnabled, pinnedMemory, storyNotes, memoryNotes,
            localEmotionState, localWorldState
        ) {
            buildUpdatedBotEntity()
        }
        NeuralVaultModal(
            bot = currentDraft,
            viewModel = viewModel,
            onDismiss = { showNeuralVault = false },
            onSaveMemory = { updatedBot ->
                pinnedMemory = updatedBot.pinnedMemory
                memoryNotes = updatedBot.memoryNotes
                storyNotes = updatedBot.storyNotes
            }
        )
    }

    // 3. Advanced Character & Side-Character Emotion Control Studio Modal
    if (showEmotionControlModal) {
        val currentDraft = remember(localEmotionState) { buildUpdatedBotEntity() }
        CharacterEmotionControlModal(
            bot = currentDraft,
            characterEmotions = characterEmotions,
            keyCharacters = charList,
            onDismiss = { showEmotionControlModal = false },
            onSaveBotEmotion = { newEmotionJson ->
                localEmotionState = EmotionState.fromJson(newEmotionJson)
            },
            onSaveCharacterEmotionState = { charName, charEmotionJson ->
                viewModel.updateCharacterEmotionState(bot.id, charName, charEmotionJson)
            }
        )
    }

    // 4. Live System Prompt Inspector BottomSheet
    if (showSystemPromptSheet) {
        val previewPrompt: String = remember(
            lastSystemPrompt, aiName, universeName, personality, scenario,
            userCharName, userCharDesc, writingStyle, customLength, intensity,
            isNsfw, enableOoc, pinnedMemory, storyNotes, memoryNotes,
            localEmotionState, localWorldState
        ) {
            val live = lastSystemPrompt
            if (!live.isNullOrBlank()) {
                live
            } else {
                buildSystemPromptPreview(
                    bot = buildUpdatedBotEntity(),
                    activeFactsCount = activeFacts.size,
                    activeEventsCount = activeEvents.size
                )
            }
        }

        MorCamBottomSheetShell(
            onDismissRequest = { showSystemPromptSheet = false },
            title = "Canlı Sistem Promptu",
            subtitle = "Karakter, duygu, hafıza ve üslup yönergelerinin tam hali",
            icon = Icons.Default.AutoAwesome,
            trailingHeaderContent = {
                MorCamChipPill(
                    label = "Kopyala",
                    selected = true,
                    onClick = {
                        clipboardManager.setText(AnnotatedString(previewPrompt))
                        Toast.makeText(context, "Sistem promptu kopyalandı", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        ) {
            MorCamGlassCard(
                cornerRadius = 18.dp,
                paddingHorizontal = 12.dp,
                paddingVertical = 12.dp
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    item {
                        Text(
                            text = previewPrompt,
                            color = MorCamTokens.LavenderLight,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.5.sp
                        )
                    }
                }
            }
        }
    }

    // 5. Chat Export Dialog (.txt / .json + Date Filter + Share / Copy)
    if (showExportDialog) {
        var exportFormat by remember { mutableStateOf("txt") }
        var dateRangeFilter by remember { mutableStateOf("all") }

        val now = System.currentTimeMillis()
        val filteredMessages = remember(activeMessages, dateRangeFilter) {
            val cutoff = when (dateRangeFilter) {
                "today" -> now - 24L * 3600L * 1000L
                "week" -> now - 7L * 24L * 3600L * 1000L
                "month" -> now - 30L * 24L * 3600L * 1000L
                else -> 0L
            }
            activeMessages.filter { it.timestamp >= cutoff }
        }

        fun formatExportContent(): String {
            return if (exportFormat == "json") {
                val root = org.json.JSONObject()
                root.put("botId", bot.id)
                root.put("botName", displayTitle)
                root.put("exportedAt", System.currentTimeMillis())
                val arr = org.json.JSONArray()
                filteredMessages.forEach { msg ->
                    val o = org.json.JSONObject()
                    o.put("role", msg.role)
                    o.put("text", msg.text)
                    o.put("timestamp", msg.timestamp)
                    arr.put(o)
                }
                root.put("messages", arr)
                root.toString(2)
            } else {
                val sb = StringBuilder()
                sb.appendLine("=== $displayTitle — Sohbet Kaydı (${filteredMessages.size} Mesaj) ===")
                sb.appendLine()
                val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                filteredMessages.forEach { msg ->
                    val who = if (msg.role == "user") (userCharName.ifBlank { "Sen" }) else displayTitle
                    sb.appendLine("[${sdf.format(java.util.Date(msg.timestamp))}] $who:")
                    sb.appendLine(msg.text)
                    sb.appendLine()
                }
                sb.toString()
            }
        }

        MorCamDialogShell(
            onDismissRequest = { showExportDialog = false },
            title = "Sohbeti Dışa Aktar",
            subtitle = "${filteredMessages.size} mesaj seçili aralıkta hazır",
            icon = Icons.Default.Share
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Dosya Formatı",
                    color = MorCamTokens.LavenderSoft,
                    fontSize = 12.sp,
                    fontFamily = LiterarySerifFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("txt" to "Metin (.txt)", "json" to "Veri (.json)").forEach { (fmt, label) ->
                        MorCamChipPill(
                            label = label,
                            selected = exportFormat == fmt,
                            onClick = { exportFormat = fmt },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = "Tarih Aralığı",
                    color = MorCamTokens.LavenderSoft,
                    fontSize = 12.sp,
                    fontFamily = LiterarySerifFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "all" to "Tümü",
                        "today" to "24 Saat",
                        "week" to "1 Hafta",
                        "month" to "1 Ay"
                    ).forEach { (rng, label) ->
                        MorCamChipPill(
                            label = label,
                            selected = dateRangeFilter == rng,
                            onClick = { dateRangeFilter = rng }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MorCamCapsuleButton(
                        text = "Kopyala",
                        onClick = {
                            clipboardManager.setText(AnnotatedString(formatExportContent()))
                            Toast.makeText(context, "Sohbet panoya kopyalandı", Toast.LENGTH_SHORT).show()
                            showExportDialog = false
                        },
                        variant = MorCamButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MorCamCapsuleButton(
                        text = "Paylaş",
                        onClick = {
                            val content = formatExportContent()
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, content)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Sohbeti Paylaş"))
                            showExportDialog = false
                        },
                        variant = MorCamButtonVariant.Primary,
                        icon = Icons.Default.Share,
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// TRUE SEE-THROUGH MOR CAM GLASS BUILDING BLOCKS
// Styled 1:1 after "Mor Camda Aiden Blackwood Hikâyesi.png"
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Deeply frosted wisteria bitmap (3-pass radius-12 separable blur) so glass panels
 * completely melt sharp cathedral edges behind them while preserving rich purple light refraction.
 */
private var cachedSettingsDeepFrostedBitmap: ImageBitmap? = null

private fun createSettingsDeepFrostedWisteriaBitmap(context: android.content.Context): ImageBitmap {
    cachedSettingsDeepFrostedBitmap?.let { return it }
    return try {
        val opts = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = 2
            inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
        }
        val raw = android.graphics.BitmapFactory.decodeResource(
            context.resources,
            R.drawable.img_chat_bg_aiden_wisteria,
            opts
        ) ?: return createFrostedWisteriaBackdropBitmap(context)

        val w = 320
        val h = ((raw.height.toFloat() / raw.width.toFloat().coerceAtLeast(1f)) * w).toInt().coerceIn(320, 960)
        val scaled = android.graphics.Bitmap.createScaledBitmap(raw, w, h, true)
        if (raw !== scaled) raw.recycle()

        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        val tmp = IntArray(w * h)

        val radius = 12
        val div = radius * 2 + 1
        repeat(3) {
            for (y in 0 until h) {
                val rowOffset = y * w
                var rSum = 0
                var gSum = 0
                var bSum = 0
                for (dx in -radius..radius) {
                    val px = pixels[rowOffset + dx.coerceIn(0, w - 1)]
                    rSum += (px ushr 16) and 0xFF
                    gSum += (px ushr 8) and 0xFF
                    bSum += px and 0xFF
                }
                for (x in 0 until w) {
                    tmp[rowOffset + x] = (0xFF shl 24) or ((rSum / div) shl 16) or ((gSum / div) shl 8) or (bSum / div)
                    val nextX = (x + radius + 1).coerceAtMost(w - 1)
                    val prevX = (x - radius).coerceAtLeast(0)
                    val nextPx = pixels[rowOffset + nextX]
                    val prevPx = pixels[rowOffset + prevX]
                    rSum += ((nextPx ushr 16) and 0xFF) - ((prevPx ushr 16) and 0xFF)
                    gSum += ((nextPx ushr 8) and 0xFF) - ((prevPx ushr 8) and 0xFF)
                    bSum += (nextPx and 0xFF) - (prevPx and 0xFF)
                }
            }
            for (x in 0 until w) {
                var rSum = 0
                var gSum = 0
                var bSum = 0
                for (dy in -radius..radius) {
                    val px = tmp[dy.coerceIn(0, h - 1) * w + x]
                    rSum += (px ushr 16) and 0xFF
                    gSum += (px ushr 8) and 0xFF
                    bSum += px and 0xFF
                }
                for (y in 0 until h) {
                    pixels[y * w + x] = (0xFF shl 24) or ((rSum / div) shl 16) or ((gSum / div) shl 8) or (bSum / div)
                    val nextY = (y + radius + 1).coerceAtMost(h - 1)
                    val prevY = (y - radius).coerceAtLeast(0)
                    val nextPx = tmp[nextY * w + x]
                    val prevPx = tmp[prevY * w + x]
                    rSum += ((nextPx ushr 16) and 0xFF) - ((prevPx ushr 16) and 0xFF)
                    gSum += ((nextPx ushr 8) and 0xFF) - ((prevPx ushr 8) and 0xFF)
                    bSum += (nextPx and 0xFF) - (prevPx and 0xFF)
                }
            }
        }

        val out = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.RGB_565)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        scaled.recycle()
        out.asImageBitmap().also { cachedSettingsDeepFrostedBitmap = it }
    } catch (_: Throwable) {
        createFrostedWisteriaBackdropBitmap(context)
    }
}

/**
 * Main Floating Mor Cam Glass Card with the signature top-center 4-point star (✦)
 * on the lavender rim border and rich frosted wisteria glass interior that never clashes with background lines.
 */
@Composable
private fun MorCamStarGlassCard(
    rootSize: IntSize,
    blurredBitmap: ImageBitmap,
    minCardHeight: Dp = 420.dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .padding(top = 6.dp)
    ) {
        // Glass Box Body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minCardHeight)
                .morCamOuterGlow(
                    cornerRadius = 24.dp,
                    shadowAlpha = 0.48f,
                    rimGlowAlpha = 0.34f,
                    bottomFlareAlpha = 0.56f
                )
                .clip(RoundedCornerShape(24.dp))
                .aetheriaBackdropBlur(rootSize, blurredBitmap, alpha = 0.96f)
                .morCamGlassSurface(
                    baseSmokeColor = Color(0x7415082A),
                    topRightFrostAlpha = 0.36f,
                    topLeftFrostAlpha = 0.24f,
                    topStarFrostAlpha = 0.28f,
                    bottomEdgeRefractionAlpha = 0.24f,
                    cornerRadius = 24.dp
                )
                .border(
                    BorderStroke(
                        1.25.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xF0F3E8FF),
                                Color(0xAAA855F7),
                                Color(0x687C3AED),
                                Color(0xE0E9D5FF)
                            )
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
                .morCamTopAndBottomSpecular(cornerRadius = 24.dp)
                .animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing))
                .padding(horizontal = 17.dp, vertical = 18.dp),
            content = content
        )

        // Signature 4-Point Glowing Star (✦) Centered on the Top Rim Border (1:1 with Reference Bot Glass Box!)
        Canvas(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-11).dp)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rLong = size.minDimension * 0.48f
            val rShort = size.minDimension * 0.11f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xCCE9D5FF),
                        Color(0x55A855F7),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = rLong
                ),
                radius = rLong,
                center = Offset(cx, cy)
            )

            val starPath = Path().apply {
                moveTo(cx, cy - rLong)
                quadraticTo(cx + rShort * 0.45f, cy - rShort * 0.45f, cx + rLong, cy)
                quadraticTo(cx + rShort * 0.45f, cy + rShort * 0.45f, cx, cy + rLong)
                quadraticTo(cx - rShort * 0.45f, cy + rShort * 0.45f, cx - rLong, cy)
                quadraticTo(cx - rShort * 0.45f, cy - rShort * 0.45f, cx, cy - rLong)
                close()
            }
            drawPath(
                path = starPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF3E8FF),
                        Color(0xFFC084FC)
                    ),
                    center = Offset(cx, cy),
                    radius = rLong
                )
            )
        }
    }
}

/**
 * Bottom 3 Quick-Action Glass Cards with monochrome luminous lavender vector icons.
 */
@Composable
private fun MorCamBottomQuickCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    rootSize: IntSize,
    blurredBitmap: ImageBitmap,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickCardInteraction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(56.dp)
            .cosmicTapSpring(
                interactionSource = quickCardInteraction,
                pressedScale = 0.94f,
                cornerRadius = 16.dp
            )
            .morCamOuterGlow(cornerRadius = 16.dp, shadowAlpha = 0.36f, rimGlowAlpha = 0.28f, bottomFlareAlpha = 0.44f)
            .clip(RoundedCornerShape(16.dp))
            .aetheriaBackdropBlur(rootSize, blurredBitmap, alpha = 0.94f)
            .morCamGlassSurface(
                baseSmokeColor = Color(0x6C15082A),
                topRightFrostAlpha = 0.26f,
                topLeftFrostAlpha = 0.18f,
                topStarFrostAlpha = 0f,
                bottomEdgeRefractionAlpha = 0.20f,
                cornerRadius = 16.dp
            )
            .border(
                BorderStroke(
                    1.05.dp,
                    Brush.linearGradient(
                        listOf(Color(0xE8F3E8FF), Color(0x88A855F7), Color(0xD8E9D5FF))
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .morCamTopAndBottomSpecular(cornerRadius = 16.dp)
            .clickable(
                interactionSource = quickCardInteraction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 7.dp, vertical = 7.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(Color(0x667C3AED), Color(0x442E1065)))
                    )
                    .border(0.85.dp, Color(0xCCE9D5FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFFF3E8FF),
                    modifier = Modifier.size(14.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = LiterarySerifFontFamily,
                    color = Color(0xFFFBF8FF),
                    fontSize = 10.6.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontFamily = LiterarySerifFontFamily,
                    color = Color(0xFFD8B4FE).copy(alpha = 0.88f),
                    fontSize = 9.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MorCamCardHeader(
    title: String,
    badge: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = LiterarySerifFontFamily,
            color = Color(0xFFFBF8FF),
            fontSize = 16.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0xCC120524),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 6f
                )
            )
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0x523B1468))
                .border(0.95.dp, Color(0xCCE9D5FF), RoundedCornerShape(50))
                .padding(horizontal = 11.dp, vertical = 4.dp)
        ) {
            Text(
                text = badge,
                fontFamily = LiterarySerifFontFamily,
                color = Color(0xFFF3E8FF),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun MorCamIconChoicePill(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(listOf(Color(0x88581C87), Color(0x783B0764), Color(0x884C1D95)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0x461A0B36), Color(0x3C120726)))
                }
            )
            .border(
                BorderStroke(
                    if (isSelected) 1.15.dp else 0.9.dp,
                    Brush.linearGradient(
                        if (isSelected) listOf(Color.White, Color(0xFFE9D5FF), Color(0xFFA855F7))
                        else listOf(Color(0x99D8B4FE), Color(0x557C3AED))
                    )
                ),
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else Color(0xFFE9D5FF),
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = label,
            fontFamily = LiterarySerifFontFamily,
            color = if (isSelected) Color.White else Color(0xFFF3E8FF),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MorCamSubtleTrayDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.6.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, Color(0x55D8B4FE), Color(0x77E9D5FF), Color(0x55D8B4FE), Color.Transparent)
                )
            )
    )
}

@Composable
private fun MorCamChoicePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(
                        listOf(Color(0x88581C87), Color(0x783B0764), Color(0x884C1D95))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0x461A0B36), Color(0x3C120726))
                    )
                }
            )
            .border(
                BorderStroke(
                    if (isSelected) 1.2.dp else 0.9.dp,
                    Brush.linearGradient(
                        if (isSelected) {
                            listOf(Color.White, Color(0xFFE9D5FF), Color(0xFFA855F7))
                        } else {
                            listOf(Color(0x99D8B4FE), Color(0x557C3AED))
                        }
                    )
                ),
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = LiterarySerifFontFamily,
            color = if (isSelected) Color.White else Color(0xFFF3E8FF),
            fontSize = 11.8.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Clean, airy toggle row inside the unified Mor Cam frosted glass tray.
 */
@Composable
private fun MorCamGlassToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val motionConfig = LocalCosmicMotionConfig.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = if (motionConfig.enableMicroSprings) CosmicMotionSpecs.snappyOrb() else tween(140),
        label = "glassToggleThumb"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontFamily = LiterarySerifFontFamily,
                color = Color(0xFFFBF8FF),
                fontSize = 13.8.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xBB100420),
                        offset = Offset(0f, 1f),
                        blurRadius = 4f
                    )
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = LiterarySerifFontFamily,
                color = Color(0xFFE9D5FF).copy(alpha = 0.80f),
                fontSize = 11.2.sp
            )
        }

        // Translucent Mor Cam Glass Capsule Switch
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (checked) {
                        Brush.horizontalGradient(listOf(Color(0xAA7E22CE), Color(0xBBA855F7)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0x481A0B36), Color(0x48120726)))
                    }
                )
                .border(
                    1.dp,
                    if (checked) Color(0xFFF3E8FF) else Color(0x77C084FC),
                    RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (checked) {
                            Brush.radialGradient(listOf(Color.White, Color(0xFFF3E8FF), Color(0xFFD8B4FE)))
                        } else {
                            Brush.radialGradient(listOf(Color(0xFFB5A4E0), Color(0xFF6B539E)))
                        }
                    )
            )
        }
    }
}

/**
 * Delicate 6.dp Translucent Glass-Tube Slider with Glowing Crystal Orb Thumb
 * (Replaces thick solid purple rectangle bars).
 */
@Composable
private fun MorCamGlassSlider(
    label: String,
    value: Int,
    accentColor: Color,
    onValueChange: (Int) -> Unit
) {
    val clamped = value.coerceIn(0, 100)
    val fraction = clamped / 100f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = LiterarySerifFontFamily,
                color = Color(0xFFFBF8FF),
                fontSize = 12.8.sp
            )
            Text(
                text = "%$clamped",
                fontFamily = LiterarySerifFontFamily,
                color = Color(0xFFF3E8FF),
                fontSize = 12.5.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val pct = ((offset.x / size.width.toFloat()).coerceIn(0f, 1f) * 100f).roundToInt()
                        onValueChange(pct)
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val pct = ((change.position.x / size.width.toFloat()).coerceIn(0f, 1f) * 100f).roundToInt()
                        onValueChange(pct)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackH = 5.5.dp.toPx()
                val cy = size.height / 2f
                val cr = trackH / 2f
                val activeW = (size.width * fraction).coerceIn(cr * 2f, size.width)

                // Translucent glass tube background
                drawRoundRect(
                    color = Color(0x33180A32),
                    topLeft = Offset(0f, cy - cr),
                    size = Size(size.width, trackH),
                    cornerRadius = CornerRadius(cr, cr)
                )
                drawRoundRect(
                    color = Color(0x66C084FC),
                    topLeft = Offset(0f, cy - cr),
                    size = Size(size.width, trackH),
                    cornerRadius = CornerRadius(cr, cr),
                    style = Stroke(width = 0.8.dp.toPx())
                )

                // Luminous amethyst-lavender active glass fill
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0x886D28D9), accentColor, Color(0xFFF3E8FF))
                    ),
                    topLeft = Offset(0f, cy - cr),
                    size = Size(activeW, trackH),
                    cornerRadius = CornerRadius(cr, cr)
                )

                // Glowing Crystal Orb Thumb
                val thumbX = (size.width * fraction).coerceIn(7.dp.toPx(), size.width - 7.dp.toPx())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF3E8FF),
                            accentColor,
                            Color.Transparent
                        ),
                        center = Offset(thumbX, cy),
                        radius = 10.dp.toPx()
                    ),
                    radius = 10.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.5.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
            }
        }
    }
}

/**
 * See-Through Mor Cam Glass Text Field (replaces opaque dark Material OutlinedTextField boxes
 * so the wisteria background remains visible through every input field).
 */
@Composable
private fun MorCamGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 6,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = LiterarySerifFontFamily,
            color = Color(0xFFE9D5FF),
            fontSize = 11.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (singleLine) 40.dp else (minLines * 22 + 20).dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x26160A2E))
                .border(
                    BorderStroke(
                        0.95.dp,
                        Brush.linearGradient(
                            listOf(Color(0xCCE9D5FF), Color(0x66A855F7), Color(0xBBD8B4FE))
                        )
                    ),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 13.dp, vertical = 10.dp)
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    fontFamily = LiterarySerifFontFamily,
                    fontStyle = FontStyle.Italic,
                    color = Color(0x99D8B4FE),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                maxLines = maxLines,
                textStyle = TextStyle(
                    color = Color(0xFFFBF8FF),
                    fontSize = 13.2.sp,
                    fontFamily = LiterarySerifFontFamily,
                    lineHeight = 19.sp
                ),
                cursorBrush = SolidColor(Color(0xFFE9D5FF)),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MorCamHairlineDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(0.8.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0x88D8B4FE),
                        Color(0xCCE9D5FF),
                        Color(0x88D8B4FE),
                        Color.Transparent
                    )
                )
            )
    )
}

private fun buildSystemPromptPreview(
    bot: BotEntity,
    activeFactsCount: Int,
    activeEventsCount: Int
): String {
    val sb = StringBuilder()
    val name = if (bot.mode == "universe") bot.universeName else bot.aiName
    sb.appendLine("=== SİSTEM YÖNERGESİ ($name) ===")
    sb.appendLine("Mod: ${if (bot.mode == "universe") "Evren / Çoklu Karakter" else "Bireysel Karakter"}")
    sb.appendLine("Anlatım Biçimi: ${if (bot.writingStyle == "chat") "Doğal Sohbet" else "Edebi Rol Yapma (RP)"}")
    sb.appendLine("Yanıt Uzunluğu: ${bot.customLength} | Yoğunluk: ${bot.intensity}")
    sb.appendLine("İzinler: NSFW=${bot.isNsfw}, OOC=${bot.enableOoc}, Proaktif=${bot.proactiveEnabled}")
    sb.appendLine()
    if (bot.aiPersonality.isNotBlank()) {
        sb.appendLine("[KARAKTER KİŞİLİĞİ]")
        sb.appendLine(bot.aiPersonality)
        sb.appendLine()
    }
    if (bot.scenario.isNotBlank()) {
        sb.appendLine("[SENARYO & ATMOSFER]")
        sb.appendLine(bot.scenario)
        sb.appendLine()
    }
    if (bot.userCharName.isNotBlank() || bot.userCharDesc.isNotBlank()) {
        sb.appendLine("[KULLANICI PERSONASI]")
        sb.appendLine("İsim: ${bot.userCharName.ifBlank { "Belirtilmedi" }}")
        if (bot.userCharDesc.isNotBlank()) sb.appendLine("Tanım: ${bot.userCharDesc}")
        sb.appendLine()
    }
    val emo = EmotionState.fromJson(bot.emotionState)
    val world = WorldState.fromJson(bot.worldAtmosphere)
    sb.appendLine("[CANLI DUYGU & DÜNYA DURUMU]")
    sb.appendLine("Baskın Duygu: ${emo.dominantEmotion} | Sevgi: %${emo.affection} | Güven: %${emo.trust} | Rahatlık: %${emo.physicalComfortScore}")
    sb.appendLine("Kırgınlık: %${emo.hurt} | Takıntı: %${emo.obsession}")
    sb.appendLine("Küresel Gerilim: %${world.macro.globalTensionLevel} | Mekan: ${world.meso.currentLocation} | Zaman: ${world.meso.timeOfDay}")
    sb.appendLine()
    if (bot.pinnedMemory.isNotBlank() || bot.storyNotes.isNotBlank() || bot.memoryNotes.isNotBlank() || activeFactsCount > 0 || activeEventsCount > 0) {
        sb.appendLine("[HAFIZA & NOTLAR ($activeFactsCount Otomatik Anı, $activeEventsCount Olay)]")
        if (bot.pinnedMemory.isNotBlank()) sb.appendLine("Sabit Hafıza: ${bot.pinnedMemory}")
        if (bot.storyNotes.isNotBlank()) sb.appendLine("Hikaye Notları: ${bot.storyNotes}")
        if (bot.memoryNotes.isNotBlank()) sb.appendLine("Ek Notlar: ${bot.memoryNotes}")
    }
    return sb.toString().trim()
}
