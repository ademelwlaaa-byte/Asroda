package com.example.data.repository

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * TimePerceptionEngine — Hikâye Takvimi Sistemi.
 * Gerçek dünya zamanından tamamen bağımsız; zaman yalnızca hikâye atlamalarıyla ilerler.
 */
object TimePerceptionEngine {

    private const val MAX_DAYS_PER_JUMP = 3650

    data class StoryTimeJumpResult(
        val daysToAdvance: Int = 0,
        val newSceneTime: String? = null
    )

    data class StoryTimeInfo(
        val storyCalendarDate: String,
        val storyDayCounter: Long = 1L,
        val currentAge: Int = 20,
        val sceneTime: String = "",
        val promptBlock: String = ""
    )

    private val VALID_SCENE_TIMES = setOf("sabah", "öğle", "akşam", "gece")

    fun validateSceneTime(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val lower = raw.lowercase(Locale.forLanguageTag("tr-TR")).trim()
        return if (lower in VALID_SCENE_TIMES) lower else null
    }

    // ---------------------------------------------------------------------------
    // Türkçe sayı kelime ayrıştırıcı — önce bileşikler, sonra tekli
    // ---------------------------------------------------------------------------

    private const val WB_L = """(?<![\p{L}\p{N}])"""
    private const val WB_R = """(?![\p{L}\p{N}])"""

    private data class NumberResult(
        val intValue: Int,
        val isHalf: Boolean = false,
        val exactValue: Double = intValue.toDouble()
    )

    private fun parseTurkishNumber(text: String): NumberResult? {
        val lower = text.lowercase(Locale.forLanguageTag("tr-TR")).trim()
        if (lower.isBlank()) return null

        // Rakam ile başlıyorsa doğrudan al (örn. "1.5", "1,5", "12")
        val decimalMatch = Regex("""${WB_L}(\d+)[.,](5)$WB_R""").find(lower)
        if (decimalMatch != null) {
            val whole = decimalMatch.groupValues[1].toIntOrNull() ?: return null
            return NumberResult(intValue = whole + 1, isHalf = true, exactValue = whole + 0.5)
        }
        val digitMatch = Regex("""${WB_L}(\d+)$WB_R""").find(lower)
        if (digitMatch != null) {
            val n = digitMatch.groupValues[1].toIntOrNull() ?: return null
            return NumberResult(n)
        }

        // "bir buçuk" → exact 1.5 (intValue = 2 for day rounding)
        if (Regex("""${WB_L}bir\s+buçuk$WB_R""").containsMatchIn(lower)) {
            return NumberResult(intValue = 2, isHalf = true, exactValue = 1.5)
        }
        if (Regex("""${WB_L}iki\s+buçuk$WB_R""").containsMatchIn(lower)) {
            return NumberResult(intValue = 3, isHalf = true, exactValue = 2.5)
        }
        // "yarım" → 0.5
        if (Regex("""${WB_L}yar[ıi]m$WB_R""").containsMatchIn(lower)) {
            return NumberResult(intValue = 0, isHalf = true, exactValue = 0.5)
        }
        // "birkaç" → 3
        if (Regex("""${WB_L}birka[çc]$WB_R""").containsMatchIn(lower)) return NumberResult(3)

        // Önce bileşik on-lu sayılar (uzundan kısaya eşleştir)
        val compositeMap = listOf(
            "yirmi dokuz" to 29, "yirmi sekiz" to 28, "yirmi yedi" to 27,
            "yirmi altı" to 26, "yirmi beş" to 25, "yirmi dört" to 24,
            "yirmi üç" to 23, "yirmi iki" to 22, "yirmi bir" to 21,
            "otuz bir" to 31, "otuz iki" to 32, "otuz" to 30,
            "yirmi" to 20, "on dokuz" to 19, "on sekiz" to 18,
            "on yedi" to 17, "on altı" to 16, "on beş" to 15,
            "on dört" to 14, "on üç" to 13, "on iki" to 12,
            "on bir" to 11, "kırk beş" to 45, "kırk" to 40,
            "elli" to 50, "altmış" to 60, "yetmiş" to 70,
            "seksen" to 80, "doksan" to 90, "yüz" to 100
        )
        for ((word, num) in compositeMap) {
            if (Regex("""${WB_L}${Regex.escape(word)}$WB_R""").containsMatchIn(lower)) return NumberResult(num)
        }

        // Tekli sayılar
        val singleMap = listOf(
            "on" to 10, "dokuz" to 9, "sekiz" to 8, "yedi" to 7,
            "altı" to 6, "alti" to 6, "beş" to 5, "bes" to 5,
            "dört" to 4, "dort" to 4, "üç" to 3, "uc" to 3,
            "iki" to 2, "bir" to 1
        )
        for ((word, num) in singleMap) {
            if (Regex("""${WB_L}${Regex.escape(word)}$WB_R""").containsMatchIn(lower)) return NumberResult(num)
        }
        return null
    }

    // ---------------------------------------------------------------------------
    // parseStoryTimeJump — hikâye atlama tespiti
    // ---------------------------------------------------------------------------

    fun parseStoryTimeJump(text: String): StoryTimeJumpResult {
        if (text.isBlank()) return StoryTimeJumpResult()

        // Tırnak içi diyalog ve OOC parantezleri çıkar
        val stripped = text
            .replace(Regex(""""[^"]*""""), " ")
            .replace(Regex("""«[^»]*»"""), " ")
            .replace(Regex("""\([^)]*\)"""), " ")
            .replace(Regex("""\[[^\]]*\]"""), " ")
        val lower = stripped.lowercase(Locale.forLanguageTag("tr-TR")).trim()
        if (lower.isBlank()) return StoryTimeJumpResult()

        // --- 1. Kesinlikle ATLAma olmayan söz/plan ifadeler ---
        // "yarın" tek başına (söz), "haftaya", "gelecek ay/hafta" → ATLA
        val futurePromisePattern = Regex("""\b(yarın|yarin|haftaya|hafta\s*ya|gelecek\s+(?:ay|hafta|yıl|yil))\b""")
        if (futurePromisePattern.containsMatchIn(lower) &&
            !Regex("""\b(?:sonra|geçti|gecti|ardından|aradan)\b""").containsMatchIn(lower)) {
            return StoryTimeJumpResult()
        }

        // --- 2. Sadece saat bağlamı (gün artmaz, sceneTime güncellenir) ---
        // "birkaç saat/dakika sonra"
        val hourMinRegex = Regex(
            """\b(?:birkaç|birkac|bir|iki|üç|uc|yarım|yarim|\d+)\s+(?:saat|dakika|dk)\s*(?:sonra|geçti|gecti|ardından)\b"""
        )
        if (hourMinRegex.containsMatchIn(lower)) {
            // Scene time tahmini
            val scene = detectSceneFromContext(lower)
            return StoryTimeJumpResult(daysToAdvance = 0, newSceneTime = scene)
        }

        // Sahne geçişi ifadeleri (gün artmaz)
        val sceneShiftRegex = Regex(
            """\b(öğle(?:\s+vakti|den)?|oglen|akşamüstü|akşama\s+doğru|akşam\s+oldu|aksam\s+oldu|gece\s+oldu|gece\s+yarısı|sabah\s+oldu)\b"""
        )
        val sceneShiftMatch = sceneShiftRegex.find(lower)
        if (sceneShiftMatch != null &&
            !Regex("""\b(?:\d+|bir|iki|üç|dört|beş|altı|yedi|sekiz|dokuz|on)\s+(?:gün|hafta|ay|yıl)\b""").containsMatchIn(lower)) {
            val sceneWord = sceneShiftMatch.value
            val scene = when {
                sceneWord.contains("öğle") || sceneWord.contains("oglen") -> "öğle"
                sceneWord.contains("gece") -> "gece"
                sceneWord.contains("sabah") -> "sabah"
                else -> "akşam"
            }
            return StoryTimeJumpResult(daysToAdvance = 0, newSceneTime = scene)
        }

        // --- 3. "ertesi sabah" / "ertesi gün" / "bir sonraki sabah" → 1 gün ---
        if (Regex("""\bertesi\s+sabah\b""").containsMatchIn(lower)) {
            return StoryTimeJumpResult(daysToAdvance = 1, newSceneTime = "sabah")
        }
        if (Regex("""\b(?:ertesi\s+gün|ertesi\s+gun|sonraki\s+gün|sonraki\s+gun|bir\s+sonraki\s+sabah)\b""").containsMatchIn(lower)) {
            return StoryTimeJumpResult(daysToAdvance = 1, newSceneTime = null)
        }

        // --- 4. Gün atlamaları — Unicode kelime sınırlı ve ZORUNLU atlama fiili ---
        // "Üç gün önce tanıştık", "İki hafta boyunca kalacağım", "Bir ay sürecek" gibi normal anlatım
        // cümleleri ASLA sahte zaman atlaması tetiklemez!
        val jumpVerb = """(?:sonra|geçti|gecti|geçmişti|gecmisti|ardından|sonrasında|geçip\s+gitti)"""
        val unitDigits = """(?:bir|iki|üç|uc|dört|dort|beş|bes|altı|alti|yedi|sekiz|dokuz)"""
        val numExpr = """(?:\d+(?:[.,]5)?|(?:bir|iki)\s+buçuk|birka[çc]|(?:yirmi|otuz|kırk|elli|altmış|yetmiş|seksen|doksan|on)(?:\s+$unitDigits)?|yüz|$unitDigits)"""

        // Yıl
        val yearPattern = Regex("""${WB_L}($numExpr)\s+y[ıi]l\s+$jumpVerb$WB_R""")
        yearPattern.find(lower)?.let { m ->
            val num = parseTurkishNumber(m.groupValues[1]) ?: return@let
            val days = clamp((num.exactValue * 365.0).roundToInt())
            return StoryTimeJumpResult(daysToAdvance = days, newSceneTime = null)
        }

        // Ay — kelime sınırı + zorunlu fiil
        val monthPattern = Regex("""${WB_L}($numExpr)\s+ay\s+$jumpVerb$WB_R""")
        monthPattern.find(lower)?.let { m ->
            val num = parseTurkishNumber(m.groupValues[1]) ?: return@let
            val days = clamp((num.exactValue * 30.0).roundToInt())
            return StoryTimeJumpResult(daysToAdvance = days, newSceneTime = null)
        }

        // Hafta — "bir buçuk hafta sonra" = round(1.5 * 7) = 11 gün
        val weekPattern = Regex("""${WB_L}($numExpr)\s+hafta\s+$jumpVerb$WB_R""")
        weekPattern.find(lower)?.let { m ->
            val num = parseTurkishNumber(m.groupValues[1]) ?: return@let
            val days = clamp((num.exactValue * 7.0).roundToInt())
            return StoryTimeJumpResult(daysToAdvance = days, newSceneTime = null)
        }

        // Gün — zorunlu fiil
        val dayPattern = Regex("""${WB_L}($numExpr)\s+gün\s+$jumpVerb$WB_R""")
        dayPattern.find(lower)?.let { m ->
            val num = parseTurkishNumber(m.groupValues[1]) ?: return@let
            if (num.intValue <= 0) return@let
            val days = clamp(num.intValue)
            val scene = detectSceneFromContext(lower)
            return StoryTimeJumpResult(daysToAdvance = days, newSceneTime = scene)
        }

        // Yalın süre ifadesi desteği (yalnızca tüm metin "<sayı> <birim>" ise, örn. story_time_skip.amount = "3 gün")
        val bareDuration = Regex("""^($numExpr)\s+(y[ıi]l|ay|hafta|gün)$""").matchEntire(lower)
        if (bareDuration != null) {
            val num = parseTurkishNumber(bareDuration.groupValues[1])
            if (num != null && num.exactValue > 0.0) {
                val unit = bareDuration.groupValues[2]
                val mult = when {
                    unit.startsWith("y") -> 365.0
                    unit == "ay" -> 30.0
                    unit == "hafta" -> 7.0
                    else -> 1.0
                }
                return StoryTimeJumpResult(daysToAdvance = clamp((num.exactValue * mult).roundToInt()), newSceneTime = null)
            }
        }

        return StoryTimeJumpResult()
    }

    private fun detectSceneFromContext(lower: String): String? {
        return when {
            lower.contains("sabah") -> "sabah"
            lower.contains("öğle") || lower.contains("oglen") -> "öğle"
            lower.contains("akşam") || lower.contains("aksam") -> "akşam"
            lower.contains("gece") -> "gece"
            else -> null
        }
    }

    /**
     * System #15: Hybrid Dual-Channel Time Skip Detector.
     * - Channel 1 (Primary): [[STATE_JSON]] "skip" field (Int days or Turkish scene/jump string)
     *   or legacy "story_time_skip" JSON object.
     * - Channel 2 (Lightweight Local Fallback): If JSON omitted "skip", inspects non-quoted
     *   narrative text (*...* or prose outside "..." / «...» / (...) / [...]) for clear
     *   time-jump expressions while rejecting future promises ("Yarın sabah görüşürüz").
     */
    fun parseHybridSkip(
        rawSkipJson: Any? = null,
        legacyStorySkipObj: org.json.JSONObject? = null,
        narrativeText: String = ""
    ): StoryTimeJumpResult {
        var daysToSkip = 0
        var newSceneTime: String? = null

        // Channel 1a: Compact "skip"
        when (rawSkipJson) {
            is Number -> {
                daysToSkip = rawSkipJson.toInt().coerceIn(0, MAX_DAYS_PER_JUMP)
            }
            is String -> {
                val trimmed = rawSkipJson.trim()
                if (trimmed.isNotBlank()) {
                    if (trimmed.contains("->")) {
                        val targetPart = trimmed.substringAfterLast("->").trim()
                        newSceneTime = validateSceneTime(targetPart)
                        val parsedJump = parseStoryTimeJump(trimmed)
                        daysToSkip = parsedJump.daysToAdvance
                    } else {
                        val asInt = trimmed.toIntOrNull()
                        if (asInt != null) {
                            daysToSkip = asInt.coerceIn(0, MAX_DAYS_PER_JUMP)
                        } else {
                            val directScene = validateSceneTime(trimmed)
                            if (directScene != null) {
                                newSceneTime = directScene
                            } else {
                                val parsedJump = parseStoryTimeJump(trimmed)
                                daysToSkip = parsedJump.daysToAdvance
                                newSceneTime = parsedJump.newSceneTime ?: validateSceneTime(trimmed)
                            }
                        }
                    }
                }
            }
        }

        // Channel 1b: Legacy "story_time_skip" JSON object
        if (daysToSkip <= 0 && newSceneTime == null && legacyStorySkipObj != null && legacyStorySkipObj.optBoolean("detected", false)) {
            val daysFromJson = legacyStorySkipObj.optInt("days", 0)
            val amountStr = legacyStorySkipObj.optString("amount", "")
            val parsedFromAmount = if (amountStr.isNotBlank()) parseStoryTimeJump(amountStr) else StoryTimeJumpResult()
            daysToSkip = if (daysFromJson > 0) daysFromJson.coerceIn(0, MAX_DAYS_PER_JUMP) else parsedFromAmount.daysToAdvance
            newSceneTime = validateSceneTime(legacyStorySkipObj.optString("scene_time", "")) ?: parsedFromAmount.newSceneTime
        }

        if (daysToSkip > 0 || newSceneTime != null) {
            return StoryTimeJumpResult(daysToAdvance = daysToSkip, newSceneTime = newSceneTime)
        }

        // Channel 2: Safe non-quoted narrative fallback (excludes "..." dialogue and future promises)
        if (narrativeText.isNotBlank()) {
            val cleanedNarrative = narrativeText
                .replace(Regex("""\[\[STATE_JSON[\s\S]*?\]\]"""), " ")
                .replace(Regex("""\[\[[^\]]*\]\]"""), " ")
            val fallback = parseStoryTimeJump(cleanedNarrative)
            if (fallback.daysToAdvance > 0 || fallback.newSceneTime != null) {
                return fallback
            }
        }

        return StoryTimeJumpResult()
    }

    // ---------------------------------------------------------------------------
    // formatStoryDuration — merkezi süre ifade fonksiyonu (4.3)
    // Gerçek takvim ayı sınırlarını, storyCalendarDate üzerinden hesaplar.
    // Parametre: gün sayısı (Long). Döndürür: Türkçe doğal dil ifadesi.
    // ---------------------------------------------------------------------------
    fun formatStoryDuration(days: Long, fromDateStr: String = ""): String {
        if (days <= 0L) return "bugün"
        if (days == 1L) return "dün"
        if (days < 7L) return "birkaç gün"

        // Gerçek takvim ayı hesabı: fromDate üzerinden geriye sar (Int taşma korumalı)
        if (fromDateStr.isNotBlank() && days <= 365_000L) {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            return try {
                val fromDate = sdf.parse(fromDateStr) ?: throw Exception()
                val calFrom = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { time = fromDate }
                val calTo = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                    time = fromDate
                    add(java.util.Calendar.DAY_OF_MONTH, -days.toInt())
                }
                val totalMonths = (calFrom.get(java.util.Calendar.YEAR) - calTo.get(java.util.Calendar.YEAR)) * 12 +
                        (calFrom.get(java.util.Calendar.MONTH) - calTo.get(java.util.Calendar.MONTH))
                val totalYears = totalMonths / 12
                val remMonths = totalMonths % 12
                // Kalan günü hesapla
                val calCheck = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                    time = calTo.time
                    add(java.util.Calendar.MONTH, totalMonths)
                }
                val remDays = ((calFrom.timeInMillis - calCheck.timeInMillis) / (1000L * 60 * 60 * 24)).toInt()
                when {
                    totalYears >= 1 -> {
                        val yearStr = "$totalYears yıl"
                        val monStr = if (remMonths > 0) " $remMonths ay" else ""
                        yearStr + monStr
                    }
                    totalMonths >= 1 -> {
                        val monStr = "$totalMonths ay"
                        val dayStr = if (remDays > 0) " $remDays gün" else ""
                        monStr + dayStr
                    }
                    else -> {
                        val weeks = days / 7
                        val rem = days % 7
                        val wStr = "$weeks hafta"
                        val dStr = if (rem > 0) " $rem gün" else ""
                        wStr + dStr
                    }
                }
            } catch (_: Exception) {
                formatStoryDurationSimple(days)
            }
        }
        return formatStoryDurationSimple(days)
    }

    // Takvim referansı olmadan basit yaklaşım
    private fun formatStoryDurationSimple(days: Long): String {
        if (days < 7L) return "birkaç gün"
        if (days < 30L) {
            val weeks = days / 7
            val rem = days % 7
            return if (rem > 0) "$weeks hafta $rem gün" else "$weeks hafta"
        }
        if (days < 365L) {
            val months = days / 30
            val rem = days % 30
            return if (rem > 0) "$months ay $rem gün" else "$months ay"
        }
        val years = days / 365
        val remMonths = (days % 365) / 30
        return if (remMonths > 0) "$years yıl $remMonths ay" else "$years yıl"
    }

    private fun clamp(days: Int): Int {
        return if (days > MAX_DAYS_PER_JUMP) {
            android.util.Log.w("TimePerception", "Atlama $days gün → $MAX_DAYS_PER_JUMP ile kırpıldı")
            MAX_DAYS_PER_JUMP
        } else days
    }

    // ---------------------------------------------------------------------------
    // advanceCalendarDateAndCheckAge — Calendar tabanlı, UTC, DAY_OF_YEAR hatası yok
    // ---------------------------------------------------------------------------

    fun advanceCalendarDateAndCheckAge(
        currentCalendarDateStr: String,
        currentDayCounter: Long,
        elapsedDaysToAdd: Int,
        birthDateStr: String,
        initialAge: Int
    ): Triple<String, Long, Int> {
        val daysToAdd = elapsedDaysToAdd.coerceAtLeast(0)
        val newDayCounter = currentDayCounter + daysToAdd

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val currentDate = if (currentCalendarDateStr.isNotBlank()) {
            try { sdf.parse(currentCalendarDateStr) } catch (_: Exception) { null }
        } else null

        val newCalendarDateStr = if (currentDate != null) {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.time = currentDate
            if (daysToAdd > 0) cal.add(Calendar.DAY_OF_MONTH, daysToAdd)
            sdf.format(cal.time)
        } else ""

        var newAge = initialAge
        if (birthDateStr.isNotBlank() && newCalendarDateStr.isNotBlank()) {
            try {
                val birthDate = sdf.parse(birthDateStr)
                val newDate = sdf.parse(newCalendarDateStr)
                if (birthDate != null && newDate != null) {
                    val calNew = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = newDate }
                    val calBirth = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = birthDate }

                    var age = calNew.get(Calendar.YEAR) - calBirth.get(Calendar.YEAR)
                    // Doğum günü bu yıl henüz geçmediyse bir eksilt (ay/gün karşılaştırması)
                    val newMM = calNew.get(Calendar.MONTH) * 100 + calNew.get(Calendar.DAY_OF_MONTH)
                    val birthMM = calBirth.get(Calendar.MONTH) * 100 + calBirth.get(Calendar.DAY_OF_MONTH)
                    if (newMM < birthMM) age--
                    if (age > 0) newAge = age
                }
            } catch (_: Exception) {}
        } else if (birthDateStr.isBlank()) {
            val extraYears = ((newDayCounter - 1) / 365).toInt()
            newAge = initialAge + extraYears
        }

        return Triple(newCalendarDateStr, newDayCounter, newAge)
    }

    // ---------------------------------------------------------------------------
    // buildStoryTimePromptBlock — tek satır, koşullu, gerçek zaman yok
    // ---------------------------------------------------------------------------

    fun buildStoryTimePromptBlock(
        storyDayCounter: Long,
        storyCalendarDate: String,
        currentAge: Int,
        sceneTime: String = "",
        hasExplicitStoryDate: Boolean = false
    ): String {
        if (storyDayCounter <= 0L) return ""

        val validScene = validateSceneTime(sceneTime)

        val parts = mutableListOf<String>()
        parts.add("Gün #$storyDayCounter")
        if (hasExplicitStoryDate && storyCalendarDate.isNotBlank()) {
            parts.add("Tarih: $storyCalendarDate")
        }
        if (currentAge > 0) {
            parts.add("Yaş: $currentAge")
        }
        if (validScene != null) {
            parts.add("Sahne: $validScene")
        }

        val singleLine = parts.joinToString(" | ")

        val moodHint = when (validScene) {
            "sabah" -> " (sabah: sakin/mahmur ton)"
            "öğle"  -> " (öğle: canlı/odaklı ton)"
            "akşam" -> " (akşam: dinlenmiş/yumuşak ton)"
            "gece"  -> " (gece: uykulu/samimi ton)"
            else    -> ""
        }

        return "\n## HİKÂYE ZAMANI: $singleLine$moodHint\n" +
               "- KURAL: Gerçek dünya saati ve tarihi yoktur. Zaman yalnızca hikâyedeki zaman atlamalarıyla ilerler. Kullanıcı ara verse de sohbet kesintisiz devam eder.\n"
    }
}
