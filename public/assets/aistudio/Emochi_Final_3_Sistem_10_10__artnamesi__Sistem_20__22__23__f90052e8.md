# 🏆 EMOCHI (ASRODA) — FİNAL 3 SİSTEM 10/10 ŞARTNAMESİ
**Kapsanan Son 3 Aktif Sistem:** `#20 Bilgi Grafiği / Knowledge Graph (6.5 → 10)` · `#22 Hassas Tetikleyiciler & Duygusal Olgunlaşma (5.0 → 10)` · `#23 Gerçek Zamanlı Akış / SSE Streaming (3.0 → 10)`

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Bu 3 sistem `Asroda-main` kod tabanında **7.5 puanın altında kalan son aktif çekirdek sistemlerdir**. Görevin, ölü/kopuk duran bu üç motoru daha önce kurduğumuz `[[STATE_JSON]]` sözleşmesi, `ProviderFallbackManager`, `ProviderHealthTracker`, `MemoryRetrievalManager` ve `ChatMessageBubbles` mimarisiyle **%100 uyumlu** biçimde canlı sohbet döngüsüne bağlayıp tamamını **kusursuz 10/10** seviyesine çıkarmaktır. *(Not: `#24 Güncelleme / Admin` şimdilik ertelenmiştir — ona dokunma.)*
>
> **Kırmızı Çizgiler:**
> 1. **Kısaltma & Regresyon Yasağı:** `// ...`, yarım metod, sahte `delay()` daktilo hilesi veya mevcut test dosyalarını kıran değişiklik **KESİNLİKLE YASAKTIR**.
> 2. **Domino Etkisi & Varlık Koruması:** Mor Cam kozmik-gotik UI tasarım dili (`#090414`, buzlu cam, `DstIn` üst/alt gradyan maskesi, Serif tipografi), `drawable-nodpi/` altındaki mevcut tüm `.webp` varlıkları (kullanılmayanlar dahil **hiçbiri silinmeyecek!**), `AvatarOptimizer` Micro-WebP (`256×256`, `WEBP_LOSSY %82`), Coil limitleri (`%18 RAM / 35 MB disk`) ve Room v48 şema uyumluluğu **kesinlikle korunacaktır**.
> 3. **Hikâye Zamanı & API 24+ Kuralı:** Tüm duygusal olgunlaşma, soğuma ve bayatlama hesapları gerçek duvar saatine değil, `bot.storyDayCounter` / `bot.storyCalendarDate` (**hikâye takvimi**) üzerine kurulur; `minSdk = 24` olduğu için `java.time.LocalDate` kullanılmaz.
> 4. **Önceki 10/10 Kurallarla Tam Uyum:**
>    - `CancellationException` asla `ProviderHealthTracker` cezası yazmaz.
>    - `AppFeatureFlags.isStrictApiTimeoutEnabled` kapalıyken SSE okuma zaman aşımı kesintisi yapılmaz; açıkken makul TTFB/akış zaman aşımı uygulanır.
>    - `refusal_auto_fallback` (sansür/vaaz koruması) SSE akışı sırasında da güvenli çalışır.
>    - Yeni ayar bayrakları Room şemasını bozmadan yalnızca `AppFeatureFlags` üzerinden saklanır.

---

## 🕸️ SİSTEM #20: BİLGİ GRAFİĞİ / KNOWLEDGE GRAPH (6.5 → 10/10)

### 🔍 Mevcut 3.5 Puanlık Boşluklar
1. **Canlı Sohbet Kopukluğu:** `WorldStateManager` içinde çok adımlı zincir çözücü (`traverseRelationGraph`) mevcut ama `EmochiRepository.detectAndResolveGraphQuery()` canlı sohbet promptu hazırlanırken **çağrılmıyor**. Bu yüzden kullanıcı *"Aiden'ın kardeşinin eski patronu kimdi?"* gibi 2–3 adımlı ilişki sorusu sorduğunda model grafiği hiç görmüyor.
2. **Döngü (Cycle), Türkçe Ek & Eş Anlamlı Körlüğü:** `"Elif'in ablası"`, `"Aiden'ın kılıcı"` gibi Türkçe iyelik ekli varlıklar graf düğümleriyle tam eşleşmeyebiliyor; ayrıca karşılıklı ilişkilerde (`A -> arkadaşı -> B` ve `B -> arkadaşı -> A`) sonsuz döngü koruması ve derinlik sınırı kesin kilitlenmeli.
3. **Çelişki Güncellemesi & Canlı Öğrenme Eksikliği:** Bir ilişki hikâyede değiştiğinde (ör. `Aiden -> sevgilisi -> Elif` iken sonradan `eski sevgilisi` veya `düşmanı` olduğunda) eski çelişen kenarın pasife alınması/güncellenmesi ve yeni ilişkilerin Ufak AI / küratör akışından otomatik eklenmesi gerekiyor.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Canlı Çok Adımlı Grafik Çözücü (`WorldStateManager.kt` & `EmochiRepository.kt`):**
   - Her kullanıcı mesajında `MemoryRetrievalManager` çağrısının hemen öncesinde `<2 ms` süren yerel **`detectAndResolveGraphQuery(botId, userQuery)`** fonksiyonunu canlı olarak çalıştır:
     * Türkçe çekim/iyelik eki temizleyici (`normalizeGraphEntity`: `"Elif'in"` -> `"elif"`, `"Aiden'la"` -> `"aiden"`) ve `CastMemberEntity.aliases` eşleşmesiyle başlangıç düğümünü bul.
     * Türkçe ilişki niyeti çıkarımı (ör. `kardeşi`, `ablası`, `babası`, `sevgilisi`, `patronu`, `düşmanı`, `arkadaşı`, `yaşadığı yer`, `sahip olduğu`, `kim`, `nerede`, `ne ilişkisi var`).
     * **BFS / Güvenli Çok Adımlı Gezinti (`traverseRelationGraph`):** Maksimum `depth = 4`, ziyaret edilmiş düğüm kümesi (`visited: MutableSet<String>`) ile **%100 döngü korumalı**, hem tam zincirleri (`Aiden --[kardeşi]--> Mira --[patronu]--> Vaelor`) hem de kısmi 1–2 adımlı ilişkileri okunaklı Türkçe cümleyle döndür.
   - Çıkan `graphResolutionNote` metnini `MemoryRetrievalManager` üzerinden `SystemPromptBuilder` P4 hafıza bloğunun **en üstüne** `[İLİŞKİ AĞI GERÇEĞİ]` başlığıyla enjekte et (aynı olgu RAG/Pinned içinde varsa tekilleştir).
2. **Otomatik İlişki Çıkarımı & Çelişki Çözümü (0 Ekstra Maliyet / Küratör + Ufak AI):**
   - Mevcut tur sonu hafıza küratörü (`extractAndCommitTurnMemories` — `ExecutionMode.UTILITY`, `temperature = 0.15`) JSON şemasına opsiyonel `"relations": [{"s":"Özne","p":"ilişki","o":"Nesne"}]` alanını ekle.
   - Yeni bir `(s, p, o)` kenarı kaydedilirken (`upsertEntityRelation`):
     * Normalize `(s, p, o)` zaten varsa çift kayıt ekleme, yalnızca güven/son görülme indeksini güncelle.
     * Tekil (exclusive) veya çelişen bir ilişki geldiyse (`sevgilisi` <-> `eski sevgilisi`, `müttefiki` <-> `düşmanı`, `yaşadığı yer` değişimi) eski çelişen kaydı temizleyip/güncelleyip yeni gerçeği yaz (eski bilginin çelişmesini engelle).
3. **Rewind, Klonlama & Neural Vault Görünürlüğü:**
   - `rewindToMessage(targetIdx)` çalıştığında `createdAtMessageIndex > targetIdx` olan `entity_relations` kayıtlarının eksiksiz silindiğini ve önbelleğin yenilendiğini doğrula.
   - `NeuralVaultModal` içinde karakterin aktif Bilgi Grafiği ilişkilerini sade Mor Cam liste olarak görüntüleme ve kullanıcıya yanlış bir ilişkiyi tek tıkla silme imkânı sun.

---

## 💔 SİSTEM #22: HASSAS TETİKLEYİCİLER & DUYGUSAL OLGUNLAŞMA (5.0 → 10/10)

### 🔍 Mevcut 5.0 Puanlık Boşluklar
1. **Canlıda Hiç Çağrılmayan Tetikleyiciler:** `evaluateSensitiveTriggers()` kodda duruyor ama canlı sohbet akışında (`generateAiReply`) çağrılmıyor! Botun hassas yaralarına (`terk edilme`, `ihanet`, `yalan` vb.) dokunulduğunda karakter gerçek bir duygusal kırılma yaşamıyor.
2. **Yapay `+15 Mesaj / +3 Gün` Olgunlaşma Kilidi:** `PendingReappraisal` motoru sabit mesaj/gün kilidine hapsedilmiş ve `pendingReappraisalDao` kayıtları canlı sohbet promptuna geri okunmuyor. Gerçek bir insanda/romanda bir yara bazen 2 mesaj sonra içten bir özürle yumuşar, bazen 20 mesaj sonra bile kapanmaz!
3. **Yüzeysel Kelime Eşleşmesi:** Sadece birebir kelime eşleştirmesi yapan tetikleyici motoru, üstü kapalı imaları (*"Zaten herkes gibi sen de bir gün çekip gideceksin"*) kaçırabiliyor veya tam tersi zararsız cümlelerde (`"ihanet kelimesinin anlamı nedir?"`) yanlış tetiklenebiliyor.

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Hibrit Canlı Hassas Tetikleyici Değerlendirmesi (`EmochiRepository.kt`, `EmotionEngine.kt`, `SystemPromptBuilder.kt`):**
   - Her kullanıcı mesajında canlı olarak `evaluateSensitiveTriggers(bot, userMessage, recentHistory)` akışını çalıştır:
     * **Kanal 1 — Hızlı Yerel Türkçe Eşleştirici (`<1 ms`):** Botun `sensitive_triggers` tablosundaki anahtar kelimeleri Türkçe ek gövdeleme + olumsuzlama/bağlam korumasıyla tarar (ör. OOC notları `((...))` ve botun yarasına yönelik olmayan genel sohbet cümlelerini eler).
     * **Kanal 2 — Ufak AI / `[[STATE_JSON]]` Semantik Tetikleyici Doğrulama:**
       - `UserSentimentAnalyzer` veya tur içi Ufak AI çalışıyorsa ve yerel tarama belirsizse/dolaylı ima varsa tetikleyici listesiyle anlamsal eşleşme (`matchedTriggerId`, `severity`) döner.
       - Ek olarak ana model kendi yanıtında `[[STATE_JSON]]` içinden `"trig": ["terk edilme"]` döndürdüğünde bunu da yakalayıp bir sonraki tur için tetikleyici sayacına ve duygusal etkiye işler.
   - **Tetikleme Etkisi & Spam Soğuma (Cooldown):**
     * Gerçek bir tetikleme oluştuğunda botun `BaselineEmotion.triggerMultiplier` (`1.2f..2.5f`) çarpanıyla duygusal yoğunluk/savunma artar ve `SystemPromptBuilder` P2 bloğuna tek satırlık sahne içi yönerge girer:
       `[HASSAS YARA TETİKLENDİ]: "${trigger.topic}" yarasına dokunuldu (şiddet: yüksek). Karakterin bağlanma stiline (${attachmentStyle}) uygun gerçekçi tepki ver — abartılı tiyatro değil, içten savunma/kırılma.`
     * Aynı tetikleyici 2 mesaj içinde üst üste patlayıp duyguları bozmasın diye `recentTriggerCount` + hafif tur soğuması uygula; ayrıca her tetiklemede bir **`PendingReappraisalEntity`** kaydı oluştur (kullanıcıya karşı anlık kırılma -> sonraki turlarda içsel olgunlaşma adayı).
2. **Katı Kilitsiz, Hikâye Odaklı Esnek Duygusal Olgunlaşma (AI + Hibrit `PendingReappraisal`):**
   - **Eski `+15 mesaj / +3 gün` sabit kilidini TAMAMEN KALDIR!**
   - Bunun yerine **esnek bağlam odaklı olgunlaşma** kur:
     * Çözülmemiş (`resolved == false && expired == false`) en güncel **1–2** `PendingReappraisalEntity` kaydını `SystemPromptBuilder` P2/P4 içine kompakt biçimde ver:
       `[İÇSEL HESAPLAŞMA (AÇIK YARA #${r.id})]: "${r.eventSummary}" (hikâye günü farkı: ${deltaStoryDays} gün, ${deltaMessages} mesaj önce). Kullanıcı samimi telafi/empati gösterdiyse veya karakter kendi içinde olgunlaştıysa yumuşat ("softened") ya da çöz ("resolved"); yara yeniden deşildiyse sertleştir ("hardened"); henüz zamanı değilse dokunma.`
     * Ana model `[[STATE_JSON]]` içinde opsiyonel `"reapp": {"id":12, "out":"softened|resolved|hardened"}` döndürdüğünde:
       - **`softened`:** Anlık öfke/savunma yumuşar (`-10..-18` negatif yoğunluk), kayıt güncellenir.
       - **`resolved`:** `resolved = true` olarak kapanır, güven/yakınlıkta gerçekçi toparlanma (`+3..+6`) ve olgunlaşma anısı (`LivingMemory`) kaydedilir.
       - **`hardened`:** Savunma derinleşir, karakter bu yaraya karşı daha temkinli hâle gelir.
     * **Hikâye Zamanı Bayatlama (Doğal Unutma Koruması):** Bir `PendingReappraisalEntity` kaydı `14+` hikâye günü geçmesine rağmen hiç açılmadıysa (veya karakterin yüksek `emotionalRegulationCapacity` profiliyle doğal olarak soğuduysa) `expired = true` işaretlenip promptu sonsuza kadar işgal etmesi engellenir.
3. **Tam Rewind, Yedekleme & UI Şeffaflığı:**
   - `rewindToMessage(targetIdx)` sırasında hedef sonrasındaki yeni reappraisal kayıtları silinir; hedef öncesinde oluşup sonradan çözülenler tekrar `resolved = false, expired = false` durumuna geri alınır.
   - `EmotionStatusSection` ve `BotSettingsModal` içinde botun aktif hassas tetikleyicileri ile (varsa) açık duygusal olgunlaşma durumu Mor Cam rozetleriyle sade ve okunaklı görünür; kullanıcı isterse tetikleyici ekleyip silebilir.

---

## ⚡ SİSTEM #23: GERÇEK ZAMANLI AKIŞ / SSE STREAMING (3.0 → 10/10)

### 🔍 Mevcut 7.0 Puanlık Boşluklar
1. **Tamamen Ölü `SseStreamParser.kt`:** Dosya projede duruyor ama hiçbir yerden çağrılmıyor.
2. **Sahte `startTypewriter` İllüzyonu (`EmochiViewModel.kt`):** Kullanıcı şu an tüm yanıtın bitmesini saniyelerce boş ekranda bekliyor, yanıt tamamen inince `startTypewriter` yapay bir harf-harf animasyonu oynatıyor (gerçek akış değil, algılanan gecikme 5–15 sn!).
3. **Canlı Akışta Gizli Etiket Sızıntısı Riski:** Gerçek SSE açıldığında modelin yanıt sonunda ürettiği `[[STATE_JSON ...]]`, `[CHARACTER_EMOTION: ...]`, `[ACTIVE_MEMORY_CALL: ...]` veya reasoning modellerin `<think>...</think>` blokları canlı ekrana akarken kullanıcıya asla görünmemelidir!

### 🛠️ 10/10 Cerrahi Düzeltmeler
1. **Çoklu Format Gerçek `SseStreamParser.kt` (OpenAI-Compatible + Google Gemini SSE):**
   - `SseStreamParser.kt` sınıfını `BufferedSource` / `BufferedReader` satır akışı üzerinden sıfır bellek şişirmesiyle çalışacak şekilde tamamla:
     * **OpenAI / OpenRouter / Groq / DeepSeek / Together / Ollama SSE:** `data: {...}` satırlarını oku; `choices[0].delta.content` tokenlarını çıkar; `choices[0].delta.reasoning_content` (DeepSeek R1 vb.) alanını kullanıcıya göstermeden ayrı iç tamponda tut; `data: [DONE]` gelince akışı temiz sonlandır.
     * **Google Gemini `streamGenerateContent?alt=sse`:** `candidates[0].content.parts[0].text` parçalarını oku ve birleştir.
     * Hatalı/yarım JSON satırlarında, boş `keep-alive` (`: OPENROUTER PROCESSING`) yorum satırlarında çökmeden güvenle devam et.
2. **Gerçek Zamanlı Gizli Etiket & `<think>` Kalkanı (`StreamingTextSanitizer.kt` — YENİ):**
   - Canlı akan her tokenı kullanıcıya göstermeden önce küçük bir **lookahead kalkanından** geçir:
     * `<think>` açıldığı anda kapanan `</think>` gelene kadar metni gizle (isteğe bağlı olarak balonda zarif *"✦ Düşünüyor..."* nabız göstergesi göster).
     * Metin içinde `[[STATE_JSON`, `[CHARACTER_EMOTION`, `[ACTIVE_MEMORY_CALL` öneklerinin herhangi bir başlangıç parçası (`[` veya `[[S...`) yakalandığında, o kuyruğu geçici tamponda beklet; gerçekten gizli etiketse ekrana **1 karakter bile sızdırmadan** yut ve ham tam metinde (`rawFullText`) sakla ki akış bittiğinde `parseAndApplyEmotionUpdates()` eksiksiz işlesin!
     * Eğer sadece normal bir köşeli parantezse (ör. `[fısıldayarak]` veya normal anlatım), eşleşme bozulduğu an tamponu anında ekrana serbest bırak.
3. **Gerçek Streaming Destekli `ProviderFallbackManager.kt`:**
   - `executeChatStream(request, onDelta: suspend (String) -> Unit, onFirstToken: () -> Unit): StreamResult` metodunu ekle (`ExecutionMode.NARRATIVE` için):
     * **Ayar Anahtarı (`AppFeatureFlags.kt`):** `KEY_SSE_STREAMING_ENABLED = "sse_streaming_enabled"` (varsayılan `true`, Genel Ayarlar -> API / Sohbet sekmesinde **"Gerçek Zamanlı Akış (SSE Streaming)"** Mor Cam anahtarı). Kapalıysa veya seçili sağlayıcı SSE desteklemiyorsa mevcut blok JSON çağrısına sorunsuz düş.
     * **OkHttp İstek Yapısı:** `"stream": true` ekle; `Accept: text/event-stream`, `Cache-Control: no-cache` başlıklarını koy. Timeout davranışını **`AppFeatureFlags.isStrictApiTimeoutEnabled`** bayrağına tam bağla (açıksa TTFB/okuma `60 sn`, kapalıysa `0` / `300 sn` sınırsız bekleme).
     * **Akıllı Fallback Kuralı:**
       - **İlk token (`onFirstToken`) gelmeden önce** HTTP `401/403/429/5xx`, `400` (bazı modellerin `stream: true` reddi -> önce aynı sağlayıcıda non-stream dene, yine olmazsa sıradaki sağlayıcıya geç!) veya ağ hatası olursa kullanıcıya hiçbir yarım metin göstermeden sıradaki yedek sağlayıcıya geç!
       - **İlk token geldikten sonra** bağlantı koparsa kullanıcıya o ana kadar gelen temiz metni kaybetmeden koru veya metin çok kısaysa (`<25 karakter`) sıradaki yedeğe geç.
       - **`CancellationException` (Kullanıcı Durdur Butonu):** OkHttp `Call.cancel()` anında ağı kapatır, `ProviderHealthTracker`'a **0 ceza** yazar ve o ana kadar gelen temiz metni ister saklar ister iptal durumuna alır.
       - **`refusal_auto_fallback` Uyumu:** İlk `120–240` karakter veya akış sonunda bariz OOC sansür/ahlak vaazı tespit edilirse ve `refusal_auto_fallback == true` ise mevcut metni temizleyip tek seferlik yumuşatılmış edebi çerçeveyle veya sıradaki sağlayıcıyla otomatik yeniden akıt.
4. **60 FPS İzole Compose Akışı & `startTypewriter` Emekliliği (`EmochiViewModel.kt`, `ChatScreen.kt`, `ChatMessageBubbles.kt`):**
   - `EmochiViewModel.kt` içindeki sahte `startTypewriter()` yapay gecikme animasyonunu **tamamen kaldır**!
   - Yeni izole akış durumu:
     ```kotlin
     val activeStreamingMessageId: StateFlow<Long?>
     val activeStreamingText: StateFlow<String>
     val isStreamingThinking: StateFlow<Boolean>
     ```
   - **Frame-Safe Coalescing (Recomposition Fırtınası Koruması):** Sunucudan saniyede 80–120 mikro token gelse bile UI `StateFlow` güncellemesini en fazla **~33 ms (30 Hz)** aralıklarla tamponlayıp (`buffer` + `flush`) yayınla; böylece telefon ısınmaz, pil yanmaz ve `ChatMessageBubbles` içinde **yalnızca aktif tek balon** (`msg.id == activeStreamingMessageId`) bu flow'u `collectAsStateWithLifecycle()` ile dinler — geri kalan tüm sohbet listesi **0 kez** recompose olur!
   - Akış sırasında kullanıcı yukarı kaydırıp eski mesajları okuyorsa otomatik aşağı çekme (`autoScroll`) kullanıcıyı zorla aşağı fırlatmasın; yalnızca kullanıcı zaten en alttaysa yumuşak takip etsin.
   - Akış bittiğinde `rawFullText` üzerinden:
     1. `cleanEmotionTags()` ile temiz metni Room `messages` tablosuna kaydet,
     2. `parseAndApplyEmotionUpdates()` ile duygu (`#14`), zaman atlama (`#15`), yan karakter (`#14`), tetikleyici/olgunlaşma (`#22`) ve kilometre taşı (`#18`) güncellemelerini uygula,
     3. Arka planda hafıza/kadro/Bilgi Grafiği küratörünü (`#1`, `#16`, `#20`) tetikle,
     4. Otomatik TTS açıksa (`#11`) duygulu Nöral seslendirmeyi başlat.

---

## 📁 ETKİLENECEK DOSYALAR HARİTASI

1. **#20 Bilgi Grafiği:**
   - `app/src/main/java/com/example/data/managers/WorldStateManager.kt` — Türkçe normalizasyon, `depth <= 4` döngü korumalı `traverseRelationGraph`, çelişki gideren `upsertEntityRelation`.
   - `app/src/main/java/com/example/data/managers/MemoryRetrievalManager.kt`, `EmochiRepository.kt`, `SystemPromptBuilder.kt` — Canlı `detectAndResolveGraphQuery` çağrısı, `[İLİŞKİ AĞI GERÇEĞİ]` enjeksiyonu, küratör `"relations"` çıkarımı.
   - `app/src/main/java/com/example/ui/components/NeuralVaultModal.kt` — Bilgi Grafiği ilişkilerini görüntüleme & tek tıkla silme.
2. **#22 Hassas Tetikleyiciler & Olgunlaşma:**
   - `app/src/main/java/com/example/data/repository/EmochiRepository.kt`, `EmotionEngine.kt`, `SystemPromptBuilder.kt` — Canlı `evaluateSensitiveTriggers`, sabit `+15 mesaj / +3 gün` kilidinin kaldırılması, `pendingReappraisalDao` canlı prompt okuma, `[[STATE_JSON]]` `"trig"` & `"reapp"` (`softened`/`resolved`/`hardened`) işleyici, 14+ hikâye günü doğal bayatlama.
   - `app/src/main/java/com/example/ui/components/EmotionStatusSection.kt` & `BotSettingsModal.kt` — Aktif hassas tetikleyiciler ve açık olgunlaşma rozeti.
3. **#23 Gerçek SSE Streaming:**
   - `app/src/main/java/com/example/util/SseStreamParser.kt` — OpenAI-compatible & Gemini SSE gerçek zamanlı ayrıştırıcı.
   - `app/src/main/java/com/example/util/StreamingTextSanitizer.kt` *(YENİ)* — `<think>` ve `[[STATE_JSON` / `[CHARACTER_EMOTION` sıfır-sızıntı lookahead kalkanı.
   - `app/src/main/java/com/example/util/AppFeatureFlags.kt` & `GlobalSettingsModal.kt` — `KEY_SSE_STREAMING_ENABLED` anahtarı.
   - `app/src/main/java/com/example/data/managers/ProviderFallbackManager.kt` — `executeChatStream`, pre-first-token fallback, `isStrictApiTimeoutEnabled` uyumu, `CancellationException` anında OkHttp iptali.
   - `app/src/main/java/com/example/ui/viewmodel/EmochiViewModel.kt`, `ChatScreen.kt`, `ChatMessageBubbles.kt` — `startTypewriter` kaldırma, 33 ms coalesced `StateFlow`, tek-balon izole akış, kullanıcı kaydırma saygısı.

---

## 🧪 ZORUNLU TEST PAKETİ (`Systems20_22_23_TenOutOfTenTest.kt`)

Aşağıdaki **10 birim testini** ekleyip mevcut tüm test dosyalarıyla birlikte eksiksiz geçmesini doğrula:
1. **#20 Çok Adımlı Bilgi Grafiği & Döngü Koruması Testi:** 3 adımlı zincirde (`Aiden -> kardeşi -> Mira -> patronu -> Vaelor`) `"Aiden'ın kardeşinin patronu kim?"` sorgusunun doğru çözümlendiğini; `A <-> B` döngülü grafikte sonsuz döngüye girmeden güvenle sonlandığını doğrula.
2. **#20 Canlı Enjeksiyon, Çelişki & Rewind Testi:** Çözümlenen zincirin `SystemPromptBuilder` çıktısına `[İLİŞKİ AĞI GERÇEĞİ]` olarak girdiğini; `sevgilisi` -> `eski sevgilisi` çelişkisinin güncellendiğini ve `rewindToMessage` sonrası hedef sonrasındaki kenarların silindiğini kanıtla.
3. **#22 Canlı Tetikleyici & Spam Cooldown Testi:** Kullanıcı mesajı botun hassas yarasına dokunduğunda `evaluateSensitiveTriggers` sonucunun canlı prompta `[HASSAS YARA TETİKLENDİ]` olarak girdiğini, `PendingReappraisalEntity` oluşturduğunu, OOC `((...))` mesajının tetiklemediğini ve arka arkaya 2 turda patlama yapmadığını doğrula.
4. **#22 Katı Kilitsiz Esnek Olgunlaşma (`reapp`) Testi:** Yalnızca 2 mesaj sonra (eski +15 mesaj kilidi olmadan!) `pendingReappraisalDao` kaydının prompta okunduğunu ve `[[STATE_JSON {"reapp":{"id":...,"out":"resolved"}}]]` geldiğinde kaydın `resolved = true` olup duyguların toparlandığını; `14+` hikâye günü geçince bayatlayıp (`expired = true`) prompttan çıktığını kanıtla.
5. **#23 OpenAI & Gemini SSE Parser Testi:** `SseStreamParser`'ın hem OpenAI `data: {"choices":[{"delta":{"content":"..."}}]}` + `data: [DONE]` hem de Gemini `candidates[0].content.parts[0].text` + `: keep-alive` satırlarını hatasız ve eksiksiz birleştirdiğini doğrula.
6. **#23 `StreamingTextSanitizer` Sıfır Sızıntı Testi:** Tokenlar 1–3 karakterlik parçalar hâlinde gelirken (`"Merhaba "`, `"<th"`, `"ink>gizli"`, `"</think>dostum "`, `"[[STA"`, `"TE_JSON {\"dom\":\"mutlu\"}]]"`) kullanıcıya akan metnin yalnızca `"Merhaba dostum "` olduğunu, gizli etiket veya `<think>` içeriğinden **0 karakter** sızdığını kanıtla.
7. **#23 Normal Köşeli Parantez Koruması Testi:** `"Merhaba [gülümseyerek] nasılsın?"` akışında `[gülümseyerek]` ifadesinin yutulmadan kullanıcıya eksiksiz aktığını doğrula.
8. **#23 Streaming Pre-First-Token Fallback & `CancellationException` Testi:** İlk sağlayıcı ilk tokendan önce `429` verdiğinde ikinci sağlayıcıya kesintisiz geçip akışın tamamlandığını; kullanıcı iptalinde (`CancellationException`) `ProviderHealthTracker` hata sayacının artmadığını doğrula.
9. **#23 SSE Kapalı / Non-Stream Fallback & `startTypewriter` Temizliği Testi:** `isSseStreamingEnabled = false` iken klasik tek parça yanıta sorunsuz düşüldüğünü ve `EmochiViewModel` içinde yapay `startTypewriter` kullanılmadığını doğrula.
10. **Derleme & Tam Regresyon:** `./gradlew testDebugUnitTest` ve `./gradlew assembleDebug` sıfır hata ve sıfır yeni uyarıyla geçer.

### ✅ Teslimat Öncesi 10/10 Doğrulama Kontrol Listesi
1. **[ ] #20 Bilgi Grafiği Canlı mı?** `detectAndResolveGraphQuery` her turda çalışıyor, 3–4 adımlı döngü-güvenli zincirler `[İLİŞKİ AĞI GERÇEĞİ]` olarak prompta giriyor, çelişkiler güncelleniyor ve Neural Vault'ta görünüyor mu?
2. **[ ] #22 Tetikleyiciler & Esnek Olgunlaşma Canlı mı?** `evaluateSensitiveTriggers` canlı sohbette çalışıyor, katı `+15 mesaj / +3 gün` kilidi kalktı, `pendingReappraisalDao` prompta okunup `"reapp"` ile `softened`/`resolved`/`hardened` işleniyor mu?
3. **[ ] #23 Gerçek SSE Streaming Akıcı & Sızıntısız mı?** Sahte `startTypewriter` kalktı, OpenAI + Gemini SSE canlı akıyor, `<think>` ve `[[STATE_JSON]]` 0 karakter sızıntıyla gizleniyor, 33 ms coalescing ile tek balon güncelleniyor ve iptal/fallback/timeout bayrağı eksiksiz çalışıyor mu?
4. **[ ] Kırmızı Çizgiler Korundu mu?** `drawable-nodpi/` altındaki mevcut hiçbir WebP silinmedi, Mor Cam tasarımı bozulmadı ve tüm mevcut + yeni testler yeşil mi?