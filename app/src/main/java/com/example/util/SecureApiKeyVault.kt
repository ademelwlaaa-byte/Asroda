package com.example.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.example.data.local.UserSettingsEntity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Auditable, open-source Android Keystore AES-256-GCM vault for all 8 built-in API keys
 * stored in [UserSettingsEntity], plus log secret redaction.
 *
 * Wire/storage format: `ENC_V2:<base64(12-byte IV || ciphertext+128-bit GCM tag)>`.
 * Backwards-compatible: unprefixed legacy plaintext keys decrypt seamlessly as-is on read
 * and are upgraded to encrypted `ENC_V2:` ciphertext on the next settings write.
 */
object SecureApiKeyVault {

    private const val TAG = "SecureApiKeyVault"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "emochi_api_keys_aes_gcm_v2"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    const val CIPHER_PREFIX = "ENC_V2:"

    private val keyLock = Any()

    @Volatile
    private var cachedKeystoreKey: SecretKey? = null

    @Volatile
    private var ephemeralFallbackKey: SecretKey? = null

    private fun getOrCreateSecretKey(): SecretKey {
        cachedKeystoreKey?.let { return it }
        synchronized(keyLock) {
            cachedKeystoreKey?.let { return it }
            // 1. Primary path: hardware-backed AndroidKeyStore (all real Android devices API 24+)
            try {
                val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                val existing = ks.getKey(KEY_ALIAS, null) as? SecretKey
                if (existing != null) {
                    cachedKeystoreKey = existing
                    return existing
                }

                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()
                keyGenerator.init(spec)
                return keyGenerator.generateKey().also { cachedKeystoreKey = it }
            } catch (_: Throwable) {
                // 2. Unit-test / Robolectric JVM fallback where AndroidKeyStore provider is unavailable
                return ephemeralFallbackKey ?: KeyGenerator.getInstance("AES").apply { init(256) }
                    .generateKey()
                    .also { ephemeralFallbackKey = it }
            }
        }
    }

    private val KNOWN_PLAINTEXT_KEY_PREFIXES = listOf(
        "sk-", "AIza", "gsk_", "nvapi-", "xai-", "hf_", "ghp_", "github_pat_", "pplx-", "fw_", "tgp_v1_"
    )

    private fun isPrintableApiKey(candidate: String): Boolean {
        if (candidate.isBlank()) return false
        return candidate.all { ch -> ch.code in 0x21..0x7E }
    }

    /**
     * Universal decryptor for both `ENC_V2:` vault ciphertext, legacy [KeystoreEncryptionManager]
     * unprefixed Base64 ciphertext, and legacy plaintext keys.
     *
     * Always handles `ENC_V2:` first so legacy [KeystoreEncryptionManager] never corrupts vault keys.
     */
    fun decryptFlexible(storedValue: String): String {
        val trimmed = storedValue.trim()
        if (trimmed.isEmpty()) return ""
        if (isEncrypted(trimmed)) {
            return decrypt(trimmed)
        }
        // Known plaintext API keys must never be fed to legacy Base64 decryptors
        if (KNOWN_PLAINTEXT_KEY_PREFIXES.any { trimmed.startsWith(it, ignoreCase = false) }) {
            return trimmed
        }
        // Attempt legacy KeystoreEncryptionManager decryption for old custom provider keys
        val legacyAttempt = try {
            KeystoreEncryptionManager.decrypt(trimmed).trim()
        } catch (_: Throwable) {
            ""
        }
        if (legacyAttempt.isNotBlank() && isPrintableApiKey(legacyAttempt)) {
            return legacyAttempt
        }
        return trimmed
    }

    fun isEncrypted(value: String): Boolean = value.startsWith(CIPHER_PREFIX)

    fun encrypt(plaintext: String): String {
        val trimmed = plaintext.trim()
        if (trimmed.isEmpty() || isEncrypted(trimmed)) return trimmed
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(trimmed.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + ciphertext.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)
            CIPHER_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.w(TAG, "Encryption failed; refusing silent plaintext downgrade: ${e.message}")
            throw IllegalStateException("API anahtarı şifrelenemedi", e)
        }
    }

    fun decrypt(storedValue: String): String {
        val trimmed = storedValue.trim()
        if (trimmed.isEmpty()) return ""
        if (!isEncrypted(trimmed)) return trimmed // Legacy plaintext transparent migration
        return try {
            val payload = Base64.decode(trimmed.removePrefix(CIPHER_PREFIX), Base64.NO_WRAP)
            if (payload.size <= GCM_IV_LENGTH_BYTES) return ""
            val iv = payload.copyOfRange(0, GCM_IV_LENGTH_BYTES)
            val ciphertext = payload.copyOfRange(GCM_IV_LENGTH_BYTES, payload.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.w(TAG, "Decryption failed (key invalidated or corrupted): ${e.message}")
            ""
        }
    }

    fun encryptSettingsKeys(settings: UserSettingsEntity): UserSettingsEntity = settings.copy(
        customApiKey = encrypt(settings.customApiKey),
        groqApiKey = encrypt(settings.groqApiKey),
        claudeApiKey = encrypt(settings.claudeApiKey),
        openaiApiKey = encrypt(settings.openaiApiKey),
        openRouterApiKey = encrypt(settings.openRouterApiKey),
        nvidiaApiKey = encrypt(settings.nvidiaApiKey),
        mistralApiKey = encrypt(settings.mistralApiKey),
        backupApiKey = encrypt(settings.backupApiKey)
    )

    fun decryptSettingsKeys(settings: UserSettingsEntity): UserSettingsEntity = settings.copy(
        customApiKey = decrypt(settings.customApiKey),
        groqApiKey = decrypt(settings.groqApiKey),
        claudeApiKey = decrypt(settings.claudeApiKey),
        openaiApiKey = decrypt(settings.openaiApiKey),
        openRouterApiKey = decrypt(settings.openRouterApiKey),
        nvidiaApiKey = decrypt(settings.nvidiaApiKey),
        mistralApiKey = decrypt(settings.mistralApiKey),
        backupApiKey = decrypt(settings.backupApiKey)
    )

    val SENSITIVE_HTTP_HEADERS: Set<String> = setOf(
        "Authorization",
        "x-api-key",
        "api-key",
        "x-goog-api-key",
        "Cookie",
        "Set-Cookie"
    )

    private val SECRET_LOG_PATTERNS = listOf(
        Regex("""\bAIza[0-9A-Za-z\-_]{20,}""") to "[REDACTED_GEMINI_KEY]",
        Regex("""\bsk-(?:ant-|or-|proj-)?[0-9A-Za-z\-_]{16,}""") to "[REDACTED_API_KEY]",
        Regex("""\bgsk_[0-9A-Za-z\-_]{16,}""") to "[REDACTED_GROQ_KEY]",
        Regex("""\bnvapi-[0-9A-Za-z\-_]{16,}""") to "[REDACTED_NVIDIA_KEY]",
        Regex("""\bxai-[0-9A-Za-z\-_]{16,}""") to "[REDACTED_XAI_KEY]",
        Regex("""\bhf_[0-9A-Za-z]{16,}""") to "[REDACTED_HF_TOKEN]",
        Regex("""\b(?:ghp_|github_pat_)[0-9A-Za-z_]{16,}""") to "[REDACTED_GITHUB_TOKEN]",
        Regex("""\bpplx-[0-9A-Za-z\-_]{16,}""") to "[REDACTED_PERPLEXITY_KEY]",
        Regex("""\bfw_[0-9A-Za-z\-_]{16,}""") to "[REDACTED_FIREWORKS_KEY]",
        Regex("""\btgp_v1_[0-9A-Za-z\-_]{16,}""") to "[REDACTED_TOGETHER_KEY]",
        Regex("""(?i)([?&](?:api_)?key=)[^&\s]+""") to "$1[REDACTED]",
        Regex("""(?i)(Bearer\s+)[0-9A-Za-z\-_.]{8,}""") to "$1[REDACTED]",
        Regex("""(?i)((?:Authorization|x-api-key|api-key|x-goog-api-key)\s*[:=]\s*)[^\r\n,;]+""") to "$1[REDACTED]",
        Regex("""(?i)("(?:apiKey|api_key|apiKeyEncrypted|access_token|secret_key|token)"\s*:\s*")[^"]+(")""") to "$1[REDACTED]$2"
    )

    /** Scrubs API keys, sensitive auth headers, JSON token fields, and bearer tokens before any log write. */
    fun redactSecrets(raw: String): String {
        if (raw.isBlank()) return raw
        var out = raw
        for ((pattern, replacement) in SECRET_LOG_PATTERNS) {
            out = pattern.replace(out, replacement)
        }
        return out
    }

    /**
     * Release-safe OkHttp logging interceptor:
     * - Strictly Level.NONE in Release (!isDebug)
     * - Level.BASIC at most in Debug (never logs raw bodies/prompts/messages)
     * - Always redacts Authorization, x-api-key, x-goog-api-key, and Cookie headers
     */
    fun createSafeHttpLoggingInterceptor(
        isDebug: Boolean = com.example.BuildConfig.DEBUG
    ): okhttp3.logging.HttpLoggingInterceptor {
        val logger = okhttp3.logging.HttpLoggingInterceptor.Logger { message ->
            if (isDebug) {
                Log.d("EmochiHttp", redactSecrets(message))
            }
        }
        return okhttp3.logging.HttpLoggingInterceptor(logger).apply {
            SENSITIVE_HTTP_HEADERS.forEach { header -> redactHeader(header) }
            level = if (isDebug) {
                okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
            } else {
                okhttp3.logging.HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /** Safe debug logger: strict no-op in Release builds and always redacts secrets in Debug. */
    fun safeDebugLog(
        tag: String,
        message: String,
        isDebug: Boolean = com.example.BuildConfig.DEBUG
    ) {
        if (!isDebug) return
        Log.d(tag, redactSecrets(message))
    }
}
