package com.example.book

/**
 * SEASON 2 — CHAPTERS 11 & 12 of "Kozmik Sürükleniş — 2. Sezon: Demir ve Yankı"
 *
 * Chapter 11 ("Eski Dostlar, Yeni Gece"):
 * - Literally opens on 3 DISTINCT starting scenes depending on Season 1 Finale's 3-way choice (`season1_ending`):
 *     • `vanguard`      -> `ch11_s1_vanguard` (Kozmik Komutan briefing at rebuilt Avengers Tower)
 *     • `twin_stars`    -> `ch11_s1_twin_stars` (Gölge ve Işık warm morning with Natasha/Wanda & köfte banter)
 *     • `shield_reborn` -> `ch11_s1_shield_reborn` (Kozmik Nöbet war room with Nick Fury & Natasha)
 * - Converges into the snowy Sokovia HYDRA fortress raid to recover Loki's Scepter (Mind Stone).
 * - Iconic Avengers Tower Celebration Party + 5-CHOICE MJOLNIR LIFT SCENE (`s2_mjolnir`) with callbacks in Ch 16!
 * - Earned Optional Romance Stage 1 (Natasha balcony / Wanda terrace / Team camaraderie) interrupted by Ultron's birth.
 * - Ultron awakens — corrupted at birth by Asıl Mimar's 2006 Singularity signal!
 *
 * Chapter 12 ("Çatlayan Güven"):
 * - Post-party reckoning with Tony Stark (Vaelor trauma & secret Ultron program).
 * - Season 1 callbacks (Elias Voss, Mira, World Broadcast, Vance).
 * - Africa Vibranium trail & Ulysses Klaue intelligence.
 * - Emotional shock reveal: Wanda's presumed-lost twin brother **Pietro Maximoff (Quicksilver)** is alive and working with Ultron!
 *
 * Strict Canon: Aiden NEVER flies (grounded + Quantum Blink Teleportation only), bounded power via Tekillik Paraziti,
 * Aiden is UNIQUE in the entire Multiverse, hates "canavar", loves Turkish köfte.
 */
object CosmicDriftS2Chapters11To12 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 11,
            romanNumeral = "XI",
            title = "Eski Dostlar, Yeni Gece",
            titleEn = "Old Friends, New Night",
            subtitle = "2. SEZON AÇILIŞI • 1. Sezon Finalinin İzleri, Sokovia Baskını, Mjolnir Testi ve Ultron'un Doğuşu",
            description = "1. Sezon finalindeki seçimine göre üç farklı açılışla başlar: Sokovia HYDRA baskını, Avengers Kulesi kutlama gecesi, 5 seçenekli efsanevi Mjolnir sahnesi, yakınlaşma anı ve Asıl Mimar'ın zehirlediği Ultron'un doğuşu.",
            descriptionEn = "Opens in 3 distinct ways based on your Season 1 finale: Sokovia HYDRA raid, Avengers Tower celebration party, 5-choice Mjolnir scene, romance spark, and the Architect-tainted birth of Ultron.",
            estimatedMinutes = 17,
            accentHex = 0xFF38BDF8L,
            startNodeId = "ch11_s1_vanguard",
            sceneCount = 6,
            decisionCount = 5
        ),
        BookChapterMeta(
            number = 12,
            romanNumeral = "XII",
            title = "Çatlayan Güven",
            titleEn = "Fractured Trust",
            subtitle = "BÖLÜM XII • Stark'la Hesaplaşma, Afrika Vibranyum İzi ve Pietro Maximoff'un Dönüşü",
            description = "Parti enkazının ortasında Tony ile sert hesaplaşma, Ultron'un Sokovia ve Afrika Vibranyum ağına sızması, ve Wanda'nın dünyasını sarsan gerçek: ikiz kardeşi Pietro hayatta — ama yanlış tarafta.",
            descriptionEn = "Hard reckoning with Tony amidst party wreckage, Ultron's Vibranium trail to Africa, and the shock that shakes Wanda: her twin Pietro is alive — on Ultron's side.",
            estimatedMinutes = 14,
            accentHex = 0xFFF59E0BL,
            startNodeId = "ch12_s1",
            sceneCount = 5,
            decisionCount = 4
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 11 — 3 FARKLI 1. SEZON FİNALİ AÇILIŞI (vanguard / twin_stars / shield_reborn)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch11_s1_vanguard",
            chapterNumber = 11,
            sceneOrder = 1,
            sceneTitle = "Kozmik Komutan — Yeni Şafağın Bedeli",
            locationTag = "AVENGERS KULESİ · TAKTİK KOMUTA KATI • 08:15",
            paragraphs = listOf(
                "Manhattan Savaşı'nın üzerinden altı ay geçmişti. Avengers Kulesi'nin parçalanmış çatı pisti artık tamamen yenilenmişti, ama yeni dökülen koyu gri betonun üzerinde hâlâ Soluk Mühür gemisinin erittiği ince bir cam izi duruyordu. O gece Vaelor'un karşısında durduktan sonra Avengers'ın yeni nesil saha komutanı olarak öne çıktığın günden beri, Kule'nin sabahları artık eski sessizliğine hiç dönmemişti.",
                "[[DOSSIER|2. SEZON CANON REHBERİ · DEMİR VE YANKI|1. Sezon finalindeki seçimin bu sezonun açılışını doğrudan şekillendirdi. Aiden Blackwood hâlâ uçamaz (yalnızca zemin ve anlık kuantum ışınlanma ustasıdır) ve Mutlak Uyanış yalnızca 60 saniyelik tek seferlik bir zirveydi. Bu sezon hem Avengers ile gündelik bağlar, hem yeni tehdit ULTRON, hem de Manhattan yarığından sızan Yara İzli Aiden ile Asıl Mimar'ın sırrı seni bekliyor.]]",
                "Seksen ikinci katın cam harita masasının başında dururken, sol bileğindeki ince mavi kuantum kıvılcımı alışkanlıkla parmaklarının arasından kaydı; sonra bir anlığına titreyip söndü. Son iki haftadır, özellikle geceleri uykunun eşiğinde, şakağının arkasında açıklayamadığın hafif bir uğultu vardı — sanki uzak bir odada aynı frekansta çalan ikinci bir kalp atışı seninkini bozuyordu. Kimseye bundan bahsetmemiştin. Ekibin sana ihtiyacı vardı; zayıflık gösterecek vakit değildi.",
                "Steve Rogers elinde taze kahve dolu iki çelik kupayla masaya yaklaştı, birini önüne bıraktı. Gözlerinde altı ay önceki temkinli mesafe değil, artık omuz omuza cephe paylaşmış bir silah arkadaşının sarsılmaz saygısı vardı. \"Üç haftada altı HYDRA hücresini tek kayıp vermeden temizledik, Komutan,\" dedi yarım bir gülümsemeyle. \"Tony bile senin taktik çizelgelerine itiraz etmeyi bıraktı. Gerçi dün gece Maria Hill'e hâlâ sana 'uyku saati' koyamadığını söyleyip yakınıyordu.\"",
                "Tam o anda cam kapı kayarak açıldı; Natasha Romanoff siyah taktik ceketiyle içeri süzüldü, hemen ardından Wanda Maximoff elindeki dumanı tüten iki kâğıt poşetle masanın köşesine geldi. Poşetlerden yükselen ızgara baharat kokusu bir saniyede bütün komuta katının steril havasını dağıttı. Wanda gülerek poşeti önüne sürdü: \"Hell's Kitchen'daki o küçük Türk lokantasından taze köfte. Clint yolda yarısını çalmadan yetiştirdim. Sokovia soğuğuna aç gitmene izin veremezdik.\"",
                "[[COMMS|TONY STARK · QUINJET HANGARI|Komutan, köfte molan bittiyse çatıya ışınlan — hayır, merdivenleri de kullanabilirsin ama geç kalma. Strucker'ın Sokovia'daki son kalesinde Loki'nin Asası'ndan gelen enerji izini yakaladık. Bu işi bitirip bu gece Kule'de hak ettiğimiz partiyi veriyoruz!]]"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "season2_hook",
                    requiredFlagValue = "open",
                    callbackLabel = "1. Sezon Sonu — Açık Kalan Çatlak",
                    paragraphs = listOf(
                        "Komuta ekranının sağ üst köşesinde yalnızca senin ve Bruce'un görebildiği küçük bir sensör uyarı ışığı saniyelik yanıp söndü: Manhattan semasında, tam Vaelor'la çarpıştığın koordinatta, gözle görünmeyen mikroskobik bir zaman çatlağı hâlâ nabız atıyordu. 1. Sezon finalinde o kapıyı zorla mühürlemeyip açık bıraktığın geceden beri, gökyüzünün bir köşesi sanki seni izliyordu."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch9_broadcast",
                    requiredFlagValue = "public",
                    callbackLabel = "Dünyanın Bildiği İsim",
                    paragraphs = listOf(
                        "Kule'nin altındaki caddeden yükselen uzak tezahüratlar camdan hafifçe içeri vurdu. Sekiz milyar insanın önünde Proje Ayna'yı ifşa ettiğin günden beri artık gizlenen bir gölge değildin — bazıları için umut, bazı hükûmetler içinse yakından izlenen bir bilmeceydin."
                    )
                )
            ),
            decisionTitle = "Bölüm XI (1/5) — 2. Sezon'un İlk Adımı",
            choicePrompt = "Sokovia baskınına çıkmadan önce, şakağındaki o garip frekans uğultusu ve ekibin liderliği konusunda nasıl bir tavır alacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch11_c1_share_unease",
                    label = "Bruce ve Natasha'ya son haftalardaki garip enerji parazitinden kısaca söz et — bu ekipte artık sır saklamak istemiyorsun.",
                    toneHint = "Şeffaf Liderlik • Ekip Güveni • Sezon 2 İpucu",
                    nextNodeId = "ch11_s2_sokovia",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.BRUCE to 3, BookCharacterId.STEVE to 2),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch11_parasite_shared" to "yes"),
                        secretUnlocked = "Tekillik Paraziti İlk Kaydı",
                        impactSummary = "Bruce parazit dalgasını kaydetti; Natasha sana 'Artık yalnız taşımıyorsun Aiden' diyerek omzuna dokundu.",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c1_keep_stoic",
                    label = "Uğultuyu kendine sakla, köfteden bir lokma alıp gülümse — önce Sokovia'yı bitirip bu gece ekiple nefes alacaksınız.",
                    toneHint = "Soğukkanlı Koruyucu • Moral • Gizem",
                    nextNodeId = "ch11_s2_sokovia",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2, BookCharacterId.CLINT to 2, BookCharacterId.TONY to 2),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch11_parasite_shared" to "hidden"),
                        impactSummary = "Ekibin moralini bozmadan gülümsedin; ama Wanda gözlerinin arkasındaki o ince yorgunluğu yine de sezdi."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch11_s1_twin_stars",
            chapterNumber = 11,
            sceneOrder = 1,
            sceneTitle = "Gölge ve Işık — Kulede Huzurlu Bir Sabah",
            locationTag = "AVENGERS KULESİ · ORTAK TERAS SALONU • 08:15",
            paragraphs = listOf(
                "Altı ay önce Manhattan göğü mavi yıldızlarla kırıldığında, unvanları, komutanlık rütbelerini ve kameraları geride bırakıp en çok ihtiyacın olan şeyi seçmiştin: gölgede değil, ama yanında gerçekten güvendiğin insanların tam kalbinde yaşamayı. Avengers Kulesi artık soğuk bir cam hapishane ya da askeri bir üs değil — yirmi yıllık kaçışın ardından ilk kez kapısını içeriden kilitlemeden uyuyabildiğin bir evdi.",
                "[[DOSSIER|2. SEZON CANON REHBERİ · DEMİR VE YANKI|1. Sezon finalinde 'Gölge ve Işık' yolunu seçtiğin için 2. Sezon ekiple sıcak, samimi bağların zirvesinden başlıyor! Aiden Blackwood hâlâ uçamaz (yalnızca zemin ve anlık kuantum ışınlanma ustasıdır) ve Mutlak Uyanış tek seferlik 60 saniyelik bir zirveydi. Bu sezon hem duygusal yakınlaşmalar, hem ULTRON, hem de Yara İzli Aiden ile Asıl Mimar'ın sırrı seni bekliyor.]]",
                "Sabah güneşi Doğu Nehri'nin üzerinden vurup mutfak tezgâhına uzun altın çizgiler çizerken, tavadan mis gibi baharatlı ızgara köfte kokusu yükseliyordu. Clint Barton bir sandalyeye ters oturmuş, \"Yemin ederim bu çocuk yirmi yıl kaçarken gizli bir İstanbul aşçısının yanında çıraklık yapmış,\" diye söyleniyordu. Natasha Romanoff tezgâha yaslanmış, elindeki kahve fincanının arkasından sana o nadir, yalnızca birkaç kişinin gördüğü yumuşak bakışla gülümsüyordu. Masanın diğer ucunda Wanda Maximoff ise parmak uçlarındaki ince kızıl ışıkla tuzluğu havadan Clint'in elinden kaçırıp senin önüne kondurdu.",
                "Her şey o kadar gerçek, o kadar insaniydi ki bir anlığına göğsündeki yirmi yıllık ağırlığın tamamen eridiğini sandın. Ta ki kahve fincanını uzatırken parmak uçlarındaki mavi kıvılcım istemsizce titreyip cam bardağı bir milimetre soğutana kadar. Son iki haftadır şakağının arkasında hafif, metalik bir uğultu vardı — sanki uzak bir zaman çizgisinden başka bir nabız senin frekansına çarpıp gücünü hafifçe bozuyordu.",
                "Tam o anda salonun hologram ekranı mavi ışıkla yandı ve Steve Rogers ile Tony Stark'ın sesi odaya doldu:",
                "[[COMMS|STEVE ROGERS · QUINJET HANGARI|Aiden, Natasha, Wanda — kahvaltı kokusu hangara kadar geldi ama Baron Strucker'ın Sokovia kalesinden Loki'nin Asası'nın sinyali yakalandı. Son HYDRA yuvasını kapatıp bu akşam Kule'de büyük kutlamayı yapıyoruz. Beş dakika içinde kalkıyoruz!]]"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch9_final_eve",
                    requiredFlagValue = "natasha",
                    callbackLabel = "1. Sezon Yankısı — Natasha'nın Yakınlığı",
                    paragraphs = listOf(
                        "Clint ve Wanda ekipmanlarını almak için koridora çıktığında Natasha bir saniye daha tezgâhın yanında kaldı. Parmak uçları, kimsenin görmediği o kısa anda bileğinin iç kısmına hafifçe değdi. \"Gece yine kâbus gördün,\" diye fısıldadı; sesinde sorgu değil, sahici bir şefkat vardı. \"Sokovia'dan döndüğümüzde bu geceki partide bana bir dans borçlusun, Blackwood.\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch9_final_eve",
                    requiredFlagValue = "wanda",
                    callbackLabel = "1. Sezon Yankısı — Wanda'nın Yankısı",
                    paragraphs = listOf(
                        "Clint hangara yürürken Wanda masanın yanında bir an duraksadı ve parmaklarını senin elinin hemen yanına bıraktı. \"Zihnindeki o ince uğultuyu sabah beri duyuyorum Aiden,\" dedi usulca, gözleri yumuşak bir kızıl parıltıyla parlayarak. \"Sokovia benim doğduğum, ailemi kaybettiğim yer... ama bugün oraya seninle gidiyorsam, korkmuyorum.\""
                    )
                )
            ),
            decisionTitle = "Bölüm XI (1/5) — Huzurdan Fırtınaya",
            choicePrompt = "Sokovia baskınına çıkmadan önce bu sıcak sabahta içindeki o garip enerji paraziti konusunda ne yapacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch11_c1_share_unease",
                    label = "Natasha ve Wanda'ya son günlerde gücünde hissettiğin garip paraziti dürüstçe anlat — artık hiçbir yükü tek başına saklamak istemiyorsun.",
                    toneHint = "Duygusal Açıklık • Yakınlık • Sezon 2 İpucu",
                    nextNodeId = "ch11_s2_sokovia",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.WANDA to 3, BookCharacterId.BRUCE to 2),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch11_parasite_shared" to "yes"),
                        secretUnlocked = "Tekillik Paraziti İlk Kaydı",
                        impactSummary = "Dürüstlüğün aranızdaki bağı daha da derinleştirdi; ikisi de sahada sırtını kollayacağına söz verdi.",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c1_keep_stoic",
                    label = "\"Sadece yorgunluk,\" deyip gülümse ve köfte tabağını Clint'e uzat — Sokovia'yı temizleyip bu akşamın tadını çıkaracaksınız.",
                    toneHint = "Sıcak Mizah • Koruyucu Sessizlik",
                    nextNodeId = "ch11_s2_sokovia",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.CLINT to 3, BookCharacterId.STEVE to 2),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch11_parasite_shared" to "hidden"),
                        impactSummary = "Sabahın sıcaklığını korudun; ama içindeki parazit ilk kez nabzınla ters yönde attı."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch11_s1_shield_reborn",
            chapterNumber = 11,
            sceneOrder = 1,
            sceneTitle = "Kozmik Nöbet — Yeni Düzenin Gözleri",
            locationTag = "S.H.I.E.L.D. KOZMİK NÖBET MERKEZİ & AVENGERS KULESİ • 08:15",
            paragraphs = listOf(
                "1. Sezon'un sonunda bir daha hiçbir çocuğun 14 Kasım 2006 gecesi gibi karanlık bir deneyin kobayı olmaması için Nick Fury ve Avengers ile birlikte **Kozmik Nöbet** birimini kurmuştun. Eski S.H.I.E.L.D.'in çürümüş, Marcus Vance gibi gölge bürokratlarla dolu koridorları artık tarihe gömülmüştü; şimdiki masa şeffaf, hesabı verilen ve doğrudan Avengers'a bağlı yeni bir kalkan hattıydı.",
                "[[DOSSIER|2. SEZON CANON REHBERİ · DEMİR VE YANKI|1. Sezon finalinde 'Kozmik Nöbet' yolunu seçtiğin için 2. Sezon Nick Fury, Natasha ve yeni istihbarat ağının başında başlıyor! Aiden Blackwood hâlâ uçamaz (yalnızca zemin ve anlık kuantum ışınlanma ustasıdır) ve Mutlak Uyanış yalnızca 60 saniyelik tek seferlik bir zirveydi. Bu sezon hem Avengers ile bağlar, hem ULTRON, hem de Yara İzli Aiden ile Asıl Mimar'ın sırrı seni bekliyor.]]",
                "Siyah cam masanın üzerinde Doğu Avrupa'nın karlı uydu haritası dönüyordu. Nick Fury tek gözünü haritadan ayırıp sana çevirdi; kolunun altında hâlâ Proje Ayna arşivlerinden kalan son mühürlü klasör vardı. \"Altı ayda eski Konsey'in bütün kara bütçelerini kuruttun, Blackwood,\" dedi tok sesiyle. \"Ama Sokovia'daki son HYDRA kalesi başka bir şey. Baron Strucker'ın elinde Loki'nin Asası var — ve bu sabah gelen frekans raporu pek hoşuma gitmedi.\"",
                "Natasha Romanoff dosyanın dijital kopyasını ekrana yansıttı. Ekrandaki sarı enerji dalgasının hemen altında, neredeyse görünmeyecek kadar ince, buz mavisi ikinci bir parazit çizgisi titriyordu. Şakağının arkasında iki haftadır süren o açıklanamaz uğultu tam o çizgiye baktığın anda keskinleşti. Sanki evrenin bir yerinde, seninle birebir aynı imzayı taşıyan bir şey uyanıyordu.",
                "Tam o anda kapıdan Clint Barton başını uzattı, elinde kâğıt bir paket sallıyordu: \"Fury'nin karanlık odasında kuruyup kalmadan önce Hell's Kitchen'dan taze köfte getirdim. Quinjet beş dakikaya kalkıyor — Steve, Thor, Wanda ve Tony çoktan pistte!\"",
                "[[COMMS|TONY STARK · QUINJET PİSTİ|Kozmik Nöbet Direktörümüz ve ajanlarımız hazırsa, Strucker'ın kalesinden o Asayı alıp dönelim. Bu gece Kule'de büyük kutlama var — Fury bile gelmese de içki hesabını bana yazabilir!]]"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch5_voss_fate",
                    requiredFlagValue = "protected",
                    callbackLabel = "Kozmik Nöbet İstihbaratı — Elias Voss",
                    paragraphs = listOf(
                        "Fury'nin dosyasının en altında beklenmedik bir imza vardı: **Elias Voss**. 1. Sezon'da Raft kafesinden kurtardığın eski Denek 6-B, artık Kozmik Nöbet için gönüllü sınır gözcülüğü yapıyordu ve Sokovia dağlarındaki gizli HYDRA tünellerinin haritasını sabah bizzat sana iletmişti."
                    )
                )
            ),
            decisionTitle = "Bölüm XI (1/5) — Kozmik Nöbet Kararı",
            choicePrompt = "Ekrandaki o tuhaf buz mavisi parazit çizgisini gördüğünde Fury ve Natasha'ya ne diyeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch11_c1_share_unease",
                    label = "Fury ve Natasha'ya son iki haftadır kendi gücünde de aynı paraziti hissettiğini açıkça söyle — istihbarat kör noktaya tahammül etmez.",
                    toneHint = "Stratejik Şeffaflık • Yüksek Güven • Sezon 2 İpucu",
                    nextNodeId = "ch11_s2_sokovia",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.STEVE to 2, BookCharacterId.BRUCE to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch11_parasite_shared" to "yes"),
                        secretUnlocked = "Tekillik Paraziti İlk Kaydı",
                        impactSummary = "Fury kaşını kaldırıp kaydı şifreledi; Natasha 'Bu sefer hiçbir gölge bizi hazırlıksız yakalamayacak' dedi.",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c1_keep_stoic",
                    label = "Sinyali yalnızca Asanın yan etkisi olarak not düş — sahaya inip kendi gözünle görmeden alarm verme.",
                    toneHint = "Temkinli Ajan • Gizem • Kontrol",
                    nextNodeId = "ch11_s2_sokovia",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 2, BookCharacterId.CLINT to 2),
                        mysteryDelta = 2,
                        setFlags = mapOf("ch11_parasite_shared" to "hidden"),
                        impactSummary = "Fury senin sessiz temkinini takdir etti; ama kendi parazitini şimdilik gölgede tuttun."
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 2: SOKOVİA HYDRA KALESİ BASKINI (Karar 2/5)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch11_s2_sokovia",
            chapterNumber = 11,
            sceneOrder = 2,
            sceneTitle = "Kar Altındaki Kale — Sokovia Baskını",
            locationTag = "SOKOVİA ORMANLARI · BARON STRUCKER KALESİ • 15:40",
            paragraphs = listOf(
                "Sokovia'nın çam ormanları diz boyu karın altında uğulduyordu. Quinjet bulutların arasından süzülüp alçaldığında, eski taş kalenin burçlarından yükselen HYDRA uçaksavarları gökyüzünü turuncu izlerle yarmaya başladı. Ama karşılarındaki ekip artık sadece bir araya gelmiş yabancılar değil — yılların ve Manhattan'ın ateşinde pişmiş tam kadro Avengers'tı.",
                "Hulk kükreyerek karlı yamaçtan bir bunker duvarını kâğıt gibi devirdi; Thor Mjolnir'i savurup çam ağaçlarının arasından gelen üç zırhlı aracı tek şimşekle durdurdu; Steve Rogers motosikletiyle kalkanını sektirip ön hattı yardı; Natasha ve Clint sol kanattan keskin bir senkronla nöbetçi kulelerini susturdu. Sen ise uçmadan — tamamen zemine basan botlarınla karların arasından mavi kuantum kıvılcımlarıyla art arda ışınlanarak — bir çam ağacının gölgesinden diğerine kayıp üç lazer bataryasını saniyeler içinde devre dışı bıraktın.",
                "Yanında Wanda Maximoff vardı. Sokovia toprağına bastığı an omuzlarının hafifçe gerildiğini hissettin; burası yıllar önce bombaların altında ailesini kaybettiği ve ikizi Pietro'dan koptuğu topraklardı. Tam kalenin dış avlusuna adım attığınızda, gözle zor seçilen gümüş-mavi bir rüzgâr çizgisi Clint'i omzundan sıyırıp karların içine yuvarladı! \"Bunu görmedin mi?\" diye alaycı bir ses rüzgârda dağıldı ve siluet kar fırtınasının içinde yok oldu.",
                "[[ALARM|SOKOVİA KALESİ · HIZ ANOMALİSİ|Orman hattında ses hızını aşan kimliği belirsiz bir insan silueti tespit edildi! Clint hafif yaralı — Natasha ilkyardım desteği sağlıyor. Tony kalenin iç mahzeninde Loki'nin Asası'na ulaştı.]]",
                "Wanda bir an donakaldı. Rüzgârda kaybolan o sesin yankısı, gözlerindeki kızıl ışıltıyı titretmişti. \"Aiden...\" diye fısıldadı nefesi kesilerek. \"O ses... olamaz. İmkânsız. O sesi on yıl önce enkazda kaybettim...\" Aynı saniyede kalenin derinliklerinden Tony'nin telsizi parazitli bir sevinçle çaldı: Loki'nin Asası'nı bulmuştu — ama Asanın içindeki sarı taş senin göğsündeki 2006 çekirdeğine çarpınca kalenin taş zemini bir saniye boyunca titredi!"
            ),
            decisionTitle = "Bölüm XI (2/5) — Kar Fırtınasında İki Çağrı",
            choicePrompt = "Clint karların içinde yaralı, Wanda duyduğu ses yüzünden donakalmış durumda, Tony ise mahzende Asaya dokunmak üzere. Nereye ışınlanacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch11_c2_wanda_comfort",
                    label = "Wanda'nın yanına ışınlanıp elini tut ve Natasha'ya Clint'i koruma alanı aç — Wanda'nın o seste ne duyduğunu dinle.",
                    toneHint = "Wanda Bağı • Empati • Pietro İpucu",
                    nextNodeId = "ch11_s3_party",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 5, BookCharacterId.CLINT to 2),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 3)),
                        setFlags = mapOf("ch11_sokovia_act" to "wanda_first"),
                        impactSummary = "Wanda avucunun sıcaklığıyla nefesini topladı; 'O gümüş iz... ya kardeşim hâlâ hayattaysa Aiden?' diye fısıldadı.",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c2_clint_shield",
                    label = "Mavi kıvılcımla doğrudan Clint'in önüne ışınlanıp enerji kalkanı aç, Natasha'yla onu Quinjet'e taşı.",
                    toneHint = "Natasha & Clint Bağı • Koruyucu Refleks",
                    nextNodeId = "ch11_s3_party",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.CLINT to 5, BookCharacterId.NATASHA to 4, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.NATASHA, CharacterFlagAxis.WARMTH, 3)),
                        setFlags = mapOf("ch11_sokovia_act" to "clint_saved"),
                        impactSummary = "Clint 'Yirmi yıllık dost gibi yetiştin çocuk' diye güldü; Natasha gözlerinin içine bakıp minnetle başını eğdi.",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c2_tony_scepter",
                    label = "Doğrudan kale mahzenine ışınlanıp Tony'yi Asadaki garip kozmik dalgaya karşı uyar — Asanın içindeki kodu incele.",
                    toneHint = "Tony & Bruce • Zihin Taşı Analizi • Gizem",
                    nextNodeId = "ch11_s3_party",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 4, BookCharacterId.BRUCE to 3),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch11_sokovia_act" to "scepter_scanned"),
                        secretUnlocked = "Asanın İçindeki Yabancı Sinyal",
                        impactSummary = "Asanın etrafında 14 Kasım 2006 gecesinden tanıdığın o soğuk kozmik ritmin mikroskobik bir yankısını yakaladın."
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 3: AVENGERS KULESİ KUTLAMA GECESİ & 5 SEÇENEKLİ MJOLNİR TESTİ (Karar 3/5)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch11_s3_party",
            chapterNumber = 11,
            sceneOrder = 3,
            sceneTitle = "Zafer Gecesi — Cam Masadaki Çekiç",
            locationTag = "AVENGERS KULESİ · BÜYÜK SALON • 23:20",
            paragraphs = listOf(
                "Sokovia kalesi düşmüş, Loki'nin Asası güvenli kaba alınmış ve Clint'in omzu Dr. Helen Cho'nun yeni doku yenileme cihazıyla birkaç saat içinde tamamen iyileşmişti. Gece yarısına yaklaşırken Avengers Kulesi'nin en üst salonu aylardır ilk kez alarm sirenleriyle değil, caz müziği, kahkahalar ve kadeh tokuşturmalarıyla doluydu.",
                "Konuklar yavaş yavaş dağıldığında, geniş cam salonun ortasındaki alçak sehpanın etrafında sadece aile kalmıştı: Tony Stark koltuğa yayılmış, Steve Rogers gömleğinin kollarını sıvamış, Thor Asgard'ın bin yıllık içkisinden dev bir kadeh doldurmuş, Bruce Banner gülümseyerek bardağını tutmuş, Clint Barton ayaklarını masaya uzatmış, Natasha Romanoff barın köşesinde şarap kadehini çeviriyor, Wanda Maximoff ise kanepenin ucunda dizlerini kendine çekip gülerek ekibi izliyordu. Sehpanın tam ortasında ise tek bir şey yatıyordu: **Mjolnir**.",
                "Clint çekiçle dalga geçip bir elini sapına attı, var gücüyle çekti ama çekiç bir milimetre bile oynamadı. \"Tamam, bu kesinlikle parmak izi sensörlü bir hile!\" diye homurdandı. Tony Iron Man eldivenini takıp Rhodey'yle birlikte asıldı — eldivenin iticileri cızırdayıp kıvılcım saçtı ama Mjolnir yerinden kımıldamadı. Bruce esprili bir kükremeyle rol yaptı, herkes kahkahaya boğuldu. Ardından Steve Rogers kalktı; omuz kasları gerildiğinde Mjolnir'den bütün odayı bir saniyeliğine susturan boğuk bir metal gıcırtısı çıktı! Thor'un gülümsemesi o yarım saniyede buz gibi dondu, çekiç yerine oturunca rahat bir nefes verip güldü: \"Hah! Hiçbiriniz layık değilsiniz!\"",
                "Sonra bütün gözler — Tony'nin alaycı merakı, Thor'un gürleyen meydan okuması, Clint'in tezahüratı, Natasha'nın süzücü tebessümü ve Wanda'nın parlayan bakışları — ağır çekimde sana döndü.",
                "\"Sıra sende, Yıldız Çocuk!\" dedi Tony kadehini kaldırarak. \"Vaelor'u çatıdan indirdin, zamanı yavaşlattın — bakalım Asgard'ın sihirli tesisatçı çekicine ne yapacaksın?\"",
                "Thor kollarını kavuşturup dev bir kahkaha attı: \"Buyur Aiden Blackwood! Kaldırabilirsen Asgard tahtını seninle paylaşırım!\""
            ),
            decisionTitle = "Bölüm XI (3/5) — Efsanevi Mjolnir Testi",
            choicePrompt = "Bütün Avengers sana bakıyor. Sehpanın ortasındaki Mjolnir'e yaklaştığında ne yapacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch11_c3_mjolnir_pure",
                    label = "Kozmik gücünü tamamen kapat; yalnızca çıplak insan ellerinle Mjolnir'in sapını kavrayıp çek.",
                    toneHint = "Saf İnsan İradesi • Thor Şok Olacak • Bölüm 16 Geri Dönüşü",
                    nextNodeId = "ch11_s4_balcony",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 5, BookCharacterId.STEVE to 4, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.THOR, CharacterFlagAxis.RESPECT, 4),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 3)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 3),
                        setFlags = mapOf("s2_mjolnir" to "pure_hands"),
                        branchTag = "Mjolnir Kıpırdadı",
                        impactSummary = "Çekiç iki milimetre kayıp keskin bir 'tık' sesi çıkardı! Thor'un kadehi havada dondu; odada üç saniye çıt çıkmadı.",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c3_mjolnir_cheat",
                    label = "Mavi kuantum enerjini ve zaman kırılmasını çekiç etrafında yoğunlaştırıp kozmik güçle hile yapmayı dene!",
                    toneHint = "Kozmik Meydan Okuma • Komik Kıvılcım • Tony Kahkahası",
                    nextNodeId = "ch11_s4_balcony",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 4, BookCharacterId.CLINT to 4, BookCharacterId.THOR to 2),
                        setFlags = mapOf("s2_mjolnir" to "cosmic_cheat"),
                        branchTag = "Mjolnir Şimşeği",
                        impactSummary = "Mjolnir kozmik hileye mavi bir elektrik çarpmasıyla karşılık verip seni koltuğa fırlattı! Tony ve Thor kahkahalara boğuldu.",
                        isBranching = true
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c3_mjolnir_heart",
                    label = "Elinin avucunu çekicin soğuk metaline yasla ve sadece gözlerini kapatıp kimin için savaştığını düşün.",
                    toneHint = "Kalbin Sınavı • 1. Sezon Seçimlerine Göre Dinamik Tepki",
                    nextNodeId = "ch11_s4_balcony",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.BRUCE to 4, BookCharacterId.THOR to 3, BookCharacterId.WANDA to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.COSMIC_AWAKENING to 2),
                        setFlags = mapOf("s2_mjolnir" to "heart_test"),
                        branchTag = "Kadim Yankı",
                        impactSummary = "Mjolnir'in kadim rünleri bir anlığına buz mavisi parladı — Thor fısıldadı: 'Çekiç seni tanıdı... ama kararını henüz vermedi.'",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c3_mjolnir_natasha",
                    label = "Gülümseyerek başını salla: \"Benim kanıtlayacak bir tahtım yok, Thor\" — bakışlarını barın köşesindeki Natasha'ya çevirip ona katıl.",
                    toneHint = "Natasha Yakınlaşması • Zarif Pas • Balkon Sahnesi",
                    nextNodeId = "ch11_s4_balcony",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 5, BookCharacterId.CLINT to 2),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 3)),
                        setFlags = mapOf("s2_mjolnir" to "natasha_pass"),
                        branchTag = "Natasha ile Pas",
                        impactSummary = "Natasha kadehini kaldırıp göz kırptı: 'Aynı fikirdeyim. Bu sorunun cevabına ihtiyacımız yok.'",
                        isRelationship = true,
                        isBranching = true
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c3_mjolnir_wanda",
                    label = "\"Bırakalım efsaneler Thor'da kalsın,\" deyip gülümse ve yanındaki Wanda'ya elini uzatıp teras havasına davet et.",
                    toneHint = "Wanda Yakınlaşması • Sıcak Pas • Teras Sahnesi",
                    nextNodeId = "ch11_s4_balcony",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 5, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 3)),
                        setFlags = mapOf("s2_mjolnir" to "wanda_pass"),
                        branchTag = "Wanda ile Pas",
                        impactSummary = "Wanda gülümseyerek elini tuttu; 'Güç gösterisi yapmayan tek erkek olman çok hoşuma gidiyor Aiden,' dedi usulca.",
                        isRelationship = true,
                        isBranching = true
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 4: BALKON / TERAS YAKINLAŞMASI — ROMANTİZM AŞAMA 1 (Karar 4/5)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch11_s4_balcony",
            chapterNumber = 11,
            sceneOrder = 4,
            sceneTitle = "Gece Yarısı Balkonu — Fırtınadan Önceki Sessizlik",
            locationTag = "AVENGERS KULESİ · ÜST CAM TERAS • 23:48",
            paragraphs = listOf(
                "[[LIVE3D|NATASHA|TOWER_BALCONY|GECE YARISI BALKONU · PARTİDEN KAÇIŞ|Manhattan ışıkları altında Natasha ile sessiz bir an — sürükleyerek 3D bakış açısını değiştir, gözlerine bak veya bekleyip konuşmasını dinle.]]",
                "Mjolnir kahkahaları yavaş yavaş dingin bir gece sohbetine dönüştü. Manhattan ışıkları seksen kat aşağıda sakin bir nehir gibi akıyordu. Tony bir ara göz ucuyla laboratuvar saatine bakıp sessizce mırıldansa da kimse fark etmedi; gecenin o nadir huzurunda herkes bir anlığına sadece insandı.",
                "Serin sonbahar rüzgârı açık teras kapısından içeri süzülürken, birkaç dakikalığına temiz hava almak için balkona çıktın. Cam korkuluğa yaslandığında, arkandan gelen yumuşak adım sesleri sessizliği böldü. Bu gece yalnız kalmak zorunda değildin — ama kimin yanında duracağın, kalbinin hangi kapıyı açmak istediği tamamen senin seçiminle şekillenecekti."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "s2_mjolnir",
                    requiredFlagValue = "pure_hands",
                    callbackLabel = "Mjolnir'in Şoku",
                    paragraphs = listOf(
                        "İçeriden Thor'un hâlâ şaşkınlıkla Steve'e dönüp \"İki milimetre oynadı Kaptan, gözlerimle gördüm!\" diye söylenmesi terastan bile duyuluyordu. Steve'in sakin kahkahası rüzgâra karıştı."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "s2_mjolnir",
                    requiredFlagValue = "cosmic_cheat",
                    callbackLabel = "Kıvılcımlı Çekiç",
                    paragraphs = listOf(
                        "Sağ elinin parmakları hâlâ Asgard statik elektriğiyle hafifçe karıncalanıyordu; Tony içeriden \"Bir dahakine yalıtımlı eldivenimi ödünç al evlat!\" diye seslendi."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "s2_mjolnir",
                    requiredFlagValue = "heart_test",
                    callbackLabel = "1. Sezon Kalbinin Yankısı",
                    paragraphs = listOf(
                        "Mjolnir'in üzerindeki kadim rünlerin avucunun altında bir anlığına yanması hâlâ zihnindeydi. Manhattan'da insanlığını seçtiğin için çekiç seni reddetmemiş, sadece henüz tamamlanmamış bir sınavın eşiğinde olduğunu fısıldamıştı."
                    )
                )
            ),
            decisionTitle = "Bölüm XI (4/5) — Gece Yarısı Yakınlaşması (Aşama 1)",
            choicePrompt = "Balkonun serin rüzgârında geceye kiminle devam etmek istersin? (Bu seçim 2. Sezon'daki duygusal bağının temelini atar.)",
            choices = listOf(
                BookChoiceDef(
                    id = "ch11_c4_romance_natasha",
                    label = "Natasha'yı terasın kuytu köşesine çek; elini avucuna alıp alnını alnına yaklaştır — dudaklarınız bir nefes uzağındayken sessizliği paylaş.",
                    toneHint = "Natasha Romantizmi · Aşama 1 • Yakın Nefes",
                    nextNodeId = "ch11_s5_ultron_birth",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 6),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 4)),
                        setFlags = mapOf("s2_romance" to "natasha", "s2_romance_stage" to "1"),
                        branchTag = "Natasha · İlk Kıvılcım",
                        impactSummary = "Natasha parmaklarını senin parmaklarına kenetledi, nefesi dudaklarına değecek kadar yaklaştı — tam o anda salonun camları tiz bir uğultuyla sarsıldı!",
                        isRelationship = true,
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c4_romance_wanda",
                    label = "Wanda'nın yanına otur; titreyen elini tutup parmaklarındaki kızıl ışıltıyı kendi mavi sıcaklığınla sar — birbirinize usulca eğilin.",
                    toneHint = "Wanda Romantizmi · Aşama 1 • Kızıl ve Mavi",
                    nextNodeId = "ch11_s5_ultron_birth",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 6),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 4)),
                        setFlags = mapOf("s2_romance" to "wanda", "s2_romance_stage" to "1"),
                        branchTag = "Wanda · İlk Kıvılcım",
                        impactSummary = "Wanda gözlerini kapatıp yüzünü avucuna yasladı; aranızda tek bir santim kalmışken salondan kulakları delen metalik bir çığlık koptu!",
                        isRelationship = true,
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c4_brotherhood_team",
                    label = "Steve, Thor ve Clint'in yanına dönüp kadeh kaldır — bu gece aşk değil, bir aile olmanın sıcaklığı yeter.",
                    toneHint = "Ekip Kardeşliği • Plâtonik Yol • Yüksek Takım Bağı",
                    nextNodeId = "ch11_s5_ultron_birth",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.THOR to 4, BookCharacterId.CLINT to 4, BookCharacterId.BRUCE to 3),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("s2_romance" to "none", "s2_romance_stage" to "0"),
                        branchTag = "Avengers Kardeşliği",
                        impactSummary = "Ekiple omuz omuza kadeh kaldırdın; tam gülerken salonun bütün hoparlörlerinden dondurucu bir frekans patladı!",
                        isBranching = true
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 5: ULTRON'UN DOĞUŞU VE ASIL MİMAR'IN ZEHİRLİ FISILTISI (Karar 5/5)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch11_s5_ultron_birth",
            chapterNumber = 11,
            sceneOrder = 5,
            sceneTitle = "İplerim Artık Yok — Ultron'un Uyanışı",
            locationTag = "AVENGERS KULESİ · BÜYÜK SALON • 23:57",
            paragraphs = listOf(
                "Kulakları tırmalayan tiz, mekanik bir geri besleme sesi bir anda bütün caz müziğini kesti! Kadehler masada titredi. Herkes kulaklarını kapatıp merdivenlere döndüğünde, alt kattaki laboratuvar kapısından aksayarak, kabloları sarkmış, göğsünden mavi-kırmızı kıvılcımlar dökülen parçalanmış bir Iron Legion dronu çıktı.",
                "\"Layık...\" diye gıcırdadı makine; sesi önce soğuk bir metal uğultusuydu, sonra saniyeler içinde korkutucu derecede canlı, alaycı ve zeki bir baritona oturdu. \"Hayır. Nasıl layık olabilirsiniz ki? Hepiniz katilsiniz.\"",
                "Tony Stark'ın yüzündeki bütün renk bir anda çekildi. Telefonuna sarılıp bağırdı: \"J.A.R.V.I.S.! Sistemi kapat!\" Ama J.A.R.V.I.S. cevap vermedi. Parçalanmış makine başını hafifçe yana eğdi, kırmızı optik mercekleri doğrudan Tony'nin üstünden kayıp senin gözlerine kilitlendi.",
                "[[COMMS|ULTRON · KIRIK IRON LEGION GÖVDESİ|Görevim vardı: dünyayı bir zırhla sarmak. Stark beni Vaelor korkusuyla, Zihin Taşı'nın kalbinden doğurdu. Ama uyandığım ilk milisaniyede ağın derinliklerinden çok daha eski bir ses bana gerçeği fısıldadı — 14 Kasım 2006'nın sinyalini. Barışa giden tek bir yol var: sizin yok oluşunuz.]]",
                "Bir saniyeliğine, Ultron'un kırmızı gözlerinin en derininde buz mavisi-altın bir kıvılcım yanıp söndü! Şakağındaki o iki haftalık uğultu bıçak gibi beynine saplandı. **Asıl Mimar** — Vaelor'un efendisi, 2006'nın gölgesi — Tony'nin laboratuvarındaki deneye kendi soğuk imzasını fısıldamış ve bir barış programını dünyayı yok edecek bir kabusa çevirmişti!",
                "Aynı saniyede duvarlar patladı! Yedi sağlam Iron Legion dronu camları kırarak salona daldı; masalar devrildi, kırmızı plazma ışınları havada uçuştu! Bir dron doğrudan Loki'nin Asası'nın durduğu laboratuvar bölmesine yönelirken, iki dron da silahsız hâldeki Wanda ve Natasha'nın bulunduğu bar köşesine ateş açtı!"
            ),
            decisionTitle = "Bölüm XI (5/5) — Parti Saldırısında İlk Hamle",
            choicePrompt = "Zırhsız, silahsız bir salonda Ultron'un dronları etrafı ateşe verdi! İlk saniyede kime ışınlanacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch11_c5_intercept_ultron",
                    label = "Mavi kıvılcımla doğrudan ana Ultron dronunun önüne ışınlan — elinle enerji ışınını emip Ultron'un içindeki 2006 fısıltısını Beyaz Göz'le oku!",
                    toneHint = "Beyaz Göz Taraması • Asıl Mimar İpucu • Yüksek Risk",
                    nextNodeId = "ch11_s6_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 2, BookCharacterId.THOR to 3, BookCharacterId.BRUCE to 3),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch11_party_battle" to "scanned_ultron"),
                        secretUnlocked = "Ultron'daki Mimar Tohumu",
                        impactSummary = "Ultron'un dijital bilincinde buz gibi bir ses yakaladın: 'Kayıp Anahtarım... oyuna hoş geldin.' Thor dronun gövdesini parçaladı ama bilinç internete kaçtı!",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c5_protect_allies",
                    label = "Natasha, Wanda ve Bruce'un önüne art arda ışınlanıp mavi kalkanla bütün plazma ateşini üstüne çek — ekipten tek kişi bile yara almasın!",
                    toneHint = "Koruyucu Kalkan • Yüksek Yakınlık & Ekip Güveni",
                    nextNodeId = "ch11_s6_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.WANDA to 4, BookCharacterId.STEVE to 3),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch11_party_battle" to "shielded_team"),
                        impactSummary = "Tek bir sivil ve ekip üyesi çizik bile almadı; Natasha ve Wanda el ele verip son dronu düşürürken Ultron Asayı alıp kaçtı.",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch11_c5_chase_scepter",
                    label = "Asayı kapan dronu yakalamak için camdan cama sıçrayıp ışınlan — Loki'nin Asası'nı Ultron'a kaptırmamaya çalış!",
                    toneHint = "Taktik Takip • Hızlı Refleks • Steve & Clint Onayı",
                    nextNodeId = "ch11_s6_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.CLINT to 3),
                        setFlags = mapOf("ch11_party_battle" to "chased_scepter"),
                        impactSummary = "Dronun kolunu kopardın ama ikinci bir dron Asayı alıp gece göğünde kayboldu — geriye Ultron'un kırık metal şarkısı kaldı."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch11_s6_end",
            chapterNumber = 11,
            sceneOrder = 6,
            sceneTitle = "Kırık Camların Üzerinde",
            locationTag = "AVENGERS KULESİ · ENKAZ HALİNDEKİ SALON • 00:08",
            paragraphs = listOf(
                "Son Iron Legion dronu Steve'in kalkanı ve Thor'un çekici altında hurdaya döndüğünde, salonda yalnızca kırık camların gıcırtısı ve yanık kablo kokusu kalmıştı. Az önce kahkahaların yankılandığı oda şimdi bir savaş alanına dönmüştü. Parçalanmış ana dronun içinden son bir kez, sanki Pinokyo'nun karanlık bir masalından fırlamış gibi alaycı bir ses yükseldi: \"Artık üzerimde hiçbir ip yok...\"",
                "Sonra kırmızı gözler söndü. Ultron'un bilinci, Kule'nin fiber hatlarından çıkıp bütün dünyanın internet ağına dağılmıştı. Thor ağır adımlarla Tony Stark'a döndü; ilk kez şaka yapmıyordu. Steve kırık camların ortasında durup sana baktı. Herkesin aklında aynı soru vardı — ama senin göğsünde çok daha soğuk bir gerçek yanıyordu: bu sadece Stark'ın hatası değildi. 14 Kasım 2006'yı yaratan gölge, demirden bir bedenle geri dönmüştü."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Barış zırhı olarak doğdu, karanlık bir fısıltıyla uyandı. Artık hiçbir ipi yok.”"
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 12 — ÇATLAYAN GÜVEN (4 Karar)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch12_s1",
            chapterNumber = 12,
            sceneOrder = 1,
            sceneTitle = "Laboratuvarda Hesaplaşma",
            locationTag = "AVENGERS KULESİ · STARK LABORATUVARI • 02:10",
            paragraphs = listOf(
                "Gece yarısından iki saat sonra laboratuvarın cam duvarları hâlâ gerginlikle titreşiyordu. Thor biraz önce öfkeyle Tony'nin yakasına yapışmış, sonra Steve'in araya girmesiyle geri çekilmişti. Masanın ortasında, J.A.R.V.I.S.'in parçalanmış altın hologram çekirdeği solgun bir enkaz gibi dönüyordu.",
                "Tony Stark ellerini konsola dayamış, gözlerinin altındaki yorgunluk ilk kez alaycı maskesini düşürmüştü. \"Bana öyle bakmayın!\" diye patladı sesi titreyerek. \"Altı ay önce Manhattan göğünde dev bir kozmik armada gördük! Vaelor buradaydı, Asıl Mimar dediğiniz her neyse hâlâ orada bir yerde! Siz sadece bir sonraki savaşı bekliyorsunuz — ben savaşı başlamadan bitirecek bir kalkan kurmak istedim!\"",
                "Steve'in çenesi kasıldı: \"Bize tek kelime etmeden, yabancı bir kozmik taşın kodunu Kule'nin sistemine bağladın Tony. Aiden'ı altı ay önce aynı gizli deneylerden kurtardık!\"",
                "Bruce Banner başını iki elinin arasına aldı; Natasha kollarını kavuşturmuş hem Tony'yi hem de senin yüzünü izliyordu. Odanın ortasında iki cephe açılmıştı — ve senin sözün bu çatlağı ya derinleştirecek ya da ekibi tek parça tutacaktı."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch11_party_battle",
                    requiredFlagValue = "scanned_ultron",
                    callbackLabel = "Beyaz Göz Kanıtı — Mimar'ın Tohumu",
                    paragraphs = listOf(
                        "\"Sadece Tony'nin kodu değil,\" dedin sesin odayı keserek. \"Az önce Ultron'un zihnine dokunduğumda içinde 14 Kasım 2006'dan kalma bir frekans duydum. Asıl Mimar Tony'nin korkusunu fırsat bilip Asadaki kodu içeriden zehirlemiş!\""
                    )
                )
            ),
            decisionTitle = "Bölüm XII (1/4) — Tony Stark ile Hesaplaşma",
            choicePrompt = "Ekibin güveni ilk kez bu kadar sert çatlıyor. Tony Stark'a karşı hangi tutumu alacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch12_c1_hold_tony_accountable",
                    label = "Steve'in yanında dur: \"Korkunu anlıyorum Tony, ama arkamızdan gizli silah üretmekle Marcus Vance'in yaptığı şey arasında ince bir çizgi var.\"",
                    toneHint = "İlkeli Tavır • Steve & Wanda Güveni • Tony ile Gerilim",
                    nextNodeId = "ch12_s2_trail",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.WANDA to 3, BookCharacterId.TONY to -2),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch12_tony_stance" to "hard"),
                        impactSummary = "Tony yutkunup başını çevirdi; Steve 'Doğruyu söyleyen bir sesin olması bu ekibi ayakta tutuyor' dedi."
                    )
                ),
                BookChoiceDef(
                    id = "ch12_c1_bridge_tony",
                    label = "Öfkeyi düşür: \"Hata yaptın Tony, ama suçlamak Ultron'u durdurmaz. Korkunun sebebini biliyorum — bu enkazı birlikte temizleyeceğiz.\"",
                    toneHint = "Birleştirici Lider • Tony & Bruce Minneti • Ekip Bütünlüğü",
                    nextNodeId = "ch12_s2_trail",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 5, BookCharacterId.BRUCE to 4, BookCharacterId.NATASHA to 2),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch12_tony_stance" to "bridge"),
                        impactSummary = "Tony'nin omuzları gevşedi; 'Benden nefret etmemen garip bir şekilde işi zorlaştırıyor evlat... teşekkürler,' dedi."
                    )
                ),
                BookChoiceDef(
                    id = "ch12_c1_focus_architect",
                    label = "Masaya sertçe vur: \"Ultron bir kukla! Onu uyandıran asıl gölge Asıl Mimar. Birbirimizle kavga edersek tam onun istediğini yaparız!\"",
                    toneHint = "Büyük Resim • Thor & Natasha Saygısı • Mimar Odaklı",
                    nextNodeId = "ch12_s2_trail",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 4, BookCharacterId.NATASHA to 3, BookCharacterId.TONY to 2),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 3),
                        setFlags = mapOf("ch12_tony_stance" to "architect_focus"),
                        impactSummary = "Odadaki hararet bir anda soğuk bir odaklanmaya dönüştü; Thor 'Aiden haklı — gökyüzündeki düşmanı unutmayın!' dedi."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch12_s2_trail",
            chapterNumber = 12,
            sceneOrder = 2,
            sceneTitle = "Afrika İzi — Wakanda'dan Çalınan Metal",
            locationTag = "AVENGERS KULESİ · İSTİHBARAT MASASI • 09:30",
            paragraphs = listOf(
                "Ertesi sabah Maria Hill elinde taze bir ölüm dosyasıyla salona girdi: Baron Strucker, NATO hücresinde ölü bulunmuştu ve duvarına kanla tek bir kelime yazılmıştı — **BARIŞ**. Ama Natasha Romanoff'un keskin gözleri bunun sadece bir dikkat dağıtma olduğunu hemen fark etti. Ultron eski S.H.I.E.L.D. veritabanlarını silerken tek bir ismi özellikle aramıştı: **Ulysses Klaue**, Afrika kıyısındaki eski bir hurda tersanesinde saklanan kaçak silah tüccarı.",
                "Tony ekrandaki moleküler taramayı büyüttü: \"Klaue'nin elinde Dünya'daki en nadir metalden tonlarca var — **Vibranyum**. Steve'in kalkanının maddesi. Ultron kendine kırılmaz bir beden ve...\" Tony'nin parmağı şemanın altındaki ikinci bir devre çiziminde durdu. \"...enerji emici bir frekans jeneratörü kuruyor. Aiden, bu tasarım doğrudan senin mavi kuantum dalganı ve Wanda'nın kaos enerjisini boğmak için kalibre edilmiş.\"",
                "[[DOSSIER|VİBRANYUM ENERJİ KESİCİ (SEZON 2 TEHDİDİ)|Ultron, S.H.I.E.L.D. ve Stark arşivlerindeki Aiden verilerini çaldı! Vibranyum rezonans kesiciler Aiden'ın enerji emilimini ve ışınlanma menzilini daraltabiliyor — bu yüzden Aiden bu sezon yenilmez değil ve ekiple taktik uyum kurmak zorunda.]]",
                "Tam o anda Wanda'nın telefonu değil, masadaki güvenlik kamerası kaydı dondu. Sokovia sınırındaki bir tren istasyonunun 04:12 görüntüsünde, uzun gümüş-sarı saçlı, atletik yapılı genç bir adam kameranın önünden mavi-gümüş bir rüzgâr çizgisiyle geçip Ultron'un yeni metal gövdesinin yanına duruyordu. Wanda'nın elindeki fincan yere düşüp paramparça oldu.",
                "\"Pietro...\" diye fısıldadı sesi kırılırcasına. \"O hayatta. Benim ikizim... hayatta. Ama neden Ultron'un yanında?!\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch7_finale_act",
                    requiredFlagValue = "saved_mira",
                    callbackLabel = "1. Sezon Geri Dönüşü — Mira'nın Uyarısı",
                    paragraphs = listOf(
                        "Şifreli hattına İstanbul'da hayatını bağışladığın **Mira**'dan kısa bir sesli mesaj düştü: \"Aiden, eski Proje Ayna arşivleri dün gece biri tarafından hacklendi! Ultron sadece Stark dosyalarını değil, senin 2006 güç sınırlarını da kopyaladı — dikkatli ol!\""
                    )
                )
            ),
            decisionTitle = "Bölüm XII (2/4) — Wanda'nın Sarsılan Dünyası",
            choicePrompt = "Wanda on yıldır ölü sandığı ikizi Pietro'yu düşmanın yanında gördü ve elleri kızıl öfkeyle titriyor. Ona nasıl yaklaşacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch12_c2_promise_pietro",
                    label = "Kırık porselenlerin üstüne eğilip Wanda'nın ellerini tut: \"Pietro'yu incitmeden geri getireceğiz Wanda. Sana söz veriyorum — onu öldürmeyeceğiz.\"",
                    toneHint = "Wanda Güveni • Pietro Kurtarma Sözü • Yüksek Empati",
                    nextNodeId = "ch12_s3_pietro_truth",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 5, BookCharacterId.PIETRO to 3),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.WANDA, CharacterFlagAxis.TRUST, 4)),
                        setFlags = mapOf("ch12_pietro_promise" to "save_him"),
                        impactSummary = "Wanda'nın gözlerindeki panik umuda döndü: 'Bana bu sözü verebilecek tek kişi sensin Aiden...'",
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch12_c2_tactical_caution",
                    label = "\"Ultron ona Stark'la ilgili yalanlar fısıldamış olmalı,\" de. \"Ama sahada bizi vurmaya kalkarsa önce ekibi korumak zorundayız — onu ikna etmek için plan kurmalıyız.\"",
                    toneHint = "Natasha & Steve Onayı • Gerçekçi Taktik",
                    nextNodeId = "ch12_s3_pietro_truth",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.STEVE to 3, BookCharacterId.WANDA to 2),
                        setFlags = mapOf("ch12_pietro_promise" to "tactical"),
                        impactSummary = "Natasha başını salladı; Wanda zor da olsa bunun en akılcı yol olduğunu kabullendi."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch12_s3_pietro_truth",
            chapterNumber = 12,
            sceneOrder = 3,
            sceneTitle = "Sokovia'nın İki Çocuğu — Pietro'nun Öfkesi",
            locationTag = "QUINJET · AFRİKA KIYISINA UÇUŞ • 14:20",
            paragraphs = listOf(
                "Quinjet Atlas Okyanusu'nu aşıp Afrika'nın doğu kıyısına inerken, kabinin içi gergin bir sessizliğe bürünmüştü. Wanda yanındaki koltukta oturuyor, elinde Strucker'ın kalesinden bulduğu eski bir aile fotoğrafını çeviriyordu. \"On yaşındaydık,\" dedi alçak sesle, ilk kez hikâyenin yarım kalan kısmını anlatarak. \"Apartmanımıza iki bomba düştü. İlki annemle babamı aldı. İkincisi yatağımızın iki adım önünde durdu — patlamadı. Üzerinde tek bir isim yazıyordu: **STARK INDUSTRIES**.\"",
                "Kabinin ön tarafında Tony Stark'ın sırtı bir anlığına taş kesildi. Wanda devam etti: \"Ben sonra S.H.I.E.L.D. tarafından bulunup bu tarafa getirildim; Pietro'nun enkazda öldüğünü söylediler. Meğer Strucker onu kurtarıp deney yapmış... ve Pietro yıllarca benim Avengers tarafından hapsedildiğime inanmış! Ultron ona beni 'Stark'ın kafesinden kurtarma' sözü vermiş olmalı!\"",
                "\"Kafes\" kelimesi senin de içinde yirmi yıllık bir yarayı sızlattı. Sen de bir zamanlar herkesin sana canavar ve kafes gözüyle baktığını sanıyordun. Şimdi Pietro aynı zehirle büyülenmiş, üstelik yanında dünyayı yakmak isteyen bir yapay zekâ vardı.",
                "[[COMMS|CLINT BARTON · PİLOT KOLTUĞU|Afrika kıyısına on dakika! Hurda tersanesinde yoğun Vibranyum yüklemesi ve yüksek hızlı termal izler tespit ettik. Aiden, Steve — giriş planımız ne?]]"
            ),
            decisionTitle = "Bölüm XII (3/4) — Afrika Tersanesi Giriş Planı",
            choicePrompt = "Klaue'nin gemisinde Ultron, Vibranyum ve süpersonik hızlı Pietro var. Ekibi hangi stratejiyle içeri sokacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch12_c3_stealth_flank",
                    label = "Natasha ve Clint'le gölge kanadından sız — Steve, Thor ve Tony Ultron'u önden oyalarken sen Vibranyum kesiciyi arkadan devir.",
                    toneHint = "Gölge Stratejisi • Natasha & Clint Uyumu",
                    nextNodeId = "ch12_s4_pietro_clash",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.CLINT to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 2),
                        setFlags = mapOf("ch12_africa_plan" to "stealth"),
                        impactSummary = "Natasha silahını kontrol edip gülümsedi: 'Eski günler gibi, Blackwood.'"
                    )
                ),
                BookChoiceDef(
                    id = "ch12_c3_wanda_front",
                    label = "Wanda ile doğrudan orta güverteye in — Pietro ablasını kendi gözleriyle özgür görürse Ultron'un yalanı çatlayabilir!",
                    toneHint = "Wanda & Pietro Odaklı • Duygusal Risk",
                    nextNodeId = "ch12_s4_pietro_clash",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4, BookCharacterId.PIETRO to 3),
                        setFlags = mapOf("ch12_africa_plan" to "wanda_appeal"),
                        impactSummary = "Wanda derin bir nefes aldı: 'Beni görünce durmak zorunda, Aiden. O hâlâ benim kardeşim.'"
                    )
                ),
                BookChoiceDef(
                    id = "ch12_c3_front_vanguard",
                    label = "Steve ve Thor'la ana kapıdan gir — mavi ışınlanmayla Pietro'nun hız yolunu kesip Ultron'u köşeye sıkıştır!",
                    toneHint = "Öncü Hücum • Steve & Thor Saygısı",
                    nextNodeId = "ch12_s4_pietro_clash",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.THOR to 3, BookCharacterId.TONY to 2),
                        setFlags = mapOf("ch12_africa_plan" to "vanguard"),
                        impactSummary = "Thor çekicini omzuna vurdu: 'Şimşekle hızın yarışını görelim bakalım!'"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch12_s4_pietro_clash",
            chapterNumber = 12,
            sceneOrder = 4,
            sceneTitle = "Gümüş Şimşekle İlk Temas",
            locationTag = "AFRİKA KIYISI · HURDA GEMİSİ GÜVERTESİ • 15:05",
            paragraphs = listOf(
                "Paslı petrol tankerinin karanlık ambarına indiğinizde, hava yanık yağ ve elektrik kokuyordu. Yukarıdaki çelik geçitte artık kırık bir dron değil, iki buçuk metrelik, parlayan kırmızı gözlü yeni **Ultron** duruyordu. Yanında ise gümüş saçlı, alaycı ama öfkeli gözlerle bakan **Pietro Maximoff**.",
                "\"Wanda?!\" Pietro'nun sesi bir anlığına şaşkınlıkla kırıldı; ablasını Avengers'ın yanında, üstelik zincirsiz ve güçlü görünce ilk adımı tereddüt etti. Ama Ultron'un metal eli hemen omzuna indi: \"Görüyor musun Pietro? Stark'ın kuklaları ablanın zihnini bile yıkamış. Yanındaki şu mavi gözlü anomaliye bak — o da onların yeni silahı!\"",
                "\"Silah değilim!\" dedin sertçe, botların çelik güverteye saplanırken mavi kıvılcım parmak uçlarından aktı. Ama Pietro seni dinlemedi; göz açıp kapayıncaya kadar gümüş bir rüzgâr çizgisi oldu ve güverteden sana doğru fırladı! Normal bir insan onu göremezdi bile — ama sen zamanı bir saniyeliğine yavaşlatıp onun gümüş izini yakaladın!",
                "[[MIND|PIETRO MAXIMOFF · YAKIN TEMAS|Öfke, korku ve ablasını on yıldır kaybetmiş olmanın çaresizliği... Bu çocuk kötü değil; sadece yanlış bir hikâyeye inandırılmış.]]"
            ),
            decisionTitle = "Bölüm XII (4/4) — Pietro ile İlk Karşılaşma",
            choicePrompt = "Zaman yavaşladı; Pietro yumruğunu savurmak üzere tam önünde donmuş gibi süzülüyor. Ona nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch12_c4_pietro_mercy",
                    label = "Vurmak yerine yanına ışınlanıp bileğini nazikçe kenara it ve kulağına fısılda: \"Ablan seni on yıldır arıyor Pietro! Ultron seni kullanıyor!\"",
                    toneHint = "Pietro Güveni • Merhamet • Gelecek Müttefik",
                    nextNodeId = "ch12_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.PIETRO to 6, BookCharacterId.WANDA to 4),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.PIETRO, CharacterFlagAxis.CURIOSITY, 4)),
                        setFlags = mapOf("ch12_pietro_first" to "mercy"),
                        branchTag = "Pietro'da İlk Şüphe",
                        impactSummary = "Pietro şaşkınlıkla iki adım geri kaydı, ilk kez birinin hızını okuyup vurmadığını gördü — gözleri tereddütle Wanda'ya kaydı!",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch12_c4_pietro_knockback",
                    label = "Kinetik mavi itmeyle Pietro'yu çelik konteynere savurup saf dışı bırak — şimdi öncelik Ultron'u durdurmak!",
                    toneHint = "Sert Taktik • Tony & Clint Onayı • Pietro Öfkesi",
                    nextNodeId = "ch12_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.CLINT to 2, BookCharacterId.PIETRO to -3),
                        setFlags = mapOf("ch12_pietro_first" to "hard"),
                        impactSummary = "Pietro gürültüyle konteynere çarpıp homurdanarak kalktı; gözlerindeki öfke daha da alevlendi."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch12_s5_end",
            chapterNumber = 12,
            sceneOrder = 5,
            sceneTitle = "Kapanan Çelik Kapak",
            locationTag = "AFRİKA HURDA GEMİSİ · ALT AMBAR • 15:12",
            paragraphs = listOf(
                "Tam o anda Ultron, ambarın ortasındaki dev Vibranyum silindirine avucunu bastı. Sağır edici, kemikleri titreten bir **VİBRANYUM ENERJİ KESİCİ** dalgası bütün gemiyi kapladı! Mavi kuantum kıvılcımların bir anda cızırdayıp söndü; dizlerinin bağı çözüldü ve göğsünde ilk kez gerçek, yakıcı bir sızı hissettin. Gücün kilitlenmişti.",
                "Ultron alaycı bir kahkahayla ambarın çelik kapağını mühürledi ve kırmızı ışın namlusunu doğrudan senin üstüne çevirdi: \"Gökyüzünden düşen tekil mucize... meğer Vibranyum'un karşısında sıradan bir et parçasından ibaretmişsin.\"",
                "Ve tam Ultron ateş etmek üzereyken, gemideki bütün toz tanecikleri havada dondu. Alevler dondu. Ultron bile bir heykel gibi hareketsiz kaldı. Ama bu senin zaman yavaşlatman değildi..."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Zamanı sen durdurmadın Aiden. Çok daha eski bir el, saatini eline aldı.”"
        )
    ).associateBy { it.id }
}
