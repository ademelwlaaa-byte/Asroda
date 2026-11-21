# 🛡️ EMOCHI — 10/10 SOHBET EKRANI (REWIND & KLON), SESLİ OKUMA (TTS) VE YEDEKLEME / DIŞA AKTARIM ŞARTNAMESİ

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Görevin, Emochi Android uygulamasındaki (Kotlin & Jetpack Compose) şu **3 ana sistemi** tespit edilen tüm teknik açıklardan, ölü/kopuk bağlantılardan ve veri/güvenlik risklerinden temizleyerek **10/10** üretim kalitesine çıkarmaktır:
> - **Sistem 10:** Sohbet Ekranı (`ChatScreen`), Geri Sarma (`Rewind`) & Klonlama (`Clone`)
> - **Sistem 11:** Sesli Okuma (`TTS`) Motoru & Ses Seçici UI
> - **Sistem 13:** Yedekleme (`BackupSnapshot`), Otomatik Kurtarma & Sohbet Dışa Aktarım (`ChatExporter`)
>
> **Kırmızı Çizgiler:**
> 1. **Kısaltma Yasağı:** Hiçbir dosyada `// ...`, sahte özellik veya UI'da açık olup arkada çalışmayan buton bırakma.
> 2. **Domino Etkisi Koruması:** Kozmik-gotik Mor Cam UI tasarım dilini (`#090414`, buzlu cam, `DstIn` üst/alt yumuşak gradyan geçişi, üst çerçeve 4 köşeli yıldız `✦`, alt ışık yansıması, Serif tipografi, `AvatarOptimizer` Micro-WebP ve Coil limitlerini) **asla bozma**.
> 3. **Güvenlik Önceliği:** Kullanıcı API anahtarları hiçbir yedekleme dosyasına varsayılan olarak düz metin yazılmayacak; uzun sohbet paylaşımlarında `TransactionTooLargeException` çökmesi yaşanmayacak.

---

## 💬 BÖLÜM 1 (SİSTEM 10): SOHBET EKRANI, GERİ SARMA (REWIND) & KLONLAMA (10/10)

### 🔍 Kod Tabanında Tespit Edilen Gerçek Kusurlar
1. **Tepe Seviye Recomposition Fırtınası (`MainActivity.kt`):** `EmochiAppMain` fonksiyonu `streamingMsgId` ve `streamingText` dahil 13 farklı `StateFlow`'u kök seviyede topluyor. Mesaj akışı sırasında her karakterde tüm ekran ağacı (TopBar, BottomBar, arka plan, eski balonlar) yeniden değerlendiriliyor.
2. **Proje Şartnamesinden Kayan Eşikler (`ChatScreen.kt`):**
   - "Son 5 Mesaj Kuralı" kodda `orderFromBottom >= 7` (son 6 mesaj) olarak kalmış; şartname gereği **son 5 mesaj** (`orderFromBottom > 5`) olmalı.
   - "En Aşağı İn" butonu `itemsFromEnd >= 4` olarak kalmış; şartname gereği **en az 3 mesaj yukarıda** (`itemsFromEnd >= 3`) belirmeli.
3. **Kodlanmış Ama Erişilemeyen Paneller & Eksik Arama Navigasyonu (`ChatScreen.kt`):**
   - `showStarredSheet` (Yıldızlanan Mesajlar) ve `showQuickProfile` (`BotQuickProfileSheet`) eksiksiz çizilmiş ama ikisini de `true` yapan hiçbir UI tetikleyicisi yok!
   - Arama çubuğu yalnızca `"X sonuç bulundu"` sayısını gösteriyor; eşleşen mesajlara atlama (`↑` / `↓`), 20 mesajlık sayfa dışındaki eski sonucu otomatik açma ve eşleşen kelimeyi vurgulama yok.
4. **Eksik Rewind & Klonlama Tutarlılığı (`EmochiRepository.kt`):**
   - `rewindToMessage()` mesaj, duygu ve temel hafızayı geri sarıyor ama `milestones`, `entity_relations`, `archived_memories` kayıtlarını ve `lastSummarizedMessageCount` / `regenerateCountMap` / `MemoryVectorCache` bellek önbelleklerini temizlemiyor.
   - `cloneBot()` `storyCalendarDate`'i hardcoded `"2026-08-29"` yapıyor, `sensitive_triggers` tablosundaki tetikleyicileri yeni bota kopyalamıyor ve kayıtlı LLM baseline profili (`getStoredBaselineResult`) yerine kaba anahtar kelime fallback'ini çalıştırıyor.

### 🛠️ 10/10 Uygulama Kuralları
1. **Sıfır Recomposition Streaming İzolasyonu (`MainActivity.kt` & `ChatScreen.kt`):**
   - `streamingMsgId` ve `streamingText` `collectAsStateWithLifecycle()` çağrılarını `EmochiAppMain`'den tamamen çıkar.
   - `ChatScreen` içine `StateFlow<String?>` olarak aktar ve **yalnızca aktif mesaj balonunun metin Composable'ında** topla. Böylece yazım/akış sırasında TopBar, BottomBar ve eski balonlar **0 recomposition** yapsın.
2. **Şartname Kurallarını Tam Hizala (`ChatScreen.kt`):**
   - **Son 5 Mesaj Kuralı:** `isUserMsgAutoCollapsible` ve `isBotMsgAutoCollapsible` eşiklerini `orderFromBottom > 5` yap. 6. ve daha eski uzun mesajlarda `▼ Devamını göster` / `▲ Daha az göster` kapsülü çıksın; en güncel 5 mesaj ne kadar uzun olursa olsun asla kesilmesin.
   - **Akıllı "En Aşağı İn" Butonu:** Eşiği `itemsFromEnd >= 3` yap; kullanıcı en alttayken gizli kalsın.
3. **Erişilemeyen Panelleri & Gerçek Mesaj İçi Aramayı Bağla:**
   - **Hızlı Profil (`BotQuickProfileSheet`):** Üst bardaki karakter avatarı/başlık kapsülüne tıklayınca `showQuickProfile = true` açılsın (sağdaki `⚙️` ikonu doğrudan tam `BotSettingsModal`'ı açmaya devam etsin).
   - **Yıldızlanan Mesajlar (`showStarredSheet`):** Arama çubuğu satırına ve `(+)` araç çekmecesine zarif Mor Cam `⭐ Yıldızlananlar` kapsülü koyarak `showStarredSheet = true` yap. Yıldızlananlar penceresinde bir mesaja tıklanınca doğrudan o mesaja kaydır.
   - **Canlı Arama Navigasyonu (`1 / N` + `↑` / `↓` + Vurgulama):**
     * Arama sonuçları geldiğinde sayaç rozeti (`1 / 5`) ile önceki/sonraki (`↑` / `↓`) cam ok butonları göster.
     * Hedef mesaj `visibleMessageLimit` (son 20) dışındaysa `visibleMessageLimit`'i otomatik genişletip `listState.animateScrollToItem()` ile o mesaja git ve mesaj metnindeki aranan kelimeyi yumuşak neon lavanta arka planla (`SpanStyle(background = Color(0x66A855F7))`) vurgula.
4. **Kusursuz Geri Sarma (`rewindToMessage`) & Klonlama (`cloneBot`):**
   - **Rewind Tamlığı:** `db.withTransaction` içinde ek olarak hedef zamandan/indeksten sonra oluşmuş `milestones` kayıtlarını, `entity_relations` ilişkilerini ve `archived_memories` kayıtlarını geri al/temizle; `routines.lastMentionedStoryDate` hedef tarihten büyükse sıfırla; işlem bitiminde `lastSummarizedMessageCount.remove(botId)`, `resetRegenerateCount(botId)` ve `MemoryVectorCache.invalidate(botId)` çağır.
   - **Klonlama Tamlığı:** `cloneBot()` içinde:
     * `storyCalendarDate` değerini `if (original.hasExplicitStoryDate) original.storyCalendarDate else ""` olarak koru (hardcoded `"2026-08-29"` kaldır).
     * `sensitive_triggers` tablosundaki tetikleyicileri `recentTriggerCount = 0` ile `newBotId` altına kopyala.
     * Başlangıç duygu durumunu kör fallback yerine `getStoredBaselineResult(original).baselineEmotionState.toJson()` üzerinden başlat.
5. **Temiz Dosya Parçalama (Görsel Değişiklik Olmadan):**
   - 4.968 satırlık `ChatScreen.kt`'yi sorumluluklarına göre böl: `ChatTopBar.kt`, `ChatMessageBubbles.kt`, `ChatInputDock.kt`, `ChatOverlays.kt`. Hiçbir görsel detay, `DstIn` gradyan maskesi veya animasyon değişmeyecek.

---

## 🔊 BÖLÜM 2 (SİSTEM 11): SESLİ OKUMA (TTS) SİSTEMİ (10/10)

### 🔍 Kod Tabanında Tespit Edilen Gerçek Kusurlar
1. **Tüm Balonlarda Aynı Anda Equalizer Yanması (`ChatScreen.kt`):** `isSpeaking` global bir boolean olduğu için tek bir mesaj okunurken ekrandaki **tüm bot mesaj balonlarında** `CosmicVoiceEqualizer` aynı anda oynuyor ve hangi mesajın okunduğu belli olmuyor.
2. **Eksik Ses Seçici & Test UI (`GlobalSettingsModal.kt`):** `UserSettingsEntity.selectedVoiceName` DB'de ve ViewModel'de tanımlı ama Ses & Dil sekmesinde cihazdaki sesleri listeleyip seçtirecek ve örnek cümle dinletecek UI tamamen eksik.
3. **Uzun Roman Paragraflarında Kesilme Riski (`EmochiViewModel.kt`):** Android `TextToSpeech.speak()` metin uzunluk sınırına takılabiliyor; ayrıca `Regex("\\*.*?\\*")` satır sonu içeren `*...*` bloklarını temizlemiyor ve `cleanEmotionTags()` çağrılmıyor.
4. **Audio Focus & `enableTts` Kontrolü Yok:** TTS konuşurken telefondaki müzik/podcast duraklamıyor (`AudioFocusRequest` yok), `BookAudioController` ile çakışıyor ve `settings.enableTts == false` iken bile manuel okuma/kontrol tutarsız davranıyor.

### 🛠️ 10/10 Uygulama Kuralları
1. **Özel `ChatTtsManager.kt` Yardımcı Sınıfı Oluştur (`util/` altında):**
   - `EmochiViewModel` içindeki ham TTS mantığını temiz bir yöneticiye taşı ve şu StateFlow'ları dışarı ver:
     * `isSpeaking: StateFlow<Boolean>`
     * `speakingMessageId: StateFlow<String?>` *(Yalnızca aktif okunan mesaj ID'si!)*
     * `availableVoices: StateFlow<List<TtsVoiceOption>>` *(Türkçe ve İngilizce yüklü sesleri filtreleyip kaliteye göre sıralar)*
   - **Audio Focus Yönetimi:** Okuma başlarken `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` (`USAGE_ASSISTANT` / `CONTENT_TYPE_SPEECH`) iste; okuma bittiğinde, durdurulduğunda veya odak kaybedildiğinde (`AUDIOFOCUS_LOSS`) sesi kesip odağı bırak. `BookAudioController` çalıyorsa çakışmayı önle.
   - **Akıllı Roman Metni Temizleme & Parçalı Kuyruklama (Chunking):**
     * Önce `EmochiRepository.cleanEmotionTags(text)` uygula; `Regex("\\*.*?\\*", RegexOption.DOT_MATCHES_ALL)` ile sahne betimlemelerini (veya ayar açıksa yalnızca yıldız işaretlerini) temizle, emojileri ve markdown sembollerini ayıkla.
     * Metni cümle sınırlarından **maks 350 karakterlik** parçalara böl: ilk parçayı `QUEUE_FLUSH`, kalanları `QUEUE_ADD` ile kuyruğa ekle; yalnızca son parçanın `utteranceId`'si bittiğinde `speakingMessageId = null` yap.

```kotlin
// Kısa referans: Uzun roman yanıtlarını cümle sınırından bölüp kesintisiz okutma
fun splitForTts(cleanText: String, maxChunkChars: Int = 350): List<String> {
    val sentences = cleanText.split(Regex("""(?<=[.!?…])\s+""")).filter { it.isNotBlank() }
    val chunks = mutableListOf<String>()
    val current = StringBuilder()
    for (s in sentences) {
        if (current.length + s.length + 1 > maxChunkChars && current.isNotEmpty()) {
            chunks.add(current.toString().trim())
            current.clear()
        }
        if (current.isNotEmpty()) current.append(' ')
        current.append(s)
    }
    if (current.isNotEmpty()) chunks.add(current.toString().trim())
    return chunks
}
```

2. **Mesaj Bazlı Equalizer & Anlık Kontrol (`ChatScreen.kt`):**
   - `ChatScreen`'e `speakingMessageId: String?` geçir.
   - Bot mesaj balonunda `val isThisMsgSpeaking = speakingMessageId == msg.id` kontrolü kullan:
     * Yalnızca aktif okunan mesajda `CosmicVoiceEqualizer` görünsün ve dokunulduğunda o mesajı durdursun; başka bir mesaja dokunulduğunda akıcı şekilde o yeni mesaja geçsin.
   - Üstteki yüzen *"Sesli okunuyor..."* barı aktifken kimin okunduğunu ve tek tıkla `Durdur` kapsülünü koru.
3. **Ses Seçici & Önizleme UI (`GlobalSettingsModal.kt` -> Ses & Dil Sekmesi):**
   - Mevcut Hız (`ttsSpeed`) ve Ton (`ttsPitch`) çiplerinin altına Mor Cam tarzı **"Ses Seçimi (`selectedVoiceName`)"** listesi ekle (Türkçe sesler üstte, ağ/yerel etiketiyle).
   - Yanına **"▶ Sesi Dinle"** ön izleme butonu koy; kullanıcı seçtiği ses + hız + ton ile anında 1 cümlelik Türkçe roman örneğini test edebilsin.
   - `AppFeatureFlags` üzerinden (Room şemasını bozmadan) iki hafif tercih ekle:
     * `ttsReadActions` (*"Eylem betimlemelerini (`*...*`) de oku"* — varsayılan `false`, yalnızca konuşmaları doğal okur).
     * `ttsAutoReadReplies` (*"Yeni bot yanıtlarını otomatik sesli oku"* — varsayılan `false`).

---

## 💾 BÖLÜM 3 (SİSTEM 13): YEDEKLEME, OTOMATİK KURTARMA & DIŞA AKTARIM (10/10)

### 🔍 Kod Tabanında Tespit Edilen Kritik Güvenlik ve Veri Kaybı Açıkları
1. **Düz Metin API Anahtarı Sızıntısı (KRİTİK):** `EmochiRepository.exportDataSnapshot()` ve `doActualBackup()` içinde `UserSettingsEntity` tüm düz metin API anahtarlarıyla (`customApiKey`, `groqApiKey`, `claudeApiKey`, `openaiApiKey`, `openRouterApiKey`, `nvidiaApiKey`, `mistralApiKey`, `backupApiKey`) JSON'a yazılıyor. Dahası `doActualBackup()` bunu `getExternalFilesDir(null)` içine de yazarak harici depolamada açık bırakıyor!
2. **Eksik Yedekleme Kapsamı (`BackupSnapshot v1`):** Yedek yalnızca `bots`, `messages`, `settings` içeriyor. Geri yüklendiğinde kullanıcının tüm **Neural Vault bilgileri (`memory_facts`), Yaşanan Anıları (`memory_events`), Checkpoint'leri, Yan Karakter Kadrosu (`cast_members`), Karakter Duyguları (`character_emotions`), Hassas Tetikleyicileri, Rutinleri, Bilgi Grafiği (`entity_relations`) ve Kilometre Taşları** tamamen kayboluyor!
3. **Kırılgan Dosya Yazımı & Açık Cloud Backup:** `file1.writeText(json)` atomik değil (çökme anında bozulur); `AndroidManifest.xml` içinde `allowBackup="true"` açıkken `backup_rules.xml` ve `data_extraction_rules.xml` tamamen yorum satırında bırakılmış.
4. **Ölü `ChatExporter.kt` & `TransactionTooLargeException` Riski:** `BotSettingsModal.kt` uzun sohbetleri `Intent.EXTRA_TEXT` ile ham metin olarak paylaşmaya çalışıyor (Android Binder ~1 MB sınırında çöker!). `ChatExporter.kt` zaten `FileProvider` ile güvenli dosya paylaşımı yapıyor ama hiçbir yerden çağrılmıyor.

### 🛠️ 10/10 Uygulama Kuralları
1. **Kapsamlı ve Geriye Dönük Uyumlu `BackupSnapshot v2` (`EmochiRepository.kt`):**
   - `BackupSnapshot` sürümünü `version = 2` yap (eski `v1` yedekleri default boş listelerle %100 sorunsuz okunabilmeli!).
   - Yeni alanlar (varsayılan `emptyList()`):
     * `memoryFacts: List<MemoryFactEntity>`
     * `memoryEvents: List<MemoryEventEntity>`
     * `memoryCheckpoints: List<MemoryCheckpointEntity>`
     * `castMembers: List<CastMemberEntity>`
     * `characterEmotions: List<CharacterEmotionEntity>`
     * `sensitiveTriggers: List<SensitiveTriggerEntity>`
     * `routines: List<RoutineEntity>`
     * `entityRelations: List<EntityRelationEntity>`
     * `milestones: List<MilestoneEntity>`
     * `exportedAt: Long`
2. **Sıfır API Anahtarı Sızıntısı & Atomik Dahili Otomatik Yedek:**
   - `UserSettingsEntity.sanitizedForBackup()` yardımcı fonksiyonu yaz: tüm API anahtar alanlarını `""` ile maskele.
   - `doActualBackup()` **ASLA** `getExternalFilesDir(null)` dizinine yazmasın (eski harici kopya varsa bir kerelik silsin). Yalnızca özel `context.filesDir` içine atomik olarak yazsın: önce `emochi_autobackup.json.tmp` dosyasına yaz -> `sync()` -> `renameTo(emochi_autobackup.json)`.
   - `exportDataSnapshot(includeApiKeys: Boolean = false)`: Varsayılan **`false`** olsun! `GlobalSettingsModal` -> Sistem sekmesinde kullanıcı açıkça *"API anahtarlarını yedeğe dahil et (Önerilmez)"* onayını açmadıkça anahtarlar JSON'a girmesin.
   - **Akıllı Geri Yükleme (`importDataSnapshot`):**
     * İçe aktarma öncesi `JSON` şemasını doğrula (bozuk dosya/metin geldiğinde veritabanına dokunmadan anlaşılır Türkçe hata ver).
     * Mevcut API anahtarlarını koru: yedek dosyasındaki anahtar alanları boşsa (`""`), cihazdaki mevcut kayıtlı API anahtarlarını **ezme**, mevcutları koru!
     * Kullanıcıya Mor Cam onay diyaloğunda **"Mevcut Verilerle Birleştir (Önerilen)"** veya **"Üzerine Yaz"** seçeneği sun ve içe aktarma özetini göster (*"4 bot, 128 mesaj, 34 hafıza kaydı yüklendi"*).
3. **Android Backup Kurallarını Sertleştir (`res/xml/backup_rules.xml` & `data_extraction_rules.xml`):**
   - Her iki XML dosyasındaki yorum satırlarını gerçek kurallarla doldur: Android Keystore ile şifrelenmiş/hassas verileri ve geçici önbellekleri (`image_cache`, `exports`) bulut yedeğinden kesin olarak hariç tut (`<exclude domain="sharedpref" .../>`, `<exclude domain="file" path="emochi_autobackup.json"/>`).
4. **Tek Merkezli, Çökmesiz Sohbet Dışa Aktarım (`ChatExporter.kt` & `BotSettingsModal.kt`):**
   - `ChatExporter.kt` içindeki deprecated `Locale("tr")` kullanımını `Locale.forLanguageTag("tr-TR")` yap ve **`ExportFormat.MARKDOWN` (`.md` edebi roman formatı)** seçeneğini ekle.
   - `ChatExporter`'a `exportToUri(context, uri, ...)` (SAF `CreateDocument` ile cihaza doğrudan kaydetme) ve `buildExportContent(...)` (kopyalama için) metodlarını ekle; paylaşım (`Intent.ACTION_SEND`) işlemini **yalnızca `FileProvider` URI'si (`Intent.EXTRA_STREAM`)** ile yap — ham `Intent.EXTRA_TEXT` ile devasa metin gönderimini tamamen kaldır!
   - `BotSettingsModal.kt` içindeki kopya export kodunu silip doğrudan `ChatExporter`'a bağla (`.txt`, `.md`, `.json` + Tarih Aralığı + Paylaş / Dosyaya Kaydet / Panoya Kopyala).
   - `cacheDir/exports/` klasöründe 24 saatten eski geçici export dosyalarını yeni export öncesi otomatik temizle.

---

## 📁 BÖLÜM 4: ETKİLENECEK DOSYALAR HARİTASI

1. `app/src/main/java/com/example/MainActivity.kt` — `streamingMsgId`/`streamingText` tepe seviye collect kaldırma, `StateFlow` aktarımı.
2. `app/src/main/java/com/example/ui/screens/ChatScreen.kt` (+ parçalanan `ChatTopBar.kt`, `ChatMessageBubbles.kt`, `ChatInputDock.kt`, `ChatOverlays.kt`) — Son 5 mesaj kuralı, 3+ mesaj "En Aşağı İn", `showQuickProfile` & `showStarredSheet` bağlantısı, mesaj içi arama `↑`/`↓` + vurgulama, `speakingMessageId` bazlı equalizer.
3. `app/src/main/java/com/example/util/ChatTtsManager.kt` *(YENİ)* — Audio focus, cümle chunking, `speakingMessageId`, `availableVoices`, roman metni temizliği.
4. `app/src/main/java/com/example/ui/viewmodel/EmochiViewModel.kt` — `ChatTtsManager` entegrasyonu, `speakingMessageId` + `availableVoices` akışları, `importBackupJson(json, mergeMode)`.
5. `app/src/main/java/com/example/ui/components/GlobalSettingsModal.kt` — Ses & Dil sekmesine Ses Seçici + "▶ Sesi Dinle" + eylem okuma/otomatik okuma anahtarları; Sistem sekmesine API anahtarı maskeleme uyarısı & Birleştir/Üzerine Yaz yedek yükleme akışı.
6. `app/src/main/java/com/example/data/repository/EmochiRepository.kt` — `rewindToMessage` tam temizlik, `cloneBot` trigger + baseline + tarih düzeltmesi, `BackupSnapshot v2` tam kapsam + API anahtarı maskeleme + atomik dahili otomatik yedek.
7. `app/src/main/java/com/example/util/ChatExporter.kt` & `app/src/main/java/com/example/ui/components/BotSettingsModal.kt` — `.txt`/`.md`/`.json` tek merkezli `FileProvider` paylaşımı + SAF kaydetme + eski export temizliği.
8. `app/src/main/res/xml/backup_rules.xml` & `app/src/main/res/xml/data_extraction_rules.xml` — Gerçek hariç tutma (exclude) kuralları.

---

## 🧪 BÖLÜM 5: ZORUNLU TEST PAKETİ VE 10/10 DOĞRULAMA LİSTESİ

Aşağıdaki **7 birim testini** ekleyip tamamının geçtiğini doğrula:
1. **Rewind Tam Temizlik Testi:** 10 mesajlık sohbette 5. mesaja geri sarıldığında 6–10 arasındaki `milestones`, `entity_relations` ve `archived_memories` kayıtlarının da temizlendiğini ve önbelleklerin sıfırlandığını doğrula.
2. **CloneBot Trigger & Baseline Koruma Testi:** Klonlanan botun orijinal bota ait `sensitive_triggers` kayıtlarını `recentTriggerCount = 0` ile aldığını, `storyCalendarDate`'in hardcoded `"2026-08-29"` olmadığını ve orijinal LLM baseline duygu durumunu koruduğunu doğrula.
3. **TTS Chunking & Metin Temizleme Testi:** 1.200 karakterlik çok paragraflı `*eylem*` + `"diyalog"` + `[[STATE_JSON ...]]` metninin etiketlerden tamamen arınıp 350 karakterlik temiz cümle parçalarına bölündüğünü doğrula.
4. **BackupSnapshot v2 Tamlık & v1 Uyumluluk Testi:** `memory_facts`, `memory_events`, `cast_members`, `sensitive_triggers`, `routines` ve `milestones` verilerinin `exportDataSnapshot()` -> `importDataSnapshot()` döngüsünde %100 korunduğunu ve eski `v1` JSON yedeğinin hatasız yüklendiğini doğrula.
5. **API Anahtarı Maskeleme Güvenlik Testi:** Varsayılan `exportDataSnapshot(includeApiKeys = false)` çıktısında hiçbir API anahtarının yer almadığını; bu yedek geri yüklendiğinde cihazdaki mevcut API anahtarlarının silinmeden korunduğunu doğrula.
6. **Atomik Otomatik Yedek Testi:** `doActualBackup()` sonrasında `context.filesDir/emochi_autobackup.json` dosyasının geçerli JSON olduğunu ve `emochi_autobackup.json.tmp` artığı kalmadığını doğrula.
7. **ChatExporter Format & Dosya Testi:** `.txt`, `.md` ve `.json` çıktılarının doğru üretildiğini ve tarih aralığı filtresinin (`LAST_7_DAYS`, `LAST_30_DAYS`, `ALL`) hatasız çalıştığını doğrula.

### ✅ Teslimat Öncesi 10/10 Kontrol Listesi
1. **[ ] Recomposition İzole & Chat Kuralları Tam mı?** Streaming sırasında yalnızca aktif balon güncelleniyor; son 5 mesaj asla kısaltılmıyor; "En Aşağı İn" 3+ mesaj yukarıda görünüyor mu?
2. **[ ] Hızlı Profil, Yıldızlananlar ve Mesaj İçi Arama (`↑`/`↓` + vurgu) Çalışıyor mu?**
3. **[ ] Rewind ve Klonlama Sıfır Veri Artığı / Sıfır Kayıp ile Çalışıyor mu?**
4. **[ ] TTS Yalnızca Okunan Balonda Equalizer Gösteriyor, Audio Focus Alıyor, Uzun Metni Kesmeden Okuyor ve Ayarlardan Ses Seçilebiliyor mu?**
5. **[ ] Yedekleme v2 Tüm Hafıza/Anı/Kadro Verilerini İçeriyor, API Anahtarlarını Maskeliyor ve Harici Depolamaya Yazmıyor mu?**
6. **[ ] `ChatExporter` `BotSettingsModal`'a Bağlı ve `FileProvider` ile Çökmeden Paylaşım Yapıyor mu?**
7. **[ ] `./gradlew testDebugUnitTest` ve `./gradlew assembleDebug` sıfır hata ve sıfır deprecation uyarısıyla geçiyor mu?**