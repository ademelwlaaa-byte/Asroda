package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Merkezi özellik toggle ve hafif kalıcı tercih sistemi.
 * Her ayar SharedPreferences'a kaydedilir (Room şemasını değiştirmeden);
 * UI katmanı StateFlow'ları dinleyerek anlık güncelleme alır.
 */
object AppFeatureFlags {

    private const val PREF_NAME = "app_feature_flags"

    // ─── Boolean Bayraklar ────────────────────────────────────────────────────
    private const val KEY_SSE_STREAMING          = "sse_streaming"
    const val KEY_SSE_STREAMING_ENABLED          = KEY_SSE_STREAMING
    private const val KEY_MEMORY_TIMELINE        = "memory_timeline"
    private const val KEY_ROUTINE_NOTIF          = "routine_notifications"
    private const val KEY_SCENE_IMAGE            = "scene_image_gen"
    private const val KEY_DYNAMIC_DIFFICULTY     = "dynamic_difficulty"
    private const val KEY_MULTI_BOT_SCENE        = "multi_bot_scene"
    private const val KEY_TOKEN_COUNTER          = "token_counter"
    const val KEY_REFUSAL_AUTO_FALLBACK          = "refusal_auto_fallback"
    const val KEY_STRICT_API_TIMEOUT             = "strict_api_timeout"
    const val KEY_TTS_READ_ACTIONS               = "tts_read_actions"
    const val KEY_TTS_AUTO_READ_REPLIES          = "tts_auto_read_replies"
    const val KEY_TTS_EMOTIONAL_PROSODY          = "tts_emotional_prosody"
    const val KEY_REDUCE_MOTION                  = "reduce_motion"
    const val KEY_MICRO_AI_CAST_SENTIMENT        = "micro_ai_cast_sentiment"

    // ─── String / Set Tercihleri ──────────────────────────────────────────────
    const val KEY_TTS_ENGINE_MODE                = "tts_engine_mode"
    const val KEY_TTS_NEURAL_VOICE               = "tts_neural_voice"
    private const val KEY_TEMPLATE_ORIGIN_BOTS   = "template_origin_bot_ids"
    private const val KEY_AI_UTIL_MODEL_PREFIX   = "ai_utility_model_"
    private const val KEY_AI_UTIL_SOURCE_PREFIX  = "ai_utility_source_"

    const val TTS_ENGINE_NEURAL = "neural"
    const val TTS_ENGINE_DEVICE = "device"
    const val DEFAULT_NEURAL_VOICE = "tr-TR-EmelNeural"

    // Varsayılanlar: kararlı özellikler açık, deneysel olanlar kapalı
    private val DEFAULTS = mapOf(
        KEY_SSE_STREAMING         to true,
        KEY_MEMORY_TIMELINE       to true,
        KEY_ROUTINE_NOTIF         to true,
        KEY_SCENE_IMAGE           to false,
        KEY_DYNAMIC_DIFFICULTY    to false,
        KEY_MULTI_BOT_SCENE       to false,
        KEY_TOKEN_COUNTER         to true,
        KEY_REFUSAL_AUTO_FALLBACK to true,
        KEY_STRICT_API_TIMEOUT    to true,
        KEY_TTS_READ_ACTIONS          to false,
        KEY_TTS_AUTO_READ_REPLIES     to false,
        KEY_TTS_EMOTIONAL_PROSODY     to true,
        KEY_REDUCE_MOTION             to false,
        KEY_MICRO_AI_CAST_SENTIMENT   to true,
    )

    private var prefs: SharedPreferences? = null
    @Volatile
    private var appContext: Context? = null
    // In-memory override backing stores so unit tests and pre-init calls behave deterministically
    private val memoryOverrides = ConcurrentHashMap<String, Boolean>()
    private val memoryStringOverrides = ConcurrentHashMap<String, String>()
    private val templateOriginBotIdsMem: MutableSet<String> = ConcurrentHashMap.newKeySet()

    // StateFlow'lar — Compose katmanı bunları collect eder
    private val _sseStreaming        = MutableStateFlow(true)
    private val _memoryTimeline      = MutableStateFlow(true)
    private val _routineNotif        = MutableStateFlow(true)
    private val _sceneImage          = MutableStateFlow(false)
    private val _dynamicDifficulty   = MutableStateFlow(false)
    private val _multiBotScene       = MutableStateFlow(false)
    private val _tokenCounter        = MutableStateFlow(true)
    private val _refusalAutoFallback = MutableStateFlow(true)
    private val _strictApiTimeout    = MutableStateFlow(true)
    private val _ttsReadActions       = MutableStateFlow(false)
    private val _ttsAutoReadReplies   = MutableStateFlow(false)
    private val _ttsEmotionalProsody  = MutableStateFlow(true)
    private val _reduceMotion         = MutableStateFlow(false)
    private val _microAiCastSentiment = MutableStateFlow(true)
    private val _ttsEngineMode        = MutableStateFlow(TTS_ENGINE_NEURAL)
    private val _ttsNeuralVoice       = MutableStateFlow(DEFAULT_NEURAL_VOICE)
    private val _aiUtilityModels      = MutableStateFlow<Map<String, String>>(emptyMap())

    val sseStreaming:         StateFlow<Boolean> = _sseStreaming.asStateFlow()
    val memoryTimeline:       StateFlow<Boolean> = _memoryTimeline.asStateFlow()
    val routineNotif:         StateFlow<Boolean> = _routineNotif.asStateFlow()
    val sceneImage:           StateFlow<Boolean> = _sceneImage.asStateFlow()
    val dynamicDifficulty:    StateFlow<Boolean> = _dynamicDifficulty.asStateFlow()
    val multiBotScene:        StateFlow<Boolean> = _multiBotScene.asStateFlow()
    val tokenCounter:         StateFlow<Boolean> = _tokenCounter.asStateFlow()
    val refusalAutoFallback:  StateFlow<Boolean> = _refusalAutoFallback.asStateFlow()
    val strictApiTimeout:     StateFlow<Boolean> = _strictApiTimeout.asStateFlow()
    val ttsReadActions:       StateFlow<Boolean> = _ttsReadActions.asStateFlow()
    val ttsAutoReadReplies:   StateFlow<Boolean> = _ttsAutoReadReplies.asStateFlow()
    val ttsEmotionalProsody:  StateFlow<Boolean> = _ttsEmotionalProsody.asStateFlow()
    val reduceMotion:         StateFlow<Boolean> = _reduceMotion.asStateFlow()
    val microAiCastSentiment: StateFlow<Boolean> = _microAiCastSentiment.asStateFlow()
    val ttsEngineMode:        StateFlow<String>  = _ttsEngineMode.asStateFlow()
    val ttsNeuralVoice:       StateFlow<String>  = _ttsNeuralVoice.asStateFlow()
    val aiUtilityModels:      StateFlow<Map<String, String>> = _aiUtilityModels.asStateFlow()

    fun init(context: Context) {
        val ctx = context.applicationContext ?: context
        appContext = ctx
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        memoryOverrides.clear()
        memoryStringOverrides.clear()
        templateOriginBotIdsMem.clear()
        syncFromPrefs()
    }

    // ─── Okuma (anlık, StateFlow bağımsız) ───────────────────────────────────
    val isSseStreamingEnabled:        Boolean get() = get(KEY_SSE_STREAMING)
    val isMemoryTimelineEnabled:      Boolean get() = get(KEY_MEMORY_TIMELINE)
    val isRoutineNotifEnabled:        Boolean get() = get(KEY_ROUTINE_NOTIF)
    val isSceneImageEnabled:          Boolean get() = get(KEY_SCENE_IMAGE)
    val isDynamicDifficultyEnabled:   Boolean get() = get(KEY_DYNAMIC_DIFFICULTY)
    val isMultiBotSceneEnabled:       Boolean get() = get(KEY_MULTI_BOT_SCENE)
    val isTokenCounterEnabled:        Boolean get() = get(KEY_TOKEN_COUNTER)
    val isRefusalAutoFallbackEnabled: Boolean get() = get(KEY_REFUSAL_AUTO_FALLBACK)
    val isStrictApiTimeoutEnabled:    Boolean get() = get(KEY_STRICT_API_TIMEOUT)
    val isTtsReadActionsEnabled:       Boolean get() = get(KEY_TTS_READ_ACTIONS)
    val isTtsAutoReadRepliesEnabled:   Boolean get() = get(KEY_TTS_AUTO_READ_REPLIES)
    val isTtsEmotionalProsodyEnabled:  Boolean get() = get(KEY_TTS_EMOTIONAL_PROSODY)
    val isReduceMotionEnabled:         Boolean get() = get(KEY_REDUCE_MOTION)
    val isMicroAiCastSentimentEnabled: Boolean get() = get(KEY_MICRO_AI_CAST_SENTIMENT)

    fun getTtsEngineMode(): String =
        getString(KEY_TTS_ENGINE_MODE, TTS_ENGINE_NEURAL).let {
            if (it == TTS_ENGINE_DEVICE) TTS_ENGINE_DEVICE else TTS_ENGINE_NEURAL
        }

    fun getTtsNeuralVoice(): String =
        getString(KEY_TTS_NEURAL_VOICE, DEFAULT_NEURAL_VOICE).ifBlank { DEFAULT_NEURAL_VOICE }

    // ─── Yazma ───────────────────────────────────────────────────────────────
    fun setSseStreaming(v: Boolean)         { set(KEY_SSE_STREAMING, v);           _sseStreaming.value = v }
    fun setMemoryTimeline(v: Boolean)       { set(KEY_MEMORY_TIMELINE, v);         _memoryTimeline.value = v }
    fun setRoutineNotif(v: Boolean, context: Context? = null) {
        set(KEY_ROUTINE_NOTIF, v)
        _routineNotif.value = v
        val ctx = context?.applicationContext ?: appContext
        if (ctx != null) {
            runCatching {
                if (v) {
                    com.example.work.RoutineNotificationWorker.schedule(ctx)
                } else {
                    com.example.work.RoutineNotificationWorker.cancel(ctx)
                }
            }
        }
    }
    fun setSceneImage(v: Boolean)           { set(KEY_SCENE_IMAGE, v);             _sceneImage.value = v }
    fun setDynamicDifficulty(v: Boolean)    { set(KEY_DYNAMIC_DIFFICULTY, v);      _dynamicDifficulty.value = v }
    fun setMultiBotScene(v: Boolean)        { set(KEY_MULTI_BOT_SCENE, v);         _multiBotScene.value = v }
    fun setTokenCounter(v: Boolean)         { set(KEY_TOKEN_COUNTER, v);           _tokenCounter.value = v }
    fun setRefusalAutoFallback(v: Boolean)  { set(KEY_REFUSAL_AUTO_FALLBACK, v);   _refusalAutoFallback.value = v }
    fun setStrictApiTimeout(v: Boolean)     { set(KEY_STRICT_API_TIMEOUT, v);      _strictApiTimeout.value = v }
    fun setTtsReadActions(v: Boolean)       { set(KEY_TTS_READ_ACTIONS, v);        _ttsReadActions.value = v }
    fun setTtsAutoReadReplies(v: Boolean)   { set(KEY_TTS_AUTO_READ_REPLIES, v);   _ttsAutoReadReplies.value = v }
    fun setTtsEmotionalProsody(v: Boolean)  { set(KEY_TTS_EMOTIONAL_PROSODY, v);   _ttsEmotionalProsody.value = v }
    fun setReduceMotion(v: Boolean)         { set(KEY_REDUCE_MOTION, v);           _reduceMotion.value = v }
    fun setMicroAiCastSentiment(v: Boolean) { set(KEY_MICRO_AI_CAST_SENTIMENT, v); _microAiCastSentiment.value = v }

    fun setTtsEngineMode(mode: String) {
        val normalized = if (mode == TTS_ENGINE_DEVICE) TTS_ENGINE_DEVICE else TTS_ENGINE_NEURAL
        setString(KEY_TTS_ENGINE_MODE, normalized)
        _ttsEngineMode.value = normalized
    }

    fun setTtsNeuralVoice(voiceId: String) {
        val clean = voiceId.trim().ifBlank { DEFAULT_NEURAL_VOICE }
        setString(KEY_TTS_NEURAL_VOICE, clean)
        _ttsNeuralVoice.value = clean
    }

    // ─── Tüm API Sağlayıcıları İçin AI Seçimli Küçük Destek Modeli ───────────
    fun getAiUtilityModel(providerKey: String): String {
        val norm = providerKey.trim().lowercase(Locale.ROOT)
        if (norm.isBlank()) return ""
        return getString("$KEY_AI_UTIL_MODEL_PREFIX$norm", "").trim()
    }

    fun getAiUtilityModelSource(providerKey: String): String {
        val norm = providerKey.trim().lowercase(Locale.ROOT)
        if (norm.isBlank()) return "default"
        return getString("$KEY_AI_UTIL_SOURCE_PREFIX$norm", "default").ifBlank { "default" }
    }

    fun setAiUtilityModel(providerKey: String, modelId: String, source: String = "ai") {
        val norm = providerKey.trim().lowercase(Locale.ROOT)
        val cleanModel = modelId.trim()
        if (norm.isBlank()) return
        setString("$KEY_AI_UTIL_MODEL_PREFIX$norm", cleanModel)
        setString("$KEY_AI_UTIL_SOURCE_PREFIX$norm", source)
        val updated = _aiUtilityModels.value.toMutableMap()
        if (cleanModel.isBlank()) updated.remove(norm) else updated[norm] = cleanModel
        _aiUtilityModels.value = updated
    }

    fun clearAiUtilityModel(providerKey: String) {
        val norm = providerKey.trim().lowercase(Locale.ROOT)
        if (norm.isBlank()) return
        val sp = prefs
        if (sp != null) {
            sp.edit()
                .remove("$KEY_AI_UTIL_MODEL_PREFIX$norm")
                .remove("$KEY_AI_UTIL_SOURCE_PREFIX$norm")
                .apply()
        }
        memoryStringOverrides.remove("$KEY_AI_UTIL_MODEL_PREFIX$norm")
        memoryStringOverrides.remove("$KEY_AI_UTIL_SOURCE_PREFIX$norm")
        val updated = _aiUtilityModels.value.toMutableMap()
        updated.remove(norm)
        _aiUtilityModels.value = updated
    }

    fun clearAiUtilityModels() {
        val sp = prefs
        if (sp != null) {
            val editor = sp.edit()
            sp.all.keys.filter {
                it.startsWith(KEY_AI_UTIL_MODEL_PREFIX) || it.startsWith(KEY_AI_UTIL_SOURCE_PREFIX)
            }.forEach { editor.remove(it) }
            editor.apply()
        }
        memoryStringOverrides.keys.removeIf {
            it.startsWith(KEY_AI_UTIL_MODEL_PREFIX) || it.startsWith(KEY_AI_UTIL_SOURCE_PREFIX)
        }
        _aiUtilityModels.value = emptyMap()
    }

    // ─── Şablon / Başlangıç Kökenli Bot Kilidi (Herkese Açık Olamaz) ─────────
    fun markTemplateOriginBot(botId: String) {
        val clean = botId.trim()
        if (clean.isBlank()) return
        templateOriginBotIdsMem.add(clean)
        val sp = prefs ?: return
        val existing = sp.getStringSet(KEY_TEMPLATE_ORIGIN_BOTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (existing.add(clean)) {
            sp.edit().putStringSet(KEY_TEMPLATE_ORIGIN_BOTS, existing).apply()
        }
    }

    fun isTemplateOriginBot(botId: String): Boolean {
        val clean = botId.trim()
        if (clean.isBlank()) return false
        if (clean in templateOriginBotIdsMem) return true
        val sp = prefs ?: return false
        return sp.getStringSet(KEY_TEMPLATE_ORIGIN_BOTS, emptySet())?.contains(clean) == true
    }

    // ─── Meta — tüm toggle listesi (UI için) ─────────────────────────────────
    data class ToggleSpec(
        val key: String,
        val label: String,
        val description: String,
        val isExperimental: Boolean = false,
        val getter: () -> Boolean,
        val setter: (Boolean) -> Unit,
    )

    fun allToggles(): List<ToggleSpec> = listOf(
        ToggleSpec(
            KEY_REFUSAL_AUTO_FALLBACK,
            "Sansür / Ahlak Vaazı Koruması",
            "Açıkken bir model karakterden çıkıp vaaz veya ret verirse yanıtı göstermeden sıradaki özgür API'ye geçer.",
            getter = { isRefusalAutoFallbackEnabled },
            setter = ::setRefusalAutoFallback
        ),
        ToggleSpec(
            KEY_STRICT_API_TIMEOUT,
            "Akıllı Zaman Aşımı Koruması (Timeout)",
            "Açıkken takılan yardımcı çağrılar 18 sn, hikâye istekleri 60 sn'de kesilip yedek API'ye geçer. Kapalıyken yavaş/düşünen modeller kesilmeden beklenir.",
            getter = { isStrictApiTimeoutEnabled },
            setter = ::setStrictApiTimeout
        ),
        ToggleSpec(
            KEY_MICRO_AI_CAST_SENTIMENT,
            "Ufak AI Kadro & Duygu Analizi",
            "Yan karakter algılama ve gizli kullanıcı alt metni analizini hızlı yardımcı AI modeliyle güçlendirir (çevrimdışıysa yerel motora düşer).",
            getter = { isMicroAiCastSentimentEnabled },
            setter = ::setMicroAiCastSentiment
        ),
        ToggleSpec(
            KEY_REDUCE_MOTION,
            "Hareketi Azalt & Pil Tasarrufu",
            "Kozmik parçacık ve yörünge animasyonlarını sadeleştirerek pil tüketimini azaltır.",
            getter = { isReduceMotionEnabled },
            setter = ::setReduceMotion
        ),
        ToggleSpec(
            KEY_TTS_EMOTIONAL_PROSODY,
            "TTS: Duygusal Tonlama (Prosodi)",
            "Karakterin o anki duygusuna (üzgün, öfkeli, romantik, heyecanlı) ve noktalama işaretlerine göre ses tonunu, hızını ve nefes aralarını otomatik ayarlar.",
            getter = { isTtsEmotionalProsodyEnabled },
            setter = ::setTtsEmotionalProsody
        ),
        ToggleSpec(
            KEY_TTS_READ_ACTIONS,
            "TTS: Eylem Betimlemelerini Oku",
            "Açıkken sesli okuma sırasında *yıldız* içindeki sahne betimlemeleri de okunur; kapalıyken yalnız diyaloglar okunur.",
            getter = { isTtsReadActionsEnabled },
            setter = ::setTtsReadActions
        ),
        ToggleSpec(
            KEY_TTS_AUTO_READ_REPLIES,
            "TTS: Yeni Yanıtları Otomatik Oku",
            "Sesli Okuma (TTS) açıkken gelen her yeni karakter yanıtını otomatik olarak seslendirir.",
            getter = { isTtsAutoReadRepliesEnabled },
            setter = ::setTtsAutoReadReplies
        ),
        ToggleSpec(KEY_SSE_STREAMING, "Gerçek Zamanlı Akış (SSE)",
            "Model yanıtlarını token token göster, daha hızlı hissettir.",
            getter = { isSseStreamingEnabled }, setter = ::setSseStreaming),
        ToggleSpec(KEY_MEMORY_TIMELINE, "Hafıza Zaman Çizelgesi (Y4)",
            "\"Şimdiye kadar neler oldu?\" ekranını etkinleştirir.",
            getter = { isMemoryTimelineEnabled }, setter = ::setMemoryTimeline),
        ToggleSpec(KEY_ROUTINE_NOTIF, "Rutin Hatırlatıcıları",
            "Arka planda çalışan günlük rutin bildirimleri.",
            getter = { isRoutineNotifEnabled }, setter = { setRoutineNotif(it) }),
        ToggleSpec(KEY_TOKEN_COUNTER, "Token Sayacı",
            "Her sohbette harcanan token miktarını göster.",
            getter = { isTokenCounterEnabled }, setter = ::setTokenCounter),
        ToggleSpec(KEY_DYNAMIC_DIFFICULTY, "Dinamik Zorluk (Deneysel)",
            "Konuşma kalıplarına göre sevgi/güven kazanım hızını otomatik kalibre et.",
            isExperimental = true,
            getter = { isDynamicDifficultyEnabled }, setter = ::setDynamicDifficulty),
        ToggleSpec(KEY_SCENE_IMAGE, "Sahne Görseli Üretimi (Deneysel)",
            "Sahne değişimlerinde Pollinations üzerinden arka plan görseli oluştur.",
            isExperimental = true,
            getter = { isSceneImageEnabled }, setter = ::setSceneImage),
        ToggleSpec(KEY_MULTI_BOT_SCENE, "Çoklu Bot Sahnesi (Deneysel)",
            "İki botun birbirleriyle konuştuğu karşılaşma modunu etkinleştirir.",
            isExperimental = true,
            getter = { isMultiBotSceneEnabled }, setter = ::setMultiBotScene),
    )

    // ─── Yardımcılar ─────────────────────────────────────────────────────────
    private fun get(key: String): Boolean {
        val sp = prefs
        if (sp == null) {
            return memoryOverrides[key] ?: (DEFAULTS[key] ?: false)
        }
        return sp.getBoolean(key, DEFAULTS[key] ?: false)
    }

    private fun set(key: String, value: Boolean) {
        val sp = prefs
        if (sp == null) {
            memoryOverrides[key] = value
            return
        }
        sp.edit().putBoolean(key, value).apply()
    }

    private fun getString(key: String, default: String): String {
        val sp = prefs
        if (sp == null) {
            return memoryStringOverrides[key] ?: default
        }
        return sp.getString(key, default) ?: default
    }

    private fun setString(key: String, value: String) {
        val sp = prefs
        if (sp == null) {
            memoryStringOverrides[key] = value
            return
        }
        sp.edit().putString(key, value).apply()
    }

    private fun syncFromPrefs() {
        _sseStreaming.value        = get(KEY_SSE_STREAMING)
        _memoryTimeline.value      = get(KEY_MEMORY_TIMELINE)
        _routineNotif.value        = get(KEY_ROUTINE_NOTIF)
        _sceneImage.value          = get(KEY_SCENE_IMAGE)
        _dynamicDifficulty.value   = get(KEY_DYNAMIC_DIFFICULTY)
        _multiBotScene.value       = get(KEY_MULTI_BOT_SCENE)
        _tokenCounter.value        = get(KEY_TOKEN_COUNTER)
        _refusalAutoFallback.value = get(KEY_REFUSAL_AUTO_FALLBACK)
        _strictApiTimeout.value    = get(KEY_STRICT_API_TIMEOUT)
        _ttsReadActions.value       = get(KEY_TTS_READ_ACTIONS)
        _ttsAutoReadReplies.value   = get(KEY_TTS_AUTO_READ_REPLIES)
        _ttsEmotionalProsody.value  = get(KEY_TTS_EMOTIONAL_PROSODY)
        _reduceMotion.value         = get(KEY_REDUCE_MOTION)
        _microAiCastSentiment.value = get(KEY_MICRO_AI_CAST_SENTIMENT)
        _ttsEngineMode.value        = getTtsEngineMode()
        _ttsNeuralVoice.value       = getTtsNeuralVoice()

        val sp = prefs
        if (sp != null) {
            val savedOriginIds = sp.getStringSet(KEY_TEMPLATE_ORIGIN_BOTS, emptySet()).orEmpty()
            templateOriginBotIdsMem.addAll(savedOriginIds)
            val utilMap = mutableMapOf<String, String>()
            for ((k, v) in sp.all) {
                if (k.startsWith(KEY_AI_UTIL_MODEL_PREFIX) && v is String && v.isNotBlank()) {
                    utilMap[k.removePrefix(KEY_AI_UTIL_MODEL_PREFIX)] = v
                }
            }
            _aiUtilityModels.value = utilMap
        }
    }
}
