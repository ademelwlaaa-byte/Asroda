package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.components.morCamCosmicGlow
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
 * and a vertical branching timeline with 1-tap chapter jump.
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        ConfigureTrueFullscreenDialogWindow()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0A0518), Color(0xFF120826), Color(0xFF070312))
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("book_fate_tree_modal")
        ) {
            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "✦ GÖRSEL KADER AĞACI · ÇEVRİMDIŞI",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFC084FC),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Rotalar, Sırlar & Madalyalar",
                        fontFamily = DisplaySerifFontFamily,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC1E1038))
                        .border(1.dp, Color(0x99C4B5FD), CircleShape)
                        .clickable { onDismiss() }
                        .testTag("book_fate_tree_close"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Summary Stats Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FateStatPill(
                    label = "Bölüm",
                    value = "${state.completedChapters.size}/${CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS}",
                    accent = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                FateStatPill(
                    label = "Rota & İz",
                    value = "${branchSummary.branchCount} · ${branchSummary.flagCount}",
                    accent = Color(0xFFC084FC),
                    modifier = Modifier.weight(1f)
                )
                FateStatPill(
                    label = "Madalya",
                    value = "$totalMedalsEarned/$totalMedalsPossible",
                    accent = Color(0xFFFDE047),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Season Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SeasonFilterChip(
                    label = "1. SEZON (Bölüm I–X)",
                    selected = seasonTab == 1,
                    accent = Color(0xFF38BDF8),
                    testTag = "fate_tree_season_1",
                    onClick = { seasonTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                SeasonFilterChip(
                    label = "2. SEZON (Bölüm XI–XXIV)",
                    selected = seasonTab == 2,
                    accent = Color(0xFFF43F5E),
                    testTag = "fate_tree_season_2",
                    onClick = { seasonTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Trophy & Medal Showcase Card
                item(key = "medal_archive") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .morCamCosmicGlow(cornerRadius = 18.dp, glowColor = Color(0xFFFDE047), alpha = 0.25f, blurDp = 14.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xE8130B28))
                            .border(1.dp, Color(0x66FDE047), RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏆 BOSS & MİNİ OYUN MADALYALARI",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFFDE047),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = branchSummary.explorerBadge,
                                fontFamily = InterFontFamily,
                                color = Color(0xFFD8B4FE),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Row 1: 3 Boss Duels
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MedalSlotTile(
                                title = "Vaelor",
                                sub = "Böl. 10",
                                rank = savedBossMedals[CosmicDuelBossId.VAELOR_ROOFTOP.name],
                                accent = Color(0xFFF43F5E),
                                modifier = Modifier.weight(1f)
                            )
                            MedalSlotTile(
                                title = "Sıfır",
                                sub = "Böl. 13",
                                rank = savedBossMedals[CosmicDuelBossId.MIRROR_ZERO.name],
                                accent = Color(0xFF38BDF8),
                                modifier = Modifier.weight(1f)
                            )
                            MedalSlotTile(
                                title = "Ultron Prime",
                                sub = "Böl. 24",
                                rank = savedBossMedals[CosmicDuelBossId.ULTRON_PRIME.name],
                                accent = Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2: 3 Interactive Story Mini-Games
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MedalSlotTile(
                                title = "Beyaz Göz",
                                sub = "Böl. 2",
                                rank = savedMiniGameRanks[StoryMiniGameId.WHITE_EYE_RESONANCE.key],
                                accent = Color(0xFFE0F2FE),
                                modifier = Modifier.weight(1f)
                            )
                            MedalSlotTile(
                                title = "Boğaz Çekirdeği",
                                sub = "Böl. 7",
                                rank = savedMiniGameRanks[StoryMiniGameId.BOSPHORUS_CORE_BALANCE.key],
                                accent = Color(0xFF38BDF8),
                                modifier = Modifier.weight(1f)
                            )
                            MedalSlotTile(
                                title = "İrade Mührü",
                                sub = "Böl. 9",
                                rank = savedMiniGameRanks[StoryMiniGameId.RED_EYE_WILLPOWER.key],
                                accent = Color(0xFFF43F5E),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Vertical Branching Chapter Timeline (memoized node projection)
                items(
                    items = chapterNodes,
                    key = { "fate_ch_${it.chapter.number}" },
                    contentType = { "fate_chapter_node" }
                ) { node ->
                    FateChapterNodeCard(
                        chapter = node.chapter,
                        isCompleted = node.isCompleted,
                        isCurrent = node.isCurrent,
                        isUnlocked = node.isUnlocked,
                        decisionCount = node.decisionCount,
                        latestDecisionLabel = node.latestDecisionLabel,
                        medalLabel = node.medalLabel,
                        onJump = { onJumpToChapter(node.chapter.number) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FateStatPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xD8150B2E))
            .border(0.9.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = InterFontFamily,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            fontFamily = InterFontFamily,
            color = accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
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
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(50))
            .background(if (selected) accent.copy(alpha = 0.25f) else Color(0x99120826))
            .border(1.dp, if (selected) accent else Color(0x44C4B5FD), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            color = if (selected) Color.White else Color(0xFFB4A3D8),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MedalSlotTile(
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
        else -> Color(0xFF64748B)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (earned) rankColor.copy(alpha = 0.14f) else Color(0x880B061A))
            .border(0.9.dp, if (earned) rankColor.copy(alpha = 0.72f) else Color(0x3394A3B8), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = if (earned) "★ $rank" else "—",
            fontFamily = InterFontFamily,
            color = rankColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = title,
            fontFamily = InterFontFamily,
            color = if (earned) Color.White else Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = sub,
            fontFamily = InterFontFamily,
            color = accent.copy(alpha = if (earned) 0.9f else 0.5f),
            fontSize = 9.sp,
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
    onJump: () -> Unit
) {
    val chapterAccent = Color(chapter.accentHex)
    val orbColor = when {
        isCurrent -> Color(0xFFFDE047)
        isCompleted -> Color(0xFF34D399)
        isUnlocked -> chapterAccent
        else -> Color(0xFF475569)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Cosmic Spine Orb
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(orbColor.copy(alpha = 0.22f))
                .border(1.4.dp, orbColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    isCurrent -> "★"
                    isCompleted -> "✓"
                    isUnlocked -> chapter.romanNumeral
                    else -> "🔒"
                },
                fontFamily = InterFontFamily,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isCurrent) Color(0xEC1B0E3B) else Color(0xCC110824))
                .border(
                    width = if (isCurrent) 1.3.dp else 0.85.dp,
                    color = if (isCurrent) Color(0xFFFDE047) else orbColor.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BÖLÜM ${chapter.romanNumeral} · ${chapter.title}",
                    fontFamily = DisplaySerifFontFamily,
                    color = if (isUnlocked) Color.White else Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (medalLabel != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x33FDE047))
                            .border(0.8.dp, Color(0xAAFDE047), RoundedCornerShape(50))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = medalLabel,
                            fontFamily = InterFontFamily,
                            color = Color(0xFFFDE047),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Text(
                text = when {
                    latestDecisionLabel != null -> "Son iz ($decisionCount karar): $latestDecisionLabel"
                    isCurrent -> "Şu an okunan bölüm · ${chapter.sceneCount} sahne"
                    isCompleted -> "Tamamlandı · ${chapter.sceneCount} sahne"
                    isUnlocked -> "Keşfe hazır · ${chapter.sceneCount} sahne"
                    else -> "Önceki bölümleri tamamlayarak aç"
                },
                fontFamily = InterFontFamily,
                color = Color(0xFFBBAAD9),
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (isUnlocked) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .heightIn(min = 38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(chapterAccent.copy(alpha = 0.22f))
                        .border(0.9.dp, chapterAccent.copy(alpha = 0.72f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onJump)
                        .testTag("fate_tree_jump_ch_${chapter.number}")
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCurrent) "Okumaya Devam Et ▶" else "Bölüme Atla ▶",
                        fontFamily = InterFontFamily,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
