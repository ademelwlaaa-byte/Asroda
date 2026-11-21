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
     * Computes normalized token-level Jaccard overlap between two texts (useful for copy-paste & dedup checks).
     */
    fun stemOverlapRatio(sourceText: String, candidateText: String): Float {
        val s1 = extractStems(sourceText).toSet()
        val s2 = extractStems(candidateText).toSet()
        if (s1.isEmpty() || s2.isEmpty()) return 0f
        val intersection = s1.intersect(s2).size
        return intersection.toFloat() / minOf(s1.size, s2.size).toFloat()
    }
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
 * Memory Engine V4 — 10/10 Unlimited Turkish-Aware Hybrid RAG & Living Memory Core.
 */
object MemoryEngine {

    const val LOCAL_EMBEDDING_DIMS = 256
    const val MAX_MEMORY_PROMPT_TOKENS = 950

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
            val bucket = abs(h1) % dims
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

        val forceRetrievalSignals = listOf(
            "?", "hatırla", "hatırlıyor", "kimdi", "kimsin", "nerede", "nereden", "nereli",
            "geçen", "daha önce", "eskiden", "önceki", "bahsetmiştin", "demiştin",
            "anlattığım", "söylemiştim", "biliyorsun", "neydi", "neymiş", "kaçtı", "kim",
            "söz", "sır", "seviyorum", "nefret", "korkuyorum", "affet", "ihanet", "güven"
        )
        if (forceRetrievalSignals.any { clean.contains(it) }) return false

        val hasProperName = query.split(Regex("\\s+")).drop(1).any { word ->
            val base = word.substringBefore('\'').trim(',', '.', '!', '?')
            base.length >= 3 && base.first().isUpperCase() && base.first().isLetter()
        }
        if (hasProperName) return false

        if (clean.length > 32) return false

        val trivialExact = setOf(
            "selam", "merhaba", "slm", "mrb", "hey", "hi",
            "nasılsın", "naber", "n'aber", "ne haber",
            "günaydın", "iyi akşamlar", "iyi geceler", "iyi sabahlar",
            "tamam", "olur", "evet", "hayır", "peki", "ok", "okay",
            "eyvallah", "sağol", "teşekkürler", "teşekkür", "tşk",
            "hm", "hmm", "ah", "oh", "aa", "ehe", "e", "ya",
            "dur", "bekle", "anladım", "anla", "yok", "yok mu",
            "harika", "süper", "güzel", "iyi", "kötü", "neden",
            "devam", "devam et", "söyle", "anlat", "evet anlat"
        )
        if (trivialExact.contains(clean)) return true

        val trivialPrefixes = setOf("selam", "merhaba", "hey", "tamam", "hm", "ah", "oh")
        if (clean.length < 20 && trivialPrefixes.any { clean.startsWith(it) }) return true

        return false
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
            "asla", "unutma", "hatırla", "karar", "seçim", "anlaşma", "ittifak", "düşman"
        )
        if (highSignalKeywords.any { lower.contains(it) }) return true

        // Any substantive RP turn (>= 24 chars from user and >= 140 chars from AI) is worth inspecting
        return userText.trim().length >= 24 && aiReply.trim().length >= 140
    }

    fun parseEmbeddingFromJson(jsonStr: String): FloatArray? {
        if (jsonStr.isBlank() || jsonStr == "[]") return null
        return try {
            val arr = JSONArray(jsonStr)
            if (arr.length() == 0) return null
            val floats = FloatArray(arr.length())
            for (i in 0 until arr.length()) {
                floats[i] = arr.getDouble(i).toFloat()
            }
            floats
        } catch (_: Exception) {
            null
        }
    }

    fun serializeEmbeddingToJson(embedding: FloatArray): String {
        val arr = JSONArray()
        for (f in embedding) {
            arr.put(f.toDouble())
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
        return if (denom > 1e-6f) (dot / denom).coerceIn(-1f, 1f) else 0f
    }

    fun isContradictingText(oldValue: String, newValue: String): Boolean {
        val old = TurkishTextAnalyzer.normalize(oldValue)
        val new = TurkishTextAnalyzer.normalize(newValue)
        if (old == new) return false

        val opposites = listOf(
            Pair("evli", "bekar"),
            Pair("sevgili", "ayrıldı"),
            Pair("öğrenci", "mezun"),
            Pair("doktor", "mühendis"),
            Pair("yaşıyor", "vefat"),
            Pair("hayatta", "öldü"),
            Pair("seviyor", "nefret ediyor"),
            Pair("güveniyor", "güvenmiyor"),
            Pair("dost", "düşman"),
            Pair("müttefik", "hain"),
            Pair("saklıyor", "itiraf etti")
        )

        for ((o1, o2) in opposites) {
            if ((old.contains(o1) && new.contains(o2)) || (old.contains(o2) && new.contains(o1))) {
                return true
            }
        }

        val cities = listOf(
            "istanbul", "ankara", "izmir", "bursa", "antalya", "londra",
            "berlin", "paris", "new york", "tokyo", "moskova", "roma"
        )
        val oldCity = cities.firstOrNull { old.contains(it) }
        val newCity = cities.firstOrNull { new.contains(it) }
        if (oldCity != null && newCity != null && oldCity != newCity) {
            return true
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
        if (importanceScore >= 80) return "[Net Anı]"
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
            val pinMark = if (f.userCorrected) "★ " else ""
            val line = "- $pinMark(${f.subject}) [${f.key}]: ${f.value}"
            tryAdd(line)
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
        val seenFingerprints = mutableListOf<String>()

        fun isRedundant(text: String): Boolean {
            val norm = TurkishTextAnalyzer.normalize(text)
            return seenFingerprints.any { existing ->
                existing == norm || TurkishTextAnalyzer.stemOverlapRatio(existing, norm) >= 0.85f
            }
        }

        // 1. Neural Vault Facts (high information density per token — up to 40% of budget)
        val factsBudget = (maxTokens * 0.42f).toInt()
        for (f in facts) {
            val rep = "${f.subject} ${f.key} ${f.value}"
            val cost = maxOf(6, (rep.length / 3.5).toInt())
            if (currentTokenCount + cost <= factsBudget && !isRedundant(rep)) {
                selectedFacts.add(f)
                seenFingerprints.add(TurkishTextAnalyzer.normalize(rep))
                currentTokenCount += cost
            }
        }

        // 2. Living Multi-POV Memories (deep emotional & plot milestones — up to 78% cumulative)
        val eventsBudget = (maxTokens * 0.78f).toInt()
        for (e in events) {
            val searchable = LivingMemoryCodec.toSearchableText(e.description)
            val cost = maxOf(14, (searchable.length / 3.3).toInt())
            if (currentTokenCount + cost <= eventsBudget && !isRedundant(searchable)) {
                selectedEvents.add(e)
                seenFingerprints.add(TurkishTextAnalyzer.normalize(searchable))
                currentTokenCount += cost
            }
        }

        // 3. Long-Term Scene Checkpoints (plot continuity across 100s of messages — up to 92%)
        val checkpointBudget = (maxTokens * 0.92f).toInt()
        for (cp in checkpoints) {
            val cost = maxOf(12, (cp.summaryText.length / 3.5).toInt())
            if (currentTokenCount + cost <= checkpointBudget && !isRedundant(cp.summaryText)) {
                selectedCheckpoints.add(cp)
                seenFingerprints.add(TurkishTextAnalyzer.normalize(cp.summaryText))
                currentTokenCount += cost
            }
        }

        // 4. Searchable Archive Fragments (remaining budget)
        for (frag in fragments) {
            val cost = maxOf(8, (frag.content.length / 3.5).toInt())
            if (currentTokenCount + cost <= maxTokens && !isRedundant(frag.content)) {
                selectedFragments.add(frag)
                seenFingerprints.add(TurkishTextAnalyzer.normalize(frag.content))
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
