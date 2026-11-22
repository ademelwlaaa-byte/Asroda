package com.example.book

/**
 * CHAPTER 10 — GRAND SEASON 1 FINALE of "Kozmik Sürükleniş" (Aiden Blackwood — MCU).
 * Rebuilt to be truly unforgettable:
 * - Aiden STAYS with the Avengers across ALL 3 legendary Season Finale destinies!
 * - 8 monumental decision points & 2 huge structural branching splits
 * - Embedded 60fps Animated Scenes (MANHATTAN_ARMADA, MUTLAK_UYANIS, BLUE_SKY_2006)
 * - Embedded 60fps Grounded Rooftop Boss Duel Mini-Game ([[GAME3D|VAELOR_ROOFTOP|...]])
 * - Jaw-dropping 2006 Parents Twist + explosive Post-Credits Season 2 Hook (Dark Future Scarred Aiden).
 */
object CosmicDriftChapter10Finale {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 10,
            romanNumeral = "X",
            title = "Yıldızların Kırıldığı Gece",
            titleEn = "The Night the Stars Broke",
            subtitle = "SEZON FİNALİ • Avengers Kulesi Çatı Düellosu, Mutlak Uyanış ve Avengers'ın Yeni Çağı",
            description = "Efsanevi Sezon Finali: Manhattan Savaşı, 2006'nın saklı gerçeği, zorunlu Vaelor çatı düellosu, Mutlak Uyanış, Avengers ile 3 büyük kader finali ve 2. Sezon kancası.",
            descriptionEn = "Legendary Season Finale: Battle of Manhattan, the hidden truth of 2006, mandatory rooftop boss duel against Vaelor, Absolute Awakening, 3 Avengers-bound destinies, and Season 2 hook.",
            estimatedMinutes = 18,
            accentHex = 0xFFEAB308L,
            startNodeId = "ch10_s1",
            sceneCount = 9,
            decisionCount = 8
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 1: MANHATTAN SAVAŞI'NIN AÇILIŞI (Karar 1/8)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch10_s1",
            chapterNumber = 10,
            sceneOrder = 1,
            sceneTitle = "Mavi Gökyüzünün Altında — Manhattan Savaşı",
            locationTag = "MANHATTAN SEMA & AVENGERS KULESİ • 06:00",
            paragraphs = listOf(
                "Gökyüzü yirmi yıl sonra bir kez daha yanlıştı — ama bu kez aşağıda, buz gibi bir yangın merdiveninde korkuyla annesinin elini bırakan beş yaşında bir çocuk yoktu.",
                "[[ANIM|MANHATTAN_ARMADA|MANHATTAN SEMA · YILDIZLARIN KIRILDIĞI GECE|Soluk Mühür ana gemisi mavi yarıktan süzülerek Avengers Kulesi'nin tam karşısında durdu.]]",
                "Manhattan'ın üzerinde açılan o devasa mavi-gümüş boyut yarığından ağır, kemikleri titreten bir uğultuyla süzülen Soluk Mühür ana gemisi, karanlık gölgesini Central Park'ın kuzeyinden Hell's Kitchen'ın dar sokaklarına kadar bütün adanın üzerine sermişti. Geminin gövdesindeki onlarca bölme aynı anda açıldı; yüzlerce gümüş-beyaz kozmik saldırı dronu gökdelenlerin cam duvarları arasına dalarak yüksek enerjili ışınlar savurmaya başladı. Aşağıdaki caddelerden yükselen siren sesleri ve panik çığlıkları soğuk sabah rüzgârında birbirine karışıyordu. Ama bu şehir bugün yalnız değildi.",
                "Helicarrier'ın açık güvertesinden tek bir mavi kuantum kıvılcımıyla doğrudan Avengers Kulesi'nin yağmurlu çatı pistine ışınlandığın an, etrafındaki bütün Avengers hattı tek bir yürek gibi harekete geçti. Thor Odinson gök gürültüsüyle bulutları yarıp ilk drone sürüsünü kör edici bir Asgard şimşeğiyle gökyüzünden sildi; Tony Stark güçlendirilmiş yörünge zırhıyla gökdelenlerin arasında süpersonik manevralar yapıp sivil otobüslerin üzerine inen enkazları havada yakaladı; Steve Rogers ve Clint Barton Midtown köprüsündeki tahliye koridorunu kalkan ve patlayıcı oklarla açtı; Wanda Maximoff ise iki elinden yükselen devasa kızıl fırtınayla çöken bir çelik kuleyi havada durdurup güvenle sokağa indirdi.",
                "Tam o anda kulağındaki kapalı devre hatta Natasha Romanoff'un baskı altında her zamankinden daha soğuk ve keskin çıkan sesi yankılandı:",
                "[[COMMS|NATASHA ROMANOFF · KAPALI DEVRE HAT|Ana geminin dış kalkan jeneratörleri üç ayrı noktada Aiden — Thor ve Tony kuzey hattını zorluyor. Ama çok daha acil bir ihanet var: Direktör Marcus Vance Avengers Kulesi'nin 85. kat çekirdeğine sızdı ve Kafes Protokolü'nü şehir çapında bir kozmik bombaya çevirmek üzere!]]",
                "İki ölümcül ateşin tam ortasındaydın: yukarıda bin iki yüz metre yükseklikte, elinde Soluk Mühür'ü tutan Yüce Hakem Vaelor seni bekliyordu; hemen aşağıda ise yirmi yıllık kaçışının mimarı Marcus Vance, hem Kule'yi hem de senin içindeki kozmik enerjiyi tek bir düğmeyle yakmaya hazırlanıyordu. Bu artık tek başına bir kaçış değil — Avengers'la birlikte verdiğin en büyük savaştı."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch5_voss_fate",
                    requiredFlagValue = "protected",
                    callbackLabel = "Müttefik — Elias Voss Sahada",
                    paragraphs = listOf(
                        "Midtown'ın doğu hattında ani, yakıcı bir kırmızı enerji dalgası üç Tarikat nakliye gemisini birden düşürdü! Kule'de Raft kafesinden kurtardığın **Elias Voss**, kulaklıktan boğuk ama sarsılmaz bir sesle bağlandı: \"Sivilleri tahliye ediyorum 7-A! Borcumu ödüyorum — sen yukarıya ve Vance'e odaklan, doğu caddeleri bende!\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_finale_act",
                    requiredFlagValue = "saved_mira",
                    callbackLabel = "Müttefik — Mira Hell's Kitchen'da",
                    paragraphs = listOf(
                        "Hell's Kitchen'ın üzerinde berrak, artık tamamen dengelenmiş bir kızıl-mavi kalkan yükseldi ve köşedeki o küçük Türk lokantasının bulunduğu sokağı düşen dev bir drone enkazından kurtardı! İstanbul'da hayatını bağışladığın **Mira** gökyüzüne bakıp sana selam verdi: \"Bana öğrettiğin gibi Aiden — yıkmak için değil, korumak için!\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch9_broadcast",
                    requiredFlagValue = "public",
                    callbackLabel = "Sekiz Milyar Tanık",
                    paragraphs = listOf(
                        "Aşağıdaki sokaklarda insanlar sadece kaçmıyordu; telefon kameraları ve haber helikopterleri çatıdan çatıya mavi kuantum şimşekleriyle ışınlanan sana çevrilmişti. Artık saklanan bir gölge değildin — sekiz milyar insan Aiden Blackwood'un Avengers ile omuz omuza bu şehri nasıl savunduğunu canlı izliyordu."
                    )
                )
            ),
            decisionTitle = "Sezon Finali (1/8) — Manhattan Savaşı'nın Açılışı",
            choicePrompt = "Savaş başladı. Hem Vance'in Kule'deki tuzağına hem de yukarıdaki Tarikat filosuna karşı açılış hamlen ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch10_c1_lead_avengers",
                    label = "Tüm Avengers hattını tek bir kozmik koordinasyonla yönet — zamanı yavaşlatıp ekibe yolu aç ve Vance'in üzerine birlikte in.",
                    toneHint = "Ekip Lideri • Avengers Birliği • Yüksek Güven",
                    nextNodeId = "ch10_s2_vance",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.THOR to 3, BookCharacterId.TONY to 3, BookCharacterId.WANDA to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.LOYALTY, 4),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.TRUST, 3)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.AVENGERS_TRUST to 5, StoryFlagAxis.TACTICAL_DISCIPLINE to 3),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch10_open" to "team_lead"),
                        branchTag = "Avengers Komutanı",
                        impactSummary = "Yirmi yıl tek başına kaçan adam, ilk kez bütün Avengers'ı tek bir uyum içinde yönetti; Manhattan savunma hattı tek gedik vermedi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c1_natasha_tower",
                    label = "Natasha ile doğrudan Avengers Kulesi'nin çekirdek odasına ışınlan — Vance'in Kafes Protokolü'nü tek dakikada bitir.",
                    toneHint = "Gölge İkilisi • Natasha • Cerrahi Operasyon",
                    nextNodeId = "ch10_s2_vance",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 5, BookCharacterId.CLINT to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 4),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 3)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.SHADOW_ALLIANCE to 4, StoryFlagAxis.TACTICAL_DISCIPLINE to 3),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch10_open" to "natasha_strike"),
                        branchTag = "Kule'de Gölge",
                        impactSummary = "Natasha ile kusursuz bir sessizlikte Kule'ye indiniz; Vance daha düğmeye basamadan etrafını sardınız.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c1_solo_meteor",
                    label = "Kırmızı Göz'ü yakıp art arda ışınlanarak Tarikat dronlarının arasından geç ve Kule çekirdeğine tek hamlede in — düşmana yıkıcı gücünü ilan et.",
                    toneHint = "Tek Kişilik Fırtına • Mutlak Güç • Sarsıcı",
                    nextNodeId = "ch10_s2_vance",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 4, BookCharacterId.TONY to 2),
                        storyAxisDeltas = mapOf(StoryFlagAxis.COSMIC_AWAKENING to 5, StoryFlagAxis.INDEPENDENCE to 3, StoryFlagAxis.MENTAL_FATIGUE to 1),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch10_open" to "solo_meteor"),
                        branchTag = "Mavi Meteor",
                        impactSummary = "Yüzlerce gümüş dronu tek bir mavi enerji dalgasıyla gökyüzünden sildin; Vaelor bile geminin güvertesinden seni izlemek zorunda kaldı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 2: MARCUS VANCE VE KAFES PROTOKOLÜ (Karar 2/8)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch10_s2_vance",
            chapterNumber = 10,
            sceneOrder = 2,
            sceneTitle = "Kafes Protokolü — Son İhanet",
            locationTag = "AVENGERS KULESİ • 85. KAT ÇEKİRDEK ODASI",
            paragraphs = listOf(
                "Avengers Kulesi'nin seksen beşinci katındaki ana enerji çekirdeğinde hava yanık ozon ve yüksek gerilim kokuyordu. Dışarıdaki savaşın sarsıntıları kurşun geçirmez cam duvarları titretiyor, uzaktan Thor'un şimşeklerinin parlaklığı odayı saniyeler içinde aydınlatıp karartıyordu. Yüksek Direktör Marcus Vance'in etrafını çeviren son dört siyah karbon zırhlı özel muhafız tek bir mavi enerji dalgasıyla yere serildiğinde bile adam geri adım atmadı.",
                "[[ALARM|KAFES PROTOKOLÜ · GERİ SAYIM 00:45|Avengers Kulesi Ana Ark Reaktörü ters rezonansa kilitlendi. Patlama yarıçapı: 12 blok. Hedef spektrum: Denek 7-A (Aiden Blackwood) kozmik sıfırlama.]]",
                "\"Yaklaşma Blackwood! Tek bir adım daha atma!\" diye haykırdı Vance. Alnından soğuk terler süzülüyordu ama gözlerindeki o soğuk, hastalıklı kibir hâlâ sönmemişti. \"Dr. Evelyn Hale bile bütün gerçeği bilmiyordu! O geceyi hâlâ bir laboratuvar kazası sanıyorsun değil mi? Baban — **Dr. Thomas Blackwood** — Proje Ayna'nın baş astrofizikçisiydi! Uzaydan yakaladığımız o kadim Soluk Mühür çekirdeğini silaha çevireceğimizi anlayınca laboratuvardan kaçırıp apartmanınızın altındaki sığınağa getirdi! Biz baskın yapınca da çekirdek ne bize ne de Vaelor'a geçmesin diye kendi canı pahasına radyasyonu üstlenip o saf mavi kıvılcımı beş yaşındaki oğluna mühürledi! Seni kurban etmedi — seni sakladı!\"",
                "Kelimeler bir şimşek gibi göğsüne indi. Yirmi yıldır gülüşünü hatırladığın baban, o gece elindeki kahveyi döken sıradan bir adam değildi; evrenin en tehlikeli sırrını kendi canı pahasına oğlunun içinde saklamış bir kahramandı.",
                "[[DOSSIER|PROJE AYNA — NİHAİ GİZLİ DOSYA (DR. THOMAS BLACKWOOD)|Tarih: 14 Kasım 2006 · Olay: Baş Astrofizikçi T. Blackwood, Soluk Mühür Çekirdeği'ni Konsey'den kaçırdı. Enerji yönlendirmesi: Oğlu Aiden Blackwood (Yaş 5). Son not: \"Oğlum onların silahı olmayacak.\"]]",
                "Çarpıcı mavi gözlerin doğrudan Vance'in gözlerine kilitlendi. Adam istemsizce bir adım geriledi; parmağı kırmızı anahtarın üstünde titriyordu. Şimdi hem gerçeğin tamamını biliyordun hem de Vance avucunun içindeydi."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch1_pact",
                    requiredFlagValue = "probed",
                    callbackLabel = "Bölüm I'den Miras — Kafes Protokolü Şifresi",
                    paragraphs = listOf(
                        "İlk gece Tony Stark'ın zihninin kenarından çektiğin o gizli kod adı şimdi hayat kurtardı: *Kafes Protokolü'nün tek manuel iptal hattı soldaki mavi kablo.* Vance senin bunu bildiğini fark ettiğinde yüzündeki bütün kan çekildi."
                    )
                )
            ),
            decisionTitle = "Sezon Finali (2/8) — Direktör Vance'in Kaderi",
            choicePrompt = "Babanın gerçeğini öğrendin ve Marcus Vance tam karşında. Onun hesabını nasıl kapatacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch10_c2_justice",
                    label = "Cihazı zaman bükmesiyle etkisiz hale getir, Vance'i tek çizik atmadan Steve ve Fury'nin adaletine teslim et.",
                    toneHint = "Adalet • Yüksek İrade • Ekip Onayı",
                    nextNodeId = "ch10_s3_rift_split",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 3, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 4),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 3)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 4, StoryFlagAxis.AVENGERS_TRUST to 4),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch10_vance" to "justice"),
                        branchTag = "Adil Hesaplaşma",
                        impactSummary = "İntikam yerine babanın gurur duyacağı adaleti seçtin; Vance kelepçelenip ömür boyu yargılanmak üzere teslim edildi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c2_white_eye_erase",
                    label = "Beyaz Göz'ü aç — Vance'in zihnindeki bütün Proje Ayna, Konsey ve 2006 sırlarını kalıcı olarak sil; onu zararsız bir gölgeye çevir.",
                    toneHint = "Beyaz Göz • Hafıza Silme • Karanlık Çözüm",
                    nextNodeId = "ch10_s3_rift_split",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 2, BookCharacterId.STEVE to -1, BookCharacterId.TONY to 1),
                        storyAxisDeltas = mapOf(StoryFlagAxis.WHITE_EYE_CONTROL to 4, StoryFlagAxis.DARK_TEMPTATION to 2, StoryFlagAxis.MENTAL_FATIGUE to 1),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch10_vance" to "erased"),
                        branchTag = "Beyaz Göz Hükmü",
                        impactSummary = "Vance bir saniye sonra neden o odada olduğunu bile hatırlamıyordu; geçmişinin karanlık arşivi zihninden tamamen silindi.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c2_shatter_empire",
                    label = "Cihazı parçalayıp bütün gizli Konsey sunucularını enerji dalgasıyla dünyaya sızdır — Vance'in kurduğu bütün sistemi bir saniyede çökert.",
                    toneHint = "Sistemi Yık • Stratejik İntikam • Tam Şeffaflık",
                    nextNodeId = "ch10_s3_rift_split",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 4, BookCharacterId.NATASHA to 4, BookCharacterId.CLINT to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.INDEPENDENCE to 3, StoryFlagAxis.PUBLIC_EXPOSURE to 3, StoryFlagAxis.TACTICAL_DISCIPLINE to 3),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch10_vance" to "exposed_empire"),
                        secretUnlocked = "Gölge Konsey'in Çöküşü",
                        branchTag = "Gölge Konsey Çöktü",
                        impactSummary = "Vance'in yirmi yıllık kara bütçesi ve babanın gerçek hikâyesi tüm dünya ekranlarına düştü; gölge konsey çöktü.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 3: BÜYÜK ORTA-FİNAL CEPHE AYRIMI (Karar 3/8 — GERÇEK BÜYÜK DALLANMA!)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch10_s3_rift_split",
            chapterNumber = 10,
            sceneOrder = 3,
            sceneTitle = "Kırık Gökyüzünün İki Kapısı",
            locationTag = "AVENGERS KULESİ • 88. KAT TERASI • 06:22",
            paragraphs = listOf(
                "Vance'in Kafes Protokolü sustuğu anda Kule'nin terasına çıktın — ve gördüğün manzara bütün savaşın seyrini bir anda ikiye böldü.",
                "Yukarıda, bin iki yüz metre yükseklikte Soluk Mühür ana gemisinin dev kalkan çekirdeği şarj oluyordu; Thor ve Tony dış zırhı zorluyordu ama kalkanı içeriden kıracak tek kişi sendin. Üstelik Kaelen yaralı olmasına rağmen geminin çekirdek odasında kadim bir kozmik silahı ateşlemek üzereydi.",
                "Ama aynı saniyede, tam beş blok güneyde — beş yaşındayken annenle babanın seni kurtardığı o eski New York apartman sokağının tam üzerinde — Vaelor'un açtığı ikinci bir **Kırık Zaman Çatlağı** yeri ikiye yardı! Sokaktaki binalar 14 Kasım 2006 gecesinin hayalet görüntüsüyle iç içe geçmeye başladı; Steve, Natasha ve Wanda yüzlerce sivili o zaman fırtınasının içinden çıkarmaya çalışırken çatlağın basıncı altında sıkışmışlardı.",
                "İki cepheye aynı anda yetişemezdin. Biri **gökyüzündeki uzay gemisinin kalbi**, diğeri **aşağıdaki 2006 zaman çatlağı ve ekibin canı**. Bu seçim hikâyenin orta finalini tamamen farklı bir cepheye taşıyacaktı."
            ),
            decisionTitle = "Sezon Finali (3/8) — BÜYÜK CEPHE DALLANMASI",
            choicePrompt = "Savaş iki tamamen farklı sahneye ayrılıyor. Yukarıdaki Ana Gemi'nin içine mi ışınlanacaksın, yoksa 2006 Zaman Çatlağı'na inip ekibi ve sokakları mı kurtaracaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch10_c3_branch_ship",
                    label = "ANA GEMİ BASKINI — Thor'un açtığı gedikten Soluk Mühür Ana Gemisi'nin çekirdeğine doğrudan ışınlan; Kaelen'i ve kalkan jeneratörünü içeriden parçala.",
                    toneHint = "BÜYÜK DAL A • Uzay Gemisi Baskını • Thor & Tony",
                    nextNodeId = "ch10_s4a_ship",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 4, BookCharacterId.TONY to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.THOR, CharacterFlagAxis.LOYALTY, 3),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 3)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.COSMIC_AWAKENING to 4, StoryFlagAxis.FIELD_INITIATIVE to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch10_mid_branch" to "mothership"),
                        branchTag = "Ana Gemi Baskını",
                        impactSummary = "Thor'un açtığı yarıktan doğrudan Soluk Mühür ana gemisinin kalbine ışınlandın; Tarikat'ın bin yıllık sırlarıyla yüzleştin.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c3_branch_manhattan",
                    label = "2006 ZAMAN ÇATLAĞI — Aşağı inip Natasha, Wanda ve Steve'in yanında dur; 2006 gecesinin hayalet fırtınasını kendi ellerinle kapat.",
                    toneHint = "BÜYÜK DAL B • 2006 Hayaleti • Natasha, Wanda & Steve",
                    nextNodeId = "ch10_s4b_manhattan",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.WANDA to 4, BookCharacterId.STEVE to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.LOYALTY, 3)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 5, StoryFlagAxis.PSYCHIC_RESONANCE to 3),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch10_mid_branch" to "manhattan_rift"),
                        branchTag = "2006 Zaman Çatlağı",
                        impactSummary = "Beş yaşındaki travmanın tam kalbine inip ekibi ve yüzlerce sivili kurtardın; geçmişin son zincirini kırdın.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 4A (BÜYÜK DAL A): SOLUK MÜHÜR ANA GEMİSİ KALBİ (Karar 4/8)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch10_s4a_ship",
            chapterNumber = 10,
            sceneOrder = 4,
            sceneTitle = "Büyük Dal A: Soluk Mühür'ün Kalbi",
            locationTag = "SOLUK MÜHÜR ANA GEMİSİ • ÇEKİRDEK SALONU • 1.200 METRE",
            paragraphs = listOf(
                "Thor Mjolnir'le ana geminin gümüş zırhında dar bir yarık açtığı saniyede, nefesini tutup tek bir mavi kuantum sıçramasıyla doğrudan o gediğin içine ışınlandın. Botların geminin soğuk zeminine çarptığında ne bir makine dairesiyle ne de bir savaş gemisiyle karşılaştın — yüksek, bembeyaz kristal sütunlarla dolu, sessiz bir kozmik katedraldi burası.",
                "Salonun tam ortasında, havada dönen antik bir kristal kayıt küresi ve önünde kırık porselen maskesiyle son savunmasını yapan **Kaelen** duruyordu. Kaelen seni görünce mızrağını kaldırdı ama bu kez sesinde kibir değil, tuhaf bir hayranlık vardı: \"Vaelor'un senden neden korktuğunu şimdi anlıyorum, Blackwood. Baban o gece sadece bir kıvılcımı saklamadı — iki evreni birbirine bağlayan tek anahtarı senin kanına mühürledi.\"",
                "[[AUDIO|SOLUK MÜHÜR ANTİK BELLEK KRİSTALİ (KOZMİK KAYIT)|\"Bin yıl önce Asıl Mimar'ın dövdüğü Tekillik Çekirdeği ikiye ayrıldı. Bir yarısı Soluk Mühür Tarikatı'nın elinde kaldı; canlı mavi çekirdek ise 14 Kasım 2006'da Dünya'daki bir astrofizikçinin oğluna mühürlendi. O çocuk bir hata değil — çoklu evrende eşi olmayan tek kilit taşıdır. Ve Asıl Mimar er ya da geç Kayıp Anahtarı'nı aramaya gelecek.\"]]",
                "Tony'nin sesi kulaklıkta çınladı: \"Aiden, kalkan jeneratörü otuz saniye içinde aşırı yüklenecek! Ya çekirdeği hemen patlatıp Vaelor'un zırh kalkanını düşürüyorsun, ya da o kristali kendi zihnine çekip Kaelen'i geçiyorsun — çabuk karar ver!\""
            ),
            decisionTitle = "Sezon Finali (4/8) — Ana Gemi Çekirdeği",
            choicePrompt = "Soluk Mühür'ün antik bellek kristali ve kalkan çekirdeği önünde. Kaelen'i ve bu odayı nasıl aşacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch10_c4a_steal_crystal",
                    label = "Kaelen'i zaman bükmesiyle dondur, antik kristalin kozmik bilgisini zihnine emip jeneratörü kapat — Vaelor'un en büyük sırrını al.",
                    toneHint = "Kozmik Bilgi • Zihin Emilimi • Gizli Avantaj",
                    nextNodeId = "ch10_s5_vaelor_duel",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.BRUCE to 3, BookCharacterId.THOR to 2),
                        storyAxisDeltas = mapOf(StoryFlagAxis.COSMIC_AWAKENING to 4, StoryFlagAxis.MYSTERY_AURA to 3, StoryFlagAxis.MENTAL_FATIGUE to 1),
                        fatigueDelta = 1,
                        mysteryDelta = 3,
                        setFlags = mapOf("ch10_mid_outcome" to "crystal_absorbed"),
                        secretUnlocked = "İki Evrenin Köprüsü (2006 Kökeni)",
                        branchTag = "Antik Sır Alındı",
                        impactSummary = "Soluk Mühür'ün bin yıllık sırrını zihnine indirdin ve geminin kalkanını düşürdün; artık Vaelor'un her hamlesini önceden okuyorsun.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c4a_shatter_core",
                    label = "Kaelen'in mızrağını kırıp Thor'un şimşeğiyle birlikte kalkan çekirdeğini tek darbede havaya uçur — gemiyi gökyüzünde felç et.",
                    toneHint = "Tam Yıkım • Thor İle Birlikte • Sıfır Tereddüt",
                    nextNodeId = "ch10_s5_vaelor_duel",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 5, BookCharacterId.TONY to 3),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.THOR, CharacterFlagAxis.LOYALTY, 4)),
                        storyAxisDeltas = mapOf(StoryFlagAxis.FIELD_INITIATIVE to 4, StoryFlagAxis.AVENGERS_TRUST to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch10_mid_outcome" to "core_shattered"),
                        branchTag = "Gemi Felç Edildi",
                        impactSummary = "Ana geminin kalkanları tamamen çöktü; bütün Tarikat drone filosu aynı saniyede gökyüzünden yere çakıldı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 4B (BÜYÜK DAL B): 2006 ZAMAN ÇATLAĞI & HAYALET SOKAK (Karar 4/8)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch10_s4b_manhattan",
            chapterNumber = 10,
            sceneOrder = 4,
            sceneTitle = "Büyük Dal B: 14 Kasım 2006'nın Hayaleti",
            locationTag = "MANHATTAN • ESKİ APARTMAN SOKAĞI • KIRIK ZAMAN ÇATLAĞI",
            paragraphs = listOf(
                "Aşağı indiğinde hava bir anda yirmi yıl geriye savruldu. Modern cam ofis binalarının üzerine 2006 yılının eski tuğla apartmanları, ıslak asfaltı ve o unutamadığın demir yangın merdiveninin yarı şeffaf, mavi bir hayaleti binmişti. Vaelor'un açtığı zaman çatlağı, doğrudan senin hafızandaki en derin yaradan besleniyordu.",
                "Sokağın ortasında Steve Rogers kalkanıyla savrulan araçları durduruyor, Natasha Romanoff son iki çocuğu güvenli alt geçide çekiyor, Wanda Maximoff ise titreyen kızıl kalkanıyla çatlağın büyümesini engellemeye çalışıyordu. Ama çatlağın tam kalbinden yükselen mavi ışık, zihninin en savunmasız köşesine dokundu:",
                "[[MIND|2006 ZAMAN YANKISI · ANNENİN VE BABANIN SESİ|Aiden, tatlım, bana bak... Sadece bana bak. O gece seni bırakmadık — baban ışığı senden değil, seni onların elinden saklamak için yönlendirdi. Şimdi koşmayı bırak oğlum; artık bir evin var.]]",
                "Wanda nefes nefese yanına koştu: \"Aiden, Vaelor bu anıyı bir silah gibi kullanıyor! Eğer çatlağı şimdi kapatmazsak bütün Manhattan'ı yutar — ya Beyaz Göz'le bu tuzağı parçala ya da enerjimi alıp benimle birlikte mühürle!\""
            ),
            decisionTitle = "Sezon Finali (4/8) — 2006 Hayaletiyle Yüzleşme",
            choicePrompt = "Geçmişinin en acı ama en onurlu anı gözlerinin önünde. Bu zaman çatlağını nasıl kapatacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch10_c4b_wanda_seal",
                    label = "Wanda'nın elini tut — mavi ve kızıl gücü tek bir kalkan dalgasında birleştirip annene ve babana veda et ve çatlağı mühürle.",
                    toneHint = "Wanda İle Birlik • Duygusal Kapanış • Şifa",
                    nextNodeId = "ch10_s5_vaelor_duel",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 5, BookCharacterId.STEVE to 3, BookCharacterId.NATASHA to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 4),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.LOYALTY, 4)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 5, StoryFlagAxis.PSYCHIC_RESONANCE to 4),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch10_mid_outcome" to "rift_healed"),
                        secretUnlocked = "2006'ya Son Veda",
                        branchTag = "Geçmişle Barış",
                        impactSummary = "Wanda ile mavi-kızıl fırtınayı birleştirip 2006 çatlağını tamamen kapattınız; yirmi yıllık suçluluk sonsuza dek silindi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c4b_white_eye_shatter",
                    label = "Beyaz Göz'ü sonuna kadar aç — Vaelor'un bu ucuz zihin tuzağını tek bakışla paramparça edip zaman enerjisini kendi gücüne kat.",
                    toneHint = "Beyaz Göz • Mutlak İrade • Güç Patlaması",
                    nextNodeId = "ch10_s5_vaelor_duel",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 4)),
                        storyAxisDeltas = mapOf(StoryFlagAxis.WHITE_EYE_CONTROL to 5, StoryFlagAxis.COSMIC_AWAKENING to 4),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch10_mid_outcome" to "illusion_shattered"),
                        branchTag = "İllüzyon Kıran",
                        impactSummary = "2006 hayaletini tek bir Beyaz Göz dalgasıyla parçalayıp bütün zaman enerjisini emdin; Natasha hayranlıkla başını salladı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 5: ZORUNLU ÇATI BOSS DÜELLOSU + VAELOR İLE KOZMİK YÜZLEŞME (Karar 5/8)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch10_s5_vaelor_duel",
            chapterNumber = 10,
            sceneOrder = 5,
            sceneTitle = "Son Yüzleşme — Avengers Kulesi Çatısında Vaelor Düellosu",
            locationTag = "AVENGERS KULESİ • PARÇALANMIŞ ÇATI PİSTİ • SAĞANAK YAĞMUR",
            paragraphs = listOf(
                "Orta cephe kazanıldığı saniyede gökyüzünden bütün adayı sarsan, kulakları sağır eden bir çatırtı koptu. **Yüce Hakem Vaelor**, elindeki kör edici **Soluk Mühür**'ü indirip gümüş bir yıldırım sütunuyla doğrudan **Avengers Kulesi'nin yağmurlu çatı pistine** indi! Çarpmanın şok dalgası beton zemini boydan boya yardı, çelik korkulukları büküp savurdu. Kule'nin tepesinde artık kaçacak yer yoktu — tek bir mavi ışınlanmayla tam karşısına çıktın.",
                "[[GAME3D|VAELOR_ROOFTOP|SON YÜZLEŞME · AVENGERS KULESİ ÇATISI|ZORUNLU BOSS DÜELLOSU — İlerlemek için Vaelor'u yen! Çatıda ışınlanarak saldırılardan kaç, Drift Formuyla enerjisini emip geri fırlat, Beyaz Göz'le zihnini felç et ve Kırmızı Göz'le yıkıcı darbeyi indir!]]",
                "Kırık betonların ve sağanak yağmurun ortasında, Vaelor'un Soluk Mühür kalkanını paramparça edip dizlerinin üstüne çökerttin. Karşında binlerce yıldır hiçbir ordunun durduramadığı o kadim kozmik yargıç artık ağır ağır soluyordu.",
                "\"Sonunda bana layık bir rakip, Aiden Blackwood,\" diye gürledi Vaelor. Soluk Mühür'ü son gücüyle yere vurduğunda çatı pistinin yarısı saf beyaz enerjiyle parçalandı. \"Baban onu benden yirmi yıl sakladı — ama bugün hem o kıvılcımı hem de bu şehri alacağım!\"",
                "Çarpışma gerçek bir kıyamet gibiydi. İlk darbede mavi enerji emilim bariyerin Vaelor'un gümüş şok dalgasını göğüsleyip üç blok ötedeki bulutları dağıttı. İkinci darbede Vaelor Soluk Mühür'ün bütün boyutsal basıncını çatıya boşalttı; yerçekimi on katına çıktı, burnundan kan akarken botların parçalanmış betona gömüldü. Ama Vaelor gardını tam açtığı o milisaniyede, son darbeyi indirecek tek bir açıklık gördün!"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch8_kaelen",
                    requiredFlagValue = "mind_raided",
                    callbackLabel = "Kaelen'in Zihninden Çekilen Sır",
                    paragraphs = listOf(
                        "Bölüm VIII'de Kaelen'in zihninden çaldığın o tek kırılma frekansı gözlerinin önünde parlak bir çizgi gibi belirdi: Vaelor'un sol omzundaki çekirdek kilidi tamamen savunmasızdı."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch9_vaelor_clash",
                    requiredFlagValue = "dimension_strike",
                    callbackLabel = "Kuzey Kutbu'ndaki Çatlak",
                    paragraphs = listOf(
                        "Kuzey Kutbu'nda boyut sıçramasıyla Soluk Mühür'de açtığın o ince çatlak şimdi bütün esere yayılıyordu — Vaelor'un silahı kendi ağırlığı altında sarsılıyordu."
                    )
                )
            ),
            decisionTitle = "Sezon Finali (5/8) — Vaelor'un Gardını Kır",
            choicePrompt = "Yüce Hakem Vaelor ölümcül darbeyi indirmek üzere. Kozmik düellonun kırılma hamlesi ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch10_c5_time_blink",
                    label = "Zamanı durdurup ardışık üç boyut sıçramasıyla Vaelor'un etrafını sar — zırhını her açıdan aynı anda parçala.",
                    toneHint = "Zaman & Işınlanma • Kusursuz Hız",
                    nextNodeId = "ch10_s6_awakening",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 3, BookCharacterId.TONY to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 4, StoryFlagAxis.COSMIC_AWAKENING to 3),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch10_duel_move" to "time_blink"),
                        branchTag = "Üçlü Boyut Darbesi",
                        impactSummary = "Vaelor senin hangi boyuttan vurduğunu bile göremeden yıldız metalinden zırhı paramparça oldu.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c5_white_eye_fear",
                    label = "Beyaz Göz'ü doğrudan Vaelor'un bin yıllık zihnine aç — ona kendi yok ettiği dünyaların çığlıklarını ve ilk kez korkuyu tattır.",
                    toneHint = "Beyaz Göz • Zihinsel Yargı • Sarsıcı",
                    nextNodeId = "ch10_s6_awakening",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 3, BookCharacterId.NATASHA to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.WHITE_EYE_CONTROL to 5, StoryFlagAxis.DARK_TEMPTATION to 1),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch10_duel_move" to "white_eye_fear"),
                        branchTag = "Yargıcın Korkusu",
                        impactSummary = "Binlerce yıldır korku nedir bilmeyen Vaelor, Beyaz Göz'ünün karşısında ilk kez dizlerinin üzerine çöktü.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c5_avengers_combo",
                    label = "Wanda, Thor ve Tony'ye işaret ver — üçünün bütün enerjisini kendi kozmik kalkanında toplayıp tek bir dev mavi ışınla Vaelor'a geri gönder.",
                    toneHint = "Avengers Birleşik Darbesi • Destansı Ekip",
                    nextNodeId = "ch10_s6_awakening",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.THOR to 4, BookCharacterId.WANDA to 4, BookCharacterId.TONY to 4),
                        storyAxisDeltas = mapOf(StoryFlagAxis.AVENGERS_TRUST to 5, StoryFlagAxis.HUMANITY_ANCHOR to 3),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch10_duel_move" to "avengers_combo"),
                        branchTag = "Birleşik Kıyamet",
                        impactSummary = "Asgard şimşeği, kızıl kaos, ark reaktörü ve senin kozmik enerjin tek bir kör edici sütunda birleşip Vaelor'u savurdu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 6: MUTLAK UYANIŞ — EVRENİN EN GÜÇLÜ VARLIĞI ANI (Karar 6/8)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch10_s6_awakening",
            chapterNumber = 10,
            sceneOrder = 6,
            sceneTitle = "Mutlak Uyanış — Evrenin En Güçlü Anı",
            locationTag = "AVENGERS KULESİ ÇATISI • ZAMANIN DIŞI",
            paragraphs = listOf(
                "Vaelor sendeleyip Soluk Mühür'ü elinden düşürdüğü saniyede, eser kırık çatı zemininde kendi kendine kritik eşiğe ulaştı — bütün Manhattan'ı ve doğu kıyısını tek bir mavi-beyaz patlamada silecek son geri sayım başlamıştı.",
                "[[ANIM|MUTLAK_UYANIS|MUTLAK UYANIŞ · EVRENİN EN GÜÇLÜ ANI|Mavi Kozmik Enerji, Beyaz Göz ve Kırmızı Göz tek bir iradede birleşti — yağmur damlaları havada dondu.]]",
                "Ve tam o saniyede, göğsünün en derininde yirmi yıldır korkuyla, mesafeyle ve kaçışla kilitli tuttuğun son kapı tamamen açıldı. Babanın seni saklamak için ödediği bedel... annenin son sözleri... dokuz yaşında yağmurda duyduğun o *canavar* fısıltısı... ve bugün aşağıda senin için sırtını ateşe veren Avengers ekibi. Hepsi tek bir saf, sınırsız bilince dönüştü.",
                "Botların çatlamış ıslak betona sapasağlam basarken, gözlerin hem baş döndürücü kozmik mavi hem de saf Beyaz Göz ve Kırmızı Göz gücüyle aynı anda parladı. Etrafındaki sağanak yağmur damlaları, parçalanmış beton taşları, fırtına rüzgârı ve hatta Soluk Mühür'ün kendi yıkıcı enerji akımı bir anda havada dondu ve **senin iradene** boyun eğdi.",
                "Bu kalıcı ya da sınırsız bir tanrılık değildi — yirmi yıldır içinde kilitli tuttuğun bütün kozmik enerjinin yalnızca **tek bir dakikalığına** taşmasıydı; insan bedenin bu muazzam basınca altmış saniyeden fazla dayanamazdı. Ama o tek dakikalığına Aiden Blackwood, evrendeki hiçbir gücün durduramayacağı zirveye ulaşmıştı. Vaelor dizlerinin üstünde, zırhı parçalanmış halde yağmurun ortasında karşısında duran sana bakıyordu. Soluk Mühür'ün bütün gücü, Manhattan'ın sekiz milyonluk kaderi ve Vaelor'un sonu şu an tek bir el hareketinin ucundaydı."
            ),
            decisionTitle = "Sezon Finali (6/8) — Mutlak Uyanış Kararı",
            choicePrompt = "Bedeninin yalnızca altmış saniye dayanabileceği bu mutlak zirve anında, Soluk Mühür'ü ve bu savaşı nasıl bitireceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch10_c6_shatter_banish",
                    label = "Soluk Mühür'ü parçalayıp Vaelor'u ve bütün Tarikat filosunu Dünya boyutundan sonsuza dek sürgün et — kozmik dengeyi koru.",
                    toneHint = "Merhametli Güç • Kozmik Denge • Temiz Zafer",
                    nextNodeId = "ch10_s7_crossroads",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.THOR to 5, BookCharacterId.WANDA to 4, BookCharacterId.NATASHA to 4),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 5, StoryFlagAxis.AVENGERS_TRUST to 5, StoryFlagAxis.COSMIC_AWAKENING to 4),
                        fatigueDelta = 2,
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch10_climax" to "banished"),
                        branchTag = "Mühür Kırıldı",
                        impactSummary = "Soluk Mühür'ü tek hareketle parçaladın ve Tarikat filosunu galaksinin ötesine sürdün. Gökyüzü bir daha açılmamak üzere kapandı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c6_absorb_seal",
                    label = "Soluk Mühür'ün bütün kozmik enerjisini kendi bedenine em — Vaelor'un gücünü sıfırlayıp evrenin yeni mutlak gücüne ulaş.",
                    toneHint = "Mutlak Güç • Sonsuz Enerji • Sarsıcı",
                    nextNodeId = "ch10_s7_crossroads",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.THOR to 3, BookCharacterId.STEVE to 1),
                        storyAxisDeltas = mapOf(StoryFlagAxis.COSMIC_AWAKENING to 6, StoryFlagAxis.DARK_TEMPTATION to 2, StoryFlagAxis.RED_EYE_INSTABILITY to 1),
                        fatigueDelta = 1,
                        redEyeDelta = 1,
                        setFlags = mapOf("ch10_climax" to "absorbed_seal"),
                        secretUnlocked = "Soluk Mühür'ün Yeni Sahibi",
                        branchTag = "Kozmik Hükümdar",
                        impactSummary = "Soluk Mühür'ün bin yıllık enerjisini kendi içine çektin; Vaelor diz çöktü ve Tarikat filosu dehşet içinde geri çekildi.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c6_rewind_city",
                    label = "Bütün kozmik gücünü zamanı geriye bükmek için harca — Vaelor'u durdururken Manhattan'daki bütün yıkımı ve yaraları sıfırla.",
                    toneHint = "Kozmik Fedakârlık • Zamanı Geri Al • Efsanevi",
                    nextNodeId = "ch10_s7_crossroads",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.NATASHA to 5, BookCharacterId.WANDA to 5, BookCharacterId.BRUCE to 5, BookCharacterId.TONY to 4),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 6, StoryFlagAxis.AVENGERS_TRUST to 6, StoryFlagAxis.MENTAL_FATIGUE to 3),
                        fatigueDelta = 3,
                        avengersTrustDelta = 6,
                        setFlags = mapOf("ch10_climax" to "rewound_city"),
                        branchTag = "Zamanın Kurtarıcısı",
                        impactSummary = "Kırılan bütün gökdelenler ve sokaklar gözlerinin önünde onarıldı; tek bir sivil bile zarar görmedi ama çatıya diz çökerken neredeyse bilincini kaybediyordun.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 7: AVENGERS'IN YENİ ÇAĞI — 3 EFSANEVİ KALIŞ FİNALİ (Karar 7/8)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch10_s7_crossroads",
            chapterNumber = 10,
            sceneOrder = 7,
            sceneTitle = "Yirmi Yılın Ardından — Avengers'ın Yeni Çağı",
            locationTag = "AVENGERS KULESİ • 88. KAT TERASI • 3 GÜN SONRA",
            paragraphs = listOf(
                "Savaşın üzerinden tam üç gün geçmişti. Manhattan'ın üzerindeki o korkunç mavi yarık bir daha açılmamak üzere kapanmış, yerini Hudson Nehri'ni altın gibi parlatan tertemiz bir ilkbahar güneşine bırakmıştı. Soluk Mühür filosu galaksinin karanlığına çekilmişti. Yüksek Direktör Marcus Vance'in gölge konseyi çökmüştü. Nick Fury bizzat Kule'ye gelip üzerinde adının yazdığı bütün eski yakalama emirlerini gözlerinin önünde yaktı — yirmi yıllık av nihayet kesin olarak bitmişti.",
                "Hızlı iyileşmen savaşın bütün izlerini çoktan silmişti. Seksen sekizinci katın güneşli terasında aşağıda yeniden nefes alan New York'a bakarken arkanda koca bir aile duruyordu: Steve Rogers, Tony Stark, Thor Odinson, Bruce Banner, Clint Barton, Wanda Maximoff ve Natasha Romanoff.",
                "Artık buradan gitmek gibi bir düşüncen yoktu. Beş yaşında kaybettiğin evi yirmi yıl sonra bu kulenin duvarlarında, bu insanların sırtında yeniden bulmuştun. Avengers'ta kalıyordun — ama bu yeni çağda **Avengers'ın içinde nasıl bir rol ve nasıl bir hayat** kuracağını yalnızca sen seçecektin."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch9_final_eve",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Natasha'nın Sessiz Sözü",
                    paragraphs = listOf(
                        "Natasha terasın kenarında durmuş, rüzgârda sana bakıyordu. Gözlerinde ne bir baskı ne de sabırsızlık vardı — ama dün gece Hell's Kitchen'daki o küçük Türk lokantasında kömür ateşinde köfte yerken kurduğunuz o sakin, gerçek gülümseme hâlâ ikinizin arasındaydı."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch9_final_eve",
                    requiredFlagValue = "wanda",
                    callbackLabel = "Wanda'nın Huzuru",
                    paragraphs = listOf(
                        "Wanda birkaç adım yanında duruyordu; zihninin sınırında artık ne korku ne de yalnızlık vardı. İki kozmik gücün birbirini tüketmeden aynı çatı altında huzurla nefes alabildiğini ikiniz de kanıtlamıştınız."
                    )
                )
            ),
            decisionTitle = "Sezon Finali (7/8) — AVENGERS'TA YENİ ÇAĞIN KADERİ",
            choicePrompt = "Avengers'ta kalıyorsun! 1. Sezonun büyük finalinde Avengers içindeki efsaneni hangi yolla mühürleyeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch10_c7_vanguard",
                    label = "FİNAL A · AVENGERS'IN KOZMİK KOMUTANI — Steve ve Tony'nin teklifini kabul et; Avengers'ın yeni kozmik savunma lideri ol ve büyük kutlamaya katıl.",
                    toneHint = "FİNAL A • Avengers Liderliği • Görkemli & Kahramanca",
                    nextNodeId = "ch10_s8_vanguard",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.TONY to 5, BookCharacterId.THOR to 5, BookCharacterId.BRUCE to 4),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TOWER_BELONGING to 6, StoryFlagAxis.AVENGERS_TRUST to 6),
                        avengersTrustDelta = 6,
                        setFlags = mapOf("season1_ending" to "vanguard"),
                        branchTag = "Final A: Kozmik Komutan",
                        impactSummary = "Aiden Blackwood, Avengers'ın yeni Kozmik Savunma Komutanı olarak Kule'de kalıcı yerini aldı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c7_twin_stars",
                    label = "FİNAL B · GÖLGE VE IŞIK — Kule'de kal ama unvanları bırak; yirmi yıllık zırhını indirip seçtiğin yakınlığın (Natasha / Wanda / Ekip) ve sıradan insan olmanın tadını çıkar.",
                    toneHint = "FİNAL B • Derin Duygusal Final • Avengers & Kalp",
                    nextNodeId = "ch10_s8_twin_stars",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 6, BookCharacterId.WANDA to 6, BookCharacterId.CLINT to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 4),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 4)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 6, StoryFlagAxis.TOWER_BELONGING to 5),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("season1_ending" to "twin_stars"),
                        branchTag = "Final B: Gölge ve Işık",
                        impactSummary = "Aiden Blackwood ilk kez kaçak ya da silah değil, sevdiği insanların yanında duran bir insan olmayı seçti.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c7_shield_reborn",
                    label = "FİNAL C · KOZMİK NÖBET MİMARI — Avengers çatısı altında Fury, Natasha ve Thor'la özel 'Kozmik Nöbet' birimini kur; bir daha hiçbir çocuk 'Denek' yapılmasın.",
                    toneHint = "FİNAL C • Avengers + Kozmik Nöbet • Stratejik & Güçlü",
                    nextNodeId = "ch10_s8_shield_reborn",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 5, BookCharacterId.THOR to 5, BookCharacterId.STEVE to 4, BookCharacterId.TONY to 4),
                        storyAxisDeltas = mapOf(StoryFlagAxis.SHADOW_ALLIANCE to 5, StoryFlagAxis.TACTICAL_DISCIPLINE to 5, StoryFlagAxis.TOWER_BELONGING to 5),
                        avengersTrustDelta = 5,
                        setFlags = mapOf("season1_ending" to "shield_reborn"),
                        secretUnlocked = "Kozmik Nöbet Protokolü",
                        branchTag = "Final C: Kozmik Nöbet",
                        impactSummary = "Avengers çatısı altında kurduğun Kozmik Nöbet ağı, hem Dünya'daki gizli laboratuvarları hem galaktik tehditleri gözetlemeye başladı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 8 (3 EFSANEVİ AVENGERS FİNALİ) + 2. SEZON MÜHRÜ (Karar 8/8)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch10_s8_vanguard",
            chapterNumber = 10,
            sceneOrder = 8,
            sceneTitle = "Final A: Avengers'ın Kozmik Komutanı",
            locationTag = "AVENGERS KULESİ & HELL'S KITCHEN • BÜYÜK KUTLAMA",
            paragraphs = listOf(
                "Steve Rogers sana resmi Avengers rozetini uzattığında terasta kısa, gururlu bir sessizlik oldu. \"Yirmi yıl önce gökyüzü senden her şeyi aldı Aiden,\" dedi Steve. \"Üç gün önce sen bütün bu şehri o gökyüzünden kurtardın. Artık bu masada sadece bir misafir değil, kozmik hattımızın komutanısın.\" Thor çekicini havaya kaldırıp gürledi: \"Asgard'ın müttefiki, Midgard'ın Kalkanı — Aiden Blackwood!\"",
                "Tony Stark gözlüğünü indirip sırıttı: \"Yeni katını hazırladım, takip çipi yok, üstelik özel espresso makinesi de koydum — Clint'ten saklı tut yeter.\"",
                "O akşam bütün Avengers ekibi — ve hayatta tuttuysan Elias Voss ile Mira — Hell's Kitchen'daki o buğulu camlı küçük Türk lokantasını tamamen kapattı. Kır saçlı yaşlı Türk usta kapıdan Avengers'ın ve çarpıcı mavi gözlü kahramanın girdiğini görünce gözleri doldu; sabaha kadar kömür ateşinde ızgara köfte, közlenmiş biber ve sıcak tırnak pide pişti. İlk kez sırtını kapıya dönüp oturdun ve kahkahaların arasında gerçekten evde olduğunu hissettin.",
                "Gece geç saatte Kule'nin seksen dördüncü katındaki pencerenden yıldızlara bakarken, gökyüzünün çok uzak bir katmanından — sadece senin duyabildiğin o kırık zaman çizgilerinin derinliğinden — ince, tanıdık bir **mavi frekans yankısı** çınladı."
            ),
            decisionTitle = "Sezon Finali (8/8) — 2. Sezon Mührü",
            choicePrompt = "Çok uzak bir kırık zaman çizgisinden gelen o gizemli mavi sinyal seni çağırıyor. 1. Sezonu kapatırken ne yapacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch10_c8_open_s2",
                    label = "Sinyali açık bırak ve Kule'nin ana bilgisayarına kaydet — 2. Sezonda Avengers ile bu gizemin üstüne gideceksin.",
                    toneHint = "2. Sezon Kapısı Açık • Kozmik Gizem",
                    nextNodeId = "ch10_s9_postcredits",
                    effect = BookChoiceEffect(
                        storyAxisDeltas = mapOf(StoryFlagAxis.MYSTERY_AURA to 3, StoryFlagAxis.COSMIC_AWAKENING to 2),
                        setFlags = mapOf("season2_hook" to "open"),
                        secretUnlocked = "2. Sezon Kancası: Kırık Zaman Sinyali",
                        branchTag = "Ufuk Açık",
                        impactSummary = "Bilinmeyen boyuttan gelen mavi sinyali dinlemeye devam ettin — 2. Sezon kapısı aralandı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c8_seal_s2",
                    label = "Şimdilik sinyali kozmik kalkanla mühürle — önce Avengers'la kazandığın bu barışı koru.",
                    toneHint = "Barış Öncelikli • Korumacı Mühür",
                    nextNodeId = "ch10_s9_postcredits",
                    effect = BookChoiceEffect(
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 3, StoryFlagAxis.TOWER_BELONGING to 2),
                        setFlags = mapOf("season2_hook" to "sealed"),
                        secretUnlocked = "2. Sezon Kancası: Mühürlü Yankı",
                        branchTag = "Barış Mührü",
                        impactSummary = "Sinyali şimdilik mühürledin — ama mührün arkasından gelen o tanıdık ses er ya da geç yeniden kapını çalacak.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch10_s8_twin_stars",
            chapterNumber = 10,
            sceneOrder = 8,
            sceneTitle = "Final B: Gölge ve Işık (Kule'de Yeni Hayat)",
            locationTag = "MANHATTAN • YAĞMUR SONRASI ÇATI & AVENGERS KULESİ",
            paragraphs = listOf(
                "\"Kule'de kalıyorum,\" dedin sakin, sıcak bir sesle. \"Büyük unvanlara ya da madalyalara ihtiyacım yok. Sadece artık kaçmak istemiyorum. Yirmi yıl sonra ilk kez bir sabah uyanıp kahvemi acele etmeden içmek, yanımdaki insanlara sırtımı korkmadan yaslamak istiyorum.\" Steve başını derin bir anlayışla eğdi; Tony tek bir şaka bile yapmadan gülümsedi.",
                "O akşam şehrin üzerindeki hafif bahar yağmuru yeni dinmişti. Yirmi yıldır sırtında taşıdığın o görünmez çelik zırh ilk kez omuzlarından düştü — artık evreni bükebilecek bir anomali değil, Avengers'ın içinde kendine gerçek bir hayat kuran Aiden Blackwood'dun.",
                "Ama gece yarısı Kule'nin en üst terasında yıldızlar parıldarken, gökyüzünün çok derinlerinden — yalnızca senin kanındaki 2006 imzasının duyabildiği kırık bir zaman çizgisinden — ürpertici derecede tanıdık bir **mavi sinyal** yankılandı."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch9_final_eve",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Natasha İle Sarsılmaz Denge",
                    paragraphs = listOf(
                        "Hell's Kitchen'daki küçük Türk lokantasında kömür ateşinde köfte yedikten sonra yağmurdan parlayan Manhattan sokaklarında yan yana yürüdünüz. Natasha elini yavaşça avucuna bıraktığında yüzünde yıllardır kimsenin görmediği o dingin, gerçek mutluluk vardı: \"Artık kaçmıyorsun Aiden. Ve ne olursa olsun, ben buradayım.\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch9_final_eve",
                    requiredFlagValue = "wanda",
                    callbackLabel = "Wanda İle Mavi ve Kızıl",
                    paragraphs = listOf(
                        "Kule terasında yıldızların altında Wanda başını omzuna yasladı; mavi kozmik enerjin ile onun kızıl ışığı ilk kez tek bir sakin ritimde nefes aldı: \"İkimiz de artık cam duvarların arkasında değiliz Aiden. Evimiz burası.\""
                    )
                )
            ),
            decisionTitle = "Sezon Finali (8/8) — 2. Sezon Mührü",
            choicePrompt = "Çok uzak bir boyuttan gelen o gizemli mavi sinyal seni çağırıyor. 1. Sezonu kapatırken ne yapacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch10_c8_open_s2",
                    label = "Sinyali açık bırak ve izini kaydet — yeni fırtına geldiğinde bu kez birlikte hazır olacaksınız.",
                    toneHint = "2. Sezon Kapısı Açık • Birlikte Hazırlık",
                    nextNodeId = "ch10_s9_postcredits",
                    effect = BookChoiceEffect(
                        storyAxisDeltas = mapOf(StoryFlagAxis.MYSTERY_AURA to 3, StoryFlagAxis.HUMANITY_ANCHOR to 2),
                        setFlags = mapOf("season2_hook" to "open"),
                        secretUnlocked = "2. Sezon Kancası: Kırık Zaman Sinyali",
                        branchTag = "Ufuk Açık",
                        impactSummary = "Sinyali dinlemeye devam ettin — 2. Sezonda Avengers'la birlikte yeni bir tehdit seni bekliyor.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c8_seal_s2",
                    label = "Sinyali şimdilik mühürle — bu gece sadece kazandığın huzurun tadını çıkar.",
                    toneHint = "Huzur Mührü • Sakin Kapanış",
                    nextNodeId = "ch10_s9_postcredits",
                    effect = BookChoiceEffect(
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 4),
                        setFlags = mapOf("season2_hook" to "sealed"),
                        secretUnlocked = "2. Sezon Kancası: Mühürlü Yankı",
                        branchTag = "Huzur Mührü",
                        impactSummary = "Sinyali şimdilik susturdun — ama kaderin 2. Sezonu çok uzakta değil.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch10_s8_shield_reborn",
            chapterNumber = 10,
            sceneOrder = 8,
            sceneTitle = "Final C: Kozmik Nöbet Mimarı (Avengers + Fury)",
            locationTag = "AVENGERS KULESİ • KOZMİK NÖBET MERKEZİ",
            paragraphs = listOf(
                "\"Avengers'ta kalıyorum,\" dedin Nick Fury ve Steve Rogers'ın gözlerinin içine bakarak. \"Ama Vance düştü diye dünyadaki bütün karanlık tesisler kendiliğinden kapanmayacak. Avengers çatısı altında yeni bir birim kuruyoruz: **Kozmik Nöbet.** Thor Asgard'dan gökyüzünü, Natasha gölge istihbaratı, Tony teknolojiyi, ben de sahayı tutacağım. Bir daha beş yaşında hiçbir çocuk camların arkasına kapatılıp 'Canavar' diye damgalanmayacak.\"",
                "Nick Fury'nin tek gözünde yıllardır ilk kez gerçek bir takdir parladı: \"Bunun için doğmuşsun Blackwood.\" Natasha kollarını kavuşturup hafifçe gülümsedi: \"İşte benim dilimden konuşmaya başladın.\" Hayatta tuttuysan Elias Voss ve Mira da bu yeni birimin ilk özel ajanları oldu.",
                "Bir hafta sonra Kule'nin yeni Kozmik Nöbet ekranları ilk kez kendi kendine saf maviye döndü: Dünya'nın ötesindeki kırık bir zaman çizgisinden doğrudan senin adına şifrelenmiş bir sinyal geliyordu."
            ),
            decisionTitle = "Sezon Finali (8/8) — 2. Sezon Mührü",
            choicePrompt = "Kozmik Nöbet tarayıcılarına takılan bu gizemli 2006 sinyaline ne yapacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch10_c8_open_s2",
                    label = "Sinyali Kozmik Nöbet şifre çözücüsüne bağla ve kapıyı açık tut — hiçbir tehdit Avengers'tan gizli kalamaz.",
                    toneHint = "2. Sezon Kapısı Açık • Kozmik İstihbarat",
                    nextNodeId = "ch10_s9_postcredits",
                    effect = BookChoiceEffect(
                        storyAxisDeltas = mapOf(StoryFlagAxis.SHADOW_ALLIANCE to 3, StoryFlagAxis.MYSTERY_AURA to 3),
                        setFlags = mapOf("season2_hook" to "open"),
                        secretUnlocked = "2. Sezon Kancası: Kırık Zaman Sinyali",
                        branchTag = "Nöbet Takibi",
                        impactSummary = "Bilinmeyen boyuttan gelen sinyali Kozmik Nöbet ağına bağladın — 2. Sezon başlıyor.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch10_c8_seal_s2",
                    label = "Sinyali karantinaya alıp mühürle — önce Dünya'daki son gizli Konsey tesislerini tamamen temizle.",
                    toneHint = "Karantina Mührü • Stratejik",
                    nextNodeId = "ch10_s9_postcredits",
                    effect = BookChoiceEffect(
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 3),
                        setFlags = mapOf("season2_hook" to "sealed"),
                        secretUnlocked = "2. Sezon Kancası: Mühürlü Yankı",
                        branchTag = "Nöbet Mührü",
                        impactSummary = "Sinyali şimdilik karantinaya aldın — ama o ses mührün arkasından Avengers'ı izlemeye devam ediyor.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 9: SEZON KAPANIŞI (Jenerik Sonrası İkiz Aiden Sahnesi Bölüm Bitiminde Seçimlerden Önce Açılır!)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch10_s9_postcredits",
            chapterNumber = 10,
            sceneOrder = 9,
            sceneTitle = "1. Sezonun Kapanışı — Uzak Bir Frekans",
            locationTag = "AVENGERS KULESİ • GECE YARISI • 2026",
            paragraphs = listOf(
                "— *1. SEZON FİNALİ TAMAMLANDI* —",
                "O gece Avengers Kulesi'nin seksen dördüncü katında New York uzun bir aradan sonra ilk kez huzurlu bir sessizliğe büründü. Yirmi yıllık kaçış bitmişti; artık saklanan bir gölge değil, bu şehrin ve bu ekibin parçasıydın.",
                "Ama gökyüzünün çok uzak bir katmanından — yalnızca senin kanındaki 2006 frekansının algılayabildiği kırık bir zaman çizgisinin derinliğinden — ince, ürpertici derecede tanıdık bir mavi sinyal karanlığın içinde tek bir kalp atışı kadar parladı.",
                "[[DOSSIER|KIRIK ZAMAN ÇİZGİSİ SİNYALİ · BEKLEYEN KAYIT|Frekans Eşleşmesi: %100.0 (Aiden Blackwood — Tekillik İmzası) · Durum: Bölümü bitirdiğinde jenerik sonrası sahne açılacak.]]"
            ),
            isChapterEnd = true,
            chapterEndQuote = "Artık kaçak bir anomali değil; Avengers'ın yeni çağını başlatan Aiden Blackwood'sun."
        )
    ).associateBy { it.id }
}
