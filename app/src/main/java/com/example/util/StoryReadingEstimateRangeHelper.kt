package com.example.util

import kotlin.math.ceil

/**
 * Formats a realistic Turkish reading-time range accounting for slow (160 WPM) to fast (240 WPM) readers.
 */
object StoryReadingEstimateRangeHelper {

    fun formatRange(wordCount: Int): String {
        if (wordCount <= 0) return "< 1 dk"
        val fastMin = ceil(wordCount / 240.0).toInt().coerceAtLeast(1)
        val slowMin = ceil(wordCount / 160.0).toInt().coerceAtLeast(fastMin)
        return if (fastMin == slowMin) "~$fastMin dk" else "~$fastMin–$slowMin dk"
    }
}
