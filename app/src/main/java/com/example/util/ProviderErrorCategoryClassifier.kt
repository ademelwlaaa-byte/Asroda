package com.example.util

import com.example.data.repository.ProviderFallbackLogEntry
import java.util.Locale

/**
 * Classifies provider fallback failure entries into concise Turkish error categories
 * and summarizes the dominant failure cause across a log batch.
 */
object ProviderErrorCategoryClassifier {

    enum class Category(val badge: String) {
        AUTH("🔑 Kimlik"),
        RATE_LIMIT("⏳ Hız Limiti"),
        QUOTA("💳 Kota"),
        TIMEOUT("⏱️ Zaman Aşımı"),
        MODEL_NOT_FOUND("🤖 Model"),
        SERVER("🛠️ Sunucu"),
        NETWORK("📡 Ağ"),
        UNKNOWN("⚠️ Diğer")
    }

    fun classify(statusCode: Int?, errorMessage: String?): Category {
        val msg = errorMessage.orEmpty().lowercase(Locale.ROOT)
        return when {
            statusCode == 401 || statusCode == 403 || msg.contains("unauthorized") || msg.contains("invalid api key") || msg.contains("forbidden") ->
                Category.AUTH
            statusCode == 402 || msg.contains("quota") || msg.contains("insufficient") || msg.contains("billing") || msg.contains("credit") ->
                Category.QUOTA
            statusCode == 429 || msg.contains("rate limit") || msg.contains("too many requests") ->
                Category.RATE_LIMIT
            statusCode == 404 || msg.contains("model_not_found") || msg.contains("not found") ->
                Category.MODEL_NOT_FOUND
            statusCode == 408 || statusCode == 504 || msg.contains("timeout") || msg.contains("timed out") ->
                Category.TIMEOUT
            statusCode != null && statusCode in 500..599 ->
                Category.SERVER
            msg.contains("unable to resolve host") || msg.contains("connect") || msg.contains("network") || msg.contains("socket") ->
                Category.NETWORK
            else -> Category.UNKNOWN
        }
    }

    fun summarizeDominantCategory(logs: List<ProviderFallbackLogEntry>): String {
        val failures = logs.filter { it.status == "FAILED" }
        if (failures.isEmpty()) return ""
        val grouped = failures
            .groupingBy { classify(it.statusCode, it.errorMessage) }
            .eachCount()
        val top = grouped.maxByOrNull { it.value } ?: return ""
        return "Baskın Hata: ${top.key.badge} (${top.value}/${failures.size})"
    }
}
