package com.example.util

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe rolling latency tracker (last 10 samples per provider) computing median & average ms.
 */
object ProviderRollingLatencyTracker {

    private const val MAX_SAMPLES = 10
    private val samples = ConcurrentHashMap<String, ArrayDeque<Long>>()

    data class LatencyMetrics(
        val sampleCount: Int,
        val averageMs: Long,
        val medianMs: Long,
        val minMs: Long,
        val maxMs: Long
    )

    fun recordSample(providerKey: String, latencyMs: Long) {
        if (latencyMs <= 0L) return
        val key = providerKey.trim().lowercase(Locale.ROOT)
        if (key.isEmpty()) return
        val q = samples.getOrPut(key) { ArrayDeque() }
        synchronized(q) {
            if (q.size >= MAX_SAMPLES) q.removeFirst()
            q.addLast(latencyMs)
        }
    }

    fun getMetrics(providerKey: String): LatencyMetrics? {
        val key = providerKey.trim().lowercase(Locale.ROOT)
        val q = samples[key] ?: return null
        val snap = synchronized(q) { q.toList() }
        if (snap.isEmpty()) return null
        val sorted = snap.sorted()
        val avg = snap.sum() / snap.size
        val med = sorted[sorted.size / 2]
        return LatencyMetrics(
            sampleCount = snap.size,
            averageMs = avg,
            medianMs = med,
            minMs = sorted.first(),
            maxMs = sorted.last()
        )
    }

    fun clearAll() {
        samples.clear()
    }
}
