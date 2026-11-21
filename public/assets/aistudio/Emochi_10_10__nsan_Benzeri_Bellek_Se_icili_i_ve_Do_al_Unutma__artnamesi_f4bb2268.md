# 🧠 EMOCHI — 10/10 İNSAN BENZERİ BELLEK SEÇİCİLİĞİ, KISA SÜRELİ HAFIZA VE DOĞAL UNUTMA (TTL) ŞARTNAMESİ

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Mevcut **Ortak (Hibrit + AI) RAG, Neural Vault ve Yaşanan Anılar** sistemini bir üst insan-benzeri zekâ katmanına çıkaracaksın.
> 1. **Temel Hedef:** Sistem her şeyi körlemesine kaydetmeyecek; **bağlamsal önemi hesaplayacak**, gereksiz mikro-hareketleri tamamen eleyecek, gündelik geçici detayları ("dün yediği yemek" gibi) **2–3 hikâye günü canlı tutup sonra doğal olarak unutacak**; ancak süresi dolmadan `"Bana dünkinden yap"` gibi dolaylı ifadeler geldiğinde o geçici kaydı **%100 isabetle** hatırlayacak.
> 2. **Sıfır Ekstra LLM Çağrısı Kuralı:** Bu filtreleme ve ömür (TTL) kararları için yeni bir API çağrısı açma! Mevcut arka plan **AI Hafıza Küratörü (`extractAndCommitTurnMemories`)** ve **Ortak Hibrit + AI RAG** boru hattının içine göm. Böylece gürültü azaldığı için token maliyeti artmak yerine **daha da düşecek**.
> 3. **Domino Etkisi Koruması:** Mevcut Mor Cam UI dilini, `[#ID]` Neural Vault CRUD yetkisini, `resolveUtilityModel()` + `X-Emochi-Call-Type: utility` sıcaklık ayrımını, `MemoryVectorCache` önbelleğini ve Emochi Roman AI'nın ahlak bekçiliği yasağını **kesinlikle bozma**.
> 4. **Kısaltma Yasağı:** `// ...` bırakma; tüm sınıfları canlı sohbet akışına bağla ve testlerle doğrula.

---

## 🎯 BÖLÜM 1: 4 KATMANLI İNSAN BENZERİ BELLEK HİYERARŞİSİ

Her gelen sohbet turundaki bilgiler, sabit bir "kaydet / kaydetme" ikiliği yerine **4 bilişsel katmana** ayrılacaktır. Ömür hesaplamaları **gerçek dünya saatine göre değil**, daima `bot.storyDayCounter` (**hikâye günü**) + mesaj sayacı hibritine göre işler:

| Katman | Kod Adı | Ne Saklar? | Ömür (TTL) | Örnekler |
| :-: | :--- | :--- | :--- | :--- |
| **0** | `DISCARD` *(Gürültü)* | Anlık sahne koreografisi, rutin mimik, dolgu eylem | **0 (Asla Kaydetme)** | *"Elini masaya koydu"*, *"Derin nefes aldı"*, *"Kahvesini karıştırdı"*, *"Pencereden baktı"* |
| **1** | `EPHEMERAL` *(Kısa Süreli)* | Gündelik ama 1–3 gün içinde atıf yapılabilecek geçici detaylar | **2–3 Hikâye Günü** *(veya hikâye günü atlamazsa 35 mesaj)* | *"Dün akşam kremalı mantarlı makarna yedi"*, *"Bugün siyah ceket giydi"*, *"Başı hafif ağrıyor"*, *"Anahtarı sehpada bıraktı"* |
| **2** | `ARC` *(Orta Vadeli Olay)* | Devam eden sahne/görev arkına bağlı geçici durumlar | **10–14 Hikâye Günü** *(veya olay çözüldüğünde arşive)* | *"Sol omzundan yaralandı, sargılı"*, *"Liman deposu operasyonu planlanıyor"*, *"İki günlüğüne küsler"* |
| **3** | `CORE` *(Kalıcı Çekirdek)* | Kimlik, sırlar, kalıcı tercihler, ilişkiler, travmalar, büyük yeminler | **Sonsuz** *(Sadece çelişki/AI CRUD veya kullanıcı silmesiyle değişir)* | *"Kremalı makarnaya bayılır"*, *"5 yaşında ailesini kaybetti"*, *"Natasha'ya artık güveniyor"*, `★` kilitli kayıtlar |

---

## 🎯 BÖLÜM 2: BAĞLAMSAL ÖNEM HESABI (NE ZAMAN GÜRÜLTÜ, NE ZAMAN ÖNEMLİ?)

Yapay zeka kelimelere değil, **olayın hikâyedeki sonucuna ve duygusal ağırlığına** bakarak karar vermelidir. Küratör sistem promptuna (`extractAndCommitTurnMemories`) ve sohbet içi tool açıklamalarına aşağıdaki **Bağlamsal Önem Karşılaştırma Kurallarını** net biçimde enjekte et:

### ⚖️ Zorunlu Bağlamsal Eleme Rubriği (Küratör Promptuna Ekle)
* **KURAL 1 — Sıradan Fiziksel Eylem Yasağı (`DISCARD`):**
  * Sahneyi ileri taşımayan, gelecek turlarda hiçbir sonuç doğurmayacak jestler (*"elini masaya koydu"*, *"arkasına yaslandı"*, *"gülümsedi"*, *"kapıyı kapattı"*) **KESİNLİKLE kaydedilmez**.
* **KURAL 2 — Bağlamsal Yükseltme İstisnası (Aynı Eylem Önemliyse Kaydet!):**
  * Aynı fiziksel hareket, sahnenin kaderini, güveni, suçu veya duygusal sınırı değiştiriyorsa **önemlidir ve kaydedilir**:
    * ❌ *"Elini masaya koydu."* → **AT (`DISCARD`)**
    * ✅ *"Elini masadaki gizli dosyaya koyup kanıtı sakladı."* → **KAYDET (`ARC` veya `CORE`)**
    * ❌ *"Elini omzuna koydu ve yürümeye devam etti."* → **AT (`DISCARD`)**
    * ✅ *"Yirmi yıl sonra ilk kez birine güvenip elini elinin üstüne koydu, geri çekilmedi."* → **KAYDET (`CORE` / `Living Memory`)**
    * ❌ *"Yemek yedi."* (Detaysız) → **AT (`DISCARD`)**
    * ✅ *"Akşam sarımsaklı acılı makarna yaptı, birlikte yediler."* → **KAYDET (`EPHEMERAL`, `ttl_days: 3`)**
* **KURAL 3 — Süre ve Önem Puanlaması:**
  * Küratör JSON çıktısındaki her `vault_ops` (`add` / `update`) işlemine şu 3 hafif alanı zorunlu kıl:
    ```json
    {
      "op": "add",
      "subject": "Kullanıcı",
      "key": "Akşam Yemeği",
      "value": "Sarımsaklı kremalı mantarlı makarna yedi",
      "tier": "ephemeral",
      "ttl_days": 3,
      "importance": 4
    }
    ```
  * Kod tarafı emniyet kilidi: `importance <= 2` gelirse (model gevezelik etmiş olsa bile) **veritabanına hiç yazmadan çöpe at**. `tier == "ephemeral"` ise `ttl_days`'i `2..4` aralığında sınırla (`coerceIn(2, 4)`).

---

## 🎯 BÖLÜM 3: DOĞAL UNUTMA (TTL REAPER) VE ALIŞKANLIĞA OTOMATİK TERFİ

### 1. Doğal Unutma Motoru (`MemoryLifecycleReaper`)
Eski geçici kayıtların Neural Vault'u ve RAG aramalarını kirletmesini önlemek için otomatik temizleyici kur:
* **Tetiklenme Zamanı:**
  1. `TimePerceptionEngine` hikâye gününü her ilerlettiğinde (`storyDayCounter` arttığında),
  2. Her 5 mesajda bir hafif yerel kontrol olarak (`<2 ms`, `0 AI token`).
* **Unutma Koşulu:**
  * `★ userCorrected == true` (kullanıcının sabitlediği) veya `tier == CORE` kayıtlar **ASLA** otomatik unutulmaz.
  * `tier == EPHEMERAL` kayıt: `(currentStoryDay > expiresStoryDay)` **VEYA** `(currentMessageCount > expiresMessageIndex)` koşulu sağlanırsa otomatik unutulur.
  * `tier == ARC` kayıt: `(currentStoryDay > expiresStoryDay)` **VE** son 10 günde RAG tarafından hiç çağrılmadıysa unutulur.
* **Sıfır Veri Kaybı (Soft-Archive):** Unutulan kayıt silinip yok edilmez; `ArchivedMemoryEntity` tablosuna `archivedReason = "natural_decay_ttl"` olarak taşınır, aktif tablodan düşer ve `MemoryVectorCache` anında güncellenir.

### 2. Tekrar Kullanımla Kalıcıya Terfi (İnsan Beyni Pekiştirme Kuralı)
* Geçici (`EPHEMERAL`) bir kayıt, süresi dolmadan **2 veya daha fazla kez** sohbette yeniden anılır/RAG tarafından seçilirse (`accessCount >= 2`) — örneğin kullanıcı 2 gün üst üste *"Yine dünkinden yap"* dediyse:
  * Ömrü uzatılır veya `ARC`/`CORE` katmanına terfi ettirilip tercihe dönüştürülür (*"Kremalı makarnayı sık sık istiyor / çok seviyor"*).
  * Böylece bir kez yenen yemek 3 gün sonra unutulurken, gerçekten sevilen bir alışkanlık kalıcı hafızaya yerleşir!

---

## 🎯 BÖLÜM 4: "BANA DÜNKİNDEN YAP" — GÖRECELİ ZAMAN VE GÖNDERİM ÇÖZÜMLEYİCİ

Kullanıcı `"Bana dünkinden yap"`, `"Sabahki kahveden getir"`, `"Geçen gün giydiğin ceket"`, `"Aynısından bir daha"` dediğinde kelime bazlı klasik arama ("makarna" kelimesi geçmediği için) başarısız olur. Bunu **Hibrit + AI ortaklığıyla** %100 çözeceksin:

### 🛠️ Nasıl Çalışacak?
1. **Göreceli Zaman & Gönderim Algılayıcı (`RelativeTimeCueResolver` — Saf Kotlin, `<1 ms`):**
   * Son kullanıcı mesajında Türkçe zamansal/anaphoric ipuçlarını yakala ve hedef hikâye gününü hesapla:

```kotlin
data class TemporalTarget(
    val targetStoryDayMin: Long,
    val targetStoryDayMax: Long,
    val isAnaphoricRef: Boolean // "dünkinden", "aynısından", "ondan", "geçen seferki"
)

fun resolveTemporalTarget(userText: String, currentStoryDay: Long): TemporalTarget? {
    val t = TurkishTextAnalyzer.normalize(userText)
    return when {
        Regex("""\b(dün|dünkü|dünki|dünkinden|dünkü\w*)\b""").containsMatchIn(t) ->
            TemporalTarget((currentStoryDay - 1).coerceAtLeast(1L), (currentStoryDay - 1).coerceAtLeast(1L), true)
        Regex("""\b(evvelsi\s+gün|iki\s+gün\s+önce|geçen\s+gün)\b""").containsMatchIn(t) ->
            TemporalTarget((currentStoryDay - 3).coerceAtLeast(1L), (currentStoryDay - 1).coerceAtLeast(1L), true)
        Regex("""\b(sabahki|az\s+önceki|bugünkü|deminki|aynısından|aynısı|ondan\s+yap)\b""").containsMatchIn(t) ->
            TemporalTarget((currentStoryDay - 1).coerceAtLeast(1L), currentStoryDay, true)
        else -> null
    }
}
```

2. **Hibrit Aday Havuzunda Zaman Destekli Yükseltme (Temporal Boost):**
   * `resolveTemporalTarget()` eşleşirse:
     * **Asla Saf Hibrit'te kesme** — bu turu zorunlu olarak **Ortak (Hibrit + AI)** yoluna yönlendir!
     * `createdStoryDay in targetStoryDayMin..targetStoryDayMax` aralığındaki tüm aktif `EPHEMERAL` ve `ARC` kayıtlarını (yemek, giysi, mekân, olay vb.) otomatik olarak Hibrit'in ilk 15 aday listesinin tepesine yerleştir (`temporalBoost = +0.55f`).
   * *Kritik İncelik:* Eğer kullanıcı `"dün"` dediği halde hikâye takvimi henüz gün atlamamışsa (`currentStoryDay == 1`), `createdStoryDay == currentStoryDay` olan son 25 mesajlık `EPHEMERAL` kayıtlarını da aday havuzuna dahil et ki takvim atlaması olmasa bile `"dünkinden"` yine çözülsün!

3. **Zaman Etiketli Mikro-İndeks ve Prompt Sunumu:**
   * Hem Utility Model seçicisine hem de ana sohbet modeline her `EPHEMERAL` ve `ARC` kaydını insan diliyle göreceli zaman etiketiyle ver:
     ```text
     [#42 | Dün (Gün #4) | Geçici](Akşam Yemeği): Sarımsaklı kremalı mantarlı makarna yaptılar
     [#45 | Bugün sabah | Geçici](Kıyafet): Siyah deri ceket giyiyor
     ```
   * Bu sayede kullanıcı *"Bana dünkinden yap"* dediğinde, ortak AI seçici `#42`'yi anında seçer, ana model de hiç bocalamadan *"Sarımsaklı kremalı makarnayı kastediyorsun değil mi?"* diyerek sahneyi kusursuz sürdürür.

---

## 🎯 BÖLÜM 5: DOMİNO ETKİSİ & VERİTABANI UYUMLULUĞU (JAR KORUMASI)

* **Önce Kontrol Et:** `MemoryFactEntity.kt` ve `MemoryFactDao.kt` kaynak kod olarak proje dizininde düzenlenebilir mi, yoksa `precompiled_missing.jar` içinde mi kilitli?
  * **Seçenek A (Kaynak kod açıksa):** `MemoryFactEntity`'ye `memoryTier`, `createdStoryDay`, `expiresStoryDay`, `expiresMessageIndex`, `accessCount` alanlarını ekle ve temiz `MIGRATION_47_48` yaz.
  * **Seçenek B (Kaynak kod JAR içinde kilitliyse — Sıfır Şema Kırılması):** Tıpkı `LivingMemoryCodec` gibi bir **`MemoryLifecycleCodec`** yaz ve ömür bilgisini `MemoryFactEntity.confidence` alanında geriye dönük uyumlu formatta sakla:
    * Örnek: `"certain"` (eski kayıtlar otomatik `CORE` sayılır)
    * Örnek: `"certain|t:ephemeral|cd:4|ed:7|em:95|hit:1"`
    * `MemoryLifecycleCodec.parseConfidenceBase()` mevcut `"certain" / "inferred" / "disputed"` kontrollerinin tamamını eksiksiz korumalıdır; böylece tek bir eski test veya DAO sorgusu bile kırılmaz.

---

## 🎯 BÖLÜM 6: NEURAL VAULT UI DOKUNUŞU (MOR CAM UYUMLU)

`NeuralVaultModal.kt` tasarım dilini bozmadan şu 3 sade iyileştirmeyi ekle:
1. **Kısa Süreli Bellek Rozeti:** `EPHEMERAL` kartlarında ince yarı saydam lavanta-kehribar bir kapsül göster: `⏳ Geçici · 2 gün kaldı` (veya `⏳ Geçici`). `ARC` kartlarında `◈ Aktif Ark` rozeti göster.
2. **Tek Tıkla Kalıcıya Sabitle (`★`):** Kullanıcı geçici bir kaydın yanındaki `★` ikonuna bastığında `userCorrected = true` ve `tier = CORE` olsun; böylece otomatik unutma devre dışı kalsın.
3. **Arşiv Sekmesinde Unutma Nedeni:** `"Arşiv"` sekmesinde süresi dolup unutulan kayıtlar `"Doğal Unutma"` etiketiyle listelensin; kullanıcı isterse tek tıkla **"Geri Yükle"** diyerek kaydı kalıcı olarak geri getirebilsin.

---

## 🎯 BÖLÜM 7: ZORUNLU TEST SENARYOLARI VE TESLİMAT KONTROL LİSTESİ

Aşağıdaki **5 senaryoyu** `MemoryEmotionRegressionTest.kt` içine birim testi olarak ekle ve tamamının geçtiğini doğrula:
1. **Gürültü Eleme Testi:** *"Elini masaya koydu ve pencereden dışarı baktı"* gibi önemsiz sahne cümlelerinin `DISCARD` / `importance <= 2` nedeniyle Neural Vault'a **kaydedilmediğini** doğrula.
2. **Bağlamsal İstisna Testi:** *"Elini masadaki şifreli dosyanın üzerine koyup kanıtı gizlice ceketine sakladı"* gibi olay örgüsünü değiştiren eylemin `ARC`/`CORE` olarak **kaydedildiğini** doğrula.
3. **"Bana Dünkinden Yap" Hatırlama Testi:** Gün #4'te `EPHEMERAL (ttl=3)` olarak kaydedilen *"akşam sarımsaklı kremalı makarna yedi"* bilgisinin, Gün #5'te kullanıcı *"Bana dünkinden yap"* dediğinde (mesajda "makarna" geçmese bile!) `RelativeTimeCueResolver` + Ortak Hibrit+AI RAG tarafından **en üst sıradan yakalandığını** doğrula.
4. **2–3 Gün Sonra Otomatik Unutma Testi:** Hikâye takvimi Gün #4'ten Gün #8'e ilerlediğinde (`+4 gün`), pekiştirilmemiş `"makarna"` kaydının `MemoryLifecycleReaper` tarafından aktif RAG'dan çıkarılıp `archived_memories` tablosuna `natural_decay_ttl` ile taşındığını doğrula.
5. **Pekiştirme -> Kalıcıya Terfi & `★` Kilit Testi:** Süresi dolmadan 2+ kez referans verilen geçici kaydın veya `★` kilitli kaydın Gün #10'da bile unutulmadığını doğrula.

### ✅ Teslimat Kontrol Listesi
1. **[ ] Sıradan hareketler engelleniyor mu?** *"Elini şuraya koydu"* gibi gürültüler Neural Vault'u şişirmiyor; ancak hikâye için kritik eylemler kaydediliyor mu?
2. **[ ] Geçici anılar 2–3 hikâye günü yaşayıp otomatik unutuluyor mu?** Süresi dolan kayıtlar sessizce `archived_memories`'e gidip `MemoryVectorCache`'ten temizleniyor mu?
3. **[ ] `"Bana dünkinden yap"` eksiksiz çalışıyor mu?** Göreceli zaman ve gönderim çözümleyici dünkü geçici kaydı yakalayıp AI'ya `[Dün | Geçici]` etiketiyle sunuyor mu?
4. **[ ] Sık tekrarlanan geçici detaylar kalıcı alışkanlığa terfi ediyor mu?**
5. **[ ] Sıfır ekstra API çağrısı ve sıfır şema/UI kırılması sağlandı mı?** `./gradlew testDebugUnitTest` ve `./gradlew assembleDebug` temiz geçiyor mu?