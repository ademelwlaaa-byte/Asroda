package com.example.data.managers

import com.example.data.local.BotDao
import com.example.data.local.EntityRegistryDao
import com.example.data.local.EntityRelationDao
import com.example.data.local.EntityRelationEntity
import com.example.data.local.RoutineDao
import com.example.data.local.RoutineEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

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

    fun getDayOfWeekTurkish(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr.trim().take(10))
            when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "Pazartesi"
                DayOfWeek.TUESDAY -> "Salı"
                DayOfWeek.WEDNESDAY -> "Çarşamba"
                DayOfWeek.THURSDAY -> "Perşembe"
                DayOfWeek.FRIDAY -> "Cuma"
                DayOfWeek.SATURDAY -> "Cumartesi"
                DayOfWeek.SUNDAY -> "Pazar"
            }
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun extractAndSaveRoutinesFromBotPersonality(
        botId: String,
        personality: String,
        scenario: String
    ): List<RoutineEntity> = withContext(Dispatchers.IO) {
        val combinedText = "$personality\n$scenario".trim()
        if (combinedText.isBlank()) return@withContext emptyList()

        val routinePatterns = listOf(
            Regex("""(?i)([^\n.!?]*?\bher\s+(?:pazartesi|salı|çarşamba|perşembe|cuma|cumartesi|pazar)\b[^\n.!?]*[.!?]?)"""),
            Regex("""(?i)([^\n.!?]*?\b(?:pazartesi|salı|çarşamba|perşembe|cuma|cumartesi|pazar)\s+günleri\b[^\n.!?]*[.!?]?)"""),
            Regex("""(?i)([^\n.!?]*?\b(?:her sabah|her akşam|her gece|her gün|her hafta|düzenli olarak|alışkanlık olarak)\b[^\n.!?]*[.!?]?)""")
        )
        val extracted = mutableListOf<RoutineEntity>()
        val seenSentences = mutableSetOf<String>()

        for (pattern in routinePatterns) {
            for (match in pattern.findAll(combinedText)) {
                val sentence = match.groupValues[1].trim()
                if (sentence.length < 10) continue
                val normSentence = sentence.lowercase(turkishLocale)
                if (!seenSentences.add(normSentence)) continue

                val lower = sentence.lowercase(turkishLocale)
                val matchedDayKey = dayNameMap.keys.firstOrNull { lower.contains(it) }
                val detectedDay = matchedDayKey?.let { dayNameMap[it] }
                val frequency = when {
                    detectedDay != null || lower.contains("her hafta") -> "weekly"
                    lower.contains("her sabah") -> "morning"
                    lower.contains("her akşam") || lower.contains("her gece") -> "evening"
                    else -> "daily"
                }

                val existing = routineDao.findRoutineByDescription(botId, sentence.take(160))
                if (existing == null) {
                    val entity = RoutineEntity(
                        botId = botId,
                        routineDescription = sentence.take(160),
                        frequency = frequency,
                        dayOfWeek = detectedDay,
                        lastMentionedStoryDate = ""
                    )
                    val newId = routineDao.insertRoutine(entity)
                    extracted.add(entity.copy(id = newId))
                } else {
                    extracted.add(existing)
                }
            }
        }
        extracted
    }

    suspend fun getActiveRoutineReminder(
        botId: String,
        currentStoryDate: String
    ): RoutineEntity? = withContext(Dispatchers.IO) {
        val routines = routineDao.getRoutinesForBot(botId)
        if (routines.isEmpty()) return@withContext null

        val todayName = getDayOfWeekTurkish(currentStoryDate)
        routines.firstOrNull { routine ->
            if (routine.lastMentionedStoryDate.equals(currentStoryDate, ignoreCase = true)) {
                return@firstOrNull false
            }
            when (routine.frequency.lowercase(turkishLocale)) {
                "weekly" -> !routine.dayOfWeek.isNullOrBlank() &&
                    routine.dayOfWeek.equals(todayName, ignoreCase = true)
                "daily", "morning", "evening" -> routine.dayOfWeek.isNullOrBlank() ||
                    routine.dayOfWeek.equals(todayName, ignoreCase = true)
                else -> false
            }
        }
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

    private fun normalizeRelationStep(raw: String): String {
        var s = raw.trim().lowercase(turkishLocale)
            .replace("’", "'")
            .replace(Regex("""[?.!,;:]+$"""), "")
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
        return s.trim()
    }

    suspend fun extractAndSaveEntityRelations(
        botId: String,
        text: String,
        currentMessageIndex: Int = 0,
        defaultConfidence: String = "certain"
    ): List<EntityRelationEntity> = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext emptyList()

        val saved = mutableListOf<EntityRelationEntity>()
        val existingRelations = entityRelationDao.getRelationsForBot(botId).toMutableList()

        suspend fun upsertRelation(sourceName: String, relType: String, targetName: String, conf: String) {
            val cleanSource = sourceName.trim()
            val cleanTarget = targetName.trim()
            val cleanRel = normalizeRelationStep(relType)
            if (cleanSource.length < 2 || cleanTarget.length < 2 || cleanRel.isBlank()) return
            if (cleanSource.equals(cleanTarget, ignoreCase = true)) return

            val exact = existingRelations.firstOrNull {
                it.sourceEntityName.equals(cleanSource, ignoreCase = true) &&
                    it.targetEntityName.equals(cleanTarget, ignoreCase = true) &&
                    normalizeRelationStep(it.relationType) == cleanRel
            }
            if (exact != null) {
                saved.add(exact)
                return
            }

            // Conflict check: same source + same relationType -> update if incoming has >= confidence and newer/equal index
            val conflicting = existingRelations.firstOrNull {
                it.sourceEntityName.equals(cleanSource, ignoreCase = true) &&
                    normalizeRelationStep(it.relationType) == cleanRel
            }
            if (conflicting != null &&
                (confidenceRank(conf) > confidenceRank(conflicting.confidence) ||
                    (confidenceRank(conf) == confidenceRank(conflicting.confidence) &&
                        currentMessageIndex >= conflicting.createdAtMessageIndex))
            ) {
                val targetEntity = entityRegistryDao.findByName(botId, cleanTarget)
                val updated = conflicting.copy(
                    targetEntityId = targetEntity?.id ?: conflicting.targetEntityId,
                    targetEntityName = cleanTarget,
                    confidence = conf,
                    createdAtMessageIndex = currentMessageIndex
                )
                entityRelationDao.updateRelation(updated)
                existingRelations.remove(conflicting)
                existingRelations.add(updated)
                saved.add(updated)
                return
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
                confidence = conf,
                createdAtMessageIndex = currentMessageIndex
            )
            val id = entityRelationDao.insertRelation(entity)
            val created = entity.copy(id = id)
            existingRelations.add(created)
            saved.add(created)
        }

        // Pattern 1: "Ahmet'in kardeşi Mehmet'tir." / "Mehmet'in en yakın arkadaşı Can'dır." / "Can'ın patronu Leyla'dır."
        val possessivePattern = Regex(
            """\b([A-ZÇĞİÖŞÜ][a-zçğıöşü]{1,22})['’](?:ın|in|un|ün|nın|nin|nun|nün)\s+((?:en\s+yakın\s+)?(?:kardeşi|ablası|abisi|babası|annesi|ortağı|avukatı|doktoru|arkadaşı|dostu|sevgilisi|düşmanı|patronu|asistanı|hocası|eşi))\s+([A-ZÇĞİÖŞÜ][a-zçğıöşü]{1,22})(?:['’][a-zçğıöşü]+)?"""
        )
        for (match in possessivePattern.findAll(text)) {
            upsertRelation(
                sourceName = match.groupValues[1],
                relType = match.groupValues[2],
                targetName = match.groupValues[3],
                conf = defaultConfidence
            )
        }

        // Pattern 2: "Ahmet'in Mehmet adlı kardeşi"
        val appositivePattern = Regex(
            """\b([A-ZÇĞİÖŞÜ][a-zçğıöşü]{2,20})(?:['’][a-zçğıöşü]+)?\s+([A-ZÇĞİÖŞÜ][a-zçğıöşü]{2,20})\s+(?:adlı\s+)?(kardeşi|ablası|abisi|babası|annesi|ortağı|avukatı|doktoru|dostu|sevgilisi|düşmanı|patronu|asistanı)\b"""
        )
        for (match in appositivePattern.findAll(text)) {
            upsertRelation(
                sourceName = match.groupValues[1],
                relType = match.groupValues[3],
                targetName = match.groupValues[2],
                conf = defaultConfidence
            )
        }

        // Pattern 3: "Ahmet ile Mehmet kardeş"
        val mutualPattern = Regex(
            """\b([A-ZÇĞİÖŞÜ][a-zçğıöşü]{2,20})\s+ile\s+([A-ZÇĞİÖŞÜ][a-zçğıöşü]{2,20})\s+(kardeş|ortak|dost|evli|sevgili|düşman|rakip)\b"""
        )
        for (match in mutualPattern.findAll(text)) {
            upsertRelation(
                sourceName = match.groupValues[1],
                relType = match.groupValues[3],
                targetName = match.groupValues[2],
                conf = defaultConfidence
            )
        }

        saved
    }

    suspend fun traverseRelationGraph(
        botId: String,
        startEntity: String,
        relationSteps: List<String>
    ): GraphTraversalResult? = withContext(Dispatchers.IO) {
        if (startEntity.isBlank() || relationSteps.isEmpty()) return@withContext null
        val allRelations = entityRelationDao.getRelationsForBot(botId)
        if (allRelations.isEmpty()) return@withContext null

        var current = startEntity.trim()
        val visited = mutableListOf(current)
        val pathDescriptions = mutableListOf<String>()

        for (rawStep in relationSteps) {
            val step = normalizeRelationStep(rawStep)
            val candidates = allRelations.filter { rel ->
                rel.sourceEntityName.equals(current, ignoreCase = true) &&
                    normalizeRelationStep(rel.relationType) == step
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
            pathDescriptions.add("$current -> [${best.relationType}] -> ${best.targetEntityName}")
            current = best.targetEntityName
            visited.add(current)
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

    suspend fun detectAndResolveGraphQuery(
        botId: String,
        query: String
    ): GraphTraversalResult? = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext null

        val startMatch = Regex("""\b([A-ZÇĞİÖŞÜ][a-zçğıöşü]{1,22})['’](?:ın|in|un|ün|nın|nin|nun|nün)\s+(.+)""").find(trimmed)
            ?: return@withContext null
        val startEntity = startMatch.groupValues[1].trim()
        var remainder = startMatch.groupValues[2]
            .replace(Regex("""(?i)\b(kimdir|kim|nedir|ne|nerede|hangisi|var mı)\b.*$"""), "")
            .replace(Regex("""[?.!,;:]+$"""), "")
            .trim()
        if (remainder.isBlank()) return@withContext null

        // Tokenize chain while keeping multi-word relation "en yakın arkadaşı/arkadaşının" intact
        val steps = mutableListOf<String>()
        val stepRegex = Regex("""(?i)(en\s+yakın\s+arkadaş[a-zçğıöşü]+|[a-zçğıöşü]{3,})""")
        for (m in stepRegex.findAll(remainder)) {
            val token = normalizeRelationStep(m.value)
            if (token.isNotBlank() && token !in setOf("ve", "ile", "bir", "bu", "şu", "o", "mi", "mı", "mu", "mü")) {
                steps.add(token)
            }
        }
        if (steps.isEmpty()) return@withContext null
        traverseRelationGraph(botId, startEntity, steps)
    }
}
