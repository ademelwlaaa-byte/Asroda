package com.example.book

/**
 * SEASON 2 — CHAPTERS 13 & 14 of "Kozmik Sürükleniş — 2. Sezon: Demir ve Yankı"
 *
 * Chapter 13 ("Donmuş Zaman ve Kâbuslar"):
 * - TIME FREEZES on the African salvage ship: **ASIL MİMAR** appears face-to-face as a chilling
 *   gold-blue cosmic projection speaking directly to Aiden ("Kayıp Anahtarım").
 * - Vibranium Dampener genuinely wounds Aiden (grounded, vulnerable — NOT OP!).
 * - Mind Stone psychic nightmare wave hits the Avengers.
 * - **SIFIR (Scarred Future Aiden)** blinks in briefly to save Aiden from Ultron's execution shot
 *   and shouts "Onu sen yarattın!" at Tony Stark before vanishing!
 *
 * Chapter 14 ("Barton Çiftliği"):
 * - Clint Barton's secret family farm — warmth, healing, Steve-Tony wood-chopping tension, Nick Fury returns!
 * - **ROMANCE STAGE 2 — FIRST REAL KISS!**
 *     • Rainy wooden porch with Natasha after her Red Room nightmare -> genuine, deeply earned first kiss!
 *     • Starlit apple orchard with Wanda after fearing for Pietro -> genuine, deeply earned first kiss!
 *     • Or heartfelt Avengers brotherhood path with Steve & Clint (zero forced romance!).
 * - Strategic choice with Nick Fury on revealing Sıfır & Asıl Mimar to the full team.
 */
object CosmicDriftS2Chapters13To14 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 13,
            romanNumeral = "XIII",
            title = "Donmuş Zaman ve Kâbuslar",
            titleEn = "Frozen Time and Nightmares",
            subtitle = "BÖLÜM XIII • Asıl Mimar ile Yüz Yüze, Vibranyum Yarası ve Sıfır'ın İlk Kurtarışı",
            description = "Afrika gemisinde zaman donar: Asıl Mimar ilk kez doğrudan Aiden'la konuşur. Ultron'un Vibranyum kesicisi Aiden'ı gerçekten yaralar, ekip kâbuslarla sarsılır ve yara izli Sıfır şok edici bir kurtarışla sahneye iner.",
            descriptionEn = "Time freezes on the African salvage ship: The Architect confronts Aiden face-to-face. Ultron's Vibranium dampener wounds Aiden, nightmares shatter the team, and scarred Zero makes a shocking rescue.",
            estimatedMinutes = 16,
            accentHex = 0xFF60A5FAL,
            startNodeId = "ch13_s1",
            sceneCount = 5,
            decisionCount = 4
        ),
        BookChapterMeta(
            number = 14,
            romanNumeral = "XIV",
            title = "Barton Çiftliği",
            titleEn = "Barton's Farm",
            subtitle = "BÖLÜM XIV • Yağmurlu Sığınak, Nick Fury'nin Dönüşü ve İlk Öpücük",
            description = "Sarsılmış Avengers, Clint'in gizli aile çiftliğine sığınır. Fırtına öncesi bu sakin gecede Fury ekibi ayağa kaldırır; yağmurlu verandada Natasha ya da yıldızlı bahçede Wanda ile kalbin mühürlendiği o unutulmaz ilk öpücük yaşanır.",
            descriptionEn = "The shaken Avengers shelter at Clint's hidden farm. Fury rallies the team, and on this quiet night before the storm comes an unforgettable, deeply earned first kiss with Natasha or Wanda — or steadfast brotherhood.",
            estimatedMinutes = 16,
            accentHex = 0xFFF472B6L,
            startNodeId = "ch14_s1",
            sceneCount = 5,
            decisionCount = 4
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 13 — DONMUŞ ZAMAN: ASIL MİMAR İLE YÜZ YÜZE (Karar 1/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch13_s1",
            chapterNumber = 13,
            sceneOrder = 1,
            sceneTitle = "Tek Çizgi — Asıl Mimar'la Yüz Yüze",
            locationTag = "AFRİKA HURDA GEMİSİ · DONMUŞ SANİYE • 15:12:00",
            paragraphs = listOf(
                "Hava bir anda cam gibi sertleşti. Ultron'un namlusundan çıkmak üzere olan kızıl plazma kıvılcımı, namlunun iki santim önünde donmuş bir yakut damlası gibi asılı kaldı. Paslı tavan kirişlerinden dökülen tozlar yere düşmeyi bıraktı. Alevler hareket etmiyordu. Yalnızca sen nefes alabiliyordun — ama kaburgalarının altındaki Vibranyum sızısı yüzünden dizlerinin üstünde zor duruyordun.",
                "[[ANIM|BLUE_SKY_2006|DONMUŞ ZAMAN · ASIL MİMAR'IN YANKISI|Gemideki tüm ses ve hareket durdu; karanlık ambarın ortasında altın-mavi tek bir çizgi gökyüzü gibi yarıldı.]]",
                "Donmuş Ultron'un tam arkasında, havada kusursuz bir dikey ışık çizgisi açıldı. İçinden zırhlı bir canavar ya da dev bir uzaylı çıkmadı — tam tersine, uzun, dingin, gece mavisi ve solgun altın dokulu bir siluet süzüldü. Yüz hatları ışığın içinde sürekli hafifçe kayıyordu; sanki insan zihni onun gerçek biçimini tam olarak kavrayamıyordu. Ama gözleri netti: **altın hareli, buz mavisi tek bir çizgi**.",
                "[[COMMS|ASIL MİMAR · ZAMANIN ÖTESİNDEN PROJEKSİYON|Sonunda göz göze geldik, Kayıp Anahtarım. Yirmi yıl önce baban Thomas Blackwood benim düzenimin tekil çekirdeğini çaldı ve beş yaşındaki kırılgan bir çocuğun kalbine sakladı. Vaelor seni zorla geri almaya kalktı — ne kadar kaba bir yöntem. Oysa ben seni kırmak istemiyorum Aiden. Sadece gözlerini açmanı istiyorum.]]",
                "Sesi bağırmıyordu; korkutucu olan tam da bu dingin, sonsuz sabırdı. Bir adım yaklaştı; bastığı paslı çelik zeminde pas bir anda kusursuz gümüşe dönüşüp geri eski hâline döndü. \"Şu etrafındaki kahramanlara bak,\" dedi başıyla donmuş Avengers'ı işaret ederek. \"Stark korkusundan kendi celladını yarattı. İnsanoğlu sevgi dedikleri o zayıflıkla kendi sonunu yazar. Ultron sadece küçük bir sınav. Çoklu evrende yalnızca **tek bir tane** olan senin gibi bir varlık, birkaç ölümlünün kalbi için harcanamaz. Bana dön... ya da sevdiğin herkesin nasıl dağıldığını izle.\""
            ),
            decisionTitle = "Bölüm XIII (1/4) — Asıl Mimar'a İlk Cevabın",
            choicePrompt = "Zamanın ötesindeki varlık tam karşında duruyor ve babandan, tekilliğinden, zayıflığından söz ediyor. Ona ne cevap vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch13_c1_defy_architect",
                    label = "Kanayan dudağını silip ayağa kalk: \"Ben senin anahtarın değilim! Babam o çekirdeği senin soğuk kafesinden kurtarmak için bana verdi — ve ben insan kalacağım!\"",
                    toneHint = "Sarsılmaz İnsanlık • Mimar'a Meydan Okuma • Yüksek İrade",
                    nextNodeId = "ch13_s2_nightmare",
                    effect = BookChoiceEffect(
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 4, StoryFlagAxis.COSMIC_AWAKENING to 2),
                        setFlags = mapOf("ch13_architect_reply" to "defied"),
                        branchTag = "Mimar'a Meydan Okudu",
                        impactSummary = "Mimar'ın altın gözleri bir anlığına soğudu: 'Öyleyse kalbinin bedelini Sokovia'da ödeyeceksin, Aiden.'",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch13_c1_question_2006",
                    label = "Öfkeni bastırıp Beyaz Göz'le onun projeksiyonunu tara: \"Tekillik Çekirdeği nedir? Neden çoklu evrende benden sadece bir tane var?!\"",
                    toneHint = "Bilgi Avcısı • 2006 Gerçeği • Gizli İz",
                    nextNodeId = "ch13_s2_nightmare",
                    effect = BookChoiceEffect(
                        fatigueDelta = 1,
                        mysteryDelta = 2,
                        setFlags = mapOf("ch13_architect_reply" to "probed"),
                        secretUnlocked = "Tek Çizgi ve Anahtar Sırrı",
                        impactSummary = "Mimar hafifçe gülümsedi: 'Çünkü bütün zaman çizgilerini tek bir sessizliğe bağlayacak kilidin tek anahtarı sensin.'",
                        isBranching = true
                    )
                ),
                BookChoiceDef(
                    id = "ch13_c1_cold_silence",
                    label = "Tek kelime etme — sadece avucundaki mavi kıvılcımı zorla yakıp onun projeksiyonuna doğru sarsılmaz bir bakışla yürü.",
                    toneHint = "Soğuk İrade • Kırmızı Göz Riski • Gizem",
                    nextNodeId = "ch13_s2_nightmare",
                    effect = BookChoiceEffect(
                        redEyeDelta = 1,
                        mysteryDelta = 1,
                        setFlags = mapOf("ch13_architect_reply" to "silent"),
                        impactSummary = "Mimar başını yana eğdi: 'Sessizlik... ilginç. İçindeki kırmızı öfke bana sandığından daha yakın.'"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 2: VİBRANYUM YARASI & ZİHİN TAŞI KÂBUSLARI (Karar 2/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch13_s2_nightmare",
            chapterNumber = 13,
            sceneOrder = 2,
            sceneTitle = "Kırılan Zihinler — Gerçek Bir Yara",
            locationTag = "AFRİKA HURDA GEMİSİ · ANA AMBAR • 15:12:01",
            paragraphs = listOf(
                "Altın-mavi çizgi göz kırpma süresinde kapandı — ve zaman sert bir çatırtıyla yeniden aktı! Ultron'un namlusundan fırlayan kızıl plazma ışını tam göğsünün sol yanına çarptı! Normalde mavi Drift enerjinle bunu rahatça emer ya da son anda ışınlanırdın; ama geminin ortasındaki Vibranyum Kesici hâlâ devredeydi. Zırhsız taktik ceketin parçalandı, omzundan sırtına doğru derin, yakıcı bir yanık açıldı ve çelik duvara sertçe çarptın.",
                "Ağzından sıcak kan tadı geldi. Altı aydır ilk kez kendini gerçekten kırılgan, gerçekten yaralı hissediyordun. Aynı saniyede Ultron'un elindeki Zihin Taşı, ambarın üst katmanına korkunç bir sarı şok dalgası yolladı! Steve Rogers dizlerinin üstüne çöküp savaş sonrası kırık bir dans salonunun hayaline saplandı; Natasha Romanoff gözleri karanlıklaşarak Kızıl Oda'nın soğuk mermer koridorlarını gördü; Thor ise Asgard'ın gökyüzünde yaklaşan kara bir fırtınanın kehanetinde donakaldı.",
                "Üst geçitten Wanda'nın boğuk çığlığı duyuldu — kendi zihinsel bariyerini açıp kâbus dalgasını Bruce'tan uzak tutmaya çalışıyordu ama Pietro hızıyla araya girince dalga doğrudan Bruce Banner'ın göğsüne çarptı! Yeşil öfke kükreyerek geminin yan duvarını parçalayıp kıyı kasabasına doğru fırladı; Tony Hulkbuster uydu kapsülünü çağırıp peşinden uçmak zorunda kaldı!",
                "[[ALARM|SAHA KRİZİ · EKİP DAĞILDI|Bruce kontrolü kaybetti — Tony durdurmaya gitti! Steve, Natasha ve Thor kâbus dalgasının etkisinde. Vibranyum Kesici gücünü %60 baskılıyor!]]"
            ),
            decisionTitle = "Bölüm XIII (2/4) — Yaralıyken Kimi Kâbustan Çıkaracaksın?",
            choicePrompt = "Omzun kanıyor, gücün yarım çalışıyor ve Ultron yaklaşırken ekip kâbusun içinde boğuluyor. Beyaz Göz'ünün son kırıntısıyla önce kimin zihnine gireceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch13_c2_wake_natasha",
                    label = "Natasha'nın yanına sürünerek ışınlan — Beyaz Göz'le Kızıl Oda kâbusunu parçala ve elini sıkıca tut: \"Buradasın Nat, ben buradayım!\"",
                    toneHint = "Natasha Bağı • Zihinsel Kurtarma • Bölüm 14'e Zemin",
                    nextNodeId = "ch13_s3_zero_rescue",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 6, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 4)),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch13_nightmare_saved" to "natasha"),
                        impactSummary = "Natasha derin bir nefesle göğsüne kapandı; gözlerinde ilk kez duvarlarını tamamen indiren bir şükran vardı.",
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch13_c2_shield_wanda",
                    label = "Zihin Taşı'nın geri tepmesiyle yere yığılan Wanda'nın önüne ışınlan — elini tutup kâbus dalgasını kendi omuzlarına çek!",
                    toneHint = "Wanda Bağı • Fedakâr Koruma • Bölüm 14'e Zemin",
                    nextNodeId = "ch13_s3_zero_rescue",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 6, BookCharacterId.PIETRO to 2),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 4)),
                        fatigueDelta = 1,
                        setFlags = mapOf("ch13_nightmare_saved" to "wanda"),
                        impactSummary = "Wanda kanayan omzuna bastırıp fısıldadı: 'Bunu benim için kendine çekmemeliydin Aiden...' Pietro bile uzaktan şokla izledi.",
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch13_c2_wake_steve_thor",
                    label = "Kısa bir mavi şok darbesiyle Steve ve Thor'u aynı anda kâbustan uyandır — sahada ayakta duran kalkan hattını hemen kur!",
                    toneHint = "Taktik Liderlik • Steve & Thor Sadakati",
                    nextNodeId = "ch13_s3_zero_rescue",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.THOR to 4, BookCharacterId.CLINT to 3),
                        avengersTrustDelta = 4,
                        fatigueDelta = 1,
                        setFlags = mapOf("ch13_nightmare_saved" to "captains"),
                        impactSummary = "Steve kalkanını kaldırıp senin önüne geçti, Thor çekiciyle iki Ultron nöbetçisini ezdi — hat yeniden kuruldu!"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 3: YARA İZLİ SIFIR'IN ŞOK EDİCİ KURTARIŞI! (Karar 3/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch13_s3_zero_rescue",
            chapterNumber = 13,
            sceneOrder = 3,
            sceneTitle = "Yara İzli Yankı — Sıfır Sahneye İner",
            locationTag = "AFRİKA HURDA GEMİSİ · ÇÖKEN GÜVERTE • 15:18",
            paragraphs = listOf(
                "Ultron ağır, gıcırtılı adımlarla dumanın içinden çıktı. Vibranyum silindirlerinin tamamını kargo gemisine yüklemişti. Senin yaralı hâline ve burun kenarından süzülen ince kan çizgisine bakarak alaycı bir sesle güldü: \"Bu kadar mıymış? Mimar'ın o kadar övdüğü tekil mucize... şimdi bitiriyorum.\"",
                "Ultron'un iki avucundaki plazma topları birleşip kör edici bir kızıl ışına dönüştü. Steve kalkanıyla yetişemeyecek kadar uzaktaydı; senin ışınlanma enerjin Vibranyum Kesici yüzünden dibe vurmuştu. Tam o ölümcül saniyede, aranızdaki havada tanıdık ama çok daha vahşi, koyu mavi-kızıl bir kuantum kıvılcımı patladı!",
                "Çelik güverteye tek dizinin üzerine inen yabancı, tek bir çıplak el hareketiyle Ultron'un kızıl ışınını havada parçalayıp Vibranyum Kesici'yi tek tekmeyle parçaladı! Başını yavaşça kaldırdığında bütün gemideki nefesler kesildi. Natasha'nın eli silahında dondu; Steve'in gözleri büyüdü; Wanda'nın dudaklarından tek bir fısıltı döküldü.",
                "Karşınızda duran adam... **sendin**. Aynı keskin yüz hatları, aynı buz mavisi gözler — ama beş yıl daha yıpranmış, koyu yırtık bir ceket giyen, sol yanağından çenesine inen uzun gümüş bir yara izi taşıyan ve gözlerinin derininde tarifsiz bir keder yanan başka bir sen!",
                "[[COMMS|SIFIR · YARA İZLİ AIDEN|Kalk ayağa Aiden! Ultron Vibranyum'la Seul'e gidecek — Dr. Helen Cho'nun Beşiği'ni çalıp kendine gerçek bir beden yapacak! Eğer o Beşik Kule'ye gelip açılırsa... hepsi ölecek!]]",
                "Tam o anda güvertenin açılan çatısından Hulk'ı zar zor durdurmuş hâlde inen Tony Stark zırhının maskesini kaldırdı ve karşıdaki yara izli kopyana şokla baktı. Sıfır'ın buz mavisi gözleri bir saniyeliğine korkutucu bir kızıllığa büründü; parmağını doğrudan Tony'ye doğrulttu: \"**Onu sen yarattın, Stark!** Benim zamanımda yaptığın hatanın aynısını burada da yaptın!\"",
                "Sıfır'ın yanında durduğun her saniye ikinizin de sol bileğinde acı verici bir statik elektrik çatırdıyordu — **Tekillik Paraziti**! Aynı zaman çizgisinde iki tane Aiden bulunamıyordu; Sıfır'ın görüntüsü tıpkı bozuk bir film şeridi gibi titremeye başladı!"
            ),
            decisionTitle = "Bölüm XIII (3/4) — Sıfır Kaybolmadan Önce",
            choicePrompt = "Yara izli gelecekteki hâlin saniyeler içinde parazitle savrulmak üzere — ve Tony'ye öfkeyle bakıyor. Bu 5 saniyede ne yapacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch13_c3_reach_zero",
                    label = "Ona doğru bir adım at: \"Dur! Sen başka bir evrenden değilsin — sen benim geleceğimsin, değil mi? Bize güven, birlikte değiştirelim!\"",
                    toneHint = "Empati • Sıfır ile İlk Bağ • Bölüm 17 Geri Dönüşü",
                    nextNodeId = "ch13_s4_retreat",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 3, BookCharacterId.STEVE to 2),
                        mysteryDelta = 2,
                        setFlags = mapOf("ch13_zero_contact" to "empathy"),
                        secretUnlocked = "Tekillik Paraziti: Aynı Kişi",
                        branchTag = "Sıfır'a El Uzattı",
                        impactSummary = "Sıfır'ın yüzündeki öfke bir saniyeliğine kırıldı: 'Sen hâlâ onları kurtarabileceğine inanıyorsun... keşke haklı çıksan Aiden.'",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch13_c3_defend_tony",
                    label = "Tony'nin önüne geç: \"Tony hata yaptı ama o benim ailem! Gelecekten geldiysen söyle — Asıl Mimar'ı nasıl durduracağız?!\"",
                    toneHint = "Tony'yi Savunma • Yüksek Sadakat • Sert Yüzleşme",
                    nextNodeId = "ch13_s4_retreat",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 5, BookCharacterId.NATASHA to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch13_zero_contact" to "defended_tony"),
                        impactSummary = "Sıfır acı bir gülümsemeyle başını salladı: 'Bunu ben de bir zamanlar söylemiştim...' ve mavi kıvılcımla kayboldu!"
                    )
                ),
                BookChoiceDef(
                    id = "ch13_c3_stop_zero",
                    label = "Sıfır'ı kaçmadan yakalamaya çalış — bu adamın niyetinden emin olmadan Kule'ye yaklaşmasına izin veremezsin!",
                    toneHint = "Temkinli Savunma • Şüphe • Taktik",
                    nextNodeId = "ch13_s4_retreat",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.CLINT to 3, BookCharacterId.ZERO to -2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.ZERO, CharacterFlagAxis.SUSPICION, 2)
                        ),
                        setFlags = mapOf("ch13_zero_contact" to "hostile"),
                        impactSummary = "Elin onun omzundan elektrik kıvılcımıyla sekip geçti; Sıfır 'Beni düşman sanırsan bu savaşı kaybedersin!' deyip kayboldu."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch13_s4_retreat",
            chapterNumber = 13,
            sceneOrder = 4,
            sceneTitle = "Yaralı Dönüş — Sessiz Quinjet",
            locationTag = "QUINJET · GÜVENLİ ROTA • 17:45",
            paragraphs = listOf(
                "Ultron Vibranyum yüküyle kaçmıştı. Hulk'ın kıyı şehrindeki tahribatı bütün dünya haberlerinde dönüyordu. Quinjet'in karanlık kabininde ilk kez kimse şaka yapmıyordu: Bruce battaniyesine sarılmış sessizce yere bakıyordu, Thor kâbusunda gördüğü karanlığı çözmek için kısa süre sonra ayrılacağını fısıldamıştı, Natasha senin omzundaki Vibranyum yanığını pansumanlıyordu.",
                "Clint pilot koltuğundan başını çevirdi, sesinde bu kez alay değil sıcak bir kararlılık vardı: \"Kule'ye dönemeyiz. Şu an bütün kameralar, hükûmetler ve Ultron üzerimizde. Sizi kimsenin — Fury'nin bile dosyaya yazmadığı — tek bir güvenli yere götürüyorum.\""
            ),
            decisionTitle = "Bölüm XIII (4/4) — Uçuşta Sessiz Destek",
            choicePrompt = "Ekip hiç olmadığı kadar kırılgan. Çiftliğe inmeden önce kimin yanına oturup sessizliği paylaşacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch13_c4_sit_natasha",
                    label = "Pansumanı bitiren Natasha'nın elini bırakma — başını kabinin camına yaslayıp birlikte sessizce nefes al.",
                    toneHint = "Natasha Yakınlaşması • Sıcak Sessizlik",
                    nextNodeId = "ch13_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4),
                        setFlags = mapOf("ch13_flight_seat" to "natasha"),
                        impactSummary = "Natasha başını hafifçe omzuna yasladı: 'Yara izli halin bile senden daha inatçı değilmiş, Aiden.'",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch13_c4_sit_wanda",
                    label = "Pietro yüzünden gözleri dolan Wanda'nın yanına otur — omzuna kendi ceketini ört.",
                    toneHint = "Wanda Yakınlaşması • Şefkat",
                    nextNodeId = "ch13_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4),
                        setFlags = mapOf("ch13_flight_seat" to "wanda"),
                        impactSummary = "Wanda ceketine sarılıp fısıldadı: 'Bana vurmak yerine gerçeği söylemeye çalıştın... Pietro bunu gördü, biliyorum.'",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch13_c4_sit_bruce_tony",
                    label = "Suçlulukla ezilen Bruce ve Tony'nin yanına otur: \"Bugün yenilmedik — sadece ders aldık. Yarın geri döneceğiz.\"",
                    toneHint = "Ekip Morali • Bruce & Tony Desteği",
                    nextNodeId = "ch13_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.BRUCE to 4, BookCharacterId.TONY to 3, BookCharacterId.STEVE to 2),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch13_flight_seat" to "team"),
                        impactSummary = "Bruce derin bir nefes verdi; Tony ilk kez başını kaldırıp sana minnetle baktı."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch13_s5_end",
            chapterNumber = 13,
            sceneOrder = 5,
            sceneTitle = "Ufkun Ötesindeki Çiftlik",
            locationTag = "ORTA BATI · GİZLİ KIRSAL VADİ • 19:20",
            paragraphs = listOf(
                "Quinjet akşam kızıllığında uzun mısır tarlalarının arasına yumuşakça indi. Uzakta sıcak sarı ışıkların yandığı ahşap bir çiftlik evi ve rüzgârda salınan bir salıncak görünüyordu. İki dünyanın ağırlığını taşıyan kahramanlar, ilk kez bir geceliğine sadece sığınacak bir çatı arıyordu."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“En büyük savaşlardan önce bile, insanın neden savaştığını hatırladığı sessiz bir gece gerekir.”"
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 14 — BARTON ÇİFTLİĞİ & İLK GERÇEK ÖPÜCÜK! (4 Karar)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch14_s1",
            chapterNumber = 14,
            sceneOrder = 1,
            sceneTitle = "Barton Ailesinin Sıcaklığı",
            locationTag = "BARTON ÇİFTLİĞİ · AHŞAP EV & BAHÇE • 20:00",
            paragraphs = listOf(
                "Clint ahşap kapıyı açıp \"Tatlım, misafir getirdim!\" diye seslendiğinde, Tony'nin \"Bu bir ajan tuzağı mı?\" şakası daha ağzından çıkmadan içeriden neşeli çocuk kahkahaları ve hamile eşi Laura Barton'ın sıcacık gülümsemesi yükseldi. Küçük Cooper ve Lila koşup \"Natasha Teyze!\" diye boynuna atladığında, o soğukkanlı süper casusun yüzündeki bütün çelik zırh bir anda eriyip gitti.",
                "Laura masaya dumanı tüten ev yapımı yemekler koyarken göz ucuyla sana baktı: \"Clint senin şu meşhur baharatlı köftenden o kadar çok bahsetti ki, bir dahaki sefere mutfağı sana bırakacağım Aiden.\" Yirmi yıl boyunca tek bir evin sıcak akşam yemeğini tatmamış biri olarak, o ahşap masadaki gülüşmeler kalbinde 2006'dan beri donmuş bir yeri sessizce ısıttı.",
                "Yemekten sonra dışarıda ince bir akşam yağmuru başladı. Evin arkasındaki odunlukta Steve Rogers ile Tony Stark arasındaki eski gerginlik yeniden alevlenmişti — Tony baltayı kütüğe saplayıp \"İnsanların korkusundan değil, gezegeni koruyacak bir kalkan kurmak için yaptım!\" diye bağırıyor, Steve ise çıplak elleriyle kalın bir kütüğü ortadan ikiye ayırıp \"Savaş bitmeden yeni savaşlar yaratamazsın Tony!\" diye karşılık veriyordu."
            ),
            decisionTitle = "Bölüm XIV (1/4) — Odunlukta İki Lider",
            choicePrompt = "Steve ile Tony'nin kavgası ekibi ikiye bölmek üzere. Araya girip ne söyleyeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch14_c1_calm_both",
                    label = "İkisinin arasına girip kütüğün üstüne otur: \"Benim yara izli geleceğim tam bu kavga yüzünden doğdu. Birbirinizi kırarsanız Mimar tek kurşun atmadan kazanır.\"",
                    toneHint = "Bilge Denge • Steve & Tony Güveni • Yüksek Takım Birliği",
                    nextNodeId = "ch14_s2_fury",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.TONY to 4),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch14_wood_clash" to "united"),
                        impactSummary = "İki lider de sustu. Steve elini omzuna koydu, Tony baltayı indirip 'Bu çocuk bizden daha olgun çıktı Kaptan' diye mırıldandı."
                    )
                ),
                BookChoiceDef(
                    id = "ch14_c1_side_steve",
                    label = "Steve'in omzuna destek ver: \"Korkuyu bahane edip özgürlüğü ve güveni feda edemeyiz Tony — bu ekip kuralsız silahlar yüzünden var olmadı.\"",
                    toneHint = "Steve Sadakati • İlkeli Duruş",
                    nextNodeId = "ch14_s2_fury",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.TONY to -1),
                        setFlags = mapOf("ch14_wood_clash" to "steve"),
                        impactSummary = "Steve minnetle başını salladı; Tony derin bir iç çekip traktörü tamir etmek üzere ahıra yürüdü."
                    )
                ),
                BookChoiceDef(
                    id = "ch14_c1_side_tony",
                    label = "Tony'ye hak ver: \"Gökyüzündeki tehdidi ben de gördüm Steve. Mimar'a karşı sadece kalkan yetmez — Tony'nin zekâsına ihtiyacımız var.\"",
                    toneHint = "Tony Sadakati • Pragmatik Gerçekçilik",
                    nextNodeId = "ch14_s2_fury",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 5, BookCharacterId.STEVE to -1),
                        setFlags = mapOf("ch14_wood_clash" to "tony"),
                        impactSummary = "Tony rahat bir nefes aldı: 'Sonunda gökyüzüne gerçekten bakan biri.' Steve düşünceli gözlerle eve yürüdü."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch14_s2_fury",
            chapterNumber = 14,
            sceneOrder = 2,
            sceneTitle = "Karanlıktan Gelen Eski Dost — Nick Fury",
            locationTag = "BARTON ÇİFTLİĞİ · MUTFAK MASASI • 21:30",
            paragraphs = listOf(
                "Ahırın karanlık köşesinden tanıdık, tok bir ses yükseldiğinde Tony neredeyse elindeki ingiliz anahtarını düşürüyordu: \"Demek traktörün ruhuyla konuşmaya başladın, Stark.\" **Nick Fury** siyah paltosuyla mutfak masasına geçip sandviçini kesti, ekibi tek tek süzdü ve hepsinin kırılan moralini üç sert cümleyle yeniden ayağa kaldırdı.",
                "\"Ultron bir makine,\" dedi Fury. \"Hızlı, kurnaz ve sınırsız öfkeli. Ama tek bir şeyi anlayamıyor: siz birbiriniz için ölmeye razı insanlarsınız. Şimdi nükleer fırlatma kodlarını kırmaya çalışıyor ama dünya ağında görünmeyen, gizemli bir koruyucu kod her saniye şifreleri değiştirip onu engelliyor!\" Tony'nin gözleri bir anda parladı: **J.A.R.V.I.S.** hâlâ yaşıyordu — parçalanmış çekirdeği internete dağılıp dünyayı sessizce koruyordu!",
                "Fury sonra tek gözünü doğrudan sana çevirdi. \"Ve şimdi senin sıran, Blackwood. Afrika'daki kameralardan tek bir kare sızdı: seninle birebir aynı yüze sahip, yara izli bir adam. Masada kimse kimseden sır saklamayacaksa, şimdi konuşma vakti.\""
            ),
            decisionTitle = "Bölüm XIV (2/4) — Fury ve Ekibe Gerçeği Açacak Mısın?",
            choicePrompt = "Fury, Tony, Steve, Natasha, Wanda, Clint ve Bruce seni dinliyor. Yara izli Sıfır ve Asıl Mimar'ın 2006 sözleri hakkında ne kadarını paylaşacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch14_c2_tell_everything",
                    label = "Her şeyi eksiksiz anlat: Asıl Mimar'ın donmuş zamanda söylediklerini, 2006'daki Tekillik Çekirdeği'ni ve Sıfır'ın uyarısını masaya koy!",
                    toneHint = "Tam Şeffaflık • Maksimum Ekip Güveni • Seul Hazırlığı",
                    nextNodeId = "ch14_s3_romance_choice",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.FURY to 5, BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 4, BookCharacterId.WANDA to 4, BookCharacterId.BRUCE to 3, BookCharacterId.TONY to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.FURY, CharacterFlagAxis.TRUST, 4),
                            Triple(BookCharacterId.FURY, CharacterFlagAxis.RESPECT, 3)
                        ),
                        avengersTrustDelta = 5,
                        setFlags = mapOf("ch14_fury_reveal" to "full_truth"),
                        branchTag = "Tam Güven Masası",
                        impactSummary = "Fury masaya avucunu bastı: 'Artık kör dövüşü bitti. Seul'deki Dr. Helen Cho'ya hemen gidiyoruz — ve o yara izli kopyanla da hesaplaşacağız.'",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch14_c2_partial_truth",
                    label = "Seul'deki Dr. Helen Cho ve Vibranyum Beşiği uyarısını paylaş ama Asıl Mimar'ın senin hakkında söylediklerini şimdilik kendine sakla.",
                    toneHint = "Ölçülü Gizem • Koruyucu Mesafe • Kişisel Yük",
                    nextNodeId = "ch14_s3_romance_choice",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 2, BookCharacterId.CLINT to 2, BookCharacterId.FURY to -1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.FURY, CharacterFlagAxis.SUSPICION, 2),
                            Triple(BookCharacterId.FURY, CharacterFlagAxis.RESPECT, 1)
                        ),
                        mysteryDelta = 2,
                        setFlags = mapOf("ch14_fury_reveal" to "partial"),
                        impactSummary = "Fury gözünü kısıp 'Bir şeyleri hâlâ göğsünde saklıyorsun evlat, ama Seul izi altın değerinde' dedi."
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 3: ROMANTİZM AŞAMA 2 — ÇİFTLİK GECESİ & İLK GERÇEK ÖPÜCÜK! (Karar 3/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch14_s3_romance_choice",
            chapterNumber = 14,
            sceneOrder = 3,
            sceneTitle = "Yağmur ve Yıldızlar — Kalbin Mührü",
            locationTag = "BARTON ÇİFTLİĞİ · GECE YARISI • 23:15",
            paragraphs = listOf(
                "Toplantı dağıldığında saat gece yarısına yaklaşmıştı. Yarın şafakta Seul'e uçacak, Ultron'un yeni beden planını durdurmak için ateşin ortasına atlayacaktınız. Belki de uzun süre bulacağınız son huzurlu gece buydu.",
                "Ev sessizleştiğinde dışarıda yağmur hafif bir çiselemeye dönmüştü. Ön verandanın ahşap korkuluğunda **Natasha Romanoff** elinde iki sıcak fincanla tek başına yağmuru izliyordu — Kızıl Oda kâbusunun izleri hâlâ omuzlarındaydı. Arka taraftaki yıldızlı elma bahçesinde ise **Wanda Maximoff**, dizlerini kendine çekip ahşap banka oturmuş, uzak gökyüzüne bakarak kardeşi Pietro için dua ediyordu. İçerideki şöminenin başında ise **Steve ve Clint** eski günlerden konuşup haritayı son kez kontrol ediyordu.",
                "Bu gece, fırtınadan hemen önce, kalbinin gerçekten ait olduğu kişinin yanına gidebilirsin."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "s2_romance",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Bölüm XI Yankısı — Yarım Kalan Yakınlık",
                    paragraphs = listOf(
                        "Natasha içeriden geçerken göz göze geldiğinizde dudağının kenarında o ince, yalnızca sana özel tebessüm belirdi: \"Partide yarım kalan bir sohbetimiz vardı, Blackwood. Verandada bekliyorum.\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "s2_romance",
                    requiredFlagValue = "wanda",
                    callbackLabel = "Bölüm XI Yankısı — Yarım Kalan Yakınlık",
                    paragraphs = listOf(
                        "Wanda elma bahçesine yürürken dönüp sana baktı; parmak uçlarındaki yumuşak kızıl ışık gece karanlığında davet gibi parladı: \"Aiden... birkaç dakika yıldızların altında benimle oturur musun?\""
                    )
                )
            ),
            decisionTitle = "Bölüm XIV (3/4) — Çiftlikte Unutulmaz Gece (Aşama 2 · İlk Öpücük)",
            choicePrompt = "Fırtına öncesi bu tek sessiz gecede kimin yanına gidip aranızdaki duvarı tamamen indireceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch14_c3_kiss_natasha",
                    label = "Yağmurlu verandaya, Natasha'nın yanına git — korkularını dinle, yüzünü avuçlarının arasına al ve onu tutkuyla öp.",
                    toneHint = "♥ NATASHA İLK ÖPÜCÜK • Aşama 2 • Derin Aşk Yolu",
                    nextNodeId = "ch14_s4_natasha_kiss",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 8),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 6),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 4)
                        ),
                        setFlags = mapOf("s2_romance" to "natasha", "s2_romance_stage" to "2", "ch14_first_kiss" to "natasha"),
                        branchTag = "Natasha · İlk Öpücük",
                        impactSummary = "Yağmurun altında bütün duvarlar yıkıldı — Natasha ile kalbinizi mühürleyen unutulmaz ilk öpücüğü paylaştın.",
                        isRelationship = true,
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch14_c3_kiss_wanda",
                    label = "Yıldızlı elma bahçesine, Wanda'nın yanına git — gözyaşını sil, ellerini tut ve onu içtenlikle, derin bir şefkatle öp.",
                    toneHint = "♥ WANDA İLK ÖPÜCÜK • Aşama 2 • Derin Aşk Yolu",
                    nextNodeId = "ch14_s4_wanda_kiss",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 8),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 6),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.TRUST, 4)
                        ),
                        setFlags = mapOf("s2_romance" to "wanda", "s2_romance_stage" to "2", "ch14_first_kiss" to "wanda"),
                        branchTag = "Wanda · İlk Öpücük",
                        impactSummary = "Yıldızların altında kızıl ve mavi ilk kez tek bir nefes oldu — Wanda ile bütün acıyı susturan ilk öpücüğü paylaştın.",
                        isRelationship = true,
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch14_c3_brotherhood_hearth",
                    label = "Şömine başında Steve ve Clint'le kal — bu gece gönül bağlarını değil, yarınki Seul operasyonunu ve silah arkadaşlığını seç.",
                    toneHint = "Avengers Kardeşliği • Plâtonik Yol • Yüksek Taktik Bağ",
                    nextNodeId = "ch14_s4_brotherhood",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 5, BookCharacterId.CLINT to 5, BookCharacterId.BRUCE to 3),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("s2_romance" to "none", "s2_romance_stage" to "0", "ch14_first_kiss" to "none"),
                        branchTag = "Çelik Kardeşlik",
                        impactSummary = "Şömine başında Steve ve Clint'le sabahı beklediniz; aranızdaki silah arkadaşlığı sarsılmaz bir yemine dönüştü.",
                        isBranching = true
                    )
                )
            )
        ),

        // ── BRANCH A: NATASHA FIRST KISS ───────────────────────────────────────
        BookSceneNode(
            id = "ch14_s4_natasha_kiss",
            chapterNumber = 14,
            sceneOrder = 4,
            sceneTitle = "Yağmurlu Veranda — İki Gölgenin Öpücüğü",
            locationTag = "BARTON ÇİFTLİĞİ · ÖN VERANDA • 23:30",
            paragraphs = listOf(
                "[[LIVE3D|NATASHA|FARM_VERANDA|YAĞMURLU VERANDA · NATASHA İLE YALNIZ|Natasha'yı canlı izle — gözlerine bak, yaklaş, konuş ya da sessizce yanında kalıp onun konuşmasını bekle.]]",
                "Ahşap kapıyı sessizce kapatıp Natasha'nın yanına yürüdüğünde, yağmur damlaları çatı oluklarından ritmik bir fısıltı gibi dökülüyordu. Natasha kupasını korkuluğa bıraktı; yüzündeki o yenilmez ajan maskesi bu gece tamamen yoktu. \"Afrika'daki o kâbusta...\" dedi sesi hafifçe kısılarak. \"Yine Kızıl Oda'daydım. Bana hiçbir zaman normal bir hayatım, bir ailem ya da korkmadan tutabileceğim bir el olmayacağını öğretmişlerdi. Kendimi hep bir... canavar sandım.\"",
                "O kelimeyi duyduğun an bir adım yaklaştın; parmakların yumuşak ama sarsılmaz bir kararlılıkla çenesini kaldırdı. \"O kelimeyi bir daha kendin için kullanma, Natasha,\" diye fısıldadın; buz mavisi gözlerin gece karanlığında sakin bir deniz gibi parlıyordu. \"Ben de yirmi yıl aynı yalana inandırıldım. Ama sen canavar değilsin. Bu dünyada sırtımı gözüm kapalı yasladığım, nefesini duyduğumda kâbuslarımın sustuğu tek insansın.\"",
                "Natasha'nın nefesi bir an durdu. Yeşil gözleri önce gözlerinin içine, sonra yavaşça dudaklarına indi. Kendi ellerini senin ceketinin yakasına doladı, seni hafifçe kendine çekti — ve aranızdaki son mesafe tamamen eridi.",
                "Dudaklarınız buluştuğunda, dünyanın bütün gürültüsü, Ultron, Mimar ve yirmi yıllık kaçış bir anda yok oldu. Önce yavaş, tereddütlü, sanki kırılmasından korkulan bir mucize gibiydi; sonra Natasha'nın parmakları ensendeki saçlarına karıştığında öpücük derinleşti, ısındı ve aylardır saklanan bütün o özlemi tek bir nefeste açığa çıkardı. Senin ellerin onun belini sıkıca kavrarken, Natasha dudaklarının arasından hafif, rahatlamış bir nefes verdi.",
                "Alınlarınız birbirine yaslı ayrıldığınızda yağmur hâlâ yağıyordu ama ikiniz de üşümüyordunuz. Natasha gülümseyerek başparmağıyla yanağını okşadı: \"Yarın Seul'de, sonra Sokovia'da... ne olursa olsun geri dönüyorsun Aiden Blackwood. Bu bir emir.\""
            ),
            decisionTitle = "Bölüm XIV (4/4) — Natasha'ya Verdiğin Söz",
            choicePrompt = "Alnı hâlâ alnına yaslıyken Natasha'ya son sözün ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch14_c4_nat_promise_kofte",
                    label = "Gülümseyerek elini öp: \"Söz veriyorum Nat — bu iş bitince Hell's Kitchen'da iki tabak sıcak köfte ve sabaha kadar sessiz bir gece bizi bekliyor.\"",
                    toneHint = "Sıcak Mizah & Aşk • Köfte Sözü • Bölüm 20 Geri Dönüşü",
                    nextNodeId = "ch14_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4),
                        setFlags = mapOf("ch14_romance_vow" to "natasha_kofte"),
                        impactSummary = "Natasha yumuşak bir kahkaha atıp seni bir kez daha öptü: 'Sözünü tutmazsan seni dünyanın öbür ucunda bile bulurum.'",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch14_c4_nat_promise_stay",
                    label = "Onu sıkıca göğsüne sar: \"Artık kaçmak yok, gizlenmek yok. Bundan sonra hangi fırtına gelirse gelsin, yanındayım.\"",
                    toneHint = "Derin Bağlılık • Sarsılmaz Yemin",
                    nextNodeId = "ch14_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4),
                        setFlags = mapOf("ch14_romance_vow" to "natasha_forever"),
                        impactSummary = "Natasha başını kalbinin üstüne koyup fısıldadı: 'İlk kez gelecekten korkmuyorum, Aiden.'",
                        isRelationship = true
                    )
                )
            )
        ),

        // ── BRANCH B: WANDA FIRST KISS ─────────────────────────────────────────
        BookSceneNode(
            id = "ch14_s4_wanda_kiss",
            chapterNumber = 14,
            sceneOrder = 4,
            sceneTitle = "Yıldızlı Elma Bahçesi — Kızıl ve Mavinin Öpücüğü",
            locationTag = "BARTON ÇİFTLİĞİ · ELMA BAHÇESİ • 23:30",
            paragraphs = listOf(
                "[[LIVE3D|WANDA|FARM_ORCHARD|YILDIZLI ELMA BAHÇESİ · WANDA İLE YALNIZ|Wanda'yı canlı izle — gözlerine bak, yaklaş, konuş ya da yıldızların altında sessizce bekleyip onun sesini dinle.]]",
                "Yağmur bulutları tam elma ağaçlarının üzerinden çekilmiş, gökyüzü binlerce berrak yıldızla açılmıştı. Ahşap bankta oturan Wanda'nın yanına yaklaştığında, ıslak çimenlerdeki adım sesini daha duymadan başını çevirdi. Gözlerinin kenarında hâlâ kurumamış ince bir yaş izi vardı. \"Pietro bana baktığında...\" diye fısıldadı sesi titreyerek. \"Gözlerinde öfke gördüm Aiden. On yıl boyunca hayatta kaldığım her gün onu düşündüm, ama şimdi onu kurtaramamaktan, herkesi yeniden kaybetmekten çok korkuyorum.\"",
                "Yanına oturdun, üşüyen ellerini kendi avuçlarının içine aldın. Parmak uçlarında titreyen o ürkek kızıl enerji, senin avuçlarındaki sakin mavi sıcaklıkla temas ettiği anda bir fırtına olmaktan çıkıp yumuşak, altın-pembe bir parıltıya dönüştü. \"Kimseyi kaybetmeyeceksin Wanda,\" dedin yavaşça, gözlerinin ta içine bakarak. \"Pietro'yu geri getireceğiz. Ve ne olursa olsun, o karanlık odalarda bir daha asla tek başına uyanmayacaksın. Çünkü ben buradayım.\"",
                "Wanda'nın dudakları hafifçe aralandı; zihnindeki o her zamanki gürültü ilk kez tamamen susmuştu. \"Zihnini dinlediğimde...\" diye fısıldadı yaklaşırken, \"...ilk kez korku değil, sadece benim adımı duyuyorum.\"",
                "Aranızdaki son boşluğu ikiniz aynı anda kapattınız. Wanda'nın dudakları senin dudaklarına değdiğinde, göğsünün içinden bütün damarlarına yayılan o sıcak, gerçek, insanî kıvılcım hiçbir kozmik gücün veremeyeceği kadar derindi. Wanda bir elini yanağına, diğerini kalbinin tam üzerine yasladı; dudaklarınız uzun, yumuşak ve tutkuyla dolu bir öpücükte birleşti. Sanki ikinizin de yirmi yıldır taşıdığı yalnızlık o yıldızlı bahçede bir gecede silinip gitmişti.",
                "Yavaşça ayrıldığınızda Wanda gülümseyerek alnını senin alnına yasladı; gözleri ilk kez kederle değil, berrak bir ışıkla parlıyordu."
            ),
            decisionTitle = "Bölüm XIV (4/4) — Wanda'ya Verdiğin Söz",
            choicePrompt = "Yıldızlı bahçede elin hâlâ Wanda'nın yanağındayken ona ne söz vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch14_c4_wanda_promise_pietro",
                    label = "Alnından öpüp fısılda: \"Pietro'yu sağ salim yanına getireceğim Wanda — sonra üçümüz aynı masada ilk kez gerçek bir aile gibi güleceğiz.\"",
                    toneHint = "Wanda & Pietro Yemini • Bölüm 19-20 Geri Dönüşü",
                    nextNodeId = "ch14_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4, BookCharacterId.PIETRO to 2),
                        setFlags = mapOf("ch14_romance_vow" to "wanda_family"),
                        impactSummary = "Wanda mutlulukla gözlerini kapatıp seni bir kez daha öptü: 'Sana bütün kalbimle inanıyorum, Aiden.'",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch14_c4_wanda_promise_home",
                    label = "Onu sıkıca sar: \"Nereye gidersek gidelim, hangi evrende olursak olalım — benim evim sensin Wanda.\"",
                    toneHint = "Saf Aşk Yemini • Derin Bağ",
                    nextNodeId = "ch14_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4),
                        setFlags = mapOf("ch14_romance_vow" to "wanda_home"),
                        impactSummary = "Wanda göğsüne sokulup fısıldadı: 'Artık ne Ultron'dan ne de Mimar'dan korkuyorum.'"
                    )
                )
            )
        ),

        // ── BRANCH C: BROTHERHOOD PATH ─────────────────────────────────────────
        BookSceneNode(
            id = "ch14_s4_brotherhood",
            chapterNumber = 14,
            sceneOrder = 4,
            sceneTitle = "Şömine Başında Çelik Yemin",
            locationTag = "BARTON ÇİFTLİĞİ · OTURMA ODASI • 23:30",
            paragraphs = listOf(
                "Şöminenin çıtırtısı altında Steve Rogers, Clint Barton ve sen Seul'ün uydu krokisinin başına oturdunuz. Clint elindeki sıcak kahveyi sana uzattı: \"Biliyor musun çocuk, ilk geldiğin gece Kule'nin terasında sana ok doğrulturken içimden 'Bu adam üç gün sonra kaçar' demiştim. Yanıldığıma hiç bu kadar sevinmemiştim.\"",
                "Steve haritanın üzerine kalkanını bırakıp gülümsedi: \"Yarın Seul'de Ultron'un yeni bedenini durduracağız. Sonra da senin şu gelecekteki yara izli kopyanla yüzleşeceğiz. Ne olursa olsun, artık tek başına değilsin Aiden — biz bir aileyiz.\""
            ),
            decisionTitle = "Bölüm XIV (4/4) — Şafak Yemini",
            choicePrompt = "Şömine başında Steve ve Clint'e şafak öncesi hangi sözü vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch14_c4_bro_vow_all",
                    label = "Kupanı ikisine doğru kaldır: \"Kimse geride kalmayacak. Ne Sokovia'da, ne Seul'de — bu ekip birlikte dönecek!\"",
                    toneHint = "Sarsılmaz Kardeşlik • Ekip Güveni Zirvede",
                    nextNodeId = "ch14_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.CLINT to 4, BookCharacterId.TONY to 2),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch14_romance_vow" to "team_oath"),
                        impactSummary = "Üç kupa şömine ışığında tokuştu; yarınki Seul saldırısı için ruhunuz hazırdı."
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch14_s5_end",
            chapterNumber = 14,
            sceneOrder = 5,
            sceneTitle = "Şafakta Seul'e Kalkış",
            locationTag = "BARTON ÇİFTLİĞİ · SABAH PİSTİ • 05:30",
            paragraphs = listOf(
                "Şafak mısır tarlalarının üstünden altın bir çizgi gibi doğarken, Quinjet'in motorları gürledi. Barton çiftliğinin sıcaklığı bir gecede kırık bir ekibi yeniden yenilmez bir aileye dönüştürmüştü. Şimdi hedef Güney Kore'nin kalbi Seul'dü — Ultron, Dr. Helen Cho'nun laboratuvarında Vibranyum ve Zihin Taşı'yla kendine kusursuz bir beden üretiyordu. Ve Kule'nin karanlık gölgelerinde, gelecekten gelen yara izli Sıfır da aynı ana hazırlanıyordu."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Bazı geceler insanın bütün savaşlarını neden verdiğini tek bir nefeste hatırlatır.”"
        )
    ).associateBy { it.id }
}
