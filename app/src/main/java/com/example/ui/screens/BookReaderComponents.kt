package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.book.BookChapterMeta
import com.example.book.BookCharacterId
import com.example.book.BookChoiceDef
import com.example.book.BookReadState
import com.example.book.CosmicDriftBookRegistry
import com.example.book.RecordedDecision
import com.example.ui.animation.cosmicGlassSheen
import com.example.ui.animation.cosmicStaggeredEnter
import com.example.ui.animation.cosmicTapSpring
import com.example.ui.components.MorCamButtonVariant
import com.example.ui.components.MorCamCapsuleButton
import com.example.ui.components.MorCamDivider
import com.example.ui.components.MorCamTokens
import com.example.ui.components.drawMorCamFourPointStar
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily

/**
 * Collector's-Edition Mor Cam Chapter Cover Banner displayed at item 0 of each chapter.
 * Uses the flagship Kozmik Sürükleniş banner artwork feathered into obsidian-amethyst glass
 * with double crystal rims, 4-point corner star glints, Roman numeral crest, and Aiden medallion.
 */
@Composable
fun CollectorChapterCoverCard(
    chapterMeta: BookChapterMeta,
    glowPulse: Float,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(chapterMeta.accentHex)
    val cardShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .morCamCosmicGlow(
                cornerRadius = 28.dp,
                glowColor = accentColor,
                alpha = 0.42f * glowPulse,
                blurDp = 18.dp
            )
            .clip(cardShape)
            .cosmicGlassSheen(cornerRadius = 28.dp, periodMs = 5200)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xE01C0C3C),
                        Color(0xF0110627),
                        Color(0xEB170933)
                    )
                )
            )
    ) {
        // Feathered hero artwork top half
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(178.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.00f to Color.White.copy(alpha = 0.92f),
                            0.46f to Color.White.copy(alpha = 0.72f),
                            0.82f to Color.White.copy(alpha = 0.22f),
                            1.00f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_book_kozmik_suruklenis_banner),
                contentDescription = "Bölüm ${chapterMeta.number} kapağı",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Velvet royal amethyst tint veil
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x452A1158),
                                Color(0x55120628),
                                Color(0xCC110627)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Roman Numeral Jewel Crest
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Color.Transparent, accentColor.copy(alpha = 0.85f)))
                        )
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xAA1A0A38))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(listOf(Color(0xEEF5EEFF), accentColor)),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "BÖLÜM ${chapterMeta.romanNumeral}",
                        fontFamily = DisplaySerifFontFamily,
                        color = Color(0xFFFAF6FF),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.2.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.85f), Color.Transparent))
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Glowing Aiden Blackwood Portrait Medallion
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .morCamCosmicGlow(cornerRadius = 32.dp, glowColor = accentColor, alpha = 0.65f, blurDp = 12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0B041A))
                    .border(
                        width = 1.6.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFFFFFFF), accentColor, Color(0xFF60A5FA), Color(0xFFE9D5FF))
                        ),
                        shape = CircleShape
                    )
                    .padding(2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_aiden_blackwood_avatar),
                    contentDescription = "Aiden Blackwood",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Playfair Display Chapter Title with Amethyst Glow
            Text(
                text = chapterMeta.title,
                style = TextStyle(
                    fontFamily = DisplaySerifFontFamily,
                    color = Color(0xFFFFFFFF),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 33.sp,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = accentColor.copy(alpha = 0.72f),
                        offset = Offset(0f, 2f),
                        blurRadius = 14f
                    )
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Poetic Subtitle
            Text(
                text = chapterMeta.subtitle,
                fontFamily = LiterarySerifFontFamily,
                fontStyle = FontStyle.Italic,
                color = Color(0xFFE2D4FA),
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Delicate Starlight Ornament Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(0.8.dp)
                        .background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0x88E9D5FF))))
                )
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawMorCamFourPointStar(
                        center = Offset(size.width / 2f, size.height / 2f),
                        outerRadius = size.minDimension * 0.48f,
                        innerRadius = size.minDimension * 0.16f,
                        coreColor = Color(0xFFF5EEFF),
                        edgeColor = accentColor,
                        glowColor = accentColor
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(0.8.dp)
                        .background(Brush.horizontalGradient(listOf(Color(0x88E9D5FF), Color.Transparent)))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real Chapter Metadata Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoverMetaBadge("${chapterMeta.sceneCount} Sahne", accentColor)
                CoverMetaBadge("${chapterMeta.decisionCount} Karar", Color(0xFFD8B4FE))
                CoverMetaBadge("~${chapterMeta.estimatedMinutes} dk", Color(0xFF93C5FD))
            }
        }

        // Double-rimmed crystal glass border + 4-point corner glints
        Canvas(modifier = Modifier.matchParentSize()) {
            val crOuter = 28.dp.toPx()
            val crInner = 25.dp.toPx()
            val inset = 3.dp.toPx()
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xF0FAF5FF),
                        accentColor.copy(alpha = 0.85f),
                        Color(0x998B5CF6),
                        Color(0xDDE9D5FF)
                    )
                ),
                cornerRadius = CornerRadius(crOuter, crOuter),
                style = Stroke(width = 1.4.dp.toPx())
            )
            drawRoundRect(
                color = Color(0x38E9D5FF),
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = CornerRadius(crInner, crInner),
                style = Stroke(width = 0.7.dp.toPx())
            )
            // Corner starlight glints
            drawMorCamFourPointStar(
                center = Offset(16.dp.toPx(), 16.dp.toPx()),
                outerRadius = 6.dp.toPx(),
                innerRadius = 1.9.dp.toPx(),
                coreColor = Color(0xFFFAF5FF),
                edgeColor = Color(0xFFE9D5FF),
                glowColor = accentColor
            )
            drawMorCamFourPointStar(
                center = Offset(size.width - 16.dp.toPx(), 16.dp.toPx()),
                outerRadius = 5.dp.toPx(),
                innerRadius = 1.6.dp.toPx(),
                coreColor = Color(0xFFFAF5FF),
                edgeColor = accentColor,
                glowColor = accentColor
            )
        }
    }
}

@Composable
private fun CoverMetaBadge(text: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.14f))
            .border(0.8.dp, accent.copy(alpha = 0.48f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontFamily = InterFontFamily,
            color = Color(0xFFF6F0FF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Ornate cosmic starlight header that introduces each scene cleanly inside the continuous
 * manuscript reading flow — replacing plain boxy cards with book-grade editorial elegance.
 */
@Composable
fun ManuscriptSceneHeader(
    sceneOrder: Int,
    sceneTitle: String,
    locationTag: String,
    chapterAccent: Color,
    isFirstScene: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = if (isFirstScene) 12.dp else 22.dp,
                bottom = 8.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hairline starlight divider between scenes
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.85.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, chapterAccent.copy(alpha = 0.55f), Color(0xAAE9D5FF))
                        )
                    )
            )
            Canvas(modifier = Modifier.size(11.dp)) {
                drawMorCamFourPointStar(
                    center = Offset(size.width / 2f, size.height / 2f),
                    outerRadius = size.minDimension * 0.48f,
                    innerRadius = size.minDimension * 0.16f,
                    coreColor = Color(0xFFF5EEFF),
                    edgeColor = chapterAccent,
                    glowColor = chapterAccent
                )
            }
            Text(
                text = "SAHNE $sceneOrder",
                fontFamily = InterFontFamily,
                color = chapterAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp
            )
            Canvas(modifier = Modifier.size(11.dp)) {
                drawMorCamFourPointStar(
                    center = Offset(size.width / 2f, size.height / 2f),
                    outerRadius = size.minDimension * 0.48f,
                    innerRadius = size.minDimension * 0.16f,
                    coreColor = Color(0xFFF5EEFF),
                    edgeColor = chapterAccent,
                    glowColor = chapterAccent
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.85.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xAAE9D5FF), chapterAccent.copy(alpha = 0.55f), Color.Transparent)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = sceneTitle,
            style = TextStyle(
                fontFamily = DisplaySerifFontFamily,
                color = Color(0xFFFAF6FF),
                fontSize = 21.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 27.sp,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = chapterAccent.copy(alpha = 0.45f),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 10f
                )
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = locationTag,
            fontFamily = InterFontFamily,
            color = Color(0xFFA992D4),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.1.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Editorial paragraph rendered directly on the dark amethyst manuscript sheet,
 * with distinct illuminated styling for quoted dialogue ("..." / “...”),
 * italic inner thoughts (*...*), and optional Collector's Drop Cap on Scene 1 Opening.
 */
@Composable
fun ManuscriptParagraphBlock(
    rawText: String,
    fontSizeSp: Float,
    chapterAccent: Color,
    useDropCap: Boolean = false
) {
    val trimmed = rawText.trim()
    val isPureDialogue = remember(trimmed) {
        (trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
            (trimmed.startsWith("“") && trimmed.endsWith("”"))
    }

    if (useDropCap && trimmed.length > 2 && trimmed[0].isLetter()) {
        val firstChar = trimmed.substring(0, 1)
        val restText = trimmed.substring(1)
        val formattedRest = remember(restText) { formatLiteraryParagraph(restText) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 5.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Illuminated Crystal Drop Cap
            Box(
                modifier = Modifier
                    .padding(end = 11.dp, top = 2.dp)
                    .size(46.dp)
                    .morCamCosmicGlow(cornerRadius = 12.dp, glowColor = chapterAccent, alpha = 0.35f, blurDp = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(chapterAccent.copy(alpha = 0.28f), Color(0x881B0B3A))
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(Color(0xEEF5EEFF), chapterAccent)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstChar,
                    style = TextStyle(
                        fontFamily = DisplaySerifFontFamily,
                        color = Color(0xFFFFFFFF),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        shadow = Shadow(
                            color = chapterAccent.copy(alpha = 0.85f),
                            offset = Offset(0f, 1f),
                            blurRadius = 10f
                        )
                    )
                )
            }

            Text(
                text = formattedRest,
                fontFamily = LiterarySerifFontFamily,
                color = Color(0xFFE0D7F5),
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp * 1.72f).sp,
                letterSpacing = 0.15.sp,
                modifier = Modifier.weight(1f)
            )
        }
    } else if (isPureDialogue) {
        // Standalone spoken line gets a subtle, refined luminous quote rail for instant scannability
        val formatted = remember(trimmed) { formatLiteraryParagraph(trimmed) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x362C145E), Color(0x18170933))
                    )
                )
                .border(0.75.dp, Color(0x40D8B4FE), RoundedCornerShape(14.dp))
                .padding(start = 12.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), chapterAccent))
                    )
            )
            Spacer(modifier = Modifier.width(11.dp))
            Text(
                text = formatted,
                fontFamily = LiterarySerifFontFamily,
                color = Color(0xFFFFFFFF),
                fontSize = (fontSizeSp + 0.4f).sp,
                lineHeight = (fontSizeSp * 1.68f).sp,
                letterSpacing = 0.18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        val formatted = remember(trimmed) { formatLiteraryParagraph(trimmed) }
        Text(
            text = formatted,
            fontFamily = LiterarySerifFontFamily,
            color = Color(0xFFDDD4F2),
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.74f).sp,
            letterSpacing = 0.15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 5.dp)
        )
    }
}

/**
 * Memory Echo Inlay — surfaces hidden cross-chapter flag callbacks in an atmospheric
 * violet-gold glass inlay so players feel their earlier choices echoing through the novel.
 */
@Composable
fun MemoryEchoInlay(
    label: String,
    body: String,
    fontSizeSp: Float,
    chapterAccent: Color
) {
    val formatted = remember(body) { formatLiteraryParagraph(body) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0x52311766), Color(0x3D1B0B3B), Color(0x2E14082C))
                )
            )
            .border(
                width = 0.95.dp,
                brush = Brush.linearGradient(
                    listOf(Color(0xAAFDE047), chapterAccent.copy(alpha = 0.7f), Color(0x558B5CF6))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFDE047),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "GEÇMİŞ KARARIN YANKISI · ${label.uppercase()}",
                    fontFamily = InterFontFamily,
                    color = Color(0xFFFEF08A),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.9.sp
                )
            }
            Text(
                text = formatted,
                fontFamily = LiterarySerifFontFamily,
                color = Color(0xFFEDE6FF),
                fontSize = (fontSizeSp - 0.5f).sp,
                lineHeight = (fontSizeSp * 1.65f).sp
            )
        }
    }
}

/**
 * Epigraph plate at the closing line of a chapter.
 */
@Composable
fun ChapterClosingQuotePlate(
    quote: String,
    chapterAccent: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(chapterAccent.copy(alpha = 0.16f), Color(0x55160830))
                )
            )
            .border(1.dp, chapterAccent.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(modifier = Modifier.size(14.dp)) {
                drawMorCamFourPointStar(
                    center = Offset(size.width / 2f, size.height / 2f),
                    outerRadius = size.minDimension * 0.48f,
                    innerRadius = size.minDimension * 0.16f,
                    coreColor = Color(0xFFFEF08A),
                    edgeColor = Color(0xFFFDE047),
                    glowColor = chapterAccent
                )
            }
            Text(
                text = "“$quote”",
                fontFamily = LiterarySerifFontFamily,
                fontStyle = FontStyle.Italic,
                color = Color(0xFFFAF6FF),
                fontSize = 15.5.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Spoiler-free Decision Seal Ribbon embedded chronologically between scenes once a choice is made.
 * Shows only the player's chosen line — keeps all consequences and character impacts hidden
 * until the Life is Strange-style Chapter End Screen.
 */
@Composable
fun BookDecisionSealRibbon(
    decision: RecordedDecision
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0x662D1460), Color(0x52190A39), Color(0x6024104F))
                )
            )
            .border(
                width = 0.95.dp,
                brush = Brush.linearGradient(
                    listOf(Color(0xBBE9D5FF), Color(0x77A855F7), Color(0x88C084FC))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 15.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null,
                    tint = Color(0xFFD8B4FE),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "SEÇİMİN  ·  ${decision.decisionTitle.uppercase()}",
                    fontFamily = InterFontFamily,
                    color = Color(0xFFD8B4FE),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.85.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = formatLiteraryParagraph(decision.selectedOption),
                fontFamily = LiterarySerifFontFamily,
                color = Color(0xFFFAF6FF),
                fontSize = 14.2.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.5.sp
            )
        }
    }
}

/**
 * In-Flow Auto-Revealing Mor Cam Choice Sanctuary.
 * Sits strictly BELOW the last paragraph of the active scene (never overlaps text) and
 * unfolds automatically with a velvet spring the instant the reader scrolls down to it.
 */
@Composable
fun AutoRevealMorCamChoiceSanctuary(
    nodeId: String,
    isRevealed: Boolean,
    prompt: String,
    choices: List<BookChoiceDef>,
    accentColor: Color,
    glowPulse: Float,
    onForceReveal: () -> Unit,
    onSelectChoice: (BookChoiceDef) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp)
            .testTag("book_choice_sanctuary_$nodeId")
    ) {
        // Gentle threshold indicator before full reveal (tappable as fallback, auto-opens on scroll)
        if (!isRevealed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x55221048))
                    .border(0.9.dp, Color(0x66D8B4FE), RoundedCornerShape(18.dp))
                    .clickable { onForceReveal() }
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Karar noktasına ulaştın · Seçenekler açılıyor...",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFE9D5FF),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isRevealed,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                expandFrom = Alignment.Top
            ) + fadeIn()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .morCamCosmicGlow(
                        cornerRadius = 26.dp,
                        glowColor = accentColor,
                        alpha = 0.42f * glowPulse,
                        blurDp = 16.dp
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xE8281252),
                                Color(0xF215082F),
                                Color(0xEE1F0D42)
                            )
                        )
                    )
                    .drawBehind {
                        // Top crystal arc reflection sheen
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.16f),
                                    Color(0xFFE9D5FF).copy(alpha = 0.04f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = size.height * 0.25f
                            ),
                            cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx())
                        )
                    }
                    .border(
                        width = 1.35.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xF0FAF5FF),
                                accentColor.copy(alpha = 0.9f),
                                Color(0x998B5CF6),
                                Color(0xDDE9D5FF)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Sanctuary Starlight Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(0.8.dp)
                                .background(Brush.horizontalGradient(listOf(Color.Transparent, accentColor.copy(alpha = 0.7f))))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Canvas(modifier = Modifier.size(13.dp)) {
                            drawMorCamFourPointStar(
                                center = Offset(size.width / 2f, size.height / 2f),
                                outerRadius = size.minDimension * 0.48f,
                                innerRadius = size.minDimension * 0.16f,
                                coreColor = Color(0xFFFAF6FF),
                                edgeColor = accentColor,
                                glowColor = accentColor
                            )
                        }
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "KARAR NOKTASI",
                            fontFamily = DisplaySerifFontFamily,
                            color = Color(0xFFFAF6FF),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Canvas(modifier = Modifier.size(13.dp)) {
                            drawMorCamFourPointStar(
                                center = Offset(size.width / 2f, size.height / 2f),
                                outerRadius = size.minDimension * 0.48f,
                                innerRadius = size.minDimension * 0.16f,
                                coreColor = Color(0xFFFAF6FF),
                                edgeColor = accentColor,
                                glowColor = accentColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(0.8.dp)
                                .background(Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.7f), Color.Transparent)))
                        )
                    }

                    // Poetic Prompt
                    Text(
                        text = prompt,
                        fontFamily = LiterarySerifFontFamily,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFFE9DEFF),
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Sculpted Mor Cam Choice Cards
                    val romanLabels = listOf("I", "II", "III", "IV")
                    choices.forEachIndexed { idx, choice ->
                        val interaction = remember(choice.id) { MutableInteractionSource() }
                        val isBranchingChoice = choice.effect.isBranching
                        val cardAccent = if (isBranchingChoice) Color(0xFFFDE047) else accentColor
                        val roman = romanLabels.getOrElse(idx) { "${idx + 1}" }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .cosmicStaggeredEnter(index = idx, key = choice.id)
                                .cosmicTapSpring(interaction, pressedScale = 0.97f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = if (isBranchingChoice) {
                                            listOf(Color(0x993E1B7A), Color(0x88240E4B), Color(0x9936166B))
                                        } else {
                                            listOf(Color(0x8C2C1459), Color(0x801B0B3B), Color(0x8C26114E))
                                        }
                                    )
                                )
                                .drawBehind {
                                    // Glossy top crystal highlight
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.14f),
                                                Color.Transparent
                                            ),
                                            startY = 0f,
                                            endY = size.height * 0.42f
                                        ),
                                        cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
                                    )
                                }
                                .border(
                                    width = if (isBranchingChoice) 1.25.dp else 1.05.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            Color(0xEEF5EEFF),
                                            cardAccent.copy(alpha = 0.85f),
                                            Color(0x809333EA)
                                        )
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable(
                                    interactionSource = interaction,
                                    indication = null
                                ) { onSelectChoice(choice) }
                                .testTag("book_choice_${choice.id}")
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Luminous Roman Medallion
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(cardAccent.copy(alpha = 0.45f), Color(0x991A0A38))
                                            )
                                        )
                                        .border(1.1.dp, cardAccent.copy(alpha = 0.85f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = roman,
                                        fontFamily = DisplaySerifFontFamily,
                                        color = Color(0xFFFFFFFF),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = formatLiteraryParagraph(choice.label),
                                        fontFamily = LiterarySerifFontFamily,
                                        color = Color(0xFFFAF6FF),
                                        fontSize = 14.8.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 21.5.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(cardAccent.copy(alpha = 0.18f))
                                                .border(0.7.dp, cardAccent.copy(alpha = 0.55f), RoundedCornerShape(50))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isBranchingChoice) "⑂ ${choice.toneHint}" else choice.toneHint,
                                                fontFamily = InterFontFamily,
                                                color = if (isBranchingChoice) Color(0xFFFEF08A) else Color(0xFFE2D4FA),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Seç",
                                    tint = Color(0xFFE9D5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean, spoiler-free End-of-Chapter Finish Sanctuary placed below the closing epigraph.
 * Reveals NO character scores or stats beforehand — instead invites the reader to tap
 * "Bölümü Bitir" to enter the cinematic Life is Strange 2-style Chapter End Screen & soundtrack.
 */
@Composable
fun MorCamChapterCompleteCard(
    state: BookReadState,
    chapterMeta: BookChapterMeta,
    accentColor: Color,
    glowPulse: Float,
    onOpenSummaryModal: () -> Unit,
    onOpenChronicle: () -> Unit,
    onNextChapter: () -> Unit,
    onRestartChapter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 10.dp)
            .morCamCosmicGlow(cornerRadius = 26.dp, glowColor = accentColor, alpha = 0.44f * glowPulse, blurDp = 18.dp)
            .clip(RoundedCornerShape(26.dp))
            .cosmicGlassSheen(cornerRadius = 26.dp, periodMs = 4600)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xEB281254), Color(0xF215082E), Color(0xEB1F0C42))
                )
            )
            .border(1.3.dp, MorCamTokens.RimGradient, RoundedCornerShape(26.dp))
            .padding(horizontal = 22.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "✦ BÖLÜM ${chapterMeta.romanNumeral} SONUNA ULAŞTIN ✦",
            fontFamily = InterFontFamily,
            color = Color(0xFFFEF08A),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp
        )

        Text(
            text = chapterMeta.title,
            fontFamily = DisplaySerifFontFamily,
            color = Color(0xFFFFFFFF),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Bu bölümde verdiğin kararların hikâyede bıraktığı izleri görmek ve bölümü sonlandırmak için devam et.",
            fontFamily = LiterarySerifFontFamily,
            fontStyle = FontStyle.Italic,
            color = Color(0xFFDCCDF7),
            fontSize = 14.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        MorCamCapsuleButton(
            text = "Bölümü Bitir ✦",
            onClick = onOpenSummaryModal,
            variant = MorCamButtonVariant.Primary,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("book_finish_chapter_button")
        )
    }
}

@Composable
private fun ChapterEndStatTile(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x521E0D3E))
            .border(0.85.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = DisplaySerifFontFamily,
            color = accent,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontFamily = InterFontFamily,
            color = MorCamTokens.LavenderMuted,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Composable
fun ReaderMorCamIconOrb(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    isActive: Boolean = false,
    activeColor: Color = Color(0xFFC084FC),
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(42.dp)
            .cosmicTapSpring(interaction, pressedScale = 0.88f)
            .clip(CircleShape)
            .background(
                if (isActive) activeColor.copy(alpha = 0.24f) else Color(0xBA1C0C3A)
            )
            .border(
                width = 1.05.dp,
                color = if (isActive) activeColor.copy(alpha = 0.85f) else Color(0x88D8B4FE),
                shape = CircleShape
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) activeColor else MorCamTokens.SilverLavenderBright,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Illuminated literary paragraph formatter:
 * - Quoted dialogue ("..." and “...”) stands out distinctly in luminous pearl-white
 *   (FontWeight.Medium + delicate amethyst glow shadow) with crisp contrast against narration.
 * - Bold (**...**) renders bright white semi-bold.
 * - Inner thoughts / emphasis (*...*) render in soft wisteria-lavender italics.
 */
fun formatLiteraryParagraph(raw: String): AnnotatedString = buildAnnotatedString {
    val dialogueStyle = SpanStyle(
        color = Color(0xFFFFFFFF),
        fontWeight = FontWeight.Medium,
        shadow = Shadow(
            color = Color(0x88D8B4FE),
            offset = Offset(0f, 0f),
            blurRadius = 7f
        )
    )
    val quoteMarkStyle = SpanStyle(
        color = Color(0xFFE9D5FF),
        fontWeight = FontWeight.SemiBold,
        shadow = Shadow(
            color = Color(0x99C084FC),
            offset = Offset(0f, 0f),
            blurRadius = 8f
        )
    )
    val thoughtStyle = SpanStyle(
        fontStyle = FontStyle.Italic,
        color = Color(0xFFD9C2FF)
    )
    val boldStyle = SpanStyle(
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFFFFFFFF)
    )

    var i = 0
    while (i < raw.length) {
        val ch = raw[i]

        // Straight double-quoted speech: "..."
        if (ch == '"') {
            val end = raw.indexOf('"', i + 1)
            if (end != -1) {
                withStyle(quoteMarkStyle) { append("“") }
                withStyle(dialogueStyle) { append(raw.substring(i + 1, end)) }
                withStyle(quoteMarkStyle) { append("”") }
                i = end + 1
                continue
            }
        }

        // Typographic curly double-quoted speech: “...”
        if (ch == '“') {
            val end = raw.indexOf('”', i + 1)
            if (end != -1) {
                withStyle(quoteMarkStyle) { append("“") }
                withStyle(dialogueStyle) { append(raw.substring(i + 1, end)) }
                withStyle(quoteMarkStyle) { append("”") }
                i = end + 1
                continue
            }
        }

        // Bold markdown: **...**
        if (i + 1 < raw.length && ch == '*' && raw[i + 1] == '*') {
            val end = raw.indexOf("**", i + 2)
            if (end != -1) {
                withStyle(boldStyle) { append(raw.substring(i + 2, end)) }
                i = end + 2
                continue
            }
        }

        // Italic inner thoughts / emphasis: *...*
        if (ch == '*') {
            val end = raw.indexOf('*', i + 1)
            if (end != -1) {
                withStyle(thoughtStyle) { append(raw.substring(i + 1, end)) }
                i = end + 1
                continue
            }
        }

        append(ch)
        i++
    }
}
