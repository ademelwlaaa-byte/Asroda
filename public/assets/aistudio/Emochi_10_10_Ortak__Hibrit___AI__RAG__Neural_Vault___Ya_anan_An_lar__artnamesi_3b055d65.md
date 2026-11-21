# 🧠 EMOCHI — 10/10 ORTAK (HİBRİT + YAPAY ZEKA) RAG, NEURAL VAULT VE YAŞANAN ANILAR ŞARTNAMESİ

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Görevin, Emochi Android uygulamasındaki (Kotlin & Jetpack Compose) **RAG + Neural Vault + Yaşanan Anılar (Living Memories)** sistemini sıfır kalite kaybı ve maksimum token verimliliğiyle **10/10** seviyesine taşımaktır.
> 1. **Temel Mimari Felsefe (Gerçek Ortaklık):** Hibrit motor da tek başına 10/10 olacak, Yapay Zeka RAG da 10/10 olacak; ancak sistemin yükünün %80'i körlemesine yerel eşleşmeye bırakılmayacak! **Yalnızca en basit, düz turlar** saf hibrit motorda çözülecek; **gerçek hikâye, duygu, alt metin ve ilişki içeren tüm turlarda Hibrit + Yapay Zeka birlikte (Co-Piloted)** çalışacak.
> 2. **Tutumlu Token + Yüksek Zekâ Dengesi:** Token tasarrufu, Yapay Zekayı kapatarak değil; **Hibrit motoru aday daraltıcı (filtre) olarak kullanıp Yapay Zekaya sadece ultra-kısa indeksler göndererek** ve **tek birleşik Utility Model çağrısıyla** yapılacak.
> 3. **Tam AI Neural Vault Yetkisi:** Yapay zeka Neural Vault'a yeni kayıt **ekleyebilecek**, mevcut kayıtları `#ID` ile **güncelleyebilecek** ve geçersizleşenleri arşive alarak **silebilecek**.
> 4. **Domino Etkisi Koruması:** Mevcut kozmik-gotik Mor Cam UI dilini (`#090414`, buzlu cam, Serif tipografi), `AvatarOptimizer` ve Coil bellek kurallarını, sohbet ekranının son 5 mesaj / kaydırma davranışlarını ve Emochi Roman AI'nın ahlak bekçiliği yasağını **asla bozma**.
> 5. **Kısaltma Yasağı:** `// ...`, yarım metod veya canlı akışa bağlanmamış ölü kod bırakma.

---

## 🎯 BÖLÜM 1: GERÇEK ORTAKLIĞA DAYALI "HİBRİT + AI" RAG MİMARİSİ

Bu sistem iki motorun birbirinin yerine geçmesi değil, **birbirini tamamlaması** üzerine kuruludur:
* **10/10 Hibrit Motorun Rolü:** Tüm veritabanını (yüzlerce/binlerce kayıt) milisaniyeler içinde ve **0 token** ile tarar, gürültüyü eler ve en alakalı aday havuzunu çıkarır.
* **10/10 Yapay Zeka RAG'ın Rolü:** Hibrit motorun çıkardığı dar ve ucuz aday havuzuna **insan seviyesinde bağlam, kinaye, duygu, alt metin ve karakter psikolojisi** uygular; en doğru kayıtları seçer ve hibritin kelime farkı yüzünden kaçırdığı soyut anı varsa ikinci mikro-aramayı tetikler.

### 🧭 Akıllı Yönlendirici (Routing — Ne Zaman Sadece Hibrit, Ne Zaman Ortak?)

#### 1) Saf 10/10 Hibrit Yolu — **YALNIZCA En Basit Şeyler (~%15–20 Tur | 0 AI Token | <10 ms)**
Sadece aşağıdaki iki basit durumda ekstra AI RAG çağrısı yapılmaz:
* **Durum A — Gündelik/Kısa Tepkiler:** `MemoryEngine.isTrivialCasualMessage(userText)` true ise (ör. `"selam"`, `"tamam"`, `"devam et"`, `"evet"` gibi kısa, içeriksiz mesajlar) -> yalnızca `★ userCorrected` sabit 3 bilgi + en güncel checkpoint verilir.
* **Durum B — Düz, Tekil ve Kesin Olgu Soruları:** Mesaj kısa, tekil bir doğrudan soruysa (ör. `"Aiden kaç yaşındaydı?"`, `"En sevdiği içecek neydi?"`), duygusal/soyut alt metin taşımıyorsa **VE** 10/10 Hibrit motorun RRF + kesin anahtar eşleşme güveni çok yüksekse (`topConfidence >= 0.88f` ve 2. adayla net fark `gap >= 0.18f`) -> doğrudan Hibrit sonucu kullanılır.

#### 2) Ortak (Hibrit + Yapay Zeka) Yolu — **Tüm Gerçek Hikâye & Karmaşık Turlar (~%80 Tur | Tek Ultra-Ucuz Utility Çağrısı)**
Yukarıdaki en basit 2 durum dışındaki **tüm anlamlı RP, diyalog, duygu, entrika, ima, sır, karakter etkileşimi ve çok katmanlı sahne turlarında** Hibrit ve Yapay Zeka **ortak boru hattında** çalışır:

1. **Adım 1 — 10/10 Hibrit Ön Tarama (`<12 ms`, `0 Token`):**
   * `MemoryRetrievalManager`, `MemoryVectorCache` üzerinden tüm `MemoryFactEntity`, `MemoryEventEntity` (Yaşanan Anılar), `MemoryCheckpointEntity` ve `MemoryFragmentEntity` kayıtlarını RRF ile tarar.
   * Çıktı: En iyi **14–16 adayın ultra-kısa 1 satırlık mikro-indeksi** (tam paragraflar değil, her aday maksimum `~12–16 token`):
     ```text
     F12: (Aiden|Sır) Beş yaşında mavi ışıkla ailesini kaybetti
     F19: (Natasha|Güven) Aiden'a temkinli ama gizlice koruyucu
     E7: [Natasha|Çatıda İlk Güven|8/10] Kaçmak yerine kalmayı seçtiği gece...
     C3: Sahne #3: S.H.I.E.L.D. alarmında Kule'den kaçmadı, ekibe güvendi.
     ```
2. **Adım 2 — Tek Birleşik AI RAG Karar Çağrısı (`resolveUtilityModel()` | `temp = 0.10` | `~350 in / ~45 out token`):**
   * Ayrı ayrı "Query Rewrite" + "Rerank" çağrısı yapıp token ve süre harcama! **Tek bir mikro Utility Model çağrısına** son 3 mesajı + aktif sahne karakterlerini + yukarıdaki 15 satırlık mikro-indeksi ver.
   * Modelden yalnızca şu ultra-kompakt JSON'u üretmesini iste:
     ```json
     {"pick":["F12","E7","F19","C3"],"miss_queries":["mavi gece kolye koordinat"]}
     ```
   * **`pick`:** Bağlam, alt metin, duygusal gerilim ve karakter ilişkisi açısından bu sahneye gerçekten hizmet eden **en keskin 4–8 kaydın ID'leri**. (Gereksiz kayıtları eler -> **ana sohbet modelinin promptunu temiz ve küçük tutar!**)
   * **`miss_queries`:** Çoğu zaman boş (`[]`) gelir. Yalnızca kullanıcı çok soyut bir şeye değinmiş ve Hibrit'in ilk 15 adayında o gizli anı eksik kalmışsa, AI 1–2 keskin Türkçe arama ifadesi yazar.
3. **Adım 3 — Hedefli İkinci Hibrit Geçiş (Yalnızca `miss_queries` doluysa | `<5 ms`, `0 Ekstra AI Token`):**
   * AI'nın ürettiği `miss_queries` ile yerel 10/10 Hibrit motor önbellekte anlık ikinci bir arama yapar, yakalanan en iyi 2–3 eksik kaydı `pick` listesine ekler.
4. **Adım 4 — Kesintisiz Fallback Garantisi:**
   * Utility Model çağrısı zaman aşımına uğrarsa (`> 2.5 sn` timeout) veya ağ/kota hatası verirse sistem **hiç hata fırlatmadan** Adım 1'deki 10/10 Hibrit sıralamasının en iyi sonuçlarıyla anında devam eder.

---

## 🎯 BÖLÜM 2: HİBRİT MOTORU TEK BAŞINA 10/10 YAPMA KURALLARI

Ortak sistemin temeli olan yerel Hibrit motor şu 5 mühendislik standardıyla 10/10 seviyesine çıkarılacaktır:

1. **RRF (Reciprocal Rank Fusion) Birleştirme (`MemoryRetrievalManager.kt`):**
   * Farklı ölçeklerdeki skorları ham toplamak yerine, 3 bağımsız sıralama kanalını RRF ile birleştir:
     * **Kanal 1 — Semantik Vektör Sıralaması:** Varsa gerçek API embedding (ör. `text-embedding-004` / uyumlu embedding), yoksa 256-dim Türkçe imzalı feature-hashing + bigram + 3/4-gram vektörü.
     * **Kanal 2 — Türkçe Morfolojik BM25 Sıralaması:** `TurkishTextAnalyzer` kök/ek temizliği + kesme işareti ayırma (`Natasha'nın -> natasha`) + IDF ağırlıklandırması.
     * **Kanal 3 — Varlık, Karakter & Önem Sıralaması:** Son 4 mesajda geçen karakter isimleri, özel isimler, `★ userCorrected` sabit kayıtlar ve `importanceScore`.
   * Formül referansı:

```kotlin
// RRF birleştirme: k = 60 ölçek farklarını ortadan kaldırır
fun rrfScore(semRank: Int, lexRank: Int, entityRank: Int, boost: Float): Float {
    val k = 60f
    return (1f / (k + semRank)) * 1.15f +
           (1f / (k + lexRank)) * 1.00f +
           (1f / (k + entityRank)) * 0.85f +
           boost
}
```

2. **Sıfır Tekrarlı RAM Önbelleği (`MemoryVectorCache`):**
   * Şu an her mesajda JSON'dan `FloatArray` parse etmek ve tüm kayıtları tekrar stem'lemek CPU'yu boşa yoruyor.
   * Son 5 aktif bot için `LruCache<String, BotMemoryIndex>` tut: her kaydın hazır `FloatArray`'i, önceden çıkarılmış `Set<String>` Türkçe kökleri ve normalize metni RAM'de beklesin.
   * Yalnızca o botta bir kayıt eklendiğinde, güncellendiğinde veya silindiğinde o botun önbelleğini anlık güncelle/temizle.

3. **Çok Turlu Bağlam Genişletme (`buildExpandedQuery`):**
   * Son kullanıcı mesajını `2x` ağırlıkla, ondan önceki 3 mesajı `1x` ağırlıkla ve sahnedeki aktif yan karakter adlarını birleştir; böylece `"ona dün verdiğin söz neydi?"` gibi zamir ağırlıklı mesajlarda bile Hibrit aday havuzu isabetli olur.

4. **Çapraz Kaynak Tekilleştirme:**
   * `MemoryFactEntity`, `MemoryEventEntity`, `MemoryCheckpointEntity` ve `MemoryFragmentEntity` arasında `stemOverlapRatio >= 0.80f` olan tekrarlı bilgileri ana prompta yalnızca en zengin/yapısal olanıyla 1 kez koy.

5. **Hibrit Çift Embedding Stratejisi:**
   * Kayıt anında önce yerel 256-dim Türkçe vektörü anında (`<1 ms`) kaydet. Eğer kullanıcının geçerli bir embedding API anahtarı varsa, arka planda (`Dispatchers.IO`) sessizce gerçek embedding ile zenginleştir; offline veya anahtarsızken sistem sıfır kayıpla yerel vektörle çalışmaya devam etsin.

---

## 🎯 BÖLÜM 3: YAPAY ZEKA TARAFINDAN TAM YÖNETİLEN NEURAL VAULT (EKLE / GÜNCELLE / SİL)

### 🔍 Mevcut Kopukluk
`EmochiRepository.buildSystemPrompt()` Neural Vault kayıtlarını prompta yazarken **veritabanı ID'lerini vermiyor**, bu yüzden yapay zeka `update_memory` ve `delete_memory` araçlarında hangi kaydı güncelleyeceğini veya sileceğini bilemiyor.

### 🛠️ 10/10 AI Neural Vault CRUD Mimarisi

1. **ID-Etiketli Ultra-Kompakt Prompt Formatı (Token Tasarrufu + Kesin Hedefleme):**
   * Neural Vault bloğunu ana modele ve küratöre daima `#ID` ile ve tek satır yoğun formatta ver:
   ```text
   [NEURAL VAULT — KESİN HAFIZA (Değişirse #ID ile güncelle/sil; ★ kullanıcı kilididir)]
   [#12](Aiden|Sır): Beş yaşında mavi ışıkla ailesini kaybetti
   [#15★](Kullanıcı|Meslek): Mimar, İstanbul'da yaşıyor
   [#19](Natasha|İlişki): Temkinli ama Aiden'ı gizlice kolluyor
   ```

2. **Çift Kanallı, Sıfır Çakışmalı Tam CRUD:**
   * **Kanal A — Sohbet İçi Anlık Araçlar (Native Tool Call + Text-Fallback):**
     * `MemoryToolRegistry.CENTRAL_TOOLS` içindeki `save_memory` (opsiyonel `key` ekle), `update_memory` ve `delete_memory` araçlarını tam çalıştır.
     * Desteklemeyen Custom Provider'lar için prompta tek satırlık `[ACTIVE_MEMORY_CALL: save_memory(...)|update_memory(...)|delete_memory(...)]` fallback kuralı ekle ve `cleanEmotionTags()` ile ekrandan %100 temizle.
     * **Toleranslı Hedef Çözümleyici:** Model `memoryId` olarak `"12"`, `"#12"`, `"F12"` gönderirse doğrudan ID ile bul; zayıf bir model ID yerine içerik metni yazarsa aktif kayıtlar içinde semantik+metin eşleşmesi (`>= 0.72f`) ile hedef kaydı otomatik yakala.
   * **Kanal B — Tur Sonrası Arka Plan AI Hafıza Küratörü (`extractAndCommitTurnMemories`):**
     * Her anlamlı tur sonrası arka planda (`Dispatchers.IO`), **kesinlikle `resolveUtilityModel()` + `X-Emochi-Call-Type: utility`** ile çalışır (kullanıcıyı bekletmez).
     * Eğer Kanal A'da ana model o turda zaten `save/update/delete` yaptıysa, yapılan işlemleri küratöre bildirerek aynı kaydın çift eklenmesini engelle.
     * Küratör mevcut `[#ID]` listesini görür ve tek JSON'da **ekleme, güncelleme ve silme** kararlarını verir:
     ```json
     {
       "vault_ops": [
         {"op":"add","subject":"...","key":"...","value":"...","confidence":"certain"},
         {"op":"update","id":19,"new_value":"Artık Aiden'a açıkça güveniyor","reason":"çatıda itiraf etti"},
         {"op":"delete","id":24,"reason":"kullanıcı bunun yanlış olduğunu belirtti"}
       ],
       "living_memory": null
     }
     ```

3. **Güvenli Silme (Soft-Archive), Çelişki Koruması & Kullanıcı Kilidi (`★`):**
   * **Veri Kaybı Yasağı:** AI bir kaydı güncellediğinde veya sildiğinde kayıt yok edilmez; `ArchivedMemoryEntity` tablosuna (`archivedReason = "ai_updated" | "ai_deleted" | "user_double_correction"`) aktarılır ve eski kayda `supersededBy` yazılır.
   * **Kullanıcı Kilidi (`userCorrected == true`):** Kullanıcının elle düzenlediği/sabitlediği (`★`) kayıtları AI kendi inisiyatifiyle değiştiremez veya silemez — **yalnızca** kullanıcının o mesajda açıkça `"hayır öyle değil, değiştir/unut/sil"` demesiyle (`explicitUserOverride = true`) güncellenebilir.
   * **Örtük Düzeltme (Implicit Learning):** Kullanıcı sohbette kayıtlı bir bilgiyi yalanlarsa 1. red -> `confidence = "disputed"` (RAG'da ceza alır); 2. red -> otomatik arşive taşınır ve aktif RAG'dan çıkarılır.

---

## 🎯 BÖLÜM 4: ÇOKLU BAKIŞ AÇILI "YAŞANAN ANILAR" (LIVING MEMORIES) 10/10 STANDARDI

`LivingMemoryCodec` formatını (`POV | TITLE | EMO | WEIGHT | NARRATIVE | [[INNER]]`) koru ve şu kalite kapılarını uygula:
1. **Acımasız Seçicilik:** Sıradan sohbetlerin **%80–85'inde `living_memory: null`** dönmelidir. Yalnızca `salience >= 7` (büyük itiraf, ihanet, hayati kurtarış, sarsıcı sır, derin yakınlaşma, yemin, kırılma noktası) anılar işlenir.
2. **Kopyala-Yapıştır Yasağı:** `LivingMemoryCodec.isCopyPasteOfChat()` filtresiyle sohbetten ham alıntı yapan veya kısa/yüzeysel anıları reddet.
3. **Gerçek Multi-POV & Söylenmemiş İç Ses:** Anıyı her zaman ana karakterin gözünden değil, o sahnede **duygusal olarak en çok sarsılan karakterin** (`userChar`, `aiChar` veya yan karakter) 1. tekil şahıs ("Ben...") ağzıyla yazdır. `[[INNER]]` iç sesi, sohbette asla dile getirilmemiş gizli duyguyu/şüpheyi taşımalıdır.
4. **Aynı Anıyı Zenginleştir (Çoğaltma Yerine Birleştir):** Aynı hikâye gününde ve aynı `povCharacter` için mevcut bir anıyla `cosineSimilarity >= 0.78f` eşleşen yeni anı gelirse yeni satır açma; eski halini arşive alıp mevcut anıyı daha yüksek `salience` ile zenginleştir.
5. **Doğal Bellek Netliği (`memoryClarityTag`):** `importanceScore >= 90` olan büyük kırılma anıları hep `[Net Anı]` kalır; daha hafif eski anılar hikâye günleri ilerledikçe `[Bulanık Anı]` / `[Silik Anı]` etiketleriyle doğal insan hafızası gibi prompta girer.

---

## 🎯 BÖLÜM 5: TOKEN EKONOMİSİ, GLOBAL INTERCEPTOR AYRIMI VE CHECKPOINT PİRAMİDİ

1. **Kritik Altyapı Düzeltmesi — `EmochiApplication.kt` Sıcaklık İzolasyonu:**
   * Mevcut global OkHttp interceptor'ı her POST isteğine `temperature = 0.90` basıyor; bu durum RAG seçimi ve Neural Vault JSON CRUD çağrılarını bozuyor.
   * Dahili `X-Emochi-Call-Type` header'ı ekle (`"narrative"` vs `"utility"`). Interceptor dışarı göndermeden önce bu header'ı **silip**:
     * `"utility"` (Ortak AI RAG seçici, Neural Vault küratörü, Checkpoint özetleyici, `BackgroundMemoryMergeWorker`) -> `temperature = 0.12`, `top_p = 0.80`, düşük `max_tokens`.
     * `"narrative"` (Ana hikâye/sohbet) -> `temperature = 0.90`, `top_p = 0.95`.

```kotlin
val callType = originalReq.header("X-Emochi-Call-Type") ?: "narrative"
val cleanBuilder = originalReq.newBuilder().removeHeader("X-Emochi-Call-Type")
val (temp, topP) = if (callType == "utility") 0.12 to 0.80 else 0.90 to 0.95
// JSON gövdesine yalnızca ilgili sağlayıcı şemasına göre temp/topP uygula ve devam et
```

2. **Utility Model Zorunluluğu:** Ortak AI RAG seçici, arka plan Neural Vault küratörü, 50-mesaj Checkpoint özetleyici ve `BackgroundMemoryMergeWorker` **kesinlikle `resolveUtilityModel()`** kullanmalıdır; pahalı ana model yalnızca hikâye yazımı için çalışır.
3. **Dinamik Token Bütçesi (Ana Sohbet Promptu İçin):**
   * Saf Hibrit (en basit tur): maks `260` hafıza tokenı.
   * Ortak Hibrit + AI RAG (standart hikâye turu): maks `750` hafıza tokenı.
   * Ortak Hibrit + AI RAG + `miss_queries` (derin sır/lore/uzun geçmiş turu): maks `1.050` hafıza tokenı (32K ve altı bağlamlı modellerde otomatik `650` tavan).
4. **50 Mesajlık Checkpoint Piramidi:**
   * Her 50 mesajda bir (`checkAndGenerateCheckpoint`) Utility modelle 2–3 cümlelik yoğun sahne özeti üret.
   * Promptta yalnızca **son 2 checkpoint** sabit olarak verilir; daha eski checkpoint'ler (`CP#1`, `CP#2`...) yalnızca Ortak Hibrit + AI RAG seçtiğinde dahil edilir.

---

## 🎯 BÖLÜM 6: NEURAL VAULT UI İYİLEŞTİRMELERİ (MOR CAM UYUMLU)

Mevcut `NeuralVaultModal.kt` ve `ChatScreen.kt` tasarım dilini koruyarak şu 4 net eklemeyi yap:
1. **Canlı CRUD Bildirimi (`memory_commit_pill`):** AI kayıt eklediğinde/güncellediğinde/sildiğinde üstteki ince buzlu cam kapsülde tam aksiyonu göster (ör. *"Neural Vault: +1 eklendi • 1 güncellendi"* veya *"Yeni Anı: Çatıda İlk Güven"*); tıklayınca `NeuralVaultModal` açılsın.
2. **Tek Tıkla Kilit (`★`) Butonu:** Her Neural Vault kartında kullanıcının kaydı kilitleyip açabileceği (`userCorrected` toggle) zarif bir yıldız/kilit ikonu bulunsun.
3. **Arşiv & Tek Tıkla Geri Al (`Undo`):** `NeuralVaultModal` filtrelerine `"Arşiv"` sekmesi ekle; AI'nın güncellediği veya sildiği eski kayıtları listele ve her birine **"Geri Yükle"** butonu koy (`restoreArchivedMemory`).
4. **Canlı RAG Mod Rozeti:** Modal üst kısmında son turda hangi yolun çalıştığını gösteren zarif bir durum rozeti (`Hibrit Hızlı` / `Ortak Hibrit + AI`) göster.

---

## 🎯 BÖLÜM 7: ETKİLENECEK DOSYALAR VE ZORUNLU TEST PAKETİ

### Dosya Haritası
1. `app/src/main/java/com/example/EmochiApplication.kt` — `X-Emochi-Call-Type` header bazlı sıcaklık ayrımı & header temizliği.
2. `app/src/main/java/com/example/data/repository/MemoryEngine.kt` — RRF yardımcıları, dinamik bütçe hesaplayıcı, kompakt `[#ID]` prompt formatı, çapraz tekilleştirme.
3. `app/src/main/java/com/example/data/managers/MemoryRetrievalManager.kt` — `MemoryVectorCache` (bot bazlı LRU), RRF 10/10 Hibrit motor, Akıllı Yönlendirici (Basit -> Saf Hibrit | Hikâye/Karmaşık -> Ortak Hibrit + AI tek birleşik çağrı + opsiyonel `miss_queries` ikinci geçiş).
4. `app/src/main/java/com/example/data/api/MemoryToolModels.kt` — `save_memory` opsiyonel `key` alanı, toleranslı ID/metin argüman ayrıştırma.
5. `app/src/main/java/com/example/data/repository/EmochiRepository.kt` — `[#ID]` prompt enjeksiyonu, tam Neural Vault CRUD (`add/update/delete` + soft-archive), Native tool + Text-fallback + Utility küratör koordinasyonu, 50-mesaj Checkpoint piramidi, örtük düzeltme (`disputed` -> `archived`).
6. `app/src/main/java/com/example/work/BackgroundMemoryMergeWorker.kt` — `resolveUtilityModel()` + `utility` header + `LivingMemory` yapısal koruma.
7. `app/src/main/java/com/example/ui/viewmodel/EmochiViewModel.kt` — Genişletilmiş `MemoryCommitNotice`, arşiv akışı ve `restoreArchivedMemory()`.
8. `app/src/main/java/com/example/ui/components/NeuralVaultModal.kt` — `★` kilit toggle, `"Arşiv"` filtresi, `"Geri Yükle"` aksiyonu ve son RAG modu rozeti.
9. `app/src/test/java/com/example/MemoryEmotionRegressionTest.kt` — Mevcut hafıza/RAG/checkpoint testlerinin onarımı + yeni doğrulama testleri.

### Zorunlu Otomatik Testler
1. **Ortak Yönlendirme Testi:** Basit selamlaşma ve birebir kesin tekil olgu sorularının `0 AI token` ile Saf Hibrit'te kaldığını; duygu, ima, alt metin ve zamir içeren hikâye turlarının ise **Ortak (Hibrit + AI)** yolundan geçerek tek Utility çağrısıyla doğru anıları seçtiğini kanıtla.
2. **AI `miss_queries` Kurtarma Testi:** İlk 15 hibrit adayda kelime farkı yüzünden kaçan soyut bir anının, AI'nın döndürdüğü `miss_queries` ile ikinci geçişte yakalandığını kanıtla.
3. **Tam AI CRUD + Soft-Archive + Undo Testi:** AI'nın `[#ID]` ile ekleme, güncelleme ve silme yaptığını; eski sürümün `archived_memories`'e yazıldığını ve `restoreArchivedMemory()` ile eksiksiz geri geldiğini kanıtla.
4. **Kullanıcı Kilidi (`★`) Testi:** `userCorrected = true` kaydının normal turda AI tarafından değiştirilemediğini, ancak kullanıcının açık düzeltme mesajıyla güncellenebildiğini kanıtla.
5. **Utility Model & Düşük Sıcaklık Testi:** Ortak AI RAG ve küratör isteklerinin ana model yerine `resolveUtilityModel()`'e ve `temperature <= 0.15` ile gittiğini, `X-Emochi-Call-Type` header'ının dışarı sızmadığını HTTP interceptor ile doğrula.
6. **Önbellek Senkronizasyon Testi (`MemoryVectorCache`):** CRUD sonrası önbelleğin bayat veri döndürmediğini doğrula.

---

## ✅ TESLİMAT ÖNCESİ 10/10 DOĞRULAMA LİSTESİ

1. **[ ] Gerçek Ortaklık Çalışıyor mu?** Yalnızca en basit turlar Saf Hibrit'te kalıyor; gerçek hikâye ve karmaşık turlarda Hibrit (aday süzücü) + AI (bağlam/alt metin seçici) tek birleşik Utility çağrısıyla birlikte çalışıyor mu?
2. **[ ] Hibrit Motor Tek Başına 10/10 mu?** RRF birleştirme, `MemoryVectorCache` ve Türkçe morfoloji `<12 ms` hızla çalışıyor ve API hatasında kesintisiz fallback sağlıyor mu?
3. **[ ] AI Neural Vault'ta Tam Yetkili mi?** `[#ID]` promptta görünüyor; AI ekleyebiliyor, güncelleyebiliyor, soft-archive ile silebiliyor ve `★` kilitli kayıtlara saygı duyuyor mu?
4. **[ ] Token Ekonomisi Maksimumda mı?** Yardımcı tüm çağrılar `resolveUtilityModel()` + `temp = 0.12` ile gidiyor, ana modele yalnızca seçilmiş en keskin anılar aktarılıyor mu?
5. **[ ] Yaşanan Anılar 10/10 Kalitede mi?** Yalnızca `>= 7/10` dönüm noktaları, çoklu POV, kopyala-yapıştır olmadan ve söylenmemiş `[[INNER]]` iç sesle kaydediliyor mu?
6. **[ ] Tüm Testler & Derleme Yeşil mi?** `./gradlew testDebugUnitTest` ve `./gradlew assembleDebug` sıfır hatayla geçiyor mu?