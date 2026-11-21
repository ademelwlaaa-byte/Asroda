package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.BotEntity
import com.example.data.local.EmotionState
import com.example.data.local.RelationshipAxes
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.local.WorldAtmosphere
import com.example.data.repository.BaselineEmotionEngine
import com.example.data.repository.KeyCharacter
import com.example.ui.components.AppBackground
import com.example.ui.components.BotAvatarImage
import com.example.ui.components.EmochiLoadingSpinner
import com.example.ui.components.MorCamCircleIconButton
import com.example.ui.components.MorCamGlassSlider
import com.example.ui.theme.DisplaySerifFontFamily
import android.widget.Toast
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.CosmicOrbLoader
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.cosmicShimmerSweep
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import com.example.util.AvatarOptimizer
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Composable
fun CreateBotWizardScreen(
    onBack: () -> Unit,
    onFinish: (BotEntity) -> Unit,
    onGenerateOpening: suspend (BotEntity) -> String,
    onAnalyzeBaseline: (suspend (BotEntity) -> BaselineEmotionEngine.BaselineAnalysisResult)? = null
) {
    var stepIdx by remember { mutableIntStateOf(0) }

    var mode by remember { mutableStateOf("personal") }
    var aiName by remember { mutableStateOf("") }
    var aiPersonality by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var pinnedMemory by remember { mutableStateOf("") }
    var scenario by remember { mutableStateOf("") }
    var universeName by remember { mutableStateOf("") }
    var userCharName by remember { mutableStateOf("") }
    var userCharDesc by remember { mutableStateOf("") }
    var openingMessage by remember { mutableStateOf("") }
    var writingStyle by remember { mutableStateOf("rp") }
    var intensity by remember { mutableStateOf("normal") }
    var isPublic by remember { mutableStateOf(true) }
    var storyStartDate by remember { mutableStateOf("") }
    var keyCharacters by remember { mutableStateOf<List<KeyCharacter>>(emptyList()) }

    // Baseline emotion analysis review state (Step 5)
    var analysisResult by remember { mutableStateOf<BaselineEmotionEngine.BaselineAnalysisResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisError by remember { mutableStateOf<String?>(null) }
    var userEditedFields by remember { mutableStateOf<Set<String>>(emptySet()) }
    var reviewRelType by remember { mutableStateOf("") }
    var reviewAttachment by remember { mutableStateOf("secure") }
    var reviewSpeechStyle by remember { mutableStateOf("") }
    var reviewDominantEmotion by remember { mutableStateOf("") }
    var reviewTriggers by remember { mutableStateOf<List<SensitiveTriggerEntity>>(emptyList()) }
    var reviewBigFive by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var reviewAxes by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var isGeneratingOpening by remember { mutableStateOf(false) }
    var genError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val frostedBackdrop = remember(context) {
        createFrostedGothicManorBitmap(context, R.drawable.img_botlist_gothic_wisteria_bg)
    }

    // Zero-permission Android Photo Picker -> OOM-safe 256x256 Micro-WebP (~15 KB Base64)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { inputUri ->
            scope.launch {
                AvatarOptimizer.processAvatarToMicroWebPBase64(context, inputUri)
                    .onSuccess { microWebPBase64 ->
                        avatarUrl = microWebPBase64
                    }
                    .onFailure {
                        Toast.makeText(
                            context,
                            "Görsel işlenemedi, lütfen başka bir fotoğraf seçin.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    val totalSteps = 6
    val stepLabels = listOf(
        "Mod Seçimi",
        if (mode == "personal") "Kimlik & Kişilik" else "Evren & Kadro",
        "Senaryo & Rol",
        "Açılış Sahnesi",
        "Duygu Profili",
        "Stil & Yayın"
    )

    BackHandler {
        if (stepIdx > 0) stepIdx-- else onBack()
    }

    LaunchedEffect(stepIdx) {
        scrollState.scrollTo(0)
    }

    fun applyAnalysisState(res: BaselineEmotionEngine.BaselineAnalysisResult) {
        analysisResult = res
        reviewRelType = res.relationshipType
        reviewAttachment = res.personalityProfile.attachmentStyle
        reviewSpeechStyle = res.speechPattern
        reviewDominantEmotion = res.baselineEmotionState.dominantEmotion
        reviewTriggers = res.sensitiveTriggers.toMutableList()
        reviewBigFive = mapOf(
            "openness" to res.personalityProfile.openness,
            "conscientiousness" to res.personalityProfile.conscientiousness,
            "extraversion" to res.personalityProfile.extraversion,
            "agreeableness" to res.personalityProfile.agreeableness,
            "neuroticism" to res.personalityProfile.neuroticism
        )
        reviewAxes = mapOf(
            "affection" to res.baselineEmotionState.affection,
            "respect" to res.baselineEmotionState.relationshipAxes.respectScore,
            "comfort" to res.baselineEmotionState.relationshipAxes.comfortScore,
            "resentment" to res.baselineEmotionState.relationshipAxes.resentmentScore
        )
    }

    fun buildCurrentDraft(): BotEntity {
        val serializedCast = if (keyCharacters.isEmpty()) "[]" else {
            val array = JSONArray()
            for (char in keyCharacters) {
                val obj = JSONObject()
                obj.put("id", char.id)
                obj.put("name", char.name)
                obj.put("desc", char.desc)
                array.put(obj)
            }
            array.toString()
        }
        val baselineEmotion = EmotionState.calculateBaselineEmotionState(
            aiName = if (mode == "personal") aiName.trim() else universeName.trim(),
            personality = aiPersonality.trim(),
            scenario = scenario.trim(),
            userCharName = userCharName.trim(),
            userCharDesc = userCharDesc.trim()
        ).toJson()

        val initialWorldAtmosphere = if (mode == "universe") {
            val intensityInt = when (intensity) {
                "düşük" -> 3
                "yüksek", "intense" -> 8
                else -> 5
            }
            WorldAtmosphere(
                mood = "sakin ve beklentili",
                intensity = intensityInt,
                currentEvent = "Açılış sahnesi ve hikayenin başlangıcı",
                macroAtmosphere = "${universeName.trim()} evreninin genel düzeni ve toplumsal kuralları hakim.",
                microAtmosphere = "Hikayenin başladığı fiziki mekan ve başlangıç ortamı."
            ).toJson()
        } else ""

        return BotEntity(
            id = UUID.randomUUID().toString(),
            mode = mode,
            aiName = aiName.trim(),
            aiPersonality = aiPersonality.trim(),
            avatarUrl = avatarUrl.trim(),
            pinnedMemory = pinnedMemory.trim(),
            scenario = scenario.trim(),
            universeName = universeName.trim(),
            keyCharactersJson = serializedCast,
            userCharName = userCharName.trim().ifBlank { "Kullanıcı" },
            userCharDesc = userCharDesc.trim(),
            openingMessage = openingMessage.trim(),
            writingStyle = writingStyle,
            intensity = intensity,
            customLength = "default",
            isNsfw = true,
            isPublic = isPublic,
            emotionState = baselineEmotion,
            previousEmotionState = baselineEmotion,
            worldAtmosphere = initialWorldAtmosphere,
            storyCalendarDate = storyStartDate.trim(),
            hasExplicitStoryDate = storyStartDate.trim().isNotBlank(),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun triggerBaselineAnalysis(resetEdits: Boolean) {
        val draft = buildCurrentDraft()
        scope.launch {
            isAnalyzing = true
            analysisError = null
            if (resetEdits) userEditedFields = emptySet()
            try {
                val res = onAnalyzeBaseline?.invoke(draft)
                    ?: BaselineEmotionEngine.calculateDeterministicFallback(
                        botId = draft.id,
                        aiName = draft.aiName,
                        personality = draft.aiPersonality,
                        scenario = draft.scenario,
                        userCharName = draft.userCharName,
                        userCharDesc = draft.userCharDesc
                    )
                applyAnalysisState(res)
            } catch (_: Exception) {
                analysisError = "Analiz başarısız. Otomatik tahmin uygulandı."
                val fb = BaselineEmotionEngine.calculateDeterministicFallback(
                    botId = draft.id,
                    aiName = draft.aiName,
                    personality = draft.aiPersonality,
                    scenario = draft.scenario,
                    userCharName = draft.userCharName,
                    userCharDesc = draft.userCharDesc
                )
                applyAnalysisState(fb)
            } finally {
                isAnalyzing = false
            }
        }
    }

    val canNext = when (stepIdx) {
        0 -> true
        1 -> if (mode == "personal") aiName.isNotBlank() && aiPersonality.isNotBlank() else universeName.isNotBlank()
        2 -> scenario.isNotBlank() && userCharName.isNotBlank()
        3 -> openingMessage.isNotBlank()
        4 -> !isAnalyzing
        5 -> true
        else -> true
    }

    val motionConfig = LocalCosmicMotionConfig.current
    val pulseGlowAlpha = if (motionConfig.enableAmbientLoops) {
        val infiniteTransition = rememberInfiniteTransition(label = "wizardMorCamAnim")
        val animatedAlpha by infiniteTransition.animateFloat(
            initialValue = 0.68f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wizardPulseGlow"
        )
        animatedAlpha
    } else {
        0.86f
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Unified Cosmic Wisteria App Background (harmonized across all app menus)
        AppBackground(activeTab = "chats")

        // Subtle amethyst atmospheric depth veil for high legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x660B0419),
                            Color(0x4D0D051F),
                            Color(0x7A090314)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                WizardMorCamHeader(
                    stepIdx = stepIdx,
                    totalSteps = totalSteps,
                    stepLabel = stepLabels.getOrElse(stepIdx) { "" },
                    frostedBackdrop = frostedBackdrop,
                    scrollSync = scrollState.value,
                    glowAlpha = pulseGlowAlpha,
                    onBackClick = { if (stepIdx > 0) stepIdx-- else onBack() }
                )
            },
            bottomBar = {
                WizardMorCamBottomDock(
                    stepIdx = stepIdx,
                    totalSteps = totalSteps,
                    canNext = canNext,
                    frostedBackdrop = frostedBackdrop,
                    scrollSync = scrollState.value,
                    glowAlpha = pulseGlowAlpha,
                    onBack = { if (stepIdx > 0) stepIdx-- else onBack() },
                    onNext = {
                        when {
                            stepIdx == 3 -> {
                                stepIdx++
                                triggerBaselineAnalysis(resetEdits = false)
                            }
                            stepIdx < totalSteps - 1 -> {
                                stepIdx++
                            }
                            else -> {
                                val base = buildCurrentDraft()
                                val res = analysisResult
                                val finalBot = if (res != null) {
                                    val axes = RelationshipAxes(
                                        affectionScore = reviewAxes["affection"] ?: res.baselineEmotionState.affection,
                                        respectScore = reviewAxes["respect"] ?: res.baselineEmotionState.relationshipAxes.respectScore,
                                        comfortScore = reviewAxes["comfort"] ?: res.baselineEmotionState.relationshipAxes.comfortScore,
                                        resentmentScore = reviewAxes["resentment"] ?: res.baselineEmotionState.relationshipAxes.resentmentScore
                                    )
                                    val editedState = res.baselineEmotionState.copy(
                                        relationshipAxes = axes,
                                        dominantEmotion = reviewDominantEmotion,
                                        speechPattern = reviewSpeechStyle,
                                        primaryEmotions = res.baselineEmotionState.primaryEmotions
                                    )
                                    val emotionJson = editedState.toJson()
                                    base.copy(
                                        emotionState = emotionJson,
                                        previousEmotionState = emotionJson,
                                        baseAffectionDifficulty = res.difficultyMultiplier,
                                        emotionalRegulationCapacity = res.regulationCapacity,
                                        baselineEmotionAnalysisJson = res.rawJson.ifBlank { "" },
                                        baselineAnalysisSource = if (userEditedFields.isNotEmpty()) "user_edited" else res.analysisSource,
                                        baselineUserEditedFields = userEditedFields.joinToString(",")
                                    )
                                } else {
                                    base
                                }
                                onFinish(finalBot)
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = stepIdx,
                transitionSpec = {
                    CosmicMotionSpecs.cosmicTabTransition(
                        forward = targetState > initialState,
                        config = motionConfig
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "wizardStepTransition"
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    when (currentStep) {
                        // ── STEP 1 / 6: INTERACTION MODE ─────────────────────
                        0 -> {
                            WizardStepHeader(
                                title = "Nasıl bir etkileşim\nistersiniz?",
                                subtitle = "AI karakter tipini ve hikaye yapısını seçin.",
                                glowAlpha = pulseGlowAlpha
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            WizardMorCamChoiceCard(
                                title = "Kişisel Karakter",
                                subtitle1 = "Sana özel bire bir sohbet.",
                                subtitle2 = "Tek ve sabit bir karakterle gerçek zamanlı bağ.",
                                iconVector = Icons.Default.Person,
                                isSelected = mode == "personal",
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                testTag = "wizard_mode_personal",
                                onClick = { mode = "personal" }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            WizardMorCamChoiceCard(
                                title = "Evren / Hikaye (RP)",
                                subtitle1 = "Kurgusal bir dünyada geçen roleplay.",
                                subtitle2 = "AI evrenin anlatıcısı olur ve kadroyu yönetir.",
                                iconVector = Icons.Default.MenuBook,
                                isSelected = mode == "universe",
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                testTag = "wizard_mode_universe",
                                onClick = { mode = "universe" }
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Subtle contextual tip pill matching Mor Cam aesthetic
                            WizardMorCamSectionCard(
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFD8B4FE),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (mode == "personal") {
                                            "Kişisel Karakter modu; derin duygu hafızası, ilişki eksenleri ve bire bir diyaloglar için idealdir."
                                        } else {
                                            "Evren (RP) modu; birden fazla karakteri, mekan atmosferini ve olay örgüsünü aynı anda yönetir."
                                        },
                                        fontFamily = InterFontFamily,
                                        color = Color(0xFFD8C9F5),
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // ── STEP 2 / 6: CHARACTER DETAILS OR UNIVERSE CAST ───
                        1 -> {
                            if (mode == "personal") {
                                WizardStepHeader(
                                    title = "Karakterinizi nasıl\ntanımlarsınız?",
                                    subtitle = "Karşınızdaki karakterin portresini, adını ve kişiliğini belirleyin.",
                                    glowAlpha = pulseGlowAlpha
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Frosted Glass Avatar Upload Card
                                WizardMorCamSectionCard(
                                    frostedBackdrop = frostedBackdrop,
                                    scrollSync = scrollState.value,
                                    modifier = Modifier
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .testTag("wizard_avatar_picker_card")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(17.dp))
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(Color(0x806D28D9), Color(0x551F0A3D))
                                                    )
                                                )
                                                .border(
                                                    width = 1.4.dp,
                                                    brush = Brush.linearGradient(
                                                        listOf(Color(0xFFF3E8FF), Color(0xFFC084FC), Color(0xFF7C3AED))
                                                    ),
                                                    shape = RoundedCornerShape(17.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            BotAvatarImage(
                                                avatarUrl = avatarUrl.takeIf { it.isNotBlank() },
                                                contentDescription = "Karakter Profil Fotoğrafı",
                                                modifier = Modifier.fillMaxSize(),
                                                fallback = {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = Color(0xFFEDE9FE),
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(15.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Bot Profil Fotoğrafı",
                                                fontFamily = DisplaySerifFontFamily,
                                                color = Color(0xFFFAF6FF),
                                                fontSize = 16.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = if (avatarUrl.isNotBlank()) {
                                                    "Görsel seçildi • Değiştirmek için dokun"
                                                } else {
                                                    "Galeriden özel bir portre veya kapak seçin."
                                                },
                                                fontFamily = InterFontFamily,
                                                color = Color(0xFFCBBCEE),
                                                fontSize = 12.5.sp,
                                                lineHeight = 17.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        if (avatarUrl.isNotBlank()) {
                                            MorCamCircleIconButton(
                                                icon = Icons.Default.Close,
                                                contentDescription = "Fotoğrafı Kaldır",
                                                onClick = { avatarUrl = "" },
                                                size = 38.dp,
                                                isDestructive = true,
                                                testTag = "wizard_remove_avatar_button"
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = Color(0xFFD8B4FE),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                WizardFieldLabel(title = "Karakter Adı", required = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                WizardMorCamTextField(
                                    value = aiName,
                                    onValueChange = { aiName = it },
                                    placeholder = "Ör. Aiden Blackwood",
                                    singleLine = true,
                                    frostedBackdrop = frostedBackdrop,
                                    scrollSync = scrollState.value,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ai_name_input")
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                WizardFieldLabel(title = "Kişilik ve Özellikler", required = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                WizardMorCamTextField(
                                    value = aiPersonality,
                                    onValueChange = { aiPersonality = it },
                                    placeholder = "Soğukkanlı mı, korumacı mı, konuşma tarzı ve geçmişi nasıl...",
                                    singleLine = false,
                                    frostedBackdrop = frostedBackdrop,
                                    scrollSync = scrollState.value,
                                    minHeight = 142.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ai_personality_input")
                                )
                            } else {
                                WizardStepHeader(
                                    title = "Evren ve karakter\nkadrosunu kurun",
                                    subtitle = "Kurgusal dünyanın adını ve ana karakter kadrosunu belirleyin.",
                                    glowAlpha = pulseGlowAlpha
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                WizardFieldLabel(title = "Evren Adı", required = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                WizardMorCamTextField(
                                    value = universeName,
                                    onValueChange = { universeName = it },
                                    placeholder = "Ör. Eldoria Krallığı veya Karanlık Akademi",
                                    singleLine = true,
                                    frostedBackdrop = frostedBackdrop,
                                    scrollSync = scrollState.value,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("universe_name_input")
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                WizardFieldLabel(title = "Ana Karakter Kadrosu (Opsiyonel)", required = false)
                                Spacer(modifier = Modifier.height(10.dp))

                                keyCharacters.forEachIndexed { idx, char ->
                                    WizardMorCamSectionCard(
                                        frostedBackdrop = frostedBackdrop,
                                        scrollSync = scrollState.value,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            WizardMorCamTextField(
                                                value = char.name,
                                                onValueChange = { newName ->
                                                    keyCharacters = keyCharacters.toMutableList().apply {
                                                        this[idx] = this[idx].copy(name = newName)
                                                    }
                                                },
                                                placeholder = "İsim",
                                                singleLine = true,
                                                frostedBackdrop = frostedBackdrop,
                                                scrollSync = scrollState.value,
                                                minHeight = 48.dp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            WizardMorCamTextField(
                                                value = char.desc,
                                                onValueChange = { newDesc ->
                                                    keyCharacters = keyCharacters.toMutableList().apply {
                                                        this[idx] = this[idx].copy(desc = newDesc)
                                                    }
                                                },
                                                placeholder = "Rol / Tanım",
                                                singleLine = true,
                                                frostedBackdrop = frostedBackdrop,
                                                scrollSync = scrollState.value,
                                                minHeight = 48.dp,
                                                modifier = Modifier.weight(1.45f)
                                            )
                                            MorCamCircleIconButton(
                                                icon = Icons.Default.Close,
                                                contentDescription = "Karakteri Sil",
                                                onClick = {
                                                    keyCharacters = keyCharacters.toMutableList().apply { removeAt(idx) }
                                                },
                                                size = 38.dp,
                                                isDestructive = true
                                            )
                                        }
                                    }
                                }

                                // Frosted Glass "Karakter Ekle" Action Capsule
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .wizardMorCamGlow(cornerRadius = 25.dp, alpha = 0.34f * pulseGlowAlpha, blurDp = 9.dp)
                                        .clip(RoundedCornerShape(50))
                                        .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.74f, scrollSync = scrollState.value)
                                        .background(
                                            Brush.horizontalGradient(listOf(Color(0x5A5B21B6), Color(0x463B1470), Color(0x5A6D28D9)))
                                        )
                                        .border(
                                            width = 1.2.dp,
                                            brush = Brush.linearGradient(listOf(Color(0xE8F3E8FF), Color(0xAAC084FC))),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .clickable {
                                            keyCharacters = keyCharacters + KeyCharacter(name = "", desc = "")
                                        }
                                        .testTag("wizard_add_character_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color(0xFFF3E8FF),
                                            modifier = Modifier.size(19.dp)
                                        )
                                        Text(
                                            text = "Karakter Ekle",
                                            fontFamily = LiterarySerifFontFamily,
                                            color = Color(0xFFFAF6FF),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // ── STEP 3 / 6: SCENARIO & USER CHARACTER ────────────
                        2 -> {
                            WizardStepHeader(
                                title = "Senaryo ve kendi\nkarakteriniz",
                                subtitle = "Sahneye giriş bağlamını ve canlandıracağınız rolü tanımlayın.",
                                glowAlpha = pulseGlowAlpha
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            WizardFieldLabel(title = "Senaryo / Bağlam", required = true)
                            Spacer(modifier = Modifier.height(8.dp))
                            WizardMorCamTextField(
                                value = scenario,
                                onValueChange = { scenario = it },
                                placeholder = "Neredesiniz, aranızdaki ilişki ne, gerilim veya hikaye nasıl başladı...",
                                singleLine = false,
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                minHeight = 130.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("scenario_input")
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            WizardFieldLabel(title = "Sizin Canlandırdığınız Karakterin Adı", required = true)
                            Spacer(modifier = Modifier.height(8.dp))
                            WizardMorCamTextField(
                                value = userCharName,
                                onValueChange = { userCharName = it },
                                placeholder = "Ör. Zeynep",
                                singleLine = true,
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("user_char_name_input")
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            WizardFieldLabel(title = "Karakterinizin Kısa Açıklaması (Opsiyonel)", required = false)
                            Spacer(modifier = Modifier.height(8.dp))
                            WizardMorCamTextField(
                                value = userCharDesc,
                                onValueChange = { userCharDesc = it },
                                placeholder = "Yaş, görünüş, kişilik özellikleri veya hikayelerdeki konumunuz...",
                                singleLine = false,
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                minHeight = 108.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // ── STEP 4 / 6: OPENING SCENE ────────────────────────
                        3 -> {
                            WizardStepHeader(
                                title = "Başlangıç sahnesi\nnasıl açılsın?",
                                subtitle = "Sohbet bu açılış mesajı ve atmosfer betimlemesiyle başlayacak.",
                                glowAlpha = pulseGlowAlpha
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            WizardFieldLabel(title = "Açılış Mesajı / Sahnesi", required = true)
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cosmicShimmerSweep(active = isGeneratingOpening, cornerRadius = 18.dp)
                            ) {
                                WizardMorCamTextField(
                                    value = openingMessage,
                                    onValueChange = { if (it.length <= 1000) openingMessage = it },
                                    placeholder = "Açılış mesajını kendiniz yazın veya aşağıdaki yapay zeka kartını kullanın...",
                                    singleLine = false,
                                    frostedBackdrop = frostedBackdrop,
                                    scrollSync = scrollState.value,
                                    minHeight = 176.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("opening_message_input")
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 12.dp, bottom = 10.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0x8027134D))
                                        .border(0.8.dp, Color(0x99C4B5FD), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${openingMessage.length} / 1000",
                                        fontFamily = InterFontFamily,
                                        color = Color(0xFFE9D5FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            WizardMorCamChoiceCard(
                                title = if (isGeneratingOpening) "Yapay Zeka Sahneyi Yazıyor..." else "AI Otomatik Yazsın",
                                subtitle1 = "Senaryo ve karakter kişiliğine uygun sinematik açılış.",
                                subtitle2 = "Tek dokunuşla atmosferik ilk mesajı oluştur.",
                                iconVector = Icons.Default.AutoAwesome,
                                isSelected = true,
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                testTag = "wizard_ai_generate_opening_button",
                                onClick = {
                                    if (!isGeneratingOpening) {
                                        scope.launch {
                                            isGeneratingOpening = true
                                            genError = null
                                            try {
                                                val generated = onGenerateOpening(buildCurrentDraft())
                                                openingMessage = generated
                                            } catch (e: Exception) {
                                                genError = e.message ?: "Oluşturulamadı."
                                            } finally {
                                                isGeneratingOpening = false
                                            }
                                        }
                                    }
                                }
                            )

                            genError?.let { err ->
                                Text(
                                    text = err,
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 12.5.sp,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                            }
                        }

                        // ── STEP 5 / 6: EMOTIONAL PROFILE & BASELINE REVIEW ──
                        4 -> {
                            WizardStepHeader(
                                title = "Duygusal profil ve\nilişki analizi",
                                subtitle = "Başlangıç duygu durumunu, bağlanma stilini ve kişilik eksenlerini inceleyin.",
                                glowAlpha = pulseGlowAlpha
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            if (isAnalyzing) {
                                WizardMorCamSectionCard(
                                    frostedBackdrop = frostedBackdrop,
                                    scrollSync = scrollState.value,
                                    modifier = Modifier.cosmicShimmerSweep(active = true, cornerRadius = 22.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 26.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CosmicOrbLoader(size = 42.dp)
                                        Spacer(Modifier.height(14.dp))
                                        Text(
                                            text = "Yapay Zeka Karakterinizi Analiz Ediyor…",
                                            fontFamily = LiterarySerifFontFamily,
                                            color = Color(0xFFF3E8FF),
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                val res = analysisResult
                                if (analysisError != null) {
                                    Text(
                                        text = "⚠ $analysisError",
                                        fontFamily = InterFontFamily,
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp)
                                    )
                                }
                                if (res != null) {
                                    val (sourceBadge, sourceColor) = when (res.analysisSource) {
                                        "llm" -> Pair("✦ Yapay Zeka Analizi", Color(0xFFE9D5FF))
                                        "keyword" -> Pair("✦ Otomatik Tahmin", Color(0xFFC4B5FD))
                                        else -> Pair("✦ Kullanıcı Düzenlemesi", Color(0xFFFAF5FF))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(Color(0x5C3B1A74))
                                                .border(1.dp, sourceColor.copy(alpha = 0.72f), RoundedCornerShape(50))
                                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = sourceBadge,
                                                fontFamily = InterFontFamily,
                                                color = sourceColor,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        if (res.confidence < 60) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(Color(0x52451A03))
                                                    .border(1.dp, Color(0xAAFB923C), RoundedCornerShape(50))
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                            ) {
                                                Text(
                                                    text = "Güven: ${res.confidence}/100",
                                                    fontFamily = InterFontFamily,
                                                    color = Color(0xFFFDE047),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    // Relationship Type Selector with smooth horizontal edge fade
                                    WizardMorCamSectionCard(
                                        title = "İlişki Türü",
                                        subtitle = "Karakter ile aranızdaki başlangıç bağı",
                                        frostedBackdrop = frostedBackdrop,
                                        scrollSync = scrollState.value
                                    ) {
                                        val relTypes = BaselineEmotionEngine.VALID_REL_TYPES.toList()
                                        val relLabels = mapOf(
                                            "parent" to "Ebeveyn",
                                            "sibling" to "Kardeş",
                                            "friend" to "Arkadaş",
                                            "romantic" to "Sevgili",
                                            "ex_romantic" to "Eski Sevgili",
                                            "conflicted_romantic" to "Çatışmalı Sevgili",
                                            "rival" to "Rakip",
                                            "enemy" to "Düşman",
                                            "stranger" to "Yabancı",
                                            "authority" to "Otorite",
                                            "mentor" to "Mentor",
                                            "subordinate" to "Ast",
                                            "companion_creature" to "Yaratık/Evcil",
                                            "other" to "Diğer"
                                        )
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                                .drawWithContent {
                                                    drawContent()
                                                    drawRect(
                                                        brush = Brush.horizontalGradient(
                                                            0.00f to Color.Transparent,
                                                            0.05f to Color.White,
                                                            0.95f to Color.White,
                                                            1.00f to Color.Transparent
                                                        ),
                                                        blendMode = BlendMode.DstIn
                                                    )
                                                }
                                        ) {
                                            items(relTypes) { rt ->
                                                WizardMorCamPillChip(
                                                    label = relLabels[rt] ?: rt,
                                                    selected = reviewRelType == rt,
                                                    onClick = {
                                                        reviewRelType = rt
                                                        userEditedFields = userEditedFields + "relationshipType"
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // Big Five Sliders
                                    WizardMorCamSectionCard(
                                        title = "Kişilik Eksenleri (Big Five)",
                                        subtitle = "Karakterin psikolojik eğilim oranları",
                                        frostedBackdrop = frostedBackdrop,
                                        scrollSync = scrollState.value
                                    ) {
                                        val bigFiveLabels = listOf(
                                            "openness" to "Açıklık",
                                            "conscientiousness" to "Sorumluluk",
                                            "extraversion" to "Dışadönüklük",
                                            "agreeableness" to "Uyumluluk",
                                            "neuroticism" to "Nevrotiklik"
                                        )
                                        bigFiveLabels.forEach { (key, label) ->
                                            val value = reviewBigFive[key] ?: 50
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontFamily = InterFontFamily,
                                                    color = Color(0xFFE5D8FF),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.width(92.dp)
                                                )
                                                MorCamGlassSlider(
                                                    value = value.toFloat(),
                                                    onValueChange = {
                                                        reviewBigFive = reviewBigFive + (key to it.toInt())
                                                        userEditedFields = userEditedFields + "bigFive"
                                                    },
                                                    valueRange = 0f..100f,
                                                    accentColor = Color(0xFFC084FC),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .width(38.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0x553B1A70))
                                                        .border(0.9.dp, Color(0x99D8B4FE), RoundedCornerShape(10.dp))
                                                        .padding(vertical = 3.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "$value",
                                                        fontFamily = InterFontFamily,
                                                        color = Color.White,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // Attachment Style
                                    WizardMorCamSectionCard(
                                        title = "Bağlanma Stili",
                                        subtitle = "Yakınlık ve güven kurma biçimi",
                                        frostedBackdrop = frostedBackdrop,
                                        scrollSync = scrollState.value
                                    ) {
                                        val attachStyles = listOf(
                                            "secure" to "Güvenli",
                                            "anxious" to "Kaygılı",
                                            "avoidant" to "Kaçınan",
                                            "disorganized" to "Düzensiz"
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            attachStyles.forEach { (key, label) ->
                                                WizardMorCamPillChip(
                                                    label = label,
                                                    selected = reviewAttachment == key,
                                                    onClick = {
                                                        reviewAttachment = key
                                                        userEditedFields = userEditedFields + "attachmentStyle"
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // Dominant Emotion & Speech Style
                                    WizardMorCamSectionCard(
                                        title = "Baskın Duygu & Konuşma Tarzı",
                                        subtitle = "İlk sahnedeki ton ve hitap biçimi",
                                        frostedBackdrop = frostedBackdrop,
                                        scrollSync = scrollState.value
                                    ) {
                                        WizardFieldLabel(title = "Baskın Duygu", required = false)
                                        Spacer(Modifier.height(6.dp))
                                        WizardMorCamTextField(
                                            value = reviewDominantEmotion,
                                            onValueChange = {
                                                reviewDominantEmotion = it
                                                userEditedFields = userEditedFields + "dominantEmotion"
                                            },
                                            placeholder = "Ör. Sakin ve mesafeli",
                                            singleLine = true,
                                            frostedBackdrop = frostedBackdrop,
                                            scrollSync = scrollState.value,
                                            minHeight = 48.dp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        WizardFieldLabel(title = "Konuşma Tarzı", required = false)
                                        Spacer(Modifier.height(6.dp))
                                        WizardMorCamTextField(
                                            value = reviewSpeechStyle,
                                            onValueChange = {
                                                reviewSpeechStyle = it
                                                userEditedFields = userEditedFields + "speechStyle"
                                            },
                                            placeholder = "Ör. Kısa, net ve otoriter cümleler",
                                            singleLine = false,
                                            frostedBackdrop = frostedBackdrop,
                                            scrollSync = scrollState.value,
                                            minHeight = 76.dp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // Sensitive Triggers
                                    WizardMorCamSectionCard(
                                        title = "Hassas Konular",
                                        subtitle = "Karakteri duygusal olarak tetikleyen unsurlar",
                                        frostedBackdrop = frostedBackdrop,
                                        scrollSync = scrollState.value
                                    ) {
                                        reviewTriggers.forEachIndexed { idx, trig ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                WizardMorCamTextField(
                                                    value = trig.triggerTopic,
                                                    onValueChange = { v ->
                                                        reviewTriggers = reviewTriggers.toMutableList().also {
                                                            it[idx] = trig.copy(triggerTopic = v)
                                                        }
                                                        userEditedFields = userEditedFields + "sensitiveTriggers"
                                                    },
                                                    placeholder = "Hassas konu...",
                                                    singleLine = true,
                                                    frostedBackdrop = frostedBackdrop,
                                                    scrollSync = scrollState.value,
                                                    minHeight = 46.dp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                MorCamCircleIconButton(
                                                    icon = Icons.Default.Close,
                                                    contentDescription = "Sil",
                                                    onClick = {
                                                        if (reviewTriggers.size > 1) {
                                                            reviewTriggers = reviewTriggers.toMutableList().also { it.removeAt(idx) }
                                                            userEditedFields = userEditedFields + "sensitiveTriggers"
                                                        }
                                                    },
                                                    size = 38.dp,
                                                    isDestructive = true
                                                )
                                            }
                                        }
                                        if (reviewTriggers.size < 3) {
                                            WizardMorCamPillChip(
                                                label = "+ Konu Ekle",
                                                selected = false,
                                                onClick = {
                                                    reviewTriggers = reviewTriggers + SensitiveTriggerEntity(
                                                        botId = "",
                                                        triggerTopic = "",
                                                        sourceDescription = "",
                                                        intensityMultiplier = 1.8
                                                    )
                                                    userEditedFields = userEditedFields + "sensitiveTriggers"
                                                }
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    // Re-analyze & Reset Frosted Action Pills
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                                .clip(RoundedCornerShape(50))
                                                .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.74f, scrollSync = scrollState.value)
                                                .background(
                                                    Brush.horizontalGradient(listOf(Color(0x5E5B21B6), Color(0x4C3B1470)))
                                                )
                                                .border(1.15.dp, Color(0xCCE9D5FF), RoundedCornerShape(50))
                                                .clickable { triggerBaselineAnalysis(resetEdits = true) }
                                                .testTag("wizard_reanalyze_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF3E8FF),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Yeniden Analiz",
                                                    fontFamily = InterFontFamily,
                                                    color = Color.White,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                                .clip(RoundedCornerShape(50))
                                                .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.72f, scrollSync = scrollState.value)
                                                .background(Color(0x441B0B36))
                                                .border(1.dp, Color(0x77C4B5FD), RoundedCornerShape(50))
                                                .clickable {
                                                    val fb = BaselineEmotionEngine.calculateDeterministicFallback(
                                                        botId = "",
                                                        aiName = aiName,
                                                        personality = aiPersonality,
                                                        scenario = scenario,
                                                        userCharName = userCharName,
                                                        userCharDesc = userCharDesc
                                                    )
                                                    applyAnalysisState(fb)
                                                    userEditedFields = emptySet()
                                                    analysisError = null
                                                }
                                                .testTag("wizard_reset_analysis_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Varsayılana Dön",
                                                fontFamily = InterFontFamily,
                                                color = Color(0xFFE5D8FF),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── STEP 6 / 6: WRITING STYLE, TONE & VISIBILITY ─────
                        5 -> {
                            WizardStepHeader(
                                title = "Yazım tarzı ve\ngörünürlük ayarları",
                                subtitle = "Yanıtların biçimini, tematik yoğunluğunu ve gizliliğini seçin.",
                                glowAlpha = pulseGlowAlpha
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            WizardFieldLabel(title = "Yazım Formatı", required = false)
                            Spacer(modifier = Modifier.height(10.dp))

                            WizardMorCamChoiceCard(
                                title = "RP / Anlatı Tarzı",
                                subtitle1 = "Üçüncü şahıs roman anlatımı.",
                                subtitle2 = "Ortam ve mimik betimlemeleri + diyaloglar.",
                                iconVector = Icons.Default.MenuBook,
                                isSelected = writingStyle == "rp",
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                onClick = { writingStyle = "rp" }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            WizardMorCamChoiceCard(
                                title = "Sade Sohbet",
                                subtitle1 = "Doğal mesajlaşma tarzı.",
                                subtitle2 = "Kısa, samimi ve doğrudan yanıtlar.",
                                iconVector = Icons.Default.Person,
                                isSelected = writingStyle == "chat",
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                onClick = { writingStyle = "chat" }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            WizardFieldLabel(title = "Tema Yoğunluğu", required = false)
                            Spacer(modifier = Modifier.height(10.dp))

                            WizardMorCamChoiceCard(
                                title = "Normal",
                                subtitle1 = "Dengeli ve yumuşak ton.",
                                subtitle2 = "Günlük hikaye akışı ve doğal dramatik yapı.",
                                iconVector = Icons.Default.Settings,
                                isSelected = intensity == "normal",
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                onClick = { intensity = "normal" }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            WizardMorCamChoiceCard(
                                title = "Yoğun",
                                subtitle1 = "Derin psikolojik gerilim ve karanlık temalar.",
                                subtitle2 = "Duygusal çatışmalar yumuşatılmadan aktarılır.",
                                iconVector = Icons.Default.AutoAwesome,
                                isSelected = intensity == "intense",
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                onClick = { intensity = "intense" }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            WizardFieldLabel(title = "Hikâye Başlangıç Tarihi (İsteğe Bağlı)", required = false)
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "Boş bırakırsanız bot yalnızca Gün #N sayacını kullanır.",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFC2B2E4),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            WizardMorCamTextField(
                                value = storyStartDate,
                                onValueChange = { storyStartDate = it },
                                placeholder = "Ör. 2045-06-15 (yyyy-AA-gg)",
                                singleLine = true,
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            WizardFieldLabel(title = "Görünürlük & Yayınlama", required = false)
                            Spacer(modifier = Modifier.height(10.dp))

                            WizardMorCamChoiceCard(
                                title = "Herkese Açık (Keşfette Yayınla)",
                                subtitle1 = "Toplulukla paylaşılır.",
                                subtitle2 = "Botunuz Keşfet sekmesinde diğer kullanıcılara görünür.",
                                iconVector = Icons.Default.Public,
                                isSelected = isPublic,
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                onClick = { isPublic = true }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            WizardMorCamChoiceCard(
                                title = "Sadece Bana Özel",
                                subtitle1 = "Tamamen gizli ve kişisel.",
                                subtitle2 = "Bot yalnızca sizin cihazınızda ve sohbetlerinizde gözükür.",
                                iconVector = Icons.Default.Lock,
                                isSelected = !isPublic,
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                onClick = { isPublic = false }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}
