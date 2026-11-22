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

    @Volatile
    private var ephemeralFallbackKey: SecretKey? = null

    private fun getOrCreateSecretKey(): SecretKey {
        // 1. Primary path: hardware-backed AndroidKeyStore (all real Android devices API 24+)
        try {
            val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val existing = ks.getKey(KEY_ALIAS, null) as? SecretKey
            if (existing != null) return existing

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
            return keyGenerator.generateKey()
        } catch (_: Throwable) {
            // 2. Unit-test / Robolectric JVM fallback where AndroidKeyStore provider is unavailable
            return ephemeralFallbackKey ?: synchronized(this) {
                ephemeralFallbackKey ?: KeyGenerator.getInstance("AES").apply { init(256) }
                    .generateKey()
                    .also { ephemeralFallbackKey = it }
            }
        }
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

    private val SECRET_LOG_PATTERNS = listOf(
        Regex("""\bAIza[0-9A-Za-z\-_]{20,}"""),
        Regex("""\bsk-(?:ant-|or-|proj-)?[0-9A-Za-z\-_]{16,}"""),
        Regex("""\bgsk_[0-9A-Za-z\-_]{16,}"""),
        Regex("""\bnvapi-[0-9A-Za-z\-_]{16,}"""),
        Regex("""(?i)([?&]key=)[^&\s]+"""),
        Regex("""(?i)(Bearer\s+)[0-9A-Za-z\-_.]{12,}""")
    )

    /** Scrubs API keys / bearer tokens before writing any message into fallback or error logs. */
    fun redactSecrets(raw: String): String {
        if (raw.isBlank()) return raw
        var out = raw
        out = SECRET_LOG_PATTERNS[0].replace(out, "[REDACTED_GEMINI_KEY]")
        out = SECRET_LOG_PATTERNS[1].replace(out, "[REDACTED_API_KEY]")
        out = SECRET_LOG_PATTERNS[2].replace(out, "[REDACTED_GROQ_KEY]")
        out = SECRET_LOG_PATTERNS[3].replace(out, "[REDACTED_NVIDIA_KEY]")
        out = SECRET_LOG_PATTERNS[4].replace(out, "$1[REDACTED]")
        out = SECRET_LOG_PATTERNS[5].replace(out, "$1[REDACTED]")
        return out
    }
}
