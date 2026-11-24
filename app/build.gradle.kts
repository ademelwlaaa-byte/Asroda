import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.example"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.aistudio.emochi.uczaob"
        minSdk = 24
        targetSdk = 36
        val baseVersionCode = 32
        val overrideVersionCode = providers.gradleProperty("versionCodeOverride").orNull?.toIntOrNull()
        versionCode = if (overrideVersionCode != null) {
            require(overrideVersionCode > baseVersionCode) {
                "versionCodeOverride ($overrideVersionCode) must be strictly greater than base versionCode ($baseVersionCode)."
            }
            overrideVersionCode
        } else {
            baseVersionCode
        }
        versionName = "4.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        // AAA mobile optimization: keep only Turkish & English locales (strips 75+ unused AndroidX languages)
        localeFilters += listOf("tr", "en")
    }

    packaging {
        resources {
            // Strip unused JVM/coroutine debug metadata & license bloat (ZERO visual/audio quality loss!)
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*.kotlin_module",
                "META-INF/*.version",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json"
            )
        }
    }

    signingConfigs {
        val debugKeystoreFile = file("${rootDir}/debug.keystore")
        create("debugConfig") {
            storeFile = debugKeystoreFile
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            val projectReleaseKeystore = file("${rootDir}/asroda-release.keystore")
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("STORE_PASSWORD")
                    ?: error("STORE_PASSWORD env var is required when KEYSTORE_PATH is set")
                keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
                keyPassword = System.getenv("KEY_PASSWORD")
                    ?: error("KEY_PASSWORD env var is required when KEYSTORE_PATH is set")
            } else if (projectReleaseKeystore.exists()) {
                val localProps = Properties().apply {
                    val lpFile = rootProject.file("local.properties")
                    if (lpFile.exists()) lpFile.inputStream().use { load(it) }
                }
                storeFile = projectReleaseKeystore
                storePassword = localProps.getProperty("ASRODA_STORE_PASSWORD")
                    ?: System.getenv("STORE_PASSWORD")
                    ?: error("ASRODA_STORE_PASSWORD in local.properties or STORE_PASSWORD env var is required")
                keyAlias = localProps.getProperty("ASRODA_KEY_ALIAS")
                    ?: System.getenv("KEY_ALIAS")
                    ?: "asroda_release"
                keyPassword = localProps.getProperty("ASRODA_KEY_PASSWORD")
                    ?: System.getenv("KEY_PASSWORD")
                    ?: storePassword
            } else {
                storeFile = debugKeystoreFile
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isCrunchPngs = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            signingConfig = signingConfigs.getByName("debugConfig")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty(
                    "robolectric.dependency.repo.url",
                    "https://maven-central.storage-download.googleapis.com/maven2"
                )
            }
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }
}

gradle.taskGraph.whenReady {
    val isReleasePackaging = allTasks.any { task ->
        val n = task.name.lowercase()
        n.contains("assemblerelease") || n.contains("bundlerelease") || n.contains("packagerelease")
    }
    val enforceFixedReleaseKeystore =
        providers.gradleProperty("requireReleaseKeystore").orNull.equals("true", ignoreCase = true) ||
            System.getenv("GITHUB_ACTIONS").equals("true", ignoreCase = true)
    if (isReleasePackaging && enforceFixedReleaseKeystore) {
        val envKeystore = System.getenv("KEYSTORE_PATH")
        val hasEnvKeystore = !envKeystore.isNullOrBlank() && file(envKeystore).exists()
        val hasLocalReleaseKeystore = file("${rootDir}/asroda-release.keystore").exists()
        if (!hasEnvKeystore && !hasLocalReleaseKeystore) {
            throw GradleException(
                "Release build blocked: fixed release keystore (KEYSTORE_PATH or asroda-release.keystore) is required. Falling back to debug or random keystore is prohibited."
            )
        }
    }
}

secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
    ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
    implementation(files("libs/precompiled_missing.jar"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(libs.converter.moshi)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit)

    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    ksp(libs.androidx.room.compiler)
    ksp(libs.moshi.kotlin.codegen)
}
