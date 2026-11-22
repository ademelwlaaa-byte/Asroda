# 🏆 EMOCHI (ASRODA) — 9.5 → 10/10 FİNAL MÜKEMMELLEŞTİRME ŞARTNAMESİ
**Kapsanan 6 Çekirdek Sistem:** `#1 Hibrit RAG & Neural Vault` · `#2 Başlangıç Duygu Analizi` · `#3 Bot Oluşturma Sihirbazı` · `#9 Çoklu API & Fallback` · `#11 Duygulu Nöral Sesli Okuma (TTS)` · `#13 Yedekleme & Dışa Aktarım`

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Bu 6 sistem `Asroda-main` kod tabanında zaten **9.5 / 10** seviyesindedir. Görevin mevcut çalışan mimariyi bozmadan, her bir sistemde kalan son **0.5 puanlık** uç durum (edge-case), senkronizasyon, yaşam döngüsü, OOM/iptal güvenliği ve ses kalitesi boşluklarını cerrahi hassasiyetle kapatıp altısını birden **kusursuz 10/10** seviyesine çıkarmaktır.
>
> **Kırmızı Çizgiler:**
> 1. **Kısaltma & Regresyon Yasağı:** `// ...`, yarım metod veya mevcut 10 test dosyasından herhangi birini bozan değişiklik **KESİNLİKLE YASAKTIR**.
> 2. **Domino Etkisi Koruması:** Mor Cam kozmik-gotik UI dili (`#090414`, buzlu cam, Serif tipografi), `AvatarOptimizer` Micro-WebP (`256×256`, `WEBP_LOSSY %82`, ~15 KB), Coil limitleri (`%18 RAM / 35 MB disk`), `ChatScreen` son 5 mesaj / kaydırma kuralları ve Room v48 şema uyumluluğu **kesinlikle korunacaktır**. Yeni ayar anahtarları Room şemasını bozmadan `AppFeatureFlags` üzerinden saklanacaktır.
> 3. **Sıfır APK Şişirme & Sıfır Ücretli API Bağımlılığı:** APK içine 50–100 MB'lık yerel ONNX/ses modeli veya ağır harici SDK **KESİNLİKLE EKLEME**; yalnızca projede zaten var olan `OkHttp`, `MediaPlayer`, `Android TextToSpeech`, `Room`, `Coroutines` ve `Compose` bileşenlerini kullan.

---

## 🧠 SİSTEM #1: HİBRİT RAG, NEURAL VAULT & YAŞANAN ANILAR (9.5 → 10/10)

### 🔍 Kalan 0.5 Puanlık Boşluklar
1. **Bilgi Grafiği (KG) ↔ RAG Kopukluğu:** `WorldStateManager` içindeki çok adımlı ilişki çözücü (`detectAndResolveGraphQuery` / `traverseRelationGraph`) çalışıyor ancak sonuçları canlı sohbet sırasında `MemoryRetrievalManager` / `SystemPromptBuilder` bağlam paketine enjekte edilmiyor.
2. **Önbellek Tutarlılığı (Cache Invalidation):** Kullanıcı Neural Vault ekranında bir bilgiyi düzenlediğinde, sildiğinde, sabitlediğinde (`isPinned`) veya sohbeti geri sardığında (`rewindToMessage`) yerel BM25 / vektör önbellekleri her yol üzerinden garantili geçersiz kılınmıyor.
3. **Çift Enjeksiyon & Token Bütçesi Taşması:** Bilgi Grafiği notu, Sabitlenmiş (Pinned) olgular, Neural Vault olguları ve Yaşanan Anılar (`LivingMemoryCodec`) arasında aynı bilginin tekrar gönderilme ihtimali bulunuyor.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Canlı KG → RAG Köprüsü (`MemoryRetrievalManager.kt`, `EmochiRepository.kt`, `SystemPromptBuilder.kt`):**
   - `MemoryRetrievalManager` çıktı paketine `graphResolutionNote: String?` alanı ekle.
   - Her kullanıcı mesajında `<2 ms` süren yerel `detectAndResolveGraphQuery(bot.id, userQuery)` çağrısını yap; geçerli bir tam veya kısmi zincir döndüyse bunu `SystemPromptBuilder` P4 hafıza bloğunun **en üstüne** `[İLİŞKİ AĞI GERÇEĞİ]` başlığıyla enjekte et.
2. **Anlamsal Tekilleştirme & Kesin Token Tavanı (`MemoryRetrievalManager.kt`):**
   - Aynı olgu veya aynı `[#ID]` hem `Pinned` hem `RAG Top-K` hem de `Graph` içinde geçiyorsa tekilleştir (öncelik: `Graph > Pinned/CORE > RAG Top-K > LivingMemory`).
   - 32K ve altı bağlam pencereli modellerde toplam hafıza bloğunu deterministik karakter/token tavanında tut (en düşük RRF skorlu öğeden başlayarak kırp; `Pinned` ve `CORE` asla kırpılmasın).
3. **Mutlak Önbellek Senkronizasyonu (`EmochiRepository.kt` & `MemoryEngine.kt`):**
   - `insertMemoryFact`, `updateMemoryFact`, `deleteMemoryFact`, `togglePinMemoryFact`, `rewindToMessage`, `importDataSnapshot` ve `BackgroundMemoryMergeWorker` tamamlandığında tek merkezli `invalidateMemoryCaches(botId)` çağrısını garanti et.
   - `natural_decay_ttl` arşivleme döngüsünde `isPinned == true`, `category == "CORE"` veya son 30 turda atıf almış hiçbir kaydın arşivlenemeyeceğini kod ve testle kilitle.

---

## 🎭 SİSTEM #2: BAŞLANGIÇ DUYGU ANALİZİ / BASELINE EMOTION (9.5 → 10/10)

### 🔍 Kalan 0.5 Puanlık Boşluklar
1. **Tekrar Analizde Çift Tetikleyici Birikmesi:** Kullanıcı botun kişiliğini düzenleyip başlangıç analizini tekrar tetiklediğinde veya klonlama/retry sırasında `sensitive_triggers` tablosunda eski + yeni tetikleyicilerin mükerrer birikme riski var.
2. **Sessiz Heuristic Fallback Belirsizliği:** AI analizi ağ/kota hatası nedeniyle yerel deterministik fallback'e düştüğünde, kullanıcı bunun AI analizi mi yoksa yerel tahmin mi olduğunu net göremiyor ve sonrasında tek tıkla AI ile yenileyemiyor.
3. **Alan Sınırlandırma (Clamping) Güvenliği:** Zayıf/küçük modellerden dönen bozuk JSON değerlerinin (`-15`, `140`, `NaN`, bilinmeyen `attachmentStyle`) baseline durumunu kirletme riski.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Kesin Şema Doğrulama & Clamping (`BaselineEmotion.kt`):**
   - Tüm sayısal duygu ve düzenleme boyutlarını (`affection`, `trust`, `mood`, `emotionalRegulationCapacity`, `triggerMultiplier` vb.) parse aşamasında kesin aralıklara sıkıştır (`coerceIn(0, 100)`, çarpan `coerceIn(1.2f, 2.5f)`).
   - `attachmentStyle` ve `dominantEmotion` alanlarını izin verilen enum/whitelist değerleriyle doğrula; geçersiz gelirse kişilik ipuçlarına göre güvenli varsayılana indir.
2. **İdempotent Tetikleyici Senkronizasyonu (`EmochiRepository.kt`):**
   - `persistBaselineTriggers(botId, triggers)` işlemini `db.withTransaction` içinde idempotent yap: yalnızca `isAutoGenerated` / eski baseline tetikleyicilerini temizleyip yeni benzersiz (normalized topic adına göre deduplicated, maks 3 adet) tetikleyicileri yaz; kullanıcının elle eklediği özel tetikleyicileri koru.
3. **Şeffaf Kaynak Rozeti & Yeniden Analiz (`CreateBotWizardBaselineStep.kt` & `BotSettingsModal.kt`):**
   - `BaselineAnalysisResult` içinde `source: BaselineSource` (`AI_ANALYZED` vs `LOCAL_HEURISTIC`) bilgisini UI'da zarif Mor Cam rozetiyle göster (*"✦ AI Psikoloji Profili"* veya *"⚡ Hızlı Yerel Profil — AI ile Yenile"*).
   - `BotSettingsModal` içine, bot kişiliği/senaryosu sonradan değiştirildiğinde isteğe bağlı çalışan **"Başlangıç Psikolojisini Yeniden Analiz Et"** aksiyonunu bağla (mevcut canlı sohbet duygularını kullanıcı onayı olmadan ezmesin).

---

## 🧙 SİSTEM #3: BOT OLUŞTURMA SİHİRBAZI — 6 ADIM (9.5 → 10/10)

### 🔍 Kalan 0.5 Puanlık Boşluklar
1. **Çift Dokunma (Double-Submit) Riski:** Son adımda "Botu Oluştur" butonuna hızlıca iki kez dokunulduğunda veya işlem sırasında geri tuşuna basıldığında mükerrer bot kaydı veya yarım kalan taslak durumu oluşabilir.
2. **Avatar Seçimi Hata Geri Bildirimi:** Bozuk dosya, desteklenmeyen format veya okuma izni hatasında `AvatarOptimizer` `null` döndüğünde kullanıcıya anlaşılır geri bildirim verilmesi ve geçici bellek temizliği garantilenmeli.
3. **Taslak Temizliği & Geri Çıkış Koruması:** Sihirbazda 2–5. adımlarda verisi olan kullanıcı yanlışlıkla sistem Geri tuşuna bastığında taslağın kaydedildiği netleşmeli; taslak yalnızca veritabanı `insertBot` işlemi **başarıyla tamamlandıktan sonra** temizlenmeli.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Atomik Oluşturma Kilidi (`CreateBotWizardScreen.kt` & `EmochiViewModel.kt`):**
   - Son oluşturma butonunu `isSubmitting` / `AtomicBoolean` kilidiyle koru; kayıt süresince buton yükleme durumuna geçsin ve ikinci tıklamayı %100 engellesin.
   - `insertBot` + `persistBaselineTriggers` + `extractRoutines` tek bir veritabanı transaction'ında tamamlandıktan sonra taslak temizlensin (`clearWizardDraft()`); herhangi bir hata olursa taslak korunup kullanıcıya Mor Cam hata mesajı gösterilsin.
2. **Güvenli Geri Çıkış & Adım Akışı (`CreateBotWizardScreen.kt` & `CreateBotWizardComponents.kt`):**
   - `BackHandler`: Adım > 1 iken bir önceki adıma dön; Adım == 1 ve doldurulmuş alan varsa kısa Mor Cam onay diyaloğu sun (*"Taslak Kaydedilerek Çıkılsın mı / Taslağı Sil / Devam Et"*).
   - Metin alanlarında tutarlı `KeyboardOptions(imeAction = ImeAction.Next)` akışı ve alan doğrulama hatalarında otomatik ilgili alana odaklanma/uyarı kapsülü sağla.
3. **Avatar Yükleme Dayanıklılığı (`AvatarOptimizer.kt` & `CreateBotWizardScreen.kt`):**
   - Galeriden gelen görsel `Dispatchers.IO` üzerinde işlenirken kısa yükleme göstergesi göster; başarısız olursa (*"Görsel okunamadı, lütfen başka bir fotoğraf seçin"*) uyarısı ver.
   - `256×256`, `WEBP_LOSSY (82)`, `~12–25 KB` Micro-WebP çıktı sözleşmesini bozmadan ara `Bitmap` nesnelerinin `try/finally` içinde kesin `recycle()` edildiğini garanti et.

---

## 🌐 SİSTEM #9: ÇOKLU API KATALOĞU, ÖNCELİK & FALLBACK (9.5 → 10/10)

### 🔍 Kalan 0.5 Puanlık Boşluklar
1. **Kullanıcı İptalinin Sağlayıcıyı Cezalandırması:** Kullanıcı yanıt üretilirken sohbetten çıkıp coroutine'i iptal ettiğinde (`CancellationException`), bunun bir ağ/sunucu hatası olarak algılanıp `ProviderHealthTracker`'da sağlayıcıya gereksiz cooldown/hata puanı yazma riski.
2. **Anahtar/Model Değişikliğinde Eski Cooldown'un Kalması:** Kullanıcı `401` (hatalı anahtar) almış bir sağlayıcının anahtarını `GlobalSettingsModal` veya `ApiHubSheets` üzerinden düzelttiğinde ya da "Test Et" başarılı olduğunda, ilgili sağlayıcının circuit-breaker cooldown'u anında sıfırlanmalı.
3. **Ayarlanabilir Zaman Aşımı (Timeout) İhtiyacı:** Kısa timeout hızlı yanıt için iyidir, ancak kullanıcı uzun düşünen (`reasoning` / `<think>`) veya yavaş ücretsiz/özel modelleri kullanırken zaman aşımı yüzünden yanıtının kesilmesini istemeyebilir — bu yüzden timeout korumasının **ayarlardan kapatılabilmesi** şarttır.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **`CancellationException` İzolasyonu (`ProviderFallbackManager.kt` & `ProviderHealthTracker.kt`):**
   - `catch (e: CancellationException) { throw e }` kuralını tüm fallback döngülerinde en üste al; kullanıcı kaynaklı iptaller asla `recordFailure()` çağırmasın ve sağlayıcı güvenilirlik skorunu düşürmesin.
2. **Anında Sağlık Sıfırlama & Canlı Telemetri (`GlobalSettingsModal.kt`, `ApiHubSheets.kt`, `EmochiViewModel.kt`):**
   - Herhangi bir sağlayıcının API anahtarı, taban URL'si veya seçili modeli kaydedildiğinde ya da bağlantı testi başarılı olduğunda `ProviderHealthTracker.resetHealth(providerKey)` çağır.
   - `ProviderReliabilityDashboardUI` ve `ActiveApiPriorityRow` kartlarının `ProviderHealthTracker` içindeki canlı `StateFlow` verisini (son gecikme ms, başarı oranı %, kalan cooldown saniyesi) gerçek zamanlı göstermesini garanti et.
3. **Ayarlardan Açılıp Kapatılabilen Akıllı Zaman Aşımı Koruması (`AppFeatureFlags.kt`, `GlobalSettingsModal.kt`, `ProviderFallbackManager.kt`, `EmochiApplication.kt`):**
   - `AppFeatureFlags.kt` içine kalıcı yeni bayrak ekle (Room şemasını bozmadan `SharedPreferences` + `StateFlow<Boolean>`):
     ```kotlin
     private const val KEY_STRICT_API_TIMEOUT = "strict_api_timeout"
     // Varsayılan: true
     // StateFlow: strictApiTimeout + getter: isStrictApiTimeoutEnabled + setter: setStrictApiTimeout(Boolean)
     ```
   - **UI Kontrolü (`GlobalSettingsModal.kt` -> API Sekmesi):** `"Sansür / Ahlak Vaazı Koruması"` anahtarının hemen altına aynı Mor Cam `GlobalMorCamSwitch` bileşeniyle ekle (ve `FeatureFlagsPanel.kt`'de de senkron göster):
     * **Başlık:** `"Akıllı Zaman Aşımı Koruması (Timeout)"`
     * **Alt Metin:** `"Açıkken takılan yardımcı çağrılar 18 sn, hikâye istekleri 60 sn'de kesilip yedek API'ye geçer. Kapalıyken yavaş/düşünen modeller kesilmeden beklenir."`
   - **Çalışma Davranışı (`OkHttp` per-call `withReadTimeout` / coroutine timeout):**
     * **`isStrictApiTimeoutEnabled == true`:** `ExecutionMode.UTILITY` -> `18 sn` okuma zaman aşımı (maks `2` fallback denemesi); `ExecutionMode.NARRATIVE` -> `60 sn` okuma zaman aşımı ve tam öncelik zinciri.
     * **`isStrictApiTimeoutEnabled == false`:** Kısa zaman aşımı zorlamasını devre dışı bırak -> hem `NARRATIVE` hem `UTILITY` çağrılarında OkHttp istek bazlı `readTimeout(0, TimeUnit.SECONDS)` / cömert `300 sn` güvenlik tavanı uygula ki yavaş ücretsiz veya uzun düşünen (DeepSeek R1, Qwen3 Thinking, yerel Ollama) modeller asla yarıda kesilmesin!
   - `refusal_auto_fallback` açıkken yakalanan OOC ret/vaaz vakalarının `ProviderHealthTracker`'da uzun süreli `AUTH/RATE_LIMIT` kilidine yol açmadığını, yalnızca o turda sıradaki sağlayıcıya devredip loglandığını doğrula.

---

## 🎙️ SİSTEM #11: ÜCRETSİZ DUYGULU NÖRAL SESLİ OKUMA / TTS MOTORU (9.5 → 10/10)

### 🔍 Kalan Boşluklar & Kullanıcı Geri Bildirimi
1. **Düz, Robotik ve Duygusuz Ses Sorunu (EN KRİTİK):** Android'in varsayılan yerel `TextToSpeech` sesleri tekdüze, mekanik ve duygusuz okuyor; tüm paragraf sabit hız (`1.0f`) ve sabit perdeyle (`1.0f`) nefessiz okunduğu için roman atmosferi kayboluyor.
2. **Geç Başlatma Yarış Durumu (Cold-Start Race) & Takılı Equalizer:** `onInit` bitmeden basılan okuma istekleri düşebiliyor; `onError`/`onStop`/odak kaybında `speakingMessageId` sıfırlanmazsa equalizer takılı kalabiliyor; `BookAudioController` ile ses çakışabiliyor.

### 🛠️ 10/10 Uygulama Kuralları (0 TL • 0 MB APK Şişmesi • Gerçek Duygulu İnsan Sesi!)

#### 1. Ücretsiz 2 Katmanlı Nöral + Duygulu Hibrit TTS Mimarisi
Kullanıcıdan hiçbir ücretli API anahtarı istemeden, APK boyutunu büyütmeden mevcut `OkHttpClient` + `MediaPlayer` + `TextToSpeech` ile **gerçek insan doğallığında ve duygulu** seslendirme kur:

* **Katman 1 — Ücretsiz Nöral Bulut Sesi (`EdgeNeuralTtsClient.kt` — YENİ, Varsayılan Açık):**
  - Mevcut `OkHttpClient` WebSocket ile Microsoft Edge Neural TTS uç noktasından ücretsiz, stüdyo kalitesinde **Türkçe ve İngilizce Nöral sesler** üret (`audio-24khz-48kbitrate-mono-mp3`):
    * **Türkçe Nöral Sesler:**
      - `tr-TR-EmelNeural` *(Emel — Doğal, sıcak kadın roman sesi)*
      - `tr-TR-AhmetNeural` *(Ahmet — Derin, akıcı erkek roman sesi)*
    * **İngilizce Nöral Sesler:** `en-US-AvaMultilingualNeural`, `en-US-AndrewMultilingualNeural`, `en-US-AriaNeural`, `en-US-GuyNeural` *(Çok dilli sesler Türkçe metinleri de ekstra doğal tonla okuyabilir)*.
  - Güncel `Sec-MS-GEC` SHA-256 zaman damgası token başlığını ve `X-Timestamp` / `MUID` / `ConnectionId` protokolünü temiz biçimde uygula; sunucu `403` saat kayması (clock skew) dönerse HTTP `Date` başlığından saat farkını otomatik düzeltip 1 kez yeniden dene.
  - **Akıllı Yerel Ses Önbelleği (`cacheDir/tts_audio_cache/`, maks `15 MB` LRU):** Üretilen `.mp3` segmentlerini `SHA-256(voice + ssml)` anahtarıyla kısa süreli önbellekle; aynı mesaj tekrar dinlendiğinde **0 ms gecikme ve 0 KB internetle** çalsın.
  - **Sıfır Kesinti Otomatik Fallback:** Cihaz çevrimdışıysa, ağ yavaşsa (`>4 sn` ilk parça gelmezse) veya uç nokta geçici yanıt vermezse kullanıcıya hata göstermeden **sessizce Katman 2'ye (Yüksek Kaliteli Cihaz Sesi + Duygulu Prosodi)** düş.

* **Katman 2 — Gelişmiş Cihaz Sesi (Çevrimdışı / Otomatik Yedek):**
  - Varsayılan robotik ses yerine, cihazda kurulu `TextToSpeech.voices` listesini akıllı sırala: **`Voice.QUALITY_VERY_HIGH`** ve Google'ın yüksek kaliteli ağ/yeni nesil Türkçe seslerini (`tr-tr-x-*-network`, `tr-tr-x-ama-local` vb.) her zaman eski düşük kaliteli seslerin üstüne koy ve varsayılan seç.
  - Türkçe ses verisi cihazda hiç yoksa (`LANG_MISSING_DATA`), Ses & Dil sekmesinde Mor Cam kartıyla tek tıkla sistem ses paketi indirme (`ACTION_INSTALL_TTS_DATA`) bağlantısı sun.

#### 2. Canlı Duygu & Roman Sahne Prosodi Motoru (`EmotionalTtsProsodyEngine.kt` — YENİ)
Sesin asla "duygusuz ve düz" okunmaması için her mesajı botun **canlı `EmotionState`'ine** + **cümle içi roman işaretlerine** göre parçalayıp dinamik tonlama (prosodi) üret:

* **Segment Sınıflandırması:**
  - `"..."` içindeki karakter **diyalogları** ile `*...*` içindeki **anlatım / iç ses** parçalarını ayır (anlatımda hafif daha sakin ve derin ton, diyalogda karakterin canlı duygusu).
  - Noktalama ve anlam ipuçlarını çözümle:
    * `...` / `—` -> dramatik nefes duraklaması (`320–480 ms`) + hafif alçalan samimi ton.
    * `!` -> canlı vurgulama ve yüksek enerji.
    * `?` -> doğal soru yükselişi ve cümle sonu kısa bekleme (`220 ms`).
* **Canlı `EmotionState` → Ses Tonu Haritası (Kullanıcının temel `ttsSpeed` ve `ttsPitch` ayarının üzerine nazikçe eklenir):**
  * **Üzgün / Kırgın / Melankolik:** Hız `-10%`, Perde `-4%` (`-4Hz`), nefes arası `420 ms` — yumuşak, yavaş ve dokunaklı.
  * **Öfkeli / Gergin / Kıskanç:** Hız `+8%`, Perde `+3%` (`+3Hz`), kısa keskin aralar `150 ms` — baskın ve gerilimli.
  * **Romantik / Çapkın / Fısıltılı / Yakın:** Hız `-12%`, Perde `-3%` (`-3Hz`), samimi uzun aralar `350 ms` — sıcak ve yakın.
  * **Mutlu / Heyecanlı / Neşeli:** Hız `+6%`, Perde `+5%` (`+5Hz`), akıcı aralar `180 ms` — enerjik ve parlak.
  * **Korkulu / Şaşkın / Tedirgin:** Hız `+5%`, Perde `+6%` (`+6Hz`), kesik aralar `260 ms` — nefes nefese ve telaşlı.
  * **Nötr / Sakin Roman:** Hız `0%`, Perde `0%`, doğal paragraf arası `280 ms`.
* **Her İki Katmanda da Çalışır:**
  - **Katman 1 (Edge Neural):** Segment başına temiz **SSML `<prosody rate="..." pitch="...">`** + `<break time="...ms"/>` üretir (tüm XML özel karakterlerini `&amp;`, `&lt;`, `&gt;` kaçışla).
  - **Katman 2 (Android TTS):** Her cümle parçası öncesi `setSpeechRate(...)` + `setPitch(...)` uygular ve cümleler arasına `playSilentUtterance(pauseMs, QUEUE_ADD, ...)` ile gerçek nefes duraklaması ekler.

#### 3. Yeni Ayar Kontrolleri (`AppFeatureFlags.kt` & `GlobalSettingsModal.kt` -> Ses & Dil Sekmesi)
Room şemasını bozmadan `AppFeatureFlags` üzerinden 3 temiz ses tercihi ekle ve **Ses & Dil** sekmesine Mor Cam tasarımıyla yerleştir:
1. **Ses Motoru Seçici (Kapsül Seçim):**
   - `✦ Nöral Duygulu Ses (Ücretsiz • Önerilen)` (`ttsEngineMode = "neural"`, varsayılan)
   - `📱 Cihaz Sesi (Çevrimdışı)` (`ttsEngineMode = "device"`)
2. **Duygusal Tonlama (Prosodi) Anahtarı (`ttsEmotionalProsody`, varsayılan `true`):**
   - *"Karakterin o anki duygusuna (üzgün, öfkeli, romantik, heyecanlı) ve noktalama işaretlerine göre ses tonunu, hızını ve nefes aralarını otomatik ayarlar."*
3. **Nöral Ses Kartları + Canlı "▶ Duygulu Dinle" Önizleme:**
   - Nöral mod seçiliyken `Emel (Kadın • Doğal TR)`, `Ahmet (Erkek • Derin TR)` ve çok dilli nöral sesler (cihaz modundayken kalite sıralı cihaz sesleri) tek tıkla seçilebilsin; **"▶ Sesi Dinle"** butonu seçili sesle gerçek duygulu bir Türkçe roman cümlesini anında okusun.

#### 4. Yaşam Döngüsü, Cold-Start & Ses Çakışma Güvenliği (`ChatTtsManager.kt`, `BookAudioController.kt`, `MainActivity.kt`)
- `speak(messageId, text, emotionState: EmotionState? = null)`: `isInitialized == false` iken gelen isteği `pendingSpeakRequest` kuyruğunda tutup hazır olunca otomatik oynat.
- Parça bazlı oynatmada (Edge Neural `MediaPlayer` kuyruğu veya Android TTS `UtteranceProgressListener`), son parça bittiğinde, hata oluştuğunda, kullanıcı durdurduğunda veya ses odağı (`AUDIOFOCUS_LOSS`) kaybedildiğinde **`_speakingMessageId.value = null`, `_isSpeaking.value = false` ve `abandonAudioFocus()`** %100 garantili çalışsın (equalizer asla takılı kalmaz).
- TTS başlarken `BookAudioController.pauseIfPlaying()` çağır; `BookAudioController` çalarken aktif TTS'i durdur. `MainActivity.onStop()` ve `EmochiViewModel.onCleared()` anında aktif ses/MediaPlayer/AudioFocus kaynaklarını temizle.

---

## 💾 SİSTEM #13: YEDEKLEME, OTOMATİK KURTARMA & DIŞA AKTARIM (9.5 → 10/10)

### 🔍 Kalan 0.5 Puanlık Boşluklar
1. **Eksik Tablo Kapsamı & İşlem Atomikliği:** Room v48'deki `custom_providers` (API anahtarları varsayılan maskeli!), `pending_reappraisals` ve `archived_memories` kayıtlarının `BackupSnapshot` v2 içinde eksiksiz taşınması ve geri yüklemenin tek bir `db.withTransaction` içinde yapılması garanti edilmeli.
2. **Devasa Sohbetlerde Dışa Aktarım OOM Riski (`ChatExporter.kt`):** 1.500+ mesajlık çok uzun bir sohbeti `.txt`/`.md`/`.json` olarak dışa aktarırken tüm metni tek `StringBuilder` olarak RAM'de tutmak düşük RAM'li cihazlarda bellek baskısı yaratabilir.
3. **Eski Artık Dosya Temizliği:** Önceki sürümlerden kalmış olabilecek `getExternalFilesDir(null)` altındaki eski güvensiz yedek kopyaları ve `cacheDir/exports/` altındaki 24 saatten eski geçici dosyalar otomatik temizlenmeli.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Tam Kapsamlı, Doğrulamalı & Tek-Transaction `BackupSnapshotManager.kt`:**
   - `BackupSnapshot v2` içine `customProviders` (`apiKey` alanı `includeApiKeys == false` iken `""` maskeli!), `pendingReappraisals` ve `archivedMemories` listelerini varsayılan `emptyList()` ile ekle (eski `v1` ve önceki `v2` yedekleriyle **%100 geriye dönük uyumlu**!).
   - `validateBackupJson(json)` ön doğrulaması ekle: bozuk JSON, boş bot/ayar yapısı veya desteklenmeyen gelecek `version > CURRENT_BACKUP_VERSION` durumunda veritabanına dokunmadan net Türkçe hata mesajı dön.
   - `importDataSnapshot(..., mergeMode: Boolean)` işleminin tamamını tek bir `db.withTransaction { ... }` bloğuna sar; geri yükleme sırasında maskeli (`""`) gelen API anahtarları cihazdaki mevcut `SecureApiKeyVault` / `UserSettingsEntity` / `CustomProviderEntity` anahtarlarını **ASLA ezmesin**.
2. **Akış Tabanlı (Streaming) OOM-Güvenli `ChatExporter.kt`:**
   - Dosyaya yazarken (`exportToFile` / `exportToUri`) dev tekil `String` üretmek yerine `Dispatchers.IO` üzerinde `BufferedWriter` ile mesajları tek tek akıtarak yaz; işlem öncesi `cacheDir/exports/` klasöründe 24 saatten eski dosyaları sil.
   - `BackupSnapshotManager.cleanupLegacyExternalBackups(context)` fonksiyonunu uygulama açılışında / otomatik yedek sırasında bir kez çalıştırıp harici depolamada kalmış eski düz metin yedek dosyasını güvenli şekilde sil.

---

## 📁 ETKİLENECEK DOSYALAR HARİTASI

1. `app/src/main/java/com/example/util/AppFeatureFlags.kt` — `KEY_STRICT_API_TIMEOUT`, `KEY_TTS_ENGINE_MODE`, `KEY_TTS_EMOTIONAL_PROSODY`, `KEY_TTS_NEURAL_VOICE`.
2. `app/src/main/java/com/example/data/managers/MemoryRetrievalManager.kt` & `SystemPromptBuilder.kt` — KG → RAG enjeksiyonu, tekilleştirme, token tavanı.
3. `app/src/main/java/com/example/data/repository/BaselineEmotion.kt` & `EmochiRepository.kt` — Kesin clamping, `BaselineSource`, idempotent trigger, `invalidateMemoryCaches`, tek-transaction bot oluşturma.
4. `app/src/main/java/com/example/ui/screens/CreateBotWizardScreen.kt`, `CreateBotWizardComponents.kt`, `CreateBotWizardBaselineStep.kt` — Double-submit kilidi, `BackHandler` taslak koruması, kaynak rozeti, avatar hata uyarısı.
5. `app/src/main/java/com/example/data/managers/ProviderFallbackManager.kt`, `ProviderHealthTracker.kt` & `EmochiApplication.kt` — `CancellationException` izolasyonu, `isStrictApiTimeoutEnabled` kontrollü timeout (`18s/60s` vs sınırsız/300s), `resetHealth` bağlantıları.
6. `app/src/main/java/com/example/util/EmotionalTtsProsodyEngine.kt` *(YENİ)* — `EmotionState` + diyalog/anlatım + noktalama tabanlı dinamik SSML & Android prosodi motoru.
7. `app/src/main/java/com/example/util/EdgeNeuralTtsClient.kt` *(YENİ)* — Ücretsiz OkHttp WebSocket Nöral TTS (`tr-TR-EmelNeural`, `tr-TR-AhmetNeural`), `Sec-MS-GEC` + saat kayması kurtarma, 15 MB MP3 LRU önbellek.
8. `app/src/main/java/com/example/util/ChatTtsManager.kt` & `BookAudioController.kt` — 2 katmanlı Nöral→Cihaz otomatik fallback, cold-start kuyruğu, garantili `speakingMessageId` sıfırlama, ses çakışma koruması, `onStop` temizliği.
9. `app/src/main/java/com/example/data/managers/BackupSnapshotManager.kt` & `app/src/main/java/com/example/util/ChatExporter.kt` — Tam tablo kapsamlı tek-transaction v2 import, streaming `BufferedWriter` export, eski dosya temizliği.
10. `app/src/main/java/com/example/ui/components/GlobalSettingsModal.kt`, `BotSettingsModal.kt`, `ApiHubSheets.kt`, `FeatureFlagsPanel.kt`, `EmochiViewModel.kt`, `MainActivity.kt` — Timeout kapatma anahtarı, Nöral/Cihaz TTS seçici, Duygusal Tonlama anahtarı, canlı sağlık sıfırlama.

---

## 🧪 ZORUNLU TEST PAKETİ (`FinalSixSystemsTenOutOfTenTest.kt`)

Aşağıdaki **9 birim testini** ekleyip mevcut tüm 10 test dosyasıyla birlikte yeşile geçir:
1. **#1 KG → RAG Enjeksiyon & Tekilleştirme Testi:** 3 adımlı Bilgi Grafiği sorgusunun `MemoryRetrievalManager` paketine `[İLİŞKİ AĞI GERÇEĞİ]` olarak girdiğini, mükerrer olguların tekilleştiğini ve `CORE`/`isPinned` kayıtlarının TTL ile arşivlenmediğini doğrula.
2. **#2 Baseline Clamping & İdempotent Trigger Testi:** Sınır dışı JSON (`affection = 999`, `trust = -50`, `multiplier = 9.0`) geldiğinde değerlerin güvenle sınırlandığını ve üst üste iki baseline analizinde `sensitive_triggers` tablosunda mükerrer kayıt oluşmadığını doğrula.
3. **#3 Sihirbaz Çift-Gönderim & Taslak Koruma Testi:** Eşzamanlı iki oluşturma çağrısının yalnızca 1 bot oluşturduğunu ve DB hatasında taslağın silinmediğini doğrula.
4. **#9 `CancellationException` & Sağlık Sıfırlama Testi:** Coroutine iptalinin `ProviderHealthTracker` hata sayacını artırmadığını; `401` sonrası anahtar güncellenince `resetHealth()` ile sağlayıcının anında tekrar hazır duruma döndüğünü kanıtla.
5. **#9 Ayarlanabilir Timeout Açık/Kapalı Testi:** `isStrictApiTimeoutEnabled = true` iken `UTILITY` çağrısının `18 sn` / `NARRATIVE`'in `60 sn` limitiyle yapılandırıldığını; `isStrictApiTimeoutEnabled = false` yapıldığında kısa zaman aşımı zorlamasının tamamen devre dışı kaldığını doğrula.
6. **#11 Duygusal Prosodi & SSML Güvenlik Testi:** `EmotionalTtsProsodyEngine`'in aynı metni `"üzgün"` ve `"öfkeli"` `EmotionState` durumlarında farklı hız/perde/duraklama değerleriyle ürettiğini, `"diyalog"` ile `*anlatım*` parçalarını ayırdığını ve `<`, `>`, `&` karakterlerini geçerli XML/SSML olarak kaçışladığını doğrula.
7. **#11 Nöral → Cihaz Sessiz Fallback & Cold-Start Testi:** Edge Neural çağrısı ağ hatası verdiğinde hata fırlatmadan otomatik Katman 2 (Cihaz TTS)'ye geçildiğini, `onInit` öncesi gelen isteğin init sonrası oynadığını ve `onError`/`onStop` anında `speakingMessageId`'nin `null`'a döndüğünü doğrula.
8. **#13 Backup v2 Tam Kapsam, Maskeleme & Bozuk JSON Rollback Testi:** `customProviders` (anahtar maskeli), `pendingReappraisals` ve `archivedMemories` verilerinin export→import döngüsünde korunduğunu, mevcut yerel API anahtarlarının boş alanlarla ezilmediğini ve bozuk JSON'da veritabanına hiç dokunulmadığını doğrula.
9. **#13 Streaming `ChatExporter` Büyük Sohbet Testi:** 1.500 mesajlık sohbetin `.txt`, `.md` ve `.json` olarak `BufferedWriter` ile hatasız dışa aktarıldığını ve 24 saatten eski export dosyalarının temizlendiğini doğrula.

### ✅ Teslimat Öncesi 10/10 Doğrulama Kontrol Listesi
1. **[ ] #1:** KG sonucu canlı RAG bağlamında, cache invalidation her mutasyonda tetikleniyor ve `CORE`/`Pinned` dokunulmaz mı?
2. **[ ] #2:** Baseline değerleri clamp ediliyor, tetikleyiciler idempotent yazılıyor ve AI/Yerel kaynak rozeti görünüyor mu?
3. **[ ] #3:** Sihirbazda çift tıklama kilidi, güvenli `BackHandler` taslak koruması ve avatar hata bildirimi eksiksiz mi?
4. **[ ] #9:** `CancellationException` sağlayıcıyı cezalandırmıyor, anahtar düzenleyince cooldown sıfırlanıyor ve **Timeout koruması Genel Ayarlar -> API sekmesinden açılıp kapatılabiliyor mu?**
5. **[ ] #11:** **Ücretsiz Nöral Türkçe sesler (`Emel` / `Ahmet`) + Canlı Duygu/Sahne Prosodi Motoru (üzgün, öfkeli, romantik, heyecanlı ton + doğal nefes araları) çalışıyor**, çevrimdışı/hata anında yüksek kaliteli cihaz sesine sessizce düşüyor ve APK boyutunu büyütmüyor mu?
6. **[ ] #13:** Yedekleme tüm tabloları tek transaction'da güvenle taşıyor, API anahtarlarını koruyor ve `ChatExporter` streaming yazım + eski dosya temizliği yapıyor mu?
7. **[ ] Derleme & Tüm Testler:** `./gradlew testDebugUnitTest` ve `./gradlew assembleDebug` sıfır hata ve sıfır yeni uyarıyla geçiyor mu?