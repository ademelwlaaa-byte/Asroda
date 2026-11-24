package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.BotEntity
import com.example.data.local.EmotionState
import com.example.data.local.PersonalityProfile
import com.example.data.local.RelationshipAxes
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.local.WorldAtmosphere
import com.example.data.repository.BaselineEmotionEngine
import com.example.data.repository.KeyCharacter
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.cosmicShimmerSweep
import com.example.ui.components.AppBackground
import com.example.ui.components.BotAvatarImage
import com.example.ui.components.MorCamCircleIconButton
import com.example.ui.components.MorCamDialogShell
import com.example.ui.components.MorCamSwitchRow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import com.example.ui.viewmodel.EmochiViewModel
import com.example.util.AvatarOptimizer
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val PERSONAL_SPARK_CHIPS = listOf(
    "Soğukkanlı & Korumacı",
    "Kıskanç & Tutkulu",
    "Gizemli & Alaycı",
    "Sadık Sırdaş",
    "Tehlikeli Anti-Kahraman",
    "Zeki & Mesafeli"
)

private val UNIVERSE_SPARK_CHIPS = listOf(
    "Karanlık Gotik & Gizemli",
    "Destansı & Sinematik",
    "Politik Entrika & İhanet",
    "Sıcak & Büyülü Atmosfer"
)

private val OPENING_TONE_OPTIONS = listOf(
    "dramatic" to "Dramatik & Gerilimli",
    "warm" to "Sıcak & Samimi",
    "mysterious" to "Gizemli & Yavaş"
)

private fun serializeWizardKeyCharacters(list: List<KeyCharacter>, dropBlank: Boolean): String {
    val filtered = if (dropBlank) list.filter { it.name.isNotBlank() } else list
    if (filtered.isEmpty()) return "[]"
    val array = JSONArray()
    for (char in filtered) {
        val obj = JSONObject()
        obj.put("id", char.id)
        obj.put("name", char.name.trim())
        obj.put("desc", char.desc.trim())
        array.put(obj)
    }
    return array.toString()
}

private fun deserializeWizardKeyCharacters(json: String): List<KeyCharacter> {
    if (json.isBlank() || json == "[]") return emptyList()
    return try {
        val arr = JSONArray(json)
        val out = mutableListOf<KeyCharacter>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(
                KeyCharacter(
                    id = o.optString("id", UUID.randomUUID().toString()),
                    name = o.optString("name", ""),
                    desc = o.optString("desc", "")
                )
            )
        }
        out
    } catch (_: Exception) {
        emptyList()
    }
}

private fun isValidIsoStoryDate(input: String): Boolean {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return true
    if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(trimmed)) return false
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        sdf.parse(trimmed) != null
    } catch (_: Exception) {
        false
    }
}

private const val WIZARD_DRAFT_PREFS = "emochi_wizard_draft_v1"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateBotWizardScreen(
    baselineAnalysisState: EmochiViewModel.BaselineAnalysisUiState = EmochiViewModel.BaselineAnalysisUiState.Idle,
    isSubmitting: Boolean = false,
    submitError: String? = null,
    onClearSubmitError: () -> Unit = {},
    onBack: () -> Unit,
    onFinish: (BotEntity, List<SensitiveTriggerEntity>, (Boolean) -> Unit) -> Unit,
    onGenerateOpening: suspend (BotEntity, Boolean, String) -> String,
    onAnalyzeBaseline: (BotEntity, Boolean, Set<String>) -> Unit = { _, _, _ -> },
    onCancelBaseline: () -> Unit = {}
) {
    val context = LocalContext.current
    val draftPrefs = remember(context) {
        context.applicationContext.getSharedPreferences(WIZARD_DRAFT_PREFS, android.content.Context.MODE_PRIVATE)
    }

    // Fixed single draftBotId across the entire wizard lifecycle (fixes orphaned triggers bug)
    val draftBotId = rememberSaveable {
        draftPrefs.getString("draftBotId", null)?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
    }

    var stepIdx by rememberSaveable { mutableIntStateOf(draftPrefs.getInt("stepIdx", 0).coerceIn(0, 5)) }
    var mode by rememberSaveable { mutableStateOf(draftPrefs.getString("mode", "personal") ?: "personal") }
    var aiName by rememberSaveable { mutableStateOf(draftPrefs.getString("aiName", "") ?: "") }
    var aiPersonality by rememberSaveable { mutableStateOf(draftPrefs.getString("aiPersonality", "") ?: "") }
    var avatarUrl by rememberSaveable { mutableStateOf(draftPrefs.getString("avatarUrl", "") ?: "") }
    var pinnedMemory by rememberSaveable { mutableStateOf(draftPrefs.getString("pinnedMemory", "") ?: "") }
    var scenario by rememberSaveable { mutableStateOf(draftPrefs.getString("scenario", "") ?: "") }
    var universeName by rememberSaveable { mutableStateOf(draftPrefs.getString("universeName", "") ?: "") }
    var userCharName by rememberSaveable { mutableStateOf(draftPrefs.getString("userCharName", "") ?: "") }
    var userCharDesc by rememberSaveable { mutableStateOf(draftPrefs.getString("userCharDesc", "") ?: "") }
    var openingMessage by rememberSaveable { mutableStateOf(draftPrefs.getString("openingMessage", "") ?: "") }
    var selectedOpeningTone by rememberSaveable { mutableStateOf(draftPrefs.getString("selectedOpeningTone", "dramatic") ?: "dramatic") }
    var writingStyle by rememberSaveable { mutableStateOf(draftPrefs.getString("writingStyle", "rp") ?: "rp") }
    var intensity by rememberSaveable { mutableStateOf(draftPrefs.getString("intensity", "normal") ?: "normal") }
    var isPublic by rememberSaveable { mutableStateOf(draftPrefs.getBoolean("isPublic", false)) } // Default: Sadece Bana Özel
    var enableOoc by rememberSaveable { mutableStateOf(draftPrefs.getBoolean("enableOoc", true)) }
    var isNsfw by rememberSaveable { mutableStateOf(draftPrefs.getBoolean("isNsfw", true)) }
    var storyStartDate by rememberSaveable { mutableStateOf(draftPrefs.getString("storyStartDate", "") ?: "") }
    var keyCharactersSerialized by rememberSaveable { mutableStateOf(draftPrefs.getString("keyCharactersSerialized", "[]") ?: "[]") }
    var showPinnedMemorySection by rememberSaveable { mutableStateOf(false) }

    fun saveWizardDraftToPrefs() {
        draftPrefs.edit()
            .putString("draftBotId", draftBotId)
            .putInt("stepIdx", stepIdx)
            .putString("mode", mode)
            .putString("aiName", aiName)
            .putString("aiPersonality", aiPersonality)
            .putString("avatarUrl", avatarUrl)
            .putString("pinnedMemory", pinnedMemory)
            .putString("scenario", scenario)
            .putString("universeName", universeName)
            .putString("userCharName", userCharName)
            .putString("userCharDesc", userCharDesc)
            .putString("openingMessage", openingMessage)
            .putString("selectedOpeningTone", selectedOpeningTone)
            .putString("writingStyle", writingStyle)
            .putString("intensity", intensity)
            .putBoolean("isPublic", isPublic)
            .putBoolean("enableOoc", enableOoc)
            .putBoolean("isNsfw", isNsfw)
            .putString("storyStartDate", storyStartDate)
            .putString("keyCharactersSerialized", keyCharactersSerialized)
            .apply()
    }

    fun clearWizardDraftPrefs() {
        draftPrefs.edit().clear().apply()
    }

    val keyCharacters = remember(keyCharactersSerialized) {
        deserializeWizardKeyCharacters(keyCharactersSerialized)
    }
    fun updateKeyCharacters(updated: List<KeyCharacter>) {
        keyCharactersSerialized = serializeWizardKeyCharacters(updated, dropBlank = false)
    }

    // Baseline emotion analysis review state (Step 5)
    var analysisResult by remember { mutableStateOf<BaselineEmotionEngine.BaselineAnalysisResult?>(null) }
    var analyzedBotSnapshot by remember { mutableStateOf<BotEntity?>(null) }
    var analysisError by remember { mutableStateOf<String?>(null) }
    var userEditedFields by remember { mutableStateOf<Set<String>>(emptySet()) }
    var reviewRelType by remember { mutableStateOf("") }
    var reviewAttachment by remember { mutableStateOf("secure") }
    var reviewSpeechStyle by remember { mutableStateOf("") }
    var reviewDominantEmotion by remember { mutableStateOf("") }
    var reviewRegulationCapacity by remember { mutableIntStateOf(50) }
    var reviewTriggers by remember { mutableStateOf<List<SensitiveTriggerEntity>>(emptyList()) }
    var reviewBigFive by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var reviewAxes by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var isGeneratingOpening by remember { mutableStateOf(false) }
    var isProcessingAvatar by remember { mutableStateOf(false) }
    var localSubmitLock by remember { mutableStateOf(false) }
    var genError by remember { mutableStateOf<String?>(null) }
    var validationHint by remember { mutableStateOf<String?>(null) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    val effectiveSubmitting = isSubmitting || localSubmitLock
    val isAnalyzing = baselineAnalysisState is EmochiViewModel.BaselineAnalysisUiState.Loading

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val frostedBackdropState = com.example.util.rememberFrostedBackdrop(
        key = "gothic_manor_${R.drawable.img_botlist_gothic_wisteria_bg}",
        drawableResId = R.drawable.img_botlist_gothic_wisteria_bg,
        targetWidth = 360,
        maxHeight = 860,
        blurRadius = 4,
        blurPasses = 2,
        violetShiftWarmPixels = true,
        outputRgb565 = true
    )
    val frostedBackdrop = frostedBackdropState.value ?: com.example.util.FrostedBackdropCache.placeholderImageBitmap

    // Zero-permission Android Photo Picker -> OOM-safe 256x256 Micro-WebP (~15 KB Base64) on Dispatchers.IO with loading indicator
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { inputUri ->
            scope.launch {
                isProcessingAvatar = true
                try {
                    AvatarOptimizer.processAvatarToMicroWebPBase64(context, inputUri)
                        .onSuccess { microWebPBase64 ->
                            avatarUrl = microWebPBase64
                        }
                        .onFailure {
                            Toast.makeText(
                                context,
                                "Görsel okunamadı, lütfen başka bir fotoğraf seçin",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } finally {
                    isProcessingAvatar = false
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

    val hasUnsavedDraft = aiName.isNotBlank() ||
        aiPersonality.isNotBlank() ||
        universeName.isNotBlank() ||
        scenario.isNotBlank() ||
        userCharName.isNotBlank() ||
        openingMessage.isNotBlank() ||
        avatarUrl.isNotBlank() ||
        pinnedMemory.isNotBlank() ||
        keyCharacters.any { it.name.isNotBlank() || it.desc.isNotBlank() }

    val isStoryDateValid = isValidIsoStoryDate(storyStartDate)

    val canNext = when (stepIdx) {
        0 -> true
        1 -> if (mode == "personal") aiName.isNotBlank() && aiPersonality.isNotBlank() else universeName.isNotBlank()
        2 -> scenario.isNotBlank() && userCharName.isNotBlank()
        3 -> openingMessage.isNotBlank()
        4 -> !isAnalyzing
        5 -> isStoryDateValid
        else -> true
    }

    fun computeMissingHint(): String? = when (stepIdx) {
        1 -> if (mode == "personal") {
            when {
                aiName.isBlank() && aiPersonality.isBlank() -> "Devam etmek için Karakter Adı ve Kişilik alanlarını doldurun."
                aiName.isBlank() -> "Devam etmek için Karakter Adı girin."
                aiPersonality.isBlank() -> "Devam etmek için Kişilik ve Özellikler alanını doldurun."
                else -> null
            }
        } else {
            if (universeName.isBlank()) "Devam etmek için Evren Adı girin." else null
        }
        2 -> when {
            scenario.isBlank() && userCharName.isBlank() -> "Devam etmek için Senaryo ve Karakterinizin Adını girin."
            scenario.isBlank() -> "Devam etmek için Senaryo / Bağlam alanını doldurun."
            userCharName.isBlank() -> "Devam etmek için canlandıracağınız karakterin adını girin."
            else -> null
        }
        3 -> if (openingMessage.isBlank()) "Devam etmek için bir açılış sahnesi yazın veya AI ile üretin." else null
        4 -> if (isAnalyzing) "Analiz tamamlanana kadar lütfen bekleyin." else null
        5 -> if (!isStoryDateValid) "Başlangıç tarihi yyyy-AA-gg biçiminde olmalı (örn. 2045-06-15) veya boş bırakılmalı." else null
        else -> null
    }

    // Clear soft validation hint whenever user edits inputs or moves steps
    LaunchedEffect(stepIdx, mode, aiName, aiPersonality, universeName, scenario, userCharName, openingMessage, storyStartDate) {
        validationHint = null
    }

    LaunchedEffect(stepIdx) {
        scrollState.scrollTo(0)
    }

    fun applyAnalysisResult(res: BaselineEmotionEngine.BaselineAnalysisResult, preserveEdits: Boolean) {
        analysisResult = res
        val edited = if (preserveEdits) userEditedFields else emptySet()
        if ("relationshipType" !in edited || reviewRelType.isBlank()) {
            reviewRelType = res.relationshipType
        }
        if ("attachmentStyle" !in edited) {
            reviewAttachment = BaselineEmotionEngine.normalizeAttachment(res.personalityProfile.attachmentStyle)
        }
        if ("speechStyle" !in edited || reviewSpeechStyle.isBlank()) {
            reviewSpeechStyle = res.speechPattern
        }
        if ("dominantEmotion" !in edited || reviewDominantEmotion.isBlank()) {
            reviewDominantEmotion = res.baselineEmotionState.dominantEmotion
        }
        if ("regulationCapacity" !in edited) {
            reviewRegulationCapacity = res.regulationCapacity.coerceIn(10, 100)
        }
        if ("sensitiveTriggers" !in edited || reviewTriggers.isEmpty()) {
            reviewTriggers = res.sensitiveTriggers.map { it.copy(botId = draftBotId) }
        }
        if ("bigFive" !in edited || reviewBigFive.isEmpty()) {
            reviewBigFive = mapOf(
                "openness" to res.personalityProfile.openness,
                "conscientiousness" to res.personalityProfile.conscientiousness,
                "extraversion" to res.personalityProfile.extraversion,
                "agreeableness" to res.personalityProfile.agreeableness,
                "neuroticism" to res.personalityProfile.neuroticism
            )
        }
        if ("axes" !in edited || reviewAxes.isEmpty()) {
            reviewAxes = mapOf(
                "affection" to res.baselineEmotionState.affection,
                "respect" to res.baselineEmotionState.relationshipAxes.respectScore,
                "comfort" to res.baselineEmotionState.relationshipAxes.comfortScore,
                "resentment" to res.baselineEmotionState.relationshipAxes.resentmentScore
            )
        }
    }

    // Observe unified ViewModel baselineAnalysisState
    LaunchedEffect(baselineAnalysisState) {
        when (val st = baselineAnalysisState) {
            is EmochiViewModel.BaselineAnalysisUiState.Loading -> {
                analysisError = null
            }
            is EmochiViewModel.BaselineAnalysisUiState.Success -> {
                analysisError = null
                analyzedBotSnapshot = st.updatedBot
                if (st.resetUserEdits) {
                    userEditedFields = emptySet()
                }
                applyAnalysisResult(st.result, preserveEdits = !st.resetUserEdits)
            }
            is EmochiViewModel.BaselineAnalysisUiState.Error -> {
                analysisError = st.message
                applyAnalysisResult(st.fallbackResult, preserveEdits = true)
            }
            is EmochiViewModel.BaselineAnalysisUiState.Idle -> Unit
        }
    }

    fun buildCurrentDraft(cleanBlankCast: Boolean = false): BotEntity {
        val serializedCast = serializeWizardKeyCharacters(keyCharacters, dropBlank = cleanBlankCast)
        val effectiveName = if (mode == "personal") aiName.trim() else universeName.trim()
        val baselineEmotion = EmotionState.calculateBaselineEmotionState(
            aiName = effectiveName,
            personality = aiPersonality.trim(),
            scenario = scenario.trim(),
            userCharName = userCharName.trim(),
            userCharDesc = userCharDesc.trim()
        ).toJson()

        val initialWorldAtmosphere = if (mode == "universe") {
            val dynamicFromAnalysis = analyzedBotSnapshot?.worldAtmosphere?.takeIf { it.isNotBlank() }
            if (dynamicFromAnalysis != null) {
                dynamicFromAnalysis
            } else {
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
            }
        } else ""

        val cleanDate = storyStartDate.trim().takeIf { it.isNotBlank() && isValidIsoStoryDate(it) } ?: ""

        return BotEntity(
            id = draftBotId,
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
            isNsfw = isNsfw,
            isPublic = isPublic,
            enableOoc = enableOoc,
            emotionState = baselineEmotion,
            previousEmotionState = baselineEmotion,
            worldAtmosphere = initialWorldAtmosphere,
            storyCalendarDate = cleanDate,
            hasExplicitStoryDate = cleanDate.isNotBlank(),
            baselineInputHash = analyzedBotSnapshot?.baselineInputHash ?: "",
            baselineEmotionAnalysisJson = analyzedBotSnapshot?.baselineEmotionAnalysisJson ?: "",
            updatedAt = System.currentTimeMillis()
        )
    }

    fun triggerBaselineAnalysis(forceReanalyze: Boolean) {
        if (forceReanalyze) {
            userEditedFields = emptySet()
        }
        val draft = buildCurrentDraft(cleanBlankCast = true)
        onAnalyzeBaseline(draft, forceReanalyze, if (forceReanalyze) emptySet() else userEditedFields)
    }

    fun handleBackAction() {
        if (stepIdx == 4 && isAnalyzing) {
            onCancelBaseline()
        }
        if (stepIdx > 0) {
            stepIdx--
        } else if (hasUnsavedDraft) {
            showExitConfirmDialog = true
        } else {
            onBack()
        }
    }

    fun advanceToStep(targetStep: Int) {
        if (targetStep == stepIdx) return
        if (stepIdx == 4 && isAnalyzing) {
            onCancelBaseline()
        }
        if (stepIdx == 1 && mode == "universe") {
            // Auto-clean empty cast rows when leaving Step 2
            val cleaned = keyCharacters.filter { it.name.isNotBlank() || it.desc.isNotBlank() }
            if (cleaned.size != keyCharacters.size) {
                updateKeyCharacters(cleaned)
            }
        }
        stepIdx = targetStep
        if (targetStep == 4) {
            triggerBaselineAnalysis(forceReanalyze = false)
        }
    }

    BackHandler {
        handleBackAction()
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
        AppBackground(activeTab = "chats")

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
            modifier = Modifier.fillMaxSize().imePadding(),
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
                    onBackClick = { handleBackAction() },
                    onStepSelect = { target ->
                        if (target < stepIdx) {
                            advanceToStep(target)
                        } else if (target == stepIdx + 1 && canNext) {
                            advanceToStep(target)
                        }
                    },
                    canJumpToStep = { target ->
                        target < stepIdx || (target == stepIdx + 1 && canNext)
                    }
                )
            },
            bottomBar = {
                WizardMorCamBottomDock(
                    stepIdx = stepIdx,
                    totalSteps = totalSteps,
                    canNext = canNext && !effectiveSubmitting,
                    isSubmitting = effectiveSubmitting,
                    frostedBackdrop = frostedBackdrop,
                    scrollSync = scrollState.value,
                    glowAlpha = pulseGlowAlpha,
                    validationHint = submitError ?: validationHint,
                    onDisabledNextClick = {
                        if (!effectiveSubmitting) {
                            validationHint = computeMissingHint()
                        }
                    },
                    onBack = {
                        if (!effectiveSubmitting) handleBackAction()
                    },
                    onNext = {
                        if (effectiveSubmitting) return@WizardMorCamBottomDock
                        when {
                            stepIdx < totalSteps - 1 -> {
                                saveWizardDraftToPrefs()
                                advanceToStep(stepIdx + 1)
                            }
                            else -> {
                                localSubmitLock = true
                                onClearSubmitError()
                                val base = buildCurrentDraft(cleanBlankCast = true)
                                val res = analysisResult
                                val finalTriggers = if (reviewTriggers.isNotEmpty()) {
                                    reviewTriggers
                                        .filter { it.triggerTopic.isNotBlank() }
                                        .map { it.copy(botId = draftBotId) }
                                } else {
                                    res?.sensitiveTriggers?.map { it.copy(botId = draftBotId) } ?: emptyList()
                                }

                                val finalBot = if (res != null) {
                                    val editedAxes = RelationshipAxes(
                                        affectionScore = reviewAxes["affection"] ?: res.baselineEmotionState.affection,
                                        respectScore = reviewAxes["respect"] ?: res.baselineEmotionState.relationshipAxes.respectScore,
                                        comfortScore = reviewAxes["comfort"] ?: res.baselineEmotionState.relationshipAxes.comfortScore,
                                        resentmentScore = reviewAxes["resentment"] ?: res.baselineEmotionState.relationshipAxes.resentmentScore
                                    )
                                    val editedProfile = PersonalityProfile(
                                        openness = reviewBigFive["openness"] ?: res.personalityProfile.openness,
                                        conscientiousness = reviewBigFive["conscientiousness"] ?: res.personalityProfile.conscientiousness,
                                        extraversion = reviewBigFive["extraversion"] ?: res.personalityProfile.extraversion,
                                        agreeableness = reviewBigFive["agreeableness"] ?: res.personalityProfile.agreeableness,
                                        neuroticism = reviewBigFive["neuroticism"] ?: res.personalityProfile.neuroticism,
                                        attachmentStyle = BaselineEmotionEngine.normalizeAttachment(reviewAttachment)
                                    )
                                    val editedState = res.baselineEmotionState.copy(
                                        relationshipAxes = editedAxes,
                                        dominantEmotion = reviewDominantEmotion.ifBlank { res.baselineEmotionState.dominantEmotion },
                                        speechPattern = reviewSpeechStyle.ifBlank { res.speechPattern }
                                    )
                                    val finalSource = if (userEditedFields.isNotEmpty()) "user_edited" else res.analysisSource
                                    val finalAnalysisResult = res.copy(
                                        relationshipType = reviewRelType.ifBlank { res.relationshipType },
                                        personalityProfile = editedProfile,
                                        regulationCapacity = reviewRegulationCapacity.coerceIn(10, 100),
                                        baselineEmotionState = editedState,
                                        sensitiveTriggers = finalTriggers,
                                        speechPattern = reviewSpeechStyle.ifBlank { res.speechPattern },
                                        analysisSource = finalSource
                                    )
                                    val emotionJson = editedState.toJson()
                                    val canonicalJson = finalAnalysisResult.toCanonicalJson()
                                    base.copy(
                                        emotionState = emotionJson,
                                        previousEmotionState = emotionJson,
                                        baseAffectionDifficulty = finalAnalysisResult.difficultyMultiplier,
                                        emotionalRegulationCapacity = finalAnalysisResult.regulationCapacity,
                                        baselineEmotionAnalysisJson = canonicalJson,
                                        baselineAnalysisSource = finalSource,
                                        baselineUserEditedFields = userEditedFields.joinToString(",")
                                    )
                                } else {
                                    base
                                }
                                onFinish(finalBot, finalTriggers) { succeeded ->
                                    localSubmitLock = false
                                    if (succeeded) {
                                        // Sistem #3: Draft cleared ONLY after atomic DB transaction succeeds
                                        clearWizardDraftPrefs()
                                    } else {
                                        // Preserve user work on DB error
                                        saveWizardDraftToPrefs()
                                    }
                                }
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            val topInset = innerPadding.calculateTopPadding()
            val bottomInset = innerPadding.calculateBottomPadding()
            val density = LocalDensity.current
            var viewportTopInWindowPx by remember { mutableFloatStateOf(0f) }
            var viewportBottomInWindowPx by remember { mutableFloatStateOf(0f) }

            val focusScrollHost = remember(scrollState) {
                WizardFocusScrollHost(scrollState = scrollState)
            }
            val topInsetPx = with(density) { topInset.toPx() }
            val bottomInsetPx = with(density) { bottomInset.toPx() }
            focusScrollHost.safeTopInWindowPx = viewportTopInWindowPx + topInsetPx
            focusScrollHost.safeBottomInWindowPx = (viewportBottomInWindowPx - bottomInsetPx)
                .coerceAtLeast(viewportTopInWindowPx + topInsetPx)

            CompositionLocalProvider(LocalWizardFocusScrollHost provides focusScrollHost) {
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
                        .onGloballyPositioned { coords ->
                            val rect = coords.boundsInWindow()
                            viewportTopInWindowPx = rect.top
                            viewportBottomInWindowPx = rect.bottom
                        }
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        .drawWithContent {
                            drawContent()
                            val h = size.height
                            if (h > 4f) {
                                val topPx = with(density) { topInset.toPx() }.coerceIn(0f, h * 0.45f)
                                val bottomPx = with(density) { bottomInset.toPx() }.coerceIn(0f, h * 0.72f)
                                val topFadePx = with(density) { 20.dp.toPx() }
                                val bottomFadePx = with(density) { 36.dp.toPx() }
                                val bottomBoundary = (h - bottomPx).coerceAtLeast(topPx + 4f)

                                val s0 = ((topPx - with(density) { 4.dp.toPx() }) / h).coerceIn(0f, 0.96f)
                                val s1 = ((topPx + topFadePx) / h).coerceIn(s0, 0.97f)
                                val s2 = ((bottomBoundary - bottomFadePx) / h).coerceIn(s1, 0.98f)
                                val s3 = ((bottomBoundary + with(density) { 10.dp.toPx() }) / h).coerceIn(s2, 1f)

                                drawRect(
                                    brush = Brush.verticalGradient(
                                        0f to Color.Transparent,
                                        s0 to Color.Transparent,
                                        s1 to Color.Black,
                                        s2 to Color.Black,
                                        s3 to Color.Transparent,
                                        1f to Color.Transparent
                                    ),
                                    blendMode = BlendMode.DstIn
                                )
                            }
                        },
                    label = "wizardStepTransition"
                ) { currentStep ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Spacer(modifier = Modifier.height(topInset + 10.dp))

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
                                iconVector = Icons.AutoMirrored.Filled.MenuBook,
                                isSelected = mode == "universe",
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                testTag = "wizard_mode_universe",
                                onClick = { mode = "universe" }
                            )

                            Spacer(modifier = Modifier.height(18.dp))

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

                                WizardAvatarPickerCard(
                                    title = "Bot Profil Fotoğrafı",
                                    emptySubtitle = "Galeriden özel bir portre veya kapak seçin.",
                                    avatarUrl = avatarUrl,
                                    isProcessing = isProcessingAvatar,
                                    frostedBackdrop = frostedBackdrop,
                                    scrollSync = scrollState.value,
                                    onPickClick = {
                                        if (!isProcessingAvatar) {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    },
                                    onRemoveClick = { avatarUrl = "" }
                                )

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
                                    minHeight = 136.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ai_personality_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "✦ Hızlı Kişilik Kıvılcımları",
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFD8B4FE),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    PERSONAL_SPARK_CHIPS.forEach { spark ->
                                        val isIncluded = aiPersonality.contains(spark, ignoreCase = true)
                                        WizardMorCamPillChip(
                                            label = if (isIncluded) "✓ $spark" else "+ $spark",
                                            selected = isIncluded,
                                            onClick = {
                                                aiPersonality = if (isIncluded) {
                                                    aiPersonality
                                                        .replace(", $spark", "", ignoreCase = true)
                                                        .replace("$spark, ", "", ignoreCase = true)
                                                        .replace(spark, "", ignoreCase = true)
                                                        .trim()
                                                } else {
                                                    if (aiPersonality.isBlank()) spark else "${aiPersonality.trim().trimEnd(',', '.')}, $spark"
                                                }
                                            }
                                        )
                                    }
                                }
                            } else {
                                WizardStepHeader(
                                    title = "Evren ve karakter\nkadrosunu kurun",
                                    subtitle = "Kurgusal dünyanın kapağını, adını, anlatıcı kurallarını ve kadrosunu belirleyin.",
                                    glowAlpha = pulseGlowAlpha
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                WizardAvatarPickerCard(
                                    title = "Evren Kapak Görseli",
                                    emptySubtitle = "Dünyanızın atmosferini yansıtan bir kapak seçin.",
                                    avatarUrl = avatarUrl,
                                    isProcessing = isProcessingAvatar,
                                    frostedBackdrop = frostedBackdrop,
                                    scrollSync = scrollState.value,
                                    onPickClick = {
                                        if (!isProcessingAvatar) {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    },
                                    onRemoveClick = { avatarUrl = "" }
                                )

                                Spacer(modifier = Modifier.height(18.dp))

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

                                Spacer(modifier = Modifier.height(18.dp))

                                WizardFieldLabel(title = "Evren Kuralları & Anlatıcı Üslubu (Opsiyonel)", required = false)
                                Spacer(modifier = Modifier.height(8.dp))
                                WizardMorCamTextField(
                                    value = aiPersonality,
                                    onValueChange = { aiPersonality = it },
                                    placeholder = "Büyü sistemi, toplumsal hiyerarşi, anlatıcının tonu ve dünya kuralları...",
                                    singleLine = false,
                                    frostedBackdrop = frostedBackdrop,
                                    scrollSync = scrollState.value,
                                    minHeight = 112.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("universe_rules_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    UNIVERSE_SPARK_CHIPS.forEach { spark ->
                                        val isIncluded = aiPersonality.contains(spark, ignoreCase = true)
                                        WizardMorCamPillChip(
                                            label = if (isIncluded) "✓ $spark" else "+ $spark",
                                            selected = isIncluded,
                                            onClick = {
                                                aiPersonality = if (isIncluded) {
                                                    aiPersonality
                                                        .replace(", $spark", "", ignoreCase = true)
                                                        .replace("$spark, ", "", ignoreCase = true)
                                                        .replace(spark, "", ignoreCase = true)
                                                        .trim()
                                                } else {
                                                    if (aiPersonality.isBlank()) spark else "${aiPersonality.trim().trimEnd(',', '.')}, $spark"
                                                }
                                            }
                                        )
                                    }
                                }

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
                                                    val next = keyCharacters.toMutableList().apply {
                                                        this[idx] = this[idx].copy(name = newName)
                                                    }
                                                    updateKeyCharacters(next)
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
                                                    val next = keyCharacters.toMutableList().apply {
                                                        this[idx] = this[idx].copy(desc = newDesc)
                                                    }
                                                    updateKeyCharacters(next)
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
                                                    val next = keyCharacters.toMutableList().apply { removeAt(idx) }
                                                    updateKeyCharacters(next)
                                                },
                                                size = 38.dp,
                                                isDestructive = true
                                            )
                                        }
                                    }
                                }

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
                                            updateKeyCharacters(keyCharacters + KeyCharacter(name = "", desc = ""))
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
                                minHeight = 104.dp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Collapsible Pinned Memory / World Rule section
                            WizardMorCamSectionCard(
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                modifier = Modifier.testTag("wizard_pinned_memory_card")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showPinnedMemorySection = !showPinnedMemorySection }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "✦ Sabit Hafıza / Evren Kuralı Ekle (Opsiyonel)",
                                            fontFamily = LiterarySerifFontFamily,
                                            color = Color(0xFFFAF6FF),
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (pinnedMemory.isNotBlank() && !showPinnedMemorySection) {
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = "Sabit kural eklendi (${pinnedMemory.length} karakter)",
                                                fontFamily = InterFontFamily,
                                                color = Color(0xFFD8B4FE),
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (showPinnedMemorySection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (showPinnedMemorySection) "Daralt" else "Genişlet",
                                        tint = Color(0xFFD8B4FE),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = showPinnedMemorySection,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        Text(
                                            text = "Botun asla unutmaması gereken çekirdek gerçekler, yasaklar veya dünyaya dair değişmez kurallar.",
                                            fontFamily = InterFontFamily,
                                            color = Color(0xFFC2B2E4),
                                            fontSize = 11.8.sp,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        WizardMorCamTextField(
                                            value = pinnedMemory,
                                            onValueChange = { pinnedMemory = it },
                                            placeholder = "Ör. Karakter asla gerçek adını ilk buluşmada söylemez...",
                                            singleLine = false,
                                            frostedBackdrop = frostedBackdrop,
                                            scrollSync = scrollState.value,
                                            minHeight = 96.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("wizard_pinned_memory_input")
                                        )
                                    }
                                }
                            }
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
                                    onValueChange = { if (it.length <= 1500) openingMessage = it },
                                    placeholder = "Açılış mesajını kendiniz yazın veya aşağıdaki yapay zeka kartını kullanın...",
                                    singleLine = false,
                                    frostedBackdrop = frostedBackdrop,
                                    scrollSync = scrollState.value,
                                    minHeight = 190.dp,
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
                                        text = "${openingMessage.length} / 1500",
                                        fontFamily = InterFontFamily,
                                        color = Color(0xFFE9D5FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "✦ Açılış Sahnesi Tonu",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFD8B4FE),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OPENING_TONE_OPTIONS.forEach { (toneKey, toneLabel) ->
                                    WizardMorCamPillChip(
                                        label = toneLabel,
                                        selected = selectedOpeningTone == toneKey,
                                        onClick = { selectedOpeningTone = toneKey }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val hasExistingOpening = openingMessage.isNotBlank()
                            WizardMorCamChoiceCard(
                                title = when {
                                    isGeneratingOpening -> "Yapay Zeka Sahneyi Yazıyor..."
                                    hasExistingOpening -> "Alternatif Açılış Üret ✦"
                                    else -> "AI Otomatik Yazsın ✦"
                                },
                                subtitle1 = if (hasExistingOpening) {
                                    "Mevcut metni tekrarlamadan seçili tonda yepyeni bir açılış üret."
                                } else {
                                    "Senaryo ve karakter kişiliğine uygun sinematik açılış."
                                },
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
                                                val generated = onGenerateOpening(
                                                    buildCurrentDraft(cleanBlankCast = true),
                                                    true,
                                                    selectedOpeningTone
                                                )
                                                openingMessage = generated.take(1500)
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
                            WizardBaselineReviewStep(
                                draftBotId = draftBotId,
                                mode = mode,
                                isAnalyzing = isAnalyzing,
                                analysisError = analysisError,
                                analysisResult = analysisResult,
                                userEditedFields = userEditedFields,
                                reviewRelType = reviewRelType,
                                reviewAttachment = reviewAttachment,
                                reviewSpeechStyle = reviewSpeechStyle,
                                reviewDominantEmotion = reviewDominantEmotion,
                                reviewRegulationCapacity = reviewRegulationCapacity,
                                reviewTriggers = reviewTriggers,
                                reviewBigFive = reviewBigFive,
                                reviewAxes = reviewAxes,
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                pulseGlowAlpha = pulseGlowAlpha,
                                onRelTypeChange = { rt ->
                                    reviewRelType = rt
                                    userEditedFields = userEditedFields + "relationshipType"
                                    if ("axes" !in userEditedFields) {
                                        val suggested = BaselineEmotionEngine.defaultAxesForRelType(rt)
                                        reviewAxes = mapOf(
                                            "affection" to suggested.affectionScore,
                                            "respect" to suggested.respectScore,
                                            "comfort" to suggested.comfortScore,
                                            "resentment" to suggested.resentmentScore
                                        )
                                    }
                                },
                                onAttachmentChange = { att ->
                                    reviewAttachment = BaselineEmotionEngine.normalizeAttachment(att)
                                    userEditedFields = userEditedFields + "attachmentStyle"
                                },
                                onDominantEmotionChange = { dom ->
                                    reviewDominantEmotion = dom
                                    userEditedFields = userEditedFields + "dominantEmotion"
                                },
                                onSpeechStyleChange = { style ->
                                    reviewSpeechStyle = style
                                    userEditedFields = userEditedFields + "speechStyle"
                                },
                                onRegulationCapacityChange = { cap ->
                                    reviewRegulationCapacity = cap.coerceIn(10, 100)
                                    userEditedFields = userEditedFields + "regulationCapacity"
                                },
                                onAxisChange = { key, value ->
                                    reviewAxes = reviewAxes + (key to value.coerceIn(0, 100))
                                    userEditedFields = userEditedFields + "axes" + "emotionState"
                                },
                                onBigFiveChange = { key, value ->
                                    reviewBigFive = reviewBigFive + (key to value.coerceIn(0, 100))
                                    userEditedFields = userEditedFields + "bigFive"
                                },
                                onTriggersChange = { nextList ->
                                    reviewTriggers = nextList.map { it.copy(botId = draftBotId) }
                                    userEditedFields = userEditedFields + "sensitiveTriggers"
                                },
                                onReanalyzeClick = {
                                    triggerBaselineAnalysis(forceReanalyze = true)
                                },
                                onResetToDefaultClick = {
                                    val effectiveName = if (mode == "personal") aiName else universeName
                                    val fb = BaselineEmotionEngine.calculateDeterministicFallback(
                                        botId = draftBotId,
                                        aiName = effectiveName,
                                        personality = aiPersonality,
                                        scenario = scenario,
                                        userCharName = userCharName,
                                        userCharDesc = userCharDesc
                                    )
                                    userEditedFields = emptySet()
                                    analysisError = null
                                    applyAnalysisResult(fb, preserveEdits = false)
                                }
                            )
                        }

                        // ── STEP 6 / 6: WRITING STYLE, TONE, PERMISSIONS & PUBLISH ─
                        5 -> {
                            WizardStepHeader(
                                title = "Yazım tarzı ve\ngörünürlük ayarları",
                                subtitle = "Yanıtların biçimini, tematik yoğunluğunu, kuralları ve gizliliğini seçin.",
                                glowAlpha = pulseGlowAlpha
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            WizardFieldLabel(title = "Yazım Formatı", required = false)
                            Spacer(modifier = Modifier.height(10.dp))

                            WizardMorCamChoiceCard(
                                title = "RP / Anlatı Tarzı",
                                subtitle1 = "Üçüncü şahıs roman anlatımı.",
                                subtitle2 = "Ortam ve mimik betimlemeleri + diyaloglar.",
                                iconVector = Icons.AutoMirrored.Filled.MenuBook,
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

                            // Story Start Date with live yyyy-MM-dd validation + "Bugünü Kullan" chip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                WizardFieldLabel(title = "Hikâye Başlangıç Tarihi", required = false)
                                WizardMorCamPillChip(
                                    label = "Bugünü Kullan",
                                    selected = false,
                                    onClick = {
                                        storyStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                                    },
                                    modifier = Modifier.testTag("wizard_use_today_date_button")
                                )
                            }
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wizard_story_date_input")
                            )
                            if (!isStoryDateValid) {
                                Text(
                                    text = "⚠ Geçersiz tarih biçimi. Lütfen yyyy-AA-gg girin (örn. 2026-05-14).",
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 11.8.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // OOC & NSFW Switches
                            WizardFieldLabel(title = "Etkileşim Kuralları & İzinler", required = false)
                            Spacer(modifier = Modifier.height(10.dp))

                            MorCamSwitchRow(
                                title = "Parantez İçi Yönetmen Komutları (OOC)",
                                subtitle = "Sohbette (sahneyi hızlandır) gibi parantez içi yönlendirmelere izin ver.",
                                checked = enableOoc,
                                onCheckedChange = { enableOoc = it },
                                modifier = Modifier.testTag("wizard_ooc_switch")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            MorCamSwitchRow(
                                title = "Sansürsüz Kurgu (+18)",
                                subtitle = "Yetişkin ve olgun dramatik temalarda yumuşatma yapma.",
                                checked = isNsfw,
                                onCheckedChange = { isNsfw = it },
                                badge = "+18",
                                modifier = Modifier.testTag("wizard_nsfw_switch")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            WizardFieldLabel(title = "Görünürlük & Yayınlama", required = false)
                            Spacer(modifier = Modifier.height(10.dp))

                            WizardMorCamChoiceCard(
                                title = "Sadece Bana Özel",
                                subtitle1 = "Tamamen gizli ve kişisel (Varsayılan).",
                                subtitle2 = "Bot yalnızca sizin cihazınızda ve sohbetlerinizde gözükür.",
                                iconVector = Icons.Default.Lock,
                                isSelected = !isPublic,
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                testTag = "wizard_visibility_private",
                                onClick = { isPublic = false }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            WizardMorCamChoiceCard(
                                title = "Herkese Açık (Keşfette Yayınla)",
                                subtitle1 = "Toplulukla paylaşılır.",
                                subtitle2 = "Botunuz Keşfet sekmesinde diğer kullanıcılara görünür.",
                                iconVector = Icons.Default.Public,
                                isSelected = isPublic,
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                glowAlpha = pulseGlowAlpha,
                                testTag = "wizard_visibility_public",
                                onClick = { isPublic = true }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Final 3-Line Mor Cam Summary Preview Card before "Botu Oluştur"
                            val displayName = if (mode == "personal") aiName.trim().ifBlank { "İsimsiz Karakter" } else universeName.trim().ifBlank { "İsimsiz Evren" }
                            val modeLabel = if (mode == "personal") "Kişisel Karakter" else "Evren / RP"
                            val relLabel = WIZARD_REL_TYPE_LABELS[reviewRelType] ?: "Dengeli"
                            val domLabel = reviewDominantEmotion.ifBlank { "Nötr" }
                            val trigCount = reviewTriggers.count { it.triggerTopic.isNotBlank() }

                            WizardMorCamSectionCard(
                                title = "✦ Oluşturma Özeti",
                                frostedBackdrop = frostedBackdrop,
                                scrollSync = scrollState.value,
                                modifier = Modifier.testTag("wizard_final_summary_card")
                            ) {
                                Text(
                                    text = "$displayName  •  $modeLabel",
                                    fontFamily = DisplaySerifFontFamily,
                                    color = Color(0xFFFAF6FF),
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "İlişki: $relLabel  •  Baskın Duygu: $domLabel",
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFE5D8FF),
                                    fontSize = 12.5.sp
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = "Hassas Konu: $trigCount adet  •  ${if (isPublic) "Herkese Açık" else "Sadece Bana Özel"}  •  OOC: ${if (enableOoc) "Açık" else "Kapalı"}",
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFC4B5FD),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                        Spacer(modifier = Modifier.height(bottomInset + 36.dp))
                    }
                }
            }
        }

        // Sistem #3: 3-Option Draft Protection Dialog on Step 1 BackHandler
        if (showExitConfirmDialog) {
            MorCamDialogShell(
                onDismissRequest = { showExitConfirmDialog = false },
                title = "Taslağı Kaydetmek İster misin?",
                subtitle = "Henüz botu oluşturmadın. Taslağı kaydedip sonra kaldığın yerden devam edebilirsin."
            ) {
                Spacer(Modifier.height(14.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(Color(0xAA7C3AED), Color(0xAA5B21B6))))
                            .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(50))
                            .clickable {
                                showExitConfirmDialog = false
                                saveWizardDraftToPrefs()
                                onCancelBaseline()
                                onBack()
                            }
                            .testTag("wizard_exit_save_draft_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Taslağı Kaydet & Çık",
                            fontFamily = LiterarySerifFontFamily,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x552E1559))
                                .border(1.dp, Color(0xAAE9D5FF), RoundedCornerShape(50))
                                .clickable { showExitConfirmDialog = false }
                                .testTag("wizard_exit_continue_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Devam Et",
                                fontFamily = LiterarySerifFontFamily,
                                color = Color(0xFFFAF6FF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x887F1D1D))
                                .border(1.dp, Color(0xCCFDA4AF), RoundedCornerShape(50))
                                .clickable {
                                    showExitConfirmDialog = false
                                    clearWizardDraftPrefs()
                                    onCancelBaseline()
                                    onBack()
                                }
                                .testTag("wizard_exit_discard_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Taslağı Sil",
                                fontFamily = LiterarySerifFontFamily,
                                color = Color(0xFFFFE4E6),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardAvatarPickerCard(
    title: String,
    emptySubtitle: String,
    avatarUrl: String,
    isProcessing: Boolean = false,
    frostedBackdrop: ImageBitmap,
    scrollSync: Int,
    onPickClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    WizardMorCamSectionCard(
        frostedBackdrop = frostedBackdrop,
        scrollSync = scrollSync,
        modifier = Modifier
            .clickable(
                enabled = !isProcessing,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onPickClick
            )
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
                if (isProcessing) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.2.dp,
                        color = Color(0xFFE9D5FF)
                    )
                } else {
                    BotAvatarImage(
                        avatarUrl = avatarUrl.takeIf { it.isNotBlank() },
                        contentDescription = title,
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
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = DisplaySerifFontFamily,
                    color = Color(0xFFFAF6FF),
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = when {
                        isProcessing -> "Görsel optimize ediliyor..."
                        avatarUrl.isNotBlank() -> "Görsel seçildi • Değiştirmek için dokun"
                        else -> emptySubtitle
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
                    onClick = onRemoveClick,
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
}
