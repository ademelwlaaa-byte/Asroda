package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

class EmochiApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        configureNovelAiInterceptor()
        try {
            com.example.work.SummarizationWorker.schedule(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            com.example.work.BackgroundMemoryMergeWorker.schedule(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            com.example.work.ProactiveMessageWorker.schedule(this)
            com.example.work.ProactiveMessageWorker.createNotificationChannel(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun configureNovelAiInterceptor() {
        com.example.data.api.RetrofitClient.customInterceptor = okhttp3.Interceptor { chain ->
            val originalReq = chain.request()
            val body = originalReq.body
            val urlStr = originalReq.url.toString()
            if (originalReq.method == "POST" && body != null &&
                (urlStr.contains("generateContent") || urlStr.contains("/chat/completions") || urlStr.contains("/messages"))
            ) {
                try {
                    val buffer = okio.Buffer()
                    body.writeTo(buffer)
                    val rawJson = buffer.readUtf8()
                    if (rawJson.trimStart().startsWith("{")) {
                        val jsonObj = org.json.JSONObject(rawJson)
                        if (urlStr.contains("generateContent")) {
                            val genCfg = jsonObj.optJSONObject("generationConfig") ?: org.json.JSONObject()
                            genCfg.put("temperature", 0.90)
                            genCfg.put("topP", 0.95)
                            jsonObj.put("generationConfig", genCfg)
                        } else {
                            jsonObj.put("temperature", 0.90)
                            if (!urlStr.contains("anthropic.com")) {
                                jsonObj.put("top_p", 0.95)
                            }
                        }
                        val contentType = body.contentType()
                        val newBody = okhttp3.RequestBody.create(contentType, jsonObj.toString())
                        val newReq = originalReq.newBuilder().post(newBody).build()
                        return@Interceptor chain.proceed(newReq)
                    }
                } catch (_: Exception) {
                }
            }
            chain.proceed(originalReq)
        }
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
