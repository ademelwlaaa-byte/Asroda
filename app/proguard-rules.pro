# ══════════════════════════════════════════════════════════════════════════════
# ProGuard / R8 Optimization Rules — Zero-Loss Image & Runtime Safety
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
