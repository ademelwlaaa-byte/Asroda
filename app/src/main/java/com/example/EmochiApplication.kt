package com.example

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.util.Log
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.data.local.AppDatabase
import com.example.data.repository.EmochiRepository
import com.example.util.AppFeatureFlags
import com.example.util.FrostedBackdropCache
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import org.json.JSONObject

class EmochiApplication : Application(), ImageLoaderFactory {

    companion object {
        const val HEADER_CALL_TYPE = "X-Emochi-Call-Type"
        const val CALL_TYPE_NARRATIVE = "narrative"
        const val CALL_TYPE_UTILITY = "utility"

        const val UTILITY_TEMPERATURE = 0.15
        const val UTILITY_TOP_P = 0.80
        const val UTILITY_DEFAULT_MAX_TOKENS = 512

        const val NARRATIVE_TEMPERATURE = 0.90
        const val NARRATIVE_TOP_P = 0.95

        const val OPENROUTER_REFERER = "https://emochi.app"
        const val OPENROUTER_TITLE = "Emochi"
        private const val MAX_POLICY_BODY_BYTES = 2L * 1024L * 1024L

        @Volatile
        private var sharedRepository: EmochiRepository? = null

        /**
         * Process-wide shared repository instance so UI ViewModel and all background Workers
         * share a single CoroutineScope, Moshi instance, and MemoryVectorCache without leaks.
         */
        fun getSharedRepository(context: Context): EmochiRepository {
            return sharedRepository ?: synchronized(this) {
                sharedRepository ?: EmochiRepository(
                    AppDatabase.getInstance(context.applicationContext),
                    context.applicationContext
                ).also { sharedRepository = it }
            }
        }

        /**
         * Coroutine-propagated call-type context so precompiled LLM adapters that do not accept
         * custom headers still receive strict utility vs. narrative temperature isolation.
         */
        val currentCallType: ThreadLocal<String> = ThreadLocal()

        suspend fun <T> withCallType(callType: String, block: suspend () -> T): T {
            return withContext(currentCallType.asContextElement(value = callType)) {
                block()
            }
        }

        /**
         * Reads and strips the internal X-Emochi-Call-Type header, injects required OpenRouter
         * attribution headers when targeting openrouter.ai, and applies provider-appropriate
         * sampling parameters (utility: temp=0.15, top_p=0.80, bounded max_tokens; narrative:
         * temp=0.90, top_p=0.95). Guarantees X-Emochi-Call-Type NEVER leaks onto the wire.
         */
        fun applyTemperaturePolicy(originalReq: Request): Request {
            val callType = originalReq.header(HEADER_CALL_TYPE)
                ?: currentCallType.get()
                ?: CALL_TYPE_NARRATIVE
            val isUtility = callType.equals(CALL_TYPE_UTILITY, ignoreCase = true)

            val cleanBuilder = originalReq.newBuilder().removeHeader(HEADER_CALL_TYPE)
            val body = originalReq.body
            val urlStr = originalReq.url.toString()

            if (urlStr.contains("openrouter.ai", ignoreCase = true)) {
                if (originalReq.header("HTTP-Referer").isNullOrBlank()) {
                    cleanBuilder.header("HTTP-Referer", OPENROUTER_REFERER)
                }
                if (originalReq.header("X-Title").isNullOrBlank()) {
                    cleanBuilder.header("X-Title", OPENROUTER_TITLE)
                }
            }

            if (originalReq.method == "POST" && body != null &&
                (body.contentLength() <= MAX_POLICY_BODY_BYTES) &&
                (urlStr.contains("generateContent") || urlStr.contains("/chat/completions") || urlStr.contains("/messages"))
            ) {
                try {
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    val rawJson = buffer.readUtf8()
                    if (rawJson.trimStart().startsWith("{")) {
                        val jsonObj = JSONObject(rawJson)
                        val (temp, topP) = if (isUtility) {
                            UTILITY_TEMPERATURE to UTILITY_TOP_P
                        } else {
                            NARRATIVE_TEMPERATURE to NARRATIVE_TOP_P
                        }

                        if (urlStr.contains("generateContent")) {
                            val genCfg = jsonObj.optJSONObject("generationConfig") ?: JSONObject()
                            genCfg.put("temperature", temp)
                            genCfg.put("topP", topP)
                            if (isUtility) {
                                val existingMax = genCfg.optInt("maxOutputTokens", 0)
                                if (existingMax <= 0 || existingMax > 700) {
                                    genCfg.put("maxOutputTokens", UTILITY_DEFAULT_MAX_TOKENS)
                                }
                            }
                            jsonObj.put("generationConfig", genCfg)
                        } else {
                            jsonObj.put("temperature", temp)
                            if (!urlStr.contains("anthropic.com")) {
                                jsonObj.put("top_p", topP)
                            }
                            if (isUtility) {
                                val existingMax = jsonObj.optInt("max_tokens", 0)
                                if (existingMax <= 0 || existingMax > 700) {
                                    jsonObj.put("max_tokens", UTILITY_DEFAULT_MAX_TOKENS)
                                }
                            }
                        }

                        val contentType = body.contentType()
                        val newBody = jsonObj.toString().toRequestBody(contentType)
                        return cleanBuilder.post(newBody).build()
                    }
                } catch (e: Exception) {
                    Log.w("EmochiApp", "Temperature policy body parse skipped: ${e.message}")
                }
            }
            return cleanBuilder.build()
        }

        fun createTemperatureInterceptor(): Interceptor = Interceptor { chain ->
            val sanitizedRequest = applyTemperaturePolicy(chain.request())
            chain.proceed(sanitizedRequest)
        }

        val installedTemperatureInterceptor: Interceptor = createTemperatureInterceptor()
    }

    override fun onCreate() {
        super.onCreate()
        AppFeatureFlags.init(this)
        configureLlmTemperatureInterceptor()
        try {
            com.example.work.SummarizationWorker.schedule(this)
        } catch (e: Exception) {
            Log.w("EmochiApp", "SummarizationWorker schedule failed: ${e.message}")
        }
        try {
            com.example.work.BackgroundMemoryMergeWorker.schedule(this)
        } catch (e: Exception) {
            Log.w("EmochiApp", "BackgroundMemoryMergeWorker schedule failed: ${e.message}")
        }
        try {
            com.example.work.ProactiveMessageWorker.schedule(this)
            com.example.work.ProactiveMessageWorker.createNotificationChannel(this)
        } catch (e: Exception) {
            Log.w("EmochiApp", "ProactiveMessageWorker schedule failed: ${e.message}")
        }
        try {
            if (AppFeatureFlags.isRoutineNotifEnabled) {
                com.example.work.RoutineNotificationWorker.schedule(this)
            }
        } catch (e: Exception) {
            Log.w("EmochiApp", "RoutineNotificationWorker schedule failed: ${e.message}")
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW ||
            level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
        ) {
            FrostedBackdropCache.evictAll()
            try {
                Coil.imageLoader(this).memoryCache?.clear()
            } catch (_: Exception) {}
        }
    }

    private fun configureLlmTemperatureInterceptor() {
        com.example.data.api.RetrofitClient.customInterceptor = installedTemperatureInterceptor
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.18) // Cap RAM usage at 18% of available application memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(35L * 1024L * 1024L) // 35 MB LRU disk cache limit
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .allowRgb565(true) // Halves RAM usage for opaque backgrounds & covers with zero visible loss
            .crossfade(300) // Smooth 300ms visual fade-in transition
            .respectCacheHeaders(false) // Persistent local disk caching
            .build()
    }
}
