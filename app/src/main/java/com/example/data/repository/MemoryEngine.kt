package com.example.data.repository

import com.example.data.local.MemoryCheckpointEntity
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MemoryFactEntity
import com.example.data.local.MemoryFragmentEntity
import org.json.JSONArray
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Turkish-aware morphological analyzer, tokenizer, and stemmer for Hybrid RAG.
 * Handles Turkish casing (İ/ı/Ş/Ğ/Ü/Ö/Ç), apostrophe-separated suffixes (Natasha'nın -> natasha),
 * and common agglutinative inflectional suffixes so Turkish queries recall memories accurately.
 */
object TurkishTextAnalyzer {
    private val TR_LOCALE = Locale("tr", "TR")

    val STOP_WORDS: Set<String> = setOf(
        "ve", "veya", "ya", "yahut", "bir", "bu", "şu", "o", "de", "da", "ki", "ile", "için",
        "en", "çok", "az", "daha", "ama", "fakat", "lakin", "ancak", "gibi", "kadar", "gore", "göre",
        "ben", "sen", "biz", "siz", "onlar", "beni", "seni", "onu", "bana", "sana", "ona",
        "benim", "senin", "onun", "bizim", "sizin", "onların",
        "mi", "mı", "mu", "mü", "misin", "mısın", "musun", "müsün",
        "her", "hep", "hiç", "bazı", "biraz", "bütün", "tüm", "diğer", "başka",
        "var", "yok", "değil", "olan", "olarak", "oldu", "olur", "olmuş", "etmek", "etti", "ediyor",
        "ne", "nasıl", "neden", "niye", "niçin", "kim", "kimi", "kime", "nerede", "nereye",
        "şey", "yani", "işte", "artık", "hala", "hâlâ", "zaten", "bile", "sadece", "yalnızca",
        "sonra", "önce", "şimdi", "burada", "orada", "şurada", "böyle", "şöyle", "öyle",
        "evet", "hayır", "tamam", "peki", "olur", "belki", "tabii", "tabi", "gerçekten",
        "dedi", "dedim", "diyor", "söyledi", "baktı", "gülümsedi", "yüzüne", "gözlerine",
        "the", "a", "an", "is", "are", "was", "were", "and", "or", "to", "in", "of", "for", "with", "on", "at", "by", "from"
    )

    // Ordered longest-first so compound suffixes peel cleanly without over-stripping roots
    private val SUFFIXES: List<String> = listOf(
        "lerimizden", "larımızdan", "lerinizden", "larınızdan",
        "lerimizde", "larımızda", "lerinizde", "larınızda",
        "lerimize", "larımıza", "lerinize", "larınıza",
        "lerimiz", "larımız", "leriniz", "larınız",
        "lerden", "lardan", "lerin", "ların", "lerde", "larda", "lere", "lara", "leri", "ları",
        "indeki", "ındaki", "undeki", "ündeki", "ndeki", "ndaki",
        "inden", "ından", "undan", "ünden", "nden", "ndan",
        "inde", "ında", "unda", "ünde", "nde", "nda",
        "inin", "ının", "unun", "ünün", "nın", "nin", "nun", "nün",
        "iyle", "ıyla", "uyla", "üyle", "yla", "yle",
        "iyor", "ıyor", "uyor", "üyor", "yor",
        "ecek", "acak", "miş", "mış", "muş", "müş",
        "dik", "dık", "duk", "dük", "tik", "tık", "tuk", "tük",
        "dan", "den", "tan", "ten", "daki", "deki", "taki", "teki",
        "lar", "ler", "lık", "lik", "luk", "lük",
        "sız", "siz", "suz", "süz",
        "miz", "mız", "muz", "müz", "niz", "nız", "nuz", "nüz",
        "sin", "sın", "sun", "sün",
        "im", "ım", "um", "üm", "in", "ın", "un", "ün",
        "da", "de", "ta", "te", "ya", "ye", "la", "le"
    )

    fun lowerTr(text: String): String = text.lowercase(TR_LOCALE)

    /**
     * Normalizes Turkish text for matching while preserving Turkish letters.
     */
    fun normalize(text: String): String {
        return lowerTr(text)
            .replace('’', '\'')
            .replace('‘', '\'')
            .replace('`', '\'')
            .trim()
    }

    /**
     * Conservatively stems a single Turkish token while preserving roots of length >= 3.
     */
    fun stemToken(rawToken: String): String {
        val clean = normalize(rawToken)
            .substringBefore('\'') // "Natasha'nın" -> "natasha"
            .replace(Regex("""[^\p{L}\p{N}]"""), "")
        if (clean.length <= 3) return clean

        var stem = clean
        // Up to 2 conservative suffix peels (e.g. "kardeş-ler-im" -> "kardeş")
        repeat(2) {
            var stripped = false
            for (suffix in SUFFIXES) {
                if (stem.length - suffix.length >= 3 && stem.endsWith(suffix)) {
                    stem = stem.dropLast(suffix.length)
                    stripped = true
                    break
                }
            }
            if (!stripped) return stem
        }
        return stem
    }

    /**
     * Extracts meaningful stemmed tokens (stopwords removed).
     */
    fun extractStems(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val normalized = normalize(text)
        val rawTokens = normalized.split(Regex("""[\s,.;:!?()\[\]{}"“”«»—–\-/\\|]+"""))
        val out = ArrayList<String>(rawTokens.size)
        for (tok in rawTokens) {
            if (tok.length < 2) continue
            val beforeApos = tok.substringBefore('\'')
            if (beforeApos.length < 2) continue
            if (STOP_WORDS.contains(beforeApos)) continue
            val stem = stemToken(tok)
            if (stem.length >= 2 && !STOP_WORDS.contains(stem)) {
                out.add(stem)
            }
        }
        return out
    }

    /**
     * Extracts likely proper nouns / character names from raw cased text (for entity linking).
     */
    fun extractProperNouns(text: String): Set<String> {
        if (text.isBlank()) return emptySet()
        val result = LinkedHashSet<String>()
        val tokens = text.split(Regex("""[\s,.;:!?()\[\]{}"“”«»—–\-/\\]+"""))
        for (raw in tokens) {
            val base = raw.substringBefore('\'').substringBefore('’')
            if (base.length < 3) continue
            val first = base.first()
            if (first.isUpperCase() && first.isLetter()) {
                val lower = lowerTr(base)
                if (!STOP_WORDS.contains(lower)) {
                    result.add(lower)
                }
            }
        }
        return result
    }

    /**
     * Computes the stem-level overlap coefficient |A∩B| / min(|A|,|B|) between two texts
     * (useful for copy-paste & dedup checks).
     */
    fun stemOverlapRatio(sourceText: String, candidateText: String): Float =
        overlapCoefficient(extractStems(sourceText).toSet(), extractStems(candidateText).toSet())

    /** Overlap coefficient on precomputed stem sets (avoids re-tokenizing in hot loops). */
    fun overlapCoefficient(s1: Set<String>, s2: Set<String>): Float {
        if (s1.isEmpty() || s2.isEmpty()) return 0f
        val (small, large) = if (s1.size <= s2.size) s1 to s2 else s2 to s1
        var intersection = 0
        for (s in small) if (large.contains(s)) intersection++
        return intersection.toFloat() / small.size.toFloat()
    }

    /** Splits normalized text into raw word tokens (apostrophe suffix kept, e.g. "roma'da"). */
    fun tokenize(normalizedText: String): List<String> =
        normalizedText.split(Regex("""[^\p{L}\p{N}']+""")).filter { it.isNotBlank() }

    /**
     * Unicode-safe word-start prefix match. Unlike `String.contains`, "öp" does NOT match "köpek"
     * and "kan" does NOT match "akan". Tokens starting with any [exceptions] prefix are ignored
     * (e.g. "kan" vs "kanepe"). Multi-word terms are matched as phrases at a word start.
     */
    fun containsWordPrefix(normalizedText: String, term: String, exceptions: Collection<String> = emptyList()): Boolean {
        if (term.isBlank() || normalizedText.isBlank()) return false
        if (term.contains(' ')) {
            var from = 0
            while (true) {
                val idx = normalizedText.indexOf(term, from)
                if (idx < 0) return false
                val prevOk = idx == 0 || !normalizedText[idx - 1].isLetterOrDigit()
                if (prevOk) return true
                from = idx + 1
            }
        }
        return tokenize(normalizedText).any { tok ->
            val bare = tok.trim('\'')
            bare.startsWith(term) && exceptions.none { bare.startsWith(it) }
        }
    }

    /**
     * Matches a noun [term] as a whole word optionally followed by a Turkish case suffix
     * ("roma", "roma'da", "romada", "romalı") but NOT unrelated words sharing the prefix ("romantik").
     */
    fun containsNounWithCaseSuffix(normalizedText: String, term: String): Boolean {
        if (term.isBlank()) return false
        if (term.contains(' ')) return containsWordPrefix(normalizedText, term)
        return tokenize(normalizedText).any { tok ->
            if (!tok.startsWith(term)) return@any false
            val rest = tok.substring(term.length)
            rest.isEmpty() || rest.startsWith('\'') || rest in NOUN_CASE_SUFFIXES
        }
    }

    private val NOUN_CASE_SUFFIXES: Set<String> = setOf(
        "da", "de", "ta", "te", "dan", "den", "tan", "ten", "ya", "ye", "a", "e",
        "yı", "yi", "yu", "yü", "ı", "i", "u", "ü", "nın", "nin", "nun", "nün", "ın", "in", "un", "ün",
        "lı", "li", "lu", "lü", "daki", "deki", "taki", "teki", "yla", "yle", "la", "le"
    )
}

/**
 * Multi-POV First-Person Living Memory ("Yaşanan Anı") model & codec.
 * Stored inside existing MemoryEventEntity.description with zero DB schema changes,
 * while remaining 100% backwards-compatible with plain-text legacy events.
 */
data class LivingMemory(
    val povCharacter: String,
    val title: String,
    val emotionTag: String,
    val salience: Int,
    val narrative: String,
    val innerThought: String,
    val isStructured: Boolean
)

object LivingMemoryCodec {
    private const val HEADER_PREFIX = "[[MEM|"
    private const val HEADER_SUFFIX = "]]"
    private const val INNER_MARKER = "[[INNER]]"

    fun encode(
        povCharacter: String,
        title: String,
        emotionTag: String,
        salience: Int,
        narrative: String,
        innerThought: String
    ): String {
        val cleanPov = povCharacter.replace("|", " ").replace("]", "").trim().ifBlank { "Karakter" }
        val cleanTitle = title.replace("|", " ").replace("]", "").trim().ifBlank { "Unutulmaz An" }
        val cleanEmo = emotionTag.replace("|", " ").replace("]", "").trim().ifBlank { "Derin Etki" }
        val cleanSalience = salience.coerceIn(1, 10)
        val cleanNarrative = narrative.trim()
        val cleanInner = innerThought.trim()

        return buildString {
            append("${HEADER_PREFIX}POV:$cleanPov|TITLE:$cleanTitle|EMO:$cleanEmo|WEIGHT:$cleanSalience$HEADER_SUFFIX\n")
            append(cleanNarrative)
            if (cleanInner.isNotBlank()) {
                append("\n$INNER_MARKER\n")
                append(cleanInner)
            }
        }
    }

    fun parse(rawDescription: String, fallbackImportance: Int = 70): LivingMemory {
        val trimmed = rawDescription.trim()
        if (!trimmed.startsWith(HEADER_PREFIX)) {
            return LivingMemory(
                povCharacter = "",
                title = "",
                emotionTag = "",
                salience = (fallbackImportance / 10).coerceIn(1, 10),
                narrative = trimmed,
                innerThought = "",
                isStructured = false
            )
        }

        val headerEnd = trimmed.indexOf(HEADER_SUFFIX)
        if (headerEnd <= HEADER_PREFIX.length) {
            return LivingMemory(
                povCharacter = "",
                title = "",
                emotionTag = "",
                salience = (fallbackImportance / 10).coerceIn(1, 10),
                narrative = trimmed,
                innerThought = "",
                isStructured = false
            )
        }

        val headerBody = trimmed.substring(HEADER_PREFIX.length, headerEnd)
        val fields = mutableMapOf<String, String>()
        headerBody.split("|").forEach { part ->
            val colonIdx = part.indexOf(':')
            if (colonIdx > 0) {
                val k = part.substring(0, colonIdx).trim().uppercase()
                val v = part.substring(colonIdx + 1).trim()
                fields[k] = v
            }
        }

        val body = trimmed.substring(headerEnd + HEADER_SUFFIX.length).trim()
        val innerIdx = body.indexOf(INNER_MARKER)
        val narrative = if (innerIdx >= 0) body.substring(0, innerIdx).trim() else body
        val innerThought = if (innerIdx >= 0) body.substring(innerIdx + INNER_MARKER.length).trim() else ""

        return LivingMemory(
            povCharacter = fields["POV"].orEmpty().ifBlank { "Karakter" },
            title = fields["TITLE"].orEmpty().ifBlank { "Unutulmaz An" },
            emotionTag = fields["EMO"].orEmpty(),
            salience = fields["WEIGHT"]?.toIntOrNull()?.coerceIn(1, 10)
                ?: (fallbackImportance / 10).coerceIn(1, 10),
            narrative = narrative,
            innerThought = innerThought,
            isStructured = true
        )
    }

    /**
     * Returns clean searchable text for vector embedding & BM25 indexing.
     */
    fun toSearchableText(rawDescription: String): String {
        val parsed = parse(rawDescription)
        if (!parsed.isStructured) return parsed.narrative
        return buildString {
            if (parsed.povCharacter.isNotBlank()) append(parsed.povCharacter).append(' ')
            if (parsed.title.isNotBlank()) append(parsed.title).append(' ')
            if (parsed.emotionTag.isNotBlank()) append(parsed.emotionTag).append(' ')
            append(parsed.narrative)
            if (parsed.innerThought.isNotBlank()) append(' ').append(parsed.innerThought)
        }.trim()
    }

    /**
     * Formats a Living Memory cleanly for LLM system prompt injection.
     */
    fun formatForPrompt(
        event: MemoryEventEntity,
        currentStoryDay: Long
    ): String {
        val parsed = parse(event.description, event.importanceScore)
        val timeLabel = MemoryEngine.storyTimeLabel(event.storyDayIndex, currentStoryDay, event.storyCalendarDateStr)
        val clarityTag = MemoryEngine.memoryClarityTag(event.storyDayIndex, currentStoryDay, event.importanceScore)

        return if (parsed.isStructured) {
            buildString {
                append("- $clarityTag [$timeLabel] [${parsed.povCharacter}'ın Gözünden — ${parsed.title}]")
                if (parsed.emotionTag.isNotBlank()) append(" (${parsed.emotionTag})")
                append(": ${parsed.narrative}")
                if (parsed.innerThought.isNotBlank()) {
                    append(" | Söylenmemiş İç Düşünce: \"${parsed.innerThought}\"")
                }
            }
        } else {
            "- $clarityTag [$timeLabel] ${parsed.narrative}"
        }
    }

    /**
     * Detects whether a generated memory narrative is lazily copy-pasted from raw chat messages.
     */
    fun isCopyPasteOfChat(narrative: String, rawChatTexts: List<String>): Boolean {
        val cleanNarr = TurkishTextAnalyzer.normalize(narrative)
        if (cleanNarr.length < 25) return true
        for (raw in rawChatTexts) {
            val cleanRaw = TurkishTextAnalyzer.normalize(raw)
            if (cleanRaw.isBlank()) continue
            if (cleanRaw.contains(cleanNarr) || cleanNarr.contains(cleanRaw) && cleanRaw.length >= 45) {
                return true
            }
            // Check high verbatim n-gram / stem overlap on long spans
            val overlap = TurkishTextAnalyzer.stemOverlapRatio(cleanNarr, cleanRaw)
            if (overlap >= 0.86f && cleanNarr.length <= cleanRaw.length + 25) {
                return true
            }
        }
        return false
    }
}

/**
 * 4-Tier Human-Like Cognitive Memory Hierarchy (Section 1):
 * - DISCARD: trivial scene choreography / filler gestures (never saved)
 * - EPHEMERAL: temporary daily details (2-3 story days or 35 messages TTL)
 * - ARC: ongoing plot/injury/mission states (10-14 story days TTL)
 * - CORE: permanent identity, preferences, secrets, relationships, ★ locked entries
 */
enum class MemoryTier(val code: String) {
    DISCARD("discard"),
    EPHEMERAL("ephemeral"),
    ARC("arc"),
    CORE("core");

    companion object {
        fun fromRaw(raw: String?): MemoryTier {
            return when (TurkishTextAnalyzer.normalize(raw.orEmpty())) {
                "discard", "noise", "ignore", "gurultu", "gürültü", "at", "tier0", "0" -> DISCARD
                "ephemeral", "temp", "temporary", "short", "kisa", "kısa", "gecici", "geçici", "tier1", "1" -> EPHEMERAL
                "arc", "mid", "plot", "ark", "orta", "gorev", "görev", "tier2", "2" -> ARC
                else -> CORE
            }
        }
    }
}

data class MemoryLifecycleMeta(
    val baseConfidence: String = "certain",
    val tier: MemoryTier = MemoryTier.CORE,
    val createdStoryDay: Long = 1L,
    val expiresStoryDay: Long = 0L,
    val expiresMessageIndex: Int = 0,
    val accessCount: Int = 0,
    val lastAccessStoryDay: Long = 0L
)

/**
 * Backwards-compatible lifecycle metadata codec stored in MemoryFactEntity.confidence
 * so JAR-locked Room entities require ZERO schema migrations:
 * - Legacy: "certain" / "inferred" / "disputed" -> parsed automatically as CORE.
 * - Structured: "certain|t:ephemeral|cd:4|ed:7|em:95|hit:1|la:4"
 */
object MemoryLifecycleCodec {
    fun parseConfidenceBase(rawConfidence: String): String {
        val base = rawConfidence.substringBefore('|').trim().lowercase()
        return base.ifBlank { "certain" }
    }

    fun isDisputed(rawConfidence: String): Boolean {
        return parseConfidenceBase(rawConfidence).equals("disputed", ignoreCase = true)
    }

    fun encode(meta: MemoryLifecycleMeta): String {
        val cleanBase = parseConfidenceBase(meta.baseConfidence)
        if (meta.tier == MemoryTier.CORE && meta.expiresStoryDay <= 0L && meta.expiresMessageIndex <= 0 && meta.accessCount <= 0) {
            return cleanBase
        }
        val cd = meta.createdStoryDay.coerceAtLeast(1L)
        val ed = meta.expiresStoryDay.coerceAtLeast(0L)
        val em = meta.expiresMessageIndex.coerceAtLeast(0)
        val hit = meta.accessCount.coerceAtLeast(0)
        val la = meta.lastAccessStoryDay.coerceAtLeast(0L)
        return "$cleanBase|t:${meta.tier.code}|cd:$cd|ed:$ed|em:$em|hit:$hit|la:$la"
    }

    fun parse(rawConfidence: String): MemoryLifecycleMeta {
        val trimmed = rawConfidence.trim()
        if (!trimmed.contains('|')) {
            return MemoryLifecycleMeta(
                baseConfidence = parseConfidenceBase(trimmed),
                tier = MemoryTier.CORE
            )
        }
        val parts = trimmed.split('|')
        val base = parts.firstOrNull()?.trim()?.lowercase().orEmpty().ifBlank { "certain" }
        var tier = MemoryTier.CORE
        var cd = 1L
        var ed = 0L
        var em = 0
        var hit = 0
        var la = 0L
        for (i in 1 until parts.size) {
            val kv = parts[i].trim()
            val idx = kv.indexOf(':')
            if (idx <= 0) continue
            val k = kv.substring(0, idx).lowercase()
            val v = kv.substring(idx + 1).trim()
            when (k) {
                "t" -> tier = MemoryTier.fromRaw(v)
                "cd" -> cd = v.toLongOrNull()?.coerceAtLeast(1L) ?: 1L
                "ed" -> ed = v.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
                "em" -> em = v.toIntOrNull()?.coerceAtLeast(0) ?: 0
                "hit" -> hit = v.toIntOrNull()?.coerceAtLeast(0) ?: 0
                "la" -> la = v.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
            }
        }
        return MemoryLifecycleMeta(
            baseConfidence = base,
            tier = tier,
            createdStoryDay = cd,
            expiresStoryDay = ed,
            expiresMessageIndex = em,
            accessCount = hit,
            lastAccessStoryDay = la
        )
    }

    fun withDisputed(rawConfidence: String): String {
        val meta = parse(rawConfidence)
        return encode(meta.copy(baseConfidence = "disputed"))
    }

    fun withPromotedCore(rawConfidence: String): String {
        val meta = parse(rawConfidence)
        val cleanBase = if (meta.baseConfidence.equals("disputed", ignoreCase = true)) "certain" else meta.baseConfidence
        return cleanBase
    }

    /**
     * Records a RAG recall hit on a temporary/arc memory.
     * When an EPHEMERAL memory reaches [promoteThreshold] hits before expiry,
     * it is automatically promoted to permanent CORE (human-like habit reinforcement).
     * Returns Pair(updatedConfidenceString, wasPromotedToCore).
     */
    fun withAccessHit(
        rawConfidence: String,
        currentStoryDay: Long,
        promoteThreshold: Int = 2
    ): Pair<String, Boolean> {
        val meta = parse(rawConfidence)
        if (meta.tier == MemoryTier.CORE) return Pair(rawConfidence, false)

        val safeDay = currentStoryDay.coerceAtLeast(1L)
        val newHits = meta.accessCount + 1
        val newLastAccess = maxOf(meta.lastAccessStoryDay, safeDay)

        if (meta.tier == MemoryTier.EPHEMERAL && newHits >= promoteThreshold) {
            return Pair(withPromotedCore(rawConfidence), true)
        }

        val updated = meta.copy(
            accessCount = newHits,
            lastAccessStoryDay = newLastAccess
        )
        return Pair(encode(updated), false)
    }

    fun remainingDays(meta: MemoryLifecycleMeta, currentStoryDay: Long): Long {
        if (meta.tier == MemoryTier.CORE || meta.expiresStoryDay <= 0L) return -1L
        return (meta.expiresStoryDay - currentStoryDay.coerceAtLeast(1L)).coerceAtLeast(0L)
    }

    fun relativeStoryDayLabel(createdStoryDay: Long, currentStoryDay: Long): String {
        if (createdStoryDay <= 0L || currentStoryDay <= 0L) return "Geçmiş"
        val diff = (currentStoryDay - createdStoryDay).coerceAtLeast(0L)
        return when (diff) {
            0L -> "Bugün"
            1L -> "Dün"
            else -> "$diff gün önce"
        }
    }
}

/**
 * Pure Kotlin (<1 ms) relative-time and anaphoric reference resolver for prompts like
 * "Bana dünkinden yap", "Sabahki kahveden getir", "Geçen gün giydiğin ceket", "Aynısından bir daha".
 */
data class TemporalTarget(
    val targetStoryDayMin: Long,
    val targetStoryDayMax: Long,
    val isAnaphoricRef: Boolean
)

object RelativeTimeCueResolver {
    // Explicit Unicode word boundaries: `\b` is ASCII-only on modern JDKs (19+) and silently fails
    // after Turkish letters (e.g. "aynısı", "dünkü"), so we never rely on it here.
    private const val WB_L = """(?<![\p{L}\p{N}])"""
    private const val WB_R = """(?![\p{L}\p{N}])"""

    // "dün", "dünkü", "dünkünden", "dünkinden", "dünden", "dünü" — but NEVER "dünya" / "Dündar".
    private val YESTERDAY_REGEX = Regex("""${WB_L}dün(kü(n(ü|den|e|de))?|ki(n(i|den|e|de))?|den|e|ü)?$WB_R""")
    private val TWO_DAYS_AGO_REGEX = Regex("""${WB_L}(evvelsi\s+gün|evvelki\s+gün|önceki\s+gün)\p{L}*$WB_R""")
    private val RECENT_DAYS_REGEX = Regex("""${WB_L}(geçen\s+gün|geçen\s+sefer|geçenlerde|geçen\s+akşam|geçen\s+gece)\p{L}*$WB_R""")
    private val LAST_WEEK_REGEX = Regex("""${WB_L}(geçen\s+hafta|geçtiğimiz\s+hafta|hafta\s+başında)\p{L}*$WB_R""")
    private val N_DAYS_AGO_REGEX = Regex("""${WB_L}(\d{1,2}|bir|iki|üç|dört|beş|altı|yedi|sekiz|dokuz|on)\s+gün\s+(önce|evvel)\p{L}*$WB_R""")
    private val SAME_DAY_OR_ANAPHORIC_REGEX = Regex(
        """${WB_L}(sabahki|öğlenki|akşamki|az\s+önceki|biraz\s+önceki|bugünkü|deminki|aynısından|aynısını|aynısı|ondan\s+yap|ondan\s+getir|yine\s+ondan|bir\s+daha\s+ondan)$WB_R"""
    )

    private val NUMBER_WORDS = mapOf(
        "bir" to 1, "iki" to 2, "üç" to 3, "dört" to 4, "beş" to 5,
        "altı" to 6, "yedi" to 7, "sekiz" to 8, "dokuz" to 9, "on" to 10
    )

    fun resolveTemporalTarget(userText: String, currentStoryDay: Long): TemporalTarget? {
        if (userText.isBlank()) return null
        val t = TurkishTextAnalyzer.normalize(userText)
        val safeDay = currentStoryDay.coerceAtLeast(1L)
        fun day(offset: Long) = (safeDay - offset).coerceAtLeast(1L)

        N_DAYS_AGO_REGEX.find(t)?.let { m ->
            val raw = m.groupValues[1]
            val n = (raw.toIntOrNull() ?: NUMBER_WORDS[raw] ?: 0).toLong()
            if (n in 1L..60L) return TemporalTarget(day(n + 1L), day((n - 1L).coerceAtLeast(1L)), true)
        }
        return when {
            YESTERDAY_REGEX.containsMatchIn(t) -> TemporalTarget(day(1L), day(1L), true)
            TWO_DAYS_AGO_REGEX.containsMatchIn(t) -> TemporalTarget(day(3L), day(2L), true)
            LAST_WEEK_REGEX.containsMatchIn(t) -> TemporalTarget(day(10L), day(4L), true)
            RECENT_DAYS_REGEX.containsMatchIn(t) -> TemporalTarget(day(3L), day(1L), true)
            SAME_DAY_OR_ANAPHORIC_REGEX.containsMatchIn(t) -> TemporalTarget(day(1L), safeDay, true)
            else -> null
        }
    }
}

/**
 * Memory Engine V4 — 10/10 Unlimited Turkish-Aware Hybrid RAG, Cognitive Selectivity & Living Memory Core.
 */
object MemoryEngine {

    const val LOCAL_EMBEDDING_DIMS = 256
    const val MAX_MEMORY_PROMPT_TOKENS = 750

    enum class RagRouteMode(val badgeLabel: String) {
        HYBRID_FAST("Hibrit Hızlı"),
        CO_PILOTED_AI("Ortak Hibrit + AI"),
        CO_PILOTED_DEEP("Ortak Hibrit + AI • Derin")
    }

    /** RRF smoothing constant (Cormack et al. 2009). */
    const val RRF_K = 60f

    /**
     * One "RRF unit" = the score a single channel awards to its #1 ranked document (1 / (k + 1)).
     * EVERY additive boost in the ranker is expressed in these units so no boost can silently
     * swamp the rank-fusion signal (previously a raw +0.55 boost was ~20x the whole RRF score).
     */
    const val RRF_UNIT = 1f / (RRF_K + 1f)

    /** Temporal-cue boost ("Bana dünkinden yap"): ≈ 1.5 top-ranks. Strong, but relevance still orders matches. */
    const val TEMPORAL_BOOST = RRF_UNIT * 1.5f

    /** Max rank in the optional remote dense channel that still contributes (classic RRF depth cutoff). */
    const val REMOTE_DENSE_RANK_CUTOFF = 20

    /**
     * Reciprocal Rank Fusion (k = 60) eliminating raw score scale mismatches across
     * Semantic, Morphological BM25, and Entity/Salience channels.
     *
     * [remoteDenseRank] is an OPTIONAL 4th channel ranked only among records that carry a
     * remote (e.g. Gemini 768-dim) embedding. It never mixes vector spaces inside one sort:
     * the local channel always compares 256-dim vectors with 256-dim vectors. Records outside
     * the remote list (or beyond [REMOTE_DENSE_RANK_CUTOFF]) simply receive no contribution.
     */
    fun rrfScore(
        semRank: Int,
        lexRank: Int,
        entityRank: Int,
        boost: Float = 0f,
        remoteDenseRank: Int? = null
    ): Float {
        val remote = if (remoteDenseRank != null && remoteDenseRank in 1..REMOTE_DENSE_RANK_CUTOFF) {
            (1f / (RRF_K + remoteDenseRank)) * 0.60f
        } else 0f
        return (1f / (RRF_K + semRank.coerceAtLeast(1))) * 1.15f +
            (1f / (RRF_K + lexRank.coerceAtLeast(1))) * 1.00f +
            (1f / (RRF_K + entityRank.coerceAtLeast(1))) * 0.85f +
            remote +
            boost
    }

    /**
     * Dynamic memory token budget for the main chat prompt based on routing tier and model context window.
     * - Pure Hybrid (simplest turns): max 260 tokens
     * - Co-Piloted Hybrid + AI (standard story turns): max 750 tokens
     * - Co-Piloted Hybrid + AI + miss_queries (deep lore/secret turns): max 1050 tokens
     * - Automatically caps at 650 tokens for <= 32K context models.
     */
    fun computeDynamicMemoryBudget(
        routeMode: RagRouteMode,
        modelContextWindowTokens: Int = 128_000
    ): Int {
        val base = when (routeMode) {
            RagRouteMode.HYBRID_FAST -> 260
            RagRouteMode.CO_PILOTED_AI -> 750
            RagRouteMode.CO_PILOTED_DEEP -> 1050
        }
        return if (modelContextWindowTokens <= 32_768) minOf(base, 650) else base
    }

    /**
     * Compact ID-tagged Neural Vault prompt line with human-readable relative time & tier tags
     * for EPHEMERAL / ARC memories so both utility RAG and the main chat model resolve
     * indirect references like "Bana dünkinden yap" effortlessly.
     */
    fun formatFactForPrompt(fact: MemoryFactEntity, currentStoryDay: Long = 0L): String {
        val meta = MemoryLifecycleCodec.parse(fact.confidence)
        if (fact.userCorrected || meta.tier == MemoryTier.CORE) {
            val lockMark = if (fact.userCorrected) "★" else ""
            return "[#${fact.id}$lockMark](${fact.subject}|${fact.key}): ${fact.value}"
        }
        val relLabel = if (currentStoryDay > 0L) {
            "${MemoryLifecycleCodec.relativeStoryDayLabel(meta.createdStoryDay, currentStoryDay)} (Gün #${meta.createdStoryDay})"
        } else {
            "Gün #${meta.createdStoryDay}"
        }
        val tierLabel = if (meta.tier == MemoryTier.EPHEMERAL) "Geçici" else "Aktif Ark"
        return "[#${fact.id} | $relLabel | $tierLabel](${fact.subject}|${fact.key}): ${fact.value}"
    }

    /**
     * Ultra-short 1-line micro-index entries (~12-18 tokens each) sent to the Co-Piloted AI RAG selector.
     */
    fun formatFactMicroIndex(fact: MemoryFactEntity, currentStoryDay: Long = 0L): String {
        val meta = MemoryLifecycleCodec.parse(fact.confidence)
        val cleanVal = fact.value.replace('\n', ' ').trim()
        if (fact.userCorrected || meta.tier == MemoryTier.CORE) {
            val lock = if (fact.userCorrected) "★" else ""
            return "F${fact.id}: (${fact.subject}|${fact.key}$lock) ${cleanVal.take(74)}"
        }
        val relLabel = if (currentStoryDay > 0L) {
            "${MemoryLifecycleCodec.relativeStoryDayLabel(meta.createdStoryDay, currentStoryDay)} (Gün #${meta.createdStoryDay})"
        } else {
            "Gün #${meta.createdStoryDay}"
        }
        val tierLabel = if (meta.tier == MemoryTier.EPHEMERAL) "Geçici" else "Aktif Ark"
        return "F${fact.id}: [$relLabel | $tierLabel] (${fact.subject}|${fact.key}) ${cleanVal.take(68)}"
    }

    /**
     * Contextual significance markers, matched at WORD START (never as raw substrings) so that
     * "öp" no longer fires on "köpek", "kan" on "akan"/"kanepe", "kod" on "kodaman", etc.
     */
    private val CONTEXTUAL_SIGNIFICANCE_MARKERS: List<String> = listOf(
        "gizli", "kanıt", "dosya", "şifreli", "şifre", "sır", "güven", "ilk kez",
        "sakla", "çal", "silah", "bıçak", "tabanca", "yara", "sargı", "kan", "itiraf",
        "yemin", "anahtar", "tehlike", "ihanet", "aşk", "aşık", "öp", "ağla", "söz", "yüzük",
        "mektup", "kod", "operasyon", "görev", "plan", "zehir", "ilaç", "küs", "kavga",
        "geri çekilmedi", "elini elinin", "yirmi yıl", "makarna", "sarımsak", "kremalı",
        "mantar", "pizza", "suşi", "çorba", "biftek", "tatlı", "pasta", "salata",
        "köfte", "kebap", "omlet", "tost", "şarap", "viski", "ramen", "kahve", "espresso",
        "latte", "ceket", "mont", "elbise", "kıyafet", "gömlek", "ağrıyor", "ağrı",
        "ateş", "hasta", "sehpa", "bıraktı", "unuttu", "alerji", "fobi"
    )

    /** Word-start prefixes that share a marker's letters but carry unrelated meaning. */
    private val SIGNIFICANCE_PREFIX_EXCEPTIONS: List<String> = listOf(
        "kanepe", "kanal", "kanat", "kanaat", "kanun", "kanser", "kantin", "kano",
        "çalış", "çalkala", "planet", "planör", "kodaman", "sözlük", "montaj", "pastel"
    )

    /**
     * Code-side Contextual Selectivity safety gate (Section 2):
     * Rejects importance <= 2 and trivial mundane physical choreography ("elini masaya koydu",
     * "pencereden baktı", "derin nefes aldı", bare "yemek yedi") UNLESS the sentence carries
     * contextual plot, trust, secret, evidence, or concrete ephemeral significance.
     */
    fun isTrivialNoiseFact(key: String, value: String, importance: Int = 5): Boolean {
        if (importance <= 2) return true
        val trimmedVal = value.trim()
        if (trimmedVal.length < 4) return true

        val norm = TurkishTextAnalyzer.normalize("$key $trimmedVal")
        val normVal = TurkishTextAnalyzer.normalize(trimmedVal)

        if (CONTEXTUAL_SIGNIFICANCE_MARKERS.any { TurkishTextAnalyzer.containsWordPrefix(norm, it, SIGNIFICANCE_PREFIX_EXCEPTIONS) }) {
            return false
        }

        val trivialChoreographyPatterns = listOf(
            Regex("""\belini\s+(masaya|omzuna|dizine|cebine|beline|masanın\s+üstüne)\s+koy"""),
            Regex("""\b(pencereden|camdan|dışarı|etrafına|etrafa|yere|tavana)\s+(dışarı\s+)?bak"""),
            Regex("""\bderin\s+(bir\s+)?nefes\s+al"""),
            Regex("""\b(iç\s+çekti|arkasına\s+yaslandı|omuz\s+silkti|başını\s+salladı|kafasını\s+salladı|gözlerini\s+kırptı)\b"""),
            Regex("""\b(kapıyı\s+kapattı|kapıyı\s+açtı|sandalyeye\s+oturdu|koltuğa\s+oturdu|ayağa\s+kalktı|yürümeye\s+devam\s+etti)\b"""),
            Regex("""\b(kahvesini|çayını)\s+karıştırdı\b"""),
            Regex("""^(sadece\s+)?(gülümsedi|oturdu|baktı|gitti|geldi|yemek\s+yedi|su\s+içti|uyudu|kalktı)\.?$""")
        )
        if (trivialChoreographyPatterns.any { it.containsMatchIn(normVal) || it.containsMatchIn(norm) }) {
            return true
        }

        return false
    }

    fun formatEventMicroIndex(event: MemoryEventEntity): String {
        val parsed = LivingMemoryCodec.parse(event.description, event.importanceScore)
        return if (parsed.isStructured) {
            val snippet = parsed.narrative.replace('\n', ' ').trim().take(74)
            "E${event.id}: [${parsed.povCharacter}|${parsed.title}|${parsed.salience}/10] $snippet"
        } else {
            val snippet = parsed.narrative.replace('\n', ' ').trim().take(80)
            "E${event.id}: [Anı|${(event.importanceScore / 10).coerceIn(1, 10)}/10] $snippet"
        }
    }

    fun formatCheckpointMicroIndex(cp: MemoryCheckpointEntity): String {
        return "C${cp.checkpointNumber}: Sahne #${cp.checkpointNumber}: ${cp.summaryText.replace('\n', ' ').trim().take(84)}"
    }

    fun formatFragmentMicroIndex(frag: MemoryFragmentEntity): String {
        return "M${frag.id}: ${frag.content.replace('\n', ' ').trim().take(74)}"
    }

    /**
     * Computes a 256-dim signed feature-hashing embedding combining:
     * - Turkish morphological stems (weight 1.4)
     * - Stem bigrams for phrase semantics (weight 1.1)
     * - Character 3-grams and 4-grams for typo/morphology resilience (weight 0.45)
     * Signed hashing dramatically reduces hash-collision noise compared to unsigned modulo buckets.
     */
    fun computeTurkishFeatureEmbedding(text: String, dims: Int = LOCAL_EMBEDDING_DIMS): FloatArray {
        val vec = FloatArray(dims)
        if (text.isBlank()) return vec

        val stems = TurkishTextAnalyzer.extractStems(text)

        fun addFeature(feature: String, weight: Float) {
            val h1 = feature.hashCode()
            val h2 = (h1 * 0x5bd1e995) xor (h1 ushr 15)
            // abs(Int.MIN_VALUE) is still negative, so the old `abs(h1) % dims` could yield a
            // negative index for non-power-of-two dims. The double-mod keeps EVERY existing bucket
            // identical (stored 256-dim vectors stay valid) while being safe for any dims.
            val bucket = ((abs(h1) % dims) + dims) % dims
            val sign = if ((h2 and 1) == 0) 1f else -1f
            vec[bucket] += sign * weight
        }

        // 1. Stemmed unigrams
        for (stem in stems) {
            addFeature("w:$stem", 1.4f)
            // Char 3-grams and 4-grams within stems
            if (stem.length >= 3) {
                for (i in 0..(stem.length - 3)) {
                    addFeature("3:${stem.substring(i, i + 3)}", 0.42f)
                }
            }
            if (stem.length >= 4) {
                for (i in 0..(stem.length - 4)) {
                    addFeature("4:${stem.substring(i, i + 4)}", 0.35f)
                }
            }
        }

        // 2. Adjacent stem bigrams (captures semantic pairs like "gizli+sır", "natasha+güven")
        for (i in 0 until (stems.size - 1)) {
            addFeature("b:${stems[i]}_${stems[i + 1]}", 1.15f)
        }

        // 3. Proper nouns boost (entity anchoring)
        for (entity in TurkishTextAnalyzer.extractProperNouns(text)) {
            addFeature("e:$entity", 1.6f)
        }

        var normSq = 0f
        for (v in vec) normSq += v * v
        val norm = sqrt(normSq)
        if (norm > 1e-6f) {
            for (i in vec.indices) vec[i] /= norm
        }
        return vec
    }

    /**
     * Checks whether a turn is purely trivial small-talk where running LLM memory extraction
     * would waste tokens and produce noise.
     */
    fun isTrivialCasualMessage(query: String): Boolean {
        val clean = TurkishTextAnalyzer.normalize(query)
        val stripped = clean.replace(Regex("""[.,!?;:]+"""), " ").replace(Regex("""\s+"""), " ").trim()

        val trivialExact = setOf(
            "selam", "merhaba", "slm", "mrb", "hey", "hi",
            "nasılsın", "naber", "n'aber", "ne haber", "selam nasılsın", "merhaba nasılsın",
            "selam naber", "merhaba naber", "hey nasılsın", "slm naber",
            "günaydın", "iyi akşamlar", "iyi geceler", "iyi sabahlar",
            "tamam", "olur", "evet", "hayır", "peki", "ok", "okay",
            "eyvallah", "sağol", "teşekkürler", "teşekkür", "tşk",
            "hm", "hmm", "ah", "oh", "aa", "ehe", "e", "ya",
            "dur", "bekle", "anladım", "anla", "yok", "yok mu",
            "harika", "süper", "güzel", "iyi", "kötü", "neden",
            "devam", "devam et", "söyle", "anlat", "evet anlat"
        )
        if (trivialExact.contains(stripped)) return true

        val forceRetrievalSignals = listOf(
            "?", "hatırla", "hatırlıyor", "kimdi", "kimsin", "nerede", "nereden", "nereli",
            "geçen", "daha önce", "eskiden", "önceki", "bahsetmiştin", "demiştin",
            "anlattığım", "söylemiştim", "biliyorsun", "neydi", "neymiş", "kaçtı", "kim",
            "söz", "sır", "seviyorum", "nefret", "korkuyorum", "affet", "ihanet", "güven",
            "dün", "dünkü", "dünki", "dünkinden", "evvelsi", "sabahki", "deminki",
            "aynısından", "aynısı", "ondan yap", "ondan getir"
        )
        if (forceRetrievalSignals.any { clean.contains(it) }) return false

        val hasProperName = query.split(Regex("\\s+")).drop(1).any { word ->
            val base = word.substringBefore('\'').trim(',', '.', '!', '?')
            base.length >= 3 && base.first().isUpperCase() && base.first().isLetter()
        }
        if (hasProperName) return false

        if (stripped.length > 32) return false

        val trivialPrefixes = setOf("selam", "merhaba", "hey", "tamam", "hm", "ah", "oh")
        if (stripped.length < 20 && trivialPrefixes.any { stripped.startsWith(it) }) return true

        return false
    }

    /**
     * Routing Durum B: Returns true ONLY for short, plain, single-fact direct questions with
     * zero emotional subtext, RP narration, dialogue quotes, or ambiguous pronouns.
     * Example true: "Aiden kaç yaşındaydı?", "En sevdiği içecek neydi?", "Natasha'nın mesleği ne?"
     * Example false: "*gözlerini kısar* Dün gece ona verdiğin sözü hâlâ tutuyor musun?"
     */
    fun isDirectSingleFactQuestion(userText: String): Boolean {
        val trimmed = userText.trim()
        if (trimmed.length !in 8..76) return false
        if (trimmed.contains('*') || trimmed.contains('"') || trimmed.contains('“') || trimmed.contains('”')) return false
        if (trimmed.count { it == '?' } > 1) return false

        val clean = TurkishTextAnalyzer.normalize(trimmed)
        val complexStorySignals = listOf(
            "neden bana", "niye bana", "hissediyor", "hissetti", "sır", "gizli", "ihanet", "yalan",
            "kırgın", "güven", "korkuyor", "kork", "aşk", "seviyor", "nefret", "o gece", "dün",
            "ona ", "buna ", "şuna ", "onunla", "aramızda", "hatırlıyor musun", "söz", "yemin",
            "affet", "özür", "çatı", "kalbim", "gözler", "sessiz", "fısılda", "bakış"
        )
        if (complexStorySignals.any { clean.contains(it) }) return false

        val plainFactPatterns = listOf(
            "kaç yaşında", "yaşı kaç", "en sevdiği", "favori", "mesleği ne", "işi ne",
            "nereli", "nerede yaşıyor", "doğum günü", "adı ne", "ismi ne", "neydi", "kaçtı",
            "hangi şehir", "hangi renk", "hangi içecek", "hangi yemek", "göz rengi"
        )
        return plainFactPatterns.any { clean.contains(it) }
    }

    /**
     * Detects explicit user intent to correct, overwrite, forget, or delete an existing memory
     * (required before AI may modify or archive a ★ user-locked fact).
     */
    fun hasExplicitUserOverrideIntent(userText: String): Boolean {
        val clean = TurkishTextAnalyzer.normalize(userText)
        val overridePhrases = listOf(
            "hayır öyle değil", "hayır oyle degil", "öyle değil", "öyle değildi",
            "yanlış hatırlıyorsun", "yanlış biliyorsun", "yanlış kayıt", "yanlış kaydet",
            "değiştir", "bunu değiştir", "güncelle", "olarak güncelle", "kilidi geç",
            "unut bunu", "bunu unut", "hafızandan sil", "bunu sil",
            "kaydı sil", "düzelt", "artık değil", "tamamen yanlış", "artık yaşamıyorum",
            "artık sevmiyorum", "bıraktım", "ayrıldık"
        )
        return overridePhrases.any { clean.contains(it) }
    }

    /**
     * Heuristic gate for deciding whether a completed user+AI exchange has enough
     * factual or emotional substance to invoke automatic Neural Vault & Living Memory extraction.
     */
    fun shouldExtractMemoriesForTurn(userText: String, aiReply: String): Boolean {
        val combined = "$userText $aiReply"
        if (userText.trim().length < 6 && aiReply.trim().length < 80) return false
        if (isTrivialCasualMessage(userText) && aiReply.trim().length < 180) return false

        val lower = TurkishTextAnalyzer.normalize(combined)
        val highSignalKeywords = listOf(
            "sır", "itiraf", "söz ver", "yemin", "ihanet", "yalan", "affet", "özür",
            "seviyorum", "aşık", "nefret", "korkuyorum", "travma", "geçmiş", "ailem",
            "annem", "babam", "kardeş", "adım", "yaşım", "güç", "yetenek", "görev",
            "plan", "tehlike", "yaralan", "kurtar", "ölüm", "öldür", "sakla", "gizli",
            "güveniyorum", "güvenmiyorum", "öp", "sarıl", "ağla", "gözyaş", "kalbim",
            "asla", "unutma", "hatırla", "karar", "seçim", "anlaşma", "ittifak", "düşman",
            "makarna", "kahve", "ceket", "kıyafet", "ağrıyor", "anahtar", "sehpada", "dosya", "kanıt"
        )
        if (highSignalKeywords.any { lower.contains(it) }) return true

        // Any substantive RP turn (>= 24 chars from user and >= 140 chars from AI) is worth inspecting
        return userText.trim().length >= 24 && aiReply.trim().length >= 140
    }

    // ── Vector helpers: SINGLE source of truth (MemoryRetrievalManager & EmochiRepository delegate here) ──

    /** Parses a stored JSON float array. Returns null for blank, malformed, or non-finite payloads. */
    fun parseEmbeddingFromJson(jsonStr: String): FloatArray? {
        if (jsonStr.isBlank() || jsonStr == "[]") return null
        return try {
            val arr = JSONArray(jsonStr)
            if (arr.length() == 0) return null
            val floats = FloatArray(arr.length())
            for (i in 0 until arr.length()) {
                val v = arr.getDouble(i).toFloat()
                if (!v.isFinite()) return null
                floats[i] = v
            }
            floats
        } catch (_: Exception) {
            null
        }
    }

    fun serializeEmbeddingToJson(embedding: FloatArray): String {
        val arr = JSONArray()
        for (f in embedding) {
            arr.put(if (f.isFinite()) f.toDouble() else 0.0)
        }
        return arr.toString()
    }

    fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.isEmpty() || v2.isEmpty() || v1.size != v2.size) return 0f
        var dot = 0f
        var n1 = 0f
        var n2 = 0f
        for (i in v1.indices) {
            dot += v1[i] * v2[i]
            n1 += v1[i] * v1[i]
            n2 += v2[i] * v2[i]
        }
        val denom = sqrt(n1) * sqrt(n2)
        if (denom <= 1e-6f) return 0f
        val sim = dot / denom
        return if (sim.isFinite()) sim.coerceIn(-1f, 1f) else 0f
    }

    // ── Contradiction detection ──────────────────────────────────────────────────

    /**
     * Mutually exclusive value groups: if the old and new values each mention a DIFFERENT
     * member of the same group, they contradict ("evli" vs "boşandı", "İstanbul" vs "İzmir").
     */
    private val EXCLUSIVE_GROUPS: List<List<String>> = listOf(
        listOf("evli", "bekar", "bekâr", "boşandı", "boşanmış", "dul", "nişanlı"),
        listOf("sevgili", "ayrıldı", "ayrılmış", "eski sevgili"),
        listOf("öğrenci", "mezun"),
        listOf("yaşıyor", "hayatta", "vefat", "öldü", "ölmüş", "hayatını kaybetti"),
        listOf("dost", "düşman", "rakip"),
        listOf("müttefik", "hain"),
        listOf("saklıyor", "itiraf etti", "açıkladı"),
        listOf("işsiz", "emekli", "çalışıyor"),
        listOf(
            "doktor", "mühendis", "öğretmen", "avukat", "polis", "asker", "hemşire", "aşçı",
            "pilot", "yazılımcı", "programcı", "gazeteci", "hakim", "savcı", "dedektif", "ajan",
            "garson", "barmen", "sanatçı", "ressam", "müzisyen", "yazar", "oyuncu", "mimar",
            "eczacı", "veteriner", "dişçi", "psikolog", "akademisyen", "profesör", "tamirci"
        ),
        listOf(
            "kırmızı", "mavi", "yeşil", "sarı", "siyah", "beyaz", "mor", "pembe", "turuncu",
            "kahverengi", "gri", "lacivert", "bordo", "turkuaz"
        )
    )

    private val CITIES: List<String> = listOf(
        "istanbul", "ankara", "izmir", "bursa", "antalya", "adana", "konya", "gaziantep", "mersin",
        "eskişehir", "trabzon", "samsun", "kayseri", "diyarbakır", "erzurum", "van", "bodrum",
        "çanakkale", "edirne", "rize", "muğla", "denizli", "malatya", "sakarya", "kocaeli",
        "londra", "berlin", "paris", "new york", "tokyo", "moskova", "roma", "madrid", "barselona",
        "amsterdam", "viyana", "prag", "dubai", "los angeles", "seul", "pekin", "milano", "atina",
        "bakü", "tiflis", "kahire", "toronto", "sidney", "münih", "brüksel", "lizbon", "varşova"
    )

    /** Antonym verb pairs where the negative is NOT simply the "-me/-ma" form of the positive. */
    private val ANTONYM_PAIRS: List<Pair<String, String>> = listOf(
        "seviyor" to "nefret ediyor",
        "seviyor" to "tiksiniyor",
        "güveniyor" to "şüpheleniyor",
        "biliyor" to "bilmiyor"
    )

    private val NEGATIVE_VERB_REGEX = Regex("""(?<![\p{L}\p{N}])(\p{L}{2,}?)(m[ıiuü]yor|m[ae]z|m[ae]d[ıi]|m[ae]m[ıi]ş)(\p{L}*)""")
    private val POSITIVE_VERB_SUFFIX = Regex("""(ıyor|iyor|uyor|üyor|yor|ar|er|ır|ir|ur|ür|dı|di|du|dü|tı|ti|tu|tü|mış|miş|muş|müş)\p{L}*$""")
    private val NUMBER_WITH_UNIT_REGEX = Regex("""(\d{1,4})\s*(yaş|yaşında|kilo|kg|cm|boy|yıl|yıllık|çocu)""")

    /**
     * Detects whether a new fact value contradicts an old one. Combines:
     * 1. Mutually exclusive groups (marital/life status, professions, colors, ...).
     * 2. Cities (word + Turkish case suffix aware: "Roma'da" ✓, "romantik" ✗).
     * 3. Verb polarity flips ("seviyor" ↔ "sevmiyor", "güveniyor" ↔ "güvenmiyor").
     * 4. Explicit negation via "değil" on otherwise overlapping statements.
     * 5. Conflicting numbers on the same unit ("25 yaşında" ↔ "30 yaşında").
     */
    fun isContradictingText(oldValue: String, newValue: String): Boolean {
        val old = TurkishTextAnalyzer.normalize(oldValue)
        val new = TurkishTextAnalyzer.normalize(newValue)
        if (old.isBlank() || new.isBlank() || old == new) return false

        fun has(text: String, term: String) = TurkishTextAnalyzer.containsNounWithCaseSuffix(text, term) ||
            TurkishTextAnalyzer.containsWordPrefix(text, term)

        // 1. Mutually exclusive groups
        for (group in EXCLUSIVE_GROUPS) {
            val oldHits = group.filter { has(old, it) }.toSet()
            val newHits = group.filter { has(new, it) }.toSet()
            if (oldHits.isNotEmpty() && newHits.isNotEmpty() && oldHits.intersect(newHits).isEmpty()) return true
        }

        // 2. Cities (strict noun match)
        val oldCities = CITIES.filter { TurkishTextAnalyzer.containsNounWithCaseSuffix(old, it) }.toSet()
        val newCities = CITIES.filter { TurkishTextAnalyzer.containsNounWithCaseSuffix(new, it) }.toSet()
        if (oldCities.isNotEmpty() && newCities.isNotEmpty() && oldCities.intersect(newCities).isEmpty()) return true

        // 3a. Explicit antonym pairs
        for ((a, b) in ANTONYM_PAIRS) {
            if ((has(old, a) && has(new, b)) || (has(old, b) && has(new, a))) return true
        }

        // 3b. Verb polarity flip: "X sevmiyor" vs "X seviyor" (same root, one negated)
        if (hasPolarityFlip(old, new) || hasPolarityFlip(new, old)) return true

        // 4. Explicit "değil" negation on an otherwise overlapping statement
        val oldNeg = TurkishTextAnalyzer.containsWordPrefix(old, "değil")
        val newNeg = TurkishTextAnalyzer.containsWordPrefix(new, "değil")
        if (oldNeg != newNeg) {
            val overlap = TurkishTextAnalyzer.overlapCoefficient(
                TurkishTextAnalyzer.extractStems(old.replace("değil", " ")).toSet(),
                TurkishTextAnalyzer.extractStems(new.replace("değil", " ")).toSet()
            )
            if (overlap >= 0.6f) return true
        }

        // 5. Same unit, different number
        val oldNums = NUMBER_WITH_UNIT_REGEX.findAll(old).associate { it.groupValues[2].take(3) to it.groupValues[1] }
        val newNums = NUMBER_WITH_UNIT_REGEX.findAll(new).associate { it.groupValues[2].take(3) to it.groupValues[1] }
        for ((unit, n) in oldNums) {
            val other = newNums[unit] ?: continue
            if (other != n) return true
        }
        return false
    }

    /** True when [negText] contains a negated verb whose root appears positively in [posText]. */
    private fun hasPolarityFlip(negText: String, posText: String): Boolean {
        val posTokens = TurkishTextAnalyzer.tokenize(posText)
        for (m in NEGATIVE_VERB_REGEX.findAll(negText)) {
            val root = m.groupValues[1]
            if (root.length < 3) continue
            val positiveMatch = posTokens.any { tok ->
                if (!tok.startsWith(root) || NEGATIVE_VERB_REGEX.matches(tok)) return@any false
                val suffix = tok.substring(root.length)
                suffix.isNotEmpty() && POSITIVE_VERB_SUFFIX.containsMatchIn(suffix)
            }
            if (positiveMatch) return true
        }
        return false
    }

    fun inferFactSubject(content: String, defaultSubject: String = "kullanıcı"): String {
        val lower = TurkishTextAnalyzer.normalize(content)
        val firstWord = Regex("""^([A-ZÇĞİÖŞÜ][a-zçğıöşü]+)""").find(content.trim())?.groupValues?.getOrNull(1).orEmpty()

        return when {
            lower.startsWith("ben ") || lower.contains("benim ") || lower.contains("seviyorum") -> "kullanıcı"
            lower.startsWith("sen ") || lower.contains("senin ") || lower.contains("seviyorsun") -> "karakter"
            firstWord.isNotBlank() && firstWord.length in 3..18 && !TurkishTextAnalyzer.STOP_WORDS.contains(TurkishTextAnalyzer.lowerTr(firstWord)) -> firstWord
            else -> defaultSubject
        }
    }

    /**
     * Infers a clean 1–3 word Turkish key for a fact when tool calls omit a key.
     */
    fun inferFactKey(content: String): String {
        val lower = TurkishTextAnalyzer.normalize(content)
        return when {
            lower.contains("sır") || lower.contains("gizli") -> "Gizli Sır"
            lower.contains("söz") || lower.contains("yemin") -> "Verilen Söz"
            lower.contains("korku") || lower.contains("fobi") || lower.contains("çekiniyor") -> "Korkusu"
            lower.contains("sev") || lower.contains("hoşlan") || lower.contains("favori") -> "Tercih / Sevgi"
            lower.contains("nefret") || lower.contains("tiksin") -> "Hoşlanmadığı"
            lower.contains("güç") || lower.contains("yetenek") || lower.contains("büyü") -> "Yetenek / Güç"
            lower.contains("aile") || lower.contains("anne") || lower.contains("baba") || lower.contains("kardeş") -> "Aile / Geçmiş"
            lower.contains("yaş") || lower.contains("doğum") -> "Yaş / Kimlik"
            lower.contains("mekan") || lower.contains("ev") || lower.contains("şehir") || lower.contains("üs") -> "Konum / Mekan"
            lower.contains("güven") || lower.contains("ilişki") || lower.contains("bağ") || lower.contains("düşman") -> "İlişki Durumu"
            lower.contains("plan") || lower.contains("görev") || lower.contains("hedef") -> "Hedef / Plan"
            lower.contains("yara") || lower.contains("hastalık") || lower.contains("sağlık") -> "Fiziksel Durum"
            else -> "Önemli Bilgi"
        }
    }

    data class FilteredMemoryResult(
        val facts: List<MemoryFactEntity>,
        val events: List<MemoryEventEntity>,
        val checkpoints: List<MemoryCheckpointEntity> = emptyList(),
        val fragments: List<MemoryFragmentEntity> = emptyList(),
        val estimatedTokens: Int = 0
    )

    fun storyTimeLabel(storyDayIndex: Long, currentStoryDay: Long, fromDateStr: String = ""): String {
        if (storyDayIndex <= 1L && currentStoryDay <= 1L) return "bugün"
        if (storyDayIndex <= 0L) return "geçmişte"
        val diff = (currentStoryDay - storyDayIndex).coerceAtLeast(0L)
        if (diff == 0L) return "bugün"
        val base = TimePerceptionEngine.formatStoryDuration(diff, fromDateStr)
        return "$base önce"
    }

    fun memoryClarityTag(
        storyDayIndex: Long,
        currentStoryDay: Long,
        importanceScore: Int
    ): String {
        if (importanceScore >= 90) return "[Net Anı]"
        if (storyDayIndex <= 0L) return "[Bulanık Anı]"

        val ageDays = (currentStoryDay - storyDayIndex).coerceAtLeast(0L).toFloat()
        val decay = ageDays * 2.0f * (1.0f - importanceScore / 100f)
        val clarity = (100f - decay).coerceIn(10f, 100f)
        return when {
            clarity >= 72f -> "[Net Anı]"
            clarity >= 38f -> "[Bulanık Anı]"
            else -> "[Silik Anı]"
        }
    }

    fun buildTimestampedMemoryBlock(
        facts: List<MemoryFactEntity>,
        events: List<MemoryEventEntity>,
        currentStoryDay: Long,
        maxTokens: Int = MAX_MEMORY_PROMPT_TOKENS
    ): String {
        var tokenCount = 0
        val lines = mutableListOf<String>()
        val seenHashes = mutableSetOf<Int>()

        fun tryAdd(line: String): Boolean {
            val cost = maxOf(1, (line.length / 3.5).toInt())
            if (tokenCount + cost > maxTokens) return false
            val hash = TurkishTextAnalyzer.normalize(line).hashCode()
            if (seenHashes.contains(hash)) return false
            seenHashes.add(hash)
            lines.add(line)
            tokenCount += cost
            return true
        }

        for (f in facts) {
            tryAdd(formatFactForPrompt(f, currentStoryDay))
        }

        for (ev in events) {
            val line = LivingMemoryCodec.formatForPrompt(ev, currentStoryDay)
            tryAdd(line)
        }

        return lines.joinToString("\n")
    }

    fun buildBoundedMemoryBlock(
        facts: List<MemoryFactEntity>,
        events: List<MemoryEventEntity>,
        checkpoints: List<MemoryCheckpointEntity> = emptyList(),
        fragments: List<MemoryFragmentEntity> = emptyList(),
        maxTokens: Int = MAX_MEMORY_PROMPT_TOKENS
    ): FilteredMemoryResult {
        var currentTokenCount = 0
        val selectedFacts = mutableListOf<MemoryFactEntity>()
        val selectedEvents = mutableListOf<MemoryEventEntity>()
        val selectedCheckpoints = mutableListOf<MemoryCheckpointEntity>()
        val selectedFragments = mutableListOf<MemoryFragmentEntity>()
        val seenFingerprints = mutableListOf<Pair<String, Set<String>>>()

        fun acceptIfUnique(text: String): Boolean {
            val norm = TurkishTextAnalyzer.normalize(text)
            val stems = TurkishTextAnalyzer.extractStems(norm).toSet()
            val redundant = seenFingerprints.any { (existingNorm, existingStems) ->
                existingNorm == norm || TurkishTextAnalyzer.overlapCoefficient(existingStems, stems) >= 0.80f
            }
            if (redundant) return false
            seenFingerprints.add(norm to stems)
            return true
        }

        // 1. Neural Vault Facts (high information density per token — up to 42% of budget)
        val factsBudget = (maxTokens * 0.42f).toInt()
        for (f in facts) {
            val rep = "${f.subject} ${f.key} ${f.value}"
            val cost = maxOf(6, (rep.length / 3.5).toInt())
            if (currentTokenCount + cost <= factsBudget && acceptIfUnique(rep)) {
                selectedFacts.add(f)
                currentTokenCount += cost
            }
        }

        // 2. Living Multi-POV Memories (deep emotional & plot milestones — up to 78% cumulative)
        val eventsBudget = (maxTokens * 0.78f).toInt()
        for (e in events) {
            val searchable = LivingMemoryCodec.toSearchableText(e.description)
            val cost = maxOf(14, (searchable.length / 3.3).toInt())
            if (currentTokenCount + cost <= eventsBudget && acceptIfUnique(searchable)) {
                selectedEvents.add(e)
                currentTokenCount += cost
            }
        }

        // 3. Long-Term Scene Checkpoints (plot continuity across 100s of messages — up to 92%)
        val checkpointBudget = (maxTokens * 0.92f).toInt()
        for (cp in checkpoints) {
            val cost = maxOf(12, (cp.summaryText.length / 3.5).toInt())
            if (currentTokenCount + cost <= checkpointBudget && acceptIfUnique(cp.summaryText)) {
                selectedCheckpoints.add(cp)
                currentTokenCount += cost
            }
        }

        // 4. Searchable Archive Fragments (remaining budget)
        for (frag in fragments) {
            val cost = maxOf(8, (frag.content.length / 3.5).toInt())
            if (currentTokenCount + cost <= maxTokens && acceptIfUnique(frag.content)) {
                selectedFragments.add(frag)
                currentTokenCount += cost
            }
        }

        return FilteredMemoryResult(
            facts = selectedFacts,
            events = selectedEvents,
            checkpoints = selectedCheckpoints,
            fragments = selectedFragments,
            estimatedTokens = currentTokenCount
        )
    }

    fun buildConciseCheckpointSummary(
        checkpointNumber: Int,
        recapEvents: String,
        recapEmotions: String
    ): String {
        val cleanEvents = recapEvents.lines()
            .map { it.trim().removePrefix("-").removePrefix("•").removePrefix("*").trim() }
            .filter { it.isNotBlank() }
            .take(4)
            .joinToString("; ")
        val cleanEmotions = recapEmotions.lines()
            .map { it.trim().removePrefix("-").removePrefix("•").removePrefix("*").trim() }
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("; ")

        return buildString {
            append("Sahne #$checkpointNumber Özeti: ")
            append(cleanEvents.ifBlank { "Önemli diyalog ve sahne gelişimi." })
            if (cleanEmotions.isNotBlank()) {
                append(" | Duygusal İz: $cleanEmotions")
            }
        }
    }
}
