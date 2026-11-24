package com.example.book

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats a concise Turkish journey/completion certificate for Book Mode sharing.
 */
object StoryCompletionCertificateHelper {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    fun buildCertificate(
        state: BookReadState,
        totalChapters: Int = CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS,
        nowMs: Long = System.currentTimeMillis()
    ): String {
        val progress = BookProgressSummaryFormatter.compute(state, totalChapters)
        val bond = BookBondSummaryHelper.compute(state)
        val dateText = SimpleDateFormat("dd MMMM yyyy", trLocale).format(Date(nowMs))
        return buildString {
            append("✦ KOZMİK SÜRÜKLENİŞ — OKUR KRONİK BELGESİ ✦\n")
            append("Tarih: ").append(dateText).append("\n")
            append(BookProgressSummaryFormatter.formatRibbon(progress)).append("\n")
            append(BookBondSummaryHelper.formatCompact(bond))
        }
    }
}
