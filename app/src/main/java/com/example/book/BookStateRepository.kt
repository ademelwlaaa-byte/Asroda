package com.example.book

import android.content.Context
import com.example.data.local.BotEntity
import com.example.ui.screens.ChapterProgressManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

import java.util.concurrent.ConcurrentHashMap

/**
 * Extensible multi-book contract for 100% deterministic (zero-AI) interactive novels.
 * New story packs simply implement [BookCatalogProvider] and register in [BookRegistryHub].
 */
interface BookCatalogProvider {
    val bookId: String
    val totalPlayableChapters: Int
    val allNodes: Map<String, BookSceneNode>
    val chapters: List<BookChapterMeta>

    fun chapterMeta(chapterNumber: Int): BookChapterMeta =
        chapters.firstOrNull { it.number == chapterNumber } ?: chapters.first()

    fun resolveChapterStartNodeId(chapterNumber: Int, flags: Map<String, String>): String =
        chapterMeta(chapterNumber).startNodeId

    fun node(nodeId: String): BookSceneNode? = allNodes[nodeId]

    fun resolveSceneContent(
        node: BookSceneNode,
        flags: Map<String, String>,
        fatigue: Int,
        redEyeInstability: Int,
        avengersTrust: Int,
        bonds: Map<BookCharacterId, Int>,
        secrets: Set<String>
    ): CosmicDriftBookRegistry.ResolvedSceneText {
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
        return CosmicDriftBookRegistry.ResolvedSceneText(out, echoes)
    }
}

/**
 * Multi-book registry hub allowing any number of deterministic interactive novels
 * to share [BookStateRepository] without hardcoding a single story.
 */
object BookRegistryHub {
    private val providers = ConcurrentHashMap<String, BookCatalogProvider>().apply {
        put(CosmicDriftBookRegistry.BOOK_ID, CosmicDriftBookRegistry)
    }

    fun register(provider: BookCatalogProvider) {
        providers[provider.bookId] = provider
    }

    fun unregister(bookId: String) {
        if (bookId != CosmicDriftBookRegistry.BOOK_ID) {
            providers.remove(bookId)
        }
    }

    fun resolve(bookId: String): BookCatalogProvider =
        providers[bookId] ?: CosmicDriftBookRegistry

    fun allBookIds(): Set<String> = providers.keys.toSet()
}

/**
 * Central registry for the built-in interactive novel "Kozmik Sürükleniş"
 * (all 10 playable chapters including Season 1 Finale, extended literary scenes, deep cross-chapter flags).
 */
object CosmicDriftBookRegistry : BookCatalogProvider {
    const val BOOK_ID = "preset_aiden_mcu_cosmic"
    const val TOTAL_PLAYABLE_CHAPTERS = 24

    override val bookId: String = BOOK_ID
    override val totalPlayableChapters: Int = TOTAL_PLAYABLE_CHAPTERS

    override val allNodes: Map<String, BookSceneNode> by lazy {
        buildMap {
            // Season 1 (Chapters 1..10)
            putAll(CosmicDriftChapters1To3.nodes)
            putAll(CosmicDriftChapters4To5.nodes)
            putAll(CosmicDriftChapters6To7.nodes)
            putAll(CosmicDriftChapters8To9.nodes)
            putAll(CosmicDriftChapter10Finale.nodes)
            // Season 2: Demir ve Yankı (Chapters 11..24)
            putAll(CosmicDriftS2Chapters11To12.nodes)
            putAll(CosmicDriftS2Chapters13To14.nodes)
            putAll(CosmicDriftS2Chapter15VossMira.nodes)
            putAll(CosmicDriftS2Chapters15To16.nodes)
            putAll(CosmicDriftS2Chapters17To18.nodes)
            putAll(CosmicDriftS2Chapters19And22.nodes)
            putAll(CosmicDriftS2Chapters19To20.nodes)
            putAll(CosmicDriftS2Chapters23To24Finale.nodes)
        }
    }

    override val chapters: List<BookChapterMeta> by lazy {
        val raw = (CosmicDriftChapters1To3.chaptersMeta +
            CosmicDriftChapters4To5.chaptersMeta +
            CosmicDriftChapters6To7.chaptersMeta +
            CosmicDriftChapters8To9.chaptersMeta +
            CosmicDriftChapter10Finale.chaptersMeta +
            CosmicDriftS2Chapters11To12.chaptersMeta +
            CosmicDriftS2Chapters13To14.chaptersMeta +
            CosmicDriftS2Chapter15VossMira.chaptersMeta +
            CosmicDriftS2Chapters15To16.chaptersMeta +
            CosmicDriftS2Chapters17To18.chaptersMeta +
            CosmicDriftS2Chapters19And22.chaptersMeta +
            CosmicDriftS2Chapters19To20.chaptersMeta +
            CosmicDriftS2Chapters23To24Finale.chaptersMeta).sortedBy { it.number }
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

    override fun resolveChapterStartNodeId(chapterNumber: Int, flags: Map<String, String>): String {
        if (chapterNumber == 11) {
            return when (flags["season1_ending"]) {
                "twin_stars" -> "ch11_s1_twin_stars"
                "shield_reborn" -> "ch11_s1_shield_reborn"
                else -> "ch11_s1_vanguard"
            }
        }
        return chapterMeta(chapterNumber).startNodeId
    }

    val totalChoiceBranches: Int by lazy {
        allNodes.values.sumOf { it.choices.size }
    }

    val totalEstimatedMinutes: Int by lazy {
        chapters.sumOf { it.estimatedMinutes }
    }

    override fun chapterMeta(chapterNumber: Int): BookChapterMeta =
        chapters.firstOrNull { it.number == chapterNumber } ?: chapters.first()

    override fun node(nodeId: String): BookSceneNode? = allNodes[nodeId]

    /**
     * Resilient detector: matches both the canonical preset ID and any previously
     * imported Room copy of "Kozmik Sürükleniş" (even if an older build gave it a UUID).
     */
    fun supportsDeterministicEngine(bot: BotEntity): Boolean =
        bot.id == BOOK_ID ||
            BookRegistryHub.allBookIds().contains(bot.id) ||
            bot.aiName.contains("Kozmik Sürükleniş", ignoreCase = true) ||
            bot.aiName.contains("Cosmic Drift", ignoreCase = true) ||
            bot.universeName.contains("Kozmik Sürükleniş", ignoreCase = true) ||
            bot.pinnedMemory.contains("KOZMİK SÜRÜKLENİŞ", ignoreCase = true)

    fun supportsDeterministicEngine(botId: String): Boolean =
        botId == BOOK_ID || BookRegistryHub.allBookIds().contains(botId)

    data class ResolvedSceneText(
        val paragraphs: List<String>,
        val memoryEchoes: List<Pair<String, String>>
    )

    override fun resolveSceneContent(
        node: BookSceneNode,
        flags: Map<String, String>,
        fatigue: Int,
        redEyeInstability: Int,
        avengersTrust: Int,
        bonds: Map<BookCharacterId, Int>,
        secrets: Set<String>
    ): ResolvedSceneText = super.resolveSceneContent(
        node = node,
        flags = flags,
        fatigue = fatigue,
        redEyeInstability = redEyeInstability,
        avengersTrust = avengersTrust,
        bonds = bonds,
        secrets = secrets
    )
}

/**
 * Instant, persistent, reactive state repository for the Interactive Book Engine.
 * Supports exact scroll position save/restore, deep cross-chapter flags,
 * zero-latency choice transitions, and clean chapter/book resets.
 */
object BookStateRepository {
    private const val PREFS_NAME = "aetheria_book_engine_state_v4"
    private val flows = mutableMapOf<String, MutableStateFlow<BookReadState>>()
    private val lastSavedScroll = mutableMapOf<String, Pair<Int, Int>>()

    @Synchronized
    fun resetForTesting() {
        flows.clear()
        lastSavedScroll.clear()
    }

    private fun defaultBonds(): Map<BookCharacterId, Int> =
        BookCharacterId.entries.associateWith { it.initialBond }

    @Synchronized
    fun stateFlow(context: Context, bookId: String = CosmicDriftBookRegistry.BOOK_ID): StateFlow<BookReadState> {
        val existing = flows[bookId]
        if (existing != null) return existing.asStateFlow()
        val loaded = loadFromPrefs(context.applicationContext, bookId)
            ?: createInitialState(bookId, chapterNumber = 1)
        lastSavedScroll[bookId] = loaded.savedScrollIndex to loaded.savedScrollOffset
        val flow = MutableStateFlow(loaded)
        flows[bookId] = flow
        return flow.asStateFlow()
    }

    fun getState(context: Context, bookId: String = CosmicDriftBookRegistry.BOOK_ID): BookReadState =
        stateFlow(context, bookId).value

    /**
     * Persists exact scroll index & pixel offset in <0.1 ms using two lightweight Int keys
     * (never re-serializes the growing transcript JSON on the main thread and never triggers
     * redundant StateFlow recompositions during active scrolling).
     */
    @Synchronized
    fun saveScrollPosition(
        context: Context,
        firstVisibleIndex: Int,
        firstVisibleOffset: Int,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ) {
        val appCtx = context.applicationContext
        val safeIndex = firstVisibleIndex.coerceAtLeast(0)
        val safeOffset = firstVisibleOffset.coerceAtLeast(0)
        val prev = lastSavedScroll[bookId]
        if (prev != null && prev.first == safeIndex && prev.second == safeOffset) return

        lastSavedScroll[bookId] = safeIndex to safeOffset
        appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt("scroll_idx_$bookId", safeIndex)
            .putInt("scroll_off_$bookId", safeOffset)
            .apply()
    }

    /**
     * Scales legacy/raw deltas down to grounded, realistic increments (+1..+3 standard,
     * +4 for major turns, +5 ONLY for truly massive watershed climax events).
     */
    internal fun normalizeDelta(raw: Int, isWatershed: Boolean): Int {
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
     * Progressively unlocks characters as the player actually encounters them in the story.
     * Once a character has been met (by chapter/scene progression OR by earning any flag),
     * they stay unlocked even if the player revisits an earlier chapter.
     */
    fun unlockedCharacters(state: BookReadState): List<BookCharacterId> {
        val maxChapterReached = maxOf(state.activeChapter, state.highestCompletedChapter)
        val catalog = BookRegistryHub.resolve(state.bookId)
        val currentSceneOrder = catalog.node(state.currentNodeId)?.sceneOrder
            ?: (state.transcript.lastOrNull { it is BookTranscriptItem.SceneBlock } as? BookTranscriptItem.SceneBlock)?.sceneOrder
            ?: 1
        return BookCharacterId.entries.filter { char ->
            val hasEarnedFlag = state.numericFlags.entries.any { (k, v) ->
                k.startsWith("char:${char.key}:") && v != 0
            }
            when {
                char.minChapter <= 1 -> true
                hasEarnedFlag -> true
                maxChapterReached > char.minChapter -> true
                state.activeChapter == char.minChapter && currentSceneOrder >= char.introSceneOrder -> true
                else -> false
            }
        }
    }

    /**
     * Returns all non-zero multi-axis flags for a specific character sorted by impact strength.
     */
    fun characterFlagsFor(state: BookReadState, charId: BookCharacterId): List<Pair<CharacterFlagAxis, Int>> {
        val prefix = "char:${charId.key}:"
        return state.numericFlags.entries
            .filter { it.key.startsWith(prefix) && it.value != 0 }
            .mapNotNull { (k, v) ->
                val axis = CharacterFlagAxis.fromKey(k.removePrefix(prefix)) ?: return@mapNotNull null
                axis to v
            }
            .sortedByDescending { kotlin.math.abs(it.second) }
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
            BookCharacterId.PIETRO -> when {
                hintLower.contains("kurtar") || hintLower.contains("kardeş") || isWatershed -> CharacterFlagAxis.LOYALTY
                hintLower.contains("güven") -> CharacterFlagAxis.TRUST
                else -> CharacterFlagAxis.RESPECT
            }
            BookCharacterId.FURY -> when {
                hintLower.contains("şüphe") || hintLower.contains("sır") -> CharacterFlagAxis.SUSPICION
                hintLower.contains("saygı") || hintLower.contains("taktik") -> CharacterFlagAxis.RESPECT
                else -> CharacterFlagAxis.TRUST
            }
            BookCharacterId.ZERO -> when {
                hintLower.contains("kefaret") || hintLower.contains("huzur") || hintLower.contains("anla") -> CharacterFlagAxis.EMPATHY
                hintLower.contains("müttefik") || hintLower.contains("yemin") || isWatershed -> CharacterFlagAxis.LOYALTY
                else -> CharacterFlagAxis.TRUST
            }
            BookCharacterId.VISION -> when {
                hintLower.contains("hakikat") || hintLower.contains("insan") -> CharacterFlagAxis.EMPATHY
                hintLower.contains("güven") || isWatershed -> CharacterFlagAxis.TRUST
                else -> CharacterFlagAxis.RESPECT
            }
            BookCharacterId.VOSS -> when {
                hintLower.contains("kardeş") || hintLower.contains("aile") || hintLower.contains("koru") || isWatershed -> CharacterFlagAxis.LOYALTY
                hintLower.contains("güven") -> CharacterFlagAxis.TRUST
                else -> CharacterFlagAxis.RESPECT
            }
            BookCharacterId.MIRA -> when {
                hintLower.contains("aile") || hintLower.contains("sevgi") || isWatershed -> CharacterFlagAxis.AFFECTION
                hintLower.contains("güven") || hintLower.contains("koru") -> CharacterFlagAxis.TRUST
                else -> CharacterFlagAxis.EMPATHY
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

        // 1) Explicit per-character multi-axis flags AND auto-inferred axes for any other characters in bondDeltas!
        val explicitlyCoveredChars = mutableSetOf<BookCharacterId>()
        effect.characterAxisDeltas.forEach { (charId, axis, delta) ->
            explicitlyCoveredChars.add(charId)
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
        effect.bondDeltas.forEach { (charId, rawDelta) ->
            if (charId !in explicitlyCoveredChars) {
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
        val catalog = BookRegistryHub.resolve(bookId)
        val current = getState(appCtx, bookId)
        val currentNode = catalog.node(current.currentNodeId) ?: return current
        val choice = currentNode.choices.firstOrNull { it.id == choiceId } ?: return current
        val nextNode = catalog.node(choice.nextNodeId) ?: return current

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

        val resolved = catalog.resolveSceneContent(
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
        val catalog = BookRegistryHub.resolve(bookId)
        val clamped = chapterNumber.coerceIn(1, catalog.totalPlayableChapters)
        val current = getState(appCtx, bookId)

        if (current.activeChapter == clamped && !forceRestartIfSame && !current.isChapterCompleted) {
            return current
        }

        val startNodeId = catalog.resolveChapterStartNodeId(clamped, current.flags)
        val startNode = catalog.node(startNodeId)
            ?: return current

        // Only grant the between-chapter rest (-1 fatigue) when advancing forward from a completed chapter;
        // prevents repeatedly jumping back and forth between chapters to farm fatigue down to 0.
        val restAdjustedFatigue = if (clamped > current.activeChapter && current.isChapterCompleted) {
            (current.fatigue - 1).coerceAtLeast(0)
        } else {
            current.fatigue
        }

        val snapshot = ChapterSnapshot(
            bonds = current.bonds,
            fatigue = restAdjustedFatigue,
            mystery = current.mystery,
            redEyeInstability = current.redEyeInstability,
            avengersTrust = current.avengersTrust,
            flags = current.flags,
            numericFlags = current.numericFlags,
            discoveredSecrets = current.discoveredSecrets,
            discoveredBranches = current.discoveredBranches
        )

        val resolved = catalog.resolveSceneContent(
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
        val catalog = BookRegistryHub.resolve(bookId)
        val startNodeId = catalog.resolveChapterStartNodeId(current.activeChapter, snap.flags)
        val startNode = catalog.node(startNodeId) ?: return current

        val resolved = catalog.resolveSceneContent(
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
        lastSavedScroll.remove(bookId)
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .remove("state_$bookId")
            .remove("state_bak_$bookId")
            .remove("scroll_idx_$bookId")
            .remove("scroll_off_$bookId")
        prefs.all.keys
            .filter {
                it.startsWith("minigame_rank_${bookId}_") ||
                    it.startsWith("minigame_applied_${bookId}_") ||
                    it.startsWith("boss_medal_${bookId}_")
            }
            .forEach { editor.remove(it) }
        editor.apply()
        val fresh = createInitialState(bookId, chapterNumber = 1)
        ChapterProgressManager.resetProgress(appCtx, bookId)
        persistAndEmit(appCtx, bookId, fresh)
        return fresh
    }

    private fun rankTier(rank: String?): Int = when (rank?.uppercase()) {
        "S" -> 3
        "A" -> 2
        "B" -> 1
        else -> 0
    }

    /**
     * 100% offline/API-free persistence for embedded [[MINIGAME|...]] stages.
     * Never downgrades a previously earned rank (S > A > B), and applies the delta
     * to real deterministic [StoryFlagAxis] numericFlags so story chapters & chronicle react!
     */
    @Synchronized
    fun recordMiniGameOutcome(
        context: Context,
        gameKey: String,
        rank: String,
        score: Int,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ): BookReadState {
        val appCtx = context.applicationContext
        val normalizedKey = gameKey.trim().uppercase()
        val normalizedRank = rank.trim().uppercase().takeIf { it in setOf("S", "A", "B") } ?: "B"
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rankPrefKey = "minigame_rank_${bookId}_$normalizedKey"
        val appliedPrefKey = "minigame_applied_${bookId}_$normalizedKey"

        val previousRank = prefs.getString(rankPrefKey, null)
        val prevTier = rankTier(previousRank)
        val newTier = rankTier(normalizedRank)
        val bestRank = if (newTier >= prevTier) normalizedRank else (previousRank ?: normalizedRank)
        prefs.edit().putString(rankPrefKey, bestRank).apply()

        val current = getState(appCtx, bookId)
        val alreadyAppliedTier = prefs.getInt(appliedPrefKey, 0)
        if (newTier <= alreadyAppliedTier) {
            return current
        }

        // Compute incremental flag reward so replaying and upgrading B -> S grants only the difference!
        fun rewardForTier(tier: Int): Map<StoryFlagAxis, Int> = when (normalizedKey) {
            "WHITE_EYE_RESONANCE" -> when (tier) {
                3 -> mapOf(StoryFlagAxis.WHITE_EYE_CONTROL to 3, StoryFlagAxis.MENTAL_FATIGUE to -1)
                2 -> mapOf(StoryFlagAxis.WHITE_EYE_CONTROL to 2, StoryFlagAxis.PSYCHIC_RESONANCE to 1)
                1 -> mapOf(StoryFlagAxis.WHITE_EYE_CONTROL to 1)
                else -> emptyMap()
            }
            "BOSPHORUS_CORE_BALANCE" -> when (tier) {
                3 -> mapOf(StoryFlagAxis.PSYCHIC_RESONANCE to 3, StoryFlagAxis.HUMANITY_ANCHOR to 2)
                2 -> mapOf(StoryFlagAxis.PSYCHIC_RESONANCE to 2, StoryFlagAxis.HUMANITY_ANCHOR to 1)
                1 -> mapOf(StoryFlagAxis.PSYCHIC_RESONANCE to 1)
                else -> emptyMap()
            }
            "RED_EYE_WILLPOWER" -> when (tier) {
                3 -> mapOf(StoryFlagAxis.RED_EYE_INSTABILITY to -3, StoryFlagAxis.HUMANITY_ANCHOR to 2)
                2 -> mapOf(StoryFlagAxis.RED_EYE_INSTABILITY to -2, StoryFlagAxis.HUMANITY_ANCHOR to 1)
                1 -> mapOf(StoryFlagAxis.RED_EYE_INSTABILITY to -1)
                else -> emptyMap()
            }
            else -> emptyMap()
        }

        val prevMap = rewardForTier(alreadyAppliedTier)
        val nextMap = rewardForTier(newTier)
        val updatedNumeric = current.numericFlags.toMutableMap()
        var fatigueDelta = 0
        var redEyeDelta = 0
        val allAxes = prevMap.keys + nextMap.keys
        for (axis in allAxes) {
            val delta = (nextMap[axis] ?: 0) - (prevMap[axis] ?: 0)
            if (delta != 0) {
                val mapKey = "story:${axis.key}"
                updatedNumeric[mapKey] = ((updatedNumeric[mapKey] ?: 0) + delta).coerceIn(-50, 100)
                if (axis == StoryFlagAxis.MENTAL_FATIGUE) fatigueDelta += delta
                if (axis == StoryFlagAxis.RED_EYE_INSTABILITY) redEyeDelta += delta
            }
        }

        val updatedFlags = current.flags.toMutableMap().apply {
            put("minigame_$normalizedKey", bestRank)
            put("minigame_${normalizedKey}_score", score.coerceAtLeast(0).toString())
        }
        val updatedState = current.copy(
            numericFlags = updatedNumeric,
            flags = updatedFlags,
            fatigue = (current.fatigue + fatigueDelta).coerceIn(0, 100),
            redEyeInstability = (current.redEyeInstability + redEyeDelta).coerceIn(0, 100)
        )
        prefs.edit().putInt(appliedPrefKey, newTier).apply()
        persistAndEmit(appCtx, bookId, updatedState)
        return updatedState
    }

    fun getSavedMiniGameRank(
        context: Context,
        gameKey: String,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ): String? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("minigame_rank_${bookId}_${gameKey.trim().uppercase()}", null)
    }

    fun getAllMiniGameRanks(
        context: Context,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ): Map<String, String> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefix = "minigame_rank_${bookId}_"
        val result = linkedMapOf<String, String>()
        prefs.all.forEach { (k, v) ->
            if (k.startsWith(prefix) && v is String && v.isNotBlank()) {
                result[k.removePrefix(prefix)] = v
            }
        }
        return result
    }

    @Synchronized
    fun recordBossMedal(
        context: Context,
        bossKey: String,
        rank: String,
        score: Int,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ): String {
        val appCtx = context.applicationContext
        val normalizedKey = bossKey.trim().uppercase()
        val normalizedRank = rank.trim().uppercase().takeIf { it in setOf("S", "A", "B") } ?: "B"
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefKey = "boss_medal_${bookId}_$normalizedKey"
        val prev = prefs.getString(prefKey, null)
        val best = if (rankTier(normalizedRank) >= rankTier(prev)) normalizedRank else (prev ?: normalizedRank)
        prefs.edit()
            .putString(prefKey, best)
            .putInt("boss_score_${bookId}_$normalizedKey", maxOf(score, prefs.getInt("boss_score_${bookId}_$normalizedKey", 0)))
            .apply()
        return best
    }

    fun getAllBossMedals(
        context: Context,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ): Map<String, String> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefix = "boss_medal_${bookId}_"
        val result = linkedMapOf<String, String>()
        prefs.all.forEach { (k, v) ->
            if (k.startsWith(prefix) && v is String && v.isNotBlank()) {
                result[k.removePrefix(prefix)] = v
            }
        }
        return result
    }

    private fun createInitialState(bookId: String, chapterNumber: Int): BookReadState {
        val catalog = BookRegistryHub.resolve(bookId)
        val meta = catalog.chapterMeta(chapterNumber)
        val startNode = catalog.node(meta.startNodeId)
            ?: catalog.allNodes.values.first()
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
        val resolved = catalog.resolveSceneContent(
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
        // Atomic single-editor persistence of state JSON + exact scroll keys (zero desync on crash)
        saveToPrefsAtomic(context, bookId, state)
        lastSavedScroll[bookId] = state.savedScrollIndex to state.savedScrollOffset
        ChapterProgressManager.setExactCompletedChapter(context, bookId, state.highestCompletedChapter)
        val flow = flows[bookId]
        if (flow != null) {
            flow.value = state
        } else {
            flows[bookId] = MutableStateFlow(state)
        }
    }

    // ── JSON Serialization ──────────────────────────────────────────────────
    private fun saveToPrefsAtomic(context: Context, bookId: String, state: BookReadState) {
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
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val prevStateJson = prefs.getString("state_$bookId", null)
            val editor = prefs.edit()
                .putString("state_$bookId", root.toString())
                .putInt("scroll_idx_$bookId", state.savedScrollIndex)
                .putInt("scroll_off_$bookId", state.savedScrollOffset)
            if (!prevStateJson.isNullOrBlank()) {
                // Keep last-known-good snapshot as crash/corruption safety net
                editor.putString("state_bak_$bookId", prevStateJson)
            }
            editor.apply()
        } catch (e: Exception) {
            android.util.Log.e("BookStateRepository", "Failed saving book state", e)
        }
    }

    /**
     * Deterministic graph-integrity validator for any [BookCatalogProvider]:
     * checks that every chapter's startNodeId and every choice's nextNodeId resolves cleanly
     * and that every chapter contains at least one chapter-end node.
     */
    fun validateBookGraphIntegrity(catalog: BookCatalogProvider): List<String> {
        val issues = mutableListOf<String>()
        if (catalog.chapters.isEmpty()) {
            issues.add("Book ${catalog.bookId} has no chapters.")
            return issues
        }
        val nodes = catalog.allNodes
        for (meta in catalog.chapters) {
            if (nodes[meta.startNodeId] == null) {
                issues.add("Chapter ${meta.number} missing startNodeId='${meta.startNodeId}'")
            }
            val chNodes = nodes.values.filter { it.chapterNumber == meta.number }
            if (chNodes.none { it.isChapterEnd }) {
                issues.add("Chapter ${meta.number} has no isChapterEnd node")
            }
        }
        for ((nodeId, node) in nodes) {
            for (choice in node.choices) {
                if (nodes[choice.nextNodeId] == null) {
                    issues.add("Node '$nodeId' choice '${choice.id}' points to missing nextNodeId='${choice.nextNodeId}'")
                }
            }
        }
        return issues
    }

    private fun loadFromPrefs(context: Context, bookId: String): BookReadState? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val primaryRaw = prefs.getString("state_$bookId", null)
        if (!primaryRaw.isNullOrBlank()) {
            parsePersistedState(prefs, bookId, primaryRaw)?.let { return it }
        }
        val backupRaw = prefs.getString("state_bak_$bookId", null)
        if (!backupRaw.isNullOrBlank()) {
            parsePersistedState(prefs, bookId, backupRaw)?.let { recovered ->
                android.util.Log.w("BookStateRepository", "Recovered $bookId from backup snapshot after primary corruption")
                return recovered
            }
        }
        return null
    }

    private fun parsePersistedState(
        prefs: android.content.SharedPreferences,
        bookId: String,
        raw: String
    ): BookReadState? {
        return try {
            val catalog = BookRegistryHub.resolve(bookId)
            val root = JSONObject(raw)
            val savedActiveChapter = root.optInt("activeChapter", 1).coerceIn(1, catalog.totalPlayableChapters)
            val loadedFlags = decodeStringMap(root.optJSONObject("flags"))
            val rawNodeId = root.optString("currentNodeId", "")
            val nodeMissing = rawNodeId.isBlank() || catalog.node(rawNodeId) == null
            val currentNodeId = if (nodeMissing) {
                // Graceful recovery: if an update renamed an old scene node, resume cleanly at the start of the
                // player's saved chapter while keeping 100% of their bonds, flags, secrets & completed chapters!
                catalog.resolveChapterStartNodeId(savedActiveChapter, loadedFlags)
            } else {
                rawNodeId
            }
            if (catalog.node(currentNodeId) == null) return null

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
            if (transcript.isEmpty() || nodeMissing) {
                val fallbackNode = catalog.node(currentNodeId) ?: return null
                val recoveredResolved = catalog.resolveSceneContent(
                    node = fallbackNode,
                    flags = loadedFlags,
                    fatigue = root.optInt("fatigue", 0).coerceIn(0, 5),
                    redEyeInstability = root.optInt("redEyeInstability", 0).coerceIn(0, 5),
                    avengersTrust = root.optInt("avengersTrust", 35).coerceIn(0, 100),
                    bonds = decodeBonds(root.optJSONObject("bonds")),
                    secrets = secrets
                )
                transcript.clear()
                transcript.add(
                    BookTranscriptItem.SceneBlock(
                        nodeId = fallbackNode.id,
                        sceneOrder = fallbackNode.sceneOrder,
                        sceneTitle = fallbackNode.sceneTitle,
                        locationTag = fallbackNode.locationTag,
                        paragraphs = recoveredResolved.paragraphs,
                        memoryEchoes = recoveredResolved.memoryEchoes,
                        isChapterEnd = fallbackNode.isChapterEnd,
                        chapterEndQuote = fallbackNode.chapterEndQuote
                    )
                )
            }

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

            val fastScrollIdx = if (prefs.contains("scroll_idx_$bookId")) {
                prefs.getInt("scroll_idx_$bookId", 0).coerceAtLeast(0)
            } else {
                root.optInt("savedScrollIndex", 0).coerceAtLeast(0)
            }
            val fastScrollOff = if (prefs.contains("scroll_off_$bookId")) {
                prefs.getInt("scroll_off_$bookId", 0).coerceAtLeast(0)
            } else {
                root.optInt("savedScrollOffset", 0).coerceAtLeast(0)
            }

            val loadedNumericFlags = decodeIntMap(root.optJSONObject("numericFlags")).toMutableMap()
            // Backward-compatible flag backfill for newly added characters (Voss, Mira, Fury, Zero)
            // if the user already played those chapters on an earlier version!
            if (loadedFlags["ch5_voss_fate"] == "protected" && loadedNumericFlags.keys.none { it.startsWith("char:voss:") }) {
                loadedNumericFlags["char:voss:trust"] = 4
                loadedNumericFlags["char:voss:loyalty"] = 4
            } else if (loadedFlags["ch5_voss_fate"] == "handed_over" && loadedNumericFlags.keys.none { it.startsWith("char:voss:") }) {
                loadedNumericFlags["char:voss:suspicion"] = 3
            }
            if (loadedFlags["ch7_finale_act"] == "saved_mira" && loadedNumericFlags.keys.none { it.startsWith("char:mira:") }) {
                loadedNumericFlags["char:mira:trust"] = 5
                loadedNumericFlags["char:mira:affection"] = 4
            } else if (loadedFlags["ch7_finale_act"] == "destroyed_tower" && loadedNumericFlags.keys.none { it.startsWith("char:mira:") }) {
                loadedNumericFlags["char:mira:respect"] = 2
                loadedNumericFlags["char:mira:fear"] = 2
            }
            if (loadedFlags["s2_zero_choice"] == "ally" && loadedNumericFlags.keys.none { it.startsWith("char:zero:") }) {
                loadedNumericFlags["char:zero:trust"] = 4
                loadedNumericFlags["char:zero:empathy"] = 4
            } else if (loadedFlags["s2_zero_choice"] == "guarded" && loadedNumericFlags.keys.none { it.startsWith("char:zero:") }) {
                loadedNumericFlags["char:zero:respect"] = 2
            }
            if (loadedFlags["ch14_fury"] == "full_truth" && loadedNumericFlags.keys.none { it.startsWith("char:fury:") }) {
                loadedNumericFlags["char:fury:trust"] = 4
                loadedNumericFlags["char:fury:respect"] = 3
            }

            BookReadState(
                bookId = root.optString("bookId", bookId),
                activeChapter = root.optInt("activeChapter", 1).coerceIn(1, catalog.totalPlayableChapters),
                completedChapters = completed,
                currentNodeId = currentNodeId,
                transcript = transcript,
                chapterDecisions = chapterDecisions,
                bonds = decodeBonds(root.optJSONObject("bonds")),
                fatigue = root.optInt("fatigue", 0).coerceIn(0, 5),
                mystery = root.optInt("mystery", 0).coerceIn(0, 10),
                redEyeInstability = root.optInt("redEyeInstability", 0).coerceIn(0, 5),
                avengersTrust = root.optInt("avengersTrust", 35).coerceIn(0, 100),
                flags = loadedFlags,
                numericFlags = loadedNumericFlags,
                discoveredSecrets = secrets,
                discoveredBranches = branches,
                isChapterCompleted = root.optBoolean("isChapterCompleted", false),
                savedScrollIndex = fastScrollIdx,
                savedScrollOffset = fastScrollOff,
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
