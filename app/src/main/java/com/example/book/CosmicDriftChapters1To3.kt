package com.example.book

/**
 * Extended, novel-length Chapters 1, 2, and 3 of "Kozmik Sürükleniş" (Aiden Blackwood — MCU).
 * Rebuilt with long, immersive literary chapters, ONLY 2 monumental high-impact crossroads per chapter,
 * deep cross-chapter flags/echoes, and 100% fidelity to Aiden Blackwood's canon.
 */
object CosmicDriftChapters1To3 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 1,
            romanNumeral = "I",
            title = "Kozmik Sürükleniş",
            titleEn = "Cosmic Drift",
            subtitle = "Mavi Işık, Çatıdaki Görü ve Avengers Kulesi",
            description = "Beş yaşında mavi ışıkla kayboluş, yirmi yıllık kaçış, New York çatısındaki psişik görü ve Avengers ile ilk büyük yüzleşme.",
            descriptionEn = "Disappearance at age 5 in blue light, 20 years on the run, the rooftop vision, and first contact with the Avengers.",
            estimatedMinutes = 16,
            accentHex = 0xFFA855F7L,
            startNodeId = "ch1_s1",
            sceneCount = 3,
            decisionCount = 2
        ),
        BookChapterMeta(
            number = 2,
            romanNumeral = "II",
            title = "Şartlar",
            titleEn = "The Terms",
            subtitle = "Cam Masada Pazarlık, Güç Sınavı ve İlk Adım",
            description = "Avengers Kulesi'nde Tony Stark ve Steve Rogers ile şartlar masası, güç gösterisi ve gölgelerle kurulan kontrollü ilk sınırlar.",
            descriptionEn = "Negotiating terms with Tony Stark and Steve Rogers at Avengers Tower, the power test, and controlled first boundaries.",
            estimatedMinutes = 13,
            accentHex = 0xFF60A5FAL,
            startNodeId = "ch2_s1",
            sceneCount = 3,
            decisionCount = 2
        ),
        BookChapterMeta(
            number = 3,
            romanNumeral = "III",
            title = "Deneme Süresi",
            titleEn = "Probation",
            subtitle = "Otuz Gün, Antrenman Salonu ve S.H.I.E.L.D. Alarmı",
            description = "Otuz günlük deneme süresi, Steve Rogers ile birebir sınav, enerji odası ve Kule'de çalan S.H.I.E.L.D. alarmında kader anı.",
            descriptionEn = "30-day trial period, sparring with Steve Rogers, energy chamber testing, and the S.H.I.E.L.D. alarm turning point.",
            estimatedMinutes = 11,
            accentHex = 0xFF22D3EEL,
            startNodeId = "ch3_s1",
            sceneCount = 3,
            decisionCount = 2
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM I: KOZMİK SÜRÜKLENİŞ (2 Büyük Dönüm Noktası)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch1_s1",
            chapterNumber = 1,
            sceneOrder = 1,
            sceneTitle = "Mavi Gökyüzü, Köfte Kokusu ve Çatıdaki Görü",
            locationTag = "HELL'S KITCHEN • 02:14",
            paragraphs = listOf(
                "Gökyüzü yanlıştı.",
                "[[ANIM|BLUE_SKY_2006|14 KASIM 2006 · MAVİ GÖKYÜZÜ|Sonsuzluk Taşı düzeyindeki mavi yarık Manhattan semalarını yardığında beş yaşındaydın.]]",
                "Beş yaşındaydın ve bunu anlatacak kelimelerin yoktu ama vücudun anlamıştı — ensendeki ince tüyler, midene oturan kurşun ağırlığı, annenin elinin aniden senin küçük avucunu bulup parmak boğumlarını acıtacak kadar sıkışı. New York geceleri her zaman sokak lambalarının kirli sarısı ya da neon kırmızısı olurdu; o gece değildi. O gece maviydi. Sanki birisi evrenin derisini görünmez bir neşterle yarmış da altından sızan Sonsuzluk Taşı düzeyindeki o soğuk, canlı ışığı kapatmayı unutmuş gibi kusursuz ve dehşet verici bir mavi.",
                "Yangın merdiveninin demiri avucunda buz gibiydi. Annenin sesi ince bir tel gibi titriyordu ama kelimeleri inatla sakindi: \"Aiden, tatlım, bana bak. Sadece bana bak, sakın yukarı bakma.\" Babanın sesi ondan da sakindi ve bu çok daha kötüydü — çünkü baban hiç öyle konuşmazdı. O hep güler, kahvesini döktüğünde bile şaka yapardı. O sakinlik, bir yetişkinin çocuğuna korkuyu göstermemek için yüzüne geçirdiği ince bir maskeydi ve sen beş yaşında bile o maskenin çatladığını görmüştün.",
                "Elini bıraktın. Neden bıraktığını yirmi yıldır çözemedin. Belki gökyüzündeki mavi yarık adını fısıldamıştı, belki göğsünün içinde daha doğmadan önce atılmış bir düğüm o an çözülmüştü. İki adım geri attın ve ışık gökyüzünden inmedi — sadece *oradaydı*. Bir saniye sonra apartman yoktu. Sokak yoktu. Yangın merdiveninin demiri yoktu. Annen yoktu. Baban yoktu. Üç blok öteden gelen siren sesi bile silinmişti. Sadece sen, boş bir karanlık ve dilinde o geceden beri hiç geçmeyen soğuk bakır tadı.",
                "O günden sonra insanlar sana iki şekilde baktı. Önce kalın kurşun camların arkasından bakan doktorlar ve siyah takım elbiseli hükümet görevlileri — ellerinde titreşen ölçüm cihazları, yüzlerinde bir çocuğa saklamayı unuttukları ilkel korku. Seni beyaz odalara koydular, bileklerine kablolar bağladılar, kanını her sabah üç farklı tüpe çektiler. Dokuz yaşında ilk kez kazayla ışınlandın; soğuk bir sorgu odasından sadece nefes almak için çıkmak istedin ve yarım saniye sonra üç sokak ötede, sağanak yağmurun altında dizlerinin üstündeydin. Burnundan ince bir kan sızıyordu. Köşeden fırlayan ajanlardan biri telsize fısıldamıştı: \"Küçük canavar hâlâ sokakta.\" O kelimeden nefret etmeyi o gece öğrendin. *Canavar.*",
                "On birinde ilk kez kaçtın ve iki gün sonra bir metro tünelinde yakalandın. On üçünde tekrar kaçtın — o kez başardın ama peşindeki iki silahlı muhafızı panikle nereye ışınladığını hâlâ bilmiyorsun; sadece bir daha seni bulamadıklarını biliyorsun. On beşinde gözlerin ilk kez bembeyaz parladı ve karşındaki sorgu ajanı gözleri açık halde mermer heykel gibi dondu; zihninden o koridoru sildiğinde elin saatlerce titremiş, kendini bir hafta boyunca kimseye göstermeden eski bir depoda saklamıştın. On yedinde ise bir kozmik tarikatın tuzağında kontrolünü kaybettiğinde yerçekiminin bile büküldüğünü, beton tavanın kâğıt gibi havaya kalktığını gördün.",
                "Yirmi birinde işler sadece Dünya ile sınırlı kalmadı. S.H.I.E.L.D.'ın özel av timi seni Prag'da köşeye sıkıştırdığında, nefesinin yettiği en uzak noktayı düşündün ve havayı yardın — gözlerini açtığında gökyüzünde iki soluk ay olan, mor kumlu yabancı bir gezegendeydin. O sıçrama seni neredeyse öldürüyordu; üç gün boyunca kıpırdayamadan titredin, hızlı iyileşmen bile yorgunluğunu zor topladı. Bir yıl sonra başka bir kırık zaman çizgisinin kenarından döndüğünde, evrenin sandığından çok daha büyük ve çok daha acımasız olduğunu anlamıştın.",
                "Şimdi yirmi beş yaşındasın. Yirmi yıldır kaçıyorsun. S.H.I.E.L.D. dosyaları, dünya hükümetlerinin termal uyduları, adını bile duymamış insanların yönettiği kozmik tarikatlar... Hepsi seni ya kontrol edilmesi gereken bir silah ya da kapatılması gereken bir anomali olarak görüyor. Son dört yıldır hiçbir otel odasında üç geceden fazla uyumadın. Her girdiğin mekânda önce çıkışları, kameraları ve kör noktaları saydın. Kimseye sırtını dönmedin. Kimseye güvenmedin.",
                "Bir de yüzün vardı — saklaması güçlerden bile daha zor olan tek şey. Kapüşonunu ne kadar indirirsen indir, karanlık bir sokak lambasının altında bile insanlar durup ikinci kez bakıyordu. Korktuklarından ya da tehditkâr göründüğünden değil; çarpıcı mavi gözlerin fazlaydı. Doğrudan bakması zor, neredeyse gerçek dışı bir parlaklık. Bir kafede kahve isterken, bir gümrük memuruna sahte pasaport uzatırken ya da metroda biriyle tesadüfen göz göze geldiğinde, göz temasını ilk bozan hep karşı taraf olurdu. Yıllar içinde bunu bir zırha çevirmiştin: mesafeli dur, az konuş, kimsenin seni gerçekten tanımasına izin verme.",
                "O gece Hell's Kitchen'ın dar, yağmurdan parlayan bir ara sokağından geçerken burnuna çarpan koku seni neredeyse olduğun yere çiviledi. Köşedeki küçük, buğulu camlı Türk lokantası gece yarısı son ızgarasını kapatıyordu; kömür ateşinde pişmiş ızgara köfte, közlenmiş biber, sumaklı soğan ve fırından yeni çıkmış sıcak ekmek kokusu yağmurlu Manhattan havasını bıçak gibi yarıyordu. Tek bir parmak hareketiyle zamanı bükebilecek, bir şehri sessizliğe gömebilecek bir kaçak olarak hayattaki tek gerçek zayıf noktanın bu olması neredeyse gülünçtü. İçerideki yaşlı usta tezgâhı siliyor, camın arkasından turuncu sıcak bir ışık sokağa vuruyordu. İki saniye durdun, ceketinin cebindeki buruşuk yirmi doları hissettin.",
                "Ama tam kapının koluna uzanacakken ensendeki hava ince, soğuk bir elektrikle cızırdadı.",
                "S.H.I.E.L.D. şehrin üç farklı bölgesinde yüksek frekanslı enerji taraması başlatmıştı. Sıradan bir insan bunu fark edemezdi; ama senin kozmik farkındalığının kenarında cama vuran binlerce görünmez elektrik iğnesi gibi hissediliyordu. Üstelik taramanın merkezi Hell's Kitchen'a doğru daralıyordu. İç çektin, kapı kolunu bıraktın ve köfte kokusunu arkanda bırakıp sokağın karanlık köşesine yürüdün. Bir nefeste altı kat yukarıdaki çatıya sıçradın — kısa, temiz, neredeyse hiç yorgunluk vermeyen bir hava bükülmesi — ve ıslak betonun kenarına diz çöktün.",
                "Çünkü seni bu gece New York'a geri getiren şey sadece eski anılar değildi. Son üç gecedir aynı psişik görü peşini bırakmıyordu. Gözlerini kapattığın an zihninin içi soğuk, parlak mavi bir enerjiyle doluyor, kısa ama nefes kesici bir transın içinde Manhattan'ın göbeğindeki o yüksek cam kuleyi görüyordun. En üst katta yuvarlak, ışıklı bir brifing masası. Havada asılı dönen mavi hologramlar. Ve masanın çevresinde, dünyadaki herkesin haber ekranlarından tanıdığı ama senin sadece kaçış haritalarından, uzak durulması gereken tehditler listesinden bildiğin altı yüz: Tony Stark, Steve Rogers, Natasha Romanoff, Wanda Maximoff, Bruce Banner ve Clint Barton.",
                "Gözlerini kapattın ve görü bu gece her zamankinden daha keskin, daha yakın geldi. Yağmurun sesi kesildi, yerini seksen sekizinci kattaki cam salonun alçak uğultusu aldı. Tony Stark parmaklarıyla havadaki dosyanı çeviriyor, ekranda beş yaşındaki kayboluşundan kalma tek bulanık fotoğrafın, Prag'daki enerji patlamasının termal kaydı ve son beş yılda üç farklı kıtada yakalanmış açıklanamayan sıçrama izlerin dönüyordu. Bruce Banner gözlüğünü çıkarmış, kaşlarını çatmış spektrum grafiklerine bakıyordu: \"Bu bir X-geni ya da klasik radyasyon mutasyonu değil Tony. Dalga boyu doğrudan Sonsuzluk Taşı düzeyinde bir rezonans izi taşıyor. Bu adamın hücresel yapısı kozmik enerjiyi depoluyor, büküyor ve geri yayıyor.\"",
                "[[DOSSIER|S.H.I.E.L.D. DOSYASI · AIDEN BLACKWOOD (GÜÇ PROFİLİ VE SINIRLARI)|• Mavi Göz (Sürükleniş): Kısa/uzun mesafeli kuantum ışınlanma, hızlı doku iyileşmesi, sınırlı enerji emme ve yerel zaman yavaşlatma (uçuş yeteneği yok — tamamen zemin & sıçrama tabanlı).\n• Beyaz Göz: Telepati, hafıza silme ve zihin felci (yoğun kullanımda burun kanaması ve ağır zihinsel yorgunluk).\n• Kırmızı Göz: Yıkıcı moleküler darbe (yüksek kontrol riski).]]",
                "\"Yani yürüyen bir nükleer reaktörden bahsediyoruz,\" dedi Tony, ama sesinde korkudan çok huzursuz bir hayranlık vardı. Steve Rogers kollarını masaya yaslamış, dosyanın son sayfasını çeviriyordu: \"Yirmi yıldır peşinde üç hükümet, S.H.I.E.L.D. ve adı kayıtlarda bile olmayan gizli birimler var. Buna rağmen tek bir sivile bilerek zarar verdiğine dair tek bir kanıt yok. Kendini savunmuş, hafıza silmiş ve kaçmış. Sadece kaçıyor.\"",
                "Ve sonra Natasha Romanoff konuştu. Masaya oturmamıştı; kollarını kavuşturmuş cam duvarın önünde duruyor, yağmurlu karanlık şehre bakıyordu. Sesi sakin, ölçülü ve buz gibi netti: \"Onu köşeye sıkıştırırsanız, bu şehirde onu durdurabileceğimiz bir şey kalmaz. S.H.I.E.L.D.'ın gönderdiği taktik timler sadece onu daha sert bir şeye dönüştürür. Onu avlamayı bırakın; masaya çağırın. Gelmek isterse kendi gelir.\"",
                "Tam o anda masanın diğer ucunda sessizce oturan Wanda Maximoff başını aniden kaldırdı. Gözlerinin kenarında ince, neredeyse görünmez bir kızıl kıvılcım titredi. Sanki kilometrelerce ötedeki bu ıslak çatıda, yağmurun altında onu dinlediğini hissetmişti. Alarm vermedi. Tony'ye ya da Steve'e dönmedi. Sadece dudakları kıpırdamadan bir saniye doğrudan karanlık camın ardındaki boşluğa — sanki tam senin gözlerinin içine — baktı.",
                "Görü aniden koptuğunda burnundan ince bir sıcaklık aktı; elinin tersiyle sildin ve parmaklarındaki hafif kan lekesine baktın. Teklifin ne olduğunu artık biliyordun: Avengers'a katılırsan av biter. Hükümetler, S.H.I.E.L.D., uydular geri çekilir. Katılmazsan kaçış sonsuza kadar sürer — gezegenler, boyutlar ve kırık zaman çizgileri arasında yapayalnız. İçindeki yirmi yıllık kaçak hâlâ \"buradan ışınlan, başka bir kıtaya geç ve bir daha arkana bakma\" diye bağırıyordu. Ama göğsündeki yorgunluk, o alışıldık alarm sesinden ilk kez daha ağırdı.",
                "Midtown yönünde, yağmur bulutlarının arasında Avengers Kulesi'nin mavi ışığı gecenin içinde bir deniz feneri gibi parlıyordu. Ayağa kalktın, ceketinin yakasını düzelttin. Oraya gitmeye karar verdin — ama o masanın karşısına nasıl çıkacağın, bu oyunun bütün kurallarını ve onların seni nasıl göreceğini ilk saniyeden belirleyecekti."
            ),
            decisionTitle = "İlk Temas — Kule'ye Nasıl Gireceksin?",
            choicePrompt = "Avengers Kulesi'nin en üst katındaki masaya çıkmak üzeresin. İlk hamlen hem onların seni nasıl göreceğini hem de bu gecenin gidişatını kökten değiştirecek.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c1_direct",
                    label = "Doğrudan masanın tam ortasına ışınlan — gücünü saklama, av olmadıklarını ilk saniyeden göster.",
                    toneHint = "Gösterişli • Yüksek Tansiyon • Güç Mesajı",
                    nextNodeId = "ch1_s2_direct",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.THOR to 2, BookCharacterId.STEVE to -1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.CURIOSITY, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 2),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.SHIELD_SUSPICION to 2,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("ch1_entry" to "direct", "first_impression" to "dominant"),
                        branchTag = "Mavi Kıvılcımla Giriş",
                        impactSummary = "Kule'nin tüm güvenlik katmanlarını delip doğrudan masanın ortasına indin; Tony büyülendi, Steve savunmaya geçti, Natasha tek gözünü bile kırpmadı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c1_shadow",
                    label = "Güvenlik kör noktasından, zamanı iki saniye yavaşlatarak balkona süzül — sessizce kapıyı çal.",
                    toneHint = "Taktik • Kontrollü • Natasha'nın Dili",
                    nextNodeId = "ch1_s2_shadow",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.STEVE to 3, BookCharacterId.CLINT to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.CLINT, CharacterFlagAxis.RESPECT, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 3,
                            StoryFlagAxis.AVENGERS_TRUST to 2,
                            StoryFlagAxis.MYSTERY_AURA to 2
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch1_entry" to "shadow", "first_impression" to "calculated"),
                        secretUnlocked = "Kule Güvenlik Kör Noktası",
                        branchTag = "Gölgeden Giriş",
                        impactSummary = "FRIDAY bile seni yakalayamadan balkona çıktın; hem kontrolünü hem de istesen onları gafil avlayabileceğini zarifçe kanıtladın.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c1_echo",
                    label = "Önce Wanda'nın bıraktığı kızıl zihinsel ipliğe sessiz bir yanıt gönder, sonra terasa in.",
                    toneHint = "Psişik • Derin Yankı • Gizli Bağ",
                    nextNodeId = "ch1_s2_echo",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4, BookCharacterId.BRUCE to 2, BookCharacterId.NATASHA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.TRUST, 2),
                            Triple(BookCharacterId.BRUCE, CharacterFlagAxis.CURIOSITY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.PSYCHIC_RESONANCE to 3,
                            StoryFlagAxis.WHITE_EYE_CONTROL to 2,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        mysteryDelta = 2,
                        avengersTrustDelta = 1,
                        setFlags = mapOf("ch1_entry" to "echo", "first_impression" to "psychic"),
                        secretUnlocked = "Kule'deki Kızıl Yankı",
                        branchTag = "Kızıl İplik Yankısı",
                        impactSummary = "Kule'ye adım atmadan önce Wanda ile sessiz bir zihinsel köprü kurdun; ikiniz arasında kimsenin bilmediği bir ilk anlayış doğdu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM I — DAL 1A: DOĞRUDAN GİRİŞ ────────────────────────────────────
        BookSceneNode(
            id = "ch1_s2_direct",
            chapterNumber = 1,
            sceneOrder = 2,
            sceneTitle = "Mavi Kıvılcımla Masa Ortasına",
            locationTag = "AVENGERS KULESİ • BRİFİNG SALONU",
            paragraphs = listOf(
                "Hava bir kalp atışı kadar büküldü. Seksen sekizinci katın filtreli, steril havasını bir anda keskin bir ozon ve soğuk yağmur kokusu bastırdı. Mavi enerji dalgası yuvarlak brifing masasının tam ortasında sessiz, kontrollü bir basınç patlamasıyla dağıldı; havadaki hologramlar bir saniyeliğine parazitlenip yeniden oturdu. Çizmelerin cilalı taş zemine değdiğinde Kule'nin güvenlik sistemi yarım saniye gecikmeyle kırmızıya döndü — FRIDAY'in sesi tavan hoparlörlerinden yankılandı: \"Efendim, 88. katta tanımlanamayan boyutsal yarık—\"",
                "\"Kapat FRIDAY, kilitlemeyi de durdur,\" dedi Tony Stark, ama bileğindeki lüks saat o daha cümlesini bitirmeden gümüş ve kırmızı bir zırh eldivenine dönüşüp avuç içi iticisini şarj etmişti. Clint Barton'ın katlanabilir yayı nasıl olduğunu insan gözünün yakalayamayacağı bir hızla açılıp gerildi; okun ucunda mavi bir şok halkası yanıp sönüyordu. Steve Rogers sandalyesini devirmeden ayağa kalktı, refleksle vücudunu Bruce Banner'ın önüne siper etti. Wanda Maximoff'un parmak uçlarında koyu kızıl bir sis toplanıp havada ince şeritler çizmeye başladı.",
                "Odada silah çekmeyen ya da savunmaya geçmeyen tek kişi Natasha Romanoff'tu. Koltuğunun kenarına yaslanmış, iki eli de açıkça görünür yerde, yüzünde en ufak bir panik kırıntısı olmadan sana bakıyordu. Ama sonra göz göze geldiniz — ve o an bile odadaki bütün o silahlı gerilim tuhaf bir şekilde yön değiştirdi. Çarpıcı mavi gözlerin salonun soğuk ışığını yansıtırken, Clint kaşlarını çatıp bakışını istemsizce elindeki okun ucuna kaydırdı; Bruce gözlüğünü parmağıyla düzelterek göz temasını ilk bozan kişi oldu. Sadece Natasha bir saniye daha dayandı, sonra dudağının kenarında neredeyse görünmez, ölçülü bir kıvrım belirdi.",
                "\"Termal sensörleri, kuantum duvarını, hareket tarayıcılarını ve üç katmanlı elektromanyetik kalkanı tek adımda delip geçtin,\" dedi Tony, eldivenini indirmeden ama sesinde korkudan çok dengesi bozulmuş bir dahinin hayranlığı vardı. \"Ya gerçekten çok aptalsın, ya da bu odadaki herkesi aynı anda yere serebilecek kadar rahatsın. Hangisi, Bay Blackwood?\"",
                "\"Kapıyı çalsaydım S.H.I.E.L.D. taktik timi beni asansörde nötralizasyon ağlarıyla karşılayacaktı,\" dedin. Sesin alçak, sakin ve yirmi yıllık yalnızlığın verdiği o soğuk netlikle doluydu. \"Adımı konuşuyordunuz. Yirmi yıllık dosyamı masaya açmışsınız. İşte buradayım — beni aramanıza gerek kalmadı. Ne istediğinizi söyleyin.\"",
                "Steve Rogers bir adım öne çıktı; elini yavaşça kaldırıp Tony'ye ve Clint'e işaret verdi. \"Silahları indirin. Hemen.\" Clint bir an tereddüt etti ama yayı gevşetti; Tony eldivenindeki ışığı söndürdü. Steve bakışlarını doğrudan senin çarpıcı mavi gözlerine kilitledi; bir anlık ağırlığı hissettiği belliydi ama iradesiyle gözünü kaçırmadı. \"Bize saldırmak isteseydi, biz daha sandalyemizden kalkmadan bitirmişti,\" dedi ekibe, ardından sana döndü. \"Aiden Blackwood. Dosyalarını satır satır okuduk. Beş yaşından beri seni kovalayan bir sistemin ortasında, elindeki güce rağmen tek bir masuma zarar vermeden hayatta kalmışsın. Sana bir hücre ya da kelepçe değil, bir seçenek sunmak için buradayız.\"",
                "\"Seçenek,\" diye tekrarladın, kelimenin tadı ağzında yabancı ve soğuktu. \"Benim hayatımda bu kelimeyi kullananların arkasında her zaman kalın cam duvarlar, elektroşok kabloları ve beyaz önlüklü doktorlar saklı oldu Yüzbaşı.\"",
                "\"Benim arkamda cam duvar yok,\" dedi Natasha Romanoff, nihayet yerinden kalkıp masanın etrafından ağır, ses çıkarmayan adımlarla yürürken. Sana fazla yaklaşmadı — bir avcının başka bir avcıya tanıdığı o görünmez kişisel sınırın tam kenarında durdu. \"S.H.I.E.L.D. seni kontrol edilemeyen bir anomali olarak etiketledi. Üç farklı hükümet ve henüz adını bile duymadığın bazı kozmik tarikatlar seni istiyor. Bizim teklifimiz basit: Avengers ile çalışırsan av biter. Takip uyduları kapanır, aktif yakalama emirleri mühürlenir. İstemezsen...\" Omuzlarını hafifçe silkti. \"Bu odadan geldiğin gibi çıkıp gidersin. Kimse yolunu kesmeyecek. Ama yarın sabah güneş doğduğunda uydular tekrar seni aramaya başlar.\"",
                "\"Peki ya sizinle kalırsam?\" diye sordun, gözlerin bir an masanın diğer ucunda duran Wanda'ya kaydı. \"Benden ne istiyorsunuz? Silahınız olmamı mı?\"",
                "\"Kendi tarafını seçmeni,\" dedi Bruce Banner yumuşak, dürüst bir sesle. \"Beni de yıllarca kovaladılar Aiden. 'Tehlike', 'anomali', 'canavar' — o kelimelerin hepsini duydum. Burada kimse senden emir alan bir asker olmanı beklemiyor. Sadece, dışarıda yaklaşan fırtınalar olduğunda aynı tarafta durmamızı istiyoruz.\"",
                "Odanın diğer ucunda Wanda sessizdi ama parmaklarındaki kızıl ışık tamamen sönmüştü. Sana bakarken diğerlerinin gördüğü o korkutucu enerji yoğunluğunu değil, yirmi yıldır hiçbir yerde kalamamış bir adamın sessiz yorgunluğunu görüyormuş gibi duruyordu. Tony nihayet eldivenini tamamen saatine geri katladı, masanın kenarına yaslanıp seni süzdü: \"Küçük bir teknik detay daha. Mavi şovun gerçekten etkileyiciydi ama salondaki biyo-sensörler iniş anında nabzının aniden düştüğünü ve oksijen tüketiminin tavan yaptığını söylüyor. Uzun mesafeli ya da peş peşe sıçramalar seni yoruyor, değil mi? Sonsuza kadar kaçamazsın Aiden. Bir noktada sırtını yaslayabileceğin bir duvar ve nefes alacağın bir gece gerekecek.\"",
                "Doğruydu. Bunu onun ilk iki dakikada fark etmesi hoşuna gitmemişti ama inkâr etmek de anlamsızdı. Salonun yerden tavana uzanan cam duvarlarının ardında yağmurlu New York gecesi sonsuz gibi uzanıyordu. Aşağıda, Hell's Kitchen'ın dar sokaklarında hâlâ S.H.I.E.L.D.'ın mavi tarama dalgaları dolaşıyordu. Yirmi yıldır ilk kez önüne bir kapı açılmıştı — kilitsiz, ama bedeli henüz belirsiz bir kapı. Bu gece vereceğin cevap, hem senin güven çizgini hem de yarın sabahki pazarlığın tüm ağırlığını belirleyecekti."
            ),
            decisionTitle = "Teklifin Ağırlığı — İlk Gece Kararı",
            choicePrompt = "Avengers sana avı bitirme sözü veriyor. Ama karşılığında bu kulede, bu insanların arasında kalman gerekecek. Nasıl karşılık vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c2_stay_terms",
                    label = "\"Kabul — ama sadece benim şartlarımla. Yarın sabah masada kuralları ben de yazacağım.\"",
                    toneHint = "Dengeli • Bağımsız • Güçlü Duruş",
                    nextNodeId = "ch1_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.NATASHA to 3, BookCharacterId.TONY to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 2,
                            StoryFlagAxis.INDEPENDENCE to 2,
                            StoryFlagAxis.TOWER_BELONGING to 2
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch1_pact" to "terms", "night_stance" to "negotiator"),
                        branchTag = "Şartlı Anlaşma",
                        impactSummary = "Teslim olmadan, eşit bir taraf olarak kalmayı seçtin. Natasha sessizce onayladı, Steve saygıyla başını eğdi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_probe_mind",
                    label = "Gözlerini bir saniyeliğine Beyaz Göz'e çevir — bu teklifin arkasında gizli bir tuzak olup olmadığını bizzat yokla.",
                    toneHint = "Beyaz Göz • Kuşkucu • Yüksek Risk",
                    nextNodeId = "ch1_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2, BookCharacterId.STEVE to -2, BookCharacterId.TONY to -2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 3),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.FEAR, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.CURIOSITY, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 3,
                            StoryFlagAxis.SHIELD_SUSPICION to 3,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        mysteryDelta = 2,
                        avengersTrustDelta = -2,
                        setFlags = mapOf("ch1_pact" to "probed", "night_stance" to "white_eye_check"),
                        secretUnlocked = "Fury'nin Gizli Acil Durum Protokolü",
                        branchTag = "Beyaz Göz Yoklaması",
                        impactSummary = "Tony'nin zihninin kenarında Fury'nin gizli 'Kafes Protokolü'nü yakaladın. Tuzak olmadığını gördün ama Steve bu hamleyi unutmayacak.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_cold_trial",
                    label = "\"Kimseye güvenmiyorum. Sadece bu gece kalacağım — yarın canım isterse yok olurum.\"",
                    toneHint = "Mesafeli • Yalnız Kurt • Soğuk Sınır",
                    nextNodeId = "ch1_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 2, BookCharacterId.CLINT to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.EMPATHY, 2),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.SUSPICION, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.MYSTERY_AURA to 2
                        ),
                        avengersTrustDelta = 0,
                        setFlags = mapOf("ch1_pact" to "cold", "night_stance" to "lone_wolf"),
                        branchTag = "Tek Gecelik Mesafe",
                        impactSummary = "Hiçbir söz vermedin, duvarlarını indirmedin. Natasha bunu en iyi anlayan kişi oldu ve sana alan tanıdı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── BÖLÜM I — DAL 1B: GÖLGEDEN GİRİŞ ────────────────────────────────────
        BookSceneNode(
            id = "ch1_s2_shadow",
            chapterNumber = 1,
            sceneOrder = 2,
            sceneTitle = "Kör Noktadan İki Saniye",
            locationTag = "AVENGERS KULESİ • 88. KAT BALKONU",
            paragraphs = listOf(
                "Zamanı yalnızca iki saniyeliğine büktün. Ama o iki saniye, yüksek teknolojili bir kaleyi sıradan bir cam eve çevirmeye yetti. Yağmur damlaları havada gümüş boncuklar gibi asılı kaldı; Avengers Kulesi'nin en üst katındaki lazer tarayıcılarının titreşimi ağır çekime düştü, rüzgâr türbinlerinin kanatları neredeyse durdu. İki optik kamera açısının kesişmediği o üç metrelik kuzeydoğu kör noktasına sessiz bir kuantum sıçramasıyla ışınlandın; çizmelerin ıslak mermer zemine tek bir ses bile çıkarmadan değdi. Zamanı serbest bıraktığında yağmur yeniden yağmaya başladı — ne bir alarm öttü, ne bir lazer kırıldı, ne de FRIDAY'in tek bir sensörü kıpırdadı.",
                "Cam balkon kapısının hemen dışından içeriyi iki saniye izledin. Altı kişi hâlâ holografik masanın çevresindeydi. Sonra sağ elini kaldırdın ve parmak boğumunla kalın zırhlı cama iki kez vurdun. *Tık. Tık.*",
                "İçerideki bütün konuşma bıçak gibi kesildi. Clint Barton refleksle sandalyesini itip elini belindeki katlanabilir ok kılıfına götürdü, Tony Stark başını hızla pencereye çevirdi. Ama cam kapının kilidini açıp soğuk yağmurun önüne çıkan kişi Natasha Romanoff oldu. Elinde silah yoktu; vücut dili tamamen rahattı ama ayaklarının duruşu yarım saniyede ölümcül bir karşı saldırıya geçebilecek kadar dengeliydi. Gözleri önce senin ıslak omuzlarına, ardından balkonun arkasındaki seksen sekiz katlık baş döndürücü boşluğa kaydı.",
                "\"FRIDAY,\" dedi Natasha, başını senden hiç çevirmeden. \"Dışarıdaki balkonda kimse var mı?\"",
                "\"Kuzeydoğu balkon sensörleri tamamen temiz, Bayan Romanoff,\" diye yanıtladı yapay zekâ tavan hoparlörlerinden. \"Rüzgâr hızı saatte on dört mil. Herhangi bir ısı veya kütle anomalisi algılanmadı.\"",
                "Tony Stark'ın yüzündeki o alışıldık rahat ifade bir anda silindi. Hızla yan masadaki tabletini kaptı, üç farklı güvenlik akışını geriye sardı. \"Bu fiziksel olarak imkânsız,\" diye mırıldandı, kaşları derin bir çizgi oluştururken. \"Termal iz yok, kütle kayması yok, elektromanyetik kırılma yok, kuantum sıçrama yok. Sen seksen sekizinci katın balkonuna nasıl çıktın—\"",
                "\"Kapıyı çalarak,\" dedin, içeri adım atarken. Dışarıdaki yağmurun ve soğuk havanın kokusu brifing salonunun sıcak, kahve kokulu atmosferine karıştı. Kapüşonunu geri indirdiğinde ve tavanın parlak beyaz ışığı doğrudan yüzüne vurduğunda, odadaki o teknik şaşkınlık bir anda çok daha farklı, sessiz bir ağırlığa dönüştü. Çarpıcı mavi gözlerin masadakilerin üzerinde yavaşça gezindi. Bruce Banner istemsizce gözlüğünü düzeltip bakışını önündeki ekrana indirdi; Clint hafifçe yutkunup göz temasını bozdu; Tony bile bir saniye duraksayıp elindeki tablete dönmek zorunda kaldı. İnsanların korktuğu için değil, doğrudan bakmakta zorlandığı için göz kaçırmasına yirmi yıldır alışıktın.",
                "Natasha göz temasını bozmadı. Sadece başını milimetre kadar yana eğdi; bir gölgenin başka bir gölgeye duyduğu o sessiz, ölçülü saygı parladı bakışlarında. \"İki saniyelik yerel zaman kayması,\" diye tahmin etti alçak, neredeyse hayranlık taşıyan bir sesle. \"Kuzeydoğu sensörünün kör noktasını nasıl bildin?\"",
                "\"Çünkü yirmi yıldır kaçan biri, bir binaya bakarken manzarayı ya da ihtişamı değil, yalnızca kör noktaları ve çıkışları görür,\" dedin. \"Beni konuşuyordunuz. Hell's Kitchen'daki taramaları başlatanlar S.H.I.E.L.D. ajanları, ama o avı tek bir imzayla durdurabilecek tek masa burasıysa, teklifinizi üçüncü ağızlardan değil doğrudan yüz yüze duymak istedim.\"",
                "Steve Rogers masadan kalkıp elini uzatmadan — çünkü zorla uzatılan ellere nasıl tepki vereceğini sezmişti — ama tüm dürüstlüğüyle karşına geçti. \"Buraya saldırmadan, kimseyi tehdit etmeden, hatta Kule'nin alarmını bile tetiklemeden geldin Aiden. Bu bize kim olduğun hakkında S.H.I.E.L.D.'ın yüz sayfalık tehdit dosyasından çok daha fazla şey söylüyor. Avengers'a katılırsan av biter. Hükümetler, S.H.I.E.L.D. ve bütün takip uyduları geri çekilir. İstemediğin hiçbir şeye zorlanmayacaksın.\"",
                "\"Ve bunu bana neden teklif ediyorsunuz?\" diye sordun, sesin hâlâ mesafeliydi. \"Dünyada benden daha kolay yönetilebilecek yüzlerce asker var.\"",
                "\"Çünkü yönetilecek bir asker aramıyoruz,\" dedi Wanda Maximoff masanın diğer ucundan sakin, ölçülü bir sesle. \"Burası bir kafes değil Aiden. Ben de bu kuleye ilk adım attığımda öyle sanmıştım. Herkesin benden korktuğunu, bir gün kapıyı üzerime kilitleyeceklerini düşünmüştüm. Ama hâlâ buradayım — çünkü kapılar içeriden açılıyor.\"",
                "Sesinde ne bir zorlama vardı ne de acıma. Sadece yaşamış birinin çıplak gerçeği. Salonun ortasında, yirmi yılın ardından ilk kez kimsenin sana silah doğrultmadığı, kimsenin telsizden 'canavar' diye fısıldamadığı bir odada duruyordun. Ama duvarların hâlâ yerindeydi; çünkü güven bir gecede kazanılan bir şey değildi."
            ),
            decisionTitle = "Teklifin Ağırlığı — İlk Gece Kararı",
            choicePrompt = "Kontrollü girişin odadaki gerilimi kırdı. Şimdi teklife vereceğin yanıt yarın sabahki şartları belirleyecek.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c2_stay_terms",
                    label = "\"Kabul — ama sadece benim şartlarımla. Yarın sabah masada kuralları ben de yazacağım.\"",
                    toneHint = "Dengeli • Bağımsız • Güçlü Duruş",
                    nextNodeId = "ch1_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.NATASHA to 3, BookCharacterId.TONY to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 2),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 3,
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 2,
                            StoryFlagAxis.TOWER_BELONGING to 2
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch1_pact" to "terms", "night_stance" to "negotiator"),
                        branchTag = "Şartlı Anlaşma",
                        impactSummary = "Eşit şartlarda masaya oturmayı kabul ettin. Steve ve Natasha seni ilk geceden müttefik gibi görmeye başladı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_probe_mind",
                    label = "Gözlerini bir saniyeliğine Beyaz Göz'e çevir — bu teklifin arkasında gizli bir tuzak olup olmadığını bizzat yokla.",
                    toneHint = "Beyaz Göz • Kuşkucu • Yüksek Risk",
                    nextNodeId = "ch1_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2, BookCharacterId.STEVE to -2, BookCharacterId.TONY to -2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 3),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.FEAR, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.CURIOSITY, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 3,
                            StoryFlagAxis.SHIELD_SUSPICION to 2,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        mysteryDelta = 2,
                        avengersTrustDelta = -2,
                        setFlags = mapOf("ch1_pact" to "probed", "night_stance" to "white_eye_check"),
                        secretUnlocked = "Fury'nin Gizli Acil Durum Protokolü",
                        branchTag = "Beyaz Göz Yoklaması",
                        impactSummary = "Tony'nin zihninin kenarında Fury'nin gizli 'Kafes Protokolü'nü yakaladın; kazandığın güveni riske attın ama artık kör değilsin.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_cold_trial",
                    label = "\"Kimseye güvenmiyorum. Sadece bu gece kalacağım — yarın canım isterse yok olurum.\"",
                    toneHint = "Mesafeli • Yalnız Kurt • Soğuk Sınır",
                    nextNodeId = "ch1_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 2, BookCharacterId.CLINT to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.EMPATHY, 2),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.SUSPICION, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.MYSTERY_AURA to 2
                        ),
                        avengersTrustDelta = 0,
                        setFlags = mapOf("ch1_pact" to "cold", "night_stance" to "lone_wolf"),
                        branchTag = "Tek Gecelik Mesafe",
                        impactSummary = "Hiçbir söz vermeden sadece bir gece için kaldın; Natasha mesafeni tek kelime etmeden kabul etti.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── BÖLÜM I — DAL 1C: KIZIL İPLİK YANKISI ───────────────────────────────
        BookSceneNode(
            id = "ch1_s2_echo",
            chapterNumber = 1,
            sceneOrder = 2,
            sceneTitle = "Kızıl İplik ve Terastaki Karşılaşma",
            locationTag = "AVENGERS KULESİ • GECE TERASI",
            paragraphs = listOf(
                "Zihninin çelikten sınırını yalnızca milimetre kadar araladın. Yıllardır kimsenin yaklaşmasına izin vermediğin o soğuk mavi boşluktan, Wanda Maximoff'un kilometrelerce öteden bıraktığı ince kızıl ipliğe tek bir sakin, ölçülü düşünce gönderdin: *Yukarıdayım. Alarm verme.*",
                "Yarım saniye boyunca sadece Hell's Kitchen'ın yağmur sesi vardı. Sonra zihninin en dış çeperinde yabancı ama şaşırtıcı derecede tanıdık bir yankı belirdi — içeri sızmaya çalışmayan, hiçbir şey talep etmeyen, yalnızca kapı eşiğinde bekleyen sakin, sıcak bir fısıltı:",
                "[[MIND|WANDA MAXIMOFF · ZİHİN YANKISI|Doğu terasının cam kapısı açık. Kimseye söylemedim. Duvarlarımı yoklamadan sadece sınırda durduğun için teşekkür ederim.]]",
                "Kısa, temiz bir mavi sıçramayla Avengers Kulesi'nin seksen sekizinci katındaki doğu terasına indiğinde yüksek irtifanın keskin yağmuru yüzüne vurdu. Altındaki şehir uçsuz bucaksız bir ışık okyanusu gibi parlıyordu. Cam kapı gerçekten aralıktı ve eşikte, rüzgâr koyu saçlarını hafifçe savururken Wanda Maximoff tek başına duruyordu. Diğer beş kişi birkaç metre içeride, hâlâ holografik masanın başında dosyanı tartışıyordu.",
                "Wanda başını kaldırıp doğrudan sana baktı. Yağmur damlalarının süzüldüğü yüzünde ve o çarpıcı mavi gözlerinde bir saniye durdu. Diğer insanlar gibi göz temasını panikle bozmadı, ama istemsizce kısa bir nefes tuttuğu belliydi — sadece ışığın değil, o gözlerin ardında saklanan dingin ama sınırsız gücün ağırlığı vardı.",
                "\"Beni bu kadar uzaktan, üstelik bir zihin saldırısı olmadan hissedebilen ilk kişisin,\" dedi Wanda, sesi yağmurun uğultusu içinde alçak ve duruydu. \"Zihnimin içine girmedin. Duvarlarımı yoklamadın. Sadece sınırda durup kapıyı çaldın. Teşekkür ederim.\"",
                "\"İzinsiz kapı açmaktan nefret ederim,\" dedin, yağmurdan ıslanmış ceketinin omuzlarını hafifçe silkeleyerek. \"İçeridekiler beni kontrol altına alınması gereken bir silah olarak mı görüyor, yoksa çözülmesi gereken bir sorun olarak mı?\"",
                "\"Tony her zamanki gibi çözmek istediği bir bilmece olarak görüyor,\" dedi Wanda, dudağının kenarında ince, yorgun bir gülümsemeyle. \"Steve sana adil davranmak istiyor. Bruce senin yaşadığın yalnızlığı kendinden tanıyor. Natasha ise...\" Hafifçe başını içeri doğru çevirdi. \"Natasha zaten senin terasta olduğunu iki dakika önce anladı.\"",
                "Gerçekten de içerideki kalın camın ardında Natasha Romanoff'un silüeti çoktan doğu kapısına dönmüştü. Gözleri doğrudan seni izliyordu — ama elini silahına götürmemiş, kimseye işaret vermemiş, senin kendi kararınla içeri adım atmanı beklemişti.",
                "Wanda ile birlikte brifing salonuna adım attığınızda Tony Stark şaşkınlıkla elindeki kahve kupasını masaya bıraktı: \"Bir dakika — FRIDAY alarm vermedi, kameralar boş, ve biz ne zamandır potansiyel Kozmik Seviye varlıkları telepatiyle çay partisine davet ediyoruz?\"",
                "\"Adım Aiden Blackwood,\" dedin, odanın ışığı altına çıktığında. Sesin sakin ama buz gibi netti. \"Ve çay içmeye gelmedim Stark. Yirmi yıldır S.H.I.E.L.D. uydularından, hükümet avcılarından ve kozmik tarikatlardan kaçıyorum. Bu avı bitirebileceğinizi söylediniz. Nasıl olacağını dinliyorum.\"",
                "Steve Rogers masanın etrafından dolaşıp saygılı bir mesafede durdu. \"Dosyanı kapattırabilecek, S.H.I.E.L.D. ve Konsey avını resmen durdurabilecek tek anlaşma bu masada Aiden. Avengers'a katılırsan kaçmak zorunda kalmazsın. Kimsenin seni bir laboratuvar kobayı ya da silah gibi kullanmasına izin vermeyiz.\"",
                "Bruce Banner gözlüğünün üstünden sana baktı, göz teması bir saniye sonra yine kendi ellerine indi: \"Beş yaşındaki o mavi enerji yarığının izlerini inceledim. O gece hayatta kalman bile bildiğimiz bütün fizik yasalarına aykırı. Sana bir kafes değil, nefes alabileceğin güvenli bir yer sunuyoruz.\"",
                "Natasha iki adım yaklaşıp durdu, gözlerini senin mavi gözlerinden ayırmadı. \"Karar tamamen senin, Aiden. Burada kimse senden ilk geceden güven ya da minnet beklemiyor. İkisi de zamanla kazanılır.\" Salonun sıcaklığı, yirmi yıllık soğuk otel odalarının ardından neredeyse gerçek dışı geliyordu — ama sen hâlâ tek bir nefesle bu kuleden yok olabilecek kadar tetikteydin."
            ),
            decisionTitle = "Teklifin Ağırlığı — İlk Gece Kararı",
            choicePrompt = "Wanda kapıyı açtı, Natasha sınırına saygı duydu, Steve anlaşmayı masaya koydu. İlk gece tavrın ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch1_c2_stay_terms",
                    label = "\"Kabul — ama sadece benim şartlarımla. Yarın sabah masada kuralları ben de yazacağım.\"",
                    toneHint = "Dengeli • Bağımsız • Güçlü Duruş",
                    nextNodeId = "ch1_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 2, BookCharacterId.STEVE to 3, BookCharacterId.NATASHA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.WARMTH, 2),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 2,
                            StoryFlagAxis.TOWER_BELONGING to 2,
                            StoryFlagAxis.PSYCHIC_RESONANCE to 1
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch1_pact" to "terms", "night_stance" to "negotiator"),
                        branchTag = "Şartlı Anlaşma",
                        impactSummary = "Eşit şartlarda kalmayı seçtin; Wanda'nın gözlerinde kısa bir rahatlama belirdi, ekip yarın sabah için sözleşti.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_probe_mind",
                    label = "Gözlerini bir saniyeliğine Beyaz Göz'e çevir — bu teklifin arkasında gizli bir tuzak olup olmadığını bizzat yokla.",
                    toneHint = "Beyaz Göz • Kuşkucu • Yüksek Risk",
                    nextNodeId = "ch1_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 1, BookCharacterId.STEVE to -2, BookCharacterId.TONY to -2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 3),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.FEAR, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.CURIOSITY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 3,
                            StoryFlagAxis.SHIELD_SUSPICION to 3,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        mysteryDelta = 2,
                        avengersTrustDelta = -2,
                        setFlags = mapOf("ch1_pact" to "probed", "night_stance" to "white_eye_check"),
                        secretUnlocked = "Fury'nin Gizli Acil Durum Protokolü",
                        branchTag = "Beyaz Göz Yoklaması",
                        impactSummary = "Tony'nin zihninin kenarında Fury'nin gizli 'Kafes Protokolü'nü yakaladın; Wanda bile bir adım geri çekildi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch1_c2_cold_trial",
                    label = "\"Kimseye güvenmiyorum. Sadece bu gece kalacağım — yarın canım isterse yok olurum.\"",
                    toneHint = "Mesafeli • Yalnız Kurt • Soğuk Sınır",
                    nextNodeId = "ch1_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 2, BookCharacterId.WANDA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.EMPATHY, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.MYSTERY_AURA to 2
                        ),
                        avengersTrustDelta = 0,
                        setFlags = mapOf("ch1_pact" to "cold", "night_stance" to "lone_wolf"),
                        branchTag = "Tek Gecelik Mesafe",
                        impactSummary = "Hiçbir söz vermedin; ne Natasha ne de Wanda seni zorladı. Mesafeni koruyarak misafir odasına çekildin.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── BÖLÜM I — FİNAL ─────────────────────────────────────────────────────
        BookSceneNode(
            id = "ch1_s3_end",
            chapterNumber = 1,
            sceneOrder = 3,
            sceneTitle = "Kilitsiz Kapı",
            locationTag = "AVENGERS KULESİ • 84. KAT MİSAFİR ODASI • 03:40",
            paragraphs = listOf(
                "Brifing salonundan aşağı inerken asansörde kimse konuşmadı. Sana gösterdikleri misafir dairesi seksen dördüncü kattaydı — geniş, minimalist, yerden tavana uzanan kurşun geçirmez cam duvarlarıyla yağmur altındaki Manhattan'ın bütün ışıklarını ayaklarının altına seren bir oda. İlk yaptığın şey yirmi yıllık refleksle odadaki bütün çıkışları ve kör noktaları saymak oldu: iki yüksek cam panel, tavandaki geniş havalandırma şaftı, banyonun arkasındaki servis duvarı ve koridor kapısı.",
                "Ama kapının yanına gelip elini kilit mekanizmasına götürdüğünde bir saniye durdun. Dışarıdan kontrol edilen elektronik bir kilit yoktu. Kart okuyucu yoktu. Enerji bariyeri ya da gaz hattı yoktu. Sadece içeriden çevrilen sıradan, mekanik bir pirinç mandal. İstediğin an o kapıyı açıp koridora çıkabilir ya da pencerenin önünde tek bir düşünceyle başka bir kıtaya sıçrayabilirdin.",
                "Yatağı kapıya itmedin. Yirmi yılın ardından, ucuz motel odalarında ve terk edilmiş depolarda geçirdiğin yüzlerce geceden sonra ilk kez bir odaya girip yatağı kapıya itmemek, sırtında neredeyse fiziksel bir boşluk hissi bıraktı. Ceketini sandalyenin sırtına attın, pencerenin kenarına geçip karanlık camdaki kendi yansımana baktın. Yirmi beş yaşında, omuzları yılların tetikte duruşuyla gerilmiş, çarpıcı mavi gözleri loş ışıkta bile fazla parlak bir adam. Ne sokaktaki sıradan insanlara tam ait, ne de gökyüzünün yarıldığı o geceye.",
                "Bir anlığına beş yaşındaki o çocuğu düşündün — yangın merdiveninde soğuk demire tutunan, annesinin elini bırakan o çocuğu. O geceyi takip eden bütün o soğuk laboratuvarları, Prag'daki mor kumlu yabancı gezegeni, adını bile bilmediğin o kırık zaman çizgisinin sessizliğini... Hepsi bir bavula bile sığmadan sadece zihninin derinliklerinde seninle birlikte bu odaya taşınmıştı.",
                "Yirmi dakika sonra, koridorun yumuşak halısında neredeyse hiç duyulmayan ama senin keskin kulaklarından kaçmayan hafif, dengeli ayak sesleri yaklaştı. Kapının tam önünde durdu. Nefes alış verişini bile duyabiliyordun — sakin, düzenli, hiçbir gerginlik taşımayan bir nabız. Kapı çalınmadı. Kimse adını seslenmedi. Sadece yere konulan bir porselenin çok hafif *tık* sesi duyuldu, ardından ayak sesleri aynı sessizlikle koridorun sonunda uzaklaştı.",
                "Beş saniye bekledin, sonra kapıyı araladın. Koridor tamamen boştu. Ama eşiğin hemen önünde beyaz bir porselen tabak duruyordu; üzerinde dumanı hâlâ hafifçe tüten, iki sıcak tırnak pide arasına sarılmış kömürde ızgara köfte, közlenmiş yeşil biber ve yanında ince bir kâğıda yazılmış ufak bir not.",
                "Notta imza yoktu. Sadece düzgün, keskin, acele edilmeden yazılmış tek bir satır: *Hell's Kitchen'daki lokanta 02:30'da kapanıyor. Açken pazarlık yapılmaz.*",
                "Natasha Romanoff. Ne kapıyı çalmış, ne gözlerinin içine bakıp bir teşekkür beklemiş, ne de senin çizdiğin kişisel sınırı tek bir santim bile ihlal etmişti. Tabağı alıp içeri geçtin, kapıyı kapattın. Kömür ateşinin ve taze ekmeğin kokusu bu steril, yüksek teknolojili odayı bir anda gerçek bir yere dönüştürürken, dudaklarının kenarında yıllardır ilk kez sahte olmayan, küçük bir gülümseme belirdi. İlk lokmayı aldın, yağmurlu şehre baktın. Yarın sabah cam masada gerçek pazarlık başlayacaktı — ve bu kez kaçan taraf olmayacaktın."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch1_pact",
                    requiredFlagValue = "probed",
                    callbackLabel = "Beyaz Göz'ün Yankısı — Kafes Protokolü",
                    paragraphs = listOf(
                        "Ama zihninin serin bir köşesinde, Beyaz Göz'ü bir saniyeliğine açtığında Tony Stark'ın hafızasının kenarından çektiğin o tek kod adı hâlâ soğuk bir ışık gibi yanıp sönüyordu: *Kafes Protokolü — Nick Fury Acil Durum Yetkisi.* Steve Rogers bu hamleni kolay kolay unutmayacaktı, biliyordun. Ama yarın sabah o cam masaya oturduğunda, onların senin bildiğini bilmediği en ağır koz artık senin elindeydi."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch1_pact",
                    requiredFlagValue = "terms",
                    callbackLabel = "Eşit Şartlar",
                    paragraphs = listOf(
                        "Teslim olmamış, ama sırtını da dönmemiştin. Yarın sabah masaya oturduğunda kuralları sadece Tony Stark ya da S.H.I.E.L.D. değil, Aiden Blackwood da yazacaktı. Bu düşünce, yirmi yıldır ilk kez geceyi uykusuz geçirmemene yetecek kadar gerçekti."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch1_entry",
                    requiredFlagValue = "echo",
                    callbackLabel = "Kızıl Sessizlik — Wanda'nın Nöbeti",
                    paragraphs = listOf(
                        "Gece ilerleyip saat dördü geçerken, zihninin en uzak sınırında Wanda Maximoff'un varlığı çok hafif, dingin bir sıcaklık gibi duruyordu — içeri girmeye çalışmayan, duvarlarını zorlamayan, sadece 'bu kulede yalnız değilsin' diyen sessiz ve saygılı bir nöbet."
                    )
                )
            ),
            isChapterEnd = true,
            chapterEndQuote = "Yirmi yıl kaçtıktan sonra en tehlikeli şey bir düşman değil, kilitsiz bir kapıdır."
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM II: ŞARTLAR (2 Büyük Dönüm Noktası)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch2_s1",
            chapterNumber = 2,
            sceneOrder = 1,
            sceneTitle = "Sabah Işığı ve Cam Masa",
            locationTag = "AVENGERS KULESİ • KONFERANS SALONU • 09:15",
            paragraphs = listOf(
                "Sabah güneşi seksen beşinci katın yerden tavana uzanan cam duvarlarından vurduğunda Manhattan tamamen farklı bir şehre dönüşmüştü. Geceki o kirli, kaçaklara saklanma yeri veren mor gölgeler çekilmiş, yerini her çatlağı, her yalanı ve her silahı fazla net gösteren acımasız bir berraklık almıştı. Kule'nin aşağısında trafik ince bir nehir gibi akıyordu; sarı taksiler oyuncak gibi görünüyordu. Ama burada, bulutların hemen altında, hava bir ameliyathane kadar sessiz ve gergindi.",
                "Gece sadece dört saat uyuyabilmiştin — o da uykudan çok, göz kapaklarının ardında sürekli tetikte bekleyen hafif bir dinlenme haliydi. Saat 06:10'da güneş henüz Hudson Nehri'ne vurmamışken uyanmış, pencerenin önünde on dakika boyunca hiç kımıldamadan şehri izlemiştin. Eski alışkanlıklar bir gecede ölmezdi: duş alırken bile banyonun buğulu camından koridor kapısının gölgesini takip ettin, giyinirken ceketinin iç cebinde kaçış için yıllardır taşıdığın üç farklı sahte kimliği parmak uçlarınla yokladın.",
                "Seksen dördüncü kattan yukarı çıkarken asansörde kısa bir an Wanda Maximoff ile karşılaştın. Üzerinde koyu bordo bir kazak, elinde dumanı tüten bir kupa siyah çay vardı. Kapılar kapanırken sana baktı, hafifçe başını eğdi ama zoraki bir sohbet açmadı. \"Uyuyabildin mi?\" diye sordu sadece, sesi alçak ve sakin. \"Yeterince,\" dedin. Dudaklarının kenarında çok hafif bir anlayış belirdi: \"İlk hafta bu binanın sessizliği insana gürültüden daha tuhaf gelir. Alışıyorsun.\" Ne bir soru daha sordu ne de zihnine uzandı.",
                "Seksen beşinci katın konferans salonuna adım attığında masada beş kişi çoktan yerlerini almıştı: Tony Stark elinde dumanı tüten çift shot espressosuyla masanın başında, Steve Rogers kolları hafifçe öne eğilmiş halde sağında, Bruce Banner önüne açtığı üç şeffaf tabletin arkasında, Wanda Maximoff pencereye yakın bir sandalyede ve Natasha Romanoff kapıyı tam gören çapraz köşede. Her zamanki gibi önce çıkışları saydın — ana asansör, kuzey yangın merdiveni kapısı, sağdaki iki yüksek cam panel ve tavandaki geniş havalandırma kanalı. Yirmi yılın hayatta kalma refleksi tek bir gecede silinmiyordu.",
                "\"Kahve?\" diye sordu Clint Barton köşedeki tezgâhtan başını kaldırıp kupayı sallayarak. \"Tony'nin makinesi üç yüz bin dolar ama hâlâ yanık tadı veriyor, baştan uyarayım. Ayrıca bu sabah S.H.I.E.L.D. hukuk ekibi iki saat boyunca telefonda bağırdı, bu yüzden hepimiz biraz gerginiz.\"",
                "\"Gerek yok,\" dedin, masanın en uzak ucundaki deri sandalyeyi çekip oturmadan önce iki saniye duraksayarak. Masanın tam ortasında iki şey duruyordu: kalın, siyah ciltli, üzerinde gümüş S.H.I.E.L.D. kartalı bulunan resmi bir mutabakat dosyası ve yanında mat titanyumdan yapılmış, bilek genişliğinde ince bir halka. Tony kahvesinden bir yudum alıp kupayı masaya bıraktı, ardından o titanyum bilekliği iki parmağıyla cam yüzeyin üzerinden sana doğru kaydırdı. Metal, camda ince, soğuk bir fısıltı çıkararak tam parmaklarının önünde durdu.",
                "\"Kelepçe değil, sakin ol,\" dedi Tony, sesinde alaycı ama her kelimesi dikkatle tartılmış bir ciddiyet vardı. \"Frekans dengeleyici ve şifreli kapalı devre iletişim cihazı. S.H.I.E.L.D. uydularına ve Dünya Güvenlik Konseyi tarayıcılarına senin 'bizimle' olduğunu bildirir; böylece bir sokak kamerasına ya da termal uydunun açısına takıldığında üç dakika içinde tepene silahlı Quinjet indirmezler. Ama açık konuşalım Blackwood — bir şartımız var. Bu masada neyle çalıştığımızı bilmemiz gerekiyor.\"",
                "Tony parmağını hafifçe şıklattı ve masanın ortasında dün geceki dosyandan çok daha ayrıntılı, kırmızı uyarılarla dolu üç boyutlu bir harita açıldı. Haritada dünya kıtaları dönüyordu: New York, Prag, Budapeşte, Tokyo, İstanbul'un güneyi ve hatta atmosferin hemen dışında saptanan açıklanamayan iki kozmik dalga sıçraması. \"Elimizdeki kayıtlarda kıtalararası ve hatta gezegenlerarası ışınlanma izleri, insanüstü hızda refleksler, dakikalar içinde kapanan ağır yaralar, mermi ve enerji emen kalkanlar, zaman bükülmesi ve...\" Bir an duraksadı, ekrandaki kırmızı dosyalardan birini öne çekti. \"...ve son altı yılda en az dört hükümet ajanının hafızasını tamamen sildiğin, iki silahlı muhafızıysa bilinmeyen bir yere ışınladığın yazıyor.\"",
                "\"Beni karanlık bir depoda elektroşok kafesine kilitlemeye çalışan dört ajan,\" diye düzelttin, sesin hiç yükselmeden ama odadaki sıcaklığı birkaç derece düşürerek. \"Onları öldürmedim. İsteseydim o koridordan tek bir kişi bile sağ çıkamazdı. Sadece o geceyi ve yüzümü unutturdum. Diğer ikisini de on üç yaşındayken panikle göndermiştim — ve ikisi de hayatta. Biri Montana'da bir tarlada, diğeri Kanada sınırında bulundu; dosyalarınıza biraz daha iyi bakın Stark.\"",
                "Clint'in dudağının kenarı hafifçe seğirdi: \"Montana fena değilmiş.\"",
                "Steve Rogers ellerini masanın üzerinde birleştirdi. Bakışları sakin, adil ama tavizsizdi. \"Kimse burada seni eski dosyalar yüzünden yargılamıyor Aiden. Beş yaşından beri avlanan bir çocuğun hayatta kalmak için ne gerekiyorsa yapmasını anlayabiliyorum. Hatta yerinde olsam ben de farklı davranır mıydım, emin değilim. Ama eğer yarın bir sahada sırtımızı birbirimize yaslayacaksak, gücünün sınırını, kontrolünü ve en önemlisi öfke anında ne zaman durabildiğini kendi gözümüzle görmemiz lazım. Bunu bir emir ya da boyun eğme değil, karşılıklı güvenin ilk adımı olarak gör.\"",
                "Bruce Banner gözlüğünü çıkarıp masaya koydu ve nazik, yorgun bir anlayışla ekledi: \"Ölçüm cihazları sadece enerji yoğunluğunu ve rezonans dalga boyunu okuyacak. Canını yakacak, gücünü bastıracak ya da seni kısıtlayacak hiçbir şey yok. Sadece... beş yaşındaki o geceden kalan kozmik imzanın ne kadar kararlı olduğunu anlamamız gerekiyor. Çünkü bu seviyedeki bir güç dengesini kaybederse, sadece seni değil bütün bir şehri yakabilir.\"",
                "\"Ve bunu bilen tek kişi sen değilsin, Doktor,\" dedin alçak bir sesle. Bruce hafifçe başını eğdi — o, içinde taşıdığı fırtınanın ağırlığını bu odadaki herkesten daha iyi biliyordu.",
                "Gözlerini masadakilerin üzerinde yavaşça gezdirdin. Yine o tanıdık, sessiz tepki gerçekleşti: Bruce bakışlarının o gerçek dışı, çarpıcı mavi ağırlığına bir saniyeden fazla dayanamayıp önündeki tablete döndü; Tony kaşını hafifçe kaldırıp kahve kupasını bahane ederek gözlerini kaçırdı; Clint bile hafifçe öksürüp pencereye baktı. Yalnızca üç kişi bakışını indirmedi — sarsılmaz iradesiyle direnen Steve Rogers, zihnindeki sessiz kızıl kalkanın arkasından seni izleyen Wanda Maximoff ve koltuğuna yaslanmış, senin bir sonraki nefesinden hangi hamleyi yapacağını okumaya çalışan Natasha Romanoff.",
                "İçindeki kozmik enerji parmak uçlarında sessizce uğulduyordu. İstersen bu cam odayı tek bir düşünceyle yerle bir edebilirdin. İstersen zamanı dondurup masadaki bütün S.H.I.E.L.D. dosyalarını küle çevirebilir, ya da Beyaz Göz'ü açıp bu beş efsaneyi üç saniyeliğine kendi zihinlerinin içine hapsedebilirdin. Ya da hiçbir şov yapmadan sınırını netçe çizebilirdin. Bugün bu masada vereceğin cevap, önümüzdeki otuz günün ve Avengers'ın sana bakışının bütün dengesini belirleyecekti."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch1_pact",
                    requiredFlagValue = "probed",
                    callbackLabel = "Kafes Protokolü Gölgesi",
                    paragraphs = listOf(
                        "Tony'nin bilekliği iterken parmaklarının milimetre kadar tereddüt ettiğini gördün. Dün gece zihninden 'Kafes Protokolü'nü çektiğini biliyor — ve bu sabah seninle açık kart oynamak zorunda olduğunun farkında."
                    )
                )
            ),
            decisionTitle = "Güç Sınavı ve Pazarlık",
            choicePrompt = "Masanın ortasında şartlar ve frekans bilekliği duruyor. Gücünü ve sınırlarını onlara nasıl göstereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c1_measured",
                    label = "Ölçülü güç gösterisi yap — havadaki bir damla kahveyi zaman bükmesiyle dondur ve bilekliği kendi şartlarınla tak.",
                    toneHint = "Zarif • Kontrollü • Stratejik Güven",
                    nextNodeId = "ch2_s2_measured",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.BRUCE to 3, BookCharacterId.TONY to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.BRUCE, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.CURIOSITY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 3,
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 2,
                            StoryFlagAxis.TOWER_BELONGING to 2
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch2_demo" to "measured", "bracelet_status" to "modified"),
                        branchTag = "Ölçülü Kıvılcım",
                        impactSummary = "Zamanı milimetrik bükerek kontrolünü kanıtladın, bileklikteki takip çipini kendi ellerinle devre dışı bırakıp yalnızca iletişim hattını kabul ettin.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c1_white_eye",
                    label = "Beyaz Göz'ü aç ve tüm salona üç saniyelik kusursuz bir illüzyon göster — sonra bilekliği masaya bırak.",
                    toneHint = "Beyaz Göz • Mutlak Güç • Sarsıcı Etki",
                    nextNodeId = "ch2_s2_white_eye",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 3, BookCharacterId.TONY to 1, BookCharacterId.STEVE to -2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.CURIOSITY, 3),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.FEAR, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 3,
                            StoryFlagAxis.COSMIC_AWAKENING to 2,
                            StoryFlagAxis.SHIELD_SUSPICION to 3
                        ),
                        fatigueDelta = 1,
                        mysteryDelta = 2,
                        avengersTrustDelta = -2,
                        setFlags = mapOf("ch2_demo" to "white_eye", "bracelet_status" to "refused"),
                        secretUnlocked = "Beyaz Göz Zihin Kalkanı",
                        branchTag = "Beyaz Göz İllüzyonu",
                        impactSummary = "Tüm salonu üç saniyeliğine yanan bir uzay boşluğuna çevirdin; herkes sınırının ne kadar korkutucu olduğunu anladı, bilekliği takmadın.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c1_refuse",
                    label = "Güç gösterisini reddet — \"Sirk hayvanı değilim. Beni sahada göreceksiniz, cam masada değil.\"",
                    toneHint = "Sert Sınır • Bağımsız • Tavizsiz",
                    nextNodeId = "ch2_s2_refuse",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.CLINT to 2, BookCharacterId.TONY to -1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 1),
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.SUSPICION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.MYSTERY_AURA to 2
                        ),
                        avengersTrustDelta = 0,
                        setFlags = mapOf("ch2_demo" to "refused", "bracelet_status" to "pocketed"),
                        branchTag = "Tavizsiz Sınır",
                        impactSummary = "Kendini kanıtlamayı reddettin; Natasha bu duruşa saygı duydu, Tony ise seni çözmek için daha da inatlaştı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── BÖLÜM II — DAL 2A: ÖLÇÜLÜ KIVILCIM ──────────────────────────────────
        BookSceneNode(
            id = "ch2_s2_measured",
            chapterNumber = 2,
            sceneOrder = 2,
            sceneTitle = "Askıda Kalan Zaman",
            locationTag = "AVENGERS KULESİ • KONFERANS SALONU & AKŞAM TERASI",
            paragraphs = listOf(
                "Ayağa kalkmadın bile. Sandalyende rahat ama her kası kontrollü bir duruşla sadece sağ elini masadan yarım karış kaldırdın. Bakışların Tony Stark'ın masanın kenarındaki porselen espresso kupasına kaydı — az önce kupayı bırakırken kenarından süzülen tek bir koyu kahve damlası masaya düşmek üzereydi.",
                "Parmak uçlarında ince, berrak, saf mavi bir halka titreşti. Hiçbir ses çıkmadı. Salondaki hava basıncı kıpırdamadı. Ama o kahve damlası tam kupanın kenarından ayrılırken havada dondu; üzerinden yükselen ince beyaz buhar şeridi bile kıpırdamadan, bir müze vitrinindeki kusursuz cam heykel gibi boşlukta asılı kaldı. Hemen etrafında üç santimetrelik yarı şeffaf, geometrik bir kozmik enerji kalkanı belirdi; mavi ışık cam masaya yansıdı ve saniyenin onda biri içinde hiçbir iz, ısı ya da duman bırakmadan söndü.",
                "Kahve damlası sessizce masaya düştü. Aynı anda Bruce Banner'ın önündeki üç şeffaf tabletin spektrum grafikleri bir anlığına ölçek dışına fırlayıp hemen tertemiz bir sıfır çizgisine indi. Bruce nefesini tutup ekrana eğildi, parmakları şaşkınlıkla grafiği büyüttü: \"Yerel zaman genişlemesi, mikro enerji kalkanı ve sıfır termal sapma... hem de tek bir mikro-saniyede, odanın hava akışını bile bozmadan. Tony, bu hassasiyet seviyesi teorik fizikte bile imkânsız sayılır. Kontrolü kusursuz.\"",
                "Masadaki mat titanyum bilekliği eline aldın. Avucunun içinde metalin soğukluğunu hissettin; içindeki mikroskobik devrelerin ve şifreli vericilerin haritası kozmik enerjinin ince titreşimiyle parmak uçlarına okunuyordu. Avucunda çıplak gözle neredeyse seçilemeyen berrak bir mavi kıvılcım dolaştı. İçerideki küçük GPS takip kristali duyulmayacak kadar hafif bir çıtırtıyla atomik düzeyde eriyip sustu; ama kapalı devre Avengers iletişim hattı yeşil ışığını yakmaya devam etti. Bilekliği sol bileğine geçirdin, metal hafif bir tık sesiyle kapandı.",
                "\"İletişim açık,\" dedin doğrudan Tony Stark'ın gözlerinin içine bakarak. \"Takip kapalı. Bana ihtiyacınız olduğunda ararsınız. Nerede uyuduğumu, hangi sokakta yemek yediğimi ya da gece yarısı hangi çatıya çıktığımı bilmenize gerek yok.\"",
                "Tony bir saniye bilekliğe, bir saniye önündeki tablette yanıp sönen 'GPS MODÜLÜ YANIT VERMİYOR' uyarısına, sonra da sana baktı. Yüzünde beklemediğin kadar gerçek, neredeyse keyifli bir sırıtış belirdi. \"İki yüz bin dolarlık vibranyum kaplı iletişim devresine tek bir çizik atmadan sadece üç milimetrelik takip kristalini kızartmışsın. Tamam, pes ediyorum Yüzbaşı — bu adamı kesinlikle ekipte istiyorum.\"",
                "Steve Rogers'ın omuzlarındaki o askeri gerilim ilk kez gözle görülür biçimde düştü. \"Anlaştık Aiden. Otuz günlük deneme süresi bugün resmen başlıyor. Bu süre boyunca S.H.I.E.L.D. ve Konsey avı askıya alınacak. Seksen dördüncü katta kendi dairen olacak; ama kimse seni burada zorla tutmuyor. İstediğin an kapıdan çıkıp gidebilirsin — senden beklediğimiz tek şey, birlikte bir sahaya indiğimizde ekibi yarı yolda bırakmaman.\"",
                "Öğleden sonra saatler beklemediğin kadar hızlı aktı. Bruce Banner seni yetmiş beşinci kattaki geniş enerji laboratuvarına götürdü; elinde şırınga ya da kısıtlayıcı cihaz olmadan, sadece eski kozmik radyasyon haritalarını gösterip beş yaşındaki o mavi ışığın neye benzediğini anlamaya çalıştı. Onun yanında ilk kez bir bilim insanının seni kobay değil, meslektaş gibi dinlediğini hissettin. Sonra mutfak katında Clint Barton kahve makinesinin nasıl çalıştığını anlatırken yarı şaka yarı ciddi kat planındaki üç acil çıkış kapısının yerini işaret etti: \"Eski alışkanlıklar zor ölür, biliyorum.\"",
                "Akşam güneşi Hudson Nehri'nin ardında koyu kızıl bir çizgiye dönüşürken Kule yavaş yavaş o gürültülü gündüz halinden sıyrılıp derin bir akşam sessizliğine büründü. Seksen dördüncü katın loş koridorundan geçerken iki farklı ışık dikkatini çekti. Açık terasta, akşam rüzgârı kızıl saçlarını hafifçe savururken korkuluğun başında tek başına Manhattan'ın gece ışıklarını izleyen Natasha Romanoff duruyordu. Hemen gerideki geniş kütüphanenin yüksek cam kenarında ise Wanda Maximoff dizlerini kendine çekmiş, parmaklarının arasında ince kızıl kıvılcımlar döndürüyordu.",
                "İkisi de seni çağırmamıştı. İkisi de kapını çalmamış, senden bir konuşma, bir açıklama ya da yakınlık talep etmemişti. Aiden Blackwood'un yirmi beş yıllık hayatında ilk kez, birinin yanına yürüyüp yürümemek tamamen kendi sessiz iradesine kalmıştı."
            ),
            decisionTitle = "Akşamın Sessizliği — Bir Adım Atacak Mısın?",
            choicePrompt = "Ne Natasha ne Wanda seni zorluyor. Aiden Blackwood için yakınlık bir zorunluluk değil, nadir ve bilinçli bir tercihtir. Bu gece ne yapacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c2_natasha",
                    label = "Terasa çık ve Natasha'nın yanına geç — fazla konuşmadan, gölgelerin dilinden ortak bir sessizlik kur.",
                    toneHint = "Natasha • Sessiz Güven • Bilinçli Adım",
                    nextNodeId = "ch2_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.WARMTH, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHADOW_ALLIANCE to 2,
                            StoryFlagAxis.HUMANITY_ANCHOR to 2
                        ),
                        setFlags = mapOf("ch2_bond_step" to "natasha", "natasha_tone" to "step_taken"),
                        branchTag = "Natasha'ya İlk Adım",
                        impactSummary = "Terasta Natasha'nın yanında durdun. Köfte notu için tek kelimeyle teşekkür ettin; Natasha yüzünde nadir, sakin bir tebessümle mesafene saygı duydu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_wanda",
                    label = "Kütüphaneye uğra ve Wanda'nın yanına otur — iki farklı gücün ağırlığını ilk kez biriyle paylaş.",
                    toneHint = "Wanda • Zihinsel Anlayış • Bilinçli Adım",
                    nextNodeId = "ch2_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.WARMTH, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.PSYCHIC_RESONANCE to 3,
                            StoryFlagAxis.HUMANITY_ANCHOR to 2
                        ),
                        setFlags = mapOf("ch2_bond_step" to "wanda", "wanda_tone" to "step_taken"),
                        branchTag = "Wanda'ya İlk Adım",
                        impactSummary = "Wanda ile güçlerin bedelini ve 'canavar' etiketini konuştun; zihinleriniz ilk kez savunmasız ama güvenli bir dengede buluştu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_alone",
                    label = "İkisine de yaklaşma — kendi odana çekil ve sınırlarını tamamen kapalı tut.",
                    toneHint = "Mesafeli • Bağımsız • Yalnız Yol",
                    nextNodeId = "ch2_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 1, BookCharacterId.WANDA to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.MYSTERY_AURA to 2
                        ),
                        setFlags = mapOf("ch2_bond_step" to "alone", "natasha_tone" to "distant", "wanda_tone" to "distant"),
                        branchTag = "Bilinçli Mesafe",
                        impactSummary = "Kimseye kapı aralamadın. Ne Natasha ne Wanda bunu kırgınlıkla karşıladı; bu kulede duvarların sana ait kaldı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── BÖLÜM II — DAL 2B: BEYAZ GÖZ İLLÜZYONU ──────────────────────────────
        BookSceneNode(
            id = "ch2_s2_white_eye",
            chapterNumber = 2,
            sceneOrder = 2,
            sceneTitle = "Üç Saniyelik Boşluk",
            locationTag = "AVENGERS KULESİ • KONFERANS SALONU",
            paragraphs = listOf(
                "\"Ne ile çalıştığınızı gerçekten görmek istiyorsunuz,\" dedin. Sesin yükselmedi ama salondaki bütün o sabah sıcaklığını ve medeni diplomasi havasını tek bir cümlede çekip aldı. \"Dosyalar, grafikler, tahminler yetmiyor. O zaman iyi bakın. Bunu yalnızca bir kez göstereceğim.\"",
                "Ayağa kalkmadın. Sadece iki elini cam masanın serin kenarına koydun ve göz bebeklerindeki o çarpıcı, gerçek dışı mavi bir anda kayboldu — yerini kör edici, saf, soğuk bir **beyaza** bıraktı. Beyaz Göz açıldığında seksen beşinci kattaki bütün sesler bıçak gibi kesildi. Ne aşağıdan gelen Manhattan'ın uğultusu kaldı, ne klimanın fısıltısı, ne de Tony Stark'ın bileğindeki kronometrenin saniye sesi.",
                "[[ANIM|WHITE_EYE_AWAKENING|BEYAZ GÖZ · ÜÇ SANİYELİK BOŞLUK|Çarpıcı mavi gözlerin kör edici beyaza döndüğünde beş Avenger'ın zihni aynı anda durdu.]]",
                "[[MINIGAME|WHITE_EYE_RESONANCE|BEYAZ GÖZ RİTİM ODAĞI|Daralan kozmik halkaları altın çemberde mühürleyip beş Avenger'ın zihnini tek bir zarar vermeden sabitle!]]",
                "Bir kalp atışında konferans salonunun yerden tavana uzanan cam duvarları, parlak sabah güneşi, gökdelenler, cam masa ve sandalyeler duman gibi eriyip gitti. Beş Avenger kendilerini yıldızsız, dipsiz, soğuk bir kozmik boşluğun ortasında asılı buldu. Ayaklarının altında yer yoktu; çevrelerinde ağır çekimde dönen berrak mavi enerji parçacıkları ve kımıldayamayan kendi bedenleri vardı. Tony'nin bileğindeki akıllı saat zırh çağırmak için tepki vermedi — çünkü zihninin o komutu kaslarına göndermesi bile durdurulmuştu. Steve Rogers'ın süper asker kasları taş kesilmişti; kolları bir milimetre bile kalkmadı. Bruce Banner nefesini bile kontrol edemedi. Yalnızca Wanda Maximoff'un etrafında ince, titrek bir kızıl halka direnmeye çalıştı; gözleri fal taşı gibi açılmış, zihinsel duvarını tutmak için tüm gücünü harcıyordu ama o bile yarı yarıya donmuş, boğazından tek kelime çıkaramıyordu.",
                "O boşluğun ortasında yalnızca senin sesin vardı — her yönden aynı anda yankılanan, sakin ve soğuk bir gerçeklik: *Sizi incitmek isteseydim, bu odaya dün gece bile girmezdim.*",
                "Üç saniye. Tam üç saniye sonra gözlerin yeniden doğal mavisine döndü. Konferans salonu aynı anda bütün gürültüsüyle geri geldi. Beş kişi kendilerini aynı deri sandalyelerde, alınlarında ince bir soğuk ter ve hızlanmış nefeslerle buldu. Masadaki tek bir dosya kâğıdı bile yerinden oynamamıştı. Tony'nin espressosu hâlâ sıcaktı. Hiç kimseye fiziksel olarak tek bir çizik bile atmamıştın — ama bu odada kimin gerçekten kontrol sahibi olduğu artık tartışma konusu değildi.",
                "\"Bilekliğe ihtiyacım yok,\" dedin, mat titanyum bandı iki parmağınla masanın ortasına doğru geri iterek. \"S.H.I.E.L.D. beni takip etmek istiyorsa uydularını açık bırakıp deneyebilir. Ama bir daha kimse bu odada bana bir sınır, bir test ya da kafes çizecekmiş gibi konuşmasın.\"",
                "Salonda uzun, kurşun gibi ağır bir sessizlik çöktü. Bruce yavaşça, titreyen bir nefes verdi; parmakları tabletin kenarında hâlâ hafifçe sarsılıyordu. Tony Stark'ın o alışıldık alaycı maskesi tamamen düşmüştü; ekrana değil, doğrudan senin sakin ellerine bakıyordu. \"Üç saniye,\" diye mırıldandı kendi kendine. \"Bütün sinir sistemimi bir yazılımmış gibi duraklattı.\"",
                "Steve Rogers yerinden kıpırdamadan gözlerini sana dikti; göğsü hızla inip kalkıyordu ama sesi çelik gibi sert ve netti: \"Gücünü gördük Aiden. Ve bunu bize tek bir zarar vermeden durdurabildiğini de gördük. Ama şunu aklından çıkarma: korkuyla kurulan hiçbir anlaşma bir hafta bile sürmez. Otuz gün deneme süresi — birbirimizi gerçekten tanımak için. Eğer bir kez daha izinsiz bu ekibin zihnine girersen, bu anlaşma o saniye biter.\"",
                "Başını hafifçe eğip kabul ettin. Şakaklarında Beyaz Göz'ü aynı anda beş güçlü zihne açmanın bıraktığı o tanıdık, soğuk sızı hafifçe zonkluyordu; ama yüzünden tek bir yorgunluk izi bile vermedin. Akşam çökerken Kule'nin geniş koridorları her zamankinden çok daha sessizdi. Gündüzki o üç saniyelik boşluk neredeyse herkesi senden bir adım uzağa itmişti. Yalnızca iki kişi hâlâ yerindeydi: açık terasta rüzgâra karşı karanlık şehri izleyen Natasha Romanoff ve kütüphanenin cam kenarında, senin Beyaz Göz'üne kısmen de olsa direnebilmiş tek kişi olarak sessizce sıcak çayını yudumlayan Wanda Maximoff. İkisi de seni aramadı, ikisi de yargılamadı."
            ),
            decisionTitle = "Akşamın Sessizliği — Bir Adım Atacak Mısın?",
            choicePrompt = "Gündüzki güç gösterisinden sonra mesafe daha da keskin. Bu gece birine yaklaşacak mısın, yoksa kendi duvarlarının ardında mı kalacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c2_natasha",
                    label = "Terasa çık ve Natasha'nın yanına geç — gölgelerin dilinden sakin bir denge kur.",
                    toneHint = "Natasha • Sessiz Güven • Bilinçli Adım",
                    nextNodeId = "ch2_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.WARMTH, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHADOW_ALLIANCE to 2,
                            StoryFlagAxis.HUMANITY_ANCHOR to 2
                        ),
                        setFlags = mapOf("ch2_bond_step" to "natasha", "natasha_tone" to "step_taken"),
                        branchTag = "Natasha'ya İlk Adım",
                        impactSummary = "Natasha gündüzki Beyaz Göz şovunu eleştirmedi; 'Kontrolünü kaybetseydin şu an konuşmuyor olurduk' dedi. Aranızda sessiz bir güven filizlendi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_wanda",
                    label = "Kütüphaneye uğra ve Wanda'nın yanına otur — Beyaz Göz'e nasıl direndiğini sor.",
                    toneHint = "Wanda • Zihinsel Anlayış • Bilinçli Adım",
                    nextNodeId = "ch2_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.WARMTH, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.PSYCHIC_RESONANCE to 3,
                            StoryFlagAxis.HUMANITY_ANCHOR to 2
                        ),
                        setFlags = mapOf("ch2_bond_step" to "wanda", "wanda_tone" to "step_taken"),
                        branchTag = "Wanda'ya İlk Adım",
                        impactSummary = "Wanda ile zihin kontrolünün ağırlığını paylaştın; ikiniz de başka insanların korkusunu fazla iyi biliyordunuz.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_alone",
                    label = "İkisine de yaklaşma — kendi odana çekil ve sınırlarını tamamen kapalı tut.",
                    toneHint = "Mesafeli • Bağımsız • Yalnız Yol",
                    nextNodeId = "ch2_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 1),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.MYSTERY_AURA to 2
                        ),
                        setFlags = mapOf("ch2_bond_step" to "alone", "natasha_tone" to "distant", "wanda_tone" to "distant"),
                        branchTag = "Bilinçli Mesafe",
                        impactSummary = "Kimseye yaklaşmadın; mesafeni bozmadan yarınki deneme sürecine hazırlandın.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── BÖLÜM II — DAL 2C: TAVİZSİZ SINIR ───────────────────────────────────
        BookSceneNode(
            id = "ch2_s2_refuse",
            chapterNumber = 2,
            sceneOrder = 2,
            sceneTitle = "Cam Masadaki Gerilim",
            locationTag = "AVENGERS KULESİ • KONFERANS SALONU",
            paragraphs = listOf(
                "\"Hayır,\" dedin. Tek kelime. Seksen beşinci katın geniş cam duvarları arasında soğuk bir kurşun gibi yankılandı.",
                "Sandalyenden hafifçe öne eğildin, sesin hiç yükselmedi ama her hecesi keskin bir cam parçası kadar netti. \"Beş yaşımdan beri birileri kalın camların arkasından bana 'şimdi yap, görelim' dedi, Stark. Beyaz önlüklü doktorlar, hükümet ajanları, laboratuvar teknisyenleri, sorgucular... Hepsi elinde ölçüm cihazıyla, yüzünde saklayamadığı o ilkel korkuyla ne kadar tehlikeli olduğumu görmek istedi. Sirk hayvanı değilim. Kimseyi eğlendirmek, kimin daha büyük silaha sahip olduğunu ölçtürmek ya da egolarını rahatlatmak için şov yapmayacağım. Beni sahada göreceksiniz — bu cam masada değil.\"",
                "Tony Stark bir an keskin, alaycı bir cevap verecek gibi öne eğildi; kaşları çatılmıştı, parmakları masanın yüzeyinde sabırsızca ritim tutuyordu. \"Bak evlat, bu ego meselesi değil, temel güvenlik—\"",
                "Ama hemen karşısında oturan Natasha Romanoff'un tek bir sakin, ölçülü bakışıyla cümlesi yarım kaldı. Natasha sandalyesinden hafifçe doğrulup Tony'ye döndü. \"Haklı, Tony,\" dedi, sesi odadaki o sert gerilimi bir anda yarıya indirdi. \"Onu buraya bir deney faresi gibi camın arkasına koyup numara yaptırmak için çağırmadık. Eğer yirmi yıllık S.H.I.E.L.D. avını gerçekten bitirmek için masaya oturduysak, ona silah değil insan gibi davranmamız gerekiyor.\"",
                "Masanın diğer ucunda Wanda Maximoff başını yavaşça eğip onayladı; gözlerinden eski bir Sokovia laboratuvarının, kurşun duvarların ve kabloların tanıdık gölgesi geçip gitti. Bruce Banner tabletlerinin ekranını sessizce kapattı: \"Haklısınız. Ölçüm cihazlarına ihtiyacımız yok.\"",
                "Sen ise masanın ortasında duran mat titanyum bilekliği iki parmağınla aldın. Bileğine takmadın — ama masada da bırakıp kapıyı çarpmadın. Ceketinin iç cebine kaydırdın. \"Bana ulaşmak için buna ihtiyacınız olursa açarım,\" dedin doğrudan Steve Rogers'a bakarak. \"Ama kolumda hiçbir hükümetin ya da kurumun metali olmayacak. Bu konuda pazarlık yok.\"",
                "Steve Rogers birkaç saniye seni sessizce süzdü, ardından ağırbaşlı, saygılı bir kabullenmeyle başını salladı: \"Öyle olsun Aiden. Sınırlarına saygı duyuyoruz. Otuz günlük deneme süresi yarın sabah başlıyor. Kurallar iki taraflı — sen bize ilk günden güvenmek zorunda değilsin, biz de seni camın arkasında test etmeyeceğiz. Ama zamanı geldiğinde sahada omuz omuza duracağız.\"",
                "Toplantı dağıldığında öğleden sonra kimse kapını çalmadı. Kule'nin yüksek katlarında, yirmi yıldır ilk kez birinin seni aramadığı, takip etmediği ama aynı zamanda seninle didişmediği garip bir huzur vardı. Kendi dairenin camından saatlerce Manhattan bulutlarını izledin. Akşam çökerken seksen dördüncü katın loş koridorundan geçtiğinde iki farklı ışık yanıyordu: açık terasta akşam rüzgârına karşı şehri izleyen Natasha Romanoff ve kütüphanenin cam kenarında kitap okuyan Wanda Maximoff. İkisi de senden tek bir adım beklemeden kendi sessizliğindeydi."
            ),
            decisionTitle = "Akşamın Sessizliği — Bir Adım Atacak Mısın?",
            choicePrompt = "Sınırlarını net çizdin. Bu gece birine bilinçli bir adım atacak mısın, yoksa yalnız mı kalacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch2_c2_natasha",
                    label = "Terasa çık ve Natasha'nın yanına geç — gölgelerin dilinden sakin bir denge kur.",
                    toneHint = "Natasha • Sessiz Güven • Bilinçli Adım",
                    nextNodeId = "ch2_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.WARMTH, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHADOW_ALLIANCE to 2,
                            StoryFlagAxis.HUMANITY_ANCHOR to 2
                        ),
                        setFlags = mapOf("ch2_bond_step" to "natasha", "natasha_tone" to "step_taken"),
                        branchTag = "Natasha'ya İlk Adım",
                        impactSummary = "Terasta Natasha'nın yanında durdun; 'Bazen hayır demek, güç göstermekten daha zordur' dedi. Sakin, baskısız bir anlayış kuruldu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_wanda",
                    label = "Kütüphaneye uğra ve Wanda'nın yanına otur — iki farklı gücün ağırlığını paylaş.",
                    toneHint = "Wanda • Zihinsel Anlayış • Bilinçli Adım",
                    nextNodeId = "ch2_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.WARMTH, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.PSYCHIC_RESONANCE to 3,
                            StoryFlagAxis.HUMANITY_ANCHOR to 2
                        ),
                        setFlags = mapOf("ch2_bond_step" to "wanda", "wanda_tone" to "step_taken"),
                        branchTag = "Wanda'ya İlk Adım",
                        impactSummary = "Wanda seni tek kelimeyle anladı: 'Bana da uzun süre camın arkasından baktılar.' İkiniz arasında sessiz bir yakınlık doğdu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch2_c2_alone",
                    label = "İkisine de yaklaşma — kendi odana çekil ve sınırlarını tamamen kapalı tut.",
                    toneHint = "Mesafeli • Bağımsız • Yalnız Yol",
                    nextNodeId = "ch2_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 1),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.MYSTERY_AURA to 2
                        ),
                        setFlags = mapOf("ch2_bond_step" to "alone", "natasha_tone" to "distant", "wanda_tone" to "distant"),
                        branchTag = "Bilinçli Mesafe",
                        impactSummary = "Kimseye kapı açmadan kendi sessizliğinde kaldın.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── BÖLÜM II — FİNAL ────────────────────────────────────────────────────
        BookSceneNode(
            id = "ch2_s3_end",
            chapterNumber = 2,
            sceneOrder = 3,
            sceneTitle = "Şartlar İmzalandı",
            locationTag = "AVENGERS KULESİ • GECE YARISI",
            paragraphs = listOf(
                "Gece yarısını geçerken Manhattan'ın üzerindeki ağır, kurşun rengi yağmur bulutları yavaşça dağıldı ve gökyüzünde uzun zamandır ilk kez birkaç soluk yıldız göründü. Daha da önemlisi, ensendeki o ince, görünmez elektrik iğneleri nihayet tamamen susmuştu. Yirmi yıldır kıtalar, yabancı gezegenler ve kırık zaman çizgileri boyunca peşini bırakmayan S.H.I.E.L.D. yüksek frekans taramaları o akşam ilk kez New York semalarından resmen çekilmişti.",
                "Seksen dördüncü kattaki odanın geniş penceresinin kenarında durup elindeki sıcak çaya baktın. Aşağıda, yüzlerce metre derinde, sekiz milyon insan hiçbir şeyden habersiz uyuyordu; hiçbiri gökyüzünün yirmi yıl önce bir gece maviye yarıldığını, ya da şu an bu cam kulenin tepesinde duran adamın tek bir nefesle bütün bir şehrin zamanını durdurabileceğini bilmiyordu. Yıllarca kaçmak sana tuhaf ama net bir hedef vermişti: uyanık kal, iz bırakma, yola devam et, kimseye alışma, arkana bakma.",
                "Bir anlığına masanın üstünde duran eski, yıpranmış deri sırt çantana baktın. Yirmi beş yıllık hayatının tamamı o tek çantanın içine sığıyordu: birkaç parça siyah kıyafet, üç farklı ülkeye ait sahte pasaport, birkaç yüz dolar nakit ve beş yaşından beri cebinde taşıdığın tek eşya — annenle babanın apartman kapısının paslanmış pirinç anahtarı. Artık o apartman bile yoktu, yerinde camdan bir ofis binası yükseliyordu. Ama anahtarı hiç atmamıştın.",
                "Şimdi, kaçmak zorunda olmadığın ilk gecede, çok daha ağır bir soruyla baş başaydın. Kaçmayı bıraktığında geriye ne kalır? Aiden Blackwood'un kim olduğunu — beş yaşındaki o çocuğun ardından yirmi yıl boyunca tuğla tuğla ördüğün duvarların arkasında neyin saklandığını — ilk kez durup görmek zorunda kalacaktın.",
                "Masada imzaladığın o görünmez anlaşma artık geçerliydi. Otuz günlük deneme süresi yarın sabah başlıyordu. Çantanı dolaba kaldırmadın, hâlâ kapının yanında duruyordu — ama ilk kez ışıkları kapattığında yatağın ucuna oturup dakikalarca kapıyı dinlemedin. Bu kulede birkaç kişinin seni sadece bir tehdit dosyası olarak görmediğini artık biliyordun."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch2_bond_step",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Terasta Sessiz Güven — Natasha",
                    paragraphs = listOf(
                        "Terasa çıkıp soğuk demir korkuluğun iki adım yanında durduğunda Natasha Romanoff başını hemen çevirmedi. Yüksek irtifanın rüzgârı kızıl saçlarından birkaç teli savururken tam iki dakika boyunca ikiniz de tek kelime etmediniz. Başka biriyle olsa bu sessizlik rahatsız edici olurdu; ama Natasha ile tuhaf biçimde dengeliydi — sanki ikiniz de kelimelerin çoğunlukla gerçeği saklamak için kullanıldığı dünyalardan geliyordunuz.",
                        "\"Dün geceki köfte için teşekkürler,\" dedin sonunda, sesin rüzgârda alçak ama net çıktı. \"O saatte açık olduğunu nereden bildin?\"",
                        "Natasha dudağının kenarında nadir, ölçülü bir kıvrımla sana döndü; çarpıcı mavi gözlerinle karşılaştığında bakışını kaçırmayan çok az insandan biriydi. \"Şehri izlemek benim işim Aiden,\" dedi sakin bir sesle. \"Ayrıca aç bir adamla sabah pazarlığı yapılmaz. Ve merak etme — bu kulede kimse senden olmadığın biri olmanı ya da duvarlarını bir gecede indirmeni beklemiyor. Ne zaman konuşmak istersen konuşursun. Susmak istersen de sorun değil.\"",
                        "\"İnsanlar genelde sessizlikten rahatsız olur,\" dedin, gözlerini tekrar Manhattan'ın ışıklarına çevirerek. Natasha hafifçe başını salladı: \"Kırmızı Oda'da büyüyenler sessizliği iyi tanır. Tehlikeli olan gürültüdür.\"",
                        "Ayrılırken arkandan seslenmedi, elini omzuna koymaya çalışmadı, senden bir sonraki görüşme için söz istemedi. Sadece rüzgârda hafifçe başını eğdi. Zorlamayan, beklemeyi bilen, gerektiğinde soğukkanlılıkla ölümcül olabilen bir gölge. Yirmi yılın ardından ilk kez birinin varlığı seni boğmamış, tam tersine zihnindeki gürültüyü susturmuştu."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch2_bond_step",
                    requiredFlagValue = "wanda",
                    callbackLabel = "Kütüphanede Kızıl Denge — Wanda",
                    paragraphs = listOf(
                        "Kütüphanenin loş eşiğinde durduğunda Wanda Maximoff başını elindeki kitaptan kaldırdı ama zihnine tek bir kıvılcım bile uzatmadı. Bu ayrıntı senin için her şeyden daha önemliydi — çünkü ikiniz de başka bir insanın zihnine dokunmanın ne kadar kolay, ama dokunmadan durmanın ne kadar büyük bir disiplin istediğini biliyordunuz. Karşısındaki deri koltuğa oturduğunda parmaklarının arasında dönen o ince kızıl ışık sessizce söndü.",
                        "\"Sana da o kelimeyi söylediler, değil mi?\" diye sordu Wanda alçak, duru bir sesle. İsim koymasına gerek yoktu; ikiniz de *canavar* kelimesini kastediyordunuz.",
                        "Başını yavaşça salladın. \"Dokuz yaşındayken. Yağmurlu bir sokakta, ilk kez kazayla ışınlandığım gece. Telsizden duydum.\" Wanda uzun, yorgun bir nefes verdi; gözleri yüksek camdan dışarıdaki karanlık şehre kaydı. \"İnsanlar anlayamadıkları şeyden korkar Aiden. Korktukları şeye de bir isim takarlar ki onu bir kafese kapatmak vicdanlarını acıtmasın. Sokovia'da bana da aynı gözle baktılar. Ama burada... en azından benim yanımda, elindeki gücü saklamak için kendini küçültmek zorunda değilsin.\"",
                        "\"Peki sen kontrolü nasıl tutuyorsun?\" diye sordun; bunu yıllardır kimseye sormamıştın. Wanda hafifçe gülümsedi: \"Bazen tutamıyorum. Ama artık bunu tek başıma taşımıyorum.\"",
                        "Kütüphaneden çıkarken ne seni tutmaya çalıştı ne de bir beklenti koydu. Yalnızca ensende kalan o sakin, zarif zihinsel sıcaklık — içeri girmeyen, sadece 'seni anlıyorum' diyen nadir bir yankı — gecenin geri kalanını biraz daha hafif kıldı."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch2_bond_step",
                    requiredFlagValue = "alone",
                    callbackLabel = "Bilinçli Mesafe",
                    paragraphs = listOf(
                        "Koridorun sonundaki iki ışığa da dönmedin. Ne terasa çıktın ne de kütüphanenin kapısından içeri adım attın. Kendi odana geçip kapıyı kapattın, pirinç mandalı çevirdin. Aiden Blackwood için yakınlık hiçbir zaman başkalarının beklentisiyle açılan bir kapı değildi — eğer bir gün birine doğru tek bir adım atacaksan, bu yalnızca senin kendi seçiminle ve senin zamanında olacaktı.",
                        "Ne Natasha ne de Wanda gece boyunca kapını çaldı. İkisi de koyduğun sınırı tek bir kırgınlık gölgesi olmadan kabul etti. Bu kulede duvarların hâlâ tamamen sana aitti."
                    )
                )
            ),
            isChapterEnd = true,
            chapterEndQuote = "Güç dünyayı bükebilir; ama kime bir adım atacağını yalnızca sen seçersin."
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM III: DENEME SÜRESİ (2 Büyük Dönüm Noktası)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch3_s1",
            chapterNumber = 3,
            sceneOrder = 1,
            sceneTitle = "Otuz Gün ve Antrenman Salonu",
            locationTag = "AVENGERS KULESİ • EĞİTİM SALONU • 14. GÜN",
            paragraphs = listOf(
                "İki hafta geçmişti. Yirmi yıl boyunca hiçbir ucuz motel odasında, hiçbir liman deposunda, hiçbir yabancı gezegenin mağarasında üç geceden fazla uyumamış biri için aynı yatakta on dört sabah üst üste uyanmak, başta neredeyse fiziksel bir huzursuzluk gibi gelmişti. İlk birkaç gece hâlâ uykuyla uyanıklık arasında refleksle odanın dört köşesini ve çıkışlarını sayıyor, koridordan geçen en ufak adım sesinde çarpıcı mavi gözlerin karanlığa anında açılıyordu. Yedinci gecede ise hayatında ilk kez sabaha kadar kesintisiz uyudun — ve uyandığında saat dokuza geliyordu. İlk tepkin panik değil, tuhaf bir şaşkınlık oldu.",
                "İkinci haftaya girerken Kule'nin kendine özgü, gürültülü ama şaşırtıcı derecede insani ritmine alışmaya başladığını fark ettin. Sabahları sekseninci kattaki ortak mutfakta Clint Barton'ın İtalyan espresso makinesiyle yürüttüğü sessiz ama inatçı savaşı izlemek, Bruce Banner'ın alt kattaki laboratuvardan dalgın dalgın çıkıp elindeki enerji haritalarını koltuğa bırakması, Tony Stark'ın ise her karşılaşmanızda güya tamamen tesadüfen enerji spektrumunu ölçmek için cebinden yeni bir sensör çıkarması... Bir gün öğleden sonra, gökyüzü serin ve açıkken Kule'den yürüyerek çıktın ve Hell's Kitchen'ın dar ara sokaklarından geçip nihayet o buğulu camlı küçük Türk lokantasının kapısını ittin.",
                "İçeri girdiğinde kapıdaki küçük pirinç zil çaldı. Kömür ateşinin sıcaklığı, ızgarada cızırdayan köftelerin, közlenmiş yeşil biberin ve taze ekmeğin kokusu soğuk New York rüzgârını anında unutturdu. Tezgâhın arkasındaki kır saçlı yaşlı Türk usta başını kaldırıp yüzüne baktığında iki saniye gözlerini kırpıştırdı — o çarpıcı, gerçek dışı mavi gözlere herkes gibi bir saniyeden fazla doğrudan bakamadı — sonra yüzüne samimi, sıcak bir gülümseme yayıldı. \"Hoş geldin evlat,\" dedi aksanlı İngilizcesiyle. \"Buralarda yenisin galiba?\"",
                "\"Eskiden buralıydım,\" dedin, köşedeki pencere kenarı masasına otururken. \"Uzun süre uzaktaydım.\" Usta beş dakika sonra önüne dumanı tüten iki porsiyon ızgara köfte, sumaklı soğan, közlenmiş biber, sıcak tırnak pide ve ince belli bardakta tavşan kanı çay koydu. İlk lokmayı aldığında omuzlarındaki görünmez gerginlik çözüldü. Yemeğinin ortasında fark ettiğin şey seni bile şaşırttı: yirmi yılın ardından ilk kez bir lokantada sırtını duvara yaslamayı unutmuştun. Tek bir bakışla zamanı yavaşlatabilen, kıtalar arası ışınlanabilen bir adamı dünyaya bağlayan şeyin kömür ateşinde pişmiş bir tabak Türk köftesi olması neredeyse gülünçtü — ama gerçekti.",
                "On ikinci günde Bruce Banner'la yetmiş beşinci kattaki güçlendirilmiş enerji odasında ilk tamamen gönüllü ölçümü yaptınız. Hiçbir kelepçe, hiçbir silah, hiçbir gizli protokol yoktu. Odanın ortasında durdun, sağ avucunun içinde saf mavi bir kozmik enerji küresi açtın, etrafındaki havayı ve zamanı yarım saniye ağırlaştırdın ve sessizce kapattın. Bruce holo-ekrandaki dalga desenlerini incelerken kaşları derin bir çizgiyle çatıldı.",
                "\"Aiden,\" dedi Bruce, sesindeki şaşkınlık bilimsel meraktan çok gerçek bir endişeye dönüşmüştü. \"Bu enerji sadece dışarıdan emdiğin ya da doğuştan gelen bir mutasyon değil. Beş yaşındayken o mavi yarık hücrelerinin içine bir tür kozmik frekans imzası işlemiş. Ve garip olan şu: bu dalga boyunun kenarında yapay bir harmonik kesik var. Yani doğada kendiliğinden açılan bir portal gibi durmuyor. Sanki birisi... o kapıyı laboratuvar koşullarında yapay olarak zorlamış.\"",
                "\"Beş yaşındaydım,\" dedin alçak sesle. \"O gece annemle babamı alan şey bir kaza değil miydi?\" Bruce yavaşça başını iki yana salladı: \"Henüz kesin kanıtım yok Aiden. Ama Tony'yle eski 2006 S.H.I.E.L.D. atmosfer kayıtlarını tarıyoruz. Bir şey bulduğumuz an ilk sana söyleyeceğim.\"",
                "O cümle iki gündür zihninin arkasında soğuk bir kıymık gibi duruyordu. On dördüncü günün sabahında Steve Rogers seni yetmiş ikinci kattaki güçlendirilmiş antrenman salonuna çağırdı. Geniş, yüksek tavanlı salonun zemini vibranyum takviyeli darbe emici alaşımla kaplıydı; duvarlardaki optik ve basınç sensörleri her darbenin hızını ve her enerji dalgasının yoğunluğunu canlı olarak kaydediyordu. Yukarıdaki kalın gözlem camının arkasında Tony Stark, Bruce Banner, Natasha Romanoff ve Wanda Maximoff sessizce yerlerini almıştı.",
                "\"Bugün bir değerlendirme ya da deney değil, sadece refleks ve uyum,\" dedi Steve, klasik yuvarlak kalkanını sol koluna geçirip deri kayışını sıkarken. Gözlerinde ne bir üstünlük çabası ne de kuşku vardı — yalnızca yarın bir kriz çıktığında sahada sırtını yaslayacağı adamı tanımak isteyen bir askerin dürüst dikkati. \"Bir operasyona çıktığımızda kimin ne zaman öne atılacağını, kimin ne zaman duracağını ve gerçek baskı altında nasıl nefes aldığımızı bilmeliyiz. Hazır mısın, Aiden?\"",
                "Başını sallayıp ağırlığını hafifçe öne verdin. İlk saniyede Steve Rogers'ın neden yaşayan bir efsane olduğunu kendi kemiklerinde hissettin. Süper asker refleksleriyle aranızdaki dört metrelik mesafeyi yarım saniyede kapattı; vibranyum kalkanın kenarı doğrudan sol omzuna doğru keskin bir yay çizerek savruldu. Çevrendeki havayı milimetre kadar büküp yana kaydın, sağ bileğini döndürüp darbenin açısını bozdun. Yumruğun kalkanın tam merkezine indiğinde koca salonda derin, yankılı bir metal çınlaması koptu.",
                "Üç dakika boyunca salon sadece hızlı ayak sesleri, kalkanın havayı yaran vınlaması ve kesik nefeslerle doldu. Steve'in her hamlesinde onlarca yıllık savaş disiplini vardı; en ufak açık vermiyor, bir sonraki adımını üç hamle önceden okuyup ağırlık merkezini ona göre kuruyordu. Ama sen de yirmi yıldır hayatta kalmak için gölgelerde, sokaklarda ve yabancı diyarlarda dövüşmüştün — ve hâlâ gerçek kozmik gücünün yüzde onunu bile açmamıştın.",
                "Dördüncü dakikaya girerken Steve tempoyu aniden yükseltti. Üst üste iki kısa kroşe ve ardından alçaktan gelen bir kalkan savurmasıyla seni salonun kuzey duvarına doğru sıkıştırdı; ayağını kilitleyip seni yere indirmeye hazırlanıyordu. Yukarıdaki gözlem camının arkasında Tony kahvesini bırakmış, Natasha kollarını kavuşturmuş, Wanda dikkatle öne eğilmişti. Herkes bu sınavı nasıl bitireceğini — ve baskı altında gücünü nasıl kullandığını — izliyordu."
            ),
            decisionTitle = "Antrenman Salonundaki Sınav",
            choicePrompt = "Steve seni köşeye sıkıştırmak üzere ve gözlem camının arkasındaki herkes nasıl karşılık vereceğini izliyor. Bu sınavı nasıl bitireceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c1_honor",
                    label = "Güçlerini kapalı tut — saf hız ve dövüş disipliniyle Steve'i kalkanından ayır, adil bir denge kur.",
                    toneHint = "Onurlu • Fiziksel Disiplin • Ekip Uyumu",
                    nextNodeId = "ch3_s2_honor",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 2, BookCharacterId.CLINT to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.LOYALTY, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 3,
                            StoryFlagAxis.AVENGERS_TRUST to 3,
                            StoryFlagAxis.FIELD_INITIATIVE to 2
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch3_sparring" to "honor"),
                        branchTag = "Onurlu Düello",
                        impactSummary = "Kozmik gücüne başvurmadan Steve'le başa baş çıkıp kalkanını kilitledin. Steve gerçek bir saygıyla elini sıktı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c1_cosmic",
                    label = "Kısa bir kozmik enerji dalgası ve mikro-ışınlanma zinciriyle maçı üç saniyede bitir.",
                    toneHint = "Kozmik Hız • Mutlak Üstünlük",
                    nextNodeId = "ch3_s2_cosmic",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.THOR to 3, BookCharacterId.STEVE to 1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.CURIOSITY, 3),
                            Triple(BookCharacterId.BRUCE, CharacterFlagAxis.CURIOSITY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.COSMIC_AWAKENING to 3,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 1,
                        setFlags = mapOf("ch3_sparring" to "cosmic"),
                        branchTag = "Kozmik Fırtına",
                        impactSummary = "Üç saniyede Steve'in arkasına sıçrayıp enerji kalkanıyla hamlesini sıfırladın; Tony verileri görünce neredeyse tableti düşürüyordu.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c1_tactical",
                    label = "Saldırıyı emip Steve'in ritmini okuyan küçük bir zaman yavaşlatmasıyla temiz bir beraberlikte dur.",
                    toneHint = "Dengeli • Kontrollü • Sıfır Gösteriş",
                    nextNodeId = "ch3_s2_tactical",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 3, BookCharacterId.WANDA to 2, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 2,
                            StoryFlagAxis.AVENGERS_TRUST to 2
                        ),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch3_sparring" to "tactical"),
                        branchTag = "Sessiz Denge",
                        impactSummary = "Ne ezdin ne ezildin — tam gerektiği kadar güç kullandın. Natasha gözlem camından hafifçe başını salladı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "orta"
                    )
                )
            )
        ),

        // ── BÖLÜM III — DAL 3A/3B/3C → S.H.I.E.L.D. ALARMI ──────────────────────
        BookSceneNode(
            id = "ch3_s2_honor",
            chapterNumber = 3,
            sceneOrder = 2,
            sceneTitle = "Kırmızı Alarm ve Yasak Kelime",
            locationTag = "AVENGERS KULESİ • 85. KAT KORİDORU",
            paragraphs = listOf(
                "Kozmik enerjini tek bir kıvılcım bile sızdırmadan tamamen kapalı tuttun. Bu sınavı güçle değil, saf disiplinle bitirmek istedin. Steve'in alçaktan savrulan vibranyum kalkanının üzerinden çevik bir sıçramayla aştın, inerken sağ elinle bileğinin iç kısmını yakalayıp ağırlık merkezini kendi hamlesinin yönünde tersine çevirdin. İki saniyelik keskin, hızlı bir yakın dövüş kilidinin ardından Steve Rogers'ın sırtı vibranyum zemine değdi; kalkanı kolundan sıyrılıp üç metre öteye kaydı. Hiç beklemeden elini uzatıp onu ayağa kaldırdın.",
                "Steve nefes nefeseydi, alnında ince ter damlaları parlıyordu ama yüzünde son derece dürüst, sıcak bir gülümseme vardı. \"İşte buna saygı duyarım,\" dedi omzundaki tozu silkeleyerek. \"Kozmik güce ya da süper seruma başvurmadan beni kalkanımdan ayıranların sayısı bir elin parmaklarını geçmez. Sahada da bu kadar disiplinliysen, bu takım seninle çok daha güçlü olacak Aiden.\" Yukarıdaki gözlem camından Clint ıslık çaldı, Natasha ise kollarını kavuşturmuş halde hafifçe başını salladı.",
                "Havlunu alıp terini silerken, yirmi yılın ardından ilk kez bir takımın içinde gerçekten nefes alabildiğini hissettin. Ama o sakin, insani atmosfer yalnızca on saniye sürdü.",
                "Bir anda tüm Kule'nin tavan ışıkları keskin, uyarıcı bir kırmızıya döndü.",
                "[[ALARM|FRIDAY · ACİL GÜVENLİK UYARISI|İzinsiz S.H.I.E.L.D. taktik müdahale timi 85. kat helikopter pistinden zorla giriş yaptı. Yetki Kodu: Dünya Güvenlik Konseyi Acil Müdahale — Hedef: Aiden Blackwood.]]",
                "Asansörün kapıları 85. katta açıldığında manzara keskin bir soğukluk taşıyordu. Ağır karbon zırhlı, siyah kasklı altı S.H.I.E.L.D. ajanı geniş cam koridoru yarım daire şeklinde tamamen kesmişti. Ellerinde sıradan kurşun değil, yüksek frekanslı nötralizasyon ve kozmik enerji bastırma tüfekleri vardı — ve altı namlunun tamamı, kırmızı lazer noktalarıyla birlikte doğrudan senin göğsüne çevriliydi. Tony Stark'ın zırh eldiveni anında bileğine kenetlendi; Wanda'nın parmak uçlarında kızıl alevler yükseldi; Natasha iki adım yana açılıp sessizce çapraz ateş açısını kapattı. Steve Rogers ise tereddüt bile etmeden kalkanını kaldırıp seninle namluların arasına girdi.",
                "\"Silahları hemen indirin!\" diye gürledi Steve, sesi koridorun cam duvarlarında yankılandı. \"Bu kulede Konsey'in izinsiz baskın yetkisi geçmez! Anlaşmamız netti — Aiden Blackwood otuz gün boyunca Avengers'ın koruması ve sorumluluğu altında!\"",
                "\"Buraya Nick Fury'nin imzası olmadan mı geldiniz?\" diye sordu Tony buz gibi bir sesle. \"Çünkü benim uydum Fury'den tek bir onay kodu görmüyor. Şu an izinsiz mülke giriyorsunuz, üstelik benim evime.\"",
                "Öndeki kıdemli komutan kaskının siperliğini sertçe yukarı kaldırdı. Gözleri doğrudan senin çarpıcı mavi gözlerine değdiğinde, bir saniyeliğine yüzünden o tanıdık, ilkel ürperti geçti — ve o korku hemen saldırgan bir öfkeye dönüştü. Parmağı tetiğin üzerinde gerildi. \"Çekil Yüzbaşı!\" diye bağırdı. \"Konsey bu sabah 2006 dosyasını yeniden açtı. Sonsuzluk Taşı düzeyindeki bu **canavar** şehrin göbeğinde serbest dolaşamaz! Ya şimdi diz çöküp teslim olur ya da burada zorla indiririz!\"",
                "O kelime. *Canavar.*",
                "Koridordaki hava bir anda beş derece soğudu. Tavan lambaları yüksek gerilim yemiş gibi şiddetle cızırdadı ve iki ampul patladı. Dokuz yaşındaki o sağanak yağmurlu sokak, burnundan sızan sıcak kan, ajanların telsizinden gelen o fısıltı, yıllarca kaldığın soğuk sorgu odaları ve yirmi yıllık kaçış tek bir saniyede göğsünde toplandı. Gözlerinin kenarında hem soğuk, kör edici bir beyaz hem de tehlikeli, kontrolü zor bir kızıl kıvılcım yanıp söndü. Yerdeki küçük toz zerrecikleri ve kırık cam parçaları yerçekiminden kopup havaya yükseldi. İstersen bu altı adamı tek bir nefesle yere serebilir, Beyaz Göz'le zihinlerini paramparça edebilir ya da buradan başka bir kıtaya ışınlanıp Avengers Kulesi'yle bütün bağını o saniye yakabilirdin."
            ),
            decisionTitle = "S.H.I.E.L.D. Alarmı — Kırılma Anı",
            choicePrompt = "Namlu göğsünde, yasak kelime söylendi ve bütün ekip ne yapacağını izliyor. Bu krizi nasıl çözeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c2_stand_ground",
                    label = "Öfkeni bastır, tek bir enerji kalkanıyla namluları indir ama kimseye zarar verme — Steve ve Natasha'ya sırtını yasla.",
                    toneHint = "Kontrol • Ekip Güvenini Kazan • Büyük İrade",
                    nextNodeId = "ch3_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 4, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.LOYALTY, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.WARMTH, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 4,
                            StoryFlagAxis.HUMANITY_ANCHOR to 3,
                            StoryFlagAxis.TOWER_BELONGING to 3
                        ),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch3_alarm" to "stood_ground"),
                        branchTag = "Kule'de Kalış",
                        impactSummary = "İçindeki fırtınayı durdurdun, kimseyi incitmeden silahları susturdun. Steve komutanı kule dışına attı — deneme süren o an resmen bitti.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c2_white_eye_lock",
                    label = "Beyaz Göz'ü aç — altı ajanı oldukları yerde felç et ve komutanın elinden silahı zorla al.",
                    toneHint = "Beyaz Göz • Mutlak Otorite • S.H.I.E.L.D. Kırılması",
                    nextNodeId = "ch3_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 2, BookCharacterId.WANDA to 1, BookCharacterId.STEVE to -2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.CURIOSITY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 3,
                            StoryFlagAxis.SHIELD_SUSPICION to 4,
                            StoryFlagAxis.RED_EYE_INSTABILITY to 2
                        ),
                        redEyeDelta = 1,
                        fatigueDelta = 1,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("ch3_alarm" to "white_eye_lock"),
                        secretUnlocked = "Konsey'in Gizli Emri",
                        branchTag = "Beyaz Göz Kilitlenmesi",
                        impactSummary = "Altı ajanı tek bakışla dondurdun ve komutanın zihninden bu baskının Fury'den değil, Konsey içindeki gizli bir elden geldiğini öğrendin.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c2_vanish_return",
                    label = "Hiçbirine karşılık vermeden anında çatıya ışınlan — kendi başına nefes al ve kuleye dönüp dönmeyeceğine kendin karar ver.",
                    toneHint = "Kaçış Refleksi • Bağımsız • Yalnızlık",
                    nextNodeId = "ch3_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 2, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.EMPATHY, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.TOWER_BELONGING to -1
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 0,
                        setFlags = mapOf("ch3_alarm" to "vanished"),
                        branchTag = "Çatıda Hesaplaşma",
                        impactSummary = "Çatışmadan çekilip çatıya sıçradın; yarım saat sonra yanına tek gelen ve tek kelimeyle suçlamayan Natasha oldu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch3_s2_cosmic",
            chapterNumber = 3,
            sceneOrder = 2,
            sceneTitle = "Kozmik Hız ve Kırmızı Alarm",
            locationTag = "AVENGERS KULESİ • 85. KAT KORİDORU",
            paragraphs = listOf(
                "Üç saniye. Tek yaptığın göz kırpmasıyla birlikte içindeki kozmik akışı serbest bırakmaktı. Geniş antrenman salonunda keskin, berrak bir mavi iz çizerek üç ardışık mikro-ışınlanma yaptın — önce Steve'in sağına, yarım saniye sonra tam arkasına, ardından kalkanının üzerine. Vibranyum kalkanın kinetik darbesini avucunun içinde tamamen emip yumuşak, kontrollü bir kozmik enerji dalgasıyla geri verdin; Steve dengesini kaybetmeden üç adım geriye kaydı ve kendini yarı şeffaf, geometrik mavi bir enerji çemberinin içinde buldu. Üç saniyede maç bitmişti.",
                "Yukarıdaki kalın gözlem camının arkasında Tony Stark elindeki tableti neredeyse yere düşürüyordu: \"Üç ayrı mekânsal sıçrama, yüzde yüz kinetik emilim ve zeminde sıfır yanık izi... Thor bunu görse kıskançlıktan çekicini duvara fırlatırdı.\" Steve nefesini toparlayıp kalkanını indirirken başını gerçek bir takdirle salladı: \"Bunu sahada düşmanlarımızın görmesini isterim Aiden. Ama üçlü sıçrama nefesini biraz kesmiş gibi — yorgunluğunu saklama, sınırlarını bilmek güçsüzlük değil.\"",
                "Havlunu alıp boynundaki teri sildiğin anda tüm Kule'nin tavan ışıkları keskin, uyarıcı bir kırmızıya döndü. FRIDAY'in acil durum sesi tüm koridorları doldurdu: \"Uyarı. İzinsiz S.H.I.E.L.D. taktik müdahale timi 85. kat helikopter pistinden zorla giriş yaptı. Yetki kodu: Dünya Güvenlik Konseyi Acil Müdahale — Hedef: Aiden Blackwood.\"",
                "Asansörün kapıları 85. katta açıldığında ağır karbon zırhlı, siyah kasklı altı S.H.I.E.L.D. ajanı geniş cam koridoru yarım daire şeklinde kesmişti. Ellerindeki yüksek frekanslı nötralizasyon tüfeklerinin kırmızı lazer noktaları doğrudan senin göğsünde toplandı. Tony'nin zırh eldiveni anında bileğine kenetlendi; Wanda'nın parmak uçlarında kızıl kıvılcımlar parladı; Natasha iki adım yana açılıp çapraz ateş açısını kapattı. Steve Rogers ise tereddüt etmeden kalkanını kaldırıp seninle namluların arasına girdi: \"Silahları hemen indirin! Bu kulede Konsey'in izinsiz baskın yetkisi geçmez!\"",
                "Öndeki kıdemli komutan kaskının siperliğini sertçe yukarı kaldırdı. Gözleri doğrudan senin çarpıcı mavi gözlerine değdiğinde yüzünden bir anlık ilkel panik geçti — ve o korku anında saldırgan bir öfkeye dönüştü. Parmağı tetiğin üstünde gerildi. \"Çekil Yüzbaşı!\" diye bağırdı. \"Konsey bu sabah 2006 dosyasını yeniden açtı. Bu **canavar** şehrin göbeğinde serbest dolaşamaz! Ya şimdi diz çöküp teslim olur ya da burada zorla indiririz!\"",
                "O kelime. *Canavar.*",
                "Koridordaki hava bir anda buz kesti. Tavan lambaları yüksek gerilim yemiş gibi şiddetle cızırdadı ve iki ampul patladı. Dokuz yaşındaki o sağanak yağmurlu sokak, telsizden gelen o fısıltı ve yirmi yıllık kaçış tek bir saniyede göğsünde toplandı. Gözlerinin kenarında hem soğuk, kör edici bir beyaz hem de tehlikeli bir kızıl kıvılcım yanıp söndü; yerdeki tozlar ve kırık cam parçaları yerçekiminden kopup havaya kalkarken bütün ekip ne yapacağını nefesini tutarak bekliyordu."
            ),
            decisionTitle = "S.H.I.E.L.D. Alarmı — Kırılma Anı",
            choicePrompt = "Namlu göğsünde, yasak kelime söylendi. Bu krizi nasıl çözeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c2_stand_ground",
                    label = "Öfkeni bastır, tek bir enerji kalkanıyla namluları indir ama kimseye zarar verme — Steve ve Natasha'ya sırtını yasla.",
                    toneHint = "Kontrol • Ekip Güvenini Kazan • Büyük İrade",
                    nextNodeId = "ch3_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 4, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.LOYALTY, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 4,
                            StoryFlagAxis.HUMANITY_ANCHOR to 3,
                            StoryFlagAxis.TOWER_BELONGING to 3
                        ),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch3_alarm" to "stood_ground"),
                        branchTag = "Kule'de Kalış",
                        impactSummary = "İçindeki fırtınayı durdurdun, kimseyi incitmeden silahları susturdun; deneme süren o an bitti.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c2_white_eye_lock",
                    label = "Beyaz Göz'ü aç — altı ajanı oldukları yerde felç et ve komutanın zihninden emrin kaynağını çek.",
                    toneHint = "Beyaz Göz • Mutlak Otorite • S.H.I.E.L.D. Kırılması",
                    nextNodeId = "ch3_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 2, BookCharacterId.STEVE to -2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.CURIOSITY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 3,
                            StoryFlagAxis.SHIELD_SUSPICION to 4,
                            StoryFlagAxis.RED_EYE_INSTABILITY to 2
                        ),
                        redEyeDelta = 1,
                        fatigueDelta = 1,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("ch3_alarm" to "white_eye_lock"),
                        secretUnlocked = "Konsey'in Gizli Emri",
                        branchTag = "Beyaz Göz Kilitlenmesi",
                        impactSummary = "Altı ajanı tek bakışla dondurdun ve baskının Konsey içindeki gizli bir elden geldiğini öğrendin.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c2_vanish_return",
                    label = "Hiçbirine karşılık vermeden anında çatıya ışınlan — kendi başına nefes al.",
                    toneHint = "Kaçış Refleksi • Bağımsız • Yalnızlık",
                    nextNodeId = "ch3_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 2, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.EMPATHY, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.TOWER_BELONGING to -1
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 0,
                        setFlags = mapOf("ch3_alarm" to "vanished"),
                        branchTag = "Çatıda Hesaplaşma",
                        impactSummary = "Çatışmadan çekilip çatıya sıçradın; yarım saat sonra yanına tek gelen Natasha oldu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch3_s2_tactical",
            chapterNumber = 3,
            sceneOrder = 2,
            sceneTitle = "Sessiz Denge ve Kırmızı Alarm",
            locationTag = "AVENGERS KULESİ • 85. KAT KORİDORU",
            paragraphs = listOf(
                "Ne ezmek istedin ne de gösteri yapmak. Steve'in vibranyum kalkanı omzuna doğru keskin bir açıyla inerken çevrendeki zamanı yarım saniyeliğine ince bir nefes kadar yavaşlattın — sadece onun adım ritmini ve omuz açısını tam okuyacak kadar. Darbenin bütün kinetik enerjisini sol avucunla sessizce sönümleyip sağ elinle kalkanın kenarını kilitledin; ikiniz aynı anda nefes nefese ama kusursuz bir beraberlikte durdunuz. Ne yere düşen vardı ne de kırılan bir gurur.",
                "Yukarıdaki kalın gözlem camının arkasında Natasha Romanoff'un hafifçe başını salladığını gördün — o, gerçek kontrolün ne kadar yıkıcı olabildiğin değil, tam olarak nerede durabildiğin olduğunu en iyi bilen insandı. Steve kalkanını indirip dürüstçe gülümsedi: \"İyi okudun Aiden. Gereğinden tek bir gram bile fazla güç harcamadın. Bu sahada hayat kurtarır.\"",
                "Ama o sakin, dengeli atmosfer yalnızca birkaç dakika sürdü. Bir anda tüm Kule'nin tavan ışıkları keskin bir kırmızıya döndü ve FRIDAY'in acil durum uyarısı koridorlarda yankılandı: \"Uyarı. İzinsiz S.H.I.E.L.D. taktik müdahale timi 85. kat helikopter pistinden zorla giriş yaptı. Yetki kodu: Dünya Güvenlik Konseyi Acil Müdahale — Hedef: Aiden Blackwood.\"",
                "Üst kat koridorunda ağır karbon zırhlı, siyah kasklı altı S.H.I.E.L.D. ajanı geniş cam koridoru kesmişti. Ellerindeki yüksek frekanslı nötralizasyon tüfeklerinin kırmızı lazer noktaları doğrudan senin göğsünde toplandı. Tony'nin zırh eldiveni bileğine kenetlendi, Natasha çapraz ateş açısını kapattı, Steve ise tereddüt etmeden seninle namluların arasına girdi: \"Silahları hemen indirin! Bu kulede Konsey'in izinsiz baskın yetkisi geçmez!\"",
                "Öndeki komutan kaskının siperliğini yukarı kaldırdı; gözleri doğrudan senin çarpıcı mavi gözlerine değdiğinde bir anlık ilkel panikle parmağı tetiğin üstünde gerildi. \"Çekil Yüzbaşı!\" diye bağırdı. \"Konsey bu sabah 2006 dosyasını yeniden açtı. Bu **canavar** şehrin göbeğinde serbest dolaşamaz! Ya teslim olur ya da burada zorla indiririz!\"",
                "O kelime koridorda buz gibi yankılandı. *Canavar.* Tavan lambaları yüksek gerilim yemiş gibi şiddetle cızırdadı, iki ampul patladı, yerdeki tozlar ve cam kırıkları yerçekiminden kopup havaya yükseldi. Dokuz yaşındaki o sağanak yağmurlu sokak, telsizdeki fısıltı ve yirmi yıllık kaçış tek bir saniyede göğsünde toplandı; içindeki kozmik enerji tek bir parmak hareketini bekliyordu."
            ),
            decisionTitle = "S.H.I.E.L.D. Alarmı — Kırılma Anı",
            choicePrompt = "Namlu göğsünde, yasak kelime söylendi. Bu krizi nasıl çözeceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch3_c2_stand_ground",
                    label = "Öfkeni bastır, tek bir enerji kalkanıyla namluları indir ama kimseye zarar verme — ekibe sırtını yasla.",
                    toneHint = "Kontrol • Ekip Güvenini Kazan • Büyük İrade",
                    nextNodeId = "ch3_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 4, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.LOYALTY, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 4,
                            StoryFlagAxis.HUMANITY_ANCHOR to 3,
                            StoryFlagAxis.TOWER_BELONGING to 3
                        ),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch3_alarm" to "stood_ground"),
                        branchTag = "Kule'de Kalış",
                        impactSummary = "İçindeki fırtınayı durdurdun, kimseyi incitmeden silahları susturdun; deneme süren o an bitti.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c2_white_eye_lock",
                    label = "Beyaz Göz'ü aç — altı ajanı oldukları yerde felç et ve komutanın zihninden emrin kaynağını çek.",
                    toneHint = "Beyaz Göz • Mutlak Otorite • S.H.I.E.L.D. Kırılması",
                    nextNodeId = "ch3_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 2, BookCharacterId.STEVE to -2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.CURIOSITY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.WHITE_EYE_CONTROL to 3,
                            StoryFlagAxis.SHIELD_SUSPICION to 4,
                            StoryFlagAxis.RED_EYE_INSTABILITY to 2
                        ),
                        redEyeDelta = 1,
                        fatigueDelta = 1,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("ch3_alarm" to "white_eye_lock"),
                        secretUnlocked = "Konsey'in Gizli Emri",
                        branchTag = "Beyaz Göz Kilitlenmesi",
                        impactSummary = "Altı ajanı tek bakışla dondurdun ve baskının Konsey içindeki gizli bir elden geldiğini öğrendin.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch3_c2_vanish_return",
                    label = "Hiçbirine karşılık vermeden anında çatıya ışınlan — kendi başına nefes al.",
                    toneHint = "Kaçış Refleksi • Bağımsız • Yalnızlık",
                    nextNodeId = "ch3_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 2, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.EMPATHY, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.TOWER_BELONGING to -1
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 0,
                        setFlags = mapOf("ch3_alarm" to "vanished"),
                        branchTag = "Çatıda Hesaplaşma",
                        impactSummary = "Çatışmadan çekilip çatıya sıçradın; yarım saat sonra yanına tek gelen Natasha oldu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM III — FİNAL ───────────────────────────────────────────────────
        BookSceneNode(
            id = "ch3_s3_end",
            chapterNumber = 3,
            sceneOrder = 3,
            sceneTitle = "Deneme Süresi Bitti",
            locationTag = "AVENGERS KULESİ • GECE",
            paragraphs = listOf(
                "S.H.I.E.L.D. taktik timi helikopter pistinden kendi gemilerine bindirilip Kule'den çıkarıldığında 85. kat koridorunda fırtınadan hemen sonraki o ağır, ozon kokulu sessizlik kalmıştı. Kırık birkaç ampul dışında hasar yoktu, tek bir ajan bile yaralanmamıştı — ama artık odadaki herkes aynı karanlık gerçeği net biçimde görüyordu: seni yirmi yıldır avlayan gölge eller sadece S.H.I.E.L.D.'ın resmi emirleriyle hareket etmiyordu. Dünya Güvenlik Konseyi'nin en kapalı koridorlarında birileri senin Avengers Kulesi'nde güvende kalmandan — ya da daha doğrusu beş yaşındaki o geceyi hatırlamaya başlaman ihtimalinden — ciddi biçimde korkuyordu.",
                "Tony Stark vakit kaybetmeden konferans salonundaki şifreli, izlenemez uydu hattını açtı ve doğrudan Nick Fury'ye ulaştı. Beş dakika sonra uzun cam masanın ortasındaki mavi hologram canlandı. Siyah uzun paltosuyla Fury'nin görüntüsü belirdi; tek gözü önce odadaki hasarsız duvarlara, ardından masadaki sessiz ekibe ve sonunda doğrudan senin çarpıcı mavi gözlerine kilitlendi. Fury, bakışlarının o gerçek dışı ağırlığından tek milimetre bile kaçmayan nadir insanlardan biriydi.",
                "\"O baskını ben onaylamadım, Blackwood,\" dedi Fury, sesi alçak, boğuk ama çelik gibi sertti. \"Ve o taktik ekibin komutanı şu an görevden alındı. Ama ortada çok daha büyük bir sorun var. S.H.I.E.L.D.'ın üstünde yer alan Dünya Güvenlik Konseyi'nin içinde, benim bile en üst düzey erişimimi atlayarak 2006 yılına ait mühürlü dosyaları kurcalayan gizli bir el var. Kule'ye izinsiz tim gönderip seni susturmaya kalkmalarının gerçek sebebi de yirmi dakika önce ortaya çıktı.\"",
                "Fury elini salladı ve hologramda Manhattan'ın güneybatı kıyı şeridi, ardından koyu kırmızı bir enerji haritası açıldı. \"Manhattan Limanı, 19 numaralı rıhtım. Eski bir HYDRA lojistik deposunun altındaki yeraltı katında bu gece yüksek yoğunluklu bir kozmik rezonans sinyali yakaladık. Bruce'un seninle iki gün önce yaptığı spektrum taramasıyla karşılaştırdık: o cihazın yaydığı dalga boyu, senin kanındaki Sonsuzluk Taşı düzeyindeki imzayla yüzde doksan yedi oranında örtüşüyor.\"",
                "Salonda nefes kesen, buz gibi bir sessizlik çöktü. Bruce Banner ekrana yaklaştı, yüzü solmuştu: \"Sana söylemiştim Aiden... o mavi yarık doğal değildi. Ve kaynağı hâlâ bu şehirde bir yerlerde çalışıyor.\"",
                "Beş yaşındayken aileni elinden alan, seni yirmi yıl boyunca gezegenler ve kırık zaman çizgileri boyunca kaçmaya mahkûm eden o mavi gökyüzünün izi, yirmi yıl sonra aynı şehirde, birkaç kilometre ötede yeniden nefes alıyordu. Steve Rogers masanın kenarından sana baktı; bakışlarında artık hiçbir tereddüt yoktu. \"Otuz günlük deneme süresi bu gece resmen bitti Aiden,\" dedi. \"İlk gerçek görevin yarın gece başlıyor. Bu kez kaçmıyoruz, saklanmıyoruz — doğrudan kaynağına gidiyoruz.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch3_alarm",
                    requiredFlagValue = "stood_ground",
                    callbackLabel = "Ekip Kalkanı — Kule'de Kalış",
                    paragraphs = listOf(
                        "Koridorda o yasak kelime söylendiğinde bütün öfkeni ve yirmi yıllık yaranı kendi iradenle bastırmış, tek bir kontrollü mavi enerji kalkanıyla altı namluyu aşağı indirip kimseye tek çizik bile atmamıştın. O an Kule'deki bütün dengeler kalıcı olarak değişti. Toplantı dağılırken Steve omzuna hafifçe dokunup yanından geçti, sesi sakin ama sarsılmazdı: \"Bugün bu odada gerçek gücün ne olduğunu hepimiz gördük Aiden. Bir daha kimse bu çatı altında sana o kelimeyi söyleyemeyecek.\"",
                        "Natasha da masadan kalkarken gözlerinin içine bakıp hafifçe başını eğdi. Söz söylemesine gerek yoktu — artık bu kulede yalnızca bir misafir değil, sırtını yaslayabilecekleri bir müttefiktin."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch3_alarm",
                    requiredFlagValue = "white_eye_lock",
                    callbackLabel = "Beyaz Göz Kilitlenmesi — Gizli Emir",
                    paragraphs = listOf(
                        "Altı zırhlı ajanı Beyaz Göz'ün tek bir soğuk bakışıyla oldukları yerde mermer heykel gibi dondurduğunda, komutanın zihninin en karanlık köşesinden çektiğin o iki kelime hâlâ zihninde soğuk bir bıçak gibi parlıyordu: *Proje Ayna — Konsey İnfaz Emri.* Steve senin izinsiz zihin okumanı hâlâ onaylamıyordu, Tony ise göz ucuyla seni izlemeye devam ediyordu.",
                        "Ama koridordan geçerken Natasha Romanoff yarım saniye durup yalnızca senin duyacağın kadar alçak bir sesle tek cümle bıraktı: \"Bazen düşmanlar gerçeği nazikçe vermiyor. O ismi aklında tut, yarın gece işimize yarayacak.\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch3_alarm",
                    requiredFlagValue = "vanished",
                    callbackLabel = "Çatıdaki Sessiz Destek",
                    paragraphs = listOf(
                        "Koridordaki o yasak kelimeden sonra öfkeni serbest bırakmamak ve o altı adamı yakmamak için tek nefeste çatıya ışınlandığında, gece rüzgârı yüzüne çarpmış ve nefesini uzun süre toparlayamamıştın. Bir an gerçekten buradan tamamen kaybolmayı, bir daha bu şehre dönmemeyi düşündün. Yarım saat sonra çatı kapısı sessizce açıldığında gelen tek kişi Natasha Romanoff oldu.",
                        "Ne 'neden kaçtın' diye sordu, ne de aşağı inmeni emretti. Sadece iki adım yanında, rüzgârın ortasında durup karanlık Manhattan'a baktı: \"Bazen uzaklaşmak, kalıp her şeyi yakmaktan daha fazla kontrol ister. Hazır olduğunda aşağı in.\" Aiden Blackwood için en ağır ama en gerçek şey, birinin seni hiç zorlamadan beklemesiydi."
                    )
                )
            ),
            isChapterEnd = true,
            chapterEndQuote = "Canavar olup olmadığına seni avlayanlar değil, elinde güç varken neyi yıkmadığın karar verir."
        )
    ).associateBy { it.id }
}
