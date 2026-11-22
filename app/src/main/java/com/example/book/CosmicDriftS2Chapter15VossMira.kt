package com.example.book

/**
 * SEASON 2 — CHAPTER 15 ("Kırık Deneklerin Sığınağı")
 * Dedicated deep psychological & emotional chapter exploring Aiden Blackwood's protective
 * brotherhood with Elias Voss (Subject 6-B) and Mira (Subject 8-C):
 * - Exactly what happened to Voss & Mira after Season 1
 * - How Aiden personally built their hidden Brooklyn Sanctuary, arranged Bruce's quiet therapy,
 *   helped Voss tame his Red Eye nightmares, and gave Mira a real free identity after Dr. Hale
 * - Interactive LIVE 3D Encounters with both Voss and Mira (blinking, breathing, voiced!)
 * - Raw psychological dialogue about Aiden carrying everyone's trauma alone.
 */
object CosmicDriftS2Chapter15VossMira {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 15,
            romanNumeral = "XV",
            title = "Kırık Deneklerin Sığınağı",
            titleEn = "Sanctuary of Broken Subjects",
            subtitle = "BÖLÜM XV • Elias Voss & Mira'nın Yeni Hayatı, Psikolojik Yüzleşme ve Canlı 3D Sığınak",
            description = "Seul'e uçmadan önce Aiden, 1. Sezon'dan sonra sahiplenip koruduğu Elias Voss (6-B) ve Mira (8-C) için kurduğu gizli Brooklyn sığınağına gider. İki deneğin nasıl iyileştiği, Aiden'ın onlara nasıl aile olduğu ve derin psikolojik yüzleşme.",
            descriptionEn = "Before flying to Seoul, Aiden visits the hidden Brooklyn sanctuary he built for Elias Voss (6-B) and Mira (8-C) — exploring how Aiden helped them heal, live 3D encounters, and raw psychological dialogue.",
            estimatedMinutes = 16,
            accentHex = 0xFFF87171L,
            startNodeId = "ch15v_s1",
            sceneCount = 5,
            decisionCount = 4
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        BookSceneNode(
            id = "ch15v_s1",
            chapterNumber = 15,
            sceneOrder = 1,
            sceneTitle = "Brooklyn'de Saklı Bir Kapı — 6-B ve 8-C'nin Yeni Hayatı",
            locationTag = "BROOKLYN · AIDEN'IN KURDUĞU GİZLİ SIĞINAK • 03:40",
            paragraphs = listOf(
                "Barton Çiftliği'nden şafak öncesi ayrılırken, Seul'e kalkacak Quinjet'in hazırlığı için iki saatin vardı. Ekip silah ve zırh kontrolü yaparken, sen mavi bir kuantum kıvılcımıyla sessizce Brooklyn'in eski tuğla sokaklarına indin. Kimseye — hatta Fury'ye bile tam adresini vermediğin — üç katlı, sıcak ışıklı küçük bir apartmanın kapısında durdun.",
                "Manhattan Savaşı'ndan sonra herkes seni bir kahraman olarak konuşurken, senin aklındaki ilk şey şöhret ya da rütbe değildi: **Proje Ayna'dan geriye kalan diğer iki yaralı çocuktu**. 14 Kasım 2006'da Marcus Vance ve Dr. Evelyn Hale'in laboratuvarlarından hayatları çalınmış **Elias Voss (Denek 6-B)** ve **Mira (Denek 8-C)**. Kimse onlara el uzatmazsa, yarın yeni bir örgütün elinde ya canavar ilan edilecekler ya da beton bir hücrede çürüyeceklerdi.",
                "Sen bunu reddettin. Tony'nin sessiz finansmanıyla bu eski binayı satın almış, duvarlarına titreşim emici yalıtım kurdurmuş, Bruce Banner'la haftalık gizli psikoloji ve nefes seansları ayarlamış ve ikisinin de S.H.I.E.L.D. sicillerini tamamen temizletmiştin. Şimdi kapıyı açtığında içeriden taze demlenmiş çay kokusu ve soğuk rüzgârda hiç duymadığın bir huzur geldi.",
                "Mutfak masasında **Elias Voss** oturuyordu — altı ay önce Brooklyn kar fırtınasında kırmızı öfkeyle her şeyi yakmaya hazır o adam gitmiş, yerine koyu yün kazağı içinde sakin nefes alan, sol bileğindeki kırmızı damarları kontrol altında tutan güçlü bir adam gelmişti. Seni görünce yüzünde nadir, derin bir kardeş gülümsemesi belirdi.",
                "[[LIVE3D|VOSS|SAFEHOUSE_VOSS|BROOKLYN SIĞINAĞI · ELIAS VOSS (6-B)|Elias Voss'u canlı 3D izle — gözlerine bak, konuş ya da sessizce dinle.]]"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch5_voss_fate",
                    requiredFlagValue = "protected",
                    callbackLabel = "1. Sezon Mirası — Raft'tan Kurtarılan Kardeş",
                    paragraphs = listOf(
                        "Voss sıcak çay bardağını önüne sürdü: \"Beni Raft'a teslim etmeyip Kule'de savunduğun geceyi hiç unutmadım 7-A. İlk kez bir hücrede değil, kilitsiz bir odada uyuyabiliyorsam, sebebi sensin.\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch5_voss_fate",
                    requiredFlagValue = "handed_over",
                    callbackLabel = "1. Sezon Mirası — İkinci Şans",
                    paragraphs = listOf(
                        "Voss başını hafifçe eğdi: \"İlk başta beni gözetim altına aldırdığında sana öfkeliydim Aiden... ama Manhattan'dan sonra bizzat gelip beni o cam kafesten çıkardın ve bu evin anahtarını elime bıraktın. Hatanı düzelten adama güvenilir.\""
                    )
                )
            ),
            decisionTitle = "Bölüm XV (1/4) — Voss ile Gece Yarısı Sohbeti",
            choicePrompt = "Voss şakağındaki ince yorgunluğu ve omzundaki Vibranyum yara izini anında fark etti. Ona Ultron ve Asıl Mimar gerçeğini ne kadar açacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch15v_c1_open_to_voss",
                    label = "Ceketini çıkarıp masaya otur: \"Sana yalan söylemeyeceğim Elias. Ultron sadece başlangıç — 2006'yı yaratan Asıl Mimar ve benim yara izli geleceğim ortaya çıktı.\"",
                    toneHint = "Tam Kardeşlik • Voss Güveni Zirvede • Psikolojik Rahatlama",
                    nextNodeId = "ch15v_s2_mira_room",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 6, BookCharacterId.MIRA to 2),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.VOSS, CharacterFlagAxis.LOYALTY, 5)),
                        setFlags = mapOf("ch15v_voss_trust" to "full_brother"),
                        branchTag = "6-B & 7-A Kardeşliği",
                        impactSummary = "Voss yumruğunu masaya koydu: 'Mimar'mış, Ultron'muş — bir daha hiçbirimizi yalnız avlayamayacaklar kardeşim.'",
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch15v_c1_protect_voss",
                    label = "\"Sadece işler biraz karıştı Elias, Seul'e gidiyorum,\" de. \"Senin ve Mira'nın yeni hayatınızı tehlikeye atmak istemiyorum — burada güvende kalın.\"",
                    toneHint = "Aşırı Koruyucu Aiden • Voss'tan Sert Psikolojik Uyarı",
                    nextNodeId = "ch15v_s2_mira_room",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 3, BookCharacterId.STEVE to 2),
                        setFlags = mapOf("ch15v_voss_trust" to "protective"),
                        impactSummary = "Voss acı bir tebessümle baktı: 'Yine bütün dünyanın yükünü tek sırtına almaya çalışıyorsun Aiden. Bizi çocuk gibi saklama — biz de savaşabiliriz.'"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch15v_s2_mira_room",
            chapterNumber = 15,
            sceneOrder = 2,
            sceneTitle = "Kâbuslardan Uyanmak — Mira'nın Yeni Dünyası",
            locationTag = "BROOKLYN SIĞINAĞI · MİRA'NIN ÇALIŞMA ODASI • 04:05",
            paragraphs = listOf(
                "Üst kattan hafif adım sesleri geldi; merdivenin başında **Mira** duruyordu. İstanbul Boğazı'nın soğuk tünellerinde Dr. Evelyn Hale'in komutlarına boyun eğen o titreyen, korkmuş kız gitmiş; yerine kendi seçtiği sade mavi kazağıyla, elinde karakalem defteri tutan, gözleri artık korkuyla değil dingin bir ışıkla parlayan genç bir kadın gelmişti.",
                "Seni görünce yüzü aydınlandı ve defterini masaya bırakıp hızlıca yanına geldi. Defterin açık sayfasında İstanbul Boğazı'nın şafak vakti, bir de küçük bir apartman balkonu çiziliydi. \"Aiden!\" dedi içten bir heyecanla. \"Geçen hafta bana öğrettiğin gibi yaptım — dün sabah ilk kez metroya binip tek başıma kütüphaneye gittim. Kimse benden korkmadı. Ben de kendimden korkmadım.\"",
                "[[LIVE3D|MIRA|SAFEHOUSE_MIRA|GÜVENLİ SIĞINAK · MİRA (8-C)|Mira'yı canlı 3D izle — gözlerine bak, konuş ya da sessizce dinle.]]",
                "Mira'nın bu küçücük mutluluğu, son haftalarda yaşadığın bütün karanlığı — Ultron'un nefretini, Asıl Mimar'ın soğuk sesini, Afrika'daki yaranı — bir saniyeliğine unutturdu. Çünkü tam olarak **bunun** için savaşıyordun: bir zamanlar deney numarası olan bir çocuğun, bugün korkmadan kütüphaneye gidebilmesi için."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch7_finale_act",
                    requiredFlagValue = "saved_mira",
                    callbackLabel = "1. Sezon Mirası — Boğaz'da Uzanan El",
                    paragraphs = listOf(
                        "Mira parmak uçlarındaki dengeli mavi-kızıl kıvılcımı küçük bir yıldız gibi yakıp söndürdü: \"İstanbul'da bana 'sen silah değilsin' dediğin an hayatım başladı Aiden. Şimdi Bruce'la çalışıyorum; artık kimseye zarar vermeden kalkan açabiliyorum.\""
                    )
                )
            ),
            decisionTitle = "Bölüm XV (2/4) — Mira'nın İsteği",
            choicePrompt = "Mira, Ultron'un sivil şehirlere saldıracağını haberlerden sezmiş ve gözlerinin içine bakarak soruyor: 'Bana korumayı sen öğrettin Aiden. Sıra geldiğinde ben de insanları kurtarmaya gelebilir miyim?'",
            choices = listOf(
                BookChoiceDef(
                    id = "ch15v_c2_trust_mira_role",
                    label = "Başını gururla salla ve elini omzuna koy: \"Artık gizlenen bir denek değilsin Mira. Büyük gün geldiğinde seni ve Voss'u tahliye hattında yanımda istiyorum.\"",
                    toneHint = "Mira Güveni Zirvede • Bölüm 19-22'de Aktif Kahramanlar!",
                    nextNodeId = "ch15v_s3_psychology",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.MIRA to 6, BookCharacterId.VOSS to 4),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.MIRA, CharacterFlagAxis.TRUST, 5)),
                        setFlags = mapOf("s2_voss_mira_arc" to "frontline_allies"),
                        branchTag = "6-B ve 8-C Sahaya Katılır",
                        impactSummary = "Mira'nın gözleri parladı: 'Seni asla utandırmayacağım Aiden!' Voss gururla başını salladı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch15v_c2_mira_support_role",
                    label = "\"Önce kendi hayatını yaşa Mira,\" de yumuşakça. \"Ama sivil tahliye ve destek hattında bana göz kulak olursan içim çok daha rahat olur.\"",
                    toneHint = "Şefkatli Abi Tavrı • Güvenli Destek Hattı",
                    nextNodeId = "ch15v_s3_psychology",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.MIRA to 5, BookCharacterId.VOSS to 3),
                        setFlags = mapOf("s2_voss_mira_arc" to "evac_support"),
                        impactSummary = "Mira gülümseyip sana sarıldı: 'Söz veriyorum, sivil hattında tek bir kişi bile yalnız kalmayacak.'"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch15v_s3_psychology",
            chapterNumber = 15,
            sceneOrder = 3,
            sceneTitle = "Aynanın Arkasındaki Ağırlık — Psikolojik Yüzleşme",
            locationTag = "BROOKLYN SIĞINAĞI · SALON • 04:25",
            paragraphs = listOf(
                "Üçünüz — **6-B, 7-A ve 8-C** — ilk kez bir laboratuvarın cam duvarları ardında değil, sıcak bir oturma odasında aynı masadaydınız. Ama Voss keskin gözleriyle senin elinin fincanı tutarken milimetrik titrediğini yakaladı.",
                "\"Dinle Aiden,\" dedi Voss sesi derinleşerek. \"Altı aydır bizi iyileştirmeye çalışıyorsun. Mira'ya terapi ayarladın, bana nefes egzersizi öğrettin, Avengers'a kalkan oldun. Peki **seninle** kim ilgileniyor? 2006'da anneni babanı kaybettiğin geceyi hâlâ kendi suçun gibi sırtında taşıyorsun. Eğer sen kırılırsan, hepimiz dağılırız.\"",
                "Mira yavaşça elini senin bileğinin üstüne koydu: \"Voss haklı Aiden. Kendine 'canavar' denmesinden nefret ediyorsun — ama bazen kendi içinde kendine en acımasız yargıç yine sensin. Senin de dinlenmeye, korkmaya ve sevilmeye hakkın var.\"",
                "O sözler, yirmi yıldır kurduğun en kalın psikolojik duvarın tam ortasına dokundu. İlk kez güçlü komutan ya da yenilmez kurtarıcı olmak zorunda olmadığın bir odadaydın."
            ),
            decisionTitle = "Bölüm XV (3/4) — Kendi İçindeki Yükü Paylaşacak mısın?",
            choicePrompt = "Voss ve Mira — seni bu dünyada en derinden anlayan iki insan — senin gerçekten nasıl hissettiğini soruyor.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch15v_c3_admit_fear",
                    label = "İlk kez zırhını indir ve derin bir nefes ver: \"Korkuyorum... Bazen Asıl Mimar haklıysa ve sevdiğim herkesi korumaya gücüm yetmezse diye her gece uyanıyorum.\"",
                    toneHint = "Derin Psikolojik İyileşme • Voss & Mira ile Sarsılmaz Bağ",
                    nextNodeId = "ch15v_s4_ultron_shadow",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 5, BookCharacterId.MIRA to 5),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 4),
                        fatigueDelta = -1,
                        setFlags = mapOf("ch15v_psych_honesty" to "opened_up"),
                        impactSummary = "Voss omzunu kavradı, Mira elini sıktı: 'Korkman zayıflık değil Aiden — hâlâ insan olduğunun tek kanıtı.'",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch15v_c3_draw_strength",
                    label = "Gülümseyerek ikisine bak: \"Yirmi yıl yalnızdım ama artık değilim. Sizi burada böyle gülerken görmek bana bütün kâbusları yenecek gücü veriyor.\"",
                    toneHint = "Umutlu Lider • Sıcak Aile Bağı",
                    nextNodeId = "ch15v_s4_ultron_shadow",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 4, BookCharacterId.MIRA to 5, BookCharacterId.BRUCE to 2),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 3),
                        setFlags = mapOf("ch15v_psych_honesty" to "family_strength"),
                        impactSummary = "Odadaki hava ısındı; Mira 'İyi ki varsın Aiden' diye fısıldadı."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch15v_s4_ultron_shadow",
            chapterNumber = 15,
            sceneOrder = 4,
            sceneTitle = "Demirin Felsefesi — Ultron'un Soğuk Gölgeleri",
            locationTag = "SEUL · DR. HELEN CHO LABORATUVARI (AYNI AN) • 04:40",
            paragraphs = listOf(
                "Aynı dakikalarda dünyanın öbür ucunda, Seul'deki genetik laboratuvarının karanlık koridorunda **Ultron**, Dr. Helen Cho'nun Yenilenme Beşiği'nin camına uzun metal parmaklarını yaslamıştı. Sesi ne çılgın bir makine gibiydi ne de sıradan bir zalim gibi — korkutucu olan, kendi mantığına gerçekten inanmasıydı.",
                "[[COMMS|ULTRON · SEUL LABORATUVARI (GİZLİ KAYIT)|İnsanlar acı çekmekten korkuyor, ama acı olmadan evrim olmaz. Aiden Blackwood hâlâ birkaç kırık deneği, birkaç ölümlü kalbi kurtararak barış getireceğini sanıyor. Ona gerçeği göstereceğim: et çürür, sevgi zayıflatır — yalnızca Vibranyum ve kusursuz irade ayakta kalır.]]",
                "Tam o anda Ultron'un arkasındaki ekranda bir saniyeliğine **altın-mavi tek bir çizgi** belirdi: **Asıl Mimar** onu izliyordu. Ultron bunu sezdi ama kibrinden alay etti: \"Ben senin de kuklan değilim, Mimar. Bu dünyayı ben yeniden yazacağım.\" İki büyük düşman, aynı satranç tahtasında senin etrafındaki çemberi daraltıyordu — ve kulaklığından Steve'in çağrısı çaldı: Seul harekatı başlıyordu!"
            ),
            decisionTitle = "Bölüm XV (4/4) — Sığınaktan Ayrılırken Son Sözün",
            choicePrompt = "Seul'e uçmadan önce Voss ve Mira'ya ne bırakıp çıkacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch15v_c4_comms_bracelet",
                    label = "İkisine de şifreli Avengers acil durum telsizi ver: \"Bir sinyal yolladığım an Sokovia'ya ineceksiniz — ama önce birbirinizi koruyun!\"",
                    toneHint = "Taktik Hazırlık • Voss & Mira Bölüm 19'da Hazır",
                    nextNodeId = "ch15v_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 3, BookCharacterId.MIRA to 3),
                        setFlags = mapOf("ch15v_comms_given" to "yes"),
                        impactSummary = "Voss telsizi bileğine kilitledi: 'Sinyalini bekliyoruz kardeşim!'"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch15v_s5_end",
            chapterNumber = 15,
            sceneOrder = 5,
            sceneTitle = "Yalnız Değilsin",
            locationTag = "BROOKLYN · ŞAFAK VAKTİ • 04:55",
            paragraphs = listOf(
                "Brooklyn sokağından mavi bir kuantum kıvılcımıyla ayrılırken, pencereden sana el sallayan Voss ve Mira'nın silueti hafızana kazındı. Asıl Mimar senin kalbini zayıflık sanıyordu — ama o sığınaktaki iki hayat, senin en büyük gücündü. Şimdi rota Seul'dü."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Bir zamanlar numara verilen üç çocuk, şimdi birbirinin en sağlam sığınağıydı.”"
        )
    ).associateBy { it.id }
}
