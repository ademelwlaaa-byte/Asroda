package com.example.book

/**
 * Chapters 4 and 5 of "Kozmik Sürükleniş" (Aiden Blackwood — MCU Evreni).
 * Extended novel-length Turkish literary prose with rich cross-chapter hidden flags,
 * Memory Echo callbacks, Red Eye instability & Avengers Trust tracking, and true branching.
 *
 * Chapter 4: İlk Görev (Manhattan Limanı HYDRA operasyonu, pusu eşiği & 3 ayrı gizli keşif dalı)
 * Chapter 5: Kırmızı Buz (Queens hangarı, Elias Voss yüzleşmesi, 3 ayrı sorgu odası yolu & gece hesaplaşması)
 */
object CosmicDriftChapters4To5 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 4,
            romanNumeral = "IV",
            title = "İlk Görev",
            titleEn = "First Mission",
            subtitle = "Manhattan Limanı, HYDRA Deposu ve Sovyet İmzası",
            description = "Fury'nin acil görev çağrısı, ilk resmi saha operasyonu, pusu anında ekip uyumu ve liman deposunun derininde geçmişe açılan beklenmedik keşif.",
            descriptionEn = "Fury's mission call, Aiden's first official field operation, and a startling discovery at Manhattan Harbor.",
            estimatedMinutes = 16,
            accentHex = 0xFFFBBF24L,
            startNodeId = "ch4_s1",
            sceneCount = 6,
            decisionCount = 5
        ),
        BookChapterMeta(
            number = 5,
            romanNumeral = "V",
            title = "Kırmızı Buz",
            titleEn = "Red Ice",
            subtitle = "Kırmızı Koruyucu, 'Kaç' Uyarısı ve Sorgu Odası",
            description = "Karanlıktan çıkan ayna: Elias Voss kimdir, seni neden yirmi yıldır arıyor ve sorgu odasında hangi gerçek saklı?",
            descriptionEn = "A mirror from the past: who is Elias Voss, why did he hunt you, and what truth lies inside the interrogation room?",
            estimatedMinutes = 17,
            accentHex = 0xFFF87171L,
            startNodeId = "ch5_s1",
            sceneCount = 6,
            decisionCount = 4
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 4: İLK GÖREV
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch4_s1",
            chapterNumber = 4,
            sceneOrder = 1,
            sceneTitle = "Fury'nin Hologramı",
            locationTag = "AVENGERS KULESİ • TAKTİK KOMUTA ODASI • 01:40",
            paragraphs = listOf(
                "Taktik komuta odasının cam masası gecenin ortasında soğuk bir mavi ışıkla canlandığında, Kule'nin seksen sekizinci katındaki sessizlik bir bıçak gibi kesildi. Masanın merkezinde beliren holografik silüet uzun siyah paltosu ve tek gözündeki sarsılmaz soğukkanlılığıyla Nick Fury'ydi. Gözünü kırpmadan doğrudan sana baktı.",
                "\"Manhattan Limanı, 19 Numaralı Rıhtım,\" dedi hiç vakit kaybetmeden; sesi metalik aktarımın içinden bile odadaki oksijeni ağırlaştırıyordu. \"Eski HYDRA kalıntıları sabaha karşı üç ağır konteyner indirdi. İçerideki enerji çekirdeği, Stark'ın uydularına göre senin biyometrik dalganla yüzde altmış sekiz oranında örtüşüyor.\"",
                "Odadaki hava bir anda buz kesti. Yüzde altmış sekiz. Yirmi yıldır bu gezegende —ve tesadüfen savrulduğun başka hiçbir yerde— senin frekansına yaklaşan tek bir kayıt bile olmamıştı. *Yüzde altmış sekiz tesadüf değildir. Yüzde altmış sekiz bir imzadır.*",
                "Tony tabletinden başını kaldırmadan frekans grafiğini büyüttü; iki mavi dalga neredeyse aynı ritimde nefes alıyordu. \"Ya birisi senin eski izlerini kopyaladı,\" diye mırıldandı, \"ya da bu şehirde seninle aynı kaynaktan gelen bir şey uyanıyor.\"",
                "Steve Rogers sana doğru yarım adım döndü. Sesinde emir değil, gerçek bir seçenek bırakmanın ağırlığı vardı: \"Zorunlu değilsin, Aiden. Deneme süren daha yeni bitti. İstersen Kule'de kalabilirsin, biz ineriz.\" Ama Natasha'nın kollarını kavuşturmuş duruşu, senin zaten kapının eşiğinde kararını vermekte olduğunu biliyordu. Fury tek kaşını kaldırdı: \"Ne diyorsun, Blackwood?\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "has_stark_band",
                    requiredFlagValue = "accepted",
                    callbackLabel = "Stark Kuantum Bilekliği",
                    paragraphs = listOf("Bileğindeki mat titanyum halka, Fury'nin hologramındaki frekans grafiği ekrana yansıdığı saniye hafifçe ısındı ve ince mavi bir çizgiyle titreşti. Tony kaşlarını kaldırdı: \"Gördün mü? Bileklik daha limana gitmeden içerideki çekirdeği tanıdı. O cihazı kabul etmen işimizi yarı yarıya kolaylaştırdı.\"")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch3_locket",
                    requiredFlagValue = "faced_past",
                    callbackLabel = "Mavi Gecenin Koordinatı",
                    paragraphs = listOf("Göğsündeki soğuk metal madalyonu bir an elinle yokladın. İçine kazılı o beş haneli koordinat hâlâ aklındaydı — ve Fury'nin haritada işaretlediği 19 Numaralı Rıhtım, o koordinatın denizle birleştiği eski nakliye hattının tam karşısındaydı.")
                )
            ),
            decisionTitle = "Fury'nin Göreve Çağrısına Cevap",
            choicePrompt = "İlk resmi saha operasyonu teklifine nasıl yanıt vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch4_c1_immediate",
                    label = "Tek kelimeyle, tereddütsüz yanıtla: \"Varım.\"",
                    toneHint = "Kararlı • Hızlı",
                    nextNodeId = "ch4_s2_harbor",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 8, BookCharacterId.NATASHA to 6, BookCharacterId.CLINT to 5),
                        avengersTrustDelta = 7,
                        setFlags = mapOf("ch4_accept" to "immediate"),
                        impactSummary = "Steve şaşırdı ama gururla başını salladı; Natasha 'İlk kez tereddütsüz varım diyorsun' diye fısıldadı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c1_conditional",
                    label = "\"Giderim ama kendi kurallarımla — içeride benim imzam varsa son sözü ben söylerim.\"",
                    toneHint = "Şartlı • Otoriter",
                    nextNodeId = "ch4_s2_harbor",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 7, BookCharacterId.CLINT to 4, BookCharacterId.STEVE to 2),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch4_accept" to "conditional"),
                        impactSummary = "Fury tek omzunu silkti: 'Sahada nefes aldığın sürece kuralları konuşuruz.' Clint gülerek 'Bu adam diplomat olmalıymış' dedi.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c1_intel",
                    label = "Önce somut istihbarat iste: \"Kaç silahlı var, alıcı kim ve deniz tarafı çıkışları nerede?\"",
                    toneHint = "Taktik • Analitik",
                    nextNodeId = "ch4_s2_harbor",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 8, BookCharacterId.STEVE to 6, BookCharacterId.TONY to 4),
                        avengersTrustDelta = 6,
                        mysteryDelta = 1,
                        secretUnlocked = "Rıhtım 19 Alıcı Kaydı",
                        setFlags = mapOf("ch4_accept" to "tactical"),
                        impactSummary = "Soğukkanlı saha soruların Natasha ve Steve'den tam not aldı; Fury gizli alıcı frekansını ekranına açtı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch4_s2_harbor",
            chapterNumber = 4,
            sceneOrder = 2,
            sceneTitle = "Sis Altında 19 Numaralı Rıhtım",
            locationTag = "MANHATTAN LİMANI • 03:20",
            paragraphs = listOf(
                "Tuzlu okyanus rüzgârı ince, iğne gibi bir yağmurla karışıp yüzüne vuruyordu. 19 Numaralı Rıhtım'ın paslı konteynerleri karanlık bir satranç tahtası gibi üst üste dizilmişti; uzaktan gelen vinç zincirlerinin rüzgârda çıkardığı metalik gıcırtı dışında liman tekinsiz bir sessizliğe gömülüydü.",
                "Kulaklığında Clint Barton'ın yüksek liman vincinin tepesinden verdiği keskin nişancı raporu yankılandı: \"İçeride on sekiz silahlı hedef. İki devriye kuzey kapısında, ikisi yüksek geçitte keskin nişancı pozisyonunda. Ana depoda ise şebekeyi sömüren yüksek bir enerji çekişi var — Bruce'un sensörleri de tavana vurdu.\"",
                "Steve kalkanını sırtından alıp bileğine sabitledi. Natasha eldivenlerindeki mavi şok disklerinin şarjını kontrol etti; yağmur damlaları kızıl saçlarının ucundan çenesine süzülüyordu. Bir saniyeliğine durup etrafına baktın. Yirmi yıl boyunca gecenin içinde hep tek başınaydın — arkanı kollayan kimse yoktu, kulaklığında adını söyleyen bir ses yoktu.",
                "*İlk kez bir kaçak değil, bir ekibin parçası olarak karanlığa bakıyorsun.* Göğsündeki o tuhaf ağırlık korku değildi; bir başkasının hayatını da omuzlarında taşımanın soğuk, keskin sorumluluğuydu.",
                "\"Giriş düzeni sende, Blackwood,\" dedi Steve alçak sesle, yağmurun gürültüsünü delen bir netlikle. \"Hızını ve menzilini en iyi sen bilirsin. Nereden giriyoruz?\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch3_alarm_branch",
                    requiredFlagValue = "stayed",
                    callbackLabel = "Kule'de Kalışın Yankısı · Clint'in Şakası",
                    paragraphs = listOf("Kulaklıktan Clint'in hafif nefesli sesi geldi: \"S.H.I.E.L.D. alarmında kaçmayıp kaldığın gün sana güvendim, Blackwood. Şimdi o ok menzilinde sırtın tamamen bende.\" Karanlığın içinde istemsizce dudaklarının kenarı kıvrıldı.")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch3_sparring",
                    requiredFlagValue = "red_eye_surge",
                    callbackLabel = "Kırmızı Göz Hatırlatması",
                    paragraphs = listOf("Natasha yanına yaklaşıp yalnızca senin duyacağın kadar alçak bir tonda konuştu: \"İçeride ne bulursak bulalım, gözlerin kırmızıya kaydığını hissettiğin an bana işaret ver. Seni orada tek başına bırakmam.\"")
                )
            ),
            decisionTitle = "Depo Operasyonunda Rol",
            choicePrompt = "Ana depoya sızarken hangi taktik rolü üstleneceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch4_c2_scout",
                    label = "Öncü keşif — ilk sen ışınlanıp gölge koridorlarını sessizce temizle ve kör noktaları aç.",
                    toneHint = "Yalnız Öncü • Hızlı",
                    nextNodeId = "ch4_s2b_ambush",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.NATASHA to 5),
                        fatigueDelta = 1,
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch4_role" to "scout"),
                        impactSummary = "Yirmi yıldır alıştığın gibi önden yürüdün — ama kulaklıktan ekibin nefesini duymak o eski yalnızlığı kırdı.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c2_sync",
                    label = "Natasha ile eş zamanlı giriş — yan yana, birbirinizin kör açısını kapatarak ilerle.",
                    toneHint = "Senkron • Natasha",
                    nextNodeId = "ch4_s2b_ambush",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 11, BookCharacterId.STEVE to 5),
                        avengersTrustDelta = 6,
                        setFlags = mapOf("ch4_role" to "sync_natasha"),
                        impactSummary = "Natasha ile adımlarınız tek kelime etmeden eşleşti; sahada kusursuz bir ikili oldunuz.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c2_overwatch",
                    label = "Yüksek kirişlerde kalıp tüm depoyu Beyaz Göz algısıyla oku ve ekibi anlık yönlendir.",
                    toneHint = "Gözcü • Stratejik",
                    nextNodeId = "ch4_s2b_ambush",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.CLINT to 8, BookCharacterId.STEVE to 8, BookCharacterId.TONY to 4),
                        avengersTrustDelta = 8,
                        setFlags = mapOf("ch4_role" to "overwatch"),
                        impactSummary = "İki çapraz pusuyu saniyeler öncesinden görüp uyardın; iki kez ekip arkadaşlarının hayatını kurtardın.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── CH4 SCENE 2B (NEW EXTENDED MID-OPERATION CLIMAX) ──
        BookSceneNode(
            id = "ch4_s2b_ambush",
            chapterNumber = 4,
            sceneOrder = 3,
            sceneTitle = "Çelik Kirişler Arasında Pusu",
            locationTag = "ANA DEPO İÇİ • MERKEZ YÜKLEME ALANI",
            paragraphs = listOf(
                "Depoya girişin ilk üç dakikası saat gibi işledi. Metal yürüme yollarında susturuculu tek bir mermi bile sekmeden dört devriye yere indi. Ama tam ana konteynerlerin arasına ulaştığınızda tavan projektörleri ani bir şimşekle patlayıp söndü — ardından acil durum sirenlerinin boğuk kırmızı ışığı bütün depoyu kana boyadı.",
                "\"Tuzak!\" diye bağırdı Steve, vibranyum kalkanını havaya kaldırıp üç ağır plazma atışını aynı anda saptırırken. Kıvılcımlar ıslak betona yağdı. \"Silahları standart HYDRA değil — enerji bastırıcı mermi kullanıyorlar!\"",
                "Yukarıdaki vinç köprüsünden bir keskin nişancı doğrudan Natasha'nın siper aldığı çelik direğe kilitlenmişti. Lazer çizgisi yağmur buharının içinde ince, ölümcül bir kızıl iplik gibi titriyordu. Aynı anda deponun merkezindeki konteynerin kapağı aralandı ve içinden boğucu bir elektrik uğultusu yükseldi; damarlarındaki mavi enerji o uğultuya şiddetle karşılık verdi.",
                "Kulaklığında Tony'nin acil uyarısı çınladı: \"Aiden, o konteyner bir tür frekans çekicisi! Yaklaşırsan ışınlanma alanını bozabilir — ama nişancı üç saniye içinde ateş edecek!\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch4_accept",
                    requiredFlagValue = "tactical",
                    callbackLabel = "Alıcı Kaydı Avantajı",
                    paragraphs = listOf("Fury'nin az önce açtığı alıcı kaydından bu birimin taktiğini zaten biliyordun: önce ışıkları keser, sonra enerji bastırıcıyı açıp kaçış yollarını kilitlerlerdi. Panik yerine üç adım sonrasını görerek hareket ettin.")
                )
            ),
            decisionTitle = "Pusu Anında Kırılma Kararı",
            choicePrompt = "Üç saniyen var: Natasha hedefte, enerji çekicisi güçleniyor. Nasıl müdahale edeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch4_c2b_save_natasha",
                    label = "Doğrudan vinç köprüsüne ışınlanıp nişancıyı yere indir, Natasha'nın hattını tamamen aç.",
                    toneHint = "Koruyucu • Hızlı",
                    nextNodeId = "ch4_s3_signature",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 9, BookCharacterId.STEVE to 5),
                        avengersTrustDelta = 6,
                        setFlags = mapOf("ch4_ambush" to "saved_natasha"),
                        impactSummary = "Lazer tetiklenmeden yarım saniye önce köprüde belirdin; Natasha aşağıdan tek bir baş hareketiyle teşekkür etti.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c2b_disrupt_core",
                    label = "Enerji çekicisinin ortasına dalıp kendi dalganla devreyi yak — tüm silahları tek darbede sustur.",
                    toneHint = "Yüksek Risk • Kırmızı Göz",
                    nextNodeId = "ch4_s3_signature",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 8, BookCharacterId.CLINT to 5, BookCharacterId.WANDA to 4, BookCharacterId.BRUCE to 5),
                        fatigueDelta = 1,
                        redEyeDelta = 1,
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch4_ambush" to "overloaded_core"),
                        impactSummary = "Gözlerinde bir anlık kızıl bir kıvılcım parladı; deponun tüm enerji silahları aynı saniyede sustu ama şakakların zonkladı.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c2b_shield_combo",
                    label = "Steve'in kalkan açısına ışınlanıp darbeyi saptır, Clint'e yukarıdan temiz ok atış koridoru yarat.",
                    toneHint = "Ekip Senkronu • Taktik",
                    nextNodeId = "ch4_s3_signature",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 9, BookCharacterId.CLINT to 8),
                        avengersTrustDelta = 9,
                        setFlags = mapOf("ch4_ambush" to "team_combo"),
                        impactSummary = "Steve, Clint ve sen sanki yıllardır aynı safta dövüşüyormuş gibi üç saniyede pusuyu tersine çevirdiniz.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── CH4 SCENE 3: MAJOR BRANCHING POINT (ENERGY SIGNATURE) ──
        BookSceneNode(
            id = "ch4_s3_signature",
            chapterNumber = 4,
            sceneOrder = 4,
            sceneTitle = "Tanıdık Frekans",
            locationTag = "ANA DEPO • ALT KASA KORİDORU",
            paragraphs = listOf(
                "Depodaki muhafızlar altı dakikada etkisiz hale getirildi. Çatışma temizdi, sivil kayıp yoktu, ekibin tek bir üyesi bile ağır yara almamıştı. Tam Steve ana konteyneri manyetik kilitle mühürlemek üzereyken ensende keskin, buz gibi bir titreşim patladı — beş yaşındayken gökyüzü maviye döndüğünde hissettiğin o frekansın birebir aynısı.",
                "Nefesin boğazında düğümlendi. Parmaklarının ucunda ince mavi kıvılcımlar kendiliğinden toplandı. Ses ana konteynerden gelmiyordu; deponun en arka beton duvarının ardında, liman planlarında hiç görünmeyen gizli bir çelik kasa odasının içinden yükseliyordu.",
                "Damarlarındaki enerji, sanki uzun zamandır kayıp bir ikizini karanlıkta tanımış gibi yanıt verdi. Duvarın ardındaki şey canlı mıydı, yoksa yirmi yıl öncesinden kalma bir makine mi? Bilmiyordun — ama adım atmazsan bir daha asla bu kadar yaklaşamayacağını iliklerine kadar hissediyordun.",
                "\"Aiden?\" dedi Natasha kulaklıktan; sesinde keskin bir dikkat vardı. \"Ana hedef mühürlendi, iki dakikaya polis ve temizlik birimi sahaya giriyor. Nereye bakıyorsun? Ne görüyorsun?\""
            ),
            decisionTitle = "Beklenmedik Enerji İmzası Kararı",
            choicePrompt = "Zaman daralıyor ve duvarın ardında geçmişinin yankısı var. Nasıl hareket edeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch4_c3_stop_team",
                    label = "Ekibi durdur: \"Üç dakika ver bana, Rogers.\" Kasa odasını ekiple birlikte aç.",
                    toneHint = "Dallanma • Ekip Keşfi",
                    nextNodeId = "ch4_s4_cryotube",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 9, BookCharacterId.NATASHA to 6),
                        avengersTrustDelta = 8,
                        secretUnlocked = "Sovyet Kriyojenik Tüp Damgası",
                        setFlags = mapOf("ch4_discovery" to "cryotube"),
                        branchTag = "Kriyojenik Tüp Keşfi",
                        impactSummary = "Ekiple açtığın odada eski Sovyet damgalı boş bir kriyojenik tüp buldun; Steve bunun sıradan bir operasyon olmadığını anladı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c3_continue",
                    label = "Planı bozma — ana operasyonu eksiksiz tamamla, kasa odasındaki şifreli sürücüyü çıkışta al.",
                    toneHint = "Dallanma • Biyometrik Sürücü",
                    nextNodeId = "ch4_s4_drive",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 10, BookCharacterId.STEVE to 5),
                        avengersTrustDelta = 6,
                        secretUnlocked = "Biyometrik Kilitli Sürücü",
                        setFlags = mapOf("ch4_discovery" to "biometric_drive"),
                        branchTag = "Biyometrik Sürücü Yolu",
                        impactSummary = "Ele geçirilen şifreli sürücünün yalnızca senin DNA ve enerji imzanla açıldığı ortaya çıktı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c3_solo",
                    label = "Kulaklığı kıs ve tek başına duvarın ötesine ışınlan — içeride ne varsa önce sen yüzleş.",
                    toneHint = "Dallanma • Gizli Yüzleşme",
                    nextNodeId = "ch4_s4_solo_voice",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -4, BookCharacterId.NATASHA to 4, BookCharacterId.WANDA to 5),
                        avengersTrustDelta = -5,
                        mysteryDelta = 3,
                        secretUnlocked = "Karanlıktaki Türkçe Uyarı",
                        setFlags = mapOf("ch4_discovery" to "solo_voice"),
                        branchTag = "Tek Başına Temas Yolu",
                        impactSummary = "Kasa odasındaki gölge figür sana Türkçe seslendi: 'Nihayet. Seni yirmi yıldır bekliyordum.'",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── CH4 SCENE 4 (3 DISTINCT DISCOVERY BRANCHES) ──
        BookSceneNode(
            id = "ch4_s4_cryotube",
            chapterNumber = 4,
            sceneOrder = 5,
            sceneTitle = "Buzlu Camın Ardındaki İz",
            locationTag = "GİZLİ KASA ODASI • MANHATTAN LİMANI",
            paragraphs = listOf(
                "Steve kalkanının kenarıyla paslı çelik sürgüyü kırdığında içeriden dondurucu, beyaz bir nitrojen buharı ayaklarınıza doğru yayıldı. Fenerlerin ışığı sisin içinde dağılırken odanın merkezinde dev, çatlamış bir kriyojenik tüp belirdi — az önce içeriden biri kendi gücüyle çıkmış gibi camında hâlâ eriyen buz damlaları aşağı süzülüyordu.",
                "Tüpün zeminindeki kablolar zorla koparılmıştı. Metal tabanın kenarında soğuk savaş döneminden kalma soluk kırmızı bir yıldız damgası ve sonradan el kazımasıyla eklenmiş taze bir kod vardı: *OBJEKT-7 / İSTANBUL ARŞİVİ.*",
                "Natasha eğilip metal etiketi fenerle aydınlattı; normalde hiç bozulmayan soğukkanlı yüzü bir saniyeliğine gerildi. \"Bu HYDRA değil,\" dedi sesi buz gibiydi. \"Çok daha eski. Sovyet çöküşünden önce yeraltına inmiş ayrı bir hücre.\"",
                "Steve ağır ağır doğrulup sana baktı. Gözlerinde şüphe değil, ciddi bir koruma içgüdüsü vardı: \"Bu sevkiyat bir silah kaçakçılığı değildi, Aiden. Birileri seni bu rıhtıma çekmek için özellikle senin imzanı yem olarak bıraktı.\""
            ),
            decisionTitle = "Steve'in 'Nasıl Geçti?' Sorusu",
            choicePrompt = "Kule'ye döndüğünüzde Steve kapının eşiğinde durup 'İlk görevin nasıl geçti?' diye soruyor.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch4_c4_honest",
                    label = "\"Düşündüğümden farklıydı, Rogers. Daha gerçekti. Bir sonraki görevde de varım.\"",
                    toneHint = "Dürüst • Bağlı",
                    nextNodeId = "ch4_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 10, BookCharacterId.CLINT to 5),
                        avengersTrustDelta = 8,
                        setFlags = mapOf("ch4_debrief" to "honest"),
                        impactSummary = "İlk kez zorunlulukla değil gönüllü olarak 'evet' dedin; Steve içtenlikle gülümsedi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c4_short",
                    label = "\"Bitti ve kimse ölmedi. Bu benim için yeterli.\"",
                    toneHint = "Kısa • Pragmatik",
                    nextNodeId = "ch4_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 6, BookCharacterId.STEVE to 3),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch4_debrief" to "short"),
                        impactSummary = "Steve çıkarken omzuna dokundu; Natasha 'İyi iş, Blackwood' diyerek onayını verdi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c4_question",
                    label = "\"Sovyet dosyasını araştırdınız mı? Çünkü bu operasyon doğrudan bana kurulmuş bir tuzaktı.\"",
                    toneHint = "Sorgulayıcı • Odaklı",
                    nextNodeId = "ch4_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 6, BookCharacterId.WANDA to 5),
                        mysteryDelta = 1,
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch4_debrief" to "focused_file"),
                        impactSummary = "Oda bir anda soğudu; Steve ve Tony Sovyet dosyasını en üst önceliğe aldı.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch4_s4_drive",
            chapterNumber = 4,
            sceneOrder = 5,
            sceneTitle = "Biyometrik Anahtar",
            locationTag = "AVENGERS KULESİ • TONY STARK LABORATUVARI",
            paragraphs = listOf(
                "Operasyon kusursuz bitti. Çıkışta gizli kasa odasının zırhlı panelinden söktüğün ağır, mat siyah veri sürücüsünü Kule'ye döner dönmez Tony'nin hologram masasına bıraktın. Stark yirmi dakika boyunca FRIDAY'in tüm kuantum kripto algoritmalarını denedi ama ekranda kırmızı kilit simgesinden başka tek bir satır bile belirmedi.",
                "\"Bu eski Sovyet askeri şifrelemesinin modifiye bir türevi,\" diye homurdandı Tony kahvesini yudumlarken. \"Kaba kuvvetle kırılması altı ay sürer — tabii fiziksel anahtarı yoksa.\"",
                "Tam o an masaya biraz daha yaklaşıp avucunu sürücünün soğuk gövdesine bıraktığında, cihazın yanındaki karanlık şerit aniden buz mavisi bir ışıkla parladı ve mekanik kilit keskin bir tık sesiyle açıldı. Tony elindeki kupayı masaya bırakıp ağır ağır sana döndü.",
                "\"Biyometrik rezonans kilidi,\" dedi şakadan tamamen arınmış, ciddi bir sesle. \"Aiden, bu cihazı kim kodladıysa senin moleküler imzanı yirmi yıl öncesinden biliyormuş. Ekranda açılan tek bir klasör adı var: *KIRMIZI BUZ — ELIAS VOSS.*\""
            ),
            decisionTitle = "Steve'in 'Nasıl Geçti?' Sorusu",
            choicePrompt = "Laboratuvardan çıktığında Steve koridorda seni bekliyor: 'İlk görevin nasıl geçti, Aiden?'",
            choices = listOf(
                BookChoiceDef(
                    id = "ch4_c4_honest",
                    label = "\"Düşündüğümden farklıydı, Rogers. Daha gerçekti. Bir sonraki görevde de varım.\"",
                    toneHint = "Dürüst • Bağlı",
                    nextNodeId = "ch4_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 10, BookCharacterId.CLINT to 5),
                        avengersTrustDelta = 8,
                        setFlags = mapOf("ch4_debrief" to "honest"),
                        impactSummary = "İlk kez zorunlulukla değil gönüllü olarak 'evet' dedin; Steve içtenlikle gülümsedi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c4_short",
                    label = "\"Bitti ve kimse ölmedi. Bu benim için yeterli.\"",
                    toneHint = "Kısa • Pragmatik",
                    nextNodeId = "ch4_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 6, BookCharacterId.STEVE to 3),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch4_debrief" to "short"),
                        impactSummary = "Steve çıkarken omzuna dokundu; Natasha 'İyi iş, Blackwood' diyerek onayını verdi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c4_question",
                    label = "\"Sovyet dosyasını araştırdınız mı? Çünkü o sürücü doğrudan bana yazılmış bir mektuptu.\"",
                    toneHint = "Sorgulayıcı • Odaklı",
                    nextNodeId = "ch4_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 6, BookCharacterId.WANDA to 5),
                        mysteryDelta = 1,
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch4_debrief" to "focused_file"),
                        impactSummary = "Oda bir anda soğudu; Steve ve Tony sürücüdeki izi en üst önceliğe aldı.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch4_s4_solo_voice",
            chapterNumber = 4,
            sceneOrder = 5,
            sceneTitle = "Karanlıktaki Ses",
            locationTag = "GİZLİ KASA ODASI • YALNIZ TEMAS",
            paragraphs = listOf(
                "Mavi kıvılcımla çelik duvarın ardına geçtiğinde kasa odası karanlıktı — ama boş değildi. Kırık bir kriyojenik kabinin hemen yanında, koyu kırmızı eski bir zırh palto giymiş uzun boylu bir adam kıpırdamadan duruyordu. Göğsündeki metal halkalar senin tenindeki statik elektrikle aynı frekansta vızıldıyordu. Seni görünce silahına uzanmadı.",
                "Bunun yerine aksanlı ama şaşırtıcı derecede net bir Türkçe ile konuştu: \"Nihayet. Seni yirmi yıldır bekliyordum, Aiden Blackwood.\"",
                "Kalbin göğüs kafesine sertçe vurdu. Yirmi yıldır kimse gerçek adını bu kadar eski bir tanışıklıkla söylememişti. Refleksle Beyaz Göz'ü ateşleyip mekânı kilitlemeye kalktığın anda adam geri çekilip elindeki sis kapsülünü botunun altında ezdi.",
                "Yoğun beyaz dumanın içinden yalnızca son cümlesi kaldı: \"Avcılar uyandı. Yakında görüşeceğiz.\" Çelik kapı kırılıp Steve ve Natasha içeri girdiğinde oda bomboştu — ama yerde, ayaklarının hemen dibinde eski bir Sovyet yıldızı kazılı buz gibi bir madalyon duruyordu."
            ),
            decisionTitle = "Steve'in 'Nasıl Geçti?' Sorusu",
            choicePrompt = "Kule'ye döndüğünüzde Steve kapının eşiğinde seni buluyor: 'Bize anlatmadığın bir şey mi oldu? İlk görevin nasıl geçti?'",
            choices = listOf(
                BookChoiceDef(
                    id = "ch4_c4_honest",
                    label = "\"İçeride biri vardı ve beni tanıyordu, Rogers. Ama kaçmak yerine geri döndüm — bu benim için yeni.\"",
                    toneHint = "Dürüst • Bağlı",
                    nextNodeId = "ch4_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 10, BookCharacterId.CLINT to 5),
                        avengersTrustDelta = 9,
                        setFlags = mapOf("ch4_debrief" to "honest"),
                        impactSummary = "Yaşadığın tuhaf karşılaşmayı saklamadın; Steve'in güveni daha da güçlendi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c4_short",
                    label = "\"Bitti. Bu şimdilik yeterli.\"",
                    toneHint = "Kısa • Pragmatik",
                    nextNodeId = "ch4_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 6, BookCharacterId.STEVE to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch4_debrief" to "short"),
                        impactSummary = "Steve çıkarken omzuna dokundu; Natasha koridorda 'İyi iş' diyerek başını eğdi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c4_question",
                    label = "\"Sovyet dosyasını araştırdınız mı? Çünkü o adam yirmi yıldır beni beklediğini söyledi.\"",
                    toneHint = "Sorgulayıcı • Odaklı",
                    nextNodeId = "ch4_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 6, BookCharacterId.WANDA to 5),
                        mysteryDelta = 1,
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch4_debrief" to "focused_file"),
                        impactSummary = "Oda buz kesti; tüm ekip Sovyet dosyasının izini sürmek için alarm durumuna geçti.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch4_s5_end",
            chapterNumber = 4,
            sceneOrder = 6,
            sceneTitle = "Buzun Altındaki İsim",
            locationTag = "AVENGERS KULESİ • GECE YARISI",
            paragraphs = listOf(
                "İlk resmi görevini tamamlamıştın. Yirmi yılın alışkanlığıyla limandan tek başına kaybolmak yerine kalmış, yan yana savaşmış ve aynı Quinjet'le geri dönmüştün. Ama 19 Numaralı Rıhtım'dan getirdiğin iz, Kule'deki bütün ekranları koyu kırmızı bir alarm rengine boyamıştı.",
                "O gece yatağını ilk kez kapıya dayamadan uyudun — ama uykun kısa sürdü. Kırk sekiz saat geçmeden, Queens köprüsünün altındaki terk edilmiş demiryolu hangarından yüksek enerjili, çıplak bir sinyal yükseldi.",
                "Bu sefer kimse saklanmıyordu. Sinyal şifresizdi ve doğrudan Avengers Kulesi'nin seksen sekizinci katını hedefliyordu. FRIDAY'in soğuk sesi koridorlarda yankılandığında ekranda tek bir isim yanıp sönüyordu: *Elias Voss. Kod Adı: Kırmızı Koruyucu.*"
            ),
            isChapterEnd = true,
            chapterEndQuote = "İlk görevi kazandın. Ama geçmişin aynası artık kapının hemen önündeydi."
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 5: KIRMIZI BUZ
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch5_s1",
            chapterNumber = 5,
            sceneOrder = 1,
            sceneTitle = "Kar Altında Kırmızı Koruyucu",
            locationTag = "ESKİ DEMİRYOLU HANGARI • QUEENS • 01:12",
            paragraphs = listOf(
                "Yağmur gece yarısından sonra sulu kara dönmüştü. Terk edilmiş demiryolu hangarının paslı çatısından sızan rüzgâr, paslanmış rayların arasında ince bir ıslık çalıyordu. Steve, Natasha, Wanda ve yüksek kirişlerde yayını gergin tutan Clint karanlık çemberi kapatarak yerlerini alırken sen içerideki tek sıcak, nabız gibi atan enerji imzasına doğru yürüdün.",
                "Paslı lokomotiflerin arasında koyu kızıl zırhlı, sol yanağı eski bir yanık iziyle çizilmiş uzun boylu bir adam hareketsiz bekliyordu. Elias Voss. Soğuk Savaş'ın karanlık laboratuvarlarında unutulmuş bir süper asker deneyinin hayatta kalan gölgesi. Zırhındaki solgun kırmızı enerji halkaları senin mavi kıvılcımlarınla şaşırtıcı bir uyum içinde titreşiyordu — sanki aynı aletin iki farklı yarısı gibi.",
                "Steve kalkanını kaldırıp öne çıktı, sesi hangar duvarlarında yankılandı: \"Teslim ol, Voss! Etrafın tamamen sarılı!\"",
                "Ama adam Steve'e dönüp bakmadı bile. Gözlerini yalnızca sana dikti, zırhlı eldivenlerini yavaşça iki yana açıp indirdi ve fısıltıyla ama buz gibi havada kristal kadar net yankılanan bir sesle konuştu:",
                "\"Kaç, Aiden. Hâlâ vaktin varken kaç.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch4_discovery",
                    requiredFlagValue = "solo_voice",
                    callbackLabel = "Limandaki Yalnız Temas",
                    paragraphs = listOf("Sesini anında tanıdın. 19 Numaralı Rıhtım'ın karanlık kasa odasında sana Türkçe seslenen gölge buydu. Bu kez sis perdesi yoktu; yüzünde düşmanlık değil, yirmi yıllık bir uykusuzluğun yorgunluğu vardı.")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch4_discovery",
                    requiredFlagValue = "biometric_drive",
                    callbackLabel = "Biyometrik Sürücü Kaydı",
                    paragraphs = listOf("Tony'nin laboratuvarında avucunun dokunuşuyla açılan o dosyanın başlığı zihninde parladı: *KIRMIZI BUZ — ELIAS VOSS.* Senin imzanı taşıyan kilidin sahibi şimdi tam on adım karşında duruyordu.")
                )
            ),
            decisionTitle = "Voss'un 'Kaç' Uyarısına Tepki",
            choicePrompt = "Elias Voss'un beklenmedik uyarısına nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch5_c1_talk",
                    label = "Dur, elini indir ve doğrudan Türkçe sor: \"Kim söyledi sana bunu? Benden ne istiyorsun?\"",
                    toneHint = "Diyalog • Yüzleşme",
                    nextNodeId = "ch5_s2_capture",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.WANDA to 6),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch5_voss_first" to "talked"),
                        impactSummary = "Voss gözlerinin içine baktı: 'Seni bulmak için kullandılar beni. Ama ben seni uyarmak istedim.'",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c1_attack",
                    label = "Diyalog yok — bu bir oyalama. Işınlanıp anında üzerine atıl ve onu etkisiz hale getir.",
                    toneHint = "Operasyon • Sert",
                    nextNodeId = "ch5_s2_capture",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 6, BookCharacterId.STEVE to 2, BookCharacterId.WANDA to -3),
                        fatigueDelta = 1,
                        redEyeDelta = 1,
                        setFlags = mapOf("ch5_voss_first" to "attacked"),
                        impactSummary = "Voss direnmeden diz çöktü, sadece 'Söylemem gereken bir şey var' dedi. Steve gözlerindeki karmaşayı fark etti.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c1_mind",
                    label = "Beyaz Göz'ü aç ve uzaktan zihninin yüzeyine dokun — yalan mı söylüyor, anında gör.",
                    toneHint = "Kozmik • Zihin Okuma",
                    nextNodeId = "ch5_s2_capture",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 9, BookCharacterId.NATASHA to 4),
                        fatigueDelta = 1,
                        mysteryDelta = 2,
                        secretUnlocked = "Voss'un Zihnindeki Fener",
                        setFlags = mapOf("ch5_voss_first" to "mind_touch"),
                        impactSummary = "Voss'un zihninde yıllarca süren acı gördün — ve en dibinde seni avlamak için değil, uyarmak için taşınan bir fener.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch5_s2_capture",
            chapterNumber = 5,
            sceneOrder = 2,
            sceneTitle = "Camın Ardındaki Ayna",
            locationTag = "AVENGERS KULESİ • 42. KAT GÜVENLİ SORGU ALANI",
            paragraphs = listOf(
                "Voss teslim oldu. Kaçabilirdi, hangardaki enerji hatlarını kullanıp savaşabilirdi; ama bileklerine Stark yapımı manyetik kelepçe takılırken tek bir kasını bile oynatmadı. Şimdi 42. katın tek yönlü aynalı sorgu odasında oturuyordu; başı hafifçe öne eğik, gözleri doğrudan karanlık aynaya —sanki camın ardında tam nerede durduğunu biliyormuş gibi— sabitlenmişti.",
                "Gözlem odasında hava gergindi. Mavi ekran ışığı yüzlere sert gölgeler düşürüyordu. \"S.H.I.E.L.D. kayıtlarına göre 2004'ten beri ölü sayılıyor,\" dedi Natasha tabletini masaya bırakarak. \"Dondurulmuş, silinmiş ve haftalar önce biri tarafından yeniden uyandırılmış. Ve yalnızca tek bir kişiyle konuşmayı şart koştu: seninle.\"",
                "Tony kollarını kavuşturdu: \"Klasik yem senaryosu olabilir. Zihninde psikolojik tetikleyici bile taşıyor olabilir.\"",
                "Wanda aynaya bir adım daha yaklaşırken sesi alçak ama kesin tınlıyordu: \"Hayır. Zihninde ağır bir travma var ama kalkanı tamamen açık. Bizi içeri davet ediyor. İstersen ben girebilirim — ya da Natasha profesyonel mesafeyle konuşturabilir. Ya da sen yüzleşirsin. Karar senin, Aiden.\""
            ),
            decisionTitle = "Voss Sorgu Odası Seçimi",
            choicePrompt = "Sorgu odasına kimin girmesi gerektiğine karar ver.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch5_c2_aiden",
                    label = "Sen gir — bu adam yirmi yıl önceki geceden geliyor, yüzleşmesi gereken kişi sensin.",
                    toneHint = "Dallanma • Birebir Yüzleşme",
                    nextNodeId = "ch5_s3_aiden_room",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 7, BookCharacterId.WANDA to 5),
                        avengersTrustDelta = 5,
                        secretUnlocked = "Aynı Eller İtirafı",
                        setFlags = mapOf("ch5_interrogation" to "aiden"),
                        branchTag = "Aiden'ın Sorgusu",
                        impactSummary = "Voss ile yüz yüze oturdun: 'İkimiz de aynı şeyiz Blackwood. Sadece farklı ellerde.'",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c2_natasha",
                    label = "Natasha girsin — sen duygusal mesafeni koruyup camın arkasından her tepkisini izle.",
                    toneHint = "Dallanma • Natasha Sorgusu",
                    nextNodeId = "ch5_s3_natasha_room",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 11, BookCharacterId.TONY to 5),
                        avengersTrustDelta = 6,
                        secretUnlocked = "Voss'un Taraf Değişimi",
                        setFlags = mapOf("ch5_interrogation" to "natasha"),
                        branchTag = "Natasha'nın Sorgusu",
                        impactSummary = "Natasha ustalıkla Voss'un geçmişini çözdü: 'Seni avlamak için yola çıktı, sonra karar değiştirdi.'",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c2_wanda",
                    label = "Wanda girsin — kelimeler yalan söyleyebilir ama zihinsel tarama gerçeği saklayamaz.",
                    toneHint = "Dallanma • Wanda Taraması",
                    nextNodeId = "ch5_s3_wanda_room",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 12),
                        avengersTrustDelta = 5,
                        secretUnlocked = "İstanbul Arşivi Anısı",
                        setFlags = mapOf("ch5_interrogation" to "wanda"),
                        branchTag = "Wanda'nın Zihin Taraması",
                        impactSummary = "Wanda Voss'un zihnindeki yıllar süren acıyı ve en alttaki İstanbul koordinatını gördü.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── CH5 SCENE 3 (3 DISTINCT INTERROGATION BRANCHES) ──
        BookSceneNode(
            id = "ch5_s3_aiden_room",
            chapterNumber = 5,
            sceneOrder = 3,
            sceneTitle = "Aynı Metalin İki Yüzü",
            locationTag = "SORGU ODASI 42-B",
            paragraphs = listOf(
                "Çelik kapı arkandan ağır bir kilit sesiyle kapandı. Metal sandalyeyi çekip Voss'un tam karşısına oturdun. Yakından bakınca koyu zırhının altındaki adam elli yaşlarında, gözlerinin altı derin uykusuzluk ve donmuş yılların izleriyle oyulmuş bir savaşçıydı.",
                "\"Beş yaşındaydın,\" dedi sesi çatallı ama sakindi. \"Mavi ışığı gökyüzünden inen bir kaza sandın. Değildi, Aiden. O gece Manhattan'da açılan şey bir yıldız kapısı değildi — *Proje 7-A*'nın ilk saha ateşlemesiydi. Beni de aynı protokolün on yıl önceki kusurlu bir versiyonuyla yaptılar. Fark ne biliyor musun? Ben kaçamadım.\"",
                "Manyetik kelepçeli bileklerini masanın soğuk yüzeyine yavaşça bıraktı. \"Seni yirmi yıl boyunca bulamadıklarını sandın. Yanılıyorsun. *Kör Nokta* dedikleri yapı seni hiç kaybetmedi — sadece doğal büyüme evresini izlediler. Şimdi İstanbul'daki eski yeraltı arşivini yeniden açtılar. Aktivasyon için ekibi topladılar.\"",
                "Gözlerini kaldırıp tam senin göz bebeklerine baktı: \"İkimiz de aynı şeyiz, Blackwood. Sadece farklı ellerde büyüdük.\""
            ),
            decisionTitle = "'İkimiz De Aynıyız' Sözüne Yorum",
            choicePrompt = "Voss'un 'ikimiz de aynıyız' sözü zihninde yankılanıyor. İçindeki cevap ne?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch5_c3_agree",
                    label = "İçten içe katıl — ve bunun seni korkuttuğunu kabul et. Farklı bir elde sen de Voss olabilirdin.",
                    toneHint = "Kırılgan • Dürüst",
                    nextNodeId = "ch5_s4_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 8, BookCharacterId.STEVE to 5),
                        setFlags = mapOf("ch5_mirror_stance" to "agreed"),
                        impactSummary = "Korkunu bastırmadın; Voss'un o zırhın içinde hâlâ bir insanlığı koruduğunu fark ettin.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c3_disagree",
                    label = "Kesin biçimde reddet — benzer başlangıçlar aynı kaderi yazmaz; kendi seçimlerini kendin yaptın.",
                    toneHint = "Kararlı • Bağımsız",
                    nextNodeId = "ch5_s4_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 8, BookCharacterId.NATASHA to 6),
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch5_mirror_stance" to "disagreed"),
                        impactSummary = "Kendi kararlarınla yürüdüğün yolun farkını net koydun; bu fark her şeydi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c3_suspend",
                    label = "Ne kabul et ne reddet — gerçeğin tamamını İstanbul'da görmeden hüküm verme.",
                    toneHint = "Soğukkanlı • Ölçülü",
                    nextNodeId = "ch5_s4_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 6, BookCharacterId.NATASHA to 4),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch5_mirror_stance" to "suspended"),
                        impactSummary = "Aceleci yargılardan kaçındın; tüm parçalar masaya inene kadar serinkanlı kaldın.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch5_s3_natasha_room",
            chapterNumber = 5,
            sceneOrder = 3,
            sceneTitle = "Camın Arkasından",
            locationTag = "GÖZLEM ODASI 42-A",
            paragraphs = listOf(
                "Natasha içeri girdiğinde odayı tek bir bakışla sahiplendi. Sandalyeye oturmadı; masanın ucuna yaslanıp Voss ile Rusça birkaç alçak, buz gibi cümle konuştu. İkisinin arasında yalnızca eski bir dünyadan kurtulabilenlerin anladığı bir sessizlik aktı; Voss'un gergin omuzları ilk kez o an hafifçe düştü.",
                "On beş dakika sonra Natasha gözlem odasına döndüğünde elinde kurşun kalemle çizilmiş bir yeraltı krokisi vardı. \"Adı *Kör Nokta*,\" dedi haritayı cam masaya açarak. \"HYDRA'dan bile eski, devletler üstü bir gölge konsorsiyumu. Voss'u seni yakalamak için dondurucudan çıkardılar — ama o senin dosyanı okuduğunda, beş yaşındaki o geceyi gördüğünde emri reddetti.\"",
                "Natasha tek yönlü aynadan Voss'a, sonra doğrudan sana baktı. Gözlerinde nadir görülen bir ciddiyet vardı: \"Bana sana iletmem için tek bir cümle bıraktı: *'Aiden'a söyle, ikimiz de aynı şeyiz. Sadece farklı ellerde.'*\""
            ),
            decisionTitle = "'İkimiz De Aynıyız' Sözüne Yorum",
            choicePrompt = "Natasha'nın ilettiği o cümleye nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch5_c3_agree",
                    label = "\"Haklı olabilir, Natasha. On üç yaşımda kaçamasaydım, bugün o sandalyede ben oturuyor olurdum.\"",
                    toneHint = "Kırılgan • Dürüst",
                    nextNodeId = "ch5_s4_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 7, BookCharacterId.NATASHA to 9),
                        setFlags = mapOf("ch5_mirror_stance" to "agreed"),
                        impactSummary = "Kırılgan dürüstlüğün odadaki sessizliği derinleştirdi; Natasha elini sessizce omzuna bıraktı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c3_disagree",
                    label = "\"Hayır. Aynı laboratuvardan çıkmış olabiliriz ama ben kimsenin silahı olmadım.\"",
                    toneHint = "Kararlı • Bağımsız",
                    nextNodeId = "ch5_s4_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 8, BookCharacterId.NATASHA to 6),
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch5_mirror_stance" to "disagreed"),
                        impactSummary = "Kendi kararlarınla yürüdüğün yolun farkını net koydun; bu fark her şeydi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c3_suspend",
                    label = "\"Şimdilik sadece İstanbul krokisine odaklanalım. Hüküm vermek için erken.\"",
                    toneHint = "Soğukkanlı • Ölçülü",
                    nextNodeId = "ch5_s4_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 6, BookCharacterId.NATASHA to 4),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch5_mirror_stance" to "suspended"),
                        impactSummary = "Aceleci yargılardan kaçındın; tüm parçalar masaya inene kadar serinkanlı kaldın.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch5_s3_wanda_room",
            chapterNumber = 5,
            sceneOrder = 3,
            sceneTitle = "Kızıl Hafıza",
            locationTag = "GÖZLEM ODASI 42-A",
            paragraphs = listOf(
                "Wanda içeri girdiğinde parmaklarının ucunda ince, sakin bir kızıl hale yükseldi; saldırgan değil, neredeyse şefkatli bir ışık gibiydi. Voss gözlerini kapatıp zihninin kapısını gönüllü olarak açtı. Camın arkasından izlerken Wanda'nın yüzünün yavaşça solduğunu, nefesinin sıklaştığını gördün.",
                "Kapı açılıp Wanda dışarı çıktığında bir an koridor duvarına yaslandı; refleksle yanına geçip kolunu tuttun. Parmakları buz gibiydi. \"Tuzak değil, Aiden,\" dedi fısıltıyla. \"Zihninde yirmi yıllık donmuş karanlık var... ama en altında tek bir anıyı koruyarak aklını kaybetmemiş: senin adın. Seni uyarmak için taşınan bir fener gibi.\"",
                "Gözlerini sana kaldırdı; kızıl ışıltı hâlâ irislerinin kenarında sönmeye çalışıyordu. \"Bir şey daha gördüm: İstanbul Boğazı'nın altında eski bir taş sarnıca gizlenmiş bir arşiv. Ve Voss'un bana bıraktığı son düşünce şuydu — *'İkimiz de aynı şeyiz Blackwood. Sadece farklı ellerde.'*\""
            ),
            decisionTitle = "'İkimiz De Aynıyız' Sözüne Yorum",
            choicePrompt = "Wanda'nın gördüklerinden sonra bu aynaya nasıl bakıyorsun?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch5_c3_agree",
                    label = "\"Beni korkutan da bu, Wanda. Farklı bir elde ben de onun yerinde olabilirdim.\"",
                    toneHint = "Kırılgan • Dürüst",
                    nextNodeId = "ch5_s4_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 10, BookCharacterId.STEVE to 4),
                        setFlags = mapOf("ch5_mirror_stance" to "agreed"),
                        impactSummary = "Wanda parmaklarını yavaşça senin bileğine değdirdi: 'Ama burada, bu eldesin artık.'",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c3_disagree",
                    label = "\"Hayır. Ne yaşarsam yaşayayım, başkasının emriyle can almadım. Biz aynı değiliz.\"",
                    toneHint = "Kararlı • Bağımsız",
                    nextNodeId = "ch5_s4_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 8, BookCharacterId.NATASHA to 6),
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch5_mirror_stance" to "disagreed"),
                        impactSummary = "Kendi çizgini net biçimde koydun; Wanda başını sallayarak saygıyla onayladı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c3_suspend",
                    label = "\"Buna İstanbul'daki dosyayı görmeden cevap veremem. Önce gerçeğin tamamını alalım.\"",
                    toneHint = "Soğukkanlı • Ölçülü",
                    nextNodeId = "ch5_s4_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 6, BookCharacterId.NATASHA to 4),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch5_mirror_stance" to "suspended"),
                        impactSummary = "Aceleci yargılardan kaçındın; tüm parçalar masaya inene kadar serinkanlı kaldın.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        // ── CH5 SCENE 4: POST-INTERROGATION DEEPENING DECISION ──
        BookSceneNode(
            id = "ch5_s4_aftermath",
            chapterNumber = 5,
            sceneOrder = 4,
            sceneTitle = "Gece Yarısı İtirafı",
            locationTag = "AVENGERS KULESİ • 88. KAT CAM KORİDOR • 03:45",
            paragraphs = listOf(
                "Sorgu sona erdiğinde Kule'nin seksen sekizinci katı derin bir sessizliğe büründü. Tony ve Steve alt katta İstanbul Boğazı'nın uydu taramalarını incelerken sen cam duvarın önünde durmuş, karla karışık yağmurun Manhattan gökdelenlerine çarpışını izliyordun.",
                "Bütün o yıllar boyunca kaçtığın şeyin bir hayalet değil, adı sanı olan bir örgüt olduğunu öğrenmek tuhaf bir ağırlık bırakmıştı. *Kör Nokta.* Beş yaşındaki o mavi geceye dokunan herkes hâlâ hayattaydı — ve bir yerlerde senin adını taşıyan bir dosya hâlâ açıktı.",
                "Koridorun ucundan hafif ayak sesleri geldi. Wanda elinde iki sıcak fincanla yanına yaklaştı; gözlerinde az önce Voss'un zihninden taşıdığı o derin hüzün henüz dağılmamıştı. \"Fury yarın sabah Voss'u Raft yüksek güvenlik tesisine nakletmek istiyor,\" dedi alçak sesle. \"Eğer bir şey söyleyeceksen... ya da Voss'u burada tutmamızı istiyorsan, şimdi konuşmalısın.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "early_interest",
                    requiredFlagValue = "wanda",
                    callbackLabel = "Wanda ile Sessiz Yakınlık",
                    paragraphs = listOf("Wanda fincanı uzatırken parmakları senin eline bir saniye fazla dokundu. \"İlk gece pencere kenarında bana baktığın anı hatırlıyorum, Aiden,\" diye fısıldadı. \"O zaman duvarların vardı. Şimdi hâlâ yorgunsun... ama artık yalnız saklanmıyorsun.\"")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "early_interest",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Natasha'nın Gölge Nöbeti",
                    paragraphs = listOf("Koridorun diğer ucunda Natasha'nın kapıya yaslanmış seni izlediğini fark ettin. Seninle göz göze gelince sadece hafifçe başını salladı: \"Fury'yi ben oyalayabilirim, Blackwood. Yeter ki ne istediğine karar ver.\"")
                )
            ),
            decisionTitle = "Elias Voss'un Kaderi",
            choicePrompt = "Fury sabah Voss'u Raft hapishanesine götürmek istiyor. Nasıl bir tavır alacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch5_c4_protect_voss",
                    label = "\"Voss Kule'de kalıyor. Bizi uyarmak için hayatını riske attı — onu Raft'a gömmelerine izin vermem.\"",
                    toneHint = "Koruyucu • Kararlı",
                    nextNodeId = "ch5_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 8, BookCharacterId.STEVE to 7),
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch5_voss_fate" to "protected"),
                        impactSummary = "Steve Fury'ye doğrudan karşı çıkıp senin yanında durdu; Voss Kule'de koruma altına alındı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c4_use_intel",
                    label = "\"İstanbul'daki dosya açılana kadar burada kalsın. Ondan sonra kararını kendisi versin.\"",
                    toneHint = "Dengeli • Stratejik",
                    nextNodeId = "ch5_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 7, BookCharacterId.TONY to 6),
                        avengersTrustDelta = 6,
                        setFlags = mapOf("ch5_voss_fate" to "conditional_hold"),
                        impactSummary = "Soğukkanlı yaklaşımın hem Tony'yi hem Natasha'yı ikna etti; Voss geçici olarak Kule'de kaldı.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c4_distrust",
                    label = "\"Ona hâlâ güvenmiyorum. Sıkı gözetim altında tutun — bu bir uzun vadeli tuzak olabilir.\"",
                    toneHint = "Temkinli • Şüpheci",
                    nextNodeId = "ch5_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 5, BookCharacterId.NATASHA to 4, BookCharacterId.WANDA to -3),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch5_voss_fate" to "strict_watch"),
                        impactSummary = "Tony ek güvenlik katmanları kurdu; Wanda ise 'Kendi yansımana güvenmek zordur' diye fısıldadı.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch5_s5_end",
            chapterNumber = 5,
            sceneOrder = 5,
            sceneTitle = "Bırakılmadın. Sadece Beklendin.",
            locationTag = "AVENGERS KULESİ • ŞAFAK ÖNCESİ • 04:10",
            paragraphs = listOf(
                "Şafaktan hemen önce, Wanda elinde eski bir harita projeksiyonuyla yeniden yanına geldi. Yüzü az öncekinden bile daha berrak — ve daha ciddiyet doluydu. Voss'un zihnindeki o son donmuş katmanı, kimsenin göremediği en derin anıyı az önce tamamen çözmüştü.",
                "Haritanın ortasında İstanbul Boğazı'nın karanlık suları ve altında parlayan ince kırmızı bir koordinat yanıyordu. Wanda derin bir nefes aldı ve senin yirmi yıllık tüm hikâyeni tek cümleyle tersine çevirdi:",
                "\"Aiden... seni yirmi yıl önce bırakmadılar. Hiçbir zaman gerçekten kaçmadın. Sadece olgunlaşmanı beklediler.\""
            ),
            isChapterEnd = true,
            chapterEndQuote = "Kırmızı Buz çözüldü. Ve ayna, hiç görmek istemediğin bir gerçeği göstermeye başladı."
        )
    ).associateBy { it.id }
}
