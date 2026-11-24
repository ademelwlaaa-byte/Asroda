package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates clean, filesystem-safe export filenames from Turkish story titles.
 */
object StoryExportFilenameHelper {

    private val nonAlnumRegex = Regex("[^a-z0-9]+")

    fun buildMarkdownFilename(title: String, nowMs: Long = System.currentTimeMillis()): String {
        val slug = ChatSearchHelper.foldTurkish(title.trim())
            .replace(nonAlnumRegex, "_")
            .trim('_')
            .take(40)
            .ifBlank { "asroda_hikaye" }
        val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(nowMs))
        return "${slug}_asroda_${dateStamp}.md"
    }
}
