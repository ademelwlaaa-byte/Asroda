package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.animation.CosmicAnimatedCounter
import com.example.ui.animation.CosmicMotionSpecs
import com.example.ui.animation.cosmicStaggeredEnter
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AffectionEventEntity
import com.example.data.local.BotEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionState
import com.example.data.local.WorldState
import com.example.data.local.PendingReappraisalEntity
import com.example.data.local.SensitiveTriggerEntity
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily

@Composable
fun EmotionStatusSection(
    bot: BotEntity,
    characterEmotions: List<CharacterEmotionEntity> = emptyList(),
    affectionEvents: List<AffectionEventEntity> = emptyList(),
    filterCount: Int = 0,
    onUpdateCharacterEmotion: ((characterName: String, mood: String, affection: Int, trust: Int, tension: Int) -> Unit)? = null,
    onDeleteCharacter: ((characterName: String) -> Unit)? = null,
    sensitiveTriggers: List<SensitiveTriggerEntity> = emptyList(),
    pendingReappraisals: List<PendingReappraisalEntity> = emptyList(),
    onAddTrigger: ((String) -> Unit)? = null,
    onDeleteTrigger: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val emotion = remember(bot.emotionState) { EmotionState.fromJson(bot.emotionState) }
    val isUniverse = bot.mode == "universe"
    val worldState = remember(bot.worldAtmosphere) { WorldState.fromJson(bot.worldAtmosphere) }
    var showTimeline by remember { mutableStateOf(false) }

    MorCamGlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        highlighted = false,
        paddingHorizontal = 16.dp,
        paddingVertical = 14.dp
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0x998B5CF6), Color(0x663B1470))))
                        .border(1.dp, MorCamTokens.RimGradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUniverse) Icons.Default.Public else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MorCamTokens.SilverLavenderBright,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isUniverse) "Evren & Atmosfer" else "Duygu & İlişki",
                        fontFamily = DisplaySerifFontFamily,
                        color = MorCamTokens.SilverLavenderBright,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isUniverse) "Dünya simülasyonu aktif" else "Gerçek zamanlı takip",
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.LavenderMuted,
                        fontSize = 10.5.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MorCamTokens.SecondaryButtonFill)
                    .border(BorderStroke(1.dp, MorCamTokens.RimGradient), RoundedCornerShape(50))
                    .padding(horizontal = 11.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${emotion.getMoodEmoji()} ${emotion.mood.replaceFirstChar { it.uppercase() }}",
                    fontFamily = LiterarySerifFontFamily,
                    color = MorCamTokens.SilverLavenderBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        MorCamDivider()
        Spacer(modifier = Modifier.height(12.dp))

        // Filter Stats Pill Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.horizontalGradient(listOf(Color(0x881B0B3B), Color(0x99110626))))
                .border(0.9.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MorCamTokens.LavenderSoft,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Filtre İstatistiği",
                    fontFamily = LiterarySerifFontFamily,
                    color = MorCamTokens.LavenderLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (filterCount > 0) MorCamTokens.DestructiveButtonFill
                        else Brush.horizontalGradient(listOf(Color(0x667C3AED), Color(0x664C1D95)))
                    )
                    .border(
                        0.85.dp,
                        if (filterCount > 0) MorCamTokens.DestructiveRimGradient else MorCamTokens.RimGradient,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (filterCount > 0) "$filterCount tetiklendi" else "Temiz",
                    fontFamily = InterFontFamily,
                    color = if (filterCount > 0) MorCamTokens.DestructiveText else MorCamTokens.SilverLavenderBright,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Metrics — cohesive lavender-amethyst-periwinkle-rose palette
        EmotionBarItem(
            label = "Yakınlık Kademesi: ${emotion.getAffectionTierLabel()}",
            value = emotion.affection,
            maxValue = 100,
            color = Color(0xFFE879F9),
            icon = "✦"
        )
        Spacer(modifier = Modifier.height(8.dp))

        EmotionBarItem(
            label = "Güven Seviyesi (Trust)",
            value = emotion.trust,
            maxValue = 100,
            color = Color(0xFFC4B5FD),
            icon = "◆"
        )
        Spacer(modifier = Modifier.height(8.dp))

        EmotionBarItem(
            label = "Fiziksel Yakınlık: ${emotion.getPhysicalComfortTierLabel()}",
            value = emotion.physicalComfortScore,
            maxValue = 100,
            color = Color(0xFFD8B4FE),
            icon = "✧"
        )
        Spacer(modifier = Modifier.height(8.dp))

        EmotionBarItem(
            label = "Kırgınlık / Mesafe (Hurt)",
            value = emotion.hurt,
            maxValue = 100,
            color = Color(0xFF818CF8),
            icon = "◇"
        )
        Spacer(modifier = Modifier.height(8.dp))

        EmotionBarItem(
            label = "Gerginlik / Stres (${emotion.tension})",
            value = when (emotion.tension.lowercase()) { "crisis" -> 100; "conflict" -> 50; else -> 0 },
            maxValue = 100,
            color = Color(0xFFF472B6),
            icon = "⚡"
        )

        if (emotion.obsession >= 50) {
            Spacer(modifier = Modifier.height(8.dp))
            EmotionBarItem(
                label = "Takıntı / Bağımlılık (Obsession)",
                value = emotion.obsession,
                maxValue = 100,
                color = MorCamTokens.DestructiveRimAccent,
                icon = "✦"
            )
        }

        // User Defined Custom Emotions
        if (emotion.customEmotions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Özel Tanımlı Duygular",
                fontFamily = LiterarySerifFontFamily,
                color = MorCamTokens.LavenderSoft,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            emotion.customEmotions.forEach { ce ->
                Spacer(modifier = Modifier.height(6.dp))
                val labelText = if (ce.purpose.isNotBlank()) "${ce.name} (${ce.difficulty}) — ${ce.purpose}" else "${ce.name} (${ce.difficulty})"
                EmotionBarItem(
                    label = labelText,
                    value = ce.currentValue.coerceIn(ce.minValue, ce.maxValue),
                    maxValue = ce.maxValue.coerceAtLeast(ce.minValue + 1),
                    color = MorCamTokens.AmethystGlow,
                    icon = "✦"
                )
            }
        }

        // Secondary & Suppressed Emotions
        if (emotion.secondaryMood.isNotBlank() || emotion.suppressedEmotion.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x8814082C))
                    .border(0.8.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                if (emotion.secondaryMood.isNotBlank()) {
                    Text(
                        text = "İkincil Duygu: ${emotion.secondaryMood}",
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.LavenderLight,
                        fontSize = 11.5.sp
                    )
                }
                if (emotion.suppressedEmotion.isNotBlank()) {
                    if (emotion.secondaryMood.isNotBlank()) Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bastırılmış İç Duygu: ${emotion.suppressedEmotion}",
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.DestructiveRimLight,
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // Mood Intensity & Resilience & Speech Pattern
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Duygu Şiddeti: ${emotion.intensity}/10", fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 11.sp)
            Text("Duygusal Direnç: ${emotion.resilience}/10", fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 11.sp)
        }

        if (emotion.speechPattern.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Konuşma Üslubu: ${emotion.speechPattern}",
                fontFamily = InterFontFamily,
                color = MorCamTokens.LavenderLight,
                fontSize = 11.sp
            )
        }

        // Universe Mode Specifics
        if (isUniverse) {
            Spacer(modifier = Modifier.height(12.dp))
            MorCamDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Evren & Atmosfer Simülasyonu",
                fontFamily = DisplaySerifFontFamily,
                color = MorCamTokens.SilverLavenderBright,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mekan: ${worldState.meso.currentLocation.ifBlank { "Bilinmiyor" }}",
                    fontFamily = InterFontFamily,
                    color = MorCamTokens.LavenderLight,
                    fontSize = 11.5.sp
                )
                Text(
                    text = "Gerilim: ${worldState.micro.sceneTension.uppercase()}",
                    fontFamily = InterFontFamily,
                    color = MorCamTokens.LavenderMuted,
                    fontSize = 11.5.sp
                )
            }

            if (worldState.macro.eraRules.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Makro Kurallar: ${worldState.macro.eraRules}",
                    fontFamily = InterFontFamily,
                    color = MorCamTokens.LavenderMuted,
                    fontSize = 11.sp
                )
            }

            if (worldState.meso.locationPersistentNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mekan Notları: ${worldState.meso.locationPersistentNotes}",
                    fontFamily = InterFontFamily,
                    color = MorCamTokens.LavenderMuted,
                    fontSize = 11.sp
                )
            }

            if (worldState.micro.sceneMood.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sahne Havası: ${worldState.micro.sceneMood}",
                    fontFamily = InterFontFamily,
                    color = MorCamTokens.LavenderSoft,
                    fontSize = 11.sp
                )
            }
        }

        // Character Emotions List & Manual Editor
        if (characterEmotions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            MorCamDivider()
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Yan Karakterler Duygu Durumu",
                fontFamily = DisplaySerifFontFamily,
                color = MorCamTokens.SilverLavenderBright,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            var editingCharacter by remember { mutableStateOf<CharacterEmotionEntity?>(null) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                characterEmotions.forEach { charEntity ->
                    val charEmotion = remember(charEntity.emotionState) { EmotionState.fromJson(charEntity.emotionState) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.verticalGradient(listOf(Color(0x9E200F42), Color(0xB0120729))))
                            .border(0.9.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(14.dp))
                            .clickable(enabled = onUpdateCharacterEmotion != null) {
                                editingCharacter = charEntity
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = charEntity.characterName,
                                    fontFamily = LiterarySerifFontFamily,
                                    color = MorCamTokens.SilverLavenderBright,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (onUpdateCharacterEmotion != null) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Düzenle",
                                        tint = MorCamTokens.LavenderSoft,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${charEmotion.getMoodEmoji()} ${charEmotion.mood}",
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.LavenderLight,
                                    fontSize = 11.sp
                                )
                                if (onDeleteCharacter != null) {
                                    MorCamCircleIconButton(
                                        icon = Icons.Default.Delete,
                                        contentDescription = "Karakteri sil",
                                        onClick = { onDeleteCharacter(charEntity.characterName) },
                                        size = 36.dp,
                                        isDestructive = true
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Sevgi: %${charEmotion.affection}", fontFamily = InterFontFamily, color = Color(0xFFF0ABFC), fontSize = 10.5.sp)
                            Text("Güven: %${charEmotion.trust}", fontFamily = InterFontFamily, color = Color(0xFFC4B5FD), fontSize = 10.5.sp)
                            Text("Gerginlik: ${charEmotion.tension}", fontFamily = InterFontFamily, color = MorCamTokens.LavenderMuted, fontSize = 10.5.sp)
                        }
                        if (charEmotion.customEmotions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Özel: " + charEmotion.customEmotions.joinToString(", ") { ce -> "${ce.name}: ${ce.currentValue}/${ce.maxValue}" },
                                fontFamily = InterFontFamily,
                                color = MorCamTokens.LavenderSoft,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            editingCharacter?.let { charEntity ->
                val currentEmotion = remember(charEntity.emotionState) { EmotionState.fromJson(charEntity.emotionState) }
                var editMood by remember(charEntity) { mutableStateOf(currentEmotion.mood) }
                var editAffection by remember(charEntity) { mutableStateOf(currentEmotion.affection.toFloat()) }
                var editTrust by remember(charEntity) { mutableStateOf(currentEmotion.trust.toFloat()) }
                var editTension by remember(charEntity) { mutableStateOf(currentEmotion.tension.toIntOrNull() ?: 10) }

                MorCamDialogShell(
                    onDismissRequest = { editingCharacter = null },
                    title = "${charEntity.characterName} Duygu Ayarı",
                    subtitle = "Yan karakterin anlık ruh hali ve bağ değerleri",
                    icon = Icons.Default.Psychology
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Ruh Hali (Mood)", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp)
                        MorCamGlassTextField(
                            value = editMood,
                            onValueChange = { editMood = it },
                            placeholder = "ör. sakin, meraklı, mesafeli..."
                        )

                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sevgi", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderLight, fontSize = 12.sp)
                                Text("%${editAffection.toInt()}", fontFamily = InterFontFamily, color = Color(0xFFF0ABFC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            MorCamGlassSlider(
                                value = editAffection,
                                onValueChange = { editAffection = it },
                                valueRange = 0f..100f,
                                accentColor = Color(0xFFE879F9),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Güven", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderLight, fontSize = 12.sp)
                                Text("%${editTrust.toInt()}", fontFamily = InterFontFamily, color = Color(0xFFC4B5FD), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            MorCamGlassSlider(
                                value = editTrust,
                                onValueChange = { editTrust = it },
                                valueRange = 0f..100f,
                                accentColor = MorCamTokens.LavenderSoft,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gerginlik", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderLight, fontSize = 12.sp)
                                Text("%$editTension", fontFamily = InterFontFamily, color = MorCamTokens.DestructiveRimLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            MorCamGlassSlider(
                                value = editTension.toFloat(),
                                onValueChange = { editTension = it.toInt() },
                                valueRange = 0f..100f,
                                accentColor = MorCamTokens.DestructiveRimAccent,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MorCamCapsuleButton(
                                text = "İptal",
                                onClick = { editingCharacter = null },
                                variant = MorCamButtonVariant.Secondary,
                                modifier = Modifier.weight(1f)
                            )
                            MorCamCapsuleButton(
                                text = "Kaydet",
                                onClick = {
                                    onUpdateCharacterEmotion?.invoke(
                                        charEntity.characterName,
                                        editMood,
                                        editAffection.toInt(),
                                        editTrust.toInt(),
                                        editTension
                                    )
                                    editingCharacter = null
                                },
                                variant = MorCamButtonVariant.Primary,
                                modifier = Modifier.weight(1.1f)
                            )
                        }
                    }
                }
            }
        }

        val formattedDate = remember(bot.updatedAt) {
            try {
                val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.forLanguageTag("tr-TR"))
                sdf.format(java.util.Date(if (bot.updatedAt > 0) bot.updatedAt else System.currentTimeMillis()))
            } catch (e: Exception) {
                ""
            }
        }
        if (formattedDate.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Son Güncelleme: $formattedDate",
                    fontFamily = InterFontFamily,
                    color = MorCamTokens.LavenderSubtle,
                    fontSize = 10.sp
                )
            }
        }

        // System #22: Active Sensitive Triggers & Open Emotional Reappraisals
        val openReappraisals = remember(pendingReappraisals) {
            pendingReappraisals.filter { !it.resolved && !it.expired }
        }
        if (sensitiveTriggers.isNotEmpty() || openReappraisals.isNotEmpty() || onAddTrigger != null) {
            Spacer(modifier = Modifier.height(12.dp))
            MorCamDivider()
            Spacer(modifier = Modifier.height(10.dp))
            SensitiveTriggersAndReappraisalsCardContent(
                sensitiveTriggers = sensitiveTriggers,
                openReappraisals = openReappraisals,
                onAddTrigger = onAddTrigger,
                onDeleteTrigger = onDeleteTrigger
            )
        }

        // Relationship History Timeline Section
        Spacer(modifier = Modifier.height(12.dp))
        MorCamDivider()
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.horizontalGradient(listOf(Color(0x99200E44), Color(0xAA13072A))))
                .border(0.95.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(14.dp))
                .clickable { showTimeline = !showTimeline }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "İlişki Zaman Çizelgesi",
                    fontFamily = LiterarySerifFontFamily,
                    color = MorCamTokens.SilverLavenderBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (affectionEvents.isNotEmpty()) {
                    Text(
                        text = "${affectionEvents.size} kayıt",
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.LavenderMuted,
                        fontSize = 10.5.sp
                    )
                }
            }
            val chevronRot by animateFloatAsState(
                targetValue = if (showTimeline) 180f else 0f,
                animationSpec = CosmicMotionSpecs.snappyOrb(),
                label = "timelineChevronRot"
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0x40A855F7))
                    .border(0.8.dp, MorCamTokens.SubtleRimGradient, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Aç/Kapat",
                    tint = MorCamTokens.LavenderLight,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = chevronRot }
                )
            }
        }

        AnimatedVisibility(
            visible = showTimeline,
            enter = expandVertically(CosmicMotionSpecs.velvetGlass()) + fadeIn(tween(220)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(140))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(8.dp))
            if (affectionEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x88110626))
                        .border(0.8.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz ilişki kırılması veya yakınlık değişimi kaydedilmedi.",
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.LavenderMuted,
                        fontSize = 11.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    affectionEvents.forEach { ev ->
                        val isPos = ev.scoreDelta > 0
                        val badgeText = if (isPos) "+${ev.scoreDelta}" else "${ev.scoreDelta}"
                        val dateStr = try {
                            val sdf = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.forLanguageTag("tr-TR"))
                            sdf.format(java.util.Date(ev.timestamp))
                        } catch (e: Exception) { "" }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.verticalGradient(listOf(Color(0x941C0D3B), Color(0xAA110626))))
                                .border(
                                    0.85.dp,
                                    if (isPos) MorCamTokens.SubtleRimGradient else MorCamTokens.DestructiveRimGradient,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isPos) Color(0x558B5CF6) else Color(0x556B163A))
                                    .border(
                                        0.8.dp,
                                        if (isPos) MorCamTokens.LavenderSoft else MorCamTokens.DestructiveRimAccent,
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontFamily = InterFontFamily,
                                    color = if (isPos) MorCamTokens.SilverLavenderBright else MorCamTokens.DestructiveText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.shortDescription,
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.SilverLavenderBright,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.5.sp
                                )
                                if (dateStr.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dateStr,
                                        fontFamily = InterFontFamily,
                                        color = MorCamTokens.LavenderSubtle,
                                        fontSize = 9.5.sp
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

/**
 * Sistem #22: Hassas Tetikleyiciler & Açık Duygusal Olgunlaşma rozet/yönetim bloğu.
 * Hem [EmotionStatusSection] hem [BotSettingsModal] tarafından ortak kullanılır.
 */
@Composable
fun SensitiveTriggersAndReappraisalsCardContent(
    sensitiveTriggers: List<SensitiveTriggerEntity>,
    openReappraisals: List<PendingReappraisalEntity>,
    onAddTrigger: ((String) -> Unit)? = null,
    onDeleteTrigger: ((Long) -> Unit)? = null
) {
    var newTriggerText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "💔 Hassas Tetikleyiciler & İçsel Olgunlaşma",
            fontFamily = DisplaySerifFontFamily,
            color = MorCamTokens.SilverLavenderBright,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (sensitiveTriggers.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                sensitiveTriggers.forEach { trig ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0x664C1D95), Color(0x771E0B38))))
                            .border(0.85.dp, MorCamTokens.DestructiveRimGradient, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ ${trig.triggerTopic}",
                                fontFamily = LiterarySerifFontFamily,
                                color = MorCamTokens.SilverLavenderBright,
                                fontSize = 12.2.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (trig.sourceDescription.isNotBlank()) {
                                Text(
                                    text = trig.sourceDescription,
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.LavenderMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                        if (onDeleteTrigger != null) {
                            MorCamCircleIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Tetikleyiciyi Sil",
                                onClick = { onDeleteTrigger(trig.id) },
                                size = 36.dp,
                                isDestructive = true,
                                modifier = Modifier.testTag("delete_trigger_${trig.id}")
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = "Tanımlı hassas tetikleyici yok.",
                fontFamily = InterFontFamily,
                color = MorCamTokens.LavenderMuted,
                fontSize = 11.sp
            )
        }

        if (onAddTrigger != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MorCamGlassTextField(
                    value = newTriggerText,
                    onValueChange = { newTriggerText = it },
                    placeholder = "Yeni tetikleyici ekle (ör. ihanet, terk edilme)",
                    singleLine = true,
                    minHeight = 46.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_trigger_input")
                )
                MorCamCircleIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Tetikleyici Ekle",
                    onClick = {
                        val trimmed = newTriggerText.trim()
                        if (trimmed.isNotBlank()) {
                            onAddTrigger(trimmed)
                            newTriggerText = ""
                        }
                    },
                    size = 48.dp,
                    isActive = newTriggerText.isNotBlank(),
                    modifier = Modifier.testTag("add_trigger_button")
                )
            }
        }

        if (openReappraisals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Açık İçsel Hesaplaşmalar (${openReappraisals.size})",
                fontFamily = LiterarySerifFontFamily,
                color = MorCamTokens.LavenderSoft,
                fontSize = 11.8.sp,
                fontWeight = FontWeight.SemiBold
            )
            openReappraisals.take(3).forEach { reapp ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0x557C3AED), Color(0x6616072C))))
                        .border(0.85.dp, MorCamTokens.RimGradient, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💠",
                        fontSize = 12.sp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = reapp.originalEventDescription,
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 11.5.sp,
                            lineHeight = 15.5.sp
                        )
                        Text(
                            text = "Açık yara #${reapp.id} • Doğal olgunlaşma bekleniyor",
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmotionBarItem(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    icon: String
) {
    val targetFraction = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
    var animTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(targetFraction) { animTarget = targetFraction }
    val animatedFraction by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "barFill"
    )

    val colorLight = Color(
        red = (color.red + 0.25f).coerceAtMost(1f),
        green = (color.green + 0.22f).coerceAtMost(1f),
        blue = (color.blue + 0.25f).coerceAtMost(1f),
        alpha = 1f
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(icon, color = colorLight, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, fontFamily = InterFontFamily, color = MorCamTokens.LavenderLight, fontSize = 11.5.sp)
            }
            CosmicAnimatedCounter(
                count = value,
                color = colorLight,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(5.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
        ) {
            val trackH = size.height
            val trackW = size.width
            val cr = CornerRadius(trackH / 2f, trackH / 2f)

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xAA1D0E3B), Color(0xCC110626))
                ),
                size = Size(trackW, trackH),
                cornerRadius = cr
            )

            drawRoundRect(
                color = color.copy(alpha = 0.14f),
                size = Size(trackW, trackH),
                cornerRadius = cr
            )

            if (animatedFraction > 0f) {
                val fillW = trackW * animatedFraction

                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(colorLight, color),
                        startX = 0f,
                        endX = fillW
                    ),
                    size = Size(fillW, trackH),
                    cornerRadius = cr
                )

                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.40f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = fillW
                    ),
                    size = Size(fillW, trackH * 0.45f),
                    cornerRadius = CornerRadius(trackH * 0.45f / 2f)
                )

                if (fillW > trackH) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.85f),
                                colorLight.copy(alpha = 0.38f),
                                Color.Transparent
                            ),
                            center = Offset(fillW - trackH / 2f, trackH / 2f),
                            radius = trackH * 1.1f
                        ),
                        radius = trackH * 1.1f,
                        center = Offset(fillW - trackH / 2f, trackH / 2f)
                    )
                }
            }
        }
    }
}
