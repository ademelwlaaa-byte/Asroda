package com.example.util

import java.util.Locale

/**
 * Abbreviation- and ordinal-aware Turkish sentence splitter for natural TTS chunking.
 * Prevents awkward mid-phrase pauses after honorifics/abbreviations ("Dr.", "Prof.", "Cad.", "Mah."),
 * Turkish ordinals ("1. sınıf", "2. bölüm"), and handles closing quotes ("\"Gidelim.\" dedi.")
 * as well as ASCII ("...") and Unicode ("…") ellipses cleanly.
 */
object TtsSentenceChunker {

    private val trLocale = Locale.forLanguageTag("tr-TR")
    private val protectedAbbrevs = setOf(
        // Honorifics, academic, legal & military titles
        "dr.", "prof.", "doç.", "yrd.", "sn.", "av.", "alb.", "yzb.", "bnb.",
        "hz.", "org.", "gen.", "yb.", "tğm.", "çvş.", "uzm.", "müh.", "mim.",
        "mr.", "mrs.", "ms.", "st.",
        // Address & location
        "cad.", "sok.", "mah.", "apt.", "blv.", "kat.", "no.",
        // Common Turkish text, reference & unit abbreviations
        "vb.", "vs.", "bkz.", "bk.", "yy.", "s.", "sy.", "sf.", "c.",
        "dk.", "sa.", "tl.", "kr.", "km.", "cm.", "mm.", "kg.", "gr.", "lt.",
        "ltd.", "şti.", "a.ş.", "inc.", "etc."
    )

    private val boundaryPuncts = setOf('.', '!', '?', '…')
    private val closingWrappers = setOf('"', '”', '»', '’', '\'', ')', ']')
    private val leadingWrappers = charArrayOf('"', '“', '«', '‘', '\'', '(', '[')
    private val turkishOrdinalRegex = Regex("""^\d{1,3}\.$""")

    fun splitSentences(text: String): List<String> {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.isEmpty()) return emptyList()

        val result = mutableListOf<String>()
        val current = StringBuilder()
        var i = 0
        while (i < clean.length) {
            val ch = clean[i]
            current.append(ch)

            if (ch in boundaryPuncts) {
                var punctCount = 1
                var lastPunct = ch
                // Consume consecutive boundary punctuation runs ("...", "?!", "!?", "…!")
                while (i + 1 < clean.length && clean[i + 1] in boundaryPuncts) {
                    i++
                    lastPunct = clean[i]
                    current.append(lastPunct)
                    punctCount++
                }
                val isSingleDot = punctCount == 1 && lastPunct == '.'

                // Attach any immediately following closing quotes/brackets (e.g. `"Gidelim." dedi.`)
                var hadClosingWrapper = false
                while (i + 1 < clean.length && clean[i + 1] in closingWrappers) {
                    i++
                    current.append(clean[i])
                    hadClosingWrapper = true
                }

                val nextIsSpaceOrEnd = (i + 1 >= clean.length) || clean[i + 1].isWhitespace()
                if (nextIsSpaceOrEnd) {
                    val hasMoreText = i + 1 < clean.length
                    if (isSingleDot && !hadClosingWrapper && hasMoreText) {
                        val rawLastToken = current.toString().trim().substringAfterLast(' ')
                        val normalizedToken = rawLastToken.trimStart(*leadingWrappers).lowercase(trLocale)
                        if (normalizedToken in protectedAbbrevs || turkishOrdinalRegex.matches(normalizedToken)) {
                            i++
                            continue
                        }
                    }
                    val sentence = current.toString().trim()
                    if (sentence.isNotEmpty()) result.add(sentence)
                    current.setLength(0)
                }
            }
            i++
        }
        val tail = current.toString().trim()
        if (tail.isNotEmpty()) result.add(tail)
        return result
    }
}
