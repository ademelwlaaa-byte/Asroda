package com.example.util

import org.json.JSONObject

/**
 * Fast pre-flight inspector for Asroda JSON backup files so users can preview exact counts
 * (bots, messages, custom APIs, key inclusion) before confirming Merge or Overwrite restore.
 */
object BackupHealthChecker {

    data class Preview(
        val isValidJson: Boolean,
        val schemaVersion: Int,
        val botCount: Int,
        val messageCount: Int,
        val customProviderCount: Int,
        val includesPlaintextKeys: Boolean
    )

    private val BUILTIN_KEY_FIELDS = listOf(
        "customApiKey", "geminiApiKey", "openaiApiKey", "claudeApiKey",
        "groqApiKey", "openRouterApiKey", "nvidiaApiKey", "mistralApiKey", "backupApiKey"
    )

    fun inspectJson(rawJson: String): Preview {
        if (rawJson.isBlank()) {
            return Preview(false, 0, 0, 0, 0, false)
        }
        return try {
            val root = JSONObject(rawJson.trim())
            if (!root.has("bots") && !root.has("settings") && !root.has("messages")) {
                return Preview(false, 0, 0, 0, 0, false)
            }
            val version = root.optInt("version", root.optInt("schemaVersion", 1))
            val bots = root.optJSONArray("bots")?.length() ?: 0
            val msgs = root.optJSONArray("messages")?.length() ?: 0
            val customProvidersArr = root.optJSONArray("customProviders")
            val customProviders = customProvidersArr?.length() ?: 0
            val settings = root.optJSONObject("settings")
            val hasSettingsKeys = settings != null && BUILTIN_KEY_FIELDS.any { field ->
                val v = settings.optString(field).trim()
                v.isNotBlank() && !SecureApiKeyVault.isEncrypted(v)
            }
            var hasCustomKeys = false
            if (customProvidersArr != null) {
                for (i in 0 until customProvidersArr.length()) {
                    val cpKey = customProvidersArr.optJSONObject(i)?.optString("apiKeyEncrypted")?.trim().orEmpty()
                    if (cpKey.isNotBlank() && !SecureApiKeyVault.isEncrypted(cpKey)) {
                        hasCustomKeys = true
                        break
                    }
                }
            }
            Preview(
                isValidJson = true,
                schemaVersion = version,
                botCount = bots,
                messageCount = msgs,
                customProviderCount = customProviders,
                includesPlaintextKeys = hasSettingsKeys || hasCustomKeys
            )
        } catch (_: Exception) {
            Preview(false, 0, 0, 0, 0, false)
        }
    }
}
