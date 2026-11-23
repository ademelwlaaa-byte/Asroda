package com.example

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReleaseUpdateHygieneTest {

    private fun resolveAppDir(): File {
        val candidates = listOf(File("."), File("app"))
        return candidates.firstOrNull { File(it, "build.gradle.kts").exists() && File(it, "src/main/AndroidManifest.xml").exists() }
            ?: error("Could not locate app/ directory from ${File(".").absolutePath}")
    }

    private fun resolveRepoRoot(): File {
        val appDir = resolveAppDir().canonicalFile
        return if (appDir.name == "app") appDir.parentFile else appDir
    }

    // ── 1. Riskli İzin & Global Cleartext Yasağı Testi ──────────────────────

    @Test
    fun test1_manifestForbidsHighRiskPermissionsAndGlobalCleartext() {
        val manifest = File(resolveAppDir(), "src/main/AndroidManifest.xml").readText()

        assertFalse("REQUEST_INSTALL_PACKAGES must NOT be in AndroidManifest.xml", manifest.contains("REQUEST_INSTALL_PACKAGES"))
        assertFalse("QUERY_ALL_PACKAGES must NOT be in AndroidManifest.xml", manifest.contains("QUERY_ALL_PACKAGES"))
        assertFalse("SYSTEM_ALERT_WINDOW must NOT be in AndroidManifest.xml", manifest.contains("SYSTEM_ALERT_WINDOW"))
        assertFalse("READ_EXTERNAL_STORAGE must NOT be in AndroidManifest.xml", manifest.contains("READ_EXTERNAL_STORAGE"))
        assertFalse("WRITE_EXTERNAL_STORAGE must NOT be in AndroidManifest.xml", manifest.contains("WRITE_EXTERNAL_STORAGE"))
        assertFalse("Global usesCleartextTraffic=true must NOT be set", manifest.contains("usesCleartextTraffic=\"true\""))

        assertTrue(
            "AndroidManifest.xml must bind @xml/network_security_config",
            manifest.contains("android:networkSecurityConfig=\"@xml/network_security_config\"")
        )
        assertTrue("MainActivity must explicitly declare android:exported=\"true\"", manifest.contains("android:exported=\"true\""))
        assertTrue("FileProvider must explicitly declare android:exported=\"false\"", manifest.contains("android:exported=\"false\""))
    }

    // ── 2. Ağ Güvenlik Yapılandırması Testi ─────────────────────────────────

    @Test
    fun test2_networkSecurityConfigEnforcesHttpsWithLocalLoopbackOnly() {
        val netConfigFile = File(resolveAppDir(), "src/main/res/xml/network_security_config.xml")
        assertTrue("network_security_config.xml must exist", netConfigFile.exists())
        val xml = netConfigFile.readText()

        assertTrue(
            "base-config must set cleartextTrafficPermitted=\"false\"",
            xml.contains("<base-config cleartextTrafficPermitted=\"false\">")
        )
        assertTrue("System trust-anchors must be preserved", xml.contains("<certificates src=\"system\""))
        assertTrue("Localhost Ollama exception must be present", xml.contains(">localhost</domain>"))
        assertTrue("127.0.0.1 loopback exception must be present", xml.contains(">127.0.0.1</domain>"))
        assertTrue("10.0.2.2 emulator loopback exception must be present", xml.contains(">10.0.2.2</domain>"))
    }

    // ── 3. Sabit Keystore Kilidi & V1/V2/V3 Release Hijyeni Testi ───────────

    @Test
    fun test3_gradleAndCiLockFixedReleaseKeystoreAndV1V2V3Signing() {
        val gradleText = File(resolveAppDir(), "build.gradle.kts").readText()
        assertFalse("Must never invoke random keytool -genkey in Gradle", gradleText.contains("keytool -genkey"))
        assertTrue("V1 signing must be enabled", gradleText.contains("enableV1Signing = true"))
        assertTrue("V2 signing must be enabled", gradleText.contains("enableV2Signing = true"))
        assertTrue("V3 signing must be enabled", gradleText.contains("enableV3Signing = true"))
        assertTrue("Release buildType must explicitly set isDebuggable = false", gradleText.contains("isDebuggable = false"))
        assertTrue("Release buildType must enable R8 minification", gradleText.contains("isMinifyEnabled = true"))
        assertTrue("Release buildType must enable resource shrinking", gradleText.contains("isShrinkResources = true"))
        assertTrue("Release build must block missing release keystore in CI/strict mode", gradleText.contains("requireReleaseKeystore"))
        assertTrue("versionCodeOverride must enforce monotonic growth", gradleText.contains("versionCodeOverride"))

        val workflowFile = File(resolveRepoRoot(), ".github/workflows/android-release.yml")
        assertTrue("android-release.yml workflow must exist", workflowFile.exists())
        val ciText = workflowFile.readText()
        assertTrue("CI must run testDebugUnitTest before assembleRelease", ciText.contains("testDebugUnitTest"))
        assertTrue("CI must verify V1/V2/V3 with apksigner", ciText.contains("apksigner") && ciText.contains("Verified using v3 scheme"))
        assertTrue("CI must support EXPECTED_CERT_SHA256 lock", ciText.contains("EXPECTED_CERT_SHA256"))
        assertTrue("CI must verify PNG/WebP resource integrity", ciText.contains("EF BF BD"))
    }

    // ── 4. Daraltılmış file_paths.xml Testi ─────────────────────────────────

    @Test
    fun test4_filePathsXmlIsStrictlyScopedToCacheExports() {
        val fpFile = File(resolveAppDir(), "src/main/res/xml/file_paths.xml")
        assertTrue("file_paths.xml must exist", fpFile.exists())
        val xml = fpFile.readText()

        assertFalse("Unsafe <root-path must NEVER appear in file_paths.xml", xml.contains("<root-path"))
        assertFalse("Unsafe <external-path must NEVER appear in file_paths.xml", xml.contains("<external-path"))
        assertTrue("Scoped cache-path exports/ must be configured", xml.contains("<cache-path") && xml.contains("exports/"))
    }

    // ── 5. Uygulama İçi Güncelleyici Dokunulmazlık & Splash Hijyeni Testi ───

    @Test
    fun test5_noInAppApkInstallerAddedAndSeamlessSplashConfigured() {
        val mainSrcRoot = File(resolveAppDir(), "src/main/java")
        val kotlinFiles = mainSrcRoot.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

        assertFalse(
            "Must NOT add AppUpdateManager in-app updater class",
            kotlinFiles.any { it.name.contains("AppUpdateManager", ignoreCase = true) }
        )
        assertFalse(
            "Must NOT invoke android.content.pm.PackageInstaller.Session",
            kotlinFiles.any { it.readText().contains("PackageInstaller.Session") }
        )

        // Verify Android 12+ splash screen suppresses the giant circular logo splash
        val v31Theme = File(resolveAppDir(), "src/main/res/values-v31/themes.xml")
        assertTrue("values-v31/themes.xml must exist for seamless launch", v31Theme.exists())
        val themeXml = v31Theme.readText()
        assertTrue(
            "API 31+ theme must set transparent splash icon",
            themeXml.contains("windowSplashScreenAnimatedIcon") && themeXml.contains("ic_splash_transparent")
        )
        assertTrue(
            "API 31+ theme must set zero splash animation duration",
            themeXml.contains("windowSplashScreenAnimationDuration")
        )
    }

    // ── 6. ProGuard Release Log Temizliği Kontrolü ──────────────────────────

    @Test
    fun test6_proguardStripsDebugAndVerboseLogsInRelease() {
        val proguard = File(resolveAppDir(), "proguard-rules.pro").readText()
        assertTrue("ProGuard must strip Log.v/Log.d via -assumenosideeffects", proguard.contains("-assumenosideeffects class android.util.Log"))
        assertTrue("ProGuard must strip android.util.Log.d", proguard.contains("public static int d(...);"))
        assertTrue("ProGuard must strip android.util.Log.v", proguard.contains("public static int v(...);"))
    }
}
