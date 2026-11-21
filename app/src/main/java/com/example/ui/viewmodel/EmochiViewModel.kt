package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.BaselineEmotionEngine
import com.example.data.repository.EmochiRepository
import com.example.util.AppFeatureFlags
import com.example.util.DynamicDifficultyCalibrator
import com.example.work.RoutineNotificationWorker
import org.json.JSONObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import java.util.UUID

sealed class UiState {
    object Loading : UiState()
    object Menu : UiState()
    object SetupWizard : UiState()
    data class Chat(val botId: String) : UiState()
}

class EmochiViewModel(application: Application) : AndroidViewModel(application) {
    val repository = EmochiRepository(AppDatabase.getInstance(application), application)

    init {
        // Feature flags SharedPreferences başlat
        AppFeatureFlags.init(application)
        // WorkManager rutin bildirimleri
        if (AppFeatureFlags.isRoutineNotifEnabled) {
            RoutineNotificationWorker.schedule(application)
        }
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Menu)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.deleteEmptyMessages()
            } catch (_: Exception) {}
        }
    }

    val botList: StateFlow<List<BotEntity>> = repository.allBots
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val providerFallbackLog = repository.providerFallbackLog

    suspend fun testLlm7Connection(): EmochiRepository.ProviderTestResult {
        return repository.testLlm7Connection()
    }

    suspend fun testPollinationsConnection(modelName: String = "openai"): EmochiRepository.ProviderTestResult {
        return repository.testPollinationsConnection(modelName)
    }

    suspend fun fetchNvidiaModels(apiKey: String): List<String> {
        return repository.fetchNvidiaModels(apiKey)
    }

    suspend fun simulateNvidiaRateLimitTest(apiKey: String, modelName: String = ""): EmochiRepository.ProviderTestResult {
        return repository.simulateNvidiaRateLimitTest(apiKey, modelName)
    }

    private val _activeBotId = MutableStateFlow<String?>(null)
    val activeBotId: StateFlow<String?> = _activeBotId.asStateFlow()

    private val _immediateBotCache = MutableStateFlow<BotEntity?>(null)
    val immediateBotCache: StateFlow<BotEntity?> = _immediateBotCache.asStateFlow()

    private val _immediateOpeningMessageCache = MutableStateFlow<List<MessageEntity>>(emptyList())
    val immediateOpeningMessageCache: StateFlow<List<MessageEntity>> = _immediateOpeningMessageCache.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeBot: StateFlow<BotEntity?> = _activeBotId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getBotFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<MessageEntity>> = _activeBotId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMessagesFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeCharacterEmotions: StateFlow<List<CharacterEmotionEntity>> = _activeBotId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getCharacterEmotionsFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeAffectionEvents: StateFlow<List<com.example.data.local.AffectionEventEntity>> = _activeBotId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getAffectionEventsFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val customProviders: StateFlow<List<com.example.data.local.CustomProviderEntity>> = repository.getCustomProvidersFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    suspend fun testCustomProviderConnection(
        baseUrl: String,
        apiKey: String,
        modelName: String,
        apiFormat: String
    ): EmochiRepository.ProviderTestResult {
        return repository.testCustomProviderConnection(baseUrl, apiKey, modelName, apiFormat)
    }

    suspend fun testOvhConnection(modelName: String = "Meta-Llama-3_3-70B-Instruct"): EmochiRepository.ProviderTestResult {
        return repository.testOvhConnection(modelName)
    }

    suspend fun fetchModelsForEndpoint(baseUrl: String, apiKey: String): List<String> {
        return repository.fetchModelsFromEndpoint(baseUrl, apiKey)
    }

    fun saveCustomProvider(
        label: String,
        baseUrl: String,
        apiKey: String,
        modelName: String,
        apiFormat: String,
        supportsFunctionCalling: Boolean,
        utilityModel: String = "",
        detectedSmallModels: String = "[]",
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val encryptedKey = com.example.util.KeystoreEncryptionManager.encrypt(apiKey.trim())
            val cleanUtil = utilityModel.trim()
            val utilSource = when {
                cleanUtil.isNotBlank() -> "manual"
                detectedSmallModels.isNotBlank() && detectedSmallModels != "[]" -> "auto"
                else -> ""
            }
            val entity = com.example.data.local.CustomProviderEntity(
                label = label.ifBlank { modelName },
                baseUrl = baseUrl.trim(),
                apiKeyEncrypted = encryptedKey,
                modelName = modelName.trim(),
                apiFormat = apiFormat,
                supportsFunctionCalling = supportsFunctionCalling,
                utilityModel = cleanUtil,
                utilityModelSource = utilSource,
                detectedSmallModels = detectedSmallModels.ifBlank { "[]" }
            )
            val newId = repository.insertCustomProvider(entity)
            com.example.data.managers.ProviderHealthTracker.resetHealth("custom_$newId")
            onSaved(newId)
        }
    }

    fun updateCustomProviderModel(id: Long, newModelName: String) {
        viewModelScope.launch {
            val existing = repository.getCustomProviderById(id) ?: return@launch
            val updated = existing.copy(modelName = newModelName.trim())
            repository.updateCustomProvider(updated)
            com.example.data.managers.ProviderHealthTracker.resetHealth("custom_$id")
            val currentSettings = repository.getOrCreateSettings()
            if (currentSettings.selectedProvider == "custom_$id") {
                updateSettings(currentSettings.copy(selectedModel = newModelName.trim()))
            }
        }
    }

    fun updateCustomProviderFull(
        id: Long,
        label: String,
        baseUrl: String,
        apiKey: String?,
        modelName: String,
        apiFormat: String,
        supportsFunctionCalling: Boolean,
        utilityModel: String? = null,
        detectedSmallModels: String? = null
    ) {
        viewModelScope.launch {
            val existing = repository.getCustomProviderById(id) ?: return@launch
            val finalEncryptedKey = if (!apiKey.isNullOrBlank()) {
                com.example.util.KeystoreEncryptionManager.encrypt(apiKey.trim())
            } else {
                existing.apiKeyEncrypted
            }
            val finalUtil = utilityModel?.trim() ?: existing.utilityModel
            val finalDetected = detectedSmallModels?.ifBlank { "[]" } ?: existing.detectedSmallModels
            val finalUtilSource = when {
                finalUtil.isNotBlank() -> "manual"
                finalDetected != "[]" -> "auto"
                else -> ""
            }
            val updated = existing.copy(
                label = label.ifBlank { modelName },
                baseUrl = baseUrl.trim(),
                apiKeyEncrypted = finalEncryptedKey,
                modelName = modelName.trim(),
                apiFormat = apiFormat,
                supportsFunctionCalling = supportsFunctionCalling,
                utilityModel = finalUtil,
                utilityModelSource = finalUtilSource,
                detectedSmallModels = finalDetected
            )
            repository.updateCustomProvider(updated)
            com.example.data.managers.ProviderHealthTracker.resetHealth("custom_$id")
            val currentSettings = repository.getOrCreateSettings()
            if (currentSettings.selectedProvider == "custom_$id") {
                updateSettings(currentSettings.copy(selectedModel = modelName.trim()))
            }
        }
    }

    fun deleteCustomProvider(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomProvider(id)
            val currentSettings = repository.getOrCreateSettings()
            if (currentSettings.selectedProvider == "custom_$id") {
                updateSettings(currentSettings.copy(selectedProvider = "gemini"))
            }
        }
    }

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Typewriter streaming
    private val _streamingMsgId = MutableStateFlow<String?>(null)
    val streamingMsgId: StateFlow<String?> = _streamingMsgId.asStateFlow()

    private val _streamingText = MutableStateFlow<String?>(null)
    val streamingText: StateFlow<String?> = _streamingText.asStateFlow()

    private var streamingJob: kotlinx.coroutines.Job? = null

    private fun startTypewriter(msgId: String, fullText: String) {
        streamingJob?.cancel()
        _streamingMsgId.value = msgId
        _streamingText.value = ""
        maybeAutoSpeakReply(msgId, fullText)
        streamingJob = viewModelScope.launch {
            val sb = StringBuilder()
            for (ch in fullText) {
                sb.append(ch)
                _streamingText.value = sb.toString()
                kotlinx.coroutines.delay(7L)
            }
            // Streaming done — clear so DB text takes over
            _streamingMsgId.value = null
            _streamingText.value = null
        }
    }

    private val ttsManager = com.example.util.ChatTtsManager(application)
    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking
    val speakingMessageId: StateFlow<String?> = ttsManager.speakingMessageId
    val availableTtsVoices: StateFlow<List<com.example.util.ChatTtsManager.TtsVoiceOption>> = ttsManager.availableVoices

    init {
        viewModelScope.launch {
            repository.getOrCreateSettings()
            repository.initStarterBotsIfEmpty()
        }
    }

    private fun maybeAutoSpeakReply(messageId: String, fullText: String) {
        val settings = userSettings.value ?: return
        if (settings.enableTts && AppFeatureFlags.isTtsAutoReadRepliesEnabled) {
            speakMessage(messageId = messageId, text = fullText)
        }
    }

    fun speakMessage(messageId: String, text: String, speed: Float? = null, pitch: Float? = null) {
        if (ttsManager.speakingMessageId.value == messageId && ttsManager.isSpeaking.value) {
            ttsManager.stop()
            return
        }
        val settings = userSettings.value
        val effSpeed = speed ?: settings?.ttsSpeed ?: 1.0f
        val effPitch = pitch ?: settings?.ttsPitch ?: 1.0f
        val voiceName = settings?.selectedVoiceName ?: ""
        val lang = settings?.appLanguage ?: "tr"
        ttsManager.speakMessage(
            messageId = messageId,
            rawText = text,
            speed = effSpeed,
            pitch = effPitch,
            voiceName = voiceName,
            languageTag = lang,
            readActions = AppFeatureFlags.isTtsReadActionsEnabled
        )
    }

    fun speakText(text: String, speed: Float? = null, pitch: Float? = null) {
        speakMessage(messageId = "tts_legacy_global", text = text, speed = speed, pitch = pitch)
    }

    fun previewTtsVoice(voiceName: String, speed: Float, pitch: Float, languageTag: String = "tr") {
        ttsManager.previewVoice(
            voiceName = voiceName,
            speed = speed,
            pitch = pitch,
            languageTag = languageTag
        )
    }

    fun refreshTtsVoices() {
        ttsManager.refreshVoices()
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }

    fun openMenu() {
        stopSpeaking()
        _errorMessage.value = null
        _activeBotId.value = null
        _immediateBotCache.value = null
        _immediateOpeningMessageCache.value = emptyList()
        EmochiRepository.activeBotId = null
        _uiState.value = UiState.Menu
    }

    fun startWizard() {
        stopSpeaking()
        _errorMessage.value = null
        _uiState.value = UiState.SetupWizard
    }

    fun openBot(botId: String) {
        stopSpeaking()
        _errorMessage.value = null
        val cachedBot = botList.value.firstOrNull { it.id == botId } ?: _immediateBotCache.value?.takeIf { it.id == botId }
        if (cachedBot != null) {
            _immediateBotCache.value = cachedBot
        }
        if (_activeBotId.value != botId) {
            _activeBotId.value = botId
        }
        EmochiRepository.activeBotId = botId
        _uiState.value = UiState.Chat(botId)

        viewModelScope.launch {
            ensureOpeningMessage(botId)
        }
    }

    fun ensureOpeningMessageForActiveBot() {
        val botId = _activeBotId.value ?: return
        viewModelScope.launch {
            ensureOpeningMessage(botId)
        }
    }

    private val openingMutex = kotlinx.coroutines.sync.Mutex()

    suspend fun ensureOpeningMessage(botId: String) {
        openingMutex.withLock {
            var bot = repository.getBot(botId)
            var retryCount = 0
            while (bot == null && retryCount < 5) {
                kotlinx.coroutines.delay(100)
                bot = repository.getBot(botId)
                retryCount++
            }

            if (bot != null) {
                val existingMsgs = repository.getMessageListForBot(botId)
                val hasInjectedRefMsgs = existingMsgs.size == 3 && existingMsgs.all { it.id.contains("_ref_msg_") }
                if (existingMsgs.isEmpty() || hasInjectedRefMsgs) {
                    if (hasInjectedRefMsgs) {
                        repository.resetMessagesForBot(bot.id)
                    }
                    val openingMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = bot.id,
                        role = "assistant",
                        text = repository.resolveOpeningText(bot),
                        timestamp = System.currentTimeMillis()
                    )
                    repository.saveMessage(openingMsg)
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // ── Wizard baseline analysis state (Single unified, cancellable flow) ─────

    sealed class BaselineAnalysisUiState {
        object Idle : BaselineAnalysisUiState()
        object Loading : BaselineAnalysisUiState()
        data class Success(
            val result: BaselineEmotionEngine.BaselineAnalysisResult,
            val updatedBot: BotEntity,
            val isLowConfidence: Boolean,
            val resetUserEdits: Boolean = false
        ) : BaselineAnalysisUiState()
        data class Error(
            val message: String,
            val fallbackResult: BaselineEmotionEngine.BaselineAnalysisResult
        ) : BaselineAnalysisUiState()
    }

    private val _baselineAnalysisState = MutableStateFlow<BaselineAnalysisUiState>(BaselineAnalysisUiState.Idle)
    val baselineAnalysisState: StateFlow<BaselineAnalysisUiState> = _baselineAnalysisState.asStateFlow()

    private var _baselineAnalysisJob: kotlinx.coroutines.Job? = null

    fun analyzeBaseline(
        bot: BotEntity,
        forceReanalyze: Boolean = false,
        userEditedFields: Set<String> = emptySet()
    ) {
        _baselineAnalysisJob?.cancel()
        _baselineAnalysisState.value = BaselineAnalysisUiState.Loading
        _baselineAnalysisJob = viewModelScope.launch {
            try {
                val (updatedBot, result) = repository.runBaselineAnalysis(bot, forceReanalyze, userEditedFields)
                _baselineAnalysisState.value = BaselineAnalysisUiState.Success(
                    result = result,
                    updatedBot = updatedBot,
                    isLowConfidence = result.confidence < 60,
                    resetUserEdits = forceReanalyze
                )
            } catch (ce: kotlinx.coroutines.CancellationException) {
                throw ce
            } catch (e: Exception) {
                val effectiveName = if (bot.mode == "universe" && bot.universeName.isNotBlank()) bot.universeName else bot.aiName
                val fallback = BaselineEmotionEngine.calculateDeterministicFallback(
                    botId = bot.id,
                    aiName = effectiveName,
                    personality = bot.aiPersonality,
                    scenario = bot.scenario,
                    userCharName = bot.userCharName,
                    userCharDesc = bot.userCharDesc
                )
                _baselineAnalysisState.value = BaselineAnalysisUiState.Error(
                    message = e.message ?: "Analiz başarısız. Otomatik tahmin uygulandı.",
                    fallbackResult = fallback
                )
            }
        }
    }

    fun cancelBaselineAnalysis() {
        _baselineAnalysisJob?.cancel()
        _baselineAnalysisJob = null
        if (_baselineAnalysisState.value is BaselineAnalysisUiState.Loading) {
            _baselineAnalysisState.value = BaselineAnalysisUiState.Idle
        }
    }

    fun resetBaselineAnalysisState() {
        _baselineAnalysisJob?.cancel()
        _baselineAnalysisJob = null
        _baselineAnalysisState.value = BaselineAnalysisUiState.Idle
    }

    fun createBotFromWizard(
        bot: BotEntity,
        triggers: List<com.example.data.local.SensitiveTriggerEntity> = emptyList(),
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isSending.value = true
                repository.saveBot(bot)

                // Always persist user-confirmed triggers anchored strictly to bot.id
                val cleanTriggers = triggers
                    .filter { it.triggerTopic.isNotBlank() }
                    .map { it.copy(botId = bot.id) }
                if (cleanTriggers.isNotEmpty()) {
                    try { repository.saveSensitiveTriggers(bot.id, cleanTriggers) } catch (_: Exception) {}
                }

                // In Universe mode, mirror cleaned KeyCharacters into CastMemberEntity rows (0 extra tokens)
                if (bot.mode == "universe") {
                    try { repository.syncWizardCastMembers(bot) } catch (_: Exception) {}
                }

                val openingMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = bot.id,
                    role = "assistant",
                    text = repository.resolveOpeningText(bot),
                    timestamp = System.currentTimeMillis()
                )
                repository.saveMessage(openingMsg)

                resetBaselineAnalysisState()
                _activeBotId.value = bot.id
                EmochiRepository.activeBotId = bot.id
                _uiState.value = UiState.Chat(bot.id)
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "Bot oluşturulamadı: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    private val sendMutex = kotlinx.coroutines.sync.Mutex()

    // Son özetlemenin gerçekleştiği mesaj sayısını botId başına tutar
    private val lastSummarizationMsgCountMap = java.util.concurrent.ConcurrentHashMap<String, Int>()

    private fun shouldRunSummarization(botId: String, totalMsgCount: Int): Boolean {
        val lastCount = lastSummarizationMsgCountMap[botId] ?: 0
        val sinceLastSummarization = totalMsgCount - lastCount
        // İlk 10 mesajda tetikleme — henüz anlatılacak pek bir şey yok
        if (totalMsgCount < 10) return false
        // Her 6 mesajda tetikle VEYA son özetlemeden bu yana 12+ mesaj geçmişse
        return totalMsgCount % 6 == 0 || sinceLastSummarization >= 12
    }

    data class MemoryCommitNotice(
        val id: Long = System.currentTimeMillis(),
        val title: String,
        val subtitle: String,
        val isLivingMemory: Boolean
    )

    private val _memoryCommitNotice = MutableStateFlow<MemoryCommitNotice?>(null)
    val memoryCommitNotice: StateFlow<MemoryCommitNotice?> = _memoryCommitNotice.asStateFlow()

    val lastRagRouteMode: StateFlow<com.example.data.repository.MemoryEngine.RagRouteMode> =
        repository.lastRagRouteMode

    private val _archivedMemories = MutableStateFlow<List<com.example.data.local.ArchivedMemoryEntity>>(emptyList())
    val archivedMemories: StateFlow<List<com.example.data.local.ArchivedMemoryEntity>> = _archivedMemories.asStateFlow()

    fun dismissMemoryCommitNotice() {
        _memoryCommitNotice.value = null
    }

    private fun emitMemoryCommitNotice(
        summary: EmochiRepository.MemoryCommitSummary,
        defaultPov: String
    ) {
        if (!summary.hasAnyChange) return
        val crudParts = mutableListOf<String>()
        if (summary.savedFactsCount > 0) crudParts.add("+${summary.savedFactsCount} eklendi")
        if (summary.updatedFactsCount > 0) crudParts.add("${summary.updatedFactsCount} güncellendi")
        if (summary.deletedFactsCount > 0) crudParts.add("${summary.deletedFactsCount} arşivlendi")
        val crudText = crudParts.joinToString(" • ")

        val hasLiving = !summary.savedMemoryTitle.isNullOrBlank()
        val notice = when {
            hasLiving && crudText.isNotBlank() -> MemoryCommitNotice(
                title = "Yeni Anı & Neural Vault Güncellendi",
                subtitle = "${summary.savedMemoryPov ?: defaultPov}'ın gözünden • $crudText",
                isLivingMemory = true
            )
            hasLiving -> MemoryCommitNotice(
                title = "Yeni Anı: ${summary.savedMemoryTitle}",
                subtitle = "${summary.savedMemoryPov ?: defaultPov}'ın gözünden işlendi",
                isLivingMemory = true
            )
            else -> MemoryCommitNotice(
                title = "Neural Vault Güncellendi",
                subtitle = crudText.ifBlank { "Bellek kaydı güncellendi" },
                isLivingMemory = false
            )
        }
        viewModelScope.launch {
            _memoryCommitNotice.value = notice
            kotlinx.coroutines.delay(4500L)
            if (_memoryCommitNotice.value?.id == notice.id) {
                _memoryCommitNotice.value = null
            }
        }
    }

    private fun schedulePostTurnMemoryExtraction(
        bot: BotEntity,
        allMsgs: List<MessageEntity>,
        userText: String,
        aiReply: String
    ) {
        if (bot.id.startsWith("book_") || bot.mode == "book") return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val summary = repository.extractAndCommitTurnMemories(
                    bot = bot,
                    recentMessages = allMsgs,
                    latestUserMsg = userText,
                    latestAiReply = aiReply
                )
                if (summary.hasAnyChange) {
                    emitMemoryCommitNotice(summary, bot.aiName)
                    if (_activeBotId.value == bot.id) {
                        _archivedMemories.value = repository.getArchivedMemories(bot.id)
                    }
                }
            } catch (_: Exception) {
                // Silent background extraction — never interrupts chat flow
            }
        }
    }

    private fun markSummarizationDone(botId: String, msgCount: Int) {
        lastSummarizationMsgCountMap[botId] = msgCount
    }

    private suspend fun withSendLock(block: suspend () -> Unit) {
        if (!sendMutex.tryLock()) {
            _isSending.value = false
            return
        }
        try {
            block()
        } finally {
            sendMutex.unlock()
            _isSending.value = false
        }
    }

    fun sendMessage(text: String) {
        val botId = _activeBotId.value ?: return
        val sanitizedText = repository.sanitizeUserInput(text)
        if (sanitizedText.isBlank() || _isSending.value) return
        _isSending.value = true

        viewModelScope.launch {
            withSendLock {
                val currentBot = activeBot.value ?: repository.getBot(botId) ?: return@withSendLock
                try {
                    _errorMessage.value = null
                    repository.resetRegenerateCount(botId)

                    val now = System.currentTimeMillis()
                    val userMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = botId,
                        role = "user",
                        text = sanitizedText,
                        timestamp = now
                    )
                    repository.saveMessage(userMsg)

                    val currentMsgs = repository.getMessageListForBot(botId)

                    val replyResult = repository.generateAiReply(currentBot, currentMsgs)
                    val replyText = replyResult.replyText
                    if (replyText.isBlank()) {
                        throw IllegalStateException("Model boş yanıt döndürdü.")
                    }
                    if (replyResult.systemPromptSnapshot.isNotBlank()) {
                        _lastSystemPrompt.value = replyResult.systemPromptSnapshot
                    }

                    val aiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = botId,
                        role = "assistant",
                        text = replyText,
                        timestamp = (now + 10L).coerceAtLeast(System.currentTimeMillis()),
                        provider = replyResult.usedProvider
                    )
                    repository.saveMessage(aiMsg)
                    startTypewriter(aiMsg.id, replyText)

                    repository.saveBot(currentBot.copy(updatedAt = System.currentTimeMillis()))

                    val finalMsgs = currentMsgs + aiMsg
                    if (replyResult.memoryCommitSummary.hasAnyChange) {
                        emitMemoryCommitNotice(replyResult.memoryCommitSummary, currentBot.aiName)
                        loadArchivedMemories(botId)
                    }
                    schedulePostTurnMemoryExtraction(currentBot, finalMsgs, sanitizedText, replyText)
                    if (shouldRunSummarization(botId, finalMsgs.size)) {
                        repository.updateMemorySummaries(currentBot, finalMsgs)
                        markSummarizationDone(botId, finalMsgs.size)
                    }

                    // Y5 — Dinamik Zorluk Kalibrasyonu (her 20 mesajda bir)
                    if (DynamicDifficultyCalibrator.shouldRun(finalMsgs.size)) {
                        val calib = DynamicDifficultyCalibrator.calibrate(currentBot, finalMsgs)
                        if (kotlin.math.abs(calib.delta) > 0.001f) {
                            val updatedBot = repository.getBot(botId)
                            if (updatedBot != null) {
                                repository.saveBot(updatedBot.copy(
                                    baseAffectionDifficulty = calib.newDifficulty.toDouble()
                                ))
                                android.util.Log.i("DynDiff",
                                    "[Y5] bot=${currentBot.aiName} " +
                                    "old=${calib.oldDifficulty} new=${calib.newDifficulty} " +
                                    "reason=${calib.reason}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    val friendlyMsg = extractUserFriendlyErrorMessage(e)
                    _errorMessage.value = friendlyMsg
                }
            }
        }
    }

    fun retryMessage(msgId: String) {
        val botId = _activeBotId.value ?: return
        if (_isSending.value) return
        _isSending.value = true

        viewModelScope.launch {
            withSendLock {
                val currentBot = activeBot.value ?: repository.getBot(botId) ?: return@withSendLock
                try {
                    _errorMessage.value = null
                    repository.deleteMessage(msgId)

                    val currentMsgs = repository.getMessageListForBot(botId)
                    val replyResult = repository.generateAiReply(currentBot, currentMsgs)
                    val replyText = replyResult.replyText
                    if (replyText.isBlank()) {
                        throw IllegalStateException("Model boş yanıt döndürdü.")
                    }

                    val now = System.currentTimeMillis()
                    val aiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = botId,
                        role = "assistant",
                        text = replyText,
                        timestamp = now,
                        status = "success",
                        provider = replyResult.usedProvider
                    )
                    repository.saveMessage(aiMsg)
                    startTypewriter(aiMsg.id, replyText)
                    repository.saveBot(currentBot.copy(updatedAt = now))

                    val finalMsgs = currentMsgs + aiMsg
                    if (replyResult.memoryCommitSummary.hasAnyChange) {
                        emitMemoryCommitNotice(replyResult.memoryCommitSummary, currentBot.aiName)
                        loadArchivedMemories(botId)
                    }
                    val lastUserText = currentMsgs.lastOrNull { it.role == "user" }?.text.orEmpty()
                    if (lastUserText.isNotBlank()) {
                        schedulePostTurnMemoryExtraction(currentBot, finalMsgs, lastUserText, replyText)
                    }
                    if (shouldRunSummarization(botId, finalMsgs.size)) {
                        repository.updateMemorySummaries(currentBot, finalMsgs)
                        markSummarizationDone(botId, finalMsgs.size)
                    }
                } catch (e: Exception) {
                    val friendlyMsg = extractUserFriendlyErrorMessage(e)
                    _errorMessage.value = friendlyMsg
                }
            }
        }
    }

    fun regenerateLastResponse() {
        val botId = _activeBotId.value ?: return
        if (_isSending.value) return
        _isSending.value = true

        viewModelScope.launch {
            withSendLock {
                val currentBot = activeBot.value ?: repository.getBot(botId) ?: return@withSendLock
                try {
                    _errorMessage.value = null

                    val msgs = repository.getMessageListForBot(botId)
                    if (msgs.isEmpty()) return@withSendLock

                    val lastIsUser = msgs.lastOrNull()?.role == "user"
                    val (remainingMsgs, targetMsg) = if (lastIsUser) {
                        Pair(msgs, null)
                    } else {
                        val lastAssistantIndex = msgs.indexOfLast { it.role == "assistant" }
                        if (lastAssistantIndex == -1) Pair(msgs, null)
                        else Pair(msgs.subList(0, lastAssistantIndex), msgs[lastAssistantIndex])
                    }

                    val botToUse = if (currentBot.previousEmotionState.isNotBlank()) {
                        val reverted = currentBot.copy(emotionState = currentBot.previousEmotionState)
                        repository.saveBot(reverted)
                        reverted
                    } else currentBot

                    // Generate first before deleting targetMsg to prevent wiping message on network error
                    repository.incrementRegenerateCount(botId)
                    val replyResult = repository.generateAiReply(botToUse, remainingMsgs)
                    val replyText = replyResult.replyText
                    if (replyText.isBlank()) {
                        throw IllegalStateException("Model boş yanıt döndürdü.")
                    }

                    if (targetMsg != null) {
                        repository.deleteMessage(targetMsg.id)
                    }

                    val newAiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = botId,
                        role = "assistant",
                        text = replyText,
                        timestamp = System.currentTimeMillis(),
                        provider = replyResult.usedProvider
                    )
                    repository.saveMessage(newAiMsg)
                    startTypewriter(newAiMsg.id, replyText)
                } catch (e: Exception) {
                    _errorMessage.value = e.message ?: "Yeniden oluşturulamadı."
                }
            }
        }
    }

    fun editMessage(msgId: String, newText: String) {
        val botId = _activeBotId.value ?: return
        val trimmedNewText = newText.trim()
        if (trimmedNewText.isBlank() || _isSending.value) return
        _isSending.value = true

        viewModelScope.launch {
            withSendLock {
                val currentBot = activeBot.value ?: repository.getBot(botId) ?: return@withSendLock
                try {
                    _errorMessage.value = null
                    val msgs = repository.getMessageListForBot(botId)
                    val idx = msgs.indexOfFirst { it.id == msgId }
                    if (idx == -1) return@withSendLock

                    val isUserMsg = msgs[idx].role == "user"
                    val editedMsg = msgs[idx].copy(text = trimmedNewText, timestamp = System.currentTimeMillis())

                    if (isUserMsg) {
                        val botToUse = if (currentBot.previousEmotionState.isNotBlank()) {
                            val reverted = currentBot.copy(emotionState = currentBot.previousEmotionState)
                            repository.saveBot(reverted)
                            reverted
                        } else currentBot

                        val truncatedList = msgs.subList(0, idx) + editedMsg
                        val replyResult = repository.generateAiReply(botToUse, truncatedList)
                        val replyText = replyResult.replyText
                        if (replyText.isBlank()) {
                            throw IllegalStateException("Model boş yanıt döndürdü.")
                        }

                        // Delete old trailing messages ONLY after generation succeeds
                        for (i in (idx + 1) until msgs.size) {
                            repository.deleteMessage(msgs[i].id)
                        }
                        repository.saveMessage(editedMsg)

                        val newAiMsg = MessageEntity(
                            id = UUID.randomUUID().toString(),
                            botId = botId,
                            role = "assistant",
                            text = replyText,
                            timestamp = (editedMsg.timestamp + 10L).coerceAtLeast(System.currentTimeMillis()),
                            provider = replyResult.usedProvider
                        )
                        repository.saveMessage(newAiMsg)
                        startTypewriter(newAiMsg.id, replyText)
                    } else {
                        repository.saveMessage(editedMsg)
                    }
                } catch (e: Exception) {
                    _errorMessage.value = e.message ?: "Mesaj düzenlenemedi."
                }
            }
        }
    }

    fun deleteMessage(msgId: String) {
        viewModelScope.launch {
            repository.deleteMessage(msgId)
        }
    }

    fun rewindToMessage(botId: String, msgId: String) {
        viewModelScope.launch {
            try {
                repository.rewindToMessage(botId, msgId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Geri alma başarısız oldu."
            }
        }
    }

    fun resetChat(botId: String, fullReset: Boolean = false) {
        viewModelScope.launch {
            val currentBot = repository.getBot(botId) ?: return@launch
            repository.resetMessagesForBot(botId)
            if (fullReset) {
                repository.resetBotMemoryAndEmotion(botId)
            }

            val updatedBot = repository.getBot(botId) ?: currentBot
            val opening = MessageEntity(
                id = UUID.randomUUID().toString(),
                botId = botId,
                role = "assistant",
                text = repository.resolveOpeningText(updatedBot),
                timestamp = System.currentTimeMillis()
            )
            repository.saveMessage(opening)
        }
    }

    fun deleteBot(botId: String) {
        viewModelScope.launch {
            repository.deleteBot(botId)
            if (_activeBotId.value == botId) {
                openMenu()
            }
        }
    }

    fun toggleBotProactive(botId: String) {
        viewModelScope.launch {
            val bot = repository.getBot(botId) ?: return@launch
            repository.saveBot(bot.copy(proactiveEnabled = !bot.proactiveEnabled))
        }
    }

    // ── Milestone System ────────────────────────────────────────────────
    private val _milestoneEvent = MutableSharedFlow<com.example.data.local.MilestoneEntity>(extraBufferCapacity = 1)
    val milestoneEvent: SharedFlow<com.example.data.local.MilestoneEntity> = _milestoneEvent.asSharedFlow()

    private suspend fun checkMilestones(botId: String, affection: Int, trust: Int) {
        try {
            val newMilestone = repository.checkAndSaveMilestone(botId, affection, trust)
            if (newMilestone != null) _milestoneEvent.tryEmit(newMilestone)
        } catch (_: Exception) {}
    }

    // ── System Prompt Debug ─────────────────────────────────────────────
    private val _lastSystemPrompt = MutableStateFlow<String?>(null)
    val lastSystemPrompt: StateFlow<String?> = _lastSystemPrompt.asStateFlow()

    fun captureSystemPrompt(bot: com.example.data.local.BotEntity, msgs: List<com.example.data.local.MessageEntity>) {
        viewModelScope.launch {
            try {
                val prompt = repository.buildSystemPrompt(bot, userSettings.value ?: com.example.data.local.UserSettingsEntity())
                _lastSystemPrompt.value = prompt
            } catch (_: Exception) {}
        }
    }

    // ── Message Search ──────────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<com.example.data.local.MessageEntity>>(emptyList())
    val searchResults: StateFlow<List<com.example.data.local.MessageEntity>> = _searchResults.asStateFlow()

    fun setSearchQuery(query: String?) {
        _searchQuery.value = query
        if (query.isNullOrBlank()) {
            _searchResults.value = emptyList()
            return
        }
        val botId = _activeBotId.value ?: return
        viewModelScope.launch {
            _searchResults.value = repository.searchMessages(botId, query.trim())
        }
    }

    fun clearSearch() {
        _searchQuery.value = null
        _searchResults.value = emptyList()
    }

    // ── Message Star ────────────────────────────────────────────────────
    fun toggleMessageStar(msgId: String) {
        viewModelScope.launch {
            repository.toggleMessageStar(msgId)
            val botId = _activeBotId.value
            if (botId != null) {
                _starredMessages.value = repository.getStarredMessages(botId)
            }
        }
    }

    fun setMessageReaction(msgId: String, reaction: String) {
        viewModelScope.launch {
            repository.setMessageReaction(msgId, reaction)
        }
    }

    private val _starredMessages = MutableStateFlow<List<com.example.data.local.MessageEntity>>(emptyList())
    val starredMessages: StateFlow<List<com.example.data.local.MessageEntity>> = _starredMessages.asStateFlow()

    fun loadStarredMessages() {
        val botId = _activeBotId.value ?: return
        viewModelScope.launch {
            _starredMessages.value = repository.getStarredMessages(botId)
        }
    }

    fun cloneBot(sourceBotId: String, onComplete: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                _isSending.value = true
                val cloned = repository.cloneBot(sourceBotId)
                if (cloned != null) {
                    _activeBotId.value = cloned.id
                    EmochiRepository.activeBotId = cloned.id
                    _uiState.value = UiState.Chat(cloned.id)
                    onComplete?.invoke(cloned.id)
                } else {
                    _errorMessage.value = "Bot kopyalanamadı: Kaynak bot bulunamadı."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Bot kopyalanırken hata oluştu: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    suspend fun getContentFilterCountForBot(botId: String): Int {
        return repository.getContentFilterCountForBot(botId)
    }

    fun retryWithSoftenedPrompt(botId: String) {
        if (_isSending.value) return
        _isSending.value = true

        viewModelScope.launch {
            withSendLock {
                val currentBot = activeBot.value ?: repository.getBot(botId) ?: return@withSendLock
                try {
                    _errorMessage.value = null
                    val msgs = repository.getMessageListForBot(botId)
                    if (msgs.isEmpty()) return@withSendLock

                    if (msgs.last().role == "assistant" && msgs.last().status == "failed") {
                        repository.deleteMessage(msgs.last().id)
                    }

                    val currentMsgs = repository.getMessageListForBot(botId)
                    if (currentMsgs.isEmpty() || currentMsgs.last().role != "user") return@withSendLock

                    val replyResult = repository.generateAiReply(
                        bot = currentBot,
                        messages = currentMsgs,
                        literaryFramingMode = true
                    )
                    val replyText = replyResult.replyText
                    if (replyText.isBlank()) {
                        throw IllegalStateException("Model boş yanıt döndürdü.")
                    }

                    val now = System.currentTimeMillis()
                    val aiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = botId,
                        role = "assistant",
                        text = replyText,
                        timestamp = now,
                        status = "success",
                        provider = replyResult.usedProvider
                    )
                    repository.saveMessage(aiMsg)
                    repository.saveBot(currentBot.copy(updatedAt = now))
                } catch (e: Exception) {
                    val friendlyMsg = extractUserFriendlyErrorMessage(e)
                    _errorMessage.value = friendlyMsg
                }
            }
        }
    }

    fun continueStoryScene() {
        val botId = _activeBotId.value ?: return
        if (_isSending.value) return
        _isSending.value = true

        viewModelScope.launch {
            withSendLock {
                val currentBot = activeBot.value ?: _immediateBotCache.value ?: repository.getBot(botId) ?: return@withSendLock
                try {
                    _errorMessage.value = null
                    val currentMsgs = repository.getMessageListForBot(botId)
                    val continuationPromptMsg = MessageEntity(
                        id = "continue_scene_prompt_${System.currentTimeMillis()}",
                        botId = botId,
                        role = "user",
                        text = "[SAHNEYİ DEVAM ETTİR]: Kullanıcının yeni bir mesaj yazmasına gerek kalmadan, mevcut roman sahnesini karakterin yeni bir eylemi, iç sesi (*...*) ve diyaloğuyla (\"...\") 2 ila 4 paragraf halinde doğrudan devam ettir.",
                        timestamp = System.currentTimeMillis()
                    )
                    val replyResult = repository.generateAiReply(currentBot, currentMsgs + continuationPromptMsg)
                    val replyText = replyResult.replyText
                    if (replyText.isBlank()) {
                        throw IllegalStateException("Model boş yanıt döndürdü.")
                    }
                    if (replyResult.systemPromptSnapshot.isNotBlank()) {
                        _lastSystemPrompt.value = replyResult.systemPromptSnapshot
                    }

                    val now = System.currentTimeMillis()
                    val aiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = botId,
                        role = "assistant",
                        text = replyText,
                        timestamp = now,
                        provider = replyResult.usedProvider
                    )
                    repository.saveMessage(aiMsg)
                    startTypewriter(aiMsg.id, replyText)
                    repository.saveBot(currentBot.copy(updatedAt = now))
                } catch (e: Exception) {
                    val friendlyMsg = extractUserFriendlyErrorMessage(e)
                    _errorMessage.value = friendlyMsg
                }
            }
        }
    }

    fun createPresetBot(preset: BotEntity) {
        stopSpeaking()
        _errorMessage.value = null

        val targetId = if (
            preset.id == "preset_aiden_mcu_cosmic" ||
            (preset.id.isNotBlank() && !preset.id.startsWith("preset_"))
        ) {
            preset.id
        } else {
            UUID.randomUUID().toString()
        }
        val now = System.currentTimeMillis()
        val botToSave = preset.copy(
            id = targetId,
            isPublic = false,
            isTemplate = false,
            totalPromptTokens = 0L,
            totalCandidateTokens = 0L,
            needsSummarization = false,
            storyNotes = "",
            memoryNotes = "",
            emotionState = """{"mood":"nötr","intensity":5,"affection":50,"trust":50,"tension":10}""",
            previousEmotionState = """{"mood":"nötr","intensity":5,"affection":50,"trust":50,"tension":10}""",
            worldAtmosphere = """{"mood":"sakin","intensity":5,"currentEvent":""}""",
            updatedAt = now
        )
        val initialOpeningMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            botId = botToSave.id,
            role = "assistant",
            text = repository.resolveOpeningText(botToSave),
            timestamp = now
        )

        // Synchronously set active bot + opening message cache and transition straight to ChatScreen!
        _immediateBotCache.value = botToSave
        _immediateOpeningMessageCache.value = listOf(initialOpeningMsg)
        _activeBotId.value = botToSave.id
        EmochiRepository.activeBotId = botToSave.id
        _uiState.value = UiState.Chat(botToSave.id)

        viewModelScope.launch {
            repository.saveBot(botToSave)
            val existingMsgs = repository.getMessageListForBot(botToSave.id)
            if (existingMsgs.isEmpty()) {
                repository.saveMessage(initialOpeningMsg)
            }
        }
    }

    fun updateBotProfile(updatedBot: BotEntity) {
        viewModelScope.launch {
            val oldBot = repository.getBot(updatedBot.id)
            if (oldBot != null) {
                val oldChars = repository.parseKeyCharacters(oldBot.keyCharactersJson)
                val newChars = repository.parseKeyCharacters(updatedBot.keyCharactersJson)
                val newNames = newChars.map { repository.cleanCharacterNameCandidate(it.name).lowercase() }.toSet()

                for (oldChar in oldChars) {
                    val oldClean = repository.cleanCharacterNameCandidate(oldChar.name)
                    if (oldClean.isNotBlank() && !newNames.contains(oldClean.lowercase())) {
                        repository.deleteCharacterAndBlacklist(updatedBot.id, oldChar.name)
                        repository.deleteCharacterAndBlacklist(updatedBot.id, oldClean)
                    }
                }
            }
            repository.saveBot(updatedBot)
        }
    }

    fun updateCharacterEmotion(botId: String, characterName: String, newMood: String, affection: Int, trust: Int, tension: Int, intensity: Int = 5) {
        viewModelScope.launch {
            val existing = repository.getEmotionForCharacter(botId, characterName)
            val updatedStateJson = JSONObject().apply {
                put("mood", newMood)
                put("intensity", intensity)
                put("affection", affection)
                put("trust", trust)
                put("tension", tension)
            }.toString()

            val entity = existing?.copy(emotionState = updatedStateJson)
                ?: CharacterEmotionEntity(botId = botId, characterName = characterName, emotionState = updatedStateJson)
            repository.insertOrUpdateCharacterEmotion(entity)
            checkMilestones(botId, affection, trust)
        }
    }

    fun updateCharacterEmotionState(botId: String, characterName: String, emotionStateJson: String) {
        viewModelScope.launch {
            val existing = repository.getEmotionForCharacter(botId, characterName)
            val entity = existing?.copy(emotionState = emotionStateJson)
                ?: CharacterEmotionEntity(botId = botId, characterName = characterName, emotionState = emotionStateJson)
            repository.insertOrUpdateCharacterEmotion(entity)
        }
    }

    fun updateSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            val previous = try { repository.getOrCreateSettings() } catch (_: Exception) { null }
            val sanitized = settings.copy(
                customApiKey = settings.customApiKey.trim(),
                groqApiKey = settings.groqApiKey.trim(),
                claudeApiKey = settings.claudeApiKey.trim(),
                openaiApiKey = settings.openaiApiKey.trim(),
                openRouterApiKey = settings.openRouterApiKey.trim(),
                openRouterModel = settings.openRouterModel.trim(),
                nvidiaApiKey = settings.nvidiaApiKey.trim(),
                nvidiaModel = settings.nvidiaModel.trim(),
                mistralApiKey = settings.mistralApiKey.trim(),
                mistralModel = settings.mistralModel.trim(),
                backupApiKey = settings.backupApiKey.trim(),
                ttsSpeed = settings.ttsSpeed.coerceIn(0.5f, 2.0f),
                ttsPitch = settings.ttsPitch.coerceIn(0.5f, 2.0f)
            )
            repository.updateUserSettings(sanitized)
            if (previous != null) {
                if (previous.customApiKey != sanitized.customApiKey || previous.geminiModel != sanitized.geminiModel) {
                    com.example.data.managers.ProviderHealthTracker.resetHealth("gemini")
                }
                if (previous.groqApiKey != sanitized.groqApiKey || previous.groqModel != sanitized.groqModel) {
                    com.example.data.managers.ProviderHealthTracker.resetHealth("groq")
                }
                if (previous.openaiApiKey != sanitized.openaiApiKey || previous.openaiModel != sanitized.openaiModel) {
                    com.example.data.managers.ProviderHealthTracker.resetHealth("openai")
                }
                if (previous.claudeApiKey != sanitized.claudeApiKey || previous.claudeModel != sanitized.claudeModel) {
                    com.example.data.managers.ProviderHealthTracker.resetHealth("claude")
                }
                if (previous.openRouterApiKey != sanitized.openRouterApiKey || previous.openRouterModel != sanitized.openRouterModel) {
                    com.example.data.managers.ProviderHealthTracker.resetHealth("openrouter")
                }
                if (previous.nvidiaApiKey != sanitized.nvidiaApiKey || previous.nvidiaModel != sanitized.nvidiaModel) {
                    com.example.data.managers.ProviderHealthTracker.resetHealth("nvidia")
                }
                if (previous.mistralApiKey != sanitized.mistralApiKey || previous.mistralModel != sanitized.mistralModel) {
                    com.example.data.managers.ProviderHealthTracker.resetHealth("mistral")
                }
            }
        }
    }

    suspend fun testOpenRouterConnection(apiKey: String, modelName: String = "deepseek/deepseek-chat"): EmochiRepository.ProviderTestResult {
        return repository.testOpenRouterConnection(apiKey, modelName)
    }

    suspend fun testNvidiaConnection(apiKey: String, modelName: String = ""): EmochiRepository.ProviderTestResult {
        return repository.testNvidiaConnection(apiKey, modelName)
    }

    suspend fun testMistralConnection(apiKey: String, modelName: String = "mistral-large-latest"): EmochiRepository.ProviderTestResult {
        return repository.testMistralConnection(apiKey, modelName)
    }

    suspend fun exportBackupJson(includeApiKeys: Boolean = false): String {
        return repository.exportDataSnapshot(includeApiKeys = includeApiKeys)
    }

    suspend fun importBackupJson(
        jsonStr: String,
        overwriteExisting: Boolean = false
    ): com.example.data.repository.BackupImportSummary {
        return repository.importDataSnapshot(jsonStr = jsonStr, overwriteExisting = overwriteExisting)
    }

    suspend fun generateOpeningForWizard(
        botDraft: BotEntity,
        forceRegenerate: Boolean = true,
        toneHint: String = ""
    ): String {
        return repository.generateOpeningMessage(
            bot = botDraft,
            forceRegenerate = forceRegenerate,
            toneHint = toneHint
        )
    }

    fun deleteCharacter(botId: String, characterName: String) {
        viewModelScope.launch {
            repository.deleteCharacterAndBlacklist(botId, characterName)
        }
    }

    /** 3.3: Otomatik eklenen bir cast member'ı sil. blacklist=true ise bir daha önerilmez. */
    fun deleteAutoAddedCastMember(botId: String, castMemberId: Long, characterName: String, blacklist: Boolean) {
        viewModelScope.launch {
            if (blacklist) {
                repository.blacklistCastMember(botId, castMemberId)
            } else {
                repository.dismissPendingCastMember(botId, castMemberId)
                // Non-blacklist: sadece sil (CastDetectorEngine blacklist'e ekleme)
                repository.deleteAutoAddedCastMemberById(botId, castMemberId)
            }
        }
    }

    suspend fun getCastMembersForBot(botId: String) = repository.getCastMembersForBot(botId)

    fun getActiveFactsFlow(botId: String) = repository.getActiveFactsFlow(botId)
    fun getActiveEventsFlow(botId: String) = repository.getActiveEventsFlow(botId)

    fun loadArchivedMemories(botId: String) {
        viewModelScope.launch {
            _archivedMemories.value = repository.getArchivedMemories(botId)
        }
    }

    fun toggleMemoryFactLock(fact: com.example.data.local.MemoryFactEntity) {
        viewModelScope.launch {
            repository.toggleMemoryFactLock(fact)
        }
    }

    fun restoreArchivedMemory(botId: String, archive: com.example.data.local.ArchivedMemoryEntity) {
        viewModelScope.launch {
            val restored = repository.restoreArchivedMemory(botId, archive)
            _archivedMemories.value = repository.getArchivedMemories(botId)
            if (restored) {
                val notice = MemoryCommitNotice(
                    title = "Kayıt Arşivden Geri Yüklendi",
                    subtitle = if (archive.memoryType.equals("event", ignoreCase = true)) {
                        "Yaşanan Anı tekrar aktif belleğe alındı"
                    } else {
                        "Neural Vault bilgisi tekrar aktif belleğe alındı"
                    },
                    isLivingMemory = archive.memoryType.equals("event", ignoreCase = true)
                )
                _memoryCommitNotice.value = notice
                kotlinx.coroutines.delay(3500L)
                if (_memoryCommitNotice.value?.id == notice.id) {
                    _memoryCommitNotice.value = null
                }
            }
        }
    }

    fun deleteMemoryFact(id: Long) {
        viewModelScope.launch {
            repository.deleteMemoryFact(id)
            _activeBotId.value?.let { _archivedMemories.value = repository.getArchivedMemories(it) }
        }
    }

    fun deleteMemoryEvent(id: Long) {
        viewModelScope.launch {
            repository.deleteMemoryEvent(id)
            _activeBotId.value?.let { _archivedMemories.value = repository.getArchivedMemories(it) }
        }
    }

    fun updateMemoryFact(fact: com.example.data.local.MemoryFactEntity) {
        viewModelScope.launch {
            repository.updateMemoryFact(fact)
        }
    }

    fun updateMemoryEvent(event: com.example.data.local.MemoryEventEntity) {
        viewModelScope.launch {
            repository.updateMemoryEvent(event)
        }
    }

    fun clearAllAutoMemories(botId: String) {
        viewModelScope.launch {
            repository.deleteAllAutoMemories(botId)
            _archivedMemories.value = repository.getArchivedMemories(botId)
        }
    }

    // --- Pending Cast Member Confirmation (3.2, 3.3) ---

    private val _pendingCastMember = MutableStateFlow<CastMemberEntity?>(null)
    val pendingCastMember: StateFlow<CastMemberEntity?> = _pendingCastMember.asStateFlow()

    // 3.2: mesaj sayacı — kaç mesaj geçtikten sonra banner otomatik kapanacak
    private var pendingCastBannerShownAtMessageCount = -1
    private val PENDING_CAST_BANNER_TIMEOUT_MESSAGES = 5

    fun checkForPendingCastMembers(botId: String, currentMessageCount: Int = -1) {
        viewModelScope.launch {
            val pending = repository.getFirstPendingCastMember(botId)
            if (pending != null) {
                // Zaman aşımı kontrolü: 5 mesaj geçtiyse ve kullanıcı cevap vermediyse sessizce kapat (= hayır)
                if (pendingCastBannerShownAtMessageCount >= 0 &&
                    currentMessageCount >= 0 &&
                    (currentMessageCount - pendingCastBannerShownAtMessageCount) >= PENDING_CAST_BANNER_TIMEOUT_MESSAGES) {
                    // Timeout: dismiss without blacklisting (sadece bu seferlik hayır)
                    repository.dismissPendingCastMember(botId, pending.id)
                    _pendingCastMember.value = null
                    pendingCastBannerShownAtMessageCount = -1
                    android.util.Log.d("CastBanner", "Auto-dismissed pending cast '${pending.name}' after $PENDING_CAST_BANNER_TIMEOUT_MESSAGES messages timeout")
                    return@launch
                }
                if (_pendingCastMember.value?.id != pending.id) {
                    pendingCastBannerShownAtMessageCount = if (currentMessageCount >= 0) currentMessageCount else pendingCastBannerShownAtMessageCount
                }
                _pendingCastMember.value = pending
            } else {
                _pendingCastMember.value = null
                pendingCastBannerShownAtMessageCount = -1
            }
        }
    }

    fun confirmPendingCastMember(botId: String, castMemberId: Long) {
        viewModelScope.launch {
            repository.confirmPendingCastMember(botId, castMemberId)
            _pendingCastMember.value = null
            pendingCastBannerShownAtMessageCount = -1
        }
    }

    /** "Hayır, önemli değil": sadece bu seferlik dismiss, blacklist'e EKLEME (gelecekte tekrar sorulabilir). */
    fun dismissCastMemberTemporarily(botId: String, castMemberId: Long) {
        viewModelScope.launch {
            repository.dismissPendingCastMember(botId, castMemberId)
            _pendingCastMember.value = null
            pendingCastBannerShownAtMessageCount = -1
        }
    }

    /** "Bir daha sorma": sets isBlacklisted=true so this name is never suggested again. */
    fun blacklistAndDismissCastMember(botId: String, castMemberId: Long) {
        viewModelScope.launch {
            repository.blacklistCastMember(botId, castMemberId)
            _pendingCastMember.value = null
            pendingCastBannerShownAtMessageCount = -1
        }
    }

    suspend fun getRecentEmotionHistory(botId: String, limit: Int = 30) =
        repository.getRecentEmotionHistory(botId, limit)

    suspend fun getRecentCharacterEmotionHistory(botId: String, characterName: String, limit: Int = 20) =
        repository.getRecentCharacterEmotionHistory(botId, characterName, limit)

    private fun extractUserFriendlyErrorMessage(e: Exception): String {
        val rawMsg = e.message ?: ""
        return when {
            rawMsg.contains("içerik kısıtlaması", ignoreCase = true) ||
            rawMsg.contains("içerik filtresi", ignoreCase = true) ||
            rawMsg.contains("güvenlik filtresine", ignoreCase = true) ||
            rawMsg.contains("politikası", ignoreCase = true) ||
            rawMsg.contains("refusal", ignoreCase = true) ||
            rawMsg.contains("content_filter", ignoreCase = true) -> {
                rawMsg.ifBlank { "Seçili AI sağlayıcısı içerik kısıtlaması politikası gereği bu yanıtı süzdü. Lütfen Ayarlar -> AI Model Ayarları menüsünden farklı bir model (ör. Groq veya Gemini) seçin." }
            }
            rawMsg.contains("429", ignoreCase = true) || rawMsg.contains("kotası", ignoreCase = true) || rawMsg.contains("rate limit", ignoreCase = true) -> {
                "API kullanım kotası doldu (429 Rate Limit). Lütfen Ayarlar -> AI Model Ayarları menüsünden API Key'inizi ekleyin veya modelinizi değiştirin."
            }
            else -> if (rawMsg.isNotBlank()) rawMsg else "Yanıt alınamadı. Lütfen ağ bağlantınızı veya API ayarlarınızı kontrol edin."
        }
    }
}
