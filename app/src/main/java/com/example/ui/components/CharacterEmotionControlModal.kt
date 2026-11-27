@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BotEntity
import com.example.data.repository.KeyCharacter
import com.example.ui.animation.CosmicAnimatedCounter
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.CosmicOrbLoader
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val EMOTION_STUDIO_TR_LOCALE: Locale = Locale.forLanguageTag("tr-TR")
private val WHITESPACE_SPLIT_REGEX = Regex("""\s+""")

private val EmotionStudioSections = listOf(
    "mood" to "Ruh Hali",
    "relations" to "İlişki",
    "primary" to "Duygular",
    "custom" to "Özel",
    "history" to "Geçmiş"
)
private val EmotionStudioSectionOrder = EmotionStudioSections.map { it.first }
private val EmotionStudioPresetMoods = listOf(
    "nötr", "mutlu", "flörtöz", "kıskanç", "utangaç", "gergin",
    "tutkulu", "kırgın", "sevecen", "soğuk", "heyecanlı", "sakin"
)
private val EmotionStudioTensionOptions = listOf(
    "none" to "Sakin",
    "mild" to "Hafif",
    "conflict" to "Çatışma",
    "crisis" to "Kriz"
)
private val EmotionStudioPrimaryColors = listOf(
    Color(0xFFE9D5FF),
    Color(0xFFA5B4FC),
    MorCamTokens.DestructiveRimAccent,
    Color(0xFFA78BFA),
    Color(0xFFC084FC),
    Color(0xFFF0ABFC),
    Color(0xFFD8B4FE)
)
private val EmotionStudioDifficulties = listOf("Kolay", "Orta", "Zor", "Aşırı Zor")
private val EmotionPillShape = RoundedCornerShape(50)
private val EmotionInnerCardShape = RoundedCornerShape(14.dp)
private val EmotionHistoryItemShape = RoundedCornerShape(12.dp)
private val RelationTierBgBrush = Brush.horizontalGradient(listOf(Color(0x995B21B6), Color(0x883B1470)))
private val CustomEmotionCardBgBrush = Brush.verticalGradient(listOf(Color(0x9E210F44), Color(0xB013082B)))

internal fun relationshipTierLabel(affection: Int): String = when (affection.coerceIn(0, 100)) {
    in 0..15 -> "Düşman"
    in 16..35 -> "Yabancı"
    in 36..55 -> "Tanıdık"
    in 56..70 -> "Arkadaş"
    in 71..85 -> "Flört"
    in 86..95 -> "Sevgili"
    else -> "Ruh Eşi"
}

internal fun emotionHistoryEmoji(dominantEmotion: String): String {
    val norm = dominantEmotion.trim().lowercase(EMOTION_STUDIO_TR_LOCALE)
    return when {
        norm in setOf("mutlu", "neşeli", "joy", "heyecanlı") -> "😄"
        norm in setOf("üzgün", "hüzünlü", "kırgın", "sad") -> "😢"
        norm in setOf("kızgın", "öfkeli", "gergin", "anger", "tense") -> "😡"
        norm.startsWith("flört") || norm in setOf("tutkulu", "sevecen", "warm") -> "💕"
        norm in setOf("sakin", "nötr", "calm") -> "😌"
        else -> "✦"
    }
}

internal fun formatTurkishEmotionLabel(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return "Nötr"
    return trimmed.replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase(EMOTION_STUDIO_TR_LOCALE) else ch.toString()
    }
}

internal fun sanitizeCustomEmotionRange(
    rawMin: String,
    rawMax: String,
    rawCurrent: String
): Triple<Int, Int, Int> {
    val minVal = (rawMin.trim().toIntOrNull() ?: 0).coerceIn(-1000, 9999)
    val parsedMax = (rawMax.trim().toIntOrNull() ?: 100).coerceIn(-999, 10000)
    val maxVal = parsedMax.coerceAtLeast(minVal + 1)
    val curVal = (rawCurrent.trim().toIntOrNull() ?: minVal).coerceIn(minVal, maxVal)
    return Triple(minVal, maxVal, curVal)
}

/**
 * Merges all known story characters for the Emotion Control Menu from:
 * 1) persisted [characterEmotions],
 * 2) explicit [keyCharacters],
 * 3) [bot.keyCharactersJson] fallback, and
 * 4) non-blacklisted [castMembers] (both manual & auto-detected).
 *
 * Deduplicates case-insensitively and preserves display order.
 */
fun collectEmotionStudioCharacters(
    bot: BotEntity,
    characterEmotions: List<com.example.data.local.CharacterEmotionEntity>,
    keyCharacters: List<KeyCharacter>,
    castMembers: List<com.example.data.local.CastMemberEntity>
): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    val seenLower = mutableSetOf<String>()
    val mainLower = bot.aiName.trim().lowercase(EMOTION_STUDIO_TR_LOCALE)
    val userLower = bot.userCharName.trim().lowercase(EMOTION_STUDIO_TR_LOCALE)

    fun tryAdd(rawName: String, desc: String = "", allowUserName: Boolean = false, bypassExtraFilter: Boolean = false) {
        val clean = rawName.trim()
        if (clean.isBlank()) return
        if (!bypassExtraFilter && (
                com.example.data.repository.CastDetectorEngine.isGenericBackgroundExtra(clean, desc) ||
                    com.example.data.repository.CastDetectorEngine.isNonPersonPlaceOrOrganization(clean)
                )
        ) {
            return
        }
        val lowerKey = clean.lowercase(EMOTION_STUDIO_TR_LOCALE)
        if (lowerKey == mainLower) return
        if (!allowUserName && userLower.isNotBlank() && lowerKey == userLower) return

        // Deduplicate alias pairs (e.g., "Natasha" & "Natasha Romanoff"), keeping the fuller canonical name!
        val aliasIdx = result.indexOfFirst { (existingName, _) ->
            existingName.equals(clean, ignoreCase = true) ||
                com.example.data.repository.CastDetectorEngine.isLikelyAlias(clean, existingName)
        }
        if (aliasIdx >= 0) {
            val (existingName, existingDesc) = result[aliasIdx]
            val preferNew = clean.split(WHITESPACE_SPLIT_REGEX).size > existingName.split(WHITESPACE_SPLIT_REGEX).size
            if (preferNew) {
                result[aliasIdx] = clean to desc.trim().ifBlank { existingDesc }
                seenLower.add(lowerKey)
            } else if (existingDesc.isBlank() && desc.isNotBlank()) {
                result[aliasIdx] = existingName to desc.trim()
            }
            return
        }

        if (seenLower.add(lowerKey)) {
            result.add(clean to desc.trim())
        }
    }

    characterEmotions.forEach { ce ->
        tryAdd(ce.characterName, allowUserName = true)
    }

    keyCharacters.forEach { kc ->
        tryAdd(kc.name, kc.desc, bypassExtraFilter = true)
    }

    if (bot.keyCharactersJson.isNotBlank() && bot.keyCharactersJson.trim() != "[]") {
        try {
            val arr = org.json.JSONArray(bot.keyCharactersJson)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                tryAdd(obj.optString("name", ""), obj.optString("desc", ""), bypassExtraFilter = true)
            }
        } catch (_: Exception) {
        }
    }

    castMembers
        .filter { cm ->
            if (cm.isBlacklisted) return@filter false
            if (!cm.isAutoAdded) return@filter true
            val isSignificant = !cm.pendingUserConfirmation || cm.appearanceCount >= 2 || cm.importanceScore >= 75
            isSignificant && !com.example.data.repository.CastDetectorEngine.isGenericBackgroundExtra(cm.name, cm.role)
        }
        .forEach { cm ->
            val combinedDesc = listOf(cm.role, cm.description).filter { it.isNotBlank() }.joinToString(" — ")
            tryAdd(cm.name, combinedDesc, bypassExtraFilter = !cm.isAutoAdded)
        }

    return result
}

@Composable
private fun EmotionStudioSectionBox(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    MorCamGlassCard(
        cornerRadius = 18.dp,
        highlighted = false,
        paddingHorizontal = 14.dp,
        paddingVertical = 14.dp
    ) {
        Text(
            text = title,
            fontFamily = DisplaySerifFontFamily,
            color = MorCamTokens.SilverLavenderBright,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = InterFontFamily,
                color = MorCamTokens.LavenderMuted,
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun EmotionStudioSliderRow(
    label: String,
    value: Int,
    accentColor: Color,
    onChange: (Int) -> Unit
) {
    val clamped = value.coerceIn(0, 100)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = LiterarySerifFontFamily,
                color = MorCamTokens.LavenderLight,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .clip(EmotionPillShape)
                    .background(accentColor.copy(alpha = 0.22f))
                    .border(0.8.dp, accentColor.copy(alpha = 0.65f), EmotionPillShape)
                    .padding(horizontal = 9.dp, vertical = 2.dp)
            ) {
                CosmicAnimatedCounter(
                    count = clamped,
                    prefix = "%",
                    color = MorCamTokens.SilverLavenderBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        MorCamGlassSlider(
            value = clamped.toFloat(),
            onValueChange = { raw ->
                val next = raw.roundToInt().coerceIn(0, 100)
                if (next != clamped) onChange(next)
            },
            valueRange = 0f..100f,
            accentColor = accentColor,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CharacterEmotionControlModal(
    bot: BotEntity,
    characterEmotions: List<com.example.data.local.CharacterEmotionEntity> = emptyList(),
    keyCharacters: List<KeyCharacter> = emptyList(),
    castMembers: List<com.example.data.local.CastMemberEntity> = emptyList(),
    initialSelectedCharacter: String = "main",
    onDismiss: () -> Unit,
    onSaveBotEmotion: (String) -> Unit,
    onSaveCharacterEmotionState: (String, String) -> Unit
) {
    var selectedTab by remember(bot.id, initialSelectedCharacter) { mutableStateOf(initialSelectedCharacter) }
    var activeSection by remember { mutableStateOf("mood") }
    val viewModel: com.example.ui.viewmodel.EmochiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    var emotionHistory by remember(bot.id) { mutableStateOf<List<com.example.data.local.EmotionHistoryEntity>>(emptyList()) }
    var historyLoaded by remember(bot.id) { mutableStateOf(false) }
    var loadedCastMembers by remember(bot.id) { mutableStateOf<List<com.example.data.local.CastMemberEntity>>(emptyList()) }
    val historyDateFormat = remember { SimpleDateFormat("dd MMM HH:mm", EMOTION_STUDIO_TR_LOCALE) }

    LaunchedEffect(bot.id) {
        loadedCastMembers = try {
            viewModel.getCastMembersForBot(bot.id).filter { !it.isBlacklisted }
        } catch (_: Exception) {
            emptyList()
        }
    }

    val effectiveCast = remember(castMembers, loadedCastMembers) {
        if (loadedCastMembers.isNotEmpty()) loadedCastMembers else castMembers
    }

    val mergedSideCharacters = remember(bot.id, bot.keyCharactersJson, characterEmotions, keyCharacters, effectiveCast) {
        collectEmotionStudioCharacters(
            bot = bot,
            characterEmotions = characterEmotions,
            keyCharacters = keyCharacters,
            castMembers = effectiveCast
        )
    }

    val characterStatesMap = remember(bot.emotionState, characterEmotions, mergedSideCharacters) {
        val map = linkedMapOf<String, com.example.data.local.EmotionState>()
        map["main"] = com.example.data.local.EmotionState.fromJson(bot.emotionState)
        val existingByLower = characterEmotions.associateBy { it.characterName.trim().lowercase(EMOTION_STUDIO_TR_LOCALE) }
        mergedSideCharacters.forEach { (name, desc) ->
            val entity = existingByLower[name.lowercase(EMOTION_STUDIO_TR_LOCALE)]
            map[name] = if (entity != null) {
                com.example.data.local.EmotionState.fromJson(entity.emotionState)
            } else {
                com.example.data.local.EmotionState.calculateBaselineEmotionState(bot.aiName, name, desc)
            }
        }
        mutableStateMapOf<String, com.example.data.local.EmotionState>().apply { putAll(map) }
    }

    LaunchedEffect(characterStatesMap.keys.toList()) {
        if (selectedTab != "main" && !characterStatesMap.containsKey(selectedTab)) {
            selectedTab = "main"
        }
    }

    val curState = characterStatesMap[selectedTab] ?: com.example.data.local.EmotionState()

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var newCustomName by remember { mutableStateOf("") }
    var newCustomDifficulty by remember { mutableStateOf("Orta") }
    var newCustomMin by remember { mutableStateOf("0") }
    var newCustomMax by remember { mutableStateOf("100") }
    var newCustomCurrent by remember { mutableStateOf("50") }
    var newCustomPurpose by remember { mutableStateOf("") }

    LaunchedEffect(activeSection, bot.id) {
        if (activeSection == "history" && !historyLoaded) {
            try {
                emotionHistory = viewModel.getRecentEmotionHistory(bot.id, 30)
            } catch (_: Exception) {
                emotionHistory = emptyList()
            } finally {
                historyLoaded = true
            }
        }
    }

    val allCharTabs = remember(mergedSideCharacters) {
        listOf("main") + mergedSideCharacters.map { it.first }
    }

    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = "Duygu & İlişki Stüdyosu",
        subtitle = "Tüm karakterler için anlık duygu ve bağ kontrolü",
        icon = Icons.Default.Psychology,
        maxWidthFraction = 0.94f
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Character Tabs
            Text(
                text = "Karakterler (${allCharTabs.size})",
                fontFamily = InterFontFamily,
                color = MorCamTokens.LavenderMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allCharTabs.forEach { tabKey ->
                    key(tabKey) {
                        MorCamChipPill(
                            label = if (tabKey == "main") bot.aiName else tabKey,
                            selected = selectedTab == tabKey,
                            icon = if (tabKey == "main") Icons.Default.AutoAwesome else Icons.Default.Person,
                            onClick = { selectedTab = tabKey }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Section Nav Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EmotionStudioSections.forEach { (secId, label) ->
                    MorCamChipPill(
                        label = label,
                        selected = activeSection == secId,
                        onClick = { activeSection = secId }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            val motionConfig = LocalCosmicMotionConfig.current

            AnimatedContent(
                targetState = activeSection,
                transitionSpec = {
                    val fromIdx = EmotionStudioSectionOrder.indexOf(initialState).coerceAtLeast(0)
                    val toIdx = EmotionStudioSectionOrder.indexOf(targetState).coerceAtLeast(0)
                    CosmicMotionSpecs.cosmicTabTransition(forward = toIdx >= fromIdx, config = motionConfig)
                },
                label = "emotionStudioSection"
            ) { secKey ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (secKey) {
                        "mood" -> {
                            EmotionStudioSectionBox(
                                title = "Baskın Ruh Hali",
                                subtitle = "Karakterin anlık duygusal tonunu ve konuşma üslubunu ayarla"
                            ) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    EmotionStudioPresetMoods.forEach { m ->
                                        MorCamChipPill(
                                            label = m,
                                            selected = curState.dominantEmotion.trim().lowercase(EMOTION_STUDIO_TR_LOCALE) == m,
                                            onClick = {
                                                characterStatesMap[selectedTab] = curState.copy(dominantEmotion = m)
                                            }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                MorCamGlassTextField(
                                    value = curState.dominantEmotion,
                                    onValueChange = { characterStatesMap[selectedTab] = curState.copy(dominantEmotion = it) },
                                    placeholder = "Özel ruh hali girin..."
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("İkincil Duygu", fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 11.sp)
                                        Spacer(Modifier.height(4.dp))
                                        MorCamGlassTextField(
                                            value = curState.computedSecondaryEmotion ?: "",
                                            onValueChange = { characterStatesMap[selectedTab] = curState.copy(computedSecondaryEmotion = it.ifBlank { null }) },
                                            placeholder = "ör. minnettar"
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Bastırılmış Duygu", fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 11.sp)
                                        Spacer(Modifier.height(4.dp))
                                        MorCamGlassTextField(
                                            value = curState.suppressedEmotion,
                                            onValueChange = { characterStatesMap[selectedTab] = curState.copy(suppressedEmotion = it) },
                                            placeholder = "ör. çekingenlik"
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text("Gerginlik Düzeyi", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.SilverLavenderBright, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    EmotionStudioTensionOptions.forEach { (tensionKey, label) ->
                                        MorCamChipPill(
                                            label = label,
                                            selected = curState.tension.equals(tensionKey, ignoreCase = true),
                                            onClick = { characterStatesMap[selectedTab] = curState.copy(tension = tensionKey) }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text("Konuşma Üslubu", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.SilverLavenderBright, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                MorCamGlassTextField(
                                    value = curState.speechPattern,
                                    onValueChange = { characterStatesMap[selectedTab] = curState.copy(speechPattern = it) },
                                    placeholder = "ör. utangaç, kısık sesli, hızlı konuşan..."
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Duygusal Direnç", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderLight, fontSize = 12.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(EmotionPillShape)
                                            .background(Color(0x45A855F7))
                                            .border(0.8.dp, MorCamTokens.SubtleRimGradient, EmotionPillShape)
                                            .padding(horizontal = 9.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "${curState.resilience}/10",
                                            fontFamily = InterFontFamily,
                                            color = MorCamTokens.SilverLavenderBright,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                MorCamGlassSlider(
                                    value = curState.resilience.toFloat().coerceIn(1f, 10f),
                                    onValueChange = { raw ->
                                        val next = raw.roundToInt().coerceIn(1, 10)
                                        if (next != curState.resilience) {
                                            characterStatesMap[selectedTab] = curState.copy(resilience = next)
                                        }
                                    },
                                    valueRange = 1f..10f,
                                    steps = 8,
                                    accentColor = MorCamTokens.AmethystGlow,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        "relations" -> {
                            EmotionStudioSectionBox(
                                title = "İlişki & Bağ Parametreleri",
                                subtitle = "Yakınlık, güven, kırgınlık ve bağlılık eksenleri"
                            ) {
                                val aff = curState.affection
                                val tierLabel = relationshipTierLabel(aff)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(EmotionInnerCardShape)
                                        .background(RelationTierBgBrush)
                                        .border(BorderStroke(1.dp, MorCamTokens.RimGradient), EmotionInnerCardShape)
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("İlişki Kademesi", fontFamily = InterFontFamily, color = MorCamTokens.LavenderLight, fontSize = 12.sp)
                                    Text(tierLabel, fontFamily = DisplaySerifFontFamily, color = MorCamTokens.SilverLavenderBright, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(12.dp))
                                EmotionStudioSliderRow("Sevgi & Yakınlık", aff, Color(0xFFE879F9)) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(relationshipAxes = curState.relationshipAxes.copy(affectionScore = v))
                                }
                                EmotionStudioSliderRow("Güven Seviyesi", curState.trust, Color(0xFFC4B5FD)) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = curState.primaryEmotions.copy(trust = v))
                                }
                                EmotionStudioSliderRow("Fiziksel Yakınlık", curState.physicalComfortScore, MorCamTokens.LavenderSoft) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(physicalComfortScore = v)
                                }
                                EmotionStudioSliderRow("Kırgınlık / Mesafe", curState.hurt, Color(0xFF818CF8)) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(relationshipAxes = curState.relationshipAxes.copy(resentmentScore = v))
                                }
                                EmotionStudioSliderRow("Takıntı / Bağımlılık", curState.obsession, MorCamTokens.DestructiveRimAccent) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(obsessionScore = v)
                                }
                            }
                        }

                        "primary" -> {
                            EmotionStudioSectionBox(
                                title = "Temel Duygular (Plutchik)",
                                subtitle = "İçsel duygu ağırlıklarını ince ayarla"
                            ) {
                                val p = curState.primaryEmotions
                                EmotionStudioSliderRow("Neşe / Mutluluk", p.joy, EmotionStudioPrimaryColors[0]) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(joy = v))
                                }
                                EmotionStudioSliderRow("Üzüntü / Keder", p.sadness, EmotionStudioPrimaryColors[1]) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(sadness = v))
                                }
                                EmotionStudioSliderRow("Öfke / Kızgınlık", p.anger, EmotionStudioPrimaryColors[2]) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(anger = v))
                                }
                                EmotionStudioSliderRow("Korku / Endişe", p.fear, EmotionStudioPrimaryColors[3]) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(fear = v))
                                }
                                EmotionStudioSliderRow("Tiksinme", p.disgust, EmotionStudioPrimaryColors[4]) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(disgust = v))
                                }
                                EmotionStudioSliderRow("Şaşkınlık", p.surprise, EmotionStudioPrimaryColors[5]) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(surprise = v))
                                }
                                EmotionStudioSliderRow("Beklenti / Heyecan", p.anticipation, EmotionStudioPrimaryColors[6]) { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(anticipation = v))
                                }
                            }
                        }

                        "custom" -> {
                            EmotionStudioSectionBox(
                                title = "Karaktere Özel Duygular",
                                subtitle = "Kendi özel ilişki sayaçlarını tanımla"
                            ) {
                                val customList = curState.customEmotions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${customList.size} özel duygu", fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 12.sp)
                                    MorCamChipPill(
                                        label = "Yeni Ekle",
                                        selected = true,
                                        icon = Icons.Default.Add,
                                        onClick = { showAddCustomDialog = true }
                                    )
                                }

                                if (customList.isEmpty()) {
                                    Spacer(Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(EmotionInnerCardShape)
                                            .background(Color(0x75120728))
                                            .border(0.85.dp, MorCamTokens.SubtleRimGradient, EmotionInnerCardShape)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Henüz özel duygu eklenmedi.\nSadakat, Şüphe veya Saygı gibi sayaçlar ekleyebilirsin.",
                                            fontFamily = InterFontFamily,
                                            color = MorCamTokens.LavenderMuted,
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Spacer(Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        customList.forEachIndexed { index, ce ->
                                            key("${ce.name}_$index") {
                                                val safeMax = ce.maxValue.coerceAtLeast(ce.minValue + 1)
                                                val clampedCur = ce.currentValue.coerceIn(ce.minValue, safeMax)
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(EmotionInnerCardShape)
                                                        .background(CustomEmotionCardBgBrush)
                                                        .border(0.95.dp, MorCamTokens.SubtleRimGradient, EmotionInnerCardShape)
                                                        .padding(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text(
                                                                text = ce.name,
                                                                fontFamily = LiterarySerifFontFamily,
                                                                color = MorCamTokens.SilverLavenderBright,
                                                                fontSize = 13.5.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(EmotionPillShape)
                                                                    .background(Color(0x45A855F7))
                                                                    .border(0.7.dp, MorCamTokens.SubtleRimGradient, EmotionPillShape)
                                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = ce.difficulty,
                                                                    fontFamily = InterFontFamily,
                                                                    color = MorCamTokens.LavenderLight,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                            }
                                                        }
                                                        MorCamCircleIconButton(
                                                            icon = Icons.Default.Delete,
                                                            contentDescription = "Sil",
                                                            onClick = {
                                                                if (index in customList.indices) {
                                                                    val updated = customList.toMutableList().apply { removeAt(index) }
                                                                    characterStatesMap[selectedTab] = curState.copy(
                                                                        customEmotionsJson = com.example.data.local.CustomEmotionDefinition.listToJsonArrayStr(updated)
                                                                    )
                                                                }
                                                            },
                                                            size = 36.dp,
                                                            isDestructive = true
                                                        )
                                                    }
                                                    if (ce.purpose.isNotBlank()) {
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(ce.purpose, fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 11.sp)
                                                    }
                                                    Spacer(Modifier.height(6.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text("${ce.minValue}–$safeMax", fontFamily = InterFontFamily, color = MorCamTokens.LavenderSubtle, fontSize = 10.5.sp)
                                                        Spacer(Modifier.weight(1f))
                                                        Text("$clampedCur", fontFamily = InterFontFamily, color = MorCamTokens.LavenderLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    MorCamGlassSlider(
                                                        value = clampedCur.toFloat(),
                                                        onValueChange = { v ->
                                                            val next = v.roundToInt().coerceIn(ce.minValue, safeMax)
                                                            if (next != ce.currentValue && index in customList.indices) {
                                                                val updatedItem = ce.copy(currentValue = next)
                                                                val updated = customList.toMutableList().apply { set(index, updatedItem) }
                                                                characterStatesMap[selectedTab] = curState.copy(
                                                                    customEmotionsJson = com.example.data.local.CustomEmotionDefinition.listToJsonArrayStr(updated)
                                                                )
                                                            }
                                                        },
                                                        valueRange = ce.minValue.toFloat()..safeMax.toFloat(),
                                                        accentColor = MorCamTokens.AmethystGlow,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "history" -> {
                            EmotionStudioSectionBox(
                                title = "Duygu & İlişki Geçmişi",
                                subtitle = "Sohbet boyunca sevgi ve güven eğilimi"
                            ) {
                                if (!historyLoaded) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                        CosmicOrbLoader(size = 38.dp)
                                    }
                                } else if (emotionHistory.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(EmotionInnerCardShape)
                                            .background(Color(0x75120728))
                                            .border(0.85.dp, MorCamTokens.SubtleRimGradient, EmotionInnerCardShape)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Henüz duygu geçmişi yok.\nSohbet ilerledikçe değişimler burada görünecek.",
                                            fontFamily = InterFontFamily,
                                            color = MorCamTokens.LavenderMuted,
                                            fontSize = 11.5.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    val recentAff = remember(emotionHistory) {
                                        emotionHistory.filter { it.characterName == null }.takeLast(20)
                                    }
                                    val recentEvents = remember(emotionHistory) {
                                        emotionHistory.takeLast(8).reversed()
                                    }
                                    if (recentAff.size >= 2) {
                                        val firstAff = recentAff.first().affectionScore
                                        val lastAff = recentAff.last().affectionScore
                                        val delta = lastAff - firstAff

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Son ${recentAff.size} kayıt", fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 11.sp)
                                            Box(
                                                modifier = Modifier
                                                    .clip(EmotionPillShape)
                                                    .background(if (delta >= 0) Color(0x558B5CF6) else Color(0x556B163A))
                                                    .border(
                                                        0.85.dp,
                                                        if (delta >= 0) MorCamTokens.RimGradient else MorCamTokens.DestructiveRimGradient,
                                                        EmotionPillShape
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${if (delta >= 0) "+" else ""}$delta sevgi değişimi",
                                                    fontFamily = InterFontFamily,
                                                    color = if (delta >= 0) MorCamTokens.SilverLavenderBright else MorCamTokens.DestructiveText,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(10.dp))

                                        // Lavender-Silver Line Chart with drawWithCache -> 0 per-frame Path/Brush allocations!
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(104.dp)
                                                .clip(EmotionInnerCardShape)
                                                .background(Color(0xBB0D051F))
                                                .border(0.95.dp, MorCamTokens.SubtleRimGradient, EmotionInnerCardShape)
                                                .drawWithCache {
                                                    val w = size.width
                                                    val h = size.height
                                                    val maxVal = 100f
                                                    val pad = 16f
                                                    val usableH = (h - pad * 2f).coerceAtLeast(1f)
                                                    val stepX = if (recentAff.size > 1) w / (recentAff.size - 1) else w
                                                    val affPts = recentAff.mapIndexed { i, e ->
                                                        Offset(i * stepX, h - (e.affectionScore.coerceIn(0, 100) / maxVal) * usableH - pad)
                                                    }
                                                    val trPts = recentAff.mapIndexed { i, e ->
                                                        Offset(i * stepX, h - (e.trustScore.coerceIn(0, 100) / maxVal) * usableH - pad)
                                                    }
                                                    val fillPath = Path().apply {
                                                        moveTo(affPts.first().x, h)
                                                        affPts.forEach { lineTo(it.x, it.y) }
                                                        lineTo(affPts.last().x, h)
                                                        close()
                                                    }
                                                    val fillBrush = Brush.verticalGradient(
                                                        listOf(Color(0x55A855F7), Color(0x00A855F7)),
                                                        startY = 0f,
                                                        endY = h
                                                    )
                                                    val guideYs = floatArrayOf(
                                                        h - 0.25f * usableH - pad,
                                                        h - 0.50f * usableH - pad,
                                                        h - 0.75f * usableH - pad
                                                    )
                                                    onDrawBehind {
                                                        for (gy in guideYs) {
                                                            drawLine(Color(0x22E9D5FF), Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                                                        }
                                                        drawPath(fillPath, fillBrush)
                                                        for (i in 0 until affPts.size - 1) {
                                                            drawLine(Color(0xFFE879F9), affPts[i], affPts[i + 1], strokeWidth = 2.6f, cap = StrokeCap.Round)
                                                        }
                                                        for (i in 0 until trPts.size - 1) {
                                                            drawLine(Color(0xFFC4B5FD), trPts[i], trPts[i + 1], strokeWidth = 2f, cap = StrokeCap.Round)
                                                        }
                                                        for (i in affPts.indices) {
                                                            val pt = affPts[i]
                                                            val dotColor = if (i > 0 && recentAff[i].affectionScore >= recentAff[i - 1].affectionScore) {
                                                                MorCamTokens.LavenderLight
                                                            } else {
                                                                MorCamTokens.DestructiveRimAccent
                                                            }
                                                            drawCircle(dotColor, 4.2f, pt)
                                                            drawCircle(Color.White, 2f, pt)
                                                        }
                                                    }
                                                }
                                        )

                                        Spacer(Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFE879F9)))
                                                Spacer(Modifier.width(5.dp))
                                                Text("Sevgi", fontFamily = InterFontFamily, color = MorCamTokens.LavenderLight, fontSize = 10.5.sp)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFC4B5FD)))
                                                Spacer(Modifier.width(5.dp))
                                                Text("Güven", fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 10.5.sp)
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))
                                    Text("Son Olaylar", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        recentEvents.forEach { h ->
                                            key("${h.timestamp}_${h.dominantEmotion}_${h.affectionScore}") {
                                                val moodEmoji = emotionHistoryEmoji(h.dominantEmotion)
                                                val formattedLabel = formatTurkishEmotionLabel(h.dominantEmotion)
                                                val formattedDate = remember(h.timestamp) {
                                                    historyDateFormat.format(Date(h.timestamp))
                                                }
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(EmotionHistoryItemShape)
                                                        .background(Color(0x99120728))
                                                        .border(0.8.dp, MorCamTokens.SubtleRimGradient, EmotionHistoryItemShape)
                                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(moodEmoji, color = MorCamTokens.LavenderSoft, fontSize = 15.sp)
                                                        Spacer(Modifier.width(8.dp))
                                                        Column {
                                                            Text(
                                                                text = formattedLabel,
                                                                fontFamily = LiterarySerifFontFamily,
                                                                color = MorCamTokens.SilverLavenderBright,
                                                                fontSize = 12.5.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            Text(
                                                                text = formattedDate,
                                                                fontFamily = InterFontFamily,
                                                                color = MorCamTokens.LavenderSubtle,
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }
                                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                        Text("Sevgi ${h.affectionScore}", fontFamily = InterFontFamily, color = Color(0xFFF0ABFC), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                        Text("Güven ${h.trustScore}", fontFamily = InterFontFamily, color = Color(0xFFC4B5FD), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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

            // Footer Actions
            Spacer(Modifier.height(14.dp))
            MorCamDivider()
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MorCamCapsuleButton(
                    text = "İptal",
                    onClick = onDismiss,
                    variant = MorCamButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                )
                MorCamCapsuleButton(
                    text = "Duyguları Kaydet",
                    onClick = {
                        characterStatesMap["main"]?.let { onSaveBotEmotion(it.toJson()) }
                        characterStatesMap.forEach { (charKey, state) ->
                            if (charKey != "main") onSaveCharacterEmotionState(charKey, state.toJson())
                        }
                        onDismiss()
                    },
                    variant = MorCamButtonVariant.Primary,
                    icon = Icons.Default.Tune,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }

    if (showAddCustomDialog) {
        MorCamDialogShell(
            onDismissRequest = { showAddCustomDialog = false },
            title = "Özel Duygu Ekle",
            subtitle = "Karaktere özel takip sayacı tanımla",
            icon = Icons.Default.AutoAwesome
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Duygu Adı", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp)
                MorCamGlassTextField(
                    value = newCustomName,
                    onValueChange = { newCustomName = it },
                    placeholder = "ör. Sadakat, Şüphe, Saygı..."
                )

                Text("Kazanım Zorluğu", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EmotionStudioDifficulties.forEach { diff ->
                        MorCamChipPill(
                            label = diff,
                            selected = newCustomDifficulty == diff,
                            onClick = { newCustomDifficulty = diff }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Min", fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 11.sp)
                        Spacer(Modifier.height(4.dp))
                        MorCamGlassTextField(
                            value = newCustomMin,
                            onValueChange = { newCustomMin = it.filter { c -> c.isDigit() } },
                            placeholder = "0"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Max", fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 11.sp)
                        Spacer(Modifier.height(4.dp))
                        MorCamGlassTextField(
                            value = newCustomMax,
                            onValueChange = { newCustomMax = it.filter { c -> c.isDigit() } },
                            placeholder = "100"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Başlangıç", fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 11.sp)
                        Spacer(Modifier.height(4.dp))
                        MorCamGlassTextField(
                            value = newCustomCurrent,
                            onValueChange = { newCustomCurrent = it.filter { c -> c.isDigit() } },
                            placeholder = "50"
                        )
                    }
                }

                Text("Duygunun Amacı / Etkisi", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp)
                MorCamGlassTextField(
                    value = newCustomPurpose,
                    onValueChange = { newCustomPurpose = it },
                    placeholder = "ör. Karakterin sadakat seviyesini ve kararlarını yönlendirir...",
                    singleLine = false,
                    minHeight = 68.dp
                )

                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MorCamCapsuleButton(
                        text = "İptal",
                        onClick = { showAddCustomDialog = false },
                        variant = MorCamButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MorCamCapsuleButton(
                        text = "Duygu Ekle",
                        onClick = {
                            val trimmedName = newCustomName.trim()
                            if (trimmedName.isNotEmpty()) {
                                val (minVal, maxVal, curVal) = sanitizeCustomEmotionRange(
                                    rawMin = newCustomMin,
                                    rawMax = newCustomMax,
                                    rawCurrent = newCustomCurrent
                                )
                                val newDef = com.example.data.local.CustomEmotionDefinition(
                                    name = trimmedName,
                                    difficulty = newCustomDifficulty,
                                    minValue = minVal,
                                    maxValue = maxVal,
                                    currentValue = curVal,
                                    purpose = newCustomPurpose.trim()
                                )
                                val updatedList = curState.customEmotions + newDef
                                characterStatesMap[selectedTab] = curState.copy(
                                    customEmotionsJson = com.example.data.local.CustomEmotionDefinition.listToJsonArrayStr(updatedList)
                                )
                                showAddCustomDialog = false
                                newCustomName = ""
                                newCustomPurpose = ""
                            }
                        },
                        enabled = newCustomName.isNotBlank(),
                        variant = MorCamButtonVariant.Primary,
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}
