package com.example.util

/**
 * Computes bounded exponential backoff delays for transient AI provider retries.
 */
object ProviderRetryDelayAdvisor {

    fun computeDelayMs(
        attemptIndex: Int,
        baseDelayMs: Long = 500L,
        maxDelayMs: Long = 8_000L
    ): Long {
        val safeAttempt = attemptIndex.coerceIn(0, 10)
        val safeBase = baseDelayMs.coerceAtLeast(100L)
        val safeMax = maxDelayMs.coerceAtLeast(safeBase)
        val factor = 1L shl safeAttempt
        return (safeBase * factor).coerceAtMost(safeMax)
    }
}
