package com.example.book

/**
 * Core data models for the deterministic, zero-latency Interactive Book Engine.
 * Tracks rich multi-axis character flags (trust, respect, suspicion, curiosity,
 * empathy, warmth, affection, loyalty, fear), deep world/cosmic/psychological flags
 * that progressively unlock across chapters, discovered secrets, branch paths,
 * full chronological transcripts, and exact saved reading scroll position.
 */
enum class BookCharacterId(
    val key: String,
    val displayName: String,
    val shortName: String,
    val possessiveName: String,
    val roleTitle: String,
    val accentHex: Long,
    val initialBond: Int,
    val minChapter: Int = 1,
    val introSceneOrder: Int = 1
) {
    STEVE(
        key = "steve",
        displayName = "Steve Rogers",
        shortName = "Steve",
        possessiveName = "Steve'in",
        roleTitle = "Saha Lideri • Güven",
        accentHex = 0xFF60A5FAL,
        initialBond = 38
    ),
    NATASHA(
        key = "natasha",
        displayName = "Natasha Romanoff",
        shortName = "Natasha",
        possessiveName = "Natasha'nın",
        roleTitle = "Gölge Ortak • Anlayış",
        accentHex = 0xFFF472B6L,
        initialBond = 42
    ),
    WANDA(
        key = "wanda",
        displayName = "Wanda Maximoff",
        shortName = "Wanda",
        possessiveName = "Wanda'nın",
        roleTitle = "Zihinsel Yankı • Empati",
        accentHex = 0xFFC084FCL,
        initialBond = 40
    ),
    TONY(
        key = "tony",
        displayName = "Tony Stark",
        shortName = "Tony",
        possessiveName = "Tony'nin",
        roleTitle = "Stratejist • Merak",
        accentHex = 0xFFFBBF24L,
        initialBond = 35
    ),
    CLINT(
        key = "clint",
        displayName = "Clint Barton",
        shortName = "Clint",
        possessiveName = "Clint'in",
        roleTitle = "Keskin Göz • Dostluk",
        accentHex = 0xFF34D399L,
        initialBond = 42
    ),
    BRUCE(
        key = "bruce",
        displayName = "Bruce Banner",
        shortName = "Bruce",
        possessiveName = "Bruce'un",
        roleTitle = "Bilim & Denge • Empati",
        accentHex = 0xFF4ADE80L,
        initialBond = 38
    ),
    THOR(
        key = "thor",
        displayName = "Thor Odinson",
        shortName = "Thor",
        possessiveName = "Thor'un",
        roleTitle = "Kozmik Müttefik • Onur",
        accentHex = 0xFF38BDF8L,
        initialBond = 36
    ),
    FURY(
        key = "fury",
        displayName = "Nick Fury",
        shortName = "Fury",
        possessiveName = "Fury'nin",
        roleTitle = "S.H.I.E.L.D. Direktörü • İstihbarat",
        accentHex = 0xFF94A3B8L,
        initialBond = 30,
        minChapter = 4,
        introSceneOrder = 1
    ),
    VOSS(
        key = "voss",
        displayName = "Elias Voss",
        shortName = "Voss",
        possessiveName = "Voss'un",
        roleTitle = "Denek 6-B • Kardeşlik",
        accentHex = 0xFFF87171L,
        initialBond = 30,
        minChapter = 5,
        introSceneOrder = 1
    ),
    MIRA(
        key = "mira",
        displayName = "Mira",
        shortName = "Mira",
        possessiveName = "Mira'nın",
        roleTitle = "Denek 8-C • Yeni Hayat",
        accentHex = 0xFF38BDF8L,
        initialBond = 32,
        minChapter = 7,
        introSceneOrder = 2
    ),
    PIETRO(
        key = "pietro",
        displayName = "Pietro Maximoff",
        shortName = "Pietro",
        possessiveName = "Pietro'nun",
        roleTitle = "Gümüş Şimşek • İkiz Kardeş",
        accentHex = 0xFF7DD3FCL,
        initialBond = 24,
        minChapter = 12,
        introSceneOrder = 2
    ),
    ZERO(
        key = "zero",
        displayName = "Sıfır",
        shortName = "Sıfır",
        possessiveName = "Sıfır'ın",
        roleTitle = "Karanlık Gelecek • Tekillik Yankısı",
        accentHex = 0xFF60A5FAL,
        initialBond = 25,
        minChapter = 13,
        introSceneOrder = 3
    ),
    VISION(
        key = "vision",
        displayName = "Vision",
        shortName = "Vision",
        possessiveName = "Vision'ın",
        roleTitle = "Zihin Taşı • Hakikat",
        accentHex = 0xFFEAB308L,
        initialBond = 34,
        minChapter = 16,
        introSceneOrder = 4
    );

    companion object {
        fun fromKey(key: String): BookCharacterId? =
            entries.firstOrNull { it.key == key }
    }
}

/**
 * Multi-axis relationship flags tracked per character.
 * Not every axis changes in Chapter 1 — deeper axes like AFFECTION ("sevgisi"),
 * LOYALTY ("sadakati"), and FEAR ("çekincesi") naturally unlock in later chapters
 * as emotional closeness, crisis alliances, or Red Eye instability emerge.
 */
enum class CharacterFlagAxis(
    val key: String,
    val turkishLabel: String,
    val accentHex: Long,
    val minUnlockChapter: Int,
    val isNegativeTone: Boolean = false
) {
    TRUST("trust", "güveni", 0xFF60A5FAL, 1),
    RESPECT("respect", "saygısı", 0xFF38BDF8L, 1),
    SUSPICION("suspicion", "şüphesi", 0xFFFCA5A5L, 1, isNegativeTone = true),
    CURIOSITY("curiosity", "merakı", 0xFFFBBF24L, 1),
    EMPATHY("empathy", "empatisi", 0xFFC084FCL, 1),
    WARMTH("warmth", "yakınlığı", 0xFF34D399L, 1),
    AFFECTION("affection", "sevgisi", 0xFFF472B6L, 2),
    LOYALTY("loyalty", "sadakati", 0xFF93C5FDL, 3),
    FEAR("fear", "çekincesi", 0xFFF87171L, 2, isNegativeTone = true);

    companion object {
        fun fromKey(key: String): CharacterFlagAxis? =
            entries.firstOrNull { it.key == key }
    }
}

/**
 * Rich world, tactical, psychological, and cosmic flags tracked across the entire saga.
 * Flags unlock organically only when relevant choices or later chapters trigger them.
 */
enum class StoryFlagAxis(
    val key: String,
    val turkishLabel: String,
    val accentHex: Long,
    val minUnlockChapter: Int,
    val isWarning: Boolean = false
) {
    AVENGERS_TRUST("avengers_trust", "Ekip güveni", 0xFF60A5FAL, 1),
    SHIELD_SUSPICION("shield_suspicion", "S.H.I.E.L.D. kuşkusu", 0xFFFCA5A5L, 1, isWarning = true),
    MYSTERY_AURA("mystery_aura", "Gizem perdesi", 0xFFC4B5FDL, 1),
    MENTAL_FATIGUE("mental_fatigue", "Zihinsel yorgunluk", 0xFFFBBF24L, 1, isWarning = true),
    WHITE_EYE_CONTROL("white_eye_control", "Beyaz Göz kontrolü", 0xFFE0F2FEL, 1),
    TACTICAL_DISCIPLINE("tactical_discipline", "Taktik disiplin", 0xFF38BDF8L, 1),
    INDEPENDENCE("independence", "Bağımsızlık çizgisi", 0xFFA78BFAL, 1),
    HUMANITY_ANCHOR("humanity_anchor", "İnsanlık bağı", 0xFF6EE7B7L, 1),
    PSYCHIC_RESONANCE("psychic_resonance", "Zihinsel rezonans", 0xFFE879F9L, 1),
    TOWER_BELONGING("tower_belonging", "Kule aidiyeti", 0xFF93C5FDL, 2),
    RED_EYE_INSTABILITY("red_eye_instability", "Kırmızı Göz dengesizliği", 0xFFF87171L, 2, isWarning = true),
    COSMIC_AWAKENING("cosmic_awakening", "Kozmik uyanış", 0xFF818CF8L, 2),
    FIELD_INITIATIVE("field_initiative", "Saha inisiyatifi", 0xFF34D399L, 3),
    SHADOW_ALLIANCE("shadow_alliance", "Gölge ittifakı", 0xFFF472B6L, 3),
    DARK_TEMPTATION("dark_temptation", "Karanlık cazibe", 0xFFFB7185L, 4, isWarning = true),
    PUBLIC_EXPOSURE("public_exposure", "Açığa çıkma riski", 0xFFFDBA74L, 4, isWarning = true);

    companion object {
        fun fromKey(key: String): StoryFlagAxis? =
            entries.firstOrNull { it.key == key }
    }
}

/**
 * A single concrete, balanced flag delta recorded for a decision.
 * Formats cleanly as e.g. "Steve'in güveni +3 arttı" or "Natasha'nın şüphesi -1 azaldı".
 */
data class FlagChangeEntry(
    val key: String,
    val label: String,
    val delta: Int,
    val accentHex: Long,
    val characterId: BookCharacterId? = null,
    val isNewlyUnlocked: Boolean = false
) {
    fun formatTurkish(): String = if (delta >= 0) {
        "$label +$delta arttı"
    } else {
        "$label $delta azaldı"
    }
}

data class BookChoiceEffect(
    val bondDeltas: Map<BookCharacterId, Int> = emptyMap(),
    val characterAxisDeltas: List<Triple<BookCharacterId, CharacterFlagAxis, Int>> = emptyList(),
    val storyAxisDeltas: Map<StoryFlagAxis, Int> = emptyMap(),
    val fatigueDelta: Int = 0,
    val mysteryDelta: Int = 0,
    val redEyeDelta: Int = 0,
    val avengersTrustDelta: Int = 0,
    val setFlags: Map<String, String> = emptyMap(),
    val secretUnlocked: String? = null,
    val branchTag: String? = null,
    val impactSummary: String,
    val isBranching: Boolean = false,
    val isRelationship: Boolean = false,
    val impactLevel: String = "orta" // "dusuk", "orta", "kritik"
)

data class BookChoiceDef(
    val id: String,
    val label: String,
    val toneHint: String,
    val nextNodeId: String,
    val effect: BookChoiceEffect
)

data class ConditionalNarrative(
    val requiredFlagKey: String? = null,
    val requiredFlagValue: String? = null,
    val requiredAnyFlagValues: Set<String>? = null,
    val requiredSecret: String? = null,
    val minFatigue: Int? = null,
    val minRedEye: Int? = null,
    val minAvengersTrust: Int? = null,
    val minBondCharacter: BookCharacterId? = null,
    val minBondValue: Int? = null,
    val callbackLabel: String? = null,
    val paragraphs: List<String>
)

data class BookSceneNode(
    val id: String,
    val chapterNumber: Int,
    val sceneOrder: Int,
    val sceneTitle: String,
    val locationTag: String,
    val paragraphs: List<String>,
    val conditionalNarratives: List<ConditionalNarrative> = emptyList(),
    val choicePrompt: String? = null,
    val decisionTitle: String? = null,
    val choices: List<BookChoiceDef> = emptyList(),
    val isChapterEnd: Boolean = false,
    val chapterEndQuote: String? = null
)

data class BookChapterMeta(
    val number: Int,
    val romanNumeral: String,
    val title: String,
    val titleEn: String,
    val subtitle: String,
    val description: String,
    val descriptionEn: String,
    val estimatedMinutes: Int,
    val accentHex: Long,
    val startNodeId: String,
    val sceneCount: Int,
    val decisionCount: Int
)

data class RecordedDecision(
    val chapterNumber: Int,
    val nodeId: String,
    val decisionTitle: String,
    val choiceId: String,
    val selectedOption: String,
    val impactText: String,
    val bondDeltas: Map<BookCharacterId, Int>,
    val flagChanges: List<FlagChangeEntry> = emptyList(),
    val fatigueDelta: Int,
    val mysteryDelta: Int,
    val redEyeDelta: Int = 0,
    val avengersTrustDelta: Int = 0,
    val secretUnlocked: String?,
    val branchTag: String?,
    val isBranching: Boolean,
    val isRelationship: Boolean,
    val impactLevel: String
)

sealed class BookTranscriptItem {
    data class SceneBlock(
        val nodeId: String,
        val sceneOrder: Int,
        val sceneTitle: String,
        val locationTag: String,
        val paragraphs: List<String>,
        val memoryEchoes: List<Pair<String, String>> = emptyList(),
        val isChapterEnd: Boolean = false,
        val chapterEndQuote: String? = null
    ) : BookTranscriptItem()

    data class DecisionBlock(
        val decision: RecordedDecision
    ) : BookTranscriptItem()
}

data class ChapterSnapshot(
    val bonds: Map<BookCharacterId, Int>,
    val fatigue: Int,
    val mystery: Int,
    val redEyeInstability: Int,
    val avengersTrust: Int,
    val flags: Map<String, String>,
    val numericFlags: Map<String, Int> = emptyMap(),
    val discoveredSecrets: Set<String>,
    val discoveredBranches: Set<String>
)

data class BookReadState(
    val bookId: String,
    val activeChapter: Int,
    val completedChapters: Set<Int>,
    val currentNodeId: String,
    val transcript: List<BookTranscriptItem>,
    val chapterDecisions: Map<Int, List<RecordedDecision>>,
    val bonds: Map<BookCharacterId, Int>,
    val fatigue: Int,
    val mystery: Int,
    val redEyeInstability: Int = 0,
    val avengersTrust: Int = 35,
    val flags: Map<String, String>,
    val numericFlags: Map<String, Int> = emptyMap(),
    val discoveredSecrets: Set<String>,
    val discoveredBranches: Set<String>,
    val isChapterCompleted: Boolean,
    val savedScrollIndex: Int = 0,
    val savedScrollOffset: Int = 0,
    val chapterStartSnapshot: ChapterSnapshot? = null
) {
    val highestCompletedChapter: Int
        get() = completedChapters.maxOrNull() ?: 0

    val currentChapterDecisions: List<RecordedDecision>
        get() = chapterDecisions[activeChapter].orEmpty()

    fun bondScore(character: BookCharacterId): Int =
        (bonds[character] ?: character.initialBond).coerceIn(0, 100)

    fun bondFraction(character: BookCharacterId): Float =
        bondScore(character) / 100f

    fun characterAxisValue(character: BookCharacterId, axis: CharacterFlagAxis): Int =
        numericFlags["char:${character.key}:${axis.key}"] ?: 0

    fun storyAxisValue(axis: StoryFlagAxis): Int =
        numericFlags["story:${axis.key}"] ?: 0
}
