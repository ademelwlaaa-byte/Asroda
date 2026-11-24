package com.example

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.animation.CosmicMotionConfig
import com.example.ui.animation.CosmicMotionPreferences
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.CosmicOrbLoader
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.LocalCosmicMotionPolicy
import com.example.ui.animation.MotionPolicy
import com.example.util.AppFeatureFlags
import com.example.ui.viewmodel.EmochiViewModel
import com.example.ui.viewmodel.UiState
import com.example.ui.screens.BotListScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreateBotWizardScreen
import com.example.ui.theme.EmochiBackground
import com.example.ui.theme.EmochiPrimary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: EmochiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        CosmicMotionPreferences.init(applicationContext)
        enableEdgeToEdge()
        // Handle proactive notification deep link
        if (intent?.action == "com.example.OPEN_CHAT") {
            val botId = intent.getStringExtra("botId")
            if (!botId.isNullOrBlank()) {
                viewModel.openBot(botId)
            }
        }
        setContent {
            val motionMode by CosmicMotionPreferences.mode.collectAsStateWithLifecycle()
            val reduceMotion by AppFeatureFlags.reduceMotion.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current
            var isAppInForeground by remember { mutableStateOf(true) }
            var lifecycleTick by remember { mutableStateOf(0) }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> {
                            isAppInForeground = true
                            lifecycleTick++
                        }
                        Lifecycle.Event.ON_STOP -> {
                            isAppInForeground = false
                        }
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val motionPolicy = remember(reduceMotion, isAppInForeground, lifecycleTick) {
                MotionPolicy.fromContext(
                    context = this@MainActivity,
                    reduceMotion = reduceMotion,
                    isAppInForeground = isAppInForeground
                )
            }
            val motionConfig = remember(motionMode, motionPolicy) {
                CosmicMotionConfig.effective(motionMode, motionPolicy)
            }

            CompositionLocalProvider(
                LocalCosmicMotionPolicy provides motionPolicy,
                LocalCosmicMotionConfig provides motionConfig
            ) {
                MyApplicationTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = EmochiBackground
                    ) {
                        EmochiAppMain(viewModel)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Sistem #11: Stop active TTS & smoothly pause background book music when app enters background
        // (idempotent with BookReaderView's ON_STOP observer so smooth 250ms fade & ON_START resume stay intact)
        viewModel.stopSpeaking()
        com.example.util.BookAudioController.pauseForBackground(fadeOutMs = 250L)
    }
}

private fun uiStateDepth(state: UiState): Int = when (state) {
    is UiState.Loading -> 0
    is UiState.Menu -> 1
    is UiState.SetupWizard -> 2
    is UiState.Chat -> 2
}

@Composable
fun EmochiAppMain(viewModel: EmochiViewModel) {
    val motionConfig = LocalCosmicMotionConfig.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val botList by viewModel.botList.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val activeBot by viewModel.activeBot.collectAsStateWithLifecycle()
    val immediateBotCache by viewModel.immediateBotCache.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val immediateOpeningMessageCache by viewModel.immediateOpeningMessageCache.collectAsStateWithLifecycle()
    val activeCharacterEmotions by viewModel.activeCharacterEmotions.collectAsStateWithLifecycle()
    val activeAffectionEvents by viewModel.activeAffectionEvents.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val baselineAnalysisState by viewModel.baselineAnalysisState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = uiState,
        transitionSpec = {
            val forward = uiStateDepth(targetState) >= uiStateDepth(initialState)
            CosmicMotionSpecs.cosmicPortalTransition(forward = forward, config = motionConfig)
        },
        label = "CosmicPortalScreenTransition"
    ) { state ->
        when (state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CosmicOrbLoader(size = 58.dp, label = "Evren hazırlanıyor...")
                }
            }

            is UiState.Menu -> {
                BotListScreen(
                    botList = botList,
                    userSettings = userSettings,
                    onOpenBot = { viewModel.openBot(it) },
                    onNewBot = { viewModel.startWizard() },
                    onDeleteBot = { viewModel.deleteBot(it) },
                    onSaveSettings = { viewModel.updateSettings(it) },
                    onExportData = { includeApiKeys -> viewModel.exportBackupJson(includeApiKeys) },
                    onImportData = { json, overwrite -> viewModel.importBackupJson(json, overwrite) },
                    onImportPresetBot = { viewModel.createPresetBot(it) },
                    onTogglePrivacy = { viewModel.updateBotProfile(it) },
                    onCloneBot = { viewModel.cloneBot(it) }
                )
            }

            is UiState.SetupWizard -> {
                val isCreatingBot by viewModel.isCreatingBot.collectAsStateWithLifecycle()
                val wizardSubmitError by viewModel.wizardSubmitError.collectAsStateWithLifecycle()
                CreateBotWizardScreen(
                    baselineAnalysisState = baselineAnalysisState,
                    isSubmitting = isCreatingBot,
                    submitError = wizardSubmitError,
                    onClearSubmitError = { viewModel.clearWizardSubmitError() },
                    onBack = {
                        viewModel.clearWizardSubmitError()
                        viewModel.resetBaselineAnalysisState()
                        viewModel.openMenu()
                    },
                    onFinish = { newBot, triggers, onDone ->
                        viewModel.createBotFromWizard(
                            bot = newBot,
                            triggers = triggers,
                            onComplete = { onDone(true) },
                            onFailure = { onDone(false) }
                        )
                    },
                    onGenerateOpening = { draft, forceRegenerate, toneHint ->
                        viewModel.generateOpeningForWizard(draft, forceRegenerate, toneHint)
                    },
                    onAnalyzeBaseline = { draft, forceReanalyze, editedFields ->
                        viewModel.analyzeBaseline(draft, forceReanalyze, editedFields)
                    },
                    onCancelBaseline = {
                        viewModel.cancelBaselineAnalysis()
                    }
                )
            }

            is UiState.Chat -> {
                BackHandler {
                    viewModel.openMenu()
                }
                val currentBot = activeBot?.takeIf { it.id == state.botId }
                    ?: immediateBotCache?.takeIf { it.id == state.botId }
                    ?: botList.firstOrNull { it.id == state.botId }
                    ?: activeBot
                    ?: immediateBotCache

                if (currentBot == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CosmicOrbLoader(size = 54.dp, label = "Sahne açılıyor...")
                    }
                } else {
                    val keyChars = remember(currentBot.keyCharactersJson) {
                        viewModel.repository.parseKeyCharacters(currentBot.keyCharactersJson)
                    }
                    val effectiveMessages = if (activeMessages.isNotEmpty()) {
                        activeMessages
                    } else {
                        immediateOpeningMessageCache.filter { it.botId == currentBot.id }
                    }

                    ChatScreen(
                        bot = currentBot,
                        messages = effectiveMessages,
                        userSettings = userSettings,
                        isSending = isSending,
                        errorMessage = errorMessage,
                        keyCharacters = keyChars,
                        characterEmotions = activeCharacterEmotions,
                        affectionEvents = activeAffectionEvents,
                        onBack = { viewModel.openMenu() },
                        onSendMessage = { text -> viewModel.sendMessage(text) },
                        onContinueScene = { viewModel.continueStoryScene() },
                        onRegenerate = { viewModel.regenerateLastResponse() },
                        onEditMessage = { msgId, newText -> viewModel.editMessage(msgId, newText) },
                        onDeleteMessage = { msgId -> viewModel.deleteMessage(msgId) },
                        onSaveBotProfile = { updatedBot, updatedChars ->
                            val json = viewModel.repository.serializeKeyCharacters(updatedChars)
                            viewModel.updateBotProfile(updatedBot.copy(keyCharactersJson = json))
                        },
                        onResetChat = { fullReset -> viewModel.resetChat(currentBot.id, fullReset) },
                        onDeleteBot = { viewModel.deleteBot(currentBot.id) },
                        onSpeakText = { text -> viewModel.speakText(text) },
                        onSpeakMessage = { msgId, text -> viewModel.speakMessage(msgId, text) },
                        speakingMessageIdFlow = viewModel.speakingMessageId,
                        onStopSpeaking = { viewModel.stopSpeaking() },
                        onEnsureOpeningMessage = { viewModel.ensureOpeningMessageForActiveBot() },
                        onClearError = { viewModel.clearError() },
                        onRetryMessage = { msgId -> viewModel.retryMessage(msgId) },
                        onRetryFromBanner = { viewModel.retryWithSoftenedPrompt(currentBot.id) },
                        onRewindToMessage = { msgId -> viewModel.rewindToMessage(currentBot.id, msgId) },
                        onCloneBot = { viewModel.cloneBot(currentBot.id) },
                        streamingMsgIdFlow = viewModel.streamingMsgId,
                        streamingTextFlow = viewModel.streamingText,
                        isStreamingThinkingFlow = viewModel.isStreamingThinking,
                        onStopGeneration = { viewModel.stopGeneration() }
                    )
                }
            }
        }
    }
}
