package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats epoch millisecond timestamps into concise Turkish relative labels.
 */
object MessageTimestampFormatter {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    fun formatRelative(timestampMs: Long, nowMs: Long = System.currentTimeMillis()): String {
        if (timestampMs <= 0L) return ""
        val diffMs = (nowMs - timestampMs).coerceAtLeast(0L)
        val mins = diffMs / 60_000L
        val hours = diffMs / 3_600_000L
        val days = diffMs / 86_400_000L
        return when {
            mins < 1L -> "Az önce"
            mins < 60L -> "$mins dk önce"
            hours < 24L -> "$hours sa önce"
            days == 1L -> "Dün"
            days < 7L -> "$days gün önce"
            else -> SimpleDateFormat("dd MMM", trLocale).format(Date(timestampMs))
        }
    }
}
