package com.example.util

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Estimates Turkish-aware LLM token usage and context window fill percentage for Story Stats.
 * Turkish agglutinative morphology averages ~1.35 tokens per whitespace-delimited word.
 */
object TokenBudgetEstimator {

    private const val TURKISH_TOKENS_PER_WORD = 1.35

    fun estimateTokens(wordCount: Int): Int {
        if (wordCount <= 0) return 0
        return (wordCount * TURKISH_TOKENS_PER_WORD).roundToInt().coerceAtLeast(1)
    }

    fun formatTokenCount(tokens: Int): String {
        val safe = tokens.coerceAtLeast(0)
        return when {
            safe < 1_000 -> "$safe token"
            else -> String.format(Locale.US, "~%.1fK token", safe / 1000.0)
        }
    }

    fun contextFillPercent(tokens: Int, contextLimitTokens: Int = 128_000): Int {
        if (tokens <= 0 || contextLimitTokens <= 0) return 0
        return ((tokens.toDouble() / contextLimitTokens.toDouble()) * 100.0)
            .roundToInt()
            .coerceIn(1, 100)
    }
}
