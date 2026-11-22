# 🏆 EMOCHI (ASRODA) — 9.0 & 8.5 → 10/10 FİNAL MÜKEMMELLEŞTİRME ŞARTNAMESİ
**Kapsanan 4 Sistem:** `#8 Emochi Roman AI Prompt Mimarisi (9.0)` · `#10 Sohbet Ekranı, Rewind & Klonlama (9.0)` · `#5 Etkileşimli Kitap Motoru & Müzik (8.5)` · `#18 İlişki Kilometre Taşları (8.5)`

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Bu 4 sistem `Asroda-main` kod tabanında zaten yüksek olgunluk seviyesindedir (`8.5–9.0 / 10`). Görevin, mevcut çalışan yapıyı bozmadan her bir sistemde kalan son mimari eksikleri, ölü kod kopukluklarını, monolitik dosya şişkinliğini ve yaşam döngüsü açıklarını cerrahi hassasiyetle kapatarak dördünü birden **kusursuz 10/10** üretim kalitesine çıkarmaktır.
>
> **Kırmızı Çizgiler:**
> 1. **Kısaltma & Regresyon Yasağı:** `// ...`, yarım metod, ölü sınıf veya mevcut 10 test dosyasını kıran değişiklik **KESİNLİKLE YASAKTIR**.
> 2. **Domino Etkisi Koruması:** Kozmik-gotik Mor Cam UI tasarım dili (`#090414`, buzlu cam, `DstIn` üst/alt gradyan maskesi, üst çerçeve `✦` yıldızı, alt ışık yansıması, Serif tipografi), `AvatarOptimizer` Micro-WebP (`256×256`, `WEBP_LOSSY %82`, ~15 KB), Coil limitleri (`%18 RAM / 35 MB disk`, `Size.ORIGINAL` yasağı) ve Emochi Roman AI'nın ahlak bekçiliği/godmodding yasakları **asla bozulmayacaktır**.
> 3. **Hikâye Zamanı Kuralı:** Zaman gerçek duvar saatine göre değil, yalnızca `bot.storyDayCounter` ve `bot.storyCalendarDate` (**hikâye takvimi**) üzerinden akar.
> 4. **Room v48 Şema Güvenliği:** Mevcut veritabanı tablolarını kırmadan ilerle; yeni sütun eklenmesi gerekiyorsa eksiksiz `Migration(48, 49)` + `schemas/.../49.json` çıktısı üret, aksi halde mevcut alanlarla ve `AppFeatureFlags` ile geriye dönük uyumlu çalış.

---

## 🧠 SİSTEM #8: EMOCHI ROMAN AI PROMPT MİMARİSİ (9.0 → 10/10)

### 🔍 Mevcut 1.0 Puanlık Boşluklar
1. **Eksik `[[STATE_JSON]]` Sözleşmesi (`SystemPromptBuilder.kt`):** Prompt mevcut durumda temel duygu alanlarını istiyor ancak arka planda ayrıştırıcısı hazır olan **`story_time_skip`**, **yan karakter duygu güncellemeleri**, **esnek duygusal olgunlaşma (`reapp`)** ve **hikâye dönüm noktası (`mile`)** alanlarını modele öğretmiyor. Bu yüzden arka plan motorları modelden bu sinyalleri hiç alamıyor.
2. **Aktif Bağlam Bütçesi & Öncelik Koruması:** Yeni eklenen kısa direktiflerin 32K bağlamlı küçük/hızlı modellerde promptu şişirmemesi için katı tek-satırlık kompakt şema ve dinamik kırpma sırası kilitlenmeli.
3. **Kısmi / Bozuk Etiket Sızıntı Riski:** Genişletilen `[[STATE_JSON ...]]` bloğu veya modelin kesik bıraktığı son satırlar kullanıcıya asla görünmemeli.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Tek Satırlık Birleşik & Opsiyonel `[[STATE_JSON]]` Sözleşmesi (`SystemPromptBuilder.kt`):**
   - P5 (Görünmez Durum Sözleşmesi) katmanını tek, net ve ultra-kompakt bir blokta birleştir (sıfır ekstra çağrı, minimum token!):
     ```text
     [GİZLİ DURUM BLOĞU — KULLANICIYA ASLA BAHSETME]:
     Yanıtının en son satırına yalnızca şu tek satırlık JSON'ı ekle (değişmeyen opsiyonel alanları HİÇ yazma):
     [[STATE_JSON {"dom":"<baskın duygu>","int":<0-100>,"aff":<-10..+10>,"trs":<-10..+10>,"skip":<opsiyonel hikaye günü atlama: ör 1 veya "sabah->gece">,"cast_emo":<opsiyonel [{"n":"İsim","d":"duygu","v":-5..+5}]>,"trig":<opsiyonel ["tetiklenen konu"]>,"reapp":<opsiyonel {"id":12,"out":"softened|resolved|hardened"}>,"mile":<opsiyonel {"k":"benzersiz_anahtar","t":"Kısa Başlık","d":"Tek cümlelik dönüm noktası"}>}]]
     ```
   - **Kritik Token Kuralı:** Modelin her turda gereksiz JSON şişirmemesi için `"skip"`, `"cast_emo"`, `"trig"`, `"reapp"` ve `"mile"` alanlarının **yalnızca sahnede gerçekten gerçekleştiğinde** ekleneceğini açıkça belirt.
2. **Koşullu Akıllı Enjeksiyon (Yalnızca Gerekliyse Prompt'a Girer):**
   - `activeInSceneCast` boşsa `cast_emo` örneğini/talimatını hiç ekleme (token tasarrufu).
   - Aktif `pendingReappraisals` yoksa `reapp` talimatını hiç ekleme; varsa yalnızca en güncel 1–2 kaydı esnek AI karar notuyla ver (katı mesaj/gün kilidi olmadan).
   - Aktif `recentMilestones` (son 3 dönüm noktası) varsa P2/P4 içine tek satırda `[İLİŞKİ DÖNÜM NOKTALARI]: ...` olarak ekle ki karakter geçmiş büyük anıları sahnede yaşatsın.
   - `literaryFramingMode == true` (`retryWithSoftenedPrompt`) durumunda edebi kurgu/roman sanat çerçevesini koruyup tonu bozmadan yeniden gönderimi doğrula.
3. **Sıfır Sızıntı Temizleyici (`EmochiRepository.cleanEmotionTags`):**
   - Hem tam `[[STATE_JSON {...}]]` hem de akışta/max-token'da yarım kesilmiş `[[STATE_JSON ...` bloklarını, eski `[CHARACTER_EMOTION: ...]` / `[ACTIVE_MEMORY_CALL: ...]` etiketlerini ve `<think>...</think>` bloklarını kullanıcıya görünmeden **%100 temizle**.
   - Parser (`parseAndApplyEmotionUpdates`), JSON içindeki tüm yeni opsiyonel alanları (`skip`, `cast_emo`, `trig`, `reapp`, `mile`) hata-dayanıklı (`runCatching`) biçimde ayrıştırıp ilgili motorlara (`TimePerceptionEngine`, `CastMemberManager`, `MilestoneDao`) tek seferde iletsin.

---

## 💬 SİSTEM #10: SOHBET EKRANI, GERİ SARMA (REWIND) & KLONLAMA (9.0 → 10/10)

### 🔍 Mevcut 1.0 Puanlık Boşluklar
1. **Hâlâ Monolitik `ChatScreen.kt` (243 KB):** `ChatTopBar.kt`, `ChatMessageBubbles.kt`, `ChatInputDock.kt` ve `ChatOverlays.kt` dosyaları oluşturulmuş ancak `ChatScreen.kt` hâlâ ~243 KB büyüklüğünde dev bir tek dosya olarak duruyor (bakım, derleme ve recomposition kapsamı açısından riskli).
2. **Eksik Rewind Temizliği (`pending_reappraisals` & `character_emotions`):** `rewindToMessage()` çoğu tabloyu geri sarıyor ancak hedef mesajdan sonra oluşturulmuş veya çözülmüş `pending_reappraisals` kayıtlarını ve hedef sonrasındaki yan karakter duygu değişimlerini/önbelleklerini eksiksiz geri almıyor.
3. **Klonlama İzolasyonu:** `cloneBot()` sonrasında yeni botun eski botla hiçbir çalışma zamanı önbelleğini (`lastSummarizedMessageCount`, `regenerateCountMap`, `MemoryVectorCache`) ve eski `milestones`/`pending_reappraisals` kalıntılarını paylaşmaması kilitlenmeli.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **`ChatScreen.kt` Gerçek Modüler Ayrıştırma (Sıfır Görsel Değişiklik Garantisi):**
   - `ChatScreen.kt` içindeki tekrarlı/dev UI bloklarını ilgili mevcut dosyalara **gerçekten taşıyıp delege et**:
     * `ChatTopBar.kt`: Üst Mor Cam başlık barı, avatar kapsülü (tıklanınca `BotQuickProfileSheet`), arama çubuğu (`1 / N` sayaç + `↑` / `↓` oklar + `⭐ Yıldızlılar`), TTS aktif barı.
     * `ChatMessageBubbles.kt`: Kullanıcı & bot mesaj balonları, **son 5 mesaj kuralı (`orderFromBottom > 5`)**, arama kelimesi neon lavanta vurgusu (`SpanStyle`), mesaj bazlı `CosmicVoiceEqualizer` (`speakingMessageId == msg.id`) ve yalnızca aktif balonda toplanan izole streaming akışı.
     * `ChatInputDock.kt`: Alt giriş kapsülü, `(+)` araç çekmecesi, durdur/gönder butonları, akıllı **3+ mesaj "En Aşağı İn" (`itemsFromEnd >= 3`)** kapsülü.
     * `ChatOverlays.kt`: Hızlı Profil, Yıldızlanan Mesajlar (tıklayınca ilgili mesaja atlama + `visibleMessageLimit` otomatik genişletme), Rewind onay diyaloğu ve Kilometre Taşı (Milestone) bildirim banner'ı.
   - `ChatScreen.kt` kendisi yalnızca state bağlama, `LazyColumn` yerleşimi, `DstIn` gradyan maskesi ve bileşen koordinasyonunu yapan temiz bir **orchestrator (<700 satır)** hâline gelsin. Hiçbir görsel piksel, renk, boşluk veya animasyon değişmeyecek!
2. **Kusursuz Tek-Transaction `rewindToMessage()` (`EmochiRepository.kt`):**
   - `db.withTransaction` içinde hedef mesaj index'i (`targetIdx`) ve zaman damgasından (`targetTimestamp`) sonraki **tüm** türetilmiş verileri eksiksiz geri sar:
     * `messages`, `memory_facts`, `memory_events`, `memory_checkpoints`, `archived_memories`
     * `milestones` (`createdAtMessageIndex > targetIdx` veya `timestamp > targetTimestamp`)
     * `entity_relations` (`createdAtMessageIndex > targetIdx`)
     * **`pending_reappraisals` (`createdAtMessageIndex > targetIdx` olanları sil; `createdAtMessageIndex <= targetIdx` olup sonradan çözülmüş/bayatlamış olanları tekrar `resolved = false, expired = false` durumuna al!)**
     * `routines`: `lastMentionedStoryDate` hedef hikâye tarihinden büyükse temizle.
     * `bot` durumu: `emotionStateJson`, `storyDayCounter`, `storyCalendarDate`, `worldStateJson` değerlerini hedefteki snapshot'a (veya güvenli en yakın checkpoint'e) geri döndür.
   - Transaction sonrasında tek merkezli `invalidateMemoryCaches(botId)` + `lastSummarizedMessageCount.remove(botId)` + `resetRegenerateCount(botId)` çağır.
3. **Tertemiz `cloneBot()` İzolasyonu (`EmochiRepository.kt`):**
   - `storyCalendarDate` değerini `if (original.hasExplicitStoryDate) original.storyCalendarDate else ""` olarak koru (hardcoded tarih yasağı).
   - `sensitive_triggers` kayıtlarını `recentTriggerCount = 0` ile yeni bota kopyala; `getStoredBaselineResult(original)` profilini başlangıç `emotionStateJson` olarak ata.
   - Eski botun `milestones`, `pending_reappraisals`, `messages` ve çalışma zamanı önbelleklerinin **hiçbirini** yeni bota bulaştırma.

---

## 📖 SİSTEM #5: ETKİLEŞİMLİ KİTAP MOTORU & BÖLÜM SONU MÜZİĞİ (8.5 → 10/10)

### 🔍 Mevcut 1.5 Puanlık Boşluklar
1. **Ölü `BookChoiceParser.kt`:** `BookChoiceParser.kt` projede duruyor ancak `BookReaderView.kt` ve `BookReaderComponents.kt` tarafından gerçek seçim akışında kullanılmıyor.
2. **Müzik Yaşam Döngüsü & TTS Çakışma Riski (`BookAudioController.kt`):** Uygulama arka plana atıldığında (`ON_PAUSE` / ekran kilidi) bölüm sonu müziğinin yumuşak fade-out ile duraklatılıp geri dönünce (kullanıcı sessize almadıysa) devam etmesi ve `ChatTtsManager` ile karşılıklı dışlama (mutual exclusion) tam garanti altına alınmalı.
3. **İçerik Paketi & Görsel Bellek Dayanıklılığı (`BookContentPackManager.kt`, `BookCinematicMedia.kt`):** Eksik/bozuk harici içerik paketi veya hızlı bölüm geçişlerinde `MediaPlayer`/Coil kaynak sızıntısı ve OOM riski tamamen kapatılmalı.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **`BookChoiceParser.kt` Canlı Okuyucu Entegrasyonu (`BookReaderView.kt`, `BookReaderComponents.kt`):**
   - Okuyucudaki tüm seçim metni, koşul etiketi, sonuç rozeti ve dallanma ayrıştırmasını tek kaynak olarak `BookChoiceParser` üzerinden yürüt (ölü kod sıfır!).
   - `BookChoiceParser`'a şu yetenekleri temiz ve deterministik biçimde ekle/doğrula:
     * Seçim metninden gömülü ton/sonuç rozetlerini (`[Cesur]`, `[Gizli]`, `[Riskli]`, karakter ilişki etkisi ipucu) ayıklayıp Mor Cam seçim kartlarında zarif etiket olarak sunma.
     * Kilitli/koşullu dallar için okunabilir kilit nedeni (*"Bölüm 2'de Thor'a güvendin"* gibi) üretme ve bozuk/boş seçim satırlarında asla çökmeme (güvenli fallback).
2. **Kusursuz Ses Yaşam Döngüsü (`BookAudioController.kt` & `BookReaderView.kt`):**
   - `BookReaderView` içine `LocalLifecycleOwner` ile `LifecycleEventObserver` bağla:
     * `ON_PAUSE`: Aktif müzik çalıyorsa `pauseForBackground()` ile 250 ms yumuşak fade-out yapıp duraklat (kullanıcının manuel `isMuted` tercihini bozmadan!).
     * `ON_RESUME`: Yalnızca arka plan nedeniyle duraklatıldıysa ve `isMuted == false` ise 300 ms yumuşak fade-in ile devam et.
     * `DisposableEffect.onDispose` (okuyucudan çıkıldığında): `stopAndRelease()` ile `MediaPlayer` + `AudioFocusRequest` kaynaklarını anında serbest bırak.
   - `ChatTtsManager` konuşmaya başladığında `BookAudioController.pauseIfPlaying()` çağır; kitap müziği başladığında aktif `ChatTtsManager.stop()` tetiklensin.
   - `MediaPlayer.setOnErrorListener` içinde hata yakalandığında kaynakları güvenle temizleyip sessizce toparla (asla çökme/takılma yok).
3. **Atomik İlerleme Kaydı & Sıfır OOM Sinematik Medya (`BookStateRepository.kt`, `BookContentPackManager.kt`, `BookCinematicMedia.kt`):**
   - Her seçim ve bölüm geçişinde `BookStateRepository` ilerleme, karar bayrakları ve ilişki puanlarını atomik yazsın; *"Bölümü Yeniden Oyna / Kitabı Sıfırla"* aksiyonu hiçbir sahipsiz bayrak bırakmasın.
   - `BookContentPackManager`: Harici paket yükleme/doğrulama başarısız olursa (bozuk dosya/checksum/IO hatası) uygulamayı çökertmeden temiz Türkçe durum mesajı verip yerleşik paketlere güvenle düşsün.
   - `BookCinematicMedia.kt`: Tüm sinematik sahne görsellerinde `ImageRequest.Builder(context).data(...).allowRgb565(true).crossfade(300).build()` standardını doğrula (`Size.ORIGINAL` kesinlikle yasak).

---

## 🏅 SİSTEM #18: İLİŞKİ KİLOMETRE TAŞLARI / MILESTONES (8.5 → 10/10)

### 🔍 Mevcut 1.5 Puanlık Boşluklar
1. **Sadece Sayısal Eşiklere Bağımlılık:** Mevcut `checkAndSaveMilestone()` yalnızca `affection` ve `trust` puanlarının sabit sayıları geçmesine bakıyor. Oysa hikâyede büyük bir dönüm noktası (*"İlk Ortak Sır"*, *"Hayatını Kurtardın"*, *"Büyük İhanet"*, *"İlk Öpücük"*, *"Zor Barışma"*) puan yuvarlak bir eşiği geçmese bile yaşanabilir.
2. **Hikâye Zamanı & Prompt Geri Bildirimi Eksikliği:** Kazanılan kilometre taşları yalnızca gerçek dünya zamanıyla kaydediliyor; hikâye günü (`storyDayCounter` / `storyCalendarDate`) bağlamı zayıf ve kazanılan dönüm noktaları karaktere hatırlatılmadığı için AI sonraki sahnelerde bunu unutabiliyor.
3. **Mükerrer Tetikleme & Rewind Temizliği:** Aynı dönüm noktasının puan inip çıktıkça (ör. güven 50 -> 48 -> 51) tekrar tekrar kazanılması engellenmeli ve geri sarma (`rewindToMessage`) sonrası gelecekten kalma milestone kalmamalı.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Hibrit (Sayısal + AI Anlatı) Kilometre Taşı Motoru (`EmochiRepository.kt`, `MilestoneDao.kt`, `MilestoneEntity.kt`):**
   - **Kanal 1 — Sayısal İlişki Eşikleri (Garantili Çekirdek):** `trust` (`25, 50, 75, 95`) ve `affection` (`30, 60, 85, 95`) eşikleri deterministik çalışmaya devam etsin.
   - **Kanal 2 — AI Anlatı Dönüm Noktaları (0 Ekstra API Çağrısı!):** Sistem #8'deki `[[STATE_JSON]]` içinden opsiyonel `"mile": {"k":"...", "t":"...", "d":"..."}` geldiğinde bunu doğrudan gerçek bir hikâye kilometre taşı olarak kaydet!
   - **Benzersizlik & Anti-Spam Kilidi:**
     * Her kilometre taşına normalize edilmiş benzersiz bir `milestoneKey` (ör. `trust_50`, `affection_85`, `story_first_secret`) ata; aynı `botId` için o anahtar zaten DB'de varsa (puan düşüp tekrar çıksa bile!) **asla ikinci kez ekleme**.
     * Tek bir sohbet turunda kullanıcıyı boğmamak için en fazla **1** yeni kilometre taşı kutlama banner'ı göster (aynı turda hem sayısal hem anlatı tetiklenirse anlatı/özel olana öncelik ver, diğerini sessizce kaydet).
2. **Hikâye Zamanı Damgası & Canlı Prompt Döngüsü:**
   - Her kilometre taşının açıklamasına/meta verisine hikâye takvimi bilgisini (ör. `Gün 4 • 12 Kasım` — `bot.storyDayCounter` / `bot.storyCalendarDate`) işle (Room şemasını bozmadan mevcut alanlarla veya temiz migrasyonla).
   - `SystemPromptBuilder` içine en önemli/en güncel **3 kilometre taşını** tek kompakt satırda ver:
     `[İLİŞKİ DÖNÜM NOKTALARI]: Gün 2: İlk Ortak Sır; Gün 5: Güven Eşiği (50); Gün 8: Büyük Barışma.`
3. **Mor Cam Banner & Dönüm Noktası Zaman Çizelgesi (`ChatOverlays.kt` & `BotSettingsModal.kt`):**
   - Yeni kilometre taşı açıldığında `ChatScreen` üstünde sohbeti kesmeyen zarif Mor Cam altın/lavanta parıltılı banner göster (`4.5 sn` sonra otomatik kaybolur, dokunulursa kapanır veya Dönüm Noktaları listesini açar).
   - `BotQuickProfileSheet` ve `BotSettingsModal` içinde karakterin kazanılmış tüm kilometre taşlarını **kronolojik hikâye zaman çizelgesi** olarak listele (sayısal eşikler ile AI anlatı dönüm noktalarını zarif ikonlarla ayırarak).
   - `rewindToMessage()` ve `BackupSnapshotManager` v2 içinde yeni anlatı kilometre taşlarının %100 geri sarıldığını ve yedeklendiğini doğrula.

---

## 📁 ETKİLENECEK DOSYALAR HARİTASI

1. `app/src/main/java/com/example/data/repository/SystemPromptBuilder.kt` — Birleşik opsiyonel `[[STATE_JSON]]` sözleşmesi (`skip`, `cast_emo`, `trig`, `reapp`, `mile`), koşullu token tasarrufu ve `[İLİŞKİ DÖNÜM NOKTALARI]` bloğu.
2. `app/src/main/java/com/example/data/repository/EmochiRepository.kt` — Genişletilmiş `parseAndApplyEmotionUpdates()` & `cleanEmotionTags()`, hibrit Milestone kaydı + deduplication, eksiksiz `rewindToMessage()` (`pending_reappraisals` + `milestones` + önbellekler) ve izole `cloneBot()`.
3. `app/src/main/java/com/example/ui/screens/ChatScreen.kt`, `ChatTopBar.kt`, `ChatMessageBubbles.kt`, `ChatInputDock.kt`, `ChatOverlays.kt` — 243 KB monolitin gerçek modüler ayrıştırılması (sıfır görsel değişiklik), son 5 mesaj & 3+ kaydırma kuralları, Milestone banner.
4. `app/src/main/java/com/example/util/BookChoiceParser.kt`, `BookReaderView.kt`, `BookReaderComponents.kt` — `BookChoiceParser` tam canlı entegrasyonu, rozet/kilit ayrıştırma.
5. `app/src/main/java/com/example/util/BookAudioController.kt`, `BookStateRepository.kt`, `BookContentPackManager.kt`, `BookCinematicMedia.kt` — `ON_PAUSE`/`ON_RESUME` fade lifecycle, `ChatTtsManager` ses çakışma koruması, atomik kitap ilerlemesi, `RGB_565` medya standardı.
6. `app/src/main/java/com/example/data/local/MilestoneDao.kt`, `MilestoneEntity.kt` & `app/src/main/java/com/example/ui/components/BotSettingsModal.kt` — Anahtar bazlı deduplication, rewind silme sorgusu, kronolojik hikâye zaman çizelgesi görünümü.

---

## 🧪 ZORUNLU TEST PAKETİ (`Systems5_8_10_18_TenOutOfTenTest.kt`)

Aşağıdaki **8 birim testini** ekleyip mevcut tüm test dosyalarıyla birlikte eksiksiz geçmesini doğrula:
1. **#8 Birleşik `[[STATE_JSON]]` & Koşullu Enjeksiyon Testi:** Aktif yan karakter ve `pendingReappraisals` yokken promptta gereksiz talimatların basılmadığını; varken `skip`, `cast_emo`, `reapp`, `mile` sözleşmesinin eklendiğini doğrula.
2. **#8 Sıfır Sızıntı & Kısmi Etiket Temizlik Testi:** Hem tam hem de ortadan kesilmiş `[[STATE_JSON {"dom":"kızgın","mile":...` ve `<think>...</think>` metinlerinin `cleanEmotionTags()` sonrasında kullanıcı metninde **0 karakter** bıraktığını kanıtla.
3. **#10 Rewind Tam Geri Sarma Testi (`pending_reappraisals` + `milestones` + `entity_relations`):** 10 mesajlık sohbette 4. mesaja geri sarıldığında 5–10 arasındaki `pending_reappraisals` ve `milestones` kayıtlarının silindiğini, 3. mesajda oluşup 7. mesajda çözülmüş bir reappraisal'ın tekrar `resolved = false` durumuna döndüğünü ve önbelleklerin temizlendiğini kanıtla.
4. **#10 Klonlama İzolasyon Testi:** Klonlanan botun eski `milestones` ve `pending_reappraisals` kayıtlarını devralmadığını, `sensitive_triggers` sayaçlarının `0` olduğunu ve hardcoded tarih taşımadığını doğrula.
5. **#5 `BookChoiceParser` Ayrıştırma & Dayanıklılık Testi:** Rozetli (`[Cesur]`), koşullu/kilitli ve bozuk/eksik biçimli kitap seçim metinlerinin `BookChoiceParser` tarafından hatasız ayrıştırıldığını doğrula.
6. **#5 Kitap Müziği Lifecycle & TTS Çakışma Testi:** `ON_PAUSE` -> arka plan duraklatma -> `ON_RESUME` yalnızca manuel sessiz değilse devam etme; TTS başladığında kitap müziğinin durakladığını doğrula.
7. **#18 Hibrit (Sayısal + AI Anlatı) Milestone & Deduplication Testi:** `trust` 50 -> 45 -> 52 olduğunda `trust_50` milestone'ının yalnızca **1 kez** kaydedildiğini; `[[STATE_JSON]]` içinden gelen `"mile": {"k":"first_secret",...}` anlatı dönüm noktasının hikâye günüyle kaydedilip tekrar geldiğinde çiftlenmediğini doğrula.
8. **#18 Milestone Prompt Geri Bildirimi & Rewind Testi:** Kazanılmış son 3 kilometre taşının `SystemPromptBuilder` çıktısında `[İLİŞKİ DÖNÜM NOKTALARI]` olarak göründüğünü ve rewind sonrası silinen taşın prompttan çıktığını doğrula.

### ✅ Teslimat Öncesi 10/10 Doğrulama Kontrol Listesi
1. **[ ] #8:** `SystemPromptBuilder` tek satırlık opsiyonel `[[STATE_JSON]]` sözleşmesiyle (`skip`, `cast_emo`, `trig`, `reapp`, `mile`) çalışıyor, boş yere token harcamıyor ve sıfır etiket sızıntısı veriyor mu?
2. **[ ] #10:** `ChatScreen.kt` gerçekten parçalanıp `<700` satırlık temiz orchestrator oldu mu (sıfır görsel değişiklikle), `rewindToMessage` `pending_reappraisals` dahil her tabloyu eksiksiz geri sarıyor mu?
3. **[ ] #5:** `BookChoiceParser` okuyucuda canlı kullanılıyor, `BookAudioController` arka plan geçişinde fade-out/fade-in ve TTS ile karşılıklı dışlama yapıyor mu?
4. **[ ] #18:** Hem sayısal eşikler hem AI anlatı dönüm noktaları hikâye takvimi damgasıyla tekilleştirilerek kaydediliyor, prompta geri besleniyor ve zaman çizelgesinde görünüyor mu?
5. **[ ] Derleme & Tüm Testler:** `./gradlew testDebugUnitTest` ve `./gradlew assembleDebug` sıfır hata ve sıfır yeni uyarıyla geçiyor mu?