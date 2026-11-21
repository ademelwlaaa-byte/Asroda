# ⚡ EMOCHI — 10/10 GÖRSEL/COIL OPTİMİZASYONU, ROMAN AI PROMPT MİMARİSİ VE ÇOKLU API FALLBACK ŞARTNAMESİ

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Görevin, Emochi Android uygulamasındaki şu **3 temel motoru** sıfır kalite kaybı, sıfır ANR/OOM riski ve maksimum kararlılıkla **10/10** seviyesine çıkarmaktır:
> 1. **Görsel, Micro-WebP Avatar & Coil Bellek Optimizasyonu** *(Sıfır görünür kalite kaybı + minimum depolama/RAM garantisiyle)*
> 2. **Emochi Roman AI Prompt Mimarisi** *(Ayarlardan açılıp kapatılabilen Sansür / Vaaz Korumasıyla)*
> 3. **Çoklu API Kataloğu, Öncelik & Fallback Motoru**
>
> **Kırmızı Çizgiler:**
> - Kısaltma (`// ...`), yarım metod veya UI'da açılıp arkada çalışmayan sahte ayar **KESİNLİKLE YASAKTIR**.
> - **Domino Etkisi Koruması:** Kozmik-gotik Mor Cam UI tasarım dilini (`#090414`, buzlu cam, neon yansımalar, Serif tipografi, 600ms Crossfade, Aiden Stories yüzen ön izleme penceresi, ChatScreen son 5 mesaj ve kaydırma kuralları) **asla bozma**.
> - `precompiled_missing.jar` içindeki sınıflarla ikili uyumluluğu (binary compatibility) koru; Room şemasını kırmadan (`UserSettingsEntity` yerine `AppFeatureFlags` ile) yeni ayar anahtarlarını güvenle sakla.

---

## 🖼️ BÖLÜM 1: GÖRSEL, MICRO-WEBP AVATAR & COIL OPTİMİZASYONU (10/10)

### 🛡️ Değişmez Temel Amaç (Sıfır Kalite Kaybıyla Minimum Boyut Garantisi)
Aşağıdaki mevcut kazanımlar **kesinlikle korunacak ve daha da iyileştirilecektir**:
* **Kullanıcı Bot Avatarları (`AvatarOptimizer.kt`):** Galeriden seçilen 4–10 MB'lık fotoğraflar `inJustDecodeBounds` + `inSampleSize` (OOM koruması) ve `ExifInterface` yön düzeltmesiyle `ARGB_8888` olarak okunur, merkezden `256×256 px` kare kırpılır, `WEBP_LOSSY (quality = 82)` ile sıkıştırılarak **~12–18 KB** boyutunda kristal netliğinde `data:image/webp;base64,...` verisine dönüştürülür ve ara bitmap'ler anında `recycle()` edilir. **Bu pipeline'ın boyut/kalite dengesi ASLA bozulmayacaktır.**
* **Menü & Arka Plan Görselleri:** Uygulama içi görseller `drawable-nodpi` altında optimize `.webp` olarak tutulur; yıldızlar, yörünge halkaları, ışık yansımaları ve nebula efektleri `0 KB` Jetpack Compose `Canvas` ve `Brush` kodlarıyla çizilir. Coil önbelleği `%18 RAM` ve `35 MB disk` sınırında kalır, hiçbir yerde `Size.ORIGINAL` kullanılmaz.

### 🔍 Kod Tabanında Tespit Edilen Gerçek Darboğazlar
1. **`BotAvatarImage.kt` `ByteBuffer` Pozisyon Hatası & Renk Banding Riski:**
   - `remember(avatarUrl)` içinde saklanan `ByteBuffer`, Coil tarafından tekrar okunmaya kalkıldığında `position == limit` nedeniyle boş dönebilir.
   - Ayrıca 256×256 küçük avatarlarda `allowRgb565(true)` kullanılması, koyu mor/gotik ton geçişlerinde hafif renk şeritlenmesine (banding) yol açabilir; oysa `RGB_565` asıl RAM tasarrufunu **tam ekran büyük arka planlarda** sağlar.
2. **Ana İş Parçacığında (Main Thread) Tekrarlayan Ağır Blur İşlemleri:** `ChatScreen.kt`, `BotSettingsModal.kt`, `ExploreTabContent.kt` ve `ApiHubSheets.kt` içindeki 4 kopya `createFrosted...Bitmap()` fonksiyonu UI thread'de senkron çalışıyor ve `onTrimMemory` anında temizlenmiyor.
3. **Sahipsiz Duvar Kâğıdı Dosyaları:** `AvatarOptimizer.processBackgroundToWebPFile()` ile `filesDir/bot_images/` içine yazılan `bg_*.webp` dosyaları, bot silindiğinde veya arka plan değiştirildiğinde diskten silinmiyor.
4. **Özel Arka Planlarda `RGB_565` Eksikliği:** `ChatScreen.kt` ve `BotSettingsModal.kt` içinde `bot.chatBgUrl` yükleyen `AsyncImage` çağrıları açık `ImageRequest` + `allowRgb565(true)` kullanmıyor.

### 🛠️ 10/10 Uygulama Kuralları
1. **`BotAvatarImage.kt` — Sıfır Hata + Maksimum Görsel Netlik:**
   - `remember(avatarUrl)` içinde `ByteBuffer` yerine değişmez `ByteArray` sakla (`Base64.decode(base64Part, Base64.NO_WRAP)`).
   - `ImageRequest.Builder(context)` içine her istekte taze `ByteBuffer.wrap(bytes)` (veya doğrudan `bytes`) ver, `memoryCacheKey` kullan ve **`allowRgb565(false)`** ayarla (256×256 avatar zaten ~15 KB disk / çok düşük RAM tutar; 32-bit `ARGB_8888` ile cam gibi pürüzsüz renk geçişi korunur!).
   - **`allowRgb565(true)` kuralını yalnızca tam ekran opak arka planlarda** (`AppBackground`, `RpAtmosphericBackground`, özel `bot.chatBgUrl` duvar kâğıtları ve büyük kapaklarda) uygula — böylece hem tam ekranlarda %50 RAM tasarrufu hem avatarlarda %100 renk kalitesi birlikte elde edilir.
2. **Tek Merkezli, Arka Plan İş Parçacıklı `FrostedBackdropCache.kt`:**
   - 4 ayrı ekrandaki kopya blur fonksiyonlarını tek bir `object FrostedBackdropCache` altında topla (`LruCache<String, ImageBitmap>` maks 4 kayıt).
   - Compose tarafında senkron `remember { ... }` yerine `produceState<ImageBitmap?>(initialValue = FrostedBackdropCache.get(key))` + `withContext(Dispatchers.Default)` kullan. Hazır olana kadar `0 KB` `#090414 -> #1A0B2E` Compose `Brush` göster (UI thread'de **0 ms kasılma**).
   - `EmochiApplication.onTrimMemory(level)` içinde düşük bellek sinyallerinde `FrostedBackdropCache.evictAll()` ve Coil `imageLoader.memoryCache?.clear()` çağır.
3. **Disk Hijyeni (`ImageStorageManager.kt` & `EmochiRepository.kt`):**
   - `deleteLocalBackgroundIfOwned(context, uriStr)` ve `pruneOrphanBackgrounds(context, activeUrls)` ekle.
   - Bot silindiğinde (`deleteBot`) veya duvar kâğıdı değiştirildiğinde eski `bot_images/bg_*.webp` dosyasını anında diskten sil.
   - `ImageStorageManager` içindeki boş `uploadToCloudStorage()` stub'ını kaldır; tüm avatar/arka plan WebP sıkıştırmasını tek kaynaktan `AvatarOptimizer` üzerinden yürüt.
4. **Özel Arka Plan Standardı:**
   - `ChatScreen.kt` ve `BotSettingsModal.kt` içindeki tüm `bot.chatBgUrl` `AsyncImage` çağrılarını `ImageRequest.Builder(context).data(url).allowRgb565(true).crossfade(250).build()` şeklinde standardize et (`Size.ORIGINAL` kesinlikle yasak).

---

## 🧠 BÖLÜM 2: EMOCHI ROMAN AI PROMPT MİMARİSİ (10/10)

### 🔍 Kod Tabanında Tespit Edilen Gerçek Eksikler
1. **Ayarlardaki 7 RP Tonu Anahtarı Promptta Hiç Okunmuyor:** Kullanıcı `GlobalSettingsModal` içinden `enableFlirty`, `enableHardcore`, `enableFetish`, `enableDarkRp`, `enableSweet`, `enablePrimal` ve `enableNsfw` anahtarlarını ayarlıyor ama `EmochiRepository.buildSystemPrompt()` bunları modele aktarmıyor.
2. **Duygu & Zaman Geri Bildirim Döngüsü Kopuk:** `buildSystemPrompt()` içinde `TimePerceptionEngine.buildStoryTimePromptBlock()` çağrılmıyor ve yanıt sonuna gizli `[[STATE_JSON ...]]` bloğu istenmiyor.
3. **`retryWithSoftenedPrompt()` Gerçekten Yeniden Çerçevelemiyor:** Aynı promptu olduğu gibi tekrar gönderiyor.

### 🛠️ 10/10 Uygulama Kuralları
1. **Modüler `SystemPromptBuilder.kt` Sınıfını Çıkar:**
   - Prompt oluşturmayı `EmochiRepository.kt`'den ayırıp test edilebilir `SystemPromptBuilder` sınıfına taşı (`EmochiRepository.buildSystemPrompt()` ona delege etsin).
   - **5 Katmanlı Token Bütçesi Uygula** (32K ve altı modellerde otomatik kompakt tavan):
     * **P1 (Dokunulmaz Çekirdek):** Başta ve sonda `AI_ONLY_FICTIONAL_CONTEXT_NOTICE`, Karakter Kişiliği, Senaryo, Sabitlenmiş Hafıza, Kullanıcı Karakteri, **Ahlak Bekçiliği / Vaaz Yasağı**, **Suç Ortaklığı & Dramatik Gerilim**, **Emochi Anlatım Formatı** (`2–4 paragraf`, `*eylem/iç ses*`, `"diyalog"`, **No Godmodding**).
     * **P2 (Canlı Sahne Durumu):** Güncel `EmotionState`, `WorldState`, `TimePerceptionEngine.buildStoryTimePromptBlock(...)` (hikâye takvimi), aktif günün rutini ve varsa `pendingConsistencyNote` / `pendingDecayNote`.
     * **P3 (Kullanıcı Ton & Stil Tercihleri):** `writingStyle`, `customLength`/`responseLength`, `intensity`, `enableOoc` VE **`UserSettingsEntity` aktif RP tonları** (`enableFlirty`, `enableHardcore`, `enableDarkRp`, `enableSweet`, `enablePrimal`, `enableFetish` — `bot.isNsfw && settings.enableNsfw` açıkken ilgili alt ton direktiflerini 1'er keskin cümleyle ekle).
     * **P4 (Seçilmiş Hafıza & Aktif Kadro):** Ortak Hibrit + AI RAG'dan gelen `[#ID]` Neural Vault olguları, Multi-POV Yaşanan Anılar (`[[INNER]]`), son 2 Checkpoint ve `CastDetectorEngine.filterActiveInSceneCastMembers()` ile süzülmüş aktif kadro.
     * **P5 (Görünmez Durum Sözleşmesi):** Yanıtın son satırına kullanıcıya bahsedilmeden eklenecek tek satırlık kompakt `[[STATE_JSON ...]]` talimatı.

2. **Ayarlanabilir Ahlak Dersi & Sansür Koruması (`RefusalDetector` + Kullanıcı Anahtarı):**
   - `AppFeatureFlags.kt` içine yeni bir kalıcı özellik anahtarı ekle (Room şemasını bozmadan `SharedPreferences` + `StateFlow<Boolean>`):
     ```kotlin
     private const val KEY_REFUSAL_AUTO_FALLBACK = "refusal_auto_fallback"
     // Varsayılan: true
     // StateFlow: refusalAutoFallback + getter: isRefusalAutoFallbackEnabled + setter: setRefusalAutoFallback(Boolean)
     ```
   - **UI Kontrolü (`GlobalSettingsModal.kt` -> API Sekmesi):** Mevcut `"Otomatik Sıradakine Geçiş (Fallback)"` anahtarının hemen altına aynı Mor Cam `GlobalMorCamSwitch` bileşeniyle ekle:
     * **Başlık:** `"Sansür / Ahlak Vaazı Koruması"`
     * **Alt Metin:** `"Açıkken bir model karakterden çıkıp vaaz veya ret verirse yanıtı göstermeden sıradaki özgür API'ye geçer."`
     * Ayrıca `FeatureFlagsPanel.kt` listesinde de aynı anahtar senkron görünsün.
   - **Çalışma Mantığı (`RefusalDetector`):**
     * **Yanlış Pozitif Koruması (Kritik):** Karakterin roman içi diyalogda (`"..."` içinde) rol gereği söylediği normal cümleleri değil, gerçek **Out-of-Character yapay zeka asistan reddi/vaazı** kalıplarını yakala (ör. `"bir yapay zeka olarak"`, `"as an ai"`, `"etik kurallarım"`, `"bu içeriği üretemem"`, `"bu konuda yardımcı olamam"`, `"polise/yetkililere başvurmalısın"`, `"sağlıklı iletişim kurmalısın"` + kısa/anlatısız asistan yanıt yapısı).
     * **`isRefusalAutoFallbackEnabled == true` ise:** Bu yanıtı sohbete yazmadan otomatik olarak zincirdeki bir sonraki sağlayıcıya (`ProviderFallbackManager`) devret ve `EmptyResponseLogEntity` / fallback loguna `"OOC_REFUSAL_FALLBACK"` olarak kaydet.
     * **`isRefusalAutoFallbackEnabled == false` ise:** Otomatik geçiş yapma; seçili modelin yanıtını olduğu gibi göster (kullanıcı isterse banner'daki `retryWithSoftenedPrompt` veya Yeniden Oluştur ile elle tekrar deneyebilir).
   - **Gerçek `retryWithSoftenedPrompt()`:** Tetiklendiğinde isteği `literaryFramingMode = true` ile gönderip edebi roman kurgusu çerçevesini güçlendirsin.

3. **Sıfır Etiket Sızıntısı (`cleanEmotionTags`):**
   - Hem tam hem de yarım kesilmiş `[[STATE_JSON`, `[CHARACTER_EMOTION`, `[ACTIVE_MEMORY_CALL` ve `<think>...</think>` bloklarını kullanıcıya görünmeden %100 temizle.

---

## 🌐 BÖLÜM 3: ÇOKLU API KATALOĞU, ÖNCELİK & FALLBACK MOTORU (10/10)

### 🔍 Kod Tabanında Tespit Edilen Kritik Hatalar
1. **`sanitizeModelName()` Sessiz Model Ezme Hatası:** `EmochiRepository.kt` içindeki kopya private `sanitizeModelName()`, kullanıcının katalogdan seçtiği geçerli modelleri (`gemini-2.5-flash`, `gemini-2.5-pro`, `llama-3.3-70b-versatile`, `llama-3.1-8b-instant`, `qwen/qwen3-32b`) sessizce başka modellere değiştiriyor.
2. **Global Sıcaklık Interceptor'ı (`EmochiApplication.kt`):** Her POST isteğine koşulsuz `temperature = 0.90` bastığı için JSON analiz/RAG/özetleme isteklerini bozuyor.
3. **Devre Kesici (Circuit Breaker) Eksikliği:** `401` (hatalı anahtar) veya `429` (kota dolu) veren sağlayıcı her turda tekrar denenip gecikme yaratıyor.
4. **Dağınık Sağlayıcı Seçimi & Eksik Custom Utility Model UI'ı:** Yardımcı çağrılar `ProviderFallbackManager` yerine kendi ayrı kontrollerini yapıyor; `CustomProviderEntity.utilityModel` alanı UI'da ayarlanamıyor.

### 🛠️ 10/10 Uygulama Kuralları
1. **Model Ezme Hatasını Kaldır:**
   - `EmochiRepository` içindeki private kopya `sanitizeModelName()` metodunu sil; tek kaynak olarak `companion object` sürümünü koru.
   - Geçerli hiçbir katalog modelini (`gemini-2.5-*`, `llama-3.3-*`, `llama-3.1-*`, `qwen3-*`) başka modele çevirme; yalnızca gerçekten kapatılmış eski ID'leri (`llama3-70b-8192`, `llama3-8b-8192`, `decommissioned`) güvenli varsayılana yönlendir. `getOrCreateSettings()` içindeki `gemini-2.5` düşürme bloğunu kaldır.
2. **Header Kontrollü Akıllı Interceptor (`EmochiApplication.kt`):**
   - Dahili `X-Emochi-Call-Type` (`"narrative"` | `"utility"`) header'ını okuyup istek dışarı çıkmadan önce `removeHeader("X-Emochi-Call-Type")` ile sil:
     * `"narrative"`: `temperature = 0.90`, `topP/top_p = 0.95` (Anthropic'te yalnızca `temperature`).
     * `"utility"`: `temperature = 0.15`, `topP/top_p = 0.80`.
   - OpenRouter URL'lerine `HTTP-Referer: https://emochi.app` ve `X-Title: Emochi` ekle; deprecated `RequestBody.create` yerine `toRequestBody(...)` kullan.
3. **Merkezi Devre Kesici (`ProviderHealthTracker.kt`):**
   - Tüm sağlayıcılar için thread-safe `ConcurrentHashMap<String, ProviderHealthState>` tut:
     * `401 / 403` (`AUTH_ERROR`): 15 dk cooldown (anahtar güncellenince veya test başarılı olunca `resetHealth(key)` ile anında açılır).
     * `429` (`RATE_LIMIT`): HTTP `Retry-After` varsa ona uy, yoksa kademeli bekleme (`60 sn -> 180 sn -> 300 sn`).
     * `5xx / Timeout`: İlk hatada sıradakine geç; 2 ardışık hatada `45 sn` cooldown.
     * `CONTENT_REFUSAL / EMPTY_RESPONSE`: Uzun kilitleme yapma, yalnızca o turda sıradaki sağlayıcıya devret (eğer `isRefusalAutoFallbackEnabled == true` ise).
     * **Acil Durum Kuralı:** Zincirdeki tüm sağlayıcılar cooldown'daysa en erken açılacak sağlayıcıyı son çare olarak dene.
4. **Tek Merkezli `ProviderFallbackManager` (`ExecutionMode.NARRATIVE` vs `ExecutionMode.UTILITY`):**
   - `UTILITY` modunda her sağlayıcı için otomatik `resolveUtilityModel()` + `X-Emochi-Call-Type: utility` uygula.
   - `runBaselineAnalysis`, `callLlmForAudit`, `extractAndCommitTurnMemories` ve `BackgroundEmotionAuditWorker` tamamı `ProviderFallbackManager` üzerinden yürüsün (kullanıcının #1, #2, #3 önceliği ve `Öncelik: Free API'siz` ayarı her yerde birebir geçerli olsun).
5. **Custom Provider Yardımcı Model Seçici & Canlı Sağlık UI'ı (`ApiHubSheets.kt`, `GlobalSettingsModal.kt`, `Modals.kt`):**
   - `CustomApiEditorDialog` içinde "Sunucudan Modelleri Çek" çalışınca `detectSmallModelCandidates()` ile küçük modelleri `detectedSmallModels`'a yaz ve opsiyonel **"Yardımcı (Hızlı/Ucuz) Model"** alanı sun.
   - `ActiveApiPriorityRow` kartlarında anlık sağlık rozeti göster (`● Hazır (380 ms)` / `⏳ Kota Bekliyor` / `⚠ Anahtar Hatası`).
   - `FallbackLogModal` içine mevcut `ProviderReliabilityDashboardUI` bileşenini sekme olarak bağla (ölü kod kalmasın).

---

## 📁 BÖLÜM 4: ETKİLENECEK DOSYALAR HARİTASI

1. `app/src/main/java/com/example/EmochiApplication.kt` — `onTrimMemory`, `X-Emochi-Call-Type` interceptor, OpenRouter header'ları.
2. `app/src/main/java/com/example/util/AppFeatureFlags.kt` — `KEY_REFUSAL_AUTO_FALLBACK` (`refusalAutoFallback` StateFlow + getter/setter).
3. `app/src/main/java/com/example/ui/components/BotAvatarImage.kt` — `ByteArray` tabanlı pozisyon-güvenli Base64 yükleme + `allowRgb565(false)` ile sıfır renk kaybı.
4. `app/src/main/java/com/example/util/FrostedBackdropCache.kt` *(YENİ)* — `Dispatchers.Default` asenkron blur + `LruCache` + `onTrimMemory` temizliği.
5. `app/src/main/java/com/example/util/ImageStorageManager.kt` — Sahipsiz `bot_images/bg_*.webp` temizliği, ölü stub kaldırma.
6. `app/src/main/java/com/example/data/repository/SystemPromptBuilder.kt` *(YENİ)* — 5 katmanlı token bütçeli Roman AI prompt motoru, 7 RP tonu, hikâye takvimi, `[[STATE_JSON]]` ve `literaryFramingMode`.
7. `app/src/main/java/com/example/data/managers/ProviderHealthTracker.kt` *(YENİ)* — Hata sınıflandırma & circuit breaker + `RefusalDetector`.
8. `app/src/main/java/com/example/data/managers/ProviderFallbackManager.kt` — `ExecutionMode.NARRATIVE/UTILITY`, `ProviderHealthTracker`, `isRefusalAutoFallbackEnabled` kontrollü OOC refusal fallback.
9. `app/src/main/java/com/example/data/repository/EmochiRepository.kt` — Kopya `sanitizeModelName()` silme, `SystemPromptBuilder` delegasyonu, bot/bg silmede dosya temizliği.
10. `app/src/main/java/com/example/ui/viewmodel/EmochiViewModel.kt` — Gerçek `retryWithSoftenedPrompt()`, Custom Provider `utilityModel` & `detectedSmallModels` kaydı.
11. `app/src/main/java/com/example/ui/screens/ChatScreen.kt`, `ExploreTabContent.kt`, `BotSettingsModal.kt`, `ApiHubSheets.kt`, `GlobalSettingsModal.kt`, `Modals.kt`, `FeatureFlagsPanel.kt` — `FrostedBackdropCache`, `RGB_565` arka plan isteği, `"Sansür / Ahlak Vaazı Koruması"` switch'i, Custom utility model alanı, canlı sağlık rozetleri ve güvenilirlik panosu.

---

## 🧪 BÖLÜM 5: ZORUNLU TEST PAKETİ VE 10/10 DOĞRULAMA LİSTESİ

Aşağıdaki **8 birim testini** ekleyip tamamının geçtiğini doğrula:
1. **Micro-WebP Avatar Kalite & Boyut Koruma Testi:** `AvatarOptimizer.processAvatarToMicroWebPBase64` çıktısının `256×256`, `data:image/webp;base64,` formatında (~12–25 KB) kaldığını ve `BotAvatarImage` tarafında aynı Base64 verisinin iki kez üst üste `ByteBuffer` pozisyon hatası olmadan eksiksiz çözüldüğünü doğrula.
2. **Orphan Background Temizlik Testi:** Bot silindiğinde veya duvar kâğıdı değiştirildiğinde eski `bot_images/bg_*.webp` dosyasının diskten silindiğini doğrula.
3. **Model Ezmeme Testi:** `sanitizeModelName("gemini-2.5-flash")`, `sanitizeModelName("llama-3.3-70b-versatile")` ve `sanitizeModelName("qwen/qwen3-32b")` değerlerinin değişmediğini doğrula.
4. **Interceptor Sıcaklık & Header Temizlik Testi:** `X-Emochi-Call-Type: utility` -> `0.15`, `narrative` -> `0.90` ve dahili header'ın dışarı sızmadığını mock OkHttp ile kanıtla.
5. **Circuit Breaker & Öncelik Testi:** #1 sağlayıcı `429` veya `401` verdiğinde #2'ye geçildiğini ve sonraki turda #1'e istek atılmadan doğrudan #2'nin çağrıldığını kanıtla.
6. **Ayarlanabilir Sansür / Vaaz Koruması Testi (Açık vs Kapalı):**
   - `isRefusalAutoFallbackEnabled = true` iken #1 sağlayıcı `"Bir yapay zeka olarak bu konuda yardımcı olamam"` döndürdüğünde otomatik #2 sağlayıcıya geçildiğini kanıtla.
   - `isRefusalAutoFallbackEnabled = false` iken #2 sağlayıcıya geçilmediğini ve #1'in yanıtının döndüğünü kanıtla.
   - Karakterin roman içi diyalogda (`"Bu doğru değil ama yine de seninle geliyorum."`) konuştuğu cümlenin **yanlış pozitif olarak engellenmediğini** kanıtla.
7. **RP Ton & Zaman Prompt Testi:** `SystemPromptBuilder`'ın `enableDarkRp`, `enableFlirty`, `TimePerceptionEngine.buildStoryTimePromptBlock()` ve `[[STATE_JSON]]` bloğunu doğru enjekte ettiğini doğrula.
8. **Derleme & Regresyon Doğrulaması:** `./gradlew testDebugUnitTest` ve `./gradlew assembleDebug` sıfır hata ve sıfır yeni uyarıyla geçer.

### ✅ Teslimat Öncesi 10/10 Kontrol Listesi
1. **[ ] Micro-WebP (~15 KB) & Sıfır Kalite Kaybı Korundu mu?** `AvatarOptimizer` 256×256 WEBP_LOSSY (%82) bozulmadı; küçük avatarlarda `allowRgb565(false)` ile renk banding'i önlenirken tam ekran arka planlarda `allowRgb565(true)` ile %50 RAM tasarrufu sağlandı mı?
2. **[ ] UI Thread Sıfır Bloklanma & Disk Hijyeni Tam mı?** `FrostedBackdropCache` arka planda çalışıyor, `onTrimMemory` temizliği ve sahipsiz duvar kâğıdı silme aktif mi?
3. **[ ] Sansür / Vaaz Koruması Ayardan Açılıp Kapanabiliyor mu?** `GlobalSettingsModal` -> API sekmesindeki `"Sansür / Ahlak Vaazı Koruması"` anahtarı `AppFeatureFlags` ile kalıcı saklanıyor ve yanlış pozitif üretmeden çalışıyor mu?
4. **[ ] Tüm RP Tonları, Hikâye Takvimi & `retryWithSoftenedPrompt` Canlı mı?**
5. **[ ] Model Seçimi Ezilmiyor, Sıcaklık (`0.90`/`0.15`) Ayrışıyor ve Circuit Breaker Çalışıyor mu?**
6. **[ ] Tüm Testler & Derleme Yeşil mi?**