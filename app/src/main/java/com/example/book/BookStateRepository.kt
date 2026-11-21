package com.example.book

import android.content.Context
import com.example.data.local.BotEntity
import com.example.ui.screens.ChapterProgressManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Central registry for the built-in interactive novel "Kozmik Sürükleniş"
 * (all 7 playable chapters, extended literary scenes, deep cross-chapter flags).
 */
object CosmicDriftBookRegistry {
    const val BOOK_ID = "preset_aiden_mcu_cosmic"
    const val TOTAL_PLAYABLE_CHAPTERS = 7

    val allNodes: Map<String, BookSceneNode> by lazy {
        buildMap {
            putAll(CosmicDriftChapters1To3.nodes)
            putAll(CosmicDriftChapters4To5.nodes)
            putAll(CosmicDriftChapters6To7.nodes)
        }
    }

    val chapters: List<BookChapterMeta> by lazy {
        val raw = CosmicDriftChapters1To3.chaptersMeta +
            CosmicDriftChapters4To5.chaptersMeta +
            CosmicDriftChapters6To7.chaptersMeta
        raw.map { meta ->
            val chapterNodes = allNodes.values.filter { it.chapterNumber == meta.number }
            val maxScene = chapterNodes.maxOfOrNull { it.sceneOrder } ?: meta.sceneCount
            val distinctDecisionOrders = chapterNodes
                .filter { it.choices.isNotEmpty() }
                .map { it.sceneOrder }
                .distinct()
                .size
            meta.copy(
                sceneCount = maxScene,
                decisionCount = distinctDecisionOrders
            )
        }
    }

    val totalChoiceBranches: Int by lazy {
        allNodes.values.sumOf { it.choices.size }
    }

    val totalEstimatedMinutes: Int by lazy {
        chapters.sumOf { it.estimatedMinutes }
    }

    fun chapterMeta(chapterNumber: Int): BookChapterMeta =
        chapters.firstOrNull { it.number == chapterNumber } ?: chapters.first()

    fun node(nodeId: String): BookSceneNode? = allNodes[nodeId]

    /**
     * Resilient detector: matches both the canonical preset ID and any previously
     * imported Room copy of "Kozmik Sürükleniş" (even if an older build gave it a UUID).
     */
    fun supportsDeterministicEngine(bot: BotEntity): Boolean =
        bot.id == BOOK_ID ||
            bot.aiName.contains("Kozmik Sürükleniş", ignoreCase = true) ||
            bot.aiName.contains("Cosmic Drift", ignoreCase = true) ||
            bot.universeName.contains("Kozmik Sürükleniş", ignoreCase = true) ||
            bot.pinnedMemory.contains("KOZMİK SÜRÜKLENİŞ", ignoreCase = true)

    fun supportsDeterministicEngine(botId: String): Boolean =
        botId == BOOK_ID

    data class ResolvedSceneText(
        val paragraphs: List<String>,
        val memoryEchoes: List<Pair<String, String>>
    )

    fun resolveSceneContent(
        node: BookSceneNode,
        flags: Map<String, String>,
        fatigue: Int,
        redEyeInstability: Int,
        avengersTrust: Int,
        bonds: Map<BookCharacterId, Int>,
        secrets: Set<String>
    ): ResolvedSceneText {
        val out = node.paragraphs.toMutableList()
        val echoes = mutableListOf<Pair<String, String>>()
        node.conditionalNarratives.forEach { cond ->
            val flagOk = when {
                cond.requiredFlagKey == null -> true
                cond.requiredAnyFlagValues != null -> flags[cond.requiredFlagKey] in cond.requiredAnyFlagValues
                else -> flags[cond.requiredFlagKey] == cond.requiredFlagValue
            }
            val secretOk = cond.requiredSecret == null || cond.requiredSecret in secrets
            val fatigueOk = cond.minFatigue == null || fatigue >= cond.minFatigue
            val redEyeOk = cond.minRedEye == null || redEyeInstability >= cond.minRedEye
            val trustOk = cond.minAvengersTrust == null || avengersTrust >= cond.minAvengersTrust
            val bondOk = cond.minBondCharacter == null ||
                (bonds[cond.minBondCharacter] ?: cond.minBondCharacter.initialBond) >= (cond.minBondValue ?: 0)
            if (flagOk && secretOk && fatigueOk && redEyeOk && trustOk && bondOk) {
                if (cond.callbackLabel != null && cond.paragraphs.isNotEmpty()) {
                    echoes.add(cond.callbackLabel to cond.paragraphs.joinToString("\n\n"))
                } else {
                    out.addAll(cond.paragraphs)
                }
            }
        }
        return ResolvedSceneText(out, echoes)
    }
}

/**
 * Instant, persistent, reactive state repository for the Interactive Book Engine.
 * Supports exact scroll position save/restore, deep cross-chapter flags,
 * zero-latency choice transitions, and clean chapter/book resets.
 */
object BookStateRepository {
    private const val PREFS_NAME = "aetheria_book_engine_state_v4"
    private val flows = mutableMapOf<String, MutableStateFlow<BookReadState>>()

    private fun defaultBonds(): Map<BookCharacterId, Int> =
        BookCharacterId.entries.associateWith { it.initialBond }

    @Synchronized
    fun stateFlow(context: Context, bookId: String = CosmicDriftBookRegistry.BOOK_ID): StateFlow<BookReadState> {
        val existing = flows[bookId]
        if (existing != null) return existing.asStateFlow()
        val loaded = loadFromPrefs(context.applicationContext, bookId)
            ?: createInitialState(bookId, chapterNumber = 1)
        val flow = MutableStateFlow(loaded)
        flows[bookId] = flow
        return flow.asStateFlow()
    }

    fun getState(context: Context, bookId: String = CosmicDriftBookRegistry.BOOK_ID): BookReadState =
        stateFlow(context, bookId).value

    /**
     * Persists exact scroll index & pixel offset so exiting mid-chapter resumes
     * at the exact paragraph where the reader left off.
     */
    @Synchronized
    fun saveScrollPosition(
        context: Context,
        firstVisibleIndex: Int,
        firstVisibleOffset: Int,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ) {
        val appCtx = context.applicationContext
        val current = getState(appCtx, bookId)
        val safeIndex = firstVisibleIndex.coerceAtLeast(0)
        val safeOffset = firstVisibleOffset.coerceAtLeast(0)
        if (current.savedScrollIndex == safeIndex && current.savedScrollOffset == safeOffset) return
        val updated = current.copy(
            savedScrollIndex = safeIndex,
            savedScrollOffset = safeOffset
        )
        saveToPrefs(appCtx, bookId, updated)
        flows[bookId]?.value = updated
    }

    /**
     * Scales legacy/raw deltas down to grounded, realistic increments (+1..+3 standard,
     * +4 for major turns, +5 ONLY for truly massive watershed climax events).
     */
    private fun normalizeDelta(raw: Int, isWatershed: Boolean): Int {
        if (raw == 0) return 0
        val sign = if (raw > 0) 1 else -1
        val absVal = kotlin.math.abs(raw)
        val scaled = when {
            absVal <= 3 -> absVal
            absVal <= 5 -> 2
            absVal <= 8 -> 3
            absVal <= 12 -> if (isWatershed) 4 else 3
            else -> if (isWatershed) 5 else 4
        }
        return sign * scaled
    }

    /**
     * Resolves a human-readable Turkish label and accent color for any stored numericFlag key.
     */
    fun describeFlagKey(key: String): Pair<String, Long> {
        if (key.startsWith("char:")) {
            val parts = key.split(":")
            if (parts.size == 3) {
                val charId = BookCharacterId.fromKey(parts[1])
                val axis = CharacterFlagAxis.fromKey(parts[2])
                if (charId != null && axis != null) {
                    val colorHex = if (axis.isNegativeTone) axis.accentHex else charId.accentHex
                    return "${charId.possessiveName} ${axis.turkishLabel}" to colorHex
                }
            }
        } else if (key.startsWith("story:")) {
            val axisKey = key.removePrefix("story:")
            val axis = StoryFlagAxis.fromKey(axisKey)
            if (axis != null) {
                return axis.turkishLabel to axis.accentHex
            }
        }
        return key to 0xFFC4B5FDL
    }

    private fun inferCharacterAxis(
        chapterNumber: Int,
        charId: BookCharacterId,
        rawDelta: Int,
        choice: BookChoiceDef
    ): Pair<CharacterFlagAxis, Int> {
        val effect = choice.effect
        val isWatershed = effect.impactLevel == "kritik" && (effect.isBranching || chapterNumber >= 6)
        val scaled = normalizeDelta(rawDelta, isWatershed)
        val hintLower = (choice.toneHint + " " + effect.impactSummary).lowercase()

        if (scaled < 0) {
            return when {
                effect.redEyeDelta > 0 && chapterNumber >= 2 -> CharacterFlagAxis.FEAR to kotlin.math.abs(scaled)
                charId == BookCharacterId.STEVE || charId == BookCharacterId.NATASHA || charId == BookCharacterId.TONY ->
                    if (hintLower.contains("kuşku") || hintLower.contains("temkin") || hintLower.contains("sessiz")) {
                        CharacterFlagAxis.SUSPICION to kotlin.math.abs(scaled)
                    } else {
                        CharacterFlagAxis.TRUST to scaled
                    }
                else -> CharacterFlagAxis.TRUST to scaled
            }
        }

        val axis = when (charId) {
            BookCharacterId.STEVE -> when {
                chapterNumber >= 5 && (hintLower.contains("omuz") || hintLower.contains("koru") || hintLower.contains("sadakat") || isWatershed) ->
                    CharacterFlagAxis.LOYALTY
                hintLower.contains("saygı") || hintLower.contains("disiplin") || hintLower.contains("dövüş") ->
                    CharacterFlagAxis.RESPECT
                else -> CharacterFlagAxis.TRUST
            }
            BookCharacterId.NATASHA -> when {
                chapterNumber >= 4 && (effect.isRelationship && (hintLower.contains("bağ") || hintLower.contains("yakın") || hintLower.contains("kalp") || rawDelta >= 10)) ->
                    CharacterFlagAxis.AFFECTION
                chapterNumber >= 3 && (hintLower.contains("ortak") || hintLower.contains("sır") || hintLower.contains("güven")) ->
                    CharacterFlagAxis.TRUST
                hintLower.contains("yakın") || hintLower.contains("anlayış") ->
                    CharacterFlagAxis.WARMTH
                else -> CharacterFlagAxis.RESPECT
            }
            BookCharacterId.WANDA -> when {
                chapterNumber >= 4 && (effect.isRelationship && (hintLower.contains("bağ") || hintLower.contains("kalp") || hintLower.contains("yakın") || rawDelta >= 10)) ->
                    CharacterFlagAxis.AFFECTION
                chapterNumber >= 3 && hintLower.contains("güven") ->
                    CharacterFlagAxis.TRUST
                else -> CharacterFlagAxis.EMPATHY
            }
            BookCharacterId.TONY -> when {
                chapterNumber >= 4 && (hintLower.contains("güven") || hintLower.contains("sadakat")) ->
                    CharacterFlagAxis.TRUST
                hintLower.contains("saygı") || hintLower.contains("mizah") || hintLower.contains("sempati") ->
                    CharacterFlagAxis.RESPECT
                else -> CharacterFlagAxis.CURIOSITY
            }
            BookCharacterId.CLINT -> when {
                chapterNumber >= 4 && (isWatershed || hintLower.contains("omuz") || hintLower.contains("koru")) ->
                    CharacterFlagAxis.LOYALTY
                hintLower.contains("güven") ->
                    CharacterFlagAxis.TRUST
                else -> CharacterFlagAxis.WARMTH
            }
            BookCharacterId.BRUCE -> when {
                hintLower.contains("merak") || hintLower.contains("bilim") || hintLower.contains("veri") ->
                    CharacterFlagAxis.CURIOSITY
                else -> CharacterFlagAxis.EMPATHY
            }
            BookCharacterId.THOR -> when {
                chapterNumber >= 5 && isWatershed -> CharacterFlagAxis.LOYALTY
                hintLower.contains("güven") -> CharacterFlagAxis.TRUST
                else -> CharacterFlagAxis.RESPECT
            }
        }
        return axis to scaled
    }

    private fun resolveFlagChangesForChoice(
        chapterNumber: Int,
        choice: BookChoiceDef,
        existingNumericFlags: Map<String, Int>
    ): List<FlagChangeEntry> {
        val effect = choice.effect
        val isWatershed = effect.impactLevel == "kritik" && (effect.isBranching || chapterNumber >= 6)
        val entries = mutableListOf<FlagChangeEntry>()
        val seenKeys = mutableSetOf<String>()

        fun appendEntry(
            key: String,
            label: String,
            delta: Int,
            accentHex: Long,
            charId: BookCharacterId? = null
        ) {
            if (delta == 0 || !seenKeys.add(key)) return
            entries.add(
                FlagChangeEntry(
                    key = key,
                    label = label,
                    delta = delta,
                    accentHex = accentHex,
                    characterId = charId,
                    isNewlyUnlocked = !existingNumericFlags.containsKey(key)
                )
            )
        }

        // 1) Explicit per-character multi-axis flags if authored on the choice
        if (effect.characterAxisDeltas.isNotEmpty()) {
            effect.characterAxisDeltas.forEach { (charId, axis, delta) ->
                val key = "char:${charId.key}:${axis.key}"
                val colorHex = if (axis.isNegativeTone) axis.accentHex else charId.accentHex
                appendEntry(
                    key = key,
                    label = "${charId.possessiveName} ${axis.turkishLabel}",
                    delta = delta,
                    accentHex = colorHex,
                    charId = charId
                )
            }
        } else {
            // Auto-infer nuanced multi-axis character flags from bondDeltas with balanced small values
            effect.bondDeltas.forEach { (charId, rawDelta) ->
                val (axis, balancedDelta) = inferCharacterAxis(chapterNumber, charId, rawDelta, choice)
                val key = "char:${charId.key}:${axis.key}"
                val colorHex = if (axis.isNegativeTone) axis.accentHex else charId.accentHex
                appendEntry(
                    key = key,
                    label = "${charId.possessiveName} ${axis.turkishLabel}",
                    delta = balancedDelta,
                    accentHex = colorHex,
                    charId = charId
                )
            }
        }

        // 2) Explicit story/world/psychological flags if authored on the choice
        if (effect.storyAxisDeltas.isNotEmpty()) {
            effect.storyAxisDeltas.forEach { (storyAxis, delta) ->
                val key = "story:${storyAxis.key}"
                appendEntry(
                    key = key,
                    label = storyAxis.turkishLabel,
                    delta = delta,
                    accentHex = storyAxis.accentHex
                )
            }
        } else {
            // Derive rich progressive world/psychological flags for chapters 2–7
            val trustScaled = normalizeDelta(effect.avengersTrustDelta, isWatershed)
            if (trustScaled != 0) {
                appendEntry(
                    key = "story:${StoryFlagAxis.AVENGERS_TRUST.key}",
                    label = StoryFlagAxis.AVENGERS_TRUST.turkishLabel,
                    delta = trustScaled,
                    accentHex = StoryFlagAxis.AVENGERS_TRUST.accentHex
                )
            }
            if (effect.redEyeDelta != 0) {
                appendEntry(
                    key = "story:${StoryFlagAxis.RED_EYE_INSTABILITY.key}",
                    label = StoryFlagAxis.RED_EYE_INSTABILITY.turkishLabel,
                    delta = effect.redEyeDelta.coerceIn(-2, 2),
                    accentHex = StoryFlagAxis.RED_EYE_INSTABILITY.accentHex
                )
                if (chapterNumber >= 4 && effect.redEyeDelta > 0) {
                    appendEntry(
                        key = "story:${StoryFlagAxis.DARK_TEMPTATION.key}",
                        label = StoryFlagAxis.DARK_TEMPTATION.turkishLabel,
                        delta = 1,
                        accentHex = StoryFlagAxis.DARK_TEMPTATION.accentHex
                    )
                }
            }
            if (effect.fatigueDelta != 0) {
                appendEntry(
                    key = "story:${StoryFlagAxis.MENTAL_FATIGUE.key}",
                    label = StoryFlagAxis.MENTAL_FATIGUE.turkishLabel,
                    delta = effect.fatigueDelta.coerceIn(-2, 2),
                    accentHex = StoryFlagAxis.MENTAL_FATIGUE.accentHex
                )
            }
            if (effect.mysteryDelta != 0) {
                appendEntry(
                    key = "story:${StoryFlagAxis.MYSTERY_AURA.key}",
                    label = StoryFlagAxis.MYSTERY_AURA.turkishLabel,
                    delta = effect.mysteryDelta.coerceIn(-2, 2),
                    accentHex = StoryFlagAxis.MYSTERY_AURA.accentHex
                )
            }
            // Progressive chapter-unlocked story flags based on choice semantics
            val summaryLower = (choice.toneHint + " " + effect.impactSummary).lowercase()
            if (chapterNumber >= 2 && (summaryLower.contains("kule") || summaryLower.contains("şart") || summaryLower.contains("kal"))) {
                appendEntry(
                    key = "story:${StoryFlagAxis.TOWER_BELONGING.key}",
                    label = StoryFlagAxis.TOWER_BELONGING.turkishLabel,
                    delta = if (trustScaled >= 0) 1 else -1,
                    accentHex = StoryFlagAxis.TOWER_BELONGING.accentHex
                )
            }
            if (chapterNumber >= 2 && (summaryLower.contains("kozmik") || summaryLower.contains("yarık") || summaryLower.contains("enerji"))) {
                appendEntry(
                    key = "story:${StoryFlagAxis.COSMIC_AWAKENING.key}",
                    label = StoryFlagAxis.COSMIC_AWAKENING.turkishLabel,
                    delta = 1,
                    accentHex = StoryFlagAxis.COSMIC_AWAKENING.accentHex
                )
            }
            if (chapterNumber >= 3 && (summaryLower.contains("s.h.i.e.l.d") || summaryLower.contains("alarm") || summaryLower.contains("fury"))) {
                appendEntry(
                    key = "story:${StoryFlagAxis.SHIELD_SUSPICION.key}",
                    label = StoryFlagAxis.SHIELD_SUSPICION.turkishLabel,
                    delta = if (trustScaled >= 0) -1 else 2,
                    accentHex = StoryFlagAxis.SHIELD_SUSPICION.accentHex
                )
            }
            if (chapterNumber >= 3 && (summaryLower.contains("dövüş") || summaryLower.contains("saha") || summaryLower.contains("operasyon") || summaryLower.contains("antrenman"))) {
                appendEntry(
                    key = "story:${StoryFlagAxis.FIELD_INITIATIVE.key}",
                    label = StoryFlagAxis.FIELD_INITIATIVE.turkishLabel,
                    delta = 2,
                    accentHex = StoryFlagAxis.FIELD_INITIATIVE.accentHex
                )
            }
            if (chapterNumber >= 3 && (summaryLower.contains("gölge") || summaryLower.contains("sır") || summaryLower.contains("istihbarat"))) {
                appendEntry(
                    key = "story:${StoryFlagAxis.SHADOW_ALLIANCE.key}",
                    label = StoryFlagAxis.SHADOW_ALLIANCE.turkishLabel,
                    delta = 1,
                    accentHex = StoryFlagAxis.SHADOW_ALLIANCE.accentHex
                )
            }
            if (chapterNumber >= 4 && (summaryLower.contains("açığa") || summaryLower.contains("kamu") || summaryLower.contains("basın") || summaryLower.contains("sokak"))) {
                appendEntry(
                    key = "story:${StoryFlagAxis.PUBLIC_EXPOSURE.key}",
                    label = StoryFlagAxis.PUBLIC_EXPOSURE.turkishLabel,
                    delta = 1,
                    accentHex = StoryFlagAxis.PUBLIC_EXPOSURE.accentHex
                )
            }
        }

        return entries
    }

    /**
     * Applies a player choice instantaneously (<1ms), updates multi-axis character & world flags,
     * balanced bonds, fatigue, mystery, Red Eye instability, Avengers trust, secrets, flags,
     * and persists state immediately.
     */
    @Synchronized
    fun selectChoice(
        context: Context,
        choiceId: String,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ): BookReadState {
        val appCtx = context.applicationContext
        val current = getState(appCtx, bookId)
        val currentNode = CosmicDriftBookRegistry.node(current.currentNodeId) ?: return current
        val choice = currentNode.choices.firstOrNull { it.id == choiceId } ?: return current
        val nextNode = CosmicDriftBookRegistry.node(choice.nextNodeId) ?: return current

        val effect = choice.effect
        val isWatershed = effect.impactLevel == "kritik" && (effect.isBranching || current.activeChapter >= 6)

        // Resolve exact balanced multi-axis flag deltas for this choice
        val resolvedFlagChanges = resolveFlagChangesForChoice(
            chapterNumber = current.activeChapter,
            choice = choice,
            existingNumericFlags = current.numericFlags
        )

        val updatedNumericFlags = current.numericFlags.toMutableMap().apply {
            resolvedFlagChanges.forEach { change ->
                val prev = this[change.key] ?: 0
                this[change.key] = (prev + change.delta).coerceIn(-25, 25)
            }
        }

        // Balanced bond deltas (keep overall character bond score realistic too)
        val normalizedBondDeltas = buildMap {
            effect.bondDeltas.forEach { (charId, raw) ->
                put(charId, normalizeDelta(raw, isWatershed))
            }
        }
        val updatedBonds = current.bonds.toMutableMap().apply {
            normalizedBondDeltas.forEach { (charId, delta) ->
                val base = this[charId] ?: charId.initialBond
                this[charId] = (base + delta).coerceIn(0, 100)
            }
        }

        val normalizedTrustDelta = normalizeDelta(effect.avengersTrustDelta, isWatershed)
        val updatedFatigue = (current.fatigue + effect.fatigueDelta.coerceIn(-2, 2)).coerceIn(0, 5)
        val updatedMystery = (current.mystery + effect.mysteryDelta.coerceIn(-2, 2)).coerceIn(0, 10)
        val updatedRedEye = (current.redEyeInstability + effect.redEyeDelta.coerceIn(-2, 2)).coerceIn(0, 5)
        val updatedTrust = (current.avengersTrust + normalizedTrustDelta).coerceIn(0, 100)
        val updatedFlags = current.flags.toMutableMap().apply {
            putAll(effect.setFlags)
        }
        val updatedSecrets = current.discoveredSecrets.toMutableSet().apply {
            effect.secretUnlocked?.let { add(it) }
        }
        val updatedBranches = current.discoveredBranches.toMutableSet().apply {
            effect.branchTag?.let { add(it) }
        }

        val recorded = RecordedDecision(
            chapterNumber = current.activeChapter,
            nodeId = currentNode.id,
            decisionTitle = currentNode.decisionTitle ?: currentNode.sceneTitle,
            choiceId = choice.id,
            selectedOption = choice.label,
            impactText = effect.impactSummary,
            bondDeltas = normalizedBondDeltas,
            flagChanges = resolvedFlagChanges,
            fatigueDelta = effect.fatigueDelta.coerceIn(-2, 2),
            mysteryDelta = effect.mysteryDelta.coerceIn(-2, 2),
            redEyeDelta = effect.redEyeDelta.coerceIn(-2, 2),
            avengersTrustDelta = normalizedTrustDelta,
            secretUnlocked = effect.secretUnlocked,
            branchTag = effect.branchTag,
            isBranching = effect.isBranching,
            isRelationship = effect.isRelationship,
            impactLevel = effect.impactLevel
        )

        val resolved = CosmicDriftBookRegistry.resolveSceneContent(
            node = nextNode,
            flags = updatedFlags,
            fatigue = updatedFatigue,
            redEyeInstability = updatedRedEye,
            avengersTrust = updatedTrust,
            bonds = updatedBonds,
            secrets = updatedSecrets
        )
        val nextSceneBlock = BookTranscriptItem.SceneBlock(
            nodeId = nextNode.id,
            sceneOrder = nextNode.sceneOrder,
            sceneTitle = nextNode.sceneTitle,
            locationTag = nextNode.locationTag,
            paragraphs = resolved.paragraphs,
            memoryEchoes = resolved.memoryEchoes,
            isChapterEnd = nextNode.isChapterEnd,
            chapterEndQuote = nextNode.chapterEndQuote
        )

        val updatedTranscript = current.transcript +
            BookTranscriptItem.DecisionBlock(recorded) +
            nextSceneBlock

        val updatedChapterDecisions = current.chapterDecisions.toMutableMap().apply {
            val prevList = this[current.activeChapter].orEmpty()
            this[current.activeChapter] = prevList + recorded
        }

        val updatedCompleted = current.completedChapters.toMutableSet().apply {
            if (nextNode.isChapterEnd) add(current.activeChapter)
        }

        val newState = current.copy(
            completedChapters = updatedCompleted,
            currentNodeId = nextNode.id,
            transcript = updatedTranscript,
            chapterDecisions = updatedChapterDecisions,
            bonds = updatedBonds,
            fatigue = updatedFatigue,
            mystery = updatedMystery,
            redEyeInstability = updatedRedEye,
            avengersTrust = updatedTrust,
            flags = updatedFlags,
            numericFlags = updatedNumericFlags,
            discoveredSecrets = updatedSecrets,
            discoveredBranches = updatedBranches,
            isChapterCompleted = nextNode.isChapterEnd
        )

        persistAndEmit(appCtx, bookId, newState)
        return newState
    }

    /**
     * Jumps directly to a specific chapter (1..7), carrying forward earned bonds,
     * flags, trust, and secrets, and resetting scroll to the very top of the new chapter.
     */
    @Synchronized
    fun jumpToChapter(
        context: Context,
        chapterNumber: Int,
        forceRestartIfSame: Boolean = false,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ): BookReadState {
        val appCtx = context.applicationContext
        val clamped = chapterNumber.coerceIn(1, CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS)
        val current = getState(appCtx, bookId)

        if (current.activeChapter == clamped && !forceRestartIfSame && !current.isChapterCompleted) {
            return current
        }

        val chapterMeta = CosmicDriftBookRegistry.chapterMeta(clamped)
        val startNode = CosmicDriftBookRegistry.node(chapterMeta.startNodeId)
            ?: return current

        val snapshot = ChapterSnapshot(
            bonds = current.bonds,
            fatigue = (current.fatigue - 1).coerceAtLeast(0),
            mystery = current.mystery,
            redEyeInstability = current.redEyeInstability,
            avengersTrust = current.avengersTrust,
            flags = current.flags,
            numericFlags = current.numericFlags,
            discoveredSecrets = current.discoveredSecrets,
            discoveredBranches = current.discoveredBranches
        )

        val resolved = CosmicDriftBookRegistry.resolveSceneContent(
            node = startNode,
            flags = snapshot.flags,
            fatigue = snapshot.fatigue,
            redEyeInstability = snapshot.redEyeInstability,
            avengersTrust = snapshot.avengersTrust,
            bonds = snapshot.bonds,
            secrets = snapshot.discoveredSecrets
        )
        val openingBlock = BookTranscriptItem.SceneBlock(
            nodeId = startNode.id,
            sceneOrder = startNode.sceneOrder,
            sceneTitle = startNode.sceneTitle,
            locationTag = startNode.locationTag,
            paragraphs = resolved.paragraphs,
            memoryEchoes = resolved.memoryEchoes,
            isChapterEnd = startNode.isChapterEnd,
            chapterEndQuote = startNode.chapterEndQuote
        )

        val updatedDecisions = current.chapterDecisions.toMutableMap().apply {
            this[clamped] = emptyList()
        }

        val newState = current.copy(
            activeChapter = clamped,
            currentNodeId = startNode.id,
            transcript = listOf(openingBlock),
            chapterDecisions = updatedDecisions,
            bonds = snapshot.bonds,
            fatigue = snapshot.fatigue,
            mystery = snapshot.mystery,
            redEyeInstability = snapshot.redEyeInstability,
            avengersTrust = snapshot.avengersTrust,
            flags = snapshot.flags,
            numericFlags = snapshot.numericFlags,
            discoveredSecrets = snapshot.discoveredSecrets,
            discoveredBranches = snapshot.discoveredBranches,
            isChapterCompleted = false,
            savedScrollIndex = 0,
            savedScrollOffset = 0,
            chapterStartSnapshot = snapshot
        )

        persistAndEmit(appCtx, bookId, newState)
        return newState
    }

    /**
     * Restarts ONLY the currently active chapter from its very first line,
     * restoring pre-chapter snapshot cleanly so replaying never farms stats.
     */
    @Synchronized
    fun restartCurrentChapter(
        context: Context,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ): BookReadState {
        val appCtx = context.applicationContext
        val current = getState(appCtx, bookId)
        val snap = current.chapterStartSnapshot ?: ChapterSnapshot(
            bonds = defaultBonds(),
            fatigue = 0,
            mystery = 0,
            redEyeInstability = 0,
            avengersTrust = 35,
            flags = emptyMap(),
            numericFlags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet()
        )
        val chapterMeta = CosmicDriftBookRegistry.chapterMeta(current.activeChapter)
        val startNode = CosmicDriftBookRegistry.node(chapterMeta.startNodeId) ?: return current

        val resolved = CosmicDriftBookRegistry.resolveSceneContent(
            node = startNode,
            flags = snap.flags,
            fatigue = snap.fatigue,
            redEyeInstability = snap.redEyeInstability,
            avengersTrust = snap.avengersTrust,
            bonds = snap.bonds,
            secrets = snap.discoveredSecrets
        )
        val openingBlock = BookTranscriptItem.SceneBlock(
            nodeId = startNode.id,
            sceneOrder = startNode.sceneOrder,
            sceneTitle = startNode.sceneTitle,
            locationTag = startNode.locationTag,
            paragraphs = resolved.paragraphs,
            memoryEchoes = resolved.memoryEchoes,
            isChapterEnd = startNode.isChapterEnd,
            chapterEndQuote = startNode.chapterEndQuote
        )

        val updatedDecisions = current.chapterDecisions.toMutableMap().apply {
            this[current.activeChapter] = emptyList()
        }

        val newState = current.copy(
            currentNodeId = startNode.id,
            transcript = listOf(openingBlock),
            chapterDecisions = updatedDecisions,
            bonds = snap.bonds,
            fatigue = snap.fatigue,
            mystery = snap.mystery,
            redEyeInstability = snap.redEyeInstability,
            avengersTrust = snap.avengersTrust,
            flags = snap.flags,
            numericFlags = snap.numericFlags,
            discoveredSecrets = snap.discoveredSecrets,
            discoveredBranches = snap.discoveredBranches,
            isChapterCompleted = false,
            savedScrollIndex = 0,
            savedScrollOffset = 0,
            chapterStartSnapshot = snap
        )

        persistAndEmit(appCtx, bookId, newState)
        return newState
    }

    /**
     * Full fresh start from Chapter 1, Scene 1, Scroll 0.
     */
    @Synchronized
    fun resetEntireBook(
        context: Context,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ): BookReadState {
        val appCtx = context.applicationContext
        val fresh = createInitialState(bookId, chapterNumber = 1)
        ChapterProgressManager.resetProgress(appCtx, bookId)
        persistAndEmit(appCtx, bookId, fresh)
        return fresh
    }

    private fun createInitialState(bookId: String, chapterNumber: Int): BookReadState {
        val meta = CosmicDriftBookRegistry.chapterMeta(chapterNumber)
        val startNode = CosmicDriftBookRegistry.node(meta.startNodeId)
            ?: CosmicDriftBookRegistry.allNodes.values.first()
        val initialBonds = defaultBonds()
        val snap = ChapterSnapshot(
            bonds = initialBonds,
            fatigue = 0,
            mystery = 0,
            redEyeInstability = 0,
            avengersTrust = 35,
            flags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet()
        )
        val resolved = CosmicDriftBookRegistry.resolveSceneContent(
            node = startNode,
            flags = emptyMap(),
            fatigue = 0,
            redEyeInstability = 0,
            avengersTrust = 35,
            bonds = initialBonds,
            secrets = emptySet()
        )
        return BookReadState(
            bookId = bookId,
            activeChapter = chapterNumber,
            completedChapters = emptySet(),
            currentNodeId = startNode.id,
            transcript = listOf(
                BookTranscriptItem.SceneBlock(
                    nodeId = startNode.id,
                    sceneOrder = startNode.sceneOrder,
                    sceneTitle = startNode.sceneTitle,
                    locationTag = startNode.locationTag,
                    paragraphs = resolved.paragraphs,
                    memoryEchoes = resolved.memoryEchoes,
                    isChapterEnd = startNode.isChapterEnd,
                    chapterEndQuote = startNode.chapterEndQuote
                )
            ),
            chapterDecisions = emptyMap(),
            bonds = initialBonds,
            fatigue = 0,
            mystery = 0,
            redEyeInstability = 0,
            avengersTrust = 35,
            flags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet(),
            isChapterCompleted = false,
            savedScrollIndex = 0,
            savedScrollOffset = 0,
            chapterStartSnapshot = snap
        )
    }

    private fun persistAndEmit(context: Context, bookId: String, state: BookReadState) {
        saveToPrefs(context, bookId, state)
        ChapterProgressManager.setExactCompletedChapter(context, bookId, state.highestCompletedChapter)
        val flow = flows[bookId]
        if (flow != null) {
            flow.value = state
        } else {
            flows[bookId] = MutableStateFlow(state)
        }
    }

    // ── JSON Serialization ──────────────────────────────────────────────────
    private fun saveToPrefs(context: Context, bookId: String, state: BookReadState) {
        try {
            val root = JSONObject().apply {
                put("bookId", state.bookId)
                put("activeChapter", state.activeChapter)
                put("currentNodeId", state.currentNodeId)
                put("fatigue", state.fatigue)
                put("mystery", state.mystery)
                put("redEyeInstability", state.redEyeInstability)
                put("avengersTrust", state.avengersTrust)
                put("isChapterCompleted", state.isChapterCompleted)
                put("savedScrollIndex", state.savedScrollIndex)
                put("savedScrollOffset", state.savedScrollOffset)
                put("completedChapters", JSONArray().apply { state.completedChapters.sorted().forEach { put(it) } })
                put("bonds", encodeBonds(state.bonds))
                put("flags", encodeStringMap(state.flags))
                put("numericFlags", encodeIntMap(state.numericFlags))
                put("secrets", JSONArray().apply { state.discoveredSecrets.forEach { put(it) } })
                put("branches", JSONArray().apply { state.discoveredBranches.forEach { put(it) } })
                put("transcript", JSONArray().apply {
                    state.transcript.forEach { item ->
                        when (item) {
                            is BookTranscriptItem.SceneBlock -> put(JSONObject().apply {
                                put("type", "scene")
                                put("nodeId", item.nodeId)
                                put("sceneOrder", item.sceneOrder)
                                put("sceneTitle", item.sceneTitle)
                                put("locationTag", item.locationTag)
                                put("isChapterEnd", item.isChapterEnd)
                                if (item.chapterEndQuote != null) put("chapterEndQuote", item.chapterEndQuote)
                                put("paragraphs", JSONArray().apply { item.paragraphs.forEach { put(it) } })
                                put("memoryEchoes", JSONArray().apply {
                                    item.memoryEchoes.forEach { (lbl, body) ->
                                        put(JSONObject().apply {
                                            put("label", lbl)
                                            put("body", body)
                                        })
                                    }
                                })
                            })
                            is BookTranscriptItem.DecisionBlock -> put(JSONObject().apply {
                                put("type", "decision")
                                put("decision", encodeDecision(item.decision))
                            })
                        }
                    }
                })
                put("chapterDecisions", JSONObject().apply {
                    state.chapterDecisions.forEach { (ch, list) ->
                        put(ch.toString(), JSONArray().apply { list.forEach { put(encodeDecision(it)) } })
                    }
                })
                state.chapterStartSnapshot?.let { snap ->
                    put("snapshot", JSONObject().apply {
                        put("bonds", encodeBonds(snap.bonds))
                        put("fatigue", snap.fatigue)
                        put("mystery", snap.mystery)
                        put("redEyeInstability", snap.redEyeInstability)
                        put("avengersTrust", snap.avengersTrust)
                        put("flags", encodeStringMap(snap.flags))
                        put("numericFlags", encodeIntMap(snap.numericFlags))
                        put("secrets", JSONArray().apply { snap.discoveredSecrets.forEach { put(it) } })
                        put("branches", JSONArray().apply { snap.discoveredBranches.forEach { put(it) } })
                    })
                }
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("state_$bookId", root.toString())
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("BookStateRepository", "Failed saving book state", e)
        }
    }

    private fun loadFromPrefs(context: Context, bookId: String): BookReadState? {
        return try {
            val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString("state_$bookId", null) ?: return null
            val root = JSONObject(raw)
            val currentNodeId = root.optString("currentNodeId", "ch1_s1")
            if (CosmicDriftBookRegistry.node(currentNodeId) == null) return null

            val completed = mutableSetOf<Int>()
            root.optJSONArray("completedChapters")?.let { arr ->
                for (i in 0 until arr.length()) completed.add(arr.optInt(i))
            }
            val secrets = mutableSetOf<String>()
            root.optJSONArray("secrets")?.let { arr ->
                for (i in 0 until arr.length()) secrets.add(arr.optString(i))
            }
            val branches = mutableSetOf<String>()
            root.optJSONArray("branches")?.let { arr ->
                for (i in 0 until arr.length()) branches.add(arr.optString(i))
            }

            val transcript = mutableListOf<BookTranscriptItem>()
            root.optJSONArray("transcript")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    if (obj.optString("type") == "scene") {
                        val paras = mutableListOf<String>()
                        obj.optJSONArray("paragraphs")?.let { pArr ->
                            for (j in 0 until pArr.length()) paras.add(pArr.optString(j))
                        }
                        val echoes = mutableListOf<Pair<String, String>>()
                        obj.optJSONArray("memoryEchoes")?.let { eArr ->
                            for (j in 0 until eArr.length()) {
                                eArr.optJSONObject(j)?.let { eo ->
                                    echoes.add(eo.optString("label") to eo.optString("body"))
                                }
                            }
                        }
                        transcript.add(
                            BookTranscriptItem.SceneBlock(
                                nodeId = obj.optString("nodeId"),
                                sceneOrder = obj.optInt("sceneOrder", 1),
                                sceneTitle = obj.optString("sceneTitle"),
                                locationTag = obj.optString("locationTag"),
                                paragraphs = paras,
                                memoryEchoes = echoes,
                                isChapterEnd = obj.optBoolean("isChapterEnd", false),
                                chapterEndQuote = if (obj.has("chapterEndQuote")) obj.optString("chapterEndQuote") else null
                            )
                        )
                    } else if (obj.optString("type") == "decision") {
                        obj.optJSONObject("decision")?.let { dObj ->
                            transcript.add(BookTranscriptItem.DecisionBlock(decodeDecision(dObj)))
                        }
                    }
                }
            }
            if (transcript.isEmpty()) return null

            val chapterDecisions = mutableMapOf<Int, List<RecordedDecision>>()
            root.optJSONObject("chapterDecisions")?.let { cdObj ->
                val keys = cdObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val chNum = k.toIntOrNull() ?: continue
                    val list = mutableListOf<RecordedDecision>()
                    cdObj.optJSONArray(k)?.let { dArr ->
                        for (i in 0 until dArr.length()) {
                            dArr.optJSONObject(i)?.let { list.add(decodeDecision(it)) }
                        }
                    }
                    chapterDecisions[chNum] = list
                }
            }

            val snapObj = root.optJSONObject("snapshot")
            val snapshot = snapObj?.let { s ->
                val sSecrets = mutableSetOf<String>()
                s.optJSONArray("secrets")?.let { arr ->
                    for (i in 0 until arr.length()) sSecrets.add(arr.optString(i))
                }
                val sBranches = mutableSetOf<String>()
                s.optJSONArray("branches")?.let { arr ->
                    for (i in 0 until arr.length()) sBranches.add(arr.optString(i))
                }
                ChapterSnapshot(
                    bonds = decodeBonds(s.optJSONObject("bonds")),
                    fatigue = s.optInt("fatigue", 0),
                    mystery = s.optInt("mystery", 0),
                    redEyeInstability = s.optInt("redEyeInstability", 0),
                    avengersTrust = s.optInt("avengersTrust", 35),
                    flags = decodeStringMap(s.optJSONObject("flags")),
                    numericFlags = decodeIntMap(s.optJSONObject("numericFlags")),
                    discoveredSecrets = sSecrets,
                    discoveredBranches = sBranches
                )
            }

            BookReadState(
                bookId = root.optString("bookId", bookId),
                activeChapter = root.optInt("activeChapter", 1).coerceIn(1, CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS),
                completedChapters = completed,
                currentNodeId = currentNodeId,
                transcript = transcript,
                chapterDecisions = chapterDecisions,
                bonds = decodeBonds(root.optJSONObject("bonds")),
                fatigue = root.optInt("fatigue", 0).coerceIn(0, 5),
                mystery = root.optInt("mystery", 0).coerceIn(0, 10),
                redEyeInstability = root.optInt("redEyeInstability", 0).coerceIn(0, 5),
                avengersTrust = root.optInt("avengersTrust", 35).coerceIn(0, 100),
                flags = decodeStringMap(root.optJSONObject("flags")),
                numericFlags = decodeIntMap(root.optJSONObject("numericFlags")),
                discoveredSecrets = secrets,
                discoveredBranches = branches,
                isChapterCompleted = root.optBoolean("isChapterCompleted", false),
                savedScrollIndex = root.optInt("savedScrollIndex", 0).coerceAtLeast(0),
                savedScrollOffset = root.optInt("savedScrollOffset", 0).coerceAtLeast(0),
                chapterStartSnapshot = snapshot
            )
        } catch (e: Exception) {
            android.util.Log.e("BookStateRepository", "Failed loading book state", e)
            null
        }
    }

    private fun encodeBonds(bonds: Map<BookCharacterId, Int>): JSONObject =
        JSONObject().apply { bonds.forEach { (k, v) -> put(k.key, v) } }

    private fun decodeBonds(obj: JSONObject?): Map<BookCharacterId, Int> {
        val base = defaultBonds().toMutableMap()
        if (obj == null) return base
        BookCharacterId.entries.forEach { c ->
            if (obj.has(c.key)) base[c] = obj.optInt(c.key, c.initialBond).coerceIn(0, 100)
        }
        return base
    }

    private fun encodeStringMap(map: Map<String, String>): JSONObject =
        JSONObject().apply { map.forEach { (k, v) -> put(k, v) } }

    private fun decodeStringMap(obj: JSONObject?): Map<String, String> {
        if (obj == null) return emptyMap()
        val out = mutableMapOf<String, String>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            out[k] = obj.optString(k)
        }
        return out
    }

    private fun encodeIntMap(map: Map<String, Int>): JSONObject =
        JSONObject().apply { map.forEach { (k, v) -> put(k, v) } }

    private fun decodeIntMap(obj: JSONObject?): Map<String, Int> {
        if (obj == null) return emptyMap()
        val out = mutableMapOf<String, Int>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            out[k] = obj.optInt(k, 0)
        }
        return out
    }

    private fun encodeDecision(d: RecordedDecision): JSONObject = JSONObject().apply {
        put("chapterNumber", d.chapterNumber)
        put("nodeId", d.nodeId)
        put("decisionTitle", d.decisionTitle)
        put("choiceId", d.choiceId)
        put("selectedOption", d.selectedOption)
        put("impactText", d.impactText)
        put("bondDeltas", encodeBonds(d.bondDeltas))
        put("flagChanges", JSONArray().apply {
            d.flagChanges.forEach { fc ->
                put(JSONObject().apply {
                    put("key", fc.key)
                    put("label", fc.label)
                    put("delta", fc.delta)
                    put("accentHex", fc.accentHex)
                    if (fc.characterId != null) put("characterKey", fc.characterId.key)
                    put("isNewlyUnlocked", fc.isNewlyUnlocked)
                })
            }
        })
        put("fatigueDelta", d.fatigueDelta)
        put("mysteryDelta", d.mysteryDelta)
        put("redEyeDelta", d.redEyeDelta)
        put("avengersTrustDelta", d.avengersTrustDelta)
        if (d.secretUnlocked != null) put("secretUnlocked", d.secretUnlocked)
        if (d.branchTag != null) put("branchTag", d.branchTag)
        put("isBranching", d.isBranching)
        put("isRelationship", d.isRelationship)
        put("impactLevel", d.impactLevel)
    }

    private fun decodeDecision(obj: JSONObject): RecordedDecision {
        val deltas = mutableMapOf<BookCharacterId, Int>()
        obj.optJSONObject("bondDeltas")?.let { bObj ->
            val keys = bObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                BookCharacterId.fromKey(k)?.let { deltas[it] = bObj.optInt(k, 0) }
            }
        }
        val flagChanges = mutableListOf<FlagChangeEntry>()
        obj.optJSONArray("flagChanges")?.let { fcArr ->
            for (i in 0 until fcArr.length()) {
                fcArr.optJSONObject(i)?.let { fObj ->
                    flagChanges.add(
                        FlagChangeEntry(
                            key = fObj.optString("key"),
                            label = fObj.optString("label"),
                            delta = fObj.optInt("delta", 0),
                            accentHex = fObj.optLong("accentHex", 0xFFC4B5FDL),
                            characterId = if (fObj.has("characterKey")) {
                                BookCharacterId.fromKey(fObj.optString("characterKey"))
                            } else null,
                            isNewlyUnlocked = fObj.optBoolean("isNewlyUnlocked", false)
                        )
                    )
                }
            }
        }
        return RecordedDecision(
            chapterNumber = obj.optInt("chapterNumber", 1),
            nodeId = obj.optString("nodeId"),
            decisionTitle = obj.optString("decisionTitle"),
            choiceId = obj.optString("choiceId"),
            selectedOption = obj.optString("selectedOption"),
            impactText = obj.optString("impactText"),
            bondDeltas = deltas,
            flagChanges = flagChanges,
            fatigueDelta = obj.optInt("fatigueDelta", 0),
            mysteryDelta = obj.optInt("mysteryDelta", 0),
            redEyeDelta = obj.optInt("redEyeDelta", 0),
            avengersTrustDelta = obj.optInt("avengersTrustDelta", 0),
            secretUnlocked = if (obj.has("secretUnlocked")) obj.optString("secretUnlocked") else null,
            branchTag = if (obj.has("branchTag")) obj.optString("branchTag") else null,
            isBranching = obj.optBoolean("isBranching", false),
            isRelationship = obj.optBoolean("isRelationship", false),
            impactLevel = obj.optString("impactLevel", "orta")
        )
    }
}
