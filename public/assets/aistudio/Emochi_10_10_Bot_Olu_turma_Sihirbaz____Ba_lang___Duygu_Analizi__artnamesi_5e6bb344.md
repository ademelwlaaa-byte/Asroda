# ✨ EMOCHI — 10/10 BOT OLUŞTURMA SİHİRBAZI (6 ADIM) & BAŞLANGIÇ DUYGU ANALİZİ (BASELINE EMOTION) ŞARTNAMESİ

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Görevin, Emochi Android uygulamasındaki **6 Adımlı Bot Oluşturma Sihirbazını** (`CreateBotWizardScreen.kt`, `CreateBotWizardComponents.kt`) ve **Başlangıç Duygu Analizi Motorunu** (`BaselineEmotion.kt`, `EmochiRepository.runBaselineAnalysis`, `EmochiViewModel.kt`) mevcut hatalarından tamamen arındırıp sıfır veri kaybı, sıfır boşa token harcaması ve üst düzey UX ile **10/10** seviyesine çıkarmaktır.
> 1. **Kısaltma Yasağı:** `// ...`, yarım fonksiyon veya UI'da görünüp veritabanına yazılmayan sahte alan bırakma.
> 2. **Domino Etkisi Koruması:** Mevcut kozmik-gotik Mor Cam UI tasarım dilini (`#090414`, buzlu cam kartlar, `WizardMorCamHeader`, `WizardMorCamBottomDock`, `MorCamGlassSlider`, Literary/Display Serif tipografi), `AvatarOptimizer` Micro-WebP (~15 KB) sıkıştırmasını ve mevcut `EmotionModels` / `Room` şemasını **kesinlikle bozma**.
> 3. **Gerçek Çalışma Kuralı:** Kullanıcının sihirbazda dokunduğu her tekil slider, etiket, hassas konu ve avatar veritabanına eksiksiz yazılmalı ve ilk sohbet mesajından itibaren AI davranışını doğrudan yönetmelidir.

---

## 🚨 BÖLÜM 1: HEMEN DÜZELTİLECEK 6 KRİTİK GİZLİ HATA (BUG FIX)

Kod tabanında doğrudan tespit edilen şu 6 ağır hata düzeltilmeden yeni özellik eklenmeyecektir:

### 1. Sabit `draftBotId` Eksikliği (Yetim Kalan `SensitiveTriggerEntity` Hatası)
* **Mevcut Hata:** `CreateBotWizardScreen.kt` içindeki `buildCurrentDraft()` her çağrıldığında yeni `UUID.randomUUID().toString()` üretiyor. Adım 5'te `runBaselineAnalysis()` hassas tetikleyicileri `draftId_1` ile Room'a kaydediyor; ancak Adım 6'da "Botu Oluştur" denilince `buildCurrentDraft()` yeniden çağrılıp `draftId_2` üretiyor. Sonuç: Yeni bot veritabanında **0 hassas tetikleyici** ile doğuyor!
* **Kesin Çözüm:** Sihirbaz açıldığında tek bir `val draftBotId = rememberSaveable { UUID.randomUUID().toString() }` oluştur ve tüm adımlarda + nihai kayıtta yalnızca bu sabit ID'yi kullan. Ayrıca tetikleyiciler `onFinish(finalBot, finalTriggers)` imzasıyla `MainActivity` -> `EmochiViewModel.createBotFromWizard(bot, triggers)` hattından kesin olarak geçirilsin.

### 2. Adım 5 Kullanıcı Düzenlemelerinin Kaydedilmeden Çöpe Gitmesi
* **Mevcut Hata:** Kullanıcı Adım 5'te `reviewRelType`, `reviewAttachment`, `reviewBigFive`, `reviewAxes` ve `reviewTriggers` değerlerini değiştirse bile, "Botu Oluştur" anında `baselineEmotionAnalysisJson = res.rawJson` (düzenlenmemiş ham JSON) yazılıyor ve `reviewTriggers` hiç kaydedilmiyor.
* **Kesin Çözüm:** `BaselineEmotionEngine` içine `BaselineAnalysisResult.toCanonicalJson(): String` serileştiricisi ekle. "Botu Oluştur" tıklandığında kullanıcının son düzenlediği tüm alanlardan (İlişki Türü, Big Five, Bağlanma Stili, Başlangıç İlişki Eksenleri, Baskın Duygu, Konuşma Tarzı, Düzenleme Kapasitesi, Zorluk Çarpanı, Hassas Konular) taze bir `BaselineAnalysisResult` oluştur, `toCanonicalJson()` ile `baselineEmotionAnalysisJson`'a yaz ve `reviewTriggers`'ı `draftBotId` ile DB'ye kaydet.

### 3. Türkçe Kesme İşaretinin JSON'ı Parçalaması (`BaselineEmotion.kt`)
* **Mevcut Hata:** `BaselineEmotionEngine.parseAnalysisJson()` içinde `.replace("'", "\"")` global olarak çalıştırılıyor. Türkçe karakter kartlarında `"Aiden'ın geçmişi"`, `"İstanbul'da"` gibi ifadelerdeki tek tırnaklar çift tırnağa dönüşüp **geçerli JSON'ı bozuyor** ve sistemi sessizce anahtar kelime fallback'ine düşürüyor!
* **Kesin Çözüm:** Global `.replace("'", "\"")` satırını **DERHAL SİL**. Önce ham temizlenmiş metni (yalnızca markdown ```json çitlerini ve sondaki virgülleri temizleyerek) doğrudan `JSONObject(...)` ile parse et. Sadece parse başarısız olursa key-boundary güvenli onarım uygula — string değerlerinin içindeki Türkçe kesme işaretlerine asla dokunma!

```kotlin
// DOĞRU YAKLAŞIM: Önce ham JSON'ı dene, Türkçe kesme işaretlerini (Aiden'ın) ASLA bozma!
val stripped = jsonStr
    .replace(Regex("""```(?:json)?""", RegexOption.IGNORE_CASE), "")
    .replace("```", "")
    .trim()
val start = stripped.indexOf('{')
val end = stripped.lastIndexOf('}')
val candidate = if (start in 0 until end) stripped.substring(start, end + 1) else return fallback
val noTrailingComma = candidate.replace(Regex(""",\s*([}\]])"""), "$1")
val root = runCatching { JSONObject(noTrailingComma) }.getOrNull() ?: return fallback
```

### 4. Bağlanma Stili Adlandırma Uyumsuzluğu (`disorganized` vs `fearful_avoidant`)
* **Mevcut Hata:** `BaselineEmotion.kt` promptu ve normalizasyonu `"disorganized"` üretiyor; ancak `PersonalityProfile` (`EmotionModels.kt`) ve `EmochiRepository.kt` içindeki çarpan hesapları `"fearful_avoidant"` bekliyor!
* **Kesin Çözüm:** Uygulama genelinde tek kanonik değer **`"fearful_avoidant"`** olsun. `BaselineEmotionEngine.normalizeAttachment()` hem `"disorganized"` hem `"korkulu"` hem `"fearful_avoidant"` girdiğini `"fearful_avoidant"`'a çevirsin; `CreateBotWizardScreen` Adım 5'teki UI çipi de arka planda `"fearful_avoidant"` (görünen etiket: `"Korkulu-Kaçınan"`) kullansın.

### 5. "AI Otomatik Yazsın" Yeniden Üretim Kilitlenmesi (`EmochiRepository.generateOpeningMessage`)
* **Mevcut Hata:** `generateOpeningMessage(bot)` ilk satırında `if (bot.openingMessage.isNotBlank()) return bot.openingMessage` diyor. Kullanıcı üretilen açılış mesajını beğenmeyip tekrar **"AI Otomatik Yazsın"** dediğinde yapay zeka hiç çağrılmıyor, aynı metin geri dönüyor!
* **Kesin Çözüm:** Fonksiyon imzasını `generateOpeningMessage(bot: BotEntity, forceRegenerate: Boolean = false, toneHint: String = "")` yap. Sihirbazdaki buton daima `forceRegenerate = true` ile çağırsın. Ayrıca kullanıcının açılış mesajı kutusuna yazdığı kısa bir taslak varsa (veya önceki açılışı beğenmediyse), bunu *"Aynı cümleleri tekrarlamadan alternatif ve daha sinematik bir açılış yaz"* talimatıyla değerlendirsin.

### 6. `EmochiViewModel` İçindeki Çift/Ölü Baseline Kodunu Tekleştir
* **Mevcut Hata:** `EmochiViewModel` içinde `analyzeBaseline()`, `baselineAnalysisState`, `cancelBaselineAnalysis()`, `applyBaselineEditsToDraft()` tanımlı ama `CreateBotWizardScreen` bunları hiç kullanmıyor; kendi lokal state'iyle `analyzeBaselineForWizard()` çağırıyor.
* **Kesin Çözüm:** Tek bir temiz akış bırak: ya tüm analiz durumunu (`Loading / Success / Error / Cancel`) `EmochiViewModel.baselineAnalysisState` üzerinden yönet, ya da kullanılmayan ölü ViewModel metodlarını temizle. Kullanıcı Adım 5'ten "Geri" basarsa devam eden analiz coroutine'i anında iptal edilsin (`cancelBaselineAnalysis()`).

---

## 🧠 BÖLÜM 2: BAŞLANGIÇ DUYGU ANALİZİNİ (BASELINE EMOTION) 10/10 YAPMA

### 1. Sıcaklık & Utility Model Disiplini (Sıfır Hata + Tutumlu Token)
* `runBaselineAnalysis()` mutlaka `resolveUtilityModel()` ile tanımlı hızlı/ucuz modeli kullansın ve isteğe **`X-Emochi-Call-Type: utility`** header'ını eklesin (`temperature = 0.15`). Böylece `EmochiApplication`'daki `0.90` hikâye sıcaklığı JSON analizini bozmaz.
* **Akıllı Hash Önbelleği (0 Ekstra Token):**
  * Adım 4 -> Adım 5 geçişinde `buildInputHash(mode, aiName, universeName, aiPersonality, scenario, userCharName, userCharDesc, keyCharacters)` hesapla.
  * Kullanıcı Adım 5'ten geri dönüp hiçbir metni değiştirmeden tekrar Adım 5'e gelirse **AI'yı tekrar çağırma**, mevcut analizi anında göster.
  * Eğer kullanıcı kişilik/senaryo metnini değiştirdiyse otomatik yeniden analiz et; ancak kullanıcının elle oynadığı `userEditedFields` alanlarını koru (kullanıcı yalnızca "Yeniden Analiz" butonuna basarsa manuel düzenlemeleri sıfırla).

### 2. Mod Farkındalıklı Analiz (Kişisel Karakter vs. Evren / RP)
* **Kişisel Karakter (`mode == "personal"`):** Mevcut 14 ilişki tipi + Big Five + Bağlanma Stili + Plutchik 8 duygu + 4 ilişki ekseni (`affection`, `respect`, `comfort`, `resentment`) + `regulationCapacity` + `affectionDifficultyMultiplier` + 3 hassas konu + konuşma tarzı + **1 cümlelik psikolojik gerekçe (`relationshipNote`)** üret.
* **Evren / Hikâye (`mode == "universe"`):** Evren modunda karakter kartı tek bir arkadaşlık değil, atmosfer ve anlatıcı kurgusudur.
  * Analiz promptuna `mode == "universe"` bağlamını ve varsa `keyCharacters` kadrosunu ekle.
  * Evren modunda AI yalnızca genel atmosferin/ana odağın başlangıç duygusunu değil, aynı zamanda başlangıç **Mekanı (`currentLocation`)**, **Zaman Dilimi (`timeOfDay`)**, **Sahne Gerilimi (`globalTensionLevel`, `sceneTension`)** ve **İlk Atmosfer Havası (`sceneMood`)** önerilerini de tek JSON içinde döndürsün; böylece `BotEntity.worldAtmosphere` kör sabit değerlerle değil, senaryoya özgü canlı başlangıçla doğsun!
  * Evren kadrosuna eklenen her `KeyCharacter` için de yerel deterministik motorla (0 ekstra token!) ilk `PersonalityProfile` ve rol bazlı `baseAffectionDifficultyMultiplier` otomatik hesaplansın.

### 3. Türkçe Dil & Çelişki Zekâsı (`BaselineEmotion.kt`)
* **Öncelik Sırası Düzeltmesi:** `calculateDeterministicFallback()` içinde `"düşman"` kelimesi geçerken `isRival` ile `isEnemy` çakışmasını net ayır; `"kardeş"`, `"anne"`, `"baba"`, `"eski sevgili"`, `"patron"`, `"doktor"`, `"rakip"` gibi çoklu Türkçe köklerde yanlış pozitifleri önle (ör. `"annelik"`, `"babacan"`, `"arkadaşça"` gibi sıfatlar gerçek akrabalık/ilişki sanılmasın).
* **Psikolojik Tutarlılık Kilidi:** LLM'den gelen sonuçlarda `applyRelConsistency()` ve `reconcileDominantEmotion()` çalışmaya devam etsin; ek olarak `affection <= 20` iken `attachmentStyle == "secure"` + `comfort >= 75` gibi saçma kombinasyonları otomatik dengelesin.
* **Kişiselleştirilmiş Hassas Tetikleyiciler:** LLM'den gelen 3 hassas tetikleyici (`sensitiveTriggers`) genel klişe kelimeler değil, doğrudan kullanıcının yazdığı kişilik/senaryodan türetilmiş spesifik konular olsun (ör. *"5 yaşındaki mavi gece"*, *"babasının ihaneti"*, *"kimliğinin açığa çıkması"*).

---

## 🎨 BÖLÜM 3: 6 ADIMLI BOT OLUŞTURMA SİHİRBAZINI 10/10 YAPMA

Mevcut Mor Cam görsel dilini (`WizardMorCamHeader`, `WizardMorCamBottomDock`, `WizardMorCamChoiceCard`, `WizardMorCamSectionCard`) koruyarak her adımı kusursuz hale getir:

### 🧭 Genel Sihirbaz Altyapısı (Tüm 6 Adım İçin)
1. **Taslak Koruma & Kazara Çıkış Kalkanı:**
   * Tüm metin/seçim state'lerini `rememberSaveable` ile sakla (ekran döndürme veya arka plana geçişte yazılanlar asla kaybolmasın).
   * Kullanıcı herhangi bir alana veri girdiyse ve "Vazgeç" veya Adım 0'da sistem geri tuşuna bastıysa, Mor Cam tarzı zarif bir **"Taslağı Bırak / Devam Et"** onay penceresi çıkar.
2. **Etkileşimli Üst İlerleme Çubuğu:**
   * Üstteki 6 segmentten **daha önce tamamlanmış adımlara** dokunularak tek tıkla o adıma geri dönülebilmeli (ileriye atlama yalnızca `canNext` sağlanmışsa açık).
3. **Akıllı Doğrulama Geri Bildirimi:**
   * "İleri" butonu pasifken kullanıcı dokunursa sessiz kalmak yerine, eksik zorunlu alanın altında yumuşak lavanta-gül vurgulu tek satır ipucu göster (ör. *"Devam etmek için Karakter Adı ve Kişilik alanlarını doldurun"*).

### 📋 Adım Bazlı 10/10 İyileştirmeler
* **Adım 1 — Mod Seçimi:** Mevcut iki Mor Cam seçim kartı (`Kişisel Karakter` / `Evren-RP`) korunur; seçim değiştiğinde önceki modun alanları silinmez (kullanıcı yanlışlıkla değiştirip geri dönerse yazdıkları durur).
* **Adım 2 — Kimlik & Kişilik / Evren & Kadro:**
  * **Evren Moduna Eksik Alanları Ekle:** Evren moduna da `AvatarOptimizer.processAvatarToMicroWebPBase64()` bağlı **Evren Kapak / Avatar Seçici** ve **Evren Kuralları / Anlatıcı Tarzı (`aiPersonality`)** alanı ekle.
  * **Hızlı Kişilik Kıvılcımları (Opsiyonel Çipler):** Kullanıcının yazmasını kolaylaştırmak için kişilik alanının altına tek tıkla eklenebilen 6–8 edebi arketip etiketi koy (*"Soğukkanlı & Korumacı"*, *"Kıskanç & Tutkulu"*, *"Gizemli & Alaycı"*, *"Sadık Sırdaş"*, *"Tehlikeli Anti-Kahraman"*).
  * **Kadro Temizliği:** Evren kadrosunda (`keyCharacters`) boş bırakılan satırlar "İleri" veya "Botu Oluştur" anında otomatik filtrelensin; her karakter kartı kompakt ve düzenlenebilir olsun.
* **Adım 3 — Senaryo & Senin Rolün:**
  * Mevcut Senaryo, Kullanıcı Karakter Adı ve Açıklama alanlarına ek olarak açılır-kapanır **"✦ Sabit Hafıza / Evren Kuralı Ekle (Opsiyonel)"** kutusu (`pinnedMemory`) koy — state zaten mevcut, UI'ya bağla!
* **Adım 4 — Açılış Sahnesi:**
  * Karakter limitini roman anlatımı için `1000` yerine **`1500`** yap.
  * **"AI Otomatik Yazsın"** kartının üstüne 3 küçük ton çipi ekle: `Dramatik & Gerilimli` | `Sıcak & Samimi` | `Gizemli & Yavaş`.
  * Yeniden üretim bug'ı (`forceRegenerate = true`) düzeltildiği için metin doluyken kart etiketi otomatik **"Alternatif Açılış Üret ✦"** olsun ve tıklanınca yeni bir sahne üretsin.
* **Adım 5 — Başlangıç Duygu Profili (Baseline İnceleme):**
  * **Psikolojik Özet Kartı:** En üstte AI'nın analiz kaynağı rozeti (`✦ Yapay Zeka Analizi` / `✦ Otomatik Tahmin` / `✦ Düzenlendi`), `confidence` uyarısı ve **`relationshipNote`** (AI'nın *"Neden bu profili seçti?"* 1–2 cümlelik açıklaması) gösterilsin.
  * **Başlangıç İlişki Eksenleri (`reviewAxes`) Slider'larını UI'ya Bağla:** State'i zaten var olan `Yakınlık (Sevgi)`, `Saygı`, `Rahatlık` ve `Kırgınlık` değerlerini `MorCamGlassSlider` ile düzenlenebilir yap!
  * **Kişilik & İnce Ayar:** İlişki Türü çipleri, Bağlanma Stili (`Güvenli / Kaygılı / Kaçınan / Korkulu-Kaçınan`), Baskın Duygu, Konuşma Tarzı, Hassas Konular (en fazla 3, tam düzenlenebilir) ve daraltılabilir **"Gelişmiş: Big Five & Duygu Direnci (`regulationCapacity`)"** bölümü düzenli ve nefes alan hiyerarşiyle sunulsun.
  * İlişki Türü değiştiğinde, kullanıcı `reviewAxes` ile manuel oynamadıysa ilgili ilişki türünün tutarlı varsayılan başlangıç eksenlerini öner.
* **Adım 6 — Stil, Zaman, İzinler & Yayın:**
  * Mevcut Yazım Formatı (`RP` / `Sade Sohbet`), Tema Yoğunluğu (`Normal` / `Yoğun`), Hikâye Başlangıç Tarihi ve Görünürlük kartlarına ek olarak:
    * **Hikâye Başlangıç Tarihi Doğrulaması:** `yyyy-MM-dd` formatını anlık doğrula (hatalı formatta uyarı göster ve geçersiz tarih kaydetme; isteğe bağlı "Bugünü Kullan" hızlı çipi koy).
    * **İçerik & Yönetmen Ayarları (`MorCamSwitchRow`):** `Parantez İçi Yönetmen Komutları (OOC)` ve `Sansürsüz Kurgu (+18)` anahtarlarını doğrudan sihirbazda seçilebilir yap.
    * **Varsayılan Gizlilik:** `isPublic` başlangıç değerini `BotEntity` şartnamesine uygun olarak **`false` (Sadece Bana Özel)** yap.
    * **Son Özet Kartı:** "Botu Oluştur" öncesi karakter adı, modu, seçilen ilişki tipi, baskın duygu ve hassas konu sayısını gösteren 3 satırlık zarif Mor Cam ön izleme şeridi ekle.

---

## 🛠️ BÖLÜM 4: ETKİLENECEK DOSYALAR VE KOD TEMİZLİĞİ

1. `app/src/main/java/com/example/data/repository/BaselineEmotion.kt`
   * Türkçe kesme işareti güvenli JSON parser (`.replace("'", "\"")` kaldırılır).
   * `"fearful_avoidant"` kanonik hizalaması.
   * `BaselineAnalysisResult.toCanonicalJson()` serileştiricisi.
   * Evren modu (`mode == "universe"`) atmosfer/mekan analizi desteği.
   * Kelime sınırı ve Türkçe sıfat false-positive iyileştirmeleri.
2. `app/src/main/java/com/example/data/repository/EmochiRepository.kt`
   * `runBaselineAnalysis()` içinde `X-Emochi-Call-Type: utility` + `resolveUtilityModel()` + genişletilmiş input hash + Evren modu `worldAtmosphere` dolumu.
   * `generateOpeningMessage(bot, forceRegenerate, toneHint)` düzeltmesi.
3. `app/src/main/java/com/example/ui/viewmodel/EmochiViewModel.kt`
   * `createBotFromWizard(bot, triggers, onComplete)` kesin tetikleyici ve `keyCharacters` -> `CastMemberEntity` aynalama kaydı.
   * Çift/ölü baseline analiz state'lerinin temizlenmesi ve coroutine iptal desteği.
4. `app/src/main/java/com/example/MainActivity.kt`
   * `CreateBotWizardScreen` `onFinish` callback'ini `(newBot, triggers)` alacak şekilde bağla.
5. `app/src/main/java/com/example/ui/screens/CreateBotWizardScreen.kt` & `CreateBotWizardComponents.kt`
   * Sabit `draftBotId`, `rememberSaveable`, çıkış onay diyaloğu, Evren modu avatar/kurallar, `pinnedMemory` alanı, Adım 5 tam eksen/gerekçe/serileştirme bağlantısı, Adım 6 tarih doğrulama + OOC/NSFW + son özet kartı.
   * Deprecated `Icons.Filled.MenuBook` -> `Icons.AutoMirrored.Filled.MenuBook` ve kullanılmayan importların temizlenmesi.

---

## 🧪 BÖLÜM 5: ZORUNLU TEST PAKETİ VE 10/10 DOĞRULAMA LİSTESİ

Aşağıdaki **6 birim testini** `MemoryEmotionRegressionTest.kt` (veya yeni `WizardBaselineEmotionTest.kt`) içine ekleyip tamamının geçtiğini doğrula:

1. **Türkçe Kesme İşareti JSON Testi:** LLM yanıtı içinde `"relationshipNote": "Aiden'ın İstanbul'daki geçmişi yüzünden temkinli"`, `"topic": "Aiden'ın ailesi"` gibi Türkçe tek tırnaklar varken `BaselineEmotionEngine.parseAnalysisJson()` metodunun fallback'e düşmeden **`analysisSource == "llm"`** olarak tam ve hatasız ayrıştırdığını kanıtla.
2. **Round-Trip Kullanıcı Düzenleme Testi:** `BaselineAnalysisResult` üzerinde `relationshipType`, `bigFive`, `attachmentStyle = "fearful_avoidant"`, `reviewAxes` ve `sensitiveTriggers` değiştirilip `toCanonicalJson()` ile kaydedildikten sonra `parseAnalysisJson()` ile okunduğunda **sıfır veri kaybı** olduğunu kanıtla.
3. **Sabit `draftBotId` & Tetikleyici Eşleşme Testi:** Sihirbaz tamamlandığında kaydedilen `BotEntity.id` ile `sensitiveTriggerDao.getTriggersForBot(bot.id)` kayıtlarının birebir aynı `botId` altında ve 3 adet olarak saklandığını doğrula.
4. **`generateOpeningMessage(forceRegenerate = true)` Testi:** `openingMessage` doluyken `forceRegenerate = true` ile çağrıldığında eski metni döndürmeyip gerçekten LLM'den yeni açılış ürettiğini mock interceptor ile kanıtla.
5. **Input Hash Önbellek Testi:** Aynı karakter girdileriyle `runBaselineAnalysis()` 2. kez çağrıldığında ağ isteği atmadan önbellekten döndüğünü (`0 network call`); senaryo değiştirildiğinde ise yeni analiz yaptığını kanıtla.
6. **Bağlanma Stili Kanonik Testi:** `"disorganized"`, `"korkulu"`, `"fearful_avoidant"` değerlerinin tamamının `"fearful_avoidant"` olarak normalize edilip `EmochiRepository` duygu çarpanlarıyla birebir eşleştiğini doğrula.

### ✅ Teslimat Öncesi Kontrol Listesi
1. **[ ] Sabit `draftBotId` ile tetikleyiciler eksiksiz kaydediliyor mu?** Yetim trigger kalmıyor mu?
2. **[ ] Adım 5'teki TÜM düzenlemeler (Big Five, İlişki Eksenleri, Bağlanma Stili, Hassas Konular) kaydedilip sohbete yansıyor mu?**
3. **[ ] Türkçe kesme işaretli LLM JSON çıktıları hiç bozulmadan ayrıştırılıyor mu?**
4. **[ ] Adım 4'te "Alternatif Açılış Üret" birden fazla kez sorunsuz yeni sahne üretiyor mu?**
5. **[ ] Evren modunda kapak/avatar, evren kuralları, `pinnedMemory` ve atmosfer analizi çalışıyor mu?**
6. **[ ] Kazara geri çıkışta taslak koruması, tarih format doğrulaması ve `isPublic = false` varsayılanı aktif mi?**
7. **[ ] `./gradlew testDebugUnitTest` ve `./gradlew assembleDebug` sıfır hata ve sıfır deprecation uyarısıyla geçiyor mu?**