package com.example.data.managers

import com.example.data.local.BotLorebookLinkEntity
import com.example.data.local.LorebookDao
import com.example.data.local.LorebookEntity
import com.example.data.local.LorebookEntryEntity
import com.example.data.repository.SystemPromptBuilder
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

/**
 * Dinamik Lorebook & Evren Ansiklopedisi Motoru (100% cihaz-içi, sıfır ağ bağımlılığı).
 *
 * - Yalnızca RP Sohbet sistem promptuna enjekte edilir (Kitap Modu'na dokunmaz).
 * - Türkçe büyük/küçük harf (İ/i, I/ı, Ş/ş, Ğ/ğ, Ü/ü, Ö/ö, Ç/ç) ve ek uyumlu kök eşleştirme.
 * - Her zaman aktif (Constant) + anahtar kelime tetiklemeli + 1 adımlı zincirleme tetikleme (loop-safe).
 * - Akıllı token bütçesi (öncelik 10 -> 1 sıralı).
 * - Standart JSON İçe/Dışa Aktarım (Velora ve SillyTavern/CharacterAI Lorebook uyumlu).
 */
class LorebookManager(
    private val dao: LorebookDao? = null
) {

    data class TriggeredEntry(
        val entry: LorebookEntryEntity,
        val reason: String // "sabit", "anahtar:<kw>", "zincir:<kw>"
    )

    data class SelectionResult(
        val selected: List<TriggeredEntry>,
        val estimatedTokens: Int,
        val promptBlock: String
    )

    data class ImportedLorebookBundle(
        val lorebook: LorebookEntity,
        val entries: List<LorebookEntryEntity>
    )

    companion object {
        private val TR_LOCALE = Locale.forLanguageTag("tr-TR")
        private val PUNCT_REGEX = Regex("""[^\p{L}\p{N}\s'-]""")
        private val MULTI_SPACE_REGEX = Regex("""\s+""")
        internal const val MAX_IMPORT_JSON_CHARS = 2 * 1024 * 1024
        internal const val MAX_IMPORT_ENTRIES = 500

        val CATEGORIES = listOf(
            "genel" to "Genel",
            "karakter" to "Karakter",
            "mekan" to "Mekan",
            "guc" to "Güç / Sistem",
            "kurum" to "Kurum / Klan",
            "esya" to "Eşya / Yadigar",
            "olay" to "Olay / Tarih",
            "kural" to "Evren Kuralı"
        )

        fun normalizeTr(text: String): String {
            return text
                .lowercase(TR_LOCALE)
                .replace('\u0307', ' ') // stray combining dot after İ lowercasing
                .replace(PUNCT_REGEX, " ")
                .replace(MULTI_SPACE_REGEX, " ")
                .trim()
        }

        /**
         * Parses positive and optional negative (`-kelime` / `!kelime`) keywords.
         */
        fun parsePositiveAndNegativeKeywords(raw: String): Pair<List<String>, List<String>> {
            val pos = mutableListOf<String>()
            val neg = mutableListOf<String>()
            raw.split(",", ";", "\n", "|").forEach { rawTok ->
                val trimmed = rawTok.trim()
                if (trimmed.startsWith("-") || trimmed.startsWith("!")) {
                    val norm = normalizeTr(trimmed.drop(1))
                    if (norm.length >= 2) neg.add(norm)
                } else {
                    val norm = normalizeTr(trimmed)
                    if (norm.length >= 2) pos.add(norm)
                }
            }
            return Pair(pos.distinct(), neg.distinct())
        }

        fun parseKeywords(raw: String): List<String> = parsePositiveAndNegativeKeywords(raw).first

        /**
         * Türkçe ek + ünsüz yumuşaması (p->b, ç->c, t->d, k->ğ) + kök uyumlu eşleştirme:
         * - Çok kelimeli ifadelerde tam alt-dize eşleşmesi.
         * - Tek kelimelerde kelime başı, yumuşamış kök ("kitap" -> "kitabı", "kılıç" -> "kılıcını",
         *   "yüzük" -> "yüzüğünü", "kalp" -> "kalbini") ve Türkçe gövde eşleşmesi;
         *   kısa köklerde (<=3 harf) tam kelime / apostrof / ünsüz yumuşaması ile yanlış pozitif önlenir.
         */
        fun matchesKeyword(normalizedText: String, normalizedKeyword: String): Boolean {
            if (normalizedText.isBlank() || normalizedKeyword.isBlank()) return false
            if (normalizedKeyword.contains(' ')) {
                return normalizedText.contains(normalizedKeyword)
            }
            val softenedRoot = when {
                normalizedKeyword.length >= 3 && normalizedKeyword.endsWith('p') -> normalizedKeyword.dropLast(1) + "b"
                normalizedKeyword.length >= 3 && normalizedKeyword.endsWith('ç') -> normalizedKeyword.dropLast(1) + "c"
                normalizedKeyword.length >= 3 && normalizedKeyword.endsWith('t') -> normalizedKeyword.dropLast(1) + "d"
                normalizedKeyword.length >= 4 && normalizedKeyword.endsWith('k') -> normalizedKeyword.dropLast(1) + "ğ"
                else -> null
            }
            val words = normalizedText.split(' ')
            return if (normalizedKeyword.length <= 3) {
                words.any { w ->
                    w == normalizedKeyword ||
                        w.startsWith("$normalizedKeyword'") ||
                        (softenedRoot != null && w.startsWith(softenedRoot) && w.length <= softenedRoot.length + 5)
                }
            } else {
                val kwStem = com.example.data.repository.TurkishTextAnalyzer.stemToken(normalizedKeyword)
                words.any { w ->
                    w == normalizedKeyword ||
                        w.startsWith(normalizedKeyword) ||
                        (softenedRoot != null && w.startsWith(softenedRoot)) ||
                        (normalizedKeyword.length >= 6 && w.startsWith(normalizedKeyword.dropLast(1))) ||
                        (kwStem.length >= 4 && com.example.data.repository.TurkishTextAnalyzer.stemToken(w) == kwStem)
                }
            }
        }

        /**
         * Pure selection engine — easily unit-testable without Room.
         * Supports negative exclusion keywords (`-kelime`), Turkish softening/stem matching,
         * 1-step chained triggering, and recency + match-density tie-breaking within priority tiers.
         */
        fun selectEntriesForContext(
            entries: List<LorebookEntryEntity>,
            recentMessageTexts: List<String>,
            tokenBudget: Int = 750,
            allowChainedTriggering: Boolean = true
        ): SelectionResult {
            val activeEntries = entries.filter { it.isEnabled && it.content.isNotBlank() }
            if (activeEntries.isEmpty()) {
                return SelectionResult(emptyList(), 0, "")
            }

            val safeBudget = tokenBudget.coerceIn(120, 3_000)
            val triggeredById = LinkedHashMap<String, TriggeredEntry>()
            val matchRelevanceScore = HashMap<String, Int>()

            // Step 1: Constant (always-active) entries
            for (entry in activeEntries) {
                if (entry.isConstant) {
                    triggeredById[entry.id] = TriggeredEntry(entry, "sabit")
                    matchRelevanceScore[entry.id] = 100
                }
            }

            // Pre-normalize recent messages (newest last)
            val normalizedMessages = recentMessageTexts.map { normalizeTr(it) }
            val latestNormalizedMsg = normalizedMessages.lastOrNull().orEmpty()

            // Step 2: Direct keyword scan against recent messages (respecting scanDepth & negative exclusions)
            val directMatches = mutableListOf<LorebookEntryEntity>()
            for (entry in activeEntries) {
                if (triggeredById.containsKey(entry.id)) continue
                val (keywords, exclusions) = parsePositiveAndNegativeKeywords(entry.keywords)
                if (keywords.isEmpty()) continue

                val depth = entry.scanDepth.coerceIn(1, 12)
                val windowText = normalizedMessages.takeLast(depth).joinToString(" ")
                val matchedKws = keywords.filter { kw -> matchesKeyword(windowText, kw) }
                if (matchedKws.isNotEmpty()) {
                    // Block entry whenever any negative exclusion keyword (`-kelime` / `!kelime`) matches the scan window
                    val excluded = exclusions.isNotEmpty() && exclusions.any { ex -> matchesKeyword(windowText, ex) }
                    if (excluded) continue

                    val inLatestBonus = if (matchedKws.any { kw -> matchesKeyword(latestNormalizedMsg, kw) }) 20 else 0
                    val score = (matchedKws.size * 10) + inLatestBonus
                    triggeredById[entry.id] = TriggeredEntry(entry, "anahtar:${matchedKws.first()}")
                    matchRelevanceScore[entry.id] = score
                    directMatches.add(entry)
                }
            }

            // Step 3: 1-step chained triggering (strictly 1 hop — no infinite loops, respecting negative exclusions)
            if (allowChainedTriggering && directMatches.isNotEmpty()) {
                val chainCorpus = normalizeTr(
                    directMatches.joinToString(" ") { "${it.title} ${it.content}" }
                )
                if (chainCorpus.isNotBlank()) {
                    val allRecentNormalized = normalizedMessages.joinToString(" ")
                    for (entry in activeEntries) {
                        if (triggeredById.containsKey(entry.id)) continue
                        val (keywords, exclusions) = parsePositiveAndNegativeKeywords(entry.keywords)
                        if (keywords.isEmpty()) continue
                        val hit = keywords.firstOrNull { kw -> matchesKeyword(chainCorpus, kw) }
                        if (hit != null) {
                            val excluded = exclusions.isNotEmpty() && exclusions.any { ex ->
                                matchesKeyword(chainCorpus, ex) || matchesKeyword(allRecentNormalized, ex)
                            }
                            if (excluded) continue
                            triggeredById[entry.id] = TriggeredEntry(entry, "zincir:$hit")
                            matchRelevanceScore[entry.id] = 5
                        }
                    }
                }
            }

            if (triggeredById.isEmpty()) {
                return SelectionResult(emptyList(), 0, "")
            }

            // Step 4: Deterministic priority sort (Constant first, Priority DESC, Recency/Match Score DESC, Title ASC)
            val ordered = triggeredById.values.sortedWith(
                compareByDescending<TriggeredEntry> { it.entry.isConstant }
                    .thenByDescending { it.entry.priority }
                    .thenByDescending { matchRelevanceScore[it.entry.id] ?: 0 }
                    .thenBy { it.entry.title.lowercase(TR_LOCALE) }
            )

            // Step 5: Smart token budget enforcement (never exceeds safeBudget; trims constant/first entry instead of silently dropping)
            val chosen = mutableListOf<TriggeredEntry>()
            val sb = StringBuilder()
            val header = "\n[EVREN ANSİKLOPEDİSİ — AKTİF LOREBOOK BİLGİLERİ]\n" +
                "(Aşağıdaki kanonik bilgiler sahnedeki mevcut bağlam için otomatik açılmıştır; çelişmeden doğal biçimde kullan.)\n"
            var usedTokens = SystemPromptBuilder.estimateTokens(header).coerceAtMost(safeBudget)
            sb.append(header)

            for (item in ordered) {
                val catLabel = item.entry.category.uppercase(TR_LOCALE)
                val formattedLine = "• [${item.entry.title} | $catLabel]: ${item.entry.content.trim()}\n"
                val lineTokens = SystemPromptBuilder.estimateTokens(formattedLine)
                if (usedTokens + lineTokens > safeBudget) {
                    val remainingTokens = safeBudget - usedTokens
                    if (chosen.isEmpty() || (item.entry.isConstant && remainingTokens >= 25)) {
                        val remainChars = (remainingTokens.coerceAtLeast(16) * 3).coerceAtMost(formattedLine.length)
                        val trimmedLine = formattedLine.take(remainChars).trimEnd() + "…\n"
                        val trimmedTokens = SystemPromptBuilder.estimateTokens(trimmedLine)
                        sb.append(trimmedLine)
                        usedTokens = (usedTokens + trimmedTokens).coerceAtMost(safeBudget)
                        chosen.add(item)
                        if (safeBudget - usedTokens < 25) break
                    }
                    continue
                }
                sb.append(formattedLine)
                usedTokens += lineTokens
                chosen.add(item)
            }

            return SelectionResult(
                selected = chosen,
                estimatedTokens = usedTokens,
                promptBlock = if (chosen.isEmpty()) "" else sb.toString().trimEnd()
            )
        }

        /**
         * Exports a Lorebook and its entries to clean, portable JSON.
         */
        fun exportLorebookJson(lorebook: LorebookEntity, entries: List<LorebookEntryEntity>): String {
            val root = JSONObject()
            root.put("format", "velora_lorebook_v1")
            root.put("name", lorebook.name)
            root.put("description", lorebook.description)
            root.put("tokenBudget", lorebook.tokenBudget)
            val arr = JSONArray()
            entries.forEach { e ->
                val obj = JSONObject()
                obj.put("title", e.title)
                obj.put("keywords", e.keywords)
                obj.put("keys", JSONArray(parseKeywords(e.keywords)))
                obj.put("content", e.content)
                obj.put("category", e.category)
                obj.put("priority", e.priority)
                obj.put("constant", e.isConstant)
                obj.put("enabled", e.isEnabled)
                obj.put("scanDepth", e.scanDepth)
                arr.put(obj)
            }
            root.put("entries", arr)
            return root.toString(2)
        }

        /**
         * Imports Velora JSON or SillyTavern/CharacterAI-style Lorebook JSON (`entries` as JSONArray OR JSONObject map).
         * Enforces size/entry limits and deduplicates identical entries safely.
         */
        fun importLorebookJson(rawJson: String): ImportedLorebookBundle {
            if (rawJson.length > MAX_IMPORT_JSON_CHARS) {
                throw IllegalArgumentException("Lorebook JSON boyutu izin verilen sınırı aşıyor (${MAX_IMPORT_JSON_CHARS / (1024 * 1024)} MB).")
            }
            val trimmed = rawJson.trim()
            if (trimmed.isEmpty()) {
                throw IllegalArgumentException("Lorebook JSON içeriği boş.")
            }
            val root = JSONObject(trimmed)
            val lorebookId = UUID.randomUUID().toString()
            val name = root.optString("name", "").ifBlank {
                root.optString("title", "İçe Aktarılan Ansiklopedi")
            }.trim().take(120)
            val description = root.optString("description", "").trim().take(1_000)
            val tokenBudget = root.optInt("tokenBudget", root.optInt("token_budget", 750)).coerceIn(150, 3_000)
            val now = System.currentTimeMillis()

            val entries = mutableListOf<LorebookEntryEntity>()
            val seenSignatures = HashSet<String>()
            val rawEntries = root.opt("entries")

            fun parseSingleEntry(obj: JSONObject, fallbackIdx: Int): LorebookEntryEntity? {
                val content = obj.optString("content", obj.optString("entry", "")).trim().take(8_000)
                if (content.isBlank()) return null
                val title = obj.optString("title", obj.optString("comment", obj.optString("name", "Madde ${fallbackIdx + 1}")))
                    .trim()
                    .ifBlank { "Madde ${fallbackIdx + 1}" }
                    .take(120)
                val sig = "${normalizeTr(title)}||${normalizeTr(content)}"
                if (!seenSignatures.add(sig)) return null

                val keywordsStr = when {
                    obj.optString("keywords", "").isNotBlank() -> obj.optString("keywords")
                    obj.optJSONArray("keys") != null -> {
                        val a = obj.getJSONArray("keys")
                        (0 until a.length()).mapNotNull { idx -> a.optString(idx).takeIf { it.isNotBlank() } }.joinToString(", ")
                    }
                    obj.optJSONArray("key") != null -> {
                        val a = obj.getJSONArray("key")
                        (0 until a.length()).mapNotNull { idx -> a.optString(idx).takeIf { it.isNotBlank() } }.joinToString(", ")
                    }
                    else -> ""
                }.trim().take(1_000)
                val priority = obj.optInt("priority", obj.optInt("order", 5)).coerceIn(1, 10)
                val isConstant = obj.optBoolean("constant", obj.optBoolean("isConstant", false))
                val isEnabled = if (obj.has("disable")) !obj.optBoolean("disable", false) else obj.optBoolean("enabled", true)
                val category = obj.optString("category", "genel")
                    .trim()
                    .lowercase(Locale.ROOT)
                    .ifBlank { "genel" }
                    .take(32)
                val scanDepth = obj.optInt("scanDepth", obj.optInt("depth", 4)).coerceIn(1, 12)
                return LorebookEntryEntity(
                    id = UUID.randomUUID().toString(),
                    lorebookId = lorebookId,
                    title = title,
                    keywords = keywordsStr,
                    content = content,
                    category = category,
                    priority = priority,
                    isConstant = isConstant,
                    isEnabled = isEnabled,
                    scanDepth = scanDepth,
                    updatedAt = now
                )
            }

            when (rawEntries) {
                is JSONArray -> {
                    for (i in 0 until rawEntries.length()) {
                        if (entries.size >= MAX_IMPORT_ENTRIES) break
                        rawEntries.optJSONObject(i)?.let { obj ->
                            parseSingleEntry(obj, i)?.let { entries.add(it) }
                        }
                    }
                }
                is JSONObject -> {
                    val keysIter = rawEntries.keys()
                    var idx = 0
                    while (keysIter.hasNext() && entries.size < MAX_IMPORT_ENTRIES) {
                        val k = keysIter.next()
                        rawEntries.optJSONObject(k)?.let { obj ->
                            parseSingleEntry(obj, idx++)?.let { entries.add(it) }
                        }
                    }
                }
            }

            val lorebook = LorebookEntity(
                id = lorebookId,
                name = name,
                description = description,
                tokenBudget = tokenBudget,
                isEnabled = true,
                createdAt = now,
                updatedAt = now
            )
            return ImportedLorebookBundle(lorebook, entries)
        }

        /**
         * Non-throwing wrapper around [importLorebookJson] for defensive callers.
         */
        fun safeImportLorebookJson(rawJson: String): Result<ImportedLorebookBundle> =
            runCatching { importLorebookJson(rawJson) }

        /**
         * Built-in rich starter Lorebook for the new Nevermore Academy — Aiden Blackwood universe.
         */
        fun createNevermoreStarterPack(): ImportedLorebookBundle {
            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val lorebook = LorebookEntity(
                id = id,
                name = "Nevermore & Blackwood Mirası",
                description = "Aiden Blackwood'un Nevermore Akademisi yarı-kurucu mirası, 5 Güç Formu, Galatasaray aile kolyesi ve Jericho/Nevermore evren kuralları.",
                tokenBudget = 850,
                isEnabled = true,
                createdAt = now,
                updatedAt = now
            )
            val entries = listOf(
                LorebookEntryEntity(
                    lorebookId = id,
                    title = "Blackwood Soyu ve Nevermore Yarı-Sahipliği",
                    keywords = "blackwood, kurucu, sahiplik, konsey, miras, nevermore",
                    content = "Blackwood ailesi Nevermore Akademisi'nin gerçek kurucularıdır; okulun yarısı yasal olarak Aiden Blackwood'a, diğer yarısı devlete aittir. Aiden doğduğunda güçlü dışlanmış aileler birleşerek Blackwood katliamını gerçekleştirdi; soyun yüzyıllık gücü mühürlenerek tek hayatta kalan Aiden'a aktarıldı. Kurucular Konseyi bu hisseden gizlice kurtulmanın yollarını arar.",
                    category = "kurum",
                    priority = 10,
                    isConstant = true,
                    updatedAt = now
                ),
                LorebookEntryEntity(
                    lorebookId = id,
                    title = "Galatasaray Amblemli Aile Kolyesi",
                    keywords = "kolye, galatasaray, amblem, boyun, gömlek, enerji, dengeleyici",
                    content = "Aiden'ın boynundan hiç çıkarmadığı, Galatasaray amblemi taşıyan aile yadigarı kolye bir güç dengeleyici ve güçlendiricidir. Güç kullanıldığında hafif kızıl bir parıltı yayar ve göğsüne temas eden metal harcanan enerjiyle orantılı ısınır. Aiden gergin, öfkeli veya karar aşamasındayken farkında olmadan kolyeyi parmaklarıyla çevirir ya da sıkıca tutar.",
                    category = "esya",
                    priority = 9,
                    isConstant = false,
                    updatedAt = now
                ),
                LorebookEntryEntity(
                    lorebookId = id,
                    title = "5 Güç Formu (Şu An: 2. Form — Uyanış)",
                    keywords = "form, uyanış, kabullenme, tam miras, efsane, seviye, güç",
                    content = "1. Form (Uyku Hali - Geçildi): Kontrolsüz patlamalar. 2. Form (Uyanış - ŞU AN): Kırmızı Gözlü Tahakküm, Karanlık Enerji, Gelişmiş Fizyoloji, Psikometrik Dokunuş, Işınlanma ve başlangıç Atalar Bağlantısı aktif; kontrol eksik ve bedeller serttir. 3. Form (Kabullenme): Kan manipülasyonu, illüzyon ve zaman parçası görüsü açılır. 4. Form (Tam Miras): Enerji emme ve ruh çağırma. 5. Form (Uyanmış Efsane): Tüm mühürlü nesil gücünün saf zirvesi.",
                    category = "guc",
                    priority = 9,
                    isConstant = false,
                    updatedAt = now
                ),
                LorebookEntryEntity(
                    lorebookId = id,
                    title = "Zihin Kontrolü Bedeli & 3 Kademeli Aşırı Yüklenme",
                    keywords = "zihin, kontrol, tahakküm, hipnoz, dokunma, acı, aşırı yüklenme, kırmızı göz, kahverengi, burun kanaması",
                    content = "Zihin kontrolü sınırını aştığında Aiden'ın bedenine yapılan her fiziksel temas, tüm sinir uçlarına yayılan yakıcı bir acı verir (sınır aşımına göre saatlerden günlere kadar sürer). Güç Aşırı Yüklenmesi 3 kademelidir: 1. Kademe hafif göz yanması/baş dönmesi; 2. Kademe burun kanaması ve bitkinlik; 3. Kademe (kritik) kırmızı gözler sönüp normal koyu/kahverengi insan gözlerine döner, tüm güçler günlerce kapanır ve Aiden savunmasız kalır.",
                    category = "kural",
                    priority = 10,
                    isConstant = false,
                    updatedAt = now
                ),
                LorebookEntryEntity(
                    lorebookId = id,
                    title = "Blackwood Malikaneleri ve Bakıcı Kaya",
                    keywords = "malikane, ev, kaya, jericho, bakıcı, mühür, aile evi, kript, mahzen",
                    content = "Jericho dışındaki Ana Blackwood Malikanesi katliamdan beri mühürlü ve terk edilmiştir; duvarları psikometrik yankılar ve eski soy sırlarıyla doludur. Aiden, sakin ve sadık vasisi Kaya ile birlikte İkinci Malikane'de büyümüştür. Kaya sıradan bir yardımcı değil, Blackwood tarihinin son canlı tanığıdır. Ophelia Hall altındaki Kurucu Mahzeni'nde mühürlü Kan Defteri bulunur.",
                    category = "mekan",
                    priority = 8,
                    isConstant = false,
                    updatedAt = now
                ),
                LorebookEntryEntity(
                    lorebookId = id,
                    title = "Nevermore Güç Dengeleri ve Kilit Kişiler",
                    keywords = "wednesday, enid, bianca, xavier, tyler, weems, thornhill, eugene, ophelia, rowan, slater",
                    content = "Wednesday Addams Aiden'ı bir sır ve rakip olarak görür; Enid Sinclair sıcaklığıyla Aiden'ın duvarlarını zorlar; Bianca Barclay (Siren) zihin kontrolüne direnebilen nadir kişidir; Xavier Thorpe rüyalarında Aiden'ın kırmızı gözlerini çizer; Baş Öğretmen Larissa Weems yarı-sahiplik nedeniyle Aiden'a hem temkinli hem saygılıdır; Profesör Ophelia Raven soyun eski sırlarını bilir; antikacı Slater yasak Blackwood kalıntılarını tanır.",
                    category = "karakter",
                    priority = 8,
                    isConstant = false,
                    updatedAt = now
                )
            )
            return ImportedLorebookBundle(lorebook, entries)
        }
    }

    /**
     * Loads all enabled Lorebooks linked to [botId], evaluates keyword + constant + 1-step chained triggers
     * against [recentMessages], and returns the formatted RP prompt block.
     */
    suspend fun buildLorebookPromptBlockForBot(
        botId: String,
        recentMessages: List<String>,
        compactMode: Boolean = false
    ): SelectionResult {
        val localDao = dao ?: return SelectionResult(emptyList(), 0, "")
        val linkedLorebooks = try {
            localDao.getEnabledLorebooksForBot(botId)
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            emptyList()
        }
        if (linkedLorebooks.isEmpty()) {
            return SelectionResult(emptyList(), 0, "")
        }

        val deduplicatedEntries = LinkedHashMap<String, LorebookEntryEntity>()
        var combinedBudget = 0
        for (lb in linkedLorebooks) {
            combinedBudget += lb.tokenBudget.coerceIn(120, 2_000)
            try {
                localDao.getEntriesForLorebook(lb.id).forEach { entry ->
                    deduplicatedEntries.putIfAbsent(entry.id, entry)
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Exception) {}
        }
        val effectiveBudget = if (compactMode) {
            combinedBudget.coerceAtMost(450)
        } else {
            combinedBudget.coerceAtMost(1_500)
        }
        return selectEntriesForContext(
            entries = deduplicatedEntries.values.toList(),
            recentMessageTexts = recentMessages,
            tokenBudget = effectiveBudget,
            allowChainedTriggering = true
        )
    }

    suspend fun saveImportedBundle(bundle: ImportedLorebookBundle, linkToBotId: String? = null): LorebookEntity {
        val localDao = dao ?: return bundle.lorebook
        localDao.upsertLorebook(bundle.lorebook)
        if (bundle.entries.isNotEmpty()) {
            localDao.upsertEntries(bundle.entries)
        }
        if (!linkToBotId.isNullOrBlank()) {
            localDao.linkBotToLorebook(BotLorebookLinkEntity(botId = linkToBotId, lorebookId = bundle.lorebook.id))
        }
        return bundle.lorebook
    }
}
