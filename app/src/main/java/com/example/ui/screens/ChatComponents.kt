package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.LocalCosmicMotionPolicy
import com.example.ui.animation.rememberCosmicPhaseState
import com.example.ui.theme.AppColors
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.sin
import kotlin.random.Random

private val ChatComponentsTrLocale: Locale = Locale.forLanguageTag("tr-TR")
private val TopVignetteBrush = Brush.verticalGradient(colors = listOf(Color(0xEE070814), Color.Transparent))
private val BottomVignetteBrush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xEE070814)))
private val LoadingBarFillBrush = Brush.horizontalGradient(listOf(AppColors.DeepPurple, AppColors.PurpleSoft))
private val MemoryThreadLineBrush = Brush.verticalGradient(
    listOf(AppColors.DeepPurple, AppColors.PrimaryViolet, Color(0x308B5CF6))
)
private val ImpactBadgeShape = RoundedCornerShape(6.dp)
private val TimelineBadgeShape = RoundedCornerShape(5.dp)
private val SpoilerBoxShape = RoundedCornerShape(8.dp)
private val SummaryCardShape = RoundedCornerShape(14.dp)

private data class ChatCosmicParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val speed: Float
)

@Composable
fun CosmicParticleBackground(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val motionConfig = LocalCosmicMotionConfig.current
    val motionPolicy = LocalCosmicMotionPolicy.current
    val allowAmbient = motionPolicy.shouldAnimateContinuously && motionConfig.enableAmbientLoops

    // Draw-phase-only State<Float> drivers -> 0 FPS root recomposition!
    val glowScaleState = rememberCosmicPhaseState(
        durationMs = 4200,
        initialValue = 0.92f,
        targetValue = 1.06f,
        staticValue = 1.0f,
        repeatMode = RepeatMode.Reverse,
        easing = FastOutSlowInEasing,
        label = "chatCosmicGlow",
        enabled = allowAmbient
    )
    val animProgressState = rememberCosmicPhaseState(
        durationMs = 45000,
        initialValue = 0f,
        targetValue = 1f,
        staticValue = 0f,
        repeatMode = RepeatMode.Restart,
        easing = LinearEasing,
        label = "chatCosmicDrift",
        enabled = allowAmbient && motionConfig.enableParticles
    )

    val radialGlowBrush = remember(accentColor) {
        Brush.radialGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.35f),
                Color(0x208B5CF6),
                Color(0x080F172A),
                Color.Transparent
            )
        )
    }

    val particles = remember {
        val rng = Random(35)
        List(35) {
            ChatCosmicParticle(
                xRatio = rng.nextFloat(),
                yRatio = rng.nextFloat(),
                radius = 1.5f + rng.nextFloat() * 3.0f,
                baseAlpha = 0.12f + rng.nextFloat() * 0.16f,
                speed = 0.4f + rng.nextFloat()
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Broad Center Radial Glow with Character Accent Color & Breathing Scale
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val s = glowScaleState.value
                    scaleX = s
                    scaleY = s
                }
                .background(radialGlowBrush)
        )

        // 2. Drifting Star Dust / Cosmic Energy Particles Canvas
        if (motionConfig.enableParticles) {
            val activeCount = (particles.size * motionConfig.particleScale).toInt().coerceIn(8, particles.size)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                if (width <= 0f || height <= 0f) return@Canvas
                val animProgress = animProgressState.value

                for (i in 0 until activeCount) {
                    val p = particles[i]
                    val currentY = ((p.yRatio - animProgress * p.speed * 0.35f) % 1f + 1f) % 1f
                    val rawX = p.xRatio + sin(animProgress * 6.28318f + p.yRatio * 12f) * 0.04f
                    val currentX = ((rawX % 1f) + 1f) % 1f

                    drawCircle(
                        color = accentColor.copy(alpha = p.baseAlpha),
                        radius = p.radius.dp.toPx(),
                        center = Offset(currentX * width, currentY * height)
                    )
                }
            }
        }

        // 3. Top Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(TopVignetteBrush)
        )

        // 4. Bottom Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.BottomCenter)
                .background(BottomVignetteBrush)
        )
    }
}

@Composable
fun PulsingLoadingText(
    modifier: Modifier = Modifier
) {
    val motionConfig = LocalCosmicMotionConfig.current
    val motionPolicy = LocalCosmicMotionPolicy.current
    val allowMotion = motionPolicy.shouldAnimateContinuously && motionConfig.enableAmbientLoops

    // Draw-phase-only State<Float> drivers -> 0 FPS root recomposition!
    val lineProgressState = rememberCosmicPhaseState(
        durationMs = 1500,
        initialValue = 0f,
        targetValue = 1f,
        staticValue = 0.62f,
        repeatMode = RepeatMode.Restart,
        easing = FastOutSlowInEasing,
        label = "shimmerLineFill",
        enabled = allowMotion
    )
    val shimmerAlphaState = rememberCosmicPhaseState(
        durationMs = 750,
        initialValue = 0.4f,
        targetValue = 1.0f,
        staticValue = 0.85f,
        repeatMode = RepeatMode.Reverse,
        easing = LinearEasing,
        label = "shimmerTextAlpha",
        enabled = allowMotion
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer { alpha = shimmerAlphaState.value.coerceIn(0f, 1f) }
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(AppColors.PurpleSoft)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sonuçlar dokunuyor...",
                color = AppColors.PurpleLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                letterSpacing = 0.9.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Canvas(
            modifier = Modifier
                .width(160.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
        ) {
            drawRect(color = Color(0x308B5CF6))
            val progress = lineProgressState.value.coerceIn(0f, 1f)
            if (progress > 0f) {
                drawRect(
                    brush = LoadingBarFillBrush,
                    size = Size(size.width * progress, size.height)
                )
            }
        }
    }
}

@Composable
fun CinematicSequentialReveal(
    choice: ChapterChoiceItem,
    phase: StepPhase
) {
    val quick = LocalCosmicMotionPolicy.current.shouldUseQuickTransitions

    // Screen Shake effect for Branching choices (skipped when Reduce Motion is active)
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(phase, quick) {
        if (phase == StepPhase.RESULT_REVEALED && choice.isBranching && !quick) {
            repeat(3) {
                shakeAnim.animateTo(4f, tween(30))
                shakeAnim.animateTo(-4f, tween(30))
            }
            shakeAnim.animateTo(0f, tween(30))
        } else {
            shakeAnim.snapTo(0f)
        }
    }

    // Overshoot Elastic Icon Reveal (scale 0 -> 1.35 -> 1.0)
    val iconScaleAnim = remember { Animatable(0f) }
    LaunchedEffect(phase, quick) {
        if (phase == StepPhase.RESULT_REVEALED) {
            if (quick) {
                iconScaleAnim.snapTo(1f)
            } else {
                iconScaleAnim.snapTo(0f)
                iconScaleAnim.animateTo(
                    targetValue = 1.35f,
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                )
                iconScaleAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(160, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val impactColor = when {
        choice.isBranching -> AppColors.WarningAmber
        choice.isRelationship -> AppColors.PinkLight
        else -> AppColors.TextSecondary
    }

    val animatedImpactColor by animateColorAsState(
        targetValue = if (phase == StepPhase.RESULT_REVEALED) impactColor else AppColors.DeepPurple,
        animationSpec = tween(if (quick) 180 else 800, easing = FastOutSlowInEasing),
        label = "MoodShiftColor"
    )

    val uppercaseTitle = remember(choice.title) { choice.title.uppercase(ChatComponentsTrLocale) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer { translationX = shakeAnim.value.dp.toPx() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = uppercaseTitle,
            color = AppColors.PrimaryPurple,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        AnimatedContent(
            targetState = choice.selectedOption,
            transitionSpec = {
                val enterMs = if (quick) 150 else 600
                val delayMs = if (quick) 0 else 150
                val exitMs = if (quick) 120 else 300
                (fadeIn(animationSpec = tween(enterMs, delayMillis = delayMs, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        initialOffsetY = { 25 },
                        animationSpec = tween(enterMs, delayMillis = delayMs, easing = FastOutSlowInEasing)
                    ) +
                    scaleIn(initialScale = 0.96f, animationSpec = tween(enterMs, delayMillis = delayMs))) togetherWith
                    (fadeOut(animationSpec = tween(exitMs, easing = FastOutLinearInEasing)) +
                        scaleOut(targetScale = 0.95f, animationSpec = tween(exitMs)))
            },
            label = "ActionTextAnim"
        ) { text ->
            Text(
                text = text,
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (phase == StepPhase.CARD_ONLY) {
            PulsingLoadingText()
        } else {
            val revealMs = if (quick) 180 else 600
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(revealMs)) +
                    expandVertically(animationSpec = tween(revealMs))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(44.dp)
                            .background(
                                Brush.verticalGradient(listOf(AppColors.DeepPurple, animatedImpactColor))
                            )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = choice.icon,
                                fontSize = 23.sp,
                                modifier = Modifier.graphicsLayer {
                                    val s = iconScaleAnim.value
                                    scaleX = s
                                    scaleY = s
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    choice.isBranching -> "💥 BÜYÜK DALLANMA ETKİSİ"
                                    choice.isRelationship -> "💕 İLİŞKİ DOKUNUŞU"
                                    else -> "💭 TAVIR ETKİSİ"
                                },
                                color = animatedImpactColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val (levelLabel, levelColor, levelBg) = when (choice.impactLevel) {
                            "kritik" -> Triple("⚡ KRİTİK ETKİ", Color(0xFFFDE047), Color(0xFF451A03))
                            "dusuk" -> Triple("· DÜŞÜK ETKİ", Color(0xFF94A3B8), Color(0xFF1E293B))
                            else -> Triple("◉ ORTA ETKİ", Color(0xFFC084FC), Color(0xFF2E1065))
                        }
                        Box(
                            modifier = Modifier
                                .clip(ImpactBadgeShape)
                                .background(levelBg)
                                .border(0.8.dp, levelColor.copy(alpha = 0.4f), ImpactBadgeShape)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                levelLabel,
                                color = levelColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = choice.impactText,
                            color = AppColors.TextWhite,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 23.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryThreadTimeline(
    choices: List<ChapterChoiceItem>,
    relationshipTease: String?
) {
    val quick = LocalCosmicMotionPolicy.current.shouldUseQuickTransitions
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(start = 11.dp, top = 12.dp, bottom = 12.dp)
                .width(2.dp)
                .fillMaxHeight()
                .background(MemoryThreadLineBrush)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            choices.forEachIndexed { idx, choice ->
                key(choice.id) {
                    var isVisible by remember(choice.id) { mutableStateOf(quick) }
                    LaunchedEffect(choice.id, quick) {
                        if (!quick) {
                            delay(idx * 90L)
                            isVisible = true
                        }
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(if (quick) 120 else 400)) +
                            slideInVertically(animationSpec = tween(if (quick) 120 else 400)) { 20 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            val nodeColor = when {
                                choice.isBranching -> AppColors.WarningAmber
                                choice.isRelationship -> AppColors.PinkLight
                                else -> AppColors.PrimaryPurple
                            }

                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(nodeColor)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = choice.title,
                                    color = AppColors.PrimaryPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = choice.selectedOption,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(5.dp))

                                val (lvlLabel, lvlColor) = when (choice.impactLevel) {
                                    "kritik" -> Pair("⚡ KRİTİK", Color(0xFFFDE047))
                                    "dusuk" -> Pair("· DÜŞÜK", Color(0xFF94A3B8))
                                    else -> Pair("◉ ORTA", Color(0xFFC084FC))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(TimelineBadgeShape)
                                        .background(lvlColor.copy(alpha = 0.12f))
                                        .border(0.5.dp, lvlColor.copy(alpha = 0.35f), TimelineBadgeShape)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        lvlLabel,
                                        color = lvlColor,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(choice.icon, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = choice.impactText,
                                        color = AppColors.TextSecondary,
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }

                                if (choice.isBranching) {
                                    var spoilerOpen by remember(choice.id) { mutableStateOf(false) }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(SpoilerBoxShape)
                                            .background(Color(0xFF1A0D35))
                                            .border(
                                                0.8.dp,
                                                AppColors.WarningAmber.copy(alpha = if (spoilerOpen) 0.5f else 0.2f),
                                                SpoilerBoxShape
                                            )
                                            .clickable { spoilerOpen = !spoilerOpen }
                                            .padding(horizontal = 10.dp, vertical = 7.dp)
                                    ) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(if (spoilerOpen) "▲" else "▼", color = AppColors.WarningAmber, fontSize = 9.sp)
                                                Text(
                                                    if (spoilerOpen) "Alternatif yolu gizle" else "🔀 Diğer yolu merak ediyorum...",
                                                    color = AppColors.WarningAmber,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            AnimatedVisibility(
                                                visible = spoilerOpen,
                                                enter = fadeIn(tween(if (quick) 100 else 300)) +
                                                    expandVertically(tween(if (quick) 100 else 300))
                                            ) {
                                                Column {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(1.dp)
                                                            .background(AppColors.WarningAmber.copy(alpha = 0.2f))
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        "Bu noktada farklı bir seçim yapsaydın hikaye dallanırdı. Her seçim kalıcı — ancak sonraki bölümde farklı yolu deneyebilirsin.",
                                                        color = AppColors.TextMuted,
                                                        fontSize = 11.5.sp,
                                                        lineHeight = 16.sp
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

            if (relationshipTease != null) {
                var isVisible by remember(relationshipTease) { mutableStateOf(quick) }
                LaunchedEffect(relationshipTease, quick) {
                    if (!quick) {
                        delay(choices.size * 90L + 100L)
                        isVisible = true
                    }
                }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(if (quick) 120 else 400)) +
                        slideInVertically(animationSpec = tween(if (quick) 120 else 400)) { 20 }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Pink)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🔮 İLERLEYEN BÖLÜM ETKİSİ",
                                color = AppColors.PinkLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = relationshipTease,
                                color = AppColors.Hex_FBCFE8,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryChoiceCard(
    title: String,
    choiceText: String,
    impact: String,
    icon: String = "💭",
    isBranching: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isBranching) AppColors.Hex_2C192E else AppColors.Hex_181728),
        border = BorderStroke(
            1.dp,
            if (isBranching) AppColors.RoseSoft else AppColors.Hex_382F58
        ),
        shape = SummaryCardShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = if (isBranching) AppColors.RoseLight else AppColors.PrimaryPurple,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = choiceText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                color = if (isBranching) AppColors.Hex_3B1220 else AppColors.Hex_121220,
                shape = SpoilerBoxShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                Text(
                    text = impact,
                    color = if (isBranching) AppColors.Hex_FECDD3 else AppColors.TextSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 17.5.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
