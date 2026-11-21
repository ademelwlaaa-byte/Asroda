package com.example.book

/**
 * Chapters 6 and 7 of "Kozmik Sürükleniş" (Aiden Blackwood — MCU Evreni).
 * Extended novel-length Turkish literary prose with deep cross-chapter Memory Echoes,
 * Red Eye instability & Avengers Trust tracking, and rich narrative payoffs.
 *
 * Chapter 6: Kırık Ayna (İstanbul Arşivi, Denek 7-A gerçeği, 3 operasyon yolu & gelecek kararı)
 * Chapter 7: Karanlık Ayna (Dr. Hale'in teslimiyeti, Prag aile sırrı, Savaş Masası,
 *            Wanda Asansörü, Natasha Çatısı, İstanbul Tünelleri ve Mira Finali)
 */
object CosmicDriftChapters6To7 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 6,
            romanNumeral = "VI",
            title = "Kırık Ayna",
            titleEn = "Broken Mirror",
            subtitle = "İstanbul Arşivi, Denek 7-A ve Kalmak Üzerine",
            description = "İstanbul arşivi bütün perdeyi yırtıyor: Aiden hiçbir zaman bir kaçak değildi — o bir projeydi. Ve Proje 7-A hâlâ sürüyor.",
            descriptionEn = "The Istanbul archive reveals the truth: Aiden was never a fugitive — he was a project. And Project 7-A is not over.",
            estimatedMinutes = 17,
            accentHex = 0xFF34D399L,
            startNodeId = "ch6_s1",
            sceneCount = 5,
            decisionCount = 4
        ),
        BookChapterMeta(
            number = 7,
            romanNumeral = "VII",
            title = "Karanlık Ayna",
            titleEn = "Dark Mirror",
            subtitle = "Dr. Hale, Prag Sırrı ve İstanbul Tünellerinde Kırmızı Göz",
            description = "Dr. Hale teslim oluyor. Ailenin izi bulunuyor. Ve İstanbul tünellerinde senin gibi bir Kırmızı Göz taşıyıcısı bekliyor.",
            descriptionEn = "Dr. Hale surrenders. Aiden's family trace is found. And a Red Eye carrier waits in the Istanbul tunnels.",
            estimatedMinutes = 22,
            accentHex = 0xFFC084FCL,
            startNodeId = "ch7_s1",
            sceneCount = 8,
            decisionCount = 7
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 6: KIRIK AYNA
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch6_s1",
            chapterNumber = 6,
            sceneOrder = 1,
            sceneTitle = "Yirmi Yıllık Gölge",
            locationTag = "AVENGERS KULESİ • 88. KAT • 04:15",
            paragraphs = listOf(
                "Wanda'nın sesi hâlâ odada yankılanıyordu. *Seni yirmi yıl önce bırakmadılar. Sadece beklediler.* Haritanın üstündeki mavi çizgiler gözünün önünde bir anlığına bulanıklaştı, sonra cam gibi keskinleşti. Yirmi yıldır kaçtığın tüm o şehirler — Lizbon'un dar sokakları, Prag'ın karlı çatıları, ucuz motellerin kapıya dayanan yatakları, her sabah saydığın çıkış kapıları — hepsi tek yönlü bir laboratuvar camının ardından mı izlenmişti?",
                "Göğsünün ortasında tuhaf bir boşluk açıldı; öfkeden daha soğuk, korkudan daha sessiz bir şey. Hayatta kalmakla gurur duyduğun yirmi yıl, başkasının deney defterinde *başarılı saha gözlemi* satırına mı denk geliyordu?",
                "Wanda bir adım yaklaştı ama omzuna dokunmadı; nefes alacak alanı sana bilinçli olarak bıraktı. \"HYDRA öncesi, devletlerden ve ordulardan bağımsız bir yapı,\" dedi sesi duruydu. \"Kendilerine *Kör Nokta* diyorlar. Voss'un anılarında tek bir tarih alev gibi parlıyor: senin beş yaşına bastığın o mavi gece.\"",
                "Odada yalnızca ikiniz vardınız. Cam duvarın hemen dışındaki brifing masasında Steve, Natasha, Tony, Bruce ve Clint sessizce bekliyor, kapıyı çalmadan önce senin hazır olmanı gözetiyordu."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch5_mirror_stance",
                    requiredFlagValue = "agreed",
                    callbackLabel = "Kırık Aynanın Yankısı",
                    paragraphs = listOf("Voss'un 'İkimiz de aynı şeyiz' sözü yeniden aklından geçti. İlk kez kaçmak yerine durup o aynanın içine bakıyordun — ve gördüğün şey artık seni felç etmiyordu.")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch5_voss_fate",
                    requiredFlagValue = "protected",
                    callbackLabel = "Voss'u Koruma Kararın",
                    paragraphs = listOf("Wanda hafifçe ekledi: \"Voss'u Raft'a göndermemeni o da duydu, Aiden. Sabah bana gardiyan aracılığıyla tek bir not yolladı: *'Sarnıcın altındaki üçüncü mühür, çocuğun kendi el iziyle açılır.'*\"")
                )
            ),
            decisionTitle = "Wanda'nın Açıklamasına Tepki",
            choicePrompt = "Bu gerçeğin ilk darbesine nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch6_c1_ask",
                    label = "Sakin kal, nefesini topla ve doğrudan sor: \"Kim? Bu örgütü kim yönetiyor, Wanda?\"",
                    toneHint = "Soğukkanlı • Odaklı",
                    nextNodeId = "ch6_s2_istanbul_plan",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 6, BookCharacterId.TONY to 5),
                        avengersTrustDelta = 4,
                        secretUnlocked = "Kör Nokta Örgüt Kaydı",
                        setFlags = mapOf("ch6_reaction" to "calm_ask"),
                        impactSummary = "Panik yerine gerçeğin köküne indin; Kör Nokta'nın yirmi yıllık gözetim protokolünü öğrendin.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c1_alone",
                    label = "Teras kapısına çık, beş dakika soğuk havada yalnız kal, sonra geri dön: \"Tamamını anlat.\"",
                    toneHint = "İçsel • Dengeli",
                    nextNodeId = "ch6_s2_istanbul_plan",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 9),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch6_reaction" to "stepped_out"),
                        impactSummary = "Wanda seni acele ettirmeden bekledi — sanki kaçmak yerine döneceğini zaten biliyormuş gibi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c1_gather",
                    label = "Kapıyı açıp tüm ekibi içeri çağır: \"Bunu herkesin duyması gerekiyor. Artık tek başıma saklamıyorum.\"",
                    toneHint = "Birlik • Güven",
                    nextNodeId = "ch6_s2_istanbul_plan",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 10, BookCharacterId.CLINT to 8, BookCharacterId.NATASHA to 7, BookCharacterId.WANDA to 6, BookCharacterId.BRUCE to 6),
                        avengersTrustDelta = 10,
                        setFlags = mapOf("ch6_reaction" to "gathered_team"),
                        impactSummary = "Steve: 'Bu artık senin tek başına sorunun değil. Bizim sorunumuz.' İlk kez bu yük hafif geldi.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch6_s2_istanbul_plan",
            chapterNumber = 6,
            sceneOrder = 2,
            sceneTitle = "İstanbul Operasyonu",
            locationTag = "QUİNJET HANGARI • HAZIRLIK • 06:00",
            paragraphs = listOf(
                "Quinjet'in türbinleri alçak, göğüste hissedilen bir uğultuyla ısınırken hangar kapıları yavaşça aralandı. Taktik hologramda İstanbul Boğazı'nın koyu lacivert suları ve tarihi yarımadanın metrelerce altında uzanan eski bir Bizans sarnıcı parlıyordu. Tony kırmızı noktayla işaretlenmiş yeraltı odasını büyüttü: \"Sarnıcın altı kurşun kaplı. Uydu içeri giremiyor; ne varsa gözünüzle görmeniz gerekecek.\"",
                "Natasha siyah saha kemerini sıktı, eldivenlerini geçirdi ve dönüp doğrudan senin önüne geldi. Etrafındaki kalabalığa rağmen o an sadece ikiniz varmışsınız gibi konuştu.",
                "\"Açık konuşacağım, Blackwood,\" dedi gözlerini kaçırmadan. \"İçeride bulacağımız dosyalar senin beş yaşındaki hayatına ait. Duygusal bulanıklık sahada en ölümcül hatadır — bunu kendi geçmişimden biliyorum. Bu operasyonda nerede durmak istiyorsun?\""
            ),
            decisionTitle = "İstanbul Operasyonunda Rol",
            choicePrompt = "İstanbul yeraltı arşivine yapılacak operasyonda hangi rolü seçeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch6_c2_lead",
                    label = "\"Liderliği ben alıyorum. Bu benim hikâyem — içeride ne varsa ilk ben göreceğim.\"",
                    toneHint = "Dallanma • Liderlik",
                    nextNodeId = "ch6_s3_istanbul_lead",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 7, BookCharacterId.STEVE to 6, BookCharacterId.CLINT to 4),
                        fatigueDelta = 1,
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch6_istanbul_role" to "lead"),
                        branchTag = "İstanbul Liderlik Yolu",
                        impactSummary = "Natasha saygıyla başını eğdi: 'Tamam. Ama duygusal ağırlığı bilerek yürü, arkandayım.'",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c2_support",
                    label = "\"Destek kanadında gelirim. Objektif kalmam zor olabilir — saha komutasını Natasha alsın.\"",
                    toneHint = "Dallanma • Olgun Destek",
                    nextNodeId = "ch6_s3_istanbul_support",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 11, BookCharacterId.STEVE to 8),
                        avengersTrustDelta = 8,
                        setFlags = mapOf("ch6_istanbul_role" to "support"),
                        branchTag = "İstanbul Destek Yolu",
                        impactSummary = "Sınırını bilip dürüstçe söylemen Natasha'nın gözlerinde sıcak bir onay uyandırdı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c2_stay_tower",
                    label = "\"Gitmiyorum. Objektif kalamam ve birinizi tehlikeye atamam — Kule'de kalıp Voss ile konuşacağım.\"",
                    toneHint = "Dallanma • Kule'de Kalış",
                    nextNodeId = "ch6_s3_tower_voss",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 11, BookCharacterId.STEVE to 6),
                        avengersTrustDelta = 6,
                        secretUnlocked = "Voss'un Kişisel Anahtarı",
                        setFlags = mapOf("ch6_istanbul_role" to "stayed_tower"),
                        branchTag = "Kule'de Voss Yüzleşmesi",
                        impactSummary = "Wanda: 'Gitmemek bazen en zor seçimdir.' Voss'la baş başa kalıp arşivin şifresini bizzat aldın.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── CH6 SCENE 3 (3 GENUINE BRANCHES) ──
        BookSceneNode(
            id = "ch6_s3_istanbul_lead",
            chapterNumber = 6,
            sceneOrder = 3,
            sceneTitle = "Sarnıcın Altındaki Dosya",
            locationTag = "İSTANBUL • YERALTI ARŞİVİ 7-A • 23:10",
            paragraphs = listOf(
                "Yağmurlu İstanbul gecesinde, eski taş duvarların arasına gizlenmiş dar merdivenlerden aşağı indiniz. Nem kokusuyla karışık keskin bir ozon kokusu burnuna çarptığında arşivin hâlâ gizli bir jeneratörle enerji çektiğini anladın. Güvenlik kapılarını uzam kıvılcımınla sessizce devre dışı bıraktın; Steve ve Natasha arkandan kusursuz bir disiplinle odayı güvene aldı.",
                "Sütunların ortasındaki karanlık cam masada tek bir fırçalanmış metal klasör bırakılmıştı — sanki burayı boşaltanlar özellikle senin eline geçmesi için geride bırakmıştı. Kapağı açtığında beş yaşındaki fotoğrafınla göz göze geldin. Saçların dağınık, yüzünde henüz hiçbir kaçışın izi yok.",
                "Fotoğrafın altındaki koyu kırmızı mühürde tek bir başlık kazılıydı: *PROJE 7-A — DENEK: AIDEN BLACKWOOD. DURUM: SERBEST GÖZLEM. AKTİVASYON BEKLENİYOR.*",
                "İkinci sayfada laboratuvar notlarının altında düzgün, eğik bir el yazısıyla atılmış bir imza duruyordu: *Baş Araştırmacı: Dr. Evelyn Hale.* Sayfayı çevirdiğinde nefesin kesildi. Beş yaşındayken gökyüzünden inen o mavi ışık rastgele bir kozmik kaza değildi. Ailen sıradan bir felakette kaybolmamıştı — sen o gece özellikle seçilmiştin."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch3_locket",
                    requiredFlagValue = "faced_past",
                    callbackLabel = "Madalyondaki Koordinat Eşleşti",
                    paragraphs = listOf("Dosyanın arka cebinden ince bir çelik anahtar kartı düştü — üstündeki beş haneli sayı, göğsündeki madalyonun içine kazılı olan o gizemli koordinatın birebir aynısıydı. Annen o madalyonu senin boynuna boşuna takmamıştı; bir gün bu odayı bulmanı istemişti.")
                )
            ),
            decisionTitle = "'Denek 7-A' Gerçeğine Yanıt",
            choicePrompt = "Yirmi yıllık hayatının bir deneyin 'serbest gözlem' aşaması olduğunu öğrendin. İçinde ne yükseliyor?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch6_c3_anger",
                    label = "Soğuk, keskin ve hedefi belli bir öfke — artık kimden hesap soracağını biliyorsun.",
                    toneHint = "Soğuk Öfke",
                    nextNodeId = "ch6_s4_future",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.TONY to 6),
                        redEyeDelta = 1,
                        secretUnlocked = "Proje 7-A Ana Dosyası",
                        setFlags = mapOf("ch6_truth_emotion" to "cold_anger"),
                        impactSummary = "Yirmi yıldır neden kaçtığını artık biliyordun; öfken dağılmadı, keskin bir odağa dönüştü.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c3_numb",
                    label = "Sessiz bir uyuşma — hiçbir şey hissetmiyorsun; büyük kırılmalar bir anda değil, katman katman işlenir.",
                    toneHint = "Sessiz • İçedönük",
                    nextNodeId = "ch6_s4_future",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 9),
                        secretUnlocked = "Proje 7-A Ana Dosyası",
                        setFlags = mapOf("ch6_truth_emotion" to "numb"),
                        impactSummary = "Gece geç saatte Natasha sessizce kahve getirdi: 'Büyük şeyler böyle işlenir — katman katman.'",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c3_relief",
                    label = "Tuhaf ama gerçek bir rahatlama — yirmi yıl sonra ilk kez bir cevabın, bir adın ve bir düşmanın var.",
                    toneHint = "Berraklık • Rahatlama",
                    nextNodeId = "ch6_s4_future",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 9, BookCharacterId.STEVE to 6),
                        avengersTrustDelta = 5,
                        secretUnlocked = "Proje 7-A Ana Dosyası",
                        setFlags = mapOf("ch6_truth_emotion" to "relief"),
                        impactSummary = "Wanda: 'İyi misin?' — 'Hayır. Ama ilk kez ne olduğunu biliyorum. Bu her şeyi değiştiriyor.'",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch6_s3_istanbul_support",
            chapterNumber = 6,
            sceneOrder = 3,
            sceneTitle = "Natasha'nın Yanında Gerçek",
            locationTag = "İSTANBUL • YERALTI ARŞİVİ 7-A • 23:10",
            paragraphs = listOf(
                "Natasha sarnıcın yankılı taş geçitlerinden ekibi tek bir el işaretiyle yönlendirdi. Sen arka kanadı koruyarak havadaki ince uzam kırılmalarını okudun; üç gizli lazer sensörünü daha tetiklenmeden devre dışı bıraktın. Arşiv odasına girdiğinizde Natasha masadaki metal klasörü önce tuzak ihtimaline karşı kendi taradı, sonra gözlerinin içine bakıp tek kelime etmeden sana uzattı.",
                "Klasörün kapağını kaldırdığında yirmi yıl geriye baktın: *PROJE 7-A — DENEK: AIDEN BLACKWOOD. DURUM: SERBEST GÖZLEM. BAŞ ARAŞTIRMACI: DR. EVELYN HALE.*",
                "Satırların arasında senin dokuz yaşındayken ilk ışınlanışının, on beşinde gözlerinin ilk kez beyaza dönüşünün tarihleri bile vardı. Biri hep bir gölge mesafesinde not tutmuştu. Beş yaşındayken mavi ışığın altında kalan yangın merdiveni bir kaza değildi. Sen bir kaçak değildin — yirmi yıl boyunca büyüyüp olgunlaşması izlenen bir projeydin."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch3_locket",
                    requiredFlagValue = "faced_past",
                    callbackLabel = "Madalyondaki Koordinat Eşleşti",
                    paragraphs = listOf("Natasha dosyanın iç kapağındaki sayı dizisini fark edip başını kaldırdı: \"Bu koordinat... senin boynundaki eski madalyondaki sayı değil mi?\" Başını salladın. Annen seni tesadüfen değil, bir gün gerçeği bulman için işaretlemişti.")
                )
            ),
            decisionTitle = "'Denek 7-A' Gerçeğine Yanıt",
            choicePrompt = "Dosya ellerinde ağırlaşırken içinde hangi duygu ağır basıyor?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch6_c3_anger",
                    label = "Soğuk, keskin ve hedefi belli bir öfke — artık kimden hesap soracağını biliyorsun.",
                    toneHint = "Soğuk Öfke",
                    nextNodeId = "ch6_s4_future",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.TONY to 6),
                        redEyeDelta = 1,
                        secretUnlocked = "Proje 7-A Ana Dosyası",
                        setFlags = mapOf("ch6_truth_emotion" to "cold_anger"),
                        impactSummary = "Yirmi yıldır neden kaçtığını artık biliyordun; öfken dağılmadı, keskin bir odağa dönüştü.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c3_numb",
                    label = "Sessiz bir uyuşma — hiçbir şey hissetmiyorsun; büyük kırılmalar bir anda değil, katman katman işlenir.",
                    toneHint = "Sessiz • İçedönük",
                    nextNodeId = "ch6_s4_future",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 9),
                        secretUnlocked = "Proje 7-A Ana Dosyası",
                        setFlags = mapOf("ch6_truth_emotion" to "numb"),
                        impactSummary = "Gece geç saatte Natasha sessizce kahve getirdi: 'Büyük şeyler böyle işlenir — katman katman.'",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c3_relief",
                    label = "Tuhaf ama gerçek bir rahatlama — yirmi yıl sonra ilk kez bir cevabın, bir adın ve bir düşmanın var.",
                    toneHint = "Berraklık • Rahatlama",
                    nextNodeId = "ch6_s4_future",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 9, BookCharacterId.STEVE to 6),
                        avengersTrustDelta = 5,
                        secretUnlocked = "Proje 7-A Ana Dosyası",
                        setFlags = mapOf("ch6_truth_emotion" to "relief"),
                        impactSummary = "Wanda: 'İyi misin?' — 'Hayır. Ama ilk kez ne olduğunu biliyorum. Bu her şeyi değiştiriyor.'",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch6_s3_tower_voss",
            chapterNumber = 6,
            sceneOrder = 3,
            sceneTitle = "Kule'de İki Kırık Ayna",
            locationTag = "AVENGERS KULESİ • 42. KAT",
            paragraphs = listOf(
                "Ekip İstanbul'a uçarken sen 42. katta Voss'un tam karşısına oturdun. Bu kez aranızda tek yönlü ayna ya da çelik cam yoktu. Voss şaşkındı ama gözlerinde derin, neredeyse baba gibi bir anlayış belirdi. \"Sarnıca gitmedin,\" dedi sesi alçaktı. \"Çünkü cevabın yalnızca kâğıtlarda değil, benim hafızamda olduğunu anladın.\"",
                "Kulaklığından Natasha'nın sesi geldiğinde İstanbul'daki ekip mühürlü terminale bağlanmıştı. Voss gözlerini kapatıp yıllardır zihninde kilitli tuttuğu kırk haneli şifre anahtarını tek nefeste okudu. Önündeki ekranda büyük kırmızı harflerle tek bir başlık açıldı:",
                "*PROJE 7-A — DENEK: AIDEN BLACKWOOD. DURUM: SERBEST GÖZLEM. BAŞ ARAŞTIRMACI: DR. EVELYN HALE.*",
                "Beş yaşındaki mavi gökyüzü kaza değildi. Yirmi yıl boyunca kaçtığını sanırken aslında geniş bir kafesin içinde izlenmiştin. Voss başını eğdi: \"Seni uyarmaya geç kaldım, çocuk. Ama hâlâ zamanın var.\""
            ),
            decisionTitle = "'Denek 7-A' Gerçeğine Yanıt",
            choicePrompt = "Gerçek nihayet ekranı doldurduğunda içinde ne hissediyorsun?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch6_c3_anger",
                    label = "Soğuk, keskin ve hedefi belli bir öfke — artık kimden hesap soracağını biliyorsun.",
                    toneHint = "Soğuk Öfke",
                    nextNodeId = "ch6_s4_future",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.TONY to 6),
                        redEyeDelta = 1,
                        secretUnlocked = "Proje 7-A Ana Dosyası",
                        setFlags = mapOf("ch6_truth_emotion" to "cold_anger"),
                        impactSummary = "Yirmi yıldır neden kaçtığını artık biliyordun; öfken dağılmadı, keskin bir odağa dönüştü.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c3_numb",
                    label = "Sessiz bir uyuşma — hiçbir şey hissetmiyorsun; büyük kırılmalar bir anda değil, katman katman işlenir.",
                    toneHint = "Sessiz • İçedönük",
                    nextNodeId = "ch6_s4_future",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 9),
                        secretUnlocked = "Proje 7-A Ana Dosyası",
                        setFlags = mapOf("ch6_truth_emotion" to "numb"),
                        impactSummary = "Gece geç saatte Natasha sessizce kahve getirdi: 'Büyük şeyler böyle işlenir — katman katman.'",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c3_relief",
                    label = "Tuhaf ama gerçek bir rahatlama — yirmi yıl sonra ilk kez bir cevabın, bir adın ve bir düşmanın var.",
                    toneHint = "Berraklık • Rahatlama",
                    nextNodeId = "ch6_s4_future",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 9, BookCharacterId.STEVE to 6),
                        avengersTrustDelta = 5,
                        secretUnlocked = "Proje 7-A Ana Dosyası",
                        setFlags = mapOf("ch6_truth_emotion" to "relief"),
                        impactSummary = "Wanda: 'İyi misin?' — 'Hayır. Ama ilk kez ne olduğunu biliyorum. Bu her şeyi değiştiriyor.'",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── CH6 SCENE 4: WHAT DO YOU WANT NEXT ──
        BookSceneNode(
            id = "ch6_s4_future",
            chapterNumber = 6,
            sceneOrder = 4,
            sceneTitle = "Yirmi Yıldan Sonra İlk İstek",
            locationTag = "AVENGERS KULESİ • 88. KAT • ŞAFAK",
            paragraphs = listOf(
                "Ertesi sabah solgun altın rengi güneş ışığı Manhattan'ın cam kulelerine vururken tüm ekip yeniden büyük masanın etrafındaydı. İstanbul'dan gelen şifreli dosyalar, enerji şemaları ve eski fotoğraflar masaya yayılmıştı; ama kimse yeni bir plan dikte etmiyordu. Odadaki beş çift göz de tek bir kişinin sözünü bekliyordu.",
                "Steve Rogers masanın etrafından dolaşıp sana doğru bir adım attı. Sesinde komutan tınısı yoktu; yalnızca seni gerçekten gören bir dostun ağırlığı vardı.",
                "\"Aiden,\" dedi. \"Yirmi yıldır başkalarının yazdığı bir hikâyeden kaçtın. Şu an kapılar açık, dosya masada ve bu oda senin yanında. Kimse seni bir şeye zorlamıyor. Söyle bize — bundan sonra *sen* ne istiyorsun?\""
            ),
            decisionTitle = "'Bundan Sonra Ne İstiyorsun?'",
            choicePrompt = "Tüm ekibin önünde kendi geleceğine dair ilk gerçek kararını ver.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch6_c4_hunt",
                    label = "\"Kör Nokta'yı bulmak istiyorum. Ve bu projeyi sonsuza kadar bitirmek.\"",
                    toneHint = "Kararlı • Taarruz",
                    nextNodeId = "ch6_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.CLINT to 10, BookCharacterId.TONY to 8, BookCharacterId.STEVE to 6, BookCharacterId.THOR to 6),
                        avengersTrustDelta = 8,
                        setFlags = mapOf("ch6_wish" to "hunt"),
                        impactSummary = "Clint gülümsedi: 'Hoş geldin Blackwood — gerçekten bu sefer.' Ekip tek yürek oldu.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c4_stay",
                    label = "\"Bilmiyorum henüz. Ama... burada kalmak istiyorum. İlk kez gitmek istemiyorum.\"",
                    toneHint = "Duygusal • Aidiyet",
                    nextNodeId = "ch6_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 12, BookCharacterId.NATASHA to 9, BookCharacterId.WANDA to 9),
                        avengersTrustDelta = 10,
                        setFlags = mapOf("ch6_wish" to "stay"),
                        impactSummary = "Steve elini uzattı: 'Yeter bu.' Yirmi yılda ilk kez uzatılan bir eli geri bırakmak istemedin.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch6_c4_save_voss",
                    label = "\"Voss'u bu kâbustan kurtarmak istiyorum. Sonra birlikte Kör Nokta'nın kapısına gideriz.\"",
                    toneHint = "Merhamet • Kurtarış",
                    nextNodeId = "ch6_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 12, BookCharacterId.STEVE to 8, BookCharacterId.CLINT to 6, BookCharacterId.BRUCE to 6),
                        avengersTrustDelta = 8,
                        setFlags = mapOf("ch6_wish" to "save_voss"),
                        impactSummary = "Wanda'nın gözleri doldu: artık kaçmak için değil, birini kurtarmak için yürüyordun.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch6_s5_end",
            chapterNumber = 6,
            sceneOrder = 5,
            sceneTitle = "Kapıdaki Ziyaretçi",
            locationTag = "AVENGERS KULESİ • LİMAN GİRİŞİ • 08:22",
            paragraphs = listOf(
                "Tam toplantı dağılırken odadaki tüm cam paneller bir anlığına titredi ve FRIDAY'in acil bildirim ışığı soğuk bir maviye döndü: \"Bay Stark, ana lobide silahsız bir kadın güvenlikten geçmeden teslim olmak istiyor. Sadece Aiden Blackwood ile konuşacağını söylüyor.\"",
                "Tony parmağını havada kaydırıp güvenlik kamerasını masanın üstüne yansıttığında odadaki herkes olduğu yerde dondu. Yağmurdan ıslanmış gri saçlı, ince çerçeveli gözlüklü, iki elini yavaşça baş hizasına kaldırmış yaşlı bir kadın doğrudan kameranın lensine bakıyordu.",
                "İstanbul sarnıcındaki dosyanın ikinci sayfasındaki fotoğrafın, o eğik imzanın birebir aynısıydı: *Dr. Evelyn Hale.*"
            ),
            isChapterEnd = true,
            chapterEndQuote = "Kırık ayna parçalandı. Şimdi seni yaratan el, kendi ayağıyla kapına geldi."
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 7: KARANLIK AYNA
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch7_s1",
            chapterNumber = 7,
            sceneOrder = 1,
            sceneTitle = "Dr. Evelyn Hale",
            locationTag = "AVENGERS KULESİ • YÜKSEK GÜVENLİK GÖRÜŞME ODASI",
            paragraphs = listOf(
                "Dr. Evelyn Hale beklediğin gibi soğuk, acımasız bir laboratuvar canavarı gibi görünmüyordu — ve bu onu zihninde büyüttüğün her kabustan çok daha rahatsız edici kılıyordu. Altmışlı yaşlarında, ince parmakları masanın kenarında hafifçe titreyen, koyu paltosunun yakasında hâlâ sabah yağmurunun damlaları parlayan yorgun bir bilim insanıydı.",
                "Masanın karşısında otururken gözleri doğrudan senin yüzüne, sonra belli belirsiz bir saniyeliğine göz bebeklerinin derinliğine kaydı — sanki içinde hâlâ o mavi kıvılcımın yaşayıp yaşamadığını ölçüyordu.",
                "\"Yirmi yıl,\" diye fısıldadı; sesinde hem suçluluk hem de tuhaf bir hayret vardı. \"Büyümüşsün, Aiden.\"",
                "Tek yönlü camın arkasında Steve, Natasha ve Tony hazırdı; odanın loş köşesinde ise Wanda sessizce duruyor, parmaklarının ucunda henüz yanmamış kızıl bir kıvılcımla senin tek bir işaretini bekliyordu."
            ),
            decisionTitle = "Dr. Hale'e Yaklaşım",
            choicePrompt = "Seni beş yaşında mavi ışığın altına sürükleyen kadın karşında. İlk hamlen ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch7_c1_ask",
                    label = "Doğrudan gözlerinin içine bak ve sor: \"Benden ne istiyorsun, Hale?\"",
                    toneHint = "Doğrudan • Keskin",
                    nextNodeId = "ch7_s2_regret",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 5),
                        setFlags = mapOf("ch7_hale_open" to "direct"),
                        impactSummary = "Hale: 'Seni tamamlamak için geldim.' O kelime ağzında buz gibi yanlış hissettirdi.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c1_step_out",
                    label = "Tek kelime etmeden odadan çık — üç dakika nefes al, hazmet, sonra kendi hızınla geri dön.",
                    toneHint = "Kontrollü • Denge",
                    nextNodeId = "ch7_s2_regret",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 7, BookCharacterId.NATASHA to 6),
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch7_hale_open" to "breathed"),
                        impactSummary = "Kaçmak yerine geri döndün; kontrolün artık tamamen sende olduğunu kanıtladın.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c1_wanda",
                    label = "Wanda'ya dönüp sessizce başını salla: \"Önce zihnini oku, sonra konuşalım.\"",
                    toneHint = "Zihinsel Doğrulama",
                    nextNodeId = "ch7_s2_regret",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 10, BookCharacterId.TONY to 4),
                        secretUnlocked = "Hale'in Gerçek Korkusu",
                        setFlags = mapOf("ch7_hale_open" to "wanda_read"),
                        impactSummary = "Wanda fısıldadı: 'Gerçekten pişman. Ve ölesiye korkuyor — senden değil, İstanbul'da uyandırdıkları şeyden.'",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch7_s2_regret",
            chapterNumber = 7,
            sceneOrder = 2,
            sceneTitle = "'Pişmanım'",
            locationTag = "GÖRÜŞME ODASI • AVENGERS KULESİ",
            paragraphs = listOf(
                "Dr. Hale ince parmaklarını birbirine kenetledi. \"Altı gün içinde Kör Nokta, İstanbul tünellerinin derinindeki *Rezonans Kulesi*'ni ateşleyecek,\" dedi sesi hızlanırken. \"Cihaz senin Kırmızı Göz frekansını uzaktan tetikleyip iradeni kilitlemek için tasarlandı. Ve yalnız değilsin, Aiden — projeden sağ çıkan bir denek daha var. Onu kuleye bağladılar.\"",
                "Masaya şifreli bir bellek diski bıraktı. \"Kuleyi durdurmak için frekans kesiciyi bizzat çekirdeğe yerleştirmeniz gerekiyor. Aksi halde sadece sen değil, Boğaz'ın altındaki tüm enerji hattı çöker.\"",
                "Başını hafifçe eğdiğinde gözlüğü buğulandı, sesi çatallandı: \"Beş yaşındaki o gece... her şey kontrolden çıktığında seni kaçırdığımı sanıyorlardı. Hayır, Aiden. Seni onlardan sakladım. Ve bütün bunları başlattığım için... pişmanım.\""
            ),
            decisionTitle = "'Pişmanım' Sözüne Tepki",
            choicePrompt = "Hale'in 'pişmanım' sözü havada asılı. Ona ne soracaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch7_c2_cold",
                    label = "\"Pişmanlık yetmez, Hale. Aktivasyon protokolünü, tetikleyici frekansı — her şeyi anlat.\"",
                    toneHint = "Soğuk • Stratejik",
                    nextNodeId = "ch7_s3_plan",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 8, BookCharacterId.NATASHA to 6),
                        avengersTrustDelta = 5,
                        secretUnlocked = "Aktivasyon Protokolü Şeması",
                        setFlags = mapOf("ch7_regret_reply" to "protocol"),
                        impactSummary = "Hale tüm teknik şemayı ve frekans kesici şifresini eksiksiz biçimde masaya döktü.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c2_parents",
                    label = "Beklenmedik tek bir soru sor: \"Annem ve babam... o gece gerçekten öldüler mi, yoksa hayattalar mı?\"",
                    toneHint = "Dallanma • Aile Sırrı",
                    nextNodeId = "ch7_s3_plan",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 10, BookCharacterId.WANDA to 8, BookCharacterId.STEVE to 8),
                        avengersTrustDelta = 7,
                        secretUnlocked = "Prag'daki Aile Konumu",
                        setFlags = mapOf("ch7_regret_reply" to "parents_alive"),
                        branchTag = "Prag Aile Kurtarma İzi",
                        impactSummary = "Hale'in gözleri doldu: 'Hayattalar. Prag'da, gizli gözetim altındalar.' Natasha anında konumu aldı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c2_enough",
                    label = "Ayağa kalk ve kapıya yürü: \"Bu kadar bilgi yeterli. Altı gün sonra ne yapacağımızı biz seçeriz.\"",
                    toneHint = "Mesafeli • Kararlı",
                    nextNodeId = "ch7_s3_plan",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5),
                        mysteryDelta = 1,
                        setFlags = mapOf("ch7_regret_reply" to "walked_out"),
                        impactSummary = "Hale'in vicdanını rahatlatmasına izin vermedin; hazırlıklar için hemen masaya döndün.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch7_s3_plan",
            chapterNumber = 7,
            sceneOrder = 3,
            sceneTitle = "Savaş Masası",
            locationTag = "AVENGERS KULESİ • 88. KAT TAKTİK SALON",
            paragraphs = listOf(
                "İstanbul tünellerinin üç boyutlu mavi hologramı masanın üstünde ağır ağır dönüyordu. Tony Stark, Dr. Hale'in verilerinden avuç içi büyüklüğünde, koyu mor çekirdekli bir *Frekans Bastırıcı* üretmişti. Cihazın merkez rezonans kulesine doğrudan temas etmesi gerekiyordu — aksi halde dalga tüm Boğaz hattını ve senin sinir sistemini geri dönüşsüz biçimde yakacaktı.",
                "\"İçerideki koruma alanı yoğun,\" dedi Steve, hologramdaki üç ana giriş tünelini işaret ederek. \"Kör Nokta ne kadar güçlü olduğumuzu biliyor. Nasıl gireceğimize şimdi burada karar veriyoruz.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch7_regret_reply",
                    requiredFlagValue = "parents_alive",
                    callbackLabel = "Prag Kurtarma Sözü",
                    paragraphs = listOf("Natasha masanın ucundan sana hafifçe başını salladı; gözlerinde sarsılmaz bir kararlılık vardı: \"Prag'daki güvenilir temasımız konum teyidini verdi, Aiden. İstanbul biter bitmez annenle babanı güvene alıyoruz. Söz veriyorum.\"")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch6_wish",
                    requiredFlagValue = "stay",
                    callbackLabel = "Kalma Kararın",
                    paragraphs = listOf("Steve elini masanın kenarına koyup sana baktı: \"Dün sabah ilk kez burada kalmak istediğini söyledin. O yüzden yarın gece herkes tek parça geri dönecek.\"")
                )
            ),
            decisionTitle = "Cihaz & Operasyon Kararı",
            choicePrompt = "İstanbul tünellerindeki büyük operasyonun ana stratejisini seç.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch7_c3_solo",
                    label = "\"Tek başıma içeri ışınlanıp cihazı takacağım — diğerleri dış çemberi tutsun.\"",
                    toneHint = "Yalnız Taarruz",
                    nextNodeId = "ch7_s4_elevator",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.CLINT to 6, BookCharacterId.STEVE to 2),
                        setFlags = mapOf("ch7_op_style" to "solo"),
                        impactSummary = "Clint omzuna vurdu: 'Sırtın kollanıyor, vaat.' Steve ise kaygıyla gözlerinin içine baktı.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c3_team",
                    label = "\"Birlikte giriyoruz — tam ekip planı. Artık tek başıma savaşmıyorum.\"",
                    toneHint = "Ekip Birliği",
                    nextNodeId = "ch7_s4_elevator",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 10, BookCharacterId.NATASHA to 7, BookCharacterId.CLINT to 8, BookCharacterId.TONY to 6, BookCharacterId.BRUCE to 6, BookCharacterId.THOR to 6),
                        avengersTrustDelta = 10,
                        setFlags = mapOf("ch7_op_style" to "team"),
                        impactSummary = "İlk kez o masanın etrafında gerçekten onlardan biri gibi oturdun.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c3_bait",
                    label = "\"Beni yem olarak kullanın — frekansımı açıp bütün savunmayı üstüme çekeyim, ekip çekirdeği indirsin.\"",
                    toneHint = "Dallanma • Yem Stratejisi",
                    nextNodeId = "ch7_s4_elevator",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 9, BookCharacterId.WANDA to 7, BookCharacterId.TONY to 7),
                        fatigueDelta = 1,
                        avengersTrustDelta = 8,
                        setFlags = mapOf("ch7_op_style" to "bait"),
                        branchTag = "Yem Operasyonu",
                        impactSummary = "'Bu sefer kaçmak için değil.' Steve uzun süre sana derin bir saygıyla baktı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── CH7 SCENE 4: WANDA ELEVATOR ──
        BookSceneNode(
            id = "ch7_s4_elevator",
            chapterNumber = 7,
            sceneOrder = 4,
            sceneTitle = "Asansördeki Sessizlik",
            locationTag = "AVENGERS KULESİ • GECE ASANSÖRÜ",
            paragraphs = listOf(
                "Operasyondan bir gece önce, katlar arasında neredeyse hiç ses çıkarmadan süzülen cam asansörde Wanda ile yalnız kaldın. Aşağıda Manhattan'ın milyonlarca ışığı cam duvarın ardında ağır ağır akıp gidiyordu; sanki bütün şehir nefesini tutmuştu.",
                "Wanda yanında dururken omzu senin koluna santimler kadar yakındı. Zihnini okumadığını biliyordun — bunu haftalar önce sana verdiği sessiz sözle bırakmıştı — ama yine de insanı çıplak bırakan o yumuşak bakışla sana döndü.",
                "\"İlk geldiğin gece zihninde sadece duvarlar, kilitli odalar ve kaçış haritaları vardı, Aiden,\" dedi fısıltıyla. \"Bugün ise... başka bir şey var. Merak. Sanki yarını gerçekten görmek istiyorsun.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "early_interest",
                    requiredFlagValue = "wanda",
                    callbackLabel = "İlk Geceden Kalan Bakış",
                    paragraphs = listOf("Wanda'nın dudaklarında ince, neredeyse kırılgan bir tebessüm belirdi: \"Ve biliyor musun? O ilk gece pencerenin kenarında dururken bana baktığın saniyeden beri, o duvarların bir gün düşeceğini biliyordum.\"")
                )
            ),
            decisionTitle = "Wanda'ya Asansör Cevabı",
            choicePrompt = "Wanda'nın bu sessiz sorusuna nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch7_c4_share",
                    label = "\"Merakı yıllarca bir lüks saydım, Wanda. Belki yanılıyormuşum — belki gitmemeye çalışıyorum.\"",
                    toneHint = "İçten • Yakın",
                    nextNodeId = "ch7_s5_rooftop",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 12),
                        setFlags = mapOf("ch7_wanda_elevator" to "shared"),
                        impactSummary = "Wanda hafifçe gülümsedi: 'Bu yeterli, Aiden. Gerçekten yeterli.'",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c4_silent",
                    label = "Hiçbir şey söyleme — kapı açıldığında sessiz bir baş selamıyla çık.",
                    toneHint = "Sessiz • Mesafeli",
                    nextNodeId = "ch7_s5_rooftop",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.WANDA to -2),
                        setFlags = mapOf("ch7_wanda_elevator" to "silent"),
                        impactSummary = "Koridorda Natasha bekliyordu; kelime gerekmeden omuz omuza yürüdünüz.",
                        isBranching = false,
                        isRelationship = false,
                        impactLevel = "dusuk"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c4_ask_back",
                    label = "\"Peki sen ne hissediyordun, Wanda? İlk kez benim zihnime dokunduğunda?\"",
                    toneHint = "Derin • Karşılıklı",
                    nextNodeId = "ch7_s5_rooftop",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 14),
                        setFlags = mapOf("ch7_wanda_elevator" to "asked_back"),
                        impactSummary = "Wanda: 'Zihnin çok yer kaplıyor. Sanki yıllardır açılmayı bekleyen kocaman bir gökyüzü gibi.'",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── CH7 SCENE 5: NATASHA ROOFTOP ──
        BookSceneNode(
            id = "ch7_s5_rooftop",
            chapterNumber = 7,
            sceneOrder = 5,
            sceneTitle = "Çatıda Sabaha Doğru",
            locationTag = "AVENGERS KULESİ • ÇATI TERASI • 03:40",
            paragraphs = listOf(
                "Uçuşa üç saat kala gözüne uyku girmedi. Merdivenlerden çatı terasına çıktığında gece rüzgârı soğuktu ama gökyüzü yıldızlarla açılmıştı. Natasha çoktan oradaydı — siyah deri ceketi rüzgârda hafifçe dalgalanıyor, elinde buharı tüten iki sıcak çay bardağı tutuyordu. Birini sana uzattı.",
                "Bir süre ikiniz de konuşmadan uyuyan New York'u izlediniz. Bazen bir insanla sessizce durabilmek, saatlerce konuşmaktan daha dürüst olurdu.",
                "\"Biliyor musun,\" dedi Natasha sonunda, ufkun morarmaya başlayan ince çizgisine bakarak. \"Artık bir odaya girdiğinde çıkışları saymayı bıraktın, Blackwood. Fark ettin mi? Kaçış rotası kurmuyorsun artık.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "early_interest",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Natasha ile Derin Bağ",
                    paragraphs = listOf("Sana yan gözle baktığında o her zamanki profesyonel zırhından eser yoktu. \"İlk gece salona ışınlandığında gözlerin bende bir saniye fazla kalmıştı,\" diye mırıldandı. \"O zaman bunu tehlike taraması sanmıştım. Yanılmışım.\"")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch3_rooftop",
                    requiredFlagValue = "sincere",
                    callbackLabel = "Çatıdaki İkinci Gece",
                    paragraphs = listOf("\"Aynı teras, aynı gece rüzgârı,\" dedi hafifçe gülerek. \"Ama bu sefer Kızıl Oda'dan da, kaçıştan da saklanmıyoruz.\"")
                )
            ),
            decisionTitle = "Çatıda Natasha İle Gece",
            choicePrompt = "Natasha'nın bu gözlemine ne söyleyeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch7_c5_confirm",
                    label = "\"Haklısın, Natasha. Artık kaçmayı düşünmüyorum.\"",
                    toneHint = "Sıcak • Net",
                    nextNodeId = "ch7_s6_tunnels",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 13),
                        setFlags = mapOf("ch7_natasha_roof" to "confirmed"),
                        impactSummary = "Natasha'nın omzu senin omzuna değdi. Saat dörde kadar tek kelime etmediniz ama ikiniz de gitmediniz.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c5_honest",
                    label = "\"Hâlâ düşünüyorum. Ama bu sefer gitmek istemiyorum.\"",
                    toneHint = "Gerçekçi • İçten",
                    nextNodeId = "ch7_s6_tunnels",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 11),
                        setFlags = mapOf("ch7_natasha_roof" to "honest"),
                        impactSummary = "Natasha: 'İşte o fark her şeydir.' Sesin o gece daha az hesaplı, daha gerçekti.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c5_silence",
                    label = "Sessiz kal — sadece yan yana durup uyuyan New York'u izleyin.",
                    toneHint = "Sessiz Eşlik",
                    nextNodeId = "ch7_s6_tunnels",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 8),
                        setFlags = mapOf("ch7_natasha_roof" to "silence"),
                        impactSummary = "Bazı cevaplar kelimeye dökülmez; Natasha başını hafifçe eğip çayından bir yudum aldı.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── CH7 SCENE 6: ISTANBUL TUNNELS & MIRA (CLIMAX) ──
        BookSceneNode(
            id = "ch7_s6_tunnels",
            chapterNumber = 7,
            sceneOrder = 6,
            sceneTitle = "Karanlık Ayna: Mira",
            locationTag = "İSTANBUL • YERALTI REZONANS TÜNELLERİ • 02:55",
            paragraphs = listOf(
                "Boğaz'ın altındaki antik taş tüneller, modern çelik kablolar ve nabız gibi atan koyu kızıl bir uğultuyla titriyordu. Dış geçitte Steve, Natasha, Wanda, Clint ve yıldırımlarıyla taş koridorları aydınlatan Thor, Kör Nokta muhafızlarının hattını yararken sen elinde Tony'nin Frekans Bastırıcısı ile merkez çekirdek odasına ulaştın.",
                "Ama oda boş değildi. Devasa rezonans kulesinin hemen önünde, yirmili yaşlarının başında genç bir kadın bekliyordu. Zayıf omuzları ağır siyah bir saha paltosunun içindeydi; saçları şakaklarına yapışmıştı. Başını sana kaldırdığı an kalbin duracak gibi oldu.",
                "Gözleri koyu, yanan bir *kırmızıydı.* Tıpkı on yedi yaşındayken aynada gördüğün, bir daha uyanmasın diye yıllarca bastırdığın o kâbus gibi.",
                "\"Aiden,\" dedi sesi titreyerek ama ürkütücü bir güçle yankılanarak. \"Ben Mira. Denek 7-B. Onlar seni serbest bıraktılar... beni ise yirmi yıl boyunca bu karanlıkta tuttular. Şimdi kule açılacak ve ilk kez bana bakan biri olacak.\"",
                "Etrafındaki taş duvarlar kırmızı enerjiyle çatlamaya başladı. Kule otuz saniye içinde ateşlenmek üzereydi. Kulaklığında Steve'in gergin sesi duyuldu: \"Aiden, cihazı hemen yerleştir — yoksa tüneller hepimizin üstüne çökecek!\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "has_stark_band",
                    requiredFlagValue = "accepted",
                    callbackLabel = "Stark Bilekliği Koruma Kalkanı",
                    paragraphs = listOf("Bileğindeki titanyum halka parlak bir mavi halka çizerek devreye girdi; Mira'nın yaydığı boğucu kırmızı dalganın sinir sistemini kilitlemesini son anda engelledi.")
                ),
                ConditionalNarrative(
                    minRedEye = 2,
                    callbackLabel = "Kırmızı Göz Yankısı",
                    paragraphs = listOf("Kendi içindeki bastırılmış Kırmızı Göz, Mira'nın frekansına şiddetle yanıt verdi. Gözlerinin kenarında kızıl bir ısı yükseldi; *onun acısını yalnızca kelimelerden değil, doğrudan damarlarından okuyabiliyordun.*")
                )
            ),
            decisionTitle = "Mira ve Rezonans Kulesi Finali",
            choicePrompt = "Karşında senin karanlık aynan duruyor. Son kararını ver.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch7_c6_reach_out",
                    label = "Cihazı beline tak, elini uzat ve Beyaz Göz'le onun zihnine dokun: \"Yalnız değilsin, Mira. Seni burada bırakmıyorum.\"",
                    toneHint = "Dallanma • Empati & Kurtarış",
                    nextNodeId = "ch7_s7_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 12, BookCharacterId.STEVE to 10, BookCharacterId.NATASHA to 6),
                        fatigueDelta = 1,
                        avengersTrustDelta = 10,
                        secretUnlocked = "Denek 7-B: Mira'nın Kurtuluşu",
                        setFlags = mapOf("ch7_mira_choice" to "saved"),
                        branchTag = "Mira'yı Kurtarma Finali",
                        impactSummary = "Mira'nın kırmızısı ilk kez söndü; kuleyi birlikte kapattınız ve yirmi yıl sonra ilk kez birine el uzattın.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c6_device_first",
                    label = "Zaman yok — uzamı büküp doğrudan çekirdeğe ışınlan, Frekans Bastırıcı'yı yerleştir ve kuleyi anında kapat.",
                    toneHint = "Dallanma • Görev Önceliği",
                    nextNodeId = "ch7_s7_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 10, BookCharacterId.NATASHA to 9, BookCharacterId.STEVE to 7),
                        avengersTrustDelta = 9,
                        secretUnlocked = "Rezonans Kulesi Kapatıldı",
                        setFlags = mapOf("ch7_mira_choice" to "shutdown_first"),
                        branchTag = "Taktik Kapatma Finali",
                        impactSummary = "Kule son saniyede sustu; Mira şok dalgasıyla geri savruldu ama hayatta kaldı ve karanlığa karıştı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c6_red_mirror",
                    label = "Kendi Kırmızı Göz'ünü bilinçli olarak aç — onun dalgasını kendi üzerinde söndürerek ekibi ve şehri koru.",
                    toneHint = "Dallanma • Kırmızı Göz Yüzleşmesi",
                    nextNodeId = "ch7_s7_aftermath",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 8, BookCharacterId.STEVE to 8, BookCharacterId.CLINT to 8, BookCharacterId.THOR to 8),
                        fatigueDelta = 2,
                        redEyeDelta = 2,
                        mysteryDelta = 2,
                        avengersTrustDelta = 7,
                        secretUnlocked = "Kırmızı Göz Hakimiyeti",
                        setFlags = mapOf("ch7_mira_choice" to "red_eye_absorb"),
                        branchTag = "Kırmızı Göz Uyanışı Finali",
                        impactSummary = "İki kırmızı frekans çarpıştı — ilk kez korkmadan kendi karanlığına hükmettin ve kuleyi parçaladın.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── CH7 SCENE 7: DAWN OVER BOSPHORUS ──
        BookSceneNode(
            id = "ch7_s7_aftermath",
            chapterNumber = 7,
            sceneOrder = 7,
            sceneTitle = "Boğaz'da Şafak",
            locationTag = "İSTANBUL • QUİNJET İNİŞ NOKTASI • 06:05",
            paragraphs = listOf(
                "Tünellerin çıkışı taze, tuzlu bir Boğaz rüzgârına açıldığında gökyüzü erguvani bir şafakla aydınlanıyordu. Kulaklığında Tony'nin rahatlamış sesi yankılandı: \"Rezonans sıfırlandı. Tüm İstanbul şebekesi normal. Ve sen hâlâ tek parçasın, Blackwood.\"",
                "Steve elini omzuna koyduğunda ilk kez refleksle geri çekilmedin. Natasha az ileride Quinjet rampasını açmış bekliyor, Wanda ise yorgun gözlerle ama içten bir gülümsemeyle sana bakıyordu. Clint yayını omzuna asıp sırt çantanı sana fırlattı: \"Kahvaltı benden, uzaylı.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch7_mira_choice",
                    requiredFlagValue = "saved",
                    callbackLabel = "Mira'nın Yeni Başlangıcı",
                    paragraphs = listOf("Quinjet'in içinde, kalın bir battaniyeye sarılı Mira camdan ilk kez gerçek gün ışığına bakıyordu. Gözleri artık kırmızı değil, yorgun bir kahverengiydi. \"Beni neden bırakmadın?\" diye fısıldadığında cevabın basitti: \"Çünkü bir zamanlar kimse bana el uzatmamıştı.\"")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_mira_choice",
                    requiredFlagValue = "shutdown_first",
                    callbackLabel = "Mira'nın İzi",
                    paragraphs = listOf("Tünelin çıkışında yerde kırık bir kolye ucu kalmıştı. Mira kaçmıştı ama hayattaydı — ve bir gün yollarınızın yeniden kesişeceğini biliyordun.")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_mira_choice",
                    requiredFlagValue = "red_eye_absorb",
                    callbackLabel = "Kırmızı Göz Dengesi",
                    paragraphs = listOf("Ellerin hâlâ hafifçe titriyordu ama gözlerin duruydu. On yedi yaşından beri bir canavar gibi sakladığın o kırmızı gücü ilk kez birini korumak için kullanmış ve kontrolü kaybetmemiştin.")
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_regret_reply",
                    requiredFlagValue = "parents_alive",
                    callbackLabel = "Prag'dan Gelen Sinyal",
                    paragraphs = listOf("Natasha telefon ekranını sana çevirdi; ekranda Prag'dan gelen şifreli tek bir onay satırı parlıyordu: *HEDEF GÜVENDE. KAPI AÇIK.* Yirmi yıl sonra ilk kez boğazında düğümlenen şey korku değildi.")
                )
            ),
            decisionTitle = "Yeni Sayfanın İlk Adımı",
            choicePrompt = "Quinjet'in rampası açık. Eve — Avengers Kulesi'ne — dönerken aklındaki son düşünce ne?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch7_c7_home",
                    label = "Rampadan yukarı yürü ve ilk kez yüksek sesle söyle: \"Eve dönelim.\"",
                    toneHint = "Aidiyet • Huzur",
                    nextNodeId = "ch7_s8_finale",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 8, BookCharacterId.NATASHA to 8, BookCharacterId.WANDA to 8, BookCharacterId.CLINT to 6, BookCharacterId.TONY to 6, BookCharacterId.BRUCE to 6, BookCharacterId.THOR to 6),
                        avengersTrustDelta = 10,
                        setFlags = mapOf("ch7_ending_vow" to "home"),
                        impactSummary = "Yirmi yıllık kaçış burada bitti. Artık bir adın, bir ekibin ve döneceğin bir yerin var.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch7_c7_unfinished",
                    label = "\"Kör Nokta'nın başı hâlâ dışarıda. Bugün kazandık — ama yarın bitireceğiz.\"",
                    toneHint = "Kararlı • Devam",
                    nextNodeId = "ch7_s8_finale",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 8, BookCharacterId.NATASHA to 8, BookCharacterId.STEVE to 6),
                        avengersTrustDelta = 8,
                        mysteryDelta = 1,
                        setFlags = mapOf("ch7_ending_vow" to "unfinished"),
                        impactSummary = "Bakışların ufka kilitlendi; ikinci perde için artık çok daha güçlü ve hazırsın.",
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch7_s8_finale",
            chapterNumber = 7,
            sceneOrder = 8,
            sceneTitle = "Yıldızların Altında Yeni Bir İsim",
            locationTag = "BULUTLARIN ÜSTÜNDE • QUİNJET • DÖNÜŞ ROTASI",
            paragraphs = listOf(
                "Quinjet İstanbul'un şafak bulutlarını yarıp gökyüzüne yükselirken camdan dışarı baktın. Gökyüzü bu sefer mavi bir kâbus değildi — derin, berrak bir sabah moruydu.",
                "Beş yaşındayken elini bıraktığın o geceyi değiştiremezdin. Ama yirmi beş yaşında kimin yanında duracağını, hangi kapıyı açacağını ve kimin elini tutacağını artık yalnızca *sen* seçiyordun.",
                "İlk yedi bölümün perdesi burada kapanırken, arka planda kurduğun tüm bağlar, açtığın sırlar ve verdiğin kararlar Hikâye Günlüğü'ne işlendi. Kör Nokta'nın gerçek mimarı ve Bölüm 8 çok yakında açılacak."
            ),
            isChapterEnd = true,
            chapterEndQuote = "Artık gökyüzünden kaçmıyorsun, Aiden Blackwood. Kendi yıldız haritanı çiziyorsun."
        )
    ).associateBy { it.id }
}
