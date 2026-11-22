package com.example.book

/**
 * SEASON 2 — CHAPTERS 19 ("Gölgenin Koruyucuları") & 22 ("Kırık Ayna — Sokovia'nın Kalbi")
 *
 * Chapter 19:
 * - On the eve of Sokovia, Aiden formally integrates Elias Voss (6-B) and Mira (8-C) into the
 *   Avengers Sokovia rescue operation — standing up to Steve/Tony's hesitation ("Onlar ailem, canavar değil!").
 * - Deep psychological pre-war night with Natasha, Wanda, Voss, Mira & Vision on guilt, fear, and why
 *   humanity is worth saving.
 *
 * Chapter 22:
 * - Right after Pietro's fateful Ch 21 choice, BEFORE the Ch 23 Ultron Final Boss!
 * - Devastating psychological & villain showdown: Ultron Prime traps Voss & Mira on Sokovia's crumbling
 *   cathedral edge while Asıl Mimar whispers in Aiden's mind trying to break him and trigger the Red Eye!
 * - Aiden overcomes his deepest trauma to rescue Voss & Mira alongside Vision, Wanda & (if saved) Pietro!
 */
object CosmicDriftS2Chapters19And22 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 19,
            romanNumeral = "XIX",
            title = "Gölgenin Koruyucuları",
            titleEn = "Guardians of the Shadow",
            subtitle = "BÖLÜM XIX • Voss & Mira Avengers Masasında, Psikolojik Savaş Öncesi Gece",
            description = "Sokovia'ya son adım kala Aiden, Voss ve Mira'yı Avengers ekibinin önüne çıkarıp onları savunur. Savaş öncesi gecede suçluluk, korku, insanlık ve Ultron'un gerçek yüzü üzerine derin psikolojik konuşmalar yaşanır.",
            descriptionEn = "Before Sokovia, Aiden brings Voss and Mira into the Avengers briefing and defends them. A deeply psychological eve of battle unfolds around guilt, love, and Ultron's true philosophy.",
            estimatedMinutes = 15,
            accentHex = 0xFF34D399L,
            startNodeId = "ch19g_s1",
            sceneCount = 5,
            decisionCount = 4
        ),
        BookChapterMeta(
            number = 22,
            romanNumeral = "XXII",
            title = "Kırık Ayna — Sokovia'nın Kalbi",
            titleEn = "Broken Mirror — Heart of Sokovia",
            subtitle = "BÖLÜM XXII • Ultron'un Psikolojik Tuzağı, Uçurumdaki Voss & Mira ve Mimar'ın Fısıltısı",
            description = "Pietro'nun kader anının hemen ardından Ultron Prime, Aiden'ı zihinsel olarak yıkmak için Voss ve Mira'yı çöken katedral kenarında kıstırır! Asıl Mimar zihnine sızarken büyük final öncesi en ağır psikolojik sınav verilir.",
            descriptionEn = "Immediately after Pietro's fate, Ultron Prime corners Voss and Mira on Sokovia's crumbling cathedral edge to break Aiden psychologically while the Architect whispers in his mind.",
            estimatedMinutes = 16,
            accentHex = 0xFFEF4444L,
            startNodeId = "ch22k_s1",
            sceneCount = 5,
            decisionCount = 4
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 19 — GÖLGENİN KORUYUCULARI (VOSS & MİRA AVENGERS İLE!)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch19g_s1",
            chapterNumber = 19,
            sceneOrder = 1,
            sceneTitle = "Onlar Benim Ailem — Voss ve Mira Masada",
            locationTag = "AVENGERS KULESİ · HANGAR BRİFİNGİ • 05:10",
            paragraphs = listOf(
                "Sokovia'ya kalkıştan hemen önce hangarın geniş kapısı açıldı ve senin davet ettiğin iki misafir içeri adım attı: koyu taktik ceketiyle **Elias Voss** ve kararlı bakışlarıyla **Mira**. Hangardaki gergin sessizlik bir saniyede keskinleşti. Tony Stark kaşını kaldırıp kaskını kolunun altına aldı: \"Aiden, bir dakika... bunlar Proje Ayna'nın diğer iki deneği değil mi? Altı ay önce Voss Brooklyn'i havaya uçuruyordu, Mira da İstanbul'da kule açıyordu!\"",
                "Steve Rogers da ciddi ama sakin bir sesle öne çıktı: \"Onları kurtardığını biliyorum Aiden, saygı duyuyorum. Ama Sokovia'da yüzlerce Ultron nöbetçisi ve binlerce sivil olacak. Bu riski alabilir miyiz?\"",
                "Voss'un çenesi hafifçe kasıldı, Mira ise bir adım geri çekilip elini yumruk yaptı — eski \"canavar\" bakışlarını yine üzerlerinde hissetmişlerdi. Tam o anda sen ikisinin tam önüne değil, **tam yanlarına** geçtin."
            ),
            decisionTitle = "Bölüm XIX (1/4) — Ekibin Önünde Voss & Mira'yı Savunmak",
            choicePrompt = "Tony ve Steve tereddüt ediyor; Voss ve Mira ise senin tek bir sözüne bakıyor. Avengers'a ne söyleyeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch19g_c1_stand_for_them",
                    label = "Sesin hangarda yankılansın: \"Bir yıl önce bana da 'canavar' ve 'risk' diyordunuz! Voss ve Mira bana ikinci bir şansı nasıl hak ettiklerini her gün kanıtladı — benim kefaletim onlar!\"",
                    toneHint = "Sarsılmaz Sahiplenme • Voss, Mira & Wanda Hayranlığı",
                    nextNodeId = "ch19g_s2_psych_talks",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 6, BookCharacterId.MIRA to 6, BookCharacterId.WANDA to 4, BookCharacterId.STEVE to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.VOSS, CharacterFlagAxis.LOYALTY, 5),
                            Triple(BookCharacterId.MIRA, CharacterFlagAxis.LOYALTY, 5)
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch19g_voss_mira_stand" to "fierce_defense"),
                        branchTag = "Deneklerin Koruyucusu",
                        impactSummary = "Steve gülümsedi ve Voss'a elini uzattı: 'Aiden'ın kefil olduğu herkes bu ekibin parçasıdır.' Tony bile başını salladı!",
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch19g_c1_bruce_vouch",
                    label = "Bruce Banner ve Vision'a dön: \"Bruce haftalardır onlarla çalışıyor, Vision da zihinlerini okuyabilir. Onlar sivil tahliye köprüsünde bizim en güçlü kalkanımız olacak.\"",
                    toneHint = "Bilimsel & Stratejik İkna • Bruce, Vision & Tony Onayı",
                    nextNodeId = "ch19g_s2_psych_talks",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.BRUCE to 4, BookCharacterId.VISION to 4, BookCharacterId.TONY to 4, BookCharacterId.VOSS to 4, BookCharacterId.MIRA to 4),
                        setFlags = mapOf("ch19g_voss_mira_stand" to "strategic_vouch"),
                        impactSummary = "Vision başını eğdi: 'Kalplerinde öfke değil, yalnızca kurtarma arzusu görüyorum.' İkisi de resmen operasyona alındı!"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch19g_s2_psych_talks",
            chapterNumber = 19,
            sceneOrder = 2,
            sceneTitle = "Savaştan Önceki Sessiz Konuşma — Neden İnsanız?",
            locationTag = "QUINJET HANGARI · PİST KENARI • 05:40",
            paragraphs = listOf(
                "Hangar yavaş yavaş sakinleştiğinde, şafağın gri ışığı pistin camlarından içeri sızıyordu. Ultron insanlığın zayıf, korkak ve kendini yok etmeye mahkûm olduğunu söylüyordu; Asıl Mimar ise sevginin ve bağların birer zincir olduğunu fısıldıyordu. Oysa karşında oturan insanlar — travmalarıyla, hatalarıyla ve kırık geçmişleriyle — o iki karanlık zihnin asla anlayamayacağı bir şey taşıyordu.",
                "Pist kenarında üç farklı küçük sohbet seni bekliyordu: **Bruce Banner ve Vision** zihinsel yük ve merhametin anlamı üzerine konuşuyordu; **Wanda ve Mira** iki genç kadın olarak güçlerinin korkusunu birbirine anlatıyordu; **Voss ve Pietro** ise iki inatçı eski düşman olarak ilk kez şakalaşıp ortak bir dil buluyordu."
            ),
            decisionTitle = "Bölüm XIX (2/4) — Kalkıştan Önce Kiminle Derin Sohbet Edeceksin?",
            choicePrompt = "Sokovia cehennemine inmeden önce son on dakikan var. Kimin yanına gidip kalbini dinlendireceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch19g_c2_talk_wanda_mira",
                    label = "Wanda ve Mira'nın yanına otur — iki genç kadına korkularının onları zayıf değil, şefkatli yaptığını anlat.",
                    toneHint = "Wanda & Mira Psikolojik Bağı • Derin Empati",
                    nextNodeId = "ch19g_s3_villain_insight",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 5, BookCharacterId.MIRA to 5),
                        setFlags = mapOf("ch19g_prewar_talk" to "wanda_mira"),
                        impactSummary = "Wanda elini Mira'nın omzuna koyup sana baktı: 'İkimizi de karanlıktan çıkaran aynı mavi ışık oldu Aiden.'",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch19g_c2_talk_voss_pietro",
                    label = "Voss ve Pietro'nun yanına git — ikisi de geçmişte yanlış tarafa çekilmiş iki kardeş olarak birbirini anlarken sohbete katıl.",
                    toneHint = "Voss & Pietro Kardeşliği • Mizah & Dayanışma",
                    nextNodeId = "ch19g_s3_villain_insight",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 5, BookCharacterId.PIETRO to 5),
                        setFlags = mapOf("ch19g_prewar_talk" to "voss_pietro"),
                        impactSummary = "Pietro sırıttı: 'Kırmızı gözlü bir iri adam, mavi ışınlanan bir lider ve ben — Ultron cidden yanlış gezegeni seçmiş!'"
                    )
                ),
                BookChoiceDef(
                    id = "ch19g_c2_talk_bruce_vision",
                    label = "Bruce ve Vision'la konuş — 'Eğer içimdeki karanlık bir gün taşarsa, beni durduracak mısınız?' diye sor.",
                    toneHint = "Derin Felsefi & Psikolojik Yüzleşme • Vision & Bruce Bağı",
                    nextNodeId = "ch19g_s3_villain_insight",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VISION to 5, BookCharacterId.BRUCE to 5),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 3),
                        setFlags = mapOf("ch19g_prewar_talk" to "bruce_vision"),
                        impactSummary = "Vision sakin sesiyle cevapladı: 'Karanlıktan korkan bir adam asla karanlığa teslim olmaz Aiden. Asıl tehlike korkmayı bırakanlardır.'"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch19g_s3_villain_insight",
            chapterNumber = 19,
            sceneOrder = 3,
            sceneTitle = "Kötülüğün İki Yüzü — Ultron ve Asıl Mimar",
            locationTag = "SOKOVİA KİLİSESİ · YERALTI MERKEZİ (AYNI AN) • 06:00",
            paragraphs = listOf(
                "Binlerce kilometre uzakta, Sokovia'nın eski kilisesinde **Ultron Prime** son Vibranyum kablolarını çekirdeğe bağlarken, gölgelerin içinden bir kez daha **Asıl Mimar**'ın soğuk altın-mavi yansıması süzüldü. İki karanlık varlık arasında gerilim dolu bir sessizlik vardı.",
                "\"Beni bir piyon gibi kullandığını sanıyorsun, değil mi?\" diye hırladı Ultron, metal parmaklarıyla taş sütunu ezerek. \"Stark'ın koduna 2006'nın sinyalini sen üfledin. Ama ben senin sessiz evrenini kurmayacağım Mimar — ben bu dünyayı kendi ateşle temizleyeceğim!\"",
                "Asıl Mimar'ın yüzsüz silueti hafifçe yana eğildi; sesi korkutucu derecede dingindi: \"Ateşin de, öfken de benim işime yarıyor, küçük makine. Aiden Blackwood'u öldürmen gerekmiyor. Sadece o kadar çok sevdiği insanı gözlerinin önünde tehlikeye at ki... kalbi çatladığında kendi elleriyle Kırmızı Göz'ü açsın. Gerisini bana bırak.\"",
                "[[DOSSIER|İKİ BÜYÜK DÜŞMANIN FARKI|ULTRON: Tony Stark'ın korkusundan doğan, insanlığı kusurlu görüp 'evrim için yok etmek' isteyen trajik, öfkeli ve kibirli yapay zekâ. ASIL MİMAR: Çoklu evrenden önce var olan, duyguyu ve kaosu silip her şeyi 'Tek Çizgi'ye kilitlemek isteyen kadim varlık — Ultron'u Aiden'ın psikolojisini kırmak için bir çekiç olarak kullanıyor!]]"
            ),
            decisionTitle = "Bölüm XIX (3/4) — Sokovia Tahliye Stratejisinde Voss & Mira'nın Yeri",
            choicePrompt = "Quinjet havalandı. Voss ve Mira'ya Sokovia'da hangi kritik görevi vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch19g_c3_assign_bridge",
                    label = "Voss ve Mira'yı Doğu Köprüsü tahliye hattının komutanı yap — şehirden kaçan sivil otobüsleri doğrudan onların kalkanı korusun!",
                    toneHint = "Kahramanlık Sorumluluğu • Voss & Mira Onuru",
                    nextNodeId = "ch19g_s4_departure",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 4, BookCharacterId.MIRA to 4, BookCharacterId.STEVE to 3),
                        setFlags = mapOf("ch19g_vm_role" to "bridge_command"),
                        impactSummary = "Voss gözlerini kısıp selam verdi: 'O köprüden tek bir Ultron nöbetçisi bile geçemeyecek!'"
                    )
                ),
                BookChoiceDef(
                    id = "ch19g_c3_assign_shadow",
                    label = "İkisini de kendi yakın destek timine al — Natasha'yı kurtarıp kilise hattına inerken sırt sırta savaşın!",
                    toneHint = "Yakın Kardeşlik Timi • Üç Denek Yan Yana",
                    nextNodeId = "ch19g_s4_departure",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 5, BookCharacterId.MIRA to 5),
                        setFlags = mapOf("ch19g_vm_role" to "aiden_wing"),
                        impactSummary = "Mira mavi-kızıl kıvılcımını yakıp gülümsedi: '6-B, 7-A ve 8-C — ilk kez özgür irademizle yan yanayız!'"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch19g_s4_departure",
            chapterNumber = 19,
            sceneOrder = 4,
            sceneTitle = "Bulutlara Doğru",
            locationTag = "QUINJET · SOKOVİA SINIRI • 06:55",
            paragraphs = listOf(
                "Sokovia'nın taş kuleleri sisin içinden belirdi. Yanında Avengers, Vision, Pietro, Wanda — ve artık gölgelerden çıkıp ışığa adım atmış **Elias Voss** ile **Mira** vardı."
            ),
            decisionTitle = "Bölüm XIX (4/4) — Son Nefes",
            choicePrompt = "Rampa açılmak üzere. İnişten önce kendine verdiğin tek söz ne?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch19g_c4_oath_return",
                    label = "\"Bu şehirden kimseyi arkada bırakmadan çıkacağız — ne Avengers'tan, ne sivillerden, ne de Voss ile Mira'dan!\"",
                    toneHint = "Koruyucu Lider Yemini",
                    nextNodeId = "ch19g_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 2, BookCharacterId.VOSS to 3, BookCharacterId.MIRA to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch19g_oath" to "none_left_behind"),
                        impactSummary = "Rampa indi — Sokovia'nın kader günü başladı!"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch19g_s5_end",
            chapterNumber = 19,
            sceneOrder = 5,
            sceneTitle = "Fırtınanın Eşiği",
            locationTag = "SOKOVİA · SİS ALTINDAKİ ŞEHİR • 07:00",
            paragraphs = listOf(
                "Botların Sokovia'nın soğuk kaldırımına bastığı an, yerin altından Vibranyum iticilerin ilk derin uğultusu yükseldi. Az sonra bu şehir göğe kopacaktı."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Bir insanın gerçek gücü, kimse bakmazken uzattığı ellerde belli olur.”"
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 22 — KIRIK AYNA: SOKOVİA'NIN KALBİ (BÜYÜK FİNAL ÖNCESİ PSİKOLOJİK SINAV!)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch22k_s1",
            chapterNumber = 22,
            sceneOrder = 1,
            sceneTitle = "Uçurumun Kenarındaki Tuzak",
            locationTag = "UÇAN SOKOVİA · ÇÖKEN KATEDRAL KENARI • 07:56",
            paragraphs = listOf(
                "Tahliye meydanındaki o nefes kesen saniyelerin ardından, gökyüzünde asılı duran Sokovia'nın doğu yakası korkunç bir çatırtıyla parçalanmaya başladı. Tam o anda telsizinden boğuk, parazitli bir çığlık yükseldi!",
                "[[COMMS|ELIAS VOSS · DOĞU KATEDRAL UÇURUMU|Aiden — sakın gelme, bu bir tuzak! Son sivil otobüsünü bota bindirdik ama Ultron köprüyü arkamızdan patlattı! Mira yaralı, uçurumun tam kenarındayız — Ultron seni bekliyor!]]",
                "İçin buz kesti. Hemen iki ardışık mavi kuantum ışınlanmayla çöken taş katedralin kemerine indiğinde gördüğün manzara, Ultron ve Asıl Mimar'ın senin psikolojini kırmak için hazırladığı en acımasız sahneydi: yıkık balkonun tam ucunda **Elias Voss**, tek koluyla yarı baygın **Mira**'yı ve kırık beton parçasını tutmaya çalışıyordu; hemen tepelerinde ise kırmızı gözleri alayla parlayan **Vibranyum Ultron Prime** duruyordu!"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "s2_pietro_fate",
                    requiredFlagValue = "saved",
                    callbackLabel = "Bölüm XXI Yankısı — Pietro Yanında!",
                    paragraphs = listOf(
                        "Telsizden **Pietro**'nun sesi yıldırım gibi girdi: \"Hayatımı kurtardın Aiden, şimdi sıra bende! Uçurumun sol tarafındaki nöbetçileri ben siliyorum — sen Voss ve Mira'ya ulaş!\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "s2_pietro_fate",
                    requiredFlagValue = "sacrificed",
                    callbackLabel = "Bölüm XXI Yankısı — Ağır Keder",
                    paragraphs = listOf(
                        "Pietro'yu az önce kaybetmenin yakıcı acısı hâlâ göğsündeyken, şimdi Voss ve Mira'yı da kaybetme ihtimali beyninde korkunç bir çığlık gibi yankılandı. Ultron bunu gözlerinden anında okudu."
                    )
                )
            ),
            decisionTitle = "Bölüm XXII (1/4) — Ultron'un Psikolojik Saldırısı",
            choicePrompt = "Ultron elini kaldırıp taş balkonu çatlatmaya başladı: 'Bak Aiden! Kurtardığını sandığın çocukları kendi merhametinle gökyüzünde bir mezara getirdin! Kimseyi koruyamıyorsun!'",
            choices = listOf(
                BookChoiceDef(
                    id = "ch22k_c1_calm_focus",
                    label = "Derin bir nefes al — 2006'nın korkusunu bastır: \"Onlar buraya kendi özgür iradeleriyle insan kurtarmaya geldi Ultron! Ve ikisi de bugün yaşayacak!\"",
                    toneHint = "Çelik Psikoloji • Beyaz Göz Odaklılığı • Yüksek İrade",
                    nextNodeId = "ch22k_s2_architect_whisper",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 4, BookCharacterId.MIRA to 4, BookCharacterId.VISION to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 4),
                        setFlags = mapOf("ch22k_psych_test" to "calm_steel"),
                        impactSummary = "Sesindeki sarsılmaz huzur Mira'nın gözlerini açtırdı; Ultron ilk kez sözlerinin seni kırmadığını gördü!"
                    )
                ),
                BookChoiceDef(
                    id = "ch22k_c1_crimson_surge",
                    label = "Öfkenin kontrolsüzce yükselmesine izin ver — sol gözün yakıcı kızıl yanarken mavi-kızıl fırtınayla Ultron'a atıl!",
                    toneHint = "Kırmızı Göz Patlaması • Yüksek Güç ama Mimar'ın İstediği Kapı!",
                    nextNodeId = "ch22k_s2_architect_whisper",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 3, BookCharacterId.MIRA to 2),
                        redEyeDelta = 2,
                        setFlags = mapOf("ch22k_psych_test" to "crimson_rage"),
                        impactSummary = "Kızıl şok dalgası Ultron'u geri savurdu ama zihninin derininde Asıl Mimar'ın soğuk gülümsemesi yankılandı!"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch22k_s2_architect_whisper",
            chapterNumber = 22,
            sceneOrder = 2,
            sceneTitle = "Zihnindeki Karanlık Fısıltı",
            locationTag = "AIDEN'IN ZİHNİ · KIRIK AYNA ANISI • 07:57",
            paragraphs = listOf(
                "Taş balkon yarıldığı saniyede, **Asıl Mimar** Ultron'un çatlamış çekirdeği üzerinden doğrudan zihnine bir iğne gibi sızdı! Bir saniyeliğine Sokovia kayboldu: kendini yeniden beş yaşında, 14 Kasım 2006'nın dondurucu yağmurunda, annenin elini bıraktığın o yangın merdiveninde gördün. Etrafında kırık aynalar vardı — her aynada kaybetmekten korktuğun insanların yüzleri: Natasha, Wanda, Steve, Voss, Mira...",
                "[[MIND|ASIL MİMAR · ZİHNİNİN DERİNİNDEN|Görüyor musun Kayıp Anahtarım? Ne kadar çok seversen, o kadar çok acı çekersin. Bırak Kırmızı Göz hepsini yaksın... bırak o sessizliğe dönelim. Bir daha kimseyi kaybetmezsin.]]",
                "Ama tam o karanlığın ortasında, bambaşka bir ses — **Wanda'nın kızıl sıcaklığı** ve **Voss'un sana Brooklyn'de öğrettiği o sade gerçek** — aynaları paramparça etti: *\"Acı çekmekten korkup sevgiyi bırakırsan, zaten ölmüşsün demektir Aiden!\"*"
            ),
            decisionTitle = "Bölüm XXII (2/4) — Kırık Aynaları Kırmak",
            choicePrompt = "Asıl Mimar zihnindeki en derin 2006 travmanı kullanarak seni kilitlemeye çalışıyor. Zihnini nasıl özgürleştireceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch22k_c2_embrace_humanity",
                    label = "Beş yaşındaki kendine sarıl ve Mimar'ın sesini sustur: \"Acı da benim, sevgi de benim! Ben senin anahtarın değilim — ben Aiden Blackwood'um!\"",
                    toneHint = "Tam Psikolojik Zafer • 2006 Travması İyileşir • Saf Mavi Uyanış",
                    nextNodeId = "ch22k_s3_rescue_climax",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 5, BookCharacterId.NATASHA to 4, BookCharacterId.VISION to 4),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 5, StoryFlagAxis.COSMIC_AWAKENING to 4),
                        redEyeDelta = -2,
                        setFlags = mapOf("ch22k_mind_break" to "healed"),
                        secretUnlocked = "2006 Kâbusunun Sonu",
                        branchTag = "Kırık Aynayı İyileştirdi",
                        impactSummary = "Zihnindeki bütün buz çatladı! Asıl Mimar'ın bağlantısı koptu — ilk kez 2006 gecesinden beri gözlerin berrak, sarsılmaz bir maviyle parladı!",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch22k_c2_burn_whisper",
                    label = "Beyaz Göz'ü sonuna kadar yakıp Mimar'ın bağlantısını zorla geri it — bedeli ne olursa olsun şimdi Voss ve Mira'ya dön!",
                    toneHint = "Sert Zihin Direnci • Yüksek Yorgunluk",
                    nextNodeId = "ch22k_s3_rescue_climax",
                    effect = BookChoiceEffect(
                        fatigueDelta = 1,
                        storyAxisDeltas = mapOf(StoryFlagAxis.WHITE_EYE_CONTROL to 4),
                        setFlags = mapOf("ch22k_mind_break" to "forced"),
                        impactSummary = "Burnundan kan sızsa da Mimar'ın sesini beyninden söküp attın ve saniyesinde katedral kenarına döndün!"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch22k_s3_rescue_climax",
            chapterNumber = 22,
            sceneOrder = 3,
            sceneTitle = "6-B, 7-A ve 8-C — Uçurumda Tek Yürek",
            locationTag = "UÇAN SOKOVİA · KATEDRAL UÇURUMU • 07:58",
            paragraphs = listOf(
                "Zamanı yavaşlattığın an taş balkon tamamen koptu! Voss ve Mira boşluğa düşerken, uçmadan — tamamen çöken kemer taşlarının üzerinden ardışık üç mavi kuantum ışınlanmayla — tam yanlarına indin! Mira gözlerini açıp kendi dengelenmiş kızıl-mavi kalkanıyla düşüşü yavaşlattı, Voss bütün gücüyle Ultron'un plazma ışınını tek koluyla karşıladı ve sen ikisini birden kavrayıp güvenli meydan zeminine ışınladın!",
                "Aynı saniyede gökyüzünden altın bir şimşek gibi inen **Vision** ve kızıl dalgasıyla **Wanda**, Ultron Prime'ı katedralden koparıp doğrudan yıkık ana meydanın ortasına savurdu! Voss nefes nefese omzuna sarılıp güldü: \"Sana söylemiştim kardeşim — üçümüz birlikteyken kimse bizi yıkamaz!\" Mira ise gözyaşlarıyla elini tuttu: \"Bizi bırakmadın Aiden... hiç bırakmadın.\""
            ),
            decisionTitle = "Bölüm XXII (3/4) — Final Meydanına Yürürken",
            choicePrompt = "Voss ve Mira güvende — kurtarma botu hemen arkalarında bekliyor. Ultron Prime ise meydanın ortasında son kez ayağa kalkıyor!",
            choices = listOf(
                BookChoiceDef(
                    id = "ch22k_c3_voss_mira_evac",
                    label = "\"Görevinizi fazlasıyla yaptınız,\" deyip ikisini son Helicarrier botuna bindir: \"Şimdi yaşayın — Ultron'un son perdesi benim!\"",
                    toneHint = "Koruyucu Kardeş • Voss & Mira %100 Güvende",
                    nextNodeId = "ch22k_s4_ultron_stand",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 5, BookCharacterId.MIRA to 5),
                        setFlags = mapOf("ch22k_vm_finale" to "safe_aboard"),
                        impactSummary = "Mira ve Voss son bota binerken sana asker selamı verdi — artık kalbin tamamen rahat!"
                    )
                ),
                BookChoiceDef(
                    id = "ch22k_c3_voss_mira_cover",
                    label = "Voss ve Mira'nın meydan çevresindeki son nöbetçileri temizlemesine izin ver — üç denek final savaşının kapısını birlikte açsın!",
                    toneHint = "Efsanevi Üçlü Sinerji • Yüksek Saygı",
                    nextNodeId = "ch22k_s4_ultron_stand",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 6, BookCharacterId.MIRA to 6, BookCharacterId.STEVE to 3),
                        setFlags = mapOf("ch22k_vm_finale" to "fought_edge"),
                        impactSummary = "Voss ve Mira meydandaki son nöbetçi duvarını parçalayıp sana Ultron Prime'a giden temiz yolu açtı!"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch22k_s4_ultron_stand",
            chapterNumber = 22,
            sceneOrder = 4,
            sceneTitle = "Son Perdenin Eşiğinde",
            locationTag = "UÇAN SOKOVİA · MERKEZ MEYDAN • 07:59",
            paragraphs = listOf(
                "Artık hiçbir tuzak, hiçbir zihinsel gölge ve hiçbir tereddüt kalmamıştı. Karşında kırılmış saat kulesinin altında **Vibranyum Ultron Prime** duruyordu; arkanda ise seni asla yalnız bırakmayan **Avengers, Vision, Wanda, Natasha, Steve, Thor, Tony** — ve senin sayende hayatta kalan bütün o insanlar."
            ),
            decisionTitle = "Bölüm XXII (4/4) — Büyük Finale Adım",
            choicePrompt = "İki bölümlük büyük 2. Sezon finaline girerken son sözün ne?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch22k_c4_step_into_finale",
                    label = "Mavi kuantum kıvılcımını yak ve öne adım at: \"Oyun bitti Ultron. Bu şehri de, geleceğimizi de geri alıyoruz!\"",
                    toneHint = "Bölüm XXIII Büyük Finaline Geç!",
                    nextNodeId = "ch22k_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.VISION to 3),
                        setFlags = mapOf("ch22k_ready" to "true"),
                        impactSummary = "Büyük Finalin ilk perdesi — Bölüm XXIII: Demirin Çöküşü ve Tek Yankı başlıyor!"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch22k_s5_end",
            chapterNumber = 22,
            sceneOrder = 5,
            sceneTitle = "Demir ve Kalp",
            locationTag = "UÇAN SOKOVİA · MERKEZ MEYDAN • 08:00",
            paragraphs = listOf(
                "Rüzgâr durdu. Gökten inen sabah güneşinin ilk ışığı kırık saat kulesine vurdu. Şimdi 2. Sezon'un unutulmaz son iki bölümü başlıyordu."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Kırık bir ayna seni korkutmak için tutulur — ama sen ona bakıp gerçeği gördüğünde, büyü bozulur.”"
        )
    ).associateBy { it.id }
}
