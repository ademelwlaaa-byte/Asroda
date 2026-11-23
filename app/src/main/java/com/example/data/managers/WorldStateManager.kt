package com.example.data.managers

import com.example.data.local.BotDao
import com.example.data.local.CastMemberEntity
import com.example.data.local.EntityRegistryDao
import com.example.data.local.EntityRelationDao
import com.example.data.local.EntityRelationEntity
import com.example.data.local.RoutineDao
import com.example.data.local.RoutineEntity
import com.example.data.local.WorldState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class GraphTraversalResult(
    val finalTargetName: String,
    val isComplete: Boolean,
    val pathDescriptions: List<String>,
    val visitedNodes: List<String>,
    val missingRelationStep: String? = null,
    val promptExplanation: String = ""
)

class WorldStateManager(
    private val botDao: BotDao,
    private val routineDao: RoutineDao,
    private val entityRelationDao: EntityRelationDao,
    private val entityRegistryDao: EntityRegistryDao
) {
    private val turkishLocale = Locale("tr", "TR")

    private val dayNameMap = linkedMapOf(
        "pazartesi" to "Pazartesi",
        "cumartesi" to "Cumartesi",
        "çarşamba" to "Çarşamba",
        "perşembe" to "Perşembe",
        "cuma" to "Cuma",
        "salı" to "Salı",
        "pazar" to "Pazar"
    )

    /** API 24+ safe (no java.time desugaring required) Turkish day-of-week resolver. */
    fun getDayOfWeekTurkish(dateStr: String): String {
        val clean = dateStr.trim().take(10)
        if (clean.length < 10) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            val parsed = sdf.parse(clean) ?: return ""
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = parsed }
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Pazartesi"
                Calendar.TUESDAY -> "Salı"
                Calendar.WEDNESDAY -> "Çarşamba"
                Calendar.THURSDAY -> "Perşembe"
                Calendar.FRIDAY -> "Cuma"
                Calendar.SATURDAY -> "Cumartesi"
                Calendar.SUNDAY -> "Pazar"
                else -> ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    private val orderedTurkishDays = listOf(
        "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"
    )

    /**
     * Resolves the Turkish weekday for a story turn:
     * - If an explicit storyCalendarDate ("yyyy-MM-dd") is available, uses API 24+ Calendar.
     * - Otherwise maps storyDayCounter deterministically to Monday..Sunday.
     */
    fun resolveStoryDayName(currentStoryDate: String, storyDayCounter: Long = 1L): String {
        val fromDate = getDayOfWeekTurkish(currentStoryDate)
        if (fromDate.isNotBlank()) return fromDate
        val idx = ((storyDayCounter - 1L).coerceAtLeast(0L) % 7L).toInt()
        return orderedTurkishDays[idx]
    }

    /**
     * Canonical story-day key used for routine de-duplication ("2026-11-16" or "DAY_3").
     */
    fun buildStoryDayKey(currentStoryDate: String, storyDayCounter: Long = 1L): String {
        val clean = currentStoryDate.trim().take(10)
        return if (clean.length == 10) clean else "DAY_${storyDayCounter.coerceAtLeast(1L)}"
    }

    private fun normalizeRoutineText(raw: String): String = raw
        .trim()
        .lowercase(turkishLocale)
        .replace(Regex("""[^\p{L}\p{N}\s]"""), " ")
        .replace(Regex("""\s+"""), " ")
        .trim()

    suspend fun getWorldState(botId: String): WorldState? = withContext(Dispatchers.IO) {
        val bot = botDao.getBotById(botId) ?: return@withContext null
        WorldState.fromJson(bot.worldAtmosphere)
    }

    suspend fun updateWorldState(botId: String, state: WorldState) = withContext(Dispatchers.IO) {
        val bot = botDao.getBotById(botId) ?: return@withContext
        botDao.insertOrUpdate(bot.copy(worldAtmosphere = state.toJson()))
    }

    suspend fun extractAndSaveRoutinesFromBotPersonality(
        botId: String,
        personality: String,
        scenario: String
    ): List<RoutineEntity> = withContext(Dispatchers.IO) {
        val combinedText = "$personality\n$scenario".trim()
        if (combinedText.isBlank()) return@withContext emptyList()

        val wbL = """(?<![\p{L}\p{N}])"""
        val wbR = """(?![\p{L}\p{N}])"""
        val routinePatterns = listOf(
            Regex("""(?iu)([^\n.!?]*?${wbL}her\s+(?:pazartesi|salı|çarşamba|perşembe|cuma|cumartesi|pazar)${wbR}[^\n.!?]*[.!?]?)"""),
            Regex("""(?iu)([^\n.!?]*?${wbL}(?:pazartesi|salı|çarşamba|perşembe|cuma|cumartesi|pazar)\s+(?:günleri|sabahları|akşamları|geceleri)${wbR}[^\n.!?]*[.!?]?)"""),
            Regex("""(?iu)([^\n.!?]*?${wbL}(?:her\s+sabah|sabahları|her\s+akşam|akşamları|her\s+gece|geceleri|her\s+gün|her\s+hafta|düzenli\s+olarak|alışkanlık\s+olarak)${wbR}[^\n.!?]*[.!?]?)""")
        )
        val existingRoutines = routineDao.getRoutinesForBot(botId)
        val existingByNorm = existingRoutines.associateBy { normalizeRoutineText(it.routineDescription) }.toMutableMap()
        val extracted = mutableListOf<RoutineEntity>()
        val seenNorms = mutableSetOf<String>()

        for (pattern in routinePatterns) {
            for (match in pattern.findAll(combinedText)) {
                val sentence = match.groupValues[1].trim()
                if (sentence.length < 10) continue
                val normSentence = normalizeRoutineText(sentence.take(160))
                if (normSentence.isBlank() || !seenNorms.add(normSentence)) continue

                val existing = existingByNorm[normSentence]
                    ?: routineDao.findRoutineByDescription(botId, sentence.take(160))
                if (existing != null) {
                    extracted.add(existing)
                    continue
                }

                val lower = sentence.lowercase(turkishLocale)
                val matchedDayKey = dayNameMap.keys.firstOrNull { lower.contains(it) }
                val detectedDay = matchedDayKey?.let { dayNameMap[it] }
                val frequency = when {
                    detectedDay != null || lower.contains("her hafta") -> "weekly"
                    lower.contains("her sabah") || lower.contains("sabahları") -> "morning"
                    lower.contains("her akşam") || lower.contains("akşamları") ||
                        lower.contains("her gece") || lower.contains("geceleri") -> "evening"
                    else -> "daily"
                }

                val entity = RoutineEntity(
                    botId = botId,
                    routineDescription = sentence.take(160),
                    frequency = frequency,
                    dayOfWeek = detectedDay,
                    lastMentionedStoryDate = ""
                )
                val newId = routineDao.insertRoutine(entity)
                val saved = entity.copy(id = newId)
                existingByNorm[normSentence] = saved
                extracted.add(saved)
            }
        }
        extracted
    }

    suspend fun getActiveRoutineReminder(
        botId: String,
        currentStoryDate: String,
        storyDayCounter: Long = 1L,
        sceneTimeOfDay: String = ""
    ): RoutineEntity? = withContext(Dispatchers.IO) {
        val routines = routineDao.getRoutinesForBot(botId)
        if (routines.isEmpty()) return@withContext null

        val dayKey = buildStoryDayKey(currentStoryDate, storyDayCounter)
        val todayName = resolveStoryDayName(currentStoryDate, storyDayCounter)
        val normalizedScene = sceneTimeOfDay.trim().lowercase(turkishLocale)

        val candidates = routines.filter { routine ->
            if (routine.routineDescription.isBlank()) return@filter false
            if (routine.lastMentionedStoryDate.equals(dayKey, ignoreCase = true) ||
                (currentStoryDate.isNotBlank() && routine.lastMentionedStoryDate.equals(currentStoryDate.trim(), ignoreCase = true))
            ) {
                return@filter false
            }
            val routineDay = routine.dayOfWeek?.trim()
            val dayMatches = routineDay.isNullOrBlank() ||
                routineDay.equals(todayName, ignoreCase = true)

            when (routine.frequency.trim().lowercase(turkishLocale)) {
                "weekly" -> !routineDay.isNullOrBlank() &&
                    routineDay.equals(todayName, ignoreCase = true)
                "morning" -> dayMatches && (normalizedScene.isBlank() || normalizedScene == "sabah")
                "evening" -> dayMatches && (normalizedScene.isBlank() || normalizedScene == "akşam" || normalizedScene == "gece")
                "daily" -> dayMatches
                else -> false
            }
        }

        // Prioritize day-specific (weekly) routines over generic daily routines
        candidates.firstOrNull { it.frequency.equals("weekly", ignoreCase = true) }
            ?: candidates.firstOrNull()
    }

    suspend fun markRoutineMentioned(
        routineId: Long,
        storyDate: String,
        botId: String
    ) = withContext(Dispatchers.IO) {
        val existing = routineDao.getRoutinesForBot(botId).firstOrNull { it.id == routineId } ?: return@withContext
        routineDao.updateRoutine(existing.copy(lastMentionedStoryDate = storyDate))
    }

    private fun confidenceRank(conf: String): Int = when (conf.trim().lowercase(turkishLocale)) {
        "certain", "high" -> 3
        "inferred", "medium" -> 2
        "rumor", "low" -> 1
        else -> 1
    }

    companion object {
        const val MAX_GRAPH_DEPTH = 4
    }

    /**
     * Normalizes a Turkish entity surface form (e.g. "Elif'in", "Aiden'la", "Vaelor'un")
     * to a clean lowercase canonical key for graph lookup & cycle detection.
     */
    fun normalizeGraphEntity(raw: String): String {
        var s = raw.trim()
            .replace("’", "'")
            .replace("`", "'")
            .replace(Regex("""[?.!,;:"'()]+$"""), "")
            .trim()
        if (s.isBlank()) return ""
        val apostropheIdx = s.indexOf('\'')
        if (apostropheIdx >= 2) {
            s = s.substring(0, apostropheIdx).trim()
        }
        return s.lowercase(turkishLocale).replace(Regex("""\s+"""), " ").trim()
    }

    private fun normalizeRelationStep(raw: String): String {
        var s = raw.trim().lowercase(turkishLocale)
            .replace("’", "'")
            .replace(Regex("""[?.!,;:]+$"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
        // Strip Turkish genitive/possessive chain suffixes: "kardeşinin" -> "kardeşi", "ortağının" -> "ortağı"
        s = when {
            s.endsWith("larının") -> s.removeSuffix("larının") + "ları"
            s.endsWith("lerinin") -> s.removeSuffix("lerinin") + "leri"
            s.endsWith("sının") -> s.removeSuffix("sının") + "sı"
            s.endsWith("sinin") -> s.removeSuffix("sinin") + "si"
            s.endsWith("sunun") -> s.removeSuffix("sunun") + "su"
            s.endsWith("sünün") -> s.removeSuffix("sünün") + "sü"
            s.endsWith("ının") -> s.removeSuffix("ının") + "ı"
            s.endsWith("inin") -> s.removeSuffix("inin") + "i"
            s.endsWith("unun") -> s.removeSuffix("unun") + "u"
            s.endsWith("ünün") -> s.removeSuffix("ünün") + "ü"
            s.endsWith("nın") && s.length > 4 -> s.removeSuffix("nın")
            s.endsWith("nin") && s.length > 4 -> s.removeSuffix("nin")
            s.endsWith("nun") && s.length > 4 -> s.removeSuffix("nun")
            s.endsWith("nün") && s.length > 4 -> s.removeSuffix("nün")
            else -> s
        }
        return when (s.trim()) {
            "eski sevgili", "ex sevgilisi", "eski kız arkadaşı", "eski erkek arkadaşı" -> "eski sevgilisi"
            "sevgili", "kız arkadaşı", "erkek arkadaşı" -> "sevgilisi"
            "eski patron" -> "eski patronu"
            "patron" -> "patronu"
            "düşman" -> "düşmanı"
            "müttefik" -> "müttefiki"
            "dost" -> "dostu"
            "arkadaş" -> "arkadaşı"
            "kardeş" -> "kardeşi"
            "abla" -> "ablası"
            "abi", "ağabey", "ağabeyi" -> "abisi"
            "baba" -> "babası"
            "anne" -> "annesi"
            "eş", "karısı", "kocası" -> "eşi"
            "eski eş", "eski karısı", "eski kocası" -> "eski eşi"
            "yaşadığı yer", "oturduğu yer", "ikametgahı", "memleketi", "evi" -> "yaşadığı yer"
            "sahip olduğu", "sahibi", "eşyası", "silahı", "kılıcı" -> if (s.trim() == "kılıcı") "kılıcı" else "sahip olduğu"
            else -> s.trim()
        }
    }

    private fun contradictoryRelationsFor(rel: String): Set<String> = when (normalizeRelationStep(rel)) {
        "sevgilisi" -> setOf("eski sevgilisi", "düşmanı")
        "eski sevgilisi" -> setOf("sevgilisi")
        "eşi" -> setOf("eski eşi", "düşmanı")
        "eski eşi" -> setOf("eşi")
        "müttefiki", "dostu", "arkadaşı", "en yakın arkadaşı" -> setOf("düşmanı", "rakibi")
        "düşmanı" -> setOf("müttefiki", "dostu", "arkadaşı", "en yakın arkadaşı", "sevgilisi", "eşi")
        "patronu" -> setOf("eski patronu")
        "eski patronu" -> setOf("patronu")
        else -> emptySet()
    }

    private fun isExclusiveRelation(rel: String): Boolean = normalizeRelationStep(rel) in setOf(
        "sevgilisi",
        "eşi",
        "patronu",
        "yaşadığı yer",
        "annesi",
        "babası"
    )

    suspend fun getEntityRelationsForBot(botId: String): List<EntityRelationEntity> = withContext(Dispatchers.IO) {
        entityRelationDao.getRelationsForBot(botId)
    }

    suspend fun deleteEntityRelation(relationId: Long) = withContext(Dispatchers.IO) {
        entityRelationDao.deleteRelation(relationId)
    }

    /**
     * System #20: Upserts a directed (source, relationType, target) edge into the Knowledge Graph.
     * - Deduplicates normalized (source, rel, target)
     * - Resolves contradictory edges between the same pair (e.g. sevgilisi -> eski sevgilisi, müttefiki -> düşmanı)
     * - Updates exclusive single-target edges (e.g. yaşadığı yer, patronu) when newer or higher confidence
     */
    suspend fun upsertEntityRelation(
        botId: String,
        sourceName: String,
        relationType: String,
        targetName: String,
        currentMessageIndex: Int = 0,
        confidence: String = "certain"
    ): EntityRelationEntity? = withContext(Dispatchers.IO) {
        val cleanSource = sourceName.trim().replace("’", "'").substringBefore("'").trim()
        val cleanTarget = targetName.trim().replace("’", "'").substringBefore("'").trim()
        val cleanRel = normalizeRelationStep(relationType)
        if (cleanSource.length < 2 || cleanTarget.length < 2 || cleanRel.isBlank()) return@withContext null
        val normSource = normalizeGraphEntity(cleanSource)
        val normTarget = normalizeGraphEntity(cleanTarget)
        if (normSource.isBlank() || normTarget.isBlank() || normSource == normTarget) return@withContext null

        val existingRelations = entityRelationDao.getRelationsForBot(botId).toMutableList()

        // 1. Exact duplicate check (normalized)
        val exact = existingRelations.firstOrNull {
            normalizeGraphEntity(it.sourceEntityName) == normSource &&
                normalizeGraphEntity(it.targetEntityName) == normTarget &&
                normalizeRelationStep(it.relationType) == cleanRel
        }
        if (exact != null) {
            val upgraded = exact.copy(
                confidence = if (confidenceRank(confidence) >= confidenceRank(exact.confidence)) confidence else exact.confidence,
                createdAtMessageIndex = maxOf(exact.createdAtMessageIndex, currentMessageIndex)
            )
            if (upgraded != exact) {
                entityRelationDao.updateRelation(upgraded)
            }
            return@withContext upgraded
        }

        // 2. Contradictory edge between SAME (source, target) pair (e.g., sevgilisi -> eski sevgilisi / düşmanı)
        val contradictions = contradictoryRelationsFor(cleanRel)
        if (contradictions.isNotEmpty()) {
            val opposing = existingRelations.filter {
                normalizeGraphEntity(it.sourceEntityName) == normSource &&
                    normalizeGraphEntity(it.targetEntityName) == normTarget &&
                    normalizeRelationStep(it.relationType) in contradictions
            }
            if (opposing.isNotEmpty()) {
                val primary = opposing.first()
                val updated = primary.copy(
                    relationType = cleanRel,
                    confidence = confidence,
                    createdAtMessageIndex = maxOf(primary.createdAtMessageIndex, currentMessageIndex)
                )
                entityRelationDao.updateRelation(updated)
                opposing.drop(1).forEach { entityRelationDao.deleteRelation(it.id) }
                return@withContext updated
            }
        }

        // 3. Same source + same relationType conflict check (update target when newer/higher confidence)
        val conflicting = existingRelations.firstOrNull {
            normalizeGraphEntity(it.sourceEntityName) == normSource &&
                normalizeRelationStep(it.relationType) == cleanRel
        }
        if (conflicting != null &&
            (confidenceRank(confidence) > confidenceRank(conflicting.confidence) ||
                (confidenceRank(confidence) == confidenceRank(conflicting.confidence) &&
                    currentMessageIndex >= conflicting.createdAtMessageIndex)) &&
            (isExclusiveRelation(cleanRel) || conflicting.createdAtMessageIndex <= currentMessageIndex)
        ) {
            val targetEntity = entityRegistryDao.findByName(botId, cleanTarget)
            val updated = conflicting.copy(
                targetEntityId = targetEntity?.id ?: conflicting.targetEntityId,
                targetEntityName = cleanTarget,
                confidence = confidence,
                createdAtMessageIndex = currentMessageIndex
            )
            entityRelationDao.updateRelation(updated)
            return@withContext updated
        }

        val sourceEntity = entityRegistryDao.findByName(botId, cleanSource)
        val targetEntity = entityRegistryDao.findByName(botId, cleanTarget)
        val entity = EntityRelationEntity(
            botId = botId,
            sourceEntityId = sourceEntity?.id ?: 0L,
            sourceEntityName = cleanSource,
            relationType = cleanRel,
            targetEntityId = targetEntity?.id ?: 0L,
            targetEntityName = cleanTarget,
            confidence = confidence,
            createdAtMessageIndex = currentMessageIndex
        )
        val id = entityRelationDao.insertRelation(entity)
        entity.copy(id = id)
    }

    suspend fun extractAndSaveEntityRelations(
        botId: String,
        text: String,
        currentMessageIndex: Int = 0,
        defaultConfidence: String = "certain"
    ): List<EntityRelationEntity> = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext emptyList()

        val saved = mutableListOf<EntityRelationEntity>()

        suspend fun saveEdge(sourceName: String, relType: String, targetName: String, conf: String) {
            upsertEntityRelation(
                botId = botId,
                sourceName = sourceName,
                relationType = relType,
                targetName = targetName,
                currentMessageIndex = currentMessageIndex,
                confidence = conf
            )?.let { saved.add(it) }
        }

        // Supports single names ("Ahmet"), multi-word names ("Kerem Aydın"), and titled names ("Dr. Kerem")
        val nameExpr = """(?:(?:Dr\.|Prof\.|Bay|Bayan|Komiser|Kaptan)\s+)?[A-ZÇĞİÖŞÜ][a-zçğıöşü]{1,22}(?:\s+[A-ZÇĞİÖŞÜ][a-zçğıöşü]{1,22})?"""

        // Pattern 1: "Ahmet'in kardeşi Mehmet'tir." / "Dr. Kerem'in eski patronu Leyla Aydın'dır."
        val possessivePattern = Regex(
            """(?<![\p{L}\p{N}])($nameExpr)['’](?:ın|in|un|ün|nın|nin|nun|nün)\s+((?:en\s+yakın\s+|eski\s+)?(?:kardeşi|ablası|abisi|babası|annesi|ortağı|avukatı|doktoru|arkadaşı|dostu|sevgilisi|düşmanı|rakibi|patronu|asistanı|hocası|eşi|müttefiki|kılıcı))\s+($nameExpr)(?:['’][a-zçğıöşü]+)?"""
        )
        for (match in possessivePattern.findAll(text)) {
            saveEdge(
                sourceName = match.groupValues[1],
                relType = match.groupValues[2],
                targetName = match.groupValues[3],
                conf = defaultConfidence
            )
        }

        // Pattern 2: "Ahmet'in Mehmet adlı kardeşi"
        val appositivePattern = Regex(
            """(?<![\p{L}\p{N}])($nameExpr)(?:['’][a-zçğıöşü]+)?\s+($nameExpr)\s+(?:adlı\s+)?((?:eski\s+)?(?:kardeşi|ablası|abisi|babası|annesi|ortağı|avukatı|doktoru|dostu|sevgilisi|düşmanı|rakibi|patronu|asistanı|müttefiki))(?![\p{L}\p{N}])"""
        )
        for (match in appositivePattern.findAll(text)) {
            saveEdge(
                sourceName = match.groupValues[1],
                relType = match.groupValues[3],
                targetName = match.groupValues[2],
                conf = defaultConfidence
            )
        }

        // Pattern 3: "Ahmet ile Mehmet kardeş" -> canonical possessive form stored in BOTH directions!
        val mutualCanonical = mapOf(
            "kardeş" to "kardeşi",
            "ortak" to "ortağı",
            "dost" to "dostu",
            "arkadaş" to "arkadaşı",
            "müttefik" to "müttefiki",
            "evli" to "eşi",
            "sevgili" to "sevgilisi",
            "düşman" to "düşmanı",
            "rakip" to "rakibi"
        )
        val mutualPattern = Regex(
            """(?<![\p{L}\p{N}])($nameExpr)\s+ile\s+($nameExpr)\s+(kardeş|ortak|dost|arkadaş|müttefik|evli|sevgili|düşman|rakip)(?![\p{L}\p{N}])"""
        )
        for (match in mutualPattern.findAll(text)) {
            val left = match.groupValues[1]
            val right = match.groupValues[2]
            val canonicalRel = mutualCanonical[match.groupValues[3].lowercase(turkishLocale)] ?: match.groupValues[3]
            saveEdge(sourceName = left, relType = canonicalRel, targetName = right, conf = defaultConfidence)
            saveEdge(sourceName = right, relType = canonicalRel, targetName = left, conf = defaultConfidence)
        }

        saved
    }

    suspend fun traverseRelationGraph(
        botId: String,
        startEntity: String,
        relationSteps: List<String>,
        maxDepth: Int = MAX_GRAPH_DEPTH
    ): GraphTraversalResult? = withContext(Dispatchers.IO) {
        val boundedSteps = relationSteps.take(maxDepth.coerceIn(1, MAX_GRAPH_DEPTH))
        if (startEntity.isBlank() || boundedSteps.isEmpty()) return@withContext null
        val allRelations = entityRelationDao.getRelationsForBot(botId)
        if (allRelations.isEmpty()) return@withContext null

        val normStart = normalizeGraphEntity(startEntity)
        if (normStart.isBlank()) return@withContext null

        // Resolve canonical casing from stored relations if available
        var current = allRelations.firstOrNull {
            normalizeGraphEntity(it.sourceEntityName) == normStart
        }?.sourceEntityName ?: startEntity.trim()

        val visited = mutableListOf(current)
        val visitedLower = mutableSetOf(normalizeGraphEntity(current))
        val pathDescriptions = mutableListOf<String>()

        for (rawStep in boundedSteps) {
            val step = normalizeRelationStep(rawStep)
            val currentNorm = normalizeGraphEntity(current)
            val candidates = allRelations.filter { rel ->
                normalizeGraphEntity(rel.sourceEntityName) == currentNorm &&
                    normalizeRelationStep(rel.relationType) == step &&
                    !visitedLower.contains(normalizeGraphEntity(rel.targetEntityName))
            }
            val best = candidates.maxWithOrNull(
                compareBy<EntityRelationEntity>({ confidenceRank(it.confidence) }, { it.createdAtMessageIndex }, { it.id })
            )
            if (best == null) {
                if (pathDescriptions.isEmpty()) return@withContext null
                val explanation = "Zincirin bir kısmını buldum: ${pathDescriptions.joinToString(" -> ")}. " +
                    "Ancak $current adlı kişinin '$step' bilgisini bilmiyorum."
                return@withContext GraphTraversalResult(
                    finalTargetName = current,
                    isComplete = false,
                    pathDescriptions = pathDescriptions,
                    visitedNodes = visited,
                    missingRelationStep = step,
                    promptExplanation = explanation
                )
            }
            pathDescriptions.add("$current --[${best.relationType}]--> ${best.targetEntityName}")
            current = best.targetEntityName
            visited.add(current)
            visitedLower.add(normalizeGraphEntity(current))
        }

        GraphTraversalResult(
            finalTargetName = current,
            isComplete = true,
            pathDescriptions = pathDescriptions,
            visitedNodes = visited,
            missingRelationStep = null,
            promptExplanation = "İlişki zinciri çözüldü: ${pathDescriptions.joinToString(" -> ")}."
        )
    }

    /**
     * Cycle-safe BFS shortest path finder between two entities (depth <= MAX_GRAPH_DEPTH).
     * Used when user asks "Aiden ile Vaelor'un ne ilişkisi var?".
     */
    suspend fun findShortestRelationPath(
        botId: String,
        startEntity: String,
        targetEntity: String,
        maxDepth: Int = MAX_GRAPH_DEPTH
    ): GraphTraversalResult? = withContext(Dispatchers.IO) {
        val normStart = normalizeGraphEntity(startEntity)
        val normTarget = normalizeGraphEntity(targetEntity)
        if (normStart.isBlank() || normTarget.isBlank() || normStart == normTarget) return@withContext null
        val allRelations = entityRelationDao.getRelationsForBot(botId)
        if (allRelations.isEmpty()) return@withContext null

        val canonicalStart = allRelations.firstOrNull {
            normalizeGraphEntity(it.sourceEntityName) == normStart
        }?.sourceEntityName ?: startEntity.trim()

        data class BfsNode(
            val entityName: String,
            val visitedNodes: List<String>,
            val pathDescriptions: List<String>
        )

        val queue = ArrayDeque<BfsNode>()
        val visitedGlobal = mutableSetOf(normStart)
        queue.add(BfsNode(canonicalStart, listOf(canonicalStart), emptyList()))
        val depthLimit = maxDepth.coerceIn(1, MAX_GRAPH_DEPTH)

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.pathDescriptions.size >= depthLimit) continue
            val currentNorm = normalizeGraphEntity(node.entityName)
            val outgoing = allRelations
                .filter { normalizeGraphEntity(it.sourceEntityName) == currentNorm }
                .sortedWith(compareByDescending<EntityRelationEntity> { confidenceRank(it.confidence) }.thenByDescending { it.createdAtMessageIndex })

            for (edge in outgoing) {
                val nextNorm = normalizeGraphEntity(edge.targetEntityName)
                val nextPaths = node.pathDescriptions + "${edge.sourceEntityName} --[${edge.relationType}]--> ${edge.targetEntityName}"
                val nextVisited = node.visitedNodes + edge.targetEntityName
                if (nextNorm == normTarget) {
                    return@withContext GraphTraversalResult(
                        finalTargetName = edge.targetEntityName,
                        isComplete = true,
                        pathDescriptions = nextPaths,
                        visitedNodes = nextVisited,
                        missingRelationStep = null,
                        promptExplanation = "İlişki zinciri çözüldü: ${nextPaths.joinToString(" -> ")}."
                    )
                }
                if (visitedGlobal.add(nextNorm)) {
                    queue.add(BfsNode(edge.targetEntityName, nextVisited, nextPaths))
                }
            }
        }
        null
    }

    private fun resolveAliasToCanonicalEntity(
        rawStart: String,
        castMembers: List<CastMemberEntity>
    ): String {
        val norm = normalizeGraphEntity(rawStart)
        if (norm.isBlank() || castMembers.isEmpty()) return rawStart.trim()
        for (member in castMembers) {
            val memberNorm = normalizeGraphEntity(member.name)
            if (memberNorm == norm) return member.name
            val firstWord = memberNorm.substringBefore(" ")
            if (firstWord.length >= 3 && firstWord == norm) return member.name
            // Also check parenthesized/quoted nicknames inside member.description
            val nickRegex = Regex("""[("«]([\p{L}\s]{2,24})[)"»]""")
            val descAliasMatch = nickRegex.findAll(member.description)
                .any { normalizeGraphEntity(it.groupValues[1]) == norm }
            if (descAliasMatch) return member.name
        }
        return rawStart.trim()
    }

    suspend fun detectAndResolveGraphQuery(
        botId: String,
        query: String,
        castMembers: List<CastMemberEntity> = emptyList()
    ): GraphTraversalResult? = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext null

        val allRelations = entityRelationDao.getRelationsForBot(botId)
        if (allRelations.isEmpty()) return@withContext null

        // 1. Possessive chain query: "Aiden'ın kardeşinin eski patronu kimdi?" / "elif'in ablası kim?"
        val possessiveStartRegex = Regex(
            """(?<![\p{L}\p{N}])((?:(?:Dr\.|Prof\.|Bay|Bayan|Komiser|Kaptan)\s+)?[\p{L}]{2,24}(?:\s+[\p{L}]{2,24})?)['’](?:ın|in|un|ün|nın|nin|nun|nün)\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        val startMatch = possessiveStartRegex.find(trimmed)
        if (startMatch != null) {
            val rawStart = startMatch.groupValues[1].trim()
            val startEntity = resolveAliasToCanonicalEntity(rawStart, castMembers)
            val remainder = startMatch.groupValues[2]
                .replace(Regex("""(?i)\b(kimdir|kimdi|kim|nedir|neydi|ne|nerede|neredeydi|hangisi|var mı|ilişkisi var)\b.*$"""), "")
                .replace(Regex("""[?.!,;:]+$"""), "")
                .trim()
            if (remainder.isNotBlank()) {
                val steps = mutableListOf<String>()
                val stepRegex = Regex(
                    """(?i)(en\s+yakın\s+arkadaş[\p{L}]+|eski\s+(?:patron|sevgili|eş)[\p{L}]*|yaşadığı\s+yer[\p{L}]*|sahip\s+olduğu[\p{L}]*|[\p{L}]{3,})"""
                )
                for (m in stepRegex.findAll(remainder)) {
                    val token = normalizeRelationStep(m.value)
                    if (token.isNotBlank() && token !in setOf("ve", "ile", "bir", "bu", "şu", "o", "mi", "mı", "mu", "mü", "hakkında", "acaba", "peki")) {
                        steps.add(token)
                    }
                }
                if (steps.isNotEmpty()) {
                    val direct = traverseRelationGraph(botId, startEntity, steps.take(MAX_GRAPH_DEPTH))
                    if (direct != null) return@withContext direct
                }
            }
        }

        // 2. General two-entity relation query: "Aiden ile Vaelor'un ne ilişkisi var?"
        val queryNorm = trimmed.lowercase(turkishLocale)
        val knownNodes = (allRelations.map { it.sourceEntityName } + allRelations.map { it.targetEntityName })
            .distinctBy { normalizeGraphEntity(it) }
        val mentionedNodes = knownNodes.filter { node ->
            val norm = normalizeGraphEntity(node)
            norm.length >= 2 && queryNorm.contains(norm)
        }
        if (mentionedNodes.size >= 2 &&
            (queryNorm.contains("ilişki") || queryNorm.contains("kim") || queryNorm.contains("bağ") || queryNorm.contains("ne"))
        ) {
            val pathForward = findShortestRelationPath(botId, mentionedNodes[0], mentionedNodes[1])
            if (pathForward != null) return@withContext pathForward
            val pathReverse = findShortestRelationPath(botId, mentionedNodes[1], mentionedNodes[0])
            if (pathReverse != null) return@withContext pathReverse
        }

        null
    }
}
