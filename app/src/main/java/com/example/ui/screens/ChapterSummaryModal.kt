package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.book.BookCharacterId
import com.example.book.BookReadState
import com.example.book.BookStateRepository
import com.example.book.CosmicDriftBookRegistry
import com.example.book.RecordedDecision
import com.example.data.local.MessageEntity
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.cosmicStaggeredEnter
import com.example.ui.animation.cosmicTapSpring
import com.example.ui.components.MorCamButtonVariant
import com.example.ui.components.MorCamCapsuleButton
import com.example.ui.components.MorCamDivider
import com.example.ui.components.MorCamTokens
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import com.example.util.BookAudioController
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private data class MeaningfulImpactLine(
    val text: String,
    val tint: Color,
    val symbol: String,
    val isNewFlag: Boolean = false
)

private data class Lis2DecisionSlide(
    val index: Int,
    val nodeId: String,
    val title: String,
    val selectedOption: String,
    val impactText: String,
    val isBranching: Boolean,
    val isRelationship: Boolean,
    val secretUnlocked: String?,
    val branchTag: String?,
    @DrawableRes val artworkResId: Int?,
    val meaningfulImpacts: List<MeaningfulImpactLine>
)

/**
 * Resolves choice-specific compressed KB-sized WebP artwork for Chapter 1's
 * Life is Strange 2-style Chapter End Screen, matched directly to the meaning
 * and characters of the exact choice the player made (with consistent Aidem design).
 */
@DrawableRes
private fun resolveChapterChoiceArtwork(
    chapterNumber: Int,
    choiceId: String,
    nodeId: String,
    orderIndex: Int
): Int? {
    if (chapterNumber != 1) return null
    return when (choiceId) {
        "ch1_c1_trace" -> R.drawable.img_ch1_c1_vision_trace
        "ch1_c1_distant", "ch1_c1_deep" -> R.drawable.img_ch1_c1_vision_calm
        "ch1_c2_direct" -> R.drawable.img_ch1_c2_direct_rift
        "ch1_c2_observe" -> R.drawable.img_ch1_c2_stealth_terrace
        "ch1_c2_retreat" -> R.drawable.img_ch1_c2_forced_rift
        "ch1_c3_cold", "ch1_c3_witty", "ch1_c3_silent" -> R.drawable.img_ch1_c3_stark_faceoff
        "ch1_c4_natasha" -> R.drawable.img_ch1_c4_natasha_gaze
        "ch1_c4_wanda" -> R.drawable.img_ch1_c4_wanda_gaze
        "ch1_c4_neutral" -> R.drawable.img_ch1_c3_stark_faceoff
        "ch1_c5_accept", "ch1_c5_probe", "ch1_c5_guarded" -> R.drawable.img_ch1_c5_tower_night
        else -> when {
            nodeId.startsWith("ch1_s1") || orderIndex == 1 -> R.drawable.img_ch1_c1_vision_calm
            nodeId.startsWith("ch1_s2") || orderIndex == 2 -> R.drawable.img_ch1_c2_direct_rift
            nodeId.startsWith("ch1_s3") || orderIndex == 3 -> R.drawable.img_ch1_c3_stark_faceoff
            nodeId.startsWith("ch1_s4") || orderIndex == 4 -> R.drawable.img_ch1_c4_wanda_gaze
            else -> R.drawable.img_ch1_c5_tower_night
        }
    }
}

/**
 * Formats every decision's concrete multi-axis flag shifts in crisp Turkish
 * (e.g. "Steve'in güveni +3 arttı", "Natasha'nın saygısı +3 arttı", "S.H.I.E.L.D. kuşkusu +2 arttı")
 * with small, balanced increments and "YENİ" highlights when new flags unlock.
 */
private fun buildMeaningfulImpactLines(rec: RecordedDecision): List<MeaningfulImpactLine> = buildList {
    if (rec.branchTag != null) {
        add(MeaningfulImpactLine("Hikâye Yolu: ${rec.branchTag}", Color(0xFFFEF08A), "⑂"))
    }
    if (rec.secretUnlocked != null) {
        add(MeaningfulImpactLine("Gizli İz: ${rec.secretUnlocked}", Color(0xFFFDE047), "✦"))
    }

    if (rec.flagChanges.isNotEmpty()) {
        rec.flagChanges.forEach { change ->
            val symbol = when {
                change.characterId != null && change.delta > 0 -> "♥"
                change.delta < 0 -> "▾"
                else -> "✦"
            }
            val tint = if (change.delta < 0) Color(0xFFFCA5A5) else Color(change.accentHex)
            add(
                MeaningfulImpactLine(
                    text = change.formatTurkish(),
                    tint = tint,
                    symbol = symbol,
                    isNewFlag = change.isNewlyUnlocked
                )
            )
        }
    } else {
        // Fallback for any pre-upgrade saved decision records
        rec.bondDeltas.entries
            .sortedByDescending { kotlin.math.abs(it.value) }
            .forEach { (charId, raw) ->
                if (raw == 0) return@forEach
                val capped = raw.coerceIn(-4, 4)
                val phrase = if (capped > 0) {
                    "${charId.possessiveName} güveni +$capped arttı"
                } else {
                    "${charId.possessiveName} güveni $capped azaldı"
                }
                add(
                    MeaningfulImpactLine(
                        text = phrase,
                        tint = if (capped > 0) Color(charId.accentHex) else Color(0xFFFCA5A5),
                        symbol = if (capped > 0) "♥" else "▾"
                    )
                )
            }
        if (rec.avengersTrustDelta != 0) {
            val capped = rec.avengersTrustDelta.coerceIn(-3, 3)
            val txt = if (capped > 0) "Ekip güveni +$capped arttı" else "Ekip güveni $capped azaldı"
            add(MeaningfulImpactLine(txt, if (capped > 0) Color(0xFF93C5FD) else Color(0xFFFCA5A5), "◈"))
        }
        if (rec.redEyeDelta > 0) {
            add(MeaningfulImpactLine("Kırmızı Göz dengesizliği +${rec.redEyeDelta.coerceAtMost(2)} arttı", Color(0xFFF87171), "◈"))
        }
        if (rec.fatigueDelta > 0) {
            add(MeaningfulImpactLine("Zihinsel yorgunluk +${rec.fatigueDelta.coerceAtMost(2)} arttı", Color(0xFFFBBF24), "◈"))
        }
        if (rec.mysteryDelta > 0) {
            add(MeaningfulImpactLine("Gizem perdesi +${rec.mysteryDelta.coerceAtMost(2)} arttı", Color(0xFFC4B5FD), "◈"))
        }
    }
}

/**
 * Cinematic Life is Strange 2-Style Full-Screen Chapter End Screen.
 *
 * Features:
 * - Launched when the player taps "Bölümü Bitir ✦" at the bottom of a completed chapter.
 * - Starts chapter end soundtrack (`R.raw.kozmiksuruklenismuzik` — "Kozmiksürüklenismüzik")
 *   fading from 0% to 100% volume over 1.5s when "Bölümü Bitir" is tapped.
 * - Living animated Ken Burns parallax background artwork per choice (Chapter 1 bespoke KB-sized WebP art)
 *   with smooth 1s crossfade, rain/stardust embers, and velvet cinema vignette.
 * - Unhurried Life is Strange 2 pacing:
 *   1. Opening Curtain (~2.2s)
 *   2. Sequential Decision Showcase (~8.6s per decision: 2.8s choice focus -> 5.8s consequence unfold),
 *      with Pause/Resume, Previous, Next, and "Tümünü Gör" controls.
 *   3. Final All-Choices Overview with top bar: [Çıkış] — [Bölüm İsmi] — [Sonraki Bölüm / Tekrar].
 * - Changing menus immediately triggers a smooth 2-second music fade-out (`fadeOutMs = 2000L`).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChapterSummaryModal(
    messages: List<MessageEntity>,
    bookReadState: BookReadState? = null,
    onDismiss: () -> Unit,
    onExitToBooks: () -> Unit = onDismiss,
    onRestartChapter: () -> Unit,
    onNextChapter: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val motionConfig = LocalCosmicMotionConfig.current

    val completedChapterNumber = remember(bookReadState, messages) {
        if (bookReadState != null) {
            bookReadState.activeChapter
        } else {
            val txt = messages.lastOrNull { it.role == "assistant" || it.role == "model" }?.text ?: ""
            when {
                txt.contains("BÖLÜM 7") -> 7
                txt.contains("BÖLÜM 6") -> 6
                txt.contains("BÖLÜM 5") -> 5
                txt.contains("BÖLÜM 4") -> 4
                txt.contains("BÖLÜM 3") -> 3
                txt.contains("BÖLÜM 2") -> 2
                else -> 1
            }
        }
    }

    val chapterMeta = remember(completedChapterNumber) {
        CosmicDriftBookRegistry.chapterMeta(completedChapterNumber)
    }
    val nextChapterMeta = remember(completedChapterNumber) {
        if (completedChapterNumber < CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS) {
            CosmicDriftBookRegistry.chapterMeta(completedChapterNumber + 1)
        } else {
            null
        }
    }
    val accentColor = Color(chapterMeta.accentHex)

    val slides: List<Lis2DecisionSlide> = remember(bookReadState, messages, completedChapterNumber) {
        if (bookReadState != null) {
            bookReadState.currentChapterDecisions.mapIndexed { idx, rec ->
                Lis2DecisionSlide(
                    index = idx + 1,
                    nodeId = rec.nodeId,
                    title = rec.decisionTitle,
                    selectedOption = rec.selectedOption,
                    impactText = rec.impactText,
                    isBranching = rec.isBranching,
                    isRelationship = rec.isRelationship,
                    secretUnlocked = rec.secretUnlocked,
                    branchTag = rec.branchTag,
                    artworkResId = resolveChapterChoiceArtwork(
                        chapterNumber = completedChapterNumber,
                        choiceId = rec.choiceId,
                        nodeId = rec.nodeId,
                        orderIndex = idx + 1
                    ),
                    meaningfulImpacts = buildMeaningfulImpactLines(rec)
                )
            }
        } else {
            messages.filter { it.role == "user" }.takeLast(6).mapIndexed { idx, msg ->
                Lis2DecisionSlide(
                    index = idx + 1,
                    nodeId = "custom_${idx + 1}",
                    title = "${idx + 1}. Karar",
                    selectedOption = msg.text,
                    impactText = "Bu karar hikâyenin gidişatını ve karakterlerin sana olan tutumunu şekillendirdi.",
                    isBranching = false,
                    isRelationship = true,
                    secretUnlocked = null,
                    branchTag = null,
                    artworkResId = resolveChapterChoiceArtwork(
                        chapterNumber = completedChapterNumber,
                        choiceId = "",
                        nodeId = "",
                        orderIndex = idx + 1
                    ),
                    meaningfulImpacts = emptyList()
                )
            }
        }
    }

    // Modes: PROMPT (brief 2.2s cinematic intro curtain) -> SEQUENTIAL (slow LiS2 showcase) -> FINAL_GRID (all choices)
    var viewMode by remember {
        mutableStateOf(
            if (slides.isNotEmpty()) SummaryViewMode.PROMPT else SummaryViewMode.FINAL_GRID
        )
    }
    var currentIndex by remember { mutableIntStateOf(0) }
    var lastAnimatedSlideIndex by remember { mutableIntStateOf(-1) }
    var stepPhase by remember { mutableStateOf(StepPhase.CARD_ONLY) }
    var isPaused by remember { mutableStateOf(false) }

    // Smooth auto-progress bar for each sequential slide (resumes cleanly after pause)
    val slideProgress = remember { Animatable(0f) }

    val isMusicPlaying by BookAudioController.isPlayingFlow.collectAsState()

    // Ensure "Kozmiksürüklenismüzik" ALWAYS starts automatically ON from 0 -> 100% in 1.5s
    LaunchedEffect(Unit) {
        BookAudioController.play(
            context = context,
            rawResId = R.raw.kozmiksuruklenismuzik,
            loop = true,
            volume = 1.0f,
            fadeInMs = 1500L,
            autoUnmute = true
        )
    }

    val leaveMenuWith2sFade = remember {
        { action: () -> Unit ->
            BookAudioController.stop(fadeOutMs = 2000L)
            action()
        }
    }

    BackHandler {
        leaveMenuWith2sFade(onDismiss)
    }

    // Brief atmospheric opening curtain before Choice 1 appears
    LaunchedEffect(viewMode) {
        if (viewMode == SummaryViewMode.PROMPT) {
            lastAnimatedSlideIndex = -1
            delay(2200)
            if (viewMode == SummaryViewMode.PROMPT) {
                currentIndex = 0
                stepPhase = StepPhase.CARD_ONLY
                slideProgress.snapTo(0f)
                viewMode = SummaryViewMode.SEQUENTIAL
            }
        } else if (viewMode != SummaryViewMode.SEQUENTIAL) {
            lastAnimatedSlideIndex = -1
        }
    }

    // Unhurried Life is Strange 2 Sequential Showcase Driver (~8.6 seconds per choice, pausable!)
    LaunchedEffect(viewMode, currentIndex, isPaused, slides.size) {
        if (viewMode != SummaryViewMode.SEQUENTIAL || slides.isEmpty()) return@LaunchedEffect
        if (lastAnimatedSlideIndex != currentIndex) {
            lastAnimatedSlideIndex = currentIndex
            slideProgress.snapTo(0f)
            stepPhase = StepPhase.CARD_ONLY
        }
        if (isPaused) return@LaunchedEffect

        val choiceOnlyEnd = 0.33f
        val choiceDurationMs = 2800
        val impactDurationMs = 5800

        // Stage A: Choice Moment (2.8s total)
        if (slideProgress.value < choiceOnlyEnd) {
            stepPhase = StepPhase.CARD_ONLY
            val remainingA = ((choiceOnlyEnd - slideProgress.value) / choiceOnlyEnd * choiceDurationMs)
                .toInt()
                .coerceAtLeast(120)
            slideProgress.animateTo(
                targetValue = choiceOnlyEnd,
                animationSpec = tween(durationMillis = remainingA, easing = LinearEasing)
            )
        }

        // Stage B: Unfold "Neye Etki Etti?" & meaningful impact lines (5.8s total)
        stepPhase = StepPhase.RESULT_REVEALED
        if (slideProgress.value < 1f) {
            val remainingB = ((1f - slideProgress.value) / (1f - choiceOnlyEnd) * impactDurationMs)
                .toInt()
                .coerceAtLeast(120)
            slideProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = remainingB, easing = LinearEasing)
            )
        }

        // Gentle breathing pause before smoothly transitioning to next decision or overview
        delay(260)
        if (!isPaused && viewMode == SummaryViewMode.SEQUENTIAL) {
            if (currentIndex < slides.lastIndex) {
                slideProgress.snapTo(0f)
                stepPhase = StepPhase.CARD_ONLY
                currentIndex += 1
            } else {
                viewMode = SummaryViewMode.FINAL_GRID
            }
        }
    }

    // Determine active background artwork for Living Ken Burns Canvas
    @DrawableRes
    val activeBackdropArtRes: Int? = remember(viewMode, currentIndex, slides, completedChapterNumber) {
        when (viewMode) {
            SummaryViewMode.PROMPT -> slides.firstOrNull()?.artworkResId
            SummaryViewMode.SEQUENTIAL -> slides.getOrNull(currentIndex)?.artworkResId
            SummaryViewMode.FINAL_GRID -> slides.lastOrNull()?.artworkResId
        }
    }

    // Dynamic qualitative narrative reflection for the Overview header (no raw arcade "+11" spam)
    val relationshipReflection = remember(bookReadState, completedChapterNumber) {
        if (bookReadState == null) {
            "Verdiğin her karar bu bölümün tonunu ve geleceğini sessizce yeniden yazdı."
        } else {
            val topChar = BookCharacterId.entries.maxByOrNull { bookReadState.bondScore(it) }
                ?: BookCharacterId.NATASHA
            val branchCount = bookReadState.currentChapterDecisions.count { it.isBranching }
            val secretCount = bookReadState.currentChapterDecisions.count { it.secretUnlocked != null }
            val bondSentence = when (topChar) {
                BookCharacterId.NATASHA -> "Bu bölümde en derin sessiz anlayış Natasha Romanoff ile aranda kuruldu."
                BookCharacterId.WANDA -> "Wanda Maximoff duvarlarının ardındaki gerçeği en derinden hisseden kişi oldu."
                BookCharacterId.STEVE -> "Steve Rogers'ın sana duyduğu saha güveni belirgin biçimde sağlamlaştı."
                BookCharacterId.TONY -> "Tony Stark hem zekân hem de kozmik potansiyelin karşısında derin bir merak duyuyor."
                BookCharacterId.CLINT -> "Clint Barton'ın samimiyeti ve keskin gözü Kule'de ilk kez güven duygusu verdi."
                BookCharacterId.BRUCE -> "Bruce Banner kontrol mücadeleni herkesten daha yakından anladı."
                BookCharacterId.THOR -> "Thor Odinson iradene ve kozmik gücüne içten bir saygı duydu."
            }
            val extra = buildList {
                if (branchCount > 0) add("$branchCount kritik yol ayrımına girdin")
                if (secretCount > 0) add("$secretCount gizli iz açığa çıkardın")
            }.joinToString(" ve ")
            if (extra.isNotBlank()) "$bondSentence Ayrıca $extra." else bondSentence
        }
    }

    // Continuous subtle motion loops for Ken Burns breathing, parallax drift & stardust
    val kenBurnsPhase: Float
    val ambientDrift: Float
    val pulseGlow: Float
    if (motionConfig.enableAmbientLoops) {
        val inf = rememberInfiniteTransition(label = "lis2LivingCanvas")
        kenBurnsPhase = inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(15000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "kenBurnsZoom"
        ).value
        ambientDrift = inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing)),
            label = "lis2Stardust"
        ).value
        pulseGlow = inf.animateFloat(
            initialValue = 0.72f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
            label = "lis2Pulse"
        ).value
    } else {
        kenBurnsPhase = 0.35f
        ambientDrift = 0f
        pulseGlow = 0.85f
    }

    val noPadTextStyle = remember {
        TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(60f)
            .background(Color(0xFF070212))
            .testTag("chapter_end_screen_root")
    ) {
        // ── LAYER 1: LIVING ANIMATED CHOICE-SPECIFIC BACKGROUND ARTWORK ─────
        Crossfade(
            targetState = activeBackdropArtRes,
            animationSpec = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
            label = "lis2ArtworkCrossfade"
        ) { artRes ->
            if (artRes != null) {
                val scale = 1.03f + kenBurnsPhase * 0.09f
                val driftX = sin(ambientDrift * 6.283f) * 18f
                val driftY = cos(ambientDrift * 6.283f) * 12f
                Image(
                    painter = painterResource(id = artRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = driftX
                            translationY = driftY
                        }
                )
            } else {
                // Atmospheric fallback for Chapters 2–7
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF15072C),
                                    Color(0xFF090316),
                                    Color(0xFF0F0524),
                                    Color(0xFF190934)
                                )
                            )
                        )
                )
            }
        }

        // ── LAYER 2: CINEMATIC LIFE IS STRANGE VIGNETTE & READABILITY SCRIM ─
        val scrimDarkness = if (viewMode == SummaryViewMode.FINAL_GRID) 0.78f else 0.56f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xFF090316).copy(alpha = (scrimDarkness + 0.18f).coerceAtMost(0.92f)),
                            0.24f to Color(0xFF0D0520).copy(alpha = (scrimDarkness - 0.18f).coerceAtLeast(0.28f)),
                            0.52f to Color(0xFF0A0318).copy(alpha = scrimDarkness),
                            0.82f to Color(0xFF080214).copy(alpha = (scrimDarkness + 0.22f).coerceAtMost(0.94f)),
                            1.00f to Color(0xFF060210).copy(alpha = 0.96f)
                        )
                    )
                )
        )

        // ── LAYER 3: SUBTLE LIVING STARDUST & LIGHT RAIN SHIMMER OVERLAY ────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor.copy(alpha = 0.16f * pulseGlow), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.22f),
                    radius = size.width * 0.85f
                ),
                radius = size.width * 0.85f,
                center = Offset(size.width * 0.5f, size.height * 0.22f)
            )
            if (motionConfig.enableAmbientLoops) {
                for (i in 0 until 24) {
                    val fx = ((i * 71 + 13) % 100) / 100f
                    val fyBase = ((i * 43 + 17) % 100) / 100f
                    val fy = (fyBase - ambientDrift * (0.24f + (i % 3) * 0.09f) + 1f) % 1f
                    val sway = sin(ambientDrift * 6.283f + i * 0.9f) * 14f
                    val alpha = (0.14f + (i % 4) * 0.06f) * pulseGlow
                    drawCircle(
                        color = if (i % 3 == 0) accentColor.copy(alpha = alpha) else Color(0xFFF5EEFF).copy(alpha = alpha),
                        radius = (1.1f + (i % 3) * 0.6f).dp.toPx(),
                        center = Offset(size.width * fx + sway, size.height * fy)
                    )
                }
            }
        }

        // ── LAYER 4: FOREGROUND EDITORIAL UI ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── TOP BAR: [Çıkış]  —  [Bölüm İsmi]  —  [Sonraki Bölüm / Tümünü Gör] ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val exitInteraction = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .cosmicTapSpring(exitInteraction, pressedScale = 0.94f)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x991C0C3A))
                        .border(0.95.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(50))
                        .clickable(interactionSource = exitInteraction, indication = null) {
                            leaveMenuWith2sFade(onExitToBooks)
                        }
                        .padding(horizontal = 12.dp)
                        .testTag("chapter_end_exit_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Çıkış",
                        tint = MorCamTokens.SilverLavenderBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Çıkış",
                        style = noPadTextStyle,
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.SilverLavenderBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Center Chapter Name (clean, centered, zero font padding clipping)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = accentColor.copy(alpha = 0.9f),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "BÖLÜM ${chapterMeta.romanNumeral} SONU",
                            style = noPadTextStyle,
                            fontFamily = InterFontFamily,
                            color = accentColor,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = chapterMeta.title,
                        style = noPadTextStyle,
                        fontFamily = DisplaySerifFontFamily,
                        color = Color(0xFFFFFFFF),
                        fontSize = 16.5.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                // Music On/Off Toggle Orb (lives right on the Chapter End Screen)
                val musicToggleInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .cosmicTapSpring(musicToggleInteraction, pressedScale = 0.92f)
                        .then(
                            if (isMusicPlaying) {
                                Modifier.morCamCosmicGlow(
                                    cornerRadius = 20.dp,
                                    glowColor = accentColor,
                                    alpha = 0.42f * pulseGlow,
                                    blurDp = 9.dp
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clip(CircleShape)
                        .background(if (isMusicPlaying) Color(0xB82A1256) else Color(0x8A170A32))
                        .border(
                            width = if (isMusicPlaying) 1.1.dp else 0.9.dp,
                            brush = if (isMusicPlaying) MorCamTokens.RimGradient else MorCamTokens.SubtleRimGradient,
                            shape = CircleShape
                        )
                        .clickable(interactionSource = musicToggleInteraction, indication = null) {
                            BookAudioController.toggleMusicInChapterEnd(
                                context = context,
                                rawResId = R.raw.kozmiksuruklenismuzik,
                                fullVolume = 1.0f
                            )
                        }
                        .testTag("chapter_end_music_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMusicPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                        contentDescription = if (isMusicPlaying) "Müziği Kapat" else "Müziği Aç",
                        tint = if (isMusicPlaying) Color(0xFFF3E8FF) else MorCamTokens.LavenderMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Right Top Bar Action
                if (viewMode != SummaryViewMode.FINAL_GRID) {
                    val skipInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .cosmicTapSpring(skipInteraction, pressedScale = 0.94f)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x881F0E42))
                            .border(0.95.dp, Color(0x88D8B4FE), RoundedCornerShape(50))
                            .clickable(interactionSource = skipInteraction, indication = null) {
                                viewMode = SummaryViewMode.FINAL_GRID
                            }
                            .padding(horizontal = 11.dp)
                            .testTag("chapter_end_skip_sequence_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tümünü Gör",
                            style = noPadTextStyle,
                            fontFamily = InterFontFamily,
                            color = Color(0xFFEDE6FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (onNextChapter != null && nextChapterMeta != null) {
                    val nextInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .cosmicTapSpring(nextInteraction, pressedScale = 0.94f)
                            .morCamCosmicGlow(cornerRadius = 20.dp, glowColor = accentColor, alpha = 0.48f * pulseGlow, blurDp = 10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MorCamTokens.PrimaryButtonFill)
                            .border(1.1.dp, MorCamTokens.RimGradient, RoundedCornerShape(50))
                            .clickable(interactionSource = nextInteraction, indication = null) {
                                leaveMenuWith2sFade(onNextChapter)
                            }
                            .padding(horizontal = 11.dp)
                            .testTag("chapter_end_top_next_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Bölüm ${nextChapterMeta.number}",
                            style = noPadTextStyle,
                            fontFamily = InterFontFamily,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Sonraki Bölüm",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                } else {
                    val replayInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .cosmicTapSpring(replayInteraction, pressedScale = 0.94f)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x9926124E))
                            .border(1.dp, MorCamTokens.RimGradient, RoundedCornerShape(50))
                            .clickable(interactionSource = replayInteraction, indication = null) {
                                leaveMenuWith2sFade(onRestartChapter)
                            }
                            .padding(horizontal = 10.dp)
                            .testTag("chapter_end_top_replay_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Tekrar",
                            tint = MorCamTokens.SilverLavenderBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Tekrar",
                            style = noPadTextStyle,
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtle Soundtrack Status Pill displaying "Kozmiksürüklenismüzik"
            val trackPillInteraction = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier
                    .cosmicTapSpring(trackPillInteraction, pressedScale = 0.96f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isMusicPlaying) Color(0x70210E46) else Color(0x5514082C))
                    .border(
                        width = 0.75.dp,
                        color = if (isMusicPlaying) accentColor.copy(alpha = 0.45f) else Color(0x40A78BFA),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable(interactionSource = trackPillInteraction, indication = null) {
                        BookAudioController.toggleMusicInChapterEnd(
                            context = context,
                            rawResId = R.raw.kozmiksuruklenismuzik,
                            fullVolume = 1.0f
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("chapter_end_track_pill"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = if (isMusicPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                    contentDescription = null,
                    tint = if (isMusicPlaying) accentColor else MorCamTokens.LavenderMuted,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (isMusicPlaying) {
                        "${BookAudioController.TRACK_DISPLAY_NAME}  ·  Açık"
                    } else {
                        "${BookAudioController.TRACK_DISPLAY_NAME}  ·  Kapalı"
                    },
                    style = noPadTextStyle,
                    fontFamily = InterFontFamily,
                    color = if (isMusicPlaying) Color(0xFFE9DDFF) else MorCamTokens.LavenderMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── STAGE BODY ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        (fadeIn(tween(650)) + scaleIn(initialScale = 0.97f, animationSpec = tween(650))) togetherWith
                            fadeOut(tween(420))
                    },
                    label = "lis2PhaseTransition"
                ) { mode ->
                    when (mode) {
                        // ── INTRO CURTAIN (2.2s cinematic breathing beat) ───
                        SummaryViewMode.PROMPT -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        currentIndex = 0
                                        stepPhase = StepPhase.CARD_ONLY
                                        viewMode = SummaryViewMode.SEQUENTIAL
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "BÖLÜM ${chapterMeta.romanNumeral} TAMAMLANDI",
                                    style = noPadTextStyle,
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFFEF08A),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.4.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = chapterMeta.title,
                                    fontFamily = DisplaySerifFontFamily,
                                    color = Color.White,
                                    fontSize = 34.sp,
                                    lineHeight = 40.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .width(86.dp)
                                        .height(1.2.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color.Transparent, accentColor, Color.Transparent)
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Verdiğin kararlar yolunu çizdi...",
                                    fontFamily = LiterarySerifFontFamily,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFFE6DCFF),
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // ── PHASE 1: LIFE IS STRANGE 2 SEQUENTIAL CHOICE SHOWCASE ──
                        SummaryViewMode.SEQUENTIAL -> {
                            val activeSlide = slides.getOrNull(currentIndex)
                            if (activeSlide != null) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Top Segmented Story Progress Bars (Life is Strange / Cinema style)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        slides.indices.forEach { idx ->
                                            val fillFraction = when {
                                                idx < currentIndex -> 1f
                                                idx == currentIndex -> slideProgress.value.coerceIn(0f, 1f)
                                                else -> 0f
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(3.2.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(Color(0x45FFFFFF))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(fillFraction)
                                                        .fillMaxHeight()
                                                        .clip(RoundedCornerShape(50))
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                listOf(accentColor, Color(0xFFFAF6FF))
                                                            )
                                                        )
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Keep top ~26% open so the living choice artwork breathes cinematically!
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        AnimatedContent(
                                            targetState = activeSlide,
                                            transitionSpec = {
                                                (fadeIn(tween(620)) + slideInVertically(
                                                    initialOffsetY = { it / 8 },
                                                    animationSpec = tween(620)
                                                )) togetherWith fadeOut(tween(340))
                                            },
                                            label = "lis2SlideCard"
                                        ) { card ->
                                            val badgeColor = when {
                                                card.isBranching -> Color(0xFFFEF08A)
                                                card.secretUnlocked != null -> Color(0xFFFDE047)
                                                card.isRelationship -> Color(0xFFF9A8D4)
                                                else -> accentColor
                                            }
                                            val badgeLabel = when {
                                                card.isBranching -> "KRİTİK YOL AYRIMI"
                                                card.secretUnlocked != null -> "GİZLİ İZ AÇILDI"
                                                card.isRelationship -> "BAĞ & İLİŞKİ ANI"
                                                else -> "KADER SEÇİMİ"
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .verticalScroll(rememberScrollState()),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                // Editorial Decision Header floating softly over the artwork
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 4.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = "0${card.index} / 0${slides.size}",
                                                            style = noPadTextStyle,
                                                            fontFamily = DisplaySerifFontFamily,
                                                            color = Color(0xFFE9D5FF),
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            letterSpacing = 1.4.sp
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(badgeColor)
                                                        )
                                                        Row(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(50))
                                                                .background(Color(0x8815082E))
                                                                .border(0.85.dp, badgeColor.copy(alpha = 0.65f), RoundedCornerShape(50))
                                                                .padding(horizontal = 9.dp, vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = when {
                                                                    card.isBranching -> Icons.Default.Route
                                                                    card.isRelationship -> Icons.Default.Favorite
                                                                    else -> Icons.Default.AutoAwesome
                                                                },
                                                                contentDescription = null,
                                                                tint = badgeColor,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Text(
                                                                text = badgeLabel,
                                                                style = noPadTextStyle,
                                                                fontFamily = InterFontFamily,
                                                                color = badgeColor,
                                                                fontSize = 9.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                letterSpacing = 0.9.sp
                                                            )
                                                        }
                                                    }

                                                    Text(
                                                        text = card.title,
                                                        fontFamily = DisplaySerifFontFamily,
                                                        color = Color(0xFFFFFFFF),
                                                        fontSize = 26.sp,
                                                        lineHeight = 31.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }

                                                // Translucent Frosted Mor Cam Narrative Card
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .morCamCosmicGlow(
                                                            cornerRadius = 26.dp,
                                                            glowColor = badgeColor,
                                                            alpha = 0.34f * pulseGlow,
                                                            blurDp = 18.dp
                                                        )
                                                        .clip(RoundedCornerShape(26.dp))
                                                        .background(
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    Color(0xCC1E0C42),
                                                                    Color(0xDD100526),
                                                                    Color(0xD9160832)
                                                                )
                                                            )
                                                        )
                                                        .border(
                                                            width = 1.2.dp,
                                                            brush = Brush.linearGradient(
                                                                listOf(
                                                                    Color(0xDDFAF5FF),
                                                                    badgeColor.copy(alpha = 0.8f),
                                                                    Color(0x888B5CF6)
                                                                )
                                                            ),
                                                            shape = RoundedCornerShape(26.dp)
                                                        )
                                                        .padding(horizontal = 20.dp, vertical = 20.dp)
                                                        .testTag("lis2_active_decision_card")
                                                ) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                                                        Text(
                                                            text = "SENİN SEÇİMİN",
                                                            style = noPadTextStyle,
                                                            fontFamily = InterFontFamily,
                                                            color = MorCamTokens.LavenderSoft,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            letterSpacing = 1.4.sp
                                                        )

                                                        Text(
                                                            text = "“${card.selectedOption}”",
                                                            fontFamily = LiterarySerifFontFamily,
                                                            color = Color(0xFFFFFFFF),
                                                            fontSize = 16.5.sp,
                                                            lineHeight = 24.5.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )

                                                        // Stage B: Smoothly unfolds the narrative meaning & consequences
                                                        AnimatedVisibility(
                                                            visible = stepPhase == StepPhase.RESULT_REVEALED,
                                                            enter = expandVertically(
                                                                animationSpec = spring(
                                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                                    stiffness = Spring.StiffnessLow
                                                                )
                                                            ) + fadeIn(tween(550))
                                                        ) {
                                                            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                                                                MorCamDivider()

                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.AutoAwesome,
                                                                        contentDescription = null,
                                                                        tint = Color(0xFFFEF08A),
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                    Text(
                                                                        text = "NEYE ETKİ ETTİ?",
                                                                        style = noPadTextStyle,
                                                                        fontFamily = InterFontFamily,
                                                                        color = Color(0xFFFEF08A),
                                                                        fontSize = 10.5.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        letterSpacing = 1.3.sp
                                                                    )
                                                                }

                                                                Text(
                                                                    text = card.impactText,
                                                                    fontFamily = LiterarySerifFontFamily,
                                                                    fontStyle = FontStyle.Italic,
                                                                    color = Color(0xFFF0E8FF),
                                                                    fontSize = 15.sp,
                                                                    lineHeight = 23.sp
                                                                )

                                                                // Concrete balanced flag shifts ("Steve'in güveni +3 arttı" etc.)
                                                                if (card.meaningfulImpacts.isNotEmpty()) {
                                                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                        card.meaningfulImpacts.forEach { line ->
                                                                            Row(
                                                                                modifier = Modifier
                                                                                    .fillMaxWidth()
                                                                                    .clip(RoundedCornerShape(12.dp))
                                                                                    .background(line.tint.copy(alpha = 0.14f))
                                                                                    .border(
                                                                                        0.8.dp,
                                                                                        line.tint.copy(alpha = 0.45f),
                                                                                        RoundedCornerShape(12.dp)
                                                                                    )
                                                                                    .padding(horizontal = 11.dp, vertical = 7.dp),
                                                                                verticalAlignment = Alignment.CenterVertically,
                                                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                            ) {
                                                                                Text(
                                                                                    text = line.symbol,
                                                                                    style = noPadTextStyle,
                                                                                    color = line.tint,
                                                                                    fontSize = 12.sp,
                                                                                    fontWeight = FontWeight.Bold
                                                                                )
                                                                                Text(
                                                                                    text = line.text,
                                                                                    fontFamily = InterFontFamily,
                                                                                    color = Color(0xFFFAF6FF),
                                                                                    fontSize = 12.2.sp,
                                                                                    lineHeight = 16.5.sp,
                                                                                    fontWeight = FontWeight.SemiBold,
                                                                                    modifier = Modifier.weight(1f)
                                                                                )
                                                                                if (line.isNewFlag) {
                                                                                    Box(
                                                                                        modifier = Modifier
                                                                                            .clip(RoundedCornerShape(50))
                                                                                            .background(Color(0x38FEF08A))
                                                                                            .border(0.7.dp, Color(0x99FEF08A), RoundedCornerShape(50))
                                                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                                    ) {
                                                                                        Text(
                                                                                            text = "YENİ",
                                                                                            style = noPadTextStyle,
                                                                                            fontFamily = InterFontFamily,
                                                                                            color = Color(0xFFFEF08A),
                                                                                            fontSize = 8.5.sp,
                                                                                            fontWeight = FontWeight.Bold,
                                                                                            letterSpacing = 0.8.sp
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Calm Playback Controls: [←]  [Duraklat / Devam]  [Sonraki →]
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Previous Choice Button
                                        val prevInteraction = remember { MutableInteractionSource() }
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .cosmicTapSpring(prevInteraction, pressedScale = 0.9f)
                                                .clip(CircleShape)
                                                .background(
                                                    if (currentIndex > 0) Color(0x99231048) else Color(0x40160A2E)
                                                )
                                                .border(
                                                    0.95.dp,
                                                    if (currentIndex > 0) Color(0x88D8B4FE) else Color(0x33D8B4FE),
                                                    CircleShape
                                                )
                                                .clickable(
                                                    enabled = currentIndex > 0,
                                                    interactionSource = prevInteraction,
                                                    indication = null
                                                ) {
                                                    isPaused = false
                                                    stepPhase = StepPhase.CARD_ONLY
                                                    currentIndex -= 1
                                                }
                                                .testTag("lis2_prev_decision_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Önceki Karar",
                                                tint = if (currentIndex > 0) Color.White else Color(0x55FFFFFF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Pause / Play Toggle Orb
                                        val pauseInteraction = remember { MutableInteractionSource() }
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .cosmicTapSpring(pauseInteraction, pressedScale = 0.9f)
                                                .clip(CircleShape)
                                                .background(Color(0x99231048))
                                                .border(0.95.dp, Color(0x88D8B4FE), CircleShape)
                                                .clickable(interactionSource = pauseInteraction, indication = null) {
                                                    isPaused = !isPaused
                                                }
                                                .testTag("lis2_pause_toggle_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                                contentDescription = if (isPaused) "Devam Et" else "Duraklat",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Next / Overview Button
                                        MorCamCapsuleButton(
                                            text = if (currentIndex < slides.lastIndex) {
                                                "Sonraki (${currentIndex + 2}/${slides.size}) →"
                                            } else {
                                                "Tüm Seçimleri Gör ✦"
                                            },
                                            onClick = {
                                                isPaused = false
                                                if (currentIndex < slides.lastIndex) {
                                                    stepPhase = StepPhase.CARD_ONLY
                                                    currentIndex += 1
                                                } else {
                                                    viewMode = SummaryViewMode.FINAL_GRID
                                                }
                                            },
                                            variant = MorCamButtonVariant.Primary,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("summary_sequential_next_btn")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }

                        // ── PHASE 2: ALL CHOICES & MEANINGFUL CONSEQUENCES BOARD ──
                        SummaryViewMode.FINAL_GRID -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Narrative Chapter Reflection Banner with Aidem's Canonical Portrait
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .morCamCosmicGlow(cornerRadius = 22.dp, glowColor = accentColor, alpha = 0.34f * pulseGlow, blurDp = 14.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0x99381874), Color(0x8A170838))
                                            )
                                        )
                                        .border(1.1.dp, accentColor.copy(alpha = 0.72f), RoundedCornerShape(22.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 62.dp, height = 82.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .border(1.1.dp, accentColor.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.img_aiden_portrait),
                                                contentDescription = "Aiden Blackwood",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "✦ BÖLÜM ${chapterMeta.romanNumeral} · KARARLARIN VE BAYRAKLAR",
                                                style = noPadTextStyle,
                                                fontFamily = InterFontFamily,
                                                color = Color(0xFFFEF08A),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                            Text(
                                                text = relationshipReflection,
                                                fontFamily = LiterarySerifFontFamily,
                                                fontStyle = FontStyle.Italic,
                                                color = Color(0xFFFAF6FF),
                                                fontSize = 13.8.sp,
                                                lineHeight = 20.5.sp
                                            )
                                        }
                                    }
                                }

                                // All Choices Together (with Choice-Specific Artwork Strips for Chapter 1!)
                                slides.forEachIndexed { idx, card ->
                                    val cardTint = when {
                                        card.isBranching -> Color(0xFFFEF08A)
                                        card.secretUnlocked != null -> Color(0xFFFDE047)
                                        card.isRelationship -> Color(0xFFF9A8D4)
                                        else -> accentColor
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .cosmicStaggeredEnter(index = idx, key = "overview_${card.index}")
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(Color(0xCC160830))
                                            .border(1.dp, cardTint.copy(alpha = 0.52f), RoundedCornerShape(22.dp))
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // Choice-Specific Painted Artwork Banner (Chapter 1)
                                            if (card.artworkResId != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(92.dp)
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = card.artworkResId),
                                                        contentDescription = card.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                Brush.verticalGradient(
                                                                    listOf(
                                                                        Color(0x280D0420),
                                                                        Color(0x9913062A),
                                                                        Color(0xEE160830)
                                                                    )
                                                                )
                                                            )
                                                    )
                                                    Row(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomStart)
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.weight(1f),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(24.dp)
                                                                    .clip(CircleShape)
                                                                    .background(cardTint.copy(alpha = 0.28f))
                                                                    .border(1.dp, cardTint.copy(alpha = 0.85f), CircleShape),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = "${card.index}",
                                                                    style = noPadTextStyle,
                                                                    fontFamily = InterFontFamily,
                                                                    color = Color.White,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                            Text(
                                                                text = card.title,
                                                                fontFamily = DisplaySerifFontFamily,
                                                                color = Color.White,
                                                                fontSize = 16.5.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                        if (card.isBranching) {
                                                            Text(
                                                                text = "⑂ Yol Ayrımı",
                                                                style = noPadTextStyle,
                                                                fontFamily = InterFontFamily,
                                                                color = Color(0xFFFEF08A),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 15.dp, vertical = 13.dp),
                                                verticalArrangement = Arrangement.spacedBy(7.dp)
                                            ) {
                                                if (card.artworkResId == null) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.weight(1f),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(22.dp)
                                                                    .clip(CircleShape)
                                                                    .background(cardTint.copy(alpha = 0.22f))
                                                                    .border(0.9.dp, cardTint.copy(alpha = 0.75f), CircleShape),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = "${card.index}",
                                                                    style = noPadTextStyle,
                                                                    fontFamily = InterFontFamily,
                                                                    color = Color.White,
                                                                    fontSize = 10.5.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                            Text(
                                                                text = card.title,
                                                                fontFamily = DisplaySerifFontFamily,
                                                                color = Color.White,
                                                                fontSize = 15.5.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                        if (card.isBranching) {
                                                            Text(
                                                                text = "⑂ Yol Ayrımı",
                                                                style = noPadTextStyle,
                                                                fontFamily = InterFontFamily,
                                                                color = Color(0xFFFEF08A),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = "“${card.selectedOption}”",
                                                    fontFamily = LiterarySerifFontFamily,
                                                    color = Color(0xFFFFFFFF),
                                                    fontSize = 14.sp,
                                                    lineHeight = 20.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                Text(
                                                    text = card.impactText,
                                                    fontFamily = InterFontFamily,
                                                    color = Color(0xFFD9C9FA),
                                                    fontSize = 12.2.sp,
                                                    lineHeight = 18.sp
                                                )

                                                if (card.meaningfulImpacts.isNotEmpty()) {
                                                    FlowRow(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalArrangement = Arrangement.spacedBy(5.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        card.meaningfulImpacts.forEach { impact ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(50))
                                                                    .background(impact.tint.copy(alpha = 0.14f))
                                                                    .border(0.75.dp, impact.tint.copy(alpha = 0.45f), RoundedCornerShape(50))
                                                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                                                            ) {
                                                                Text(
                                                                    text = "${impact.symbol} ${impact.text}",
                                                                    style = noPadTextStyle,
                                                                    fontFamily = InterFontFamily,
                                                                    color = Color(0xFFFAF6FF),
                                                                    fontSize = 10.2.sp,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Active / Unlocked Multi-Axis Flags Board (only flags actually triggered so far!)
                                if (bookReadState != null) {
                                    val unlockedCharFlags = remember(bookReadState.numericFlags) {
                                        bookReadState.numericFlags.entries
                                            .filter { it.key.startsWith("char:") && it.value != 0 }
                                            .sortedByDescending { kotlin.math.abs(it.value) }
                                    }
                                    val unlockedStoryFlags = remember(bookReadState.numericFlags) {
                                        bookReadState.numericFlags.entries
                                            .filter { it.key.startsWith("story:") && it.value != 0 }
                                            .sortedByDescending { kotlin.math.abs(it.value) }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xB3160830))
                                            .border(0.95.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(20.dp))
                                            .padding(15.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "AÇILAN & DEĞİŞEN BAYRAKLAR",
                                                style = noPadTextStyle,
                                                fontFamily = InterFontFamily,
                                                color = Color(0xFFFEF08A),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.1.sp
                                            )
                                            Text(
                                                text = "${unlockedCharFlags.size + unlockedStoryFlags.size} Aktif",
                                                style = noPadTextStyle,
                                                fontFamily = InterFontFamily,
                                                color = MorCamTokens.LavenderSoft,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        if (unlockedCharFlags.isNotEmpty()) {
                                            Text(
                                                text = "Karakter Bayrakları",
                                                style = noPadTextStyle,
                                                fontFamily = InterFontFamily,
                                                color = MorCamTokens.SilverLavenderBright,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                                verticalArrangement = Arrangement.spacedBy(7.dp)
                                            ) {
                                                unlockedCharFlags.forEach { (key, value) ->
                                                    val (label, colorHex) = BookStateRepository.describeFlagKey(key)
                                                    val chipColor = if (value < 0) Color(0xFFFCA5A5) else Color(colorHex)
                                                    val sign = if (value > 0) "+$value" else "$value"
                                                    Row(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(50))
                                                            .background(chipColor.copy(alpha = 0.15f))
                                                            .border(0.8.dp, chipColor.copy(alpha = 0.52f), RoundedCornerShape(50))
                                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            style = noPadTextStyle,
                                                            fontFamily = InterFontFamily,
                                                            color = Color(0xFFFAF6FF),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                        Text(
                                                            text = sign,
                                                            style = noPadTextStyle,
                                                            fontFamily = InterFontFamily,
                                                            color = chipColor,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (unlockedStoryFlags.isNotEmpty()) {
                                            if (unlockedCharFlags.isNotEmpty()) MorCamDivider()
                                            Text(
                                                text = "Hikâye & Kozmik Bayraklar",
                                                style = noPadTextStyle,
                                                fontFamily = InterFontFamily,
                                                color = MorCamTokens.SilverLavenderBright,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                                verticalArrangement = Arrangement.spacedBy(7.dp)
                                            ) {
                                                unlockedStoryFlags.forEach { (key, value) ->
                                                    val (label, colorHex) = BookStateRepository.describeFlagKey(key)
                                                    val chipColor = if (value < 0) Color(0xFFFCA5A5) else Color(colorHex)
                                                    val sign = if (value > 0) "+$value" else "$value"
                                                    Row(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(50))
                                                            .background(chipColor.copy(alpha = 0.14f))
                                                            .border(0.8.dp, chipColor.copy(alpha = 0.48f), RoundedCornerShape(50))
                                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            style = noPadTextStyle,
                                                            fontFamily = InterFontFamily,
                                                            color = Color(0xFFFAF6FF),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                        Text(
                                                            text = sign,
                                                            style = noPadTextStyle,
                                                            fontFamily = InterFontFamily,
                                                            color = chipColor,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Text(
                                            text = "✦ İlerleyen bölümlerde yeni karakter ve hikâye bayrakları (sevgi, sadakat, Kule aidiyeti, Kırmızı Göz dengesizliği vb.) seçimlerinle açılır.",
                                            fontFamily = InterFontFamily,
                                            fontStyle = FontStyle.Italic,
                                            color = MorCamTokens.LavenderSoft.copy(alpha = 0.85f),
                                            fontSize = 10.5.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Primary Bottom CTA: Next Chapter
                                if (onNextChapter != null && nextChapterMeta != null) {
                                    MorCamCapsuleButton(
                                        text = "Bölüm ${nextChapterMeta.number}: ${nextChapterMeta.title} →",
                                        onClick = { leaveMenuWith2sFade(onNextChapter) },
                                        variant = MorCamButtonVariant.Primary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("summary_next_chapter_btn")
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MorCamCapsuleButton(
                                        text = "Bölümü Tekrar Oyna",
                                        onClick = { leaveMenuWith2sFade(onRestartChapter) },
                                        variant = MorCamButtonVariant.Secondary,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("summary_replay_btn")
                                    )
                                    MorCamCapsuleButton(
                                        text = "Metne Dön",
                                        onClick = { leaveMenuWith2sFade(onDismiss) },
                                        variant = MorCamButtonVariant.Secondary,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("summary_return_btn")
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
