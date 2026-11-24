package com.example.util

import java.util.Locale

/**
 * Multi-token separator-tolerant filter for Custom API model lists
 * (e.g. "llama 3.3 free" matches "meta-llama/llama-3.3-70b-instruct:free").
 */
object ProviderModelFilterHelper {

    fun filterModels(models: List<String>, rawQuery: String): List<String> {
        val tokens = rawQuery.trim()
            .lowercase(Locale.ROOT)
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
        if (tokens.isEmpty()) return models
        return models.filter { model ->
            val lower = model.lowercase(Locale.ROOT)
            tokens.all { tok -> lower.contains(tok) }
        }
    }
}
