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
import kotlinx.coroutines.CancellationException
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
    companion object {
        private val TURKISH_LOCALE: Locale = Locale.forLanguageTag("tr-TR")
        private val BRACKET_REGEX = Regex("""\[.*?\]""")
        private val PAREN_REGEX = Regex("""\(.*?\)""")
        private val QUOTE_PUNCT_REGEX = Regex("""["'«»“”‘’*`_~]""")
        private val WHITESPACE_SPLIT_REGEX = Regex("""\s+""")
        private val CAST_TAG_REGEX = Regex("""\[CAST_MEMBER:\s*([^|\]]+?)(?:\s*\|\s*([^\]]+))?\]""", RegexOption.IGNORE_CASE)
        private val YEAR_REGEX = Regex("""\b(19\d{2}|20\d{2}|21\d{2})\b""")

        /**
         * Canonical Turkish-locale comparison key (safely folds İ/i and I/ı and stray combining dots).
         */
        fun turkishNameKey(raw: String): String {
            return raw.trim()
                .lowercase(TURKISH_LOCALE)
                .replace('\u0307', ' ')
                .replace(WHITESPACE_SPLIT_REGEX, " ")
                .trim()
        }

        fun sanitizeAffectionScore(score: Int, delta: Int = 0): Int =
            (score + delta.coerceIn(-15, 15)).coerceIn(0, 100)

        /**
         * Pure UTC month/day-aware birthday age calculator (falls back to 4-digit year difference).
         * Returns null when inputs are invalid or birthDate is after currentDate.
         */
        fun computeExactAge(birthDateStr: String, currentDateStr: String): Int? {
            if (birthDateStr.isBlank() || currentDateStr.isBlank()) return null
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            val currentFullDate = try {
                val iso = currentDateStr.trim().take(10)
                if (iso.length == 10) sdf.parse(iso) else null
            } catch (_: Exception) {
                null
            }
            val birthFullDate = try {
                val iso = birthDateStr.trim().take(10)
                if (iso.length == 10) sdf.parse(iso) else null
            } catch (_: Exception) {
                null
            }
            if (currentFullDate != null && birthFullDate != null) {
                if (birthFullDate.after(currentFullDate)) return null
                val calCur = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = currentFullDate }
                val calBirth = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = birthFullDate }
                var age = calCur.get(Calendar.YEAR) - calBirth.get(Calendar.YEAR)
                val curMd = calCur.get(Calendar.MONTH) * 100 + calCur.get(Calendar.DAY_OF_MONTH)
                val birthMd = calBirth.get(Calendar.MONTH) * 100 + calBirth.get(Calendar.DAY_OF_MONTH)
                if (curMd < birthMd) age--
                return if (age >= 0) age.coerceIn(1, 150) else null
            }
            val currentYear = YEAR_REGEX.find(currentDateStr)?.value?.toIntOrNull() ?: return null
            val birthYear = YEAR_REGEX.find(birthDateStr)?.value?.toIntOrNull() ?: return null
            return if (currentYear >= birthYear) (currentYear - birthYear).coerceIn(1, 150) else null
        }
    }

    private val turkishLocale: Locale get() = TURKISH_LOCALE

    fun cleanCharacterNameCandidate(raw: String): String {
        val cleaned = raw
            .replace(BRACKET_REGEX, "")
            .replace(PAREN_REGEX, "")
            .replace(QUOTE_PUNCT_REGEX, "")
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

        val blacklistedTokens = getBlacklistedAliasTokensForBot(botId)
        val cleanKey = turkishNameKey(CastDetectorEngine.normalizeTurkishEntityName(cleanName))
        if (blacklistedTokens.contains(cleanKey) ||
            blacklistedTokens.contains(turkishNameKey(cleanName)) ||
            blacklistedTokens.any { bl -> CastDetectorEngine.isLikelyAlias(cleanName, bl) }
        ) {
            return@withContext
        }

        val allExisting = castMemberDao.getCastMembersForBot(botId)
        val existing = castMemberDao.findByName(botId, cleanName)
            ?: allExisting.firstOrNull { member ->
                turkishNameKey(member.name) == turkishNameKey(cleanName) ||
                    CastDetectorEngine.matchesMemberOrAlias(cleanName, listOf(keyChar.name), member)
            }
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
        } else if (!existing.isBlacklisted && existing.description.isBlank() && keyChar.desc.isNotBlank()) {
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
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            emptyList()
        }
        val namesFromQuery = try {
            castMemberDao.getBlacklistedNamesForBot(botId)
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            emptyList()
        }
        buildSet {
            namesFromQuery.forEach { n ->
                val norm = turkishNameKey(CastDetectorEngine.normalizeTurkishEntityName(n))
                if (norm.isNotBlank()) add(norm)
                val raw = turkishNameKey(n)
                if (raw.isNotBlank()) add(raw)
            }
            blacklistedRows.forEach { row ->
                val norm = turkishNameKey(CastDetectorEngine.normalizeTurkishEntityName(row.name))
                if (norm.isNotBlank()) add(norm)
                CastDetectorEngine.extractStoredAliases(row.multiRoleJson).forEach { alias ->
                    val alNorm = turkishNameKey(CastDetectorEngine.normalizeTurkishEntityName(alias))
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
        // Hard gate: never register unnamed/generic background extras OR places/organizations/English non-names
        if (CastDetectorEngine.isGenericBackgroundExtra(rawClean, role) ||
            CastDetectorEngine.isGenericBackgroundExtra(cleanName, titleRole ?: role) ||
            CastDetectorEngine.isNonPersonPlaceOrOrganization(rawClean) ||
            CastDetectorEngine.isNonPersonPlaceOrOrganization(cleanName)
        ) {
            return@withContext
        }

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

        // Alias-aware lookup: "Doktor Elif" <-> "Elif", Turkish İ/ı case fold, stored aliases in multiRoleJson
        val allExisting = castMemberDao.getCastMembersForBot(botId)
        val cleanKey = turkishNameKey(cleanName)
        val existing = castMemberDao.findByName(botId, cleanName)
            ?: allExisting.firstOrNull { member ->
                turkishNameKey(member.name) == cleanKey ||
                    CastDetectorEngine.matchesMemberOrAlias(cleanName, aliases + rawClean, member)
            }

        val candidateScore = CastDetectorEngine.scoreCandidate(
            candidateName = rawClean,
            sceneContext = cueSentence.ifBlank { role },
            appearanceCount = (existing?.appearanceCount ?: 0) + 1,
            inferredRole = titleRole ?: role
        )
        val effectiveConfidence = confidence ?: candidateScore.confidence
        val normalizedRoleInput = CastDetectorEngine.normalizeTurkishCastRole(role)
        val normalizedRelation = CastDetectorEngine.normalizeTurkishCastRelation(relation)
        val resolvedRole = when {
            normalizedRoleInput.isNotBlank() && normalizedRoleInput != "Yan Karakter" -> normalizedRoleInput
            !titleRole.isNullOrBlank() -> CastDetectorEngine.normalizeTurkishCastRole(titleRole)
            else -> CastDetectorEngine.normalizeTurkishCastRole(candidateScore.role)
        }
        val difficultyMult = EmochiRepository.calculateCastMemberDifficultyMultiplier(
            role = resolvedRole,
            characterName = cleanName,
            cueSentence = cueSentence
        )

        val aliasesToPersist = buildList {
            addAll(aliases)
            if (rawClean.isNotBlank() && turkishNameKey(rawClean) != cleanKey) {
                add(rawClean)
            }
        }
        val enrichedDesc = buildString {
            if (normalizedRelation.isNotBlank()) append(normalizedRelation.trim())
            if (cueSentence.isNotBlank()) {
                if (isNotEmpty()) append(" — ")
                append(cueSentence.take(200))
            }
        }.take(240)

        var canonicalForEmotion = cleanName
        if (existing == null) {
            if (confidence == null && !candidateScore.isSignificant && effectiveConfidence < 45) return@withContext
            val initialAffection = sanitizeAffectionScore(50, affectionDelta)
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
                    secondaryRole = normalizedRelation.take(40),
                    multiRoleJson = multiRoleWithAliases,
                    lastRoleEvaluationIndex = currentMessageIndex ?: 0,
                    personalityJson = personalityJson
                )
            )
        } else if (!existing.isBlacklisted) {
            val newCount = existing.appearanceCount + 1
            val updatedAffection = sanitizeAffectionScore(existing.affectionScore, affectionDelta)
            val promotedImportance = when {
                newCount >= 6 -> maxOf(existing.importanceScore, 90)
                newCount >= 3 -> maxOf(existing.importanceScore, 80)
                else -> maxOf(existing.importanceScore, 60)
            }.coerceIn(0, 100)
            // Canonical full-name promotion: if an auto-added card was "Natasha" and now "Natasha Romanoff"
            // appears as a strict superset alias, promote the canonical name & keep "Natasha" as alias!
            val existingTokens = existing.name.trim().split(WHITESPACE_SPLIT_REGEX).filter { it.isNotBlank() }
            val newTokens = cleanName.trim().split(WHITESPACE_SPLIT_REGEX).filter { it.isNotBlank() }
            val shouldPromoteFullName = existing.isAutoAdded &&
                newTokens.size in 2..3 &&
                newTokens.size > existingTokens.size &&
                CastDetectorEngine.isLikelyAlias(existing.name, cleanName) &&
                allExisting.none { it.id != existing.id && turkishNameKey(it.name) == cleanKey }
            val finalCanonicalName = if (shouldPromoteFullName) cleanName else existing.name
            canonicalForEmotion = finalCanonicalName
            val combinedAliases = buildList {
                addAll(aliasesToPersist)
                if (shouldPromoteFullName) add(existing.name)
            }
            val ensuredPersonality = existing.personalityJson.ifBlank {
                PersonalityProfile.deriveFromPersonality(finalCanonicalName, resolvedRole, cueSentence).toJson()
            }
            val mergedMultiRole = CastDetectorEngine.mergeStoredAliases(
                existingMultiRoleJson = existing.multiRoleJson,
                newAliases = combinedAliases,
                canonicalName = finalCanonicalName
            )
            val updatedAge = if (age != null && age in 1..150 && (existing.currentAge == 20 || existing.currentAge <= 0)) {
                age
            } else {
                existing.currentAge
            }
            val autoConfirmedOnRecurrence = existing.isAutoAdded && existing.pendingUserConfirmation && newCount >= 2
            val currentRoleClean = CastDetectorEngine.normalizeTurkishCastRole(existing.role)
            castMemberDao.updateCastMember(
                existing.copy(
                    name = finalCanonicalName,
                    role = if (currentRoleClean.isBlank() || currentRoleClean == "Yan Karakter") resolvedRole else currentRoleClean,
                    description = if (existing.description.isBlank() && enrichedDesc.isNotBlank()) enrichedDesc else existing.description,
                    secondaryRole = if (existing.secondaryRole.isBlank() && normalizedRelation.isNotBlank()) normalizedRelation.take(40) else existing.secondaryRole,
                    currentAge = updatedAge,
                    initialAge = if (existing.initialAge == 20 && updatedAge != 20) updatedAge else existing.initialAge,
                    affectionScore = updatedAffection,
                    importanceScore = promotedImportance,
                    appearanceCount = newCount,
                    pendingUserConfirmation = if (autoConfirmedOnRecurrence) false else existing.pendingUserConfirmation,
                    multiRoleJson = mergedMultiRole,
                    lastRoleEvaluationIndex = currentMessageIndex?.let { maxOf(existing.lastRoleEvaluationIndex, it) } ?: existing.lastRoleEvaluationIndex,
                    lastSceneContext = if (cueSentence.isNotBlank()) cueSentence.take(200) else existing.lastSceneContext,
                    personalityJson = ensuredPersonality
                )
            )
            // Keep CharacterEmotionEntity synchronized if canonical name was promoted
            if (shouldPromoteFullName && finalCanonicalName != existing.name) {
                val oldEmo = emotionDao.getEmotionForCharacter(botId, existing.name)
                if (oldEmo != null && emotionDao.getEmotionForCharacter(botId, finalCanonicalName) == null) {
                    emotionDao.insertOrUpdate(oldEmo.copy(id = 0L, characterName = finalCanonicalName))
                    emotionDao.deleteByCharacterName(botId, existing.name)
                }
            }
        } else {
            return@withContext
        }

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
        for (match in CAST_TAG_REGEX.findAll(aiReplyText)) {
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
            if (isPeriodicDue && turkishNameKey(member.role) == turkishNameKey("Yabancı") && member.affectionScore >= 80) {
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
            val rolesOnlyArr = JSONArray()
            rolesSet.forEach { rolesOnlyArr.put(it) }
            // Preserve all previously stored alias:... entries inside multiRoleJson!
            val preservedAliases = CastDetectorEngine.extractStoredAliases(member.multiRoleJson)
            val mergedMultiRoleJson = CastDetectorEngine.mergeStoredAliases(
                existingMultiRoleJson = rolesOnlyArr.toString(),
                newAliases = preservedAliases,
                canonicalName = member.name
            )

            castMemberDao.updateCastMember(
                member.copy(
                    secondaryRole = updatedSecondary,
                    multiRoleJson = mergedMultiRoleJson,
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
            CastDetectorEngine.extractStoredAliases(member.multiRoleJson).forEach { alias ->
                if (alias.isNotBlank()) emotionDao.deleteByCharacterName(botId, alias)
            }
        }
    }

    suspend fun deleteAutoAddedCastMemberById(botId: String, castMemberId: Long) = withContext(Dispatchers.IO) {
        val member = castMemberDao.getCastMembersForBot(botId).firstOrNull { it.id == castMemberId }
        castMemberDao.deleteCastMember(castMemberId)
        if (member != null) {
            emotionDao.deleteByCharacterName(botId, member.name)
            CastDetectorEngine.extractStoredAliases(member.multiRoleJson).forEach { alias ->
                if (alias.isNotBlank()) emotionDao.deleteByCharacterName(botId, alias)
            }
        }
    }

    suspend fun deleteCharacterAndBlacklist(botId: String, characterName: String) = withContext(Dispatchers.IO) {
        val cleanName = cleanCharacterNameCandidate(characterName)
        if (cleanName.isBlank() && characterName.isBlank()) return@withContext
        val cleanKey = turkishNameKey(cleanName)
        val existing = castMemberDao.findByName(botId, cleanName)
            ?: castMemberDao.findByName(botId, characterName)
            ?: castMemberDao.getCastMembersForBot(botId).firstOrNull { member ->
                (cleanKey.isNotBlank() && turkishNameKey(member.name) == cleanKey) ||
                    (cleanName.length >= 2 && CastDetectorEngine.matchesMemberOrAlias(cleanName, listOf(characterName), member))
            }
        if (existing != null) {
            castMemberDao.updateCastMember(
                existing.copy(
                    isBlacklisted = true,
                    pendingUserConfirmation = false
                )
            )
            emotionDao.deleteByCharacterName(botId, existing.name)
            CastDetectorEngine.extractStoredAliases(existing.multiRoleJson).forEach { alias ->
                if (alias.isNotBlank()) emotionDao.deleteByCharacterName(botId, alias)
            }
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
        if (characterName.isNotBlank()) {
            emotionDao.deleteByCharacterName(botId, characterName)
        }
        if (cleanName.isNotBlank() && cleanName != characterName) {
            emotionDao.deleteByCharacterName(botId, cleanName)
        }
    }

    /**
     * Exact month/day-aware birthday age calculation (matches TimePerceptionEngine).
     * Falls back to year-only difference only when full yyyy-MM-dd is unavailable.
     */
    suspend fun updateCastMembersAgeForBot(botId: String, currentStoryCalendarDateStr: String) = withContext(Dispatchers.IO) {
        if (currentStoryCalendarDateStr.isBlank()) return@withContext
        val members = castMemberDao.getCastMembersForBot(botId)
        for (member in members) {
            if (member.birthDate.isBlank()) continue
            val computedAge = computeExactAge(member.birthDate, currentStoryCalendarDateStr)
            if (computedAge != null && computedAge != member.currentAge) {
                castMemberDao.updateCastMember(member.copy(currentAge = computedAge))
            }
        }
    }

    data class CastCleanupReport(
        val removedFalsePositives: Int = 0,
        val mergedDuplicates: Int = 0,
        val normalizedTurkishRoles: Int = 0
    )

    /**
     * Audits and self-heals a bot's auto-added cast roster:
     * 1. Removes auto-added false positives (generic extras, places, buildings, organizations, English non-names, corrupt names).
     * 2. Merges duplicate alias & Turkish case-colliding cards (e.g. "Natasha" + "Natasha Romanoff", "İrem" + "irem").
     * 3. Normalizes any leaked English role/secondaryRole labels into crisp Turkish and clamps scores.
     */
    suspend fun auditAndCleanCastForBot(botId: String): CastCleanupReport = withContext(Dispatchers.IO) {
        val all = castMemberDao.getCastMembersForBot(botId).filter { !it.isBlacklisted }
        if (all.isEmpty()) return@withContext CastCleanupReport()

        var removed = 0
        var merged = 0
        var normalizedRoles = 0

        val survivors = mutableListOf<CastMemberEntity>()
        // Prioritize manual cards & fuller names & higher appearance counts
        val ordered = all.sortedWith(
            compareBy<CastMemberEntity> { it.isAutoAdded }
                .thenByDescending { it.name.trim().split(WHITESPACE_SPLIT_REGEX).size }
                .thenByDescending { it.appearanceCount }
                .thenByDescending { it.importanceScore }
        )

        for (member in ordered) {
            val cleanedName = cleanCharacterNameCandidate(member.name)
            if (member.isAutoAdded && (
                cleanedName.length < 2 ||
                    cleanedName.count { it.isLetter() } < 2 ||
                    CastDetectorEngine.isGenericBackgroundExtra(member.name, member.role) ||
                    CastDetectorEngine.isNonPersonPlaceOrOrganization(member.name)
                )
            ) {
                castMemberDao.deleteCastMember(member.id)
                emotionDao.deleteByCharacterName(botId, member.name)
                removed++
                continue
            }

            val memberKey = turkishNameKey(member.name)
            val existingMatchIdx = survivors.indexOfFirst { kept ->
                turkishNameKey(kept.name) == memberKey ||
                    CastDetectorEngine.matchesMemberOrAlias(
                        candidateName = member.name,
                        candidateAliases = CastDetectorEngine.extractStoredAliases(member.multiRoleJson),
                        member = kept
                    )
            }
            if (existingMatchIdx >= 0 && member.isAutoAdded) {
                val kept = survivors[existingMatchIdx]
                val mergedAliases = CastDetectorEngine.mergeStoredAliases(
                    existingMultiRoleJson = kept.multiRoleJson,
                    newAliases = CastDetectorEngine.extractStoredAliases(member.multiRoleJson) + member.name,
                    canonicalName = kept.name
                )
                val upgradedKept = kept.copy(
                    appearanceCount = kept.appearanceCount + member.appearanceCount,
                    importanceScore = maxOf(kept.importanceScore, member.importanceScore).coerceIn(0, 100),
                    affectionScore = kept.affectionScore.coerceIn(0, 100),
                    description = kept.description.ifBlank { member.description },
                    secondaryRole = kept.secondaryRole.ifBlank { member.secondaryRole },
                    multiRoleJson = mergedAliases
                )
                castMemberDao.updateCastMember(upgradedKept)
                castMemberDao.deleteCastMember(member.id)
                emotionDao.deleteByCharacterName(botId, member.name)
                survivors[existingMatchIdx] = upgradedKept
                merged++
                continue
            }

            val cleanRole = CastDetectorEngine.normalizeTurkishCastRole(member.role)
            val cleanSecondary = CastDetectorEngine.normalizeTurkishCastRelation(member.secondaryRole)
            val clampedAffection = member.affectionScore.coerceIn(0, 100)
            val clampedImportance = member.importanceScore.coerceIn(0, 100)
            if (cleanRole != member.role ||
                cleanSecondary != member.secondaryRole ||
                clampedAffection != member.affectionScore ||
                clampedImportance != member.importanceScore
            ) {
                val updated = member.copy(
                    role = cleanRole,
                    secondaryRole = cleanSecondary,
                    affectionScore = clampedAffection,
                    importanceScore = clampedImportance
                )
                castMemberDao.updateCastMember(updated)
                survivors.add(updated)
                if (cleanRole != member.role || cleanSecondary != member.secondaryRole) {
                    normalizedRoles++
                }
            } else {
                survivors.add(member)
            }
        }

        CastCleanupReport(
            removedFalsePositives = removed,
            mergedDuplicates = merged,
            normalizedTurkishRoles = normalizedRoles
        )
    }
}
