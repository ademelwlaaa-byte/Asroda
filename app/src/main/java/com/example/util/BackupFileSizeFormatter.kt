package com.example.util

import java.util.Locale

/**
 * Formats byte counts into concise Turkish human-readable size strings (B, KB, MB, GB).
 */
object BackupFileSizeFormatter {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    fun formatBytes(bytes: Long): String {
        val safe = bytes.coerceAtLeast(0L)
        return when {
            safe < 1024L -> "$safe B"
            safe < 1024L * 1024L -> String.format(trLocale, "%.1f KB", safe / 1024.0)
            safe < 1024L * 1024L * 1024L -> String.format(trLocale, "%.2f MB", safe / (1024.0 * 1024.0))
            else -> String.format(trLocale, "%.2f GB", safe / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
