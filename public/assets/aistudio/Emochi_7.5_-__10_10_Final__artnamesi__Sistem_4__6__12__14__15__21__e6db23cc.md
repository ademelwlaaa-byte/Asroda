# 🏆 EMOCHI (ASRODA) — 7.5 → 10/10 FİNAL MÜKEMMELLEŞTİRME ŞARTNAMESİ
**Kapsanan 6 Sistem:** `#4 Görsel, Logo & Coil Optimizasyonu` · `#6 Aiden Stories & Şablonlar Kataloğu` · `#12 Arka Plan İşçileri` · `#14 Canlı Duygu Motoru V3 & Yan Karakterler` · `#15 Hikâye Takvimi & Zaman Algısı` · `#21 Rutin Takibi & Bildirimleri`

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Bu 6 sistem `Asroda-main` kod tabanında şu an **7.5 / 10** seviyesindedir. Görevin, özellikle telefonda varsayılan Android robotunun çıkmasına yol açan **12 bozuk ikon PNG'sini** kökünden çözüp sıfır kalite kaybıyla boyutunu küçültmek ve tüm medya, arka plan işçisi, canlı duygu, yan karakter, hikâye takvimi ve rutin döngülerini uçtan uca bağlayarak bu 6 sistemin tamamını **kusursuz 10/10** üretim kalitesine çıkarmaktır. *(Not: 7.5 altındaki `#20`, `#22`, `#23`, `#24` ve bulut Keşfet backend'i bu paketin kapsamı dışındadır — onlara dokunup kapsamı dağıtma.)*
>
> **Kırmızı Çizgiler:**
> 1. **Kısaltma & Regresyon Yasağı:** `// ...`, yarım metod, ölü sınıf veya mevcut 10 test dosyasını kıran değişiklik **KESİNLİKLE YASAKTIR**.
> 2. **Kullanılmayan WebP Varlıklarına Dokunma Yasağı:** `app/src/main/res/drawable-nodpi/` altındaki **hiçbir `.webp` dosyası silinmeyecektir!** Kullanılmayanlar dahil mevcut 96 WebP varlığının tamamı olduğu gibi korunacaktır.
> 3. **Domino Etkisi Koruması:** Mor Cam kozmik-gotik UI tasarım dili (`#090414`, buzlu cam, `DstIn` üst/alt gradyan maskesi, üst çerçeve `✦` yıldızı, alt ışık yansıması, Serif tipografi, 600ms Crossfade, Aiden Stories yüzen ön izleme penceresi), `AvatarOptimizer` Micro-WebP (`256×256`, `WEBP_LOSSY %82`, ~15 KB), `BotAvatarImage` (`allowRgb565(false)`), Coil limitleri (`%18 RAM / 35 MB disk`, `Size.ORIGINAL` yasağı) ve `ChatScreen` kuralları **asla bozulmayacaktır**.
> 4. **API 24+ Uyumluluğu:** `minSdk = 24` olduğu için takvim/gün/yaş hesaplarında yalnızca `java.util.Calendar` + `SimpleDateFormat` kullan; `java.time.LocalDate` kesinlikle kullanma.
> 5. **Hikâye Zamanı Kuralı:** Karakter duygularının zamanla sönümlenmesi, yaş/doğum günü ilerlemesi ve rutin hatırlatmaları gerçek duvar saatine göre değil, yalnızca `bot.storyDayCounter` ve `bot.storyCalendarDate` (**hikâye takvimi**) üzerinden çalışır.
> 6. **Sıfır Ekstra API Maliyeti:** Duygu, yan karakter, zaman atlama ve rutin döngüleri sohbet başına **0 ekstra LLM çağrısı** üretmelidir; mevcut `[[STATE_JSON]]` sözleşmesi üzerinden çalışacaktır.

---

## 🖼️ SİSTEM #4: GÖRSEL, LOGO ONARIMI & SIFIR KALİTE KAYIPLI BOYUT OPTİMİZASYONU (7.5 → 10/10)

### 🔍 Kod Tabanında Tespit Edilen Kritik Hatalar
1. **12 Bozuk İkon PNG Dosyası (Android Robotu Bug'ının %100 Kaynağı):**
   - `app/src/main/res/drawable/ic_app_logo.png`, `app/src/main/res/drawable/ic_vai_logo.png` ve `app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}-v4/ic_launcher*.png` (10 dosya) — toplam **12 PNG dosyası** UTF-8 (`EF BF BD`) dönüşüm hasarı nedeniyle bozuk ve CRC doğrulamasından geçemiyor!
   - `app/build.gradle.kts` içinde `isCrunchPngs = false` olduğu için Gradle sessizce bu bozuk PNG'leri APK'ya paketliyor; Android launcher ve bildirim yöneticisi decode edemediği için **mor kareli varsayılan Android robotuna** düşüyor.
   - **Sağlam Orijinaller Hazır:** Aynı dosyaların %100 sağlam orijinalleri proje kökündeki yanlış konumlandırılmış **`res/`** klasöründe (`res/drawable/ic_app_logo.png`, `res/drawable/ic_launcher_foreground_asset.png`, `res/drawable/ic_vai_logo.png` ve `res/mipmap-*-v4/ic_launcher*.png`) duruyor.
2. **Bozuk Adaptive Icon & Aşırı Büyük Logo Dosyaları:**
   - `app/src/main/res/drawable/ic_launcher_foreground.xml` içinde `<layer-list><bitmap android:src="@drawable/ic_app_logo" />` kullanılıyor; oysa sağlam `ic_launcher_foreground_asset.png` (432×432, güvenli alanlı) hazır.
   - `ic_launcher_background.xml` rengi `#050B21` (lacivert) olarak kalmış; Android 13+ temalı ikonlar için `<monochrome>` katmanı yok.
   - Sağlam orijinal PNG'ler toplam **~2.7 MB** yer kaplıyor (`ic_vai_logo.png` 1.41 MB, `ic_app_logo.png` 869 KB, `ic_launcher_foreground_asset.png` 187 KB, 10 mipmap PNG ~170 KB). `minSdk = 24` olduğu için Android launcher ve drawable katmanları **şeffaf WebP (Lossless / Q95)** formatını %100 yerel olarak destekler; görsel netlikten tek piksel bile ödün vermeden bu 2.7 MB'lık yük **~200–350 KB** seviyesine indirilebilir!

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **12 Bozuk İkonu Kökten Onar & Sıfır Kalite Kaybıyla WebP'ye Dönüştür:**
   - Kaynak olarak **yalnızca** proje kökündeki sağlam `Asroda-main/res/` içindeki orijinal PNG dosyalarını kullan (bozuk `app/src/main/res/` PNG'lerini tamamen sil, kaynak çakışması — duplicate resource — bırakma!).
   - **10 Mipmap Launcher İkonu (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`):**
     * Kök `res/mipmap-*-v4/ic_launcher.png` ve `ic_launcher_round.png` (10 dosya) görsellerini **aynı çözünürlüklerini ve alfa şeffaflıklarını koruyarak** yüksek verimli **WebP (`cwebp -lossless -z 9` veya görsel olarak ayırt edilemez `Q=95 -m 6 -alpha_q 100`)** formatına dönüştürüp `app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.webp` ve `ic_launcher_round.webp` olarak kaydet; eski bozuk `.png` dosyalarını (ve boşalan `-v4` çakışmalarını) sil.
     * Varsa eski/çakışan `drawable-v24/ic_launcher*` veya `mipmap-anydpi-v33/` artıklarını temizle.
   - **Adaptive Ön Plan & Uygulama İçi Logolar (Sıfır Kalite Kaybı + %85+ Boyut Kazancı):**
     * Kök `res/drawable/ic_launcher_foreground_asset.png` (432×432, 187 KB) -> **`app/src/main/res/drawable-nodpi/ic_launcher_foreground_asset.webp`** (`432×432`, WebP Q=95 / lossless alpha, hedef `<45 KB`).
     * Kök `res/drawable/ic_app_logo.png` (1024×1024, 869 KB) -> **`app/src/main/res/drawable-nodpi/ic_app_logo.webp`** (`512×512` yüksek kaliteli Lanczos ölçekleme + WebP Q=94 / alpha Q=100, hedef `<55 KB`, eski bozuk `drawable/ic_app_logo.png` dosyasını sil — tüm `R.drawable.ic_app_logo` referansları birebir çalışır!).
     * Kök `res/drawable/ic_vai_logo.png` (1024×1024, 1.41 MB) -> Eğer uygulama içi arayüzde renkli logo olarak referans ediliyorsa **`app/src/main/res/drawable-nodpi/ic_vai_logo.webp`** (`512×512` Lanczos + WebP Q=94 / alpha Q=100, hedef `<65 KB`, eski bozuk `drawable/ic_vai_logo.png` silinsin).
     * **Bildirim Küçük İkonu (`ic_notification_emochi.xml` — YENİ):** Android bildirim çubuğu yalnızca tek renk beyaz şeffaf silüet beklediği için (renkli PNG/WebP beyaz kare görünür!), `app/src/main/res/drawable/ic_notification_emochi.xml` adında 24×24 dp, şeffaf zeminli saf beyaz (`#FFFFFF`) kozmik yıldız / VAI amblemli vektör drawable (`~1 KB`) oluştur ve tüm bildirim işçilerinde (`RoutineNotificationWorker`, `ProactiveMessageWorker`) bunu kullan.
   - **Adaptive Icon XML Temizliği:**
     * `app/src/main/res/drawable/ic_launcher_background.xml`: Arka plan rengini Emochi'nin kozmik-gotik tabanı **`#090414`** yap.
     * `app/src/main/res/drawable/ic_launcher_monochrome.xml` *(YENİ)*: Android 13+ temalı ikonlar için temiz 108×108 dp tek renk vektör katmanı.
     * `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` & `ic_launcher_round.xml`: Bozuk `<layer-list><bitmap>` sarmalamasını (`drawable/ic_launcher_foreground.xml`) tamamen kaldır; doğrudan:
       - `<background android:drawable="@drawable/ic_launcher_background" />`
       - `<foreground android:drawable="@drawable/ic_launcher_foreground_asset" />`
       - `<monochrome android:drawable="@drawable/ic_launcher_monochrome" />` bağla.
   - Dönüşüm ve doğrulama tamamlanınca proje kökündeki yanlış **`res/`** klasörünü tamamen sil.
   - `AndroidManifest.xml` içinde `android:icon="@mipmap/ic_launcher"` ve `android:roundIcon="@mipmap/ic_launcher_round"` tanımlarının yalnızca `<application>` üzerinde tek merkezli olduğunu doğrula.
2. **Mevcut WebP Koruması & Micro-WebP Sözleşmesi:**
   - **KESİN KURAL:** `app/src/main/res/drawable-nodpi/` altındaki mevcut **96 `.webp` dosyasının HİÇBİRİNİ SİLME** (kullanılmayanlar dahil tamamı ileride kullanılmak üzere kalacaktır!).
   - `AvatarOptimizer` (`256×256`, `WEBP_LOSSY %82`, ~15 KB), `BotAvatarImage` (`ByteArray` + `allowRgb565(false)`), tam ekran arka planlarda `allowRgb565(true)` ve `FrostedBackdropCache` + `onTrimMemory` akışının eksiksiz kaldığını doğrula.

---

## 🌌 SİSTEM #6: AIDEN STORIES & ŞABLONLAR KATALOĞU (7.5 → 10/10)

### 🔍 Mevcut 2.5 Puanlık Boşluklar
1. **`AidenStoriesModal.kt` Merkezi Buzlu Camı Kullanmıyor:** Diğer 8 ekran `FrostedBackdropCache`'e geçti ancak `AidenStoriesModal.kt` kendi ağır arka plan/blur akışını koruyor; açılışta ana iş parçacığında gereksiz yük ve RAM kullanımı oluşuyor.
2. **Görsel İstek & Liste Performans Tutarsızlığı (`AidenStoriesModal.kt`, `ExploreTabContent.kt`):** Büyük kart kapaklarında açık `ImageRequest` (`allowRgb565(true)`, sabit hedef boyut, `crossfade`) standardı her yerde tutarlı değil; hızlı kaydırmada gereksiz recomposition ve görsel yükleme gecikmesi yaşanabiliyor.
3. **Şablondan Bot Oluşturma & Yerel Keşfet Cilası:** Bir Aiden hikâyesi veya şablon kartı seçildiğinde hızlı çift-tıklama ile mükerrer bot oluşma riski kapatılmalı; şablonun `BaselineEmotion` profili, hassas tetikleyicileri, hikâye başlangıç tarihi/günü ve rutinleri yeni bota eksiksiz ve temiz aktarılmalı. *(Not: Bulut Keşfet sunucusu şimdilik kapsam dışıdır — yerel Şablonlar + Yerel Keşfet deneyimi 10/10 pürüzsüz hâle getirilecektir.)*

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **`AidenStoriesModal.kt` → `FrostedBackdropCache` Entegrasyonu:**
   - `AidenStoriesModal.kt` içindeki tüm lokal/senkron blur veya ağır arka plan bitmap oluşturma kodunu kaldırıp **`FrostedBackdropCache`** + `produceState` (`Dispatchers.Default`) yapısına bağla (hazır olana kadar `0 KB` `#090414 -> #1A0B2E` Compose `Brush` — UI thread'de 0 ms takılma).
   - Aiden Stories yüzen ön izleme penceresinin ve Mor Cam kart estetiğinin görsel tasarımını **birebir koru**.
2. **Standartlaştırılmış Coil Kart Pipeline'ı (`AidenStoriesModal.kt`, `ExploreTabContent.kt`):**
   - Carousel, geniş kartlar (`img_aiden_wide_thumb*`), öne çıkan kapaklar ve şablon kartlarındaki tüm görselleri `ImageRequest.Builder(context).data(...).allowRgb565(true).crossfade(220).build()` üzerinden yükle (`Size.ORIGINAL` yasak).
   - Tüm `LazyRow` / `LazyColumn` / `LazyVerticalGrid` listelerinde kararlı `key = { it.id }` ve `contentType` tanımla; arama/kategori filtrelemesini `remember(query, selectedCategory, items)` ile türetilmiş (derived) hâlde hesapla.
3. **Atomik & İdempotent Şablon Başlatma (`EmochiViewModel.kt`, `ExploreTabContent.kt`, `AidenStoriesModal.kt`):**
   - Şablondan veya Aiden Stories kartından *"Hikâyeye Başla / Botu Ekle"* tıklandığında:
     * Çift tıklama kilidi (`isCreatingFromTemplate`) ile mükerrer bot oluşturulmasını engelle.
     * Oluşturulan bota şablonun kişiliği, senaryosu, ilk mesajı, optimize avatarı, `BaselineEmotion` başlangıç durumu, hassas tetikleyicileri ve rutinleri tek transaction'da işlensin.
     * Hali hazırda aynı şablondan sohbeti olan kullanıcıya zarif Mor Cam seçeneği sun (*"Mevcut Sohbete Devam Et"* veya *"Yeni Temiz Kopya Başlat"*).
   - Yerel arama/filtre sonucu boşsa Mor Cam temalı, net Türkçe boş durum kartı (*"Aramana uygun şablon bulunamadı — Filtreyi Temizle"*) göster.

---

## ⚙️ SİSTEM #12: ARKA PLAN İŞÇİLERİ — PROAKTİF, BELLEK & DUYGU DENETİMİ (7.5 → 10/10)

### 🔍 Mevcut 2.5 Puanlık Boşluklar
1. **Bozuk Bildirim İkonu & İzin Güvenliği:** `RoutineNotificationWorker.kt` (ve varsa diğer bildirim çağrıları) bozuk ve 1.41 MB boyutundaki `ic_vai_logo.png`'yi kullanıyor; Android 13+ (API 33+) cihazlarda `POST_NOTIFICATIONS` izni kapalıyken veya aktif sohbet açıkken bildirim gönderme kenar durumları sıkılaştırılmalı.
2. **Önbellek Senkronizasyonu (`BackgroundMemoryMergeWorker.kt`):** Arka planda bellek birleştirme / TTL arşivleme bittikten sonra `EmochiRepository.invalidateMemoryCaches(botId)` tetiklenmezse ön plandaki sohbet bayat RAG önbelleği okuyabilir.
3. **Çalışma Politikası, Pil Kısıtları & `CancellationException` Hijyeni:**
   - `ProactiveMessageWorker`, `BackgroundMemoryMergeWorker`, `BackgroundEmotionAuditWorker` ve `RoutineNotificationWorker` kayıtlarında tek tip `ExistingPeriodicWorkPolicy` ve `Constraints` (`setRequiresBatteryNotLow(true)`, ağ kullananlarda `NetworkType.CONNECTED`) garantilenmeli.
   - Tüm AI çağrısı yapan işçiler doğrudan `ProviderFallbackManager` (`ExecutionMode.UTILITY`) üzerinden geçmeli; kullanıcı `Öncelik: Free API'siz` seçtiyse veya uygun sağlayıcı yoksa kota yakmadan/crash atmadan `Result.success()` dönmeli.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Temiz Bildirim İkonu, Sessiz Saatler & Aktif Sohbet Koruması (`ProactiveMessageWorker.kt`, `RoutineNotificationWorker.kt`):**
   - Tüm bildirimlerde yeni 24×24 dp beyaz vektör **`R.drawable.ic_notification_emochi`** küçük ikonunu kullan (Android bildirim çubuğunda %100 keskin ve uyumlu görünüm).
   - Android 13+ (`TIRAMISU`) cihazlarda `ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED` kontrolü yap; izin yoksa crash atmadan `Result.success()` dön.
   - Kullanıcı o an ilgili botun sohbetindeyse (`bot.id == EmochiRepository.activeBotId`) aynı bota bildirim gönderme.
   - Her iki bildirim işçisinin `PendingIntent`'i `Intent(ctx, MainActivity::class.java)` + `action = "com.example.OPEN_CHAT"` + `putExtra("botId", bot.id)` + `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE` ile doğrudan o karakterin sohbetini açsın.
   - Gece rahatsız etmeme koruması (23:00–08:00 yerel saat aralığında proaktif/rutin bildirimlerini sessizce ertele; `proactiveEnabled == false` olan botları her zaman atla).
2. **Bellek & Duygu İşçisi Dayanıklılığı (`BackgroundMemoryMergeWorker.kt`, `BackgroundEmotionAuditWorker.kt`):**
   - `BackgroundMemoryMergeWorker`: Birleştirme veya `natural_decay_ttl` arşivlemesi yapılan her `botId` için işlem sonunda `invalidateMemoryCaches(botId)` çağır; `isPinned == true` ve `category == "CORE"` kayıtlarına asla dokunma.
   - `BackgroundEmotionAuditWorker`: `ProviderFallbackManager` (`ExecutionMode.UTILITY`) kullan; `CancellationException` gelirse yeniden fırlat, geçici ağ hatalarında `runAttemptCount < 2` ise `Result.retry()`, aksi halde `Result.success()` dönerek WorkManager döngüsünü kilitleme.
3. **Tek Merkezli Güvenli Zamanlama (`EmochiApplication.kt`):**
   - Tüm periyodik işçilerin `WorkManager.enqueueUniquePeriodicWork(..., ExistingPeriodicWorkPolicy.KEEP, ...)` ile tekilleştirilmiş, pil-dostu kısıtlarla planlandığını ve özellik bayrağı kapatıldığında (`setRoutineNotif(false)`) ilgili işin `cancelUniqueWork(...)` ile anında iptal edildiğini doğrula.

---

## ❤️‍🔥 SİSTEM #14: CANLI DUYGU MOTORU V3 & YAN KARAKTER DUYGULARI (7.5 → 10/10)

### 🔍 Mevcut 2.5 Puanlık Boşluklar
1. **Zaman Bazlı Duygu Sönümlemesi Canlıda Kopuk:** `applyPassiveDecayPerMessage()` çalışıyor ama hikâye takviminde günler geçtiğinde (`story_time_skip` ile örneğin `"ertesi sabah"` veya `"3 gün sonra"`) çalışan **`applyNaturalEmotionalDecay()`** canlı sohbet akışına bağlı değil! Bu yüzden karakter 5 hikâye günü geçse bile saniyeler önce kavga etmiş gibi aynı anlık öfkede kalabiliyor.
2. **Yan Karakter Duyguları Tek Yönlü Kalıyor:** `character_emotions` tablosu ve `CharacterEmotionControlModal` var ancak `SystemPromptBuilder` aktif yan karakterlerin mevcut duygularını prompta net vermediği ve `[[STATE_JSON]]` içindeki `"cast_emo"` çıktısını `CharacterEmotionDao`'ya uçtan uca işlemediği için yan karakterler hikâye boyunca donuk kalıyor.
3. **Sınır Taşması & Kişilik Uyumsuzluğu:** Duygu değişim deltalarının her karakterin `BaselineEmotion` profili (`emotionalRegulationCapacity`, `attachmentStyle`) ile uyumlu yumuşatılması ve `[0..100]` arasında kesin kilitlenmesi şart.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Hikâye Zamanına Bağlı Doğal Duygu Sönümlemesi (`EmotionEngine.kt`, `EmotionEngineManager.kt`, `EmochiRepository.kt`):**
   - `applyNaturalEmotionalDecay(state, baseline, deltaStoryDays)` fonksiyonunu **yalnızca hikâye takvimi gün farkıyla (`deltaStoryDays > 0`)** çalışacak şekilde canlı sohbet akışına bağla (gerçek duvar saati değil!):
     * Anlık/geçici duygular (öfke, panik, ani şaşkınlık) geçen hikâye gününe ve karakterin `emotionalRegulationCapacity` (0–100) değerine göre kademeli olarak baz çizgisine (`baselineEmotionState`) yaklaşır (ör. düzenleme kapasitesi yüksek karakter 2 hikâye gününde sakinleşirken, kin tutan/düşük kapasiteli karakter çok daha yavaş soğur).
     * Uzun vadeli ilişki eksenleri (`trust`, `affection`, `deepBond`) zaman geçmekle sıfırlanmaz; yalnızca çok uzun kopukluklarda (ör. `deltaStoryDays >= 14`) çok hafif mesafelenme (`-1..-3`) gösterir.
   - Mesaj başı pasif sönümleme (`applyPassiveDecayPerMessage`) ile hikâye günü sönümlemesinin çift-düşüm yapmaması için aynı turda `deltaStoryDays > 0` çalıştıysa o turda ekstra pasif decay uygulama.
2. **Uçtan Uca Yan Karakter Duygu Döngüsü (`SystemPromptBuilder.kt`, `EmochiRepository.kt`, `CastMemberManager.kt`, `CharacterEmotionControlModal.kt`):**
   - **Prompta Girdi:** Sahnede aktif olan yan karakterlerin (maks 4 karakter) güncel duygu özetini tek kompakt satırda ver:
     `[AKTİF YAN KARAKTER DUYGULARI]: Elif (temkinli, güven:42, yakınlık:35); Hakan (gergin, güven:28, yakınlık:20)`
   - **Modelden Çıktı (`[[STATE_JSON]]` + Eski Etiket Uyumluluğu):**
     * Hem yeni `[[STATE_JSON]]` içindeki `"cast_emo": [{"n":"Elif","d":"rahatlamış","v":+4}]` dizisini hem de geriye dönük uyumluluk için eski `[CHARACTER_EMOTION: ...]` etiketini ayrıştır.
     * İlgili karakteri Türkçe ek normalizasyonu ve `aliases` eşleşmesiyle (`CastMemberManager`) bul; `CharacterEmotionEntity` içindeki duygu, güven/yakınlık değerlerini `[-8..+8]` tur içi güvenli delta sınırı ve `[0..100]` kesin clamp ile güncelle.
     * Güncelleme anında `CharacterEmotionControlModal` ve `EmotionStatusSection` Flow üzerinden canlı yenilensin.
3. **Duygu Tutarlılığı & Rewind Senkronu:**
   - `EmotionStatusSection.kt` içinde ana karakterin güncel baskın duygusu, yoğunluğu, güven/yakınlık değerleri ve aktif sahne yan karakterlerinin duyguları senkron ve akıcı görünsün.
   - `rewindToMessage()` geri sarma işleminde yan karakter duygularının da tutarlı biçimde geri alındığını/temizlendiğini doğrula.

---

## ⏳ SİSTEM #15: HİKÂYE TAKVİMİ & ZAMAN ALGISI / `TimePerception` (7.5 → 10/10)

### 🔍 Mevcut 2.5 Puanlık Boşluklar
1. **Kopuk `story_time_skip` Döngüsü:** `TimePerception.kt` içinde zaman atlama mantığı ve `buildStoryTimePromptBlock` hazır ancak `SystemPromptBuilder` modele `"skip"` alanını ne zaman üreteceğini öğretmiyor; ayrıca model `"skip": 2` veya `"skip": "ertesi sabah"` döndürdüğünde bu değişiklik tüm bağlı alt sistemlere (duygu sönümlemesi, rutinler, yaş/doğum günü, bayat reappraisal temizliği) atomik yayılmıyor.
2. **Açık Tarih vs Göreli Gün Ayrımı:** Kullanıcının açık bir tarih vermediği fantastik/zamansız hikâyelerde (`hasExplicitStoryDate == false`) sahte veya sabit bir takvim tarihi (`"2026-08-29"` gibi) uydurulmamalı; yalnızca `"Gün N • Sabah/Öğle/Akşam/Gece"` olarak akmalı.
3. **Türkçe Zaman İfadesi Yedek Algılaması:** Küçük/zayıf modeller bazen metinde *"Üç gün sonra..."* veya *"Ertesi sabah uyandığında..."* yazıp JSON'a `"skip"` koymayı unutabilir.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Tam Bağlı Hikâye Zamanı Motoru (`TimePerception.kt`, `SystemPromptBuilder.kt`, `EmochiRepository.kt`):**
   - `SystemPromptBuilder` P2 bloğuna `TimePerceptionEngine.buildStoryTimePromptBlock(...)` çıktısını ekle ve P5 `[[STATE_JSON]]` talimatında `"skip"` kuralını net ver:
     * Aynı gün içinde zaman ilerlediyse: `"skip": "öğle"`, `"akşam"`, `"gece"`.
     * Gün atladıysa: `"skip": 1` (ertesi gün), `"skip": 3` (3 gün sonra) veya `"skip": "ertesi sabah"`.
     * Zaman atlamadıysa `"skip"` alanını **hiç yazma**.
   - **Hibrit Çift Kanallı Algılama (`TimePerception.kt`):**
     * **Birincil Kanal:** `[[STATE_JSON]]` içinden gelen `"skip"` değeri (sayısal gün veya Türkçe gün dilimi).
     * **Hafif Yerel Yedek:** Model `"skip"` yazmadıysa ancak bot yanıtının **anlatımında (`*...*` veya diyalog dışı anlatımda)** açık bir zaman atlama başlığı geçiyorsa (*"ertesi sabah"*, *"ertesi gün"*, *"üç gün sonra"*, *"bir hafta sonra"* — tırnak içi `"..."` diyalogları ve gelecek planları *"yarın gideriz"* hariç tutarak!) güvenle algıla.
2. **Atomik Zaman İlerleme Kaskadı (`advanceStoryTime` — `EmochiRepository.kt`):**
   - `deltaDays > 0` veya `sceneTimeOfDay` değiştiğinde tek bir koordineli akış çalıştır:
     1. `bot.storyDayCounter += deltaDays`.
     2. `bot.hasExplicitStoryDate == true` ise `java.util.Calendar` + `SimpleDateFormat("yyyy-MM-dd", Locale.US)` ile `bot.storyCalendarDate` tarihini kesin olarak `deltaDays` gün ileri al (`hasExplicitStoryDate == false` ise boş bırak, asla uydurma tarih yazma!).
     3. `deltaDays > 0` ise **Sistem #14** `applyNaturalEmotionalDecay(state, baseline, deltaDays)` çağır.
     4. `deltaDays > 0` ve bir yıl/doğum günü eşiği geçildiyse karakter & yan karakter yaş güncellemesini (`CastDetector` / `TimePerception`) kontrol et.
     5. `deltaDays > 0` ise yeni gün için **Sistem #21** rutin uygunluğunu aç ve 14+ hikâye günü geçmiş çözülmemiş `pending_reappraisals` kayıtlarını bayatlat (`expired = true`).
     6. `WorldState` içindeki sahne zamanını (`sabah` / `öğle` / `akşam` / `gece`) güncelle ve sohbet üst bilgisinde / Hızlı Profil'de tutarlı göster.
3. **Klonlama & Rewind Tam Uyumu:**
   - Rewind yapıldığında `storyDayCounter`, `storyCalendarDate` ve `sceneTimeOfDay` hedef noktaya geri dönsün; klonlamada `hasExplicitStoryDate == false` iken `storyCalendarDate = ""` kalsın.

---

## 📅 SİSTEM #21: RUTİN TAKİBİ & RUTİN BİLDİRİMLERİ (7.5 → 10/10)

### 🔍 Mevcut 2.5 Puanlık Boşluklar
1. **Canlı Sohbette Gün Filtresi Eksikliği:** `RoutineNotificationWorker.kt` tarafında API 24 `Calendar` + Türkçe günler + `OPEN_CHAT` deep-link zaten iyileştirildi; **ancak** canlı sohbet promptu hazırlanırken `EmochiRepository` / `SystemPromptBuilder` tarafında aktif hikâye gününe göre filtreleme yapılmadan yalnızca `firstOrNull()` alınıyor ve anılan rutin `markRoutineMentioned` ile işaretlenmediği için yanlış günde veya her turda tekrar tekrar gelebiliyor!
2. **Bozuk Bildirim İkonu & Bayrak Senkronu:** `RoutineNotificationWorker.kt` bozuk `ic_vai_logo.png`'yi kullanıyor; `AppFeatureFlags.setRoutineNotif(v)` değiştiğinde worker'ın anında planlanması/iptal edilmesi kesinleştirilmeli.
3. **Geniş Türkçe Rutin Çıkarımı (`WorldStateManager.kt`):** Karakter açıklamasındaki `"Her Pazartesi antrenmana gider"`, `"Sabahları mutlaka Türk kahvesi içer"`, `"Cuma akşamları sahilde yürür"` gibi doğal ifadelerin tamamı yakalanmalı.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Canlı Sohbette Doğru Gün & Tekrar Korumalı Rutin Döngüsü (`EmochiRepository.kt` & `SystemPromptBuilder.kt`):**
   - `getActiveRoutineReminder(botId, storyDateStr, storyDayCounter, sceneTimeOfDay)` metodunu canlı `generateAiReply()` akışına bağla (kör `firstOrNull()` kullanımını tamamen kaldır!):
     * `weekly` rutinler: `hasExplicitStoryDate == true` ise `getDayOfWeekTurkish(storyDateStr)` ile, değilse `storyDayCounter` üzerinden deterministik haftanın günüyle **case-insensitive Türkçe eşleşme** yap (`"Pazartesi"`, `"Salı"`, ..., `"Pazar"`).
     * `morning` / `evening` / `daily` rutinler: `sceneTimeOfDay` uygunsa (veya belirtilmemişse) seç.
     * **Tekrar Engeli:** `lastMentionedStoryDate == currentStoryDayKey` (tarih veya `"DAY_$storyDayCounter"`) olan rutin aynı hikâye gününde **asla** ikinci kez enjekte edilmesin; ertesi hafta / ertesi gün otomatik yeniden açılsın.
   - Aktif rutin bulunduğunda `SystemPromptBuilder` P2 bloğuna tek doğal cümle ekle:
     `[BUGÜNÜN KARAKTER RUTİNİ]: Bugün ${dayName} — alışkanlığı: "${routine.routineDescription}". Sahneye doğal uyuyorsa satır arasında hissettir, zorlama.`
   - Bot yanıtı başarıyla üretildikten sonra `markRoutineMentioned(routine.id, currentStoryDayKey, bot.id)` çağır.
2. **Doğal Türkçe Rutin Çıkarımı & Temizlik (`WorldStateManager.kt`):**
   - `"her pazartesi ..."`, `"pazartesi günleri ..."`, `"sabahları ..."`, `"her sabah ..."`, `"akşamları ..."`, `"her gece ..."` kalıplarının tamamını destekle; aynı bot için mükerrer rutin kaydını normalize metin karşılaştırmasıyla engelle.
3. **`RoutineNotificationWorker.kt` 10/10 Cilası:**
   - Bildirim ikonunu yeni 24×24 dp beyaz vektör **`R.drawable.ic_notification_emochi`** yap.
   - `Calendar` tabanlı API 24+ Türkçe gün eşleştirmesi, `POST_NOTIFICATIONS` izin kontrolü, aktif açık sohbet (`activeBotId`) atlama, 23:00–08:00 sessiz saat koruması ve `com.example.OPEN_CHAT` + `botId` deep-link'ini doğrula.
   - `AppFeatureFlags.setRoutineNotif(enabled)` değiştiğinde `RoutineNotificationWorker.schedule(ctx)` / `cancel(ctx)` anında çalışsın.

---

## 📁 ETKİLENECEK DOSYALAR HARİTASI

1. **İkon & Sıfır Kalite Kayıplı WebP Optimizasyonu (#4):**
   - `app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.webp` & `ic_launcher_round.webp` *(sağlam kök `res/` PNG'lerinden sıfır kalite kaybıyla dönüştürülür; bozuk `.png` dosyaları silinir)*
   - `app/src/main/res/drawable-nodpi/ic_launcher_foreground_asset.webp` *(432×432, `<45 KB`)*, `ic_app_logo.webp` *(512×512, `<55 KB`)*, `ic_vai_logo.webp` *(512×512, `<65 KB`)* *(bozuk `.png` karşılıkları silinir)*
   - `app/src/main/res/drawable/ic_notification_emochi.xml` *(YENİ 24dp saf beyaz bildirim vektörü)*, `ic_launcher_monochrome.xml` *(YENİ)*, `ic_launcher_background.xml` (`#090414`)
   - `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` & `ic_launcher_round.xml`
   - Kök `res/` klasörü *(SİL)* — **`drawable-nodpi/` altındaki mevcut 96 `.webp` dosyasının HİÇBİRİ SİLİNMEYECEK!**
2. **Aiden & Şablonlar (#6):**
   - `app/src/main/java/com/example/ui/screens/AidenStoriesModal.kt` & `ExploreTabContent.kt`
   - `app/src/main/java/com/example/ui/viewmodel/EmochiViewModel.kt`
3. **Arka Plan İşçileri (#12 & #21):**
   - `app/src/main/java/com/example/work/ProactiveMessageWorker.kt`, `RoutineNotificationWorker.kt`, `BackgroundMemoryMergeWorker.kt`, `BackgroundEmotionAuditWorker.kt`
   - `app/src/main/java/com/example/EmochiApplication.kt` & `FeatureFlagsPanel.kt`
4. **Canlı Duygu V3, Zaman Algısı & Rutinler (#14, #15, #21):**
   - `app/src/main/java/com/example/data/repository/EmotionEngine.kt`, `TimePerception.kt`, `SystemPromptBuilder.kt`, `EmochiRepository.kt`
   - `app/src/main/java/com/example/data/managers/EmotionEngineManager.kt`, `CastMemberManager.kt`, `WorldStateManager.kt`
   - `app/src/main/java/com/example/ui/components/EmotionStatusSection.kt` & `CharacterEmotionControlModal.kt`

---

## 🧪 ZORUNLU TEST PAKETİ (`Systems4_6_12_14_15_21_TenOutOfTenTest.kt`)

Aşağıdaki **9 birim testini** ekleyip mevcut tüm test dosyalarıyla birlikte eksiksiz geçmesini doğrula:
1. **#4 Logo Bütünlük, Boyut & WebP Koruma Testi:**
   - Tüm `mipmap-*/ic_launcher*.webp` ve `drawable-nodpi/ic_launcher_foreground_asset.webp`, `ic_app_logo.webp`, `ic_vai_logo.webp` dosyalarının geçerli `RIFF....WEBP` başlığı taşıdığını (sıfır `EF BF BD` hasarı!), toplam boyutlarının ciddi biçimde küçüldüğünü (`ic_app_logo.webp < 90 KB`, `ic_vai_logo.webp < 100 KB`, `ic_launcher_foreground_asset.webp < 70 KB`), eski bozuk logo PNG'lerinin ve kök `res/` klasörünün kaldırıldığını doğrula.
   - `ic_notification_emochi.xml` ve `ic_launcher_monochrome.xml` vektörlerinin var olduğunu ve `drawable-nodpi/` altındaki mevcut 96 `.webp` dosyasının eksiksiz korunduğunu doğrula.
2. **#6 Şablon Klonlama İdempotency Testi:** Aynı Aiden/Şablon kartından oluşturulan botun baseline duygu, tetikleyici ve rutinlerini eksiksiz aldığını ve çift tıklama kilidinin mükerrer kayıt oluşturmadığını doğrula.
3. **#12 Arka Plan İşçisi Güvenlik & Cache Invalidation Testi:** Aktif açık sohbet (`activeBotId`) ve sessiz saatler (23:00–08:00) için bildirimin atlandığını; `BackgroundMemoryMergeWorker` sonrasında `invalidateMemoryCaches` çağrıldığını ve `CORE`/`isPinned` olgularının korunduğunu doğrula.
4. **#14 Hikâye Zamanı Bazlı Duygu Sönümleme Testi:** `deltaStoryDays = 3` ilerlediğinde geçici öfkenin karakterin `emotionalRegulationCapacity` profiline orantılı olarak baz çizgisine yaklaştığını; `trust`/`affection` eksenlerinin 3 günde bozulmadığını ve gerçek duvar saatine bağlı olmadığını doğrula.
5. **#14 Yan Karakter `cast_emo` Uçtan Uca Testi:** `[[STATE_JSON {"dom":"sakin","cast_emo":[{"n":"Elif'in","d":"rahatlamış","v":5}]}]]` çıktısının Türkçe ek temizliğiyle `"Elif"` karakterini bulup `CharacterEmotionEntity` kaydını güvenli clamp ile güncellediğini ve sonraki `SystemPromptBuilder` çıktısında göründüğünü kanıtla.
6. **#15 `story_time_skip` Kaskad & Açık Tarih Koruması Testi:**
   - `hasExplicitStoryDate == true` (`"2026-11-10"`) iken `"skip": 3` geldiğinde tarihin API 24 `Calendar` ile `"2026-11-13"` ve `storyDayCounter`'ın `+3` olduğunu doğrula.
   - `hasExplicitStoryDate == false` iken `"skip": 2` geldiğinde `storyDayCounter`'ın `+2` olduğunu ama `storyCalendarDate`'in boş (`""`) kaldığını (hardcoded tarih uydurulmadığını) doğrula.
   - Tırnak içindeki `"Yarın sabah görüşürüz"` diyalogunun yanlışlıkla zaman atlama tetiklemediğini kanıtla.
7. **#21 Gün Filtreli Rutin & Tekrar Engeli Testi:** Pazartesi tarihli hikâye gününde yalnızca Pazartesi rutininin seçildiğini (Salı rutininin seçilmediğini), `markRoutineMentioned()` sonrasında aynı hikâye gününde tekrar enjekte edilmediğini ve ertesi Pazartesi yeniden açıldığını doğrula.
8. **#21 Doğal Türkçe Rutin Çıkarım Testi:** `"Her Pazartesi antrenmana gider. Sabahları mutlaka kahve içer."` kişilik metninden 2 ayrı benzersiz rutin çıkarıldığını ve tekrar çalıştırıldığında çiftlenmediğini doğrula.
9. **Derleme & Tam Regresyon:** `./gradlew testDebugUnitTest` ve `./gradlew assembleDebug` sıfır hata ve sıfır yeni uyarıyla geçer.

### ✅ Teslimat Öncesi 10/10 Doğrulama Kontrol Listesi
1. **[ ] #4 Logo Onarımı & Sıfır Kalite Kayıplı Boyut Optimizasyonu Tam mı?** 10 `mipmap-*/ic_launcher*.webp`, `ic_launcher_foreground_asset.webp`, `ic_app_logo.webp` ve `ic_vai_logo.webp` sağlam kök `res/` orijinallerinden kalite kaybı olmadan WebP'ye çevrilip boyutları ~2.7 MB'tan ~250–350 KB seviyesine indirildi mi? `ic_notification_emochi.xml` + `ic_launcher_monochrome.xml` eklendi mi, kök `res/` silindi mi ve **`drawable-nodpi/` altındaki 96 mevcut WebP'ye hiç dokunulmadı mı?**
2. **[ ] #6 Aiden Stories & Şablonlar Akıcı mı?** `AidenStoriesModal` `FrostedBackdropCache` kullanıyor, tüm kartlar `RGB_565` Coil standardında ve çift tıklama kilidi aktif mi?
3. **[ ] #12 Arka Plan İşçileri Sessiz, Pil Dostu & Güvenli mi?** Vektör `ic_notification_emochi`, `POST_NOTIFICATIONS` kontrolü, 23:00–08:00 sessiz saatler, `activeBotId` koruması ve `OPEN_CHAT` deep-link tam mı?
4. **[ ] #14 Canlı Duygu V3 & Yan Karakterler Yaşıyor mu?** Hikâye günü ilerleyince `applyNaturalEmotionalDecay` çalışıyor ve `cast_emo` ile yan karakter duyguları hem DB'de hem promptta güncelleniyor mu?
5. **[ ] #15 Hikâye Takvimi Uçtan Uca Bağlı mı?** `story_time_skip` takvimi, duygu sönümlemesini, yaş/doğum gününü ve rutinleri API 24 uyumlu biçimde atomik güncelliyor mu?
6. **[ ] #21 Rutinler Doğru Günde ve Tekrarsız Çalışıyor mu?** Kör `firstOrNull()` kalktı, gün eşleşmesi + `markRoutineMentioned` canlıda ve bildirim ikonu sağlam mı?
7. **[ ] Tüm Testler & Derleme Yeşil mi?**