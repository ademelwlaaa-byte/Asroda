package com.example.util

/**
 * Provides clear Turkish target word-count hints for Story Director response length presets.
 */
object ResponseLengthTargetHelper {

    fun describeTarget(responseLengthKey: String): String {
        return when (responseLengthKey.trim().lowercase(java.util.Locale.ROOT)) {
            "short" -> "Hedef: ~60-120 kelime (hızlı diyalog)"
            "detailed" -> "Hedef: ~300-450 kelime (zengin betimleme)"
            "novel" -> "Hedef: ~500+ kelime (uzun edebi bölüm)"
            else -> "Hedef: ~150-250 kelime (dengeli akış)"
        }
    }
}
