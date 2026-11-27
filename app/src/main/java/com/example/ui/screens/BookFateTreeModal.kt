package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.book.BookBranchDiscoveryHelper
import com.example.book.BookChapterMeta
import com.example.book.BookReadState
import com.example.book.CosmicDriftBookRegistry
import com.example.ui.components.MorCamDivider
import com.example.ui.components.MorCamTokens
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.components.verticalFadingEdge
import com.example.ui.screens.cosmic3d.CosmicDuelBossId
import com.example.ui.screens.cosmic3d.StoryMiniGameId
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily

internal data class FateChapterNodeModel(
    val chapter: BookChapterMeta,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
    val isUnlocked: Boolean,
    val decisionCount: Int,
    val latestDecisionLabel: String?,
    val medalLabel: String?
)

internal fun buildFateTreeChapterNodes(
    allChapters: List<BookChapterMeta>,
    seasonTab: Int,
    state: BookReadState,
    savedMiniGameRanks: Map<String, String>,
    savedBossMedals: Map<String, String>
): List<FateChapterNodeModel> {
    val highestUnlocked = (state.highestCompletedChapter + 1)
        .coerceAtMost(CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS)
    val seasonChapters = if (seasonTab == 1) {
        allChapters.filter { it.number <= 10 }
    } else {
        allChapters.filter { it.number >= 11 }
    }
    return seasonChapters.map { chapter ->
        val isCompleted = chapter.number in state.completedChapters
        val isCurrent = chapter.number == state.activeChapter
        val isUnlocked = chapter.number <= highestUnlocked || isCurrent || isCompleted
        val decisions = state.chapterDecisions[chapter.number].orEmpty()
        val chapterMedal = when (chapter.number) {
            2 -> savedMiniGameRanks[StoryMiniGameId.WHITE_EYE_RESONANCE.key]?.let { "👁 $it" }
            7 -> savedMiniGameRanks[StoryMiniGameId.BOSPHORUS_CORE_BALANCE.key]?.let { "🌀 $it" }
            9 -> savedMiniGameRanks[StoryMiniGameId.RED_EYE_WILLPOWER.key]?.let { "🔴 $it" }
            10 -> savedBossMedals[CosmicDuelBossId.VAELOR_ROOFTOP.name]?.let { "⚔ $it" }
            13 -> savedBossMedals[CosmicDuelBossId.MIRROR_ZERO.name]?.let { "⚔ $it" }
            24 -> savedBossMedals[CosmicDuelBossId.ULTRON_PRIME.name]?.let { "⚔ $it" }
            else -> null
        }
        FateChapterNodeModel(
            chapter = chapter,
            isCompleted = isCompleted,
            isCurrent = isCurrent,
            isUnlocked = isUnlocked,
            decisionCount = decisions.size,
            latestDecisionLabel = decisions.lastOrNull()?.selectedOption,
            medalLabel = chapterMedal
        )
    }
}

/**
 * 100% offline Visual Fate Tree & Medal Archive ("Görsel Kader Ağacı") for Kozmik Sürükleniş.
 * Displays discovered story branches, secrets, earned Boss Duel & Story Mini-Game S/A/B medals,
 * and a connected celestial timeline with 1-tap chapter jump.
 */
@Composable
fun BookFateTreeModal(
    state: BookReadState,
    savedMiniGameRanks: Map<String, String>,
    savedBossMedals: Map<String, String>,
    onJumpToChapter: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var seasonTab by remember { mutableIntStateOf(if (state.activeChapter >= 11) 2 else 1) }
    val branchSummary = remember(state.discoveredBranches, state.flags) {
        BookBranchDiscoveryHelper.compute(state)
    }
    val allChapters = remember { CosmicDriftBookRegistry.chapters }
    val chapterNodes = remember(
        allChapters,
        seasonTab,
        state.completedChapters,
        state.activeChapter,
        state.highestCompletedChapter,
        state.chapterDecisions,
        savedMiniGameRanks,
        savedBossMedals
    ) {
        buildFateTreeChapterNodes(
            allChapters = allChapters,
            seasonTab = seasonTab,
            state = state,
            savedMiniGameRanks = savedMiniGameRanks,
            savedBossMedals = savedBossMedals
        )
    }
    val totalMedalsEarned = remember(savedMiniGameRanks, savedBossMedals) {
        savedMiniGameRanks.size + savedBossMedals.size
    }
    val totalMedalsPossible = remember {
        StoryMiniGameId.entries.size + CosmicDuelBossId.entries.size
    }
    val completionFraction = remember(state.completedChapters.size) {
        (state.completedChapters.size.toFloat() / CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS.toFloat())
            .coerceIn(0f, 1f)
    }

    val listState = rememberLazyListState()
    val showTopFade by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 8
        }
    }
    val showBottomFade by remember(listState) {
        derivedStateOf { listState.canScrollForward }
    }
    val topFadeAlpha by animateFloatAsState(
        targetValue = if (showTopFade) 1f else 0f,
        animationSpec = tween(180),
        label = "fate_tree_top_fade"
    )
    val bottomFadeAlpha by animateFloatAsState(
        targetValue = if (showBottomFade) 1f else 0f,
        animationSpec = tween(180),
        label = "fate_tree_bottom_fade"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        ConfigureTrueFullscreenDialogWindow(hideSystemBars = false)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0A0419),
                            Color(0xFF13082B),
                            Color(0xFF0C0520),
                            Color(0xFF060211)
                        )
                    )
                )
                .testTag("book_fate_tree_modal")
        ) {
            // Atmospheric Celestial Nebula Backdrop
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x3D9333EA), Color(0x146D28D9), Color.Transparent),
                        center = Offset(size.width * 0.84f, size.height * 0.08f),
                        radius = size.width * 0.78f
                    ),
                    center = Offset(size.width * 0.84f, size.height * 0.08f),
                    radius = size.width * 0.78f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x2838BDF8), Color(0x0D1D4ED8), Color.Transparent),
                        center = Offset(size.width * 0.14f, size.height * 0.36f),
                        radius = size.width * 0.65f
                    ),
                    center = Offset(size.width * 0.14f, size.height * 0.36f),
                    radius = size.width * 0.65f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x24F43F5E), Color.Transparent),
                        center = Offset(size.width * 0.86f, size.height * 0.82f),
                        radius = size.width * 0.60f
                    ),
                    center = Offset(size.width * 0.86f, size.height * 0.82f),
                    radius = size.width * 0.60f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Sculpted Cosmic Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 16.dp, top = 12.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x28C084FC))
                                .border(0.75.dp, Color(0x66D8B4FE), RoundedCornerShape(50))
                                .padding(horizontal = 9.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFE9D5FF),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "GÖRSEL KADER AĞACI",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFE9D5FF),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.9.sp
                            )
                        }
                        Text(
                            text = "Rotalar, Sırlar & Madalyalar",
                            fontFamily = DisplaySerifFontFamily,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xCC28144D), Color(0xDD15092B))
                                )
                            )
                            .border(1.dp, Color(0x88D8B4FE), CircleShape)
                            .clickable(onClick = onDismiss)
                            .testTag("book_fate_tree_close"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Unified Frosted Telemetry Ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FateStatPill(
                        icon = Icons.Default.MenuBook,
                        label = "Bölüm",
                        value = "${state.completedChapters.size}/${CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS}",
                        accent = Color(0xFF38BDF8),
                        progress = completionFraction,
                        modifier = Modifier.weight(1f)
                    )
                    FateStatPill(
                        icon = Icons.Default.AccountTree,
                        label = "Rota & İz",
                        value = "${branchSummary.branchCount} · ${branchSummary.flagCount}",
                        accent = Color(0xFFC084FC),
                        progress = null,
                        modifier = Modifier.weight(1f)
                    )
                    FateStatPill(
                        icon = Icons.Default.EmojiEvents,
                        label = "Madalya",
                        value = "$totalMedalsEarned/$totalMedalsPossible",
                        accent = Color(0xFFFDE047),
                        progress = (totalMedalsEarned.toFloat() / totalMedalsPossible.coerceAtLeast(1).toFloat()),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sleek Segmented Season Capsule Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x99120729))
                        .border(0.9.dp, Color(0x44C4B5FD), RoundedCornerShape(50))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SeasonFilterChip(
                        label = "1. Sezon · I–X",
                        selected = seasonTab == 1,
                        accent = Color(0xFF38BDF8),
                        testTag = "fate_tree_season_1",
                        onClick = { seasonTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    SeasonFilterChip(
                        label = "2. Sezon · XI–XXIV",
                        selected = seasonTab == 2,
                        accent = Color(0xFFF43F5E),
                        testTag = "fate_tree_season_2",
                        onClick = { seasonTab = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                MorCamDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Scrollable Chronicle Content with Smooth Top & Bottom Feather Transitions
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalFadingEdge(
                            topFadeHeight = 22.dp,
                            bottomFadeHeight = 30.dp,
                            topFadeAlpha = topFadeAlpha,
                            bottomFadeAlpha = bottomFadeAlpha
                        ),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Trophy & Medal Vault Card
                    item(key = "medal_archive") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 18.dp)
                                .morCamCosmicGlow(
                                    cornerRadius = 22.dp,
                                    glowColor = Color(0xFFFDE047),
                                    alpha = 0.22f,
                                    blurDp = 16.dp
                                )
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xE81E113D),
                                            Color(0xEC13092B),
                                            Color(0xF20C061D)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            Color(0x99FDE047),
                                            Color(0x55C084FC),
                                            Color(0x4438BDF8)
                                        )
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x2EFDE047))
                                            .border(0.8.dp, Color(0x88FDE047), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = Color(0xFFFDE047),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = "MADALYA ARŞİVİ",
                                        fontFamily = InterFontFamily,
                                        color = Color(0xFFFDE047),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0x33A855F7))
                                        .border(0.75.dp, Color(0x66D8B4FE), RoundedCornerShape(50))
                                        .padding(horizontal = 9.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = branchSummary.explorerBadge,
                                        fontFamily = InterFontFamily,
                                        color = Color(0xFFE9D5FF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Row 1: 3 Boss Duels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MedalSlotTile(
                                    icon = Icons.Default.Shield,
                                    title = "Vaelor",
                                    sub = "Böl. 10",
                                    rank = savedBossMedals[CosmicDuelBossId.VAELOR_ROOFTOP.name],
                                    accent = Color(0xFFF43F5E),
                                    modifier = Modifier.weight(1f)
                                )
                                MedalSlotTile(
                                    icon = Icons.Default.Bolt,
                                    title = "Sıfır",
                                    sub = "Böl. 13",
                                    rank = savedBossMedals[CosmicDuelBossId.MIRROR_ZERO.name],
                                    accent = Color(0xFF38BDF8),
                                    modifier = Modifier.weight(1f)
                                )
                                MedalSlotTile(
                                    icon = Icons.Default.Whatshot,
                                    title = "Ultron Prime",
                                    sub = "Böl. 24",
                                    rank = savedBossMedals[CosmicDuelBossId.ULTRON_PRIME.name],
                                    accent = Color(0xFFEF4444),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Row 2: 3 Interactive Story Mini-Games
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MedalSlotTile(
                                    icon = Icons.Default.Visibility,
                                    title = "Beyaz Göz",
                                    sub = "Böl. 2",
                                    rank = savedMiniGameRanks[StoryMiniGameId.WHITE_EYE_RESONANCE.key],
                                    accent = Color(0xFFE0F2FE),
                                    modifier = Modifier.weight(1f)
                                )
                                MedalSlotTile(
                                    icon = Icons.Default.Waves,
                                    title = "Boğaz Çekirdeği",
                                    sub = "Böl. 7",
                                    rank = savedMiniGameRanks[StoryMiniGameId.BOSPHORUS_CORE_BALANCE.key],
                                    accent = Color(0xFF38BDF8),
                                    modifier = Modifier.weight(1f)
                                )
                                MedalSlotTile(
                                    icon = Icons.Default.AutoAwesome,
                                    title = "İrade Mührü",
                                    sub = "Böl. 9",
                                    rank = savedMiniGameRanks[StoryMiniGameId.RED_EYE_WILLPOWER.key],
                                    accent = Color(0xFFF43F5E),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Timeline Section Caption
                    item(key = "timeline_section_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (seasonTab == 1) "1. SEZON ZAMAN ÇİZGİSİ" else "2. SEZON ZAMAN ÇİZGİSİ",
                                fontFamily = InterFontFamily,
                                color = MorCamTokens.LavenderSoft,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0x66C084FC), Color.Transparent)
                                        )
                                    )
                            )
                        }
                    }

                    // Connected Celestial Vertical Branching Chapter Timeline
                    itemsIndexed(
                        items = chapterNodes,
                        key = { _, node -> "fate_ch_${node.chapter.number}" },
                        contentType = { _, _ -> "fate_chapter_node" }
                    ) { index, node ->
                        FateChapterNodeCard(
                            chapter = node.chapter,
                            isCompleted = node.isCompleted,
                            isCurrent = node.isCurrent,
                            isUnlocked = node.isUnlocked,
                            decisionCount = node.decisionCount,
                            latestDecisionLabel = node.latestDecisionLabel,
                            medalLabel = node.medalLabel,
                            isFirst = index == 0,
                            isLast = index == chapterNodes.lastIndex,
                            onJump = { onJumpToChapter(node.chapter.number) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FateStatPill(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    progress: Float?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xD81B0E38), Color(0xE0110725))
                )
            )
            .border(0.9.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = value,
                fontFamily = InterFontFamily,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
        Text(
            text = label,
            fontFamily = InterFontFamily,
            color = accent.copy(alpha = 0.92f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        if (progress != null) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .fillMaxWidth(0.78f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x33FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                )
            }
        }
    }
}

@Composable
private fun SeasonFilterChip(
    label: String,
    selected: Boolean,
    accent: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.34f), Color(0x887C3AED))
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .then(
                if (selected) {
                    Modifier.border(1.dp, accent.copy(alpha = 0.85f), RoundedCornerShape(50))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            color = if (selected) Color.White else Color(0xFFB4A3D8),
            fontSize = 11.5.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MedalSlotTile(
    icon: ImageVector,
    title: String,
    sub: String,
    rank: String?,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val earned = !rank.isNullOrBlank()
    val rankColor = when (rank) {
        "S" -> Color(0xFFFDE047)
        "A" -> Color(0xFF38BDF8)
        "B" -> Color(0xFFC084FC)
        else -> Color(0xFF7C6EA0)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (earned) {
                    Brush.verticalGradient(
                        listOf(rankColor.copy(alpha = 0.22f), Color(0xCC120826))
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color(0xAA160C2E), Color(0xCC0C061B))
                    )
                }
            )
            .border(
                width = if (earned) 1.dp else 0.75.dp,
                color = if (earned) rankColor.copy(alpha = 0.75f) else Color(0x36C4B5FD),
                shape = RoundedCornerShape(15.dp)
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Sculpted Crest Ring
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (earned) rankColor.copy(alpha = 0.20f) else Color(0x441B1038))
                .border(
                    width = 1.dp,
                    color = if (earned) rankColor else accent.copy(alpha = 0.30f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (earned) {
                Text(
                    text = rank!!,
                    fontFamily = DisplaySerifFontFamily,
                    color = rankColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent.copy(alpha = 0.52f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = title,
            fontFamily = InterFontFamily,
            color = if (earned) Color.White else Color(0xFFCBBCE8),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (earned) "$sub · ★$rank" else sub,
            fontFamily = InterFontFamily,
            color = if (earned) rankColor else accent.copy(alpha = 0.65f),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun FateChapterNodeCard(
    chapter: BookChapterMeta,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isUnlocked: Boolean,
    decisionCount: Int,
    latestDecisionLabel: String?,
    medalLabel: String?,
    isFirst: Boolean,
    isLast: Boolean,
    onJump: () -> Unit
) {
    val chapterAccent = Color(chapter.accentHex)
    val orbColor = when {
        isCurrent -> Color(0xFFFDE047)
        isCompleted -> Color(0xFF34D399)
        isUnlocked -> chapterAccent
        else -> Color(0xFF524A6E)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Connected Celestial Vertical Spine Column
        Column(
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Connector Line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(14.dp)
                    .background(
                        if (!isFirst) {
                            Brush.verticalGradient(
                                listOf(
                                    if (isUnlocked) orbColor.copy(alpha = 0.55f) else Color(0x337C6EA0),
                                    if (isUnlocked) orbColor.copy(alpha = 0.80f) else Color(0x337C6EA0)
                                )
                            )
                        } else {
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
            )

            // Glowing Node Orb
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .then(
                        if (isCurrent || isCompleted) {
                            Modifier.morCamCosmicGlow(
                                cornerRadius = 18.dp,
                                glowColor = orbColor,
                                alpha = if (isCurrent) 0.55f else 0.30f,
                                blurDp = 10.dp
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                orbColor.copy(alpha = if (isUnlocked) 0.36f else 0.16f),
                                Color(0xE0110725)
                            )
                        )
                    )
                    .border(
                        width = if (isCurrent) 1.6.dp else 1.2.dp,
                        color = orbColor.copy(alpha = if (isUnlocked) 0.95f else 0.55f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCurrent -> Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Aktif Bölüm",
                        tint = Color(0xFFFDE047),
                        modifier = Modifier.size(17.dp)
                    )
                    isCompleted -> Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Tamamlandı",
                        tint = Color(0xFF6EE7B7),
                        modifier = Modifier.size(17.dp)
                    )
                    isUnlocked -> Text(
                        text = chapter.romanNumeral,
                        fontFamily = DisplaySerifFontFamily,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    else -> Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Kilitli",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Bottom Connector Line stretching to the next node
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(
                        if (!isLast) {
                            Brush.verticalGradient(
                                listOf(
                                    if (isUnlocked) orbColor.copy(alpha = 0.72f) else Color(0x337C6EA0),
                                    Color(0x337C6EA0)
                                )
                            )
                        } else {
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
            )
        }

        // Frosted Glassmorphic Chapter Card
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .then(
                    if (isCurrent) {
                        Modifier.morCamCosmicGlow(
                            cornerRadius = 18.dp,
                            glowColor = Color(0xFFFDE047),
                            alpha = 0.24f,
                            blurDp = 14.dp
                        )
                    } else {
                        Modifier
                    }
                )
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isCurrent) {
                        Brush.linearGradient(listOf(Color(0xEC24114A), Color(0xF0150930)))
                    } else if (isUnlocked) {
                        Brush.linearGradient(listOf(Color(0xD6180B33), Color(0xE00F0622)))
                    } else {
                        Brush.linearGradient(listOf(Color(0x9E120A24), Color(0xB20A0516)))
                    }
                )
                .border(
                    width = if (isCurrent) 1.25.dp else 0.85.dp,
                    color = when {
                        isCurrent -> Color(0xCCFDE047)
                        isCompleted -> Color(0x7734D399)
                        isUnlocked -> chapterAccent.copy(alpha = 0.45f)
                        else -> Color(0x2EC4B5FD)
                    },
                    shape = RoundedCornerShape(18.dp)
                )
                .then(
                    if (isUnlocked) {
                        Modifier.clickable(onClick = onJump)
                    } else {
                        Modifier
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Eyebrow Row: Chapter Roman Numeral + Active/Medal Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "BÖLÜM ${chapter.romanNumeral} · ${chapter.sceneCount} SAHNE",
                        fontFamily = InterFontFamily,
                        color = if (isCurrent) Color(0xFFFDE047) else if (isUnlocked) chapterAccent.copy(alpha = 0.92f) else Color(0xFF7C7499),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0x33FDE047))
                                    .border(0.7.dp, Color(0x99FDE047), RoundedCornerShape(50))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AKTİF",
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFFDE047),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        if (medalLabel != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0x33FDE047))
                                    .border(0.75.dp, Color(0xAAFDE047), RoundedCornerShape(50))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = medalLabel,
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFFDE047),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // Serif Chapter Title
                Text(
                    text = chapter.title,
                    fontFamily = DisplaySerifFontFamily,
                    color = if (isUnlocked) Color.White else Color(0xFF9489B3),
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Decision / Status Subtitle
                Text(
                    text = when {
                        latestDecisionLabel != null -> "Son iz ($decisionCount karar): $latestDecisionLabel"
                        isCurrent -> "Şu an okunan bölüm — kaldığın yerden devam et"
                        isCompleted -> "Bölüm tamamlandı — yeniden keşfedebilirsin"
                        isUnlocked -> "Keşfe hazır — yeni rotaları aç"
                        else -> "Önceki bölümleri tamamlayarak kilidi aç"
                    },
                    fontFamily = InterFontFamily,
                    color = if (isUnlocked) Color(0xFFCBBBE8) else Color(0xFF7B7099),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (isUnlocked) {
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .heightIn(min = 38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                if (isCurrent) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF6D28D9), Color(0xFF9333EA))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(chapterAccent.copy(alpha = 0.24f), Color(0x447C3AED))
                                    )
                                }
                            )
                            .border(
                                width = 0.9.dp,
                                color = if (isCurrent) Color(0xCCFDE047) else chapterAccent.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(11.dp)
                            )
                            .clickable(onClick = onJump)
                            .testTag("fate_tree_jump_ch_${chapter.number}")
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isCurrent) "Okumaya Devam Et" else "Bölüme Atla",
                            fontFamily = InterFontFamily,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
