# ══════════════════════════════════════════════════════════════════════════════
# ProGuard / R8 Optimization Rules — Zero-Loss Runtime, DTO & JAR Safety
# ══════════════════════════════════════════════════════════════════════════════

# Keep Kotlin metadata and coroutines internals
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable
-keep class kotlin.Metadata { *; }

# Keep all app data entities, DTOs, API models, book engine models, and JSON-serialized classes
-keep class com.example.data.** { *; }
-keep class com.example.book.** { *; }
-keep class com.example.ui.viewmodel.** { *; }
-keep class com.example.ui.screens.cosmic3d.** { *; }
-keep class com.example.R$drawable { *; }
-keep class com.example.R$raw { *; }

# Explicit DTO & Backup / Emotion / Micro-AI Serialization Protection (Sistem #19)
-keep class com.example.data.repository.BackupSnapshot { *; }
-keep class com.example.data.managers.BackupSnapshotManager { *; }
-keep class com.example.data.local.EmotionState { *; }
-keep class com.example.data.local.PersonalityProfile { *; }
-keep class com.example.data.local.CustomProviderEntity { *; }
-keep class com.example.data.api.MemoryToolModels** { *; }
-keep class com.example.data.repository.BaselineEmotionEngine$BaselineAnalysisResult { *; }
-keep class com.example.data.repository.MicroAiCastMemberDto { *; }
-keep class com.example.data.repository.MicroAiCastResponse { *; }
-keep class com.example.util.MicroAiSentimentDto { *; }
-keep class com.example.util.SentimentAnalysisResult { *; }

# Keep Moshi @Json annotated fields across all packages
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# ══════════════════════════════════════════════════════════════════════════════
# Audited app/libs/precompiled_missing.jar Inventory (102 classes, 0 shadowed):
# - com.example.data.api.*   (GeminiAdapter, GeminiApiService, Gemini* + JsonAdapters,
#                             GenericOpenAICompatibleAdapter, GenericAnthropicCompatibleAdapter,
#                             LLMAdapterFactory, RetrofitClient, ReliabilityTier, TokenUsage)
# - com.example.data.local.* (Core Room Entities & DAOs — covered by com.example.data.**)
# - com.example.ui.theme.*   (AppColors, Spacing)
# - com.example.util.*       (KeystoreEncryptionManager, OutputQualityValidator,
#                             ProviderRateLimitTracker, Llm7RateLimitTracker, QuotaType, etc.)
# - com.example.work.*       (SummarizationWorker)
# ══════════════════════════════════════════════════════════════════════════════
-keep class com.example.ui.theme.AppColors { *; }
-keep class com.example.ui.theme.Spacing { *; }
-keep class com.example.util.KeystoreEncryptionManager { *; }
-keep class com.example.util.OutputQualityValidator { *; }
-keep class com.example.util.ProviderRateLimitTracker { *; }
-keep class com.example.util.ProviderLimitConfig { *; }
-keep class com.example.util.ProviderTrackerState { *; }
-keep class com.example.util.ProviderUsageStats { *; }
-keep class com.example.util.Llm7RateLimitTracker { *; }
-keep class com.example.util.Llm7UsageStats { *; }
-keep class com.example.util.QuotaType { *; }
-keep class com.example.work.SummarizationWorker { *; }

# Keep Room database, DAOs, and generated implementations
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep Moshi adapters and @JsonClass models
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Keep Retrofit service interfaces and method signatures
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep WorkManager Workers
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep Coil & AndroidX ExifInterface internals
-keep class coil.** { *; }
-dontwarn coil.**
-keep class androidx.exifinterface.** { *; }

# Keep BuildConfig fields
-keep class com.example.BuildConfig { *; }

# Strip verbose & debug Logcat calls in Release builds (Sistem #19 Log Safety)
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
