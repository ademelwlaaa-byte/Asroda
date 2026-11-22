package com.example.book

/**
 * SEASON 2 — CHAPTER 21 ("Kilisenin Savunması & Gümüş Şimşeğin Kaderi") of
 * "Kozmik Sürükleniş — 2. Sezon: Demir ve Yankı"
 *
 * - Iconic Sokovia Church Core defense against Ultron's iron legion.
 * - **BIGGEST CHOICE OF SEASON 2**: Push Time Dilation & Grounded Blink to the absolute limit to
 *   SAVE PIETRO MAXIMOFF (`s2_pietro_fate = "saved"`) at severe physical toll, OR lock down the
 *   Church Core first and witness Pietro's heartbreaking canon sacrifice (`s2_pietro_fate = "sacrificed"`).
 * - Bridges directly into Chapter 22 ("Kırık Ayna — Sokovia'nın Kalbi") where Ultron corners Voss & Mira!
 */
object CosmicDriftS2Chapters19To20 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 21,
            romanNumeral = "XXI",
            title = "Kilisenin Savunması",
            titleEn = "Defense of the Church",
            subtitle = "BÖLÜM XXI • Çekirdek Çemberi, Quinjet Pususu ve Pietro Maximoff'un Kaderi",
            description = "Uçan şehrin kalbindeki kilisede amansız savunma! Ultron'un kaçırdığı Quinjet tahliye meydanına ateş açtığında, 2. Sezon'un en ağır kader kararı verilir: Pietro Maximoff'u kurtarabilecek misin?",
            descriptionEn = "Relentless defense of the flying city's church core! When Ultron hijacks the Quinjet and fires on Clint and a child, you face Season 2's defining choice: can you save Pietro Maximoff?",
            estimatedMinutes = 15,
            accentHex = 0xFF7DD3FCL,
            startNodeId = "ch19_s1",
            sceneCount = 5,
            decisionCount = 4
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 21 — KİLİSENİN SAVUNMASI (Karar 1/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch19_s1",
            chapterNumber = 21,
            sceneOrder = 1,
            sceneTitle = "Kırılmayan Çember",
            locationTag = "UÇAN SOKOVİA · MERKEZ KİLİSE • 07:47",
            paragraphs = listOf(
                "Yüzlerce demir gövde kilisenin kırık vitraylarından, çökmüş tavanından ve taş sütunlarının arasından içeri yağdı! Hava ozon, barut ve erimiş çelik kokuyordu. Ama bu çemberde duran kimse tek adım geri atmıyordu.",
                "Steve Rogers kalkanını sektirip üç nöbetçiyi aynı çizgide parçaladı; Natasha elektro-coplarıyla ikisini devre dışı bırakıp kalkanı havada Steve'e geri attı; Thor Mjolnir'le sütunları titreten şimşekler indirdi; Wanda iki elindeki kızıl kasırgayla kapıdan dalan bir manga dronu kâğıt gibi büktü; Pietro ise gümüş bir girdap gibi kilisenin etrafında dönüp nöbetçileri şaşkına çevirdi. Sen de zemine sağlam basan ayaklarınla bir sütundan diğerine mavi kuantum şimşekleriyle ışınlanıyor, gelen plazma ateşini emip Ultron'un ön saflarına iki kat güçle iade ediyordun!",
                "Yukarıdaki bulutların arasından Vision, Thor ve Tony'nin üçlü enerji ışını — **Zihin Taşı'nın altını, Mjolnir'in şimşeği ve Iron Man'in itici plazması** — birleşip Vibranyum Ultron Prime'ın göğsüne çarptı! Ultron'un gümüş zırhı kıpkırmızı kor kesilip çatladı ve dev makine kilisenin dış meydanına savruldu!"
            ),
            decisionTitle = "Bölüm XXI (1/4) — Ultron Geri Savrulduğunda",
            choicePrompt = "Ultron'un zırhı çatladı ama dış meydanda hâlâ yüzlerce sivil son Helicarrier botuna koşuyor. İlk hamlen ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch19_c1_boost_triple_beam",
                    label = "Mavi kuantum ışınını Vision, Thor ve Tony'nin birleşik ışınına ekleyip Ultron'un dış zırhını iyice zayıflat!",
                    toneHint = "Dörtlü Kozmik Işın • Thor, Tony & Vision Uyumu",
                    nextNodeId = "ch19_s2_evac_sprint",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VISION to 4, BookCharacterId.THOR to 3, BookCharacterId.TONY to 3),
                        setFlags = mapOf("ch19_beam_act" to "quad_beam"),
                        impactSummary = "Dörtlü ışın Ultron'un Vibranyum zırhının yarısını eritti — makine öfkeyle geri çekilip bir Quinjet'e atladı!"
                    )
                ),
                BookChoiceDef(
                    id = "ch19_c1_sweep_evac_lane",
                    label = "Dış meydana ışınlanıp son sivil konvoyunu koru — Clint ve Pietro'nun kurtarma botlarına geçiş yolunu tamamen temizle!",
                    toneHint = "Sivil Önceliği • Clint, Pietro & Steve Güveni",
                    nextNodeId = "ch19_s2_evac_sprint",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.CLINT to 4, BookCharacterId.PIETRO to 4, BookCharacterId.STEVE to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch19_beam_act" to "cleared_evac"),
                        impactSummary = "Son otobüs dolusu sivil güvenle Helicarrier botuna ulaştı — ama tam o anda gökyüzünden karanlık bir Quinjet sesi yükseldi!"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 2: TAHLİYENİN SON DAKİKASI (Karar 2/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch19_s2_evac_sprint",
            chapterNumber = 21,
            sceneOrder = 2,
            sceneTitle = "Son Kurtarma Botu",
            locationTag = "UÇAN SOKOVİA · BATI İSKELE MEYDANI • 07:53",
            paragraphs = listOf(
                "Steve telsizden bağırdı: \"Son kurtarma botu doluyor! Herkes iskeleye çekilsin — Wanda çekirdeği bir dakika daha tutacak!\" Ama tam son anne bota atlamışken, enkazın arkasında küçük oğlunun geride kaldığını görüp çığlık attı! Clint Barton tereddüt bile etmeden yayını sırtına atıp enkazın içine koştu ve küçük çocuğu kucağına aldı.",
                "Aynı saniyede gökyüzünde dumanların arasından Avengers'ın kendi **Quinjet**'i alçaldı — ama pilot koltuğunda Clint yoktu! Çatlamış gövdesiyle kontrolü ele geçiren **Ultron**, uçağın ağır makineli gatling toplarını doğrudan Clint'in, küçük çocuğun ve taş zeminin üzerine kilitledi!",
                "[[ALARM|ÖLÜMCÜL PUSU · İSKELE MEYDANI|Ultron kaçırdığı Quinjet'in ağır gatling toplarını ateşledi! Clint Barton ve küçük çocuk tamamen açık hedefte — mermiler 1.2 saniye içinde çarpacak!]]",
                "Sağ köşede, kırık bir arabanın yanında **Pietro Maximoff** durumu gördü — ve gözlerinde tek bir tereddüt bile olmadan gümüş bir şimşeğe dönüşüp kendini Clint ile çocuğun önüne atmaya fırladı! Aynı anda arkandaki kiliseden bir başka alarm patladı: son bir kamikaze Ultron dronu Wanda'nın arkasına sızıp Vibranyum çekirdek düğmesine dalışa geçti!"
            ),
            decisionTitle = "Bölüm XXI (2/4) — Saniyelerin Yarışı",
            choicePrompt = "İki felaket aynı saniyede patladı: Pietro kendini mermilerin önüne atıyor, arkanda ise bir dron çekirdek düğmesine dalıyor! Nasıl odaklanacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch19_c2_focus_time",
                    label = "Zaman yavaşlatmayı ve mavi ışınlanmayı maksimum sınıra zorla — gözlerin parlıyor, iki cepheyi de saniyelik zaman aralığında oku!",
                    toneHint = "Tam Odaklanma • Pietro'yu Kurtarma Fırsatını Açar",
                    nextNodeId = "ch19_s3_pietro_fate",
                    effect = BookChoiceEffect(
                        fatigueDelta = 1,
                        setFlags = mapOf("ch19_focus" to "time_limit"),
                        impactSummary = "Dünya ağır çekime düştü — her mermi havada parlayan bakır bir iğne gibi yavaşladı!"
                    )
                ),
                BookChoiceDef(
                    id = "ch19_c2_call_wanda",
                    label = "Wanda'ya telsizden haykırıp kiliseyi uyarırken kendini iskele meydanına fırlat!",
                    toneHint = "Ekip Koordinasyonu • Yüksek Gerilim",
                    nextNodeId = "ch19_s3_pietro_fate",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2, BookCharacterId.CLINT to 2),
                        setFlags = mapOf("ch19_focus" to "team_call"),
                        impactSummary = "Wanda sesini duyup kilise kapısına döndü — şimdi bütün kader önündeki yarım saniyeye sıkıştı!"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 3: 2. SEZON'UN EN BÜYÜK KARARI — PİETRO MAXİMOFF'UN KADERİ! (Karar 3/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch19_s3_pietro_fate",
            chapterNumber = 21,
            sceneOrder = 3,
            sceneTitle = "Bunu Görmedin mi? — Kader Anı",
            locationTag = "UÇAN SOKOVİA · DONMUŞ SANİYE • 07:54:02",
            paragraphs = listOf(
                "Zaman saniyenin onda birine indi. Havada yirmi dört adet yüksek kalibreli gatling mermisi, turuncu kıvılcım izleriyle doğrudan Clint Barton'a, kucağındaki küçük çocuğa — ve onların önüne dev bir beton levhayla koşan **Pietro Maximoff**'a doğru uçuyordu! Pietro bütün hızıyla Clint'i siperin arkasına itmişti ama kendisi tam mermi yağmurunun ortasında açıkta kalmıştı.",
                "Burnundan ince bir kan sızdı. Bu kadar uzun mesafeye art arda üç kez ışınlanıp tam gücüyle kinetik bir Drift Kalkanı açmak, kaburgalarındaki Vibranyum yarasını yeniden açacak ve seni ölümcül derecede tüketecekti. Üstelik arkadaki kilise çekirdeğine dalan dronu Wanda tek başına yakalayabilecek miydi, emin değildin. Bu, 2. Sezon'un bütün kaderini ve 3. Sezon'un kadrosunu değiştirecek tek seçimdi."
            ),
            decisionTitle = "Bölüm XXI (3/4) — 2. SEZON'UN EN BÜYÜK KARARI: PİETRO'NUN KADERİ",
            choicePrompt = "Mermiler Pietro'ya on santim kaldı! Bütün bedenini riske atıp Pietro'yu kurtaracak mısın, yoksa önce çekirdek dronunu durdurup şehri garantiye mi alacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch19_c3_pietro_save",
                    label = "BEDELİNİ ÖDE VE PİETRO'YU KURTAR! Üçlü ardışık ışınlanmayla tam Pietro'nun önüne atla, mavi Drift kalkanıyla bütün mermi yağmurunu yut!",
                    toneHint = "★ PİETRO HAYATTA KALIR! • Ağır Fiziksel Bedel • Wanda'nın Sonsuz Minneti",
                    nextNodeId = "ch19_s4_pietro_lived",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.PIETRO to 10, BookCharacterId.WANDA to 8, BookCharacterId.CLINT to 6, BookCharacterId.STEVE to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.PIETRO, CharacterFlagAxis.LOYALTY, 6),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 6)
                        ),
                        fatigueDelta = 2,
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 5),
                        setFlags = mapOf("s2_pietro_fate" to "saved"),
                        branchTag = "Gümüş Şimşeği Kurtardı",
                        secretUnlocked = "Değişen Kader: Pietro Yaşıyor",
                        impactSummary = "Zamanı ve ölümü yendin! Pietro Maximoff hayatta kaldı — 2. Sezon finalinde ve 3. Sezon'da seninle savaşacak!",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch19_c3_pietro_hesitate",
                    label = "Önce kilise çekirdeğine dalan kamikaze dronu ışınlanarak parçala — tek bir anlık hata bütün şehri düşürebilir, sonra hemen Pietro'ya dön!",
                    toneHint = "Şehir Önceliği • Trajik Canon Fedakârlığı • Pietro Kaybedilir",
                    nextNodeId = "ch19_s4_pietro_fallen",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.TONY to 2, BookCharacterId.WANDA to -2),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 4),
                        setFlags = mapOf("s2_pietro_fate" to "sacrificed"),
                        branchTag = "Gümüş Şimşeğin Vedası",
                        impactSummary = "Şehrin düşmesini önledin ama meydana yarım saniye geç vardın — Pietro, Clint ve çocuğu kurtarmak için kendini feda etti...",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BRANCH A: PIETRO LIVES! ────────────────────────────────────────────
        BookSceneNode(
            id = "ch19_s4_pietro_lived",
            chapterNumber = 21,
            sceneOrder = 4,
            sceneTitle = "Ölümü Geçen Mavi Kıvılcım — Pietro Yaşıyor!",
            locationTag = "UÇAN SOKOVİA · BATI İSKELE MEYDANI • 07:54:04",
            paragraphs = listOf(
                "Zamanı paramparça eden üç ardışık mavi kuantum patlamasıyla kırık asfaltı yaktın ve tam ilk merminin Pietro'nun göğsüne değeceği milisaniyede araya girdin! İki avucunu öne uzattın — vücudundaki bütün kalan enerjiyi tek bir kubbeye döküp dev bir **Mavi Drift Emilim Kalkanı** açtın! Yirmi dört ağır kalibreli mermi şok dalgalarıyla kalkanına saplandı, mavi ateşle eriyip havada buharlaştı!",
                "Darbenin basıncı seni dizlerinin üstüne düşürdü, iki burnundan da kan aktı — ama ne Clint, ne küçük çocuk, ne de **Pietro Maximoff** tek bir kurşun bile yemedi!",
                "Pietro nefes nefese göğsüne, sonra kanayan dudağınla yerde gülümseyen sana baktı; şaşkınlıktan neredeyse sesi çıkmıyordu: \"Sen... sen mermilerin önüne mi ışınlandın? Bunu... bunu gerçekten görmedim, mavi göz!\" Hemen koşup seni koltuk altından kavradı ve yıldırım hızıyla güvenli duvara çekti. Aynı anda kiliseden yükselen kızıl şimşekle Wanda kamikaze dronu kendi başına paramparça etti ve gözlerinde mutluluk gözyaşlarıyla ikinizin yanına koştu: \"İkiniz de hayattasınız!\"",
                "Yukarıda ise Hulk kükreyerek kaçırılan Quinjet'in kanadına sıçradı, kokpit camını parçalayıp **Vibranyum Ultron Prime**'ı doğrudan katedral kanadına fırlattı — ama o anda telsizden **Elias Voss**'un alarm dolu sesi çınladı!"
            ),
            decisionTitle = "Bölüm XXI (4/4) — Hayatta Kalan İkizlerle Son Bakış",
            choicePrompt = "Pietro hayatta, Wanda koluna sarılı, Clint çocuğu annesine verdi ama Ultron katedraldeki Voss ve Mira'ya doğru yöneldi! Pietro'ya ne diyeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch19_c4_banter_pietro",
                    label = "Kanayan dudağını silip gülümse: \"Sıradaki yarışı kazandım Pietro — sivilleri bota bindir, sonra katedralde Voss ve Mira'yı kurtaralım!\"",
                    toneHint = "Pietro Kardeşliği • İkonik Replik",
                    nextNodeId = "ch19_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.PIETRO to 4, BookCharacterId.WANDA to 3),
                        setFlags = mapOf("ch19_closing" to "pietro_banter"),
                        impactSummary = "Pietro kahkaha atıp omzuna vurdu: 'Seninle yeni üste köfte yemeden hiçbir yere gitmiyorum Aiden!'"
                    )
                )
            )
        ),

        // ── BRANCH B: PIETRO SACRIFICED (CANON TRAGEDY) ────────────────────────
        BookSceneNode(
            id = "ch19_s4_pietro_fallen",
            chapterNumber = 21,
            sceneOrder = 4,
            sceneTitle = "Gümüş Şimşeğin Son Sözü",
            locationTag = "UÇAN SOKOVİA · BATI İSKELE MEYDANI • 07:54:05",
            paragraphs = listOf(
                "Önce kiliseye ışınlanıp çekirdeğe dalan dronu tek darbeyle parçaladın — Sokovia düşmekten kurtuldu. Ama hemen ardından iskele meydanına ışınlandığında, dünyanın en ağır yarım saniyesini kaybetmiştin.",
                "Clint Barton ve küçük çocuk tek çizik almadan beton siperin arkasında hayattaydı. Ama önlerinde ayakta duran **Pietro Maximoff**, göğsünde kurşun izleriyle sendeledi, Clint'e dönüp solgun bir tebessümle o unutulmaz sözü fısıldadı: \"Bunu... görmedin mi?\" Ve yere yığıldı.",
                "Aynı saniyede kilisenin ortasında Wanda Maximoff dizlerinin üstüne çöküp yürekleri parçalayan bir çığlık attı! İki elinden yükselen devasa kızıl şok dalgası elli metrelik alandaki bütün Ultron nöbetçilerini bir saniyede toza çevirdi. Yukarıda Hulk, Ultron Prime'ı Quinjet'ten fırlatıp katedralin doğu ucuna indirdi — tam da **Voss ve Mira**'nın son sivilleri tahliye ettiği balkona!"
            ),
            decisionTitle = "Bölüm XXI (4/4) — Kırık Kalple İleri",
            choicePrompt = "Pietro'nun fedakârlığı bütün ekibin yüreğini yaktı. Şimdi Ultron katedralde Voss ve Mira'yı hedef alıyor — yürürken ne söz vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch19_c4_avenge_pietro",
                    label = "Pietro'nun gözlerini usulca kapat ve mavi kıvılcımını yak: \"Bir tek kişiyi daha kaybetmeyeceğim Pietro — Ultron bugün burada bitecek.\"",
                    toneHint = "Vakar ve Kararlılık • Wanda'ya Söz",
                    nextNodeId = "ch19_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4, BookCharacterId.CLINT to 4),
                        setFlags = mapOf("ch19_closing" to "pietro_avenged"),
                        impactSummary = "Clint gözyaşını silip Pietro'yu kurtarma botuna taşıdı; Wanda sana bakıp fısıldadı: 'Voss ve Mira'yı kurtar Aiden — ben hemen arkandayım.'"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch19_s5_end",
            chapterNumber = 21,
            sceneOrder = 5,
            sceneTitle = "Katedraldeki Alarm",
            locationTag = "UÇAN SOKOVİA · BATI İSKELE MEYDANI • 07:55",
            paragraphs = listOf(
                "Son kurtarma botu da bütün sivillerle birlikte Helicarrier'a yönelirken, katedralin doğu ucundan kırmızı plazma patlaması göğü yırttı. Ultron Prime yaralıydı, öfkeliydi — ve seni kalbinden vurmak için tam olarak senin koruduğun o iki insanı seçmişti: **Elias Voss ve Mira**."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Demir ne kadar sert olursa olsun, sevdiğin insanları koruma kararlılığı sonunda onu çatlatır.”"
        )
    ).associateBy { it.id }
}
