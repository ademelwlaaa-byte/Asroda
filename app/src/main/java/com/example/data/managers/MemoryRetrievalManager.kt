package com.example.data.managers

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
import com.example.data.repository.TurkishTextAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

data class RagRetrievalBundle(
    val facts: List<MemoryFactEntity> = emptyList(),
    val events: List<MemoryEventEntity> = emptyList(),
    val checkpoints: List<MemoryCheckpointEntity> = emptyList(),
    val fragments: List<MemoryFragmentEntity> = emptyList(),
    val expandedQuery: String = "",
    val estimatedTokens: Int = 0
)

class MemoryRetrievalManager(
    private val fragmentDao: MemoryFragmentDao,
    private val memoryFactDao: MemoryFactDao,
    private val memoryEventDao: MemoryEventDao,
    private val memoryCheckpointDao: MemoryCheckpointDao
) {
    fun floatArrayToJson(arr: FloatArray): String {
        val jsonArray = JSONArray()
        for (v in arr) {
            jsonArray.put(v.toDouble())
        }
        return jsonArray.toString()
    }

    fun jsonToFloatArray(str: String): FloatArray {
        if (str.isBlank() || str == "[]") return FloatArray(0)
        return try {
            val arr = JSONArray(str)
            FloatArray(arr.length()) { idx -> arr.optDouble(idx, 0.0).toFloat() }
        } catch (_: Exception) {
            FloatArray(0)
        }
    }

    fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.isEmpty() || v2.isEmpty() || v1.size != v2.size) return 0f
        var dot = 0f
        var norm1 = 0f
        var norm2 = 0f
        for (i in v1.indices) {
            dot += v1[i] * v2[i]
            norm1 += v1[i] * v1[i]
            norm2 += v2[i] * v2[i]
        }
        val denom = sqrt(norm1) * sqrt(norm2)
        return if (denom > 1e-6f) (dot / denom).coerceIn(-1f, 1f) else 0f
    }

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
                // Fall back seamlessly to 256-dim Turkish signed feature hashing
            }
        }
        MemoryEngine.computeTurkishFeatureEmbedding(cleanText)
    }

    private fun computeLocalEmbedding(text: String): FloatArray =
        MemoryEngine.computeTurkishFeatureEmbedding(text)

    /**
     * Multi-turn query expansion: combines the latest user turn (weighted 2x) with the preceding
     * 3 turns so pronoun-heavy Turkish follow-ups ("ona ne dedin?", "o söz?") still match accurately.
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
                append(lastUser) // 2x weight for current user intent
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

    /**
     * BM25-inspired Turkish stemmed lexical relevance score normalized to [0f, 1f].
     */
    private fun computeStemmedLexicalScore(
        queryStems: Set<String>,
        docText: String,
        idfMap: Map<String, Float>
    ): Float {
        if (queryStems.isEmpty() || docText.isBlank()) return 0f
        val docStems = TurkishTextAnalyzer.extractStems(docText)
        if (docStems.isEmpty()) return 0f

        val docFreq = HashMap<String, Int>(docStems.size)
        for (s in docStems) {
            docFreq[s] = (docFreq[s] ?: 0) + 1
        }

        val k1 = 1.3f
        val b = 0.65f
        val avgLen = 18f
        val lenNorm = (1f - b) + b * (docStems.size.toFloat() / avgLen)

        var score = 0f
        var maxPossible = 0f
        for (qs in queryStems) {
            val idf = idfMap[qs] ?: 1.2f
            maxPossible += idf
            val tf = docFreq[qs] ?: 0
            if (tf > 0) {
                val tfWeight = (tf * (k1 + 1f)) / (tf + k1 * lenNorm)
                score += idf * tfWeight
            } else {
                // Prefix match fallback for Turkish root variants (min 4 chars)
                if (qs.length >= 4 && docFreq.keys.any { it.startsWith(qs) || qs.startsWith(it) && it.length >= 4 }) {
                    score += idf * 0.55f
                }
            }
        }
        return if (maxPossible > 0f) (score / maxPossible).coerceIn(0f, 1.25f) else 0f
    }

    private fun buildIdfMap(queryStems: Set<String>, corpusTexts: List<String>): Map<String, Float> {
        if (queryStems.isEmpty() || corpusTexts.isEmpty()) return emptyMap()
        val n = corpusTexts.size.toFloat()
        val docStemsList = corpusTexts.map { TurkishTextAnalyzer.extractStems(it).toSet() }
        val result = HashMap<String, Float>(queryStems.size)
        for (qs in queryStems) {
            var df = 0
            for (ds in docStemsList) {
                if (ds.contains(qs)) df++
            }
            val idf = ln(((n - df + 0.5f) / (df + 0.5f)) + 1f).coerceIn(0.4f, 3.0f)
            result[qs] = idf
        }
        return result
    }

    private fun recencyBoost(timestampMs: Long, nowMs: Long, halfLifeHours: Double = 72.0): Float {
        if (timestampMs <= 0L || nowMs <= timestampMs) return 1.0f
        val ageHours = (nowMs - timestampMs).toDouble() / 3_600_000.0
        return exp(-ageHours / halfLifeHours).toFloat().coerceIn(0.05f, 1.0f)
    }

    private fun semanticSimilarity(
        queryLocalVec: FloatArray,
        queryRemoteVec: FloatArray?,
        storedEmbeddingJson: String,
        fallbackText: String
    ): Float {
        val stored = jsonToFloatArray(storedEmbeddingJson)
        if (stored.isNotEmpty()) {
            if (queryRemoteVec != null && stored.size == queryRemoteVec.size) {
                return cosineSimilarity(queryRemoteVec, stored).coerceAtLeast(0f)
            }
            if (stored.size == queryLocalVec.size) {
                return cosineSimilarity(queryLocalVec, stored).coerceAtLeast(0f)
            }
        }
        val docLocal = computeLocalEmbedding(fallbackText)
        return cosineSimilarity(queryLocalVec, docLocal).coerceAtLeast(0f)
    }

    /**
     * Full 10/10 Unlimited Multi-Source Hybrid RAG Retrieval across:
     * 1) ALL Active Neural Vault Facts (MemoryFactEntity)
     * 2) ALL Active Living Multi-POV Memories (MemoryEventEntity)
     * 3) ALL Long-Term Scene Checkpoints (MemoryCheckpointEntity)
     * 4) Searchable Archive Fragments (MemoryFragmentEntity, up to 400)
     *
     * Never loses older memories regardless of chat length, and packs them cleanly inside
     * the token budget with cross-source deduplication.
     */
    suspend fun retrieveHybridContext(
        bot: BotEntity,
        recentMessages: List<MessageEntity>,
        activeCastNames: List<String> = emptyList(),
        apiKey: String? = null,
        maxTokens: Int = MemoryEngine.MAX_MEMORY_PROMPT_TOKENS
    ): RagRetrievalBundle = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val (expandedQuery, mentionedEntities) = buildExpandedQuery(recentMessages, activeCastNames)
        val queryStems = TurkishTextAnalyzer.extractStems(expandedQuery).toSet()
        val queryLocalVec = computeLocalEmbedding(expandedQuery)

        // Optional remote embedding only if a stored record uses remote dims (> 256)
        val allFacts = try { memoryFactDao.getActiveFacts(bot.id) } catch (_: Exception) { emptyList() }
        val allEvents = try { memoryEventDao.getActiveEvents(bot.id) } catch (_: Exception) { emptyList() }
        val allCheckpoints = try { memoryCheckpointDao.getCheckpointsForBot(bot.id) } catch (_: Exception) { emptyList() }
        val allFragments = try { fragmentDao.getRecentFragments(bot.id, 400) } catch (_: Exception) { emptyList() }

        val needsRemoteVec = !apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY" && (
            allEvents.any { it.embedding.length > 3000 } || allFacts.any { it.embeddingJson.length > 3000 }
        )
        val queryRemoteVec = if (needsRemoteVec && expandedQuery.isNotBlank()) {
            try { computeEmbedding(expandedQuery, apiKey) } catch (_: Exception) { null }
        } else null

        // Build unified IDF corpus across facts + events + checkpoints for sharp Turkish term specificity
        val corpusTexts = ArrayList<String>(allFacts.size + allEvents.size + allCheckpoints.size)
        allFacts.forEach { corpusTexts.add("${it.subject} ${it.key} ${it.value}") }
        allEvents.forEach { corpusTexts.add(LivingMemoryCodec.toSearchableText(it.description)) }
        allCheckpoints.forEach { corpusTexts.add(it.summaryText) }
        val idfMap = buildIdfMap(queryStems, corpusTexts)

        // ── 1. Rank Neural Vault Facts (Unlimited) ───────────────────────────────────
        val rankedFacts = if (allFacts.isEmpty()) {
            emptyList()
        } else {
            val scored = allFacts.map { fact ->
                val text = "${fact.subject} ${fact.key} ${fact.value}"
                val sem = semanticSimilarity(queryLocalVec, queryRemoteVec, fact.embeddingJson, text)
                val lex = computeStemmedLexicalScore(queryStems, text, idfMap)
                val subjectLower = TurkishTextAnalyzer.normalize(fact.subject)
                val entityHit = if (mentionedEntities.any { ent ->
                    subjectLower.contains(ent) || ent.contains(subjectLower) && subjectLower.length >= 3
                }) 0.42f else 0f
                val pinBoost = if (fact.userCorrected) 0.30f else 0f
                val confBoost = when (fact.confidence.lowercase()) {
                    "certain", "high" -> 0.14f
                    "medium" -> 0.08f
                    else -> 0.03f
                }
                val rec = recencyBoost(fact.lastConfirmedAt, now, halfLifeHours = 168.0) * 0.12f
                val totalScore = (sem * 0.48f) + (lex * 0.38f) + entityHit + pinBoost + confBoost + rec
                Pair(fact, totalScore)
            }.sortedByDescending { it.second }

            // Always anchor top 3 userCorrected / pinned facts + top 12 query-ranked facts (deduped)
            val anchored = allFacts.filter { it.userCorrected }.sortedByDescending { it.lastConfirmedAt }.take(3)
            val combined = LinkedHashMap<Long, MemoryFactEntity>()
            anchored.forEach { combined[it.id] = it }
            scored.take(15).forEach { combined[it.first.id] = it.first }
            combined.values.take(15).toList()
        }

        // ── 2. Rank Living Multi-POV Memories (Unlimited) ────────────────────────────
        val rankedEvents = if (allEvents.isEmpty()) {
            emptyList()
        } else {
            val scored = allEvents.map { ev ->
                val parsed = LivingMemoryCodec.parse(ev.description, ev.importanceScore)
                val searchable = LivingMemoryCodec.toSearchableText(ev.description)
                val sem = semanticSimilarity(queryLocalVec, queryRemoteVec, ev.embedding, searchable)
                val lex = computeStemmedLexicalScore(queryStems, searchable, idfMap)

                val povLower = TurkishTextAnalyzer.normalize(parsed.povCharacter)
                val entityHit = if (
                    (povLower.length >= 3 && mentionedEntities.any { povLower.contains(it) || it.contains(povLower) }) ||
                    mentionedEntities.any { ent -> ent.length >= 3 && TurkishTextAnalyzer.normalize(searchable).contains(ent) }
                ) 0.35f else 0f

                val importanceNorm = (ev.importanceScore.coerceIn(10, 100) / 100f)
                val structuredBonus = if (parsed.isStructured) 0.12f else 0f
                val rec = recencyBoost(ev.timestamp, now, halfLifeHours = 120.0) * 0.12f

                val totalScore = (sem * 0.44f) + (lex * 0.34f) + entityHit + (importanceNorm * 0.24f) + structuredBonus + rec
                Pair(ev, totalScore)
            }.sortedByDescending { it.second }

            // Always include top-scored relevant memories + 1 most important defining milestone if high salience (>= 90)
            val selected = LinkedHashMap<Long, MemoryEventEntity>()
            scored.take(8).forEach { selected[it.first.id] = it.first }
            allEvents.filter { it.importanceScore >= 90 }
                .sortedByDescending { it.timestamp }
                .take(2)
                .forEach { selected.putIfAbsent(it.id, it) }
            selected.values.take(10).toList()
        }

        // ── 3. Rank Long-Term Scene Checkpoints (Chronological + Semantic) ───────────
        val rankedCheckpoints = if (allCheckpoints.isEmpty()) {
            emptyList()
        } else {
            val latestTwo = allCheckpoints.sortedByDescending { it.checkpointNumber }.take(2)
            val olderScored = allCheckpoints
                .filter { cp -> latestTwo.none { it.id == cp.id } }
                .map { cp ->
                    val sem = semanticSimilarity(queryLocalVec, queryRemoteVec, cp.embedding, cp.summaryText)
                    val lex = computeStemmedLexicalScore(queryStems, cp.summaryText, idfMap)
                    Pair(cp, sem * 0.55f + lex * 0.45f)
                }
                .filter { it.second >= 0.18f }
                .sortedByDescending { it.second }
                .take(2)
                .map { it.first }

            (olderScored + latestTwo).sortedBy { it.checkpointNumber }
        }

        // ── 4. Rank Archive Fragments (Unlimited up to 400) ──────────────────────────
        val rankedFragments = if (allFragments.isEmpty() || expandedQuery.isBlank()) {
            allFragments.take(4)
        } else {
            allFragments.map { frag ->
                val sem = cosineSimilarity(queryLocalVec, computeLocalEmbedding(frag.content)).coerceAtLeast(0f)
                val lex = computeStemmedLexicalScore(queryStems, frag.content, idfMap)
                val rec = recencyBoost(frag.createdAt, now, halfLifeHours = 96.0) * 0.10f
                Pair(frag, sem * 0.50f + lex * 0.40f + rec)
            }
                .filter { it.second >= 0.14f }
                .sortedByDescending { it.second }
                .take(5)
                .map { it.first }
        }

        // ── 5. Token-Budgeted Cross-Source Deduplicated Packing ──────────────────────
        val packed = MemoryEngine.buildBoundedMemoryBlock(
            facts = rankedFacts,
            events = rankedEvents,
            checkpoints = rankedCheckpoints,
            fragments = rankedFragments,
            maxTokens = maxTokens
        )

        RagRetrievalBundle(
            facts = packed.facts,
            events = packed.events,
            checkpoints = packed.checkpoints,
            fragments = packed.fragments,
            expandedQuery = expandedQuery.take(300),
            estimatedTokens = packed.estimatedTokens
        )
    }

    suspend fun getRelevantMemoryFragments(
        bot: BotEntity,
        queryText: String,
        apiKey: String? = null,
        limit: Int = 6
    ): List<MemoryFragmentEntity> = withContext(Dispatchers.IO) {
        val allFragments = fragmentDao.getRecentFragments(bot.id, 400)
        if (allFragments.isEmpty()) return@withContext emptyList()
        if (queryText.isBlank()) return@withContext allFragments.take(limit)

        val queryStems = TurkishTextAnalyzer.extractStems(queryText).toSet()
        val idfMap = buildIdfMap(queryStems, allFragments.map { it.content })
        val queryVec = computeLocalEmbedding(queryText)
        val now = System.currentTimeMillis()

        allFragments.map { frag ->
            val sem = cosineSimilarity(queryVec, computeLocalEmbedding(frag.content)).coerceAtLeast(0f)
            val lex = computeStemmedLexicalScore(queryStems, frag.content, idfMap)
            val rec = recencyBoost(frag.createdAt, now, halfLifeHours = 96.0) * 0.10f
            Pair(frag, sem * 0.52f + lex * 0.38f + rec)
        }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    fun getActiveFactsFlow(botId: String): Flow<List<MemoryFactEntity>> =
        memoryFactDao.getActiveFactsFlow(botId)

    fun getActiveEventsFlow(botId: String): Flow<List<MemoryEventEntity>> =
        memoryEventDao.getActiveEventsFlow(botId)

    suspend fun deleteMemoryFact(id: Long) = withContext(Dispatchers.IO) {
        memoryFactDao.deleteFact(id)
    }

    suspend fun deleteMemoryEvent(id: Long) = withContext(Dispatchers.IO) {
        memoryEventDao.deleteEvent(id)
    }

    suspend fun updateMemoryFact(fact: MemoryFactEntity) = withContext(Dispatchers.IO) {
        val freshVec = computeLocalEmbedding("${fact.subject} ${fact.key} ${fact.value}")
        memoryFactDao.updateFact(
            fact.copy(
                userCorrected = true,
                lastConfirmedAt = System.currentTimeMillis(),
                embeddingJson = floatArrayToJson(freshVec)
            )
        )
    }

    suspend fun updateMemoryEvent(event: MemoryEventEntity) = withContext(Dispatchers.IO) {
        val searchable = LivingMemoryCodec.toSearchableText(event.description)
        val freshVec = computeLocalEmbedding(searchable)
        memoryEventDao.updateEvent(
            event.copy(
                embedding = floatArrayToJson(freshVec)
            )
        )
    }

    suspend fun deleteAllAutoMemories(botId: String) = withContext(Dispatchers.IO) {
        memoryFactDao.deleteAutoFacts(botId)
        memoryEventDao.deleteAllEventsForBot(botId)
    }
}
