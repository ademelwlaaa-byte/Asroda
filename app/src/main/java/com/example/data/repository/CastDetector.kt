package com.example.data.repository

import android.content.Context
import com.example.util.AppFeatureFlags
import com.squareup.moshi.Json
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * R8-safe DTO for Micro-AI (ExecutionMode.UTILITY) Cast Extraction & Validation.
 */
data class MicroAiCastMemberDto(
    @Json(name = "name") val name: String = "",
    @Json(name = "is_person") val isPerson: Boolean = true,
    @Json(name = "aliases") val aliases: List<String> = emptyList(),
    @Json(name = "role") val role: String = "Yan Karakter",
    @Json(name = "relation") val relation: String = "",
    @Json(name = "age") val age: Int? = null,
    @Json(name = "in_scene") val inScene: Boolean = true
)

/**
 * R8-safe wrapper DTO for Micro-AI Cast Extraction JSON response.
 */
data class MicroAiCastResponse(
    @Json(name = "cast") val cast: List<MicroAiCastMemberDto> = emptyList(),
    @Json(name = "rejected") val rejected: List<String> = emptyList()
)

data class CastExtractionResult(
    val members: List<MicroAiCastMemberDto>,
    val rejected: Set<String> = emptySet(),
    val usedMicroAi: Boolean = false
)

/**
 * Hybrid Cast Member Detection & Alias Resolution Engine (Sistem #16 — 10/10).
 *
 * Layer 1 (<1 ms local Turkish pre-filter & morphological stemmer):
 * - normalizeTurkishEntityName: "Leyla'nın", "Leyla'yla", "Leyla'ya" -> "Leyla"
 * - Title+Name, Kinship/Role+Name, Two-Word Full Name, Speech & Action cues
 * - Expanded Turkish blacklist (cities, landmarks, days, months, sentence-starter verbs/adverbs)
 * - Smart Micro-AI gate: shouldInvokeMicroAiCastExtraction() skips trivial short messages
 *
 * Layer 2 (Real Micro-AI / ExecutionMode.UTILITY validator, temperature=0.15, max_tokens=240):
 * - extractAndValidateCastWithMicroAi(): extracts indirect literary characters ("yaşlı saatçi Kadir")
 *   and filters false positives ("Galata", "Karanlık") with silent Layer 1 fallback on offline/error/off.
 * - filterActiveInSceneCastMembers(): strictly caps prompt injection to max 4 active in-scene characters.
 */
object CastDetectorEngine {

    private val TR_LOCALE = Locale.forLanguageTag("tr-TR")
    private const val ALIAS_PREFIX = "alias:"

    private val DEFAULT_FALLBACK_BLACKLIST = setOf(
        "bugün", "yarın", "dün", "sabah", "akşam", "gece", "gündüz", "şimdi", "sonra", "önce", "burada", "orada", "şurada",
        "burası", "orası", "şurası", "çünkü", "sadece", "oysa", "lakin", "fakat", "ayrıca", "özellikle", "nitekim", "ancak",
        "belki", "böyle", "şöyle", "öyle", "peki", "tamam", "pekala", "lütfen", "efendim", "kullanıcı", "karakter", "insan",
        "adam", "kadın", "çocuk", "genç", "yaşlı", "polis", "garson", "hoca", "öğretmen", "müşteri", "sürücü", "asker", "kral",
        "kraliçe", "prens", "prenses", "profesör", "amiral", "komutan", "yüzbaşı", "kaptan", "komiser", "doktor",
        "istanbul", "ankara", "izmir", "türkiye", "bursa", "antalya", "adana", "konya", "gaziantep", "trabzon", "eskişehir",
        "mersin", "galata", "beyoğlu", "taksim", "kadıköy", "beşiktaş", "üsküdar", "karaköy", "ortaköy", "nişantaşı", "boğaz",
        "boğaziçi", "kapadokya", "karadeniz", "akdeniz", "ege", "marmara", "anadolu", "avrupa", "asya", "londra", "paris",
        "roma", "berlin", "viyana", "atina", "kahire", "tokyo", "moskova", "kuzey", "güney", "doğu", "batı",
        "dünya", "güneş", "ay", "yıldız", "sokak", "cadde", "oda", "masa", "sandalye", "kapı", "pencere", "araba", "telefon",
        "kitap", "kalem", "kılıç", "bıçak", "kalkan", "ateş", "su", "toprak", "hava", "ruh", "ışık", "karanlık", "ağaç",
        "orman", "deniz", "göl", "nehir", "dağ", "taş", "kaya", "ev", "saray", "şato", "kule", "zindan", "okul", "hastane",
        "otel", "meyhane", "restoran", "kafe", "dükkan", "pazar", "şehir", "köy", "ülke", "krallık", "imparatorluk",
        "lonca", "birlik", "takım", "grup", "sürü", "ordu", "donanma", "meclis", "sessizlik", "yağmur", "rüzgar", "fırtına",
        "sis", "gölge", "şafak", "alacakaranlık", "mehtap", "kış", "yaz", "sonbahar", "ilkbahar",
        "pazartesi", "salı", "çarşamba", "perşembe", "cuma", "cumartesi",
        "ocak", "şubat", "mart", "nisan", "mayıs", "haziran", "temmuz", "ağustos", "eylül", "ekim", "kasım", "aralık",
        "evvel", "ahir", "üstelik", "sanki", "hatta", "madem", "meğer", "güya", "zira", "aksi",
        "yine", "yeniden", "hemen", "derhal", "biraz", "çok", "fazla", "az", "hiç", "tüm", "bütün", "her", "kendi", "biri",
        "diğeri", "başka", "hangi", "nasıl", "neden", "niçin", "nere", "nerede", "nereden", "nereye", "kim", "kime", "kimden",
        "kimi", "gözü", "gözleri", "sesi", "yüzü", "adımları", "elleri", "bakışı", "dudakları", "başını", "elini", "arkası",
        "zorba", "soğuk", "sıcak", "büyük", "küçük", "uzun", "kısa", "sessiz", "hızlı", "yavaş", "güçlü", "zayıf",
        "evet", "hayır", "işte", "galiba", "sanırım", "bence", "sence", "zaten", "aslında", "gerçekten", "sonunda",
        "baktı", "geldi", "gitti", "dedi", "sordu", "durdu", "kalktı", "oturdu", "yürüdü", "koştu", "döndü", "gülümsedi",
        "fısıldadı", "bağırdı", "sus", "bekle", "bak", "gel", "git", "dinle", "bırak", "yapma", "olmaz", "yeter",
        "merhaba", "selam", "günaydın", "teşekkürler", "affedersin", "pardon", "özür", "keşke", "acaba", "nihayet",
        "asla", "kesinlikle", "muhtemelen", "herhalde", "elbette", "tabii", "haydi", "hadi", "aman", "vay", "hey"
    )

    private val GENERIC_EXTRAS_BLACKLIST = setOf(
        "garson", "zorba", "zorba çocuk", "okul zorbası", "sınıf zorbası", "lise zorbası", "zorba öğrenci",
        "çocuk", "adam", "kadın", "genç", "yaşlı", "kız", "oğlan", "delikanlı", "polis", "polis memuru",
        "müşteri", "yoldan geçen", "şoför", "taksici", "nöbetçi", "bekçi", "güvenlik", "güvenlik görevlisi",
        "biri", "herkes", "hizmetli", "tezgahtar", "kasiyer", "barmen", "muhabir", "paparazzi", "serseri",
        "öğrenci", "lise öğrencisi", "sınıf arkadaşı", "okul arkadaşı", "taraftar", "hayran", "seyirci",
        "yabancı", "figüran", "kalabalık", "grup", "çete üyesi", "koruma", "görevli", "memur", "kurye",
        "bully", "school bully", "random bully", "jock", "student", "classmate", "waiter", "waitress",
        "bartender", "cashier", "guard", "security guard", "officer", "cop", "driver", "reporter",
        "stranger", "passerby", "bystander", "thug", "goon", "crowd", "extra", "npc", "kid", "boy", "girl",
        "guy", "man", "woman"
    )

    private val GENERIC_EXTRA_ADJECTIVES = setOf(
        "zorba", "kaba", "sinirli", "öfkeli", "uzun", "kısa", "iri", "şişman", "zayıf", "genç", "yaşlı",
        "küçük", "büyük", "yabancı", "tanımadık", "bilinmeyen", "isimsiz", "rastgele", "sıradan", "kötü",
        "alaycı", "küstah", "sarhoş", "serseri", "esmer", "sarışın", "kızıl", "maskeli", "silahlı",
        "okul", "sınıf", "lise", "koridor", "sokak", "mahalle", "bar", "kulüp", "restoran", "kafe",
        "angry", "rude", "tall", "big", "random", "unnamed", "unknown", "generic", "minor", "school",
        "high", "hallway", "drunk", "masked", "armed", "passing", "background"
    )

    private val GENERIC_EXTRA_NOUNS = setOf(
        "zorba", "zorbası", "çocuk", "çocuğu", "öğrenci", "öğrencisi", "arkadaş", "arkadaşı",
        "adam", "kadın", "kız", "oğlan", "genç", "delikanlı", "garson", "barmen", "kasiyer",
        "şoför", "taksici", "polis", "memur", "memuru", "güvenlik", "görevli", "görevlisi",
        "bekçi", "nöbetçi", "koruma", "hizmetli", "tezgahtar", "müşteri", "muhabir", "paparazzi",
        "taraftar", "hayran", "seyirci", "yabancı", "serseri", "haydut", "çete", "figüran", "kurye",
        "bully", "kid", "boy", "girl", "guy", "man", "woman", "student", "classmate", "jock",
        "waiter", "waitress", "bartender", "guard", "officer", "cop", "driver", "stranger",
        "thug", "goon", "extra", "npc", "reporter", "fan", "customer", "clerk"
    )

    private val MINOR_ROLE_KEYWORDS = setOf(
        "figüran", "arka plan", "geçici figüran", "isimsiz", "rastgele", "yoldan geçen",
        "background", "unnamed", "extra", "random extra", "minor extra"
    )

    private val ENGLISH_NON_NAME_BLACKLIST = setOf(
        "however", "suddenly", "chapter", "scene", "system", "morning", "evening", "night",
        "today", "tomorrow", "yesterday", "silence", "shadow", "voice", "door", "window",
        "room", "street", "city", "tower", "academy", "office", "hospital", "coffee", "rain",
        "maybe", "actually", "anyway", "nothing", "something", "someone", "everyone", "nobody",
        "finally", "quietly", "softly", "slowly", "really", "always", "never", "still", "already",
        "because", "although", "between", "through", "without", "assistant", "narrator", "character",
        "memory", "secret", "trust", "promise", "moment", "story", "world", "universe", "flashback",
        "epilogue", "prologue", "part", "section", "note", "update", "status", "mood", "emotion",
        "hello", "sorry", "please", "thanks", "thank", "welcome", "goodbye", "listen", "wait",
        "shield", "hydra", "avengers", "nevermore", "jericho", "manhattan", "brooklyn", "queens",
        "new york", "central park", "times square", "stark tower", "avengers tower"
    )

    private val NON_PERSON_SUFFIX_WORDS = setOf(
        // Turkish place, building, institution & organization suffixes
        "kulesi", "kule", "akademisi", "akademi", "lisesi", "üniversitesi", "hastanesi", "kliniği",
        "sokağı", "caddesi", "bulvarı", "meydanı", "köprüsü", "parkı", "bahçesi", "sarayı", "kalesi",
        "malikanesi", "malikânesi", "köşkü", "konağı", "sarnıcı", "konseyi", "teşkilatı", "örgütü",
        "birliği", "bakanlığı", "binası", "merkezi", "üssü", "laboratuvarı", "odası", "salonu",
        "mahzeni", "kripti", "ormanı", "dağı", "denizi", "nehri", "gölü", "adası", "körfezi",
        "limanı", "iskelesi", "istasyonu", "havaalanı", "oteli", "kafesi", "restoranı", "barı",
        "kulübü", "vakfı", "şirketi", "holdingi", "dairesi", "bölümü", "komitesi", "meclisi",
        "derneği", "imparatorluğu", "krallığı", "cumhuriyeti", "devleti", "eyaleti", "ilçesi",
        "mahallesi", "semti", "çarşısı", "pazarı", "tesisi", "protokolü", "programı", "projesi",
        "dosyası", "defteri", "kolyesi", "yüzüğü", "kılıcı", "taşı", "mührü", "nokta", "noktası",
        // English place, building & organization suffixes
        "tower", "academy", "hall", "park", "street", "avenue", "road", "boulevard", "building",
        "center", "centre", "base", "lab", "laboratory", "agency", "council", "order", "guild",
        "kingdom", "empire", "city", "town", "village", "island", "bridge", "hotel", "cafe",
        "station", "airport", "hospital", "school", "university", "college", "square", "plaza",
        "mansion", "castle", "palace", "room", "house", "corp", "corporation", "industries",
        "foundation", "institute", "protocol", "project", "program", "department", "division",
        "cistern", "fortress", "citadel", "sanctuary", "temple", "church", "cathedral", "library"
    )

    private val CAST_ROLE_EN_TO_TR: Map<String, String> = mapOf(
        "side character" to "Yan Karakter",
        "supporting character" to "Yan Karakter",
        "character" to "Yan Karakter",
        "friend" to "Arkadaş",
        "best friend" to "En Yakın Dost",
        "close friend" to "Yakın Dost",
        "ally" to "Müttefik",
        "enemy" to "Düşman",
        "rival" to "Rakip",
        "villain" to "Düşman",
        "antagonist" to "Rakip / Düşman",
        "stranger" to "Yabancı",
        "acquaintance" to "Tanıdık",
        "doctor" to "Doktor",
        "physician" to "Doktor",
        "healer" to "Şifacı",
        "nurse" to "Hemşire",
        "detective" to "Dedektif",
        "investigator" to "Müfettiş",
        "agent" to "Ajan",
        "spy" to "Casus",
        "captain" to "Kaptan",
        "commander" to "Komutan",
        "leader" to "Lider",
        "director" to "Direktör",
        "boss" to "Patron",
        "mentor" to "Akıl Hocası",
        "teacher" to "Öğretmen",
        "professor" to "Profesör",
        "headmaster" to "Müdür",
        "principal" to "Müdür",
        "partner" to "Ortak",
        "colleague" to "İş Arkadaşı",
        "neighbor" to "Komşu",
        "lover" to "Sevgili",
        "ex-lover" to "Eski Sevgili",
        "ex" to "Eski Sevgili",
        "spouse" to "Eş",
        "wife" to "Eş",
        "husband" to "Eş",
        "sibling" to "Kardeş",
        "brother" to "Erkek Kardeş",
        "sister" to "Kız Kardeş",
        "mother" to "Anne",
        "father" to "Baba",
        "guardian" to "Vasi",
        "caretaker" to "Bakıcı",
        "bodyguard" to "Koruma",
        "assistant" to "Yardımcı",
        "scientist" to "Bilim İnsanı",
        "engineer" to "Mühendis",
        "inventor" to "Mucit",
        "journalist" to "Gazeteci",
        "lawyer" to "Avukat",
        "merchant" to "Tüccar",
        "warrior" to "Savaşçı",
        "knight" to "Şövalye",
        "mage" to "Büyücü",
        "witch" to "Cadı",
        "prince" to "Prens",
        "princess" to "Prenses",
        "king" to "Kral",
        "queen" to "Kraliçe"
    )

    /**
     * Returns true when [candidateName] clearly refers to a place, building, organization,
     * institution, artifact, or English non-name token (e.g., "Galata Kulesi", "Nevermore Akademisi",
     * "Stark Tower", "Kurucular Konseyi", "Kör Nokta", "However").
     */
    fun isNonPersonPlaceOrOrganization(candidateName: String): Boolean {
        val clean = normalizeTurkishEntityName(candidateName).ifBlank { candidateName.trim() }
        val lower = clean.lowercase(TR_LOCALE).replace(Regex("""\s+"""), " ").trim()
        if (lower.length < 2) return true
        if (lower in ENGLISH_NON_NAME_BLACKLIST) return true
        if (lower.any { it.isDigit() }) return true // e.g. "Denek 7-A", "Bölüm 3", "Kat 4"

        val tokens = lower.split(" ").filter { it.isNotBlank() }
        if (tokens.isEmpty() || tokens.size > 4) return true
        if (tokens.size == 1 && (tokens[0] in ENGLISH_NON_NAME_BLACKLIST || tokens[0] in NON_PERSON_SUFFIX_WORDS)) {
            return true
        }
        // Multi-word candidate ending in a place/building/org/artifact noun -> NOT a person!
        if (tokens.size >= 2 && (tokens.last() in NON_PERSON_SUFFIX_WORDS || tokens.first() in NON_PERSON_SUFFIX_WORDS)) {
            return true
        }
        // Pure abbreviation / acronym (e.g. "S.H.I.E.L.D.", "FBI", "CIA", "NATO")
        val lettersOnly = clean.replace(Regex("""[^A-Za-zÇĞİÖŞÜçğıöşü]"""), "")
        if (lettersOnly.length in 2..8 && lettersOnly.all { it.isUpperCase() } && clean.contains('.')) {
            return true
        }
        return false
    }

    /** Normalizes any leaked English cast role into crisp Turkish. */
    fun normalizeTurkishCastRole(rawRole: String, fallback: String = "Yan Karakter"): String {
        val clean = rawRole.trim().replace(Regex("""\s+"""), " ")
        if (clean.isBlank()) return fallback
        val low = clean.lowercase(Locale.ROOT)
        CAST_ROLE_EN_TO_TR[low]?.let { return it }
        if (TurkishLanguageGuard.isEnglishDominant(clean)) return fallback
        return clean.take(40)
    }

    /** Normalizes any leaked English cast relation into crisp Turkish. */
    fun normalizeTurkishCastRelation(rawRelation: String): String {
        val clean = rawRelation.trim().replace(Regex("""\s+"""), " ")
        if (clean.isBlank()) return ""
        val fromRel = TurkishLanguageGuard.normalizeTurkishRelation(clean)
        if (fromRel.isNotBlank()) return fromRel
        val low = clean.lowercase(Locale.ROOT)
        CAST_ROLE_EN_TO_TR[low]?.let { return it }
        return if (TurkishLanguageGuard.isEnglishDominant(clean)) "" else clean.take(40)
    }

    /**
     * Returns true when [candidateName] (or its [role]) represents an unnamed generic/background extra
     * such as "Zorba Çocuk", "Okul Zorbası", "Garson", "Sınıf Arkadaşı", "School Bully", etc.
     * Named story characters (e.g., "Gwen Stacy", "Flash Thompson", "Kadir") return false so the
     * cast list can organically grow as meaningful named characters enter the chat.
     */
    fun isGenericBackgroundExtra(candidateName: String, role: String = ""): Boolean {
        val clean = normalizeTurkishEntityName(candidateName).ifBlank { candidateName.trim() }
        val lower = clean.lowercase(TR_LOCALE).replace(Regex("""\s+"""), " ").trim()
        if (lower.length < 2) return true
        if (lower in GENERIC_EXTRAS_BLACKLIST) return true

        val roleLower = role.trim().lowercase(TR_LOCALE)
        if (roleLower.isNotBlank() && MINOR_ROLE_KEYWORDS.any { roleLower.contains(it) }) {
            return true
        }

        val tokens = lower.split(" ").filter { it.isNotBlank() }
        if (tokens.isEmpty()) return true

        // Multi-word generic phrases like "zorba çocuk", "kaba lise öğrencisi", "angry school bully"
        val allGenericTokens = tokens.all { tok ->
            tok in GENERIC_EXTRAS_BLACKLIST ||
                tok in GENERIC_EXTRA_ADJECTIVES ||
                tok in GENERIC_EXTRA_NOUNS ||
                tok in DEFAULT_FALLBACK_BLACKLIST
        }
        val containsGenericNoun = tokens.any { it in GENERIC_EXTRA_NOUNS || it in GENERIC_EXTRAS_BLACKLIST }
        if (allGenericTokens && containsGenericNoun) return true

        // Single-word generic archetype
        if (tokens.size == 1 && (tokens[0] in GENERIC_EXTRA_NOUNS || tokens[0] in GENERIC_EXTRA_ADJECTIVES)) {
            return true
        }
        return false
    }

    private val HONORIFICS = listOf(
        "Bey", "Hanım", "Efendi", "Ağa", "Usta", "Abi", "Abla", "Komutan",
        "Kaptan", "Doktor", "Hazretleri", "Paşa", "Hoca", "Teyze", "Amca", "Dayı", "Yenge", "Dede", "Peder"
    )

    private val TITLES = listOf(
        "Bay", "Bayan", "Doktor", "Prof", "Profesör", "Dr.", "Komiser", "Başkomiser", "Yüzbaşı", "Kaptan", "Amiral",
        "Komutan", "Lord", "Prens", "Prenses", "Kral", "Kraliçe", "Usta", "Hoca", "Savaşçı",
        "Büyücü", "Şövalye", "Mimar", "Aziz", "Üstat", "Gözcü", "Dedektif", "Ajan", "Müfettiş",
        "Savcı", "Hâkim", "Teğmen", "Binbaşı", "Albay", "General", "Sultan", "Paşa", "Dük", "Kont",
        "Baron", "Barones", "Rahip", "Cadı", "Gölge", "Muhafız", "Hizmetkar", "Tüccar", "Eczacı", "Leydi",
        "Avukat", "Hemşire"
    )

    private val DIALOGUE_ACTION_VERBS = listOf(
        "dedi", "sordu", "fısıldadı", "bağırdı", "haykırdı", "mırıldandı",
        "yanıtladı", "ekledi", "güldü", "sırıttı", "iç geçirdi", "kaşlarını çattı",
        "omuz silkti", "seslendi", "atıldı", "cevap verdi"
    )

    private val KINSHIP_OR_ROLE_CUES = listOf(
        "kardeşim", "kardeşi", "abim", "abisi", "ablam", "ablası", "kuzenim", "kuzeni",
        "amcam", "amcası", "dayım", "dayısı", "teyzem", "teyzesi", "halam", "halası",
        "annem", "annesi", "babam", "babası", "dedem", "dedesi", "ninem", "ninesi",
        "arkadaşım", "arkadaşı", "dostum", "dostu", "komşum", "komşusu",
        "sevgilim", "sevgilisi", "eski sevgilim", "eski sevgilisi", "nişanlım", "nişanlısı",
        "eşim", "eşi", "patronum", "patronu", "ortağım", "ortağı", "saatçi", "terzi",
        "fırıncı", "avukat", "komiser", "doktor", "kaptan", "yardımcım", "yardımcısı"
    )

    @Volatile
    private var cachedBlacklist: Set<String>? = null
    private val blacklistLock = Any()

    fun getBlacklist(context: Context? = null): Set<String> {
        cachedBlacklist?.let { return it }
        if (context == null) return DEFAULT_FALLBACK_BLACKLIST

        return synchronized(blacklistLock) {
            cachedBlacklist?.let { return@synchronized it }
            try {
                val jsonStr = context.assets.open("cast_blacklist.json").bufferedReader().use { it.readText() }
                val arr = JSONArray(jsonStr)
                val set = mutableSetOf<String>()
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i)?.trim()?.lowercase(TR_LOCALE)
                    if (!item.isNullOrBlank()) set.add(item)
                }
                val result = set + DEFAULT_FALLBACK_BLACKLIST
                cachedBlacklist = result
                result
            } catch (_: Exception) {
                DEFAULT_FALLBACK_BLACKLIST
            }
        }
    }

    fun addToBlacklist(context: Context? = null, name: String) {
        val clean = normalizeTurkishEntityName(name).ifBlank { name.trim() }.lowercase(TR_LOCALE)
        if (clean.isBlank()) return
        synchronized(blacklistLock) {
            val current = getBlacklist(context)
            cachedBlacklist = current + clean + name.trim().lowercase(TR_LOCALE)
        }
    }

    // Türkçe hâl/iyelik eklerinin listesi — apostrof sonrası parça için
    private val TURKISH_INFLECTION_SUFFIXES = setOf(
        "nın", "nin", "nun", "nün", "ın", "in", "un", "ün",
        "a", "e", "ya", "ye",
        "da", "de", "ta", "te",
        "dan", "den", "tan", "ten",
        "nda", "nde", "ndan", "nden",
        "yla", "yle", "la", "le",
        "ı", "i", "u", "ü", "yı", "yi", "yu", "yü",
        "m", "ım", "im", "um", "üm",
        "n", "s", "si", "sı", "su", "sü",
        "lar", "ler", "ları", "leri",
        "lara", "lere", "lardan", "lerden", "larda", "lerde",
        "ca", "ce", "ça", "çe"
    )

    private const val WB_L = """(?<![\p{L}\p{N}])"""
    private const val WB_R = """(?![\p{L}\p{N}])"""

    /**
     * Proper name stemmer: apostrof varsa Türkçe hal/iyelik eklerini doğrular ve soyar.
     * Örnek: "Leyla'nın" / "Leyla’yla" / "Leyla'ya" -> "Leyla", "Zar'thul" -> "Zar'thul".
     */
    fun stemTurkishProperName(rawName: String): String {
        val clean = rawName
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("«", "»")
            .removeSurrounding("“", "”")
            .trimEnd('.', ',', ';', ':', '!', '?')
            .trim()
        if (clean.isBlank()) return ""

        val apostropheRegex = Regex("""['‘’`]""")
        if (apostropheRegex.containsMatchIn(clean)) {
            // Support multi-word names where last token carries the suffix: "Aiden Blackwood'un" -> "Aiden Blackwood"
            val tokens = clean.split(Regex("""\s+""")).filter { it.isNotBlank() }
            if (tokens.size > 1 && apostropheRegex.containsMatchIn(tokens.last())) {
                val stemmedLast = stemSingleToken(tokens.last(), apostropheRegex)
                return (tokens.dropLast(1) + stemmedLast).joinToString(" ").trim()
            }
            return stemSingleToken(clean, apostropheRegex)
        }
        return clean
    }

    private fun stemSingleToken(token: String, apostropheRegex: Regex): String {
        val parts = token.split(apostropheRegex, limit = 2)
        val base = parts[0].trim()
        val suffix = if (parts.size > 1) parts[1].trim().lowercase(TR_LOCALE) else ""
        return if (suffix in TURKISH_INFLECTION_SUFFIXES && base.length >= 2) base else token
    }

    /**
     * Canonical Turkish entity normalizer (Sistem #16):
     * Strips brackets/quotes, removes prefix titles & suffix honorifics, and stems Turkish
     * apostrophe case/possessive suffixes ("Leyla'nın", "Leyla'yla", "Leyla'ya" -> "Leyla").
     */
    fun normalizeTurkishEntityName(raw: String): String {
        val stripped = raw
            .replace(Regex("""\[.*?\]"""), " ")
            .replace(Regex("""\(.*?\)"""), " ")
            .replace(Regex("""["«»“”*`_~]"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .trimEnd('.', ',', ';', ':', '!', '?')
        if (stripped.isBlank()) return ""
        val (canonical, _) = extractCanonicalNameAndRole(stripped)
        return stemTurkishProperName(canonical).replace(Regex("""\s+"""), " ").trim()
    }

    /**
     * Normalizes aliases such as "Doktor Kerem", "Kerem Bey", "Kerem" to root candidate name.
     */
    fun extractCanonicalNameAndRole(rawCandidate: String): Pair<String, String?> {
        val trimmed = rawCandidate.trim()
        var roleFromTitle: String? = null

        for (title in TITLES) {
            if (trimmed.startsWith("$title ", ignoreCase = true)) {
                val root = trimmed.substring(title.length).trim()
                roleFromTitle = when {
                    title.contains("Doktor", ignoreCase = true) || title.contains("Dr.", ignoreCase = true) -> "Doktor"
                    title.contains("Komutan", ignoreCase = true) || title.contains("Kaptan", ignoreCase = true) -> "Komutan"
                    title.contains("Komiser", ignoreCase = true) -> "Komiser"
                    else -> title
                }
                return Pair(stemTurkishProperName(root), roleFromTitle)
            }
        }

        for (hon in HONORIFICS) {
            if (trimmed.endsWith(" $hon", ignoreCase = true)) {
                val root = trimmed.substring(0, trimmed.length - hon.length).trim()
                roleFromTitle = when (hon) {
                    "Doktor" -> "Doktor"
                    "Komutan", "Kaptan" -> "Komutan"
                    else -> null
                }
                return Pair(stemTurkishProperName(root), roleFromTitle)
            }
        }

        return Pair(stemTurkishProperName(trimmed), null)
    }

    data class CandidateScore(
        val isSignificant: Boolean,
        val role: String,
        val confidence: Int,
        val difficultyMultiplier: Double,
        val reason: String
    )

    fun scoreCandidate(
        candidateName: String,
        sceneContext: String,
        appearanceCount: Int = 1,
        inferredRole: String = "Yan Karakter",
        context: Context? = null
    ): CandidateScore {
        val (canonical, titleRole) = extractCanonicalNameAndRole(candidateName)
        val clean = canonical.ifBlank { candidateName.trim() }
        val lower = clean.lowercase(TR_LOCALE)
        val blacklist = getBlacklist(context)

        if (isGenericBackgroundExtra(clean, inferredRole) ||
            isNonPersonPlaceOrOrganization(clean) ||
            blacklist.contains(lower) ||
            clean.length < 2
        ) {
            return CandidateScore(
                isSignificant = false,
                role = "Figüran",
                confidence = 15,
                difficultyMultiplier = 1.0,
                reason = "Genel figüran, mekan/kurum veya kara liste eşleşmesi."
            )
        }

        var confidence = 40
        val role = titleRole ?: inferredRole

        if (appearanceCount >= 2) {
            confidence = maxOf(confidence, 85)
        }

        val escapedClean = Regex.escape(lower)
        val lowerScene = sceneContext.lowercase(TR_LOCALE)
        val hasSpeechCue = DIALOGUE_ACTION_VERBS.any { verb ->
            val v = Regex.escape(verb)
            Regex("""${WB_L}$escapedClean\s+$v$WB_R""").containsMatchIn(lowerScene) ||
                Regex("""${WB_L}$v\s+$escapedClean$WB_R""").containsMatchIn(lowerScene)
        } || Regex("""["»”]\s*,?\s*$escapedClean$WB_R""").containsMatchIn(lowerScene)
        if (hasSpeechCue) {
            confidence += 30
        }

        if (titleRole != null) {
            confidence += 20
        }

        val isProperCase = clean.firstOrNull()?.isUpperCase() == true && clean.length in 3..25
        if (isProperCase) {
            confidence += 15
        }

        confidence = confidence.coerceIn(0, 100)

        val mult = when {
            role.contains("Doktor", ignoreCase = true) || role.contains("Komutan", ignoreCase = true) || role.contains("Müdür", ignoreCase = true) -> 0.75
            role.contains("Düşman", ignoreCase = true) || role.contains("Rakip", ignoreCase = true) -> 0.50
            role.contains("Arkadaş", ignoreCase = true) || role.contains("Dost", ignoreCase = true) -> 1.15
            else -> 1.0
        }

        val isSignificant = confidence >= 50
        return CandidateScore(
            isSignificant = isSignificant,
            role = if (role.isNotBlank() && role != "Yan Karakter") role else "Yan Karakter",
            confidence = confidence,
            difficultyMultiplier = mult,
            reason = if (hasSpeechCue) "Aktif diyalog sahibi karakter" else "Sahnede adı geçen karakter"
        )
    }

    val AMBIGUOUS_PROPER_NAMES = setOf(
        "deniz", "yıldız", "ay", "güneş", "ateş", "ışık", "kaya", "su", "umut", "gül",
        "bulut", "duman", "çiçek", "rüya", "ece", "nur", "nar", "dağ"
    )

    enum class SignalStrength { STRONG, WEAK }

    data class CandidateHit(
        val rawName: String,
        val canonicalName: String,
        val role: String,
        val signalStrength: SignalStrength,
        val cueSnippet: String
    )

    private val NAME_PATTERN = """[\p{Lu}\p{Lt}][\p{L}\p{M}'‘’`-]{1,29}"""

    /**
     * Detects character name candidates from combined text with signal strength classification.
     * Supports Turkish suffixes, titles, kinship/role intros, two-word full names, speech & action cues.
     */
    fun detectCandidates(
        combinedText: String,
        blacklist: Set<String>,
        newCharsJson: String = ""
    ): List<CandidateHit> {
        val candidates = mutableMapOf<String, CandidateHit>()

        fun addCandidate(rawName: String, role: String, strength: SignalStrength, cue: String) {
            val canonical = normalizeTurkishEntityName(rawName)
            if (canonical.isBlank() || canonical.length < 2) return
            if (strength == SignalStrength.WEAK && canonical.length <= 2) return
            val lower = canonical.lowercase(TR_LOCALE)
            val normalizedRole = normalizeTurkishCastRole(role)
            if (isGenericBackgroundExtra(canonical, normalizedRole)) return
            if (isNonPersonPlaceOrOrganization(canonical)) return
            if (lower in AMBIGUOUS_PROPER_NAMES && strength == SignalStrength.WEAK) return
            if (lower in blacklist && lower !in AMBIGUOUS_PROPER_NAMES) return
            if (lower in blacklist && lower in AMBIGUOUS_PROPER_NAMES && strength == SignalStrength.WEAK) return
            val tokens = lower.split(Regex("""\s+""")).filter { it.isNotBlank() }
            if (tokens.isNotEmpty() && tokens.all { it in blacklist }) return

            val existing = candidates[lower]
            if (existing == null || (existing.signalStrength == SignalStrength.WEAK && strength == SignalStrength.STRONG)) {
                candidates[lower] = CandidateHit(rawName, canonical, normalizedRole, strength, cue)
            }
        }

        val stripped = combinedText
            .replace(Regex(""""[^"]{0,400}""""), " ")
            .replace(Regex("""«[^»]{0,400}»"""), " ")
            .replace(Regex("""\([^)]{0,200}\)"""), " ")
            .replace(Regex("""\[[^\]]{0,200}\]"""), " ")

        // 0. newChars JSON (STRONG)
        if (newCharsJson.isNotBlank()) {
            try {
                val arr = JSONArray(newCharsJson)
                val limit = minOf(arr.length(), 5)
                for (i in 0 until limit) {
                    val item = arr.opt(i)
                    val name: String?
                    val role: String
                    when (item) {
                        is JSONObject -> {
                            name = (item.optString("n").ifBlank { item.optString("name") }).ifBlank { null }
                            role = item.optString("r").ifBlank { item.optString("role").ifBlank { "Yan Karakter" } }
                        }
                        is String -> { name = item.ifBlank { null }; role = "Yan Karakter" }
                        else -> continue
                    }
                    if (!name.isNullOrBlank()) addCandidate(name, role, SignalStrength.STRONG, "newChars:$name")
                }
            } catch (_: Exception) {}
        }

        // 1. Açık etiketler [CHARACTER_EMOTION: Ad | Rol] (STRONG)
        val tagRegex = Regex("""(?is)\[\[?(?:CHARACTER_EMOTION|CAST_MEMBER|CHARACTER):\s*([^\]\n|,]+)(?:[|\s,]([^\]\n]*))?]]?""")
        tagRegex.findAll(combinedText).forEach { m ->
            val name = m.groupValues[1].trim()
            val role = m.groupValues[2].trim().ifBlank { "Yan Karakter" }
            if (name.isNotBlank()) addCandidate(name, role, SignalStrength.STRONG, m.value.take(120))
        }

        // 2. Unvan + İsim  "Doktor Elif", "Komiser Hakan", "Kaptan Voss" (STRONG)
        val titlePattern = Regex(
            """${WB_L}(Bay|Bayan|Doktor|Prof|Profesör|Dr\.|Komiser|Başkomiser|Yüzbaşı|Kaptan|Amiral|Komutan|Lord|Prens|Prenses|Kral|Kraliçe|Usta|Hoca|Savaşçı|Büyücü|Şövalye|Mimar|Aziz|Üstat|Gözcü|Dedektif|Ajan|Müfettiş|Savcı|Hâkim|Teğmen|Binbaşı|Albay|General|Sultan|Paşa|Dük|Kont|Baron|Barones|Rahip|Cadı|Gölge|Muhafız|Hizmetkar|Tüccar|Eczacı|Yargıç|Bakan|Başkan|Reis|Çavuş|Efe|Kethüda|Seyis|Avukat|Hemşire)\s+($NAME_PATTERN)$WB_R"""
        )
        titlePattern.findAll(stripped).forEach { m ->
            addCandidate(m.groupValues[2], m.groupValues[1], SignalStrength.STRONG, m.value.take(120))
        }

        // 2b. Akrabalık / Meslek / Rol + İsim: "kardeşi Mehmet", "eski sevgilisi Deniz", "saatçi Kadir", "kuzeni Mira" (STRONG)
        val kinshipPattern = Regex(
            """(?i)${WB_L}(eski\s+sevgilisi|eski\s+sevgilim|kardeşi|kardeşim|abisi|abim|ablası|ablam|kuzeni|kuzenim|amcası|dayısı|teyzesi|halası|annesi|babası|dedesi|arkadaşı|dostu|komşusu|patronu|ortağı|sevgilisi|nişanlısı|eşi|saatçi|terzi|fırıncı|yardımcısı|koruması)\s+($NAME_PATTERN)$WB_R"""
        )
        kinshipPattern.findAll(stripped).forEach { m ->
            val rawName = m.groupValues[2]
            if (rawName.firstOrNull()?.isUpperCase() == true) {
                addCandidate(rawName, m.groupValues[1].trim().replaceFirstChar { it.titlecase(TR_LOCALE) }, SignalStrength.STRONG, m.value.take(120))
            }
        }

        // 2c. İki kelimeli tam ad: "Aiden Blackwood" (STRONG if mid-sentence and both tokens proper-cased & non-blacklisted)
        val fullNamePattern = Regex("""(?<=[\s,;:–—-])($NAME_PATTERN)\s+($NAME_PATTERN)$WB_R""")
        fullNamePattern.findAll(stripped).forEach { m ->
            val first = stemTurkishProperName(m.groupValues[1])
            val second = stemTurkishProperName(m.groupValues[2])
            val fLower = first.lowercase(TR_LOCALE)
            val sLower = second.lowercase(TR_LOCALE)
            val isTitle = TITLES.any { it.equals(first, ignoreCase = true) }
            val isHon = HONORIFICS.any { it.equals(second, ignoreCase = true) }
            if (!isTitle && !isHon &&
                first.length >= 3 && second.length >= 3 &&
                fLower !in blacklist && sLower !in blacklist &&
                fLower !in AMBIGUOUS_PROPER_NAMES
            ) {
                addCandidate("$first $second", "Yan Karakter", SignalStrength.STRONG, m.value.take(120))
            }
        }

        // 3. İsim + Hitap  "Kerem Bey" (STRONG)
        val honorificPattern = Regex(
            """${WB_L}($NAME_PATTERN)\s+(Bey|Hanım|Efendi|Ağa|Usta|Abi|Abla|Komutan|Kaptan|Doktor|Hazretleri|Paşa|Hoca|Teyze|Amca|Dayı|Yenge|Dede|Peder)$WB_R"""
        )
        honorificPattern.findAll(stripped).forEach { m ->
            val name = m.groupValues[1].trim()
            addCandidate(name, m.groupValues[2], SignalStrength.STRONG, m.value.take(120))
        }

        // 4. Açık tanıtma  "X adında / X adlı" (STRONG)
        val introPattern = Regex(
            """${WB_L}($NAME_PATTERN)\s+(adında|isminde|adlı|denen|diye\s+çağrılan|isimli|adındaki|ismindeki|arkadaşım|doktorum|yardımcım|sekreterim|muhafızım)$WB_R"""
        )
        introPattern.findAll(stripped).forEach { m ->
            addCandidate(m.groupValues[1], "Yan Karakter", SignalStrength.STRONG, m.value.take(120))
        }

        // 5. Konuşma atfı (STRONG)
        val speechVerbs = """(?:dedi|sordu|fısıldadı|bağırdı|güldü|mırıldandı|haykırdı|söyledi|yanıtladı|ekledi|çıkıştı|karşılık\s+verdi|seslendi|atıldı|cevap\s+verdi|cevapladı)"""
        val speechPost = Regex("""(?:"[^"]{3,300}"|«[^»]{3,300}»)\s*,?\s*$speechVerbs\s+($NAME_PATTERN)$WB_R""")
        val speechPre = Regex("""${WB_L}($NAME_PATTERN)\s*,\s*(?:"[^"]{3,300}"|«[^»]{3,300}»)""")
        val speechDirect = Regex("""${WB_L}($NAME_PATTERN)\s+$speechVerbs$WB_R""")
        speechPost.findAll(combinedText).forEach { m -> addCandidate(m.groupValues[1], "Sahnede Konuşan Karakter", SignalStrength.STRONG, m.value.take(120)) }
        speechPre.findAll(combinedText).forEach { m -> addCandidate(m.groupValues[1], "Sahnede Konuşan Karakter", SignalStrength.STRONG, m.value.take(120)) }
        speechDirect.findAll(stripped).forEach { m -> addCandidate(m.groupValues[1], "Sahnede Konuşan Karakter", SignalStrength.STRONG, m.value.take(120)) }

        // 6. Eylem kalıpları (STRONG)
        val actionVerbs = """(?:içeri\s+girdi|kapıyı\s+çaldı|masaya\s+oturdu|elini\s+uzattı|gülümsedi|arkasını\s+döndü|ayağa\s+kalktı|bize\s+katıldı|yaklaştı|konuşmaya\s+başladı|yanımıza\s+geldi|bize\s+baktı|odaya\s+girdi|kapıdan\s+çıktı)"""
        val actionPattern = Regex("""${WB_L}($NAME_PATTERN)\s+$actionVerbs$WB_R""")
        actionPattern.findAll(stripped).forEach { m -> addCandidate(m.groupValues[1], "Sahnede Eyleme Geçen Karakter", SignalStrength.STRONG, m.value.take(120)) }

        // 7. Apostroflu özel isim (WEAK)
        val apostrophePattern = Regex("""${WB_L}([\p{L}]{2,25})['‘’`](${TURKISH_INFLECTION_SUFFIXES.joinToString("|") { Regex.escape(it) }})$WB_R""")
        apostrophePattern.findAll(combinedText).forEach { m ->
            val base = m.groupValues[1]
            val capitalized = base.replaceFirstChar { if (it.isLowerCase()) it.titlecase(TR_LOCALE) else it.toString() }
            addCandidate(capitalized, "Yan Karakter", SignalStrength.WEAK, m.value.take(80))
        }

        return candidates.values.toList()
    }

    /**
     * Fast (<1 ms) gate deciding whether Micro-AI (ExecutionMode.UTILITY) cast extraction
     * should be invoked for this turn.
     * Skips trivial short turns ("Tamam geliyorum") to save latency and tokens.
     */
    fun shouldInvokeMicroAiCastExtraction(
        text: String,
        botName: String = "",
        userCharName: String = "",
        knownNames: Set<String> = emptySet(),
        blacklist: Set<String> = getBlacklist()
    ): Boolean {
        val clean = text.trim()
        if (clean.length < 24) return false
        val words = clean.split(Regex("""\s+""")).filter { it.isNotBlank() }
        if (words.size < 4) return false

        val excludedLower = buildSet {
            addAll(blacklist)
            if (botName.isNotBlank()) add(normalizeTurkishEntityName(botName).lowercase(TR_LOCALE))
            if (userCharName.isNotBlank()) add(normalizeTurkishEntityName(userCharName).lowercase(TR_LOCALE))
            knownNames.forEach { n ->
                val norm = normalizeTurkishEntityName(n).lowercase(TR_LOCALE)
                if (norm.isNotBlank()) add(norm)
            }
        }

        // 1. Check for role/kinship/literary intro cues
        val lowerText = clean.lowercase(TR_LOCALE)
        val hasRoleCue = KINSHIP_OR_ROLE_CUES.any { cue ->
            Regex("""${WB_L}${Regex.escape(cue)}$WB_R""").containsMatchIn(lowerText)
        }
        if (hasRoleCue) return true

        // 2. Check for any mid-sentence capitalized proper noun not already known/blacklisted
        val midSentenceCapRegex = Regex("""(?<=[\s,;:–—"'«“*])($NAME_PATTERN)$WB_R""")
        val hasNewProperNoun = midSentenceCapRegex.findAll(clean).any { m ->
            val norm = normalizeTurkishEntityName(m.groupValues[1])
            val low = norm.lowercase(TR_LOCALE)
            norm.length >= 3 && low !in excludedLower
        }
        if (hasNewProperNoun) return true

        // 3. Check if local detector found any candidate needing validation/disambiguation
        val localHits = detectCandidates(clean, blacklist)
        return localHits.any { hit ->
            val low = hit.canonicalName.lowercase(TR_LOCALE)
            low !in excludedLower
        }
    }

    /**
     * Layer 2 — Micro-AI Cast Extractor & Validator (ExecutionMode.UTILITY, temp=0.15, max_tokens=240).
     * Extracts indirect literary characters ("yaşlı saatçi Kadir") and filters false positives
     * ("Galata", "Karanlık"). Silently falls back to Layer 1 local detection if disabled, offline,
     * or if the utility caller fails/returns invalid JSON.
     */
    suspend fun extractAndValidateCastWithMicroAi(
        recentTwoMessagesText: String,
        botName: String,
        userCharName: String,
        knownNames: Set<String> = emptySet(),
        blacklistedDbNames: Set<String> = emptySet(),
        context: Context? = null,
        forceMicroAi: Boolean = false,
        utilityCaller: (suspend (systemPrompt: String, userPrompt: String, maxTokens: Int) -> String)? = null
    ): CastExtractionResult {
        val baseBlacklist = getBlacklist(context) + blacklistedDbNames.map { it.lowercase(TR_LOCALE) }
        val excludedMain = buildSet {
            if (botName.isNotBlank()) add(normalizeTurkishEntityName(botName).lowercase(TR_LOCALE))
            if (userCharName.isNotBlank()) add(normalizeTurkishEntityName(userCharName).lowercase(TR_LOCALE))
        }

        val localHits = detectCandidates(recentTwoMessagesText, baseBlacklist)
            .filter { hit ->
                val low = hit.canonicalName.lowercase(TR_LOCALE)
                low !in excludedMain && blacklistedDbNames.none { isLikelyAlias(hit.canonicalName, it) }
            }

        fun buildLocalFallback(): CastExtractionResult {
            val strongLocal = localHits
                .filter { it.signalStrength == SignalStrength.STRONG }
                .map { hit ->
                    MicroAiCastMemberDto(
                        name = hit.canonicalName,
                        isPerson = true,
                        aliases = if (!hit.rawName.equals(hit.canonicalName, ignoreCase = true)) listOf(hit.rawName) else emptyList(),
                        role = hit.role,
                        relation = "",
                        age = null,
                        inScene = true
                    )
                }
            return CastExtractionResult(members = strongLocal, rejected = emptySet(), usedMicroAi = false)
        }

        if (!AppFeatureFlags.isMicroAiCastSentimentEnabled || utilityCaller == null) {
            return buildLocalFallback()
        }

        if (!forceMicroAi && !shouldInvokeMicroAiCastExtraction(
                text = recentTwoMessagesText,
                botName = botName,
                userCharName = userCharName,
                knownNames = knownNames,
                blacklist = baseBlacklist
            )
        ) {
            return buildLocalFallback()
        }

        val systemPrompt = """
Sen Asroda'nın hikâye karakteri çıkarıcı ve figüran/yanlış pozitif eleyici yardımcı motorusun.
Yalnızca GEÇERLİ tek JSON nesnesi üret; markdown veya açıklama YAZMA.
Kurallar:
1. Yalnızca GERÇEK ÖZEL İSMİ olan ve hikâyede/olay örgüsünde anlamlı veya tekrar eden rolü bulunan gerçek karakterleri "cast" dizisine al ("is_person": true).
2. Ana bot ($botName) ve kullanıcı karakterini ($userCharName) ASLA ekleme.
3. İsimsiz, tek sahnelik veya sıradan figüranları ("zorba çocuk", "okul zorbası", "garson", "öğrenci", "sınıf arkadaşı", "güvenlik görevlisi", "polis", "şoför", "yabancı" vb.) ve şehir/mekan/kurum/bina/soyut kavramları ("Galata", "Galata Kulesi", "Nevermore Akademisi", "Stark Tower", "Karanlık" vb.) ASLA cast'e alma, doğrudan "rejected" dizisine koy.
4. Türkçe çekim eklerini soyup kanonik ad yaz ("Leyla'nın" -> "Leyla").
5. "role" ve "relation" alanları KESİNLİKLE %100 DOĞAL TÜRKÇE olmalıdır ("Friend", "Enemy", "Doctor" gibi İngilizce etiketler YASAKTIR!).
6. Şu anda fiziksel olarak sahnede aktif olanlar için "in_scene": true, sadece geçmişten anılanlar için false yaz.
JSON ŞEMASI:
{"cast":[{"name":"Kadir","is_person":true,"aliases":["yaşlı saatçi"],"role":"saat ustası","relation":"mahalle tanıdığı","age":62,"in_scene":true}],"rejected":["Galata","zorba çocuk"]}
        """.trimIndent()

        val suspiciousCandidates = localHits.map { it.canonicalName }.distinct().take(8)
        val userPrompt = buildString {
            appendLine("ANA BOT: $botName | KULLANICI: $userCharName")
            if (knownNames.isNotEmpty()) appendLine("MEVCUT KAYITLI KADRO: ${knownNames.take(20).joinToString(", ")}")
            if (suspiciousCandidates.isNotEmpty()) appendLine("YEREL ŞÜPHELİ ADAYLAR: ${suspiciousCandidates.joinToString(", ")}")
            appendLine("SON SAHNE METNİ:")
            append(recentTwoMessagesText.take(1200))
        }

        val raw = try {
            utilityCaller(systemPrompt, userPrompt, 240)
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            return buildLocalFallback()
        }

        val parsed = parseMicroAiCastResponse(raw) ?: return buildLocalFallback()
        return sanitizeMicroAiCastResponse(
            response = parsed,
            botName = botName,
            userCharName = userCharName,
            blacklist = baseBlacklist,
            blacklistedDbNames = blacklistedDbNames
        )
    }

    /**
     * Parses a Micro-AI JSON string (either standalone or embedded in unified curator JSON)
     * into [MicroAiCastResponse]. Returns null if JSON is invalid.
     */
    fun parseMicroAiCastResponse(rawJson: String): MicroAiCastResponse? {
        if (rawJson.isBlank()) return null
        val cleaned = rawJson.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return try {
            val obj = JSONObject(cleaned.substring(start, end + 1))
            parseMicroAiCastFromJsonObject(obj)
        } catch (_: Exception) {
            null
        }
    }

    fun parseMicroAiCastFromJsonObject(obj: JSONObject): MicroAiCastResponse? {
        val castArr = obj.optJSONArray("cast")
        val rejArr = obj.optJSONArray("rejected")
        if (castArr == null && rejArr == null) return null

        val members = mutableListOf<MicroAiCastMemberDto>()
        if (castArr != null) {
            for (i in 0 until minOf(castArr.length(), 8)) {
                val item = castArr.optJSONObject(i) ?: continue
                val name = item.optString("name", "").trim()
                if (name.isBlank()) continue
                val isPerson = item.optBoolean("is_person", true)
                val aliases = mutableListOf<String>()
                val aliasArr = item.optJSONArray("aliases")
                if (aliasArr != null) {
                    for (a in 0 until minOf(aliasArr.length(), 6)) {
                        val al = aliasArr.optString(a, "").trim()
                        if (al.length >= 2) aliases.add(al)
                    }
                }
                val role = item.optString("role", "Yan Karakter").trim().ifBlank { "Yan Karakter" }
                val relation = item.optString("relation", "").trim()
                val age = if (item.has("age") && !item.isNull("age")) {
                    item.optInt("age", -1).takeIf { it in 1..150 }
                } else null
                val inScene = item.optBoolean("in_scene", true)
                members.add(
                    MicroAiCastMemberDto(
                        name = name,
                        isPerson = isPerson,
                        aliases = aliases,
                        role = role,
                        relation = relation,
                        age = age,
                        inScene = inScene
                    )
                )
            }
        }

        val rejected = mutableListOf<String>()
        if (rejArr != null) {
            for (i in 0 until minOf(rejArr.length(), 12)) {
                val r = rejArr.optString(i, "").trim()
                if (r.isNotBlank()) rejected.add(r)
            }
        }
        return MicroAiCastResponse(cast = members, rejected = rejected)
    }

    fun sanitizeMicroAiCastResponse(
        response: MicroAiCastResponse,
        botName: String,
        userCharName: String,
        blacklist: Set<String> = getBlacklist(),
        blacklistedDbNames: Set<String> = emptySet()
    ): CastExtractionResult {
        val rejectedNormalized = response.rejected
            .map { normalizeTurkishEntityName(it).lowercase(TR_LOCALE) }
            .filter { it.isNotBlank() }
            .toMutableSet()

        val excludedMain = buildSet {
            if (botName.isNotBlank()) add(normalizeTurkishEntityName(botName).lowercase(TR_LOCALE))
            if (userCharName.isNotBlank()) add(normalizeTurkishEntityName(userCharName).lowercase(TR_LOCALE))
        }

        val deduped = linkedMapOf<String, MicroAiCastMemberDto>()
        for (rawMember in response.cast) {
            val normalizedName = normalizeTurkishEntityName(rawMember.name)
            val lower = normalizedName.lowercase(TR_LOCALE)
            if (!rawMember.isPerson) {
                if (lower.isNotBlank()) rejectedNormalized.add(lower)
                continue
            }
            if (normalizedName.length < 2) continue
            if (lower in excludedMain || lower in rejectedNormalized) continue
            val normalizedRole = normalizeTurkishCastRole(rawMember.role)
            val normalizedRelation = normalizeTurkishCastRelation(rawMember.relation)
            if (isGenericBackgroundExtra(normalizedName, normalizedRole) ||
                isNonPersonPlaceOrOrganization(normalizedName)
            ) {
                rejectedNormalized.add(lower)
                continue
            }
            if (lower in blacklist && lower !in AMBIGUOUS_PROPER_NAMES) {
                rejectedNormalized.add(lower)
                continue
            }
            val cleanedAliases = rawMember.aliases
                .map { it.trim() }
                .filter { it.length >= 2 && !it.equals(normalizedName, ignoreCase = true) }
                .distinct()

            val hitsBlacklistedDb = blacklistedDbNames.any { bl ->
                isLikelyAlias(normalizedName, bl) ||
                    cleanedAliases.any { al -> isLikelyAlias(al, bl) }
            }
            if (hitsBlacklistedDb) {
                rejectedNormalized.add(lower)
                continue
            }

            // Merge with an existing entry if exact match OR alias match (e.g., "Natasha" & "Natasha Romanoff")
            val aliasKey = deduped.entries.firstOrNull { (k, dto) ->
                k == lower ||
                    isLikelyAlias(normalizedName, dto.name) ||
                    cleanedAliases.any { isLikelyAlias(it, dto.name) } ||
                    dto.aliases.any { isLikelyAlias(normalizedName, it) }
            }?.key
            val existing = aliasKey?.let { deduped[it] }
            if (existing == null) {
                deduped[lower] = rawMember.copy(
                    name = normalizedName,
                    role = normalizedRole,
                    relation = normalizedRelation,
                    aliases = cleanedAliases
                )
            } else {
                // Prefer the fuller canonical name (e.g., "Natasha Romanoff" over "Natasha") and retain the shorter as alias
                val preferNewName = normalizedName.split(' ').size > existing.name.split(' ').size &&
                    isLikelyAlias(existing.name, normalizedName)
                val finalName = if (preferNewName) normalizedName else existing.name
                val extraAlias = if (preferNewName) existing.name else normalizedName
                val mergedAliases = (existing.aliases + cleanedAliases + extraAlias)
                    .filter { it.length >= 2 && !it.equals(finalName, ignoreCase = true) }
                    .distinct()
                val mergedDto = existing.copy(
                    name = finalName,
                    aliases = mergedAliases,
                    role = if (existing.role == "Yan Karakter" && normalizedRole.isNotBlank()) normalizedRole else existing.role,
                    relation = existing.relation.ifBlank { normalizedRelation },
                    age = existing.age ?: rawMember.age,
                    inScene = existing.inScene || rawMember.inScene
                )
                if (preferNewName) {
                    deduped.remove(aliasKey!!)
                    deduped[finalName.lowercase(TR_LOCALE)] = mergedDto
                } else {
                    deduped[aliasKey!!] = mergedDto
                }
            }
        }

        return CastExtractionResult(
            members = deduped.values.toList(),
            rejected = rejectedNormalized,
            usedMicroAi = true
        )
    }

    /**
     * Extracts persisted aliases stored in CastMemberEntity.multiRoleJson with "alias:" prefix.
     * Preserves Room v48 schema with zero column changes.
     */
    fun extractStoredAliases(multiRoleJson: String): Set<String> {
        if (multiRoleJson.isBlank() || multiRoleJson == "[]") return emptySet()
        return try {
            val arr = JSONArray(multiRoleJson)
            buildSet {
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i, "").trim()
                    if (item.startsWith(ALIAS_PREFIX, ignoreCase = true)) {
                        val alias = item.substring(ALIAS_PREFIX.length).trim()
                        if (alias.length >= 2) add(alias)
                    }
                }
            }
        } catch (_: Exception) {
            emptySet()
        }
    }

    /**
     * Merges new aliases into CastMemberEntity.multiRoleJson without disturbing existing roles.
     */
    fun mergeStoredAliases(existingMultiRoleJson: String, newAliases: Collection<String>, canonicalName: String = ""): String {
        val entries = linkedSetOf<String>()
        if (existingMultiRoleJson.isNotBlank() && existingMultiRoleJson != "[]") {
            try {
                val arr = JSONArray(existingMultiRoleJson)
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i, "").trim()
                    if (item.isNotBlank()) entries.add(item)
                }
            } catch (_: Exception) {}
        }
        val canonicalLower = canonicalName.trim().lowercase(TR_LOCALE)
        for (raw in newAliases) {
            val clean = raw.trim()
            if (clean.length >= 2 && clean.lowercase(TR_LOCALE) != canonicalLower) {
                val alreadyPresent = entries.any {
                    it.equals("$ALIAS_PREFIX$clean", ignoreCase = true)
                }
                if (!alreadyPresent) entries.add("$ALIAS_PREFIX$clean")
            }
        }
        val out = JSONArray()
        entries.forEach { out.put(it) }
        return out.toString()
    }

    private val UNAPOSTROPHIZED_NAME_SUFFIXES = setOf(
        "a", "e", "ı", "i", "u", "ü", "ya", "ye", "yı", "yi", "yu", "yü",
        "da", "de", "ta", "te", "dan", "den", "tan", "ten",
        "la", "le", "yla", "yle", "ın", "in", "un", "ün", "nın", "nin", "nun", "nün"
    )

    /**
     * Unicode word-boundary aware name mention check, with both apostrophe (`Natasha'nın`)
     * and casual unapostrophized (`Natashayla`, `Kadire`) Turkish case-suffix tolerance.
     */
    fun isNameMentionedInText(name: String, text: String): Boolean {
        if (name.isBlank() || text.isBlank()) return false
        val lowerText = text.lowercase(TR_LOCALE)
        val nameLower = name.lowercase(TR_LOCALE).trim()
        val stem = stemTurkishProperName(name).lowercase(TR_LOCALE).trim()

        val candidates = setOfNotNull(
            nameLower.takeIf { it.length >= 2 },
            stem.takeIf { it.length >= 2 && it != nameLower }
        )
        for (c in candidates) {
            val esc = Regex.escape(c)
            if (Regex("""${WB_L}$esc$WB_R""").containsMatchIn(lowerText)) return true
            if (Regex("""${WB_L}$esc['‘’`][\p{L}]+$WB_R""").containsMatchIn(lowerText)) return true
            // Casual Turkish typing without apostrophe for names >= 4 chars (e.g., "Natashayla", "Kadirle", "Elifi")
            if (c.length >= 4 && !c.contains(' ')) {
                val m = Regex("""${WB_L}$esc([\p{L}]{1,3})$WB_R""").find(lowerText)
                if (m != null && m.groupValues[1] in UNAPOSTROPHIZED_NAME_SUFFIXES) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Token-subset & Turkish-normalized alias check:
     * "Doktor Elif" <-> "Elif", "Leyla'nın" <-> "Leyla", "Kerem" <-> "Kerem Aydın".
     */
    fun isLikelyAlias(candidate: String, existingName: String): Boolean {
        val c = normalizeTurkishEntityName(candidate).lowercase(TR_LOCALE)
        val e = normalizeTurkishEntityName(existingName).lowercase(TR_LOCALE)
        if (c.isBlank() || e.isBlank()) return false
        if (c == e) return true
        val cTokens = c.split(Regex("""\s+""")).filter { it.length >= 2 }.toSet()
        val eTokens = e.split(Regex("""\s+""")).filter { it.length >= 2 }.toSet()
        return (cTokens.isNotEmpty() && eTokens.containsAll(cTokens)) ||
            (eTokens.isNotEmpty() && cTokens.containsAll(eTokens))
    }

    fun matchesMemberOrAlias(
        candidateName: String,
        candidateAliases: Collection<String>,
        member: com.example.data.local.CastMemberEntity
    ): Boolean {
        val storedAliases = extractStoredAliases(member.multiRoleJson)
        val allMemberTokens = storedAliases + member.name
        val allCandidateTokens = candidateAliases + candidateName
        return allCandidateTokens.any { cand ->
            allMemberTokens.any { existing -> isLikelyAlias(cand, existing) }
        }
    }

    /**
     * Active in-scene cast filter (Sistem #16):
     * Returns at most [maxActive] (default & capped at 4) non-blacklisted characters that are
     * explicitly in-scene, mentioned by name/alias in the last 6 messages, or high-importance (>=90) core cast.
     */
    fun filterActiveInSceneCastMembers(
        allCastMembers: List<com.example.data.local.CastMemberEntity>,
        recentMessagesText: String,
        maxActive: Int = 4,
        explicitInSceneNames: Set<String> = emptySet()
    ): List<com.example.data.local.CastMemberEntity> {
        if (allCastMembers.isEmpty()) return emptyList()
        val limit = maxActive.coerceIn(1, 4)
        val normalizedExplicit = explicitInSceneNames
            .map { normalizeTurkishEntityName(it).lowercase(TR_LOCALE) }
            .filter { it.isNotBlank() }
            .toSet()

        val activeScored = allCastMembers
            .filter { !it.isBlacklisted }
            .mapNotNull { member ->
                val storedAliases = extractStoredAliases(member.multiRoleJson)
                val memberLower = normalizeTurkishEntityName(member.name).lowercase(TR_LOCALE)
                val isExplicitInScene = memberLower in normalizedExplicit ||
                    storedAliases.any { normalizeTurkishEntityName(it).lowercase(TR_LOCALE) in normalizedExplicit }
                val isMentioned = isNameMentionedInText(member.name, recentMessagesText) ||
                    storedAliases.any { isNameMentionedInText(it, recentMessagesText) }
                val isCore = member.importanceScore >= 90

                if (!isExplicitInScene && !isMentioned && !isCore) {
                    null
                } else {
                    val priority = (if (isExplicitInScene) 1000 else 0) +
                        (if (isMentioned) 500 else 0) +
                        member.importanceScore +
                        member.appearanceCount.coerceAtMost(50)
                    member to priority
                }
            }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }

        return activeScored
    }
}
