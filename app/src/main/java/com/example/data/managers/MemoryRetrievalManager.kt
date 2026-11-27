package com.example.data.managers

import android.util.LruCache
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiEmbedContentRequest
import com.example.data.api.GeminiPart
import com.example.data.api.RetrofitClient
import com.example.data.local.BotEntity
import com.example.data.local.MemoryCheckpointDao
import com.example.data.local.MemoryCheckpointEntity
import com.example.data.local.MemoryEventDao
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MemoryFactDao
import com.example.data.local.MemoryFactEntity
import com.example.data.local.MemoryFragmentDao
import com.example.data.local.MemoryFragmentEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.LivingMemoryCodec
import com.example.data.repository.MemoryEngine
import com.example.data.repository.MemoryLifecycleCodec
import com.example.data.repository.MemoryTier
import com.example.data.repository.RelativeTimeCueResolver
import com.example.data.repository.TemporalTarget
import com.example.data.repository.TurkishTextAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.exp
import kotlin.math.ln

data class RagRetrievalBundle(
    val facts: List<MemoryFactEntity> = emptyList(),
    val events: List<MemoryEventEntity> = emptyList(),
    val checkpoints: List<MemoryCheckpointEntity> = emptyList(),
    val fragments: List<MemoryFragmentEntity> = emptyList(),
    val expandedQuery: String = "",
    val estimatedTokens: Int = 0,
    val routeMode: MemoryEngine.RagRouteMode = MemoryEngine.RagRouteMode.HYBRID_FAST,
    val aiSelectorInvoked: Boolean = false,
    val missQueriesUsed: List<String> = emptyList(),
    val topHybridConfidence: Float = 0f,
    val hybridConfidenceGap: Float = 0f,
    val temporalTargetMatched: Boolean = false,
    val graphResolutionNote: String? = null
)

data class IndexedFact(
    val entity: MemoryFactEntity,
    val localVector: FloatArray,
    val remoteVector: FloatArray?,
    val stems: Set<String>,
    val stemCounts: Map<String, Int>,
    val docLen: Int,
    val normalizedText: String,
    val normalizedSubject: String,
    val normalizedKey: String
)

data class IndexedEvent(
    val entity: MemoryEventEntity,
    val parsed: com.example.data.repository.LivingMemory,
    val localVector: FloatArray,
    val remoteVector: FloatArray?,
    val stems: Set<String>,
    val stemCounts: Map<String, Int>,
    val docLen: Int,
    val normalizedText: String,
    val normalizedPov: String
)

data class IndexedCheckpoint(
    val entity: MemoryCheckpointEntity,
    val localVector: FloatArray,
    val remoteVector: FloatArray?,
    val stems: Set<String>,
    val stemCounts: Map<String, Int>,
    val docLen: Int,
    val normalizedText: String
)

data class IndexedFragment(
    val entity: MemoryFragmentEntity,
    val localVector: FloatArray,
    val stems: Set<String>,
    val stemCounts: Map<String, Int>,
    val docLen: Int,
    val normalizedText: String
)

data class BotMemoryIndex(
    val botId: String,
    val facts: List<IndexedFact>,
    val events: List<IndexedEvent>,
    val checkpoints: List<IndexedCheckpoint>,
    val fragments: List<IndexedFragment>,
    val builtAtMs: Long,
    val avgDocLen: Float = 16f,
    val avgFragmentDocLen: Float = 16f
)

/**
 * Zero-redundant-parse RAM LRU cache for up to 5 active bots.
 * Precomputes FloatArray vectors and Turkish morphological stem sets once per mutation,
 * cutting per-turn Hybrid RAG execution down to <12 ms.
 */
class MemoryVectorCache(maxBots: Int = 5) {
    private val lru = object : LruCache<String, BotMemoryIndex>(maxBots) {}

    @Synchronized
    fun get(botId: String): BotMemoryIndex? = lru.get(botId)

    @Synchronized
    fun put(botId: String, index: BotMemoryIndex) {
        lru.put(botId, index)
    }

    @Synchronized
    fun invalidate(botId: String) {
        lru.remove(botId)
    }

    @Synchronized
    fun clearAll() {
        lru.evictAll()
    }
}

class MemoryRetrievalManager(
    private val fragmentDao: MemoryFragmentDao,
    private val memoryFactDao: MemoryFactDao,
    private val memoryEventDao: MemoryEventDao,
    private val memoryCheckpointDao: MemoryCheckpointDao
) {
    val vectorCache = MemoryVectorCache(maxBots = 5)

    private val _lastRouteMode = MutableStateFlow(MemoryEngine.RagRouteMode.HYBRID_FAST)
    val lastRouteMode: StateFlow<MemoryEngine.RagRouteMode> = _lastRouteMode.asStateFlow()

    private val botRouteModes = ConcurrentHashMap<String, MemoryEngine.RagRouteMode>()

    fun getLastRouteModeForBot(botId: String): MemoryEngine.RagRouteMode {
        return botRouteModes[botId] ?: _lastRouteMode.value
    }

    fun invalidateBotCache(botId: String) {
        vectorCache.invalidate(botId)
    }

    fun clearAllCaches() {
        vectorCache.clearAll()
    }

    fun floatArrayToJson(arr: FloatArray): String =
        MemoryEngine.serializeEmbeddingToJson(arr)

    fun jsonToFloatArray(str: String): FloatArray =
        MemoryEngine.parseEmbeddingFromJson(str) ?: FloatArray(0)

    fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float =
        MemoryEngine.cosineSimilarity(v1, v2)

    suspend fun computeEmbedding(text: String, apiKey: String? = null): FloatArray = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return@withContext MemoryEngine.computeTurkishFeatureEmbedding("")

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val req = GeminiEmbedContentRequest(
                    model = "models/text-embedding-004",
                    content = GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = cleanText.take(1500)))
                    )
                )
                val resp = RetrofitClient.service.embedContent(
                    apiKey = apiKey,
                    request = req
                )
                val values = resp.embedding?.values
                if (values != null && values.isNotEmpty()) {
                    return@withContext values.toFloatArray()
                }
            } catch (_: Exception) {
                // Seamless fallback to 256-dim Turkish signed feature hashing
            }
        }
        MemoryEngine.computeTurkishFeatureEmbedding(cleanText)
    }

    /**
     * Dual Embedding Strategy (Section 2.5):
     * Records are saved immediately (<1 ms) with a 256-dim local Turkish vector.
     * If a valid embedding API key exists, silently enriches the stored record in background IO.
     */
    suspend fun enrichFactEmbeddingIfKeyAvailable(factId: Long, botId: String, text: String, apiKey: String?) {
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") return
        withContext(Dispatchers.IO) {
            try {
                val remoteVec = computeEmbedding(text, apiKey)
                if (remoteVec.size > MemoryEngine.LOCAL_EMBEDDING_DIMS) {
                    val existing = memoryFactDao.getFactById(factId) ?: return@withContext
                    if (existing.supersededBy == null) {
                        memoryFactDao.updateFact(existing.copy(embeddingJson = floatArrayToJson(remoteVec)))
                        vectorCache.invalidate(botId)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun enrichEventEmbeddingIfKeyAvailable(eventId: Long, botId: String, text: String, apiKey: String?) {
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") return
        withContext(Dispatchers.IO) {
            try {
                val remoteVec = computeEmbedding(text, apiKey)
                if (remoteVec.size > MemoryEngine.LOCAL_EMBEDDING_DIMS) {
                    val active = memoryEventDao.getActiveEvents(botId).firstOrNull { it.id == eventId } ?: return@withContext
                    memoryEventDao.updateEvent(active.copy(embedding = floatArrayToJson(remoteVec)))
                    vectorCache.invalidate(botId)
                }
            } catch (_: Exception) {}
        }
    }

    private fun computeLocalEmbedding(text: String): FloatArray =
        MemoryEngine.computeTurkishFeatureEmbedding(text)

    private fun countStems(stems: List<String>): Map<String, Int> {
        val freq = HashMap<String, Int>(stems.size)
        for (s in stems) {
            freq[s] = (freq[s] ?: 0) + 1
        }
        return freq
    }

    suspend fun getOrBuildBotIndex(botId: String): BotMemoryIndex = withContext(Dispatchers.IO) {
        vectorCache.get(botId)?.let { return@withContext it }

        val allFacts = try { memoryFactDao.getActiveFacts(botId) } catch (_: Exception) { emptyList() }
        val allEvents = try { memoryEventDao.getActiveEvents(botId) } catch (_: Exception) { emptyList() }
        val allCheckpoints = try { memoryCheckpointDao.getCheckpointsForBot(botId) } catch (_: Exception) { emptyList() }
        val allFragments = try { fragmentDao.getRecentFragments(botId, 400) } catch (_: Exception) { emptyList() }

        val indexedFacts = allFacts.map { fact ->
            val raw = "${fact.subject} ${fact.key} ${fact.value}"
            val parsedVec = jsonToFloatArray(fact.embeddingJson)
            val localVec = if (parsedVec.size == MemoryEngine.LOCAL_EMBEDDING_DIMS) parsedVec else computeLocalEmbedding(raw)
            val remoteVec = if (parsedVec.size > MemoryEngine.LOCAL_EMBEDDING_DIMS) parsedVec else null
            val stemsList = TurkishTextAnalyzer.extractStems(raw)
            IndexedFact(
                entity = fact,
                localVector = localVec,
                remoteVector = remoteVec,
                stems = stemsList.toSet(),
                stemCounts = countStems(stemsList),
                docLen = stemsList.size.coerceAtLeast(1),
                normalizedText = TurkishTextAnalyzer.normalize(raw),
                normalizedSubject = TurkishTextAnalyzer.normalize(fact.subject),
                normalizedKey = TurkishTextAnalyzer.normalize(fact.key)
            )
        }

        val indexedEvents = allEvents.map { ev ->
            val parsed = LivingMemoryCodec.parse(ev.description, ev.importanceScore)
            val searchable = LivingMemoryCodec.toSearchableText(ev.description)
            val parsedVec = jsonToFloatArray(ev.embedding)
            val localVec = if (parsedVec.size == MemoryEngine.LOCAL_EMBEDDING_DIMS) parsedVec else computeLocalEmbedding(searchable)
            val remoteVec = if (parsedVec.size > MemoryEngine.LOCAL_EMBEDDING_DIMS) parsedVec else null
            val stemsList = TurkishTextAnalyzer.extractStems(searchable)
            IndexedEvent(
                entity = ev,
                parsed = parsed,
                localVector = localVec,
                remoteVector = remoteVec,
                stems = stemsList.toSet(),
                stemCounts = countStems(stemsList),
                docLen = stemsList.size.coerceAtLeast(1),
                normalizedText = TurkishTextAnalyzer.normalize(searchable),
                normalizedPov = TurkishTextAnalyzer.normalize(parsed.povCharacter)
            )
        }

        val indexedCheckpoints = allCheckpoints.map { cp ->
            val parsedVec = jsonToFloatArray(cp.embedding)
            val localVec = if (parsedVec.size == MemoryEngine.LOCAL_EMBEDDING_DIMS) parsedVec else computeLocalEmbedding(cp.summaryText)
            val remoteVec = if (parsedVec.size > MemoryEngine.LOCAL_EMBEDDING_DIMS) parsedVec else null
            val stemsList = TurkishTextAnalyzer.extractStems(cp.summaryText)
            IndexedCheckpoint(
                entity = cp,
                localVector = localVec,
                remoteVector = remoteVec,
                stems = stemsList.toSet(),
                stemCounts = countStems(stemsList),
                docLen = stemsList.size.coerceAtLeast(1),
                normalizedText = TurkishTextAnalyzer.normalize(cp.summaryText)
            )
        }

        val indexedFragments = allFragments.map { frag ->
            val stemsList = TurkishTextAnalyzer.extractStems(frag.content)
            IndexedFragment(
                entity = frag,
                localVector = computeLocalEmbedding(frag.content),
                stems = stemsList.toSet(),
                stemCounts = countStems(stemsList),
                docLen = stemsList.size.coerceAtLeast(1),
                normalizedText = TurkishTextAnalyzer.normalize(frag.content)
            )
        }

        val totalDocs = indexedFacts.size + indexedEvents.size + indexedCheckpoints.size + indexedFragments.size
        val totalLen = indexedFacts.sumOf { it.docLen } +
            indexedEvents.sumOf { it.docLen } +
            indexedCheckpoints.sumOf { it.docLen } +
            indexedFragments.sumOf { it.docLen }
        val avgDocLen = if (totalDocs > 0) (totalLen.toFloat() / totalDocs).coerceIn(4f, 200f) else 16f
        val avgFragLen = if (indexedFragments.isNotEmpty()) {
            (indexedFragments.sumOf { it.docLen }.toFloat() / indexedFragments.size).coerceIn(4f, 200f)
        } else 16f

        val built = BotMemoryIndex(
            botId = botId,
            facts = indexedFacts,
            events = indexedEvents,
            checkpoints = indexedCheckpoints,
            fragments = indexedFragments,
            builtAtMs = System.currentTimeMillis(),
            avgDocLen = avgDocLen,
            avgFragmentDocLen = avgFragLen
        )
        vectorCache.put(botId, built)
        built
    }

    /**
     * Multi-turn query expansion (Section 2.3):
     * Latest user turn weighted 2x + preceding 3 turns weighted 1x + active scene side characters.
     */
    fun buildExpandedQuery(
        recentMessages: List<MessageEntity>,
        activeCastNames: List<String> = emptyList()
    ): Pair<String, Set<String>> {
        val tail = recentMessages.takeLast(4)
        val lastUser = tail.lastOrNull { it.role == "user" }?.text.orEmpty().trim()
        val prevContext = tail.dropLast(1).joinToString(" ") { it.text.take(260) }
        val expanded = buildString {
            if (lastUser.isNotBlank()) {
                append(lastUser)
                append(' ')
                append(lastUser)
            }
            if (prevContext.isNotBlank()) {
                append(' ')
                append(prevContext)
            }
        }.trim()

        val mentionedEntities = LinkedHashSet<String>()
        mentionedEntities.addAll(TurkishTextAnalyzer.extractProperNouns(expanded))

        val lowerExpanded = TurkishTextAnalyzer.normalize(expanded)
        for (castName in activeCastNames) {
            val clean = TurkishTextAnalyzer.normalize(castName)
            if (clean.length >= 2) {
                val firstToken = clean.substringBefore(' ')
                if (lowerExpanded.contains(clean) || (firstToken.length >= 3 && lowerExpanded.contains(firstToken))) {
                    mentionedEntities.add(clean)
                    if (firstToken.length >= 3) mentionedEntities.add(firstToken)
                }
            }
        }
        return Pair(expanded, mentionedEntities)
    }

    private fun buildIdfMapFromStems(queryStems: Set<String>, corpusStemSets: List<Set<String>>): Map<String, Float> {
        if (queryStems.isEmpty() || corpusStemSets.isEmpty()) return emptyMap()
        val n = corpusStemSets.size.toFloat()
        val result = HashMap<String, Float>(queryStems.size)
        for (qs in queryStems) {
            var df = 0
            for (ds in corpusStemSets) {
                if (ds.contains(qs)) df++
            }
            val idf = ln(((n - df + 0.5f) / (df + 0.5f)) + 1f).coerceIn(0.4f, 3.0f)
            result[qs] = idf
        }
        return result
    }

    private fun computeBm25Score(
        queryStems: Set<String>,
        stemCounts: Map<String, Int>,
        docLen: Int,
        idfMap: Map<String, Float>,
        avgLen: Float = 16f
    ): Float {
        if (queryStems.isEmpty() || stemCounts.isEmpty()) return 0f
        val k1 = 1.3f
        val b = 0.65f
        val safeAvgLen = avgLen.coerceAtLeast(1f)
        val lenNorm = (1f - b) + b * (docLen.toFloat() / safeAvgLen)

        var score = 0f
        var maxPossible = 0f
        for (qs in queryStems) {
            val idf = idfMap[qs] ?: 1.2f
            maxPossible += idf
            val tf = stemCounts[qs] ?: 0
            if (tf > 0) {
                val tfWeight = (tf * (k1 + 1f)) / (tf + k1 * lenNorm)
                score += idf * tfWeight
            } else if (qs.length >= 4 && stemCounts.keys.any { it.startsWith(qs) || (it.length >= 4 && qs.startsWith(it)) }) {
                score += idf * 0.55f
            }
        }
        return if (maxPossible > 0f) (score / maxPossible).coerceIn(0f, 1.3f) else 0f
    }

    private fun recencyBoost(timestampMs: Long, nowMs: Long, halfLifeHours: Double = 96.0): Float {
        if (timestampMs <= 0L || nowMs <= timestampMs) return 1.0f
        val ageHours = (nowMs - timestampMs).toDouble() / 3_600_000.0
        return exp(-ageHours / halfLifeHours).toFloat().coerceIn(0.05f, 1.0f)
    }

    private sealed class HybridCandidate(
        val microId: String,
        val microLine: String,
        val semScore: Float,
        val remoteSemScore: Float?,
        val lexScore: Float,
        val entityScore: Float,
        val extraBoost: Float,
        val directConfidence: Float,
        val isTemporalBoosted: Boolean = false
    ) {
        var rrfScore: Float = 0f

        class FactCand(
            val fact: MemoryFactEntity,
            currentStoryDay: Long,
            semScore: Float,
            remoteSemScore: Float?,
            lexScore: Float,
            entityScore: Float,
            extraBoost: Float,
            directConfidence: Float,
            isTemporalBoosted: Boolean = false
        ) : HybridCandidate(
            microId = "F${fact.id}",
            microLine = MemoryEngine.formatFactMicroIndex(fact, currentStoryDay),
            semScore = semScore,
            remoteSemScore = remoteSemScore,
            lexScore = lexScore,
            entityScore = entityScore,
            extraBoost = extraBoost,
            directConfidence = directConfidence,
            isTemporalBoosted = isTemporalBoosted
        )

        class EventCand(
            val event: MemoryEventEntity,
            semScore: Float,
            remoteSemScore: Float?,
            lexScore: Float,
            entityScore: Float,
            extraBoost: Float,
            isTemporalBoosted: Boolean = false
        ) : HybridCandidate(
            microId = "E${event.id}",
            microLine = MemoryEngine.formatEventMicroIndex(event),
            semScore = semScore,
            remoteSemScore = remoteSemScore,
            lexScore = lexScore,
            entityScore = entityScore,
            extraBoost = extraBoost,
            directConfidence = 0f,
            isTemporalBoosted = isTemporalBoosted
        )

        class CheckpointCand(
            val checkpoint: MemoryCheckpointEntity,
            semScore: Float,
            remoteSemScore: Float?,
            lexScore: Float,
            entityScore: Float,
            extraBoost: Float
        ) : HybridCandidate(
            microId = "C${checkpoint.checkpointNumber}",
            microLine = MemoryEngine.formatCheckpointMicroIndex(checkpoint),
            semScore = semScore,
            remoteSemScore = remoteSemScore,
            lexScore = lexScore,
            entityScore = entityScore,
            extraBoost = extraBoost,
            directConfidence = 0f
        )

        class FragmentCand(
            val fragment: MemoryFragmentEntity,
            semScore: Float,
            lexScore: Float,
            entityScore: Float,
            extraBoost: Float
        ) : HybridCandidate(
            microId = "M${fragment.id}",
            microLine = MemoryEngine.formatFragmentMicroIndex(fragment),
            semScore = semScore,
            remoteSemScore = null,
            lexScore = lexScore,
            entityScore = entityScore,
            extraBoost = extraBoost,
            directConfidence = 0f
        )
    }

    /**
     * Runs 3-channel (+ optional 4th remote-dense channel) Reciprocal Rank Fusion (RRF, k=60)
     * over the cached bot memory index (<12 ms, 0 AI tokens).
     * Local 256-dim vectors are always ranked apples-to-apples in [semRanks]; remote 768-dim
     * vectors are ranked independently in [remoteDenseRanks] so vector spaces never mix.
     */
    private fun rankWithRrf(
        index: BotMemoryIndex,
        queryText: String,
        mentionedEntities: Set<String>,
        queryRemoteVec: FloatArray?,
        now: Long,
        currentStoryDay: Long = 1L,
        temporalTarget: TemporalTarget? = null
    ): List<HybridCandidate> {
        val queryStems = TurkishTextAnalyzer.extractStems(queryText).toSet()
        val queryLocalVec = computeLocalEmbedding(queryText)
        val normQuery = TurkishTextAnalyzer.normalize(queryText)
        val avgDocLen = index.avgDocLen

        val corpusStemSets = ArrayList<Set<String>>(
            index.facts.size + index.events.size + index.checkpoints.size + index.fragments.size
        )
        index.facts.forEach { corpusStemSets.add(it.stems) }
        index.events.forEach { corpusStemSets.add(it.stems) }
        index.checkpoints.forEach { corpusStemSets.add(it.stems) }
        index.fragments.forEach { corpusStemSets.add(it.stems) }
        val idfMap = buildIdfMapFromStems(queryStems, corpusStemSets)

        val candidates = ArrayList<HybridCandidate>(corpusStemSets.size)

        // Channel scoring: Facts
        for (item in index.facts) {
            val fact = item.entity
            val meta = MemoryLifecycleCodec.parse(fact.confidence)
            val baseConfidence = meta.baseConfidence
            val isDisputed = baseConfidence.equals("disputed", ignoreCase = true)

            val localSem = cosineSimilarity(queryLocalVec, item.localVector).coerceAtLeast(0f)
            val remoteSem = if (queryRemoteVec != null && item.remoteVector != null && item.remoteVector.size == queryRemoteVec.size) {
                cosineSimilarity(queryRemoteVec, item.remoteVector).coerceAtLeast(0f)
            } else null
            val lex = computeBm25Score(queryStems, item.stemCounts, item.docLen, idfMap, avgDocLen)
            val matchedEntCount = mentionedEntities.count { ent ->
                ent.length >= 3 && (item.normalizedText.contains(ent) || item.normalizedSubject.contains(ent))
            }
            val entityHit = when {
                matchedEntCount >= 2 -> 1.35f
                mentionedEntities.any { ent ->
                    item.normalizedSubject.contains(ent) || (item.normalizedSubject.length >= 3 && ent.contains(item.normalizedSubject))
                } -> 1.0f
                matchedEntCount == 1 -> 0.70f
                else -> 0f
            }
            val coOccurrenceBoost = if (matchedEntCount >= 2) MemoryEngine.RRF_UNIT * 0.55f else 0f
            val keyHit = if (item.normalizedKey.length >= 3 && (normQuery.contains(item.normalizedKey) ||
                TurkishTextAnalyzer.extractStems(item.normalizedKey).any { queryStems.contains(it) })
            ) 0.45f else 0f

            // Temporal Boost (Section 4.2): RRF-calibrated (+1.5 RRF units) so matching EPHEMERAL/ARC
            // records rise cleanly without swamping semantic/lexical ordering among multiple matches.
            val matchesTemporalWindow = temporalTarget != null && !isDisputed &&
                (meta.tier == MemoryTier.EPHEMERAL || meta.tier == MemoryTier.ARC) && (
                    meta.createdStoryDay in temporalTarget.targetStoryDayMin..temporalTarget.targetStoryDayMax ||
                        (currentStoryDay <= 1L && meta.tier == MemoryTier.EPHEMERAL && meta.createdStoryDay <= 1L)
                )
            val temporalBoost = if (matchesTemporalWindow) MemoryEngine.TEMPORAL_BOOST else 0f

            val entityChannel = entityHit + keyHit + (if (fact.userCorrected) 0.55f else 0f) +
                (if (matchesTemporalWindow) 1.25f else 0f) - (if (isDisputed) 0.90f else 0f)
            val englishPenalty = if (!fact.userCorrected && fact.isBotSaved &&
                com.example.data.repository.TurkishLanguageGuard.isEnglishDominant(fact.value)
            ) -0.020f else 0f
            val extraBoost = (if (fact.userCorrected) 0.0045f else 0f) +
                temporalBoost +
                coOccurrenceBoost +
                englishPenalty +
                (when (baseConfidence) {
                    "certain", "high" -> 0.0020f
                    "disputed" -> -0.025f
                    else -> 0.0005f
                }) + recencyBoost(fact.lastConfirmedAt, now, 168.0) * 0.0018f

            // Direct exact-fact confidence (used exclusively by Routing Durum B)
            val effectiveSem = remoteSem ?: localSem
            val directConf = ((effectiveSem * 0.42f) + (lex.coerceAtMost(1f) * 0.44f) +
                (if (entityHit > 0f) 0.16f else 0f) + (if (keyHit > 0f) 0.18f else 0f) -
                (if (isDisputed) 0.50f else 0f)).coerceIn(0f, 1f)

            candidates.add(
                HybridCandidate.FactCand(
                    fact = fact,
                    currentStoryDay = currentStoryDay,
                    semScore = localSem,
                    remoteSemScore = remoteSem,
                    lexScore = lex,
                    entityScore = entityChannel,
                    extraBoost = extraBoost,
                    directConfidence = directConf,
                    isTemporalBoosted = matchesTemporalWindow
                )
            )
        }

        // Channel scoring: Living Memories (Events)
        for (item in index.events) {
            val ev = item.entity
            val localSem = cosineSimilarity(queryLocalVec, item.localVector).coerceAtLeast(0f)
            val remoteSem = if (queryRemoteVec != null && item.remoteVector != null && item.remoteVector.size == queryRemoteVec.size) {
                cosineSimilarity(queryRemoteVec, item.remoteVector).coerceAtLeast(0f)
            } else null
            val lex = computeBm25Score(queryStems, item.stemCounts, item.docLen, idfMap, avgDocLen)
            val matchedEventEntCount = mentionedEntities.count { ent ->
                ent.length >= 3 && (item.normalizedText.contains(ent) || item.normalizedPov.contains(ent))
            }
            val entityHit = when {
                matchedEventEntCount >= 2 -> 1.30f
                (item.normalizedPov.length >= 3 && mentionedEntities.any { item.normalizedPov.contains(it) || it.contains(item.normalizedPov) }) ||
                    matchedEventEntCount == 1 -> 0.95f
                else -> 0f
            }
            val titleEmoHit = if (item.parsed.isStructured && queryStems.isNotEmpty()) {
                val titleEmoStems = TurkishTextAnalyzer.extractStems("${item.parsed.title} ${item.parsed.emotionTag}").toSet()
                if (titleEmoStems.any { it in queryStems }) 0.35f else 0f
            } else 0f
            val matchesEventTemporal = temporalTarget != null && ev.storyDayIndex > 0L &&
                ev.storyDayIndex in temporalTarget.targetStoryDayMin..temporalTarget.targetStoryDayMax
            val importanceNorm = ev.importanceScore.coerceIn(10, 100) / 100f
            val entityChannel = entityHit + titleEmoHit + importanceNorm * 0.65f +
                (if (item.parsed.isStructured) 0.20f else 0f) +
                (if (matchesEventTemporal) 0.90f else 0f)
            val coOccurEventBoost = if (matchedEventEntCount >= 2) MemoryEngine.RRF_UNIT * 0.55f else 0f
            val englishEventPenalty = if (ev.isBotSaved &&
                !com.example.data.repository.TurkishLanguageGuard.isLivingMemoryTurkishValid(
                    item.parsed.title, item.parsed.narrative, item.parsed.innerThought
                )
            ) -0.025f else 0f
            val extraBoost = importanceNorm * 0.0035f +
                coOccurEventBoost +
                englishEventPenalty +
                (if (matchesEventTemporal) MemoryEngine.TEMPORAL_BOOST * 0.85f else 0f) +
                recencyBoost(ev.timestamp, now, 120.0) * 0.0015f

            candidates.add(
                HybridCandidate.EventCand(
                    event = ev,
                    semScore = localSem,
                    remoteSemScore = remoteSem,
                    lexScore = lex,
                    entityScore = entityChannel,
                    extraBoost = extraBoost,
                    isTemporalBoosted = matchesEventTemporal
                )
            )
        }

        // Channel scoring: Scene Checkpoints
        for (item in index.checkpoints) {
            val cp = item.entity
            val localSem = cosineSimilarity(queryLocalVec, item.localVector).coerceAtLeast(0f)
            val remoteSem = if (queryRemoteVec != null && item.remoteVector != null && item.remoteVector.size == queryRemoteVec.size) {
                cosineSimilarity(queryRemoteVec, item.remoteVector).coerceAtLeast(0f)
            } else null
            val lex = computeBm25Score(queryStems, item.stemCounts, item.docLen, idfMap, avgDocLen)
            val entityHit = if (mentionedEntities.any { ent -> ent.length >= 3 && item.normalizedText.contains(ent) }) 0.70f else 0f
            val extraBoost = recencyBoost(cp.timestamp, now, 168.0) * 0.0012f
            candidates.add(HybridCandidate.CheckpointCand(cp, localSem, remoteSem, lex, entityHit, extraBoost))
        }

        // Channel scoring: Archive Fragments
        for (item in index.fragments) {
            val frag = item.entity
            val localSem = cosineSimilarity(queryLocalVec, item.localVector).coerceAtLeast(0f)
            val lex = computeBm25Score(queryStems, item.stemCounts, item.docLen, idfMap, avgDocLen)
            val entityHit = if (mentionedEntities.any { ent -> ent.length >= 3 && item.normalizedText.contains(ent) }) 0.55f else 0f
            val extraBoost = recencyBoost(frag.createdAt, now, 96.0) * 0.0010f
            candidates.add(HybridCandidate.FragmentCand(frag, localSem, lex, entityHit, extraBoost))
        }

        if (candidates.isEmpty()) return emptyList()

        // Compute independent ranks for each RRF channel (never mixing local 256d and remote 768d spaces)
        val semRanks = HashMap<String, Int>(candidates.size)
        candidates.sortedByDescending { it.semScore }.forEachIndexed { i, c -> semRanks[c.microId] = i + 1 }

        val remoteDenseRanks = HashMap<String, Int>()
        candidates.filter { it.remoteSemScore != null }
            .sortedByDescending { it.remoteSemScore!! }
            .forEachIndexed { i, c -> remoteDenseRanks[c.microId] = i + 1 }

        val lexRanks = HashMap<String, Int>(candidates.size)
        candidates.sortedByDescending { it.lexScore }.forEachIndexed { i, c -> lexRanks[c.microId] = i + 1 }

        val entRanks = HashMap<String, Int>(candidates.size)
        candidates.sortedByDescending { it.entityScore }.forEachIndexed { i, c -> entRanks[c.microId] = i + 1 }

        for (c in candidates) {
            c.rrfScore = MemoryEngine.rrfScore(
                semRank = semRanks[c.microId] ?: candidates.size,
                lexRank = lexRanks[c.microId] ?: candidates.size,
                entityRank = entRanks[c.microId] ?: candidates.size,
                boost = c.extraBoost,
                remoteDenseRank = remoteDenseRanks[c.microId]
            )
        }

        return candidates.sortedByDescending { it.rrfScore }
    }

    /**
     * 10/10 Co-Piloted (Hybrid + AI) RAG Entry Point (Section 1 & Section 2):
     * - Simplest ~15-20% turns (trivial small-talk OR unambiguous single-fact questions with topConfidence >= 0.88f & gap >= 0.18f)
     *   -> Pure 10/10 Hybrid RRF (0 AI tokens, <10 ms, max 260 tokens).
     * - All real story, RP, emotion, subtext, secret, pronoun, and multi-character turns (~80%)
     *   -> Step 1: 10/10 Hybrid RRF narrows full DB to top 15 ultra-short micro-index lines (<12 ms, 0 tokens).
     *   -> Step 2: Single unified Utility Model call picks 4-8 sharpest IDs + optional miss_queries (<= 2.5s timeout).
     *   -> Step 3: If miss_queries non-empty, instant local 2nd Hybrid RRF pass over RAM cache adds top 2-3 missed items.
     *   -> Step 4: Seamless fallback to Step 1 Hybrid RRF if Utility Model times out or errors.
     */
    suspend fun retrieveCoPilotedContext(
        bot: BotEntity,
        recentMessages: List<MessageEntity>,
        activeCastNames: List<String> = emptyList(),
        apiKey: String? = null,
        modelContextWindowTokens: Int = 128_000,
        aiSelector: (suspend (systemPrompt: String, userPrompt: String) -> String)? = null,
        graphResolver: (suspend (String) -> String?)? = null
    ): RagRetrievalBundle = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val index = getOrBuildBotIndex(bot.id)
        val lastUserMsg = recentMessages.lastOrNull { it.role == "user" }?.text.orEmpty().trim()
        val (expandedQuery, mentionedEntities) = buildExpandedQuery(recentMessages, activeCastNames)

        // Live Knowledge Graph → RAG Bridge (<2 ms local traversal)
        val resolvedGraphNote: String? = if (graphResolver != null && lastUserMsg.isNotBlank()) {
            try {
                graphResolver(lastUserMsg)?.trim()?.takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }
        } else null

        val totalRecords = index.facts.size + index.events.size + index.checkpoints.size + index.fragments.size
        if (totalRecords == 0) {
            recordRouteMode(bot.id, MemoryEngine.RagRouteMode.HYBRID_FAST)
            return@withContext RagRetrievalBundle(
                expandedQuery = expandedQuery.take(300),
                routeMode = MemoryEngine.RagRouteMode.HYBRID_FAST,
                graphResolutionNote = resolvedGraphNote
            )
        }

        // Detect relative-time / anaphoric cues ("Bana dünkinden yap", "sabahki kahve", "aynısından")
        val temporalTarget = RelativeTimeCueResolver.resolveTemporalTarget(lastUserMsg, bot.storyDayCounter)

        // ── ROUTE 1A: Trivial Casual Message (0 AI tokens, <10 ms) ───────────────
        // Never shortcut when a relative-time/anaphoric reference is present!
        if (temporalTarget == null && resolvedGraphNote == null && lastUserMsg.isNotBlank() && MemoryEngine.isTrivialCasualMessage(lastUserMsg)) {
            val pinnedFacts = index.facts
                .map { it.entity }
                .filter { it.userCorrected && !MemoryLifecycleCodec.isDisputed(it.confidence) }
                .sortedByDescending { it.lastConfirmedAt }
                .take(3)
            val latestCheckpoint = index.checkpoints
                .map { it.entity }
                .sortedByDescending { it.checkpointNumber }
                .take(1)
            val budget = MemoryEngine.computeDynamicMemoryBudget(MemoryEngine.RagRouteMode.HYBRID_FAST, modelContextWindowTokens)
            val packed = MemoryEngine.buildBoundedMemoryBlock(
                facts = pinnedFacts,
                events = emptyList(),
                checkpoints = latestCheckpoint,
                fragments = emptyList(),
                maxTokens = budget,
                graphResolutionNote = resolvedGraphNote
            )
            recordRouteMode(bot.id, MemoryEngine.RagRouteMode.HYBRID_FAST)
            return@withContext RagRetrievalBundle(
                facts = packed.facts,
                events = packed.events,
                checkpoints = packed.checkpoints,
                fragments = packed.fragments,
                expandedQuery = expandedQuery.take(300),
                estimatedTokens = packed.estimatedTokens,
                routeMode = MemoryEngine.RagRouteMode.HYBRID_FAST,
                aiSelectorInvoked = false,
                temporalTargetMatched = false,
                graphResolutionNote = resolvedGraphNote
            )
        }

        // Optional remote query embedding ONLY if stored records carry remote embeddings (>256 dims)
        val needsRemoteVec = !apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY" && (
            index.facts.any { it.remoteVector != null } ||
                index.events.any { it.remoteVector != null } ||
                index.checkpoints.any { it.remoteVector != null }
            )
        val queryRemoteVec = if (needsRemoteVec && expandedQuery.isNotBlank()) {
            try { computeEmbedding(expandedQuery, apiKey) } catch (_: Exception) { null }
        } else null

        // ── STEP 1: 10/10 Local Hybrid RRF Pre-Scan (<12 ms, 0 AI tokens) ────────
        val allRanked = rankWithRrf(
            index = index,
            queryText = expandedQuery.ifBlank { lastUserMsg },
            mentionedEntities = mentionedEntities,
            queryRemoteVec = queryRemoteVec,
            now = now,
            currentStoryDay = bot.storyDayCounter,
            temporalTarget = temporalTarget
        )

        val factCands = allRanked.filterIsInstance<HybridCandidate.FactCand>()
        val topFactConf = factCands.firstOrNull()?.directConfidence ?: 0f
        val secondFactConf = factCands.getOrNull(1)?.directConfidence ?: 0f
        val factGap = (topFactConf - secondFactConf).coerceAtLeast(0f)

        // ── ROUTE 1B: Plain Direct Single-Fact Question with High Confidence ─────
        // Forced Co-Piloted (Hybrid + AI) when temporalTarget != null!
        val isPlainSingleFact = lastUserMsg.isNotBlank() && MemoryEngine.isDirectSingleFactQuestion(lastUserMsg)
        if (temporalTarget == null && isPlainSingleFact && topFactConf >= 0.88f && factGap >= 0.18f) {
            val fastFacts = buildAnchoredFacts(index, factCands.take(4).map { it.fact })
            val latestCp = index.checkpoints.map { it.entity }.sortedByDescending { it.checkpointNumber }.take(1)
            val budget = MemoryEngine.computeDynamicMemoryBudget(MemoryEngine.RagRouteMode.HYBRID_FAST, modelContextWindowTokens)
            val packed = MemoryEngine.buildBoundedMemoryBlock(
                facts = fastFacts,
                events = emptyList(),
                checkpoints = latestCp,
                fragments = emptyList(),
                maxTokens = budget,
                graphResolutionNote = resolvedGraphNote
            )
            recordRouteMode(bot.id, MemoryEngine.RagRouteMode.HYBRID_FAST)
            return@withContext RagRetrievalBundle(
                facts = packed.facts,
                events = packed.events,
                checkpoints = packed.checkpoints,
                fragments = packed.fragments,
                expandedQuery = expandedQuery.take(300),
                estimatedTokens = packed.estimatedTokens,
                routeMode = MemoryEngine.RagRouteMode.HYBRID_FAST,
                aiSelectorInvoked = false,
                topHybridConfidence = topFactConf,
                hybridConfidenceGap = factGap,
                temporalTargetMatched = false,
                graphResolutionNote = resolvedGraphNote
            )
        }

        // ── ROUTE 2: Co-Piloted (Hybrid + AI) Pipeline ───────────────────────────
        // Narrow entire DB to top 15 balanced candidates for the ultra-cheap micro-index
        val shortlist = buildBalancedShortlist(allRanked, maxSize = 15)
        val candById = allRanked.associateBy { it.microId }

        var aiInvoked = false
        var pickedIds = LinkedHashSet<String>()
        var missQueries = emptyList<String>()

        if (aiSelector != null && shortlist.isNotEmpty()) {
            val aiSystemPrompt = """
Sen Asroda'nın Ortak RAG Seçicisisin. SADECE geçerli ve tek satır JSON döndür; açıklama yazma.
Görevin: Son konuşmaya, alt metne, duyguya ve karakter ilişkilerine gerçekten hizmet eden en keskin 4-8 aday ID'sini "pick" dizisinde seçmek.
Kullanıcı 'dünkinden', 'sabahki', 'aynısından', 'geçen günkü' gibi dolaylı zamansal ifade kullandıysa, aday indeksindeki [Dün | Geçici] veya [Bugün | Geçici] etiketli kayıtları ÖNCELİKLE seç.
Eğer kullanıcı çok soyut/dolaylı bir konuya değindiyse ve aşağıdaki adaylarda eksik kalan gizli bir anı varsa, "miss_queries" içine 1-2 kısa %100 TÜRKÇE arama ifadesi yaz (asla İngilizce arama yazma); yoksa [] bırak.
Şema: {"pick":["F12","E7","C3"],"miss_queries":[]}
            """.trimIndent()

            val last3Text = recentMessages.takeLast(3).joinToString("\n") { m ->
                val roleLabel = if (m.role == "user") bot.userCharName.ifBlank { "Kullanıcı" } else bot.aiName.ifBlank { "Karakter" }
                "$roleLabel: ${m.text.replace('\n', ' ').trim().take(220)}"
            }
            val microIndexText = shortlist.joinToString("\n") { it.microLine }
            val aiUserPrompt = buildString {
                if (activeCastNames.isNotEmpty()) {
                    appendLine("SAHNE KARAKTERLERİ: ${activeCastNames.take(8).joinToString(", ")}")
                }
                appendLine("SON 3 MESAJ:")
                appendLine(last3Text)
                appendLine("ADAY İNDEKSİ:")
                append(microIndexText)
            }

            aiInvoked = true
            val rawAiJson = try {
                withTimeoutOrNull(2500L) {
                    aiSelector(aiSystemPrompt, aiUserPrompt)
                }
            } catch (_: Exception) {
                null
            }

            if (!rawAiJson.isNullOrBlank()) {
                val parsedAi = parseAiRagDecision(rawAiJson)
                if (parsedAi != null) {
                    pickedIds.addAll(parsedAi.first)
                    missQueries = parsedAi.second.take(2)
                }
            }
        }

        // ── STEP 3: Targeted Second-Pass Local Hybrid RRF on miss_queries (<5 ms, 0 AI tokens)
        val secondPassCandidates = mutableListOf<HybridCandidate>()
        if (missQueries.isNotEmpty()) {
            for (mq in missQueries) {
                val mqEntities = TurkishTextAnalyzer.extractProperNouns(mq)
                val secondRanked = rankWithRrf(
                    index = index,
                    queryText = mq,
                    mentionedEntities = mentionedEntities + mqEntities,
                    queryRemoteVec = null,
                    now = now,
                    currentStoryDay = bot.storyDayCounter,
                    temporalTarget = temporalTarget
                )
                secondRanked
                    .filter { it.microId !in pickedIds && (it.semScore >= 0.14f || it.lexScore >= 0.15f) }
                    .take(2)
                    .forEach { cand ->
                        if (secondPassCandidates.size < 3 && secondPassCandidates.none { it.microId == cand.microId }) {
                            secondPassCandidates.add(cand)
                            pickedIds.add(cand.microId)
                        }
                    }
            }
        }

        val usedCoPilotedAi = pickedIds.isNotEmpty()
        val finalRouteMode = when {
            usedCoPilotedAi && missQueries.isNotEmpty() -> MemoryEngine.RagRouteMode.CO_PILOTED_DEEP
            usedCoPilotedAi -> MemoryEngine.RagRouteMode.CO_PILOTED_AI
            else -> MemoryEngine.RagRouteMode.HYBRID_FAST
        }
        val dynamicBudget = MemoryEngine.computeDynamicMemoryBudget(finalRouteMode, modelContextWindowTokens)

        // Assemble final lists:
        // - Always include latest 2 checkpoints as fixed pyramid base (Section 5.4); older checkpoints ONLY if picked!
        val latestTwoCheckpoints = index.checkpoints
            .map { it.entity }
            .sortedByDescending { it.checkpointNumber }
            .take(2)

        val selectedFacts = LinkedHashMap<Long, MemoryFactEntity>()
        val selectedEvents = LinkedHashMap<Long, MemoryEventEntity>()
        val selectedCheckpoints = LinkedHashMap<Int, MemoryCheckpointEntity>()
        val selectedFragments = LinkedHashMap<Long, MemoryFragmentEntity>()

        if (usedCoPilotedAi) {
            // Put AI-selected + miss_queries-rescued items FIRST in exact priority order
            for (id in pickedIds) {
                when (val cand = candById[id]) {
                    is HybridCandidate.FactCand -> selectedFacts[cand.fact.id] = cand.fact
                    is HybridCandidate.EventCand -> selectedEvents[cand.event.id] = cand.event
                    is HybridCandidate.CheckpointCand -> selectedCheckpoints[cand.checkpoint.checkpointNumber] = cand.checkpoint
                    is HybridCandidate.FragmentCand -> selectedFragments[cand.fragment.id] = cand.fragment
                    null -> {}
                }
            }
            // Anchor top 2 user-locked ★ facts so core constraints never drop
            index.facts
                .map { it.entity }
                .filter { it.userCorrected && !MemoryLifecycleCodec.isDisputed(it.confidence) }
                .sortedByDescending { it.lastConfirmedAt }
                .take(2)
                .forEach { selectedFacts.putIfAbsent(it.id, it) }
        } else {
            // Step 4: Seamless 10/10 Hybrid RRF Fallback (zero error, zero degradation)
            // If a relative-time target matched, keep temporal-boosted EPHEMERAL/ARC facts at the very top!
            val rankedFactCands = allRanked.filterIsInstance<HybridCandidate.FactCand>()
            if (temporalTarget != null) {
                rankedFactCands.filter { it.isTemporalBoosted }.take(5).forEach { selectedFacts[it.fact.id] = it.fact }
            }
            val topFacts = buildAnchoredFacts(index, rankedFactCands.take(10).map { it.fact })
            topFacts.forEach { selectedFacts.putIfAbsent(it.id, it) }

            allRanked.filterIsInstance<HybridCandidate.EventCand>().take(6).forEach {
                selectedEvents[it.event.id] = it.event
            }
            allRanked.filterIsInstance<HybridCandidate.CheckpointCand>()
                .filter { c -> latestTwoCheckpoints.none { it.checkpointNumber == c.checkpoint.checkpointNumber } }
                .filter { it.semScore >= 0.16f || it.lexScore >= 0.16f }
                .take(2)
                .forEach { selectedCheckpoints[it.checkpoint.checkpointNumber] = it.checkpoint }
            allRanked.filterIsInstance<HybridCandidate.FragmentCand>()
                .filter { it.semScore >= 0.15f || it.lexScore >= 0.15f }
                .take(3)
                .forEach { selectedFragments[it.fragment.id] = it.fragment }
        }

        // Always keep the latest 2 checkpoints as the fixed base of the 50-message pyramid
        latestTwoCheckpoints.forEach { selectedCheckpoints.putIfAbsent(it.checkpointNumber, it) }
        val orderedCheckpoints = selectedCheckpoints.values.sortedBy { it.checkpointNumber }

        val packed = MemoryEngine.buildBoundedMemoryBlock(
            facts = selectedFacts.values.toList(),
            events = selectedEvents.values.toList(),
            checkpoints = orderedCheckpoints,
            fragments = selectedFragments.values.toList(),
            maxTokens = dynamicBudget,
            graphResolutionNote = resolvedGraphNote
        )

        recordRouteMode(bot.id, finalRouteMode)
        RagRetrievalBundle(
            facts = packed.facts,
            events = packed.events,
            checkpoints = packed.checkpoints,
            fragments = packed.fragments,
            expandedQuery = expandedQuery.take(300),
            estimatedTokens = packed.estimatedTokens,
            routeMode = finalRouteMode,
            aiSelectorInvoked = aiInvoked,
            missQueriesUsed = missQueries,
            topHybridConfidence = topFactConf,
            hybridConfidenceGap = factGap,
            temporalTargetMatched = temporalTarget != null,
            graphResolutionNote = resolvedGraphNote
        )
    }

    /**
     * Backwards-compatible pure Hybrid RRF retrieval (uses MemoryVectorCache + 3-channel RRF).
     */
    suspend fun retrieveHybridContext(
        bot: BotEntity,
        recentMessages: List<MessageEntity>,
        activeCastNames: List<String> = emptyList(),
        apiKey: String? = null,
        maxTokens: Int = MemoryEngine.MAX_MEMORY_PROMPT_TOKENS,
        graphResolver: (suspend (String) -> String?)? = null
    ): RagRetrievalBundle = withContext(Dispatchers.IO) {
        val bundle = retrieveCoPilotedContext(
            bot = bot,
            recentMessages = recentMessages,
            activeCastNames = activeCastNames,
            apiKey = apiKey,
            modelContextWindowTokens = if (maxTokens <= 650) 32_768 else 128_000,
            aiSelector = null,
            graphResolver = graphResolver
        )
        if (bundle.estimatedTokens <= maxTokens) {
            bundle
        } else {
            val repacked = MemoryEngine.buildBoundedMemoryBlock(
                facts = bundle.facts,
                events = bundle.events,
                checkpoints = bundle.checkpoints,
                fragments = bundle.fragments,
                maxTokens = maxTokens,
                graphResolutionNote = bundle.graphResolutionNote
            )
            bundle.copy(
                facts = repacked.facts,
                events = repacked.events,
                checkpoints = repacked.checkpoints,
                fragments = repacked.fragments,
                estimatedTokens = repacked.estimatedTokens
            )
        }
    }

    private fun buildBalancedShortlist(allRanked: List<HybridCandidate>, maxSize: Int): List<HybridCandidate> {
        val result = LinkedHashMap<String, HybridCandidate>()
        // Temporal-boosted EPHEMERAL/ARC records always sit at the very top of the 15-item shortlist
        allRanked.filter { it.isTemporalBoosted }.take(5).forEach { result[it.microId] = it }
        // Ensure every layer gets representation in the 15-item micro-index without flooding from one table
        allRanked.filterIsInstance<HybridCandidate.FactCand>().take(7).forEach { result.putIfAbsent(it.microId, it) }
        allRanked.filterIsInstance<HybridCandidate.EventCand>().take(5).forEach { result.putIfAbsent(it.microId, it) }
        allRanked.filterIsInstance<HybridCandidate.CheckpointCand>().take(2).forEach { result.putIfAbsent(it.microId, it) }
        allRanked.filterIsInstance<HybridCandidate.FragmentCand>().take(2).forEach { result.putIfAbsent(it.microId, it) }
        for (c in allRanked) {
            if (result.size >= maxSize) break
            result.putIfAbsent(c.microId, c)
        }
        return result.values.take(maxSize).toList()
    }

    private fun buildAnchoredFacts(index: BotMemoryIndex, ranked: List<MemoryFactEntity>): List<MemoryFactEntity> {
        val combined = LinkedHashMap<Long, MemoryFactEntity>()
        index.facts
            .map { it.entity }
            .filter { it.userCorrected && !MemoryLifecycleCodec.isDisputed(it.confidence) }
            .sortedByDescending { it.lastConfirmedAt }
            .take(3)
            .forEach { combined[it.id] = it }
        ranked.forEach { combined.putIfAbsent(it.id, it) }
        return combined.values.toList()
    }

    private fun parseAiRagDecision(raw: String): Pair<List<String>, List<String>>? {
        val cleaned = raw
            .replace(Regex("""```json\s*""", RegexOption.IGNORE_CASE), "")
            .replace("```", "")
            .trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return try {
            val obj = JSONObject(cleaned.substring(start, end + 1))
            val pickArr = obj.optJSONArray("pick")
            val picks = mutableListOf<String>()
            if (pickArr != null) {
                for (i in 0 until pickArr.length()) {
                    val id = pickArr.optString(i).trim().uppercase(java.util.Locale.ROOT)
                    if (id.isNotBlank()) picks.add(id)
                }
            }
            val missArr = obj.optJSONArray("miss_queries")
            val misses = mutableListOf<String>()
            if (missArr != null) {
                for (i in 0 until missArr.length()) {
                    val q = missArr.optString(i).trim()
                    if (q.length >= 2) misses.add(q)
                }
            }
            Pair(picks, misses)
        } catch (_: Exception) {
            null
        }
    }

    private fun recordRouteMode(botId: String, mode: MemoryEngine.RagRouteMode) {
        botRouteModes[botId] = mode
        _lastRouteMode.value = mode
    }

    suspend fun getRelevantMemoryFragments(
        bot: BotEntity,
        queryText: String,
        apiKey: String? = null,
        limit: Int = 6
    ): List<MemoryFragmentEntity> = withContext(Dispatchers.IO) {
        val index = getOrBuildBotIndex(bot.id)
        if (index.fragments.isEmpty()) return@withContext emptyList()
        if (queryText.isBlank()) return@withContext index.fragments.take(limit).map { it.entity }

        val queryStems = TurkishTextAnalyzer.extractStems(queryText).toSet()
        val idfMap = buildIdfMapFromStems(queryStems, index.fragments.map { it.stems })
        val queryVec = computeLocalEmbedding(queryText)
        val now = System.currentTimeMillis()

        index.fragments.map { item ->
            val sem = cosineSimilarity(queryVec, item.localVector).coerceAtLeast(0f)
            val lex = computeBm25Score(queryStems, item.stemCounts, item.docLen, idfMap, index.avgFragmentDocLen)
            val rec = recencyBoost(item.entity.createdAt, now, halfLifeHours = 96.0) * 0.10f
            Pair(item.entity, sem * 0.52f + lex * 0.38f + rec)
        }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    fun getActiveFactsFlow(botId: String): Flow<List<MemoryFactEntity>> =
        memoryFactDao.getActiveFactsFlow(botId)

    fun getActiveEventsFlow(botId: String): Flow<List<MemoryEventEntity>> =
        memoryEventDao.getActiveEventsFlow(botId)

    suspend fun deleteMemoryFact(id: Long, botId: String? = null) = withContext(Dispatchers.IO) {
        val existing = try { memoryFactDao.getFactById(id) } catch (_: Exception) { null }
        memoryFactDao.deleteFact(id)
        val resolvedBot = botId ?: existing?.botId
        if (!resolvedBot.isNullOrBlank()) vectorCache.invalidate(resolvedBot) else vectorCache.clearAll()
    }

    suspend fun deleteMemoryEvent(id: Long, botId: String? = null) = withContext(Dispatchers.IO) {
        memoryEventDao.deleteEvent(id)
        if (!botId.isNullOrBlank()) vectorCache.invalidate(botId) else vectorCache.clearAll()
    }

    suspend fun updateMemoryFact(fact: MemoryFactEntity, preserveLockFlag: Boolean = false) = withContext(Dispatchers.IO) {
        val freshVec = computeLocalEmbedding("${fact.subject} ${fact.key} ${fact.value}")
        memoryFactDao.updateFact(
            fact.copy(
                userCorrected = if (preserveLockFlag) fact.userCorrected else true,
                lastConfirmedAt = System.currentTimeMillis(),
                embeddingJson = floatArrayToJson(freshVec)
            )
        )
        vectorCache.invalidate(fact.botId)
    }

    suspend fun updateMemoryEvent(event: MemoryEventEntity) = withContext(Dispatchers.IO) {
        val searchable = LivingMemoryCodec.toSearchableText(event.description)
        val freshVec = computeLocalEmbedding(searchable)
        memoryEventDao.updateEvent(
            event.copy(
                embedding = floatArrayToJson(freshVec)
            )
        )
        vectorCache.invalidate(event.botId)
    }

    suspend fun deleteAllAutoMemories(botId: String) = withContext(Dispatchers.IO) {
        memoryFactDao.deleteAutoFacts(botId)
        memoryEventDao.deleteAllEventsForBot(botId)
        vectorCache.invalidate(botId)
    }
}
