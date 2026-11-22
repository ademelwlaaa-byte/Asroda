package com.example.book

/**
 * Extended, novel-length Chapters 8 and 9 of "Kozmik Sürükleniş" (Aiden Blackwood — MCU).
 * Each chapter features 4 high-impact decision crossroads, deep callbacks to Chapters 1-7
 * (Natasha/Wanda/Alone paths, Elias Voss, Mira, Kafes Protokolü), and builds directly
 * into the monumental Season 1 Finale in Chapter 10.
 */
object CosmicDriftChapters8To9 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 8,
            romanNumeral = "VIII",
            title = "Kırık Gökler",
            titleEn = "Shattered Skies",
            subtitle = "Thor'un Uyarısı, Soluk Mühür Tarikatı ve Gölge Konsey",
            description = "İstanbul'dan iki hafta sonra New York gökyüzünde 2006'yı andıran yeni bir mavi çatlak, Thor Odinson'ın kozmik uyarısı ve Konsey Direktörü Marcus Vance'in tuzağı.",
            descriptionEn = "Two weeks after Istanbul, a new blue rift appears over New York, Thor Odinson delivers a cosmic warning, and Council Director Marcus Vance springs his trap.",
            estimatedMinutes = 10,
            accentHex = 0xFF38BDF8L,
            startNodeId = "ch8_s1",
            sceneCount = 5,
            decisionCount = 4
        ),
        BookChapterMeta(
            number = 9,
            romanNumeral = "IX",
            title = "Kıyamet Aynası",
            titleEn = "The Doomsday Mirror",
            subtitle = "Kuzey Kutbu Kalesi, Canlı Yayın Tuzağı ve Vaelor'un İnişi",
            description = "Kuzey Kutbu'ndaki Sıfır Noktası, Direktör Vance'in dünyayı ayağa kaldıran hamlesi, Yüce Hakem Vaelor ile ilk çarpışma ve büyük sezon finali öncesi son gece.",
            descriptionEn = "Ground Zero in the Arctic, Director Vance's global broadcast gambit, first clash with High Arbiter Vaelor, and the final night before the season finale.",
            estimatedMinutes = 9,
            accentHex = 0xFFF97316L,
            startNodeId = "ch9_s1",
            sceneCount = 5,
            decisionCount = 4
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM VIII: KIRIK GÖKLER (4 Büyük Karar Noktası)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch8_s1",
            chapterNumber = 8,
            sceneOrder = 1,
            sceneTitle = "Bifrost'un Gölgesi ve İkinci Mavi Çatlak",
            locationTag = "AVENGERS KULESİ • 88. KAT TERASI • 21:30",
            paragraphs = listOf(
                "İstanbul Boğazı'ndaki o altın şafağın üzerinden tam iki hafta geçmişti. Yirmi yıllık kaçışın ardından ilk kez ne ensende S.H.I.E.L.D. uydularının elektrikli iğneleri vardı ne de gece yarısı yatağından fırlamana sebep olan kırmızı alarmlar. Manhattan'ın havası ılıman bir bahar akşamına dönmüştü. Ama bu tuhaf, alışılmadık huzurun fazla sessiz olduğunu — fırtınadan hemen önce rüzgârın durduğu o tekinsiz ana benzediğini — içten içe hissediyordun.",
                "Akşam saat dokuz buçukta Avengers Kulesi'nin seksen sekizinci kat terasında, rüzgâra karşı dururken ensendeki o eski bakır tadı aniden geri döndü. Bu kez bir insan uydusundan ya da yeraltı laboratuvarından gelmiyordu. Başını yukarı kaldırdığında Manhattan'ın bulutlu gece gökyüzünün tam ortasında, 2006 yılında beş yaşındayken gördüğün o korkunç mavi yarığın ince, jilet gibi keskin bir yankısı üç saniyeliğine parlayıp kapandı. Sokaktaki sıradan insanlar bunu sadece uzak bir şimşek sandı; ama senin damarlarındaki kozmik enerji o üç saniyede buz gibi irkildi.",
                "Aynı saniyede gökyüzünden kör edici, gökkuşağı renginde muazzam bir ışık sütunu Kule'nin terasına indi. Rüzgâr kükredi, cam korkuluklar titredi ve mermer zemine kadim Asgard rünleri kazındı. Işık söndüğünde karşında kırmızı pelerini rüzgârda savrulan, elinde Mjolnir'i gevşekçe tutan **Thor Odinson** duruyordu. Yüzünde her zamanki neşeli savaşçı gülümsemesinden tek bir eser yoktu.",
                "\"Stark, Yüzbaşı, Romanoff!\" diye gürledi Thor, ardından gözleri doğrudan senin çarpıcı mavi gözlerine kilitlendi. Thor bir tanrıydı; sıradan insanlar gibi bakışlarını kaçırmadı ama ilk kez bir ölümlüde — ya da ölümlüyü aşan birinde — bu yoğunlukta bir Sonsuzluk Taşı yankısı gördüğü için bir saniye duraksadı. \"Demek Aiden Blackwood sensin. Dokuz Diyar'ın ötesinde bile adını fısıldayanlar var.\"",
                "İçeriden hızla çıkan Steve Rogers, Tony Stark, Natasha Romanoff ve Wanda Maximoff terasa doldu. Tony'nin elinde çoktan tableti vardı: \"Hey Goldilocks, yeni mermer döşemeyi yine yaktın. Bu saatte hayırdır?\"",
                "Thor çekicini beline indirdi, sesi terastaki bütün gürültüyü susturdu: \"İstanbul'da iki hafta önce söndürdüğünüz o kule, evrenin derinliklerine devasa bir kozmik dalga gönderdi. **Soluk Mühür Tarikatı** — eski kozmik tarikatların en acımasızı — o dalgayı duydu. Yüce Hakem **Vaelor** liderliğindeki öncü filoları üç gün içinde Dünya yörüngesine girecek. Onlar evrende Sonsuzluk Taşı düzeyinde güce sahip kontrolsüz hiçbir varlığın yaşamasına izin vermez. Aiden'ı istiyorlar — ve onu koruyan herkesi silmekten çekinmezler.\"",
                "\"Sadece Tarikat değil,\" dedi Natasha Romanoff, elindeki şifreli tableti masaya yansıtarak. Ekranda gümüş saçlı, buz gibi bakışlı bir adamın profili belirdi: **Yüksek Direktör Marcus Vance.** \"Dr. Evelyn Hale teslim olduktan sonra Proje Ayna'nın gerçek patronu — Dünya Güvenlik Konseyi'nin en tepesindeki gölge direktör — ortaya çıktı. Vance, kendi gizli imparatorluğunu korumak için New York'un altındaki eski bir Konsey sığınağından Tarikat'la gizli anlaşma yapmış. Yani hem içeriden hem yukarıdan kuşatılıyoruz.\"",
                "[[DOSSIER|KOZMİK TEHDİT ÖZETİ · KİM KİMDİR?|• Soluk Mühür Tarikatı (Yüce Hakem Vaelor & Öncü Kaelen): İstanbul'daki Rezonans Kulesi'nin uzaya yaydığı mavi dalgayı duyup geldiler. 2006'da Aiden'a mühürlenen kayıp kozmik çekirdeği geri istiyorlar.\n• Direktör Marcus Vance: Dr. Evelyn Hale'in gizli patronu. Aiden'ı Tarikat'a teslim edip karşılığında uzaylı silah teknolojisi almak istiyor.\n• Kıyamet Aynası: Vance'in Tarikat filosunu Dünya'ya indirmek için kurduğu kozmik enerji kafesi.]]",
                "Yukarıda, bulutların arasında o mavi çatlak ikinci kez daha geniş ve daha yakıcı biçimde titredi. İlk hamleyi kimin yapacağı bu savaşın bütün yönünü belirleyecekti."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch7_ending",
                    requiredFlagValue = "natasha_home",
                    callbackLabel = "Boğaz'dan Manhattan'a — Natasha",
                    paragraphs = listOf(
                        "İstanbul'daki o sabah köftesinden beri aranızdaki sessiz denge daha da derinleşmişti. Thor'un uyarısı bittiğinde Natasha yanına yaklaşıp yalnızca ikinizin duyacağı kadar alçak bir sesle konuştu: \"Yine gökyüzü, yine aynı kovalamaca. Ama bu kez yalnız değilsin.\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_ending",
                    requiredFlagValue = "wanda_home",
                    callbackLabel = "Mavi ve Kızıl — Wanda",
                    paragraphs = listOf(
                        "İstanbul'dan döndüğünüzden beri Wanda ile aranızdaki zihinsel uyum hiç bozulmamıştı. Gökyüzündeki mavi çatlak parladığında Wanda hemen yanında durup parmak uçlarında kızıl kalkanı hazırladı: \"Bu kez ne gelirse gelsin, birlikte karşılayacağız.\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_ending",
                    requiredFlagValue = "lone_horizon",
                    callbackLabel = "Özgür Ufuk'tan Dönüş",
                    paragraphs = listOf(
                        "İstanbul'dan sonra kendi yoluna gitmiştin — ama bileğindeki iletişim hattı yarım saat önce kırmızı acil koduyla yandığında tek bir nefesle, yirmi saniyede Manhattan'a geri ışınlandın. Steve seni terasta görünce başını saygıyla eğdi: \"Sözünü tuttun Aiden.\""
                    )
                )
            ),
            decisionTitle = "İlk Hamle — İki Cepheli Tehdit",
            choicePrompt = "Gökyüzünde Soluk Mühür Tarikatı'nın çatlağı, yeraltında ise Direktör Marcus Vance'in ihaneti var. İlk karşı hamlen ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch8_c1_thor_sky",
                    label = "Thor ve Wanda ile Kule'nin en üst çatı kulesine ışınlan — mavi çatlağı aşağıdan kozmik bariyerle kilitleyip Tarikat'ın öncüsünü yere indir.",
                    toneHint = "Kozmik Güç • Thor & Wanda • Doğrudan Savunma",
                    nextNodeId = "ch8_s2_rift",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 4, BookCharacterId.WANDA to 3, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.THOR, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.TRUST, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.COSMIC_AWAKENING to 3,
                            StoryFlagAxis.FIELD_INITIATIVE to 3,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch8_opening" to "thor_sky"),
                        branchTag = "Gök Bariyeri",
                        impactSummary = "Thor'un şimşeği ve senin kozmik kalkanın bulutların arasında birleşti; Tarikat'ın öncüsü Kaelen doğrudan seninle yüzleşmek için yere indi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch8_c1_natasha_bunker",
                    label = "Natasha ile Konsey'in New York yeraltı sığınağına gizlice ışınlan — Direktör Vance'in Tarikat'la kurduğu bağı yerinde kes.",
                    toneHint = "Gölge Sızması • Natasha • Stratejik Darbe",
                    nextNodeId = "ch8_s2_bunker",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.CLINT to 2, BookCharacterId.TONY to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.LOYALTY, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHADOW_ALLIANCE to 3,
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 3
                        ),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch8_opening" to "natasha_bunker"),
                        secretUnlocked = "Vance'in Soluk Mühür Antlaşması",
                        branchTag = "Sığınak Baskını",
                        impactSummary = "Natasha ile Konsey'in gizli sığınağına sızdınız; Vance'in Tarikat öncüsü Kaelen'e şehrin savunma kodlarını sattığını suçüstü yakaladınız.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch8_c1_white_eye_trace",
                    label = "Terasta Beyaz Göz'ü ve psişik görüyü sonuna kadar aç — hem gökyüzündeki çatlağın hem Vance'in zihninin haritasını tek seferde oku.",
                    toneHint = "Beyaz Göz • Psişik Keşif • Yüksek Yorgunluk",
                    nextNodeId = "ch8_s2_rift",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.BRUCE to 3, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.BRUCE, CharacterFlagAxis.CURIOSITY, 3),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 3,
                            StoryFlagAxis.PSYCHIC_RESONANCE to 3,
                            StoryFlagAxis.MENTAL_FATIGUE to 2
                        ),
                        fatigueDelta = 2,
                        mysteryDelta = 3,
                        setFlags = mapOf("ch8_opening" to "psychic_trace"),
                        secretUnlocked = "İkinci Mavi Kıvılcım",
                        branchTag = "Kozmik Harita",
                        impactSummary = "2006 gecesinin sadece tek bir kıvılcım olmadığını, Tarikat'ın asıl aradığı şeyin 'Kıyamet Aynası' olduğunu önceden gördün.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM VIII — SAHNE 2A: GÖKYÜZÜ ÇATLAĞI & KAELEN ─────────────────────
        BookSceneNode(
            id = "ch8_s2_rift",
            chapterNumber = 8,
            sceneOrder = 2,
            sceneTitle = "Tarikat'ın Öncüsü: Kaelen",
            locationTag = "MANHATTAN SEMA & CENTRAL PARK KUZEYİ • 22:15",
            paragraphs = listOf(
                "Avengers Kulesi'nin en yüksek çatı kulesine ışınlandığında soğuk gece rüzgârı etrafında ince bir mavi enerji kabuğu oluşturdu. Thor gökyüzüne fırlayıp şimşekleriyle bulutları gümüş çizgilerle yararken, Wanda ile ikiniz zeminden uzanan mavi-kızıl akımlarla çatlağın kenarlarını kilitlediniz. Ama çatlağın içinden sıradan bir keşif gemisi değil, gümüş-beyaz zırhlı, yüzü soluk porselen bir maskeyle kaplı tek bir kozmik savaşçı indi: **Kaelen — Soluk Mühür'ün Öncüsü.**",
                "Kaelen'in etrafındaki hava zamanın dışındaymış gibi dalgalanıyordu. Central Park'ın ıssız kuzey meydanına indiğinizde çevredeki bütün sokak lambaları bir anda söndü. Kaelen porselen maskesinin ardından doğrudan sana döndü — Thor'u bile görmezden geldi.",
                "\"Demek 2006'nın kaçağı sensin, Aiden Blackwood,\" dedi Kaelen; sesi aynı anda hem fısıltı hem de yankılı bir koro gibi çıkıyordu. \"Yirmi yıldır başka gezegenlere, kırık boyutlara sıçradın ama izini hiç silemedin. Dünya'daki o küçük Konsey Direktörü Marcus Vance bize yerini gümüş tepside sundu. Ve sen hâlâ gerçeği bilmiyorsun: beş yaşındayken içine giren o mavi kıvılcım, Soluk Mühür'ün kayıp yarısıdır. Yüce Hakem Vaelor onu geri almaya geliyor.\"",
                "Thor çekicini savurup öne atıldı ama Kaelen tek bir el hareketiyle şimşeği soğuk, beyaz bir boşlukta sönümleyip Thor'u yirmi metre geriye fırlattı. Ardından gümüş mızrağını doğrudan senin göğsüne çevirdi — mızrağın ucunda senin kanındaki kozmik frekansı bastıran korkutucu bir enerji dönüyordu. Bu savaşta sıradan güç yetmeyecekti."
            ),
            decisionTitle = "Kaelen İle İlk Kozmik Düello",
            choicePrompt = "Tarikat'ın öncüsü Kaelen hem Thor'u geri itti hem de senin enerjini bastıran bir mızrakla saldırıyor. Onu nasıl kıracaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch8_c2_time_break",
                    label = "Zamanı beş saniye mutlak duruşa al — Kaelen'in mızrağını elinden alıp kozmik dalgayla zırhını parçala.",
                    toneHint = "Zaman Bükmesi • Hızlı Zafer • Yüksek Kontrol",
                    nextNodeId = "ch8_s3",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 4, BookCharacterId.STEVE to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.THOR, CharacterFlagAxis.LOYALTY, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 3,
                            StoryFlagAxis.COSMIC_AWAKENING to 2
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch8_kaelen" to "time_broken"),
                        branchTag = "Donmuş Saniye",
                        impactSummary = "Zamanı durdurup Kaelen'in kozmik mızrağını parçaladın; öncü ağır yaralı halde yörüngeye kaçmak zorunda kaldı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch8_c2_absorb_reflect",
                    label = "Mızrağın bastırma enerjisini doğrudan kendi bedeninde em ve iki katı kozmik dalga olarak geri patlat.",
                    toneHint = "Enerji Emilimi • Mutlak Güç • Riskli",
                    nextNodeId = "ch8_s3",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.THOR to 3, BookCharacterId.WANDA to 2),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.COSMIC_AWAKENING to 4,
                            StoryFlagAxis.RED_EYE_INSTABILITY to 1,
                            StoryFlagAxis.MENTAL_FATIGUE to 2
                        ),
                        fatigueDelta = 2,
                        redEyeDelta = 1,
                        setFlags = mapOf("ch8_kaelen" to "absorbed"),
                        branchTag = "Kozmik Yankı",
                        impactSummary = "Tarikat'ın kendi silahının enerjisini emip geri patlattın; Kaelen'in maskesi parçalandı, gücünün sınırı olmadığını bütün ekip gördü.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch8_c2_white_eye_steal",
                    label = "Beyaz Göz'ü son sınırına kadar aç — Kaelen'i felç edip zihninden Tarikat filosunun zayıf noktasını çek.",
                    toneHint = "Beyaz Göz • Zihin Hırsızlığı • Kritik İstihbarat",
                    nextNodeId = "ch8_s3",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.WANDA to 2, BookCharacterId.STEVE to -1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 4,
                            StoryFlagAxis.MENTAL_FATIGUE to 2
                        ),
                        fatigueDelta = 2,
                        mysteryDelta = 2,
                        setFlags = mapOf("ch8_kaelen" to "mind_raided"),
                        secretUnlocked = "Soluk Mühür Çekirdek Zaafı",
                        branchTag = "Zihin Sızması",
                        impactSummary = "Kaelen'in zihninden Vaelor'un ana gemisinin ve Soluk Mühür'ün tek kırılma frekansını çektin.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM VIII — SAHNE 2B: KONSEY SIĞINAĞI & VANCE ──────────────────────
        BookSceneNode(
            id = "ch8_s2_bunker",
            chapterNumber = 8,
            sceneOrder = 2,
            sceneTitle = "Yeraltında İhanet: Direktör Vance",
            locationTag = "MANHATTAN ALTI • GİZLİ KONSEY SIĞINAĞI • 22:15",
            paragraphs = listOf(
                "Natasha Romanoff elini omzuna koyduğunda tek bir sessiz mavi sıçramayla Manhattan'ın altındaki eski metro hatlarının üç yüz metre derinine, Dünya Güvenlik Konseyi'nin resmi haritalarda bile görünmeyen gizli komuta sığınağına indiniz. Natasha indiği saniyede iki güvenlik kamerasını minik elektro-disklerle kör etti; sen ise koridorun ucundaki dört ağır silahlı muhafızın silahlarını tek bir sessiz enerji dalgasıyla kilitleyip bayılttın.",
                "Ana komuta odasının kalın camından içeri baktığınızda kanını donduran manzarayla karşılaştın: gümüş saçlı, kusursuz takım elbiseli **Yüksek Direktör Marcus Vance**, dev bir hologram masasının karşısında Soluk Mühür Tarikatı'nın porselen maskeli öncüsü **Kaelen** ile pazarlık yapıyordu.",
                "\"Anlaşmamız açık,\" diyordu Vance, sesinde en ufak bir ihanet utancı yoktu. \"Aiden Blackwood'u ve onun taşıdığı kozmik kıvılcımı size teslim ediyoruz. Karşılığında Tarikat Dünya'ya dokunmayacak ve bize Kıyamet Aynası'nın kozmik enerji şemalarını bırakacak.\"",
                "\"İnsanlar her zaman ateşle pazarlık yapabileceğini sanır,\" diye fısıldadı Kaelen. Tam o anda başını çevirip doğrudan camın arkasındaki sana baktı. \"Ve bakın kim gelmiş, Direktör.\"",
                "Sığınağın çelik kapıları kırmızı alarmla kilitlendi. Vance acil kaçış asansörüne doğru çekilirken Kaelen gümüş kozmik mızrağını çekip camı parçalayarak üzerinize atıldı. Natasha hemen Vance'in kaçış hattını kesmek için yan koridora fırlarken Kaelen'in ölümcül saldırısı doğrudan sana yöneldi."
            ),
            decisionTitle = "Sığınakta Kaelen İle Çarpışma",
            choicePrompt = "Dar yeraltı sığınağında Kaelen mızrağıyla üzerine geliyor, Vance ise kaçmak üzere. Nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch8_c2_time_break",
                    label = "Zamanı beş saniye mutlak duruşa al — Kaelen'in mızrağını kırıp Natasha'ya Vance'in verilerini alması için alan aç.",
                    toneHint = "Zaman Bükmesi • Natasha İle Uyum",
                    nextNodeId = "ch8_s3",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.STEVE to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHADOW_ALLIANCE to 3,
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 3
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch8_kaelen" to "time_broken"),
                        branchTag = "Donmuş Saniye",
                        impactSummary = "Zamanı durdurup Kaelen'i yere serdin; Natasha Vance'in şifreli Kıyamet Aynası dosyasını çalmayı başardı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch8_c2_absorb_reflect",
                    label = "Kaelen'in kozmik saldırısını göğsünde emip iki katıyla geri patlat — sığınağın tavanını sarsıp onu geri püskürt.",
                    toneHint = "Enerji Emilimi • Mutlak Güç",
                    nextNodeId = "ch8_s3",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.THOR to 2, BookCharacterId.NATASHA to 2),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.COSMIC_AWAKENING to 4,
                            StoryFlagAxis.RED_EYE_INSTABILITY to 1,
                            StoryFlagAxis.MENTAL_FATIGUE to 2
                        ),
                        fatigueDelta = 2,
                        redEyeDelta = 1,
                        setFlags = mapOf("ch8_kaelen" to "absorbed"),
                        branchTag = "Kozmik Yankı",
                        impactSummary = "Dar sığınakta Kaelen'in gücünü kendi silahına çevirdin; Kaelen parçalanmış maskesiyle boyut kapısından kaçtı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch8_c2_white_eye_steal",
                    label = "Beyaz Göz'ü aç — Kaelen'i felç edip zihninden Tarikat'ın Soluk Mühür zaafını söküp al.",
                    toneHint = "Beyaz Göz • Kritik İstihbarat",
                    nextNodeId = "ch8_s3",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 4,
                            StoryFlagAxis.MENTAL_FATIGUE to 2
                        ),
                        fatigueDelta = 2,
                        mysteryDelta = 2,
                        setFlags = mapOf("ch8_kaelen" to "mind_raided"),
                        secretUnlocked = "Soluk Mühür Çekirdek Zaafı",
                        branchTag = "Zihin Sızması",
                        impactSummary = "Kaelen kaçmadan önce zihninden Vaelor'un ana gemisinin tek kırılma frekansını aldın.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM VIII — SAHNE 3: YORGUNLUK VE SESSİZ BAĞLAR ────────────────────
        BookSceneNode(
            id = "ch8_s3",
            chapterNumber = 8,
            sceneOrder = 3,
            sceneTitle = "Fırtınanın Ortasında Bir Nefes",
            locationTag = "AVENGERS KULESİ • 84. KAT • 01:10",
            paragraphs = listOf(
                "Kule'ye döndüğünüzde gece yarısını çoktan geçmişti. Central Park'taki ya da yeraltı sığınağındaki o ilk temas, sıradan bir sokak çatışması değildi. Kaelen'in gümüş mızrağından yayılan o soğuk, metalik kozmik bastırma enerjisi, hızlı iyileşmeni bile normalden çok daha uzun süre zorlamıştı. Kollarındaki ince yanık izleri yavaşça kapanırken omuzlarında, Prag'da iki aylı o yabancı gezegene sıçradığın geceden beri hissetmediğin kadar ağır bir çekilme vardı.",
                "Banyoda lavabonun mermer kenarına tutunup yüzüne soğuk su çarptın. Başını kaldırıp aynadaki yansımana baktığında iki saniyeliğine nefesin kesildi: çarpıcı mavi gözlerin loş ışıkta bile her zamankinden çok daha parlak, neredeyse elektrikli bir yoğunlukla yanıyordu. Beş yaşında yangın merdiveninde içine yerleşen o Sonsuzluk Taşı düzeyindeki mavi kıvılcım, Kaelen'in silahıyla temas ettikten sonra yirmi yıldır uyuduğu derinlikten tamamen uyanmaya başlamıştı. İçindeki güç artık sadece savunma için değil, yaklaşan fırtınaya karşılık vermek için uğulduyordu.",
                "Yukarıdaki seksen beşinci katın ana salonunda Tony Stark, Bruce Banner ve Thor Odinson, Soluk Mühür Tarikatı'nın bin yıllık Asgard kayıtlarını ve Direktör Marcus Vance'in kaçtığı Kuzey Kutbu koordinatlarını çözmeye çalışıyordu. Thor'un sesi koridorun sonundan bile boğuk bir gök gürültüsü gibi geliyordu: \"Vaelor merhamet bilmez Stark. Onun gözünde bir gezegenin hayatta kalması, kozmik düzenin bozulmasından daha önemsizdir.\"",
                "\"Bunu duymak harika oldu Goldilocks,\" diye homurdandı Tony. \"Tam da sabah kahvemi yudumlarken ihtiyacım olan motivasyon konuşması.\"",
                "Senin ise kırk sekiz saat sonra başlayacak o devasa savaştan önce yalnızca bir saatlik sessizliğin vardı. Seksen dördüncü katın loş koridorundan geçtiğinde üç farklı ışık yanıyordu: silah bakım masasında tek başına Kuzey Kutbu sığınağının eski Sovyet ve Konsey haritalarına bakan Natasha Romanoff, balkonda soğuk gece havasında parmak uçlarındaki kızıl enerjiyi yeni bir frekansa dengelemeye çalışan Wanda Maximoff ve brifing salonunun köşesinde Asgard yıldız haritalarını açmış seni bekleyen Thor Odinson.",
                "Her zamanki gibi kimse kapını çalmadı. Kimse adını seslenmedi, kimse senden bir söz ya da yakınlık talep etmedi. Bu bir saati kiminle — ya da kendi duvarlarının ardında tek başına mı — geçireceğin yine yalnızca Aiden Blackwood'un kendi nadir iradesine kalmıştı."
            ),
            decisionTitle = "Savaş Öncesi Hazırlık — Kiminle Yan Yana?",
            choicePrompt = "Soluk Mühür filosu yörüngeye yaklaşırken bu kritik geceyi kiminle değerlendireceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch8_c3_natasha",
                    label = "Natasha'nın yanına git — Vance'in sığınak planlarını birlikte çözüp aranızdaki sessiz güveni pekiştir.",
                    toneHint = "Natasha • Gölge Planı • Bilinçli Yakınlık",
                    nextNodeId = "ch8_s4",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.LOYALTY, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHADOW_ALLIANCE to 3,
                            StoryFlagAxis.HUMANITY_ANCHOR to 2
                        ),
                        setFlags = mapOf("ch8_ally_night" to "natasha"),
                        branchTag = "Natasha İle Gece",
                        impactSummary = "Natasha ile Kuzey Kutbu kalesinin tek kör noktasını çıkardınız; sabah olmadan önce aranızda sözlere gerek bırakmayan kırılmaz bir bağ kuruldu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch8_c3_wanda",
                    label = "Wanda'nın yanına çık — iki kozmik gücün ortak rezonansını Vaelor'a karşı birlikte eğit.",
                    toneHint = "Wanda • Kozmik Uyum • Bilinçli Yakınlık",
                    nextNodeId = "ch8_s4",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.LOYALTY, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.PSYCHIC_RESONANCE to 4,
                            StoryFlagAxis.HUMANITY_ANCHOR to 2
                        ),
                        setFlags = mapOf("ch8_ally_night" to "wanda"),
                        branchTag = "Wanda İle Rezonans",
                        impactSummary = "Wanda ile mavi ve kızıl enerjinizi tek bir kalkan frekansında birleştirdiniz; zihinleriniz ilk kez tam bir huzurla senkronize oldu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch8_c3_thor",
                    label = "Thor ile Asgard yıldız haritalarını incele — Soluk Mühür Tarikatı'nın bin yıllık savaş taktiklerini öğren.",
                    toneHint = "Thor • Kozmik Strateji • Asgard İttifakı",
                    nextNodeId = "ch8_s4",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 4, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.THOR, CharacterFlagAxis.LOYALTY, 3),
                            Triple(BookCharacterId.THOR, CharacterFlagAxis.TRUST, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.COSMIC_AWAKENING to 3,
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 2
                        ),
                        setFlags = mapOf("ch8_ally_night" to "thor"),
                        secretUnlocked = "Asgard Kozmik Kırılma Taktikleri",
                        branchTag = "Asgard İttifakı",
                        impactSummary = "Thor sana Soluk Mühür'ün geçmişte yok ettiği dünyaları ve Vaelor'un tek onur zayıflığını anlattı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch8_c3_alone",
                    label = "Kimsenin yanına gitme — çatıya çıkıp kendi kozmik sınırlarını sessizce test et.",
                    toneHint = "Yalnız Kurt • Bağımsız • Saf Odak",
                    nextNodeId = "ch8_s4",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 1, BookCharacterId.WANDA to 1),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 4,
                            StoryFlagAxis.WHITE_EYE_CONTROL to 2
                        ),
                        setFlags = mapOf("ch8_ally_night" to "alone"),
                        branchTag = "Yalnız Hazırlık",
                        impactSummary = "Duygusal bağlardan uzak durup bütün odağını kendi gücünün mutlak kontrolüne verdin.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── BÖLÜM VIII — SAHNE 4: NICK FURY'NİN ACİL KODU ───────────────────────
        BookSceneNode(
            id = "ch8_s4",
            chapterNumber = 8,
            sceneOrder = 4,
            sceneTitle = "Kıyamet Aynası Devrede",
            locationTag = "AVENGERS KULESİ • KOMUTA MERKEZİ • 05:00",
            paragraphs = listOf(
                "Şafak sökmeden bir saat önce Kule'nin bütün cam duvarları acil durum kırmızısına döndü. FRIDAY'in sesi alarm tonuyla yükseldi ama Tony tek hareketle sesi kıstı. Asansör kapıları açıldığında gelen bir baskın timi değil, bizzat **Nick Fury** idi — siyah deri paltosu sabah yağmurundan ıslanmış, tek gözünde yıllardır kimsenin görmediği kadar karanlık, buz gibi bir ciddiyetle brifing salonuna yürüdü.",
                "\"Vaktimiz bitti,\" dedi Fury, elindeki şifreli siyah veri küpünü cam masanın ortasına fırlatarak. Devasa üç boyutlu Dünya haritası tavana kadar açıldı.",
                "[[DOSSIER|NICK FURY — ŞİFRELİ VERİ KÜPÜ (KIYAMET AYNASI)|Yüksek Direktör Marcus Vance Konsey'in bütün acil durum yetkilerini gasp etti. Üçlü rezonans kafesi noktaları: Kuzey Kutbu (Sıfır Noktası), Londra, Pasifik. Üst atmosferde 3 Soluk Mühür kruvazörü yörüngeye girdi.]]",
                "Tony Stark hologramı iki eliyle büyüttü; her zamanki alaycı yüzü bir anda kireç gibi oldu: \"Bu deli Dünya'nın etrafında üçlü bir kozmik rezonans kafesi kuruyor! Bakın, enerji akışı dışarıya değil içeriye kilitleniyor — amacı sadece Aiden'ı yakalayıp teslim etmek değil. Soluk Mühür filosunu doğrudan Kuzey Kutbu'ndaki Sıfır Noktası'na indirip Dünya'yı yeni bir kozmik imparatorluğun kapısına çevirecek!\"",
                "\"Ve aynı anda,\" dedi Bruce Banner, elindeki tableti titreyerek masaya bırakırken, \"üst atmosferde üç dev Soluk Mühür kruvazörü yörüngeye girdi. Termal kalkanları Asgard teknolojisinden bile farklı bir dalga boyunda çalışıyor. İki kritik hedefimiz var ama bütün ekibi ikiye bölersek iki cephede de eziliriz.\"",
                "Steve Rogers ellerini masaya dayayıp doğrudan senin gözlerinin içine baktı. \"Aiden, bu savaşın ekseni sensin. Kaelen'le de Vance'in sistemleriyle de ilk elden yüzleştin. İlk büyük darbeyi nereye indiriyoruz?\""
            ),
            decisionTitle = "Savaşın İlk Cephesi — Kritik Strateji",
            choicePrompt = "Bölüm IX'un açılış cephesini belirliyorsun. İlk olarak Direktör Vance'in Kuzey Kutbu kalesini mi, yoksa yörüngedeki Tarikat öncü filosunu mu vuracaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch8_c4_arctic",
                    label = "Önce Kuzey Kutbu'ndaki Sıfır Noktası'na vur — Vance'in Kıyamet Aynası'nı kaynağında yok et.",
                    toneHint = "Kuzey Kutbu Baskını • Vance İle Hesaplaşma",
                    nextNodeId = "ch8_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.NATASHA to 3, BookCharacterId.CLINT to 2),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 3,
                            StoryFlagAxis.AVENGERS_TRUST to 3
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch8_target" to "arctic_first"),
                        branchTag = "Kuzey Kutbu Cephesi",
                        impactSummary = "Yerdeki ihaneti önce kesmeye karar verdin; Quinjet ve Helicarrier rotayı buz altındaki Sıfır Noktası'na çevirdi.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch8_c4_orbit",
                    label = "Önce Tony'nin yüksek irtifa Quinjet'iyle Thor'la üst atmosfere çık — öncü kruvazörün güvertesine ışınlanıp iletişim kulesini kır, sonra Kuzey Kutbu'na in.",
                    toneHint = "Yörünge Çarpışması • Kozmik Gösteri • Yüksek Risk",
                    nextNodeId = "ch8_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 4, BookCharacterId.TONY to 3, BookCharacterId.WANDA to 2),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.COSMIC_AWAKENING to 4,
                            StoryFlagAxis.FIELD_INITIATIVE to 3,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch8_target" to "orbit_first"),
                        branchTag = "Yörünge Taarruzu",
                        impactSummary = "Gökyüzündeki tehdidi daha yere inmeden sarsmayı seçtin; Thor gürleyerek çekicini kaldırdı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM VIII — FİNAL ──────────────────────────────────────────────────
        BookSceneNode(
            id = "ch8_s5_end",
            chapterNumber = 8,
            sceneOrder = 5,
            sceneTitle = "Gökler Kırılmadan Önce",
            locationTag = "AVENGERS KULESİ • HANGAR • 05:45",
            paragraphs = listOf(
                "Hangarın dev kapıları açıldığında Manhattan'ın üzerindeki gökyüzü artık normal sabah mavisi değildi. Bulutların en yüksek katmanında ince, ürpertici bir soluk ışık ağı örülmeye başlamıştı — Soluk Mühür Tarikatı'nın filosu Dünya'nın kapısındaydı.",
                "Steve Rogers kalkanını sırtına bağladı, Thor pelerinini savurdu, Wanda ellerindeki kızıl ışığı topladı ve Natasha Quinjet'in pilot koltuğuna geçmeden önce dönüp son kez senin gözlerinin içine baktı. Yirmi yıl boyunca kaçtığın bütün gölgeler — Konsey, S.H.I.E.L.D. ihanetleri, kozmik tarikatlar ve beş yaşındaki o mavi gece — nihayet tek bir savaşta birleşiyordu.",
                "Quinjet'in motorları kükredi ve hangar kapılarından fırtınalı gökyüzüne süzüldünüz. Bu kez avlanan sen değildin — kırık göklerin altına Avengers ile omuz omuza, doğrudan kendin gidiyordun."
            ),
            isChapterEnd = true,
            chapterEndQuote = "Gökyüzü bir kez kırıldığında, onu yeniden dikmek için içindeki fırtınadan korkmamalısın."
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM IX: KIYAMET AYNASI (4 Büyük Karar Noktası — Sezon Finaline Giriş)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch9_s1",
            chapterNumber = 9,
            sceneOrder = 1,
            sceneTitle = "Buzun Altındaki Sıfır Noktası",
            locationTag = "KUZEY KUTBU • KONSEY KALESİ • -38°C",
            paragraphs = listOf(
                "Kuzey Kutbu'nun sonsuz, kör edici beyaz fırtınasının ortasında, yüz metrelik kadim buz tabakasının içine oyulmuş siyah titanyum kale karanlık bir hançer gibi gökyüzüne yükseliyordu. Rüzgâr eksi otuz sekiz derecede metal duvarlarda uluyor, buz parçaları Quinjet'in zırhlı camlarına kurşun gibi çarpıyordu. Burası **Proje Ayna — Sıfır Noktası**'ydı. 2006 yılında beş yaşındayken New York gökyüzünde açılan o mavi kıvılcımın, annenin elini bıraktığın o gecenin ilk planları bu buzdan duvarların içinde çizilmişti.",
                "Dış savunma kuleleri fırtınayı yaran kör edici mavi plazma ateşleri açarken Avengers ekibi üç koldan inişe geçti. Thor Odinson gök gürültüsüyle buz fırtınasının üstünden inip kuzey radar kulesini tek darbede toz etti; Tony Stark zırhının tam itici gücüyle uçaksavar bataryalarının arasında manevra yapıp kuleleri susturdu; Steve Rogers ve Clint Barton ana buz köprüsündeki ağır makineli hattını kalkan ve patlayıcı oklarla yardı. Sen ise soğuğun tenini bile işlemediği saf mavi kozmik kalkanının içinde, rüzgârı yararak doğrudan kalenin yirmi metrelik siyah titanyum kapısına yürüdün.",
                "İçeri girdiğinizde koridorlar buz gibi değil, tuhaf ve rahatsız edici biçimde sıcaktı — ayaklarınızın elli metre altındaki dev **Kıyamet Aynası** reaktörü tam kapasite uğulduyordu. Duvarlardaki cam boruların içinden senin kanındaki enerjiye benzeyen parlak mavi akımlar geçiyordu. Ana komuta köprüsünün kalın kurşun camının arkasında **Yüksek Direktör Marcus Vance** elleri arkasında, hiç paniklemeden, sanki bir satranç ustası rakibinin son hamlesini bekliyormuş gibi sana bakıyordu.",
                "Önündeki geniş çelik platformda elli ağır karbon zırhlı Konsey komandosu ve doğrudan senin kozmik enerjini emip bastırmak için tasarlanmış üç devasa nötralizasyon kulesi mevzilenmişti. Hoparlörlerden Vance'in soğuk, kontrollü sesi yankılandı: \"Hoş geldin, Denek 7-A. Yirmi yıl geciktin ama sonunda ait olduğun yere — doğduğun laboratuvara — geldin. Bakalım Avengers'ın o meşhur mucizesi üçlü bastırma alanı altında ne kadar nefes alabilecek.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch8_target",
                    requiredFlagValue = "orbit_first",
                    callbackLabel = "Yörünge Darbesinin Meyvesi",
                    paragraphs = listOf(
                        "Kuzey Kutbu'na inmeden önce Thor ve Tony ile üst atmosferde vurduğunuz Tarikat öncü kruvazörü hâlâ dumanlar içinde sürüklendiği için, Vance'in beklediği uzay takviyesi yirmi dakika gecikmişti. Zaman sizin lehinize işliyordu."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch8_target",
                    requiredFlagValue = "arctic_first",
                    callbackLabel = "Erken Baskın Avantajı",
                    paragraphs = listOf(
                        "Doğrudan Kuzey Kutbu'na indiğiniz için Vance'in dış savunma kalkanları henüz tam şarj olmamıştı; Steve ve Natasha ana kapıyı rekor sürede kırdı."
                    )
                )
            ),
            decisionTitle = "Sıfır Noktası Baskını — Kaleye Giriş",
            choicePrompt = "Elli ağır zırhlı asker ve üç kozmik bastırma kulesi komuta köprüsünü koruyor. Vance'e nasıl ulaşacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch9_c1_shatter",
                    label = "Kozmik kalkanını genişletip bütün bastırma dalgasını kır — Steve ve Thor ile doğrudan komuta köprüsüne yararak gir.",
                    toneHint = "Doğrudan Taarruz • Durdurulamaz Güç",
                    nextNodeId = "ch9_s2_clash",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 3, BookCharacterId.STEVE to 3),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.COSMIC_AWAKENING to 3,
                            StoryFlagAxis.FIELD_INITIATIVE to 3
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch9_entry" to "shatter"),
                        branchTag = "Kalkan Yarma",
                        impactSummary = "Bastırma kulelerini tek kozmik dalgayla parçalayıp elli komandoyu sıfır kayıpla dağıttınız.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch9_c1_time_sabotage",
                    label = "Zamanı dört saniye dondurup doğrudan Kıyamet Aynası reaktörünün ana hatlarını sök — kuleleri enerjisiz bırak.",
                    toneHint = "Zaman Bükmesi • Cerrahi Sabotaj",
                    nextNodeId = "ch9_s2_clash",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.BRUCE to 3),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 3,
                            StoryFlagAxis.WHITE_EYE_CONTROL to 2
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch9_entry" to "sabotage"),
                        branchTag = "Zaman Sabotajı",
                        impactSummary = "Reaktörün besleme hatlarını dört saniyede kestin; Vance'in bütün savunma kuleleri tek kurşun sıkamadan söndü.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch9_c1_shadow_flank",
                    label = "Natasha ve Clint ile havalandırma şaftından komuta köprüsünün tam arkasına ışınlan — Vance'i kendi masasında gafil avla.",
                    toneHint = "Gölge Kuşatması • Natasha & Clint",
                    nextNodeId = "ch9_s2_clash",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.CLINT to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.CLINT, CharacterFlagAxis.RESPECT, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHADOW_ALLIANCE to 3,
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 3
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch9_entry" to "shadow_flank"),
                        branchTag = "Gölge Kuşatması",
                        impactSummary = "Tek bir alarm bile tetiklemeden Vance'in sırtında belirdiniz; Natasha tabancasını doğrudan Direktör'ün ensesine dayadı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM IX — SAHNE 2: CANLI YAYIN TUZAĞI & GEÇMİŞİN MÜTTEFİKLERİ ──────
        BookSceneNode(
            id = "ch9_s2_clash",
            chapterNumber = 9,
            sceneOrder = 2,
            sceneTitle = "Dünyanın Gözleri Önünde",
            locationTag = "KUZEY KUTBU • KOMUTA KÖPRÜSÜ",
            paragraphs = listOf(
                "Komuta köprüsünün kalın kurşun camları mavi enerji basıncıyla parçalanıp içeri girdiğinizde, elli zırhlı komando ve bastırma kuleleri artık devre dışıydı. Ama Yüksek Direktör Marcus Vance bir adım bile geri çekilmedi. Yüzünde buz gibi, hastalıklı bir kibirle karışık soğukkanlı bir gülümseme vardı. Sağ elinin başparmağı, ana konsolun ortasındaki şeffaf kapağı açılmış kırmızı bir uydu anahtarının hemen üzerindeydi.",
                "\"Beni burada öldürebilirsiniz, Blackwood,\" dedi Vance, sesi komuta köprüsünün soğuk sessizliğinde yankılandı. \"Ya da Yüzbaşı'nın o meşhur kelepçelerini takıp beni mahkemeye çıkarabilirsiniz. Ama parmağım bu düğmeye indiği an, bütün dünya televizyonları, haber ağları ve akıllı telefonlar canlı olarak bu odaya bağlanacak. Beş yaşında ailesini yok eden, hükümet ajanlarının hafızasını silen, uzaylı bir tarikatı Dünya'ya çeken o kontrolsüz **canavarın** yüzünü sekiz milyar insan aynı anda görecek!\"",
                "O kelime yine ağzından döküldü: *Canavar.* Odadaki hava bir anda buz kesti; tavan lambaları yüksek gerilim yemiş gibi titreşti. Steve Rogers kalkanını kaldırıp öne atıldı: \"Sakın yapma Vance!\" Ama tam o saniyede Vance gizli bir yedek bölmenin kilidini açtı ve ikinci bir ağır zırhlı muhafız timi yan kapılardan içeri akın etti. Vance kırmızı düğmeye bastı — dev duvar ekranında küresel uydu bağlantısı hızla dolmaya başladı: yüzde elli... altmış... yetmiş beş... Sekiz milyar insanın gözü birkaç saniye içinde doğrudan senin çarpıcı mavi gözlerine kilitlenmek üzereydi."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch5_voss_fate",
                    requiredFlagValue = "protected",
                    callbackLabel = "Elias Voss'un Geri Dönüşü",
                    paragraphs = listOf(
                        "Tam yan kapıdan ağır zırhlı timler girerken koridorda keskin bir kırmızı enerji patlaması yankılandı! Kule'de Raft kafesinden kurtardığın **Elias Voss**, soluk kırmızı kalkanıyla kapıyı tek başına tutarak ortaya çıktı: \"Sana bir borcum vardı 7-A! Vance ile işini bitir, bu kapı bende!\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_finale_act",
                    requiredFlagValue = "saved_mira",
                    callbackLabel = "Mira'nın Kefareti",
                    paragraphs = listOf(
                        "Tavanın üstünden berrak, artık dengelenmiş bir kızıl ışık indi! İstanbul tünellerinde kendi canın pahasına kurtardığın **Mira**, gözlerinde öfke değil sarsılmaz bir kararlılıkla Vance'in nötralizasyon toplarını parçaladı: \"Bize canavar diyenleri bugün bitiriyoruz Aiden.\""
                    )
                )
            ),
            decisionTitle = "Küresel Yayın — Dünyaya Ne Göstereceksin?",
            choicePrompt = "Vance'in küresel yayını saniyeler içinde açılmak üzere. Gizli kalmayı mı seçeceksin, yoksa dünyanın seni ilk kez görmesine izin mi vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch9_c2_cut_feed",
                    label = "Enerji darbesiyle uydu vericisini parçala — kimliğini gölgede tut ve Vance'i sessizce etkisiz hale getir.",
                    toneHint = "Gizlilik • Gölgede Kal • Sıfır Açığa Çıkma",
                    nextNodeId = "ch9_s3",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.CLINT to 2),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.MYSTERY_AURA to 3,
                            StoryFlagAxis.SHADOW_ALLIANCE to 2
                        ),
                        setFlags = mapOf("ch9_broadcast" to "cut"),
                        branchTag = "Gölgede Kaldın",
                        impactSummary = "Yayın yüzde doksan sekizde kesildi; dünya adını hiç duymadı, gizem perdesi korundu.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch9_c2_open_world",
                    label = "Yayını kesme — kameranın karşısına geç, Vance'in ihanetini tüm dünyaya ifşa et ve saklanmayı sonsuza dek bitir.",
                    toneHint = "Açığa Çıkış • Tarihi An • Yüksek Etki",
                    nextNodeId = "ch9_s3",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.TONY to 4, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.PUBLIC_EXPOSURE to 5,
                            StoryFlagAxis.AVENGERS_TRUST to 3,
                            StoryFlagAxis.HUMANITY_ANCHOR to 3
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch9_broadcast" to "public"),
                        branchTag = "Dünyaya İlan",
                        impactSummary = "Sekiz milyar insan Aiden Blackwood'u ve çarpıcı mavi gözlerini ilk kez gördü; Vance'in ihaneti canlı yayında çöktü.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch9_c2_white_eye_turn",
                    label = "Beyaz Göz'ü aç — Vance'in kendi komuta timini ona karşı çevir ve yayını Vance'in kendi itirafına dönüştür.",
                    toneHint = "Beyaz Göz • Mutlak Manipülasyon • Acımasız Zekâ",
                    nextNodeId = "ch9_s3",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.TONY to 2, BookCharacterId.STEVE to -2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.CURIOSITY, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 4,
                            StoryFlagAxis.DARK_TEMPTATION to 2,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch9_broadcast" to "vance_confession"),
                        branchTag = "Vance'in İtirafı",
                        impactSummary = "Vance canlı yayında kendi ağzıyla Proje Ayna'yı ve Tarikat'la yaptığı ihaneti itiraf etti; Konsey tek dakikada dağıldı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM IX — SAHNE 3: YÜCE HAKEM VAELOR'UN İNİŞİ ──────────────────────
        BookSceneNode(
            id = "ch9_s3",
            chapterNumber = 9,
            sceneOrder = 3,
            sceneTitle = "Yüce Hakem Vaelor",
            locationTag = "KUZEY KUTBU • YIKILAN KALE AVLUSU",
            paragraphs = listOf(
                "Vance'in bütün oyunu çöktüğü ve Kıyamet Aynası'nın ana vericisi sustuğu saniyede Kuzey Kutbu'nun karanlık gökyüzü gök gürültüsüyle ortadan ikiye yarıldı. Bu kez Manhattan'daki gibi ince bir çatlak değildi — kilometrelerce genişliğinde, etrafındaki karı, buz kütlelerini ve kaleden kopan çelik levhaları bile yukarı doğru çeken korkunç, bembeyaz ve derin mavi bir kozmik boyut kapısıydı. Kaledeki bütün elektronik cihazlar ve Tony'nin kaskındaki yardımcı ekranlar aynı anda parazitlenip yandı.",
                "Kapının tam ortasından tek bir devasa figür ağır ağır, hiçbir acele etmeden buz kaplı avluya indi: üç metre boyunda, eski ölü yıldızların metalinden dövülmüş gümüş-siyah zırhıyla **Yüce Hakem Vaelor.** Sağ elinde Sonsuzluk Taşı düzeyinde kadim ve baş döndürücü bir kozmik eser olan **Soluk Mühür**'ü tutuyordu. Bastığı yerde on metre kalınlığındaki buz tabakası anında ince bir toza dönüştü.",
                "\"Bütün bu küçük ölümlü oyunları bitti,\" dedi Vaelor. Sesi havadan değil, doğrudan herkesin kafatasının içinden bir gök gürültüsü gibi yankılandı. \"Aiden Blackwood. Yirmi yıl boyunca kıtalar ve boyutlar arasında kaçtın ama içindeki o mavi kıvılcım bu gezegene ait değil. O, Soluk Mühür'ün kayıp yarısıdır. Onu ya şimdi diz çöküp kendi rızanla teslim edersin, ya da yarın güneş doğduğunda doğduğun o New York şehrini gökyüzünden silip küllerinden kendim alırım.\"",
                "\"Bu gezegenin koruyucuları hâlâ ayakta, Vaelor!\" diye kükredi Thor Odinson ve Mjolnir'den gökyüzünü yaran dev bir şimşek indirdi. Ama Vaelor sadece sol elini kaldırdı, Asgard şimşeğini soğuk bir boşlukta kıvılcım bile çıkarmadan yuttu ve Soluk Mühür'ü sertçe buzun üstüne vurdu.",
                "Muazzam, kör edici bir kozmik şok dalgası bütün kaleyi kâğıt gibi parçalayarak Avengers ekibinin üzerine doğru yuvarlandı. Steve kalkanını öne verdi, Natasha ve Clint sipere atladı ama o dalga çelik duvarları bile toz ediyordu. O saniyede kemiklerinde hissettin: bu, yirmi beş yıllık hayatında karşılaştığın ilk gerçek kozmik kıyametti."
            ),
            decisionTitle = "Vaelor'un Şok Dalgası — İlk Büyük Sınav",
            choicePrompt = "Soluk Mühür'ün yıkıcı kozmik dalgası tüm ekibi ezip geçmek üzere. Nasıl karşılayacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch9_c3_solo_tank",
                    label = "Ekibin önüne geç ve bütün kozmik şok dalgasını tek başına kendi kalkanınla göğüsle — Vaelor'a geri adım atmayacağını göster.",
                    toneHint = "Tek Kişilik Kalkan • Kahramanca • Ağır Yorgunluk",
                    nextNodeId = "ch9_s4",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 4, BookCharacterId.WANDA to 3, BookCharacterId.THOR to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.LOYALTY, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 4,
                            StoryFlagAxis.HUMANITY_ANCHOR to 3,
                            StoryFlagAxis.MENTAL_FATIGUE to 2
                        ),
                        fatigueDelta = 2,
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch9_vaelor_clash" to "solo_shield"),
                        branchTag = "Kırılmaz Kalkan",
                        impactSummary = "Dizlerin buzun içine gömüldü ama kalkanın kırılmadı; bütün ekip sağ kurtuldu, Vaelor ilk kez şaşkınlıkla geri çekildi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch9_c3_trinity",
                    label = "Wanda ve Thor ile üçlü kozmik kalkan kur — mavi, kızıl ve şimşeği birleştirip dalgayı Vaelor'a geri yansıt.",
                    toneHint = "Üçlü İttifak • Wanda & Thor • Kozmik Sinerji",
                    nextNodeId = "ch9_s4",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4, BookCharacterId.THOR to 4, BookCharacterId.TONY to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.THOR, CharacterFlagAxis.LOYALTY, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.COSMIC_AWAKENING to 4,
                            StoryFlagAxis.PSYCHIC_RESONANCE to 3
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch9_vaelor_clash" to "trinity"),
                        branchTag = "Üçlü Kalkan",
                        impactSummary = "Mavi kozmik güç, kızıl kaos ve Asgard şimşeği birleşti; Vaelor'un zırhında ilk çatlağı açtınız.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch9_c3_dimension_strike",
                    label = "Kırık zaman çizgisine yarım saniyelik boyut sıçraması yapıp doğrudan Vaelor'un arkasından vur — Soluk Mühür'ü çatlat.",
                    toneHint = "Boyut Sıçraması • Ölümcül Sürpriz • Yüksek Risk",
                    nextNodeId = "ch9_s4",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.NATASHA to 3, BookCharacterId.THOR to 2),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.COSMIC_AWAKENING to 4,
                            StoryFlagAxis.RED_EYE_INSTABILITY to 1
                        ),
                        fatigueDelta = 2,
                        redEyeDelta = 1,
                        setFlags = mapOf("ch9_vaelor_clash" to "dimension_strike"),
                        secretUnlocked = "Soluk Mühür'deki İlk Çatlak",
                        branchTag = "Boyut Hançeri",
                        impactSummary = "Vaelor'un beklemediği bir boyuttan çıkıp Soluk Mühür'de ince bir çatlak açtın; final savaşında bu çatlak en büyük kozun olacak.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM IX — SAHNE 4: SEZON FİNALİ ÖNCESİ SON GECE ────────────────────
        BookSceneNode(
            id = "ch9_s4",
            chapterNumber = 9,
            sceneOrder = 4,
            sceneTitle = "Yıldızların Kırılmasından Önceki Son Gece",
            locationTag = "S.H.I.E.L.D. HELICARRIER GÜVERTESİ • NEW YORK YAKLAŞIMI • 02:30",
            paragraphs = listOf(
                "Yüce Hakem Vaelor Kuzey Kutbu'ndaki yıkık kaleden geri çekildiğinde arkasında tek bir buz gibi ültimatom bıraktı: şafakta, güneşin doğması gereken saatte Manhattan'ın üzerinde son hesaplaşma verilecekti. Ama buz kalesi çökerken ortada tehlikeli bir açık kalmıştı — şok dalgasının yarattığı kaosta **Yüksek Direktör Marcus Vance**, gizli bir Konsey kaçış kapsülüyle enkazdan sıyrılıp New York yönüne kaçmayı başarmıştı. Quinjet'le dönüp Atlantik üzerinde gizli modda süzülen Nick Fury'nin dev Helicarrier'ına indiğinizde güverte soğuk okyanus rüzgârıyla uğulduyordu. Aşağıda, uzak batı ufkunda Manhattan'ın altın ışıkları sisin içinden parlıyordu — doğduğun, büyüdüğün, beş yaşında annenin elini bıraktığın ve yirmi yıl sonra geri döndüğün şehir.",
                "Yarın sabah verilecek savaş, yirmi yıllık bütün hikâyenin büyük sezon finali olacaktı. İçerideki uçuş güvertesinde ve brifing salonunda herkes kendi sessiz hazırlığındaydı: Tony Stark hasarlı zırhının göğüs reaktörünü yüksek kozmik direnç moduna çekiyor, Steve Rogers ve Thor Odinson Manhattan köprülerinin tahliye hatlarını çiziyor, Bruce Banner ise Soluk Mühür'ün frekansını dengeleyecek son hesapları bitiriyordu. Nick Fury köprüde sessizce nöbet tutuyordu.",
                "Uçuş güvertesinin en karanlık, rüzgârlı ucunda ise fırtına öncesi son bir sessizlik vardı. Natasha Romanoff soğuk demir korkuluğun kenarında, rüzgâr kızıl saçlarını savururken tek başına New York'un uzak ışıklarına bakıyordu. Birkaç adım ötede Wanda Maximoff, elleri ceketinin ceplerinde, bulutların arasında ince bir yara gibi parlayan o soluk mavi çatlağı izliyordu.",
                "İkisi de her zamanki gibi sana tek bir baskı bile yapmıyordu — ne bir beklenti, ne zoraki bir veda, ne de tek taraflı bir ısrar. Ama yarın gökyüzü yarıldığında kimin hayatta kalacağını kimse bilmiyordu. Ve bu son gecede kalbinin kimi ya da hangi sessizliği seçtiği, yarınki büyük finalin ruhunu belirleyecekti."
            ),
            decisionTitle = "Final Öncesi Son Gece — Kalbinin ve Yolunun Tercihi",
            choicePrompt = "Yarın şafakta Manhattan gökyüzünde Vaelor'la yüzleşeceksin. Bu son geceyi nasıl geçireceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch9_c4_natasha_vow",
                    label = "Natasha'nın yanına yürü — \"Yarın bu işi bitirince, o Hell's Kitchen köftecisine bu kez birlikte gidiyoruz.\"",
                    toneHint = "Natasha • Derin & Bilinçli Adım • Sessiz Söz",
                    nextNodeId = "ch9_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 5),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 4),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.LOYALTY, 4)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.HUMANITY_ANCHOR to 4,
                            StoryFlagAxis.SHADOW_ALLIANCE to 3
                        ),
                        setFlags = mapOf("ch9_final_eve" to "natasha"),
                        branchTag = "Natasha'ya Söz",
                        impactSummary = "Natasha yüzünde nadir, derin bir sıcaklıkla elini korkuluğun üstünde senin elinin yanına bıraktı: 'Anlaştık Aiden. Ölmek yok.'",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch9_c4_wanda_vow",
                    label = "Wanda'nın yanına geç — \"Yarın gökyüzü yarıldığında korkmayacağım; çünkü artık yanımda beni anlayan biri var.\"",
                    toneHint = "Wanda • Derin & Bilinçli Adım • Ruhsal Bağ",
                    nextNodeId = "ch9_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 5),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 4),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.LOYALTY, 4)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.HUMANITY_ANCHOR to 4,
                            StoryFlagAxis.PSYCHIC_RESONANCE to 4
                        ),
                        setFlags = mapOf("ch9_final_eve" to "wanda"),
                        branchTag = "Wanda'ya Söz",
                        impactSummary = "Wanda elini tutup hafifçe başını omzuna yasladı; mavi ve kızıl ışık gece karanlığında huzurla titreşti.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch9_c4_alone_vow",
                    label = "Kimseye söz verme — güvertenin en ucunda tek başına şafağı bekle, bütün ağırlığı yalnızca kendi omuzlarında tut.",
                    toneHint = "Yalnız Kurt • Mutlak Odak • Mesafeli",
                    nextNodeId = "ch9_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 1, BookCharacterId.WANDA to 1, BookCharacterId.STEVE to 2),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 5,
                            StoryFlagAxis.WHITE_EYE_CONTROL to 2
                        ),
                        setFlags = mapOf("ch9_final_eve" to "alone"),
                        branchTag = "Yalnız Nöbet",
                        impactSummary = "Ne Natasha ne Wanda seni zorladı; ikisi de sessizce mesafene saygı duydu. Şafağı tek başına karşıladın.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── BÖLÜM IX — FİNAL (SEZON FİNALİNE GEÇİŞ) ─────────────────────────────
        BookSceneNode(
            id = "ch9_s5_end",
            chapterNumber = 9,
            sceneOrder = 5,
            sceneTitle = "Yirmi Yıl Sonra Aynı Mavi Gökyüzü",
            locationTag = "MANHATTAN SEMA • ŞAFAK • 05:55",
            paragraphs = listOf(
                "Şafak söktüğünde güneş doğmadı. Onun yerine, tam yirmi yıl önce beş yaşındaki bir çocuğun soğuk yangın merdiveninden baktığı o kusursuz, dehşet verici, canlı **mavi ışık** Manhattan'ın bütün gökyüzünü boydan boya yardı. Aşağıdaki caddelerde trafik bir anda durdu; insanlar arabalarından inip gökyüzündeki o gerçek dışı yarığa bakakaldı.",
                "Soluk Mühür Tarikatı'nın kilometrelerce uzunluktaki gümüş-siyah ana gemisi bulutların arasından ağır, görkemli bir sessizlikle süzülerek Avengers Kulesi'nin tam karşısında durdu. Yüce Hakem Vaelor geminin açık ön güvertesinden şehre bakıyor, sağ elindeki Soluk Mühür'ün ışığı gökdelenlerin camlarına vuruyordu.",
                "Helicarrier'ın açık uçuş güvertesinden bir adım öne çıktın. Okyanus rüzgârı siyah ceketini savurdu; çarpıcı mavi gözlerin bu kez gökyüzündeki o dev yarıktan bile daha parlak, daha kararlı yandı. Arkanda Steve Rogers kalkanını kaldırdı, Thor çekicini şimşekle doldurdu, Wanda kızıl ışığını açtı, Natasha ise sakin bir nefesle yanındaki yerini aldı. Beş yaşındayken o ışığın altında çaresiz bir çocuktun. Şimdi yirmi beş yaşındaydın, kaçmayı bırakmıştın ve içindeki güç artık tamamen uyanmıştı. **Büyük Sezon Finali başlıyordu.**"
            ),
            isChapterEnd = true,
            chapterEndQuote = "Yirmi yıl önce gökyüzü beni aldı. Bugün gökyüzüne ben gidiyorum."
        )
    ).associateBy { it.id }
}
