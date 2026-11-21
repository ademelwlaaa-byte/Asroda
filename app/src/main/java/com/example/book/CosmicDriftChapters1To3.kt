package com.example.book

/**
 * Extended, deeply literary Chapters 1, 2, and 3 of "Kozmik Sürükleniş" (Aiden Blackwood — MCU).
 * Every scene features rich novel-length prose, distinct quoted dialogue, and cross-chapter
 * hidden flags (vision_depth, first_impression, stark_reply, early_interest, night_intel,
 * power_demo, trust_reply, natasha_tone, wanda_tone, outlook, sparring, alarm_branch, locket_memory).
 */
object CosmicDriftChapters1To3 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 1,
            romanNumeral = "I",
            title = "Kozmik Sürükleniş",
            titleEn = "Cosmic Drift",
            subtitle = "Mavi Işık, Çatıdaki Görü ve Avengers Kulesi",
            description = "Beş yaşında mavi ışıkla kayboluş, yirmi yıllık kaçış, New York çatısındaki görü ve Avengers ile ilk temas.",
            descriptionEn = "Disappearance at age 5 in blue light, 20 years on the run, the rooftop vision, and first contact with the Avengers.",
            estimatedMinutes = 16,
            accentHex = 0xFFA855F7L,
            startNodeId = "ch1_s1",
            sceneCount = 6,
            decisionCount = 5
        ),
        BookChapterMeta(
            number = 2,
            romanNumeral = "II",
            title = "Şartlar",
            titleEn = "The Terms",
            subtitle = "Cam Masada Pazarlık, Güç Sınavı ve İlk Kırılma",
            description = "Avengers Kulesi'nde Tony Stark ve Steve Rogers ile şartlar masası, güç gösterisi ve gölgelerle kurulan ilk bağlar.",
            descriptionEn = "Negotiating terms with Tony Stark and Steve Rogers at Avengers Tower, the power test, and fragile first bonds.",
            estimatedMinutes = 17,
            accentHex = 0xFF60A5FAL,
            startNodeId = "ch2_s1",
            sceneCount = 7,
            decisionCount = 6
        ),
        BookChapterMeta(
            number = 3,
            romanNumeral = "III",
            title = "Deneme Süresi",
            titleEn = "Probation",
            subtitle = "Otuz Gün, Antrenman Salonu ve S.H.I.E.L.D. Alarmı",
            description = "Otuz günlük deneme süresi, Steve Rogers ile birebir sınav, enerji odası ve Kule'de çalan acil alarmda kader anı.",
            descriptionEn = "30-day trial period, sparring with Steve Rogers, energy chamber testing, and the S.H.I.E.L.D. alarm turning point.",
            estimatedMinutes = 20,
            accentHex = 0xFF22D3EEL,
            startNodeId = "ch3_s1",
            sceneCount = 8,
            decisionCount = 7
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM I: KOZMİK SÜRÜKLENİŞ
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch1_s1",
            chapterNumber = 1,
            sceneOrder = 1,
            sceneTitle = "Mavi Gökyüzü ve Çatıdaki Görü",
            locationTag = "HELL'S KITCHEN ÇATISI • 02:14",
            paragraphs = listOf(
                "Gökyüzü yanlıştı.",
                "Beş yaşındaydın ve bunu anlayacak kelimelerin yoktu ama vücudun anlamıştı — ensendeki tüyler, midendeki o kurşun ağırlığı, annenin elinin aniden senin küçük avucunu bulup kemiklerini acıtacak kadar sıkışı. New York'un üzerine çöken ışık her zaman gündoğumu turuncusu ya da neon sarısı olurdu; o gece değildi. O gece maviydi. Ama gökyüzünün hiç görmediğin, neredeyse canlı bir mavisi; sanki biri evrenin derisini neşterle yarmış da altından sızan şeyi göstermeye korkmuş gibi soğuk, kusursuz bir mavi.",
                "Yangın merdiveninin demiri buz gibiydi avucunda. Annenin sesi titriyordu ama kelimeleri sakindi: \"Aiden, tatlım, bana bak. Sadece bana bak, sakın gökyüzüne bakma.\" Babanın sesi ondan da sakindi ve bu çok daha kötüydü — çünkü baban hiç öyle konuşmazdı. O hep güler, mutfakta kahve döktüğünde bile şaka yapardı. O sakinlik, bir yetişkinin çocuğunu korkutmamak için yüzüne geçirdiği ince bir maskeydi ve sen beş yaşında bile o maskenin ardındaki dehşeti görebiliyordun.",
                "Elini bıraktın. Neden bıraktığını hiçbir zaman tam hatırlamayacaksın. Belki rüzgârın içinden gelen o derin uğultu adını fısıldamıştı, belki gökyüzündeki mavi yarık doğrudan göğsündeki bir şeyi çağırmıştı. İki adım geri attın ve ışık gökyüzünden inmedi — sadece *oradaydı*. Sanki hep oradaymış gibi. Bir saniye sonra bina yoktu. Sokak yoktu. Altındaki yangın merdiveni bile yoktu, ama sen havada asılı kalmış gibi düşmüyordun.",
                "Kimse yoktu. Annen yoktu. Baban yoktu. Üç sokak öteden gelen siren sesi, komşunun köpeğinin havlaması, rüzgârın ıslığı — hepsi silinmişti. Sadece sen, boş bir gökyüzü ve ağzında o geceden beri hiç geçmeyen soğuk bakır tadı.",
                "O günden sonra insanlar sana hep camın arkasından baktı. Önce doktorlar — ellerinde titreşen cihazlar, gözlerinde bir çocuğa saklamayı unuttukları o ilkel korku. Sonra siyah takım elbiseli devlet görevlileri, ardından isimlerini bile öğrenmek istemediğin soğuk laboratuvar odaları. Dokuz yaşında ilk kez kazayla ışınlandın; bir sorgu odasından çıkmak istedin ve yarım saniye sonra üç blok ötede, yağmurun altında dizlerinin üstündeydin. Burnun kanıyordu. Seni bulan ajanlardan biri telsize fısıldamıştı: \"Küçük canavar hâlâ sokakta.\" O kelimeden nefret etmeyi o gece öğrendin.",
                "On birinde ilk kez kaçtın ve yakalandın. On üçünde tekrar kaçtın — o kez başardın ama peşindeki iki adamı korkuyla nereye ışınladığını hâlâ bilmiyorsun. On beşinde gözlerin ilk kez beyaza döndü ve karşındaki muhafız gözleri açık halde heykel gibi dondu. Kırmızı olan ise on yedinde geldi. O gece yanan depoyu ve kulaklarında yankılanan çığlığı hatırlamamak için hâlâ karanlıkta nefes egzersizleri yapıyorsun.",
                "Şimdi yirmi beş yaşındasın. Yirmi yıldır koşuyorsun — bazen bir şehirden diğerine, bazen bir kıtadan ötekine, bir kez de nasıl olduğunu hâlâ çözemediğin bir şekilde artık var olmayan bir zaman kıvrımının dışına. Son dört yıldır hiçbir otel odasında üç geceden fazla kalmadın. Yatağını her gece kapıya ittin, girdiğin her mekânda önce çıkışları saydın. Bunun bir hayat olmadığını biliyorsun. Ama bildiğin tek hayat bu.",
                "New York gecesi gürültülüydü ve sen burada olmaman gerektiğini biliyordun. S.H.I.E.L.D. şehrin üç farklı bölgesinde yüksek frekanslı tarama yapıyordu — farkındalığının kenarında cama vuran elektrik iğneleri gibi. Yine de buradaydın. Çünkü son üç gecedir aynı görüyü görüyordun: yüksek bir cam kule, yuvarlak bir masa, mavi hologramlar, dosyalardan yarım yamalak tanıdığın yüzler ve evrenin kulağına inatla tekrarladığı tek bir kelime.",
                "*Aday.*",
                "Bir teklif kılığına bürünmüş hiçbir şeye güvenmiyordun. Ama yorgundun — uykuyla geçmeyen, kemiklerinin içine yerleşmiş derin bir yorgunluk. Her içgüdün sana \"şimdi ışınlan ve kaybol\" diye bağırırken ıslak çatı betonunda diz çöktün, gözlerini kapattın ve mavi yankının zihninin sınırlarına vurmasına izin verdin."
            ),
            decisionTitle = "Görüye Yaklaşım",
            choicePrompt = "Görü zihnini içine çekerken zihinsel sınırını nasıl yöneteceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c1_distant",
                    label = "Yüzeyde kal — yalnızca konuşulanları dinle, zihnini yormadan gücünü koru.",
                    toneHint = "Temkinli • Kontrollü",
                    nextNodeId = "ch1_s2_distant",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 2,
                            StoryFlagAxis.WHITE_EYE_CONTROL to 1
                        ),
                        avengersTrustDelta = 1,
                        setFlags = mapOf("vision_depth" to "distant"),
                        impactSummary = "Zihnini yıpratmadan görüyü okudun; fiziksel direncin ve sakinliğin tam kaldı.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c1_deep",
                    label = "Görüye derinden dal — masadaki gizli dosyaları ve enerji kayıtlarını zorla çek.",
                    toneHint = "Riskli • Derin İstihbarat",
                    nextNodeId = "ch1_s2_deep",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2, BookCharacterId.TONY to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.CURIOSITY, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.MENTAL_FATIGUE to 1,
                            StoryFlagAxis.MYSTERY_AURA to 1
                        ),
                        fatigueDelta = 1,
                        mysteryDelta = 1,
                        setFlags = mapOf("vision_depth" to "deep"),
                        secretUnlocked = "Doğu Avrupa Gizli Sorgusu",
                        impactSummary = "Görünün derininden gizli sorgu bilgisini çektin; karşılığında zihinsel yorgunluk ve burun kanaması yaşadın.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c1_trace",
                    label = "Beyaz Göz'ü yarım aç ve bu görüyü sana ulaştıran enerji ipliğini tersine takip et.",
                    toneHint = "Kozmik • Karşılıklı Temas",
                    nextNodeId = "ch1_s2_trace",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 3, BookCharacterId.NATASHA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.CURIOSITY, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.PSYCHIC_RESONANCE to 2,
                            StoryFlagAxis.WHITE_EYE_CONTROL to 2,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        mysteryDelta = 2,
                        setFlags = mapOf("vision_depth" to "traced"),
                        secretUnlocked = "Kule'deki Kızıl Yankı",
                        impactSummary = "Görünün ters ucunda Wanda Maximoff'un zihinsel izini buldun; o da seni hissetti ama alarm vermedi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch1_s2_distant",
            chapterNumber = 1,
            sceneOrder = 2,
            sceneTitle = "Hologramdaki Yüzler",
            locationTag = "ZİHİNSEL YANKI • AVENGERS KULESİ",
            paragraphs = listOf(
                "Zihnini kalın, şeffaf bir cam duvarın arkasında tuttun. Görü keskin kenarlarla değil, yağmur damlalarının arkasından bakar gibi aktı ama özü apaçıktı: Manhattan'ın göbeğinde göğe uzanan o devasa cam kule. En üst katta yuvarlak masanın çevresine toplanmış altı siluet.",
                "Masanın ortasında senin bulanık bir güvenlik kamerası görüntün dönüyordu — üç ay önce Prag'da bir gece treninden sızmış yarım saniyelik bir kare. Siyah kapüşonun yüzünün yarısını örtmüştü ama gözlerindeki mavi-beyaz yansıma kamerayı neredeyse yakmıştı.",
                "\"Yirmi yıldır arkasında tek bir parmak izi bırakmadan yaşayan birini neden tam şimdi bulduk?\" diyordu sakin, derin bir ses. Steve Rogers. Kollarını göğsünde kavuşturmuş, holograma değil karşısındaki adama bakıyordu. \"S.H.I.E.L.D. onu bir silah sınıfına koymuş. Bizim ne yapmamızı bekliyorsun, Tony?\"",
                "\"Fury onu çelik bir odaya kilitlemeden önce masaya çağırmamızı bekliyorum, Kaptan,\" diye yanıtladı Tony Stark, parmağının ucuyla hologramı büyütürken. \"Çocuk kaçmıyor, sadece hayatta kalmaya çalışıyor. Enerji imzası son iki haftada üç kez Manhattan üzerinde parladı. Ya bize gelmek istiyor ya da arkasında kendinden büyük bir fırtına var.\"",
                "Kızıl saçlı kadın — Natasha Romanoff — masanın kenarından sakinçe konuştu: \"Eğer gerçekten bu şehirdeyse, onu biz bulmayacağız. O bizi izlemeyi bitirdiğinde kendisi karar verecek.\"",
                "Görü dağıldı. Nefesin düzenliydi, parmak uçların sabit. Gözlerini açtığında sisin ardında Avengers Kulesi'nin mavi ışıkları dev bir deniz feneri gibi yükseliyordu. Yirmi yıllık kaçışın ilk kavşağı önündeydi."
            ),
            decisionTitle = "Kule'ye Giriş Stratejisi",
            choicePrompt = "Avengers Kulesi'ne nasıl yaklaşacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c2_direct",
                    label = "Bekleme — doğrudan toplantı salonunun ortasına ışınlan ve kartları masaya koy.",
                    toneHint = "Dallanma • Dürtüsel Giriş",
                    nextNodeId = "ch1_s3_impulsive",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.STEVE to -2, BookCharacterId.CLINT to 1, BookCharacterId.THOR to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.CURIOSITY, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2),
                            Triple(BookCharacterId.THOR, CharacterFlagAxis.RESPECT, 2),
                            Triple(BookCharacterId.CLINT, CharacterFlagAxis.WARMTH, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to -1,
                            StoryFlagAxis.INDEPENDENCE to 2
                        ),
                        avengersTrustDelta = -1,
                        setFlags = mapOf("first_impression" to "impulsive"),
                        branchTag = "Doğrudan Işınlanma",
                        impactSummary = "Aniden salonun ortasında belirmen savunma reflekslerini tetikledi; Stark etkilendi ama Steve temkinlendi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_observe",
                    label = "Önce karşı çatıdan güvenlik taramasını çöz, kör noktayı yakalayıp sessizce terastan gir.",
                    toneHint = "Dallanma • Hesaplı Giriş",
                    nextNodeId = "ch1_s3_calculated",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 2,
                            StoryFlagAxis.AVENGERS_TRUST to 2
                        ),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("first_impression" to "calculated"),
                        branchTag = "Kör Noktadan Giriş",
                        impactSummary = "Kule'nin güvenlik döngüsünü çözüp sessizce girmen Natasha Romanoff'un profesyonel saygısını kazandı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_retreat",
                    label = "Geri çekil ve gitme — yirmi yıllık kuralını bozma, Montreal koordinatına sıçra.",
                    toneHint = "Dallanma • Zorunlu Yarık",
                    nextNodeId = "ch1_s3_retreat",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2, BookCharacterId.STEVE to -1, BookCharacterId.NATASHA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 2),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHIELD_SUSPICION to 2,
                            StoryFlagAxis.INDEPENDENCE to 2,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        mysteryDelta = 1,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("first_impression" to "forced_rift"),
                        branchTag = "Zorunlu Yarık Dalı",
                        secretUnlocked = "S.H.I.E.L.D. Uydu Kilitlenmesi",
                        impactSummary = "Kaçmayı seçtin ancak S.H.I.E.L.D. kuantum uydusu yarığını bozup seni Kule terasına savurdu.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch1_s2_deep",
            chapterNumber = 1,
            sceneOrder = 2,
            sceneTitle = "Derin Yankı ve Bakır Tadı",
            locationTag = "ZİHİNSEL YANKI • DERİN KATMAN",
            paragraphs = listOf(
                "Zihinsel duvarı indirdin ve görünün en dibine daldın. Bulanıklık bir anda kristal keskinliğine dönüştü; artık sadece sesleri değil, cam masadaki kahve fincanlarının sıcaklığını, havalandırmanın uğultusunu ve odadaki altı kalp atışının ritmini bile duyabiliyordun.",
                "\"Frekans sıradan bir kuantum sıçraması değil,\" diyordu Bruce Banner'ın gergin sesi, holografik dalga grafiğini işaret ederek. \"Yirmi yıl önce Manhattan'da atmosfer sensörlerinin kaydettiği açıklanamayan mavi anomalinin birebir aynısı. O ışık sönmedi, Tony. Bir çocuğun sinir sistemine yerleşti.\"",
                "\"Ve mesele sadece S.H.I.E.L.D. değil,\" diye ekledi Natasha Romanoff, parmaklarını masadaki koyu mavi dosyanın kenarına vurarak. \"Üç hafta önce şifreli bir uydu kanalından bu çocuğun biyometrik profili sorgulanmış. Kaynak S.H.I.E.L.D. değil. HYDRA kayıtlarında bile yok. Doğu Avrupa'dan, muhtemelen İstanbul ya da Prag hattından gelen çok eski bir protokol.\"",
                "Tony başını yavaşça kaldırdı. \"Yani biri onu yirmi yıl sonra yeniden hatırladı. Bizden önce bulurlarsa, onu masaya davet etmekle yetinmeyecekler.\"",
                "Görü aniden ince bir cam gibi çatladı. Burnundan ılık bir damla süzülüp dudağının kenarına indi; elinin tersiyle sildin. Şakakların zonkluyordu ama paha biçilmez bir bilgi almıştın: peşindeki tek gölge S.H.I.E.L.D. değildi. Yağmurun ötesinde Avengers Kulesi'nin mavi ışıkları göğe yükseliyordu."
            ),
            decisionTitle = "Kule'ye Giriş Stratejisi",
            choicePrompt = "Duyduklarından sonra Avengers Kulesi'ne nasıl yaklaşacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c2_direct",
                    label = "Bekleme — doğrudan toplantı salonunun ortasına ışınlan ve kartları masaya koy.",
                    toneHint = "Dallanma • Dürtüsel Giriş",
                    nextNodeId = "ch1_s3_impulsive",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.STEVE to -2, BookCharacterId.CLINT to 1, BookCharacterId.THOR to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.CURIOSITY, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2),
                            Triple(BookCharacterId.THOR, CharacterFlagAxis.RESPECT, 2),
                            Triple(BookCharacterId.CLINT, CharacterFlagAxis.WARMTH, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to -1,
                            StoryFlagAxis.INDEPENDENCE to 2
                        ),
                        avengersTrustDelta = -1,
                        setFlags = mapOf("first_impression" to "impulsive"),
                        branchTag = "Doğrudan Işınlanma",
                        impactSummary = "Aniden salonun ortasında belirmen savunma reflekslerini tetikledi; Stark etkilendi ama Steve temkinlendi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_observe",
                    label = "Önce karşı çatıdan güvenlik taramasını çöz, kör noktayı yakalayıp sessizce terastan gir.",
                    toneHint = "Dallanma • Hesaplı Giriş",
                    nextNodeId = "ch1_s3_calculated",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 2,
                            StoryFlagAxis.AVENGERS_TRUST to 2
                        ),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("first_impression" to "calculated"),
                        branchTag = "Kör Noktadan Giriş",
                        impactSummary = "Kule'nin güvenlik döngüsünü çözüp sessizce girmen Natasha Romanoff'un profesyonel saygısını kazandı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_retreat",
                    label = "Geri çekil ve gitme — yirmi yıllık kuralını bozma, Montreal koordinatına sıçra.",
                    toneHint = "Dallanma • Zorunlu Yarık",
                    nextNodeId = "ch1_s3_retreat",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2, BookCharacterId.STEVE to -1, BookCharacterId.NATASHA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 2),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHIELD_SUSPICION to 2,
                            StoryFlagAxis.INDEPENDENCE to 2,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        mysteryDelta = 1,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("first_impression" to "forced_rift"),
                        branchTag = "Zorunlu Yarık Dalı",
                        secretUnlocked = "S.H.I.E.L.D. Uydu Kilitlenmesi",
                        impactSummary = "Kaçmayı seçtin ancak S.H.I.E.L.D. kuantum uydusu yarığını bozup seni Kule terasına savurdu.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch1_s2_trace",
            chapterNumber = 1,
            sceneOrder = 2,
            sceneTitle = "Kızıl İplik",
            locationTag = "ZİHİNSEL YANKI • KARŞILIKLI TEMAS",
            paragraphs = listOf(
                "Göz bebeklerinin çevresinde ince, buz beyazı bir halka yandı. Görüye pasif kalmak yerine, seni çağıran o kozmik akımı tersine izledin — karanlık bir okyanusta gerili ince bir ışık telini parmaklarına dolar gibi.",
                "Telin ucunda ne bir uydu vardı ne de bir bilgisayar. Bir insan zihni vardı. Cam pencerenin önünde duran, parmak uçlarında belli belirsiz kızıl kıvılcımlar dans eden genç kadın bir anda başını çevirdi. Görünün içinden dosdoğru *sana* baktı. Odadaki diğer beş kişi konuşmaya devam ediyordu ama o artık odada değildi; seninle aynı sessiz boşluktaydı.",
                "\"Buradasın,\" diye yankılandı sesi, dudakları kıpırdamadan ama cam kadar berrak. \"Bizi izliyorsun. Hayır, geri çekilme — kimseye söylemeyeceğim. Zihnindeki duvarların nasıl örüldüğünü tanıyorum. Ben de uzun süre öyle duvarların ardında yaşadım.\"",
                "Kızıl ışık bir saniyeliğine mavi kozmik dalgana değdi; beklenmedik biçimde yakıcı değil, sıcaktı. \"Bu bir tuzak değil, Aiden. En azından benim tarafımdan değil. İstersen gel.\"",
                "Bağlantı koptuğunda çatıdaki yağmur damlalarının soğuğunu yeniden yüzünde hissettin. Şakaklarında hafif bir sızı vardı ama göğsünde yirmi yıldır ilk kez korkudan başka bir duygu kıpırdandı: sessiz bir merak."
            ),
            decisionTitle = "Kule'ye Giriş Stratejisi",
            choicePrompt = "Wanda'nın sessiz davetinden sonra Avengers Kulesi'ne nasıl yaklaşacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c2_direct",
                    label = "Bekleme — doğrudan toplantı salonunun ortasına ışınlan ve kartları masaya koy.",
                    toneHint = "Dallanma • Dürtüsel Giriş",
                    nextNodeId = "ch1_s3_impulsive",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.STEVE to -2, BookCharacterId.CLINT to 1, BookCharacterId.THOR to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.CURIOSITY, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2),
                            Triple(BookCharacterId.THOR, CharacterFlagAxis.RESPECT, 2),
                            Triple(BookCharacterId.CLINT, CharacterFlagAxis.WARMTH, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to -1,
                            StoryFlagAxis.INDEPENDENCE to 2
                        ),
                        avengersTrustDelta = -1,
                        setFlags = mapOf("first_impression" to "impulsive"),
                        branchTag = "Doğrudan Işınlanma",
                        impactSummary = "Aniden salonun ortasında belirmen savunma reflekslerini tetikledi; Stark etkilendi ama Steve temkinlendi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_observe",
                    label = "Önce karşı çatıdan güvenlik taramasını çöz, kör noktayı yakalayıp sessizce terastan gir.",
                    toneHint = "Dallanma • Hesaplı Giriş",
                    nextNodeId = "ch1_s3_calculated",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 2,
                            StoryFlagAxis.AVENGERS_TRUST to 2
                        ),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("first_impression" to "calculated"),
                        branchTag = "Kör Noktadan Giriş",
                        impactSummary = "Kule'nin güvenlik döngüsünü çözüp sessizce girmen Natasha Romanoff'un profesyonel saygısını kazandı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_retreat",
                    label = "Geri çekil ve gitme — yirmi yıllık kuralını bozma, Montreal koordinatına sıçra.",
                    toneHint = "Dallanma • Zorunlu Yarık",
                    nextNodeId = "ch1_s3_retreat",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2, BookCharacterId.STEVE to -1, BookCharacterId.NATASHA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 2),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHIELD_SUSPICION to 2,
                            StoryFlagAxis.INDEPENDENCE to 2,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        mysteryDelta = 1,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("first_impression" to "forced_rift"),
                        branchTag = "Zorunlu Yarık Dalı",
                        secretUnlocked = "S.H.I.E.L.D. Uydu Kilitlenmesi",
                        impactSummary = "Kaçmayı seçtin ancak S.H.I.E.L.D. kuantum uydusu yarığını bozup seni Kule terasına savurdu.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── CH1 SCENE 3 (3 GENUINE BRANCHES) ──
        BookSceneNode(
            id = "ch1_s3_impulsive",
            chapterNumber = 1,
            sceneOrder = 3,
            sceneTitle = "Mavi Kıvılcımla Giriş",
            locationTag = "AVENGERS KULESİ • 88. KAT TOPLANTI SALONU",
            paragraphs = listOf(
                "Işınlanmak artık nefes almak kadar içgüdüseldi. Bir an Hell's Kitchen'ın yağmur kokan çatısındaydın; göz kırpması kadar kısa, sessiz bir mavi parlamayla doğrudan 88. katın ortasında belirdin ve çizmelerin cilalı cam zemine değdi. Kule'nin güvenlik şeritleri yarım saniye gecikmeyle kırmızıya döndü.",
                "Oda tek nefeste kilitlendi. Steve Rogers'ın eli refleksle masanın yanındaki kalkanına uzandı; Natasha Romanoff sandalyesinden akıcı bir kedi sessizliğiyle kalkıp ağırlığını öne aldı; Clint Barton koltuğunun yanındaki katlanabilir yayını tek harekette eline aldı. Pencere kenarındaki Wanda'nın parmak uçlarında kızıl bir hale titreşti; odanın öbür ucundaki Bruce Banner ise gözlüğünü düzeltip geriye yarım adım attı ama kimse saldırmadı.",
                "\"FRIDAY, güvenlik duvarımıza üç yüz milyon dolar ödedik, hatırlatayım,\" dedi Tony Stark, saatini metalik bir zırh eldivenine dönüştürerek. Mavi hologram ışığı yüzüne vurduğunda kaşlarından biri yarım milim kalktı; beklediği yorgun bir kaçak yerine keskin yüz hatları, kusursuz duruşu ve buz mavisi gözleriyle adeta odanın tüm havasını emen genç bir adam karşısında duruyordu. \"Dramatik girişlerden anlıyorsun, Bay Hayalet. Ayrıca yirmi yıldır kaçakta olan birine göre fazlasıyla dergi kapağından fırlamış gibisin. Şimdi kimse kimseyi buharlaştırmadan önce bir isimle başlayalım mı?\"",
                "Altı çift göz üzerindeydi — ve birkaçında tehditten çok istemsiz bir şaşkınlık vardı. Çıkışları sessizce saydın: iki zırhlı asansör, kuzeyde acil merdiven, güneyde kırılabilir cam teras. Ve elbette, tek bir düşünce uzağında sonsuz boşluk."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    minFatigue = 1,
                    callbackLabel = "Fiziksel Durum · Zihinsel Yorgunluk",
                    paragraphs = listOf("Görünün bıraktığı baskı şakaklarında hâlâ hafifçe atıyordu; sağ elinin parmakları milimetrik bir titreme geçirdi. Natasha Romanoff bunu yarım saniye içinde fark etti ama kimseye işaret vermedi.")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "vision_depth",
                    requiredFlagValue = "traced",
                    callbackLabel = "Geçmiş Kararın Yankısı · Kızıl İplik",
                    paragraphs = listOf("Wanda'nın gözleri seninle buluştuğunda parmaklarındaki kızıl kıvılcımlar tamamen söndü; dudaklarında yalnızca senin anlayacağın küçücük bir 'geldin' ifadesi belirdi.")
                )
            ),
            decisionTitle = "Tony Stark'a İlk Yanıt",
            choicePrompt = "Stark'ın sorusuna ve odadaki yüksek gerilime nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c3_cold",
                    label = "\"Aiden Blackwood. Dosyam zaten önünüzde açık — oyalanmadan ne istediğinizi söyleyin.\"",
                    toneHint = "Soğuk • Mesafeli",
                    nextNodeId = "ch1_s4_confrontation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 1, BookCharacterId.NATASHA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 1),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 1)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.INDEPENDENCE to 2),
                        setFlags = mapOf("ch1_stark_reply" to "cold"),
                        impactSummary = "Zırhını indirmeden doğrudan konuya girdin; mesafeli ve net bir çizgi çizdin.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c3_witty",
                    label = "\"Kapı zilinizi çalsaydım resepsiyon beni sekizinci katta S.H.I.E.L.D.'e teslim ederdi, Stark. Kestirme yol daha hesaplı.\"",
                    toneHint = "Alaycı • Kuru Mizah",
                    nextNodeId = "ch1_s4_confrontation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.CLINT to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.CLINT, CharacterFlagAxis.WARMTH, 2)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.AVENGERS_TRUST to 1),
                        avengersTrustDelta = 1,
                        setFlags = mapOf("ch1_stark_reply" to "witty"),
                        impactSummary = "Kuru mizahın Tony Stark'ın sempatisini ve Clint Barton'ın gülümsemesini kazandı; gerilim belirgin biçimde gevşedi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c3_silent",
                    label = "Hiç isim verme. Sadece gözlerini Stark'ın parlayan zırh eldivenine dik ve buz gibi sessiz kal.",
                    toneHint = "Gizemli • Sessiz Baskı",
                    nextNodeId = "ch1_s4_confrontation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -2, BookCharacterId.NATASHA to 2, BookCharacterId.WANDA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.CURIOSITY, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.MYSTERY_AURA to 2,
                            StoryFlagAxis.AVENGERS_TRUST to -1
                        ),
                        mysteryDelta = 2,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("ch1_stark_reply" to "silent"),
                        impactSummary = "Tekinsiz sessizliğin gizemini derinleştirdi; Steve'in kuşkusu yükselirken Natasha seni daha dikkatli tarttı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch1_s3_calculated",
            chapterNumber = 1,
            sceneOrder = 3,
            sceneTitle = "Kör Noktadan Adım",
            locationTag = "AVENGERS KULESİ • 88. KAT TERAS GİRİŞİ",
            paragraphs = listOf(
                "Acele etmedin. Karşı gökdelenin yağmurlu pervazında dört dakika boyunca Kule'nin kızılötesi tarama döngüsünü izledin. Her kırk iki saniyede bir kuzey terasındaki kamera bir buçuk saniyeliğine kör açıya dönüyordu. Yirmi yıldır hayatta kalmanın kuralı gösteriş değil, zamanlamaydı.",
                "Hava bir anlığına ince mavi kıvılcımlarla titredi ve tek bir ses bile çıkarmadan doğrudan teras korkuluğunun hemen dışında belirdin. Sürgülü cam kapıyı usulca aralayıp içeri adım attığında ne kırmızı ışık yandı ne de tek bir siren öttü. İlk fark eden Natasha Romanoff oldu; başını çevirdiğinde silahına uzanmadı, yalnızca dudaklarının ucunda profesyonel bir takdir kıvrımı belirdi.",
                "\"FRIDAY?\" dedi Tony Stark şaşkınlıkla arkasına dönerek. \"Kuzey terasındaki o bir buçuk saniyelik ölü bölgeyi ne zaman kapatacaktık?\" Ardından sana döndü, gözlerini kısarak tepeden tırnağa süzdü; yağmur damlalarının altından bile parlayan o keskin buz mavisi bakışların ve zahmetsiz karizmanın odadaki gerilimi garip biçimde kırdığını fark etmişti. \"Etkileyici. Gerçekten. S.H.I.E.L.D. dosyası tehlikeli olduğunu yazmıştı ama İtalyan moda çekiminden çıkıp terasıma yürüyeceğini eklemeyi unutmuşlar. Sana ne diye hitap etmeliyiz?\"",
                "Steve Rogers ellerini masanın üzerine bıraktı — tehditkâr değil ama tetikte. \"Alarm çalmadan gelebilecekken saklanmak yerine kendini gösterdin. Bu zaten bir şey anlatıyor.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    minFatigue = 1,
                    callbackLabel = "Fiziksel Durum · Zihinsel Yorgunluk",
                    paragraphs = listOf("Gözlerinin altında hafif bir yorgunluk gölgesi vardı; yine de soğukkanlı duruşun bunu neredeyse kusursuz gizliyordu.")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "vision_depth",
                    requiredFlagValue = "traced",
                    callbackLabel = "Geçmiş Kararın Yankısı · Kızıl İplik",
                    paragraphs = listOf("Wanda camın kenarından hafifçe başını eğdi: \"Beni duydun,\" dedi alçak sesle, yalnızca senin duyabileceğin tonda.")
                )
            ),
            decisionTitle = "Tony Stark'a İlk Yanıt",
            choicePrompt = "Stark'ın sorusuna ve odadaki dikkate nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c3_cold",
                    label = "\"Aiden Blackwood. Dosyam zaten önünüzde açık — oyalanmadan ne istediğinizi söyleyin.\"",
                    toneHint = "Soğuk • Mesafeli",
                    nextNodeId = "ch1_s4_confrontation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 1, BookCharacterId.NATASHA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 1),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 1)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.INDEPENDENCE to 2),
                        setFlags = mapOf("ch1_stark_reply" to "cold"),
                        impactSummary = "Zırhını indirmeden doğrudan konuya girdin; mesafeli ve net bir çizgi çizdin.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c3_witty",
                    label = "\"Kuzey kameranız kırk iki saniyede bir uyuyor, Stark. Güvenlik bütçenizden biraz tasarruf etseniz yeridir.\"",
                    toneHint = "Alaycı • Kuru Mizah",
                    nextNodeId = "ch1_s4_confrontation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.CLINT to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.CLINT, CharacterFlagAxis.WARMTH, 2)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.AVENGERS_TRUST to 1),
                        avengersTrustDelta = 1,
                        setFlags = mapOf("ch1_stark_reply" to "witty"),
                        impactSummary = "Kuru mizahın Tony Stark'ın sempatisini ve Clint Barton'ın kahkahasını kazandı; gerilim çözüldü.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c3_silent",
                    label = "Hiç isim verme. Sadece masadaki kendi bulanık hologramına bakıp sessiz kal.",
                    toneHint = "Gizemli • Sessiz Baskı",
                    nextNodeId = "ch1_s4_confrontation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -2, BookCharacterId.NATASHA to 2, BookCharacterId.WANDA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.CURIOSITY, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.MYSTERY_AURA to 2,
                            StoryFlagAxis.AVENGERS_TRUST to -1
                        ),
                        mysteryDelta = 2,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("ch1_stark_reply" to "silent"),
                        impactSummary = "Tekinsiz sessizliğin gizemini derinleştirdi; Steve'in kuşkusu yükselirken Natasha seni daha yakından inceledi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch1_s3_retreat",
            chapterNumber = 1,
            sceneOrder = 3,
            sceneTitle = "Zorunlu Yarık",
            locationTag = "AVENGERS KULESİ • 88. KAT TERAS",
            paragraphs = listOf(
                "Hayır. Yirmi yıl boyunca tek bir kural seni hayatta tutmuştu: seni çağıran masaya asla oturma. Arkanı döndün, Montreal'deki eski bir demiryolu deposunun koordinatını zihninde sabitledin ve uzamı büktün.",
                "Ama tam sıçrama milisaniyesinde gökyüzünden görünmez bir balyoz indi. S.H.I.E.L.D.'in yörüngedeki yeni kuantum bastırma uydusu bölgeyi tam o anda tarıyordu. Anlık ışınlanman mor elektrik arklarıyla savruldu; Montreal koordinatı elinden kaydı ve boşluk seni şehrin en parlak enerji çekirdeğine — Avengers Kulesi'nin reaktör hattına — doğru fırlattı.",
                "Terasın ıslak zeminine tek dizinin üstünde düştün; omzun cam çerçeveye sertçe çarptı. İçerideki ışıklar bir anda parlarken cam kapılar açıldı. Steve Rogers kalkanı hazır öne çıktı, arkasında Natasha ve zırh eldiveni parlayan Tony vardı. Pencere kenarındaki genç kadın — Wanda — parmaklarını hafifçe indirerek etrafında çakan mavi-mor statik kıvılcımları havada söndürdü.",
                "\"Uydu darbesini radarda yakaladık,\" dedi Tony Stark, eldivenini yarım indirerek. \"S.H.I.E.L.D. seni havada ikiye ayırmak üzereydi, evlat. Bize gelmek istemediğin belli ama şansına terasım sağlam. Şimdi nefes al ve bize kim olduğunu söyle.\""
            ),
            decisionTitle = "Tony Stark'a İlk Yanıt",
            choicePrompt = "Sarsılmış ama ayaktasın. Stark'a nasıl yanıt vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c3_cold",
                    label = "\"Aiden Blackwood. Beni kurtarmanızı istemedim — uyduyu susturun, kendi yoluma gideyim.\"",
                    toneHint = "Soğuk • Mesafeli",
                    nextNodeId = "ch1_s4_confrontation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 1, BookCharacterId.NATASHA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 1),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 1)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.INDEPENDENCE to 2),
                        setFlags = mapOf("ch1_stark_reply" to "cold"),
                        impactSummary = "Zor duruma düşmene rağmen bağımsız ve sert çizgini bozmadın.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c3_witty",
                    label = "\"Terasın manzarası güzelmiş Stark, ama iniş servisinizi kesinlikle gözden geçirmeniz lazım.\"",
                    toneHint = "Alaycı • Kuru Mizah",
                    nextNodeId = "ch1_s4_confrontation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.CLINT to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.CLINT, CharacterFlagAxis.WARMTH, 2)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.AVENGERS_TRUST to 1),
                        avengersTrustDelta = 1,
                        setFlags = mapOf("ch1_stark_reply" to "witty"),
                        impactSummary = "Sert inişe rağmen espri yapman Tony Stark ve Clint Barton'ın hemen ısınmasını sağladı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c3_silent",
                    label = "Tek kelime etmeden yavaşça ayağa kalk, avucundaki mavi kıvılcımı söndür ve sessizce izle.",
                    toneHint = "Gizemli • Sessiz Baskı",
                    nextNodeId = "ch1_s4_confrontation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -2, BookCharacterId.NATASHA to 2, BookCharacterId.WANDA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.CURIOSITY, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.MYSTERY_AURA to 2,
                            StoryFlagAxis.AVENGERS_TRUST to -1
                        ),
                        mysteryDelta = 2,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("ch1_stark_reply" to "silent"),
                        impactSummary = "Sessiz ve sert duruşun Steve'in kuşkularını artırdı ama odadaki herkesin dikkatini sana kilitledi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── CH1 SCENE 4 ──
        BookSceneNode(
            id = "ch1_s4_confrontation",
            chapterNumber = 1,
            sceneOrder = 4,
            sceneTitle = "Masanın Çevresi",
            locationTag = "AVENGERS KULESİ • 88. KAT",
            paragraphs = listOf(
                "Tony eldivenini tamamen geri çekti; salonun kırmızı güvenlik ışıkları yeniden yumuşak maviye döndü. Steve Rogers bir adım öne çıkıp masadaki hologramı kapattı. \"Seni kilitlemek için çağırmadık, Aiden,\" dedi sesi alçak ama çelik gibi dürüsttü. \"S.H.I.E.L.D. seni tek bir kelimeyle özetlemiş: tehdit. Bizse yirmi yıldır kimseden yardım almadan hayatta kalan birinin hikâyesinin diğer yarısını dinlemek istiyoruz.\"",
                "Clint Barton sandalyesinin arkasına yaslanıp yayını masaya bıraktı, rahat bir gülümsemeyle ekledi: \"Kısacası, dışarıda seni avlayan birkaç gizli servis varken burada tavan sağlam, kahve de bedava. En azından oturup konuşmaya değer.\"",
                "Ama senin dikkatin konuşanlardan çok, sessiz kalan iki kişideydi. Masanın sol tarafında duran kızıl saçlı kadın — Natasha Romanoff — tek bir kasını oynatmıyordu ama ağırlık merkezi her an harekete hazırdı. Yine de buz mavisi gözlerin doğrudan ona çevrildiğinde, dünyanın en soğukkanlı ajanının bakışlarında bile yarım saniyelik, tehlikeli bir hayranlık kıvılcımı gelip geçti; hemen ardından o tanıdık keskin ajan berraklığı yeniden yerine oturdu.",
                "Diğer yanda, yağmurun vurduğu boydan boya camın önünde duran Wanda Maximoff sana bakıyordu. O bakışta ne sorgu ne de silah vardı — keskin yüz hatlarının ardındaki soğuk duvarlara rağmen gözlerindeki o saf mavi derinliğin ne kadar sarsıcı ve tanıdık geldiğini hisseden, yumuşak ama derin bir kızıl sessizlik."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch1_stark_reply",
                    requiredFlagValue = "witty",
                    callbackLabel = "Geçmiş Kararın Yankısı · Stark'a Yanıt",
                    paragraphs = listOf("Tony hâlâ yarım bir gülümsemeyle başını sallıyordu: \"Bak Steve, en azından espri anlayışı sağlam. Bu kulenin ihtiyacı olan şey tam da buydu.\"")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch1_stark_reply",
                    requiredFlagValue = "silent",
                    callbackLabel = "Geçmiş Kararın Yankısı · Sessiz Duruş",
                    paragraphs = listOf("Steve'in kaşları hafifçe çatıldı. \"Sessizlik bir savunma biçimi, bunu anlıyorum. Ama burada kimseye zırh göstermek zorunda değilsin.\"")
                )
            ),
            decisionTitle = "Bakış Odak Noktası",
            choicePrompt = "Odayı süzerken bakışların kimin üzerinde gereğinden bir saniye fazla kalacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c4_natasha",
                    label = "Bakışların Natasha Romanoff'un üzerinde kalsın — gölgeleri tanıyan o keskin bakışa karşılık ver.",
                    toneHint = "Bağ • Natasha",
                    nextNodeId = "ch1_s5_offer",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.WARMTH, 2)
                        ),
                        setFlags = mapOf("early_interest" to "natasha"),
                        impactSummary = "Natasha Romanoff ile aranızda kelimesiz ama elektrik yüklü bir anlayış bağı kuruldu.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c4_wanda",
                    label = "Bakışların pencere kenarındaki Wanda Maximoff'a kilitlensin — zihnindeki sessiz yankıyı kabul et.",
                    toneHint = "Bağ • Wanda",
                    nextNodeId = "ch1_s5_offer",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 2)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.PSYCHIC_RESONANCE to 1),
                        setFlags = mapOf("early_interest" to "wanda"),
                        impactSummary = "Wanda Maximoff zihnindeki çağrıyı hissetti; aranızda derin bir empati kıvılcımı doğdu.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c4_neutral",
                    label = "Kimseye özel bir bakış verme; tüm odayı eşit mesafeyle, soğukkanlı biçimde tara.",
                    toneHint = "Nötr • Profesyonel",
                    nextNodeId = "ch1_s5_offer",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 2, BookCharacterId.TONY to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 2),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 2,
                            StoryFlagAxis.AVENGERS_TRUST to 1
                        ),
                        mysteryDelta = 1,
                        avengersTrustDelta = 1,
                        setFlags = mapOf("early_interest" to "none"),
                        impactSummary = "Duygusal açık vermeden profesyonel mesafeni korudun; Steve disiplinini not etti.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        // ── CH1 SCENE 5 ──
        BookSceneNode(
            id = "ch1_s5_offer",
            chapterNumber = 1,
            sceneOrder = 5,
            sceneTitle = "Teklifin Ağırlığı",
            locationTag = "AVENGERS KULESİ • 88. KAT",
            paragraphs = listOf(
                "Steve masaya bir adım daha yaklaştı. \"Sana kelepçe takmıyoruz, Aiden. Bir hücre de yok. Bu gece burada bir oda, sıcak bir yemek ve yarın sabah masaya oturup şartları eşit olarak konuşma fırsatı öneriyoruz. İstersen yarın gün doğumunda kapıdan yürüyüp çıkabilirsin.\"",
                "\"Ya da duvarlardan geçip,\" diye mırıldandı Clint hafifçe gülerek. \"Hangisi daha pratikse.\"",
                "Mavi ışık beş yaşında hayatından her şeyi aldığından beri, hiç kimse sana kapının kilitsiz olduğunu söylememişti. Her zaman bir deney camı, bir kelepçe ya da bir sakinleştirici iğne vardı. Cebinin içinden eski gümüş kolyenin soğuk kenarına dokundun — o geceden kalan tek parça.",
                "Dışarıda New York yağmuru camlara vuruyordu. Kalırsan yirmi yıllık kaçış kuralını ilk kez çiğneyecektin. Gidersen, karanlıkta seni yeniden aramaya başlayan o bilinmeyen güçle tek başına karşılaşacaktın."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "early_interest",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Bağ Yankısı · Natasha",
                    paragraphs = listOf("Natasha masanın ucundan hafifçe başını eğdi. Sesi yalnızca senin duyacağın kadar alçaktı: \"Kaçmayı zaten ustalıkla biliyorsun, Blackwood. Asıl zor olan bir gece kalmak.\"")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "early_interest",
                    requiredFlagValue = "wanda",
                    callbackLabel = "Bağ Yankısı · Wanda",
                    paragraphs = listOf("Wanda pencereden bir adım uzaklaştı. Gözlerinde sakin bir dürüstlük vardı: \"Burada herkesin geçmişi ağır. Yalnız değilsin — kalmayı seçersen.\"")
                )
            ),
            decisionTitle = "İlk Gece Kararı",
            choicePrompt = "Bu gece Kule'de kalma teklifine nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c5_accept",
                    label = "\"Bir gece. Yarın şartları masada konuşuruz — kapı dışarıdan kilitlenirse camı kırmam gerekmez.\"",
                    toneHint = "Şartlı Kabul",
                    nextNodeId = "ch1_s6_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.CLINT to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.CLINT, CharacterFlagAxis.WARMTH, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 2,
                            StoryFlagAxis.HUMANITY_ANCHOR to 1
                        ),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch1_night_choice" to "conditional"),
                        impactSummary = "Sınırlarını net çizerek bir gece kalmayı kabul ettin; ekip sözüne saygı duydu.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c5_probe",
                    label = "\"Kalırım. Ama sabah masaya oturmadan önce beni Doğu Avrupa'dan sorgulayan o gizli kaydı göreceğim.\"",
                    toneHint = "Stratejik • İstihbarat Talebi",
                    nextNodeId = "ch1_s6_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 2, BookCharacterId.NATASHA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 1,
                            StoryFlagAxis.AVENGERS_TRUST to 1
                        ),
                        avengersTrustDelta = 1,
                        secretUnlocked = "Doğu Avrupa Arşiv İzi",
                        setFlags = mapOf("ch1_night_choice" to "demanded_intel"),
                        impactSummary = "Hemen karşı hamle yaptın; Stark ve Natasha gizli sorgu dosyasını sabah paylaşmayı kabul etti.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c5_guarded",
                    label = "\"Söz vermiyorum. Odayı gösterin — sabah hâlâ burada olursam konuşuruz.\"",
                    toneHint = "Bağımsız • Gizemli",
                    nextNodeId = "ch1_s6_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2, BookCharacterId.STEVE to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 2),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 2,
                            StoryFlagAxis.MYSTERY_AURA to 1
                        ),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch1_night_choice" to "guarded"),
                        impactSummary = "Açık kapı bırakarak odaya geçtin; ekip sabah geri dönüp dönmeyeceğini merakla bekledi.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── CH1 END NODE ──
        BookSceneNode(
            id = "ch1_s6_end",
            chapterNumber = 1,
            sceneOrder = 6,
            sceneTitle = "Kilitsiz Kapı",
            locationTag = "AVENGERS KULESİ • 74. KAT MİSAFİR ODASI",
            paragraphs = listOf(
                "Oda tahmin ettiğinden çok daha sadeydi: dingin koyu gri duvarlar, Manhattan'a bakan boydan boya cam, masada dumanı yeni sönen bir fincan koyu kahve. Kapının kilit mekanizmasını parmaklarınla yokladın — dışarıdan kilitlenecek tek bir sürgü bile yoktu. Yirmi yıldır ilk kez yatağı kapının önüne itmedin.",
                "Cebinden gümüş kolyeyi çıkardın, avucunda çevirdin. Aşağıda New York milyonlarca kehribar ışıkla nefes alıyordu. Yukarıdaki katta hâlâ senin adını konuşan insanlar vardı. Belki büyük bir tuzaktı. Belki de yirmi yıllık sürüklenişin ilk gerçek durağı.",
                "Gözlerini kapattığında bu kez mavi ışık kâbus değildi — sadece yaklaşan büyük fırtınanın uzak, derin uğultusuydu."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "first_impression",
                    requiredFlagValue = "forced_rift",
                    callbackLabel = "Dal Yankısı · Zorunlu Yarık",
                    paragraphs = listOf("Teras camının hemen dışında hâlâ uzam yarığından kalan keskin ozon kokusu asılıydı. S.H.I.E.L.D. uydusu seni bir kez yakalamıştı — bir dahaki sefere çok daha hızlı kilitlenecekti.")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "early_interest",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Bağ Yankısı · Natasha'nın Notu",
                    paragraphs = listOf("Kapının altından ince, koyu renkli bir kart kaydırılmıştı. Üzerinde tek bir satır, düzgün bir el yazısıyla: \"Sabah 07:30. Kahveyi sek içiyorsan 82. kat. — N.\"")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "early_interest",
                    requiredFlagValue = "wanda",
                    callbackLabel = "Bağ Yankısı · Kızıl Nöbet",
                    paragraphs = listOf("Gecenin ilerleyen saatinde camın yüzeyinde sıcak, belli belirsiz bir kızıl kıvılcım titreyip söndü — eski kâbusların kapıyı çalmasını önleyen sessiz bir nöbet gibi.")
                )
            ),
            isChapterEnd = true,
            chapterEndQuote = "Yirmi yıldır ilk kez bir kapının önüne yatak itmeden uyudun."
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM II: ŞARTLAR
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch2_s1",
            chapterNumber = 2,
            sceneOrder = 1,
            sceneTitle = "Sabah Işığı ve Cam Masa",
            locationTag = "AVENGERS KULESİ • 84. KAT BRİFİNG ODASI • 08:10",
            paragraphs = listOf(
                "Sabah güneşi Manhattan'ın cam gökdelenlerinden kırılıp brifing odasına dolduğunda sen masanın tam karşısında çoktan yerini almıştın. Gece yalnızca üç saat uyumuştun ama zihnin bıçak gibi keskindi; pencereden vuran soğuk sabah ışığı keskin çene hattını ve canlı buz mavisi gözlerini öylesine net aydınlatıyordu ki, koridordan geçen iki S.H.I.E.L.D. teknisyeninin camın ardından dönüp ikinci kez baktığını fark ettin.",
                "Tony Stark elinde tablet ve iki fincan espressoyla içeri girdi, birini masanın üzerinden sana doğru kaydırdı. Hemen arkasından Steve Rogers, Natasha Romanoff ve Wanda Maximoff geldiler; kapı aralığından başını uzatan Clint, Tony'nin kulağına duyulacak kadar alçak ama alaycı bir sesle \"Üç saat uykuyla bile bu kadar haksız derecede iyi görünmek yasak olmalı,\" diye mırıldandı. Natasha dudaklarının ucunda beliren ince tebessümü saklamadı. Stark ise tableti cam masaya bıraktığında havada üç boyutlu, titreşen mor-mavi bir enerji spektrumu yükseldi.",
                "\"Günaydın, Bay Sürüklenme,\" dedi Tony, spektrumdaki iki sivri tepe noktasını parmağıyla işaret ederek. \"Dün geceki geçişinden kalan uzay-zaman izini inceledik. Bu klasik bir ışınlanma değil — uzayı büküyorsun, üstüne bir de biyoelektrik alanın iki ayrı yüksek frekans taşıyor. Biri buz beyazı, diğeri koyu kırmızı dalga boyunda. Bizimle masaya oturacaksan, elindeki kartların en azından ölçeğini bilmemiz gerekiyor.\"",
                "Steve kollarını masaya dayadı, gözleri sakin ve doğrudan üzerindeydi. \"Kimse senden bir laboratuvar deneği olmanı istemiyor, Aiden. Ama yan yana duracaksak neyle karşı karşıya olduğumuzu bilmeliyiz. Bize ne kadarını göstermeye hazırsın?\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch1_night_choice",
                    requiredFlagValue = "demanded_intel",
                    callbackLabel = "Geçmiş Kararın Yankısı · İstihbarat Sözü",
                    paragraphs = listOf("Natasha masanın ucundan şifreli bir klasörü önüne itti. \"Söz verdiğimiz gibi,\" dedi. \"Doğu Avrupa'dan seni sorgulayan gizli kanalın şifre başlığı. Karşılıklı dürüstlükle başlayalım.\"")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "first_impression",
                    requiredFlagValue = "calculated",
                    callbackLabel = "Geçmiş Kararın Yankısı · Kör Nokta",
                    paragraphs = listOf("Tony hafifçe homurdandı: \"Bu arada kuzey terasındaki kamera açısını sabah altıda yamaladım. Bir dahaki sefere o kadar kolay giremeyeceksin.\"")
                )
            ),
            decisionTitle = "Güç Gösterimi Stratejisi",
            choicePrompt = "Stark ve Rogers'ın önünde güçlerinin ne kadarını göstereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c1_measured",
                    label = "Avucunda küçük, kontrollü bir mavi ışık küresi aç ve temel sınırlarını sakinçe anlat.",
                    toneHint = "Dengeli • Kontrollü",
                    nextNodeId = "ch2_s2_measured",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 6, BookCharacterId.TONY to 5),
                        avengersTrustDelta = 6,
                        setFlags = mapOf("ch2_power_demo" to "measured"),
                        impactSummary = "Kontrollü gösterin Stark ve Rogers'ın güçlerini gerçekçi biçimde değerlendirmesini sağladı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c1_refuse",
                    label = "Gösteriyi tamamen reddet: \"Sirk hayvanı değilim. Sözüme güvenmiyorsanız masa burada biter.\"",
                    toneHint = "Dallanma • Sert Sınır",
                    nextNodeId = "ch2_s2_refuse",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to -6, BookCharacterId.STEVE to -4, BookCharacterId.NATASHA to 7),
                        mysteryDelta = 2,
                        avengersTrustDelta = -5,
                        setFlags = mapOf("ch2_power_demo" to "refused"),
                        branchTag = "Güç Gösterisini Reddetme",
                        impactSummary = "Gizemini korudun; Stark ve Steve'in kuşkusu artsa da Natasha sınır koymanı takdir etti.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c1_generous",
                    label = "Cömert davran — uzamı büküp odadaki bardağı ışınla ve Beyaz/Kırmızı Göz katmanlarını açıkla.",
                    toneHint = "Dallanma • Açık Gösterim",
                    nextNodeId = "ch2_s2_generous",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 11, BookCharacterId.WANDA to 6, BookCharacterId.STEVE to 4),
                        fatigueDelta = 1,
                        avengersTrustDelta = 8,
                        setFlags = mapOf("ch2_power_demo" to "generous"),
                        secretUnlocked = "Çift Frekanslı Göz Teorisi",
                        branchTag = "Tam Spektrum Paylaşımı",
                        impactSummary = "Katmanlı formlarını açıklaman Tony Stark'ta büyük bir bilimsel merak ve güven uyandırdı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch2_s2_measured",
            chapterNumber = 2,
            sceneOrder = 2,
            sceneTitle = "Ölçülü Kıvılcım",
            locationTag = "AVENGERS KULESİ • 84. KAT",
            paragraphs = listOf(
                "Elini masanın ortasına uzattın. Havadaki moleküller soğuk bir çıtırtıyla büküldü; avucunun üç santim üzerinde berrak, saf mavi bir ışık küresi sessizce dönmeye başladı. Ne bir cam titredi ne de odadaki sensörler kırmızıya geçti.",
                "\"Işınlanma, uzam bükme ve iki eşik formu,\" dedin düz ama net bir sesle. \"Beyaz olan zihni ve kinetik hareketi durdurur. Kırmızı olan ise yıkıcıdır — mecbur kalmadıkça uyandırmam. Şimdilik bilmeniz gereken her şey bu.\"",
                "Tony holografik sensöre eğilip hafifçe ıslık çaldı. \"Sıfır enerji sızıntısı. Bu kadar sıkı bir disiplini tek başına mı öğrendin?\" Steve başını yavaşça salladı. \"Bir silahı taşırken parmağını tetikten uzak tutabilmek... işte güvenin ilk adımı budur, Aiden.\""
            ),
            decisionTitle = "Steve'in Güven Sözüne Tepki",
            choicePrompt = "Steve 'güven' kelimesini masaya bıraktığında nasıl yanıt vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c2_sarcastic",
                    label = "\"Güven mi? En son birine güvendiğimde dokuz yaşındaydım ve koluma takip çipi takıyorlardı.\"",
                    toneHint = "Alaycı • Savunmacı",
                    nextNodeId = "ch2_s3_natasha",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -2, BookCharacterId.NATASHA to 5, BookCharacterId.TONY to 3),
                        setFlags = mapOf("ch2_trust_reply" to "sarcastic"),
                        impactSummary = "Sınırını sertçe çizdin; Steve geri adım atmadı ama geçmişindeki yarayı duydu.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_sincere",
                    label = "\"Kolay değil, Rogers. Yirmi yıldır hayatta kalmamın tek sebebi kimseye güvenmemekti. Ama dinliyorum.\"",
                    toneHint = "Dürüst • Açık",
                    nextNodeId = "ch2_s3_natasha",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 11, BookCharacterId.CLINT to 5, BookCharacterId.BRUCE to 4),
                        avengersTrustDelta = 7,
                        setFlags = mapOf("ch2_trust_reply" to "sincere"),
                        impactSummary = "Dürüstlüğün Steve Rogers'ın derin saygısını ve desteğini kazandı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_deflect",
                    label = "Cevap verme; gözlerini masadaki şartlar dosyasına indirip konuyu maddelere çevir.",
                    toneHint = "Mesafeli • Sessiz",
                    nextNodeId = "ch2_s3_natasha",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -4),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch2_trust_reply" to "silent"),
                        impactSummary = "Konuyu geçiştirmen Steve'in temkinli mesafesini korumasına neden oldu.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch2_s2_refuse",
            chapterNumber = 2,
            sceneOrder = 2,
            sceneTitle = "Cam Masadaki Gerilim",
            locationTag = "AVENGERS KULESİ • 84. KAT",
            paragraphs = listOf(
                "Ellerini ceketinin ceplerinden bile çıkarmadın. \"On beş yıl boyunca beyaz önlüklü adamlar camın arkasından bana 'bir kez daha yap' dedi, Stark. Test odaları bitti. Ya eşit şartları konuşuruz, ya da ben bu camdan yürüyüp çıkarım.\"",
                "Tony'nin çenesi kasıldı; hologramı tek hareketle kapattı. \"Harika. Kör uçuş yapmayı zaten hep sevmişimdir.\" Odadaki hava bir anda buz kesti. Ama Natasha sandalyesinde hafifçe öne eğildi, sesinde keskin bir sakinlik vardı: \"Haklı, Tony. Kimse dün gece tanıştığı bir odaya tüm zayıflıklarını sermez.\"",
                "Steve derin bir nefes aldı ve bakışlarını sana sabitledi. \"Pekâlâ, gösteri yok. Ama bir anlaşma tek taraflı yürümez, Aiden. Bir noktada güven kelimesine yer açmamız gerekecek.\""
            ),
            decisionTitle = "Steve'in Güven Sözüne Tepki",
            choicePrompt = "Steve 'güven' kelimesini masaya bıraktığında nasıl yanıt vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c2_sarcastic",
                    label = "\"Güven mi? En son birine güvendiğimde dokuz yaşındaydım ve koluma takip çipi takıyorlardı.\"",
                    toneHint = "Alaycı • Savunmacı",
                    nextNodeId = "ch2_s3_natasha",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -2, BookCharacterId.NATASHA to 5, BookCharacterId.TONY to 3),
                        setFlags = mapOf("ch2_trust_reply" to "sarcastic"),
                        impactSummary = "Sınırını sertçe çizdin; Steve geri adım atmadı ama geçmişindeki yarayı duydu.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_sincere",
                    label = "\"Kolay değil, Rogers. Yirmi yıldır hayatta kalmamın tek sebebi kimseye güvenmemekti. Ama dinliyorum.\"",
                    toneHint = "Dürüst • Açık",
                    nextNodeId = "ch2_s3_natasha",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 11, BookCharacterId.CLINT to 5, BookCharacterId.BRUCE to 4),
                        avengersTrustDelta = 7,
                        setFlags = mapOf("ch2_trust_reply" to "sincere"),
                        impactSummary = "Dürüstlüğün Steve Rogers'ın derin saygısını ve desteğini kazandı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_deflect",
                    label = "Cevap verme; gözlerini masadaki şartlar dosyasına indirip konuyu maddelere çevir.",
                    toneHint = "Mesafeli • Sessiz",
                    nextNodeId = "ch2_s3_natasha",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -4),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch2_trust_reply" to "silent"),
                        impactSummary = "Konuyu geçiştirmen Steve'in temkinli mesafesini korumasına neden oldu.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch2_s2_generous",
            chapterNumber = 2,
            sceneOrder = 2,
            sceneTitle = "İki Gözün Spektrumu",
            locationTag = "AVENGERS KULESİ • 84. KAT",
            paragraphs = listOf(
                "Parmaklarını hafifçe şıklattın. Tony'nin önündeki su bardağı tek bir ses çıkarmadan havada mavi bir kıvılcımla silindi ve yarım saniye sonra Steve'in elinin iki santim yanında belirdi. Ardından göz bebeklerinin etrafında anlık, keskin bir beyaz halka parlayıp söndü; odadaki hava bir saniyeliğine ağırlığını kaybetti.",
                "\"Üç katman,\" dedin sakin bir sesle. \"Uzam sıçraması, Beyaz Göz — algı ve kinetik durdurma — ve bir de Kırmızı Göz. Kırmızı olanı uyandırmanın bedeli ağırdır, bu yüzden onu kilitli tutuyorum.\"",
                "Tony yerinden fırlayıp holograma yaklaştı. \"FRIDAY, o spektrumu kaydettin mi? Bu klasik mutasyon değil... iki ayrı kozmik rezonans tek bir insan sinir sistemine kenetlenmiş!\" Wanda'nın gözleri hafifçe büyüdü; dalganın içindeki eski yalnızlığı odadaki herkesten farklı duymuştu.",
                "Steve elini bardağın yanına koydu, sakin bir saygıyla başını eğdi. \"Bunu paylaşmak cesaret ister, Aiden. Karşılığında sana aynı dürüstlüğü borçluyuz. Güven iki yönlü çalışır.\""
            ),
            decisionTitle = "Steve'in Güven Sözüne Tepki",
            choicePrompt = "Steve 'güven' kelimesini masaya bıraktığında nasıl yanıt vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c2_sarcastic",
                    label = "\"Güven mi? En son birine güvendiğimde dokuz yaşındaydım ve koluma takip çipi takıyorlardı.\"",
                    toneHint = "Alaycı • Savunmacı",
                    nextNodeId = "ch2_s3_natasha",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -2, BookCharacterId.NATASHA to 5, BookCharacterId.TONY to 3),
                        setFlags = mapOf("ch2_trust_reply" to "sarcastic"),
                        impactSummary = "Sınırını sertçe çizdin; Steve geri adım atmadı ama geçmişindeki yarayı duydu.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_sincere",
                    label = "\"Kolay değil, Rogers. Yirmi yıldır hayatta kalmamın tek sebebi kimseye güvenmemekti. Ama dinliyorum.\"",
                    toneHint = "Dürüst • Açık",
                    nextNodeId = "ch2_s3_natasha",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 11, BookCharacterId.CLINT to 5, BookCharacterId.BRUCE to 4),
                        avengersTrustDelta = 7,
                        setFlags = mapOf("ch2_trust_reply" to "sincere"),
                        impactSummary = "Dürüstlüğün Steve Rogers'ın derin saygısını ve desteğini kazandı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_deflect",
                    label = "Cevap verme; gözlerini masadaki şartlar dosyasına indirip konuyu maddelere çevir.",
                    toneHint = "Mesafeli • Sessiz",
                    nextNodeId = "ch2_s3_natasha",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -4),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch2_trust_reply" to "silent"),
                        impactSummary = "Konuyu geçiştirmen Steve'in temkinli mesafesini korumasına neden oldu.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── CH2 SCENE 3: NATASHA ──
        BookSceneNode(
            id = "ch2_s3_natasha",
            chapterNumber = 2,
            sceneOrder = 3,
            sceneTitle = "Gölgelerin Dili",
            locationTag = "AVENGERS KULESİ • 82. KAT DİNLENME ALANI",
            paragraphs = listOf(
                "Toplantıya kısa bir ara verildiğinde koridorun sonundaki cam balkona çıktın. Beş dakika sonra kahve makinesinin uğultusu kesildi ve Natasha Romanoff iki koyu fincanla yanında belirdi. Birini tek kelime etmeden uzattı.",
                "\"Kapıya sırtını dönmeden oturuyorsun,\" dedi fincanından küçük bir yudum alarak. \"Her üç dakikada bir camdaki yansımadan koridoru tarıyorsun. Yirmi yılın alışkanlığı kolay silinmez.\"",
                "Yüzünde ne yargı ne de acıma vardı; yalnızca bir aynaya bakan birinin tanıdık sükûneti. \"Stark seni çözülecek bir denklem olarak görüyor, Steve ise kurtarılacak bir asker. Ben ne gördüğümü söyleyeyim mi, Blackwood? Gölgede yaşamayı kusursuz öğrenmiş ama gün ışığının hâlâ canını yakıp yakmayacağını merak eden birini.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "early_interest",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Bağ Yankısı · İlk Bakış",
                    paragraphs = listOf("Dün geceden beri aranızda asılı duran o sessiz elektrik şimdi daha yakındı. Fincanı uzatırken parmak uçlarınız bir saniyeliğine değdi; ikiniz de geri çekilmediniz.")
                )
            ),
            decisionTitle = "Natasha İle İlişki Tavrı",
            choicePrompt = "Natasha'nın seni bu kadar net okumasına nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c3_silent_gaze",
                    label = "Sessiz kal, kahveyi al ve bakışlarını onda uzat — kelimelere gerek bırakma.",
                    toneHint = "Sessiz Anlayış",
                    nextNodeId = "ch2_s4_wanda",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 8),
                        setFlags = mapOf("ch2_natasha_tone" to "silent_bond"),
                        impactSummary = "Gölgelerden gelen iki insan arasındaki sessiz anlayış derinleşti.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c3_verbal",
                    label = "\"Bunu söylemek için aynı gölgelerden geçmiş olmak gerekir, Romanoff. Senin de çıkışları saydığını gördüm.\"",
                    toneHint = "Doğrudan • Eşit",
                    nextNodeId = "ch2_s4_wanda",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 12),
                        setFlags = mapOf("ch2_natasha_tone" to "direct_spark"),
                        impactSummary = "Onun da zırhını gördüğünü söylemen Natasha ile güçlü ve eşit bir bağ kurdu.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c3_distance",
                    label = "Fincanı tezgâha bırak, kısa bir baş selamıyla mesafe koyup koridora dön.",
                    toneHint = "Mesafeli",
                    nextNodeId = "ch2_s4_wanda",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to -4),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch2_natasha_tone" to "distant"),
                        impactSummary = "Natasha ile arandaki mesafeyi korudun; gizemli ama kapalı kaldın.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── CH2 SCENE 4: WANDA ──
        BookSceneNode(
            id = "ch2_s4_wanda",
            chapterNumber = 2,
            sceneOrder = 4,
            sceneTitle = "Kızıl İçgörü",
            locationTag = "AVENGERS KULESİ • 79. KAT GÖZLEM TERASI",
            paragraphs = listOf(
                "Öğleden sonra şartların taslağı önündeydi: kimlik gizliliği, S.H.I.E.L.D.'den dokunulmazlık, karşılığında Kule içinde kalıp güçlerinin sınırlarını ekip eşliğinde dengelemek. Şartlar adildi — yine de göğsünde görünmez bir daralma vardı.",
                "Gözlem terasının rüzgârlı ucunda Wanda Maximoff'u buldun. Ellerini metal korkuluğa yaslamış, Hudson'ın üzerinde biriken kurşuni bulutları izliyordu. Sen yaklaşırken dönmeden konuştu: \"Zihnini okumuyorum, Aiden. Ama kapıların ne kadar kalın olduğunu hissedebiliyorum.\"",
                "Sana döndüğünde gözlerinde yumuşak, neredeyse hüzünlü bir berraklık vardı. \"Bazen kaçtığımız şey peşimizdeki adamlar değil. Kayıp hissetmek. Bir yere ait olmayı unutmuş olmak. Sen de uzun süredir böyle hissediyorsun, değil mi?\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "vision_depth",
                    requiredFlagValue = "traced",
                    callbackLabel = "Geçmiş Kararın Yankısı · Kızıl İplik",
                    paragraphs = listOf("Wanda'nın sesi biraz daha yumuşadı: \"Dün gece görünün ucunda beni bulduğunda korkmadım. Sadece... benim kadar uzun süredir sessiz kalmış birini ilk kez duydum.\"")
                )
            ),
            decisionTitle = "Wanda'nın İçgörüsüne Tepki",
            choicePrompt = "Wanda'nın bu kadar yakına dokunan sözlerine nasıl yanıt vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c4_suspect",
                    label = "\"Zihnime girmediğinden emin misin, Maximoff? Bu kadar isabetli tahminler beni rahatsız eder.\"",
                    toneHint = "Temkinli • Sorgulayıcı",
                    nextNodeId = "ch2_s5_stark_lab",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2),
                        setFlags = mapOf("ch2_wanda_tone" to "guarded"),
                        impactSummary = "Wanda anlayışla gülümsedi ve sınırlarına saygı duyarak geri adım attı.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c4_accept",
                    label = "\"Evet... Bazen hangi şehrin benim olduğunu bile hatırlamıyorum. Sadece durmadan hareket etmeyi biliyorum.\"",
                    toneHint = "Açık • Empatik",
                    nextNodeId = "ch2_s5_stark_lab",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 12),
                        setFlags = mapOf("ch2_wanda_tone" to "open_heart"),
                        impactSummary = "Kayıp hissettiğini açıkça kabul etmen Wanda ile aranızda derin bir duygusal bağ kurdu.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c4_shut",
                    label = "\"Benim ne hissettiğim bu anlaşmanın maddeleri arasında değil.\" Konuyu sertçe kapat.",
                    toneHint = "Sert • Kapalı",
                    nextNodeId = "ch2_s5_stark_lab",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to -6),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch2_wanda_tone" to "closed"),
                        impactSummary = "Duygusal kapıyı sertçe kapattın; Wanda geri çekilip mesafene uydu.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── CH2 NEW SCENE 5: STARK LAB & SECRET FREQUENCY CHECK ──
        BookSceneNode(
            id = "ch2_s5_stark_lab",
            chapterNumber = 2,
            sceneOrder = 5,
            sceneTitle = "Atölyedeki İkinci Şart",
            locationTag = "AVENGERS KULESİ • TONY STARK ATÖLYESİ • 22:15",
            paragraphs = listOf(
                "Akşamın geç saatinde odana dönmeden önce 78. kattaki atölyeden mavi bir ışık sızdığını gördün. Tony Stark hâlâ uyanıktı; önünde ince bir titanyum bileklik ve iki ayrı frekans dalgası duruyordu.",
                "\"Bunu takip cihazı sanıp beni camdan atmadan önce dinle,\" dedi Tony başını kaldırmadan. \"S.H.I.E.L.D.'in yörüngedeki kuantum tarayıcısını bozacak kişisel bir frekans perdesi. Takarsan seni uyduya görünmez yapar. Ama karşılığında tek bir şartım var: Kırmızı Göz denen o şey kontrolünden çıkarsa bana dürüstçe haber vereceksin.\"",
                "Bileklik masanın kenarında soğuk gümüş gibi parlıyordu. Bir kalkan olabilirdi — ya da ince bir tasma."
            ),
            decisionTitle = "Stark'ın Frekans Bilekliği",
            choicePrompt = "Tony'nin uzattığı frekans perdesi bilekliğini kabul edecek misin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c5_take_band",
                    label = "Bilekliği al ve tak: \"İçinde takip çipi bulursam bileğini değil reaktörünü sökerim, Stark.\"",
                    toneHint = "Pragmatik • Güven Adımı",
                    nextNodeId = "ch2_s6_window",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 9, BookCharacterId.STEVE to 4),
                        avengersTrustDelta = 6,
                        secretUnlocked = "Stark Kuantum Perdesi",
                        setFlags = mapOf("has_stark_band" to "accepted"),
                        impactSummary = "Bilekliği kabul ettin; Tony bunu kendisine duyulan nadir bir güven işareti olarak aldı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c5_refuse_band",
                    label = "Geri it: \"Yirmi yıldır hiçbir bileklik takmadan uydulardan kaçtım. Kendi gölgem bana yeter.\"",
                    toneHint = "Bağımsız • Şüpheci",
                    nextNodeId = "ch2_s6_window",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to -3, BookCharacterId.NATASHA to 4),
                        mysteryDelta = 1,
                        setFlags = mapOf("has_stark_band" to "no"),
                        impactSummary = "Bağımsızlığını her şeyin üstünde tuttun; Tony omuz silkip bilekliği çekmeceye bıraktı.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        // ── CH2 SCENE 6: NIGHT WINDOW DECISION ──
        BookSceneNode(
            id = "ch2_s6_window",
            chapterNumber = 2,
            sceneOrder = 6,
            sceneTitle = "Penceredeki Gece",
            locationTag = "AVENGERS KULESİ • 74. KAT • 01:08",
            paragraphs = listOf(
                "Gece yarısını geçmişti. Masanın üstünde Steve Rogers'ın imzaladığı geçici protokol duruyordu: *Otuz Günlük Deneme ve Karşılıklı Koruma.* Ne bir zincir vardı ne de gizli bir dipnot — Tony Stark'ın bütün alaycılığına rağmen belge şaşırtıcı derecede temizdi.",
                "Cama yaklaşıp avucunu soğuk yüzeye dayadın. Aşağıda Hudson Nehri siyah ipek bir şerit gibi uzanıyordu. Bir düşünce uzağında Tokyo vardı, Lizbon vardı, yeniden tek başına ama kimseye borçlu olmamak vardı. Ama koridorun ucunda seni ilk kez bir dosya numarası gibi görmeyen insanlar da vardı.",
                "Yarın sabah o imzayı atmadan önce, kendi içinde hangi tarafa daha yakın olduğunu bilmeliydin."
            ),
            decisionTitle = "Penceredeki Gece Kararı",
            choicePrompt = "New York ışıklarına bakarken içindeki karar hangi yöne ağır basıyor?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c6_hope",
                    label = "Temkinli bir iyimserlikle kalmaya karar ver — belki bu kez kaçmadan da nefes alınabilir.",
                    toneHint = "Umutlu • Açık",
                    nextNodeId = "ch2_s7_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.WANDA to 5, BookCharacterId.CLINT to 4, BookCharacterId.BRUCE to 4),
                        avengersTrustDelta = 6,
                        setFlags = mapOf("ch2_outlook" to "optimistic"),
                        impactSummary = "Avengers'a karşı temkinli ama umutlu bir adım attın; kalma fikri ilk kez gerçek geldi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c6_escape_plan",
                    label = "Kule'nin tüm çıkışlarını ve kaçış rotalarını zihnine kazı — anlaşma bozulursa saniyede yok ol.",
                    toneHint = "Dallanma • Hazır Kaçış",
                    nextNodeId = "ch2_s7_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.STEVE to -3),
                        mysteryDelta = 2,
                        setFlags = mapOf("ch2_outlook" to "escape_ready"),
                        branchTag = "Hazır Kaçış Protokolü",
                        impactSummary = "Şüpheci ve kaçış odaklı refleksini korudun; kriz anında anında tepki vermeye hazırsın.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c6_conflicted",
                    label = "Kararı zamana bırak — ne kör güven ne de hemen kaçış; otuz günü bir sınav gibi yaşa.",
                    toneHint = "Dengeli • Kararsız",
                    nextNodeId = "ch2_s7_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 4, BookCharacterId.NATASHA to 3),
                        setFlags = mapOf("ch2_outlook" to "conflicted"),
                        impactSummary = "Dengeli şüpheciliğini korudun; sözlerin değil eylemlerin konuşmasını beklemeye karar verdin.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        // ── CH2 END NODE ──
        BookSceneNode(
            id = "ch2_s7_end",
            chapterNumber = 2,
            sceneOrder = 7,
            sceneTitle = "Şartlar İmzalandı",
            locationTag = "AVENGERS KULESİ • ŞAFAK VAKTİ",
            paragraphs = listOf(
                "Şafak sökerken kalemi eline aldın. Kağıdın altına sadece sahte bir baş harf değil, yirmi yıldır sakladığın gerçek adını yazdın: *Aiden Blackwood.* Kalemin kâğıtta bıraktığı ses bir kapının kapanışı değil, yeni bir koridorun açılışı gibiydi.",
                "Aynı dakikalarda, binlerce kilometre doğuda, eski bir Sovyet arşiv binasının beton bodrumunda tozlu bir ekran kendi kendine mavi bir ışıkla uyandı. Ekranda yalnızca üç kelime yanıp sönüyordu: *DENEK 7-A — AKTİF.*"
            ),
            isChapterEnd = true,
            chapterEndQuote = "Şartlar kabul edildi. Ama geçmişin seninle yaptığı anlaşma henüz bitmemişti."
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM III: DENEME SÜRESİ
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch3_s1",
            chapterNumber = 3,
            sceneOrder = 1,
            sceneTitle = "Otuz Günlük Teklif",
            locationTag = "AVENGERS KULESİ • 60. KAT EĞİTİM MERKEZİ",
            paragraphs = listOf(
                "Üçüncü sabahında Kule artık ilk geceki kadar yabancı gelmiyordu — yine de her koridor dönüşünde istemsizce kamera açılarını ölçüyordun. 60. katın yüksek çelik kapıları açıldığında geniş antrenman salonunun parlak ışıkları yüzüne vurdu.",
                "Steve Rogers ellerini beyaz sargı beziyle sararken başını kaldırdı. \"Hoş geldin, Aiden. Protokolümüz resmi olarak bugün başlıyor: otuz günlük deneme süresi. Bu sürede kimse seni izinsiz bir operasyona sürmeyecek, ama hem fiziksel hem enerji sınırlarını ekip içinde tanımamız gerekiyor.\"",
                "\"Otuz gün uzun gelebilir,\" dedi arka taraftaki monitörleri açan Bruce Banner, gözlüğünü düzelterek. \"Ama stres altındaki enerji dalgalanmanı görmeden sana gerçek bir koruma sağlayamayız.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "has_stark_band",
                    requiredFlagValue = "accepted",
                    callbackLabel = "Geçmiş Kararın Yankısı · Kuantum Perdesi",
                    paragraphs = listOf("Bileğindeki Stark frekans perdesi monitörle hafifçe senkronize oldu; Bruce şaşkınlıkla gülümsedi: \"Tony bunu takacağını pek sanmıyordu, güzel başlangıç.\"")
                )
            ),
            decisionTitle = "Otuz Günlük Teklife Cevap",
            choicePrompt = "Otuz günlük deneme süresini hangi koşullarla kabul edeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c1_terms",
                    label = "\"Otuz gün kabul. Ama istediğim an çıkış kapısı açık kalır — tek bir izinsiz deney olursa biter.\"",
                    toneHint = "Şartlı • Özgür",
                    nextNodeId = "ch3_s2_sparring_prep",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 4),
                        setFlags = mapOf("ch3_probation_mode" to "conditional"),
                        impactSummary = "Özgürlüğünden ödün vermeden net sınırlarla ilk adımı attın.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c1_unconditional",
                    label = "\"Anlaştık, Rogers. Tereddüt yok — otuz gün boyunca tam odakla buradayım.\"",
                    toneHint = "Kararlı • Güven Veren",
                    nextNodeId = "ch3_s2_sparring_prep",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 10, BookCharacterId.CLINT to 5, BookCharacterId.BRUCE to 6),
                        avengersTrustDelta = 8,
                        setFlags = mapOf("ch3_probation_mode" to "committed"),
                        impactSummary = "Tereddütsüz kararlılığın Steve Rogers'ın sana duyduğu güveni belirgin biçimde yükseltti.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c1_short",
                    label = "\"Otuz gün fazla. Önce bir hafta deneyelim — iki taraf da sağ kalırsa uzatırız.\"",
                    toneHint = "Temkinli • Kısa Süre",
                    nextNodeId = "ch3_s2_sparring_prep",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 4, BookCharacterId.STEVE to -2),
                        setFlags = mapOf("ch3_probation_mode" to "short_trial"),
                        impactSummary = "Steve anlayış gösterdi ama ekibin temkinli yaklaşımı bir süre daha devam etti.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch3_s2_sparring_prep",
            chapterNumber = 3,
            sceneOrder = 2,
            sceneTitle = "Minderdeki Sınav",
            locationTag = "AVENGERS KULESİ • ANTRENMAN SALONU",
            paragraphs = listOf(
                "Steve kalkanını kenardaki askıya bıraktı ve minderin merkezine yürüdü. Yukarıdaki cam gözlem balkonundan Natasha, Wanda, Tony ve Bruce izliyordu; Clint ise kollarını korkuluğa yaslayıp gülümseyerek seslendi: \"Bahis açsak mı, Kaptan? Benim oyum sürpriz atımızda!\"",
                "\"Güçlerin olmadan değil, güçlerinle nasıl nefes aldığını görmek istiyorum,\" dedi Steve, gardını kaldırarak. \"Savunmamı test et, Aiden. Unutma, bu bir kavga değil — kontrol egzersizi.\"",
                "İlk kez bu kalibrede bir savaşçıyla karşı karşıyaydın. İlk iki hamleyi kısa uzam kaymalarıyla savuşturdun; üçüncüsünde Steve'in hızı ve ritmi şaşırtıcıydı. Kalkanı olmadan bile tek bir denge kaybını affetmiyor, seni adım adım köşeye sıkıştırıyordu. Damarlarındaki enerji baskıya tepki vererek ısındı."
            ),
            decisionTitle = "Steve'e Karşı Güç Gösterimi",
            choicePrompt = "Steve baskıyı artırdığında gücünün ne kadarını serbest bırakacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c2_control",
                    label = "Kendini tamamen kontrol et; yalnızca kısa ışınlanma adımlarıyla savunmasını dengele, zarar verme.",
                    toneHint = "Disiplinli • Kontrollü",
                    nextNodeId = "ch3_s3_energy_test",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 9, BookCharacterId.NATASHA to 6),
                        avengersTrustDelta = 6,
                        setFlags = mapOf("ch3_sparring" to "controlled"),
                        impactSummary = "Üstün kontrolün ve hızın Natasha ile Steve'in derin takdirini kazandı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c2_push",
                    label = "Steve'in savunmasını sonuna kadar zorla — Beyaz Göz kıvılcımıyla onu köşeye sıkıştır ama son milimetrede dur.",
                    toneHint = "Keskin • Etkileyici",
                    nextNodeId = "ch3_s3_energy_test",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 7, BookCharacterId.NATASHA to 9, BookCharacterId.TONY to 6),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch3_sparring" to "pushed_hard"),
                        impactSummary = "Hızın ve son andaki duruşun tüm balkonu sessiz bir hayranlığa boğdu.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c2_red_eye",
                    label = "Geri tutma — Kırmızı Göz ivmesini yarım saniye açıp şok dalgasıyla minderi kazan.",
                    toneHint = "Dallanma • Kırmızı Göz Taşması",
                    nextNodeId = "ch3_s3_energy_test",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 7, BookCharacterId.STEVE to -3, BookCharacterId.WANDA to 6, BookCharacterId.BRUCE to 5),
                        fatigueDelta = 1,
                        redEyeDelta = 2,
                        mysteryDelta = 2,
                        setFlags = mapOf("ch3_sparring" to "red_eye_surge"),
                        branchTag = "Kırmızı Göz Taşması",
                        secretUnlocked = "Kırmızı Göz Rezonans Eşiği",
                        impactSummary = "Bruce Banner ölçüm değerleriyle irkildi; Kırmızı Göz kıvılcımı tüm balkona gücünün gerçek ölçeğini gösterdi.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch3_s3_energy_test",
            chapterNumber = 3,
            sceneOrder = 3,
            sceneTitle = "Enerji Odası ve Akşam Masası",
            locationTag = "AVENGERS KULESİ • YÜKSEK YALITIM ODASI",
            paragraphs = listOf(
                "Öğleden sonra Bruce ve Tony seni vibranyum alaşımlı enerji odasına aldı. Odanın ortasındaki hedef sütunu, normalde Thor'un yıldırımlarına dayanması için tasarlanmıştı.",
                "\"Sadece doğal bir dalga gönder, Aiden,\" dedi Bruce camın arkasındaki mikrofondan. \"Sınırını ölçmek istiyoruz, seni tüketmek değil.\"",
                "Avucundan çıkan kozmik dalga kalın yalıtım camlarını titrettiğinde monitörlerdeki çizgiler alışılmadık bir mor-mavi zirve çizdi. Test bitip ortak mutfağa çıktığınızda ise hava bambaşkaydı. Clint Barton elinde sıcak bir tepsiyle masaya oturmuş, sana doğru bir tabak uzatmıştı: \"Eğer bir Avengers kulesinde yaşıyorsan, Kaptan'ın 1940'lardan kalma tatsız beslenme listesini reddetmek zorundasın. Yoksa seni ilk haftadan kaçırırız, Süper Hayalet.\" Masanın öbür ucunda Thor devasa kupasını kaldırıp gür bir kahkahayla başını salladı."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch3_sparring",
                    requiredFlagValue = "red_eye_surge",
                    callbackLabel = "Geçmiş Kararın Yankısı · Kırmızı Göz",
                    paragraphs = listOf("Tony monitörden başını kaldırıp ıslık çaldı: \"Minderdeki o kırmızı kıvılcımdan sonra hedef sütununu çift katmanla güçlendirdim. İyi ki yapmışım — neredeyse eritiyordun.\"")
                )
            ),
            decisionTitle = "Clint Barton'ın Şakasına Tepki",
            choicePrompt = "Masadaki sıcak ortama ve Clint'in takılmasına nasıl yanıt vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c3_joke",
                    label = "\"Merak etme Barton, Kaptan'ın listesinden kaçmak S.H.I.E.L.D.'den kaçmaktan kolay görünmüyor.\" Kuru bir espriyle karşılık ver.",
                    toneHint = "Samimi • Esprili",
                    nextNodeId = "ch3_s4_alarm",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.CLINT to 12, BookCharacterId.TONY to 5, BookCharacterId.STEVE to 4, BookCharacterId.THOR to 6),
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch3_dinner" to "joked"),
                        impactSummary = "Yıllar sonra ilk kez bir masada kahkahalarla güldün; Clint ve Thor ile güçlü bir dostluk kuruldu.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c3_smile",
                    label = "Sessiz kal ama dudaklarının ucunda gerçek bir gülümsemeyle tabağı al.",
                    toneHint = "Sakin • Sıcak",
                    nextNodeId = "ch3_s4_alarm",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.CLINT to 6, BookCharacterId.WANDA to 5, BookCharacterId.BRUCE to 5),
                        setFlags = mapOf("ch3_dinner" to "smiled"),
                        impactSummary = "Mesafeli ama samimi gülümsemen masada saygı ve rahatlık yarattı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c3_cold",
                    label = "Şakayı ciddiye alıp soğuk dur: \"Bana lakap takmasan iyi olur, Barton.\"",
                    toneHint = "Soğuk • Mesafeli",
                    nextNodeId = "ch3_s4_alarm",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.CLINT to -5),
                        setFlags = mapOf("ch3_dinner" to "cold"),
                        impactSummary = "Mesafeli zırhını korudun; Clint omuz silkip geri adım attı.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        // ── CH3 SCENE 4: MAJOR BRANCHING POINT (S.H.I.E.L.D. ALARM) ──
        BookSceneNode(
            id = "ch3_s4_alarm",
            chapterNumber = 3,
            sceneOrder = 4,
            sceneTitle = "Kırmızı Alarm",
            locationTag = "AVENGERS KULESİ • 14. GÜN • 21:47",
            paragraphs = listOf(
                "On dördüncü gecede, Kule'nin tüm koridorları aniden koyu kehribar bir ışıkla yıkandı. FRIDAY'in sesi tavan hoparlörlerinden buz gibi yankılandı: \"Uyarı: İki adet S.H.I.E.L.D. yüksek öncelikli zırhlı Quinjet hava sahasına girdi. İniş yetkisi talep ediyorlar. Gerekçe: Sınıflandırılmamış Varlık Teslimi.\"",
                "İki kelime bütün vücudunu dondurdu: *Varlık Teslimi.* Yirmi yıllık bütün kâbuslar, beyaz duvarlar ve kelepçeler tek bir saniyede geri döndü. Kalp atışın hızlandı; parmak uçlarında mavi uzam kıvılcımları kendiliğinden yanmaya başladı.",
                "Steve elini havaya kaldırıp doğrudan sana döndü: \"Aiden, bana bak! Kimse seni teslim etmiyor. Yukarı çıkıp onları geri çevireceğim. Sadece... buradan ayrılma, tamam mı?\"",
                "Wanda bir adım yaklaştı, gözleri endişeyle parlıyordu. Ama camın dışından Quinjet türbinlerinin uğultusu salonu titretiyordu. Yirmi yıllık refleksin sana tek bir şey haykırıyordu: *Şimdi git. Kapanmadan git.*"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch2_trust_reply",
                    requiredFlagValue = "sincere",
                    callbackLabel = "Geçmiş Kararın Yankısı · Güven Sözü",
                    paragraphs = listOf("Steve'in sesi bir ton daha yakındı: \"Bana güvenmenin ne kadar zor olduğunu söylemiştin. Şimdi o sözü tutma sırası bende, Aiden.\"")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch2_outlook",
                    requiredFlagValue = "escape_ready",
                    callbackLabel = "Geçmiş Kararın Yankısı · Hazır Kaçış",
                    paragraphs = listOf("Günler önce ezberlediğin üç çıkış rotası zihninde anında parladı — güney havalandırma boşluğu, çatı anteni, ya da üç yüz kilometre kuzey.")
                )
            ),
            decisionTitle = "S.H.I.E.L.D. Alarmında Kader Kararı",
            choicePrompt = "Quinjet'ler çatıya inerken yirmi yıllık refleksine mi uyacaksın, yoksa ekibe mi güveneceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c4_stay",
                    label = "Kule'de kal. Kıvılcımları avucunda söndür ve Steve ile ekibe güvenmeyi seç.",
                    toneHint = "Büyük Dallanma • Güven",
                    nextNodeId = "ch3_s5_stayed",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 12, BookCharacterId.WANDA to 8, BookCharacterId.NATASHA to 6),
                        avengersTrustDelta = 12,
                        setFlags = mapOf("ch3_alarm_branch" to "stayed"),
                        branchTag = "Kule'de Kalış Yolu",
                        impactSummary = "Kaçmak yerine kalmayı seçtin; ekibin güveni ve senin içsel kırılman bir dönüm noktasına ulaştı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c4_roof",
                    label = "Refleksle en üst çatıya ışınlan, inişi gölgeden izle — tehlike geçince kendi adımınla geri dön.",
                    toneHint = "Büyük Dallanma • Kısa Kaçış",
                    nextNodeId = "ch3_s5_short_escape",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 7, BookCharacterId.NATASHA to 6, BookCharacterId.STEVE to 2),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch3_alarm_branch" to "short_escape"),
                        branchTag = "Çatı Gözlem Yolu",
                        impactSummary = "Refleksle uzaklaştın ama kaçıp gitmek yerine geri döndün; Wanda cesaretini övdü.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c4_far",
                    label = "Her şeyi geride bırakıp yüzlerce kilometre uzağa, karlı bir kuzey kasabasına ışınlan.",
                    toneHint = "Büyük Dallanma • Büyük Kaçış",
                    nextNodeId = "ch3_s5_big_escape",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -4, BookCharacterId.TONY to -3, BookCharacterId.WANDA to 5, BookCharacterId.THOR to 8),
                        fatigueDelta = 1,
                        mysteryDelta = 2,
                        avengersTrustDelta = -6,
                        setFlags = mapOf("ch3_alarm_branch" to "big_escape"),
                        branchTag = "Büyük Kaçış Yolu",
                        secretUnlocked = "Kuzey Fırtınasında Dönüş",
                        impactSummary = "Üç gün sonra kuzeyde ekibin seni avlamak için değil, geri çağırmak için geldiğini gördün.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── CH3 SCENE 5 (3 GENUINE BRANCH SCENES) ──
        BookSceneNode(
            id = "ch3_s5_stayed",
            chapterNumber = 3,
            sceneOrder = 5,
            sceneTitle = "Kalmanın Cesaretinde",
            locationTag = "AVENGERS KULESİ • 88. KAT",
            paragraphs = listOf(
                "Ellerini yumruk yapıp mavi kıvılcımları avucunda söndürdün. Kalbin kaburgalarına vuruyordu ama tek bir adım geri atmadın. \"Yukarı çık, Rogers,\" dedin sesin kısık ama çelik gibiydi. \"Buradayım.\"",
                "Yirmi dakika sonra asansör kapısı açıldığında Steve ve Tony yalnız döndü. Stark'ın yüzünde zafer dolu bir sırıtış vardı: \"S.H.I.E.L.D. temsilcilerine avukatlarım ve Kaptan'ın kalkanı eşliğinde çok nazikçe 'hayır' dedik. Yetki belgeleri çöpe gitti, Aiden. Burada dokunulmazsın.\"",
                "Steve omzuna hafifçe dokundu. \"Kaldığın için teşekkürler. Bunun senin için ne kadar zor olduğunu biliyorum.\" O gece geç saatte nefes almak için çatıya çıktığında rüzgârın içinde tek başına olmadığını fark ettin: Natasha Romanoff korkuluğun kenarında, New York'un ışıklarını izliyordu.",
                "\"Bütün tehlike geçti,\" dedi başını sana çevirerek. \"Yine de omuzların hâlâ gergin. Söyle bakalım, Blackwood — neden hâlâ bu kadar tetiktesin?\""
            ),
            decisionTitle = "Natasha İle Çatı Diyaloğu",
            choicePrompt = "Natasha'nın bu doğrudan sorusuna nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c5_sincere",
                    label = "\"Çünkü kalmak, kaybedecek bir şey biriktirmek demek. Ve bu, kaçmaktan daha korkutucu.\"",
                    toneHint = "Tam Dürüst",
                    nextNodeId = "ch3_s6_locket",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 12),
                        setFlags = mapOf("ch3_rooftop" to "sincere"),
                        secretUnlocked = "Kızıl Oda İtirafı",
                        impactSummary = "Natasha kendi Kızıl Oda geçmişinden bir parça paylaştı; aranızda sarsılmaz bir güven oluştu.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c5_joke",
                    label = "\"Sadece manzarayı kontrol ediyordum, Romanoff. Otuz gün bitmeden şehri ezberlemem lazım.\"",
                    toneHint = "Yarım Şaka",
                    nextNodeId = "ch3_s6_locket",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 5),
                        setFlags = mapOf("ch3_rooftop" to "joking"),
                        impactSummary = "Natasha hafifçe gülümsedi ve otuz günlük sürenin daha yeni başladığını hatırlattı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c5_close",
                    label = "\"Sadece hava alıyorum. İyiyim.\" Konuyu kapatıp sessizliğe dön.",
                    toneHint = "Kapalı",
                    nextNodeId = "ch3_s6_locket",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to -2),
                        setFlags = mapOf("ch3_rooftop" to "closed"),
                        impactSummary = "Natasha mesafene saygı duyup iyi geceler dileyerek geri çekildi.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch3_s5_short_escape",
            chapterNumber = 3,
            sceneOrder = 5,
            sceneTitle = "Çatıdan Dönüş",
            locationTag = "AVENGERS KULESİ • ÜST PİST",
            paragraphs = listOf(
                "Hava bir anda büküldü ve sen kulenin en üst anten kulesinin gölgesindeydin. Aşağıda iki siyah Quinjet pistte durmuş, zırhlı ajanlar inmişti. Ama gördüğün sahne beklediğin gibi değildi: Steve Rogers tek başına pistin ortasında durmuş, Tony Stark ise zırhı açık halde ajanların önüne set çekmişti. Seni aramadılar bile — seni korudular.",
                "Quinjet'ler boş dönüp gece göğünde kaybolduğunda ışınlanmadın. Merdivenlerden yürüyerek aşağı indin. Kapıda Wanda bekliyordu; yüzünde öfke değil, derin bir rahatlama vardı. \"Geri döndün,\" dedi alçak sesle. \"Kaçmamak kolay değildir. Ama kaçtıktan sonra geri dönmek... o daha büyük bir cesaret.\"",
                "Gece yarısı rüzgârı dinlemek için yeniden çatıya çıktığında Natasha Romanoff da oradaydı. Sana bakıp hafifçe başını salladı: \"İyi gözlem noktası seçmişsin, Blackwood. Söyle bakalım — tehlike geçtiğine göre neden hâlâ bu kadar tetiktesin?\""
            ),
            decisionTitle = "Natasha İle Çatı Diyaloğu",
            choicePrompt = "Natasha'nın sorusuna nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c5_sincere",
                    label = "\"Çünkü kalmak, kaybedecek bir şey biriktirmek demek. Ve bu, kaçmaktan daha korkutucu.\"",
                    toneHint = "Tam Dürüst",
                    nextNodeId = "ch3_s6_locket",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 12),
                        setFlags = mapOf("ch3_rooftop" to "sincere"),
                        secretUnlocked = "Kızıl Oda İtirafı",
                        impactSummary = "Natasha kendi Kızıl Oda geçmişinden bir parça paylaştı; aranızda sarsılmaz bir güven oluştu.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c5_joke",
                    label = "\"Sadece manzarayı kontrol ediyordum, Romanoff. Otuz gün bitmeden şehri ezberlemem lazım.\"",
                    toneHint = "Yarım Şaka",
                    nextNodeId = "ch3_s6_locket",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 5),
                        setFlags = mapOf("ch3_rooftop" to "joking"),
                        impactSummary = "Natasha hafifçe gülümsedi ve otuz günlük sürenin daha yeni başladığını hatırlattı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c5_close",
                    label = "\"Sadece hava alıyorum. İyiyim.\" Konuyu kapatıp sessizliğe dön.",
                    toneHint = "Kapalı",
                    nextNodeId = "ch3_s6_locket",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to -2),
                        setFlags = mapOf("ch3_rooftop" to "closed"),
                        impactSummary = "Natasha mesafene saygı duyup iyi geceler dileyerek geri çekildi.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch3_s5_big_escape",
            chapterNumber = 3,
            sceneOrder = 5,
            sceneTitle = "Üç Gün Sonra, Kuzeyde",
            locationTag = "VERMONT KIYI KASABASI → AVENGERS KULESİ",
            paragraphs = listOf(
                "Yüzlerce kilometreyi tek bir nefesle yırttın. Kendini karla kaplı, ıssız bir Vermont iskelesinde bulduğunda ciğerlerin buz gibi havayla doldu. Üç gün boyunca küçük bir motelde kaldın; kapıya yine yatak dayalıydı. Ama garip bir şey oldu — üçüncü günün akşamında kapını çalan S.H.I.E.L.D. değildi.",
                "Gökyüzünde alçak bir gök gürültüsü yuvarlandı ve iskelenin ucunda kırmızı pelerinli dev bir siluet belirdi. Thor, yanında silahsız duran Steve Rogers ile yürüyüp geldi. \"Seni avlamaya gelmedik, yıldız yolcusu,\" dedi Thor sakin bir gürlemeyle. \"Sadece kapının hâlâ açık olduğunu söylemeye geldik. S.H.I.E.L.D. eli boş döndü.\"",
                "Ertesi gece Kule'nin çatısında yeniden New York'a bakıyordun. Kimse hesap sormadı. Yalnızca Natasha Romanoff yanına gelip korkuluğa yaslandı: \"Üç gün rekorun değil ama geri dönmen yeni bir şey, Blackwood. Neden hâlâ bu kadar tetiktesin?\""
            ),
            decisionTitle = "Natasha İle Çatı Diyaloğu",
            choicePrompt = "Geri döndüğün bu gecede Natasha'ya ne söyleyeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c5_sincere",
                    label = "\"Çünkü kalmak, kaybedecek bir şey biriktirmek demek. Ve bu, kaçmaktan daha korkutucu.\"",
                    toneHint = "Tam Dürüst",
                    nextNodeId = "ch3_s6_locket",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 12),
                        setFlags = mapOf("ch3_rooftop" to "sincere"),
                        secretUnlocked = "Kızıl Oda İtirafı",
                        impactSummary = "Natasha kendi Kızıl Oda geçmişinden bir parça paylaştı; aranızda sarsılmaz bir güven oluştu.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c5_joke",
                    label = "\"Vermont'un kahvesi berbattı, Romanoff. Mecburen geri döndüm.\"",
                    toneHint = "Yarım Şaka",
                    nextNodeId = "ch3_s6_locket",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 5),
                        setFlags = mapOf("ch3_rooftop" to "joking"),
                        impactSummary = "Natasha hafifçe gülümsedi ve otuz günlük sürenin hâlâ devam ettiğini hatırlattı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c5_close",
                    label = "\"Sadece hava alıyorum. İyiyim.\" Konuyu kapatıp sessizliğe dön.",
                    toneHint = "Kapalı",
                    nextNodeId = "ch3_s6_locket",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to -2),
                        setFlags = mapOf("ch3_rooftop" to "closed"),
                        impactSummary = "Natasha mesafene saygı duyup iyi geceler dileyerek geri çekildi.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        // ── CH3 SCENE 6: LOCKET MOMENT ──
        BookSceneNode(
            id = "ch3_s6_locket",
            chapterNumber = 3,
            sceneOrder = 6,
            sceneTitle = "Gümüş Kolye ve Otuzuncu Gece",
            locationTag = "AVENGERS KULESİ • ÇATI TERASI • 02:30",
            paragraphs = listOf(
                "Natasha içeri girdiğinde çatıda yalnız kaldın. Rüzgâr ceketinin yakasını savururken elin cebindeki eski gümüş kolyeye gitti. Kapağı hafifçe çizikti; yirmi yıldır her şehirden, her boyuttan sağ çıkan tek hatıra.",
                "Otuzuncu günün sabahı neredeyse gelmişti. Bu kulede hâlâ geçici bir yabancı mıydın, yoksa ilk kez bir yerin parçası olmaya mı başlıyordun? Kolyenin soğuk metali avucunda yavaşça ısındı."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch3_rooftop",
                    requiredFlagValue = "sincere",
                    callbackLabel = "Bağ Yankısı · Kızıl Oda İtirafı",
                    paragraphs = listOf("Natasha'nın gitmeden önce fısıldadığı cümle hâlâ kulaklarındaydı: \"Kızıl Oda'dan çıktığımda ben de ilk altı ay kapının arkasında uyudum, Aiden. Bir gün tamamen geçmiyor... ama paylaşacak biri olduğunda hafifliyor.\"")
                )
            ),
            decisionTitle = "Kolye Anı & İç Ses",
            choicePrompt = "Gümüş kolyeye bakarken geçmişinle nasıl yüzleşeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c6_hope",
                    label = "Kolyeyi avucunda sıkıca tut ve New York ışıklarına umutla bak — belki bu kez kaybetmeden tutabilirsin.",
                    toneHint = "Umutlu",
                    nextNodeId = "ch3_s7_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 5, BookCharacterId.STEVE to 4),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch3_locket" to "hope"),
                        impactSummary = "Yıllar sonra ilk kez bir şeyleri kaybetmeden tutabileceğine inandın.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c6_suppress",
                    label = "Kolyeyi hızla cebine geri koy — duygusal zayıflık sahada hata yaptırır.",
                    toneHint = "Soğukkanlı",
                    nextNodeId = "ch3_s7_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch3_locket" to "suppressed"),
                        impactSummary = "Soğukkanlı zırhını korumaya devam ettin; duygularını kilidin ardına aldın.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c6_remember",
                    label = "Kolyenin kapağını açıp yirmi yıl önceki mavi gökyüzünü korkmadan hatırla ve iç yüzünü incele.",
                    toneHint = "Yüzleşme • Gizli Kazıma",
                    nextNodeId = "ch3_s7_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 7, BookCharacterId.NATASHA to 4),
                        secretUnlocked = "Mavi Gecenin Koordinatı",
                        setFlags = mapOf("ch3_locket" to "faced_past"),
                        impactSummary = "Geçmişin yükünü bastırmak yerine kabullendin; kolyenin iç kapağında mikroskobik bir seri kodu ('7-A / IST') fark ettin.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch3_s7_end",
            chapterNumber = 3,
            sceneOrder = 7,
            sceneTitle = "Deneme Süresi Tamamlandı",
            locationTag = "AVENGERS KULESİ • 30. GÜN ŞAFAĞI",
            paragraphs = listOf(
                "Otuzuncu günün sabahında brifing ekranı acil bir bildirimle aydınlandı. Nick Fury'nin hologramı masanın ortasında belirdiğinde yüzünde en ufak bir yumuşaklık yoktu. Manhattan Limanı'nda eski bir HYDRA hücresine ait yasadışı bir sevkiyat tespit edilmişti — ve sevkiyatın enerji imzası, Tony Stark'ın kayıtlarına göre, Aiden Blackwood'unkine ürkütücü derecede benziyordu.",
                "Deneme süresi bitmişti. Şimdi gerçek sahaya inme vaktiydi."
            ),
            isChapterEnd = true,
            chapterEndQuote = "Otuz gün bitti. Gölgelerden çıkıp ilk gerçek göreve yürüme zamanı."
        )
    ).associateBy { it.id }
}
