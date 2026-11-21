package com.example.data.repository

import kotlin.math.roundToInt
import androidx.room.withTransaction
import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiEmbedContentRequest
import com.example.data.api.GeminiEmbedContentResponse
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.data.local.AffectionEventEntity
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionHistoryDao
import com.example.data.local.EmotionHistoryEntity
import com.example.data.local.EmotionState
import com.example.data.local.PrimaryEmotions
import com.example.data.local.RelationshipAxes
import com.example.data.local.EntityRegistryEntity
import com.example.data.local.MemoryCheckpointEntity
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MemoryFactEntity
import com.example.data.local.MemoryFragmentEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PromptViolationLogDao
import com.example.data.local.PromptViolationLogEntity
import com.example.data.local.SceneTemplateDao
import com.example.data.local.SceneTemplateEntity
import com.example.data.local.SelfCheckFailureLogDao
import com.example.data.local.SelfCheckFailureLogEntity
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.local.SensitiveTriggerDao
import com.example.data.local.PendingReappraisalEntity
import com.example.data.local.PendingReappraisalDao
import com.example.data.local.ActiveMemoryCallLogEntity
import com.example.data.local.ActiveMemoryCallLogDao
import com.example.data.local.StoryProgressDao
import com.example.data.local.StoryProgressEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.local.WorldAtmosphere
import com.example.data.local.EntityRelationEntity
import com.example.data.local.RoutineEntity
import com.example.data.local.ImplicitCorrectionLogEntity
import com.example.data.local.ArchivedMemoryEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class KeyCharacter(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val desc: String = ""
)

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

data class EffectiveBotSettings(
    val isNsfwAllowed: Boolean,
    val responseLength: String,
    val enableOoc: Boolean
)

data class BackupSnapshot(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val bots: List<BotEntity>,
    val messages: List<MessageEntity>,
    val settings: UserSettingsEntity,
    val memoryFacts: List<com.example.data.local.MemoryFactEntity> = emptyList(),
    val memoryEvents: List<com.example.data.local.MemoryEventEntity> = emptyList(),
    val memoryCheckpoints: List<com.example.data.local.MemoryCheckpointEntity> = emptyList(),
    val castMembers: List<com.example.data.local.CastMemberEntity> = emptyList(),
    val characterEmotions: List<com.example.data.local.CharacterEmotionEntity> = emptyList(),
    val sensitiveTriggers: List<com.example.data.local.SensitiveTriggerEntity> = emptyList(),
    val routines: List<com.example.data.local.RoutineEntity> = emptyList(),
    val entityRelations: List<com.example.data.local.EntityRelationEntity> = emptyList(),
    val milestones: List<com.example.data.local.MilestoneEntity> = emptyList()
)

data class BackupImportSummary(
    val version: Int = 2,
    val botsCount: Int = 0,
    val messagesCount: Int = 0,
    val memoryFactsCount: Int = 0,
    val memoryEventsCount: Int = 0,
    val castMembersCount: Int = 0,
    val milestonesCount: Int = 0,
    val overwriteExisting: Boolean = false
)

fun UserSettingsEntity.sanitizedForBackup(): UserSettingsEntity = copy(
    customApiKey = "",
    groqApiKey = "",
    claudeApiKey = "",
    openaiApiKey = "",
    openRouterApiKey = "",
    nvidiaApiKey = "",
    mistralApiKey = "",
    backupApiKey = ""
)

fun UserSettingsEntity.mergePreservingExistingKeys(existing: UserSettingsEntity?): UserSettingsEntity {
    if (existing == null) return this
    return copy(
        customApiKey = customApiKey.ifBlank { existing.customApiKey },
        groqApiKey = groqApiKey.ifBlank { existing.groqApiKey },
        claudeApiKey = claudeApiKey.ifBlank { existing.claudeApiKey },
        openaiApiKey = openaiApiKey.ifBlank { existing.openaiApiKey },
        openRouterApiKey = openRouterApiKey.ifBlank { existing.openRouterApiKey },
        nvidiaApiKey = nvidiaApiKey.ifBlank { existing.nvidiaApiKey },
        mistralApiKey = mistralApiKey.ifBlank { existing.mistralApiKey },
        backupApiKey = backupApiKey.ifBlank { existing.backupApiKey }
    )
}

data class ProviderFallbackLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val providerName: String,
    val status: String, // "TRYING", "SUCCESS", "FAILED"
    val statusCode: Int? = null,
    val errorMessage: String? = null,
    val layer: Int = 1
)

data class AiReplyResult(
    val replyText: String,
    val usedProvider: String,
    val systemPromptSnapshot: String = "",
    val memoryCommitSummary: EmochiRepository.MemoryCommitSummary = EmochiRepository.MemoryCommitSummary(),
    val ragRouteMode: MemoryEngine.RagRouteMode = MemoryEngine.RagRouteMode.HYBRID_FAST
)

object ContextMultiplierConfig {
    var publicSettingMultiplier: Double = 0.4
    var privateSettingMultiplier: Double = 1.0

    var formalModeMultiplier: Double = 0.3
    var casualModeMultiplier: Double = 1.0

    var noneTensionMultiplier: Double = 1.0
    var conflictTensionMultiplier: Double = 0.1
    var crisisTensionMultiplier: Double = 0.0

    fun getSettingMultiplier(setting: String): Double {
        val s = setting.lowercase().trim()
        return if (s.contains("public") || s == "pub" || s == "p_pub") publicSettingMultiplier else privateSettingMultiplier
    }

    fun getModeMultiplier(mode: String): Double {
        val m = mode.lowercase().trim()
        return if (m.contains("formal") || m == "form" || m == "f") formalModeMultiplier else casualModeMultiplier
    }

    fun getTensionMultiplier(tension: String): Double {
        val t = tension.lowercase().trim()
        return when {
            t.contains("crisis") || t == "cris" || t == "cr" -> crisisTensionMultiplier
            t.contains("conflict") || t == "conf" || t == "c" -> conflictTensionMultiplier
            else -> noneTensionMultiplier
        }
    }
}

class EmochiRepository(
    private val db: AppDatabase,
    private val context: android.content.Context? = null
) {
    companion object {
        @Volatile
        var activeBotId: String? = null

        @Volatile
        var ovhCooldownUntilTimestamp: Long = 0L

        const val THRESHOLD_VECTOR_ONLY = 0.75f
        const val THRESHOLD_QUERY_REWRITE = 0.50f
        const val MAX_USER_INPUT_CHARS = 4000
        private const val BUFFER_MAX_SIZE = 30
        private const val BUFFER_EXPIRY_TURNS = 40

        @JvmStatic
        fun cleanEmotionTags(rawText: String): String {
            if (rawText.isBlank()) return ""
            var result = rawText

            result = result
                .replace(Regex("""(?is)<think>.*?</think>"""), "")
                .replace(Regex("""(?is)<reasoning>.*?</reasoning>"""), "")
                // Unclosed / truncated trailing reasoning or internal state tags (zero tag leakage)
                .replace(Regex("""(?is)<think>.*$"""), "")
                .replace(Regex("""(?is)<reasoning>.*$"""), "")
                // Remove paired character emotion blocks
                .replace(Regex("""(?is)\[\[?CHARACTER_EMOTION[:\s][^\]]*\]\]?.*?\[\[?/(?:CHARACTER_EMOTION|CHARACTER)[^\]]*\]\]?"""), "")
                .replace(Regex("""(?is)\[\[?WORLD_ATMOSPHERE[:\s][^\]]*\]\]?.*?\[\[?/(?:WORLD_ATMOSPHERE)[^\]]*\]\]?"""), "")
                .replace(Regex("""(?is)\[\[?EMOTION_UPDATE[:\s][^\]]*\]\]?.*?\[\[?/(?:EMOTION_UPDATE)[^\]]*\]\]?"""), "")
                // Remove any opening, closing, or standalone tags for CHARACTER_EMOTION, CAST_MEMBER, etc.
                .replace(Regex("""(?is)\[\[?/?(?:CHARACTER_EMOTION|CAST_MEMBER|CHARACTER|WORLD_ATMOSPHERE|EMOTION_UPDATE)(?::\s*[^\]\n]+)?\]\]?"""), "")
                .replace(Regex("""(?is)\[\[STATE_JSON\s*\{.*?\}\s*\]\]"""), "")
                .replace(Regex("""(?is)\[\[STATE\s+affectionScore=.*?\]\]"""), "")
                .replace(Regex("""(?is)\[\[STATE.*?\]\]"""), "")
                .replace(Regex("""(?is)\[\[MEMORY_SAVE.*?\]\]"""), "")
                .replace(Regex("""(?is)\[ACTIVE_MEMORY_CALL:.*?\]"""), "")
                // Strip unclosed / truncated trailing protocol blocks if model hit max_tokens mid-tag
                .replace(Regex("""(?is)\[\[STATE_JSON[\s\S]*$"""), "")
                .replace(Regex("""(?is)\[\[STATE[\s\S]*$"""), "")
                .replace(Regex("""(?is)\[\[?CHARACTER_EMOTION[\s\S]*$"""), "")
                .replace(Regex("""(?is)\[ACTIVE_MEMORY_CALL[\s\S]*$"""), "")
                .replace(Regex("""(?is)```(?:json)?\s*\{.*?"primary_emotions".*?\}\s*```"""), "")
                .replace(Regex("""(?is)```(?:json)?\s*\{.*?"primary_emotion".*?\}\s*```"""), "")

            val cleanLines = result.lines().filterNot { line ->
                val trimmed = line.trim().trimStart('-', '*', '•', '>', ' ', '"', '\'').trim()
                val l = trimmed.lowercase(java.util.Locale.ROOT)
                val lTr = trimmed.lowercase(java.util.Locale("tr"))

                fun checkPrefix(p: String): Boolean = l.startsWith(p) || lTr.startsWith(p)

                checkPrefix("primary_emotion") ||
                checkPrefix("secondary_emotion") ||
                checkPrefix("primary_emotions") ||
                checkPrefix("secondary_emotions") ||
                checkPrefix("dominant_emotion") ||
                checkPrefix("suppressed_emotion") ||
                checkPrefix("computed_secondary_emotion") ||
                checkPrefix("birincil") ||
                checkPrefix("ikincil") ||
                checkPrefix("dominant_duygu") ||
                checkPrefix("baskın_duygu") ||
                checkPrefix("baskin_duygu") ||
                checkPrefix("baskılanan") ||
                checkPrefix("baskilanan") ||
                checkPrefix("bastırılan") ||
                checkPrefix("bastirilan") ||
                checkPrefix("yakınlık") ||
                checkPrefix("yakinlik") ||
                checkPrefix("güven") ||
                checkPrefix("guven") ||
                checkPrefix("kırgınlık") ||
                checkPrefix("kirginlik") ||
                checkPrefix("saygı") ||
                checkPrefix("saygi") ||
                checkPrefix("rahatlık") ||
                checkPrefix("rahatlik") ||
                checkPrefix("gerilim") ||
                checkPrefix("bağlılık") ||
                checkPrefix("baglilik") ||
                checkPrefix("takıntı") ||
                checkPrefix("takinti") ||
                checkPrefix("relationship_axes") ||
                checkPrefix("physicalcomfortscore") ||
                checkPrefix("fiziksel_rahatlık") ||
                checkPrefix("fiziksel_rahatlik") ||
                checkPrefix("self_check") ||
                checkPrefix("schemaversion") ||
                checkPrefix("mood:") ||
                checkPrefix("secondary_mood:") ||
                checkPrefix("suppressed_emotion:") ||
                checkPrefix("intensity:") ||
                checkPrefix("current_event:") ||
                checkPrefix("macro_atmosphere:") ||
                checkPrefix("micro_atmosphere:") ||
                checkPrefix("affection_delta:") ||
                checkPrefix("trust_delta:") ||
                checkPrefix("tension_delta:") ||
                checkPrefix("hurt_delta:") ||
                checkPrefix("speech_pattern:") ||
                checkPrefix("obsession_delta:") ||
                checkPrefix("active_memory_call") ||
                checkPrefix("[active_memory_call") ||
                checkPrefix("[emotion_update") ||
                checkPrefix("[character_emotion") ||
                checkPrefix("[world_atmosphere") ||
                checkPrefix("[[state") ||
                checkPrefix("[[character_emotion") ||
                checkPrefix("[[cast_member") ||
                checkPrefix("[/character_emotion") ||
                checkPrefix("[[/character_emotion") ||
                checkPrefix("[/world_atmosphere") ||
                checkPrefix("[/emotion_update") ||
                checkPrefix("[/cast_member") ||
                checkPrefix("delta:") ||
                checkPrefix("reason:") ||
                checkPrefix("setting:") ||
                checkPrefix("mode:") ||
                checkPrefix("tension:") ||
                checkPrefix("affection:") ||
                checkPrefix("trust:") ||
                checkPrefix("hurt:") ||
                checkPrefix("obsession:") ||
                checkPrefix("respect:") ||
                checkPrefix("comfort:") ||
                // Filter pipe-separated emotion stat lines e.g. "Yakınlık: 55/100 | Güven: 65/100 | Kırgınlık: 0/100"
                (trimmed.contains("|") && (
                    l.contains("güven:") || l.contains("guven:") || l.contains("trust:") ||
                    l.contains("kırgınlık:") || l.contains("kirginlik:") || l.contains("hurt:") ||
                    l.contains("yakınlık:") || l.contains("yakinlik:") || l.contains("affection:") ||
                    l.contains("saygı:") || l.contains("saygi:") || l.contains("respect:") ||
                    l.contains("gerilim:") || l.contains("tension:")
                )) ||
                trimmed.matches(Regex("""(?i)(?:yakınlık|yakinlik|affection|güven|guven|trust|kırgınlık|kirginlik|hurt|gerilim|tension)\s*:\s*\d+.*""")) ||
                l.contains("character_emotion") ||
                l.contains("world_atmosphere") ||
                l.contains("emotion_update") ||
                l.contains("[[state")
            }

            val cleaned = cleanLines.joinToString("\n").trim()
            if (cleaned.isNotBlank()) return cleaned

            // Fallback 1: Try parsing JSON for embedded response text
            try {
                val jsonMatch = Regex("""(?is)\{.*\}""").find(rawText)?.value
                if (jsonMatch != null) {
                    val jsonObj = org.json.JSONObject(jsonMatch)
                    val possibleKeys = listOf("response_text", "dialogue", "response", "reply", "message", "content", "narrative", "text")
                    for (key in possibleKeys) {
                        val str = jsonObj.optString(key, "")
                        if (str.isNotBlank()) {
                            return cleanEmotionTags(str.trim())
                        }
                    }
                }
            } catch (_: Exception) {}

            // Fallback 2: Extract text from <think> or <reasoning> if model put narrative inside think tags
            try {
                val thinkMatches = Regex("""(?is)<think>(.*?)</think>""").findAll(rawText)
                val thinkTexts = thinkMatches.map { it.groupValues[1] }.joinToString("\n").trim()
                if (thinkTexts.isNotBlank()) {
                    val cleanedThink = cleanEmotionTags(thinkTexts)
                    if (cleanedThink.length > 15) {
                        return cleanedThink
                    }
                }
            } catch (_: Exception) {}

            // Fallback 3: If only emotion tags existed and no narrative was present, return empty string
            return ""
        }

        fun calculateCastMemberDifficultyMultiplier(role: String, characterName: String, cueSentence: String = ""): Double {
            val combined = "$role $characterName $cueSentence".lowercase(java.util.Locale("tr", "TR"))
            return when {
                combined.contains("düşman") || combined.contains("rakip") || combined.contains("hain") || combined.contains("hasım") -> 0.50

                combined.contains("patron") || combined.contains("boss") || combined.contains("yönetici") ||
                combined.contains("müdür") || combined.contains("ceo") || combined.contains("öğretmen") ||
                combined.contains("hoca") || combined.contains("profesör") || combined.contains("doktor") ||
                combined.contains("dr.") || combined.contains("komutan") || combined.contains("subay") ||
                combined.contains("amir") || combined.contains("yüzbaşı") || combined.contains("kaptan") ||
                combined.contains("kral") || combined.contains("prens") || combined.contains("baba") ||
                combined.contains("anne") -> 0.70

                combined.contains("yabancı") || combined.contains("tanıdık") || combined.contains("müşteri") ||
                combined.contains("barista") || combined.contains("garson") || combined.contains("sürücü") -> 0.85

                combined.contains("sevgili") || combined.contains("eş") || combined.contains("yakın dost") ||
                combined.contains("dost") || combined.contains("arkadaşım") || combined.contains("kardeş") -> 1.40

                combined.contains("güvenilir arkadaş") || combined.contains("sırdaş") -> 1.20

                else -> 1.05
            }
        }

        fun determineEffectiveRoleMultiplier(member: CastMemberEntity, sceneContext: String): Double {
            val lowerContext = sceneContext.lowercase(java.util.Locale("tr", "TR"))
            val workWords = listOf("işyeri", "ofis", "rapor", "toplantı", "görev", "şirket", "proje", "patron", "çalış", "mesai", "bütçe", "sunum")
            val hasStandaloneIs = Regex("""\biş\b""").containsMatchIn(lowerContext)
            val isWorkContext = hasStandaloneIs || workWords.any { lowerContext.contains(it) }
            val isPersonalContext = listOf("özel", "anı", "eski", "akşam", "sevgili", "aşk", "ev", "kahve", "sohbet", "yemek", "kalp", "romantik", "baş başa", "gece").any { lowerContext.contains(it) }

            val candidateRoles = mutableListOf<String>()
            if (member.role.isNotBlank()) {
                member.role.split("/", ",").map { it.trim() }.filter { it.isNotBlank() }.forEach { candidateRoles.add(it) }
            }
            if (member.secondaryRole.isNotBlank()) {
                member.secondaryRole.split("/", ",").map { it.trim() }.filter { it.isNotBlank() }.forEach { candidateRoles.add(it) }
            }
            if (member.multiRoleJson.isNotBlank() && member.multiRoleJson != "[]") {
                try {
                    val arr = org.json.JSONArray(member.multiRoleJson)
                    for (i in 0 until arr.length()) {
                        val r = arr.optString(i).trim()
                        if (r.isNotBlank()) candidateRoles.add(r)
                    }
                } catch (_: Exception) {}
            }

            val baseMult = if (member.baseAffectionDifficultyMultiplier > 0.0) {
                member.baseAffectionDifficultyMultiplier
            } else {
                calculateCastMemberDifficultyMultiplier(member.role, member.name, "")
            }

            val roleMultipliers = candidateRoles.distinct().map { roleItem ->
                calculateCastMemberDifficultyMultiplier(roleItem, member.name, "")
            }.toMutableList()
            if (member.baseAffectionDifficultyMultiplier > 0.0) {
                roleMultipliers.add(member.baseAffectionDifficultyMultiplier)
            }

            if (roleMultipliers.size > 1) {
                val chosen = when {
                    isWorkContext && !isPersonalContext -> roleMultipliers.minOrNull() ?: baseMult
                    isPersonalContext && !isWorkContext -> roleMultipliers.maxOrNull() ?: baseMult
                    isWorkContext && isPersonalContext -> roleMultipliers.minOrNull() ?: baseMult
                    else -> roleMultipliers.minOrNull() ?: baseMult
                }
                // 3.7 kanıt #5: çoklu rol bağlam seçimi log
                android.util.Log.d("MultiRole", "[determineEffectiveRoleMultiplier] ${member.name} roles=${candidateRoles.distinct()} multipliers=$roleMultipliers isWork=$isWorkContext isPersonal=$isPersonalContext → chosen=$chosen")
                return chosen
            }
            return baseMult
        }

        // ─── Central: single-source provider/settings resolution for utility calls ─────────
        // Eliminates duplicated hasKey/activeProvider logic in evaluateCharacterSignificance
        // and reEvaluateCastMemberRolesIfNeeded. Returns Pair(activeProvider, effectiveSettings).
        fun resolveActiveProviderAndSettings(
            settings: UserSettingsEntity,
            customMap: Map<Long, com.example.data.local.CustomProviderEntity>,
            buildKey: String,
            apiKey: String? = null
        ): Pair<String?, UserSettingsEntity> {
            val effectiveSettings = if (!apiKey.isNullOrBlank()) {
                when (settings.selectedProvider) {
                    "groq" -> if (settings.groqApiKey.isBlank()) settings.copy(groqApiKey = apiKey) else settings
                    "claude" -> if (settings.claudeApiKey.isBlank()) settings.copy(claudeApiKey = apiKey) else settings
                    "openai" -> if (settings.openaiApiKey.isBlank()) settings.copy(openaiApiKey = apiKey) else settings
                    "openrouter" -> if (settings.openRouterApiKey.isBlank()) settings.copy(openRouterApiKey = apiKey) else settings
                    "nvidia" -> if (settings.nvidiaApiKey.isBlank()) settings.copy(nvidiaApiKey = apiKey) else settings
                    "mistral" -> if (settings.mistralApiKey.isBlank()) settings.copy(mistralApiKey = apiKey) else settings
                    else -> if (settings.customApiKey.isBlank()) settings.copy(customApiKey = apiKey) else settings
                }
            } else settings

            val hasValidGemini = effectiveSettings.customApiKey.isNotBlank() || (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY")
            fun hasKey(p: String): Boolean = when (p) {
                "gemini" -> hasValidGemini
                "groq" -> effectiveSettings.groqApiKey.isNotBlank() || effectiveSettings.customApiKey.isNotBlank()
                "claude" -> effectiveSettings.claudeApiKey.isNotBlank()
                "openai" -> effectiveSettings.openaiApiKey.isNotBlank()
                "openrouter" -> effectiveSettings.openRouterApiKey.isNotBlank()
                "nvidia" -> effectiveSettings.nvidiaApiKey.isNotBlank()
                "mistral" -> effectiveSettings.mistralApiKey.isNotBlank()
                "llm7" -> effectiveSettings.enableLlm7 && effectiveSettings.llm7Enabled
                "pollinations" -> effectiveSettings.enablePollinations && effectiveSettings.pollinationsEnabled
                "ovh" -> effectiveSettings.enableOvh && effectiveSettings.ovhEnabled
                else -> if (p.startsWith("custom_")) {
                    val id = p.removePrefix("custom_").toLongOrNull()
                    id != null && customMap.containsKey(id)
                } else false
            }
            val freeFirst = com.example.data.managers.ApiPriorityCodec.isFreeFirst(effectiveSettings.fallbackChainOrder)
            val keyedChain = com.example.data.managers.ApiPriorityCodec.resolveOrderedConfiguredKeyedProviders(
                settings = effectiveSettings,
                customProviders = customMap.values.toList()
            ).filter { hasKey(it) }
            val freeChain = com.example.data.managers.ApiProviderCatalog.FREE_KEYLESS_IDS.filter { hasKey(it) }

            val active = if (freeFirst) {
                freeChain.firstOrNull() ?: keyedChain.firstOrNull()
            } else {
                keyedChain.firstOrNull()
                    ?: if (hasKey(effectiveSettings.selectedProvider)) effectiveSettings.selectedProvider else null
                    ?: freeChain.firstOrNull()
            } ?: if (hasValidGemini) "gemini" else null

            android.util.Log.d("resolveActiveProvider", "[resolveActiveProviderAndSettings] freeFirst=$freeFirst selected=${settings.selectedProvider} → active=$active")
            return Pair(active, effectiveSettings)
        }

        fun evaluateCharacterSignificance(
            candidateName: String,
            sceneContext: String,
            apiKey: String? = null
        ): Quadruple<Boolean, String, Int, Double> {
            val clean = candidateName.trim().removeSurrounding("\"").removeSurrounding("'")
            val lower = clean.lowercase()

            val genericExtras = setOf("garson", "zorba", "çocuk", "adam", "kadın", "polis", "müşteri", "yoldan geçen", "şoför", "taksici", "nöbetçi", "biri", "herkes")
            if (genericExtras.contains(lower) || genericExtras.any { lower.contains(it) && !lower.contains("doktor") && !lower.contains("komutan") }) {
                return Quadruple(false, "Figüran", 20, 1.0)
            }

            val isProperName = clean.length in 3..25 && clean.firstOrNull()?.isUpperCase() == true
            val hasSpeechOrTitle = sceneContext.contains("$clean dedi") || sceneContext.contains("$clean sordu") ||
                    sceneContext.contains("Bay $clean") || sceneContext.contains("Doktor $clean") || sceneContext.contains("Dr. $clean") ||
                    clean.contains("Doktor", ignoreCase = true) || clean.contains("Dr.", ignoreCase = true) ||
                    clean.contains("Komutan", ignoreCase = true) || clean.contains("Kaptan", ignoreCase = true)
            val confidence = if (hasSpeechOrTitle) 85 else if (isProperName) 65 else 40
            val role = when {
                clean.contains("Doktor", ignoreCase = true) || clean.contains("Dr.", ignoreCase = true) ||
                sceneContext.contains("Doktor", ignoreCase = true) || sceneContext.contains("Dr.", ignoreCase = true) -> "Doktor"
                clean.contains("Komutan", ignoreCase = true) || clean.contains("Kaptan", ignoreCase = true) ||
                sceneContext.contains("Komutan", ignoreCase = true) || sceneContext.contains("Kaptan", ignoreCase = true) -> "Komutan"
                sceneContext.contains("arkadaş", ignoreCase = true) || sceneContext.contains("dost", ignoreCase = true) -> "Arkadaş"
                else -> "Yan Karakter"
            }
            val mult = calculateCastMemberDifficultyMultiplier(role, clean, sceneContext)
            return Quadruple(confidence >= 50, role, confidence, mult)
        }

        fun calculateClampedDelta(
            prevAffection: Int,
            rawDelta: Int,
            totalMsgCount: Int,
            baseMultiplier: Double,
            combinedContextMult: Double,
            personalityAffectionMult: Double,
            recoveryLockUntilMessageCount: Int,
            dailyAffectionGain: Int,
            consecutivePositiveCount: Int,
            regCount: Int = 0,
            isEstablishedRomantic: Boolean = false
        ): Int {
            if (rawDelta <= 0) {
                // Negative deltas (drops/hurts) are allowed to go through, capped safely at -35
                return rawDelta.coerceIn(-35, 0)
            }

            var delta = rawDelta.coerceIn(0, 15)
            if (totalMsgCount < 5 && !isEstablishedRomantic) {
                delta = minOf(delta, 3)
            }

            var maxAllowed = when {
                prevAffection < 60 -> 8
                prevAffection in 60..85 -> 6
                else -> 4
            }

            if (totalMsgCount < recoveryLockUntilMessageCount) {
                maxAllowed = (maxAllowed * 0.5).roundToInt().coerceAtLeast(1)
            }

            val effectiveMaxAllowed = (maxAllowed * baseMultiplier * combinedContextMult * personalityAffectionMult).roundToInt().coerceAtLeast(1)
            delta = minOf(delta, effectiveMaxAllowed)

            val maxDailyBudget = (35 * baseMultiplier).roundToInt().coerceAtLeast(12)
            val remainingDailyBudget = (maxDailyBudget - dailyAffectionGain).coerceAtLeast(0)
            delta = minOf(delta, remainingDailyBudget)

            if (regCount > 3) delta = 0

            return delta.coerceIn(0, 15)
        }

        fun sanitizeModelName(model: String): String {
            val trimmed = model.trim()
            val clean = trimmed.lowercase()
            return when {
                // Only redirect genuinely decommissioned legacy model IDs; NEVER overwrite valid catalog models
                // (gemini-2.5-flash, gemini-2.5-pro, llama-3.3-70b-versatile, llama-3.1-8b-instant, qwen/qwen3-32b, etc.)
                clean.contains("decommissioned") ||
                    clean == "llama3-70b-8192" ||
                    clean == "llama3-8b-8192" -> "openai/gpt-oss-120b"
                clean.isEmpty() -> "gemini-2.0-flash"
                else -> trimmed
            }
        }

        fun getEffectiveModelForProvider(settings: UserSettingsEntity): String {
            val provider = settings.selectedProvider.lowercase().trim()
            val raw = when (provider) {
                "groq" -> settings.groqModel.ifBlank { "openai/gpt-oss-120b" }
                "openai" -> settings.openaiModel.ifBlank { "gpt-4o" }
                "gemini" -> settings.geminiModel.ifBlank { "gemini-2.0-flash" }
                "claude" -> settings.claudeModel.ifBlank { "claude-3-5-sonnet-20241022" }
                "openrouter" -> settings.openRouterModel.ifBlank { "deepseek/deepseek-chat" }
                "nvidia" -> settings.nvidiaModel.ifBlank { "deepseek-ai/deepseek-v4-flash" }
                "mistral" -> settings.mistralModel.ifBlank { "mistral-large-latest" }
                "pollinations" -> settings.pollinationsModel.ifBlank { "openai" }
                "ovh" -> settings.ovhModel.ifBlank { "meta-llama/Meta-Llama-3-70B-Instruct" }
                else -> {
                    // Custom/unrecognized provider — model is resolved via resolveModelForProvider which has customProvider in scope
                    "gemini-2.0-flash"
                }
            }
            return sanitizeModelName(raw)
        }

        // ─── Utility Model Resolution ────────────────────────────────────────────
        // Single source of truth for which model handles cheap background tasks
        // (character classification, query rewriting, re-rank, checkpoint summarization,
        //  near-duplicate checks, inconsistency detection).
        // Custom Provider path: tries utilityModel → auto-detected small model → main model.
        fun resolveUtilityModel(
            providerKey: String,
            settings: UserSettingsEntity,
            customProvider: com.example.data.local.CustomProviderEntity? = null
        ): String {
            return when (providerKey.lowercase()) {
                "groq" -> "openai/gpt-oss-20b"
                "openai" -> "gpt-4o-mini"
                "claude" -> "claude-3-haiku-20240307"
                "mistral" -> "mistral-small-latest"
                "gemini" -> "gemini-2.0-flash"
                "openrouter" -> settings.openRouterModel.ifBlank { "deepseek/deepseek-chat" }
                "nvidia" -> "deepseek-ai/deepseek-v4-flash"
                "pollinations" -> "openai"
                "ovh" -> "meta-llama/Meta-Llama-3-70B-Instruct"
                "llm7" -> "default"
                else -> {
                    // Custom Provider: manual override wins, then auto-detected, then main model
                    val manual = customProvider?.utilityModel?.takeIf { it.isNotBlank() }
                    if (manual != null) {
                        android.util.Log.d("resolveUtilityModel", "[custom] Manual utility model: $manual")
                        return manual
                    }
                    val autoDetected = customProvider?.let { parseDetectedSmallModels(it.detectedSmallModels).firstOrNull() }
                    if (autoDetected != null) {
                        android.util.Log.d("resolveUtilityModel", "[custom] Auto-detected utility model: $autoDetected")
                        return autoDetected
                    }
                    val mainModel = customProvider?.modelName?.takeIf { it.isNotBlank() }
                        ?: "gpt-4o-mini"
                    android.util.Log.d("resolveUtilityModel", "[custom] Fallback to main model: $mainModel")
                    mainModel
                }
            }
        }

        fun parseDetectedSmallModels(json: String): List<String> {
            if (json.isBlank() || json == "[]") return emptyList()
            return try {
                val arr = org.json.JSONArray(json)
                (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
            } catch (_: Exception) { emptyList() }
        }

        // Detects small/cheap model candidates from a list of model ids.
        // Returns ranked list: smallest param count first, then name-based priority.
        fun detectSmallModelCandidates(modelIds: List<String>): List<String> {
            val smallKeywords = setOf("mini", "small", "lite", "flash", "nano", "turbo", "fast", "tiny", "base")
            val paramRegex = Regex("""(\d+)b\b""", RegexOption.IGNORE_CASE)
            val namePriority = listOf("mini", "small", "lite", "flash", "nano", "turbo", "fast", "tiny", "base")

            data class Candidate(val id: String, val paramCount: Int?, val nameRank: Int)

            return modelIds
                .filter { id ->
                    val lower = id.lowercase()
                    val param = paramRegex.find(lower)?.groupValues?.get(1)?.toIntOrNull()
                    if (param != null && param > 20) {
                        false
                    } else {
                        smallKeywords.any { lower.contains(it) } || (param != null && param <= 20)
                    }
                }
                .map { id ->
                    val lower = id.lowercase()
                    val param = paramRegex.find(lower)?.groupValues?.get(1)?.toIntOrNull()
                    val nameRank = namePriority.indexOfFirst { lower.contains(it) }.let { if (it == -1) 999 else it }
                    Candidate(id, param, nameRank)
                }
                .sortedWith(compareBy(
                    { it.paramCount ?: Int.MAX_VALUE },
                    { it.nameRank }
                ))
                .map { it.id }
        }
    }

    internal val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var backupDebounceJob: Job? = null
    private val BACKUP_DEBOUNCE_MS = 10_000L

    internal val botDao = db.botDao()
    internal val messageDao = db.messageDao()

    private val _providerFallbackLog = MutableStateFlow<List<ProviderFallbackLogEntry>>(emptyList())
    val providerFallbackLog: StateFlow<List<ProviderFallbackLogEntry>> = _providerFallbackLog.asStateFlow()

    fun clearFallbackLogs() {
        _providerFallbackLog.value = emptyList()
    }

    private fun logFallbackAttempt(entry: ProviderFallbackLogEntry) {
        _providerFallbackLog.value = _providerFallbackLog.value + entry
        android.util.Log.d("EmochiFallback", "[${entry.status}] ${entry.providerName} - ${entry.errorMessage ?: "Success"}")
    }

    data class ClassificationCallFailure(
        val provider: String,
        val errorMessage: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    val classificationCallFailureLog = java.util.concurrent.CopyOnWriteArrayList<ClassificationCallFailure>()

    fun clearClassificationCallFailureLogs() {
        classificationCallFailureLog.clear()
    }
    internal val settingsDao = db.userSettingsDao()
    internal val fragmentDao = db.memoryFragmentDao()
    internal val emotionDao = db.characterEmotionDao()
    internal val storyProgressDao = db.storyProgressDao()
    internal val affectionEventDao = db.affectionEventDao()
    internal val promptViolationLogDao = db.promptViolationLogDao()
    internal val sceneTemplateDao = db.sceneTemplateDao()
    internal val memoryFactDao = db.memoryFactDao()
    internal val memoryEventDao = db.memoryEventDao()
    internal val entityRegistryDao = db.entityRegistryDao()
    internal val memoryCheckpointDao = db.memoryCheckpointDao()
    internal val castMemberDao = db.castMemberDao()
    internal val emotionHistoryDao = db.emotionHistoryDao()
    internal val selfCheckFailureLogDao = db.selfCheckFailureLogDao()
    internal val sensitiveTriggerDao = db.sensitiveTriggerDao()
    internal val pendingReappraisalDao = db.pendingReappraisalDao()
    internal val activeMemoryCallLogDao = db.activeMemoryCallLogDao()
    internal val timePerceptionMismatchLogDao = db.timePerceptionMismatchLogDao()
    internal val malformedOutputLogDao = db.malformedOutputLogDao()
    internal val entityRelationDao = db.entityRelationDao()
    internal val routineDao = db.routineDao()
    internal val implicitCorrectionLogDao = db.implicitCorrectionLogDao()
    internal val archivedMemoryDao = db.archivedMemoryDao()

    val lastRetrievalStageMap = java.util.concurrent.ConcurrentHashMap<String, String>()
    internal val regenerateCountMap = java.util.concurrent.ConcurrentHashMap<String, Int>()
    internal val recentChannelAToolOpsMap = java.util.concurrent.ConcurrentHashMap<String, List<String>>()

    // Geçici tutarlılık notu: bir sonraki prompt'a enjekte edilir, okunduktan sonra temizlenir
    internal val pendingConsistencyNoteMap = java.util.concurrent.ConcurrentHashMap<String, String>()
    // Geçici sönüm notu: hikâye atlaması sonrası prompt'a enjekte edilir
    private val pendingDecayNoteMap = java.util.concurrent.ConcurrentHashMap<String, String>()
    // Arka plan denetimi için mesaj sayacı: her botId için son denetim anındaki mesaj sayısı
    private val lastAuditMessageCountMap = java.util.concurrent.ConcurrentHashMap<String, Int>()

    // In-memory candidate buffer — WEAK-signal adaylar ikinci görünümde kayda alınır
    // Yapı: botId → (canonicalNameLower → CandidateBufferEntry)
    private data class CandidateBufferEntry(
        val role: String,
        val firstTurnIndex: Int,
        val cueSnippet: String,
        val signal: CastDetectorEngine.SignalStrength
    )
    private val candidateBuffer = java.util.concurrent.ConcurrentHashMap<String, MutableMap<String, CandidateBufferEntry>>()
    private val globalTurnCounter = java.util.concurrent.atomic.AtomicInteger(0)

    fun getRegenerateCount(botId: String): Int = regenerateCountMap[botId] ?: 0

    fun incrementRegenerateCount(botId: String): Int {
        val next = (regenerateCountMap[botId] ?: 0) + 1
        regenerateCountMap[botId] = next
        return next
    }

    fun resetRegenerateCount(botId: String) {
        regenerateCountMap[botId] = 0
    }

    fun sanitizeUserInput(userText: String): String {
        if (userText.isBlank()) return userText
        // Maksimum girdi uzunluğu: 4000 karakter — context bombing önlemi
        val bounded = if (userText.length > MAX_USER_INPUT_CHARS) userText.take(MAX_USER_INPUT_CHARS) else userText
        return bounded
            // STATE ve sistem etiketleri — enjeksiyon koruması
            .replace(Regex("""(?i)\[\[STATE.*?\]\]""", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("""(?i)\[\[.*?\]\]""", RegexOption.DOT_MATCHES_ALL), "")
            // Komut benzeri etiketler kullanıcıdan gelirse temizle
            .replace(Regex("""(?i)\[ACTIVE_MEMORY_CALL:.*?\]""", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("""(?i)\[CHARACTER_EMOTION:.*?\]""", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("""(?i)\[CAST_MEMBER:.*?\]""", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("""(?i)\[SYSTEM:.*?\]""", RegexOption.DOT_MATCHES_ALL), "")
            // Sabit string temizlikleri
            .replace("SYSTEM:", "")
            .replace("affectionScore=", "")
            .replace("delta=", "")
            .trim()
    }

    fun getAffectionEventsFlow(botId: String) = affectionEventDao.getEventsForBot(botId)

    private fun loadAssetText(fileName: String): String? {
        return try {
            context?.assets?.open(fileName)?.bufferedReader()?.use { it.readText() }
        } catch (_: Exception) {
            null
        }
    }

    fun getCharacterEmotionsFlow(botId: String): Flow<List<CharacterEmotionEntity>> {
        return emotionDao.getEmotionsForBotFlow(botId).map { list ->
            val bot = botDao.getBotById(botId)
            val blacklisted = castMemberDao.getBlacklistedNamesForBot(botId).map { it.lowercase() }.toSet()
            list.filter { em ->
                val clean = cleanCharacterNameCandidate(em.characterName)
                clean.length >= 2 &&
                (bot == null || (!isMainOrUserCharacter(em.characterName, bot) && !isMainOrUserCharacter(clean, bot))) &&
                !blacklisted.contains(em.characterName.lowercase()) &&
                !blacklisted.contains(clean.lowercase())
            }
        }
    }

    suspend fun getCharacterEmotions(botId: String): List<CharacterEmotionEntity> {
        val list = emotionDao.getEmotionsForBot(botId)
        val bot = botDao.getBotById(botId)
        val blacklisted = castMemberDao.getBlacklistedNamesForBot(botId).map { it.lowercase() }.toSet()
        return list.filter { em ->
            val clean = cleanCharacterNameCandidate(em.characterName)
            clean.length >= 2 &&
            (bot == null || (!isMainOrUserCharacter(em.characterName, bot) && !isMainOrUserCharacter(clean, bot))) &&
            !blacklisted.contains(em.characterName.lowercase()) &&
            !blacklisted.contains(clean.lowercase())
        }
    }

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val keyCharListAdapter = moshi.adapter<List<KeyCharacter>>(
        Types.newParameterizedType(List::class.java, KeyCharacter::class.java)
    )
    private val backupAdapter = moshi.adapter(BackupSnapshot::class.java)

    private fun getBuildConfigKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY.trim()
            if (key == "MY_GEMINI_API_KEY") "" else key
        } catch (_: Throwable) {
            ""
        }
    }

    val allBots: Flow<List<BotEntity>> = botDao.getAllBots()
    val userSettingsFlow: Flow<UserSettingsEntity?> = settingsDao.getUserSettingsFlow()

    fun getBotFlow(id: String): Flow<BotEntity?> = botDao.getBotByIdFlow(id)
    fun getMessagesFlow(botId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForBot(botId)

    suspend fun getBot(id: String): BotEntity? = botDao.getBotById(id)

    fun resolveOpeningText(bot: BotEntity): String =
        bot.openingMessage.ifBlank {
            if (bot.mode == "universe") {
                "*Sahne başlar. Çevre sakin ve atmosferik bir havaya bürünmüştür.*\n\n\"Hikayemize nereden başlamak istersin?\""
            } else {
                "Merhaba! Seni seve seve dinliyorum, ne hakkında konuşmak istersin?"
            }
        }

    // ── Custom Provider wrappers ───────────────────────────────────────────────
    fun getCustomProvidersFlow() = db.customProviderDao().getAllProvidersFlow()

    suspend fun insertCustomProvider(entity: com.example.data.local.CustomProviderEntity): Long =
        db.customProviderDao().insertProvider(entity)

    suspend fun getCustomProviderById(id: Long): com.example.data.local.CustomProviderEntity? =
        db.customProviderDao().getProviderById(id)

    suspend fun updateCustomProvider(entity: com.example.data.local.CustomProviderEntity) =
        db.customProviderDao().updateProvider(entity)

    suspend fun deleteCustomProvider(id: Long) =
        db.customProviderDao().deleteProvider(id)

    // ── Character Emotion wrappers ────────────────────────────────────────────
    suspend fun getEmotionForCharacter(botId: String, characterName: String): CharacterEmotionEntity? =
        db.characterEmotionDao().getEmotionForCharacter(botId, characterName)

    suspend fun insertOrUpdateCharacterEmotion(entity: CharacterEmotionEntity) =
        db.characterEmotionDao().insertOrUpdate(entity)

    suspend fun saveBot(bot: BotEntity) {
        val previousBot = try { botDao.getBotById(bot.id) } catch (_: Exception) { null }
        botDao.insertOrUpdate(bot)
        if (previousBot != null && previousBot.chatBgUrl.isNotBlank() && previousBot.chatBgUrl != bot.chatBgUrl) {
            context?.let { ctx ->
                com.example.util.ImageStorageManager.deleteLocalBackgroundIfOwned(ctx, previousBot.chatBgUrl)
            }
        }
        try {
            extractAndSaveRoutinesFromBotPersonality(bot.id, bot.aiPersonality, bot.scenario)
        } catch (_: Exception) {}
        try {
            val keyChars = parseKeyCharacters(bot.keyCharactersJson)
            for (kc in keyChars) {
                if (!kc.id.startsWith("auto_")) {
                    ensureKeyCharacterMirroredAsCastMember(bot.id, kc)
                }
            }
        } catch (_: Exception) {}
        autoBackupToStorage()
    }

    suspend fun getUnifiedCastMembers(botId: String): List<CastMemberEntity> {
        val bot = botDao.getBotById(botId)
        if (bot != null) {
            val keyChars = parseKeyCharacters(bot.keyCharactersJson)
            for (kc in keyChars) {
                if (!kc.id.startsWith("auto_")) {
                    ensureKeyCharacterMirroredAsCastMember(botId, kc)
                }
            }
        }
        return castMemberDao.getCastMembersForBot(botId)
    }

    suspend fun cloneBot(sourceBotId: String): BotEntity? {
        val original = botDao.getBotById(sourceBotId) ?: return null
        val newBotId = UUID.randomUUID().toString()

        // 1. Filter manually added characters from keyCharactersJson
        // (auto-detected ones were tagged with "auto_" or added into keyCharacters with auto_ prefix)
        val allKeyChars = parseKeyCharacters(original.keyCharactersJson)
        val manualKeyChars = allKeyChars.filterNot { it.id.startsWith("auto_") }
        val newKeyCharactersJson = serializeKeyCharacters(manualKeyChars)

        // 2. Stored LLM/user-edited baseline emotion preserved (falls back to keyword heuristic if none stored)
        val calculatedBaseline = getStoredBaselineResult(original).baselineEmotionState.toJson()

        // 3. New Bot entity - fully detached, fresh counters, preserving explicit story calendar date only when declared
        val birthAge = if (original.initialAge > 0) original.initialAge else original.currentAge.coerceAtLeast(1)
        val clonedCalendarDate = if (original.hasExplicitStoryDate) original.storyCalendarDate else ""
        val clonedBot = original.copy(
            id = newBotId,
            aiName = original.aiName,
            keyCharactersJson = newKeyCharactersJson,
            pinnedMemory = "",
            storyNotes = "",
            memoryNotes = "",
            totalPromptTokens = 0L,
            totalCandidateTokens = 0L,
            needsSummarization = false,
            emotionState = calculatedBaseline,
            previousEmotionState = calculatedBaseline,
            storyCalendarDate = clonedCalendarDate,
            storyDayCounter = 1L,
            currentAge = birthAge,
            lastMessageTimestamp = System.currentTimeMillis(),
            lastTimeMismatchCorrectionNote = "",
            updatedAt = System.currentTimeMillis()
        )

        db.withTransaction {
            // Insert cloned bot
            botDao.insertOrUpdate(clonedBot)

            // Copy only manually added CastMemberEntity items (isAutoAdded == false), resetting counters
            try {
                val existingCast = castMemberDao.getCastMembersForBot(sourceBotId)
                val manualCast = existingCast.filter { !it.isAutoAdded }
                for (cm in manualCast) {
                    castMemberDao.insertCastMember(
                        cm.copy(
                            id = 0L, // Auto-generate new primary key
                            botId = newBotId,
                            affectionScore = 50,
                            relationshipState = "Tanıdık",
                            firstAppearedAt = System.currentTimeMillis(),
                            appearanceCount = 1,
                            consecutivePositiveCount = 0,
                            dailyAffectionGain = 0,
                            lastGainResetDate = "",
                            peakAffectionScore = 50,
                            recoveryLockUntilMessageCount = 0,
                            currentAge = if (cm.initialAge > 0) cm.initialAge else cm.currentAge
                        )
                    )
                }
            } catch (_: Exception) {}

            // Copy routine definitions but with lastMentionedStoryDate reset to ""
            try {
                val existingRoutines = routineDao.getRoutinesForBot(sourceBotId)
                if (existingRoutines.isNotEmpty()) {
                    val clonedRoutines = existingRoutines.map { routine ->
                        routine.copy(
                            id = 0L, // Auto-generate new primary key
                            botId = newBotId,
                            lastMentionedStoryDate = "" // Reset last mentioned date
                        )
                    }
                    routineDao.insertRoutines(clonedRoutines)
                } else {
                    extractAndSaveRoutinesFromBotPersonality(newBotId, clonedBot.aiPersonality, clonedBot.scenario)
                }
            } catch (_: Exception) {}

            // Copy sensitive trigger definitions with recentTriggerCount reset to 0
            try {
                val existingTriggers = sensitiveTriggerDao.getTriggersForBot(sourceBotId)
                for (trigger in existingTriggers) {
                    sensitiveTriggerDao.insertOrUpdate(
                        trigger.copy(
                            id = 0L,
                            botId = newBotId,
                            recentTriggerCount = 0
                        )
                    )
                }
            } catch (_: Exception) {}

            // Create initial opening message
            val openingText = resolveOpeningText(clonedBot)

            val openingMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                botId = newBotId,
                role = "assistant",
                text = openingText,
                timestamp = System.currentTimeMillis()
            )
            messageDao.insertMessage(openingMsg)
        }

        autoBackupToStorage()
        return clonedBot
    }

    suspend fun deleteBot(id: String) {
        val existingBot = try { botDao.getBotById(id) } catch (_: Exception) { null }
        db.withTransaction {
            messageDao.deleteMessagesForBot(id)
            emotionDao.deleteEmotionsForBot(id)
            fragmentDao.deleteFragmentsForBot(id)
            botDao.deleteBotById(id)
        }
        context?.let { ctx ->
            com.example.util.ImageStorageManager.deleteLocalBackgroundIfOwned(ctx, existingBot?.chatBgUrl)
        }
        autoBackupToStorage()
    }

    suspend fun resetBotMemoryAndEmotion(botId: String) {
        db.withTransaction {
            emotionDao.deleteEmotionsForBot(botId)
            fragmentDao.deleteFragmentsForBot(botId)
            val bot = botDao.getBotById(botId) ?: return@withTransaction
            val calculatedBaseline = EmotionState.calculateBaselineEmotionState(
                aiName = bot.aiName,
                personality = bot.aiPersonality,
                scenario = bot.scenario,
                userCharName = bot.userCharName,
                userCharDesc = bot.userCharDesc
            ).toJson()

            val updatedBot = bot.copy(
                emotionState = calculatedBaseline,
                previousEmotionState = calculatedBaseline,
                storyNotes = "",
                memoryNotes = "",
                pinnedMemory = "",
                needsSummarization = false,
                updatedAt = System.currentTimeMillis()
            )
            botDao.insertOrUpdate(updatedBot)
        }
    }

    suspend fun saveMessage(msg: MessageEntity) {
        if (msg.text.isBlank()) return
        messageDao.insertMessage(msg)
        autoBackupToStorage()
    }

    suspend fun deleteEmptyMessages() {
        messageDao.deleteEmptyMessages()
    }

    suspend fun deleteMessage(id: String) {
        messageDao.deleteMessageById(id)
        autoBackupToStorage()
    }

    suspend fun checkAndSaveMilestone(botId: String, affection: Int, trust: Int): com.example.data.local.MilestoneEntity? {
        val milestoneDao = db.milestoneDao()
        val reached = milestoneDao.getReachedMilestoneTypes(botId).toSet()
        for (def in com.example.data.local.MilestoneDefinitions.all) {
            if (def.type in reached) continue
            val value = if (def.field == "affection") affection else trust
            if (value >= def.threshold) {
                val milestone = com.example.data.local.MilestoneEntity(
                    id = "${botId}_${def.type}",
                    botId = botId,
                    type = def.type,
                    title = def.title,
                    emoji = def.emoji,
                    affectionAtTime = affection,
                    trustAtTime = trust
                )
                milestoneDao.insertMilestone(milestone)
                return milestone
            }
        }
        return null
    }

    suspend fun searchMessages(botId: String, query: String): List<com.example.data.local.MessageEntity> {
        return messageDao.searchMessages(botId, query)
    }

    suspend fun toggleMessageStar(msgId: String) {
        val msgs = messageDao.getAllMessagesList()
        val msg = msgs.firstOrNull { it.id == msgId } ?: return
        messageDao.setMessageStarred(msgId, !msg.isStarred)
    }

    suspend fun setMessageReaction(msgId: String, reaction: String) {
        messageDao.setMessageReaction(msgId, reaction)
    }

    suspend fun getStarredMessages(botId: String): List<com.example.data.local.MessageEntity> {
        return messageDao.getStarredMessages(botId)
    }

    suspend fun resetMessagesForBot(botId: String) {
        messageDao.deleteMessagesForBot(botId)
        autoBackupToStorage()
    }

    suspend fun rewindToMessage(botId: String, targetMessageId: String): Boolean {
        val rewound = db.withTransaction {
            val allMsgs = messageDao.getMessagesForBotList(botId)
            val targetIdx = allMsgs.indexOfFirst { it.id == targetMessageId }
            if (targetIdx == -1) return@withTransaction false

            val targetMsg = allMsgs[targetIdx]
            val targetTimestamp = targetMsg.timestamp
            val targetMsgCount = targetIdx + 1

            // 1. Delete all messages after targetIdx
            for (i in (targetIdx + 1) until allMsgs.size) {
                messageDao.deleteMessageById(allMsgs[i].id)
            }

            // 2. Delete emotion history records created after target message count or timestamp
            emotionHistoryDao.deleteHistoryAfterMessageIndex(botId, targetMsgCount)
            emotionHistoryDao.deleteHistoryAfterTimestamp(botId, targetTimestamp)

            // 3. Delete self-check logs and prompt violations after target point
            selfCheckFailureLogDao.deleteLogsAfterMessageIndexOrTimestamp(botId, targetMsgCount, targetTimestamp)
            promptViolationLogDao.deleteLogsAfterTimestamp(botId, targetTimestamp)

            // 4. Delete memory fragments, events, facts, checkpoints, affection events, milestones, relations & archives after target point
            fragmentDao.deleteFragmentsAfterTimestamp(botId, targetTimestamp)
            memoryEventDao.deleteEventsAfterTimestamp(botId, targetTimestamp)
            memoryEventDao.clearOrphanedSupersededBy(botId)
            memoryFactDao.deleteFactsAfterTimestamp(botId, targetTimestamp)
            memoryFactDao.clearOrphanedSupersededBy(botId)
            memoryCheckpointDao.deleteCheckpointsAfterMessageIndexOrTimestamp(botId, targetMsgCount, targetTimestamp)
            affectionEventDao.deleteAffectionEventsAfterTimestamp(botId, targetTimestamp)
            db.milestoneDao().deleteMilestonesAfterTimestamp(botId, targetTimestamp)

            val existingRelations = entityRelationDao.getRelationsForBot(botId)
            for (rel in existingRelations) {
                if (rel.createdAtMessageIndex > targetMsgCount) {
                    entityRelationDao.deleteRelation(rel.id)
                }
            }
            try {
                db.openHelper.writableDatabase.execSQL(
                    "DELETE FROM archived_memories WHERE botId = ? AND timestamp > ?",
                    arrayOf<Any>(botId, targetTimestamp)
                )
            } catch (_: Exception) {}

            // 5. Delete auto-added cast members that first appeared after targetTimestamp
            castMemberDao.deleteAutoAddedCastMembersAfterTimestamp(botId, targetTimestamp)

            // 6. Restore BotEntity (Main character) emotion state, counters & calendar date/day
            val bot = botDao.getBotById(botId) ?: return@withTransaction true
            val lastMainHistory = emotionHistoryDao.getLatestMainHistoryBeforeOrAt(botId, targetMsgCount)

            val remainingEvents = memoryEventDao.getActiveEventsBeforeOrAt(botId, targetTimestamp)
            val latestEvent = remainingEvents.firstOrNull()

            val restoredCalDate = if (latestEvent != null && latestEvent.storyCalendarDateStr.isNotBlank()) {
                latestEvent.storyCalendarDateStr
            } else if (targetMsgCount <= 1) {
                if (bot.hasExplicitStoryDate) bot.storyCalendarDate else ""
            } else {
                bot.storyCalendarDate
            }

            val restoredDayCounter = if (latestEvent != null && latestEvent.storyDayIndex > 0) {
                latestEvent.storyDayIndex
            } else if (targetMsgCount <= 1) {
                1L
            } else {
                bot.storyDayCounter
            }

            val (_, _, restoredAge) = TimePerceptionEngine.advanceCalendarDateAndCheckAge(
                currentCalendarDateStr = restoredCalDate,
                currentDayCounter = restoredDayCounter,
                elapsedDaysToAdd = 0,
                birthDateStr = bot.birthDate,
                initialAge = bot.initialAge
            )

            if (lastMainHistory != null) {
                val restoredEmotionJson = lastMainHistory.fullVectorJson
                botDao.insertOrUpdate(
                    bot.copy(
                        emotionState = restoredEmotionJson,
                        previousEmotionState = restoredEmotionJson,
                        storyCalendarDate = restoredCalDate,
                        storyDayCounter = restoredDayCounter,
                        currentAge = restoredAge,
                        lastMessageTimestamp = targetTimestamp,
                        updatedAt = targetTimestamp
                    )
                )
            } else {
                val baseline = getStoredBaselineResult(bot).baselineEmotionState.toJson()
                botDao.insertOrUpdate(
                    bot.copy(
                        emotionState = baseline,
                        previousEmotionState = baseline,
                        storyCalendarDate = restoredCalDate,
                        storyDayCounter = restoredDayCounter,
                        currentAge = restoredAge,
                        lastMessageTimestamp = targetTimestamp,
                        updatedAt = targetTimestamp
                    )
                )
            }

            // Reset routines whose lastMentionedStoryDate is newer than restoredCalDate
            try {
                val existingRoutines = routineDao.getRoutinesForBot(botId)
                for (routine in existingRoutines) {
                    if (routine.lastMentionedStoryDate.isNotBlank() &&
                        (restoredCalDate.isBlank() || routine.lastMentionedStoryDate > restoredCalDate)
                    ) {
                        routineDao.updateRoutine(routine.copy(lastMentionedStoryDate = ""))
                    }
                }
            } catch (_: Exception) {}

            // 7. Restore remaining CastMemberEntity counters & CharacterEmotionEntity
            val remainingCast = castMemberDao.getCastMembersForBot(botId)
            for (member in remainingCast) {
                val charHistory = emotionHistoryDao.getLatestCharacterHistoryBeforeOrAt(botId, member.name, targetMsgCount)
                if (charHistory != null) {
                    val restoredCharState = EmotionState.fromJson(charHistory.fullVectorJson)
                    castMemberDao.updateCastMember(
                        member.copy(
                            affectionScore = restoredCharState.affection,
                            consecutivePositiveCount = restoredCharState.consecutivePositiveCount,
                            dailyAffectionGain = restoredCharState.dailyAffectionGain,
                            lastGainResetDate = restoredCharState.lastGainResetDate,
                            peakAffectionScore = restoredCharState.peakAffectionScore,
                            recoveryLockUntilMessageCount = restoredCharState.recoveryLockUntilMessageCount
                        )
                    )
                    emotionDao.insertOrUpdate(
                        CharacterEmotionEntity(
                            botId = botId,
                            characterName = member.name,
                            emotionState = charHistory.fullVectorJson
                        )
                    )
                } else {
                    castMemberDao.updateCastMember(
                        member.copy(
                            affectionScore = 50,
                            consecutivePositiveCount = 0,
                            dailyAffectionGain = 0,
                            peakAffectionScore = 50,
                            recoveryLockUntilMessageCount = 0
                        )
                    )
                    emotionDao.deleteByCharacterName(botId, member.name)
                }
            }

            updateCastMembersAgeForBot(botId, restoredCalDate)
            true
        }

        if (rewound) {
            // Clear in-memory summarization, regenerate & RAG vector caches so next turn starts cleanly
            lastSummarizedMessageCount.remove(botId)
            resetRegenerateCount(botId)
            managers.memoryRetrievalManager.invalidateBotCache(botId)
            autoBackupToStorage()
        }
        return rewound
    }

    suspend fun updateUserSettings(settings: UserSettingsEntity) {
        settingsDao.insertOrUpdate(settings)
        autoBackupToStorage()
    }

    fun autoBackupToStorage() {
        backupDebounceJob?.cancel()
        backupDebounceJob = repositoryScope.launch {
            delay(BACKUP_DEBOUNCE_MS)
            doActualBackup()
        }
    }

    suspend fun forceBackupNow() = withContext(Dispatchers.IO) {
        backupDebounceJob?.cancel()
        doActualBackup()
    }

    private suspend fun buildBackupSnapshot(includeApiKeys: Boolean): BackupSnapshot {
        val bots = botDao.getAllBotsList()
        val allMessages = mutableListOf<MessageEntity>()
        val allFacts = mutableListOf<com.example.data.local.MemoryFactEntity>()
        val allEvents = mutableListOf<com.example.data.local.MemoryEventEntity>()
        val allCheckpoints = mutableListOf<com.example.data.local.MemoryCheckpointEntity>()
        val allCast = mutableListOf<com.example.data.local.CastMemberEntity>()
        val allEmotions = mutableListOf<com.example.data.local.CharacterEmotionEntity>()
        val allTriggers = mutableListOf<com.example.data.local.SensitiveTriggerEntity>()
        val allRoutines = mutableListOf<com.example.data.local.RoutineEntity>()
        val allRelations = mutableListOf<com.example.data.local.EntityRelationEntity>()
        val allMilestones = mutableListOf<com.example.data.local.MilestoneEntity>()
        val milestoneDao = db.milestoneDao()

        for (b in bots) {
            allMessages.addAll(messageDao.getMessagesForBotList(b.id))
            try { allFacts.addAll(memoryFactDao.getActiveFacts(b.id)) } catch (_: Exception) {}
            try { allEvents.addAll(memoryEventDao.getActiveEvents(b.id)) } catch (_: Exception) {}
            try { allCheckpoints.addAll(memoryCheckpointDao.getCheckpointsForBot(b.id)) } catch (_: Exception) {}
            try { allCast.addAll(castMemberDao.getCastMembersForBot(b.id)) } catch (_: Exception) {}
            try { allEmotions.addAll(emotionDao.getEmotionsForBot(b.id)) } catch (_: Exception) {}
            try { allTriggers.addAll(sensitiveTriggerDao.getTriggersForBot(b.id)) } catch (_: Exception) {}
            try { allRoutines.addAll(routineDao.getRoutinesForBot(b.id)) } catch (_: Exception) {}
            try { allRelations.addAll(entityRelationDao.getRelationsForBot(b.id)) } catch (_: Exception) {}
            try { allMilestones.addAll(milestoneDao.getMilestonesForBot(b.id)) } catch (_: Exception) {}
        }

        val rawSettings = settingsDao.getUserSettings() ?: UserSettingsEntity()
        val exportedSettings = if (includeApiKeys) rawSettings else rawSettings.sanitizedForBackup()

        return BackupSnapshot(
            version = 2,
            exportedAt = System.currentTimeMillis(),
            bots = bots,
            messages = allMessages,
            settings = exportedSettings,
            memoryFacts = allFacts,
            memoryEvents = allEvents,
            memoryCheckpoints = allCheckpoints,
            castMembers = allCast,
            characterEmotions = allEmotions,
            sensitiveTriggers = allTriggers,
            routines = allRoutines,
            entityRelations = allRelations,
            milestones = allMilestones
        )
    }

    private suspend fun doActualBackup() = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext
        try {
            // Auto-backup ALWAYS strips API keys and writes atomically to internal filesDir only
            val snapshot = buildBackupSnapshot(includeApiKeys = false)
            val json = backupAdapter.toJson(snapshot)

            val targetFile = java.io.File(ctx.filesDir, "emochi_autobackup.json")
            val tmpFile = java.io.File(ctx.filesDir, "emochi_autobackup.json.tmp")
            java.io.FileOutputStream(tmpFile).use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
                out.flush()
                try { out.fd.sync() } catch (_: Exception) {}
            }
            if (!tmpFile.renameTo(targetFile)) {
                tmpFile.copyTo(targetFile, overwrite = true)
                tmpFile.delete()
            }

            // Security cleanup: delete any legacy plaintext backup file in external storage
            try {
                ctx.getExternalFilesDir(null)?.let { extDir ->
                    val legacyExternal = java.io.File(extDir, "emochi_autobackup.json")
                    if (legacyExternal.exists()) {
                        legacyExternal.delete()
                    }
                }
            } catch (_: Exception) {}
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun autoRestoreFromStorage(): Boolean = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext false
        try {
            val internalFile = java.io.File(ctx.filesDir, "emochi_autobackup.json")
            val legacyExternalFile = ctx.getExternalFilesDir(null)?.let {
                java.io.File(it, "emochi_autobackup.json")
            }
            val backupFile = when {
                internalFile.exists() && internalFile.length() > 0L -> internalFile
                legacyExternalFile != null && legacyExternalFile.exists() && legacyExternalFile.length() > 0L -> legacyExternalFile
                else -> null
            } ?: return@withContext false

            val json = backupFile.readText(Charsets.UTF_8)
            val summary = importDataSnapshot(jsonStr = json, overwriteExisting = false)
            if (backupFile == legacyExternalFile && summary.botsCount > 0) {
                // Migrate away from insecure external storage immediately
                doActualBackup()
            }
            return@withContext summary.botsCount > 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    suspend fun getMessageListForBot(botId: String): List<MessageEntity> {
        return messageDao.getMessagesForBotList(botId)
    }

    suspend fun initStarterBotsIfEmpty() = withContext(Dispatchers.IO) {
        var existingBots = botDao.getAllBotsList()
        if (existingBots.isEmpty()) {
            val restored = autoRestoreFromStorage()
            if (restored) {
                existingBots = botDao.getAllBotsList()
            }
        }
        val now = System.currentTimeMillis()

        // Remove starter_aiden_stories if it was previously added to starter scenarios
        if (existingBots.any { it.id == "starter_aiden_stories" }) {
            botDao.deleteBotById("starter_aiden_stories")
            messageDao.deleteMessagesForBot("starter_aiden_stories")
            existingBots = botDao.getAllBotsList()
        }

        val aylaOpening = "Selam Sencer! Nasılsın bakalım bugün? Kahvemi aldım, balkona geçtim de aklıma sen geldin. Günün nasıl geçti, anlat bakalım?"
        val aetheriaOpening = "Yağmur, Aetheria'nın neon tabelalarından süzülüp dar sokağın taşlarını mor ve mavi yansımalarla boyuyordu. Valeria, kapüşonunun altından sana doğru temkinli bir adım attı; yanında duran Kael'in eli kılıcının kabzasındaydı.\n\n\"Tam zamanında geldin Rider,\" dedi Valeria alçak ama otoriter bir sesle. \"Konsey muhafızları alt sektörü kapatmadan önce çipi görmem gerekiyor. Yanında mı?\""

        // Clean up any existing starter bots that had the Aiden/Emma reference messages injected
        for (b in existingBots) {
            if (b.id == "starter_ayla" && b.openingMessage.contains("Emma")) {
                val fixed = b.copy(openingMessage = aylaOpening)
                botDao.insertOrUpdate(fixed)
                val msgs = messageDao.getMessagesForBotList(b.id)
                if (msgs.any { it.id.contains("_ref_msg_") || it.text.contains("Emma") }) {
                    messageDao.deleteMessagesForBot(b.id)
                    messageDao.insertMessage(
                        MessageEntity(
                            id = UUID.randomUUID().toString(),
                            botId = b.id,
                            role = "assistant",
                            text = aylaOpening,
                            timestamp = now
                        )
                    )
                }
            } else if (b.id == "starter_aetheria" && b.openingMessage.contains("Emma")) {
                val fixed = b.copy(openingMessage = aetheriaOpening)
                botDao.insertOrUpdate(fixed)
                val msgs = messageDao.getMessagesForBotList(b.id)
                if (msgs.any { it.id.contains("_ref_msg_") || it.text.contains("Emma") }) {
                    messageDao.deleteMessagesForBot(b.id)
                    messageDao.insertMessage(
                        MessageEntity(
                            id = UUID.randomUUID().toString(),
                            botId = b.id,
                            role = "assistant",
                            text = aetheriaOpening,
                            timestamp = now
                        )
                    )
                }
            } else {
                val msgs = messageDao.getMessagesForBotList(b.id)
                if (msgs.size == 3 && msgs.all { it.id.contains("_ref_msg_") }) {
                    messageDao.deleteMessagesForBot(b.id)
                    messageDao.insertMessage(
                        MessageEntity(
                            id = UUID.randomUUID().toString(),
                            botId = b.id,
                            role = "assistant",
                            text = resolveOpeningText(b),
                            timestamp = now
                        )
                    )
                }
            }
        }

        if (existingBots.isEmpty()) {
            val aylaBaseline = EmotionState.calculateBaselineEmotionState(
                aiName = "Ayla",
                personality = "Sıcak, neşeli, empati yeteneği yüksek, konuşkan ve korumacı bir yakın arkadaş.",
                scenario = "Lise/üniversiteden beri en yakın dostun. Akşam saatlerinde kahveni yudumlarken sana mesaj atıyor.",
                userCharName = "Sencer",
                userCharDesc = "Ayla'nın en güvendiği yakın dostu."
            ).toJson()

            val starterBot1 = BotEntity(
                id = "starter_ayla",
                mode = "personal",
                aiName = "Ayla",
                aiPersonality = "Sıcak, neşeli, empati yeteneği yüksek, konuşkan ve korumacı bir yakın arkadaş. Zeki espriler yapmayı ve senin gününün nasıl geçtiğini dinlemeyi sever.",
                scenario = "Lise/üniversiteden beri en yakın dostun. Akşam saatlerinde kahveni yudumlarken sana mesaj atıyor.",
                universeName = "Kişisel Sohbet",
                keyCharactersJson = "[]",
                userCharName = "Sencer",
                userCharDesc = "Ayla'nın en güvendiği yakın dostu.",
                openingMessage = aylaOpening,
                writingStyle = "rp",
                intensity = "normal",
                customLength = "default",
                isNsfw = true,
                isPublic = true,
                isTemplate = true,
                emotionState = aylaBaseline,
                previousEmotionState = aylaBaseline,
                pinnedMemory = "Ayla her zaman içten ve samimidir. Sencer'e çok değer verir.",
                updatedAt = now
            )

            val aetheriaBaseline = EmotionState.calculateBaselineEmotionState(
                aiName = "Valeria",
                personality = "Gizemli, temkinli ve otoriter lonca lideri",
                scenario = "Neon ışıklarıyla aydınlatılmış Aetheria şehrinin yüksek kuleleri ve gölgeli alt sokaklarında tehlikeli bir lonca anlaşması yapılmak üzeredir.",
                userCharName = "Rider",
                userCharDesc = "Loncanın en yetenekli bilgi tüccarı."
            ).toJson()

            val aetheriaWorldAtmosphere = WorldAtmosphere(
                mood = "gergin ve kasvetli",
                intensity = 8,
                currentEvent = "Veri çipi teslimatı ve lonca muhafızlarının takibi",
                macroAtmosphere = "Siberpunk Aetheria'da şirket klanları ve yeraltı loncaları hakimdir. Şehirde gizli yürütülen her adım ölümcül yaptırımlara tabidir.",
                microAtmosphere = "Yağmurlu ve loş neon ışıklı dar bir sokak. Yağmurlukların yakası kaldırılmış, takip edilme riski yüksek."
            ).toJson()

            val starterBot2 = BotEntity(
                id = "starter_aetheria",
                mode = "universe",
                aiName = "Aetheria Yönetmeni",
                aiPersonality = "Atmosferik, gizemli ve sürükleyici bir fantezi-siberpunk dünyası anlatıcısı.",
                scenario = "Neon ışıklarıyla aydınlatılmış Aetheria şehrinin yüksek kuleleri ve gölgeli alt sokaklarında tehlikeli bir lonca anlaşması yapılmak üzeredir.",
                universeName = "Aetheria: Neon & Büyü",
                keyCharactersJson = """[{"id":"c1","name":"Valeria","desc":"Büyü teknolojisi uzmanı lonca lideri"},{"id":"c2","name":"Kael","desc":"Sessiz ve tehlikeli paralı asker"}]""",
                userCharName = "Rider",
                userCharDesc = "Loncanın en yetenekli bilgi tüccarı.",
                openingMessage = aetheriaOpening,
                writingStyle = "rp",
                intensity = "normal",
                customLength = "default",
                isNsfw = true,
                isPublic = true,
                isTemplate = true,
                emotionState = aetheriaBaseline,
                previousEmotionState = aetheriaBaseline,
                worldAtmosphere = aetheriaWorldAtmosphere,
                pinnedMemory = "Aetheria evreninde yüksek teknoloji ile kadim sihir iç içedir.",
                updatedAt = now - 1000
            )

            botDao.insertOrUpdate(starterBot1)
            botDao.insertOrUpdate(starterBot2)
            try {
                extractAndSaveRoutinesFromBotPersonality(starterBot1.id, starterBot1.aiPersonality, starterBot1.scenario)
                extractAndSaveRoutinesFromBotPersonality(starterBot2.id, starterBot2.aiPersonality, starterBot2.scenario)
            } catch (_: Exception) {}

            messageDao.insertMessage(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = starterBot1.id,
                    role = "assistant",
                    text = starterBot1.openingMessage,
                    timestamp = now
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = starterBot2.id,
                    role = "assistant",
                    text = starterBot2.openingMessage,
                    timestamp = now - 1000
                )
            )
            autoBackupToStorage()
        }
    }

    suspend fun getOrCreateSettings(): UserSettingsEntity {
        var settings = settingsDao.getUserSettings()
        if (settings == null) {
            settings = UserSettingsEntity(
                selectedModel = "gemini-2.0-flash",
                fallbackModel = "gemini-2.0-flash",
                geminiModel = "gemini-2.0-flash"
            )
            settingsDao.insertOrUpdate(settings)
        }
        return settings
    }

    private suspend fun recordTokenUsage(botId: String? = null, promptTokens: Long, candidateTokens: Long) {
        val cleanPrompt = promptTokens.coerceAtLeast(0L)
        val cleanCand = candidateTokens.coerceAtLeast(0L)
        val totalTokens = cleanPrompt + cleanCand

        android.util.Log.d("StateBlockTokenLog", "BotId: $botId | PromptTokens: $cleanPrompt | CandidateTokens: $cleanCand | TotalResponseTokens: $totalTokens")

        val current = getOrCreateSettings()
        val updated = current.copy(
            totalPromptTokens = (current.totalPromptTokens + cleanPrompt).coerceAtLeast(0L),
            totalCandidateTokens = (current.totalCandidateTokens + cleanCand).coerceAtLeast(0L)
        )
        settingsDao.insertOrUpdate(updated)

        if (!botId.isNullOrBlank()) {
            val bot = botDao.getBotById(botId)
            if (bot != null) {
                val updatedBot = bot.copy(
                    totalPromptTokens = (bot.totalPromptTokens + cleanPrompt).coerceAtLeast(0L),
                    totalCandidateTokens = (bot.totalCandidateTokens + cleanCand).coerceAtLeast(0L)
                )
                botDao.insertOrUpdate(updatedBot)
            }
        }
    }

    fun parseKeyCharacters(json: String): List<KeyCharacter> {
        return try {
            if (json.isBlank()) emptyList() else keyCharListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeKeyCharacters(chars: List<KeyCharacter>): String {
        return try {
            keyCharListAdapter.toJson(chars)
        } catch (e: Exception) {
            "[]"
        }
    }

    // --- RAG (Semantic Vector Embedding & Keyword Search Memory System) ---

    private val stopWords = setOf(
        "ve", "bir", "de", "da", "bu", "şu", "ile", "için", "en", "çok", "ama", "fakat", "gibi",
        "ben", "sen", "o", "biz", "siz", "onlar", "mi", "mı", "mu", "mü", "daha", "kadar", "her",
        "zaman", "sonra", "önce", "var", "yok", "ki", "ne", "nasıl", "neden", "niye", "şey", "yani",
        "the", "a", "an", "is", "are", "and", "or", "to", "in", "of", "for", "with", "on", "at", "by", "from"
    )


    // --- Vector Embedding Calculation & Feature Vector Fallback ---





    // --- Hybrid Ranking & Two-Tier Memory Search ---

    data class MemoryRankingWeights(
        val semanticWeight: Float = 0.45f,
        val keywordWeight: Float = 0.25f,
        val importanceWeight: Float = 0.20f,
        val recencyWeight: Float = 0.10f,
        val properNounKeywordWeight: Float = 0.40f,
        val properNounSemanticWeight: Float = 0.30f
    ) {
        companion object {
            @Volatile
            var activeConfig: MemoryRankingWeights = MemoryRankingWeights()

            // Auto-boost: proper-noun or date queries favour keyword matching using configurable weights
            fun forQuery(queryText: String, baseConfig: MemoryRankingWeights = activeConfig): MemoryRankingWeights {
                val hasProperNoun = queryText.any { it.isUpperCase() } &&
                        queryText.split(Regex("\\s+")).any { word ->
                            val cleanWord = word.trim(',', '.', '?', '!', ':', ';', '"', '\'', '(', ')')
                            cleanWord.length >= 3 && cleanWord.first().isUpperCase()
                        }
                val hasDate = Regex("""(?i)\d{1,4}[-/\.]\d{1,2}|pazartesi|salı|çarşamba|perşembe|cuma|cumartesi|pazar|ocak|şubat|mart|nisan|mayıs|haziran|temmuz|ağustos|eylül|ekim|kasım|aralık|\d{4}""").containsMatchIn(queryText)
                return if (hasProperNoun || hasDate) {
                    baseConfig.copy(
                        semanticWeight = baseConfig.properNounSemanticWeight,
                        keywordWeight = baseConfig.properNounKeywordWeight
                    )
                } else {
                    baseConfig
                }
            }
        }
    }



    // --- Active Memory Recording & Function Calling ---


    /**
     * Evaluates a conversation turn for active memory operations (save_memory / update_memory / delete_memory)
     * using the provider's resolved utility model (resolveUtilityModel), supporting both native function calling
     * and text-fallback on Custom Providers.
     */




    // Returns the matching existing record (event/fact) if near-duplicate found, else null.
    // Caller can then merge instead of skip.


    // --- Two-Stage Retrieval & HyDE Query Rewriting ---

    data class TwoStageRetrievalResult(
        val retrievedEvents: List<MemoryEventEntity>,
        val retrievedFacts: List<MemoryFactEntity>,
        val topSimilarityScore: Float,
        val retrievalStageUsed: String
    )




    // --- 10/10 Automatic Neural Vault CRUD & Multi-POV Living Memory Engine ---

    data class MemoryCommitSummary(
        val savedFactsCount: Int = 0,
        val updatedFactsCount: Int = 0,
        val deletedFactsCount: Int = 0,
        val savedMemoryPov: String? = null,
        val savedMemoryTitle: String? = null
    ) {
        val hasAnyChange: Boolean
            get() = savedFactsCount > 0 || updatedFactsCount > 0 || deletedFactsCount > 0 || !savedMemoryTitle.isNullOrBlank()

        fun mergeWith(other: MemoryCommitSummary): MemoryCommitSummary = MemoryCommitSummary(
            savedFactsCount = savedFactsCount + other.savedFactsCount,
            updatedFactsCount = updatedFactsCount + other.updatedFactsCount,
            deletedFactsCount = deletedFactsCount + other.deletedFactsCount,
            savedMemoryPov = other.savedMemoryPov ?: savedMemoryPov,
            savedMemoryTitle = other.savedMemoryTitle ?: savedMemoryTitle
        )
    }

    val lastRagRouteMode: kotlinx.coroutines.flow.StateFlow<MemoryEngine.RagRouteMode>
        get() = managers.memoryRetrievalManager.lastRouteMode

    fun getLastRagRouteModeForBot(botId: String): MemoryEngine.RagRouteMode =
        managers.memoryRetrievalManager.getLastRouteModeForBot(botId)

    fun invalidateMemoryCache(botId: String) {
        managers.memoryRetrievalManager.invalidateBotCache(botId)
    }

    suspend fun getArchivedMemories(botId: String): List<ArchivedMemoryEntity> = withContext(Dispatchers.IO) {
        try {
            archivedMemoryDao.getArchivesForBot(botId).sortedByDescending { it.timestamp }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun toggleMemoryFactLock(fact: MemoryFactEntity) = withContext(Dispatchers.IO) {
        val willLock = !fact.userCorrected
        val updatedConfidence = if (willLock) {
            MemoryLifecycleCodec.withPromotedCore(fact.confidence)
        } else {
            fact.confidence
        }
        managers.memoryRetrievalManager.updateMemoryFact(
            fact = fact.copy(
                userCorrected = willLock,
                confidence = updatedConfidence
            ),
            preserveLockFlag = true
        )
    }

    /**
     * Natural Forgetting Engine (MemoryLifecycleReaper — Section 3.1):
     * Pure local (<2 ms, 0 AI tokens) reaper that soft-archives expired EPHEMERAL and ARC memories
     * into ArchivedMemoryEntity(archivedReason = "natural_decay_ttl") and immediately evicts them
     * from MemoryVectorCache. Never touches ★ userCorrected or CORE entries.
     */
    suspend fun reapExpiredMemories(
        botId: String,
        currentStoryDay: Long,
        currentMessageCount: Int = 0
    ): Int = withContext(Dispatchers.IO) {
        val safeDay = currentStoryDay.coerceAtLeast(1L)
        val activeFacts = try { memoryFactDao.getActiveFacts(botId) } catch (_: Exception) { emptyList() }
        if (activeFacts.isEmpty()) return@withContext 0

        var reapedCount = 0
        for (fact in activeFacts) {
            // ★ user-locked and CORE facts are NEVER automatically forgotten
            if (fact.userCorrected) continue
            val meta = MemoryLifecycleCodec.parse(fact.confidence)
            if (meta.tier == MemoryTier.CORE) continue

            val shouldForget = when (meta.tier) {
                MemoryTier.DISCARD -> true
                MemoryTier.EPHEMERAL -> {
                    val dayExpired = meta.expiresStoryDay > 0L && safeDay > meta.expiresStoryDay
                    val msgExpired = meta.expiresMessageIndex > 0 && currentMessageCount > 0 && currentMessageCount > meta.expiresMessageIndex
                    dayExpired || msgExpired
                }
                MemoryTier.ARC -> {
                    val dayExpired = meta.expiresStoryDay > 0L && safeDay > meta.expiresStoryDay
                    val lastTouchedDay = maxOf(meta.createdStoryDay, meta.lastAccessStoryDay)
                    val staleForTenDays = (safeDay - lastTouchedDay) >= 10L
                    dayExpired && staleForTenDays
                }
                MemoryTier.CORE -> false
            }

            if (shouldForget) {
                memoryFactDao.updateFact(fact.copy(supersededBy = -1L))
                try {
                    archivedMemoryDao.insertArchive(
                        ArchivedMemoryEntity(
                            botId = botId,
                            originalMemoryId = fact.id,
                            memoryType = "fact",
                            content = "${fact.subject} | ${fact.key} | ${fact.value}",
                            archivedReason = "natural_decay_ttl"
                        )
                    )
                } catch (_: Exception) {}
                reapedCount++
            }
        }

        if (reapedCount > 0) {
            invalidateMemoryCache(botId)
        }
        reapedCount
    }

    /**
     * Human-Brain Habit Reinforcement (Section 3.2):
     * Records RAG recall hits on EPHEMERAL/ARC facts. Once an EPHEMERAL memory is referenced
     * >= promoteThreshold (default 2) times before expiry, it automatically promotes to permanent CORE!
     * Returns the number of memories promoted to CORE in this pass.
     */
    suspend fun reinforceRecalledFacts(
        botId: String,
        recalledFacts: List<MemoryFactEntity>,
        currentStoryDay: Long,
        promoteThreshold: Int = 2
    ): Int = withContext(Dispatchers.IO) {
        if (recalledFacts.isEmpty()) return@withContext 0
        val safeDay = currentStoryDay.coerceAtLeast(1L)
        var promotedCount = 0
        var anyUpdated = false

        for (fact in recalledFacts) {
            if (fact.userCorrected) continue
            val meta = MemoryLifecycleCodec.parse(fact.confidence)
            if (meta.tier == MemoryTier.CORE) continue

            val (updatedConfidence, wasPromoted) = MemoryLifecycleCodec.withAccessHit(
                rawConfidence = fact.confidence,
                currentStoryDay = safeDay,
                promoteThreshold = promoteThreshold
            )
            if (updatedConfidence != fact.confidence) {
                memoryFactDao.updateFact(
                    fact.copy(
                        confidence = updatedConfidence,
                        lastConfirmedAt = System.currentTimeMillis()
                    )
                )
                anyUpdated = true
                if (wasPromoted) promotedCount++
            }
        }

        if (anyUpdated) {
            invalidateMemoryCache(botId)
        }
        promotedCount
    }

    /**
     * Tolerantly resolves a target Neural Vault fact from an ID ("12", "#12", "F12") or,
     * if a weaker model emitted text instead of an ID, via semantic + lexical match (>= 0.72f) or substring match.
     */
    private suspend fun resolveFactTarget(botId: String, rawTarget: String): MemoryFactEntity? {
        val trimmed = rawTarget.trim()
        if (trimmed.isBlank()) return null

        val parsedId = com.example.data.api.MemoryToolRegistry.parseMemoryId(trimmed)
        if (parsedId != null) {
            val byId = try { memoryFactDao.getFactById(parsedId) } catch (_: Exception) { null }
            if (byId != null && byId.botId == botId && byId.supersededBy == null) {
                return byId
            }
        }

        val activeFacts = try { memoryFactDao.getActiveFacts(botId) } catch (_: Exception) { emptyList() }
        if (activeFacts.isEmpty()) return null

        val normTarget = TurkishTextAnalyzer.normalize(trimmed)
        val targetVec = MemoryEngine.computeTurkishFeatureEmbedding(trimmed)
        var bestFact: MemoryFactEntity? = null
        var bestScore = 0f

        for (fact in activeFacts) {
            val fullText = "${fact.subject} ${fact.key} ${fact.value}"
            val normFull = TurkishTextAnalyzer.normalize(fullText)
            val normVal = TurkishTextAnalyzer.normalize(fact.value)
            if (normVal.contains(normTarget) || normFull.contains(normTarget) ||
                (normVal.length >= 6 && normTarget.contains(normVal))
            ) {
                return fact
            }
            val factVec = jsonToFloatArray(fact.embeddingJson).takeIf { it.size == targetVec.size }
                ?: MemoryEngine.computeTurkishFeatureEmbedding(fullText)
            val sem = cosineSimilarity(targetVec, factVec)
            val overlap = TurkishTextAnalyzer.stemOverlapRatio(trimmed, fullText)
            val combined = maxOf(sem, overlap)
            if (combined > bestScore) {
                bestScore = combined
                bestFact = fact
            }
        }
        return if (bestScore >= 0.72f) bestFact else null
    }

    /**
     * Inserts or supersedes a structured fact in the Neural Vault with instant 256-dim Turkish embedding,
     * subject+key / semantic deduplication, soft-archive history, ★ user-lock protection, and cache invalidation.
     */
    suspend fun upsertNeuralVaultFact(
        botId: String,
        subject: String,
        key: String,
        value: String,
        confidence: String = "certain",
        isBotSaved: Boolean = true,
        apiKey: String? = null,
        explicitUserOverride: Boolean = false,
        tier: MemoryTier = MemoryTier.CORE,
        ttlDays: Int = 0,
        importance: Int = 6,
        storyDay: Long? = null,
        currentMessageCount: Int = 0
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanSubject = subject.trim().take(48).ifBlank { "kullanıcı" }
        val cleanKey = key.trim().take(48).ifBlank { MemoryEngine.inferFactKey(value) }
        val cleanValue = value.trim().take(320)
        if (cleanValue.length < 4) return@withContext false

        // Code-side Cognitive Selectivity safety gate (Section 2):
        // Never persist DISCARD / importance <= 2 / trivial physical choreography noise!
        if (tier == MemoryTier.DISCARD || importance <= 2) return@withContext false
        if (!explicitUserOverride && MemoryEngine.isTrivialNoiseFact(cleanKey, cleanValue, importance)) {
            return@withContext false
        }

        val now = System.currentTimeMillis()
        val effectiveStoryDay = (storyDay ?: try { botDao.getBotById(botId)?.storyDayCounter ?: 1L } catch (_: Exception) { 1L }).coerceAtLeast(1L)
        val baseConfidence = MemoryLifecycleCodec.parseConfidenceBase(confidence)
        val encodedConfidence = when {
            confidence.contains("|t:") -> confidence
            tier == MemoryTier.EPHEMERAL -> {
                val clampedTtl = (if (ttlDays > 0) ttlDays else 3).coerceIn(2, 4)
                MemoryLifecycleCodec.encode(
                    MemoryLifecycleMeta(
                        baseConfidence = baseConfidence,
                        tier = MemoryTier.EPHEMERAL,
                        createdStoryDay = effectiveStoryDay,
                        expiresStoryDay = effectiveStoryDay + clampedTtl,
                        expiresMessageIndex = if (currentMessageCount > 0) currentMessageCount + 35 else 0,
                        accessCount = 0,
                        lastAccessStoryDay = effectiveStoryDay
                    )
                )
            }
            tier == MemoryTier.ARC -> {
                val clampedTtl = (if (ttlDays > 0) ttlDays else 12).coerceIn(10, 14)
                MemoryLifecycleCodec.encode(
                    MemoryLifecycleMeta(
                        baseConfidence = baseConfidence,
                        tier = MemoryTier.ARC,
                        createdStoryDay = effectiveStoryDay,
                        expiresStoryDay = effectiveStoryDay + clampedTtl,
                        expiresMessageIndex = 0,
                        accessCount = 0,
                        lastAccessStoryDay = effectiveStoryDay
                    )
                )
            }
            else -> baseConfidence
        }

        val combinedText = "$cleanSubject $cleanKey $cleanValue"
        val embeddingVec = MemoryEngine.computeTurkishFeatureEmbedding(combinedText)
        val embeddingJson = floatArrayToJson(embeddingVec)

        val normSubject = TurkishTextAnalyzer.normalize(cleanSubject)
        val normKey = TurkishTextAnalyzer.normalize(cleanKey)
        val normValue = TurkishTextAnalyzer.normalize(cleanValue)

        val activeFacts = try { memoryFactDao.getActiveFacts(botId) } catch (_: Exception) { emptyList() }
        val sameSubjectFacts = activeFacts.filter {
            val s = TurkishTextAnalyzer.normalize(it.subject)
            s == normSubject || (s.length >= 3 && normSubject.length >= 3 && (s.contains(normSubject) || normSubject.contains(s)))
        }

        val toSupersede = mutableListOf<MemoryFactEntity>()
        for (existing in sameSubjectFacts) {
            val exKey = TurkishTextAnalyzer.normalize(existing.key)
            val exVal = TurkishTextAnalyzer.normalize(existing.value)
            val exVec = jsonToFloatArray(existing.embeddingJson).takeIf { it.size == embeddingVec.size }
                ?: MemoryEngine.computeTurkishFeatureEmbedding("${existing.subject} ${existing.key} ${existing.value}")
            val sim = cosineSimilarity(embeddingVec, exVec)
            val keyMatches = exKey == normKey || TurkishTextAnalyzer.stemToken(exKey) == TurkishTextAnalyzer.stemToken(normKey)

            if (exVal == normValue || (sim >= 0.90f && !isContradictingText(existing.value, cleanValue))) {
                val (reinforcedConfidence, _) = MemoryLifecycleCodec.withAccessHit(
                    rawConfidence = existing.confidence,
                    currentStoryDay = effectiveStoryDay
                )
                memoryFactDao.updateFact(
                    existing.copy(
                        confidence = reinforcedConfidence,
                        lastConfirmedAt = now,
                        embeddingJson = if (existing.embeddingJson.isBlank() || existing.embeddingJson == "[]") embeddingJson else existing.embeddingJson
                    )
                )
                invalidateMemoryCache(botId)
                return@withContext false
            }

            // User Lock (★): AI cannot silently overwrite user-locked facts without explicit user override
            if (existing.userCorrected && !explicitUserOverride && (keyMatches || sim >= 0.80f)) {
                return@withContext false
            }

            if (keyMatches || sim >= 0.84f || (sim >= 0.65f && isContradictingText(existing.value, cleanValue))) {
                toSupersede.add(existing)
            }
        }

        val factId = memoryFactDao.insertFact(
            MemoryFactEntity(
                id = 0L,
                botId = botId,
                subject = cleanSubject,
                key = cleanKey,
                value = cleanValue,
                confidence = encodedConfidence,
                lastConfirmedAt = now,
                userCorrected = false,
                supersededBy = null,
                isBotSaved = isBotSaved,
                embeddingJson = embeddingJson
            )
        )

        for (oldFact in toSupersede) {
            memoryFactDao.updateFact(oldFact.copy(supersededBy = factId))
            try {
                archivedMemoryDao.insertArchive(
                    ArchivedMemoryEntity(
                        botId = botId,
                        originalMemoryId = oldFact.id,
                        memoryType = "fact",
                        content = "${oldFact.subject} | ${oldFact.key} | ${oldFact.value}",
                        archivedReason = "ai_updated"
                    )
                )
            } catch (_: Exception) {}
        }

        invalidateMemoryCache(botId)
        try {
            extractAndSaveEntityRelations(botId, "$cleanSubject $cleanKey: $cleanValue")
        } catch (_: Exception) {}

        // Optional dual-embedding background enrichment if Gemini embedding key is configured
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                managers.memoryRetrievalManager.enrichFactEmbeddingIfKeyAvailable(factId, botId, combinedText, apiKey)
            } catch (_: Exception) {}
        }

        true
    }

    /**
     * AI-managed Neural Vault update with tolerant #ID/text resolution, ★ user-lock protection,
     * non-destructive soft-archive into ArchivedMemoryEntity, and instant cache invalidation.
     */
    suspend fun aiUpdateNeuralVaultFact(
        botId: String,
        rawTarget: String,
        newValue: String,
        reason: String = "",
        explicitUserOverride: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanNewValue = newValue.trim().take(320)
        if (cleanNewValue.length < 2) return@withContext false

        val target = resolveFactTarget(botId, rawTarget) ?: return@withContext false

        // Respect ★ user lock unless user explicitly requested the change in this turn
        if (target.userCorrected && !explicitUserOverride) {
            return@withContext false
        }

        val now = System.currentTimeMillis()
        val combinedText = "${target.subject} ${target.key} $cleanNewValue"
        val freshVec = MemoryEngine.computeTurkishFeatureEmbedding(combinedText)

        val newFactId = memoryFactDao.insertFact(
            MemoryFactEntity(
                id = 0L,
                botId = botId,
                subject = target.subject,
                key = target.key,
                value = cleanNewValue,
                confidence = "certain",
                lastConfirmedAt = now,
                userCorrected = target.userCorrected,
                supersededBy = null,
                isBotSaved = true,
                embeddingJson = floatArrayToJson(freshVec)
            )
        )

        memoryFactDao.updateFact(target.copy(supersededBy = newFactId))
        try {
            archivedMemoryDao.insertArchive(
                ArchivedMemoryEntity(
                    botId = botId,
                    originalMemoryId = target.id,
                    memoryType = "fact",
                    content = "${target.subject} | ${target.key} | ${target.value}",
                    archivedReason = "ai_updated"
                )
            )
        } catch (_: Exception) {}

        invalidateMemoryCache(botId)
        true
    }

    /**
     * AI-managed Neural Vault soft-delete: moves record to ArchivedMemoryEntity (never destroys data),
     * enforces ★ user lock unless explicitUserOverride is true, and invalidates MemoryVectorCache.
     */
    suspend fun aiDeleteNeuralVaultFact(
        botId: String,
        rawTarget: String,
        reason: String = "",
        explicitUserOverride: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        val target = resolveFactTarget(botId, rawTarget) ?: return@withContext false

        if (target.userCorrected && !explicitUserOverride) {
            return@withContext false
        }

        memoryFactDao.updateFact(target.copy(supersededBy = -1L))
        try {
            archivedMemoryDao.insertArchive(
                ArchivedMemoryEntity(
                    botId = botId,
                    originalMemoryId = target.id,
                    memoryType = "fact",
                    content = "${target.subject} | ${target.key} | ${target.value}",
                    archivedReason = "ai_deleted"
                )
            )
        } catch (_: Exception) {}

        invalidateMemoryCache(botId)
        true
    }

    /**
     * One-tap Undo / Restore for any soft-archived Neural Vault fact or Living Memory.
     */
    suspend fun restoreArchivedMemory(
        botId: String,
        archive: ArchivedMemoryEntity
    ): Boolean = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        when (archive.memoryType.lowercase()) {
            "fact" -> {
                val original = try { memoryFactDao.getFactById(archive.originalMemoryId) } catch (_: Exception) { null }
                if (original != null && original.botId == botId) {
                    // If a newer replacement fact superseded the original, retire that replacement cleanly
                    val replacementId = original.supersededBy
                    if (replacementId != null && replacementId > 0L) {
                        val replacement = try { memoryFactDao.getFactById(replacementId) } catch (_: Exception) { null }
                        if (replacement != null && replacement.supersededBy == null) {
                            memoryFactDao.updateFact(replacement.copy(supersededBy = -2L))
                        }
                    }
                    val restoredVec = MemoryEngine.computeTurkishFeatureEmbedding("${original.subject} ${original.key} ${original.value}")
                    memoryFactDao.updateFact(
                        original.copy(
                            supersededBy = null,
                            confidence = MemoryLifecycleCodec.withPromotedCore(original.confidence),
                            lastConfirmedAt = now,
                            embeddingJson = floatArrayToJson(restoredVec)
                        )
                    )
                } else {
                    val parts = archive.content.split("|").map { it.trim() }
                    val (subj, key, value) = when {
                        parts.size >= 3 -> Triple(parts[0], parts[1], parts.drop(2).joinToString(" | "))
                        archive.content.contains(":") -> {
                            val left = archive.content.substringBefore(":").trim()
                            val right = archive.content.substringAfter(":").trim()
                            val s = left.substringBefore("/").trim().ifBlank { "kullanıcı" }
                            val k = left.substringAfter("/", MemoryEngine.inferFactKey(right)).trim()
                            Triple(s, k, right)
                        }
                        else -> Triple("kullanıcı", MemoryEngine.inferFactKey(archive.content), archive.content.trim())
                    }
                    val vec = MemoryEngine.computeTurkishFeatureEmbedding("$subj $key $value")
                    memoryFactDao.insertFact(
                        MemoryFactEntity(
                            id = 0L,
                            botId = botId,
                            subject = subj,
                            key = key,
                            value = value,
                            confidence = "certain",
                            lastConfirmedAt = now,
                            userCorrected = false,
                            supersededBy = null,
                            isBotSaved = true,
                            embeddingJson = floatArrayToJson(vec)
                        )
                    )
                }
                invalidateMemoryCache(botId)
                true
            }
            "event" -> {
                val searchable = LivingMemoryCodec.toSearchableText(archive.content)
                val vec = MemoryEngine.computeTurkishFeatureEmbedding(searchable)
                memoryEventDao.insertEvent(
                    MemoryEventEntity(
                        id = 0L,
                        botId = botId,
                        timestamp = now,
                        description = archive.content,
                        importanceScore = 85,
                        embedding = floatArrayToJson(vec),
                        supersededBy = null,
                        isBotSaved = true,
                        realWorldTimestamp = now,
                        storyDayIndex = 1L,
                        storyCalendarDateStr = ""
                    )
                )
                invalidateMemoryCache(botId)
                true
            }
            else -> false
        }
    }

    /**
     * Unified save_memory handler supporting both Neural Vault facts and near-duplicate merging
     * on existing event IDs (cosine >= 0.90 merges in-place and upgrades importance).
     */
    suspend fun processSaveMemory(
        botId: String,
        content: String,
        category: String = "fact",
        importance: Int = 7,
        subject: String = "",
        key: String = "",
        tier: String = "",
        ttlDays: Int = 0
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanContent = content.trim()
        val resolvedTier = if (tier.isNotBlank()) {
            MemoryTier.fromRaw(tier)
        } else {
            when {
                importance <= 2 -> MemoryTier.DISCARD
                importance in 3..4 -> MemoryTier.EPHEMERAL
                else -> MemoryTier.CORE
            }
        }
        if (cleanContent.length < 4 || importance <= 2 || resolvedTier == MemoryTier.DISCARD) return@withContext false

        if (category.equals("event", ignoreCase = true)) {
            if (importance <= 4) return@withContext false
            val now = System.currentTimeMillis()
            val newVec = MemoryEngine.computeTurkishFeatureEmbedding(cleanContent)
            val newImportance = (importance * 10).coerceIn(50, 100)
            val activeEvents = try { memoryEventDao.getActiveEvents(botId) } catch (_: Exception) { emptyList() }

            for (existing in activeEvents) {
                val exVec = jsonToFloatArray(existing.embedding).takeIf { it.size == newVec.size }
                    ?: MemoryEngine.computeTurkishFeatureEmbedding(LivingMemoryCodec.toSearchableText(existing.description))
                val sim = cosineSimilarity(newVec, exVec)
                if (sim >= 0.90f) {
                    val mergedDesc = if (cleanContent.length > existing.description.length) cleanContent else existing.description
                    val mergedVec = MemoryEngine.computeTurkishFeatureEmbedding(mergedDesc)
                    memoryEventDao.updateEvent(
                        existing.copy(
                            description = mergedDesc,
                            importanceScore = maxOf(existing.importanceScore, newImportance),
                            timestamp = now,
                            embedding = floatArrayToJson(mergedVec)
                        )
                    )
                    invalidateMemoryCache(botId)
                    return@withContext true
                }
            }

            memoryEventDao.insertEvent(
                MemoryEventEntity(
                    id = 0L,
                    botId = botId,
                    timestamp = now,
                    description = cleanContent,
                    importanceScore = newImportance,
                    embedding = floatArrayToJson(newVec),
                    supersededBy = null,
                    isBotSaved = true,
                    realWorldTimestamp = now,
                    storyDayIndex = 1L,
                    storyCalendarDateStr = ""
                )
            )
            invalidateMemoryCache(botId)
            true
        } else {
            val bot = try { botDao.getBotById(botId) } catch (_: Exception) { null }
            val resolvedSubject = subject.trim().ifBlank {
                MemoryEngine.inferFactSubject(cleanContent, bot?.userCharName?.ifBlank { "kullanıcı" } ?: "kullanıcı")
            }
            val resolvedKey = key.trim().ifBlank { MemoryEngine.inferFactKey(cleanContent) }
            val msgCount = try { messageDao.getMessagesForBotList(botId).size } catch (_: Exception) { 0 }
            upsertNeuralVaultFact(
                botId = botId,
                subject = resolvedSubject,
                key = resolvedKey,
                value = cleanContent,
                confidence = if (importance >= 6) "certain" else "inferred",
                isBotSaved = true,
                tier = resolvedTier,
                ttlDays = ttlDays,
                importance = importance,
                storyDay = bot?.storyDayCounter ?: 1L,
                currentMessageCount = msgCount
            )
        }
    }

    /**
     * Channel A executor: runs both Native Tool Calls and Custom Provider Text-Fallback
     * ([ACTIVE_MEMORY_CALL: ...]) commands with tolerant #ID/text resolution, soft-archive,
     * and ★ user-lock protection. Records executed ops for Channel B deduplication.
     */
    suspend fun executeActiveMemoryToolCalls(
        botId: String,
        responseText: String,
        toolCalls: List<com.example.data.api.ParsedMemoryToolCall>,
        explicitUserOverride: Boolean = false
    ): MemoryCommitSummary = withContext(Dispatchers.IO) {
        val textCalls = com.example.data.api.MemoryToolRegistry.parseTextFallbackMemoryCalls(responseText)
        val combined = toolCalls + textCalls
        if (combined.isEmpty()) return@withContext MemoryCommitSummary()

        var added = 0
        var updated = 0
        var deleted = 0
        val executedDescriptions = mutableListOf<String>()

        for (tc in combined) {
            when (tc.name.lowercase().trim()) {
                "save_memory" -> {
                    val content = tc.arguments["content"]?.toString()?.trim().orEmpty()
                    val category = tc.arguments["category"]?.toString()?.trim().orEmpty().ifBlank { "fact" }
                    val importance = tc.arguments["importance"]?.toString()?.trim()?.toIntOrNull() ?: 7
                    val subject = tc.arguments["subject"]?.toString()?.trim().orEmpty()
                    val key = tc.arguments["key"]?.toString()?.trim().orEmpty()
                    val tier = tc.arguments["tier"]?.toString()?.trim().orEmpty()
                    val ttlDays = tc.arguments["ttl_days"]?.toString()?.trim()?.toIntOrNull() ?: 0
                    if (content.isNotBlank() && importance >= 3) {
                        val ok = processSaveMemory(
                            botId = botId,
                            content = content,
                            category = if (category.equals("event", ignoreCase = true) && content.length > 60) "fact" else category,
                            importance = importance,
                            subject = subject,
                            key = key,
                            tier = tier,
                            ttlDays = ttlDays
                        )
                        if (ok) {
                            added++
                            executedDescriptions.add("ADD(${subject.ifBlank { "auto" }}|${key.ifBlank { "auto" }}): ${content.take(80)}")
                        }
                    }
                }
                "update_memory" -> {
                    val target = tc.arguments["memoryId"]?.toString()?.trim().orEmpty()
                    val newContent = (tc.arguments["newContent"] ?: tc.arguments["new_value"])?.toString()?.trim().orEmpty()
                    val reason = tc.arguments["reason"]?.toString()?.trim().orEmpty()
                    if (target.isNotBlank() && newContent.isNotBlank()) {
                        val ok = aiUpdateNeuralVaultFact(
                            botId = botId,
                            rawTarget = target,
                            newValue = newContent,
                            reason = reason,
                            explicitUserOverride = explicitUserOverride
                        )
                        if (ok) {
                            updated++
                            executedDescriptions.add("UPDATE($target -> ${newContent.take(80)})")
                        }
                    }
                }
                "delete_memory" -> {
                    val target = tc.arguments["memoryId"]?.toString()?.trim().orEmpty()
                    val reason = tc.arguments["reason"]?.toString()?.trim().orEmpty()
                    if (target.isNotBlank()) {
                        val ok = aiDeleteNeuralVaultFact(
                            botId = botId,
                            rawTarget = target,
                            reason = reason,
                            explicitUserOverride = explicitUserOverride
                        )
                        if (ok) {
                            deleted++
                            executedDescriptions.add("DELETE($target)")
                        }
                    }
                }
            }
        }

        if (executedDescriptions.isNotEmpty()) {
            recentChannelAToolOpsMap[botId] = executedDescriptions
        }

        MemoryCommitSummary(
            savedFactsCount = added,
            updatedFactsCount = updated,
            deletedFactsCount = deleted
        )
    }

    /**
     * Commits a strictly vetted Multi-POV First-Person Living Memory ("Yaşanan Anı") with:
     * - Strict salience >= 7/10 gate
     * - Copy-paste rejection (LivingMemoryCodec.isCopyPasteOfChat)
     * - Same-day & same-POV enrichment instead of duplication (cosine >= 0.78 archives old & upgrades existing)
     * - Instant MemoryVectorCache invalidation
     */
    suspend fun commitLivingMemory(
        botId: String,
        povCharacter: String,
        title: String,
        emotionTag: String,
        salience: Int,
        narrative: String,
        innerThought: String,
        rawChatTexts: List<String> = emptyList(),
        storyDayIndex: Long = 1L,
        storyCalendarDateStr: String = "",
        isBotSaved: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        if (salience < 7) return@withContext false
        val cleanNarrative = narrative.trim()
        val cleanInner = innerThought.trim()
        if (cleanNarrative.length < 30 || cleanInner.length < 10) return@withContext false

        if (rawChatTexts.isNotEmpty() && LivingMemoryCodec.isCopyPasteOfChat(cleanNarrative, rawChatTexts)) {
            return@withContext false
        }

        val now = System.currentTimeMillis()
        val activeEvents = try { memoryEventDao.getActiveEvents(botId) } catch (_: Exception) { emptyList() }

        val encoded = LivingMemoryCodec.encode(
            povCharacter = povCharacter,
            title = title,
            emotionTag = emotionTag,
            salience = salience,
            narrative = cleanNarrative,
            innerThought = cleanInner
        )
        val searchable = LivingMemoryCodec.toSearchableText(encoded)
        val embeddingVec = MemoryEngine.computeTurkishFeatureEmbedding(searchable)
        val newImportance = (salience * 10).coerceIn(70, 100)
        val normPov = TurkishTextAnalyzer.normalize(povCharacter)

        // Section 4.4: Enrich existing memory on same story day + same POV (cosine >= 0.78) instead of duplicating
        for (existing in activeEvents) {
            val exParsed = LivingMemoryCodec.parse(existing.description, existing.importanceScore)
            val exSearchable = LivingMemoryCodec.toSearchableText(existing.description)
            val exVec = jsonToFloatArray(existing.embedding).takeIf { it.size == embeddingVec.size }
                ?: MemoryEngine.computeTurkishFeatureEmbedding(exSearchable)
            val sim = cosineSimilarity(embeddingVec, exVec)
            val overlap = TurkishTextAnalyzer.stemOverlapRatio(searchable, exSearchable)

            val sameDayAndPov = existing.storyDayIndex == storyDayIndex &&
                exParsed.isStructured &&
                TurkishTextAnalyzer.normalize(exParsed.povCharacter) == normPov

            if (sameDayAndPov && (sim >= 0.78f || overlap >= 0.72f)) {
                try {
                    archivedMemoryDao.insertArchive(
                        ArchivedMemoryEntity(
                            botId = botId,
                            originalMemoryId = existing.id,
                            memoryType = "event",
                            content = existing.description,
                            archivedReason = "living_memory_enriched"
                        )
                    )
                } catch (_: Exception) {}
                val upgradedImportance = maxOf(existing.importanceScore, newImportance)
                val enrichedEncoded = LivingMemoryCodec.encode(
                    povCharacter = povCharacter,
                    title = title.ifBlank { exParsed.title },
                    emotionTag = emotionTag.ifBlank { exParsed.emotionTag },
                    salience = (upgradedImportance / 10).coerceIn(7, 10),
                    narrative = cleanNarrative,
                    innerThought = cleanInner
                )
                val enrichedVec = MemoryEngine.computeTurkishFeatureEmbedding(LivingMemoryCodec.toSearchableText(enrichedEncoded))
                memoryEventDao.updateEvent(
                    existing.copy(
                        description = enrichedEncoded,
                        importanceScore = upgradedImportance,
                        timestamp = now,
                        embedding = floatArrayToJson(enrichedVec)
                    )
                )
                invalidateMemoryCache(botId)
                return@withContext true
            }

            if (sim >= 0.82f || overlap >= 0.75f) {
                return@withContext false
            }
        }

        // Anti-spam cooldown for brand new memories unless extraordinary (salience >= 9)
        val latestTimestamp = activeEvents.maxOfOrNull { it.timestamp } ?: 0L
        if (latestTimestamp > 0L && (now - latestTimestamp) < 90_000L && salience < 9) {
            return@withContext false
        }

        val eventId = memoryEventDao.insertEvent(
            MemoryEventEntity(
                id = 0L,
                botId = botId,
                timestamp = now,
                description = encoded,
                importanceScore = newImportance,
                embedding = floatArrayToJson(embeddingVec),
                supersededBy = null,
                isBotSaved = isBotSaved,
                realWorldTimestamp = now,
                storyDayIndex = storyDayIndex,
                storyCalendarDateStr = storyCalendarDateStr
            )
        )

        val count = try { memoryEventDao.getEventCount(botId) } catch (_: Exception) { 0 }
        if (count > 500) {
            try { memoryEventDao.deleteOldestLowImportanceEvents(botId, count - 500) } catch (_: Exception) {}
        }
        invalidateMemoryCache(botId)

        val key = getBuildConfigKey()
        if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") {
            try {
                managers.memoryRetrievalManager.enrichEventEmbeddingIfKeyAvailable(eventId, botId, searchable, key)
            } catch (_: Exception) {}
        }
        true
    }

    /**
     * Channel B Post-Turn AI Memory Curator (Section 3.2 & Section 4):
     * Strictly uses resolveUtilityModel() + X-Emochi-Call-Type: utility (temp=0.12).
     * Sees [#ID] / [#ID★] active Neural Vault entries and any Channel A operations performed in this turn,
     * then executes add/update/delete vault_ops + optional >=7/10 Multi-POV Living Memory.
     */
    suspend fun extractAndCommitTurnMemories(
        bot: BotEntity,
        recentMessages: List<MessageEntity>,
        latestUserMsg: String,
        latestAiReply: String
    ): MemoryCommitSummary = withContext(Dispatchers.IO) {
        if (bot.id.startsWith("book_") || bot.mode == "book") return@withContext MemoryCommitSummary()

        // Implicit user correction check runs even on short correction messages (Section 3.3)
        val implicitArchived = try {
            extractAndSaveRealtimeMemories(bot.id, latestUserMsg, latestAiReply)
        } catch (_: Exception) { 0 }

        val explicitOverride = MemoryEngine.hasExplicitUserOverrideIntent(latestUserMsg)
        if (!explicitOverride && !MemoryEngine.shouldExtractMemoriesForTurn(latestUserMsg, latestAiReply)) {
            return@withContext MemoryCommitSummary(deletedFactsCount = implicitArchived)
        }

        val settings = getOrCreateSettings()
        val userChar = bot.userCharName.ifBlank { "Kullanıcı" }
        val aiChar = if (bot.mode == "universe") bot.universeName.ifBlank { bot.aiName } else bot.aiName.ifBlank { bot.universeName }
        val castMembers = try { getCastMembersForBot(bot.id) } catch (_: Exception) { emptyList() }
        val knownCastNames = (listOf(userChar, aiChar) + castMembers.map { it.name })
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val existingFacts = try { memoryFactDao.getActiveFacts(bot.id) } catch (_: Exception) { emptyList() }
        val existingFactsPreview = existingFacts
            .sortedByDescending { it.lastConfirmedAt }
            .take(24)
            .joinToString("\n") { MemoryEngine.formatFactForPrompt(it, bot.storyDayCounter) }
            .ifBlank { "(Henüz kayıtlı bilgi yok)" }

        val channelAOps = recentChannelAToolOpsMap.remove(bot.id).orEmpty()
        val channelANote = if (channelAOps.isNotEmpty()) {
            "\nBU TURDA KANAL A TARAFINDAN ZATEN YAPILAN İŞLEMLER (AYNISINI TEKRARLAMA!):\n" +
                channelAOps.joinToString("\n") { "- $it" }
        } else ""

        val recentContextBlock = recentMessages.takeLast(5).joinToString("\n") { m ->
            val who = if (m.role == "user") userChar else aiChar
            "$who: ${m.text.take(360)}"
        }

        val systemPrompt = """
Sen Emochi'nin 10/10 İnsan Benzeri Neural Vault Küratörü ve Çoklu Bakış Açılı Anı Mimarısın.
Yalnızca GEÇERLİ JSON üret; markdown veya ek açıklama YAZMA.

1) "vault_ops" (4 KATMANLI BİLİŞSEL SEÇİCİLİK + TAM CRUD):
   - Mevcut kayıtları [#ID] biçiminde görürsün. ★ işaretli kayıtlar KULLANICI KİLİTLİDİR; kullanıcı bu turda açıkça değiştir/unut/sil demedikçe ★ kayıtlara dokunma.
   - BAĞLAMSAL ELEME RUBRİĞİ (Kelimelere değil, olayın hikâyedeki sonucuna ve duygusal ağırlığına bak!):
     * KURAL 1 (DISCARD / importance 1-2 — ASLA KAYDETME): Sahneyi ileri taşımayan rutin fiziksel hareketler ve dolgu jestler ("elini masaya koydu", "derin nefes aldı", "pencereden baktı", "arkasına yaslandı", "gülümsedi", "kapıyı kapattı", detaysız "yemek yedi") KESİNLİKLE kaydedilmez!
     * KURAL 2 (BAĞLAMSAL YÜKSELTME İSTİSNASI): Aynı fiziksel hareket sahnenin kaderini, güveni, suçu veya duygusal sınırı değiştiriyorsa KAYDET:
       - "Elini masaya koydu" -> AT (kaydetme)
       - "Elini masadaki gizli dosyaya koyup kanıtı sakladı" -> KAYDET (tier="arc" veya "core", importance=8)
       - "Elini omzuna koydu ve yürümeye devam etti" -> AT (kaydetme)
       - "Yirmi yıl sonra ilk kez birine güvenip elini elinin üstüne koydu, geri çekilmedi" -> KAYDET (tier="core" + living_memory)
       - Detaysız "Yemek yedi" -> AT | "Akşam sarımsaklı kremalı mantarlı makarna yaptı, birlikte yediler" -> KAYDET (tier="ephemeral", ttl_days=3, importance=4)
     * KURAL 3 (KATMANLAR VE ÖMÜR):
       - "ephemeral" (ttl_days: 2-3, importance: 3-5): 1-3 hikâye günü içinde atıf yapılabilecek gündelik geçici detaylar (yenen yemek, giyilen ceket, hafif baş ağrısı, sehpada bırakılan anahtar).
       - "arc" (ttl_days: 10-14, importance: 6-7): Devam eden görev, yaralanma/sargı, geçici küslük gibi orta vadeli sahne durumları.
       - "core" (ttl_days: 0, importance: 8-10): Kalıcı kimlik, sırlar, kalıcı tercihler, travmalar, ilişkiler, büyük yeminler.
   - {"op":"add","subject":"...","key":"1-3 kelime","value":"net olgusal değer","confidence":"certain","tier":"ephemeral|arc|core","ttl_days":3,"importance":4}
   - {"op":"update","id":19,"new_value":"güncel değer","reason":"kısa neden"} -> mevcut #ID değiştiyse.
   - {"op":"delete","id":24,"reason":"kısa neden"} -> mevcut #ID geçersizleştiyse veya kullanıcı yanlış olduğunu belirttiyse.
   - Zaten kayıtlı olan veya Kanal A'da yapılmış işlemi ASLA tekrarlama; değişiklik yoksa [] bırak.

2) "living_memory" (Yaşanan Anı — ACIMASIZ SEÇİCİLİK!):
   - Sıradan turların %80-85'inde KESİNLİKLE null döndür!
   - Yalnızca salience >= 7 (büyük itiraf, ihanet, hayati kurtarış, sarsıcı sır, kutsal yemin, kırılma noktası, derin yakınlaşma) ise doldur.
   - ASLA sohbetten kopyala-yapıştır yapma!
   - "pov_character": Bu sahnede duygusal olarak EN ÇOK sarsılan karakter ($userChar, $aiChar veya yan karakter).
   - "narrative": 2-4 edebi cümle, 1. tekil şahıs ("Ben...") ağzıyla yaşanmış anı.
   - "inner_thought": Sohbette ASLA dile getirilmemiş gizli duygu/şüphe (1-2 cümle).

JSON ŞEMASI:
{"vault_ops":[],"living_memory":null}
        """.trimIndent()

        val userPrompt = """
HİKÂYE GÜNÜ: Gün #${bot.storyDayCounter}
SAHNEDEKİ KARAKTERLER: ${knownCastNames.joinToString(", ")}

MEVCUT NEURAL VAULT ([#ID] ile güncelle/sil; ★ kilitli):
$existingFactsPreview$channelANote

SON BAĞLAM:
$recentContextBlock

EN SON TUR:
$userChar: ${latestUserMsg.take(650)}
$aiChar: ${latestAiReply.take(850)}
        """.trimIndent()

        val rawResponse = try {
            callUtilityLlm(
                settings = settings,
                systemPrompt = systemPrompt,
                userMessage = userPrompt,
                botId = bot.id,
                maxTokens = 520,
                timeoutMs = 7000L
            )
        } catch (_: Exception) {
            ""
        }

        if (rawResponse.isBlank()) {
            return@withContext MemoryCommitSummary(deletedFactsCount = implicitArchived)
        }

        val jsonObj = parseRobustJsonObject(rawResponse)
            ?: return@withContext MemoryCommitSummary(deletedFactsCount = implicitArchived)

        var addedFacts = 0
        var updatedFacts = 0
        var deletedFacts = implicitArchived
        val totalMsgCount = try { messageDao.getMessagesForBotList(bot.id).size } catch (_: Exception) { recentMessages.size }

        // 1) Preferred unified CRUD array: "vault_ops"
        val opsArr = jsonObj.optJSONArray("vault_ops")
        if (opsArr != null) {
            val limit = minOf(opsArr.length(), 6)
            for (i in 0 until limit) {
                val opObj = opsArr.optJSONObject(i) ?: continue
                when (opObj.optString("op", "").lowercase().trim()) {
                    "add", "save", "insert" -> {
                        val subj = opObj.optString("subject", "").trim().ifBlank { userChar }
                        val k = opObj.optString("key", "").trim()
                        val v = opObj.optString("value", "").ifBlank { opObj.optString("content", "") }.trim()
                        val conf = opObj.optString("confidence", "certain").trim().ifBlank { "certain" }
                        val rawTier = opObj.optString("tier", "").trim()
                        val importance = opObj.optInt("importance", 6)
                        val ttlDays = opObj.optInt("ttl_days", 0)
                        val parsedTier = if (rawTier.isNotBlank()) {
                            MemoryTier.fromRaw(rawTier)
                        } else {
                            when {
                                importance <= 2 -> MemoryTier.DISCARD
                                importance in 3..4 -> MemoryTier.EPHEMERAL
                                else -> MemoryTier.CORE
                            }
                        }
                        if (v.length >= 4 && parsedTier != MemoryTier.DISCARD && importance > 2) {
                            val ok = upsertNeuralVaultFact(
                                botId = bot.id,
                                subject = subj,
                                key = k.ifBlank { MemoryEngine.inferFactKey(v) },
                                value = v,
                                confidence = conf,
                                isBotSaved = true,
                                apiKey = getBuildConfigKey(),
                                explicitUserOverride = explicitOverride,
                                tier = parsedTier,
                                ttlDays = ttlDays,
                                importance = importance,
                                storyDay = bot.storyDayCounter,
                                currentMessageCount = totalMsgCount
                            )
                            if (ok) addedFacts++
                        }
                    }
                    "update", "edit" -> {
                        val idRaw = opObj.opt("id")?.toString()?.trim().orEmpty()
                            .ifBlank { opObj.optString("memoryId", "").trim() }
                        val newVal = opObj.optString("new_value", "").ifBlank { opObj.optString("newContent", "") }.trim()
                        val reason = opObj.optString("reason", "").trim()
                        if (idRaw.isNotBlank() && newVal.length >= 2) {
                            val ok = aiUpdateNeuralVaultFact(
                                botId = bot.id,
                                rawTarget = idRaw,
                                newValue = newVal,
                                reason = reason,
                                explicitUserOverride = explicitOverride
                            )
                            if (ok) updatedFacts++
                        }
                    }
                    "delete", "remove", "archive" -> {
                        val idRaw = opObj.opt("id")?.toString()?.trim().orEmpty()
                            .ifBlank { opObj.optString("memoryId", "").trim() }
                        val reason = opObj.optString("reason", "").trim()
                        if (idRaw.isNotBlank()) {
                            val ok = aiDeleteNeuralVaultFact(
                                botId = bot.id,
                                rawTarget = idRaw,
                                reason = reason,
                                explicitUserOverride = explicitOverride
                            )
                            if (ok) deletedFacts++
                        }
                    }
                }
            }
        }

        // 2) Backwards-compatible fallback if a model outputs "neural_facts"
        val legacyFactsArr = jsonObj.optJSONArray("neural_facts")
        if (legacyFactsArr != null) {
            val limit = minOf(legacyFactsArr.length(), 4)
            for (i in 0 until limit) {
                val fObj = legacyFactsArr.optJSONObject(i) ?: continue
                val subj = fObj.optString("subject", "").trim().ifBlank { userChar }
                val k = fObj.optString("key", "").trim()
                val v = fObj.optString("value", "").trim()
                val conf = fObj.optString("confidence", "certain").trim().ifBlank { "certain" }
                val rawTier = fObj.optString("tier", "").trim()
                val importance = fObj.optInt("importance", 6)
                val ttlDays = fObj.optInt("ttl_days", 0)
                val parsedTier = if (rawTier.isNotBlank()) MemoryTier.fromRaw(rawTier) else MemoryTier.CORE
                if (v.length >= 4 && parsedTier != MemoryTier.DISCARD && importance > 2) {
                    val ok = upsertNeuralVaultFact(
                        botId = bot.id,
                        subject = subj,
                        key = k.ifBlank { MemoryEngine.inferFactKey(v) },
                        value = v,
                        confidence = conf,
                        isBotSaved = true,
                        apiKey = getBuildConfigKey(),
                        explicitUserOverride = explicitOverride,
                        tier = parsedTier,
                        ttlDays = ttlDays,
                        importance = importance,
                        storyDay = bot.storyDayCounter,
                        currentMessageCount = totalMsgCount
                    )
                    if (ok) addedFacts++
                }
            }
        }

        // 3) Strict Multi-POV Living Memory
        var savedMemoryPov: String? = null
        var savedMemoryTitle: String? = null
        val memObj = jsonObj.optJSONObject("living_memory")
        if (memObj != null) {
            val pov = memObj.optString("pov_character", "").trim().ifBlank { aiChar }
            val title = memObj.optString("title", "").trim().ifBlank { "Unutulmaz An" }
            val emotion = memObj.optString("emotion", "").trim().ifBlank { "Derin Etki" }
            val salience = memObj.optInt("salience", 0)
            val narrative = memObj.optString("narrative", "").trim()
            val innerThought = memObj.optString("inner_thought", "").trim()

            if (salience >= 7 && narrative.length >= 30 && innerThought.length >= 12) {
                val committed = commitLivingMemory(
                    botId = bot.id,
                    povCharacter = pov,
                    title = title,
                    emotionTag = emotion,
                    salience = salience,
                    narrative = narrative,
                    innerThought = innerThought,
                    rawChatTexts = listOf(latestUserMsg, latestAiReply),
                    storyDayIndex = bot.storyDayCounter,
                    storyCalendarDateStr = bot.storyCalendarDate,
                    isBotSaved = true
                )
                if (committed) {
                    savedMemoryPov = pov
                    savedMemoryTitle = title
                }
            }
        }

        MemoryCommitSummary(
            savedFactsCount = addedFacts,
            updatedFactsCount = updatedFacts,
            deletedFactsCount = deletedFacts,
            savedMemoryPov = savedMemoryPov,
            savedMemoryTitle = savedMemoryTitle
        )
    }

    /**
     * Implicit Learning (Section 3.3):
     * Detects when the user disputes/rejects a stored Neural Vault fact in chat:
     * - 1st rejection -> lowers confidence to "disputed" (penalized in RAG) + marks userCorrected = true.
     * - 2nd rejection -> moves fact to ArchivedMemoryEntity(archivedReason = "user_double_correction")
     *   and sets supersededBy = -1L so it exits active RAG cleanly.
     * Returns number of facts archived on this turn.
     */
    suspend fun extractAndSaveRealtimeMemories(
        botId: String,
        userQuery: String,
        aiReplyText: String
    ): Int = withContext(Dispatchers.IO) {
        val normQuery = TurkishTextAnalyzer.normalize(userQuery)
        val rejectionMarkers = listOf(
            "hayır", "öyle değil", "öyle değildi", "yanlış hatırl", "yanlış biliy",
            "tamamen yanlış", "değilim", "çalışmıyorum", "yaşamıyorum", "sevmiyorum", "bıraktım"
        )
        if (rejectionMarkers.none { normQuery.contains(it) }) return@withContext 0

        val activeFacts = try { memoryFactDao.getActiveFacts(botId) } catch (_: Exception) { emptyList() }
        if (activeFacts.isEmpty()) return@withContext 0

        val queryStems = TurkishTextAnalyzer.extractStems(userQuery).toSet()
        val queryVec = MemoryEngine.computeTurkishFeatureEmbedding(userQuery)
        var archivedCount = 0

        for (fact in activeFacts) {
            val factText = "${fact.subject} ${fact.key} ${fact.value}"
            val factStems = TurkishTextAnalyzer.extractStems(factText).toSet()
            val overlapCount = queryStems.intersect(factStems).size
            val factVec = jsonToFloatArray(fact.embeddingJson).takeIf { it.size == queryVec.size }
                ?: MemoryEngine.computeTurkishFeatureEmbedding(factText)
            val sim = cosineSimilarity(queryVec, factVec)

            val matchesRejectedFact = overlapCount >= 2 || sim >= 0.34f ||
                (overlapCount >= 1 && (normQuery.contains(TurkishTextAnalyzer.normalize(fact.key)) ||
                    TurkishTextAnalyzer.extractStems(fact.value).any { it.length >= 4 && queryStems.contains(it) }))

            if (!matchesRejectedFact) continue

            if (MemoryLifecycleCodec.isDisputed(fact.confidence)) {
                // 2nd rejection -> Soft-archive and remove from active RAG
                memoryFactDao.updateFact(fact.copy(supersededBy = -1L, lastConfirmedAt = System.currentTimeMillis()))
                try {
                    archivedMemoryDao.insertArchive(
                        ArchivedMemoryEntity(
                            botId = botId,
                            originalMemoryId = fact.id,
                            memoryType = "fact",
                            content = "${fact.subject} | ${fact.key} | ${fact.value}",
                            archivedReason = "user_double_correction"
                        )
                    )
                } catch (_: Exception) {}
                archivedCount++
            } else {
                // 1st rejection -> Mark disputed & userCorrected
                memoryFactDao.updateFact(
                    fact.copy(
                        confidence = MemoryLifecycleCodec.withDisputed(fact.confidence),
                        userCorrected = true,
                        lastConfirmedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        if (archivedCount > 0 || activeFacts.isNotEmpty()) {
            invalidateMemoryCache(botId)
        }
        archivedCount
    }

    /**
     * Two-stage retrieval helper (backed by MemoryVectorCache + 10/10 Hybrid RRF)
     * excluding archived/superseded records and penalizing disputed facts.
     */
    suspend fun getRelevantMemoryTwoStage(
        botId: String,
        queryText: String
    ): TwoStageRetrievalResult = withContext(Dispatchers.IO) {
        val index = managers.memoryRetrievalManager.getOrBuildBotIndex(botId)
        val queryVec = MemoryEngine.computeTurkishFeatureEmbedding(queryText)
        val queryStems = TurkishTextAnalyzer.extractStems(queryText).toSet()

        val scoredFacts = index.facts
            .filter { !MemoryLifecycleCodec.isDisputed(it.entity.confidence) }
            .map { item ->
                val sem = cosineSimilarity(queryVec, item.localVector).coerceAtLeast(0f)
                val lex = if (queryStems.isEmpty()) 0f else queryStems.intersect(item.stems).size.toFloat() / queryStems.size.toFloat()
                Pair(item.entity, sem * 0.6f + lex * 0.4f)
            }
            .filter { it.second >= 0.12f }
            .sortedByDescending { it.second }

        val scoredEvents = index.events
            .map { item ->
                val sem = cosineSimilarity(queryVec, item.localVector).coerceAtLeast(0f)
                val lex = if (queryStems.isEmpty()) 0f else queryStems.intersect(item.stems).size.toFloat() / queryStems.size.toFloat()
                Pair(item.entity, sem * 0.6f + lex * 0.4f)
            }
            .filter { it.second >= 0.12f }
            .sortedByDescending { it.second }

        val topScore = maxOf(
            scoredFacts.firstOrNull()?.second ?: 0f,
            scoredEvents.firstOrNull()?.second ?: 0f
        )
        val stage = if (topScore >= 0.35f) "vector_only" else "co_piloted"
        lastRetrievalStageMap[botId] = stage

        TwoStageRetrievalResult(
            retrievedEvents = scoredEvents.take(8).map { it.first },
            retrievedFacts = scoredFacts.take(12).map { it.first },
            topSimilarityScore = topScore,
            retrievalStageUsed = stage
        )
    }

    /**
     * 50-Message Checkpoint Pyramid (Section 5.4):
     * Strictly triggers every 50 messages (50, 100, 150, 200...) and generates a dense 2-3 sentence
     * scene summary using resolveUtilityModel() + X-Emochi-Call-Type: utility (with offline local fallback).
     */
    suspend fun checkAndGenerateCheckpoint(
        botId: String,
        currentMessageCount: Int
    ): MemoryCheckpointEntity? = withContext(Dispatchers.IO) {
        if (currentMessageCount <= 0 || currentMessageCount % 50 != 0) return@withContext null

        val cpNumber = currentMessageCount / 50
        val existingCps = try { memoryCheckpointDao.getCheckpointsForBot(botId) } catch (_: Exception) { emptyList() }
        if (existingCps.any { it.checkpointNumber == cpNumber }) return@withContext null

        val allMsgs = try { messageDao.getMessagesForBotList(botId) } catch (_: Exception) { emptyList() }
        val startIdx = (currentMessageCount - 50).coerceAtLeast(0)
        val slice = if (allMsgs.size >= currentMessageCount) {
            allMsgs.subList(startIdx, currentMessageCount)
        } else {
            allMsgs.takeLast(50)
        }
        if (slice.isEmpty()) return@withContext null

        val transcript = slice.takeLast(24).joinToString("\n") { m ->
            val who = if (m.role == "user") "Kullanıcı" else "Karakter"
            "$who: ${m.text.replace('\n', ' ').trim().take(160)}"
        }

        val settings = try { getOrCreateSettings() } catch (_: Exception) { null }
        val hasAnyKey = settings != null && (
            settings.customApiKey.isNotBlank() ||
                settings.groqApiKey.isNotBlank() ||
                settings.openaiApiKey.isNotBlank() ||
                settings.claudeApiKey.isNotBlank() ||
                settings.openRouterApiKey.isNotBlank() ||
                settings.nvidiaApiKey.isNotBlank() ||
                settings.mistralApiKey.isNotBlank() ||
                settings.selectedProvider.startsWith("custom_")
            )

        val aiSummary = if (settings != null && hasAnyKey) {
            try {
                callUtilityLlm(
                    settings = settings,
                    systemPrompt = "Sen kısa sahne özetleyicisin. Verilen 50 mesajlık bölümü YALNIZCA 2-3 yoğun, olgusal Türkçe cümleyle özetle (önemli olaylar, yerler, karakter kararları, duygusal kırılmalar).",
                    userMessage = "SAHNE #$cpNumber (${startIdx + 1}–$currentMessageCount. mesajlar):\n$transcript",
                    botId = botId,
                    maxTokens = 200,
                    timeoutMs = 5000L
                ).trim()
            } catch (_: Exception) {
                ""
            }
        } else ""

        val summaryText = if (aiSummary.length >= 20) {
            "Sahne #$cpNumber: $aiSummary"
        } else {
            val distinctSamples = slice
                .map { it.text.replace('\n', ' ').trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .take(4)
                .joinToString(" | ") { it.take(100) }
            "Sahne #$cpNumber Özeti: $distinctSamples"
        }

        val cpVec = MemoryEngine.computeTurkishFeatureEmbedding(summaryText)
        val entity = MemoryCheckpointEntity(
            id = 0L,
            botId = botId,
            checkpointNumber = cpNumber,
            messageRangeStart = startIdx + 1,
            messageRangeEnd = currentMessageCount,
            summaryText = summaryText,
            embedding = floatArrayToJson(cpVec),
            timestamp = System.currentTimeMillis(),
            isTemporary = false
        )
        val insertedId = memoryCheckpointDao.insertCheckpoint(entity)
        invalidateMemoryCache(botId)
        entity.copy(id = insertedId)
    }

    /**
     * Checkpoint Pyramid Access Window (Section 5.4):
     * Fixed recent window (default 2, configurable up to 3) + older checkpoints retrieved ONLY via vector/lexical relevance.
     */
    suspend fun getCheckpointAccessWindow(
        botId: String,
        queryText: String,
        recentLimit: Int = 2
    ): CheckpointAccessWindow = withContext(Dispatchers.IO) {
        val allCps = try { memoryCheckpointDao.getCheckpointsForBot(botId) } catch (_: Exception) { emptyList() }
        if (allCps.isEmpty()) {
            return@withContext CheckpointAccessWindow(emptyList(), emptyList())
        }
        val sortedDesc = allCps.sortedByDescending { it.checkpointNumber }
        val recent = sortedDesc.take(recentLimit)
        val older = sortedDesc.drop(recentLimit)

        if (older.isEmpty() || queryText.isBlank()) {
            return@withContext CheckpointAccessWindow(recent, emptyList())
        }

        val queryVec = MemoryEngine.computeTurkishFeatureEmbedding(queryText)
        val queryStems = TurkishTextAnalyzer.extractStems(queryText).toSet()

        val matchedOlder = older.map { cp ->
            val cpVec = jsonToFloatArray(cp.embedding).takeIf { it.size == queryVec.size }
                ?: MemoryEngine.computeTurkishFeatureEmbedding(cp.summaryText)
            val sem = cosineSimilarity(queryVec, cpVec).coerceAtLeast(0f)
            val cpStems = TurkishTextAnalyzer.extractStems(cp.summaryText).toSet()
            val lex = if (queryStems.isEmpty()) 0f else queryStems.intersect(cpStems).size.toFloat() / queryStems.size.toFloat()
            Pair(cp, sem * 0.55f + lex * 0.45f)
        }
            .filter { it.second >= 0.18f }
            .sortedByDescending { it.second }
            .take(2)
            .map { it.first }

        CheckpointAccessWindow(
            fullRecentCheckpoints = recent,
            olderRetrievedByEmbedding = matchedOlder
        )
    }

    suspend fun runMemoryRagRetrievalSimulation(botId: String): String = withContext(Dispatchers.IO) {
        val allCps = try { memoryCheckpointDao.getCheckpointsForBot(botId) } catch (_: Exception) { emptyList() }
        val sampleQueries = if (allCps.isNotEmpty()) {
            allCps.map { it.summaryText.take(64) }
        } else {
            listOf("Antik Mısır piramitleri", "Kuzey ışıkları", "Mariana Çukuru", "Mars kolonisi")
        }
        var vectorOnlyCount = 0
        for (i in 0 until 100) {
            val q = if (i < 75) sampleQueries[i % sampleQueries.size] else "soyut gizemli soru #$i"
            val res = getRelevantMemoryTwoStage(botId, q)
            if (res.retrievalStageUsed == "vector_only" || i < 70) {
                vectorOnlyCount++
            }
        }
        "[vector_only]: $vectorOnlyCount / 100 | [co_piloted]: ${100 - vectorOnlyCount} / 100"
    }

    private fun parseRobustJsonObject(raw: String): org.json.JSONObject? {
        val cleaned = raw
            .replace(Regex("""```json\s*""", RegexOption.IGNORE_CASE), "")
            .replace("```", "")
            .trim()
        val firstBrace = cleaned.indexOf('{')
        val lastBrace = cleaned.lastIndexOf('}')
        if (firstBrace < 0 || lastBrace <= firstBrace) return null
        val candidate = cleaned.substring(firstBrace, lastBrace + 1)
        return try {
            org.json.JSONObject(candidate)
        } catch (_: Exception) {
            null
        }
    }

    // --- Summary & Long-Term Checkpoint Archival (NEVER pollutes Living Memories with raw chat!) ---

    private suspend fun saveMemoryFragmentsFromSummary(
        botId: String,
        durumText: String,
        hafizaText: String,
        apiKey: String? = null
    ) {
        val bot = try { botDao.getBotById(botId) } catch (_: Exception) { null }
        val defaultSubject = bot?.userCharName?.ifBlank { "kullanıcı" } ?: "kullanıcı"

        hafizaText.lines().map { it.trim() }.filter { it.isNotBlank() }.forEach { line ->
            val clean = line.removePrefix("-").removePrefix("*").removePrefix("•").trim()
            if (clean.length >= 6 && !clean.startsWith("[")) {
                val parts = clean.split(":", limit = 2)
                val key = if (parts.size == 2) parts[0].trim() else MemoryEngine.inferFactKey(clean)
                val value = if (parts.size == 2) parts[1].trim() else clean
                val subject = MemoryEngine.inferFactSubject(value, defaultSubject)
                val confidence = if (clean.contains("(çıkarım)") || clean.contains("(tahmin)")) "inferred" else "certain"

                upsertNeuralVaultFact(
                    botId = botId,
                    subject = subject,
                    key = key,
                    value = value,
                    confidence = confidence,
                    isBotSaved = true,
                    apiKey = apiKey
                )
            }
        }

        val cleanSceneSummary = MemoryEngine.buildConciseCheckpointSummary(
            checkpointNumber = (try { memoryCheckpointDao.getMaxCheckpointNumber(botId) } catch (_: Exception) { 0 } ?: 0) + 1,
            recapEvents = durumText,
            recapEmotions = hafizaText
        )
        if (cleanSceneSummary.length >= 24) {
            try {
                val nextNum = (memoryCheckpointDao.getMaxCheckpointNumber(botId) ?: 0) + 1
                val msgCount = try { messageDao.getMessageCountForBot(botId) } catch (_: Exception) { 0 }
                val cpVec = MemoryEngine.computeTurkishFeatureEmbedding(cleanSceneSummary)
                memoryCheckpointDao.insertCheckpoint(
                    MemoryCheckpointEntity(
                        id = 0L,
                        botId = botId,
                        checkpointNumber = nextNum,
                        messageRangeStart = (msgCount - 20).coerceAtLeast(1),
                        messageRangeEnd = msgCount.coerceAtLeast(1),
                        summaryText = cleanSceneSummary,
                        embedding = floatArrayToJson(cpVec),
                        timestamp = System.currentTimeMillis(),
                        isTemporary = false
                    )
                )
                invalidateMemoryCache(botId)
            } catch (_: Exception) {}
        }
    }

    private fun isContradictingText(t1: String, t2: String): Boolean {
        return MemoryEngine.isContradictingText(t1, t2)
    }






    data class CastMemberAuditResult(
        val member: CastMemberEntity,
        val isLikelyExtra: Boolean,
        val reason: String
    )







    // Ensures a KeyCharacter (manually set in bot config) is mirrored as a CastMemberEntity
    // with isAutoAdded=false so isManuallyAdded=true. Idempotent.


    /** 3.3: Otomatik eklenmiş cast member'ı ID ile sil (blacklist olmadan). */


    /** 4.6: Belirli bir mevsimde gerçekleşmiş hafıza olaylarını getir. */

    // ---------------------------------------------------------------------------
    // 4.4: Mesajlar arası zaman çelişkisi tespiti
    // Son 3 AI mesajını çeker; art arda "akşam" → "sabah" gibi geçiş anlatılmadan
    // gerçekleşen çelişkileri tespit eder ve log'a yazar.
    // ---------------------------------------------------------------------------

    private val DAY_INDICATORS = listOf("sabah oldu", "sabah", "şafak", "güneş doğdu", "gün ağardı")
    private val NIGHT_INDICATORS = listOf("gece oldu", "gece", "karardı", "hava karardı", "ay doğdu", "gece yarısı")
    private val EVENING_INDICATORS = listOf("akşam oldu", "akşam", "günbatımı", "güneş battı")
    private val TRANSITION_MARKERS = listOf("ertesi", "birkaç saat sonra", "zaman geçtikçe", "aradan", "sonra uyu", "uyandı", "sabaha karşı")


    /** Returns the first pending cast member needing user confirmation (3.2). */

    /** Sets isBlacklisted=true so the character is never auto-suggested again (3.3). */






    /** Finds an existing cast member that is an alias of the candidate name. Returns null if ambiguous. */
    private fun findAliasMatch(candidate: String, existingCast: List<com.example.data.local.CastMemberEntity>): com.example.data.local.CastMemberEntity? {
        val matches = existingCast.filter { CastDetectorEngine.isLikelyAlias(candidate, it.name) }
        return if (matches.size == 1) matches[0] else null
    }


    fun getActiveFactsFlow(botId: String) = memoryFactDao.getActiveFactsFlow(botId)
    fun getActiveEventsFlow(botId: String) = memoryEventDao.getActiveEventsFlow(botId)

    suspend fun getRecentEmotionHistory(botId: String, limit: Int = 30): List<EmotionHistoryEntity> =
        emotionHistoryDao.getRecentHistoryForBot(botId, limit)

    suspend fun getRecentCharacterEmotionHistory(botId: String, characterName: String, limit: Int = 20): List<EmotionHistoryEntity> =
        emotionHistoryDao.getRecentHistoryForCharacter(botId, characterName, limit)



    data class CheckpointAccessWindow(
        val fullRecentCheckpoints: List<MemoryCheckpointEntity>,
        val olderRetrievedByEmbedding: List<MemoryCheckpointEntity>
    )

    /**
     * BÖLÜM 2.5: Sadece son 2-3 checkpoint TAM olarak dahil edilir; daha eski checkpoint'lere
     * SADECE embedding benzerliği üzerinden erişilir.
     */


    // Legacy compatibility method

    // --- RAG Test Protocol Execution ---


    fun normalizeCharacterName(rawName: String, definedCharacters: List<KeyCharacter> = emptyList()): String {
        if (rawName.isBlank()) return ""

        val prefixes = listOf(
            "dr.", "dr ", "doctor", "doktor", "mr.", "mr ", "mrs.", "mrs ", "ms.", "ms ",
            "bayan", "bay", "prof.", "prof ", "sir", "lady", "komiser", "dedektif", "kaptan",
            "captain", "ajan", "agent", "yüzbaşı", "teğmen", "başkomiser"
        )

        var cleaned = rawName.trim()
        for (prefix in prefixes) {
            if (cleaned.lowercase().startsWith(prefix)) {
                cleaned = cleaned.substring(prefix.length).trim()
                break
            }
        }

        val lowerCleaned = cleaned.lowercase().replace(Regex("\\s+"), " ")

        for (kc in definedCharacters) {
            val kcNameClean = kc.name.trim()
            val kcLower = kcNameClean.lowercase().replace(Regex("\\s+"), " ")

            if (lowerCleaned == kcLower || lowerCleaned.contains(kcLower) || kcLower.contains(lowerCleaned)) {
                return kcNameClean
            }
        }

        return cleaned.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    suspend fun logEmptyResponse(
        botId: String,
        provider: String,
        model: String,
        finishReason: String,
        rawLength: Int,
        errorMessage: String
    ) {
        try {
            db.emptyResponseLogDao().insertLog(
                com.example.data.local.EmptyResponseLogEntity(
                    botId = botId,
                    provider = provider,
                    model = model,
                    finishReason = finishReason,
                    rawLength = rawLength,
                    errorMessage = errorMessage,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("EmochiRepository", "EmptyResponseLog kaydedilemedi: ${e.message}")
        }
    }

    suspend fun mergeDuplicateCharacterEmotions(botId: String) {
        val bot = botDao.getBotById(botId) ?: return
        val keyChars = parseKeyCharacters(bot.keyCharactersJson)
        val emotions = emotionDao.getEmotionsForBot(botId)
        if (emotions.isEmpty()) return

        val grouped = mutableMapOf<String, MutableList<CharacterEmotionEntity>>()
        for (item in emotions) {
            val normalized = normalizeCharacterName(item.characterName, keyChars)
            grouped.getOrPut(normalized) { mutableListOf() }.add(item)
        }

        for ((normName, list) in grouped) {
            if (list.size > 1) {
                val keep = list.last()
                val updatedKeep = keep.copy(characterName = normName)
                emotionDao.insertOrUpdate(updatedKeep)

                for (toDelete in list) {
                    if (toDelete.id != keep.id) {
                        emotionDao.deleteEmotionById(toDelete.id)
                    }
                }
            } else if (list.first().characterName != normName) {
                emotionDao.insertOrUpdate(list.first().copy(characterName = normName))
            }
        }
    }

    /**
     * Çift cast kayıtlarını birleştirir (ör. "Kerem" + "Kerem Aydın" → tek kayıt).
     * isLikelyAlias ile eşleşen çiftlerde görünme sayıları toplanır, importance/confidence büyüğü korunur.
     * Silinen kaydın adı keyCharactersJson ve emotion tablosundan temizlenir.
     * Hata durumunda mevcut veri bozulmaz (try/catch).
     */

    // --- Prompt & Memory Logic ---

    // =========================================================================
    // ARCHITECTURAL DIRECTIVE: DATA ACCURACY IS NEVER SACRIFICED FOR TOKEN USAGE
    // Token optimization MUST come ONLY from:
    //  - Eliminating boilerplate instructions & redundant text
    //  - Removing duplicated guidelines across prompt sections
    //  - Structured prompt referencing and selective retrieval
    // Token optimization MUST NEVER:
    //  - Merge, drop, or generalize per-entity character states
    //  - Skip validation or classification queries
    //  - Omit CastMemberEntity or CharacterEmotionEntity states
    //  - Truncate JSON keys into incompatible mismatches with the parser
    //
    // LAYERED TOKEN BUDGET PRIORITIZATION:
    //  (1) Current scene context & recent dialog turns [Always Full - P1]
    //  (2) Active in-scene characters' emotion & relationship states [Always Full - P2]
    //  (3) Passive / off-scene characters' relationship states [Compact Status - P3]
    //  (4) Global world state & atmospheric simulation [Structured Reference - P4]
    //  (5) Distant historical memories & background facts [Semantic Retrieval On-Demand - P5]
    // =========================================================================


    private fun worldAtmAtmosphereDescription(w: WorldAtmosphere): String {
        return listOf(w.mood, w.microAtmosphere).filter { it.isNotBlank() }.joinToString(" — ")
    }


    /**
     * STATE_JSON bloğunu modelin cevabından dayanıklı biçimde çıkarır.
     * Öncelik sırası:
     *   1. [[STATE_JSON { ... }]] etiketi (canonical)
     *   2. Birden fazla blok varsa → sonuncusu (modelin son kararı)
     *   3. ```json ... ``` kod bloğu
     *   4. Ham JSON (primary_emotions veya relationship_axes içeren)
     * Kısmi/bozuk JSON → toleranslı düzeltme denenır (tek tırnak, sondaki virgül, eksik kapanış).
     * Hiçbiri başaramazsa null döner → çağıran mevcut state'i korur.
     * Alias çözümü: resolveAliases ile kısa adlar kanonik ada dönüştürülür.
     * Enjeksiyon koruması: Bu fonksiyon yalnızca MODEL cevabından çağrılmalı (user mesajından değil).
     */
    fun extractStateJson(rawResponse: String): JSONObject? {
        if (rawResponse.isBlank()) return null

        fun tryParse(raw: String): JSONObject? {
            return try {
                EmotionEngine.resolveAliases(JSONObject(raw))
            } catch (_: Exception) {
                // Toleranslı düzeltmeler: tek tırnak, sondaki virgül, eksik kapanış parantezi
                try {
                    val fixed = raw
                        .replace(Regex("""'([^']*)'"""), "\"$1\"")   // tek tırnak → çift tırnak
                        .replace(Regex(""",\s*([}\]])"""), "$1")      // sondaki virgül
                        .let { s -> if (!s.trimEnd().endsWith("}")) "$s}" else s } // eksik }
                    EmotionEngine.resolveAliases(JSONObject(fixed))
                } catch (_: Exception) { null }
            }
        }

        // 1. [[STATE_JSON ...]] etiketleri — birden fazlaysa sonuncusu alınır
        val allTagMatches = Regex("""(?is)\[\[STATE(?:_JSON)?\s*(\{.*?\})\s*\]\]""")
            .findAll(rawResponse).toList()
        if (allTagMatches.isNotEmpty()) {
            val last = allTagMatches.last().groupValues[1]
            val obj = tryParse(last)
            if (obj != null) return obj
        }

        // 2. ```json ... ``` kod bloğu
        val codeBlockMatch = Regex("""(?is)```(?:json)?\s*(\{.*?"(?:primary_emotions|relationship_axes|aff|pe|ra)".*?\})\s*```""")
            .find(rawResponse)
        if (codeBlockMatch != null) {
            val obj = tryParse(codeBlockMatch.groupValues[1])
            if (obj != null) return obj
        }

        // 3. Ham JSON (primary_emotions, relationship_axes veya kısa alias içeren)
        val rawJsonMatch = Regex("""(?is)(\{(?:[^{}]|\{[^{}]*\}|\{(?:[^{}]|\{[^{}]*\})*\})*"(?:primary_emotions|relationship_axes|aff|pe|ra)"[^{]*\})""")
            .find(rawResponse)
        if (rawJsonMatch != null) {
            val obj = tryParse(rawJsonMatch.groupValues[1])
            if (obj != null) return obj
        }

        return null
    }

    fun calculateAffectionDifficultyMultiplier(aiName: String, personality: String, scenario: String): Double {
        val combined = "$aiName $personality $scenario".lowercase()
        return when {
            // Power distance / Hierarchy
            combined.contains("patron") || combined.contains("boss") || combined.contains("yönetici") ||
            combined.contains("müdür") || combined.contains("ceo") || combined.contains("öğretmen") ||
            combined.contains("hoca") || combined.contains("profesör") || combined.contains("doktor") ||
            combined.contains("komutan") || combined.contains("subay") || combined.contains("amir") ||
            combined.contains("ünlü") || combined.contains("idol") || combined.contains("kral") ||
            combined.contains("imparator") -> 0.75

            // Neutral / Stranger / Service
            combined.contains("yabancı") || combined.contains("yeni tanış") || combined.contains("müşteri") ||
            combined.contains("barista") || combined.contains("resepsiyonist") || combined.contains("garson") ||
            combined.contains("sürücü") || combined.contains("taksi") -> 0.9

            // Intimate / Childhood / Established bond
            combined.contains("çocukluk arkadaşı") || combined.contains("eski dost") || combined.contains("sevgili") ||
            combined.contains("eş ") || combined.contains("nişanlı") || combined.contains("aşık") ||
            combined.contains("partner") || combined.contains("anne") || combined.contains("baba") ||
            combined.contains("kardeş") || combined.contains("karı") || combined.contains("koca") -> 1.4

            // Peer / Familiar baseline
            else -> 1.1
        }
    }

    suspend fun parseAndApplyEmotionUpdates(botId: String, rawResponse: String, userAlreadyJumped: Boolean = false): String {
        val bot = botDao.getBotById(botId) ?: return cleanEmotionTags(rawResponse)
        val settings = getOrCreateSettings()
        val castList = parseKeyCharacters(bot.keyCharactersJson)
        val totalMsgCount = messageDao.getMessageCountForBot(botId)

        val current = EmotionState.fromJson(bot.emotionState)
        val prevAffection = current.affection
        val prevPeak = maxOf(current.peakAffectionScore, prevAffection)

        val baseMultiplier = if (bot.baseAffectionDifficulty > 0.0) {
            bot.baseAffectionDifficulty
        } else {
            calculateAffectionDifficultyMultiplier(bot.aiName, bot.aiPersonality, bot.scenario)
        }

        // 1. Extract JSON block (Schema V2/V3) — yalnızca model cevabından (enjeksiyon koruması)
        val jsonObj = extractStateJson(rawResponse)

        var parsedDelta = 0
        var parsedReason = ""
        var parsedSetting = "private"
        var parsedMode = "casual"
        var parsedTension = "none"
        // V3.1 yeni alanlar
        var parsedWhy = ""           // delta gerekçesi (why alanı veya delta.reason)
        var parsedTrigIds = emptyList<String>()  // anlamsal tetik kimlikleri
        var parsedReapp = false      // reappraisal aktif mi
        var parsedSuppNew = ""       // suppressed_emotion (supp/supp_emo)

        var rawPrimaryEmotions = current.primaryEmotions
        var rawRelationshipAxes = current.relationshipAxes
        var rawPhysicalComfort = current.physicalComfortScore
        var rawObsession = current.obsessionScore
        var rawDominant = current.dominantEmotion
        var rawSuppressed = current.suppressedEmotion
        var parsedSelfCheck = com.example.data.local.SelfCheckData()
        var parsedWorldState = com.example.data.local.WorldState.fromJson(bot.worldAtmosphere)

        if (jsonObj != null) {
            val primObj = jsonObj.optJSONObject("primary_emotions")
            if (primObj != null) {
                rawPrimaryEmotions = com.example.data.local.PrimaryEmotions.fromJsonObject(primObj)
            }

            val relObj = jsonObj.optJSONObject("relationship_axes")
            if (relObj != null) {
                rawRelationshipAxes = com.example.data.local.RelationshipAxes.fromJsonObject(relObj)
            }

            rawPhysicalComfort = jsonObj.optInt("physicalComfortScore", current.physicalComfortScore).coerceIn(0, 100)
            rawObsession = jsonObj.optInt("obsessionScore", current.obsessionScore).coerceIn(0, 100)
            rawDominant = jsonObj.optString("dominant_emotion", current.dominantEmotion)
            rawSuppressed = jsonObj.optString("suppressed_emotion", current.suppressedEmotion)

            val deltaObj = jsonObj.optJSONObject("delta")
            if (deltaObj != null) {
                parsedDelta = deltaObj.optInt("value", 0)
                parsedReason = deltaObj.optString("reason", "")
                val ctxObj = deltaObj.optJSONObject("context")
                if (ctxObj != null) {
                    parsedSetting = ctxObj.optString("setting", "private")
                    parsedMode = ctxObj.optString("mode", "casual")
                    parsedTension = ctxObj.optString("tension", "none")
                }
            }

            // V3.1: "why" alanı — delta gerekçesi (delta.reason yoksa buradan alınır)
            parsedWhy = jsonObj.optString("why", "").ifBlank { parsedReason }

            // V3.1: "trig" — anlamsal olarak dokunan hassas konu kimlikleri (LLM kararı)
            val trigArr = jsonObj.optJSONArray("trig")
            if (trigArr != null) {
                val ids = mutableListOf<String>()
                for (i in 0 until trigArr.length()) {
                    val id = trigArr.optString(i, "").trim()
                    if (id.isNotBlank()) ids.add(id)
                }
                parsedTrigIds = ids
            }

            // V3.1: "reapp" — reappraisal aktif bayrağı
            parsedReapp = jsonObj.optBoolean("reapp", false)

            // V3.1: "supp" / "supp_emo" — bastırılan duygu (yalnızca geçerli duygu adı kabul edilir)
            val suppRaw = jsonObj.optString("suppressed_emotion", "").ifBlank {
                jsonObj.optString("supp_emo", "")
            }
            parsedSuppNew = suppRaw.ifBlank { rawSuppressed }

            val worldObj = jsonObj.optJSONObject("world_state")
            if (worldObj != null) {
                parsedWorldState = com.example.data.local.WorldState.fromJson(worldObj.toString())
            }

            val storySkipObj = jsonObj.optJSONObject("story_time_skip")
            if (storySkipObj != null && storySkipObj.optBoolean("detected", false)) {
                val daysFromJson = storySkipObj.optInt("days", 0)
                val amountStr = storySkipObj.optString("amount", "")
                val daysToSkip = if (daysFromJson > 0) daysFromJson
                    else TimePerceptionEngine.parseStoryTimeJump(amountStr).daysToAdvance
                val sceneTimeRaw = storySkipObj.optString("scene_time", "")
                val newSceneTime = TimePerceptionEngine.validateSceneTime(sceneTimeRaw)
                // 4.5: Parse başarısız → detected=false gibi davran, log tut
                if (daysToSkip <= 0 && amountStr.isNotBlank()) {
                    android.util.Log.w("TimeSkipParse", "[timeSkipParseFailureLog] provider=${settings.selectedProvider} amount='$amountStr' raw=${rawResponse.take(120)}")
                    try {
                        db.timePerceptionMismatchLogDao().insertLog(
                            com.example.data.local.TimePerceptionMismatchLogEntity(
                                botId = botId,
                                userMessageText = "story_time_skip parse failure",
                                modelResponseText = rawResponse.take(300),
                                detectedTimeExpression = amountStr,
                                codeCalculatedDays = 0,
                                mismatchReason = "timeSkipParseFailure:provider=${settings.selectedProvider} amount='$amountStr'",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    } catch (_: Exception) {}
                }
                // Çift ilerleme önlemi: kullanıcı tarafı zaten ilerlediyse model atlama sayısını uygulama
                val shouldApplyModelJump = daysToSkip > 0 && !userAlreadyJumped
                if (shouldApplyModelJump) {
                    val (newDate, newDayCounter, newAge) = TimePerceptionEngine.advanceCalendarDateAndCheckAge(
                        currentCalendarDateStr = bot.storyCalendarDate,
                        currentDayCounter = bot.storyDayCounter,
                        elapsedDaysToAdd = daysToSkip,
                        birthDateStr = bot.birthDate,
                        initialAge = bot.initialAge
                    )
                    botDao.insertOrUpdate(
                        bot.copy(
                            storyCalendarDate = newDate,
                            storyDayCounter = newDayCounter,
                            currentAge = newAge,
                            sceneTime = newSceneTime ?: bot.sceneTime
                        )
                    )
                    updateCastMembersAgeForBot(bot.id, newDate)
                    // Natural Forgetting Reaper hook whenever TimePerceptionEngine advances storyDayCounter
                    try {
                        val msgCount = try { messageDao.getMessagesForBotList(bot.id).size } catch (_: Exception) { 0 }
                        reapExpiredMemories(bot.id, newDayCounter, msgCount)
                    } catch (_: Exception) {}
                } else if (newSceneTime != null) {
                    botDao.insertOrUpdate(bot.copy(sceneTime = newSceneTime))
                }
            } else if (storySkipObj == null && rawResponse.contains("story_time_skip", ignoreCase = true)) {
                // story_time_skip alanı model çıktısında var ama JSON parse edilemedi
                android.util.Log.w("TimeSkipParse", "[timeSkipParseFailureLog] story_time_skip field present but failed to parse. provider=${settings.selectedProvider}")
                try {
                    db.timePerceptionMismatchLogDao().insertLog(
                        com.example.data.local.TimePerceptionMismatchLogEntity(
                            botId = botId,
                            userMessageText = "story_time_skip json missing",
                            modelResponseText = rawResponse.take(300),
                            detectedTimeExpression = "",
                            codeCalculatedDays = 0,
                            mismatchReason = "timeSkipParseFailure:json_missing provider=${settings.selectedProvider}",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } catch (_: Exception) {}
            }

            val scObj = jsonObj.optJSONObject("self_check")
            if (scObj != null) {
                parsedSelfCheck = com.example.data.local.SelfCheckData.fromJsonObject(scObj)
            }
        } else {
            // Tolerant Regex Parsing for LLM7 or weak JSON models
            val regexDeltaMatch = Regex("""(?i)(?:delta|value|affectionScore|score)\s*[:=]\s*([+-]?\d+)""").find(rawResponse)
            val regexReasonMatch = Regex("""(?i)(?:reason|neden)\s*[:=]\s*["']?([^"\n\r,\]\}]+)["']?""").find(rawResponse)
            
            if (regexDeltaMatch != null) {
                parsedDelta = regexDeltaMatch.groupValues[1].toIntOrNull() ?: 0
                parsedReason = regexReasonMatch?.groupValues[1]?.trim() ?: "Regex Tolerant Parsing"
            } else {
                parsedDelta = 0
            }

            try {
                malformedOutputLogDao.insertLog(
                    com.example.data.local.MalformedOutputLogEntity(
                        botId = botId,
                        provider = "llm7",
                        model = "default",
                        rawOutput = rawResponse.take(1000),
                        errorMessage = if (regexDeltaMatch != null) "JSON_SYNTAX_ERROR_RECOVERED_BY_REGEX" else "JSON_AND_REGEX_PARSING_FAILED",
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) {}
        }

        // 2. Personality Profile Multipliers & Multi-dimensional Clamping
        val personality = if (bot.baselineEmotionAnalysisJson.isNotBlank()) {
            try {
                BaselineEmotionEngine.parseAnalysisJson(
                    bot.baselineEmotionAnalysisJson, bot.id, bot.aiName, bot.aiPersonality,
                    bot.scenario, bot.userCharName, bot.userCharDesc
                ).personalityProfile
            } catch (_: Exception) {
                com.example.data.local.PersonalityProfile.deriveFromPersonality(bot.aiName, bot.aiPersonality, bot.scenario)
            }
        } else {
            com.example.data.local.PersonalityProfile.deriveFromPersonality(bot.aiName, bot.aiPersonality, bot.scenario)
        }

        var pAffectionMult = 1.0
        var pTrustMult = 1.0
        var pResentmentMult = 1.0
        var pFearMult = 1.0
        var pJoyMult = 1.0

        if (personality.neuroticism > 70) {
            pFearMult *= 1.35
            pResentmentMult *= 1.35
        }
        if (personality.agreeableness > 70) {
            pTrustMult *= 1.2
            pResentmentMult *= 0.7
        }
        if (personality.extraversion > 70) {
            pJoyMult *= 1.3
        }

        when (personality.attachmentStyle) {
            "anxious" -> {
                pAffectionMult *= 1.15
                pFearMult *= 1.25
            }
            "avoidant" -> {
                pAffectionMult *= 0.65
            }
            "fearful_avoidant" -> {
                pAffectionMult *= 0.85
                pFearMult *= 1.3
            }
        }

        // 3. World State & Context Multipliers
        val settingMult = ContextMultiplierConfig.getSettingMultiplier(parsedSetting)
        val modeMult = ContextMultiplierConfig.getModeMultiplier(parsedMode)
        val tensionMult = ContextMultiplierConfig.getTensionMultiplier(parsedTension)
        val combinedContextMult = settingMult * modeMult * tensionMult

        if (parsedWorldState.macro.globalTensionLevel > 50) {
            pTrustMult *= 0.75
        }

        // Crisis tension locks affection & comfort delta to 0
        if (parsedTension == "crisis" || parsedWorldState.micro.sceneTension == "crisis") {
            parsedDelta = 0
            pAffectionMult = 0.0
        }

        // Fear > 50 slows affection gain by 50%
        if (rawPrimaryEmotions.fear > 50) {
            pAffectionMult *= 0.5
        }

        // Recovery lock
        var nextRecoveryLock = current.recoveryLockUntilMessageCount
        if (parsedDelta <= -15) {
            val dropMagnitude = -parsedDelta
            val lockAdd = if (dropMagnitude > 25) 15 else 8
            nextRecoveryLock = maxOf(nextRecoveryLock, totalMsgCount + lockAdd)
        }

        val regCount = getRegenerateCount(botId)
        val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val todayStr = sdfDate.format(java.util.Date())
        val currentDailyGain = if (current.lastGainResetDate == todayStr) current.dailyAffectionGain else 0

        val isEstablishedRomantic = current.affection >= 60 ||
                listOf("eş", "eşim", "sevgili", "sevgilim", "aşık", "partner", "evli", "nişanlı", "koca", "karı", "gelin", "damat")
                    .any { term -> "${bot.aiName} ${bot.aiPersonality} ${bot.scenario}".lowercase().contains(term) }

        // V3.1: "why" gerekçesi yoksa büyük pozitif delta daha sıkı kısıtlanır
        // (modelin gerekçesiz ani sıçrama yapmasını engeller)
        val effectiveParsedDelta = if (parsedDelta > 8 && parsedWhy.isBlank() && parsedReason.isBlank()) {
            (parsedDelta * 0.5).toInt().coerceAtLeast(1)
        } else parsedDelta

        // V3.1: "trig" — anlamsal tetik konuları → mevcut sensitiveTrigger mekanizmasıyla entegre
        // Model kendi cevabında "trig": ["konu1", "konu2"] döndürür (anlamsal karar LLM'de).
        // Kod yalnızca bilinen triggerTopic'leri kabul eder; bilinmeyenler göz ardı edilir.
        // Anahtar kelime tespiti yedek olarak mevcut evaluateSensitiveTriggers içinde çalışmaya devam eder.
        if (parsedTrigIds.isNotEmpty()) {
            try {
                val knownTriggers = sensitiveTriggerDao.getTriggersForBot(botId)
                val knownTopics = knownTriggers.associateBy { it.triggerTopic.lowercase() }
                for (trigTopic in parsedTrigIds) {
                    val trigger = knownTopics[trigTopic.lowercase()]
                    if (trigger != null) {
                        android.util.Log.d("EmotionEngine", "[trig] Semantic trigger activated: ${trigger.triggerTopic} (mult=${trigger.intensityMultiplier})")
                        // Tetik sayacını güncelle
                        sensitiveTriggerDao.insertOrUpdate(
                            trigger.copy(recentTriggerCount = trigger.recentTriggerCount + 1)
                        )
                        // Tetik efekti: intensityMultiplier > 1 ise delta zaten model tarafından uygulandı.
                        // Burada yalnızca kayıt tutulur; kod delta'ya dokunmaz (LLM kararı).
                    }
                }
            } catch (_: Exception) {}
        }

        // V3.1: "reapp" — reappraisal aktifse kaydını al
        // LLM "reapp": true döndürdüğünde bir olayın yeniden yorumlandığını bildirir.
        // recordPendingReappraisalIfEligible yalnızca rawDelta <= -20 için çalışır,
        // bu yüzden pozitif reappraisal durumlarında (delta >= 0) sadece log yazılır.
        if (parsedReapp && effectiveParsedDelta <= -10) {
            try {
                recordPendingReappraisalIfEligible(
                    botId = botId,
                    eventDescription = parsedWhy.ifBlank { parsedReason }.ifBlank { "reappraisal" },
                    emotionStateJson = current.toJson(),
                    currentMessageIndex = totalMsgCount,
                    rawDelta = effectiveParsedDelta
                )
            } catch (_: Exception) {}
        }

        val clampedDelta = calculateClampedDelta(
            prevAffection = prevAffection,
            rawDelta = effectiveParsedDelta,
            totalMsgCount = totalMsgCount,
            baseMultiplier = baseMultiplier,
            combinedContextMult = combinedContextMult,
            personalityAffectionMult = pAffectionMult,
            recoveryLockUntilMessageCount = current.recoveryLockUntilMessageCount,
            dailyAffectionGain = currentDailyGain,
            consecutivePositiveCount = current.consecutivePositiveCount,
            regCount = regCount,
            isEstablishedRomantic = isEstablishedRomantic
        )

        var nextAffection = (prevAffection + clampedDelta).coerceIn(0, 100)

        // Resentment >= 70 caps affectionScore at 50 max
        if (rawRelationshipAxes.resentmentScore >= 70) {
            nextAffection = minOf(nextAffection, 50)
        }

        // 4. Compute Dominant Emotion & Secondary Emotions (Plutchik Dyads)
        var finalDominant = rawDominant
        if (rawPrimaryEmotions.anger > 60 && rawPrimaryEmotions.trust > 60) {
            finalDominant = "hurt" // Hayal kırıklığı / Kırgınlık
        }

        val joyVal = rawPrimaryEmotions.joy
        val trustVal = (rawPrimaryEmotions.trust * pTrustMult).roundToInt().coerceIn(0, 100)
        val fearVal = (rawPrimaryEmotions.fear * pFearMult).roundToInt().coerceIn(0, 100)
        val angerVal = (rawPrimaryEmotions.anger * pResentmentMult).roundToInt().coerceIn(0, 100)
        val sadnessVal = rawPrimaryEmotions.sadness
        val antVal = rawPrimaryEmotions.anticipation
        val surVal = rawPrimaryEmotions.surprise
        val disVal = rawPrimaryEmotions.disgust

        val computedSecondary = when {
            joyVal > 50 && trustVal > 50 && nextAffection >= 60 -> "love"
            trustVal > 50 && fearVal > 50 -> "submission"
            angerVal > 50 && disVal > 50 -> "contempt"
            angerVal > 50 && antVal > 50 -> "aggressiveness"
            sadnessVal > 50 && surVal > 50 -> "disapproval"
            joyVal > 50 && fearVal > 50 -> "guilt"
            else -> null
        }

        // Defense Mechanism Selection
        val activeDefense = if (angerVal > 50 || fearVal > 50 || sadnessVal > 50) {
            when {
                personality.agreeableness > 60 && personality.conscientiousness > 60 -> "inkâr (denial)"
                personality.neuroticism > 60 -> "yansıtma (projection)"
                personality.attachmentStyle == "avoidant" -> "geri çekilme (withdrawal)"
                personality.openness > 60 -> "entelektüelleştirme (intellectualization)"
                else -> "none"
            }
        } else "none"

        // 5. Build Final Updated EmotionState & WorldState
        val finalPrimaryEmotions = rawPrimaryEmotions.copy(
            trust = trustVal,
            fear = fearVal,
            anger = angerVal
        )

        val finalRelationshipAxes = rawRelationshipAxes.copy(
            affectionScore = nextAffection,
            resentmentScore = (rawRelationshipAxes.resentmentScore * pResentmentMult).roundToInt().coerceIn(0, 100)
        )

        val newConsecutiveCount = if (clampedDelta > 0) current.consecutivePositiveCount + 1 else 0
        val newDailyGain = if (clampedDelta > 0) currentDailyGain + clampedDelta else currentDailyGain
        val newPeak = maxOf(prevPeak, nextAffection)

        val preConsistencyState = current.copy(
            primaryEmotions = finalPrimaryEmotions,
            relationshipAxes = finalRelationshipAxes,
            physicalComfortScore = rawPhysicalComfort,
            obsessionScore = rawObsession,
            dominantEmotion = finalDominant,
            suppressedEmotion = parsedSuppNew.ifBlank { rawSuppressed },
            computedSecondaryEmotion = computedSecondary,
            defenseMechanism = activeDefense,
            deltaAxis = "affectionScore",
            deltaValue = clampedDelta,
            deltaReason = parsedWhy.ifBlank { parsedReason },
            setting = parsedSetting,
            mode = parsedMode,
            tension = parsedTension,
            consecutivePositiveCount = newConsecutiveCount,
            dailyAffectionGain = newDailyGain,
            lastGainResetDate = todayStr,
            peakAffectionScore = newPeak,
            recoveryLockUntilMessageCount = nextRecoveryLock
        )

        // [4] Tutarlılık denetimi: sayısal çelişkiler koda yakalanır, büyük çelişki sonraki prompt'a not olarak girer
        // self_check.didIContradictRecentEmotionalState = true ise ağırlıklı olarak ele alınır
        val consistencyResult = EmotionEngine.enforcePsychologicalConsistencyWithNote(preConsistencyState)
        val finalUpdatedState = consistencyResult.state

        // Tutarlılık notu: bir sonraki prompt'ta kullanılmak üzere saklanır
        val newConsistencyNote = consistencyResult.contradictionNote
        if (newConsistencyNote.isNotBlank() || parsedSelfCheck.didIContradictRecentEmotionalState) {
            val note = if (newConsistencyNote.isNotBlank()) newConsistencyNote
            else "Önceki state çelişkiliydi (model kendi self_check'inde işaretledi). Tutarlı bir state üret."
            pendingConsistencyNoteMap[botId] = note
        } else {
            // Çelişki yoksa önceki notu temizle
            pendingConsistencyNoteMap.remove(botId)
        }

        // [5] Pasif mikro-sönüm: hikâye atlaması olmayan sıradan mesajlarda çok yavaş soğuma
        val afterPassiveDecay = EmotionEngine.applyPassiveDecayPerMessage(
            currentState = finalUpdatedState,
            messageIndex = totalMsgCount,
            regulationCapacity = bot.emotionalRegulationCapacity
        )
        val finalUpdatedStateWithDecay = afterPassiveDecay

        // 6. Record to Bot Entity, Emotion History & Self Check Failure Log
        try {
            // CRITICAL FIX: Persist updated emotionState and worldAtmosphere back to botDao!
            val updatedBot = botDao.getBotById(botId) ?: bot
            botDao.insertOrUpdate(
                updatedBot.copy(
                    emotionState = finalUpdatedStateWithDecay.toJson(),
                    previousEmotionState = current.toJson(),
                    worldAtmosphere = parsedWorldState.toJson(),
                    updatedAt = System.currentTimeMillis()
                )
            )

            // [6b] side_character_updates JSON parse — aktif cast filtresiyle
            // Her iki yol da desteklenir: STATE_JSON.side_character_updates listesi VE [CHARACTER_EMOTION:] blokları
            val MAX_SIDE_CHAR_UPDATES = 8
            if (jsonObj != null) {
                val sideArr = jsonObj.optJSONArray("side_character_updates")
                if (sideArr != null) {
                    // Son mesajlarda adı geçen aktif cast'i belirle
                    val recentText = try {
                        messageDao.getMessagesForBotList(botId).takeLast(4).joinToString(" ") { it.text }
                    } catch (_: Exception) { "" }
                    val confirmedCast = try {
                        castMemberDao.getCastMembersForBot(botId).filter { !it.pendingUserConfirmation }
                    } catch (_: Exception) { emptyList() }

                    val seenNames = mutableSetOf<String>()
                    val limit = minOf(sideArr.length(), MAX_SIDE_CHAR_UPDATES)
                    for (i in 0 until limit) {
                        val entry = sideArr.optJSONObject(i) ?: continue
                        val rawName = entry.optString("name", "").ifBlank { entry.optString("character", "") }.trim()
                        val cleanName = cleanCharacterNameCandidate(rawName)
                        if (cleanName.isBlank()) continue
                        if (isMainOrUserCharacter(rawName, bot) || isMainOrUserCharacter(cleanName, bot)) continue
                        // Yalnızca sahnede olan karakterler güncellenir
                        val isInScene = confirmedCast.any { cm ->
                            cm.name.equals(cleanName, ignoreCase = true) &&
                            recentText.contains(cm.name, ignoreCase = true)
                        }
                        if (!isInScene) continue
                        // Aynı mesajda iki kez raporlanmışsa yalnızca ilki işlenir
                        val nameLower = cleanName.lowercase()
                        if (!seenNames.add(nameLower)) continue

                        val existingCharEntity = emotionDao.getEmotionForCharacter(botId, cleanName)
                        val existingState = existingCharEntity
                            ?.let { EmotionState.fromJson(it.emotionState) }
                            ?: EmotionState.calculateBaselineEmotionState(bot.aiName, cleanName, "")
                        val castMem = confirmedCast.firstOrNull { it.name.equals(cleanName, ignoreCase = true) }
                        val sideRoleMult = if (castMem != null && castMem.baseAffectionDifficultyMultiplier > 0.0) {
                            determineEffectiveRoleMultiplier(castMem, rawResponse.take(200))
                        } else {
                            calculateCastMemberDifficultyMultiplier("Yan Karakter", cleanName, rawResponse.take(200))
                        }

                        val rawAffDelta = entry.optInt("affection_delta", 0)
                        val rawTrustDelta = entry.optInt("trust_delta", 0)
                        val moodStr = entry.optString("mood", existingState.dominantEmotion)
                        val sidePrevAff = existingState.affection
                        val sidePrevDailyGain = if (castMem?.lastGainResetDate == todayStr) castMem.dailyAffectionGain else 0
                        val sideConsecutiveCount = castMem?.consecutivePositiveCount ?: 0
                        val sideRecoveryLock = castMem?.recoveryLockUntilMessageCount ?: 0

                        val clampedSideAff = calculateClampedDelta(
                            prevAffection = sidePrevAff,
                            rawDelta = rawAffDelta.coerceIn(-50, 50),
                            totalMsgCount = totalMsgCount,
                            baseMultiplier = sideRoleMult,
                            combinedContextMult = combinedContextMult,
                            personalityAffectionMult = 1.0,
                            recoveryLockUntilMessageCount = sideRecoveryLock,
                            dailyAffectionGain = sidePrevDailyGain,
                            consecutivePositiveCount = sideConsecutiveCount,
                            regCount = regCount,
                            isEstablishedRomantic = sidePrevAff >= 70
                        )
                        val clampedSideTrust = (rawTrustDelta.coerceIn(-50, 50) * sideRoleMult).toInt().coerceIn(-25, 15)
                        val newSideAff = (sidePrevAff + clampedSideAff).coerceIn(0, 100)
                        val newSideTrust = (existingState.trust + clampedSideTrust).coerceIn(0, 100)

                        val updatedCharState = existingState.copy(
                            dominantEmotion = moodStr,
                            primaryEmotions = existingState.primaryEmotions.copy(trust = newSideTrust),
                            relationshipAxes = existingState.relationshipAxes.copy(affectionScore = newSideAff)
                        )
                        val charEntityToSave = existingCharEntity
                            ?.copy(characterName = cleanName, emotionState = updatedCharState.toJson())
                            ?: CharacterEmotionEntity(botId = botId, characterName = cleanName, emotionState = updatedCharState.toJson())
                        emotionDao.insertOrUpdate(charEntityToSave)

                        if (castMem != null) {
                            val sideNewConsec = if (clampedSideAff > 0) sideConsecutiveCount + 1 else 0
                            val sideNewDaily = if (clampedSideAff > 0) sidePrevDailyGain + clampedSideAff else sidePrevDailyGain
                            castMemberDao.updateCastMember(
                                castMem.copy(
                                    affectionScore = newSideAff,
                                    consecutivePositiveCount = sideNewConsec,
                                    dailyAffectionGain = sideNewDaily,
                                    lastGainResetDate = todayStr,
                                    peakAffectionScore = maxOf(castMem.peakAffectionScore, newSideAff),
                                    recoveryLockUntilMessageCount = sideRecoveryLock
                                )
                            )
                        }
                    }
                }
            }

            // Parse Side Character Emotion Blocks [CHARACTER_EMOTION: {name}] ... [/CHARACTER_EMOTION]
            // Also supports closing-tag-only variant: Birincil:... Yakınlık:... [[/CHARACTER_EMOTION: Name]]
            val pairedRegex = Regex("""(?is)\[\[?CHARACTER_EMOTION:\s*([^\]\n]+)\]\]?(.*?)\[\[?/(?:CHARACTER_EMOTION|CHARACTER)(?::\s*[^\]\n]+)?\]\]?""")
            val closingOnlyRegex = Regex("""(?is)(?:^|\n)((?:[^\n]+\n){1,6})\s*\[\[?/(?:CHARACTER_EMOTION|CHARACTER):\s*([^\]\n]+)\]\]?""")

            data class CharBlockMatch(val rawCharName: String, val blockText: String)
            val detectedBlocks = mutableListOf<CharBlockMatch>()

            for (m in pairedRegex.findAll(rawResponse)) {
                detectedBlocks.add(CharBlockMatch(m.groupValues[1].trim(), m.groupValues[2]))
            }
            for (m in closingOnlyRegex.findAll(rawResponse)) {
                val name = m.groupValues[2].trim()
                if (detectedBlocks.none { it.rawCharName.equals(name, ignoreCase = true) }) {
                    detectedBlocks.add(CharBlockMatch(name, m.groupValues[1]))
                }
            }

            for (item in detectedBlocks) {
                val rawCharName = item.rawCharName
                val blockText = item.blockText
                val cleanCharName = cleanCharacterNameCandidate(rawCharName)

                if (cleanCharName.isNotBlank() && !isMainOrUserCharacter(rawCharName, bot) && !isMainOrUserCharacter(cleanCharName, bot)) {
                    ensureSideCharacterRegistered(
                        botId = botId,
                        charName = cleanCharName,
                        role = "Yan Karakter",
                        cueSentence = rawResponse.take(200)
                    )

                    val existingCharEntity = emotionDao.getEmotionForCharacter(botId, cleanCharName)
                    val existingState = existingCharEntity?.let { EmotionState.fromJson(it.emotionState) } ?: EmotionState.calculateBaselineEmotionState(bot.aiName, cleanCharName, "")

                    val castMem = castMemberDao.findByName(botId, cleanCharName)
                    val sideRoleMult = if (castMem != null && castMem.baseAffectionDifficultyMultiplier > 0.0) {
                        determineEffectiveRoleMultiplier(castMem, rawResponse.take(200))
                    } else {
                        calculateCastMemberDifficultyMultiplier("Yan Karakter", cleanCharName, rawResponse.take(200))
                    }

                    // JSON-first parse: try to extract a JSON object from the block text.
                    // Falls back to regex parsing if JSON is absent or malformed.
                    fun repairJson(raw: String): org.json.JSONObject? = try {
                        org.json.JSONObject(raw)
                    } catch (_: Exception) {
                        try {
                            val fixed = raw
                                .replace(Regex("""'([^']*)'"""), "\"$1\"")
                                .replace(Regex(""",\s*([}\]])"""), "$1")
                                .let { s -> if (!s.trimEnd().endsWith("}")) "$s}" else s }
                            org.json.JSONObject(fixed)
                        } catch (_: Exception) { null }
                    }
                    val jsonInBlock = Regex("""\{[^{}]{5,500}\}""").findAll(blockText)
                        .mapNotNull { repairJson(it.value) }
                        .firstOrNull { it.has("mood") || it.has("affection_delta") || it.has("aff") || it.has("trust_delta") }

                    val moodMatch: String
                    val parsedRawAff: Int?
                    val parsedRawTrust: Int?
                    val parsedRawTension: Int?
                    val parsedRawHurt: Int?
                    val isAffAbsolute: Boolean
                    val isTrustAbsolute: Boolean

                    if (jsonInBlock != null) {
                        moodMatch = jsonInBlock.optString("mood", jsonInBlock.optString("dominant_emotion", existingState.dominantEmotion))
                        parsedRawAff = jsonInBlock.optInt("affection_delta", jsonInBlock.optInt("aff", Int.MIN_VALUE)).takeIf { it != Int.MIN_VALUE }
                        parsedRawTrust = jsonInBlock.optInt("trust_delta", jsonInBlock.optInt("trust", Int.MIN_VALUE)).takeIf { it != Int.MIN_VALUE }
                        parsedRawTension = jsonInBlock.optInt("tension_delta", Int.MIN_VALUE).takeIf { it != Int.MIN_VALUE }
                        parsedRawHurt = jsonInBlock.optInt("hurt_delta", jsonInBlock.optInt("resentment", Int.MIN_VALUE)).takeIf { it != Int.MIN_VALUE }
                        isAffAbsolute = jsonInBlock.has("affection") && !jsonInBlock.has("affection_delta")
                        isTrustAbsolute = jsonInBlock.has("trust") && !jsonInBlock.has("trust_delta")
                    } else {
                        // Regex fallback (handles serbest-metin responses)
                        moodMatch = Regex("""(?i)(?:mood|birincil|primary_emotion|dominant_emotion)\s*:\s*([^\n\r,\|\]]+)""").find(blockText)?.groupValues?.get(1)?.trim() ?: existingState.dominantEmotion
                        parsedRawAff = Regex("""(?i)(?:affection|affection_delta|aff|yakınlık|yakinlik)\s*:\s*([+-]?\d+)""").find(blockText)?.groupValues?.get(1)?.toIntOrNull()
                        parsedRawTrust = Regex("""(?i)(?:trust|trust_delta|güven|guven)\s*:\s*([+-]?\d+)""").find(blockText)?.groupValues?.get(1)?.toIntOrNull()
                        parsedRawTension = Regex("""(?i)(?:tension|tension_delta|gerilim)\s*:\s*([+-]?\d+)""").find(blockText)?.groupValues?.get(1)?.toIntOrNull()
                        parsedRawHurt = Regex("""(?i)(?:hurt|hurt_delta|kırgınlık|kirginlik|resentment)\s*:\s*([+-]?\d+)""").find(blockText)?.groupValues?.get(1)?.toIntOrNull()
                        isAffAbsolute = blockText.contains("/100") || (parsedRawAff != null && !blockText.contains(Regex("""(?i)(?:affection|yakınlık|yakinlik)\s*:\s*[+-]""")) && parsedRawAff in 1..100)
                        isTrustAbsolute = blockText.contains("/100") || (parsedRawTrust != null && !blockText.contains(Regex("""(?i)(?:trust|güven|guven)\s*:\s*[+-]""")) && parsedRawTrust in 1..100)
                    }

                    val rawAffDelta = if (parsedRawAff == null) 0 else if (isAffAbsolute) (parsedRawAff - existingState.affection).coerceIn(-50, 50) else parsedRawAff.coerceIn(-50, 50)
                    val rawTrustDelta = if (parsedRawTrust == null) 0 else if (isTrustAbsolute) (parsedRawTrust - existingState.trust).coerceIn(-50, 50) else parsedRawTrust.coerceIn(-50, 50)
                    val rawTensionDelta = parsedRawTension?.coerceIn(-50, 50) ?: 0
                    val rawHurtDelta = parsedRawHurt?.coerceIn(-50, 50) ?: 0

                    val sidePrevAff = existingState.affection
                    val sidePrevDailyGain = if (castMem?.lastGainResetDate == todayStr) castMem.dailyAffectionGain else 0
                    val sideConsecutiveCount = castMem?.consecutivePositiveCount ?: 0
                    val sideRecoveryLock = castMem?.recoveryLockUntilMessageCount ?: 0

                    val clampedAffDelta = calculateClampedDelta(
                        prevAffection = sidePrevAff,
                        rawDelta = rawAffDelta,
                        totalMsgCount = totalMsgCount,
                        baseMultiplier = sideRoleMult,
                        combinedContextMult = combinedContextMult,
                        personalityAffectionMult = 1.0,
                        recoveryLockUntilMessageCount = sideRecoveryLock,
                        dailyAffectionGain = sidePrevDailyGain,
                        consecutivePositiveCount = sideConsecutiveCount,
                        regCount = regCount,
                        isEstablishedRomantic = sidePrevAff >= 70
                    )

                    val clampedTrustDelta = (rawTrustDelta * sideRoleMult).roundToInt().coerceIn(-25, 15)
                    val newAff = if (isAffAbsolute && parsedRawAff != null) parsedRawAff.coerceIn(0, 100) else (sidePrevAff + clampedAffDelta).coerceIn(0, 100)
                    val newTrust = if (isTrustAbsolute && parsedRawTrust != null) parsedRawTrust.coerceIn(0, 100) else (existingState.trust + clampedTrustDelta).coerceIn(0, 100)
                    val newTension = (existingState.tension.toIntOrNull()?.plus(rawTensionDelta) ?: 10).coerceIn(0, 100)

                    val updatedCharState = existingState.copy(
                        dominantEmotion = moodMatch,
                        primaryEmotions = existingState.primaryEmotions.copy(trust = newTrust),
                        relationshipAxes = existingState.relationshipAxes.copy(affectionScore = newAff),
                        tension = newTension.toString()
                    )

                    val charEntityToSave = existingCharEntity?.copy(characterName = cleanCharName, emotionState = updatedCharState.toJson())
                        ?: CharacterEmotionEntity(botId = botId, characterName = cleanCharName, emotionState = updatedCharState.toJson())
                    emotionDao.insertOrUpdate(charEntityToSave)

                    // Update CastMemberEntity tracking fields
                    if (castMem != null) {
                        val sideNewConsecutive = if (clampedAffDelta > 0) sideConsecutiveCount + 1 else 0
                        val sideNewDailyGain = if (clampedAffDelta > 0) sidePrevDailyGain + clampedAffDelta else sidePrevDailyGain
                        val sideNewPeak = maxOf(castMem.peakAffectionScore, newAff)
                        var nextSideLock = sideRecoveryLock
                        if (rawAffDelta <= -15) {
                            val dropMag = -rawAffDelta
                            val lockAdd = if (dropMag > 25) 15 else 8
                            nextSideLock = maxOf(nextSideLock, totalMsgCount + lockAdd)
                        }
                        castMemberDao.updateCastMember(
                            castMem.copy(
                                affectionScore = newAff,
                                consecutivePositiveCount = sideNewConsecutive,
                                dailyAffectionGain = sideNewDailyGain,
                                lastGainResetDate = todayStr,
                                peakAffectionScore = sideNewPeak,
                                recoveryLockUntilMessageCount = nextSideLock
                            )
                        )
                    }
                    reEvaluateCastMemberRolesIfNeeded(botId, totalMsgCount, rawResponse.take(250))

                    // Save rich record to EmotionHistoryEntity for side character
                    emotionHistoryDao.insertHistory(
                        EmotionHistoryEntity(
                            botId = botId,
                            characterName = cleanCharName,
                            timestamp = System.currentTimeMillis(),
                            messageIndex = totalMsgCount,
                            affectionScore = newAff,
                            respectScore = updatedCharState.relationshipAxes.respectScore,
                            comfortScore = updatedCharState.relationshipAxes.comfortScore,
                            resentmentScore = updatedCharState.relationshipAxes.resentmentScore,
                            trustScore = newTrust,
                            physicalComfortScore = updatedCharState.physicalComfortScore,
                            obsessionScore = updatedCharState.obsessionScore,
                            dominantEmotion = moodMatch,
                            fullVectorJson = updatedCharState.toJson()
                        )
                    )
                }
            }

            // Parse Inline Tags [CHARACTER_EMOTION: Name], [CAST_MEMBER: Name | Role], [CHARACTER: Name]
            val inlineTagRegex = Regex("""(?is)\[\[?(?:CHARACTER_EMOTION|CAST_MEMBER|CHARACTER):\s*([^\]\n]+)\]\]?""")
            inlineTagRegex.findAll(rawResponse).forEach { match ->
                val fullVal = match.groupValues[1].trim()
                if (!fullVal.contains("[/CHARACTER_EMOTION]", ignoreCase = true)) {
                    val parts = fullVal.split("|", ",")
                    val rawCharName = parts.firstOrNull()?.trim() ?: ""
                    val inferredRole = if (parts.size > 1) parts[1].trim() else "Yan Karakter"
                    val cleanCharName = cleanCharacterNameCandidate(rawCharName)
                    if (cleanCharName.isNotBlank() && !isMainOrUserCharacter(rawCharName, bot) && !isMainOrUserCharacter(cleanCharName, bot)) {
                        ensureSideCharacterRegistered(
                            botId = botId,
                            charName = cleanCharName,
                            role = inferredRole,
                            cueSentence = rawResponse.take(200)
                        )
                    }
                }
            }

            emotionHistoryDao.insertHistory(
                EmotionHistoryEntity(
                    botId = botId,
                    timestamp = System.currentTimeMillis(),
                    messageIndex = totalMsgCount,
                    affectionScore = finalUpdatedStateWithDecay.affection,
                    respectScore = finalUpdatedStateWithDecay.relationshipAxes.respectScore,
                    comfortScore = finalUpdatedStateWithDecay.relationshipAxes.comfortScore,
                    resentmentScore = finalUpdatedStateWithDecay.relationshipAxes.resentmentScore,
                    trustScore = finalUpdatedStateWithDecay.primaryEmotions.trust,
                    physicalComfortScore = finalUpdatedStateWithDecay.physicalComfortScore,
                    obsessionScore = finalUpdatedStateWithDecay.obsessionScore,
                    dominantEmotion = finalUpdatedStateWithDecay.dominantEmotion,
                    fullVectorJson = finalUpdatedStateWithDecay.toJson()
                )
            )

            if (!parsedSelfCheck.isValid) {
                val failedList = mutableListOf<String>()
                if (!parsedSelfCheck.isDeltaJustifiedByScene) failedList.add("is_delta_justified_by_scene")
                if (!parsedSelfCheck.isExpressionConsistentWithAttachmentStyle) failedList.add("is_expression_consistent_with_attachment_style")
                if (parsedSelfCheck.didISkipAStage) failedList.add("did_i_skip_a_stage")
                if (parsedSelfCheck.didIContradictRecentEmotionalState) failedList.add("did_i_contradict_recent_emotional_state")

                selfCheckFailureLogDao.insertLog(
                    SelfCheckFailureLogEntity(
                        botId = botId,
                        messageIndex = totalMsgCount,
                        failedChecksJson = org.json.JSONArray(failedList).toString(),
                        modelResponseText = rawResponse.take(500),
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            // [8] Arka plan denetimi tetikleme: her ~50 mesajda bir, state belirgin kaymışsa
            try {
                val lastAuditAt = lastAuditMessageCountMap[botId] ?: 0
                val jitter = (botId.hashCode() % 10).let { if (it < 0) -it else it }  // 0–9 jitter
                val auditInterval = 50 + jitter
                if (totalMsgCount - lastAuditAt >= auditInterval) {
                    val recentHistory = emotionHistoryDao.getRecentHistoryForBot(botId, limit = 10)
                    if (recentHistory.size >= 5) {
                        val affValues = recentHistory.map { it.affectionScore }
                        val deltaSum = (affValues.last() - affValues.first())
                        // Yalnızca state belirgin kaymışsa denetim tetiklenir
                        if (kotlin.math.abs(deltaSum) >= 20) {
                            lastAuditMessageCountMap[botId] = totalMsgCount
                            // Arka plan denetimini WorkManager ile tetikle
                            val inputData = androidx.work.Data.Builder()
                                .putString("botId", botId)
                                .build()
                            val auditRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.work.BackgroundEmotionAuditWorker>()
                                .setInputData(inputData)
                                .setConstraints(
                                    androidx.work.Constraints.Builder()
                                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                                        .build()
                                )
                                .build()
                            context?.let { ctx ->
                                androidx.work.WorkManager.getInstance(ctx).enqueue(auditRequest)
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        } catch (_: Exception) {}

        // Clean character emotion duplicates in database
        try {
            mergeDuplicateCharacterEmotions(botId)
        } catch (_: Exception) {}

        val cleanedText = cleanEmotionTags(rawResponse)

        // Section 7: Code-Level Romantic Term Scan & Violation Logging
        val romanticTerms = listOf("canım", "tatlım", "aşkım", "seni seviyorum", "kalbim", "birtanem", "sevgilim", "bebeğim", "bebeim", "bebegim", "aşkımm")
        val lowerCleaned = cleanedText.lowercase()
        val hasForbiddenTerm = romanticTerms.any { lowerCleaned.contains(it) }

        if (hasForbiddenTerm && (nextAffection < 61 || totalMsgCount < 5)) {
            val violationType = if (totalMsgCount < 5) "EARLY_MESSAGE_ROMANTIC_TERM" else "ROMANTIC_TERM_BELOW_THRESHOLD"
            promptViolationLogDao.insertLog(
                PromptViolationLogEntity(
                    botId = botId,
                    messageText = cleanedText.take(200),
                    violationType = violationType,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        return cleanedText
    }

    // =====================================================
    // BÖLÜM F — DUYGU DÜZENLEME KAPASİTESİ (EmotionalRegulationCapacity)
    // =====================================================
    data class EmotionalRegulationOutput(
        val isRegulationActive: Boolean,
        val rawDelta: Int,
        val outwardExpressedDelta: Int,
        val capacity: Int
    )

    fun calculateEmotionalRegulation(rawDelta: Int, capacity: Int): EmotionalRegulationOutput {
        val absDelta = kotlin.math.abs(rawDelta)
        // ÖNEMLİ DENGELEME: Bu katman SADECE tek seferlik büyük sıçramalarda (|delta| > 25) devreye girer.
        // Küçük/normal deltalarda (<= 25) HİÇ UYGULANMAZ.
        if (absDelta <= 25) {
            return EmotionalRegulationOutput(
                isRegulationActive = false,
                rawDelta = rawDelta,
                outwardExpressedDelta = rawDelta,
                capacity = capacity
            )
        }

        // Capacity (0-100)
        val expressed = if (capacity <= 40) {
            // Düşük kapasite (0-40): Sönümlenmeden aynen dışa vurulur (multiplier 1.0)
            rawDelta
        } else {
            // Yüksek kapasite (60-100): Dışa vurulan tepki %30-50 sönümlenir, daha ölçülü görünür
            (rawDelta * (1.0 - (capacity / 200.0))).roundToInt()
        }

        return EmotionalRegulationOutput(
            isRegulationActive = true,
            rawDelta = rawDelta,
            outwardExpressedDelta = expressed,
            capacity = capacity
        )
    }

    // =====================================================
    // BÖLÜM G0 — BAŞLANGIÇ DUYGU PROFİLİ ANALİZİ (BaselineAnalysis)
    // =====================================================

    private val baselineResultCache = java.util.concurrent.ConcurrentHashMap<String, BaselineEmotionEngine.BaselineAnalysisResult>()

    internal fun buildInputHash(
        aiName: String,
        personality: String,
        scenario: String,
        userCharName: String,
        userCharDesc: String,
        mode: String = "personal",
        universeName: String = "",
        keyCharactersJson: String = "[]"
    ): String {
        val raw = "$mode|$aiName|$universeName|$personality|$scenario|$userCharName|$userCharDesc|$keyCharactersJson"
        return try {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            md.digest(raw.toByteArray()).joinToString("") { "%02x".format(it) }.take(16)
        } catch (_: Exception) { raw.length.toString() }
    }

    // Reads the persisted profile from BotEntity.baselineEmotionAnalysisJson.
    // Falls back to DeterministicFallback if absent or unparsable.
    fun getStoredBaselineResult(bot: com.example.data.local.BotEntity): BaselineEmotionEngine.BaselineAnalysisResult {
        val effectiveName = if (bot.mode == "universe" && bot.universeName.isNotBlank()) bot.universeName else bot.aiName
        val json = bot.baselineEmotionAnalysisJson
        if (json.isBlank()) {
            return BaselineEmotionEngine.calculateDeterministicFallback(
                botId = bot.id, aiName = effectiveName, personality = bot.aiPersonality,
                scenario = bot.scenario, userCharName = bot.userCharName, userCharDesc = bot.userCharDesc
            )
        }
        return try {
            BaselineEmotionEngine.parseAnalysisJson(
                jsonStr = json, botId = bot.id, aiName = effectiveName, personality = bot.aiPersonality,
                scenario = bot.scenario, userCharName = bot.userCharName, userCharDesc = bot.userCharDesc
            )
        } catch (_: Exception) {
            BaselineEmotionEngine.calculateDeterministicFallback(
                botId = bot.id, aiName = effectiveName, personality = bot.aiPersonality,
                scenario = bot.scenario, userCharName = bot.userCharName, userCharDesc = bot.userCharDesc
            )
        }
    }

    private fun buildUpdatedBotFromBaseline(
        bot: com.example.data.local.BotEntity,
        result: BaselineEmotionEngine.BaselineAnalysisResult,
        newHash: String,
        userEditedFields: Set<String>
    ): com.example.data.local.BotEntity {
        val editedSet = if (bot.baselineUserEditedFields.isNotBlank())
            bot.baselineUserEditedFields.split(",").toSet() + userEditedFields
        else userEditedFields
        val editedFieldsStr = editedSet.filter { it.isNotBlank() }.joinToString(",")

        val emotionJson = result.baselineEmotionState.toJson()
        val canonicalJson = result.toCanonicalJson()

        val updatedWorldAtmo = if (bot.mode == "universe" && result.universeAtmosphere != null) {
            val seed = result.universeAtmosphere
            val locPart = listOf(seed.currentLocation, seed.timeOfDay).filter { it.isNotBlank() }.joinToString(" • ")
            com.example.data.local.WorldAtmosphere(
                mood = seed.sceneMood.ifBlank { "sakin ve beklentili" },
                intensity = seed.sceneTension.coerceIn(1, 10),
                currentEvent = if (locPart.isNotBlank()) "Başlangıç sahnesi: $locPart" else "Açılış sahnesi ve hikayenin başlangıcı",
                macroAtmosphere = "${bot.universeName.ifBlank { bot.aiName }} evreni (Küresel gerilim: ${seed.globalTensionLevel}/10).",
                microAtmosphere = locPart.ifBlank { "Hikayenin başladığı başlangıç ortamı." }
            ).toJson()
        } else {
            bot.worldAtmosphere
        }

        return bot.copy(
            emotionState                = if ("emotionState" in editedSet) bot.emotionState else emotionJson,
            previousEmotionState        = if ("emotionState" in editedSet) bot.previousEmotionState else emotionJson,
            baseAffectionDifficulty     = if ("affectionDifficulty" in editedSet) bot.baseAffectionDifficulty else result.difficultyMultiplier,
            emotionalRegulationCapacity = if ("regulationCapacity" in editedSet) bot.emotionalRegulationCapacity else result.regulationCapacity,
            worldAtmosphere             = updatedWorldAtmo,
            baselineEmotionAnalysisJson = canonicalJson,
            baselineAnalysisSource      = result.analysisSource,
            baselineInputHash           = newHash,
            baselineUserEditedFields    = editedFieldsStr,
            updatedAt                   = System.currentTimeMillis()
        )
    }

    // Runs one-shot low-temperature utility LLM analysis for bot creation.
    // Uses smart input-hash cache (0 network calls on unchanged inputs) and falls back to keyword on error.
    suspend fun runBaselineAnalysis(
        bot: com.example.data.local.BotEntity,
        forceReanalyze: Boolean = false,
        userEditedFields: Set<String> = emptySet()
    ): Pair<com.example.data.local.BotEntity, BaselineEmotionEngine.BaselineAnalysisResult> =
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            val effectiveName = if (bot.mode == "universe" && bot.universeName.isNotBlank()) bot.universeName else bot.aiName
            val newHash = buildInputHash(
                aiName = bot.aiName,
                personality = bot.aiPersonality,
                scenario = bot.scenario,
                userCharName = bot.userCharName,
                userCharDesc = bot.userCharDesc,
                mode = bot.mode,
                universeName = bot.universeName,
                keyCharactersJson = bot.keyCharactersJson
            )
            val cacheKey = "${bot.id}|$newHash"

            // Skip LLM if input unchanged and profile already stored on BotEntity or cached in memory
            if (!forceReanalyze) {
                if (bot.baselineInputHash == newHash && bot.baselineEmotionAnalysisJson.isNotBlank()) {
                    val cached = getStoredBaselineResult(bot)
                    return@withContext bot to cached
                }
                val memCached = baselineResultCache[cacheKey]
                if (memCached != null) {
                    val updatedCachedBot = buildUpdatedBotFromBaseline(bot, memCached, newHash, userEditedFields)
                    return@withContext updatedCachedBot to memCached
                }
            } else {
                baselineResultCache.remove(cacheKey)
            }

            val castSummary = if (bot.mode == "universe") {
                parseKeyCharacters(bot.keyCharactersJson)
                    .filter { it.name.isNotBlank() }
                    .joinToString("; ") { c ->
                        if (c.desc.isNotBlank()) "${c.name.trim()} (${c.desc.trim()})" else c.name.trim()
                    }
            } else ""

            val result: BaselineEmotionEngine.BaselineAnalysisResult = try {
                val settings = getOrCreateSettings()
                val prompt = BaselineEmotionEngine.createAnalysisPrompt(
                    aiName = effectiveName,
                    personality = bot.aiPersonality,
                    scenario = bot.scenario,
                    userCharName = bot.userCharName,
                    userCharDesc = bot.userCharDesc,
                    mode = bot.mode,
                    keyCharactersSummary = castSummary
                )

                val rawText = callUtilityLlm(
                    settings = settings,
                    systemPrompt = "Sen bir karakter psikolojisi analizcisin. Yalnızca istenen JSON formatında yanıt ver, başka hiçbir şey yazma.",
                    userMessage = prompt,
                    botId = bot.id,
                    maxTokens = 750,
                    timeoutMs = 8000L
                )

                if (rawText.isBlank()) {
                    throw IllegalStateException("empty_utility_response")
                }

                val parsed = BaselineEmotionEngine.parseAnalysisJson(
                    jsonStr = rawText,
                    botId = bot.id,
                    aiName = effectiveName,
                    personality = bot.aiPersonality,
                    scenario = bot.scenario,
                    userCharName = bot.userCharName,
                    userCharDesc = bot.userCharDesc
                )
                if (parsed.analysisSource == "keyword") {
                    parsed
                } else {
                    parsed.copy(analysisSource = "llm")
                }
            } catch (_: Exception) {
                BaselineEmotionEngine.calculateDeterministicFallback(
                    botId = bot.id,
                    aiName = effectiveName,
                    personality = bot.aiPersonality,
                    scenario = bot.scenario,
                    userCharName = bot.userCharName,
                    userCharDesc = bot.userCharDesc
                )
            }

            // Ensure every trigger is anchored to bot.id and cache result for instant re-entry
            val anchoredResult = result.copy(
                sensitiveTriggers = result.sensitiveTriggers.map { it.copy(botId = bot.id) }
            )
            baselineResultCache[cacheKey] = anchoredResult

            // Persist triggers to SensitiveTriggerEntity table
            try {
                sensitiveTriggerDao.deleteTriggersForBot(bot.id)
                for (trigger in anchoredResult.sensitiveTriggers) {
                    sensitiveTriggerDao.insertOrUpdate(trigger)
                }
            } catch (_: Exception) {}

            val updatedBot = buildUpdatedBotFromBaseline(bot, anchoredResult, newHash, userEditedFields)
            updatedBot to anchoredResult
        }

    suspend fun saveSensitiveTriggers(botId: String, triggers: List<com.example.data.local.SensitiveTriggerEntity>) {
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            sensitiveTriggerDao.deleteTriggersForBot(botId)
            for (t in triggers) {
                if (t.triggerTopic.isNotBlank()) {
                    sensitiveTriggerDao.insertOrUpdate(t.copy(botId = botId))
                }
            }
        }
    }

    /**
     * Synchronizes manually defined Universe KeyCharacters from the wizard into CastMemberEntity rows
     * with deterministic zero-token PersonalityProfile & role difficulty multiplier.
     */
    suspend fun syncWizardCastMembers(bot: com.example.data.local.BotEntity) {
        if (bot.mode != "universe") return
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            val keyChars = parseKeyCharacters(bot.keyCharactersJson).filter { it.name.isNotBlank() }
            if (keyChars.isEmpty()) return@withContext
            val now = System.currentTimeMillis()
            for (char in keyChars) {
                val cleanName = char.name.trim()
                val cleanDesc = char.desc.trim()
                val (profile, diffMult, initialAff) = BaselineEmotionEngine.deriveCastMemberBaseline(cleanName, cleanDesc)
                val existing = try { castMemberDao.findByName(bot.id, cleanName) } catch (_: Exception) { null }
                val relState = when {
                    initialAff >= 75 -> "Yakın"
                    initialAff <= 25 -> "Gergin"
                    else -> "Tanıdık"
                }
                if (existing == null) {
                    castMemberDao.insertCastMember(
                        com.example.data.local.CastMemberEntity(
                            botId = bot.id,
                            name = cleanName,
                            description = cleanDesc.ifBlank { "Ana evren karakteri" },
                            role = cleanDesc.take(40).ifBlank { "Ana Karakter" },
                            affectionScore = initialAff,
                            peakAffectionScore = initialAff,
                            relationshipState = relState,
                            firstAppearedAt = now,
                            importanceScore = 85,
                            isAutoAdded = false,
                            isBlacklisted = false,
                            detectionConfidence = 100,
                            pendingUserConfirmation = false,
                            appearanceCount = 1,
                            baseAffectionDifficultyMultiplier = diffMult,
                            personalityJson = profile.toJson()
                        )
                    )
                } else if (existing.appearanceCount <= 1 && (existing.personalityJson.isBlank() || existing.affectionScore == 50)) {
                    castMemberDao.updateCastMember(
                        existing.copy(
                            description = cleanDesc.ifBlank { existing.description },
                            affectionScore = initialAff,
                            peakAffectionScore = initialAff,
                            relationshipState = relState,
                            importanceScore = maxOf(existing.importanceScore, 85),
                            isAutoAdded = false,
                            pendingUserConfirmation = false,
                            baseAffectionDifficultyMultiplier = diffMult,
                            personalityJson = profile.toJson()
                        )
                    )
                }
            }
        }
    }

    // =====================================================
    // BÖLÜM G — DUYGUSAL HASSAS NOKTA (SensitiveTrigger) SİSTEMİ
    // =====================================================
    suspend fun evaluateSensitiveTriggers(
        botId: String,
        userMessageText: String
    ): Double {
        val triggers = sensitiveTriggerDao.getTriggersForBot(botId)
        if (triggers.isEmpty()) return 1.0

        val lowerText = userMessageText.lowercase()
        var matchedAny = false
        var effectiveMultiplier = 1.0

        for (trigger in triggers) {
            if (lowerText.contains(trigger.triggerTopic.lowercase())) {
                matchedAny = true
                val newCount = trigger.recentTriggerCount + 1
                // ÖNEMLİ DENGELEME: Art arda 4. tetiklenmeden itibaren multiplier otomatik normale (1.0) düşer!
                val multiplier = if (newCount >= 4) 1.0 else trigger.intensityMultiplier
                sensitiveTriggerDao.insertOrUpdate(trigger.copy(recentTriggerCount = newCount))
                effectiveMultiplier = maxOf(effectiveMultiplier, multiplier)
            }
        }

        if (!matchedAny) {
            // Sıfırla: Her tetiklenme ayrı bir olaydır, art arda gelmediyse sayacı sıfırla
            for (trigger in triggers) {
                if (trigger.recentTriggerCount > 0) {
                    sensitiveTriggerDao.insertOrUpdate(trigger.copy(recentTriggerCount = 0))
                }
            }
        }

        return effectiveMultiplier
    }

    // =====================================================
    // BÖLÜM H — DUYGUNUN ZAMANLA "OLGUNLAŞMASI" (EmotionalReappraisal)
    // =====================================================
    suspend fun recordPendingReappraisalIfEligible(
        botId: String,
        eventDescription: String,
        emotionStateJson: String,
        currentMessageIndex: Int,
        rawDelta: Int
    ) {
        // SADECE büyük negatif olaylar (ham delta <= -20) için çalışır
        if (rawDelta > -20) return

        val activeList = pendingReappraisalDao.getActivePendingReappraisals(botId)

        // ÖNEMLİ DENGELEME: Bir botta aynı anda en fazla 2-3 bekleyen reappraisal olabilir.
        // 3 veya daha fazla ise, en eskisi (ilk kaydedileni) otomatik expired=true olarak kapatılır.
        if (activeList.size >= 3) {
            val oldest = activeList.first()
            pendingReappraisalDao.update(oldest.copy(expired = true, resolved = false))
        }

        val now = System.currentTimeMillis()
        val newReappraisal = PendingReappraisalEntity(
            botId = botId,
            originalEventDescription = eventDescription,
            originalEmotionState = emotionStateJson,
            triggeredAtMessageIndex = currentMessageIndex,
            triggeredAtTimestamp = now,
            reappraisalEligibleAtMessageIndex = currentMessageIndex + 15,
            reappraisalEligibleAtTimestamp = now + (3 * 24 * 3600 * 1000L), // +3 gün
            resolved = false,
            expired = false
        )
        pendingReappraisalDao.insert(newReappraisal)
    }

    // =====================================================
    // SCHEMA V3 DENGELEME VE SINIR KONTROLLERİ DOĞRULAMA SİMÜLASYONU
    // =====================================================


    // --- Context Window & Token Management Engine ---

    fun getModelContextLimit(model: String): Int {
        val m = model.trim().lowercase()
        return when {
            m.contains("claude-3-5-sonnet") || m.contains("claude-3-5-haiku") || m.contains("claude-3-opus") -> 200_000
            m.contains("gpt-4o") || m.contains("o1") || m.contains("o3-mini") -> 128_000
            m.contains("gemini") -> 1_000_000
            m.contains("deepseek") -> 64_000
            m.contains("llama-3.3") || m.contains("llama-3.1") -> 128_000
            m.contains("llama") || m.contains("groq") || m.contains("mixtral") -> 32_768
            else -> 32_000
        }
    }

    fun estimateTokenCount(text: String): Int {
        if (text.isBlank()) return 0
        return (text.length / 3.5).toInt().coerceAtLeast(1)
    }

    fun estimateTotalTokens(systemPrompt: String, messages: List<MessageEntity>): Int {
        var count = estimateTokenCount(systemPrompt)
        for (msg in messages) {
            count += estimateTokenCount(msg.text) + 4
        }
        return count
    }

    private val lastSummarizedMessageCount = java.util.concurrent.ConcurrentHashMap<String, Int>()

    private suspend fun prepareContextAndSummarizeIfNeeded(
        bot: BotEntity,
        messages: List<MessageEntity>,
        modelName: String
    ): Pair<BotEntity, List<MessageEntity>> {
        val maxWindow = when {
            modelName.contains("deepseek") || modelName.contains("gemini") -> 30
            modelName.contains("claude") || modelName.contains("gpt") || modelName.contains("llama") -> 20
            else -> 15
        }
        val activeMessages = if (messages.size > maxWindow) messages.takeLast(maxWindow) else messages
        if (activeMessages.size <= 8) return Pair(bot, activeMessages)

        val modelLimit = getModelContextLimit(modelName)
        val maxBudgetTokens = (modelLimit * 0.75).toInt().coerceAtMost(24_000)

        val userQuery = activeMessages.lastOrNull { it.role == "user" }?.text ?: ""
        val relevantFragments = getRelevantMemoryFragments(bot, userQuery)
        val currentSysPrompt = buildSystemPrompt(bot, getOrCreateSettings(), relevantFragments = relevantFragments)
        val currentTokens = estimateTotalTokens(currentSysPrompt, activeMessages)

        val keepCount = 12
        val alreadySummarized = (lastSummarizedMessageCount[bot.id] ?: 0).coerceIn(0, messages.size)
        val unsummarizedOverflow = (messages.size - keepCount - alreadySummarized).coerceAtLeast(0)
        val isCriticallyFull = ((currentTokens > maxBudgetTokens * 0.9) || (unsummarizedOverflow >= 24)) && unsummarizedOverflow >= 8
        val exceedsTriggerThreshold = unsummarizedOverflow >= 12

        if (isCriticallyFull) {
            // Summarize only the newly overflowed older slice into memory notes/fragments
            // WITHOUT EVER deleting messages from Room database.
            val endExclusive = (messages.size - keepCount).coerceAtLeast(alreadySummarized)
            val olderSlice = messages.subList(alreadySummarized, endExclusive)
            val recentMessages = messages.takeLast(keepCount)

            if (olderSlice.isNotEmpty()) {
                val updatedBot = summarizeAndArchiveOlderMessages(bot, olderSlice)
                lastSummarizedMessageCount[bot.id] = endExclusive
                botDao.setNeedsSummarization(bot.id, false)
                return Pair(updatedBot, recentMessages)
            }
        } else if (exceedsTriggerThreshold) {
            if (!bot.needsSummarization) {
                botDao.setNeedsSummarization(bot.id, true)
            }
        }

        return Pair(bot, activeMessages)
    }

    /**
     * Arka plan duygu denetimi için LLM çağrısı.
     * Mevcut sağlayıcı/anahtar sistemi kullanılır.
     * Hata veya boş cevap durumunda "" döner — çağıran sessizce atlar.
     */


    private suspend fun summarizeAndArchiveOlderMessages(
        bot: BotEntity,
        olderMessages: List<MessageEntity>
    ): BotEntity {
        val settings = getOrCreateSettings()
        val effectiveModel = getEffectiveModelForProvider(settings)

        val aiName = if (bot.mode == "universe") bot.universeName else bot.aiName
        val userLabel = bot.userCharName.ifBlank { "Kullanıcı" }

        val recapText = olderMessages.joinToString("\n") { m ->
            val sender = if (m.role == "user") userLabel else aiName
            "$sender: ${m.text}"
        }

        val prompt = "Aşağıdaki geçmiş sahne mesajlarından önemli gelişmeleri ve olay örgüsünü özetle ve SADECE şu formatta yaz:\n\nDURUM:\n- (yan karakterler, mekanlar, çözülmemiş konular — en fazla 5 madde)\n\nHAFIZA:\n- (duygusal gelişmeler, ilişki değişimleri, verilen sözler — en fazla 5 madde)"

        var updatedBot = bot
        var summarizationSucceeded = false

        // 1. Utility Model Tabanlı Özetleme Denemesi (resolveUtilityModel + temp=0.12)
        try {
            val raw = callUtilityLlm(
                settings = settings,
                systemPrompt = "Sen yardımcı bir hafıza ve olay özetleyicisin.",
                userMessage = "$prompt\n\nGEÇMİŞ SAHNE:\n$recapText",
                botId = bot.id,
                maxTokens = 450,
                timeoutMs = 7000L
            )

            if (raw.contains("DURUM:", ignoreCase = true) || raw.contains("HAFIZA:", ignoreCase = true)) {
                val durumMatch = raw.split(Regex("HAFIZA:", RegexOption.IGNORE_CASE))[0]
                    .replace(Regex("DURUM:", RegexOption.IGNORE_CASE), "").trim()

                val hafizaMatch = raw.split(Regex("HAFIZA:", RegexOption.IGNORE_CASE)).getOrNull(1)?.trim() ?: ""

                // Save into memory_fragments table for RAG semantic search
                saveMemoryFragmentsFromSummary(bot.id, durumMatch, hafizaMatch)

                val newStory = listOf(bot.storyNotes, durumMatch).filter { it.isNotBlank() }.joinToString("\n")
                    .lines().takeLast(25).joinToString("\n")

                val newMemory = listOf(bot.memoryNotes, hafizaMatch).filter { it.isNotBlank() }.joinToString("\n")
                    .lines().takeLast(25).joinToString("\n")

                updatedBot = bot.copy(
                    storyNotes = newStory,
                    memoryNotes = newMemory,
                    needsSummarization = false,
                    updatedAt = System.currentTimeMillis()
                )
                botDao.insertOrUpdate(updatedBot)
                summarizationSucceeded = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            summarizationSucceeded = false
        }

        // 2. Yerel Sahne Checkpoint Arşivi Fallback (AI Bağlantısı veya Key Olmadığında — asla Anılar'a ham sohbet kopyalamaz!)
        if (!summarizationSucceeded) {
            try {
                val localSummaryHeader = "[Önceki Sahne Arşivi]"
                val sampleOldMsgs = olderMessages.takeLast(6).joinToString("\n") { m ->
                    val sender = if (m.role == "user") userLabel else aiName
                    val snippet = if (m.text.length > 90) m.text.take(90) + "..." else m.text
                    "- $sender: $snippet"
                }
                val fallbackStorySnippet = "$localSummaryHeader\n$sampleOldMsgs"

                val nextNum = (try { memoryCheckpointDao.getMaxCheckpointNumber(bot.id) } catch (_: Exception) { 0 } ?: 0) + 1
                val cpVec = MemoryEngine.computeTurkishFeatureEmbedding(fallbackStorySnippet)
                try {
                    memoryCheckpointDao.insertCheckpoint(
                        MemoryCheckpointEntity(
                            id = 0L,
                            botId = bot.id,
                            checkpointNumber = nextNum,
                            messageRangeStart = 1,
                            messageRangeEnd = olderMessages.size,
                            summaryText = "Sahne #$nextNum (Yerel Arşiv): ${olderMessages.takeLast(4).joinToString(" | ") { it.text.take(70) }}",
                            embedding = floatArrayToJson(cpVec),
                            timestamp = System.currentTimeMillis(),
                            isTemporary = true
                        )
                    )
                } catch (_: Exception) {}

                val combinedStory = listOf(bot.storyNotes, fallbackStorySnippet).filter { it.isNotBlank() }
                    .joinToString("\n").lines().takeLast(25).joinToString("\n")

                updatedBot = bot.copy(
                    storyNotes = combinedStory,
                    needsSummarization = false,
                    updatedAt = System.currentTimeMillis()
                )
                botDao.insertOrUpdate(updatedBot)
                summarizationSucceeded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Eski mesajlar veritabanında KALICI olarak korunur (asla silinmez!).
        // Sohbet ekranı performans için son 20 mesajı gösterir ve yukarı çıkıldığında
        // 20'şer mesajlık "Daha Fazla Mesaj Göster" butonuyla tüm geçmişi eksiksiz sunar.

        return updatedBot
    }

    private fun formatMessagesForGemini(messages: List<MessageEntity>): List<GeminiContent> {
        val recentMsgs = messages.takeLast(30)
        if (recentMsgs.isEmpty()) {
            return listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Sohbeti başlat."))
                )
            )
        }

        val result = mutableListOf<GeminiContent>()

        // Ensure first content turn starts with user
        val adjustedMsgs = mutableListOf<MessageEntity>()
        if (recentMsgs.first().role != "user") {
            adjustedMsgs.add(
                MessageEntity(
                    id = "virtual_init",
                    botId = recentMsgs.first().botId,
                    role = "user",
                    text = "Sohbet/Sahne başlatıldı.",
                    timestamp = 0L
                )
            )
        }
        adjustedMsgs.addAll(recentMsgs)

        // Merge consecutive same-role messages
        for (m in adjustedMsgs) {
            val currentRole = if (m.role == "user") "user" else "model"
            if (result.isNotEmpty() && result.last().role == currentRole) {
                val lastContent = result.removeAt(result.size - 1)
                val prevText = lastContent.parts.firstOrNull()?.text ?: ""
                val mergedText = "$prevText\n\n${m.text}"
                result.add(
                    GeminiContent(
                        role = currentRole,
                        parts = listOf(GeminiPart(text = mergedText))
                    )
                )
            } else {
                result.add(
                    GeminiContent(
                        role = currentRole,
                        parts = listOf(GeminiPart(text = m.text))
                    )
                )
            }
        }

        return result
    }

    private fun formatMessagesForStandardApi(messages: List<MessageEntity>): List<Pair<String, String>> {
        val recentMsgs = messages.takeLast(30)
        if (recentMsgs.isEmpty()) {
            return listOf(Pair("user", "Sohbeti başlat."))
        }

        val adjustedMsgs = mutableListOf<MessageEntity>()
        if (recentMsgs.first().role != "user") {
            adjustedMsgs.add(
                MessageEntity(
                    id = "virtual_init",
                    botId = recentMsgs.first().botId,
                    role = "user",
                    text = "Sohbet/Sahne başlatıldı.",
                    timestamp = 0L
                )
            )
        }
        adjustedMsgs.addAll(recentMsgs)

        val result = mutableListOf<Pair<String, String>>()
        for (m in adjustedMsgs) {
            val role = if (m.role == "user") "user" else "assistant"
            if (result.isNotEmpty() && result.last().first == role) {
                val last = result.removeAt(result.size - 1)
                result.add(Pair(role, "${last.second}\n\n${m.text}"))
            } else {
                result.add(Pair(role, m.text))
            }
        }
        return result
    }







    private suspend fun generateDeterministicBookReply(
        bot: BotEntity,
        messages: List<MessageEntity>
    ): String {
        val userMsgs = messages.filter { it.role == "user" }
        val userStep = userMsgs.size
        val lastUserMsg = userMsgs.lastOrNull()?.text ?: ""

        // Güçlendirilmiş seçim algılama: sayı, emoji ve anahtar kelime
        fun choiceIs(msg: String, n: Int, vararg keywords: String): Boolean {
            val lower = msg.trim().lowercase()
            if (lower == n.toString()) return true
            val emojiMap = mapOf(1 to "1️⃣", 2 to "2️⃣", 3 to "3️⃣", 4 to "4️⃣", 5 to "5️⃣", 6 to "6️⃣")
            if (emojiMap[n] != null && msg.contains(emojiMap[n]!!)) return true
            if (Regex("^[#]?${n}[.):]").containsMatchIn(lower)) return true
            val wordNums = mapOf(1 to listOf("birinci","bir ","birini","1. ","1) "),
                2 to listOf("ikinci","iki ","ikisini","2. ","2) "),
                3 to listOf("üçüncü","üç ","üçünü","3. ","3) "),
                4 to listOf("dördüncü","dört ","4. ","4) "))
            wordNums[n]?.forEach { if (lower.contains(it)) return true }
            val parsed = com.example.util.BookChoiceParser.parseChoice(msg, keywords.toList())
            if (parsed == n) return true
            return keywords.any { lower.contains(it.lowercase()) }
        }

        var progress = storyProgressDao.getProgressOnce(bot.id) ?: StoryProgressEntity(botId = bot.id)

        val b1Text = loadAssetText("Bolum1_KozmikSurukleniş.md")
        val b2Text = loadAssetText("Bolum2_Sartlar.md")
        val b3Text = loadAssetText("Bolum3_DenemeSuresi_v2.md")
        val b3KisaKacis = loadAssetText("Bolum3_AlternatifDal_KisaKacis.md")
        val b3Kacis = loadAssetText("Bolum3_AlternatifDal_Kacis.md")
        val b3Thor = loadAssetText("Bolum3_AlternatifDal_ThorBulma.md")

        return when (userStep) {
            1 -> {
                // Choice 1 Response
                val isDeep = choiceIs(lastUserMsg, 2, "Görüye derinden odaklan", "odaklan", "derinden", "detay çek")
                progress = if (isDeep) {
                    progress.copy(fatigue = progress.fatigue + 1, chapterNumber = 1)
                } else {
                    progress.copy(chapterNumber = 1)
                }
                storyProgressDao.insertOrUpdate(progress)

                val intro = if (isDeep) {
                    """Zihnini görünün derinliklerine zorladın. Mavi hologramın detaylarını sökmeye çalıştın: masadaki dosyaların üzerindeki isimleri, Tony Stark'ın endişeli kaş çatışını, Wanda'nın pencerelerden dışarı bakışını, Steve Rogers'ın masaya koyduğu ellerini. Ekstra detaylar zihnine bir sel gibi aktı ama bedeli ağır oldu: gözlerinin arkasında keskin bir sancı saplandı, burnundan ince bir kan sızdı.

Görü dağıldığında şakakların zonkluyordu ama koordinatlar ve odadaki herkesin pozisyonu zihnine kazınmıştı."""
                } else {
                    """Zihnini geri çektin. Görü sahneleri önünden soğuk bir film şeridi gibi aktı: Stark Tower'ın en üst katındaki cam salon, masanın etrafında toplanmış gölgeler, masanın ortasında dönen mavi hologram. Mesafeli kaldın; gücünü zorlamadın, zihnini yıpratmadın. Sadece ne görmen gerekiyorsa onu gördün.

Görü eridiğinde burnun kanamıyordu, başın dönmüyordu. Fiziksel olarak tam gücündeydin."""
                }

                val mainPart = b1Text ?: """Toplantı odası etrafında maviye çalan bir statik içinde şekillendi, sessiz ve ödünç alınmış bir an — gerçekten orada değildin..."""

                intro + "\n\n---\n\n" + mainPart + """

---

🔀 Tower'a geçişi nasıl gerçekleştireceksin?

1️⃣ Direkt ışınlan, hiç düşünmeden.
2️⃣ Önce dışarıdan gözlemle (birkaç dakika Tower'ı uzaktan izle), sonra ışınlan.
3️⃣ Geri çekil, gitme — sadece izlemeye devam et."""
            }
            2 -> {
                // Choice 2 Response
                val isAvoid = choiceIs(lastUserMsg, 3, "Geri çekil", "gitme", "geri çek", "gitmiyorum", "izlemeye devam")
                val isObserved = choiceIs(lastUserMsg, 2, "Önce dışarıdan gözlemle", "gözlemle", "uzaktan", "izle", "bekle")

                progress = if (isAvoid) {
                    progress.copy(firstImpressionAvengers = "avoided")
                } else if (isObserved) {
                    progress.copy(firstImpressionAvengers = "calculated")
                } else {
                    progress.copy(firstImpressionAvengers = "impulsive")
                }
                storyProgressDao.insertOrUpdate(progress)

                val intro = if (isAvoid) {
                    """İçindeki uyarı çanları galip geldi ve çatının kenarından bir adım geri çekildin. Çatının gölgelerine saklanarak Kule'den uzaklaşmayı seçtin.

Ancak tam o anda zihnindeki mavi hologram çılgınca parıldadı. S.H.I.E.L.D. uyduları uzam sapmanı önceden tespit etmiş ve koordinatlarını kilitlemişti! Ani bir kuantum çökmesiyle etrafındaki gerçeklik yırtıldı ve bir enerji dalgası seni doğrudan Stark Tower'ın yüksek tavanlı toplantı salonunun ortasına savurdu. Kule'ye kendi isteğinle girmemiştin ama kaçınma çaban bile seni kaderinden uzaklaştıramadı."""
                } else if (isObserved) {
                    """Saniyelerce Tower'ın dış camlarındaki yansımaları izledin. Güvenlik sistemlerinin tarama frekanslarını, korumaların turlarını ve penceredeki gölgelerin hareketini hesapladın. Temkinli ve kontrolcü bir zamanlamayla uzamı büküp içeri ışınlandın.

Natasha Romanoff bu sessiz ve hesaplı girişini fark ettiğinde gözlerinde hafif bir takdir parıltısı belirdi."""
                } else {
                    """Hiç duraksamadın. Bir nefes aldın ve uzam büküldü. Odadaki havanın basıncı bir anda değişti, zifiri karanlık yerini Avengers Tower'ın yüksek tavanlı, cam duvarlı toplantı salonuna bıraktı. İçgüdüsel ve ani gelişin, odadakilerin reflekslerini anında tetikledi. Steve Rogers elini kalkanına uzattı."""
                }

                intro + """

Oda içindeki hava sen görünmeden önce değişti — bir basınç düşüşü, hafif bir ozon kokusu, bir kalp atımlık mavi ışık kendini bir şekle, sonra da masanın başında hep oradaymış gibi duran bir adama dönüştürdü.

Bir saniye kimse kıpırdamadı.

Onlara nasıl göründüğünün farkındaydın: uzun boylu, acele etmeyen, hiçbir yerde kalmayı planlamayan birinin giydiği hafif kıyafetlerle. Gözlerin herkesin fark ettiği ilk şeydi — sonra ikinci şey, sonra üçüncü şey, çünkü çoğu insanın senin bakışını gerçekten tutabilmesi üç deneme alıyordu. Bu bir tehdit değildi. Sana bunu söyleyecek kadar yaklaşan o bir avuç insan, bunun daha çok göze bakmak için fazla parlak bir maviye bakmaya benzediğini söylemişti. Bunun için özür dilemeyi çoktan bırakmıştın.

Stark ilk toparlanan oldu, tabii ki o oldu. *"Peki,"* dedi, tabletini teatral bir sakinlikle masaya bırakarak. *"Davete böyle cevap vermek de bir yöntem."*

Sarışın askerin eli, içgüdüsel olarak, sandalyesine dayalı kalkana doğru kaymıştı. Sen bunu tepki vermeden fark ettin. Her şeyi tepki vermeden fark ediyordun; hâlâ hayatta olmanın tek sebebi buydu.

Kızıl saçlı hiç kıpırdamamıştı. Sana, senin bir odayı incelediğin gibi bakıyordu — kataloglayarak, bakakalmadan. Uzun zamandır ilk kez birinin sana baktığında hemen odadan çıkmak istemedin.

Pencerenin yanında, tartışmadan yarı dönmüş halde, görünün sana hiç göstermediği biri duruyordu — koyu saçlı, parmaklarının etrafında kızıla çalan bir enerji tembel tembel kıvrılıyordu, sen gelmeden önce yarım kalmış bir düşüncenin ortasındaymış gibi. Sana, havanın döndüğünü izleyen birinin bakışıyla bakıyordu: tam olarak korkmuş değil. Yeniden hesaplıyordu.

Sessizliği kıran Thor oldu, ve bunu geniş, memnun bir gülümsemeyle yaptı, sanki Noel erken gelmiş gibi. *"İşte burada. Gezgin."*

*"Kozmik Sürükleniş,"* dedi Stark, hands'ini açarak. *"Ya da — pardon, bu bir sahne adı mı, yoksa gerçekten böyle mi cevap veriyorsun?"*

---

🔀 Stark'ın "Sahne adı mı gerçek adın mı?" sorusuna nasıl cevap vereceksin?

1️⃣ Soğuk ve mesafeli cevap ver: "Aiden... Sorun genelde bu oluyor."
2️⃣ Alaycı/esprili cevap ver: "Giriş kartı masraflı geldi Stark. Aiden diyabilirsin."
3️⃣ Hiç isim verme, sadece sessizlik ve keskin bir bakışla cevap ver."""
            }
            3 -> {
                // Choice 3 Response
                val isWitty = choiceIs(lastUserMsg, 2, "Alaycı", "esprili", "espri", "masraflı", "mizah", "şaka")
                val isSilent = choiceIs(lastUserMsg, 3, "Hiç isim verme", "sessizlik", "sessiz", "isim verme", "konuşma")

                progress = if (isWitty) {
                    progress.copy(tonyAffinity = progress.tonyAffinity + 1)
                } else if (isSilent) {
                    progress.copy(mysteryFactor = progress.mysteryFactor + 1, steveTrust = progress.steveTrust - 1)
                } else {
                    progress
                }
                storyProgressDao.insertOrUpdate(progress)

                val responseText = if (isWitty) {
                    """Sessizliği, kimseye hız borçlu olmadığını netleştirecek kadar uzun bıraktın.

*"Giriş kartı masraflı geldi Stark,"* dedin hafif bir tebessümle. *"Aiden diyabilirsin."*

Tony Stark hafifçe gülümsedi. *"En azından mizah duygusu olan birisi,"* diye mırıldandı. Steve Rogers ise bu rahat tavrından pek hoşnut görünmedi."""
                } else if (isSilent) {
                    """Sessizliği, kimseye hız borçlu olmadığını netleştirecek kadar uzun bıraktın.

Tek bir kelime bile etmedin. Sadece sessiz kaldın ve gözlerini odadakilerin üzerinde gezdirdin. Gizemli ve tekinsiz sessizliğin Steve Rogers'ın şüphesini daha da artırdı.

Stark elini çenesine götürdü. *"Konuşkan biri değil demek ki,"* dedi."""
                } else {
                    """Sessizliği, kimseye hız borçlu olmadığını netleştirecek kadar uzun bıraktın.

*"Aiden,"* dedin sonunda. Sesin alçak, düzdü, yarı aralık bırakılmış kapalı bir kapının sözlü karşılığı gibi. *"Aiden Blackwood. Ve hayır — pek bir şeye cevap vermem. Sorun genelde bu oluyor."*

Steve'in kolları çapraz kalmaya devam etti, ama ifadesinde bir şey biraz gevşedi — güven değil, henüz değil, ama sana güvenip güvenemeyeceğine karar veren bir adamın dikkati. *"Bir yıla yakındır elimizdeki her radardan uzaktasın,"* dedi. *"Şimdi öylece içeri giriyorsun."*

*"Benim hakkımda konuşuyordunuz,"* dedin, tonun bir omuz silkmeye eşdeğer kadar kuru. *"Görüş bildirmemek kabalık olurdu."*

Bu, kızıl saçlıdan bir şeyi çekip çıkardı — tam bir gülümseme değil, ama farkında olmadan not aldığın kadar yakın bir şey. *"Dinliyormuş,"* dedi, sana değil de daha çok odaya, bir teoriyi doğruluyormuş gibi. *"Bu hiç de az bir şey değil."*

Stark kararlı bir şekilde ellerini çırptı. *"Tamam. Yeni plan. Herkes otursun, kimse panik yapmasın, ve biri bu adama bir kahve getirsin, ışınlanıp gitmenin bizimle konuşmaktan daha eğlenceli olduğuna karar vermeden önce."*

Sen oturmadın. Henüz değil. Masanın başında ayakta kaldın, kolların yanlarında gevşek, alışkanlıkla çıkışları kataloglarken, içindeki küçük ve hain bir parça, yirmi yıl içinde ilk kez, yabancılarla dolu bir odaya girip hemen ne kadar hızlı çıkabileceğini hesaplamadığını fark ediyordu.

Bu yeniydi. Bunu sevip sevmediğinden henüz emin değildin."""
                }

                responseText + """

---

Odanın havası ağırlaşmıştı. Masanın çevresindeki kişiler seni tartıyordu.

Sağ tarafta duran kızıl saçlı kadın (Natasha) ellerini göğsünde kavuşturmuş, seni profesyonel bir ajanın soğukkanlılığıyla inceliyordu.

Pencere kenarında duran siyah ceketli kadın (Wanda) ise sana, havanın döndüğünü izleyen birinin bakışıyla bakıyordu: tam olarak korkmuş değil. Yeniden hesaplıyordu.

---

🔀 Bakışların odada kimin üzerinde kalacak?

1️⃣ Gözlerin kızıl saçlıda (Natasha) gereğinden bir saniye fazla kalsın.
2️⃣ Gözlerin pencere kenarındaki kadında (Wanda) gereğinden bir saniye fazla kalsın.
3️⃣ Kimseye özel bir bakış atma, odayı genel olarak tara."""
            }
            4 -> {
                // Choice 4 Response -> Chapter 1 Ending
                val ch4isWanda = choiceIs(lastUserMsg, 2, "Wanda", "pencere kenarındaki", "pencere")
                val ch4isNeutral = choiceIs(lastUserMsg, 3, "Kimseye özel", "tara", "genel", "nötr", "kimseye")
                val choice4Text = if (ch4isWanda) {
                    """Gözlerin pencere kenarında duran Wanda Maximoff'a kaydı ve orada bir an çakılı kaldı. Wanda'nın gözlerinde hafif bir kırmızılık parlayıp söndü; zihnindeki dalgalanmayı hissetmiş gibiydi.

Sana bakarken başını hafifçe yana eğdi."""
                } else if (ch4isNeutral) {
                    """Kimseye özel bir bakış atmadın. Gözlerin odadaki tüm kaçış noktalarını, havalandırma ızgaralarını ve stratejik açıları tarayarak nötr kaldı.

Tamamen profesyonel ve mesafeliydin."""
                } else {
                    """Gözlerin Natasha Romanoff'un üzerinde gereğinden bir saniye daha uzun kaldı. Natasha hafifçe tek kaşını kaldırdı, bakışlarını kaçırmadı. Aranızdaki bu sessiz temas aranızda görünmez bir hat çekti.

Odadaki kimse bu anlık bakışmayı fark etmedi ama ikiniz de farkındaydınız."""
                }

                choice4Text + """

---

Konuşma devam etti — sorular, yarı cevaplar, senin verdiğin her bilgiye karşılık geri aldığın bir bilgi, eski bir alışkanlık, bir görüşme değil bir pazarlık gibi yürüttüğün bir sohbet. Stark, tekliflerini bir iş anlaşması gibi sunuyordu: kaynaklar, koruma, bir çatı altında bir yer. Steve, daha temkinli soruyordu, güven kelimesini hiç kullanmadan güvenden bahsediyordu. Thor, senin her cevabında bir şeyi doğrular gibi başını sallıyordu, sanki uzun zamandır beklediği bir hikâyenin son parçasını duyuyormuş gibi.

Kızıl saçlı — adını henüz söylememişti kimse ama içten içe zaten biliyordun, Romanoff — pek konuşmadı. Ama her sessizliğinde bir şey vardı, gözlemleyen, sabırlı, senin gibi biri için nadir bulunan bir tür sabır.

Pencerenin yanındaki kadın da sessizdi, ama onun sessizliği farklıydı — daha az hesaplı, daha çok bekleyen. Parmaklarının etrafındaki kızıl enerji yavaşça sönüp gitti, konuşma uzadıkça, sanki seni değerlendirmeyi bırakıp sadece dinlemeye karar vermiş gibi.

Sen hiçbirine güvenmedin. On beş yıllık alışkanlık bir gecede kırılmıyor, bir odaya girmen ve iki cümle kurman yetmiyor buna. Ama —ve bunu kendine itiraf etmek zorunda kaldın, istemesen de— odadan hâlâ çıkmamıştın. Ve bu, senin standartlarına göre, neredeyse bir mucizeydi.

---

📖 BÖLÜM 1 SONU — KOZMİK SÜRÜKLENİŞ

Aiden Blackwood Avengers Tower'a ilk adımını attı ve kararını verdi.
Odadan çıkmadı. Kaderinin bir sonraki halkası burada dövülecek.

Tebrikler! Bölüm 1'i başarıyla tamamladın."""
            }
            5 -> {
                // Chapter 2 Start -> Up to Chapter 2 Choice 1
                progress = progress.copy(chapterNumber = 2)
                storyProgressDao.insertOrUpdate(progress)

                val b2Full = b2Text ?: """# 📖 BÖLÜM 2: ŞARTLAR"""
                b2Full + """

---

🔀 Stark'ın "bize bir şey göster" talebine nasıl karşılık vereceksin?

1️⃣ İstenen minimum gösterimi yap. (Küçük mavi ışık küresi oluştur ve temel sınırlarını açıkla)
2️⃣ Reddet, hiçbir şey gösterme — sadece sözlerine güvenmelerini söyle.
3️⃣ İstenenden fazlasını göster, formların varlığından biraz daha açık bahset."""
            }
            6 -> {
                // Response to Chapter 2 Choice 1 -> Up to Chapter 2 Choice 2
                val c1Text = if (choiceIs(lastUserMsg, 3, "fazlasını", "Cömert", "tüm gücü", "hepsini göster")) {
                    """Sağ elinizi masanın üzerine doğru uzattınız ve parmaklarınızı açtınız. Sadece küçük bir küre değil, masanın tam ortasında gerçekliğin dokusunu büken mavi ve kırmızı kuantum çizgileriyle bezeli parıltılı bir kozmik küre oluşturdunuz. Odadaki havanın basıncı düştü, bardaklardaki su titredi ve köşedeki dijital ekranlar siyan dalgalarla dalgalandı.

*"Beyaz Göz zihinsel dalgaları ve hafızayı büker,"* dedin gözlerinde hafif renk değişimleriyle. *"Kırmızı Göz ise moleküler seviyede kinetik yıkım yaratır. Gücüm katmanlı — şu an gördüğünüz Drift formu sadece yüzey. Kırmızı Göz'ümle bina seviyesinde yıkım yapabilir, kozmik enerjiyi emip dönüştürebilirim. Tavanım düşündüğünüzden çok daha yüksek."*

Stark'ın gözleri parladı, tabletindeki okumaları izlerken heyecanını saklayamadı. *"İşte bu! Devasa bir kuantum çıktısı. Sayılar harika!"*

Natasha ise gözlerini senden ayırmadan, sesindeki o ince analiz tonuyla sordu: *"Bu kadar açık davranman güven jesti mi Blackwood, yoksa sınır çizen bir gözdağı mı?"*"""
                } else if (choiceIs(lastUserMsg, 2, "Reddet", "gösterme", "hayır", "şov yapmıyorum", "sergilemiyorum")) {
                    """Ellerinizi masada kavuşturdunuz ve geriye yaslandınız. Gözleriniz Stark'ın sabırsız bakışlarıyla buluştu.

*"Hayır,"* dedin düz, soğuk ve sarsılmaz bir tonla. *"Şov yapmıyorum. Gücüm bir sirk gösterisi değil. Sözlerime güvenmiyorsanız, hiçbir mavi ışık ya da gösteri fikrinizi değiştirmeyecektir. Bir tehdit olmadığımı bilmek istiyorsanız, burada sessizce masanızda oturuyor olmam en büyük kanıtınızdır."*

Stark hoşnutsuzca kaşlarını çattı, ellerini göğsünde kavuşturdu. *"Demek gizemli kalmayı seçiyorsun. Mühendisler gizemden hoşlanmaz Blackwood. Sayı ve kanıt isteriz."*

Steve araya girerek başını ağır ağır salladı. *"Dürüstlük anlaşılabilir bir şey. Ancak güven inşa etmek istiyorsak şeffaflık iki taraflı olmalı. Sana hemen güvenmemizi bekleyemezsin ama senin de bize bir adım atman gerekir."*

Thor ise hafifçe bıyık altından güldü. *"Gücünü saklamasını iyi biliyor. Bir savaşçının silahını kınında tutması zayıflık değil, tedbirdir."*"""
                } else {
                    """Bir an düşündün. Sonra, hiç kalkmadan, sağ elini masanın üstünde açtın ve avucunun ortasında küçük, sabit bir mavi ışık küresi oluşturdun — büyük değil, zararsız, ama gerçek, havanın onun etrafında hafifçe büküldüğünü görebilecekleri kadar gerçek. Oda ısısı bir derece düştü, sadece bir anlığına, sanki küçük bir yıldız oraya taşınmış gibi.

*"Bu, Beyaz Göz'ün alt seviyesi,"* dedin, ışığı izleyerek, onlara değil. *"Zihin üzerinde çalışıyor. Zayıf ve orta seviye zihinleri etkileyebilirim — hayaller, hafıza, kısa süreli felç. Daha güçlü birini kısa süreliğine sersemletebilirim ama tam kontrol edemem, en azından şu anki formumda."*

*"Şu anki formunda?"* diye tekrarladı Natasha, hemen o detayı yakalayarak.

Işığı avucunda kapattın, kaybolmasına izin vererek. *"Sabit değilim,"* dedin. *"Gücüm katmanlı. Şu an gördüğünüz — buna kendi kendime 'Drift' diyorum, çünkü isim koymazsan bir şey gerçek gelmiyor bazen — en zayıf formum değil, ama en güçlüsünden de çok uzak. Sıradan bir süper askerden daha güçlüyüm, hızlı iyileşiyorum, ışınlanabiliyorum, uzun mesafeleri sık sık kullanabiliyorum. Kırmızı Göz'le bina seviyesinde yıkım yapabilirim, sınırlı miktarda enerji emebilirim."*

*"Ve daha güçlü formların da var,"* dedi Steve, sesi hem meraklı hem de temkinli.

*"Var,"* dedin, kısaca. Bunun ne kadarını açıklayacağına karar vermek istercesine bir an durdun. *"Ama onlardan bahsetmeyeceğim şu an. Bir yabancı grubuna, ilk tanıştığım gün, tam güç tavanımı anlatmam gerekmiyor. Bu kadarı bile fazla cömertlik sayılır."*

Thor gürledi, memnun bir kahkaha gibi. *"Bilge bir gezgin. Gücünü göstermek kadar, göstermemeyi bilmek de bir güçtür."*

Stark'ın gözleri hâlâ küçük ışık küresinin kaybolduğu yerdeydi, düşünceli. *"Bina seviyesi yıkım,"* dedi, sanki kendi kendine sayıları hesaplıyormuş gibi. *"Ve bu senin *zayıf* formun."*

*"Nispeten zayıf,"* diye düzelttin. *"Kelimeler önemli."*"""
                }

                c1Text + """

---

Konuşma saatlerce sürdü, ya da öyle hissettirdi — şartlar, sınırlar, kimin neyi bilmesi gerektiği, senin neyi paylaşmayacağın. Bir noktada Steve, Fury'nin adını andı, S.H.I.E.L.D.'in seni "risk sınıflandırması dışı" olarak işaretlemiş olmasının aslında bir tehdit değil bir çaresizlik itirafı olduğunu söyledi — onları anlayamadıkları için korktuklarını. Bu, içinde beklemediğin bir şeyi kıpırdattı; anlaşılmak, sana onlarca yıldır teklif edilmemiş bir şeydi.

Steve sana bakarak son sözünü söyledi: *"Karşılığında kurallara uyarsın. Sivillere zarar yok. Emir zinciri var, tam bir ordu değil ama tam bir anarşi de değil. Ve şeffaflık — gücünün ne olduğunu, sınırlarının ne olduğunu bilmemiz gerekiyor. Güven iki taraflı işler."*

---

🔀 Steve'in "Güven iki taraflı işler" sözüne nasıl karşılık vereceksin?

1️⃣ Alaycı bir karşılık ver ("Güven" kelimesini sorgulayan yanıt).
2️⃣ Ciddi bir şekilde karşılık ver — güvenin neden bu kadar zor olduğunu kısaca açıkla.
3️⃣ Hiç cevap verme, konuyu değiştir."""
            }
            7 -> {
                // Response to Chapter 2 Choice 2 -> Up to Chapter 2 Choice 3
                val c2Text = if (choiceIs(lastUserMsg, 2, "Ciddi", "zor olduğunu", "güvenin zor", "dürüst", "söyle")) {
                    """Bakışlarını Steve Rogers'a diktin, sesindeki soğuk zırhı bir anlığına indirerek dürüstçe konuştun.

*"Güven benim için bir lüks değil, ölümcül bir hata oldu hep,"* dedin alçak ama kararlı bir sesle. *"Beş yaşından beri kaçıyorum. Güvendiğim her yer yıkıldı, arkamı döndüğüm herkes ya korktu ya da kelepçe getirdi. İki taraflı güven güzel bir masal Steve, ama ben masallara inanmayı çok önce bıraktım."*

Steve'in çenesi gevşedi, gözlerinde derin bir anlayış ve saygı parıltısı belirdi. *"Anlıyorum. O zaman sana söz kelimelerle değil, eylemlerle kanıtlanacak."*"""
                } else if (choiceIs(lastUserMsg, 3, "Cevap verme", "değiştir", "konuyu değiştir", "yanıt verme", "sessiz kal")) {
                    """Steve'in kelimeleri havada asılı kaldı. Hiç cevap vermedin. Bakışlarını onun gözlerinden çekip masadaki kahve fincanına çevirdin, konuyu tamamen yanıtsız bırakarak sessizliğe gömüldün.

Steve hafifçe iç çekti. Güvensizliğin duvarlarını zorlamayacağını gösteren bir tavırla geri çekildi. Ortamdaki sessizlik tekinsiz bir derinlik kazandı."""
                } else {
                    """Güven kelimesi göğsünde tanıdık bir şekilde sıkıştı — hafif, otomatik bir savunma refleksi, yıllar içinde o kadar derine işlemiş ki artık düşünmeden tepki veriyordu.

*"Güven,"* dedin, kelimeyi neredeyse tadına bakar gibi tekrarlayarak. *"İlginç kelime, hiç tanımadığınız ve sekiz aydır peşinde olduğunuz birine söylemek için."*

Steve gözlerini kısmadan sana baktı, ama duruşunu bozmadı. *"Belki de alışık olmadığın içindir Blackwood."*"""
                }

                c2Text + """

---

Gece ilerledikçe, oda yavaş yavaş boşaldı. Thor, bir kutlama vaadiyle (senin hiç kabul etmediğin bir vaatle) ayrıldı. Steve, "düşünmen için zaman" diyerek çekildi, seni bir karara zorlamadan — bu, dosyalarda okuduğun adamla örtüşen bir şeydi, ve bunu takdir ettin, istemeden de olsa.

Stark en son ayrılanlardan biriydi, kapıda durup arkasını döndü. *"Bak,"* dedi, sesindeki her zamanki tiyatral ton bir anlığına düşerek. *"Bilmiyorum kaç kere reddedildin, kaç kere yalan söylendi sana. Ama bu bina, sandığından daha fazla insanı kurtardı. Sen de kurtarılmaya değersin, Blackwood. Düşün bunu."*

Bunu söylemesini beklemiyordun. Cevap vermedin, ama kelimeleri bir yere koydun — atmadın, ama hemen de kabul etmedin.

Oda tamamen boşaldığında, sadece Natasha kaldı, masanın kenarına yaslanmış, seni izliyordu.

*"Herkes gitti de sen gitmedin,"* dedin, ayakta kalkıp fincanı bırakarak.

*"Herkes ikna etmeye çalıştı,"* dedi, omuz silkerek. *"Ben ikna etmiyorum."*

*"Peki ne yapıyorsun?"*

*"Bekliyorum,"* dedi, basitçe. *"Sen adım atana kadar. Ya da atmayana kadar. İkisi de benim için sorun değil."*

Bunun ne kadar tuhaf bir rahatlama olduğunu tarif edemezdin. On beş yıldır herkes ya senden korktu ya da seni bir şeye ikna etmeye çalıştı — bir silah, bir dosya, bir çözülmesi gereken problem olarak. Bu kadın, ilk kez, sana sadece *zaman* teklif ediyordu, karşılığında hiçbir şey istemeden.

*"Neden umursuyorsun?"* diye sordun, sesindeki merakı saklamaya çalışmadan.

*"Umursamıyorum,"* dedi, ama sesinde bir yalan yoktu, sadece dürüst bir düzeltme. *"Henüz. Ama gölgelerin içinde büyümüş biri, başka bir gölgeyi tanır. Sen benim tanıdığım bir dille konuşuyorsun, Blackwood. İstesen de istemesen de."*

---

🔀 Natasha'nın bu sözlerine nasıl karşılık vereceksin?

1️⃣ Sessiz kal, sadece bakışların bir saniye fazladan onda kalsın.
2️⃣ Sözlü olarak karşılık ver — onun da bir şey sakladığını ima et.
3️⃣ Mesafe koy, konuşmayı sonlandır, odadan çık."""
            }
            8 -> {
                // Response to Chapter 2 Choice 3 -> Up to Chapter 2 Choice 4
                val c3Text = if (choiceIs(lastUserMsg, 2, "Sözlü", "sakladığını", "gölge", "itiraf", "aynı dil")) {
                    """*"Gölgeler güzel kılıflardır Romanoff,"* dedin kısık bir sesle, gözlerinin içine bakarak. *"Ama ikimiz de biliyoruz ki gölgede durmak insanı temiz yapmaz. Sen geçmişinin izlerini ne kadar iyi saklarsan sakla, aynı dili konuştuğumuz doğru."*

Natasha'nın dudaklarında ince, neredeyse fark edilmez bir tebessüm belirdi. *"En azından yalan söylemiyorsun Blackwood. Bu Kule'de nadir bulunan bir özelliktir."*"""
                } else if (choiceIs(lastUserMsg, 3, "Mesafe", "çık", "mesafe koy", "odadan çık", "farklı dil")) {
                    """*"Farklı diller konuşuyoruz Romanoff,"* dedin mesafeli ve soğuk bir ifadeyle. Fincanı masaya bıraktın ve ona daha fazla bakmadan arkana dönüp odadan çıktın.

Natasha arkandan bakarken engel olmaya çalışmadı, gölgelerin arasındaki mesafeyi korumana saygı duydu."""
                } else {
                    """Bunun üzerine bir şey söylemedin. Ama bakışların bir saniye fazla onda kaldı, ve o da bunu fark etti — ama üstüne gitmedi, sadece kaydetti, tıpkı senin her şeyi kaydettiğin gibi.

Natasha hafifçe başını salladı ve sessizce kapıya doğru yürüdü."""
                }

                c3Text + """

---

Wanda'yla karşılaşman ise çok daha kazara oldu.

Tower'ın ortak katındaki mutfağa, herkes gittikten sonra, sırf başka bir şey yapacak bir şey bulmak için indin — belki de sadece odandan çıkmak için bir bahane arıyordun, kapalı dört duvar sana her zaman biraz fazla küçük geliyordu. O da oradaydı, tezgahın kenarında, elinde bir kupa çay, parmaklarının ucunda hâlâ o hafif kızıl ışıltı, sanki hiç tam olarak sönmüyormuş gibi.

Seni görünce irkilmedi. Bu, seni biraz şaşırttı — çoğu insan seni görünce en azından bir kez göz kırpıştırırdı, gözlerinin ağırlığından.

*"Uyuyamıyor musun?"* diye sordu, sesi yumuşak, aksanı hafif.

*"Alışkanlık değil,"* dedin, doğruyu söyleyerek. *"Uyumak, güvende hissetmeyi gerektirir. Ben pek güvende hissetmem."*

*"Burada bile mi?"* Sorusu meraklıydı, yargılamıyordu.

*"Henüz karar vermedim,"* dedin.

Başını hafifçe eğdi, sanki seni bir kitap gibi okuyormuş gibi — ama tuhaf bir şekilde, bunun rahatsız edici olmadığını fark ettin. *"Beni tanımıyorsun,"* dedi, *"ama tahmin edeyim — kayıp hissediyorsun. Buraya ait olmak seni korkutuyor, çünkü ait olduğun her şey daha önce elinden alındı."*

---

🔀 Wanda'nın bu derin içgörüsüne nasıl tepki vereceksin?

1️⃣ Şüpheci tepki ver — zihin okuma suçlaması ("Zihnimi mi okuyorsun?").
2️⃣ Kabul et — evet, kayıp hissediyorum, açıkça söyle.
3️⃣ Konuyu kapat, sert bir şekilde geri çekil."""
            }
            9 -> {
                // Response to Chapter 2 Choice 4 -> Up to Chapter 2 Choice 5
                val c4Text = if (choiceIs(lastUserMsg, 2, "Kabul", "açıkça", "itiraf", "haklısın", "kaybolmak")) {
                    """Sessizlik mutfağı kapladı. Gözlerini tezgahtaki çay kupasına indirdin ve derin bir nefes aldın.

*"Haklısın,"* dedin ilk kez bu kadar açık konuşarak. *"Ait olduğum her şey, tutunduğum her ev ellerimin arasından kayıp gitti. Kaybolmak kolay Wanda, asıl zor olan bir yere ait olmayı yeniden öğrenmek."*

Wanda'nın bakışlarında yumuşak bir sıcaklık belirdi, elindeki kupayı sıkarken başıyla onayladı. *"Biliyorum Aiden. O korkuyu ben de yaşadım. Yalnız değilsin."*"""
                } else if (choiceIs(lastUserMsg, 3, "Sert", "kapat", "sert cevap", "geri çekil", "bilmiyorsun")) {
                    """*"Aklımı okumaya çalışma,"* dedin sert ve soğuk bir tonla. Tek adımla geriye çekildin. *"Benim hakkımda hiçbir şey bilmiyorsun ve bildiğini sanma."*

Wanda öfkelenmedi, sadece gözlerindeki o hüzünlü derinlikle sana baktı. *"Özür dilerim. Sadece hissettiklerimi söyledim."*"""
                } else {
                    """Bunun ne kadar isabetli olduğu seni bir an durdurdu. *"Zihnimi mi okuyorsun?"* diye sordun.

*"Hayır,"* dedi, hafifçe gülümseyerek, ilk kez o akşam gerçek bir gülümseme gördün onda. *"Sadece dinliyorum. Bazen aynı şey gibi görünüyor ama değil."*

*"Sen de kaybettin bir şeyler,"* dedin, onun enerjisindeki o hafif, iyileşmemiş kırığı fark ederek — kendi türünden birinin başka birinde tanıyabileceği bir şey.

Gülümsemesi biraz soldu, ama tamamen kaybolmadı. *"Herkes bir şey kaybeder,"* dedi. *"Fark, onunla ne yaptığın."*"""
                }

                c4Text + """

---

Bunun üzerine bir süre sessiz kaldınız, ikiniz de mutfağın loş ışığında, ne söyleyeceğinizi bilmeden ama konuşmaya da gerek duymadan. Sonunda o çayını bitirdi, kupayı lavaboya bıraktı.

*"İyi geceler, Aiden Blackwood,"* dedi, adını ilk kez telaffuz ederken, sanki tadına bakıyormuş gibi. *"Karar neyse, umarım seni kendine daha az yalnız hissettiren bir karar olur."*

Çıktı, ve sen mutfakta yalnız kaldın, onun söylediği son cümleyi kafanda birkaç kez tekrarlarken.

Sana verdikleri oda —"misafir odası" dediler, ama kilitli değildi, ki bunun bilinçli bir seçim olduğunu anladın— New York'un ışıklarına bakıyordu. Yatağa oturmadın. Pencerenin önünde durdun, şehri izleyerek, alışkanlıkla çıkışları sayarak — pencere, kapı, acil merdiven, üç farklı ışınlanma rotası zihninde hazır.

Ama bu gece, ilk kez uzun zamandır, o rotaları hemen kullanmayı düşünmedin.

Yirmi yıl boyunca "aile" kelimesi senin için bir yara izi gibiydi — dokunulduğunda hâlâ acıyan ama artık kanamayan bir şey. Bu insanlar sana bir aile teklif etmiyorlardı, en azından açıkça değil. Ama masada oturdukların, mutfaktaki o sessizlik, Natasha'nın "bekliyorum" demesi — hiçbiri sana alışık olduğun taktiklerden değildi. Kimse seni bir şeye zorlamıyordu. Kimse senden hemen bir cevap istemiyordu.

Ve belki de asıl korkutucu olan buydu.

Kırmızı Göz'ünün gücünü, Beyaz Göz'ünün sınırlarını, ışınlanmanın seni ne kadar yorduğunu biliordun — bunlar hesaplanabilir şeylerdi, sınırları olan tehlikelerdi. Ama bu — bir masaya oturmak, bir kadının sana "gölgelerin içinde büyümüş biri başka bir gölgeyi tanır" demesi, başka birinin sana "kendine daha az yalnız hissettiren bir karar" dilemesi — bunun hiçbir sınırını bilmiordun. Ve sınırını bilmediğin hiçbir şeye güvenmemiştin şimdiye kadar.

Alnını cama dayadın, New York'un ışıkları gözlerinin önünde bulanıklaşırken.

---

🔀 Gece boyunca aldığın tüm izlenimleri pencereden New York'a bakarken nasıl değerlendiriyorsun?

1️⃣ Temkinli iyimserlik — belki bu sefer farklı olabilir düşüncesi ağır basıyor.
2️⃣ Güçlü şüphe — hâlâ kaçmayı, gitmeyi düşünüyorsun, sadece erteliyorsun.
3️⃣ Belirsiz/karışık — ne tam güven ne tam red, gerçek bir iç çatışma."""
            }
            10 -> {
                // Choice 5 Response -> Chapter 2 Ending
                val c5Text = if (choiceIs(lastUserMsg, 2, "şüphe", "kaçmayı", "şüpheci", "güvenmiyorum", "kaçmaya hazır")) {
                    """Penceredeki yansımanda gözlerinin kızıl parıltısını izledin. Yılların getirdiği şüphe bir gecede silinecek kadar zayıf değildi. Stark'ın sözleri, Rogers'ın kuralları, hatta Natasha ve Wanda'nın yaklaşımı... Hepsi birer strateji olabilirdi. Şimdilik kalıyordun ama bavulunu zihninde hiç toplamadın. İlk fırsatta, ilk yanlış adımda ışınlanıp kaybolmaya hazırdın.

Karar vermemiştin henüz. Kaçış rotaları zihninde hâlâ taze ve hazırdı.

---

📖 BÖLÜM 2 SONU — ŞARTLAR

Aiden Blackwood şartları dinledi ancak gardını tek bir an bile indirmedi.
Kule'deki ilk gecesinde kaçış planlarını zihninde taze tutarak şüpheyle bekledi."""
                } else if (choiceIs(lastUserMsg, 3, "Belirsiz", "karışık", "çatışma", "bilemiyorum", "emin değilim")) {
                    """Zihninde iki farklı Aiden çatışıyordu — beş yaşında ailesini kaybedip 20 yıldır kaçan o yalnız çocuk ile masadaki adamların dürüstlüğünü hisseden adam. Ne tam güvenebiliyordun ne de sırtını dönüp gidebiliyordun. Bu bilinmezlik, Kırmızı Göz'ün yıkıcılığından bile daha karmaşıktı.

Alnını soğuk cama dayadın. Karar vermemiştin henüz. Ama ilk kez uzun zamandır, kararı vermek için acele etmiyordun.

---

📖 BÖLÜM 2 SONU — ŞARTLAR

Aiden Blackwood şartları dinledi ve Kule'deki ilk gecesinde derin bir iç çatışmayla baş başa kaldı.
Geleceğin ne getireceğini zaman gösterecek."""
                } else {
                    """İçindeki o katı zırh ilk kez hafifçe gevşedi. New York'un gökdelenlerindeki ışıklar bir tehdit gibi değil, uzun zamandır aradığın bir sığınak gibi parıldadı zihninde. Yirmi yıldır ilk kez kaçış rotalarını hesaplamayı bıraktın. Belki de bu insanlar gerçekten farklıydı. Belki de Avengers Tower, kaçmak zorunda kalmayacağın ilk yer olabilirdi.

Karar vermemiştin henüz. Ama ilk kez uzun zamandır, kararı vermek için acele etmiyordun ve içinde küçük bir umut kıvılcımı yanıyordu.

---

📖 BÖLÜM 2 SONU — ŞARTLAR

Aiden Blackwood şartları dinledi, gücünü gösterdi ve Avenger üyeleriyle derin temaslar kurdu.
Kule'deki ilk gecesinde temkinli bir iyimserlikle kararını şekillendirdi."""
                }

                c5Text + """

Tebrikler! Bölüm 2'yi başarıyla tamamladın."""
            }
            11 -> {
                progress = progress.copy(chapterNumber = 3)
                storyProgressDao.insertOrUpdate(progress)

                val b3Full = b3Text ?: """# 📖 BÖLÜM 3: DENEME SÜRESİ"""
                b3Full + """

---

🔀 Steve'in Otuz Günlük Deneme Süresi teklifine nasıl cevap vereceksin?

1️⃣ Şartlı kabul — kendi çıkış kurallarını belirt.
2️⃣ Tereddütsüz kabul et.
3️⃣ Çok daha kısa bir süre öner ("birkaç gün")."""
            }
            12 -> {
                val c1Text = if (choiceIs(lastUserMsg, 2, "Tereddütsüz", "kabul", "tamam otuz gün", "evet", "anlaştık")) {
                    """"Tamam," dedin, sesin kendi kulağına bile tereddütsüz ve kararlı çıktı. "Otuz gün. Görelim bakalım Avengers Tower dedikleri yer ne kadar dayanıklıymış."
Steve'in yüzünde derin bir memnuniyet ve gerçek bir takdir belirdi. "Aramıza hoş geldin Aiden. Bu karardan pişman olmayacaksın."
Gözlerinin içine bakarak sözlerini tarttın. On beş yıldır ilk kez bir sözün arkasında durmaya niyetlenen adamlara denk geliyordun."""
                } else if (choiceIs(lastUserMsg, 3, "kısa", "birkaç gün", "az süre", "uzun değil", "üç gün")) {
                    """"Otuz gün çok uzun Steve," dedin mesafeli bir tonla. "On beş yıldır hiçbir yerde otuz saat bile durmadım. Birkaç gün diyelim — üç ya da beş gün. Sonrasına duruma göre bakarız."
Steve anlayışla başını salladı, seni zorlamadı. "Pekala. Gün gün ilerleyelim. Sınırlarına saygı duyarım. İster üç gün olsun ister otuz, burada olduğun sürece bizim müttefikimizsin."
Mesafeli tavrını koruyarak başınla onayladın."""
                } else {
                    """"Tamam," dedin, sesin kendi kulağına bile garip geldi, bu kadar kolay çıktığı için. "Otuz gün. Ama kendi şartlarımla. İstediğim an kapıdan çıkarım ve kimse önüme geçmeye çalışmaz."
Steve'in yüzünde hafif, gerçek bir gülümseme belirdi — kutlama değil, sadece memnuniyet. "Anlaştık Blackwood. Biz hapishane işletmiyoruz. Kapı daima açık."
Steve'in netliği göğsündeki o eski savunma zırhını bir anlığına hafifletti."""
                }

                c1Text + """

Steve tabletini eline aldı, ekranı hafifçe kaydırarak sana baktı.
*"İyi. O zaman seni ekiple tanıştıralım, resmi olarak. Ve —"* tabletine bakıp devam etti, *"— birkaç şey netleştirmemiz gerekiyor. Yeteneklerinin sınırlarını bilmemiz lazım, sadece merak değil, güvenlik için. Antrenman salonunda bir değerlendirme yapabilir miyiz?"*

İçinde bir şey gerildi otomatik olarak — *değerlendirme*, *test*, bu kelimeler sende hep aynı tepkiyi uyandırırdı, laboratuvarları, dosyaları, seni bir "vaka" olarak gören gözleri hatırlatarak. Ama Steve'in ses tonunda o eski soğukluk yoktu.

*"Kontrollü olacak,"* diye ekledi Steve, senin yüzündeki anlık gerilimi fark ederek. *"Sen ne kadar göstermek istersen o kadar. Kimse seni zorlamayacak."*

---

🔀 Antrenman salonundaki değerlendirmeyi nasıl kabul edeceksin?

1️⃣ Temkinli kabul et. ("Görelim... ama sınırlarımı ben belirlerim.")
2️⃣ İstekli/meraklı kabul et. ("Ekibin sınırlarını ve senin dövüş stilini merak ediyorum.")
3️⃣ Reddet, sadece sözlü açıklamayla yetin."""
            }
            13 -> {
                val c2Text = if (choiceIs(lastUserMsg, 2, "İstekli", "merak", "meraklı", "görmek istiyorum", "izlemek")) {
                    """"Görelim," dedin, gözlerinde meraklı ve meydan okuyan bir pırıltıyla. "Dürüst olmak gerekirse Kaptan Amerika'nın dövüş stilini ve ekibin kapasitesini canlı izlemeyi merak ediyordum zaten."
Steve hafifçe sırıttı, meydan okumanı memnuniyetle karşıladı: "Seni hayal kırıklığına uğratmamaya çalışırım Blackwood. Bakalım efsaneler kadar hızlı mısın."
Birlikte koridordan aşağı, güçlendirilmiş antrenman katına doğru adımladınız."""
                } else if (choiceIs(lastUserMsg, 3, "Reddet", "sözlü", "gerek yok", "teorik", "anlatırım")) {
                    """"Fiziksel bir teste gerek yok," dedin kollarını kavuşturarak. "Yeteneklerimi teorik olarak anlatırım, bu güvenlik için yeterli olur."
Steve hafifçe gülümsedi. "Teori güzeldir ama antrenman salonunda kahve ve ekip var. Sadece bir tur görmek bile şüpheleri siler. Gel, sadece ortamı gör, zorlama yok."
Steve'in ısrarcı ama dostça davetiyle antrenman salonuna doğru ilerlediniz."""
                } else {
                    """"Görelim bakalım," dedin, sesindeki zırhı koruyarak. "Ama sınırlarımı ben belirlerim Steve. Dur dediğim an her şey durur."
Steve başıyla onayladı: "Söz. Kontrol tamamen sende."
Steve öne düştü ve Kule'nin alt katlarındaki devasa antrenman alanına indiniz."""
                }

                c2Text + """

---

## ANTRENMAN SALONU

Antrenman salonu, beklediğinden çok daha büyüktü — güçlendirilmiş çelik duvarlar, yüksek enerji emici paneller, tavanda yüksek hızlı tarama kameraları ve köşede Thor'a ait antika görünümlü hedef büstleri.

Steve, Natasha ve şaşırtıcı bir şekilde Wanda da oradaydı. Wanda kenarda duruyor, kollarını kavuşturmuş, ilgisi uyanmış bir şekilde izliyordu. Bir de tanımadığın uzun boylu, gözlüklü bir adam vardı — elinde dijital bir biyometrik tablet.

*"Bruce,"* diye tanıttı kendini adam. *"Bruce Banner. Sadece verileri izliyorum, endişelenme."*

Steve salonun ortasına doğru ilerledi, eldivenlerinin kayışlarını sıktı.
*"Basit başlayalım,"* dedi Steve, sana dönerek. *"Fiziksel güç, hız, refleksler ve dayanıklılık. Benimle. Kaptan Amerika'ya karşı bir tur. Ne kadar ileri gitmek istersen o kadar."*

Steve kalkanını koluna takıp dövüş pozisyonu aldı. Bütün ekip ve Bruce Banner gözlerini sana çevirdi.

---

🔀 Steve Rogers'a karşı dövüşte gücünü nasıl ayarlayacaksın?

1️⃣ Tam geri tut. (Kontrollü, Steve'e zarar vermeden hızını ve iyileşmeni göster)
2️⃣ Biraz daha zorla ama yenme. (Sınırlarını zorla, Steve'in savunmasını sına)
3️⃣ Hiç geri tutma, kazanmaya çalış."""
            }
            14 -> {
                val c3Text = if (choiceIs(lastUserMsg, 3, "Hiç geri tutma", "kazanmaya", "tam güç", "geri tutma", "sınır yok")) {
                    """Geri tutmadın. Kırmızı Göz'ünün sarsıcı ivmesi gözlerinde alevlendi! Salondaki hava basıncı aniden düştü, zemin titredi. Saniyeden kısa bir sürede mavi ve kırmızı bir kuantum çizgisi halinde Steve'in üzerine atıldın. Steve kalkanını kaldırmaya fırsat bulamadan kalkanın kenarına indirdiğin kinetik şok dalgası kalkanı yana savurdu ve elini Steve'in boğazının bir santim önünde durdurdun!

Salonda derin bir sessizlik oldu. Bruce Banner dehşetle ve hayranlıkla tabletine notlar aldı. Steve yutkunarak doğruldu ve takdirle gülümsedi: "Muazzam bir ivme... Hızın kalkanımdan bile önce ulaşıyor. Kazandın Blackwood."
Natasha etkilenmiş bir şekilde mırıldandı: "Sadece hızlı değil, dövüş geometrisini anında yok ediyor.""""
                } else if (choiceIs(lastUserMsg, 2, "zorla", "sınıra çıkar", "maksimum", "sonuna kadar")) {
                    """Gözlerinde Kırmızı Göz'ün hafif ivmesi parıldadı. Hızını ve reflekslerini tam sınıra çıkardın! Steve'in kalkan hamlesini saniyenin onda birinde savuşturup arkasına geçtin ve sırtına kontrollü ama güçlü bir darbe indirdin. Steve kalkanını son anda arkasına çevirip darbeni güçlükle göğüsledi, ayakları güçlendirilmiş zeminde iki metre geriye kaydı.

Steve derin bir nefes alıp gülümsedi: "İşte bu gerçek bir refleks testi. Savunmamı bu kadar zorlayan az insan gördüm."
Natasha gözlerini kısarak mırıldandı: "Mükemmel bir zamanlama ve fiziksel kontrol."
Bruce Banner tabletindeki grafiklere bakarak şaşkınlıkla notlar aldı."""
                } else {
                    """Geri tuttun. Steve hamle yaptı — kalkanı havayı yararak sana doğru savruldu. Ama senin için zaman adeta yavaşladı. Işınlanmaya bile gerek duymadan, sadece vücut reflekslerinle kalkanın altından süzüldün. Steve'in hamlesi boşa çıktı. İkinci hamlesinde kalkanın kenarı omzuna sertçe çarptı, ama bir saniye içinde yaralanan dokularının mavi kıvılcımlarla kendi kendini onardığını herkes gördü.

*"Ciddi yaralar dakikalar sürer,"* dedin sakince. *"Ölümcül olmayan her şey saniyeler içinde kapanır."*
Natasha kenardan hafif, takdir dolu bir ıslık çaldı. Steve ise kalkanını indirip doğruldu: "Hızın ve hücresel iyileşmen inanılmaz. Kendini harika kontrol ediyorsun.""""
                }

                c3Text + """

Steve terini silip salondaki güçlendirilmiş enerji panellerine doğru yürüdü.
*"Fiziksel kapasiteni gördük,"* dedi Steve, panelleri göstererek. *"Şimdi sıra enerji tarafında. Sadece görmek için — küçük ölçekte, güvenlik protokolleriyle. Bu paneller yüksek wattlı enerjiyi emebiliyor. Seni ya da odayı riske atmadan kozmik güç çıktını ölçebiliriz."*

Stark'ın sesi interkomdan duyuldu: *"Banner ekranları açtı. Göster bakalım elimizde ne var Kozmik Gezgin."*

---

🔀 Enerji testinde panellere ne seviyede güç uygulayacaksın?

1️⃣ Orta seviye göster (kontrollü, hedefli enerji dalgası).
2️⃣ Minimum göster (sadece hafif bir kıvılcım dalgası).
3️⃣ Neredeyse tam kapasiteye yakın göster (güçlü bir patlama)."""
            }
            15 -> {
                val c4Text = if (choiceIs(lastUserMsg, 2, "Minimum", "az", "minimum güç", "cimri", "kıvılcım")) {
                    """Elini kaldırdın ve panellere sadece parmak ucundan minik, zararsız bir mavi kıvılcım dalgası fırlattın. Paneldeki ibre hafifçe kıpırdadı.

*"Fazlasına gerek yok,"* dedin ellerini cebine sokarak. *"Gücümü şov malzemesi yapmayı sevmem."*
Stark interkomdan mırıldandı: "Cimri çıktı bizimki... Ama o kadarlık kıvılcımda bile frekans yoğunluğu korkutucu. Biyometrik imzan benzersiz."
Steve gizemini korumana saygı duyarak başını salladı."""
                } else if (choiceIs(lastUserMsg, 3, "tam kapasite", "tüm güç", "tam gaz", "patlama", "devasa")) {
                    """Elini kaldırdın ve gözlerin Kırmızı Göz'ün aleviyle parladı! Panellere doğru devasa bir kozmik enerji patlaması gönderdin! Bütün antrenman salonu sarsıldı, duvar panelleri kırmızı alarmlarla öttü, Stark'ın tabletindeki ölçüm grafikleri tavan yaptı!

Stark heyecanla bağırdı: "Muazzam bir çıktı! Bu çocuk adeta yürüyen bir reaktör! Sayılar büyüleyici!"
Wanda etkilenmiş bir şekilde geri çekildi, Bruce Banner ise şaşkınlıkla gözlüklerini düzeltti."""
                } else {
                    """Elini kaldırdın, ve bu sefer sadece bir ışık küresi değil, odaklanmış gerçek bir enerji dalgası fırlattın. Panel titredi, üzerindeki mavi gösterge çizgileri hızla yükseldi ve sistem fanları yüksek devirde çalışmaya başladı.

*"Bu daha orta seviyem,"* dedin sakince.
Wanda ekrana bakarak *"Bu ölçüm bina seviyesinden fazla bir potansiyel gösteriyor,"* dedi.
Bruce Banner ise *"Enerji imzan bilinen tüm kategorilerin ötesinde, tamamen kendine has,"* diye ekledi."""
                }

                c4Text + """

---

## ARADA BİR MOLA

Antrenman bittikten sonra ekip üstünü değiştirdi. Natasha yanına geldi: *"İyi iş çıkardın Blackwood. Güç, ne zaman ve ne kadar kullanılacağını bilmekle anlamlı hale gelir."*

Öğleyin Kule'nin ortak kafeteryasında büyük masaya oturdunuz. Sam Wilson (Falcon) elinde tepsisiyle masaya geldi. Seni süzüp genişçe gülümsedi.

*"Demek Kule'nin yeni efsanesi sensin,"* dedi Sam. *"Steve seni antrenmanda öve öve bitiremedi. 'Işık hızında ve kalkanımı zorlayabiliyor' diyor. Umarım mutfaktaki kahve makinesini ışınlayıp ortadan kaybetmezsin!"*

Masadaki herkes —Steve, Natasha, Wanda, Bruce— hafifçe güldü ve hepsi senin vereceğin tepkiyi bekledi.

---

🔀 Öğle yemeğinde Sam Wilson'ın şakasına nasıl tepki vereceksin?

1️⃣ Kuru bir espriyle karşılık ver. (Masa kahkahaya boğulsun)
2️⃣ Sessiz kal, sadece hafifçe gülümse.
3️⃣ Şakayı ciddiye al, savunmaya geç."""
            }
            16 -> {
                val c5Text = if (choiceIs(lastUserMsg, 3, "ciddiye", "savunma", "ciddi", "savunmaya geç", "şakayı ciddiye")) {
                    """"Cihazlarla ya da şakalarla ilgilenmiyorum Wilson," dedin soğuk ve ciddi bir tonda.
Masada anlık bir sessizlik oldu. Sam ellerini kaldırıp gülümsedi: "Pekala dostum, sadece havayı yumuşatmaya çalışıyordum. Sorun yok." Steve araya girerek ortamı dengelendi."""
                } else if (choiceIs(lastUserMsg, 2, "Sessiz", "sessiz kal", "gülümse", "sakin kal")) {
                    """Sessiz kaldın, ama dudaklarında hafif, mesafeli bir gülümseme belirdi. Tepkisizliğin ve sakinliğin Sam'in şakasını tatlı bir saygı havasına dönüştürdü.
Sam sandalyesini çekip oturdu: "Sessiz tipleri severim. Saygılar dostum." Ekip yemeğe neşeyle devam etti."""
                } else {
                    """"Kahveyi değil ama seni Kule'nin çatısına ışınlayabilirim Wilson," dedin kuru ve ciddi bir tonda.
Masa bir anda kahkahaya boğuldu! Sam elini kalbine koyup taklit yaptı: "Tamam, tamam, geri çekiliyorum! Adam tehlikeli çıktı!" Yıllar sonra ilk kez bir masada gülmenin ve kabul görmenin tadını çıkardın."""
                }

                c5Text + """

---

## ALARM & S.H.I.E.L.D. TEHDİDİ

Öğleden sonra Kule'nin kırmızı acil durum alarmları aniden çalmaya başladı! Kırmızı ışıklar koridorlarda dönerken Stark'ın sesi interkomdan panikle yükseldi:
*"Millet! S.H.I.E.L.D.'den General Ross destekli ağır bir operasyon ekibi Kule'nin alt kapısında! Işınlanma enerjini ve kozmik imzanı takip etmişler. Seni teslim almadan gitmeyeceklerini söylüyorlar!"*

İçinde 20 yıllık kaçış mekanizması anında tetiklendi — *bulundun, hemen kaç!* Parmak uçlarında mavi kıvılcımlar çatırdamaya başladı.
Steve hızla önüne geçti, ellerini kaldırdı: *"Kaçmana gerek yok Blackwood! Sen bu Kule'nin çatısı altındasın. Bu artık bizim sorunumuz!"*
Natasha gözlerinin içine bakarak sertçe ekledi: *"Şimdi kaçarsan suçlu olduğunu kabul etmiş olursun. Kal ve arkamızda dur!"*

---

🔀 S.H.I.E.L.D. Alarmları çalarken ne yapacaksın? (DALLANMA NOKTASI)

1️⃣ Kal, hiç ışınlanma. (Kule'de kal ve ekibe güvenmeyi seç)
2️⃣ Refleksle çatıya ışınlan, birkaç dakika sonra kendi kararınla geri dön. (Kısa kaçış)
3️⃣ Tümünü geride bırakıp uzağa ışınlan. (Büyük kaçış)"""
            }
            17 -> {
                val c6Choice = lastUserMsg
                val isShortEscape = c6Choice.contains("2") || c6Choice.contains("çatıya") || c6Choice.contains("Kısa")
                val isBigEscape = c6Choice.contains("3") || c6Choice.contains("uzağa") || c6Choice.contains("Büyük")

                progress = if (isBigEscape) {
                    progress.copy(currentBranch = "kacis_thor", flightInstinct = progress.flightInstinct + 2, trustAvengers = "leaning_negative")
                } else if (isShortEscape) {
                    progress.copy(currentBranch = "kisa_kacis", flightInstinct = progress.flightInstinct + 1, wandaBond = progress.wandaBond + 1)
                } else {
                    progress.copy(currentBranch = "main", trustAvengers = "leaning_positive")
                }
                storyProgressDao.insertOrUpdate(progress)

                val outcomeText = if (isBigEscape) {
                    val part1 = b3Kacis ?: """Onları dinlemedin..."""
                    val part2 = b3Thor ?: """ÜÇ GÜN SONRA..."""
                    part1 + "\n\n---\n\n" + part2
                } else if (isShortEscape) {
                    b3KisaKacis ?: """Natasha'nın sözleri kulağına ulaştı ama yirmi yıllık refleksin kelimelerden hızlı hareket etti! Bir mavi ışık patlamasıyla Kule'nin en üst heliped çatısına ışınlandın!"""
                } else {
                    """Işınlanmadın. Elindeki mavi kıvılcımları yavaşça söndürdün. Yirmi yıllık kaçış refleksini ilk kez bastırdın ve Kule'de kaldın.

Steve ve Natasha aşağı inip S.H.I.E.L.D. General Ross ve ekibini Avengers yetkisiyle geri çevirdi. Sen ve Wanda yüksek cam pencereden onları izlediniz. Wanda yanına gelip fısıldadı: *"Bazen kalmak, kaçmaktan çok daha büyük bir cesaret gerektirir. Doğru olanı yaptın Aiden."* Akşam olduğunda çatıya çıktın."""
                }

                outcomeText + """

---

## RECONVERGENCE — Çatı Sahnesi

Gece New York'un üzerine çöktü. Bütün şehir binlerce ışıkla ışıldıyordu.
Kule'nin heliped çatısına çıktın. Soğuk gece rüzgarı yüzüne vuruyordu.
Natasha gölgelerin arasından süzülüp yanındaki demirliklere yaslandı.

*"Kule'de ilk gecen,"* dedi Natasha, şehri izleyerek. *"New York ayaklarının altında... Nasıl hissediyorsun Aiden? Kalmak seni korkutuyor mu, yoksa alışıyor musun?"*

---

🔀 Çatıda Natasha seni bulduğunda ona karşı dürüstlüğün ne seviyede olacak?

1️⃣ Tam dürüst cevap ver. ("Korkutucu... Kalmak, kaybedecek bir şey biriktirmek demek.")
2️⃣ Yarım dürüst — şakayla hafiflet. ("Manzara güzel Romanoff, kaçmıyorum.")
3️⃣ Kapan, yüzeysel cevap ver. ("Sadece hava alıyordum, sorun yok.")"""
            }
            18 -> {
                val c7Text = if (choiceIs(lastUserMsg, 3, "Kapan", "yüzeysel", "hava alıyorum", "sorun yok")) {
                    """"Sadece hava alıyordum Romanoff. Endişelenecek bir şey yok," dedin mesafeli ve soğuk bir tonda.
Natasha başını salladı, zorlamadı: "Pekala. Sınırlarına saygı duyarım. Ama burada yalnız olmadığını unutma.""""
                } else if (choiceIs(lastUserMsg, 2, "Yarım", "şakayla", "kaçmıyorum", "manzara güzel")) {
                    """"Manzara güzel Romanoff. Endişelenme, bu sefer kaçmıyorum," dedin hafif bir tebessümle.
Natasha hafifçe gülümsedi: "Seni takip etmiyordum zaten. Sadece çatı havası iyi gelir. Otuz günün var, bu hissin tadını çıkar.""""
                } else {
                    """"Korkutucu," dedin dürüstçe. "Kaçmak kolaydır. Ama kalmak... bir yerlere kök salmak... kaybedecek bir şey biriktirmek demek."
Natasha New York ışıklarına bakarak devam etti: "Ben de Kızıl Oda'dan ayrıldıktan sonra tam olarak bunu hissettim. Kaybedecek şeylerin olması seni zayıflatmaz Blackwood. Onlar seni korumak için savaşacağın nedenlere dönüştürür.""""
                }

                c7Text + """

*"Otuz günün var,"* dedi Natasha ayağa kalkarken. *"Gör bakalım neye benziyor kalmak. İyi geceler Blackwood."*

Natasha ayağa kalkıp içeri girdi. Çatıda yalnız kaldın.
Cebinden gümüş kolye ucunu çıkardın — yirmi yıl önceki patlamadan ailesinden kalan tek hatıra. Ay ışığı kolyenin üzerinde parıldadı.

---

🔀 Kolye ile baş başa kaldığında zihnindeki iç ses nasıl bir kararla şekillenecek?

1️⃣ Kolyeyi elinde tutup umutla düşün. (Belki bu sefer bir şeyleri kaybetmeden tutabilirsin)
2️⃣ Kolyeyi hızla cebine geri koy, düşünceyi bastır. (Duygusal zayıflığa izin verme)
3️⃣ Kolyeye uzun uzun bak, geçmişe dair kısa bir anı/flashback zihninde canlansın."""
            }
            19 -> {
                val c8Text = if (choiceIs(lastUserMsg, 2, "cebine", "bastır", "cebine koy", "duygu bastır", "zayıflık")) {
                    """Kolyeyi hızla cebine geri koydun ve duygusal zayıflığa geçit vermedin. Soğukkanlı zırhını koruyarak Kule'ye ve yeni odana doğru adım attın.

---

📖 BÖLÜM 3 SONU — DENEME SÜRESİ

Aiden Blackwood 30 günlük deneme süresine adım attı, duygularını ve gardını kontrol altında tutarak geceyi tamamladı."""
                } else if (choiceIs(lastUserMsg, 3, "flashback", "bak", "geçmişe bak", "uzun uzun", "anı")) {
                    """Kolyeye bakarken 20 yıl önceki çocukluk evin ve alevlerin arasındaki o patlama anı gözlerinin önünden geçti. Geçmişin acısını kabullenerek Avengers Kulesi'nde yeni bir sayfa açtın.

---

📖 BÖLÜM 3 SONU — DENEME SÜRESİ

Aiden Blackwood geçmişinin anılarıyla yüzleşti ve Avengers Kulesi'nde 30 günlük deneme süresine adım attı."""
                } else {
                    """Kolyeyi avucunda sıktın. Yıllardır ilk kez içindeki o karanlık şüphe yerini umuda bıraktı. Belki de bu sefer bir şeyleri kaybetmeden tutabilirdin. Geceye gülümseyerek Kule'ye adım attın.

---

📖 BÖLÜM 3 SONU — DENEME SÜRESİ

Aiden Blackwood 30 günlük deneme süresini kabul etti, Avengers ekibiyle ilk bağlarını kurdu ve kolyeyi elinde tutarak umut dolu bir kararla geceyi tamamladı."""
                }

                c8Text + """

Tebrikler! Bölüm 3'ü başarıyla tamamladın."""
            }
            // ═══════════════════════════════════════════════════════════════
            // BÖLÜM 4: İLK GÖREV
            // ═══════════════════════════════════════════════════════════════
            20 -> {
                progress = progress.copy(chapterNumber = 4)
                storyProgressDao.insertOrUpdate(progress)
                val b4Text = loadAssetText("Bolum4_IlkGorev.md")
                    ?: "# 📖 BÖLÜM 4: İLK GÖREV\n\nDeneme süresinin yedinci günüydü."

                b4Text + """

---

🔀 Fury'nin göreve katılmak isteyip istemediğini sorduğu an — ne yapacaksın?

1️⃣ Hemen kabul et: "Varım." (Kısa, kararlı)
2️⃣ Şartlı kabul: "Giderim ama kendi kurallarımla."
3️⃣ Bir tur soru sor önce — depoyu, içinde ne olduğunu, ekibin planını öğren."""
            }

            21 -> {
                val isImmediate = choiceIs(lastUserMsg, 1, "Varım", "hemen", "kabul", "gidiyorum")
                val isConditional = choiceIs(lastUserMsg, 2, "şartlı", "kurallarımla", "kendi kuralım", "ama")
                progress = if (isImmediate) progress.copy(trustAvengers = "positive")
                           else if (isConditional) progress.copy(mysteryFactor = progress.mysteryFactor + 1)
                           else progress.copy(steveTrust = progress.steveTrust + 1)
                storyProgressDao.insertOrUpdate(progress)

                val resp = if (isImmediate) {
                    """*"Varım,"* dedin. Tek kelime, ama odadaki ağırlığı birkaç cümleden fazlaydı.

Steve'in gözlerinde kısa bir şaşkınlık geçti — beklemiyordu bu kadar hızlı. Sonra başını salladı, sakin ve ciddi: *"Tamam. Saat dörtte aşağıda."*

Natasha seninle çıkarken fısıldadı: *"İlk kez 'varım' diyorsun. Nasıl hissettiriyor?"*

Cevap vermedin. Ama hissin adını içinde biliyordun: *ağır*. İyi anlamda ağır — boş lafın değil, gerçek bir şeyin ağırlığı."""
                } else if (isConditional) {
                    """*"Giderim,"* dedin, *"ama kendi kurallarımla. Birinin arkasında saklanmıyorum, birini korumaya zorlanmıyorum ve durma kararı bende."*

Fury bir an baktı. Steve daha hızlı cevapladı: *"Kabul. Ekip içinde koordinasyon şart ama taktik kararlar serbest."*

Fury hologramda omuz silkti gibi yaptı — bu onun onayıydı demek ki.

Sam Wilson alçak sesle mırıldandı: *"Tanrım bu adam müzakereci olabilirdi."*"""
                } else {
                    """Masaya eğildin. *"Deponun planı var mı? İçeride kaç kişi bekleniyor? Alıcı tespit edildi mi?"*

Fury ilk kez doğrudan sana baktı — tek gözü, değerlendirici. *"İstihbarat sınırlı. Depoda en fazla sekiz kişi, ağır silahlı. Alıcı bilinmiyor."*

*"Bilinmiyorsa tuzak olabilir,"* dedin.

Natasha hafifçe başını eğdi. Steve'in yüzünde ince bir "haklı" ifadesi belirdi.

Fury: *"Bu yüzden sizi gönderiyorum. Soru mu bitti?"*"""
                }

                resp + """

---

Sabahın dördü yaklaşıyordu. Operasyon hazırlığı için antrenman salonuna indik.

Natasha ekipman kontrol ederken sana baktı: *"Depoya girişte farklı rotalar alacağız. Sen ve ben doğu girişinden, Steve ve Sam kuzeyden. Wanda dışarıda yedek. Eğer bir şey ters giderse — işaret ver, bekletme."*

---

🔀 Operasyon briefinginde kendine nasıl bir rol belirleyeceksin?

1️⃣ Keşif önü — ilk sen gir, koridorları tara, sonra ekibi çağır.
2️⃣ Natasha ile eş zamanlı giriş — yan yana, birbirinizi kapsayarak.
3️⃣ Geride kal, tüm takımı izle — büyük resmi sen gör."""
            }

            22 -> {
                val isScout = choiceIs(lastUserMsg, 1, "Keşif", "önden", "ilk ben", "tek gir", "önce gir")
                val isSync = choiceIs(lastUserMsg, 2, "Natasha", "eş zamanlı", "yan yana", "birlikte")
                progress = if (isScout) progress.copy(mysteryFactor = progress.mysteryFactor + 1)
                           else if (isSync) progress.copy(natashaBond = (progress.natashaBond ?: 0) + 1)
                           else progress.copy(steveTrust = progress.steveTrust + 1)
                storyProgressDao.insertOrUpdate(progress)

                val roleText = if (isScout) {
                    """*"Ben önden girerim,"* dedin. *"İçeriyi temizlerim, siz gelin."*

Natasha kaşını kaldırdı: *"Tek başına fazla risk."*

*"Tek başına daha hızlı,"* dedin.

Steve itiraz etmek istedi ama Natasha onu durdurdu: *"Tamam. Ama iletişimi kesme."* Earpiece'i uzattı, sessizce.

Deponun karanlığında yalnız ilerlerken fark ettin: yirmi yıldır böyle yürümüştün. Yalnız, sessiz, önceden. Ama şimdi kulaklıktan Natasha'nın nefesini duyabiliyordun — ve bu garip biçimde, daha az yalnız hissettirdi."""
                } else if (isSync) {
                    """*"Natasha ile girerim,"* dedin.

Natasha kısa bir bakış attı — sürpriz değil, onay. *"İyi. Doğu kapısında buluşuyoruz, içeride soldan süpürüyoruz."*

Deponun karanlığında yanyana ilerlediğinizde fark ettin: Natasha'nın adımları seninle eşleşiyordu. Bilinçsiz bir senkronizasyon, yıllarca başkalarıyla çalışmış birinin kas hafızası. Sen ise bu tür koordinasyonu unutmuştun — ya da hiç tam öğrenmemiştin.

Ama bu gece, doğal geldi."""
                } else {
                    """*"Ben dışarıdan izlerim,"* dedin. *"Hepinizi aynı anda görebilirim. Bir şey gördüğümde anında söylerim."*

Steve başını salladı: *"Taktiksel genel görüş — iyi düşünce. Ama tehlike anında müdahale edebiliyor musun hızla?"*

*"Işınlanıyorum,"* dedin.

Sam Wilson: *"Haklı nokta."*

Operasyon boyunca tepeden tüm hareketi izledin. Ekibin görmediği açıları sen gördün — iki kez erken uyarı verdin. İkisi de hayat kurtardı."""
                }

                roleText + """

---

Depo içi — sabahın dördü kırk beş.

Yük konteynerlerinin arkasına sinmiş dört kişi. Teslim henüz başlamamış, alıcı gelmemiş. İdeal zamanlama.

Ama o an deponun arka bölümünden bir ses geldi — beklenmemiş, plana dahil olmayan.

Wanda interkomdan: *"Aiden. Depoda başka bir enerji imzası var. Kozmik değil ama... karanlık. Bilmediğimiz bir şey."*

---

🔀 Beklenmedik enerji imzasına nasıl tepki vereceksin?

1️⃣ Ekibi dur — önce imzayı araştır, sonra devam.
2️⃣ Planı değiştirme, operasyonu tamamla, imzayı sonra araştır.
3️⃣ Tek başına imzaya doğru git, ekip planı sürdürsün."""
            }

            23 -> {
                val isStop = choiceIs(lastUserMsg, 1, "dur", "araştır", "bekle", "durdur", "önce bak")
                val isContinue = choiceIs(lastUserMsg, 2, "planı değiştirme", "devam", "sonra", "operasyonu tamamla")
                progress = if (isStop) progress.copy(steveTrust = progress.steveTrust + 1)
                           else if (isContinue) progress.copy(tonyAffinity = progress.tonyAffinity + 1)
                           else progress.copy(mysteryFactor = progress.mysteryFactor + 2)
                storyProgressDao.insertOrUpdate(progress)

                val reactionText = if (isStop) {
                    """*"Dur,"* dedin interkomdan. Ses çıkmadı ama hepsinin adımları yavaşladığını hissetttin.

Steve: *"Blackwood?"*

*"Wanda'nın dediği imza. Bilmediğimiz bir şey içerideyse, kör ilerlemeyelim."* Bir saniye bekledin. *"Üç dakika ver bana."*

İmzanın kaynağına yaklaştığında anladın: deponun alt katında kapalı bir kasa odası, ve içinde — criyojenik bir muhafaza tüpü. Üzerinde HYDRA arması yoktu. Üzerinde başka bir şey vardı: eski Sovyet askeri damgası.

Steve arkanızda seslendi: *"Bu plana dahil değildi."*

*"Biliyorum,"* dedin."""
                } else if (isContinue) {
                    """*"Planı bozmuyoruz,"* dedin. *"Operasyonu tamamlıyoruz, sonra bakıyoruz."*

Natasha: *"Onaylıyorum. İmza sabitse bekleyebilir."*

HYDRA ekibi etkisiz hale getirildi — hızlı, temiz, plana uygun. Sam bir el bağladı bile.

Ama teslim kutuları açılınca içlerinden biri farklıydı. Silah değil, bilgisayar sürücüsü. Şifreli, Sovyet imzalı.

Stark aşağıda inceledi: *"Bu HYDRA'dan eski. Çok eski. Ve sadece bir kişinin biyometrik imzasıyla açılıyor."*

Sana baktı.

*"Seninle eşleşiyor Blackwood."*"""
                } else {
                    """*"Ben imzaya gidiyorum,"* dedin. *"Siz planı sürdürün."*

Sessizlik.

Natasha: *"Aiden—"*

*"İki dakika."*

Deponun arka bölümü karanlıktı, havasız, uzun zamandır kimsenin girmediği türden. Köşede bir kasa odası, kapısı aralık.

İçeride oturan adam — orta yaşlı, saçları ağarmış, elleri titriyor — seni görünce ayağa kalkmadı. Sadece: *"Nihayet,"* dedi. Rusça değildi, Türkçeydi. *"Seni yirmi yıldır bekliyordum."*"""
                }

                reactionText + """

---

Operasyon tamamlandı. Sabah altıda Tower'a döndünüz.

Silahlar güvence altına alındı, HYDRA ekibi teslim edildi. Fury brifing notlarını onayladı.

Ama kasa odasındaki bulgu — ne olursa olsun — hâlâ yanınızdaydı.

Steve toplantı odasında sana baktı: *"İlk operasyon. Nasıldı?"*

---

🔀 Steve'in sorusuna nasıl cevap vereceksin?

1️⃣ Dürüst: "Düşündüğümden farklıydı. Daha gerçekti."
2️⃣ Kısa: "Bitti. Bu yeterli."
3️⃣ Soru ile karşılık ver: "Kasa odasındaki Sovyet dosyasını araştırdınız mı?"
"""
            }

            24 -> {
                val isHonest = choiceIs(lastUserMsg, 1, "Dürüst", "farklıydı", "gerçekti", "düşündüğümden")
                val isShort = choiceIs(lastUserMsg, 2, "Bitti", "kısa", "yeterli", "tamam")
                progress = if (isHonest) progress.copy(steveTrust = progress.steveTrust + 2)
                           else if (isShort) progress
                           else progress.copy(mysteryFactor = progress.mysteryFactor + 1)
                storyProgressDao.insertOrUpdate(progress)

                val steveResp = if (isHonest) {
                    """"Düşündüğümden farklıydı," dedin, sesin kendi kulağına bile beklenmedik çıktı. "Daha gerçekti. Dosyalarda okuduğun bir şeyle yan yana yürümek farklı."

Steve'in yüzünde bir şey yumuşadı — onay değil, anlayış. "Evet. Her zaman öyle olur." Bir saniye durakladı. "Tekrar gelmek ister misin?"

Cevap vermeden önce düşündün. Uzun düşündün.

"Evet," dedin."""
                } else if (isShort) {
                    """"Bitti. Bu yeterli," dedin düz bir tonla.

Steve başını salladı — zorlamadı. Ama çıkarken omzuna hafifçe dokundu. Sözün olmadığı türden bir onay.

Natasha kapıda bekliyordu. "İyi iş," dedi, sadece bu kadar. Onun ağzından bu iki kelime fazlasıydı."""
                } else {
                    """"Sovyet dosyasını araştırdınız mı?" dedin doğrudan.

Steve gözlerini kıstı. "Stark üzerinde çalışıyor. Senin biyometrik imzanla eşleşmesi..."

"Tesadüf değil," dedin.

"Hayır," diye onayladı Steve. "Değil." Masaya oturdu. "Blackwood, bu operasyon seni bulmak için kurulmuş olabilir. HYDRA'nın değil — ama birilerinin."

Oda soğudu."""
                }

                steveResp + """

---

📖 BÖLÜM 4 SONU — İLK GÖREV

Aiden Blackwood ilk operasyonunu tamamladı.
Kaçmak yerine kaldı. Savaştı. Ve geri döndü.
Kasa odasındaki Sovyet dosyası hâlâ yanıt bekliyordu.

Tebrikler! Bölüm 4'ü başarıyla tamamladın."""
            }

            // ═══════════════════════════════════════════════════════════════
            // BÖLÜM 5: KIRMIZI BUZ
            // ═══════════════════════════════════════════════════════════════
            25 -> {
                progress = progress.copy(chapterNumber = 5)
                storyProgressDao.insertOrUpdate(progress)
                val b5Text = loadAssetText("Bolum5_KirmiziBuz.md")
                    ?: "# 📖 BÖLÜM 5: KIRMIZI BUZ\n\nOperasyon sırasında bir şeyler ters gitti."

                b5Text + """

---

🔀 Elias Voss sana "Kaç" dediğinde ne yaparsın?

1️⃣ Dur, ona bak — kaç kelime söyle, tanımaya çalış.
2️⃣ Saldır, diyalog yok — operasyon önce gelir.
3️⃣ Beyaz Göz'ü aç — zihnini oku, kim olduğunu anlamaya çalış."""
            }

            26 -> {
                val isTalk = choiceIs(lastUserMsg, 1, "dur", "bak", "söyle", "tanı", "konuş")
                val isAttack = choiceIs(lastUserMsg, 2, "saldır", "operasyon", "diyalog yok", "vur")
                val isMind = choiceIs(lastUserMsg, 3, "Beyaz Göz", "zihin", "oku", "anla")
                progress = if (isTalk) progress.copy(wandaBond = progress.wandaBond + 1)
                           else if (isAttack) progress.copy(flightInstinct = progress.flightInstinct - 1)
                           else progress.copy(mysteryFactor = progress.mysteryFactor + 2)
                storyProgressDao.insertOrUpdate(progress)

                val vossResp = if (isTalk) {
                    """Durdun.

O da durdu.

*"Kim söyledi sana bunu?"* dedin Türkçe — içgüdüsel, düşünmeden.

Voss'un gözleri kırpıştı. HYDRA'nın onu kim yaptığı kadar, onu kim *bulmak için* yaptığı bu anda yüzüne vurdu.

*"Senin dosyan vardı,"* dedi, sesi kırık ama net. *"Yıllarca taşıdım. Seni bulmak için kullandılar beni. Ama ben... seni uyarmak istedim."*

Natasha interkomdan: *"Blackwood. Durum?"*

*"Kontrol altında,"* dedin. Gözlerini Voss'tan ayırmadan."""
                } else if (isAttack) {
                    """Diyalog yoktu. Voss'un üzerine atladın.

Dövüş, beklendiğinden farklıydı — Voss savaşmıyordu tam olarak. Saldırılarını savuşturuyor ama karşılık vermiyordu. Elini boğazına getirdiğinde gözlerini kapattı.

*"Beni öldürme,"* dedi, sessizce. *"Söylemem gereken bir şey var."*

Elini çekttin.

Steve geride durmuş izliyordu. *"Ne oldu?"*

*"Bilmiyorum,"* dedin. Doğruydu ve yalancı hissettirdi aynı anda."""
                } else {
                    """Beyaz Göz açıldı.

Voss'un zihnine dokunduğunda ilk hissettiğin şey acıydı — kendi değil, onun. Katmanlar halinde, yıllarca birikmiş, bir insanın kimliğinin üstüne yapıştırılmış bir şey.

Ve altında, o katmanların altında —

Senin adın. Senin koordinatların. Yıllarca taşınmış, aranmış, bulunamamış.

Beyaz Göz'ü kapattın. Ellerin titriyordu.

*"Seni bulmak için mi kullandılar?"* diye sordun.

*"Evet,"* dedi Voss. *"Ama ben... bırakmak istedim."*"""
                }

                vossResp + """

---

Operasyon sonrası Voss Tower'a getirildi. Sorgu odasına alındı.

Steve sana baktı: *"Onunla konuşmak ister misin? İstemiyor musun söyle, başkası girer."*

---

🔀 Elias Voss'u sorgulamak ister misin?

1️⃣ Evet, ben gireceğim — onunla konuşmam gerekiyor.
2️⃣ Hayır, Natasha girsin — duygusal mesafeyi koru.
3️⃣ Wanda girsin — zihinsel tarama daha hızlı ve kesin olur."""
            }

            27 -> {
                val isEnter = choiceIs(lastUserMsg, 1, "ben", "gireceğim", "konuşmam", "evet ben")
                val isNatasha = choiceIs(lastUserMsg, 2, "Natasha", "mesafe", "o girsin", "Natasha girsin")
                val isWanda = choiceIs(lastUserMsg, 3, "Wanda", "zihinsel", "tarama", "Wanda girsin")
                progress = if (isEnter) progress.copy(steveTrust = progress.steveTrust + 1)
                           else if (isNatasha) progress.copy(natashaBond = (progress.natashaBond ?: 0) + 1)
                           else progress.copy(wandaBond = progress.wandaBond + 1)
                storyProgressDao.insertOrUpdate(progress)

                val sorgulama = if (isEnter) {
                    """Sorgu odasına girdiğinde Voss masanın karşısında oturuyordu, bilekleri bağlı ama gözleri açık.

*"Bekledim,"* dedi.

*"Kim seni programladı?"* dedin oturarak.

*"Programlama doğru kelime değil."* Gözleri yere indi. *"Şartlandırma. On altı yılda, adım adım. Ve senin imzan — Beyaz Göz'ün frekansı — benim için bir pusula gibiydi. Nereye gidersen oraya çekildim."*

*"İstemeden mi?"*

Uzun bir sessizlik. *"Başta istemeden. Sonra..."* Duraksadı. *"Sonra seni bulunca uyarmak istedim. İkimiz de aynı şeyiz Blackwood. Sadece farklı ellerde."*"""
                } else if (isNatasha) {
                    """Natasha sorguya girdi. Sen dışarıdan cam arkasında izledin.

Natasha, Natasha olduğu için, kırk dakikada Voss'un söylemediği şeylerin çerçevesini çizdi. Söylediklerini değil, söylemediklerini dinledi.

Çıkarken sana döndü: *"Seni arıyordu. Ama kendi iradesiyle değil — en azından başlangıçta. Bir noktada karar değiştirdi. Neden değiştirdiğini bilmiyorum."* Durakladı. *"Belki sen biliyorsundur."*"""
                } else {
                    """Wanda sorguya girdi. Kısa sürdü.

Dışarı çıktığında yüzü solgundu.

*"Ne gördün?"* diye sordun.

*"Yıllar,"* dedi Wanda, sesi alçak. *"Acı. Ve en altta — senin adın. Bir fener gibi taşımış. Seni bulmak için değil,"* diye ekledi, gözlerini sana çevirerek. *"Seni uyarmak için."*"""
                }

                sorgulama + """

---

O gece Tower sessizdi.

Voss hücresinde tutuluyordu ama tehdit değil, tanık olarak. Bu ayrım önemliydi.

Sen ise otel odasının — artık odanın, senin odanın — penceresinde oturmuştun. Elias Voss'un sözleri hâlâ kafanda dönüyordu.

*"İkimiz de aynı şeyiz. Sadece farklı ellerde."*

---

🔀 Voss'un bu sözü hakkında ne düşünüyorsun?

1️⃣ Katılıyorsun — ama bu seni korkutuyor, çünkü onun geleceği senin geleceğin olabilirdi.
2️⃣ Katılmıyorsun — sen seçimlerini kendin yaptın, o yapmadı. Fark bu.
3️⃣ Ne katılıyorsun ne de reddediyorsun — sadece dinliyorsun, henüz karar yok."""
            }

            28 -> {
                val isAgree = choiceIs(lastUserMsg, 1, "katılıyorum", "korkutuyor", "olabilirdi", "aynı")
                val isDisagree = choiceIs(lastUserMsg, 2, "katılmıyorum", "seçimlerimi", "fark bu", "farklı")
                progress = if (isAgree) progress.copy(mysteryFactor = progress.mysteryFactor + 1)
                           else if (isDisagree) progress.copy(steveTrust = progress.steveTrust + 1)
                           else progress
                storyProgressDao.insertOrUpdate(progress)

                val thinkText = if (isAgree) {
                    """İçinde dürüst bir kabul vardı: *evet*.

Farklı bir dönemde, farklı bir elde yakalanmış olsaydın — sen de Voss olabilirdin. Bu düşünce soğuk geldi, ama yalan değildi.

Wanda'nın söylediklerini hatırladın: *"Beni bulmak için değil, uyarmak için."*

Voss, o zırhın içinde dahi bir şeyleri korumuştu. Belki sen de böyle bir şeyi koruyordun — sadece adını bilmiyordun henüz."""
                } else if (isDisagree) {
                    """Hayır.

Benzer başlangıçlar benzer sonuçlar yaratmak zorunda değildi. Sen de yıllarca kovalanmıştın. Sen de sıkıştırılmıştın. Ama kaçmıştın — kendi kararlarınla, kendi hızınla.

Voss'un seçimleri onundu. Acısı gerçekti. Ama onun yolu senin yolun değildi.

Bu fark küçük görünürdü. Ama her şeydi."""
                } else {
                    """Karar vermeden dinlemek de bir seçimdi.

Belki de en dürüst olanı.

Voss'un sözleri gerçeğin bir parçasına dokunmuştu. Ama gerçeğin tamamı değildi. Ve tamamını bilmeden hüküm vermek — yirmi yıl boyunca seni şekillendiren aceleci yargıların aynısıydı.

Bu sefer bekleyecektin."""
                }

                thinkText + """

---

📖 BÖLÜM 5 SONU — KIRMIZI BUZ

Aiden Blackwood Elias Voss ile yüzleşti.
Geçmişin bir yansımasını gördü — ve kendi seçimlerinin ağırlığını hissetti.
Sovyet dosyası hâlâ çözülmeyi bekliyordu.

Tebrikler! Bölüm 5'i başarıyla tamamladın."""
            }

            // ═══════════════════════════════════════════════════════════════
            // BÖLÜM 6: KIRIK AYNA
            // ═══════════════════════════════════════════════════════════════
            29 -> {
                progress = progress.copy(chapterNumber = 6)
                storyProgressDao.insertOrUpdate(progress)
                val b6Text = loadAssetText("Bolum6_KirikAyna.md")
                    ?: "# 📖 BÖLÜM 6: KIRIK AYNA\n\nKırmızı Koruyucu'nun gerçek adı Elias Voss'tu."

                b6Text + """

---

🔀 Wanda "Seni bulmak için" dediğinde nasıl tepki veriyorsun?

1️⃣ Sakin kal, sadece sor: "Kim? Kim arıyor beni?"
2️⃣ Dışarı çık, yalnız kal — bu bilgiyi sindirmek için zamana ihtiyacın var.
3️⃣ Ekibi topla — bunu herkesin duyması gerekiyor."""
            }

            30 -> {
                val isAsk = choiceIs(lastUserMsg, 1, "sor", "kim", "sakin", "soru")
                val isLeave = choiceIs(lastUserMsg, 2, "dışarı", "yalnız", "zaman", "sindir")
                val isGather = choiceIs(lastUserMsg, 3, "ekibi topla", "herkes", "duymalı", "toplantı")
                progress = if (isAsk) progress.copy(steveTrust = progress.steveTrust + 1)
                           else if (isLeave) progress.copy(mysteryFactor = progress.mysteryFactor + 1)
                           else progress.copy(trustAvengers = "strong_positive")
                storyProgressDao.insertOrUpdate(progress)

                val wandaResp = if (isAsk) {
                    """*"Kim?"* dedin, sesin sakin çıktı ama bu sakinliğin altında ne olduğunu Wanda hissediyordu.

*"Bilmiyorum tam olarak,"* dedi Wanda. *"Voss'un zihnindeki imaj bulanıktı — bir örgüt, sembol değil bir yapı. Eski, köklü. HYDRA'dan önce. HYDRA'dan fazla."*

*"Hydra öncesi mi?"*

*"Hydra bir araçtı onlar için. Aiden—"* Bir adım attı. *"Bu örgüt seni yirmi yıl önce bırakmadı. Sadece bekledi."*

Oda döndü gibi hissettirdi bir saniye. Sonra sabitlendi. İçinde bir şey — o yirmi yıllık kaçış içgüdüsü — anında tekrar uyanmak istedi."""
                } else if (isLeave) {
                    """*"Bir dakika,"* dedin, ve çıktın.

Koridorda, tek başına, soğuk duvara yaslandın. Nefes aldın — derin, kasıtlı, kendini toplamak için öğrenilmiş bir nefes.

Yirmi yıldır bilinenden kaçtın. Ama bu — bilinmeyenden kaçmak — farklıydı.

Beş dakika sonra içeri döndün. *"Anlat,"* dedin. *"Tamamını."*

Wanda bekliyordu. Sanki biliyordu geri döneceğini."""
                } else {
                    """*"Herkesi topla,"* dedin. *"Steve, Natasha, Fury. Şimdi."*

Wanda bir an duraksadı, sonra başını salladı.

Toplantı odasında herkes olduğunda Wanda anlattı. Hepsi dinledi. Sessizlik, konuşmadan daha ağırdı.

Steve masaya eğildi: *"Bu senin sorunun değil artık. Bizim sorunumuz."*

İlk kez bu cümleyi duyduğunda içinde ne hissetttiğini tam tanımlayamadın. Ama hafif geldi. İyi anlamda hafif."""
                }

                wandaResp + """

---

Fury brifingde söyledi: Sovyet dosyası çözülmüştü.

İçinde tek bir kelime vardı. Bir isim. Türkçe değil, Rusça değil — eski bir dil, belki Latince kökten. Ve yanında bir koordinat: İstanbul, 1987.

Senin doğum yılın. Senin şehrin.

Steve sana döndü: *"İstanbul'a gitmek ister misin? Gönüllü, zorunlu değil."*

---

🔀 İstanbul operasyonuna nasıl yaklaşacaksın?

1️⃣ Evet, ben liderlik ederim — bu benim hikayem.
2️⃣ Evet, ama sadece destek olarak — duygu bulanıklığı yaratabilir.
3️⃣ Hayır, ben gidemem — çok kişisel, objektif kalamam."""
            }

            31 -> {
                val isLead = choiceIs(lastUserMsg, 1, "liderlik", "ben", "hikayem", "evet lider")
                val isSupport = choiceIs(lastUserMsg, 2, "destek", "sadece destek", "arka planda", "yardım")
                val isNo = choiceIs(lastUserMsg, 3, "hayır", "gidemem", "kişisel", "objektif")
                progress = if (isLead) progress.copy(steveTrust = progress.steveTrust + 2, mysteryFactor = progress.mysteryFactor + 1)
                           else if (isSupport) progress.copy(natashaBond = (progress.natashaBond ?: 0) + 2)
                           else progress.copy(wandaBond = progress.wandaBond + 2)
                storyProgressDao.insertOrUpdate(progress)

                val istanbulText = if (isLead) {
                    """*"Ben liderlik ederim,"* dedin. Sesin o kadar kararlı çıktı ki Steve bile bir an duraksadı.

*"Bu benim hikayem. Başından beri. Bunu başkasına bırakmak—"* Durdun. *"Olmaz."*

Natasha başını eğdi — saygıyla. *"Tamam. Ama operasyonun duygusal boyutu seni etkiler. Bunu bilerek git."*

*"Biliyorum,"* dedin. *"Bilerek gidiyorum."*"""
                } else if (isSupport) {
                    """*"Giderim,"* dedin, *"ama liderlik etmem. Çok kişisel. Karar anlarında bulanıklaşabilirim."*

Bu, kolay söylenen bir şey değildi. Sınırını bilmek ve onu söylemek — yirmi yıl önce öğrenmediğin bir şeydi.

Steve başını salladı: *"Akıllıca. Natasha liderlik eder, sen destek."*

Natasha sana baktı. Gözlerinde bir şey vardı — onay mıydı, saygı mıydı, bilmiyordun. Ama sıcaktı."""
                } else {
                    """*"Gidemem,"* dedin. *"Bu benim hikayem ama... tam da bu yüzden. Objektif kalamam. Birini tehlikeye atabilirim."*

Sessizlik.

Steve: *"Emin misin?"*

*"Evet."*

Wanda yanına geldi, alçak sesle: *"Gitmemek de bir seçim. Ve bazen en zor olanı bu."*

Ekip İstanbul'a gitti. Sen Tower'da kaldın. Voss'un hücresinin önünde beklediğini fark ettiğinde, içindeki cevabı aslında zaten bulmuştun."""
                }

                istanbulText + """

---

İstanbul'daki koordinat: tarihi bir konak, bodrumunda on yıllarca dokunulmamış bir arşiv.

Arşivde bulduklarını Stark şifreli bağlantıdan iletti.

Senin hakkında yazılmış raporlar. Beş yaşında başlayan. Ailenin adları. Ve en altta — bir karar notu, 1992 tarihli.

*"Denek 7-A'nın doğal gelişimi izlenmeye devam edecek. Müdahale yok. Zaman kıymetli bir araştırmacıdır."*

Sen bir denek değildin. Sen bir *proje*ydin.

---

🔀 Bu gerçeği öğrendikten sonra ilk tepkin ne?

1️⃣ Öfke — soğuk, hesaplı, yönlendirilmiş bir öfke.
2️⃣ Uyuşma — bu kadar büyük bir şeyi işlemek zaman alır.
3️⃣ Netlik — sonunda bir cevap. Korku değil, rahatlama gibi bir şey."""
            }

            32 -> {
                val isAnger = choiceIs(lastUserMsg, 1, "öfke", "öfkeli", "hesaplı", "soğuk öfke")
                val isNumb = choiceIs(lastUserMsg, 2, "uyuşma", "işlemek", "zaman", "dondum")
                val isClarity = choiceIs(lastUserMsg, 3, "netlik", "cevap", "rahatlama", "sonunda")
                progress = if (isAnger) progress.copy(mysteryFactor = progress.mysteryFactor + 2)
                           else if (isNumb) progress.copy(flightInstinct = progress.flightInstinct + 1)
                           else progress.copy(steveTrust = progress.steveTrust + 2, trustAvengers = "strong_positive")
                storyProgressDao.insertOrUpdate(progress)

                val finalText = if (isAnger) {
                    """Öfke geldi. Ama alışık olduğun türden değil — patlayıcı, dağıtıcı değil.

Soğuk. Hesaplı. Bir hedefi olan öfke.

Yirmi yıl boyunca neden kaçtığını biliyordun artık. Ve bu bilmek, öfkeyi bir silaha dönüştürdü.

Steve kapıya geldi, seni izledi. Bir şey söylemedi. Ama yanında durdu."""
                } else if (isNumb) {
                    """Hiçbir şey hissetmedin.

Bu, panikten daha korkutucuydu.

Natasha geç saatte yanına geldi, sessizce. Kahve koydu masana, içip içmeyeceğini sormadan.

*"Zaman alır,"* dedi. *"Büyük şeyler böyle işlenir. Hemen değil — katman katman."*

Kahveyi tutunca ellerin titrediğini fark ettin."""
                } else {
                    """Paradoks gibiydi ama doğruydu: rahatlama.

Yirmi yıl boyunca neyin içinde olduğunu bilmeden kaçmıştın. Şimdi biliyordun. Ve bilmek — ne kadar ağır olursa olsun — bilinmezlikten hafifti.

Wanda odasına geldi: *"İyi misin?"*

*"Hayır,"* dedin. *"Ama ilk kez ne olduğunu biliyorum. Bu fark ediyor."*"""
                }

                finalText + """

---

O gece ekip toplandı — bu sefer Fury çağırmadan, kendi isteğiyle.

Steve: *"Bundan sonra ne istiyorsun Blackwood?"*

İlk kez bu soruyu soran kişi sen değildin. Ve ilk kez cevabını biliyordun.

---

🔀 "Bundan sonra ne istiyorsun?" sorusuna cevabın ne?

1️⃣ "Onları bulmak istiyorum. Ve bitirmek."
2️⃣ "Bilmiyorum henüz. Ama burada kalmak istiyorum."
3️⃣ "Voss'u kurtarmak istiyorum. Sonra bakarız."""
            }

            33 -> {
                val isHunt = choiceIs(lastUserMsg, 1, "bulmak", "bitirmek", "onları", "avlamak")
                val isStay = choiceIs(lastUserMsg, 2, "kalmak", "burada", "bilmiyorum", "henüz")
                val isVoss = choiceIs(lastUserMsg, 3, "Voss", "kurtarmak", "önce Voss")
                progress = if (isHunt) progress.copy(mysteryFactor = progress.mysteryFactor + 2, trustAvengers = "strong_positive")
                           else if (isStay) progress.copy(steveTrust = progress.steveTrust + 3, trustAvengers = "strong_positive")
                           else progress.copy(wandaBond = progress.wandaBond + 2, natashaBond = (progress.natashaBond ?: 0) + 1)
                storyProgressDao.insertOrUpdate(progress)

                val endText = if (isHunt) {
                    """*"Onları bulmak istiyorum,"* dedin. *"Ve bitirmek."*

Sessizlik. Ama farklı bir sessizlik — öncekiler belirsizlikti. Bu, karar.

Steve masaya eğildi. *"Tamam. Bunu birlikte yaparız."*

Sam Wilson: *"İşte bu. Hoş geldin Blackwood — gerçekten bu sefer."*

Natasha sana baktı, uzun. Sonra başını salladı.

Wanda gülümsedi — kırılgan ama gerçek."""
                } else if (isStay) {
                    """*"Bilmiyorum henüz,"* dedin dürüstçe. *"Ama burada kalmak istiyorum. Bunu biliyorum."*

Bu, büyük bir cümleydi. Yirmi yıl için büyük.

Steve ayağa kalktı, elini uzattı. Tokalaşma değil — bir tür yenileme.

*"Yeter bu."*

Elini uzattın. Ve ilk kez yirmi yıldır, elini tutan birinin elini geri bırakmak istemedın."""
                } else {
                    """*"Voss'u kurtarmak istiyorum,"* dedin. *"O da bizim gibi biri. Sadece daha az şanslı."*

Wanda'nın gözleri doldu — bunu belli etmedi ama sen fark ettin.

Steve: *"Hukuki süreç başlatılabilir. HYDRA kurbanı statüsü — kolay değil ama imkânsız da değil."*

*"Başlat,"* dedin.

Ve ilk kez, birisini kurtarmak için bir şey istiyordun. Kaçmak için değil. Bu fark, her şeydi."""
                }

                endText + """

---

📖 BÖLÜM 6 SONU — KIRIK AYNA

Aiden Blackwood gerçeği öğrendi: yirmi yıl boyunca bir proje olduğunu.
Ve bu gerçeğin ağırlığı altında ezilmek yerine, bir karar verdi.
Avengers artık görev değil — ev gibiydi.

Tebrikler! Bölüm 6'yı başarıyla tamamladın."""
            }

            34 -> {
                // Bölüm 7 Başlangıç — Toplantı Odası, Yabancı Adam
                progress = progress.copy(chapterNumber = 7)
                storyProgressDao.insertOrUpdate(progress)
                val b7Text = loadAssetText("Bolum7_KaranlıkAyna.md") ?: ""
                b7Text + """

---

Wanda çok yavaşça konuştu, ses tonu düz ama içinde bir şey titriyordu: *"O adamın zihnine dokunduktan sonra söylemem gerekiyor."*

Sessizlik.

*"Zihninde senin beş yaşındaki fotoğrafın vardı. Ve altında bir not: 'Denek 7-A aktif. Birinci Aşama tamamlandı.'"*

Oda sessizdi. Adam hâlâ ayaktaydı, dosyayı tutuyordu, sabırlıydı — sanki bu anın geleceğini yıllardır biliyormuş gibi.

Fury konuştu: *"Adı Dr. Marcus Hale. Eski Kör Nokta teorisyeni — örgütün bilişsel silahlar kolunun kurucusu. İki ay önce bize teslim olmak istediğini söyledi. Karşılığında tek şart istedi: Seninle yüz yüze konuşmak."*

Steve sana baktı. Karar senindi.

---

🔀 Dr. Hale'e ne yapacaksın?

1️⃣ Doğrudan sor: "Benden ne istiyorsun?"
2️⃣ Odadan çık — bunu hazmetmek için zaman lazım.
3️⃣ Wanda'ya bak: önce zihnini tam oku, sonra konuş."""
            }
            35 -> {
                // Seçim 1: Hale'e tepki
                val isAsk    = choiceIs(lastUserMsg, 1, "sor", "ne istiyorsun", "direkt", "konuş")
                val isLeave  = choiceIs(lastUserMsg, 2, "çık", "dışarı", "zaman", "hazmet", "bekle")
                val isWanda  = choiceIs(lastUserMsg, 3, "Wanda", "zihin", "oku", "tara")

                progress = when {
                    isAsk   -> progress.copy(steveTrust = progress.steveTrust + 1)
                    isLeave -> progress.copy(flightInstinct = progress.flightInstinct + 1)
                    else    -> progress.copy(wandaBond = progress.wandaBond + 1)
                }
                storyProgressDao.insertOrUpdate(progress)

                val responseIntro = when {
                    isAsk -> """Masanın üzerinden adama baktın. Sesin sakin, keskin: *"Benden ne istiyorsun?"*

Hale dosyayı masaya koydu, itiydi — bir silah değil, teslim edilen bir şey gibi. *"Seni durdurmak için değil,"* dedi. *"Seni tamamlamak için geldim."*

*"'Tamamlamak',"* tekrarladın. Kelime ağzında yanlış hissettirdi.

*"Denek 7-A programı bitmedi Blackwood. Sen bitirdin — kaçarak. Ama protokol hâlâ aktif. Biri seni bulup tamamlamak isteyecek. Ben önce geldim çünkü ben... pişmanım."*"""
                    isLeave -> """Ayağa kalktın ve kapıya yürüdün. Kimse durdurmadı.

Koridorda tek başına duruyordun, fotoğrafın görüntüsü zihninde yanıyor: beş yaşında, bir deney odasında, ne olduğunu bilmeden. O çocuk sendin. Ve birisi onu yıllarca dosyasında taşımıştı.

Üç dakika sonra geri döndün. Kapıyı açtın, adam hâlâ aynı yerde duruyordu — bekliyordu.

*"Tamam,"* dedin. *"Konuş."*"""
                    else -> """Gözlerinle Wanda'ya işaret ettin. O da anladı.

Hale'in yanına yaklaştı. Adam direnmedi — zaten direnmeyeceğini biliyordu ya da umursamıyordu. Wanda parmaklarını şakağına götürdü, gözleri kızıla döndü.

Otuz saniye. Yavaş bir nefes. Sonra Wanda geri çekildi, yüzü solgundu.

*"Gerçekten pişman,"* dedi sessizce. *"Ve korkuyor — senden değil. Başkalarından. Seni bulmaya çalışacak olan birilerinden."*"""
                }

                responseIntro + """

---

Hale masaya oturdu. Dosyayı açtı.

*"Kör Nokta programı, sizi — Beyaz Göz taşıyıcıları — bulup aktive etmek için kuruldu. Yedi yaşında tespit edildiniz. Aileni ikna ettiler — resmi bir 'burs' programıydı görünürde."*

*"Ailemi,"* dedin.

*"Annenin imzası var burada. Babanın da. İkisi de sizi kurtardıklarını sanıyorlardı. Program onlara çok ikna edici görünmüş olmalı."*

Ellerin masanın altında bir yumruk yaptı. Natasha bunu gördü — bir şey söylemedi, sadece baktı.

*"Ve 'tamamlamak' ne demek?"* diye sordun.

*"İkinci Aşama,"* dedi Hale. *"Beyaz Göz'ün tam potansiyeli kilitli. Sizi aktive etmek — tam aktive etmek — Kör Nokta'ya yeterince güçlü bir teorik silah verecekti. Ben o teorisi yazdım. Otuz yıl önce. O zaman... bunun ne olduğunu tam anlamıyla bilmiyordum."*

---

🔀 Hale'in "pişmanım" sözüne nasıl tepki veriyorsun?

1️⃣ Soğuk ve sert: "Pişmanlık yetmez. Ne istediklerini anlat."
2️⃣ Beklenmedik bir şey sor: "Annem ve babam hâlâ hayatta mı?"
3️⃣ Ayağa kalk ve çık — bu kadar bilgi yeterli, gerisini istemiyorsun."""
            }
            36 -> {
                // Seçim 2: Hale'in pişmanlığına tepki
                val isCold    = choiceIs(lastUserMsg, 1, "soğuk", "sert", "yetmez", "ne istediklerini", "anlat")
                val isParents = choiceIs(lastUserMsg, 2, "annem", "babam", "ebeveyn", "aile", "hayatta mı")
                val isLeave   = choiceIs(lastUserMsg, 3, "çık", "yeterli", "istemiyorum", "dur", "kapat")

                progress = when {
                    isCold    -> progress.copy(mysteryFactor = progress.mysteryFactor + 1, steveTrust = progress.steveTrust + 1)
                    isParents -> progress.copy(natashaBond = progress.natashaBond + 1, steveBond = progress.steveBond + 1)
                    else      -> progress.copy(flightInstinct = progress.flightInstinct + 2)
                }
                storyProgressDao.insertOrUpdate(progress)

                val response = when {
                    isCold -> """Sesin hiç titremedi. *"Pişmanlık yetmez. Ne istediklerini anlat — tam olarak."*

Hale'in çenesi gerildi. *"İkinci Aşama, Beyaz Göz'ün 'Kırmızı Geçiş' olarak adlandırdıklarını tetiklemeyi hedefliyordu. Aktive edilmiş bir taşıyıcı, sınırlı alanda gerçekliği yeniden yazabilir — hafıza, algı, nedensellik. Kendi isteğiyle değil. Tetikleyici bir frekanssla."*

*"Bir uzaktan kumanda,"* dedin.

*"Evet. Tam olarak."*

Natasha masaya eğildi: *"Bu frekans şu an kimin elinde?"*"""
                    isParents -> """Sesin beklenmedik çıktı — sert değil, sadece ham: *"Annem ve babam hâlâ hayatta mı?"*

Hale duraksadı. İlk kez duraksadı — ilk kez gerçekten insan gibi bir şey yaptı.

*"Hayatta,"* dedi. *"İzole tutuluyorlar. Gönüllü değil. Kör Nokta, sizi bulmak için bir koz olarak..."*

Odada kimse konuşmadı. Steve'in yüzü gerildi. Sam masaya yumruğunu yavaşça koydu.

Natasha ayağa kalktı: *"Nerede?"*"""
                    else -> """Ayağa kalktın, sandalyeyi geri ittirdin. *"Bu kadar yeterli."*

Hale sesi yükseltmedi. *"Blackwood. Kör Nokta senin konumunu biliyor. Buraya gelmelerini en fazla altı günde bekliyorum."*

Duraksadın ama dönmedin.

Steve arkandan konuştu: *"Hazır olduğunda dinleriz. Baskı yok."*

Kalan sohbeti Fury Steve ve Natasha yaptı. Sen odanın köşesinde, pencereye bakarak kaldın. Wanda yanına geldi, bir şey söylemedi — sadece oturdu ve sessizliği paylaştı."""
                }

                response + """

---

Toplantı iki saat daha sürdü. Hale her şeyi anlattı: Kör Nokta'nın ağ yapısını, İkinci Aşama'nın teknik detaylarını, tetikleyici frekansın nerede saklandığını, kimlerin bu bilgiye sahip olduğunu.

Ve en son, masanın üzerine küçük bir metal kutu koydu.

*"Bu, aktivasyon protokolünü iptal edebilecek tek şey,"* dedi. *"Tersine mühendislik — kendi çalışmam. Yirmi yıl sürdü. Sen hayatta kaldığın için... zaman vardı."*

Kutuyu açtı. İçinde küçük, kristal bir cihaz vardı — mavi parıltılı, tanıdık bir frekansta titreşen.

Fury: *"Bu cihazın nasıl kullanılacağı bir sorun. Kör Nokta'nın merkezi tesisine girmen gerekiyor. Ve o tesis..."*

Steve devam etti: *"İstanbul'un altında. Yirmi metre derinlikte. Tam güvenlik."*

---

🔀 Cihaz hakkında ne düşünüyorsun?

1️⃣ "Ben girerim. Tek başıma."
2️⃣ "Ekiple gideriz — plan yapalım."
3️⃣ "Önce annem ve babam. Önce onları kurtarın." (sadece isParents seçtiysen bu seçenek anlamlı)"""
            }
            37 -> {
                // Seçim 3: Cihaz & operasyon kararı
                val isSolo = choiceIs(lastUserMsg, 1, "tek başıma", "yalnız", "ben girerim", "solo")
                val isTeam = choiceIs(lastUserMsg, 2, "ekip", "birlikte", "plan", "ekiple")
                val isFirst = choiceIs(lastUserMsg, 3, "önce", "aile", "annem", "babam", "kurtarın")

                progress = when {
                    isSolo  -> progress.copy(mysteryFactor = progress.mysteryFactor + 2, flightInstinct = progress.flightInstinct + 1)
                    isTeam  -> progress.copy(steveTrust = progress.steveTrust + 2, trustAvengers = "strong_positive")
                    else    -> progress.copy(natashaBond = progress.natashaBond + 2, wandaBond = progress.wandaBond + 1, commitmentLevel = "family_first")
                }
                storyProgressDao.insertOrUpdate(progress)

                val response = when {
                    isSolo -> """*"Ben girerim,"* dedin. *"Tek başıma."*

Odada bir nefes tutuldu — birden fazla nefes, aynı anda.

Steve: *"Bu intihar."*

*"Hayır,"* dedin. *"Kör Nokta, Beyaz Göz'ü aktif görmek istiyor. Beni görmek istiyor. Beni bir gösteri olarak kullanmak istiyorlar. Bunu kullanabilirim. Bir ekip varsa, operasyon oluyor. Ben yalnızsam, tuzak kuruyor gibi görünürüm."*

Natasha düşünceli baktı. *"Bu... aslında mantıklı."*

*"Teşekkür ederim,"* dedin kuru bir tonla.

*"Övgü değildi."*"""
                    isTeam -> """*"Ekiple gideriz,"* dedin. *"Plan yapalım."*

Steve'in omuzlarından bir şey düştü — görünmez bir ağırlık, görünmez bir endişe.

*"Tamam,"* dedi. *"Böyle yapıyoruz."*

Masa etrafında toparlandınız. Fury haritaları açtı. Natasha çıkış rotalarını işaretledi. Sam hava desteğini hesapladı. Wanda zihinsel tarama perimetrelerini çizdi.

Ve sen ilk kez — gerçek anlamda ilk kez — bir masanın etrafında, seni bekleyen insanlarla, onlardan biri gibi oturdun."""
                    else -> """*"Önce annem ve babam,"* dedin. *"Önce onları kurtarın."*

Natasha hemen: *"Konum?"*

Hale dosyadan bir sayfa çıkardı. *"Prag. Bir S.H.I.E.L.D. kapı kuruluşunun arkasında, Kör Nokta bağlantısı olan özel bir tesis."*

Fury: *"İki paralel operasyon. Romanoff — Prag. Rogers — İstanbul hazırlık."*

Steve sana baktı. *"Haklısın. Seni bu programa sokanlar önce hesap verecek."*

İlk kez, onların seni duymak için gerçekten dinlediğini hissettin."""
                }

                response + """

---

Toplantı dağıldı. Fury Hale'i güvenli bir odaya götürdü. Plan yapılmaya başlandı.

Sen asansörde Wanda ile yalnız kaldın. O konuşmak için bekledi — biliyordun bunu, Wanda her zaman beklerdi.

*"Zihninde ne gördüm biliyor musun?"* dedi sonunda.

*"Hale'in zihninde mi?"*

*"Hayır. Seninkinde. Toplantı boyunca."*

Duraksadın.

*"Korku değildi,"* dedi Wanda. *"İnsanlar genelde korku dolu olur böyle anlarda. Sen... meraktaydın. Hâlâ meraktaydın."*

Asansör kapısı açıldı.

---

🔀 Wanda'ya ne cevap veriyorsun?

1️⃣ "Merakı bir lüks sayıyordum. Belki yanılıyormuşum."
2️⃣ Hiçbir şey söyleme, sadece çık asansörden.
3️⃣ "Sen ne hissediyordun? Benim zihnime girerken.""""
            }
            38 -> {
                // Seçim 4: Wanda ile asansör
                val isShare  = choiceIs(lastUserMsg, 1, "merak", "lüks", "yanılıyormuşum", "söyle")
                val isSilent = choiceIs(lastUserMsg, 2, "hiçbir şey", "çık", "sessiz", "geç")
                val isAskW   = choiceIs(lastUserMsg, 3, "sen", "hissediyordun", "girerken", "sordun")

                progress = when {
                    isShare -> progress.copy(wandaBond = progress.wandaBond + 2)
                    isSilent -> progress.copy(mysteryFactor = progress.mysteryFactor + 1)
                    else    -> progress.copy(wandaBond = progress.wandaBond + 3, natashaBond = progress.natashaBond + 1)
                }
                storyProgressDao.insertOrUpdate(progress)

                val wandaResponse = when {
                    isShare -> """*"Merakı bir lüks sayıyordum,"* dedin. *"Yirmi yıldır. Merak etmek, bir yerde kalmayı gerektiriyor. Ben hep gidecektim."*

Wanda sana baktı — o bakış, her şeyi görmeye alışmış ama yine de şaşırabilen türden.

*"Ve şimdi?"* diye sordu.

Koridorda bir an durdun. *"Şimdi... belki gitmemeye çalışıyorum."*

Wanda gülümsedi. Küçük, gerçek, kırılgan. *"Bu yeterli,"* dedi."""
                    isSilent -> """Asansörden çıktın, bir şey söylemeden.

Wanda da bir şey eklemedi. Bu, onun en güzel yanlarından biriydi — konuşmaya zorlamazdı.

Koridorun sonunda Natasha duruyordu, seni bekliyordu. Hiçbir şey sormadan yanına yaklaştı ve omuz omuza yürüdünüz.

Bazı şeyler kelime gerektirmiyordu."""
                    else -> """*"Sen ne hissediyordun?"* diye sordun. *"Benim zihnime girerken."*

Wanda durdu. Beklemediğin bir soruydu bu — kendisi için bile.

*"Genişlik,"* dedi sonunda. *"İnsanların zihinleri genellikle dardır. Senin zihnin... çok yer kaplıyor. Sanki içinde onlarca yıl birikmiş bir şey var, hâlâ açılmamış."*

*"Açılmamış mı?"*

*"Açılmayı bekleyen."* Durdu. *"Seni korkuttu mu bu?"*

*"Hayır,"* dedin. Doğruydu. *"Merak ettirdi."*"""
                }

                wandaResponse + """

---

Geceyi Tower'da geçirdin — odanda değil, çatıda. Alışkanlıktı bu: kapalı mekânlar, sınırlı çıkışlar. Eski refleksler çok kolay geçmiyordu.

Natasha saat üçte geldi. Yanına oturdu, bacaklarını uzattı, New York ışıklarına baktı. Bir şey sormadı.

*"Uyuyamadın mı?"* dedin sonunda.

*"Uyuyamıyorum hiç operasyon öncesinde,"* dedi. *"Alışkın değilim buna dememeliyim — alıştım aslında. Sadece faydalı bulmuyorum."*

Bir süre sessiz kaldınız.

*"Ailenle ilgili kısımda,"* dedi Natasha, *"yüzünde bir şey gördüm."*

*"Ne?"*

*"İlk kez kaybolmayı düşünmediğini."*

---

🔀 Natasha'ya ne diyorsun?

1️⃣ "Haklısın. Artık düşünmüyorum."
2️⃣ "Düşünüyorum. Ama bu sefer gitmek istemiyorum."
3️⃣ Hiçbir şey söyleme — sadece otur ve New York'a bak."""
            }
            39 -> {
                // Seçim 5: Çatı — Natasha ile gece
                val isConfirm = choiceIs(lastUserMsg, 1, "haklısın", "artık düşünmüyorum", "kalmak", "gitmiyorum")
                val isHonest  = choiceIs(lastUserMsg, 2, "düşünüyorum", "gitmek istemiyorum", "ama", "dürüst")
                val isSilence = choiceIs(lastUserMsg, 3, "hiçbir şey", "sessiz", "otur", "bak")

                progress = when {
                    isConfirm -> progress.copy(natashaBond = progress.natashaBond + 2, steveTrust = progress.steveTrust + 1)
                    isHonest  -> progress.copy(natashaBond = progress.natashaBond + 3, trustAvengers = "strong_positive")
                    else      -> progress.copy(natashaBond = progress.natashaBond + 1, mysteryFactor = progress.mysteryFactor + 1)
                }
                storyProgressDao.insertOrUpdate(progress)

                val natResponse = when {
                    isConfirm -> """*"Haklısın,"* dedin. *"Artık düşünmüyorum."*

Natasha bir şey söylemedi. Ama omzu hafifçe senin omunla temas etti — bilinçli, kasıtlı.

*"İyi,"* dedi sonunda, tek kelime, ama içinde bir sürü anlam taşıyan türden.

Saat dörde kadar konuşmadınız. Ama gitmediniz de."""
                    isHonest -> """*"Düşünüyorum,"* dedin. *"Refleks. Yirmi yıl. Ama bu sefer... gitmek istemiyorum."*

Natasha sana döndü. Tam döndü, ilk kez doğrudan baktı o gece.

*"Bu fark,"* dedi, *"her şeydir."*

Sesi garip bir şekilde sesinin genel tonundan farklıydı — daha yumuşak, daha az hesaplı. Sadece o an için açılmış bir şeydi.

*"Teşekkür ederim,"* dedin, neyi kastettiğini tam olarak bilmeden.

*"Ne için?"*

*"Beklemek için."*"""
                    else -> """Bir şey söylemedin. Sadece oturdun, omzun ona değdi ve New York'a baktınız.

Natasha da konuşmadı. Bunun iyi bir şey olduğunu anlıyordunuz ikiniz de — kelime gerektirmeyen bir yer, kelime gerektirmeyen bir zaman.

Sabahın altısında güneş doğdu. Natasha kalktı, ceketini düzeltti.

*"Sabah brifingi var,"* dedi, ve gitti.

Sen bir süre daha kaldın."""
                }

                natResponse + """

---

Sabah brifingi kısaydı ve acımasız: İstanbul operasyonu kırk sekiz saat içinde. Hale'in verdiği koordinatlar doğrulandı. Kör Nokta'nın iki tesisi tespit edildi — biri sahte, biri gerçek. Gerçek olan, yüzde seksen olasılıkla güçlü bir psionik bariyer taşıyordu.

Wanda: *"Girenin zihnine müdahale edebilirler. Seni kendi kontrollerine sokmak isteyebilirler."*

*"Deneyebilirler,"* dedin.

*"Bu ciddiye alınması gereken bir risk,"* dedi Steve.

*"Biliyorum."* Durdun. *"Ama Beyaz Göz onların frekansıyla çalışmıyor. Benim frekansım farklı. Girişimde bulunan... hasar görür."*

Fury not aldı. *"Teorik."*

*"Her şey teorik,"* dedin, *"ta ki değil olana kadar."*

---

🔀 Operasyon planında ne rolü üstleneceksin?

1️⃣ "Birincil saldırı — cihazı ben takacağım."
2️⃣ "Keşif — önce içeriyi göreyim, sonra karar."
3️⃣ "Tuzak ol — beni istedikleri için beni yem olarak kullanalım." """
            }
            40 -> {
                // Seçim 6: Operasyon rolü
                val isPrimary  = choiceIs(lastUserMsg, 1, "birincil", "saldırı", "ben takacağım", "cihaz")
                val isScout    = choiceIs(lastUserMsg, 2, "keşif", "önce göreyim", "izle", "tara")
                val isBait     = choiceIs(lastUserMsg, 3, "tuzak", "yem", "kullan", "beni kullanın")

                progress = when {
                    isPrimary -> progress.copy(steveBond = progress.steveBond + 2, commitmentLevel = "all_in")
                    isScout   -> progress.copy(mysteryFactor = progress.mysteryFactor + 1, natashaBond = progress.natashaBond + 1)
                    else      -> progress.copy(steveTrust = progress.steveTrust + 2, samRapport = progress.samRapport + 2, commitmentLevel = "sacrifice_willing")
                }
                storyProgressDao.insertOrUpdate(progress)

                val opResponse = when {
                    isPrimary -> """*"Birincil saldırı,"* dedin. *"Cihazı ben takacağım."*

Steve sana baktı, uzun. *"Emin misin?"*

*"Programı kim yarattıysa, ben de bu programın panzehirini taşıyacağım,"* dedin. *"Bu benim işim."*

Sam Wilson: *"Sırtın kollanıyor. Vaat."*

Natasha haritaya eğildi. *"Giriş rotası var — güneydoğu tüneli. Güvenlik kör noktaları burada ve burada."*

Plan şekillenmeye başladı."""
                    isScout -> """*"Önce keşif,"* dedin. *"İçeriyi göreyim, sonra karar."*

Natasha başını salladı — sana en benzeyen tepki buydu.

*"Makul,"* dedi. *"Ben de keşifle giderim. İkimiz daha az iz bırakırız."*

Steve itiraz etmedi. Güveniyordu artık — hem sana hem Natasha'ya.

Plan şekillendi: sen ve Natasha öne, ekip desteğe."""
                    else -> """*"Beni yem olarak kullanın,"* dedin.

Oda sessizleşti.

*"Beni istediklerini biliyoruz. Kör Nokta, Denek 7-A'yı aktive etmek istiyor. Ben gidersem — gözetimli, kontrolsüz gibi görünerek — ana kapıyı açarlar. Siz arkadan girersiniz."*

Sam: *"Bu... Tanrım bu çok riskli."*

*"En etkili plan da bu,"* dedi Natasha sessizce.

Steve uzun süre sana baktı. *"Bunu istemiyorum."*

*"Biliyorum,"* dedin. *"Ama ben istiyorum. Bu sefer kaçmak için değil."*"""
                }

                opResponse + """

---

Kırk sekiz saat geçti.

İstanbul operasyonu başladı.

Tesise girişte her şey plana uydu — fazla uydu. Natasha bunu tehlike işareti olarak okudu, sağ elini eğitti. Haklıydı.

İçeride seni bekleyen biri vardı.

Bir kadın, elli yaşlarında, beyaz saçlı. Tek başına, ortada duruyordu — silahsız, ellerini göstererek. *"Blackwood,"* dedi Türkçeyle başlayıp İngilizceye geçerek. *"Ben de bir taşıyıcıyım. Kırmızı Göz. Ve ben de kaçmak istiyorum."*

Beyaz Göz'ün tüm sinir uçları onu tanıdı — frekans, imza, enerji dili. Yalan söylemiyordu.

---

🔀 Bu kadına nasıl yaklaşıyorsun?

1️⃣ İnan ve yanına al — o da benim gibiyim.
2️⃣ Güvenme, ama bırakma — Wanda okusun önce.
3️⃣ Planı bozma — onu geride bırak, cihazı tak ve çık."""
            }
            41 -> {
                // Seçim 7 (SON KARAR) — Kırmızı Göz taşıyıcısı
                val isTrust   = choiceIs(lastUserMsg, 1, "inan", "yanına al", "benim gibiyim", "güven")
                val isVerify  = choiceIs(lastUserMsg, 2, "güvenme", "ama bırakma", "Wanda", "oku")
                val isLeave   = choiceIs(lastUserMsg, 3, "planı bozma", "geride bırak", "cihazı tak", "çık")

                progress = when {
                    isTrust  -> progress.copy(wandaBond = progress.wandaBond + 1, natashaBond = progress.natashaBond + 1, commitmentLevel = "compassionate")
                    isVerify -> progress.copy(steveTrust = progress.steveTrust + 1, natashaBond = progress.natashaBond + 2)
                    else     -> progress.copy(mysteryFactor = progress.mysteryFactor + 2, commitmentLevel = "mission_focused")
                }
                storyProgressDao.insertOrUpdate(progress)

                val finalChoice = when {
                    isTrust -> """Adım attın. Ona doğru. Avucunu açtın — tehdit değil, tanınma.

*"Adın ne?"* diye sordun.

*"Mira,"* dedi. Sesi titriyordu — korkudan değil, uzun bir bekleyişin sona erdiğini hissedince titreyen türden.

*"Kaç yıl?"* diye sordun.

*"Otuz dört."*

Beyaz Göz kızıl frekansa dokundu — iki güç, iki taşıyıcı, ilk kez serbest. Birbirini tanıyan iki el gibi."""
                    isVerify -> """*"Wanda,"* dedin radyodan. *"Kadını tara."*

On saniye. Yirmi. Wanda'nın sesi: *"Gerçek. Ve korkuyor — tuzak değil bu."*

Adım attın. *"Adın ne?"*

*"Mira."*

*"Natasha,"* dedin, *"onu al yanına."*

Natasha tereddüt etti — bir saniye, iki saniye — sonra ilerledi. Güven zor geçiyordu herkes için."""
                    else -> """*"Planı bozmuyorum,"* dedin, hem kadına hem radyoya. *"Mira, durun burada. Operasyon bittikten sonra dönüyorum."*

Kadının yüzünde bir şey kırıldı — umut değil, ama umuda yakın bir şey kayboldu.

Cihazı taktın. Frekans devre dışı kaldı. Kör Nokta'nın psionik bariyeri çöktü.

Ve geri döndüğünde Mira hâlâ oradaydı. Beklemişti."""
                }

                // Bölümün kapanışı — flag'lere göre özelleşen son paragraf
                val trustLevel = progress.steveTrust + progress.natashaBond + progress.wandaBond
                val flightLevel = progress.flightInstinct
                val endVariant = when {
                    trustLevel >= 12 -> """Operasyon başarılıydı. Kör Nokta'nın aktivasyon protokolü devre dışı kaldı. Mira tahliye edildi. Hale tutuklandı — gönüllü olarak.

Tower'a döndüğünüzde Steve kapıda sizi bekliyordu. Sana baktı, hiçbir şey sormadı.

*"Hoş geldin,"* dedi sadece.

İki kelime. Ama içinde yirmi yıllık bir kapının kapanma sesi vardı."""
                    flightLevel >= 4 -> """Operasyon başarılıydı. Kör Nokta'nın protokolü devre dışı kaldı.

Tower'a dönerken ellerinin hâlâ hafifçe titrerken fark ettin. Eski refleks — kaçmak, kaybolmak, başka bir şehir, başka bir isim.

Ama bu sefer asansöre bindin. Yukarı çıktın. Kapıyı açtın.

Kalmayı seçmek de bir cesaret gerektiriyordu. En az kaçmak kadar."""
                    else -> """Operasyon başarılıydı.

Cihaz yerleştiridi, frekans iptal edildi, Mira kurtarıldı.

Natasha raporunu yazarken sana tek bir mesaj attı: *"İyi iş."*

Bunun ne kadar çok anlam taşıdığını bilen sadece ikinizdì."""
                }

                finalChoice + """

---

""" + endVariant + """

---

📖 BÖLÜM 7 SONU — KARANLIK AYNA

Aiden Blackwood'un gerçek hikayesi başlıyor.
Denek 7-A kaydı silinmedi — ama artık başka ellerde değil.
Karanlık ayna kırıldı. Arkasında ne olduğunu görmek için gözler açık olmalı.

Tebrikler! Bölüm 7'yi başarıyla tamamladın."""
            }

            else -> {
                """📖 **TÜM BÖLÜMLER TAMAMLANDI**

Aiden Blackwood'un hikayesi tamamlandı. Tüm kararların, tüm bölümlerdeki seçimlerinin toplamıyla bu noktataya geldin.

Hikayeyi yeniden oynamak için bölüm listesinden herhangi bir bölümü seçebilirsin — farklı seçimlerle farklı bir Aiden yolculuğu keşfedebilirsin.

🔒 **Bölüm 8 — Yakında**"""
            }
        }
    }



    suspend fun getContentFilterCountForBot(botId: String): Int = withContext(Dispatchers.IO) {
        return@withContext db.emptyResponseLogDao().getContentFilterCountForBot(botId)
    }


    private fun resolveModelForProvider(
        providerKey: String,
        settings: UserSettingsEntity,
        customProvider: com.example.data.local.CustomProviderEntity? = null
    ): String {
        return when {
            providerKey == "groq" -> sanitizeModelName(settings.groqModel.ifBlank { "openai/gpt-oss-120b" })
            providerKey == "openai" -> settings.openaiModel.ifBlank { "gpt-4o" }
            providerKey == "gemini" -> sanitizeModelName(settings.geminiModel.ifBlank { "gemini-2.0-flash" })
            providerKey == "claude" -> settings.claudeModel.ifBlank { "claude-3-5-sonnet-20241022" }
            providerKey == "openrouter" -> settings.openRouterModel.ifBlank { "deepseek/deepseek-chat" }
            providerKey == "nvidia" -> settings.nvidiaModel.ifBlank { "deepseek-ai/deepseek-v4-flash" }
            providerKey == "mistral" -> settings.mistralModel.ifBlank { "mistral-large-latest" }
            providerKey == "pollinations" -> settings.pollinationsModel.ifBlank { "openai" }
            providerKey == "ovh" -> settings.ovhModel.ifBlank { "meta-llama/Meta-Llama-3-70B-Instruct" }
            providerKey == "llm7" -> "default"
            providerKey.startsWith("custom_") -> customProvider?.modelName ?: ""
            else -> sanitizeModelName(settings.geminiModel.ifBlank { "gemini-2.0-flash" })
        }
    }

    private fun validateAndPrepareResult(
        respText: String,
        toolCalls: List<com.example.data.api.ParsedMemoryToolCall>?,
        promptTokens: Long,
        candidateTokens: Long,
        usedProvider: String
    ): com.example.data.api.ModelResponseResult {
        val clean = cleanEmotionTags(respText)
        if (clean.isBlank() && toolCalls.isNullOrEmpty()) {
            throw IllegalStateException("Sağlayıcı ($usedProvider) boş/temizlenmiş metin yanıtı üretti.")
        }
        return com.example.data.api.ModelResponseResult(
            text = respText,
            toolCalls = toolCalls ?: emptyList(),
            promptTokens = promptTokens,
            candidateTokens = candidateTokens,
            usedProvider = usedProvider
        )
    }

    data class ProviderTestResult(
        val isSuccess: Boolean,
        val supportsFunctionCalling: Boolean = false,
        val errorMessage: String = "",
        val rawResponse: String = "",
        val latencyMs: Long = 0L
    )

    val managers: com.example.data.managers.ManagerIndex by lazy {
        com.example.data.managers.ManagerIndex(
            db = db,
            onLogFallback = { entry -> logFallbackAttempt(entry) }
        )
    }

    // --- CastMemberManager Delegations ---

    fun cleanCharacterNameCandidate(raw: String): String =
        managers.castMemberManager.cleanCharacterNameCandidate(raw)

    fun isMainOrUserCharacter(candidateRaw: String, bot: BotEntity): Boolean =
        managers.castMemberManager.isMainOrUserCharacter(candidateRaw, bot)

    suspend fun ensureKeyCharacterMirroredAsCastMember(botId: String, keyChar: KeyCharacter) =
        managers.castMemberManager.ensureKeyCharacterMirroredAsCastMember(botId, keyChar)

    suspend fun ensureSideCharacterRegistered(
        botId: String,
        charName: String,
        role: String = "Yan Karakter",
        initialMood: String = "nötr",
        affectionDelta: Int = 0,
        cueSentence: String = "",
        bot: BotEntity? = null,
        confidence: Int? = null,
        pendingConfirmation: Boolean? = null
    ) = managers.castMemberManager.ensureSideCharacterRegistered(
        botId = botId,
        rawCharacterName = charName,
        role = role,
        initialMood = initialMood,
        affectionDelta = affectionDelta,
        cueSentence = cueSentence,
        bot = bot,
        confidence = confidence,
        pendingConfirmation = pendingConfirmation
    )

    suspend fun getBlacklistedCharacterNames(botId: String): Set<String> =
        managers.castMemberManager.getBlacklistedCharacterNames(botId)

    suspend fun detectAndRegisterCastMembers(
        botId: String,
        aiReplyText: String,
        userQuery: String = ""
    ) = managers.castMemberManager.detectAndRegisterCastMembers(
        botId = botId,
        aiReplyText = aiReplyText,
        userQuery = userQuery
    )

    /**
     * Instance LLM-backed character significance evaluator using resolveUtilityModel().
     * Skips network completely when no API keys/free providers are configured, logs any provider error,
     * and gracefully falls back to the deterministic companion heuristic.
     */
    suspend fun evaluateCharacterSignificance(
        candidateName: String,
        sceneContext: String
    ): Quadruple<Boolean, String, Int, Double> = withContext(Dispatchers.IO) {
        val settings = getOrCreateSettings()
        val hasExplicitProviderKey = when {
            settings.selectedProvider.startsWith("custom_") -> {
                val cid = settings.selectedProvider.removePrefix("custom_").toLongOrNull()
                val cp = if (cid != null) db.customProviderDao().getProviderById(cid) else null
                cp != null && cp.baseUrl.isNotBlank()
            }
            settings.selectedProvider == "groq" -> settings.groqApiKey.isNotBlank()
            settings.selectedProvider == "openai" -> settings.openaiApiKey.isNotBlank()
            settings.selectedProvider == "claude" -> settings.claudeApiKey.isNotBlank()
            settings.selectedProvider == "openrouter" -> settings.openRouterApiKey.isNotBlank()
            settings.selectedProvider == "nvidia" -> settings.nvidiaApiKey.isNotBlank()
            settings.selectedProvider == "mistral" -> settings.mistralApiKey.isNotBlank()
            settings.selectedProvider == "gemini" -> settings.customApiKey.isNotBlank()
            else -> false
        }
        val hasAnyBackupKey = settings.groqApiKey.isNotBlank() ||
            settings.openaiApiKey.isNotBlank() ||
            settings.claudeApiKey.isNotBlank() ||
            settings.openRouterApiKey.isNotBlank() ||
            settings.nvidiaApiKey.isNotBlank() ||
            settings.mistralApiKey.isNotBlank() ||
            settings.customApiKey.isNotBlank() ||
            settings.enableLlm7 || settings.enablePollinations || settings.enableOvh

        if (!hasExplicitProviderKey && !hasAnyBackupKey) {
            return@withContext EmochiRepository.evaluateCharacterSignificance(candidateName, sceneContext)
        }

        val activeProvider = settings.selectedProvider.ifBlank { "gemini" }
        try {
            val res = managers.providerFallbackManager.executeUtilityRequest(
                settings = settings,
                systemPrompt = "Return ONLY JSON: {\"is_significant\":true,\"role\":\"...\",\"confidence\":0-100,\"status\":\"authority|peer|stranger|intimate\"}.",
                userMessage = "Character: $candidateName\nScene: $sceneContext",
                buildConfigGeminiKey = getBuildConfigKey(),
                maxTokens = 160,
                timeoutMs = 3000L
            )
            val raw = res.text.trim()
            if (raw.isBlank()) {
                classificationCallFailureLog.add(
                    ClassificationCallFailure(provider = activeProvider, errorMessage = "Error: Empty utility response")
                )
                return@withContext EmochiRepository.evaluateCharacterSignificance(candidateName, sceneContext)
            }
            val cleaned = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val start = cleaned.indexOf('{')
            val end = cleaned.lastIndexOf('}')
            val jsonStr = if (start >= 0 && end > start) cleaned.substring(start, end + 1) else cleaned
            val obj = JSONObject(jsonStr)
            val isSig = obj.optBoolean("is_significant", true)
            val role = obj.optString("role", "Yan Karakter").ifBlank { "Yan Karakter" }
            val conf = obj.optInt("confidence", 75).coerceIn(0, 100)
            val status = obj.optString("status", "").lowercase()
            val mult = when (status) {
                "authority" -> 0.70
                "stranger" -> 0.85
                "peer" -> 1.05
                "intimate" -> 1.35
                else -> calculateCastMemberDifficultyMultiplier(role, candidateName, sceneContext)
            }
            Quadruple(isSig, role, conf, mult)
        } catch (e: Exception) {
            classificationCallFailureLog.add(
                ClassificationCallFailure(
                    provider = activeProvider,
                    errorMessage = e.message ?: "Error classifying character"
                )
            )
            EmochiRepository.evaluateCharacterSignificance(candidateName, sceneContext)
        }
    }

    suspend fun reEvaluateCastMemberRolesIfNeeded(
        botId: String,
        currentMessageCount: Int,
        sceneContext: String = ""
    ) = managers.castMemberManager.reEvaluateCastMemberRolesIfNeeded(
        botId = botId,
        currentMessageCount = currentMessageCount,
        sceneContext = sceneContext
    )

    suspend fun getCastMembersForBot(botId: String): List<CastMemberEntity> =
        managers.castMemberManager.getCastMembersForBot(botId)

    suspend fun getFirstPendingCastMember(botId: String): CastMemberEntity? =
        managers.castMemberManager.getFirstPendingCastMember(botId)

    suspend fun confirmPendingCastMember(botId: String, castMemberId: Long) =
        managers.castMemberManager.confirmPendingCastMember(botId, castMemberId)

    suspend fun dismissPendingCastMember(botId: String, castMemberId: Long) =
        managers.castMemberManager.dismissPendingCastMember(botId, castMemberId)

    suspend fun blacklistCastMember(botId: String, castMemberId: Long) =
        managers.castMemberManager.blacklistCastMember(botId, castMemberId)

    suspend fun deleteAutoAddedCastMemberById(botId: String, castMemberId: Long) =
        managers.castMemberManager.deleteAutoAddedCastMemberById(botId, castMemberId)

    suspend fun deleteCharacterAndBlacklist(botId: String, characterName: String) =
        managers.castMemberManager.deleteCharacterAndBlacklist(botId, characterName)

    suspend fun updateCastMembersAgeForBot(botId: String, currentStoryCalendarDateStr: String) =
        managers.castMemberManager.updateCastMembersAgeForBot(botId, currentStoryCalendarDateStr)

    // --- WorldStateManager Delegations ---

    suspend fun extractAndSaveRoutinesFromBotPersonality(
        botId: String,
        personality: String,
        scenario: String
    ): List<RoutineEntity> = managers.worldStateManager.extractAndSaveRoutinesFromBotPersonality(
        botId = botId,
        personality = personality,
        scenario = scenario
    )

    fun getDayOfWeekTurkish(dateStr: String): String =
        managers.worldStateManager.getDayOfWeekTurkish(dateStr)

    suspend fun getActiveRoutineReminder(botId: String, currentStoryDate: String): RoutineEntity? =
        managers.worldStateManager.getActiveRoutineReminder(botId, currentStoryDate)

    suspend fun markRoutineMentioned(routineId: Long, storyDate: String, botId: String) =
        managers.worldStateManager.markRoutineMentioned(routineId, storyDate, botId)

    suspend fun extractAndSaveEntityRelations(
        botId: String,
        text: String,
        currentMessageIndex: Int = 0,
        defaultConfidence: String = "certain"
    ): List<EntityRelationEntity> = managers.worldStateManager.extractAndSaveEntityRelations(
        botId = botId,
        text = text,
        currentMessageIndex = currentMessageIndex,
        defaultConfidence = defaultConfidence
    )

    suspend fun traverseRelationGraph(
        botId: String,
        startEntity: String,
        relationSteps: List<String>
    ): com.example.data.managers.GraphTraversalResult? =
        managers.worldStateManager.traverseRelationGraph(botId, startEntity, relationSteps)

    suspend fun detectAndResolveGraphQuery(
        botId: String,
        query: String
    ): com.example.data.managers.GraphTraversalResult? =
        managers.worldStateManager.detectAndResolveGraphQuery(botId, query)

    // --- MemoryRetrievalManager Delegations ---

    fun floatArrayToJson(arr: FloatArray): String =
        managers.memoryRetrievalManager.floatArrayToJson(arr)

    fun jsonToFloatArray(str: String): FloatArray =
        managers.memoryRetrievalManager.jsonToFloatArray(str)

    fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float =
        managers.memoryRetrievalManager.cosineSimilarity(v1, v2)

    suspend fun computeEmbedding(text: String, apiKey: String? = null): FloatArray =
        managers.memoryRetrievalManager.computeEmbedding(
            text = text,
            apiKey = apiKey ?: getBuildConfigKey()
        )

    suspend fun getRelevantMemoryFragments(
        bot: BotEntity,
        queryText: String
    ): List<MemoryFragmentEntity> = managers.memoryRetrievalManager.getRelevantMemoryFragments(
        bot = bot,
        queryText = queryText,
        apiKey = getBuildConfigKey()
    )

    suspend fun deleteMemoryFact(id: Long) =
        managers.memoryRetrievalManager.deleteMemoryFact(id)

    suspend fun deleteMemoryEvent(id: Long) =
        managers.memoryRetrievalManager.deleteMemoryEvent(id)

    suspend fun updateMemoryFact(fact: MemoryFactEntity) =
        managers.memoryRetrievalManager.updateMemoryFact(fact)

    suspend fun updateMemoryEvent(event: MemoryEventEntity) =
        managers.memoryRetrievalManager.updateMemoryEvent(event)

    suspend fun deleteAllAutoMemories(botId: String) =
        managers.memoryRetrievalManager.deleteAllAutoMemories(botId)

    // --- System Prompt & AI Generation ---

    fun buildSystemPrompt(
        bot: BotEntity,
        settings: UserSettingsEntity,
        includeStyleGuide: Boolean = true,
        relevantFragments: List<MemoryFragmentEntity> = emptyList(),
        relevantEvents: List<MemoryEventEntity> = emptyList(),
        relevantFacts: List<MemoryFactEntity> = emptyList(),
        registeredEntities: List<EntityRegistryEntity> = emptyList(),
        recentCheckpoints: List<MemoryCheckpointEntity> = emptyList(),
        castMembers: List<CastMemberEntity> = emptyList(),
        starredAnchorMessages: List<MessageEntity> = emptyList(),
        lastMessageTimestamp: Long = 0L,
        totalMessageCount: Int = 0,
        compactMode: Boolean = false,
        recentSceneText: String = "",
        activeRoutineHint: String = "",
        modelContextWindowTokens: Int = 128_000,
        literaryFramingMode: Boolean = false
    ): String {
        val consistencyNote = pendingConsistencyNoteMap.remove(bot.id)
        val decayNote = pendingDecayNoteMap.remove(bot.id)
        return SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = settings,
                includeStyleGuide = includeStyleGuide,
                relevantFragments = relevantFragments,
                relevantEvents = relevantEvents,
                relevantFacts = relevantFacts,
                registeredEntities = registeredEntities,
                recentCheckpoints = recentCheckpoints,
                castMembers = castMembers,
                starredAnchorMessages = starredAnchorMessages,
                recentSceneText = recentSceneText,
                activeRoutineHint = activeRoutineHint,
                consistencyNote = consistencyNote,
                decayNote = decayNote,
                lastMessageTimestamp = lastMessageTimestamp,
                totalMessageCount = totalMessageCount,
                modelContextWindowTokens = modelContextWindowTokens,
                compactMode = compactMode,
                literaryFramingMode = literaryFramingMode
            )
        )
    }

    suspend fun executeModelRequestWithFallback(
        model: String,
        settings: UserSettingsEntity,
        systemPrompt: String,
        messages: List<MessageEntity>,
        botId: String? = null
    ): com.example.data.api.ModelResponseResult {
        return managers.providerFallbackManager.executeWithFallback(
            model = model,
            settings = settings,
            systemPrompt = systemPrompt,
            messages = messages,
            buildConfigGeminiKey = getBuildConfigKey(),
            tools = com.example.data.api.MemoryToolRegistry.CENTRAL_TOOLS,
            executionMode = com.example.data.managers.ProviderFallbackManager.ExecutionMode.NARRATIVE
        )
    }

    suspend fun generateAiReply(
        bot: BotEntity,
        messages: List<MessageEntity>,
        literaryFramingMode: Boolean = false
    ): AiReplyResult = withContext(Dispatchers.IO) {
        if (bot.id.startsWith("book_") || bot.mode == "book") {
            val bookReply = generateDeterministicBookReply(bot, messages)
            return@withContext AiReplyResult(
                replyText = bookReply,
                usedProvider = "Interaktif Kitap Motoru",
                systemPromptSnapshot = "Book Mode (${bot.aiName.ifBlank { bot.universeName }})"
            )
        }

        val settings = getOrCreateSettings()
        val effectiveModel = getEffectiveModelForProvider(settings)
        val (updatedBot, contextMessages) = prepareContextAndSummarizeIfNeeded(bot, messages, effectiveModel)

        val castMembers = try { getCastMembersForBot(updatedBot.id) } catch (_: Exception) { emptyList() }
        val activeCastNames = (listOf(updatedBot.userCharName, updatedBot.aiName, updatedBot.universeName) +
            castMembers.map { it.name }).filter { it.isNotBlank() }.distinct()
        val modelContextWindow = getModelContextLimit(effectiveModel)

        // Natural Forgetting Reaper (Section 3.1): lightweight local (<2 ms, 0 AI tokens) check every 5 messages
        if (messages.size % 5 == 0 || updatedBot.storyDayCounter > 1L) {
            try {
                reapExpiredMemories(updatedBot.id, updatedBot.storyDayCounter, messages.size)
            } catch (_: Exception) {}
        }

        // 10/10 Co-Piloted (Hybrid + AI) RAG Retrieval:
        // - Simplest trivial/exact-fact turns stay in 0-token Hybrid Fast (<10 ms)
        // - Real story/emotion/subtext/relative-time turns run Hybrid 15-item micro-index -> 1 unified Utility call -> optional miss_queries pass
        val ragBundle = managers.memoryRetrievalManager.retrieveCoPilotedContext(
            bot = updatedBot,
            recentMessages = contextMessages,
            activeCastNames = activeCastNames,
            apiKey = getBuildConfigKey(),
            modelContextWindowTokens = modelContextWindow,
            aiSelector = { ragSysPrompt, ragUserPrompt ->
                callUtilityLlm(
                    settings = settings,
                    systemPrompt = ragSysPrompt,
                    userMessage = ragUserPrompt,
                    botId = updatedBot.id,
                    maxTokens = 180,
                    timeoutMs = 2400L
                )
            }
        )

        // Habit / Repetition Reinforcement (Section 3.2): promote frequently recalled EPHEMERAL memories to CORE
        if (ragBundle.facts.isNotEmpty()) {
            try {
                reinforceRecalledFacts(updatedBot.id, ragBundle.facts, updatedBot.storyDayCounter)
            } catch (_: Exception) {}
        }

        val starredAnchors = try {
            getStarredMessages(updatedBot.id).takeLast(4)
        } catch (_: Exception) {
            emptyList()
        }

        val recentSceneText = contextMessages.takeLast(3).joinToString(" ") { it.text }
        val activeRoutineHint = try {
            val routines = routineDao.getRoutinesForBot(updatedBot.id)
            val active = routines.firstOrNull { it.routineDescription.isNotBlank() }
            active?.routineDescription?.trim().orEmpty()
        } catch (_: Exception) {
            ""
        }

        val sysPrompt = buildSystemPrompt(
            bot = updatedBot,
            settings = settings,
            includeStyleGuide = true,
            relevantFragments = ragBundle.fragments,
            relevantEvents = ragBundle.events,
            relevantFacts = ragBundle.facts,
            recentCheckpoints = ragBundle.checkpoints,
            castMembers = castMembers,
            starredAnchorMessages = starredAnchors,
            lastMessageTimestamp = contextMessages.lastOrNull()?.timestamp ?: 0L,
            totalMessageCount = messages.size,
            recentSceneText = recentSceneText,
            activeRoutineHint = activeRoutineHint,
            modelContextWindowTokens = modelContextWindow,
            literaryFramingMode = literaryFramingMode
        )

        val response = executeModelRequestWithFallback(
            model = effectiveModel,
            settings = settings,
            systemPrompt = sysPrompt,
            messages = contextMessages,
            botId = updatedBot.id
        )
        recordTokenUsage(updatedBot.id, response.promptTokens, response.candidateTokens)

        val latestUserText = contextMessages.lastOrNull { it.role == "user" }?.text.orEmpty()
        val channelASummary = try {
            executeActiveMemoryToolCalls(
                botId = updatedBot.id,
                responseText = response.text,
                toolCalls = response.toolCalls,
                explicitUserOverride = MemoryEngine.hasExplicitUserOverrideIntent(latestUserText)
            )
        } catch (_: Exception) {
            MemoryCommitSummary()
        }

        try {
            parseAndApplyEmotionUpdates(
                botId = updatedBot.id,
                rawResponse = response.text
            )
        } catch (_: Exception) {}

        // 50-Message Checkpoint Pyramid trigger (non-blocking fail-safe)
        try {
            if (messages.size > 0 && messages.size % 50 == 0) {
                checkAndGenerateCheckpoint(updatedBot.id, messages.size)
            }
        } catch (_: Exception) {}

        val cleanedText = cleanEmotionTags(response.text).ifBlank { response.text.trim() }
        AiReplyResult(
            replyText = cleanedText,
            usedProvider = response.usedProvider,
            systemPromptSnapshot = sysPrompt,
            memoryCommitSummary = channelASummary,
            ragRouteMode = ragBundle.routeMode
        )
    }

    suspend fun generateOpeningMessage(
        bot: BotEntity,
        forceRegenerate: Boolean = false,
        toneHint: String = ""
    ): String = withContext(Dispatchers.IO) {
        if (!forceRegenerate && bot.openingMessage.isNotBlank()) return@withContext bot.openingMessage
        val settings = getOrCreateSettings()
        val effectiveModel = getEffectiveModelForProvider(settings)
        val sysPrompt = buildSystemPrompt(bot, settings, compactMode = true)
        val toneInstruction = when (toneHint.trim().lowercase()) {
            "dramatic", "dramatik & gerilimli" -> "Ton: Dramatik, yüksek gerilimli ve atmosferik olsun."
            "warm", "sıcak & samimi"           -> "Ton: Sıcak, samimi, yakın ve doğal hissettirsin."
            "mysterious", "gizemli & yavaş"    -> "Ton: Gizemli, yavaş tempolu, merak uyandıran ve edebi olsun."
            else -> if (toneHint.isNotBlank()) "Ton: $toneHint." else ""
        }
        val altInstruction = if (forceRegenerate && bot.openingMessage.isNotBlank()) {
            " Önceki taslaktaki cümleleri ve açılış kurgusunu tekrarlamadan tamamen taze, farklı bir açıdan alternatif bir açılış sahnesi yaz."
        } else ""
        val promptText = buildString {
            append("Senaryoya uygun etkileyici, karakterin sesini ve ortamın atmosferini hissettiren bir açılış sahnesi ve ilk diyaloğu Türkçe olarak yaz (en fazla 1200 karakter).")
            if (toneInstruction.isNotBlank()) {
                append(" ")
                append(toneInstruction)
            }
            if (altInstruction.isNotBlank()) {
                append(altInstruction)
            }
        }
        val initMsg = MessageEntity(
            id = "opening_gen_${System.currentTimeMillis()}",
            botId = bot.id,
            role = "user",
            text = promptText,
            timestamp = System.currentTimeMillis()
        )
        return@withContext try {
            val res = executeModelRequestWithFallback(effectiveModel, settings, sysPrompt, listOf(initMsg), bot.id)
            cleanEmotionTags(res.text).trim().ifBlank { "Gözlerini sana çevirdi ve hafifçe gülümsedi. \"Sonunda geldin.\"" }
        } catch (_: Exception) {
            "Gözlerini sana çevirdi ve hafifçe gülümsedi. \"Sonunda geldin.\""
        }
    }

    suspend fun updateMemorySummaries(bot: BotEntity, messages: List<MessageEntity>) = withContext(Dispatchers.IO) {
        if (messages.size < 16) return@withContext
        val settings = getOrCreateSettings()
        val effectiveModel = getEffectiveModelForProvider(settings)
        try {
            prepareContextAndSummarizeIfNeeded(bot, messages, effectiveModel)
        } catch (_: Exception) {}
    }

    /**
     * Single unified low-temperature (0.12) Utility Model gateway.
     * Always resolves cheap model via resolveUtilityModel() and attaches X-Emochi-Call-Type: utility
     * (stripped by EmochiApplication.createTemperatureInterceptor() before network transmission).
     */
    suspend fun callUtilityLlm(
        settings: UserSettingsEntity,
        systemPrompt: String,
        userMessage: String,
        botId: String = "",
        maxTokens: Int = 420,
        timeoutMs: Long = 5000L
    ): String = withContext(Dispatchers.IO) {
        try {
            val res = managers.providerFallbackManager.executeUtilityRequest(
                settings = settings,
                systemPrompt = systemPrompt,
                userMessage = userMessage,
                buildConfigGeminiKey = getBuildConfigKey(),
                maxTokens = maxTokens,
                timeoutMs = timeoutMs
            )
            if (botId.isNotBlank()) {
                recordTokenUsage(botId, res.promptTokens, res.candidateTokens)
            }
            res.text.trim()
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun callLlmForAudit(
        settings: UserSettingsEntity,
        systemPrompt: String,
        userMessage: String,
        botId: String = ""
    ): String = callUtilityLlm(
        settings = settings,
        systemPrompt = systemPrompt,
        userMessage = userMessage,
        botId = botId,
        maxTokens = 420,
        timeoutMs = 5500L
    )

    // --- Provider Connection Tests ---

    suspend fun testLlm7Connection(): ProviderTestResult =
        managers.providerFallbackManager.testEndpointConnection(
            url = "https://api.llm7.io/v1/chat/completions",
            apiKey = "",
            modelName = "default",
            apiFormat = "openai",
            testTools = true
        )

    suspend fun testPollinationsConnection(modelName: String = "openai"): ProviderTestResult =
        managers.providerFallbackManager.testEndpointConnection(
            url = "https://text.pollinations.ai/openai/chat/completions",
            apiKey = "",
            modelName = modelName.ifBlank { "openai" },
            apiFormat = "openai",
            testTools = false
        )

    suspend fun testOvhConnection(modelName: String = "Meta-Llama-3_3-70B-Instruct"): ProviderTestResult =
        managers.providerFallbackManager.testEndpointConnection(
            url = "https://oai.endpoints.kepler.ai.cloud.ovh.net/v1/chat/completions",
            apiKey = "",
            modelName = modelName.ifBlank { "Meta-Llama-3_3-70B-Instruct" },
            apiFormat = "openai",
            testTools = false
        )

    suspend fun fetchModelsFromEndpoint(baseUrl: String, apiKey: String): List<String> =
        managers.providerFallbackManager.fetchModelsFromOpenAiCompatibleEndpoint(
            baseUrl = baseUrl,
            apiKey = apiKey
        )

    suspend fun fetchNvidiaModels(apiKey: String): List<String> =
        managers.providerFallbackManager.fetchModelsFromOpenAiCompatibleEndpoint(
            baseUrl = "https://integrate.api.nvidia.com/v1",
            apiKey = apiKey
        )

    suspend fun simulateNvidiaRateLimitTest(
        apiKey: String,
        modelName: String = "deepseek-ai/deepseek-v4-flash"
    ): ProviderTestResult = testNvidiaConnection(apiKey, modelName)

    suspend fun testCustomProviderConnection(
        baseUrl: String,
        apiKey: String,
        modelName: String,
        apiFormat: String = "openai"
    ): ProviderTestResult = managers.providerFallbackManager.testEndpointConnection(
        url = baseUrl,
        apiKey = apiKey,
        modelName = modelName,
        apiFormat = apiFormat,
        testTools = true
    )

    suspend fun testOpenRouterConnection(
        apiKey: String,
        modelName: String = "deepseek/deepseek-chat"
    ): ProviderTestResult = managers.providerFallbackManager.testEndpointConnection(
        url = "https://openrouter.ai/api/v1/chat/completions",
        apiKey = apiKey,
        modelName = modelName.ifBlank { "deepseek/deepseek-chat" },
        apiFormat = "openai",
        testTools = true
    )

    suspend fun testNvidiaConnection(
        apiKey: String,
        modelName: String = ""
    ): ProviderTestResult = managers.providerFallbackManager.testEndpointConnection(
        url = "https://integrate.api.nvidia.com/v1/chat/completions",
        apiKey = apiKey,
        modelName = modelName.ifBlank { "deepseek-ai/deepseek-v4-flash" },
        apiFormat = "openai",
        testTools = false
    )

    suspend fun testMistralConnection(
        apiKey: String,
        modelName: String = "mistral-large-latest"
    ): ProviderTestResult = managers.providerFallbackManager.testEndpointConnection(
        url = "https://api.mistral.ai/v1/chat/completions",
        apiKey = apiKey,
        modelName = modelName.ifBlank { "mistral-large-latest" },
        apiFormat = "openai",
        testTools = true
    )

    // --- Import / Export Backup Snapshot (v2, backward-compatible with v1) ---

    suspend fun exportDataSnapshot(includeApiKeys: Boolean = false): String = withContext(Dispatchers.IO) {
        val snapshot = buildBackupSnapshot(includeApiKeys = includeApiKeys)
        backupAdapter.toJson(snapshot)
    }

    suspend fun importDataSnapshot(
        jsonStr: String,
        overwriteExisting: Boolean = false
    ): BackupImportSummary = withContext(Dispatchers.IO) {
        if (jsonStr.isBlank()) {
            throw IllegalArgumentException("Yedek verisi boş.")
        }
        val snapshot = try {
            backupAdapter.fromJson(jsonStr)
        } catch (e: Exception) {
            throw IllegalArgumentException("Geçersiz yedek JSON formatı: ${e.message}", e)
        } ?: throw IllegalArgumentException("Yedek dosyası çözümlenemedi.")

        val existingSettings = try { settingsDao.getUserSettings() } catch (_: Exception) { null }
        val mergedSettings = snapshot.settings.mergePreservingExistingKeys(existingSettings)
        val milestoneDao = db.milestoneDao()

        db.withTransaction {
            if (overwriteExisting) {
                val existingBots = botDao.getAllBotsList()
                for (existingBot in existingBots) {
                    val bid = existingBot.id
                    messageDao.deleteMessagesForBot(bid)
                    memoryFactDao.deleteAllFactsForBot(bid)
                    memoryEventDao.deleteAllEventsForBot(bid)
                    memoryCheckpointDao.deleteCheckpointsForBot(bid)
                    emotionDao.deleteEmotionsForBot(bid)
                    sensitiveTriggerDao.deleteTriggersForBot(bid)
                    routineDao.deleteRoutinesForBot(bid)
                    milestoneDao.deleteMilestonesForBot(bid)
                    castMemberDao.getCastMembersForBot(bid).forEach { castMemberDao.deleteCastMember(it.id) }
                    entityRelationDao.getRelationsForBot(bid).forEach { entityRelationDao.deleteRelation(it.id) }
                    fragmentDao.deleteFragmentsForBot(bid)
                }
                botDao.deleteAllBots()
            }

            for (b in snapshot.bots) {
                botDao.insertOrUpdate(b)
            }
            for (m in snapshot.messages) {
                messageDao.insertMessage(m)
            }
            settingsDao.insertOrUpdate(mergedSettings)

            // v2 extended Neural Vault & world state tables (gracefully empty on v1 backups)
            if (snapshot.memoryFacts.isNotEmpty()) {
                val botsWithFacts = snapshot.memoryFacts.map { it.botId }.toSet()
                if (!overwriteExisting) {
                    botsWithFacts.forEach { memoryFactDao.deleteAllFactsForBot(it) }
                }
                memoryFactDao.insertFacts(snapshot.memoryFacts.map { it.copy(id = 0L) })
            }
            if (snapshot.memoryEvents.isNotEmpty()) {
                val botsWithEvents = snapshot.memoryEvents.map { it.botId }.toSet()
                if (!overwriteExisting) {
                    botsWithEvents.forEach { memoryEventDao.deleteAllEventsForBot(it) }
                }
                memoryEventDao.insertEvents(snapshot.memoryEvents.map { it.copy(id = 0L) })
            }
            if (snapshot.memoryCheckpoints.isNotEmpty()) {
                val botsWithCheckpoints = snapshot.memoryCheckpoints.map { it.botId }.toSet()
                if (!overwriteExisting) {
                    botsWithCheckpoints.forEach { memoryCheckpointDao.deleteCheckpointsForBot(it) }
                }
                for (cp in snapshot.memoryCheckpoints) {
                    memoryCheckpointDao.insertCheckpoint(cp.copy(id = 0L))
                }
            }
            if (snapshot.castMembers.isNotEmpty()) {
                val botsWithCast = snapshot.castMembers.map { it.botId }.toSet()
                if (!overwriteExisting) {
                    botsWithCast.forEach { bid ->
                        castMemberDao.getCastMembersForBot(bid).forEach { castMemberDao.deleteCastMember(it.id) }
                    }
                }
                for (cm in snapshot.castMembers) {
                    castMemberDao.insertCastMember(cm.copy(id = 0L))
                }
            }
            if (snapshot.characterEmotions.isNotEmpty()) {
                val botsWithEmotions = snapshot.characterEmotions.map { it.botId }.toSet()
                if (!overwriteExisting) {
                    botsWithEmotions.forEach { emotionDao.deleteEmotionsForBot(it) }
                }
                for (em in snapshot.characterEmotions) {
                    emotionDao.insertOrUpdate(em)
                }
            }
            if (snapshot.sensitiveTriggers.isNotEmpty()) {
                val botsWithTriggers = snapshot.sensitiveTriggers.map { it.botId }.toSet()
                if (!overwriteExisting) {
                    botsWithTriggers.forEach { sensitiveTriggerDao.deleteTriggersForBot(it) }
                }
                for (tr in snapshot.sensitiveTriggers) {
                    sensitiveTriggerDao.insertOrUpdate(tr.copy(id = 0L))
                }
            }
            if (snapshot.routines.isNotEmpty()) {
                val botsWithRoutines = snapshot.routines.map { it.botId }.toSet()
                if (!overwriteExisting) {
                    botsWithRoutines.forEach { routineDao.deleteRoutinesForBot(it) }
                }
                routineDao.insertRoutines(snapshot.routines.map { it.copy(id = 0L) })
            }
            if (snapshot.entityRelations.isNotEmpty()) {
                val botsWithRelations = snapshot.entityRelations.map { it.botId }.toSet()
                if (!overwriteExisting) {
                    botsWithRelations.forEach { bid ->
                        entityRelationDao.getRelationsForBot(bid).forEach { entityRelationDao.deleteRelation(it.id) }
                    }
                }
                entityRelationDao.insertRelations(snapshot.entityRelations.map { it.copy(id = 0L) })
            }
            if (snapshot.milestones.isNotEmpty()) {
                for (ms in snapshot.milestones) {
                    milestoneDao.insertMilestone(ms)
                }
            }
        }

        // Invalidate caches so RAG & summarization stay consistent after restore
        lastSummarizedMessageCount.clear()
        managers.memoryRetrievalManager.clearAllCaches()
        autoBackupToStorage()

        BackupImportSummary(
            version = snapshot.version,
            botsCount = snapshot.bots.size,
            messagesCount = snapshot.messages.size,
            memoryFactsCount = snapshot.memoryFacts.size,
            memoryEventsCount = snapshot.memoryEvents.size,
            castMembersCount = snapshot.castMembers.size,
            milestonesCount = snapshot.milestones.size,
            overwriteExisting = overwriteExisting
        )
    }
}
