package com.example.book

/**
 * Extended, novel-length Chapters 4 and 5 of "Kozmik Sürükleniş" (Aiden Blackwood — MCU).
 * Rebuilt with long immersive prose, ONLY 2 monumental high-impact crossroads per chapter,
 * deep cross-chapter flags/echoes, and 100% fidelity to Aiden Blackwood's canon.
 */
object CosmicDriftChapters4To5 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 4,
            romanNumeral = "IV",
            title = "İlk Görev",
            titleEn = "First Mission",
            subtitle = "Manhattan Limanı, HYDRA Deposu ve Sovyet İmzası",
            description = "Fury'nin görevi, sis altındaki 19 numaralı rıhtım, pusu anında kritik karar ve beş yaşındaki kayboluşunla aynı frekansı taşıyan gizli kasa.",
            descriptionEn = "Fury's mission, Pier 19 in the fog, a high-stakes ambush, and a hidden vault carrying the exact frequency of your childhood disappearance.",
            estimatedMinutes = 11,
            accentHex = 0xFF34D399L,
            startNodeId = "ch4_s1",
            sceneCount = 3,
            decisionCount = 2
        ),
        BookChapterMeta(
            number = 5,
            romanNumeral = "V",
            title = "Kırmızı Buz",
            titleEn = "Red Ice",
            subtitle = "Elias Voss, 'Kaç' Uyarısı ve Sorgu Odası",
            description = "Kar altında gizemli saldırgan Elias Voss ile yüzleşme, cam sorgu odasında büyük itiraf ve geçmişinin karanlık kapısı.",
            descriptionEn = "Confronting Elias Voss in the snow, the interrogation room confession, and the dark door to your past.",
            estimatedMinutes = 10,
            accentHex = 0xFFF87171L,
            startNodeId = "ch5_s1",
            sceneCount = 3,
            decisionCount = 2
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM IV: İLK GÖREV (2 Büyük Dönüm Noktası)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch4_s1",
            chapterNumber = 4,
            sceneOrder = 1,
            sceneTitle = "Sis Altında 19 Numaralı Rıhtım",
            locationTag = "MANHATTAN LİMANI • 19 NUMARALI RIHTIM • 01:40",
            paragraphs = listOf(
                "Quinjet gece yarısından bir saat sonra sessiz uçuş modunda Hudson Nehri'nin üzerinden alçalırken, kokpit camına vuran ince yağmur damlaları Manhattan'ın kıyı ışıklarını uzun mavi çizgilere bölüyordu. Arka kabinde kimse gereksiz konuşmuyordu. Steve Rogers kalkanının kayışlarını son kez kontrol ediyordu; Clint Barton ok kılıfına üç farklı şok oku yerleştirmişti; Natasha Romanoff ise sessizce siyah eldivenlerini geçirirken ara sıra senin ellerine bakıyordu.",
                "\"İlk saha görevin için fena bir gece seçmedik,\" dedi Clint, kemerini çözerken yarı ciddi bir tebessümle. \"Eski bir HYDRA deposu, donmuş nehir rüzgârı ve ne olduğunu bilmediğimiz bir enerji çekirdeği. Avengers'a hoş geldin.\"",
                "Tam o anda zihninin kenarında kısa, soğuk bir mavi kıvılcım çaktı — bir saniyelik psişik görü. Göz kapaklarının ardında buz tutmuş bir cam kapsül, eski Kiril harfleriyle yazılmış yanık bir klasör ve beş yaşındaki kendi yüzün belirdi; sonra bir nefes içinde kayboldu. Burnunda belli belirsiz bir sıcaklık hissettin ama kanamadı. Natasha'nın o kısa dalgınlığı anında fark ettiğini gördün; ama sana ne gördüğünü sormadı, sadece başıyla iniş rampasını işaret etti. \"İniyoruz.\"",
                "Hudson Nehri'nden yükselen yoğun, tuzlu sis, 19 numaralı rıhtımdaki paslı yük konteynerlerini ve dev liman vinçlerini karanlık hayalet gemilere çevirmişti. Sokak lambalarının kirli sarı ışığı on adım ötede sisin içinde boğuluyordu; suyun çürümüş ahşap iskele direklerine vuran tekdüze sesi ve uzaktan gelen tek bir sis düdüğü dışında bütün rıhtım tuhaf, neredeyse yapay bir sessizliğe gömülmüştü. Siyah taktik ceketinin yakasını kaldırıp soğuk rüzgâra karşı durdun. Bu senin Avengers ile çıktığın ilk gerçek saha göreviydi — ama omuzlarında ilk kez yalnız olmadığın için değil, burada bulacağın şeyin geçmişine ait olduğunu bildiğin için ağır bir gerilim vardı.",
                "Kulağındaki kapalı devre iletişim hattından Steve Rogers'ın sakin, alışıldık komuta sesi geldi: \"Ekip, pozisyon teyidi. Kuzey ana kapısı bende, Clint doğu çatı hattında yüksek görüşte, Natasha batı iskelesinde gölge konumda. Wanda destek hattında bekliyor. Aiden, Bruce'un işaretlediği enerji imzası doğrudan 4 numaralı hangarın altındaki derin yeraltı katından geliyor. İki yüz metrelik çapta tek bir sivil yok.\"",
                "\"Anlaşıldı,\" dedi Clint Barton'ın sesi hattan. \"Çatıdaki iki gözcüyü sessizce indirdim — eski tip gece görüş gözlüğü takıyorlar ama silahları hiç de eski değil. En az yirmi dört profesyonel paralı asker içeride, üstelik tavan kirişlerinde iki adet ağır plazma kulesi kurulu. Bu bir kaçakçılık deposu değil, askeri bir kale. Biri burayı özellikle birinin gelmesini bekleyerek donatmış.\"",
                "Havanın içinde o tanıdık, soğuk bakır tadı vardı — beş yaşındayken New York gökyüzü maviye yarıldığı gece ağzında kalan ve yirmi yıldır hiçbir yağmurun, hiçbir yeni şehrin, hiçbir uzak gezegenin silmeye yetmediği aynı metalik tat. Gözlerini iki saniyeliğine kapattığında kozmik farkındalığın kalın beton duvarların, paslı çelik kapıların ve yeraltı katmanlarının ötesine sızdı. Eski HYDRA depolarından biri olduğu belliydi ama içerideki enerji sıradan bir Tesseract artığı ya da Chitauri hurdası değildi. Yeraltı katının tam merkezinde, nabız gibi atan, neredeyse canlı bir mavi rezonans çekirdeği hissediyordun — ve senin yaklaştığını hisseder gibi her beş saniyede bir titreşimi hızlanıyordu.",
                "\"Bu bir silah deposu değil,\" dedin kulaklıktan alçak, gergin bir sesle. \"İçerideki çekirdek benim frekansımı çağırıyor. Sanki yirmi yıl önce yarım kalmış bir kilit, anahtarının yaklaştığını anlamış gibi. Burayı kuranlar benim bir gün geleceğimi biliyormuş.\"",
                "Hattın diğer ucundan Tony Stark'ın Kule'deki komuta merkezinden gelen sesi duyuldu: \"Banner spektrumu canlı izliyor, Blackwood. İçerideki cihaz senin kanındaki kozmik imzanın eski, ham bir prototipi gibi titreşiyor. Ne olursa olsun o çekirdeği ve içindeki veri çekirdeklerini sağlam istiyoruz — ama kahramanlık yapıp kendini havaya uçurma, Kule'deki sağlık sigortan henüz onaylanmadı.\"",
                "\"Stark, ciddiyet,\" diye uyardı Steve ama sesi hafifti. Sonra Natasha Romanoff'un sesi kulaklıkta sakinleşti; o, tehlike büyüdükçe sesi daha da alçalan ve berraklaşan insanlardandı: \"İçerideki sistem profesyonelce tuzaklanmış Aiden. Kapı eşiklerinde yüksek voltajlı termal sensörler, yeraltı tünellerinde lazer kilitleri ve çekirdeğe bağlı kendi kendini imha eden veri kabloları var. Tek bir yanlış adım hem hangarı havaya uçurur hem de beş yaşındaki o geceyle ilgili bütün kanıtları saniyeler içinde kül eder. Hamle senin. Önden Steve ile birlikte kapıyı kırıp mı giriyorsun, benimle batı iskelesindeki gölge hattından mı sızacaksın, yoksa kendi bildiğin gibi mi hareket edeceksin?\"",
                "Soğuk nehir rüzgârı saçlarını savururken çarpıcı mavi gözlerin karanlık hangara kilitlendi. Tam cevap vermek üzereyken hangarın içinden ağır, hidrolik bir çelik kapının kapanma sesi yankılandı. Aynı saniyede kulaklıkta Clint Barton'ın keskin uyarısı çınladı: \"Dikkat! Çatıdaki bütün termal sensörler aniden açıldı — tavan kuleleri tam güç mavi plazma yüklüyor! Kapı üstü projektörleri yandı! İçeridekiler geldiğimizi baştan beri biliyormuş! Bu bir pusu!\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch2_bond_step",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Gölge Ortaklığı",
                    paragraphs = listOf(
                        "Batı iskelesinin kenarından Natasha'nın kısa bakışı sana değdi — ne acele ne panik. Sadece sırtını yaslayabileceğin sessiz bir güven."
                    )
                )
            ),
            decisionTitle = "Rıhtımdaki Pusu — Operasyon Stratejisi",
            choicePrompt = "Pusu kuruldu ve içerideki gizemli enerji çekirdeği imha edilmek üzere. Bu ilk gerçek görevde ekibe ve gücüne nasıl yön vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch4_c1_shield_team",
                    label = "Zamanı üç saniye yavaşlatıp tüm ekibi kozmik kalkanla koru — pusuyu sıfır kayıpla kırıp Steve ile içeri dal.",
                    toneHint = "Ekip Kalkanı • Kahramanca • Yüksek Güven",
                    nextNodeId = "ch4_s2_team",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.CLINT to 3, BookCharacterId.NATASHA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.LOYALTY, 3),
                            Triple(BookCharacterId.CLINT, CharacterFlagAxis.TRUST, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 4,
                            StoryFlagAxis.FIELD_INITIATIVE to 3,
                            StoryFlagAxis.MENTAL_FATIGUE to 1
                        ),
                        fatigueDelta = 1,
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch4_approach" to "team_shield"),
                        branchTag = "Ekip Kalkanı",
                        impactSummary = "Zamanı büküp plazma ateşini havada dondurdun; ekip tek çizik almadan hangarı temizledi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c1_shadow_natasha",
                    label = "Natasha ile birlikte yeraltı havalandırmasına ışınlan — çatışmayı pas geçip doğrudan gizli kasaya ulaş.",
                    toneHint = "Gölge Sızması • Natasha İle Omuz Omuza",
                    nextNodeId = "ch4_s2_shadow",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.TONY to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.LOYALTY, 3)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHADOW_ALLIANCE to 3,
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 3
                        ),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch4_approach" to "shadow_natasha"),
                        secretUnlocked = "HYDRA Biyometrik Şifre",
                        branchTag = "Gölge İkilisi",
                        impactSummary = "Natasha ile tek mermi sıkmadan yeraltı kasasına indiniz; aranızdaki sessiz uyum kusursuzdu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c1_solo_overwhelm",
                    label = "Ekibe dışarıda kalmasını söyle — içeri tek başına ışınlanıp Beyaz Göz ve enerji dalgasıyla 24 askeri saniyeler içinde etkisiz bırak.",
                    toneHint = "Tek Kişilik Ordu • Mutlak Güç • Yüksek Yorgunluk",
                    nextNodeId = "ch4_s2_solo",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.STEVE to -1, BookCharacterId.WANDA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.CURIOSITY, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.COSMIC_AWAKENING to 3,
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.MENTAL_FATIGUE to 2
                        ),
                        fatigueDelta = 2,
                        avengersTrustDelta = 0,
                        setFlags = mapOf("ch4_approach" to "solo_storm"),
                        branchTag = "Tek Kişilik Fırtına",
                        impactSummary = "Yirmi dört silahlı askeri on saniyede tek başına indirdin; ama burnundan sızan kan ve dizlerindeki titreme bedelini gösterdi.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM IV — DAL 4A/4B/4C → YERALTI KASASI VE SOVYET İMZASI ───────────
        BookSceneNode(
            id = "ch4_s2_team",
            chapterNumber = 4,
            sceneOrder = 2,
            sceneTitle = "Buzlu Camın Ardındaki İz",
            locationTag = "19 NUMARALI RIHTIM • YERALTI KASASI",
            paragraphs = listOf(
                "Hangarın tavan kirişlerindeki iki ağır plazma kulesi aynı anda kör edici, kulak sağır edici bir mavi ışıkla ateş açtığında tek bir milisaniye bile tereddüt etmedin. \"Ekip, pozisyonunuzu koruyun!\" diye bağırdın kulaklığa, iki kolunu yanlara açtın ve çevrendeki zamanı üç tam saniyeliğine ağır bir balın içine gömdün.",
                "Ses kesildi. Rüzgâr dondu. Havadaki yüksek enerjili plazma ışınları Steve Rogers'ın ve Natasha'nın iki metre önünde parlayan cam mızraklar gibi havada asılı kaldı; projektörlerin ışığındaki sis tanecikleri bile kıpırdamıyordu. İki adım öne sıçradın, ekibin tamamını içine alan geniş, yarı şeffaf, geometrik bir kozmik enerji kalkanı açtın ve gelen bütün o yıkıcı plazma akımını iki avucunun içinde emip soğuk, zararsız bir mavi rüzgâra dönüştürdün.",
                "Zaman yeniden aktığında pusunun bütün ölümcül avantajı tek bir saniyede çökmüştü. \"Şimdi!\" diye haykırdı Steve. Vibranyum kalkanı havada yay çizip sol plazma kulesini kıvılcımlar içinde parçaladı; Clint Barton'ın doğu çatısından inen elektroşok okları sağdaki kuleyi ve üç paralı askerin silahını aynı anda susturdu. Natasha Romanoff sisin içinden bir gölge gibi çıkıp yan hattaki dört askeri tek tek yakın dövüşle etkisiz hale getirdi. Beş dakika içinde koca hangar tek bir Avenger bile çizik almadan tamamen sessizleşmişti. Şakaklarında zaman bükmesinin ve enerji emiliminin bıraktığı hafif, keskin bir zonklama vardı — ama Steve'in yanından geçerken yüzündeki o derin güven ifadesini görmek her şeye değmişti.",
                "\"Güzel işti Aiden,\" dedi Steve nefesini toparlayarak. \"Aşağı inen merdiveni buldum. Güvenliği biz tutuyoruz, sen çekirdeği kontrol et.\"",
                "Yeraltı katına inen ağır, paslı çelik kapının elektronik kilidini ellerinin arasındaki mavi enerjiyle sessizce büküp açtığında içeriden soğuk, kuru, eski bir hastane morgunu andıran laboratuvar havası yüzüne vurdu. Merdivenlerin sonundaki geniş beton odanın duvarları rutubetle lekelenmişti; ama odanın tam ortasında, eski Sovyet döneminden kalma, kalın camları kırağı ve buz tutmuş iki metre boyunda silindirik bir rezonans kapsülü duruyordu.",
                "Yanındaki paslanmış çelik masanın üzerinde kenarları yanık, ıslanıp kurumuş kalın bir arşiv klasörü vardı:",
                "[[DOSSIER|PROJE AYNA — DENEK 7-A (GİZLİ KLASÖR)|Tarih: 14 Kasım 2006 · Konum: New York · Rezonans: Sonsuzluk Taşı Düzeyi · Not: Beklenmeyen tam rezonans uyumu. Denek 7-A (Aiden Blackwood) hayatta kaldı. Aile kayıp. Gözetim altına alınacak. İkinci Arşiv Koordinatı: İstanbul, Yerebatan Altı Sarnıç.]]",
                "Kalbin göğsünde tek bir ağır, yavaş darbe attı. 2006. Beş yaşındaydın. Yangın merdiveni. Annenin titreyen eli. Dosyanın ilk sayfasını açtığında kendi çocukluk fotoğrafını gördün — beş yaşında, kameraya korkmadan bakan çarpıcı mavi gözler. Hemen altında babanın ve annenin isimleri, oturdukları apartmanın tam adresi ve yanına kırmızı kalemle düşülmüş tek bir soğuk not: *Beklenmeyen tam rezonans uyumu. Denek hayatta kaldı. Aile kayıp. Gözetim altına alınacak.*",
                "Tam ikinci sayfaya uzandığın anda arkanızdaki donmuş rezonans kapsülünün üzerindeki mavi ışık aniden canlandı — ve senin kalp atışınla birebir aynı ritimde yanıp sönmeye başladı. Kapsülün biyometrik kilidi elinin sıcaklığını ya da parmak izini değil, doğrudan kanındaki o Sonsuzluk Taşı düzeyindeki kozmik imzayı tanımıştı. Kalın metalin içinden keskin bir *tık* sesi geldi, gizli koruma bölmesi açıldı ve soğuk beyaz buharın içinden iki şey çıktı: titanyum kaplı şifreli bir veri diski ve metal plakaya lazerle kazınmış koordinatlar — **İSTANBUL: SARNIÇ ARŞİVİ.**",
                "O gece bir kaza değildi. Gökyüzü kendiliğinden yarılmamıştı. Birileri o kapıyı bilerek açmıştı — ve sen yirmi yıldır onların yarattığı enkazın içinden kaçıyordun. Tam veri diskini avucunda sıkarken yukarıdaki demir merdivenlerden Steve Rogers'ın ve Natasha'nın hızlı ayak sesleri yaklaştı. \"Aiden!\" diye seslendi Steve aşağıya doğru. \"Yukarısı tamamen temiz. Aşağıda ne buldun, her şey yolunda mı?\""
            ),
            decisionTitle = "Sırrın Kaderi — Veriyi Paylaşacak Mısın?",
            choicePrompt = "Beş yaşındayken aileni öldüren olay bir kaza değilmiş — bir proje. Bu veriyi ekiple hemen paylaşacak mısın, yoksa gizli mi tutacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch4_c2_share_all",
                    label = "Dosyayı ve veri diskini doğrudan Steve ve Natasha'nın önüne koy — \"Bu benim geçmişim. Birlikte çözelim.\"",
                    toneHint = "Tam Güven • Ekip İttifakı",
                    nextNodeId = "ch4_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 3, BookCharacterId.BRUCE to 3, BookCharacterId.FURY to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.FURY, CharacterFlagAxis.TRUST, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 4,
                            StoryFlagAxis.TOWER_BELONGING to 3
                        ),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch4_disk" to "shared"),
                        secretUnlocked = "Proje Ayna: Denek 7-A",
                        branchTag = "Açık Dosya",
                        impactSummary = "Yirmi yıllık sırrını ilk kez bir ekiple paylaştın; Bruce ve Tony diski çözmek için hemen çalışmaya başladı.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c2_hide_disk",
                    label = "Veri diskini cebine sakla — ekibe sadece boş kapsülü göster, gerçeği önce kendin araştır.",
                    toneHint = "Gizli Gündem • Kimseye Güvenme",
                    nextNodeId = "ch4_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -1, BookCharacterId.TONY to -1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.CURIOSITY, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.MYSTERY_AURA to 3
                        ),
                        mysteryDelta = 2,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("ch4_disk" to "hidden"),
                        secretUnlocked = "Gizli İstanbul Koordinatları",
                        branchTag = "Saklı Disk",
                        impactSummary = "Diski kimseye göstermeden cebine indirdin. Steve inanır gibi oldu — ama Natasha gözlerinden bir şey sakladığını hemen anladı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch4_s2_shadow",
            chapterNumber = 4,
            sceneOrder = 2,
            sceneTitle = "Biyometrik Kasa ve Gölgedeki Gerçek",
            locationTag = "19 NUMARALI RIHTIM • YERALTI KASASI",
            paragraphs = listOf(
                "\"Steve, Clint — yukarıyı otuz saniye oyalayın,\" dedin kulaklıktan sakin ama net bir sesle. Sonra batı iskelesindeki gölgenin içinde bekleyen Natasha Romanoff'a döndün ve elini uzatmadan önce kısa bir baş işareti verdin. Natasha tek bir saniye bile tereddüt etmedi; sırtını sana yaslayıp sol elini omzuna koydu. Bir nefesin içinde havayı büktün ve ikiniz birlikte 4 numaralı hangarın altındaki dar, paslı havalandırma boşluğuna sessizce ışınlandınız.",
                "Kısa bir sıçramaydı — ama iki bedeni birden mekândan çekip geçirmek göğsünde soğuk, tanıdık bir çekilme bıraktı. İndiğinizde Natasha sendelemedi bile; yere tek dizinin üstünde, sıfır sesle kondu. Gözleri karanlığa bir saniyede alışırken senin nefesini kontrol etti: \"İyi misin?\" Başını hafifçe salladın. \"İki kişilik sıçrama biraz daha ağır geliyor. Hepsi bu.\"",
                "Havalandırma ızgarasını sessizce indirip yeraltı koridoruna süzüldüğünüzde, köşede nöbet tutan iki ağır silahlı paralı asker daha başlarını çeviremeden sahne bitti. Natasha sağdakini tek, akıcı bir boyun kilidiyle yere indirdi; sen ise soldakinin tüfeğini küçük bir mavi enerji dalgasıyla kilitleyip ensesine tek bir darbeyle onu sessizce bayılttın. Tek bir mermi sıkılmamıştı. Tek bir alarm bile çalmamıştı. Sanki yıllardır birlikte operasyon yürütüyormuşsunuz gibi, konuşulmasına bile gerek olmayan ölümcül, sakin bir uyum vardı aranızda.",
                "Yeraltı kasasının paslı çelik kapısını açtığınızda içeriden soğuk, kuru bir laboratuvar havası yüzünüze vurdu. Odanın tam ortasındaki eski Sovyet rezonans kapsülü senin yaklaşmanla birlikte derin, soğuk bir mavi ışıkla canlandı. Kalın camın üzerindeki kırağı ve buz tabakası ince çatlaklarla çözüldü. Biyometrik kilit elinin ısısını değil, doğrudan kanındaki Sonsuzluk Taşı düzeyindeki kozmik imzayı tanımıştı. Gizli koruma bölmesi *tık* diye açıldı ve soğuk buharın içinden üç şey çıktı: kenarları yanık bir klasör — **PROJE AYNA — DENEK 7-A / NEW YORK 2006** — üzerinde annenle babanın adının geçtiği şifreli titanyum bir veri diski ve metal plakaya kazınmış koordinatlar: **İSTANBUL, SARNIÇ ARŞİVİ.**",
                "Klasörün ilk sayfasındaki beş yaşındaki fotoğrafına baktığında boğazında sert bir düğüm oluştu. *Denek hayatta kaldı. Aile kayıp.* O gece gökyüzü kazara yarılmamıştı.",
                "Natasha hemen yanındaydı. Dosyanın köşesindeki solmuş Kiril mührünü tek bakışta tanıdı; gözlerindeki o sakin, ölçülü ifade bir saniyeliğine buz gibi sertleşti. Parmağıyla mührün altındaki küçük yıldızı işaret etti. \"Bu eski Kırmızı Oda ve HYDRA'nın 2000'lerin başındaki gizli ortak araştırma damgası, Aiden,\" dedi alçak, çelik gibi net bir sesle. \"Kırmızı Oda'dayken bu mührün geçtiği tek bir fısıltı bile insanı öldürmeye yeterdi. Beş yaşındayken ailenin öldüğü o gece... bu gökyüzünden inen bir kaza değildi. Birileri o enerjiyi bilerek tetiklemiş.\"",
                "Yukarıdan Steve Rogers ve Clint Barton'ın çatışmayı bitirdiğini haber veren ağır ayak sesleri demir merdivenlerden aşağı doğru yaklaşıyordu. Natasha doğrudan senin çarpıcı mavi gözlerinin içine baktı — gözlerinde ne acıma vardı, ne yargılama, ne de acele. Yalnızca sırtını yaslayabileceğin dürüst, karanlıktan anlayan bir ortaklık. \"Yukarıdakilere ne kadarını söyleyeceksin?\" diye sordu alçak sesle. \"Bu senin geçmişin, senin kanın. Karar tamamen senin — nasıl istersen öyle oynarım.\""
            ),
            decisionTitle = "Sırrın Kaderi — Veriyi Paylaşacak Mısın?",
            choicePrompt = "Natasha kararını sana bırakıyor. Bu gerçeği tüm ekibe açacak mısın, yoksa şimdilik sadece aranızda mı kalacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch4_c2_share_all",
                    label = "Dosyayı tüm ekiple paylaş — \"Artık karanlıkta tek başıma yürümeyeceğim.\"",
                    toneHint = "Tam Güven • Ekip İttifakı",
                    nextNodeId = "ch4_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.NATASHA to 3, BookCharacterId.BRUCE to 3, BookCharacterId.FURY to 3),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.FURY, CharacterFlagAxis.TRUST, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 4,
                            StoryFlagAxis.TOWER_BELONGING to 3
                        ),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch4_disk" to "shared"),
                        secretUnlocked = "Proje Ayna: Denek 7-A",
                        branchTag = "Açık Dosya",
                        impactSummary = "Diski tüm ekibin önüne koydun; Natasha başını onaylayarak eğdi.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c2_hide_disk",
                    label = "\"Şimdilik sadece ikimizde kalsın\" — diski gizle, üst kattakilere sadece boş kasayı rapor et.",
                    toneHint = "Gölge İttifakı • Sadece Natasha",
                    nextNodeId = "ch4_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.STEVE to -1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.SHADOW_ALLIANCE to 4,
                            StoryFlagAxis.MYSTERY_AURA to 2
                        ),
                        mysteryDelta = 2,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("ch4_disk" to "natasha_only"),
                        secretUnlocked = "Gizli İstanbul Koordinatları",
                        branchTag = "Sadece İkimiz",
                        impactSummary = "Diski sadece Natasha ile paylaştın; ikiniz arasında tüm ekibin dışında, kırılmaz bir gölge anlaşması kuruldu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch4_s2_solo",
            chapterNumber = 4,
            sceneOrder = 2,
            sceneTitle = "Sessizliğin Bedeli",
            locationTag = "19 NUMARALI RIHTIM • YERALTI KASASI",
            paragraphs = listOf(
                "\"Dışarıda kalın,\" dedin kulaklığa. Sesin ne bir rica ne de tartışmaya açık bir öneriydi. Steve Rogers'ın \"Aiden, bekle, protokol—\" diye itiraz etmesine fırsat vermeden tek bir keskin mavi kuantum sıçramasıyla 4 numaralı hangarın içine ışınlandın; ağır çelik kapı basınç dalgasıyla menteşelerinden kopup savruldu. Yirmi dört namlu aynı saniyede sana çevrildi, tavan kirişlerindeki iki ağır plazma kulesi kör edici mavi ışıkla ateşini başlattı.",
                "On saniye. Bütün savaş yalnızca on saniye sürdü. Göz bebeklerindeki o çarpıcı mavi yerini saf, soğuk bir **beyaza** bıraktığında hangardaki bütün silah sesleri bıçak gibi kesildi. Beyaz Göz'ün yarattığı tek bir yoğun, sarsıcı zihin dalgasıyla yirmi dört profesyonel paralı asker aynı anda tüfeklerini beton zemine bırakıp dizlerinin üzerine çöktü; tavan kulelerinden inen yüksek enerjili plazma ışınlarını ise iki avucunun içinde emip kendi etrafında dönen parıltılı bir enerji halkasına çevirdin ve tek bir bilek hareketiyle kuleleri kıvılcımlar içinde eritip susturdun. Hangar bir anda mezar sessizliğine büründü.",
                "Kulaklıkta Tony Stark'ın şaşkın ıslığı duyuldu: \"Tamam, resmen tek kişilik bir orduyu işe almışız.\" Steve'in sesi ise daha ciddi, daha temkinliydi: \"İyi misin Aiden? İçeri geliyoruz.\"",
                "\"Yukarısı temiz, kimse ölmedi,\" dedin, sesin hafifçe çatallı çıkmıştı. Yeraltı kasasına inen dar demir merdivenlerden aşağı indiğinde şakaklarında keskin, nabız gibi atan ağır bir zonklama başladı. Burnundan ince bir çizgi halinde sıcak kan sızdı; elinin tersiyle sildin. Hızlı iyileşmen hemen devreye girip damarlarını onarsa da, aynı on saniye içinde hem ardışık ışınlanma, hem Beyaz Göz zihin felci, hem de plazma emilimi kullanmanın bedeli dizlerinde hafif, sinir bozucu bir titreme bırakmıştı. İçinde olağanüstü bir kozmik güç taşıyordun, ama bedenin hâlâ insan etinden ve sinirlerinden yapılmaydı — gücünün bir sınırı ve ağır bir bedeli vardı.",
                "Yeraltı kasasının paslı kapısını açtığında odanın tam ortasındaki buz tutmuş eski Sovyet rezonans kapsülü senin yaklaşmanla birlikte derin bir mavi ışıkla canlandı. Kalın camdaki kırağı çatladı. Biyometrik kilit kanındaki kozmik imzayı tanıdı ve gizli koruma bölmesi *tık* diye açıldı. İçinden çıkan **PROJE AYNA — DENEK 7-A / NEW YORK 2006** yazılı yanık klasör, annenle babanın adının geçtiği şifreli titanyum veri diski ve metal plakaya kazınmış **İstanbul — Sarnıç Arşivi** koordinatları ellerinde buz gibi duruyordu.",
                "İlk sayfadaki beş yaşındaki fotoğrafına baktın. O gece gökyüzü kazara yarılmamıştı. Tam veri diskini avucunun içine alıp dosyayı kapatırken yukarıdaki demir merdivenlerden Steve Rogers ve Natasha Romanoff'un hızlı ayak sesleri yaklaştı."
            ),
            decisionTitle = "Sırrın Kaderi — Veriyi Paylaşacak Mısın?",
            choicePrompt = "Ekip yukarıdan iniyor. Elindeki gerçeği onlara açacak mısın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch4_c2_share_all",
                    label = "Dosyayı Steve ve Natasha'nın önüne koy — \"Bu benim geçmişim. Birlikte çözelim.\"",
                    toneHint = "Tam Güven • Ekip İttifakı",
                    nextNodeId = "ch4_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.NATASHA to 3, BookCharacterId.BRUCE to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.AVENGERS_TRUST to 3, StoryFlagAxis.TOWER_BELONGING to 2),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch4_disk" to "shared"),
                        secretUnlocked = "Proje Ayna: Denek 7-A",
                        branchTag = "Açık Dosya",
                        impactSummary = "Tek başına girdiğin kasadan çıkan sırrı ekiple paylaştın.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch4_c2_hide_disk",
                    label = "Veri diskini cebine sakla — kimseye güvenmeden önce gerçeği kendin çöz.",
                    toneHint = "Gizli Gündem • Yalnız Yol",
                    nextNodeId = "ch4_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to -1, BookCharacterId.TONY to -1),
                        storyAxisDeltas = mapOf(StoryFlagAxis.INDEPENDENCE to 3, StoryFlagAxis.MYSTERY_AURA to 3),
                        mysteryDelta = 2,
                        avengersTrustDelta = -1,
                        setFlags = mapOf("ch4_disk" to "hidden"),
                        secretUnlocked = "Gizli İstanbul Koordinatları",
                        branchTag = "Saklı Disk",
                        impactSummary = "Diski saklayıp ekibe sadece boş kasayı gösterdin.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM IV — FİNAL ────────────────────────────────────────────────────
        BookSceneNode(
            id = "ch4_s3_end",
            chapterNumber = 4,
            sceneOrder = 3,
            sceneTitle = "Buzun Altındaki İsim",
            locationTag = "MANHATTAN LİMANI • ŞAFAK ÖNCESİ",
            paragraphs = listOf(
                "4 numaralı hangardan çıktığınızda Hudson Nehri'nin üzerinden esen sert rüzgâr artık yağmur değil, yüzü kesen ince ve soğuk bir kar getiriyordu. Uzakta Manhattan'ın ışıkları beyaz sisin içinde soluk sarı lekelere dönüşmüştü; kıyıya bağlı eski yük gemilerinin halatları gıcırdıyordu. Etkisiz hale getirilen yirmi dört paralı asker Fury'nin güvenilir temizlik ekibine devredilirken, sen rıhtımın ucunda durup son kez yeraltı kasasından çıkardığın o buz gibi gerçeği düşündün.",
                "Beş yaşındayken New York gökyüzünde açılan o mavi yarık bir doğa olayı değildi. Birinin laboratuvarında yazılmış, adlandırılmış ve numaralandırılmış bir projeydi: **Proje Ayna.** Ve sen o projenin hayatta kalan 7-A numaralı deneğiydin. Yirmi yıldır peşinden gelen S.H.I.E.L.D. timleri, karanlık hükümet birimleri ve kozmik tarikatlar sadece gücünden korktukları için değil — o gecenin yaşayan tek kanıtı olduğun için seni susturmaya çalışıyordu.",
                "Ama asıl kanını donduran ayrıntı, sen tam yeraltı kasasının paslı kapısından çıkmak üzereyken, rezonans kapsülünün yanındaki çatlamış eski monitörde son bir kez parlayıp sönen yeşil harfli satırdı. Yirmi yıldır kendine bu evrende tek bir anomali, tek bir kırık deney, hiçbir yere ait olmayan tek bir yabancı olduğunu söylemiştin.",
                "Oysa o kırık ekranda kendi denek numaranın hemen üstünde başka bir isim parlıyordu: **DENEK 6-B: ELIAS VOSS — DURUM: KAÇAK / AKTİF / SON SİNYAL: BROOKLYN.**",
                "Bir saat sonra Avengers Kulesi'nin seksen beşinci katındaki brifing masasına döndüğünüzde şafak hâlâ sökmemişti. Dışarıda kar fırtınası şiddetlenmiş, cam duvarların kenarını beyaz bir buz tabakası kaplamıştı. Bruce Banner laboratuvardan hızla gelip elindeki tableti cam masaya koydu; yüzü solgundu. \"Aiden, 19 numaralı rıhtımdaki kapsülün çevresinden aldığımız enerji izleri 2006'daki o mavi yarığın birebir aynısı,\" dedi. \"Ama çok daha garip bir şey var: o kapsül dün geceye kadar uyku modundaydı. Onu dışarıdan birisi, şu an bizimle aynı şehirde olan birisi uzaktan uyandırmış.\"",
                "\"Tuzak mıydı?\" diye sordu Steve Rogers kaşlarını çatarak. Tony Stark hologramı çevirdi, kırmızı bir dalga grafiğini büyüttü: \"Tuzak değil Yüzbaşı — bir çağrı işareti. Biri rıhtımdaki eski kapsülü özellikle Aiden'ın frekansını yakalayıp onu buraya çekmek için çalıştırmış.\"",
                "\"Elias Voss,\" diye tekrarladı Natasha Romanoff alçak, buz gibi bir sesle; eski Kırmızı Oda ve S.H.I.E.L.D. arşivlerini tarayan parmakları tablette durdu. \"Bütün resmi kayıtlarda on dokuz yıl önce ölü olarak görünüyor. Demek o da kaçmayı başarmış.\"",
                "Tam cümlesini bitirdiği saniyede FRIDAY'in keskin uyarısı salonu böldü: \"Efendim, Brooklyn eski demiryolu yük terminalinde yüksek yoğunluklu, kararsız bir kozmik rezonans sinyali tespit edildi. İmza Bay Blackwood'un spektrumuyla yüzde doksan iki oranında eşleşiyor.\" Yalnız değildin. Seninle aynı kozmik ateşten geçmiş, aynı karanlıktan çıkmış biri daha vardı — ve şu an kar fırtınasının tam ortasında doğrudan seni çağırıyordu."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch4_disk",
                    requiredFlagValue = "shared",
                    callbackLabel = "Açık Dosya — Ekip Desteği",
                    paragraphs = listOf(
                        "Quinjet kar fırtınasının içinden Kule'ye dönerken Steve Rogers ve Bruce Banner veri diskini çoktan çok katmanlı şifre çözücüye bağlamıştı. Tony Stark ekrandaki ilk şifrelenmiş satırları görünce şakayı tamamen bıraktı: \"Bu kodlama seviyesi eski Konsey kara bütçesine ait.\" Yirmi yıldır tek başına taşıdığın o ağır yükü ilk kez başkasının masasına koymak, göğsünde alışılmadık ama gerçek bir hafiflik bırakmıştı."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch4_disk",
                    requiredFlagValue = "natasha_only",
                    callbackLabel = "Sadece İkimiz — Gölge Anlaşması",
                    paragraphs = listOf(
                        "Quinjet'in arka koltuğunda titanyum veri diski siyah ceketinin iç cebinde buz gibi duruyordu. Karşında oturan Natasha Romanoff eldivenlerini çıkarırken gözlerini kısa bir an doğrudan senin mavi gözlerine çevirdi; ne bir soru sordu, ne endişe etti, ne de seni sorguladı. Aranızdaki o sessiz gölge anlaşması, koca Avengers Kulesi'ndeki bütün güvenlik protokollerinden daha sağlamdı."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch4_disk",
                    requiredFlagValue = "hidden",
                    callbackLabel = "Saklı Disk — Yalnız Sır",
                    paragraphs = listOf(
                        "Titanyum veri diski ceketinin iç cebinde ağır, soğuk bir taş gibi duruyordu. Steve kasada sadece eski, silinmiş HYDRA kayıtları olduğunu söylediğinde seni sorgulamadı — ama Natasha Romanoff'un tek bir saniyelik yan bakışından, senin bir şeyi sakladığını anında okuduğunu biliyordun. Yine de hiçbir şey söylemedi; çünkü sınırını kendin açmadığın sürece zorlamayacağını çoktan anlamıştın."
                    )
                )
            ),
            isChapterEnd = true,
            chapterEndQuote = "Geçmişin buzun altında donup kalmaz; sadece çözüleceği geceyi bekler."
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM V: KIRMIZI BUZ (2 Büyük Dönüm Noktası)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch5_s1",
            chapterNumber = 5,
            sceneOrder = 1,
            sceneTitle = "Kar Altında Elias Voss",
            locationTag = "BROOKLYN ESKİ TERMİNALİ • KAR FIRTINASI • 23:10",
            paragraphs = listOf(
                "Ertesi gece New York'u vuran sert kar fırtınası şehrin bütün gürültüsünü beyaz bir yorganın altına gömmüştü. Köprülerin halatları buz tutmuş, sokaklar saat on olmadan tamamen boşalmıştı. Ama Avengers Kulesi'nin seksen beşinci katındaki büyük cam ekranda, Brooklyn'in terk edilmiş eski demiryolu yük terminalinin tam ortasında yanıp sönen koyu kırmızı bir enerji halkası vardı. Bruce Banner spektrumu iki kez kontrol edip başını sana çevirmişti: \"Aiden, bu senin frekansının neredeyse aynısı — ama çok daha dengesiz, çatlak ve yakıcı. Sanki aynı güç kontrolsüz bir çatlak borudan sızıyor gibi.\"",
                "\"Elias Voss,\" demiştin. 19 numaralı rıhtımdaki kırık monitörde yanıp sönen o isim: *Denek 6-B.* Yirmi yıl boyunca bu dünyada tek olduğunu sanmıştın; başka gezegenlere sıçrarken bile kendin gibi birini hiç görmemiştin. Şimdi aynı ateşten geçmiş biri birkaç kilometre ötede, kar fırtınasının içinde seni bekliyordu.",
                "Ekip terminalin üç yüz metre gerisinde, buz tutmuş eski üst geçitlerde sessizce mevzilenmişti. \"Önden tek başıma yürüyeceğim,\" demiştin Steve Rogers'a. \"Eğer benim yaşadıklarımın yarısını bile yaşadıysa, kalabalık bir timi gördüğü saniye ya kaçar ya da panikle bütün terminali havaya uçurur.\" Steve bir an tereddüt etmiş, sonra Natasha'nın sessiz onayıyla kabul etmişti — ama kulaklık hattı sürekli açıktı.",
                "Tek başına diz boyu karla kaplı paslı rayların arasında yürüdün. Grafitili eski yük vagonları, kırık camlı depo çatıları ve rüzgârda inleyen çelik direkler tipinin içinde zor seçiliyordu. Çizmenin altında ezilen buzun sesi rüzgârda kayboluyordu. Havada yine o tanıdık soğuk bakır tadı vardı — ama bu kez yanında yanmış ozon, kırık elektrik telleri ve acı bir metal kokusu gibi rahatsız edici bir ağırlık taşıyordu. İki dev yük vagonunun arasındaki karanlık gölgeden uzun boylu, soluk yüzlü, üzerinde omuzları kar tutmuş yıpranmış siyah bir palto olan bir adam yavaşça çıktı.",
                "Göz göze geldiğinizde kar fırtınasının bütün gürültüsü bir anlığına uzaklaştı. Adam otuzlu yaşlarının başında görünüyordu ama gözlerinin altındaki derin mor halkalar ve çatlamış dudakları çok daha ağır, yıllardır dinmemiş bir yorgunluk taşıyordu. Ve asıl sarsıcı olan gözleriydi: senin o çarpıcı, berrak mavi ışığının karanlık, titrek, dengesiz bir ikizi olan soluk bir kırmızı parıltı yanıp sönüyordu. **Elias Voss — Denek 6-B.**",
                "\"Demek 7-A sonunda cam kafesten çıktı,\" dedi Voss. Sesi rüzgârda çatallı ama şaşırtıcı derecede sakindi; sanki yıllardır bu konuşmayı kafasında tekrar tekrar kurmuş gibi. Saldırmadı. İki elini de paltosunun dışına çıkarıp boş, soğuktan kızarmış avuçlarını gösterdi. \"Gözlerine bak... seninki hâlâ saf, berrak mavi. Demek senin mutasyonun bedeninle barışmış, dengede kalmış. Şanslısın Aiden. Bende o kadar zarif olmadı.\"",
                "İki adım yaklaştın, parmak uçlarında koruyucu mavi kozmik enerji sessizce hazırlandı ama dışarı tek bir kıvılcım bile sızmadı. \"Beni nereden tanıyorsun? Ve 19 numaralı rıhtımdaki Sovyet kapsülünün seninle ne ilgisi var?\"",
                "Voss acı, kısa bir kahkaha attı; nefesi dondurucu havada beyaz bir buluta dönüştü. \"Aynı mezardan çıktık Aiden. Aynı doktorların, aynı kurşun camların, aynı ölçüm cihazlarının altından. Beni dinle, vaktimiz çok az. Senin peşinde sadece S.H.I.E.L.D. ya da sıradan hükümetler yok. 2006'da New York gökyüzünü yaranlar, bizi bu hale getirenler hâlâ hayatta ve Dünya Güvenlik Konseyi'nin en derin gölgesinde saklanıyorlar. İstanbul'daki sarnıç arşivini açtıklarında seni kurtarmaya değil, yarım kalan projeyi tamamlamaya — ya da bütün kanıtları bizimle birlikte gömmeye — gelecekler. *Kaç.* Avengers Kulesi seni koruyamaz. O kostümlü kahramanların hiçbiri bizi yaratanların ne kadar soğukkanlı olduğunu bilmiyor.\"",
                "\"Yirmi yıldır zaten kaçıyorum,\" dedin buz gibi bir netlikle. \"Başka kıtalara, başka gezegenlere bile sıçradım. Kaçmak hiçbir şeyi çözmedi. Ve eğer gerçekten kaçmamı istiyorsan neden bu sinyali açıp kendini açık hedef yaptın?\"",
                "\"Çünkü başka türlü seni o cam kuleden çıkaramazdım!\" diye karşılık verdi Voss, gözlerindeki soluk kırmızı ışık bir an parlayıp ayaklarının altındaki diz boyu karı cızırdayarak sıcak buhara çevirdi. Tam o anda kulağındaki iletişim hattında Steve Rogers'ın keskin, acil komutu çınladı: \"Aiden, enerji dalgalanması kritik seviyeye çıktı! Etrafı tamamen sardık — işaretimle birlikte Voss'u etkisiz hale getirip Kule'ye getiriyoruz!\"",
                "Başını hafifçe yukarı kaldırdığında tipi perdesinin içinden depo çatısındaki iki tanıdık silüeti seçtin: parmak uçlarında kızıl kıvılcımlar hazır bekleyen Wanda Maximoff ve keskin nişan hattında sıfır hareketle duran Natasha Romanoff. Voss yukarıdaki gölgeleri aynı anda fark etti ama kaçmaya ya da saldırmaya yeltenmedi. Yalnızca gözlerini kısıp acı bir alayla doğrudan sana baktı: \"Bak, yeni sahiplerin de gelmiş. Seçimini yap, 7-A. Beni onlara teslim edip ellerime kelepçe taktıracak mısın, yoksa gerçeği bizzat benden mi dinleyeceksin?\""
            ),
            decisionTitle = "Elias Voss İle İlk Karşılaşma",
            choicePrompt = "Elias Voss seni uyarmaya mı geldi, yoksa bir tuzak mı? Ekip baskına hazır bekliyor.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch5_c1_capture",
                    label = "Voss'u enerji kalkanıyla kilitle ve ekiple birlikte Kule'nin sorgu odasına götür — gerçeği kontrollü ortamda öğren.",
                    toneHint = "Kontrollü • Ekip Protokolü • Sorgu Odası",
                    nextNodeId = "ch5_s2_interrogation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.TONY to 2, BookCharacterId.NATASHA to 2, BookCharacterId.VOSS to -1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 2),
                            Triple(BookCharacterId.VOSS, CharacterFlagAxis.SUSPICION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.AVENGERS_TRUST to 3,
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 2
                        ),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch5_voss_meet" to "captured"),
                        branchTag = "Kule'de Sorgu",
                        impactSummary = "Voss'u zarar vermeden etkisiz hale getirip Kule'nin cam sorgu odasına getirdiniz.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c1_talk_alone",
                    label = "Ekibe beş dakika beklemesini söyle — Voss ile kar fırtınasının ortasında birebir konuş ve zihnini Wanda'nın yardımı olmadan kendin oku.",
                    toneHint = "Birebir Yüzleşme • Yüksek Risk • Derin Sır",
                    nextNodeId = "ch5_s2_snow_truth",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 4, BookCharacterId.WANDA to 2, BookCharacterId.NATASHA to 1, BookCharacterId.STEVE to -1),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.VOSS, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.VOSS, CharacterFlagAxis.EMPATHY, 2),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.EMPATHY, 2),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.SUSPICION, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.INDEPENDENCE to 3,
                            StoryFlagAxis.DARK_TEMPTATION to 2,
                            StoryFlagAxis.MYSTERY_AURA to 2
                        ),
                        mysteryDelta = 2,
                        setFlags = mapOf("ch5_voss_meet" to "private_talk"),
                        secretUnlocked = "Dr. Evelyn Hale'in Adı",
                        branchTag = "Karda İtiraf",
                        impactSummary = "Voss'un zihnindeki acıyı bizzat gördün; arkasındaki baş bilim insanı Dr. Evelyn Hale'in adını ilk orada duydun ve onu ikna edip Kule'ye getirdin.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM V — DAL 5A & 5B → SORGU ODASI & KADER KARARI ──────────────────
        BookSceneNode(
            id = "ch5_s2_interrogation",
            chapterNumber = 5,
            sceneOrder = 2,
            sceneTitle = "Camın Ardındaki Ayna",
            locationTag = "AVENGERS KULESİ • YERALTI SORGU ODASI",
            paragraphs = listOf(
                "Brooklyn'deki kar fırtınasında Voss'un etrafını tek bir temiz, kusursuz mavi enerji kalkanıyla sardın. Bir saniyeliğine direnmeye çalıştı — etrafında dengesiz kırmızı kıvılcımlar patlayıp diz boyu karı sıcak buhara çevirdi — ama senin kontrolün onunkinden katbekat derindi. Tek bir çizik bile atmadan, onu incitmeden enerjisini sönümledin ve Steve Rogers'ın önderliğindeki ekiple birlikte Avengers Kulesi'nin güçlendirilmiş yeraltı sorgu odasına getirdiniz.",
                "Şimdi o soğuk, ses yalıtımlı, çelik duvarlı odada sadece ikiniz vardınız. Kalın, tek yönlü gözlem camının arkasında Tony Stark, Steve Rogers, Bruce Banner, Natasha Romanoff ve Wanda Maximoff her nefesi ve her kelimeyi dinliyordu. Elias Voss, bileklerinde Bruce'un acilen hazırladığı enerji sönümleyici titanyum kelepçelerle metal masanın karşısında oturuyordu. Gözlerindeki o soluk kırmızı ışık tepedeki sert floresan lambanın altında zayıfça titriyordu.",
                "Bir dakika boyunca kimse konuşmadı. Sadece havalandırmanın alçak uğultusu ve Voss'un düzenli ama yorgun nefesi vardı. Sonra masaya doğru hafifçe eğildi, kelepçelerin zinciri soğuk metale değdi.",
                "\"Bana iyi bak, Aiden,\" diye fısıldadı Voss. \"Bana bakarken bir düşman değil, eğer beş yaşında kaçmayı başaramasaydın dönüşeceğin karanlık aynayı görüyorsun. 2006 yılında New York gökyüzünde açılan o mavi ışık kazara inmedi. **Dr. Evelyn Hale** ve Dünya Güvenlik Konseyi'nin resmi kayıtlarda bile geçmeyen gölge birimi, Sonsuzluk Taşları'nın evrensel enerji frekansını yapay olarak kopyalayıp gezegen savunması adı altında mutlak bir silaha dönüştürmeye çalışıyordu. Projenin adı buydu: *Proje Ayna.*",
                "Ellerin masanın buz gibi kenarında istemsizce kasıldı; parmak boğumların beyazlaştı. Yirmi yıldır bir doğa anomalisi ya da kör bir kaza sandığın gece, bir bilim insanının ve karanlık bir konseyin masasında imzalanmış bir deneyin yan etkisiydi.",
                "\"Annenle baban...\" Voss bir saniye duraksadı, sesi garip bir biçimde yumuşadı. \"Onlar sadece yanlış binada değildi Aiden. Deney apartmanın iki sokak ötesindeki eski yeraltı istasyonunda kontrolden çıktı, ama senin çocuk bedenin o saf kozmik frekansı parçalanmadan emebilen tek insan yapısı oldu. Ben **Denek 6-B**'ydim. Senden bir yıl önce, başka bir şehirde denediler. Hayatta kaldım ama vücudum frekansı hiçbir zaman tam dengeleyemedi — her kullandığımda sinirlerimi içeriden yakıyor, gözlerimi kırmızıya çeviriyor.\"",
                "\"Peki İstanbul?\" diye sordun, sesin soğuk ve keskindi. \"Rıhtımdaki veride İstanbul'daki bir sarnıç arşivinin koordinatları vardı. Orada ne var?\"",
                "Voss derin, kesik bir nefes aldı. \"Üçüncü bir kişi daha var Aiden. **Mira — Denek 8-C.** Senden iki yıl sonra, Doğu Avrupa sınırında aynı deneyin son kalıntısına maruz kaldı. Ama senin gibi kaçıp kendi kontrolünü öğrenmesine hiç fırsat verilmedi. Yıllarca Dr. Hale'in tesislerinde, cam duvarların arkasında 'canavar' diye etiketlenip eğitildi. Sonunda kaçtı — ama bütün öfkesini, kaybını ve acısını bir kıyamet silahına dönüştürdü. Şimdi İstanbul Boğazı'nın altındaki eski Bizans sarnıçlarında Proje Ayna'nın ana Rezonans Kulesi'ni yeniden çalıştırmaya hazırlanıyor.\"",
                "[[DOSSIER|PROJE AYNA — DENEK ÖZETİ (KİM KİMDİR?)|• Denek 6-B (Elias Voss): 2005'teki ilk prototip deneyden sağ çıktı; gücü kırmızı ve dengesiz, vücudunu yakıyor.\n• Denek 7-A (Aiden Blackwood): 14 Kasım 2006'da mavi kozmik çekirdekle tam uyum sağlayan tek kişi.\n• Denek 8-C (Mira): 2008'de yakalanıp kafeste büyütüldü; şimdi İstanbul'daki ana Rezonans Kulesi'ni ateşleyip intikam almak istiyor.]]",
                "Odadaki ses tamamen kesildi. Gözlem camının arkasından Tony Stark'ın alışıldık esprilerinden tek bir kırıntı bile gelmiyordu. Voss başını kaldırdı, yüzünde kırık, acı bir gülümseme belirdi. \"İkimiz de aynı ateşte dövülmüş iki kırık kılıcız Aiden. S.H.I.E.L.D. nakil timi yarın sabah şafakta buraya gelip beni okyanusun ortasındaki Raft hapishanesine atacak — güneş görmeyen, zihin uyuşturan, ses geçirmeyen o derin kafese. Seni de dokuz yaşındayken öyle bir kafese koymaya çalışmışlardı, değil mi? Şimdi karar tamamen senin: beni sisteme teslim mi edeceksin, yoksa İstanbul'a giderken beni Kule'de, kendi sorumluluğunda mı tutacaksın?\""
            ),
            decisionTitle = "Elias Voss'un Kaderi",
            choicePrompt = "S.H.I.E.L.D. transfer ekibi yolda. Voss bir düşman mı, yoksa senin gibi bir kurban mı?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch5_c2_protect_voss",
                    label = "S.H.I.E.L.D. transferini engelle — Voss'un Kule'de senin sorumluluğunda kalması için Steve ve Tony'nin karşısına çık.",
                    toneHint = "Merhamet • Sorumluluk • Müttefik Kazan",
                    nextNodeId = "ch5_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 6, BookCharacterId.WANDA to 4, BookCharacterId.NATASHA to 2, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.VOSS, CharacterFlagAxis.LOYALTY, 4),
                            Triple(BookCharacterId.VOSS, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 2)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.HUMANITY_ANCHOR to 4,
                            StoryFlagAxis.SHIELD_SUSPICION to 2
                        ),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch5_voss_fate" to "protected"),
                        branchTag = "Voss Kule'de Kaldı",
                        impactSummary = "Voss'u Raft kafesinden kurtardın; Wanda sana derin bir sıcaklıkla baktı. Artık İstanbul'a giderken içeriden bir rehberin var.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c2_handover_voss",
                    label = "Transferi engelleme — Voss'u S.H.I.E.L.D.'a teslim et ama Dr. Hale ve İstanbul dosyasını kendin devral.",
                    toneHint = "Soğukkanlı • Pragmatik • Mesafeli",
                    nextNodeId = "ch5_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 2, BookCharacterId.STEVE to 1, BookCharacterId.FURY to 2, BookCharacterId.WANDA to -1, BookCharacterId.VOSS to -2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.TONY, CharacterFlagAxis.RESPECT, 2),
                            Triple(BookCharacterId.FURY, CharacterFlagAxis.TRUST, 2),
                            Triple(BookCharacterId.VOSS, CharacterFlagAxis.SUSPICION, 3),
                            Triple(BookCharacterId.NATASHA, CharacterFlagAxis.RESPECT, 1)
                        ),
                        storyAxisDeltas = mapOf(
                            StoryFlagAxis.TACTICAL_DISCIPLINE to 3,
                            StoryFlagAxis.INDEPENDENCE to 2
                        ),
                        avengersTrustDelta = 1,
                        setFlags = mapOf("ch5_voss_fate" to "handed_over"),
                        branchTag = "Soğukkanlı Karar",
                        impactSummary = "Duygusal bağ kurmadan Voss'u sisteme teslim ettin; ama gitmeden önce sana İstanbul tünelleri için tek bir şifre fısıldadı.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch5_s2_snow_truth",
            chapterNumber = 5,
            sceneOrder = 2,
            sceneTitle = "Karda İki Kırık Ayna",
            locationTag = "AVENGERS KULESİ • YERALTI SORGU ODASI",
            paragraphs = listOf(
                "\"Beş dakika bekleyin, kimse ateş etmesin,\" dedin kulaklığa. Sesindeki o soğuk, tartışmaya kapalı kesinlik Steve Rogers'ı bir saniyeliğine durdurdu; çatıdaki Natasha Romanoff ise tek kelimeyle onay verip keskin nişan hattını sabit tuttu: \"Beş dakikan var Aiden, bir saniye fazla değil.\"",
                "Kar fırtınasının ortasında Elias Voss'un iki adım karşısına yürüdün. Ne kalkan açtın ne de saldırı pozisyonu aldın. Sadece göz bebeklerindeki çarpıcı maviyi yumuşak, dingin bir beyaza çevirdin — Beyaz Göz'ü hayatında ilk kez bir silah ya da kilit olarak değil, sessiz bir kapı olarak araladın. \"Bana yalan söylüyorsan bu karların altından bir daha kalkamazsın,\" dedin. \"Bırak göreyim.\"",
                "Voss gözlerini kapatmadı; aksine bütün zihinsel savunmasını indirdi. Zihnine dokunduğunda karşına çıkan şey kötülük, kin ya da bir tuzak değildi — parçalanmış, yanık kokulu, karanlık bir çocukluktu. Beyaz laboratuvar önlüklü, gözleri yorgunluktan kızarmış bir baş bilim insanı: **Dr. Evelyn Hale.** 2006 yılında Dünya Güvenlik Konseyi'nin gölge birimi adına Sonsuzluk Taşı frekansını yapay olarak kopyalamaya çalışan o karanlık deney — **Proje Ayna.** Beş yaşındayken New York gökyüzünde açılan o mavi yarık bir kaza değildi; apartmanın iki sokak ötesindeki yeraltı istasyonundan kaçan kontrolsüz bir kozmik dalgaydı.",
                "Ve Voss'un zihninin en derin, en yakıcı köşesinde üçüncü bir yüz yankılanıyordu: yıllarca cam duvarların arkasında 'canavar' diye damgalandıktan sonra kaçan ve şimdi İstanbul Boğazı'nın altındaki eski Bizans sarnıçlarında Rezonans Kulesi'ni yeniden ateşlemeye hazırlanan üçüncü denek — **Mira (Denek 8-C).**",
                "[[DOSSIER|PROJE AYNA — DENEK ÖZETİ (ZİHİN OKUMA KAYDI)|• Denek 6-B (Elias Voss): 2005'teki ilk deneyden sağ çıktı; kırmızı, dengesiz gücü vücudunu yakıyor.\n• Denek 7-A (Aiden Blackwood): 14 Kasım 2006'da saf mavi çekirdekle kusursuz uyum sağlayan tek kişi.\n• Denek 8-C (Mira): Yıllarca cam hücrede tutuldu; şimdi İstanbul'daki ana Rezonans Kulesi'ni çalıştırmak üzere.]]",
                "Gözlerin yeniden doğal mavisine döndüğünde soğuk havada nefes nefeseydin; şakakların zonkluyordu. Voss ise yıllardır ilk kez birinin kendi kırık acısını yargılamadan, korkmadan gördüğünü fark etmişti. Cebindeki küçük enerji tetikleyicisini karların içine bıraktı, tek bir kavga bile çıkarmadan seninle birlikte Avengers Kulesi'ne gelmeyi kabul etti.",
                "Bir saat sonra Kule'nin güçlendirilmiş yeraltı sorgu odasında, kalın tek yönlü gözlem camının önünde karşı karşıya oturuyordunuz. Camın arkasında Steve, Tony, Bruce, Natasha ve Wanda sessizce bekliyordu. Voss bileklerindeki enerji sönümleyici titanyum kelepçelere baktı, sonra doğrudan senin gözlerinin içine: \"Bütün gerçeği bizzat zihnimden gördün Aiden. Dr. Evelyn Hale, Proje Ayna, Mira... tek bir kelimesi bile yalan değildi. Ve yarın şafakta S.H.I.E.L.D. nakil timi gelip beni okyanusun ortasındaki o güneş görmeyen Raft kafesine kapatacak. Seni de dokuz yaşındayken öyle bir kafese atmaya çalışmışlardı. Şimdi karar tamamen senin: beni sisteme teslim mi edeceksin, yoksa İstanbul'a giderken beni Kule'de kendi sorumluluğunda mı tutacaksın?\""
            ),
            decisionTitle = "Elias Voss'un Kaderi",
            choicePrompt = "Voss'un kaderi senin iki dudağının arasında.",
            choices = listOf(
                BookChoiceDef(
                    id = "ch5_c2_protect_voss",
                    label = "Voss'u Kule'de kendi sorumluluğunda tut — kimse bir daha kafese kapatılmayacak.",
                    toneHint = "Merhamet • Sorumluluk • Müttefik Kazan",
                    nextNodeId = "ch5_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.VOSS to 6, BookCharacterId.WANDA to 4, BookCharacterId.NATASHA to 2, BookCharacterId.STEVE to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.VOSS, CharacterFlagAxis.LOYALTY, 4),
                            Triple(BookCharacterId.VOSS, CharacterFlagAxis.TRUST, 3),
                            Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 3),
                            Triple(BookCharacterId.STEVE, CharacterFlagAxis.RESPECT, 2)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 4, StoryFlagAxis.SHIELD_SUSPICION to 2),
                        avengersTrustDelta = 2,
                        setFlags = mapOf("ch5_voss_fate" to "protected"),
                        branchTag = "Voss Kule'de Kaldı",
                        impactSummary = "Voss'u kafesten kurtardın; İstanbul operasyonunda en kritik rehberin oldu.",
                        isBranching = true,
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch5_c2_handover_voss",
                    label = "Voss'u S.H.I.E.L.D.'a teslim et — İstanbul'a kendi kurallarınla git.",
                    toneHint = "Soğukkanlı • Pragmatik • Mesafeli",
                    nextNodeId = "ch5_s3_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 2, BookCharacterId.STEVE to 1),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 3, StoryFlagAxis.INDEPENDENCE to 2),
                        avengersTrustDelta = 1,
                        setFlags = mapOf("ch5_voss_fate" to "handed_over"),
                        branchTag = "Soğukkanlı Karar",
                        impactSummary = "Voss'u teslim ettin ve İstanbul dosyasına tek başına odaklandın.",
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "kritik"
                    )
                )
            )
        ),

        // ── BÖLÜM V — FİNAL ─────────────────────────────────────────────────────
        BookSceneNode(
            id = "ch5_s3_end",
            chapterNumber = 5,
            sceneOrder = 3,
            sceneTitle = "Bırakılmadın. Sadece Beklendin.",
            locationTag = "AVENGERS KULESİ • HANGAR & ŞAFAK",
            paragraphs = listOf(
                "Sorgu odasından çıktığında gece yarısını çoktan geçmişti. Seksen dördüncü kattaki odana çekildin ama gözlerini bile kapatmadın. Cam duvara yaslanıp dışarıda hâlâ yağan karı izlerken zihninde üç farklı yüz dönüp duruyordu: 2006'nın mavi gecesi, gözlerinde kırık bir kırmızı ateş taşıyan Elias Voss ve İstanbul'un karanlık sarnıçlarında bütün dünyaya öfke biriktiren Mira.",
                "Ertesi sabah soluk kış güneşi buz tutmuş Manhattan gökdelenlerinin arkasından yükselirken Avengers Kulesi'nin doksanıncı katındaki gizli hangarda Quinjet'in çift itici motorları alçak, derin bir uğultuyla ısınmaya başlamıştı. Kokpitin üç boyutlu hologram ekranında transatlantik uçuş rotası parlak mavi bir çizgiyle işaretlenmişti: **İSTANBUL — BOĞAZİÇİ.**",
                "Boğaz'ın soğuk sularının altındaki yüzlerce yıllık eski Bizans sarnıçlarının ve karanlık yeraltı su tünellerinin derinliklerinde, beş yaşındayken aileni elinden alan o mavi gecenin bütün saklı cevapları — ve senin öfke dolu, karanlık aynan olan Mira — bekliyordu. Hangarda herkes kendi sessiz hazırlığındaydı: Steve Rogers ekipman sandıklarını ve yüksek voltajlı enerji sönümleyicilerini kilitliyordu; Clint Barton ok başlıklarını kontrol edip Tony'nin hazırladığı yeni termal tarayıcıları çantaya yerleştiriyordu; Natasha Romanoff uçuş planını, İstanbul'un eski yeraltı su haritasını ve acil tahliye noktalarını ince bir tablete indiriyordu; Wanda Maximoff ise hangarın cam kenarında durmuş, derin bir sessizlik içinde zihnini yaklaşan fırtınaya hazırlıyordu.",
                "Tony Stark yanına gelip kolunu hafifçe omzuna yaslamadan, mesafene dikkat ederek durdu. \"Ben Kule'de kalıp Dünya Güvenlik Konseyi'nin uydularını ve o kara bütçe yöneticilerini oyalayacağım Blackwood,\" dedi. \"İletişim hattın hep açık olacak. Ve bir tavsiye: İstanbul'a indiğinde o meşhur köftecilerden birine uğramak istersen, önce Rezonans Kulesi'ni kapat.\"",
                "\"Bunu Natasha'dan mı duydun?\" diye sordun, dudaklarının kenarında çok hafif bir kıvrımla. Tony sırıttı: \"Bu kulede hiçbir sır uzun süre saklanmaz, özellikle mutfakla ilgiliyse.\"",
                "Quinjet'in açık rampasının kenarında durup elindeki küçük metal koordinat plakasına son kez baktın. Yirmi yıl boyunca kıtalar, yabancı gezegenler ve kırık zaman çizgileri boyunca sadece kaçtın. Bir yere tutunmamak, arkana bakmamak, gerçeği eşelememek hayatta kalma kuralındı. Ama artık kaçış gerçekten bitmişti. Bu kez başka bir şehre saklanmak için değil — yirmi yıl önce yarım kalan hikâyenin kalbine yürüyüp kendi adını geri almak için yola çıkıyordun.",
                "Natasha kokpitin yanından sana kısa bir bakış attı: \"Sekiz saatlik uçuş, Aiden. Hazır mısın?\" Başını sallayıp rampadan içeri yürüdün. Kapı hidrolik bir tıslamayla kapandı ve Quinjet kış bulutlarını yararak doğuya doğru yükseldi."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch5_voss_fate",
                    requiredFlagValue = "protected",
                    callbackLabel = "Kafes Kırıldı — Voss Kule'de",
                    paragraphs = listOf(
                        "Şafak sökerken S.H.I.E.L.D. nakil timi helikopter pistine indiğinde, Steve Rogers ve Tony Stark ile birlikte kapıya çıkıp Elias Voss'u teslim etmedin. \"Onun sorumluluğu bende,\" dedin; sesin o kadar netti ki nakil komutanı geri çekilmek zorunda kaldı. Bruce Banner'ın gözetimindeki güvenli tıbbi kanatta, kelepçesiz ama dengeli bir odada kalacaktı.",
                        "Gitmeden hemen önce camın ardından sana İstanbul sarnıçlarının üçüncü sütunundaki gizli giriş şifresini ve Mira'nın kalkanındaki tek zayıf noktayı fısıldadı. Ve hangara yürürken Wanda Maximoff yanından geçip sana yıllardır neredeyse kimseye bakmadığı kadar derin, sakin ve minnet dolu bir sıcaklıkla baktı."
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch5_voss_fate",
                    requiredFlagValue = "handed_over",
                    callbackLabel = "Soğukkanlı Protokol",
                    paragraphs = listOf(
                        "Elias Voss'u şafakta S.H.I.E.L.D. nakil birimine teslim ettin; geçmişin yarattığı o ağır duygusal bağın yargını bulandırmasına izin vermeden yalnızca göreve ve güvenliğe odaklandın. Tony ve Steve bu soğukkanlı disipline sessizce saygı duydu; Wanda ise üzüntüsünü saklamadı ama kararını sorgulamadı.",
                        "Ancak nakil aracına binmeden hemen önce Voss başını omzunun üzerinden çevirip yalnızca senin duyacağın kadar alçak, kırık bir sesle tek bir şifre fısıldadı: \"Yerebatan'ın üçüncü sütunu, Aiden. Mira orada doğdu. Kendine dikkat et, 7-A.\""
                    )
                )
            ),
            isChapterEnd = true,
            chapterEndQuote = "Bazen karanlığın içine yürümeden kendi ışığının nereden geldiğini göremezsin."
        )
    ).associateBy { it.id }
}
