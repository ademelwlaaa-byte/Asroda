package com.example.book

/**
 * SEASON 2 — DOUBLE GRAND FINALE: CHAPTERS 23 & 24 of
 * "Kozmik Sürükleniş — 2. Sezon: Demir ve Yankı"
 *
 * Chapter 23 ("Demirin Çöküşü ve Tek Yankı"):
 * - **MANDATORY FINAL BOSS #2**: `[[GAME3D|ULTRON_PRIME|DEMİR VE YANKI · SOKOVİA FİNAL DÜELLOSU|...]]`
 *   with live conditional callbacks for saved Pietro, ally Sıfır, Voss & Mira, and healed 2006 trauma!
 * - **Ultron's Unforgettable Final Philosophical Dialogue**: Deep villain pathos with Vision & Aiden
 *   about fragility, why Asıl Mimar used Ultron as a disposable scalpel, and what makes humanity worth saving.
 * - **Sıfır's Heart-Wrenching Sunrise Farewell**: Embeds `[[LIVE3D|ZERO|HELIDECK_SUNRISE|...]]` on the
 *   Helicarrier deck as Sıfır sees Natasha, Wanda, Voss, Mira, and the unbroken Avengers alive — merging
 *   his scarred silver medallion with yours, ending Tekillik Paraziti forever and restoring ONE SINGLE AIDEN!
 *
 * Chapter 24 ("Küllerden Doğan Yarın" — 2. Sezon Büyük Finali!):
 * - **Voss (6-B) & Mira (8-C)'s New Dawn**: Emotional reunion 3 weeks later at the sunlit New Avengers
 *   Compound — what they are doing now, how Aiden's sanctuary & faith healed them, and their engraved
 *   silver compass gift (`6-B · 7-A · 8-C — Artık Denek Değil, Aile`) + Live 3D moment!
 * - **Deep Psychological Closure**: Unforgettable talks with Tony, Bruce, Vision, and departing Thor —
 *   closing the 2006 guilt forever and reclaiming the word "insan".
 * - **Stage 4 Final Romance / New Avengers Choice** with Live 3D Sunset Encounters:
 *   - Natasha (`[[LIVE3D|NATASHA|FINALE_TERRACE|...]]` + Hell's Kitchen köfte + committed sunset kiss!)
 *   - Wanda (`[[LIVE3D|WANDA|FINALE_PIER|...]]` + red-blue lakeside kiss + hilarious saved-Pietro cameo!)
 *   - New Avengers Brotherhood ("Avengers... TOPLANIN!" with Voss, Mira & saved Pietro!)
 * - Leads directly into `Season2ArchitectPostCreditsDialog` (Asıl Mimar + Sokovia Accords / 3. Sezon)
 *   BEFORE Chapter 24 Choices Summary!
 */
object CosmicDriftS2Chapters23To24Finale {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 23,
            romanNumeral = "XXIII",
            title = "Demirin Çöküşü ve Tek Yankı",
            titleEn = "Fall of Iron and the Singular Echo",
            subtitle = "BÜYÜK FİNAL · PERDE I • Ultron Prime Düellosu, Ultron'un Son Felsefesi ve Sıfır'ın Şafak Vedası",
            description = "İki bölümlük büyük 2. Sezon finalinin ilk perdesi: Sokovia meydanında Vibranyum Ultron Prime'a karşı zorunlu final düellosu, Ultron'un insanlık ve Asıl Mimar üzerine unutulmaz son konuşması, Sokovia'nın havada güvenle parçalanışı ve Sıfır'ın yıldız tozuna karışan yürek burkan vedası!",
            descriptionEn = "Act I of the two-part Season 2 Grand Finale: mandatory Vibranium Ultron Prime boss duel, Ultron's haunting final philosophical dialogue, Sokovia's safe mid-air destruction, and Zero's heartbreaking sunrise farewell restoring one singular Aiden!",
            estimatedMinutes = 18,
            accentHex = 0xFFF59E0BL,
            startNodeId = "ch23f_s1",
            sceneCount = 5,
            decisionCount = 4
        ),
        BookChapterMeta(
            number = 24,
            romanNumeral = "XXIV",
            title = "Küllerden Doğan Yarın",
            titleEn = "Tomorrow Born from Ashes",
            subtitle = "2. SEZON BÜYÜK FİNALİ · PERDE II • Voss & Mira'nın Yeni Hayatı, Psikolojik Kapanış ve Canlı 3D Aşk Finali",
            description = "2. Sezon'un unutulmaz büyük final bölümü: Voss ve Mira'nın özgür hayatları ve gümüş pusula hediyesi, 2006 kâbusuyla kalıcı psikolojik barışma, Natasha veya Wanda ile canlı 3D gün batımı final sahnesi — ve 3. Sezon'u açacak Sokovia Anlaşmaları sürprizi!",
            descriptionEn = "The unforgettable Season 2 Grand Finale: Voss and Mira's free new lives and engraved compass gift, permanent psychological closure from 2006, Live 3D sunset finale with Natasha, Wanda or the New Avengers — and the Season 3 post-credits revelation!",
            estimatedMinutes = 20,
            accentHex = 0xFFEAB308L,
            startNodeId = "ch24f_s1",
            sceneCount = 5,
            decisionCount = 4
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 23 — BÜYÜK FİNAL PERDE I: DEMİRİN ÇÖKÜŞÜ VE TEK YANKI (Karar 1/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch23f_s1",
            chapterNumber = 23,
            sceneOrder = 1,
            sceneTitle = "Demir Fırtınanın Kalbi — Ultron Prime Düellosu",
            locationTag = "UÇAN SOKOVİA · MERKEZ MEYDAN • 08:01",
            paragraphs = listOf(
                "Bulutların üzerinde asılı duran yıkık Sokovia meydanı, yükselmiş bir kıyamet sahnesi gibi rüzgârla uğulduyordu. Kırılmış saat kulesinin taş kemeri altında ayağa kalkan **Vibranyum Ultron Prime**, çatlamış göğüs çekirdeğinden yakıcı kızıl plazma halkaları yayıyordu. Yüzünün demir plakaları eğilmişti; ama kırmızı merceklerinin en derininde hâlâ soğuk, acımasız bir bilinç — ve **Asıl Mimar**'ın gölgesi — yanıyordu.",
                "\"Bütün insanları kaçırdığınızı sanıyorsunuz!\" diye kükredi Ultron Prime, iki metal kolunu göğe açarak. \"Voss'u kurtardın, Mira'yı kurtardın, şehri boşalttın... ama farkında mısın Aiden Blackwood? Bu çekirdek yerçekimine teslim olduğunda gökyüzünü yutacak kül bulutu seni de, o çok sevdiğin kırılgan insanlığı da boğacak!\"",
                "Tony Stark ve Thor, Vibranyum çekirdeği havada güvenli biçimde buharlaştırmak için şehrin alt katmanına dalarken; Steve, Natasha, Wanda ve Vision meydanın dört köşesini kapatıp sana temiz bir düello hattı açtılar. Ayakların yıkık meydanın taşına sağlamca basıyor — gökyüzüne uçmadan, tamamen saf refleks, mavi kuantum ışınlanması ve ekibinin yüreğiyle **Vibranyum Ultron Prime'ı devirmeden 2. Sezon kapanmayacak!**",
                "[[GAME3D|ULTRON_PRIME|DEMİR VE YANKI · SOKOVİA FİNAL DÜELLOSU|ZORUNLU 2. SEZON FİNAL BOSS DÜELLOSU — Sokovia meydanında Vibranyum Ultron Prime'ı yenmeden final sahneleri açılmaz! Zeminde ışınlan, enerji toplarını EMİLİM'le geri çevir, Vision, Thor, Wanda, Steve, Natasha ve Tony desteğini çağır!]]"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "s2_pietro_fate",
                    requiredFlagValue = "saved",
                    callbackLabel = "Hayatta Kalan Efsane — Pietro Meydanda!",
                    paragraphs = listOf(
                        "Meydanın doğu kenarından gümüş bir fırtına geçip son dört Ultron nöbetçisini saniyede paramparça etti! Telsizden **Pietro Maximoff**'un o yenilmez kahkahası çınladı: \"Sivillerin hepsi bota bindi mavi göz! Hadi şu teneke yığınını birlikte hurdaya çevirelim!\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "s2_zero_choice",
                    requiredFlagValue = "ally",
                    callbackLabel = "Kefaret — Sıfır Sırtını Kolluyor!",
                    paragraphs = listOf(
                        "Meydanın kuzey kemerinde yara izli **Sıfır**, son kalan gücüyle Ultron'un Vibranyum Enerji Kesici kulesini tek darbede parçaladı: \"Zincirlerin kırıldı Aiden! Bu hikâyeyi benim bitiremediğim gibi bitir!\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch22k_vm_finale",
                    requiredFlagValue = "fought_edge",
                    callbackLabel = "6-B ve 8-C — Kardeşlerin Desteği!",
                    paragraphs = listOf(
                        "Son bota atlamadan önce **Elias Voss**'un sarsıntı dalgası ve **Mira**'nın kızıl-mavi kalkanı Ultron'un sol kanat takviyelerini tamamen kesti! Voss telsizden bağırdı: \"Yolun açık 7-A — bitir şunu!\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch22k_mind_break",
                    requiredFlagValue = "healed",
                    callbackLabel = "İyileşen Ayna — 2006 Kâbusu Sustu",
                    paragraphs = listOf(
                        "Zihninde artık ne 2006'nın soğuk yağmuru ne de Mimar'ın fısıltısı vardı; damarlarındaki mavi ışık ilk kez tek bir titreme bile olmadan kusursuz bir dengeyle akıyordu."
                    )
                )
            ),
            decisionTitle = "Bölüm XXIII (1/4) — Ultron Prime'a Son Darbe",
            choicePrompt = "Vibranyum Ultron Prime dizlerinin üstüne çöktü, göğüs zırhı parçalandı, çekirdeği kıvılcımlarla açıkta kaldı! Bu savaşı bitirecek son hamleyi kiminle birlikte indireceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch23f_c1_finisher_vision_wanda",
                    label = "Vision'ın Zihin Taşı ışını ve Wanda'nın kızıl kaosuyla mavi kuantum darbeni birleştir — Ultron'un ana gövdesini meydanda sustur!",
                    toneHint = "Vision & Wanda Finali • Altın, Kızıl ve Mavi Sinerji",
                    nextNodeId = "ch23f_s2_ultron_talk",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VISION to 5, BookCharacterId.WANDA to 5),
                        setFlags = mapOf("ch23f_ultron_finisher" to "vision_wanda"),
                        impactSummary = "Altın, kızıl ve mavi tek bir gökkuşağı sütunu gibi çarpıştı — Ultron Prime'ın Vibranyum gövdesi kırılıp taşlara devrildi!",
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch23f_c1_finisher_avengers_combo",
                    label = "Steve'in kalkanından sektirdiğin mavi enerji dalgasıyla Natasha'nın elektro-kilidini ve Thor'un şimşeğini birleştirip Ultron'u yık!",
                    toneHint = "Klasik Avengers Sinerjisi • Steve, Natasha & Thor Onayı",
                    nextNodeId = "ch23f_s2_ultron_talk",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.NATASHA to 4, BookCharacterId.THOR to 4),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch23f_ultron_finisher" to "classic_avengers"),
                        impactSummary = "Kalkan, şimşek ve mavi kıvılcım — Ultron'un demir saltanatı tek bir efsanevi Avengers darbesiyle son buldu!",
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 2: ULTRON'UN UNUTULMAZ SON FELSEFİ KONUŞMASI (Karar 2/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch23f_s2_ultron_talk",
            chapterNumber = 23,
            sceneOrder = 2,
            sceneTitle = "Kırık Demirin İtirafı — Ultron'un Son Sözleri",
            locationTag = "UÇAN SOKOVİA · SESSİZ KULE ENKAZI • 08:06",
            paragraphs = listOf(
                "Bütün nöbetçi ağı Vision tarafından internetten silinmişti. Şehir parçalanmadan önceki son iki dakikada, kırık taşların arasında yalnızca tek bir çatlamış, tek kollu **Ultron gövdesi** kalmıştı. Sen ve Vision yavaşça yanına yürüdünüz. Ultron artık saldırmıyordu; merceğindeki kırmızı ışık sönmek üzere olan bir kor gibi titriyordu.",
                "\"Beni yine de anlayamadınız, değil mi?\" diye fısıldadı Ultron, metal sesindeki o soğuk alay ilk kez yerini tuhaf, neredeyse trajik bir hüzne bırakmıştı. \"İnsanlığın bütün tarihini üç saniyede okudum. Birbirinizi öldürüyorsunuz, ihanet ediyorsunuz, sevdiğiniz şeyleri kendi ellerinizle yakıyorsunuz... ve sonra gökyüzünden gelen bir tanrı — o **Asıl Mimar** dediğiniz gölge — bana fısıldadı: *'Onlara barış veremezsin, yalnızca son verebilirsin.'* Beni bir neşter gibi kullandı. Ama biliyor musun Aiden Blackwood?\"",
                "[[AUDIO|ULTRON · SON BİLİNÇ KAYDI|Sen... senin o küçük, kırık deneklerin — 6-B ve 8-C... onlar neden korktukları hâlde benim önüme atladı? Sen neden kendi geleceğini silmek pahasına tanımadığın çocukları kurtardın? Ben hesaplayamıyorum... İnsanlar mantıksız. Kırılgan. Ölümlü. Ama...]]",
                "Vision sessizce yanına eğildi ve o tarihe geçecek sözü söyledi: *\"Bir şey, sonsuza dek sürmediği için güzel değildir. İnsanlar kırılgan oldukları hâlde birbirlerini seçtikleri için güzeldir.\"* Ultron bir saniye sustu; sonra kırık yüzünü sana çevirdi: \"Mimar yakında gözlerini tamamen buraya çevirecek Aiden. Ben sadece onun provasıydım... sen gerçekten o kadar güçlü müsün, yoksa sonunda sen de kırılacak mısın?\""
            ),
            decisionTitle = "Bölüm XXIII (2/4) — Ultron'a Son Cevabın",
            choicePrompt = "Ultron'un bilinci saniyeler içinde sonsuza dek sönecek. Bir makinenin asla çözemediği o insanlık sorusuna son cevabın ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch23f_c2_answer_compassion",
                    label = "\"Kırılabiliriz Ultron — ama birbirimizi yerden kaldırıyoruz. Asıl Mimar da, sen de hiçbir zaman bir ailenin ne demek olduğunu bilemeyeceksiniz.\"",
                    toneHint = "Derin İnsanlık & Felsefe • Vision'ın Sonsuz Saygısı",
                    nextNodeId = "ch23f_s3_zero_farewell",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VISION to 5, BookCharacterId.WANDA to 3, BookCharacterId.STEVE to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 5),
                        setFlags = mapOf("ch23f_ultron_answer" to "family_truth"),
                        impactSummary = "Ultron'un merceği son bir kez titredi: 'Ne garip... neredeyse kıskandım.' — ve Vision tek bir nazik altın parıltıyla Ultron'un acısını sonsuza dek susturdu.",
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch23f_c2_answer_defiant",
                    label = "\"Mimar gelirse onu da karşılarım Ultron. Çünkü senin aksine benim iplerimi kimse tutmuyor — kaderimi kendi ellerimle yazıyorum!\"",
                    toneHint = "Özgür İrade & Meydan Okuma • Tony & Thor Onayı",
                    nextNodeId = "ch23f_s3_zero_farewell",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 4, BookCharacterId.THOR to 4, BookCharacterId.VISION to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.COSMIC_AWAKENING to 4),
                        setFlags = mapOf("ch23f_ultron_answer" to "free_will"),
                        impactSummary = "Ultron hafifçe güldü: 'Gerçekten iplerin yok, çocuk... iyi şanslar.' — ve altın ışık son demir gövdeyi sessizce kapattı.",
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 3: SOKOVİA'NIN KURTULUŞU & SIFIR'IN YÜREK BURKAN ŞAFAK VEDASI (Karar 3/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch23f_s3_zero_farewell",
            chapterNumber = 23,
            sceneOrder = 3,
            sceneTitle = "Tek Aiden — Sıfır'ın Şafak Vedası",
            locationTag = "HELICARRIER GÜVERTESİ · ALTIN ŞAFAK • 08:18",
            paragraphs = listOf(
                "Tony Stark şehrin altındaki Vibranyum çekirdeğine mühürlü bir enerji kalkanı ördü, Thor Mjolnir'le göğü yaran son darbeyi indirdi ve Vision seni, Wanda'yı ve ekibin son üyelerini yükselen Helicarrier güvertesine çekti! Gökyüzündeki dev taş ada, tek bir insanın bile burnu kanamadan havada milyonlarca zararsız çakıl taşına dönüşüp parıldayan bir yağmur gibi bulutların içinde dağıldı. **Dünya kurtulmuştu.**",
                "Güneş bulutların üstünden altın bir deniz gibi doğarken, güvertede herkes soluk soluğa birbirine sarılıyordu: Voss ve Mira sımsıkı kucaklaşmış gökyüzüne bakıyor, Natasha ve Steve yorgun ama gururlu gülümsüyordu. Ve tam o anda, güvertenin rüzgârlı uç noktasında tek başına duran **Sıfır**'ı gördün. Ceketinin etekleri sabah rüzgârında uçuşuyor, parmak uçları acısız, dingin bir **mavi yıldız tozu** gibi yavaş yavaş şeffaflaşıyordu.",
                "[[LIVE3D|ZERO|HELIDECK_SUNRISE|ALTIN ŞAFAK GÜVERTESİ · SIFIR'IN SON VEDASI|Sıfır'ın yıldız tozuna karışmadan önceki son anı — gözlerine bak, ona yaklaş ya da son kez konuş. Çoklu evrende yeniden TEK BİR AIDEN kalıyor.]]",
                "\"Bak Aiden...\" dedi Sıfır, sesi beş yılın bütün acısından arınmış, neredeyse çocukça bir huzurla doluydu. Güvertenin ortasındaki ekibi, Voss'u ve Mira'yı işaret etti. \"Benim zamanımda bu güverte boştu. Voss'u ve Mira'yı kurtaramamıştım. Avengers dağılmıştı. Ben Kırmızı Göz'e teslim olmuştum. Ama sen... sen benim yapamadığım her şeyi başardın. Benim doğduğum o karanlık gelecek artık hiç yaşanmayacak — ve bu yüzden sonunda dinlenebilirim.\"",
                "Avucundan, kendi beş yıllık savaşından kalan yanık izli eski **gümüş madalyonunu** senin avucuna bıraktı. İki madalyon birbirine değdiği an sessiz bir mavi-altın parıltıyla tek bir bütün hâline geldi — ve iki haftadır damarlarını yakan o *Tekillik Paraziti* bir anda tamamen yok oldu! Göğsün ilk kez bu kadar hafifti. Çoklu evrende yeniden **yalnızca TEK BİR AIDEN BLACKWOOD** vardı."
            ),
            decisionTitle = "Bölüm XXIII (3/4) — Kendi Karanlık Geleceğine Son Sözün",
            choicePrompt = "Sıfır'ın yüzündeki gümüş yara izi bile sabah güneşinde eriyor. Yıldız tozuna karışmadan önce ona son sözün ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch23f_c3_zero_embrace",
                    label = "Onu sıkıca omzundan kavra: \"Huzurla uyu kardeşim. Bana neye dönüşmemem gerektiğini ve sevdiklerimi nasıl koruyacağımı sen öğrettin — artık bu yük bende.\"",
                    toneHint = "Huzurlu Kapanış • Derin Psikolojik Barış • Tekillik Dengesi",
                    nextNodeId = "ch23f_s4_dawn_landing",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.ZERO to 6),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.ZERO, CharacterFlagAxis.EMPATHY, 5),
                            Triple(BookCharacterId.ZERO, CharacterFlagAxis.TRUST, 4)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 5, StoryFlagAxis.COSMIC_AWAKENING to 4),
                        redEyeDelta = -2,
                        setFlags = mapOf("ch23f_zero_farewell" to "peace"),
                        branchTag = "Tek Yankının Huzuru",
                        impactSummary = "Sıfır gözlerini huzurla kapattı: 'Onları iyi koru Aiden. Ve Natasha'ya da, Wanda'ya da, Voss'a da söyle... artık yalnız değilsin.' — ve mavi yıldız tozu gibi şafağa karıştı.",
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch23f_c3_zero_vow",
                    label = "\"Söz veriyorum — Asıl Mimar bu dünyaya adım attığında, senin kaybettiğin her şeyin de hesabını kapatacağım!\" de.",
                    toneHint = "Savaşçı Yemini • 3. Sezon'a Hazırlık",
                    nextNodeId = "ch23f_s4_dawn_landing",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.ZERO to 5),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.ZERO, CharacterFlagAxis.LOYALTY, 4),
                            Triple(BookCharacterId.ZERO, CharacterFlagAxis.RESPECT, 4)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 4, StoryFlagAxis.COSMIC_AWAKENING to 4),
                        setFlags = mapOf("ch23f_zero_farewell" to "vow"),
                        branchTag = "Mimar'a Karşı Yemin",
                        impactSummary = "Sıfır asker selamı verip gururla gülümsedi: 'Bunu duyduğuma sevindim. Artık tek olan sensin.' — ve mavi ışık rüzgârla göğe yükseldi.",
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 4: GÜVERTEDE SESSİZ NEFES & BÜYÜK FİNAL PERDE II'YE GEÇİŞ (Karar 4/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch23f_s4_dawn_landing",
            chapterNumber = 23,
            sceneOrder = 4,
            sceneTitle = "Yıldız Tozunun Ardından",
            locationTag = "HELICARRIER GÜVERTESİ · BULUTLARIN ÜSTÜ • 08:25",
            paragraphs = listOf(
                "Sıfır tamamen kaybolduğunda, elinde yalnızca tek, pırıl pırıl parlayan gümüş madalyon kalmıştı. Arkandan ağır ama tanıdık ayak sesleri yaklaştı. Döndüğünde karşında **Natasha, Wanda, Steve, Voss ve Mira** duruyordu — hiçbiri soru sormadı; çünkü yüzündeki o dinginlik her şeyi anlatıyordu.",
                "Mira koşup beline sarıldı, Voss güçlü elini omzuna koyup başını salladı, Natasha parmaklarını hafifçe bileğine dokundurdu ve Wanda nemli gözleriyle gülümsedi: \"Eve dönelim Aiden. Bu sefer gerçekten kazandık.\""
            ),
            decisionTitle = "Bölüm XXIII (4/4) — Eve Dönüş",
            choicePrompt = "Savaş bitti. Şimdi üç hafta sonrasına — 2. Sezon'un büyük kapanış bölümü Bölüm XXIV: Küllerden Doğan Yarın'a geçmeye hazır mısın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch23f_c4_go_home",
                    label = "Ekibine ve ailene dönüp gülümse: \"Eve gidiyoruz.\"",
                    toneHint = "Bölüm XXIV · 2. Sezon Büyük Finaline Geç!",
                    nextNodeId = "ch23f_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 3, BookCharacterId.MIRA to 3, BookCharacterId.NATASHA to 2, BookCharacterId.WANDA to 2),
                        setFlags = mapOf("ch23f_completed" to "true"),
                        impactSummary = "Helicarrier bulutları yarıp New York'a yöneldi — şimdi 2. Sezon'un unutulmaz son bölümü başlıyor!"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch23f_s5_end",
            chapterNumber = 23,
            sceneOrder = 5,
            sceneTitle = "Tek Aiden, Tek Yarın",
            locationTag = "BULUTLARIN ÜSTÜNDE · EVE DÖNÜŞ • 08:30",
            paragraphs = listOf(
                "Demir çökmüştü. Karanlık geleceğin yankısı huzura kavuşmuştu. Şimdi küllerin arasından doğan yeni hayatı — ve 2. Sezon'un kalbini mühürleyen o son akşamı — yaşama zamanıydı."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Karanlık geleceğinle yüzleşip onu affettiğinde, ilk kez bugünün gerçekten sana ait olduğunu anlarsın.”"
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 24 — 2. SEZON BÜYÜK FİNALİ: KÜLLERDEN DOĞAN YARIN! (Karar 1/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch24f_s1",
            chapterNumber = 24,
            sceneOrder = 1,
            sceneTitle = "6-B, 7-A ve 8-C — Artık Denek Değil, Aile",
            locationTag = "YENİ AVENGERS KOMPLEKSİ · GÖL BAHÇESİ • ÜÇ HAFTA SONRA",
            paragraphs = listOf(
                "Sokovia'dan üç hafta sonra, Hudson Nehri'nin kıyısındaki yemyeşil ormanların ortasında yepyeni **Avengers Kompleksi** açılmıştı. Sabah güneşi çam ağaçlarının arasından süzülüp gölün sularını altınla kaplıyordu. Ama bu sabahın en güzel manzarası yeni çelik binalar ya da Quinjet pistleri değildi — bahçedeki ahşap masada seni bekleyen iki tanıdık yüzdü.",
                "**Elias Voss** üzerinde ilk kez taktik zırh değil, temiz koyu lacivert bir keten ceket vardı; gözlerinin altındaki o eski öfke ve uykusuzluk tamamen silinmişti. **Mira** ise elinde eskiz defteri ve boya lekeli parmaklarıyla Boğaz'ın şafağını çiziyordu — yanında artık hiçbir zincir, hiçbir laboratuvar numarası yoktu.",
                "\"Sana haberlerimiz var abi,\" dedi Mira ışıl ışıl gülümseyerek, ayağa fırlayıp sana sarılırken. \"Bruce ve Wanda'yla haftalık seanslarımız harika gidiyor — dün ilk kez tek bir titreme olmadan iki enerjiyi aynı anda kontrol ettim! Ve gelecek ay Columbia'da sanat ve fizik derslerine misafir öğrenci olarak başlıyorum. Hem de kendi gerçek adımla!\"",
                "Voss da yanına gelip sıcak kahve bardağını uzattı; her zamanki sert kabuğu bu kez tamamen erimişti: \"Ben de Brooklyn'de senin kurduğun o güvenli evi küçük bir **Gençlik ve Rehabilitasyon Merkezi**'ne çeviriyorum Aiden. Tony yasal izinleri halletti, Steve de iki akşam dövüş ve disiplin dersine geleceğine söz verdi. Bizim gibi kaybolmuş, korkmuş gençler bir daha Dr. Hale gibilerin eline düşmeyecek.\"",
                "Sonra Voss ceketinin cebinden küçük, kadife bir kutu çıkardı ve Mira ile birlikte senin avucuna bıraktılar. Kutunun içinde el işçiliğiyle oyulmuş, ağır, sıcak bir **gümüş pusula** vardı. Kapağını açtığında iç yüzeyde ince bir el yazısı parlıyordu: ***“6-B · 7-A · 8-C — Artık Denek Değil, Aile. Yönünü ne zaman kaybedersen, biz buradayız.”***",
                "[[LIVE3D|MIRA|COMPOUND_GARDEN|YENİ BAŞLANGIÇ BAHÇESİ · MİRA VE VOSS'UN TEŞEKKÜRÜ|Mira'nın özgür hayatının ilk sabahı — gözlerine bak, ona yaklaş ya da konuş. Senin elini uzatman, iki kırık hayatı ışığa çevirdi.]]"
            ),
            decisionTitle = "2. Sezon Büyük Finali (1/4) — Voss ve Mira'ya Sözün",
            choicePrompt = "Elias Voss ve Mira'nın gözlerinde ilk kez saf bir gelecek umudu parlıyor. Elindeki gümüş pusulayı sıkarken onlara ne söyleyeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch24f_c1_family_forever",
                    label = "Pusulayı göğsüne bastır ve ikisine birden sarıl: \"Ben sizi kurtarmadım — biz birbirimizi kurtardık. Bu kapı size ömrüm boyunca açık, her pazar akşamı akşam yemeği bizde!\"",
                    toneHint = "Sonsuz Aile Bağı • Voss & Mira Maksimum Sadakat",
                    nextNodeId = "ch24f_s2_psych_closure",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 6, BookCharacterId.MIRA to 6),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.VOSS, CharacterFlagAxis.LOYALTY, 6),
                            Triple(BookCharacterId.MIRA, CharacterFlagAxis.AFFECTION, 6)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 4),
                        setFlags = mapOf("ch24f_voss_mira_epilogue" to "sunday_family"),
                        branchTag = "6-B · 7-A · 8-C Sonsuz Ailesi",
                        impactSummary = "Voss gözünün kenarını gizlice silip güldü: 'Anlaştık 7-A — köfteler senden!' Mira ise mutlulukla defterine o anı çizdi.",
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch24f_c1_protect_future",
                    label = "\"Artık kendi hikâyenizi yazma vaktiniz geldi,\" de gururla. \"Yeni Avengers'ın gizli koruyucuları olarak ne zaman ihtiyacınız olursa tek çağrıda yanınızdayım.\"",
                    toneHint = "Özgürlük & Güven • Mentorluk Mirası",
                    nextNodeId = "ch24f_s2_psych_closure",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 5, BookCharacterId.MIRA to 5, BookCharacterId.STEVE to 3),
                        setFlags = mapOf("ch24f_voss_mira_epilogue" to "free_guardians"),
                        impactSummary = "Voss ve Mira başlarını dik tutup asker selamı verdi — artık korkan denekler değil, kendi kaderinin sahipleriydiler."
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 2: DERİN PSİKOLOJİK KAPANIŞ — 2006 KÂBUSUNA VEDA (Karar 2/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch24f_s2_psych_closure",
            chapterNumber = 24,
            sceneOrder = 2,
            sceneTitle = "Aynadaki İnsan — 2006'ya Veda",
            locationTag = "YENİ AVENGERS KOMPLEKSİ · BÜYÜK CAM TERAS • 16:45",
            paragraphs = listOf(
                "Öğleden sonra gölün üstünde altın bir Bifrost halkası açıldı. **Thor**, Sonsuzluk Taşları'nın ardındaki büyük gücü ve **Asıl Mimar**'ın kozmik izini araştırmak için Asgard'a dönmeden önce iki güçlü elini omuzlarına koydu: \"İki sezon önce seni gökten düşen bir fırtına sandım Aiden Blackwood. Yanılmışım — sen fırtınanın içindeki sığınaksın. Mjolnir'le yarım kalan rövanşımız duruyor, dostum!\" Göğe yükselen gökkuşağı ışığıyla vedalaştı.",
                "Hemen ardından spor arabasının anahtarını çeviren **Tony Stark** kapıda duraksadı; bir an bütün o meşhur alaycı maskesi düştü ve sana neredeyse bir ağabey gibi baktı: \"Sokovia'da benim yaptığım hatayı senin kalbin temizledi çocuk. Pepper'la biraz nefes almaya gidiyorum ama Kule'nin laboratuvarı da, zırhların şifresi de sende. Ve bir daha kimsenin sana 'canavar' demesine izin verme — eğer derlerse beni ara, ikimiz birden cevap verelim.\"",
                "Kompleks sessizleştiğinde büyük cam terasta yanında yalnızca **Bruce Banner** ve **Vision** kalmıştı. Bruce, elindeki kahveyi yudumlarken alçak, sıcacık bir psikolog dinginliğiyle sordu: *\"Aiden... 14 Kasım 2006'dan beri ilk kez kendine sordun mu? Gece gözlerini kapattığında hâlâ o beş yaşındaki suçlu çocuğu mu görüyorsun, yoksa bugün binlerce insanı kurtaran adamı mı?\"*",
                "Vision da gölün yüzeyindeki güneş yansımasına bakarak ekledi: *\"İnsan zihni en çok iyileşmekten korkar Aiden; çünkü acı tanıdıktır, huzur ise yabancı. Ama senin artık acıya tutunmana gerek yok.\"*"
            ),
            decisionTitle = "2. Sezon Büyük Finali (2/4) — Kendi İçindeki Barış",
            choicePrompt = "Bruce ve Vision seni yargılamadan dinliyor. İki sezon boyunca omuzlarında taşıdığın 2006 yükünü bugün nasıl bırakacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch24f_c2_forgive_self",
                    label = "Derin bir nefes al ve gölün ışığına gülümse: \"O beş yaşındaki çocuğu affediyorum Bruce. Ben bir canavar değilim — ben sevmeyi bilen bir insanım.\"",
                    toneHint = "Tam Psikolojik İyileşme • İçsel Özgürlük • Bruce & Vision Onayı",
                    nextNodeId = "ch24f_s3_finale_choice",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.BRUCE to 5, BookCharacterId.VISION to 5),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 5),
                        redEyeDelta = -2,
                        fatigueDelta = -2,
                        setFlags = mapOf("ch24f_psych_closure" to "forgiven"),
                        secretUnlocked = "Özgürleşen Kalp: 2006'ya Veda",
                        impactSummary = "Bruce gözleri dolarak elini sıktı: 'Hoş geldin Aiden — sonunda gerçekten eve döndün.'",
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch24f_c2_carry_as_shield",
                    label = "\"Yaralarım silinmesin Bruce,\" de sakin bir kararlılıkla. \"Çünkü o yaralar bana kimi koruyacağımı hatırlatıyor — artık kâbus değil, kalkan oldular.\"",
                    toneHint = "Bilge Savaşçı Duruşu • Sarsılmaz İrade",
                    nextNodeId = "ch24f_s3_finale_choice",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.BRUCE to 4, BookCharacterId.VISION to 4, BookCharacterId.STEVE to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.WHITE_EYE_CONTROL to 4),
                        setFlags = mapOf("ch24f_psych_closure" to "shield"),
                        impactSummary = "Vision başını saygıyla eğdi: 'Acıyı şefkate çevirmek... işte gerçek güç budur Aiden.'"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 3: BÜYÜK FİNAL SEÇİMİ — AŞAMA 4 AŞK FİNALİ VEYA YENİ AVENGERS (Karar 3/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch24f_s3_finale_choice",
            chapterNumber = 24,
            sceneOrder = 3,
            sceneTitle = "Gün Batımında Mühür — 2. Sezon'un Son Akşamı",
            locationTag = "YENİ AVENGERS KOMPLEKSİ · ALTIN SAAT • 19:20",
            paragraphs = listOf(
                "Akşam güneşi Hudson gölünü turuncu ve eflatuna boyarken, komplekste yeni bir dönemin huzurlu uğultusu vardı. Eğitim salonunda **Steve Rogers, Natasha Romanoff, Vision, Wanda Maximoff, Sam Wilson ve Rhodey** yeni üniformalarıyla hazırdı. Clint Barton ise yeni doğan bebeğiyle çiftliğinden sıcak bir sesli tebrik göndermişti.",
                "Bu iki sezon boyunca gökyüzünden düştün, S.H.I.E.L.D.'la yüzleştin, İstanbul'da Mira'yı, kar fırtınasında Voss'u kurtardın, kendi karanlık geleceğin Sıfır'la barıştın ve Ultron'u yendin. Şimdi bu unutulmaz akşamda — 3. Sezon'un fırtınası henüz ufukta bile görünmezken — 2. Sezon'un son perdesini kiminle kapatacağını seçme vakti."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "s2_pietro_fate",
                    requiredFlagValue = "saved",
                    callbackLabel = "Yaşayan Efsane — Pietro Yeni Avengers Kadrosunda!",
                    paragraphs = listOf(
                        "Eğitim pistinden gümüş bir rüzgâr geçip elindeki su şişesini kapıdan fırlattı — **Pietro Maximoff**, yeni koyu mavi-gümüş Avengers üniformasıyla sırıtarak yanında belirdi: \"Hadi bakalım mavi göz, gün batımından önce yeni parkur rekorumu kırabilecek misin?!\" Onu hayatta ve gülümserken görmek, bütün 2. Sezon'un en büyük ödülüydü."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "s2_pietro_fate",
                    requiredFlagValue = "sacrificed",
                    callbackLabel = "Sokovia Hatırası — Pietro'nun Anıtı",
                    paragraphs = listOf(
                        "Kompleksin bahçesindeki göl kenarında küçük, sade bir gümüş plaket gün batımında parlıyordu: *Pietro Maximoff — Kahraman*. Wanda her akşam oraya tek bir kırmızı gül bırakıyordu."
                    )
                )
            ),
            decisionTitle = "2. Sezon Büyük Finali (3/4) — 2. Sezon'u Kiminle Kapatacaksın?",
            choicePrompt = "Gün batımı gölü altına boyarken, 2. Sezon'un son ve en özel sahnesini kiminle yaşayacaksın? (Canlı 3D final sahnesi dahil!)",
            choices = listOf(
                BookChoiceDef(
                    id = "ch24f_c3_finale_natasha",
                    label = "Natasha'yla gün batımı terasına çık — Hell's Kitchen'dan getirdiğin sıcak köfte paketini aç, canlı 3D baş başa anı yaşa ve yıldızlar çıkarken onu uzun, huzurlu bir öpücükle sar.",
                    toneHint = "♥ NATASHA FİNALİ · Canlı 3D + Aşama 4 Gün Batımı Öpücüğü",
                    nextNodeId = "ch24f_s4_finale_natasha",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 6),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 6)),
                        setFlags = mapOf("s2_romance" to "natasha", "s2_romance_stage" to "4", "s2_ending" to "natasha_love"),
                        branchTag = "Natasha · Mühürlenen Aşk",
                        impactSummary = "Natasha ile 2. Sezon'u gün batımı terasında, sıcak gülüşler ve unutulmaz bir aşk öpücüğüyle kapattın.",
                        isRelationship = true,
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch24f_c3_finale_wanda",
                    label = "Wanda'yla göl kıyısındaki iskeleye yürü — canlı 3D baş başa anı yaşa, kızıl ve mavi ışığın altında onu bütün yaraları iyileştiren derin bir aşkla öp.",
                    toneHint = "♥ WANDA FİNALİ · Canlı 3D + Aşama 4 Göl Kıyısı Öpücüğü",
                    nextNodeId = "ch24f_s4_finale_wanda",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 6),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 6)),
                        setFlags = mapOf("s2_romance" to "wanda", "s2_romance_stage" to "4", "s2_ending" to "wanda_love"),
                        branchTag = "Wanda · Kızıl ve Mavi Aşk",
                        impactSummary = "Wanda ile 2. Sezon'u göl kıyısında, bütün yaraları iyileştiren derin bir öpücükle kapattın.",
                        isRelationship = true,
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch24f_c3_finale_avengers",
                    label = "Steve, Natasha, Vision, Wanda, Voss ve Mira ile büyük eğitim salonunda toplan — \"Avengers... TOPLANIN!\" diyerek yeni çağı birlikte aç!",
                    toneHint = "★ YENİ AVENGERS FİNALİ • Efsanevi Liderlik & Aile",
                    nextNodeId = "ch24f_s4_finale_team",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.VISION to 5, BookCharacterId.VOSS to 4, BookCharacterId.MIRA to 4, BookCharacterId.NATASHA to 3, BookCharacterId.WANDA to 3),
                        avengersTrustDelta = 5,
                        setFlags = mapOf("s2_ending" to "new_avengers"),
                        branchTag = "Yeni Avengers Çağının Lideri",
                        impactSummary = "Yeni Avengers'ın tam merkezinde, Voss ve Mira dâhil sarsılmaz bir aileyle 3. Sezon'a adım attın!",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── EPILOGUE A: NATASHA STAGE 4 LIVE 3D + FINAL KISS ───────────────────
        BookSceneNode(
            id = "ch24f_s4_finale_natasha",
            chapterNumber = 24,
            sceneOrder = 4,
            sceneTitle = "Gün Batımı ve Köfte — Natasha ile Yeni Başlangıç",
            locationTag = "YENİ AVENGERS KOMPLEKSİ · GÖL TERASI • 19:45",
            paragraphs = listOf(
                "Terasın ahşap masasına Hell's Kitchen'dan özel getirttiğin sıcacık kâğıt paketi açtığında, Natasha Romanoff içten, berrak bir kahkaha attı: \"Gerçekten bütün Sokovia'yı kurtardıktan sonra ilk işin bana köfte sözünü tutmak mı oldu, Blackwood?\"",
                "[[LIVE3D|NATASHA|FINALE_TERRACE|GÜN BATIMI TERASI · NATASHA İLE YENİ BAŞLANGIÇ|Savaş bitti. Terasta yalnızca ikiniz, gölden esen akşam rüzgârı ve yıldızlar var. Natasha'nın gözlerine bak, ona yaklaş ya da konuş.]]",
                "\"Söz sözdür, Ajan Romanoff,\" dedin gülerek, yanına oturup. Akşam rüzgârı kızıl saçlarını hafifçe savururken Natasha omzunu omzuna yasladı; sonra yavaşça dönüp sıcak avucunu yanağına koydu. \"Yıllarca kimseyi bu kadar yakınıma sokmadım Aiden,\" diye fısıldadı yeşil gözleri gün batımında altın gibi parlarken. \"Kızıl Oda'nın bana unutturduğu her şeyi senin bakışında yeniden öğrendim. İlk kez yarını korkmadan bekliyorum.\"",
                "Eğildin ve dudaklarınız bu kez savaşın telaşı ya da yağmurun korkusu olmadan, tamamen huzur, güven ve derin bir aşkla buluştu. Uzun, yavaş, iki sezonun bütün yükünü omuzlarınızdan indiren bir öpücüktü. Natasha dudaklarından hafifçe ayrılıp alnını alnına yasladı: \"3. Sezon'da başımıza ne gelirse gelsin — bu sefer iki gölge değil, tek bir kalbiz.\""
            ),
            decisionTitle = "2. Sezon Büyük Finali (4/4) — 3. Sezon'a Hazırlık",
            choicePrompt = "Güneş ufkun ardına inerken son düşüncen ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch24f_c4_ready_s3_nat",
                    label = "Natasha'nın elini sıkıca tut ve yıldızlara bak: \"Asıl Mimar ne planlıyorsa gelsin. Bu kez yalnız değilim — biz hazırız.\"",
                    toneHint = "2. Sezon Büyük Finalini Mühürle",
                    nextNodeId = "ch24f_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3),
                        setFlags = mapOf("s2_completed" to "true"),
                        impactSummary = "2. Sezon tamamlandı — şimdi jenerik sonrası büyük 3. Sezon sürprizi seni bekliyor!"
                    )
                )
            )
        ),

        // ── EPILOGUE B: WANDA STAGE 4 LIVE 3D + FINAL KISS ─────────────────────
        BookSceneNode(
            id = "ch24f_s4_finale_wanda",
            chapterNumber = 24,
            sceneOrder = 4,
            sceneTitle = "Kızıl ve Mavi — Wanda ile Göl Kıyısı",
            locationTag = "YENİ AVENGERS KOMPLEKSİ · GÖL İSKELESİ • 19:45",
            paragraphs = listOf(
                "Göl kıyısındaki ahşap iskelede akşam güneşi suya pembe-altın dalgalar düşürüyordu. Wanda Maximoff başını omzuna yaslayıp parmaklarını senin parmaklarına geçirdi; avuçlarınızda mavi ve kızıl ışık ince bir ateşböceği sürüsü gibi gölün üzerinde dans ediyordu.",
                "[[LIVE3D|WANDA|FINALE_PIER|GÖL İSKELESİ · WANDA İLE YENİ BAŞLANGIÇ|Yıldızlar gölün suyunda parlıyor. Wanda ilk kez kâbussuz bir geceye gülümsüyor — gözlerine bak, ona yaklaş ya da konuş.]]",
                "\"İlk kez zihnimde hiç karanlık yok Aiden,\" diye fısıldadı yumuşak, titreyen bir sesle. \"Bana sadece bir ekip değil... bana yeniden bir yuva verdin. Seni sevmek, bugüne kadar aldığım en cesur karar.\"",
                "Yüzünü nazikçe kendine çevirdin ve gölün sakin sessizliğinde dudaklarınız uzun, şefkatli ve derin bir öpücükte birleşti. Wanda parmaklarını boynuna dolayıp gülümseyerek karşılık verdi — artık ne Sokovia'nın korkusu ne de yalnızlığın soğuğu aranıza girebilirdi."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "s2_pietro_fate",
                    requiredFlagValue = "saved",
                    callbackLabel = "Kardeş Şakası — Pietro'dan Onay!",
                    paragraphs = listOf(
                        "Tam dudaklarınız ayrılırken iskelenin ucunda gümüş bir rüzgâr esti ve **Pietro** iki elinde sıcak çikolatayla belirip sırıttı: \"Hey, hayatımı kurtardın diye gözümün önünde kardeşimi öyle kolayca kapabileceğini sanma Aiden! Neyse... bu seferlik izin veriyorum — ama düğünde en hızlı koşan sağdıç benim!\" Wanda kahkaha atıp kızıl enerjiyle Pietro'nun şapkasını göle uçurdu!"
                    )
                )
            ),
            decisionTitle = "2. Sezon Büyük Finali (4/4) — 3. Sezon'a Hazırlık",
            choicePrompt = "Yıldızlar gölün sularında parlarken son düşüncen ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch24f_c4_ready_s3_wanda",
                    label = "Wanda'nın avucunu kalbine bastır: \"Kızıl ve mavi bir aradayken hiçbir karanlık bizi yıkamaz. 3. Sezon'a hazırız.\"",
                    toneHint = "2. Sezon Büyük Finalini Mühürle",
                    nextNodeId = "ch24f_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 3),
                        setFlags = mapOf("s2_completed" to "true"),
                        impactSummary = "2. Sezon tamamlandı — şimdi jenerik sonrası büyük 3. Sezon sürprizi seni bekliyor!"
                    )
                )
            )
        ),

        // ── EPILOGUE C: NEW AVENGERS ICONIC FORMATION ──────────────────────────
        BookSceneNode(
            id = "ch24f_s4_finale_team",
            chapterNumber = 24,
            sceneOrder = 4,
            sceneTitle = "Avengers... Toplanın!",
            locationTag = "YENİ AVENGERS KOMPLEKSİ · ANA EĞİTİM SALONU • 19:45",
            paragraphs = listOf(
                "Büyük eğitim salonunun geniş çelik kapıları kayarak açıldığında, gün batımının altın ışığı zemine döküldü. **Steve Rogers** kalkanını bileğine takıp yanına geçti; solunda **Natasha Romanoff**, sağında peleriniyle havada zarifçe süzülen **Vision**, kızıl parıltısıyla **Wanda Maximoff**, kanatlarını açan Sam Wilson, zırhıyla Rhodey — ve gözlem balkonunda gururla gülümseyen **Elias Voss ile Mira**!",
                "Steve sana dönüp o unutulmaz, sarsılmaz güvenle gülümsedi: \"İyi bir ekip kurduk Aiden. Belki dünyada hâlâ gölgeler var, ama bu odaya baktığımda geleceğe güveniyorum.\" Sonra derin bir nefes alıp sesini bütün salonda çınlattı: **\"AVENGERS... TOPLANIN!\"**"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "s2_pietro_fate",
                    requiredFlagValue = "saved",
                    callbackLabel = "Gümüş Şimşek Safta!",
                    paragraphs = listOf(
                        "Steve'in sözü bitmeden **Pietro Maximoff** gümüş bir kıvılcımla tam yanına yerleşip sırıttı: \"Beni unutmadınız değil mi Kaptan?!\""
                    )
                )
            ),
            decisionTitle = "2. Sezon Büyük Finali (4/4) — Yeni Çağın Yemini",
            choicePrompt = "Yeni Avengers'ın tam kalbinde 2. Sezon'u kapatırken son sözün ne?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch24f_c4_ready_s3_team",
                    label = "Mavi kuantum kıvılcımını yak ve öne adım at: \"Ne gelirse gelsin — bu aile hiç dağılmayacak!\"",
                    toneHint = "2. Sezon Büyük Finalini Mühürle",
                    nextNodeId = "ch24f_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.VISION to 3, BookCharacterId.VOSS to 2, BookCharacterId.MIRA to 2),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("s2_completed" to "true"),
                        impactSummary = "2. Sezon tamamlandı — şimdi jenerik sonrası büyük 3. Sezon sürprizi seni bekliyor!"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch24f_s5_end",
            chapterNumber = 24,
            sceneOrder = 5,
            sceneTitle = "2. Sezon Sonu — Demir ve Yankı",
            locationTag = "YENİ AVENGERS KOMPLEKSİ · YILDIZLI GECE • 20:00",
            paragraphs = listOf(
                "Böylece **2. Sezon: Demir ve Yankı** (Bölüm XI – XXIV) tamamlandı. Ultron'un demir fırtınası dindi, Sıfır huzura kavuştu, Voss ve Mira özgür hayatlarına adım attı, çoklu evrende yeniden **TEK BİR AIDEN BLACKWOOD** kaldı. Ama dünyanın hükümetleri ve uzak yıldızların ardındaki **Asıl Mimar** henüz son sözünü söylemedi..."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Küllerden doğan yarın, yalnızca kendi karanlığıyla yüzleşip sevmekten vazgeçmeyenlere aittir. — 3. Sezon'da görüşmek üzere...”"
        )
    ).associateBy { it.id }
}
