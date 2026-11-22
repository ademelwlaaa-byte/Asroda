# 🏆 EMOCHI (ASRODA) — 8.0 → 10/10 FİNAL MÜKEMMELLEŞTİRME ŞARTNAMESİ
**Kapsanan 4 Sistem:** `#7 Mor Cam Tasarım & Kozmik Animasyon Motoru (8.0)` · `#16 Otomatik Karakter Algılama / CastDetector + Ufak AI (8.0)` · `#17 Dinamik Zorluk & Kullanıcı Duygu Sensörü + Ufak AI (8.0)` · `#19 Veritabanı, Güvenlik, Build / CI & Testler (8.0)`

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Bu 4 sistem `Asroda-main` kod tabanında **8.0 / 10** olgunluğundadır. Görevin, mevcut çalışan hiçbir özelliği veya görsel kimliği bozmadan, bu 4 sistemdeki pil/GPU tüketimi, katı regex/kelime listesi sınırları, sessiz veritabanı sıfırlama tehlikesi (`fallbackToDestructiveMigration`), kapalı kutu JAR borcu ve eksik CI test kapısını kökten çözerek tamamını **kusursuz 10/10** üretim standardına çıkarmaktır.
>
> **Kırmızı Çizgiler:**
> 1. **Kısaltma & Regresyon Yasağı:** `// ...`, yarım metod, ölü sınıf veya mevcut 10 test dosyasını (`~95 @Test`) kıran değişiklik **KESİNLİKLE YASAKTIR**.
> 2. **Domino Etkisi Koruması:** Kozmik-gotik Mor Cam UI tasarım dili (`#090414`, buzlu cam, `DstIn` üst/alt gradyan maskesi, üst çerçeve `✦` yıldızı, alt ışık yansıması, Serif tipografi), `AvatarOptimizer` Micro-WebP (`256×256`, `WEBP_LOSSY %82`, ~15 KB), Coil limitleri (`%18 RAM / 35 MB disk`, `Size.ORIGINAL` yasağı), `ChatScreen` son 5 mesaj / kaydırma kuralları ve Emochi Roman AI'nın ahlak bekçiliği/godmodding yasakları **asla bozulmayacaktır**.
> 3. **Akıllı Ufak AI (Utility Model) & Sıfır Takılma Kuralı:** `#16 CastDetector` ve `#17 UserSentimentAnalyzer / DynamicDifficultyCalibrator` sistemlerinde gerçek **Ufak AI (`ExecutionMode.UTILITY`, `X-Emochi-Call-Type: utility`, `temperature = 0.15`, düşük token bütçeli hızlı yardımcı model)** kullanılacaktır. Ancak bu mikro çağrılar sohbet yanıtını **asla kilitlemeyecek**: kısa yarış zaman aşımı (`2.5 sn`) veya arka plan / birleşik Utility akışıyla çalışacak, çevrimdışı / kota hatası / ayar kapalı durumunda **%100 sorunsuz yerel motora** düşecektir.
> 4. **Room v48 Veri Güvenliği:** Hiçbir kullanıcının mevcut sohbeti, botu veya Neural Vault verisi güncelleme sırasında asla silinmeyecektir. Yeni ayar anahtarları Room şemasını bozmadan `AppFeatureFlags` üzerinden saklanacaktır.

---

## ✨ SİSTEM #7: MOR CAM TASARIM & KOZMİK ANİMASYON MOTORU (8.0 → 10/10)

### 🔍 Mevcut 2.0 Puanlık Boşluklar
1. **Reduce Motion (Hareketi Azalt) Desteği Yok:** Android sisteminin erişilebilirlik / animasyon kapatma ayarına (`Settings.Global.ANIMATOR_DURATION_SCALE == 0f`) veya kullanıcının pil tasarrufu tercihine saygı duyulmuyor.
2. **Arka Planda Pil & GPU Sızıntısı:** `CosmicMotionEngine.kt`, `CosmicVisualEffects.kt` ve `OrbView.kt` içindeki sonsuz animasyonlar (`rememberInfiniteTransition`, sürekli yeniden çizilen yıldız/nebula parçacıkları) uygulama arka plana atıldığında (`ON_STOP`) veya ekran kapandığında duraklatılmıyor.
3. **Çizim Döngüsünde (Draw Phase) Bellek Tahsisi:** `Canvas` / `drawBehind` blokları içinde her frame'de yeni `Brush`, `Path` veya liste nesnesi üretilmesi GC (Garbage Collection) takılmalarına (jank) yol açabilir.

### 🛠️ 10/10 Uygulama Kuralları
1. **Merkezi Hareket & Yaşam Döngüsü Yöneticisi (`CosmicMotionEngine.kt`):**
   - `AppFeatureFlags.kt` içine Room şemasını bozmadan yeni kalıcı tercih ekle:
     * `KEY_REDUCE_MOTION = "reduce_motion"` (`reduceMotion: StateFlow<Boolean>`, varsayılan `false`).
     * `GlobalSettingsModal.kt` -> Görünüm/Sistem sekmesine Mor Cam `GlobalMorCamSwitch` ekle: **"Hareketi Azalt & Pil Tasarrufu"** (*"Kozmik parçacık ve yörünge animasyonlarını sadeleştirerek pil tüketimini azaltır."*).
   - `CompositionLocal` olarak **`LocalCosmicMotionPolicy`** sağla (`MotionPolicy(reduceMotion: Boolean, isAppInForeground: Boolean)`):
     * Hem sistem `Settings.Global.ANIMATOR_DURATION_SCALE == 0f` hem de `PowerManager.isPowerSaveMode` + `AppFeatureFlags.reduceMotion` durumunu tek bir `shouldAnimateContinuously` bayrağında birleştir.
     * `MainActivity.kt` seviyesinde `LifecycleEventObserver` (`ON_START` -> `true`, `ON_STOP` -> `false`) ile `isAppInForeground` durumunu canlı yayınla.
2. **Sıfır Arka Plan GPU Tüketimi (`CosmicVisualEffects.kt`, `OrbView.kt`, `AppBackground.kt`, `MorCamDesignSystem.kt`):**
   - `shouldAnimateContinuously == false` olduğunda (arka plan, pil tasarrufu veya Reduce Motion açık):
     * Sonsuz `rememberInfiniteTransition` ve parçacık faz döngülerini **çalıştırma**; yerine sabit, estetik bir kozmik ışık/yıldız karesi (0 FPS yeniden çizim, 0% GPU!) çiz.
     * Kritik geçişleri (sayfa/modal açılışı, `600ms Crossfade`) yumuşak ama kısa (`150ms` veya anlık) hâle getir.
   - `rememberCosmicPhase(...)` yardımcı fonksiyonunu yaz; tüm kozmik efektler dağınık `rememberInfiniteTransition` kurmak yerine tek bir yaşam döngüsü-farkında faz kaynağını kullansın.
3. **Sıfır Frame-İçi Tahsis (Zero-Allocation Draw) & Tekrarlanmayan Buzlu Cam:**
   - `CosmicVisualEffects.kt` ve `MorCamDesignSystem.kt` içindeki tüm `Canvas` / `drawBehind` / `drawWithCache` bloklarını denetle: `Brush`, `Path`, yıldız koordinat listeleri ve `Paint` nesnelerini `remember` veya `drawWithCache` içinde önbellekle; çizim bloğunda liste/nesne oluşturma.
   - `MorCamDesignSystem.kt` içindeki cam kenarlık, üst `✦` yıldızı, alt ışık yansıması ve `DstIn` gradyan maskelerinin görsel görünümünü **birebir koru** (piksel sapması sıfır).

---

## 🎭 SİSTEM #16: OTOMATİK KARAKTER ALGILAMA / `CastDetector` + UFAK AI (8.0 → 10/10)

### 🔍 Mevcut 2.0 Puanlık Boşluklar
1. **Ağırlıklı Regex Sınırı (`CastDetector.kt`):** Yalnızca sabit Türkçe kalıplarla (`"kardeşim Ahmet"`, `"Dr. Selin"`) geçen yan karakterler yakalanıyor; edebi roman anlatımında dolaylı tanıtılan karakterler (*"Kapının eşiğinde duran yaşlı saatçi Kadir sessizce gülümsedi"*, *"Kuzeni Mira ile yıllardır konuşmuyordu"*) kaçırılabiliyor.
2. **Türkçe Çekim Eki & Yanlış Pozitif Sızıntısı:** İki kelimeli yer/mekân adları, mecazlar, cümle başı büyük harfli kelimeler veya Türkçe iyelik/hal ekli isimler (`"Mehmet'le"`, `"Leyla'nın"`, `"Kerem'i"`) bazen ya mükerrer yeni karakter gibi kaydediliyor ya da yanlışlıkla kişi sanılıyor.
3. **Sahne Aktifliği & Kadro Tutarlılığı:** Sahnede fiziksel olarak bulunmayan (sadece geçmişten anılan) karakterlerin aktif kadro promptunu şişirmemesi gerekir.

### 🛠️ 10/10 Uygulama Kuralları (Hibrit: Hızlı Yerel Ön-Filtre + Gerçek Ufak AI Doğrulayıcı)
1. **Katman 1 — Hızlı Yerel Türkçe Ön-Filtre & Ek Gövdeleyici (`CastDetector.kt`, `<1 ms`):**
   - Türkçe kesme işareti ve hal/iyelik eki gövdeleyici (`normalizeTurkishEntityName`): `"Leyla'nın"`, `"Leyla'ya"`, `"Leyla'yla"` -> tek kanonik ad **`"Leyla"`**.
   - Ünvan + İsim (`"Komiser Hakan"`, `"Doktor Elif"`, `"Kaptan Voss"`), akrabalık/rol + İsim (`"kardeşi Mehmet"`, `"eski sevgilisi Deniz"`) ve iki kelimeli tam ad (`"Aiden Blackwood"`) çıkarımını güçlendir.
   - `assets/cast_blacklist.json` listesini genişlet: ana bot adı, kullanıcı karakter adı, gün/ay adları, şehir/mekân adları, yaygın Türkçe cümle başı fiil/zarfları ve tek/iki harfli gürültüler asla aday olmasın.
   - Hızlı **Ufak AI Tetik Kararı (`shouldInvokeMicroAiCastExtraction`)**: Metinde yeni büyük harfli özel ad, rol/akrabalık/ünvan ipucu veya yerel regex'in emin olamadığı belirsiz aday varsa Ufak AI katmanını tetikle; sıradan 2 kelimelik mesajlarda (`"Tamam geliyorum"`) gereksiz API çağrısı yapma!
2. **Katman 2 — Ufak AI Kadro Çıkarıcı & Doğrulayıcı (`CastDetector.kt` + `ProviderFallbackManager` `ExecutionMode.UTILITY`):**
   - **Ayar Anahtarı (`AppFeatureFlags.kt`):** `KEY_MICRO_AI_CAST_SENTIMENT = "micro_ai_cast_sentiment"` (varsayılan `true`, Genel Ayarlar -> AI / Sistem sekmesinde **"Ufak AI Kadro & Duygu Analizi"** Mor Cam anahtarı ile kontrol edilebilir).
   - **Dedicated Micro-AI Metodu (`extractAndValidateCastWithMicroAi`):**
     * Arka planda (`Dispatchers.IO`, sohbet akışını **0 ms** bloklar!) **Utility Model** (`ExecutionMode.UTILITY`, `X-Emochi-Call-Type: utility`, `temperature = 0.15`, `max_tokens = 240`, yalnızca JSON çıktı) ile çalışır.
     * Son 2 mesajı + mevcut kayıtlı yan karakter isim listesini + yerel ön-filtrenin bulduğu şüpheli adayları gönderir ve şu kompakt JSON'ı alır:
       ```json
       {"cast":[{"name":"Kadir","is_person":true,"aliases":["yaşlı saatçi"],"role":"saat ustası","relation":"mahalle tanıdığı","age":62,"in_scene":true}],"rejected":["Galata","Karanlık"]}
       ```
     * **Çift Fayda:** (1) Regex'in yakalayamadığı edebi yan karakterleri (`"yaşlı saatçi Kadir"`) çıkarır; (2) Regex'in yanlışlıkla kişi sandığı yer/mekân/kavram isimlerini (`"is_person": false` / `rejected`) otomatik eler!
   - **Birleşik Küratör Entegrasyonu:** Zaten çalışan `extractAndCommitTurnMemories()` Utility AI çağrısının olduğu turlarda bu şema aynı çağrı içinde birleştirilerek ekstra çağrı tasarrufu yapılır; hafıza küratörünün çalışmadığı ara turlarda ancak `shouldInvokeMicroAiCastExtraction == true` ise bağımsız `extractAndValidateCastWithMicroAi()` arka planda koşar.
   - **Sessiz Fallback:** `isMicroAiCastSentimentEnabled == false`, ağ yok veya Utility AI hata/iptal verirse **sıfır hata ile** Katman 1 yerel sonucunu uygular.
3. **Akıllı Tekilleştirme, Kara Liste Kilidi & Aktif Sahne Filtresi (`CastMemberManager.kt`):**
   - Yeni aday gelmeden önce mevcut `cast_members` tablosunda tam ad (Türkçe normalize), `aliases` listesi (`"Doktor Elif"` <-> `"Elif"`) ve `isBlacklisted == true` kontrolü yap.
   - Eşleşme varsa yeni satır oluşturma; mevcut kaydın eksik alanlarını (`age`, `role`, `aliases`) sessizce zenginleştir ve `lastSeenMessageIndex` değerini güncelle.
   - Kullanıcı tarafından reddedilen/kara listeye alınan bir isim **Ufak AI'dan veya alias üzerinden gelse bile bir daha asla yeniden eklenmesin**.
   - **Sahne İçi Aktif Kadro Filtresi (`filterActiveInSceneCastMembers`):** `SystemPromptBuilder`'a yalnızca şu anki sahnede aktif (`in_scene == true`) veya son 6 mesajda doğrudan etkileşime giren en fazla **4 yan karakter** verilsin.

---

## 🧭 SİSTEM #17: DİNAMİK ZORLUK & KULLANICI DUYGU SENSÖRÜ + UFAK AI (8.0 → 10/10)

### 🔍 Mevcut 2.0 Puanlık Boşluklar
1. **Katı Kelime Listesi Körlüğü (`UserSentimentAnalyzer.kt`):** `"mutluyum"` kelimesini görünce pozitif sayıyor ama **`"hiç de mutlu değilim"`**, **`"neyse boşver, sen her zaman haklısın..."`**, **`"güya çok sevindim (!)"`** veya üstü kapalı kırgınlık/pasif-agresif Türkçe alt metinleri kaçırıyor.
2. **OOC (Karakter Dışı) vs IC (Rol İçi) Karışımı:** Kullanıcının rol yapma gereği yazdığı dramatik çatışma (`*kılıcını çekip öfkeyle bağırdı*`) ile gerçek kullanıcı memnuniyetsizliği/tempo isteği (`((biraz kısa ve hızlı ilerleyelim))` veya karakterden sıkılma) birbirinden ayrılmıyor.
3. **Zıplayan Zorluk Salınımı (`DynamicDifficultyCalibrator.kt`):** Tek bir mesajdaki ani duygu değişimi zorluk/direniş katsayısını sertçe zıplatabiliyor ve karakterin kendi doğasını (ör. soğuk/inatçı bir karakterin kolay kolay yumuşamaması gerektiği gerçeğini) ezebiliyor.

### 🛠️ 10/10 Uygulama Kuralları (Hibrit: Anlık Yerel Türkçe Sensör + Ufak AI Alt Metin Motoru)
1. **Katman 1 — Anlık Yerel Türkçe Sensör (`UserSentimentAnalyzer.kt`, `<1 ms`):**
   - **Negation Window (Olumsuzlama Penceresi):** Duygu kelimesinin 1–3 kelime sağında/solunda Türkçe olumsuzlama (`değil`, `değilim`, `hiç`, `asla`, `yok`, `olmadı`, `istemiyorum`, `-ma/-me` eki) varsa polariteyi **tersine çevir** (`"mutlu değilim"` -> negatif, `"kızgın değilim"` -> nötr/yumuşak).
   - **Yoğunlaştırıcılar (Intensifiers):** `"çok"`, `"aşırı"`, `"fazlasıyla"`, `"gerçekten"`, `"!!!"` skoru `1.35x` güçlendirsin; `"biraz"`, `"hafif"`, `"sanki"` skoru `0.6x` yumuşatsın.
   - **İroni / Alay Sinyalleri:** `(!)`, `"güya"`, `"tabii canım"`, `"aynen öyle..."`, `"neyse boşver"` gibi kalıplarda yüzeysel pozitif kelimenin skorunu düşür/tersle ve `needsMicroAiDisambiguation = true` işaretle.
   - **IC vs OOC Ayrımı:** Parantez içi OOC notlarını (`((...))`, `[OOC: ...]`) ayrı analiz et; OOC direktifleri sahne temposu tercihi olarak işle, karakterin rol içi duygusal çatışmasına karıştırma.
2. **Katman 2 — Ufak AI Alt Metin & Zorluk Değerlendiricisi (`UserSentimentAnalyzer.kt` + `ProviderFallbackManager` `ExecutionMode.UTILITY`):**
   - **Ne Zaman Tetiklenir? (Akıllı Bütçe & Sıfır Yavaşlama):**
     * Kısa, düz, belirsiz olmayan mesajlarda (`<35 karakter` ve ironi/çelişki şüphesi yok, ör. `"Günaydın"`, `"Tamam gidelim"`) boşuna ekstra istek atmaz — Katman 1 yerel analiz anında yeterli olur.
     * Duygusal olarak yüklü, uzun (`>=45 karakter`), pasif-agresif/ironi şüpheli (`needsMicroAiDisambiguation == true`) veya rol içi dramatik gerilimli mesajlarda **`analyzeSentimentWithMicroAi()`** devreye girer (`AppFeatureFlags.isMicroAiCastSentimentEnabled == true` iken).
   - **Yarış Zaman Aşımı (`withTimeoutOrNull(2500L)`):**
     * Utility Model (`ExecutionMode.UTILITY`, `X-Emochi-Call-Type: utility`, `temperature = 0.15`, `max_tokens = 130`) ile kullanıcı mesajı + son bot mesajı özetini değerlendirip şu ultra-kompakt JSON'ı döner:
       ```json
       {"tone":"hurt|warm|tense|playful|distant|sarcastic|neutral","valence":-0.75,"intensity":0.80,"is_ooc_meta":false,"subtext":"kırgınlığını gizleyip mesafe koyuyor","resistance_delta":-0.08}
       ```
     * **2.5 sn içinde yanıt gelirse:** O turun promptuna ve `DynamicDifficultyCalibrator`'a **`0.70 Ufak AI + 0.30 Yerel`** ağırlığıyla anında yansır ve `SystemPromptBuilder`'a tek satırlık `[KULLANICI ALT METNİ]: ...` ipucu verir (bot sadece kelimelere değil, gizli kırgınlığa/alaya da insansı tepki verir!).
     * **2.5 sn içinde yetişmezse veya API/ağ hatası olursa:** Sohbeti **1 ms bile bekletmeden** Katman 1 yerel analizle hemen devam eder; ayrıca ana modelin `[[STATE_JSON]]` içindeki opsiyonel `"u_tone"` sinyaliyle sonraki tur kalibrasyonunu günceller.
3. **Yumuşak Histerezis & Kişilik Çapalı `DynamicDifficultyCalibrator.kt`:**
   - **Üstel Hareketli Ortalama (EMA):** Zorluk/direniş skorunu tek mesajda sıçratmak yerine `newScore = 0.70 * prevScore + 0.30 * turnTarget` (ve Ufak AI'dan gelen `resistance_delta` değeri `coerceIn(-0.15f, +0.15f)` ile sınırlı) şeklinde pürüzsüz güncelle (ani dalgalanma ve "duygusal şizofreni" sıfır!).
   - **Kişilik Taban Çapası (Baseline Anchor):** Botun `BaselineEmotion` profilindeki `attachmentStyle` ve `emotionalRegulationCapacity` değerine göre minimum/maksimum zorluk koridoru belirle (`dismissive_avoidant` soğuk bir karakter 2 mesajda tamamen savunmasız kalmasın; sıcak/destekleyici bir karakter de gereksiz duvar örmesin).
   - **OOC Koruması:** Ufak AI veya yerel sensör `is_ooc_meta == true` dediyse rol içi direniş skorunu değiştirme.
   - **Özellik Bayrağı Tam Uyumu:** `AppFeatureFlags.isDynamicDifficultyEnabled == false` ise kalibratör nötr (`1.0f`) çarpan döndürsün ve prompta ekstra zorluk direktifi enjekte etmesin.

---

## 🛡️ SİSTEM #19: VERİTABANI, GÜVENLİK, BUILD / CI & TESTLER (8.0 → 10/10)

### 🔍 Mevcut 2.0 Puanlık Boşluklar
1. **Veri Silme Bombası (`AppDatabase.kt`):** `.fallbackToDestructiveMigration()` (veya `fallbackToDestructiveMigrationFrom`) açık bırakılmış. Gelecekte tek bir migrasyon atlandığında kullanıcının tüm botları, mesajları ve anıları sessizce yok olur!
2. **Eksik CI Test Kapısı (`.github/workflows/android-release.yml`):** Workflow doğrudan `assembleRelease` derliyor; 10 test dosyasını (`./gradlew testDebugUnitTest`) çalıştırmadığı için bozuk koddan Release APK çıkabiliyor.
3. **`precompiled_missing.jar` Teknik Borcu & R8/Log Güvenliği:**
   - `app/libs/precompiled_missing.jar` içindeki 102 sınıfın hangilerinin gerçekten çağrıldığı, hangilerinin Kotlin kaynak dosyalarıyla gölgelendiği belirsiz.
   - Release derlemelerde hassas API anahtarı / sistem promptu / kullanıcı mesajı loglarının `Logcat`'e sızmaması ve R8 (`proguard-rules.pro`) ile tüm JSON/Room/Backup modellerinin kırılmaması kesinleştirilmeli.

### 🛠️ 10/10 Uygulama Kuralları
1. **Yıkıcı Migrasyonu Kaldır & Şema Kilidi Kur (`AppDatabase.kt` & Test):**
   - `AppDatabase.kt` içindeki `.fallbackToDestructiveMigration()` çağrısını **KESİNLİKLE KALDIR**!
   - Mevcut v1 → v48 arası tüm migrasyon zincirinin kesintisiz (`addMigrations(*ALL_MIGRATIONS)`) olduğunu doğrula; `exportSchema = true` ve `app/schemas/com.example.data.local.AppDatabase/48.json` dosyasının güncel şemayla birebir eşleştiğini garanti et.
   - **Migrasyon Bütünlük Birim Testi:** `ALL_MIGRATIONS` dizisinin boşluksuz olduğunu, `48.json` şema dosyasının var olup geçerli JSON barındırdığını ve `AppDatabase` kodunda `fallbackToDestructiveMigration` çağrısı kalmadığını otomatik doğrulayan test ekle.
2. **Zorunlu CI Kalite Kapısı (`.github/workflows/android-release.yml`):**
   - `assembleRelease` adımından **önce** zorunlu kalite kapısı adımlarını ekle:
     1. `./gradlew testDebugUnitTest --stacktrace` *(Tek bir test bile kalırsa Release APK derlemesi anında dursun!)*
     2. Test sonuçlarını (JUnit XML raporu) hata durumunda kolay incelenmesi için artifact olarak yükle (`if: always()`).
   - Mevcut sabit Release keystore çözme, `assembleRelease` ve `apksigner verify` (V1+V2+V3) adımlarını **aynen koru**; hiçbir gizli anahtarın workflow loglarına basılmadığını doğrula.
3. **`precompiled_missing.jar` Cerrahi Denetimi & Sürüm Güvenliği:**
   - `app/libs/precompiled_missing.jar` içindeki sınıfları proje Kotlin kodlarıyla çapraz karşılaştır:
     * Proje içinde gerçek `.kt` kaynak dosyası zaten yazılmış olan kopya/gölgelenmiş sınıfları tespit edip çakışma riski olmadığından emin ol.
     * Aktif olarak hâlâ JAR'dan çağrılan veri/yardımcı sınıflar varsa kaynak kodlarına taşı veya taşıması riskliyse her biri için `proguard-rules.pro` koruma kuralı + kod içi kısa envanter notu bırak ki R8 release derlemesinde `ClassNotFoundException` / `NoSuchMethodError` **asla** yaşanmasın.
4. **Release Güvenlik & R8 Sertleştirme (`proguard-rules.pro`, `SecureApiKeyVault.kt`, `EmochiApplication.kt`):**
   - `proguard-rules.pro` içine tüm Gson/Room/Backup/API DTO sınıfları (`BackupSnapshot`, `EmotionState`, `BaselineAnalysisResult`, `CustomProviderEntity`, `MemoryToolModels`, yeni Ufak AI Cast/Sentiment DTO'ları vb.) için eksiksiz `-keep` kurallarını doğrula.
   - Release build'lerde (`!BuildConfig.DEBUG`) hiçbir API anahtarı, Authorization header'ı, tam kullanıcı mesajı veya ham promptun `Log.d`/`Log.i`/`Log.v` ile Logcat'e yazılmamasını sağla (`OkHttp` logging yalnızca `BuildConfig.DEBUG` içinde ve `Authorization`/`x-api-key` başlıkları `redactHeader` ile maskeli olsun).

---

## 📁 ETKİLENECEK DOSYALAR HARİTASI

1. `app/src/main/java/com/example/util/AppFeatureFlags.kt` — `KEY_REDUCE_MOTION` ve `KEY_MICRO_AI_CAST_SENTIMENT` (varsayılan `true`).
2. `app/src/main/java/com/example/ui/animation/CosmicMotionEngine.kt`, `CosmicVisualEffects.kt`, `OrbView.kt`, `AppBackground.kt`, `MorCamDesignSystem.kt` — `LocalCosmicMotionPolicy`, `ON_START`/`ON_STOP` arka plan durdurma, Reduce Motion / pil tasarrufu desteği, zero-allocation `drawWithCache`.
3. `app/src/main/java/com/example/MainActivity.kt`, `GlobalSettingsModal.kt`, `FeatureFlagsPanel.kt` — `LocalCosmicMotionPolicy` sağlayıcı, "Hareketi Azalt & Pil Tasarrufu" ve "Ufak AI Kadro & Duygu Analizi" anahtarları.
4. `app/src/main/java/com/example/data/repository/CastDetector.kt`, `CastMemberManager.kt` & `app/src/main/assets/cast_blacklist.json` — Türkçe ek gövdeleyici, `shouldInvokeMicroAiCastExtraction`, `extractAndValidateCastWithMicroAi` (`ExecutionMode.UTILITY`), alias deduplication, aktif sahne filtresi (maks 4).
5. `app/src/main/java/com/example/util/UserSentimentAnalyzer.kt` & `DynamicDifficultyCalibrator.kt` — Türkçe olumsuzlama/yoğunlaştırıcı/ironi/OOC + `analyzeSentimentWithMicroAi` (`ExecutionMode.UTILITY`, `2.5 sn` yarış timeout + yerel fallback), EMA histerezis ve kişilik taban çapası.
6. `app/src/main/java/com/example/data/repository/EmochiRepository.kt` & `SystemPromptBuilder.kt` — Birleşik/arka plan Ufak AI kadro çağrısı, `[KULLANICI ALT METNİ]` tek satır ipucu, `[[STATE_JSON]]` `"u_tone"` harmanlaması.
7. `app/src/main/java/com/example/data/local/AppDatabase.kt` — `fallbackToDestructiveMigration()` kaldırma, kesintisiz migrasyon zinciri doğrulaması.
8. `.github/workflows/android-release.yml` — `assembleRelease` öncesi zorunlu `./gradlew testDebugUnitTest` kapısı ve test raporu artifact'i.
9. `app/proguard-rules.pro` & `app/src/main/java/com/example/EmochiApplication.kt` — R8 DTO koruma kuralları, `precompiled_missing.jar` uyumluluğu ve Release log/header redaction güvenliği.

---

## 🧪 ZORUNLU TEST PAKETİ (`Systems7_16_17_19_TenOutOfTenTest.kt`)

Aşağıdaki **10 birim testini** ekleyip mevcut tüm test dosyalarıyla birlikte eksiksiz geçmesini doğrula:
1. **#7 Motion Policy Testi:** `reduceMotion = true`, `isAppInForeground = false` veya `isPowerSaveMode = true` durumlarının tamamında `MotionPolicy.shouldAnimateContinuously == false` döndüğünü; normal ön planda `true` olduğunu doğrula.
2. **#16 Türkçe Ek Normalizasyonu & Kara Liste Testi:** `"Leyla'nın"`, `"Leyla'yla"`, `"Leyla'ya"` ifadelerinin tek kanonik `"Leyla"` olarak tekilleştiğini; gün/mekân/fiil/ana bot adlarının ve kara listedeki bir karakterin alias'ının asla eklenmediğini kanıtla.
3. **#16 Ufak AI (`UTILITY`) Kadro Çıkarımı & Yanlış Pozitif Eleme Testi:** Sahte/mock Utility AI yanıtından (`"cast":[{"name":"Kadir","is_person":true,...}],"rejected":["Galata"]`) edebi karakterin alias+rol+yaş bilgileriyle eklendiğini, `"Galata"` gibi yer adının elendiğini, kara listedeki adın reddedildiğini ve `filterActiveInSceneCastMembers()` çıktısının maks 4 aktif karakter döndürdüğünü doğrula.
4. **#16 Ufak AI Kapalı / Hata Fallback Testi:** `isMicroAiCastSentimentEnabled = false` veya Utility AI hata verdiğinde `CastDetector`'ın çökmeden yerel Türkçe çıkarıcıya sorunsuz düştüğünü kanıtla.
5. **#17 Türkçe Olumsuzlama, İroni & OOC Yerel Testi:**
   - `"hiç de mutlu değilim"` -> negatif skor,
   - `"kızgın değilim, merak etme"` -> öfke/negatif tetiklemez,
   - `"güya çok sevindim (!)"` -> pozitif şişirme yapmaz + `needsMicroAiDisambiguation == true`,
   - `"((biraz hızlı geçelim))"` -> OOC olarak ayrıştırılır, IC duygusal çatışmayı kirletmez.
6. **#17 Ufak AI (`UTILITY`) Alt Metin Harmanlama & 2.5 sn Timeout Fallback Testi:**
   - Mock Utility AI `"neyse boşver, sen haklısın..."` için `tone="hurt", valence=-0.75, subtext="kırgınlığını gizliyor"` döndürdüğünde sonucun `0.70 AI + 0.30 Yerel` şeklinde harmanlanıp `subtext` ürettiğini doğrula.
   - Mock Utility AI `3 sn` geciktiğinde `2.5 sn` yarış zaman aşımının devreye girip sohbeti bekletmeden yerel sonuca düştüğünü kanıtla.
   - `"Tamam"` gibi kısa düz mesajlarda gereksiz Ufak AI çağrısı yapılmadığını doğrula.
7. **#17 EMA Histerezis, OOC Koruması & Kişilik Çapası Testi:** Tek bir sert/yumuşak mesajda `DynamicDifficultyCalibrator` skorunun ani sıçrama yapmadığını, `is_ooc_meta = true` iken direnişin değişmediğini, `isDynamicDifficultyEnabled = false` iken `1.0f` nötr döndüğünü ve `dismissive_avoidant` karakterin taban direniş koridorunun altına düşmediğini kanıtla.
8. **#19 Yıkıcı Migrasyon Yasağı & Şema Bütünlüğü Testi:** `AppDatabase` migrasyon zincirinin v48'e kesintisiz ulaştığını, `schemas/.../48.json` dosyasının geçerli olduğunu ve kaynakta `fallbackToDestructiveMigration` kalmadığını doğrula.
9. **#19 Release Log & R8 Güvenlik Testi:** `BackupSnapshot`, `EmotionState`, `BaselineAnalysisResult` ve yeni Ufak AI Cast/Sentiment DTO'larının JSON serileştirme/geri yükleme döngüsünün bozulmadığını ve header/key maskeleme yardımcısının API anahtarlarını loglardan gizlediğini doğrula.
10. **Derleme & Tam Regresyon:** `./gradlew testDebugUnitTest` ve `./gradlew assembleDebug` sıfır hata ve sıfır yeni uyarıyla geçer.

### ✅ Teslimat Öncesi 10/10 Doğrulama Kontrol Listesi
1. **[ ] #7:** `LocalCosmicMotionPolicy` arka planda (`ON_STOP`) ve "Hareketi Azalt / Pil Tasarrufu" modunda tüm sonsuz kozmik animasyonları 0% GPU'ya indiriyor, `Canvas` içinde frame başı bellek tahsisi yok ve Mor Cam görseli birebir korunuyor mu?
2. **[ ] #16:** `CastDetector` hem hızlı Türkçe ön-filtreleme yapıyor hem de **Ufak AI (`ExecutionMode.UTILITY`, `temp = 0.15`)** ile dolaylı edebi karakterleri yakalayıp yer/mecaz yanlış pozitiflerini eliyor (ve çevrimdışı/kapalıyken yerel motora düşüyor) mu?
3. **[ ] #17:** `UserSentimentAnalyzer` karmaşık/ironik/pasif-agresif Türkçe mesajlarda **Ufak AI (`ExecutionMode.UTILITY`, `2.5 sn` yarış timeout)** ile gizli alt metni ve IC/OOC ayrımını yakalıyor, kısa mesajlarda boşa çağrı atmıyor; `DynamicDifficultyCalibrator` EMA histerezis + kişilik çapasıyla dengeli çalışıyor mu?
4. **[ ] #19:** `fallbackToDestructiveMigration()` tamamen kaldırıldı, `android-release.yml` Release öncesi zorunlu `testDebugUnitTest` kapısı koşuyor, R8 kuralları + `precompiled_missing.jar` uyumluluğu ve Release log güvenliği tam mı?
5. **[ ] Tüm Testler & Derleme Yeşil mi?**