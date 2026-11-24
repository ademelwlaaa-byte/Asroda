package com.example.util

import java.util.Locale

/**
 * Abbreviation-aware Turkish sentence splitter for natural TTS chunking.
 * Prevents awkward mid-phrase pauses after honorifics and abbreviations like "Dr.", "Prof.", "Sn.".
 */
object TtsSentenceChunker {

    private val trLocale = Locale.forLanguageTag("tr-TR")
    private val protectedAbbrevs = setOf(
        "dr.", "prof.", "doç.", "yrd.", "sn.", "av.", "alb.", "yzb.", "bnb.",
        "vb.", "vs.", "bkz.", "yy.", "s.", "sy.", "no.", "st.", "mr.", "mrs.", "ms."
    )

    fun splitSentences(text: String): List<String> {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.isEmpty()) return emptyList()

        val result = mutableListOf<String>()
        val current = StringBuilder()
        var i = 0
        while (i < clean.length) {
            val ch = clean[i]
            current.append(ch)
            val isBoundaryPunct = ch == '.' || ch == '!' || ch == '?'
            val nextIsSpaceOrEnd = (i + 1 >= clean.length) || clean[i + 1].isWhitespace()
            if (isBoundaryPunct && nextIsSpaceOrEnd) {
                // Skip ellipsis runs ("...") until the last dot
                if (ch == '.' && i + 1 < clean.length && clean[i + 1] == '.') {
                    i++
                    continue
                }
                val lastToken = current.toString().trim().substringAfterLast(' ').lowercase(trLocale)
                if (ch == '.' && lastToken in protectedAbbrevs) {
                    i++
                    continue
                }
                val sentence = current.toString().trim()
                if (sentence.isNotEmpty()) result.add(sentence)
                current.setLength(0)
            }
            i++
        }
        val tail = current.toString().trim()
        if (tail.isNotEmpty()) result.add(tail)
        return result
    }
}
