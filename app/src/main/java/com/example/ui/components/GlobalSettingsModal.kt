@file:OptIn(ExperimentalLayoutApi::class)
package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.example.ui.animation.CosmicAnimatedCounter
import com.example.ui.animation.CosmicMotionPreferences
import com.example.ui.animation.CosmicMotionPreviewStrip
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.MotionIntensityMode
import com.example.ui.animation.cosmicGlassSheen
import com.example.ui.animation.cosmicStaggeredEnter
import com.example.ui.animation.cosmicTapSpring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.theme.LiterarySerifFontFamily
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.zIndex
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.CustomProviderEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.managers.ApiPriorityCodec
import com.example.data.managers.ApiProviderCatalog
import com.example.data.managers.CatalogProviderSpec
import com.example.data.repository.EmochiRepository
import com.example.ui.screens.LocalAetheriaRootPos
import com.example.ui.screens.LocalAetheriaRootSize
import com.example.ui.screens.aetheriaBackdropBlur
import com.example.ui.screens.createFrostedWisteriaBackdropBitmap
import com.example.ui.screens.morCamGlassSurface
import com.example.ui.screens.morCamOuterGlow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import kotlinx.coroutines.launch

private data class ActiveApiItemUi(
    val key: String, // "gemini", "groq", ..., or "custom_12"
    val name: String,
    val model: String,
    val subtitle: String,
    val apiFormat: String,
    val baseUrl: String,
    val apiKeyPlain: String,
    val builtinSlot: String? = null,
    val customEntity: CustomProviderEntity? = null
)

@Composable
fun GlobalSettingsModal(
    settings: UserSettingsEntity,
    onDismiss: () -> Unit,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    onExportData: suspend (Boolean) -> String,
    onImportData: suspend (String, Boolean) -> com.example.data.repository.BackupImportSummary,
    viewModel: com.example.ui.viewmodel.EmochiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    BackHandler { onDismiss() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val modalRootSizeState = remember { mutableStateOf(IntSize.Zero) }
    val modalRootPosState = remember { mutableStateOf(Offset.Zero) }
    val rootSize = modalRootSizeState.value
    val blurredBgState = com.example.util.rememberFrostedBackdrop(
        key = "global_settings_cosmic_bg",
        drawableResId = R.drawable.img_global_settings_cosmic_bg,
        targetWidth = 360,
        maxHeight = 1080,
        blurRadius = 4,
        blurPasses = 2,
        outputRgb565 = false
    )
    val blurredBg = blurredBgState.value ?: com.example.util.FrostedBackdropCache.placeholderImageBitmap
    val refPlates = remember(context) { loadGlobalSettingsRefPlates(context) }
    val motionConfig = LocalCosmicMotionConfig.current
    val currentMotionMode by CosmicMotionPreferences.mode.collectAsState()
    val refusalAutoFallback by com.example.util.AppFeatureFlags.refusalAutoFallback.collectAsState()
    val strictApiTimeout by com.example.util.AppFeatureFlags.strictApiTimeout.collectAsState()
    val ttsEngineMode by com.example.util.AppFeatureFlags.ttsEngineMode.collectAsState()
    val ttsEmotionalProsody by com.example.util.AppFeatureFlags.ttsEmotionalProsody.collectAsState()
    val ttsNeuralVoice by com.example.util.AppFeatureFlags.ttsNeuralVoice.collectAsState()
    val aiUtilityModelsMap by com.example.util.AppFeatureFlags.aiUtilityModels.collectAsState()
    val isSelectingUtilityKey by viewModel.isSelectingUtilityModel.collectAsState()
    val utilitySelectionFeedback by viewModel.utilityModelSelectionMessage.collectAsState()
    val isTurkishVoiceMissing by viewModel.isTurkishVoiceMissing.collectAsState()
    val providerHealthMap by com.example.data.managers.ProviderHealthTracker.healthSnapshot.collectAsState()

    val customProviders by viewModel.customProviders.collectAsState()

    // ── Built-in Keyed Provider State ──
    var geminiApiKey by remember { mutableStateOf(settings.customApiKey) }
    var geminiModel by remember { mutableStateOf(settings.geminiModel.ifBlank { "gemini-2.0-flash" }) }
    var groqApiKey by remember { mutableStateOf(settings.groqApiKey) }
    var groqModel by remember { mutableStateOf(settings.groqModel.ifBlank { "openai/gpt-oss-120b" }) }
    var claudeApiKey by remember { mutableStateOf(settings.claudeApiKey) }
    var claudeModel by remember { mutableStateOf(settings.claudeModel.ifBlank { "claude-3-5-sonnet-20241022" }) }
    var openaiApiKey by remember { mutableStateOf(settings.openaiApiKey) }
    var openaiModel by remember { mutableStateOf(settings.openaiModel.ifBlank { "gpt-4o" }) }
    var openRouterApiKey by remember { mutableStateOf(settings.openRouterApiKey) }
    var openRouterModel by remember { mutableStateOf(settings.openRouterModel.ifBlank { "deepseek/deepseek-chat" }) }
    var nvidiaApiKey by remember { mutableStateOf(settings.nvidiaApiKey) }
    var nvidiaModel by remember { mutableStateOf(settings.nvidiaModel.ifBlank { "meta/llama-3.3-70b-instruct" }) }
    var mistralApiKey by remember { mutableStateOf(settings.mistralApiKey) }
    var mistralModel by remember { mutableStateOf(settings.mistralModel.ifBlank { "mistral-large-latest" }) }

    // ── Free Keyless Provider State ──
    var prioritizeFreeKeyless by remember {
        mutableStateOf(ApiPriorityCodec.isFreeFirst(settings.fallbackChainOrder))
    }
    var enablePollinations by remember { mutableStateOf(settings.enablePollinations && settings.pollinationsEnabled) }
    var pollinationsModel by remember { mutableStateOf(settings.pollinationsModel.ifBlank { "openai" }) }
    var enableLlm7 by remember { mutableStateOf(settings.enableLlm7 || settings.llm7Enabled) }
    var enableOvh by remember { mutableStateOf(settings.enableOvh && settings.ovhEnabled) }
    var ovhModel by remember { mutableStateOf(settings.ovhModel.ifBlank { "Meta-Llama-3_3-70B-Instruct" }) }
    var enableAutoFallback by remember { mutableStateOf(settings.enableAutoFallback) }

    // ── Ordered Active API Priority List ──
    var orderedApiKeys by remember {
        mutableStateOf(
            ApiPriorityCodec.resolveOrderedConfiguredKeyedProviders(settings, customProviders)
        )
    }

    // Keep custom providers synced into orderedApiKeys whenever customProviders updates
    LaunchedEffect(customProviders) {
        val existingCustomKeys = customProviders.map { "custom_${it.id}" }.toSet()
        val cleaned = orderedApiKeys.filter { key ->
            !key.startsWith("custom_") || key in existingCustomKeys
        }.toMutableList()
        for (cp in customProviders) {
            val k = "custom_${cp.id}"
            if (k !in cleaned) cleaned.add(k)
        }
        if (cleaned != orderedApiKeys) {
            orderedApiKeys = cleaned
        }
    }

    // ── Chat, Roleplay, Voice, Language, Backup State (100% Preserved) ──
    var responseLength by remember { mutableStateOf(settings.responseLength) }
    var enableNsfw by remember { mutableStateOf(settings.enableNsfw) }
    var enableFlirty by remember { mutableStateOf(settings.enableFlirty) }
    var enableHardcore by remember { mutableStateOf(settings.enableHardcore) }
    var enableFetish by remember { mutableStateOf(settings.enableFetish) }
    var enableDarkRp by remember { mutableStateOf(settings.enableDarkRp) }
    var enableSweet by remember { mutableStateOf(settings.enableSweet) }
    var enablePrimal by remember { mutableStateOf(settings.enablePrimal) }

    var enableTts by remember { mutableStateOf(settings.enableTts) }
    var ttsSpeed by remember { mutableStateOf(settings.ttsSpeed) }
    var ttsPitch by remember { mutableStateOf(settings.ttsPitch) }
    var selectedVoiceName by remember { mutableStateOf(settings.selectedVoiceName) }
    var appLanguage by remember { mutableStateOf(settings.appLanguage) }

    var exportJson by remember { mutableStateOf("") }
    var importJson by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }
    var includeApiKeysInBackup by remember { mutableStateOf(false) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var showVoicePickerDialog by remember { mutableStateOf(false) }

    val availableTtsVoices by viewModel.availableTtsVoices.collectAsState()
    val isTtsSpeaking by viewModel.isSpeaking.collectAsState()
    val ttsReadActions by com.example.util.AppFeatureFlags.ttsReadActions.collectAsState()
    val ttsAutoReadReplies by com.example.util.AppFeatureFlags.ttsAutoReadReplies.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.stopSpeaking() }
    }

    // ── Tabs & Sub-Sheet Navigation ──
    // 0: API, 1: Sohbet, 2: Ses & Dil, 3: Sistem
    var activeTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(activeTab) {
        if (activeTab == 2) {
            viewModel.refreshTtsVoices()
        }
    }
    var showCatalogSearch by remember { mutableStateOf(false) }
    var configuringCatalogSpec by remember { mutableStateOf<CatalogProviderSpec?>(null) }
    var editingCatalogExistingCustom by remember { mutableStateOf<CustomProviderEntity?>(null) }
    var showCustomApiDialog by remember { mutableStateOf(false) }
    var editingCustomProvider by remember { mutableStateOf<CustomProviderEntity?>(null) }
    var showFreeKeylessSheet by remember { mutableStateOf(false) }
    var showFallbackLogModal by remember { mutableStateOf(false) }
    var showFeatureFlagsPanel by remember { mutableStateOf(false) }
    var showUpdateModal by remember { mutableStateOf(false) }

    // Inline test state for active API priority cards
    var testingActiveKey by remember { mutableStateOf<String?>(null) }
    var activeTestResults by remember { mutableStateOf<Map<String, EmochiRepository.ProviderTestResult>>(emptyMap()) }

    fun executeImportWithMode(jsonContent: String, overwriteExisting: Boolean) {
        scope.launch {
            isBusy = true
            try {
                val summary = onImportData(jsonContent, overwriteExisting)
                val memoryCount = summary.memoryFactsCount + summary.memoryEventsCount
                val summaryText = "Yüklendi (v${summary.version}): ${summary.botsCount} bot, ${summary.messagesCount} mesaj, $memoryCount anı"
                Toast.makeText(context, summaryText, Toast.LENGTH_LONG).show()
                statusMessage = summaryText
                importJson = ""
                pendingImportJson = null
            } catch (e: Exception) {
                statusMessage = "Yükleme hatası: ${e.message}"
                Toast.makeText(context, "Yükleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isBusy = false
            }
        }
    }

    // ── Backup File Launchers ──
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isBusy = true
                    val content = context.contentResolver.openInputStream(it)?.use { stream ->
                        stream.bufferedReader().use { reader -> reader.readText() }
                    } ?: ""
                    if (content.isNotBlank()) {
                        pendingImportJson = content
                    } else {
                        statusMessage = "Seçilen yedek dosyası boş."
                    }
                } catch (e: Exception) {
                    statusMessage = "Dosya okuma hatası: ${e.message}"
                } finally {
                    isBusy = false
                }
            }
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isBusy = true
                    val json = onExportData(includeApiKeysInBackup)
                    exportJson = json
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, "Yedek dosyaya kaydedildi!", Toast.LENGTH_SHORT).show()
                    statusMessage = if (includeApiKeysInBackup) {
                        "Yedek (API anahtarlarıyla) dosyaya kaydedildi."
                    } else {
                        "Güvenli yedek (anahtarsız, v2) dosyaya kaydedildi."
                    }
                } catch (e: Exception) {
                    statusMessage = "Kaydetme hatası: ${e.message}"
                } finally {
                    isBusy = false
                }
            }
        }
    }

    // ── Helpers for Active API List ──
    fun maskKey(key: String): String {
        val t = key.trim()
        if (t.isBlank()) return "Anahtarsız / Yerel"
        if (t.length <= 8) return "••••••"
        return "${t.take(4)}••••${t.takeLast(4)}"
    }

    val customMap = remember(customProviders) { customProviders.associateBy { "custom_${it.id}" } }

    fun buildActiveApiItem(key: String): ActiveApiItemUi? {
        return when (key) {
            "gemini" -> if (geminiApiKey.isNotBlank()) ActiveApiItemUi(
                key = "gemini", name = "Google Gemini", model = geminiModel,
                subtitle = maskKey(geminiApiKey), apiFormat = "gemini",
                baseUrl = "https://generativelanguage.googleapis.com/v1beta",
                apiKeyPlain = geminiApiKey, builtinSlot = "gemini"
            ) else null
            "openai" -> if (openaiApiKey.isNotBlank()) ActiveApiItemUi(
                key = "openai", name = "OpenAI (ChatGPT)", model = openaiModel,
                subtitle = maskKey(openaiApiKey), apiFormat = "openai",
                baseUrl = "https://api.openai.com/v1",
                apiKeyPlain = openaiApiKey, builtinSlot = "openai"
            ) else null
            "claude" -> if (claudeApiKey.isNotBlank()) ActiveApiItemUi(
                key = "claude", name = "Anthropic Claude", model = claudeModel,
                subtitle = maskKey(claudeApiKey), apiFormat = "anthropic",
                baseUrl = "https://api.anthropic.com",
                apiKeyPlain = claudeApiKey, builtinSlot = "claude"
            ) else null
            "groq" -> if (groqApiKey.isNotBlank()) ActiveApiItemUi(
                key = "groq", name = "Groq Cloud", model = groqModel,
                subtitle = maskKey(groqApiKey), apiFormat = "openai",
                baseUrl = "https://api.groq.com/openai/v1",
                apiKeyPlain = groqApiKey, builtinSlot = "groq"
            ) else null
            "openrouter" -> if (openRouterApiKey.isNotBlank()) ActiveApiItemUi(
                key = "openrouter", name = "OpenRouter", model = openRouterModel,
                subtitle = maskKey(openRouterApiKey), apiFormat = "openai",
                baseUrl = "https://openrouter.ai/api/v1",
                apiKeyPlain = openRouterApiKey, builtinSlot = "openrouter"
            ) else null
            "nvidia" -> if (nvidiaApiKey.isNotBlank()) ActiveApiItemUi(
                key = "nvidia", name = "NVIDIA NIM", model = nvidiaModel,
                subtitle = maskKey(nvidiaApiKey), apiFormat = "openai",
                baseUrl = "https://integrate.api.nvidia.com/v1",
                apiKeyPlain = nvidiaApiKey, builtinSlot = "nvidia"
            ) else null
            "mistral" -> if (mistralApiKey.isNotBlank()) ActiveApiItemUi(
                key = "mistral", name = "Mistral AI", model = mistralModel,
                subtitle = maskKey(mistralApiKey), apiFormat = "openai",
                baseUrl = "https://api.mistral.ai/v1",
                apiKeyPlain = mistralApiKey, builtinSlot = "mistral"
            ) else null
            else -> if (key.startsWith("custom_")) {
                val cp = customMap[key]
                if (cp != null) {
                    val decrypted = try {
                        com.example.util.KeystoreEncryptionManager.decrypt(cp.apiKeyEncrypted)
                    } catch (_: Exception) {
                        ""
                    }
                    ActiveApiItemUi(
                        key = key,
                        name = cp.label,
                        model = cp.modelName,
                        subtitle = "${maskKey(decrypted)} • ${cp.baseUrl.removePrefix("https://").removePrefix("http://").substringBefore("/")}",
                        apiFormat = cp.apiFormat,
                        baseUrl = cp.baseUrl,
                        apiKeyPlain = decrypted,
                        customEntity = cp
                    )
                } else null
            } else null
        }
    }

    val activeApiItems: List<ActiveApiItemUi> = remember(
        orderedApiKeys,
        geminiApiKey, geminiModel,
        openaiApiKey, openaiModel,
        claudeApiKey, claudeModel,
        groqApiKey, groqModel,
        openRouterApiKey, openRouterModel,
        nvidiaApiKey, nvidiaModel,
        mistralApiKey, mistralModel,
        customProviders
    ) {
        orderedApiKeys.mapNotNull { buildActiveApiItem(it) }
    }

    val configuredBuiltinSlots = remember(
        geminiApiKey, openaiApiKey, claudeApiKey, groqApiKey,
        openRouterApiKey, nvidiaApiKey, mistralApiKey
    ) {
        buildSet {
            if (geminiApiKey.isNotBlank()) add("gemini")
            if (openaiApiKey.isNotBlank()) add("openai")
            if (claudeApiKey.isNotBlank()) add("claude")
            if (groqApiKey.isNotBlank()) add("groq")
            if (openRouterApiKey.isNotBlank()) add("openrouter")
            if (nvidiaApiKey.isNotBlank()) add("nvidia")
            if (mistralApiKey.isNotBlank()) add("mistral")
        }
    }

    val activeFreeCount = listOf(enablePollinations, enableLlm7, enableOvh).count { it }

    fun moveApiItem(index: Int, delta: Int) {
        val target = index + delta
        val visibleKeys = activeApiItems.map { it.key }.toMutableList()
        if (index !in visibleKeys.indices || target !in visibleKeys.indices) return
        val tmp = visibleKeys[index]
        visibleKeys[index] = visibleKeys[target]
        visibleKeys[target] = tmp
        orderedApiKeys = visibleKeys
    }

    fun removeApiItem(item: ActiveApiItemUi) {
        when (item.builtinSlot) {
            "gemini" -> geminiApiKey = ""
            "openai" -> openaiApiKey = ""
            "claude" -> claudeApiKey = ""
            "groq" -> groqApiKey = ""
            "openrouter" -> openRouterApiKey = ""
            "nvidia" -> nvidiaApiKey = ""
            "mistral" -> mistralApiKey = ""
            else -> item.customEntity?.let { viewModel.deleteCustomProvider(it.id) }
        }
        orderedApiKeys = orderedApiKeys.filterNot { it == item.key }
        activeTestResults = activeTestResults - item.key
    }

    fun persistAndClose() {
        val visibleKeys = activeApiItems.map { it.key }
        val encodedOrder = ApiPriorityCodec.encode(prioritizeFreeKeyless, visibleKeys)

        val firstFreeProvider = when {
            enablePollinations -> "pollinations" to pollinationsModel
            enableLlm7 -> "llm7" to "default"
            enableOvh -> "ovh" to ovhModel
            else -> null
        }
        val firstKeyedItem = activeApiItems.firstOrNull()

        val (effectivePrimaryProvider, effectivePrimaryModel) = when {
            prioritizeFreeKeyless && firstFreeProvider != null -> firstFreeProvider
            firstKeyedItem != null -> firstKeyedItem.key to firstKeyedItem.model
            firstFreeProvider != null -> firstFreeProvider
            else -> "pollinations" to pollinationsModel
        }

        onSaveSettings(
            settings.copy(
                customApiKey = geminiApiKey.trim(),
                geminiModel = geminiModel.trim().ifBlank { "gemini-2.0-flash" },
                groqApiKey = groqApiKey.trim(),
                groqModel = groqModel.trim().ifBlank { "openai/gpt-oss-120b" },
                claudeApiKey = claudeApiKey.trim(),
                claudeModel = claudeModel.trim().ifBlank { "claude-3-5-sonnet-20241022" },
                openaiApiKey = openaiApiKey.trim(),
                openaiModel = openaiModel.trim().ifBlank { "gpt-4o" },
                openRouterApiKey = openRouterApiKey.trim(),
                openRouterModel = openRouterModel.trim().ifBlank { "deepseek/deepseek-chat" },
                nvidiaApiKey = nvidiaApiKey.trim(),
                nvidiaModel = nvidiaModel.trim().ifBlank { "meta/llama-3.3-70b-instruct" },
                mistralApiKey = mistralApiKey.trim(),
                mistralModel = mistralModel.trim().ifBlank { "mistral-large-latest" },
                selectedProvider = effectivePrimaryProvider,
                selectedModel = effectivePrimaryModel,
                fallbackChainOrder = encodedOrder,
                enableAutoFallback = enableAutoFallback,
                enablePollinations = enablePollinations,
                pollinationsEnabled = enablePollinations,
                pollinationsModel = pollinationsModel,
                enableLlm7 = enableLlm7,
                llm7Enabled = enableLlm7,
                enableOvh = enableOvh,
                ovhEnabled = enableOvh,
                ovhModel = ovhModel,
                openRouterEnabled = openRouterApiKey.isNotBlank(),
                nvidiaEnabled = nvidiaApiKey.isNotBlank(),
                mistralEnabled = mistralApiKey.isNotBlank(),
                responseLength = responseLength,
                enableNsfw = enableNsfw,
                enableFlirty = enableFlirty,
                enableHardcore = enableHardcore,
                enableFetish = enableFetish,
                enableDarkRp = enableDarkRp,
                enableSweet = enableSweet,
                enablePrimal = enablePrimal,
                enableTts = enableTts,
                ttsSpeed = ttsSpeed,
                ttsPitch = ttsPitch,
                selectedVoiceName = selectedVoiceName,
                appLanguage = appLanguage
            )
        )
        Toast.makeText(context, "Ayarlar kaydedildi", Toast.LENGTH_SHORT).show()
        onDismiss()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FULL-SCREEN MOR NEON COSMIC GLASS OVERLAY (1:1 REFERENCE ACCURACY)
    // ═══════════════════════════════════════════════════════════════════════════
    androidx.activity.compose.BackHandler(
        enabled = !showCatalogSearch && configuringCatalogSpec == null && !showCustomApiDialog && !showFreeKeylessSheet
    ) {
        onDismiss()
    }
    CompositionLocalProvider(
        LocalAetheriaRootSize provides modalRootSizeState,
        LocalAetheriaRootPos provides modalRootPosState
    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(50f)
            .onGloballyPositioned { coords ->
                if (coords.isAttached) {
                    if (modalRootSizeState.value != coords.size) {
                        modalRootSizeState.value = coords.size
                    }
                    val pos = coords.positionInRoot()
                    if (modalRootPosState.value != pos) {
                        modalRootPosState.value = pos
                    }
                }
            }
            .background(Color(0xFF070212))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consume background touches */ }
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_global_settings_cosmic_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
                // ── 1. SYMMETRICAL MOR NEON HEADER (EXACT REFERENCE PLATES) ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    GlobalGlassIconOrb(
                        rootSize = rootSize,
                        blurredBg = blurredBg,
                        onClick = onDismiss,
                        size = 44.dp,
                        orbStyle = GlobalOrbStyle.HEADER_LEFT,
                        modifier = Modifier.testTag("global_settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Geri",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Center Mor Neon glass capsule (exact reference plate + subtle glass sheen)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .drawRefPlate(
                                bitmap = refPlates.headerPill,
                                bleedFracX = 0.039f,
                                bleedFracY = 0.220f
                            )
                            .cosmicGlassSheen(
                                cornerRadius = 25.dp,
                                periodMs = 6200,
                                maxAlpha = 0.14f
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Genel Ayarlar",
                                color = Color.White,
                                fontSize = 18.5.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = DisplaySerifFontFamily,
                                maxLines = 1,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color(0x889333EA),
                                        offset = Offset(0f, 1f),
                                        blurRadius = 8f
                                    )
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "✦",
                                    color = Color(0xFFE9D5FF),
                                    fontSize = 9.5.sp
                                )
                                Text(
                                    text = "API & Sistem Merkezi",
                                    color = GlobalMorCamColors.LavenderSubtext,
                                    fontSize = 10.8.sp,
                                    letterSpacing = 0.25.sp,
                                    fontFamily = InterFontFamily,
                                    maxLines = 1
                                )
                                Text(
                                    text = "✦",
                                    color = Color(0xFFE9D5FF),
                                    fontSize = 9.5.sp
                                )
                            }
                        }
                    }

                    GlobalGlassIconOrb(
                        rootSize = rootSize,
                        blurredBg = blurredBg,
                        onClick = { showFallbackLogModal = true },
                        size = 44.dp,
                        orbStyle = GlobalOrbStyle.HEADER_RIGHT,
                        modifier = Modifier.testTag("global_settings_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Bağlantı ve Yedek Logları",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // ── 2. FOUR NEON GLASS PILL TABS (RESPONSIVE FOR ALL PHONE WIDTHS) ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf(
                        Triple("API", Icons.Default.Bolt, 0.88f),
                        Triple("Sohbet", Icons.Default.ChatBubble, 1.00f),
                        Triple("Ses & Dil", Icons.Default.GraphicEq, 1.14f),
                        Triple("Sistem", Icons.Default.Settings, 0.98f)
                    )
                    tabs.forEachIndexed { index, (label, tabIcon, tabWeight) ->
                        val selected = activeTab == index
                        val tabInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .weight(tabWeight)
                                .height(38.dp)
                                .cosmicTapSpring(
                                    interactionSource = tabInteraction,
                                    pressedScale = 0.93f,
                                    cornerRadius = 19.dp
                                )
                                .drawRefPlate(
                                    bitmap = if (selected) refPlates.tabActive else refPlates.tabInactive,
                                    bleedFracX = if (selected) 0.123f else 0.110f,
                                    bleedFracY = if (selected) 0.270f else 0.243f
                                )
                                .clickable(
                                    interactionSource = tabInteraction,
                                    indication = null
                                ) { activeTab = index }
                                .testTag("global_settings_tab_$index")
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = tabIcon,
                                    contentDescription = null,
                                    tint = if (selected) Color.White else Color(0xFFB784FF),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = label,
                                    color = if (selected) Color.White else Color(0xFFDFD5F5),
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = InterFontFamily,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // ── 3. TAB CONTENT AREA (DIRECTIONAL COSMIC TAB GLIDE) ──
                AnimatedContent(
                    targetState = activeTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    transitionSpec = {
                        CosmicMotionSpecs.cosmicTabTransition(
                            forward = targetState >= initialState,
                            config = motionConfig
                        )
                    },
                    label = "globalSettingsTabContent"
                ) { tabIdx ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (tabIdx) {
                        // ═══════════════════════════════════════════════════════
                        // TAB 0: STREAMLINED MOR NEON API HUB (5 SEPARATE CARDS)
                        // ═══════════════════════════════════════════════════════
                        0 -> {
                            // 1) ÖNCELİK: FREE API'SİZ TOGGLE CARD
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                highlighted = prioritizeFreeKeyless,
                                flipHorizontal = false,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cosmicStaggeredEnter(index = 0, key = "gs_tab0_0")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = if (prioritizeFreeKeyless) GlobalMorCamColors.SoftEmerald else Color(0xFFB784FF),
                                                modifier = Modifier.size(17.dp)
                                            )
                                            Text(
                                                text = "Öncelik: Free API'siz",
                                                color = Color.White,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                        Text(
                                            text = if (prioritizeFreeKeyless)
                                                "Açık — Önce etkin Free API'siz servisler kullanılır; hata olursa ekli API sıran devreye girer."
                                            else
                                                "Kapalı — Önce aşağıdaki ekli API'ların (#1'den başlayarak) kullanılır; gerekirse Free API'siz yedek olur.",
                                            color = GlobalMorCamColors.LavenderSubtext,
                                            fontSize = 11.2.sp,
                                            lineHeight = 14.8.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    GlobalMorCamSwitch(
                                        checked = prioritizeFreeKeyless,
                                        onCheckedChange = { prioritizeFreeKeyless = it },
                                        modifier = Modifier.testTag("free_priority_toggle")
                                    )
                                }
                            }

                            // 2) SEPARATE FLOATING NEON CARD A: API Sağlayıcı Ara & Ekle
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                flipHorizontal = false,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cosmicStaggeredEnter(index = 1, key = "gs_tab0_1")
                            ) {
                                ApiHubEntryRow(
                                    refPlates = refPlates,
                                    icon = Icons.Default.Search,
                                    iconAccent = Color(0xFFF3E8FF),
                                    title = "API Sağlayıcı Ara & Ekle",
                                    subtitle = "${ApiProviderCatalog.catalogProviders.size}+ servis • Gemini, OpenAI, Claude, DeepSeek, Groq, OpenRouter...",
                                    badgeText = "Katalog",
                                    onClick = { showCatalogSearch = true },
                                    modifier = Modifier.testTag("api_search_entry_button")
                                )
                            }

                            // 3) SEPARATE FLOATING NEON CARD B: Özel (Custom) API
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                flipHorizontal = true,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cosmicStaggeredEnter(index = 2, key = "gs_tab0_2")
                            ) {
                                ApiHubEntryRow(
                                    refPlates = refPlates,
                                    icon = Icons.Default.Code,
                                    iconAccent = Color(0xFFF3E8FF),
                                    title = "Özel (Custom) API",
                                    subtitle = "OpenAI veya Anthropic uyumlu kendi sunucu adresini ekle.",
                                    badgeText = "URL",
                                    onClick = {
                                        editingCustomProvider = null
                                        showCustomApiDialog = true
                                    },
                                    modifier = Modifier.testTag("custom_api_entry_button")
                                )
                            }

                            // 4) SEPARATE FLOATING NEON CARD C: Free API'siz Sağlayıcılar (Emerald Accent)
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                flipHorizontal = false,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cosmicStaggeredEnter(index = 3, key = "gs_tab0_3")
                            ) {
                                ApiHubEntryRow(
                                    refPlates = refPlates,
                                    icon = Icons.Default.AutoAwesome,
                                    iconAccent = GlobalMorCamColors.SoftEmerald,
                                    title = "Free API'siz Sağlayıcılar",
                                    subtitle = "Pollinations, LLM7, OVH AI — anahtar gerektirmez.",
                                    badgeText = "$activeFreeCount Aktif",
                                    badgeCount = activeFreeCount,
                                    badgeCountSuffix = " Aktif",
                                    badgeTint = if (activeFreeCount > 0) Color(0xFF4ADE80) else GlobalMorCamColors.MutedLavender,
                                    onClick = { showFreeKeylessSheet = true },
                                    modifier = Modifier.testTag("free_keyless_entry_button")
                                )
                            }

                            // 5) ACTIVE API PRIORITY LIST (SIRALAMA SİSTEMİ)
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                flipHorizontal = false,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cosmicStaggeredEnter(index = 4, key = "gs_tab0_4")
                                    .animateContentSize()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Sync,
                                                contentDescription = null,
                                                tint = Color(0xFFB784FF),
                                                modifier = Modifier.size(17.dp)
                                            )
                                            Text(
                                                text = "Ekli API'ler & Öncelik Sırası",
                                                color = Color.White,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                        Text(
                                            text = if (activeApiItems.isEmpty())
                                                "Henüz anahtarlı veya özel API eklenmedi."
                                            else
                                                "En üstteki (#1) ilk tercihtir; oklarla sırayı değiştir.",
                                            color = GlobalMorCamColors.LavenderSubtext,
                                            fontSize = 11.2.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xCC351763), Color(0xCC1E0B3D))
                                                )
                                            )
                                            .border(1.dp, Color(0xBB9F7AEA), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        CosmicAnimatedCounter(
                                            count = activeApiItems.size,
                                            suffix = " Ekli",
                                            color = Color(0xFFF3E8FF),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (activeApiItems.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xD6090216))
                                            .border(
                                                BorderStroke(1.dp, Color(0x887C3AED)),
                                                RoundedCornerShape(14.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.radialGradient(
                                                            listOf(Color(0x66A855F7), Color.Transparent)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Key,
                                                    contentDescription = null,
                                                    tint = Color(0xFFC084FC),
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                            Text(
                                                text = "Kendi API anahtarını eklemek için yukarıdan\n\"API Sağlayıcı Ara & Ekle\" seçeneğine dokun.",
                                                color = Color(0xFFD8CCF0),
                                                fontSize = 11.2.sp,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 15.sp,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        activeApiItems.forEachIndexed { index, item ->
                                            val isFirst = index == 0
                                            val testRes = activeTestResults[item.key]
                                            val activeUtilityModel = item.customEntity?.utilityModel?.takeIf { it.isNotBlank() }
                                                ?: aiUtilityModelsMap[item.key.lowercase()]?.takeIf { it.isNotBlank() }
                                                ?: ""
                                            ActiveApiPriorityRow(
                                                index = index,
                                                totalCount = activeApiItems.size,
                                                item = item,
                                                isFirstPreference = isFirst && !prioritizeFreeKeyless,
                                                isTesting = testingActiveKey == item.key,
                                                testResult = testRes,
                                                healthState = providerHealthMap[item.key.lowercase()],
                                                activeUtilityModel = activeUtilityModel,
                                                isSelectingUtility = isSelectingUtilityKey == item.key,
                                                onAiSelectUtilityModel = {
                                                    viewModel.selectUtilityModelWithAi(
                                                        providerKey = item.key,
                                                        apiKeyOverride = item.apiKeyPlain.takeIf { it.isNotBlank() },
                                                        baseUrlOverride = item.baseUrl.takeIf { it.isNotBlank() }
                                                    )
                                                },
                                                onMoveUp = { moveApiItem(index, -1) },
                                                onMoveDown = { moveApiItem(index, 1) },
                                                onEdit = {
                                                    if (item.builtinSlot != null) {
                                                        val spec = ApiProviderCatalog.findById(item.builtinSlot)
                                                        if (spec != null) {
                                                            editingCatalogExistingCustom = null
                                                            configuringCatalogSpec = spec
                                                        }
                                                    } else if (item.customEntity != null) {
                                                        val matchingCatalog = ApiProviderCatalog.catalogProviders.firstOrNull {
                                                            it.builtinSlot == null &&
                                                                it.baseUrl.trimEnd('/').equals(item.customEntity.baseUrl.trimEnd('/'), ignoreCase = true)
                                                        }
                                                        if (matchingCatalog != null) {
                                                            editingCatalogExistingCustom = item.customEntity
                                                            configuringCatalogSpec = matchingCatalog
                                                        } else {
                                                            editingCustomProvider = item.customEntity
                                                            showCustomApiDialog = true
                                                        }
                                                    }
                                                },
                                                onTest = {
                                                    scope.launch {
                                                        testingActiveKey = item.key
                                                        val res = viewModel.testCustomProviderConnection(
                                                            baseUrl = item.baseUrl,
                                                            apiKey = item.apiKeyPlain,
                                                            modelName = item.model,
                                                            apiFormat = item.apiFormat
                                                        )
                                                        if (res.isSuccess) {
                                                            com.example.data.managers.ProviderHealthTracker.resetHealth(item.key)
                                                            com.example.data.managers.ProviderHealthTracker.recordSuccess(item.key, res.latencyMs)
                                                            val hasManualCustomUtil = !item.customEntity?.utilityModel.isNullOrBlank()
                                                            if (!hasManualCustomUtil && com.example.util.AppFeatureFlags.getAiUtilityModel(item.key).isBlank()) {
                                                                viewModel.selectUtilityModelWithAi(
                                                                    providerKey = item.key,
                                                                    apiKeyOverride = item.apiKeyPlain.takeIf { it.isNotBlank() },
                                                                    baseUrlOverride = item.baseUrl.takeIf { it.isNotBlank() },
                                                                    silent = true
                                                                )
                                                            }
                                                        }
                                                        activeTestResults = activeTestResults + (item.key to res)
                                                        testingActiveKey = null
                                                    }
                                                },
                                                onDelete = { removeApiItem(item) }
                                            )
                                        }
                                    }
                                }

                                if (!utilitySelectionFeedback.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x552E1065))
                                            .border(0.9.dp, Color(0xAAD8B4FE), RoundedCornerShape(12.dp))
                                            .clickable { viewModel.clearUtilityModelSelectionMessage() }
                                            .padding(horizontal = 10.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = utilitySelectionFeedback.orEmpty(),
                                            color = Color(0xFFF3E8FF),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(9.dp))
                                MorCamDivider()
                                Spacer(modifier = Modifier.height(8.dp))

                                // Automatic fallback switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Otomatik Sıradakine Geçiş (Fallback)",
                                            color = GlobalMorCamColors.PearlText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = InterFontFamily
                                        )
                                        Text(
                                            text = "İlk tercih hata veya kota verirse otomatik #2, #3 ve yedeklere geç.",
                                            color = GlobalMorCamColors.MutedLavender,
                                            fontSize = 11.sp,
                                            lineHeight = 14.5.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    GlobalMorCamSwitch(
                                        checked = enableAutoFallback,
                                        onCheckedChange = { enableAutoFallback = it },
                                        modifier = Modifier.testTag("auto_fallback_toggle")
                                    )
                                }

                                Spacer(modifier = Modifier.height(9.dp))
                                MorCamDivider()
                                Spacer(modifier = Modifier.height(8.dp))

                                // Configurable Censorship / Moralizing Auto-Fallback Protection
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Sansür / Ahlak Vaazı Koruması",
                                            color = GlobalMorCamColors.PearlText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = InterFontFamily
                                        )
                                        Text(
                                            text = "Açıkken bir model karakterden çıkıp vaaz veya ret verirse yanıtı göstermeden sıradaki özgür API'ye geçer.",
                                            color = GlobalMorCamColors.MutedLavender,
                                            fontSize = 11.sp,
                                            lineHeight = 14.5.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    GlobalMorCamSwitch(
                                        checked = refusalAutoFallback,
                                        onCheckedChange = { com.example.util.AppFeatureFlags.setRefusalAutoFallback(it) },
                                        modifier = Modifier.testTag("refusal_auto_fallback_toggle")
                                    )
                                }

                                Spacer(modifier = Modifier.height(9.dp))
                                MorCamDivider()
                                Spacer(modifier = Modifier.height(8.dp))

                                // Sistem #9: Configurable Strict API Timeout Guard
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Akıllı Zaman Aşımı Koruması (Timeout)",
                                            color = GlobalMorCamColors.PearlText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = InterFontFamily
                                        )
                                        Text(
                                            text = "Açıkken yanıt vermeyen API en fazla 60 sn (yardımcı çağrılar 18 sn) beklenip sıradakine geçer. Yavaş yerel/düşünen (reasoning) modellerde kapatarak süresiz bekleyebilirsin.",
                                            color = GlobalMorCamColors.MutedLavender,
                                            fontSize = 11.sp,
                                            lineHeight = 14.5.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    GlobalMorCamSwitch(
                                        checked = strictApiTimeout,
                                        onCheckedChange = { com.example.util.AppFeatureFlags.setStrictApiTimeout(it) },
                                        modifier = Modifier.testTag("strict_api_timeout_toggle")
                                    )
                                }
                            }
                        }

                        // ═══════════════════════════════════════════════════════
                        // TAB 1: SOHBET & TON (RESPONSE LENGTH + RP FILTERS)
                        // ═══════════════════════════════════════════════════════
                        1 -> {
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Varsayılan Yanıt Uzunluğu",
                                    color = GlobalMorCamColors.PearlText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily
                                )
                                Text(
                                    text = "Karakterlerin varsayılan anlatım uzunluğunu seç.",
                                    color = GlobalMorCamColors.LavenderSubtext,
                                    fontSize = 11.5.sp,
                                    fontFamily = InterFontFamily
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        "short" to "Kısa",
                                        "orta" to "Orta",
                                        "standard" to "Standart",
                                        "long" to "Uzun"
                                    ).forEach { (key, label) ->
                                        val selected = responseLength == key
                                        val pillShape = RoundedCornerShape(50)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 42.dp)
                                                .then(
                                                    if (selected) {
                                                        Modifier.morCamCosmicGlow(
                                                            cornerRadius = 50.dp,
                                                            glowColor = Color(0xFFB366FF),
                                                            alpha = 0.52f,
                                                            blurDp = 12.dp
                                                        )
                                                    } else Modifier
                                                )
                                                .clip(pillShape)
                                                .background(
                                                    brush = if (selected) {
                                                        Brush.verticalGradient(
                                                            listOf(Color(0xEE8B5CF6), Color(0xEE5B21B6))
                                                        )
                                                    } else {
                                                        Brush.verticalGradient(
                                                            listOf(Color(0x88241145), Color(0x88120726))
                                                        )
                                                    }
                                                )
                                                .border(
                                                    width = if (selected) 1.35.dp else 1.dp,
                                                    brush = if (selected) {
                                                        Brush.linearGradient(
                                                            listOf(Color(0xFFFDF7FF), Color(0xFFD8B4FE), Color(0xFF9333EA))
                                                        )
                                                    } else {
                                                        Brush.linearGradient(
                                                            listOf(Color(0x88D8B4FE), Color(0x448B5CF6))
                                                        )
                                                    },
                                                    shape = pillShape
                                                )
                                                .clickable { responseLength = key },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (selected) Color.White else GlobalMorCamColors.LavenderSubtext,
                                                fontSize = 12.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                    }
                                }
                            }

                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Rol Yapma & İçerik Tonları (+18)",
                                    color = GlobalMorCamColors.PearlText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily
                                )
                                Text(
                                    text = "Hikayelerde izin verilen anlatım tonlarını belirle.",
                                    color = GlobalMorCamColors.LavenderSubtext,
                                    fontSize = 11.5.sp,
                                    fontFamily = InterFontFamily
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val toggles = listOf(
                                    Triple("18+ (NSFW) Genel Kilidini Aç", "Yetişkin ve sansürsüz kurgu sahneleri", enableNsfw to { v: Boolean -> enableNsfw = v }),
                                    Triple("Çapkınlık (Flirty)", "Flörtöz ve yakınlaştırıcı diyaloglar", enableFlirty to { v: Boolean -> enableFlirty = v }),
                                    Triple("Sert Mod (Hardcore)", "Yüksek gerilimli ve yoğun sahne anlatımı", enableHardcore to { v: Boolean -> enableHardcore = v }),
                                    Triple("Fantezi (Fetish)", "Özel fantezi ve tema özgürlüğü", enableFetish to { v: Boolean -> enableFetish = v }),
                                    Triple("Karanlık (Dark RP)", "Gotik, psikolojik ve karanlık hikaye tonları", enableDarkRp to { v: Boolean -> enableDarkRp = v }),
                                    Triple("Romantik (Sweet)", "Duygusal, sıcak ve şefkatli yakınlaşmalar", enableSweet to { v: Boolean -> enableSweet = v }),
                                    Triple("Vahşi (Primal)", "İçgüdüsel ve yüksek tutkulu dinamikler", enablePrimal to { v: Boolean -> enablePrimal = v })
                                )

                                toggles.forEachIndexed { idx, (title, sub, pair) ->
                                    if (idx > 0) {
                                        MorCamDivider(
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = title,
                                                color = GlobalMorCamColors.PearlText,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = InterFontFamily
                                            )
                                            Text(
                                                text = sub,
                                                color = GlobalMorCamColors.MutedLavender,
                                                fontSize = 11.sp,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        GlobalMorCamSwitch(
                                            checked = pair.first,
                                            onCheckedChange = pair.second
                                        )
                                    }
                                }
                            }
                        }

                        // ═══════════════════════════════════════════════════════
                        // TAB 2: SES & DİL (TTS + APP LANGUAGE)
                        // ═══════════════════════════════════════════════════════
                        2 -> {
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = null,
                                            tint = GlobalMorCamColors.BrightViolet,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Sesli Okuma (TTS)",
                                                color = GlobalMorCamColors.PearlText,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = InterFontFamily
                                            )
                                            Text(
                                                text = "Karakter yanıtlarını sesli dinleme desteği",
                                                color = GlobalMorCamColors.LavenderSubtext,
                                                fontSize = 11.5.sp,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                    }
                                    GlobalMorCamSwitch(
                                        checked = enableTts,
                                        onCheckedChange = { enableTts = it }
                                    )
                                }

                                AnimatedVisibility(
                                    visible = enableTts,
                                    enter = expandVertically(CosmicMotionSpecs.velvetGlass()) + fadeIn(tween(220)),
                                    exit = shrinkVertically(tween(190)) + fadeOut(tween(150))
                                ) {
                                    Column {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Okuma Hızı: ${"%.2f".format(ttsSpeed)}x",
                                        color = GlobalMorCamColors.LavenderSubtext,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = InterFontFamily
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(0.8f to "0.8x", 1.0f to "1.0x", 1.25f to "1.25x", 1.5f to "1.5x").forEach { (sp, lbl) ->
                                            val sel = ttsSpeed == sp
                                            val pillShape = RoundedCornerShape(50)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 40.dp)
                                                    .then(
                                                        if (sel) {
                                                            Modifier.morCamCosmicGlow(
                                                                cornerRadius = 50.dp,
                                                                glowColor = Color(0xFFB366FF),
                                                                alpha = 0.50f,
                                                                blurDp = 11.dp
                                                            )
                                                        } else Modifier
                                                    )
                                                    .clip(pillShape)
                                                    .background(
                                                        brush = if (sel) {
                                                            Brush.verticalGradient(listOf(Color(0xEE8B5CF6), Color(0xEE5B21B6)))
                                                        } else {
                                                            Brush.verticalGradient(listOf(Color(0x88241145), Color(0x88120726)))
                                                        }
                                                    )
                                                    .border(
                                                        width = if (sel) 1.3.dp else 1.dp,
                                                        brush = if (sel) {
                                                            Brush.linearGradient(listOf(Color(0xFFFDF7FF), Color(0xFFD8B4FE), Color(0xFF9333EA)))
                                                        } else {
                                                            Brush.linearGradient(listOf(Color(0x88D8B4FE), Color(0x448B5CF6)))
                                                        },
                                                        shape = pillShape
                                                    )
                                                    .clickable { ttsSpeed = sp },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = lbl,
                                                    color = if (sel) Color.White else GlobalMorCamColors.LavenderSubtext,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                                    fontFamily = InterFontFamily
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Ses Tonu (Pitch): ${"%.1f".format(ttsPitch)}x",
                                        color = GlobalMorCamColors.LavenderSubtext,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = InterFontFamily
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(0.8f to "Kalın (0.8)", 1.0f to "Normal (1.0)", 1.2f to "İnce (1.2)").forEach { (p, lbl) ->
                                            val sel = ttsPitch == p
                                            val pillShape = RoundedCornerShape(50)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 40.dp)
                                                    .then(
                                                        if (sel) {
                                                            Modifier.morCamCosmicGlow(
                                                                cornerRadius = 50.dp,
                                                                glowColor = Color(0xFFB366FF),
                                                                alpha = 0.50f,
                                                                blurDp = 11.dp
                                                            )
                                                        } else Modifier
                                                    )
                                                    .clip(pillShape)
                                                    .background(
                                                        brush = if (sel) {
                                                            Brush.verticalGradient(listOf(Color(0xEE8B5CF6), Color(0xEE5B21B6)))
                                                        } else {
                                                            Brush.verticalGradient(listOf(Color(0x88241145), Color(0x88120726)))
                                                        }
                                                    )
                                                    .border(
                                                        width = if (sel) 1.3.dp else 1.dp,
                                                        brush = if (sel) {
                                                            Brush.linearGradient(listOf(Color(0xFFFDF7FF), Color(0xFFD8B4FE), Color(0xFF9333EA)))
                                                        } else {
                                                            Brush.linearGradient(listOf(Color(0x88D8B4FE), Color(0x448B5CF6)))
                                                        },
                                                        shape = pillShape
                                                    )
                                                    .clickable { ttsPitch = p },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = lbl,
                                                    color = if (sel) Color.White else GlobalMorCamColors.LavenderSubtext,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                                    fontFamily = InterFontFamily
                                                )
                                            }
                                        }
                                    }

                                    // ── Sistem #11: Free 2-Tier Emotional Neural TTS Engine Selector ──
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Ses Motoru (Ücretsiz 2 Katmanlı)",
                                        color = GlobalMorCamColors.LavenderSubtext,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = InterFontFamily
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            "neural" to "✦ Nöral Duygulu (Ücretsiz)",
                                            "device" to "📱 Cihaz Sesi (Çevrimdışı)"
                                        ).forEach { (modeKey, modeLabel) ->
                                            val isSelected = ttsEngineMode == modeKey
                                            val pillShape = RoundedCornerShape(50)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 42.dp)
                                                    .clip(pillShape)
                                                    .background(
                                                        if (isSelected) Brush.verticalGradient(listOf(Color(0xEE8B5CF6), Color(0xEE5B21B6)))
                                                        else Brush.verticalGradient(listOf(Color(0x88241145), Color(0x88120726)))
                                                    )
                                                    .border(
                                                        width = if (isSelected) 1.3.dp else 1.dp,
                                                        brush = if (isSelected) Brush.linearGradient(listOf(Color(0xFFFDF7FF), Color(0xFFD8B4FE), Color(0xFF9333EA)))
                                                        else Brush.linearGradient(listOf(Color(0x88D8B4FE), Color(0x448B5CF6))),
                                                        shape = pillShape
                                                    )
                                                    .clickable { com.example.util.AppFeatureFlags.setTtsEngineMode(modeKey) }
                                                    .padding(horizontal = 8.dp)
                                                    .testTag("tts_engine_mode_$modeKey"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = modeLabel,
                                                    color = if (isSelected) Color.White else GlobalMorCamColors.LavenderSubtext,
                                                    fontSize = 11.2.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontFamily = InterFontFamily
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Duygusal Tonlama (Prosodi)",
                                                color = GlobalMorCamColors.PearlText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = InterFontFamily
                                            )
                                            Text(
                                                text = "Botun ruh haline, fısıltı ve haykırışlara göre doğal ses tonu",
                                                color = GlobalMorCamColors.LavenderSubtext,
                                                fontSize = 11.sp,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                        GlobalMorCamSwitch(
                                            checked = ttsEmotionalProsody,
                                            onCheckedChange = { com.example.util.AppFeatureFlags.setTtsEmotionalProsody(it) },
                                            modifier = Modifier.testTag("tts_emotional_prosody_toggle")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    if (ttsEngineMode == "neural") {
                                        Text(
                                            text = "Nöral Ses Karakteri (İnsan Kalitesi • Çevrimdışı Otomatik Yedekli)",
                                            color = GlobalMorCamColors.LavenderSubtext,
                                            fontSize = 11.8.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = InterFontFamily
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            com.example.util.EdgeNeuralTtsClient.NEURAL_VOICES.forEach { nVoice ->
                                                val isSel = ttsNeuralVoice == nVoice.id
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(13.dp))
                                                        .background(if (isSel) Color(0x667C3AED) else Color(0x44190A30))
                                                        .border(
                                                            1.dp,
                                                            if (isSel) Color(0xFFE9D5FF) else Color(0x44A855F7),
                                                            RoundedCornerShape(13.dp)
                                                        )
                                                        .clickable { com.example.util.AppFeatureFlags.setTtsNeuralVoice(nVoice.id) }
                                                        .padding(horizontal = 11.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "✦ ${nVoice.displayName}",
                                                            color = Color.White,
                                                            fontSize = 12.2.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontFamily = InterFontFamily
                                                        )
                                                        Text(
                                                            text = nVoice.subtitle,
                                                            color = GlobalMorCamColors.LavenderSubtext,
                                                            fontSize = 10.5.sp,
                                                            fontFamily = InterFontFamily
                                                        )
                                                    }
                                                    if (isSel) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFF3E8FF), modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // ── Mor Cam Voice Picker & Live Preview ──
                                    val activeVoiceLabel = remember(selectedVoiceName, availableTtsVoices, ttsEngineMode, ttsNeuralVoice) {
                                        if (ttsEngineMode == "neural") {
                                            com.example.util.EdgeNeuralTtsClient.NEURAL_VOICES.firstOrNull { it.id == ttsNeuralVoice }?.displayName
                                                ?: "Emel (Türkçe • Kadın)"
                                        } else if (selectedVoiceName.isBlank()) {
                                            "Sistem Varsayılanı (${if (appLanguage == "en") "English" else "Türkçe"})"
                                        } else {
                                            availableTtsVoices.firstOrNull { it.name == selectedVoiceName }?.displayName
                                                ?: selectedVoiceName.take(28)
                                        }
                                    }
                                    if (ttsEngineMode != "neural") {
                                        Text(
                                            text = "Cihaz Sesi Seçimi (${availableTtsVoices.size} cihaz sesi)",
                                            color = GlobalMorCamColors.LavenderSubtext,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = InterFontFamily
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1.25f)
                                                .heightIn(min = 44.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    Brush.verticalGradient(listOf(Color(0x88241145), Color(0x88120726)))
                                                )
                                                .border(
                                                    1.dp,
                                                    Brush.linearGradient(listOf(Color(0x99D8B4FE), Color(0x448B5CF6))),
                                                    RoundedCornerShape(14.dp)
                                                )
                                                .clickable {
                                                    viewModel.refreshTtsVoices()
                                                    showVoicePickerDialog = true
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .testTag("tts_voice_picker_button"),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = "🎙️ $activeVoiceLabel",
                                                color = GlobalMorCamColors.PearlText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontFamily = InterFontFamily
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(0.92f)
                                                .heightIn(min = 44.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (isTtsSpeaking) {
                                                        Brush.verticalGradient(listOf(Color(0xEEDC2626), Color(0xEE991B1B)))
                                                    } else {
                                                        Brush.verticalGradient(listOf(Color(0xEE8B5CF6), Color(0xEE5B21B6)))
                                                    }
                                                )
                                                .border(
                                                    1.dp,
                                                    Brush.linearGradient(listOf(Color(0xFFFDF7FF), Color(0xFFD8B4FE))),
                                                    RoundedCornerShape(14.dp)
                                                )
                                                .clickable {
                                                    if (isTtsSpeaking) {
                                                        viewModel.stopSpeaking()
                                                    } else {
                                                        viewModel.previewTtsVoice(
                                                            voiceName = selectedVoiceName,
                                                            speed = ttsSpeed,
                                                            pitch = ttsPitch,
                                                            languageTag = appLanguage,
                                                            neuralVoiceId = if (ttsEngineMode == "neural") ttsNeuralVoice else null
                                                        )
                                                    }
                                                }
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                                .testTag("tts_preview_voice_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isTtsSpeaking) "⏹ Durdur" else "▶ Duygulu Dinle",
                                                color = Color.White,
                                                fontSize = 11.8.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                    }

                                    if (isTurkishVoiceMissing && ttsEngineMode == "device") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0x4D451A03))
                                                .border(1.dp, Color(0xAAF59E0B), RoundedCornerShape(14.dp))
                                                .clickable {
                                                    try {
                                                        val installIntent = android.content.Intent(android.speech.tts.TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                                                        installIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        context.startActivity(installIntent)
                                                    } catch (_: Exception) {
                                                        Toast.makeText(context, "TTS dil paketi ekranı açılamadı.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .padding(horizontal = 12.dp, vertical = 9.dp)
                                                .testTag("tts_install_turkish_pack_button"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Türkçe Yüksek Kaliteli Cihaz Sesi İndir",
                                                    color = Color(0xFFFDE68A),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = InterFontFamily
                                                )
                                                Text(
                                                    text = "Çevrimdışı modda doğal Türkçe ses için Google TTS paketini tek dokunuşla kur.",
                                                    color = Color(0xFFFEF3C7),
                                                    fontSize = 10.5.sp,
                                                    fontFamily = InterFontFamily
                                                )
                                            }
                                            Text(
                                                text = "İndir →",
                                                color = Color.White,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Aksiyon (*...*) Metinlerini Oku",
                                                color = GlobalMorCamColors.PearlText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = InterFontFamily
                                            )
                                            Text(
                                                text = "Kapalıyken yalnızca diyaloglar seslendirilir",
                                                color = GlobalMorCamColors.LavenderSubtext,
                                                fontSize = 11.sp,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                        GlobalMorCamSwitch(
                                            checked = ttsReadActions,
                                            onCheckedChange = { com.example.util.AppFeatureFlags.setTtsReadActions(it) }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Yeni Yanıtları Otomatik Oku",
                                                color = GlobalMorCamColors.PearlText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = InterFontFamily
                                            )
                                            Text(
                                                text = "Karakter her yeni mesaj yazdığında sesi başlatır",
                                                color = GlobalMorCamColors.LavenderSubtext,
                                                fontSize = 11.sp,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                        GlobalMorCamSwitch(
                                            checked = ttsAutoReadReplies,
                                            onCheckedChange = { com.example.util.AppFeatureFlags.setTtsAutoReadReplies(it) }
                                        )
                                    }
                                    }
                                }
                            }

                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = GlobalMorCamColors.BrightViolet,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Uygulama & Yanıt Dili",
                                            color = GlobalMorCamColors.PearlText,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = InterFontFamily
                                        )
                                        Text(
                                            text = "Yapay zeka yanıtları için varsayılan dil tercihi",
                                            color = GlobalMorCamColors.LavenderSubtext,
                                            fontSize = 11.5.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf("tr" to "Türkçe (Varsayılan)", "en" to "English").forEach { (code, title) ->
                                        val selected = appLanguage == code
                                        val pillShape = RoundedCornerShape(50)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 44.dp)
                                                .then(
                                                    if (selected) {
                                                        Modifier.morCamCosmicGlow(
                                                            cornerRadius = 50.dp,
                                                            glowColor = Color(0xFFB366FF),
                                                            alpha = 0.52f,
                                                            blurDp = 12.dp
                                                        )
                                                    } else Modifier
                                                )
                                                .clip(pillShape)
                                                .background(
                                                    brush = if (selected) {
                                                        Brush.verticalGradient(listOf(Color(0xEE8B5CF6), Color(0xEE5B21B6)))
                                                    } else {
                                                        Brush.verticalGradient(listOf(Color(0x88241145), Color(0x88120726)))
                                                    }
                                                )
                                                .border(
                                                    width = if (selected) 1.35.dp else 1.dp,
                                                    brush = if (selected) {
                                                        Brush.linearGradient(listOf(Color(0xFFFDF7FF), Color(0xFFD8B4FE), Color(0xFF9333EA)))
                                                    } else {
                                                        Brush.linearGradient(listOf(Color(0x88D8B4FE), Color(0x448B5CF6)))
                                                    },
                                                    shape = pillShape
                                                )
                                                .clickable { appLanguage = code },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = title,
                                                color = if (selected) Color.White else GlobalMorCamColors.LavenderSubtext,
                                                fontSize = 13.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ═══════════════════════════════════════════════════════
                        // TAB 3: SİSTEM (TOKENS, BACKUP/RESTORE, UPDATES, FLAGS)
                        // ═══════════════════════════════════════════════════════
                        3 -> {
                            val totalTokens = settings.totalPromptTokens + settings.totalCandidateTokens

                            // Cosmic Motion & Animation Intensity Control Card
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                highlighted = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cosmicStaggeredEnter(index = 0, key = "gs_tab3_0")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFD8B4FE),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Kozmik Hareket & Animasyonlar",
                                            color = GlobalMorCamColors.PearlText,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = InterFontFamily
                                        )
                                        Text(
                                            text = "Yay fiziği, cam parıltısı ve yıldız tozu yoğunluğunu seç.",
                                            color = GlobalMorCamColors.LavenderSubtext,
                                            fontSize = 11.2.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MotionIntensityMode.entries.forEach { mode ->
                                        val selected = currentMotionMode == mode
                                        val pillInteraction = remember { MutableInteractionSource() }
                                        val pillShape = RoundedCornerShape(50)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 42.dp)
                                                .cosmicTapSpring(
                                                    interactionSource = pillInteraction,
                                                    pressedScale = 0.94f,
                                                    cornerRadius = 21.dp
                                                )
                                                .then(
                                                    if (selected) {
                                                        Modifier.morCamCosmicGlow(
                                                            cornerRadius = 50.dp,
                                                            glowColor = Color(0xFFB366FF),
                                                            alpha = 0.52f,
                                                            blurDp = 12.dp
                                                        )
                                                    } else Modifier
                                                )
                                                .clip(pillShape)
                                                .background(
                                                    brush = if (selected) {
                                                        Brush.verticalGradient(listOf(Color(0xEE8B5CF6), Color(0xEE5B21B6)))
                                                    } else {
                                                        Brush.verticalGradient(listOf(Color(0x88241145), Color(0x88120726)))
                                                    }
                                                )
                                                .border(
                                                    width = if (selected) 1.35.dp else 1.dp,
                                                    brush = if (selected) {
                                                        Brush.linearGradient(listOf(Color(0xFFFDF7FF), Color(0xFFD8B4FE), Color(0xFF9333EA)))
                                                    } else {
                                                        Brush.linearGradient(listOf(Color(0x88D8B4FE), Color(0x448B5CF6)))
                                                    },
                                                    shape = pillShape
                                                )
                                                .clickable(
                                                    interactionSource = pillInteraction,
                                                    indication = null
                                                ) {
                                                    CosmicMotionPreferences.setMode(context, mode)
                                                }
                                                .testTag("motion_mode_${mode.key}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = mode.label,
                                                color = if (selected) Color.White else GlobalMorCamColors.LavenderSubtext,
                                                fontSize = 12.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                fontFamily = InterFontFamily,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                CosmicMotionPreviewStrip(mode = currentMotionMode)
                                Spacer(modifier = Modifier.height(14.dp))

                                val reduceMotionEnabled by com.example.util.AppFeatureFlags.reduceMotion.collectAsStateWithLifecycle()
                                val microAiCastSentimentEnabled by com.example.util.AppFeatureFlags.microAiCastSentiment.collectAsStateWithLifecycle()

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reduce_motion_switch"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                        Text(
                                            text = "Hareketi Azalt & Pil Tasarrufu",
                                            color = GlobalMorCamColors.PearlText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = InterFontFamily
                                        )
                                        Text(
                                            text = "Kozmik parçacık ve yörünge animasyonlarını sadeleştirerek pil tüketimini azaltır.",
                                            color = GlobalMorCamColors.LavenderSubtext,
                                            fontSize = 11.sp,
                                            lineHeight = 14.5.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                    GlobalMorCamSwitch(
                                        checked = reduceMotionEnabled,
                                        onCheckedChange = { com.example.util.AppFeatureFlags.setReduceMotion(it) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("micro_ai_cast_sentiment_switch"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                        Text(
                                            text = "Ufak AI Kadro & Duygu Analizi",
                                            color = GlobalMorCamColors.PearlText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = InterFontFamily
                                        )
                                        Text(
                                            text = "Edebi yan karakter çıkarımı ve gizli kullanıcı alt metni için hızlı yardımcı modeli kullanır (çevrimdışı yerel fallback).",
                                            color = GlobalMorCamColors.LavenderSubtext,
                                            fontSize = 11.sp,
                                            lineHeight = 14.5.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                    GlobalMorCamSwitch(
                                        checked = microAiCastSentimentEnabled,
                                        onCheckedChange = { com.example.util.AppFeatureFlags.setMicroAiCastSentiment(it) }
                                    )
                                }
                            }

                            // Token stats card
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cosmicStaggeredEnter(index = 1, key = "gs_tab3_1")
                            ) {
                                Text(
                                    text = "Token Kullanım Özeti",
                                    color = GlobalMorCamColors.PearlText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TokenStatBox("Girdi", "${settings.totalPromptTokens}", Modifier.weight(1f))
                                    TokenStatBox("Çıktı", "${settings.totalCandidateTokens}", Modifier.weight(1f))
                                    TokenStatBox("Toplam", "$totalTokens", Modifier.weight(1f))
                                }
                            }

                            // Backup & Restore Card
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Yedekleme & Geri Yükleme (v2)",
                                    color = GlobalMorCamColors.PearlText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily
                                )
                                Text(
                                    text = "Karakterler, sohbetler, Neural Vault anıları, kadro, rutinler ve başarımları tam paket olarak dışa/içe aktar.",
                                    color = GlobalMorCamColors.LavenderSubtext,
                                    fontSize = 11.5.sp,
                                    fontFamily = InterFontFamily
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "API anahtarlarını yedeğe dahil et (Önerilmez)",
                                            color = if (includeApiKeysInBackup) Color(0xFFFCA5A5) else GlobalMorCamColors.PearlText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = InterFontFamily
                                        )
                                        Text(
                                            text = if (includeApiKeysInBackup) "Dikkat: Düz metin anahtarlar JSON içine yazılır!" else "Güvenli mod: Tüm API anahtarları maskelenir",
                                            color = GlobalMorCamColors.LavenderSubtext,
                                            fontSize = 10.5.sp,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                    GlobalMorCamSwitch(
                                        checked = includeApiKeysInBackup,
                                        onCheckedChange = { includeApiKeysInBackup = it }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SmallGlassActionPill(
                                        icon = Icons.Default.ContentCopy,
                                        label = "Kopyala",
                                        onClick = {
                                            scope.launch {
                                                isBusy = true
                                                try {
                                                    val json = onExportData(includeApiKeysInBackup)
                                                    exportJson = json
                                                    clipboardManager.setText(AnnotatedString(json))
                                                    Toast.makeText(context, "Yedek panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                                                    statusMessage = "v2 yedek panoya kopyalandı."
                                                } catch (e: Exception) {
                                                    statusMessage = "Hata: ${e.message}"
                                                } finally {
                                                    isBusy = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SmallGlassActionPill(
                                        icon = Icons.Default.FileDownload,
                                        label = "Kaydet",
                                        onClick = {
                                            saveFileLauncher.launch("emochi_backup_${System.currentTimeMillis() / 1000}.json")
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SmallGlassActionPill(
                                        icon = Icons.Default.Share,
                                        label = "Paylaş",
                                        onClick = {
                                            scope.launch {
                                                isBusy = true
                                                try {
                                                    val json = onExportData(includeApiKeysInBackup)
                                                    exportJson = json
                                                    com.example.util.ChatExporter.shareJsonFile(
                                                        context = context,
                                                        jsonContent = json,
                                                        fileName = "emochi_backup_${System.currentTimeMillis() / 1000}.json",
                                                        chooserTitle = "Yedeği Dosya Olarak Paylaş"
                                                    )
                                                    statusMessage = "Yedek dosyası paylaşıma hazırlandı."
                                                } catch (e: Exception) {
                                                    statusMessage = "Paylaşım hatası: ${e.message}"
                                                } finally {
                                                    isBusy = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    GlobalGlassPrimaryButton(
                                        text = "Dosya Seç (.json)",
                                        enabled = !isBusy,
                                        onClick = { filePickerLauncher.launch("application/json") },
                                        modifier = Modifier.weight(1.3f)
                                    )
                                    GlobalGlassSecondaryButton(
                                        text = "Panodan Yapıştır",
                                        enabled = !isBusy,
                                        onClick = {
                                            clipboardManager.getText()?.let { text ->
                                                importJson = text.text
                                                Toast.makeText(context, "Metin panodan alındı!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (importJson.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    GlobalGlassInputField(
                                        value = importJson,
                                        onValueChange = { importJson = it },
                                        placeholder = "JSON yedek metni...",
                                        singleLine = false
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    GlobalGlassPrimaryButton(
                                        text = "Yüklemeyi Başlat",
                                        loading = isBusy,
                                        enabled = !isBusy,
                                        onClick = { pendingImportJson = importJson },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                statusMessage?.let { msg ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = msg,
                                        color = GlobalMorCamColors.SoftEmerald,
                                        fontSize = 11.5.sp,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }

                            // Separate Floating Neon Cards for Reading Streak, Cache Cleaner, Updates & Feature Flags
                            val streakState = remember { com.example.util.ReadingStreakTracker.getState(context) }
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ApiHubEntryRow(
                                    refPlates = refPlates,
                                    icon = Icons.Default.AutoAwesome,
                                    iconAccent = Color(0xFFFDE68A),
                                    title = "Okuma & Hikaye Serisi",
                                    subtitle = "En uzun seri: ${streakState.longestStreakDays} gün • Toplam aktif: ${streakState.totalActiveDays} gün",
                                    badgeText = "🔥 ${streakState.currentStreakDays} Gün",
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            "Harika gidiyorsun! Güncel serin: ${streakState.currentStreakDays} gün",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                            var cacheReport by remember {
                                mutableStateOf(com.example.util.AppCacheCleaner.inspectCache(context))
                            }
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ApiHubEntryRow(
                                    refPlates = refPlates,
                                    icon = Icons.Default.DeleteOutline,
                                    iconAccent = Color(0xFFFDE68A),
                                    title = "Önbelleği Temizle",
                                    subtitle = "Ses: ${com.example.util.AppCacheCleaner.formatBytes(cacheReport.ttsBytes)} • Görsel: ${com.example.util.AppCacheCleaner.formatBytes(cacheReport.imageBytes)} (Sohbetler korunur)",
                                    badgeText = com.example.util.AppCacheCleaner.formatBytes(cacheReport.totalBytes),
                                    onClick = {
                                        val freed = com.example.util.AppCacheCleaner.clearDisposableCaches(context)
                                        cacheReport = com.example.util.AppCacheCleaner.inspectCache(context)
                                        Toast.makeText(
                                            context,
                                            "Önbellek temizlendi (${com.example.util.AppCacheCleaner.formatBytes(freed)} açıldı)",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ApiHubEntryRow(
                                    refPlates = refPlates,
                                    icon = Icons.Default.SystemUpdate,
                                    iconAccent = Color(0xFFF3E8FF),
                                    title = "Sürüm & Güncellemeler",
                                    subtitle = "Mevcut sürümü ve APK güncellemelerini denetle",
                                    badgeText = "v${com.example.BuildConfig.VERSION_NAME}",
                                    onClick = { showUpdateModal = true }
                                )
                            }
                            GlobalGlassCard(
                                rootSize = rootSize,
                                blurredBg = blurredBg,
                                flipHorizontal = true,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ApiHubEntryRow(
                                    refPlates = refPlates,
                                    icon = Icons.Default.Tune,
                                    iconAccent = Color(0xFFF3E8FF),
                                    title = "Özellikler & Deneysel Ayarlar",
                                    subtitle = "Gelişmiş hafıza, duygu ve motor anahtarları",
                                    badgeText = "Panel",
                                    onClick = { showFeatureFlagsPanel = true }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
                } // end AnimatedContent

                // ── 4. FLOATING MOR NEON FOOTER (EXACT REFERENCE PLATES) ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlobalGlassIconOrb(
                        rootSize = rootSize,
                        blurredBg = blurredBg,
                        onClick = onDismiss,
                        size = 46.dp,
                        orbStyle = GlobalOrbStyle.FOOTER_CLOSE,
                        modifier = Modifier.testTag("global_settings_cancel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Vazgeç",
                            tint = Color.White,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    val saveInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .cosmicTapSpring(
                                interactionSource = saveInteraction,
                                pressedScale = 0.955f,
                                cornerRadius = 23.dp
                            )
                            .drawRefPlate(
                                bitmap = refPlates.footerSave,
                                bleedFracX = 0.042f,
                                bleedFracY = 0.227f
                            )
                            .cosmicGlassSheen(
                                cornerRadius = 23.dp,
                                periodMs = 4800,
                                maxAlpha = 0.16f
                            )
                            .clickable(
                                interactionSource = saveInteraction,
                                indication = null
                            ) { persistAndClose() }
                            .testTag("global_settings_save_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text(
                                text = "Değişiklikleri Kaydet",
                                color = Color.White,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = InterFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "✦",
                                color = Color(0xFFE9D5FF),
                                fontSize = 14.sp
                            )
                        }
                    }

                    GlobalGlassIconOrb(
                        rootSize = rootSize,
                        blurredBg = blurredBg,
                        onClick = { persistAndClose() },
                        size = 46.dp,
                        orbStyle = GlobalOrbStyle.FOOTER_CHECK,
                        modifier = Modifier.testTag("global_settings_save_orb")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Kaydet",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

        // ── Mor Cam Voice Picker Dialog ──
        if (showVoicePickerDialog) {
            MorCamDialogShell(
                onDismissRequest = { showVoicePickerDialog = false },
                title = "Sesli Okuma Sesi Seç",
                subtitle = "Türkçe sesler en üstte listelenir",
                icon = Icons.AutoMirrored.Filled.VolumeUp
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "voice_default") {
                        val isDef = selectedVoiceName.isBlank()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDef) Color(0x668B5CF6) else Color(0x331E0E38))
                                .border(
                                    1.dp,
                                    if (isDef) Color(0xFFE9D5FF) else Color(0x44A855F7),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedVoiceName = ""
                                    showVoicePickerDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sistem Varsayılanı",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily
                                )
                                Text(
                                    text = "Cihazın varsayılan Türkçe/seçili dil sesi",
                                    color = GlobalMorCamColors.LavenderSubtext,
                                    fontSize = 11.sp,
                                    fontFamily = InterFontFamily
                                )
                            }
                            if (isDef) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFF3E8FF), modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    items(availableTtsVoices.size, key = { idx -> availableTtsVoices[idx].name }) { idx ->
                        val voice = availableTtsVoices[idx]
                        val isSel = selectedVoiceName == voice.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSel) Color(0x668B5CF6) else Color(0x331E0E38))
                                .border(
                                    1.dp,
                                    if (isSel) Color(0xFFE9D5FF) else Color(0x44A855F7),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedVoiceName = voice.name
                                    showVoicePickerDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = voice.displayName,
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontFamily = InterFontFamily
                                )
                                Text(
                                    text = "${voice.localeTag} • ${if (voice.isNetwork) "Ağ Sesi" else "Çevrimdışı"}",
                                    color = GlobalMorCamColors.LavenderSubtext,
                                    fontSize = 10.5.sp,
                                    fontFamily = InterFontFamily
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "▶",
                                    color = Color(0xFFE9D5FF),
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0x44A855F7))
                                        .clickable {
                                            viewModel.previewTtsVoice(
                                                voiceName = voice.name,
                                                speed = ttsSpeed,
                                                pitch = ttsPitch,
                                                languageTag = appLanguage
                                            )
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFF3E8FF), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Mor Cam Backup Restore Mode Confirmation (Merge vs Overwrite) ──
        pendingImportJson?.let { jsonToImport ->
            val preview = remember(jsonToImport) { com.example.util.BackupHealthChecker.inspectJson(jsonToImport) }
            MorCamDialogShell(
                onDismissRequest = { if (!isBusy) pendingImportJson = null },
                title = "Geri Yükleme Modunu Seç",
                subtitle = if (preview.isValidJson) {
                    "Önizleme: ${preview.botCount} karakter • ${preview.messageCount} mesaj • ${preview.customProviderCount} özel API"
                } else {
                    "Mevcut API anahtarların korunur; verilerin nasıl yükleneceğini seç."
                },
                icon = Icons.Default.FileDownload
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlobalGlassPrimaryButton(
                        text = "Mevcutlarla Birleştir (Önerilen)",
                        loading = isBusy,
                        enabled = !isBusy,
                        onClick = { executeImportWithMode(jsonToImport, overwriteExisting = false) },
                        modifier = Modifier.fillMaxWidth().testTag("backup_import_merge_button")
                    )
                    GlobalGlassSecondaryButton(
                        text = "Üzerine Yaz (Mevcutları Temizle)",
                        enabled = !isBusy,
                        onClick = { executeImportWithMode(jsonToImport, overwriteExisting = true) },
                        modifier = Modifier.fillMaxWidth().testTag("backup_import_overwrite_button")
                    )
                    Text(
                        text = "Not: Birleştir modu mevcut karakterlerini korur ve yedekteki verileri ekler/günceller. Üzerine Yaz mevcut tüm sohbetleri silip yedeği yükler.",
                        color = GlobalMorCamColors.LavenderSubtext,
                        fontSize = 11.sp,
                        fontFamily = InterFontFamily
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SUB-DIALOGS & SHEETS
    // ═══════════════════════════════════════════════════════════════════════════

    if (showCatalogSearch) {
        ApiCatalogSearchDialog(
            rootSize = rootSize,
            blurredBg = blurredBg,
            configuredBuiltinSlots = configuredBuiltinSlots,
            configuredCustomUrls = customProviders.map { it.baseUrl.trimEnd('/') }.toSet(),
            onSelectProvider = { spec ->
                showCatalogSearch = false
                editingCatalogExistingCustom = if (spec.builtinSlot == null) {
                    customProviders.firstOrNull {
                        it.baseUrl.trimEnd('/').equals(spec.baseUrl.trimEnd('/'), ignoreCase = true)
                    }
                } else null
                configuringCatalogSpec = spec
            },
            onOpenCustomApi = {
                editingCustomProvider = null
                showCustomApiDialog = true
            },
            onDismiss = { showCatalogSearch = false }
        )
    }

    configuringCatalogSpec?.let { spec ->
        val existingCustom = editingCatalogExistingCustom
        val initialKey = when (spec.builtinSlot) {
            "gemini" -> geminiApiKey
            "openai" -> openaiApiKey
            "claude" -> claudeApiKey
            "groq" -> groqApiKey
            "openrouter" -> openRouterApiKey
            "nvidia" -> nvidiaApiKey
            "mistral" -> mistralApiKey
            else -> ""
        }
        val initialModel = when (spec.builtinSlot) {
            "gemini" -> geminiModel
            "openai" -> openaiModel
            "claude" -> claudeModel
            "groq" -> groqModel
            "openrouter" -> openRouterModel
            "nvidia" -> nvidiaModel
            "mistral" -> mistralModel
            else -> existingCustom?.modelName ?: spec.defaultModel
        }

        ApiProviderConfigureDialog(
            spec = spec,
            initialApiKey = initialKey,
            initialModel = initialModel,
            isEditingExisting = (spec.builtinSlot != null && initialKey.isNotBlank()) || existingCustom != null,
            rootSize = rootSize,
            blurredBg = blurredBg,
            onTestConnection = { baseUrl, key, model, format ->
                val resolvedKey = if (key.isBlank() && existingCustom != null) {
                    try {
                        com.example.util.KeystoreEncryptionManager.decrypt(existingCustom.apiKeyEncrypted)
                    } catch (_: Exception) {
                        ""
                    }
                } else key
                viewModel.testCustomProviderConnection(baseUrl, resolvedKey, model, format)
            },
            onFetchModels = { baseUrl, key ->
                val resolvedKey = if (key.isBlank() && existingCustom != null) {
                    try {
                        com.example.util.KeystoreEncryptionManager.decrypt(existingCustom.apiKeyEncrypted)
                    } catch (_: Exception) {
                        ""
                    }
                } else key
                viewModel.fetchModelsForEndpoint(baseUrl, resolvedKey)
            },
            onConfirm = { enteredKey, chosenModel, supportsTools ->
                val slot = spec.builtinSlot
                if (slot != null) {
                    when (slot) {
                        "gemini" -> {
                            if (enteredKey.isNotBlank()) geminiApiKey = enteredKey
                            geminiModel = chosenModel
                        }
                        "openai" -> {
                            if (enteredKey.isNotBlank()) openaiApiKey = enteredKey
                            openaiModel = chosenModel
                        }
                        "claude" -> {
                            if (enteredKey.isNotBlank()) claudeApiKey = enteredKey
                            claudeModel = chosenModel
                        }
                        "groq" -> {
                            if (enteredKey.isNotBlank()) groqApiKey = enteredKey
                            groqModel = chosenModel
                        }
                        "openrouter" -> {
                            if (enteredKey.isNotBlank()) openRouterApiKey = enteredKey
                            openRouterModel = chosenModel
                        }
                        "nvidia" -> {
                            if (enteredKey.isNotBlank()) nvidiaApiKey = enteredKey
                            nvidiaModel = chosenModel
                        }
                        "mistral" -> {
                            if (enteredKey.isNotBlank()) mistralApiKey = enteredKey
                            mistralModel = chosenModel
                        }
                    }
                    if (slot !in orderedApiKeys) {
                        orderedApiKeys = orderedApiKeys + slot
                    }
                    // Sistem #9 + U2: Clear cooldown immediately and trigger AI small utility model selection
                    com.example.data.managers.ProviderHealthTracker.resetHealth(slot)
                    if (enteredKey.isNotBlank()) {
                        viewModel.selectUtilityModelWithAi(
                            providerKey = slot,
                            apiKeyOverride = enteredKey,
                            silent = true
                        )
                    }
                } else if (existingCustom != null) {
                    viewModel.updateCustomProviderFull(
                        id = existingCustom.id,
                        label = spec.name,
                        baseUrl = spec.baseUrl,
                        apiKey = enteredKey.ifBlank { null },
                        modelName = chosenModel,
                        apiFormat = spec.apiFormat,
                        supportsFunctionCalling = supportsTools
                    )
                    val ck = "custom_${existingCustom.id}"
                    if (ck !in orderedApiKeys) orderedApiKeys = orderedApiKeys + ck
                } else {
                    viewModel.saveCustomProvider(
                        label = spec.name,
                        baseUrl = spec.baseUrl,
                        apiKey = enteredKey,
                        modelName = chosenModel,
                        apiFormat = spec.apiFormat,
                        supportsFunctionCalling = supportsTools,
                        onSaved = { newId ->
                            val ck = "custom_$newId"
                            if (ck !in orderedApiKeys) orderedApiKeys = orderedApiKeys + ck
                        }
                    )
                }
                configuringCatalogSpec = null
                editingCatalogExistingCustom = null
            },
            onDismiss = {
                configuringCatalogSpec = null
                editingCatalogExistingCustom = null
            }
        )
    }

    if (showCustomApiDialog) {
        val editing = editingCustomProvider
        CustomApiEditorDialog(
            editingProvider = editing,
            rootSize = rootSize,
            blurredBg = blurredBg,
            onTestConnection = { baseUrl, key, model, format ->
                val resolvedKey = if (key.isBlank() && editing != null) {
                    try {
                        com.example.util.KeystoreEncryptionManager.decrypt(editing.apiKeyEncrypted)
                    } catch (_: Exception) {
                        ""
                    }
                } else key
                viewModel.testCustomProviderConnection(baseUrl, resolvedKey, model, format)
            },
            onFetchModels = { baseUrl, key ->
                val resolvedKey = if (key.isBlank() && editing != null) {
                    try {
                        com.example.util.KeystoreEncryptionManager.decrypt(editing.apiKeyEncrypted)
                    } catch (_: Exception) {
                        ""
                    }
                } else key
                viewModel.fetchModelsForEndpoint(baseUrl, resolvedKey)
            },
            onSave = { label, baseUrl, key, model, format, supportsTools, utilityModel, detectedSmallModelsJson ->
                if (editing != null) {
                    viewModel.updateCustomProviderFull(
                        id = editing.id,
                        label = label,
                        baseUrl = baseUrl,
                        apiKey = key.ifBlank { null },
                        modelName = model,
                        apiFormat = format,
                        supportsFunctionCalling = supportsTools,
                        utilityModel = utilityModel,
                        detectedSmallModels = detectedSmallModelsJson
                    )
                } else {
                    viewModel.saveCustomProvider(
                        label = label,
                        baseUrl = baseUrl,
                        apiKey = key,
                        modelName = model,
                        apiFormat = format,
                        supportsFunctionCalling = supportsTools,
                        utilityModel = utilityModel,
                        detectedSmallModels = detectedSmallModelsJson,
                        onSaved = { newId ->
                            val ck = "custom_$newId"
                            if (ck !in orderedApiKeys) orderedApiKeys = orderedApiKeys + ck
                        }
                    )
                }
                showCustomApiDialog = false
                editingCustomProvider = null
            },
            onDismiss = {
                showCustomApiDialog = false
                editingCustomProvider = null
            }
        )
    }

    if (showFreeKeylessSheet) {
        FreeKeylessProvidersDialog(
            rootSize = rootSize,
            blurredBg = blurredBg,
            prioritizeFreeKeyless = prioritizeFreeKeyless,
            enablePollinations = enablePollinations,
            onTogglePollinations = { enablePollinations = it },
            pollinationsModel = pollinationsModel,
            onSelectPollinationsModel = { pollinationsModel = it },
            enableLlm7 = enableLlm7,
            onToggleLlm7 = { enableLlm7 = it },
            enableOvh = enableOvh,
            onToggleOvh = { enableOvh = it },
            ovhModel = ovhModel,
            onSelectOvhModel = { ovhModel = it },
            onTestPollinations = { model -> viewModel.testPollinationsConnection(model) },
            onTestLlm7 = { viewModel.testLlm7Connection() },
            onTestOvh = { model -> viewModel.testOvhConnection(model) },
            onDismiss = { showFreeKeylessSheet = false }
        )
    }

    if (showUpdateModal) {
        UpdateCheckerModal(onDismiss = { showUpdateModal = false })
    }

    if (showFallbackLogModal) {
        val logs = viewModel.providerFallbackLog.collectAsState().value
        FallbackLogModal(logs = logs, onDismiss = { showFallbackLogModal = false })
    }

    if (showFeatureFlagsPanel) {
        FeatureFlagsPanel(onDismiss = { showFeatureFlagsPanel = false })
    }
    } // end CompositionLocalProvider
}

// ═══════════════════════════════════════════════════════════════════════════════
// PRIVATE SUB-COMPONENTS FOR GLOBAL SETTINGS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ApiHubEntryRow(
    refPlates: GlobalSettingsRefPlates,
    icon: ImageVector,
    iconAccent: Color,
    title: String,
    subtitle: String,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeTint: Color = Color(0xFFF3E8FF),
    badgeCount: Int? = null,
    badgeCountSuffix: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isEmerald = iconAccent == GlobalMorCamColors.SoftEmerald || badgeTint == Color(0xFF4ADE80) || badgeTint == GlobalMorCamColors.SoftEmerald

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .cosmicTapSpring(
                interactionSource = interactionSource,
                pressedScale = 0.975f,
                cornerRadius = 18.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 13.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            modifier = Modifier
                .size(39.dp)
                .drawRefPlate(
                    bitmap = if (isEmerald) refPlates.iconEmerald else refPlates.iconPurple,
                    bleedFracX = 0.140f,
                    bleedFracY = 0.140f
                ),
            contentAlignment = Alignment.Center
        ) {
            // iconEmerald already contains the exact reference glowing mint 3-sparkle glyph
            if (!isEmerald) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.3.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = InterFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = GlobalMorCamColors.LavenderSubtext,
                fontSize = 11.sp,
                lineHeight = 14.2.sp,
                fontFamily = InterFontFamily,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .heightIn(min = 28.dp)
                .drawRefPlate(
                    bitmap = if (isEmerald) refPlates.badgeEmerald else refPlates.badgePurple,
                    bleedFracX = 0.075f,
                    bleedFracY = 0.182f
                )
                .padding(horizontal = 12.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            if (badgeCount != null) {
                CosmicAnimatedCounter(
                    count = badgeCount,
                    suffix = badgeCountSuffix,
                    color = badgeTint,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = badgeText,
                    color = badgeTint,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = InterFontFamily,
                    maxLines = 1
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFE9D5FF),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ActiveApiPriorityRow(
    index: Int,
    totalCount: Int,
    item: ActiveApiItemUi,
    isFirstPreference: Boolean,
    isTesting: Boolean,
    testResult: EmochiRepository.ProviderTestResult?,
    healthState: com.example.data.managers.ProviderHealthState? = null,
    activeUtilityModel: String = "",
    isSelectingUtility: Boolean = false,
    onAiSelectUtilityModel: () -> Unit = {},
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onTest: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val nowMs = System.currentTimeMillis()
    val inCooldown = healthState != null && healthState.cooldownUntilMs > nowMs
    val remSec = if (inCooldown) ((healthState!!.cooldownUntilMs - nowMs) / 1000L).coerceAtLeast(1L) else 0L
    val (healthLabel, healthColor, healthBg) = when {
        inCooldown && healthState?.lastFailureType == com.example.data.managers.ProviderFailureType.AUTH_ERROR ->
            Triple("⚠ Anahtar Hatası", Color(0xFFFCA5A5), Color(0x44881337))
        inCooldown && healthState?.lastFailureType == com.example.data.managers.ProviderFailureType.RATE_LIMIT ->
            Triple("⏳ Kota Bekliyor (${remSec}s)", Color(0xFFFDE68A), Color(0x4478350F))
        inCooldown ->
            Triple("⏳ Beklemede (${remSec}s)", Color(0xFFFDE68A), Color(0x4478350F))
        (healthState?.lastLatencyMs ?: 0L) > 0L ->
            Triple("● Hazır (${healthState!!.lastLatencyMs} ms)", GlobalMorCamColors.SoftEmerald, Color(0x33064E3B))
        else ->
            Triple("● Hazır", GlobalMorCamColors.SoftEmerald, Color(0x2B064E3B))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isFirstPreference) Color(0x702B1454) else Color(0x55120826))
            .border(
                width = if (isFirstPreference) 1.2.dp else 0.9.dp,
                color = if (isFirstPreference) Color(0xCCE9D5FF) else Color(0x55B794F4),
                shape = shape
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Rank badge (#1, #2, #3...)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFirstPreference) Brush.linearGradient(
                            listOf(Color(0xFF7C3AED), Color(0xFFC084FC))
                        ) else Brush.linearGradient(
                            listOf(Color(0x664C1D95), Color(0x662E1065))
                        )
                    )
                    .border(
                        1.dp,
                        if (isFirstPreference) Color(0xFFF3E8FF) else Color(0x66C084FC),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${index + 1}",
                    color = GlobalMorCamColors.PearlText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = InterFontFamily
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.name,
                        color = GlobalMorCamColors.PearlText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (index == 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isFirstPreference) Color(0x446EE7B7) else Color(0x33C084FC)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isFirstPreference) "İlk Tercih" else "API #1",
                                color = if (isFirstPreference) GlobalMorCamColors.SoftEmerald else Color(0xFFE9D5FF),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = InterFontFamily
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.model,
                        color = GlobalMorCamColors.BrightViolet,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(healthBg)
                            .border(0.6.dp, healthColor.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = healthLabel,
                            color = healthColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily,
                            maxLines = 1
                        )
                    }
                }
                // U2: AI-selected small utility/support model row for ALL providers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = if (activeUtilityModel.isNotBlank()) "✦ Destek: $activeUtilityModel" else "✦ Destek: AI Otomatik",
                        color = Color(0xFFA7F3D0),
                        fontSize = 10.2.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x4D5B21B6))
                            .border(0.7.dp, Color(0xAAD8B4FE), RoundedCornerShape(8.dp))
                            .clickable(enabled = !isSelectingUtility) { onAiSelectUtilityModel() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("ai_select_utility_${item.key}")
                    ) {
                        Text(
                            text = if (isSelectingUtility) "Seçiyor..." else "AI Seç",
                            color = Color(0xFFF3E8FF),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }

            // Reorder controls (Up / Down)
            if (totalCount > 1) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReorderIconButton(
                        icon = Icons.Default.ArrowUpward,
                        contentDescription = "Yukarı Taşı",
                        enabled = index > 0,
                        onClick = onMoveUp,
                        modifier = Modifier.testTag("api_move_up_$index")
                    )
                    ReorderIconButton(
                        icon = Icons.Default.ArrowDownward,
                        contentDescription = "Aşağı Taşı",
                        enabled = index < totalCount - 1,
                        onClick = onMoveDown,
                        modifier = Modifier.testTag("api_move_down_$index")
                    )
                }
            }
        }

        // Action row: Edit, Test, Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniGlassRowButton(
                icon = Icons.Default.Edit,
                label = "Düzenle",
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            )
            MiniGlassRowButton(
                icon = Icons.Default.PlayArrow,
                label = if (isTesting) "Test..." else "Test Et",
                loading = isTesting,
                onClick = onTest,
                modifier = Modifier.weight(1f)
            )
            MiniGlassRowButton(
                icon = Icons.Default.DeleteOutline,
                label = "Kaldır",
                tint = GlobalMorCamColors.SoftRose,
                onClick = onDelete,
                modifier = Modifier.weight(0.9f)
            )
        }

        testResult?.let { TestResultGlassBanner(it) }
    }
}

@Composable
private fun ReorderIconButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.90f else 1f,
        animationSpec = tween(100),
        label = "reorderScale"
    )
    Box(
        modifier = modifier
            .size(34.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(
                if (enabled) Brush.radialGradient(listOf(Color(0x887C3AED), Color(0x993B1470)))
                else Brush.radialGradient(listOf(Color(0x3D231144), Color(0x33130829)))
            )
            .border(
                BorderStroke(
                    width = if (enabled) 1.dp else 0.75.dp,
                    brush = if (enabled) MorCamTokens.RimGradient else MorCamTokens.SubtleRimGradient
                ),
                CircleShape
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) GlobalMorCamColors.PearlText else GlobalMorCamColors.MutedLavender.copy(alpha = 0.42f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun MiniGlassRowButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = GlobalMorCamColors.LavenderSubtext,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !loading) 0.95f else 1f,
        animationSpec = tween(100),
        label = "miniGlassBtnScale"
    )
    val pillShape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .heightIn(min = 38.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(pillShape)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0x7A24104A), Color(0x8E15082E), Color(0x7A24104A))
                )
            )
            .border(
                BorderStroke(
                    0.9.dp,
                    Brush.linearGradient(
                        listOf(tint.copy(alpha = 0.72f), Color(0x4D7C3AED), tint.copy(alpha = 0.55f))
                    )
                ),
                pillShape
            )
            .clickable(
                enabled = !loading,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = tint,
                strokeWidth = 1.6.dp,
                modifier = Modifier.size(13.dp)
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = LiterarySerifFontFamily,
            maxLines = 1
        )
    }
}

@Composable
private fun TokenStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66120726))
            .border(0.9.dp, Color(0x55B794F4), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = GlobalMorCamColors.MutedLavender,
            fontSize = 11.sp,
            fontFamily = InterFontFamily
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = GlobalMorCamColors.PearlText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = InterFontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SmallGlassActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x66180B30))
            .border(0.9.dp, Color(0x66C084FC), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlobalMorCamColors.BrightViolet,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = GlobalMorCamColors.PearlText,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = InterFontFamily,
            maxLines = 1
        )
    }
}
