# 🛡️ EMOCHI (ASRODA) — SORUNSUZ APK ÜSTÜNE GÜNCELLEME, SABİT İMZA & RİSK HİJYENİ ŞARTNAMESİ
**Kapsam:** `Sabit Release Keystore Kilidi (V1+V2+V3)` · `Otomatik Sürüm/İmza Doğrulama (CI)` · `Play Protect & OEM Güvenlik Tarayıcısı Risk Hijyeni` · `Sıkı Ağ & Manifest Güvenliği`

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Bu şartname **uygulama içi güncelleme (in-app updater) DEĞİLDİR!** Uygulama içindeki `UpdateCheckerModal` ve güncelleme butonuna **KESİNLİKLE DOKUNMA** (şimdilik işlevsiz/ertelendi kalacak).
>
> **Tek ve Net Hedef:** Yeni sürüm `.apk` dosyasını telefona indirip dokunduğunda Android sistem yükleyicisinin *"Uygulama yüklenmedi / Paket çakışması"* hatası vermeden doğrudan **"Güncelle"** diyerek mevcut verilerin (botlar, sohbetler, ayarlar) üstüne sorunsuz kurulmasını ve Play Protect / Xiaomi / Samsung güvenlik tarayıcılarının çıkardığı gereksiz *"Güvenlik Riski"* uyarılarının teknik olarak en aza indirilmesini sağlamaktır.
>
> **Kırmızı Çizgiler:**
> 1. **Uygulama İçi Güncelleyici Yasağı:** `AppUpdateManager`, `PackageInstaller.Session`, `version.json` veya `REQUEST_INSTALL_PACKAGES` iznini **KESİNLİKLE EKLEME!** *(Kullanılmayan `REQUEST_INSTALL_PACKAGES` izni Play Protect ve OEM tarayıcılarda en yüksek risk puanı üreten izinlerden biridir.)*
> 2. **Kısaltma & Regresyon Yasağı:** `// ...`, yarım metod veya mevcut test dosyalarını kıran değişiklik **KESİNLİKLE YASAKTIR**.
> 3. **Domino Etkisi Koruması:** Mor Cam UI tasarımı, `drawable-nodpi/` altındaki mevcut tüm WebP varlıkları, yerel Ollama bağlantısı ve Room v48 veritabanı şeması **asla bozulmayacaktır**.

---

## 🔑 BÖLÜM 1: SABİT RELEASE İMZA KİLİDİ & PAKET ÇAKIŞMASI SIFIRLAMA

Android'de yeni bir APK'nın mevcut uygulamanın üstüne **"Güncelle"** diyerek veri silmeden kurulabilmesi için **3 şartın %100 tutması gerekir:** (1) Aynı `applicationId`, (2) Birebir aynı imzalama sertifikası, (3) Daha yüksek `versionCode`.

### 🛠️ Uygulama Kuralları (`app/build.gradle.kts` & `.github/workflows/android-release.yml`)
1. **Rastgele / Geçici Keystore Yasağı (`app/build.gradle.kts`):**
   - `signingConfigs.release` yapılandırmasında keystore dosyası veya şifreleri eksikse sessizce `debug.keystore`'a düşme ya da rastgele yeni anahtar üretme davranışını **tamamen yasakla**; Release derlemesinde keystore eksikse açık hata fırlatıp derlemeyi durdur.
   - İmzalama şemalarını eksiksiz kilitle:
     ```kotlin
     enableV1Signing = true
     enableV2Signing = true
     enableV3Signing = true
     ```
   - `buildTypes.release` içinde şunları kesinleştir:
     * `isDebuggable = false` *(Debuggable bir APK güvenlik tarayıcıları tarafından doğrudan güvensiz/riskli işaretlenir!)*
     * `isMinifyEnabled = true`
     * `isShrinkResources = true`
     * `signingConfig = signingConfigs.getByName("release")`
   - `applicationId` (`com.aistudio.emochi.uczaob`) hiçbir sürümde değiştirilmeyecek şekilde sabit kalsın.
2. **Sürüm Kodu (`versionCode`) & Sertifika Doğrulama Kapısı (`.github/workflows/android-release.yml`):**
   - `assembleRelease` öncesinde zorunlu `./gradlew testDebugUnitTest --stacktrace` adımını çalıştır.
   - Derleme sonrasında `apksigner verify --verbose --print-certs <release.apk>` çalıştır ve şu 3 kontrolü otomatik yap:
     1. V1, V2 ve V3 imzalarının tamamı `true` (`Verified using v1/v2/v3 scheme`).
     2. İmza sertifikasının `SHA-256` parmak izini (`EXPECTED_CERT_SHA256` secret/var tanımlıysa birebir eşleştir; uyuşmazsa derlemeyi **FAIL** et ki yanlış imzalı APK asla eline geçmesin!).
     3. APK içindeki `versionCode` değerini (`aapt2 dump badging`) ve `SHA-256` dosya özetini GitHub Actions Step Summary'ye okunaklı yaz.
   - İsteğe bağlı güvenli `versionCode` override desteği ekle: `app/build.gradle.kts` içinde `versionCode` varsayılan değerini korusun ama CI'dan `-PversionCodeOverride=...` verilirse yalnızca mevcut taban değerden büyük olduğu sürece kabul etsin (yanlışlıkla düşük `versionCode` basıp güncelleme hatası almayı engeller).

---

## 🛡️ BÖLÜM 2: PLAY PROTECT & OEM "RİSKLİ UYGULAMA" HİJYENİ

Google Play Protect, Xiaomi Güvenlik, Samsung Device Care ve Huawei tarayıcılarının bir APK'yı "riskli" işaretlemesine neden olan tüm teknik bayrakları tek tek temizle:

### 1. Sıkı `AndroidManifest.xml` & Minimum Yetki Temizliği
- **Yasaklı / Gereksiz İzin Denetimi:** Aşağıdaki yüksek riskli izinlerin hiçbirinin manifestte (veya bağımlılıkların birleşik manifestinde) yer almadığını doğrula; varsa kaldır (gerekirse `tools:node="remove"` ile):
  * `REQUEST_INSTALL_PACKAGES`
  * `QUERY_ALL_PACKAGES`
  * `SYSTEM_ALERT_WINDOW`
  * `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` (Android 10+ scoped storage / photo picker kullanıldığı için eski geniş depolama izinleri gereksiz risk üretir; yalnızca gerçekten şartsa `android:maxSdkVersion="28"` ile kısıtla)
- **Açık `android:exported` Denetimi:** Manifest'teki tüm `<activity>`, `<service>`, `<receiver>` ve `<provider>` tanımlarında `android:exported` açıkça belirtilsin; yalnızca başlatıcı `MainActivity` `exported="true"`, diğer tüm iç bileşenler ve `FileProvider` kesinlikle `exported="false"` olsun.
- **Daraltılmış `file_paths.xml`:** `app/src/main/res/xml/file_paths.xml` dosyasında yalnızca uygulamanın kendi `cache-path` (`exports/`) yolu tanımlı olsun; geniş `<root-path>` veya `<external-path path="." />` gibi güvenlik açığı sayılan tanımlar kesinlikle bulunmasın.

### 2. Global `usesCleartextTraffic="true"` Yerine Akıllı `network_security_config.xml`
Uygulama genelinde `android:usesCleartextTraffic="true"` açık bırakmak Play Protect ve Android güvenlik tarayıcılarında **en yaygın uyarı sebeplerinden biridir**.
- `app/src/main/res/xml/network_security_config.xml` *(YENİ)* dosyasını oluştur:
  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <network-security-config>
      <!-- Tüm dış internet trafiği zorunlu HTTPS (Play Protect & güvenlik uyumlu) -->
      <base-config cleartextTrafficPermitted="false">
          <trust-anchors>
              <certificates src="system" />
          </trust-anchors>
      </base-config>

      <!-- Yalnızca yerel cihaz / yerel ağ Ollama kurulumları için kontrollü HTTP izni -->
      <domain-config cleartextTrafficPermitted="true">
          <domain includeSubdomains="false">localhost</domain>
          <domain includeSubdomains="false">127.0.0.1</domain>
          <domain includeSubdomains="false">10.0.2.2</domain>
      </domain-config>
  </network-security-config>
  ```
- `AndroidManifest.xml` içindeki `<application>` etiketinden `android:usesCleartextTraffic="true"` varsa kaldır ve yerine **`android:networkSecurityConfig="@xml/network_security_config"`** bağla.
- *Not:* Kullanıcı özel bir yerel LAN IP'si (ör. `192.168.1.x:11434` Ollama) kullanıyorsa ve HTTPS değilse, `ApiHubSheets` / bağlantı testi katmanında kullanıcıya net bilgi verilsin; genel internetin tamamı HTTPS korumalı kalsın.

### 3. Paket Bütünlüğü, R8 Temizliği & Sıfır Hassas Log
- **Release Log & Debug İzlerinin Temizlenmesi (`proguard-rules.pro` & `EmochiApplication.kt`):**
  * `proguard-rules.pro` içine Release derlemede `android.util.Log.v` ve `android.util.Log.d` çağrılarını temizleyen `-assumenosideeffects` kuralını ekle (API istekleri, kullanıcı mesajları veya anahtarlar Logcat'e sızmaz).
  * `OkHttp` HTTP logging interceptor'ı yalnızca `BuildConfig.DEBUG` altında çalışsın.
- **Paket Kaynak Bütünlüğü (CI Kontrolü):**
  * `.github/workflows/android-release.yml` içine `app/src/main/res/` altındaki tüm `.png` ve `.webp` dosyalarının geçerli dosya başlığına sahip olduğunu (sıfır `EF BF BD` hasarı!) doğrulayan 5 satırlık Python/shell kontrol adımı ekle — bozuk ikon/kaynak taşıyan APK asla yayınlanmasın.
- **Aynı `minSdk`/`targetSdk` Tutarlılığı:** `app/build.gradle.kts` içinde `targetSdk` güncel Android standardında (34/35) tutulsun *(eski `targetSdk` değerleri Android 14+ ve Play Protect'te "Bu uygulama Android'in eski bir sürümü için tasarlandı / riskli" uyarısını doğrudan tetikler!)*.

---

## 📁 ETKİLENECEK DOSYALAR HARİTASI

1. `app/build.gradle.kts` — Sabit Release keystore zorunluluğu (rastgele keystore yasağı), V1+V2+V3 imzalama, `isDebuggable = false`, `isMinifyEnabled = true`, `isShrinkResources = true`, güncel `targetSdk` ve güvenli `versionCode` kontrolü.
2. `.github/workflows/android-release.yml` — `testDebugUnitTest` kapısı, `res/` kaynak bütünlük kontrolü, `assembleRelease`, `apksigner verify --verbose --print-certs` + sertifika parmak izi (`EXPECTED_CERT_SHA256`) doğrulaması ve `versionCode`/SHA-256 özet raporu.
3. `app/src/main/res/xml/network_security_config.xml` *(YENİ)* — Global zorunlu HTTPS + yalnızca `localhost`/`127.0.0.1`/`10.0.2.2` yerel istisnası.
4. `app/src/main/AndroidManifest.xml` & `app/src/main/res/xml/file_paths.xml` — Riskli/gereksiz izin temizliği (`REQUEST_INSTALL_PACKAGES` yok!), `networkSecurityConfig` bağlantısı, açık `android:exported`, daraltılmış `file_paths.xml`.
5. `app/proguard-rules.pro` & `app/src/main/java/com/example/EmochiApplication.kt` — Release `Log.d`/`Log.v` temizliği ve debug-only ağ loglama.

---

## 🧪 ZORUNLU TEST PAKETİ (`ReleaseUpdateHygieneTest.kt`)

Aşağıdaki **6 birim testini** ekleyip mevcut tüm test dosyalarıyla birlikte yeşile geçir:
1. **Riskli İzin & Global Cleartext Yasağı Testi:** `AndroidManifest.xml` kaynak dosyasında `REQUEST_INSTALL_PACKAGES`, `QUERY_ALL_PACKAGES`, `SYSTEM_ALERT_WINDOW` ve `usesCleartextTraffic="true"` ifadelerinin **bulunmadığını**; `android:networkSecurityConfig="@xml/network_security_config"` tanımının bulunduğunu doğrula.
2. **Ağ Güvenlik Yapılandırması Testi:** `res/xml/network_security_config.xml` dosyasının var olduğunu, `base-config` için `cleartextTrafficPermitted="false"` ayarlandığını ve yalnızca yerel loopback adreslerine istisna verildiğini doğrula.
3. **Sabit Keystore Kilidi Testi:** `app/build.gradle.kts` içinde Release derlemesi için rastgele `keytool -genkey` veya `debug` signing fallback'i bulunmadığını ve V1/V2/V3 imzalama ile `isDebuggable = false` ayarlarının korunduğunu doğrula.
4. **Daraltılmış `file_paths.xml` Testi:** `file_paths.xml` içinde `<root-path` veya tüm harici depolamayı açan güvensiz kural bulunmadığını doğrula.
5. **Uygulama İçi Güncelleyici Dokunulmazlık Kontrolü:** Proje içinde gereksiz yeni bir `AppUpdateManager` / `PackageInstaller` servisi eklenmediğini doğrula.
6. **Derleme & Tam Regresyon:** `./gradlew testDebugUnitTest` ve `./gradlew assembleDebug` sıfır hata ve sıfır yeni uyarıyla geçer.

### ✅ Teslimat Öncesi Doğrulama Kontrol Listesi
1. **[ ] Uygulama içi güncelleme butonuna dokunulmadı mı?** (`UpdateCheckerModal` olduğu gibi bırakıldı, `REQUEST_INSTALL_PACKAGES` eklenmedi.)
2. **[ ] Sabit İmza & Çakışmasız "Güncelle" Kilitlendi mi?** Release derlemesi yalnızca GitHub Secrets'taki sabit keystore ile V1+V2+V3 imzalanıyor, eksikse hata verip duruyor ve `apksigner verify` + sertifika parmak izi doğrulanıyor mu?
3. **[ ] Play Protect / OEM Risk Hijyeni Tam mı?** `isDebuggable = false`, global `usesCleartextTraffic="true"` kaldırılıp `network_security_config.xml` bağlandı, gereksiz izinler temizlendi, `targetSdk` güncel ve Release logları temiz mi?
4. **[ ] Tek Seferlik Hatırlatma:** Telefonda hâlâ eski rastgele `debug.keystore` ile imzalı sürüm kuruluysa yalnızca **1 defaya mahsus** yedek alıp eskiyi kaldırarak yeni sabit Release APK'yı kur; ondan sonraki tüm yeni sürümler üstüne dokunduğunda doğrudan **"Güncelle"** deyip veri silmeden kurulacaktır.
5. **[ ] Tüm Testler & Derleme Yeşil mi?**