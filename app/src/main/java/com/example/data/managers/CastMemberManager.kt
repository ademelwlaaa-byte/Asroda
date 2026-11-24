package com.example.data.managers

import com.example.data.local.BotDao
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberDao
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionDao
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionState
import com.example.data.local.PersonalityProfile
import com.example.data.repository.CastDetectorEngine
import com.example.data.repository.EmochiRepository
import com.example.data.repository.KeyCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class CastMemberManager(
    private val castMemberDao: CastMemberDao,
    private val emotionDao: CharacterEmotionDao,
    private val botDao: BotDao
) {
    private val turkishLocale = Locale.forLanguageTag("tr-TR")

    fun cleanCharacterNameCandidate(raw: String): String {
        val cleaned = raw
            .replace(Regex("""\[.*?\]"""), "")
            .replace(Regex("""\(.*?\)"""), "")
            .replace(Regex("""["'«»“”‘’*`_~]"""), "")
            .trim()
        val firstPart = cleaned.split(":", "-", "—", "|", "/").firstOrNull()?.trim() ?: cleaned
        return firstPart.take(45).trim()
    }

    fun isMainOrUserCharacter(candidateRaw: String, bot: BotEntity): Boolean {
        val clean = cleanCharacterNameCandidate(candidateRaw).lowercase(turkishLocale)
        if (clean.isBlank() || clean.length < 2) return true

        val mainNames = listOf(
            bot.aiName,
            bot.userCharName,
            bot.universeName,
            "kullanıcı",
            "user",
            "anlatıcı",
            "narrator",
            "sistem",
            "system",
            "ana karakter",
            "sen",
            "ben"
        ).map { it.trim().lowercase(turkishLocale) }.filter { it.isNotBlank() }

        return mainNames.any { main ->
            clean == main ||
                (main.length >= 3 && clean.startsWith("$main ")) ||
                (clean.length >= 3 && main.startsWith("$clean "))
        }
    }

    suspend fun ensureKeyCharacterMirroredAsCastMember(botId: String, keyChar: KeyCharacter) = withContext(Dispatchers.IO) {
        val cleanName = cleanCharacterNameCandidate(keyChar.name)
        if (cleanName.length < 2) return@withContext

        val blacklisted = castMemberDao.getBlacklistedNamesForBot(botId).map { it.lowercase(turkishLocale) }
        if (blacklisted.contains(cleanName.lowercase(turkishLocale))) return@withContext

        val existing = castMemberDao.findByName(botId, cleanName)
        val baseMult = EmochiRepository.calculateCastMemberDifficultyMultiplier(
            role = keyChar.desc.ifBlank { "Anahtar Karakter" },
            characterName = cleanName,
            cueSentence = keyChar.desc
        )
        if (existing == null) {
            val personalityJson = PersonalityProfile.deriveFromPersonality(cleanName, keyChar.desc, "").toJson()
            castMemberDao.insertCastMember(
                CastMemberEntity(
                    botId = botId,
                    name = cleanName,
                    description = keyChar.desc,
                    role = keyChar.desc.take(40).ifBlank { "Anahtar Karakter" },
                    affectionScore = 50,
                    relationshipState = "Tanıdık",
                    importanceScore = 90, // 0-100 scale: core manual cast always passes >= 90 filter
                    isAutoAdded = false,
                    isBlacklisted = false,
                    detectionConfidence = 100,
                    pendingUserConfirmation = false,
                    baseAffectionDifficultyMultiplier = baseMult,
                    personalityJson = personalityJson
                )
            )
        } else if (existing.description.isBlank() && keyChar.desc.isNotBlank()) {
            castMemberDao.updateCastMember(
                existing.copy(
                    description = keyChar.desc,
                    importanceScore = maxOf(existing.importanceScore, 90),
                    pendingUserConfirmation = false
                )
            )
        }
    }

    suspend fun getBlacklistedAliasTokensForBot(botId: String): Set<String> = withContext(Dispatchers.IO) {
        val blacklistedRows = try {
            castMemberDao.getBlacklistedCastMembersForBot(botId)
        } catch (_: Exception) {
            emptyList()
        }
        val namesFromQuery = try {
            castMemberDao.getBlacklistedNamesForBot(botId)
        } catch (_: Exception) {
            emptyList()
        }
        buildSet {
            namesFromQuery.forEach { n ->
                val norm = CastDetectorEngine.normalizeTurkishEntityName(n).lowercase(turkishLocale)
                if (norm.isNotBlank()) add(norm)
                val raw = n.trim().lowercase(turkishLocale)
                if (raw.isNotBlank()) add(raw)
            }
            blacklistedRows.forEach { row ->
                val norm = CastDetectorEngine.normalizeTurkishEntityName(row.name).lowercase(turkishLocale)
                if (norm.isNotBlank()) add(norm)
                CastDetectorEngine.extractStoredAliases(row.multiRoleJson).forEach { alias ->
                    val alNorm = CastDetectorEngine.normalizeTurkishEntityName(alias).lowercase(turkishLocale)
                    if (alNorm.isNotBlank()) add(alNorm)
                }
            }
        }
    }

    suspend fun ensureSideCharacterRegistered(
        botId: String,
        rawCharacterName: String,
        role: String = "Yan Karakter",
        initialMood: String = "nötr",
        affectionDelta: Int = 0,
        cueSentence: String = "",
        bot: BotEntity? = null,
        confidence: Int? = null,
        pendingConfirmation: Boolean? = null,
        aliases: List<String> = emptyList(),
        relation: String = "",
        age: Int? = null,
        currentMessageIndex: Int? = null
    ) = withContext(Dispatchers.IO) {
        val rawClean = cleanCharacterNameCandidate(rawCharacterName)
        val (canonicalName, titleRole) = CastDetectorEngine.extractCanonicalNameAndRole(rawClean)
        val cleanName = CastDetectorEngine.normalizeTurkishEntityName(canonicalName.ifBlank { rawClean })
        if (cleanName.length < 2) return@withContext

        val targetBot = bot ?: botDao.getBotById(botId)
        if (targetBot != null && isMainOrUserCharacter(cleanName, targetBot)) return@withContext

        val blacklistedTokens = getBlacklistedAliasTokensForBot(botId)
        val candidateCheckTokens = aliases + listOf(rawClean, cleanName)
        val isBlacklistedCandidate = candidateCheckTokens.any { token ->
            val low = CastDetectorEngine.normalizeTurkishEntityName(token).lowercase(turkishLocale)
            low.isNotBlank() && (
                blacklistedTokens.contains(low) ||
                    blacklistedTokens.any { bl -> CastDetectorEngine.isLikelyAlias(token, bl) }
                )
        }
        if (isBlacklistedCandidate) return@withContext

        // Alias-aware lookup: "Doktor Elif" <-> "Elif", stored aliases in multiRoleJson, Turkish normalized names
        val allExisting = castMemberDao.getCastMembersForBot(botId)
        val existing = castMemberDao.findByName(botId, cleanName)
            ?: allExisting.firstOrNull { member ->
                CastDetectorEngine.matchesMemberOrAlias(cleanName, aliases + rawClean, member)
            }

        val candidateScore = CastDetectorEngine.scoreCandidate(
            candidateName = rawClean,
            sceneContext = cueSentence.ifBlank { role },
            appearanceCount = (existing?.appearanceCount ?: 0) + 1,
            inferredRole = titleRole ?: role
        )
        val effectiveConfidence = confidence ?: candidateScore.confidence
        val resolvedRole = when {
            role.isNotBlank() && role != "Yan Karakter" -> role
            !titleRole.isNullOrBlank() -> titleRole
            else -> candidateScore.role
        }
        val difficultyMult = EmochiRepository.calculateCastMemberDifficultyMultiplier(
            role = resolvedRole,
            characterName = cleanName,
            cueSentence = cueSentence
        )

        val aliasesToPersist = buildList {
            addAll(aliases)
            if (rawClean.isNotBlank() && !rawClean.equals(cleanName, ignoreCase = true)) {
                add(rawClean)
            }
        }
        val enrichedDesc = buildString {
            if (relation.isNotBlank()) append(relation.trim())
            if (cueSentence.isNotBlank()) {
                if (isNotEmpty()) append(" — ")
                append(cueSentence.take(200))
            }
        }.take(240)

        if (existing == null) {
            if (confidence == null && !candidateScore.isSignificant && effectiveConfidence < 45) return@withContext
            val initialAffection = (50 + affectionDelta.coerceIn(-15, 15)).coerceIn(0, 100)
            val resolvedPending = pendingConfirmation ?: (effectiveConfidence in 50..70)
            val initialImportance = when {
                effectiveConfidence >= 85 -> 75
                effectiveConfidence >= 70 -> 65
                else -> 55
            }
            val personalityJson = PersonalityProfile.deriveFromPersonality(cleanName, resolvedRole, cueSentence).toJson()
            val multiRoleWithAliases = CastDetectorEngine.mergeStoredAliases("[]", aliasesToPersist, cleanName)
            val resolvedAge = age?.coerceIn(1, 150) ?: 20
            castMemberDao.insertCastMember(
                CastMemberEntity(
                    botId = botId,
                    name = cleanName,
                    description = enrichedDesc,
                    role = resolvedRole,
                    affectionScore = initialAffection,
                    relationshipState = initialMood.ifBlank { "Tanıdık" },
                    importanceScore = initialImportance,
                    isAutoAdded = true,
                    isBlacklisted = false,
                    currentAge = resolvedAge,
                    initialAge = resolvedAge,
                    detectionConfidence = effectiveConfidence,
                    pendingUserConfirmation = resolvedPending,
                    appearanceCount = 1,
                    lastSceneContext = cueSentence.take(200),
                    baseAffectionDifficultyMultiplier = difficultyMult,
                    secondaryRole = relation.take(40),
                    multiRoleJson = multiRoleWithAliases,
                    lastRoleEvaluationIndex = currentMessageIndex ?: 0,
                    personalityJson = personalityJson
                )
            )
        } else if (!existing.isBlacklisted) {
            val newCount = existing.appearanceCount + 1
            val updatedAffection = (existing.affectionScore + affectionDelta.coerceIn(-15, 15)).coerceIn(0, 100)
            val promotedImportance = when {
                newCount >= 6 -> maxOf(existing.importanceScore, 90)
                newCount >= 3 -> maxOf(existing.importanceScore, 80)
                else -> maxOf(existing.importanceScore, 60)
            }
            val ensuredPersonality = existing.personalityJson.ifBlank {
                PersonalityProfile.deriveFromPersonality(existing.name, resolvedRole, cueSentence).toJson()
            }
            val mergedMultiRole = CastDetectorEngine.mergeStoredAliases(
                existingMultiRoleJson = existing.multiRoleJson,
                newAliases = aliasesToPersist,
                canonicalName = existing.name
            )
            val updatedAge = if (age != null && age in 1..150 && (existing.currentAge == 20 || existing.currentAge <= 0)) {
                age
            } else {
                existing.currentAge
            }
            castMemberDao.updateCastMember(
                existing.copy(
                    role = if (existing.role.isBlank() || existing.role == "Yan Karakter") resolvedRole else existing.role,
                    description = if (existing.description.isBlank() && enrichedDesc.isNotBlank()) enrichedDesc else existing.description,
                    secondaryRole = if (existing.secondaryRole.isBlank() && relation.isNotBlank()) relation.take(40) else existing.secondaryRole,
                    currentAge = updatedAge,
                    initialAge = if (existing.initialAge == 20 && updatedAge != 20) updatedAge else existing.initialAge,
                    affectionScore = updatedAffection,
                    importanceScore = promotedImportance,
                    appearanceCount = newCount,
                    multiRoleJson = mergedMultiRole,
                    lastRoleEvaluationIndex = currentMessageIndex?.let { maxOf(existing.lastRoleEvaluationIndex, it) } ?: existing.lastRoleEvaluationIndex,
                    lastSceneContext = if (cueSentence.isNotBlank()) cueSentence.take(200) else existing.lastSceneContext,
                    personalityJson = ensuredPersonality
                )
            )
        }

        val canonicalForEmotion = existing?.name ?: cleanName
        val existingEmotion = emotionDao.getEmotionForCharacter(botId, canonicalForEmotion)
        if (existingEmotion == null) {
            val baseState = EmotionState.calculateBaselineEmotionState(
                aiName = canonicalForEmotion,
                personality = resolvedRole,
                scenario = cueSentence
            ).copy(dominantEmotion = initialMood.ifBlank { "nötr" })
            emotionDao.insertOrUpdate(
                CharacterEmotionEntity(
                    botId = botId,
                    characterName = canonicalForEmotion,
                    emotionState = baseState.toJson()
                )
            )
        }
    }

    /**
     * Applies validated Micro-AI (ExecutionMode.UTILITY) cast extraction results to Room.
     * Strictly enforces blacklist lock (even across aliases) and deduplicates/enriches existing entries.
     * Returns the set of active in-scene canonical character names extracted in this turn.
     */
    suspend fun applyMicroAiCastExtractionResult(
        botId: String,
        result: com.example.data.repository.CastExtractionResult,
        bot: BotEntity? = null,
        sceneContext: String = "",
        currentMessageIndex: Int? = null
    ): Set<String> = withContext(Dispatchers.IO) {
        val targetBot = bot ?: botDao.getBotById(botId)
        val activeInScene = mutableSetOf<String>()
        for (member in result.members) {
            if (!member.isPerson) continue
            val canonical = CastDetectorEngine.normalizeTurkishEntityName(member.name)
            if (canonical.length < 2) continue
            if (targetBot != null && isMainOrUserCharacter(canonical, targetBot)) continue
            ensureSideCharacterRegistered(
                botId = botId,
                rawCharacterName = canonical,
                role = member.role.ifBlank { "Yan Karakter" },
                cueSentence = sceneContext.take(200),
                bot = targetBot,
                confidence = if (result.usedMicroAi) 88 else null,
                pendingConfirmation = if (result.usedMicroAi) false else null,
                aliases = member.aliases,
                relation = member.relation,
                age = member.age,
                currentMessageIndex = currentMessageIndex
            )
            if (member.inScene) {
                activeInScene.add(canonical)
            }
        }
        activeInScene
    }

    suspend fun getBlacklistedCharacterNames(botId: String): Set<String> = withContext(Dispatchers.IO) {
        getBlacklistedAliasTokensForBot(botId)
    }

    suspend fun detectAndRegisterCastMembers(
        botId: String,
        aiReplyText: String,
        userQuery: String = ""
    ) = withContext(Dispatchers.IO) {
        val bot = botDao.getBotById(botId)
        val dbBlacklisted = getBlacklistedAliasTokensForBot(botId)
        val mergedBlacklist = CastDetectorEngine.getBlacklist() + dbBlacklisted
        val handledNames = mutableSetOf<String>()

        // 1) Explicit [CAST_MEMBER: Name | Role] tag (high confidence, auto-confirmed)
        val tagRegex = Regex("""\[CAST_MEMBER:\s*([^|\]]+?)(?:\s*\|\s*([^\]]+))?\]""", RegexOption.IGNORE_CASE)
        for (match in tagRegex.findAll(aiReplyText)) {
            val name = CastDetectorEngine.normalizeTurkishEntityName(cleanCharacterNameCandidate(match.groupValues[1]))
            val role = match.groupValues.getOrNull(2)?.trim().orEmpty().ifBlank { "Yan Karakter" }
            if (name.length < 2) continue
            val lower = name.lowercase(turkishLocale)
            if (dbBlacklisted.contains(lower) || dbBlacklisted.any { CastDetectorEngine.isLikelyAlias(name, it) }) continue
            if (bot != null && isMainOrUserCharacter(name, bot)) continue
            handledNames.add(lower)
            ensureSideCharacterRegistered(
                botId = botId,
                rawCharacterName = name,
                role = role,
                cueSentence = aiReplyText.take(200),
                bot = bot,
                confidence = 90,
                pendingConfirmation = false
            )
        }

        // 2) Full CastDetectorEngine Layer 1 scan (titles, kinship, full names, honorifics, intros, speech, actions)
        val detectedHits = CastDetectorEngine.detectCandidates(
            combinedText = aiReplyText,
            blacklist = mergedBlacklist
        )
        for (hit in detectedHits) {
            if (hit.signalStrength != CastDetectorEngine.SignalStrength.STRONG) continue
            val name = CastDetectorEngine.normalizeTurkishEntityName(cleanCharacterNameCandidate(hit.canonicalName))
            if (name.length < 2) continue
            val lower = name.lowercase(turkishLocale)
            if (handledNames.contains(lower)) continue
            if (dbBlacklisted.contains(lower) || dbBlacklisted.any { CastDetectorEngine.isLikelyAlias(name, it) }) continue
            if (bot != null && isMainOrUserCharacter(name, bot)) continue
            handledNames.add(lower)

            val isExplicitTag = hit.cueSnippet.startsWith("[") || hit.cueSnippet.startsWith("newChars:")
            val isBorderlineSingleHit = !isExplicitTag &&
                (hit.role == "Sahnede Konuşan Karakter" || hit.role == "Sahnede Eyleme Geçen Karakter" || hit.role == "Yan Karakter")

            ensureSideCharacterRegistered(
                botId = botId,
                rawCharacterName = name,
                role = if (hit.role == "Sahnede Konuşan Karakter" || hit.role == "Sahnede Eyleme Geçen Karakter") "Yan Karakter" else hit.role,
                cueSentence = hit.cueSnippet.ifBlank { aiReplyText.take(200) },
                bot = bot,
                confidence = if (isBorderlineSingleHit) 65 else null,
                pendingConfirmation = if (isBorderlineSingleHit) true else null
            )
        }
    }

    suspend fun reEvaluateCastMemberRolesIfNeeded(
        botId: String,
        currentMessageCount: Int,
        sceneContext: String = ""
    ) = withContext(Dispatchers.IO) {
        if (sceneContext.isBlank()) return@withContext
        val members = castMemberDao.getConfirmedCastMembersForBot(botId)
        val lowerScene = sceneContext.lowercase(turkishLocale)
        val hasMajorBetrayalEvent = lowerScene.contains("ihanet") ||
            lowerScene.contains("düşman saf") ||
            lowerScene.contains("arkadan bıçakla") ||
            lowerScene.contains("sattı")

        for (member in members) {
            val isPeriodicDue = currentMessageCount > 0 &&
                (currentMessageCount - member.lastRoleEvaluationIndex >= 30 ||
                    (currentMessageCount % 30 == 0 && currentMessageCount > member.lastRoleEvaluationIndex))
            // Word-boundary mention check prevents "Ali" matching "Alışveriş"
            val mentionsMember = CastDetectorEngine.isNameMentionedInText(member.name, sceneContext)
            if (!isPeriodicDue && !(hasMajorBetrayalEvent && mentionsMember)) continue

            // Major story event (betrayal / enemy turn): ONLY flip characters actually mentioned in the betrayal scene!
            if (hasMajorBetrayalEvent && mentionsMember) {
                castMemberDao.updateCastMember(
                    member.copy(
                        role = "Rakip / Düşman",
                        baseAffectionDifficultyMultiplier = 0.5,
                        lastRoleEvaluationIndex = currentMessageCount,
                        lastSceneContext = sceneContext.take(200)
                    )
                )
                continue
            }

            // Periodic 30-message relationship evolution (e.g., Yabancı with affection >= 80 -> Yakın Dost, 1.3)
            if (isPeriodicDue && member.role.equals("Yabancı", ignoreCase = true) && member.affectionScore >= 80) {
                castMemberDao.updateCastMember(
                    member.copy(
                        role = "Yakın Dost",
                        baseAffectionDifficultyMultiplier = 1.3,
                        lastRoleEvaluationIndex = currentMessageCount,
                        lastSceneContext = sceneContext.take(200)
                    )
                )
                continue
            }

            if (!mentionsMember) continue

            val (_, inferredRole, _, newMult) = EmochiRepository.evaluateCharacterSignificance(
                candidateName = member.name,
                sceneContext = sceneContext
            )
            val updatedSecondary = if (inferredRole != member.role && inferredRole != "Yan Karakter") {
                inferredRole
            } else {
                member.secondaryRole
            }
            val rolesSet = linkedSetOf<String>()
            if (member.role.isNotBlank()) rolesSet.add(member.role)
            if (updatedSecondary.isNotBlank()) rolesSet.add(updatedSecondary)
            val multiArr = JSONArray()
            rolesSet.forEach { multiArr.put(it) }

            castMemberDao.updateCastMember(
                member.copy(
                    secondaryRole = updatedSecondary,
                    multiRoleJson = multiArr.toString(),
                    lastRoleEvaluationIndex = currentMessageCount,
                    lastSceneContext = sceneContext.take(200),
                    baseAffectionDifficultyMultiplier = if (member.baseAffectionDifficultyMultiplier > 0.0) {
                        member.baseAffectionDifficultyMultiplier
                    } else {
                        newMult
                    }
                )
            )
        }
    }

    suspend fun getCastMembersForBot(botId: String): List<CastMemberEntity> = withContext(Dispatchers.IO) {
        castMemberDao.getCastMembersForBot(botId).filter { !it.isBlacklisted }
    }

    suspend fun getFirstPendingCastMember(botId: String): CastMemberEntity? = withContext(Dispatchers.IO) {
        castMemberDao.getPendingCastMembersForBot(botId).firstOrNull { !it.isBlacklisted }
    }

    suspend fun confirmPendingCastMember(botId: String, castMemberId: Long) = withContext(Dispatchers.IO) {
        val member = castMemberDao.getCastMembersForBot(botId).firstOrNull { it.id == castMemberId } ?: return@withContext
        castMemberDao.updateCastMember(
            member.copy(
                pendingUserConfirmation = false,
                isBlacklisted = false,
                detectionConfidence = maxOf(member.detectionConfidence, 90)
            )
        )
    }

    suspend fun dismissPendingCastMember(botId: String, castMemberId: Long) = withContext(Dispatchers.IO) {
        val member = castMemberDao.getCastMembersForBot(botId).firstOrNull { it.id == castMemberId } ?: return@withContext
        castMemberDao.updateCastMember(member.copy(pendingUserConfirmation = false))
    }

    suspend fun blacklistCastMember(botId: String, castMemberId: Long) = withContext(Dispatchers.IO) {
        val member = castMemberDao.getCastMembersForBot(botId).firstOrNull { it.id == castMemberId }
        castMemberDao.blacklistCastMember(castMemberId)
        if (member != null) {
            emotionDao.deleteByCharacterName(botId, member.name)
        }
    }

    suspend fun deleteAutoAddedCastMemberById(botId: String, castMemberId: Long) = withContext(Dispatchers.IO) {
        val member = castMemberDao.getCastMembersForBot(botId).firstOrNull { it.id == castMemberId }
        castMemberDao.deleteCastMember(castMemberId)
        if (member != null) {
            emotionDao.deleteByCharacterName(botId, member.name)
        }
    }

    suspend fun deleteCharacterAndBlacklist(botId: String, characterName: String) = withContext(Dispatchers.IO) {
        val cleanName = cleanCharacterNameCandidate(characterName)
        val existing = castMemberDao.findByName(botId, cleanName) ?: castMemberDao.findByName(botId, characterName)
        if (existing != null) {
            castMemberDao.updateCastMember(
                existing.copy(
                    isBlacklisted = true,
                    pendingUserConfirmation = false
                )
            )
        } else if (cleanName.isNotBlank()) {
            castMemberDao.insertCastMember(
                CastMemberEntity(
                    botId = botId,
                    name = cleanName,
                    description = "",
                    isAutoAdded = true,
                    isBlacklisted = true,
                    pendingUserConfirmation = false
                )
            )
        }
        emotionDao.deleteByCharacterName(botId, characterName)
        if (cleanName != characterName) {
            emotionDao.deleteByCharacterName(botId, cleanName)
        }
    }

    /**
     * Exact month/day-aware birthday age calculation (matches TimePerceptionEngine).
     * Falls back to year-only difference only when full yyyy-MM-dd is unavailable.
     */
    suspend fun updateCastMembersAgeForBot(botId: String, currentStoryCalendarDateStr: String) = withContext(Dispatchers.IO) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }
        val currentFullDate = try {
            val iso = currentStoryCalendarDateStr.trim().take(10)
            if (iso.length == 10) sdf.parse(iso) else null
        } catch (_: Exception) {
            null
        }
        val currentYear = Regex("""\b(19\d{2}|20\d{2}|21\d{2})\b""").find(currentStoryCalendarDateStr)?.value?.toIntOrNull()
            ?: return@withContext

        val members = castMemberDao.getCastMembersForBot(botId)
        for (member in members) {
            if (member.birthDate.isBlank()) continue
            val birthFullDate = try {
                val iso = member.birthDate.trim().take(10)
                if (iso.length == 10) sdf.parse(iso) else null
            } catch (_: Exception) {
                null
            }
            val computedAge: Int? = if (currentFullDate != null && birthFullDate != null) {
                val calCur = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = currentFullDate }
                val calBirth = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = birthFullDate }
                var age = calCur.get(Calendar.YEAR) - calBirth.get(Calendar.YEAR)
                val curMd = calCur.get(Calendar.MONTH) * 100 + calCur.get(Calendar.DAY_OF_MONTH)
                val birthMd = calBirth.get(Calendar.MONTH) * 100 + calBirth.get(Calendar.DAY_OF_MONTH)
                if (curMd < birthMd) age--
                age.coerceIn(1, 150)
            } else {
                val birthYear = Regex("""\b(19\d{2}|20\d{2}|21\d{2})\b""").find(member.birthDate)?.value?.toIntOrNull()
                if (birthYear != null && currentYear >= birthYear) (currentYear - birthYear).coerceIn(1, 150) else null
            }
            if (computedAge != null && computedAge != member.currentAge) {
                castMemberDao.updateCastMember(member.copy(currentAge = computedAge))
            }
        }
    }
}
