package com.example.book

/**
 * Extended, novel-length Chapters 6 and 7 of "Kozmik Sürükleniş" (Aiden Blackwood — MCU).
 * Rebuilt with long immersive prose, ONLY 2 monumental high-impact crossroads in Ch 6 and 3 in Ch 7,
 * deep cross-chapter flags/echoes, and 100% fidelity to Aiden Blackwood's canon.
 */
object CosmicDriftChapters6To7 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 6,
            romanNumeral = "VI",
            title = "Kırık Ayna",
            titleEn = "Broken Mirror",
            subtitle = "İstanbul Arşivi, Denek 7-A ve Kalmak Üzerine",
            description = "İstanbul sarnıçlarının altında Denek 7-A dosyasının tamamı, Boğaz kıyısında gerçek bir nefes ve yirmi yılın ardından verilen en büyük karar.",
            descriptionEn = "The full Subject 7-A file beneath Istanbul's cisterns, a quiet breath by the Bosphorus, and the biggest choice after twenty years.",
            estimatedMinutes = 9,
            accentHex = 0xFFC084FCL,
            startNodeId = "ch6_s1",
            sceneCount = 3,
            decisionCount = 2
        ),
        BookChapterMeta(
            number = 7,
            romanNumeral = "VII",
            title = "Karanlık Ayna",
            titleEn = "Dark Mirror",
            subtitle = "Dr. Evelyn Hale, İstanbul Tünelleri ve Şafak",
            description = "Dr. Evelyn Hale ile yüzleşme, İstanbul tünellerinde Mira ve Rezonans Kulesi finali, ve Boğaz'da yeni bir sayfa.",
            descriptionEn = "Confronting Dr. Evelyn Hale, the Istanbul tunnels finale against Mira and the Resonance Tower, and dawn over the Bosphorus.",
            estimatedMinutes = 12,
            accentHex = 0xFFF472B6L,
            startNodeId = "ch7_s1",
            sceneCount = 4,
            decisionCount = 3
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM VI: KIRIK AYNA (2 Büyük Dönüm Noktası)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch6_s1",
            chapterNumber = 6,
            sceneOrder = 1,
            sceneTitle = "Boğaz Rüzgârı ve Sarnıcın Altı",
            locationTag = "İSTANBUL • YEREBATAN ALTI GİZLİ ARŞİV • 22:30",
            paragraphs = listOf(
                "Quinjet sekiz saatlik okyanus geçişinin ardından gece yarısına doğru Karadeniz yönünden alçalırken, kokpit camının altında iki kıtayı birbirine bağlayan o uçsuz bucaksız altın ışık nehri göründü: İstanbul. Uçuş boyunca kabin çoğunlukla sessizdi. Steve Rogers yeraltı haritalarını ezberlemiş, Clint oklarının uçlarını kontrol etmiş, Wanda pencereden bulutları izlemişti. Natasha ise sadece iki kez yerinden kalkıp sana sıcak siyah çay uzatmış, tek kelime etmeden kendi koltuğuna dönmüştü.",
                "İstanbul'un gecesi New York'unkine hiç benzemiyordu. Boğaz'dan esen tuzlu, serin rüzgâr, yüzlerce yıllık taş sokaklardaki yağmur kokusuna, uzak camilerin kubbelerinden süzülen soluk sarı ışıklara, ıslak Arnavut kaldırımlarına ve Karaköy'ün dar bir ara sokağındaki küçük ocakbaşından yükselen kömürde ızgara köfte dumanına karışıyordu. Quinjet'i şehrin kuzey dışındaki gizli S.H.I.E.L.D. pistine indirdikten sonra kalabalıktan uzak, loş taş yokuştan yürürken Natasha Romanoff o tanıdık köfte kokusunu senden tam bir saniye önce fark etti.",
                "Durup omzunun üzerinden sana tek kelime etmeden baktı — dudağının kenarında sadece ikinizin anladığı o ince, sakin kıvrım vardı: *Önce iş, sonra yemek.* \"Gülümseme Romanoff,\" diye mırıldandın alçak sesle, \"o kömür ateşi New York'takinden daha tehlikeli.\" Yirmi yıldır dünyanın yarısını, hatta başka gezegenlerin mor gökyüzünü bile dolaşmıştın; ama iki kıtayı birbirine bağlayan bu kadim şehrin gecesinde, nefesini tutmanı gerektirmeyen tuhaf bir ağırlık ve gerçek bir sıcaklık vardı. Yoldan geçen yaşlı bir çaycı tepsisini taşırken seninle göz göze gelince bir saniyeliğine duraksayıp gözünü kaçırdı — çarpıcı mavi gözlerin bu şehirde de aynı sessiz, karşı konulmaz etkiyi bırakıyordu.",
                "Ama yarım saat sonra, gecenin sessizliğine gömülmüş eski bir Bizans sarnıcının üçüncü sütununun ardındaki gizli çelik kapıyı açıp yeraltına indiğinizde şehrin bütün sıcaklığı yukarıda kaldı. Merdivenlerden otuz metre aşağı indikçe hava keskinleşti; taş duvarların arkasında yüzlerce yıllık su sızıntısının sesi modern bir jeneratörün alçak uğultusuna karıştı. Sarnıcın altındaki bu gizli arşiv karanlık, nemli ve tuhaf biçimde canlıydı. Yüksek taş kemerlerin arasında paslı borulardan damlayan soğuk sular yankılanıyor, karanlık su birikintilerinin yüzeyinde duvarlara gömülü mavi laboratuvar ekranlarının yansıması titriyordu. Kalın kurşun cam panellerin arkasında yirmi yıl öncesine ait deney kayıtları, beyin dalgası haritaları ve Sonsuzluk Taşı frekans şemaları sıralanmıştı. Odanın tam ortasındaki ana sunucuda ise tek bir dosya açıktı: **PROJE AYNA — DENEK 7-A: AIDEN BLACKWOOD.**",
                "Steve Rogers ve Clint Barton üst girişteki taş kemerleri güvene alırken sen ana sunucuya yaklaştın. Elini yaklaştırdığın an biyometrik ekran kanındaki kozmik frekansı tanıdı ve yirmi yıldır kilitli duran eski güvenlik kamerası görüntüleri ile ses kayıtları kendiliğinden oynamaya başladı. Tarih ekranın köşesinde buz gibi parladı: **14 KASIM 2006 — NEW YORK — 23:41.**",
                "Ekranda beş yaşındaki halini gördün. Yangın merdiveninin soğuk demirine küçük parmaklarınla tutunuyorsun; gökyüzü o korkunç, canlı maviyle yarılıyor ve sen annenin elini bırakıyorsun. Ama yirmi yıldır sadece kendi bulanık hafızandan bildiğin o saniyenin öncesi ve hemen sonrası ilk kez karşına çıktı: apartmanın iki sokak altındaki gizli sığınakta bir kozmik enerji çekirdeği aşırı yüklenmiş, ölümcül radyasyon dalgası doğrudan senin durduğun kata yönelmişti. Annenle baban kaçmamıştı. Baban o saniyede bir kontrol panelini kilitleyip ölümcül dalganın en yıkıcı kısmını annenle birlikte kendi üzerlerine çekmiş, yalnızca saf ve zararsız mavi enerjinin sana geçmesini sağlamışlardı. Babanın son saniyede yüzündeki o sakin maske korkudan değil, oğlunu kurtardığını bilmenin sarsılmaz kararlılığındandı.",
                "Wanda Maximoff yanına yaklaştı, elini yavaşça soğuk cam panele yaklaştırdı; gözlerinin kenarında ince, kederli bir kızıl gölge belirdi. \"Burada hâlâ eski bir zihin ve enerji yankısı var Aiden,\" diye fısıldadı, sesi taş kemerlerde yankılandı. \"Annenle baban... o gece seni korumak için bütün yıkıcı akımı kendi üzerlerine çekmişler. Sen onları kaybetmedin. Elini bıraktığın için ölmediler. Onlar seni bilerek, sevgiyle kurtardı.\"",
                "Kelimeler göğsünün tam ortasına ağır, sıcak bir dalga gibi indi. Yirmi yıldır her uykusuz gecede, her yeni ucuz otel odasında, her kaçışta sırtında sessizce taşıdığın o zehirli suçluluk — *neden annemin elini bıraktım?* sorusu — ilk kez ortadan ikiye çatladı ve yerini derin, yakıcı ama arındırıcı bir berraklığa bıraktı. Steve geride sessizce başını eğdi; Natasha ise tek kelime etmeden çevreyi taramaya devam ederek sana nefes alacak, kimsenin acımadığı o onurlu alanı bıraktı.",
                "Tam o anda arşivin derinliklerinden alarm çalmadan soğuk, keskin, sinir bozucu bir enerji sinyali yükseldi. Ana sunucudaki bütün dosyalar bir anda kırmızı bir geri sayımla silinmeye başladı: **04:00.** Suyun altından ağır, kemikleri titreten bir sarsıntı geçti; yüksek taş kemerlerin ucundaki karanlık tünelde kör edici kızıl bir parıltı yanıp söndü. **Mira** bu tünellerdeydi, geldiğinizi biliyordu ve hem arşivi hem de bütün sarnıcı dört dakika içinde havaya uçurmak üzereydi."
            ),
            decisionTitle = "İstanbul Arşivi — Kiminle ve Nasıl İlerleyeceksin?",
            choicePrompt = "Arşiv dakikalar içinde silinecek. Bu operasyonda hem gerçeği hem de yanındakileri nasıl yöneteceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch6_c1_natasha",
                    label = "Natasha ile omuz omuza arşiv çekirdeğine in — sessiz, ölümcül bir koordinasyonla veriyi kurtar.",
                    toneHint = "Natasha • Tam Güven • Gölge Operasyonu",
                    nextNodeId = "ch6_s2_natasha",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.LOYALTY, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHADOW_ALLIANCE to 3,
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 3
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch6_partner" to "natasha"),
                        secretUnlocked = "Annenin Son Ses Kaydı",
                        branchTag = "Natasha İle Sarnıç",
                        impactSummary = "Natasha ile kusursuz bir sessizlikte arşivi kurtardınız; annenle babanın o geceye ait son kaydını buldun.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c1_wanda",
                    label = "Wanda ile zihinsel rezonans kurup arşivin enerji kilidini aç — Mira'nın bıraktığı izi birlikte oku.",
                    toneHint = "Wanda • Zihinsel Köprü • Kozmik Derinlik",
                    nextNodeId = "ch6_s2_wanda",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4, BookCharacterId.BRUCE to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.LOYALTY, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.PSYCHIC_RESONANCE to 4,
                            StoryFlagAxis.COSMIC_AWAKENING to 2
                        ),
                        mysteryDelta = 2,
                        setFlags = mapOf("ch6_partner" to "wanda"),
                        secretUnlocked = "Mira'nın Zihin İzi",
                        branchTag = "Wanda İle Rezonans",
                        impactSummary = "Wanda ile mavi ve kızıl enerjiyi birleştirip arşivi kurtardınız; Mira'nın acısını ve planını zihninden çektiniz.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c1_solo",
                    label = "İkisini de geride bırak — zamanı büküp arşive tek başına gir, bu geçmişle yalnız yüzleş.",
                    toneHint = "Yalnız Kurt • Bağımsız • Ağır Bedel",
                    nextNodeId = "ch6_s2_solo",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 2, BookCharacterId.NATASHA to 1, BookCharacterId.WANDA to 1),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 4,
                            StoryFlagAxis.MENTAL_FATIGUE to 2
                        ),
                        fatigueDelta = 2,
                        setFlags = mapOf("ch6_partner" to "solo"),
                        secretUnlocked = "Denek 7-A Tam Dosya",
                        branchTag = "Yalnız Yüzleşme",
                        impactSummary = "Kimseyi riske atmadan arşive tek başına girdin; dosyayı kurtardın ama yorgunluk seni neredeyse yere seriyordu.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM VI — DAL 6A/6B/6C → YİRMİ YILDAN SONRA İLK İSTEK ─────────────
        BookSceneNode(
            id = "ch6_s2_natasha",
            chapterNumber = 6,
            sceneOrder = 2,
            sceneTitle = "Boğaz Kıyısında Sessiz Gerçek",
            locationTag = "İSTANBUL • BOĞAZ KIYISI • 02:00",
            paragraphs = listOf(
                "Sarnıçtaki o dört dakika, tek bir fazladan kelime bile gerektirmeyen kusursuz bir gölge operasyonuna dönüştü. Sen geri sayım patlayıcılarının zaman akışını üçer saniyelik nefeslerle askıya alırken Natasha Romanoff karanlık suyun üstünden akrobatik bir hızla geçip iki kızılötesi lazer tuzağını saniyeler içinde devre dışı bıraktı ve ana veri çekirdeğini şifreli belleğe kopyaladı.",
                "Geri sayım sıfıra inerken tünelin ucundan Mira'nın son anda fırlattığı yakıcı, dengesiz kırmızı şok dalgası taş kemerleri parçalayarak üzerinize geldi. Önüne geçip iki elinle berrak bir mavi kozmik kalkan açtın; patlamanın bütün basıncını avuçlarında emip sönümledin ve ikiniz birlikte taş merdivenlerden tek çizik almadan İstanbul gecesine çıktınız.",
                "Dışarı çıktığınızda yağmur tamamen dinmişti. Boğaz'ın kara, derin sularında Asya yakasının soluk sarı ışıkları yavaşça titriyordu; uzaktan geçen dev bir yük gemisinin boğuk düdüğü geceyi ikiye yardı. Avucunun içinde annenle babanın 2006 gecesine ait son ses kaydı ve tam arşiv belleği duruyordu. Az önce kulaklıkta dinlediğin o kırk saniyelik kayıtta, annenin son sözleri hâlâ göğsünde yankılanıyordu: *Yaşa Aiden. Sadece yaşa, sakın arkana bakma.*",
                "Artık neden hayatta olduğunu, o mavi gecenin bir lanet ya da senin hatan değil, uğruna ödenmiş bir sevgi olduğunu biliyordun. Steve, Wanda ve Clint birkaç yüz metre gerideki güvenli evde Dr. Hale'in izlerini ve Rezonans Kulesi'nin haritasını inceliyordu; sana bu gece yarım saatlik bir nefes alanı bırakmışlardı.",
                "Natasha iki adım yanında, denizden nemlenmiş taş korkuluğa yaslanmıştı. Boğaz'ın soğuk rüzgârı siyah ceketinin yakasını ve kızıl saçlarını hafifçe savuruyordu. Senden tek bir kelime beklemiyordu; her zamanki gibi sessizliğine saygı duyuyor, duvarlarını zorlamıyor, seni sıradan teselli cümleleriyle boğmaya çalışmıyordu. O, gölgelerde büyümüş birinin başka bir gölgeye verebileceği en nadir, en değerli şeyi veriyordu: baskısız, güvenli bir sessizlik.",
                "Beş dakika boyunca sadece suyun taşlara vurmasını dinlediniz. Uzaktaki bir köprüden geçen arabanın farları suyun üstünde soluk bir iz bıraktı. Aklına beş yaşındaki o mavi gece geldi, ardından on dokuz yıl boyunca uykusuz geçirdiğin tren garları, sahte pasaport kuyrukları, Prag'ın mor kumlu yabancı gökyüzü... Ve şimdi burada, Boğaz'ın soğuk rüzgârında ilk kez nefesini tutmadan duruyordun.",
                "\"Annemle babam beni kurtarmış,\" dedin sonunda, sesin rüzgârda neredeyse bir fısıltı gibiydi. \"Yirmi yıldır elini bıraktığım için öldüklerini sandım. Her gece o saniyeyi tekrar tekrar yaşadım.\"",
                "Natasha bir saniye denize baktı, sonra yavaşça sana döndü. Çarpıcı mavi gözlerinle karşılaştığında bakışları her zamanki gibi sabit, sakin ve dürüsttü. \"Kırmızı Oda'da bana yıllarca hayatta kalmanın suç olduğunu öğrettiler Aiden,\" dedi alçak bir sesle. \"Hayatta kalmak suç değil. Onlar senin yaşamanı istedi — sadece nefes almanı değil, gerçekten yaşamanı.\"",
                "Bir an durdu, rüzgârda ceketinin yakasını düzeltti. \"Yirmi yıldır tek bir amacın vardı: iz bırakmamak ve kaçmak. Şimdi bütün gerçeği biliyorsun. Yarın gece Mira ve Dr. Hale bittiğinde, bütün o kovalamaca kapandığında... gerçekten ne istiyorsun?\""
            ),
            decisionTitle = "Yirmi Yıldan Sonra İlk İstek",
            choicePrompt = "İlk kez kaçmak zorunda olmadığın bir gelecek ihtimali önünde duruyor. Kalbin ve zihnin neyi seçiyor?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch6_c2_stay_and_step",
                    label = "\"Artık kaçmak istemiyorum. Kule'de kalacağım — ve bu kez gerçekten birine yer açacağım.\"",
                    toneHint = "Kalış • Bilinçli Duygusal Adım • Büyük Değişim",
                    nextNodeId = "ch6_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.STEVE to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TOWER_BELONGING to 4,
                            StoryFlagAxis.HUMANITY_ANCHOR to 4
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch6_desire" to "stay_bond"),
                        branchTag = "Kalmayı Seçmek",
                        impactSummary = "Aiden Blackwood hayatında ilk kez kendi iradesiyle bir adım attı. Natasha bakışını kaçırmadan yanına biraz daha yaklaştı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c2_finish_first",
                    label = "\"Önce bu işi bitireceğim. Dr. Hale ve Mira bitmeden hiçbir şeye söz vermem.\"",
                    toneHint = "Odaklı • Mesafeli • Görev Öncelikli",
                    nextNodeId = "ch6_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 2, BookCharacterId.STEVE to 2),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 3, StoryFlagAxis.INDEPENDENCE to 2),
                        setFlags = mapOf("ch6_desire" to "mission_first"),
                        branchTag = "Önce Hesaplaşma",
                        impactSummary = "Duygularını erteleyip gözünü tamamen final hesaplaşmasına diktin; Natasha bunu kırgınlık duymadan kabul etti.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch6_s2_wanda",
            chapterNumber = 6,
            sceneOrder = 2,
            sceneTitle = "İki Kırık Ayna, Tek Sessizlik",
            locationTag = "İSTANBUL • BOĞAZ KIYISI • 02:00",
            paragraphs = listOf(
                "Sarnıcın en derin kemerinin altında, mavi kozmik enerjin ile Wanda Maximoff'un kızıl kaos gücü ilk kez tek bir akışta birleştiğinde tuhaf, yıllardır hiç yaşamadığın bir şey oldu: güç seni tüketmedi, şakaklarını yakmadı, nefesini kesmedi. İki farklı evren dalgası birbirini dengeledi. Arşivin kendi kendini imha eden enerji kilidini saniyeler içinde açtınız, annenle babanın son kaydını kurtardınız ve Mira'nın tünelin derinliklerinde bıraktığı o yakıcı zihin izini — yıllarca cam duvarların arkasında birikmiş öfkesini, yalnızlığını ve Boğaz'ın altındaki Rezonans Kulesi'nin tam koordinatlarını — tek bir ortak dokunuşla okudunuz.",
                "Sarnıçtan yerüstüne çıktığınızda yağmur tamamen dinmişti. Boğaz'ın soğuk, karanlık suları taş rıhtıma usulca vuruyor, uzaktan geçen bir gece vapurunun ışıkları suda altın şeritler halinde dağılıyordu. Avucunun içinde annenle babanın kaydı duruyordu — yirmi yıl boyunca taşıdığın o suçluluk nihayet yerini dürüst bir vedaya bırakmıştı.",
                "Wanda iki adım yanında duruyordu. Ellerini koyu ceketinin ceplerine sokmuş, Boğaz rüzgârında karşı kıyıya bakıyordu. Ne seni konuşturmaya çalışıyor ne de izinsiz zihnine dokunuyordu. İkiniz arasındaki o nadir zihinsel köprü yalnızca senin izin verdiğin kadar, senin açtığın kapı kadar vardı.",
                "Uzun, rahatlatıcı bir sessizliğin ardından \"Annemle babam beni kurtarmış,\" dedin alçak sesle. \"Yirmi yıldır elini bıraktığım için öldüklerini sanıyordum. O suçluluğu her şehirde, her boyutta sırtımda taşıdım.\"",
                "Wanda başını hafifçe sana çevirdi. Gözlerinde ne acıma ne de bir beklenti vardı — yalnızca kendi kayıplarını yaşamış birinin duru, derin anlayışı. \"Ben de Sokovia'da ailemi kaybettiğim ve ikizim Pietro'dan koptuğum gece aynı şeyi düşündüm Aiden,\" diye fısıldadı. \"Bazen hayatta kalan kişi en ağır yükü taşır. Ama onlar bizim suçlulukla gölgelerde saklanmamız için gitmediler.\"",
                "Bir saniye Boğaz'ın karanlık sularına baktı, sonra tekrar sana döndü. \"Bütün hayatın boyunca senden korkan insanlardan, cam duvarlardan ve uydulardan kaçtın. Şimdi aileni, geçmişini ve neden hayatta olduğunu biliyorsun. Yarın gece bu savaş bittiğinde... gerçekten ne istiyorsun?\""
            ),
            decisionTitle = "Yirmi Yıldan Sonra İlk İstek",
            choicePrompt = "Wanda soruyu sordu ve sessizce bekliyor. Hamle tamamen sende.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch6_c2_stay_and_step",
                    label = "\"Kaçmayı bırakıyorum. Kule'de kalacağım — ve seninle bu sessizliği paylaşmak istiyorum.\"",
                    toneHint = "Wanda • Bilinçli Duygusal Adım • Derin Bağ",
                    nextNodeId = "ch6_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.TRUST, 3)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TOWER_BELONGING to 4, StoryFlagAxis.HUMANITY_ANCHOR to 4),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch6_desire" to "stay_bond"),
                        branchTag = "Wanda İle Kalış",
                        impactSummary = "Aiden Blackwood kendi isteğiyle bir adım attı. Wanda'nın gözlerinde ilk kez saf, kırılgan bir mutluluk parladı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c2_finish_first",
                    label = "\"Önce Mira ve Dr. Hale bitecek. Ondan sonrasını şafakta konuşuruz.\"",
                    toneHint = "Odaklı • Mesafeli • Görev Öncelikli",
                    nextNodeId = "ch6_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2, BookCharacterId.STEVE to 2),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 3, StoryFlagAxis.INDEPENDENCE to 2),
                        setFlags = mapOf("ch6_desire" to "mission_first"),
                        branchTag = "Önce Hesaplaşma",
                        impactSummary = "Duygularını erteleyip odağını finale çevirdin.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch6_s2_solo",
            chapterNumber = 6,
            sceneOrder = 2,
            sceneTitle = "Tek Başına Taşınan Ağırlık",
            locationTag = "İSTANBUL • BOĞAZ KIYISI • 02:00",
            paragraphs = listOf(
                "Kimseyi kendi geçmişinin ateşine sürüklemedin. Ekibe kapıda kalmalarını söyledin, derin bir nefes aldın ve çevrendeki zamanı dört tam saniyeliğine dondurup sarnıcın çökmek üzere olan ana arşiv odasına tek başına daldın. Kızılötesi lazer tuzaklarını berrak bir mavi enerji kalkanıyla eritip susturdun; Denek 7-A tam dosyasını, Rezonans Kulesi haritasını ve annenle babanın o geceye ait son kaydını çekirdek patlamadan tam yarım saniye önce çekip aldın.",
                "Sarnıcın dışına ışınlandığında dizlerin ıslak Arnavut taşlarının üstünde hafifçe titriyordu, burnundan ince bir çizgi halinde sıcak kan sızmıştı — hızlı iyileşmen hemen devreye girse de dört saniyelik tam zaman dondurma ve acil sıçrama bedeni ağır vurmuştu. Ama bütün gerçek elindeydi.",
                "Boğaz'ın tuzlu, serin gece havasını ciğerlerine çekerken Steve Rogers, Natasha Romanoff ve Wanda Maximoff birkaç adım geride, taş korkuluğun başında sessizce bekliyordu. Hiçbiri seni acele ettirmedi, hiçbiri neden tek başına girdiğini sorgulamadı, hiçbiri omuzlarına dokunmaya kalkmadı. Yirmi yıllık yalnızlık alışkanlığı hâlâ sert bir zırh gibi omuzlarındaydı; ama hayatında ilk kez, arkana dönüp baktığında seni avlamak için değil, sessizce sırtını tutmak için orada duran insanlar vardı. Ve ilk kez kendine dürüst bir soru sormak zorundaydın: yarın gece bu savaş bittiğinde gerçekten ne yapacaksın?"
            ),
            decisionTitle = "Yirmi Yıldan Sonra İlk İstek",
            choicePrompt = "Finalden önceki son sakin gecede kendine dürüst ol: bu savaş bittiğinde ne yapacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch6_c2_stay_and_step",
                    label = "Kule'de kalmaya karar ver — artık kaçak değil, kendi yerini seçen biri ol.",
                    toneHint = "Aidiyet • Yeni Sayfa",
                    nextNodeId = "ch6_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.NATASHA to 2, BookCharacterId.WANDA to 2),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TOWER_BELONGING to 4, StoryFlagAxis.HUMANITY_ANCHOR to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch6_desire" to "stay_bond"),
                        branchTag = "Kalmayı Seçmek",
                        impactSummary = "İçinde ilk kez bir yere ait olma fikrine yer açtın.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c2_finish_first",
                    label = "Bağımsız kal — bu işi bitir, ama hiçbir yere zincirlenme.",
                    toneHint = "Özgür • Yalnız Kurt",
                    nextNodeId = "ch6_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 1),
                        storyAxisDeltas = mapOf(StoryFlagAxis.INDEPENDENCE to 4),
                        setFlags = mapOf("ch6_desire" to "mission_first"),
                        branchTag = "Özgür Yol",
                        impactSummary = "Özgürlüğünü her şeyin üstünde tutmaya devam ettin.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch6_s3_end",
            chapterNumber = 6,
            sceneOrder = 3,
            sceneTitle = "Kapıdaki Ziyaretçi",
            locationTag = "İSTANBUL GÜVENLİ EV • 04:15",
            paragraphs = listOf(
                "Şafaktan iki saat önce, Boğaz'ın dar yamaçlarına bakan o küçük, eski taş güvenli evde zaman ağır ilerliyordu. Masanın üzerinde sarnıçtan çıkardığınız haritalar, enerji grafikleri ve Tony Stark'ın New York'tan canlı olarak güncellediği yeraltı tünel kesitleri açıktı. Steve Rogers elindeki sıcak kahveyle haritaya eğilmişti, Clint Barton pencere kenarından karanlık sokağı gözlüyordu, Wanda ve Natasha ise sessizce son mühimmat ve frekans kontrollerini yapıyordu.",
                "Sen pencerenin hemen önündeki ahşap sandalyede oturup sarnıçtan kurtardığın o eski 2006 ses kaydını ikinci kez dinledin.",
                "[[AUDIO|14 KASIM 2006 — ANNE & BABA SON SES KAYDI (00:42)|\"Aiden, sakın korkma oğlum. Sadece gözlerini kapat ve nefes al.\" — [Mavi yarık uğultusu, cam kırılması] — \"Yaşa Aiden. Sadece yaşa, sakın arkana bakma.\"]]",
                "Kayıt bittiğinde kulaklığı masaya koydun. Yirmi yıldır ilk kez o geceyi düşündüğünde göğsünde buz değil, acı ama temiz bir gurur vardı.",
                "Saat tam 04:15'i gösterdiğinde dış ahşap kapıda iki kısa, ölçülü ve hiç acele etmeyen vuruş duyuldu. *Tak. Tak.* Odadaki bütün hareket tek bir saniyede dondu. Steve refleksle kalkanına uzandı, Clint yayını sessizce kaldırdı, Natasha bir gölge gibi kapının yanındaki kör noktaya geçti. Ama kapıyı kendi elinle açtığında eşikte silahlı bir S.H.I.E.L.D. timi ya da Konsey suikastçısı yoktu.",
                "Soğuk gece havasının içinde tek bir kişi duruyordu: saçlarına kır düşmüş, omuzları yılların yüküyle çökmüş, üzerinde yağmurdan ıslanmış eski bir trençkot olan altmışlı yaşlarında bir kadın. Tamamen silahsızdı; iki elini de açıkça görünür tutuyordu. Gözlerinin altında yirmi yıllık uykusuzluğun ve ağır bir vicdan azabının silinmez gölgesi vardı. Fotoğrafını 19 numaralı rıhtımdaki yanık dosyalarda görmüştün: **Dr. Evelyn Hale.**",
                "\"Beni burada, bu eşikte öldürmeden önce üç dakika dinle Aiden,\" dedi kadın. Sesi soğuk Boğaz rüzgârında ince bir cam gibi çatlıyordu; senin çarpıcı mavi gözlerine bir saniyeden fazla bakamadı, bakışını yere indirdi. \"Kaçmadım. Koruma getirmedim. Kendim geldim. Çünkü Mira on dakika önce Boğaz'ın altındaki ana Rezonans Kulesi'ni çalıştırdı. Eğer şafak sökmeden durdurulmazsa, İstanbul'un üzerinde 2006'da New York'ta açılan o mavi yarığın on katı büyüklüğünde bir kozmik yırtık açılacak. Onu ve o kuleyi durdurabilecek tek kişi sensin.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch6_desire",
                    requiredFlagValue = "stay_bond",
                    callbackLabel = "İlk Kez Bir Gelecek",
                    paragraphs = listOf(
                        "Boğaz kıyısında yıllardır ilk kez kendi isteğinle bir adım atmış, kaçmayı bırakıp kalmaya karar vermiştin. Şimdi kapının eşiğinde duran geçmişin mimarına bakarken, savunacak gerçek bir geleceğin olduğunu ilk kez kemiklerinde hissediyordun."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch6_desire",
                    requiredFlagValue = "mission_first",
                    callbackLabel = "Önce Hesaplaşma",
                    paragraphs = listOf(
                        "Gelecek, güven ya da kalmak — hepsini şafaktan sonraya ertelemiştin. Şimdi yirmi yıllık hesaplaşmanın son perdesi tam kapının eşiğinde duruyordu."
                    )
                )
            ),
            isChapterEnd = true,
            chapterEndQuote = "Kırık bir aynaya baktığında seni korkutan cam değil, içindeki kendi yüzündür."
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM VII: KARANLIK AYNA (3 Büyük Dönüm Noktası — Büyük Final)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch7_s1",
            chapterNumber = 7,
            sceneOrder = 1,
            sceneTitle = "Dr. Evelyn Hale ve Savaş Masası",
            locationTag = "İSTANBUL GÜVENLİ EV • 04:45",
            paragraphs = listOf(
                "Dr. Evelyn Hale, taş güvenli evin uzun ahşap masasının diğer ucunda elleri hafifçe titreyerek oturuyordu. Beş yaşındayken New York gökyüzünün maviye yarıldığı, annenin elini bıraktığın ve bütün çocukluğunu bir saniyede kaybettiğin o gecenin baş mimarı tam karşındaydı. Odadaki hava ağır, soğuk ve elektrikliydi; kimse kadına su ya da kahve ikram etmemişti. Clint duvar kenarında kollarını kavuşturmuştu, Natasha ise kadının her mikro ifadesini, her nefesini soğuk bir dikkatle okuyordu.",
                "\"Bana sadece tek bir sorunun cevabını ver,\" dedin, sesin o kadar alçak ama o kadar buz gibiydi ki masadaki cam bardak bile ince bir titreşimle yankılandı. \"O gece apartmanın altındaki çekirdeği kapatsaydın, annemle babam yaşar mıydı?\"",
                "Dr. Hale'in omuzları çöktü. Gözlerinden yaş akmadı — sanki yirmi yılda ağlayacak hiçbir şeyi kalmamış gibiydi. \"Hayır Aiden,\" diye fısıldadı, doğrudan senin çarpıcı mavi gözlerine bakmaya cesaret edemeyip bakışını kendi titreyen ellerine indirdi. \"Çekirdek kritik eşiği üç dakika önce geçmişti. Kapatmaya çalıştık, yemin ederim çalıştık. Ama çok geçti. Pişmanım — bunun annenle babanı geri getirmeyeceğini, çalınmış yirmi yılını telafi etmeyeceğini biliyorum.\"",
                "\"2006'da Dünya Güvenlik Konseyi'nin gölge komitesi — özellikle de üstümüzdeki gizli yönetici — uzaydan yakalanan kadim bir kozmik çekirdekten savunma kalkanı yapmamızı emretti,\" diye devam etti Hale, sesi titreyerek. \"Bize bunun bütün gezegeni koruyacağını söylediler. Biz de kendi kibrimize inandık. Ama o gece çekirdek laboratuvardan kaçırılıp senin mahallende kontrolden çıktı; bana kaza dediler, üstünü örttüler. Annenle babanın neden orada olduğunu Konsey'in tepesindekilerden başka kimse bilmiyordu. Sen o saf mavi enerjiyi parçalanmadan taşıyabilen tek çocuktun. Konsey seni hemen bir silaha dönüştürmek istedi; ben ise dosyayı karartıp kaçmana göz yumduğumu sanarak yıllarca kendimi kandırdım.\"",
                "Derin, kesik bir nefes alıp trençkotunun iç cebinden eski, şifreli bir titanyum kontrol anahtarı çıkarıp masanın ortasına koydu. \"Elias Voss acısını sessizliğe ve karanlığa gömdü. Sen yirmi yıl kaçtın ama elindeki evren düzeyindeki güce rağmen insanlığını hiç kaybetmedin. **Mira** — Denek 8-C — ise o şansa hiç sahip olmadı. Yıllarca Konsey'in kalın cam hücrelerinde 'canavar' diye damgalandı, her gün acı eşiği test edildi. Şimdi Boğaz'ın altındaki eski metro ve Bizans sarnıçlarının birleştiği devasa mağarada Proje Ayna'nın ana Rezonans Kulesi'ni ateşledi. Şehir uyanmadan o yarığı açıp onu kafese kapatan dünyayı kendi mavi ateşiyle yakmak istiyor.\"",
                "\"Neden doğrudan S.H.I.E.L.D.'a gitmedin?\" diye sordu Steve Rogers sert bir sesle. Hale acı bir şekilde başını iki yana salladı: \"Çünkü Konsey'deki bazı adamlar o kulenin patlamasını ya da Mira'nın öldürülmesini istiyor Yüzbaşı. Bütün kanıtları tek gecede temizlemek için.\"",
                "Tam o anda masanın ortasında Tony Stark'ın New York'tan bağlanan parlak mavi hologramı açıldı. Üç boyutlu yeraltı tünel haritasında kırk metrelik kırmızı bir enerji çekirdeği nabız gibi atıyordu. \"Dr. Hale'in getirdiği anahtar verilerini doğruladım,\" dedi Tony; sesinde o alışıldık alaycılıktan tek bir eser bile yoktu, tamamen ciddiydi. \"Rezonans Kulesi tam otuz dakika içinde kritik eşiğe ulaşacak. İçerideki kozmik enerji emilimi korkunç düzeyde — benim en yeni Mark zırhım bile çekirdeğe yüz metreden fazla yaklaşsa bütün devreleri anında erir. İki seçeneğimiz var: ya tünelleri dışarıdan yüksek patlayıcılarla vurup mağarayı çökerteceğiz — ki Mira içeride kesin olarak ölür ve Boğaz'da büyük bir sarsıntı riski doğar — ya da o enerji fırtınasının içine yürüyüp ayakta kalabilecek tek kişi içeri girip doğrudan Mira ile yüzleşecek.\"",
                "Odadaki bütün gözler yavaşça sana döndü. Steve Rogers, Natasha Romanoff, Wanda Maximoff, Clint Barton ve hologramın arkasındaki Tony Stark. Çarpıcı mavi gözlerin masanın soğuk mavi ışığında parladı; bu kez odadaki hiç kimse gözünü kaçırmaya bile yeltenemedi. Yirmi yıl önce bir yangın merdiveninde başlayan bu hikâyeyi bitirecek olan kişi sendin — ama o tünellere kiminle ve nasıl gireceğine yalnızca sen karar verecektin."
            ),
            decisionTitle = "Dr. Hale ve Operasyon Planı",
            choicePrompt = "Otuz dakikan var. Hem Dr. Hale'e hem de İstanbul tünellerine nasıl yaklaşacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch7_c1_team_plan",
                    label = "Dr. Hale'in verisini al ve tüm ekiple koordineli gir — Steve ve Natasha çevreyi tutarken Wanda ile merkeze in.",
                    toneHint = "Ekip Birliği • Tam Güven • Stratejik",
                    nextNodeId = "ch7_s2_tunnels",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 3, BookCharacterId.WANDA to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.LOYALTY, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.AVENGERS_TRUST to 4, StoryFlagAxis.TACTICAL_DISCIPLINE to 3),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch7_plan" to "team"),
                        branchTag = "Avengers İle Final",
                        impactSummary = "İlk kez gerçek bir Avenger gibi ekiple omuz omuza tünellere indin.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c1_solo_plan",
                    label = "Ekibe tünelin ağzında kalmasını söyle — Mira'nın karşısına tek başına çık, kimseyi kendi geçmişinin ateşine sürükleme.",
                    toneHint = "Tek Kişilik Final • Koruyucu • Ağır Yük",
                    nextNodeId = "ch7_s2_tunnels",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 2, BookCharacterId.WANDA to 2, BookCharacterId.TONY to 2),
                        storyAxisDeltas = mapOf(StoryFlagAxis.INDEPENDENCE to 4, StoryFlagAxis.COSMIC_AWAKENING to 3),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch7_plan" to "solo"),
                        branchTag = "Yalnız Final",
                        impactSummary = "Sevdiklerini ve ekibi ateş hattının dışında bırakıp tünellere tek başına yürüdün.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM VII — SAHNE 2: KARANLIK AYNA: MİRA ────────────────────────────
        BookSceneNode(
            id = "ch7_s2_tunnels",
            chapterNumber = 7,
            sceneOrder = 2,
            sceneTitle = "Karanlık Ayna: Mira",
            locationTag = "BOĞAZ ALTI REZONANS KULESİ • 05:20",
            paragraphs = listOf(
                "Boğaz'ın soğuk sularının altındaki antik taş tünellerin ve kullanılmayan eski metro hatlarının birleştiği en derin mağaraya inmek bile başlı başına bir sınavdı. Yüzlerce metre aşağı indikçe duvarlardaki paslı borular enerji fırtınasının basıncıyla inliyor, kulaklık hattı zaman zaman statik cızırtılarla kopuyordu. Ayağının altındaki su birikintileri yukarı doğru süzülen küçük kürelere dönüşmüştü — yerçekimi ve fizik kuralları burada artık tamamen çökmüştü.",
                "Devasa kemerli mağaranın tam ortasında kırk metrelik çelik, titanyum ve parlayan kristalden oluşan **Rezonans Kulesi** yükseliyor, etrafında kör edici bir mavi ve koyu kızıl kozmik fırtına dönüyordu. Havada asılı kalan dev kaya parçaları, bükülmüş eski tren rayları, kopmuş çelik halatlar ve çatlayan beton sütunlar ağır çekimde kulenin çevresinde ölümcül bir yörüngeye girmişti.",
                "[[ANIM|BOSPHORUS_RESONANCE|BOĞAZ ALTI · REZONANS KULESİ|Kırk metrelik çelik çekirdeğin çevresinde mavi kozmik ışık ve Mira'nın kızıl ateşi çarpışıyordu.]]",
                "Havanın içindeki o soğuk bakır tadı artık nefesini kesen bir elektrik fırtınasına dönüşmüştü. Yukarıdaki kalın kaya tabakasının çatlaklarından Boğaz'ın tuzlu suları ince şelaleler halinde aşağı dökülüyor, ama yere düşmeden kulenin yaydığı ısıyla beyaz buhara dönüşüyordu.",
                "Ve fırtınanın tam merkezinde, yerden beş metre yüksekte havada süzülen genç bir kadın duruyordu: **Mira — Denek 8-C.** Yüzü bembeyazdı, uzun siyah saçları statik elektrikle havaya kalkmıştı ve gözleri senin o berrak, çarpıcı mavi ışığının kontrolden çıkmış, yakıcı bir gölgesi gibi saf kızıl bir ateşle parlıyordu. Çevrende berrak mavi bir kozmik enerji kalkanı açtın ve uçuşan kaya parçalarını kenara iterek fırtınanın kalbine doğru yürüdün.",
                "\"Sonunda geldin, 7-A,\" diye haykırdı Mira. Sesi enerji fırtınasının kulak sağır edici uğultusunu bastırıp mağaranın her duvarında yankılandı. \"Seni dosyalardan hep kıskandım biliyor musun? *Dengeli denek. Kusursuz uyum. Kaçmayı başaran mucize çocuk.* Bize hep **canavar** dediler Aiden! Çocukluğumuzdan beri kalın camların arkasından baktılar, kanımızı ölçtüler, ailelerimizi elimizden aldılar, sonra da kafese kapatmak için peşimize silahlı timler saldılar! Sen neden hâlâ onların tarafındasın? Onların cam kulesinde uyuyup onların emirleriyle mi yaşıyorsun?\"",
                "\"Kimsenin emriyle yaşamıyorum Mira,\" dedin, sesin fırtınanın ortasında şaşırtıcı derecede sakin, derin ve net çıktı. İki adım daha yaklaştın, avuçlarını açık tutarak. \"O kelimeyi bana da söylediler. Dokuz yaşındaydım. Sağanak yağmurda burnumdan kan akarken telsizden duydum. Yirmi yıl boyunca kimseye sırtımı dönmedim, kimseye güvenmedim. Ama şu an bu kule patlarsa yukarıda uyuyan milyonlarca masum insan — hiçbir deneyden haberi olmayan çocuklar, aileler — ölecek. Bizi yaratanlar değil. Eğer bunu yaparsan, onların bize taktığı o ismi kendi ellerinle gerçeğe çevirmiş olacaksın.\"",
                "Mira'nın gözlerinden iki kör edici kızıl şimşek fırlayıp senin mavi kalkanına çarptı. Darbe seni iki adım geriye kaydırdı, botların taş zeminde kıvılcım çıkardı ama kalkanın tek bir çatlak bile vermedi. \"Çok geç!\" diye haykırdı Mira, sesinde o yakıcı öfkenin hemen altında kırık, derin, neredeyse çocuksu bir çaresizlik vardı. \"Ben durduramıyorum Aiden — çekirdek zaten çatladı, enerjim kuleye kilitlendi! Çekilirsem beni yakıyor, bırakırsam şehri yakacak! Bırak kule patlasın! Bu dünya da bizim yandığımız gibi yansın!\"",
                "Rezonans Kulesi'nin ana çekirdeği kör edici, kulakları sağır eden bir mavi-kızıl ışıkla çatlamaya başlamıştı. İki dakikan bile yoktu. Yukarıdaki Boğaz'ın suları tavan çatlaklarından artık kalın akıntılar halinde dökülüyordu. İçindeki kozmik güç, yirmi yıldır ilk kez bütün sınırlarını zorlayarak damarlarında uğulduyordu — ama bedenin bu yükü uzun süre kaldıramazdı.",
                "Önünde iki yol vardı: ya Beyaz Göz'ü öfkeyle değil şefkatle açıp Rezonans Kulesi'nin bütün yıkıcı enerjisini kendi göğsünde emerek Mira'yı kurtaracaktın — kendi bedenini sonsuzluğun sınırına kadar yakma pahasına — ya da zamanı tamamen dondurup Rezonans Kulesi'ni acımasız bir taktik hassasiyetle parçalayarak Mira'nın gücünü mühürleyip şehri sıfır riskle kurtaracaktın."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch7_plan",
                    requiredFlagValue = "team",
                    callbackLabel = "Avengers Omuz Omuza",
                    paragraphs = listOf(
                        "Tünelin girişinde Steve Rogers vibranyum kalkanıyla düşen dev kaya parçalarını karşılıyor, Clint Barton şok oklarıyla çöken kabloları kesiyor, Natasha Romanoff son otomatik savunma kulelerini susturuyor ve Wanda Maximoff iki elinden yükselen güçlü kızıl kalkanla tünel tavanının çökmesini engelliyordu. Kulaklıktan Natasha'nın baskı altında her zamankinden daha sakin çıkan sesi geldi: \"Sırtın bizde Aiden. Kimse arkandan gelmeyecek. Bitir şu işi.\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_plan",
                    requiredFlagValue = "solo",
                    callbackLabel = "Yalnız Yüzleşme",
                    paragraphs = listOf(
                        "Ekibi tünelin güvenli ağzında bırakmıştın — bu senin beş yaşında başlayan geçmişindi ve kimsenin senin ateşinde yanmasına izin vermeyecektin. Mağaranın içinde sadece sen, Mira ve evrenin kurallarını bükebilecek iki kırık güç karşı karşıyaydı."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch5_voss_fate",
                    requiredFlagValue = "protected",
                    callbackLabel = "Elias Voss'un Anahtarı",
                    paragraphs = listOf(
                        "Kule'de Raft kafesinden koruduğun Elias Voss'un gitmeden önce verdiği o küçük frekans notu tam şu saniyede hayat kurtardı: Mira'nın kalkanındaki tek zayıf nokta — sol çekirdek bağlantı hattı — fırtınanın ortasında gözlerinin önünde kristal gibi netleşmişti."
                    )
                )
            ),
            decisionTitle = "Mira ve Rezonans Kulesi — Büyük Final Kararı",
            choicePrompt = "İstanbul'un ve Mira'nın kaderi senin ellerinde. Bu savaşı nasıl bitireceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch7_c2_save_mira",
                    label = "Beyaz Göz'ü şefkatle aç ve tüm kule enerjisini kendi bedeninde emerek Mira'yı kurtar — \"Canavar değiliz.\"",
                    toneHint = "Kurtuluş • En Yüksek İrade • Kozmik Fedakârlık",
                    nextNodeId = "ch7_s3_dawn",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.MIRA to 8, BookCharacterId.WANDA to 5, BookCharacterId.NATASHA to 4, BookCharacterId.STEVE to 5),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.MIRA, CharacterFlagAxis.TRUST, 5),
                            Triple(BookCharacterId.MIRA, CharacterFlagAxis.AFFECTION, 4),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.LOYALTY, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.HUMANITY_ANCHOR to 5,
                            StoryFlagAxis.WHITE_EYE_CONTROL to 4,
                            StoryFlagAxis.AVENGERS_TRUST to 5
                        ),
                        fatigueDelta = 2,
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch7_finale_act" to "saved_mira"),
                        branchTag = "Mira Kurtarıldı",
                        impactSummary = "Evren düzeyindeki enerjiyi kendi göğsünde sönümleyip Mira'yı ölümden ve öfkesinden kurtardın. Kule sessizce söndü.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c2_destroy_tower",
                    label = "Önce zamanı dondurup Rezonans Kulesi'ni parçalara ayır — Mira'nın gücünü mühürleyip şehri kesin güvenceye al.",
                    toneHint = "Taktik • Kesin Çözüm • Sıfır Risk",
                    nextNodeId = "ch7_s3_dawn",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 4, BookCharacterId.STEVE to 3, BookCharacterId.NATASHA to 3, BookCharacterId.MIRA to -1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.MIRA, CharacterFlagAxis.RESPECT, 2),
                            Triple(BookCharacterId.MIRA, CharacterFlagAxis.FEAR, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 4,
                            StoryFlagAxis.AVENGERS_TRUST to 4
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch7_finale_act" to "destroyed_tower"),
                        branchTag = "Kesin Zafer",
                        impactSummary = "Zamanı dondurup kuleyi saniyeler içinde etkisiz hale getirdin; şehir tek bir cam bile kırılmadan kurtuldu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM VII — SAHNE 3: BOĞAZ'DA ŞAFAK VE SON ADIM ────────────────────
        BookSceneNode(
            id = "ch7_s3_dawn",
            chapterNumber = 7,
            sceneOrder = 3,
            sceneTitle = "Boğaz'da Şafak",
            locationTag = "İSTANBUL • BOĞAZ KIYISI • 06:30",
            paragraphs = listOf(
                "Güneş Asya yakasının tepelerinden ağır ağır yükselirken İstanbul Boğazı'nın soğuk suları erimiş altına boyanmıştı. Martıların keskin ama huzurlu sesleri sabahın duru havasında yankılanıyor, karşı kıyıdaki ilk şehir hatları vapurları uyanmaya başlayan iki kıtanın arasını beyaz köpüklerle yarıyordu. Şehrin yedi milyon sakini hiçbir şeyden habersiz sıcak çaylarını koyuyor, perdelerini açıyor, sıradan bir güne başlıyordu — az önce ayaklarının kırk metre altında bütün bir kıtanın kaderinin yazıldığını hiç bilmeden.",
                "Yeraltındaki Rezonans Kulesi artık tamamen sessizdi — ama kapanırken gökyüzüne fırlattığı son mavi enerji dalgası atmosferi delip derin uzaya doğru sessiz bir işaret fişeği gibi yayılıp kaybolmuştu; o sabah kimse bu sinyali evrenin öte ucunda kimlerin duyduğunu henüz bilmiyordu. Dr. Evelyn Hale hiçbir direniş göstermeden kendi isteğiyle Nick Fury'nin güvenli adalet birimine teslim olmuş, Proje Ayna'nın bilinen laboratuvarları Tony Stark ile Fury'nin ortak hamlesiyle mühürlenmişti. Yirmi yıldır peşini bırakmayan o bitmek bilmeyen S.H.I.E.L.D. kovalamacası nihayet durmuştu.",
                "Hızlı iyileşmen şakaklarındaki, ellerindeki ve omuzlarındaki bütün çizikleri çoktan kapatmıştı ama göğsünde uzun, yirmi yıllık bir maratonun ardından gelen tatlı, derin bir yorgunluk vardı. Ceketinin yakasını indirdin, sabah güneşinin yüzündeki soğukluğu alışını hissettin.",
                "Az ilerideki küçük sahil lokantasının taş fırınından ve açık mangalından sabahın ilk demli çay, sıcak tırnak pide ve kömürde pişen ızgara köfte kokusu rüzgârla kıyıya kadar ulaşıyordu. Kıtalar arası ışınlanabilen, zamanı yavaşlatabilen bir adam olarak, iki yüz yıl yaşasan bile bu kokunun seni gülümsetmekten asla vazgeçmeyeceğini biliyordun.",
                "Rıhtımın hemen ucunda, sabah güneşine karşı Natasha Romanoff duruyordu. Kollarını kavuşturmuş, rüzgâr kızıl saçlarını savururken denize bakıyordu; her zamanki gibi ne seni çağırıyor, ne bir adım bekliyor, ne de acele ettiriyordu. Birkaç adım ötede Wanda Maximoff, parmaklarının arasındaki son ince kızıl kıvılcımı Boğaz rüzgârına bırakıp gözlerini kapattı ve uzun, huzurlu bir nefes aldı. Daha geride Steve Rogers, Tony Stark'ın gönderdiği ikmal hattını kapatan Clint Barton ve tıbbi çantasını toplayan Bruce Banner, Quinjet'in açık rampasının yanında sessizce senin kararını bekliyordu.",
                "Kimse senden bir söz, bir yemin, bir bağlılık ya da bir adım talep etmiyordu. Yirmi beş yaşındaydın — ve hayatında ilk kez kaçmak için değil, yaşamak için bir yön seçecektin. Kimin yanında duracağına, nereye döneceğine yalnızca sen karar veriyordun."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch7_finale_act",
                    requiredFlagValue = "saved_mira",
                    callbackLabel = "Mira'nın Kurtuluşu — Kozmik Fedakârlık",
                    paragraphs = listOf(
                        "Tünelde Beyaz Göz'ünü öfkeyle ya da kontrol tutkusuyla değil, saf bir şefkatle açıp Rezonans Kulesi'nin bütün yıkıcı enerjisini kendi göğsünde emip sönümlediğin o sonsuz üç saniye, mağaranın tamamını kör edici bir mavi huzurla doldurmuştu. Göğsün yanarken bile bırakmamıştın — ve sonunda Mira'nın gözlerindeki o çılgın kızıl ateş ilk kez söndü, dizlerinin üzerine çöküp ağlamaya başladı.",
                        "Şu an Quinjet'in tıbbi bölmesinde Bruce Banner ve Wanda'nın gözetiminde güvenle uyuyordu. Bir canavar değil, yirmi yıllık bir karanlıktan kurtarılmış bir insandı. Steve Rogers tünelden çıkarken omzuna koyduğu elin sıcaklığı hâlâ üzerindeydi."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_finale_act",
                    requiredFlagValue = "destroyed_tower",
                    callbackLabel = "Kusursuz Müdahale — Kesin Zafer",
                    paragraphs = listOf(
                        "Zamanı beş tam saniyeliğine mutlak bir sessizliğe dondurup Rezonans Kulesi'nin kırk metrelik çelik çekirdeğini milimetrik enerji darbeleriyle parçalara ayırdığında, yukarıdaki milyonlarca insan tek bir sarsıntı bile hissetmeden uyumaya devam etmişti. Mira'nın kontrolden çıkan gücü güvenli bir kozmik kalkanla mühürlenip nötralize edilmiş, şehir tek bir pencere camı bile kırılmadan kurtulmuştu.",
                        "Tony Stark hattan canlı enerji grafiğini gördüğünde uzun bir sessizliğin ardından tek cümle kurmuştu: \"Beş saniyede kıyameti iptal etti. Artık resmi olarak Avengers'ın en tehlikeli ve en disiplinli adamı sensin Blackwood.\""
                    )
                )
            ),
            decisionTitle = "Yeni Sayfanın İlk Adımı",
            choicePrompt = "Şafak söktü. Aiden Blackwood bundan sonra hangi yolu ve kimin yanını seçecek?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch7_c3_natasha_home",
                    label = "Natasha'nın yanına yürü — \"Kule'ye dönüyoruz. Ama önce şu köşede kahvaltı borcun var.\"",
                    toneHint = "Natasha Yolu • Sessiz & Derin Bağ • Avengers",
                    nextNodeId = "ch7_s4_finale",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 5, BookCharacterId.STEVE to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 4),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.LOYALTY, 4)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TOWER_BELONGING to 5, StoryFlagAxis.HUMANITY_ANCHOR to 4),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch7_ending" to "natasha_home"),
                        branchTag = "Natasha İle Şafak",
                        impactSummary = "Aiden adımını attı. Natasha yıllardır ilk kez gerçekten güldü ve birlikte şafağa yürüdünüz.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c3_wanda_home",
                    label = "Wanda'nın yanına yürü — \"Artık iki farklı güç değiliz. Kule'de birlikte kalalım.\"",
                    toneHint = "Wanda Yolu • Zihinsel & Kalbi Bağ • Avengers",
                    nextNodeId = "ch7_s4_finale",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 5, BookCharacterId.STEVE to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 4),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.LOYALTY, 4)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TOWER_BELONGING to 5, StoryFlagAxis.PSYCHIC_RESONANCE to 4),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch7_ending" to "wanda_home"),
                        branchTag = "Wanda İle Şafak",
                        impactSummary = "Aiden adımını attı. Wanda elini uzattığında zihinlerinizde ilk kez tam bir huzur yankılandı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c3_lone_horizon",
                    label = "Ekibe başınla selam ver ama kalbini kimsede bırakma — \"İhtiyacınız olduğunda buradayım. Ama yolum hâlâ benim.\"",
                    toneHint = "Bağımsız Final • Özgür Gezgin • Mesafeli",
                    nextNodeId = "ch7_s4_finale",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 2, BookCharacterId.WANDA to 2, BookCharacterId.STEVE to 2),
                        storyAxisDeltas = mapOf(StoryFlagAxis.INDEPENDENCE to 5, StoryFlagAxis.MYSTERY_AURA to 3),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch7_ending" to "lone_horizon"),
                        branchTag = "Özgür Ufuk",
                        impactSummary = "Av bitti, müttefiklerin var — ama Aiden Blackwood kimseye zincirlenmeden kendi ufkuyla kaldı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM VII — BÜYÜK FİNAL ─────────────────────────────────────────────
        BookSceneNode(
            id = "ch7_s4_finale",
            chapterNumber = 7,
            sceneOrder = 4,
            sceneTitle = "Yıldızların Altında Yeni Bir İsim",
            locationTag = "BOĞAZ • YENİ BAŞLANGIÇ",
            paragraphs = listOf(
                "Beş yaşındayken New York semalarında açılan mavi bir ışığın içinde annesini, babasını ve bütün çocukluğunu kaybeden o küçük çocuk artık kaçmıyordu. Yirmi yıl boyunca kıtalar, yabancı gezegenler ve kırık zaman çizgileri arasında sırtını hep duvarlara yaslamış, her girdiği odada çıkışları saymış, kimseye güvenmemiş ve hiçbir kapının kilidini açık bırakmamıştı.",
                "Ama o sabah İstanbul Boğazı'nın üzerindeki berrak, altın renkli gökyüzüne baktığında ağzında ne o yirmi yıllık soğuk bakır tadı vardı, ne de ensesinde S.H.I.E.L.D. uydularının görünmez elektrik iğneleri. Annenle babanın seni kurtarmak için ödediği o bedel nihayet boşa gitmemişti — hayatta kalmakla yetinmemiş, yaşamayı seçmiştin.",
                "Artık ne dosyalardaki bir deney numarasıydın, ne kapatılması gereken kozmik bir anomali, ne de birilerinin korktuğu için 'canavar' diye damgaladığı bir gölge. Kendi sınırlarını, kendi gücünü ve kendi sessizliğini bilen Aiden Blackwood'dun. Ve önünde hayatında ilk kez, kaçarak değil bilinçle seçerek yazacağın açık bir ufuk uzanıyordu."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch7_ending",
                    requiredFlagValue = "natasha_home",
                    callbackLabel = "Gölgeden Şafağa — Natasha Romanoff",
                    paragraphs = listOf(
                        "Natasha Romanoff'un yanına yürüyüp o cümleyi kurduğunda iki saniye boyunca Boğaz rüzgârından başka hiçbir ses çıkmadı. Sonra Natasha sana döndü; çarpıcı mavi gözlerine her zamanki gibi tek milimetre bile kaçırmadan baktı ve yıllardır neredeyse hiç kimsenin görmediği kadar gerçek, dingin ve sıcak bir gülümseme yüzüne yayıldı.",
                        "\"Kömür ateşinde ızgara köfte, sıcak pide ve demli çay,\" dedi alçak, keyifli bir sesle. \"Kabul Aiden. Ama hesabı Stark'ın sınırsız kartından ödüyoruz — ve dönüşte Quinjet'i ben kullanıyorum.\"",
                        "Sahil yolundaki küçük lokantaya doğru yan yana yürürken omzu hafifçe omzuna değdi. Zorlamadan, acele etmeden, senden olmadığın biri olmanı beklemeden — tam da Aiden Blackwood'un dünyasına yakışan o nadir, sakin ve gerektiğinde bütün dünyayı karşısına alabilecek kadar sarsılmaz dengeyle. Aiden ilk kez kendi iradesiyle bir adım atmıştı ve bu kez yanında duran gölge hiç gitmeyecekti."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_ending",
                    requiredFlagValue = "wanda_home",
                    callbackLabel = "Mavi ve Kızıl — Wanda Maximoff",
                    paragraphs = listOf(
                        "Wanda Maximoff'un yanına yürüdüğünde zihnine izinsiz dokunmadı; sadece başını kaldırıp doğrudan senin çarpıcı mavi gözlerine baktı ve parmaklarını yavaşça senin avucuna bıraktı. O saniyede ne senin berrak mavi kozmik enerjin ne de onun kızıl kaos gücü birbiriyle çarpıştı — ikisi ilk kez aynı sakin, huzurlu ritimde nefes aldı.",
                        "\"Artık cam duvarların arkasında değiliz Aiden,\" diye fısıldadı Wanda, gözleri Boğaz'ın sabah güneşinde parlayarak. \"Ve artık ikimiz de bu ağırlığı tek başımıza taşımak zorunda değiliz.\"",
                        "Quinjet'e doğru el ele yürürken Steve'in yüzünde sessiz bir gülümseme belirdi. Yirmi yılın ardından ilk kez bir kuleye dönmenin sadece geçici bir sığınak değil, gerçekten kendine ait bir ev olabileceğini hissettin."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_ending",
                    requiredFlagValue = "lone_horizon",
                    callbackLabel = "Özgür Ufuk — Yalnız Gezgin",
                    paragraphs = listOf(
                        "Steve Rogers başını derin bir saygıyla eğdi, Clint Barton \"Yine de o Türk lokantasının adresini bırak\" diye seslendi, Tony ise hattan \"İletişim bilekliğini açık tut yeter, dünyanın başı yine derde girecek\" dedi. Natasha tek bir kırgınlık gölgesi olmadan gözlerinin içine bakıp hafifçe başını eğdi; Wanda ise sabah rüzgârında sessiz, anlayışlı bir tebessümle elini kaldırdı. Kimse seni durdurmaya ya da kalmaya zorlamaya çalışmadı.",
                        "Av bitmişti. Arkanda seni anlayan ve sırtını kollayacak gerçek müttefiklerin vardı — ama Aiden Blackwood kimseye zincirlenmeden, bu kez korktuğu ya da kaçtığı için değil tamamen özgürce seçtiği için güneşin doğduğu altın ufka doğru yürüdü."
                    )
                )
            ),
            isChapterEnd = true,
            chapterEndQuote = "Artık kaçak bir anomali değil; kendi kaderini yazan Aiden Blackwood'sun."
        )
    ).associateBy { it.id }
}
