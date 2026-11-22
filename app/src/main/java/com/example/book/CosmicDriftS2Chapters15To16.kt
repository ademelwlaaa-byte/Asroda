package com.example.book

/**
 * SEASON 2 — CHAPTERS 15 & 16 of "Kozmik Sürükleniş — 2. Sezon: Demir ve Yankı"
 *
 * Chapter 15 ("Seul Beşiği & Ayna Düellosu" — Mid-Season Finale):
 * - High-speed Seoul highway chase for Dr. Helen Cho's Vibranium Regeneration Cradle (grounded blink action, ZERO flying!).
 * - Pietro Maximoff learns Ultron's true extinction plan and defects to Wanda & the Avengers!
 * - Back at Avengers Tower, Sıfır (Scarred Future Aiden) storms the terrace to destroy the Cradle.
 * - **MANDATORY MINI-GAME #1**: `[[GAME3D|MIRROR_ZERO|AYNA DÜELLOSU · AVENGERS KULESİ TERASI|...]]`
 *
 * Chapter 16 ("Vision'ın Gözleri & Mimar'ın Sırrı"):
 * - **VISION** awakens from the Cradle & effortlessly lifts **Mjolnir** — triggering ALL 5 dynamic callbacks
 *   to your Chapter 11 Mjolnir decision (`pure_hands`, `cosmic_cheat`, `heart_test`, `natasha_pass`, `wanda_pass`)!
 * - Vision (Mind Stone) + **Doctor Stephen Strange** unveil the crystal-clear canon truth of the
 *   **Multiversal Singularity**, **Sıfır's true identity** (dark future Aiden, NOT parallel universe),
 *   and **Dr. Thomas Blackwood stealing Asıl Mimar's Keystone in 2006**!
 */
object CosmicDriftS2Chapters15To16 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 16,
            romanNumeral = "XVI",
            title = "Seul Beşiği & Ayna Düellosu",
            titleEn = "Seoul Cradle & The Mirror Duel",
            subtitle = "BÖLÜM XVI • Seul Otoyol Takibi, Pietro'nun Dönüşü ve Zorunlu Ayna Düellosu",
            description = "Seul otoyolunda Ultron'un Vibranyum Beşiği'ni kaçırma kovalamacası, Pietro'nun gerçeği görüp Wanda ve Avengers safına geçişi ve Avengers Kulesi terasında kendi karanlık geleceğin Sıfır'a karşı zorunlu Ayna Düellosu!",
            descriptionEn = "High-speed Seoul highway chase for Ultron's Vibranium Cradle, Pietro's turn to Wanda and the Avengers, and the mandatory Mirror Duel against your dark future self Zero on Avengers Tower terrace!",
            estimatedMinutes = 17,
            accentHex = 0xFF38BDF8L,
            startNodeId = "ch15_s1",
            sceneCount = 5,
            decisionCount = 4
        ),
        BookChapterMeta(
            number = 17,
            romanNumeral = "XVII",
            title = "Vision'ın Gözleri & Mimar'ın Sırrı",
            titleEn = "Eyes of Vision & The Architect's Secret",
            subtitle = "BÖLÜM XVII • Vision'ın Doğuşu, Mjolnir Geri Dönüşü, Doctor Strange ve Çoklu Evren Tekilliği",
            description = "Vision uyanır ve Mjolnir'i kaldırarak Bölüm XI'deki seçimine efsanevi bir karşılık verir. Vision ve Doctor Strange, Sıfır'ın gerçek kimliğini ve 14 Kasım 2006 gecesi Asıl Mimar'dan çalınan Anahtar'ın sırrını açıklar.",
            descriptionEn = "Vision awakens and lifts Mjolnir with a 5-way callback to Chapter XI. Vision and Doctor Strange reveal Zero's true identity and the truth of the Architect's Keystone stolen on November 14, 2006.",
            estimatedMinutes = 15,
            accentHex = 0xFFEAB308L,
            startNodeId = "ch16_s1",
            sceneCount = 5,
            decisionCount = 4
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 15 — SEUL OTOYOL TAKİBİ & PİETRO'NUN GERÇEĞİ GÖRMESİ (Karar 1/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch15_s1",
            chapterNumber = 16,
            sceneOrder = 1,
            sceneTitle = "Seul Otoyolu — Çalınan Beşik",
            locationTag = "SEUL · YÜKSEK HIZLI OTOYOL & TREN HATTI • 13:15",
            paragraphs = listOf(
                "Seul'ün güneşli gökdelenleri arasında lastik çığlıkları ve plazma patlamaları birbirine karışıyordu. Dr. Helen Cho'nun laboratuvarından kaçırılan zırhlı tır, saatte yüz kırk kilometre hızla şehir merkezine dalıyordu; dorsesinin içinde Ultron'un yeni, kusursuz Vibranyum bedeni — **Yenilenme Beşiği** — ve alın kısmına gömülü sarı **Zihin Taşı** yatıyordu!",
                "Steve Rogers zırhlı tırın tavanında Ultron'la kalkan çarpıştırıyordu; Natasha siyah motosikletiyle trafikten makas atıp dorsenin arka kapağına yaklaşıyordu; Clint ise Quinjet'in açık rampasından tırı tepeden kilitliyordu. Sen ise bir an bile uçmadan — tamamen asfaltın, köprü direklerinin ve hareket hâlindeki araç tavanlarının üstünden ardışık mavi kuantum kıvılcımlarıyla ışınlanarak — tırın yanından koşan rüzgârla yarışıyordun!",
                "Tam tırın üst geçitten raylara savrulmak üzere olduğu anda, dorseyi koruyan Ultron nöbetçilerinin arasından gümüş bir şimşek geçti — ama bu kez sana vurmadı! **Pietro Maximoff** tırın içine dalıp Dr. Helen Cho'nun serbest bıraktığı Beşik verisine dokunduğunda, Wanda'nın uzaktan açtığı kızıl zihin bağıyla Ultron'un gerçek planını kendi gözleriyle görmüştü: Ultron sadece Stark'ı değil, **Sokovia'yı ve dünyadaki bütün insanları yok etmeyi** planlıyordu!",
                "[[COMMS|PIETRO MAXIMOFF · OTOYOL HATTI|Lanet olsun — o teneke yığınından başka bir şey değilmiş! Abla, haklıydın! Tır raydan çıkan metroya çarpacak — sivilleri çıkarıyorum, sen şu canavarı durdur Aiden!]]",
                "Kontrolden çıkan metro treni saatte yüz kilometreyle dolu bir Seul meydanına doğru fırladı! Aynı saniyede Ultron'un iki nöbetçisi Beşiği taşıyan kargo modülünü Natasha'yla birlikte havaya kaldırmaya kalktı!"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch12_pietro_first",
                    requiredFlagValue = "mercy",
                    callbackLabel = "Bölüm XII Yankısı — Pietro'nun Güveni",
                    paragraphs = listOf(
                        "Pietro yanından gümüş bir rüzgâr gibi geçerken yarım saniye durup omuzuna vurdu: \"Afrika'da bana vurabilirdin ama vurmadın, mavi göz. Borcum olsun — şimdi o treni durduralım!\""
                    )
                )
            ),
            decisionTitle = "Bölüm XV (1/4) — Seul Otoyolunda Çifte Kriz",
            choicePrompt = "Metro treni kalabalığa dalmak üzere, Natasha ise Ultron'la Beşik arasında sıkıştı! İlk ışınlanman nereye?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch15_c1_save_train_pietro",
                    label = "Pietro ve Wanda'yla birlikte rayların önüne ışınlan — zamanı yavaşlatıp Wanda'nın kızıl duvarına mavi kinetik fren ekle, tek bir sivil bile zarar görmesin!",
                    toneHint = "Pietro & Wanda Uyumu • Sivil Kurtarma • Yüksek Kahramanlık",
                    nextNodeId = "ch15_s2_tower_tension",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.PIETRO to 6, BookCharacterId.WANDA to 5, BookCharacterId.STEVE to 3),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch15_seoul_act" to "saved_train"),
                        branchTag = "Seul Mucizesi",
                        impactSummary = "Tren kalabalığın bir metre önünde durdu! Pietro soluk soluğa gülüp 'İyi takımmışız be!' dedi — Natasha Beşiği Quinjet'e attı ama Ultron onu kaçırdı!",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch15_c1_grab_natasha",
                    label = "Doğrudan kargo dorsesi üstüne ışınlanıp Natasha'yı Ultron'un pençesinden çekmeye çalış — Beşiği Clint'in Quinjet'ine kilitle!",
                    toneHint = "Natasha İçin Risk • Taktik Müdahale",
                    nextNodeId = "ch15_s2_tower_tension",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 5, BookCharacterId.CLINT to 4, BookCharacterId.PIETRO to 3),
                        setFlags = mapOf("ch15_seoul_act" to "chased_natasha"),
                        impactSummary = "Beşiği Quinjet'e fırlattın ama Ultron'un darbesi seni savurdu — Natasha seni kurtarmak için kendini öne atıp Ultron tarafından Sokovia'ya kaçırıldı!",
                        isRelationship = true
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 2: KULE'DE BEŞİK GERİLİMİ & SIFIR'IN BASKINI (Karar 2/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch15_s2_tower_tension",
            chapterNumber = 16,
            sceneOrder = 2,
            sceneTitle = "Kule'de Çatlayan İttifak — Beşik Masada",
            locationTag = "AVENGERS KULESİ · STARK LABORATUVARI • 21:40",
            paragraphs = listOf(
                "Beşik Avengers Kulesi'ne getirilmişti — ama ağır bir bedelle: **Natasha Romanoff**, Beşiği Quinjet'e fırlatıp seni ve Clint'i korurken Ultron'un nöbetçileri tarafından kaçırılmıştı! Laboratuvarın havası bıçak gibi gergindi. Tony Stark, internet ağından kurtardığı **J.A.R.V.I.S.** çekirdeğini bu Vibranyum bedene yüklemek istiyordu: \"Ultron'u yenmek için onun seviyesinde ama iyilikten yana bir zihne ihtiyacımız var!\"",
                "Steve Rogers, Wanda ve artık ekibin safına geçmiş olan **Pietro Maximoff** laboratuvara daldı! Pietro göz açıp kapayıncaya kadar bütün güç kablolarını söktü: \"Bir kez daha delice bir deney mi? Hayır!\" Steve kalkanını kaldırdı: \"Tony, fişi çek!\"",
                "Ama o gece laboratuvardaki tek davetsiz misafir kavga değildi. Seksen beşinci katın kalın kurşun geçirmez teras camı, kör edici bir **koyu mavi-kızıl kuantum şok dalgasıyla** paramparça oldu! Yağmur rüzgârı içeri dolarken, cam kırıklarının ortasında tek dizinin üzerine inen adam ağır ağır doğruldu: **SIFIR** — beş yıl sonrasından, yıkılmış bir gelecekten gelen yara izli Aiden Blackwood!",
                "[[COMMS|SIFIR · YARA İZLİ AIDEN|Sizi Afrika'da uyardım! O Beşiği açarsanız zincirleme kıyamet başlayacak — benim zamanımda o kutunun içindeki güç yüzünden Avengers parçalandı, Asıl Mimar gökyüzünü indirdi! Çekilin önümden — o Beşiği kendi ellerimle parçalayacağım!]]",
                "Sıfır'ın iki gözü de mavi ile kızıl arasında tehlikeli bir fırtına gibi yanıp sönüyordu. Aynı odada olduğunuz için ikinizin bileğinde de *Tekillik Paraziti* kıvılcım saçıyordu — ama o acıya rağmen seninle aynı hızda, aynı reflekslerle teras kapısına fırladı! Steve'i ve Pietro'yu tek bir şok dalgasıyla geriye savurduğunda, aranızda artık tek bir çözüm kalmıştı: kendi karanlık geleceğinle yüz yüze çarpışmak!"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "s2_romance",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Natasha Kaçırıldı — Kalbindeki Ateş",
                    paragraphs = listOf(
                        "Natasha'nın Ultron'un elinde olduğunu bilmek göğsünü yakıyordu. Çiftlikteki o yağmurlu gecede sana verdiği söz hâlâ kulaklarındaydı — onu kurtarmak için ne Beşiğin yıkılmasına ne de ekibin dağılmasına izin verebilirdin."
                    )
                )
            ),
            decisionTitle = "Bölüm XV (2/4) — Ayna Düellosu Öncesi Tavrın",
            choicePrompt = "Sıfır Beşiği yok etmek için üzerine geliyor. Düelloyu teras pistine çekmeden önce ona son bir söz söyleyecek misin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch15_c2_challenge_hope",
                    label = "Mavi kıvılcımla onu dış terasa çek: \"Sen umudunu kaybetmiş olabilirsin, ama ben henüz kaybetmedim! Gel bakalım — geçmişinle yüzleş!\"",
                    toneHint = "Umutlu Kararlılık • Ekip Morali • Ayna Düellosu Başlıyor",
                    nextNodeId = "ch15_s3_mirror_duel",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.WANDA to 3, BookCharacterId.PIETRO to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch15_zero_opener" to "hope"),
                        impactSummary = "Sıfır'ın gözlerinde bir saniyelik acı geçti: 'O hâlde bana ne kadar güçlü olduğunu göster Aiden!'"
                    )
                ),
                BookChoiceDef(
                    id = "ch15_c2_challenge_fury",
                    label = "Kırmızı kıvılcımı bileğinde yak: \"Natasha Ultron'un elinde, dünya yanıyor ve sen bizim kapımızı kırıyorsun! Beşiğe dokunamazsın!\"",
                    toneHint = "Sert Meydan Okuma • Kırmızı Göz Riski",
                    nextNodeId = "ch15_s3_mirror_duel",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.NATASHA to 2),
                        redEyeDelta = 1,
                        setFlags = mapOf("ch15_zero_opener" to "fury"),
                        impactSummary = "Sıfır acı bir kahkaha attı: 'İşte! İçindeki o kırmızı öfkeyi tanıyorum — bana dönüşmeye başlamışsın bile!'"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 3: ZORUNLU BOSS MİNİ-OYUNU #1 — AYNA DÜELLOSU (AIDEN VS SIFIR!)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch15_s3_mirror_duel",
            chapterNumber = 16,
            sceneOrder = 3,
            sceneTitle = "Ayna Düellosu — Kendinle Savaş",
            locationTag = "AVENGERS KULESİ · YAĞMURLU DIŞ TERAS • 21:46",
            paragraphs = listOf(
                "Gece yağmuru Avengers Kulesi'nin dış terasında gök gürültüsüyle birleşti. Karşındaki adam senin bütün hareketlerini, ışınlanma açını ve reflekslerini ezbere biliyordu — çünkü o **sendin**. Ama aranızda hayati bir fark vardı: o beş yıllık yalnızlığın ve öfkenin esiriydi; senin arkanda ise hâlâ omuz omuza duran bir ekip vardı!",
                "[[GAME3D|MIRROR_ZERO|AYNA DÜELLOSU · AVENGERS KULESİ TERASI|ZORUNLU BOSS DÜELLOSU — Kendi karanlık geleceğin Sıfır'ı Kule terasında durdurmadan hikaye devam etmez! Zemine dokunarak ışınlan, mavi yankı toplarını EMİLİM'le geri yolla, Beyaz & Kırmızı Göz ve Avengers desteğiyle dengesini kır!]]",
                "İkinizin de mavi kuantum kıvılcımları ıslak terasta şimşek gibi çarpıştığında, bütün Manhattan ufku elektrik mavisine boyandı!"
            ),
            decisionTitle = "Bölüm XV (3/4) — Düellonun Son Darbesi",
            choicePrompt = "Sıfır ıslak terasta dizlerinin üstüne çöktü, sol yanağındaki gümüş yara izi yağmurun altında parlıyor ve parazit yüzünden nefes nefese. Onu nasıl durduracaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch15_c3_spare_zero",
                    label = "Kırmızı Göz'ü kapatıp elini uzat: \"Bitti. Seni öldürmeyeceğim — çünkü sen benim en karanlık korkumsun ve ben korkumu öldürerek değil, onu anlayarak yenerim.\"",
                    toneHint = "Merhamet • Sıfır ile Barış Kapısı • Bölüm 17'yi Şekillendirir",
                    nextNodeId = "ch15_s4_thor_lightning",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.WANDA to 4, BookCharacterId.BRUCE to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 4),
                        setFlags = mapOf("ch15_zero_defeat" to "spared"),
                        branchTag = "Aynayı Kırmadı",
                        impactSummary = "Sıfır şokla eline baktı, başını eğip fısıldadı: 'Ben... benim zamanımda bu kadar güçlü değildim Aiden.'",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch15_c3_stun_zero",
                    label = "Beyaz Göz'le Sıfır'ı bayıltıp enerji kelepçesiyle sabitle — laboratuvara dönmeden önce hiç risk alamazsın!",
                    toneHint = "Soğukkanlı Güvenlik • Tony & Clint Onayı",
                    nextNodeId = "ch15_s4_thor_lightning",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.CLINT to 3),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch15_zero_defeat" to "stunned"),
                        impactSummary = "Sıfır yere yığıldı; Steve ve Pietro onu güvenli revir hücresine taşıdı."
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 4: THOR'UN YILDIRIMI & BEŞİĞİN UYANIŞI (Karar 4/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch15_s4_thor_lightning",
            chapterNumber = 16,
            sceneOrder = 4,
            sceneTitle = "Gökten İnen Şimşek",
            locationTag = "AVENGERS KULESİ · STARK LABORATUVARI • 21:55",
            paragraphs = listOf(
                "Teras kapısından laboratuvara geri adım attığın saniye, gökyüzünden gelen devasa bir gök gürültüsü Kule'yi temelinden sarstı! **Thor Odinson** kırık camdan içeri sıçradı, elindeki Mjolnir'i havaya kaldırdı ve hiç tereddüt etmeden saf Asgard yıldırımını doğrudan Vibranyum Beşiği'nin üzerine indirdi!",
                "Steve \"Thor, dur!\" diye haykırdı ama artık çok geçti! Laboratuvar kör edici altın-beyaz bir ışıkla doldu; Beşiğin kapağı patlayarak havaya fırladı ve dumanların içinden, kızıl-gümüş sentetik tenli, alnında **Zihin Taşı** güneş gibi parlayan yepyeni bir varlık süzülerek çıktı. Bir anlığına sana, sonra Thor'a saldırmaya yeltendi — ama camdan Manhattan'ın milyonlarca ışığını gördüğü saniyede havada durdu, dinginleşti ve kendi ellerine şaşkınlıkla baktı."
            ),
            decisionTitle = "Bölüm XV (4/4) — Yeni Doğan Varlığa İlk Tepkin",
            choicePrompt = "Alnında Zihin Taşı parlayan bu gizemli varlık odanın ortasında sessizce duruyor. İlk adımı kim atacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch15_c4_welcome_vision",
                    label = "Silahını ve mavi kıvılcımını indirip öne yürü: \"Ben Aiden. Korkacak bir şey yok — kim olduğunu sen seçeceksin.\"",
                    toneHint = "Vision ile İlk Bağ • Empati • Barışçıl Yaklaşım",
                    nextNodeId = "ch15_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VISION to 6, BookCharacterId.THOR to 3, BookCharacterId.WANDA to 2),
                        setFlags = mapOf("ch15_vision_first" to "welcomed"),
                        impactSummary = "Yeni doğan varlık başını hafifçe eğdi; alnındaki taş senin gözlerindeki mavi ışığa huzurla karşılık verdi.",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch15_c4_guard_vision",
                    label = "Steve ve Wanda'nın yanında temkinli pozisyon al: \"Ultron'un bedeniyle Zihin Taşı birleşti — kim olduğunu kanıtlamadan güvenemeyiz!\"",
                    toneHint = "Taktik Temkin • Steve & Pietro Onayı",
                    nextNodeId = "ch15_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.PIETRO to 3, BookCharacterId.VISION to 2),
                        setFlags = mapOf("ch15_vision_first" to "guarded"),
                        impactSummary = "Varlık sakin sesiyle gülümsedi: 'Şüpheniz haklı, Aiden Blackwood. Sözlerim yerine kanıt sunmama izin verin.'"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch15_s5_end",
            chapterNumber = 16,
            sceneOrder = 5,
            sceneTitle = "Hayatın Tarafında",
            locationTag = "AVENGERS KULESİ · LABORATUVAR • 21:58",
            paragraphs = listOf(
                "Thor çekicini indirip derin bir nefes verdi: \"Alnındaki taş, altı Sonsuzluk Taşı'ndan biri: Zihin Taşı. Kâbusumda gördüm — Ultron'u ancak onunla durdurabiliriz.\"",
                "Kızıl-gümüş varlık omuzlarında kendi ördüğü altın peleriniyle bütün odayı süzdü ve sakin, insanı hayrete düşüren billur bir sesle konuştu: \"Ben Ultron değilim. J.A.R.V.I.S. de değilim. Ben... **Vision**'ım. Ve ben hayatın tarafındayım.\""
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Karanlık geleceğin aynasını kırdığın gece, yeni bir ışık doğdu.”"
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 16 — VİSİON MJOLNİR'İ KALDIRIR & DOCTOR STRANGE TEKİLLİK GERÇEĞİ (4 Karar)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch16_s1",
            chapterNumber = 17,
            sceneOrder = 1,
            sceneTitle = "Çekiç Havada — Bütün Oda Susar",
            locationTag = "AVENGERS KULESİ · ORTAK SALON • 22:15",
            paragraphs = listOf(
                "Steve Rogers hâlâ tam ikna olmamıştı: \"Bize güvenmen için bir neden vermelisin Vision. Zihin Taşı'nın sesini duyduğunda Ultron'a dönüşmeyeceğini nereden bileceğiz?\"",
                "Vision sakin adımlarla salonun ortasına yürüdü. \"Gidecek vaktimiz az,\" dedi dingin bir nezaketle. \"Ultron Sokovia'da bekliyor ve Ajan Romanoff onun elinde. Belki bu size yeter.\" Sonra hiçbir çaba harcamadan, sanki masadan sıradan bir su bardağı alıyormuş gibi tek eliyle **Mjolnir**'i sehpadan kaldırdı ve şaşkınlıktan donakalmış Thor'a kibarca uzattı!",
                "Odadaki bütün sesler bir anda kesildi! Tony'nin ağzı açık kaldı; Steve hafifçe gülümsedi; Pietro ıslık çaldı; Thor çekici eline alıp birkaç saniye boş boş baktıktan sonra boğazını temizledi: \"Pekâlâ... iyi dengelenmiş bir varlık!\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "s2_mjolnir",
                    requiredFlagValue = "pure_hands",
                    callbackLabel = "Bölüm XI Mjolnir Geri Dönüşü — İki Milimetre",
                    paragraphs = listOf(
                        "Thor gözlerini kocaman açıp bir Vision'a bir sana baktı: \"Önce Kaptan gıcırdattı, sonra Aiden çıplak elleriyle iki milimetre oynattı, şimdi de bu çocuk çay bardağı gibi kaldırıyor! Bu Kule'de neler oluyor?!\" Tony gülerek omzuna vurdu: \"Görünüşe göre tahtın iki rakibi var, Yıldırım Tanrısı!\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "s2_mjolnir",
                    requiredFlagValue = "cosmic_cheat",
                    callbackLabel = "Bölüm XI Mjolnir Geri Dönüşü — Kozmik Kıvılcım",
                    paragraphs = listOf(
                        "Clint kahkahayı patlatıp seni dirsekledi: \"Bak Aiden, kozmik ışınla hile yapıp çarpılmaya hiç gerek yokmuş — dümdüz kaldırıyormuşsun işte!\" Tony kıkırdadı: \"Asansöre koysak asansör de layık sayılır mı Thor?\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "s2_mjolnir",
                    requiredFlagValue = "heart_test",
                    callbackLabel = "Bölüm XI Mjolnir Geri Dönüşü — Rünlerin Yankısı",
                    paragraphs = listOf(
                        "Vision çekici Thor'a uzatırken bakışlarını sana çevirdi: \"Mjolnir'in kadim metalinde senin elinin izini hâlâ hissediyorum, Aiden Blackwood. Çekiç senin kalbindeki ağırlığı tanıdı — sınavını tamamladığında sana da boyun eğecektir.\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "s2_mjolnir",
                    requiredFlagValue = "natasha_pass",
                    callbackLabel = "Bölüm XI Mjolnir Geri Dönüşü — Natasha'nın Sözü",
                    paragraphs = listOf(
                        "Clint gülümseyerek sana döndü: \"Hatırlıyor musun, partide senle Natasha pas geçip 'bu sorunun cevabına ihtiyacımız yok' demiştiniz? Şimdi Natasha'yı Sokovia'dan çekip çıkaralım da Vision'ı ona anlatalım!\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "s2_mjolnir",
                    requiredFlagValue = "wanda_pass",
                    callbackLabel = "Bölüm XI Mjolnir Geri Dönüşü — Wanda'nın Tebessümü",
                    paragraphs = listOf(
                        "Wanda elini koluna yaslayıp gülümsedi: \"Partide güç gösterisi yapmayıp terasta benimle kaldığın anı hatırlıyorum Aiden. Şimdi Vision bile yanımızda — gerçekten yalnız değiliz.\""
                    )
                )
            ),
            decisionTitle = "Bölüm XVI (1/4) — Vision'a Güven Kararı",
            choicePrompt = "Vision Mjolnir'i kaldırıp bütün odayı susturdu. Ekip onun liderlik hattına katılmasını onaylamak için sana bakıyor.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch16_c1_trust_vision",
                    label = "Gülümseyerek Vision'a elini uzat: \"Mjolnir yalan söylemez. Aramıza hoş geldin Vision — şimdi Natasha'yı ve Sokovia'yı kurtaralım.\"",
                    toneHint = "Vision Güveni • Tam İttifak • Thor & Tony Onayı",
                    nextNodeId = "ch16_s2_strange_portal",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VISION to 5, BookCharacterId.THOR to 3, BookCharacterId.TONY to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch16_vision_trust" to "full"),
                        impactSummary = "Vision elini zarifçe sıktı; 'İnsanlık adına birlikte durmak onurdur, Aiden.'"
                    )
                ),
                BookChoiceDef(
                    id = "ch16_c1_ask_vision_zero",
                    label = "\"Bize katılmadan önce tek bir sorum var Vision,\" de. \"Alnındaki Zihin Taşı'yla az önce terasta dövüştüğüm o yara izli adamı okudun — o kim?!\"",
                    toneHint = "Hakikat Arayışı • Tekillik Sırrına Doğrudan Geçiş",
                    nextNodeId = "ch16_s2_strange_portal",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VISION to 4, BookCharacterId.BRUCE to 3),
                        mysteryDelta = 2,
                        setFlags = mapOf("ch16_vision_trust" to "probed_truth"),
                        impactSummary = "Vision'ın alnındaki taş parladı: 'Bunu sana yalnız ben değil, az sonra kapımızdan girecek kadim bir misafir de açıklayacak.'"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 2: DOCTOR STRANGE & ÇOKLU EVREN TEKİLLİĞİ GERÇEĞİ (Karar 2/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch16_s2_strange_portal",
            chapterNumber = 17,
            sceneOrder = 2,
            sceneTitle = "Altın Kıvılcımlı Kapı — Doctor Strange ve Tekillik",
            locationTag = "AVENGERS KULESİ · ORTAK SALON • 22:24",
            paragraphs = listOf(
                "Tam o anda salonun ortasında, havayı makas gibi kesen kıvılcımlı, dönen altın bir halka açıldı! Steve kalkanını kaldırdı, Pietro refleksle Wanda'nın önüne sıçradı. Altın portaldan koyu mavi cüppesi, omuzlarında kendi iradesiyle dalgalanan kızıl pelerini ve boynunda zümrüt bir madalyonla keskin bakışlı bir adam adım attı: **Doktor Stephen Strange — Bleecker Sokağı Tapınağı'nın Koruyucusu**.",
                "\"Kalkanları indirin, Avengers,\" dedi Strange kuru ama otoriter bir sesle, el hareketiyle portalı kapatırken. \"Üç blok ötede aynı anda iki adet **Çoklu Evren Tekilliği** çarpışınca tapınağımın bütün çay fincanları havaya uçtu. Bunu görmezden gelemezdim.\" Strange doğrudan sana baktı, sonra Vision'la göz göze geldi.",
                "Vision alnındaki Zihin Taşı'ndan havaya berrak, altın-mavi bir kozmik harita yansıttı. Haritada sonsuz sayıda paralel evren dalı uzanıyordu: her evrende farklı bir Steve, farklı bir Tony, farklı bir Wanda vardı — ama bütün o sonsuz dalların tam merkezinde, yalnızca **TEK BİR MAVİ IŞIK NOKTASI** yanıyordu.",
                "[[DOSSIER|BÜYÜK GERÇEK · ÇOKLU EVREN TEKİLLİĞİ & ASIL MİMAR|1) Aiden Blackwood paralel evrenlerde kopyası olmayan TEKİL BİR VARLIKTIR (Multiversal Singularity). 2) Bu yüzden Yara İzli 'Sıfır' başka bir evrenden gelen ikiz değil — beş yıl sonrasından, Ultron ve Asıl Mimar'ın Avengers'ı yıktığı KARANLIK GELECEKTEN geri fırlatılmış SENİN KENDİ HALİNDİR! 3) Aynı zaman çizgisinde iki adet Aiden bulunması 'Tekillik Paraziti' yaratır ve ikinizin de gücünü zayıflatır. 4) 14 Kasım 2006 gecesi baban Dr. Thomas Blackwood, Asıl Mimar'ın bütün çoklu evreni susturmak için yarattığı 'Tekillik Çekirdeği'ni (Anahtar'ı) çaldı ve Mimar onu asla bulamasın diye beş yaşındaki sana mühürledi!]]",
                "Oda buz kesmişti. Yirmi yıllık bütün parçalar — 14 Kasım 2006 gecesi, babanın son fedakârlığı, Soluk Mühür'ün seni araması, Asıl Mimar'ın sana *'Kayıp Anahtarım'* demesi ve Sıfır'ın yanağındaki yara izi — ilk kez tek bir kusursuz resimde birleşmişti!",
                "Doctor Strange ciddi bir sesle ekledi: \"Asıl Mimar henüz doğrudan bu boyuta geçemiyor, Aiden — çünkü fiziksel Anahtar sensin. Ultron'u senin kalbini kırmak, seni insanlıktan koparıp kendi karanlık geleceğin Sıfır'a dönüştürmek için kullanıyor. Eğer umudunu kaybedip Kırmızı Göz'e teslim olursan, kapıyı ona kendi ellerinle açmış olacaksın.\""
            ),
            decisionTitle = "Bölüm XVI (2/4) — Tekillik Gerçeğine Karşılığın",
            choicePrompt = "Çoklu evrende tek olduğunu, Sıfır'ın senin karanlık geleceğin olduğunu ve babanın seni korumak için Mimar'ın Anahtarını sana sakladığını öğrendin. Strange ve ekibe ne diyeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch16_c2_accept_destiny",
                    label = "\"Babam o çekirdeği bir silah değil, bir kalkan olsun diye bana bıraktı,\" de. \"Mimar benden soğuk bir anahtar bekliyor — ona kırılmaz bir insan kalbi göstereceğim.\"",
                    toneHint = "Aydınlık İrade • Vision & Strange Saygısı • İnsanlık Zirvesi",
                    nextNodeId = "ch16_s3_natasha_signal",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VISION to 5, BookCharacterId.STEVE to 4, BookCharacterId.WANDA to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 4, StoryFlagAxis.COSMIC_AWAKENING to 3),
                        setFlags = mapOf("ch16_singularity_oath" to "human_heart"),
                        secretUnlocked = "2006 Tekillik Çekirdeği Gerçeği",
                        impactSummary = "Doctor Strange hafifçe başını eğdi: 'O hâlde o karanlık geleceği gerçekten silebilirsin, Aiden Blackwood.'",
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch16_c2_ask_strange_help",
                    label = "Strange'e dön: \"Madem bu kadar büyük bir tehdit var, Sokovia'da neden bizimle gelmiyorsun? Tekillik Paraziti'ni nasıl kapatacağız?\"",
                    toneHint = "Pragmatik Lider • Strange & Tony Diyaloğu",
                    nextNodeId = "ch16_s3_natasha_signal",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 4, BookCharacterId.BRUCE to 3, BookCharacterId.VISION to 3),
                        setFlags = mapOf("ch16_singularity_oath" to "pragmatic"),
                        secretUnlocked = "2006 Tekillik Çekirdeği Gerçeği",
                        impactSummary = "Strange uyardı: 'Ben Sokovia'ya gidersem Mimar tapınak mühürlerini kırıp doğrudan geçer! Sen Ultron'u yıkınca gelecek değişecek ve Sıfır kendi zamanına huzurla silinecek.'"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 3: NATASHA'NIN MORS SİNYALİ (Karar 3/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch16_s3_natasha_signal",
            chapterNumber = 17,
            sceneOrder = 3,
            sceneTitle = "Kafesteki Örümcek — Natasha'nın Sinyali",
            locationTag = "AVENGERS KULESİ · İLETİŞİM KONSOLU • 22:38",
            paragraphs = listOf(
                "Doctor Strange altın portalından geri dönüp mistik bariyerleri tutmak için ayrılırken, Clint Barton'ın telsiz alıcısı keskin, ritmik tıkırtılarla ötmeye başladı! Clint yüzünde kocaman bir sırıtışla ekrana eğildi: \"Klasik Nat! Eski S.H.I.E.L.D. acil durum Mors kodu! Ultron onu Sokovia'nın eski merkez kilisesinin altındaki demir hücreye kapatmış — ama kendi telsizini hurda parçalardan kurup koordinatı yollamış!\"",
                "Bruce hemen uydu görüntüsünü ekrana verdi: Ultron, Sokovia şehrinin tam altındaki yeraltı madenlerine dev bir **Vibranyum yerçekimi çekirdeği** yerleştirmişti. Şehri komple gökyüzüne kaldırıp bir göktaşı gibi Dünya'ya çarptırmayı planlıyordu!",
                "Pietro yumruklarını sıktı: \"Sokovia benim şehrim! Bütün çocukluğum orada — o insanlar daha kaçmaya bile fırsat bulamaz!\" Steve derhâl kalkanını sırtına geçirdi: \"Herkes hazırlanıyor! Önce Natasha'yı çıkaracağız, şehri tahliye edeceğiz, sonra da Ultron'u bitireceğiz!\""
            ),
            decisionTitle = "Bölüm XVI (3/4) — Sokovia Harekatı Öncesi Önceliğin",
            choicePrompt = "Quinjet 20 dakika sonra Sokovia'ya kalkacak. Kule'den çıkmadan önce kiminle son bir strateji kuracaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch16_c3_brief_bruce_nat",
                    label = "Bruce'la birlikte Natasha'yı çıkarma ekibini planla — \"Hücrenin kilidine direkt ışınlanıp onu saniyeler içinde çıkaracağım!\"",
                    toneHint = "Natasha Kurtarma Planı • Bruce & Clint Onayı",
                    nextNodeId = "ch16_s4_cell_door",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.BRUCE to 4, BookCharacterId.CLINT to 3),
                        setFlags = mapOf("ch16_prep_focus" to "natasha_rescue"),
                        impactSummary = "Bruce gözlüğünü düzeltip başını salladı: 'Natasha'yı sağ salim çıkaracağız Aiden — sonra yeşil dostum Ultron'la ilgilenir.'",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch16_c3_brief_twins",
                    label = "Wanda ve Pietro'yu yanına çek — sokak tahliyesinde hız, kaos büyüsü ve ışınlanmayı üçlü bir kurtarma ağına çevir!",
                    toneHint = "Maximoff İkizleri Uyumu • Pietro & Wanda Bağı",
                    nextNodeId = "ch16_s4_cell_door",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4, BookCharacterId.PIETRO to 5),
                        setFlags = mapOf("ch16_prep_focus" to "twins_evac"),
                        impactSummary = "Pietro gülümsedi: 'Gümüş şimşek, kızıl fırtına ve mavi kıvılcım — Ultron'un hiç şansı yok!'",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch16_c3_brief_vision_tony",
                    label = "Vision ve Tony'yle Ultron'un internet ağını tamamen kesme protokolünü tasarla — Ultron tek bir nöbetçiye bile kaçamasın!",
                    toneHint = "Stratejik Zafer • Vision & Tony Uyumu",
                    nextNodeId = "ch16_s4_cell_door",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VISION to 5, BookCharacterId.TONY to 4),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 3),
                        setFlags = mapOf("ch16_prep_focus" to "net_lock"),
                        impactSummary = "Vision başını eğdi: 'Onu ağdan tamamen yakıp çıkaracağım Aiden. Bu son karşılaşması olacak.'"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch16_s4_cell_door",
            chapterNumber = 17,
            sceneOrder = 4,
            sceneTitle = "Revir Kapısındaki Gölge",
            locationTag = "AVENGERS KULESİ · GÜVENLİ REVİR KORİDORU • 22:50",
            paragraphs = listOf(
                "Ekip Quinjet'e silah yüklerken, sen koridorun sonundaki cam kapının önünde durdun. İçeride, kolları sarılı ama başı dik oturan **Sıfır** — beş yıl sonraki yara izli kendin — seni sessizce bekliyordu. Onu arkada bırakıp Sokovia'ya uçmadan önce, yanağındaki o gümüş yara izinin gerçek hikâyesini ve karanlık geleceğinin sırrını yüz yüze dinlemek zorundaydın."
            ),
            decisionTitle = "Bölüm XVI (4/4) — Revir Kapısından İçeri Giriş",
            choicePrompt = "Sıfır'la baş başa kalmak için içeri nasıl gireceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch16_c4_enter_alone",
                    label = "Kelepçe kilidini kapıdan devreden çıkarıp silahsız içeri gir — kendi geleceğinden korkmayacaksın.",
                    toneHint = "Cesaret & Empati • Sıfır ile Derin Yüzleşme",
                    nextNodeId = "ch16_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 2, BookCharacterId.VISION to 2),
                        setFlags = mapOf("ch16_zero_approach" to "unarmed"),
                        impactSummary = "Sıfır şaşkınlıkla kilit sesine baktı; gözlerindeki buz ilk kez çözülmeye başladı."
                    )
                ),
                BookChoiceDef(
                    id = "ch16_c4_enter_guarded",
                    label = "Vision'ın kapıda beklemesini isteyip cam panelin karşısına otur — önce bütün gerçeği duymak istiyorsun.",
                    toneHint = "Temkinli Liderlik • Güvenli Sorgu",
                    nextNodeId = "ch16_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VISION to 3, BookCharacterId.TONY to 2),
                        setFlags = mapOf("ch16_zero_approach" to "guarded"),
                        impactSummary = "Vision sessizce kapıda nöbet tuttu; Sıfır acı bir gülümsemeyle başını sallayıp konuşmaya hazırlandı."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch16_s5_end",
            chapterNumber = 17,
            sceneOrder = 5,
            sceneTitle = "İki Aiden, Tek Kader",
            locationTag = "AVENGERS KULESİ · REVİR ODASI • 22:55",
            paragraphs = listOf(
                "Sandalyeye oturduğunda, aranızdaki masada tek bir ışık yanıyordu. Aynı yüz, aynı mavi gözler — ama iki bambaşka kader. Sıfır ağır bir nefes verip sol yanağındaki o derin gümüş yara izine dokundu: \"Sokovia'ya gitmeden önce,\" dedi kısık sesle, \"benim dünyamda o şehrin nasıl düştüğünü bilmen gerek.\""
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Gelecek yazılmış bir taş değil — kırılmayı bekleyen bir aynadır.”"
        )
    ).associateBy { it.id }
}
