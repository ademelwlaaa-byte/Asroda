package com.example.book

/**
 * SEASON 2 — CHAPTERS 17 & 18 of "Kozmik Sürükleniş — 2. Sezon: Demir ve Yankı"
 *
 * Chapter 17 ("Yara İzinin Gerçeği"):
 * - Deep emotional confrontation with wounded Sıfır: confesses the heartbreaking truth of his silver
 *   cheek scar, how his isolated timeline broke in Sokovia, and why YOUR bonds changed everything.
 * - Big branching decision: trust Sıfır to come to Sokovia (`s2_zero_choice = "ally"`) vs. keep him
 *   secured at the Tower so your Singularity operates parasite-free (`s2_zero_choice = "tower"`).
 * - Season 1 allies callback: saved Elias Voss & Mira answer the call for Sokovia civilian evacuation!
 * - Infiltration into Sokovia's fortress & breaking Natasha Romanoff out of Ultron's cell.
 *
 * Chapter 18 ("Yükselen Şehir"):
 * - Sokovia rips out of the earth and ascends into the clouds!
 * - Grounded high-stakes street rescue with the full Avengers + Vision + Pietro + Wanda.
 * - **ROMANCE STAGE 3 — "GERİ DÖN" BATTLE PROMISE KISS** (Natasha or Wanda, or epic Avengers brotherhood vow).
 * - Ultron Prime's cracked visor glows GOLD-BLUE as **Asıl Mimar** confronts Aiden mid-battle!
 */
object CosmicDriftS2Chapters17To18 {

    val chaptersMeta: List<BookChapterMeta> = listOf(
        BookChapterMeta(
            number = 18,
            romanNumeral = "XVIII",
            title = "Yara İzinin Gerçeği",
            titleEn = "Truth of the Scar",
            subtitle = "BÖLÜM XVIII • Canlı 3D Sıfır Yüzleşmesi, Ultron'un Felsefesi ve Natasha'yı Kurtarma",
            description = "Yaralı Sıfır yanağındaki gümüş yara izinin ve çöken geleceğin acı gerçeğini canlı 3D yüzleşmede itiraf eder. Ultron ve Asıl Mimar'ın karanlık planı derinleşirken ekip Sokovia'ya inip Natasha'yı kafesten çıkarır.",
            descriptionEn = "Wounded Zero confesses the truth of his scar in a live 3D encounter. As Ultron and the Architect's philosophy deepens, the Avengers storm Sokovia to rescue Natasha.",
            estimatedMinutes = 16,
            accentHex = 0xFF818CF8L,
            startNodeId = "ch17_s1",
            sceneCount = 5,
            decisionCount = 4
        ),
        BookChapterMeta(
            number = 20,
            romanNumeral = "XX",
            title = "Yükselen Şehir",
            titleEn = "The Rising City",
            subtitle = "BÖLÜM XX • Gökyüzüne Kopan Sokovia, Savaş Öncesi Öpücük ve Mimar'ın Meydan Okuması",
            description = "Sokovia yeryüzünden kopup bulutlara yükselir! Avengers, Vision, Pietro, Voss ve Mira sivilleri kurtarırken, kalbinin ait olduğu kişiyle 'geri dön' öpücüğü yaşanır ve Asıl Mimar Ultron'un gözlerinden sana seslenir.",
            descriptionEn = "Sokovia tears into the sky! Avengers, Vision, Pietro, Voss, and Mira rescue civilians, a passionate 'come back to me' kiss seals your vow, and the Architect speaks through Ultron's cracked visor.",
            estimatedMinutes = 16,
            accentHex = 0xFFF43F5EL,
            startNodeId = "ch18_s1",
            sceneCount = 5,
            decisionCount = 4
        )
    )

    val nodes: Map<String, BookSceneNode> = listOf(
        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 17 — SIFIR'IN İTİRAFI & YARA İZİNİN GERÇEĞİ (Karar 1/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch17_s1",
            chapterNumber = 18,
            sceneOrder = 1,
            sceneTitle = "Kırık Aynanın İtirafı",
            locationTag = "AVENGERS KULESİ · REVİR ODASI • 23:00",
            paragraphs = listOf(
                "[[LIVE3D|ZERO|TOWER_CELL|REVİR ODASI · KENDİ KARANLIK GELECEĞİNLE YÜZ YÜZE|Sıfır'ın gözlerine bak, 3D kamerayı sürükle ve beş yıllık yalnızlığın sesini dinle.]]",
                "Revir odasının loş mavi ışığında Sıfır uzun süre kendi ellerine baktı. Az önce terasta çarpıştığınız o öfkeli gölge gitmiş, yerine beş yıl boyunca yalnızca enkazların arasında nefes almış yorgun bir adam gelmişti.",
                "\"Benim zamanımda,\" diye başladı sesi pürüzlü bir fısıltıyla, \"Manhattan'da Vaelor'u yendikten sonra korktum. Kimseye yaklaşmadım. Çiftliğe hiç gitmedik. Stark'ı affetmedim, Natasha'ya duvar ördüm, Wanda'ya sırtımı döndüm. Kendimi Kule'den dışarı attım çünkü hâlâ bir 'canavar' olduğuma inanıyordum.\"",
                "Parmaklarını sol yanağındaki o uzun gümüş yara izinin üzerinde gezdirdi. \"Bu izi bana Ultron değil, **Sokovia'nın düşüşü** verdi. Vision hiç doğmadı. Pietro o meydanda öldü. Steve'in kalkanı Manhattan küllerinin arasında ikiye bölündü. Ben her şey bittikten sonra çaresizlikle Kırmızı Göz'ü sonuna kadar açtım — ve tam o anda Asıl Mimar gökyüzünden indi. Doctor Strange son nefesinde zamanı kırıp beni beş yıl geriye, sana yolladı. Ama iki tane Tekillik aynı zamana sığmıyor Aiden... bu parazit beni her saat biraz daha siliyor.\"",
                "Başını kaldırıp buz mavisi gözlerinle doğrudan kendi gözlerinin içine baktı: \"Ben Beşiği yok etmek için geldim çünkü Vision'ı tanımıyordum. Ama az önce o varlığın Mjolnir'i kaldırdığını, Pietro'nun ablasının yanında durduğunu ve senin hâlâ sevmekten korkmadığını gördüm. Sen... benim başaramadığım her şeyi başarmışsın.\""
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch15_zero_defeat",
                    requiredFlagValue = "spared",
                    callbackLabel = "Ayna Düellosu Yankısı — Merhamet",
                    paragraphs = listOf(
                        "Sıfır'ın sesi hafifçe titredi: \"Terasta beni öldürebilirdin Aiden. Elini uzattığın o saniye anladım — sen asla bana dönüşmeyeceksin.\""
                    )
                )
            ),
            decisionTitle = "Bölüm XVII (1/4) — Sıfır'ın Kader Kararı",
            choicePrompt = "Sıfır Sokovia'da seninle omuz omuza savaşıp kefaretini ödemek istiyor — ama yanında durursa Tekillik Paraziti ikinizin gücünü biraz zorlayacak. Onu Sokovia'ya götürecek misin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch17_c1_trust_zero_ally",
                    label = "Onu ayağa kaldır: \"Benimle geliyorsun. Geçmişini yıkan o şehri bugün birlikte kurtaracağız — parazite rağmen sırt sırta duracağız!\"",
                    toneHint = "Sıfır Müttefik Olur • Sokovia'da İki Aiden • Epik Kefaret",
                    nextNodeId = "ch17_s2_allies_call",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.ZERO to 6, BookCharacterId.STEVE to 3, BookCharacterId.WANDA to 3, BookCharacterId.VISION to 4),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.ZERO, CharacterFlagAxis.TRUST, 4),
                            Triple(BookCharacterId.ZERO, CharacterFlagAxis.LOYALTY, 4)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 3),
                        setFlags = mapOf("s2_zero_choice" to "ally"),
                        branchTag = "Sıfır ile İttifak",
                        impactSummary = "Sıfır'ın gözlerinde beş yıl sonra ilk kez umut parladı: 'Bu sefer kimse geride kalmayacak, Aiden.'",
                        isBranching = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch17_c1_keep_zero_tower",
                    label = "Omzuna elini koy: \"Çok kan kaybettin ve parazit ikimizi de tüketiyor. Kule'de kalıp Bruce'un sistemleriyle dünyayı koru — ben Sokovia'da tam gücümle savaşacağım.\"",
                    toneHint = "Tam Güç • Sıfır Kule'de Güvende • Taktik Akıl",
                    nextNodeId = "ch17_s2_allies_call",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.ZERO to 3, BookCharacterId.TONY to 4, BookCharacterId.BRUCE to 3, BookCharacterId.NATASHA to 2),
                        characterAxisDeltas = listOf(
                            Triple(BookCharacterId.ZERO, CharacterFlagAxis.RESPECT, 3)
                        ),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 3),
                        setFlags = mapOf("s2_zero_choice" to "tower"),
                        branchTag = "Saf Tekillik Gücü",
                        impactSummary = "Sıfır başını kabullenerek eğdi: 'Haklısın — tam gücünle git. Kule'den kalkanın olacağım.'",
                        isBranching = true
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 2: 1. SEZON DOSTLARI GERİ DÖNÜYOR & SOKOVİA'YA İNİŞ (Karar 2/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch17_s2_allies_call",
            chapterNumber = 18,
            sceneOrder = 2,
            sceneTitle = "Eski Borçlar — Sokovia'ya İniş",
            locationTag = "QUINJET · SOKOVİA HAVA SAHASI • 06:40",
            paragraphs = listOf(
                "Şafak soğuğunda Quinjet, Sokovia'nın sisli vadilerine doğru alçaldı. Kabinde tam kadro hazırdı: Steve Rogers kalkanını koluna geçirdi, Thor Mjolnir'i kavradı, Tony Stark yeni Mark 45 zırhının ön testini bitirdi, Vision altın peleriniyle sükûnetle bekliyordu, Clint ok kılıfını kilitledi, Wanda ve Pietro ise yıllar sonra ilk kez aynı safta, birbirlerinin elini sıkarak doğdukları şehre bakıyordu.",
                "Steve kabinin ortasında durup ekibe o unutulmaz bakışıyla seslendi: \"Ultron bizi birer canavar sanıyor. Belki de haklı olduğu tek bir yer var: bugün ona durduramayacağı bir aile göstereceğiz. Siviller çıkana kadar kimse şehri terk etmeyecek — bir kişi bile ölürse, ayağa kalkıp yürümeye devam edeceğiz!\"",
                "[[COMMS|NICK FURY · GİZLİ HELICARRIER HATTI|Eski dostlarım, tek başınıza değilsiniz. Ben ve eski S.H.I.E.L.D. ekibim, acil tahliye nakliye gemilerini kaldırdık — gökyüzünden size destek olacağız!]]"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "ch5_voss_fate",
                    requiredFlagValue = "protected",
                    callbackLabel = "1. Sezon Müttefiki — Elias Voss Destek Hattında!",
                    paragraphs = listOf(
                        "Fury'nin telsizinin hemen ardından kalın, kararlı bir ses daha bağlandı: \"Ben **Elias Voss**, 7-A! Fury'nin Helicarrier kurtarma filikasındayım — 1. Sezon'da beni Raft'tan kurtardığın günü unutmadım! Sokovia sivilleri için kuzey tahliye hattını ben tutuyorum!\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "ch7_finale_act",
                    requiredFlagValue = "saved_mira",
                    callbackLabel = "1. Sezon Müttefiki — Mira Sahada!",
                    paragraphs = listOf(
                        "Doğu sınır karakolundan bir başka tanıdık ses yükseldi: \"İstanbul'da bana yeni bir hayat verdin Aiden — ben **Mira**! Sınır köprüsündeki otobüs konvoyunu kendi bariyerimle koruyorum, siz şehrin kalbine inin!\""
                    )
                )
            ),
            decisionTitle = "Bölüm XVII (2/4) — Sokovia'da İlk Görev Dağılımı",
            choicePrompt = "Quinjet şehrin dış tepesine indi. İlk dakika herkes pozisyon alıyor — Natasha'yı hücreden çıkarmaya kimle gideceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch17_c2_natasha_with_bruce",
                    label = "Bruce Banner'la doğrudan kale altındaki hücreye ışınlan — Natasha'yı ilk elden çıkarıp birlikte yüzeye dön!",
                    toneHint = "Natasha Kurtarışı • Bruce Uyumu • Doğrudan Sızma",
                    nextNodeId = "ch17_s3_natasha_free",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 5, BookCharacterId.BRUCE to 4),
                        setFlags = mapOf("ch17_rescue_team" to "bruce_aiden"),
                        impactSummary = "Bruce'la birlikte yeraltı tüneline indin; nöbetçi dronları tek mavi şokla indirip Natasha'nın kafesine ulaştın!",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch17_c2_evac_with_twins",
                    label = "Önce Wanda ve Pietro'yla meydandaki polis merkezini ve sivilleri uyar, sonra yıldırım hızıyla Bruce'un hücreye ulaşmasına yol aç!",
                    toneHint = "Sokovia Tahliyesi • Wanda & Pietro Uyumu",
                    nextNodeId = "ch17_s3_natasha_free",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 4, BookCharacterId.PIETRO to 5, BookCharacterId.STEVE to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch17_rescue_team" to "evac_first"),
                        impactSummary = "Pietro şehri on saniyede tahliye alarmına geçirdi, Wanda halkı köprüye yönlendirdi — sen de hemen hücre tüneline ışınlandın!"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 3: NATASHA'YI KURTARMA (Karar 3/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch17_s3_natasha_free",
            chapterNumber = 18,
            sceneOrder = 3,
            sceneTitle = "Kırılan Kafes — Natasha Özgür",
            locationTag = "SOKOVİA KALESİ · YERALTI HÜCRESİ • 06:58",
            paragraphs = listOf(
                "Nemli taş mahzenin demir parmaklıklarını mavi kinetik bir darbeyle ikiye ayırdığında, Natasha Romanoff içeriden gülümseyerek çıktı — elinde çoktan söktüğü bir Ultron nöbetçisinin elektro-copu vardı. \"Bir dakika geciktin Blackwood,\" dedi göz kırparak, ama hemen ardından yüzündeki ciddiyet geri döndü. \"Ultron beni öldürmedi çünkü bana yeni planını anlatmak için bir izleyici istiyordu. Şehrin bütün altını Vibranyum iticilerle döşemiş — az sonra düğmeye basacak!\"",
                "Tam o anda yerin derinliklerinden kulakları sağır eden, gök gürültüsünden bile ağır bir **metal çığlığı** yükseldi! Taş duvarlar yarıldı, tavan tozları başınızdan döküldü. Ultron yüzlerce demir nöbetçiyi sokaklara salmış ve şehrin altındaki yerçekimi motorlarını çalıştırmıştı! Bruce derin bir nefes alıp Natasha'ya baktı, \"Diğer adamın sahaya inme vakti geldi,\" diyerek Hulk'a dönüşüp üst tavanı parçalayarak meydana fırladı!"
            ),
            decisionTitle = "Bölüm XVII (3/4) — Yüzeye Çıkış Anı",
            choicePrompt = "Yeraltı koridoru çökerken Natasha'yla omuz omuza yukarı çıkıyorsun. Dışarıdaki ilk hedef ne?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch17_c3_bridge_civilians",
                    label = "Natasha'yı elinden tutup doğrudan çöken ana köprüye ışınlan — kenarda asılı kalan otobüsü birlikte çekip kurtarın!",
                    toneHint = "Kahramanca Kurtarma • Natasha & Steve Uyumu",
                    nextNodeId = "ch17_s4_tremor",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 4, BookCharacterId.STEVE to 3),
                        avengersTrustDelta = 3,
                        setFlags = mapOf("ch17_surface_act" to "bridge"),
                        impactSummary = "Çöken köprüdeki bütün yolcuları saniyeler içinde güvenli tarafa çektiniz!"
                    )
                ),
                BookChoiceDef(
                    id = "ch17_c3_church_scout",
                    label = "Doğrudan merkez kilisesindeki Vibranyum çekirdeğini taramaya ışınlan — Ultron'un sistem açığını Tony ve Vision'a bildir!",
                    toneHint = "Stratejik Keşif • Tony & Vision Avantajı",
                    nextNodeId = "ch17_s4_tremor",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 4, BookCharacterId.VISION to 4),
                        storyAxisDeltas = mapOf(StoryFlagAxis.TACTICAL_DISCIPLINE to 3),
                        setFlags = mapOf("ch17_surface_act" to "core_scout"),
                        impactSummary = "Tony çekirdeğin zayıf halkasını haritaladı: 'Anahtar düşerse bütün şehir düşer — kiliseyi ne pahasına olursa olsun tutmalıyız!'"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch17_s4_tremor",
            chapterNumber = 18,
            sceneOrder = 4,
            sceneTitle = "Toprak Yarıldı",
            locationTag = "SOKOVİA MEYDANI · KOPMA ANI • 07:05",
            paragraphs = listOf(
                "Şehrin dört bir yanındaki asfalt caddeler dev çatlaklarla yarıldı. Toprağın altından dev mavi-kırmızı Vibranyum roket iticileri alev kusarak patladı — ve bütün Sokovia kenti, binaları, köprüleri ve binlerce insanıyla birlikte yavaş yavaş yerden kopmaya başladı!",
                "[[ALARM|SOKOVİA ACİL DURUMU · YÜKSELİŞ BAŞLADI|Şehir yerden koptu ve dakikada 400 metre hızla atmosfere tırmanıyor! Çıkış köprüleri koptu — tüm Avengers savunma hattına!]]"
            ),
            decisionTitle = "Bölüm XVII (4/4) — Yükselişin İlk Saniyesi",
            choicePrompt = "Zemin ayaklarının altında titreyip gökyüzüne doğru kalkarken ekibe telsizden hangi mesajı vereceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch17_c4_rally_cry",
                    label = "\"Kimse aşağı bakmasın!\" diye haykır telsizden. \"Bu şehir düşmeyecek, kimse ölmeyecek — Avengers, toplanın!\"",
                    toneHint = "İlham Veren Lider • Tüm Ekip Morali +",
                    nextNodeId = "ch17_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 3, BookCharacterId.THOR to 3, BookCharacterId.WANDA to 3, BookCharacterId.PIETRO to 3, BookCharacterId.NATASHA to 3),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch17_rally" to "inspired"),
                        impactSummary = "Steve gülümsedi: 'Kaptanlığın hakkını veriyorsun Aiden — hadi şu teneke orduyu indirelim!'"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch17_s5_end",
            chapterNumber = 18,
            sceneOrder = 5,
            sceneTitle = "Bulutların Arasında Bir Ada",
            locationTag = "UÇAN SOKOVİA · 1.200 METRE İRTİFA • 07:10",
            paragraphs = listOf(
                "Sokovia artık bulutların içindeydi. Etrafınızda yüzlerce Ultron nöbetçisi uçuyor, aşağıda ise uçurumun kenarında çaresizce bekleyen siviller vardı. Ama bu gökyüzü adasında umut henüz ölmemişti — çünkü Avengers, Vision, Pietro ve Aiden Blackwood oradaydı."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Bir şehir göğe kalktığında, kahramanların ayakları yere her zamankinden sağlam basmalıdır.”"
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // BÖLÜM 18 — YÜKSELEN ŞEHİR, SAVAŞ ÖPÜCÜĞÜ (AŞAMA 3) & MİMAR'IN SESİ (4 Karar)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch18_s1",
            chapterNumber = 20,
            sceneOrder = 1,
            sceneTitle = "Gökyüzü Sokaklarında Tahliye",
            locationTag = "UÇAN SOKOVİA · DOĞU BULVARI • 07:16",
            paragraphs = listOf(
                "Bulutlar apartman pencerelerinin arasından akıyordu. Uçamadığın için şehrin kopmuş uçurum kenarları her zamankinden daha tehlikeliydi — ama her adımın kusursuzdu: bir apartmanın balkonundan diğer caddenin asfaltına ardışık mavi kuantum kıvılcımlarıyla ışınlanıyor, düşen duvarları kinetik kalkanla durdurup çocukları Clint ve Pietro'nun tahliye otobüslerine taşıyordun.",
                "Vision tam o sırada Ultron'un ana gövdesinin önüne inip alnındaki Zihin Taşı'yla onu internet ağından tamamen kopardı! Ultron acıyla kükreyip geri savruldu: artık dünyaya kaçamazdı — son bir nöbetçi kalana dek burada, Sokovia'da bitirilecekti!",
                "[[COMMS|CLINT BARTON · HELICARRIER KURTARMA GEMİLERİ|Gökyüzüne bakın dostlar! Fury eski Helicarrier'ı bulutların arasından çıkardı — kurtarma botları kenarlara yanaşıyor! Herkes sivilleri kenar iskelelerine taşısın!]]"
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "s2_zero_choice",
                    requiredFlagValue = "ally",
                    callbackLabel = "Müttefik — Sıfır Sokovia Sokaklarında!",
                    paragraphs = listOf(
                        "Sol kanatta üç Ultron nöbetçisi bir çocuk parkını kuşatmışken, koyu mavi bir kıvılcımla **Sıfır** araya ışınlandı ve üçünü tek darbede hurdaya çevirdi! Telsizden nefes nefese güldü: \"Benim zamanımda bu parkı kurtaramamıştım Aiden... şimdi bir borç daha kapandı!\""
                    )
                )
            ),
            decisionTitle = "Bölüm XVIII (1/4) — Tahliye Hattında Destek",
            choicePrompt = "Helicarrier kurtarma gemileri kenara yanaşırken iki farklı cephede yardım çağrısı yükseliyor. Kime ışınlanacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch18_c1_help_wanda_plaza",
                    label = "Korkuyla donakalan Wanda'nın yanına ışınlan — \"Bu senin suçun değil Wanda! Kapıdan dışarı adımını attığın an sen bir Avenger'sın!\"",
                    toneHint = "Wanda İlhamı • Kızıl Kaos Güçlenir",
                    nextNodeId = "ch18_s2_battle_kiss",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 5, BookCharacterId.PIETRO to 3),
                        setFlags = mapOf("ch18_street_help" to "wanda"),
                        impactSummary = "Wanda gözyaşını silip gözlerini kızıl ateşle yaktı; tek bir dalgayla on iki nöbetçiyi sokağın ötesine savurdu!",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch18_c1_help_nat_clint",
                    label = "Tahliye iskelesinde kuşatılan Natasha ve Clint'in önüne ışınlan — mavi enerji emilimiyle nöbetçi ateşini yutup onları kurtar!",
                    toneHint = "Natasha & Clint Uyumu • Koruyucu Refleks",
                    nextNodeId = "ch18_s2_battle_kiss",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 5, BookCharacterId.CLINT to 4),
                        setFlags = mapOf("ch18_street_help" to "natasha"),
                        impactSummary = "Natasha sırtını sırtına yaslayıp güldü: 'Sana güvenmekle hiç yanılmadım, Blackwood!'",
                        isRelationship = true
                    )
                ),
                BookChoiceDef(
                    id = "ch18_c1_help_vision_thor",
                    label = "Kilise meydanında Ultron'un dev zırhıyla çarpışan Thor ve Vision'ın yanına ışınlanıp üçlü baskı kur!",
                    toneHint = "Ağır Taarruz • Thor & Vision Saygısı",
                    nextNodeId = "ch18_s2_battle_kiss",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.THOR to 4, BookCharacterId.VISION to 4, BookCharacterId.TONY to 3),
                        setFlags = mapOf("ch18_street_help" to "heavy"),
                        impactSummary = "Üçlü saldırı Ultron'un göğüs zırhında ilk çatlağı açtı!"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 2: ROMANTİZM AŞAMA 3 — "GERİ DÖN" SAVAŞ ÖPÜCÜĞÜ! (Karar 2/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch18_s2_battle_kiss",
            chapterNumber = 20,
            sceneOrder = 2,
            sceneTitle = "Son Nefesten Önce — Geri Dön Sözü",
            locationTag = "UÇAN SOKOVİA · KİLİSE GİRİŞ KEMERİ • 07:28",
            paragraphs = listOf(
                "Şehir artık üç bin metre yükseklikteydi; hava buz gibi incelmiş, uzaktan Ultron'un son dev nöbetçi dalgası karanlık bir çekirge sürüsü gibi kilise meydanına doğru akıyordu. Az sonra bütün ekip son savunma için kilisedeki Vibranyum çekirdeğinin etrafında toplanacak — ve kimsenin bu savaştan sağ çıkacağının garantisi yoktu.",
                "Toz ve dumanın içinde bir anlığına zaman yavaşladı. Kalabalığın hemen kıyısında, fırtınanın tam ortasında son bir kez göz göze gelme fırsatın vardı."
            ),
            conditionalNarratives = listOf(
                ConditionalNarrative(
                    requiredFlagKey = "s2_romance",
                    requiredFlagValue = "natasha",
                    callbackLabel = "Natasha · Çiftlikteki Öpücüğün Yankısı",
                    paragraphs = listOf(
                        "Natasha elindeki elektro-copları kemerine takıp koşarak yanına geldi; yanağında ince bir kül izi vardı ama yeşil gözleri alev gibi parlıyordu. Yakandan tutup seni taş kemerin gölgesine çekti: \"Eğer burada ölürsen Aiden Blackwood, seni öbür dünyada bile rahat bırakmam — duydun mu?\""
                    )
                ),
                ConditionalNarrative(
                    requiredFlagKey = "s2_romance",
                    requiredFlagValue = "wanda",
                    callbackLabel = "Wanda · Yıldızlı Bahçenin Yankısı",
                    paragraphs = listOf(
                        "Wanda parmak uçlarındaki kızıl kıvılcımları indirip hızla yanına koştu, iki elini göğsüne yasladı: \"Bütün şehir titriyor Aiden... çekirdeği ben koruyacağım ama lütfen kendine dikkat et, seni kaybetmeye dayanamam!\""
                    )
                )
            ),
            decisionTitle = "Bölüm XVIII (2/4) — Savaşın Ortasında Kalp Sözü (Aşama 3)",
            choicePrompt = "Ultron'un ordusu otuz saniye içinde meydana inecek. Son savaşa girmeden önce ne yapacaksın?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch18_c2_battle_kiss_natasha",
                    label = "Natasha'nın yüzünü ellerinin arasına al ve toz dumanın ortasında onu derin, tutkulu bir 'geri dön' öpücüğüyle öp: \"İkimiz de döneceğiz, Nat.\"",
                    toneHint = "♥ NATASHA SAVAŞ ÖPÜCÜĞÜ • Aşama 3 • Tutkulu Yemin",
                    nextNodeId = "ch18_s3_architect_visor",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.NATASHA to 6),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.NATASHA, CharacterFlagAxis.AFFECTION, 5)),
                        setFlags = mapOf("s2_romance" to "natasha", "s2_romance_stage" to "3"),
                        branchTag = "Savaşta Mühürlenen Aşk",
                        impactSummary = "Natasha dudaklarından ayrılırken nefes nefese gülümsedi: 'Köfte borcun hâlâ geçerli — şimdi şu tenekeleri bitirelim!'",
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch18_c2_battle_kiss_wanda",
                    label = "Wanda'yı sıkıca kendine çek ve gökyüzünün ortasında onu sıcak, sarsılmaz bir 'geri dön' öpücüğüyle öp: \"Korkma, yanındayım.\"",
                    toneHint = "♥ WANDA SAVAŞ ÖPÜCÜĞÜ • Aşama 3 • Tutkulu Yemin",
                    nextNodeId = "ch18_s3_architect_visor",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 6),
                        characterAxisDeltas = listOf(Triple(BookCharacterId.WANDA, CharacterFlagAxis.AFFECTION, 5)),
                        setFlags = mapOf("s2_romance" to "wanda", "s2_romance_stage" to "3"),
                        branchTag = "Kızıl ve Mavi Yemin",
                        impactSummary = "Wanda'nın bütün korkusu bir anda saf güce dönüştü; alnını öpüp fısıldadı: 'Bu şehri birlikte kurtaracağız, Aiden!'",
                        isRelationship = true,
                        impactLevel = "kritik"
                    )
                ),
                BookChoiceDef(
                    id = "ch18_c2_brotherhood_vow",
                    label = "Steve, Thor ve Pietro'yla yumruk tokuştur: \"Kimse bu meydandan tek başına yürümeyecek — sonuna kadar birlikteyiz!\"",
                    toneHint = "Avengers Kardeşliği • Plâtonik Yol • Savaş Yemini",
                    nextNodeId = "ch18_s3_architect_visor",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.THOR to 4, BookCharacterId.PIETRO to 4),
                        avengersTrustDelta = 4,
                        setFlags = mapOf("ch18_team_vow" to "brotherhood"),
                        impactSummary = "Thor çekicini havaya kaldırdı, Pietro sırıttı: 'Haydi o zaman — tarih yazalım!'"
                    )
                )
            )
        ),

        // ═══════════════════════════════════════════════════════════════════════
        // SAHNE 3: ULTRON'UN ÇATLAYAN GÖZLERİNDE ASIL MİMAR! (Karar 3/4)
        // ═══════════════════════════════════════════════════════════════════════
        BookSceneNode(
            id = "ch18_s3_architect_visor",
            chapterNumber = 20,
            sceneOrder = 3,
            sceneTitle = "Altın-Mavi Gözler — Mimar'ın Meydan Okuması",
            locationTag = "UÇAN SOKOVİA · KİLİSE ÖN MEYDANI • 07:34",
            paragraphs = listOf(
                "Gökten ağır bir şimşek gibi düşen **Vibranyum Ultron Prime**, kilise meydanının taşlarını parçalayarak tam önünde durdu. İki buçuk metrelik parlak gümüş-karbon zırhından duman yükseliyordu. Ama tam Ultron konuşmak için çenesini açtığında, mekanik sesi bir anda dondu, çatladı — ve yüzündeki kızıl optik mercekler titreyerek o tüyler ürpertici **altın hareli buz mavisine** dönüştü!",
                "Meydandaki rüzgâr bir saniyeliğine sustu. Karşındaki metal gövdeden artık Ultron'un öfkeli sesi değil, Afrika gemisinde zamanı donduran **ASIL MİMAR**'ın sakin, ilahi ve dondurucu yankısı yükseldi:",
                "[[COMMS|ASIL MİMAR · ULTRON'UN ZIRHINDAN DOĞRUDAN KONUŞUYOR|Beni şaşırttın, Kayıp Anahtarım. Kendi yara izli geleceğini yendin, zayıf insanlara hâlâ tutunuyorsun. Ama bak etrafına: bu şehir dört kilometre yukarıda, binlerce insan nefesini tutmuş düşüşü bekliyor. Tek bir seçimin var Aiden Blackwood — ya o kırılgan sevginle birlikte gökten düşersin, ya da Kırmızı Göz'ünü açıp benim düzenime boyun eğersin!]]",
                "Ultron'un metal bedeni Mimar'ın devasa bilincini taşımakta zorlanıyordu; omuzlarından mavi-altın kıvılcımlar döküldü. Sonra Mimar'ın sesi alaycı bir vedayla çekildi: \"Göster bana Aiden... insan kalbin demire ve ölüme dayanabilecek mi?\" Mercekler yeniden yakıcı kırmızıya döndü ve Ultron Prime kükreyerek bütün ordusuna saldırı emri verdi!"
            ),
            decisionTitle = "Bölüm XVIII (3/4) — Mimar'ın Sözlerine Yanıtın",
            choicePrompt = "Asıl Mimar bütün ekibin duyduğu anda seni Kırmızı Göz'e ve karanlığa kışkırttı. Meydanı titreten cevabın ne olacak?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch18_c3_reject_architect",
                    label = "Mavi kuantum ışığını iki elinde yak: \"Beni asla kontrol edemeyeceksin Mimar! Bu şehir de, bu aile de ayakta kalacak — demirini kırıp sana geleceğiz!\"",
                    toneHint = "Mavi İrade • Mimar'a Meydan Okuma • Ekip Güveni +",
                    nextNodeId = "ch18_s4_circle_formation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.STEVE to 4, BookCharacterId.VISION to 4, BookCharacterId.WANDA to 3, BookCharacterId.NATASHA to 3),
                        storyAxisDeltas = mapOf(StoryFlagAxis.HUMANITY_ANCHOR to 4),
                        setFlags = mapOf("ch18_architect_answer" to "blue_will"),
                        impactSummary = "Mavi ışığın meydandaki bütün Avengers'a yeni bir soluk verdi — Vision 'Mimar ilk kez yanıldı' dedi."
                    )
                ),
                BookChoiceDef(
                    id = "ch18_c3_tap_red_controlled",
                    label = "Bir gözünü buz mavisi, diğerini kontrollü kızıl yak: \"Kırmızı Göz'ümden korkmuyorum — ama onu senin düzenin için değil, sevdiklerimi korumak için kullanacağım!\"",
                    toneHint = "İkili Güç Dengesi • Yüksek Risk • Korkusuz İrade",
                    nextNodeId = "ch18_s4_circle_formation",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.TONY to 3, BookCharacterId.THOR to 4, BookCharacterId.PIETRO to 3),
                        redEyeDelta = 1,
                        storyAxisDeltas = mapOf(StoryFlagAxis.COSMIC_AWAKENING to 3),
                        setFlags = mapOf("ch18_architect_answer" to "dual_eye"),
                        impactSummary = "Ultron bile bir adım geriledi — hem mavi hem kızıl enerjiyi aynı anda dizginlediğini ilk kez görüyordu!"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch18_s4_circle_formation",
            chapterNumber = 20,
            sceneOrder = 4,
            sceneTitle = "Kilisede Çember",
            locationTag = "UÇAN SOKOVİA · MERKEZ KİLİSE • 07:40",
            paragraphs = listOf(
                "Eski taş kilisenin kubbesi çökmüştü; tam ortada dev Vibranyum anahtar parlıyordu. Eğer tek bir Ultron nöbetçisi bile o düğmeye dokunursa bütün şehir taş gibi Dünya'ya çakılacaktı. Birer birer hepiniz çekirdeğin etrafında efsanevi çemberi kurdunuz: Steve kalkanıyla, Thor Mjolnir'le, Tony yanan iticilerle, Natasha elektro-coplarıyla, Clint gerilmiş yayıyla, Hulk kükreyerek, Wanda kızıl fırtınasıyla, Pietro gümüş şimşeğiyle, Vision altın taşıyla — ve sen, mavi kuantum kıvılcımlarınla o çemberin tam kalbinde!",
                "Thor gülümseyerek dışarıdaki yüzlerce demir askere baktı: \"Elinden gelenin en iyisi bu mu, Ultron?!\" Ve Ultron elini kaldırdı — yüzlerce demir gövde dört koldan kiliseye daldı!"
            ),
            decisionTitle = "Bölüm XVIII (4/4) — Çemberde Pozisyonun",
            choicePrompt = "Tarihin en büyük Avengers savunması başlıyor! Çemberde hangi görevi üstleneceksin?",
            choices = listOf(
                BookChoiceDef(
                    id = "ch18_c4_core_guardian",
                    label = "Wanda ve Vision'la doğrudan çekirdeğin önünü tut — ışınlanarak sızan her nöbetçiyi havada parçalayıp anahtarı koru!",
                    toneHint = "Çekirdek Muhafızı • Wanda & Vision Uyumu",
                    nextNodeId = "ch18_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.WANDA to 3, BookCharacterId.VISION to 3),
                        setFlags = mapOf("ch18_circle_role" to "core"),
                        impactSummary = "Çekirdeğe tek bir metal el bile yaklaşamadı!"
                    )
                ),
                BookChoiceDef(
                    id = "ch18_c4_flank_sweeper",
                    label = "Pietro, Steve ve Natasha'yla dış kemerleri tut — sivillerin son helikopter botlarına binmesi için geniş güvenlik koridoru aç!",
                    toneHint = "Dış Hat Kahramanı • Pietro, Steve & Natasha Uyumu",
                    nextNodeId = "ch18_s5_end",
                    effect = BookChoiceEffect(
                        bondDeltas = mapOf(BookCharacterId.PIETRO to 4, BookCharacterId.STEVE to 3, BookCharacterId.NATASHA to 3),
                        setFlags = mapOf("ch18_circle_role" to "outer"),
                        impactSummary = "Dış hat sarsılmaz bir duvara dönüştü — son sivil aileler kurtarma botlarına koşmaya başladı!"
                    )
                )
            )
        ),

        BookSceneNode(
            id = "ch18_s5_end",
            chapterNumber = 20,
            sceneOrder = 5,
            sceneTitle = "Demir Yağmuru",
            locationTag = "UÇAN SOKOVİA · MERKEZ KİLİSE • 07:45",
            paragraphs = listOf(
                "Kilise kemerleri plazma ateşi ve şimşeklerle sarsıldı. Her saniye onlarca nöbetçi parçalanıyordu ama gökyüzünde bir Quinjet'in kaçırıldığını ve namlularının tam tahliye meydanına — Clint Barton'ın kucağındaki küçük bir çocuğa ve Pietro Maximoff'a — çevrildiğini henüz kimse bilmiyordu."
            ),
            isChapterEnd = true,
            chapterEndQuote = "“Bir saniye bazen bütün bir hayatı — ya da sonsuz bir pişmanlığı — ayırır.”"
        )
    ).associateBy { it.id }
}
